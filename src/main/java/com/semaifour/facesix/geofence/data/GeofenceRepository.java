package com.semaifour.facesix.geofence.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GeofenceRepository extends MongoRepository<Geofence, String> {

	Geofence findOneById(String id);

	@Query("{id:{$in:?0}}")
	List<Geofence> findByIds(List<String> ids);

	List<Geofence> findByCid(String cid);

	List<Geofence> findBySid(String sid);

	List<Geofence> findBySpid(String spid);

	List<Geofence> findByNameIgnoreCase(String fenceName);

	@Query("{spid:?0,name: {$regex : '^?1$', $options: 'i'}}")
	List<Geofence> findBySpidAndName(String spid, String name);

	@Query("{cid:?0,status: {$regex : '^?1$', $options: 'i'}}")
	List<Geofence> findByCidAndStatus(String cid, String status);

	@Query("{sid:?0,status: {$regex : '^?1$', $options: 'i'}}")
	List<Geofence> findBySidAndStatus(String sid, String status);

	@Query("{spid:?0,status: {$regex : '^?1$', $options: 'i'}}")
	List<Geofence> findBySpidAndStatus(String spid, String status);

	@Query("{spid:{$in:?0},status: {$regex : '^?1$', $options: 'i'}}")
	List<Geofence> findBySpidInAndStatus(List<String> spid, String status);

	@Query("{sid:{$in:?0},status: {$regex : '^?1$', $options: 'i'}}")
	List<Geofence> findBySidInAndStatus(List<String> sid, String status);

}
