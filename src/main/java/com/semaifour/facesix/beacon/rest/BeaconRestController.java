package com.semaifour.facesix.beacon.rest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconAlertDataService;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.data.ReportBeacon;
import com.semaifour.facesix.beacon.data.ReportBeaconService;
import com.semaifour.facesix.beacon.util.BeaconFileImportUtil;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.mqtt.Payload;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * Rest Controller for managing beacon scanning, checkout, checkin, etc
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/beacon")
public class BeaconRestController extends WebController {

	@Autowired
	BeaconDeviceService beaconDeviceService;

	@Autowired
	ReportBeaconService reportBeaconService;

	@Autowired
	BeaconAlertDataService beaconAlertDataService;

	@Autowired
	PortionService portionService;
	
	@Autowired
	BeaconFileImportUtil fileImportUtil;

	static Logger LOG = LoggerFactory.getLogger(BeaconRestController.class.getName());

	@Autowired
	private BeaconService beaconService;

	@Autowired
	private DeviceEventPublisher mqttPublisher;

	/**
	 * 
	 * Return list of beacons in scanned list currently
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/scanned", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> scannedList(HttpServletRequest request, HttpServletResponse response) {
		// FIXME: enable scoping and return only becons scanned by this session
		// return beaconService.getScannedBeacons(request.getSession().getId());
		return beaconService.getScannedBeacons();
	}

	/**
	 * 
	 * Return list of beacons in scanned list currently
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/checkedout", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> BycheckedoutList(
			@RequestParam(value = "cid", required = false) String cid) {
		String status = Beacon.STATUS.checkedout.name();
		if (cid != null) {
			return beaconService.getSavedBeaconByCidAndStatus(cid, status);
		}
		return beaconService.getSavedBeaconsByStatus(status);
	}

	/**
	 * 
	 * Return list of beacons that is checkedin
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/checkedin", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> BycheckedinList(@RequestParam(value = "cid", required = true) String cid) {
		
		final String status = Beacon.STATUS.checkedin.name();
		final String sortBy = "createdOn";
		
		Sort sort = new Sort(Sort.Direction.DESC, sortBy);
				
		return beaconService.findByCidStatus(cid,status,sort);
	}

	@RequestMapping(value = "/list/checkout", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> checkoutList(@RequestParam(value = "cid", required = true) String cid) {

		String status 		= Beacon.STATUS.checkedout.name();
		final String sortBy = "createdOn";
		Sort sort 			= new Sort(Sort.Direction.DESC, sortBy);
		
		return beaconService.findByCidStatus(cid, status, sort);
	}

	/**
	 * 
	 * Return list of beacons in scanned list currently
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/scanner", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> checkedoutList(@RequestParam(value = "suid", required = true) String suid) {
		return beaconService.getSavedBeaconsByScanner(suid);
	}

	/**
	 * Check-in a used beacon
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/checkin", method = RequestMethod.POST)
	public @ResponseBody Beacon checkin(@RequestParam(value = "id", required = true) String id,
			HttpServletRequest request, HttpServletResponse response) {
		return beaconService.checkin(id, whoami(request, response));
	}

	/**
	 * 
	 * Returns beacon found in scanned list of beacon
	 * 
	 * @param macaddr
	 * @return
	 */
	@RequestMapping(value = "/get/scanned", method = RequestMethod.GET)
	public Beacon scanned(@RequestParam("macaddr") String macaddr, HttpServletRequest request,
			HttpServletResponse response) {
		return beaconService.getScannedBeacon(macaddr, request.getSession().getId());
	}

	@RequestMapping(value = "/remove/scanned", method = RequestMethod.DELETE)
	public Beacon removeScannedBeacon(@RequestParam("macaddr") String macaddr, HttpServletRequest request,
			HttpServletResponse response) {
		return beaconService.removeScannedBeacon(macaddr, request.getSession().getId());
	}

	@RequestMapping(value = "/clear/scanned", method = RequestMethod.DELETE)
	public void removeScannedBeacon(HttpServletRequest request, HttpServletResponse response) {
		beaconService.clearScannedBeacons(request.getSession().getId());
	}

	@RequestMapping(value = "/triggerscan", method = RequestMethod.GET)
	public Payload triggerscan(@RequestParam("cid") String cid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(cid, "scan-beacon-tags", "start scanning beacon tags", request, response);
	}

	@RequestMapping(value = "/trigger/scan/start", method = RequestMethod.GET)
	public Payload triggerScanStart(@RequestParam("suid") String suid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(suid, "start-scan-beacon-tags", "start scanning beacon tags", request, response);
	}

	@RequestMapping(value = "/trigger/scan/abort", method = RequestMethod.GET)
	public Payload triggerScanAbort(@RequestParam("suid") String suid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(suid, "abort-scan-beacon-tags", "abort scanning beacon tags", request, response);
	}

