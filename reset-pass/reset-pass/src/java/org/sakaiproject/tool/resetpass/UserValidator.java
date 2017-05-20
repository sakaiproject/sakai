package org.sakaiproject.tool.resetpass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {

	private static Logger m_log  = LoggerFactory.getLogger(UserValidator.class);

	// prefix for error messages - indicates they are to be pulled from tool configuration rather than a resource bundle
	private final String TOOL_CONFIG_PREFIX = "toolconfig_";
	
	public boolean supports(Class clazz) {
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
	
	private SecurityService securityService;
	public void setSecurityService(SecurityService ss){
		this.securityService = ss;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager tm) {
		this.toolManager = tm;
	}
	
	public void validate(Object obj, Errors errors) {
		RetUser retUser = (RetUser)obj;
		m_log.debug("validating user " + retUser.getEmail());

		if (retUser.getEmail() == null || "".equals(retUser.getEmail()))
		{
			m_log.debug("no email provided");
			errors.reject("noemailprovided", "no email provided");
			return;
		}
		
		Collection<User> c = this.userDirectoryService.findUsersByEmail(retUser.getEmail().trim());
		if (c.size()>1) {
			m_log.debug("more than one email!");
			errors.reject("morethanone","more than one email");
			return;
		} else if (c.size()==0) {
			m_log.debug("no such email");
			errors.reject("nosuchuser","no such user");
			return;
		}
		Iterator<User> i = c.iterator();
		User user = (User)i.next();
		m_log.debug("got user " + user.getId() + " of type " + user.getType());
		if (securityService.isSuperUser(user.getId())) {
			m_log.warn("tryng to change superuser password");
			rejectWrongType(errors);
			return;
		}
		boolean allroles = serverConfigurationService.getBoolean("resetPass.resetAllRoles",false);
		if (!allroles){
			// SAK-24379 - deprecate the resetRoles property
			String[] roles = serverConfigurationService.getStrings("accountValidator.accountTypes.accept");
			String[] rolesOld = serverConfigurationService.getStrings("resetRoles");
			if (rolesOld != null)
			{
				m_log.warn("Found the resetRoles property; it is deprecated in favour of accountValidator.accountTypes.accept");
				if (roles == null)
				{
					roles = rolesOld;
				}
			}
		    if (roles == null ){
		        roles = new String[]{"guest"};
		    }
		    List<String> rolesL = Arrays.asList(roles);
		    if (!rolesL.contains(user.getType())) {
		        m_log.warn("this is a type don't change");
		        rejectWrongType(errors);
		        return;
		    }
		}
		retUser.setUser(user);
	}

	/**
	 * Explains that the user's type is incorrect.
	 * Looks for a custom message in the tool properties first,
	 * if there is no custom message, it goes to the message bundle
	 */
	private void rejectWrongType(Errors errors)
	{
		Placement placement = toolManager.getCurrentPlacement();
		String toolPropWrongType = placement.getConfig().getProperty("wrongtype");
		if (StringUtils.isBlank(toolPropWrongType))
		{
			errors.reject("wrongtype", "wrong type");
		}
		else
		{
			errors.reject(TOOL_CONFIG_PREFIX + "wrongtype", toolPropWrongType);
		}
	}

}
