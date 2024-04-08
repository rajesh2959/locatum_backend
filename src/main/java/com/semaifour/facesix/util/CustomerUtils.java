package com.semaifour.facesix.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;

import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.session.SessionCache;
import com.semaifour.facesix.spring.CCC;

import net.sf.json.JSONObject;
@Controller
public class CustomerUtils {
	
	@Autowired
	CustomerService customerService;
	
	@Value("${facesix.cloud.name}")
	private String cloudUrl;
	
	@Value("${facesix.cloud.security.enable}")
	private int cloudSecurity;
	
	@Value("${spring.mail.host}")
	private String quberHost;
	
	@Value("${spring.mail.port}")
	private String quberPort;
	
	@Value("${spring.mail.username}")
	private String quberUsername;
	
	@Value("${spring.mail.password}")
	private String quberPassword;
	
	@Value("${progsets.query.url}")
	private String progsets_url;
	
	@Autowired
	private Cryptor cryptor;
	
	@Autowired
	private SessionCache sessionCache;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private PortionService portionService;
	
	
	public static  ConcurrentHashMap<String, String>  binaryUpgradeCache = new ConcurrentHashMap<String, String>();
	
	@Autowired
	private CCC _CCC;
	
	static Logger LOG = LoggerFactory.getLogger(CustomerUtils.class.getName());
	
	public static long getRemainglicenceDays(Date expireDate) throws ParseException {
		long diff = expireDate.getTime() - new Date().getTime();
		long remainDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		//System.out.println("SimpleDateCalulation remainDays " + remainDays + "startDate " + startDate);
		return remainDays;
	}

