package org.sakaiproject.tool.resetpass;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.component.api.ServerConfigurationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

public class UserValidator implements Validator {

	private static Log m_log  = LogFactory.getLog(UserValidator.class);
	
	public boolean supports(Class clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(User.class);
	}

	public String userEmail;
	
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService ds){
		this.userDirectoryService = ds;
	}
	
	public void validate(Object obj, Errors errors) {
		// TODO Auto-generated method stub
		RetUser retUser = (RetUser)obj;
		m_log.debug("validating user " + retUser.getEmail());
		
		Collection c = this.userDirectoryService.findUsersByEmail(retUser.getEmail().trim());
		if (c.size()>1) {
			m_log.debug("more than one email!");
			errors.reject("morethanone","more than one email");
			return;
		} else if (c.size()==0) {
			m_log.debug("no such email");
			errors.reject("nosuchuser","no such user");
			return;
		}
		Iterator i = c.iterator();
		User user = (User)i.next();
		m_log.debug("got user " + user.getId() + " of type " + user.getType());
		String[] roles = serverConfigurationService.getStrings("resetRoles");
		if (roles == null ){
			roles = new String[]{"guest"};
		}
		List rolesL = Arrays.asList(roles);
		if (!rolesL.contains(user.getType())) {
			m_log.warn("this is a type don't change");
			errors.reject("wrongtype","wrong type");
			return;
		}
		
		retUser.setUser(user);
	}

}
