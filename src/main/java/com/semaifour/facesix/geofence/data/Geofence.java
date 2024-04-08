package com.semaifour.facesix.geofence.data;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

public class Geofence {

	@Id
	private String id;

	private String name;
	private String fenceType;
	private String cid;
	private String sid;
	private String spid;
	private List<Point> xyPoints;
	private List<String> associatedAlerts;
	private String uiCoordinates;
	private String status;
	private String pkid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFenceType() {
		return fenceType;
	}

	public void setFenceType(String fenceType) {
		this.fenceType = fenceType;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
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

	public List<Point> getXyPoints() {
		return xyPoints;
	}

	public void setXyPoints(List<Point> xyPoints) {
		this.xyPoints = xyPoints;
	}

	public List<String> getAssociatedAlerts() {
		return associatedAlerts;
	}

	public void setAssociatedAlerts(List<String> associatedAlerts) {
		this.associatedAlerts = associatedAlerts;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPkid() {
		return pkid;
	}

	public void setPkid(String pkid) {
		this.pkid = pkid;
	}

	public String getUiCoordinates() {
		return uiCoordinates;
	}

	public void setUiCoordinates(String uiCoordinates) {
		this.uiCoordinates = uiCoordinates;
	}

	@Override
	public String toString() {
		return "Geofence [id=" + id + ", name=" + name + ", fenceType=" + fenceType + ", cid=" + cid + ", sid=" + sid
				+ ", spid=" + spid + ", xyPoints=" + xyPoints + ", associatedAlerts=" + associatedAlerts
				+ ", uiCoordinates=" + uiCoordinates + ", status=" + status + ", pkid=" + pkid + "]";
	}

}
