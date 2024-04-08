package com.semaifour.facesix.util;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

	/*
	 * Mail content for Geofence Alert
	 */
	public StringBuilder buildGeoFenceTable() {
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("&nbsp;&nbsp;You have a new GeoFence Alert Message!!!<br/>")
				.append("&nbsp;&nbsp;Below are the list of events that occured based on created geofence alert")
				.append("<br/>")
				.append("<table border=\"1\" style=\"border-collapse:collapse;text-align:center;width: 100%;\">")
				.append("<tr>")
				.append(" <th style=\"padding:10px\">S.No</th>")
				.append(" <th style=\"padding:10px\">Tag Id</th>")
				.append(" <th style=\"padding:10px\">Assigned To</th>")
				.append(" <th style=\"padding:10px\">Venue</th>")
				.append(" <th style=\"padding:10px\">Floor</th>")
				.append(" <th style=\"padding:10px\">Geo-Fence</th>")
				.append(" <th style=\"padding:10px\">Event</th>")
				.append(" <th style=\"padding:10px\">Alert Name</th>")
				.append(" <th style=\"padding:10px\">Trigger Time</th>")
				.append("</tr>");
		return mailBody;
	}
	
	public StringBuilder buildBeaconAlertDataTable() {

		StringBuilder mailBody = new StringBuilder();
		mailBody.append("&nbsp;&nbsp;You have a new Alert Message!!!<br/>")
				.append("&nbsp;&nbsp;Below is the detailed list of Tags who are not in their assigned location. ")
				.append("Kindly look into this as a high priority.<br/>")
				.append("<br/>")
				.append("&nbsp;&nbsp;<br/> TAGS INACTIVE IN ASSIGNED REGION <br/> <br/>")
				.append("<table border=\"1\" style=\"border-collapse:collapse;text-align:center;width: 100%;\">")
				.append("<tr>")
				.append(" <th style=\"padding:10px\">S.No</th>")
				.append(" <th style=\"padding:10px\">Id</th>")
				.append(" <th style=\"padding:10px\">Tag Type</th>")
				.append(" <th style=\"padding:10px\">Assigned To</th>")
				.append(" <th style=\"padding:10px\">Floor Name</th>")
				.append(" <th style=\"padding:10px\">Location Name</th>")
				.append(" <th style=\"padding:10px\">Time</th>")
				.append(" <th style=\"padding:10px\">Timespent</th>")
				.append(" <th style=\"padding:10px\">Current Location</th>")
				.append("</tr>");
		
		return mailBody;
	}

	public StringBuilder buildInactivityTable () {
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("&nbsp;&nbsp;You have a new Alert Message!!!<br/>")
				.append("&nbsp;&nbsp;Below is the detailed list of inactive tags with its last seen location. ")
				.append("Kindly look into this as a high priority.<br/>")
				.append("<br/>")
				.append("&nbsp;&nbsp;TAGS NOT SEEN IN ANY VENUE ARE LISTED BELOW <br/> <br/>")
				.append("<table border=\"1\" style=\"border-collapse:collapse;text-align:center;width: 100%;\">")
				.append("<tr>")
				.append(" <th style=\"padding:10px\">S.No</th>")
				.append(" <th style=\"padding:10px\">Id</th>")
				.append(" <th style=\"padding:10px\">Tag Type</th>")
				.append(" <th style=\"padding:10px\">Assigned To</th>")
				.append(" <th style=\"padding:10px\">Floor Name</th>")
				.append(" <th style=\"padding:10px\">Location</th>")
				.append(" <th style=\"padding:10px\">Last Seen</th>")
				.append("</tr>");
			
		return mailBody;
		
	}
	
}
