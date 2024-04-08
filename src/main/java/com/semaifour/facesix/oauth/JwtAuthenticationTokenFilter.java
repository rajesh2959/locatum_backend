package com.semaifour.facesix.oauth;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;

public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

	
	Logger LOG = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class.getName());
	
	
	@Autowired
	CustomerService customerService;
	
    public JwtAuthenticationTokenFilter() {
        super(JwtSecurityConfig.getWhiteListRestApi());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {

    	String apiKey = httpServletRequest.getHeader("API-Key");
        
        if (apiKey == null || apiKey.isEmpty()) {
        	throw new JwtTokenMissingException("UNAUTHORIZED TOKEN");
        }

        Customer customer = customerService.findOneByRestToken(apiKey);
        if(customer != null) {
        	
        		apiKey = customer.getJwtrestToken();
        	
        	JwtAuthenticationToken token = new JwtAuthenticationToken(apiKey);
            return getAuthenticationManager().authenticate(token);
        } else {
        	throw new JwtTokenMissingException("UNAUTHORIZED TOKEN");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
      super.successfulAuthentication(request, response, chain, authResult);
      chain.doFilter(request, response);
    }
}
