package com.semaifour.facesix.account.role;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.semaifour.facesix.data.account.UserAccount;

@Service
public class RoleService {

	Logger LOG = LoggerFactory.getLogger(RoleService.class.getName());

	
	
	@Autowired(required = false)
	private RoleRepository repository;

	public RoleService() {
	}

	public List<Role> findByRoleName(String name) {
		return repository.findByRoleName(QueryParser.escape(name));
	}

	public List<Role> findByUid(String uid) {
		return repository.findByUid(QueryParser.escape(uid));
	}

	public Role findById(String id) {
		return repository.findOne(QueryParser.escape(id));
	}

	public boolean exists(String id) {
		return repository.exists(QueryParser.escape(id));
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public void delete(String id) {
		repository.delete(QueryParser.escape(id));
	}

	public void delete(Role role) {
		repository.delete(role);
	}

	public long count() {
		return repository.count();
	}

	public Role save(Role role) {
		return save(role, true);
	}

	public Role save(Role role, boolean notify) {
		role = repository.save(role);
		return role;
	}

	public Iterable<Role> findAll() {
		return repository.findAll();
	}
	
	public List<String> roleList() {
		List<String> roleList = new ArrayList<String>();
		for (UserAccount.ROLE role : UserAccount.ROLE.values()) {
			roleList.add(role.name());
		}
		return roleList;
	}
	/**
	 * Used to check if the given role is available in RoleList
	 * @param role
	 * @return isRolePresent boolean value
	 */
	public boolean isRoleAvailable(String role) {
		boolean isRolePresent = false;
		List<String> roleList = roleList();
		if (role != null) {
			for (String itr : roleList) {
				if (itr.equalsIgnoreCase(role)) {
					isRolePresent = true;
					break;
				}
			}
		}
		return isRolePresent;
	}
}
