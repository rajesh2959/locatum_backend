package com.semaifour.facesix.report.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.report.data.ReportDashboard;
import com.semaifour.facesix.report.data.ReportDashboardService;

@RestController
@RequestMapping("/rest/report/dashboard")
public class ReportDashboardRestController {

	@Autowired
	ReportDashboardService reportDashboardService;

	@RequestMapping("/save")
	public Restponse<String> save(@RequestBody ReportDashboard dashboarData) {
		boolean success = true;
		int code = 200;
		String message = "Dashboard Saved Successfully";
		try {
			reportDashboardService.save(dashboarData);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
			code = 500;
			message = "Failed to save the Dashboard! ";
		}
		return new Restponse<String>(success, code, message);
	}

	@RequestMapping("/view")
	public ReportDashboard view(@RequestParam(value = "id", required = true) String id) {
		ReportDashboard dashboard = null;
		try {
			dashboard = reportDashboardService.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dashboard;
	}

	@RequestMapping("/list")
	public List<ReportDashboard> list(@RequestParam(value = "cid", required = true) String cid) {
		List<ReportDashboard> dashboardList = null;
		try {
			dashboardList = reportDashboardService.findByCid(cid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dashboardList;
	}

	@RequestMapping("/delete")
	public Restponse<String> delete(@RequestBody List<String> ids) {
		boolean success = true;
		int code = 200;
		String message = "Dashboard Deleted Succesfully";
		try {
			if (ids != null && !ids.isEmpty()) {
				List<ReportDashboard> dashboardlist = reportDashboardService.findByIds(ids);
				if (dashboardlist == null || dashboardlist.isEmpty()) {
					success = false;
					code = 404;
					message = "Dasboard you are trying to delete does not exist!";
				} else {
					reportDashboardService.deleteList(dashboardlist);
				}
			} else {
				success = false;
				code = 400;
				message = "Seclect Dashobards to delete!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
			code = 500;
			message = "Failed to delete the dashboard ! Try again";
		}
		return new Restponse<String>(success, code, message);
	}
}
