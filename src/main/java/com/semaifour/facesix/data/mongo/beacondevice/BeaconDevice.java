package com.semaifour.facesix.data.mongo.beacondevice;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.semaifour.facesix.domain.FSObject;

@Document(collection="BeaconDevice")
public class BeaconDevice extends FSObject {
	
	public enum STATUS { REGISTERED, CONFIGURED, AUTOCONFIGURED }
	public enum STATE  { inactive, active}
	public enum THIRD_PARTY_STATE  { ACTIVE, PAUSED, UNKNOWN}
	public enum GATEWAY_TYPE {server,receiver};
	
	@Id
	private String  id;
	private String  uuid;
	private String  fstype;
	private String  conf;
	private String  template;
	private String  state;
	private String  others;
	public  String  sid;
	public  String  spid;
	private String  cid;
	private int	    activetag;
	private String  tagjsonstring;
	private String 	type;
	private int		checkedintag;
	private int		checkedoutTag;
	private int		exitTag;
	private String	ip;
	private String	personType;
	private String  geopoints;
	private String  georesult;
	private String  pixelresult;
	private String  coordinateresult;
	private String  build;
	private String debugflag;
	private String keepAliveInterval;
	private double    tlu;
	private String lastseen;
	private String customizeInactivityMailSent;
	private String devCrashTimestamp;
	private String devCrashdumpFileName;
	private String devCrashDumpUploadStatus;
	private String tunnelIp;
	private String source;
	
	public String 	parent;
	public String 	svid;
	public String 	swid;	
	public String 	xposition;
	public String 	yposition;
	public String 	gparent;
	
	private String binaryType;
	private String upgradeType;
	private String binaryReason;
	
	public BeaconDevice() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFstype() {
		return fstype;
	}

	public void setFstype(String fstype) {
		this.fstype = fstype;
	}

	public String getConf() {
		return conf;
	}

	public void setConf(String conf) {
		this.conf = conf;
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

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
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

	public int getActivetag() {
		return activetag;
	}

	public void setActivetag(int activetag) {
		this.activetag = activetag;
	}

	public String getTagjsonstring() {
		return tagjsonstring;
	}

	public void setTagjsonstring(String tagjsonstring) {
		this.tagjsonstring = tagjsonstring;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCheckedintag() {
		return checkedintag;
	}

	public void setCheckedintag(int checkedintag) {
		this.checkedintag = checkedintag;
	}

	public int getCheckedoutTag() {
		return checkedoutTag;
	}

	public void setCheckedoutTag(int checkedoutTag) {
		this.checkedoutTag = checkedoutTag;
	}

	public int getExitTag() {
		return exitTag;
	}

	public void setExitTag(int exitTag) {
		this.exitTag = exitTag;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPersonType() {
		return personType;
	}

	public void setPersonType(String personType) {
		this.personType = personType;
	}

	public String getGeopoints() {
		return geopoints;
	}

	public void setGeopoints(String geopoints) {
		this.geopoints = geopoints;
	}

	public String getGeoresult() {
		return georesult;
	}

	public void setGeoresult(String georesult) {
		this.georesult = georesult;
	}

	public String getPixelresult() {
		return pixelresult;
	}

	public void setPixelresult(String pixelresult) {
		this.pixelresult = pixelresult;
	}

	public String getCoordinateresult() {
		return coordinateresult;
	}

	public void setCoordinateresult(String coordinateresult) {
		this.coordinateresult = coordinateresult;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String getDebugflag() {
		return debugflag;
	}

	public void setDebugflag(String debugflag) {
		this.debugflag = debugflag;
	}

	public String getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public void setKeepAliveInterval(String keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	public double getTlu() {
		return tlu;
	}

	public void setTlu(double tlu) {
		this.tlu = tlu;
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

	public String getDevCrashTimestamp() {
		return devCrashTimestamp;
	}

	public void setDevCrashTimestamp(String devCrashTimestamp) {
		this.devCrashTimestamp = devCrashTimestamp;
	}

	public String getDevCrashdumpFileName() {
		return devCrashdumpFileName;
	}

	public void setDevCrashdumpFileName(String devCrashdumpFileName) {
		this.devCrashdumpFileName = devCrashdumpFileName;
	}

	public String getDevCrashDumpUploadStatus() {
		return devCrashDumpUploadStatus;
	}

	public void setDevCrashDumpUploadStatus(String devCrashDumpUploadStatus) {
		this.devCrashDumpUploadStatus = devCrashDumpUploadStatus;
	}

	public String getTunnelIp() {
		return tunnelIp;
	}

	public void setTunnelIp(String tunnelIp) {
		this.tunnelIp = tunnelIp;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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
	
	public String getBinaryReason() {
		return binaryReason;
	}

	public void setBinaryReason(String binaryReason) {
		this.binaryReason = binaryReason;
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

	public String getGparent() {
		return gparent;
	}

	public void setGparent(String gparent) {
		this.gparent = gparent;
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

	@Override
	public String toString() {
		return "BeaconDevice [id=" + id + ", uuid=" + uuid + ", fstype=" + fstype + ", conf=" + conf + ", template="
				+ template + ", state=" + state + ", others=" + others + ", sid=" + sid + ", spid=" + spid + ", cid="
				+ cid + ", activetag=" + activetag + ", tagjsonstring=" + tagjsonstring + ", type="
				+ type + ", checkedintag=" + checkedintag + ", checkedoutTag=" + checkedoutTag + ", exitTag=" + exitTag
				+ ", ip=" + ip + ", personType=" + personType + ", geopoints=" + geopoints + ", georesult=" + georesult
				+ ", pixelresult=" + pixelresult + ", coordinateresult=" + coordinateresult + ", build=" + build
				+ ", debugflag=" + debugflag + ", keepAliveInterval=" + keepAliveInterval + ", tlu=" + tlu
				+ ", lastseen=" + lastseen + ", customizeInactivityMailSent=" + customizeInactivityMailSent
				+ ", devCrashTimestamp=" + devCrashTimestamp + ", devCrashdumpFileName=" + devCrashdumpFileName
				+ ", devCrashDumpUploadStatus=" + devCrashDumpUploadStatus + ", tunnelIp=" + tunnelIp + ", source="
				+ source + ", xposition=" + xposition + ", yposition=" + yposition + "]";
	}

	
}
