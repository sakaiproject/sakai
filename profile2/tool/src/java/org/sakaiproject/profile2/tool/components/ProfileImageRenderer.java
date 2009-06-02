package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.sakaiproject.profile2.api.Profile;
import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.profile2.api.SakaiProxy;
import org.sakaiproject.profile2.tool.ProfileApplication;

/** 
 * This is a helper panel for displaying a user's profile image.
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileImageRenderer extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Render a profile image for a user, based on the settings supplied
	 * @param id		- wicket:id
	 * @param userX		- user whose image we are showing
	 * @param allowed	- if this image is allowed to be viewed by the person requesting it. If false, a default image will be used.
	 * @param size		- ProfileImageManager.PROFILE_IMAGE_MAIN, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL
	 * @param cacheable	- if this image is allowed to be cached or not. If having issues with sticky images from AJAX updates, set this to false to ensure the image is updated every request
	 */
	public ProfileImageRenderer(String id, String userX, boolean allowed, int size, boolean cacheable) {
		super(id);
		
		//if we aren't allowed to view it, no use processing, just show default
		if(!allowed) {
			add(new ContextImage("img",new Model(getDefaultImage())));
			return;
		}
		
		//get API's
        SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
        Profile profile = ProfileApplication.get().getProfile();
                
		//what type of image are we to show?
		int type = sakaiProxy.getProfilePictureType();
		
		//UPLOAD
		if(type == ProfileConstants.PICTURE_SETTING_UPLOAD) {

			final byte[] bytes = profile.getCurrentProfileImageForUser(userX, size);
			
			//use profile bytes or add default image if none
			if(bytes != null && bytes.length > 0){
			
				BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
					protected byte[] getImageData() {
						return bytes;
					}
				};
				if(cacheable) {
					add(new Image("img",photoResource));
				} else {
					add(new NonCachingImage("img",photoResource));
				}
			} else {
				add(new ContextImage("img",new Model(getDefaultImage())));
			}
		
		//EXTERNAL IMAGE
		} else if (type == ProfileConstants.PICTURE_SETTING_URL) {
			
			String url = profile.getExternalImageUrl(userX, ProfileConstants.PROFILE_IMAGE_MAIN);
			
			//add uploaded image or default
			if(url != null) {
				add(new ExternalImage("img",url));
			} else {
				add(new ContextImage("img",new Model(getDefaultImage())));
			}
			
		//INVALID TYPE, SHOW DEFAULT
		} else {
			add(new ContextImage("img",new Model(getDefaultImage())));
		}
		
	}
	
	
	/**
	 * Get the default image to be used if not allowed or none available.
	 * @return
	 */
	private String getDefaultImage() {
		return ProfileConstants.UNAVAILABLE_IMAGE;
	}
	
	
}
