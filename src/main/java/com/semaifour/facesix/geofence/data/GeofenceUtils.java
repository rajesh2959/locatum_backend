package com.semaifour.facesix.geofence.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.spring.CCC;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.EmailService;
import com.semaifour.facesix.util.EmailTemplateService;

@Service
public class GeofenceUtils {
	
	private static String classname	= GeofenceUtils.class.getName();
	static Logger LOG 				= LoggerFactory.getLogger(classname);
	
	@Autowired
	private GeofenceService geofenceService;
	
	@Autowired
	private CustomerUtils customerUtils;
	
	@Autowired
	private UserAccountService userAccountService;
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private CCC _CCC;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private EmailTemplateService emailTemplateService;
	
	DateFormat format 				  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private String dashboardAlertIndex = "dashboard-alert-event";
	
	@PostConstruct
	private void init() {
		dashboardAlertIndex = _CCC.properties.getProperty("dashboard.alert.table", dashboardAlertIndex);
	}
	
	/*
	 * Consistency parameters for geofence alert triggers
	 */
	static final int ENTRY_COUNT = 3;
	static final int EXIT_COUNT  = 3;
	
	static final int STATE_RESET_THRESHOLD 	= 30000; // 30s

	/*public boolean shouldPerformAction(String triggertype, boolean isinside,long count, int entryCount, int exitCount) {
		boolean performaction = false;
		switch (triggertype) {
		case "entry":
			if (isinside && ENTRY_COUNT == count) {
				performaction = isinside;
			} 
			break;
		case "exit":
			if (!isinside && EXIT_COUNT == count) {
				performaction = !isinside;
			} 
			break;
		case "both":
			if ((isinside && ENTRY_COUNT == count) || (!isinside && EXIT_COUNT == count)) {
				performaction = !performaction;
			} 
		}
		return false;
	}*/
	
	/**
	 * 
	 * @param cid
	 * @param status
	 * @return
	 */
	
	public Map<String, Geofence> getGeofenceMap(String cid, String status) {
		Map<String, Geofence> geofenceMap = new HashMap<String,Geofence>();
		List<Geofence> fencelist = geofenceService.findByCidAndStatus(cid, status);
		if(fencelist != null && !fencelist.isEmpty()) {
			for(Geofence fence : fencelist) {
				geofenceMap.put(fence.getId(), fence);
			}
		}
		return geofenceMap;
	}
	
	public boolean setRefStatus(boolean isinside ,String count_status,long count){
		boolean setRefStatus = false;
		if(isinside && count_status.equals("entry") && ENTRY_COUNT == count) {
			setRefStatus = true;
		} else if((!isinside) && count_status.equals("exit") && EXIT_COUNT == count){
			setRefStatus = true;
		}
		return setRefStatus;
	}

	public JSONObject updateFenceStatus(String tagid,HashMap<String,Object> fenceStatus, boolean isinside, boolean logenabled,TimeZone timeZone) {
		
		String currentState = isinside ? "entry" : "exit";
		
		String ref_status = fenceStatus.containsKey("ref_status") ? fenceStatus.get("ref_status").toString() : "";

		String count_status = fenceStatus.containsKey("count_status") ? fenceStatus.get("count_status").toString() : "";

		long count = 0;
		if (fenceStatus.containsKey("count")) {
			String strCount = fenceStatus.get("count").toString();
			count = Long.valueOf(strCount);
		}
		long count_timestamp = 0;
		if (fenceStatus.containsKey("count_timestamp")) {
			String strCount_timestamp = fenceStatus.get("count_timestamp").toString();
			count_timestamp = Long.valueOf(strCount_timestamp);
		}
		
		long current_time =System.currentTimeMillis();
		
		if (!currentState.equals(ref_status) && currentState.equals(count_status) 
				&& (current_time - count_timestamp) < STATE_RESET_THRESHOLD) {
			boolean performaction = setRefStatus(isinside, count_status, count);
			
			if(performaction) {
				ref_status = currentState;
			}
			
			// used to store the entry time,so that it can be retrieved while processing exit time
			
			if(performaction && isinside) {
				format.setTimeZone(timeZone);
				String strNowDate = format.format(new Date());
				fenceStatus.put("entry_time", strNowDate);
			} 
			
			customerUtils.logs(logenabled, classname,
					" Geofence performaction  " + performaction + " isinside = " + isinside );
			
		} else if (!currentState.equals(count_status) || (current_time - count_timestamp) >= STATE_RESET_THRESHOLD) {
			count_status = currentState;
			count = 1;
		}
		
		if(currentState.equals(count_status)) {
			count++;
		}

		JSONObject json = new JSONObject();
		json.put("ref_status", ref_status);
		json.put("count", count);
		json.put("count_status", count_status);
		json.put("count_timestamp", current_time);
		json.put("entry_time", fenceStatus.get("entry_time"));
		
		
		customerUtils.logs(logenabled, classname, " Geofence json for tagid "+tagid+" = "+json);
		
		return json;
	}
	
