package com.semaifour.facesix.geofence.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GeofenceAlertRepository extends MongoRepository<GeofenceAlert, String>{

	GeofenceAlert findOneById(String id);
	
	@Query("{id:{$in:?0}}")
	List<GeofenceAlert> findByIds(List<String> ids);
	
	List<GeofenceAlert> findByCid(String cid);

	@Query("{cid:?0,name: {$regex : '^?1$', $options: 'i'}}")
	List<GeofenceAlert> findByCidAndName(String cid, String name);

	@Query("{id:{$in:?0},triggerType:{$in:?1},associations:{$in:?2}}")
	List<GeofenceAlert> findByIdsTriggertypeAndAssociations(List<String> id, List<String> triggerType, List<String> associations);

	@Query("{cid:?0,status:?1}")
	List<GeofenceAlert> findByCidAndStatus(String cid, String status);

}
