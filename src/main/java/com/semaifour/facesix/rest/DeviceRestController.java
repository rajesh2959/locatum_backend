package com.semaifour.facesix.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.account.rest.CaptivePortalRestController;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.captive.portal.CaptivePortalService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.ClientDevice;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.qubercast.QuberCast;
import com.semaifour.facesix.data.qubercast.QuberCastService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.spring.SpringComponentUtils;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.DeviceHelper;
import com.semaifour.facesix.util.EmailService;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;
import com.semaifour.facesix.probe.oui.ProbeOUIService;
import com.semaifour.facesix.service.NetworkConfService;

/**
 * 
 * Rest Device Controller handles all rest calls
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/device")
public class DeviceRestController extends WebController {
	
	static Logger LOG = LoggerFactory.getLogger(DeviceRestController.class.getName());
	
	@Autowired
	DeviceService deviceManager;

	@Autowired
	DeviceEventPublisher deviceEventMqttPub;
	
	@Autowired
	ClientDeviceService 	clientDeviceService;	
	
	@Autowired
	QuberCastService qubercastService;
	
	@Autowired
	private DeviceEventPublisher mqttPublisher;
	
	@Autowired
	NetworkConfRestController networkConfRestController;
	
	@Autowired
	FSqlRestController fsqlRestController;
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	CustomerUtils customerUtils;
	
	@Autowired
	QubercommScannerRestController qubercommScannerRestController;

	@Autowired
	ProbeOUIService probeOUIService;
	
	@Autowired
	NetworkDeviceRestController networkDeviceRestController;
	
	@Autowired
	CaptivePortalService captivePortalService;
	
	@Autowired
	CaptivePortalRestController  captivePortalRestController;
	
	@Autowired
	private BeaconDeviceService beaconDeviceService;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private PortionService portionService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private NetworkConfService networkConfService;
	
	private String 	prop_event_table_index = "facesix-prop-client-event";
	
	private String 	device_history_event = "device-history-event";
	
	@PostConstruct
	public void init() {
		device_history_event   = _CCC.properties.getProperty("facesix.device.event.history.table",device_history_event);
		prop_event_table_index = _CCC.properties.getProperty("facesix.data.prop.event.table",
				prop_event_table_index);

	}
	
	String mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\",\"ap\":\"{2}\",\"mac\":\"{3}\", \"by\":\"{4}\"";
	
	String reffId="a5a5";

	static Font smallBold 	= new Font(Font.FontFamily.TIMES_ROMAN, 12, 	Font.BOLD);
    static Font catFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 18, 	Font.BOLD);
    static Font redFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 12, 	Font.NORMAL);
    static Font subFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 16,     Font.BOLD);
    static boolean ThreadTobeStarted = false;
    
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public @ResponseBody Iterable<Device> listAll(@RequestParam(value="size", defaultValue="100") String size, 
										@RequestParam(value="page", defaultValue="0") String page) {
		return deviceManager.findAll();
	}
	
	@RequestMapping(value = "/list/{status}", method = RequestMethod.GET)
	public @ResponseBody List<Device> list(@PathVariable("status") String status) {
		Sort sort = new Sort(Sort.Direction.DESC, "modifiedOn");
		List<Device> deviceList = deviceManager.findByStatus(status,sort);
		if (deviceList != null) {
			deviceList.forEach(device -> {
				Date modifiedOn = device.getModifiedOn();
				if (modifiedOn != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					String zoneName = sdf.getTimeZone().getDisplayName(false, 0);
					device.setDescription(sdf.format(modifiedOn)+" "+zoneName);
				}
				device.setUid(device.getUid().toLowerCase());
			});
		}
		return deviceList;
	}
	 
	 @RequestMapping(value = "/info", method = RequestMethod.GET)
	 public @ResponseBody Device info(@RequestParam("uid") String uid) {
			Device device = deviceManager.findOneByUid(uid);
			if (device != null) {
				device.setUid(uid.toLowerCase());
			}
			return device;
	}
	
    @RequestMapping(value = "conf", method = RequestMethod.GET)
    public  String confGet(@RequestParam("uid") String uid) {
		
    	Device device = deviceManager.findOneByUid(uid);
		
		//LOG.info("UID" + uid);
		
		if (device != null && !Device.STATUS.REGISTERED.name().equalsIgnoreCase(device.getStatus())) {
			
			 org.json.simple.JSONObject template = customerUtils.stringToSimpleJson(device.getConf());
			
			device.setUid(uid.toLowerCase());
			
			String cid 		= null;
			String sid 		= null;
			String spid 	= null;
			
			if (device.getCid() != null) {
				cid = device.getCid();
				template.put("cid", cid);
			}
			if (device.getSid() != null) {
				sid = device.getSid();
				template.put("sid", sid);
			}
			if (device.getSpid() != null) {
				spid = device.getSpid();
				template.put("spid", spid);
			}
			
			int balancer = device.getNetwork_balancer();
			template.put("network_balancer", balancer);
			
			String keepalive = device.getKeepAliveInterval()== null ? "30": device.getKeepAliveInterval();
			template.put("keepalive", keepalive);

			String  root = device.getRoot() == null ? "no" : device.getRoot();
			template.put("root", root);
			
			String lanbridge = device.getLanbridge();
			String wanbridge = device.getWanbridge();

			template.put("lan_bridge", lanbridge);
			template.put("wan_bridge", wanbridge);

			deviceManager.pickLanWanConfig(template, device);
	
			return template.toString();
		} else {
			return "{ \"uid\" :\"" + uid + "\" , \"status\":\"NOT_FOUND\" }";
		}			
    }
    
    @RequestMapping(value = "conf", method = RequestMethod.POST)
    public  String confPost(@RequestParam(value="uid", required=true) String uid, 
    						@RequestParam(value="name", required=false) String name, 
    						@RequestBody String conf) {
    	
		Device device = deviceManager.findOneByUid(uid);

		conf = StringUtils.trim(conf);

		String oldConf = conf;

		if (device != null) {
			oldConf = device.getConf();
			device.setConf(conf);
			if (!StringUtils.isEmpty(name)) {
				device.setName(name);
			}
			device.setModifiedOn(new Date());
			deviceManager.save(device);
		} else {
			uid = StringUtils.trim(uid);
			if (StringUtils.isEmpty(name)) {
				name = uid; 
			} else {
				name = StringUtils.trim(name);
			}
			
			device = new Device();
			device.setCreatedOn(new Date());
			device.setUid(uid);
			device.setName(name);
			device.setConf(conf);
			device.setModifiedOn(new Date());
			//device.setIp("0.0.0.0");
		}
		return oldConf;
    }

   @RequestMapping(value = "topology", method = RequestMethod.GET)
    public  String topology(@RequestParam("uid") String uid) {
		Device device = deviceManager.findOneByUid(uid);
		try {
			if (device != null) {
				return DeviceHelper.toJSON4D3Network(device);
			} 
		} catch (Exception e) {
			LOG.warn("Exception parsing device :" + uid, e);
		}
		return "{ \"uid\" :\"" + uid + "\" , \"status\":\"NOT_FOUND\" }";
    }
    
    
    @RequestMapping(value = "rpc", method = RequestMethod.POST)
    public String rpc(@RequestParam(value="uid", required=true) String uid,
    		          @RequestParam(value="ap", required=true) String ap,
    				  @RequestParam(value="mac", required=false) String mac,
    				  @RequestParam(value="cmd", required=true) String cmd, 
    				  @RequestParam(value="args",required=false) String[] args) {
    	
    	String ret = "SUCCESS: RPC Message Sent";
    	//LOG.info("RPC::UID" + uid);
    	//LOG.info("RPC::MAC" + mac);
    	//LOG.info("RPC::AP" + ap);

		try {
			Device device = deviceManager.findOneByUid(uid);
			
			if (device != null && !Device.STATUS.REGISTERED.name().equalsIgnoreCase(device.getStatus())) {
				
				//LOG.info("RPC Status" + device.getStatus());
				String message = MessageFormat.format(mqttMsgTemplate, new Object[]{cmd, 
																			   device.getUid(),
																			   ap,
																			   mac,
																			   "device_update"});
				if (cmd.equals("RESET")) {
					deviceManager.reset(device, true);
				} else {
					deviceEventMqttPub.publish("{" + message + "}", uid);
				}
				
				if (cmd.equals("UNBLOCK")) {
					//LOG.info("Deleted UNBLOCK MACCC" + mac);
					mac = mac.replaceAll("[^a-zA-Z0-9]", "");
					ClientDevice clientDevice = clientDeviceService.findOneByPeermac(mac);
					if (clientDevice != null) {
						clientDeviceService.delete(clientDevice.id);
					}
				}
				
				LOG.info("SUCCESS: RPC Message Sent |uid:" + uid + "|cmd:" + cmd );
				
			} else {
				ret = "FAILURE: Invalid Device";
				LOG.info("Invalid Device" );
			}
		} catch (Exception e) {
			ret = "Error: FATAL error occured";
			LOG.error("FAILURE: RPC Message Failed |uid :"+ uid + "|cmd:" + cmd , e);
			ret = "FAILURE: RPC Message Failed";
		}
    	
    	return ret;
    }
    
    
	@RequestMapping(value = "rpcQcast", method = RequestMethod.POST)
	public String rpcQcast(@RequestParam(value = "uid", required = true) String uid,
						   @RequestParam(value = "ap", required = true) String ap,
						   @RequestParam(value = "mac", required = false) String mac,
						   @RequestParam(value = "cmd", required = true) String cmd,
						   @RequestParam(value = "args", required = false) String[] args) {
		
		String ret = "SUCCESS: RPCQCAST Message Sent";
		//LOG.info("RPCQCAST::UID" + uid);
		//LOG.info("RPCQCAST::MAC" + mac);
		//LOG.info("RPCQCAST::AP" + ap);
		String qcastmqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"by\":\"{2}\", \"newversion\":\"{3}\", \"value\":{4} ";
			
		JSONObject jsonObject = new JSONObject();
		QuberCast quber = qubercastService.findByReffId("a5a5");

		if (quber != null) {
			jsonObject.put("mediaPath", 		quber.getMediaPath());
			jsonObject.put("multicastPort", 	quber.getMulticastPort());
			jsonObject.put("mulicastAddress", 	quber.getMulicastAddress());
			jsonObject.put("totalFiles", 		quber.getLogFile());
			jsonObject.put("payLoad", 			quber.getLogLevel());
		}
					
		if (cmd.equals("KILL") || cmd.equals("REFRESH")) {
			String header = cmd.equals("KILL")?"QCAST_CLOSE":"QCAST_RESTART";
			
			Iterable<Device> devices = deviceManager.findAll();
			
			if (devices != null) {
				for (Device device : devices) {
						String msg = MessageFormat.format(qcastmqttMsgTemplate, new Object[] { header, device.getUid().toLowerCase(),
								"qubercloud", "0xFE", jsonObject.toString() });
						mqttPublisher.publish("{" + msg + "}", device.getUid().toLowerCase());
				}
			}
			
			return "OK";
		}
			

		try {
			
			Device device = deviceManager.findOneByUid(uid);

			if (device != null && !Device.STATUS.REGISTERED.name().equalsIgnoreCase(device.getStatus())) {
				
				if ( cmd.equals("QCAST")|| cmd.equals("QCLOS")|| cmd.equals("QCASTRESET")) {
					String header = cmd.equals("QCAST")?"AP_QCAST_START":"AP_QCAST_CLOSE";
					
					if (cmd.equals("QCASTRESET")) {
						header="AP_QCAST_RESET";
					}
						String msg = MessageFormat.format(qcastmqttMsgTemplate,
								new Object[] { header, device.getUid().toLowerCase(), "qubercloud", "0xFE", jsonObject.toString() });
						mqttPublisher.publish("{" + msg + "}", device.getUid().toLowerCase());
				}

				LOG.info("UID:" + uid + "|cmd:" + cmd +"jsonObject " +jsonObject);

			} else {
				ret = "FAILURE: Invalid Device";
				LOG.info("Invalid Device");
			}
		} catch (Exception e) {
			ret = "Error: FATAL error occured"+e;
		}		


		return ret;
	}
	
		
	@RequestMapping(value = "/cust/dev/list", method = RequestMethod.GET)
	public JSONObject list(
					@RequestParam(value = "cid", required = true) String cid,
					@RequestParam(value = "uid", required = false) String uid,
				    HttpServletRequest request) throws IOException{

		JSONObject devlist = new JSONObject();
		try {
			
			JSONObject dev 				= null;
			JSONArray dev_array 		= new JSONArray();
			List<Device> device 		= new ArrayList<Device>();
				
			if (uid == null || uid.isEmpty() || uid.equals("undefined")) {
				device = deviceManager.findByCid(cid);
			} else {
				Device dv = deviceManager.findByUidAndCid(uid, cid);
				if(dv  != null){
					device.add(dv);
				}else {
					device = deviceManager.findByCidAndAlias(cid, uid);
				}
			}
					
			if (device != null) {
				List<Device> sorted_device = new ArrayList<Device>(device);
				
				if (sorted_device.size() > 1) {
					sorted_device.sort((Device dev1,Device dev2)->dev1.getUid().compareTo(dev2.getUid())); 
				}
				for (Device dv : sorted_device) {
						dev = new JSONObject();
						
						String state 		= (dv.getState() == null )? "inactive" :  dv.getState();
						String deviceType 	= (dv.getTypefs() == null)  ? "ap" : dv.getTypefs();
						String role 		= (dv.getRole() == null) ? "ap" : dv.getRole();
	
						if (deviceType.equals("server") || deviceType.equals("switch")) {
							continue;
						}

						dev.put("id", 		   dv.getId());
						dev.put("mac_address", dv.getUid());
						dev.put("dev_name",    dv.getName());
						dev.put("status",      dv.getStatus());
						dev.put("state",       state);
						dev.put("alias",       dv.getName());
						dev.put("cid",         dv.getCid());
						dev.put("ap_type", 	   role);
						
						String ip  	= dv.getIp();
						
						String devIp = (ip == null || ip.isEmpty()) ? "0.0.0.0" : ip;
						dev.put("ip",           devIp);
						
						if (!state.equalsIgnoreCase("inactive") && !devIp.equals("0.0.0.0")) {
							dev.put("cmd_enable", "1");
						} else {
							dev.put("cmd_enable", "0");
						}
						
						
						dev_array.add(dev);
				}
				devlist.put("cust_dev_list", dev_array);
			}
		} catch (Exception e) {
			LOG.info("while getting customer device list error", e);
		}
		return devlist;
	}

	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public String export(@RequestParam(value = "cid", required = true) String cid,
			  			 HttpServletRequest request, HttpServletResponse response) 
			  		     throws IOException, ParseException {
		
		String pdfFileName  = "./uploads/qubexport.pdf";
		String logoFileName = "./uploads/logo-home.png";
		
		//String pdfFileName  = "C:/files/quber.pdf";
		//String logoFileName = "C:/files/2g-on.png";
		
			
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			Document document = new Document();
			try {
				FileOutputStream os = new FileOutputStream(pdfFileName);
				@SuppressWarnings("unused")
				PdfWriter pdfWriter = PdfWriter.getInstance(document, os);
				document.open();
				Paragraph paragraph = new Paragraph();
				Image image2 = Image.getInstance(logoFileName);
				image2.scaleAbsoluteHeight(25f);//scaleAbsolute(50f, 50f);
				image2.scaleAbsoluteWidth(100f);
				paragraph.add(image2);
				paragraph.setAlignment(Element.ALIGN_LEFT);
				paragraph.add("Qubercloud Log Summary");
				paragraph.setAlignment(Element.ALIGN_CENTER);
				
				addEmptyLine(paragraph, 1);
			    
			    // Will create: Report generated by: _name, _date
			    paragraph.add(new Paragraph("Report generated by: " + System.getProperty("user.name") + ", " + new Date(), smallBold));
			    addEmptyLine(paragraph, 3);	
			    document.add(paragraph);
			    
			    document.newPage();
			    
			    addlogContent (document,cid);			    
				
				document.close();

				File pdfFile = new File(pdfFileName);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
				response.setContentLength((int) pdfFile.length());
				FileInputStream fileInputStream = new FileInputStream(pdfFile);
				OutputStream responseOutputStream = response.getOutputStream();
				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}
				responseOutputStream.close();
				fileInputStream.close();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//return pdfFileName;
		}

		return pdfFileName;
	}
	
	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private void addlogContent(Document document,String cid) throws DocumentException, IOException, ParseException {
		Anchor anchor = new Anchor("Qubercloud LOG Summary", catFont);
		anchor.setName("Qubercloud LOG Summary");

		// Second parameter is the number of the chapter
		Chapter catPart = new Chapter(new Paragraph(anchor), 1);

		Paragraph subPara = new Paragraph("Qubercloud LOGS", subFont);
		addEmptyLine(subPara, 1);

		Section subCatPart = catPart.addSection(subPara);

		// add a table
		createPropReqTable(subCatPart, document,cid);

		// now add all this to the document
		document.add(catPart);

	}
	
	@SuppressWarnings("unchecked")
	private void createPropReqTable(Section subCatPart, Document document,String cid)
			throws IOException, ParseException, DocumentException {
		
		PdfPTable table = new PdfPTable(4);

		PdfPCell c1 = new PdfPCell(new Phrase("PEER MAC"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("STATUS"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("STATE"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("ALIAS"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);
		table.setHeaderRows(1);
		
		table.setHeaderRows(1);

		JSONObject newJObject = null;
		newJObject = list(cid, null, null);

		if (newJObject != null) {
			JSONArray devicelist = (JSONArray) newJObject.get("cust_dev_list");
			//LOG.info("Export file " +devicelist.toString());
			if (devicelist != null) {
				Iterator<JSONObject> i = devicelist.iterator();
				while (i.hasNext()) {
					JSONObject slide =i.next();
					String peer_mac = (String) slide.get("mac_address");
					table.addCell(peer_mac);
					String status = (String) slide.get("status");
					table.addCell(status);
					String state = (String) slide.get("state");
					table.addCell(state);
					String alias = (String) slide.get("alias");
					table.addCell(alias);

				}
			}
		}

		subCatPart.add(table);
	}
	
	// GET DEFAULT CONFIG MESH
	@RequestMapping(value = "/meshdefaultconfig", method = RequestMethod.POST)
	public JSONObject custmeshconfig(
			@RequestParam(value = "band2g", required = false) String band2g,
			@RequestParam(value = "band5g", required = false) String band5g,
			HttpServletRequest request,	HttpServletResponse response) throws IOException {
		JSONObject JSONCONF	=  null;
		try {
			String tconf = null;
			String template = "mesh_default";
			if (band2g.equals("2G") && band5g.equals("5G")) {
				template = "mesh_2G5G";
			} else if (band5g.equals("5G")) {
				template = "mesh_5G";
			} else if (band2g.equals("2G")) {
				template = "mesh_2G";
			} else {
				template = "mesh_default";
			}
			tconf = SpringComponentUtils.getApplicationMessages().getMessage("facesix.device.template."+template);
			JSONCONF= customerUtils.stringToSimpleJson(tconf);
			//LOG.info("MESH CONFIG  tconf " + JSONCONF);
		} catch (Exception e) {
			LOG.info("WHILE CUSTOMER MESH CONFIG ERROR {}", e);
		}
		return JSONCONF;
	}
	
		//GET DEFAULT CONFIG LEGACY
		@RequestMapping(value = "/legacydefaultconfig", method = RequestMethod.POST)
		public JSONObject custlegactest(
				@RequestParam(value = "band2g", required = false) String band2g,
				@RequestParam(value = "band5g", required = false) String band5g,
				HttpServletRequest request,	HttpServletResponse response) throws IOException {
			
			JSONObject JSONCONF	=  null;
			try {
				String tconf = null;
				String template = "default";
				if (band2g.equals("2G") && band5g.equals("5G")) {
					template = "template_2G5G";
				} else if (band5g.equals("5G")) {
					template = "template_5G";
				} else if (band2g.equals("2G")) {
					template = "template_2G";
				}
				 tconf = SpringComponentUtils.getApplicationMessages().getMessage("facesix.device.template."+template);
				 JSONCONF	= customerUtils.stringToSimpleJson(tconf);
				 LOG.info("LEGACY CONFIG  template " + JSONCONF);
			} catch (Exception e) {
				LOG.info("WHILE CUSTOMER LEGACY CONFIG ERROR {}", e);
			}
			return JSONCONF;
		}
		
		@RequestMapping(value = "/uploadconfig", method = RequestMethod.POST)
		public JSONObject jsonFileRead(
						MultipartHttpServletRequest request,
						HttpServletRequest req,	
						HttpServletResponse res) throws IOException {
			
			JSONObject JSONCONF	=  null;
			try {
				
				 Iterator<String> itrator = request.getFileNames();
			     MultipartFile multiFile = request.getFile(itrator.next());
		         
		         String content = new String(multiFile.getBytes(), "UTF-8");
		         JSONCONF	= customerUtils.stringToSimpleJson(content);
		        LOG.info("CONFIG  Template " + JSONCONF);
			} catch (Exception e) {
				LOG.info("WHILE FILE UPLOAD ERROR {}", e);
			}
			return JSONCONF;
		}
		
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public void delete(@RequestParam("uid") String uid, @RequestParam("cid") String cid,
					   @RequestParam(value = "sid",  required = false) String sid,
					   @RequestParam(value = "spid", required = false) String spid,
					   @RequestParam(value = "place",required = false,defaultValue = "gateway") String place,
					   HttpServletRequest request,HttpServletResponse response) throws Exception {

		//LOG.info("Quber SPOT UID " +uid);
		//LOG.info("Quber SPOT CID " +cid);
		
		String str = "/facesix/spots?cid="+cid+"&sid="+sid+"&spid="+spid;
		
		try {
			
			if (place.equals("gwregdevice")) {
				str = "/facesix/gwregdevices?cid="+cid;
			}
			
			Device device = deviceManager.findOneByUid(uid);
			
			String[] stringArray = new String[] { "none" };
			if (device != null) {
				rpc(uid, null, null, "DELETE", stringArray);
				deviceManager.delete(device);
				device.setId(null);
			}
			response.sendRedirect(str);
		} catch (Exception e) {
			response.sendRedirect(str);
			LOG.info("While Device Delete Error " ,e);
		}
		
	}
	
	@RequestMapping(value = "/checkDuplicate", method = RequestMethod.GET)
	public  String checkDuplicate(@RequestParam("uid") String uid, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String retresponse = "new";
		
		if (StringUtils.isEmpty(uid)) {
			LOG.info("uid is empty");
		} else {
			Device device = deviceManager.findOneByUid(uid);
			if (device != null)  {
				if (!Device.STATUS.REGISTERED.name().equalsIgnoreCase(device.getStatus())
						&& device.getUid().equalsIgnoreCase(uid)) {
					retresponse = "duplicate";
				}
			}
		}	
		return retresponse;
	}
	
	@RequestMapping(value = "/deleteall", method = RequestMethod.GET)
	public void deleteall(@RequestParam(value = "cid", required = false) String cid,
						 HttpServletRequest request,
					     HttpServletResponse response) throws IOException{

		if (cid == null || cid.isEmpty()) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		
		String str = "/facesix/gwregdevices?cid="+cid;
	
		try {
			final String status = Device.STATUS.REGISTERED.name();
			
			List<Device> device= deviceManager.findByStatus(status);	
			
			if (device != null) {
				for (Device dv : device) {
					if (status.equals(dv.getStatus())) {
						deviceManager.delete(dv);
					}
				}
			}
			response.sendRedirect(str);
		} catch (Exception e) {
			response.sendRedirect(str);
		}
	}
	
	/*
	 * used to GET the root Node device configuration(2G or 5G)
	 * @param cid used find the root node device
	 * 
	 */
	
	@RequestMapping(value = "/device_config" ,method = RequestMethod.GET)
	public Restponse<JSONArray> deviceConfigList(@RequestParam("cid") final String cid,HttpServletRequest requst) {
		
		JSONArray resulArray 	= new JSONArray();
		JSONObject resultObject = new JSONObject();
		
		boolean sucess = false;
		int code 	   = 401;
		
		if (SessionUtil.isAuthorized(requst.getSession())) {
		
			List<Device> dev = deviceManager.findByCid(cid);
			
			Device rootDevice = null;
			
			if (dev != null && dev.size() > 0) {
				
				for (Device device : dev) {
					String isRootDevice = device.getRoot() == null ? "NA" : device.getRoot();
					if (isRootDevice.equals("yes")) {
						rootDevice = device;
						break;
					} else {
						continue;
					}
				}
				
				if (rootDevice == null) 
					rootDevice =  dev.get(0);
				
				String temp  = rootDevice.getTemplate();
				
				JSONObject template = customerUtils.stringToSimpleJson(temp);
				
				JSONArray interfaces = null;
				
				if (template.containsKey("interfaces2g")) {
					interfaces = (JSONArray)template.get("interfaces2g");
				} else if (template.containsKey("interfaces5g")) {
					interfaces = (JSONArray)template.get("interfaces5g");
				}
				
				if (interfaces != null) {
					interfaces.forEach(item -> {
						JSONObject object = (JSONObject)item;
						String mode 	  = (String)object.get("mode");
						
						if (!mode.equals("mesh")) {
							resultObject.put("key", 		object.get("key"));
							resultObject.put("encryption",  object.get("encryption"));
							resultObject.put("ssid", 		object.get("ssid"));
							resulArray.add(resultObject);
						}
					});
				}
				
				code   = 200;
				sucess = true;
			} else {
				code   = 404;
				sucess = false;
			}
		}
		return new Restponse<JSONArray>(sucess,code,resulArray);
	}
	
	@PostMapping("/changeMyDeviceConfig")
	public Restponse<String> postDeviceConfig(@RequestParam("cid") String cid, @RequestBody String conf,
								   HttpServletRequest requst) {
		
		int code 	    = 401;
		boolean success = false;
		String message  = "UnAuthorized User.";
		
		try {
			
			if (SessionUtil.isAuthorized(requst.getSession())) {
				
				if (conf == null || conf.isEmpty()) {
					code 	= 404;
					message = "Please add atleast one interfacte.";
					return new Restponse<String>(success, code, message);
				}

				List<Device> dev = deviceManager.findByCid(cid);
				
				JSONArray currConf = customerUtils.stringToSimpleJsonArray(conf);
				
				 LOG.info("current Conf " + currConf);
				
				if (dev != null && dev.size() > 0) {
					for (Device device : dev) {
						
						String temp  = device.getTemplate();
						
						JSONObject prevConf = customerUtils.stringToSimpleJson(temp);
						
						LOG.info(" @@@@@@ alias @@@@@@@ "      + device.getName() +
								" prevDeviceConf " + prevConf);
						
						int _2G_Prev_Node_Size = 0;
						int _2G_mesh_Node_Size = 0;
						
						int _5G_mesh_Node_Size = 0;
						int _5G_Prev_Node_Size = 0;
						
						int curr_Node_Size = 0;
						
						JSONObject myNewConf = new JSONObject();
						
						JSONArray newInterfaces2g = new JSONArray();
						JSONArray newInterfaces5g = new JSONArray();
						
						Iterator<JSONObject> curr_node = currConf.iterator();
						while (curr_node.hasNext()) {
							JSONObject obj = curr_node.next();
							curr_Node_Size ++;
						}
						
						LOG.info("curr_Node_Size " + curr_Node_Size);
						
						/*
						 *  2G Interface
						 * 
						 */
						
						if (prevConf.containsKey("interfaces2g")) {
							
							JSONArray interfaces2g  = (JSONArray) prevConf.get("interfaces2g");
							Iterator<JSONObject> it = interfaces2g.iterator();
							
							while (it.hasNext()) {
								JSONObject obj = it.next();
								if (obj.containsKey("mode") && !"mesh".equals(obj.get("mode"))) {
									_2G_Prev_Node_Size++;
								} else {
									_2G_mesh_Node_Size ++;
								}
							}
							
							LOG.info("_2G_Prev_Node_Size " + _2G_Prev_Node_Size + " _2G_mesh_Node_Size " + _2G_mesh_Node_Size);
							

							if (_2G_Prev_Node_Size == curr_Node_Size) {
								this.changeDeviceConfig(interfaces2g, currConf,newInterfaces2g);
								myNewConf.put("radio2g", 	  prevConf.get("radio2g"));
								myNewConf.put("interfaces2g", newInterfaces2g);
								
							} else if (_2G_Prev_Node_Size != curr_Node_Size) {

								if (_2G_Prev_Node_Size > curr_Node_Size) {

									int nodeDiff = _2G_Prev_Node_Size - curr_Node_Size;
									
									LOG.info(" 2G nodeDiff " + nodeDiff + " _2G_Prev_Node_Size " +_2G_Prev_Node_Size);

									int myLogic = _2G_Prev_Node_Size + _2G_mesh_Node_Size;
									
									for (int i = 1; i <= nodeDiff; i++) {

										//int removeLastIndex = _2G_Prev_Node_Size - i;
										
										int removeLastIndex = myLogic -i;
										
										LOG.info(" I " + i + " removeLastIndex " +removeLastIndex);

										JSONObject obj = (JSONObject) interfaces2g.get(removeLastIndex);
										
										if (obj.containsKey("mode")) {
											String mode = (String) obj.get("mode");
											if (!mode.equals("mesh")) {
												interfaces2g.remove(removeLastIndex);
											} else if (mode.equals("mesh")) {
												nodeDiff = nodeDiff + 1;
												LOG.info("MESH NODE 2G ADD THE INDEX " + nodeDiff);
											} else {
												continue;
											}
										}
									}
									this.changeDeviceConfig(interfaces2g, currConf, newInterfaces2g); 
									
								} else {
									for (int i = 0; i < curr_Node_Size; i++) {

										if (i >= _2G_Prev_Node_Size) {

											LOG.info("@@@ FOUND new 2G Interface @@@@@ " + " i " + i);
											
											JSONObject obj = new JSONObject();
											obj.put("mode", "ap");
											
											JSONObject newObj  = (JSONObject)currConf.get(i);
											
											if (newObj == null || newObj.isEmpty()) {
												continue;
											}
											
											String encryption  = (String)newObj.get("encryption");
											String ssid		   = (String)newObj.get("ssid");
											String key         = (String)newObj.get("key");
											
											if (newObj.containsKey("encryption") && encryption.equals("open")) {
												obj.remove("key");
												obj.put("encryption", encryption);
											} else {
												if (newObj.containsKey("key"))
													obj.put("key", key);
												if (newObj.containsKey("encryption"))
													obj.put("encryption", encryption);
											}
											
											obj.put("erp", 			"1");
											obj.put("ssid",		 	ssid);
											obj.put("bcastssid", 	"1");
											obj.put("amsdu", 		"1"); 
											obj.put("fixedrate", 	"1");
											obj.put("multicat_snoop","1");
											obj.put("hotspot", 		"off");
											obj.put("ampdu", 		"1");
											obj.put("mcast", 		"1");
											obj.put("acl", 			"blacklist");
											obj.put("bridge", 		"wan");

											interfaces2g.add(obj);
										}
									}
									this.changeDeviceConfig(interfaces2g, currConf, newInterfaces2g); 
								}

								myNewConf.put("radio2g", prevConf.get("radio2g"));
								myNewConf.put("interfaces2g", newInterfaces2g);
							}

						}
						
						
						/*
						 *  5G Interface
						 * 
						 */

						if (prevConf.containsKey("interfaces5g")) {
							
							JSONArray interfaces5g  = (JSONArray) prevConf.get("interfaces5g");
							Iterator<JSONObject> it = interfaces5g.iterator();
							
							while (it.hasNext()) {
								JSONObject obj = it.next();
								if (obj.containsKey("mode") && !obj.get("mode").equals("mesh")) {
									_5G_Prev_Node_Size ++;
								} else {
									_5G_mesh_Node_Size ++;
								}
							}

							LOG.info("_5G_Prev_Node_Size " + _5G_Prev_Node_Size);
							
							if (_5G_Prev_Node_Size == curr_Node_Size) {
								this.changeDeviceConfig(interfaces5g, currConf,newInterfaces5g);
								myNewConf.put("radio5g", 	  prevConf.get("radio5g"));
								myNewConf.put("interfaces5g", newInterfaces5g);
								
							} else if (_5G_Prev_Node_Size != curr_Node_Size) {

								if (_5G_Prev_Node_Size > curr_Node_Size) {

									int nodeDiff = _5G_Prev_Node_Size - curr_Node_Size;

									LOG.info("5G nodeDiff " + nodeDiff + " 5G_Prev_Node_Size " + _5G_Prev_Node_Size);

									int myLogic = _5G_Prev_Node_Size+_5G_mesh_Node_Size;
									
									for (int i = 1; i <= nodeDiff; i++) {
										
										int removeLastIndex = myLogic - i;
											
										JSONObject obj = (JSONObject) interfaces5g.get(removeLastIndex);
										String mode    = (String) obj.get("mode");
										
										LOG.info("obj " +obj);
										
										if (obj.containsKey("mode")) {
											if (!mode.equals("mesh")) {
												interfaces5g.remove(removeLastIndex);
											} else if (mode.equals("mesh")) {
												nodeDiff = nodeDiff + 1;
												LOG.info("MESH NODE 5G ADD THE INDEX " +nodeDiff);
											} else {
												continue;
											}
										}
										LOG.info("####### removed index 5G #######" + removeLastIndex);
									}
									this.changeDeviceConfig(interfaces5g, currConf, newInterfaces5g);
								} else {
									for (int i = 0; i <curr_Node_Size; i++) {
										
										if (i >= _5G_Prev_Node_Size) {
											
											LOG.info("@@@ FOUND new 5G Interface @@@@@ " + " i " + i);
											
											JSONObject obj = new JSONObject();
											obj.put("mode", "ap");
											
											JSONObject newObj = (JSONObject)currConf.get(i);
											if (newObj == null || newObj.isEmpty()) {
												continue;
											}
											String encryption = (String)newObj.get("encryption");
											
											LOG.info("@@@ newObj 5G index @@@@@ " +newObj);
											
											if (encryption.equals("open")) {
												obj.put("encryption", newObj.get("encryption"));
											} else {
												if (newObj.containsKey("key"))
													obj.put("key", newObj.get("key"));
												if (newObj.containsKey("encryption"))
													obj.put("encryption", newObj.get("encryption"));
											}
											
											obj.put("erp", 			"1");
											obj.put("ssid", 		newObj.get("ssid"));
											obj.put("bcastssid", 	"1");
											obj.put("amsdu", 		"1");
											obj.put("fixedrate", 	"6");
											obj.put("multicat_snoop", "1");
											obj.put("hotspot", 		"off");
											obj.put("ampdu", 		"1");
											obj.put("mcast", 		"6");
											obj.put("acl", 			"blacklist");
											obj.put("bridge", 		"wan");
											
											interfaces5g.add(obj);
											
											LOG.info("interfaces5g " + interfaces5g);
										}
									}
									this.changeDeviceConfig(interfaces5g, currConf, newInterfaces5g); 
								}
								myNewConf.put("radio5g", 	  prevConf.get("radio5g"));
								myNewConf.put("interfaces5g", newInterfaces5g);

							}
						}
						
						LOG.info(" ##### changed config  ######### " + myNewConf);
						
						/* 
						 * First sent the updated config for Root devices
						 * 
						 */
						
						if (!myNewConf.isEmpty() && myNewConf.size() > 0) {
							device.setTemplate(myNewConf.toString());
							device.setConf(myNewConf.toString());
							device.setModifiedBy(SessionUtil.currentUser(requst.getSession()));
							deviceManager.saveAndSendMqtt(device, true);
						}
					}
					
					success = true;
					code 	= 200;
					message = "Configuation has been updated successfully.";
					
				} else {
					success = false;
					code 	= 404;
					message = "Device not found.";
				}
			}

		} catch (Exception e) {
			LOG.error("While Device configuartion update failed " + e);
			success = false;
			code = 500;
			message = "Error occured while update configuration.";
			e.printStackTrace();
		}

		return new Restponse<String>(success, code, message);
	}
	
	/*
	 * This method used to update the basic device configuration (SSID,KEY and Encryption)
	 * @ previousConfig device configuration
	 * @ currentConfig current configuration
	 * 
	 */
	
	public JSONArray changeDeviceConfig(JSONArray previousConfig,JSONArray currentConfig,JSONArray newInterface) {
		
		JSONArray prevConf = new JSONArray();
		int index 		   = 0;
		
		Iterator<JSONObject> it = previousConfig.iterator();
		
		while (it.hasNext()) {
			JSONObject myobj = (JSONObject) it.next();
			String mode = (String) myobj.get("mode");
			if (mode.equals("mesh")) {
				newInterface.add(myobj);
			} else {
				prevConf.add(myobj);
				index++;
			}
		}
		
		LOG.info("######## total index  ######## " + index);
		
		for (int i = 0; i <index; i++) {
			
			LOG.info(" ##### loop count ######## " + i);
			
			JSONObject prevObj = (JSONObject)prevConf.get(i);
			JSONObject currObj = (JSONObject)currentConfig.get(i);

			if (prevObj == null || currObj == null)
				continue;

			String mode  = (String)prevObj.get("mode");
			
			if (!mode.equals("mesh")) {
				
				if (prevObj.containsKey("encryption")) {
					String encryption = (String) currObj.getOrDefault("encryption", "NA");

					if (encryption.equals("open")) {
						prevObj.remove("key");
						prevObj.put("encryption", encryption);
					} else {
						if (currObj.containsKey("key")) {
							String currKey = (String) currObj.get("key");
							if (!encryption.equals("open")) {
								prevObj.put("key", currKey);
							}
						}
						if (currObj.containsKey("encryption")) {
							String currEncryption = (String) currObj.get("encryption");
							prevObj.put("encryption", currEncryption);
						}
					}
				}
				
				if (currObj.containsKey("ssid")) {
					String currSsid = (String) currObj.get("ssid");
					prevObj.put("ssid", currSsid);
				}
				newInterface.add(prevObj);
			}

		}

		return newInterface;
	}
	
	/**
	 * 1.Used to store the device crash dump files
	 * @param file
	 * @param uid
	 * @return
	 */
	
	@PostMapping("/crash-dump-post")
	public Restponse<String> crashDumpPost(@RequestParam("file") MultipartFile file,
											@RequestParam("uid") String uid) {
		
		String body 	 = "Error";
		int code 		 = 500;
		boolean  success = false;
		
		try {
			
			LOG.info("File : " + file);
			
			if (file != null) {

				long size 			= file.getSize();
				String fileName 	= file.getOriginalFilename();
				String mediaType    = file.getContentType();
				
				LOG.info(" size " + size + " fileName " + fileName + " mediaType " + mediaType +" uid " +uid);
				
					DateFormat formatterUTC = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					
					LOG.info(" devUid " + uid);

					Device device = deviceManager.findOneByUid(uid);
					
					if (device != null) {
						
						String cid 		= device.getCid();
						String alias    = device.getName();
						
						String domain = CustomerUtils.domain.nmesh.name();
						String folder = customerUtils.createCrashDumpFolderName(domain);

						Customer customer = customerService.findById(cid);
						if (customer != null) {
							String zone = customer.getTimezone();
							TimeZone timeZone = customerUtils.FetchTimeZone(zone);
							formatterUTC.setTimeZone(timeZone);
						} else {
							formatterUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
						}

						String time = formatterUTC.format(new Date());
						
						LOG.info("Crash Time " + time);
						
						String cloudPath = _CCC.properties.getProperty("facesix.fileio.binary.root", "/var/www/html");
						
						String myServerPath = cloudPath+"/"+folder;
						File myfile = new File(myServerPath);

						if (!myfile.exists()) {
							boolean mysuccess = myfile.mkdirs();
							if (mysuccess) {
								LOG.info("Created Directory : " + myfile.getPath());
							} else {
								LOG.info("could not create directory..");
							}
						} else {
							LOG.info("Directory alredy exists");
						}
						
						String uuid = uid.replaceAll("[^a-zA-Z0-9]", "");
						
						fileName = uuid+"_"+System.currentTimeMillis()+"_"+fileName;
						
						String fileLocation = cloudPath+"/"+folder+"/"+fileName;
						
						 FileOutputStream fos = new FileOutputStream(fileLocation); 
						 fos.write(file.getBytes());
						 fos.close(); 
						 
						 /*
						  * Email Notification 
						  * 
						  */
						 
						 String sid  = device.getSid();
						 String spid = device.getSpid();
						 
						 emailService.notifyCrashDumpAlert(uid, cid, alias, sid, spid, fileName);
						
						/*
						 *  Elastic Search posting the crash dump history
						 * 
						 */
						HashMap<String, Object> map = new HashMap<String,Object>();
						
						final String opcode = "gw_device_crash_dump";
						final String type   = "device_crash_dump";
						
						map.put("opcode", 	 opcode);
						map.put("type", 	 type);
						map.put("uid", 		 uid);
						map.put("cid", 		 cid);
						map.put("crash_time",time);
						map.put("filename",  fileName);
						map.put("alias",  	 alias);
						
						elasticService.post(device_history_event, type, map); 
						
						body 	 = "Crash dump saved sucessfully.";
						code 	 = 200;
						success  = true;
						
					} else {
						code    = 404;
						body    = "Device not found." + uid;
						success = false;
					}
				
			} else {
				code 	= 204;
				success	= false;
				body 	= "No Content." +file;
			}
			
		} catch (Exception e) {
			code 	= 500;
			success	= false;
			body 	= "Error"+e.getMessage();
		}
		
		return new Restponse<String>(success, code, body);
    }
    
    /**
     * 
     *  used to search given filename is exists in the directory
     * @param fileName
     * @return;
     * 
     */
    
	@GetMapping("/fileNameExists")
	public Restponse<String> fileNameExists(@RequestParam("fileName") final String fileName) {

		String body 	= "File not found";
		int code 		= 404;
		boolean success = false;
		
		try {
			
			LOG.info(" FileName " + fileName);
			
			String cloudPath = _CCC.properties.getProperty("facesix.fileio.binary.root", "/var/www/html");
			
			File file = new File(cloudPath);
			
			if (file.exists()) {
				File[] fileList = file.listFiles();
				for (File f : fileList) {
					if (f.isDirectory()) {
						String[] fName = f.list();
						for (String str : fName) {
							if (str.equals(fileName)) {
								success = true;
								body 	= "File is avilable";
								code 	= 200;
								break;
							} else {
								continue;
							}
						}
					}
				}
			} else {
				code 	= 404;
				body 	= "Directory not found";
				success = false;
			}
			
		} catch (Exception e) {
			code 	= 500;
			body 	= "Error";
			success = false;
			
		}
		return new Restponse<String>(success, code, body);
	}

	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public JSONArray searchDevice() {
		
		List<String> deviceStatus = Arrays.asList("REGISTERED");
		
		List<Device> deviceIter				= deviceManager.findByConfiguredDevice(deviceStatus);
		List<BeaconDevice> beaconDeviceIter = beaconDeviceService.findByConfiguredDevice(deviceStatus);
		
		HashMap<String, String> customerMap = new HashMap<String,String>();
		HashMap<String, String> siteMap = new HashMap<String,String>();
		HashMap<String, String> floorMap = new HashMap<String,String>();
		
		JSONArray array = new JSONArray();
		
		try {
			
			if (deviceIter != null) {
				for (Device device : deviceIter) {
					
					String cid 		= device.getCid();
					String sid 		= device.getSid();
					String spid 	= device.getSpid();
					String status 	= device.getStatus() == null ? "unkown" : device.getStatus();
					String type 	= (device.getTypefs() == null)  ? "ap" : device.getTypefs();

					if (type.equals("server") || type.equals("switch")) {
						continue;
					}
					
					JSONObject object = new JSONObject();
					
					if (!Device.STATUS.REGISTERED.name().equalsIgnoreCase(status) && cid != null) {
						
						object.put("uid", 		device.getUid());
						object.put("name", 	    device.getName() == null ? "-" : device.getName());
						object.put("cid", 	    cid);
						object.put("state", 	device.getState());
						object.put("domain", 	"Nmesh");
						object.put("uid", 		device.getUid());
						
						String customerName = "-";
						String siteName  = "-";
						String floorName = "-";
						
						if (cid != null) {
							if (customerMap.containsKey(cid)) {
								customerName = customerMap.get(cid);
							} else {
								Customer customer = customerService.findById(cid);
								if (customer != null) {
									customerName = customer.getCustomerName();
								}
								customerMap.put(cid, customerName);
							}
						}
						if (sid != null) {
							if (siteMap.containsKey(sid)) {
								siteName = siteMap.get(sid);
							} else {
								Site site = siteService.findById(sid);
								if (site != null) {
									siteName = site.getUid();
								}
								siteMap.put(sid, siteName);
							}
						}

						if (spid != null) {
							if (floorMap.containsKey(spid)) {
								floorName = floorMap.get(spid);
							} else {
								Portion portion = portionService.findById(spid);
								if (portion != null) {
									floorName = portion.getUid();
								}
								floorMap.put(spid, floorName);
							}
						}

						object.put("customerName",   customerName);
						object.put("venueName",   siteName);
						object.put("floorName",  floorName);
						object.put("weblink", 	"/facesix/spots?cid="+cid+"&uid="+device.getUid());
						
						array.add(object);
					}

				}
			}
			if (beaconDeviceIter !=null) {
				for (BeaconDevice device : beaconDeviceIter) {
					
					String cid 		= device.getCid();
					String sid 		= device.getSid();
					String spid 	= device.getSpid();
					String status 	= device.getStatus() == null ? "unkown" : device.getStatus();
					
					JSONObject object = new JSONObject();
					
					if (!BeaconDevice.STATUS.REGISTERED.name().equalsIgnoreCase(status) && cid != null) {
						
						object.put("uid", 		device.getUid());
						object.put("name", 	    device.getName() == null ? "-" : device.getName());
						object.put("cid", 	    cid);
						object.put("state", 	device.getState());
						object.put("domain", 	"Locatum");
						object.put("uid", 		device.getUid());
						
						String customerName = "-";
						String siteName  = "-";
						String floorName = "-";
						
						if (cid !=null) {
							if (customerMap.containsKey(cid)) {
								customerName = customerMap.get(cid);
							} else {
								Customer customer = customerService.findById(cid);
								if (customer != null) {
									customerName = customer.getCustomerName();
								}
								customerMap.put(cid, customerName);
							}
						}

						if (sid != null) {
							if (siteMap.containsKey(sid)) {
								siteName = siteMap.get(sid);
							} else {
								Site site = siteService.findById(sid);
								if (site != null) {
									siteName = site.getUid();
								}
								siteMap.put(sid, siteName);
							}
						}

						if (spid != null) {
							if (floorMap.containsKey(spid)) {
								floorName = floorMap.get(spid);
							} else {
								Portion portion = portionService.findById(spid);
								if (portion != null) {
									floorName = portion.getUid();
								}
								floorMap.put(spid, floorName);
							}
						}

						object.put("customerName",   customerName);
						object.put("venueName",   siteName);
						object.put("floorName",  floorName);
						object.put("weblink", 	"/facesix/web/finder/device/list?cid="+cid+"&uid="+device.getUid());
						
						array.add(object);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("array size " +array.size());
		
		return array;
	}
	
	/**
	 * Used to configure and update the gateways(AP)
	 * @param strJson
	 * @param request
	 * @param response
	 * @return
	 */
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Restponse<String> save(@RequestBody String strJson,HttpServletRequest request, HttpServletResponse response) {
		
		
		LOG.info("requst payload " +strJson);
		
		String body 	= "UnAuthorized User";
		boolean success = false;
		int code 		= 401;
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			String cur_user = SessionUtil.currentUser(request.getSession());
			
			JSONObject payload = customerUtils.stringToSimpleJson(strJson);
			
			 String uid 	= (String)payload.get("uid");
			 String param   = (String)payload.get("param");
			 String sid     = (String)payload.get("sid");
			 String spid    = (String)payload.get("spid");
			
			try {
				
				LOG.info("requst payload " +payload);
				
				JSONObject resultJson = networkConfService.validateInputValues(payload);
				
				boolean status = (boolean) resultJson.get("status");
				
				LOG.info("Test case status " +resultJson);
				
				if (!status) {
					String message = (String) resultJson.get("body");
					LOG.info("Test case failed ");
					return new Restponse<String>(status, 400, message);
				} else {
					
					LOG.info("Test case pass ");
					LOG.info("condition pass processing save opertion");
					
					Device existingDevice = deviceManager.findOneByUid(uid);
					
					HashMap<String, Object> result = networkConfService.apConfig(payload,cur_user,request,response);
					
					Device updatedDevice = (Device)result.get("device");
					boolean isSave       = (boolean)result.get("status");
					
					if (!isSave) {
						 body = "Duplicate device " +uid;
						 return new Restponse<String>(false, 400,body);
					} else {
						
						if (param.equals("DeviceConfig") || StringUtils.isBlank(spid)) {
							
							/*
							 * If the device is new or existing device config is not
							 * same as the changed device config (except alias name)
							 * save and send the MQTT message
							 * Else just save the changes and set the sendMQTT as false
							 * 
							 */
							if (existingDevice == null || !existingDevice.equals(updatedDevice)) {
								
								updatedDevice = deviceManager.saveAndSendMqtt(updatedDevice, true);
								
								body 	= "Device has been saved successfully";
								code 	= 200;
								success = true;
								
							} else {
								updatedDevice = deviceManager.saveAndSendMqtt(updatedDevice, false);
								
								body 	= "Device has been updated successfully";
								code 	= 200;
								success = true;
							}
							
							return new Restponse<String>(success, code,body);
							

						} else if (StringUtils.isNotBlank(sid) && StringUtils.isNotBlank(spid)) {
							if (updatedDevice != null && StringUtils.isNotBlank(updatedDevice.getSpid())) {
								/*
								 * If the device is new or existing device config is not
								 * same as the changed device config (except alias name)
								 * save and send the MQTT message
								 * Else just save the changes and set sendMQTT as false
								 * 
								 */
								if (existingDevice == null || !existingDevice.equals(updatedDevice)) {
									updatedDevice = deviceManager.saveAndSendMqtt(updatedDevice, true);
									
									body 	= "Device has been saved successfully";
									code 	= 200;
									success = true;
									
								} else {
									updatedDevice = deviceManager.saveAndSendMqtt(updatedDevice, false);
									
									body 	= "Device has been updated successfully";
									code 	= 200;
									success = true;
								}
								
								return new Restponse<String>(success, code,body);
							}
						}
					}
				}
			} catch (Exception e) {
				success = false;
				code 	= 500;
				body 	= e.getMessage();
				e.printStackTrace();
			}
		}	
		return new Restponse<String>(success, code,body);
	}
	
	public ElasticService getElasticService() {
		if (elasticService == null) {
			elasticService = Application.context.getBean(ElasticService.class);
		}
		return elasticService;
	}
}