	public JSONObject getAlertsJson(Map<String,Object> paramenters, JSONObject alertsJson, boolean logenabled) {
		
		JSONObject smsAlertJson = (JSONObject)alertsJson.get("smsAlertJson");
		JSONObject mailAlertJson = (JSONObject)alertsJson.get("mailAlertJson");
		JSONObject dashboardAlertJson = (JSONObject)alertsJson.get("dashboardAlertJson");
		
		Beacon beacon 		= (Beacon)paramenters.get("beacon");
		Geofence geofence   = (Geofence)paramenters.get("geofence");
		
		Map<String, GeofenceAlert> geofenceAlertMap = (Map<String, GeofenceAlert>) paramenters.get("alertMap");
		String event 	 = (String)paramenters.get("event");
		String venuename = (String)paramenters.get("venuename");
		
		List<String> alertlist = geofence.getAssociatedAlerts();
		String triggertime = beacon.getLastReportingTime();
		String floorname = beacon.getLocation();
		String assignedto = beacon.getAssignedTo();
		String tagid = beacon.getMacaddr();
		String tagtype = beacon.getTagType();
		
		JSONObject alertJson = null;
		String fenceName = geofence.getName();
		JSONArray fenceAlertList = null;
		
		String cid = geofence.getCid();
		String sid = geofence.getSid();
		String spid = geofence.getSpid();

		for (String alertId : alertlist) {

			if (geofenceAlertMap.containsKey(alertId)) {
				GeofenceAlert alert = geofenceAlertMap.get(alertId);
				String triggertype = alert.getTriggerType();
				List<String> associations = alert.getAssociations();
				if ((associations.contains(tagid) || associations.contains(tagtype))
						&& (triggertype.equals(event) || triggertype.equals("both"))) {

					List<String> channel = alert.getChannel();
					String alertname = alert.getName();
					
					customerUtils.logs(logenabled, classname, " Geofence alert for tagid " + tagid + " geofence = "
							+ fenceName + " alertName = " + alertname + " channel = " + channel);

					alertJson = new JSONObject();
					alertJson.put("tagid", tagid);
					alertJson.put("assignedto", assignedto);
					alertJson.put("floorname", floorname);
					alertJson.put("gName", fenceName);
					alertJson.put("triggertime", triggertime);
					alertJson.put("event", event);
					alertJson.put("alertname", alertname);
					alertJson.put("venuename", venuename);
					alertJson.put("cid", cid);
					alertJson.put("sid", sid);
					alertJson.put("spid", spid);

					if (channel.contains("sms")) {
						fenceAlertList = new JSONArray();
						if (smsAlertJson.containsKey(tagid)) {
							fenceAlertList = (JSONArray)smsAlertJson.get(tagid);
						}
						fenceAlertList.add(alertJson);
						smsAlertJson.put(tagid, fenceAlertList);
					}

					if (channel.contains("mail")) {
						fenceAlertList = new JSONArray();
						if (mailAlertJson.containsKey(tagid)) {
							fenceAlertList =(JSONArray) mailAlertJson.get(tagid);
						}
						fenceAlertList.add(alertJson);
						mailAlertJson.put(tagid, fenceAlertList);
					}

					if (channel.contains("dashboard-alert")) {
						fenceAlertList = new JSONArray();
						if (dashboardAlertJson.containsKey(tagid)) {
							fenceAlertList =(JSONArray) dashboardAlertJson.get(tagid);
						}
						fenceAlertList.add(alertJson);
						dashboardAlertJson.put(tagid, fenceAlertList);
					}
				}
			}
		}
		alertsJson.put("smsAlertJson",smsAlertJson);
		alertsJson.put("mailAlertJson",mailAlertJson);
		alertsJson.put("dashboardAlertJson",dashboardAlertJson);
		
		return alertsJson;
	}
	
	
	/**
	 * Posts geofence dashboard alert.
	 * @param custId - Id of the customer
	 * @param dashboardAlertJson - Triggered dashboard alert content
	 * @param log - debug log status
	 */
	
