package com.semaifour.facesix.service;

import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.util.CustomerUtils;

@Service
public class NetworkConfService {

	static Logger LOG = LoggerFactory.getLogger(NetworkConfService.class.getName());

	@Autowired
	DeviceService devService;

	@Autowired
	NetworkConfRestController networkConfRestController;
	
	@Autowired
	CustomerUtils customerUtils;

	public HashMap<String, Object> apConfig(JSONObject payload,String cur_user,HttpServletRequest request,HttpServletResponse response) throws Exception {

		boolean isSave = false;
		
		 String cid    	= (String)payload.get("cid");
		 String uid 	= (String)payload.get("uid");
		 String sid     = (String)payload.get("sid");
		 String spid    = (String)payload.get("spid");
		 
		 String template = String.valueOf(payload.get("conf"));
		 
		 String param   			= (String)payload.get("param");
		 String alias    			= (String)payload.get("alias");
		 
		 String  network_balancer = "0";
		 int balancer   = 0;
		 
		 if (payload.containsKey("network_balancer")) {
			 network_balancer = payload.getOrDefault("network_balancer","0").toString();
			 balancer =  Integer.parseInt(network_balancer);
		 }
		 
		 String statusInterval      = (String)payload.get("keepAliveInterval");
		 String root    			= (String)payload.get("root");
		 String workingMode    		= (String)payload.get("workingMode");
		
		Device device = getDeviceService().findOneByUid(uid);
		
		if (param.equals("DeviceConfig") || StringUtils.isBlank(spid)) {

			boolean isnewDevice = false;

			if (device == null) {
				isnewDevice = true;
				device = new Device();
				device.setCreatedBy(cur_user);
				device.setModifiedBy(cur_user);
				device.setUid(uid);
				device.setTypefs("ap");
				device.setState("inactive");
				device.setCid(cid);
				device.setIp("0.0.0.0");
			}

			if (isnewDevice) {
				device.setStatus(Device.STATUS.AUTOCONFIGURED.name());
			} else {
				device.setStatus(Device.STATUS.CONFIGURED.name());
				device.setModifiedBy(cur_user);
				device.setModifiedOn(new Date(System.currentTimeMillis()));
			}

			String devCid = device.getCid();

			if (StringUtils.isEmpty(devCid)) {
				device.setCid(cid);
			}

			device.setName(alias);
			device.setTemplate(template);
			device.setConf(template);

			device.setNetwork_balancer(balancer);
			device.setKeepAliveInterval(statusInterval);
			device.setRoot(root);

			if (device.getTypefs() == null) {
				device.setTypefs("ap");
			}
			device.setWorkingMode(workingMode);

			/**
			 * LAN and WAN Config
			 */
			updateLanWanConfig(device,payload);

			isSave = true;
			
		} else if (StringUtils.isNotBlank(sid) && StringUtils.isNotBlank(spid)) {
			/*
			 * case : 1 If device is null (New Device),it should be saved
			 * case : 2 If device is not null but 
			 * 				i)  Status equals to REGISTERED and If Spid is blank it should be saved.
			 * 				ii) Status not equals to REGISTERED and If Spid is blank 
			 * 						but Cid is not blank it should be saved.
			 * case : 3 If device is not null update the device config
			 */
			
			

			if (device == null) {
				isSave = true;
			}

			String status = device.getStatus();
			
			if (device != null) {
				if (status.equals(Device.STATUS.REGISTERED.name())
						&& StringUtils.isBlank(device.getSpid())) {
					isSave = true;
				} else if (!status.equals(Device.STATUS.REGISTERED.name())
						&& StringUtils.isBlank(device.getSpid()) && StringUtils.isNotBlank(device.getCid())) {
					isSave = true;
				}
			}

			if (isSave) {
				if (request.getSession().getAttribute("json") != null) {
					String conf = request.getSession().getAttribute("json").toString(); 	
					JSONParser parser = new JSONParser();
					if (conf != null) {
						payload = (JSONObject) parser.parse(conf);
					}
				}
				
				JSONObject networkSetting = new JSONObject();

				if (payload.containsKey("network_settings")) {
					networkSetting = (JSONObject) payload.get("network_settings");
				}
				
				payload.put("type", "ap");
				payload.put("apFlag", "1");
				payload.put("alias", alias);
				payload.put("json", template);
				payload.put("network_settings", networkSetting);
				
				networkConfRestController.save(payload.toString(), request, response);

			} else if (device != null && StringUtils.isNotBlank(device.getSpid())) {
				
				isSave = true;
				
				device.setStatus(Device.STATUS.CONFIGURED.name());
				device.setName(alias);
				device.setKeepAliveInterval(statusInterval);
				device.setTemplate(template);
				device.setConf(template);
				device.setModifiedBy(cur_user);
				device.setModifiedOn(new Date(System.currentTimeMillis()));
				device.setNetwork_balancer(balancer);
				device.setRoot(root);
				device.setWorkingMode(workingMode);
				
				/**
				 * LAN and WAN config
				 */
				
				this.updateLanWanConfig(device, payload);
				
				
			}
		}

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("device", device);
		result.put("status", isSave);
		
		return result;

	}

