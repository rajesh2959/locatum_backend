package com.semaifour.facesix.beacon.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.finder.geo.GeoFinderLayoutData;
import com.semaifour.facesix.beacon.finder.geo.GeoFinderLayoutDataService;
import com.semaifour.facesix.beacon.util.BeaconDeviceFileImportUtil;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.outsource.OutsourceRestController;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.spring.SpringComponentUtils;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/rest/beacon/device")
public class BeaconDeviceRestController extends WebController{

	@Autowired
	BeaconDeviceService beaconDeviceService;
	
	@Autowired
	FSqlRestController fsqlRestController;
	
	@Autowired
	DeviceEventPublisher deviceEventMqttPub;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	private GeoFinderLayoutDataService geoService;
	
	@Autowired
	PortionService portionService;
		
	@Autowired
	CustomerUtils CustomerUtils;
	
	@Autowired
	DeviceService deviceService;
	
	@Autowired
	UserAccountService userService;
	
	@Autowired
	ElasticService elasticService;
	
	@Autowired
	SiteService siteService;
	
	@Autowired
	BeaconDeviceFileImportUtil fileImportUtil;
	
	@Autowired
	private OutsourceRestController outsourceRestController;
	
	String mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\",\"ap\":\"{2}\",\"mac\":\"{3}\", \"by\":\"{4}\"";
	
	static Logger LOG = LoggerFactory.getLogger(BeaconDeviceRestController.class.getName());

	private String indexname = "facesix*";
	
	@PostConstruct
	public void init() {
		indexname 		= _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
	}
	
	@Value("${facesix.cloud.name}")
	private String cloudUrl;
	
	
	@RequestMapping(value = "/listAll", method = RequestMethod.GET)
	public @ResponseBody Iterable<BeaconDevice> listAll() {
		return beaconDeviceService.findAll();
	}
	
