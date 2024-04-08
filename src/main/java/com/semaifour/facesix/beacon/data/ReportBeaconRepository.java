package com.semaifour.facesix.beacon.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ReportBeaconRepository extends MongoRepository<ReportBeacon, String>{

	ReportBeacon findOneByMacaddr(String macaddr);

	List<ReportBeacon> findByCid(String cid);

	@Query("{macaddr:{$in:?0}}")
	List<ReportBeacon> findByMacaddrs(List<String> macaddrs);

}
