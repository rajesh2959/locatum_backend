package com.semaifour.facesix.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.ScannerMqttMessageHandler;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.data.TagType;
import com.semaifour.facesix.beacon.data.TagTypeCacheService;
import com.semaifour.facesix.beacon.data.TagTypeService;
import com.semaifour.facesix.beacon.rest.BLENetworkDeviceRestController;
import com.semaifour.facesix.beacon.rest.BeaconDeviceRestController;
import com.semaifour.facesix.beacon.rest.GeoFinderRestController;
import com.semaifour.facesix.beacon.rest.TagTypeRestController;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.mongo.device.ClientDeviceService;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.impl.qubercloud.DeviceUpdateEventHandler;
import com.semaifour.facesix.service.NetworkConfService;
import com.semaifour.facesix.stats.ClientCache;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * Rest Device Controller handles all rest calls for network configuration
 *
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/site/portion/networkdevice")
public class NetworkConfRestController extends WebController {

	static Logger LOG = LoggerFactory.getLogger(NetworkConfRestController.class.getName());

	@Autowired
	SiteService siteService;

	@Autowired
	PortionService portionService;

	@Autowired
	DeviceService devService;

	@Autowired
	DeviceRestController deviceRestCtrl;

	@Autowired
	NetworkDeviceRestController devcontroller;

	DeviceUpdateEventHandler devupdate;

	@Autowired
	BeaconDeviceService beaconDeviceService;

	@Autowired
	BeaconDeviceRestController beaconDeviceRestController;

	ScannerMqttMessageHandler scannerMqttMessageHandler;
	
	@Autowired
	GeoFinderRestController geoFinderRestController;

	@Autowired
	BLENetworkDeviceRestController bleRestController;

	@Autowired
	BeaconService beaconService;

	@Autowired
	CustomerUtils customerUtils;

	@Autowired
	QubercommScannerRestController quberScannerRestCtr;

	@Autowired
	CustomerUtils CustomerUtils;

	@Autowired
	FSqlRestController fsqlRestController;

	@Autowired
	CustomerService customerService;

	@Autowired
	ClientDeviceService clientDeviceService;

	@Autowired
	ClientCache clientCache;

	@Autowired
	NetworkConfService networkConfService;

	@Autowired
	private TagTypeService tagTypeService;

	@Autowired
	private TagTypeCacheService tagTypeCache;

	@RequestMapping(value = "/byuid", method = RequestMethod.GET)
	public List<Device> getuid(@RequestParam("uid") String uid) {
		List<Device> list = getDeviceService().findByUid(uid);
		return list;
	}

	@RequestMapping(value = "/byspid", method = RequestMethod.GET)
	public List<Device> byspid(@RequestParam("spid") String spid) {
		List<Device> list = getDeviceService().findBySpid(spid);
		return list;
	}

