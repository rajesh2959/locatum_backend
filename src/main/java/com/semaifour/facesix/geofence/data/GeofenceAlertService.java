package com.semaifour.facesix.geofence.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeofenceAlertService {
	
	@Autowired
	GeofenceAlertRepository repository;

	public GeofenceAlert save(GeofenceAlert geofenceAlert) {
		geofenceAlert = repository.save(geofenceAlert);
		if(geofenceAlert.getPkid() == null) {
			geofenceAlert.setPkid(geofenceAlert.getId());
			geofenceAlert = repository.save(geofenceAlert);
		}
		return geofenceAlert;
	}

	public List<GeofenceAlert> findByCid(String cid) {
		return repository.findByCid(cid);
	}
	
	public GeofenceAlert findOneById(String id) {
		return repository.findOneById(id);
	}

	public List<GeofenceAlert> findByIds(List<String> ids){
		return repository.findByIds(ids);
	}

	public void deleteList(List<GeofenceAlert> geofences) {
		repository.delete(geofences);
	}

	public List<GeofenceAlert> findByCidAndName(String cid, String name) {
		return repository.findByCidAndName(cid,name);
	}

	public List<GeofenceAlert> findByCidAndStatus(String cid, String alertStatus) {
		return repository.findByCidAndStatus(cid,alertStatus);
	}
	
	public Map<String, GeofenceAlert> getFenceAlertMap(String cid, String alertStatus) {
		List<GeofenceAlert> alertList = findByCidAndStatus(cid,alertStatus);
		if(alertList != null && !alertList.isEmpty()) {
			Map<String, GeofenceAlert> alertMap = new HashMap<String,GeofenceAlert>();
			for(GeofenceAlert alert: alertList) {
				alertMap.put(alert.getId(), alert);
			}
			return alertMap;
		}
		return null;
	}
}
