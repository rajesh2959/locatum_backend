package com.semaifour.facesix.schedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconAlertData;
import com.semaifour.facesix.beacon.data.BeaconAlertDataService;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.data.ReportBeacon;
import com.semaifour.facesix.beacon.data.ReportBeaconService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.geofence.data.Geofence;
import com.semaifour.facesix.geofence.data.GeofenceAlert;
import com.semaifour.facesix.geofence.data.GeofenceAlertService;
import com.semaifour.facesix.geofence.data.GeofenceService;
import com.semaifour.facesix.geofence.data.GeofenceUtils;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.spring.CCC;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.EmailService;
import com.semaifour.facesix.util.EmailTemplateService;

@Controller
public class TrilaterationScheduledTask extends RecursiveTask<Integer> {

	@Autowired
	private CCC _CCC;
	
	@Autowired
	private CustomerService customerservice;
	
	@Autowired
	private CustomerUtils customerUtils;
	
	@Autowired
	private UserAccountService userAccountService;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private PortionService portionservice;
	
	@Autowired
	private BeaconService beaconservice;
	
	@Autowired
	private ReportBeaconService reportBeaconservice;
	
	@Autowired
	private BeaconDeviceService beaconDeviceService;
	
	@Autowired
	private NetworkConfRestController networkConfRestController;
	
	@Autowired
	private BeaconAlertDataService beaconAlertDataService; 
	
	@Autowired
	private GeofenceService geofenceService;
	
	@Autowired
	private GeofenceAlertService geofenceAlertService;
		
	@Autowired
	private GeofenceUtils geofenceUtils;
	
	@Autowired
	private FSqlRestController fsqlRestController;
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private EmailTemplateService emailTemplateService;
	
	@Value("${facesix.trilaterationscheduledtask.enable}")
	private boolean tritask_enable;


	private static final long serialVersionUID = 1L;
	private static final String SCHEDULER_TIME = "11";
	private static final String FENCE_STATE    = "enabled";
	private boolean flag 					   = false;
	private static String classname			   = TrilaterationScheduledTask.class.getName();
	static Logger LOG 						   = LoggerFactory.getLogger(classname);
	static final int THRESHOLD 				   = 1;
	//testing
	//String reportEventIndex 	 = "facesix-report-event";
	String reportEventIndex 	 = "facesix-int-beacon-event";
	String dashboardAlertIndex 	 = "dashboard-alert-event";
	String batteryIndex			 = "facesix*";

	List<String> solution 		 = CustomerUtils.locatumSolutionList;
	
	DateFormat format 				  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	DateFormat parse 				  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	DateFormat batteryCheckDateFormat = new SimpleDateFormat("hh");
	
	ForkJoinPool forkJoinPool   = new ForkJoinPool();
	
	private final static boolean isEodAlert = false;
	String cid = null;
	String custName = null;

	boolean logenabled  = false;

	int reportFlag     = 0;
	int tlu			   = 0;

	TimeZone timezone   = null;

	private List<Portion> portionlist = null;
	private HashMap<String,String> reportMap = null;
	
	@PostConstruct
	private void init() {
		reportEventIndex = _CCC.properties.getProperty("facesix.data.beacon.trilateration.table",
				reportEventIndex);
		dashboardAlertIndex = _CCC.properties.getProperty("dashboard.alert.table", dashboardAlertIndex);
		batteryIndex = _CCC.properties.getProperty("elasticsearch.indexnamepattern", batteryIndex);
	}
	
	private void setCid (String cid) {
		this.cid = cid;
	}
	
	private void setCustomerName (String customerName) {
		this.custName = customerName;
	}
	
	private void setLog(boolean log) {
		this.logenabled = log;
	}
	
	private void setTimeZone(TimeZone timezone) {
		this.timezone = timezone;
	}
	
	private void setReportFlag(int flag) {
		this.reportFlag = flag;
	}
	
	private void setPortionlist(List<Portion> portionlist) {
		this.portionlist = portionlist;
	}
	
	private void setReportMap(HashMap<String,String> rpMap) {
		this.reportMap = rpMap;
	}
	
	
	/*
	 * Fetch Battery status for all tags
	 */
	private boolean processingBattery() {

		boolean bRet = false;

		try {

			ArrayList<Beacon> taglist 		= new ArrayList<Beacon>();
			List<Map<String, Object>> logs  = null;
			Iterable<Customer> customer 	= getCustomerService().findAll();
			
			for (Customer cust : customer) {
				String cid 		= cust.getId();
				
				if (getCustomerUtils().trilateration(cid) || getCustomerUtils().entryexit(cid)) {
				
					String status 	= "checkedout";
					
					Collection<Beacon> beaconCollection = getBeaconService().getSavedBeaconByCidAndStatus(cid, status);
					
					for (Beacon beacon : beaconCollection) {
						
						String tagid = beacon.getMacaddr();
						
						String fsql = "index="+batteryIndex+",size=1,query=timestamp:>now-12h"
								+ " AND opcode:\"batteryReport\" AND uid:\""+tagid+"\" | value(uid,taguid,NA);"
								+ " value(batt_level,batteryLevel,NA);value(batt_timestamp,time,NA) | table";

						logs = getFSqlRestController().query(fsql);

						if (logs == null || logs.isEmpty()) {
							continue;
						}
						Map<String, Object> map = logs.get(0);
						
						if (map.containsKey("batteryLevel")) {
							
							String nextUpdateTime    = map.getOrDefault("time", "0").toString();
							String batteryLevelStr   = map.getOrDefault("batteryLevel","0").toString();
							
							long batteryTimeStamp = Long.valueOf(nextUpdateTime);
							int battLevel  		  = Integer.parseInt(batteryLevelStr);
							
							beacon.setBattery_timestamp(batteryTimeStamp);
							beacon.setBattery_level(battLevel);
							beacon.setModifiedBy("cloud");
							beacon.setModifiedOn(new Date());
							taglist.add(beacon);
							bRet = true;
						}
		
					}
					getBeaconService().save(taglist);
					
					String battery 			= cust.getBattery_threshold();
					int batteryThreshold 	= 40;
					int lowBatteryCount 	= 0;
					
					if (battery != null && !battery.isEmpty()) {
						batteryThreshold = Integer.parseInt(battery);
					}
					
					List<Beacon> lowBatteryBeacons = getBeaconService().findByCidStatusAndBatteryLevel(cid, "checkedout", batteryThreshold);
					
					if (lowBatteryBeacons != null && lowBatteryBeacons.size() > 0) {
						lowBatteryCount = lowBatteryBeacons.size();
					}
					
					cust.setBatteryAlertCount(lowBatteryCount);
					cust = getCustomerService().save(cust);
				}
			}

		} catch (Exception e) {
			LOG.info("iam Battery Processor " +e.getMessage());
			e.printStackTrace();
		}

		return bRet;
	}
	
	
	