	public static SimpleMailMessage constructResetTokenEmail(String emailId, String name, String message) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(emailId);
		email.setSubject("Qubercomm Notification");
		email.setText("Hi" + "," + name + " " + message);
		return email;
	}
	
	public static Date formatDate(Date date) throws ParseException {
		final String pattern = "dd/MM/yyyy";
		Date dateValue = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			if (null != date) {
				String strDate = sdf.format(date);
				dateValue = sdf.parse(strDate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dateValue;
	}

	public static final String ACTIVE() {
		return "ACTIVE";
	}

	public static final String INACTIVE() {
		return "INACTIVE";
	}

	public static String getRemaingDaysHoursMinutesSeconds(Date startDate, Date endDate) throws ParseException {

		String ddhhmmss = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
		startDate = sdf.parse(sdf.format(startDate));
		endDate = sdf.parse(sdf.format(endDate));
		long different = endDate.getTime() - startDate.getTime();

		//System.out.println("startDate : " + startDate);
		//System.out.println("endDate : " + endDate);
		//System.out.println("different : " + different);

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;

		ddhhmmss = elapsedDays + "-" + elapsedHours + "-" + elapsedMinutes + "-" + elapsedSeconds;
		//System.out.printf("%d days, %d hours, %d minutes, %d seconds%n", elapsedDays, elapsedHours, elapsedMinutes,
		//		elapsedSeconds);
		return ddhhmmss;

	}

	public  TimeZone FetchTimeZone(String zone){
		TimeZone timezone = null;
		timezone = TimeZone.getTimeZone(zone);
		return timezone;
	}
	public boolean GeoFinder(String customerId) {
		Customer customer = customerService.findById(customerId);
		if (customer != null) {
			if (customer.getSolution().equals("GeoFinder")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean Gateway(String customerId) {
		Customer customer = customerService.findById(customerId);
		boolean isTrue = false;
		if (customer != null) {
			if (customer.getSolution().equals("Gateway")) {
				isTrue = true;
			} else if (customer.getVenueType().equals("Gateway")) {
				isTrue = true;
			}
		}
		return isTrue;
	}
	
	public boolean Vpn(String customerId) {
		Customer customer = customerService.findById(customerId);
		if (customer != null) {
			String vpn = customer.getVpn();
			 if (vpn != null && vpn.equals("true")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean GatewayFinder(String customerId) {
		Customer customer = customerService.findById(customerId);
		if (customer != null) {
			 if (customer.getSolution().equals("GatewayFinder")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean GeoLocation(String customerId) {
		Customer customer = customerService.findById(customerId);
		if (customer != null) {
			 if (customer.getSolution().equals("GeoLocation")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean entryexit(String customerId) {
		Customer customer = customerService.findById(customerId);
		if (customer != null) {
			 if (customer.getVenueType().equalsIgnoreCase("Patient-Tracker")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean Locatum(String cid) {
		Customer customer = customerService.findById(cid);
		if (customer != null) {
			if (customer.getVenueType().equalsIgnoreCase("Locatum")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean trilateration(String cid) {
	
		if (cid != null && !cid.isEmpty()) {
			Customer customer = customerService.findById(cid);
			if (customer != null) {
				 if (GeoFinder(cid) || GatewayFinder(cid)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public boolean Heatmap(String cid) {
		Customer customer = customerService.findById(cid);
		if (customer != null) {
			if (customer.getSolution().equals("Heatmap")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public void logs(boolean logenabled,String classname, String msg){
		if (logenabled) {
			Logger LOG = LoggerFactory.getLogger(classname);
			LOG.info(msg);
		}
	}
	
	public static boolean isStringNotEmpty(String input){
        if(input == null || input.equals("null") ||  input.length() == 0 || input.isEmpty() ){
            return false;
        }
        return true;
    }
	
	public String cloudUrl() {

		String security = null;

		
		if (cloudSecurity == 1) {
			security = "https://";
		} else {
			security = "http://";
		}

		String url = security + cloudUrl;

		LOG.info("CLOUD URL " +url);
		
		return url;
	}
	
	
	public void customizeSupportEmail (String cid, String toEmail, String subject, String body, File attachment) {
		
		try {

			if (StringUtils.isEmpty(toEmail)) {
				LOG.info("To email Id Empty for given Message " + body);
				return;
			}
			
			String fromEmail = quberUsername;
			String password  = quberPassword;
			String host 	 = quberHost;
			String port 	 = quberPort;
			String regards 	 = "QUBERCOMM TECHNOLOGIES.";

			Customer cx = null;
			
			if (StringUtils.isNotEmpty(cid)) {
				cx = customerService.findById(cid);
			}

			if (cx != null) {
				String supportEnable = cx.getCustSupportEmailEnable();
				if (StringUtils.isNotEmpty(supportEnable)) {
					if (supportEnable.equals("true")) {
						fromEmail 		= cx.getCustSupportEmailId();
						password 		= cx.getCustSupportPassword();
						host			= cx.getCustSupportHost();
						port 			= cx.getCustSupportPort();
						regards 		= cx.getCustomerName().toUpperCase();
					}
				} else if(Gateway(cid)) {
					regards = "NMESH";
				}else if(Locatum(cid)) {
					regards = "LOCATUM";
				}
			}

			if (body.contains("<br/>")) {
				body += "&nbsp;&nbsp;Regards,</b> <br/>"
						+ "&nbsp;&nbsp;Support Team </b> <br/>"
					 + "&nbsp;&nbsp;" + regards + "</b> <br/>";
			} else {
				body += "\n\n Regards,\n " + regards;
			}
				
			Properties props = new Properties();
			
			props.put("mail.smtp.host", host); 
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			
			final String fromEmailId 	= fromEmail;
			final String emailPassword 	= password;
			
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmailId, emailPassword);
				}
			};
			
			Session session = Session.getInstance(props, auth);
			
			sendEmail(session, fromEmailId, toEmail, subject, body, attachment);

		} catch (Exception e) {
			LOG.error("while mail send error ");
			e.printStackTrace();
		}
	}
	
	public void sendEmail(Session session, String fromEmail, String toEmail, 
						  String subject, String body,File attachment) {

		try {

			MimeMessage msg = new MimeMessage(session);

			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress(fromEmail));
			msg.setReplyTo(InternetAddress.parse(fromEmail, false));
			msg.setSubject(subject, "UTF-8");

			if (body.contains("<br/>")) { // HTML Table Format
				msg.setText(body, "UTF-8", "html");
			} else { // Plain Text Format
				msg.setText(body, "UTF-8");
			}

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

			if (attachment != null) {

				Multipart multipart = new MimeMultipart();
				BodyPart messageAttachmentBody = new MimeBodyPart();
				BodyPart messagePlainTextBodyPart = new MimeBodyPart();

				/*
				 * File Attachment
				 * 
				 */

				messageAttachmentBody = new MimeBodyPart();
				DataSource source = new FileDataSource(attachment.getPath());
				messageAttachmentBody.setDataHandler(new DataHandler(source));
				messageAttachmentBody.setFileName(attachment.getName());
				multipart.addBodyPart(messageAttachmentBody);

				/*
				 * Plain Text
				 * 
				 */

				messagePlainTextBodyPart = new MimeBodyPart();
				messagePlainTextBodyPart.setText(body);
				multipart.addBodyPart(messagePlainTextBodyPart);

				msg.setContent(multipart);
			}

			LOG.info("Email Ready To send.....");
			Transport.send(msg);
			LOG.info("EMail Sent Successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long calculateElapsedTime(Date entry, Date exit) {
		long duration = exit.getTime() - entry.getTime();
		long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		return diffInSeconds;
	}
	
	private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		}
	};

	public String getCurrentTimeForZone(String cid) {
		String formatDate = null;
		try {
			if (cid != null) {
				Customer customer = customerService.findById(cid);
				if (customer != null) {
					TimeZone zone = FetchTimeZone(customer.getTimezone());
					df.get().setTimeZone(zone);
				}
			}
		} catch (Exception e) {
			formatDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());
			LOG.error("while get getCurrentTimeForZone error " +e);
			e.printStackTrace();
		}

		formatDate = df.get().format(new Date());
		return formatDate;
	}
	
	  
	 public static String formatFileSize(long size) {
		 
	        String hrSize = "0.00 KB";

	       double b = size;
	       double k = size/1024.0;
	       double m = ((size/1024.0)/1024.0);
	      // double g = (((size/1024.0)/1024.0)/1024.0);
	      // double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

	        DecimalFormat dec = new DecimalFormat("0.00");
	        hrSize = dec.format(m).concat(" MB");
	        
			/*if (t > 1) {
				hrSize = dec.format(t).concat(" TB");
			} else if (g > 1) {
				hrSize = dec.format(g).concat(" GB");
			} else if (m > 1) {
				hrSize = dec.format(m).concat(" MB");
			} else if (k > 1) {
				hrSize = dec.format(k).concat(" KB");
			} else {
				hrSize = dec.format(b).concat(" Bytes");
			} */

	        return hrSize;
	    }
	 
	 
	 public static int minutes_to_seconds(String time,double buff_sec) {
			
		int in_minutes 			= Integer.parseInt(time);
		int in_seconds   		= (in_minutes * 60);
		double buffer_in_sec 	= (in_seconds * buff_sec);
		double fsql_in_sec 		= (in_seconds + buffer_in_sec);
		int fs_time 			= (int) Math.round(fsql_in_sec);

		return fs_time;
	}

	public static String secondsto_hours_minus_days(long seconds) {

		int day 	= (int) TimeUnit.SECONDS.toDays(seconds);
		long hours  = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
		long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
		long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

		String ssmmhhdd 	= String.valueOf(day) 	 +"d:" 
							+ String.valueOf(hours)  +"h:" 
							+ String.valueOf(minute) +"m:"
							+ String.valueOf(second) +"s";

		return ssmmhhdd;

	}
	
	public static String secondsToHHMMSS(long timestampinseconds) {
		long hours = timestampinseconds / 3600;
		long minutes = (timestampinseconds % 3600) / 60;
		long seconds = timestampinseconds % 60;
		String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		return time;

	}
	
	 public static String bytesconverter(long size) {
		 
	        String hrSize = "0 KB";

	       double b = size;
	       double k = size/1024.0;
	       double m = ((size/1024.0)/1024.0);
	       double g = (((size/1024.0)/1024.0)/1024.0);
	       double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

	        DecimalFormat dec = new DecimalFormat("0.00");
	        hrSize = dec.format(m).concat(" MB");
	        
			if (t > 1) {
				hrSize = dec.format(t).concat(" TB");
			} else if (g > 1) {
				hrSize = dec.format(g).concat(" GB");
			} else if (m > 1) {
				hrSize = dec.format(m).concat(" MB");
			} else if (k > 1) {
				hrSize = dec.format(k).concat(" KB");
			} else {
				hrSize = dec.format(b).concat(" Bytes");
			} 

	        return hrSize;
	    }
	 
	public DateFormat set_date_format_hh_mm_ss(String cid) {
		DateFormat hhmmss = new SimpleDateFormat("HH:mm");
		if (cid != null) {
			Customer cx = customerService.findById(cid);
			if (cx != null) {
				TimeZone istTimeZone = FetchTimeZone(cx.getTimezone());
				hhmmss.setTimeZone(istTimeZone);
			}
		} else {
			TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
			hhmmss.setTimeZone(istTimeZone);
		}
		return hhmmss;
	}

	public boolean isRetail(String cid) {
		boolean isTrue = false;
		Customer customer = customerService.findById(cid);
		if (customer != null) {
			isTrue = customer.getSolution().equalsIgnoreCase("Retail") ? true : false;
		}
		return isTrue;
	}
	
	/**
	 * 
	 * @param cid
	 * @param request
	 * @param response
	 * @return
	 */
	public String resolveSiteCustomer(String cid, HttpServletRequest request, HttpServletResponse response) {
		if (!sessionCache.equals(request.getSession(), "cid", cid)) {
			Customer customer = customerService.findById(cid);
			if (customer != null) {
				sessionCache.setAttribute(request.getSession(), "cid", customer.getId());
				SessionUtil.setCurrentCustomer(request.getSession(), customer.getId());
				SessionUtil.setCurrentSiteCustomerId(request.getSession(), cid);
				
			} else {
				return null;
			}
		}
		return cid;
	}
	/**
	 * 
	 * @param sid
	 * @param request
	 * @param response
	 * @return
	 */
	public String resolveSite(String sid, HttpServletRequest request, HttpServletResponse response) {
		if (!sessionCache.equals(request.getSession(), "sid", sid)) {
			Site site = siteService.findById(sid);
			if (site != null) {
				sessionCache.setAttribute(request.getSession(), "sid", sid);
				sessionCache.setAttribute(request.getSession(), "suid", site.getUid());
				sessionCache.setAttribute(request.getSession(), "cid", site.getCustomerId());
				SessionUtil.setCurrentSite(request.getSession(), sid);
				SessionUtil.setCurrentCustomer(request.getSession(), site.getCustomerId());
				SessionUtil.setCurrentSiteCustomerId(request.getSession(), site.getCustomerId());
			} else {
				return null;
			}
		}
		return sid;
	}
	
	/**
	 * 
	 * @param spid
	 * @param request
	 * @param response
	 * @return
	 */
		
	public String resolveSitePortion(String spid, HttpServletRequest request, HttpServletResponse response) {
		if (!sessionCache.equals(request.getSession(), "spid", spid)) {
			Portion site = portionService.findById(spid);
			if (site != null) {
				sessionCache.setAttribute(request.getSession(), "spid", spid);
				sessionCache.setAttribute(request.getSession(), "spuid", site.getUid());
				sessionCache.setAttribute(request.getSession(), "cid", site.getCid());
				SessionUtil.setCurrentCustomer(request.getSession(), site.getCid());
				SessionUtil.setCurrentPortionCustomerId(request.getSession(), site.getCid());
				
			} else {
				return null;
			}
		}
		return spid;
	}
	
	/*
	 * Query From Progsets
	 */
	public JSONObject progset_query(String query) throws Exception {

		String inputLine = "";
		byte[] postData = query.getBytes(StandardCharsets.UTF_8);
		int postDataLength = query.length();

		URL obj = new URL(progsets_url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setDoOutput(true);
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/psql");
		con.setRequestProperty("Authorization", "Basic admin:nimda");
		con.setRequestProperty("charset", "utf-8");
		con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		con.setUseCaches(false);
		con.getOutputStream().write(postData);

		InputStream is = con.getInputStream();

		if (is != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			inputLine = in.readLine();
			in.close();
		}
		return JSONObject.fromObject(inputLine);
	}
	
	
	public static String bytesToMbps(double size) {
		String mbps = "0.00 mbps";
		double m = ((size / 1024.0) / 1024.0);
		DecimalFormat dec = new DecimalFormat("0.00");
		mbps = dec.format(m).concat(" mbps");
		return mbps;
	}
	
	public static String decimalFormat(double data) {
		DecimalFormat dec = new DecimalFormat("##.###");
		return dec.format(data);
	}
	
	
	public static List<String> getLocatumsolution() {
		List<String> solution = Arrays.asList("GatewayFinder","GeoFinder");
		return solution;
	}

	public static List<String> getLocatumActiveTagStatus() {
		List<String> activeTagStatus = Arrays.asList("active","idle");
		return activeTagStatus;
	}
	
	/**
	 * Used to delete file
	 * @param type
	 * @param fileName
	 * @return
	 */
	
	public String removeFile(final String type, String fileName) {

		String body = "File not found";
		
		if (StringUtils.isEmpty(fileName)) {
			return body;
		}

		String targetFolder ="./uploads";
		
		if (type.equals("binary")) {
			 targetFolder = _CCC.properties.getProperty("facesix.fileio.binary.root", "/var/www/html");
		} else if (type.equals("uploads")) {
			fileName = fileName.split("/")[2];
			targetFolder = _CCC.properties.getProperty("facesix.fileio.root", "./_FSUPLOADS_");
		} else {
			body = "File type unrecognized";
			LOG.info("type " +body);
			return body;
		}
		

		String filePath = targetFolder + "/" + fileName;
		File file = new File(filePath);

		if (file.exists()) {
			boolean success = file.delete();
			if (success) {
				body = "File deleted sucess";
			} else {
				body = "File  deletion failed!!!";
			}
		} 
		
		LOG.info(" FILE NAME" + fileName + " RESPONSE " + body);
		
		return body;
	}

	public static List<String> locatumSolutionList = Arrays.asList("GatewayFinder","GeoFinder");
	
	public static String getUploadType(String type) {

		if (type.equals("floor") || type.equals("venue") || type.equals("profile")) {
			return "uploads";
		} else if (type.equals("binary")) {
			return "binary";
		}

		return type;
	}
	
	public static enum domain {
		locatum, nmesh
	};
	
	public static enum crashDumpFolder {
		nmesh_crash_dump, locatum_crash_dump
	};

	public String createCrashDumpFolderName(String domain) {
		if (domain.equals("locatum")) {
			return CustomerUtils.crashDumpFolder.locatum_crash_dump.name();
		} else {
			return CustomerUtils.crashDumpFolder.nmesh_crash_dump.name();
		}
	}
	
	/**
	 * Used to convert string to JSON  object
	 * @param template
	 * @return
	 */
	public org.json.simple.JSONObject stringToSimpleJson(String jsonString) {
		JSONParser parser = new JSONParser();
		org.json.simple.JSONObject jsonPayload = new org.json.simple.JSONObject();
		try {
			jsonPayload = (org.json.simple.JSONObject) parser.parse(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonPayload;
	}
	
	/**
	 * Used to convert string to JSONArray  object
	 * @param jsonString
	 * @return
	 */
	public JSONArray stringToSimpleJsonArray(String jsonString) {
		JSONArray array = new JSONArray();
		try {
			JSONParser parser = new JSONParser();
			array = (JSONArray) parser.parse("[{\"user_id\": 1}]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;
	}
	
	/**
	 * Data usage auto type conversion (MB to kb,MB to GB,MB to TB) 
	 * @param size
	 * @return
	 */
	public static String convertDataUsage(double size) {
		String s = "";
		if (size < 1) {
			return String.format("%.2f", size * 1024l) + " KB";
		} else if (size >= 1024l && size < (1024l * 1024l)) {
			return String.format("%.2f", size / 1024l) + " GB";
		} else if (size >= (1024l * 1024l)) {
			return String.format("%.2f", size / (1024l * 1024l)) + " TB";
		} else {
			s = String.format("%.2f", size) + " MB";
		}

		return s;
  }
	public String formatReportDate(final String strDate) {
		
		String formatedDateStr = "";
		
		if (!StringUtils.isEmpty(strDate)) {
			
			try {
				
				DateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				formatedDateStr 	  = format.format(parser.parse(strDate));
				
			} catch (ParseException e) {
				formatedDateStr = strDate;
				LOG.info("formatting given date error  " + strDate + " Error Message " + e.getMessage());
			}
		}
		
		LOG.debug("input date " +strDate + " formatedDateStr " +formatedDateStr);
		return formatedDateStr;
	}
	
	
	public static DateFormat gatewayKeepAliveDateFormat() {
		DateFormat dateFormat  = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		return dateFormat;
	}

	public static DateFormat gatewayKeepAliveDateParser() {
		DateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return dateParser;
	}

}
