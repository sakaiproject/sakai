/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItemComment;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Wall comment container.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class WallItemCommentPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	public WallItemCommentPanel(String id, WallItemComment comment) {
		super(id);
		
		setOutputMarkupId(true);
		
		// image wrapper, links to profile
		Link<String> wallItemPhoto = new Link<String>("wallItemPhotoWrap",
				new Model<String>(comment.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}
		};

		// image		
		ProfileImage photo = new ProfileImage("wallItemPhoto", new Model<String>(comment.getCreatorUuid()));
		photo.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		wallItemPhoto.add(photo);
		
		add(wallItemPhoto);

		// name and link to profile
		Link<String> wallItemProfileLink = new Link<String>(
				"wallItemProfileLink", new Model<String>(comment
						.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}

		};
		wallItemProfileLink.add(new Label("wallItemCommentName", sakaiProxy
				.getUserDisplayName(comment.getCreatorUuid())));
		add(wallItemProfileLink);
		
		// content of the comment
		add(new Label("wallItemCommentText", new ResourceModel(comment.getText())));
				
		add(new Label("wallItemCommentDate", ProfileUtils.convertDateToString(comment
				.getDate(), ProfileConstants.WALL_DISPLAY_DATE_FORMAT)));
	}
}
