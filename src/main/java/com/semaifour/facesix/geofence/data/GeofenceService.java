package com.semaifour.facesix.geofence.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

@Service
public class GeofenceService {

	@Autowired
	GeofenceRepository repository;

	/**
	 * This determines if the given lat and lon is within the geofence or not
	 * 
	 * @param polygon geofence object to be referred
	 * @param lat     latitude of the point
	 * @param lon     longitude of the point
	 * @return true if point lies inside else false
	 * 
	 */
	public boolean isInside(String polygonSpid,String polygonType,List<Point> points, double lat, double lon, String spid) {
		boolean result = false;
		if(!polygonSpid.equals(spid)) {
			return result;
		}
		Point test = new Point(lat, lon);
		if (polygonType.equalsIgnoreCase("circle")) {
			Point center = points.get(0);
			Point p = points.get(1);
			double radius2 = Math.pow((p.getX() - center.getX()), 2) + Math.pow((p.getY() - center.getY()), 2);
			double d2 = Math.pow(test.getX() - center.getX(), 2) - Math.pow(test.getY() - center.getY(), 2);
			if (d2 <= radius2) {
				result = !result;
			}
		} else {
			for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
				double diff = points.get(j).getY() - points.get(i).getY();
				if (points.get(i).equals(test)) {
					return true;
				}
				if (diff != 0 && (points.get(i).getY() > test.getY()) != (points.get(j).getY() > test.getY())
						&& (test.getX() < (points.get(j).getX() - points.get(i).getX())
								* (test.getY() - points.get(i).getY()) / diff + points.get(i).getX())) {
					result = !result;
				}
			}
		}
		return result;
	}

	public Geofence findOneById(String id) {
		return repository.findOneById(id);
	}

	public Geofence save(Geofence geofence) {
		geofence = repository.save(geofence);
		if(geofence.getPkid() == null) {
			geofence.setPkid(geofence.getId());
			geofence = repository.save(geofence);
		}
		return geofence;
	}

	public void delete(List<Geofence> geofences) {
		repository.delete(geofences);
	}

	public List<Geofence> findByIds(List<String> ids) {
		return repository.findByIds(ids);
	}

	public List<Geofence> findByCid(String cid) {
		return repository.findByCid(cid);
	}

	public List<Geofence> findBySid(String sid) {
		return repository.findBySid(sid);
	}

	public List<Geofence> findBySpid(String spid) {
		return repository.findBySpid(spid);
	}

	public List<Geofence> findByNameIgnoreCase(String fenceName) {
		return repository.findByNameIgnoreCase(fenceName);
	}

	public List<Geofence> findBySpidAndName(String spid, String name) {
		return repository.findBySpidAndName(spid,name);
	}

	public List<Geofence> findByCidAndStatus(String cid, String status) {
		return repository.findByCidAndStatus(cid,status);
	}

	public List<Geofence> findBySidAndStatus(String sid, String status) {
		return repository.findBySidAndStatus(sid,status);
	}

	public List<Geofence> findBySpidAndStatus(String spid, String status) {
		return repository.findBySpidAndStatus(spid,status);
	}

	public List<Geofence> findBySpidInAndStatus(List<String> spid, String status) {
		return repository.findBySpidInAndStatus(spid,status);
	}

	public List<Geofence> findBySidInAndStatus(List<String> sid, String status) {
		return repository.findBySidInAndStatus(sid,status);
	}
}
