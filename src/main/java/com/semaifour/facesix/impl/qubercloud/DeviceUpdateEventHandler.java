package com.semaifour.facesix.impl.qubercloud;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.qubercast.QuberCast;
import com.semaifour.facesix.data.qubercast.QuberCastService;
import com.semaifour.facesix.device.data.DeviceItem;
import com.semaifour.facesix.device.data.DeviceItemService;
import com.semaifour.facesix.mesh.service.MeshMonitorService;
import com.semaifour.facesix.mqtt.DefaultMqttMessageReceiver;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.mqtt.Payload;
import com.semaifour.facesix.rest.DeviceRestController;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.rest.NetworkDeviceRestController;
import com.semaifour.facesix.rest.NmeshRetailerRestController;
import com.semaifour.facesix.service.NetworkConfService;
import com.semaifour.facesix.spring.CCC;
import com.semaifour.facesix.spring.SpringComponentUtils;
import com.semaifour.facesix.stats.ClientCache;
import com.semaifour.facesix.util.CustomerUtils;
import net.sf.json.JSONObject;

public class DeviceUpdateEventHandler extends DefaultMqttMessageReceiver {
	
	private static Logger LOG = LoggerFactory.getLogger(DeviceUpdateEventHandler.class.getName());
	
	DeviceService 			_deviceService;
	
	ClientDeviceService 	_clientDeviceService;
	
	@Autowired
	QuberCastService _qubercastService;
	
	@Autowired
	NetworkConfRestController netRestController;
	
	@Autowired
	DeviceRestController _deviceRestController;
	
	@Autowired
	NetworkDeviceRestController networkDeviceRestController;
	
	@Autowired
	NmeshRetailerRestController nMeshRetailerRestController;
	
	@Autowired private NetworkConfService networkConfService;
	
	@Autowired
	private DeviceEventPublisher _mqttPublisher;
	
	private DeviceItemService _deviceItemService;
	
	@Autowired private ClientCache cache;
	
	@Autowired private ElasticService elasticService;
	
	@Autowired private CCC _CCC;
	
	@Autowired private MeshMonitorService meshMonitorService;
	
	@Autowired private CustomerUtils customerUtils;
	
	
	public DeviceUpdateEventHandler() {
	}
	
	@Override
	public boolean messageArrived(String topic, MqttMessage message) {
		return messageArrived(topic, message.toString());
	}
	