	@Override
	protected Integer compute() {
		int tagcount = 0;
		long stimer = System.currentTimeMillis();
		try {
			if(this.reportFlag == 1) {
				reportProcess(this.reportMap,this.logenabled,isEodAlert);	
			} else {
				
				String cid 			 = this.cid;
				String customerName  = this.custName;
				boolean logenabled   = this.logenabled;
				TimeZone timezone    = this.timezone;
				String beacon_status = "checkedout";
				long lastseenAfter 	 = System.currentTimeMillis() - 3000;
				
				List<Beacon> beaconlist = getBeaconService().findByCidStatusAndLastSeenAfter(cid, beacon_status, lastseenAfter);
				
				JSONObject alertsJson = new JSONObject();
				alertsJson.put("smsAlertJson", new JSONObject());
				alertsJson.put("mailAlertJson", new JSONObject());
				alertsJson.put("dashboardAlertJson", new JSONObject());

				if(beaconlist == null || beaconlist.isEmpty()) {
					tagInactivityMail(cid,alertsJson);
					return 1;
				}else {
					tagcount = beaconlist.size();
				}
				getCustomerUtils().logs(logenabled, classname, "At customer "+customerName+" Beaconlist size = "+beaconlist.size());
				ArrayList<TrilaterationScheduledTask> rpttasks = new ArrayList<TrilaterationScheduledTask>();
				List<Geofence> geofencelist = getGeofenceService().findByCidAndStatus(cid, FENCE_STATE);
				Map<String,GeofenceAlert> geofenceAlertMap = getGeofenceAlertService().getFenceAlertMap(cid, FENCE_STATE);
				
				List<Beacon> saveBeaconList = new ArrayList<Beacon>();
				List<ReportBeacon> saveReportBeaconList = new ArrayList<ReportBeacon>();
				
				for (Beacon beacon : beaconlist) {

					String tagid = beacon.getMacaddr();
					
					ReportBeacon reportBeacon = getReportBeaconService().findOneByMacaddr(tagid);
					
					if (reportBeacon == null) {
						reportBeacon = getReportBeaconService().setNewReportBeacon(beacon);
					}
					
					if (beacon.getLastSeen() > reportBeacon.getLastSeen()) {

						String prev_loc = reportBeacon.getReciverinfo();
						String cur_loc  = beacon.getReciverinfo();
						String location_type = "receiver";

						if (!prev_loc.equals(cur_loc)) {
							HashMap<String, String> rpMap = new HashMap<String, String>();

							rpMap.put("macaddr", tagid);
							rpMap.put("cid", cid);
							rpMap.put("tagtype", beacon.getTagType());
							rpMap.put("location_type", location_type);
							rpMap.put("prev_sid", reportBeacon.getSid());
							rpMap.put("cur_sid", beacon.getSid());
							rpMap.put("prev_spid", reportBeacon.getSpid());
							rpMap.put("cur_spid", beacon.getSpid());
							rpMap.put("prev_reuid", prev_loc);
							rpMap.put("cur_reuid", cur_loc);
							rpMap.put("en_location", reportBeacon.getEntry_loc());
							rpMap.put("en_floor", reportBeacon.getEntry_floor());
							rpMap.put("date", beacon.getEntry_loc());
							rpMap.put("assto", beacon.getAssignedTo());
							rpMap.put("exitReason", "receiver_change");

							TrilaterationScheduledTask rtask = new TrilaterationScheduledTask();
							rtask.setReportFlag(1);
							rtask.setLog(logenabled);
							rtask.setTimeZone(timezone);
							rtask.setCid(cid);
							rtask.setCustomerName(customerName);
							rtask.setReportMap(rpMap);
							rtask.fork();
							rpttasks.add(rtask);
							
							getCustomerUtils().logs(logenabled, classname, "Location Change: tagid "+tagid+" prev_loc = "+ prev_loc+ " cur_loc = "+cur_loc);
						}
					}

					if (geofencelist != null && !geofencelist.isEmpty()) {
						
						Map<String, String> siteMap = new HashMap<String, String>();
						JSONObject geofencestatusJson = beacon.getGeofencestatus();
						
						if (geofencestatusJson == null) {
							geofencestatusJson = new JSONObject();
						}
						double x = Double.valueOf(beacon.getX());
						double y = Double.valueOf(beacon.getY());
						String bcnSpid = beacon.getSpid();
						String location_type = "geofence";
						String tagtype = beacon.getTagType();

						for (Geofence geofence : geofencelist) {

							String fenceid = geofence.getId();
							HashMap<String,Object> fenceStatus = new HashMap<String,Object>();
							String fencesid = geofence.getSid();
							String fencespid = geofence.getSpid();
							String fenceType = geofence.getFenceType();
							List<Point> points = geofence.getXyPoints();

							if (geofencestatusJson.containsKey(fenceid)) {
								fenceStatus = (HashMap<String,Object>) geofencestatusJson.get(fenceid);
							}

							String prev_ref_status = fenceStatus.containsKey("ref_status")
									? fenceStatus.get("ref_status").toString() : "";
							
							boolean isinside = getGeofenceService().isInside(fencespid, fenceType, points, x, y, bcnSpid);
							
							getCustomerUtils().logs(logenabled, classname, " Fence "+geofence.getName()+" isInside = "+isinside);
							
							fenceStatus = getGeofenceUtils().updateFenceStatus(tagid, fenceStatus, isinside, logenabled,timezone);
							
							String cur_ref_status = fenceStatus.containsKey("ref_status") ? fenceStatus.get("ref_status").toString() : "";
							
							String prev_entry_time = "";
							String cur_entry_time = "";
							
							if (fenceStatus.get("entry_time") != null) {
								prev_entry_time = fenceStatus.containsKey("entry_time")
										? (String)fenceStatus.get("entry_time") : beacon.getEntry_loc();
							}
							
							//if (fenceStatus.get("entry_time") != null) {
							//	cur_entry_time = fenceStatus.containsKey("entry_time") ? fenceStatus.get("entry_time").toString() : beacon.getEntry_loc();
							//}
							
							format.setTimeZone(timezone);
							cur_entry_time = format.format(new Date());

							getCustomerUtils().logs(logenabled, classname, "Geofence: prev_entry_time "
									+ prev_entry_time + " cur_entry_time=" + cur_entry_time + " tagid " + tagid);
							
							if (!cur_ref_status.equals(prev_ref_status)) {

								HashMap<String, String> rpMap = new HashMap<String, String>();

								rpMap.put("macaddr", tagid);
								rpMap.put("cid", cid);
								rpMap.put("assto", beacon.getAssignedTo());
								rpMap.put("tagtype", tagtype);
								rpMap.put("location_type", location_type);
								rpMap.put("sid", fencesid);
								rpMap.put("spid", fencespid);
								rpMap.put("fence", fenceid);
								rpMap.put("en_location", prev_entry_time);
								rpMap.put("date", cur_entry_time);
								rpMap.put("exitReason", "geofence_change");

								TrilaterationScheduledTask rtask = new TrilaterationScheduledTask();
								rtask.setReportFlag(1);
								rtask.setLog(logenabled);
								rtask.setTimeZone(timezone);
								rtask.setCid(cid);
								rtask.setReportMap(rpMap);
								rtask.fork();
								rpttasks.add(rtask);

								String venuename = "unknown";

								if (siteMap.containsKey(fencesid)) {
									venuename = siteMap.get(fencesid);
								} else {
									Site s = getSiteService().findById(fencesid);
									venuename = s.getUid();
								}

								Map<String,Object> parameters = new HashMap<String,Object>();
								parameters.put("beacon", beacon);
								parameters.put("geofence", geofence);
								parameters.put("alertMap", geofenceAlertMap);
								parameters.put("venuename", venuename);
								parameters.put("event", cur_ref_status);

								alertsJson = getGeofenceUtils().getAlertsJson(parameters,alertsJson,logenabled);
								getCustomerUtils().logs(logenabled, classname, "Geofence: Change in geofence for tag "+tagid+ " fencename = "+geofence.getName());
							}
							geofencestatusJson.put(fenceid, fenceStatus);
						}
						beacon.setGeofencestatus(geofencestatusJson);
					}
					
					reportBeacon.setCid(beacon.getCid());
					reportBeacon.setSid(beacon.getSid());
					reportBeacon.setSpid(beacon.getSpid());
					reportBeacon.setLocation(beacon.getLocation());
					reportBeacon.setReciverinfo(beacon.getReciverinfo());
					reportBeacon.setEntryFloor(beacon.getEntryFloor());
					reportBeacon.setEntry_loc(beacon.getEntry_loc());
					reportBeacon.setLastReportingTime(beacon.getLastReportingTime());
					reportBeacon.setLastSeen(beacon.getLastSeen());
					reportBeacon.setReciveralias(beacon.getReciveralias());
					reportBeacon.setGeofencestatus(beacon.getGeofencestatus());
					
					saveReportBeaconList.add(reportBeacon);
					saveBeaconList.add(beacon);
				}
				getBeaconService().save(saveBeaconList);
				getReportBeaconService().save(saveReportBeaconList);
				tagInactivityMail(cid,alertsJson);
			}
		}catch(Exception e) {
			LOG.info("iam computing task " +e.getMessage());
			e.printStackTrace();
		}
		long elapsed = System.currentTimeMillis() - stimer;
		getCustomerUtils().logs(logenabled, classname, "Compute elapsed time = "+elapsed);
		return tagcount;
	}

