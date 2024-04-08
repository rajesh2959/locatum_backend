package com.semaifour.facesix.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.geofence.data.GeofenceAlertService;
import com.semaifour.facesix.geofence.data.GeofenceService;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.util.CustomerUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/rest/locatum/test")
public class Test {

	static Logger LOG = LoggerFactory.getLogger(Test.class.getName());
	
	@Autowired
	BeaconService beaconService;

	@Autowired
	GeofenceService geofenceService;
	
	@Autowired
	GeofenceAlertService geofenceAlertService;
	
	@Autowired
	MongoOperations mongoOperations;
	
	@Autowired
	FSqlRestController 		fsqlRestController;
	
	@Autowired
	ElasticService elasticService;
	
	@Autowired
	ElasticsearchTemplate elasticsearchTemplate;
	
	@Autowired
	CustomerUtils customerUtils;
	
	/**
	 * @param cid
	 * @param source - Generate mock data for inactivity / geoFence
	 * @param sid
	 * @param spid
	 * @param tagid
	 * @return
	 * 
	 * 		This function generates mock data for inactivity pop up displayed in
	 *         Dashboard.
	 */
	@RequestMapping(value = "/inactivity_pop_up", method = RequestMethod.GET)
	public ConcurrentHashMap<String, Map<String, Object>> tagdashboardInacPop(
			@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "source", required = true) String source,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "tagid", required = false) String tagid) {

		if (!source.equals("inactive") && !source.equals("geoFence")) {
			source = "inactive";
		}

		List<Beacon> beaconlist = new ArrayList<Beacon>();
		Map<String, Object> inactivitypop = null;
		String status = "checkedout";
		if (StringUtils.isNotEmpty(tagid)) {
			Beacon bcn = beaconService.findOneByMacaddr(tagid);
			if (bcn != null) {
				beaconlist.add(bcn);
			}
		} else if (StringUtils.isNotEmpty(spid)) {
			beaconlist = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
		} else if (StringUtils.isNotEmpty(sid)) {
			beaconlist = beaconService.getSavedBeaconBySidAndStatus(sid, status);
		} else {
			beaconlist = (List<Beacon>) beaconService.getSavedBeaconByCidAndStatus(cid, status);
		}

		if (beaconlist.size() > 0) {
			for (Beacon bcn : beaconlist) {
				tagid = bcn.getMacaddr();
				inactivitypop = new HashMap<String, Object>();
				inactivitypop.put("timestamp", System.currentTimeMillis());
				inactivitypop.put("assignedTo", bcn.getAssignedTo());
				inactivitypop.put("current_location", bcn.getReciverinfo());
				inactivitypop.put("source", source);
				inactivitypop.put("lastSeenDate", System.currentTimeMillis() - 3000);
				beaconService.inactivityPopupMap.put(tagid, inactivitypop);
			}
		}

		return beaconService.inactivityPopupMap;
	}

	@RequestMapping(value = "/geofence/polygon")
	public boolean testPolygon() {
		boolean result = false;
		Point[] latlonpoints = new Point[4];
		latlonpoints[0] = new Point(0, 0);
		latlonpoints[1] = new Point(0, 5);
		latlonpoints[2] = new Point(5, 5);
		latlonpoints[3] = new Point(5, 0);

		double lat = 2, lon = 1;

		result = geofenceService.isInside("1","rectangle",Arrays.asList(latlonpoints), lat, lon,"1");
		return result;
	}

	@RequestMapping(value = "/geofence/circle")
	public boolean testcircle() {
		boolean result = false;
		Point[] latlonpoints = new Point[2];
		latlonpoints[0] = new Point(0, 0);
		latlonpoints[1] = new Point(5, 0);

		double lat = 5, lon = 0;

		result = geofenceService.isInside("1","circle",Arrays.asList(latlonpoints), lat, lon,"1");
		return result;
	}
	
	@RequestMapping(value="/lookup")
	private net.sf.json.JSONObject getFenceAlerts(@RequestParam(value = "cid", required = true) String id) {
		net.sf.json.JSONObject associations = new net.sf.json.JSONObject();
		try{
			LookupOperation lookupOperation = LookupOperation.newLookup().
					from("geofence").
					localField("pkid").
					foreignField("associatedAlerts").
					as("geo");

			UnwindOperation unwindgeo = Aggregation.unwind("geo");
			UnwindOperation unwindassoc = Aggregation.unwind("associations");

			ProjectionOperation project = Aggregation.project("pkid", "triggerType", "cid", "name", "status", "category", "channel","associations")
					.andExclude("_id")
					.and("$geo.pkid").as("fenceid")
					.and("$geo.name").as("gName")
					.and("$geo.fenceType").as("fenceType")
					.and("$geo.sid").as("sid")
					.and("$geo.spid").as("spid")
					.and("$geo.xyPoints").as("xyPoints")
					.and("$geo.status").as("gStatus");
			
			Criteria cr1= Criteria.where("status").is("enabled");
			Criteria cr2 = Criteria.where("cid").is(id);
			Criteria cr3 = Criteria.where("geo.status").is("enabled");
			Criteria cr4 = Criteria.where("geo.cid").is(id);
			
			cr1.andOperator(cr2);
			cr3.andOperator(cr4);
			
			AggregationOperation match1 = Aggregation.match(cr1);
			AggregationOperation match2 = Aggregation.match(cr3);
			
			SortOperation sort = Aggregation.sort(Direction.ASC, "name");
			
			Aggregation aggregation = Aggregation.newAggregation(lookupOperation, match1, unwindgeo, match2, project, unwindassoc,sort);

			List<BasicDBObject> results = mongoOperations.aggregate(aggregation, "geofenceAlert", BasicDBObject.class).getMappedResults();
			
			if (results != null && results.size() > 0) {
				String association = null;
				List<BasicDBObject> alertObject = null;
				for (BasicDBObject res : results) {
					association = null;
					alertObject = new ArrayList<BasicDBObject>();
					association = res.getString("associations");
					if (associations.containsKey(association)) {
						alertObject.addAll(associations.getJSONArray(association));
					}
					alertObject.add(res);
					associations.put(association, alertObject);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return associations;
	}

	@RequestMapping("/alert/geofence")
	public JSONArray geofence(
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			HttpServletRequest request) {
		JSONArray alert = new JSONArray();
		int queryTimeInHours = 1;
		try {
			String dashboardAlertIndex = "dashboard-alert-event";
			String fsql = "index=" + dashboardAlertIndex + ",sort=timestamp desc,size=10,type=alert,query=timestamp:>now-"+queryTimeInHours+"h";
			
			if(!StringUtils.isEmpty(cid)) {
				fsql+= " AND cid:"+cid;
			}
			if(!StringUtils.isEmpty(sid)) {
				fsql+= " AND sid:"+sid;
			}
			if(!StringUtils.isEmpty(spid)) {
				fsql+=" AND spid:"+spid;
			}
			
			fsql += "|value(tagid,tagid,NA);value(assignedto,assignedto,NA);value(floorname,floorname,NA);value(gName,gName, NA);"
					+ "value(event,event, NA);value(alertname,alertname,NA);value(triggertime,triggertime,NA)|table;";

		  List<Map<String, Object>> logs = fsqlRestController.query(fsql);
		  
		  if(logs != null && logs.size() > 0) {
			  String type = "Geofence";
			  for(Map<String,Object> log:logs) {
				  String event = log.get("event").toString();
				  String assignedto = log.get("assignedto").toString();
				  String fencename = log.get("gName").toString();
				  String triggertime = log.get("triggertime").toString();
				  
				  if(event.equals("entry")) {
					  event = "entered";
				  }else {
					  event = "exited";
				  }
				  String text = assignedto+" "+event+" "+fencename+" at "+triggertime;
				  JSONObject object = new JSONObject();
					object.put("type", 	type);
					object.put("text",	text);
					alert.add(object);
			  }
		  }
		}catch(Exception e) {
			LOG.info("exception caught while quering ");
		}
		return alert;
	}
	@RequestMapping("/bulk/post")
	public void bulkpost() {
		String indexname = "test_index";
		String type = "test";
		Map<String,Object> docMap = null;
		List<Map<String,Object>> docList = new ArrayList<Map<String,Object>>();
		for(int i = 1;i<10;i++) {
			docMap = new HashMap<String,Object>();
			docMap.put("i", i);
			docList.add(docMap);
		}
		elasticService.postList(indexname, type, docList);
	}
	
	@RequestMapping("/get/index")
	public List<Map<String,Object>> get() {
		String indexname = "test_index";
		String type = "test";
		int queryTimeInHours = 30;
		String fsql = "index=" + indexname + ",sort=timestamp desc,size=10,type="+type+",query=timestamp:>now-1h";
		fsql += "|value(i,i,NA);value(timestamp,timestamp,NA)|table;";
		return fsqlRestController.query(fsql);
	}
	
	/*
	 * Test progsets query execution
	 */
	@RequestMapping("/query")
	public JSONObject query(){
		
		JSONObject query_result = null;
		 
		try {
			
			String fsql = "";
			
			fsql =  "viewName= ielastic?index=facesix-int-beacon-event/trilateration\n" + 
					"viewName= ssql?sql=SELECT viewName.spid,SUM(viewName.elapsed_loc) from viewName where viewName.opcode=\"reports\" GROUP BY viewName.spid\n" + 
					"return?view=viewName&as=map\n" + 
					"close";
			
			  LOG.info("fsql " +fsql);
			  
			  query_result = customerUtils.progset_query(fsql);
		} catch (Exception e) {
			LOG.warn("Error querying data for ssql "+e);
		}
		return query_result;

	}
}
