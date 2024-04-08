package com.semaifour.facesix.account.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.audit.Audit;
import com.semaifour.facesix.audit.AuditService;
import com.semaifour.facesix.audit.AuditUtil;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.web.WebController;


@RestController
@RequestMapping("/rest/audit")
public class AuditRestController extends WebController{
	Logger LOG = LoggerFactory.getLogger(AuditRestController.class.getName());
	
	// String Constants
	private static final String PASSWORD_UPDATION 	 = "Password Updation";
	private static final String USER_CREATION 	 	 = "User Creation";
	private static final String USER_UPDATION 	 	 = "User Updation";
	private static final String USER_DELETION 	 	 = "User Deletion";
	private static final String CUS_CREATION		 = "Customer Creation";
	private static final String CUS_UPDATION		 = "Customer Updation";
	private static final String CUS_DELETION		 = "Customer Deletion";
	private static final String USER_COLLECTION_NAME = "userAccount";
	private static final String CUS_COLLECTION_NAME	 = "customer";
	
	private static String auditEvent 	 = null;
	private static String collectionName = null;
	JSONObject changedFromjson 			 = null;
	JSONObject changedTojson 			 = null;
	
	@Autowired
	private AuditService auditService;
	
	/**
	 * Function used to capture password update events.It occurs when
	 * 	1)Profile password update
	 * 	2)User password update
	 * 	3)Forget password update
	 * @param user
	 * @param newPassword
	 * @param request
	 * @param response
	 */
	public void passwordUpdateEvent(UserAccount user, String newPassword, HttpServletRequest request, HttpServletResponse response) {
		Audit audit		 = new Audit();
		auditEvent 		 = PASSWORD_UPDATION;
		collectionName 	 = USER_COLLECTION_NAME;
		
		audit.setAuditEvent(auditEvent);
		
		audit.setCollectionName(collectionName);
		audit.setCollectionPkid(user.getPkid());
		
		audit.setDoneBy(whoami(request, response));
		audit.setDoneTo(user.getEmail());

		changedFromjson = new JSONObject();
		changedTojson   = new JSONObject();

		changedFromjson.put("password", user.getPassword());
		try {
			changedTojson.put("password", _CCC.cryptor.encrypt(newPassword));
		} catch (Exception e) {
			LOG.info("Audit failed. Error occured while password encryption. " + e.getMessage());
			return;
		}

		audit.setChangedFrom(changedFromjson);
		audit.setChangedTo(changedTojson);

		auditService.save(audit);
	}
	
	/**
	 * Function used to capture User creation or User updation events.
	 * @param newUserAccount
	 * @param existingUserAccount
	 * @param isNewUser
	 */
	public void userSaveEvent(UserAccount newUserAccount, UserAccount existingUserAccount, boolean isNewUser) {

		if (newUserAccount != null) {
			Audit audit = new Audit();
			collectionName = USER_COLLECTION_NAME;

			changedFromjson = new JSONObject();
			changedTojson = new JSONObject();

			if (isNewUser) {
				auditEvent = USER_CREATION;
				changedFromjson = null; 
				changedTojson   = AuditUtil.createUserAccountJson(newUserAccount);
			} else {
				auditEvent = USER_UPDATION;
				changedFromjson = AuditUtil.createUserAccountJson(existingUserAccount);
				changedTojson   = AuditUtil.createUserAccountJson(newUserAccount);
			}

			audit.setAuditEvent(auditEvent);
			audit.setCollectionName(collectionName);
			audit.setCollectionPkid(newUserAccount.getPkid());
			audit.setDoneBy(newUserAccount.getModifiedBy());
			audit.setDoneTo(newUserAccount.getEmail());
			audit.setChangedFrom(changedFromjson);
			audit.setChangedTo(changedTojson);

			auditService.save(audit);
		}
	}
	
	/**
	 * Function used to capture User deletion events.
	 * @param user
	 * @param request
	 * @param response
	 */
	public void userDeletionEvent(UserAccount user, HttpServletRequest request,HttpServletResponse response){
		if(user != null){
			Audit audit		 = new Audit();
			auditEvent 		 = USER_DELETION;
			collectionName 	 = USER_COLLECTION_NAME;
			
			changedFromjson = AuditUtil.createUserAccountJson(user);
			changedTojson   = null; 			// After deletion user json will be null
	
			audit.setAuditEvent(auditEvent);
			audit.setCollectionName(collectionName);
			audit.setCollectionPkid(user.getPkid());
			audit.setDoneBy(whoami(request, response));
			audit.setDoneTo(user.getEmail());
			audit.setChangedFrom(changedFromjson);
			audit.setChangedTo(changedTojson);
	
			auditService.save(audit);
		}
	}
	
	/**
	 * Function used to capture Customer creation or Customer updation events.
	 * @param newCustomer
	 * @param existingCustomer
	 * @param isNewCustomer
	 */
	public void customerSaveEvent(Customer newCustomer, Customer existingCustomer, boolean isNewCustomer) {
		if(newCustomer != null){
			Audit audit = new Audit();
			collectionName = CUS_COLLECTION_NAME;

			changedFromjson = new JSONObject();
			changedTojson   = new JSONObject();
			
			if(isNewCustomer){
				auditEvent = CUS_CREATION;
				changedFromjson = null;
				changedTojson   = AuditUtil.createCustomerJson(newCustomer);
			} else{
				auditEvent = CUS_UPDATION;
				changedFromjson = AuditUtil.createCustomerJson(existingCustomer);
				changedTojson   = AuditUtil.createCustomerJson(newCustomer);
			}
			
			audit.setAuditEvent(auditEvent);
			audit.setCollectionName(collectionName);
			audit.setCollectionPkid(newCustomer.getPkid());
			audit.setDoneBy(newCustomer.getModifiedBy());
			audit.setDoneTo(newCustomer.getEmail());
			audit.setChangedFrom(changedFromjson);
			audit.setChangedTo(changedTojson);

			auditService.save(audit);
		}
	}
	
	/**
	 * Function used to capture Customer Deletion events.
	 * @param customer
	 * @param request
	 * @param response
	 */
	public void customerDeletionEvent(Customer customer, HttpServletRequest request,HttpServletResponse response){
		if(customer != null){
			Audit audit		 = new Audit();
			auditEvent 		 = CUS_DELETION;
			collectionName 	 = CUS_COLLECTION_NAME;
			
			changedFromjson = AuditUtil.createCustomerJson(customer);
			changedTojson   = null; 			// After deletion user json will be null
	
			audit.setAuditEvent(auditEvent);
			audit.setCollectionName(collectionName);
			audit.setCollectionPkid(customer.getPkid());
			audit.setDoneBy(whoami(request, response));
			audit.setDoneTo(customer.getEmail());
			audit.setChangedFrom(changedFromjson);
			audit.setChangedTo(changedTojson);
	
			auditService.save(audit);
		}
	}
}
