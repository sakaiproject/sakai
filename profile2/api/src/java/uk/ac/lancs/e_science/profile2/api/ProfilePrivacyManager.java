package uk.ac.lancs.e_science.profile2.api;


public class ProfilePrivacyManager {

	//setup the profile privacy values
	public static final int PRIVACY_OPTION_EVERYONE = 0; 
	public static final int PRIVACY_OPTION_ONLYFRIENDS = 1; 
	public static final int PRIVACY_OPTION_ONLYME = 2; 

	//TODO allow these to be overriden in sakai.properties
	
	//these values are also used when creating a default privacy record for a user
	
	//if they have no privacy record, ie have not turned explicitly turned searches on or off
	public static final boolean DEFAULT_SEARCH_VISIBILITY = true;
	
	//if they have no privacy record, ie have not turned explicitly turned profile and image visibility on or off
	public static final boolean DEFAULT_PROFILE_VISIBILITY = true;
	
	//if they have no privacy record, ie have not turned explicitly turned birth year display on or off
	public static final boolean DEFAULT_BIRTHYEAR_VISIBILITY = false;
	
	//if the user doing a search finds themself in the results, should they be included in the results?
	public static final boolean SELF_SEARCH_VISIBILITY = true;
	
	
}
