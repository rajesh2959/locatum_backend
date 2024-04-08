package com.semaifour.facesix.mesh.rest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.mesh.service.PathSelection;
import com.semaifour.facesix.mesh.service.MeshMonitorMQTTService;
import com.semaifour.facesix.mesh.service.MeshMonitorService;

@RestController
@RequestMapping("/rest/ui")
public class MeshMonitorRestController {

	static Logger logger = LoggerFactory.getLogger(MeshMonitorRestController.class.getName());
	
	private static final String ORDER_BY_DESC = "desc";
	
	
	@Autowired private MeshMonitorService meshMonitorService;
	
	@Autowired private MeshMonitorMQTTService meshMonitorMQTTService;
	
	
	/**
	 * 
	 * @param param
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/simulation",method = RequestMethod.POST)
	public Restponse<String> SimulationON(@RequestBody JSONObject param,HttpServletRequest request,HttpServletResponse response) {
		
		boolean success = true;
		int code  		= 200;
		String body 	= "success";
		 	 
		try {
			
			logger.info(" simulation payload " +param);
			
			String simuStatus    = (String)param.getOrDefault("simulation", "false");
			
			if (simuStatus.equals("false")) {
				meshMonitorMQTTService.simulationOff(param);
			} else {
				  String profile_type  = (String)param.getOrDefault("profile_type", "basic");
				switch (profile_type) {
				
				case "basic": {
					  meshMonitorMQTTService.basic(param);
					  break;
				}
				case "gaming": {
					 meshMonitorMQTTService.gaming(param);
					 break;
				}
				case "moderate": {
					 meshMonitorMQTTService.moderate(param);
					 break;
				}
				case "idle": {
					 meshMonitorMQTTService.idle(param);
					 break;
				}
				default:
					break;
				}
			}
			
		} catch (Exception e) {
			success = false;
			code 	= 500;
			body 	= "while simulation post error " +e.getMessage();
			e.printStackTrace();
		}
		
		return new Restponse<String>(success, code, body);
	}
	
	/**
	 * systemStats
	 * @return
	 */
	
	@GetMapping("/systemStats")
	public JSONObject systemStats (@RequestParam("uid") final String uid) {
		JSONObject systemStats = meshMonitorService.systemStatsUI(uid);
		return systemStats;
	}
	
	/**
	 * videoStats
	 * @return
	 */
	
	@GetMapping("/videoStats")
	public JSONObject video_stats (@RequestParam("uid") final String uid) {
		JSONObject systemStats = meshMonitorService.videoStatsUI(uid);
		return systemStats;
	}
	
	@GetMapping("/pathSelection")
	public JSONArray pathSelection(@RequestParam("uid") final String uid) {
		return meshMonitorService.getPathSelection(uid);
	}
	
	
	@SuppressWarnings("unchecked")
	@GetMapping("/pathSelectionHistogram")
	public List<JSONObject> pathSelectionhistogram(@RequestParam("uid") final String uid){
		JSONArray array = meshMonitorService.getPathSelectionHistogram(uid);
		if (array != null) {
			List<JSONObject> sortedArray =  sort(array,ORDER_BY_DESC);
			
			List<JSONObject>  unmodifiableStats = Collections.unmodifiableList(sortedArray);
			
			int size = unmodifiableStats.size();
			
			List<JSONObject> data = null;

			if (size >= 200) {
				data = unmodifiableStats.subList(size - 200, size);
			} else {
				data = unmodifiableStats.subList(0, size);
			}
			
			return data;
		}

		return array;
	}
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> sort (JSONArray array,String orderBy) {
		
	    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
	    
		Iterator<JSONObject> it = array.iterator();
		while (it.hasNext()) {
			JSONObject data = it.next();
			jsonValues.add(data);
		}

	    Collections.sort( jsonValues, new Comparator<JSONObject>() {
	        private static final String KEY_NAME = "lastSeen";

	        @Override
	        public int compare(JSONObject a, JSONObject b) {
	                String valA =  a.get(KEY_NAME).toString();
	                String valB =  b.get(KEY_NAME).toString();
	                
	                if (orderBy.equals("desc")) {
	                	return valB.compareTo(valA);
	                } else {
	                	return valA.compareTo(valB);
	                }
	            
	        }
	    });

	    return jsonValues;
	}
	@PostMapping("/pathselectionTest")
	public boolean pathselectionTest(@RequestBody JSONObject json) throws IOException, InterruptedException {
		
		for (int i = 0; i < 50; i++) {
			
			Thread.sleep(20);
			
			ObjectMapper mapper =new ObjectMapper();
			PathSelection payload = mapper.readValue(json.toString(),PathSelection.class);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			String utcDate = dateFormat.format(new Date());
			
			payload.setTimestamp(utcDate);
			payload.setLastSeen(System.currentTimeMillis());
			
			 meshMonitorService.updatePathSelection(payload);
		}
		
		return true;
		
	}
	
	@GetMapping("/simulationPersistence")
	public JSONObject simulationPersistence(@RequestParam("uid") final String uid) {
		return meshMonitorMQTTService.simulationPersistenceCache.get(uid);
	}
	
	
	@GetMapping("/systemStatsRemoveByUid")
	public void systemStatsRemoveByUid(@RequestParam("uid") final String uid) {
		 meshMonitorService.removeSystemStats(uid);
	}
	
	
	@GetMapping("/systemStatsClear")
	public void systemStatsClear() {
		 meshMonitorService.clearSystemStats();
	}
	
	
	@GetMapping("/videoStatsRemoveByUid")
	public void videoStatsRemoveByUid(@RequestParam("uid") final String uid) {
		 meshMonitorService.removeVideoStats(uid);
	}
	
	@GetMapping("/videoStatsClear")
	public void videoStatsClear() {
		meshMonitorService.clearVideoStats();
	}
	
	@GetMapping("/pathSelectionClear")
	public void pathSelectionClear() {
		meshMonitorService.clearPathSelectionCache();
	}
	
	
	@GetMapping("/pathSelectionHistoryClear")
	public void pathSelectionHistoryClear() {
		meshMonitorService.clearPathSelectionHistogramCache();
	}
	
	@GetMapping("/listVaps")
	public ConcurrentHashMap<String, String> listVaps() {
		return meshMonitorService.vapCache;
	}
	
}
