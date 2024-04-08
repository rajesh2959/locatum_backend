package com.semaifour.facesix.rest;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.semaifour.facesix.data.mongo.device.ClientDevice;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RequestMapping("/rest/acl")
@RestController
public class ACLRestController extends WebController {

	static Logger LOG = LoggerFactory.getLogger(ACLRestController.class.getName());
	
	
	@Autowired
	private ClientDeviceService clientDeviceService;

	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private DeviceEventPublisher mqttPublisher;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private PortionService portionService;
	
	private String  mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"ssid\":\"{2}\", \"peer_mac\":\"{3}\" ";

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject locationlist(@RequestParam(value = "cid", required = false) String cid,
								   @RequestParam(value = "pid", required = false) String policy,
								   HttpServletRequest request) {

		JSONObject json = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonList = new JSONObject();

		 if (policy.equals("Venue")) {
			List<Site> site = siteService.findByCustomerId(cid);
			if (site != null) {
				for (Site s : site) {
					json = new JSONObject();
					json.put("id", s.getId());
					json.put("name", s.getUid());
					jsonArray.add(json);
				}
			}
		} else if (policy.equals("Floor")) {
			List<Portion> portion = portionService.findByCid(cid);
			if (portion != null) {
				for (Portion p : portion) {
					json = new JSONObject();
					json.put("id",   p.getId());
					json.put("name", p.getUid());
					jsonArray.add(json);
				}
			}
		} else {
			List<Device> ndList = deviceService.findByCid(cid);
			if (ndList != null) {
				for (Device d : ndList) {
					json = new JSONObject();
					json.put("id", d.getUid());
					json.put("name", d.getName());
					jsonArray.add(json);
				}
			}
		}

		jsonList.put("location", jsonArray);
		return jsonList;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/device_vap_ssid")
	public String  device_config(@RequestParam("uid") String uid, HttpServletRequest request) {

			try {

				Device device = deviceService.findOneByUid(uid);
				
				if (device != null ) {
					
					String conf 	= device.getConf();
					JSONObject temp = JSONObject.fromObject(conf);
					
					String ssid 		= "";
					
				if (temp.containsKey("interfaces2g")) {
					Iterator<JSONObject> it = temp.getJSONArray("interfaces2g").iterator();
					while (it.hasNext()) {
						JSONObject json = it.next();
						if (json.containsKey("ssid")) {
							ssid = (String) json.get("ssid");
							break;
						}
					}
				} else if (temp.containsKey("interfaces5g")) {
					Iterator<JSONObject> it = temp.getJSONArray("interfaces5g").iterator();
					while (it.hasNext()) {
						JSONObject json = it.next();
						if (json.containsKey("ssid")) {
							ssid = (String) json.get("ssid");
							break;
						}
					}
				}
				return ssid; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "/save")
	public String change_ssid(@RequestBody String str, HttpServletRequest request) {

			String result = "error";

			try {
				
				JSONObject object = JSONObject.fromObject(str);
				
				String client_mac 	= (String)object.get("client_mac");
				String policy 		= (String)object.get("pid");
				String ssid 		= (String)object.get("ssid");
				String rule 		    = (String)object.get("location");
				String universalId 	= null;
				
				String cid 		= (String)object.get("cid");
				String sid 		= (String)object.get("sid");
				String spid 	= (String)object.get("spid");
				
				String clientMac = client_mac.trim().toLowerCase();
				String peer_mac  = null;
				
				if (clientMac != null) {
					peer_mac = clientMac.replaceAll("[^a-zA-Z0-9]", "");
				}
				
				LOG.info("ACL cid " +cid);
				
				String status = "blocked";
				
				ClientDevice qubeClient = clientDeviceService.findByPeermacAndStatus(peer_mac, status);
				
				if (qubeClient == null) {
					
					qubeClient = new ClientDevice();
					
					qubeClient.setMac(clientMac);
					qubeClient.setPeermac(peer_mac);
					
					if (policy.equals("Customer")) {
						universalId = cid;
					} else if (policy.equals("Venue")) {
						universalId = sid ;
					} else if (policy.equals("Floor")) {
						universalId = spid;
					} else {
						universalId = rule;
					}

					if (policy.equals("uid")) {
						qubeClient.setUid(rule);
						String uuid = rule.replaceAll("[^a-zA-Z0-9]", "");
						qubeClient.setUuid(uuid);
					} else {
						qubeClient.setUid(null);
					}
					
					if (sid != null && !sid.isEmpty())
						qubeClient.setSid(sid);
					if (spid != null && !spid.isEmpty())
						qubeClient.setSpid(spid);
					
					qubeClient.setSsid(ssid);
					qubeClient.setPid(policy);
					qubeClient.setCid(cid);
					qubeClient.setStatus("blocked");

					qubeClient.setCreatedOn(new Date());
					qubeClient.setCreatedBy(SessionUtil.currentUser(request.getSession()));
					qubeClient.setModifiedOn(new Date());
					qubeClient.setModifiedBy(qubeClient.getCreatedBy());
					qubeClient = clientDeviceService.save(qubeClient);
					
					String	MQTTMessage = MessageFormat.format(mqttMsgTemplate,	new Object[] { "BLOCK", universalId, ssid, clientMac });
					mqttPublisher.publish("{" + MQTTMessage + "}", universalId);

				result = "success";

			} else {
				result = "duplicate";
				return result;
			}

		} catch (Exception e) {
			result = "error";
			e.printStackTrace();
		}
		return result;
	}
	
	
	@RequestMapping(value = "rpcacl", method = RequestMethod.POST)
	public String rpcACL  (@RequestParam(value = "uid",  required = true)  String uid,
						   @RequestParam(value = "ap",   required = true)  String ap,
						   @RequestParam(value = "mac",  required = false) String mac,
						   @RequestParam(value = "cmd",  required = true) String cmd,
						   @RequestParam(value = "sid",  required = false) String sid,
						   @RequestParam(value = "spid", required = false)  String spid,
						   @RequestParam(value = "cid",  required = false) String cid ) {
		
		String ret = "SUCCESS:RPCACL Message Sent";
		
		try {
			
			LOG.info("RPCACL::UID	" + uid);
			LOG.info("RPCACL::MAC	" + mac);
			LOG.info("RPCACL::SID	" + sid);
			LOG.info("RPCACL::SPID" + spid);
			LOG.info("RPCACL::CID	" + cid);
			LOG.info("RPCACL::CMD	" + cmd);
			
			String ACLMQTTMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"ssid\":\"{2}\", \"peer_mac\":\"{3}\" ";
			
			String peer_mac 	= "";
			ClientDevice cdev   = null;
			String universalId  = null;
			
			String status = "blocked";
			
			if (mac != null) {
				peer_mac = mac.replaceAll("[^a-zA-Z0-9]", "");
				cdev = clientDeviceService.findByPeermacAndStatus(peer_mac, status);
			}
			
			LOG.info(" RPCACL::peer_mac	" + peer_mac);
			
			if (cdev != null && cmd.equals("ACL")) { //UID BASED MQTT MESSAGE
				
				String policy = cdev.getPid();

				if (policy != null) {
					if (policy.equals("Customer")) {
						universalId = cdev.getCid();
					} else if (policy.equals("Venue")) {
						universalId = cdev.getSid();
					} else if (policy.equals("Floor")) {
						universalId = cdev.getSpid();
					} else {
						universalId = cdev.getUid().toLowerCase();
					}
				}
	
				String message = MessageFormat.format(ACLMQTTMsgTemplate,new Object[] { "UNBLOCK", universalId, cdev.getSsid(), cdev.getMac()});
				mqttPublisher.publish("{" + message + "}", universalId);
			
				if (cmd.equals("ACL")) { // REMOVE THE UNBLOCK LIST
					clientDeviceService.delete(cdev.getId());
				}
				
				return "OK";

			}
			List<ClientDevice> clientDevList = new ArrayList<ClientDevice>();
			
			Iterable<ClientDevice> devices = clientDeviceService.findBySidAndStatus(sid, "blocked");
			
			if (devices != null) { // RESET
				for (ClientDevice device : devices) {
					if (cmd.equals("RACL") || cmd.equals("QACL")) {
						
						String policy = device.getPid();
						
						if (policy != null) {
							if (policy.equals("Customer")) {
								universalId = device.getCid();
							} else if (policy.equals("Venue")) {
								universalId = device.getSid();
							} else if (policy.equals("Floor")) {
								universalId = device.getSpid();
							} else {
								universalId = device.getUid().toLowerCase();
							}
						}
		
						String message = MessageFormat.format(ACLMQTTMsgTemplate, new Object[] { "UNBLOCK", universalId,
								device.getSsid(),  device.getMac() });
						mqttPublisher.publish("{" + message + "}", universalId);
						
						clientDevList.add(device);
					}

				}		
				if (cmd.equals("RACL") && clientDevList.size() >0) {
					clientDeviceService.delete(clientDevList);
				}
				LOG.info(" UID:" + uid + "|cmd:" + cmd);
			}
		} catch (Exception e) {
			LOG.info("While ACLrpc SEnd Error " ,e);
		}
		return ret;
	}

}
