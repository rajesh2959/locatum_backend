package com.semaifour.facesix.report.data;

import java.util.List;

import org.springframework.data.annotation.Id;

public class ReportDashboard extends ReportBase {

	@Id
	private String id;
	private List<String> visualId;
	private String uiContent;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getVisualId() {
		return visualId;
	}

	public void setVisualId(List<String> visualId) {
		this.visualId = visualId;
	}

	public String getUiContent() {
		return uiContent;
	}

	public void setUiContent(String uiContent) {
		this.uiContent = uiContent;
	}

	@Override
	public String toString() {
		return "ReportDashboards [id=" + id + ", visualId=" + visualId + ", uiContent=" + uiContent + "]";
	}

}
