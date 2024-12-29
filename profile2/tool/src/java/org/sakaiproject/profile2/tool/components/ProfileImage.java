/**
 * Copyright (c) 2008-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
