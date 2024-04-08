package com.semaifour.facesix.rest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.account.PrivilegeService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.finder.geo.GeoFinderLayoutData;
import com.semaifour.facesix.beacon.finder.geo.GeoFinderLayoutDataService;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.ClientDevice;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.session.SessionCache;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

@RequestMapping("/rest/site")
@RestController
public class SiteRestController extends WebController{
	
	Logger LOG = LoggerFactory.getLogger(SiteRestController.class.getName());
	
	private final static int QUBER_UNAUTHORIZE_ERR_CODE = 401;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private PrivilegeService privilegeService;
	
	@Autowired
	private CustomerService customerService;

	@Autowired
	private BeaconService beaconService;

	@Autowired
	private BeaconDeviceService beaconDeviceService;

	@Autowired
	private GeoFinderLayoutDataService geoFinderLayoutDataService;

	@Autowired
	private ClientDeviceService clientDeviceService;

	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private PortionService  portionService;
	
	@Autowired
	private CustomerUtils customerUtils;
	
	@Autowired
	private SessionCache sessionCache;
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public @ResponseBody List<Site> list(@RequestParam("cid") String cid,HttpServletRequest request,HttpServletResponse respopnse) {
		
		sessionCache.clearAttribute(request.getSession(), "sid", "suid", "spid", "spuid", "cid");
		customerUtils.resolveSiteCustomer(cid, request, respopnse);
		SessionUtil.setCurrentSiteCustomerId (request.getSession(), cid);
		
		List<Site> siteList = siteService.findByCustomerId(cid);
		return siteList;
	}
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public @ResponseBody Site get(@RequestParam(value = "id", required = true) String id) {
		return siteService.findById(id);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Restponse<String> delete(@RequestParam(value = "sid", required = true) String sid,HttpServletRequest requst) {

		boolean success = false;
		int code		= 401;
		String message  = "UnAuthorized User.";
		
		if (SessionUtil.isAuthorized(requst.getSession())) {
			
			Site site = siteService.findById(sid);
			
			try {
				
				if (site != null) {
					
					List<Beacon> beacon = beaconService.getSavedBeaconBySid(sid);
					
					if (beacon != null) {
						beacon.forEach(dev -> {
							beaconService.delete(dev);
						});
					}

					List<BeaconDevice> beaconDevice = beaconDeviceService.findBySid(sid);
					
					if (beaconDevice != null) {
						beaconDevice.forEach(dev -> {
							beaconDeviceService.delete(dev);
						});
					}

					List<ClientDevice> clientDeviceList = clientDeviceService.findBySid(sid);
					
					if (clientDeviceList != null) {
						clientDeviceList.forEach(dev -> {
							clientDeviceService.delete(dev);
						});
					}
					
					List<Device> deviceList = deviceService.findBySid(sid);
					
					if (deviceList != null) {
						deviceList.forEach(dev -> {
							deviceService.delete(dev);
						});
					}
					
					String type = CustomerUtils.getUploadType("venue");
					
					List<GeoFinderLayoutData> geofinder = geoFinderLayoutDataService.findBySid(sid);

					if (geofinder != null) {
						geofinder.forEach(geo -> {
							String floorPlan = geo.getOutputFilePath();
							geoFinderLayoutDataService.delete(geo);
							customerUtils.removeFile(type, floorPlan);
						});
					}

					List<Portion> portionList = portionService.findBySiteId(sid);
					
					if (portionList != null) {
						portionList.forEach(portion -> {
							String floorPlan 	= portion.getPlanFilepath();
							String jniFileName  = portion.getJNIFilepath();
							portionService.delete(portion);
							
							customerUtils.removeFile(type, floorPlan);
							customerUtils.removeFile(type, jniFileName);
						});
					}

					siteService.delete(site);

					message  = "deleted successfully.";
					code 	 = 200;
					success  = true;
					
				} else {
					message = "site not found.";
					success = false;
					code    = 404;
				}
			} catch (Exception e) {
				message = "failed to delete site.";
				success = false;
				code    = 500;
			}
		}
		return new Restponse<String>(success, code, message);
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public @ResponseBody Site save(@RequestBody Site newfso, HttpServletRequest request, HttpServletResponse response) {
		
		if (newfso.getId() == null) {
			newfso.setCreatedOn(new Date());
			newfso.setCreatedBy(SessionUtil.currentUser(request.getSession()));
			newfso.setCustomerId(SessionUtil.getCurrentSiteCustomerId(request.getSession()));
			newfso.setStatus(CustomerUtils.ACTIVE());
		} else {
			Site site = siteService.findById(newfso.getId());
			site.setUid(newfso.getUid());
			site.setLatitude(newfso.getLatitude());
			site.setLongitude(newfso.getLongitude());
			site.setName(newfso.getName());
			site.update(newfso);
			site.setModifiedOn(new Date());
			site.setModifiedBy(SessionUtil.currentUser(request.getSession()));
			newfso = site;
		}
		
		newfso = siteService.save(newfso);
		return newfso;
	}
	
	@RequestMapping(value = "/support", method = RequestMethod.POST)
	public @ResponseBody Restponse<Site> support(@RequestBody Map<String, String> params, HttpServletRequest request, HttpServletResponse response) {
		try {
			Site site = siteService.findById(params.get("sid"));
			if (site != null) {
				site.setSupportFlag(Boolean.valueOf(params.get("flag")));
				site.setModifiedBy(whoami(request, response));
				site.setModifiedOn(now());
				siteService.save(site);
				return new Restponse<Site>(site);
			}
		} catch (Exception e) {
			LOG.error("eror updating support flag for {} ", params, e);
		}
		return new Restponse<Site>(false, QUBER_UNAUTHORIZE_ERR_CODE);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/filter/list", method = RequestMethod.GET)
	public JSONArray list(@RequestParam(value= "cid",required = true) String cid) {
		JSONArray siteArray = new JSONArray();
		List<Site> siteList = siteService.findByCustomerId(cid);
		JSONObject json = null;
		for(Site s: siteList) {
			json = new JSONObject();
			json.put("id", s.getId());
			json.put("name", s.getName());
			siteArray.add(json);
		}
		return siteArray;
	}
}