	@RequestMapping(value = "/configure", method = RequestMethod.GET)
	public JSONObject configure(@RequestParam("uid") String uid,@RequestParam("cid") String cid) {

		JSONObject jsonObject = new JSONObject();
		BeaconDevice device = beaconDeviceService.findOneByUid(uid);
		
		if (device != null) {
			
			String state  		= (!StringUtils.hasLength(device.getState())) ? BeaconDevice.STATE.inactive.name() : device.getState();
			String status 		= (!StringUtils.hasLength(device.getStatus())) ? BeaconDevice.STATUS.REGISTERED.name(): device.getStatus();
			String custId 		= device.getCid() == null ? cid : device.getCid();
			String ip  			= device.getIp();
			String tunnelIp		= device.getTunnelIp();
			String devIp 		= (!StringUtils.hasLength(ip)) ? "0.0.0.0" : ip;
			tunnelIp 			= (!StringUtils.hasLength(tunnelIp)) ? "0.0.0.0" : tunnelIp;
			
			String myconf = SpringComponentUtils.getApplicationMessages().getMessage("facesix.beacon.device.template.default");
			String conf   = (!StringUtils.hasLength(device.getConf())) ? myconf : device.getConf(); 
			String source = (!StringUtils.hasLength(device.getSource())) ? "qubercomm" :  device.getSource();
			
			jsonObject.put("state", 		state);
			jsonObject.put("id", 			device.getId());
			jsonObject.put("mac_address",   device.getUid());
			jsonObject.put("alias", 		(!StringUtils.hasLength(device.getName())) ? device.getUid() : device.getName());
			jsonObject.put("status",		status);
			jsonObject.put("cid", 			custId);
			jsonObject.put("bleType", 		(!StringUtils.hasLength(device.getType())) ? "receiver" : device.getType());
			jsonObject.put("debugflag",    device.getDebugflag());
			jsonObject.put("sid",          device.getSid());
			jsonObject.put("spid",         device.getSpid());
			jsonObject.put("ip",           devIp);
			jsonObject.put("tunnelip",     tunnelIp);
			jsonObject.put("conf", 			conf);
			jsonObject.put("source",    	source);
		}
		
		return jsonObject;
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(@RequestParam("cid") String cid) {		

		JSONArray array = new JSONArray();
		
		try {
			
			List<BeaconDevice> device = null;
			device = beaconDeviceService.findByCid(cid);
		
			String serverip = "0.0.0.0";
			String uid = "";
			String solution = "";
			String type = "";
			String sid = "";
			String spid = "";
			String ip = "";
			String tunnelIp = "";
			JSONObject conf = null;
			String tagThreshold  = "20";
			String vpn = "disable";
			
			if (device != null) {

				Customer acc = customerService.findById(cid);
				if (acc != null) {
					serverip 		= acc.getBleserverip();
					solution 		= acc.getVenueType();
					tagThreshold 	= acc.getThreshold();
					vpn 			= acc.getVpn();
				}
				
				if (vpn != null && vpn.equals("true")) {
					vpn = "enable";
				} else {
					vpn = "disable";
				}

				for (BeaconDevice dev : device) {
					conf = new JSONObject();
					
					uid = dev.getUid().toUpperCase();
					sid = dev.getSid();
					spid = dev.getSpid();
					type = dev.getType();
					ip = dev.getIp() == null? "0.0.0.0":dev.getIp();
					tunnelIp = dev.getTunnelIp() == null?"0.0.0.0":dev.getTunnelIp();

					String source = dev.getSource() == null? "qubercomm":dev.getSource();
					boolean guest = ! source.equals("qubercomm");

					conf.put("uid", uid);
					
					if (type != null) {
						conf.put("type", type);
					}
					if (cid != null) {
						conf.put("cid", cid);
					}
					if (sid != null) {
						conf.put("sid", sid);
					}
					if (spid != null) {
						conf.put("spid", spid);
					}
					
					conf.put("ip",ip);
					conf.put("tunnelip", tunnelIp);
					conf.put("guest", guest);

					conf.put("serverip", serverip);
					if(solution != null){
						if (solution.equalsIgnoreCase("Locatum")){
							conf.put("solution", "trilateration");
						} else if(solution.equalsIgnoreCase("Patient-Tracker")){
							conf.put("solution", "entryexit");
						} else {
							conf.put("solution", "gateway");
						}
					}
					conf.put("tagthreshold", tagThreshold);
					
					String mqttDebugFlag = "enable";
			
					if (dev.getDebugflag() != null) {
						String debug = dev.getDebugflag().trim();
						if (debug.equalsIgnoreCase("unchecked")) {
							mqttDebugFlag = "disable";
						}
					} else {
						mqttDebugFlag = "disable";
					}
					
					conf.put("debug", mqttDebugFlag);
					
					if (dev.getPixelresult() != null) {
						conf.put("deviceinfo", dev.getPixelresult());
					}
					
					conf.put("source", source);

					String temp = dev.getConf();
					net.sf.json.JSONObject template	 = net.sf.json.JSONObject.fromObject(temp);
					template = beaconDeviceService.addVpnToConf(vpn,template);
					conf.put("conf", template);
					array.add(conf);
				}
			}
		} catch(Exception e) {
			LOG.info("while configured device listing error ",e);
			return null;
		}
		return array.toString();
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
    public  String info(@RequestParam("uid") String uid) {
		
		BeaconDevice device = beaconDeviceService.findOneByUid(uid);
		
		String cid = "";
		String serverip = null;
		String type = null;
		String tagThreshold  = "20";
		String sid = "";
		String spid = "";
		String ip = "";
		String tunnelIp = "";
		String vpn = "disable";
		
		//LOG.info("UID" + uid);
		
		if (device != null && !BeaconDevice.STATUS.REGISTERED.name().equalsIgnoreCase(device.getStatus())) {
			
			JSONObject conf = new JSONObject();
			cid = device.getCid();
			sid = device.getSid();
			spid = device.getSpid();
			ip = device.getIp() == null ? "0.0.0.0" : device.getIp();
			tunnelIp = device.getTunnelIp() == null ? "0.0.0.0" : device.getTunnelIp();
			String source = device.getSource() == null? "qubercomm":device.getSource();

			conf.put("uid", 	device.getUid().toUpperCase());
			conf.put("type",  	device.getType());
			
			if (cid != null) {
				conf.put("cid", cid);
			}
			if (sid != null) {
				conf.put("sid", sid);
			}
			if (spid != null) {
				conf.put("spid", spid);
			}

			conf.put("ip", ip);
			conf.put("source",source);
			conf.put("tunnelip", tunnelIp);
			
			Customer cust = customerService.findById(cid);
			if (cust !=null) {
				type     = cust.getVenueType();
				if (cust.getBleserverip() != null) {
					serverip = cust.getBleserverip();
				}
				tagThreshold 	= cust.getThreshold();
				vpn = cust.getVpn();
			}
			
			if (serverip == null) {
				serverip = "0.0.0.0";
			}
			
			conf.put("serverip", serverip);
			
			if(type != null){
				if (type.equalsIgnoreCase("Locatum")){
					conf.put("solution", "trilateration");
				} else if(type.equalsIgnoreCase("Patient-Tracker")){
					conf.put("solution", "entryexit");
				} else {
					conf.put("solution", "gateway");
				}
			}
			
			conf.put("tagthreshold", tagThreshold);
			
			String mqttDebugFlag = "enable";
			
			if (device.getDebugflag() != null) {
				String debug = device.getDebugflag().trim();
				if (debug.equalsIgnoreCase("unchecked")) {
					mqttDebugFlag = "disable";
				}
			} else {
				mqttDebugFlag = "disable";
			}
			
			conf.put("debug", mqttDebugFlag);
			
			if (device.getPixelresult() != null) {
				conf.put("deviceinfo", device.getPixelresult());
			}
			
			String temp = device.getConf();
			net.sf.json.JSONObject template	 = net.sf.json.JSONObject.fromObject(temp);
			net.sf.json.JSONObject diag_json = beaconDeviceService.makeDiagJson(template); //diag_details merge key and value
			
			if(vpn != null && vpn.equals("true")){
				vpn = "enable";
			} else {
				vpn = "disable";
			}
			
			template = beaconDeviceService.addVpnToConf(vpn,template);

			if (diag_json != null && diag_json.size() > 0) {
				template.put("diag_details",diag_json);
			}
			
			conf.put("conf", template);

			return conf.toString();
		} else {
			return "{ \"uid\" :\"" + uid + "\" , \"status\":\"NOT_FOUND\" }";
		}			
    }
 
	@RequestMapping(value = "/floor_info", method = RequestMethod.GET)
	public String floor_info(@RequestParam("sid") String sid) {

		JSONObject conf = new JSONObject();

		if (sid != null && !sid.isEmpty()) {

			List<Portion> floors = portionService.findBySiteId(sid);

			int countFloor = 0;
			JSONObject flr = null;
			JSONArray flr_array = new JSONArray();

			if (floors != null) {
				
				Iterator<Portion> iter = floors.iterator();
				while (iter.hasNext()) {
					Portion floor = iter.next();
					String spid = floor.getId();
					flr = new JSONObject();
					flr.put(String.valueOf(countFloor), spid);
					GeoFinderLayoutData device = geoService.getSavedGeoLayoutDataBySpid(spid);
					if (device != null) {
						flr.put("geoboundary", device.getGeoPointslist());
						flr.put("geoinfo", 	   device.getGeoresult());
						//flr.put("deviceinfo",  device.getPixelresult());//device location info
					}
					flr_array.add(flr);
					countFloor += 1;
				}
				conf.put("floorcount", String.valueOf(countFloor));
				conf.put("floorDetails", flr_array);

			}
			//LOG.info("conf>>>>>>>> " +conf);
			return conf.toString();
		} else {
			return null;
		}

	}
	
	 	@RequestMapping(value = "/scanner", method = RequestMethod.GET)
		public JSONObject bleScanner(@RequestParam(value = "cid", required = false) String cid,
							   HttpServletRequest request) throws IOException{

		 	
			JSONObject devlist = new JSONObject();
			try {
				
				JSONObject dev 				= null;
				JSONArray dev_array 		= new JSONArray();
				List<BeaconDevice> device 	= null;
				String state 				= "inactive";
				String deviceType			= "scanner";
				device 						= beaconDeviceService.findByCidAndType(cid, deviceType);
				
				if (device != null) {
					
					HashMap<String, String> siteMap = new HashMap<String, String>();
					HashMap<String, String> floorMap = new HashMap<String, String>();
					
					for (BeaconDevice dv : device) {
						dev = new JSONObject();
							state = dv.getState();
							dev.put("state", 		state.toUpperCase());
		                    dev.put("id", 			dv.getId());
							dev.put("mac_address",  dv.getUid());
							dev.put("dev_name", 	dv.getName());
							dev.put("status",		dv.getStatus());
							dev.put("cid", 			dv.getCid());
							dev.put("bleType", 		dv.getType());
							dev.put("debugflag",    dv.getDebugflag());
							dev.put("sid",         (dv.getSid() == null) ? "NA" : dv.getSid());
							dev.put("spid",        (dv.getSpid() == null) ? "NA" : dv.getSpid());
							
							String ip  				= dv.getIp();
							String tunnelIp			= dv.getTunnelIp();
							String devIp = (ip == null || ip.isEmpty()) ? "0.0.0.0" : ip;
							tunnelIp = (tunnelIp == null || tunnelIp.isEmpty()) ? "0.0.0.0" : tunnelIp;
							dev.put("ip",           devIp);
							dev.put("tunnelip",     tunnelIp);
							
							if (!state.equalsIgnoreCase("inactive") && !devIp.equals("0.0.0.0")) {
								dev.put("cmd_enable", "1");
							} else {
								dev.put("cmd_enable", "0");
							}
							
							String sid  = dv.getSid();
							String spid = dv.getSpid();
							
							String floorName = "unknown";
							String siteName  = "unknown";
							
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
									Portion p = portionService.findById(spid);
									if (p != null) {
										floorName = p.getUid();
									}
									floorMap.put(spid, floorName);
								}
							}
							
							dev.put("siteName",   siteName);
							dev.put("floorName",  floorName);
							
							
							dev_array.add(dev);
					}
					devlist.put("blescanner", dev_array);
				}
			} catch (Exception e) {
				LOG.info("while getting customer device list error", e);
			}
			return devlist;
		}
		
