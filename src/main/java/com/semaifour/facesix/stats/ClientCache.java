package com.semaifour.facesix.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.rest.NmeshRetailerRestController;
import com.semaifour.facesix.spring.CCC;

/**
 * 
 * Cache that maintains all the client list
 * 
 */

@Component
public class ClientCache {
	
	static Logger LOG = LoggerFactory.getLogger(ClientCache.class.getName());
	
	@Value("${facesix.trilaterationscheduledtask.enable}")
	private boolean tritask_enable;
	
	@Autowired
	private NmeshRetailerRestController nmeshRetailerRestController;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private CCC _CCC;
	
	private String 	indexname = "device-history-event";
	

	@PostConstruct
	public void init() {
		indexname = _CCC.properties.getProperty("facesix.device.event.history.table",indexname);
	}
	
	private final ConcurrentHashMap<String, ConcurrentHashMap<String,HashMap<String,Object>>> cache = new ConcurrentHashMap<String, ConcurrentHashMap<String,HashMap<String,Object>>>();
	
	/**
	 * Add or Update the client list into the cache by its uid and peer_mac
	 * 
	 * @param uid 
	 * @param peer_mac 
	 * @param peer_list 
	 */
	public void add(final String uid, final String peer_mac, final HashMap<String, Object> peer_list) {
		
		if (!cache.containsKey(uid) ) {
			final ConcurrentHashMap<String, HashMap<String, Object>> client_peer = new ConcurrentHashMap<>();
			client_peer.put(peer_mac, peer_list);
			cache.putIfAbsent(uid, client_peer);
		} else {
			if (cache.containsKey(uid) && cache.get(uid).containsKey(peer_mac)) {
				cache.get(uid).get(peer_mac).putAll(peer_list);
			} else if (cache.containsKey(uid) && !cache.get(uid).containsKey(peer_mac)) {
				cache.get(uid).put(peer_mac, peer_list);
			}
		}
	}

	/**
	 * 
	 *  Invalidating the clients for every 4 minutes when there is no update
	 *  
	 */
	@Scheduled(fixedDelay = 10000)//4 minutes = 240000 
	public void makeClient_inactive() {
		/*if (!tritask_enable) {
 			return;
		}*/
		if (this.cache != null) {
			Set<String> keys = this.cache.keySet();
			for (String uid : keys) {
				if (this.cache.containsKey(uid)) {
					ConcurrentHashMap<String, HashMap<String, Object>> peer_map = this.cache.get(uid);
					if (peer_map != null) {
						for (ConcurrentHashMap.Entry<String, HashMap<String, Object>> client : peer_map.entrySet()) {
							String peerMac = client.getKey();
							if(client.getValue().containsKey("last_seen")) {
								long last_seen = (long) client.getValue().get("last_seen");
								boolean timeExceeds = isTimeExceeds(last_seen);
								if (timeExceeds) {
									clear_assoc_client(uid, peerMac,null,"5Ghz");
								}
							}
						}
					}
				}
			}
		}

	}
	
	/**
	 * 
	 * Check the assoc_client_mac available in the cache 
	 * @param assoc_client_mac
	 * @return
	 * 
	 */
	public boolean  findByClientMac(String assoc_client_mac) {

		boolean isAvailable = false;
		
		if (this.cache != null) {
			Set<String> keys = this.cache.keySet();
			for (String uid : keys) {
				if (this.cache.containsKey(uid)) {
					ConcurrentHashMap<String, HashMap<String, Object>> peer_map = this.cache.get(uid);
					if (peer_map != null && peer_map.containsKey(assoc_client_mac)) {
						isAvailable  = true;
					}
				}
			}
		}
		
		return isAvailable;

	}
	
	/**
	 * check the last_seen time diffreence exceeds 4 minutes
	 * @param last_seen
	 * @return
	 * 
	 */
	public boolean isTimeExceeds(long last_seen) {
		long currentTime = System.currentTimeMillis();
		// 4 minutes = 240000 milliseconds 
		// 2 minutes = 120000 milliseconds 
		// 6 minutes = 360000 milliseconds
		long time_difference = currentTime - last_seen;
		if(time_difference >= 240000) {// if time difference is more than 6 min 
			return true;
		}
		return false;
	}
	
