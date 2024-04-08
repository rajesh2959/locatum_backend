package com.semaifour.facesix.report.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semaifour.facesix.report.data.ReportLabels;
import com.semaifour.facesix.report.data.ReportVisuals;
import com.semaifour.facesix.util.CustomerUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReportVisualsResponseUtil {
	
	static Logger LOG = LoggerFactory.getLogger(ReportVisualsResponseUtil.class.getName());
	
	private final static DateFormat ES_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
	
	private final static String strArrTimeSlice[] = { "Time_Slice", "12 - 1am", "1 - 2am", "2 - 3am", "3 - 4am", "4 - 5am",
			"5 - 6am", "6 - 7am", "7 - 8am", "8 - 9am", "9 - 10am", "10 - 11am", "11am - 12pm", "12 - 1pm", "1 - 2pm",
			"2 - 3pm", "3 - 4pm", "4 - 5pm", "5 - 6pm", "6 - 7pm", "7 - 8pm", "8 - 9pm", "9 - 10pm", "10 - 11pm",
			"11pm - 12am" };

	//@Autowired
	//CustomerUtils customerUtils;
	
	public static void buildPreviewResponse(ReportVisuals visualObject, JSONObject resultQuery, JSONObject previewContent, 
			String strFromTimeESFormat, String strToTimeESFormat, boolean success) throws Exception {
		if (success) {

			boolean isAttendence = false;
			boolean isTableChart = false;
			JSONArray columnArr = new JSONArray();

			JSONArray body = (JSONArray) resultQuery.get("body");
			JSONObject content = (JSONObject) body.get(0);
			previewContent.put("status", true);
			previewContent.put("id", visualObject.getId());

			// Formatting the output data before sending the response
			if (content.containsKey("data")) {
				
				SimpleDateFormat reportDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				SimpleDateFormat reportDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				
				JSONArray dataArr = (JSONArray) content.get("data");
				
				String units = "hours";
				double factor = 60 * 60.0;
				
				if (dataArr != null && dataArr.size() > 0) {
					
				List<ReportLabels> metrics = visualObject.getMetrics();
				List<ReportLabels> buckets = visualObject.getBuckets();
				ReportLabels metric = null;
				ReportLabels bucket = null;
				

				if (metrics != null && metrics.size() > 0) {
					metric = metrics.get(0);
					String operation = metric.getAggOperation();
					String bucketStr = "";
					if (buckets != null && buckets.size() > 0) {
						bucket = buckets.get(0);
						bucketStr = bucket.getFieldName();
					}
					boolean isGeoFence = bucketStr.equalsIgnoreCase("geofence") ? true : false;

					if (operation.equalsIgnoreCase("avg timespent") && !isGeoFence) {
						JSONArray jsonValueArr = dataArr.getJSONArray(1);
						int daysInBetween = daysInBetween(strFromTimeESFormat, strToTimeESFormat);
						if (daysInBetween > 0) {
							for (int i = 1; i < jsonValueArr.size(); i++) {
								String avgData = "0";
								String strValue = jsonValueArr.getString(i);
								if (StringUtils.isNotEmpty(strValue) && !strValue.equals("null")) {
									avgData = Long.toString(
											Long.parseLong(jsonValueArr.getString(i)) / daysInBetween(strFromTimeESFormat, strToTimeESFormat));
								}
								jsonValueArr.set(i, avgData);
							}
						}
					}

					if ((operation.equalsIgnoreCase("total timespent")
							|| operation.equalsIgnoreCase("avg timespent")) && !isGeoFence) {
						JSONArray jsonValueArr = dataArr.getJSONArray(1);
						if (daysInBetween(strFromTimeESFormat, strToTimeESFormat) <= 1) {
							units = "minutes";
							factor = 60.0;
						}
						for (int i = 1; i < jsonValueArr.size(); i++) {
							double valueInDecimals = 0;
							String strValue = jsonValueArr.getString(i);
							if (StringUtils.isNotEmpty(strValue) && !strValue.equals("null")) {
								valueInDecimals = Long.parseLong(strValue) / factor;
							}
							String avgData = Double.toString((double) Math.round(valueInDecimals * 100) / 100);
							jsonValueArr.set(i, avgData);
						}
					}
					
					if (operation.equalsIgnoreCase("Attendance")) {
						
						JSONArray elpArray = new JSONArray();
						elpArray.add(0, "Time Spent");

						isAttendence = true;

						JSONArray tagArray 	   = (JSONArray) dataArr.get(0);
						JSONArray inTimeArray  = (JSONArray) dataArr.get(1);
						JSONArray outTimeArray = (JSONArray) dataArr.get(2);
						

						// clear query response payload and make UI formated Date format 
						dataArr.clear();
						
						int count = 0;
						
						
						for (int i = 0; i < inTimeArray.size(); i++) {

							Object inTimeObject = inTimeArray.get(i);
							String inTime 		= String.valueOf(inTimeObject);
							if (!StringUtils.isEmpty(inTime) && !inTime.equalsIgnoreCase("In_Time") && !inTime.equals("null")) {
								count++;
								String days = "-";
								if (outTimeArray.get(i) != null) {
									String outTime = (String) outTimeArray.get(i);
									// LOG.info("inTime" + inTime + " outTime " + outTime);
									if (!StringUtils.isEmpty(outTime) && !StringUtils.isEmpty(inTime)) {
										Date outTimeDate = reportDateParser.parse(outTime);
										Date inTimeDate = reportDateParser.parse(inTime);
										long elpSeconds = CustomerUtils.calculateElapsedTime(inTimeDate,outTimeDate);
										days = CustomerUtils.secondsto_hours_minus_days(elpSeconds);

									}
								}
								elpArray.add(count, days);
								
								String trimmedInTime = reportDateFormat.format(reportDateParser.parse(inTime));
								inTimeArray.set(i, trimmedInTime);

							}
						}
						
						
						for (int i = 0; i < outTimeArray.size(); i++) {
							String outTime 		  = (String) outTimeArray.get(i);
							String trimmedOutTime = "-";

							if (!StringUtils.isEmpty(outTime)) {
								if (!outTime.equals("Out_Time")) {
									trimmedOutTime = reportDateFormat.format(reportDateParser.parse(outTime));
								} else {
									trimmedOutTime = outTime;
								}
							}
							outTimeArray.set(i, trimmedOutTime);
						}

						
						dataArr.add(0, tagArray);
						dataArr.add(1, inTimeArray);
						dataArr.add(2, outTimeArray);
						dataArr.add(3, elpArray);
					}

					/**
					 * occupancy_geofence
					 * 
					 * 
					 * 1.Create Map (K,V) K = time slice of the day (12am-1am,1am-2am,2am-3am .....)
					 * V = ArrayList of empty minutes (0,60)
					 * 
					 * 2.Convert elp seconds to minutes
					 * 
					 * 
					 */

					if (operation.equalsIgnoreCase("occupancy_geofence")) {
						
						isTableChart = true;
						columnArr.add(strArrTimeSlice[0]);
						
						HashMap<String, HashMap<String, ArrayList<Integer>>> recordMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

						Iterator<JSONArray> occupancy_geofenceIter = dataArr.iterator();

						while (occupancy_geofenceIter.hasNext()) {

							JSONArray occupancy_geofence = occupancy_geofenceIter.next();

							String fenceName = (String) occupancy_geofence.get(0);
							String strEntryTime = (String) occupancy_geofence.get(2);

							long seconds = 0;

							if (occupancy_geofence.get(1) != null) {
								seconds = Long.valueOf(occupancy_geofence.get(1).toString());
							}

							long elpMinutes 	   = (seconds) / 60;
							long roundedElpMinutes = elpMinutes + 1;

							DateFormat parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
							SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

							String strSliceDate = sdf.format(parse.parse(strEntryTime));

							//System.out.println("roundedElpMinutes " +roundedElpMinutes + " fenceName " +fenceName);
							//System.out.println("fenceName :  " + fenceName + " strEntryTime :  " + strEntryTime
							//		+ " roundedElpMinutes : " + roundedElpMinutes + " strSliceDate "
							//		+ strSliceDate);

							String hh = strSliceDate.split(":")[0];
							String mm = strSliceDate.split(":")[1];

							int index = Integer.parseInt(mm);
							HashMap<String, ArrayList<Integer>> occupancyInitializedMap = null;
							if (recordMap.containsKey(fenceName)) {
								occupancyInitializedMap = recordMap.get(fenceName);
							} else {
								occupancyInitializedMap = getOccupancyInitializedMap();
							}

							int intValueHH = Integer.parseInt(hh);
							String strTimeSlice = strArrTimeSlice[intValueHH + 1];

							ArrayList<Integer> value = occupancyInitializedMap.get(strTimeSlice);
							//System.out.println("occupancyInitializedMap " + occupancyInitializedMap);
							//System.out.println("hh " + intValueHH);
							//System.out.println("index " + index);
							//System.out.println("before value " + value);

							value.set(index, 1);
							// update elp values in minutes
							int count = 0;
							int hourCount = 0;
							

							for (int i = 0; i < roundedElpMinutes - 1; i++) {
								count++;
								
								int range = index + count;
								//System.out.println("occupancy_geofence range" +range);
								if (range >= 60) {
									hourCount ++;
									int arrayLen = intValueHH + hourCount +1;
									if (arrayLen < strArrTimeSlice.length) {
										strTimeSlice = strArrTimeSlice[arrayLen];
										value = occupancyInitializedMap.get(strTimeSlice);
									}
									index = 0;
									count = 0;
								} 
								
								value.set(index + count, 1);
							}

							// System.out.println("after value " +value);
							recordMap.put(fenceName, occupancyInitializedMap);
						}

						// Formatting the Data in the front-end expected format

						// clean and refill the payload ui format

						dataArr.clear();

						dataArr.add(strArrTimeSlice);

						for (Map.Entry<String, HashMap<String, ArrayList<Integer>>> entry : recordMap.entrySet()) {
							String fenceName = entry.getKey();
							HashMap<String, ArrayList<Integer>> fenceData = entry.getValue();
							ArrayList<String> listFenceArr = new ArrayList<String>();
							listFenceArr.add(fenceName);
							columnArr.add(fenceName);
							for (int i = 1; i < strArrTimeSlice.length; i++) {
								ArrayList<Integer> listMinutes = fenceData.get(strArrTimeSlice[i]);
								Iterator<Integer> listMinutesItr = listMinutes.iterator();
								int minutesCount = 0;
								while (listMinutesItr.hasNext()) {
									int ts = listMinutesItr.next();
									if (ts == 1) {
										minutesCount++;
									}
								}
								listFenceArr.add("Free: " + (60 - minutesCount) + " In-Use: " + minutesCount);
							}

							dataArr.add(listFenceArr);
						}

					} else if (operation.equalsIgnoreCase("occupancy_geofence_day")) {


						HashMap<String, HashMap<String, ArrayList<Integer>>> recordMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

						Iterator<JSONArray> occupancy_geofenceIter = dataArr.iterator();

						while (occupancy_geofenceIter.hasNext()) {

							JSONArray occupancy_geofence = occupancy_geofenceIter.next();

							String fenceName = (String) occupancy_geofence.get(0);
							String strEntryTime = (String) occupancy_geofence.get(2);

							long seconds = 0;

							if (occupancy_geofence.get(1) != null) {
								seconds = Long.valueOf(occupancy_geofence.get(1).toString());
							}

							long elpMinutes = (seconds) / 60;
							long roundedElpMinutes = elpMinutes + 1;

							DateFormat parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
							SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

							String strSliceDate = sdf.format(parse.parse(strEntryTime));
							
							String hh = strSliceDate.split(":")[0];
							String mm = strSliceDate.split(":")[1];

							int index = Integer.parseInt(mm);
							HashMap<String, ArrayList<Integer>> occupancyInitializedMap = null;
							if (recordMap.containsKey(fenceName)) {
								occupancyInitializedMap = recordMap.get(fenceName);
							} else {
								occupancyInitializedMap = getOccupancyInitializedMap();
							}

							int intValueHH = Integer.parseInt(hh);
							String strTimeSlice = strArrTimeSlice[intValueHH + 1];

							ArrayList<Integer> value = occupancyInitializedMap.get(strTimeSlice);
				

							value.set(index, 1);
							// update elp values in minutes
							int count = 0;
							int hourCount = 0;
							
							for (int i = 0; i < roundedElpMinutes - 1; i++) {
								count++;
								int range = index + count;
								//System.out.println("occupancy_geofence range" +range);
								if (range >= 60) {
									hourCount ++;
									int arrayLen = intValueHH + hourCount +1;
									if (arrayLen < strArrTimeSlice.length) {
										strTimeSlice = strArrTimeSlice[arrayLen];
										value = occupancyInitializedMap.get(strTimeSlice);
									}
									index = 0;
									count = 0;
								} 
								
								value.set(index + count, 1);
							}

							// System.out.println("after value " +value);
							recordMap.put(fenceName, occupancyInitializedMap);
						}

						// Formatting the Data in the front-end expected format

						// clean and refill the payload ui format

						dataArr.clear();
						
						/**
						recordMap.clear();
						HashMap<String, ArrayList<Integer>> occupancyMap = new HashMap<String, ArrayList<Integer>>();

						ArrayList<Integer> minutesList;

						for (int i = 1; i < strArrTimeSlice.length; i++) {
							String slice = strArrTimeSlice[i];
							minutesList = new ArrayList<Integer>();
							for (int seconds = 0; seconds < 60; seconds++) {
								if (seconds ==5 || seconds == 6 || seconds == 40)
									minutesList.add(1);
								else
									minutesList.add(0);
							}
							occupancyMap.put(slice, minutesList);
						}
						
						 recordMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
						 recordMap.put("gopi", occupancyMap);
						 recordMap.put("raja", occupancyMap);
						 recordMap.put("ram", occupancyMap);
						
						**/
						
						ArrayList<String> timeSliceDaysList = new ArrayList<String>();
						timeSliceDaysList.add("Free");
						timeSliceDaysList.add("In Use");
						
						ArrayList<String> listFenceArr = new ArrayList<String>();
						
						ArrayList<ArrayList<Long>> freeAndInUseList =  new ArrayList<ArrayList<Long>>();
						
						ArrayList<Long> freeDayList	 = new ArrayList<Long>();
						ArrayList<Long> inUseDayList = new ArrayList<Long>();
						
						for (Map.Entry<String, HashMap<String, ArrayList<Integer>>> entry : recordMap.entrySet()) {
							
							String fenceName = entry.getKey();

							HashMap<String, ArrayList<Integer>> fenceData = entry.getValue();
							listFenceArr.add(fenceName);
							
							long freeMinutesCount  = 0;
							long inUseMinutesCount = 0;
							
							for (int i = 1; i < strArrTimeSlice.length; i++) {
								
								ArrayList<Integer> listMinutes = fenceData.get(strArrTimeSlice[i]);
								Iterator<Integer> listMinutesItr = listMinutes.iterator();
								
								int minutesCount = 0;
								while (listMinutesItr.hasNext()) {
									int ts = listMinutesItr.next();
									if (ts == 1) {
										minutesCount++;
									}
								}
								
								long freeCount 	= 60-minutesCount;
								long inUseCount = minutesCount;

								freeMinutesCount += freeCount;
								inUseMinutesCount += inUseCount;
							}
						    freeDayList.add(freeMinutesCount);
							inUseDayList.add(inUseMinutesCount);
							
						}

						freeAndInUseList.add(0, freeDayList);
						freeAndInUseList.add(1, inUseDayList);
						
						dataArr.add(0, listFenceArr);
						dataArr.add(1, timeSliceDaysList);
						dataArr.add(2, freeAndInUseList);

					}

					if (operation.equalsIgnoreCase("timespent by tags")) {
						if (daysInBetween(strFromTimeESFormat, strToTimeESFormat) <= 1) {
							units = "minutes";
							factor = 60.0;
						}
						JSONArray tagArr = new JSONArray();
						JSONArray metricArr = new JSONArray();
						JSONArray chartData = new JSONArray();
						LinkedHashMap<String, JSONArray> metricTagMap = new LinkedHashMap<String, JSONArray>();
						for (int i = 0; i < dataArr.size(); i++) {
							JSONArray jsonValueArr = dataArr.getJSONArray(i);
							String tagId = (String) jsonValueArr.get(0);
							String metricName = (String) jsonValueArr.get(1);

							if (!tagArr.contains(tagId)) {
								tagArr.add(tagId);
							}
							if (!metricArr.contains(metricName)) {
								metricArr.add(metricName);
							}

						}

						for (int i = 0; i < metricArr.size(); i++) {
							String metricName = metricArr.getString(i);
							for (int j = 0; j < tagArr.size(); j++) {
								String tagId = tagArr.getString(j);
								for (int k = 0; k < dataArr.size(); k++) {
									JSONArray jsonValueArr = dataArr.getJSONArray(k);
									double valueInDecimals = 0;
									String strValue = jsonValueArr.getString(2);
									if (StringUtils.isNotEmpty(strValue) && !strValue.equals("null")) {
										valueInDecimals = Long.parseLong(strValue) / factor;
									}
									double elpMinutes = ((valueInDecimals * 100) / 100);
									
									double avgData = Math.ceil(elpMinutes);

									if (metricName.equals(jsonValueArr.getString(1))
											&& tagId.equals(jsonValueArr.getString(0))) {
										if (metricTagMap.containsKey(metricName)) {
											JSONArray jsonValue = metricTagMap.get(metricName);
											jsonValue.add(avgData);
											metricTagMap.put(metricName, jsonValue);
										} else {
											JSONArray jsonValue = new JSONArray();
											jsonValue.add(avgData);
											metricTagMap.put(metricName, jsonValue);
										}
									}
								}
							}
						}
						for (Map.Entry<String, JSONArray> map : metricTagMap.entrySet()) {
							chartData.add(map.getValue());
						}

						dataArr.clear();
						dataArr.add(tagArr);
						dataArr.add(metricArr);
						dataArr.add(chartData);
					}

					if (isGeoFence) {
						units = "times";
					}
				}
			}
				previewContent.put("data", dataArr);
				previewContent.put("units", units);
			}
			if(isTableChart) {
				previewContent.put("columns", columnArr);
			}else if (content.containsKey("columns")) {
				JSONArray columns = (JSONArray) content.get("columns");
				if (isAttendence) {
					columns.add(columns.size(), "Time Spent");
				} 
				
				if (columns.contains("Occupied_Time")) {
					columns.remove("Occupied_Time");
				}
				previewContent.put("columns", columns);
			}
		}
	}
	
	private static int daysInBetween(String dateStr1, String dateStr2) throws Exception {

		int days = 0;
		try {
			Date date1 = ES_DATEFORMAT.parse(dateStr1);
			Date date2 = ES_DATEFORMAT.parse(dateStr2);
			days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24));
			return Math.abs(days);
		} catch (ParseException e) {
			LOG.info("Date not in expected format");
			throw new Exception("Date not in expected format");
		}
	}
	
	private static HashMap<String, ArrayList<Integer>> getOccupancyInitializedMap() {

		HashMap<String, ArrayList<Integer>> occupancyMap = new HashMap<String, ArrayList<Integer>>();

		ArrayList<Integer> minutesList;

		for (int i = 1; i < strArrTimeSlice.length; i++) {
			String slice = strArrTimeSlice[i];
			minutesList = new ArrayList<Integer>();
			for (int seconds = 0; seconds < 60; seconds++) {
				minutesList.add(0);
			}
			occupancyMap.put(slice, minutesList);
		}

		return occupancyMap;
	}
}
