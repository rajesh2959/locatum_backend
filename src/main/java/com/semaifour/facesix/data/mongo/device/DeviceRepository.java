package com.semaifour.facesix.data.mongo.device;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DeviceRepository extends MongoRepository<Device, String> {

	public List<Device> findByName(String name);

	@Query("{uid: {$regex : '^?0$', $options: 'i'}}")
	public List<Device> findByUid(String uid);

	public List<Device> findByStatus(String status,Sort sort);
	
	public List<Device> findByStatus(String status);
	
	public List<Device> findBySpid(String spid);

	public List<Device> findBySid(String sid);

	public List<Device> findByCid(String cid);

	@Query("{uid: {$regex : '^?0$', $options: 'i'},cid : ?1}")
	public List<Device> findByUidAndCid(String uid, String cid);

	public Iterable<Device> findByCidAndState(String cid, String state);

	public List<Device> findByCidAndName(String cid, String alias);

	public List<Device> findBySidAndState(String sid, String state);

	public List<Device> findBySpidAndState(String spid, String state);

	public List<Device> findBySwid(String swid);

	@Query("{status: {$in:?0}}")
	public Iterable<Device> findByStatusin(List<String> status);

	@Query("{status: {$nin:?0}}")
	public List<Device> findByConfiguredDevice(List<String> status);
	
}