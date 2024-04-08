package com.semaifour.facesix.mesh.service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.data.mongo.device.Device;
import com.semaifour.facesix.data.mongo.device.DeviceService;
import com.semaifour.facesix.util.CustomerUtils;

 
@Service
public class MeshMonitorService {
	
	private @Autowired DeviceService deviceService;
	
	static Logger logger = LoggerFactory.getLogger(MeshMonitorService.class.getName());

	final static int pathSelectionRecordSize 		 = 200;
	final static int statsRecordSize 				 = 120;
	final static long statsRecordInvalidateTimestamp = 120000;
	final static long pathHistoryInvalidateTimestamp = 3600000;
	
	public final ConcurrentHashMap<String, List<Map<String, Object>>> systemStatsCache 		 = new ConcurrentHashMap<String,List<Map<String, Object>>>();
	public final ConcurrentHashMap<String, List<Map<String, Object>>> videoStatsCache 		 = new ConcurrentHashMap<String,List<Map<String, Object>>>();
	public final ConcurrentHashMap<String, PathSelection> pathSelectionCache 				 = new ConcurrentHashMap<String,PathSelection>();
	public final ConcurrentHashMap<String, HashMap<String,PathSelection>> pathSelectionHistogramCache = new ConcurrentHashMap<String,HashMap<String,PathSelection>>();
	
	public final ConcurrentHashMap<String, String> vapCache = new ConcurrentHashMap<String,String>();
	
	public void updateDeviceVapIds(Map<String, Object> map) {
		
		try {
			
			String uid = (String) map.get("uid");
			
			List<HashMap<String, Object>> vapList = (List<HashMap<String, Object>>) map.get("vap_list");
			
			if (!CollectionUtils.isEmpty(vapList)) {
				Iterator<HashMap<String, Object>> it = vapList.iterator();
				while (it.hasNext()) {
					HashMap<String, Object> vapMap = it.next();
					String vapId = (String) vapMap.get("vap_bssid");
					vapCache.put(vapId, uid);
				}
			}
		} catch (Exception e) {
			logger.error("while add bssid occured error " +e);
			e.printStackTrace();
		}
	}
	
	public boolean isTimeExceeds(long lastSeen,long inactivetime) {
		long currentTime = System.currentTimeMillis();
		// 1 minutes = 60000 milliseconds
		// 4 minutes = 240000 milliseconds 
		// 2 minutes = 120000 milliseconds 
		// 6 minutes = 360000 milliseconds
		long time_difference = currentTime - lastSeen;
		if(time_difference >= inactivetime) {
			return true;
		}
		return false;
	}

	/**
	 * Used to store system stats and video stats
	 * @param payload
	 * @return
	 */
	
	public boolean systemStats(Map<String, Object> payload) {
		
		//logger.info("systemStats Payload " + payload);
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:MM:ss");
		String date 		 = sdf.format(new Date());
		long timestamp 		 = System.currentTimeMillis();
		
		String uid = (String)payload.getOrDefault("uid","-");
		
		payload.put("lastSeen", timestamp);
		payload.put("timestamp", date);
		
		List<Map<String, Object>> data = systemStatsCache.get(uid);
		
		if (CollectionUtils.isEmpty(data)) {
			data = new ArrayList<Map<String, Object>>();
		}
		
		data.add(payload);
		
		systemStatsCache.put(uid, data);
		
		return true;
	}
	
   public boolean videoStats(Map<String, Object> payload) {
		
		//logger.info("videoStats Payload " + payload);
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:MM:ss");
		String date 		 = sdf.format(new Date());
		long timestamp 		 = System.currentTimeMillis();
		
		String uid = (String)payload.getOrDefault("uid","-");
		
		payload.put("lastSeen", timestamp);
		payload.put("timestamp", date);
		
		List<Map<String, Object>> data = videoStatsCache.get(uid);
		
		if (CollectionUtils.isEmpty(data)) {
			data = new ArrayList<Map<String, Object>>();
		}
		
		data.add(payload);
		
		videoStatsCache.put(uid, data);
		
		return true;
	}
	
