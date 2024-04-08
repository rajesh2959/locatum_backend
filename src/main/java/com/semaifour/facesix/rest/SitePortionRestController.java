package com.semaifour.facesix.rest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.semaifour.facesix.geofence.data.Geofence;
import com.semaifour.facesix.geofence.data.GeofenceService;
import com.semaifour.facesix.imageconverter.ImageConverter;
import com.semaifour.facesix.imageconverter.JpegToTiffConverter;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

/**
 * 
 * Rest Device Controller handles all rest calls
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/site/portion")
public class SitePortionRestController extends WebController {
	
	static Logger LOG = LoggerFactory.getLogger(SitePortionRestController.class.getName());
	
	@Autowired
	private SiteService siteService;
	
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
	HttpServletResponse servresponse;
	
	@Autowired
	CustomerUtils customerUtils;
	
	@Autowired
	GeofenceService geofenceService;

	private ImageConverter converter;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public @ResponseBody Restponse<Iterable<Portion>> list(@RequestParam("sid") final String sid,
			HttpServletRequest request,HttpServletResponse response) {
		if (SessionUtil.isAuthorized(request.getSession())) {
			Iterable<Portion> portion = portionService.findBySiteId(sid);
			customerUtils.resolveSite(sid, request, response);
			return new Restponse<Iterable<Portion>>(true, 200, portion);
		} else {
			return new Restponse<Iterable<Portion>>(false, 401, null);
		}
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public @ResponseBody Restponse<Portion> edit(@RequestParam("spid") String spid,HttpServletRequest request) {
		if (SessionUtil.isAuthorized(request.getSession())) {
			Portion portion = portionService.findById(spid);
			return new Restponse<Portion>(true, 200, portion);
		} else {
			return new Restponse<Portion>(false, 401, null);
		}
	}
	
	@RequestMapping(value = "/networkconfig", method = RequestMethod.GET)
	public @ResponseBody Restponse<Portion> networkconfig(@RequestParam("spid") String spid,HttpServletRequest request) {
		if (SessionUtil.isAuthorized(request.getSession())) {
			Portion portion = portionService.findById(spid);
			return new Restponse<Portion>(true, 200, portion);
		} else {
			return new Restponse<Portion>(false, 401, null);
		}
	}

	@RequestMapping("/floorview")
	public Restponse<Portion> floorview(@RequestParam("spid") String spid,HttpServletRequest request) {
		if (SessionUtil.isAuthorized(request.getSession())) {
			Portion portion = portionService.findById(spid);
			return new Restponse<Portion>(true, 200, portion);
		} else {
			return new Restponse<Portion>(false, 401, null);
		}
	}
	@RequestMapping("/geoconfig")
	public Restponse<Portion> geoconfig(@RequestParam("spid") String spid, HttpServletRequest request) {
		if (SessionUtil.isAuthorized(request.getSession())) {
			Portion portion = portionService.findById(spid);
			return new Restponse<Portion>(true, 200, portion);
		} else {
			return new Restponse<Portion>(false, 401, null);
		}
	}
	
	@RequestMapping("/multiDelete")
	public Restponse<String> multiDelete(@RequestParam("spids") String spids, HttpServletRequest request) {
		
		String body = "UnAuthorized User.";
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			body = "Floor has been deleted successfully.";
		
			List<String> floors = Arrays.asList(spids.trim().split("\\s*,\\s*"));
			
			for (String spid : floors) {
				this.delete(spid, request);
			}

			return new Restponse<String>(true, 200, body);
		} else {
			return new Restponse<String>(false, 401, body);
		}
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Restponse<String> save(@RequestParam("file") MultipartFile planFile,
								  @RequestParam("floor") String floor,
								  HttpServletRequest request, HttpServletResponse response) {
		
		String message  = "UnAuthorized User.";
		boolean success = false;
		int code		= 401;

		try {
			
			if (SessionUtil.isAuthorized(request.getSession())) {
			
			message  = "Floor has been updated successfully.";
					
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			
			Portion newfso = objectMapper.readValue(floor, Portion.class);
			String sid = newfso.getSiteId();
			
			customerUtils.resolveSite(sid, request, response);
			
			Site site=siteService.findById(sid);
			String cid = site.getCustomerId();
			
			if (newfso.getId() == null || newfso.getId().equals("0")) {
				newfso.setId(null);
				newfso.setStatus(CustomerUtils.ACTIVE());
				newfso.setCreatedBy(SessionUtil.currentUser(request.getSession()));
				newfso.setCid(cid);
				newfso.setCreatedOn(new Date());
				message  = "Floor has been created successfully.";
			} else {
				Portion portion = portionService.findById(newfso.getId());
				portion.setUid(newfso.getUid());
				portion.setDescription(newfso.getDescription());
				portion.setModifiedOn(new Date());
				portion.setModifiedBy(SessionUtil.currentUser(request.getSession()));
				newfso = portion;
			}

			newfso = portionService.save(newfso);
			
			code 	 = 200;
			success  = true;
			
			if (planFile != null && !planFile.isEmpty() && planFile.getSize() > 1) {
			
				try {
				
					Path path = Paths.get(_CCC.properties.getProperty("facesix.fileio.root", "./_FSUPLOADS_"), (newfso.getId() + "_" + planFile.getOriginalFilename()));
					Files.createDirectories(path.getParent());
					
					LOG.info( "FileName " + planFile.getOriginalFilename());
					LOG.info( "GetName " + planFile.getName());
					LOG.info( "PlanFile " + planFile.toString());
					LOG.info( "Dest " + path.toString());
					LOG.info( "PathFile " + path.getFileName());

					String name 	= planFile.getOriginalFilename().replaceAll("\\s+","_");
					String fileName = name.split("\\.")[0];
					
					Path jnipath = Paths.get(_CCC.properties.getProperty("facesix.fileio.root", "./_FSUPLOADS_"), ("geo"+newfso.getId() + "_" + fileName + ".tif"));
					
					converter = new JpegToTiffConverter(); 
					converter.convert(planFile.getInputStream(), jnipath.toString());
					
					int width  = 0;
					int height = 0;
					
					BufferedImage bimg = ImageIO.read(planFile.getInputStream());
					width      = bimg.getWidth();
					height     = bimg.getHeight();
					
					LOG.info(" geo path  " + jnipath.toString());
					LOG.info(" jni file name  " + fileName + " height " +height + " width " +width);
					
					Files.copy(planFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					newfso.setPlanFilepath(path.toString());
					newfso.setJNIFilepath(jnipath.toString());
					newfso.setHeight(height);
					newfso.setWidth(width);
					newfso = portionService.save(newfso);
					
				}  catch (IOException e) {
					message = "failed to save floor";
					success = false;
					code    = 500;
					}
				}
			}
		} catch (Exception e) {
			message = "failed.";
			success = false;
			code    = 500;
			e.printStackTrace();
		}
		
		
		return new Restponse<String>(success, code, message);
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Restponse<String> delete(@RequestParam(value = "spid", required = true) String spid,HttpServletRequest request) {
		
		boolean success = false;
		int code		= 401;
		String message  = "UnAuthorized User.";
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			try {
				
				Portion portion = portionService.findById(spid);
				
				if (portion != null) {
					
					List<Beacon> beacon = beaconService.getSavedBeaconBySpid(spid);
					
					if (beacon != null) {
						beacon.forEach(dev -> {
							beaconService.delete(dev);
						});
					}

					List<BeaconDevice> beaconDevice = beaconDeviceService.findBySpid(spid);
					
					if (beaconDevice != null) {
						beaconDevice.forEach(dev -> {
							beaconDeviceService.delete(dev);
						});
					}

					List<ClientDevice> clientDeviceList = clientDeviceService.findBySpid(spid);
					
					if (clientDeviceList != null) {
						clientDeviceList.forEach(dev -> {
							clientDeviceService.delete(dev);
						});
					}
					
					List<Device> deviceList = deviceService.findBySpid(spid);
					
					if (deviceList != null) {
						deviceList.forEach(dev -> {
							deviceService.delete(dev);
						});
					}
					
					String type = CustomerUtils.getUploadType("floor");
		
					GeoFinderLayoutData geofinder = geoFinderLayoutDataService.getSavedGeoLayoutDataBySpid(spid);

					if (geofinder != null) {
						String fileName = geofinder.getOutputFilePath();
						geoFinderLayoutDataService.delete(geofinder);
						customerUtils.removeFile(type, fileName);
					}

					List<Geofence> geofenceList = geofenceService.findBySpid(spid);
					if(geofenceList != null && geofenceList.size() > 0) {
						geofenceService.delete(geofenceList);
					}

					String floorPlan = portion.getPlanFilepath();
					customerUtils.removeFile(type, floorPlan);
					
					portionService.delete(portion);
				
					message  = "successfully deleted floor.";
					code 	 = 200;
					success  = true;
					
				} else {
					message = "portion not found.";
					success = false;
					code    = 404;
				}
			} catch (Exception e) {
				message = "failed to delete floor.";
				success = false;
				code    = 500;
			}	
		}
		

		return new Restponse<String>(success, code, message);
	}
	
    @RequestMapping(value = "conf", method = RequestMethod.GET)
    public  String confGet(@RequestParam("spid") String spid) {
    	Portion portion = portionService.findById(spid);
    	return portion.getNetworkConfigJson();
    }
    
    @RequestMapping(value = "conf", method = RequestMethod.POST)
    public  String confPost(@RequestParam(value="spid", required=true) String spid, 
    						@RequestBody String conf) {
    	
    	Portion portion = portionService.findById(spid);
    	portion.setNetworkConfigJson(conf);
    	portionService.save(portion);
		return conf;
    }
    
    @RequestMapping(value="/reset",method = RequestMethod.DELETE)
    public String resetFloorPlan(@RequestParam(value="spid", required=true) String spid,
    						     HttpServletRequest request, HttpServletResponse response) {
    	
    	String ret = "Floor Plan Deleted Failed.";
    	if (SessionUtil.isAuthorized(request.getSession())) {
    		Portion portion = portionService.findById(spid);
    		if (portion != null) {
    			
    			String jniimg 	= portion.getJNIFilepath();
    			String floorimg = portion.getPlanFilepath();
    			
    			deleteFile(jniimg);
    			deleteFile(floorimg);
    			
    			portion.setModifiedOn(new Date());
    			portion.setImageJson("");
    			portion.setPlanFilepath("");
    			portion.setJNIFilepath("");
    			portion.setHeight(0);
    			portion.setWidth(0);
    			portion = portionService.save(portion);
				ret = "Floor Plan Deleted successfully.";
			}
		} else {
			ret = "Unauthorized User.";
		}

    	return ret;
    }
    
	public void deleteFile(String path) {
		try {

			File file = new File(path);

			if (file.exists()) {
				if (file.delete()) {
					LOG.info(file.getName() + " is deleted!");
				} else {
					LOG.info("Delete operation is failed.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
    @RequestMapping(value = "/floorplan/save",method = RequestMethod.POST)
	public Restponse<String> save(
			@RequestParam(value = "file" ,		required = true) MultipartFile planFile,
			@RequestParam(value = "spid",  		required = false) String spid,
			@RequestParam(value = "sid",  		required = true) String sid,
			@RequestParam(value = "json",  		required = true) String imageJson,
			@RequestParam(value = "title",  	required = true) String uid,
			@RequestParam(value = "desc",		required = true) String desc,
			HttpServletRequest request,	  	HttpServletResponse response){
		
    	boolean success = false;
    	int code 		= 401;
    	String body 	= "UnAuthorized User";
		
		try {
			
			//if (SessionUtil.isAuthorized(request.getSession())) {
				
			LOG.info("sid" + sid + "spid" + spid + " uid " + uid + " planFile" + planFile);

				Site site = siteService.findById(sid);

				if (site == null) {
					LOG.info("Site Not found " + site);
					body	 = "Site Not found";
					success  = false;
					code	 = 404;
					return new Restponse<String>(success, code, body);
				} else {
					LOG.info("Floor plan  Site name " + site.getUid());
					
					Portion portion = null;
	
					if (StringUtils.hasText(spid)) {
						portion = portionService.findById(spid);
					}
	
					if (portion == null) {
						portion = new Portion();
						portion.setCreatedBy(SessionUtil.currentUser(request.getSession()));
						portion.setCreatedOn(new Date());
						portion.setCid(site.getCustomerId());
						portion.setSiteId(site.getId());
						portion.setStatus(CustomerUtils.ACTIVE());
					}

					portion.setUid(uid);
					portion.setDescription(desc);
					portion.setModifiedOn(new Date());
					portion.setImageJson(imageJson);
					portion = portionService.save(portion);
					
					if(!planFile.isEmpty() && planFile.getSize() > 1) {
						
						Path path = Paths.get(_CCC.properties.getProperty("facesix.fileio.root", "./_FSUPLOADS_"), (portion.getId() + "_" + planFile.getOriginalFilename()));
						Files.createDirectories(path.getParent());
					
						LOG.info( "FileName " + planFile.getOriginalFilename() + 
								" GetName " + planFile.getName() +
								" PlanFile " + planFile.toString() +
								" Dest " + 	path.toString() +
						        " PathFile " + path.getFileName());
					
						String name 	= planFile.getOriginalFilename();
						String fileName = name.split("\\.")[0];
						
						Path jnipath = Paths.get(_CCC.properties.getProperty("facesix.fileio.root", "./_FSUPLOADS_"), ("geo"+portion.getId() + "_" + fileName + ".tif"));
						
						converter = new JpegToTiffConverter(); 
						try {
							converter.convert(planFile.getInputStream(), jnipath.toString());
						} catch (Exception e) {
							body 		= "tif file conversion error";
							code 		= 500;
							success 	= false;
							e.printStackTrace();
							return new Restponse<String>(success, code, body);
	
						}
						
						int width  = 0;
						int height = 0;
						
						BufferedImage bimg = ImageIO.read(planFile.getInputStream());
						width      		   = bimg.getWidth();
						height     		   = bimg.getHeight();
						
						LOG.info(" geo path  " + jnipath.toString() +
						 " jni file name  " + fileName + " height " +height + " width " +width);
						
						Files.copy(planFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
						portion.setPlanFilepath(path.toString());
						portion.setJNIFilepath(jnipath.toString());
						portion.setHeight(height);
						portion.setWidth(width);
						portion = portionService.save(portion);
					
					}
					
					spid = portion.getId();
					
					body = "Floor has been saved successfully";
					code 	= 200;
					success = false;
					
					return new Restponse<String>(success, code, body,spid);

				}
			//}
		} catch (IOException e) {
			code = 500;
			success = false;
			body = "While save floor occurred error";
			e.printStackTrace();

		}

		return new Restponse<String>(success, code, body);
	}
    
    @RequestMapping("/open")
	public String open(@RequestParam(value = "spid", required = true) String spid, HttpServletRequest request, HttpServletResponse response) {
		
    	LOG.info("portion edit spid " +	  spid);
    	
    	Portion portion =  portionService.findById(spid);
		
		if (portion !=null) {
			String imageJson = portion.getImageJson();
			return imageJson;
		}
		return null;
		
	}
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/filter/list", method = RequestMethod.POST)
	public JSONArray list(@RequestParam(value= "cid",required = true) String cid,
			@RequestParam(value = "sid", required = false) List<String> sid) {
		JSONArray portionArray = new JSONArray();
		List<Portion> portionList = null;
		if (sid == null || sid.size() == 0) {
			portionList = portionService.findByCid(cid);
		} else {
			portionList = portionService.findBySiteIdIn(sid);
		}
		JSONObject json = null;
		for (Portion p : portionList) {
			json = new JSONObject();
			json.put("id", p.getId());
			json.put("name", p.getUid());
			portionArray.add(json);
		}
		return portionArray;
	}
   
}