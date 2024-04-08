
package com.semaifour.facesix.data.mongo.device;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.semaifour.facesix.account.rest.CaptivePortalRestController;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SimpleCache;

@Service
public class DeviceService {
	
	static Logger LOG = LoggerFactory.getLogger(DeviceService.class.getName());

	@Autowired
	private DeviceRepository repository;
	
	@Autowired
	private DeviceEventPublisher mqttPublisher;
	
	@Autowired
	ClientDeviceService  clientDeviceService;
	
	@Autowired
	CaptivePortalRestController captivePortalRestController;
	
	@Autowired
	DeviceBssidService deviceBssidService;
	
	@Autowired
	DeviceBssidRepository deviceBssidRepository;
	
	String mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"by\":\"{2}\", \"newversion\":\"{3}\", \"value\":{4}, \"name\":\"{5}\" ";

	@Autowired
	SimpleCache<HeartBeat> deviceHealthCache;
	
	@Autowired
	CustomerUtils customerUtils;
	
	public DeviceService() {
	}
	
	public Page<Device> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}
	
	public List<Device> findByName(String name) {
		return repository.findByName(name);
	}
	
	public List<Device> findByUid(String uid) {
		return repository.findByUid(uid);
	}
	
	public Device findOneByName(String name) {
		List<Device> list = findByName(name);
		if (list != null & list.size() > 0) return list.get(0);
		return null;
	}
	
	public Device findOneByUid(String uid) {
		List<Device> list = findByUid(uid);
		if (list != null & list.size() > 0 ) {
			Device bdev = list.get(0);
			if (uid.equalsIgnoreCase(bdev.getUid())){
				return bdev;
			}			
		}
		return null;
	}
	
	public Device findByUidAndCid(String uid,String cid) {
		List<Device> device  = repository.findByUidAndCid(uid,cid);
		if (device != null && device.size() > 0) {
			Device dev = device.get(0);
			if (dev.getUid().equalsIgnoreCase(uid)) {
				return dev;
			}
		} 
		return null;
	}
	
	public Device findById(String id) {
		return repository.findOne(id);
	}
	
	public boolean exists(String id) {
		return repository.exists(id);
	}
	
	public boolean exists(String uid, String name) {
		if (findOneByUid(uid) != null) return true;
		if (findOneByName(name) != null) return true;
		return false;
	}
	
	public void deleteAll() {
		repository.deleteAll();
	}
	
	public void delete(String id) {
		Device device = repository.findOne(id);
		repository.delete(id);
		if (device != null) {
			notify(device, "DELETE");
		}
	}
	
	public void delete(Device device) {
		
		String uid = device.getUid();
		repository.delete(device);
		
		List<DeviceBssid> devicebssid = deviceBssidRepository.findByUid(uid);
		if (devicebssid != null) {
			devicebssid.forEach(dev -> {
				deviceBssidRepository.delete(dev.getId());
			});
		}
		notify(device, "DELETE");
	}
	
	public long count() {
		return repository.count();
	}
	
	
	public List<Device> findBySid(String sid){
		return repository.findBySid(sid);
	}
	
	public List<Device> findBySpid(String spid){
		return repository.findBySpid(spid);
	}
	
	public List<Device> findByCid(String cid){
		return repository.findByCid(cid);
	}
	
	/**
	 * Save device and notify 
	 * 
	 * @param device
	 * @return
	 */
	public Device save(Device device) {
		return save(device, true);
	}
	
	/**
	 * 
	 * Save device and notify=true or false
	 * 
	 * @param device
	 * @param notify
	 * @return
	 */
	public Device save(Device device, boolean notify) {
		device = repository.save(device);
		//LOG.info("Device saved successfully :" + device.getId());
		if (device.getPkid()== null) {
			device.setPkid(device.getId());
			device = repository.save(device);
		}
		if (notify) {
			notify(device, "UPDATE");
		}
		return device;
	}
	
	public Device reset(Device device, boolean notify) {
		if (notify) {
			notify(device, "RESET");		
		}
		LOG.info("Device reset successfully :" + device.getId());
		return device;
	}	

	public Iterable<Device> findAll() {
		return repository.findAll();
	}
	public List<Device> findByStatus(String status) {
		return repository.findByStatus(status);
	}

	public List<Device> findByStatus(String status,Sort sort) {
		return repository.findByStatus(status,sort);
	}
	
	public void updateDeviceHealth(Map<String, Object> map) {
		HeartBeat beat = new HeartBeat(map);
		this.deviceHealthCache.putForGood(beat.getUid(), beat);
	}
	
	public Collection<HeartBeat> getAllDeviceHealth() {
		return this.deviceHealthCache.values();
	}
	
	public HeartBeat getDeviceHealth(String uid) {
		return this.deviceHealthCache.get(uid);
	}
	
	public void clearDeviceHealthCache() {
		this.deviceHealthCache.clear();
	}
		
	public HeartBeat clearDeviceHealthCache(String uid) {
		return this.deviceHealthCache.clear(uid);
	}
	
	/**
	 * 
	 * Sends a notification message to the device with given opcode
	 * 
	 * 	<pre>
	 *  { 
	 *    "opcode":"UPDATE|DELETE|RESET", "uid":"xxxxxx", "by":"modified_by", 
	 *    "newversion":"new version numer", "value":{..config..}, 
	 *    "name":"name of device"
	 *  }
     * </pre>
	 * @param device
	 * @param opcode
	 * @return
	 */
	public boolean notify(Device device, String opcode) {
		try {
			String message = MessageFormat.format(mqttMsgTemplate,new Object[]{opcode, 
																	   device.getUid().toLowerCase(),
																	   device.getModifiedBy(),
																	   "1",
																	   device.getConf(),
																	   device.getName()});
			mqttPublisher.publish("{" + message + "}", device.getUid().toLowerCase());
			LOG.debug("Device Config MQTT Data" + message);
			return true;
		} catch (Exception e) {
			LOG.warn("Failed to notify update", e);
			return false;
		}
	}
		
	public Device saveAndSendMqtt(Device device, boolean notify) {
		device = repository.save(device);
		LOG.info(" Device Saved " + device.getUid());
		
		if (device.getPkid()== null) {
			device.setPkid(device.getId());
			device = repository.save(device);
		}
		
		if (notify) {
			mqttNotify(device, "UPDATE");
		}
		return device;
	}

	
	private boolean mqttNotify(Device device, String opcode) {
		try {
			
			String conf =  device.getConf();
			
			JSONObject template	= customerUtils.stringToSimpleJson(conf);
			
			String cid 			= StringUtils.isEmpty(device.getCid()) ? ""  :device.getCid();
			String sid 			= StringUtils.isEmpty(device.getSid()) ? ""  :device.getSid();;
			String spid 		= StringUtils.isEmpty(device.getSpid()) ? ""  :device.getSpid();;
			
			template.put("cid", cid);
			template.put("sid", sid);
			template.put("spid", spid);
			
			int clientBalancer = device.getNetwork_balancer();
						
			template.put("network_balancer", clientBalancer);
			
			String keepalive = StringUtils.isEmpty(device.getKeepAliveInterval()) ? "30" : device.getKeepAliveInterval();
			template.put("keepalive", keepalive);
			
			String  root = StringUtils.isEmpty(device.getRoot()) ? "no" : device.getRoot();
			template.put("root", root);

			String lanbridge = device.getLanbridge();
			String wanbridge = device.getWanbridge();

			template.put("lan_bridge", lanbridge);
			template.put("wan_bridge", wanbridge);

			this.pickLanWanConfig(template,device);
			
			String mqttTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"by\":\"{2}\", \"value\":{3}";
			String message = MessageFormat.format(mqttTemplate,new Object[]{opcode,
																	    device.getUid().toLowerCase(),
																	    device.getModifiedBy(),
																	    template.toString()});
			mqttPublisher.publish("{" + message + "}", device.getUid().toLowerCase());
			LOG.info("mqqt message " +message);
			LOG.debug("Device Config New MQTT Data" + message);
			return true;
		} catch (Exception e) {
			LOG.warn("Failed to notify update", e);
			return false;
		}
	}

	public void pickLanWanConfig(JSONObject template,Device device) {
		
		JSONObject ethernet_setting = new JSONObject();
		JSONObject lan_setting 		= new JSONObject();
		JSONObject wan_setting 		= new JSONObject();

		String lan 	    = StringUtils.isEmpty(device.getLan_Flag()) ? "NA" : device.getLan_Flag();
		String lan_only = StringUtils.isEmpty(device.getLan_Only()) ? "NA" : device.getLan_Only();
		String wan      = StringUtils.isEmpty(device.getWan_Flag()) ? "NA" : device.getWan_Flag();
		
		if (lan.equals("lan_static") && wan.equals("wan_static")) {  // case : 1
			
			lan_setting.put("proto",     "static");
			lan_setting.put("ipv4_addr", device.getLan_ipv4_Addr());
			lan_setting.put("ipv4_mask", device.getLan_ipv4_Mask());
			lan_setting.put("dhcp_svr",  "yes");
			
			wan_setting.put("proto",    	"static");
			wan_setting.put("ipv4_addr",    device.getWan_ipv4_Addr());
			wan_setting.put("ipv4_mask",    device.getWan_ipv4_mask());
			wan_setting.put("ipv4_gateway", device.getWan_ipv4_gateway());
			wan_setting.put("ipv4_dns",     device.getWan_ipv4_dns());
			wan_setting.put("ipv4_dns1",    device.getWan_ipv4_dns1());
			
		} else if (lan.equals("lan_static") && wan.equals("wan_dhcp")) { // case : 2
			
			lan_setting.put("proto",     "static");
			lan_setting.put("ipv4_addr", device.getLan_ipv4_Addr());
			lan_setting.put("ipv4_mask", device.getLan_ipv4_Mask());
			lan_setting.put("dhcp_svr",  "yes");
			
			wan_setting.put("proto",    	"dhcp");
			wan_setting.put("ipv4_dns",     device.getWan_ipv4_dhcp_dns());
			wan_setting.put("ipv4_dns1",    device.getWan_ipv4_dhcp_dns1());
			
		} else if (lan.equals("lan_static") && lan_only.equals("lan_only")) { // case : 3
			
			lan_setting.put("proto",     "static");
			lan_setting.put("ipv4_addr", device.getLan_ipv4_Addr());
			lan_setting.put("ipv4_mask", device.getLan_ipv4_Mask());
			lan_setting.put("ipv4_gateway", device.getLan_ipv4_gateway());
			
			lan_setting.put("ipv4_dns",   device.getLan_ipv4_dns());
			lan_setting.put("ipv4_dns1",  device.getLan_ipv4_dns1());
			lan_setting.put("dhcp_svr",  "no");
			
			wan_setting.put("proto",    	"lan_only");
			
		} else if (lan.equals("lan_dhcp") && lan_only.equals("lan_only")) { // case : 4 
			
			lan_setting.put("proto",     "dhcp");
			lan_setting.put("ipv4_dns",   device.getLan_ipv4_dhcp_dns());
			lan_setting.put("ipv4_dns1",  device.getLan_ipv4_dhcp_dns1());
			lan_setting.put("dhcp_svr",  "no");
			
			wan_setting.put("proto",    	"lan_only");
			
		} else if (lan.equals("lan_dhcp") && wan.equals("wan_static")) { //case :5
			
			lan_setting.put("proto",     "dhcp");
			lan_setting.put("ipv4_dns",   device.getLan_ipv4_dhcp_dns());
			lan_setting.put("ipv4_dns1",  device.getLan_ipv4_dhcp_dns1());
			lan_setting.put("dhcp_svr",  "no");
			
			wan_setting.put("proto",    	"static");
			wan_setting.put("ipv4_addr",    device.getWan_ipv4_Addr());
			wan_setting.put("ipv4_mask",    device.getWan_ipv4_mask());
			wan_setting.put("ipv4_gateway", device.getWan_ipv4_gateway());
			wan_setting.put("ipv4_dns",     device.getWan_ipv4_dns());
			wan_setting.put("ipv4_dns1",    device.getWan_ipv4_dns1());
			
		} else if (lan.equals("lan_dhcp") && wan.equals("wan_dhcp")) { //case :6
			
			lan_setting.put("proto",     "dhcp");
			lan_setting.put("ipv4_dns",   device.getLan_ipv4_dhcp_dns());
			lan_setting.put("ipv4_dns1",  device.getLan_ipv4_dhcp_dns1());
			lan_setting.put("dhcp_svr",  "no");
			
			wan_setting.put("proto",    	"dhcp");
			wan_setting.put("ipv4_dns",     device.getWan_ipv4_dhcp_dns());
			wan_setting.put("ipv4_dns1",    device.getWan_ipv4_dhcp_dns1());
		}
		
		JSONArray lan_setting_array = new JSONArray();
		JSONArray wan_setting_array = new JSONArray();
		
		lan_setting_array.add(lan_setting);
		ethernet_setting.put("lan",lan_setting_array);
		
		wan_setting_array.add(wan_setting);
		ethernet_setting.put("wan",wan_setting_array);
		
		template.put("network_setting", ethernet_setting);
		
	}

	public Iterable<Device> findByCidAndState(String cid, String state) {
		return repository.findByCidAndState(cid,state);
	}
	public List<Device> findBySidAndState(String sid, String state) {
		return repository.findBySidAndState(sid,state);
	}
	public List<Device> findBySpidAndState(String spid, String state) {
		return repository.findBySpidAndState(spid,state);
	}
	
	public List<Device> findByCidAndAlias(String cid, String alias) {
		List<Device> deviceList = new ArrayList<Device>();
		List<Device> devices = repository.findByCidAndName(cid, alias);
		if (devices != null && devices.size() > 0) {
			for (Device device : devices) {
				String devName = device.getName().trim();
				if (devName.equalsIgnoreCase(alias)) {
					deviceList.add(device);
				}
			}
			return deviceList;
		}
		return devices;
	}
	
	public void save(List<Device> device) {
		repository.save(device);
	}

	public List<Device> findBy(String spid, String sid, String swid) {
		if (spid != null) {
			return findBySpid(spid);
		} else if (sid != null) {
			return findBySid(sid);
		} else {
			return findBySwid(swid);
		}
	}
	
	public List<Device> findByGlobal(String cid, String sid, String spid,String uid) {
		if (uid != null) {
			return findByUid(uid);
		} else if (spid != null) {
			return findBySpid(spid);
		} else if (sid !=null){
			return findBySid(sid);
		} else {
			return findByCid(cid);
		}
	}

	private List<Device> findBySwid(String swid) {
		return repository.findBySwid(swid);
	}

	public Iterable<Device> findByStatusin(List<String> status) {
		return repository.findByStatusin(status);
	}

	public List<Device> findByConfiguredDevice(List<String> status) {
		return repository.findByConfiguredDevice(status);
	}

	/**
	 * Update the device capability LAN and WAN Details
	 * @param device
	 * @param map
	 * @return
	 */
	
	public Device updateLanWanConfig(Device device, Map<String, Object> map) {
		
		HashMap<String, Object> lanDetails = new HashMap<String, Object>();
		HashMap<String, Object> wanDetails = new HashMap<String, Object>();
		
		if (map.containsKey("network_setting")) {
			
			HashMap<String, Object> network_setting = (HashMap<String, Object>) map.get("network_setting");
			
			if (network_setting != null) {
				
				if (network_setting.containsKey("lan")) {
					List<HashMap<String, Object>> myLan = (List<HashMap<String, Object>>) network_setting.get("lan");
					if (myLan != null && myLan.size() > 0) {
						lanDetails = myLan.get(0);
					}
				}

				if (network_setting.containsKey("wan")) {
					List<HashMap<String, Object>> myWan = (List<HashMap<String, Object>>) network_setting.get("wan");
					if (myWan != null && myWan.size() > 0) {
						wanDetails = myWan.get(0);
					}
				}
				
				String lan = "";
				String wan   = "";
				
				String lan_ipv4_addr =  "";
				String lan_ipv4_mask = "";
				String lan_dhcp_svr = "";
				String lan_ipv4_gateway = "";
				String lan_ipv4_dns1 = "";
				String lan_ipv4_dns = "";
				
				String wan_ipv4_addr = "";
				String wan_ipv4_mask = "";
				String wan_ipv4_gateway = "";
				String wan_ipv4_dns = "";
				String wan_ipv4_dns1 = "";

				if (lanDetails != null) {
					lan = (String) lanDetails.get("proto");
					lan_ipv4_addr = (String) lanDetails.get("ipv4_addr");
					lan_ipv4_mask = (String) lanDetails.get("ipv4_mask");
					lan_dhcp_svr = (String) lanDetails.get("dhcp_svr");
					lan_ipv4_gateway = (String) lanDetails.get("ipv4_gateway");
					lan_ipv4_dns1 = (String) lanDetails.get("ipv4_dns1");
					lan_ipv4_dns = (String) lanDetails.get("ipv4_dns");
				}
				if (wanDetails != null) {
					wan = (String) wanDetails.get("proto");
					wan_ipv4_addr = (String) wanDetails.get("ipv4_addr");
					wan_ipv4_mask = (String) wanDetails.get("ipv4_mask");
					wan_ipv4_gateway = (String) wanDetails.get("ipv4_gateway");
					wan_ipv4_dns = (String) wanDetails.get("ipv4_dns");
					wan_ipv4_dns1 = (String) wanDetails.get("ipv4_dns1");

				}

				if (lan.equals("static") && wan.equals("static")) { // case : 1

					device.setLan_Flag("lan_static");
					device.setLan_ipv4_Addr(lan_ipv4_addr);
					device.setLan_ipv4_Mask(lan_ipv4_mask);
					device.setDhcp_svr_state(lan_dhcp_svr);

					device.setWan_Flag("wan_static");
					device.setWan_ipv4_Addr(wan_ipv4_addr);
					device.setWan_ipv4_mask(wan_ipv4_mask);
					device.setWan_ipv4_gateway(wan_ipv4_gateway);
					device.setWan_ipv4_dns(wan_ipv4_dns);
					device.setWan_ipv4_dns1(wan_ipv4_dns1);

				} else if (lan.equals("static") && wan.equals("dhcp")) { // case : 2

					device.setLan_Flag("lan_static");
					device.setLan_ipv4_Addr(lan_ipv4_addr);
					device.setLan_ipv4_Mask(lan_ipv4_mask);
					device.setDhcp_svr_state(lan_dhcp_svr);

					device.setWan_Flag("wan_dhcp");
					
					// new attributes
					
					device.setWan_ipv4_Addr(wan_ipv4_addr);
					device.setWan_ipv4_mask(wan_ipv4_mask);
					device.setWan_ipv4_gateway(wan_ipv4_gateway);
					
					device.setWan_ipv4_dns(wan_ipv4_dns);
					device.setWan_ipv4_dns1(wan_ipv4_dns1);

				} else if (lan.equals("static") && wan.equals("lan_only")) { // case : 3

					device.setLan_Flag("lan_static");
					device.setLan_ipv4_Addr(lan_ipv4_addr);
					device.setLan_ipv4_Mask(lan_ipv4_mask);
					device.setLan_ipv4_gateway(lan_ipv4_gateway);
					device.setLan_ipv4_dns(lan_ipv4_dns);
					device.setLan_ipv4_dns1(lan_ipv4_dns1);
					device.setDhcp_svr_state("no");

					device.setLan_Only("lan_only");

				} else if (lan.equals("lan_dhcp") && wan.equals("lan_only")) { // case : 4

					device.setLan_Flag("lan_dhcp");
					device.setLan_ipv4_dns(lan_ipv4_dns);
					device.setLan_ipv4_dns1(lan_ipv4_dns1);
					device.setDhcp_svr_state(lan_dhcp_svr);

					device.setLan_Only("lan_only");

				} else if (lan.equals("lan_dhcp") && wan.equals("static")) { // case :5

					device.setLan_Flag("lan_dhcp");
					device.setLan_ipv4_dns(lan_ipv4_dns);
					device.setLan_ipv4_dns1(lan_ipv4_dns1);
					device.setDhcp_svr_state("no");

					device.setWan_Flag("wan_static");
					device.setWan_ipv4_Addr(wan_ipv4_addr);
					device.setWan_ipv4_mask(wan_ipv4_mask);
					device.setWan_ipv4_gateway(wan_ipv4_gateway);
					device.setWan_ipv4_dns(wan_ipv4_dns);
					device.setWan_ipv4_dns1(wan_ipv4_dns1);

				} else if (lan.equals("lan_dhcp") && wan.equals("dhcp")) { // case :6

					device.setLan_Flag("lan_dhcp");
					device.setLan_ipv4_dns(lan_ipv4_dns);
					device.setLan_ipv4_dns1(lan_ipv4_dns1);
					device.setDhcp_svr_state(lan_dhcp_svr);

					device.setWan_Flag("wan_dhcp");
					device.setWan_ipv4_dns(wan_ipv4_dns);
					device.setWan_ipv4_dns1(wan_ipv4_dns1);
				}
			}
		}

		return device;
	}

}
