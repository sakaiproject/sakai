package org.sakaiproject.scorm.service.sakai.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public abstract class SakaiStatefulService implements LearningManagementSystem, ScormConstants {

	private static Log log = LogFactory.getLog(SakaiStatefulService.class);
	
	protected abstract ServerConfigurationService configurationService();
	protected abstract SecurityService securityService();
	protected abstract SessionManager sessionManager();
	protected abstract SiteService siteService();
	protected abstract ToolManager toolManager();
	protected abstract UserDirectoryService userDirectoryService();
	
	protected abstract LearnerDao learnerDao();
	
	public String currentContext() {
		return toolManager().getCurrentPlacement().getContext();
	}
	
	public String currentLearnerId() {
		String learnerId = sessionManager().getCurrentSessionUserId();
		
		return learnerId;
	}
	
	public boolean canLaunchNewWindow() {
		return true;
	}
	
	public boolean canUseRelativeUrls() {
		return false;
	}
	
	public boolean canModify(String context) {
		return canConfigure(context) || canDelete(context) || canViewResults(context);
	}
	
	public boolean canConfigure(String context) {
		return hasPermission(context, "scorm.configure");
	}

	public boolean canDelete(String context) {
		return hasPermission(context, "scorm.delete");
	}

	public boolean canLaunch(String context) {
		return hasPermission(context, "scorm.launch");
	}

	public boolean canUpload(String context) {
		return hasPermission(context, "scorm.upload");
	}

	public boolean canValidate(String context) {
		return hasPermission(context, "scorm.validate");
	}

	public boolean canViewResults(String context) {
		return hasPermission(context, "scorm.grade");
	}
	
	public boolean isOwner() {
		return true;
	}
	
	public String getLearnerName(String learnerId) {
		String displayName = null;
		try {
			User user = userDirectoryService().getUser(learnerId);
			
			if (user != null)
				displayName = user.getDisplayName();
			
		} catch (UserNotDefinedException e) {
			log.error("Could not determine display name for user " + learnerId, e);
		}
		
		return displayName;
	}
	
	public Learner getLearner(String learnerId) throws LearnerNotDefinedException {
		return learnerDao().load(learnerId);
	}
	
	protected boolean hasPermission(String context, String lock) {
		String reference = siteService().siteReference(context);
		
		return unlockCheck(lock, reference);
	}
	
	protected boolean unlockCheck(String lock, String ref)
	{
		boolean isAllowed = securityService().isSuperUser();
		if(! isAllowed)
		{
			// make a reference from the resource id, if specified
			/*String ref = null;
			if (id != null)
			{
				ref = siteService().siteReference(id);
			}*/
			
			isAllowed = ref != null && securityService().unlock(lock, ref);
		}
		
		return isAllowed;
		
	}

	
}
