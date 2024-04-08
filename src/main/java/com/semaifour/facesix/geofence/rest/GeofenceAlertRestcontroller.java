package com.semaifour.facesix.geofence.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.geofence.data.GeofenceAlert;
import com.semaifour.facesix.geofence.data.GeofenceAlertService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/rest/geofence/alert")
public class GeofenceAlertRestcontroller {

	@Autowired
	GeofenceAlertService geofenceAlertService;

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public GeofenceAlert view(@RequestParam(value = "id", required = true) String id,
			HttpServletRequest request, HttpServletResponse response) {
		GeofenceAlert geofencealert = geofenceAlertService.findOneById(id);
		return geofencealert;
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<GeofenceAlert> list(@RequestParam(value = "cid", required = true) String cid,
			HttpServletRequest request, HttpServletResponse response) {
		List<GeofenceAlert> geofencealertlist = geofenceAlertService.findByCid(cid);
		return geofencealertlist;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Restponse<String> save(@RequestBody GeofenceAlert geofenceAlert, HttpServletRequest request,
			HttpServletResponse response) {
		boolean success = true;
		int code = 200;
		String message = "Alert Saved Successfully";
		try {
			String alertName = geofenceAlert.getName();
			String cid = geofenceAlert.getCid();
			boolean duplicate = false;
			List<GeofenceAlert> alertlist = geofenceAlertService.findByCidAndName(cid, alertName);
			if (alertlist != null && alertlist.size() > 0) {
				duplicate = true;
				if (alertlist.size() == 1 && geofenceAlert.getId() != null) {
					GeofenceAlert alert = alertlist.get(0);
					if (alert.getId().equals(geofenceAlert.getId())) {
						duplicate = false;
					}
				}
			}
			if (!duplicate) {
				String status = geofenceAlert.getStatus();
				geofenceAlert.setStatus(status.toLowerCase());
				geofenceAlert = geofenceAlertService.save(geofenceAlert);
			} else {
				success = false;
				code = 500;
				message = "Duplicate Alert Name found !!";
			}
		} catch (Exception e) {
			success =false;
			code = 500;
			message = "Failed to save alert!";
			e.printStackTrace();
		}
		return new Restponse<String>(success,code,message);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Restponse<String> delete(@RequestBody JSONObject data, HttpServletRequest request, HttpServletResponse response) {

		JSONArray geofenceAlertIds = data.getJSONArray("ids");
		String message = "Alert Deleted Successfully";
		boolean success = true;
		int code = 200;
		try {
			List<String> ids = new ArrayList<String>();
			ids.addAll(geofenceAlertIds);
			List<GeofenceAlert> geofences = geofenceAlertService.findByIds(ids);
			if (geofences.size() > 0) {
				geofenceAlertService.deleteList(geofences);
			} else {
				success = false;
				code = 500;
				message = "Alert not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			success = false;
			code = 500;
			message = "Failed to delete Alert !!";
		}
		return new Restponse<String>(success,code,message);
	}
}
