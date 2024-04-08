package com.semaifour.facesix.audit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
	static Logger LOG = LoggerFactory.getLogger(AuditService.class.getName());
	
	@Autowired
	private AuditRepository auditRepository;
	
	public List<Audit> findByAuditEvent(String auditEvent){
		return auditRepository.findByAuditEvent(auditEvent);
	}
	
	public Audit save(Audit audit){
		audit = auditRepository.save(audit);
		return audit;
	}
}
