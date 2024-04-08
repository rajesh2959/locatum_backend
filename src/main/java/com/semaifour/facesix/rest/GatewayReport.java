package com.semaifour.facesix.rest;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import com.semaifour.facesix.data.elasticsearch.ElasticsearchConfiguration;
import com.semaifour.facesix.data.mongo.device.ClientDevice;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.stats.ClientCache;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.HeaderFooterPageEvent;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

@RestController
@RequestMapping("/rest/gatewayreport")
public class GatewayReport extends WebController {
	
	static Logger LOG = LoggerFactory.getLogger(GatewayReport.class.getName());
	
	
	static Font smallBold  = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	static Font catFont    = new Font(Font.FontFamily.HELVETICA,   16, Font.BOLD);
	static Font redFont    = new Font(Font.FontFamily.HELVETICA,   10, Font.NORMAL);
	static Font subFont    = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
	static Font headerFont = new Font(Font.FontFamily.HELVETICA,   12, Font.BOLD);
    
    DateFormat format 			   = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	TimeZone timezone     		   = null;
	
    @Autowired
   	NetworkDeviceRestController 	networkDeviceRestController;
    
    @Autowired
	SiteService siteService;
    
	@Autowired
	PortionService portionService;
	
	@Autowired
	DeviceService devService;
	
	@Autowired
	CustomerService customerservice;
	
	@Autowired
	UserAccountService userAccountService;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	CustomerUtils customerUtils;		
	
	@Autowired
	DeviceService deviceservice;
	
	@Autowired
	DeviceRestController deviceRestController;
	
	@Autowired
	ClientCache clientCache;
	

	private String indexname = "facesix*";
	
	@Autowired
	FSqlRestController 		fsqlRestController;
	
	@Autowired
	ClientDeviceService  _clientDeviceService;
	
	@Autowired
	ElasticsearchConfiguration elasticsearchConfiguration;
	
	String 	device_history_event = "device-history-event";
	
	@PostConstruct
	public void init() {
		indexname 		= _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
		device_history_event = _CCC.properties.getProperty("facesix.device.event.history.table",device_history_event);
	}
	
	@RequestMapping(value = "/format", method = RequestMethod.GET)
	public String format(
			@RequestParam(value = "cid", 		required = false) String cid,
			@RequestParam(value = "venuename",  required = false) String sid,
			@RequestParam(value = "floorname",  required = false) String spid,
			@RequestParam(value = "location", 	required = false) String location,
			@RequestParam(value = "macaddr", 	required = false) String macaddr,
			@RequestParam(value = "filtertype", required = true)  String filtertype,
			@RequestParam(value = "time", 		required = false) String days,
			@RequestParam(value = "fileformat", required = true)  String fileformat,
			@RequestParam(value = "devStatus",  required = false) String devStatus,
			HttpServletRequest request,HttpServletResponse response) throws IOException, ParseException {
		
			String result = "";
			
			if (fileformat.equals("pdf")) {
				result = pdf(cid,sid,spid,location,macaddr,filtertype,days,devStatus,request,response);
			} else {
				result = csv(cid,sid,spid,location,macaddr,filtertype,days,devStatus, request,response);
			}
		
		return result;
	}
	
