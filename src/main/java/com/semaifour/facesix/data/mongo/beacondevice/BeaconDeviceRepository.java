package com.semaifour.facesix.data.mongo.beacondevice;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface BeaconDeviceRepository extends MongoRepository<BeaconDevice, String> {

	public List<BeaconDevice> findByName(String name);

	@Query("{uid: {$regex : '^?0$', $options: 'i'}}")
	public List<BeaconDevice> findByUid(String uid);

	public List<BeaconDevice> findByStatus(String status);

	public List<BeaconDevice> findBySpid(String spid);

	public List<BeaconDevice> findBySid(String sid);

	public List<BeaconDevice> findByCid(String cid);

	public BeaconDevice findByUuid(String uid);

	@Query("{uid: {$regex : '^?0$', $options: 'i'},cid : ?1}")
	public List<BeaconDevice> findByUidAndCid(String uid, String cid);

	public List<BeaconDevice> findByCidAndType(String cid, String deviceType);

	public List<BeaconDevice> findBySidAndType(String sid, String deviceType);

	public List<BeaconDevice> findBySpidAndType(String spid, String deviceType);

	public List<BeaconDevice> findByCidAndName(String cid, String alias);

	public List<BeaconDevice> findByCidAndTypeAndName(String cid, String deviceType, String alias);

	public BeaconDevice findByUidAndCidAndType(String uid, String cid, String deviceType);

	public List<BeaconDevice> findBySidAndState(String sid, String state);

	public List<BeaconDevice> findBySpidAndState(String spid, String state);

	public List<BeaconDevice> findByCidAndState(String cid, String state);

	@Query("{spid: {$in :?0},type : ?1}")
	public List<BeaconDevice> findBySpidInAndType(List<String> spid, String type);

	@Query("{sid: {$in :?0},type : ?1}")
	public List<BeaconDevice> findBySidInAndType(List<String> sid, String type);

	@Query("{uid:{$in:?0}}")
	public List<BeaconDevice> findByUids(List<String> uids);
	
	@Query("{status:{$nin:?0}}")
	public List<BeaconDevice> findByConfiguredDevice(List<String> status);

	@Query("{cid:?0,type:?1}")
	public List<BeaconDevice> findByCidAndType(String cid, String deviceType, Sort sort);

	@Query("{spid:?0}")
	public List<BeaconDevice> findBySpid(String spid, Sort sort);

	@Query("{sid:?0}")
	public List<BeaconDevice> findBySid(String sid, Sort sort);

	@Query("{cid:?0}")
	public List<BeaconDevice> findByCid(String cid, Sort sort);

}