	public void sendGeofenceAlertDashBoard(String custId, JSONObject dashboardAlertJson, boolean log) {
		
		if(dashboardAlertJson == null || dashboardAlertJson.isEmpty()) {
			return;
		}
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for (Object key : dashboardAlertJson.keySet()) {
						String fenceid = (String) key;
						JSONArray alertlist = (JSONArray)dashboardAlertJson.get(fenceid);
						Iterator<JSONObject> iter = alertlist.iterator();
						while (iter.hasNext()) {
							Map<String, Object> resultMap = new HashMap<String, Object>();
							ObjectMapper mapperObj = new ObjectMapper();
							JSONObject alert = iter.next();
							resultMap = mapperObj.readValue(alert.toString(), new TypeReference<HashMap<String, Object>>() {});
							resultMap.remove("xyPoints");
							getElasticService().post(dashboardAlertIndex, "alert", resultMap);
						}
					}
				} catch (Exception e) {
					LOG.info("iam sendGeofenceAlertDashBoard " +e.getMessage());
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	/**
	 * Sends geofence alert mail to users who have enabled email option.
	 * @param custId - Id of the customer
	 * @param mailJson - Triggered mail alert content
	 * @param log - debug log status
	 */
	public void sendGeoFenceAlertMail(String custId, JSONObject mailJson, boolean log) {
		
		if(mailJson == null || mailJson.isEmpty()) {
			return;
		}
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String ismailalert = "true";
					List<UserAccount> useracclist = getUserAccountService().findByCustomerIdAndIsMailAlert(custId, ismailalert);

					boolean needsMail = useracclist != null && useracclist.size() > 0;
					String newline = System.getProperty("line.separator");
					if (needsMail) {
						StringBuilder mailbody = emailTemplateService.buildGeoFenceTable();
						int sno = 1;
						String tagid, assignedto, floor, geofence, alertname, event, triggertime, venuename;
						for (Object key : mailJson.keySet()) {
							String fenceid = (String) key;
							JSONArray alertlist = (JSONArray)mailJson.get(fenceid);
							Iterator<JSONObject> iter = alertlist.iterator();
							while (iter.hasNext()) {
								JSONObject alert = iter.next();
								tagid = alert.get("tagid").toString();
								assignedto = alert.get("assignedto").toString();
								floor = alert.get("floorname").toString();
								geofence = alert.get("gName").toString();
								event = alert.get("event").toString();
								alertname = alert.get("alertname").toString();
								triggertime = alert.get("triggertime").toString();
								venuename = alert.get("venuename").toString();

								mailbody.append("<tr>").append("<td>" + sno + "</td>")
										.append("<td  style=\"color:blue\">" + tagid + newline + "</td>")
										.append("<td  style=\"color:blue\">" + assignedto + newline + "</td>")
										.append("<td  style=\"color:blue\">" + venuename + newline + "</td>")
										.append("<td  style=\"color:blue\">" + floor + newline + "</td>")
										.append("<td  style=\"color:blue\">" + geofence + newline + "</td>")
										.append("<td  style=\"color:blue\">" + event + newline + "</td>")
										.append("<td  style=\"color:blue\">" + alertname + newline + "</td>")
										.append("<td  style=\"color:blue\">" + triggertime + newline + "</td>")
										.append("</tr>");
								sno++;
							}
						}
						mailbody.append("</table><br/>");
						emailService.sendMailToUsers(mailbody, useracclist);
					}
				} catch(Exception e) {
					LOG.info("iam sendGeoFenceAlertMail " +e.getMessage());
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void sendGeoFenceAlertSms(String custId, JSONObject smsJson, boolean log) {
		if(smsJson == null || smsJson.isEmpty()) {
			return;
		}
	}

	public UserAccountService getUserAccountService() {
		if (userAccountService == null) {
			userAccountService = Application.context.getBean(UserAccountService.class);
		}
		return userAccountService;
	}
	
	public ElasticService getElasticService() {
		if (elasticService == null) {
			elasticService = Application.context.getBean(ElasticService.class);
		}
		return elasticService;
	}
}
