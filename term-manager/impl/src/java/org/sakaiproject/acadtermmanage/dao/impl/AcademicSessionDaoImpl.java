package org.sakaiproject.acadtermmanage.dao.impl;

import java.util.List;

import org.sakaiproject.acadtermmanage.dao.AcademicSessionDao;
import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.logic.AcademicSessionSakaiProxy;
import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.exception.IdExistsException;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 *  A bit of an overkill: it just passes the method calls on to the Sakai proxy object.
 *  Then again, it might be a good idea to catch the Exceptions thrown by the Coursemanagement-API 
 *  and replace them with our own so that more general AcademicSessionDao Interface doesn't introduce an unnecessary 
 *  dependency on the Coursemanagement-API simply because of the Exceptions this implementation uses.   
 *  
 *   
 */
@Slf4j
public class AcademicSessionDaoImpl implements AcademicSessionDao {
		
	@Setter
	private AcademicSessionSakaiProxy cm_api;
	
	@Override
	public AcademicSession getAcademicSession(String eid){
		try {			
			return cm_api.getAcademicSession(eid);
		}
		catch (IdNotFoundException e) {
			log.debug("EID not found: \""+eid+"\"");
			return null;
		}
	}

	@Override
	public List<AcademicSession> getAcademicSessions() {
		return cm_api.getAcademicSessions();
	}

	@Override
	public AcademicSession addAcademicSession(Semester t) throws DuplicateKeyException {
		String eid = t.getEid();
		try {
			return cm_api.createAcademicSession(eid, t.getTitle(),
				t.getDescription(), t.getStartDate(), t.getEndDate());
		}
		catch (IdExistsException e) {
			String msg ="EID already in DB: \""+eid+"\"";		
			if  (log.isDebugEnabled()) {				 
				log.debug(msg);
				// WTF? The API seems to throw IdExistsExceptions for every problem.
				// Not only existing IDs, but also when you try to insert a null value into a "NOT NULL" DB column
				// (e.g. when the Semester doesn't have a description or title).
				// Let's see if the exception message is at least different in those cases..:  
				log.debug("original message: "+e.getMessage());
			}			
			throw new DuplicateKeyException(msg);
		}
	}

	
	
	@Override
	public void removeAcademicSession(String eid) {
		 cm_api.removeAcademicSession(eid);	
	}
	
	@Override
	public List<AcademicSession> getCurrentSessions(){
		return cm_api.getCurrentAcademicSessions();
	}

	@Override
	public void setCurrentSessions(List<String> eids) {
		cm_api.setCurrentAcademicSessions(eids);		
	}
	
	
	public void init() {
		// just here to please the bean definition in components.xml
	}

	@Override
	public void updateAcademicSession(AcademicSession session) {
		cm_api.updateAcademicSession(session);
		
	}

	@Override
	public List<Site> getSitesForTerm (String termEID) {
		return cm_api.getSitesForTerm(termEID);
	}
	
	@Override
	public Site getSite(String siteID){
		try {
			return cm_api.getSite(siteID);
		}
		catch (IdUnusedException e) {
			log.debug("site id unused: "+siteID);
			return null;
		}
	}
	@Override
	public boolean  saveSite(Site site){
		try {
			cm_api.saveSite(site);
			return true;
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
	
}
