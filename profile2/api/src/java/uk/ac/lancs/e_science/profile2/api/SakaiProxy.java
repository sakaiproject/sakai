package uk.ac.lancs.e_science.profile2.api;

public interface SakaiProxy {
	
	public String getMessage();
	
	/**
	 * @return the current sakai user id (not username)
	 */
	public String getCurrentUserId();
}
