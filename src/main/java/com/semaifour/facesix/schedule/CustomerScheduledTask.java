package com.semaifour.facesix.schedule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.account.PrivilegeService;
import com.semaifour.facesix.account.rest.CustomerRestController;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.rest.FinderReport;
import com.semaifour.facesix.rest.DeviceRestController;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.rest.GatewayReport;
import com.semaifour.facesix.rest.HybridAlert;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.gustpass.GustpassService;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.rest.NetworkDeviceRestController;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.web.SitePortionWebController;
import com.semaifour.facesix.web.WebController;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Service
public class CustomerScheduledTask extends WebController {

	Logger LOG = LoggerFactory.getLogger(CustomerScheduledTask.class.getName());
	
	@Autowired
	CustomerService customerService;

	@Autowired
	UserAccountService userAccountService;

	@Autowired
	SiteService siteService;

	@Autowired
	PortionService portionService;

	@Autowired
	PrivilegeService privilegeService;

	@Autowired
	NetworkConfRestController networkcntrl;	
	
	@Autowired
	private MailSender mailSender;
	
	@Autowired
	GustpassService gustpassService;
	
	@Autowired
	SitePortionWebController sitePortionWebController;

	@Autowired
	NetworkDeviceRestController networkDeviceRestController;

	@Autowired
	ClientDeviceService  _clientDeviceService;
	
	@Autowired
	DeviceService  _deviceService;
	
	@Autowired
	CustomerUtils  customerUtils;
	
	@Autowired
	FinderReport  finderReport;
	
	@Autowired
	DeviceRestController deviceRestcontroller;

	@Autowired
	GatewayReport  gatewayReport;
	
	@Autowired
	HybridAlert hybridAlert;
	
	@Autowired
	FSqlRestController 	fsqlRestController;
	
	@Autowired
	BeaconDeviceService beaconDeviceService;
	
	@Autowired
	CustomerRestController customerRestController;
	
	@Autowired
	ElasticService elasticService;
	
	@Autowired
	private BeaconService beaconService;
	
	@Autowired
	private TrilaterationScheduledTask trilaterationScheduledTask;
	
	String 	device_history = "device-history-event";
	
	
	@Value("${facesix.trilaterationscheduledtask.enable}")
	private boolean tritask_enable;
	
	public static final String ACCOUNT_SID = "AC38ee76f18327cbd3f8303fc62dc08640";
 	public static final String AUTH_TOKEN = "b6ac1ba74de883f212ea7eafb2210faf";
	
 	private String indexname = "facesix*";
 	private final static boolean  isEodAlert = true;
 	
 	@PostConstruct
	public void init() {
		indexname 		= _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
		device_history = _CCC.properties.getProperty("facesix.device.history.event.table",device_history);
	}
 	
	DateFormat format  = CustomerUtils.gatewayKeepAliveDateFormat();
 	DateFormat parse   = CustomerUtils.gatewayKeepAliveDateParser();
 
 	
 	
/*	
	+-------------------- second (0 - 59)
	|  +----------------- minute (0 - 59)
	|  |  +-------------- hour (0 - 23)
	|  |  |  +----------- day of month (1 - 31)
	|  |  |  |  +-------- month (1 - 12)
	|  |  |  |  |  +----- day of week (0 - 6) (Sunday=0 or 7)
	|  |  |  |  |  |  +-- year [optional]
	|  |  |  |  |  |  |
	*  *  *  *  *  *  * command to be executed 
	*/
	
