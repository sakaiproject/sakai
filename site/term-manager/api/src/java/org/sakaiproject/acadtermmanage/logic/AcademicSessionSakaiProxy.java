package org.sakaiproject.acadtermmanage.logic;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.exception.IdExistsException;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
public interface AcademicSessionSakaiProxy {
	
	
	public AcademicSession getAcademicSession(String eid) throws IdNotFoundException;
	
	public List<AcademicSession> getAcademicSessions();

	public AcademicSession createAcademicSession(String eid, String title, 
			String description, Date startDate, Date endDate) throws IdExistsException;

	public void removeAcademicSession(String eid);
		
	public void setCurrentAcademicSessions(List<String>eids);
	
	public List<AcademicSession> getCurrentAcademicSessions();
	
	public void updateAcademicSession(AcademicSession session);
	
	
	
	public List<Site> getSitesForTerm (String termEID);

	public Site getSite(String siteID) throws IdUnusedException;

	void saveSite(Site site)throws IdUnusedException, PermissionException;

	
	
	public void notifyEventServiceOfUpdate (String termEID);

	public void notifyEventServiceOfInsert (String termEID);
	
	
	
	public String getCurrentUserId();

	public String getCurrentSiteId();
	
	public Locale getCurrentUserLocale();

	
	
	
	public String getSkinRepoProperty();
	
	public String getToolSkinCSS(String skinRepo);
	
	
	
	
	/**
	 * Adds as Sakai permission (function) to Sakai if doesn't already exist. 
	 * 
	 * @param functionName name of the permission/function to register
	 */
	public void registerPermission(String functionName);	
	
	public boolean isUserAllowedFunction(String userID, String siteID, String function);

	public boolean isCurrentlyAllowed(String functionName);	
	
	public boolean isSuperUser();
	
	public void pushSecurityAdvisor(SecurityAdvisor securityOverride);

	public void popSecurityAdvisor(SecurityAdvisor securityOverride);

	AcademicSession addAcademicSession(Semester t) throws DuplicateKeyException;
	
	
}
