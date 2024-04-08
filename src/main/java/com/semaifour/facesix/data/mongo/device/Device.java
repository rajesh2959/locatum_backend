package com.semaifour.facesix.data.mongo.device;

import java.util.LinkedHashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.semaifour.facesix.domain.FSObject;
import com.semaifour.facesix.rest.NetworkConfRestController;

@Document(collection="Device")
public class Device extends FSObject {
	
	public enum STATUS { REGISTERED, CONFIGURED, AUTOCONFIGURED }
	public enum STATE  { inactive, active, idle}
	
	static Logger LOG = LoggerFactory.getLogger(NetworkConfRestController.class.getName());


	@Id
	private String id;
	private String conf;
	private String fstype;
	private String template;
	private String state;
	private int vap2gcount;
	private int vap5gcount;
	public String  sid;
	public String  spid;
	private String cid;
	private String ip;
	private String role;
	private int network_balancer;
	public String keepAliveInterval;
	public String root;
	public String lastseen;
	private String customizeInactivityMailSent;
	private String customizekeepAliveflag;
	public String workingMode;
	public String 	parent;
	public String 	svid;
	public String 	swid;	
	public String 	xposition;
	public String 	yposition;
	public String 	gparent;
	
	private String lanbridge;
	private String wanbridge;
	
	/*
	 * Device TX and Rx
	 * 
	 */
	
	public long     vap_tx_bytes;
	public long     vap_rx_bytes;
	public long     lastPeerUpdates;
	
	private String   graylogtime;
	
	/*
	 *  LAN Configuration
	 * 
	 */
	private String lan_Flag;
	private String lan_ipv4_Addr;
	private String lan_ipv4_Mask;
	private String lan_ipv4_dns;
	private String lan_ipv4_dns1;
	private String lan_ipv4_gateway;
	private String lan_ipv4_dhcp_dns;
	private String lan_ipv4_dhcp_dns1;
	
	/*
	 *  WAN Configuration
	 */
	
	private String wan_Flag;
	private String wan_ipv4_Addr;
	private String wan_ipv4_mask;
	private String wan_ipv4_dns;
	private String wan_ipv4_dns1;
	private String wan_ipv4_gateway;
	private String wan_ipv4_dhcp_dns;
	private String wan_ipv4_dhcp_dns1;
	
	private String dhcp_svr_state;
		
	/*
	 * Device BuildVersion And BuildTime
	 * 
	 */
	
	private String buildVersion;
	private String buildTime;
	
	private String lan_Only;
	
	
	private double deviceTxBytes;
	private double deviceRxBytes;
	
	private int twogChannel;
	private int fivegChannel;
	
	private String binaryType;
	private String upgradeType;
	private String binaryReason;
	
		
	public Device() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConf() {
		return conf;
	}

	public void setConf(String conf) {
		this.conf = conf;
	}

	public String getFstype() {
		return fstype;
	}