	@RequestMapping(value = "/bysid", method = RequestMethod.GET)
	public List<Device> bysid(@RequestParam("sid") String sid) {
		List<Device> list = getDeviceService().findBySid(sid);
		return list;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONArray list(@RequestParam("spid") String spid) {

		JSONArray deviceList = new JSONArray();

		List<Device> devices = getDeviceService().findBySpid(spid);
		List<BeaconDevice> beaconDevices = getBeaconDeviceService().findBySpid(spid);

		if (devices != null) {
			devices.forEach(device -> {
				JSONObject object = new JSONObject();
				object.put("pkid", device.getPkid());
				object.put("uid", device.getUid());
				object.put("cid", device.getCid());
				object.put("sid", device.getSid());
				object.put("spid", device.getSpid());
				object.put("status", StringUtils.isBlank(device.getState()) ? "inactive" : device.getState());
				object.put("xposition", device.getXposition());
				object.put("yposition", device.getYposition());
				object.put("alias", device.getName());
				object.put("svid", device.getSvid());
				object.put("parent", StringUtils.isBlank(device.getParent()) ? "NA" : device.getParent());
				object.put("source", "qubercomm");
				object.put("typefs", StringUtils.isBlank(device.getTypefs()) ? "ap" : device.getTypefs());
				object.put("swid", device.getSwid());
				object.put("gparent", device.getGparent());
				deviceList.add(object);
			});
		}

		if (beaconDevices != null) {
			beaconDevices.forEach(device -> {
				JSONObject object = new JSONObject();
				object.put("pkid", device.getPkid());
				object.put("uid", device.getUid());
				object.put("cid", device.getCid());
				object.put("sid", device.getSid());
				object.put("spid", device.getSpid());
				object.put("status", StringUtils.isBlank(device.getState()) ? "inactive" : device.getState());
				object.put("xposition", device.getXposition());
				object.put("yposition", device.getYposition());
				object.put("alias", device.getName());
				object.put("parent", StringUtils.isBlank(device.getParent()) ? "NA" : device.getParent());
				object.put("svid", device.getSvid());
				object.put("gparent", device.getGparent());
				object.put("bleType", StringUtils.isBlank(device.getType()) ? BeaconDevice.GATEWAY_TYPE.receiver.name() : device.getType());
				object.put("source", device.getSource());
				object.put("typefs", StringUtils.isBlank(device.getTypefs()) ? "sensor" : device.getTypefs());

				deviceList.add(object);
			});
		}

		return deviceList;
	}

	@RequestMapping(value = "/finder/list", method = RequestMethod.GET)
	public JSONObject finderList(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "param", required = false) String param,
			@RequestParam(value = "time", required = false) Long time) throws ParseException {

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		String classname = this.getClass().getName();

		long st = System.currentTimeMillis();

		List<BeaconDevice> beaconDeviceList = getBeaconDeviceService().findBySpid(spid);

		boolean getlogs = false;
		boolean enableLogs = false;
		JSONObject info = new JSONObject();
		JSONObject jsonObj = null;
		JSONArray array = new JSONArray();
		Object tagCount = "0";
		JSONArray tagList = new JSONArray();

		try {

			String zone = "UTC";
			
			if (param != null && param.equals("1")) {
				tagList = taginfo(null, macaddr);
				tagCount = tagList.size();
			}

			for (BeaconDevice beaconDevice : beaconDeviceList) {

				cid = beaconDevice.getCid();

				String source = StringUtils.isEmpty(beaconDevice.getSource()) ? "qubercomm" : beaconDevice.getSource();

				if (!getlogs) {
					Customer customer = customerService.findById(cid);
					if (customer != null) {
						String logs = customer.getLogs();
						if (!StringUtils.isEmpty(logs)) {
							enableLogs = logs.equals("true");
						}
						zone = customer.getTimezone();
					}
					
					if (StringUtils.isEmpty(zone)) {
						zone = "UTC";
					}
					
					TimeZone timezone = customerUtils.FetchTimeZone(zone);
					format.setTimeZone(timezone);
				}
				
				getlogs = true;
				
				jsonObj = new JSONObject();
				jsonObj.put("parent", StringUtils.isBlank(beaconDevice.getParent()) ? "NA" : beaconDevice.getParent());
				jsonObj.put("typefs",   StringUtils.isBlank(beaconDevice.getTypefs()) ? "sensor" : beaconDevice.getTypefs());
				jsonObj.put("status", 	StringUtils.isBlank(beaconDevice.getState()) ? "inactive" : beaconDevice.getState());
				jsonObj.put("uid", beaconDevice.getUid());
				jsonObj.put("xposition", beaconDevice.xposition);
				jsonObj.put("yposition", beaconDevice.yposition);
				jsonObj.put("sid", beaconDevice.sid);
				jsonObj.put("spid", beaconDevice.spid);
				jsonObj.put("cid", beaconDevice.getCid());
				jsonObj.put("uuid", beaconDevice.getUid());
				
				String gatewayType = beaconDevice.getType();
				
				jsonObj.put("bleType", StringUtils.isBlank(gatewayType)  ? "RECEIVER" : gatewayType.toUpperCase());
				jsonObj.put("alias", beaconDevice.getName());
				jsonObj.put("source", source);
				array.add(jsonObj);
			}

			info.put("list", array);
			info.put("taglist", tagList);
			info.put("tagcount", tagCount);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("while device list getting error ", e.getMessage());
		}
		if (time != null) {
			long receiveTime = st - time;
			customerUtils.logs(enableLogs, classname," Time taken to receive request = "+receiveTime+" milliseconds");
		}
		long endTime = System.currentTimeMillis();
		long el = endTime - st;
		String compTime = format.format(new Date());
		long endtime = format.parse(compTime).getTime();
		//customerUtils.logs(enableLogs, classname," Time taken to give response = "+el+" milliseconds");
		info.put("endTime", endtime);
		return info;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/heatMapDeviceList", method = RequestMethod.GET)
	public JSONObject heatMap(
			 @RequestParam(value="cid",	required=false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid) {

		List<Device> list = null;
		JSONObject info = null;
		JSONObject jsonObj = null;
		JSONArray array = null;
		org.json.simple.JSONObject probObj = null;
		org.json.simple.JSONArray probArray = null;
		Hashtable<String, Integer> map = null;
		JSONArray devType_array = new JSONArray();

		info = new JSONObject();
		array = new JSONArray();
		map = new Hashtable<String, Integer>();

		String customerId = "";
		int devCount = 0;
		long associateCount = 0;
		int dups = 0;
		int android = 0;
		int windows = 0;
		int ios = 0;
		int speaker = 0;
		int printer = 0;
		int others = 0;
		long probCount = 0;
		int _2G = 0;
		int _5G = 0;

		try {

			if (spid != null && !spid.isEmpty()) {
				list = getDeviceService().findBySpid(spid);
			} else if (sid != null && !sid.isEmpty()) {
				list = getDeviceService().findBySid(sid);
			} else if (uid != null && !uid.isEmpty()) {
				list = getDeviceService().findByUid(uid);
			} else {
				list = getDeviceService().findByCid(cid);
			}

			for (Device device : list) {

				jsonObj = new JSONObject();

				customerId = device.getCid();

				String typefs = StringUtils.isBlank(device.getTypefs()) ? "ap" : device.getTypefs();

				jsonObj.put("parent", StringUtils.isBlank(device.getParent()) ? "NA" : device.getParent());
				jsonObj.put("typefs", typefs);
				jsonObj.put("status", StringUtils.isBlank(device.getState()) ? "inactive" : device.getState());
				jsonObj.put("uid", device.getUid());
				jsonObj.put("xposition", device.xposition);
				jsonObj.put("yposition", device.yposition);
				jsonObj.put("sid", device.sid);
				jsonObj.put("spid", device.spid);
				jsonObj.put("cid", customerId);
				jsonObj.put("uuid", device.getUid());
				jsonObj.put("bletype", typefs);

				probArray = new org.json.simple.JSONArray();

				devCount++;
				String devUid = device.getUid();

				String time = "5m";
				org.json.simple.JSONObject object = quberScannerRestCtr.probe_req_stats(devUid, time, null, null, null);

				if (object != null && object.containsKey("probe_req_stats")) {

					org.json.simple.JSONArray valObject = (org.json.simple.JSONArray) object.get("probe_req_stats");

					Iterator<org.json.simple.JSONObject> iter = valObject.iterator();

					while (iter.hasNext()) {

						org.json.simple.JSONObject prob = iter.next();

						String probMac = (String) prob.getOrDefault("mac_address", "NA");

						if (clientCache.findByClientMac(probMac)) {
							continue;
						}

						if (map.containsKey(probMac)) {
							dups++;
							int dupsCount = Integer.parseInt(String.valueOf(map.get(probMac))) + 1;
							map.put(probMac, dupsCount);
							continue;
						} else {

							map.put(probMac, 0);

							probObj = new org.json.simple.JSONObject();

							String channel = (String) prob.getOrDefault("channel", "NA");
							String assoc = (String) prob.getOrDefault("associated", "NA");
							Object signal = (Object) prob.getOrDefault("signal", 0);
							String devtype = (String) prob.getOrDefault("devtype", "0");

							if (devtype.equals("mac")) {
								ios++;
							} else if (devtype.equals("android")) {
								android++;
							} else if (devtype.equals("speaker")) {
								speaker++;
							} else if (devtype.equals("printer")) {
								printer++;
							} else if (devtype.equals("windows")) {
								windows++;
							} else if (devtype.equals("laptop")) {
								others++;
							}

							probObj.put("node_mac", devUid);
							probObj.put("mac_address", probMac);
							probObj.put("channel", channel);
							probObj.put("associated", assoc);
							probObj.put("signal", signal);
							probObj.put("devtype", devtype);

							probArray.add(probObj);

						}
					}
				}

					ConcurrentHashMap<String, HashMap<String, Object>> cache_map = clientCache.get_assoc_device_clients(devUid);

				if (cache_map == null || cache_map.isEmpty())
					continue;

				for (ConcurrentHashMap.Entry<String, HashMap<String, Object>> temp_map : cache_map.entrySet()) {

					associateCount++;

					String peerMac = temp_map.getKey();

					HashMap<String, Object> peerMap = temp_map.getValue();

					String devType = (String) peerMap.getOrDefault("os", "laptop");
					String radioType = (String) peerMap.getOrDefault("radio_type", "2.4Ghz");
					String ssid = (String) peerMap.getOrDefault("ssid", "UNKNOWN");

					probObj = new org.json.simple.JSONObject();

					probObj.put("node_mac", devUid);
					probObj.put("mac_address", peerMac);
					probObj.put("radio", radioType);
					probObj.put("channel", "NA");
					probObj.put("associated", "Yes");
					probObj.put("signal", "NA");
					probObj.put("devtype", devType);
					probObj.put("ssid", ssid);

					probArray.add(probObj);
				}
			}

			if (probArray != null) {
				jsonObj.put("heatmap", probArray);
			}

			array.add(jsonObj);

			DateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");

			final Date currentTime = new Date();
			Customer customer = customerService.findById(customerId);

			if (customer != null) {
				String zone = customer.getTimezone();
				if (zone != null && !zone.isEmpty()) {
					sdf.setTimeZone(TimeZone.getTimeZone(zone));
				}
			}

			String cur_date = sdf.format(currentTime);

			JSONArray dev_array1 = new JSONArray();
			JSONArray dev_array2 = new JSONArray();
			JSONArray dev_array3 = new JSONArray();
			JSONArray dev_array4 = new JSONArray();
			JSONArray dev_array5 = new JSONArray();

			JSONArray speaker_array = new JSONArray();
			JSONArray printer_array = new JSONArray();

			dev_array1.add(0, "Mac");
			dev_array1.add(1, ios);

			dev_array2.add(0, "Android");
			dev_array2.add(1, android);

			dev_array3.add(0, "Win");
			dev_array3.add(1, windows);

			speaker_array.add(0, "Speaker");
			speaker_array.add(1, speaker);

			printer_array.add(0, "Printer");
			printer_array.add(1, printer);

			dev_array4.add(0, "Others");
			dev_array4.add(1, others);

			probCount = ios + android + windows + speaker + printer + others;

			dev_array5.add(0, "Total");
			dev_array5.add(1, probCount);

			devType_array.add(0, dev_array1);
			devType_array.add(1, dev_array2);
			devType_array.add(2, dev_array3);
			devType_array.add(3, speaker_array);
			devType_array.add(4, printer_array);
			devType_array.add(5, dev_array4);
			devType_array.add(6, dev_array5);

			JSONArray chartsArray = new JSONArray();

			chartsArray.add(0, "Chart Details");
			chartsArray.add(1, probCount);
			chartsArray.add(2, cur_date);

			info.put("list", array);
			info.put("probCount", probCount);
			info.put("dupsCount", dups);
			info.put("assCount", associateCount);
			info.put("devCount", devCount);
			info.put("devType", devType_array);
			info.put("chartDetails", chartsArray);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("while heatMapDeviceList getting error ", e);
		}

		return info;
	}

	@RequestMapping(value = "taginfo", method = RequestMethod.GET)
	public JSONArray taginfo(
							@RequestParam(value="spid" ,   required=false) String spid,
			@RequestParam(value = "macaddr", required = false) String macaddr) {

		JSONArray array = new JSONArray();

		try {

			String status = "checkedout";

			List<Beacon> beaconList = null;

			if (null == spid) {
				beaconList = beaconService.getSavedBeaconByMacaddrAndStatus(macaddr, status);
			} else {
				beaconList = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
			}

			if (beaconList != null) {
				beaconList.parallelStream().forEach(beacon -> {

					String cid = beacon.getCid();
					String tagType = beacon.getTag_type();

					JSONObject tagProperties = tagTypeCache.getCacheTagTypeAttributes(cid, tagType);
					String tagIcon = (String) tagProperties.getOrDefault("tagIcon", "\uf007");

					JSONObject jsonObj = new JSONObject();

					jsonObj.put("macaddr", beacon.getMacaddr());
					jsonObj.put("floor", beacon.getLocation());
					jsonObj.put("x", beacon.getX());
					jsonObj.put("y", beacon.getY());
					jsonObj.put("state", beacon.getState());
					jsonObj.put("assignedto", beacon.getAssignedTo());
					jsonObj.put("tagType", tagType);
					jsonObj.put("tagtype", tagIcon);
					jsonObj.put("tagIcon", tagIcon);
					jsonObj.put("tagIconColor", tagProperties.getOrDefault("tagIconColor", "lime"));
					jsonObj.put("client_type", "tag");
					jsonObj.put("distance", beacon.getDistance());
					jsonObj.put("reciverId", beacon.getReciverinfo());
					jsonObj.put("location", beacon.getReciveralias());
					jsonObj.put("spid", beacon.getSpid());
					jsonObj.put("height", beacon.getHeight());
					jsonObj.put("width", beacon.getWidth());
					jsonObj.put("lastSeen", beacon.getLastSeen());
					jsonObj.put("lastReportingTime", customerUtils.formatReportDate(beacon.getLastReportingTime()));
					array.add(jsonObj);
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return array;
	}

	@RequestMapping(value = "/personinfo", method = RequestMethod.GET)
	public JSONArray  personinfo(
							@RequestParam(value="spid" ,   required=false) String spid,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "time", required = false) Long time) {
		long stTime = System.currentTimeMillis();
		if (time != null) {
			long curTime = System.currentTimeMillis() - time;
		}
		List<Beacon> beaconList = null;
		String status = "checkedout";

		if (null == spid) {
			beaconList = beaconService.getSavedBeaconByMacaddrAndStatus(macaddr, status);
		} else {
			beaconList = beaconService.getSavedBeaconBySpidAndStatus(spid, status);
		}

		JSONArray array = new JSONArray();

		if (CollectionUtils.isNotEmpty(beaconList)) {

			beaconList.parallelStream().forEach(beacon -> {

				String cid = beacon.getCid();
				String tagType = beacon.getTag_type();
				JSONObject tagProperties = tagTypeCache.getCacheTagTypeAttributes(cid, tagType);

				JSONObject jsonObj = new JSONObject();

				jsonObj.put("id", beacon.getPkid());
				jsonObj.put("macaddr", beacon.getMacaddr());
				jsonObj.put("assignedTo", beacon.getAssignedTo());
				jsonObj.put("cid", cid);
				jsonObj.put("sid", beacon.getSid());
				jsonObj.put("spid", beacon.getSpid());
				jsonObj.put("battery_level", beacon.getBattery_level());
				jsonObj.put("location", beacon.getLocation());
				jsonObj.put("x", beacon.getX());
				jsonObj.put("y", beacon.getY());
				jsonObj.put("state", beacon.getState());
				jsonObj.put("reciveralias", beacon.getReciveralias());
				jsonObj.put("width", beacon.getWidth());
				jsonObj.put("height", beacon.getHeight());
				jsonObj.put("lastactive", beacon.getLastactive());
				jsonObj.put("lastReportingTime", customerUtils.formatReportDate(beacon.getLastReportingTime()));
				jsonObj.put("tag_type", tagType);
				jsonObj.put("tagType", tagType);
				jsonObj.put("tagIcon", tagProperties.getOrDefault("tagIcon", "\uf007"));
				jsonObj.put("tagIconColor", tagProperties.getOrDefault("tagIconColor", "lime"));
				array.add(jsonObj);

			});
		}

		long elapsedTime = System.currentTimeMillis() - stTime;
		// LOG.info(" Person Info elapsed time "+elapsedTime);
		return array;
	}

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public JSONObject view(@RequestParam("sid") String sid) throws IOException {

		JSONObject venue = new JSONObject();

		try {

			JSONArray flr_array = new JSONArray();
			JSONObject floor = null;
			JSONObject flrdev = null;
			JSONArray dev_array = null;
			JSONObject dev = null;

			Site site = siteService.findById(sid);
			String siteName = "venue";

			if (site != null) {
				siteName = site.getUid();
			}

			venue.put("name", siteName);
			venue.put("sid", sid);

			List<Portion> portionLIst = portionService.findBySiteId(sid);

			if (portionLIst != null) {

				for (Portion portion : portionLIst) {

					String spid = portion.getId();

					List<BeaconDevice> beaconDevice = getBeaconDeviceService().findBySpid(spid);
					List<Device> devices = getDeviceService().findBySpid(spid);

					floor = new JSONObject();
					flrdev = new JSONObject();

					flrdev.put("name", portion.getUid());
					flrdev.put("spid", spid);

					dev_array = new JSONArray();

					if (devices != null) {
						for (Device device : devices) {
							dev = new JSONObject();

							String source = "qubercomm";

							dev.put("id", device.getId());
							dev.put("uid", device.getUid());
							dev.put("typefs", StringUtils.isBlank(device.getTypefs()) ? "ap" : device.getTypefs());
							dev.put("source", source);
							dev.put("alias", device.getName());
							dev.put("parent", StringUtils.isBlank(device.getParent()) ? "NA" : device.getParent());
							dev.put("xposition", device.getXposition());
							dev.put("yposition", device.getYposition());
							dev.put("spid", spid);
							dev.put("sid", sid);
							dev.put("status", (device.getState() == null) ? "inactive" : device.getState());

							dev_array.add(dev);
						}
					}
					if (beaconDevice != null) {
						for (BeaconDevice beaconDev : beaconDevice) {

							dev = new JSONObject();

							String source = beaconDev.getSource() == null ? "qubercomm" : beaconDev.getSource();

							dev.put("id", beaconDev.getId());
							dev.put("uid", beaconDev.getUid());
							dev.put("typefs", StringUtils.isBlank(beaconDev.getTypefs()) ? "sensor" : beaconDev.getTypefs());
							dev.put("source", source);
							dev.put("alias", beaconDev.getName());
							dev.put("parent", StringUtils.isBlank(beaconDev.getParent()) ? "NA" : beaconDev.getParent());
							dev.put("xposition", beaconDev.getXposition());
							dev.put("yposition", beaconDev.getYposition());
							dev.put("spid", spid);
							dev.put("sid", sid);
							dev.put("status", (beaconDev.getState() == null) ? "inactive" : beaconDev.getState());

							dev_array.add(dev);
						}
					}

					flrdev.put("devices", dev_array);
					floor.put("floor", flrdev);
					flr_array.add(floor);
				}
			}

			if (floor != null) {
				// flr_array.add(floor);
				venue.put("floors", flr_array);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return venue;
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Device confGet(@RequestParam("uid") String uid) {
		Device nd = getDeviceService().findOneByUid(uid);
		return nd;
	}

	@RequestMapping(value = "/getid", method = RequestMethod.GET)
	public Device getid(@RequestParam("id") String id) {
		Device nd = getDeviceService().findById(id);
		return nd;
	}

	public void deletesid(String sid) {
		Device device = null;
		List<Device> list = getDeviceService().findBySid(sid);
		Iterator<Device> iterator = list.iterator();

		while (iterator.hasNext()) {
			device = iterator.next();
			if (device != null) {
				getDeviceService().delete(device);
			}
		}
	}

	public void deletespid(String spid) {
		List<Device> list = getDeviceService().findBySpid(spid);
		Iterator<Device> iterator = list.iterator();

		while (iterator.hasNext()) {
			Device device = iterator.next();
			if (device != null) {
				getDeviceService().delete(device);

			}
		}
	}

	/**
	 * Delete by its 'id'.
	 *
	 * @param id
	 * @return
	 */

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Restponse<String>  delete(@RequestParam("spid") String spid, @RequestParam("uid") String uid ,@RequestParam("type") String type ) throws Exception {

		int code = 200;
		boolean success = true;
		String body = "Successfully deleted device";

		try {

			boolean isAvilable = false;

			Device device = getDeviceService().findOneByUid(uid);

			String[] stringArray = new String[] { "none" };

			if (device != null) {
				deviceRestCtrl.rpc(uid, null, null, "DELETE", stringArray);
				devService.delete(device);
				isAvilable = true;
			}

			if (type.equals("sensor") || type.equals("server")) {

				BeaconDevice beaconDevice = getBeaconDeviceService().findOneByUid(uid);

				if (beaconDevice != null) {
					String cid = beaconDevice.getCid();
					String bleType = beaconDevice.getType();
					beaconDeviceService.resetServerIP(bleType, cid);
					getBeaconDeviceService().delete(beaconDevice);
					isAvilable = true;
				}
			}

			List<Device> deviceList = getDeviceService().findBySpid(spid);
			if (type.equals("ap")) {
				for (Device dev : deviceList) { // AP Child BLE Device delete
					String parent = dev.parent == null ? "NA" : dev.parent;
					if (parent.equals(uid)) { //
						getDeviceService().delete(dev);
					}
				}
			}

			List<Device> list = getDeviceService().findBySpid(spid);
			Iterator<Device> iterator = list.iterator();

			String id = uid;
			if (!type.equals("ap")) {
				id = uid.replaceAll("[^a-zA-Z0-9]", "");
			}

			while (iterator.hasNext()) {

				Device devices = iterator.next();
				String nd_uid = devices.getUid();
				String nd_svid = devices.svid;
				String nd_swid = devices.swid;

				if (type.equals("ap")) {
					if (nd_uid.equals(uid)) {
						getDeviceService().delete(devices);
						return new Restponse<String>(success, code, body);
					}

				}

				if (nd_svid.equals(id) || nd_swid.equals(id)) {
					device = getDeviceService().findOneByUid(nd_uid);
					if (device != null) {
						deviceRestCtrl.rpc(nd_uid, null, null, "DELETE", stringArray);
						devService.delete(device);
						device.setId(null);
					}

				}

			}

			if (!isAvilable) {
				code = 404;
				success = false;
				body = "Device not found";
			}

		} catch (Exception e) {
			success = false;
			code = 500;
			body = "an error occurred while deleting device " + e.getMessage();
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);

	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public JSONObject update(@RequestBody String newfso) throws Exception {

		JSONObject json = JSONObject.fromObject(newfso);
		String spid = (String) json.get("spid");
		String xposition = (String) json.get("xposition");
		String yposition = (String) json.get("yposition");
		String uid = (String) json.get("uid");
		String type = (String) json.getOrDefault("type", "NA");
		String parent = (String) json.getOrDefault("parent", "NA");

		// LOG.info("json " +json.toString());

		try {

			if (parent.equalsIgnoreCase("ble") || type.equalsIgnoreCase("sensor")) {

				BeaconDevice beacondevice = getBeaconDeviceService().findOneByUid(uid);

				if (beacondevice != null) {

					beacondevice.setXposition(xposition);
					beacondevice.setYposition(yposition);
					beacondevice.setModifiedOn(new Date(System.currentTimeMillis()));
					beacondevice.setModifiedBy("cloud");
					getBeaconDeviceService().save(beacondevice, false);

					geoFinderRestController.Pixel2Coordinate(spid, uid, xposition, yposition);
				}
			}

			Device device = getDeviceService().findOneByUid(uid);

			if (device != null) {
				device.setXposition(xposition);
				device.setYposition(yposition);
				device.setModifiedOn(new Date(System.currentTimeMillis()));
				device.setModifiedBy("cloud");
				getDeviceService().save(device, false);
			}

		} catch (Exception e) {
			LOG.info("while reposition error ", e);
		}

		return json;
	}

	@RequestMapping(value = "/blesave", method = RequestMethod.POST)
	public void blesave(
			@RequestParam(value = "uuid", required = true) String uid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "conf", required = true) String conf,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "param", required = false) String param,
			@RequestParam(value = "source", required = false) String source,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String str = "/facesix/web/finder/device/list?cid=" + cid + "&sid=" + sid + "&spid=" + spid;

		if (SessionUtil.isAuthorized(request.getSession())) {

			if (param.equals("DeviceConfig")) {
				str = "/facesix/web/finder/device/list?cid=" + cid + "&sid=" + sid + "&spid=" + spid;
			} else {
				str = "/facesix/web/site/portion/nwcfg?sid=" + sid + "&spid=" + spid + "&uid=?&cid=" + cid;
			}

			String cur_user = SessionUtil.currentUser(request.getSession());
			LOG.info("Param " + param);
			LOG.info("URL  " + str);
			try {

				String bleType = bleType(conf, "type");
				String keepAliveInterval = bleType(conf, "keepaliveinterval");
				String tlu = "0";
				double tluinterval = 0;
				if (source == null || source.isEmpty()) {
					source = "qubercomm";
				}

				if (source.equals("qubercomm")) {
					tlu = bleType(conf, "tluinterval");
					tluinterval = Double.parseDouble(tlu);
				}

				net.sf.json.JSONObject template = net.sf.json.JSONObject.fromObject(conf);

				BeaconDevice beacondevice = null;
				beacondevice = getBeaconDeviceService().findOneByUid(uid);

				if (param.equals("DeviceConfig") || StringUtils.isBlank(spid)) {
					if (beacondevice == null) {
						LOG.info("beacondevice save >>>>");
						beacondevice = new BeaconDevice();
						beacondevice.setCreatedBy(cur_user);
						beacondevice.setUid(uid);
						beacondevice.setCid(cid);
						beacondevice.setName(name);
						beacondevice.setStatus(BeaconDevice.STATUS.AUTOCONFIGURED.name());
						beacondevice.setState("inactive");
						beacondevice.setTemplate(template.toString());
						beacondevice.setConf(template.toString());
						beacondevice.setModifiedBy(cur_user);
						beacondevice.setType(bleType);
						beacondevice.setIp("0.0.0.0");
						beacondevice.setKeepAliveInterval(keepAliveInterval);
						beacondevice.setTlu(tluinterval);
						beacondevice.setSource(source);
						beacondevice.setTypefs("sensor");
						beacondevice = getBeaconDeviceService().save(beacondevice, true);
					} else {

						LOG.info("beacondevice update>>>>>>");

						String isConfigured = beacondevice.getStatus();
						if (!isConfigured.equalsIgnoreCase("CONFIGURED")) {
							beacondevice.setCid(cid);
						}
						beacondevice.setTypefs("sensor");
						beacondevice.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
						beacondevice.setName(name);
						beacondevice.setTemplate(template.toString());
						beacondevice.setConf(template.toString());
						beacondevice.setType(bleType); // BLE type scanner or receiver or server
						beacondevice.setKeepAliveInterval(keepAliveInterval);
						beacondevice.setTlu(tluinterval);
						beacondevice.setSource(source);
						beacondevice.setModifiedBy(cur_user);
						beacondevice.setModifiedOn(new Date(System.currentTimeMillis()));
						beacondevice = getBeaconDeviceService().save(beacondevice, true);

					}
				} else if (StringUtils.isNotBlank(sid) && StringUtils.isNotBlank(spid)) {

					// case : 1 device null should be save
					// case : 2 device not null but status REGISTERED should be save
					// case : 3 device not null update the device config

					boolean isSave = false;

					if (beacondevice == null) {
						isSave = true;
					}

					if (beacondevice != null) {
						if (beacondevice.getStatus().equals(BeaconDevice.STATUS.REGISTERED.name())
								&& StringUtils.isBlank(beacondevice.getSpid())) {
							isSave = true;
						} else if (!beacondevice.getStatus().equals(BeaconDevice.STATUS.REGISTERED.name())
									&& StringUtils.isBlank(beacondevice.getSpid()) && StringUtils.isNotBlank(beacondevice.getCid())) {
							isSave = true;
						}
					}

					if (isSave) {

						String json = request.getSession().getAttribute("json").toString();
						JSONObject beacondetails = JSONObject.fromObject(json);
						String type = (String) beacondetails.get("type");

						if (type.equals("ble")) {
							type = "sensor";
						} else if (type.equals("bleserver")) {
							type = "server";
						}

						// LOG.info("JSON tYPE>>>>>>>>>>>>>>>>>>.. " +beacondetails.get("type"));
						// LOG.info("beacondetails TYPE >>>>>>>>>>>>>>>>>>. " +type);
						beacondetails.put("tluinterval", tluinterval);
						beacondetails.put("keepAliveInterval", keepAliveInterval);
						beacondetails.put("type", type);
						beacondetails.put("sensorFlag", "1");
						beacondetails.put("alias", name);
						beacondetails.put("json", conf);
						beacondetails.put("source", source);
						this.save(beacondetails.toString(), request, response);

					} else if (beacondevice != null && StringUtils.isNotBlank(beacondevice.getSpid())) {

						beacondevice.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
						beacondevice.setName(name);
						beacondevice.setTemplate(template.toString());
						beacondevice.setConf(template.toString());
						beacondevice.setType(bleType); // BLE type scanner or receiver or server
						beacondevice.setKeepAliveInterval(keepAliveInterval);
						beacondevice.setTlu(tluinterval);
						beacondevice.setSource(source);

						if (beacondevice.getSid() == null || beacondevice.getSid().isEmpty()) {
							beacondevice.setSid(sid);
						}
						if (beacondevice.getSpid() == null || beacondevice.getSpid().isEmpty()) {
							beacondevice.setSpid(spid);
						}

						beacondevice.setModifiedBy(cur_user);
						beacondevice.setModifiedOn(new Date(System.currentTimeMillis()));
						beacondevice = getBeaconDeviceService().save(beacondevice, true);
					}
				}

				response.sendRedirect(str);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String bleType(String conf, String type) {

		String value = "";

		net.sf.json.JSONObject template = net.sf.json.JSONObject.fromObject(conf);
		JSONArray jsonArray = new JSONArray();
		net.sf.json.JSONObject jsonObject = new JSONObject();

		if (template.get("attributes") != null) {
			jsonArray = template.getJSONArray("attributes");
			if (jsonArray != null && jsonArray.size() > 0) {
				jsonObject = jsonArray.getJSONObject(0);
			}
		}

		if (jsonObject.get(type) != null) {
			value = jsonObject.getString(type);
		}
		// LOG.info("value " +value);
		return value;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Restponse<String> save(@RequestBody String newfso, HttpServletRequest request, HttpServletResponse response) throws Exception {

	  JSONObject json 	= JSONObject.fromObject(newfso);
		
		int code 		= 401;
		boolean success = false;
		String body 	= "Unauthorized user";
		
	if (SessionUtil.isAuthorized(request.getSession())) {

	  	 code 		= 200;
		 success = true;
		 body 	= "Successfully saved device";
		
		try {
			
		String cur_user = SessionUtil.currentUser(request.getSession());

		String parent 		= (String) json.get("parent");
		String sid 			= (String) json.get("sid");
		String spid 		= (String) json.get("spid");
		String xposition 	= (String) json.get("xposition");
		String yposition 	= (String) json.get("yposition");
		String uid 			= (String) json.get("uid");
		String type 		= (String) json.get("type");
		String gparent 		= (String) json.get("gparent");
		String cid          = (String)json.get("cid");
		String svid 		= "";
		String swid 		= "";
		String source 		= json.containsKey("source")?(String)json.getString("source"):"qubercomm";
		String keepAliveInterval = (String) json.get("keepAliveInterval");

		if (StringUtils.isEmpty(cid)) {
			Portion portion = portionService.findById(spid);
			cid = portion.getCid();
		}

		int dup = 0;

		request.getSession().setAttribute("json", json);
		
		String apFlag 			= json.containsKey("apFlag") ? (String) json.get("apFlag") : "0";
		String alias 			= json.containsKey("alias") ? (String) json.get("alias") :    "Device";
		int network_balancer 	= json.containsKey("network_balancer") ? (int) json.get("network_balancer") : 0;
		String sensorFlag 		= json.containsKey("sensorFlag") ? (String) json.get("sensorFlag") : "0";
		double tlu 				= json.containsKey("tluinterval") ? Double.parseDouble(json.getString("tluinterval")) : 3;
		String workingMode 		= json.containsKey("workingMode") ? (String) json.get("workingMode") : "normalmode";
		String root 			= json.containsKey("root") ? (String) json.get("root") : "no";

		/*
		 * LAN and WAN config
		 */
		
		org.json.simple.JSONObject networkSetting = new org.json.simple.JSONObject();

		if (json.containsKey("network_setting")) {
			String strNetworkSetting = json.get("network_setting").toString();
			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject setting = (org.json.simple.JSONObject) parser.parse(strNetworkSetting);
			networkSetting.put("network_setting", setting);
		}

		if (json.containsKey("lan_bridge")) {
			String lan_bridge = (String) json.get("lan_bridge");
			networkSetting.put("lan_bridge", lan_bridge);
		}
		if (json.containsKey("lan_bridge")) {
			String wan_bridge = (String) json.get("wan_bridge");
			networkSetting.put("wan_bridge", wan_bridge);
		}

		Iterable<BeaconDevice> beaconDeviceList = getBeaconDeviceService().findAll();
		Iterator<BeaconDevice> beaconDeviceIter = beaconDeviceList.iterator();

		Iterable<Device> deviceList = getDeviceService().findAll();
		Iterator<Device> deviceIter = deviceList.iterator();

		String deviceStatus = Device.STATUS.REGISTERED.name();
			
			Device dev = getDeviceService().findOneByUid(uid);

			if (dev != null) {
				
				LOG.info("dev " +dev.getStatus());
				
				if (deviceStatus.equalsIgnoreCase(dev.getStatus()) && type.equals("ap")  && apFlag.equals("1")) {

					while (deviceIter.hasNext()) {
						Device nd_dup = deviceIter.next();
						String nd_uid = nd_dup.getUid();

						if ((type.equals("ap") && parent.equals(nd_uid))) {
							swid = parent.replaceAll("[^a-zA-Z0-9]", "");
							svid = gparent.replaceAll("[^a-zA-Z0-9]", "");
							break;
						}

						if ((type.equals("sensor") && parent.equals(nd_uid))) {
							swid = gparent.replaceAll("[^a-zA-Z0-9]", "");
							break;
						}
					}

					String conf = null;
					if (json.containsKey("json")) {
						conf = json.getString("json");
					}

					dev.setName(alias);
					dev.setCid(cid);
					dev.setSid(sid);
					dev.setSpid(spid);
					dev.setStatus(Device.STATUS.CONFIGURED.name());
					dev.setState("inactive");
					dev.setTypefs(type);
					dev.setParent(parent);
					dev.setGparent(gparent);
					dev.setSvid(svid);
					dev.setSwid(swid);
					dev.setXposition(xposition);
					dev.setYposition(yposition);
					
					dev.setConf(conf);
					dev.setTemplate(conf);
					dev.setKeepAliveInterval(keepAliveInterval);
					dev.setRoot(root);
					dev.setNetwork_balancer(network_balancer);
					
					dev.setWorkingMode(workingMode);

					/*
					 * LAN and WAN config
					 */
					
					networkConfService.updateLanWanConfig(dev, networkSetting);
					
					dev.setModifiedBy(cur_user);
					
					getDeviceService().saveAndSendMqtt(dev,true);
					
					return new Restponse<String>(success, code,body);
				}
			}

		/**
		 * Nmesh Device duplicate validation
		 */
		
		while (deviceIter.hasNext()) {
			
			Device nmeshDevice  = deviceIter.next();
			
			String devUid  = nmeshDevice.getUid();
			String devSid  = nmeshDevice.getSid();
			String devSpid = nmeshDevice.getSpid();
			
			/*LOG.info("Nmesh devSid  " +devSid);
			LOG.info("Nmesh devSpid " +devSpid);
			LOG.info("Nmesh devSpid " +devUid);
			LOG.info("Nmesh devSpid " +nmeshDevice.getStatus());*/
			
			// case :1 skip REGISTERED device
			// case :2 skip auto,configured && (sid  or spid null)
			// case :3 status not equals and (sid or spid) not null

				if (devUid.equalsIgnoreCase(uid)) {
					if (deviceStatus.equals(nmeshDevice.getStatus())) {
						dup = 0;
						LOG.info("REGISTERED device skiped duplicate " +uid);
						break;
					} else if (!deviceStatus.equals(nmeshDevice.getStatus())
							&& (StringUtils.isBlank(devSid) || StringUtils.isBlank(devSpid))) {
						LOG.info("Not REGISTERED device but sid or spid is null skiped duplicate " +uid);
						dup = 0;
						break;
					} else if (!deviceStatus.equals(nmeshDevice.getStatus())
							&& (StringUtils.isNotBlank(devSid) || StringUtils.isNotBlank(devSpid))) {
						LOG.info("Not REGISTERED device but sid or spid is not null duplicate " +uid);
						dup = 1;
						break;
					} else {
						dup = 0;
					}
				}
			}
		
		/**
		 * Locatum Device duplicate validation 
		 */
		
		while (beaconDeviceIter.hasNext()) {
			
			BeaconDevice beaconDev  = beaconDeviceIter.next();
			
			String devUid  = beaconDev.getUid();
			String devSid  = beaconDev.getSid();
			String devSpid = beaconDev.getSpid();
			
		/*	LOG.info("Locatum devSid  " +devSid);
			LOG.info("Locatum devSpid " +devSpid);
			LOG.info("Locatum devSpid " +devUid);
			LOG.info("Locatum devSpid " +beaconDev.getStatus());
			*/
			// case :1 skip REGISTERED device
			// case :2 skip auto,configured && (sid or spid null)
			// case :3 status not equals and (sid or spid) not null

				if (devUid.equalsIgnoreCase(uid)) {
					if (deviceStatus.equals(beaconDev.getStatus())) {
						dup = 0;
						LOG.info("REGISTERED device skiped duplicate " +uid);
						break;
					} else if (!deviceStatus.equals(beaconDev.getStatus())
							&& (StringUtils.isBlank(devSid) || StringUtils.isBlank(devSpid))) {
						LOG.info("Not REGISTERED device but sid or spid is null skiped duplicate " +uid);
						dup = 0;
						break;
					} else if (!deviceStatus.equals(beaconDev.getStatus())
							&& (StringUtils.isNotBlank(devSid) || StringUtils.isNotBlank(devSpid))) {
						LOG.info("Not REGISTERED device but sid or spid is not null duplicate " +uid);
						dup = 1;
						break;
					} else {
						dup = 0;
					}
				}
			}

			if (dup == 1) {
				LOG.info("Duplicate MAC Found + UID:" + uid);
				code 	= 422;
				success = false;
				body 	= "Duplicate device given Uid " + uid;
				return new Restponse<String>(success, code, body);
			}

			if (type.equals("server")) {
				svid = uid.replaceAll("[^a-zA-Z0-9]", "");
			}

			deviceList 	 = getDeviceService().findBySpid(spid);
			deviceIter = deviceList.iterator();

			while (deviceIter.hasNext()) {
				
				Device nd_dup = deviceIter.next();
				String nd_uid = nd_dup.getUid();

				if (type.equals("switch") && parent.equals(nd_uid)) {
					swid = uid.replaceAll("[^a-zA-Z0-9]", "");
					svid = parent.replaceAll("[^a-zA-Z0-9]", "");
					break;
				}

				if (type.equals("ap") && parent.equals(nd_uid)) {
					swid = parent.replaceAll("[^a-zA-Z0-9]", "");
					svid = gparent.replaceAll("[^a-zA-Z0-9]", "");
					break;
				}

				if (type.equals("sensor") && parent.equals(nd_uid)) {
					swid = gparent.replaceAll("[^a-zA-Z0-9]", "");
					break;
				}
			}
			
		if (apFlag.equals("1") && type.equals("ap")) {

				Device device = getDeviceService().findOneByUid(uid);

				if (device == null) {
					device = new Device();
					device.setUid(uid);
					device.setCreatedBy(cur_user);
					device.setCreatedOn(new Date(System.currentTimeMillis()));
					device.setIp("0.0.0.0");
					device.setStatus(Device.STATUS.AUTOCONFIGURED.name());
					device.setState("inactive");
				}
				
				String conf = null;
				if (json.containsKey("json")) {
					conf = json.getString("json");
				}
				
				device.setParent(parent);
				device.setGparent(gparent);
				device.setSvid(svid);
				device.setSwid(swid);
				device.setXposition(xposition);
				device.setYposition(yposition);
				
				device.setTypefs(type);
				device.setName(alias);
				device.setCid(cid);
				device.setSid(sid);
				device.setSpid(spid);
				device.setConf(conf);
				device.setTemplate(conf);
				
				device.setKeepAliveInterval(keepAliveInterval);
				device.setRoot(root);
				device.setNetwork_balancer(network_balancer);
				device.setWorkingMode(workingMode);
				device.setStatus(Device.STATUS.CONFIGURED.name());

				/*
				 * LAN and WAN config 
				 */
				
				networkConfService.updateLanWanConfig(device, networkSetting);
				
				device = getDeviceService().saveAndSendMqtt(device, true);
			
		} else if (!sensorFlag.equals("0") && type.equals("server") || type.equals("sensor")) {
			
				if (type.equals("server")) {
					svid = uid.replaceAll("[^a-zA-Z0-9]", "");
				}

				beaconDeviceList 	 = getBeaconDeviceService().findBySpid(spid);
				beaconDeviceIter 	 = beaconDeviceList.iterator();

				while (beaconDeviceIter.hasNext()) {
					
					BeaconDevice nd_dup = beaconDeviceIter.next();
					String nd_uid = nd_dup.getUid();

					if (type.equals("switch") && parent.equals(nd_uid)) {
						swid = uid.replaceAll("[^a-zA-Z0-9]", "");
						svid = parent.replaceAll("[^a-zA-Z0-9]", "");
						break;
					}

					if (type.equals("ap") && parent.equals(nd_uid)) {
						swid = parent.replaceAll("[^a-zA-Z0-9]", "");
						svid = gparent.replaceAll("[^a-zA-Z0-9]", "");
						break;
					}

					if (type.equals("sensor") && parent.equals(nd_uid)) {
						swid = gparent.replaceAll("[^a-zA-Z0-9]", "");
						break;
					}
				}
				
				if (sensorFlag.equals("1")) {
					
					BeaconDevice beacondevice 	    = getBeaconDeviceService().findOneByUid(uid);
					net.sf.json.JSONObject template = net.sf.json.JSONObject.fromObject(json.get("json"));
					
					String bleType = bleType(template.toString(),"type");
					
					if (type.equals("server")) {

						if (beacondevice == null) {
							beacondevice = new BeaconDevice();
							beacondevice.setUid(uid);
							beacondevice.setName(alias);
							beacondevice.setTypefs(type);
							beacondevice.setCreatedOn(now());
							beacondevice.setStatus(BeaconDevice.STATUS.AUTOCONFIGURED.name());
							beacondevice.setIp("0.0.0.0");
							beacondevice.setType(bleType);
							beacondevice.setSource(source);
							beacondevice.setState("active");
						}

						beacondevice.setTemplate(String.valueOf(template));
						beacondevice.setConf(String.valueOf(template));
						beacondevice.setName(alias);
						beacondevice.setParent(parent);
						beacondevice.setGparent(gparent);
						beacondevice.setSvid(svid);
						beacondevice.setSwid(swid);
						beacondevice.setXposition(xposition);
						beacondevice.setYposition(yposition);
						beacondevice.setTemplate(template.toString());
						beacondevice.setConf(template.toString());
						beacondevice.setCid(cid);
						beacondevice.setSid(sid);
						beacondevice.setSpid(spid);
						beacondevice.setName(alias);
						beacondevice.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
						beacondevice.setState("active");
						beacondevice.setType(bleType);
						beacondevice.setModifiedBy(cur_user);
						beacondevice.setKeepAliveInterval(keepAliveInterval);
						beacondevice.setTlu(tlu);
						beacondevice.setSource(source);
						beacondevice.setTypefs(type);
						beacondevice = getBeaconDeviceService().save(beacondevice, true);
						
						geoFinderRestController.Pixel2Coordinate(spid, uid, xposition, yposition);
						
					} else if (type.equals("sensor")) {
						
						if (beacondevice == null) {
							beacondevice = new BeaconDevice();
							beacondevice.setUid(uid);
							beacondevice.setName(alias);
							beacondevice.setTypefs(type);
							beacondevice.setCreatedOn(now());
							beacondevice.setStatus(BeaconDevice.STATUS.AUTOCONFIGURED.name());
							beacondevice.setIp("0.0.0.0");
							beacondevice.setType(bleType);
							beacondevice.setSource(source);
							beacondevice.setState("active");
						}

						beacondevice.setTemplate(String.valueOf(template));
						beacondevice.setConf(String.valueOf(template));
						beacondevice.setParent(parent);
						beacondevice.setGparent(gparent);
						beacondevice.setSvid(svid);
						beacondevice.setSwid(swid);
						beacondevice.setXposition(xposition);
						beacondevice.setYposition(yposition);
						beacondevice.setCid(cid);
						beacondevice.setSid(sid);
						beacondevice.setSpid(spid);
						beacondevice.setName(alias);
						beacondevice.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
						beacondevice.setState("active");
						beacondevice.setType(bleType);
						beacondevice.setModifiedBy(cur_user);
						beacondevice.setKeepAliveInterval(keepAliveInterval);
						beacondevice.setTlu(tlu);
						beacondevice.setSource(source);
						beacondevice.setTypefs(type);
						beacondevice = getBeaconDeviceService().save(beacondevice, true);
						
						geoFinderRestController.Pixel2Coordinate(spid, uid, xposition, yposition);
						
					}
				}
			} else if(type.equals("server") || type.equals("switch")) {
				LOG.info("N-Mesh device  " + type + " added " +uid);
				Device nd = new Device();
				nd.setCreatedOn(new Date());
				nd.setCreatedBy(SessionUtil.currentUser(request.getSession()));
				nd.setModifiedOn(new Date());
				nd.setModifiedBy(nd.getCreatedBy());
				nd.setParent(parent);
				nd.setGparent(gparent);
				nd.setCid(cid);
				nd.setSid(sid);
				nd.setSpid(spid);
				nd.setSvid(svid);
				nd.setSwid(swid);
				nd.setXposition(xposition);
				nd.setYposition(yposition);
				nd.setUid(uid);
				nd.setTypefs(type);
				nd.setState("inactive");
				nd.setStatus(Device.STATUS.CONFIGURED.name());
				nd = getDeviceService().save(nd);
			}

		}catch(Exception e) {
			success = false;
			code 	= 500;
			body 	= "an error occurred while deleting device " +e.getMessage();
			LOG.info("While saving REGISTERED AP error",e);
		}

	}return new Restponse<String>(success,code,body);

	}

	@RequestMapping(value = "/exportJSONConfig", method = RequestMethod.GET)
	public String exportJSONConfig(@RequestParam(value = "uid", required = true) String uid,
			@RequestParam(value = "conf", required = true) String conf, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String fileName = "./uploads/devconfig.txt";

		FileWriter file = null;
		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;

		try {

			if (conf != null) {

				file = new FileWriter(fileName);
				file.write(conf);
				file.flush();

				File pdfFile = new File(fileName);
				response.setContentType("text/plain");
				response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
				response.setContentLength((int) pdfFile.length());

				fileInputStream = new FileInputStream(pdfFile);
				responseOutputStream = response.getOutputStream();

				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fileInputStream.close();
			responseOutputStream.close();
			file.close();
		}

		return fileName;
	}

	@RequestMapping(value = "/isDuplicateDevice", method = RequestMethod.GET)
	public String isDuplicateDevice(@RequestParam(value = "uid", required = true) String uid) {

		Iterable<Device> list = getDeviceService().findAll();
		Iterator<Device> iterator = list.iterator();
		Device device = null;
		int dup = 0;
		while (iterator.hasNext()) {
			device = iterator.next();
			String device_uid = device.getUid();
			if (device_uid.toLowerCase().equals(uid.toLowerCase())) {
				dup = 1;
			}
		}
		if (dup == 1) {
			LOG.info(" Device Duplicate MAC Found + UID:" + uid);
			return "found";
		}
		return "notfound";
	}

	/**
	 * used to save the device
	 * 
	 * @param json
	 */

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "ibeaconsave", method = RequestMethod.POST)
	public Restponse<String> beaconSave(@RequestBody String newfso, HttpServletRequest request, HttpServletResponse response) {

		String body = "UnAuthorized User";
		boolean success = false;
		int code = 401;

		// if (SessionUtil.isAuthorized(request.getSession())) {

		try {

			LOG.info("ibeaconsave payload " + newfso);

			String cur_user = SessionUtil.currentUser(request.getSession());

			JSONObject json = JSONObject.fromObject(newfso);

			String uid = (String) json.get("uid");
			String param = (String) json.getOrDefault("param", "NA");
			String sid = (String) json.get("sid");
			String spid = (String) json.get("spid");
			String cid = (String) json.get("cid");
			String alias = (String) json.get("name");
			String conf = (String) json.getString("conf");
			String xposition = (String) json.get("xposition");
			String yposition = (String) json.get("yposition");
			String parent = (String) json.get("parent");
			String gparent = (String) json.get("gparent");
			String state = Device.STATE.inactive.name();
			String type = (String) json.getOrDefault("type", "bleserver");
			String source = (String) json.getOrDefault("source", "qubercomm");

			String keepalive = bleType(conf, "keepaliveinterval");
			String bleType = bleType(conf.toString(), "type");

			if (type.equals("ble"))
				type = "sensor";
			else if (type.equals("bleserver"))
				type = "server";

			double tluinterval = 0;

			if (source.equals("qubercomm")) {
				String tlu = bleType(conf, "tluinterval");
				tluinterval = Double.parseDouble(tlu);
			}

			BeaconDevice device = beaconDeviceService.findOneByUid(uid);

			if (param.equals("CustomConfig") || StringUtils.isBlank(spid)) {

				if (device == null) {
					LOG.info("ibeaconsave save >>>>");
					device = new BeaconDevice();
					device.setCreatedBy(cur_user);
					device.setCreatedOn(new Date(System.currentTimeMillis()));
					device.setUid(uid);
					device.setCid(cid);
					device.setName(alias);
					device.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
					device.setState(state);
					device.setTemplate(conf.toString());
					device.setConf(conf.toString());
					device.setType(bleType);
					device.setIp("0.0.0.0");
					device.setKeepAliveInterval(keepalive);
					device.setTlu(tluinterval);
					device.setSource(source);
					device.setTypefs("sensor");
					device = getBeaconDeviceService().save(device, true);

					body = "Device has been saved successfully.";
					code = 200;
					success = true;
					return new Restponse<String>(success, code, body);

				} else {

					LOG.info("ibeaconsave update>>>>>>");

					String isConfigured = device.getStatus();
					if (!isConfigured.equalsIgnoreCase(BeaconDevice.STATUS.CONFIGURED.name())) {
						device.setCid(cid);
					}
					device.setTypefs("sensor");
					device.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
					device.setName(alias);
					device.setTemplate(conf.toString());
					device.setConf(conf.toString());
					device.setType(bleType); // BLE type scanner or receiver or server
					device.setKeepAliveInterval(keepalive);
					device.setTlu(tluinterval);
					device.setSource(source);
					device.setModifiedBy(cur_user);
					device.setModifiedOn(new Date(System.currentTimeMillis()));
					device = getBeaconDeviceService().save(device, true);

					body = "Device has been saved successfully.";
					code = 200;
					success = true;
					return new Restponse<String>(success, code, body);
				}

			} else if (param.equals("FloorConfig") && StringUtils.isNotBlank(sid) && StringUtils.isNotBlank(spid)) {

				LOG.info("ibeaconsave floorConfig");

				// case : 1 device null should be save
				// case : 2 device not null but status REGISTERED should be save
				// case : 3 device not null update the device config

				if (spid != null) {
					Portion portion = portionService.findById(spid);
					if (portion != null) {
						if (cid == null)
							cid = portion.getCid();
						if (sid == null)
							sid = portion.getSiteId();
					}
				}

				boolean isSave = false;

				if (device == null) {
					isSave = true;
					device = new BeaconDevice();
					device.setUid(uid);
					device.setCreatedBy(cur_user);
					device.setCreatedOn(new Date(System.currentTimeMillis()));
					device.setIp("0.0.0.0");
					device.setState(state);
					LOG.info("ibeaconsave new device floor config");
				} else {
					isSave = true;
					device.setModifiedBy(cur_user);
					device.setModifiedOn(new Date(System.currentTimeMillis()));
				}

				if (device != null) {
					String status = device.getStatus();
					if (!BeaconDevice.STATUS.REGISTERED.name().equals(status)
							&& StringUtils.isBlank(device.getSpid())) {
						isSave = true;
					} else if (!BeaconDevice.STATUS.REGISTERED.name().equals(status)
							&& StringUtils.isBlank(device.getSpid()) && StringUtils.isNotBlank(device.getCid())) {
						isSave = true;
					}
				}

				if (isSave) {

					if (StringUtils.isEmpty(device.getCid())) {
						device.setCid(cid);
					}

					device.setName(alias);
					device.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
					device.setTemplate(conf.toString());
					device.setConf(conf.toString());
					device.setModifiedBy(cur_user);
					device.setType(bleType);
					device.setKeepAliveInterval(keepalive);
					device.setTlu(tluinterval);
					device.setSource(source);
					device.setTypefs(type);
					device.setParent(parent);
					device.setGparent(gparent);

					if (device.getSpid() == null)
						device.setSpid(spid);
					if (device.getSid() == null)
						device.setSid(sid);
					if (StringUtils.isNotBlank(xposition))
						device.setXposition(xposition);
					if (StringUtils.isNotBlank(yposition))
						device.setYposition(yposition);

					if (StringUtils.isNotBlank(xposition) && StringUtils.isNotBlank(yposition)) {
						geoFinderRestController.Pixel2Coordinate(spid, uid, xposition, yposition);
					}

					device = getBeaconDeviceService().save(device, true);

					body = "Device has been saved successfully.";
					code = 200;
					success = true;
					return new Restponse<String>(success, code, body);

				} else if (device != null) {

					LOG.info("ibeaconsave floor config update");

					if (StringUtils.isEmpty(device.getCid())) {
						device.setCid(cid);
					}
					
					device.setName(alias);
					device.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
					device.setTemplate(conf.toString());
					device.setConf(conf.toString());
					device.setModifiedBy(cur_user);
					device.setType(bleType);
					device.setKeepAliveInterval(keepalive);
					device.setTlu(tluinterval);
					device.setSource(source);

					if (StringUtils.isBlank(device.getTypefs())) {
						device.setTypefs(type);
					}
					if (StringUtils.isBlank(device.getParent())) {
						device.setParent(parent);
					}
					if (StringUtils.isBlank(device.getParent())) {
						device.setGparent(gparent);
					}
					if (StringUtils.isBlank(device.getSpid())) {
						device.setSpid(spid);
					}
					if (StringUtils.isBlank(device.getSid())) {
						device.setSid(sid);
					}
					if (StringUtils.isNotBlank(xposition)) {
						device.setXposition(xposition);
					}
					if (StringUtils.isNotBlank(yposition)) {
						device.setYposition(yposition);
					}

					if (StringUtils.isNotBlank(xposition) && StringUtils.isNotBlank(yposition)) {
						geoFinderRestController.Pixel2Coordinate(spid, uid, xposition, yposition);
					}

					device = getBeaconDeviceService().save(device, true);

					body = "Device has been saved successfully.";
					code = 200;
					success = true;

					return new Restponse<String>(success, code, body);
				}
			}

		} catch (Exception e) {
			body = "Error " + e.getMessage();
			code = 500;
			success = false;
			e.printStackTrace();
		}
		// }

		return new Restponse<String>(success, code, body);
	}

	private DeviceService getDeviceService() {
		if (devService == null) {
			devService = Application.context.getBean(DeviceService.class);
		}
		return devService;
	}

	private BeaconDeviceService getBeaconDeviceService() {
		if (beaconDeviceService == null) {
			beaconDeviceService = Application.context.getBean(BeaconDeviceService.class);
		}
		return beaconDeviceService;
	}public ClientDeviceService getClientDeviceService() {
		if (clientDeviceService == null) {
			clientDeviceService = Application.context.getBean(ClientDeviceService.class);
		}
		return clientDeviceService;
	}
}
