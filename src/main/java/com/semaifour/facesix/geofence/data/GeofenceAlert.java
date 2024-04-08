package com.semaifour.facesix.geofence.data;

import java.util.List;

import org.springframework.data.annotation.Id;

public class GeofenceAlert {

	@Id
	private String id;

	private String cid;
	private String name;
	private String triggerType; // entry,exit,both
	private String status; // enabled,disabled
	private String category; // tagname,tagtype
	private List<String> channel; // sms,mail,popup
	private List<String> associations; // list of tagids or tagtypes
	private String pkid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<String> getChannel() {
		return channel;
	}

	public void setChannel(List<String> channel) {
		this.channel = channel;
	}

	public List<String> getAssociations() {
		return associations;
	}

	public void setAssociations(List<String> associations) {
		this.associations = associations;
	}

	public String getPkid() {
		return pkid;
	}

	public void setPkid(String pkid) {
		this.pkid = pkid;
	}

	@Override
	public String toString() {
		return "GeofenceAlert [id=" + id + ", cid=" + cid + ", name=" + name + ", triggerType=" + triggerType
				+ ", status=" + status + ", category=" + category + ", channel=" + channel + ", associations="
				+ associations + ", pkid=" + pkid + "]";
	}
}
