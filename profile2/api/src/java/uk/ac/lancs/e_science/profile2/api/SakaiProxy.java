package uk.ac.lancs.e_science.profile2.api;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.type.Type;

public interface SakaiProxy {
	
	public String getCurrentSiteId();
	
	public String getCurrentUserId();
	
	public String getUserEid(String userId);

	public String getUserDisplayName(String userId);
	
	public String getUserEmail(String userId);
	
	public boolean isUserAdmin(String userId);
	
	public SakaiPerson getSakaiPerson(String userId);
	
	public SakaiPerson getSakaiPersonPrototype();
	
	public boolean updateSakaiPerson(SakaiPerson sakaiPerson);
	
}
