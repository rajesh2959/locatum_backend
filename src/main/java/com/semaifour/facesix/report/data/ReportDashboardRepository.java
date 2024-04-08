package com.semaifour.facesix.report.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ReportDashboardRepository extends MongoRepository<ReportDashboard, String> {

	ReportDashboard findById(String id);

	@Query("{id:{$in:?0}}")
	List<ReportDashboard> findByIds(List<String> id);

	List<ReportDashboard> findByCid(String cid);

}
