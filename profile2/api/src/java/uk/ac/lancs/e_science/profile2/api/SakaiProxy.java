package uk.ac.lancs.e_science.profile2.api;

public interface SakaiProxy {
	
	public String getCurrentSiteId();
	
	public String getCurrentUserId();
	
	public String getUserEid(String userId);

	public String getUserDisplayName(String userId);
	
	public String getUserEmail(String userId);
	
	public boolean isUserAdmin(String userId);
}
