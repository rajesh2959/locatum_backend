package com.semaifour.facesix.beacon.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportBeaconService {

	private static String classname 	= ReportBeaconService.class.getName();

	Logger LOG = LoggerFactory.getLogger(classname);
	
	@Autowired(required = false)
	private ReportBeaconRepository repository;
	
	public ReportBeacon findOneByMacaddr(String tagid) {
		return repository.findOneByMacaddr(tagid);
	}

	public ReportBeacon save(ReportBeacon reportBeacon) {
		return repository.save(reportBeacon);
	}

	public List<ReportBeacon> saveList(List<ReportBeacon> reportBeaconList) {
		return repository.save(reportBeaconList);
	}

	public List<ReportBeacon> findByCid(String cid) {
		return repository.findByCid(cid);
	}

	public void delete(ReportBeacon rb) {
		repository.delete(rb);
	}
	
	public void deleteList(List<ReportBeacon> rb) {
		repository.delete(rb);
	}

	public List<ReportBeacon> findByMacaddrs(List<String> macaddrs) {
		return repository.findByMacaddrs(macaddrs);
	}

	public List<ReportBeacon> save(List<ReportBeacon> reportBeaconList) {
		return repository.save(reportBeaconList);
	}
	
	/*
	 * Create a replica of the beacon
	 */
	
	public ReportBeacon setNewReportBeacon(Beacon beacon) {
		ReportBeacon reportBeacon = new ReportBeacon();
		reportBeacon.setCid(beacon.getCid());
		reportBeacon.setSid(beacon.getSid());
		reportBeacon.setSpid(beacon.getSpid());
		reportBeacon.setReciverinfo(beacon.getReciverinfo());
		reportBeacon.setEntry_floor(beacon.getEntry_floor());
		reportBeacon.setEntry_loc(beacon.getEntry_loc());
		reportBeacon.setTagType(beacon.getTagType());
		reportBeacon.setMacaddr(beacon.getMacaddr());
		reportBeacon.setAssignedTo(beacon.getAssignedTo());
		reportBeacon.setLastSeen(beacon.getLastSeen());
		reportBeacon = save(reportBeacon);
		return reportBeacon;
	}


}
