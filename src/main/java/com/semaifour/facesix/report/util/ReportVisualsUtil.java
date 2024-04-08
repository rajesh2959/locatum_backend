package com.semaifour.facesix.report.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semaifour.facesix.report.data.Filter;
import com.semaifour.facesix.report.data.ReportLabels;
import com.semaifour.facesix.report.data.ReportVisuals;
import com.semaifour.facesix.report.data.SubBuckets;

public final class ReportVisualsUtil {
	
	static Logger LOG = LoggerFactory.getLogger(ReportVisualsUtil.class);
	
	private final static DateFormat ES_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
	
	private final static long MILLIS_5POINT5_HRS =  19800000l;// subtract 5.5hrs to obtain UTC time
	private final static long MILLIS_24_HRS   = 86400000l; // add 24hrs

	private final static String ES_FIELD_ENTRY_LOC = "entry_loc";
	private final static String ES_FIELD_EXIT_LOC = "exit_loc";
	private final static String ES_FIELD_ELAPSED_LOC = "elapsed_loc";
	private final static String ES_FIELD_ELAPSED_FLOOR = "elapsed_floor";
	
	private final static String STR_METRIC_TOTAL_TIMESPENT = "total timespent";
	private final static String STR_METRIC_AVG_TIMESPENT = "avg timespent";
	private final static String STR_METRIC_TAG_COUNT = "tag count";
	private final static String STR_METRIC_ATTENDANCE = "attendance";
	private final static String STR_METRIC_OCCUPANCY_GEOFENCE = "occupancy_geofence";
	private final static String STR_METRIC_OCCUPANCY_GEOFENCE_DAY = "occupancy_geofence_day";
	private final static String STR_METRIC_TIMESPENT_BY_TAGS = "timespent by tags";
	private final static String STR_METRIC_OCCUPIED_TIME = "Occupied_Time";
	
	private final static String STR_BUCKET_SID = "sid";
	private final static String STR_BUCKET_SPID = "spid";
	private final static String STR_BUCKET_LOCATION = "location";
	private final static String STR_BUCKET_GEOFENCE = "geofence";
	private final static String STR_BUCKET_TAGID = "tagid";
	private final static String STR_BUCKET_TAG_TYPE = "tagtype";
	
	private final static String STR_SUBBUCKET_VENUE = "Venue";
	private final static String STR_SUBBUCKET_FLOOR = "Floor";
	private final static String STR_SUBBUCKET_GEOFENCE = "Free/In Use";
	
	private static String select_fields = "";
	private static String from_viewName = " FROM viewName";		//viewName is the name of the ssql object1;
	private static String where_clause = "";
	private static String group_by = " GROUP BY ";
	private static String base_query = "";
	private static String ssql_query = "";
	private static String mongo_based_query = "";				//ssql object2 will be from the mongoDB;
	private static String mongo_stacked_chart_query="";
	private static String ssql_return_query = "";
	private static String join_query = "";
	
	private static String cid = "";
	private static String tagId = STR_BUCKET_TAGID;
	private static List<ReportLabels> metrics = null;
	private static List<ReportLabels> buckets = null;
	private static List<Filter> filters = null;
	private static List<SubBuckets> subBucketsList  = null;
	
	private static String metricAggOperation = "";
	private static String metricLabel = "";
	private static String bucket_field_name = "";
	private static String bucket_label = "";
	
	private static boolean isGeoFence = false;					// have to see if we can substitute these boolean variables with a diff logic
	private static boolean isAttendance = false;
	private static boolean isOccupancy_geofence = false;
	
	private static String chartType = "";
	private static enum CHART_TYPE { stackedbar, pie, bar, line, table }
	
