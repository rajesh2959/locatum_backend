package com.semaifour.facesix.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.account.rest.AuditRestController;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;
import com.semaifour.facesix.web.WelcomeController;
import net.sf.json.JSONObject;

/**
 * @author Qubercomm Technologies
 * 
 */
@RestController
@RequestMapping("/rest/qubercloud")
public class LoginRestController extends WebController {

	Logger LOG=LoggerFactory.getLogger(LoginRestController.class.getName());
	
	@Autowired	private UserAccountService userService;
	
	@Autowired private CustomerService customerService;
	
	@Autowired private CustomerUtils customerUtils;
	
	@Autowired private WelcomeController welcomeController;
	
	@Autowired
	AuditRestController auditRestController;
	
	/**
	 *  This method used to login the user
	 * @param userId 
	 * @param password 
	 * @return return user details
	 * 
	 */
	@RequestMapping("/login")
	public Restponse<JSONObject> welcome(@RequestParam(value = "userId", required = true) String userId,
							  			 @RequestParam(value = "password", required = true) String password,
							  			 HttpServletRequest request,HttpServletResponse response) {

		JSONObject message = new JSONObject();
		
		boolean status 	= false;
		int    code   	= 401;
		
		try {
			
			LOG.info("User Id " + userId);
			
			if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(password)) {
				
				userId = userId.trim();
				
				UserAccount userAccount = userService.findOneByUid(userId);
				
				if (userAccount != null ) {

					if (userAccount != null && userAccount.getResetStatus()) {
						userAccount.setResetStatus(false);
						userAccount = userService.saveContact(userAccount);
					}
					
					String userStatus 	= userAccount.getStatus();
					String userPassword = userAccount.getPassword();
					String userUid      = userAccount.getUid().trim();
					String Id      		= userAccount.getId();
					String  role		= userAccount.getRole().trim();
					
					String cid 		= null;
					String solution = null;
					
					boolean isTrue = StringUtils.equals(userPassword, _CCC.cryptor.encrypt(password));
					
					if (userStatus.equalsIgnoreCase("ACTIVE") && isTrue) {
						
						if (!role.equals("superadmin")) {
							cid = userAccount.getCustomerId();
							Customer customer = customerService.findById(cid);
							if (customer != null) {
								solution = customer.getSolution();
								String custName = customer.getCustomerName();

								message.put("cid", 	   cid);
								message.put("custname", custName);
								message.put("solution", solution);
								
								SessionUtil.setCurrentCustomer(request.getSession(), cid);
								SessionUtil.setCurrentSiteCustomerId(request.getSession(), cid);
							}
						}
						
						SessionUtil.authorizeSession(request.getSession(), userUid);
						sessionCache.setAttribute(request.getSession(), "user", userAccount);
						sessionCache.setAttribute(request.getSession(), "time", now());
						sessionCache.setAttribute(request.getSession(), "role", role);
						sessionCache.setAttribute(request.getSession(), "userId", Id);
						
						SessionUtil.setCurrentSiteCustomerId(request.getSession(), cid);
						SessionUtil.setCurrentCustomer(request.getSession(), cid);
						
						Map<String, Object> privilege = new HashMap<String, Object>();
						privilege.put("role",role);
						privilege.put("id",  cid);
						
						sessionCache.setAttribute(request.getSession(),"privs", privilege);
						
						switch (role) {
						
						case "superadmin":
							
							privilege.put("CUST_READ", true);
							privilege.put("SITE_READ", true);
							privilege.put("SYS_READ", true);
							privilege.put("ACC_READ", true);

							privilege.put("CUST_WRITE", true);
							privilege.put("SITE_WRITE", true);
							privilege.put("SYS_WRITE", true);
							privilege.put("ACC_WRITE", true);
							
							privilege.put("BINARY_WRITE", true);
							privilege.put("ADMIN_WRITE", true);
							privilege.put("INACTIVE_WRITE", true);
							privilege.put("MANAGEMENT_WRITE", true);
						
							privilege.put("SUPERAPP_ADMIN_WRITE", true);
							
							break;
						case "appadmin":
							
							if (StringUtils.equals("Gateway", solution)	|| StringUtils.equals("GatewayFinder", solution)) 
								privilege.put("CAPTIVE_PORTAL", true);
							 else 
								privilege.put("CAPTIVE_PORTAL", false);
							
							privilege.put("CUST_READ", false);
							privilege.put("SITE_READ", true);
							privilege.put("SYS_READ", true);
							privilege.put("ACC_READ", true);

							privilege.put("CUST_WRITE", false);
							privilege.put("SITE_WRITE", true);
							privilege.put("SYS_WRITE", true);
							privilege.put("ACC_WRITE", true);	
							privilege.put("APP_WRITE", true);
							
							privilege.put("BINARY_WRITE", true);
							privilege.put("MANAGEMENT_WRITE", true);
							
							privilege.put("SUPERAPP_ADMIN_WRITE", true);
							
							break;
						case "siteadmin":
							
							privilege.put("CUST_READ", false);
							privilege.put("SITE_READ", true);
							privilege.put("SYS_READ", true);
							privilege.put("ACC_READ", true);

							privilege.put("CUST_WRITE", false);
							privilege.put("SITE_WRITE", true);
							privilege.put("SYS_WRITE", false);
							privilege.put("ACC_WRITE", false);
							privilege.put("APP_WRITE", true);
							
							break;
						case "sysadmin":

							privilege.put("CUST_READ", false);
							privilege.put("SITE_READ", true);
							privilege.put("SYS_READ", true);
							privilege.put("ACC_READ", true);

							privilege.put("CUST_WRITE", false);
							privilege.put("SITE_WRITE", false);
							privilege.put("SYS_WRITE", true);
							privilege.put("ACC_WRITE", false);	
							privilege.put("APP_WRITE", true);
							
							break;
						case "useradmin":
							
							if (StringUtils.equals("Gateway", solution)	|| StringUtils.equals("GatewayFinder", solution)) 
								privilege.put("CAPTIVE_PORTAL", true);
							 else 
								privilege.put("CAPTIVE_PORTAL", false);
							
							
							privilege.put("CUST_READ", false);
							privilege.put("SITE_READ", true);
							privilege.put("SYS_READ", true);
							privilege.put("ACC_READ", true);

							privilege.put("CUST_WRITE", false);
							privilege.put("SITE_WRITE", false);
							privilege.put("SYS_WRITE", false);
							privilege.put("ACC_WRITE", true);	
							privilege.put("MANAGEMENT_WRITE", true);
							
							break;
						case "user":
							
							privilege.put("CUST_READ", false);
							privilege.put("SITE_READ", true);
							privilege.put("SYS_READ", true);
							privilege.put("ACC_READ", true);

							privilege.put("CUST_WRITE", false);
							privilege.put("SITE_WRITE", false);
							privilege.put("SYS_WRITE", false);
							privilege.put("ACC_WRITE", false);
							privilege.put("APP_WRITE", true);
							
							break;
						default:
							break;
						}
						
						status = true;
						code   = 200;
						int ttls = 14400000; 
						
						message.put("role", 	role);
						message.put("userId", 	Id);
						message.put("uid", 		userId);
						message.put("ttls", 	ttls);
						
					} else {
						status = false;
						code   = 401;
					}
				}
			}
		
		} catch (Exception e) {
			LOG.error("While Login users Error" + e);
			status  = false;
			code 	= 401;
		}
		
