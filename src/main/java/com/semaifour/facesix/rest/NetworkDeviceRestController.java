package com.semaifour.facesix.rest;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.util.FileCopyUtils;
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
import com.semaifour.facesix.beacon.rest.BLENetworkDeviceRestController;
import com.semaifour.facesix.beacon.rest.FinderReport;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.elasticsearch.ElasticsearchConfiguration;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.ClientDevice;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.probe.oui.ProbeOUI;
import com.semaifour.facesix.probe.oui.ProbeOUIService;
import com.semaifour.facesix.session.SessionCache;
import com.semaifour.facesix.stats.ClientCache;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

/**
 * 
 * Rest Device Controller handles all rest calls for network configuration
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/site/portion/networkdevice")
public class NetworkDeviceRestController extends WebController {
	
	static Logger LOG = LoggerFactory.getLogger(NetworkDeviceRestController.class.getName());
	static Font smallBold 	= new Font(Font.FontFamily.TIMES_ROMAN, 12, 	Font.BOLD);
    static Font catFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 18, 	Font.BOLD);
    static Font redFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 12, 	Font.NORMAL);
    static Font subFont 	= new Font(Font.FontFamily.TIMES_ROMAN, 16,     Font.BOLD);
    
    /**
     * NetworkBalancer data details 
     */
    
    static final String STEER_STA_LIST 				= "steer_sta_list";
    static final String NETWORK_BALANCER_STEEER_REQ = "network_balancer_steer_req";
    static final String STEER_BSS_LIST 				= "steer_bss_list";
    static final String STEER_INDEX_TYPE			= "networkBalance";
    
    
    static boolean ThreadTobeStarted = false;
    DateFormat parse 			   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    DateFormat format 			   = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	
	@Autowired
	DeviceService 	deviceService;
	
	@Autowired
	ClientDeviceService 	clientDeviceService;
	
	@Autowired
	SiteService siteService;
	
	@Autowired
	PortionService portionService;	
	   
	@Autowired
	FSqlRestController 		fsqlRestController;
	
	@Autowired
	SessionCache sessionCache;
	
	@Autowired
	CustomerUtils customerUtils;
	
	@Autowired
	BeaconService beaconService;
	
	@Autowired
	FinderReport trilaterationReport;
	
	@Autowired
	BeaconDeviceService beacondeviceservice;
	
	@Autowired
	ProbeOUIService probeOUIService;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	NetworkConfRestController networkConfRestController;

	@Autowired
	BLENetworkDeviceRestController bleNetworkDeviceRestController;
	
	@Autowired
	NmeshRetailerRestController nmeshRetailerRestController;
	
	@Autowired
	ElasticsearchConfiguration elasticsearchConfiguration;

	@Autowired
	ElasticService elasticService;
		
	@Autowired
	private ClientCache clientCache;
	
	private String indexname = "facesix*";

	String 	device_history_event = "device-history-event";
	
	private int blk_count   = 0;

	
	List<Map<String, Object>> peer_txrxlist = null;
	Map<String, Object> vap_map  			= null;
	Map<String, Object> vap5g_map  			= null;

	@PostConstruct
	public void init() {
		peer_txrxlist 	= new ArrayList<Map<String, Object>>();
		vap_map  		= new HashMap<String, Object>();
		vap5g_map		= new HashMap<String, Object>();

		indexname 		= _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
		device_history_event = _CCC.properties.getProperty("facesix.device.event.history.table",device_history_event);
	}
	
	public static String buildDeviceArrayCondition(List<Device> list, String fieldname) {
		if (list.size() > 0) {
    		StringBuilder sb = new StringBuilder(fieldname).append(":(");
    		boolean isFirst = true;
    		for (Device beacon : list) {
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
	
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/venuelist", method = RequestMethod.GET)
    public  int checkedout(@RequestParam(value="type",  required=true)String type,
    					   @RequestParam(value="cid", 	required=true)String cid,
    					   @RequestParam(value="sid", 	required=true)String sid) {
    	
    	
    	
    	JSONArray array = new JSONArray();
    	
    
    	List<Device> deviceList			= deviceService.findBySid(sid);
    	List<BeaconDevice> beaconDeviceList = beacondeviceservice.findBySid(sid);
    	
		if (deviceList != null) {
			deviceList.forEach(device -> {
				
    			JSONObject object = new JSONObject();
    			
    			String status = device.getStatus() == null ? "NA" : device.getStatus();
    			String typefs = device.getTypefs() == null ? "NA" : device.getTypefs();
    			String parent = device.parent == null ? "NA" : device.parent;
    			String role = device.getRole();
    			
    			if (typefs.equals("server") || typefs.equals("stwich")) {
    			} else {
    				object.put("status", status);
        			object.put("typefs",typefs);
        			object.put("parent", parent);
        			object.put("role", role);
        			array.add(object);
    			}
    			
    		});
    	}
    	
		if (beaconDeviceList != null) {
			beaconDeviceList.forEach(device -> {
				JSONObject object = new JSONObject();
    			
    			String status = device.getStatus() == null ? "NA" : device.getStatus();
    			String typefs = device.getTypefs() == null ? "NA" : device.getTypefs();
    			String parent = device.parent == null ? "NA" : device.parent;
    			
    			object.put("status", status);
    			object.put("typefs",typefs);
    			object.put("parent", parent);
    			
    			array.add(object);
    		});
    	}
    	
    	
    	
    	int device_count = 0;
    	
    	if (type.equals("1")) {
    		List<Portion> portionList = portionService.findBySiteId(sid);
	    	if (portionList.size() > 0 ) {
	    		device_count = portionList.size();
	    	}
    	}
    	
    	
    	
    	if (customerUtils.trilateration(cid) && type.equals("4")) {
			
    		String state   = Beacon.STATE.active.name();
			String status  = Beacon.STATUS.checkedout.name();
			
			List<Beacon> beaconList  = beaconService.getSavedBeaconByCidSidStateAndStatus(cid, sid, state, status);
			
			if (beaconList != null) {
				device_count = beaconList.size();
			}

		} else if (type.equals("4") && array != null) {

    			Iterator<JSONObject> iterator = array.iterator();
        			while (iterator.hasNext()) {
        				
        			    JSONObject obj = (JSONObject) iterator.next();
        			    String  status = (String)obj.get("status");
        			    String  typefs =  (String)obj.get("typefs");
            			String parent  = (String)obj.get("parent");
        			    
            			if (cid != null) {
    		    			if (customerUtils.Gateway(cid) || customerUtils.Heatmap(cid)) {
    		    				if (typefs.equals("ap") && status.equals("active")) {
    		    					device_count++;
    		    				}
    		    			} else if (customerUtils.GeoFinder(cid)) {
    		    				if ( (typefs.equals("sensor") && status.equals("active")) ||
    		    					 (parent.equals("ble") && status.equals("active"))) {
    		    					device_count++;
    		    				}
    		    				
    		    			} else {
    		    				if ( (typefs.equals("ap") && status.equals("active"))     ||
    		    					 (typefs.equals("sensor") && status.equals("active")) ||
    			    				 (parent.equals("ble") && status.equals("active"))) {
    		    						device_count++;
    		    				}
    		    			}
    	    			} else {
    	    				if (typefs.equals("ap") && status.equals("active")) {
    	    					device_count++;
    	    				}   				
    	    			}
        			}
        			
        	}  	
		
    	if (type.equals("2") || type.equals("3")) {
			if (array != null) {
				
				Iterator<JSONObject> iterator = array.iterator();
    			while (iterator.hasNext()) {
    				
    			    JSONObject obj  = (JSONObject) iterator.next();
    			    String  typefs  =  (String)obj.get("typefs");
        			String parent   = (String)obj.get("parent");
        			String role  	= (String)obj.get("role");
        			
        			if (type.equals("2")) {
		    			if (customerUtils.Gateway(cid) || customerUtils.Heatmap(cid)) {
		        			if (typefs.equals("ap") && (role != null && !role.equals("ap"))) {
		        				device_count++;
		        			}
		    			} else if (customerUtils.GeoFinder(cid)) {
		    				if (typefs.equals("sensor") || parent.equals("ble")) {
		    					device_count++;
		    				}
		    			} else {
		    				if (typefs.equals("ap")) {
		    					device_count++;
		    				}		    				
		    			}	    				
	    			} else {
		    			if (customerUtils.Gateway(cid) || customerUtils.Heatmap(cid)) {
		    				if (typefs.equals("ap") && (role == null || role.equals("ap"))) {
		        				device_count++;
		        			}

		    			} else if (customerUtils.GeoFinder(cid)) {
		    				if (typefs.equals("sensor") || parent.equals("ble")) {
		    					//device_count = device_count + nd.getCheckedoutTag();
		    				}

		    			} else {
		    				if (typefs.equals("sensor") || parent.equals("ble")) {
		    					device_count++;
		    				}        				
		    				
		    			}	    				
	    				
	    			}
        			
    			}
			}   		
    	}

		if (customerUtils.trilateration(cid)) {
			if (type.equals("3")) {
				String status  = Beacon.STATUS.checkedout.name();
				String state   = Beacon.STATE.inactive.name();
				
				// Total Tag checkout for particular venue
				
				List<Beacon> total_beacon 	 = beaconService.getSavedBeaconBySidAndStatus(sid, status);
				if (CollectionUtils.isNotEmpty(total_beacon)) {
					device_count = total_beacon.size();
				} 
			}
		}		
		
    	return device_count;
    }
	
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/gatewayMetrics", method = RequestMethod.GET)
    public JSONObject gatewayMetrics(
 		   @RequestParam(value="cid", 	required=true)String cid,
 		   @RequestParam(value="sid", 	required=true)String sid) {

 	   long start = System.currentTimeMillis();

 	   JSONObject payload = new JSONObject();

 	   String workList[] = { "FloorCount", "Checked-out-Tag-Count", "Active-Tag-Count", "GatewayCount" };

 	   ExecutorService executorService = Executors.newFixedThreadPool(workList.length);

 	   payload.put("floorCount",   0);
 	   payload.put("gatewayCount", 0);
 	   payload.put("checkedoutTagCount", 0);
 	   payload.put("activeTagCount", 0);

 	   for (int i = 0; i < workList.length; i++) {

 		  // LOG.info("gatewayMetrics Running " + Thread.currentThread().getName());

 		   final String taskName = workList[i];

 		   executorService.execute(new Runnable() {
 			   @Override
 			   public void run() {
 				   
 				   switch (taskName) {
 				   
 				   case "FloorCount":
 					   List<Portion> portionList = portionService.findBySiteId(sid);
 					   if (portionList.size() > 0) {
 						   payload.put("floorCount", portionList.size());
 					   }
 					   break;
 				   case "Checked-out-Tag-Count": {
 					   
 					  final String status  	 = Beacon.STATUS.checkedout.name();
 					  int checkedOutTagCount = 0;
 					   
 					   List<Beacon> beaconList 	 = beaconService.getSavedBeaconBySidAndStatus(sid, status);
 					   
 					   if (beaconList !=null) {
 						  checkedOutTagCount = beaconList.size();
 					   }
 					   
 					   payload.put("checkedoutTagCount", checkedOutTagCount);
 				   }
 				   break;
 				   case "Active-Tag-Count":	{
 					   
 					   String status  = Beacon.STATUS.checkedout.name();

 					  List<String> tagState = CustomerUtils.getLocatumActiveTagStatus();
 					  
 					   List<Beacon> beaconList  = beaconService.getCidSidStateAndStatus(cid, sid, tagState, status);

 					   if (beaconList != null) {
 						   payload.put("activeTagCount",beaconList.size());
 					   }
 				   }
 				   break;
 				   case "GatewayCount":
 					   
 					   final String gatewayType = BeaconDevice.GATEWAY_TYPE.receiver.name();
 					   List<BeaconDevice> beaconDeviceList = beacondeviceservice.findBySidAndType(sid,gatewayType);

 					   if (beaconDeviceList !=null) {
 						   payload.put("gatewayCount",beaconDeviceList.size());
 					   }
 					   break;
 				   default:
 					   break;
 				   }
 			   }
 		   });
 	   }

 		executorService.shutdown();
 		try {
 			// Blocks until all 100 submitted threads have finished!
 			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	   long end = System.currentTimeMillis();

 	   long elp = end - start;
 	   long seconds = TimeUnit.MILLISECONDS.toSeconds(elp);

 	   LOG.info("elp milli seconds for venue Metrics " + elp + " seconds " + seconds);

 	   return payload;
    }
    
    @RequestMapping(value = "/peercount", method = RequestMethod.GET)
    public  int peercount(@RequestParam(value="cid",  required=false)String cid,
    					  @RequestParam(value="sid",  required=false)String sid,
    					  @RequestParam(value="spid", required=false)String spid, 
    					  @RequestParam(value="swid", required=false)String swid) {
    
    	int device_count = 0;
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}
    	
    	List<Device> devices = deviceService.findBy(spid, sid, swid);
    	
    	if (devices == null) {
    		return  0;
    	}
    	
		List<Device> mlist  = Collections.unmodifiableList(devices);
		List<Device> list   = new ArrayList<Device>(mlist);  
    	
		if (list.size() > 0) {
			String state  = Device.STATE.active.name();
    		for (Device nd : list) { 
    			
    			String status = nd.getStatus() == null ? "NA" : nd.getStatus();
    			String typefs = nd.getTypefs() == null ? "NA" : nd.getTypefs();
    			
    			if (cid != null) {
    				
	    			if (customerUtils.Gateway(cid)) {
	    				if (typefs.equals("ap") && status.equals(state)) {
	    					device_count++;
	    				}
	    			} else if (customerUtils.GeoFinder(cid)) {
	    				if ( (typefs.equals("sensor") && status.equals(state)) ||
	    					 (nd.parent.equals("ble") && status.equals(state))) {
	    					device_count++;
	    				}
	    				
	    			} else {
	    				if ( (typefs.equals("ap") && status.equals(state))     ||
	    					 (typefs.equals("sensor") && status.equals(state)) ||
		    				 (nd.parent.equals("ble") && status.equals(state))) {
	    						device_count++;
	    				}
	    			}
    			} else {
    				if (typefs.equals("ap") && status.equals(state)) {
    					device_count++;
    				}   				
    			}
    			
    		}
		}
		
    	return device_count;
    }
        
  public String size(String place) {
		String size = "10";
		if (place != null && place.equals("htmlchart")) {
			size = "2000";
		}
		return size;
	}
  
    
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/rxtx", method = RequestMethod.GET)
  public  List<net.sf.json.JSONObject> rxtx(
		  							   @RequestParam(value="uid",  required=true)  String uid,
  								 	   @RequestParam(value="time", required=false, defaultValue="30m") String time,
  								 	   HttpServletRequest request, HttpServletResponse response) {
  	
		  	
		  	JSONObject data_json_object = null;
		  	JSONArray data_array = new JSONArray();
		  	
		if (SessionUtil.isAuthorized(request.getSession())) {

			//LOG.info("time " + time);

			try {
  			
          	Device device =  deviceService.findOneByUid(uid);
  	
				if (device != null) {
  				
					String cid = device.getCid();
					
					DateFormat hhmmss = customerUtils.set_date_format_hh_mm_ss(cid);
		  			parse.setTimeZone(TimeZone.getTimeZone("UTC"));
		  			
					HashMap<String,HashMap<String,String>> metrics_map = new HashMap<String,HashMap<String,String>>();
      				
      						int vap2G = device.getVap2gcount();
      						int vap5G = device.getVap5gcount();
      						
      						for (int i = 0; i < vap2G; i++) {
      							
      							String query = "index="+indexname +",sort=timestamp desc,size=20,query=timestamp:>now-"+time+" AND "
      									 +" uid:\""+uid+"\" AND vap_id:\""+i+"\" AND radio_type:\"2.4Ghz\"|"
      									+ " value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA);|table,sort=Date:desc;";

      							List<Map<String,Object>> _2g_logs = fsqlRestController.query(query);
      							
      							//LOG.info("_2g_logs " +_2g_logs);
      							
      							if (_2g_logs != null && _2g_logs.size() > 0) {
      								
      								Iterator<Map<String, Object>> it = _2g_logs.iterator();
      								
      								while (it.hasNext()) {
      									
      									Map<String, Object> data = it.next();
      							      	
          								String  tx 			= data.get("Tx").toString();
          								String rx 			= data.get("Rx").toString();
          								String _2g_timestamp     	= (String)data.get("time");

          								Date date_time_stamp = parse.parse(_2g_timestamp);
          								_2g_timestamp	     = hhmmss.format(date_time_stamp);

    									if (metrics_map.containsKey(_2g_timestamp)) {

    										HashMap<String, String> metrics = metrics_map.get(_2g_timestamp);

    										String mat_tx  = metrics.get("Tx");
    										String mat_rx  = metrics.get("Rx");
    										
											double data_tx = Double.valueOf(tx) + Double.valueOf(mat_tx);
											double data_rx = Double.valueOf(rx) + Double.valueOf(mat_rx);

    										metrics.put("Tx", String.valueOf(data_tx));
    										metrics.put("Rx", String.valueOf(data_rx));
    										
    										metrics_map.put(_2g_timestamp, metrics);
    										
    									} else {
    										HashMap<String, String> metrics = new HashMap<String, String>();
    										metrics.put("Tx", 		String.valueOf(tx));
    										metrics.put("Rx", 		String.valueOf(rx));
    										metrics.put("Time", 	_2g_timestamp);
    										metrics_map.put(_2g_timestamp, metrics);
										}
									}

								}
							}
      						
      						for (int i = 0; i < vap5G; i++) {
      							
      							String query = "index="+indexname +",sort=timestamp desc,size=20,query=timestamp:>now-"+time+" AND "
      										+ "uid:\""+uid+"\" AND vap_id:\""+i+"\" AND radio_type:\"5Ghz\"|"
      										+ "value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA);|table,sort=Date:desc;";

      							List<Map<String, Object>> _5g_logs = fsqlRestController.query(query);
      							
      							//LOG.info("_5g_logs " +_5g_logs);
      							
      							if (_5g_logs != null && _5g_logs.size() >0) {

      								Iterator<Map<String, Object>> it = _5g_logs.iterator();
      								
      								while (it.hasNext()) {
      									
      									Map<String, Object> data = it.next();
      									
      									String tx 					=  data.get("Tx").toString();
          								String rx 					=  data.get("Rx").toString();
          								
          								String _5g_timestamp    = (String)data.get("time");
          								
          								Date date_time_stamp = parse.parse(_5g_timestamp);
          								_5g_timestamp	     = hhmmss.format(date_time_stamp);

    									if (metrics_map.containsKey(_5g_timestamp)) {

    										HashMap<String, String> metrics = metrics_map.get(_5g_timestamp);

    										String mat_tx  = metrics.get("Tx");
    										String mat_rx  = metrics.get("Rx");
    										
    										double data_tx = Double.valueOf(tx) + Double.valueOf(mat_tx);
    										double data_rx = Double.valueOf(rx) + Double.valueOf(mat_rx);

    										metrics.put("Tx", String.valueOf(data_tx));
    										metrics.put("Rx", String.valueOf(data_rx));

    										metrics_map.put(_5g_timestamp, metrics);
    										
    									} else {
    										HashMap<String, String> metrics = new HashMap<String, String>();
    										metrics.put("Tx", 		String.valueOf(tx));
    										metrics.put("Rx", 		String.valueOf(rx));
    										metrics.put("Time", 	_5g_timestamp);
    										metrics_map.put(_5g_timestamp, metrics);
										}

									}

								}
							}
					

					int count = 0;

					if (metrics_map != null) {

						Iterator<Map.Entry<String, HashMap<String, String>>> parent = metrics_map.entrySet().iterator();

						while (parent.hasNext()) {

							count++;

							if (count == 10) {
								break;
							}

							data_json_object = new JSONObject();
							
							Map.Entry<String, HashMap<String, String>> parentPair = parent.next();
							
							HashMap<String, String> map = parentPair.getValue();
							String tx = map.get("Tx");
							String rx = map.get("Rx");
							String datatime =parentPair.getKey();
							
							data_json_object.put("Tx",tx);
							data_json_object.put("Rx", rx);
							data_json_object.put("time", datatime);
							data_array.add(data_json_object);
							
						}
					}
      				
				}

			} catch (Exception e) {
				LOG.error("While Processing TX And RX occurred error " + e);
				e.printStackTrace();
			}
		}

		List<net.sf.json.JSONObject> result = null;

		if (data_array != null) {
			result = sortByTimestamp(data_array, "time");
		}

		// LOG.info("result - " + result);

		return result;

	}
  
  private List<net.sf.json.JSONObject> sortByTimestamp(JSONArray data,final String key) {
		
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
	            return valA.compareTo(valB);
	        }
	    });
		
		return jsonValues;

	}
  

    @RequestMapping(value = "/venueagg", method = RequestMethod.GET)
	public List<Map<String, Object>> venueagg(@RequestParam(value = "sid", required = true) String sid, HttpServletRequest request, HttpServletResponse response) {
    
    	List<net.sf.json.JSONObject> rxtx = new ArrayList<net.sf.json.JSONObject>();
    	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
    	
    	
    	try {
    		
    		
			List<Portion> portion = portionService.findBySiteId(sid);
			
			if (portion != null) {
				Collections.sort(portion);   
				for (Portion p : portion) {
					
					String spid  = p.getId();
					String cid   = p.getCid();
					String floor_name = p.getUid();
					
					 rxtx = this.avg_tx_rx(null, spid, null, "30m", cid, request, response);
					 
					 Map<String, Object> map = new HashMap<String,Object>();
					 
					if (rxtx != null && rxtx.size() > 0) {

						net.sf.json.JSONObject object = rxtx.get(0);

						map.put("Floor", floor_name);
						map.put("Tx", 	object.get("Tx"));
						map.put("Rx", 	object.get("Rx"));
						map.put("time", object.get("time"));
						ret.add(map);
					}
				}
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	    	
    	//LOG.info("VENUE MAP STR" + ret.toString());
    	return ret;
    }
    
   
    @RequestMapping(value = "/flraggr", method = RequestMethod.GET)
    public List<Map<String, Object>> flraggr(
    										 @RequestParam(value="sid",  required=false) String sid,
    										 @RequestParam(value="spid", required=false) String spid, 
    										 @RequestParam(value="time", required=false, defaultValue="120") String time,
    										 @RequestParam(value="cid",  required=false) String cid,
    										 HttpServletRequest request, HttpServletResponse response) {
    
    	Map<String, Object> map = null;
    	List<Map<String, Object>> rxtx = null;
    	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
    	if (sid == null) {
        	//rxtx = rxtxagg(null, spid, null, null, time, "20", "2G", cid, request, response);    		
    	} else {
        	//rxtx = rxtxagg(sid, null, null, null, time, "20", "2G",cid, request, response);
    	}
    	
    	//LOG.info("RXTX RES" + rxtx);
	/*	if (rxtx.size() > 0 ) {
			map = rxtx.get(0);
			map.put("Radio", "2G");
			ret.add(map);
		}*/
    	if (sid == null) {
        	//rxtx = rxtxagg(null, spid, null, null, time, "20", "5G", cid, request, response);    		
    	} else {
        	//rxtx = rxtxagg(sid, null, null, null, time, "20", "5G", cid, request, response);
    	}
    	//LOG.info("RXTX RES11111111" + rxtx);
		/*if (rxtx.size() > 0 ) {
			map = rxtx.get(0);
			map.put("Radio", "5G");
			ret.add(map);
		}*/
    	    	
    	//LOG.info("FLOOR MAP STR" + ret.toString());
    	return ret;
    }    
    
    
    @RequestMapping(value = "/peeraggr", method = RequestMethod.GET)
    public List<Map<String, Object>> peeraggr(@RequestParam(value="uid", required=true) String uid, 
    										  @RequestParam(value="time", required=false, defaultValue="120") String time) {
    
    	Map<String, Object> map = null;
    	if (peer_txrxlist.size() > 0) {
	    	map = peer_txrxlist.get(0);
	    	String id = (String) map.get("uid");
	    	if (uid.equals(id)) {
	    		return peer_txrxlist;
	    	}
    	}
    	
    	return EMPTY_LIST_MAP;
    }     
    
    
    /*@SuppressWarnings("unused")
	@RequestMapping(value = "/rxtxagg", method = RequestMethod.GET)
    public   List<Map<String, Object>> rxtxagg(@RequestParam(value="sid", required=false) String sid, 
    								 	      @RequestParam(value="spid", required=false) String spid,
    								 	      @RequestParam(value="uid", required=false) String uid,
    								 	      @RequestParam(value="swid", required=false) String swid,
    								 	      @RequestParam(value="time", required=false, defaultValue="5") String time,
    								 	      @RequestParam(value="interval", required=false, defaultValue="2") String interval,
    								 	      @RequestParam(value="radio", required=false, defaultValue="2G5G") String radio,
    								 	     @RequestParam(value="cid", required=false) String cid,
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
    	
    	List<Device> devices = networkDeviceService.findBy(spid, sid, swid);
    	//LOG.info("Device" + devices);
    	if (devices != null && uid == null) {
    		String uidBuidler = buildArrayCondition(devices, "uid");
    		//LOG.info("dddd" + uidBuidler);
    		if (uidBuidler.length() > 0) {
    			esql = esql + uidBuidler;
    		} else {
    			return EMPTY_LIST_MAP;
    		}
        	//esql = esql + buildArrayCondition(devices, "uid");
        	
    	} else if (uid != null) {
    		esql = esql + "uid:\"" + uid + "\"";
    	}
    	
    	if (radio.equals("2G")) {
        	esql = esql + "AND radio_type:\"2.4Ghz\"";
    	} else if (radio.equals("5G")) {
        	esql = esql + "AND radio_type:\"5Ghz\"";
    	} else {
        	esql = esql + "AND radio_type:(\"5Ghz\" OR \"2.4Ghz\")";
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
	    	
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    	
	    	DateFormat hhmmss = CustomerUtils.set_date_format_hh_mm_ss(cid);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			
	    	List<Map<String, Object>> rxtx = new ArrayList<Map<String, Object>>();
	    	Map<String, Object> map = null;
	    	
	    	for (Histogram.Bucket entry : histogram.getBuckets()) {
	    		
				try {
					
					count++;
					
		    		map = new HashMap<String, Object>();
		    		
		    		String eldate 			= entry.getKey().toString();
					Date date_time_stamp 	= sdf.parse(eldate);
					String timestamp 	     = hhmmss.format(date_time_stamp);
					
		    		map.put("time",timestamp);
		    		
		    		List<Aggregation> aggs = entry.getAggregations().asList();
		    		for(Aggregation agg : aggs) {
		    			String name = agg.getName();
		    			map.put(agg.getName(), ((SingleValue)agg).value());
		    			
		    		}
		    		
		    		rxtx.add(map);
		    		
		    		if (count >= 10) {
		    			break;
		    		}
		    		
				} catch (Exception e) {
					e.printStackTrace();
				}
	    		
	    	}
	    	
	    	//LOG.info("RXTX AFF==>" + rxtx);
	    	
	    	return rxtx;
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    }*/
    
    
    @SuppressWarnings("unused")
	@RequestMapping(value = "/avg_tx_rx", method = RequestMethod.GET)
	public List<net.sf.json.JSONObject> avg_tx_rx(
							  @RequestParam(value = "sid", required = false) String sid,
							  @RequestParam(value = "spid", required = false) String spid,
							  @RequestParam(value = "uid", required = false) String uid,
							  @RequestParam(value = "time", required = false, defaultValue = "20m") String time,
							  @RequestParam(value = "cid", required = false) String cid,
    						  HttpServletRequest request, HttpServletResponse response) {
    	
    	
    	JSONObject json_bject = new JSONObject();
    	JSONArray  data_array 		= new JSONArray();
    	
    	if (SessionUtil.isAuthorized(request.getSession())) {
    		
    		try {
    	    	
    	    	
    			JSONObject data_json_object = null;
            	
            	List<Device> devices = null;

				if (spid != null) {
					devices = deviceService.findBySpid(spid);
				} else if (sid != null) {
					devices = deviceService.findBySid(sid);
				} else if (uid != null) {
					devices = deviceService.findByUid(uid);
				}

				DateFormat hhmmss = customerUtils.set_date_format_hh_mm_ss(cid);
	  			parse.setTimeZone(TimeZone.getTimeZone("UTC"));
    			
    	    	HashMap<String,HashMap<String,String>> metrics_map = new HashMap<String,HashMap<String,String>>();
    	    	
    	    	if (devices != null) {
      				
    	    		DecimalFormat decimalFormat = new DecimalFormat("#.##");
    	    		
      				for(Device dev : devices) {
      						
      						int vap2G = dev.getVap2gcount();
      						int vap5G = dev.getVap5gcount();
      						
      						String dev_uid = dev.getUid();
      						
      						for (int i = 0; i < vap2G; i++) {
      							
      							String query = "index="+indexname +",sort=timestamp desc,size=20,query=timestamp:>now-"+time+" AND "
      									 +" uid:\""+dev_uid+"\" AND vap_id:\""+i+"\" AND radio_type:\"2.4Ghz\"|"
      									+ " value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA);|table,sort=Date:desc;";

      							List<Map<String,Object>> _2g_logs = fsqlRestController.query(query);
      							
      							//LOG.info("_2g_logs " +_2g_logs);
      							
      							if (_2g_logs != null && _2g_logs.size() > 0) {
      								
      								Iterator<Map<String, Object>> it = _2g_logs.iterator();
      								
      								while (it.hasNext()) {
      									
      									Map<String, Object> data = it.next();
      							      	
          								String tx 			=  data.get("Tx").toString();
          								String rx 			=  data.get("Rx").toString();
          								
          								String _2g_timestamp     	= (String)data.get("time");

          								Date date_time_stamp = parse.parse(_2g_timestamp);
          								_2g_timestamp	     = hhmmss.format(date_time_stamp);

    									if (metrics_map.containsKey(_2g_timestamp)) {

    										HashMap<String, String> metrics = metrics_map.get(_2g_timestamp);

    										String mat_tx  = metrics.get("avg_tx");
    										String mat_rx  = metrics.get("avg_rx");
    										
    										double data_tx =  Double.valueOf(tx);
    										double data_rx =  Double.valueOf(rx);

    										double avg_tx = data_tx + Double.valueOf(mat_tx)/2;
    										double avg_rx = data_rx + Double.valueOf(mat_rx)/2;
    										
    										//LOG.info(" avg_tx " +decimalFormat.format(avg_tx) +" avg_rx " +decimalFormat.format(avg_rx));
    										
    										metrics.put("avg_tx", decimalFormat.format(avg_tx));
    										metrics.put("avg_rx", decimalFormat.format(avg_rx));
    										
    										metrics_map.put(_2g_timestamp, metrics);
    										
    									} else {
    										HashMap<String, String> metrics = new HashMap<String, String>();
    										metrics.put("avg_tx", 		String.valueOf(tx));
    										metrics.put("avg_rx", 		String.valueOf(rx));
    										metrics_map.put(_2g_timestamp, metrics);
    									}
    								}

    							}
    						}
      						
      						for (int i = 0; i < vap5G; i++) {
      							
      							String query = "index="+indexname +",sort=timestamp desc,size=20,query=timestamp:>now-"+time+" AND "
      										+ "uid:\""+dev_uid+"\" AND vap_id:\""+i+"\" AND radio_type:\"5Ghz\"|"
      										+ "value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA);|table,sort=Date:desc;";

      							List<Map<String, Object>> _5g_logs = fsqlRestController.query(query);
      							
      							//LOG.info("_5g_logs " +_5g_logs);
      							
      							if (_5g_logs != null && _5g_logs.size() >0) {

      								Iterator<Map<String, Object>> it = _5g_logs.iterator();
      								
      								while (it.hasNext()) {
      									
      									Map<String, Object> data = it.next();
      									
      									String tx 					= data.get("Tx").toString();
      									String rx 					= data.get("Rx").toString();
          								String _5g_timestamp    = (String)data.get("time");
          								
          								Date date_time_stamp = parse.parse(_5g_timestamp);
          								_5g_timestamp	     = hhmmss.format(date_time_stamp);

    									if (metrics_map.containsKey(_5g_timestamp)) {

    										HashMap<String, String> metrics = metrics_map.get(_5g_timestamp);

    										String mat_tx  = metrics.get("avg_tx");
    										String mat_rx  = metrics.get("avg_rx");
    										
    										double avg_tx = Double.valueOf(tx) + Double.valueOf(mat_tx)/2;
    										double avg_rx = Double.valueOf(rx) + Double.valueOf(mat_rx)/2;

    										metrics.put("avg_tx", decimalFormat.format(avg_tx));
    										metrics.put("avg_rx", decimalFormat.format(avg_rx));

    										metrics_map.put(_5g_timestamp, metrics);
    										
    									} else {
    										HashMap<String, String> metrics = new HashMap<String, String>();
    										metrics.put("avg_tx", 		String.valueOf(tx));
    										metrics.put("avg_rx", 		String.valueOf(rx));
    										metrics.put("Time", 		_5g_timestamp);
    										metrics_map.put(_5g_timestamp, metrics);
    									}

    								}

    							}
    						}
    					}
    				}
      				
      				//LOG.info("AVG TX RX metrics_map " + metrics_map);
      				
      				int count = 0;
      				
    				if (metrics_map != null) {
    				
    					Iterator<Map.Entry<String, HashMap<String, String>>> parent = metrics_map.entrySet().iterator();
    					
    					while (parent.hasNext()) {
    						
    						count ++;
    						
    						if (count == 10) {
    							break;
    						}
    						
    						data_json_object = new JSONObject();
    						
    						Map.Entry<String, HashMap<String, String>> parentPair = parent.next();
    						
    						HashMap<String, String> map = parentPair.getValue();
    						String tx = map.get("avg_tx");
    						String rx = map.get("avg_rx");
    						String datatime =parentPair.getKey();
    						
    						data_json_object.put("Tx",tx);
    						data_json_object.put("Rx",rx);
    						data_json_object.put("time", datatime);
    						data_array.add(data_json_object);
    						
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List<net.sf.json.JSONObject> result = null;

		if (data_array != null) {
			result = sortByTimestamp(data_array, "time");
		}

		//LOG.info("FLOOR AVG TX RX " + result);
		
		return result;
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

    	List<Device> devices = deviceService.findBy(spid, sid, swid);
    	    	
    	if (devices != null && uid == null) {
    		String uidbuilder = buildDeviceArrayCondition(devices, "uid");
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
    	
    	List<Device> devices = deviceService.findBy(spid, sid, swid);
    	
    	if (devices != null && uid == null) {
    		String uidBuilder = buildDeviceArrayCondition(devices, "uid");
    		if (uidBuilder.length() > 0 ) {
    			esql = esql + uidBuilder;
    		} else {
    			return EMPTY_LIST_MAP;
    		}
    		//esql = esql + buildArrayCondition(devices, "uid");
        	
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
    public  List<String> alerts(@RequestParam(value="cid",  required=false) String cid,
    							@RequestParam(value="sid",  required=false) String sid, 
    							@RequestParam(value="spid", required=false) String spid,
    							HttpServletRequest request) {

		String str					    = null;
		List<BeaconDevice> beaconDevice = null;
		List<Device> device 			= null;
		ArrayList<String> alert 	    = new ArrayList<String>();

		final String state = "inactive";
		
		if (cid == null || cid.isEmpty()) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		
		Customer customer = customerService.findById(cid);
		if (customer != null) {
			TimeZone totimezone = customerUtils.FetchTimeZone(customer.getTimezone());
			format.setTimeZone(totimezone);
		} else {
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		if (customerUtils.Gateway(cid) || customerUtils.GatewayFinder(cid)) {

			if (sid != null) {
				device = deviceService.findBySidAndState(sid,state);
			} else if (spid != null) {
				device = deviceService.findBySpidAndState(spid,state);
			}

			for (Device d : device) {
				String uid = d.getUid();
				String type = d.getTypefs();
				if (StringUtils.isNotBlank(type) && type.equals("server") || type.equals("switch")) {
					continue;
				}
				str = "AP (Mac id: " + uid.toUpperCase() + ") Status : " + "inactive";
				alert.add(str);
			}
		}
		
		if (customerUtils.GeoFinder(cid) || customerUtils.GatewayFinder(cid)) {
			if (sid != null) {
				beaconDevice = beacondeviceservice.findBySidAndState(sid,state);
			} else if (spid != null) {
				beaconDevice = beacondeviceservice.findBySpidAndState(spid,state);
			}

			for (BeaconDevice beaconDev : beaconDevice) {
				String source = beaconDev.getSource() == null ? "qubercomm" : beaconDev.getSource();
				String lastSeen = beaconDev.getLastseen();
				if (!source.equals("qubercomm")) {
					continue;
				}
				if (StringUtils.isNotEmpty(lastSeen)) {
					lastSeen = "Last Active Time :" + lastSeen +" "+format.getTimeZone().getDisplayName(false, 0);
				} else {
					lastSeen = "Gateway was never active";
				}
				String uid = beaconDev.getUid();
				str = beaconDev.getType().toUpperCase() + "( " + uid + " ) ---> " + lastSeen;
				alert.add(str);
			}
		}
		return alert;
	}
    
    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public  List<Map<String, Object>> activity(@RequestParam(value="sid",  		required=false) String sid, 
    								 	       @RequestParam(value="spid", 		required=false) String spid,
    								 	       @RequestParam(value="swid", 		required=false) String swid,
    								 	       @RequestParam(value="uid",  		required=false) String uid,
    								 	       @RequestParam(value="duration", 	required=false, defaultValue="24h") String duration) {
    	
    	List<Map<String, Object>>  logs  = EMPTY_LIST_MAP;
    	//String fsql = "index=" + indexname + ",sort=timestamp desc,size=25,query=log_type:\"log\"|value(message,snapshot,NA);value(timestamp,time,NA);|table";
    	String fsql = "index=" + indexname + ",sort=timestamp desc,query=doctype:\"syslog\" AND timestamp:>now-" + duration + " |value(message,snapshot,NA);value(timestamp,time,NA);|table";
    	
    	logs = fsqlRestController.query(fsql);
		    	    	
    	return logs;
    }   
    
    
    @RequestMapping(value = "/loginfo", method = RequestMethod.GET)
    public  List<Map<String, Object>> loginfo(@RequestParam(value="sid",  		required=false) String sid, 
    								 	       @RequestParam(value="spid", 		required=false) String spid,
    								 	       @RequestParam(value="swid", 		required=false) String swid,
    								 	       @RequestParam(value="uid",  		required=false) String uid,
    								 	       @RequestParam(value="duration", 	required=false, defaultValue="30m") String duration) {
    	
    	List<Map<String, Object>>  logs  = EMPTY_LIST_MAP;
    	String fsql = "index=" + indexname + ",sort=timestamp desc,size=25,query=log_type:\"log\"|value(message,snapshot,NA);value(timestamp,time,NA);|table";
    	//String fsql = "index=" + indexname + ",sort=timestamp desc,size=25,query=doctype:\"syslog\"|value(message,snapshot,NA);value(timestamp,time,NA);|table";
    	
    	logs = fsqlRestController.query(fsql);
		    	    	
    	return logs;
    }     

	public String recordSize(String place,String duration) {
		
		String size = "1";
		
		
		
		if (duration == null) {
			duration = "4h";
		}

		if (place != null && place.equals("htmlchart")) {
			if (duration.equals("4h")) {
				size = "10";
			} else if (duration.equals("6h")) {
				size = "20";
			} else if (duration.equals("8h")) {
				size = "30";
			} else if (duration.equals("12h")) {
				size = "40";
			} else {
				size = "50";
			}
		}
		return size;
	}
	
    @RequestMapping(value = "/getcpu", method = RequestMethod.GET)
    public  List<Map<String, Object>> getcpu(@RequestParam(value="sid",  required=false) String sid, 
    								 	     @RequestParam(value="spid", required=false) String spid,
    								 	     @RequestParam(value="swid", required=false) String swid,
    								 	     @RequestParam(value="uid",  required=false) String uid,
    								 	     @RequestParam(value="duration", required=false, defaultValue="4m") String duration,
    								 	    @RequestParam(value="place",  required=false) String place) {
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}

    	String size = recordSize(place,duration);
    	
    	List<Device> devices = deviceService.findBy(spid, sid, swid);
		String fsql = "index=" + indexname + ",sort=timestamp desc,size="+size+",query=cpu_stats:\"Qubercloud Manager\" AND timestamp:>now-" + duration + " AND ";

    	if (devices != null && devices.size() > 0 && uid == null) {
    		
    		String uidBuilder = buildDeviceArrayCondition(devices, "uid") + "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";
    		if (uidBuilder.length() > 0 ) {
    			fsql = fsql + uidBuilder;
    		} else {
    			return EMPTY_LIST_MAP;
    		}
    	
    		//fsql = fsql + buildArrayCondition(devices, "uid") + "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";

    		return fsqlRestController.query(fsql);
    	} else if (uid != null){
    		fsql = fsql + "uid:\"" + uid + "\"";
    		fsql = fsql + "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";

    		return fsqlRestController.query(fsql);	
    		
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    }
    
    @RequestMapping(value = "/gw_deviceupstate", method = RequestMethod.GET)
    public  List<Map<String, Object>>  DeviceUpTime(@RequestParam(value="uid",required=true) String uid,
    												@RequestParam(value="duration", required=false, defaultValue="30m") String duration) {
    	
    	List<Map<String, Object>>  res  = EMPTY_LIST_MAP;

    	uid = uid.toLowerCase();

    	String fsql =    " index="+indexname+",sort=timestamp desc,size=1,query=cpu_stats:\"Qubercloud Manager\""
		    			+" AND timestamp:>now-"+duration+" AND uid:\""+uid+"\"|value(uid,uid,NA);"
		    			+" value(cpu_percentage,cpu,NA);value(timestamp,time,NA);"
		    			+" value(cpu_days,cpuDays,NA);value(cpu_hours,cpuHours,NA);value(cpu_minutes,cpuMinutes,NA);" 
						+" value(app_days,appDays,NA);value(app_hours,appHours,NA);value(app_minutes,appMinutes,NA);|table";
    	
		res = fsqlRestController.query(fsql);
		
		return res;
    }
    
    @RequestMapping(value = "/getmem", method = RequestMethod.GET)
    public  List<Map<String, Object>> getmem(@RequestParam(value="sid",  required=false) String sid, 
    								 	     @RequestParam(value="spid", required=false) String spid,
    								 	     @RequestParam(value="swid", required=false) String swid,
    								 	     @RequestParam(value="uid",  required=false) String uid,
    								 	     @RequestParam(value="duration", required=false, defaultValue="4m") String duration,
    								 	    @RequestParam(value="place",  required=false) String place) {
    	
    	if (swid != null) {
    		swid = swid.replaceAll("[^a-zA-Z0-9]", "");
    	}

    	List<Device> devices = deviceService.findBy(spid, sid, swid);

    	String size = recordSize(place,duration);
    	
		String fsql = "index=" + indexname + ",sort=timestamp desc,size="+size+",query=cpu_stats:\"Qubercloud Manager\" AND timestamp:>now-" + duration + " AND ";

    	
    	if (devices != null && devices.size() > 0 && uid == null) {
    	
    		String uidBuilder = buildDeviceArrayCondition(devices, "uid") + "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";
    		if (uidBuilder.length() > 0 ) {
    			fsql = fsql + uidBuilder;
    		} else {
    			return EMPTY_LIST_MAP;
    		}
    		
    		//fsql = fsql + buildArrayCondition(devices, "uid") + "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";

    		return fsqlRestController.query(fsql);
    	} else if (uid != null){
    		fsql = fsql + "uid:\"" + uid + "\"";
    		fsql = fsql + "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";

    		return fsqlRestController.query(fsql);	
    	} else {
    		return EMPTY_LIST_MAP;
    	}
    }    
    
   
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getpeers", method = RequestMethod.GET)
    public JSONObject getpeers(@RequestParam(value="sid", 	 	required=false) String sid, 
    						   @RequestParam(value="spid", 	 	required=false) String spid,
    						   @RequestParam(value="cid", 	 	required=false) String cid,
    						   @RequestParam(value="uid", 	 	required=false) String uid,
    						   @RequestParam(value="swid",	 	required=false) String swid
    						   ) throws IOException {

    	
    	JSONArray  dev_array 	= new JSONArray();
    	JSONArray client 		= new JSONArray();
    	JSONObject devlist 	 	=null;
    	JSONObject details =  new JSONObject();
    	
    	List<ClientDevice> clientDevices  = null;

    	HashMap<String, Integer> dupsmap = new HashMap<String, Integer>();
    	
    	try {
    		
    		int android 			= 0;
    		int windows 			= 0;
    		int ios 				= 0;
    		int	speaker 			= 0;
    		int	printer 			= 0;
    		int others				= 0;
    		long totCount			= 0;
    		int _2G					= 0;
    		long _5G				= 0;
    		int blocked_Count 		= 0;
    		
    		int _11K_Count = 0;
    		int _11R_Count = 0;
    		int _11V_Count = 0;
    		
    		final String status = "active";
    		String blocked 		= "blocked";
    		
    		List<ClientDevice> blockedClients = null;
    		
    		if (swid != null) {

				swid = swid.replaceAll("[^a-zA-Z0-9]", "");
				
				List<String> uidList  = new ArrayList<>();
				List<Device> nd = deviceService.findBy(null, null, swid);

				if (nd != null) {
					nd.forEach(n -> uidList.add(n.getUid()));
				}
				clientDevices = getClientDeviceService().getSavedPeerList(uidList, status);

			} else {
				
				if (uid != null) {
					String uuid   = uid.replaceAll("[^a-zA-Z0-9]", "");
					clientDevices = getClientDeviceService().findByUuidAndStatus(uuid, status);
					blockedClients = getClientDeviceService().findByUuidAndStatus(uuid, blocked);
				} else if (spid != null) {
					clientDevices = getClientDeviceService().findBySpidAndStatus(spid, status);
					blockedClients = getClientDeviceService().findBySpidAndStatus(spid, blocked);
				} else if (sid != null) {
					clientDevices = getClientDeviceService().findBySidAndStatus(sid, status);
					blockedClients = getClientDeviceService().findBySidAndStatus(sid, blocked);
				} else {
					clientDevices = getClientDeviceService().findByCidAndStatus(cid, status);
					blockedClients = getClientDeviceService().findByCidAndStatus(cid, blocked);
				}
			}
    		
    		HashMap<String, String> map = new HashMap<String, String> ();
    		
    		if (clientDevices !=null) {
    			for (ClientDevice clientDev : clientDevices) {

    				devlist =  new JSONObject();
    				
    				boolean _11r = clientDev.is_11r();
    				boolean _11k = clientDev.is_11k();
    				boolean _11v = clientDev.is_11v();
    				
    				String client_mac  = clientDev.getMac();
    				String ap 		    = clientDev.getUid();
    				String devtype   	= clientDev.getTypefs() == null ? "Others" : clientDev.getTypefs();
    				String radioType	= clientDev.getRadio_type()== null ? "2.4Ghz" : clientDev.getRadio_type();
    				String ssid      	= clientDev.getSsid() == null ? "UNKNOWN" : clientDev.getSsid();
    				String rssi         = clientDev.getPeer_rssi() == null ? "0" : clientDev.getPeer_rssi();
    				String ip         = clientDev.getPeer_ip() == null ? "NA" : clientDev.getPeer_ip();
    				
    				if (dupsmap.containsKey(client_mac)) {
						dupsmap.put(client_mac, dupsmap.get(client_mac) + 1);
						continue;
					} else {
						
						if (radioType.equals("2.4Ghz")) {
	    					_2G++;
	    				} else {
	    					_5G++;
	    				}
	    				
	    				switch (devtype) {
	    				case "android":
	    					android++;
	    					break;
	    				case "speaker":
	    					speaker++;
	    					break;
	    				case "mac":
	    					ios++;
	    					break;
	    				case "printer":
	    					printer++;
	    					break;
	    				case "windows":
	    					windows++;
	    					break;
	    				default:
	    					others++;
	    					break;
	    				}

						String location = "NA";

						if (!map.containsValue(ap)) {
							Device dev = deviceService.findOneByUid(ap);
							if (dev != null) {
								location = dev.getName();
							}
							map.put("uid", ap);
							map.put("alias", location);

						} else {
							location = map.get("alias");
						}

						String hostname = clientDev.getPeer_hostname();

	    				devlist.put("mac_address",  client_mac);
	    				devlist.put("rssi", 		rssi);
	    				devlist.put("tx", 			customerUtils.bytesconverter(clientDev.getCur_peer_tx_bytes()));
	    				devlist.put("rx", 			customerUtils.bytesconverter(clientDev.getCur_peer_rx_bytes()));
	    				devlist.put("k11", 			_11k);
	    				devlist.put("r11", 			_11r);
	    				devlist.put("v11", 			_11v);
	    				devlist.put("client_type",  devtype);
	    				devlist.put("devtype",      hostname);
	    				devlist.put("ssid", 		ssid);
	    				devlist.put("radio", 		radioType);
	    				devlist.put("uid", 			ap);
	    				devlist.put("ap", 	    	ap);
	    				devlist.put("associated",   "Yes");
	    				devlist.put("channel", 		"NA");
	    				devlist.put("signal", 	    clientDev.getPeer_signal_strength());
	    				devlist.put("ip", 	    	ip);
	    				devlist.put("conn_time",	customerUtils.secondsto_hours_minus_days(clientDev.getPeer_conn_time()));
	    				devlist.put("location", 	location);

	    				client.add(devlist);

	    				if (_11k) {
	    					_11K_Count++;
	    				} else if (_11r) {
	    					_11R_Count++;
	    				} else if (_11v) {
	    					_11V_Count++;
	    				}
						dupsmap.put(client_mac, 0);
					}
    			}			
    		}
    		
    		JSONArray  _2Gdev_array  = new JSONArray();
			JSONArray  _5Gdev_array  = new JSONArray();
			JSONArray  ios_array 	 = new JSONArray();
			JSONArray  android_array = new JSONArray();
			JSONArray  windows_array = new JSONArray();
			JSONArray  speaker_array = new JSONArray();
			JSONArray  printer_array = new JSONArray();
			JSONArray  others_array  = new JSONArray();
			JSONArray  total_array   = new JSONArray();
			JSONArray  active_array  = new JSONArray();

			JSONArray  _11k_array 	 = new JSONArray();
			JSONArray  _11r_array    = new JSONArray();
			JSONArray  _11v_array   = new JSONArray();
			
			totCount = _2G + _5G;
			
			if (blockedClients != null) {
				blocked_Count = blockedClients.size();
			}
			
        	//if (client != null && client.size() > 0) {

    			_2Gdev_array.add(0, "2G");
    			_2Gdev_array.add(1, _2G);

    			_5Gdev_array.add(0, "5G");
    			_5Gdev_array.add(1, _5G);

    			total_array.add(0, "Total");
    			total_array.add(1, totCount);
    			
    			active_array.add(0, "Active");
    			active_array.add(1, totCount);
    			
    			ios_array.add(0, "ios");
    			ios_array.add(1, ios);
    			
    			android_array.add(0, "android");
    			android_array.add(1, android);
    			
    			windows_array.add(0, "windows");
    			windows_array.add(1, windows);
    			
    			speaker_array.add(0, "speaker");
    			speaker_array.add(1, speaker);
    			
    			printer_array.add(0, "printer");
    			printer_array.add(1, printer);
    			
    			others_array.add(0, "others");
    			others_array.add(1, others);

    			_11k_array.add(0, "11K");
    			_11k_array.add(1, _11K_Count);

    			_11r_array.add(0, "11R");
    			_11r_array.add(1, _11R_Count);

    			_11v_array.add(0, "11V");
    			_11v_array.add(1, _11V_Count);
    	
    			dev_array.add(0,  client);
    			dev_array.add(1,  ios_array);
    			dev_array.add(2,  android_array);
    			dev_array.add(3,  windows_array);
    			dev_array.add(4,  speaker_array);
    			dev_array.add(5,  printer_array);
    			dev_array.add(6,  others_array);
    			
    			dev_array.add(7, total_array);
    			dev_array.add(8, _2Gdev_array);
    			dev_array.add(9, _5Gdev_array);
    			dev_array.add(10,  active_array);
    			
    			JSONArray  blocked_client = new JSONArray();
    			blocked_client.add(0,"Block");
    			blocked_client.add(1, blocked_Count);
    			
    			dev_array.add(11,blocked_client);
    			
    			dev_array.add(12, _11k_array);
    			dev_array.add(13, _11r_array);
    			dev_array.add(14,  _11v_array);
    		//}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
    	//LOG.info("Device Connected" +dev_array);
    	
    	details.put("devicesConnected", dev_array);		
    	
		return details;		
    }
    
    
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getvaps", method = RequestMethod.GET)
    public JSONObject getvaps(@RequestParam(value="sid", 	  required=false) String sid, 
    						  @RequestParam(value="spid", 	  required=false) String spid,
    						  @RequestParam(value="uid", 	  required=false) String uid,
    						  @RequestParam(value="duration", required=false, defaultValue="5m") String duration) throws IOException {
    	
    	JSONArray  dev_array = new JSONArray();
    	JSONObject devlist 	 = new JSONObject();
    	
    	int count_2g  = 0;
    	int count_5g  = 0;
    	
    	List<Device> devices = deviceService.findBy(spid, sid, null);
    	
    	if (devices != null && uid == null) {
	    	for (Device nd : devices) { 
	    		Device dv = deviceService.findOneByUid(nd.getUid());
	    		if (dv != null) {
	    			count_2g += dv.getVap2gcount();
	    			count_5g += dv.getVap5gcount();
	    	}
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
    	int vap_2g 		 = 0;
    	int vap_5g 		 = 0;
    	
		Device device = deviceService.findOneByUid(uid);

		if (device != null) {
			vap_2g = device.getVap2gcount();
			vap_5g = device.getVap5gcount();
		}
    	
    	
    	for (int i = 0;  i < num_intf; i++) {
    		dev = new JSONObject();
    		switch (i) {
    			case 0:
    	    		dev.put("device", "wlan2g");
    	    		if (vap_2g != 0) {
    	    			dev.put("status", "enabled");
    	    			dev.put("vapcount", vap_2g);
    	    		} else {
    	    			dev.put("status", "disabled");
    	    			dev.put("vapcount", "1");
    	    		}
    				break;
    			case 1:
    	    		dev.put("device", "wlan5g");
    	    		if (vap_5g != 0) {
    	    			dev.put("status", "enabled");
    	    			dev.put("vapcount", vap_5g);
    	    		} else {
    	    			dev.put("status", "disabled");
    	    			dev.put("vapcount", "1");
    	    		}
    				break;
    			case 2:
    	    		dev.put("device", "ble");
	    			dev.put("status", "disabled");
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
	@RequestMapping(value = "/getdevcon", method = RequestMethod.GET)
    public JSONObject getdevcon(@RequestParam(value="sid", 		required=false) String sid, 
    							@RequestParam(value="spid", 	required=false) String spid,
    							@RequestParam(value="swid", 	required=false) String swid,
    							@RequestParam(value="uid", 	  	required=false) String uid,
    							@RequestParam(value="duration", required=false, defaultValue="5m") String duration) throws IOException {
    	
    	JSONArray  dev_array = null;   	
    	JSONObject devlist 	 = new JSONObject();
		dev_array = new JSONArray();
		
		Device dv = deviceService.findOneByUid(uid);
		
		if (dv != null) {
			
			JSONArray  dev_array1 = new JSONArray();
			JSONArray  dev_array2 = new JSONArray();
			JSONArray  dev_array3 = new JSONArray();
			JSONArray  dev_array4 = new JSONArray();
	    	
			dev_array1.add (0, "Mac");
			dev_array1.add (1, 0);
			dev_array2.add (0, "Android");
			dev_array2.add (1, 0);
			dev_array3.add (0, "Win");
			dev_array3.add (1, 0);
			dev_array4.add (0, "Others");
			dev_array4.add (1, 0);		
			
			dev_array.add  (0, dev_array1);
			dev_array.add  (1, dev_array2);
			dev_array.add  (2, dev_array3);
			dev_array.add  (3, dev_array4);			
			
		}
    					
		devlist.put("devicesConnected", dev_array);	
		
    	//LOG.info("Connected Clients" +devlist.toString());
    	
    	return devlist;
    	
    }
@SuppressWarnings("unchecked")
@RequestMapping(value = "/getdevtype", method = RequestMethod.GET)
public JSONObject getdevtype(@RequestParam(value="sid", 		required=false) String sid, 
				 	      	 @RequestParam(value="spid", 		required=false) String spid,
				 	      	 @RequestParam(value="uid", 		required=false) String uid,
				 	      	 @RequestParam(value="duration", 	required=false, defaultValue="5m") String duration) throws IOException {

		JSONArray  dev_array = null;
		JSONObject devlist 	 = new JSONObject();
		dev_array = new JSONArray();
		
		Device dv = deviceService.findOneByUid(uid);
		
		if (dv != null) {

			JSONArray  dev_array1 = new JSONArray();
			JSONArray  dev_array2 = new JSONArray();
	    	
			dev_array2.add (0, "2G");
			dev_array2.add (1, dv.getVap2gcount());
			dev_array1.add (0, "5G");
			dev_array1.add (1, dv.getVap5gcount());
	
			dev_array.add  (0, dev_array2);
			dev_array.add  (1, dev_array1);
		}
		
		devlist.put("typeOfDevices", dev_array);	
		
    	//LOG.info("typeOfDevices" +devlist.toString());			
				
		return devlist;
	}  
@SuppressWarnings("unchecked")
@RequestMapping(value = "/getstacount", method = RequestMethod.GET)
public JSONObject getstacount(@RequestParam(value="sid", 	required=false) String sid, 
			 	      	 @RequestParam(value="spid", 		required=false) String spid,
			 	      	 @RequestParam(value="uid", 		required=false) String uid,
			 	      	 @RequestParam(value="duration", 	required=false, defaultValue="5m") String duration) throws IOException {

		JSONArray  dev_array = null;
		JSONObject devlist 	 = new JSONObject();
		dev_array = new JSONArray();
		
		Device dv = deviceService.findOneByUid(uid);
		
		if (dv != null) {
	
			JSONArray  dev_array1 = new JSONArray();
			JSONArray  dev_array2 = new JSONArray();
	    	
			dev_array2.add (0, "Active");
			dev_array2.add (1, 0);
			dev_array1.add (0, "Block");
			dev_array1.add (1, blk_count);
	
			dev_array.add  (0, dev_array2);
			dev_array.add  (1, dev_array1);
		}
		
		devlist.put("typeOfDevices", dev_array);	
		
		//LOG.info("typeOfDevices" +devlist.toString());			
				
		return devlist;
	} 
    
	public  ArrayList<Integer> probeCount(String peer_mac, JSONObject dev) {
		
		int ios 		= 0;
		int android 	= 0;
		int windows 	= 0;
		int router 		= 0;
	    int	speaker 	= 0;
	    int printer  	= 0;
	    int  other 		= 0;
	    
		String inputLine = "Unknown Vendor";
		String probeMac  = peer_mac;
		
		probeMac = probeMac.substring(0,8).toUpperCase();
		
		ProbeOUI oui = null;
		oui 		 = probeOUIService.findOneByUid(probeMac);
		
		if (oui != null) {
			inputLine = oui.getVendorName();
		}
		inputLine = inputLine.toLowerCase();

		
		String vendorType = inputLine; 
		vendorType 		  = vendorType+="...";
			
		dev.put("devtype", vendorType.toUpperCase());
		
		
		ArrayList<Integer> count = new ArrayList<Integer>();
	    		
		if (inputLine.contains("apple")) {
			ios++;
			dev.put("client_type", "mac");
		}  else if (inputLine.contains("lenovo")
			      || inputLine.contains("asustek") 
			      || inputLine.contains("oppo")
			      || inputLine.contains("vivo")
			      || inputLine.contains("lgelectr")
			      || inputLine.contains("sonymobi")
			      || inputLine.contains("motorola")
			      || inputLine.contains("google")
			      || inputLine.contains("xiaomi")
			      || inputLine.contains("oneplus")
			      || inputLine.contains("samsung")
			      || inputLine.contains("htc")
			      || inputLine.contains("gioneeco")
			      || inputLine.contains("zte")
			      || inputLine.contains("huawei")
			      || inputLine.contains("chiunmai")) {
			android++;
			dev.put("client_type", "android");
		} /*else if (inputLine.contains("cisco") 
				|| inputLine.contains("ruckus")
				|| inputLine.contains("juniper")
				|| inputLine.contains("d-link")
				|| inputLine.contains("tp-link")
				|| inputLine.contains("compex")
				|| inputLine.contains("ubiquiti")
				|| inputLine.contains("netgear")
				|| inputLine.contains("eero")
				|| inputLine.contains("merunetw")
				|| inputLine.contains("plume")
				|| inputLine.contains("buffalo")
				|| inputLine.contains("mojo")
				|| inputLine.contains("compal")
				|| inputLine.contains("aruba")) {
			dev.put("client_type", "router");
				router++;
		}*/ else if (  inputLine.contains("bose")
				   ||inputLine.contains("jbl")) {
			speaker ++;
			dev.put("client_type", "speaker");
		} else if (   inputLine.contains("canon")
				   || inputLine.contains("roku")
				   || inputLine.contains("nintendo")
				   || inputLine.contains("hp")
				   || inputLine.contains("hewlett")) {
			printer ++;
			dev.put("client_type", "printer");
		}else if (inputLine.contains("microsof")) {
			windows++;
			dev.put("client_type", "windows");
		} else {
			other++;
			dev.put("client_type", "laptop");
		}
		
		
		count.add(0, ios);
		count.add(1, android);
		count.add(2, windows);
		count.add(3, speaker);
		count.add(4, printer);
		count.add(5, other);
		
		
		return count;
	}
	
	/**
	 * 
	 * Given stat value is set or summed up at the src vector at index 1
	 * 
	 * @param src
	 * @param stat
	 */
	void addStat(JSONArray src, String type, int stat) {
		if (src.isEmpty()) {
			src.add(0, type);
			src.add(1, stat);
		} else {
			stat = (Integer) src.get(1) + stat;
			src.set(1, stat);
		}
	}
	
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/tcpudpconn", method = RequestMethod.GET)
    public  JSONObject tcpudpconn(@RequestParam(value="uid", required=true)String uid) {
    	
    	int tcp = 0;
    	int udp = 0;
    	
    	List<Map<String, Object>>  conn  = EMPTY_LIST_MAP;
    	
    	String fsql  = "index="+indexname +",sort=timestamp desc,size=1,query=timestamp:>now-" + "1m" + " AND ";

		fsql = fsql + "uid:\"" + uid + "\"";
		fsql = fsql + "AND web_stats:\"Qubercloud Manager\"|value(num_tcp,tcp,NA);value(num_udp,udp,NA);value(timestamp,time,NA)|table";
    	conn =  fsqlRestController.query(fsql);
    	   	
		Iterator<Map<String, Object>> iterator = conn.iterator();
		
		while (iterator.hasNext()) {
			
			TreeMap<String , Object> me = new TreeMap<String, Object> (iterator.next());
			String tcp_str = (String)me.values().toArray()[0];
			String udp_str = (String)me.values().toArray()[2]; 
			tcp=Integer.parseInt(tcp_str);
			udp=Integer.parseInt(udp_str);
		}
 
		JSONArray  dev_array = null;
		JSONObject devlist 	 = new JSONObject();
		dev_array = new JSONArray();
		JSONArray  dev_array1 = new JSONArray();
		JSONArray  dev_array2 = new JSONArray();
		
		dev_array1.add (0, "TCP");
		dev_array1.add (1, tcp);
		dev_array2.add (0, "UDP");
		dev_array2.add (1, udp);		
		
		dev_array.add  (0, dev_array1);
		dev_array.add  (1, dev_array2);
		
		devlist.put("tcpudpconnections", dev_array);	
		
		//LOG.info("tcpudpconnections" +devlist.toString());		
    	
    	return devlist;
    }
    
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getblkpeers", method = RequestMethod.GET)
    public JSONObject getblkpeers(@RequestParam(value="sid", 	 	required=false) String sid, 
    						   	  @RequestParam(value="spid", 	 	required=false) String spid,
    						   	  @RequestParam(value="cid", 	 	required=false) String cid,
    						   	  @RequestParam(value="uid", 	 	required=false) String uid,
    						   	  @RequestParam(value="duration", 	required=false, defaultValue="5m") String duration) throws IOException {

    	JSONArray  dev_array = new JSONArray();    	
    	JSONObject devlist 	 = new JSONObject();
    	JSONObject dev 		 = null;
    
    	Iterable<ClientDevice> clientDevices = null;

		final String status = "blocked";
		
		if (uid != null) {
			clientDevices = getClientDeviceService().findByUid(uid);
		} else if (spid != null) {
			clientDevices = getClientDeviceService().findBySpid(spid);
		} else if (sid != null) {
			clientDevices = getClientDeviceService().findBySid(sid);
		} else {
			clientDevices = getClientDeviceService().findByCid(cid);
		}

    	if (clientDevices != null) {
    		
    		HashMap<String, String> map = new HashMap<String, String> ();
    		
	    	for (ClientDevice nd : clientDevices) {    		
	    		dev = new JSONObject();
	    		
				String ip         = nd.getPeer_ip() == null ? "NA" : nd.getPeer_ip();
				
				String location = "NA";

				String ap = nd.getUid();
				
				if (!map.containsValue(ap)) {
					Device device = deviceService.findOneByUid(ap);
					if (device != null) {
						location = device.getName();
					}
					map.put("uid", ap);
					map.put("alias", location);

				} else {
					location = map.get("alias");
				}

				String hostname = nd.getPeer_hostname();
				
				dev.put("mac_address", 	nd.getMac());
				dev.put("ap", 			ap);
				dev.put("ip", 	        ip);
				dev.put("radio", 		nd.getRadio_type());
				dev.put("rssi", 		nd.getPeer_rssi());
				dev.put("conn_time",	customerUtils.secondsto_hours_minus_days(nd.getPeer_conn_time()));
				dev.put("devtype", 		hostname);
				dev.put("ssid", 		nd.getSsid());
				dev.put("rx", 			nd.getCur_peer_rx_bytes());
				dev.put("tx", 			nd.getCur_peer_tx_bytes());
				dev.put("location", 	location);
				
				String uidstr = nd.getTypefs();
				
				if (uidstr != null) {
					if (uidstr.toLowerCase().contains("apple")) {
						dev.put("client_type", "mac");
					} else if (uidstr.toLowerCase().contains("android")) {
						dev.put("client_type", "android");
					} else if (uidstr.toLowerCase().contains("windows")) {
						dev.put("client_type", "windows");
					} else {
						dev.put("client_type", "laptop");
					}
				} else {
					dev.put("client_type", "laptop");
				}

				dev_array.add (dev);
				blk_count++;
				
			}
    	}
    	
		devlist.put("blockedClients", dev_array);
		//OG.info("JSN.Blocked :" + devlist.toString());

		return devlist;		
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/getacl", method = RequestMethod.GET)
    public JSONObject getacl (	  @RequestParam(value="sid", 	 	required=false) String sid, 
    						   	  @RequestParam(value="spid", 	 	required=false) String spid,
    						   	  @RequestParam(value="cid", 	 	required=false) String cid,
    						   	  @RequestParam(value="uid", 	 	required=false) String uid,
    						   	  @RequestParam(value="duration", 	required=false, defaultValue="5m") String duration) throws IOException {

    	JSONArray  dev_array = new JSONArray();    	
    	JSONObject devlist 	 = new JSONObject();
    	JSONObject dev 		 = null;
    	
    	Iterable<ClientDevice> clientDevices = null;
    	final String status = "blocked";
    	
		if (uid != null) {
			String uuid = uid.replaceAll("[^a-zA-Z0-9]", "");
			clientDevices = getClientDeviceService().findByUuidAndStatus(uuid, status);
		} else if (spid != null) {
			clientDevices = getClientDeviceService().findBySpidAndStatus(spid, status);
		} else if (sid != null) {
			clientDevices = getClientDeviceService().findBySidAndStatus(sid, status);
		} else {
			clientDevices = getClientDeviceService().findByCidAndStatus(cid, status);
		}
    	
    	
    	
    	if (clientDevices != null) {
	    	for (ClientDevice nd : clientDevices) {    		
	    		dev = new JSONObject();
	    		
				String policy = nd.getPid() == null ? "NA" : nd.getPid();
	    		
				dev.put("mac_address", 	nd.getMac());
				dev.put("devtype", 		nd.getPeer_hostname());
				dev.put("acl", 			nd.getAcl());
				dev.put("pid", 			policy);
				dev.put("ssid", 		nd.getSsid());
				
				
				String location = "GLOBAL";
				
				if (policy.equals("Venue") && nd.getSid() != null) {
					Site site = siteService.findById(nd.getSid());
					if (site != null) {
						location = site.getUid();
					}

				} else if (policy.equals("Floor") && nd.getSpid() != null) {
					Portion portion = portionService.findById(nd.getSpid());
					if (portion != null) {
						location = portion.getUid();
					}
				} else if (policy.equals("uid") && nd.getUid() != null) {
					Device ndList = deviceService.findOneByUid(nd.getUid());
					if (ndList != null) {
						location = ndList.getName();
					}
				}

				 dev.put("uid", location);
				 
				 String uidstr = nd.getTypefs();
				 
				if (uidstr != null) {
					if (uidstr.toLowerCase().contains("apple")) {
						dev.put("client_type", "mac");	
					} else if (uidstr.toLowerCase().contains("android")) {
						dev.put("client_type", "android");
					} else if (uidstr.toLowerCase().contains("windows")) {
						dev.put("client_type", "windows");
					} else {
						dev.put("client_type", "laptop");
					}
				} else {
					dev.put("client_type", "laptop");
				}
				
				dev_array.add (dev);
			}
    	} 
    	   	
		devlist.put("aclClients", dev_array);
		//LOG.info("JSN.ACLPeers :" + devlist.toString());

		return devlist;		
    }    
    
    
    @RequestMapping(value = "/report", method = RequestMethod.GET)
	public void report(
			@RequestParam(value = "cid", required = false) String cid,
			HttpServletRequest request,HttpServletResponse response) {
    	
    	if (customerUtils.trilateration(cid)) {
			trilaterationReport.pdf(request, response);
		}else {
    		LOG.info("=========Entry Exit Solution ==============");
    	}	
    }
    
	@RequestMapping(value = "/imgcapture", method = RequestMethod.GET)
	public String imgcapture(HttpServletRequest request, HttpServletResponse response) throws IOException {	
		
		String imgFileName = "./uploads/screenshot.jpg";
		
		OutputStream responseOutputStream = null;
		FileInputStream fileInputStream   = null;
		
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
		        
		    	File jpegFile = new File(imgFileName);
				response.setContentType("application/jpeg");
				response.setHeader("Content-Disposition", "attachment; filename=" + imgFileName);
				response.setContentLength((int) jpegFile.length());
				
				fileInputStream 		= new FileInputStream(jpegFile);
				responseOutputStream 	= response.getOutputStream();
				int bytes;
				
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}	
		      } catch(Exception e){
		    	  e.printStackTrace();
		      }finally{
					responseOutputStream.close();
					fileInputStream.close();
		      }
			//return imgFileName;
		}
			
		return imgFileName;
	}
	
	@RequestMapping(value = "/gw_alert", method = RequestMethod.GET)
	public JSONObject alert(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "pdfgen", required = false) Boolean pdfgen) throws IOException{

	
		JSONObject devlist  = new JSONObject();
		JSONArray dev_array = new JSONArray();
		
		try {	

			JSONObject dev 					= null;
			Iterable<Device> devicesList 	= null;	
			String state 					= "inactive";
			devicesList 					= deviceService.findByCidAndState(cid, state);
			
			Customer cx = customerService.findById(cid);
			if (cx != null) {
				TimeZone totimezone = customerUtils.FetchTimeZone(cx.getTimezone());
				format.setTimeZone(totimezone);
			} else {
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
			}

			if (devicesList != null) {
				
				HashMap<String,String> portionMap   = new HashMap<String,String>();
				
				for (Device device : devicesList) {
					
					String type  = device.getTypefs();
					
					if (StringUtils.isNotBlank(type) && type.equals("server") || type.equals("switch")) {
						continue;
					}
					
					String lastSeen 	= device.getLastseen();
					String uid   		= device.getUid();
					String spid 		= device.getSpid();
					String floorName   	= "NA";
					
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
					
					
					String timestamp = "-";
					if (!StringUtils.isEmpty(lastSeen)) {
						timestamp = lastSeen +" "+format.getTimeZone().getDisplayName(false, 0);
					}
					
					dev = new JSONObject();
					
					dev.put("macaddr", 	   uid);
					dev.put("state",       device.getState());
					dev.put("alias",       device.getName());
					dev.put("portionname", floorName);
					dev.put("timestamp",   timestamp);
					dev_array.add(dev);
				}

				if (dev_array == null || dev_array.size() == 0) {
					if(pdfgen != null && pdfgen){
						return null;
					}
					dev_array = defaultDatas(dev_array);
				}
					
				devlist.put("inactive_list", dev_array);
			//	LOG.info("inactive---------"+dev_array.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return devlist;
	}
	
	public JSONArray defaultDatas(JSONArray dev_array) {
		JSONObject dev = new JSONObject();
		dev.put("macaddr",  	"-");
		dev.put("floorname", 	"NA");
		dev.put("alias", 		"NA");
		dev.put("portionname", 	"NA");
	    dev.put("sitename",    	"NA");
	    dev.put("state",    	"-");
	    dev.put("status",       "Unknown");
	    dev.put("timestamp",     "NA");
		dev_array.add(dev);
		return dev_array;
	}
	

	@RequestMapping(value = "/GW_Device_crash_info", method = RequestMethod.GET)
	public List<Map<String, Object>>  GW_Device_crash_info(@RequestParam(value = "cid", required = true) String cid,
										   @RequestParam(value = "time", required = true) String time) {
		try {
			
			final String opcode = "gw_device_crash_dump";
			final String type   = "device_crash_dump";
			
			if (StringUtils.isBlank(time)) {
				time = "10d";
			}
			
			String fsql = "index=" + device_history_event+",size=500,type="+type+",query=timestamp:>now-"+time+" AND opcode:"
					+ opcode + " AND ";
					
			List<Device> devices = deviceService.findByCid(cid);
			if (devices != null) {
				String uidbuilder = buildDeviceArrayCondition(devices, "uid");
				fsql = fsql +uidbuilder;
			}
			fsql += " |value(uid,uid, NA); value(crash_time,crash_time,NA);value(cid,cid,NA);value(uid,uid,NA);"
					+ " value(filename,filename,NA);value(alias,alias,NA);value(timestamp,time,NA);|table";
			
			return fsqlRestController.query(fsql);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EMPTY_LIST_MAP;

	}
	
		/**
		 * Used to download the crash dump file
		 * @param request
		 * @param response
		 * @param fileName
		 * @throws IOException
		 */
	   	@RequestMapping("/GW_Device_crash_dump_dowmload")
		public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response,@RequestParam("filename") String fileName) {

	   		try {
	   			
	   			String basePath = _CCC.properties.getProperty("facesix.fileio.binary.root", "/var/www/html");
	   			
	   			String domain 	  = CustomerUtils.domain.nmesh.name();
	   			String folder 	  = customerUtils.createCrashDumpFolderName(domain);

	   			String path = basePath + "/" + folder + "/" + fileName;
	   			File file = new File(path);
	   			
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
	 
	 /**
	 * RRM Request from device
	 * 
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/rrm")
	public boolean rrm(@RequestBody Map<String, Object> rrm) {
		try {
			//LOG.info("###### rrm payload ###### " + rrm);
			if (rrm != null && !rrm.isEmpty()) {
				elasticService.post(device_history_event, STEER_INDEX_TYPE, rrm);
			}

		} catch (Exception e) {
			LOG.info("While posting rrm data error " + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}
	
	
	/**
	 * Used BSS Response
	 * @param bss
	 * @return
	 */

	@RequestMapping(value = "/bss")
	public boolean bss(@RequestBody Map<String, Object> bss) {

		try {

			LOG.info("##### bss payload ######" + bss);

			if (bss != null && !bss.isEmpty()) {

				String uid = (String) bss.get("uid");

				Device device = deviceService.findOneByUid(uid);

				if (device == null) {
					LOG.info("Device Not Found " + uid);
					return false;
				}

				String cid = device.getCid();

				if (bss.containsKey(STEER_STA_LIST)) {
					
					HashMap<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("uid", uid);
					dataMap.put("cid", cid);
					dataMap.put("opcode", bss.get("opcode"));

					if (device.getSid() != null) {
						dataMap.put("sid", device.getSid());
					}
					if (device.getSpid() != null) {
						dataMap.put("spid", device.getSpid());
					}

					dataMap.put(STEER_STA_LIST, bss.get(STEER_STA_LIST));

					elasticService.post(device_history_event, STEER_INDEX_TYPE, dataMap);

				}
			}

		} catch (Exception e) {
			LOG.error(" While posting bss data error " + e);
		}

		return true;
	}
	 
	@RequestMapping(value = "/activeClients", method = RequestMethod.GET)
	public List<ClientDevice> activeClients(
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid) {

		List<ClientDevice> clientDevices = null;
		String status = "active";

		if (uid != null) {
			String uuid = uid.replaceAll("[^a-zA-Z0-9]", "");
			clientDevices = getClientDeviceService().findByUuidAndStatus(uuid, status);
		} else if (spid != null) {
			clientDevices = getClientDeviceService().findBySpidAndStatus(spid, status);
		} else {
			clientDevices = getClientDeviceService().findBySidAndStatus(sid, status);
		} 

		return clientDevices;

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/networkBalancer", method = RequestMethod.GET)
	public JSONArray networkBalancer(
				@RequestParam(value = "cid", required = true) String cid,
				@RequestParam(value = "macId", required = false) String macId,
				@RequestParam(value = "timestamp", required = false, defaultValue = "10m") String timestamp) {

		JSONArray dataArray = new JSONArray();

		try {
			
			//macId 	  = "dc:ef:ca:83:b0:93";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

			String query = QueryBuilders.matchPhraseQuery("client_mac", macId).toString();
			String query2 = QueryBuilders.matchPhraseQuery(STEER_STA_LIST+".MAC_ID", macId).toString();

			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().should(QueryBuilders.simpleQueryStringQuery(query));
			boolQuery.should(QueryBuilders.simpleQueryStringQuery(query2));
			
			if (elasticsearchConfiguration.getInstance() == null) {
				return dataArray;
			}
			
			SearchResponse response = elasticsearchConfiguration.getInstance().prepareSearch(device_history_event)
					.setTypes(STEER_INDEX_TYPE).addSort("timestamp", SortOrder.DESC).setQuery(boolQuery).setFrom(0)
					.setSize(1000).execute().actionGet();

			for (SearchHit hit : response.getHits().getHits()) {

				String message 	  = hit.getSourceAsString();
				
				JSONParser parser = new JSONParser();
				JSONObject data	  = (JSONObject) parser.parse(message);

				String opcode 	= (String) data.getOrDefault("opcode","NA");
				String time 	= (String) data.get("timestamp");
				
				Date dateTime 	= sdf.parse(time);
				String dateStr  = format.format(dateTime);

				String status = "-";

				if (data.containsKey("status")) {
					status = data.get("status").toString();
				}

				if (opcode.equals(NETWORK_BALANCER_STEEER_REQ)) {
					String uid = (String) data.get("uid");
					if (data.containsKey(STEER_STA_LIST)) {
						JSONArray array = (JSONArray) data.get(STEER_STA_LIST);
						Iterator<JSONObject> steerStaIter = array.iterator();
						while (steerStaIter.hasNext()) {
							JSONObject obj = steerStaIter.next();
							if (obj.containsValue(macId)) {
								JSONObject object = validateSteerData(obj);
								object.put("uid",    uid);
								object.put("timestamp", (String) data.get("timestamp"));
								data = object;
								break;
							} else {
								continue;
							}
						}
					}
				}

				JSONObject dataObject = new JSONObject();

				dataObject.put("time", dateStr);
				dataObject.put("opcode", opcode);
				dataObject.put("status", status);
				dataObject.put("payload", String.valueOf(data));
				dataArray.add(dataObject);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("While networkBalancer processing error ", e);
		}
		
		return dataArray;
	}

	public JSONObject validateSteerData(JSONObject json) {
		
		if (json.containsKey(STEER_BSS_LIST)) {
			
			JSONArray array = (JSONArray)json.get(STEER_BSS_LIST);
			
			Iterator<JSONObject> steerObject = array.iterator();
			
			while (steerObject.hasNext()) {
				
				JSONObject data = steerObject.next();
				
				if (data.containsKey("reject_unspecified")) {
					String reject_unspecified = data.get("reject_unspecified").toString();
					if (reject_unspecified.equals("0")) {
						data.remove("reject_unspecified");
					}
				}
				if (data.containsKey("reject_beacon")) {
					String reject_beacon = data.get("reject_beacon").toString();
					if (reject_beacon.equals("0")) {
						data.remove("reject_beacon");
					}
				}
				if (data.containsKey("reject_capab")) {
					String reject_capab = data.get("reject_capab").toString();
					if (reject_capab.equals("0")) {
						data.remove("reject_capab");
					}
				}
				if (data.containsKey("reject_undesired")) {
					String reject_undesired = data.get("reject_undesired").toString();
					if (reject_undesired.equals("0")) {
						data.remove("reject_undesired");
					}
				}
				if (data.containsKey("reject_delay_request")) {
					String reject_delay_request = data.get("reject_delay_request").toString();
					if (reject_delay_request.equals("0")) {
						data.remove("reject_delay_request");
					}
				}
				if (data.containsKey("reject_candidates")) {
					String reject_candidates = data.get("reject_candidates").toString();
					if (reject_candidates.equals("0")) {
						data.remove("reject_candidates");
					}
				}
				if (data.containsKey("reject_no_suitable")) {
					String reject_no_suitable = data.get("reject_no_suitable").toString();
					if (reject_no_suitable.equals("0")) {
						data.remove("reject_no_suitable");
					}
				}
				if (data.containsKey("reject_leaving_ess")) {
					String reject_leaving_ess = data.get("reject_leaving_ess").toString();
					if (reject_leaving_ess.equals("0")) {
						data.remove("reject_leaving_ess");
					}
				}
			}
		}
		return json;
	}


	public boolean isnewRecords(String uid, String lastUpdateTime, List<Map<String, Object>> datas,boolean enablelog) {

		boolean status = true;

			try {
				
				String className = this.getClass().getName();
				
				
				Map<String, Object> map = datas.get(0);
			
				String grayLogtime 		= (String) map.get("timestamp");
				Date cur_graylog_date 	= parse.parse(grayLogtime);

			if (lastUpdateTime != null) {
				Date prev_graylog_date = parse.parse(lastUpdateTime);
				if (prev_graylog_date.equals(cur_graylog_date) || prev_graylog_date.after(cur_graylog_date)) {
					//CustomerUtils.logs(enablelog, className, "---RADIO STATS OLD RECORDS---- DEVICE UID " + uid);
					status = false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;

	}

	public ConcurrentHashMap<String, Object> getBasicClientDetails(Device dev, boolean enablelog, String duration) {

		ConcurrentHashMap<String, Object> dev_map = new ConcurrentHashMap<String, Object>();
		
		try {
			
			final String uid 		 	 = dev.getUid();
			final String lastUpdateTime = dev.getGraylogtime();
			
			int vap2G = dev.getVap2gcount();
			int vap5G = dev.getVap5gcount();
			
			List<Map<String,Object>> logs = new ArrayList<Map<String,Object>>();
			
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			
			for (int i = 0; i < vap2G; i++) {
				
				String query = "index="+indexname +",sort=timestamp desc,size=1,query=timestamp:>now-"+ duration+" AND "
						 +" uid:\""+uid+"\" AND vap_id:\""+i+"\" AND radio_type:\"2.4Ghz\"|"
						+ " value(message,message, NA);value(timestamp,timestamp,NA);|table,sort=Date:desc;";

				List<Map<String,Object>> twoG_Details = fsqlRestController.query(query);
				
				if (twoG_Details != null && twoG_Details.size() > 0) {
					boolean isNew = isnewRecords(uid, lastUpdateTime, twoG_Details,enablelog);
					if (isNew) {
						logs.addAll(twoG_Details);
					} else {
						continue;
					}
				}
			}
			
			for (int i = 0; i < vap5G; i++) {
				
				String query = "index="+indexname +",sort=timestamp desc,size=1,query=timestamp:>now-"+duration+" AND "
							+ "uid:\""+uid+"\" AND vap_id:\""+i+"\" AND radio_type:\"5Ghz\"|"
							+ " value(message,message, NA);value(timestamp,timestamp,NA);|table,sort=Date:desc;";

				List<Map<String,Object>> fiveG_Details = fsqlRestController.query(query);
				if (fiveG_Details != null && fiveG_Details.size() > 0) {
					boolean isNew = isnewRecords(uid, lastUpdateTime, fiveG_Details,enablelog);
					if (isNew) {
						logs.addAll(fiveG_Details);
					} else {
						continue;
					}
				}
			}
			
			if (!logs.isEmpty() && logs.size() > 0) {
				this.getRadioValues(dev, map, dev_map,  enablelog, logs);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dev_map;
	}
	
	

	@SuppressWarnings("unchecked")
	 boolean  getRadioValues(final Device dev, HashMap<String, Integer> map,
			   ConcurrentHashMap<String, Object> dev_map,boolean enablelog, List<Map<String, Object>> details) {
		
		JSONParser jsonparser = new JSONParser();
	
		long _2G = 0;
		long _5G = 0;
		
		int ios 		= 0;
		int android 	= 0;
		int windows 	= 0;
		int printer 	= 0;
		int speaker 	= 0;
		int	others	 	= 0;
		
		int _11K_Count 	= 0;
		int _11R_Count 	= 0;
		int _11V_Count 	= 0;
		
		double _vap_tx_bytes =  0;
		double _vap_rx_bytes =  0;
		
		if (details != null) {

			final String uid 		= dev.getUid();
			
			String grayLogtime = null;
			
			try {

				for (Map<String, Object> info : details) {

					grayLogtime        = (String) info.get("timestamp");
					String jsonStr     = (String) info.get("message");

					JSONObject message = (JSONObject) jsonparser.parse(jsonStr);
					String radiotype   = (String) message.get("radio_type");
					String ssid        = (String) message.get("vap_ssid");
					String vap_mac     = (String) message.get("vap_mac");

					if (message.containsKey("_vap_tx_bytes")) {
						_vap_tx_bytes += Double.valueOf(message.get("_vap_tx_bytes").toString());
					}
					if (message.containsKey("_vap_rx_bytes")) {
						_vap_rx_bytes += Double.valueOf(message.get("_vap_rx_bytes").toString());
					}

					if (message.containsKey("peer_list")) {
						
						ConcurrentHashMap<String, HashMap<String, Object>> cache = clientCache.get_assoc_device_clients(uid);
							
						JSONArray peerList = (JSONArray) message.get("peer_list");
						Iterator<JSONObject> iter = peerList.iterator();

						while (iter.hasNext()) {

							JSONObject peer = iter.next();
							
							HashMap<String,Object> peer_map =new ObjectMapper().readValue(peer.toString(), HashMap.class);
							
							String mac = (String) peer.get("peer_mac");

							boolean _11k = (boolean) peer.getOrDefault("11k", false);
							boolean _11v = (boolean) peer.getOrDefault("11v", false);
							boolean _11r = (boolean) peer.getOrDefault("11r", false);
							
							long peer_signal_strength 	= 0;
							
							if (peer.containsKey("peer_signal_strength")){
								peer_signal_strength = (long) peer.get("peer_signal_strength");
								peer_map.put("peer_signal_strength", peer_signal_strength);
							}
							

						
							peer_map.put("ssid", 		ssid);
							peer_map.put("radio_type",  radiotype);
							peer_map.put("bssid", vap_mac);
							peer_map.put("last_seen",   System.currentTimeMillis());
							
							if (cache != null && cache.containsKey(mac) && cache.get(mac).containsKey("os")) {
							} else {
								JSONObject manufacture = nmeshRetailerRestController.deviceManufacture(mac);
								String os = (String) manufacture.get("os");
								peer_map.put("os", os);
								String hostName    = (String) manufacture.get("manufactureName");
								peer_map.put("host", hostName);
							}
							clientCache.add(uid, mac, peer_map);
							
							
							JSONObject values = new JSONObject();
							ArrayList<Integer> devTypeCount = probeCount(mac, values);
							
							if (radiotype.equalsIgnoreCase("2.4Ghz")) {
								_2G++;
							} else {
								_5G++;
							}
							
							ios 	+= devTypeCount.get(0);
							android += devTypeCount.get(1);
							windows += devTypeCount.get(2);
							speaker += devTypeCount.get(3);
							printer += devTypeCount.get(4);
							others  += devTypeCount.get(5);
							
							if (_11k) {
								_11K_Count++;
							} else if (_11r) {
								_11R_Count++;
							} else if (_11v) {
								_11V_Count++;
							}
							
						}
					}
				}

				dev_map.put("_2G", (int) _2G);
				dev_map.put("_5G", (int) _5G);
				dev_map.put("tx",  _vap_tx_bytes);
				dev_map.put("rx",  _vap_rx_bytes);

				dev_map.put("mac", ios);
				dev_map.put("android", android);
				dev_map.put("win", windows);
				dev_map.put("speaker", speaker);
				dev_map.put("printer", printer);
				dev_map.put("other", others);
				dev_map.put("_11K", _11K_Count);
				dev_map.put("_11R", _11R_Count);
				dev_map.put("_11V", _11V_Count);

				dev.setGraylogtime(grayLogtime);
				deviceService.save(dev, false);

			} catch (Exception e) {
				LOG.error("while processing clients peer list error - > " +e);
				e.printStackTrace();
			}
		}

		return true;
	}

  public boolean peer_assoc( Map<String, Object> map) {
	  
	  try {
		    
		  LOG.info("peer_assoc " +map);
		  
			if (!map.containsKey("uid") || !map.containsKey("peer_mac")) {
				return false;
			}
			
		    String uid 	   		= (String) map.get("uid");
			String mac 	   		= (String) map.get("peer_mac");
			
			String radio_type 	= (String) map.get("radio_type");
			String vap_ssid 	= (String) map.get("vap_ssid");
			
			String wlan_type 	= (String) map.get("wlan_type");
			String peer_caps    = (String) map.get("peer_caps");
			
			int   peer_nss      = 0;
			int signal_strength = 0;
			int peer_rssi       = 0;
			int _peer_tx_bytes  = 0;
			int _peer_rx_bytes  = 0;
			
			if (map.containsKey("peer_rssi"))
				peer_rssi = (int) map.getOrDefault("peer_rssi", 0);
			if (map.containsKey("_peer_tx_bytes"))
				_peer_tx_bytes = (int) map.getOrDefault("_peer_tx_bytes", 0);
			if (map.containsKey("_peer_rx_bytes"))
				_peer_rx_bytes = (int) map.getOrDefault("_peer_rx_bytes", 0);
			if (map.containsKey("peer_nss"))
				peer_nss = (int) map.get("peer_nss");
			if (map.containsKey("peer_signal_strength"))
				signal_strength = (int) map.get("peer_signal_strength");

			boolean _11v = (boolean) map.get("11v");
			boolean _11k = (boolean) map.get("11k");
			boolean _11r = (boolean) map.get("11r");
			
			Device dev =  null;
			dev = deviceService.findOneByUid(uid);

			if (dev == null) {
				LOG.info(" DEVICE NOT FOUND " + uid);
				return false;
			}

			String cid 		= dev.getCid();
			String sid 		= dev.getSid();
			String spid 	= dev.getSpid();

			ClientDevice client = clientDeviceService.findByMac(mac);
			
			if (client == null) {
				client = new ClientDevice();
				client.setCreatedOn(new Date());
				client.setCreatedBy("cloud");
				client.setMac(mac);
				String peermac = mac.replaceAll("[^a-zA-Z0-9]", "");
				client.setPeermac(peermac);
			}
				
			client.setLastactive(System.currentTimeMillis());
			client.setCid(cid);
			client.setSid(sid);
			client.setSpid(spid);
			
			client.setRadio_type(radio_type);
			if (vap_ssid != null)
				client.setSsid(vap_ssid);
			client.setPeer_rssi(String.valueOf(peer_rssi));
			client.setCur_peer_tx_bytes(_peer_tx_bytes);
			client.setCur_peer_rx_bytes(_peer_rx_bytes);
			
			client.setPrev_peer_tx_bytes(_peer_tx_bytes);
			client.setPrev_peer_rx_bytes(_peer_rx_bytes);

			String entryTime = customerUtils.getCurrentTimeForZone(cid);
			client.setEntryTime(entryTime);

			String uuid = uid.replaceAll("[^a-zA-Z0-9]", "");
			client.setUid(uid);
			client.setUuid(uuid);
			
			client.set_11k(_11k);
			client.set_11r(_11r);
			client.set_11v(_11v);
			
			client.setWlan_type(wlan_type);
			client.setPeer_caps_client(peer_caps);
			client.setNo_of_streams(peer_nss);
			client.setPeer_signal_strength(signal_strength);
			client.setPeer_conn_time(0);
			
			JSONObject manufacture = nmeshRetailerRestController.deviceManufacture(mac);

			String hostName    = (String) manufacture.get("manufactureName");
			String os 		   = (String) manufacture.get("os");
			
			client.setTypefs(os);
			client.setPeer_hostname(hostName);
			client.setStatus("active");

			getClientDeviceService().save(client);
			
			return true;
			
		} catch (Exception e) {
			LOG.error("While processing peer_assoc event error-> " + e);
			e.printStackTrace();
		}
		return false;
	}

	public boolean peer_disassoc(Map<String, Object> map) {

		try {

			if (!map.containsKey("uid") || !map.containsKey("peer_mac")) {
				return false;
			}
		
			String uid = (String) map.get("uid");
			String mac = (String) map.get("peer_mac");

			LOG.info("peer_disassoc " + map);

			ClientDevice client = clientDeviceService.findByMac(mac);

			if (client != null) {

				String cid	 	 = client.getCid();
				String sid 		 = client.getSid();
				String spid 	 = client.getSpid();
				String entryTime = client.getEntryTime();

				long prev_tx = client.getPrev_peer_tx_bytes();
				long prev_rx = client.getPrev_peer_rx_bytes();

				long cur_tx = client.getCur_peer_tx_bytes();
				long cur_rx = client.getCur_peer_rx_bytes();

				String exitTime = customerUtils.getCurrentTimeForZone(cid);

				Date entry = format.parse(entryTime);
				Date exit  = format.parse(exitTime);

				long elapsedTime = CustomerUtils.calculateElapsedTime(entry, exit);

				String devStatus = client.getStatus();
				String policy 	 = client.getPid() == null ? "NA" : client.getPid();
				
				boolean isBlocked = isBlocked(policy);

				if (isBlocked || devStatus.equals("blocked")) {
					client.setExitTime(exitTime);
					clientDeviceService.save(client);
					LOG.info(" disconnected event arrived  Mac ID blocked by admin " +mac);
				} else {
					clientDeviceService.delete(client);
				}

				long used_tx = cur_tx - prev_tx;
				long used_rx = cur_rx - prev_rx;

				if (used_tx < 0) {
					used_tx = 0;
				}
				if (used_rx < 0) {
					used_rx = 0;
				}
				//LOG.info(" USERS TX " + used_tx + " USERS RX " + used_rx);

				if (elapsedTime >= 0) {
					this.client_Disc_post(uid, sid, spid, cid, mac, used_tx, used_rx, entryTime, exitTime,
							elapsedTime);
				}
			}
			return true;

		} catch (Exception e) {
			LOG.error("While processing peer_disassoc Event Error -> " + e);
			e.printStackTrace();
		}

		return false;
	}

	public void client_Disc_post(String uid, String sid, String spid, String cid, String peermac, long tx, long rx,
			String entryTime, String exitTime, long elapsedTime) {

		HashMap<String, Object> reportmap = new HashMap<String, Object>();

		reportmap.put("opcode", "device_details");
		reportmap.put("uid", uid);

		if (cid != null)
			reportmap.put("cid", cid);
		if (sid != null)
			reportmap.put("sid", sid);
		if (spid != null)
			reportmap.put("spid", spid);

		reportmap.put("peer_mac", 		peermac);
		reportmap.put("peer_tx", 		tx);
		reportmap.put("peer_rx", 		rx);
		reportmap.put("entry_time", 	entryTime);
		reportmap.put("exit_time", 		exitTime);
		reportmap.put("elapsed_time", 	elapsedTime);

		//LOG.info(" @@@@@@@@ DISS CONNECTION DATA PUSH ES @@@@@@@@@@  " + reportmap);

		elasticService.post(device_history_event, "location_change_event", reportmap);

		reportmap.clear();
	}
	
	private ClientDeviceService getClientDeviceService() {

		try {
			if (clientDeviceService == null) {
				clientDeviceService = Application.context.getBean(ClientDeviceService.class);
			}
		} catch (Exception e) {

		}
		return clientDeviceService;
	}
	
	public boolean isBlocked(String policy) {
		boolean isBlocked = false;
		switch (policy) {
		case "uid":
			isBlocked = true;
			break;
		case "Customer":
			isBlocked = true;
			break;
		case "Venue":
			isBlocked = true;
			break;
		case "Floor":
			isBlocked = true;
			break;
		}

		return isBlocked;
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/deviceCounts", method = RequestMethod.GET)
	public JSONObject activeClientsCount(
			 				 @RequestParam(value="cid", required=false) String cid,
							 @RequestParam(value="sid", required=false) String sid, 
				 	      	 @RequestParam(value="spid",required=false) String spid,
				 	      	 @RequestParam(value="uid", required=false) String uid) throws IOException {

			JSONArray  dev_array = null;
			JSONObject devlist 	 = new JSONObject();
			dev_array = new JSONArray();
			
			List<Device> devices = null;
			
			if (uid !=null) {
				devices = deviceService.findByUid(uid);
			} else if (spid !=null) {
				devices = deviceService.findBySpid(spid);
			} else if (sid !=null) {
				devices =deviceService.findBySid(sid);
			} else {
				devices = deviceService.findByCid(cid);
			}
			
			int deployedDevices = 0;
			int _2G 			= 0;
			int _5G		 		= 0;
			int total 			= 0;
			
			if (devices != null) {
				for (Device device : devices) {
					if ("ap".equals(device.getTypefs()))
						   deployedDevices++;
				}
			}
			
			JSONArray  _5G_array = new JSONArray();
			JSONArray  _2G_array = new JSONArray();
			JSONArray  tot_array = new JSONArray();
			JSONArray  devCount_array = new JSONArray();
			
	    	
			_2G_array.add (0, "2G");
			_2G_array.add (1, _2G);
			
			
			_5G_array.add (0, "5G");
			_5G_array.add (1, _5G);
			
			total = _2G+_5G;
			
			tot_array.add (0, "Total");
			tot_array.add (1, total);
			
			devCount_array.add (0, "DeployedDevices");
			devCount_array.add (1, deployedDevices);
	
			dev_array.add  (0, _2G_array);
			dev_array.add  (1, _5G_array);
			dev_array.add  (2, tot_array);
			dev_array.add  (3, devCount_array);
			
			devlist.put("devicesCounts", dev_array);	
			
			return devlist;
		}


	public boolean dcs_chan_switch(Map<String, Object> map) {
		
		try {

			String uid 			 = (String)map.get("uid");
			String bssid 		 = (String)map.get("bssid");
			String ssid 		 = (String)map.get("ssid");
			String radio_type	 = (String)map.get("radio_type");
			
			int channel_number = 0;
			if (map.containsKey("CHANNEL")) {
				channel_number = (int) map.get("CHANNEL");
			}

			Device device = deviceService.findOneByUid(uid);

			if (device == null) {
				LOG.info(" DEVICE NOT FOUND " + uid);
				return false;
			}
			
			if (channel_number != 0) {
				if (radio_type.contains("2g")) {
					device.setTwogChannel(channel_number);
				} else {
					device.setFivegChannel(channel_number);
				}
				deviceService.save(device,false);
			}

			String cid 		= device.getCid();
			String sid 		= device.getSid();
			String spid 	= device.getSpid();
			
			HashMap<String, Object> report_map = new HashMap<String, Object>();
			
			report_map.put("opcode",    "dcs_chan_switch");
			report_map.put("cid", 		cid);
			if (sid != null)
				report_map.put("sid",	sid);
			if (spid != null)
				report_map.put("spid",  spid);
			report_map.put("uid", 		uid);
			report_map.put("bssid", 	bssid);
			report_map.put("ssid", 		ssid);
			report_map.put("radio_type",radio_type);
			report_map.put("channel", 	channel_number);
			report_map.put("bandwidth", map.get("BANDWIDTH"));

			//LOG.info(" @@@@@@@@ dcs_chan_switch DATA PUSH ES @@@@@@@@@@  " + report_map);

			elasticService.post(device_history_event, "device_history_dcs_chan_switch", report_map);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

}