		/**
		 * @param cid
		 * @param uid
		 * @param request
		 * @return
		 * @throws IOException
		 * 
		 * This function returns receiver list sorted by source field
		 */
		@RequestMapping(value = "/receiver", method = RequestMethod.GET)
		@SuppressWarnings("unchecked")
		public JSONArray bleReceiver(@RequestParam(value = "cid", required = false) String cid,
									  @RequestParam(value = "uid", required = false) String uid,
								      HttpServletRequest request) throws IOException{

			JSONArray dev_array 	= new JSONArray();
			
			try {
				
				JSONObject dev 				= null;
				
				List<BeaconDevice> device 	= new  ArrayList<BeaconDevice>();
				String deviceType           = "receiver";
				
				if (StringUtils.isEmpty(uid) || uid.equals("undefined")) {
					
					final String sortBy = "createdOn";
					Sort sort = new Sort(Sort.Direction.DESC, sortBy);
					
					device 	= beaconDeviceService.findByCidAndType(cid, deviceType,sort);
					
				} else {
					BeaconDevice dv = beaconDeviceService.findByUidAndCidAndType(uid, cid,deviceType);
					if (dv == null) {
						device = beaconDeviceService.findByCidAndTypeAndName(cid,deviceType, uid);
					}else {
						device.add(dv);
					}
				}
				
				if (device != null) {
					
					HashMap<String, String> siteMap = new HashMap<String, String>();
					HashMap<String, String> floorMap = new HashMap<String, String>();
					
					for (BeaconDevice dv : device) {
						
						dev 	= new JSONObject();
						
						String state 	 = dv.getState();
						String deviceUid = dv.getUid();
						
						String source = StringUtils.isEmpty(dv.getSource()) ? "qubercomm":dv.getSource();
						boolean guest = ! source.equals("qubercomm");

						if (!source.equals("qubercomm")) {
							org.json.simple.JSONObject gatewayState = outsourceRestController.gatewayStatus(deviceUid);
							state 	 = (String)gatewayState.get("state");
						}
						
						dev.put("state", 		state.toUpperCase());
	                    dev.put("id", 			dv.getId());
						dev.put("mac_address",  deviceUid);
						dev.put("dev_name", 	dv.getName());
						dev.put("status",		dv.getStatus());
						dev.put("cid", 			dv.getCid());
						dev.put("bleType", 		dv.getType());
						dev.put("debugflag",    dv.getDebugflag());
						dev.put("source",       source);
						dev.put("guest",        guest);
						dev.put("sid",        (dv.getSid() == null) ? "NA" : dv.getSid());
						dev.put("spid",        (dv.getSpid() == null) ? "NA" : dv.getSpid());
						
						String ip  				= dv.getIp();
						String tunnelIp			= dv.getTunnelIp();
						
						String devIp 	= (StringUtils.isEmpty(ip)) ? "0.0.0.0" : ip;
						tunnelIp 		= (StringUtils.isEmpty(tunnelIp)) ? "0.0.0.0" : tunnelIp;

						dev.put("ip",           devIp);
						dev.put("tunnelip",     tunnelIp);
						
						if (!state.equalsIgnoreCase("inactive") && !devIp.equals("0.0.0.0")) {
							dev.put("cmd_enable", "1");
						} else {
							dev.put("cmd_enable", "0");
						}
						
						String sid  = dv.getSid();
						String spid = dv.getSpid();
						
						String floorName = "unknown";
						String siteName  = "unknown";
						
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
								Portion p = portionService.findById(spid);
								if (p != null) {
									floorName = p.getUid();
								}
								floorMap.put(spid, floorName);
							}
						}
						
						dev.put("siteName",   siteName);
						dev.put("floorName",  floorName);
						
						
						dev_array.add(dev);
					}
				}
			} catch (Exception e) {
				LOG.info("while getting customer device list error", e);
			}
			return dev_array;
		}
		
		@RequestMapping(value = "/server", method = RequestMethod.GET)
		@SuppressWarnings("unchecked")
		public JSONArray list(@RequestParam(value = "cid", required = false) String cid,
							   HttpServletRequest request) throws IOException{

			JSONArray dev_array 		= new JSONArray();
			
			try {
				
				JSONObject dev 				= null;
			
				List<BeaconDevice> device 	= null;
				String deviceType			= "server";
				device 						= beaconDeviceService.findByCidAndType(cid, deviceType);
				
				if (device != null) {
					
					HashMap<String, String> siteMap = new HashMap<String, String>();
					HashMap<String, String> floorMap = new HashMap<String, String>();
					
					for (BeaconDevice dv : device) {
						dev = new JSONObject();
						
						String state  = dv.getState();
						String source = StringUtils.isEmpty(dv.getSource()) ? "qubercomm":dv.getSource();
						
						dev.put("state", 		state.toUpperCase());
	                    dev.put("id", 			dv.getId());
						dev.put("mac_address",  dv.getUid());
						dev.put("dev_name", 	dv.getName());
						dev.put("status",		dv.getStatus());
						dev.put("cid", 			dv.getCid());
						dev.put("bleType", 		dv.getType());
						dev.put("debugflag",    dv.getDebugflag());
						dev.put("sid",         (dv.getSid() == null) ? "NA" : dv.getSid());
						dev.put("spid",        (dv.getSpid() == null) ? "NA" : dv.getSpid());
						dev.put("source", 		source);
						String ip  				= dv.getIp();
						String tunnelIp			= dv.getTunnelIp();
						String devIp = (ip == null || ip.isEmpty()) ? "0.0.0.0" : ip;
						tunnelIp = (tunnelIp == null || tunnelIp.isEmpty()) ? "0.0.0.0" : tunnelIp;
						
						dev.put("ip",          devIp);
						dev.put("tunnelip",     tunnelIp);
						
						if (!state.equalsIgnoreCase("inactive") && !devIp.equals("0.0.0.0")) {
							dev.put("cmd_enable", "1");
						} else {
							dev.put("cmd_enable", "0");
						}
						
						String sid  = dv.getSid();
						String spid = dv.getSpid();
						
						String floorName = "unknown";
						String siteName  = "unknown";
						
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
								Portion p = portionService.findById(spid);
								if (p != null) {
									floorName = p.getUid();
								}
								floorMap.put(spid, floorName);
							}
						}
						
						dev.put("siteName",   siteName);
						dev.put("floorName",  floorName);
						
						
						dev_array.add(dev);
					}
				}
			} catch (Exception e) {
				LOG.info("while getting customer device list error", e);
			}
			return dev_array;
		}
	
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public void delete(@RequestParam("uid") String uid,
					   @RequestParam("cid") String cid,
					   @RequestParam(value = "sid", required = false) String sid,
					   @RequestParam(value = "spid", required = false) String spid,
					   HttpServletRequest request,
					   HttpServletResponse response) throws Exception {

		//LOG.info("BeaconDevice UID " +uid);
		//LOG.info("BeaconDevice  CID " +cid);
		
		String str = "/facesix/web/finder/device/list?cid="+cid+"&sid="+sid+"&spid="+spid;
		
		try {
			
			String type = null;
			
			BeaconDevice device = beaconDeviceService.findOneByUid(uid);
			if (device != null) {
				type = device.getType();
				beaconDeviceService.delete(device);
				device.setId(null);
			}
			beaconDeviceService.resetServerIP(type,cid);
			
			response.sendRedirect(str);
		} catch (Exception e) {
			response.sendRedirect(str);
			LOG.info("While Device Delete Error " ,e);
		}
		
	}
	
	@RequestMapping(value = "/ibeacondelete", method = RequestMethod.DELETE)
	public Restponse<String> ibeacondelete(@RequestParam("uid") String uid,HttpServletRequest request, HttpServletResponse response) throws Exception {

		int code 		= 200;
		boolean success = true;
		String body 	= "Successfully deleted device";
		
		try {
			
			BeaconDevice device = beaconDeviceService.findOneByUid(uid);
			
			if (device != null) {
				String type = device.getType();
				String cid  = device.getCid();
				beaconDeviceService.delete(device);
				device.setId(null);
				beaconDeviceService.resetServerIP(type,cid);
			} else {
				code 	= 404;
				success = false;
				body 	= "Device not found";
			}
			
			
		} catch (Exception e) {
			success = false;
			code 	= 500;
			body 	= "an error occurred while deleting device " +e.getMessage();
		}
		
		return new Restponse<String>(success, code,body);
	}
	
	/**
	 * Used to delete multiple gateways
	 */
	
	@RequestMapping(value = "/ibeaconBulkDelete", method = RequestMethod.POST)
	public Restponse<String> ibeaconBulkDelete(@RequestBody String[] macList) {

		int code 		= 200;
		boolean success = true;
		String body 	= "Successfully deleted device";
		
		try {
			
			List<String> uids = Arrays.asList(macList);
			
			List<BeaconDevice> deviceList = beaconDeviceService.findByUids(uids);
			
			if (deviceList != null) {
				for (BeaconDevice beaconDevice : deviceList) {
					String type = beaconDevice.getType();
					String cid = beaconDevice.getCid();
					beaconDeviceService.delete(beaconDevice);
					if ("server".equals(type)) {
						beaconDeviceService.resetServerIP(type, cid);
					}
				}
			} else {
				code 	= 404;
				success = false;
				body 	= "Device not found";
			}
			
			
		} catch (Exception e) {
			success = false;
			code 	= 500;
			body 	= "an error occurred while deleting device " +e.getMessage();
		}
		
		return new Restponse<String>(success, code,body);
	}
	
	
	@RequestMapping(value = "/regdevice/deleteall", method = RequestMethod.DELETE)
	public Restponse<String> registeredDeleteall(@RequestParam(value = "cid", required = false) String cid)  {

		int code 		= 200;
		boolean success = true;
		String body 	= "Successfully deleted device";
		boolean isAvilable = true;
		
		try {

			final String status = BeaconDevice.STATUS.REGISTERED.name();
			Iterable<BeaconDevice> device = beaconDeviceService.findByStatus(status);
			
			if (device != null) {
				for (BeaconDevice dv : device) {
					if (status.equalsIgnoreCase(dv.getStatus())) {
						beaconDeviceService.delete(dv);
					}
				}
			} else {
				isAvilable = false;
			}

			if (!isAvilable) {
				code = 404;
				success = false;
				body = "Device not found";
			}
			
		} catch (Exception e) {
			success = false;
			code 	= 500;
			body 	= "an error occurred while deleting device " +e.getMessage();
			e.printStackTrace();
		}
		
		return new Restponse<String>(success, code,body);
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public void save(
			@RequestParam(value = "uuid", required = true) String uid,
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "conf", required = true) String conf,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "source", required = false) String source,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		//LOG.info("BEACON SAVE UID  " + uid);
		//LOG.info("BEACON SAVE NAME " + name);
		//LOG.info("BEACON SAVE JSON  " + conf);
		//LOG.info("BEACON SAVE CID  " + cid);
		
		String str = "/facesix/web/finder/device/list?cid="+cid;
		
		try {
			
			net.sf.json.JSONObject template = net.sf.json.JSONObject.fromObject(conf);
			BeaconDevice device = null;
			
			device =beaconDeviceService.findOneByUid(uid);
			if(source==null || source.isEmpty()) {
				source="qubercomm";
			}
				if (device == null) {
					//LOG.info("BEACON SAVE >>>>");
					device = new BeaconDevice();
					device.setCreatedBy(SessionUtil.currentUser(request.getSession()));
					device.setUid(uid);
					device.setName(name);
					device.setFstype("scanner");
					device.setStatus(Device.STATUS.AUTOCONFIGURED.name());
					device.setState("inactive");
					device.setCid(cid);
					device.setTemplate(template.toString());
					device.setConf(template.toString());
					device.setModifiedBy("Cloud");
					device.setSource(source);
					device = beaconDeviceService.save(device, true);
				} else {
					//LOG.info("BEACON UPDATE>>>>>>");
					device.setStatus(Device.STATUS.CONFIGURED.name());
					device.setName(name);
					device.setTemplate(template.toString());
					device.setConf(template.toString());
					device.setModifiedBy("Cloud");
					device.setModifiedOn(new Date(System.currentTimeMillis()));
					device.setCid(cid);
					device.setSource(source);
					device =beaconDeviceService.save(device, true);
				}
				response.sendRedirect(str);

		} catch (Exception e) {
			response.sendRedirect(str);
			LOG.info("While beacon save error ",e);
		}
	}
	
	@RequestMapping(value = "/registered", method = RequestMethod.GET)
	public JSONArray register(@RequestParam(value = "cid", required = false) String cid,
							   HttpServletRequest request) throws IOException{

		if (cid == null || cid.isEmpty()) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		
		JSONArray dev_array = new JSONArray();	
		
		try {
			
			JSONObject dev = null;
			
			final String status = BeaconDevice.STATUS.REGISTERED.name();
			List<BeaconDevice> device= beaconDeviceService.findByStatus(status);

			if (device != null) {
				for (BeaconDevice dv : device) {
					dev = new JSONObject();
						dev.put("state", 		"UNKNOWN");
	                    dev.put("id", 			dv.getId());
						dev.put("mac_address",  dv.getUid());
						dev.put("dev_name", 	dv.getName());
						dev.put("status",		dv.getStatus());
						dev.put("cid", 			cid);
						dev.put("bleType", 		dv.getFstype());
						dev.put("ip",           dv.getIp());
						dev.put("cmd_enable", 	"0");
						dev.put("source",      "qubercomm");
						dev_array.add(dev);
					}
			}
		} catch (Exception e) {
			LOG.info("while getting registered device list error", e);
		}
		return dev_array;
	}
	
	@RequestMapping(value = "/beacondefaultconfig", method = RequestMethod.POST)
	public JSONObject custlegactest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		net.sf.json.JSONObject JSONCONF = null;
		try {
			String tconf = null;
			tconf = SpringComponentUtils.getApplicationMessages().getMessage("facesix.beacon.device.template.default");
			JSONCONF = net.sf.json.JSONObject.fromObject(tconf);
			//LOG.info("BEACON DEFAULT CONFIG " + JSONCONF);
		} catch (Exception e) {
			LOG.info("WHILE GETTING BEACON DEFAULT CONFIG ERROR {}", e);
		}
		return JSONCONF;
	}
	@RequestMapping(value = "/rpc", method = RequestMethod.POST)
	public String rpc(@RequestParam(value = "uid", required = true) String uid,
			@RequestParam(value = "ap", required = true) String ap,
			@RequestParam(value = "mac", required = false) String mac,
			@RequestParam(value = "cmd", required = true) String cmd) {

		String ret = "SUCCESS: RPC Message Sent";
		//LOG.info("RPC::UID " + uid);
		//LOG.info("RPC::MAC " + mac);
		//LOG.info("RPC::AP " + ap);
		//LOG.info("RPC::CMD " + cmd);
		try {

			BeaconDevice device = beaconDeviceService.findOneByUid(uid);

			if (device != null && !BeaconDevice.STATUS.REGISTERED.name().equalsIgnoreCase(device.getStatus())) {

				//LOG.info("RPC Status" + device.getStatus());
				String message = MessageFormat.format(mqttMsgTemplate,new Object[] { cmd, device.getUid().toUpperCase(), ap, mac, "device_update" });
				
				if (cmd.equals("RESET")) {
					beaconDeviceService.reset(device, true);
				} else {
					deviceEventMqttPub.publish("{" + message + "}", uid.toUpperCase());
				}

				//LOG.info("SUCCESS: RPC Message Sent |uid:" + uid + "|cmd:" + cmd);

			} else {
				ret = "FAILURE: Invalid Device";
				//LOG.info("Invalid Device");
			}
		} catch (Exception e) {
			ret = "Error: FATAL error occured";
			LOG.error("FAILURE: RPC Message Failed |uid :" + uid + "|cmd:" + cmd, e);
			ret = "FAILURE: RPC Message Failed";
		}

		return ret;
	}
	@RequestMapping(value = "/checkDuplicate", method = RequestMethod.GET)
	public Restponse<String> checkDuplicate(
			@RequestParam("uid") String uid,
			@RequestParam(value = "config" ,required = false ,defaultValue = "CustomConfig") String param) throws Exception {
		
		boolean success = true;
		int code 		= 200;
		String body 	= "new";
		int dup			= 0;

		if (StringUtils.isEmpty(uid)) {
			LOG.info("uid is empty");
		} else {
			BeaconDevice device = beaconDeviceService.findOneByUid(uid);
			if (device != null) {
				
				String status    = device.getStatus();
				String deviceUid = device.getUid();
				String myStatus  = BeaconDevice.STATUS.REGISTERED.name();
				
				
				if (param.equals("CustomConfig")) {
					if (!myStatus.equalsIgnoreCase(status) && deviceUid.equalsIgnoreCase(uid)) {
						dup = 1;
					} else {
						dup = 0;
					}
				} else if (param.equals("FloorConfig")) {
					
					String sid = device.getSid();
					String spid = device.getSpid();
					
					if (deviceUid.equalsIgnoreCase(uid)) {
						if (myStatus.equals(status) || StringUtils.isEmpty(spid)) {
							dup = 0;
							LOG.info("REGISTERED device skiped duplicate " +uid);
						} else if (!myStatus.equals(status)
								&& (StringUtils.isEmpty(sid) || StringUtils.isEmpty(spid))) {
							LOG.info("Not REGISTERED device but sid or spid is null skiped duplicate " +uid);
							dup = 0;
						} else if (!myStatus.equals(status)
								&& (!StringUtils.isEmpty(sid) || !StringUtils.isEmpty(spid))) {
							LOG.info("Not REGISTERED device but sid or spid is not null duplicate " +uid);
							dup = 1;
						} else {
							dup = 0;
						}
					} else {
						dup = 0;
					}
				}
			}
		}
		if (dup == 1) {
			body 	= "duplicate";
			success = false;
			code 	= 422;
		}
		
		return new Restponse<String>(success, code, body);
	}
	@RequestMapping(value = "/deleteall", method = RequestMethod.GET)
	public void deleteall(@RequestParam(value = "cid", required = false) String cid,
						 HttpServletRequest request,
					     HttpServletResponse response) throws IOException{

		if (cid == null || cid.isEmpty()) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		
		String str = "/facesix/web/finder/device/reglist?cid="+cid;
		
		try {
			final String status = BeaconDevice.STATUS.REGISTERED.name();
			List<BeaconDevice> device= beaconDeviceService.findByStatus(status);	
			if (device != null) {
				for (BeaconDevice dv : device) {
					if (status.equals(dv.getStatus())) {
						beaconDeviceService.delete(dv);
					}
				}
			}
			
			response.sendRedirect(str);
		} catch (Exception e) {
			response.sendRedirect(str);
		}
	}
	
	@RequestMapping(value = "/binary/save",method = RequestMethod.POST)
	public JSONObject save(@RequestParam(value = "file" ,required = true) MultipartFile file,
			@RequestParam(value = "upgradeType" ,required = true) final String upgradeType,
			@RequestParam(value="binaryType",	required = true)  final String binaryType,
			@RequestParam(value = "sid", 		required = false) String sid,
			@RequestParam(value = "spid", 		required = false) String spid,
			@RequestParam(value = "location", 	required = false) String location,
			@RequestParam(value = "cid", 		required = false) String cid,
			HttpServletRequest request,			HttpServletResponse response) {
		
		
		String universalId  = null;
		int deviceCount		= 0;
		JSONObject retJsonObject = new JSONObject();
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			try {
				
				LOG.info(" upgradeType " + upgradeType +" binaryType " + binaryType);
				LOG.info(" CId " + cid+" SId " + sid+ " SPId " + spid+ " location " + location);
				
				Path path 		   		   = null;
				String fileName    		   = null;
				String md5CheckSum 		   = null;
				boolean isUID 		 	   = false;
				boolean IsgwSolution 	   = false;
	
				switch (upgradeType) {
		
				case "venue":
		
					if (sid != null && sid.equals("all")) {
						universalId = cid;
					} else if (sid !=null){
						universalId = sid;
					}
					break;
					
				case "floor":
					
					if (sid.equals("all") && cid !=null) {
						universalId = cid;
					} else if (spid != null && spid.equals("all") && sid != null) {
						universalId = sid;
					} else  if (spid != null){
						universalId = spid;
					}
					break;
					
				case "location":
					
					if (location != null && location.contains(":") && (location.trim().length() == 17)) {
						universalId = location;
					} else if (sid != null && sid.equals("all") && cid !=null) {
						universalId = cid;
					} else if (spid != null && spid.equals("all") && sid != null)  {
						universalId = sid;
					}  else if (location != null && location.equals("all") && spid != null)  {
						universalId = spid;
					} 
						break;
				}
				
				List<Device> device = deviceService.findByGlobal(universalId, universalId, universalId, universalId);
				if (device != null) {
					device.forEach(dev -> {
						dev.setBinaryType(binaryType);
						dev.setUpgradeType(upgradeType);
						dev.setBinaryReason("FAILURE");
						deviceService.save(dev, false);
					});
				}

				List<BeaconDevice> beaconDevice = beaconDeviceService.findByGlobal(universalId, universalId,universalId, universalId);
				if (beaconDevice != null) {
					device.forEach(dev -> {
						dev.setBinaryType(binaryType);
						dev.setUpgradeType(upgradeType);
						dev.setBinaryReason("FAILURE");
						deviceService.save(dev, false);
					});
				}

				if (universalId != null && universalId.contains(":")) {
					isUID = true;
				} else {
					isUID = false;
				}

				if (CustomerUtils.Gateway(cid) || CustomerUtils.isRetail(cid)) {
					IsgwSolution = true;
				}

				if (isUID == true && IsgwSolution == false) {
					universalId   = universalId.toUpperCase();
				} else if (IsgwSolution == true && isUID==true) {
					universalId  = universalId.toLowerCase();
				}	
				
				LOG.info(" MQTT Uiversal Id   " + universalId + " deviceCount " +deviceCount);
				
				if (file != null && !file.isEmpty() && file.getSize() > 1) {
					fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
					path = Paths.get(_CCC.properties.getProperty("facesix.fileio.binary.root", "/var/www/html"),(fileName));
					//path 	 = Paths.get(_CCC.properties.getProperty("facesix.fileio.root", "./_FSUPLOADS_"),(fileName)); // test path
					Files.createDirectories(path.getParent());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					md5CheckSum = checkSumMD5(path.toString());
				} else {
					LOG.info("File is " +file);
					return null;
				}
				
				if (fileName != null) {
					CustomerUtils.binaryUpgradeCache.put(cid, fileName);
				}
				JSONObject json  = new JSONObject();
				String cloudName = cloudUrl;
				
				json.put("filename", 	fileName);
				json.put("filepath", 	cloudName);
				json.put("md5sum", 		md5CheckSum);
				String opcode 			= binaryType;
				
				BINARY_BOOT(json, opcode,universalId);
				
			} catch (IOException e) {
				e.printStackTrace();
				LOG.warn("Failed save binary files", e);
			}

			retJsonObject.put("universalId", universalId);
			retJsonObject.put("upgradeType", upgradeType);
			retJsonObject.put("deviceCount", deviceCount);
			
			//LOG.info(" binary retJsonObject " +retJsonObject);
			
			return retJsonObject;
			
		}

		return retJsonObject;

	}
	
	
	public void BINARY_BOOT(JSONObject json,String opcode, String id) {
		
		try {
			
			String mqttMsgTemplate = " \"opcode\":\"{0}\",\"by\":\"{1}\", \"type\":\"{2}\", \"value\":{3} ";
			String message = MessageFormat.format(mqttMsgTemplate, new Object[] { opcode, "qubercloud", "DFU", json });
			deviceEventMqttPub.publish("{" + message + "}", id);
			
			LOG.info("BINARY BOOT MQTT MESSAGE " + message +" " + id);
		} catch (Exception e) {
			LOG.warn("Failed to notify update", e);
		}
		
	}

	public static String checkSumMD5(String file) throws IOException {
		String checksum = "";
		FileInputStream inputStream = new FileInputStream(file);
		try {
			checksum = DigestUtils.md5Hex(inputStream);
			 LOG.info("Calulating MD5 checksum  "+checksum);
		} catch (IOException ex) {
			LOG.info("While Calulating MD5 checksum error ",ex);
		}finally{
			inputStream.close();
		}
		return checksum;
	}

	
	@RequestMapping(value = "/upgrade", method = RequestMethod.GET)
	public JSONArray upgrade(@RequestParam(value = "upgradeType", required = true) String upgradeType,
						     @RequestParam(value = "cid", 		required = true) String cid,
							 @RequestParam(value = "uid",required = true) String universalId,
						     HttpServletRequest request) throws IOException {

		JSONArray  array = new JSONArray ();
		
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			try {
				
				if (upgradeType == null || upgradeType.isEmpty()) {
					return array;
				}
				
				LOG.info(" upgradeType " + upgradeType);
				LOG.info(" upgrade cid " + cid);
				LOG.info(" upgrade universalId " + universalId);
							
				
				List<Device> device = deviceService.findByGlobal(universalId, universalId, universalId, universalId);
				if (device != null) {
					device.forEach(dev -> {
						JSONObject json = new JSONObject();
						String ip 		  = dev.getIp();
						String  version   = dev.getBuildVersion();
						String buildtime = dev.getBuildTime();
						String status = "Failure";
						if (dev.getBinaryReason() != null) {
							status = dev.getBinaryReason().toUpperCase();
						}
						version   = (version == null || version.isEmpty()) ? "UNKOWN" : version;
						buildtime = (buildtime == null || buildtime.isEmpty()) ? "UNKOWN" : buildtime;
						
						json.put("uid", 		dev.getUid());
						json.put("binaryreason",status);
						json.put("binaryType",  dev.getBinaryType());
						json.put("upgradeType", dev.getUpgradeType());
						json.put("ip",			ip);
						json.put("version",     version);
						json.put("buildtime",   buildtime);
						array.add(json);
						
					});
				}

				List<BeaconDevice> beaconDevice = beaconDeviceService.findByGlobal(universalId, universalId,universalId, universalId);
				if (beaconDevice != null) {
					device.forEach(dev -> {
						JSONObject json = new JSONObject();
						String ip 		  = dev.getIp();
						String  version   = dev.getBuildVersion();
						String buildtime = dev.getBuildTime();
						String status = "Failure";
						
						if (dev.getBinaryReason() != null) {
							status = dev.getBinaryReason().toUpperCase();
						}
						version   = (version == null || version.isEmpty()) ? "UNKOWN" : version;
						buildtime = (buildtime == null || buildtime.isEmpty()) ? "UNKOWN" : buildtime;
						
						json.put("uid", 		dev.getUid());
						json.put("binaryreason",status);
						json.put("binaryType",  dev.getBinaryType());
						json.put("upgradeType", dev.getUpgradeType());
						json.put("ip",			ip);
						json.put("version",     version);
						json.put("buildtime",   buildtime);
						array.add(json);
					});
				}

				addArrayToElasticSearch(cid,array,upgradeType,request);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return array;	
			
		}
		return array;
	}
	
	private void addArrayToElasticSearch(String cid, JSONArray array,String upgradeType, HttpServletRequest request) {
				
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			Map<String, Object> postData = new HashMap<String, Object>();
			String current_user 		 = "Unknown";
			Customer  cx 			     = customerService.findById(cid);
			TimeZone timezone 			 = CustomerUtils.FetchTimeZone(cx.getTimezone());
			
			DateFormat format 			 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			format.setTimeZone(timezone);
			
			String current_time 		= format.format(new Date());
			String updgradeEventTable   = "device-history-event";
		
			
			updgradeEventTable = _CCC.properties.getProperty("device.history.event.table", updgradeEventTable);
			current_user 	   = SessionUtil.currentUser(request.getSession());
			
			UserAccount user = userService.findOneByEmail(current_user);
			if(user != null){
				current_user = user.getFname()+" "+user.getLname();
			}
			
			postData.put("opcode",     "upgradeHistory");
			postData.put("cid", 	    cid);
			postData.put("userName",    current_user.toUpperCase());
			postData.put("upgradeType", upgradeType.toUpperCase());
			postData.put("time",	    current_time);
			postData.put("status", 		array);
			
			elasticService.post(updgradeEventTable, "device-upgrade", postData);
		}
	}

	@RequestMapping(value = "/upgradeHistory", method = RequestMethod.GET)
	public List<Map<String, Object>> upgradeHistory(@RequestParam(value = "cid", required = true) String cid,
													@RequestParam(value = "time", required = false) String timeInterval,
													HttpServletRequest request, HttpServletResponse response) {
		
		List<Map<String, Object>> upgradeHistory  = null;
		
		if (SessionUtil.isAuthorized(request.getSession())) {

			if (timeInterval == null) {
				timeInterval = "365d";
			}
			
			String indexname     = _CCC.properties.getProperty("device.history.event.table","device-history-event");
			String deviceUpgrade = "device-upgrade";
			
			String fsql = "index="+indexname+",type ="+deviceUpgrade+",sort=timestamp desc,"
					+ "query=timestamp:>now-" + timeInterval +" AND cid:" + cid + " AND opcode:\"upgradeHistory\" "
					+ "|value(userName,userName, NA);value(upgradeType,upgradeType,NA);value(time,time,NA);value(status,status,NA)|table ;";	

			upgradeHistory = fsqlRestController.query(fsql);
		}
		return upgradeHistory;
	}

	@RequestMapping(value = "/scannerList", method = RequestMethod.GET)
	public JSONObject locationlist(@RequestParam(value = "cid", required  = true) String cid,
								   @RequestParam(value = "type", required = true) String type,
								   HttpServletRequest request,HttpServletResponse response) {
		
		if (cid == null) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		
		JSONObject json 	= null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonList = new JSONObject();
	
		try {
			
			List<BeaconDevice> deviceType = beaconDeviceService.findByCidAndType(cid, type);
			for (BeaconDevice device : deviceType) {
				json = new JSONObject();
				json.put("uid",	 		 device.getName());
				String scanduration 	 = scanDuration(device.getConf());
				json.put("scanduration", scanduration);
				jsonArray.add(json);
			}
			
			
		} catch (Exception e) {
			LOG.error("While Scanner Duration getting error " + e);
		}
		jsonList.put("scanner", jsonArray);
		return jsonList;

	}
	
	public  String scanDuration(String conf) {

		String scanduration = "10";

		net.sf.json.JSONObject template   = net.sf.json.JSONObject.fromObject(conf);
		JSONArray jsonArray 		      = new JSONArray();
		net.sf.json.JSONObject jsonObject = new JSONObject();

		if (template.get("attributes") != null) {
			jsonArray = template.getJSONArray("attributes");
			if (jsonArray != null && jsonArray.size() > 0) {
				jsonObject = jsonArray.getJSONObject(0);
			}
		}

		if (jsonObject.get("scanduration") != null) {
			scanduration = jsonObject.getString("scanduration");
			scanduration = scanduration.split("\\.")[0];
		}
		LOG.info("scanduration " +scanduration);
		return scanduration;
	}
	
	@RequestMapping(value = "/debugByDevices", method = RequestMethod.POST)
	public Restponse<String>  debugByDevices(
		   @RequestParam(value = "sid",   		required = false) String  sid,
		   @RequestParam(value = "spid", 		required = false) String spid,
		   @RequestParam(value = "uid", 		required = false) String uid,
		   @RequestParam(value = "cid", 		required = false) String cid,
		   @RequestParam(value = "debugflag", 	required = true) String flag,
		   @RequestParam(value = "type",        required = false) String type,
		   HttpServletRequest request, 			HttpServletResponse response) throws IOException{

			boolean success = true;
			int code 		= 200;
			String body 	= "Successfully enabled debug option";
			
		try {

			String debug 			  			= "disable";
			String opcode 			 		 	= "device_logging";
			String  debugflag         			= "unchecked";
			
			ArrayList<BeaconDevice> deviceList  = new ArrayList<BeaconDevice>();
			
			if (flag.equals("true")) {
				debug 	  = "enable";
				debugflag = "checked";
			}

			String template = " \"opcode\":\"{0}\", \"device_uid\":\"{1}\",\"debug\":\"{2}\"";
			String message  =  "";
			
			if (StringUtils.hasLength(uid) && !StringUtils.hasLength(cid)) { // select by uid
				BeaconDevice device = beaconDeviceService.findOneByUid(uid);
				deviceList.add(device);
				LOG.info("find by uid " +uid);
			} else if (StringUtils.hasLength(cid)) { // select all
				List<BeaconDevice> beaconDevice = beaconDeviceService.findByCidAndType(cid,type);
				deviceList.addAll(beaconDevice);
				LOG.info("find by cid " +cid + " type " +type);
			}

			boolean sentMqtt= false;
			
			if (deviceList.size() > 0) {
				for (BeaconDevice dv : deviceList) {
					dv.setDebugflag(debugflag);
					dv.setModifiedOn(new Date());
					dv.setModifiedBy(whoami(request, response));
					beaconDeviceService.save(dv, false);
					uid 		= dv.getUid().toUpperCase();
					message 	= MessageFormat.format(template, new Object[] { opcode, uid, debug });
					deviceEventMqttPub.publish("{" + message + "}", uid);
					sentMqtt = true;
				}
			} else {
				success = false;
				code    = 404;
				body    = "Device not found";
			}
			
			if (sentMqtt) {
				success = true;
				code    = 200;
				body    = "Successfully enabled debug option";
			}

		} catch (Exception e) {
			 success = false;
			 code 	 = 500;
			 body 	= "an error occurred while enabling debug option " +e.getMessage();
		}
		
		return new Restponse<String>(success, code, body);
	}
	
	@RequestMapping(value = "/buildversion", method = RequestMethod.GET)
	public JSONObject buildversion(
			@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "uid", required = true) String uid,
			HttpServletRequest request,HttpServletResponse response) {

		JSONObject json = new JSONObject();
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			if ((uid != null && uid.equalsIgnoreCase("all"))) {
				return json;
			}

			if (cid == null || cid.isEmpty()) {
				cid = SessionUtil.getCurrentCustomer(request.getSession());
			}
			
			String version 			  = "UNKNOWN";
			String buildtime   		  = "UNKNOWN";
			
			Device device 			  = null;
			BeaconDevice beaconDevice = null;
			
			device 		 = deviceService.findOneByUid(uid);
			beaconDevice = beaconDeviceService.findOneByUid(uid);
			
			if (CustomerUtils.Gateway(cid) || CustomerUtils.Heatmap(cid)) {
					json.put("version",   version);
					json.put("buildtime", buildtime);
				return json;
				
			} else if (CustomerUtils.GeoFinder(cid)) {
				
				if (beaconDevice != null && uid.equalsIgnoreCase(beaconDevice.getUid())) {
					
					version = beaconDevice.getVersion();
					buildtime = beaconDevice.getBuild();
					
					version   = (version == null || version.isEmpty()) ? "UNKOWN" : version;
					buildtime = (buildtime == null || buildtime.isEmpty()) ? "UNKOWN" : buildtime;
					
					json.put("version", version);
					json.put("buildtime", buildtime);
				}
				
				return json;

			} else if (CustomerUtils.GatewayFinder(cid)){
				
				if (device != null && uid.equalsIgnoreCase(device.getUid())) {
					json.put("version", version);
					json.put("buildtime", buildtime);
					return json;
				}
				if (beaconDevice != null && uid.equalsIgnoreCase(beaconDevice.getUid())) {
					
					version = beaconDevice.getVersion();
					buildtime = beaconDevice.getBuild();
					
					version   = (version == null || version.isEmpty()) ? "UNKOWN" : version;
					buildtime = (buildtime == null || buildtime.isEmpty()) ? "UNKOWN" : buildtime;
					
					json.put("version", version);
					json.put("buildtime", buildtime);
					return json;
				}
			}
		}

		return json;

	}
	
	@RequestMapping(value = "/binaryDeviceUid", method = RequestMethod.GET)
	public JSONObject binaryDeviceUid(
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid, HttpServletRequest request,
			HttpServletResponse response) {

		JSONObject jsonList = new JSONObject();
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			JSONObject json 		= null;
			JSONArray jsonArray 	= new JSONArray();
			
			if ((sid != null && sid.equalsIgnoreCase("all")) || (spid != null && spid.equalsIgnoreCase("all"))) {
				return jsonList;
			}

			if (cid == null || cid.isEmpty()) {
				cid = SessionUtil.getCurrentCustomer(request.getSession());
			}
			
			List<Device> device 			= null;
			List<BeaconDevice> beaconDevice = null;
			
			device 		 = deviceService.findBySpid(spid);
			beaconDevice = beaconDeviceService.findBySpid(spid);
			
			if (CustomerUtils.Gateway(cid) || CustomerUtils.Heatmap(cid)) {
				
				for(Device dev : device) {
					json = new JSONObject();
					json.put("uid", dev.getUid());
					json.put("name", dev.getName());
					jsonArray.add(json);
				}
				jsonList.put("location", jsonArray);
				
				return jsonList;
			} else if (CustomerUtils.GeoFinder(cid)) {
				
				for (BeaconDevice dev : beaconDevice) {
					json = new JSONObject();
					json.put("uid", dev.getUid());
					json.put("name", dev.getName());
					jsonArray.add(json);
				}
				
				jsonList.put("location", jsonArray);
				return jsonList;

			} else if (CustomerUtils.GatewayFinder(cid)){
				
				for(Device dev : device) {
					json = new JSONObject();
					json.put("uid", dev.getUid());
					json.put("name", dev.getName());
					jsonArray.add(json);
				}
				
				for (BeaconDevice dev : beaconDevice) {
					json = new JSONObject();
					json.put("uid",  dev.getUid());
					json.put("name", dev.getName());
					jsonArray.add(json);
				}
				jsonList.put("location", jsonArray);
				
				return jsonList;
			}

		}

		return jsonList;

	}
	
	@RequestMapping(value = "/ibeaconDelete", method = RequestMethod.GET)
	public Restponse<String> ibeaconDelete(@RequestParam("uid") String uid,@RequestParam("cid") String cid,
								HttpServletRequest request,HttpServletResponse response) throws Exception {

		boolean success = false;
		int code 		= 500;
		String body 	= "while delete device occure error";
		
		try {
			
			String type = null;
			
			BeaconDevice device = beaconDeviceService.findOneByUid(uid);
			if (device != null) {
				type = device.getType();
				beaconDeviceService.delete(device);
				device.setId(null);
			}
			
			beaconDeviceService.resetServerIP(type,cid);
			
			success = true;
			code 	= 200;
			body 	= "device has been deleted successfully.";
			
		} catch (Exception e) {
			body 	= "while delete device occure error." +e.getMessage();
			success = false;
			code 	= 500;
		}
		
		return new Restponse<String>(success, code, body);
		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/filter/list", method = RequestMethod.POST)
	public JSONArray list(@RequestParam(value= "cid",required = true) String cid,
			@RequestParam(value = "sid", required = false) List<String> sid,
			@RequestParam(value = "spid", required = false) List<String> spid,
			@RequestParam(value = "type", required = true) String type) {
		
		JSONArray beaconDeviceArray = new JSONArray();
		List<BeaconDevice> beacondeviceList = null;
		if (!CollectionUtils.isEmpty(spid)) {
			beacondeviceList = beaconDeviceService.findBySpidInAndType(spid, type);
		} else if (!CollectionUtils.isEmpty(sid)) {
			beacondeviceList = beaconDeviceService.findBySidInAndType(sid, type);
		} else {
			beacondeviceList = beaconDeviceService.findByCidAndType(cid, type);
		}

		JSONObject json = null;
		for (BeaconDevice beaconDevice : beacondeviceList) {
			json = new JSONObject();
			json.put("id", beaconDevice.getUid());
			json.put("name", beaconDevice.getName());
			beaconDeviceArray.add(json);
		}
		return beaconDeviceArray;
	}
	

	/**
	 * Used to Import Gateways
	 * @param cid
	 * @param request
	 * @param response
	 * @return
	 */
/*	
@RequestMapping(value = "/gatewayBulkUpload", method = RequestMethod.POST)
	public  Restponse<String> gatewayBulkUpload(
			@RequestParam("cid") final String cid, MultipartHttpServletRequest request,
			HttpServletResponse response) {

		int code 		= 200;
		boolean success = true;
		String body 	= "Gateways has been imported successfully.";
		
		boolean isInvalidDevice  = false;
		boolean isAppenedMessage = false;
		boolean isvalidFileFormat  =false;
		String GatewayMacValidation = "^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$";
		boolean isSaved    = true;
		
		try {
			
			Iterator<String> itrator = request.getFileNames();
			MultipartFile multiFile  = request.getFile(itrator.next());
			

			LOG.info(multiFile.getContentType());

			if (multiFile.getContentType().equals("text/plain")) {
				isvalidFileFormat = true;
				LOG.info("given text in .txt format");
			} else if (multiFile.getContentType()
					.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
				isvalidFileFormat = true;
				LOG.info("given txt in docx format");
			} else if (multiFile.getContentType().equals("application/msword")) {
				isvalidFileFormat = true;
			   LOG.info("given text in .doc format");
			} else if (multiFile.getContentType().equals("application/octet-stream")) {
				isvalidFileFormat = true;
				LOG.info("given file with no extension");
			}
			if (!isvalidFileFormat) {
				body = "File imported is not in the expected format";
				success = false;
				code = 500;

				return new Restponse<String>(success, code, body);

			}

			String content = new String(multiFile.getBytes(), "UTF-8");
			
			if (StringUtils.hasLength(content)) {
				
				
				String gateways[] = content.split("\n");
				
				for (String uid : gateways) {
					
					 
					String deviceUid  = StringUtils.trimAllWhitespace(uid);
					
					if(!deviceUid.isEmpty()) {
				
					Pattern pattern = Pattern.compile(GatewayMacValidation);
					Matcher matcher = pattern.matcher(deviceUid);
				
					if (matcher.matches()) {
						BeaconDevice beaconDevice  = beaconDeviceService.findOneByUid(deviceUid);
						if (beaconDevice != null) {
							String status = BeaconDevice.STATUS.REGISTERED.name();
							String beaconDeviceStatus = beaconDevice.getStatus();
							if (status.equalsIgnoreCase(beaconDeviceStatus)) {
								isSaved = true;
							} else {
								isSaved = false;
								LOG.info("Duplicate Device " + uid);
							}
						} else {
							beaconDevice = new BeaconDevice();
							beaconDevice.setUid(deviceUid);
							beaconDevice.setCreatedBy(whoami(request, response));
							beaconDevice.setCreatedOn(new Date());
						}

						if (isSaved && beaconDevice != null) {
							
							String conf     = SpringComponentUtils.getApplicationMessages().getMessage("facesix.beacon.device.template.default");
							String template = String.valueOf(conf);
							
							beaconDevice.setName(deviceUid);
							beaconDevice.setCid(cid);
							beaconDevice.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
							beaconDevice.setState("active");
							beaconDevice.setTemplate(template);
							beaconDevice.setConf(template);
							beaconDevice.setType("receiver");
							beaconDevice.setIp("0.0.0.0");
							beaconDevice.setKeepAliveInterval("3");
							beaconDevice.setTlu(1);
							beaconDevice.setSource("qubercomm");
							beaconDevice.setTypefs("sensor");
							beaconDevice.setDescription("bukimported");
							
							beaconDevice.setModifiedBy(whoami(request, response));
							beaconDevice.setModifiedOn(new Date());
							
							beaconDevice = beaconDeviceService.save(beaconDevice, true);

						}
					} else {
						isInvalidDevice = true;
						LOG.info("Invalid Device Uid" +deviceUid);
					}
				}
				}
					
					if (( !isSaved )&& isInvalidDevice) {
						body = "Gateway MAC addresses that already existed  and Invalid Gateway MAC address were found are skipped";
					}
					else if(!isSaved) {
						body = "Gateway MAC addresses that already existed are skipped";
					}
					else if (isInvalidDevice) {
						if (isSaved == false && isAppenedMessage == false) {
							isAppenedMessage = true;
							body += "Invalid gateway MAC addresses were found that are skipped.";
						} else {
							body = "Invalid gateway MAC addresses were found that are skipped.";
						}
						
					}
				
			} else {
				code = 404;
				success = false;
				body = "Empty file";
			}
		} catch (Exception e) {
			code = 500;
			success = false;
			body = "Error occurred during gateway bulk import process" + e.getMessage();
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);
	}*/
	/**
	 * Used to Import Gateways
	 * Expecting in.xls,.xlsx,.csv format
	 * @param cid
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "/gatewayBulkUpload", method = RequestMethod.POST)
	public  Restponse<String> gatewayBulkUpload1(
			@RequestParam("cid") final String cid, MultipartHttpServletRequest request,
			HttpServletResponse response) throws IOException {

		int code = 200;
		boolean success = true;
		String body = "File found was empty !! ";
		boolean isValidFileFormat;
		Workbook workbook = null;
		try {
			Iterator<String> itrator = request.getFileNames();
			MultipartFile multiFile = request.getFile(itrator.next());
			isValidFileFormat=fileImportUtil.fileValidation(multiFile);
			if (!isValidFileFormat) {
				body = "File imported is not in the expected format";
				success = false;
				code = 415;
				return new Restponse<String>(success, code, body);
			}
			else {
				workbook=fileImportUtil.workBookCreation(multiFile);
			}

			if (workbook != null) {
				Restponse<String> fileResponse=fileImportUtil.excelFileProcessing(workbook, cid,whoami(request,response));
				body=fileResponse.getBody();
				code=fileResponse.getCode();
				success=fileResponse.isSuccess();
			} else {
				String content = new String(multiFile.getBytes(), "UTF-8");
				if (!StringUtils.isEmpty(content)) {
					Restponse<String> fileResponse=fileImportUtil.csvFileProcessing(content,cid,whoami(request,response));
					body=fileResponse.getBody();
					code=fileResponse.getCode();
					success=fileResponse.isSuccess();
				}
				else {
					body = "File found was empty !! ";
					success = false;
					code = 412;
				}
			}
		} catch (Exception e) {
			code = 500;
			success = false;
			body = "Error occurred during gateway bulk import process" + e.getMessage();

		}
		finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					LOG.error("Error while trying to close the workbook" + e.getMessage());
				}
			}
		}
		return new Restponse<String>(success, code, body);
	}
	
	/**
	 * Used to list out unConfigured devices in floor
	 * @param cid
	 * @return
	 */
	
	@RequestMapping(value = "/nonConfiguredDeviceInFloor", method = RequestMethod.GET)
	public JSONArray nonConfiguredDeviceInFloor(@RequestParam("cid") String cid) {

		JSONArray deviceArray = new JSONArray();
		
		List<BeaconDevice> deviceList = beaconDeviceService.findByCid(cid);
		
		if (deviceList != null) {
			for (BeaconDevice device : deviceList) {
				String spid = device.getSpid();
				
				if (StringUtils.isEmpty(spid)) {
					
					String conf    = device.getConf();
					String source  = (!StringUtils.hasLength(device.getSource())) ? "qubercomm" :  device.getSource();
					
					JSONObject jsonObject = new JSONObject();
					
					jsonObject.put("id", 		 device.getId());
					jsonObject.put("mac_address",device.getUid());
					jsonObject.put("alias", 	 device.getName());
					jsonObject.put("status",	 device.getStatus());
					jsonObject.put("cid", 		 cid);
					jsonObject.put("conf", 		 conf);
					jsonObject.put("source",     source);
					
					deviceArray.add(jsonObject);
				} else {
					continue;
				}
			}
		}
		return deviceArray;
	}
}