	/**
	 * This schedular takes care of 
	 * 		-reportprocessing for trilateration solution.
	 * 		-tagInactivity for trilateration and entry-exit solution.
	 * 		-geofence alert check for trilateration solution.
	 * 		-battery check for tags
	 */
	
	@Scheduled (fixedDelay=500)
	public void trilaterationTask() {
		
		
		if (!tritask_enable) {
			return;
		}
		
		try {
			
			long stimer = System.currentTimeMillis();
			String currentTime 	= batteryCheckDateFormat.format(new Date()).toString();
			if (currentTime.equals(SCHEDULER_TIME) && flag == false) {
				processingBattery();
				flag = true;
				Thread.sleep(60000);
				return;
			}
			if(!currentTime.equals(SCHEDULER_TIME) && flag == true){
				flag = false;
			} 
			Iterable<Customer> customerlist = getCustomerService().findBySolutionAndStatus(solution,"ACTIVE");
			List<TrilaterationScheduledTask> myRecursiveTask = new ArrayList<TrilaterationScheduledTask>();
			boolean enablelog = false;
			int taskCount = 0;
			int tagExecutorCount = 0;
			
			for (Customer cx : customerlist) {
				String cust_id = cx.getId();

				if(cx.getLogs() != null && cx.getLogs().equals("true")) {
					enablelog = true;
				}else{
					enablelog = false;
				}
				
				String customerName = cx.getCustomerName();
				String sol 			= cx.getVenueType();
				String zone			= cx.getTimezone();
				
				boolean entryExit = sol.equals("Patient-Tracker") ? true : false;

				/*
				 * follow trilateration path only for locatum solution
				 */
				if (!entryExit) {
					
					List<Portion> floorlist	= getPortionService().findByCid(cust_id);

					if (floorlist == null) {
						continue;
					}
					
					TrilaterationScheduledTask task = new TrilaterationScheduledTask ();
					task.setLog(enablelog);
					task.setTimeZone(getCustomerUtils().FetchTimeZone(zone));
					task.setCid(cust_id);
					task.setCustomerName(customerName);
					task.setReportFlag (0);
					task.setPortionlist(floorlist);
					myRecursiveTask.add(task);
					forkJoinPool.execute(task);
					taskCount++;
				} else {
					JSONObject alertsJson = new JSONObject();
					alertsJson.put("smsAlertJson", new JSONObject());
					alertsJson.put("mailAlertJson", new JSONObject());
					alertsJson.put("dashboardAlertJson", new JSONObject());
					tagInactivityMail(cust_id,alertsJson);
				}
			}
			tagExecutorCount = taskCount;
			taskCount = 0;
			do {
				TimeUnit.MILLISECONDS.sleep(500);
				
				if (!CollectionUtils.isEmpty(myRecursiveTask)) {
					if (myRecursiveTask.get(taskCount).isDone() 
							|| myRecursiveTask.get(taskCount).isCancelled()
							|| myRecursiveTask.get(taskCount).isCompletedAbnormally()
							|| myRecursiveTask.get(taskCount).isCompletedNormally()) {
						taskCount++;
					}
				}

				if (taskCount == tagExecutorCount) {
					break;
				}
			} while (tagExecutorCount >= taskCount);
			
			for (taskCount = 0; taskCount < tagExecutorCount; taskCount++) {
				if (!CollectionUtils.isEmpty(myRecursiveTask)) {
					myRecursiveTask.get(taskCount).join();
				}
			}
			long elapsed = System.currentTimeMillis() - stimer;
			getCustomerUtils().logs(enablelog,classname,"*** Shutdown NOW ***");
			getCustomerUtils().logs(enablelog,classname,"time elapsed = "+elapsed);
		} catch(Exception e) {
			LOG.info("iam forking task " +e.getMessage());
			e.printStackTrace();
		}
	}