	/**
	 * 
	 * @param visualObject
	 * @param strFromTimeESFormat
	 * @param strToTimeESFormat
	 * @param return_type
	 * @return
	 */
	public static String buildSSQLQuery(ReportVisuals visualObject, String strFromTimeESFormat, String strToTimeESFormat) {
		String query = "";
		String fssql_return_type = "column";		//default response return-type from progsets
		ReportLabels bucket = null;
		
		// Step1: Initialize values for Base SSQL query 
		initializeBuildObjects(strFromTimeESFormat, strToTimeESFormat);
		
		// Step2: Prepare to build ES based SSQL query
		cid = visualObject.getCid();
		metrics = visualObject.getMetrics();
		buckets = visualObject.getBuckets();
		filters = visualObject.getFilters();
		chartType = visualObject.getChartType();
		
		if (buckets != null && buckets.size() > 0) {
			bucket = buckets.get(0);
			bucket_field_name = bucket.getFieldName();

			if (bucket_field_name.equalsIgnoreCase(STR_BUCKET_GEOFENCE)) {
				isGeoFence = true;
			}
			bucket_field_name = isGeoFence ? STR_BUCKET_LOCATION : bucket_field_name; // For geofence, we still use "location" as
																				// the attribute name in ES documents
			bucket_label = bucket.getCustomLabel().replace(" ", "_");
			
			select_fields += bucket_field_name;
			group_by += bucket_field_name;
			
			if (chartType.equals(CHART_TYPE.stackedbar.name())) {
				prepareToBuildForStackedBarChart();
			}
		}
		
		// Step3: Build ES based SSQL Query - object1
		if (metrics != null && metrics.size() > 0) {
			ReportLabels metric = metrics.get(0);
			metricLabel = metric.getCustomLabel();
			metricAggOperation = metric.getAggOperation().toLowerCase();
			
			buildESBasedQuery(strFromTimeESFormat, strToTimeESFormat);

			//Deviations in SSQL return-type based on metricCategory
			if (metricAggOperation.equalsIgnoreCase(STR_METRIC_TIMESPENT_BY_TAGS)) {
				fssql_return_type = "list";
			} else if(isOccupancy_geofence) {
				fssql_return_type = "list";
			}
		}
		
		// Step4: Build MongoDB-based SSQL Query - object2
		if (bucket != null) {
			buildMongoBasedQuery(bucket.getFieldName());
		}
		
		// Step5: Any additional Where-Clause items to be used as filters in the SSQL object1
		if (filters != null && filters.size() > 0) {
			for (Filter filter : filters) {
				List<String> values = filter.getValue();
				if (values != null && values.size() > 0) {
					where_clause += " AND " + filter.getFieldname() + " IN (";
					for (int i = 0; i < values.size(); i++) {
						if (i != 0) {
							where_clause += ",";
						}
						where_clause += "'" + values.get(i) + "'";
					}
					where_clause += ")";
				}
			}
		}

		// Step6: Put all of SSQL object1 statements together and join object2
		ssql_query = select_fields + from_viewName + where_clause + group_by + "\n";
		
		// Tag Count and TagType Filter Option no need for mongo_based_query logic
		
		buildJoinQuery();
		
		if (!bucket_field_name.equals(STR_BUCKET_TAG_TYPE)) { 
			if (chartType.equals(CHART_TYPE.stackedbar.name())) {
				ssql_query += mongo_based_query +mongo_stacked_chart_query+ "\nviewName= ssql?sql=";
			} else {
				ssql_query += mongo_based_query + "\nviewName= ssql?sql=";
			}
			
		}
		
		// Step7: return statement of the SSQL query
		ssql_return_query = "\nreturn?view=viewName&as=" + fssql_return_type + "\nclose";
		
		// Step8: Put all of the query statements together
		query = base_query + ssql_query + join_query + ssql_return_query;

		return query;
	}
	
	private static void prepareToBuildForStackedBarChart() {
		ReportLabels reportLabels = buckets.get(0);
		subBucketsList = reportLabels.getSubBuckets();
		
		if(!CollectionUtils.isEmpty(subBucketsList)) {
			SubBuckets subBucket 	 = subBucketsList.get(0);
			String bucketFieldName 	 = subBucket.getFieldName();
			
			if (!StringUtils.isEmpty(bucketFieldName)) {
				select_fields += "," + bucketFieldName;
				group_by += "," + bucketFieldName;
			}
		}		
	}

