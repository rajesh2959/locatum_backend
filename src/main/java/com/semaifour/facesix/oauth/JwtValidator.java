package com.semaifour.facesix.oauth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;

@Component
public class JwtValidator {

	Logger LOG = LoggerFactory.getLogger(JwtValidator.class.getName());

	@Autowired
	CustomerService customerService;
	
    public Customer validate(String token) {
    	return customerService.findOneByRestToken(token);
    }
}
