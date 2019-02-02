package org.sakaiproject.acadtermmanage.dao;

import java.util.List;

import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.site.api.Site;

public interface AcademicSessionDao {

	public AcademicSession getAcademicSession(String eid);
	
	public List<AcademicSession> getAcademicSessions();
		
	public List<AcademicSession> getCurrentSessions();

	public void setCurrentSessions(List<String>eids);
	
	public AcademicSession addAcademicSession(Semester t) throws DuplicateKeyException;
	
	public void removeAcademicSession(String eid);
	
	public void updateAcademicSession(AcademicSession session);

	public List<Site> getSitesForTerm (String termEID);

	public Site getSite(String siteID);
	
	public boolean  saveSite(Site site);

}
