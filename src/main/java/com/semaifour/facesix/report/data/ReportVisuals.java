package com.semaifour.facesix.report.data;

import java.util.List;

import org.springframework.data.annotation.Id;

public class ReportVisuals extends ReportBase {

	@Id
	private String id;
	private String chartType;
	private List<ReportLabels> metrics;
	private List<ReportLabels> buckets;
	private List<Filter> filters;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public List<ReportLabels> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<ReportLabels> metrics) {
		this.metrics = metrics;
	}

	public List<ReportLabels> getBuckets() {
		return buckets;
	}

	public void setBuckets(List<ReportLabels> buckets) {
		this.buckets = buckets;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	@Override
	public String toString() {
		return "ReportVisuals [id=" + id + ", chartType=" + chartType + ", metrics=" + metrics + ", buckets=" + buckets
				+ ", filters=" + filters + "]";
	}

}