	public void peer_assoc(Map<String, Object> map) {
		
		LOG.info("peer_assoc " +map);
		
		HashMap<String, Object> peer_map = new HashMap<String, Object>(map);
		
		String uid      = (String)peer_map.get("uid");
		String peer_mac = (String)peer_map.get("peer_mac");
		
		JSONObject manufacture = nmeshRetailerRestController.deviceManufacture(peer_mac);

		String hostName    = (String) manufacture.get("manufactureName");
		String os 		   = (String) manufacture.get("os");
		
		peer_map.put("os", os);
		peer_map.put("host", hostName);
		peer_map.put("last_seen",System.currentTimeMillis());
		
		this.add(uid, peer_mac,peer_map);
		
	}
	
	public void peer_disassoc(Map<String, Object> map) {
		
		
		String peer_mac = (String) map.get("peer_mac");
		String uid      = (String) map.get("uid");
		String bssid    = (String) map.get("bssid");
		String radio    = (String) map.get("radio_type");
		
		HashMap<String, Object> clientCache = this.get_assoc_client(uid, peer_mac);
		
		this.clear_assoc_client(uid, peer_mac ,bssid,radio);
		
		/**
		 * Report push
		 */
		
		Device dev = deviceService.findOneByUid(uid);

		String cid 	= null;
		String sid 	= null;
		String spid = null;
		
		if (dev != null) {
			 cid 	= dev.getCid();
			 sid 	= dev.getSid();
			 spid 	= dev.getSpid();
		}

		HashMap<String, Object> reportmap = new HashMap<String, Object>();

		reportmap.put("opcode", "device_details");
		reportmap.put("uid", uid);

		LOG.info("Disassoc" + map);
		
			if (sid != null)
				reportmap.put("sid", sid);
			if (spid != null)
				reportmap.put("spid", spid);

			if (clientCache != null && clientCache.size() > 0) {
				
				reportmap.put("peer_mac", 		 peer_mac);
				reportmap.put("cid", 		 	 cid);
				reportmap.put("_peer_tx_bytes",  clientCache.get("_peer_tx_bytes"));
				reportmap.put("_peer_rx_bytes",  clientCache.get("_peer_rx_bytes"));
				reportmap.put("peer_conntime", 	 clientCache.get("peer_conntime"));
				reportmap.put("ip", 			 clientCache.getOrDefault("ip", "0.0.0.0"));
				reportmap.put("ssid", clientCache.get("ssid"));
				reportmap.put("radio_type", clientCache.get("radio_type"));
				reportmap.put("os", clientCache.get("os"));
				
				if(elasticService != null) {
					elasticService.post(indexname, "location_change_event", reportmap);
				}
			}
			
	}
	
	public ConcurrentHashMap<String, HashMap<String, Object>> get_assoc_device_clients(final String uid) {
		return cache.get(uid);
	}
	
	public Collection<ConcurrentHashMap<String, HashMap<String, Object>>> findAllClients() {
		return cache.values();
	}
	
	public HashMap<String, Object> get_assoc_client(final String uid,final String peer_mac) {
		if(cache.containsKey(uid) && cache.get(uid).containsKey(peer_mac)) {
			return cache.get(uid).get(peer_mac);
		}
		return new HashMap<String, Object>();
	}

	public ConcurrentHashMap<String, HashMap<String, Object>> clear_device_assoc_client(final String uid) {
		cache.get(uid).clear();
		return get_assoc_device_clients(uid);
	}

	public void clear_assoc_client(final String uid, final String peer_mac ,final String bssid ,final String radio) {
		if(cache.containsKey(uid) && cache.get(uid).containsKey(peer_mac)) {
			HashMap<String, Object>  client_map = cache.get(uid).get(peer_mac);
			
			String client_bssid = (String) client_map.get("bssid");
			
			if(bssid != null && bssid.equals(client_bssid)){
				cache.get(uid).remove(peer_mac);
			} else if(bssid == null || bssid.length()==0){
				cache.get(uid).remove(peer_mac);
			} else{
				this.cache.get(uid).get(peer_mac).put("radio_type",radio);
			}
			
		}
	}
	
	public boolean containKey(final String uid) {
		if (uid!=null && cache.containsKey(uid)) {
			ConcurrentHashMap<String, HashMap<String, Object>> map = cache.get(uid);
			if (map == null || map.isEmpty()) {
				return false;
			}
			return true;
		} else
			return false;
	}
}