	public Device updateLanWanConfig(Device device,JSONObject data) {
		
		
		if (data.containsKey("network_settings")) {

			JSONObject payload = (JSONObject) data.get("network_settings");
			
			if (payload != null && !payload.isEmpty()) {
				
				String lan_flag = (String)payload.get("lan");
				String lan_only = (String)payload.get("lan_only_checkbox");
				String wan_flag = (String)payload.get("wan");

				String lan_ipv4_addr = (String)payload.get("lan_ipv4_addr");
				String lan_ipv4_mask = (String)payload.get("lan_ipv4_mask");
				String lan_ipv4_dns = (String)payload.get("lan_ipv4_dns");
				String lan_ipv4_dns1 = (String)payload.get("lan_ipv4_dns1");
				String lan_ipv4_gateway = (String)payload.get("lan_ipv4_gateway");
				String lan_ipv4_dhcp_dns = (String)payload.get("lan_ipv4_dhcp_dns");
				String lan_ipv4_dhcp_dns1 = (String)payload.get("lan_ipv4_dhcp_dns1");

				String wan_ipv4_addr = (String)payload.get("wan_ipv4_addr");
				String wan_ipv4_mask = (String)payload.get("wan_ipv4_mask");
				String wan_ipv4_dns = (String)payload.get("wan_ipv4_dns");
				String wan_ipv4_dns1 = (String)payload.get("wan_ipv4_dns1");
				String wan_ipv4_gateway = (String)payload.get("wan_ipv4_gateway");
				String wan_ipv4_dhcp_dns = (String)payload.get("wan_ipv4_dhcp_dns");
				String wan_ipv4_dhcp_dns1 = (String)payload.get("wan_ipv4_dhcp_dns1");

				LOG.info(" LAN FLAG " + lan_flag + " lan_only " + lan_only + " wan_flag " + wan_flag);
				
				
				/*
				 * LAN Static
				 */
				device.setLan_Flag(lan_flag);
				device.setLan_Only(lan_only);
				device.setLan_ipv4_Addr(lan_ipv4_addr);
				device.setLan_ipv4_Mask(lan_ipv4_mask);
				device.setLan_ipv4_dns(lan_ipv4_dns);
				device.setLan_ipv4_dns1(lan_ipv4_dns1);
				device.setLan_ipv4_gateway(lan_ipv4_gateway);

				/*
				 * LAN DHCP
				 */
				device.setLan_ipv4_dhcp_dns(lan_ipv4_dhcp_dns);
				device.setLan_ipv4_dhcp_dns1(lan_ipv4_dhcp_dns1);

				/*
				 * WAN Static
				 * 
				 */
				device.setWan_Flag(wan_flag);
				device.setWan_ipv4_Addr(wan_ipv4_addr);
				device.setWan_ipv4_mask(wan_ipv4_mask);
				device.setWan_ipv4_dns(wan_ipv4_dns);
				device.setWan_ipv4_dns1(wan_ipv4_dns1);
				device.setWan_ipv4_gateway(wan_ipv4_gateway);

				/*
				 * WAN DHCP
				 * 
				 */

				device.setWan_ipv4_dhcp_dns(wan_ipv4_dhcp_dns);
				device.setWan_ipv4_dhcp_dns1(wan_ipv4_dhcp_dns1);
			}
		}

		if (data.containsKey("lan_bridge")) {
			String lan_bridge = (String) data.get("lan_bridge");
			device.setLanbridge(lan_bridge);
		}
		if (data.containsKey("lan_bridge")) {
			String wan_bridge = (String) data.get("wan_bridge");
			device.setWanbridge(wan_bridge);
		}

		return device;

	}
	
	private DeviceService getDeviceService() {
		if (devService == null) {
			devService = Application.context.getBean(DeviceService.class);
		}
		return devService;
	}

	public JSONObject validateInputValues(JSONObject payload) {

		 JSONObject response = new  JSONObject();
		
		 	String body 	= "success";
			boolean success = true;
			int code 		= 200;
			
		try {
			
			 String uid 	= (String)payload.get("uid");
			 String param   = (String)payload.get("param");
			 String cid 	= (String)payload.get("cid");
			 String sid     = (String)payload.get("sid");
			 String spid    = (String)payload.get("spid");
			 String alias   = (String)payload.get("alias");
			 String template = String.valueOf(payload.get("conf"));
				
			 LOG.info(" conf " +template);
		 
			 if (StringUtils.isEmpty(template)) {
				 body = "Required String parameter 'conf' is not present";
				 code = 400;
				success = false;
			 } else if (StringUtils.isEmpty(param)) {
				body = "Required String parameter 'param' is not present";
				code = 400;
				success = false;
			} else if (StringUtils.isEmpty(uid)) {
				 body = "Required String parameter 'uid' is not present";
				 code = 400;
				 success = false;
			 } else	 if (StringUtils.isEmpty(cid)) {
				 body = "Required String parameter 'cid' is not present";
				 code = 400;
				 success = false;
			 } else	if (param.equals("FloorConfig")) {
				if (StringUtils.isEmpty(sid)) {
					body = "Required String parameter sid is not present";
					code = 400;
					success = false;
				} else if (StringUtils.isEmpty(spid)) {
					body = "Required String parameter spid is not present";
					code = 400;
					success = false;
				}
			} else	 if (StringUtils.isEmpty(alias)) {
				 body = "Required String parameter alias is not present";
				 code = 400;
				 success = false;
			 }
			 
		} catch (Exception e) {
			 body = e.getMessage();
			 code = 500;
			 success = false;
			 e.printStackTrace();
		}
		
		response.put("body", body);
		response.put("code", code);
		response.put("status", success);
		
		return response;
	}



}
