package com.semaifour.facesix.outsource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.web.WebController;

@RestController
@RequestMapping("/rest/outsource")
public class OutsourceRestController extends WebController{
	
	static Logger LOG = LoggerFactory.getLogger(OutsourceRestController.class.getName());
	
	public static ConcurrentHashMap<String,Long> keepAlive = new ConcurrentHashMap<String,Long>(); 
	
	@Autowired
	private DeviceEventPublisher mqttPublisher;
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private BeaconService  beaconService;
	
	private String 	device_history_event = "device-history-event";
	
	@Autowired
	private BeaconDeviceService beacondeviceService;
	
	private static final int KEEP_ALIVE_INTERVAL = 3 * 60 * 1000; //three minutes
	
	@PostConstruct
	public void init() {
		device_history_event = _CCC.properties.getProperty("facesix.device.event.history.table",device_history_event);
	}
	
	@RequestMapping(value = "/ibeacon", method = RequestMethod.POST)
	public void ibeacon(@RequestBody byte[] value){
		try {
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(value));
			BufferedReader br = new BufferedReader(new InputStreamReader(gis,"UTF-8"));
			StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        String result = sb.toString();
	       
	      //  LOG.info("Ibeacon" + result);
	        
	        br.close();
	        gis.close();
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(result);
			long timestamp = System.currentTimeMillis();
			json.put("cloudtimestamp", timestamp);
			
			String opcode = "beacon-payload";
			String template = " \"opcode\":\"{0}\",\"uid\":\"{1}\",\"message\":{2}";
			String uid = null;
			if (json.containsKey("gateway_euid")) {
				uid = json.get("gateway_euid").toString();
			} else {
				uid = "00:00:00.00.00:00";
			}
			String message = MessageFormat.format(template, new Object[] { opcode, uid, json });
			mqttPublisher.publish("{" + message + "}", uid);

			// gateway status elastic post
			this.cluGatewayStatusHistory(json);
			
			
		} catch(Exception e) {
			LOG.info("exception while reading value "+ value);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param payload
	 * @param source
	 */
	
	@RequestMapping(value = "/cluGatewayStatusHistory", method = RequestMethod.POST)
	public boolean cluGatewayStatusHistory(JSONObject payload) {

		try {
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			DateFormat parse  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			parse.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			String zoneName = parse.getTimeZone().getDisplayName(false, 0);
			
			String cloudSeen = format.format(new Date());
				
			long tagCount = 0;
			
			String uid = (String)payload.get("gateway_euid");
			
			BeaconDevice beacondevice = beacondeviceService.findOneByUid(uid);
			
			if (beacondevice != null) {

				long timestamp = System.currentTimeMillis();
				keepAlive.put(uid, timestamp);
				
				String	serverSentTime = payload.get("timestamp").toString();
				serverSentTime = format.format(new Date(Long.valueOf(serverSentTime)));
								
				ArrayList<String> tags = new ArrayList<String>();
				
				if (payload.containsKey("events")) {
					
					JSONArray tagList = (JSONArray) payload.get("events");
					Iterator<JSONObject> tagIter = tagList.iterator();

					while (tagIter.hasNext()) {
						JSONObject jsonObject = tagIter.next();
						String tagId = (String)jsonObject.get("device_euid");
						String deviceeuid = tagId.substring(tagId.length()-17);
						tags.add(deviceeuid);
					}
					
					List<Beacon> beaconList = beaconService.findByMacaddrs(tags);
					
					if (CollectionUtils.isNotEmpty(beaconList)) {
						tagCount = beaconList.size();
					}
				}
				
				HashMap<String, Object> gatewayStatuspayload = new HashMap<String, Object>();
				gatewayStatuspayload.put("opcode", 		"gateway_status");
				gatewayStatuspayload.put("uid",    		uid);
				gatewayStatuspayload.put("tagCount",    tagCount);
				gatewayStatuspayload.put("serverSent",  serverSentTime);
				gatewayStatuspayload.put("cloudSeen",   cloudSeen+" "+zoneName);
				
				elasticService.post(device_history_event, "clu_gateway_status_history", gatewayStatuspayload);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public JSONObject gatewayStatus(String uid) {

		String state 	= BeaconDevice.THIRD_PARTY_STATE.ACTIVE.name();
		String lastSeen = "NA";
		
		if (keepAlive.containsKey(uid)) {

			long lastUpdatedTime 	 = keepAlive.get(uid);
			long diffFromKAInterval	 = System.currentTimeMillis() - KEEP_ALIVE_INTERVAL;

			if (lastUpdatedTime < diffFromKAInterval) {
				state = BeaconDevice.THIRD_PARTY_STATE.PAUSED.name();
			} else {
				state = BeaconDevice.THIRD_PARTY_STATE.ACTIVE.name();
			}

			long lastSeenMilliSec = keepAlive.get(uid);
			
			DateFormat df 		= new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			Date lastSeenDate 	= new Date(lastSeenMilliSec);
			lastSeen 			= df.format(lastSeenDate);
			
		} else {
			state = BeaconDevice.THIRD_PARTY_STATE.UNKNOWN.name();
		}

		JSONObject gatewayState = new JSONObject();
		gatewayState.put("state", 	 state);
		gatewayState.put("lastSeen", lastSeen);
		
		return gatewayState;
	}
}