		return new Restponse<JSONObject>(status, code, message);

	}
	
	/**
	 *  This method used to validate the user's mail ID and 
	 *  send resetPassword link with ID and generated token to the validated mailID
	 *  @param emailId 
	 *  
	 */
	@RequestMapping("/forgetpassword")
	public Restponse<String> fgnotify(@RequestParam(value = "emailId", required = true) String emailId,HttpServletRequest request,HttpServletResponse response) {
		
		boolean success = true;
		int code 		= 200;
		String body 	= "Reset password url has been sent to your email address.";
		
		try {

			LOG.info(" User emailId" + emailId);

			UserAccount user = userService.findOneByEmail(emailId);

			if (user != null && user.getUid().equals(emailId)) {

				String userStatus = user.getStatus();
				
				if (!userStatus.equalsIgnoreCase(CustomerUtils.INACTIVE())) {
					
					Date date 	 = new Date();
					String token = UUID.randomUUID().toString();
					LOG.info("token " +token);
					user.setToken(token);
					user.setResetStatus(true);
					user.setResetTime(date.getTime());
					user = userService.saveContact(user);

					String contextPath 	= customerUtils.cloudUrl() +request.getContextPath();//"http://localhost:8175/facesix/angular/index.html#!";
					String url 		= contextPath + "/angular/index.html#!/reset/" + user.getId() + "/" + token;
					LOG.info("url " +url);
					String name 	= user.getFname();
					name 			= name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();// capitalize first letter
					String cid 		= user.getCustomerId();
					String toMail 	= user.getEmail();
					
					String msg 		= "Hi " + name+",\n\n Please click below link to reset or activate your "
									+ "username / password. \n Note: The Below link will expire after 30 minutes.\n" + url;
					
					customerUtils.customizeSupportEmail(cid, toMail, "Reset Password", msg, null);
					
				} else {
					code 	= 400;
					success = false;
					body    = "User not alive.";
				}
			} else {
				code 	= 404;
				success = false;
				body    = "User not found.";
			}

		} catch (Exception e) {
			success = false;
			code = 500;
			body = "While password reset request error";
			e.printStackTrace();
		}
		
		return new Restponse<String>(success, code, body);
	}
	
	/**
	 *  This method used to validate the user's ID and token
	 *  @param id 
	 *  @param token
	 *  
	 */
	@RequestMapping("/resetpassword")
	public Restponse<String> reset(@RequestParam(value="id",  required=true) String id, 
				@RequestParam(value="token",  required=true) String token,HttpServletRequest request,HttpServletResponse response) {
	
		boolean success = true;
		int code 		= 200;
		String body 	= "You can reset your userPassword";


		try {

			UserAccount user = userService.findById(id);
		
			if (user != null) {
				
				Date now = new Date();
				boolean notExpried = welcomeController.withinTimeLimit(user.getResetTime(),now.getTime());
				boolean resetStatus = user.getResetStatus();
				
				LOG.info(" notExpried " + notExpried + " resetStatus " + resetStatus);
				
				if (!notExpried || !resetStatus) {
					success = false;
					code 	= 400; //Bad Request
					body = "Link has either expired or it has been already used";
					return new Restponse<String>(success, code, body);
				} else	if (!user.getToken().equals(token)) {
					success = false;
					code 	= 400; //Bad Request
					body 	= "Invalid user token.";
				} 
			} else {
				success = false;
				code 	= 404;
				body 	= "User not found.";
			}

		} catch (Exception e) {
			success = false;
			code = 500; 
			body = "While reset password occurring error";
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);

	}

	/**
	 *  This method used to save the new password for the respective user
	 *  @param userId 
	 *  @param token 
	 *  @param password 
	 *  @param cpassword 
	 *  
	 */

	@RequestMapping(value = "/changepassword")
	public Restponse<String> changepwd(@RequestParam(value="id",  required=true) String userId, 
			@RequestParam(value="token",  required=true) String token,
			@RequestParam(value="password",  required=true) String password, 
			@RequestParam(value="cpassword",  required=true) String cpassword,
			HttpServletRequest request, HttpServletResponse response) {
		
		
		boolean success = true;
		int code 		= 200;
		String body 	= "Your password has been reset successfully!.";
		
		try {
			
			UserAccount user = userService.findById(userId);
		
			if (user != null) {
				
				Date now = new Date();
				boolean notExpried = welcomeController.withinTimeLimit(user.getResetTime(),now.getTime());
				boolean resetStatus = user.getResetStatus();
				String passwordStrengthRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
				
				LOG.info(" notExpried " + notExpried + " resetStatus " + resetStatus);
				
				if (!notExpried || !resetStatus) {
					success = false;
					code 	= 400; //Bad Request
					body = "Link has either expired or it has been already used";
					return new Restponse<String>(success, code, body);
				} else	if (user.getToken().equals(token)) {
					if (StringUtils.isEmpty(password) || StringUtils.isEmpty(cpassword) || 
							!Pattern.matches(passwordStrengthRegex,password) || !Pattern.matches(passwordStrengthRegex,cpassword)){
						success = false;
						code 	= 400; // Bad Request
						body 	= "Password or confirm password does not satisfy the requirement.";
						LOG.info(" New password matches " + Pattern.matches(passwordStrengthRegex,password) + " Confirm password matches " + Pattern.matches(passwordStrengthRegex,cpassword));
					
					} else if (!password.equals(cpassword)){
						success = false;
						code 	= 400; // Bad Request
						body 	= "Password and confirm password not matched.";	
						
					} else {
						user.setResetStatus(false);
						user.setPassword(_CCC.cryptor.encrypt(password));
						
						//Audit Event - Password Updation
						auditRestController.passwordUpdateEvent(user,password,request, response);
						
						user = userService.saveContact(user);
						
					}
				} else {
					success = false;
					code 	= 400; //Bad Request
					body 	= "Invalid user token.";
				}
			} else {
				success = false;
				code 	= 404;
				body 	= "User not found.";
			}

		} catch (Exception e) {
			success = false;
			code = 500; 
			body = "While reset password occurring error";
			e.printStackTrace();
		}
		return new Restponse<String>(success, code, body);
	}

}
