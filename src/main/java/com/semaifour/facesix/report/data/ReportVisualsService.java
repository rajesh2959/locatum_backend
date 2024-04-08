package com.semaifour.facesix.report.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportVisualsService {

	@Autowired
	ReportVisualsRepository repository;

	public ReportVisuals save(ReportVisuals visualObject) {
		visualObject = repository.save(visualObject);
		return visualObject;
	}

	public List<ReportVisuals> findByCid(String cid) {
		return repository.findByCid(cid);
	}

	public List<ReportVisuals> findByIds(List<String> ids) {
		return repository.findByIds(ids);
	}

	public ReportVisuals findById(String id) {
		return repository.findById(id);
	}

	public void delete(ReportVisuals visual) {
		repository.delete(visual);
	}
	
	public void deleteList(List<ReportVisuals> visualList) {
		repository.delete(visualList);
	}


}