  /* The ? means it depends on other fields.
     The * means all months.
  */
	
	
 	@Scheduled(cron = "0 14 17 * * ?")
	public void customerLicencesExpiredChecking() {

 		
 		if (!tritask_enable) {
 			//LOG.info("customerLicencesExpiredChecking scheduledtask disabled developmen environment");
			return;
		}
 		
 		//LOG.info("customerLicencesExpiredChecking scheduledtask enabled production environment");
 		
		try {
			
			LOG.info("******* Customer Licence Expired Checking *****");
			
			String message=	" Your license has been expired."
					+ " To renew your license please contact our sales team "
					+ " or send an email to support@qubercomm.com.";

			Iterable<Customer> customerList =  customerService.findAll();
			
			if (customerList != null) {
						
					for (Customer customer : customerList) { // license expired checking
						if (customer.getStatus() != null) {
							if (customer.getStatus().equals(CustomerUtils.ACTIVE())) {
							long remainDays = CustomerUtils.getRemainglicenceDays(customer.getServiceExpiryDate());
							LOG.info("remainDays " +remainDays);
							switch ((int) remainDays) {
								case 10:
								case 5:
								case 4:
								case 3:
								case 2:
								case 1:

									message = "Hi "+customer.getCustomerName()+"\n\n Your license will expire in " + remainDays + "(days)."
											+ " To renew your license kindly contact our sales team or send an email to "
											+ "support@qubercomm.com.";
									
								customerUtils.customizeSupportEmail(null, customer.getEmail(), "License Expiry Remainder", message, null);
								break;
							default:
								break;
							}
						}
					}
				}
				
				
				for (Customer customer : customerList) {
					if (customer.getStatus() != null && customer.getStatus().equals(CustomerUtils.ACTIVE())) {

						long remainDays = CustomerUtils.getRemainglicenceDays(customer.getServiceExpiryDate());
						LOG.info("startdate  " + customer.getServiceStartDate() + " enddate "+customer.getServiceExpiryDate());
						LOG.info("CustomerName "+customer.getCustomerName() +" remainDays " + remainDays);

						if (remainDays <= 0) {
							String  userMessage = "Your Portal has been deactivated. To get more information contact admin.\n ";
							customerRestController.changeState(customer.getId(),CustomerUtils.INACTIVE(),message,userMessage);
						}
					}
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	
 	/*	
	+-------------------- second (0 - 59)
	|  +----------------- minute (0 - 59)
	|  |  +-------------- hour (0 - 23)
	|  |  |  +----------- day of month (1 - 31)
	|  |  |  |  +-------- month (1 - 12)
	|  |  |  |  |  +----- day of week (0 - 6) (Sunday=0 or 7)
	|  |  |  |  |  |  +-- year [optional]
	|  |  |  |  |  |  |
	*  *  *  *  *  *  * command to be executed 
	*/
	
  /* The ? means it depends on other fields.
     The * means all months.
  */
 	
	@Scheduled(cron = "0 01 11 * * ?")
	public void perDayDeviceAndTagAlerts() {

		if (!tritask_enable) {
			//LOG.info("networkDeviceAlertForCustomer scheduledtask disabled development environment");
			return;
		}
		
		//LOG.info("networkDeviceAlertForCustomer scheduledtask disabled production environment");
		
		Iterable<UserAccount> usersList = userAccountService.findAll();

		try {
			for (UserAccount users : usersList) {
				if (users.getStatus().equals(CustomerUtils.ACTIVE())) {
					String cid = users.getCustomerId();
					String userId = users.getUid();
					if (StringUtils.isNotBlank(userId)) {
						if (customerUtils.GeoFinder(cid)) {
							finderReport.emailTrigger(userId);
						} else if (customerUtils.Gateway(cid)) {
							gatewayReport.emailTrigger(userId);
						} else if (customerUtils.GatewayFinder(cid)) {
							hybridAlert.emailTrigger(userId);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.info("DeviceEmail Alert Error ", e);
		}

	}

 	@Scheduled (fixedDelay=30000)
	public void deviceKeepAliveTask() {

		try {
			
			if (!tritask_enable) {
				return;
			}
			
			Thread t = new Thread(new Runnable(){

				@Override
				public void run() {
				
				    JSONArray  devArray = new JSONArray();
					Iterable<Customer> customerList = customerService.findAll();

					if (customerList !=null) {
						for(Customer customer : customerList) {
							
							devArray.clear();
							String status = customer.getStatus();
							String  cid	  = customer.getId();
							String  name  = customer.getCustomerName();
							
							boolean enablelog = false;
							if (customer.getLogs() != null && customer.getLogs().equals("true")) {
								enablelog = true;
							}
							
							int inacGWDevCount 		= 0;
							int inacFinderDevCount  = 0;
							
							if ("ACTIVE".equalsIgnoreCase(status)) {
	
								TimeZone totimezone = customerUtils.FetchTimeZone(customer.getTimezone());
								format.setTimeZone(totimezone);
								
								String timestamp 	= "NA";
								String portionName  = "NA";
								String lastSeen     = "NA";
								
								List<Device> device = getDeviceService().findByCid(cid);
								JSONObject devJson  = null;
								
								String className = this.getClass().getName();
								
								if (device != null) {

									for (Device dev : device) {

										String uid 		 = dev.getUid();
										String state     = dev.getState();
										String location  = dev.getName() == null ? "NA" : dev.getName();
										String keepAlive = dev.getKeepAliveInterval();
										
										if (keepAlive != null && !keepAlive.isEmpty()) {
											double buff_sec = 0.02;
											int  fsql_time_in_sec  = CustomerUtils.minutes_to_seconds(keepAlive,buff_sec);
											keepAlive = String.valueOf(fsql_time_in_sec)+"s";
										} else {
											keepAlive = "1m";
										}

										String fsql ="index="+ indexname +",sort=timestamp desc,size=1,query=opcode:\"keep_alive\" "
												+ " AND timestamp:>now-"+keepAlive+" "
												+ " AND uid:\"" + uid +"\" |value(uid,uid,NA);value(timestamp,time,NA);|table";
										
										List<Map<String, Object>> logs = fsqlRestController.query(fsql);

										String message = " UID " + uid + " keep_alive_seconds " + keepAlive + " logs " + logs + " state " + state;
										
										customerUtils.logs(enablelog, className, " Message " + message);
										
										if (logs == null || logs.isEmpty()) {
											
											inacGWDevCount ++;

											String GWMailSent = dev.getCustomizeInactivityMailSent() == null ? "false" : dev.getCustomizeInactivityMailSent();
											
											if (!state.equals(Device.STATE.inactive.name())) {
												
												dev.setState(Device.STATE.inactive.name());
												dev.setModifiedBy("cloud");
												dev.setModifiedOn(new Date(System.currentTimeMillis()));
												dev = getDeviceService().save(dev, false);
												
												if (GWMailSent.equals("false")) {
													if(dev.getSpid() !=null) {
														Portion portion = portionService.findById(dev.getSpid());
														if (portion != null) {
															portionName = portion.getUid() == null ? "NA" : portion.getUid().toUpperCase();
														}	
													}
													
													if (dev.getLastseen() !=null) {
														lastSeen = dev.getLastseen();
													}
													
													devJson =  new JSONObject();
													devJson.put("uid", 	     uid);
													devJson.put("status", 	"INACTIVE");
													devJson.put("alias",     location);
													devJson.put("floor",     portionName);
													devJson.put("lastseen",  lastSeen);
													devArray.add(devJson);
													
													dev.setCustomizeInactivityMailSent("true");
													getDeviceService().save(dev, false);
												}
											}

										} else {

											if (logs != null && logs.size() > 0) {
												try {
													
													Map<String, Object> logMap  = logs.get(0);
													String time   				= (String) logMap.get("time");
													Date dateTime 				= parse.parse(time);
													timestamp     				= format.format(dateTime);
													
												} catch (Exception e) {
													timestamp = format.format(new Date());
												}
												if (!state.equals(Device.STATE.active.name())) {
													dev.setState(Device.STATE.active.name());
													dev.setCustomizeInactivityMailSent("false");
												}
												dev.setLastseen(timestamp);
												dev.setModifiedBy("cloud");
												dev.setModifiedOn(new Date(System.currentTimeMillis()));
												getDeviceService().save(dev, false);

											}
										}
									}
								}

								List<BeaconDevice> beaconDevice = beaconDeviceService.findByCid(cid);

								if (beaconDevice != null) {

									for (BeaconDevice beaconDev : beaconDevice) {
										
										String source=beaconDev.getSource()==null?"qubercomm":beaconDev.getSource();
										if(!source.equals("qubercomm")){
											continue;
										}

										String uid 					   = beaconDev.getUid();
										String state				   = beaconDev.getState();
										String keep_alive_duration 	   = "30s";
										keep_alive_duration 	  	   = beaconDev.getKeepAliveInterval();
										
										if (keep_alive_duration != null && !keep_alive_duration.isEmpty()) {
											int alive_duration = Integer.parseInt(keep_alive_duration) + 30;
											keep_alive_duration = String.valueOf(alive_duration) + "s";
										} else {
											keep_alive_duration = "30s";
										}
										
										String fsql = "index=" + indexname + ",sort=timestamp desc,size=1,query=opcode:\"keep_alive\""
												+ " AND timestamp:>now-" + keep_alive_duration + " AND uid:\"" + uid + "\" | "
												+ "value(timestamp,time,NA);|table";

										List<Map<String, Object>> logs = fsqlRestController.query(fsql);
										
										if (logs == null || logs.isEmpty()) {
											
											inacFinderDevCount ++;
											
											String FinderMailSent = beaconDev.getCustomizeInactivityMailSent() == null ? "false" :beaconDev.getCustomizeInactivityMailSent();
											
											if (!state.equals(BeaconDevice.STATE.inactive.name())) {
												beaconDev.setState(BeaconDevice.STATE.inactive.name());
												beaconDev = beaconDeviceService.save(beaconDev, false);
											}
											if (FinderMailSent.equals("false")) {
												
												String location  = beaconDev.getName() == null ? "NA" : beaconDev.getName();
												if(beaconDev.getSpid() !=null) {
													Portion portion = portionService.findById(beaconDev.getSpid());
													if (portion != null) {
														portionName = portion.getUid() == null ? "NA" : portion.getUid().toUpperCase();
													}
												}

												if (beaconDev.getLastseen() !=null) {
													lastSeen = beaconDev.getLastseen();
												}
												
												devJson =  new JSONObject();
												devJson.put("uid", 	    uid);
												devJson.put("status", 	"INACTIVE");
												devJson.put("alias",     location);
												devJson.put("floor",     portionName);
												devJson.put("lastseen",  lastSeen);
												devArray.add(devJson);
												
												beaconDev.setCustomizeInactivityMailSent("true");
												beaconDeviceService.save(beaconDev, false);
											}
											
										} else {
											
											if (logs != null && logs.size() > 0) {
												
												try {

													Map<String, Object> logMap = logs.get(0);
													String time 			= (String) logMap.get("time");
													Date dateTime 			= parse.parse(time);
													timestamp 				= format.format(dateTime);

												} catch (ParseException e) {
													timestamp = format.format(new Date());
												}

												if (!state.equals(BeaconDevice.STATE.active.name())) {
													beaconDev.setState(BeaconDevice.STATE.active.name());
													beaconDev.setCustomizeInactivityMailSent("false");
												}
												beaconDev.setLastseen(timestamp);
												beaconDeviceService.save(beaconDev, false);
											}
										}
									}

								}
								
								if (devArray != null && (!devArray.isEmpty() && devArray.size() > 0)) {
									builDeviceEmailBody(devArray,cid,name);
								}
	
								int prevDevInacCount = customer.getDeviceAlertCount();
								String solution      = customer.getSolution().toLowerCase();
								int curDevInacCount  = 0;
								
								if (solution.contains("finder")) {
									curDevInacCount += inacFinderDevCount;
								}
								if (solution.contains("gateway") || solution.contains("retail") || solution.contains("heatmap")) {
									curDevInacCount += inacGWDevCount;
								}
			
								if (curDevInacCount != prevDevInacCount) {
									customer.setDeviceAlertCount(curDevInacCount);
									customer.setModifiedBy("cloud");
									customerService.save(customer);
								}
								inacGWDevCount = 0;
								inacFinderDevCount = 0;
							}
						}
					}
				}
			});
			t.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
 	
 	
 	@Scheduled (fixedDelay=3000)
	public void radioSts() {

		try {
			
			if (!tritask_enable) {
				return;
			}
			
			List<String> solution = Arrays.asList("GatewayFinder","Gateway","Retail");
			
			Thread t = new Thread(new Runnable(){

				@Override
				public void run() {
					Iterable<Customer> customerList = customerService.findBySolutionAndStatus(solution,"ACTIVE");
					
					for (Customer customer : customerList) {

						String cid = customer.getId();

						boolean enablelog = false;
						if (customer.getLogs() != null && customer.getLogs().equals("true")) {
							enablelog = true;
						}

						List<Device> device = getDeviceService().findByCid(cid);

						for (Device dev : device) {

							String uid = dev.getUid();
							String sid = dev.getSid();
							String spid = dev.getSpid();

							final String duration = "1m";

							long _2g = 0;
							long _5g = 0;
							int ios = 0;
							int android = 0;
							int windows = 0;
							int printer = 0;
							int speaker = 0;
							int others = 0;

							int _11K_count = 0;
							int _11R_count = 0;
							int _11V_count = 0;

							final String className = this.getClass().getName();

							ConcurrentHashMap<String, Object> dev_map =
									networkDeviceRestController.getBasicClientDetails(dev,enablelog, duration);

							if (dev_map != null && dev_map.size() > 0) {
								
								_2g = (int) dev_map.get("_2G");
								_5g = (int) dev_map.get("_5G");

							   if (_2g != 0 || _5g != 0) {

								customerUtils.logs(enablelog, className, "uid " + uid);
								customerUtils.logs(enablelog, className, "radio stats map " + dev_map);

								ios     = (int)dev_map.get("mac");
								android = (int)dev_map.get("android");
								windows = (int)dev_map.get("win");
								printer = (int)dev_map.get("printer");
								speaker = (int)dev_map.get("speaker");
								others  = (int)dev_map.get("other");

								_11K_count = (int)dev_map.get("_11K");
								_11R_count = (int)dev_map.get("_11R");
								_11V_count = (int)dev_map.get("_11V");

								double prev_Tx = dev.getDeviceTxBytes();
								double prev_Rx = dev.getDeviceRxBytes();

								double curr_Tx = 0;
								double curr_Rx = 0;

								if (dev_map.containsKey("tx")) {
									curr_Tx = Double.valueOf(dev_map.get("tx").toString());
								}
								if (dev_map.containsKey("rx")) {
									curr_Rx = Double.valueOf(dev_map.get("rx").toString());
								}

								double Tx = curr_Tx - prev_Tx;
								double Rx = curr_Rx - prev_Rx;

								if (Tx < 0) {
									Tx = 0;
								}
								if (Rx < 0) {
									Rx = 0;
								}

								
									final String type = "device_metrics";
									HashMap<String, Object> jsonMap = new HashMap<String, Object>();

									jsonMap.put("opcode", "device_details");
									jsonMap.put("cid", cid);
									jsonMap.put("uid", uid);

									if (sid != null)
										jsonMap.put("sid", sid);
									if (spid != null)
										jsonMap.put("spid", spid);

									jsonMap.put("tx", Tx);
									jsonMap.put("rx", Rx);
									jsonMap.put("_2G", _2g);
									jsonMap.put("_5G", _5g);
									jsonMap.put("ios", ios);
									jsonMap.put("android", android);
									jsonMap.put("windows", windows);
									jsonMap.put("printer", printer);
									jsonMap.put("speaker", speaker);
									jsonMap.put("other", others);
									jsonMap.put("_11k_count", _11K_count);
									jsonMap.put("_11r_count", _11R_count);
									jsonMap.put("_11v_count", _11V_count);

									dev.setDeviceRxBytes(curr_Rx);
									dev.setDeviceTxBytes(curr_Tx);
									getDeviceService().save(dev,false);
									
									elasticService.post(device_history, type, jsonMap);
									jsonMap.clear();
								}
							}
						}
					}

				}
			});
			t.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
 	
 	
 	
 	@Scheduled(cron="0 0 0 * * ?")
 	private void processingEODElaspsedTime () {
 		
 		try {
 			
 			
 			if (!tritask_enable) {
				return;
			}
 			
 			List<String> solution  = CustomerUtils.getLocatumsolution();
 			List<String> tagState = CustomerUtils.getLocatumActiveTagStatus();
 			
 			LOG.info("processingEODElaspsedTime !!!!");
 			
 			List<Customer> customerlist = (List<Customer>)customerService.findBySolutionAndStatus(solution, "ACTIVE");
 			
 			Thread thread = new Thread(new Runnable(){
				
 				@Override
				public void run() {
					
					if (!CollectionUtils.isEmpty(customerlist)) {
						
						for (Customer customer : customerlist) {
		 					
		 					final String customerName = customer.getCustomerName();
		 					final String cid 		  = customer.getId();
		 					
		 					DateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		 					
		 					TimeZone zone = customerUtils.FetchTimeZone(customer.getTimezone());
		 					dateFormat.setTimeZone(zone);
							
		 					List<Beacon> beaconList = beaconService.findByCidStateAndStatus(cid, tagState,"checkedout");
		 					
		 					if (!CollectionUtils.isEmpty(beaconList)) {
		 						
		 						for (Beacon beacon : beaconList) {
		 							
		 							final String tagId 		= beacon.getMacaddr();
		 							final String assTo 		= beacon.getAssignedTo();
									final String tagType 	= beacon.getTag_type();
									final String sid     	= beacon.getSid();
									final String spid     	= beacon.getSpid();
									
									final String entryLoc 	= beacon.getEntry_loc();
									final String entryFloor = beacon.getEntry_floor();
									
									String ruid 		= beacon.getReciverinfo();
									
									String now = dateFormat.format(new Date());

									beacon.setEntry_floor(now);
									beacon.setEntry_loc(now);
									beaconService.save(beacon, false);
									
									LOG.info("customerName" + customerName + " tagId  " + tagId + " assTo " + assTo
											+ "now " + now + " entryLoc " + entryLoc + " entryFloor " + entryFloor);
									
		 							final String location_type = "receiver";

									HashMap<String, String> rpMap = new HashMap<String, String>();
									
									rpMap.put("macaddr",   tagId);
									rpMap.put("cid", 	   cid);
									rpMap.put("tagtype",   tagType);
									rpMap.put("prev_sid",  sid);
									rpMap.put("cur_sid",   sid);
									rpMap.put("prev_spid", spid);
									rpMap.put("cur_spid",  spid);
									rpMap.put("prev_reuid",ruid);
									rpMap.put("cur_reuid", ruid);
									rpMap.put("en_location", entryLoc);
									rpMap.put("en_floor",  entryFloor);
									rpMap.put("date",      now);
									rpMap.put("assto",     assTo);
									rpMap.put("location_type", location_type);
									rpMap.put("exitReason", "EOD");

									boolean enablelog = false;
									if (customer.getLogs() != null && customer.getLogs().equals("true")) {
										enablelog = true;
									}
									
									trilaterationScheduledTask.reportProcess(rpMap,enablelog,isEodAlert);
		 						}
		 					}
		 				}
		 			}
				}
				
 			});
 			thread.start();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
	private void builDeviceEmailBody(JSONArray devArray, String  cid,String customerName) {

		String newline 			= System.getProperty("line.separator");
		StringBuilder mailBody  = emailHeader();
		/*StringBuilder smsBody 	= new StringBuilder();
		
		 smsBody.append("")
				.append("ALERTS :")
				.append(newline)
				.append("DEVICES @RISK, REQUIRE YOUR IMMEDIATE")
				.append(" ATTENTION SENT MAIL TO YOU FOR ")
				.append("DETAILED INFO.");*/
		 
		try {

			int i = 0;
			Iterator<JSONObject> itr = devArray.iterator();
			
			while (itr.hasNext()) {
				JSONObject devObj = itr.next();
				i ++;
				String uid 		= (String)devObj.get("uid");
				String status 	= (String)devObj.get("status");
				
				String alias 	= (String)devObj.get("alias");
				String floor 	= (String)devObj.get("floor");
				String lastSeen	= (String)devObj.get("lastseen");
				
				
				mailBody.append("<tr>")
				.append("<td>"+i+"</td>")
				.append("<td  style=\"color:blue\">"+floor.toUpperCase()+newline+"</td>")
				.append("<td  style=\"color:blue\">"+alias.toUpperCase()+newline+"</td>")
				.append("<td  style=\"color:blue\">"+uid+newline+"</td>")
				.append("<td  style=\"color:blue\">"+lastSeen+newline+"</td>")
				.append("<td  style=\"color:blue\">"+status+newline+"</td>")
				.append("</tr>");				 
			}
			
			mailBody.append("</table>");
			mailBody.append("</div><br/><br/>");
			
			final String ismailalert = "true";
			List<UserAccount> users = userAccountService.findByCustomerIdCustomizeEmailSmsAndIsMailAlert(cid, ismailalert,ismailalert);
			//LOG.info("users " +users);
			
			final String subject = "INACTIVE DEVICES NOTIFICATION";

			if (users !=null) {
				for(UserAccount acc : users) {
					if (acc.getStatus().equalsIgnoreCase("active")) {
						String email  = acc.getEmail().trim();
						//constructEmailAlert(email, mailBody);
						customerUtils.customizeSupportEmail(cid, email, subject, mailBody.toString(), null);
					}
				}
			}
			
		} catch (Exception e) {
			LOG.error("while email sent error " +e);
		}

	}

	private StringBuilder emailHeader() {
		
		StringBuilder mailBody = new StringBuilder();
		
		mailBody.append("<div style=\"padding:0px\">")
		 .append(" Dear Customer,")
		 .append("<br/>")
		 .append("<br/>")
		 .append("&nbsp;&nbsp;You have a new Alert Message!!!<br/>")
		 .append("&nbsp;&nbsp;Please find below are the detailed list inactive devices list. Please look in to this as a high priority.<br/>")
		 .append("&nbsp;&nbsp;ALERTS - DEVICES @RISK, REQUIRE YOUR IMMEDIATE ATTENTION.<br/>")
		 .append("<br/>")
		 .append("<table border=\"1\" style=\"border-collapse:collapse;text-align:center;width: 100%;\">")
		 .append("<tr>")
		 .append(" <th style=\"padding:10px\">S.No</th>")
		 .append(" <th style=\"padding:10px\">Floor Name</th>")
		 .append(" <th style=\"padding:10px\">Alias</th>")
		 .append(" <th style=\"padding:10px\">Mac ID</th>")
		 .append(" <th style=\"padding:10px\">Last Seen</th>")
		 .append(" <th style=\"padding:10px\">Status</th>")
		 .append("</tr>");
		
		return mailBody;
	}


	public void EmailandSMSTrigger(String emailId, String mobile, String name, String message) {
			
		try {
			// EMAIL
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();// capitalize first letter
			
			if (emailId != null && message != null) {
				SimpleMailMessage email = new SimpleMailMessage();
				email.setTo(emailId);
				email.setSubject("Qubercomm Notification");
				email.setText("Hi" + " " + name + ",\n " + message+"\nRegards,\nQubercomm Technologies");
				if (email != null) {
					this.mailSender.send(email);
				}
			}

/*			// SMS
			if (mobile != null && message != null) {
				Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
				Message msg = Message.creator(new PhoneNumber("+91" + mobile), new PhoneNumber("+14782988032"), message).create();
			}*/

		} catch (Exception e) {
			LOG.error("CustomerLincenceRemaindern EmailTrigger Error " + e);
		}
	}
	/*
	private void constructEmailAlert(final String emailId, final StringBuilder body) {
				final String deviceInfo = body.toString();
				javaMailSender.send(new MimeMessagePreparator() {
				public void prepare(MimeMessage mimeMessage) throws MessagingException {
					MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
					message.setTo(emailId);
					message.setSubject("Qubercomm Notification");
					message.setText(deviceInfo, true);
				}
			});
		}
	*/
	
	public ClientDeviceService getClientDeviceService() {
		if (_clientDeviceService == null) {
			_clientDeviceService = Application.context.getBean(ClientDeviceService.class);
		}
		return _clientDeviceService;
	}
	
	public DeviceService getDeviceService() {
		if (_deviceService == null) {
			_deviceService = Application.context.getBean(DeviceService.class);
		}
		return _deviceService;
	}
	
}
