package com.semaifour.facesix.audit;

import java.util.Date;
import org.json.JSONObject;
import com.semaifour.facesix.domain.FSObject;

public class Audit extends FSObject{
	
	//audit fields
	private String auditEvent;
	
	private String collectionName;
	private String collectionPkid;
	
	private String doneBy;
	private String doneTo;
	
	private JSONObject changedFrom;
	private JSONObject changedTo;
	
	
	public String getAuditEvent() {
		return auditEvent;
	}
	public String getCollectionName() {
		return collectionName;
	}
	public String getCollectionPkid() {
		return collectionPkid;
	}
	public String getDoneBy() {
		return doneBy;
	}
	public String getDoneTo() {
		return doneTo;
	}
	public JSONObject getChangedFrom() {
		return changedFrom;
	}
	public JSONObject getChangedTo() {
		return changedTo;
	}
	public void setAuditEvent(String auditEvent) {
		this.auditEvent = auditEvent;
	}
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	public void setCollectionPkid(String collectionPkid) {
		this.collectionPkid = collectionPkid;
	}
	public void setDoneBy(String doneBy) {
		this.doneBy = doneBy;
	}
	public void setDoneTo(String doneTo) {
		this.doneTo = doneTo;
	}
	public void setChangedFrom(JSONObject changedFrom) {
		this.changedFrom = changedFrom;
	}
	public void setChangedTo(JSONObject changedTo) {
		this.changedTo = changedTo;
	}
	@Override
	public String toString() {
		return "Audit [auditEvent=" + auditEvent + ", collectionName=" + collectionName + ", collectionPkid="
				+ ", changedFrom=" + changedFrom + ", changedTo=" + changedTo + "]";
	}
	
	
}
