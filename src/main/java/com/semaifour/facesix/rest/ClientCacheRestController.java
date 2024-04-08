package com.semaifour.facesix.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.data.mongo.device.*;
import com.semaifour.facesix.stats.ClientCache;
import com.semaifour.facesix.util.CustomerUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RequestMapping("/rest/client/cache")
@RestController
public class ClientCacheRestController {
	
	@Autowired
	ClientCache clientCache;
	
	@Autowired
	DeviceService deviceService;
	
	@Autowired
	ClientDeviceService clientDeviceService;
	
	@GetMapping("/device_assoc_client")
	public JSONObject device_assoc_client(
			@RequestParam(value="uid",required=false) final String uid,
			@RequestParam(value="spid",required=false) final String spid,
			@RequestParam(value="sid",required=false) final String sid,
			@RequestParam(value="cid",required=false) final String cid) {
		
		List<Device> device = null;
		
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
		int _11K_Count			= 0;
		int _11R_Count 			= 0;
		int _11V_Count 			= 0;
		
		List<ClientDevice> blockedClients = null;
		
		JSONObject obj 			= new JSONObject();
		JSONObject peerList 	= null;
		JSONArray peerListArray = new JSONArray();

		if (uid != null) {
			device = deviceService.findByUid(uid);
		} else if (spid != null) {
			device = deviceService.findBySpid(spid);
		} else if (sid != null) {
			device = deviceService.findBySid(sid);
		} else {
			device = deviceService.findByCid(cid);
		}
		
		if (device != null) {
			
			for (Device dev : device) {

				String dev_uid = dev.getUid().toLowerCase();
				String alias   = dev.getName();
				
				String x   = dev.getXposition();
				String y   = dev.getYposition();

				blockedClients = clientDeviceService.findByUid(dev_uid);
				
				if (blockedClients != null && blockedClients.size() >0) {
					blocked_Count += blockedClients.size();
				}
				
				if (clientCache.containKey(dev_uid)) {

					ConcurrentHashMap<String, HashMap<String, Object>> unsorted_map = clientCache.get_assoc_device_clients(dev_uid);
					
					if (unsorted_map == null || unsorted_map.isEmpty())
						continue;
					
					Map<String, HashMap<String, Object>> sorted_map = Collections.synchronizedMap(new TreeMap<String,HashMap<String, Object>>(unsorted_map));
					
					for(ConcurrentHashMap.Entry<String, HashMap<String, Object>> temp_map : sorted_map.entrySet()) {

						peerList  = new JSONObject();
						
						String peerMac = temp_map.getKey();
						
						HashMap<String, Object> peerMap = temp_map.getValue();
						
	    				String ssid  = (String)peerMap.getOrDefault("ssid", "UNKNOWN");
	    				int rssi     = (int)peerMap.getOrDefault("peer_rssi", 0);
	    				String ip    = (String)peerMap.getOrDefault("ip", "0.0.0.0");

	    				String devtype   = (String)peerMap.getOrDefault("os", "laptop");
	    				String radioType = (String)peerMap.getOrDefault("radio_type", "2.4Ghz");
	    				
	    				double peer_tx = Double.valueOf(peerMap.getOrDefault("_peer_tx_bytes",0).toString());
	    				double peer_rx = Double.valueOf(peerMap.getOrDefault("_peer_rx_bytes",0).toString());
	    				
	    				String  tx = CustomerUtils.convertDataUsage(peer_tx);
	    				String  rx = CustomerUtils.convertDataUsage(peer_rx);
	    				String hostname = (String) peerMap.getOrDefault("host", "-");

	    				long conntime = Long.parseLong(peerMap.getOrDefault("peer_conntime",0).toString());
	    				
	    				String  peer_conntime = CustomerUtils.secondsto_hours_minus_days(conntime);
	    				
						boolean _11r = (boolean)peerMap.get("11r");
	    				boolean _11k = (boolean)peerMap.get("11k");
	    				boolean _11v = (boolean)peerMap.get("11v");
	    				
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
	    				
	    				if (_11k) {
	    					_11K_Count++;
	    				} else if (_11r) {
	    					_11R_Count++;
	    				} else if (_11v) {
	    					_11V_Count++;
	    				}
	    				
						
						peerList.put("mac_address",  peerMac);
						peerList.put("rssi", 		rssi);
						peerList.put("tx", 			tx);
						peerList.put("rx", 			rx);
						peerList.put("k11", 		_11k);
	    				peerList.put("r11", 		_11r);
	    				peerList.put("v11", 		_11v);
	    				peerList.put("os",  		 devtype);
	    				peerList.put("host",      hostname);
	    				peerList.put("ssid", 		ssid);
	    				peerList.put("radio", 		radioType);
	    				peerList.put("uid", 		dev_uid);
	    				peerList.put("ap", 	    	dev_uid);
	    				peerList.put("associated",  "Yes");
	    				peerList.put("channel", 	"NA");
	    				peerList.put("ip", 	    	ip);
	    				peerList.put("conn_time",	peer_conntime);
	    				peerList.put("location", 	alias);
	    				peerList.put("x", x);
	    				peerList.put("y", y);
	    				
	    				peerListArray.add(peerList);
					}
				}
			}
			
			totCount = _2G + _5G;
			
			obj.put("clientConnected", peerListArray);
			
			JSONObject clientType = new JSONObject();
			
			clientType.put("ios", ios);
			clientType.put("android", android);
			clientType.put("windows", windows);
			clientType.put("speaker", speaker);
			clientType.put("printer", printer);
			clientType.put("others", others);
			
			obj.put("clientType", clientType);
			
			JSONObject radioType = new JSONObject();
			radioType.put("_2G", _2G);
			radioType.put("_5G", _5G);
			radioType.put("total", totCount);
			radioType.put("block", blocked_Count);
			
			obj.put("radioType", radioType);
			
			JSONObject clientCapability = new JSONObject();
			clientCapability.put("_11k", _11K_Count);
			clientCapability.put("_11r", _11R_Count);
			clientCapability.put("_11v", _11V_Count);

			obj.put("clientCapability", clientCapability);
			
		}
		return obj;
	}
	
	@GetMapping("/get_assoc_client")
	public HashMap<String, Object> get_assoc_client(@RequestParam("uid") final String uid,
													@RequestParam("peer_mac") final String peer_mac) {
		return clientCache.get_assoc_client(uid, peer_mac);
	}
	
	
	@GetMapping("/clear_device_assoc_client")
	public ConcurrentHashMap<String, HashMap<String, Object>> clear_device_assoc_client(@RequestParam("uid") final String uid) {
		return clientCache.clear_device_assoc_client(uid);
	}
	
	@GetMapping("/clear_assoc_client")
	public void clear_assoc_client(@RequestParam("uid") final String uid,
								   @RequestParam("peer_mac") final String peer_mac) {
		clientCache.clear_assoc_client(uid, peer_mac,null,"5Ghz");
	}

	@PostMapping("/assoc")
	public void peer_assoc(@RequestBody(required = true) Map<String, Object> map) {
		clientCache.peer_assoc(map);
	}
	
	@PostMapping("/disassoc")
	public void peer_disassoc(@RequestBody(required = true) Map<String, Object> map) {
		clientCache.peer_disassoc(map);
	}
}
