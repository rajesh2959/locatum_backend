package com.semaifour.facesix.account.role.rest;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.account.PrivilegeService;
import com.semaifour.facesix.account.role.RoleService;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

@RequestMapping("/rest/role")
@RestController
public class RoleRestController extends WebController {

	Logger LOG = LoggerFactory.getLogger(RoleRestController.class.getName());

	@Autowired
	RoleService roleService;

	@Autowired
	PrivilegeService privilegeService;
	
	@Autowired
	UserAccountService userAccountService;
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public @ResponseBody Iterable<String> list(HttpServletRequest request) {

		List<String> roleList = null;
		
		if (SessionUtil.isAuthorized(request.getSession())) {
			
			UserAccount currentuser = userAccountService.findOneByUid(SessionUtil.currentUser(request.getSession()));

			if (currentuser != null) {
				String curRole = currentuser.getRole();

				try {

					if (curRole.equalsIgnoreCase(UserAccount.ROLE.superadmin.name())) {
						roleList = roleService.roleList();
					} else if (curRole.equalsIgnoreCase(UserAccount.ROLE.appadmin.name())) {
						roleList = new ArrayList<String>();
						roleList.add(UserAccount.ROLE.appadmin.name());
						roleList.add(UserAccount.ROLE.siteadmin.name());
						roleList.add(UserAccount.ROLE.sysadmin.name());
						roleList.add(UserAccount.ROLE.useradmin.name());
						roleList.add(UserAccount.ROLE.user.name());
					} else if (curRole.equalsIgnoreCase(UserAccount.ROLE.useradmin.name())) {
						roleList = new ArrayList<String>();
						roleList.add(UserAccount.ROLE.user.name());
					}

				} catch (Exception e) {
					LOG.error("Getting Role List Error ", e);
				}
			} else {
				LOG.info("Account not found given session Id" +request.getSession());
			}
			
		}
		
		return roleList;

	}
}
