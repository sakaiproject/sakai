package org.sakaiproject.profile2.tool.components;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.image.resource.LocalizedImageResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Profile2 ProfileImageRenderer component.
 * 
 * <p>This component should be used whenever you want to render a user's profile image. 
 * Choose the most appropriate constructor for your needs and situation.</p>
 * 
 * <p>Note that in order to request another user's image you should supply either a full Person object
 * containing the Privacy settings, or the ProfilePrivacy settings directly. If you do not have this information
 * you can pass null as the ProfilePrivacy attribute and it will be consulted for you.</p>
 * 
 * <p>If you do not provide a ProfilePreferences object (or Person object containing this info), it will be looked up.</p>
 * 
 * <p>If you do not provide the size or cache settings, they will be defaults (size=main, cache=true).
 * 
 * <p>In short, always provide all information (and preferably a full Person object)</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileImageRenderer extends Image implements IResourceListener {
	
	private static final long serialVersionUID = 1L;

	private String userUuid;
	private boolean cache;
	private int size;
	private ProfilePreferences prefs;
	private ProfilePrivacy privacy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileImageLogic")
	private ProfileImageLogic imageLogic;
	
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * Minimal constructor. Use this for when requesting your own image and only if you don't have access to the ProfilePreferences object. 
	 * Will lookup ProfilePreferences and uses defaults for size(main) and cache(true). 
	 *
	 * @param id		markup ID
	 * @param userUuid	uuid of the user to retrieve the image for
	 */
	public ProfileImageRenderer(final String id, final String userUuid) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		
		//set defaults
		this.prefs = null;
		this.privacy = null;
		this.size = getDefaultSize();
		this.cache = getDefaultCache();
	}
	
	/**
	 * Minimal constructor. Use this for when requesting your own image. Uses defaults for size(main) and cache(true). 
	 *
	 * @param id		markup ID
	 * @param userUuid	uuid of the user to retrieve the image for
	 * @param prefs		ProfilePreferences object for the user
	 */
	public ProfileImageRenderer(final String id, final String userUuid, final ProfilePreferences prefs) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		this.prefs = prefs;
		
		//set defaults
		this.privacy = null;
		this.size = getDefaultSize();
		this.cache = getDefaultCache();
	}
	
	/**
	 * Minimal constructor. Use this for when requesting your own image and want to control the size and cache setting.
	 *
	 * @param id		markup ID
	 * @param userUuid	uuid of the user to retrieve the image for.
	 * @param prefs		ProfilePreferences object for the user.
	 * @param size		image size: 1 for main, 2 for thumbnail.
	 * @param cache		if this image is allowed to be cached by the browser or not. If having issues with
	 * 					dynamic images sticking from AJAX updates, set this to false to ensure the image is updated every request.
	 */
	public ProfileImageRenderer(final String id, final String userUuid, final ProfilePreferences prefs, final int size, final boolean cache) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		this.prefs = prefs;
		this.size = size;
		this.cache = cache;
		
		//set defaults
		this.privacy = null;
	}
	
	/**
	 * Minimal constructor. Use this when requesting someone else's image. Uses defaults for size and cache.
	 * @param id		markup ID
	 * @param userUuid	uuid of the user to retrieve the image for
	 * @param prefs		ProfilePreferences object for the user
	 * @param privacy	ProfilePrivacy object for the user
	 */
	public ProfileImageRenderer(final String id, final String userUuid, final ProfilePreferences prefs, final ProfilePrivacy privacy) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		this.privacy = privacy;
		this.prefs = prefs;
		
		//set defaults
		this.size = getDefaultSize();
		this.cache = getDefaultCache();
	}
	
	/**
	 * Full constructor where each item is explicitly provided.
	 * @param id		markup ID
	 * @param userUuid	uuid of the user to retrieve the image for
	 * @param prefs		ProfilePreferences object for the user
	 * @param privacy	ProfilePrivacy object for the user
	 * @param size		image size: 1 for main, 2 for thumbnail.
	 * @param cache		if this image is allowed to be cached by the browser or not. If having issues with
	 * 					dynamic images sticking from AJAX updates, set this to false to ensure the image is updated every request.
	 */
	public ProfileImageRenderer(final String id, final String userUuid, final ProfilePreferences prefs, final ProfilePrivacy privacy, final int size, final boolean cache) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		this.privacy = privacy;
		this.prefs = prefs;
		this.size = size;
		this.cache = cache;
		
	}
	
	

	/**
	 * Full constructor that takes a Person object instead of split data. Defaults will be used for size(main) and cache(true).
	 * @param id		markup ID
	 * @param person	Person object for the user containing all data
	 */
	public ProfileImageRenderer(final String id, final Person person) {
		super(id);
		
		//extract data
		this.userUuid = person.getUuid();
		this.prefs = person.getPreferences();
		this.privacy = person.getPrivacy();
		
		//set defaults
		this.size = getDefaultSize();
		this.cache = getDefaultCache();
		
	}
	
	
	/**
	 * Full constructor that takes a Person object and allows control over the size and cache settings.
	 *
	 * @param id		markup ID
	 * @param person	Person object for the user containing all data
	 * @param size		image size: 1 for main, 2 for thumbnail.
	 * @param cache		if this image is allowed to be cached by the browser or not. If having issues with
	 * 					dynamic images sticking from AJAX updates, set this to false to ensure the image is updated every request.
	 */
	public ProfileImageRenderer(final String id, final Person person, final int size, final boolean cache) {
		super(id);
				
		//extract data
		this.userUuid = person.getUuid();
		this.prefs = person.getPreferences();
		this.privacy = person.getPrivacy();
		
		//set incoming
		this.size = size;
		this.cache = cache;
	}
	
	/**
	 * @see org.apache.wicket.IResourceListener#onResourceRequested()
	 */
	public void onResourceRequested(){
		localizedImageResource.onResourceRequested();
	}
	
	/**
	 * Render the tag
	 */
	@Override
	public void onComponentTag(final ComponentTag tag) {
		super.onComponentTag(tag);
		
		//get the image
		ProfileImage image = imageLogic.getProfileImage(userUuid, prefs, privacy, size);
		
		//do binary
		final byte[] bytes = image.getBinary();
		if(bytes != null && bytes.length > 0) {
			
			BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
				private static final long serialVersionUID = 1L;

				protected byte[] getImageData() {
					return bytes;
				}
			};
			
			localizedImageResource.setResource(photoResource);
			localizedImageResource.setSrcAttribute(tag);
			
			if(!cache){
				addNoCacheNoise(tag);
			}
			
			//add alt text
			tag.put("alt", image.getAltText());
			
			return;
		}
		
		//do url
		String url = image.getUrl();
		if(StringUtils.isNotBlank(url)) {
			tag.put("src", url);
			/* DO NOT add cache noise to URL based images as they won't stick, it's only the dynamic ones that can sometimes.
			if(!cache){
				addNoCacheNoise(tag);
			}
			*/
			//add alt text
			tag.put("alt", image.getAltText());
			return;
		}
		
		//do default
		tag.put("src", getDefaultImage(size));
		
		//add alt text
		tag.put("alt", image.getAltText());
	}
	
	
	
	
	
	/**
	 * Get the default image URL
	 * 
	 * @param size image size: 1 for main, 2 for thumbnail.
	 * @return
	 */
	private String getDefaultImage(final int size) {

		if (ProfileConstants.PROFILE_IMAGE_THUMBNAIL == size) {
			return getRequest().getRelativePathPrefixToContextRoot() + ProfileConstants.UNAVAILABLE_IMAGE_THUMBNAIL;
		} 
		
		return getRequest().getRelativePathPrefixToContextRoot() + ProfileConstants.UNAVAILABLE_IMAGE;
	}
	
	/**
	 * Get the default image size
	 * @return
	 */
	private int getDefaultSize() {
		return ProfileConstants.PROFILE_IMAGE_MAIN;
	}
	
	/**
	 * Get the default cache setting
	 * @return
	 */
	private boolean getDefaultCache() {
		return ProfileConstants.PROFILE_IMAGE_CACHE;
	}
	
	
	
	
	/**
	 * Add noise to the image URL, similar to Wicket's NonCachingImage.
	 * 
	 * @param tag
	 * @param url
	 * @return
	 */
	private void addNoCacheNoise(ComponentTag tag) {
		StringBuilder url = new StringBuilder();
		String tagSrc = tag.getAttributes().getString("src");
		url.append(tagSrc);
		
		url.append((tagSrc.indexOf('?') >= 0) ? "&" : "?");
		url.append("wicket:antiCache=" + System.currentTimeMillis());
		
		tag.put("src", url);
	}
	
	
}