	private final void tagInactivityMail(String cust_id, JSONObject alertJson) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					Customer cx = getCustomerService().findById(cust_id);
					
					JSONObject alertsJson = alertJson;
					
					long defaultInactiveTime  = 60;
					long time 				  = 0;
					
					String cid 			  = cx.getId();
					String sentMail 	  = "false";
					String status 		  = "checkedout";
					String ismailalert 	  = "true";
					String inactivityMail = cx.getInactivityMail();

					boolean logs 		  = true;
					boolean hasDataToSend = false;
					boolean needsMail 	  = false;
					boolean entryexit 	  = cx.getVenueType().equals("Patient-Tracker");
					
					List<Beacon> inactiveBeacons 		  = new ArrayList<Beacon>();
					List<Beacon> beaconlist 	 		  = null;
					List<UserAccount> useracclist 		  = getUserAccountService().findByCustomerIdAndIsMailAlert(cid,ismailalert);
					List<BeaconAlertData> beaconAlertData = getBeaconAlertDataService().findByCid(cid);
					
					int useracc_size 		= useracclist != null ? useracclist.size() : 0;
					String newline 			= System.getProperty("line.separator");
					StringBuilder mailBody 	= null;
					
					TimeZone zone 			= getCustomerUtils().FetchTimeZone(cx.getTimezone());
					format.setTimeZone(zone);
					
					final String date = format.format(new Date(System.currentTimeMillis()));
					
					if (useracc_size > 0 && inactivityMail != null && inactivityMail.equals("true")) {
						needsMail = true;
					}
					
					if (cx.getLogs() == null || cx.getLogs().equals("false")) {
						logs = false;
					}
					
					if (cx.getTagInact() != null) {
						defaultInactiveTime = (long) (Double.parseDouble(cx.getTagInact()) *60000);
					}
					
					time = System.currentTimeMillis() - defaultInactiveTime;
					
					inactiveBeacons = getBeaconService().findByCidStatusMailSentLastSeenBefore(cid, status, sentMail, time);
					
					if (inactiveBeacons != null && inactiveBeacons.size() > 0 ) {
						mailBody 	= getEmailTemplateService().buildInactivityTable();
						int i =1;
						beaconlist = new ArrayList<Beacon>();
						
						Map<String,Geofence> geofenceMap  = getGeofenceUtils().getGeofenceMap(cid,FENCE_STATE);
						
						Map<String,GeofenceAlert> geofenceAlertMap = getGeofenceAlertService().getFenceAlertMap(cid, FENCE_STATE);
						Map<String, String> siteMap = new HashMap<String, String>();
						
						for (Beacon beacon : inactiveBeacons) {
							
							String tagstate = beacon.getState() == null ? "" : beacon.getState();
							
							if(entryexit && !tagstate.equals("inactive")){
								continue;
							}
							
							if(!hasDataToSend){
								hasDataToSend = true;
							}
							String tagId 		= beacon.getMacaddr();
							String tagType 		= beacon.getTagType();
							String assignedTo 	= beacon.getAssignedTo();
							String floorName 	= "NA";
							String location 	= "NA";
							String lastseenDate = "-";
							String sid 		    = beacon.getSid();
							String spid 		= beacon.getSpid();
							String ruid 		= beacon.getReciverinfo();
							String entry_loc 	= beacon.getEntry_loc();
							String entry_floor  = beacon.getEntry_floor();
							long lastSeen  		= beacon.getLastSeen();
							
							/*
							if (beacon.getCid().equals("5998be75faf13e2ca463bbf1")) { // for Ruckus Demo
								String message = "[Locatum] " + beacon.getAssignedTo()+ " has exited the assigned location.";
								smsRestController.sendSMS(message, null);
							}
							*/

							if (needsMail) {
								
								if(lastSeen != 0) {
									lastseenDate = date;
								}
								
								if(beacon.getLocation() != null){
									floorName 	= beacon.getLocation().toUpperCase();
								}
								
								if(beacon.getReciveralias() != null){
									location 	= beacon.getReciveralias().toUpperCase();
								}
								
								mailBody.append("<tr>")
										.append("<td>" + i + "</td>")
										.append("<td  style=\"color:blue\">" + tagId + newline + "</td>")
										.append("<td  style=\"color:blue\">" + tagType + newline + "</td>")
										.append("<td  style=\"color:blue\">" + assignedTo + newline + "</td>")
										.append("<td  style=\"color:blue\">" + floorName + newline + "</td>")
										.append("<td  style=\"color:blue\">" + location + newline + "</td>")
										.append("<td  style=\"color:blue\">" + lastseenDate + newline + "</td>").append("</tr>");			
							
								beacon.setMailsent("true");
								i++;
							}

							if (!tagstate.equals("inactive")) {
								
								ReportBeacon reportBeacon = getReportBeaconService().findOneByMacaddr(tagId);
								
								
								if(reportBeacon != null) {
									reportBeacon.setState("inactive");
									//reportBeacon.setEntry_floor(null); // report calculation elapsed time zero fixes
									//reportBeacon.setEntry_loc(null);
									getReportBeaconService().save(reportBeacon);
								}
								
								//date = beacon.getLastReportingTime();
								
								beacon.setState("inactive");
								
								String location_type = "receiver";
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
								rpMap.put("en_location", entry_loc);
								rpMap.put("en_floor",  entry_floor);
								rpMap.put("date",      date);
								rpMap.put("assto",     assignedTo);
								rpMap.put("location_type", location_type);
								rpMap.put("exitReason", "receiver_inactive");

								reportProcess(rpMap,logs,isEodAlert);
								beacon.setExitTime(date);

								Map<String, Object> inactivitypop = new HashMap<String, Object>();
								inactivitypop.put("timestamp", 			System.currentTimeMillis());
								inactivitypop.put("assignedTo", 		assignedTo);
								inactivitypop.put("current_location", 	location);
								inactivitypop.put("lastSeenDate", 		lastSeen);
								inactivitypop.put("source", 			"inactive");

								getBeaconService().inactivityPopupMap.put(tagId, inactivitypop);

								JSONObject geofenceStatus = beacon.getGeofencestatus();
								
								if (geofenceStatus != null && !geofenceStatus.isEmpty()) {
									
									Set<String> keys = geofenceStatus.keySet();
									
									location_type = "geofence";
									
									for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
										String fenceid = (String) iterator.next();
								
										HashMap<String,Object> fenceStatus = (HashMap<String,Object>)geofenceStatus.get(fenceid);
										
										if (fenceStatus.isEmpty()) {
											continue;
										}
										
										String ref_status 	   = fenceStatus.containsKey("ref_status") ? fenceStatus.get("ref_status").toString() : "";
										
										String prev_entry_time = "";
										
										if (fenceStatus.get("entry_time") != null) {
											prev_entry_time = (String)fenceStatus.get("entry_time");
										} else {
											prev_entry_time =  beacon.getEntry_loc();
										}

										if (geofenceMap.containsKey(fenceid)) {
											
											Geofence geofence  = geofenceMap.get(fenceid);
											
											if (geofence != null) {
												
												String fencespid     = geofence.getSpid();
												String fencesid 	 = geofence.getSid();
												String venuename 	 = "unknown";

												if (siteMap.containsKey(fencesid)) {
													venuename = siteMap.get(fencesid);
												} else {
													Site site = getSiteService().findById(fencesid);
													if (site != null) {
														venuename = site.getUid();
													}
												}

												if(ref_status.equals("entry")) {
													
													rpMap = new HashMap<String, String>();

													rpMap.put("macaddr", tagId);
													rpMap.put("cid", cid);
													rpMap.put("assto", beacon.getAssignedTo());
													rpMap.put("tagtype", tagType);
													rpMap.put("location_type", location_type);
													rpMap.put("sid", fencesid);
													rpMap.put("spid", fencespid);
													rpMap.put("fence", fenceid);
													rpMap.put("en_location", prev_entry_time);
													rpMap.put("date", date);
													rpMap.put("exitReason", "geofence_inactive");

													reportProcess(rpMap, logs,isEodAlert);
													fenceStatus.put("ref_status", "exit");
													
													geofenceStatus.put(fenceid,fenceStatus);
													beacon.setGeofencestatus(geofenceStatus);
													
													Map<String,Object> parameters = new HashMap<String,Object>();
													parameters.put("beacon", beacon);
													parameters.put("geofence", geofence);
													parameters.put("alertMap", geofenceAlertMap);
													parameters.put("venuename", venuename);
													parameters.put("event", "exit");

													alertsJson = getGeofenceUtils().getAlertsJson(parameters,alertsJson,logs);
												}
											}
											
										}
									}
								}
							}
							beaconlist.add(beacon);	
						}