	public String pdf(
					@RequestParam(value = "cid", 		required = false) String cid,
					@RequestParam(value = "venuename",  required = false) String sid,
					@RequestParam(value = "floorname",  required = false) String spid,
					@RequestParam(value = "location", 	required = false) String location,
					@RequestParam(value = "macaddr", 	required = false) String macaddr,
					@RequestParam(value = "filtertype", required = true)  String filtertype,
					@RequestParam(value = "time", 		required = false) String days,
					@RequestParam(value = "devStatus", required = false) String devStatus,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		
		String pdfFileName  = "./uploads/qubercloud.pdf";
		String logoFileName = "./uploads/logo-home.png";
		
		//LOG.info("PDF func: cid "+cid+" sid "+sid+" spid "+spid + " macaddr "+macaddr+" time "+days);
		
		//String pdfFileName  = "Report.pdf";
		//String logoFileName = "/home/qubercomm/Desktop/pdf/logo.png";
		
		FileOutputStream os       		  = null; 
		FileInputStream fileInputStream   = null;
		OutputStream responseOutputStream = null;
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			Document document = new Document(PageSize.A4, 36, 36, 90, 55);
			try {
				
				if (cid == null) {
					cid = SessionUtil.getCurrentCustomer(request.getSession());
					if (cid == null) {
						return null;
					}
				}
				
				String currentuser 		= SessionUtil.currentUser(request.getSession());
				UserAccount cur_user 	= userAccountService.findOneByEmail(currentuser);
				String userName 	    = cur_user.getFname() + " " + cur_user.getLname();

				Customer customer = customerservice.findById(cid);
				logoFileName = customer.getLogofile() == null ? logoFileName : customer.getLogofile();
				String customerName = customer.getCustomerName();
				
				Path path = Paths.get(logoFileName);
				
				if (!Files.exists(path)) {
					logoFileName = "./uploads/logo-home.png";
				}
				timezone = customerUtils.FetchTimeZone(customer.getTimezone());
				format.setTimeZone(timezone);
				
				os = new FileOutputStream(pdfFileName);
				PdfWriter writer = PdfWriter.getInstance(document, os);
				HeaderFooterPageEvent event = new HeaderFooterPageEvent(customerName, userName, logoFileName, format.format(new Date()));
				writer.setPageEvent(event);
				document.open();
			    
			    addContent (writer,cid, sid, spid, location, macaddr, filtertype, days, devStatus, document);			    
				
				document.close();

				File pdfFile = new File(pdfFileName);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
				response.setContentLength((int) pdfFile.length());
				fileInputStream = new FileInputStream(pdfFile);
			    responseOutputStream = response.getOutputStream();
				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (responseOutputStream != null) {
					responseOutputStream.close();
				}
				if (fileInputStream != null) {
					fileInputStream.close();
				}
				if (os != null) {
					os.close();
				}
			}
		}
		return pdfFileName;
	}
	
	private void addContent(PdfWriter writer,String cid, String sid, String spid, String location, String macaddr, String filtertype,
			String days, String devStatus, Document document) throws DocumentException, IOException, ParseException {

		Paragraph subCatPart =  new Paragraph();
		document.add(subCatPart);
		createTable(writer,cid, sid, spid, location, macaddr, filtertype, days, devStatus, subCatPart, document);

	}
	  
	@SuppressWarnings({ "unchecked", "deprecation" })
	private  void createTable(PdfWriter writer,	String cid, String sid, String  spid, String  location, String  macaddr,
				String filtertype, String days, String  devStatus, Paragraph subCatPart, 
				Document document) throws IOException, ParseException, DocumentException {

		//LOG.info("CreateTable filtertype" + filtertype+" cid "+cid+" sid "+sid+" spid "+spid + " macaddr "+macaddr+ "location"+ location + " time "+days);

		if(filtertype.equals("deviceInfo")){
			
			Paragraph content = new Paragraph("Device Information",subFont);
			PdfPTable table   = new PdfPTable(9);
			table.setWidthPercentage(100);
		
			PdfPCell c1 = new PdfPCell(new Phrase("UID",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setColspan(2);
			table.addCell(c1);
			
			c1 = new PdfPCell(new Phrase("FLOOR",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);
			
			c1 = new PdfPCell(new Phrase("LOCATION",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);
			
			c1 = new PdfPCell(new Phrase("DEVICE UPTIME",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);
			
			c1 = new PdfPCell(new Phrase("APP UPTIME",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);
			
			c1 = new PdfPCell(new Phrase("STATE",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);
			
			c1 = new PdfPCell(new Phrase("LAST SEEN",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setColspan(2);
			table.addCell(c1);
			
			table.setHeaderRows(1);
			
			if(sid != null && (sid.equals("all") || sid.equals("undefined"))){
				sid = "";
			}
			if(spid != null && (spid.equals("all") || spid.equals("undefined"))){
				spid = "";
			}
			
			
			JSONArray processedDetail 	= deviceInfo(cid, sid, spid, null, null);
			Iterator<JSONObject> iterProcessedDetail = processedDetail.iterator();
			JSONObject json = null;
			
			while(iterProcessedDetail.hasNext()) {
				
				json = iterProcessedDetail.next();

				String uid 			= json.get("uid").toString();
				String floorname 	= json.get("floorname").toString();
				String locationname = json.get("locationname").toString();
				String deviceUptime = json.get("deviceUptime").toString();
				String appUptime 	= json.get("appUptime").toString();
				String state 		= json.get("state").toString();
				String lastseen 	= json.get("lastseen").toString();
				
				c1 = new PdfPCell(new Phrase(uid,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				c1.setColspan(2);
				table.addCell(c1);
				
				c1 = new PdfPCell(new Phrase(floorname,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);
				
				c1 = new PdfPCell(new Phrase(locationname,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);
				
				c1 = new PdfPCell(new Phrase(deviceUptime,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);
				
				c1 = new PdfPCell(new Phrase(appUptime,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);
				
				c1 = new PdfPCell(new Phrase(state,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);
				
				c1 = new PdfPCell(new Phrase(lastseen,redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				c1.setColspan(2);
				table.addCell(c1);
				
			}
			content.add(table);
			subCatPart.add(content);
			document.add(subCatPart);
			
		} else if (filtertype.equals("dev_inact_histy")) {
			
			Paragraph content = new Paragraph("Device Inactivity Histogram",subFont);
			
			PdfPTable table = new PdfPTable(6);
			table.setWidthPercentage(100);
			
			PdfPCell cell = new PdfPCell(new Phrase("UID", headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase("Location", headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase("Last Seen", headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase("Online", headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase("Down Time", headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			
			cell= new PdfPCell(new Phrase("Reason", headerFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			
			JSONArray dev_inact_histy = dev_inact_histy(cid, days);

			if (dev_inact_histy != null) {
				
				Iterator<JSONObject> it = dev_inact_histy.iterator();
				
				while (it.hasNext()) {
					
					JSONObject map = it.next();
					
					String uid 			= (String) map.get("uid");
					String lastseen 	=  map.get("lastseen").toString();
					String onlinetime 	=  map.get("onlinetime").toString();
					String alias 		= (String) map.getOrDefault("location", "AP");
					String reason 		= (String) map.getOrDefault("reason", "Unkown");
					
					long elp = 0;

					if (map.containsKey("elp")) {
						elp =  TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(map.get("elp").toString()));
					}

					String downTime = CustomerUtils.secondsto_hours_minus_days(elp);
					
					PdfPCell data_point = new PdfPCell(new Phrase(uid,redFont));
					data_point.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(data_point);
					
					data_point = new PdfPCell(new Phrase(alias,redFont));
					data_point.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(data_point);
					
					data_point = new PdfPCell(new Phrase(lastseen,redFont));
					data_point.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(data_point);
					
					data_point = new PdfPCell(new Phrase(onlinetime,redFont));
					data_point.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(data_point);
					
					data_point = new PdfPCell(new Phrase(downTime,redFont));
					data_point.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(data_point);
					
					data_point = new PdfPCell(new Phrase(reason,redFont));
					data_point.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(data_point);
				}

				content.add(table);
				subCatPart.add(content);
				document.add(subCatPart);

			} else {
				document.add(addNoDataToPDF(subCatPart));
			}

		} else {

			List<Device> devices = null;
			
			JSONObject details		= null;
			boolean venuewise 		= false;
			boolean floorwise 		= false;
			boolean customerwise 	= false;
			
			
			switch (filtertype) {
			case "default":
				devices = deviceservice.findByCid(cid);
				break;
				
			case "venue":
				
				if (sid.isEmpty() || sid.equals("all"))  {
					devices = deviceservice.findByCid(cid);
					customerwise = true;
					details = reportDetails("cid",days,devices);
				} else {
					devices = deviceservice.findBySid(sid);
					venuewise = true;
					details = reportDetails("sid",days,devices);
				}
			
				break;
				
			case "floor":
				
				if (sid.isEmpty() || sid.equals("all")) {
					customerwise = true;
					devices = deviceservice.findByCid(cid);
					details = reportDetails("cid",days,devices);
				} else if(spid.isEmpty() || spid.equals("all")) {
					venuewise = true;
					devices = deviceservice.findBySid(sid);
					details = reportDetails("sid",days,devices);
				} else {
					floorwise = true;
					devices = deviceservice.findBySpid(spid);
					details = reportDetails("spid",days,devices);
				}
				break;
				
			case "location":
				
				if (sid.isEmpty() || sid.equals("all")) {
					customerwise = true;
					devices = deviceservice.findByCid(cid);
					details = reportDetails("cid",days,devices);
				} else if(spid.isEmpty() || spid.equals("all")) {
					venuewise = true;
					devices = deviceservice.findBySid(sid);
					details = reportDetails("sid",days,devices);
				} else if (location.isEmpty() || location.equals("all")) {
					floorwise = true;
					devices = deviceservice.findBySpid(spid);
					details = reportDetails("spid",days,devices);
				} else {
					devices = deviceservice.findByUid(location);
					details = reportDetails("uid",days,devices);
				}
				break;
				
			case "devStatus":
				devices = (List)getDeviceByCidAndStatus(cid, devStatus);
			}
			
			/*
			 *  AGG Device TX,RX,STATION CLIENTS
			 * 
			 */
			
			PdfPTable agg_dev_tx_rx_Table = new PdfPTable(2);
			agg_dev_tx_rx_Table.setWidthPercentage(100);
			
			PdfPCell a1 = new PdfPCell(new Phrase("Total TX Bytes ",headerFont));
			a1.setHorizontalAlignment(Element.ALIGN_CENTER);
			agg_dev_tx_rx_Table.addCell(a1);

			a1 = new PdfPCell(new Phrase("Total RX Bytes",headerFont));
			a1.setHorizontalAlignment(Element.ALIGN_CENTER);
			agg_dev_tx_rx_Table.addCell(a1);

			agg_dev_tx_rx_Table.setHeaderRows(1);
			
			/*
			 *  DEVICE WISSE TX RX TABLE 
			 * 
			 */
			
			PdfPTable dev_tx_rx_Table = new PdfPTable(4);
			dev_tx_rx_Table.setWidthPercentage(100);
			
			PdfPCell b1 = new PdfPCell(new Phrase("Uid ",headerFont));
			b1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_tx_rx_Table.addCell(b1);

			b1 = new PdfPCell(new Phrase("Location",headerFont));
			b1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_tx_rx_Table.addCell(b1);
			
			b1 = new PdfPCell(new Phrase("TX Bytes ",headerFont));
			b1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_tx_rx_Table.addCell(b1);

			b1 = new PdfPCell(new Phrase("RX Bytes",headerFont));
			b1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_tx_rx_Table.addCell(b1);


			dev_tx_rx_Table.setHeaderRows(1);
			
			/*
			 *   2G AND 5G CLIENTS TABLE
			 * 
			 */
			
			PdfPTable avg_2G_5G_Table = new PdfPTable(2);
			avg_2G_5G_Table.setWidthPercentage(100);
			
			PdfPCell c1 = new PdfPCell(new Phrase("Avg 2G",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			avg_2G_5G_Table.addCell(c1);

			c1 = new PdfPCell(new Phrase("Avg 5G",headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			avg_2G_5G_Table.addCell(c1);

			/*
			 *  AP wise avg 2G and 5G
			 * 
			 * 
			 */
			
			PdfPTable device_avg_2G_5G_Table = new PdfPTable(4);
			device_avg_2G_5G_Table.setWidthPercentage(100);
			
			PdfPCell d1 = new PdfPCell(new Phrase("Uid ",headerFont));
			d1.setHorizontalAlignment(Element.ALIGN_CENTER);
			device_avg_2G_5G_Table.addCell(d1);

			d1 = new PdfPCell(new Phrase("Location",headerFont));
			d1.setHorizontalAlignment(Element.ALIGN_CENTER);
			device_avg_2G_5G_Table.addCell(d1);
		
			d1 = new PdfPCell(new Phrase("Avg 2G",headerFont));
			d1.setHorizontalAlignment(Element.ALIGN_CENTER);
			device_avg_2G_5G_Table.addCell(d1);

			d1 = new PdfPCell(new Phrase("Avg 5G",headerFont));
			d1.setHorizontalAlignment(Element.ALIGN_CENTER);
			device_avg_2G_5G_Table.addCell(d1);

			device_avg_2G_5G_Table.setHeaderRows(1);
			
			/*
			 *  MIN AND MAX 2G AND 5G
			 */
			
			PdfPTable min_max_2G_5G_Table = new PdfPTable(4);
			min_max_2G_5G_Table.setWidthPercentage(100);
			
			PdfPCell e1 = new PdfPCell(new Phrase("Min 2G ",headerFont));
			e1.setHorizontalAlignment(Element.ALIGN_CENTER);
			min_max_2G_5G_Table.addCell(e1);

			e1 = new PdfPCell(new Phrase("Max 2G ",headerFont));
			e1.setHorizontalAlignment(Element.ALIGN_CENTER);
			min_max_2G_5G_Table.addCell(e1);

			e1 = new PdfPCell(new Phrase("Min 5G ",headerFont));
			e1.setHorizontalAlignment(Element.ALIGN_CENTER);
			min_max_2G_5G_Table.addCell(e1);

			e1 = new PdfPCell(new Phrase("Max 5G ",headerFont));
			e1.setHorizontalAlignment(Element.ALIGN_CENTER);
			min_max_2G_5G_Table.addCell(e1);

			min_max_2G_5G_Table.setHeaderRows(1);
			
			/*
			 *  STATION WISSE MIN AND MAX 25 AND 5G
			 */
			
			PdfPTable dev_min_max_2G_5G_Table = new PdfPTable(6);
			dev_min_max_2G_5G_Table.setWidthPercentage(100);
			
			PdfPCell f1 = new PdfPCell(new Phrase("Uid ",headerFont));
			f1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_min_max_2G_5G_Table.addCell(f1);

			f1 = new PdfPCell(new Phrase("Location",headerFont));
			f1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_min_max_2G_5G_Table.addCell(f1);
			
			f1 = new PdfPCell(new Phrase("Min 2G ",headerFont));
			f1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_min_max_2G_5G_Table.addCell(f1);

			f1 = new PdfPCell(new Phrase("Max 2G ",headerFont));
			f1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_min_max_2G_5G_Table.addCell(f1);

			f1 = new PdfPCell(new Phrase("Min 5G ",headerFont));
			f1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_min_max_2G_5G_Table.addCell(f1);

			f1 = new PdfPCell(new Phrase("Max 5G ",headerFont));
			f1.setHorizontalAlignment(Element.ALIGN_CENTER);
			dev_min_max_2G_5G_Table.addCell(f1);

			dev_min_max_2G_5G_Table.setHeaderRows(1);
			
			/*
			 *  LOCATION WISE CLIENT COUNT AND BANDWIDTH
			 * 
			 */
		
			PdfPTable client_count_bandwidth_Table = new PdfPTable(6);
			client_count_bandwidth_Table.setWidthPercentage(100);
			
			PdfPCell g1 = new PdfPCell(new Phrase("Uid ",headerFont));
			g1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_count_bandwidth_Table.addCell(g1);

			g1 = new PdfPCell(new Phrase("Location",headerFont));
			g1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_count_bandwidth_Table.addCell(g1);
			
			g1 = new PdfPCell(new Phrase("TX Bytes ",headerFont));
			g1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_count_bandwidth_Table.addCell(g1);

			g1 = new PdfPCell(new Phrase("RX Bytes",headerFont));
			g1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_count_bandwidth_Table.addCell(g1);

			g1 = new PdfPCell(new Phrase("Avg 2G",headerFont));
			g1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_count_bandwidth_Table.addCell(g1);

			g1 = new PdfPCell(new Phrase("Avg 5G",headerFont));
			g1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_count_bandwidth_Table.addCell(g1);

			client_count_bandwidth_Table.setHeaderRows(1);
			
			/*
			 *  11K ,11R AND 11V COUNT
			 * 
			 */
		
			PdfPTable client_Capability_Table = new PdfPTable(5);
			client_Capability_Table.setWidthPercentage(100);
			
			PdfPCell h1 = new PdfPCell(new Phrase("Uid ",headerFont));
			h1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_Capability_Table.addCell(h1);

			h1 = new PdfPCell(new Phrase("Location",headerFont));
			h1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_Capability_Table.addCell(h1);
			
			h1 = new PdfPCell(new Phrase("11K ",headerFont));
			h1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_Capability_Table.addCell(h1);

			h1 = new PdfPCell(new Phrase("11R",headerFont));
			h1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_Capability_Table.addCell(h1);

			h1 = new PdfPCell(new Phrase("11V",headerFont));
			h1.setHorizontalAlignment(Element.ALIGN_CENTER);
			client_Capability_Table.addCell(h1);

			client_Capability_Table.setHeaderRows(1);
			
			/*
			 *  TOP 5 CLIENT CONSUMED TX  
			 * 
			 */
		
			PdfPTable top_5_clients_consumed_TX_Table = new PdfPTable(2);
			top_5_clients_consumed_TX_Table.setWidthPercentage(100);
			
			PdfPCell i1 = new PdfPCell(new Phrase("Mac ID  ",headerFont));
			i1.setHorizontalAlignment(Element.ALIGN_CENTER);
			top_5_clients_consumed_TX_Table.addCell(i1);

			i1 = new PdfPCell(new Phrase("TX",headerFont));
			i1.setHorizontalAlignment(Element.ALIGN_CENTER);
			top_5_clients_consumed_TX_Table.addCell(i1);
			
			top_5_clients_consumed_TX_Table.setHeaderRows(1);
			/*
			 *  TOP 5 CLIENT CONSUMED RX  
			 * 
			 */
		
			PdfPTable top_5_clients_RX_Table = new PdfPTable(2);
			top_5_clients_RX_Table.setWidthPercentage(100);
			
			PdfPCell j1 = new PdfPCell(new Phrase("Mac ID  ",headerFont));
			j1.setHorizontalAlignment(Element.ALIGN_CENTER);
			top_5_clients_RX_Table.addCell(j1);

			j1 = new PdfPCell(new Phrase("RX",headerFont));
			j1.setHorizontalAlignment(Element.ALIGN_CENTER);
			top_5_clients_RX_Table.addCell(j1);
			
			top_5_clients_RX_Table.setHeaderRows(1);
			
			final DefaultCategoryDataset dev_tx_rx_Dataset = new DefaultCategoryDataset();
			
			Image devtxrxbarChartImage 		= null;
			
			final DefaultCategoryDataset client_capability_Dataset = new DefaultCategoryDataset();
			Image client_capability_barChartImage 		= null;
			
			String title = "Location";

			if (customerwise) {
				title = "Customer";
			} else if (venuewise) {
				title = "Venue";
			} else if (floorwise) {
				title = "Floor";
			}
			
			if (details != null && details.size() > 0) {
				
				if (details.containsKey("agg_txrx")) {

					JSONObject JStr = (JSONObject)details.get("agg_txrx");
					
					String agg_tx = (String)JStr.get("agg_tx");
					String agg_rx = (String)JStr.get("agg_rx");
					
					
					agg_tx  = CustomerUtils.formatFileSize(Long.valueOf(agg_tx));
					agg_rx  = CustomerUtils.formatFileSize(Long.valueOf(agg_rx));
					
					a1 = new PdfPCell(new Phrase(agg_tx,redFont));
					a1.setHorizontalAlignment(Element.ALIGN_CENTER);
					agg_dev_tx_rx_Table.addCell(a1);
					
					a1 = new PdfPCell(new Phrase(agg_rx,redFont));
					a1.setHorizontalAlignment(Element.ALIGN_CENTER);
					agg_dev_tx_rx_Table.addCell(a1);
					
				}
				/*
				 *  Device wises agg Tx and Rx
				 * 
				 */
				
				 
				if (details.containsKey("dev_txrx")) {

					JSONArray JStr = (JSONArray)details.get("dev_txrx");
					Iterator<JSONObject> obj = JStr.iterator();
					
					final String series1 = "TX";
					final String series2 = "RX";
			        
					while (obj.hasNext()) {
						
						JSONObject dataObject = obj.next();
						
						String agg_tx = (String)dataObject.get("tx");
						String agg_rx = (String)dataObject.get("rx");
						
						final String uid = (String)dataObject.get("uid");
						final String alias = (String)dataObject.get("alias");
						
						final String category1 = alias;
						
						agg_tx  = CustomerUtils.formatFileSize(Long.valueOf(agg_tx));
						agg_rx  = CustomerUtils.formatFileSize(Long.valueOf(agg_rx));
						
						 dev_tx_rx_Dataset.addValue(Double.valueOf(agg_tx.split("\\s+")[0]), series1, category1);
					     dev_tx_rx_Dataset.addValue(Double.valueOf(agg_rx.split("\\s+")[0]), series2, category1);
						
						b1 = new PdfPCell(new Phrase(uid,redFont));
						b1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_tx_rx_Table.addCell(b1);
						
						b1 = new PdfPCell(new Phrase(alias,redFont));
						b1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_tx_rx_Table.addCell(b1);
						
						b1 = new PdfPCell(new Phrase(agg_tx,redFont));
						b1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_tx_rx_Table.addCell(b1);
						
						b1 = new PdfPCell(new Phrase(agg_rx,redFont));
						b1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_tx_rx_Table.addCell(b1);
						
					}
					
			        final JFreeChart chart = ChartFactory.createBarChart(
			        	"Consumed TX And RX",
			            "Location",
			            "TX And RX (MB)",   
			            dev_tx_rx_Dataset,
			            PlotOrientation.VERTICAL,
			            true, 
			            true, 
			            false 
			        );
			        
			        chart.setBackgroundPaint(new Color(255, 255, 255));
			        CategoryPlot plot = chart.getCategoryPlot();
			       // plot.getRenderer().setSeriesPaint(0, new Color(128, 0, 0));
			       // plot.getRenderer().setSeriesPaint(1, new Color(0, 0, 255));
			        
			        CategoryItemRenderer renderer = ((CategoryPlot)chart.getPlot()).getRenderer();
			        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			        renderer.setBaseItemLabelsVisible(true);
			        ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, 
			                TextAnchor.TOP_CENTER);
			        renderer.setBasePositiveItemLabelPosition(position);
			        renderer.setItemLabelPaint(Color.BLACK);
			        
			        plot.setDataset(1, dev_tx_rx_Dataset);
			        plot.mapDatasetToRangeAxis(1, 1);		
			        
					BufferedImage bufferedImage = chart.createBufferedImage(500, 430);
					devtxrxbarChartImage = Image.getInstance(writer, bufferedImage, 1.0f);
				}
				if (details.containsKey("avg_clients")) {

					JSONObject JStr = (JSONObject)details.get("avg_clients");
					
					final String avg_2g = (String)JStr.get("avg_2g");
					final String avg_5g = (String)JStr.get("avg_5g");
					
					c1 = new PdfPCell(new Phrase(avg_2g,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					avg_2G_5G_Table.addCell(c1);
					
					c1 = new PdfPCell(new Phrase(avg_5g,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					avg_2G_5G_Table.addCell(c1);
				}
			
				/*
				 * Station wises avg 2G and 5G
				 * 
				 */
				
				if (details.containsKey("dev_txrx")) {
					
					JSONArray JStr = (JSONArray) details.get("dev_txrx");
					Iterator<JSONObject> obj = JStr.iterator();
	
					while (obj.hasNext()) {
	
						JSONObject dataObject = obj.next();
						
						final String avg_2g = (String)dataObject.get("avg_2g");
						final String avg_5g = (String)dataObject.get("avg_5g");
						final String uid 	= (String)dataObject.get("uid");
						final String alias = (String)dataObject.get("alias");
						
						f1 = new PdfPCell(new Phrase(uid,redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						device_avg_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(alias,redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						device_avg_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(avg_2g,redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						device_avg_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(avg_5g,redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						device_avg_2G_5G_Table.addCell(f1);

					}
				}
				
				if (details.containsKey("min_max_clients")) {

					JSONObject JStr = (JSONObject)details.get("min_max_clients");
					
					final int min_2g = (int)JStr.get("min_2g");
					final int max_2g = (int)JStr.get("max_2g");
					
					final int min_5g = (int)JStr.get("min_5g");
					final int max_5g = (int)JStr.get("max_5g");
					
					
					e1 = new PdfPCell(new Phrase(String.valueOf(min_2g),redFont));
					e1.setHorizontalAlignment(Element.ALIGN_CENTER);
					min_max_2G_5G_Table.addCell(e1);
					
					e1 = new PdfPCell(new Phrase(String.valueOf(max_2g),redFont));
					e1.setHorizontalAlignment(Element.ALIGN_CENTER);
					min_max_2G_5G_Table.addCell(e1);
					
					
					e1 = new PdfPCell(new Phrase(String.valueOf(min_5g),redFont));
					e1.setHorizontalAlignment(Element.ALIGN_CENTER);
					min_max_2G_5G_Table.addCell(e1);
					
					e1 = new PdfPCell(new Phrase(String.valueOf(max_5g),redFont));
					e1.setHorizontalAlignment(Element.ALIGN_CENTER);
					min_max_2G_5G_Table.addCell(e1);
					
				}
				
				if (details.containsKey("dev_txrx")) {
					
					JSONArray JStr = (JSONArray)details.get("dev_txrx");
					Iterator<JSONObject> obj = JStr.iterator();
					
					DecimalFormat decimalFormat = new DecimalFormat("#.##");
					
					while (obj.hasNext()) {
						
						JSONObject dataObject = obj.next();
						
						final double min2g = (double)dataObject.get("min_2g");
						final double max2g = (double)dataObject.get("max_2g");
						final double min5g = (double)dataObject.get("min_5g");
						final double max_5g = (double)dataObject.get("max_5g");
						
						
						final String uid = (String)dataObject.get("uid");
						final String alias = (String)dataObject.get("alias");
						
						f1 = new PdfPCell(new Phrase(uid,redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_min_max_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(alias,redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_min_max_2G_5G_Table.addCell(f1);
						
						
						f1 = new PdfPCell(new Phrase(decimalFormat.format(min2g),redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_min_max_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(decimalFormat.format(max2g),redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_min_max_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(decimalFormat.format(min5g),redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_min_max_2G_5G_Table.addCell(f1);
						
						f1 = new PdfPCell(new Phrase(decimalFormat.format(max_5g),redFont));
						f1.setHorizontalAlignment(Element.ALIGN_CENTER);
						dev_min_max_2G_5G_Table.addCell(f1);
					}
				}
				if (details.containsKey("dev_txrx")) {

					JSONArray JStr = (JSONArray)details.get("dev_txrx");
					Iterator<JSONObject> obj = JStr.iterator();
					
					while (obj.hasNext()) {
						
						JSONObject dataObject = obj.next();
						
						String tx = (String)dataObject.get("tx");
						String rx = (String)dataObject.get("rx");
						
						final String _2g = (String)dataObject.get("avg_2g");
						final String _5g = (String)dataObject.get("avg_5g");
						final String uid = (String)dataObject.get("uid");
						final String alias = (String)dataObject.get("alias");
						
						tx  = CustomerUtils.formatFileSize(Long.valueOf(tx));
						rx  = CustomerUtils.formatFileSize(Long.valueOf(rx));
						
						g1 = new PdfPCell(new Phrase(uid,redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_count_bandwidth_Table.addCell(g1);
						
						g1 = new PdfPCell(new Phrase(alias,redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_count_bandwidth_Table.addCell(g1);
						
						b1 = new PdfPCell(new Phrase(tx,redFont));
						b1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_count_bandwidth_Table.addCell(b1);
						
						g1 = new PdfPCell(new Phrase(rx,redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_count_bandwidth_Table.addCell(g1);
						
						g1 = new PdfPCell(new Phrase(_2g.toUpperCase(),redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_count_bandwidth_Table.addCell(g1);
						
						g1 = new PdfPCell(new Phrase(_5g,redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_count_bandwidth_Table.addCell(g1);

					}

				}
				if (details.containsKey("dev_txrx")) {

					JSONArray JStr = (JSONArray)details.get("dev_txrx");
					Iterator<JSONObject> obj = JStr.iterator();
					
					while (obj.hasNext()) {
						
						JSONObject dataObject = obj.next();
						
						final int _11k = (int)dataObject.get("_11k");
						final int _11r = (int)dataObject.get("_11r");
						final int _11v = (int)dataObject.get("_11v");
						
						final String uid = (String)dataObject.get("uid");
						final String alias = (String)dataObject.get("alias");
						
						client_capability_Dataset.addValue(_11k, "11K", alias);
						client_capability_Dataset.addValue(_11r, "11R", alias);
						client_capability_Dataset.addValue(_11v, "11V", alias);
					     
						
						g1 = new PdfPCell(new Phrase(uid,redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_Capability_Table.addCell(g1);
						
						g1 = new PdfPCell(new Phrase(alias,redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_Capability_Table.addCell(g1);
						
						b1 = new PdfPCell(new Phrase(String.valueOf(_11k),redFont));
						b1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_Capability_Table.addCell(b1);
						
						g1 = new PdfPCell(new Phrase(String.valueOf(_11r),redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_Capability_Table.addCell(g1);
						
						g1 = new PdfPCell(new Phrase(String.valueOf(_11v),redFont));
						g1.setHorizontalAlignment(Element.ALIGN_CENTER);
						client_Capability_Table.addCell(g1);

					}
					
					 final JFreeChart chart = ChartFactory.createBarChart(
					            "Client Capability(11K,11R,11V)",
					            "Location",
					            "11K,11R,11V",
					            client_capability_Dataset,
					            PlotOrientation.VERTICAL,
					            true, 
					            true, 
					            false
					        );
					        
					        chart.setBackgroundPaint(Color.white);

					        final CategoryPlot plot = chart.getCategoryPlot();
					        plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
					        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

					        CategoryItemRenderer renderer = ((CategoryPlot)chart.getPlot()).getRenderer();
					        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
					        renderer.setBaseItemLabelsVisible(true);
					        ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, 
					                TextAnchor.TOP_CENTER);
					        renderer.setBasePositiveItemLabelPosition(position);
					        renderer.setItemLabelPaint(Color.BLACK);
					      				        
					        plot.setDataset(1, client_capability_Dataset);
					        plot.mapDatasetToRangeAxis(1, 1);			       

							BufferedImage bufferedImage = chart.createBufferedImage(520, 400);
							client_capability_barChartImage = Image.getInstance(writer, bufferedImage, 1.0f);

				}
				
				if (details.containsKey("top5clients_consumed_tx_rx")) {

					JSONArray data = (JSONArray) details.get("top5clients_consumed_tx_rx");

					final String sortbyTX = "peer_tx";
					
					List<net.sf.json.JSONObject> jsonListdata = sortByValue(data, sortbyTX);

					Iterator<net.sf.json.JSONObject> it = jsonListdata.iterator();

					while (it.hasNext()) {
						net.sf.json.JSONObject obj = it.next();
						
						String macAddress = (String) obj.get("peer_mac");
						long peer_tx = Long.parseLong(obj.get("peer_tx").toString());

						String strTx  = CustomerUtils.formatFileSize(Long.valueOf(peer_tx));
						
						i1 = new PdfPCell(new Phrase(macAddress, redFont));
						i1.setHorizontalAlignment(Element.ALIGN_CENTER);
						top_5_clients_consumed_TX_Table.addCell(i1);

						i1 = new PdfPCell(new Phrase(strTx, redFont));
						i1.setHorizontalAlignment(Element.ALIGN_CENTER);
						top_5_clients_consumed_TX_Table.addCell(i1);

					}
					
					final String sortbyRX = "peer_rx";

					JSONArray rxData = (JSONArray) details.get("top5clients_consumed_tx_rx");

					List<net.sf.json.JSONObject> jsonrxData = sortByValue(rxData, sortbyRX);

					Iterator<net.sf.json.JSONObject> rxDataIt = jsonrxData.iterator();
					
					while (rxDataIt.hasNext()) {
						net.sf.json.JSONObject obj = rxDataIt.next();

						String macAddress = (String) obj.get("peer_mac");
						long peer_rx 	  = Long.parseLong(obj.get("peer_rx").toString());

						String strRx  = CustomerUtils.formatFileSize(Long.valueOf(peer_rx));
						
						j1 = new PdfPCell(new Phrase(macAddress, redFont));
						j1.setHorizontalAlignment(Element.ALIGN_CENTER);
						top_5_clients_RX_Table.addCell(j1);

						j1 = new PdfPCell(new Phrase(strRx, redFont));
						j1.setHorizontalAlignment(Element.ALIGN_CENTER);
						top_5_clients_RX_Table.addCell(j1);

					}
				}
				
				Paragraph Para =  null;
				
				if (agg_dev_tx_rx_Table.size() > 1) {
					Para = new Paragraph(title + " Total Consumed TX And RX ", subFont);
					addEmptyLine(Para, 1);
					Para.add(agg_dev_tx_rx_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}

				if (dev_tx_rx_Table.size() > 1) {
					Para = new Paragraph("Location Consumed TX And RX", subFont);
					addEmptyLine(Para, 1);
					Para.add(dev_tx_rx_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
					
					Paragraph para1=  new Paragraph();
					Para = new Paragraph(" Location Total TX And RX", subFont);
					para1.add(devtxrxbarChartImage);
					para1.setAlignment(Element.ALIGN_LEFT);
					addEmptyLine(para1, 1);
					Para.add(para1);
					addEmptyLine(para1, 2);
					document.add(para1);
				}
						
				if (avg_2G_5G_Table.size() > 1) {
					Para = new Paragraph(title + " Avg 2G/5G Clients Connected", subFont);
					addEmptyLine(Para, 1);
					Para.add(avg_2G_5G_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
				
				if (device_avg_2G_5G_Table.size() > 1) {
					Para = new Paragraph(" Location Avg 2G/5G Clients", subFont);
					addEmptyLine(Para, 1);
					Para.add(device_avg_2G_5G_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
				
				if (min_max_2G_5G_Table.size() > 1) {
					Para = new Paragraph(title + " Min And  Max 2G/5G Clients", subFont);
					addEmptyLine(Para, 1);
					Para.add(min_max_2G_5G_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
				
				if (dev_min_max_2G_5G_Table.size() > 1) {
					Para = new Paragraph(" Location Min And Max 2G/5G Clients", subFont);
					addEmptyLine(Para, 1);
					Para.add(dev_min_max_2G_5G_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
				if (client_count_bandwidth_Table.size() > 1) {
					Para = new Paragraph(" Location Clients Count And Bandwidth", subFont);
					addEmptyLine(Para, 1);
					Para.add(client_count_bandwidth_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
				if (client_Capability_Table.size() > 1) {
					Para = new Paragraph(" Location Clients Capability ", subFont);
					addEmptyLine(Para, 1);
					Para.add(client_Capability_Table);
					addEmptyLine(Para, 1);
					document.add(Para);

					Paragraph para1 = new Paragraph();
					Para = new Paragraph(title + " Total 11K,11R And 11V", subFont);
					para1.add(client_capability_barChartImage);
					para1.setAlignment(Element.ALIGN_LEFT);
					addEmptyLine(para1, 1);
					Para.add(para1);
					addEmptyLine(para1, 2);
					document.add(para1);

				}
				if (top_5_clients_consumed_TX_Table.size() > 1) {
					Para = new Paragraph(title + " Top 5 Clients Consumed TX ", subFont);
					addEmptyLine(Para, 1);
					Para.add(top_5_clients_consumed_TX_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
				if (top_5_clients_RX_Table.size() > 1) {
					Para = new Paragraph(title + " Top 5 Clients Consumed RX ", subFont);
					addEmptyLine(Para, 1);
					Para.add(top_5_clients_RX_Table);
					addEmptyLine(Para, 1);
					document.add(Para);
				}
			}

		}
	}
	
	/*
	 * used for processing Device inactivity report list
	 * @param cid customer Id
	 * @param timestamp Searching ES Query
	 * 
	 */
	public JSONArray dev_inact_histy(String cid,String timestamp) {
		
			TimeZone timezone 	= null;
			JSONObject object   = null;
			JSONArray array     = new JSONArray();

			DateFormat format  = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		 	DateFormat parse   = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		 	
		 	List<Device> devices = deviceservice.findByCid(cid);
			Customer customer = customerservice.findById(cid);
			
			if (customer != null) 
				timezone = customerUtils.FetchTimeZone(customer.getTimezone());
			 else 
				 timezone = TimeZone.getTimeZone("Asia/Kolkata");

			format.setTimeZone(timezone);
			parse.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			LOG.info("@@@@@@@@@@@@@@@@@ Device Inactivity Histogram Report @@@@@@@@@@@@@@@@@@@@@@");
			
		if (devices != null) {
			
			for (Device device : devices) {
		
				String uid   	= device.getUid();
				String state 	= device.getState();
				String lastSeen = device.getLastseen() == null ? "NA" : device.getLastseen();
				String location = device.getName();
				
				LOG.info("UId " +uid);
				
				/*
				 * Current Device State
				 */
				
				if (state != null && state.equals("inactive")) {
					object = new JSONObject();
					object.put("uid",         uid);
					object.put("location",    location);
					object.put("lastseen",    lastSeen);
					object.put("onlinetime",  "-");
					object.put("elp",         0);
					object.put("reason",     "Unknown");
					array.add(object);
				}
				
				NavigableMap<String, Object> keep_alive_map = new TreeMap<>();
				
				String esql =" opcode:keep_alive AND timestamp:>now-"+timestamp+" AND uid:\""+uid+ "\"";
				
				QueryBuilder builder = QueryBuilders.queryStringQuery(esql);
				
				SearchResponse scrollResp = elasticsearchConfiguration.getInstance().prepareSearch(indexname)
						.addSort("timestamp", SortOrder.DESC)
		                // set the timeout value - important for long running scrolls
		                  .setScroll(new TimeValue(60000))
		                  .setQuery(builder)
		                  // example of filter to query documents between a time range
		                  //.setPostFilter(QueryBuilders.rangeQuery("timestamp").from("2018-09-01 00:00:00.000").to("2018-10-23 00:00:00.000"))
		                  .setSize(10000).execute().actionGet(); //10,000 hits per shard will be returned for each scroll
		     
				while (true) {
		          	
					for (SearchHit hit : scrollResp.getHits().getHits()) {
				    	try {
				    		
				    		String message = hit.getSourceAsString();
							
							JSONParser parser = new JSONParser();
							
							JSONObject newJObject = (JSONObject) parser.parse(message);
							
							String devId 		= (String)newJObject.get("uid");
							String esTime  		= (String)newJObject.get("timestamp");
							String Str_fs_Time  = esTime.substring(0, esTime.length() - 7); // avoid duplicate millisecond and  second

							keep_alive_map.put(Str_fs_Time, devId);
							
						} catch (ParseException e) {
							e.printStackTrace();
						}
				    	
					}
		              //update the scroll ID
		              scrollResp = elasticsearchConfiguration.getInstance().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
		            
		              //Break condition: No hits are returned so the scroll is finished
		              if (scrollResp.getHits().getHits().length == 0) {
		                  break;
		              }
		          }
				
				for (Map.Entry<String, Object> e : keep_alive_map.entrySet()) {
					
				    Map.Entry<String, Object> prev_keep_alive_time = keep_alive_map.lowerEntry(e.getKey());
				    Map.Entry<String, Object> next_keep_alive_time = keep_alive_map.floorEntry(e.getKey());
				    
				    String previewsTime = null;
				    
					if (prev_keep_alive_time != null) 
						previewsTime = prev_keep_alive_time.getKey();
					
					String currentTime = next_keep_alive_time.getKey();
				     
					if (previewsTime != null) {
						try {
							
							Date cur_date  = parse.parse(currentTime);
							Date prev_date = parse.parse(previewsTime);
							
							long curr_time_millisec = cur_date.getTime();
							long prev_time_millisec = prev_date.getTime();
							
							long elp = curr_time_millisec - prev_time_millisec;
							
							if (elp >= 78000) { // 1.3(m) > consider as device inactive
													     
							   TreeMap<Date, TreeMap<String,Object>> prev_dev_and_app_up_time 
													= device_up_time_and_app_up_time(uid,prev_date,this.timezone);
								
								TreeMap<Date, TreeMap<String,Object>> curr_dev_and_app_up_time 
													= device_up_time_and_app_up_time(uid,cur_date,this.timezone);
								
								long prev_dev_up_time = 0;
								long prev_app_up_time = 0;
								
								long curr_dev_up_time = 0;
								long curr_app_up_time = 0;
								
							if (prev_dev_and_app_up_time != null) {
								TreeMap<String, Object> before_value = searchNearestDate(prev_dev_and_app_up_time,prev_date, "prev");
								if (before_value != null && before_value.containsKey("dev_up_time_in_seconds")) {
									prev_dev_up_time = (long) before_value.get("dev_up_time_in_seconds");
									prev_app_up_time = (long) before_value.get("app_up_time_in_seconds");
								}
							}
							if (curr_dev_and_app_up_time != null) {
								TreeMap<String, Object> current_value = searchNearestDate(curr_dev_and_app_up_time, cur_date, "curr");
								if (current_value != null && current_value.containsKey("dev_up_time_in_seconds")) {
									curr_dev_up_time = (long) current_value.get("dev_up_time_in_seconds");
									curr_app_up_time = (long) current_value.get("app_up_time_in_seconds");
								}
							}
							
								long dev_up_time_in_seconds = curr_dev_up_time - prev_dev_up_time;
								long app_up_time_in_seconds = curr_app_up_time - prev_app_up_time;
								
								//LOG.info("dev_up_time_in_seconds " +dev_up_time_in_seconds + " app_up_time_in_seconds " +app_up_time_in_seconds);
								
								String reason = "Unknown";
								
								if (dev_up_time_in_seconds <= 0 && app_up_time_in_seconds <= 0) {
									reason = "Device reboot";
								} else if (dev_up_time_in_seconds > 0 && app_up_time_in_seconds > 0) {
									reason = "Internet issue";
								} else {
									reason = "Unknown";
								}
							
								LOG.info("#####reason ######: " + reason);
								
								Date prev_online_date_time 	= parse.parse(previewsTime);
								String prev_online_time_str = format.format(prev_online_date_time);
								
								Date curr_online_date_time = parse.parse(currentTime);
								String curr_online_time_str = format.format(curr_online_date_time);
								
								object = new JSONObject();
								object.put("uid",       uid);
								object.put("location",  location);
								object.put("lastseen",  prev_online_time_str);
								object.put("onlinetime",curr_online_time_str);
								object.put("elp",       elp);
								object.put("reason",    reason);
								
								array.add(object);
						}
					} catch (Exception ex) {
						LOG.info("While Processing deviceinfo details error " + e);
						ex.printStackTrace();
						}
					}
				}
			}
		}
		return array;
	}
	
	/*
	 *  used for getting prev and current device up time
	 *  @param uid device uid
	 *  @param Date given fsDate
	 *  @param TimeZone Corresponding Customer timeZone
	 * 
	 */
	public TreeMap<Date, TreeMap<String,Object>> device_up_time_and_app_up_time(String uid,Date time,TimeZone timezone) {
 		
 		
		DateFormat format  = new SimpleDateFormat("yyyy/MM/dd");
	 	DateFormat parse   = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		format.setTimeZone(timezone);
		parse.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		String fs_time  = format.format(time);
		
 		String dev_info_fsql = "index="+indexname+",sort=timestamp desc,size=500,query=cpu_stats:\"Qubercloud Manager\""
				+ " AND timestamp=" +fs_time+" AND uid:\""+uid+"\"|value(uid,uid,NA);"
    			+" value(cpu_days,cpuDays,NA);value(cpu_hours,cpuHours,NA);value(cpu_minutes,cpuMinutes,NA);" 
				+" value(app_days,appDays,NA);value(app_hours,appHours,NA);value(app_minutes,appMinutes,NA);"
				+ " value(timestamp,timestamp,NA);|table";
		
		List<Map<String,Object>> device_info_fsql = fsqlRestController.query(dev_info_fsql);
		
		TreeMap<Date, TreeMap<String,Object>> info = new TreeMap<Date, TreeMap<String,Object>>();
		
		if (device_info_fsql != null) {
			
			device_info_fsql.forEach(map -> {
				
				try {
					
					String timestamp  = (String) map.get("timestamp");
					String esTime     = timestamp.substring(0, timestamp.length() - 7);
					
					Date parse_date 	= parse.parse(esTime);
					String format_date  = format.format(parse_date);
					
					if (fs_time.equals(format_date)) {

						int dev_dd = (int) map.getOrDefault("cpuDays", 0);
						int dev_hh = (int) map.getOrDefault("cpuHours", 0);
						int dev_mm = (int) map.getOrDefault("cpuMinutes", 0);
						
						int app_dd  = (int) map.getOrDefault("appDays", 0);
						int app_hh  = (int) map.getOrDefault("appHours", 0);
						int app_mm  = (int) map.getOrDefault("appMinutes", 0);
						
						long dev_up_time_in_mins = dev_dd * 24 * 60 + dev_hh * 60 + dev_mm;
						long app_up_time_in_mins = app_dd * 24 * 60 + app_hh * 60 + app_mm;
						
						long dev_up_time_in_seconds = dev_up_time_in_mins * 60;
						long app_up_time_in_seconds = app_up_time_in_mins * 60;
						
						TreeMap<String,Object> device_info_time = new TreeMap<String,Object>();
						device_info_time.put("dev_up_time_in_seconds", dev_up_time_in_seconds);
						device_info_time.put("app_up_time_in_seconds", app_up_time_in_seconds);
						
						info.put(parse_date, device_info_time);
					}
				} catch (Exception e) {
					LOG.info("While Processing device_up_time_and_app_up_time error " +e);
					e.printStackTrace();
				}
			});
			return info;
		}
		return null;
	}

	/* 
	 *  used for searching nearest date
	 *  @param dates list of date
	 *  @param tagrgetDate find corresponding  nearest date
	 *  @param flag searching prev or current nearest date
	 *  
	 */
	
	public TreeMap<String,Object> searchNearestDate(TreeMap<Date, TreeMap<String,Object>> dates, Date tagrgetDate,String flag) {
		
		long minDiff     = -1;
		long currentTime = tagrgetDate.getTime();
		Date minDate     = null;
		
		if (dates != null && !dates.isEmpty()) {

			Set<Date> key = dates.keySet();

			for (Date date : key) {
				
				long prevDiff = Math.abs(currentTime - date.getTime());
				
				if (flag.equals("prev")) {
					if ((minDiff == -1) || (prevDiff < minDiff)) {
						minDiff = prevDiff;
						minDate = date;
					}
				} else {
					long currDiff = currentTime - date.getTime();
					if (currDiff < 0) {
						minDate = date;
						break;
					}
				}
			}
			
			TreeMap<String, Object> nearestDate = new TreeMap<String, Object>();
			if (minDate != null) {
				nearestDate = dates.get(minDate);
			}
			//LOG.info("@@@@@ " + flag + " @@@@@@@@@@@ " + minDate + " uptime " +uptime);
			
			return nearestDate;
		}
		
		return null;
	}
	
	private List<net.sf.json.JSONObject> sortByValue(JSONArray data,final String key) {
		
		net.sf.json.JSONArray formatData =net.sf.json.JSONArray.fromObject(data);
		
		List<net.sf.json.JSONObject> jsonValues = new ArrayList<net.sf.json.JSONObject>();
		for (int i = 0; i < formatData.size(); i++) {
			jsonValues.add(formatData.getJSONObject(i));
		}
		
	    Collections.sort(jsonValues, new Comparator<net.sf.json.JSONObject>() {
	        @Override
	        public int compare(net.sf.json.JSONObject a, net.sf.json.JSONObject b) {
	            String valA = String.valueOf(a.get(key));
	            String valB = String.valueOf(b.get(key));
	            return Integer.valueOf(valB).compareTo(Integer.valueOf(valA));
	        }
	    });
		
		int to = jsonValues.size() > 5 ? 5 : jsonValues.size();
	    
	    List<net.sf.json.JSONObject> sortedList = jsonValues.subList(0, to);
	   
		
		return sortedList;

	}
	

	private Iterable<Device> getDeviceByCidAndStatus(String cid, String devStatus) {
		
		Iterable<Device> device = null;
		
		if (devStatus.equals("all")) {
			device = deviceservice.findByCid(cid);
		} else {
			device = deviceservice.findByCidAndState(cid, devStatus);
		}
		
		return device;
	}

	public String csv(
			@RequestParam(value = "cid", 		required = false) String cid,
			@RequestParam(value = "venuename",  required = false) String sid,
			@RequestParam(value = "floorname",  required = false) String spid,
			@RequestParam(value = "location", 	required = false) String location,
			@RequestParam(value = "macaddr", 	required = false) String macaddr,
			@RequestParam(value = "filtertype", required = true)  String filtertype,
			@RequestParam(value = "time", 		required = false) String days,
			@RequestParam(value = "devStatus", required = false) String devStatus,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//LOG.info("Csvfunc filtertype" + filtertype+" cid "+cid+" sid "+sid+" spid "+spid + " macaddr "+macaddr+ "location"+ location + " time "+days);
		
		String csvFileName  = "Report.csv";
		
		
		OutputStream out = null;
		
		try {
			if (SessionUtil.isAuthorized(request.getSession())) {
				
				String result = "";
				
				if (sid != null && (sid.equals("all") || sid.equals("undefined"))) {
					sid = "";
				}
				if (spid != null && (spid.equals("all") || spid.equals("undefined"))) {
					spid = "";
				}
				
				JSONObject details  = null;
				
				if (filtertype.equals("deviceInfo")) {

					result = "UID,FLOOR,LOCATION,DEVICE UPTIME,APP UPTIME,STATE,LAST SEEN\n";
					JSONArray processedDetail = deviceInfo(cid, sid, spid, request, response);
					Iterator<JSONObject> iterProcessedDetail = processedDetail.iterator();
					JSONObject json = null;
					while(iterProcessedDetail.hasNext()){
						json 				= iterProcessedDetail.next();
						String uid 			= json.get("uid").toString();
						String floorname 	= json.get("floorname").toString();
						String locationname = json.get("locationname").toString();
						String deviceUptime = json.get("deviceUptime").toString();
						String appUptime 	= json.get("appUptime").toString();
						String state 		= json.get("state").toString();
						String lastseen 	= json.get("lastseen").toString();
						result += uid+","+floorname+","+locationname+","+deviceUptime+","+appUptime+","+state+","+lastseen+"\n";
					}
				} else if (filtertype.equals("dev_inact_histy")) {
					
					result = "UID,Location,Last Seen,Online,Down Time,Reason\n";
					
					JSONArray dev_inact_histy = dev_inact_histy(cid, days);
					
					if (dev_inact_histy != null) {
						
						Iterator it = dev_inact_histy.iterator();
						
						while (it.hasNext()) {
							
							JSONObject map = (JSONObject)it.next();
							
							String uid 			= (String) map.get("uid");
							String alias 		= (String) map.getOrDefault("location", "AP");
							String lastseen 	=  map.get("lastseen").toString();
							String onlinetime 	=  map.get("onlinetime").toString();
							String reason 		= (String) map.getOrDefault("reason", "Unkown");
							
							long elp = 0;

							if (map.containsKey("elp")) {
								elp = Long.parseLong(map.get("elp").toString());
							}

							String secondsto_hours_minus_days = CustomerUtils.secondsto_hours_minus_days(elp);
							
							result += uid+","+alias+","+lastseen+","+onlinetime+","+secondsto_hours_minus_days+","+reason+"\n";
							
						}
					}
				} else {
					
					if (days == null)
						days = "12h";
					
					boolean venuewisse 		= false;
					boolean floorwisse 		= false;
					boolean locationwisse 	= false;
					boolean customerwisse 	= false;

					List<Device> devices = null;
					
					switch (filtertype)
					{
					case "default":
						devices = deviceservice.findByCid(cid);
						details = reportDetails("cid",days,devices);
						customerwisse  = true;
						break;
						
					case "venue":
						
						if (sid.isEmpty() || sid.equals("all")) {
							devices = deviceservice.findByCid(cid);
							details = reportDetails("sid",days,devices);
							customerwisse  = true;
						} else {
							devices = deviceservice.findBySid(sid);
							details = reportDetails("sid",days,devices);
							venuewisse = true;
						}
							
						break;
						
					case "floor":
						
						if (sid.isEmpty() || sid.equals("all")) {
							devices = deviceservice.findByCid(cid);
							details = reportDetails("cid",days,devices);
							customerwisse  = true;
						}
							
						else if(spid.isEmpty() || spid.equals("all")) {
							devices = deviceservice.findBySid(sid);
							details = reportDetails("sid",days,devices);
							venuewisse = true;
						} else {
							devices = deviceservice.findBySpid(spid);
							details = reportDetails("spid",days,devices);
							floorwisse = true;
						}
						break;
						
					case "location":
						
						if (sid.isEmpty() || sid.equals("all")) {
							devices = deviceservice.findByCid(cid);
							details = reportDetails("cid",days,devices);
							customerwisse  = true;
						} else if(spid.isEmpty() || spid.equals("all")) {
							devices = deviceservice.findBySid(sid);
							details = reportDetails("sid",days,devices);
							venuewisse = true;
						} else if (location.isEmpty() || location.equals("all")){
							devices = deviceservice.findBySpid(spid);
							details = reportDetails("spid",days,devices);
							floorwisse = true;
						}else {
							devices = deviceservice.findByUid(location);
							details = reportDetails("uid",days,devices);
							locationwisse = true;
						}
						break;
						
					case "devStatus":
						devices = (List<Device>)getDeviceByCidAndStatus(cid, devStatus);
						customerwisse  = true;
					}
					
					
					String title = "Location";

					if (customerwisse) {
						title = "Customer";
					} else if (venuewisse) {
						title = "Venue";
					} else if (floorwisse) {
						title = "Floor";
					}
					
					if (details != null) {

						if (details.containsKey("agg_txrx")) {

							String header1 = "TX,RX,";
							String data1   = "";
							String Device  = "";
							
							JSONObject JStr = (JSONObject)details.get("agg_txrx");
							
							String agg_tx = (String)JStr.get("agg_tx");
							String agg_rx = (String)JStr.get("agg_rx");
							
							
							agg_tx  = CustomerUtils.formatFileSize(Long.valueOf(agg_tx));
							agg_rx  = CustomerUtils.formatFileSize(Long.valueOf(agg_rx));
							
							Device = agg_tx + "," + agg_rx + "," + "\n";
							data1  = data1.concat(Device);
							
							result = result.concat(title + " Total Consumed TX And RX");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);
							
							result = result.concat("\n\n");
							
						}
						/*
						 *  Device wises agg tx rx
						 * 
						 */
						
						if (details.containsKey("dev_txrx")) {

							JSONArray JStr = (JSONArray)details.get("dev_txrx");
							Iterator<JSONObject> obj = JStr.iterator();
							
							String header1 = "Uid,Alias,TX,RX,";
							String data1   = "";
							String Device  = "";
					        
							
							while (obj.hasNext()) {
								
								JSONObject dataObject = obj.next();
								
								String agg_tx = (String)dataObject.get("tx");
								String agg_rx = (String)dataObject.get("rx");
								
								final String uid = (String)dataObject.get("uid");
								final String alias = (String)dataObject.get("alias");
								
								agg_tx  = CustomerUtils.formatFileSize(Long.valueOf(agg_tx));
								agg_rx  = CustomerUtils.formatFileSize(Long.valueOf(agg_rx));
								
								Device = uid + "," +alias + "," + agg_tx + "," + agg_rx + "," + "\n";
								data1 = data1.concat(Device);
								
								
							}
							
							result = result.concat("Location Consumed TX And RX");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);
							
							result = result.concat("\n\n");
							
						}
						if (details.containsKey("avg_clients")) {

							String header1 = "Avg 2G,Avg 5G,";
							String data1   = "";
							String Device  = "";
							
							JSONObject JStr = (JSONObject)details.get("avg_clients");
							
							final String avg_2g = (String)JStr.get("avg_2g");
							final String avg_5g = (String)JStr.get("avg_5g");
							
							Device = avg_2g + "," +avg_5g + "," + "\n";
							data1  = data1.concat(Device);
							
							result = result.concat(title + " Avg 2G/5G Clients Connected");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);
							
							result = result.concat("\n\n");
							
						}					
						/*
						 * Station wises avg 2g and 5g
						 * 
						 */
						
						if (details.containsKey("dev_txrx")) {
							
							JSONArray JStr = (JSONArray) details.get("dev_txrx");
							Iterator<JSONObject> obj = JStr.iterator();
			
							String header1 = "Uid,Alias,Avg 2G,Avg 5G,";
							String data1   = "";
							String Device  = "";
							
							while (obj.hasNext()) {
			
								JSONObject dataObject = obj.next();
								
								final String avg_2g = (String)dataObject.get("avg_2g");
								final String avg_5g = (String)dataObject.get("avg_5g");
								final String uid 	= (String)dataObject.get("uid");
								final String alias = (String)dataObject.get("alias");
								
								Device = uid + "," + alias + "," + avg_2g + "," + avg_5g + "," + "\n";
								data1 = data1.concat(Device);
								
								
							}
							
							result = result.concat("Location Avg 2G/5G Clients");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);
							
							result = result.concat("\n\n");
						}
						
						if (details.containsKey("min_max_clients")) {

							JSONObject JStr = (JSONObject)details.get("min_max_clients");
							
							String data1   = "";
							String header1 = "Min 2G,Max 2G,Min 5G,Max 5G,";
							String Device  = "";
							
							final int min_2g = (int)JStr.get("min_2g");
							final int max_2g = (int)JStr.get("max_2g");
							
							final int min_5g = (int)JStr.get("min_5g");
							final int max_5g = (int)JStr.get("max_5g");
							
							Device = min_2g + "," + max_2g + "," + min_5g + "," + max_5g + "," + "\n";
							data1 = data1.concat(Device);
							
							result = result.concat(title + " Min And Max 2G/5G Clients");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);

							result = result.concat("\n\n");

						}

						if (details.containsKey("dev_txrx")) {
							
							JSONArray JStr = (JSONArray)details.get("dev_txrx");
							Iterator<JSONObject> obj = JStr.iterator();

							DecimalFormat decimalFormat = new DecimalFormat("#.##");
							
							String data1   = "";
							String header1 = "Uid,Alias,Min 2G,Max 2G,Min 5G,Max 5G,";
							String Device  = "";
							
							while (obj.hasNext()) {
								
								JSONObject dataObject = obj.next();
								
								final double min2g = (double)dataObject.get("min_2g");
								final double max2g = (double)dataObject.get("max_2g");
								final double min5g = (double)dataObject.get("min_5g");
								final double max5g = (double)dataObject.get("max_5g");
								
								
								final String uid = (String)dataObject.get("uid");
								final String alias = (String)dataObject.get("alias");
								
								Device = uid + "," + alias + "," + decimalFormat.format(min2g) + "," 
								+ decimalFormat.format(max2g) + "," + decimalFormat.format(min5g) 
								+ "," + decimalFormat.format(max5g)+ ","+ "\n";
								
								data1 = data1.concat(Device);	
								
							}
							
							result = result.concat("Location Min And Max 2G/5G Clients");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);

							result = result.concat("\n\n");
							
						}
						if (details.containsKey("dev_txrx")) {

							JSONArray JStr = (JSONArray)details.get("dev_txrx");
							Iterator<JSONObject> obj = JStr.iterator();
							
							String data1   = "";
							String header1 = "Uid,Alias,TX Bytes, RX Bytes,Avg 2G,Avg 5G,";
							String Device  = "";
							
							while (obj.hasNext()) {
								
								JSONObject dataObject = obj.next();
								
								String tx = (String)dataObject.get("tx");
								String rx = (String)dataObject.get("rx");
								
								final String _2g = (String)dataObject.get("_2g");
								final String _5g = (String)dataObject.get("_5g");
								final String uid = (String)dataObject.get("uid");
								final String alias = (String)dataObject.get("alias");
								
								tx  = CustomerUtils.formatFileSize(Long.valueOf(tx));
								rx  = CustomerUtils.formatFileSize(Long.valueOf(rx));
								
								Device = uid + "," + alias + "," + tx + "," + rx + "," + _2g + "," + _5g + ","
										+ "\n";
								data1 = data1.concat(Device);	
								
							}
							result = result.concat("Location Clients Count And Bandwidth");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);
							
							result = result.concat("\n\n");

						}
						if (details.containsKey("dev_txrx")) {

							JSONArray JStr = (JSONArray)details.get("dev_txrx");
							Iterator<JSONObject> obj = JStr.iterator();
							
							String data1   = "";
							String header1 = "Uid,Alias,11K,11R,11V,";
							String Device  = "";
							
							while (obj.hasNext()) {
								
								JSONObject dataObject = obj.next();
								
								final int _11k = (int)dataObject.get("_11k");
								final int _11r = (int)dataObject.get("_11r");
								final int _11v = (int)dataObject.get("_11v");
								
								final String uid   = (String)dataObject.get("uid");
								final String alias = (String)dataObject.get("alias");
								
								Device = uid + "," + alias + "," + _11k + "," + _11r + "," + _11v + "," + "\n";
								data1 = data1.concat(Device);	
								
							}
							
							result = result.concat("Location Clients Capability");
							result = result.concat("\n");
							result = result.concat(header1);
							result = result.concat("\n");
							result = result.concat(data1);

							result = result.concat("\n\n");
							
						}
						
						if (details.containsKey("top5clients_consumed_tx_rx")) {

							JSONArray data = (JSONArray) details.get("top5clients_consumed_tx_rx");

							final String sortbyTX = "peer_tx";
							
							List<net.sf.json.JSONObject> jsonListdata = sortByValue(data, sortbyTX);

							Iterator<net.sf.json.JSONObject> it = jsonListdata.iterator();

							String Device = null;
							
							String txdata1 = "";
							String rxdata1 = "";
							
							String txheader1 = "Client Mac,TX,";
							String rxheader1 = "Client Mac,RX,";
							
							while (it.hasNext()) {
								net.sf.json.JSONObject obj = it.next();
								
								String macAddress = (String) obj.get("peer_mac");
								int peer_tx = (int) obj.get("peer_tx");

								String strTx  = CustomerUtils.formatFileSize(Long.valueOf(peer_tx));
								
								Device = macAddress + "," + strTx + "," + "\n";
								txdata1 = txdata1.concat(Device);	
								
							}
							
							if (!txdata1.isEmpty()) {
								result = result.concat(title + " Top 5 Clients Consumed TX");
								result = result.concat("\n");
								result = result.concat(txheader1);
								result = result.concat("\n");
								result = result.concat(txdata1);
								result = result.concat("\n\n");
							}
							
							final String sortbyRX = "peer_rx";

							JSONArray rxData = (JSONArray) details.get("top5clients_consumed_tx_rx");

							List<net.sf.json.JSONObject> jsonrxData = sortByValue(rxData, sortbyRX);

							Iterator<net.sf.json.JSONObject> rxDataIt = jsonrxData.iterator();
							
							String DeviceRX = null;
							
							while (rxDataIt.hasNext()) {
								net.sf.json.JSONObject obj = rxDataIt.next();

								String macAddress = (String) obj.get("peer_mac");
								int peer_rx 	  = (int) obj.get("peer_rx");

								String strRx  = CustomerUtils.formatFileSize(Long.valueOf(peer_rx));
								
								DeviceRX = macAddress + "," + strRx + "," + "\n";
								rxdata1 = rxdata1.concat(DeviceRX);	

							}
							if (!rxdata1.isEmpty()) {
								result = result.concat(title + " Top 5 Clients Consumed RX");
								result = result.concat("\n");
								result = result.concat(rxheader1);
								result = result.concat("\n");
								result = result.concat(rxdata1);
								result = result.concat("\n\n");
							}
						}
					}
				}

				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; filename=" + csvFileName);
				out = response.getOutputStream();
				out.write(result.getBytes());
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out !=null) {
				out.flush();
				out.close();
			} 
			
		}
		
		
		return csvFileName;
	}
	
	
	@RequestMapping(value = "/gw_alertpdf", method = RequestMethod.GET)
	public String tagalertpdf(
			@RequestParam(value = "cid", required = false) String cid,
			HttpServletRequest request,HttpServletResponse response) {
		
		//String pdfFileName  = "GatewayAlertReport.pdf";
		//String logoFileName = "/home/qubercomm/Desktop/pdf/logo.png";
		
		String pdfFileName  = "./uploads/alert.pdf";
		String logoFileName = "./uploads/logo-home.png";

		if (SessionUtil.isAuthorized(request.getSession())) {

			Document document = new Document(PageSize.A4, 36, 36, 90, 55);
			try {
				
				if(cid == null){
					cid = SessionUtil.getCurrentCustomer(request.getSession());
				}
				Customer cx = customerservice.findByUId(cid);
				String cx_name = cx.getCustomerName();
				timezone = customerUtils.FetchTimeZone(cx.getTimezone());// cx.getTimezone()
				format.setTimeZone(timezone);
				
				String currentuser = SessionUtil.currentUser(request.getSession());
				UserAccount cur_user = userAccountService.findOneByEmail(currentuser);
				String userName = cur_user.getFname() + " " + cur_user.getLname();
				
				logoFileName = cx.getLogofile() == null ? logoFileName : cx.getLogofile();
				Path path = Paths.get(logoFileName);
				
				if (!Files.exists(path)) {
					logoFileName = "./uploads/logo-home.png";
				}
				
				FileOutputStream os = new FileOutputStream(pdfFileName);
				PdfWriter writer = PdfWriter.getInstance(document, os);
				HeaderFooterPageEvent event = new HeaderFooterPageEvent(cx_name, userName, logoFileName, format.format(new Date()));
				writer.setPageEvent(event);
				document.open();

				addContent(document, cid,cx_name);
				document.close();

				File pdfFile = new File(pdfFileName);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
				response.setContentLength((int) pdfFile.length());
				
				FileInputStream fileInputStream   = new FileInputStream(pdfFile);
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

		}
		return pdfFileName;
	}

	private void addContent(Document document, String cid,String customerName) {
		try {

			Paragraph subCatPart = new Paragraph();

			// add a table
			createTable(subCatPart, document, cid,customerName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void createTable(Paragraph subCatPart, Document document, String cid,String customerName) throws DocumentException {

		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(100);

		PdfPCell c1 = new PdfPCell(new Phrase("UID",headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setColspan(2);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Alias",headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);
		
		c1 = new PdfPCell(new Phrase("Floor Name",headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Status",headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Last Active",headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setColspan(2);
		table.addCell(c1);

		table.setHeaderRows(1);

		try {
			Boolean generatepdf = true;
			JSONObject deviceslist 		= networkDeviceRestController.alert(cid,generatepdf);

			if (deviceslist != null && !deviceslist.isEmpty()) {
				
				JSONArray array 	   		  = (JSONArray)deviceslist.get("inactive_list");
				Iterator<JSONObject> iterator = array.iterator();

				while (iterator.hasNext()) {
					JSONObject rep = iterator.next();

					String macaddr 		= (String) rep.get("macaddr");
					String alias		= (String) rep.get("alias");
					String floor		= (String) rep.get("portionname");
					String status		= (String) rep.get("state");
					String lastactive	= (String) rep.get("timestamp");

					c1 = new PdfPCell(new Phrase(macaddr,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					c1.setColspan(2);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(alias,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(floor,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(status,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(lastactive,redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					c1.setColspan(2);
					table.addCell(c1);
				}
				subCatPart = new Paragraph("Device Alerts ", subFont);
				addEmptyLine(subCatPart, 1);
				subCatPart.add(table);
				document.add(subCatPart);
			}else{
				subCatPart = addNoDataToPDF(subCatPart);
				document.add(subCatPart);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void emailTrigger(String uid) {

		String pdfFileName  = "alert.pdf";
		String logoFileName = "./uploads/logo-home.png";
		
		UserAccount users	= userAccountService.findOneByUid(uid);

		if (users != null && users.isMailalert() != null && users.isMailalert().equals("true")) {
			
			String cid 		  = users.getCustomerId();
			String email      = users.getEmail();

			try {
				
					JSONObject deviceslist	  = networkDeviceRestController.alert(cid,null);
					JSONArray inactiveDevices = (JSONArray)deviceslist.get("inactive_list");
					
					JSONObject inactivetag = (JSONObject) inactiveDevices.get(0);
					String inactivemac 	   = inactivetag.get("macaddr").toString();
				
					if (inactivemac.equals("-")) {
						LOG.info("=====NO DEVICE ALTER FOUND=====");
						return;
					}

					Document document = new Document(PageSize.A4, 36, 36, 90, 55);
					LOG.info("Email Alerts enabled user " +uid);
					
					Customer cx 	= customerservice.findById(cid);
					String cx_name  = cx.getCustomerName();
					String userName = users.getFname() + " " + users.getLname();
					timezone = customerUtils.FetchTimeZone(cx.getTimezone());// cx.getTimezone()
					format.setTimeZone(timezone);
					
					logoFileName = cx.getLogofile() == null ? logoFileName : cx.getLogofile();
					Path path = Paths.get(logoFileName);
					
					if (!Files.exists(path)) {
						logoFileName = "./uploads/logo-home.png";
					}
					
					File file = new File(pdfFileName);
					FileOutputStream os = new FileOutputStream(file);
					PdfWriter writer = PdfWriter.getInstance(document, os);
					HeaderFooterPageEvent event = new HeaderFooterPageEvent(cx_name, userName, logoFileName, format.format(new Date()));
					writer.setPageEvent(event);
					document.open();
					
					addContent(document, cid,cx_name);
					document.close();
					os.close();

					String body = "Dear "+cx_name+",\n\n You have a new Alert Message!!!\n"
								+ "PFA detailed list of inactive devices.\n Please look in to this as a high priority.\n"
								+ "ALERTS - DEVICES @RISK, REQUIRES YOUR IMMEDIATE ATTENTION. \n";
			
					final String subject = "Alert Notification";
					
					customerUtils.customizeSupportEmail(cid, email, subject, body, file);


			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LOG.info("Email Alerts disabled user email " +uid);
		}
	}
	
public void EmailTriggeringForAlerts(File pdfFile,String email) {
		
		StringBuilder mailBody = new StringBuilder();
		
		mailBody
		 .append("Dear Customer,<br/>")
		 .append("You have a new Alert Message!!!<br/>")
		 .append("PFA detailed list of inactive devices.<br/> Please look in to this as a high priority.<br/>")
		 .append("ALERTS - DEVICES/TAGS @RISK, REQUIRE YOUR IMMEDIATE ATTENTION <br/>");
		 
		LOG.info("email id " +		email);
		LOG.info("mail body  " +   mailBody);
					
		javaMailSender.send(new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");			
				message.setTo(email);
				message.setSubject("Qubercomm Notification");
				message.setText(mailBody.toString(), true);
				message.addAttachment("alert.pdf", pdfFile);
			}
		});
	}
	
	@RequestMapping(value = "/gw_alertcsv", method = RequestMethod.GET)
	public String gatewayalertcsv(@RequestParam(value = "cid", required = true) String cid,
			HttpServletRequest request, HttpServletResponse response) throws IOException {

		String csvFileName = "./uploads/alert.csv";
		

		try {
			
			if (SessionUtil.isAuthorized(request.getSession())) {
				
				String result 		 = "";
				String gatewayheader = "";

				gatewayheader = "UID,Floor Name ,Alias,Status,Last Active\n";
					
					JSONObject deviceslist = networkDeviceRestController.alert(cid,null);

					if (deviceslist != null && !deviceslist.isEmpty()) {
						
						result = result.concat("DEVICES ALERT");
						result = result.concat("\n");
						result = result.concat(gatewayheader);

						JSONArray array 		= (JSONArray) deviceslist.get("inactive_list");
						Iterator<JSONObject> i  = array.iterator();

						String inactivedevices = null;

						while (i.hasNext()) {

							JSONObject rep = i.next();

							String macaddr 		= (String) rep.get("macaddr");
							String alias 		= (String) rep.get("alias");
							String floor 		= (String) rep.get("portionname");
							String status 		= (String) rep.get("state");
							String lastactive 	= (String) rep.get("state");

							inactivedevices = macaddr + "," + floor + "," + alias 
											+ "," + status + ","+lastactive+"\n";

							result = result.concat(inactivedevices);
						}
						result = result.concat("\n\n");
					}

			
				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; filename=" + csvFileName);
				OutputStream out = response.getOutputStream();
				out.write(result.getBytes());
				
				out.flush();
				out.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvFileName;
	}
	
	private Paragraph addNoDataToPDF(Paragraph paragraph) {
		addEmptyLine(paragraph, 5);
		PdfPTable table  = new PdfPTable(1);
		table.setWidthPercentage(100);
		PdfPCell c1 = new PdfPCell(new Phrase("No Data....",redFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setBorder(Rectangle.NO_BORDER);
		table.addCell(c1);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		paragraph.add(table);
		return paragraph;
	}
	@RequestMapping(value = "/locationlist", method = RequestMethod.GET)
	public JSONObject locationlist(@RequestParam(value = "cid", required = false) String cid,
								   @RequestParam(value = "sid", required = false) String sid,
								   @RequestParam(value = "spid", required = false) String spid,
								HttpServletRequest request,HttpServletResponse response) {
		if(cid == null){
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		JSONObject json 		= null;
		JSONArray jsonArray 	= new JSONArray();
		JSONObject jsonList 	= new JSONObject();
		Iterable<Device> ndList = new ArrayList<Device>();
		
		if(sid.equalsIgnoreCase("all") || spid.equalsIgnoreCase("all")){
			return  jsonList;
		}
		
		ndList = deviceservice.findBySpid(spid);
		
		if (ndList != null) {
			for (Device d : ndList) {
				json = new JSONObject();
					json.put("id",	 d.getUid());
					json.put("name", d.getName());
					jsonArray.add(json);
			}
			jsonList.put("location", jsonArray);
		}
		return jsonList;

	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/gw_htmlCharts", method = RequestMethod.GET)
	public JSONObject gw_htmlCharts(HttpServletRequest request, HttpServletResponse response) {
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			String filterType 		= request.getParameter("filtertype");
			String cid 		  		= request.getParameter("cid");
			String sid 		  		= request.getParameter("venuename");
			String spid 	  		= request.getParameter("floorname");
			String location   		= request.getParameter("location");
			String time 			= request.getParameter("time");
			
			LOG.info(" filterType " + filterType + " cid " + cid + " sid " + sid + " spid " + spid);
			LOG.info(" location " + location + " time " + time);
			
			
			if (time == null || time.isEmpty()) {
				time = "24h";
			}
			
			String place 				  		= "htmlchart";
			List<Map<String, Object>> cpu 		= null;
			List<Map<String, Object>> mem 		= null;
			List<net.sf.json.JSONObject>  rxtx 	= null;
			List<Device> devices 	= null;
			
			JSONObject detailsJson 		= new JSONObject();
			JSONArray   clientDetails 	= null;
			
			
			switch (filterType) {
			
			case "default": {
				devices   = deviceservice.findByCid(cid);
				clientDetails 	= clientsDetails(devices,time);
			}
				
				break;
			case "venue":
				
				if (sid != null && sid.equals("all")) {
					devices 	 = deviceservice.findByCid(cid);
					clientDetails   = clientsDetails(devices,time);
				} else {
					devices 	= deviceservice.findBySid(sid);
					clientDetails  = clientsDetails(devices,time);
					rxtx 		  	= networkDeviceRestController.avg_tx_rx(sid, null, null, time, cid, request, response);
				}
				break;

			case "floor":

				if (sid.equals("all")) {
					devices = deviceservice.findByCid(cid);
				} else if (spid != null && spid.equals("all")) {
					devices = deviceservice.findBySid(sid);
					rxtx 		  = networkDeviceRestController.avg_tx_rx(sid,null, null, time, cid,request, response);
				} else {
					devices = deviceservice.findBySpid(spid);
					rxtx 		  = networkDeviceRestController.avg_tx_rx(null,spid, null, time, cid,request, response);
				}
				break;

			case "location":
				
				if (sid != null && sid.equals("all")) {
					devices = deviceservice.findByCid(cid);
					clientDetails = clientsDetails(devices,time);
				} else if (spid != null && spid.equals("all")) {
					devices  = deviceservice.findBySid(sid);
					clientDetails = clientsDetails(devices,time);
					rxtx 		   = networkDeviceRestController.avg_tx_rx(sid,null, null, time, cid,request, response);
				} else if (location != null && location.equals("all")) {
					devices  = deviceservice.findBySpid(spid);
					clientDetails = clientsDetails(devices,time);
					rxtx 		   = networkDeviceRestController.avg_tx_rx(null,spid, null, time, cid,request, response);
				} else {
					
					devices = deviceservice.findByUid(location);
					
					if (devices != null && devices.size() >0) {
						
						Device dev = devices.get(0);
						String uid 		  = dev.getUid().toLowerCase();
						
						clientDetails  = clientsDetails(devices,time);	
						cpu			 	= networkDeviceRestController.getcpu(null, null, null, uid, time, place);
						mem 		 	= networkDeviceRestController.getmem(null, null, null, uid, time, place);
						rxtx		 	= networkDeviceRestController.rxtx(uid, time, request, response);
					}
					
					detailsJson.put("cpu", cpu);
					detailsJson.put("mem", mem);

				}

				break;

			default:
				break;
			}
			
			detailsJson.put("clientDetails",	 clientDetails);
			detailsJson.put("rxtx",			 rxtx);
			
			return detailsJson;
		}
		return null;
	}

	
	
	@SuppressWarnings("unchecked")
	private JSONArray clientsDetails(List<Device> networkDevice,String time) {
		
		JSONArray   result   		=  new JSONArray();
		JSONArray   dev_array   	=  null;
		JSONObject locationObject 	=  null;
		
		try {
			
			for (Device device : networkDevice) {
				
				String uid = device.getUid().toLowerCase();


				int vap_2g = device.getVap2gcount();
				int vap_5g = device.getVap5gcount();
						
				dev_array = new JSONArray();
				exec_fsql_getpeer(uid, vap_2g, vap_5g, dev_array, time);

				String location = device.getName() + " (" + uid + ")";

				if (dev_array != null && dev_array.size() > 0) {
					locationObject = new JSONObject();

					locationObject.put("uid", location);
					locationObject.put("details", dev_array);

					result.add(locationObject);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("processing clientsDetails  error " + e);
		}

		return result;
	}

	private boolean exec_fsql_getpeer(String uid, int vap2g, int vap5g, JSONArray  dev_array,String duration) throws IOException {
    	
    	String fsql 	= "";
    	String fsql_5g 	= "";
    	int i			= 0;
  
    	List<Map<String, Object>>  logs  = EMPTY_LIST_MAP;
    	List<Map<String, Object>>  qlogs = EMPTY_LIST_MAP;
    		
    	for (i = 0; i < vap2g; i++ ) {
    		
    		fsql 	= "index=" + indexname + ",sort=timestamp desc,";
    		
    		fsql	= fsql + "query=timestamp:>now-"+duration+"" + " AND ";
    		fsql 	= fsql + "uid:\"" + uid + "\"";
			fsql 	= fsql + " AND vap_id:\"" + i + "\"";
			fsql 	= fsql + " AND radio_type:\"2.4Ghz\"|value(message,snapshot, NA);value(timestamp,timestamp, NA)|table";
			
			//LOG.info("2G FSQL PEER QUERY" + fsql);
			logs = fsqlRestController.query(fsql);
			
			if (logs != EMPTY_LIST_MAP) {
				addPeers (uid, logs, dev_array);
			}
    	}
    	    	
    	for (i= 0; i < vap5g; i++ ) {
    		
    		fsql_5g 	= "index=" + indexname + ",sort=timestamp desc,";
    		
    		fsql_5g	= fsql_5g + "query=timestamp:>now-"+duration+"" + " AND ";
    		fsql_5g = fsql_5g + "uid:\"" + uid + "\"";
    		fsql_5g = fsql_5g + " AND vap_id:\"" + i + "\"";
    		fsql_5g = fsql_5g + " AND radio_type:\"5Ghz\"|value(message,snapshot, NA);value(timestamp,timestamp, NA)|table";
    		
    		//LOG.info("5G FSQL PEER QUERY" + fsql_5g);
			
    		qlogs = fsqlRestController.query(fsql_5g);
					
			if (qlogs != EMPTY_LIST_MAP) {
				addPeers (uid, qlogs, dev_array);
			}
    	}
    	return true;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes", "rawtypes" })
	private boolean addPeers (String uid, List<Map<String, Object>> logs, JSONArray dev_array) throws IOException {
		
		try {
			
			int recordCount 	= 0;
			JSONObject dev_obj 	= null;
			Iterator<Map<String, Object>> iterator = logs.iterator();
			
			if (logs != null) {
				recordCount = logs.size();
			}

			long prev_Count = 0;
			
			while (iterator.hasNext()) {
				
				TreeMap<String , Object> me = new TreeMap<String, Object> (iterator.next());

				long count_2G = 0;
				long count_5G = 0;
				
				String JStr 		= (String) me.values().toArray()[0];
				String timestamp 	= (String) me.values().toArray()[1];

				JSONObject newJObject = null;
				JSONParser parser = new JSONParser();
				
				newJObject = (JSONObject) parser.parse(JStr);
				
				String radio_type 		= (String) newJObject.get("radio_type");
				long client_count 		= (long) newJObject.getOrDefault("client_count",0);
				
				if (radio_type.equals("2.4Ghz")) {
					count_2G =  client_count;
				} else {
					count_5G =  client_count;
				}
				
				if (count_2G != 0 || count_5G != 0) {
					dev_obj = new JSONObject();

					if (prev_Count != client_count) {

						if (count_2G != 0) {
							dev_obj.put("twoG", count_2G);
						}
						if (count_5G != 0) {
							dev_obj.put("fiveG", count_5G);
						}
						
						dev_obj.put("time", timestamp);
						dev_array.add(dev_obj);
						prev_Count = client_count;
					} else {
						continue;
					}

				}

			}

			if ((dev_array != null && !dev_array.isEmpty()) && dev_array.size() == 1) {
				
				Map<String, Object> last = logs.get(recordCount-1);
				JSONParser parser 		 = new JSONParser();
				
				String JStr 		= (String)last.get("snapshot");
				String timestamp 	= (String)last.get("timestamp");
				
				JSONObject newJObject   = (JSONObject) parser.parse(JStr);
				String radio_type 		= (String) newJObject.get("radio_type");
				long client_count 		= (long) newJObject.getOrDefault("client_count",0);
				
				long count_2G = 0;
				long count_5G = 0;
				
				if (radio_type.equals("2.4Ghz")) {
					count_2G =  client_count;
				} else {
					count_5G =  client_count;
				}
				
				if (count_2G != 0 || count_5G != 0) {
					
					dev_obj = new JSONObject();

					if (count_2G != 0) {
						dev_obj.put("twoG", count_2G);
					}
					if (count_5G != 0) {
						dev_obj.put("fiveG", count_5G);
					}
					dev_obj.put("time", timestamp);
					
					dev_array.add(dev_obj);
				}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}    

	@RequestMapping(value = "/deviceInfo", method = RequestMethod.GET)
	public JSONArray deviceInfo(@RequestParam(value = "cid", required = false) String cid,
								@RequestParam(value = "sid", required = false) String sid,
								@RequestParam(value = "spid", required = false) String spid, 
								HttpServletRequest request,HttpServletResponse response) {
		
		JSONArray deviceInfo = new JSONArray();
		JSONObject deviceObj = null;
		List<Device> deviceList = null;
		Map<String,String> portionmap = new HashMap<String,String>();
			
		if (spid != null && !spid.isEmpty()) {
			deviceList = deviceservice.findBySpid(spid);
		} else if (sid != null && !sid.isEmpty()) {
			deviceList = deviceservice.findBySid(sid);
		} else {
			deviceList = deviceservice.findByCid(cid);
		}
		
		String duration = "30m";
		List<Map<String, Object>>  uptimeInfo = null;
		if (deviceList != null && deviceList.size() > 0) {
			List<Device> sorted_device = new ArrayList<Device>(deviceList);
			
			if (sorted_device.size() > 1) {
				sorted_device.sort((Device dev1,Device dev2)->dev1.getUid().compareTo(dev2.getUid())); 
			}
			for(Device device : sorted_device){
				
				String type = device.getTypefs();
				
				if (StringUtils.isNotBlank(type) && type.equals("server") || type.equals("switch")) {
					continue;
				}
				String uid 				= device.getUid();
				String deviceUptime 	= "0d:0h:0m";
				String appUptime 		= "0d:0h:0m";
				String locationname 	= device.getName();
				String state		    = device.getState()== null ? "inactive" : device.getState();
				String lastseen 	    = device.getLastseen() == null ? "NA" : device.getLastseen();
				String devspid 		    = device.getSpid() == null ? "NA" : device.getSpid() ;
				
				String floorname 		= "NA";
				
				if (portionmap.containsKey(devspid)) {
					floorname = portionmap.get(devspid);
				} else {
					Portion p = portionService.findById(devspid);
					if (p != null) {
						floorname = p.getUid().toUpperCase();
					}
					portionmap.put(devspid, floorname);
				}
				
				String buildVersion = device.getBuildVersion();
				String buildTime 	= device.getBuildTime();
				
				buildVersion = (buildVersion == null || buildVersion.isEmpty()) ? "-" : buildVersion; 
				buildTime    = (buildTime == null || buildTime.isEmpty()) ? "-" : buildTime; 

				uid = uid.toLowerCase();
		    	
				String fsql = " index="+indexname+",sort=timestamp desc,size=1,query=cpu_stats:\"Qubercloud Manager\""
		    			+" AND timestamp:>now-"+duration+" AND uid:\""+uid+"\"|value(uid,uid,NA);"
		    			+" value(cpu_percentage,cpu,NA);value(timestamp,time,NA);"
		    			+" value(cpu_days,cpuDays,NA);value(cpu_hours,cpuHours,NA);value(cpu_minutes,cpuMinutes,NA);" 
						+" value(app_days,appDays,NA);value(app_hours,appHours,NA);value(app_minutes,appMinutes,NA);|table";
    	
				uptimeInfo = fsqlRestController.query(fsql);
				
				if(uptimeInfo != null && uptimeInfo.size()>0){
					Map<String, Object> info = uptimeInfo.get(0);
					
					deviceUptime = info.getOrDefault("cpuDays", 0) +"d:"
								 + info.getOrDefault("cpuHours", 0) +"h:"
								 + info.getOrDefault("cpuMinutes", 0) +"m";
					
					appUptime    = info.getOrDefault("appDays", 0) +"d:"
							 	 + info.getOrDefault("appHours", 0) +"h:"
							 	 + info.getOrDefault("appMinutes", 0) +"m";
				}
				
				deviceObj = new JSONObject();

				deviceObj.put("uid", 			uid);
				deviceObj.put("floorname", 		floorname);
				deviceObj.put("locationname", 	locationname);
				deviceObj.put("deviceUptime", 	deviceUptime);
				deviceObj.put("appUptime", 		appUptime);
				deviceObj.put("state", 			state);
				deviceObj.put("lastseen", 		lastseen);
				deviceObj.put("buildVersion", 	buildVersion);
				deviceObj.put("buildTime", 		buildTime);
				
				deviceInfo.add(deviceObj);
				
			}
		}
		return deviceInfo;
	}
	
	
    @SuppressWarnings("unchecked")
	public  JSONObject reportDetails(String reportType,String time, List<Device> devices) {
    	
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
		 
		 	JSONObject result 		= new JSONObject();
		 	JSONObject jsonObject 	= null;
		 	JSONArray txrxArray 	= new JSONArray();
		 	
		 	HashMap<String, HashMap<String, Long>> hashmap = new HashMap<String, HashMap<String, Long>>();
		 	
			double add_TX = 0;
			double add_RX = 0;
			double add_2G = 0;
			double add_5G = 0;
			
			double avg_2G = 0;
			double avg_5G = 0;
			
			int totla_min_2g = 0;
			int totla_max_2g = 0;
			
			int totla_min_5g = 0;
			int totla_max_5g = 0;
	
		try {
			
			if (devices != null) {

				int size = devices.size();
		
				for (Device dev : devices) {
					
					String devUid = dev.getUid();
					String alias  = dev.getName();

					String deviceFsql ="index="+device_history_event+",type=device_metrics, "
							+ " query=opcode:device_details AND timestamp:>now-"+time+""
							+ " AND uid:\""+devUid+"\" | sum(tx,TX,NA);sum(rx,RX,NA);"
							+ " avg(_2G,avg_2G,NA);avg(_5G,avg_5G,NA);"
							+ " min(_2G,min_2G,NA);max(_2G,max_2G,NA);"
							+ " min(_5G,min_5G,NA);max(_5G,max_5G,NA);"
							+ " avg(_11k_count,_11k_count,NA);"
							+ " avg(_11r_count,_11r_count,NA);"
							+ " avg(_11v_count,_11v_count,NA); "
							+ " value(timestamp,timestamp,NA);|table,sort=Date:desc;";
	
						List<Map<String,Object>> logs = fsqlRestController.query(deviceFsql);
						
					String clientsFsql = "index="+device_history_event+",type=location_change_event,"
							+ " query=opcode:device_details AND exit_time:* AND timestamp:>now-"+time+" AND uid:\""+devUid+"\" "
							+ " AND cid:\""+dev.getCid()+"\"|value(peer_mac,peer_mac,NA);value(peer_tx,peer_tx,NA);value(peer_rx,peer_rx,NA);"
							+ " value(timestamp,timestamp,NA);|table,sort=Date:desc;";
						
					//	LOG.info("clientsFsql " + clientsFsql);
					
						List<Map<String,Object>> clientsLogs = fsqlRestController.query(clientsFsql);
					
						if (!logs.isEmpty() && logs.size() > 0) {
							
							Iterator it = logs.iterator();
							
							while(it.hasNext()) {
								
								Map<String, Object> map = (Map<String, Object>)it.next();
							
								double TX = 0;
								double RX = 0;
								
								if (map.containsKey("TX"))
									TX = Double.parseDouble(map.get("TX").toString());
								if (map.containsKey("RX"))
									RX = Double.parseDouble(map.get("RX").toString());
								
								int average_2G 	= (int)map.getOrDefault("avg_2G", 0);
								int average_5G 	= (int)map.getOrDefault("avg_5G", 0);
								int _11k_count 	= (int)map.getOrDefault("_11k_count", 0);
								int _11r_count 	= (int)map.getOrDefault("_11r_count", 0);
								int _11v_count 	= (int)map.getOrDefault("_11v_count", 0);
								
								double min_2G 	= 0;
								double max_2G 	= 0;
								double min_5G 	= 0;
								double max_5G 	= 0;
								
								String strMin_2G = String.valueOf(map.get("min_2G"));
								String strMax_2G = String.valueOf(map.get("max_2G"));
								String strMin_5G = String.valueOf(map.get("min_5G"));
								String strMax_5G = String.valueOf(map.get("max_5G"));
								
								min_2G = strMin_2G == "null" ? 0 : Double.parseDouble(strMin_2G);
								max_2G = strMax_2G == "null" ? 0 : Double.parseDouble(strMax_2G);
			
								min_5G = strMin_5G == "null" ? 0 : Double.parseDouble(strMin_5G);
								max_5G = strMax_5G == "null" ? 0 : Double.parseDouble(strMax_5G);
	
								totla_min_2g += min_2G;
								totla_max_2g += max_2G;
			
								totla_min_5g += min_5G;
								totla_max_5g += max_5G;
							
								avg_2G +=  average_2G;
								avg_5G +=  average_5G;
								
								add_TX += TX;
								add_RX += RX;
								add_2G += avg_2G;
								add_5G += avg_5G;
	
								String tx = decimalFormat.format(TX);
								String rx = decimalFormat.format(RX);
								
							//	String _2g = decimalFormat.format(average_2G);
							//	String _5g = decimalFormat.format(average_5G);
							
								
								jsonObject = new JSONObject();
								jsonObject.put("alias", alias);
								jsonObject.put("uid", devUid);
								jsonObject.put("tx",  tx);
								jsonObject.put("rx",  rx);
								//jsonObject.put("_2g", _2g);
								//jsonObject.put("_5g", _5g);
								
								jsonObject.put("avg_2g",  decimalFormat.format(average_2G));
								jsonObject.put("avg_5g",  decimalFormat.format(average_5G));
								
								jsonObject.put("min_2g", min_2G);
								jsonObject.put("max_2g", max_2G);
								jsonObject.put("min_5g", min_5G);
								jsonObject.put("max_5g", max_5G); 
								jsonObject.put("_11k", _11k_count);
								jsonObject.put("_11r", _11r_count);
								jsonObject.put("_11v", _11v_count);
								
								txrxArray.add(jsonObject);
							}
					}
							

					/*
					 * CURRENT ACTIVE CLIENTS LIST
					 * 
					 */
						
					
					if (clientCache.containKey(devUid)) {
						
						ConcurrentHashMap<String, HashMap<String, Object>> client_map = clientCache.get_assoc_device_clients(devUid);
						
						for(ConcurrentHashMap.Entry<String, HashMap<String, Object>> peer_client : client_map.entrySet()) {
							
							String peermac = peer_client.getKey();
							HashMap<String, Object> peer_list = peer_client.getValue();
							
							long used_tx = Long.parseLong(peer_list.getOrDefault("_peer_tx_bytes",0).toString());
		    				long used_rx = Long.parseLong(peer_list.getOrDefault("_peer_rx_bytes",0).toString());
		    				
		    				if (!hashmap.containsKey(peermac)) {
								
								HashMap<String, Long> newMAp = new HashMap<String, Long>();
								newMAp.put("peer_tx", used_tx);
								newMAp.put("peer_rx", used_rx);
								hashmap.put(peermac, newMAp);
								
							} else {

								HashMap<String, Long> valMap = hashmap.get(peermac);
								long addpeer_tx = valMap.get("peer_tx") + used_tx;
								long addpeer_rx = valMap.get("peer_rx") + used_rx;
								
								HashMap<String, Long> newMAp = new HashMap<String, Long>();
								newMAp.put("peer_tx", addpeer_tx);
								newMAp.put("peer_rx", addpeer_rx);
								hashmap.put(peermac,newMAp);
							}

						}

						/*
						 * ES CLIENTS LIST
						 */
						
						LOG.info("clientsLogs " +clientsLogs);
						
						if (clientsLogs != null && clientsLogs.size() > 0) {
							
							Iterator<Map<String, Object>> iter = clientsLogs.iterator();
							
							while (iter.hasNext()) {
							
								Map<String, Object> map = iter.next();
								String peermac = (String) map.get("peer_mac");

								long peer_tx = Long.parseLong(map.getOrDefault("peer_tx", 0).toString());
								long peer_rx = Long.parseLong(map.getOrDefault("peer_rx", 0).toString());
								
								//LOG.info("peermac " +peermac);
								//LOG.info("query function  json map " +hashmap);
														
								if (!hashmap.containsKey(peermac)) {
									
									HashMap<String, Long> newMAp = new HashMap<String, Long>();
									newMAp.put("peer_tx",peer_tx);
									newMAp.put("peer_rx",peer_rx);
									hashmap.put(peermac, newMAp);
									
								} else {

									HashMap<String, Long> valMap = hashmap.get(peermac);
								    long addpeer_tx = valMap.get("peer_tx") + peer_tx;
									long addpeer_rx = valMap.get("peer_rx") + peer_rx;
									
									HashMap<String, Long> newMAp = new HashMap<String, Long>();
									newMAp.put("peer_tx", addpeer_tx);
									newMAp.put("peer_rx", addpeer_rx);
									
									hashmap.put(peermac,newMAp);
								}
							}
						}
					}
				}
				
				
				result.put("dev_txrx", 	  txrxArray);

				JSONObject aggDetails = new JSONObject();
				
				if (!reportType.equals("uid")) {
					
					aggDetails.put("agg_tx",  decimalFormat.format(add_TX));
					aggDetails.put("agg_rx",  decimalFormat.format(add_RX));
					aggDetails.put("agg_2g",  decimalFormat.format(add_2G));
					aggDetails.put("agg_5g",  decimalFormat.format(add_5G));
					result.put("agg_txrx", aggDetails);
					
					double average_2G = avg_2G / size;
					double average_5G = avg_5G / size;
					
					JSONObject avg2g_5gDetails = new JSONObject();
					
					avg2g_5gDetails.put("avg_2g", decimalFormat.format(average_2G));
					avg2g_5gDetails.put("avg_5g", decimalFormat.format(average_5G));
					result.put("avg_clients",     avg2g_5gDetails);
					
					JSONObject minandmax2g_5gDetails = new JSONObject();
					
					minandmax2g_5gDetails.put("min_2g", totla_min_2g);
					minandmax2g_5gDetails.put("max_2g", totla_max_2g);
					minandmax2g_5gDetails.put("min_5g", totla_min_5g);
					minandmax2g_5gDetails.put("max_5g", totla_max_5g);
					result.put("min_max_clients", minandmax2g_5gDetails);
					
				}

				JSONObject obj = null;
				JSONArray array = new JSONArray();

				for (Map.Entry<String, HashMap<String, Long>> letterEntry : hashmap.entrySet()) {

					String macAddress = letterEntry.getKey();

					Map<String, Long> nameEntry = letterEntry.getValue();
					long peer_tx = nameEntry.get("peer_tx");
					long peer_rx = nameEntry.get("peer_rx");

					if (peer_tx != 0 && peer_rx != 0) {
						obj = new JSONObject();
						obj.put("peer_mac", macAddress);
						obj.put("peer_tx", peer_tx);
						obj.put("peer_rx", peer_rx);
						array.add(obj);
					}
					
				}
				result.put("top5clients_consumed_tx_rx", array);
			}

		} catch (Exception e) {
			LOG.error("While Gateway Report generation error-> " + e);
			e.printStackTrace();
		}

		LOG.info("AGG Device TXRX  result " + result);

		return result;

	}
    
    public ClientDeviceService getClientDeviceService() {
		if (_clientDeviceService == null) {
			_clientDeviceService = Application.context.getBean(ClientDeviceService.class);
		}
		return _clientDeviceService;
	}
}
