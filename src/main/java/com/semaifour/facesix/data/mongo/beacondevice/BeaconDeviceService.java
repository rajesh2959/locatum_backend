
package com.semaifour.facesix.data.mongo.beacondevice;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.mongo.device.HeartBeat;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.util.SimpleCache;

@Service
public class BeaconDeviceService {
	
	static Logger LOG = LoggerFactory.getLogger(BeaconDeviceService.class.getName());

	@Autowired
	private BeaconDeviceRepository repository;
	
	@Autowired
	private DeviceEventPublisher mqttPublisher;
	
	@Autowired
	NetworkConfRestController netRestController;
	
	@Autowired
	BeaconDeviceService beaconDeviceService;
	
	String mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\",\"type\":\"{2}\",\"cid\":\"{3}\","
							+ " \"sid\":\"{4}\",\"spid\":\"{5}\",\"ip\":\"{6}\", \"serverip\":\"{7}\", "
							+ " \"solution\":\"{8}\",\"tagthreshold\":\"{9}\", \"conf\":{10}, \"tunnelip\":\"{11}\",\"source\":\"{12}\"";

	@Autowired
	SimpleCache<HeartBeat> beaconDeviceHealthCache;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	PortionService portionService;
	
	public BeaconDeviceService() {
	}
	
	public Page<BeaconDevice> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}
	
	public List<BeaconDevice> findByName(String name) {
		return repository.findByName(name);
	}
	
	public List<BeaconDevice> findByUid(String uid) {
		return repository.findByUid(uid);
	}
	
	public BeaconDevice findOneByName(String name) {
		List<BeaconDevice> list = findByName(name);
		if (list != null & list.size() > 0) return list.get(0);
		return null;
	}
	
	public BeaconDevice findOneByUid(String uid) {
		List<BeaconDevice> list = findByUid(uid);
		if (list != null & list.size() > 0 ) {
			BeaconDevice bdev = list.get(0);
			if (uid.equalsIgnoreCase(bdev.getUid())){
				return bdev;
			}
			
		}
		return null;
	}
	
	public BeaconDevice findByUidAndCid(String uid,String cid) {
		List<BeaconDevice> devicelist  = repository.findByUidAndCid(uid,cid);
		if (devicelist != null && devicelist.size() > 0) {
			BeaconDevice device = devicelist.get(0);
			if (device.getUid().equalsIgnoreCase(uid)) {
				return device;
			}
		}
		return null;
	}
	
	public BeaconDevice findOneByUuid(String uid) {
		return repository.findByUuid(uid);
	}
	
	public BeaconDevice findById(String id) {
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
		BeaconDevice device = repository.findOne(id);
		repository.delete(id);
		if (device != null) {
			notify_delete(device, "device-delete");
		}
	}
	
	public void delete(BeaconDevice device) {
		repository.delete(device);
		notify_delete(device, "device-delete");
	}
	
	public long count() {
		return repository.count();
	}
	
	
	public List<BeaconDevice> findBySid(String sid){
		return repository.findBySid(sid);
	}
	
	public List<BeaconDevice> findBySpid(String spid){
		return repository.findBySpid(spid);
	}
	
	public List<BeaconDevice> findByCid(String cid){
		return repository.findByCid(cid);
	}
	
	public BeaconDevice save(BeaconDevice device) {
		return save(device, true);
	}

	public BeaconDevice save(BeaconDevice device, boolean notify) {
		device = repository.save(device);
		if (device.getPkid()== null) {
			device.setPkid(device.getId());
			device = repository.save(device);
		}
		//LOG.info(" Beacon Device saved successfully :" + device.getId());
		if (notify) {
			notify(device, "device-update");
		}
		return device;
	}
	
	public BeaconDevice reset(BeaconDevice device, boolean notify) {
		if (notify) {
			notify(device, "RESET");		
		}
		//LOG.info("Beacon Device reset successfully :" + device.getId());
		return device;
	}	

	public List<BeaconDevice> findAll() {
		return repository.findAll();
	}
	
	

	public List<BeaconDevice> findByStatus(String status) {
		return repository.findByStatus(status);
	}

	public void updateDeviceHealth(Map<String, Object> map) {
		HeartBeat beat = new HeartBeat(map);
		this.beaconDeviceHealthCache.putForGood(beat.getUid(), beat);
	
		BeaconDevice beaconDevice = findOneByUid(beat.getUid());
		
		if (beaconDevice != null) {
			String status = beaconDevice.getState();
			if (status.equals("inactive")) {
				beaconDevice.setState("active");
				beaconDevice.setModifiedBy("Cloud");
				beaconDevice.setModifiedOn(new Date(System.currentTimeMillis()));
				save (beaconDevice, false);
				
			}
		}
	}
	

	/*public String updateTagType(String tag_type) {
		
		//LOG.info("********************** TagType====== > " +tag_type);
		
		String code = "\uf007"; //default tag Type
		
		try {
			
			if (tag_type != null) {
				//personType =personType.split("/+")[1];
				
				//LOG.info(" ********************** tag_type after split ====== > " +tag_type);

				if (tag_type.equals("Doctor")) {
					code = "\uf0f0";
				} else if (tag_type.equals("WheelChair")) {
					code = "\uf193";
				} else if (tag_type.equals("Asset")) {
					code = "\uf217";
				} else if (tag_type.equals("Bed")) {
					code = "\uf236";
				} else if (tag_type.equals("Ambulance")) {
					code = "\uf0f9";
				} else if (tag_type.equals("MedicalKit")) {
					code = "\uf0fa";
				} else if (tag_type.equals("Heartbeat")) {
					code = "\uf21e";
				} else if (tag_type.equals("Cycle")) {
					code = "\uf206";
				} else if (tag_type.equals("Truck")) {
					code = "\uf0d1";
				} else if (tag_type.equals("Bus")) {
					code = "\uf207";
				} else if (tag_type.equals("Car")) {
					code = "\uf1b9";
				} else if (tag_type.equals("Child")) {
					code = "\uf1ae";
				} else if (tag_type.equals("Female")) {
					code = "\uf182";
				} else if (tag_type.equals("Male")) {
					code = "\uf183";
				} else if (tag_type.equals("Fax")) {
					code = "\uf1ac";
				} else if (tag_type.equals("User")) {
					code = "\uf007";
				} else if (tag_type.equals("Library")) {
					code = "\uf02d";
				} else if (tag_type.equals("Hotel")) {
					code = "\uf0f5";
				} else if (tag_type.equals("Fireextinguisher")) {
					code = "\uf134";
				} else if (tag_type.equals("Print")) {
					code = "\uf02f";
				} else if (tag_type.equals("Clock")) {
					code = "\uf017";
				} else if (tag_type.equals("Film")) {
					code = "\uf008";
				} else if (tag_type.equals("Music")) {
					code = "\uf001";
				} else if (tag_type.equals("Levelup")) {
					code = "\uf148";
				} else if (tag_type.equals("Leveldown")) {
					code = "\uf149";
				} else if (tag_type.equals("Trash")) {
					code = "\uf014";
				} else if (tag_type.equals("Home")) {
					code = "\uf015";
				} else if (tag_type.equals("Videocamera")) {
					code = "\uf03d";
				} else if (tag_type.equals("Circle")) {
					code = "\uf05a";
				} else if (tag_type.equals("Gift")) {
					code = "\uf06b";
				} else if (tag_type.equals("Exit")) {
					code = "\uf08b";
				} else if (tag_type.equals("Key")) {
					code = "\uf084";
				} else if (tag_type.equals("Camera")) {
					code = "\uf083";
				} else if (tag_type.equals("Phone")) {
					code = "\uf083";
				} else if (tag_type.equals("Creditcard")) {
					code = "\uf09d";
				} else if (tag_type.equals("Speaker")) {
					code = "\uf0a1"; 
				} else if (tag_type.equals("Powerroom")) {
					code = "\uf1e6";
				} else if (tag_type.equals("Toolset")) {
					code = "\uf0ad";
				} else if (tag_type.equals("Batteryroom")) {
					code = "\uf241";
				} else if (tag_type.equals("Computerroom")) {
					code = "\uf241";
				} else if (tag_type.equals("Kidsroom")) {
					code = "\uf113";
				} else if (tag_type.equals("TVroom")) {
					code = "\uf26c";
				} else if (tag_type.equals("Contractor")) {
					code = "\uf007";
				} else if (tag_type.equals("Employee")) {
					code = "\uf007";
				} else if (tag_type.equals("Visitor")) {
					code = "\uf007";
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		//LOG.info(" result ********************** Tag Type " + tag_type + " Person hexadecimal code  " + code);
		return code;
	}*/
	
	
	public Collection<HeartBeat> getAllDeviceHealth() {
		return this.beaconDeviceHealthCache.values();
	}
	
	public HeartBeat getDeviceHealth(String uid) {
		return this.beaconDeviceHealthCache.get(uid);
	}
	
	public void clearDeviceHealthCache() {
		this.beaconDeviceHealthCache.clear();
	}
		
	public HeartBeat clearDeviceHealthCache(String uid) {
		return this.beaconDeviceHealthCache.clear(uid);
	}
	
	public boolean notify(BeaconDevice device, String opcode) {
		try {
			
			String cid 			= "";
			String sid 			= "";
			String spid 		= "";
			String pixelresult  = "";
			String type 		= "";
			String ip 			= "0.0.0.0";
			String tunnelIp 	= "0.0.0.0";
			String tagThreshold = "20";
			String solution 	= "";
			String serverip 	= "0.0.0.0";
			String dev_type     ="";
			String vpn 			="disable";
			
			String conf 					 =  device.getConf();
			net.sf.json.JSONObject template	 = net.sf.json.JSONObject.fromObject(conf);
			net.sf.json.JSONObject diag_json = makeDiagJson(template); //diag_details merge key and value
			
			if (device.getIp() != null) {
				ip = device.getIp();
			}
			if (device.getCid() != null) {
				cid = device.getCid();
			}
			if (device.getSid() != null) {
				sid = device.getSid();
			}
			if (device.getSpid() != null) {
				spid = device.getSpid();
			}
			if (device.getTunnelIp() != null) {
				tunnelIp = device.getTunnelIp();
			}
			
			String source = device.getSource() == null? "qubercomm":device.getSource();
			
			dev_type = device.getType();

			Customer acc = getCustomerService().findById(cid);
			if (acc != null) {
				serverip 		= acc.getBleserverip();
				type 			= acc.getVenueType();
				tagThreshold 	= acc.getThreshold();
				vpn				= acc.getVpn();
			}
			
			if(vpn != null && vpn.equals("true")){
				vpn = "enable";
			} else {
				vpn = "disable";
			}
			
			template = addVpnToConf(vpn,template);

			if (type != null) {
				
				if (type.equalsIgnoreCase("Locatum")) {
					solution = "trilateration";
				} else if( type.equalsIgnoreCase("Patient-Tracker")){
					solution = "entryexit";
				} else {
					solution = "gateway";
				}
			}

			pixelresult = device.getPixelresult();

			if (pixelresult != null) {
				template.put("deviceinfo", pixelresult);
			}
			if (diag_json != null && diag_json.size() > 0) {
				template.put("diag_details", diag_json);
			}

			String deviceType = "server";
			List<BeaconDevice> servers = null;
			if(!dev_type.equals(deviceType)){
				 servers = beaconDeviceService.findByCidAndType(cid, deviceType);
			}

			String message = MessageFormat.format(mqttMsgTemplate,new Object[]{opcode, 
																	   device.getUid().toUpperCase(),
																	   dev_type,
																	   cid,
																	   sid,
																	   spid,
																	   ip,
																	   serverip,
																	   solution,
																	   tagThreshold,
																	   template.toString(),
																	   tunnelIp,
																	   source});
			mqttPublisher.publish("{" + message + "}", device.getUid().toUpperCase());

			if (servers !=null && servers.size() > 0) {
				BeaconDevice server = servers.get(0);
				mqttPublisher.publish("{" + message + "}", server.getUid().toUpperCase());
			}
			return true;
		} catch (Exception e) {
			LOG.warn("Failed to notify update", e);
			return false;
		}
	}
	
	public net.sf.json.JSONObject addVpnToConf(String vpn, net.sf.json.JSONObject template) {
		if(template.containsKey("attributes")){
		
			net.sf.json.JSONArray array = template.getJSONArray("attributes");
			net.sf.json.JSONArray newtemplate = new net.sf.json.JSONArray();
		
			net.sf.json.JSONObject confAttrib = (net.sf.json.JSONObject) array.get(0);
			
			confAttrib.put("tunnel",vpn);
			newtemplate.add(confAttrib);
			template.put("attributes",newtemplate);
		}
		return template;
	}

	public List<BeaconDevice> findByCidAndType(String cid, String deviceType) {
		return repository.findByCidAndType(cid,deviceType);
	}

	public net.sf.json.JSONObject makeDiagJson(net.sf.json.JSONObject template) {
	
		try {

			net.sf.json.JSONObject diag_obj = new net.sf.json.JSONObject();
			
			JSONArray attributes = new JSONArray();
			
			if (template.containsKey("attributes")) {
				
				net.sf.json.JSONArray array = template.getJSONArray("attributes");
				
				for (int i = 0; i < array.size(); i++) {

					/*
					 * merge Diagnostic key-value pairs
					 */
					
					if (array.getJSONObject(i).containsKey("diag_key")
							&& array.getJSONObject(i).containsKey("diag_value")) {

						net.sf.json.JSONObject key = array.getJSONObject(i);
						net.sf.json.JSONObject val = array.getJSONObject(i);

						String keyData = (String) key.get("diag_key");
						String valData = (String) val.get("diag_value");

						if (StringUtils.isNotBlank(keyData) && StringUtils.isNotBlank(valData)) {
							diag_obj.put(keyData, valData);
						}
						
						/*
						 * discard input key-value pair part of attributes
						 */
						
						array.getJSONObject(i).remove("diag_key");
						array.getJSONObject(i).remove("diag_value");
						
					} else {
						attributes.add(array.getJSONObject(i));
					}
				}
				
				
				if (attributes.isEmpty() || attributes.size() < 0) {
					template.put("attributes", array);
				} else {
					template.put("attributes", attributes);
				}

				return diag_obj;
			}
		} catch (Exception e) {
			LOG.info("While diag_key-value json convert error ",e);
		}
		return null;
	}

	public boolean notify_delete(BeaconDevice device, String opcode) {
		try {
			
			String cid = "";
			String sid = "";
			String spid = "";
			String ip = "0.0.0.0"; //server ip
			String dev_type = "";
			
			String conf =  device.getConf();
			net.sf.json.JSONObject template	= net.sf.json.JSONObject.fromObject(conf);
			
			if (device.getCid() != null) {
				cid = device.getCid();
				template.put("cid",  cid);
			}
			if (device.getSid() != null) {
				sid = device.getSid();
				template.put("sid",  sid);
			}
			if (device.getSpid() != null) {
				spid = device.getSpid();
				template.put("spid", spid);
			}
			
			String source = device.getSource() == null? "qubercomm":device.getSource();
			
			dev_type = device.getType();
			
			Customer acc = getCustomerService().findById(cid);
			if (acc != null) {
				ip = acc.getBleserverip();
			}
			if (ip==null) {
				ip="0.0.0.0";
			}
		
			String mqttDeleteTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\",\"type\":\"{2}\",\"serverip\":\"{3}\", \"by\":\"{4}\", \"newversion\":\"{5}\", \"conf\":{6},\"source\":\"{7}\"";

			String message = MessageFormat.format(mqttDeleteTemplate,new Object[]{opcode, 
																	   device.getUid().toUpperCase(),
																	   dev_type,
																	   ip,
																	   device.getModifiedBy(),
																	   "1",
																	   template.toString(),
																	   source});
			mqttPublisher.publish("{" + message + "}", device.getUid().toUpperCase());
			
			String deviceType 		   = "server";
			List<BeaconDevice> servers = null;
			if(!dev_type.equals(deviceType)){
				servers= beaconDeviceService.findByCidAndType(cid, deviceType);
			}
			
			if (servers != null && servers.size() > 0) {
				BeaconDevice server = servers.get(0);
				mqttPublisher.publish("{" + message + "}", server.getUid().toUpperCase());
			}
			
			return true;
		} catch (Exception e) {
			LOG.warn("Failed to notify update", e);
			return false;
		}
	}
	public void universal_ServerIP_MQTT(String id,String ip) {
		try {
			
			String opcode = "serverip-update";
			String mqttTemplate = " \"opcode\":\"{0}\", \"serverip\":\"{1}\"";
			
			String message = MessageFormat.format(mqttTemplate, new Object[] { opcode, ip});
			mqttPublisher.publish("{" + message + "}", id);
			
			//LOG.info("universal_ServerIP_MQTT " +message);
			//LOG.info("universal_id  " +id);
			
		} catch (Exception e) {
			LOG.error("while Universal_ServerIP_MQTT  Publich Error ",e);
		}
		

	}
	
	
	private CustomerService getCustomerService() {
		try {
			if (customerService == null) {
				customerService = Application.context.getBean(CustomerService.class);
			}
		} catch (Exception e) {

		}

		return customerService;
	}

	public void resetServerIP(String type, String cid) {
		//LOG.info("****** Reset Server IP ******");
		//LOG.info("Device Type " + type + " cid " + cid);
		if (type != null && type.equals("server")) {
			Customer customer = customerService.findById(cid);
			if (customer != null) {
				customer.setBleserverip("0.0.0.0");
				customer.setModifiedBy("cloud");
				customer.setModifiedOn(new Date(System.currentTimeMillis()));
				customerService.save(customer);
			}
		}

	}

	public List<BeaconDevice> findBySidAndType(String sid, String deviceType) {
		return repository.findBySidAndType(sid,deviceType);
	}

	public List<BeaconDevice> findBySpidAndType(String spid, String deviceType) {
		return repository.findBySpidAndType(spid,deviceType);
	}

	public List<BeaconDevice> findByCidAndName(String cid, String name) {
		List<BeaconDevice> deviceList = new ArrayList<BeaconDevice>();
		List<BeaconDevice> beaconDeviceList = repository.findByCidAndName(cid, name);
		if (beaconDeviceList != null && beaconDeviceList.size() > 0) {
			for (BeaconDevice device : beaconDeviceList) {
				if (device.getName().equalsIgnoreCase(name)) {
					deviceList.add(device);
				}
			}
			return deviceList;
		}
		return beaconDeviceList;
	}
	
	public List<BeaconDevice> findByCidAndTypeAndName(String cid,String type, String name) {
		List<BeaconDevice> deviceList = new ArrayList<BeaconDevice>();
		List<BeaconDevice> beaconDeviceList = repository.findByCidAndTypeAndName(cid, type, name);
		if (beaconDeviceList != null && beaconDeviceList.size() > 0) {
			for (BeaconDevice device : beaconDeviceList) {
				String devName = device.getName().trim();
				if (devName.equalsIgnoreCase(name)) {
					deviceList.add(device);
				}
			}
			return deviceList;
		}
		return beaconDeviceList;
	}

	public BeaconDevice findByUidAndCidAndType(String uid, String cid, String deviceType) {
		return repository.findByUidAndCidAndType(uid,cid,deviceType);
	}

	public List<BeaconDevice> findByCidAndState(String cid, String state) {
		return repository.findByCidAndState(cid,state);
	}
	
	public List<BeaconDevice> findBySidAndState(String sid, String state) {
		return repository.findBySidAndState(sid,state);
	}
	
	public List<BeaconDevice> findBySpidAndState(String spid, String state) {
		return repository.findBySpidAndState(spid,state);
	}
	
	
	public List<BeaconDevice> findByCidTypeAndUids(String cid,String type, JSONArray placeUids) {
		List<BeaconDevice> list = new ArrayList<BeaconDevice>();
		Iterator<String> iter = placeUids.iterator();
		BeaconDevice bd = null;
		String alias = "";
		while (iter.hasNext()) {
			alias = iter.next();
			bd =  findByUidAndCidAndType(alias, cid,type);
			list.add(bd);
		}
		return list;
	}

	public List<BeaconDevice> findByActiveDevices(String spid, String sid) {
		final String state = "active";
		if (sid != null) {
			return findBySidAndState(sid, state);
		} else if (spid != null) {
			return findBySpidAndState(spid, state);
		}
		return null;
	}

	public List<BeaconDevice> findBy(String spid, String sid, String swid) {
		if (sid != null) {
			return findBySid(sid);
		} else if (spid != null) {
			return findBySpid(spid);
		}
		return null;
	}

	public List<BeaconDevice> findByGlobal(String cid, String sid, String spid,String uid) {
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

	public List<BeaconDevice> findBySpidInAndType(List<String> spid, String type) {
		return repository.findBySpidInAndType(spid,type);
	}

	public List<BeaconDevice> findBySidInAndType(List<String> sid, String type) {
		return repository.findBySidInAndType(sid,type);
	}

	public List<BeaconDevice> findByUids(List<String> uids) {
		return repository.findByUids(uids);
	}

	public List<BeaconDevice> findByConfiguredDevice(List<String> status) {
		return repository.findByConfiguredDevice(status);
	}

	public List<BeaconDevice> findByCidAndType(String cid, String deviceType, Sort sort) {
		return repository.findByCidAndType(cid,deviceType,sort);
	}

	public List<BeaconDevice> findBySpid(String spid, Sort sort) {
		return repository.findBySpid(spid,sort);
	}

	public List<BeaconDevice> findBySid(String sid, Sort sort) {
		return repository.findBySid(sid,sort);
	}

	public List<BeaconDevice> findByCid(String cid, Sort sort) {
		return repository.findByCid(cid,sort);
	}
}
