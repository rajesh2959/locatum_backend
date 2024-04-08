package com.semaifour.facesix.oauth;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;

@Component
public class JwtAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	Logger LOG=LoggerFactory.getLogger(JwtAuthenticationProvider.class.getName());
	
    @Autowired
    private CustomerService customerService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    /**
     * 
     * @param username
     * @param usernamePasswordAuthenticationToken
     * @return
     * @throws AuthenticationException
     * 
     * This function retrieves the user details and authenticates it.
     */
    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) usernamePasswordAuthenticationToken;
        String token 								  = jwtAuthenticationToken.getToken();
       
        Customer customer = customerService.findOneByJwtrestToken(token);
        
        if (customer == null) {
        	throw new JwtTokenMissingException("UNAUTHORIZED TOKEN");
        }
        
        String cid = customer.getId();
        
       /*
        * Grant authority to the customer.
        */
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(cid);
        
        return new JwtUserDetails(cid,token,grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return (JwtAuthenticationToken.class.isAssignableFrom(aClass));
    }
}