	@RequestMapping(value = "/trigger/scan/complete", method = RequestMethod.GET)
	public Payload triggerScanComplete(@RequestParam("suid") String suid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(suid, "complete-scan-beacon-tags", "complete scanning beacon tags", request,
				response);
	}

	/**
	 * 
	 * Builds a Paylod and sends it as a scanner command
	 * 
	 * @param suid
	 * @param opcode
	 * @param message
	 * @param request
	 * @param response
	 * @return
	 */
	private Payload sendScannerCommand(String cid, String opcode, String message, HttpServletRequest request,
			HttpServletResponse response) {
		Payload payload = new Payload(opcode, whoami(request, response), cid, message);
		payload.put("reqid", request.getSession().getId());
		payload.put("cid", cid);
		if (beaconService.sendBeaconCommand(payload)) {
			payload.put("status", "sent-success");
		} else {
			payload.put("status", "sent-failed");
		}
		return payload;
	}

	/**
	 * 
	 * @param macaddr
	 * @param assto
	 * @param type
	 * @param cid
	 * @param name
	 * @param bi
	 * @param txpwr
	 * @param tagmod
	 * @param reftx
	 * @param request
	 * @param response
	 * @return
	 */
	
	@RequestMapping(value = "/checkout/beacon", method = RequestMethod.POST)
	public @ResponseBody Beacon checkout(
			@RequestParam(value = "macaddr", required = true) String macaddr,
			@RequestParam(value = "assto", required = true) String assto,
			@RequestParam(value = "patient", required = false) String type,
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "bi", required = false) String bi,
			@RequestParam(value = "txpwr", required = false) String txpwr,
			@RequestParam(value = "tagmod", required = false) String tagmod,
			@RequestParam(value = "reftx", required = false) String reftx, HttpServletRequest request,
			HttpServletResponse response) {

		if (StringUtils.isEmpty(cid)) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		// String patientNameAndType = assto + "/" + type;
		// LOG.info(" macaddr " + macaddr + " assto " + assto + " patient Type " + type
		// + " cid " + cid+ "tagname" + name);
		return beaconService.checkout(macaddr, assto, type, cid, name, bi, txpwr, tagmod, reftx, null,
				whoami(request, response), request);
	}

	@RequestMapping(value = "/checkin/beacon", method = RequestMethod.POST)
	public Restponse<String> checkin(@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "macaddr", required = false) String macaddr, HttpServletRequest request,
			HttpServletResponse response) {

		int code = 200;
		boolean success = true;
		String body = "Tag has been checked-in successfully.";

		try {
			beaconService.checkin(id, whoami(request, response));
		} catch (Exception e) {
			code = 500;
			success = false;
			body = "While tag checked-in occurred error.";
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);
	}

	/**
	 * used to checkin muliple tags
	 * 
	 * @param tagids
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/bulkCheckInTag", method = RequestMethod.POST)
	public Restponse<String> mulipletagcheckedin(@RequestBody String[] tagids, HttpServletRequest request,
			HttpServletResponse response) {

		int code = 200;
		boolean success = true;
		String body = "Tag has been checked-in successfully.";

		try {
			List<String> ids = Arrays.asList(tagids);
			for (String id : ids) {
				beaconService.checkin(id, whoami(request, response));
			}
		} catch (Exception e) {
			code = 500;
			success = false;
			body = "While tag bulkCheckeInTag occurred error." + e.getMessage();
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Restponse<String> delete(@RequestBody String[] macList, String status, HttpServletRequest request,
			HttpServletResponse response) {

		int code = 200;
		boolean success = true;
		String body = "Tag has been deleted successfully.";

		try {

			List<String> macaddrs = Arrays.asList(macList);

			List<ReportBeacon> reportBeacon = new ArrayList<ReportBeacon>();
			List<Beacon> beacon = new ArrayList<Beacon>();

			List<Beacon> ibeacon = beaconService.findByMacaddrs(macaddrs);
			List<ReportBeacon> rBeacon = reportBeaconService.findByMacaddrs(macaddrs);

			if (ibeacon != null)
				beacon.addAll(ibeacon);
			if (rBeacon != null)
				reportBeacon.addAll(rBeacon);

			if (reportBeacon.size() > 0) {
				reportBeaconService.deleteList(reportBeacon);
			}

			if (beacon.size() > 0) {
				for (Beacon b : beacon) {
					String beaconStatus = b.getStatus();
					if (beaconStatus.equals(Beacon.STATUS.checkedout.name())) {
						beaconService.markExitForBeacon(b);
					}
				}
				beaconService.delete(beacon);
			} else {
				code = 404;
				success = false;
				body = "Tag not found.";
			}

		} catch (Exception e) {
			code = 500;
			success = false;
			body = "While delete tag occurred error.";
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);
	}

	@RequestMapping(value = "/scanned", method = RequestMethod.GET)
	public JSONObject scanned(@RequestParam(value = "cid", required = true) String cid) throws IOException {

		JSONObject devlist = new JSONObject();
		try {

			String beacon_cid = "";
			JSONObject dev = null;
			JSONArray dev_array = new JSONArray();

			// LOG.info(" *** cid *** " +cid);

			Collection<Beacon> beacon = beaconService.getScannedBeacons();

			// LOG.info("TAG LIST " +beacon.toString());

			if (beacon != null) {
				for (Beacon dv : beacon) {
					beacon_cid = dv.getCid();
					// LOG.info("===cid==== " +cid +"beacon_cid " +beacon_cid);
					if (cid.equals(beacon_cid)) {
						// LOG.info("===cid EQU==== " +cid +"beacon_cid " +beacon_cid);
						dev = new JSONObject();
						String scid = dv.getScannerUid();
						String mid = dv.getMacaddr();
						dev.put("id", dv.getId());
						dev.put("uid", dv.getUid());
						dev.put("macaddr", mid);
						dev.put("scannerUid", scid);

						Beacon bcMac = beaconService.findOneByMacaddr(mid);
						if (bcMac != null) {
							// LOG.info("Beacon CheckedIn Already!!!!");
							dev.put("checkedin", Beacon.STATUS.checkedin.name());
						} else {
							// LOG.info("New beacon!!!!");
							dev.put("newbeacon", "newbeacon");
						}
						BeaconDevice device = beaconDeviceService.findOneByUid(scid);
						if (device != null) {
							dev.put("scanner", device.getName());
						} else {
							dev.put("scanner", "Unknown");
						}

						dev.put("minor", dv.getMinor());
						dev.put("major", dv.getMajor());
						dev.put("dev_name", dv.getDevice_name());
						dev_array.add(dev);
					} else {
						// LOG.info("other customer beacon " + cid);
						continue;
					}
				}
				devlist.put("scanned", dev_array);
			}
		} catch (Exception e) {
			LOG.info("while getting scanned beacon list error", e);
		}
		return devlist;
	}

	
	/**
	 * Used to Import Tag
	 * expecting .xls,.xlsx,.csv format
	 * @param cid
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/tagimport", method = RequestMethod.POST)
	public Restponse<String> tagimport1(@RequestParam("cid") final String cid, MultipartHttpServletRequest request,
			HttpServletRequest req, HttpServletResponse res) {

		boolean success = true;
		int code = 200;
		String body = "File found was empty !! Add tagids to checkout.";
		boolean isValidFileFormat = false;
		Workbook workbook=null;
		try {

			Iterator<String> itrator = request.getFileNames();
			MultipartFile multiFile = request.getFile(itrator.next());
			isValidFileFormat=fileImportUtil.fileValidation(multiFile);
			
			if (!isValidFileFormat) {
				body = "File imported is not in the expected format";
				success = false;
				code = 500;
				return new Restponse<String>(success, code, body);
			}
			else {
				workbook=fileImportUtil.workBookCreation(multiFile);
			}
			
			if(workbook!=null) {
				Restponse<String> fileResponse=fileImportUtil.excelFileProcessing(workbook, cid);
				body=fileResponse.getBody();
				code=fileResponse.getCode();
				success=fileResponse.isSuccess();
			}
			else {
			
			String content = new String(multiFile.getBytes(), "UTF-8");
			if (StringUtils.isNotBlank(content)) {
				Restponse<String> fileResponse=fileImportUtil.csvFileProcessing(content,cid);
				body=fileResponse.getBody();
				code=fileResponse.getCode();
				success=fileResponse.isSuccess();
			}
		  else {
				body = "File found was empty !! Add tagids to checkout.";
				success = false;
				code = 412;
			}
			}
		} catch (Exception e) {
			body = "While Tag import occurred error.";
			success = false;
			code = 500;
			e.printStackTrace();
		}
		finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					LOG.error("Error while trying to close the workbook" + e.getMessage());
				}
			}
		}
		
		return new Restponse<String>(success, code, body);
	}

	/**
	 * used to update the tag details
	 * 
	 * @return response
	 * 
	 */
	@PostMapping("/tagconfigure")
	public Restponse<String> save(@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "conf", required = false) String conf, HttpServletRequest request,
			HttpServletResponse response) {

		boolean success = true;
		int code = 200;
		String body = "Tag has been updated successfully.";

		try {

			net.sf.json.JSONObject data = net.sf.json.JSONObject.fromObject(conf);

			LOG.info(" data " + data);

			if (data != null) {
				Beacon beacon = beaconService.findOneByMacaddr(macaddr);
				if (beacon != null) {

					String name = data.getString("name");
					String tagtype = data.getString("tagtype");
					String minor = data.getString("minor");
					String major = data.getString("major");
					String assignedto = data.getString("assignedto");
					String uuid = null;
					String txpower = data.getString("txpower");
					String interval = "0";
					String tagMod = (String) data.get("tagmod");
					String refTx = data.getString("reftx");

					if (data.containsKey("uuid"))
						uuid = (String) data.get("uuid");
					if (data.containsKey("interval"))
						interval = data.getString("interval");

					beacon.setName(name);
					beacon.setTagType(tagtype);
					beacon.setMinor(Integer.parseInt(minor));
					beacon.setMajor(Integer.parseInt(major));
					beacon.setAssignedTo(assignedto);
					beacon.setUid(uuid);
					beacon.setTxPower(Integer.parseInt(txpower));
					beacon.setInterval(Integer.parseInt(interval));
					beacon.setTemplate(conf);
					beacon.setTagModel(tagMod);
					beacon.setRefTxPwr(refTx);
					beacon.setModifiedOn(new Date());
					beacon.setModifiedBy(whoami(request, response));
					beacon = beaconService.save(beacon, true);

				} else {
					success = false;
					code = 404;
					body = "Tag not found";
				}

			} else {
				success = false;
				code = 404;
				body = "conf not empty";
			}
		} catch (Exception e) {
			success = false;
			code = 500;
			body = "While update tag occured error.";
		}

		return new Restponse<String>(success, code, body);
	}

	@RequestMapping(value = "/checkedout", method = RequestMethod.GET)
	public JSONArray checkedout(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "name", required = false) String name) throws IOException {

		JSONArray dev_array = new JSONArray();

		try {

			JSONObject dev = null;
			List<Beacon> beacon = null;

			if (name != null && name.contains(":")) {
				beacon = beaconService.getSavedBeaconByCidAndMacAddr(cid, name.toUpperCase());
			} else if (sid != null && !sid.equals("") && name != null && !name.equalsIgnoreCase("undefined")) {
				beacon = beaconService.getSavedBeaconBySidAndAssignedTo(sid, name);
				if (beacon == null || beacon.isEmpty()) {
					beacon = beaconService.getSavedBeaconBySidAndTagType(sid, name);
					if (beacon == null || beacon.isEmpty()) {
						beacon = beaconService.getSavedBeaconBySid(sid);
					}
				}
			} else {
				beacon = beaconService.getSavedBeaconByCidAndAssignedTo(cid, name);
				if (beacon == null || beacon.isEmpty()) {
					beacon = beaconService.getSavedBeaconByCidAndTagType(cid, name);
				}
			}

			String status = "";
			String color = "";
			String fafa = "";

			if (beacon == null || beacon.isEmpty()) {
				beacon = beaconService.getSavedBeaconByCid(cid);
			}

			if (beacon != null) {
				for (Beacon dv : beacon) {
					status = dv.getStatus();
					if (status.equals(Beacon.STATUS.checkedout.name())) {
						dev = new JSONObject();
						dev.put("id", dv.getId());
						dev.put("uid", dv.getUid());
						dev.put("macaddr", dv.getMacaddr());
						dev.put("state", dv.getState().toUpperCase());
						dev.put("dev_name", dv.getDevice_name());
						dev.put("assignedTo", dv.getAssignedTo().toUpperCase());
						dev.put("tagtype", dv.getTagType().toUpperCase());
						dev.put("sid", (dv.getSid() == null) ? "NA" : dv.getSid());
						dev.put("spid", (dv.getSpid() == null) ? "NA" : dv.getSpid());
						dev.put("cid", (dv.getCid() == null) ? "NA" : dv.getCid());

						String debug = "unchecked";
						if (dv.getDebug() != null) {
							debug = dv.getDebug().equals("enable") ? "checked" : "unchecked";
						}
						dev.put("debugflag", debug);

						if (dv.getBattery_level() != 0) {
							int battery = dv.getBattery_level();
							String batteryinfo = beaconService.batteryStatus(battery);
							dev.put("fafa", batteryinfo.split("&")[0]);
							dev.put("color", batteryinfo.split("&")[1]);
							dev.put("battery", battery);// battery percentage
						} else {
							color = "black";
							fafa = "fa fa-battery-empty fa-2x";
							dev.put("fafa", fafa);
							dev.put("color", color);
							dev.put("battery", 0);
						}
						String floorName = (dv.getLocation() == null || dv.getLocation().isEmpty()) ? "NA"
								: dv.getLocation().toUpperCase();
						dev.put("location", floorName); // floor name

						String reciverAlias = dv.getReciveralias() == null ? "NA" : dv.getReciveralias().toUpperCase();
						dev.put("alias", reciverAlias);

						dev_array.add(dev);
					}

				}
			}
		} catch (Exception e) {
			LOG.info("while getting checkedout beacon list error", e);
		}
		return dev_array;
	}

	@RequestMapping(value = "/debugByTag", method = RequestMethod.POST)
	public Restponse<String> debugByTag(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "debugflag", required = true) String flag, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		boolean success = true;
		int code = 200;
		String body = "Debug has been enabled successfully.";

		try {

			JSONObject tagJson = new JSONObject();
			JSONArray tagArray = new JSONArray();

			ArrayList<Beacon> beaconList = new ArrayList<Beacon>();
			ArrayList<Beacon> list = new ArrayList<Beacon>();

			String status = Beacon.STATUS.checkedout.name();
			Collection<Beacon> beacon = null;
			String template = " \"opcode\":\"{0}\",\"tag_list\":{1}";
			String message = "";
			String opcode = "tag_logging";
			String debugflag = "disable";

			if (flag.equals("true")) {
				debugflag = "enable";
			}

			if (StringUtils.isEmpty(cid)) {
				cid = SessionUtil.getCurrentCustomer(request.getSession());
			}

			if (StringUtils.isBlank(macaddr)) { // selectAll
				beacon = beaconService.getSavedBeaconByCidAndStatus(cid, status);
				beaconList.addAll(beacon);
				LOG.info("Tag Select All by cid " + cid);
			} else { // Select By Tag
				Beacon b = beaconService.findOneByMacaddr(macaddr);
				beaconList.add(b);
				LOG.info("Tag Based MQTT Topic By MacAddr " + macaddr);
			}

			if (beaconList != null) {
				for (Beacon b : beaconList) {
					b.setDebug(debugflag);
					b.setModifiedOn(new Date());
					b.setModifiedBy(whoami(request, response));
					list.add(b);

					macaddr = b.getMacaddr();

					tagJson.put("tag_uid", macaddr);
					tagJson.put("debug", debugflag);
					tagArray.add(tagJson);
				}
				beaconService.save(list);

				message = MessageFormat.format(template, new Object[] { opcode, tagArray });
				mqttPublisher.publish("{" + message + "}", cid);
			}
		} catch (Exception e) {
			success = false;
			code = 500;
			body = "While enable Debug flag ocuured error.";
			e.printStackTrace();
		}

		return new Restponse<String>(success, code, body);
	}

	@RequestMapping(value = "/avgTimeTaken", method = RequestMethod.GET)
	public JSONObject avgTimeTaken(@RequestParam(value = "cid", required = true) String cid, HttpServletRequest request,
			HttpServletResponse response) {
		Collection<Beacon> beaconlist = beaconService.getSavedBeaconByCidAndStatus(cid, Beacon.STATUS.checkedout.name());
		double avgTimeToReceiveData = 0, avgTimeToProcessData = 0, avgEndToEndDelay = 0;
		for (Beacon b : beaconlist) {

			double TimeDiffToReceiveData = 0, TimeDiffToProcessData = 0, TimeDiffForEndToEndDelay = 0;

			Date recordSent = b.getRecordSent();
			Date recordSeen = b.getRecordSeen();
			Date recordUpdated = b.getRecordUpdate();

			TimeDiffToReceiveData = beaconService.getDiff(recordSeen, recordSent);
			TimeDiffToProcessData = beaconService.getDiff(recordUpdated, recordSeen);
			TimeDiffForEndToEndDelay = beaconService.getDiff(recordUpdated, recordSent);

			avgTimeToReceiveData += TimeDiffToReceiveData;
			avgTimeToProcessData += TimeDiffToProcessData;
			avgEndToEndDelay += TimeDiffForEndToEndDelay;
		}

		int beaconcount = beaconlist.size();
		avgTimeToReceiveData = avgTimeToReceiveData / beaconcount;
		avgTimeToProcessData = avgTimeToProcessData / beaconcount;
		avgEndToEndDelay = avgEndToEndDelay / beaconcount;

		JSONObject json = new JSONObject();
		json.put("avgTimeToReceiveData(millisec)", avgTimeToReceiveData);
		json.put("avgTimeToProcessData(millisec)", avgTimeToProcessData);
		json.put("avgEndToEndDelay(millisec)", avgEndToEndDelay);
		return json;
	}

	@RequestMapping(value = "/perTagAvgTime", method = RequestMethod.GET)
	public JSONObject perTagAvgTime(@RequestParam(value = "macaddr", required = true) String macaddr,
			HttpServletRequest request, HttpServletResponse response) {
		Beacon beacon = beaconService.findOneByMacaddr(macaddr);
		double avgTimeToReceiveData = 0, avgTimeToProcessData = 0, avgEndToEndDelay = 0;
		avgTimeToReceiveData = beacon.getAvgReceiveTime();
		avgTimeToProcessData = beacon.getAvgProcessTime();
		avgEndToEndDelay = beacon.getAvgUpdateTime();

		JSONObject json = new JSONObject();
		json.put("tagid", macaddr);
		json.put("avgTimeToReceiveData", avgTimeToReceiveData);
		json.put("avgTimeToProcessData", avgTimeToProcessData);
		json.put("avgTimeToUpdateData", avgEndToEndDelay);
		return json;
	}

	@RequestMapping(value = "/avgTagDetailByCid", method = RequestMethod.GET)
	public JSONArray avgTagDetailByCid(@RequestParam(value = "cid", required = true) String cid,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONArray jsonArray = new JSONArray();
		try {
			List<Beacon> beaconList = beaconService.getSavedBeaconByCid(cid);
			for (Beacon b : beaconList) {
				JSONObject json = perTagAvgTime(b.getMacaddr(), null, null);
				jsonArray.add(json);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	/*
	 * /checkout/list
	 * 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/checkout/tag", method = RequestMethod.POST)
	public Restponse<String> checkoutList(@RequestBody JSONObject data, HttpServletRequest request,
			HttpServletResponse response) {

		String body = "Tag has been checkout successfully";
		int code = 200;
		boolean success = true;

		if (data != null) {
			if (data.containsKey("cid")) {
				String cid = (String) data.get("cid");
				if (data.containsKey("beacon")) {

					JSONArray beaconlist = data.getJSONArray("beacon");

					beaconlist.forEach(mytag -> {
						JSONObject json = (JSONObject) mytag;

						String mac = json.getString("macaddr");
						String assignedTo = json.getString("assignedTo");
						String tag = json.getString("tag_type");
						String tagmod = json.getString("tagmod");
						String reftx = json.getString("reftx");

						if (StringUtils.isBlank(assignedTo))
							assignedTo = "quser";
						if (StringUtils.isBlank(tag))
							tag = "qtag";

						Beacon beacon = beaconService.checkout(mac, assignedTo, tag, cid, BeaconService.TAG_NAME, BeaconService.BEACON_INTERVEL, BeaconService.TX_POWER,
								tagmod, reftx, null, whoami(request, response), request);
					});
				} else {
					body = "Beacon List not found";
					code = 404;
					success = false;
				}
			} else {
				body = "Cid not found";
				code = 404;
				success = false;
			}
		} else {
			body = "Data not found";
			code = 404;
			success = false;
		}
		return new Restponse<String>(success, code, body);
	}

	/**
	 * @param cid
	 * @param sid
	 * @param spid
	 * @param tagid
	 * @param request
	 * @param response
	 * @return
	 * 
	 * 		This method provides the data about the tags that are inactive.
	 */
	@RequestMapping(value = "/inactivity_pop_up", method = RequestMethod.GET)
	public JSONArray inactivityPopUp(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "tagid", required = false) String tagid, HttpServletRequest request,
			HttpServletResponse response) {
		JSONArray data = new JSONArray();
		if (StringUtils.isNotEmpty(tagid)) {
			Map<String, Object> inactivityPopUpDetail = beaconService.findByIncativityPopUp(tagid);
			if (inactivityPopUpDetail != null) {
				data.add(inactivityPopUpDetail);
			}
		} else if (StringUtils.isNotEmpty(spid) || StringUtils.isNotEmpty(sid)) {
			List<Beacon> beaconList = null;
			Map<String, Object> inactivityPopUpDetail = null;
			String state = "inactive";
			String status = "checkedout";
			if (StringUtils.isNotEmpty(spid)) {
				beaconList = beaconService.getSavedBeaconByCidSpidStateAndStatus(cid, spid, state, status);
			} else {
				beaconList = beaconService.getSavedBeaconByCidSidStateAndStatus(cid, sid, state, status);
			}
			for (Beacon b : beaconList) {
				String tid = b.getMacaddr();
				inactivityPopUpDetail = beaconService.findByIncativityPopUp(tid);
				if (inactivityPopUpDetail != null) {
					data.add(inactivityPopUpDetail);
				}
			}
		}
		return data;
	}
	/**
	 * @param cid
	 * @param sid
	 * @param spid
	 * @param tagid
	 * @param request
	 * @param response
	 * @return
	 * 
	 * 		This method provides data about tags that go out of their geo-fenced
	 *         region.
	 */
	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @RequestMapping(value = "/geo_fence_pop_up", method = RequestMethod.GET)
	 * public JSONArray geoFencePopUp(@RequestParam(value="cid",required=true)
	 * String cid,
	 * 
	 * @RequestParam(value="sid",required=false) String sid,
	 * 
	 * @RequestParam(value="spid",required=false) String spid,
	 * 
	 * @RequestParam(value="tagid",required=false) String tagid, HttpServletRequest
	 * request, HttpServletResponse response) { JSONArray data = new JSONArray();
	 * List<String> addedtags = new ArrayList<String>(); JSONObject json = new
	 * JSONObject(); if (StringUtils.isNotEmpty(tagid)) { Map<String, Object>
	 * geoFencePopUpDetail = beaconService.findByGeoFencePopUp(tagid); if
	 * (geoFencePopUpDetail != null) { data.add(geoFencePopUpDetail); } } else if
	 * (StringUtils.isNotEmpty(spid) || StringUtils.isNotEmpty(sid)) {
	 * List<BeaconDevice> locationList = null; String deviceType = "receiver"; if
	 * (StringUtils.isNotEmpty(sid)) { List<Portion> portionList =
	 * portionService.findBySiteId(sid); for (Portion p : portionList) { String
	 * placeid = p.getId(); json = getDataByPlaceId(data, placeid, cid,addedtags);
	 * data = (JSONArray)json.get("data"); addedtags =
	 * json.getJSONArray("addedtags"); } locationList =
	 * beaconDeviceService.findBySidAndType(sid, deviceType); } else { json =
	 * getDataByPlaceId(data, spid, cid,addedtags); data =
	 * (JSONArray)json.get("data"); locationList =
	 * beaconDeviceService.findBySpidAndType(spid, deviceType); } if(locationList !=
	 * null && locationList.size()>0) { for(BeaconDevice bd: locationList) { String
	 * placeid = bd.getUid(); json = getDataByPlaceId(data, placeid, cid,addedtags);
	 * data = (JSONArray)json.get("data"); addedtags =
	 * (JSONArray)json.get("addedtags"); } } } return data; }
	 * 
	 * private JSONObject getDataByPlaceId(JSONArray data, String placeid, String
	 * cid, List<String> addedtags) { List<BeaconAlertData> beaconAlertData =
	 * beaconAlertDataService.findByPlaceIds(placeid); JSONObject json = new
	 * JSONObject(); Map<String, Object> inactivityDetail = null; String status =
	 * "checkedout"; for (BeaconAlertData bcnAlData : beaconAlertData) { JSONArray
	 * tagids = bcnAlData.getTagids(); if (tagids.get(0).equals("all")) {
	 * Iterable<Beacon> beaconlist = beaconService.getSavedBeaconByCidAndStatus(cid,
	 * status); for (Beacon b : beaconlist) { String b_tagid = b.getMacaddr(); if
	 * (!addedtags.contains(b_tagid)) { inactivityDetail =
	 * beaconService.findByGeoFencePopUp(b_tagid); if (inactivityDetail != null) {
	 * data.add(inactivityDetail); addedtags.add(b_tagid); } } } } else {
	 * Iterator<String> iterId = tagids.iterator(); while (iterId.hasNext()) {
	 * String tid = iterId.next(); if (!addedtags.contains(tid)) { inactivityDetail
	 * = beaconService.findByGeoFencePopUp(tid); if (inactivityDetail != null) {
	 * data.add(inactivityDetail); addedtags.add(tid); } } } } } json.put("data",
	 * data); json.put("addedtags", addedtags); return json; }
	 */

	/**
	 * Used to edit the tag
	 * 
	 * @param model
	 * @param macaddr
	 * @param sid
	 * @param spid
	 * @param cid
	 * @param request
	 * @param response
	 * @return
	 */

	@RequestMapping("/configure")
	public Restponse<Beacon> tagconfigure(@RequestParam(value = "macaddr", required = true) String macaddr) {

		LOG.info(" Tag configure macaddr : " + macaddr);

		JSONObject json = new JSONObject();

		JSONObject tagJson = new JSONObject();
		JSONArray tagArray = new JSONArray();

		int code = 200;
		boolean success = true;

		Beacon beacon = beaconService.findOneByMacaddr(macaddr);

		if (beacon != null) {
			if (beacon.getTemplate() == null) {
				json.put("minor", beacon.getMinor());
				json.put("major", beacon.getMajor());
				json.put("assignedto", beacon.getAssignedTo());
				json.put("uuid", beacon.getUid());
				json.put("tagtype", beacon.getTagType());
				json.put("name", beacon.getName());
				json.put("txpower", beacon.getTxPower());
				json.put("interval", beacon.getInterval());
				json.put("tagmod", beacon.getTagModel());
				json.put("reftx", beacon.getRefTxPwr());
				tagArray.add(json);
				tagJson.put("attributes", tagArray);

				beacon.setTemplate(tagJson.toString());
				beaconService.save(beacon, false);
			}
		} else {
			code = 404;
			success = false;
		}
		return new Restponse<Beacon>(success, code, beacon);
	}

	/**
	 * Used to save and update the tag details
	 * @param macaddr
	 * @param cid
	 * @param conf
	 * @return
	 */

	@RequestMapping("/save")
	public Restponse<String> save(
			@RequestParam(value = "macaddr", required = true) String macaddr,
			@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "conf", required = true) String conf,
			HttpServletRequest request,HttpServletResponse response ) {

		boolean success = true;
		int code 		= 200;
		String body 	= "Tag details Saved successfully";

		try {
			
			net.sf.json.JSONObject payload = net.sf.json.JSONObject.fromObject(conf);
			
			if (payload.containsKey("attributes")) {

				JSONArray attribute = payload.getJSONArray("attributes");
				JSONObject obj 		= attribute.getJSONObject(0);

				String name 		= (String) obj.get("name");
				String tagtype 		= (String) obj.get("tagtype");
				String minor 		= obj.getString("minor");
				String major 		= obj.getString("major");
				String assignedto 	= (String) obj.get("assignedto");
				String uuid 		= obj.getString("uuid");
				String txpower 		= obj.getString("txpower");
				String interval 	= null;
				String tagMod 		= (String) obj.get("tagmod");
				String refTx 		= (String) obj.getOrDefault("reftx", "-59");

				if (obj.containsKey("interval")) {
					interval = obj.getString("interval");
				} 
				
				String whoami = whoami(request, response);
				
				Beacon beacon = beaconService.findOneByMacaddr(macaddr);
				if ( beacon == null) {
					 LOG.info("checkout tag macaddr " + macaddr + "assignedto " +assignedto);
					 
					 
					 if (StringUtils.isEmpty(txpower)) {
						 txpower =  BeaconService.TX_POWER;
					 } if (StringUtils.isEmpty(interval)) {
						 interval = BeaconService.BEACON_INTERVEL;
					 }
					 
					 beacon = beaconService.checkout(macaddr, assignedto, tagtype, cid, BeaconService.TAG_NAME, interval, txpower,
								tagMod, refTx, null, whoami, request);
					 
				} else {
					LOG.info("update tag macaddr " + macaddr + "assignedto " +assignedto);
					beacon.setName(name);
					beacon.setTagType(tagtype);
					beacon.setMinor(Integer.parseInt(minor));
					beacon.setMajor(Integer.parseInt(major));
					beacon.setAssignedTo(assignedto);
					beacon.setUid(uuid);
					beacon.setTxPower(Integer.parseInt(txpower));
					beacon.setInterval(Integer.parseInt(interval));
					beacon.setTemplate(payload.toString());
					beacon.setTagModel(tagMod);
					beacon.setRefTxPwr(refTx);
					beacon.setModifiedOn(new Date());
					beacon.setModifiedBy(whoami);

					beacon = beaconService.save(beacon, true);
					
				}

			} else {
				success = false;
				code 	= 404;
				body 	= "Tag attribute not found.";
			}
			
		} catch (Exception e) {
			success = false;
			code 	= 500;
			body 	= "While tag saving error occured";
			LOG.info("Error While tag update " +e.getMessage());
		}

		return new Restponse<String>(success, code, body);
	}
	
	/**
	 * This Method used to check duplicate Tag
	 * @param macAddr
	 * @return
	 */
	
	@RequestMapping(value="/checkDuplicate",method=RequestMethod.GET)
	public Restponse<String> checkDuplicate(@RequestParam("macaddr") final String macAddr) {
		
		boolean success = true;
		int code 		= 200;
		String body 	= "Please configure the tag";

		Beacon beacon = beaconService.findOneByMacaddr(macAddr);

		if (beacon != null) {
			if (beacon.getMacaddr().equalsIgnoreCase(macAddr)) {
				success = false;
				code 	= 422;
				body 	= "This tag already existing";
			}
 		}

 		return new Restponse<String>(success,code,body);
	}
	
	
	/**
	 * Used to save tag details
	 * @param tag
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/savetag",method=RequestMethod.POST)
	public Restponse<String> saveTagDetail(@RequestBody JSONObject tag, HttpServletRequest request, HttpServletResponse response) {
		boolean success = true;
		int code 		= 200;
		String body 	= "Tag details Saved successfully";

 		String macaddr = tag.getString("macaddr");
		Beacon beacon = beaconService.findOneByMacaddr(macaddr);

 		if(beacon != null) {
			String assignedTo = tag.getString("assignedTo");
			String tag_type   = tag.getString("tag_type");
			String tagModel   = tag.getString("tagmodel");
			String refTxPwr   = tag.getString("refTxPwr");

 			beacon.setAssignedTo(assignedTo);
			beacon.setTag_type(tag_type);
			beacon.setTagModel(tagModel);
			beacon.setRefTxPwr(refTxPwr);
			beacon.setModifiedOn(new Date());

 			beacon = beaconService.save(beacon, true);

 		}else {
			success = false;
			code = 404;
			body = "Tag details not found";
		}

 		return new Restponse<String>(success,code,body);
	}
}
