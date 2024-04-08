package com.semaifour.facesix.util;

import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;

@Controller
public class ValidationUtil {
	
	private final String MAILID_VALIDATION_STR = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	private final String MOBNUM_VALIDATION_STR = "\\d{10}";
	
	/**
	 * Used to validate Email ID and returns boolean value 
	 * @param emailId
	 * @return isValid boolean value
	 */
	
	public boolean isValidMailId(String emailId) {
		boolean isValid = false;
		if (emailId != null && Pattern.matches(MAILID_VALIDATION_STR, emailId)) {
			isValid = true;
		}
		return isValid;
	}
	
	/**
	 * Used to validate mobile Number and returns boolean value 
	 * @param mobileNumber
	 * @return isValid boolean value
	 */
	
	public boolean isValidMobileNumber(String mobileNumber) {
		boolean isValid = false;
		if (mobileNumber != null && Pattern.matches(MOBNUM_VALIDATION_STR, mobileNumber)) {
			isValid = true;
		}
		return isValid;
	}

}
