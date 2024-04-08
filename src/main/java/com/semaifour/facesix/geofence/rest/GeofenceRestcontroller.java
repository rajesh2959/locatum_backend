package com.semaifour.facesix.geofence.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.geofence.data.Geofence;
import com.semaifour.facesix.geofence.data.GeofenceAlert;
import com.semaifour.facesix.geofence.data.GeofenceAlertService;
import com.semaifour.facesix.geofence.data.GeofenceService;
import com.semaifour.facesix.util.SessionUtil;

@RestController
@RequestMapping("/rest/geofence")
@SuppressWarnings("unchecked")
public class GeofenceRestcontroller {

	static Logger LOG = LoggerFactory.getLogger(GeofenceRestcontroller.class.getName());

	@Autowired
	GeofenceService geofenceService;
	
	@Autowired
	GeofenceAlertService geofenceAlertService;

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public JSONObject view(@RequestParam(value = "id", required = true) String id,
			HttpServletRequest request, HttpServletResponse response) {
		JSONObject result = new JSONObject();
		try {
			
			Geofence geofence = geofenceService.findOneById(id);
			if(geofence == null) {
				return result;
			}
			
			List<String> alertlist = geofence.getAssociatedAlerts();
			
			JSONArray alertarray = new JSONArray();
			JSONObject alertjson = null;
			
			ObjectMapper mapper = new ObjectMapper();
			JSONParser parser = new JSONParser();
			
			String jsonString = mapper.writeValueAsString(geofence);
			result = (JSONObject) parser.parse(jsonString);
			List<String> removeAlert = new ArrayList<String>();
			for (String alertid : alertlist) {
				String alertname = null;
				alertjson = new JSONObject();
				GeofenceAlert alert = geofenceAlertService.findOneById(alertid);
				if (alert != null) {
					alertname = alert.getName();
					alertjson.put("id", alertid);
					alertjson.put("name", alertname);
					alertarray.add(alertjson);
				} else {
					removeAlert.add(alertid);
				}
			}
			if (removeAlert.size() > 0) {
				alertlist.removeAll(removeAlert);
				geofence.setAssociatedAlerts(alertlist);
				geofenceService.save(geofence);
			}
			result.put("associatedAlerts", alertarray);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONArray list(@RequestParam(value= "cid",required= false) String cid,
			   				   @RequestParam(value= "sid",required= false) String sid,
							   @RequestParam(value= "spid",required= false) String spid,
							   HttpServletRequest request, HttpServletResponse response) {
		
		JSONArray geofenceArray = new JSONArray();
		try {
			List<Geofence> geofencelist = null;
			Map<String, String> alertMap = new HashMap<String, String>();
			ObjectMapper mapper = new ObjectMapper();
			JSONParser parser = new JSONParser();

			if (StringUtils.isNotEmpty(spid)) {
				geofencelist = geofenceService.findBySpid(spid);
			} else if (StringUtils.isNotEmpty(sid)) {
				geofencelist = geofenceService.findBySid(sid);
			} else if (StringUtils.isNotEmpty(cid)) {
				geofencelist = geofenceService.findByCid(cid);
			}

			if (geofencelist != null && geofencelist.size() > 0) {
				for (Geofence geofence : geofencelist) {
					
					String jsonString = mapper.writeValueAsString(geofence);
					JSONObject fence = (JSONObject) parser.parse(jsonString);
					
					List<String> alertlist = geofence.getAssociatedAlerts();
					JSONArray alertarray = new JSONArray();
					JSONObject alertjson = null;
					
					for (String alertid : alertlist) {
						String alertname = null;
						alertjson = new JSONObject();
						if (alertMap.containsKey(alertid)) {
							alertname = String.valueOf(alertMap.get(alertid));
						} else {
							GeofenceAlert alert = geofenceAlertService.findOneById(alertid);
							if (alert != null) {
								alertname = alert.getName();
							} else {
								alertname = "Alert Deleted";
							}
							alertMap.put(alertid, alertname);
						}
						alertjson.put("id", alertid);
						alertjson.put("name", alertname);
						alertarray.add(alertjson);
					}
					fence.put("associatedAlerts", alertarray);
					geofenceArray.add(fence);

				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return geofenceArray;
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Restponse<String> save(@RequestBody Geofence geofenceData, HttpServletRequest request, HttpServletResponse response) {

		String message = "Geofence Saved Successfully !!";
		boolean success = true;
		int code = 200;
		try {
			String fenceName = geofenceData.getName();
			String spid = geofenceData.getSpid();
			boolean duplicate = false;
			List<Geofence> geofencelist = geofenceService.findBySpidAndName(spid,fenceName);
			if(geofencelist != null && geofencelist.size() > 0 ) {
				duplicate = true;
				if(geofencelist.size() == 1 && geofenceData.getId() != null) {
					Geofence geofence = geofencelist.get(0);
					if(geofence.getId().equals(geofenceData.getId())){
						duplicate = false;
					}
				}
			}
			if(!duplicate) {
				String status = geofenceData.getStatus();
				geofenceData.setStatus(status.toLowerCase());
				geofenceData = geofenceService.save(geofenceData);
			}else {
				success = false;
				code = 500;
				message = "Duplicate Geofence Name found !!!";
			}
		} catch (Exception e) {
			success = false;
			code = 500;
			message = "Failed to save the Geofence";
			e.printStackTrace();
		}
		return new Restponse<String>(success,code,message);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Restponse<String> delete(@RequestBody net.sf.json.JSONObject data, HttpServletRequest request, HttpServletResponse response) {
		
		net.sf.json.JSONArray geofenceDataIds = data.getJSONArray("ids");
		String message = "Geofence Deleted Successfully !!";
		boolean success = true;
		int code = 200;
		try {
			List<String> ids = new ArrayList<String>();
			ids.addAll(geofenceDataIds);
			List<Geofence> geofences = geofenceService.findByIds(ids);
			if(geofences.size() > 0) {
				geofenceService.delete(geofences);
			} else {
				message = "Geofence not found";
			}
		} catch (Exception e) {
			success = false;
			code = 500;
			message = "Failed to Delete the Geofence";
			e.printStackTrace();
		}
		return new Restponse<String>(success,code,message);
	}
	
	/**
	 *  
	 * @param fenceName
	 * @param spid
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/duplicateGeofenceName", method = RequestMethod.GET, params = {"fenceName!="})
	public Restponse<String> duplicateGeofenceName(
						@RequestParam("fenceName") String fenceName,
						@RequestParam("spid") String spid, 
						HttpServletRequest request) {
		
		String message 	= "UnAuthorized User";
		boolean flag 	= false;
		int code		= 401;
		
		LOG.info(" fenceName " + fenceName + " spid  " + spid);
		
		try {
			
			if (SessionUtil.isAuthorized(request.getSession())) {
				
				fenceName = StringUtils.trimToEmpty(fenceName);
				
				List<Geofence> geofence = geofenceService.findByNameIgnoreCase(fenceName);
				
				LOG.info(" geofence " + geofence);
			
				if (geofence != null && geofence.size() > 0) {
					
					Geofence fence 	= geofence.get(0);
					String dbSPid   = (fence.getSpid() == null) ? "NA" : fence.getSpid();
					String name 	= StringUtils.trimToEmpty(fence.getName());
					
					if (name.equalsIgnoreCase(fenceName) && spid.equals(dbSPid)) {
							message = "duplicate";
							flag 	= false;
							code    = 400;
					} else {
						flag 	= true;
						message = "new";
						code    = 200;
					}
				} else {
					flag 	= true;
					message = "new";
					code    = 200;
				}
			}
		} catch (Exception e) {
			message = "Error " + e.getMessage();
			flag 	= false;
			code	= 500;
			e.printStackTrace();
		}
		
		return new Restponse<String>(flag, code, message);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/filter/list", method = RequestMethod.POST)
	public JSONArray list(@RequestParam(value= "cid",required = true) String cid,
			@RequestParam(value = "sid", required = false) List<String> sid,
			@RequestParam(value = "spid", required = false) List<String> spid,
			@RequestParam(value = "status", required = true) String status) {
		
		JSONArray geofenceArray = new JSONArray();
		List<Geofence> geofenceList = null;
		if (spid != null && spid.size() > 0) {
			geofenceList = geofenceService.findBySpidInAndStatus(spid,status);
		} else if (sid != null && sid.size() > 0) {
			geofenceList = geofenceService.findBySidInAndStatus(sid,status);
		} else {
			geofenceList = geofenceService.findByCidAndStatus(cid,status);
		}

		JSONObject json = null;
		for (Geofence geofence : geofenceList) {
			json = new JSONObject();
			json.put("id", geofence.getId());
			json.put("name", geofence.getName());
			geofenceArray.add(json);
		}
		return geofenceArray;
	}
}
