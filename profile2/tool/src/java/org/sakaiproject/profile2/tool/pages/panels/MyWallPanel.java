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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.dataproviders.WallItemDataProvider;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * User's wall panel.
 */
public class MyWallPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	/**
	 * Creates a new instance of <code>MyWallPanel</code>.
	 */
	public MyWallPanel(String id) {

		super(id);

		// pass in user IDs and check privacy in here.
		
		WallItemDataProvider provider = new WallItemDataProvider(sakaiProxy
				.getCurrentUserId());

		// container which wraps list
		final WebMarkupContainer wallItemsContainer = new WebMarkupContainer(
				"wallItemsContainer");

		wallItemsContainer.setOutputMarkupId(true);

		// if no wall items, display a message
		if (0 == provider.size()) {
			wallItemsContainer.add(new Label("wallInformationMessage",
					new ResourceModel("text.wall.no.items")));
		} else {
			wallItemsContainer.add(new Label("wallInformationMessage"));
		}

		// TODO haven't decided whether to add a navigator yet

		DataView<WallItem> wallItemsDataView = new DataView<WallItem>(
				"wallItems", provider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<WallItem> item) {

				WallItem wallItem = (WallItem) item.getDefaultModelObject();

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
				item.add(wallItemPhoto);

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
				item.add(wallItemProfileLink);

				item.add(new Label("wallItemText", wallItem.getText()));
				item.add(new Label("wallItemDate", ProfileUtils
						.convertDateToString(wallItem.getDate(),
								"dd MMMMM, kk:mm")));

			}
		};

		wallItemsDataView.setOutputMarkupId(true);
		// wallItemsDataView.setItemsPerPage(10);

		wallItemsContainer.add(wallItemsDataView);
		add(wallItemsContainer);
	}

}