	private static void initializeBuildObjects(String strFromTimeESFormat, String strToTimeESFormat) {
		select_fields = "";
		mongo_based_query = "";
		group_by = " GROUP BY ";
		isGeoFence = false;
		isAttendance = false;
		isOccupancy_geofence = false;
		
		//Default values for Base SSQL query and the where-clause used in SSQL object1
		base_query = "viewName= ielastic?index=facesix-int-beacon-event/trilateration\n"
				+ "viewName= ssql?sql=SELECT ";

		try {
			long fromTime = substractHours(strFromTimeESFormat, MILLIS_5POINT5_HRS);
			long toTime   = fromTime + MILLIS_24_HRS;

			strFromTimeESFormat = formatDate(fromTime);
			strToTimeESFormat   = formatDate(toTime);
			
			LOG.info(" from date " + strFromTimeESFormat + " to date" + strToTimeESFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 

		where_clause = " WHERE opcode=\"reports\" AND timestamp >= '" + strFromTimeESFormat + "' AND timestamp <= '" + strToTimeESFormat
				+ "' AND cid=\"" + cid + "\"";		
	}

	private static void buildESBasedQuery(String strFromTimeESFormat, String strToTimeESFormat) {

		String operation = "";
		switch (metricAggOperation) {
		case STR_METRIC_TOTAL_TIMESPENT:
			if (isGeoFence) {																// For geofence, we don't store the amount of
				operation = ", COUNT(" + getOperationFieldName(bucket_field_name) + ")"; 	// time in ES, so using COUNT
			} else {
				operation = ", SUM(" + getOperationFieldName(bucket_field_name) + ")";
			}
			select_fields += operation;
			break;
		case STR_METRIC_AVG_TIMESPENT:
			if (isGeoFence) {																// For geofence, we don't store the amount of
				operation = ", COUNT(" + getOperationFieldName(bucket_field_name) + ")"; 	// time in ES, so using COUNT
			} else {
				operation = ", SUM(" + getOperationFieldName(bucket_field_name) + ")";
			}
			select_fields += operation;
			break;
		case STR_METRIC_TAG_COUNT:
			select_fields += operation = ", COUNT(" + bucket_field_name + ")";
			break;
		case STR_METRIC_ATTENDANCE: { 
			isAttendance = true;
				where_clause += " AND location_type=\"receiver\" ";
				select_fields += operation = ", MIN(" + ES_FIELD_ENTRY_LOC + ") as In_Time ,MAX(" + ES_FIELD_EXIT_LOC + ") ";
			break;
		}
		case STR_METRIC_OCCUPANCY_GEOFENCE: 
		case STR_METRIC_OCCUPANCY_GEOFENCE_DAY : {

			isOccupancy_geofence = true;
			
			where_clause += " AND location_type=\"geofence\" AND " + ES_FIELD_ELAPSED_LOC + "!=0 ";
			
			select_fields += operation = ", " + ES_FIELD_ENTRY_LOC + " as "+STR_METRIC_OCCUPIED_TIME+" ," + ES_FIELD_ELAPSED_LOC + " ";
			
			group_by += "," + ES_FIELD_ENTRY_LOC + "," + ES_FIELD_ELAPSED_LOC;

			break;
		}
		case STR_METRIC_TIMESPENT_BY_TAGS:
			
		//	select_fields +=select_fields;
			operation = ", SUM(" + ES_FIELD_ELAPSED_LOC + ")";
			select_fields += operation;
			
			where_clause +=" AND cid=\"" + cid + "\" AND location_type=\"receiver\"";
			
		//	group_by += "," + tagId;
		}

		if (StringUtils.isBlank(metricLabel)) {
			metricLabel = operation; // metric.getFieldName()
		}
		metricLabel = metricLabel.replace(" ", "_");
		select_fields += " AS " + metricLabel;
	}

	/**
	 * Evaluating the Bucket Operation of the UI input to construct the mongo based query
	 * 
	 * @param bucketFieldName
	 */
	private static void buildMongoBasedQuery(String bucketFieldName) {

		switch (bucketFieldName) {
		case STR_BUCKET_SID:
			mongo_based_query = "location= imongo?collection=site\nlocation= ssql?sql=SELECT pkid as id,uid as name FROM location WHERE cid=\""
					+ cid + "\"";
			break;
		case STR_BUCKET_SPID:
			if (StringUtils.isBlank(mongo_based_query)) {
				mongo_based_query = "location= imongo?collection=portion\nlocation= ssql?sql=SELECT pkid as id,uid as name FROM location  WHERE cid=\""
						+ cid + "\"";
			}
			break;
		case STR_BUCKET_LOCATION:
			if (StringUtils.isBlank(mongo_based_query)) {
				mongo_based_query = "location= imongo?collection=BeaconDevice\nlocation= ssql?sql=SELECT uid as id,name as name FROM location WHERE cid=\""
						+ cid + "\"";
			}
			break;
		case STR_BUCKET_GEOFENCE:
			if (isOccupancy_geofence) {
				mongo_based_query = "occupancy_geofence= imongo?collection=geofence\noccupancy_geofence= ssql?sql=SELECT pkid as id,name as GeoFence_Name FROM occupancy_geofence ";
			} else {
				mongo_based_query = "location= imongo?collection=geofence\nlocation= ssql?sql=SELECT pkid as id,name as name FROM location  WHERE cid=\""
						+ cid + "\"";
				where_clause += " AND location_type = \"geofence\"";
			}
			break;
		case STR_BUCKET_TAGID:
			mongo_based_query = "tags= imongo?collection=beacon\ntags= ssql?sql=SELECT macaddr as id,assignedTo as name,sid,spid FROM tags WHERE cid=\""
					+ cid + "\"";
			break;
		}
		
	}

	private static void buildJoinQuery() {
		String stackedBar = CHART_TYPE.stackedbar.name();
		
		if (isAttendance) {
			if (bucket_label.equals("tags")) {
				bucket_label = "Tags";
			}
			join_query = "SELECT name AS " + bucket_label
					+ ",viewName.In_Time,viewName." + metricLabel + " as Out_Time " + " FROM viewName,tags" + " WHERE "
					+ bucket_field_name + "=" + "id";
		} else {
			if (StringUtils.isNotBlank(mongo_based_query)) {
				if (! metricAggOperation.equals(STR_METRIC_TIMESPENT_BY_TAGS)) {
					if (chartType.equals(CHART_TYPE.table.name()) && isOccupancy_geofence) {
						join_query = "SELECT GeoFence_Name AS " + bucket_label + ",viewName."+metricLabel+",viewName."
								+ STR_METRIC_OCCUPIED_TIME+" FROM viewName,occupancy_geofence WHERE "
								+ bucket_field_name + "=" + "id ORDER BY viewName."+STR_METRIC_OCCUPIED_TIME+"";
					} else {
						join_query = "SELECT name AS " + bucket_label + ",viewName." + metricLabel + " FROM viewName,location WHERE "
								+ bucket_field_name + "=" + "id";
					}
				}
			}
		}

		if (stackedBar.equals(chartType)) {
			buildStackedBarJoinQuery();
		}
	}
	
	private static void buildStackedBarJoinQuery() {
		
		SubBuckets subBucket 	 = subBucketsList.get(0);
		String bucketFieldName 	 = subBucket.getFieldName();
		String splitBy 	 	 	 = subBucket.getSplitBy();
		
		switch (splitBy) {
		case STR_SUBBUCKET_VENUE:
			
			//The 2nd MongoDB based query
			mongo_stacked_chart_query = "\nlocation= imongo?collection=site\n"
					+ "location= ssql?sql=SELECT pkid as id,uid FROM location WHERE cid=\""+cid+"\"";

			join_query = "SELECT tags.name AS "+bucket_label+",uid AS "+splitBy+",viewName."+metricLabel
					+ " FROM viewName,location,tags WHERE viewName."+bucketFieldName+"=tags."+bucketFieldName+""
					+ " AND viewName." + bucketFieldName + "=" + "location.id AND tags.id=viewName."+tagId+""
					+ " ORDER BY "+bucket_label+"," + splitBy;
			break;

		case STR_SUBBUCKET_FLOOR:

			//The 2nd MongoDB based query
			mongo_stacked_chart_query = "\nlocation= imongo?collection=portion\nlocation= ssql?sql=SELECT pkid as id,uid FROM location WHERE cid=\""
					+ cid + "\"";

			join_query = "SELECT tags.name as "+bucket_label+",uid AS " + splitBy + ",viewName." + metricLabel
					+ " FROM viewName,location,tags WHERE viewName." + bucketFieldName + "=" + "location.id AND tags.id=viewName."+tagId+""
					+ " ORDER BY "+bucket_label+"," + splitBy;
			break;

		case STR_SUBBUCKET_GEOFENCE:

			join_query = " SELECT GeoFence_Name AS "
					+ bucket_label + ",viewName." + metricLabel + ", viewName." + STR_METRIC_OCCUPIED_TIME+" "
					+ " FROM viewName,occupancy_geofence" + " WHERE " + bucket_field_name + "="
					+ "id ORDER BY viewName." + STR_METRIC_OCCUPIED_TIME+"";
			break;
		
		default:
			LOG.info("SubBuckets doesn't match any given type " + splitBy);
			break;
		}
	}

	private static String formatDate(long fromTimestamp) {
		Date fromDate = new Date(fromTimestamp);
		String strDate = ES_DATEFORMAT.format(fromDate);
		return strDate;
	}

	private static long substractHours(String strDate, long subtractTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date fromDate 		= sdf.parse(strDate);
		long millisec 		= fromDate.getTime() - subtractTime;
		return millisec;
	}

	private static String getOperationFieldName(String bucket_field_name) {
		String timespentOf = ES_FIELD_ELAPSED_LOC;
		if (bucket_field_name.equalsIgnoreCase(STR_BUCKET_SPID) || bucket_field_name.equalsIgnoreCase(STR_BUCKET_SID)) {
			timespentOf = ES_FIELD_ELAPSED_FLOOR;
		}
		return timespentOf;
	}
}
