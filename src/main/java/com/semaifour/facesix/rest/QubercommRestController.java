package com.semaifour.facesix.rest;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.web.WebController;
import com.semaifour.facesix.web.WelcomeController;
import net.sf.json.JSONObject;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class QubercommRestController extends WebController {

	Logger LOG=LoggerFactory.getLogger(QubercommRestController.class.getName());
	
	@Autowired	private UserAccountService userService;
	
	@Autowired private WelcomeController welcomeController;
	
	@RequestMapping(value = "/resetpassword")
	public Restponse<String> changepwd(@RequestParam(value = "user", required = true) String data,
							HttpServletRequest request, HttpServletResponse response) {
		
		
		boolean success = true;
		int code 		= 200;
		String body 	= "Your password has been reset successfully!.";
		
		try {

			net.sf.json.JSONObject myData = JSONObject.fromObject(data);
			
			LOG.info("myData" 		+ myData);
			
			String userId 	  = (String)myData.get("userId");
			String token 	  = (String)myData.get("token");
			String password   = (String)myData.get("password");
			String cpassword  = (String)myData.get("cpassword");
	
			UserAccount user = userService.findById(userId);
		
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
				} else	if (user.getToken().equals(token)) {
					if (StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(cpassword) && password.equals(cpassword)) {
						user.setResetStatus(false);
						user.setPassword(_CCC.cryptor.encrypt(password));
						user = userService.saveContact(user);
					} else {
						success = false;
						code 	= 400; // Bad Request
						body 	= "Password and confirm password not matched.";	
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