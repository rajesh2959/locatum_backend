package com.semaifour.facesix.web;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/web/mesh")
public class NmeshMonitorWebController extends WebController {

	static Logger LOG = LoggerFactory.getLogger(NmeshMonitorWebController.class.getName());
	
	
	@GetMapping("/systemdashboard")
	public String dashboaed(Map<String, Object> model,
			  @RequestParam(value = "sid", required = false) String sid,
			  @RequestParam(value = "spid", required = false) String spid,
			  @RequestParam(value = "cid", required = true) String cid,
			  @RequestParam(value = "uid", required = true) String uid,
			  HttpServletRequest request, HttpServletResponse response) {
				
		model.put("sid", sid);
		model.put("spid",spid);
		model.put("cid", cid);
		model.put("uid", uid);
		
		return _CCC.pages.getPage("facesix.meshmonitor.device.dashboard", "dashboard");
	}
	
	@GetMapping("/simulation")
	public String simulation(Map<String, Object> model,
			  @RequestParam(value = "sid", required = false) String sid,
			  @RequestParam(value = "spid", required = false) String spid,
			  @RequestParam(value = "cid", required = false) String cid,
			  @RequestParam(value = "uid", required = true) String uid,
			HttpServletRequest request, HttpServletResponse response) {
		
		model.put("sid", sid);
		model.put("spid",spid);
		model.put("cid", cid);
		model.put("uid", uid);
		return _CCC.pages.getPage("facesix.meshmonitor.device.simulation", "simulation");
	}
	
	@GetMapping("/videostats")
	public String videostats(Map<String, Object> model,
			  @RequestParam(value = "sid", required = false) String sid,
			  @RequestParam(value = "spid", required = false) String spid,
			  @RequestParam(value = "cid", required = true) String cid,
			  @RequestParam(value = "uid", required = true) String uid,
			HttpServletRequest request, HttpServletResponse response) {
		
		model.put("sid", sid);
		model.put("spid",spid);
		model.put("cid", cid);
		model.put("uid", uid);
		return _CCC.pages.getPage("facesix.meshmonitor.device.video.stats", "videostats");
	}
	
	@GetMapping("/pathselection")
	public String dmesh(Map<String, Object> model,
			  @RequestParam(value = "sid", required = false) String sid,
			  @RequestParam(value = "spid", required = false) String spid,
			  @RequestParam(value = "cid", required = true) String cid,
			  @RequestParam(value = "uid", required = true) String uid,
			HttpServletRequest request, HttpServletResponse response) {
		
		model.put("sid", sid);
		model.put("spid",spid);
		model.put("cid", cid);
		model.put("uid", uid);
		return _CCC.pages.getPage("facesix.meshmonitor.device.pathselection", "pathselection");
	}
}
