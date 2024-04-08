package com.semaifour.facesix.report.data;

import java.util.List;

public class ReportLabels {

	private String customLabel;
	private String aggOperation;
	private String fieldName;
	private List<SubBuckets> subBuckets;

	public String getCustomLabel() {
		return customLabel;
	}

	public void setCustomLabel(String customLabel) {
		this.customLabel = customLabel;
	}

	public String getAggOperation() {
		return aggOperation;
	}

	public void setAggOperation(String aggOperation) {
		this.aggOperation = aggOperation;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public List<SubBuckets> getSubBuckets() {
		return subBuckets;
	}

	public void setSubBuckets(List<SubBuckets> subBuckets) {
		this.subBuckets = subBuckets;
	}

	@Override
	public String toString() {
		return "ReportLabels [customLabel=" + customLabel + ", aggOperation=" + aggOperation + ", fieldName="
				+ fieldName + ", subBuckets=" + subBuckets + "]";
	}

}