	@Override
	public boolean messageArrived(String topic, String message) {		
		if (LOG.isDebugEnabled()) LOG.debug("handling msqtt message at " + topic + " : " + message);
		
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(message.toString(), new TypeReference<HashMap<String, Object>>(){});

			if (map == null || map.isEmpty()) {
				LOG.info("MQTT map  = " + map);
				return false;
			}

			if (!map.containsKey("opcode")) {
				return false;
			}

			String opcode = (String) map.get("opcode").toString().toLowerCase();

			//LOG.info(" MQTT OPCODE " + opcode);
			//LOG.info(" MQTT MAP " + map);

			switch (opcode) {

			case "device_update":
				update(map);
				break;
			case "device_create":
				map.put("status", Device.STATUS.AUTOCONFIGURED.name());
				create(map, true);
				break;
			case "device_register":
				map.put("status", Device.STATUS.REGISTERED.name());
				create(map, false);
				break;
			case "device_add_item":
				String type = (String) map.get("typefs");
				createDeviceItem(map, type != null ? type : "ITEMLIST");
				break;
			case "device_remove_item":
				final String fstype = (String) map.get("typefs");
				removeDeviceItem(map, fstype != null ? fstype : "ITEMLIST");
				break;
			case "device_unallow_item":
				createDeviceItem(map, "ALLOWLIST");
				break;
			case "device_block_item":
				createDeviceItem(map, "BLOCKLIST");
				break;
			case "device_unblock_item":
				createDeviceItem(map, "BLOCKLIST");
				break;
			case "device_heartbeat":
				getDeviceService().updateDeviceHealth(map);
				break;
			case "ping_request":
				ping(map);
				break;
			case "peer_update":
				peer_update(map);
				break;
			case "qcast_get_cfg":
				qcast_update(map);
				break;
			case "upgrade":
				binarySetting_upgrade(map);
				break;
			case "network_balancer_metrics_req" :
				getNetworkDeviceRestController().rrm(map);
				break;
			case "network_balancer_steer_req":
				getNetworkDeviceRestController().bss(map);
				break;
			case "peer_assoc":
				cache().peer_assoc(map);
				break;
			case "peer_disassoc": {
				cache().peer_disassoc(map);
			}
			case "dcs_chan_switch":
				getNetworkDeviceRestController().dcs_chan_switch(map);
				break;
			case "mesh_stats":
				getnMeshRetailerRestController().mesh_stats(map);
				break;
			case "dmesh_system_stats": {
				getMeshMonitorService().systemStats(map);
				break;
			}
			case "video_stats": {
				getMeshMonitorService().videoStats(map);
				break;
			}
			case "mesh_path_add" : {
				getMeshMonitorService().pathSelection(map);
				break;
			}
			case "mesh_path_del" : {
				getMeshMonitorService().pathSelection(map);
				break;
			}
			case "mesh_path_update" : {
				getMeshMonitorService().pathSelection(map);
				break;
			}
			default:
				LOG.warn("Unsupported opcode :" + opcode);
				break;
			}
			
		} catch (Exception e) {
			LOG.error("Failed to process messageArrvied at " + topic + " : " + message, e);
			return false;
		}
		return true;
		
	}

	private boolean binarySetting_upgrade(Map<String, Object> map) throws Exception {
		try {
			
			String uid   =(String) map.get("uid");
			String reson =(String) map.get("REASON");
			
			if (uid != null) {
				Device device = getDeviceService().findOneByUid(uid);
				if (device != null) {
					String cid = device.getCid();
					if (cid != null) {
						String fileName = CustomerUtils.binaryUpgradeCache.get(cid);
						if (fileName != null) {
							String type = "binary";
							getCustomerUtils().removeFile(type,fileName);
						}
					}
					
					device.setModifiedOn(new Date(System.currentTimeMillis()));
					device.setBinaryReason(reson);
					getDeviceService().save(device, false);
				}
			}
			return true;
		} catch (Exception e) {
			LOG.warn("Error "+e.getStackTrace());
		}
		return false;
	}

	private boolean createDeviceItem(Map<String, Object> map, String type) {
		try {
			String uid = (String)map.get("uid");
			String mac = (String)map.get("mac");
			String by = (String)map.get("by");
			DeviceItem item = new DeviceItem(uid, mac, type);
			item.setModifiedBy(by);
			item.setCreatedBy(by);
			Properties p = new Properties();
			p.putAll(map);
			item.setSettings(p);
			deviceItemService().save(item, Boolean.valueOf((String)map.get("notify")));
			//LOG.info("created DeviceItem[ {}, {}, {} ]", uid, mac, type);
			return true;
		} catch (Exception e) {
			LOG.warn("exception createDeviceItem() [{}]", map.toString(), e);
			return false;
		}
	}

	private boolean removeDeviceItem(Map<String, Object> map, String type) {
		try {
			String uid = (String)map.get("uid");
			String mac = (String)map.get("mac");
			deviceItemService().delete(mac);
			//LOG.info("removed DeviceItem[ {}, {}, {} ]", uid, mac, type);
			return true;
		} catch (Exception e) {
			LOG.warn("exception createDeviceItem() [{}]", map.toString(), e);
			return false;
		}
	}

	/**
	 * 
	 * Send ping response
	 * 
	 * @param map
	 * @return
	 */
	private boolean ping(Map<String, Object> map) {
		String uid = (String)map.get("uid");
		try {
			String msg = (String)map.get("message");
			Payload payload = new Payload("ping_response", uid, uid, msg);
			getDeviceEventPublisher().publish(payload.toJSONString(), uid);
		} catch (Exception e) {
			LOG.warn("ping_response for ping_reqquest from [{}] failed", uid, e);
		}
		return true;
	}

	public boolean create(Map<String, Object> map, boolean notify) throws Exception {
		
		String uid 		= (String)map.get("uid");
		String name 	= (String)map.get("name");
		Object template = map.get("template");
		String by 		= (String)map.get("by");
		
		LOG.info("creating/registering device :" + uid);

		if (name == null) {
			name = uid;
		}
			
		Device device = getDeviceService().findOneByUid(uid);
		Date dt = new Date();
	
		if (device == null) {
			device = new Device();
			device.setCreatedBy(by);
			device.setCreatedOn(dt);
			device.setUid(uid);
			device.setName(name);
			device.setIp("0.0.0.0");
		}
		
		device = getDeviceService().updateLanWanConfig(device,map);
		
		device.setModifiedBy(by);
		device.setModifiedOn(dt);
		device.setStatus((String)map.get("status"));
		device.setState("inactive");
		
		device.setDescription(String.valueOf(template));
		device.setTemplate(String.valueOf(template));
		device.setConf(String.valueOf(template));
		
		getDeviceService().saveAndSendMqtt(device, notify);
		
		return true;
	}
	
	public boolean update(Map<String, Object> map) throws Exception {
		
		LOG.info("device_update map>>>>: " + map);
		
		String uid 		= (String)map.get("uid");
		String en 		= (String)map.get("_entity");
		Object vapcnt 	=  map.get("_vapcnt");
		String ip 		= (String)map.get("ip");
		String role		= (String)map.get("device_role");
		
		String buildVersion	= (String)map.get("version");
		String buildTime	= (String)map.get("buildtime");
				
		if (map.containsKey("vap_list")) {
			getMeshMonitorService().updateDeviceVapIds(map);
		}
				
		Device dv = getDeviceService().findOneByUid(uid);
		
		if (dv != null) {
			if (en != null) {
				if (en.equals("radio2g")) {
					if (StringUtils.isNotBlank(String.valueOf(vapcnt))) {
						int vapCount2G = Integer.parseInt(String.valueOf(vapcnt));
						dv.setVap2gcount(vapCount2G);
					}
				} else if (en.equals("radio5g")) {
					if (StringUtils.isNotBlank(String.valueOf(vapcnt))) {
						int vapCount5G = Integer.parseInt(String.valueOf(vapcnt));
						  dv.setVap5gcount(vapCount5G);
					}
				}
			}
			
			if (ip != null && !ip.isEmpty()) {
				dv.setIp(ip);
			}
			
			if (role != null) {
				dv.setRole(role);
			} else {
				dv.setRole("ap");
			}
			
			if (buildVersion != null && !buildVersion.isEmpty()) {
				dv.setBuildVersion(buildVersion);
			}
			if (buildTime != null && !buildTime.isEmpty()) {
				dv.setBuildTime(buildTime);
			}

			dv.setState("active");
			dv.setModifiedBy((String)map.get("by"));
			dv.setModifiedOn(new Date(System.currentTimeMillis()));
	
			getDeviceService().save(dv, false);
		}

		return true;
	}
	
	public boolean peer_update(Map<String, Object> map) throws Exception {
		
		String uid  	= (String)map.get("uid");
		int peer_count  = (int)map.get("peer_count");
		
		String state	= "active";
		
		Device dv = getDeviceService().findOneByUid(uid);
		 		
		if (dv != null) {

			LOG.info ("PEER UID" + uid);
			LOG.info ("peer_count" + peer_count);
			
			try {

				dv.setState(state);
				dv.setModifiedBy((String)map.get("by"));
				dv.setModifiedOn(new Date(System.currentTimeMillis()));
				getDeviceService().save(dv, false);
				
			}catch (NumberFormatException nfe) {
				LOG.info("Error in Format" + System.currentTimeMillis());
			}
						
			return true;
		}
						
		return false;
	}	
	
	@SuppressWarnings("unchecked")
	public boolean qcast_update(Map<String, Object> map) throws Exception {
		String qcastmqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"by\":\"{2}\", \"newversion\":\"{3}\", \"value\":{4} ";
		
		JSONObject jsonObject = new JSONObject();
		String uid  		  = (String)map.get("uid");
		QuberCast quber 	  = getQuberCastService().findByReffId("a5a5");
		if (quber != null) {
			jsonObject.put("mediaPath", 		quber.getMediaPath());
			jsonObject.put("multicastPort", 	quber.getMulticastPort());
			jsonObject.put("mulicastAddress", 	quber.getMulicastAddress());
			jsonObject.put("totalFiles", 		quber.getLogFile());
			jsonObject.put("payLoad", 			quber.getLogLevel());
		}
		
		Device device = getDeviceService().findOneByUid(uid);
		String header = "AP_QCAST_START";
		if (device != null) {
			String msg = MessageFormat.format(qcastmqttMsgTemplate,
					new Object[] { header, device.getUid(), "qubercloud", "0xFE", jsonObject.toString() });
			getDeviceEventPublisher().publish("{" + msg + "}", device.getUid());
		}
		
		return true;
	}
	
	private DeviceService getDeviceService() {
		
		try {
			if (_deviceService == null) {
				_deviceService = Application.context.getBean(DeviceService.class);
			}
		} catch (Exception e) {
			
		}
		return _deviceService;
	}
	
	private QuberCastService getQuberCastService() {
		try {
			if (_qubercastService == null) {
				_qubercastService = Application.context.getBean(QuberCastService.class);
			}
		} catch (Exception e) {
			
		}
		return _qubercastService;
	}	
	
	
	private DeviceEventPublisher getDeviceEventPublisher() {
		
		try {
			if (_mqttPublisher == null) {
				_mqttPublisher = Application.context.getBean(DeviceEventPublisher.class);
			}
		} catch (Exception e) {
			
		}
		return _mqttPublisher;
	}

	private DeviceItemService deviceItemService() {
		
		try {
			if (_deviceItemService == null) {
				_deviceItemService = Application.context.getBean(DeviceItemService.class);
			}			
		} catch (Exception e) {
			
		}

		return _deviceItemService;
	}

	private NetworkDeviceRestController getNetworkDeviceRestController() {

		try {
			if (networkDeviceRestController == null) {
				networkDeviceRestController = Application.context.getBean(NetworkDeviceRestController.class);
			}
		} catch (Exception e) {
			LOG.info("NetworkDeviceRestController  Bean Initialization failed...");
		}
		return networkDeviceRestController;
	}
	private NmeshRetailerRestController getnMeshRetailerRestController() {

		try {
			if (nMeshRetailerRestController == null) {
				nMeshRetailerRestController = Application.context.getBean(NmeshRetailerRestController.class);
			}
		} catch (Exception e) {
			LOG.info("NmeshRetailerRestController  Bean Initialization failed...");
		}
		return nMeshRetailerRestController;
	}
	
	private ClientCache cache() {
		
		try {
			if (cache == null) {
				cache = Application.context.getBean(ClientCache.class);
			}
		} catch (Exception e) {
			cache = Application.context.getBean(ClientCache.class);
		}

		return cache;
	}
	
	
	public CCC getCCC() {
		if (_CCC == null) {
			_CCC = Application.context.getBean(CCC.class);
		}
		return _CCC;
	}
	
	public ElasticService getElasticService() {
		if (elasticService == null) {
			elasticService = Application.context.getBean(ElasticService.class);
		}
		return elasticService;
	}
	public MeshMonitorService getMeshMonitorService() {
		if (meshMonitorService == null) {
			meshMonitorService = Application.context.getBean(MeshMonitorService.class);
		}
		return meshMonitorService;
	}
	
	public CustomerUtils getCustomerUtils() {
		if (customerUtils == null) {
			customerUtils = Application.context.getBean(CustomerUtils.class);
		}
		return customerUtils;
	}
	
	public NetworkConfService getNetworkConfService() {
		if (networkConfService == null) {
			networkConfService = Application.context.getBean(NetworkConfService.class);
		}
		return networkConfService;
	}
	
	
	@Override
	public String getName() {
		return "DefaultMqttMessageReceiver";
	}

}
