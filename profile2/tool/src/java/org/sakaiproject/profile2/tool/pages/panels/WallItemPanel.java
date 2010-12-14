/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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
package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Wall item container.
 */
public class WallItemPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	public WallItemPanel(String id, WallItem wallItem) {
		super(id);

		// image wrapper, links to profile
		Link<String> wallItemPhoto = new Link<String>(
				"wallItemPhotoWrap", new Model<String>(wallItem
						.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}
		};

		// image
		wallItemPhoto.add(new ProfileImageRenderer("wallItemPhoto",
				wallItem.getCreatorUuid()));
		add(wallItemPhoto);

		// name and link to profile
		Link<String> wallItemProfileLink = new Link<String>(
				"wallItemProfileLink", new Model<String>(wallItem
						.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}

		};
		wallItemProfileLink.add(new Label("wallItemName", wallItem
				.getCreatorName()));
		add(wallItemProfileLink);

		add(new Label("wallItemText", wallItem.getText()));
		add(new Label("wallItemDate", ProfileUtils
				.convertDateToString(wallItem.getDate(),
						"dd MMMMM, kk:mm")));
	}

}
