package com.semaifour.facesix.report.data;

public class ReportBase {

	private String cid;
	private String entityType;
	private String name;
	private String description;
	private long lastmodified;

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(long lastmodified) {
		this.lastmodified = lastmodified;
	}

	@Override
	public String toString() {
		return "ReportBase [cid=" + cid + ", entityType=" + entityType + ", name=" + name + ", description="
				+ description + ", lastmodified=" + lastmodified + "]";
	}
}
