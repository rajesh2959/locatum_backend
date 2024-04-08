package com.semaifour.facesix.report.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ReportVisualsRepository extends MongoRepository<ReportVisuals, String> {

	ReportVisuals findById(String id);

	@Query("{id:{$in:?0}}")
	List<ReportVisuals> findByIds(List<String> ids);

	List<ReportVisuals> findByCid(String cid);

}
