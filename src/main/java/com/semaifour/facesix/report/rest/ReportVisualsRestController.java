package com.semaifour.facesix.report.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.report.data.ReportVisuals;
import com.semaifour.facesix.report.data.ReportVisualsService;
import com.semaifour.facesix.report.util.ReportVisualsResponseUtil;
import com.semaifour.facesix.report.util.ReportVisualsUtil;
import com.semaifour.facesix.util.CustomerUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/rest/report/visuals")
public class ReportVisualsRestController {

	static Logger LOG = LoggerFactory.getLogger(ReportVisualsRestController.class.getName());
	static String reportFromDate = "";
	static String reportToDate = "";

	static Date date = null;
	private final static DateFormat ES_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
	static Calendar cal = Calendar.getInstance();
	
	@Autowired
	ReportVisualsService visualService;

	@Autowired
	CustomerUtils customerUtils;

	@RequestMapping("/save")
	public Restponse<String> save(@RequestBody ReportVisuals visualObject) {
		String message = "Visual Saved Successfully !!";
		boolean success = true;
		int code = 200;
		try {
			visualObject = visualService.save(visualObject);
		} catch (Exception e) {
			e.printStackTrace();
			message = "Failed to Save Visual!! Try again";
			success = false;
			code = 500;
		}
		return new Restponse<String>(success, code, message);
	}

	@RequestMapping("/view")
	public ReportVisuals view(@RequestParam(value = "id", required = true) String id) {
		ReportVisuals visual = null;
		try {
			visual = visualService.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visual;
	}

	/**
	 * @param id
	 * @return
	 */
	@RequestMapping("/reportPreview")
	public JSONObject reportPreview(@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to) {

		ReportVisuals visual = visualService.findById(id);
		if (visual != null) {
			if (StringUtils.isEmpty(from) || StringUtils.isEmpty(to)) {
				buildFormattedDateRange();
				from = reportFromDate;
				to = reportToDate;
			}
			return this.preview(visual, from, to);
		} else {
			JSONObject previewContent = new JSONObject();
			previewContent.put("status", false);
			previewContent.put("id", "na");
			previewContent.put("data", new JSONArray());
			previewContent.put("columns", new JSONArray());
			return previewContent;
		}
	}

	/**
	 * Used to bulk preview
	 * 
	 * @param id
	 * @param from
	 * @param to
	 * @return
	 */
	@RequestMapping(value = "/bulkReportPreview", method = RequestMethod.POST)
	public JSONArray bulkReportPreview(@RequestBody String[] visualIds,
			@RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to) {

		List<String> ids = Arrays.asList(visualIds);

		List<ReportVisuals> visualList = visualService.findByIds(ids);

		JSONArray visualArray = new JSONArray();

		if (visualList != null && visualList.size() > 0) {
			visualList.forEach(visual -> {
				final String visualId = visual.getId();
				if (StringUtils.isNotEmpty(from) && StringUtils.isNotEmpty(to)) {
					buildFormattedDateRange(from, to);
				} else {
					buildFormattedDateRange();
				}
				JSONObject result = this.reportPreview(visualId, reportFromDate, reportToDate);
				visualArray.add(result);
			});
		}

		return visualArray;
	}

	@RequestMapping(value = "/preview", method = RequestMethod.POST)
	public JSONObject preview(@RequestBody ReportVisuals visualObject,
			@RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to) {
		JSONObject previewContent = new JSONObject();
		previewContent.put("status", false);
		previewContent.put("data", new JSONArray());
		previewContent.put("columns", new JSONArray());
		String query = "";
		try {
			LOG.info("Preview for Chart: " + visualObject.getChartType() + "; " + visualObject.getMetrics() + "; "
					+ visualObject.getBuckets() + "; " + visualObject.getFilters());
			if (StringUtils.isEmpty(from) || StringUtils.isEmpty(to)) {
				buildFormattedDateRange();
				from = reportFromDate;
				to = reportToDate;
			}

			query = ReportVisualsUtil.buildSSQLQuery(visualObject, from, to);
			LOG.info("query = " + query);
			JSONObject queryResult = customerUtils.progset_query(query);
			boolean success = (boolean) queryResult.get("success");
			ReportVisualsResponseUtil.buildPreviewResponse(visualObject, queryResult, previewContent, from, to, success);

			System.out.println("Report Response payload " + previewContent);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("Issue while preparing the data for the Visual: " + e.getMessage());
			previewContent.put("data", "Issue while preparing the data for the Visual: " + e.getMessage());
		}
		return previewContent;
	}

	private void buildFormattedDateRange() {
		date = new Date();
		reportToDate = ES_DATEFORMAT.format(date);

		cal.setTime(date);
		cal.add(Calendar.DATE, -1);
		reportFromDate = ES_DATEFORMAT.format(cal.getTime());
	}

	private void buildFormattedDateRange(String fromDateStr, String toDateStr) {
		try {
			if (StringUtils.isNotEmpty(fromDateStr)) {
				date = ES_DATEFORMAT.parse(fromDateStr);
				reportFromDate = ES_DATEFORMAT.format(date);
			}

			if (StringUtils.isNotEmpty(toDateStr)) {
				date = ES_DATEFORMAT.parse(toDateStr);
				reportToDate = ES_DATEFORMAT.format(date);
			}
		} catch (ParseException pe) {
			LOG.error("Error parsing the date from String " + fromDateStr + " " + toDateStr);
		}
	}

	@RequestMapping("/list")
	public List<ReportVisuals> list(@RequestParam(value = "cid", required = true) String cid) {
		List<ReportVisuals> visualList = null;
		try {
			visualList = visualService.findByCid(cid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visualList;
	}

	@RequestMapping("/delete")
	public Restponse<String> delete(@RequestBody List<String> ids) {
		String message = "Visuals Deleted Successfully !!";
		boolean success = true;
		int code = 200;
		try {
			if (ids != null && ids.size() > 0) {
				List<ReportVisuals> visualList = null;
				visualList = visualService.findByIds(ids);
				if (visualList == null || visualList.isEmpty()) {
					message = "Selected Visuals does not exist!!";
					success = false;
					code = 400;
				} else {
					visualService.deleteList(visualList);
				}
			} else {
				message = "Kindly Select Visuals to delete";
				success = false;
				code = 400;
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "Failed to Delete Visual!! Try again";
			success = false;
			code = 500;
		}
		return new Restponse<String>(success, code, message);
	}
}