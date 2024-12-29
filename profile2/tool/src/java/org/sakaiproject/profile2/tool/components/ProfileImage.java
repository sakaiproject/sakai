package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.sakaiproject.profile2.util.ProfileConstants;
import lombok.Setter;

/**
 * Renders a user's profile image via the  <sakai-user-photo> webcomponent.
 * <p>
 * Attach to a  <sakai-user-photo> tag and provide the user uuid in the model
 * <p>
 * e.g.
 * <code>
 * <sakai-user-photo wicket:id="photo" />
 * </code>
 * <br />
 * <code>
 * add(new ProfileImage("photo", new Model<String>(userUuid)));
 * </code>
 * <p>
 * For different size images, override the setSize
 * <p>
 * Note that browsers will cache the image for a while (this is the point!) so if users are changing images all the time, a browser cache flush is in order for the display to remain current.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
public class ProfileImage extends WebComponent {

	private static final long serialVersionUID = 1L;
	private int size = ProfileConstants.PROFILE_IMAGE_MAIN; //default
	private String siteId;

	public ProfileImage(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "sakai-user-photo");

		String userUuid = this.getDefaultModelObjectAsString();
		tag.put("user-id", userUuid != null ? userUuid : "");

		if (siteId != null) {
			tag.put("site-id", siteId);
		}

		String sizeClass = "large"; // default for main profile picture.
		switch (this.size) {
			case ProfileConstants.PROFILE_IMAGE_MAIN: {
				sizeClass = "medium";
				break;
			}
			case ProfileConstants.PROFILE_IMAGE_THUMBNAIL, ProfileConstants.PROFILE_IMAGE_AVATAR: {
				sizeClass = "large-thumbnail";
				break;
			}
        }
		tag.put("classes", sizeClass);
		tag.put("profile-popup", "off"); // Always turn off
	}
	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		// Nothing to render, the web component handles the rendering.
	}
}