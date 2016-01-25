package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.sakaiproject.profile2.util.ProfileConstants;
import java.util.concurrent.TimeUnit;

/**
 * Renders a user's profile image via the direct entity URL. 
 * 
 * Attach to an img tag and provide the user uuid in the model
 * 
 * e.g.
 * <code>
 * &lt;img wicket:id="photo" /&gt;
 * </code>
 * <br />
 * <code>
 * add(new ProfileImage("photo", new Model<String>(userUuid)));
 * </code>
 * 
 * For different size images, override the setSize
 * 
 * Note that browsers will cache the image for a while (this is the point!) so if users are changing images all the time, a browser cache flush is in order for the display to remain current.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class ProfileImage extends WebComponent {

	private static final long serialVersionUID = 1L;
	
	private int size = ProfileConstants.PROFILE_IMAGE_MAIN; //default

	public ProfileImage(String id, IModel<String> model) {
	    super(id, model);
	}
	
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "img");
		
		String userUuid = this.getDefaultModelObjectAsString();
		
		//determine size
		String sizePart = "";
		switch (this.size) {
			case ProfileConstants.PROFILE_IMAGE_MAIN: {
				break;
			}
			case ProfileConstants.PROFILE_IMAGE_THUMBNAIL: {
				sizePart = "/thumb";
				break;
			}
			case ProfileConstants.PROFILE_IMAGE_AVATAR: {
				sizePart = "/avatar";
				break;
			}
		}
		
		//Cache for a minute
		String url = "/direct/profile/"+userUuid + "/image" + sizePart + "?t=" + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
		
		tag.put("src", url);
	}
	
	/**
	 * Use to specify ProfileConstants.PROFILE_IMAGE_THUMBNAIL or ProfileConstants.PROFILE_IMAGE_AVATAR
	 * Leave as is to use the main size image
	 * 
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	
}
