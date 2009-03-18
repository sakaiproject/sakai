package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;

/** 
 * This is a helper panel for displaying a user's profile image in a div.
 * Friend status and if the image is allowed to be viewed by a person should already be determined at this stage.
 * @author swinsbur
 *
 */
public class ProfileImageRenderer extends Panel {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Render a profile image for a user, based on the settings supplied
	 * @param id		- wicket:id
	 * @param userX		- user whose image we are showing
	 * @param size		- ProfileImageManager.PROFILE_IMAGE_MAIN, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL
	 */
	public ProfileImageRenderer(String id, String userX, int size) {
		super(id);
		
		//get API's
        SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
        Profile profile = ProfileApplication.get().getProfile();
                
		//what type of image are we to show?
		int type = sakaiProxy.getProfilePictureType();
		
		//UPLOAD
		if(type == ProfileImageManager.PICTURE_SETTING_UPLOAD) {

			final byte[] bytes = profile.getCurrentProfileImageForUser(userX, size);
			
			//use profile bytes or add default image if none
			if(bytes != null && bytes.length > 0){
			
				BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
					protected byte[] getImageData() {
						return bytes;
					}
				};
			
				add(new Image("img",photoResource));
			} else {
				add(new ContextImage("img",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
			}
		
		//EXTERNAL URL
		} else if (type == ProfileImageManager.PICTURE_SETTING_URL) {
			
			String url = profile.getExternalImageUrl(userX, ProfileImageManager.PROFILE_IMAGE_MAIN, true);
			
			//add uploaded iamge or default
			if(url != null) {
				add(new ExternalImage("img",url));
			} else {
				add(new ContextImage("img",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
			}
			
		//INVALID TYPE, SHOW DEFAULT
		} else {
			add(new ContextImage("img",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
		}
		
	}

}
