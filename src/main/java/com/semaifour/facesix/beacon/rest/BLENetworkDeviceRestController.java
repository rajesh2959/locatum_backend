package com.semaifour.facesix.beacon.rest;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.Font;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.outsource.OutsourceRestController;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.rest.NetworkDeviceRestController;
import com.semaifour.facesix.session.SessionCache;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

/**
 * 
 * Rest BLE Device Controller handles all rest calls for network configuration
 * 
 * @author mjs
 *
 */
@Controller
@RestController
@RequestMapping("/rest/beacon/ble/networkdevice")
public class BLENetworkDeviceRestController extends WebController {
	
	static Logger LOG = LoggerFactory.getLogger(BLENetworkDeviceRestController.class.getName());
	static Font smallBold 	= new Font(Font.FontFamily.TIMES_ROMAN, 12, 	Font.BOLD);
    static Font catFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 18, 	Font.BOLD);
    static Font redFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 12, 	Font.NORMAL);
    static Font subFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 16,     Font.BOLD);
    static boolean ThreadTobeStarted = false;
    DateFormat parse 			   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    DateFormat format 			   = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    
	
	@Autowired
	BeaconService 	beaconService;
	
	@Autowired
	SiteService siteService;
	
	@Autowired
	PortionService portionService;	
	   
	@Autowired
	FSqlRestController 		fsqlRestController;
	
	@Autowired
	BeaconDeviceService beacondeviceService;
	
	@Autowired
	SessionCache sessionCache;

	@Autowired
	ClientDeviceService clientService;
	
	@Autowired
	GeoFinderRestController  geoFinderRestController;
	
	@Autowired
	NetworkConfRestController netRestController;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	CustomerUtils customerUtils;
	
	@Autowired
	ElasticService elasticService;
	
	@Autowired
	NetworkDeviceRestController networkDeviceRestController;
	
	@Autowired
	DeviceService deviceService;
	
	@Autowired
	private OutsourceRestController outsourceRestController;
	
	String 	device_history_event = "device-history-event";
	String dashboardAlertIndex = "dashboard-alert-event";
	String sosAlertIndex	= "sos-alert-event";
	String reportIndex = "facesix-int-beacon-event";
	private String indexname = "facesix*";

	DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	TimeZone timezone     		   = null;
		
	@PostConstruct
	public void init() {
		indexname 		= _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
		device_history_event = _CCC.properties.getProperty("facesix.device.event.history.table",device_history_event);
		reportIndex = _CCC.properties.getProperty("facesix.data.beacon.trilateration.table", reportIndex);
		dashboardAlertIndex = _CCC.properties.getProperty("dashboard.alert.table",dashboardAlertIndex);
		sosAlertIndex = _CCC.properties.getProperty("sos.alert.table",sosAlertIndex);
	}
	
	public static String buildBeaconDeviceArrayCondition(List<BeaconDevice> list, String fieldname) {
		if (list.size() > 0) {
    		StringBuilder sb = new StringBuilder(fieldname).append(":(");
    		boolean isFirst = true;
    		for (BeaconDevice beacon : list) {
	    			if (isFirst) {
	    				isFirst = false;
	    			} else {
	    				sb.append(" OR ");
	    			}
	    			sb.append("\"").append(beacon.getUid()).append("\"");
    		}
    		sb.append(")");
    		return sb.toString();
		} else {
			return "";
		}
	}
	
    @RequestMapping(value = "/peercount", method = RequestMethod.GET)
    public  int peercount(@RequestParam(value="spid", required=false)String spid, 
    					  @RequestParam(value="cid", required=false)String cid) {
    
    	int device_count = 0;

		String state = Beacon.STATE.active.name();
		String status = Beacon.STATUS.checkedout.name();
		List<Beacon> beacon = beaconService.getSavedBeaconByCidSpidStateAndStatus(cid, spid, state, status);
		if (beacon != null) {
			device_count = beacon.size();
		}
		
		return device_count;
    }
    
    @RequestMapping(value = "/rxtx", method = RequestMethod.GET)
    public  List<Map<String, Object>> rxtx(@RequestParam(value="sid",  required=false)  String sid, 
    								 	   @RequestParam(value="spid", required=false)  String spid,
    								 	   @RequestParam(value="swid", required=false)  String swid,
    								 	   @RequestParam(value="uid",  required=false)  String uid,
    								 	   @RequestParam(value="time", required=false) String time,
    								 	   @RequestParam(value="place", required=false) String place,
    								 	   HttpServletRequest request, HttpServletResponse response) {
    	String fsql   = null;
    	
    	//LOG.info("***uid  " +uid);
		try {
			if (time == null || time.isEmpty() || time.equals("undefined")) {
				time = "12h";
			}
			
			int size = 10;
			if (place != null && place.equals("report")) {
				size = 2000;
			}
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == true) {
				fsql = "index=qubercomm_*,sort=timestamp desc,size="+size+",query=";
			} else {
				fsql = "index="+indexname +",sort=timestamp desc,size="+size+",query=timestamp:>now-"+time + " AND ";
			}
		} catch (Exception e) {
			fsql = "index=" + indexname + ",sort=timestamp desc,size=10,query=timestamp:>now-" + time + " AND ";
		}
		
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}
        	
     	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);
     	
    	List<Map<String, Object>>  logs   = EMPTY_LIST_MAP;
    	
    	if (devices != null && uid == null) {
    		String uidbuilder = buildBeaconDeviceArrayCondition(devices, "uid");
    		if (uidbuilder.length() > 0) {
	        	fsql = fsql + uidbuilder;
	        	fsql = fsql + " AND opcode:\"system_stats\"|value(downlink,Rx,NA);value(uplink,Tx,NA);value(timestamp,time,NA)|table";    	        	
	        	logs   =  fsqlRestController.query(fsql);	        	
    		} else {
    			//LOG.info("Oops No infrastructure available ");
    		}
        	
    	} else if (uid != null) {
    		uid = uid.toUpperCase();
    		fsql = fsql + "uid:\"" + uid + "\"";
    		fsql = fsql + " AND opcode:\"system_stats\"|value(downlink,Rx,NA);value(uplink,Tx,NA);value(timestamp,time,NA)|table";
        	logs   =  fsqlRestController.query(fsql);		
    	}
    	    	    	
    	//LOG.info("BLE RXTX ==> " + logs);
    		//LOG.info("BLE RXTX fsql ==> " + fsql);
    	
    	return logs;

    }
    
    @RequestMapping(value = "/venueagg", method = RequestMethod.GET)
    public List<Map<String, Object>> venueagg(
    		@RequestParam(value="sid", required=true) String sid, 
    		@RequestParam(value="cid", required=false) String cid,
    		HttpServletRequest request, HttpServletResponse response) {
    
    	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
    	
    	Map<String, Object> map  = new HashMap<String, Object>();
    	Map<String, Object> tmap = new HashMap<String, Object>();
    	Map<String, Object> omap = new HashMap<String, Object>();
    	Map<String, Object> imap = new HashMap<String, Object>();
    	Map<String, Object> idmap= new HashMap<String, Object>();
    	
    	int activetag 		= 0;
    	int checkedoutTag  	= 0;
    	int checkedinTag  	= 0;
    	int inactiveTag  	= 0;
    	int idleTag			= 0;
    	
    	try {
    		
    		String status 	= Beacon.STATUS.checkedout.name();
    		String active 	= Beacon.STATE.active.name();
    		String inactive = Beacon.STATE.inactive.name();
    		String idle 	= Beacon.STATE.idle.name();

    		
				Collection<Beacon> checkedout = beaconService.getSavedBeaconByCidAndStatus(cid, status);
				Collection<Beacon> checkedin  = beaconService.getSavedBeaconByCidAndStatus(cid, Beacon.STATUS.checkedin.name());
				List<Beacon> inactiveList 		  = beaconService.getSavedBeaconByCidSidStateAndStatus(cid, sid, inactive, status);
				List<Beacon> idleList			  = beaconService.getSavedBeaconByCidSidStateAndStatus(cid, sid, idle, status);
				List<Beacon> activeList 		  = beaconService.getSavedBeaconByCidSidStateAndStatus(cid, sid, active, status);

				checkedoutTag = checkedout==null?0:checkedout.size();
				checkedinTag  = checkedin==null?0:checkedin.size();
				
				inactiveTag = inactive == null ? 0 : inactiveList.size();
				idleTag 	= idle == null ?0 : idleList.size();
				activetag   = active == null ? 0 : activeList.size();

	    	omap.put("Status", "In-Use");
	    	omap.put("Tags", checkedoutTag);
	    	ret.add(omap);	
	    	
	    	imap.put("Status", "Available");
	    	imap.put("Tags", checkedinTag);
	    	ret.add(imap);
	    	
	    	tmap.put("Status", "Active");
	    	tmap.put("Tags", activetag);
	    	ret.add(tmap);
	    	
	    	if(!customerUtils.entryexit(cid)){
	    		idmap.put("Status", "Idle");
	    		idmap.put("Tags", 	idleTag);
		    	ret.add(idmap);
	    	}
	    	
    		map.put("Status", "Inactive");
    		map.put("Tags", 	inactiveTag);
	    	ret.add(map);
	    	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return ret;
    }
    
    @RequestMapping(value = "/flraggr", method = RequestMethod.GET)
    public List<Map<String, Object>> flraggr(@RequestParam(value="spid", required=true) String spid, 
    										 @RequestParam(value="time", required=false, defaultValue="120") String time,
    										 HttpServletRequest request, HttpServletResponse response) {
    
    	Map<String, Object> map = null;
    	List<Map<String, Object>> rxtx = null;
    	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
    	
    	rxtx = rxtxagg(null, spid, null, null, time, "20", "BLE", request, response);
		if (rxtx.size() > 0 ) {
			map = rxtx.get(0);
			map.put("Radio", "BLE");
			ret.add(map);
		}		
    	    	
   // 	LOG.info("FLOOR MAP STR" + ret.toString());
    	return ret;
    }    
    
    @SuppressWarnings("unused")
	@RequestMapping(value = "/rxtxagg", method = RequestMethod.GET)
    public   List<Map<String, Object>> rxtxagg(@RequestParam(value="sid", required=false) String sid, 
    								 	      @RequestParam(value="spid", required=false) String spid,
    								 	      @RequestParam(value="uid", required=false) String uid,
    								 	      @RequestParam(value="swid", required=false) String swid,
    								 	      @RequestParam(value="time", required=false, defaultValue="12") String time,
    								 	      @RequestParam(value="interval", required=false, defaultValue="2") String interval,
    								 	      @RequestParam(value="radio", required=false, defaultValue="2G5G") String radio,
    								 	      HttpServletRequest request, HttpServletResponse response) {
    	String esql = "";
    	int count   = 0;
    	time = time+"h";
    	interval = interval+"h";
    	if (time.equals("12")) {
    		interval = "12h";
    	} else if (time.equals("18")) {
    		interval = "18h";
    	}else if (time.equals("24")) {
    		interval = "24h";
    	} else {
    		interval = "12h";
    	}
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}    	
    	
    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);
    	
    	if (devices != null && uid == null) {
    		String uidBuidler = buildBeaconDeviceArrayCondition(devices, "uid");
    		if (uidBuidler.length() > 0) {
    			esql = esql + uidBuidler;
    		} else {
    			return EMPTY_LIST_MAP;
    		}
        	
    	} else if (uid != null) {
    		esql = esql + "uid:\"" + uid + "\"";
    	}
    	
    	if (radio.equals("BLE")) {
        	esql = esql + "AND opcode:\"system_stats\"";       	
    	}	
    	
		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == false) {
				esql = esql + " AND timestamp:>now-" + time;
			}
		} catch (Exception e) {
				esql = esql + " AND timestamp:>now-" + time;
		}
       
    	//LOG.info("ESQL" + esql);
    	
    	if (esql != null) {
    		QueryBuilder builder = QueryBuilders.queryStringQuery(esql);
	    	SearchQuery sq = new NativeSearchQueryBuilder()
	    					.withQuery(builder)
	    					.withSort(new FieldSortBuilder("timestamp").order(SortOrder.DESC))
	    					.addAggregation(AggregationBuilders.dateHistogram("bucket")
	    						.field("timestamp")
	    						.interval(new DateHistogramInterval(interval))
	    						.minDocCount(1)
	    						.subAggregation(AggregationBuilders.min("min_vap_rx_bytes").field("_vap_rx_bytes"))
	    						.subAggregation(AggregationBuilders.max("max_vap_rx_bytes").field("_vap_rx_bytes"))
	    						.subAggregation(AggregationBuilders.avg("avg_vap_rx_bytes").field("_vap_rx_bytes"))
	    						.subAggregation(AggregationBuilders.min("min_vap_tx_bytes").field("_vap_tx_bytes"))
	    						.subAggregation(AggregationBuilders.max("max_vap_tx_bytes").field("_vap_tx_bytes"))
	    						.subAggregation(AggregationBuilders.avg("avg_vap_tx_bytes").field("_vap_tx_bytes"))
	    						.subAggregation(AggregationBuilders.avg("avg_ble_rx_bytes").field("downlink"))
	    						.subAggregation(AggregationBuilders.avg("avg_ble_tx_bytes").field("uplink"))
	    						).build();
	    	sq.addIndices(indexname);

	    	sq.setPageable(new PageRequest(0,1));
	    	
	    	Histogram histogram = _CCC.elasticsearchTemplate.query(sq, new ResultsExtractor<Histogram>() {
				@Override
				public Histogram extract(SearchResponse response) {
					return response.getAggregations().get("bucket");
				}
			});
	    	
	    	List<Map<String, Object>> rxtx = new ArrayList<Map<String, Object>>();
	    	Map<String, Object> map = null;
	    	for (Histogram.Bucket entry : histogram.getBuckets()) {
	    		count++;
	    		map = new HashMap();
	    		map.put("time", entry.getKey().toString());
	    		List<Aggregation> aggs = entry.getAggregations().asList();
	    		for(Aggregation agg : aggs) {
	    			String name = agg.getName();
	    			map.put(agg.getName(), ((SingleValue)agg).value());
	    			
	    		}
	    		
	    		rxtx.add(map);
	    		
	    		if (count >= 10) {
	    			break;
	    		}
	    		
	    	}
	    	
	    	//LOG.info("RXTX AFFR " + rxtx);
	    	return rxtx;
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    }
    
    @RequestMapping(value = "/netflow", method = RequestMethod.GET)
    public  List<Map<String, Object>> netflow(@RequestParam(value="sid",  required=false) String sid, 
    								 	      @RequestParam(value="spid", required=false) String spid,
    								 	      @RequestParam(value="swid", required=false) String swid,
    								 	      @RequestParam(value="uid",  required=false) String uid,
    								 	      @RequestParam(value="time", required=false, defaultValue="5") String time,
    								 	      HttpServletRequest request, HttpServletResponse response) {
    	String fsql   = null;
    	
		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == true) {
				fsql = "index=qubercomm_*,sort=timestamp desc,size=10,query=";
			} else {
				fsql = "index="+indexname +",sort=timestamp desc,size=10,query=timestamp:>now-" + time + "m" + " AND ";
			}
		} catch (Exception e) {
				fsql = "index="+indexname +",sort=timestamp desc,size=10,query=timestamp:>now-" + time + "m" + " AND ";
		}

    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}

    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);
    	    	
    	if (devices != null && uid == null) {
    		String uidbuilder = buildBeaconDeviceArrayCondition(devices, "uid");
    		if (uidbuilder.length() > 0) {
	        	fsql = fsql + uidbuilder;
	        	fsql = fsql + " AND web_stats:\"Qubercloud Manager\"|value(num_social,social,NA);value(num_chat,chat,NA);value(num_ecomm,ecom,NA);value(num_http,web,NA);value(timestamp,time,NA)|table";
	        	return fsqlRestController.query(fsql);
    		}
    	} else if (uid != null) {
    		fsql = fsql + "uid:\"" + uid + "\"";
    		fsql = fsql + " AND web_stats:\"Qubercloud Manager\"|value(num_social,social,NA);value(num_chat,chat,NA);value(num_ecomm,ecom,NA);value(num_http,web,NA);value(timestamp,time,NA)|table";
        	return fsqlRestController.query(fsql);    		
    	}
    	
    	return EMPTY_LIST_MAP;
    }
    
    
    @RequestMapping(value = "/netagg", method = RequestMethod.GET)
    public   List<Map<String, Object>> netagg(@RequestParam(value="sid", required=false) String sid, 
    								 	      @RequestParam(value="spid", required=false) String spid,
    								 	      @RequestParam(value="uid", required=false) String uid,
    								 	      @RequestParam(value="swid", required=false) String swid,
    								 	      @RequestParam(value="time", required=false, defaultValue="30") String time,
    								 	      @RequestParam(value="interval", required=false, defaultValue="5") String interval,
    								 	     HttpServletRequest request, HttpServletResponse response) {
    	String esql = "";
    	int count   = 0;
    	time = time+"m";
    	interval = interval+"m";
    	if (time.equals("5")) {
    		interval = "1m";
    	} else if (time.equals("15")) {
    		interval = "2m";
    	} else if (time.equals("30")) {
    		interval = "5m";
    	} else if (time.equals("60")) {
    		interval = "10m";
    	}else if (time.equals("120")) {
    		interval = "20m";
    	}
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}    	
    	
    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);
    	
    	if (devices != null && uid == null) {
        	esql = esql + buildBeaconDeviceArrayCondition(devices, "uid");
        	esql = esql + "AND web_stats:\"Qubercloud Manager\"";
    	} else if (uid != null) {
    		esql = esql + "uid:\"" + uid + "\"";
    		esql = esql + "AND web_stats:\"Qubercloud Manager\"";
    	}
    	
		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == false) {
				esql = esql + " AND timestamp:>now-" + time;
			}
		} catch (Exception e) {
				esql = esql + " AND timestamp:>now-" + time;
		}
        
        
    	if (esql != null) {
    		QueryBuilder builder = QueryBuilders.queryStringQuery(esql);
	    	SearchQuery sq = new NativeSearchQueryBuilder()
	    					.withQuery(builder)
	    					.withSort(new FieldSortBuilder("timestamp").order(SortOrder.DESC))
	    					.addAggregation(AggregationBuilders.dateHistogram("bucket")
	    						.field("timestamp")
	    						.interval(new DateHistogramInterval(interval))
	    						.minDocCount(1)
	    						.subAggregation(AggregationBuilders.min("min_num_social").field("web_social_count"))
	    						.subAggregation(AggregationBuilders.max("max_num_social").field("web_social_count"))
	    						.subAggregation(AggregationBuilders.avg("avg_num_social").field("web_social_count"))
	    						.subAggregation(AggregationBuilders.min("min_num_chat").field("web_chat_count"))
	    						.subAggregation(AggregationBuilders.max("max_num_chat").field("web_chat_count"))	    						
	    						.subAggregation(AggregationBuilders.avg("avg_num_chat").field("web_chat_count"))
	    						.subAggregation(AggregationBuilders.min("min_num_ecomm").field("web_ecomm_count"))
	    						.subAggregation(AggregationBuilders.max("max_num_ecomm").field("web_ecomm_count"))		    						
	    						.subAggregation(AggregationBuilders.avg("avg_num_ecomm").field("web_ecomm_count"))
	    						.subAggregation(AggregationBuilders.min("min_num_http").field("web_http_count"))
	    						.subAggregation(AggregationBuilders.max("max_num_http").field("web_http_count"))	    						
	    						.subAggregation(AggregationBuilders.avg("avg_num_http").field("web_http_count"))
	    						).build();
	    	sq.addIndices(indexname);
	    	//sq.addTypes("message");
	    	sq.setPageable(new PageRequest(0,1));
	    	
	    	Histogram histogram = _CCC.elasticsearchTemplate.query(sq, new ResultsExtractor<Histogram>() {
				@Override
				public Histogram extract(SearchResponse response) {
					return response.getAggregations().get("bucket");
				}
			});
	    	
	    	List<Map<String, Object>> net = new ArrayList<Map<String, Object>>();
	    	Map<String, Object> map = null;
	    	for (Histogram.Bucket entry : histogram.getBuckets()) {
	    		count++;
	    		map = new HashMap();
	    		map.put("time", entry.getKey());
	    		List<Aggregation> aggs = entry.getAggregations().asList();
	    		for(Aggregation agg : aggs) {
	    			String name = agg.getName();
	    			map.put(agg.getName(), ((SingleValue)agg).value());
	    			
	    		}
	    		net.add(map);
	    		if (count >= 10) {
	    			break;
	    		}
	    		
	    	}
	    	return net;
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    }    
    
    @RequestMapping(value = "/alerts", method = RequestMethod.GET)
    public  List<String> alerts(@RequestParam(value="sid",  required=false) String sid, 
    								 	     @RequestParam(value="spid", required=false) String spid,
    								 	     @RequestParam(value="swid", required=false) String swid,
    								 	     @RequestParam(value="uid",  required=false) String uid,
    								 	     @RequestParam(value="duration", required=false, defaultValue="30m") String duration,
    								 	    HttpServletRequest request, HttpServletResponse response) {
    	
    	String val;
    	String str = null;
    	ArrayList<String> alert = new ArrayList<String>();
    	
    	List<BeaconDevice> devices = null;
    	
    	if (sid != null){
    		devices = beacondeviceService.findBySid(sid);
    	} else  if (spid != null) {
    		devices = beacondeviceService.findBySpid(spid);
    	}
    	    	    	
    	if (devices != null) {

    		for (BeaconDevice nd : devices) {
    			
    			String state  = nd.getState() == null ? "inactive" : nd.getState();
				String source = nd.getSource() == null ? "qubercomm" : nd.getSource();
				
				if (!source.equals("qubercomm")) {
					continue;
				}
				if (state.equals("inactive")) {
					val = nd.getUid();
					str = "Device MAC " + val + " Status " + nd.getStatus();
					alert.add(str);
				}

			}
    	}
    	
    	
    	return alert;
    }
    
      
    @RequestMapping(value = "/getcpu", method = RequestMethod.GET)
    public  List<Map<String, Object>> getcpu(@RequestParam(value="sid",  required=false) String sid, 
    								 	     @RequestParam(value="spid", required=false) String spid,
    								 	     @RequestParam(value="swid", required=false) String swid,
    								 	     @RequestParam(value="uid",  required=false) String uid,
    								 	     @RequestParam(value="place",  required=false) String place,
    								 	     @RequestParam(value="duration", required=false, defaultValue="30m") String duration) {
    	
    	List<Map<String, Object>>  res  = EMPTY_LIST_MAP;
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}
   	
    	int size = 1;
    	if(place != null && place.equals("report")){
    		size = 2000;
    	}
    	
    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);
		//String fsql = "index=" + indexname + ",sort=timestamp desc,size=1,query=opcode:\"system_stats\" AND timestamp:>now-30m AND ";
		String fsql = "index=" + indexname + ",sort=timestamp desc,size="+size+",query=opcode:\"system_stats\" AND timestamp:>now-" + duration + " AND ";


    	if (devices != null && devices.size() > 0 && uid == null) {
    	
    		fsql = fsql + buildBeaconDeviceArrayCondition(devices, "uid") + "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";
    		//LOG.info("FSQL CPU =" + fsql);
    		res =  fsqlRestController.query(fsql);
    		//LOG.info( "RESULT QUERY" + res);
    		return res;
    		
    	} else if (uid != null){
    		uid =uid.toUpperCase();
    		fsql = fsql + "uid:\"" + uid + "\"";
    		fsql = fsql + "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";
    		//LOG.info("FSQL CPU1 =" + fsql);
    		res =   fsqlRestController.query(fsql);	
    		//LOG.info( "RESULT QUERY" + res);
    		return res;
    		
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    }
    
    
    @RequestMapping(value = "/deviceupstate", method = RequestMethod.GET)
    public  List<Map<String, Object>>  DeviceUpTime(@RequestParam(value="uid",required=true) String uid,
    												@RequestParam(value="duration", required=false, defaultValue="30m") String duration) {
    	
    	List<Map<String, Object>>  res  = EMPTY_LIST_MAP;

    	uid = uid.toUpperCase();
    	
		String fsql =    " index=" + indexname + ",sort=timestamp desc,size=1,query=opcode:\"system_stats\" "
						+" AND timestamp:>now-"+duration+" AND uid:\"" + uid +"\" |value(uid,uid,NA);"
						+" value(cpu_percentage,cpu,NA);value(ram_percentage,ram_value,NA);" 
						+" value(cpu_days,cpuDays,NA);value(cpu_hours,cpuHours,NA);value(cpu_minutes,cpuMinutes,NA);" 
						+" value(app_days,appDays,NA);value(app_hours,appHours,NA);value(app_minutes,appMinutes,NA);" 
						+" value(uplink,bletx,NA);value(downlink,blerx,NA);value(timestamp,time,NA);|table ";
		
		res = fsqlRestController.query(fsql);
		
		return res;
    }
    
    @RequestMapping(value = "/getmem", method = RequestMethod.GET)
    public  List<Map<String, Object>> getmem(@RequestParam(value="sid",  required=false) String sid, 
    								 	     @RequestParam(value="spid", required=false) String spid,
    								 	     @RequestParam(value="swid", required=false) String swid,
    								 	     @RequestParam(value="uid",  required=false) String uid,
    								 	     @RequestParam(value="place",  required=false) String place,
    								 	     @RequestParam(value="duration", required=false, defaultValue="30m") String duration) {
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}
    	

    	int size = 1;
    	if(place != null && place.equals("report")){
    		size = 2000;
    	}
    	
    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);

    	//String fsql = "index=" + indexname + ",sort=timestamp desc,size=1,query=opcode:\"system_stats\" AND timestamp:>now-30m AND ";
		String fsql = "index=" + indexname + ",sort=timestamp desc,size="+size+",query=opcode:\"system_stats\" AND timestamp:>now-" + duration + " AND ";
    	
    	if (devices != null && devices.size() > 0 && uid == null) {
    	
    		fsql = fsql + buildBeaconDeviceArrayCondition(devices, "uid") + "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";
    		return fsqlRestController.query(fsql);
    	} else if (uid != null){
    		uid  = uid.toUpperCase();
    		fsql = fsql + "uid:\"" + uid + "\"";
    		fsql = fsql + "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";

    		return fsqlRestController.query(fsql);	
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    } 
    
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/gettags", method = RequestMethod.GET)
    public JSONObject gettags(@RequestParam(value="sid", 	 	required=false) String sid, 
    						   @RequestParam(value="spid", 	 	required=false) String spid,
    						   @RequestParam(value="swid", 	 	required=false) String swid,
    						   @RequestParam(value="uid", 	 	required=false) String uid,
    						   @RequestParam(value="duration", 	required=false, defaultValue="5m") String duration) throws IOException {

    	JSONArray  dev_array = new JSONArray();
    	
    	JSONObject devlist 	 = new JSONObject();
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}
		
		int tagcount   = 0;
		int activeRecv = 0;
		int scanner	   = 0;
		int server	   = 0;
    	
    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, swid);
    	
    	if (devices != null && uid == null) {
    		
			for (BeaconDevice nd : devices) {

				tagcount = tagcount + nd.getActivetag();

				String deviceType = nd.getType() == null ? BeaconDevice.GATEWAY_TYPE.receiver.name() : nd.getType();
				
				if (deviceType.equalsIgnoreCase(BeaconDevice.GATEWAY_TYPE.receiver.name())) {
					activeRecv++;
				}
				if (deviceType.equalsIgnoreCase("scanner")) {
					scanner++;
				}
				if (deviceType.equalsIgnoreCase("server")) {
					server++;
				}

			}
    	}
    	
    	
		JSONArray  tag_client = new JSONArray();
		tag_client.add(0,"Tags");
		tag_client.add(1, tagcount);
		
		JSONArray  ble_client = new JSONArray();
		ble_client.add(0,"Receiver");
		ble_client.add(1, activeRecv);
		
		JSONArray  scan_client = new JSONArray();
		scan_client.add(0,"Scanner");
		scan_client.add(1, scanner);
		
		JSONArray  srv_client = new JSONArray();
		srv_client.add(0,"Server");
		srv_client.add(1, server);
		
		dev_array.add(dev_array.size(), tag_client);

		dev_array.add(dev_array.size(), ble_client);
		
		dev_array.add(dev_array.size(), scan_client);
		
		dev_array.add(dev_array.size(), srv_client);
		
		devlist.put("devicesConnected", dev_array);		
		
    	
		return devlist;		
    }
    
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getpeers", method = RequestMethod.GET)
    public JSONObject getpeers(@RequestParam(value="spid", required=false) String spid,
    						   @RequestParam(value= "cid", required=false) String  cid,
    						   @RequestParam(value="uid",  required=false) String uid) throws IOException {

    	JSONObject devlist 	 = new JSONObject();
    	List<Beacon> beacon 	= null ;
    	
		if (uid != null) {
			uid = uid.toUpperCase();
		}
		
		//LOG.info("spid " + spid + "cid " + cid + "uid " + uid);
		
		if (customerUtils.trilateration(cid)) {

			//LOG.info("==========locatum getPeers =============");
			
			BeaconDevice device = beacondeviceService.findOneByUid(uid);
			String bleType = BeaconDevice.GATEWAY_TYPE.receiver.name();

			if (device != null) {
				bleType = device.getType();
			}
			if (bleType.equalsIgnoreCase("server")) {
				beacon = beaconService.getSavedBeaconByServerid(uid);
			} else {
				beacon = beaconService.findByReciverinfo(uid);
			}

			if (beacon != null) {
				devlist = processingTagDetails(beacon);
			}

	
			//LOG.info("devlist>>>>>>>>>>>>>>>>.. " +devlist);
			return devlist;
		}
		return devlist;		
    }
    
	@SuppressWarnings("unchecked")
	private JSONObject processingTagDetails(List<Beacon> beacon) {
		try {
			
			JSONObject taglist 	 	= null;
	    	JSONArray  tagarray 	= new JSONArray();
	    	JSONArray  dev_array 	= new JSONArray();
	    	JSONObject devlist 	 	= new JSONObject();
	    	String state 			= "";
	    	String 	loc_str			= "";
	    	int 	keyFound		= 0;
	    	int location_cnt		= 0;
	    	int activetag			= 0;
	    	String status           = "";
	    	String color 			= "";
			String fafa 			= "";
			
	    	Map<String, Integer> loc_map = new HashMap<String, Integer>();
	    	JSONArray  active_tag_array  = new JSONArray();
	    	JSONArray  locate_tag_array  = new JSONArray();
	    	
				for (Beacon b : beacon) {
					
					status = b.getStatus();
					state  = b.getState();
					
					if ((state.equals("active") || state.equals("idle")) && status.equals("checkedout")) {
						
						taglist = new JSONObject();

						loc_str = b.getLocation().toUpperCase();
						taglist.put("taguid",   	b.getMacaddr());
						taglist.put("location", 	loc_str);
						taglist.put("serverid", 	b.getServerid());
						taglist.put("range", 		b.getRange());
						taglist.put("accuracy", 	b.getAccuracy());
						taglist.put("assignedto", 	b.getAssignedTo().toUpperCase());
						taglist.put("tagtype", 		b.getTag_type().toUpperCase());
						taglist.put("client_type",  "tag");
						taglist.put("distance", 	b.getDistance());

						if (b.getBattery_level() != 0) {
							int battery 		= b.getBattery_level();
							String batteryinfo  = beaconService.batteryStatus(battery);
							taglist.put("fafa",     batteryinfo.split("&")[0]);
							taglist.put("color",    batteryinfo.split("&")[1]);
							taglist.put("battery", battery); //battery percentage
						} else { //battery is  null 
							color = "black";
							fafa  = "fa fa-battery-empty fa-2x";
							taglist.put("fafa",     fafa);
							taglist.put("color",    color);
						}
						tagarray.add (taglist);
						activetag++;

						Set<String> keys = loc_map.keySet();
						keyFound = 0;
						for (String key : keys) {
							if (key.equals(loc_str)) {
								int i = loc_map.get(key);

								//LOG.info("KEY " + key + "value " + i);
								loc_map.put(key, i + 1);
								keyFound = 1;
								//LOG.info("KEY1 " + key + "value1 " + i);
							}
						}

						if (keyFound == 0) {
							loc_map.put(loc_str, 1);
							//LOG.info("KEY2 " + loc_str + "value2" + 1);
						}
					}

				}

			active_tag_array.add (0, "Active Tags");
	 		active_tag_array.add (1, activetag);
	 		
	 		Set<String> keys = loc_map.keySet();
	 		for (String key : keys) {
	 			location_cnt++;
	 		}
	 		
	 		locate_tag_array.add (0, "Active Locations");
	 		locate_tag_array.add (1, location_cnt);
	 		
	 		//LOG.info("Location Map ==>" + loc_map);
	 		

							
			dev_array.add(0, tagarray);
			dev_array.add(1, active_tag_array);
			dev_array.add(2, locate_tag_array);
			int dev_count = 2;
			int i = 1;
	 		for (String key : keys) {
	 			JSONArray  locatum_array = new JSONArray();
	 			locatum_array.add(0, key);
	 			locatum_array.add(1, loc_map.get(key));
	 			dev_array.add (dev_count+i, locatum_array);
	 			i = i + 1;
	 		}
					
			devlist.put("devicesConnected", dev_array);	
			return devlist;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getvaps", method = RequestMethod.GET)
    public JSONObject getvaps(@RequestParam(value="sid", 	  required=false) String sid, 
    						  @RequestParam(value="spid", 	  required=false) String spid,
    						  @RequestParam(value="uid", 	  required=false) String uid,
    						  @RequestParam(value="duration", required=false, defaultValue="5m") String duration) throws IOException {
    	
    	JSONArray  dev_array = new JSONArray();
    	JSONObject dev 	     = null;    	
    	JSONObject devlist 	 = new JSONObject();
    	
    	int num_intf  = 3;
    	String vap_2g = "1";
    	String vap_5g = "1";
    	int count_2g  = 0;
    	int count_5g  = 0;
    	List<BeaconDevice> devices = beacondeviceService.findBy(spid, sid, null);
    	
    	if (devices != null && uid == null) {
	    	for (BeaconDevice nd : devices) { 
	    		
	    		BeaconDevice dv = getBeaconDeviceService().findOneByUid(nd.getUid());
	    		if (dv != null) {
		    		if (vap_2g == null) {
		    			vap_2g = "1";
		    		}	    		
		    		if (vap_5g == null) {
		    			vap_5g = "1";
		    		}
	    		}
	    		
	    		count_2g += Integer.parseInt(vap_2g);
	    		count_5g += Integer.parseInt(vap_5g);
	    	}
    	}
    	
		JSONArray  dev_array1 = new JSONArray();
		JSONArray  dev_array2 = new JSONArray();
		
		dev_array1.add (0, "2GVAP");
		dev_array1.add (1, count_2g);
		dev_array2.add (0, "5GVAP");
		dev_array2.add (1, count_5g);
		
		dev_array.add  (0, dev_array1);
		dev_array.add  (1, dev_array2);
		
		devlist.put("ActiveVaps", dev_array);	
		
		//LOG.info("ActiveVaps" +devlist.toString());	
    	
    	return devlist;
    	
    }    
    
    
	/*connectedInterfaces": [
	                		{"device":"WLAN","status":"enabled"},
	                		{"device":"XBEE","status":"disabled"},
	                		{"device":"PLC","status":"enabled"},
	                		{"device":"BLE","status":"disabled"}
	                	],    
    */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getintf", method = RequestMethod.GET)
    public JSONObject getintf(@RequestParam(value="sid", 	  required=false) String sid, 
    						  @RequestParam(value="spid", 	  required=false) String spid,
    						  @RequestParam(value="uid", 	  required=false) String uid,
    						  @RequestParam(value="duration", required=false, defaultValue="5m") String duration) throws IOException {
    	
    	JSONArray  dev_array = new JSONArray();
    	JSONObject dev 	     = null;    	
    	JSONObject devlist 	 = new JSONObject();
    	int num_intf 		 = 4;
    	
    	for (int i = 0;  i < num_intf; i++) {
    		dev = new JSONObject();
    		switch (i) {
    			case 0:
    					dev.put("device", "wlan2g");
    	    			dev.put("status", "disabled");
    	    			dev.put("vapcount", "1");
    				break;
    			case 1:
    	    			dev.put("device", "wlan5g");
    	    			dev.put("status", "disabled");
    	    			dev.put("vapcount", "1");
    				break;
    			case 2:
    	    		dev.put("device", "ble");
	    			dev.put("status", "enabled");
	    			dev.put("vapcount", "1");   				
    				break;
    			case 3:
    	    		dev.put("device", "xbee");
	    			dev.put("status", "disabled");
	    			dev.put("vapcount", "1");   				
    				break;    				
    				
    			default:
    				break;
    		}

    		dev_array.add(dev);
    	}
    	devlist.put("connectedInterfaces", dev_array);
    	
    	//LOG.info("connectedInterfaces" +devlist.toString());
    	
    	return devlist;
    	
    }    
    
    @SuppressWarnings("unchecked")
   	@RequestMapping(value = "/battery", method = RequestMethod.GET)
       public JSONObject getbattery(@RequestParam(value="sid", 	  required=false) String sid, 
       						  @RequestParam(value="spid", 	  required=false) String spid,
       						  @RequestParam(value="uid", 	  required=false) String uid,
       						  @RequestParam(value="duration", required=false, defaultValue="5m") String duration) throws IOException {
       	
       	JSONArray  dev_array = new JSONArray();
       	JSONObject dev 	     = null;    	
       	JSONObject devlist 	 = new JSONObject();
       	int i  = 0;
       	
       	List<Beacon> beacon =  null ;
		if (uid != null) {
			uid = uid.toUpperCase();
		}
		
    	beacon = beaconService.findByReciverId(uid);
    	
		if (beacon != null) {
			for (Beacon b : beacon) {
			if (b.getUpdatedstatus().equalsIgnoreCase("entry")) {
				dev = new JSONObject();
				dev.put("device", b.getMacaddr());
				dev.put("status", i++);
				dev.put("batterylevel", b.getBattery_level());
				dev_array.add(dev);
			}
		  }
		}

       	devlist.put("batteryinfo", dev_array);
       	
       	return devlist;
       	
       }  
	
	@RequestMapping(value = "/imgcapture", method = RequestMethod.GET)
	public String imgcapture(HttpServletRequest request, HttpServletResponse response) throws IOException {	
		
		String imgFileName = "./uploads/screenshot.jpg";
		
		if (SessionUtil.isAuthorized(request.getSession())) {
		    try {

		    	System.setProperty("java.awt.headless", "false"); 
		        Toolkit tool 	  = Toolkit.getDefaultToolkit();
		        Dimension d 	  = tool.getScreenSize();
		        Rectangle rect 	  = new Rectangle(d);
		        Robot robot 	  = new Robot();
		        File f 			  = new File(imgFileName);
		        BufferedImage img = robot.createScreenCapture(rect);
		        
		        ImageIO.write(img,"jpeg",f);
		        
		      } catch(Exception e){
		    	  e.printStackTrace();
		      }
		    
			File jpegFile = new File(imgFileName);
			response.setContentType("application/jpeg");
			response.setHeader("Content-Disposition", "attachment; filename=" + imgFileName);
			response.setContentLength((int) jpegFile.length());
			
			FileInputStream fileInputStream 	= new FileInputStream(jpegFile);
			OutputStream responseOutputStream 	= response.getOutputStream();
			int bytes;
			
			while ((bytes = fileInputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}	
			
			fileInputStream.close();		    
			responseOutputStream.close();
			return imgFileName;
		}
			
		return imgFileName;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/tagactivity", method = RequestMethod.GET)
    public  JSONObject bottleneck(@RequestParam(value="macaddr", required=true) String macaddr,
    							  @RequestParam(value="time", required=false) String days,
    							  HttpServletRequest request, HttpServletResponse response) {
    	
    	String fsql   = null;
    	List<Map<String, Object>>  logs   = EMPTY_LIST_MAP;
    	
		String size = "100";
		
		if (!StringUtils.isEmpty(days)) {
			if (days.equals("12h"))
				size = "100";
			else if (days.equals("1d"))
				size = "300";
		}

		JSONObject json 	= null;
    	JSONObject jsonList = new JSONObject();
    	JSONArray jsonArray = new JSONArray();
    	
    	Map<String,String> portionMap = new HashMap<String,String>();
    	Map<String,String> deviceMap = new HashMap<String,String>();

		try {
			
			fsql = "index="+reportIndex+", ";
			
			if (size != null) {
				fsql = fsql.concat("size=" + size + ", ");
			}
			
			fsql = fsql.concat("type=trilateration,query=timestamp:>now-" + days+" AND opcode:\"reports\" AND tagid:\""+macaddr+"\""
						+ " AND location_type:receiver,sort=timestamp DESC|value(timestamp,Date,typecast=date);value(tagid,tagid,null);"
						+ " value(cid,cid,null);value(sid,sid,null);value(location,location,null);value(entry_floor,entry_floor,null);"
						+ " value(entry_loc,entry,null);value(exit_floor,exit_floor,null);value(exit_loc,exit,null);"
						+ " value(elapsed_floor,elapsed_floor,null);value(elapsed_loc,elapsed,null);value(spid,Spid,null);value(assingedto,assingedto,null);|table,sort=Date:desc;");

		   // LOG.info("tagactivity fsql========== >   " +fsql);
		   // LOG.info("days" +days +"size " +size);
			
			logs   =  fsqlRestController.query(fsql);
			
			String elapsed 	= "";
			String exit 	= null;
			String entry 	= null;
			
			Beacon beacon 	=  beaconService.findOneByMacaddr(macaddr);
			
			if(beacon == null){
				return null;
			}
			
			String tagType  = beacon.getTag_type();
			String cid      = beacon.getCid();
			
			
			/**
			 *   logic has been moved  to CustomerUtils (formatReportDate())
			 
			if (cid != null) {
				Customer customer = customerService.findById(cid);

				if (customer != null) {
					TimeZone totimezone = customerUtils.FetchTimeZone(customer.getTimezone());
					format.setTimeZone(totimezone);
				} else {
					format.setTimeZone(TimeZone.getTimeZone("UTC"));
				}
			}
			**/
			
			String floorname = beacon.getLocation()== null ? "NA" : beacon.getLocation().toUpperCase();
			String location  = beacon.getReciveralias() == null ? "NA" : beacon.getReciveralias().toUpperCase();
			
			String entryLoc = beacon.getEntry_loc();
			entryLoc 		= customerUtils.formatReportDate(entryLoc);
			
			String exit_time = customerUtils.formatReportDate(beacon.getExitTime());
			
			String def_entry = beacon.getState().equals(Beacon.STATE.inactive.name())? " INACTIVE " : entryLoc;
			String exitTime  = beacon.getExitTime() == null? " INACTIVE ": exit_time;
			String def_exit  = beacon.getState().equals(Beacon.STATE.inactive.name())? exitTime : "Did not Exit";
			
			json = new JSONObject();
			json.put("tagid", 		macaddr);
			json.put("assignedTo", 	beacon.getAssignedTo());
			json.put("tagType", 	tagType);
			json.put("floorname", 	floorname);
			json.put("location", 	location);
			json.put("entry", 		def_entry);
			json.put("exit", 		def_exit);
			json.put("elapsed", 	"0");
			jsonArray.add(json);
			
			for(Map<String,Object> map : logs ) {
				json = new JSONObject();
						
				if (map.containsKey("exit") && map.containsKey("entry")) {
					entry 	= map.get("entry").toString();
					elapsed = map.get("elapsed").toString();
					exit 	= map.get("exit").toString();
					int elap = Integer.parseInt(elapsed);
					if (elap != 0) {
						int hours = elap / 3600;
						int minutes = (elap % 3600) / 60;
						int seconds = (elap % 3600) % 60;
						elapsed = String.format("%02d:%02d:%02d", hours, minutes, seconds);
					}
				}else{
					continue;
				}
				
				String tagid 		= (String) map.get("tagid");
				String assingedto 	= (String) map.get("assingedto");
				String spid 		= (String) map.get("Spid");
				String locationuid  = (String) map.get("location");

				if(locationuid == null || locationuid.isEmpty()){
					continue;
				}
				
				if (deviceMap.containsKey(locationuid)) {
					location = deviceMap.get(locationuid);
				} else {
					BeaconDevice device = beacondeviceService.findOneByUid(locationuid);
					if (device != null) {
						location = device.getName();
					}
					deviceMap.put(locationuid, location);
				}

			
				if (portionMap.containsKey(spid)) {
					floorname = portionMap.get(spid);
				} else {
					Portion p = portionService.findById(spid);
					if (p != null) {
						floorname = p.getUid().toUpperCase();
					} else {
						floorname = "NA";
					}
					portionMap.put(spid, floorname);
				}

				entry = customerUtils.formatReportDate(entry);
				exit  = customerUtils.formatReportDate(exit);
				
				if (StringUtils.isEmpty(entry)) {
					entry = "INACTIVE";
				}
				
				json.put("tagid", 		tagid);
				json.put("assignedTo", 	assingedto);
				json.put("tagType", 	tagType);
				json.put("floorname", 	floorname);
				json.put("location", 	location);
				json.put("entry", 		entry);
				json.put("exit", 		exit);
				json.put("elapsed", 	elapsed);
				jsonArray.add(json);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		jsonList.put("bottleneck", jsonArray);
//		LOG.info("jsonList "+jsonList);
    	return jsonList;

    }
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/PatientVisitPathBySession", method = RequestMethod.GET)
    public  JSONObject PatientVisitPathBySession(@RequestParam(value="macaddr", required=true) String macaddr,
				    							@RequestParam(value="time", required=false) String days,
				    							HttpServletRequest request, HttpServletResponse response) {
    	
    
		String fsql   = null;
    	List<Map<String, Object>>  logs   = EMPTY_LIST_MAP;
    	String visitId = "";
    	
    	JSONObject json =null;
    	JSONObject jsonList = new JSONObject();
    	JSONArray jsonArray = new JSONArray();
    	
		try {
			
			//LOG.info("PatientVisitPathBySession Tag macaddr <<<<<<<< " +macaddr);
			//LOG.info("PatientVisitPathBySession  days <<<<<<<< " +days);
			
			Beacon beacon = null;
			beacon = beaconService.findOneByMacaddr(macaddr);
			
			if (beacon  != null) {
				visitId = beacon.getId();
			}
			
			
			fsql = " fsql=index=fsi-beacon-event-agarwal,size=100,query=opcode:\"entry-exit\" AND timestamp:>now-"+days+ " "
					+ " AND visitId:\""+visitId+"\" | value(timestamp,Date,typecast=date);value(name,Name,null);"
					+ " value(location,Location,null);value(timeElapsed,Time Elapsed,null);value(opcode,Action,null);"
					+ " value(visitId,Session,null)|table,sort=Date:desc ";

			//LOG.info(" PatientVisitPathBySession fsql " +fsql);
			
			logs   =  fsqlRestController.query(fsql);
	
			for(Map<String,Object> map : logs ) {
				json = new JSONObject();
				if (map.get("Date") != null) {
					json.put("date", df.format(map.get("Date")));
				}
				json.put("location", 	map.get("Location"));
				json.put("timeElapsed", map.get("Time Elapsed"));
				json.put("visit", 		map.get("Action"));
				jsonArray.add(json);
			}
			
			//LOG.info(" PatientVisitPathBySession JSON Array  ======== >  " +jsonArray);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		jsonList.put("bottleneck", jsonArray);
    	return jsonList;

    }
	

	@RequestMapping(value = "/finder_log", method = RequestMethod.GET)
    public  List<Map<String, Object>> finderlog(@RequestParam(value="duration",required=false) String days,
												@RequestParam(value = "cid", required = false) String cid,
												@RequestParam(value = "uid", required = false) String uid) {

		List<Map<String, Object>> logs = EMPTY_LIST_MAP;
		try {

			List<BeaconDevice> devices = null;
			
			if (uid != null && !uid.isEmpty()) {
				devices = getBeaconDeviceService().findByUid(uid);
			} else {
				devices = getBeaconDeviceService().findByCid(cid);
			}
			
			String fsql = "index="+indexname+",sort=timestamp desc,size=1000,query=finder_log:\"user-level\" AND timestamp:>now-"+days+" AND ";
			
			if (devices != null && devices.size() >0) {
				
				String uidbuilder = buildBeaconDeviceArrayCondition(devices, "lmac");
				fsql = fsql + uidbuilder.toLowerCase();
				fsql+= " |value(message,snapshot,NA);value(lmac,lmac,NA);value(timestamp,time,NA);|table";
				
				//LOG.info("fsql " +fsql);
				
				logs = fsqlRestController.query(fsql);
			}
			//LOG.info("logs " +logs);
		} catch(Exception e) {
			LOG.error("while Finder Log getting error ",e);
		}
    	return logs;
    }
	
	 @RequestMapping(value = "/venue/peercount", method = RequestMethod.GET)
	 public  int venuePeercount(@RequestParam(value="sid", required=true)String sid,@RequestParam(value="cid", required=true)String cid) {
	    
			int device_count = 0;
			try {

			if (customerUtils.trilateration(cid)) {
				
				String state 		= Beacon.STATE.active.name();
				String status 		= Beacon.STATUS.checkedout.name();
				
				List<Beacon> beacon = beaconService.getSavedBeaconByCidSidStateAndStatus(cid,sid, state, status);
				
				if (beacon != null) {
					device_count = beacon.size();
				}
			} else {
					
					List<BeaconDevice> devices = null;
			    	devices = beacondeviceService.findBySid(sid);
			    	
			    	if (devices == null) {
			    		return  0;
			    	}
					
					if (devices.size() > 0) {
			    		for (BeaconDevice nd : devices) {
			    			device_count = device_count + nd.getActivetag();
			    		}
					}
				}
			} catch (Exception e) {
				LOG.info("While getting venue peercount error " ,e);
			}
	    	
	    	return device_count;
	    }
				
		 @RequestMapping(value = "/venue/agg", method = RequestMethod.GET)
		    	public List<Map<String, Object>> venueaggNew(@RequestParam(value="sid", required=true) String sid,
		    												 @RequestParam(value="cid", required=false) String cid,
		    												 HttpServletRequest request, HttpServletResponse response) {
		    
		    	Map<String, Object> map = null;
		    	List<Map<String, Object>> rxtx = null;
		    	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		    	
				List<BeaconDevice> ulist  = beacondeviceService.findBySid(sid);
				List<BeaconDevice> mlist  = Collections.unmodifiableList(ulist);
				List<BeaconDevice> list   = new ArrayList<BeaconDevice>(mlist);
				
		    	String flrName;
		    	String prev_spid	= "";
		    	
		    	try {
			    	for (BeaconDevice nd : list) { 
			    		
			    		if (prev_spid.equals(nd.spid) == false) {
				    		rxtx = venue_agg(nd.spid,cid);
							Portion port = portionService.findById(nd.spid);
							
							if (port!= null) {
								flrName = port.getUid();
							} else {
								flrName = "Floor";
							}
				    		
				    		if (rxtx.size() > 0 ) {
				    			map = rxtx.get(0);
				    			map.put("Status", flrName);
				    			if (ret.contains(map)) {
				    				// nothing to do
				    			} else {
				    				ret.add(map);
				    			}
				    			
				    		}
				    		
				    		prev_spid = nd.spid;
			    		}
			    	}
		    	} catch (Exception e) {
		    		LOG.info("While getting venue Aggr error " ,e);
		    	}
		    	
		    	return ret;
		    }
		 
		public List<Map<String, Object>> venue_agg(String spid,String cid) {
		
			List<BeaconDevice> devices = beacondeviceService.findBySpid(spid);
			List<Map<String, Object>> venueMap = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmap = new HashMap<String, Object>();
	    	int activetag = 0;
	    	int idletag   = 0;
	    	int inacttag  = 0;
	    	String state  = "";
	    	String status = "checkedout";
	    	boolean entryExit = customerUtils.entryexit(cid);
	    	
		if (customerUtils.trilateration(cid)) {
			List<Beacon> beacon = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
			if (beacon != null) {
				for (Beacon b : beacon) {
					state 				= b.getState();
					String beacon_cid 	= b.getCid();
					
					if (cid.equals(beacon_cid)) {
						
						if (state.equals(Beacon.STATE.active.name())) {
							activetag++;
						}
						if (state.equals(Beacon.STATE.inactive.name())) {
							inacttag++;
						}
						if (state.equals(Beacon.STATE.idle.name())) {
							idletag++;
						}
					}
				}
			}
			//LOG.info(" trilateration Floor vs Traffic Active tags " + activetag);
		} else {
			for (BeaconDevice entry : devices) {
				activetag = activetag + entry.getActivetag();
			}
		}
			
	    	tmap.put("Status", 	   "Active");
	    	tmap.put("activeTags", activetag);
	    	tmap.put("inactTags",  inacttag);
	    	if(!entryExit){
	    		tmap.put("idleTags",   idletag);
	    	}
	    	venueMap.add(tmap);
	    	
			return venueMap;
	
		}
	
	@SuppressWarnings("unchecked")
	public JSONArray processTagType(JSONObject maplist) {

		JSONArray dev_array = new JSONArray();
		JSONObject dev 		= null;

		for (Iterator iterator = maplist.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			dev = new JSONObject();
			dev.put("tagType", key); // Tag type
			dev.put("tagCount", maplist.get(key)); // Tag count
			dev_array.add(dev);
		}

		return dev_array;

	}
	
	 	@SuppressWarnings("unchecked")
		@RequestMapping(value = "/venue/connectedTagType", method = RequestMethod.GET)
	    public JSONArray connectedTagType(@RequestParam(value="sid", required=false) String sid,
	    								   @RequestParam(value="spid",required=false) String spid,
	    								   @RequestParam(value="uid", required=false) String uid,
	    								   @RequestParam(value="cid", required=false) String cid) throws IOException {
	    	
	    	JSONArray  dev_array = new JSONArray();
	    	JSONObject dev 	     = null;    	
	    	String status = Beacon.STATUS.checkedout.name();
	    	String state  = "";
	    	
	    	try {
	    		
	    		if (customerUtils.trilateration(cid)) {
		
					//LOG.info(" cid " + cid + "sid " + sid + "spid " + spid);
	    				
	    			List<Beacon> beacon = null;
	    			JSONObject map 	 	= new JSONObject();
	    			
					if (spid != null) {
						beacon = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
					} else if (sid != null) {
						beacon = beaconService.getSavedBeaconBySidAndStatus(sid, status);
					} else if (uid != null){
						
						BeaconDevice device =  beacondeviceService.findOneByUid(uid);
						String bleType = "";
						
						if (device != null) {
							spid 	=  device.getSpid();
							bleType = device.getType();
						}
						
						uid = uid.toUpperCase();
						
						if (bleType.equals("server")) { // server based tag type
							beacon = beaconService.getSavedBeaconByServerid(uid);
						} else { // Receiver based tag type  							
							beacon = beaconService.findByReciverinfo(uid); // Receiver based tag type
						}
					}
	    			
					
					// floor and venue based tag type  process
					if (beacon != null) {
						for (Beacon b : beacon) {
							String tag_type 	= b.getTagType();
							status 				= b.getStatus();
							state  				= b.getState();
							String beacon_cid   = b.getCid();
							
							if (status.equalsIgnoreCase(Beacon.STATUS.checkedout.name()) && !state.equals(Beacon.STATE.inactive.name()) && cid.equals(beacon_cid)) {
								if (map.containsKey(tag_type)) {
									int count = Integer.parseInt(String.valueOf(map.get(tag_type)));
									map.put(tag_type, count+1);
								}  else {
									map.put(tag_type, 1);
								}
							}
						}
						dev_array = processTagType(map);				
					}
					if (dev_array == null || dev_array.isEmpty()) {
						dev = new JSONObject();
						dev.put("tagType", "Tag"); // Tag type
						dev.put("tagCount", 0); // Tag count
						dev_array.add(dev);
					}
				
					//devlist.put("connectedTagTypes", dev_array);
					//LOG.info(" Connected Tag Type " + devlist);	
					
					return dev_array;
			}
		} catch (Exception e) {
			LOG.info("While getting connected TagType error ", e);
		} 	
	    	
    	if (dev == null || dev.size() <= 0) {
			dev = new JSONObject();
			dev.put("tagType", "Tag"); // Tag type
			dev.put("tagCount", 0); // Tag count
			dev_array.add(dev);
		}
		
    	//devlist.put("connectedTagTypes", dev_array);
		return dev_array;
	} 
		 
	@RequestMapping(value = "/venue/checkoutTag", method = RequestMethod.GET)
	public int venueCheckedout(@RequestParam(value = "sid", required = false) String sid,
							   @RequestParam(value = "cid", required = false) String cid) {

		int device_count = 0;
			
		if (customerUtils.trilateration(cid)) {
			String status 		= Beacon.STATUS.checkedout.name();
			List<Beacon> beacon = beaconService.getSavedBeaconBySidAndStatus(sid, status);
			if (beacon == null) {
				return device_count;
			}
			device_count = beacon.size();
			return device_count;
		} else {

			List<BeaconDevice> devices = beacondeviceService.findBySid(sid);
			if (devices == null) {
				return 0;
			}

			if (devices.size() > 0) {
				for (BeaconDevice nd : devices) {
					device_count = device_count + nd.getCheckedoutTag();
				}
			}
		}
		return device_count;
	}
	
		
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/venue/gettags", method = RequestMethod.GET)
    public JSONObject venueTags(@RequestParam(value="sid",required=false) String sid) throws IOException {

    	JSONArray  dev_array = new JSONArray();
    	
    	JSONObject devlist 	 = new JSONObject();
    	int tagcount   = 0;
		int activeRecv = 0;
		int scanner	   = 0;
		int server	   = 0;
    	
    	List<BeaconDevice> devices = beacondeviceService.findBySid(sid);
    	
    	if (devices != null) {
    		
	    	for (BeaconDevice nd : devices) { 
					
				String deviceType = nd.getType() == null ? BeaconDevice.GATEWAY_TYPE.receiver.name() : nd.getType();

				tagcount = tagcount + nd.getActivetag();

				if (deviceType.equalsIgnoreCase(BeaconDevice.GATEWAY_TYPE.receiver.name())) {
					activeRecv++;
				}
				if (deviceType.equalsIgnoreCase("scanner")) {
					scanner++;
				}
				if (deviceType.equalsIgnoreCase("server")) {
					server++;
				}

			}
    	}
    	
		JSONArray  tag_client = new JSONArray();
		tag_client.add(0,"Tags");
		tag_client.add(1, tagcount);
		
		JSONArray  ble_client = new JSONArray();
		ble_client.add(0,"Receiver");
		ble_client.add(1, activeRecv);
		
		JSONArray  scan_client = new JSONArray();
		scan_client.add(0,"Scanner");
		scan_client.add(1, scanner);
		
		JSONArray  srv_client = new JSONArray();
		srv_client.add(0,"Server");
		srv_client.add(1, server);
		
		dev_array.add(dev_array.size(), tag_client);

		dev_array.add(dev_array.size(), ble_client);
		
		dev_array.add(dev_array.size(), scan_client);
		
		dev_array.add(dev_array.size(), srv_client);
		
		devlist.put("devicesConnected", dev_array);		
		
		return devlist;		
    }

	@RequestMapping(value = "/beacon/alerts", method = RequestMethod.GET)
	public ArrayList<String> alert(
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			HttpServletRequest request) {
		
		ArrayList<String> alert = new ArrayList<String>();
		
		final String status 	 = Beacon.STATUS.checkedout.name();
		
		List<Beacon> beaconsList  = null;
		
		if (spid != null) {
			beaconsList = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
		} else if (sid != null) {
			beaconsList = beaconService.getSavedBeaconBySidAndStatus(sid, status);
		} else {
			Collection<Beacon> beacon = beaconService.getSavedBeaconByCidAndStatus(cid, status);
			if (beacon != null) {
				beaconsList = new ArrayList<Beacon>();
				beaconsList.addAll(beacon);
			}
		}
		if (beaconsList != null) {
			
			Customer customer = customerService.findById(cid);
			
			if (customer != null) {
				TimeZone totimezone = customerUtils.FetchTimeZone(customer.getTimezone());
				format.setTimeZone(totimezone);
			} else {
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
			}
			
			beaconsList.forEach(ibeacon -> {

				String state 		= ibeacon.getState();
				String macAddr 		= ibeacon.getMacaddr();
				String assignedTo 	= ibeacon.getAssignedTo().toUpperCase();
				String lastReportingTime = ibeacon.getLastReportingTime();

				if (StringUtils.isNotEmpty(lastReportingTime)) {
					lastReportingTime = "Last Active Time :" + lastReportingTime +" "+format.getTimeZone().getDisplayName(false, 0);
				} else {
					lastReportingTime = "Tag was never reported";
				}

				if (state.equalsIgnoreCase(Beacon.STATE.inactive.name())) {
					alert.add(assignedTo + " ( " + macAddr + " ) ---> " + lastReportingTime);
				}

				int battery = ibeacon.getBattery_level();

				if (battery <= 40) {
					alert.add(assignedTo + " ( " + macAddr + " ) Battery level <= " + battery + "%");
				}

			});
		}
   		 return alert;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/inactivetags", method = RequestMethod.GET)
	public JSONObject inactiveTags(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "pdfgenration", required = false) Boolean pdfgenration) {
		
		if (cid == null || cid.isEmpty()) {
			return null;
		}

		JSONObject devlist = new JSONObject();
		JSONArray dev_array = new JSONArray();
		
		try {
			
			JSONObject dev = null;
			String state   = Beacon.STATE.inactive.name();
			String status  = Beacon.STATUS.checkedout.name();
			
			Collection<Beacon> beacons 	= null;
			beacons 				    = beaconService.getSavedBeaconByCidStateAndStatus(cid,state,status);
			
			if (customerUtils.trilateration(cid)) {
				
				
				Customer customer = customerService.findById(cid);
				if (customer != null) {
					TimeZone totimezone = customerUtils.FetchTimeZone(customer.getTimezone());
					format.setTimeZone(totimezone);
				} else {
					format.setTimeZone(TimeZone.getTimeZone("UTC"));
				}
				
				for (Beacon beacon : beacons) {
					
					String floorname = beacon.getLocation() != null? beacon.getLocation():"NA";
					String alias 	 = beacon.getReciveralias() != null? beacon.getReciveralias():"NA";
					String lastSeen  = beacon.getLastReportingTime();
					
					String timeStamp = "NOT SEEN";
					
					if (StringUtils.isNotEmpty(lastSeen)) {
						timeStamp = customerUtils.formatReportDate(lastSeen);
					}
					
					dev = new JSONObject();

					dev.put("macaddr", 		beacon.getMacaddr());
					dev.put("minor", 		beacon.getMinor());
					dev.put("major",		beacon.getMajor());
					dev.put("assignedTo", 	beacon.getAssignedTo().toUpperCase());
					dev.put("tagtype", 		beacon.getTagType().toUpperCase());
					dev.put("floorname", 	floorname);
					dev.put("state", 	    state.toUpperCase());
					dev.put("alias", 	    alias.toUpperCase());
					dev.put("lastSeen", 	timeStamp);
					dev_array.add(dev);
				}
			} else {
				// entry-exit
				//LOG.info("entry-exit");
			}
		} catch (Exception e) {
			LOG.info("tagalert error " +e);
			e.printStackTrace();
		}
		
		if (dev_array == null || dev_array.size() == 0) {
			if(pdfgenration != null && pdfgenration){
				return null;
			}
			dev_array = defaultDatas(dev_array);
		}
	
		devlist.put("inactivetags", dev_array);
		//LOG.info("inactive tags>>>>>>>>>>>>>>" + devlist.toString());
		return devlist;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray defaultDatas(JSONArray dev_array) {
		JSONObject dev = new JSONObject();
		dev.put("macaddr",  	"-");
		dev.put("minor", 		"0");
		dev.put("major", 		"0");
		dev.put("assignedTo", 	"-");
		dev.put("tagtype", 		"-");
		dev.put("floorname", 	"NA");
		dev.put("alias", 		"NA");
		dev.put("batterylevel", "NA");
		dev.put("uid", 			"-");
		dev.put("type", 		"-");
		dev.put("portionname", 	"NA");
	    dev.put("sitename",    	"NA");
	    dev.put("state",    	"-");
	    dev.put("status",       "Unknown");
	    dev.put("timestamp", 	"NOT SEEN");
		dev_array.add(dev);
		return dev_array;
	}


	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/beaconbattery", method = RequestMethod.GET)
	public JSONObject beaconBatteryAlert(
			@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "level", required = true) String level,
			@RequestParam(value = "pdfgenration", required = false) Boolean pdfgenration) {
		
		if (cid == null || cid.isEmpty()) {
			return null;
		}

		JSONObject devlist = new JSONObject();
		JSONArray dev_array = new JSONArray();
		
		try {
			
			int batterylevel = Integer.parseInt(level);
			JSONObject dev 					    = null;
			int batteryLevel  					= 0;
			String status 						= Beacon.STATUS.checkedout.name();
			
			Collection<Beacon> beacons 	= null;
			beacons = beaconService.getSavedBeaconByCidAndStatus(cid, status);
			
			if (customerUtils.trilateration(cid)) {
				for (Beacon dv : beacons) {
					if (dv.getBattery_level() != 0) {
						
						batteryLevel 		   = dv.getBattery_level();
						String reciverLocation = dv.getReciveralias()==null ? "NA" : dv.getReciveralias().toUpperCase();
						String floorname 	   = dv.getLocation()==null ? "NA" : dv.getLocation().toUpperCase();
						
						if (batteryLevel < batterylevel) {
							dev =	new JSONObject();
							dev.put("macaddr",  	dv.getMacaddr());
							dev.put("minor", 		dv.getMinor());
							dev.put("major", 		dv.getMajor());
							dev.put("assignedTo", 	dv.getAssignedTo().toUpperCase());
							dev.put("tagtype", 		dv.getTagType().toUpperCase());
							dev.put("batterylevel", batteryLevel+"%");
							dev.put("floorname", 	floorname);
							dev.put("alias", 	    reciverLocation);
							dev_array.add(dev);
						}
					}
				}
			} else {
				// entry-exit
				//LOG.info("entry-exit");
			}
		} catch (Exception e) {
			LOG.info("beaconbattery error " +e);
			e.printStackTrace();
		}
		
		if (dev_array == null || dev_array.size() == 0) {
			if(pdfgenration != null && pdfgenration){
				return null;
			}
			dev_array = defaultDatas(dev_array);
		}
		
		devlist.put("beaconbattery", dev_array);
		
		return devlist;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/beacondevicealert", method = RequestMethod.GET)
	public JSONObject beaconDeviceAlert(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "pdfgenration", required = false) Boolean pdfgenration) {
		
		if (StringUtils.isEmpty(cid)) {
			return null;
		}

		JSONObject devlist  = new JSONObject();
		JSONArray dev_array = new JSONArray();
		
		try {

			JSONObject dev 	= null;

			List<BeaconDevice> devices = beacondeviceService.findByCid(cid);
			
			if (devices != null) {
				
				HashMap<String,String> portionMap   = new HashMap<String,String>();
				
				Customer customer = customerService.findById(cid);
				
				if (customer != null) {
					TimeZone timeZone = customerUtils.FetchTimeZone(customer.getTimezone());
					format.setTimeZone(timeZone);
				} else {
					format.setTimeZone(TimeZone.getTimeZone("UTC"));
				}
				
				for (BeaconDevice device : devices) {

					String source = device.getSource() == null ? "qubercomm" : device.getSource();
					
					String lastSeen = "";
					String state    = "";
					
					if (!source.equals("qubercomm")) {
						org.json.simple.JSONObject gatewayState = outsourceRestController.gatewayStatus(device.getUid());
						state 	 = (String)gatewayState.get("state");
						lastSeen = (String)gatewayState.get("lastSeen");
					} else {
						state    = device.getState();
						lastSeen = device.getLastseen();
					}
					
					if (BeaconDevice.STATE.active.name().equalsIgnoreCase(state)) {
						continue;
					}
					
					dev = new JSONObject();
					
					String location  = device.getName() == null ? "NA" : device.getName().toUpperCase();
					String spid 	 = device.getSpid();
					
					String floorName = "NA";
					
					if (spid != null) {
						if (portionMap.containsKey(spid)) {
							floorName = portionMap.get(spid);
						} else {
							Portion portion = portionService.findById(spid);
							if (portion != null) {
								floorName = portion.getUid() == null ? "NA" : portion.getUid();
							}
							portionMap.put(spid, floorName);
						}
					}

					String timestamp = "NOT SEEN";
					
					if (!StringUtils.isEmpty(lastSeen) && !lastSeen.equals("NA")) {
						timestamp = lastSeen;
					}
					
					String fileStatus   = device.getDevCrashDumpUploadStatus() == null ? "NA" : device.getDevCrashDumpUploadStatus();
					String fileName	    = device.getDevCrashdumpFileName()== null ? "NA" : device.getDevCrashdumpFileName();
					
					String crashState = "enabled";
					
					if (fileStatus.isEmpty() || fileStatus.equals("NA") || !fileStatus.equals("0")) {
						crashState = "disabled";
					}
					
					dev.put("portionname", 	floorName);
					dev.put("uid", 			device.getUid());
					dev.put("type",  	    device.getType().toUpperCase());
					dev.put("status", 		state.toUpperCase());
					dev.put("alias", 		location);
					dev.put("timestamp", 	timestamp);
					dev.put("filestatus", 	fileStatus);
					dev.put("fileName",     fileName);
					dev.put("crashState", 	crashState);
					dev_array.add(dev);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if ((dev_array == null || dev_array.size() == 0)) {
			if(pdfgenration != null && pdfgenration){
				return null;
			}
			dev_array = defaultDatas(dev_array);
		}
		
		devlist.put("beacondevicealert", dev_array);
		
		return devlist;
	}
	
	@RequestMapping(value = "/inactiveTagsCount", method = RequestMethod.GET)
    public  int inactiveTags(@RequestParam(value="cid", required=true)String cid,
    						 @RequestParam(value="sid", required=false)String sid,
    					  	 @RequestParam(value="spid",required=false)String spid) {
    
    	int inactiveTags = 0;
	   	
    	if (customerUtils.trilateration(cid)) {
    		
    		List<Beacon> beacon = null;

    		String status 		= Beacon.STATUS.checkedout.name();
    		String state        = Beacon.STATE.inactive.name();
    		
			if (spid != null) {
				beacon = beaconService.getSavedBeaconByCidSpidStateAndStatus(cid,spid,state,status);
			} else {
				beacon = beaconService.getSavedBeaconByCidSidStateAndStatus(cid,sid,state,status);
			}
        	
			inactiveTags = beacon == null ? 0 : beacon.size();

		} else {
			LOG.info("Entry- exit");
		}
	   	//	LOG.info("inactive Tags  count " +inactiveTags);
    	return inactiveTags;
    }
	
	@RequestMapping(value = "/idleTagsCount", method = RequestMethod.GET)
	public int idleTags(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid) {

		int idleTags = 0;

		if (customerUtils.trilateration(cid)) {

			List<Beacon> beacon = null;

			String status 		= Beacon.STATUS.checkedout.name();
			String state        = Beacon.STATE.idle.name();
    		
			if (spid != null) {
				beacon = beaconService.getSavedBeaconByCidSpidStateAndStatus(cid,spid,state,status);
			} else {
				beacon = beaconService.getSavedBeaconByCidSidStateAndStatus(cid,sid,state,status);
			}

			idleTags = beacon == null ? 0 : beacon.size();

		} else {
			LOG.info("Entry- exit");
		}
		//LOG.info("idleTags Tags  count " + idleTags);
		return idleTags;
	}
	
	@RequestMapping(value = "/alltagstatus", method = RequestMethod.GET)
    public JSONObject heatMap(@RequestParam(value="cid",required = true) String cid,
    						  @RequestParam(value="sid",required =  false) String sid,
    						  @RequestParam(value="spid",required =  false) String spid) 
    						 throws IOException {
		
		
		
		
		int active 			= 0;
		int idle 			= 0;
		int inactive		= 0;
		int total  			= 0;
		
		String state  		 =  Beacon.STATE.inactive.name();
		final String status  =   Beacon.STATUS.checkedout.name();
		
		if (customerUtils.trilateration(cid)) {
			
			Collection<Beacon> beacon = null;
			
			if (sid != null) {
				beacon = beaconService.getSavedBeaconBySidAndStatus(sid, status);
			} else if (spid != null) {
				beacon = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
			} else {
				beacon = beaconService.getSavedBeaconByCidAndStatus(cid, status);
			}
			
			if (beacon != null) {
				for (Beacon b : beacon) {
					
					state 				= b.getState();
					String beacon_cid 	= b.getCid();
					
					if (cid.equals(beacon_cid)) {
						
						if (state.equals(Beacon.STATE.active.name())) {
							active++;
						}
						if (state.equals(Beacon.STATE.inactive.name())) {
							inactive++;
						}
						if (state.equals(Beacon.STATE.idle.name())) {
							idle++;
						}
					}
				}
			}
		} else {
			//LOG.info("other solution");
		}
		
		JSONObject devlist      = new JSONObject();
		JSONArray  data_array 	= new JSONArray();
		
		JSONArray  active_array 	=  new JSONArray();
		JSONArray  idle_array 		=  new JSONArray();
		JSONArray  inactive_array 	=  new JSONArray() ;
		JSONArray  total_array 		=  new JSONArray() ;
		
		
		active_array.add(0, "Active");
		active_array.add(1, active);
		
		idle_array.add(0,"Idle");
		idle_array.add(1,idle);
		
		inactive_array.add(0,"Inactive");
		inactive_array.add(1,inactive);
		
		
		total = active + idle +inactive;
		
		total_array.add(0,"Total");
		total_array.add(1,total);
		
		
		data_array.add(0,active_array);
		data_array.add(1,idle_array);
		data_array.add(2,inactive_array);
		data_array.add(3,total_array);
		
		
		devlist.put("tagstatus", data_array);	
		
		
		return devlist;
	}
	
	private BeaconDeviceService getBeaconDeviceService() {
		if (beacondeviceService == null) {
			beacondeviceService = Application.context.getBean(BeaconDeviceService.class);
		}
		return beacondeviceService;
	}	
	
	@RequestMapping(value = "/finderScatterChart", method = RequestMethod.GET)
    public JSONObject finderScatterChart(@RequestParam(value="cid",required = false) String cid,
    						  @RequestParam(value="sid",required =  false) String sid,
    						  @RequestParam(value="spid",required =  false) String spid) 
    						 throws IOException {
		Collection<Beacon> beaconlist = null;
		String status 		   = "checkedout";
		
		if(spid != null){
			beaconlist = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
		}else if(sid != null){
			beaconlist = beaconService.getSavedBeaconBySidAndStatus(sid, status);
		}else{
			beaconlist = beaconService.getSavedBeaconByCidAndStatus(cid, status);
		}
		
		String active = Beacon.STATE.active.name();
		String inactive = Beacon.STATE.active.name();
		String idle  = Beacon.STATE.active.name();
		
		JSONObject stateJson =  null;
		JSONObject stateDetails =  null;
		JSONArray jsonArray =  null;
		
		JSONObject tagjson =  null;
		JSONArray taglist =  null;
		JSONObject json =  null;
		
		HashSet<String> floornames =  new HashSet<String>();
		if(beaconlist != null){
			 stateJson = new JSONObject();
			for (Beacon beacon : beaconlist) {
				int count = 0;
				String b_state = beacon.getState();
				String tagid = beacon.getMacaddr();
				String b_sid = beacon.getSid();
				String b_spid = beacon.getSpid();
				String b_floorname = beacon.getLocation();
				floornames.add(b_floorname);
				
				if (b_floorname == null) {
					b_floorname = "unknown";
				} else {
					b_floorname = b_floorname.toLowerCase();
				}

				if (stateJson.containsKey(b_state)) {

					stateDetails = (JSONObject) stateJson.get(b_state);
					jsonArray = (JSONArray) stateDetails.get("floors");
					count = Integer.parseInt(stateDetails.get("count").toString()) + 1;

					int floorAvailable = 0;

					for (int i = 0; i < jsonArray.size(); i++) {
						json = (JSONObject) jsonArray.get(i);
						if (json.containsValue(b_floorname)) {
							taglist = (JSONArray) json.get("taglist");

							tagjson = new JSONObject();

							tagjson.put("tagid", tagid);
							tagjson.put("sid", b_sid);
							tagjson.put("spid", b_spid);

							taglist.add(tagjson);

							json.replace("taglist", taglist);

							jsonArray.remove(i);
							jsonArray.add(json);
							floorAvailable = 1;
							break;
						}
					}
					if (floorAvailable == 0) {

						json = new JSONObject();
						taglist = new JSONArray();
						tagjson = new JSONObject();

						tagjson.put("tagid", tagid);
						tagjson.put("sid", b_sid);
						tagjson.put("spid", b_spid);

						taglist.add(tagjson);

						json.put("floorname", b_floorname);
						json.put("taglist", taglist);

						jsonArray.add(json);
					}
				} else {
					stateDetails = new JSONObject();
					jsonArray = new JSONArray();
					json = new JSONObject();
					tagjson = new JSONObject();
					taglist = new JSONArray();
					count = 1;

					tagjson.put("tagid", tagid);
					tagjson.put("sid", b_sid);
					tagjson.put("spid", b_spid);

					taglist.add(tagjson);

					json.put("floorname", b_floorname);
					json.put("taglist", taglist);
					jsonArray.add(json);
				}
				stateDetails.put("count", count);
				stateDetails.put("floors", jsonArray);
				
				stateJson.put(b_state, stateDetails);
			}
		}
		if (!stateJson.containsKey(active)
			|| !stateJson.containsKey(inactive)
			|| !stateJson.containsKey(idle)) {
				
				json = new JSONObject();
				json.put("count", 0);
				if (!stateJson.containsKey(active)) {
					stateJson.put("active", json);
				}
				if (!stateJson.containsKey(inactive)) {
					stateJson.put("inactive", json);
				}
				if (!stateJson.containsKey(idle)) {
					stateJson.put("idle", json);
				}
			}
		stateJson.put("floornames", floornames);
		return stateJson;
	}
	
	@RequestMapping(value = "inactivityNotify", method = RequestMethod.GET)
	public int inactivitNotify(@RequestParam(value = "cid", required = true) String cid,
			HttpServletRequest req,HttpServletResponse res) {
		
		int count = 0;
		if (SessionUtil.isAuthorized(req.getSession())) {
			Customer customer = customerService.findById(cid);
			if (customer !=null) {
				int inacDeviceCount 	= customer.getDeviceAlertCount();
				int inacTagsCount   	= customer.getTagAlertCount();
				int lowBatteryCount 	= customer.getBatteryAlertCount();
				count = inacDeviceCount + inacTagsCount + lowBatteryCount;
			}
		}
		
		return count;
	}
	
	   @GetMapping("/CrashDumpFileDownload")
	   public void downloadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam("filename") String fileName) {

		   
		   try {
			   
			   
			   String basePath = _CCC.properties.getProperty("facesix.fileio.binary.root", "/var/www/html");

				String domain = CustomerUtils.domain.locatum.name();
				String folder = customerUtils.createCrashDumpFolderName(domain);

				String path = basePath + "/" + folder + "/" + fileName;
				File file   = new File(path);

				if (file.exists()) {

					LOG.info(" Found file " + file.getName());

					String attachmentFileName = file.getName();
					String mimeType = URLConnection.guessContentTypeFromName(attachmentFileName);

					if (mimeType == null) {
						mimeType = "application/octet-stream";
					}

					response.setContentType(mimeType);
					response.setHeader("Content-Disposition",String.format("attachment; filename=\"" + attachmentFileName + "\""));
					response.setContentLength((int) file.length());
					InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
					FileCopyUtils.copy(inputStream, response.getOutputStream());

				} else {
					LOG.info("File not found....");
				}
				
		   } catch (Exception e) {
			   e.printStackTrace();
		   }
		   
		

	}
	
	public boolean  deviceDupmDetails(Map<String, Object> map) {
		try {
			
			String cid 				=  (String)map.get("cid");
			String uid 				=  (String)map.get("uid");
			int crash_timestamp 	=  (int)map.get("timestamp");
			String  daemon_info		=  (String)map.get("victim");
			String  version			=  (String)map.get("version");
			String  fileName		=  (String)map.get("filename");
			int  upload_status		=  (int)map.get("upload_status");
			String strUploadStatus 	= String.valueOf(upload_status);
			
			final String opcode 	= "device_crash_info";
			final String type   	= "device_crash";
			
			HashMap<String,Object> jsonMap = new HashMap<String,Object>();
			
			jsonMap.put("opcode",		 	opcode);
			jsonMap.put("uid",		     	uid);
			jsonMap.put("cid",  			cid);
			jsonMap.put("filename",  		fileName); 
			jsonMap.put("version",  		version); 
			jsonMap.put("daemon_info",  	daemon_info); 
			jsonMap.put("crash_timestamp",  crash_timestamp);
			jsonMap.put("upload_state",    strUploadStatus); 
			
			elasticService.post(device_history_event, type, jsonMap);  		
			
			jsonMap.clear();

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("while device crash details posting error " +e);
		}
		return true;

	}

	
	@RequestMapping(value = "/finder_Device_crash_info", method = RequestMethod.GET)
	public JSONArray  getDevice_crash_info(@RequestParam(value = "cid", required = true) String cid,
											@RequestParam(value = "time", required = true) String time) {
		try {
			
			final String opcode = "device_crash_info";
			final String type   = "device_crash";
			
			List<Map<String, Object>>  logs = EMPTY_LIST_MAP;
			
			if (time == null || time.isEmpty()) {
				time = "10d";
			}
			String size      = "500";
			
			String fsql = "index=" + device_history_event+",size=" + size + ",type="+type+",query=timestamp:>now-"+time+" AND opcode:"
					+ opcode + " AND ";
					
			List<BeaconDevice> devices = getBeaconDeviceService().findByCid(cid);
			if (devices != null) {
				String uidbuilder = buildBeaconDeviceArrayCondition(devices, "uid");
				fsql = fsql +uidbuilder;
			}
			fsql += " |value(uid,uid, NA);"
					+ " value(crash_timestamp,crash_timestamp,NA);"
					+ " value(cid,cid,NA);value(daemon_info,daemon_info,NA);"
					+ " value(version,version,NA);value(filename,filename,NA);value(upload_state,upload_state,NA);"
					+ " value(timestamp,time,NA);|table";
			
			logs   =  fsqlRestController.query(fsql);
			
			JSONObject object = null;
			JSONArray   array = new JSONArray();
			
			BeaconDevice beaconDevice =  null;
			
			if (logs !=null) {
				Iterator<Map<String, Object>> iterator = logs.iterator();
	    		while (iterator.hasNext()) {
	    			Map<String, Object> map = iterator.next();
	    			
	    			object = new JSONObject();
	    			
	    			String devUid 			= (String)map.get("uid");
	    			int crashTime 			= (int)map.get("crash_timestamp");
	    			String filename 		= (String)map.getOrDefault("filename","NA");
	    			String upload_status 	= (String)map.get("upload_state");
	    			
	    			String alias = "NA";
	    			beaconDevice = getBeaconDeviceService().findOneByUid(devUid);
					if (beaconDevice != null && beaconDevice.getUid().equalsIgnoreCase(devUid)) {
						alias = beaconDevice.getName();
					}
					
	    			object.put("uid", 		 devUid);
	    			object.put("crashTime",  crashTime);
	    			object.put("filename",   filename);
	    			object.put("alias",  	 alias);
	    			object.put("status_code",upload_status);
	    			array.add(object);
	    			
	    		}
				
			}
			
			
			return array;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	@RequestMapping(value = "/venue/taginfo", method = RequestMethod.GET)
	public JSONObject venue(@RequestParam(value = "cid", required = true) String cid,
									  @RequestParam(value = "sid", required = true) String sid,
									  HttpServletRequest request, HttpServletResponse response) {
		JSONObject venueDetails = new JSONObject();
		try {
			int activeTags = venuePeercount(sid, cid);
			int inactiveTags = inactiveTags(cid, sid, null);
			int idleTags = idleTags(cid, sid, null);
			int checkedoutTags = venueCheckedout(sid, cid);
			List<Map<String, Object>> floorVsTraffic = venueaggNew(sid, cid, request, response);
			JSONArray connectedTagType = connectedTagType(sid, null, null, cid);

			venueDetails.put("activeTags", activeTags);
			venueDetails.put("inactiveTags", inactiveTags);
			venueDetails.put("idleTags", idleTags);
			venueDetails.put("totalCheckedoutTags", checkedoutTags);
			venueDetails.put("floorVsTraffic", floorVsTraffic);
			venueDetails.put("connectedTagType", connectedTagType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return venueDetails;
	}

	@RequestMapping(value = "/floor/taginfo", method = RequestMethod.GET)
	public JSONObject floor(@RequestParam(value = "cid", required = true) String cid,
									  @RequestParam(value = "sid", required = false) String sid,
									  @RequestParam(value = "spid", required = false) String spid,
									  HttpServletRequest request, HttpServletResponse response) {
		
		JSONObject floorDetails = new JSONObject();
		
		try {
			
			int activeTags 	 = peercount(spid,cid);
			int inactiveTags = inactiveTags(cid,sid,spid);
			int idleTags 	 = idleTags(cid,sid,spid);
			
			JSONArray connectedTagType = connectedTagType(sid,spid,null,cid);
			floorDetails.put("activeTags", activeTags);
			floorDetails.put("inactiveTags", inactiveTags);
			floorDetails.put("idleTags", idleTags);
			floorDetails.put("connectedTagType", connectedTagType);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return floorDetails;
	}
	
	/**
	 * Generic gateways alerts(Locatum and gateway)
	 * 
	 * @param cid
	 * @param sid
	 * @param spid
	 * @param request
	 * @return
	 * @throws InterruptedException 
	 */
	
	@RequestMapping(value = "/gateway_alerts", method = RequestMethod.GET)
	public ArrayList<JSONObject> gatewayAlerts(
			@RequestParam(value = "cid", required = false) final String cid,
			@RequestParam(value = "sid", required = false) final String sid,
			@RequestParam(value = "spid", required = false) final String spid,
			HttpServletRequest request) {
		
		long start = System.currentTimeMillis();
		
		JSONArray alert   = new JSONArray();
		
		String workList[] = { "Tag", "Gateway", "Geofence", "SOS" };

		ExecutorService executorService = Executors.newFixedThreadPool(workList.length);
		
		try {

			for (int i = 0; i < workList.length; i++) {

				final String taskName = workList[i];
				final int queryTimeInHours = 1;

				executorService.execute(new Runnable() {
					@Override
					public void run() {

						switch (taskName) {

						case "Tag":

							final String status       = Beacon.STATUS.checkedout.name();
							List<Beacon> beaconsList  = null;

							if (spid != null) {
								beaconsList = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
							} else if (sid != null) {
								beaconsList = beaconService.getSavedBeaconBySidAndStatus(sid, status);
							} else {
								Collection<Beacon> beacon = beaconService.getSavedBeaconByCidAndStatus(cid, status);
								if (beacon != null) {
									beaconsList = new ArrayList<Beacon>();
									beaconsList.addAll(beacon);
								}
							}

							if (beaconsList != null) {
								beaconsList.parallelStream().forEach(ibeacon -> {

									String state 		= ibeacon.getState();
									String assignedTo 	= ibeacon.getAssignedTo();
									String lastSeen 	= ibeacon.getLastReportingTime();

										
									lastSeen = customerUtils.formatReportDate(lastSeen);
									
									if (StringUtils.isEmpty(lastSeen))
										lastSeen = "Tag was never reported";
									else
										lastSeen = "Last active time: " + lastSeen;

									if (state.equalsIgnoreCase("inactive")) {
										JSONObject object = new JSONObject();
										object.put("type", 	"Tag");
										object.put("text",	 assignedTo + " ---> "+lastSeen);
										object.put("time",	 lastSeen);
										alert.add(object);
									}

									int battery = ibeacon.getBattery_level();

									if (battery <= 40) {
										String batteryLevel = "level <= " + battery + "%";
										JSONObject object = new JSONObject();
										object.put("type", 		"Battery");
										object.put("text",		assignedTo + " ---> " +batteryLevel);
										alert.add(object);
									}

								});
							}
							break;
						case "Gateway":

							List<BeaconDevice> beaconDevice = null;
							List<Device> device 			= null;

							final String state = Beacon.STATE.inactive.name();

								if (sid != null) {
									device = deviceService.findBySidAndState(sid,state);
								} else if (spid != null) {
									device = deviceService.findBySpidAndState(spid,state);
								}

								if (device !=null) {

									device.forEach(dev -> {

										String lastSeen = dev.getLastseen();
										String time 	= StringUtils.isEmpty(lastSeen) ? "" : lastSeen;
										
										if (StringUtils.isEmpty(lastSeen))
											lastSeen = "Was never reported";
										else
											lastSeen = "Last active time: " + lastSeen;

										JSONObject object = new JSONObject();

										object.put("type", 		dev.getFstype());
										object.put("text",		dev.getName()+ " ---> " +lastSeen);
										object.put("time",	 time);
										alert.add(object);
									});

							}


								if (sid != null) {
									beaconDevice = getBeaconDeviceService().findBySidAndState(sid, state);
								} else if (spid != null) {
									beaconDevice = getBeaconDeviceService().findBySpidAndState(spid, state);
								}

								if (beaconDevice != null) {
									beaconDevice.forEach(dev -> {

										String lastSeen = dev.getLastseen();
										String time 	= StringUtils.isEmpty(lastSeen) ? "" : lastSeen;
										
										if (StringUtils.isEmpty(lastSeen))
											lastSeen = "Was never reported";
										else
											lastSeen = "Last active time:" + lastSeen;

										JSONObject object = new JSONObject();

										object.put("type", 	dev.getType());
										object.put("text", 	dev.getName() +" ---> " +lastSeen);
										object.put("time",	 time);
										alert.add(object);
									});
								}
							break;
						case "Geofence": {

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
										+ "value(event,event, NA);value(name,name,NA);value(triggertime,triggertime,NA);"
										+ "value(cid,cid,NA)|table;";
								List<Map<String, Object>> logs = fsqlRestController.query(fsql);

								if(logs != null && logs.size() > 0) {
									String type = "Tag";
									for(Map<String,Object> log:logs) {
										String event 			= log.get("event").toString();
										String assignedto	 	= log.get("assignedto").toString();
										String fencename 	= log.get("gName").toString();
										String triggertime 	= log.get("triggertime").toString();
										String cid 			= (String)log.get("cid");

										triggertime = customerUtils.formatReportDate(triggertime);
										
										if(event.equals("entry")) {
											event = "entered";
										}else {
											event = "exited";
										}
										String text = assignedto+" "+event+" "+fencename+" at "+triggertime;
										JSONObject object = new JSONObject();
										object.put("type", 	type);
										object.put("text",	text);
										object.put("time",	 triggertime);
										alert.add(object);
									}
								}
							}
							break;
						case "SOS": {

								String fsql = "index=" + sosAlertIndex + ",sort=timestamp desc,type=alert,query=timestamp:>now-" + queryTimeInHours + "h";

								if (!StringUtils.isEmpty(cid)) {
									fsql += " AND cid:" + cid;
								}
								if (!StringUtils.isEmpty(sid)) {
									fsql += " AND sid:" + sid;
								}
								if (!StringUtils.isEmpty(spid)) {
									fsql += " AND spid:" + spid;
								}

								fsql += "|value(tagid,tagid,NA);value(assignedto,assignedto,NA);value(floorname,floorname,NA);value(venuename,venuename, NA);"
										+ "value(locationname,locationname, NA);value(sosTime,sosTime,NA);value(cid,cid,NA);|table;";
								List<Map<String, Object>> logs = fsqlRestController.query(fsql);

								if(logs != null && logs.size() > 0) {
									String type = "Tag";
									String message = "";
									for (Map<String, Object> log : logs) {
										String assignedto = (String) log.get("assignedto");
										String floorname = (String) log.get("floorname");
										String venuename = (String) log.get("venuename");
										String locationname = (String) log.get("locationname");
										String sosTime 		= (String) log.get("sosTime");
										
										sosTime = customerUtils.formatReportDate(sosTime);
										message = "User " + assignedto + " made a SOS call from " + "Venue - " + venuename + "; Floor Name - "
												+ floorname + "; Location Name - " + locationname + " at " + sosTime;
										
										JSONObject object = new JSONObject();
										object.put("type", 	type);
										object.put("text",	message);
										object.put("time",	 sosTime);
										alert.add(object);
									}
								}
							}
							break;
						default:
							LOG.info("Unkown Task Name " +taskName );
							break;
						}

					}
				});

			}

			executorService.shutdown();
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<JSONObject> data = new ArrayList<JSONObject>();

		if (!CollectionUtils.isEmpty(alert)) {
			data = sortByTimestamp(alert);
		}
		
		long end = System.currentTimeMillis();
		
		long elp = end - start;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(elp);
	
		//LOG.info("elp milli seconds " + elp + " seconds " + seconds);

		return data;
	}
	
	
	
	/**
	 * Used to sorting the desc order of alerts
	 * @param data
	 * @return
	 */
	public ArrayList<JSONObject> sortByTimestamp(JSONArray array) {
		ArrayList<JSONObject> list = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
            list.add((JSONObject) array.get(i));
        }
		Collections.sort(list, new TimeStampComparator());
		return list;

	}
	
	/**
	 * third party gateways history
	 * @param cid
	 * @param uid
	 * @param time
	 * @return
	 */
	
	@RequestMapping(value = "/gatewaystatushistory", method = RequestMethod.GET)
	public JSONArray  gatewaystatushistory(
							@RequestParam(value = "uid", required = true) String uids[],
							@RequestParam(value = "time", required = true) String time) {
		
		JSONArray array = new JSONArray();
		
		try {
			
			final String limit ="\\^";
			
			final String opcode = "gateway_status";
			final String type   = "clu_gateway_status_history";
			
			if (StringUtils.isBlank(time)) {
				time = "1h";
			}
			
			String fsql = "index="+device_history_event+",sort=timestamp desc,size=500,type="+type+",query=timestamp:>now-"+time+" AND opcode:"
					+ opcode + " AND ";
			
			List<BeaconDevice> devices = null;
			
			if (uids != null) {
				List<String> macaddrs = Arrays.asList(uids);
				devices = beacondeviceService.findByUids(macaddrs);
			}
			
			if (CollectionUtils.isNotEmpty(devices)) {
				
				String uidbuilder = buildBeaconDeviceArrayCondition(devices, "uid");
				fsql = fsql +uidbuilder;
			
			fsql += " |value(uid,uid, NA); value(tagCount,tagCount,NA);value(serverSent,serverSent,NA);value(cloudSeen,cloudSeen,NA);"
					+ " value(timestamp,time,NA);|table";
			
			
			List<Map<String, Object>> data = fsqlRestController.query(fsql);
			
			if (CollectionUtils.isNotEmpty(data)) {
				
				HashMap<String, String> gatewayMap = new HashMap<String, String>();
				HashMap<String, String> siteMap = new HashMap<String, String>();
				HashMap<String, String> floorMap = new HashMap<String, String>();
				
				Iterator<Map<String, Object>> listIter = data.iterator();
				
				while (listIter.hasNext()) {
					
					Map<String, Object> record = listIter.next();
					
					String floorName = "unknown";
					String siteName  = "unknown";
					String location  = "unknown";
					
					String sid 		= null;
					String spid 	= null;
					
					String gatewyUid = (String)record.get("uid");
					
					if (gatewyUid != null) {
						if (gatewayMap.containsKey(gatewyUid)) {
							location = gatewayMap.get(gatewyUid);
						} else {
							BeaconDevice beaconDevice = beacondeviceService.findOneByUid(gatewyUid);
							if (beaconDevice != null) {
								location = beaconDevice.getName()+"^"+beaconDevice.getSid()+"^"+beaconDevice.getSpid();
							} else {
								location = location+"^"+"unknown"+"^"+"unknown";
							}
							
							gatewayMap.put(gatewyUid, location);
						}
					}
					
					String loc = "unknown";
					
					if (location.split(limit).length == 3) {
						loc 	 = location.split(limit)[0];
						sid 	 = location.split(limit)[1];
						spid 	 = location.split(limit)[2];
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
							Portion p = portionService.findById(spid);
							if (p != null) {
								floorName = p.getUid();
							}
							floorMap.put(spid, floorName);
						}
					}
					
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("uid", 	   gatewyUid);
					jsonObject.put("location", loc);
					jsonObject.put("site", siteName);
					jsonObject.put("floor", floorName);
					jsonObject.put("tagCount", record.get("tagCount"));
					jsonObject.put("serverSent", record.get("serverSent"));
					jsonObject.put("cloudSeen", record.get("cloudSeen"));
					
					array.add(jsonObject);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;

	}
}

 class TimeStampComparator implements Comparator<JSONObject> {
	@Override
	public int compare(JSONObject o1, JSONObject o2) {

		String valA = String.valueOf(o1.get("time"));
		String valB = String.valueOf(o2.get("time"));
		
		return valB.compareTo(valA);
	}
}
