package com.semaifour.facesix.audit;

import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.data.account.UserAccount;

@Service
public class AuditUtil {
	
	private static SimpleDateFormat advanceDateFormat 	 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	/**
	 * Function used to create a JSON for user account
	 * @param user
	 * @return
	 */
	public static JSONObject createUserAccountJson(UserAccount user){
		JSONObject json = new JSONObject();
		
		if(user != null){
			json.put("pkid", 		user.getPkid());
			json.put("fname", 		user.getFname());
			json.put("lname", 		user.getLname());
			json.put("email", 		user.getEmail());
			json.put("phone", 		user.getPhone());
			json.put("password", 	user.getPassword());
			json.put("designation", user.getDesignation());
			json.put("role", 		user.getRole());
			json.put("cid", 		user.getCustomerId());
			json.put("isMailAlert", user.getIsMailalert());
			json.put("isSmsAlert", 	user.getIsSmsalert());
			json.put("resetRequest",user.getResetStatus());
			json.put("count", 		user.getCount());
			json.put("resetTime", 	user.getResetTime());
			json.put("uid", 		user.getUid());
			json.put("status", 		user.getStatus());
			json.put("version", 	user.getVersion());
			json.put("createdBy", 	user.getCreatedBy());
			json.put("createdOn", 	user.getCreatedOn());
			json.put("modifiedBy", 	user.getModifiedBy());
			json.put("modifedOn", 	user.getModifiedOn());
		}
		
		return json;
	}
	
	/**
	 * Function used to create a JSON for customer
	 * @param customer
	 * @return
	 */
	public static JSONObject createCustomerJson(Customer customer){
		JSONObject json = new JSONObject();
		String serviceStrDate = "";
		String serviceExpDate = "";
		
		if(customer != null){
			String cid = customer.getId();
			json.put("id", 						cid);
			json.put("customerName", 			customer.getCustomerName());
			json.put("country", 				customer.getCountry());
			json.put("venueType", 				customer.getVenueType());
			json.put("address", 				customer.getAddress());
			json.put("city", 					customer.getCity());
			json.put("state",					customer.getState());
			json.put("postalCode", 				customer.getPostalCode());
			json.put("offerPackage",			customer.getOfferPackage());
			json.put("noOfGateway", 			customer.getNoOfGateway());
			json.put("preferedUrlName", 		customer.getPreferedUrlName());
			if (customer.getServiceStartDate() != null)
				serviceStrDate = advanceDateFormat.format(customer.getServiceStartDate());
			json.put("serviceStartDate", 		serviceStrDate);
			if (customer.getServiceExpiryDate() != null)
				serviceExpDate = advanceDateFormat.format(customer.getServiceExpiryDate());
			json.put("serviceExpiryDate", 		serviceExpDate);
			json.put("serviceDurationinMonths", customer.getServiceDurationinMonths());
			json.put("contactPerson", 			customer.getContactPerson());
			json.put("contactPersonlname", 		customer.getContactPersonlname());
			json.put("designation", 			customer.getDesignation());
			json.put("contactNumber", 			customer.getContactNumber());
			json.put("mobileNumber", 			customer.getMobileNumber());
			json.put("qubercommAssist", 		customer.getQubercommAssist());
			json.put("email", 					customer.getEmail());
			json.put("status", 					customer.getStatus());
			json.put("alexacerfilepath", 		customer.getAlexacerfilepath());
			json.put("alexakeyfilepath", 		customer.getAlexakeyfilepath());
			json.put("alexaendpoint", 	 		customer.getAlexaendpoint());
			json.put("alexatopic", 		 		customer.getAlexatopic());
			json.put("tagcount", 			 	customer.getTagcount());
			json.put("threshold", 		 		customer.getThreshold());
			json.put("timezone", 		 		customer.getTimezone());
			json.put("bleserverip", 		 	customer.getBleserverip());
			json.put("logs", 		 	        customer.getLogs());
			json.put("tagInact", 		 		customer.getTagInact());
			json.put("logofile", 		 	    customer.getLogofile());
			json.put("background", 		 	    customer.getBackground());
			json.put("discover_text", 		 	customer.getDiscover_link());
			json.put("discover_link", 		 	customer.getDiscover_link());
			json.put("facebook", 		 	    customer.getFacebook());
			json.put("twitter", 		 	    customer.getTwitter());
			json.put("linkedin", 		 	    customer.getLinkedin());
			json.put("mqttToken", 		 	    customer.getMqttToken());
			json.put("restToken", 			    customer.getRestToken());
			json.put("jwtmqttToken", 		    customer.getJwtmqttToken());
			json.put("jwtrestToken", 			customer.getJwtrestToken());
			json.put("oauth", 					customer.getOauth());
			json.put("userAccId", 		 		customer.getUserAccId());
			json.put("solution",				customer.getSolution());
			json.put("inactivityMail", 			customer.getInactivityMail());
			json.put("inactivitySMS", 			customer.getInactivitySMS());
			json.put("vpn", 		 	    	customer.getVpn());
			json.put("simulationStatus", 		customer.getSimulationStatus());
			
		}
		return json;
	}
	
}
