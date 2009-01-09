package uk.ac.lancs.e_science.profile2.api;

public class ProfileImage {

	//default if not specified in sakai.properties as profile.picture.max (megs)
	public static final int MAX_PROFILE_IMAGE_UPLOAD_SIZE = 2; 
	
	//one side will be scaled to this if larger. 400 is large enough (TODO can be overriden)
	public static final int MAX_IMAGE_XY = 400; 	
    

}