	/**
	 * used to store Path Selection data
	 * @param payload
	 * @return
	 */
	public boolean pathSelection (Map<String, Object> payload) {
		
		try {
			
			//logger.info("PathSelection Payload " + payload);
			
			long timestamp = System.currentTimeMillis();
			
			ObjectMapper mapper  = new ObjectMapper();
			String payloadString = mapper.writeValueAsString(payload);
			
			PathSelection pathSelection = mapper.readValue(payloadString, PathSelection.class);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			String utcDate = dateFormat.format(new Date());
			
			pathSelection.setTimestamp(utcDate);
			pathSelection.setLastSeen(timestamp);
			
			this.updatePathSelection(pathSelection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return true;
	}

	
	public void removeVideoStats(String uid) {
		this.videoStatsCache.remove(uid);
	}
	
	public void clearVideoStats() {
		this.videoStatsCache.clear();
	}
	
	public void removeSystemStats(String uid) {
		this.systemStatsCache.remove(uid);
	}
	
	public void clearSystemStats() {
		this.systemStatsCache.clear();
	}
	
	public void clearPathSelectionCache() {
		pathSelectionCache.clear();
	}
	
	public void clearPathSelectionHistogramCache() {
		pathSelectionHistogramCache.clear();
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject systemStatsUI(String uid) {
		
		/**
		 * min,max,avg
		 */
		
		JSONObject result = new JSONObject();
		
		try {
			
			int sumCpu 			= 0;
			int sumMem 			= 0;
			long sumOfUplink 	= 0;
			long sumOfDownlink 	= 0;
			
			double avgCpu 		= 0;
			double avgMem 		= 0;
			double avgUplink 	= 0;
			double avgDownlink 	= 0;
			
			String MbpsUplink   = "0.0 mbps";
			String MbpsDownlink = "0.0 mbps";
			
			int minCpu = 0;
			int maxCpu = 0;
			
			int minMem = 0;
			int maxMem = 0;
			
			long minTx = 0;
			long maxTx = 0;
			
			long minRx = 0;
			long maxRx = 0;
			
			JSONArray cpu          = new JSONArray();
			JSONArray mem          = new JSONArray();
			JSONArray network      = new JSONArray();
			JSONArray channelUsage = new JSONArray();
			
			JSONObject dataPoints =  null;
			
			/**
			 *  current update for battery,channel
			 */
			int currChannel 	  		= 0;
			String batteryMode 	  		= "battery";
			String batteryStatus  		= "0";
			String batteryRemainingTime = "0";
			int batteryPercent 	  		= 0;
			
			List<Map<String, Object>> systemStats = systemStatsCache.get(uid);
			
			JSONObject currentStats = new JSONObject();
			JSONObject avgStats = new JSONObject();
			
			if (!CollectionUtils.isEmpty(systemStats)) {
				
				List<Map<String, Object>>  unmodifiableStats = Collections.unmodifiableList(systemStats);
				
				int size = unmodifiableStats.size();
				
				List<Map<String, Object>> data = null;

				if (size >= statsRecordSize) {
					data = unmodifiableStats.subList(size - statsRecordSize, size);
				} else {
					data = unmodifiableStats.subList(0, size);
				}

				int dataSize = data.size();
				
				ArrayList<Integer> cpuMinMax 	= new ArrayList<>(dataSize);
				ArrayList<Integer> memMinMax 	= new ArrayList<>(dataSize);
				ArrayList<Long> uplinkMinMax 	= new ArrayList<>(dataSize);
				ArrayList<Long> downlinkMinMax  = new ArrayList<>(dataSize);
				
				Iterator<Map<String, Object>> object = data.iterator();
				
				Map<String, Object> recentRecord = data.get(dataSize-1);
				
				if (recentRecord.containsKey("channel")) 
						currChannel = Integer.parseInt(recentRecord.get("channel").toString());
				if (recentRecord.containsKey("battery_percentage"))
						batteryPercent = Integer.parseInt(recentRecord.get("battery_percentage").toString());
				if (recentRecord.containsKey("battery_status"))
						batteryStatus  = recentRecord.get("battery_status").toString();
				if (recentRecord.containsKey("power_mode"))
						batteryMode    = (String)recentRecord.get("power_mode");
				if (recentRecord.containsKey("battery_remaining_time")) {
					batteryRemainingTime = recentRecord.get("battery_remaining_time").toString();
					long battRemaingTime = Long.valueOf(batteryRemainingTime);
					if (!batteryMode.equals("AC") && battRemaingTime > 0) {
						batteryRemainingTime = CustomerUtils.secondsToHHMMSS(battRemaingTime);
					}
				}		
				while (object.hasNext()) {
					
					Map<String, Object> obj = object.next();
					
					if (obj.containsKey("cpu_percentage")) {
						
						int cpuPercent = Integer.parseInt(obj.get("cpu_percentage").toString());
						
						sumCpu += cpuPercent;
						cpuMinMax.add(cpuPercent);
						
						dataPoints = new JSONObject();
						dataPoints.put("cpu", cpuPercent);
						dataPoints.put("time", obj.get("timestamp"));
						cpu.add(dataPoints);
					}
					
					if (obj.containsKey("ram_percentage")) {
						
						int memPercent = Integer.parseInt(obj.get("ram_percentage").toString());
						
						sumMem += memPercent;
						memMinMax.add(memPercent);
						
						dataPoints = new JSONObject();
						dataPoints.put("mem", memPercent);
						dataPoints.put("time", obj.get("timestamp"));
						mem.add(dataPoints);
						
					}
					
					if (obj.containsKey("uplink") && obj.containsKey("downlink")) {
						
						String uplinkStr = obj.get("uplink").toString();
						long longuplink = Long.valueOf(uplinkStr);
						
						sumOfUplink += longuplink;
						uplinkMinMax.add(longuplink);
						
						String downlinkStr = obj.get("downlink").toString();
						long longDownlink = Long.valueOf(downlinkStr);
						
						sumOfDownlink += longDownlink;
						downlinkMinMax.add(longDownlink);
						
						dataPoints = new JSONObject();
						dataPoints.put("uplink", longuplink);
						dataPoints.put("downlink", longDownlink);
						dataPoints.put("time", obj.get("timestamp"));
						network.add(dataPoints);
					}
					if (obj.containsKey("channel_usage")) {
						
						String channel_usage = obj.get("channel_usage").toString();
						int channelUsageInt  = Integer.valueOf(channel_usage);
						
						dataPoints = new JSONObject();
						dataPoints.put("channelUsage", channelUsageInt);
						dataPoints.put("time", obj.get("timestamp"));
						channelUsage.add(dataPoints);
					}
				}
				
				
				
				if (sumCpu != 0) {
					avgCpu = sumCpu / dataSize;
				}
				if (sumMem != 0) {
					avgMem = sumMem / dataSize;
				}

				if (sumOfUplink != 0) {
					avgUplink = sumOfUplink / dataSize;
					MbpsUplink = CustomerUtils.bytesToMbps(avgUplink);
				}
				if (sumOfDownlink != 0) {
					avgDownlink = sumOfDownlink / dataSize;
					MbpsDownlink = CustomerUtils.bytesToMbps(avgDownlink);
				}
				
				 Collections.sort(cpuMinMax);
				 Collections.sort(memMinMax);
				 Collections.sort(uplinkMinMax);
				 Collections.sort(downlinkMinMax);
				
				 minCpu = cpuMinMax.get(0);
				 maxCpu = cpuMinMax.get(cpuMinMax.size()-1);
				
				 minMem = memMinMax.get(0);
				 maxMem = memMinMax.get(memMinMax.size()-1);
				
				 minTx = uplinkMinMax.get(0);
				 maxTx = uplinkMinMax.get(uplinkMinMax.size()-1);
				
				 minRx = downlinkMinMax.get(0);
				 maxRx = downlinkMinMax.get(downlinkMinMax.size()-1);
			}
			
			
			currentStats.put("currChannel", 		currChannel);
			currentStats.put("currBatteryPercent", 	batteryPercent);
			currentStats.put("currBatteryStatus", 	batteryStatus);
			currentStats.put("currPowerMode", 		batteryMode);
			currentStats.put("currbattRemainingTime", batteryRemainingTime);
			
			/**
			 * avg cpu,mem,network
			 */
			
			avgStats.put("avgCpu", 	  avgCpu);
			avgStats.put("avgMem", 	  avgMem);
			avgStats.put("avgUplink", 	  CustomerUtils.decimalFormat(avgUplink));
			avgStats.put("avgDownlink", 	  CustomerUtils.decimalFormat(avgDownlink));
			avgStats.put("avgMbpsUplink",   MbpsUplink);
			avgStats.put("avgMbpsDownlink", MbpsDownlink);
			
			/**
			 * Min and max values for cpu,mem,network
			 */
			
			avgStats.put("minCpu", 	  minCpu);
			avgStats.put("maxCpu", 	  maxCpu);
			avgStats.put("minMem", 	  minMem);
			avgStats.put("maxMem", 	  maxMem);
			avgStats.put("minUplink", 	minTx);
			avgStats.put("maxUplink", 	maxTx);
			avgStats.put("minDownlink", 	 minRx);
			avgStats.put("maxDownlink", 	 maxRx);
			
			
			/**
			 * final data point result
			 */
			
			result.put("cpu", 			cpu);
			result.put("mem", 			mem);
			result.put("network", 		network);
			result.put("chennelusage",  channelUsage);
			result.put("currentStats",   currentStats);
			result.put("systemStatsAvg", avgStats);
		} catch (Exception e) {
			logger.error("system stats processing error " +e);
			e.printStackTrace();
		}
		
		
		
		return result;
	}

	@SuppressWarnings("unchecked")
	public JSONObject videoStatsUI(String uid) {
		
		/**
		 * min,max,avg
		 */
		
		JSONObject result = new JSONObject();
		
		try {
			
			double sumOfAvLatency 	= 0;
			int sumOfFrameDrops 	= 0;
			long sumOfAudioBuffSize = 0;
			long sumOfVideoBuffSize = 0;
			float sumOfLagDuration 	= 0;
			
			double minAvLatency = 0;
			double maxAvLatency = 0;
			
			int minFrameDrops = 0;
			int maxFrameDrops = 0;
			
			long minAudioBuffSize = 0;
			long maxAudioBuffSize = 0;
			
			long minVideoBuffSize = 0;
			long maxVideoBuffSize = 0;
			
			float minLagDuration = 0;
			float maxLagDuration = 0;
			
			double avgAvLatency 	    = 0;
			double avgFrameDropLatency 	= 0;
			double avgAudioBuffTime 	= 0;
			double avgVideoBuffTime  	= 0;
			double avgLagDuration 		= 0;
			
			
			
			JSONArray avLatency      = new JSONArray();
			JSONArray frameDrops     = new JSONArray();
			JSONArray audioVideoBuff = new JSONArray();
			
			JSONArray lagDuration = new JSONArray();
			
			JSONObject dataPoints =  null;
			
			/**
			 *  current updates
			 */
			
			String filename 	   = "NA";
			String resolution 	   = "0";
			String frameRate  	   = "0";
			String audioBitRate    = "0";
			String videoDuration   = "0";
			String initialBuffTime = "0";
			String videoBitRate    = "0";
			
			
			List<Map<String, Object>> videoStats = videoStatsCache.get(uid);
			
			JSONObject currentStats  = new JSONObject();
			JSONObject avgVideoStats = new JSONObject();
			
			if (!CollectionUtils.isEmpty(videoStats)) {
				
				List<Map<String, Object>>  unmodifiableStats = Collections.unmodifiableList(videoStats);
				
				int size = unmodifiableStats.size();
				
				List<Map<String, Object>> data = null;

				if (size >= statsRecordSize) {
					data = unmodifiableStats.subList(size - statsRecordSize, size);
				} else {
					data = unmodifiableStats.subList(0, size);
				}
				
				int dataSize = data.size();

				ArrayList<Double> avLatencyMinMax 	 = new ArrayList<>(dataSize);
				ArrayList<Integer> frameDropsMinMax  = new ArrayList<>(dataSize);
				ArrayList<Long> audioBuffSizeMinMax  = new ArrayList<>(dataSize);
				ArrayList<Long> videoBuffSizeMinMax  = new ArrayList<>(dataSize);
				ArrayList<Float> lagDurationMinMax   = new ArrayList<>(dataSize);
				
				Iterator<Map<String, Object>> object = data.iterator();
				
				Map<String, Object> recentRecord = data.get(dataSize-1);
				
				if (recentRecord.containsKey("filename")) 
					filename = (String)recentRecord.get("filename");
				if (recentRecord.containsKey("resolution"))
					resolution = (String)recentRecord.get("resolution");
				if (recentRecord.containsKey("frame_rate"))
					frameRate  = recentRecord.get("frame_rate").toString();
				if (recentRecord.containsKey("audio_bitrate"))
					audioBitRate    = recentRecord.get("audio_bitrate").toString();
				if (recentRecord.containsKey("video_duration"))
					videoDuration    = recentRecord.get("video_duration").toString();
				if (recentRecord.containsKey("initial_buffer_time"))
					initialBuffTime = recentRecord.get("initial_buffer_time").toString();
					initialBuffTime = CustomerUtils.decimalFormat(Double.valueOf(initialBuffTime));
				if (recentRecord.containsKey("video_bitrate"))
					videoBitRate    = recentRecord.get("video_bitrate").toString();
				
				while (object.hasNext()) {
					
					Map<String, Object> obj = object.next();
					
					if (obj.containsKey("av_latency")) {
						
						double latency = Double.parseDouble(obj.get("av_latency").toString());
						
						sumOfAvLatency += latency;
						avLatencyMinMax.add(latency);
						
						dataPoints = new JSONObject();
						
						dataPoints.put("avLatency", CustomerUtils.decimalFormat(latency));
						dataPoints.put("time", obj.get("timestamp"));
						avLatency.add(dataPoints);
					}
					
					if (obj.containsKey("frame_drops")) {
						
						int frameDrop = Integer.parseInt(obj.get("frame_drops").toString());
						
						sumOfFrameDrops += frameDrop;
						frameDropsMinMax.add(frameDrop);
						
						dataPoints = new JSONObject();
						dataPoints.put("frameDrop", frameDrop);
						dataPoints.put("time", obj.get("timestamp"));
						frameDrops.add(dataPoints);
						
					}
					
					if (obj.containsKey("video_buffer_size") && obj.containsKey("audio_buffer_size")) {
						
						String videoBufttSizeStr = obj.get("video_buffer_size").toString();
						long videoBuffSizeLong = Long.valueOf(videoBufttSizeStr);
						
						sumOfVideoBuffSize += videoBuffSizeLong;
						videoBuffSizeMinMax.add(videoBuffSizeLong);
						
						String audioBuffSizeStr = obj.get("audio_buffer_size").toString();
						long audioBuffSizeLong = Long.valueOf(audioBuffSizeStr);
						
						sumOfAudioBuffSize += audioBuffSizeLong;
						audioBuffSizeMinMax.add(audioBuffSizeLong);
						
						dataPoints = new JSONObject();
						
						dataPoints.put("videoBuffSize", videoBuffSizeLong);
						dataPoints.put("audioBuffSize", audioBuffSizeLong);
						dataPoints.put("time", obj.get("timestamp"));
						
						audioVideoBuff.add(dataPoints);
					}
					if (obj.containsKey("lag_duration")) {
						
						String lagDurationStr   = obj.get("lag_duration").toString();
						float lagDurationFloat  = Float.parseFloat(lagDurationStr);
						
						sumOfLagDuration += lagDurationFloat;
						lagDurationMinMax.add(lagDurationFloat);
						
						dataPoints = new JSONObject();
						dataPoints.put("lagDuration", lagDurationFloat);
						dataPoints.put("time", obj.get("timestamp"));
						lagDuration.add(dataPoints);
					}
				}

				if (sumOfAvLatency != 0) {
					avgAvLatency = sumOfAvLatency / dataSize;
				}
				if (sumOfFrameDrops != 0) {
					avgFrameDropLatency = sumOfFrameDrops / dataSize;
				}

				if (sumOfAudioBuffSize != 0) {
					avgAudioBuffTime = sumOfAudioBuffSize / dataSize;
				}
				if (sumOfVideoBuffSize !=0) {
					avgVideoBuffTime = sumOfVideoBuffSize / dataSize;
				}

				if (sumOfLagDuration != 0) {
					avgLagDuration = sumOfLagDuration / dataSize;
				}
				
				 Collections.sort(avLatencyMinMax);
				 Collections.sort(frameDropsMinMax);
				 Collections.sort(audioBuffSizeMinMax);
				 Collections.sort(videoBuffSizeMinMax);
				 Collections.sort(lagDurationMinMax);
				 
				
				 minAvLatency = avLatencyMinMax.get(0);
				 maxAvLatency = avLatencyMinMax.get(avLatencyMinMax.size()-1);
				
				 minFrameDrops = frameDropsMinMax.get(0);
				 maxFrameDrops = frameDropsMinMax.get(frameDropsMinMax.size()-1);
				
				 minAudioBuffSize = audioBuffSizeMinMax.get(0);
				 maxAudioBuffSize = audioBuffSizeMinMax.get(audioBuffSizeMinMax.size()-1);
				 
				 minVideoBuffSize = videoBuffSizeMinMax.get(0);
				 maxVideoBuffSize = videoBuffSizeMinMax.get(videoBuffSizeMinMax.size()-1);
				 
				 minLagDuration = lagDurationMinMax.get(0);
				 maxLagDuration  = lagDurationMinMax.get(lagDurationMinMax.size()-1);
				 
				
				
			}
			
			
			currentStats.put("fileName", 		filename);
			currentStats.put("resolution", 		resolution);
			currentStats.put("frameRate", 		frameRate);
			currentStats.put("audioBitRate", 	audioBitRate);
			currentStats.put("videoBitRate", 	videoBitRate);
			currentStats.put("videoDuration", 	videoDuration);
			currentStats.put("initialBuffTime", initialBuffTime);

			/**
			 * avg
			 */
			
			avgVideoStats.put("avgAvLatency", 	       CustomerUtils.decimalFormat(avgAvLatency));
			avgVideoStats.put("avgFrameDropLatency",   CustomerUtils.decimalFormat(avgFrameDropLatency));
			avgVideoStats.put("avgAudioBuffTime", 	   CustomerUtils.decimalFormat(avgAudioBuffTime));
			avgVideoStats.put("avgVideoBuffTime", 	   CustomerUtils.decimalFormat(avgVideoBuffTime));
			avgVideoStats.put("avgLagDuration", 	   CustomerUtils.decimalFormat(avgLagDuration));
			
			avgVideoStats.put("minAvLatency", 	  	   CustomerUtils.decimalFormat(minAvLatency));
			avgVideoStats.put("maxAvLatency", 	  	   CustomerUtils.decimalFormat(maxAvLatency));
			
			avgVideoStats.put("minFrameDrops", 		   CustomerUtils.decimalFormat(minFrameDrops));
			avgVideoStats.put("maxFrameDrops", 		   CustomerUtils.decimalFormat(maxFrameDrops));
			
			avgVideoStats.put("minAudioBuffSize", 	   CustomerUtils.decimalFormat(minAudioBuffSize));
			avgVideoStats.put("maxAudioBuffSize", 	   CustomerUtils.decimalFormat(maxAudioBuffSize));
			
			avgVideoStats.put("minVideoBuffSize", 	   CustomerUtils.decimalFormat(minVideoBuffSize));
			avgVideoStats.put("maxVideoBuffSize", 	   CustomerUtils.decimalFormat(maxVideoBuffSize));
			
			avgVideoStats.put("minLagDuration", 	   CustomerUtils.decimalFormat(minLagDuration));
			avgVideoStats.put("maxLagDuration", 	   CustomerUtils.decimalFormat(maxLagDuration));
			
			
			/**
			 * final data point result
			 */
			
			result.put("avLatency", 		 avLatency);
			result.put("frameDrops", 		 frameDrops);
			result.put("audioVideoBuffSize", audioVideoBuff);
			result.put("lagDuration",        lagDuration);
			result.put("currentStats",       currentStats);
			result.put("avgStats",           avgVideoStats);
		} catch (Exception e) {
			logger.error("video stats processing error " +e);
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	public int getRecordSize(String uid) {

		int size = 0;
		
		try {
			
			if (!pathSelectionHistogramCache.isEmpty()) {
				Set<String> uids = this.pathSelectionHistogramCache.keySet();
				if (!CollectionUtils.isEmpty(uids)) {
					for (String keys : uids) {
						String keyUid = keys.split("#")[0];
						if (keyUid.equals(uid)) {
							size++;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("while calulating records size occured error " +e);
			e.printStackTrace();
		}

		return size;
	}
	
	public boolean updatePathSelection(PathSelection payload) {

		try {
			
			String opcode = payload.getOpcode();
			String dest   = payload.getDst();
			String uid    = payload.getUid();
			
			String pathSelectionKey = uid + "#" + dest;
			
			/*
			 * Path selection Histogram
			 */
			
			HashMap<String, PathSelection> pathSelectionMetrics = new HashMap<String,PathSelection>();
			pathSelectionMetrics.put(pathSelectionKey, payload);
			
			String pathHistoryKey = uid+"#"+payload.getLastSeen();
			this.pathSelectionHistogramCache.put(pathHistoryKey,pathSelectionMetrics);
			
			int recordSize = getRecordSize(uid);
			
			if (recordSize >= pathSelectionRecordSize) {
				this.removePathSelectionHistoryBasedOnRecordSize(uid);
			}

			if (opcode.equals("mesh_path_add")) {
				pathSelectionCache.put(pathSelectionKey, payload);
			} else if (opcode.equals("mesh_path_del")) {
				if (pathSelectionCache.containsKey(pathSelectionKey)) {
					pathSelectionCache.remove(pathSelectionKey);
				} else {
					logger.info("Path remove dest not found  " +dest);
				}
			} else if (opcode.equals("mesh_path_update")) {
				
				if (pathSelectionCache.containsKey(pathSelectionKey)) {
					PathSelection prevPathSelection = pathSelectionCache.get(pathSelectionKey);
					if (prevPathSelection != null) {
		
						/*
						 * Prev hop and score
						 */
						prevPathSelection.setPrev_hop(prevPathSelection.getNext_hop());
						prevPathSelection.setPrev_score(prevPathSelection.getScore());
						prevPathSelection.setPrev_num_hops(prevPathSelection.getNum_hops());
						
						/*
						 * Current hop and score
						 */
						
						prevPathSelection.setNext_hop(payload.getNext_hop());
						prevPathSelection.setScore(payload.getScore());
						prevPathSelection.setNum_hops(payload.getNum_hops());
						
						pathSelectionCache.put(pathSelectionKey, prevPathSelection);
						
						//logger.info("mesh_path_update avilable " +prevPathSelection.toString());
					}
					
				} else {
					/*
					 * in case that device already connected  or when cloud was down 
					 */
					pathSelectionCache.put(pathSelectionKey, payload);
				}
			}
		} catch (Exception e) {
			logger.error("while path selection update occured error " +e);
			e.printStackTrace();
		}

		return true;

	}
	
	/**
	 * Used to invalidate record based  cache size
	 * @param pathSelectionHistogramCache
	 * @return
	 */
	
	private boolean removePathSelectionHistoryBasedOnRecordSize(String uid) {
			 
		try {
			
			Set<String> mykeys = new HashSet<String>();

			Set<String> timestamp = this.pathSelectionHistogramCache.keySet();

			if (!CollectionUtils.isEmpty(timestamp)) {
				for (String keys : timestamp) {
					String keyUid = keys.split("#")[0];
					if (keyUid.equals(uid)) {
						mykeys.add(keys);
					}
				}

				List<String> uids = new ArrayList<String>(mykeys);
				Collections.sort(uids, new Comparator<String>() {
					@Override
					public int compare(String a, String b) {
						return a.compareTo(b);
					}
				});

				uids.forEach(mykeyuid -> {
					if (this.pathSelectionHistogramCache.containsKey(mykeyuid)) {
						HashMap<String, PathSelection> path = this.pathSelectionHistogramCache.get(mykeyuid);
						if (path != null) {
							for (Map.Entry me : path.entrySet()) {
								int cacheSize = this.getRecordSize(uid);
								if (cacheSize > pathSelectionRecordSize) {
									this.pathSelectionHistogramCache.remove(mykeyuid);
									//logger.info("removed key " + mykeyuid);
								}
							}
						}
					}
				});

			}
			
		} catch (Exception e) {
			logger.error("while path selection histroy delete occured error " +e);
			e.printStackTrace();
		}

		return true;
	}
	

	@SuppressWarnings("unchecked")
	public JSONArray getPathSelection(String uid) {

		JSONArray array = new JSONArray();

		try {
			
			ConcurrentHashMap<String, PathSelection> payload = this.pathSelectionCache;

			if (!payload.isEmpty()) {
				Set<String> keys = payload.keySet();
				keys.forEach(key -> {
					if (key.contains("#")) {
						
						String uuid = key.split("#")[0];
						PathSelection path = this.pathSelectionCache.get(key);
						
						if (uuid.equals(uid) && path !=null) {
							
								JSONObject object = new JSONObject();
								
								String dest =  path.getDst();
								String destPathName = pathName(dest);
								
								String nextHop = path.getNext_hop();
								String nextHopName = pathName(nextHop);
								
								String prevhop = path.getPrev_hop();
								String prevhopName = "-";
								
								if (!StringUtils.isEmpty(prevhop)) {
									prevhopName = pathName(prevhop);
								}
								
								object.put("uid",  			path.getUid());
								object.put("destination",   destPathName);
								object.put("nextHop", 		nextHopName);
								object.put("prevHop", 		prevhopName);
								
								object.put("score", 		path.getScore());
								object.put("prevScore", 	path.getPrev_score() == 0 ? "-" : path.getPrev_score());
								object.put("timeStamp", 	path.getTimestamp());
								object.put("numHope", 		path.getNum_hops());
								object.put("prevNumHope", 	path.getPrev_num_hops());
								
								array.add(object);
						}
					}

				});
			}
		} catch (Exception e) {
			logger.error("while iterate pathSelection occured error " + e);
			e.printStackTrace();
		}	

		return array;
	}
	
	ConcurrentHashMap<String,String> deviceNameMap = new ConcurrentHashMap<String,String>();
	
	public String pathName(String vapId) {
		
		String deviceName = "";
		
		if (StringUtils.isEmpty(vapId)) {
			return "-";
		} else {
			try {
			
				if (!vapCache.isEmpty()) {
					if (vapCache.containsKey(vapId)) {
						String myUid = (String) this.vapCache.get(vapId);
						if (StringUtils.isEmpty(myUid)) {
							deviceName = vapId;
						} else {
							if (deviceNameMap.containsKey(myUid)) {
								deviceName = deviceNameMap.get(myUid);
							} else {
								Device device = deviceService.findOneByUid(myUid);
								if (device != null) {
									deviceName = device.getName();
								} else {
									deviceName = vapId;
								}
								deviceNameMap.put(myUid, deviceName);
							}
						}
					} 
				}
				
			} catch (Exception e) {
				logger.error("while getting path name occured error " + e  + " vapId " +vapId + " vapCache " +vapCache);
				e.printStackTrace();
			}	
		}
		

		if (StringUtils.isEmpty(deviceName)) {
			deviceName = vapId;
		}

		return deviceName;
	}

	public ConcurrentHashMap<String, PathSelection> pathSelectionPayload() {
		return this.pathSelectionCache;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getPathSelectionHistogram(String uid) {
		
		JSONArray array = new JSONArray();
		
		try {
			
			ConcurrentHashMap<String, HashMap<String,PathSelection>> payload = this.pathSelectionHistogramCache;

			if (!payload.isEmpty()) {

				for (Map.Entry me : payload.entrySet()) {
					
					String timeStampKey = (String)me.getKey();
					HashMap<String,PathSelection> data = payload.get(timeStampKey);
					
					if (data.isEmpty()) {
						continue;
					}

					for (Map.Entry val : data.entrySet()) {
						
						String key 			= (String)val.getKey();
						PathSelection path = data.get(key);
						
						if (!key.contains("#")) {
							continue;
						}

						String uuid = key.split("#")[0];
						
						if (uuid.equals(uid) && path !=null) {
							
								JSONObject object = new JSONObject();
								
								String dest =  path.getDst();
								String destPathName = pathName(dest);
								
								String nextHop = path.getNext_hop();
								String nextHopName = pathName(nextHop);
								
								String prevhop = path.getPrev_hop();
								String prevhopName = "-";
								
								if (!StringUtils.isEmpty(prevhop)) {
									prevhopName = pathName(prevhop);
								}
								
								object.put("uid",  			path.getUid());
								object.put("destination",   destPathName);
								object.put("nextHop", 		nextHopName);
								object.put("prevHop", 		prevhopName);
								
								object.put("score", 		path.getScore());
								object.put("prevScore", 	path.getPrev_score() == 0 ? "-" : path.getPrev_score());
								object.put("timeStamp", 	path.getTimestamp());
								object.put("lastSeen", 	    path.getLastSeen());
								object.put("numHope", 		path.getNum_hops());
								object.put("prevNumHope", 	path.getPrev_num_hops());
								
								String opcode = path.getOpcode();

								if ("mesh_path_add".equals(opcode)) {
									opcode = "ADD";
								} else if ("mesh_path_del".equals(opcode)) {
									opcode = "DELETE";
								} else if ("mesh_path_update".equals(opcode)) {
									opcode = "UPDATE";
								} else {
								opcode = "-";
							}

							object.put("event", opcode);
							array.add(object);
						}
					}

				}
			}
		} catch(Exception e) {
			logger.error("while iterate path selection history occured error " +e);
			e.printStackTrace();
		}
		
		
		return array;
	}

	@Scheduled(fixedDelay = 5000)
	public void makeClient_inactive() {
			try {
				
				
				/*
				 * System stats cache
				 */
				
				if (systemStatsCache != null) {
					Set<String> keys = systemStatsCache.keySet();
					keys.forEach(uid->{
						if (systemStatsCache.containsKey(uid)) {
							List<Map<String, Object>> peer_map = systemStatsCache.get(uid);
							if (peer_map != null) {
								for (int i = 0; i < peer_map.size(); i++) {
									Map<String, Object> details = peer_map.get(i);
									if (details.containsKey("lastSeen")) {
									long last_seen = (long) details.get("lastSeen");
									boolean timeExceeds = isTimeExceeds(last_seen,statsRecordInvalidateTimestamp);
									if (timeExceeds) {
										systemStatsCache.get(uid).remove(systemStatsCache.get(uid).get(i));
										//logger.info("removed element index " +i + " last_seen " +last_seen );
										}
									}
								}
							}
						}
					});
				}
				
				/*
				 * Video stats cache
				 */
				
				if (videoStatsCache != null) {
					Set<String> keys = videoStatsCache.keySet();
					keys.forEach(uid->{
						if (videoStatsCache.containsKey(uid)) {
							List<Map<String, Object>> peer_map = videoStatsCache.get(uid);
							if (peer_map != null) {
								for (int i = 0; i < peer_map.size(); i++) {
									Map<String, Object> details = peer_map.get(i);
									if (details.containsKey("lastSeen")) {
										long last_seen = (long) details.get("lastSeen");
										boolean timeExceeds = isTimeExceeds(last_seen,statsRecordInvalidateTimestamp);
										if (timeExceeds) {
											videoStatsCache.get(uid).remove(videoStatsCache.get(uid).get(i));
										}
									}
								}
							}
						}
					});
				}
				
				
				/*
				 * Path Selection Histogram
				 */

				if (pathSelectionHistogramCache != null) {
					Set<String> timekeys = pathSelectionHistogramCache.keySet();
					timekeys.forEach(key -> {
						if (pathSelectionHistogramCache.containsKey(key)) {
							HashMap<String, PathSelection> path = pathSelectionHistogramCache.get(key);
							if (path != null) {
								for (Map.Entry me : path.entrySet()) {
									String uidKey = (String) me.getKey();
									PathSelection pathSelection = path.get(uidKey);
									if (pathSelection != null) {
										long last_seen = pathSelection.getLastSeen();
										boolean timeExceeds = isTimeExceeds(last_seen, pathHistoryInvalidateTimestamp);
										if (timeExceeds) {
											pathSelectionHistogramCache.remove(key);
										}
									}
								}
							}
						}
					});
				}

		} catch (Exception e) {
			logger.error("while cache invalidator occured error " + e);
			e.printStackTrace();
}

	}

}