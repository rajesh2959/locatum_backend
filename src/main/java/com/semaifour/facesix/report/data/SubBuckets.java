package com.semaifour.facesix.report.data;

public class SubBuckets {

	private String splitBy;
	private String fieldName;

	public String getSplitBy() {
		return splitBy;
	}

	public void setSplitBy(String splitBy) {
		this.splitBy = splitBy;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toString() {
		return "SubBuckets [splitBy=" + splitBy + ", fieldName=" + fieldName + "]";
	}

}
