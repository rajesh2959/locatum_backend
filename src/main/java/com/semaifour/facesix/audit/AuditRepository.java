package com.semaifour.facesix.audit;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditRepository extends MongoRepository<Audit, String>{
	public List<Audit> findByAuditEvent(String auditEvent);
	
}