	public void setFstype(String fstype) {
		this.fstype = fstype;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getVap2gcount() {
		return vap2gcount;
	}

	public void setVap2gcount(int vap2gcount) {
		this.vap2gcount = vap2gcount;
	}

	public int getVap5gcount() {
		return vap5gcount;
	}

	public void setVap5gcount(int vap5gcount) {
		this.vap5gcount = vap5gcount;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSpid() {
		return spid;
	}

	public void setSpid(String spid) {
		this.spid = spid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public void setKeepAliveInterval(String keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getLastseen() {
		return lastseen;
	}

	public void setLastseen(String lastseen) {
		this.lastseen = lastseen;
	}

	public String getCustomizeInactivityMailSent() {
		return customizeInactivityMailSent;
	}

	public void setCustomizeInactivityMailSent(String customizeInactivityMailSent) {
		this.customizeInactivityMailSent = customizeInactivityMailSent;
	}

	public String getCustomizekeepAliveflag() {
		return customizekeepAliveflag;
	}

	public void setCustomizekeepAliveflag(String customizekeepAliveflag) {
		this.customizekeepAliveflag = customizekeepAliveflag;
	}

	public String getWorkingMode() {
		return workingMode;
	}

	public void setWorkingMode(String workingMode) {
		this.workingMode = workingMode;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getSvid() {
		return svid;
	}

	public void setSvid(String svid) {
		this.svid = svid;
	}

	public String getSwid() {
		return swid;
	}

	public void setSwid(String swid) {
		this.swid = swid;
	}

	public String getXposition() {
		return xposition;
	}

	public void setXposition(String xposition) {
		this.xposition = xposition;
	}

	public String getYposition() {
		return yposition;
	}

	public void setYposition(String yposition) {
		this.yposition = yposition;
	}

	public String getGparent() {
		return gparent;
	}

	public void setGparent(String gparent) {
		this.gparent = gparent;
	}

	public long getVap_tx_bytes() {
		return vap_tx_bytes;
	}

	public void setVap_tx_bytes(long vap_tx_bytes) {
		this.vap_tx_bytes = vap_tx_bytes;
	}

	public long getVap_rx_bytes() {
		return vap_rx_bytes;
	}

	public void setVap_rx_bytes(long vap_rx_bytes) {
		this.vap_rx_bytes = vap_rx_bytes;
	}

	public long getLastPeerUpdates() {
		return lastPeerUpdates;
	}

	public void setLastPeerUpdates(long lastPeerUpdates) {
		this.lastPeerUpdates = lastPeerUpdates;
	}

	public String getGraylogtime() {
		return graylogtime;
	}

	public void setGraylogtime(String graylogtime) {
		this.graylogtime = graylogtime;
	}

	public String getLan_Flag() {
		return lan_Flag;
	}

	public void setLan_Flag(String lan_Flag) {
		this.lan_Flag = lan_Flag;
	}

	public String getLan_ipv4_Addr() {
		return lan_ipv4_Addr;
	}

	public void setLan_ipv4_Addr(String lan_ipv4_Addr) {
		this.lan_ipv4_Addr = lan_ipv4_Addr;
	}

	public String getLan_ipv4_Mask() {
		return lan_ipv4_Mask;
	}

	public void setLan_ipv4_Mask(String lan_ipv4_Mask) {
		this.lan_ipv4_Mask = lan_ipv4_Mask;
	}

	public String getLan_ipv4_dns() {
		return lan_ipv4_dns;
	}

	public void setLan_ipv4_dns(String lan_ipv4_dns) {
		this.lan_ipv4_dns = lan_ipv4_dns;
	}

	public String getLan_ipv4_dns1() {
		return lan_ipv4_dns1;
	}

	public void setLan_ipv4_dns1(String lan_ipv4_dns1) {
		this.lan_ipv4_dns1 = lan_ipv4_dns1;
	}

	public String getLan_ipv4_gateway() {
		return lan_ipv4_gateway;
	}

	public void setLan_ipv4_gateway(String lan_ipv4_gateway) {
		this.lan_ipv4_gateway = lan_ipv4_gateway;
	}

	public String getLan_ipv4_dhcp_dns() {
		return lan_ipv4_dhcp_dns;
	}

	public void setLan_ipv4_dhcp_dns(String lan_ipv4_dhcp_dns) {
		this.lan_ipv4_dhcp_dns = lan_ipv4_dhcp_dns;
	}

	public String getLan_ipv4_dhcp_dns1() {
		return lan_ipv4_dhcp_dns1;
	}

	public void setLan_ipv4_dhcp_dns1(String lan_ipv4_dhcp_dns1) {
		this.lan_ipv4_dhcp_dns1 = lan_ipv4_dhcp_dns1;
	}

	public String getWan_Flag() {
		return wan_Flag;
	}

	public void setWan_Flag(String wan_Flag) {
		this.wan_Flag = wan_Flag;
	}

	public String getWan_ipv4_Addr() {
		return wan_ipv4_Addr;
	}

	public void setWan_ipv4_Addr(String wan_ipv4_Addr) {
		this.wan_ipv4_Addr = wan_ipv4_Addr;
	}

	public String getWan_ipv4_mask() {
		return wan_ipv4_mask;
	}

	public void setWan_ipv4_mask(String wan_ipv4_mask) {
		this.wan_ipv4_mask = wan_ipv4_mask;
	}

	public String getWan_ipv4_dns() {
		return wan_ipv4_dns;
	}

	public void setWan_ipv4_dns(String wan_ipv4_dns) {
		this.wan_ipv4_dns = wan_ipv4_dns;
	}

	public String getWan_ipv4_dns1() {
		return wan_ipv4_dns1;
	}

	public void setWan_ipv4_dns1(String wan_ipv4_dns1) {
		this.wan_ipv4_dns1 = wan_ipv4_dns1;
	}

	public String getWan_ipv4_gateway() {
		return wan_ipv4_gateway;
	}

	public void setWan_ipv4_gateway(String wan_ipv4_gateway) {
		this.wan_ipv4_gateway = wan_ipv4_gateway;
	}

	public String getWan_ipv4_dhcp_dns() {
		return wan_ipv4_dhcp_dns;
	}

	public void setWan_ipv4_dhcp_dns(String wan_ipv4_dhcp_dns) {
		this.wan_ipv4_dhcp_dns = wan_ipv4_dhcp_dns;
	}

	public String getWan_ipv4_dhcp_dns1() {
		return wan_ipv4_dhcp_dns1;
	}

	public void setWan_ipv4_dhcp_dns1(String wan_ipv4_dhcp_dns1) {
		this.wan_ipv4_dhcp_dns1 = wan_ipv4_dhcp_dns1;
	}

	public String getDhcp_svr_state() {
		return dhcp_svr_state;
	}

	public void setDhcp_svr_state(String dhcp_svr_state) {
		this.dhcp_svr_state = dhcp_svr_state;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(String buildTime) {
		this.buildTime = buildTime;
	}

	public String getLan_Only() {
		return lan_Only;
	}

	public void setLan_Only(String lan_Only) {
		this.lan_Only = lan_Only;
	}

	public String getBinaryReason() {
		return binaryReason;
	}

	public void setBinaryReason(String binaryReason) {
		this.binaryReason = binaryReason;
	}

	public double getDeviceTxBytes() {
		return deviceTxBytes;
	}

	public void setDeviceTxBytes(double deviceTxBytes) {
		this.deviceTxBytes = deviceTxBytes;
	}

	public double getDeviceRxBytes() {
		return deviceRxBytes;
	}

	public void setDeviceRxBytes(double deviceRxBytes) {
		this.deviceRxBytes = deviceRxBytes;
	}

	public int getTwogChannel() {
		return twogChannel;
	}

	public void setTwogChannel(int twogChannel) {
		this.twogChannel = twogChannel;
	}

	public int getFivegChannel() {
		return fivegChannel;
	}

	public void setFivegChannel(int fivegChannel) {
		this.fivegChannel = fivegChannel;
	}

	public String getBinaryType() {
		return binaryType;
	}

	public void setBinaryType(String binaryType) {
		this.binaryType = binaryType;
	}

	public String getUpgradeType() {
		return upgradeType;
	}

	public void setUpgradeType(String upgradeType) {
		this.upgradeType = upgradeType;
	}

	public int getNetwork_balancer() {
		return network_balancer;
	}

	public void setNetwork_balancer(int network_balancer) {
		this.network_balancer = network_balancer;
	}

	public String getLanbridge() {
		return lanbridge;
	}

	public void setLanbridge(String lanbridge) {
		this.lanbridge = lanbridge;
	}

	public String getWanbridge() {
		return wanbridge;
	}

	public void setWanbridge(String wanbridge) {
		this.wanbridge = wanbridge;
	}

	@Override
	public boolean equals(Object object) {
		/*
		 * Check if object is an instance of Device or not
		 */
		if (!(object instanceof Device)) {
			return false;
		}
		/* 
		 * typecast object to Device so that we can compare data members
		 */
		Device device = (Device) object;
		boolean equal = false;
		
		LinkedHashMap<String,Object> existingDeviceMap = getDeviceMap(this);
		LinkedHashMap<String,Object> changedDeviceMap  = getDeviceMap(device);

		if (existingDeviceMap.equals(changedDeviceMap) && confEqual(getConf(), device.getConf())) {
			equal = true;
		}
		return equal;
	}
	
	/*
	 * A method to create a Map for device data members
	 */
	public LinkedHashMap<String,Object> getDeviceMap(Device device){
		
		LinkedHashMap<String,Object> deviceMap = new LinkedHashMap<String,Object>();

		deviceMap.put("id", 			  	 device.getId());
		deviceMap.put("state",			  	 device.getState());
		deviceMap.put("sid", 		      	 device.getSid());
		deviceMap.put("spid", 				 device.getSpid());
		deviceMap.put("cid", 				 device.getCid());
		deviceMap.put("client_balancer",	 device.getNetwork_balancer());
		deviceMap.put("KeepAliveInterval", 	 device.getKeepAliveInterval());
		deviceMap.put("root", 			 	 device.getRoot());
		deviceMap.put("workingMode", 	 	 device.getWorkingMode());
		deviceMap.put("template",		 	 device.getTemplate());
		
		deviceMap.put("lan_Flag",		 	 device.getLan_Flag());
		deviceMap.put("lan_ipv4_Addr",	 	 device.getLan_ipv4_Addr());
		deviceMap.put("lan_ipv4_Mask",		 device.getLan_ipv4_Mask());
		deviceMap.put("lan_ipv4_dns",		 device.getLan_ipv4_dns());
		deviceMap.put("lan_ipv4_dns1",		 device.getLan_ipv4_dns1());
		deviceMap.put("lan_ipv4_dhcp_dns",	 device.getLan_ipv4_dhcp_dns());
		deviceMap.put("lan_ipv4_dhcp_dns1",	 device.getLan_ipv4_dhcp_dns1());
		deviceMap.put("lan_ipv4_gateway",	 device.getLan_ipv4_gateway());
		deviceMap.put("lan_Only",			 device.getLan_Only());
		
		deviceMap.put("wan_Flag",			 device.getWan_Flag());
		deviceMap.put("wan_ipv4_Addr",		 device.getWan_ipv4_Addr());
		deviceMap.put("wan_ipv4_mask",		 device.getWan_ipv4_mask());
		deviceMap.put("wan_ipv4_dns",		 device.getWan_ipv4_dns());
		deviceMap.put("wan_ipv4_dns1",		 device.getWan_ipv4_dns1());
		deviceMap.put("wan_ipv4_dhcp_dns",	 device.getWan_ipv4_dhcp_dns());
		deviceMap.put("wan_ipv4_dhcp_dns1",	 device.getWan_ipv4_dhcp_dns1());
		deviceMap.put("wan_ipv4_gateway",	 device.getWan_ipv4_gateway());
		
		deviceMap.put("lanbridge",			 device.getLanbridge());
		deviceMap.put("wanbridge",			 device.getWanbridge());
		
		deviceMap.put("uid",				 device.getUid());
		deviceMap.put("typefs",				 device.getTypefs());
		deviceMap.put("status",				 device.getStatus());
		deviceMap.put("createdBy",			 device.getCreatedBy());
		deviceMap.put("modifiedBy",			 device.getModifiedBy());
		
		
		return deviceMap;
	}
	
	
	
	/*
	 * A method to check the configuration of two objects are equal or not.
	 */
	public boolean confEqual(String prevConf, String currConf){
		ObjectMapper objectMapper 	= new ObjectMapper();
		boolean confEqual = false;

		try {
			Map<String, Object> prevConfMap = (Map<String, Object>) (objectMapper.readValue(prevConf, Map.class));
			Map<String, Object> currConfMap = (Map<String, Object>) (objectMapper.readValue(conf, Map.class));

			if (prevConfMap.equals(currConfMap)) {
				confEqual = true;
			}

		} catch (Exception e) {
			LOG.info("Ap Config equality Error ", e);
		}
		return confEqual;
	}
	
}
