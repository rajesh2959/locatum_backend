package com.semaifour.facesix.report.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportDashboardService {

	@Autowired
	ReportDashboardRepository repository;

	public ReportDashboard save(ReportDashboard dashboarData) {
		return repository.save(dashboarData);
	}
	
	public ReportDashboard findById(String id) {
		return repository.findById(id);
	}

	public List<ReportDashboard> findByCid(String cid) {
		return repository.findByCid(cid);
	}

	public List<ReportDashboard> findByIds(List<String> ids) {
		return repository.findByIds(ids);
	}

	public void delete(ReportDashboard dashboard) {
		repository.delete(dashboard);
	}

	public void deleteList(List<ReportDashboard> dashboardlist) {
		repository.delete(dashboardlist);
	}

}