						if (beaconlist.size() >0) {
							mailBody.append("</table><br/>");
							getBeaconService().save(beaconlist);
						}
						
						if (needsMail && hasDataToSend) {
							getEmailService().sendMailToUsers(mailBody, useracclist);
						} else {
							getCustomerUtils().logs(logs, classname, cx.getCustomerName()+" needs mail = "+needsMail+" and has data to send = "+hasDataToSend);
						}
					}else{
						getCustomerUtils().logs(logs, classname, " No inactive tags for customer "+cx.getCustomerName());
					}


					if(alertJson != null && !alertJson.isEmpty()) {
						JSONObject dashboardAlertJson = (JSONObject)alertsJson.get("dashboardAlertJson");
						JSONObject mailAlertJson = (JSONObject)alertsJson.get("mailAlertJson");
						JSONObject smsAlertJson = (JSONObject)alertsJson.get("smsAlertJson");

						if (!dashboardAlertJson.isEmpty()) {
							geofenceUtils.sendGeofenceAlertDashBoard(cust_id, dashboardAlertJson, logs);
						}
						if (!mailAlertJson.isEmpty()) {
							geofenceUtils.sendGeoFenceAlertMail(cust_id, mailAlertJson, logs);
						}
						if (!smsAlertJson.isEmpty()) {
							geofenceUtils.sendGeoFenceAlertSms(cust_id, smsAlertJson, logs);
						}
					}

					if (beaconAlertData != null && beaconAlertData.size() > 0 && needsMail) {
						
						boolean allTagidsenabled   = false;
						boolean allplaceIdsenabled = false;
						hasDataToSend = false;
						String tagtype;
						String place;
						String fieldName = "";
						String activeMailSent = "true";

						List<Beacon> activeBeacons = null;

						JSONArray tagids;
						JSONArray placeIds;

						long duration = 0;
						long inactivityTime = 0;
						int i = 1;

						status = "checkedout";

						List<Map<String, Object>> result = null;
						Set<String> tags = new HashSet<String>();

						for (BeaconAlertData b : beaconAlertData) {

							tagtype  = b.getTagtype();
							tagids   = b.getTagids();
							place    = b.getPlace();
							placeIds = b.getPlaceIds();
							duration = (long) (b.getDuration() * 60000);

							inactiveBeacons = new ArrayList<Beacon>();
							
							if(place.equals("venue")){
								fieldName = "sid";
							}else if(place.equals("floor")){
								fieldName = "spid";
							}else if(place.equals("location")){
								fieldName = "location";
							}

							if (placeIds.size() == 1 && placeIds.get(0).equals("all")){
								allplaceIdsenabled = true;
							}

							if (tagids.size() == 1 && tagids.get(0).equals("all")){
								allTagidsenabled = true;
							}

							inactivityTime = System.currentTimeMillis() - duration;

							if (allTagidsenabled && allplaceIdsenabled) {

								beaconlist = getBeaconService().getSavedBeaconByCidTagTypeAndStatus(cid, tagtype, status);

							} else if (allplaceIdsenabled && !allTagidsenabled) {

								beaconlist = getBeaconService().findByCidTagTypeStatusAndTagIds(cid,tagtype,status,tagids);

							} else if (!allplaceIdsenabled && allTagidsenabled) {

								if (place.equals("venue")) {
									beaconlist = getBeaconService().findByCidTagTypeStatusAndNotSids(cid,tagtype,status,placeIds);
									activeBeacons = getBeaconService().findByCidTagTypeStatusSidsAndLastSeenAfterMailSent(cid,tagtype,status,placeIds,inactivityTime,activeMailSent);
								} else if (place.equals("floor")) {
									beaconlist = getBeaconService().findByCidTagTypeStatusAndNotSpids(cid, tagtype, status, placeIds);
									activeBeacons = getBeaconService().findByCidTagTypeStatusSpidsAndLastSeenAfterMailSent(cid,tagtype,status,placeIds,inactivityTime,activeMailSent);
								} else if (place.equals("location")) {
									beaconlist = getBeaconService().findByCidTagTypeStatusAndNotReceiverInfos(cid,tagtype,status,placeIds);
									activeBeacons = getBeaconService().findByCidTagTypeStatusReceiverInfosAndLastSeenAfterMailSent(cid,tagtype,status,placeIds,inactivityTime,activeMailSent);
								}
							} else if (!allplaceIdsenabled && !allTagidsenabled) {
								if (place.equals("venue")) {
									beaconlist = getBeaconService().findByCidTagTypeStatusTagIdsAndNotSids(cid,tagtype,status,tagids,placeIds);
									activeBeacons = getBeaconService().findByCidTagTypeStatusTagIdsSidsAndLastSeenAfterMailSent(cid,tagtype,status,tagids,placeIds,inactivityTime,activeMailSent);
								} else if (place.equals("floor")) {
									beaconlist = getBeaconService().findByCidTagTypeStatusTagIdsAndNotSpids(cid,tagtype,status,tagids,placeIds);
									activeBeacons = getBeaconService().findByCidTagTypeStatusTagIdsSpidsAndLastSeenAfterMailSent(cid,tagtype,status,tagids,placeIds,inactivityTime,activeMailSent);
								} else if (place.equals("location")) {
									beaconlist = getBeaconService().findByCidTagTypeStatusTagIdsAndNotReceiverInfos(cid,tagtype,status,tagids,placeIds);
									activeBeacons = getBeaconService().findByCidTagTypeStatusTagIdsReceiverInfosAndLastSeenAfterMailSent(cid,tagtype,status,tagids,placeIds,inactivityTime,activeMailSent);
								}
							}

							inactiveBeacons = new ArrayList<Beacon>();
							
							if (beaconlist != null && beaconlist.size() > 0) {
								
								inactivityTime  = format.parse(date).getTime() - duration;

								Map<String,String> portionMap  = new HashMap<String,String>();
								Map<String,String> receiverMap = new HashMap<String,String>();
								Map<String, Object> log 	   = null;
								
								Portion p 		= null;
								BeaconDevice bd = null;
								
								for(Beacon beacon : beaconlist){
									
									String tagid 	  	= beacon.getMacaddr();
									String tagType	 	= beacon.getTag_type();
									String assignedto 	= beacon.getAssignedTo();
									String floorname  	= beacon.getLocation()				   == null ? "NA" 	: beacon.getLocation();
									String location     = beacon.getReciveralias() 		   == null ? "NA" 	: beacon.getReciveralias();
									String lastSeenDate = beacon.getLastReportingTime() 	   == null ? "NA" 	: beacon.getLastReportingTime();
									String mail 		= beacon.getLocalInactivityMailSent() == null ? "false": beacon.getLocalInactivityMailSent();
									String sid 			= beacon.getSid()					   == null ? "NA" 	: beacon.getSid();
									String spid 		= beacon.getSpid()					   == null ? "NA" 	: beacon.getSpid();
									String uid			= beacon.getReciverinfo()			   == null ? "NA" 	: beacon.getReciverinfo();
									String timespent 	= "00:00:00";
									String cur_location = location;
									boolean addtag = true;
									
									if((place.equals("venue") && !sid.equals("NA") && placeIds.contains(sid))||
									   (place.equals("floor") && !spid.equals("NA") && placeIds.contains(spid))||
									   (place.equals("location") && !uid.equals("NA") && placeIds.contains(placeIds))){
										long elps = getCustomerUtils().calculateElapsedTime(format.parse(beacon.getEntry_loc()),format.parse(beacon.getLastReportingTime()));
										timespent = getTimeSpent(String.valueOf(elps));
									}else{

										String fsql = "index="+reportEventIndex + ",size=1,type=trilateration,query="+
													  "opcode:\"reports\" AND cid:" + cid +" AND tagid:\""+tagid+"\" ";
										
										if(!allplaceIdsenabled){
											fsql += "AND " + BuildFsqlOrCondition(placeIds, fieldName);
										}
										
										if(!place.equals("location")){
											fsql += " AND exit_floor:*" ;
										}
										
										fsql += " AND exit_loc:* AND location_type:receiver,sort=timestamp DESC|value(timestamp,Date,typecast=date);" + " value(tagid,tagid,null);"
												+ " value(assingedto,assingedto,null);value(spid,spid,null);value(location,location,null);"
												+ "value(entry_floor ,entry_floor,null);value(exit_floor,exit_floor,null);value(elapsed_floor,elapsed_floor,null);"
												+ "value(entry_loc ,entry_loc,null);value(exit_loc,exit_loc,null);value(elapsed_loc,elapsed_loc,null);"
												+ "|table,sort=Date:desc;";
										
										result = getFSqlRestController().query(fsql);
										
										if(result != null && !result.isEmpty()){
											
											log = result.get(0);
											
											lastSeenDate  = log.get("exit_loc").toString();
											
											long exittime = format.parse(lastSeenDate).getTime();
											
											if ((exittime < inactivityTime) && (mail== null || mail.equals("false"))) {
												
												spid = log.get("spid").toString();
												uid = log.get("location").toString();

												if(portionMap.containsKey(spid)){
													floorname = portionMap.get(spid);
												}else{
													 p = getPortionService().findById(spid);
													if (p != null) {
														floorname = p.getUid();
													}
													 portionMap.put(spid, floorname);
												}
												
												if (receiverMap.containsKey(uid)) {
													location = receiverMap.get(uid);
												} else {
													bd = getBeaconDeviceService().findByUidAndCid(uid, cid);
													if (bd != null) {
														location = bd.getName();
													}
													receiverMap.put(uid, location);
												}
												
												timespent = getTimeSpent(log.get("elapsed_loc").toString());
												
											} else {
												
												addtag = false;
												
												if ((exittime > inactivityTime) && mail.equals("true")) {
													beacon.setLocalInactivityMailSent("false");
													beacon.setAssginedLocationLastSeen(lastSeenDate);
													inactiveBeacons.add(beacon);
												}
											}
										}
									}
									
									if ((addtag && tags.contains(tagid)) || (addtag && mail != null && mail.equals("true"))) {
										addtag = false;
									} else if (addtag) {
										tags.add(tagid);
									}
									
									if (addtag) {
										
										if (!hasDataToSend || mailBody == null) {
											mailBody 	= getEmailTemplateService().buildBeaconAlertDataTable();
											hasDataToSend = true;
										}
										
										beacon.setLocalInactivityMailSent("true");
										beacon.setAssginedLocationLastSeen(lastSeenDate);
										inactiveBeacons.add(beacon);
										
										if (cur_location.equals(location)) {
											cur_location = "-";
										}

										mailBody.append("<tr>")
												.append("<td>" + i + "</td>")
												.append("<td  style=\"color:blue\">" + tagid + newline + "</td>")
												.append("<td  style=\"color:blue\">" + tagType + newline + "</td>")
												.append("<td  style=\"color:blue\">" + assignedto + newline + "</td>")
												.append("<td  style=\"color:blue\">" + floorname + newline + "</td>")
												.append("<td  style=\"color:blue\">" + location + newline + "</td>")
												.append("<td  style=\"color:blue\">" + lastSeenDate + newline + "</td>")
												.append("<td  style=\"color:blue\">" + timespent + newline + "</td>")
												.append("<td  style=\"color:blue\">" + cur_location + newline + "</td>")
												.append("</tr>");
									}
								}
								
								if (inactiveBeacons.size() > 0) {
									getBeaconService().save(inactiveBeacons);
								}
							}
							
							beaconlist = new ArrayList<Beacon>();
							if(activeBeacons != null && activeBeacons.size()>0){
								for(Beacon bcn: activeBeacons){
									bcn.setLocalInactivityMailSent("false");
									beaconlist.add(bcn);
								}
								getBeaconService().save(beaconlist);
							}
						}

						if (needsMail && hasDataToSend && mailBody != null) {
							mailBody.append("</table><br/></div><br/>");
							getEmailService().sendMailToUsers(mailBody, useracclist);
						} else {
							getCustomerUtils().logs(logs, classname, "NOT IN ASSIGNED LOCATION TAGS needsMail = "+needsMail+" has data to send ="+hasDataToSend);
						}
					}
				
					Collection<Beacon> inactiveBeacon = getBeaconService().getSavedBeaconByCidStateAndStatus(cid, "inactive", "checkedout");
					
					int curtInacTagsCount = 0;
					int prevInacTagCount  = cx.getTagAlertCount();
					
					if(inactiveBeacon != null && inactiveBeacon.size()>0){
						curtInacTagsCount = inactiveBeacon.size();
					}
					if (prevInacTagCount != curtInacTagsCount) {
						cx.setTagAlertCount(curtInacTagsCount);
						getCustomerService().save(cx);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private String BuildFsqlOrCondition(JSONArray placeIds, String fieldName) {
				if (placeIds.size() > 0) {
		    		StringBuilder sb = new StringBuilder(fieldName).append(":(");
		    		boolean isFirst = true;
		    		Iterator<String> iter = placeIds.iterator();
		    		String placeid;
					while (iter.hasNext()) {
						placeid = iter.next();
						if (isFirst) {
							isFirst = false;
						} else {
							sb.append(" OR ");
						}
						sb.append("\"" + placeid + "\"");
					}
					sb.append(")");

					return sb.toString();
				} else {
					return "";
				}
			}
			
			private String getTimeSpent(String elapsed) {
				String timespent = "-";
				int elps = Integer.parseInt(elapsed);
				if (elps != 0) {
					int hours = elps / 3600;
					int minutes = (elps % 3600) / 60;
					int seconds = (elps % 3600) % 60;
					timespent = String.format("%02d:%02d:%02d", hours, minutes, seconds);
				}
				return timespent;
			}
		});
		t.start();
	}

	public void reportProcess(HashMap<String, String> rpMap,boolean debuglog,boolean eod) {
		
		try {
			
			boolean isPush = false;
			
			String location_type = rpMap.getOrDefault("location_type","-");
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			String cid		  = rpMap.get("cid");
			String tagid      = rpMap.get("macaddr");
			String assignedto = rpMap.get("assto");
			String tagtype 	  = rpMap.get("tagtype");
			String exitTime   = rpMap.get("date");
			String exitReason = rpMap.get("exitReason");
			
			if(location_type.equals("geofence")) {
				
				String sid  = rpMap.get("sid");
				String spid  = rpMap.get("spid");
				String fence = rpMap.get("fence");
				String en_location = rpMap.get("en_location");
				
				Date entry_location = en_location == null ? format.parse(exitTime) : format.parse(en_location);
				Date exit_location  = format.parse(exitTime);
				long elapsed_loc	= getCustomerUtils().calculateElapsedTime(entry_location, exit_location);
				
				map.put("opcode",		"reports");
				map.put("tagid", 		tagid);
				map.put("tagtype", 		tagtype);
				map.put("assignedto",   assignedto);
				map.put("cid", 			cid);
				map.put("sid", 			sid);
				map.put("spid", 		spid);
				map.put("location",     fence);
				map.put("location_type", location_type);
				map.put("entry_loc", 	en_location);
				map.put("exit_loc", 	exitTime);
				map.put("elapsed_loc", 	elapsed_loc);
				map.put("exit_reason",  exitReason);
				
				isPush = true;
				
			} else {
				
				String sid        		= rpMap.get("prev_sid");
				String prev_spid 		= rpMap.get("prev_spid");
				String prev_rec 		= rpMap.get("prev_reuid");
				
				String cur_spid 		= rpMap.get("cur_spid");
				String cur_rec 			= rpMap.get("cur_reuid");
				
				String entry_floor 		= rpMap.get("en_floor");
				String entry_loc   		= rpMap.get("en_location");

				String exit_floor  		= exitTime;
				String exit_loc		    = exitTime;
				
				Date entry_flr = entry_floor == null ? format.parse(exitTime) : format.parse(entry_floor);
				Date exit_flr = format.parse(exit_floor);

				Date entry_location = entry_loc == null ? format.parse(exitTime) : format.parse(entry_loc);
				Date exit_location = format.parse(exit_loc);

				long elapsed_loc = getCustomerUtils().calculateElapsedTime(entry_location, exit_location);
				
				if (elapsed_loc <= 0) {
					LOG.info("*************** elapsed_loc ZERO ***********" +elapsed_loc);
					LOG.info("entry_location " + entry_location + " exit_location " +exit_location);
					LOG.info("entry_flr " + entry_flr + " exit_flr " +exit_flr);
					LOG.info("rpMap " +rpMap.toString());
					return;
				}

				getCustomerUtils().logs(this.logenabled, classname,"Report For Tag Pyaload "+rpMap.toString());
				
				
				map.put("opcode",		"reports");
				map.put("tagid", 		tagid);
				map.put("assingedto",   assignedto);
				map.put("tagtype", 		tagtype);
				map.put("cid", 			cid);
				map.put("sid", 			sid);
				map.put("spid", 		prev_spid);
				map.put("location", 	prev_rec);
				map.put("entry_loc", 	entry_loc);
				map.put("exit_loc", 	exit_loc);
				map.put("elapsed_loc", 	elapsed_loc);
				map.put("location_type", location_type);
				
				long elapsed_flr = getCustomerUtils().calculateElapsedTime(entry_flr, exit_flr);
				
				if (elapsed_flr <= 0) {
					LOG.info("*************** elapsed_flr ZERO ***********" +elapsed_loc);
					LOG.info("entry_location " + elapsed_flr + " tagid " +tagid);
					LOG.info("entry_flr " + entry_flr + " exit_flr " +exit_flr);
					LOG.info("rpMap " +rpMap.toString());
					return;
				}
				
				if (prev_spid != null && (!prev_spid.equalsIgnoreCase(cur_spid))) {
					map.put("entry_floor",  entry_floor);
					map.put("exit_floor", 	exit_floor);
					map.put("elapsed_floor",elapsed_flr);
					isPush = true;
				} else if(prev_rec != null && !prev_rec.equalsIgnoreCase(cur_rec)){
					map.put("location_type", location_type);
					isPush = true;
				} else if ("receiver_inactive".equals(exitReason)){
					isPush = true;
				}
				
				if (eod) { // End of day pushing tag Entry and exit time
					map.put("entry_floor",  entry_floor);
					map.put("exit_floor", 	exit_floor);
					map.put("elapsed_floor",elapsed_flr);
					map.put("eod", 			true);
					isPush = true;
				}
			}
			if(!map.isEmpty() && isPush) {
				getElasticService().post(reportEventIndex, "trilateration", map);
			}
		} catch(Exception e) {
			LOG.info("iam reportProcessor " +e.getMessage());
			e.printStackTrace();
		}
	}
	
	public CustomerService getCustomerService() {
		if (customerservice == null) {
			customerservice = Application.context.getBean(CustomerService.class);
		}
		return customerservice;
	}

	public PortionService getPortionService() {
		if (portionservice == null) {
			portionservice = Application.context.getBean(PortionService.class);
		}
		return portionservice;
	}

	public BeaconService getBeaconService() {
		if (beaconservice == null) {
			beaconservice = Application.context.getBean(BeaconService.class);
		}
		return beaconservice;
	}

	private FSqlRestController getFSqlRestController() {
		if (fsqlRestController == null && Application.context != null) {
			fsqlRestController = Application.context.getBean(FSqlRestController.class);
		}
		return fsqlRestController;
	}

	public ElasticService getElasticService() {
		if (elasticService == null) {
			elasticService = Application.context.getBean(ElasticService.class);
		}
		return elasticService;
	}

	public CCC getCCC() {
		if (_CCC == null) {
			_CCC = Application.context.getBean(CCC.class);
		}
		return _CCC;
	}
	
	public CustomerUtils getCustomerUtils() {
		if (customerUtils == null) {
			customerUtils = Application.context.getBean(CustomerUtils.class);
		}
		return customerUtils;
	}

	public BeaconDeviceService getBeaconDeviceService() {
		if (beaconDeviceService == null) {
			beaconDeviceService = Application.context.getBean(BeaconDeviceService.class);
		}
		return beaconDeviceService;
	}
	
	public UserAccountService getUserAccountService() {
		if (userAccountService == null) {
			userAccountService = Application.context.getBean(UserAccountService.class);
		}
		return userAccountService;
	}
	
	public JavaMailSender getJavaMailSender() {
		if (javaMailSender == null) {
			javaMailSender = Application.context.getBean(JavaMailSender.class);
		}
		return javaMailSender;
	}
	
	public BeaconAlertDataService getBeaconAlertDataService() {
		if (beaconAlertDataService == null) {
			beaconAlertDataService = Application.context.getBean(BeaconAlertDataService.class);
		}
		return beaconAlertDataService;
	}
	
	public NetworkConfRestController getNetworkConfRestController() {
		if (networkConfRestController == null) {
			networkConfRestController = Application.context.getBean(NetworkConfRestController.class);
		}
		return networkConfRestController;
	}
	
	private ReportBeaconService getReportBeaconService() {
		if (reportBeaconservice == null) {
			reportBeaconservice = Application.context.getBean(ReportBeaconService.class);
		}
		return reportBeaconservice;
	}

	private GeofenceService getGeofenceService() {
		if(geofenceService == null) {
			geofenceService = Application.context.getBean(GeofenceService.class);
		}
		return geofenceService;
	}
	
	private SiteService getSiteService() {
		if(siteService == null) {
			siteService = Application.context.getBean(SiteService.class);
		}
		return siteService;
	}

	private GeofenceUtils getGeofenceUtils() {
		if(geofenceUtils == null) {
			geofenceUtils = Application.context.getBean(GeofenceUtils.class);
		}
		return geofenceUtils;
	}
	
	private GeofenceAlertService getGeofenceAlertService() {
		if(geofenceAlertService == null) {
			geofenceAlertService = Application.context.getBean(GeofenceAlertService.class);
		}
		return geofenceAlertService;
	}
	
	private EmailService getEmailService() {
		if(emailService == null) {
			emailService = Application.context.getBean(EmailService.class);
		}
		return emailService;
	}
	
	private EmailTemplateService getEmailTemplateService() {
		if(emailTemplateService == null) {
			emailTemplateService = Application.context.getBean(EmailTemplateService.class);
		}
		return emailTemplateService;
	}
}