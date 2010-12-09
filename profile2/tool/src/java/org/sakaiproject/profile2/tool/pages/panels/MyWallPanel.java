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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileStatusLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.dataproviders.ConfirmedFriendsDataProvider;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * User's wall panel.
 */
public class MyWallPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileStatusLogic")
	private ProfileStatusLogic statusLogic;
	
	/**
	 * 
	 */
	public MyWallPanel(String id) {

		super(id);

		// TODO WallItemDataProvider
		ConfirmedFriendsDataProvider provider = new ConfirmedFriendsDataProvider(
				sakaiProxy.getCurrentUserId());

		// container which wraps list
		final WebMarkupContainer wallItemsContainer = new WebMarkupContainer(
				"wallItemsContainer");

		wallItemsContainer.setOutputMarkupId(true);
		
		// TODO WallItem
		DataView<Person> wallItemsDataView = new DataView<Person>(
				"wallItems", provider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<Person> item) {

				Person person = (Person) item.getDefaultModelObject();

				// image wrapper, links to profile
				Link<String> wallItem = new Link<String>("wallItemPhotoWrap",
						new Model<String>(person.getUuid())) {

					private static final long serialVersionUID = 1L;

					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}
				};

				// image
				wallItem.add(new ProfileImageRenderer("wallItemPhoto", person
						.getUuid()));
				item.add(wallItem);

				// name and link to profile
				Link<String> wallItemProfileLink = new Link<String>(
						"wallItemProfileLink", new Model<String>(person
								.getUuid())) {

					private static final long serialVersionUID = 1L;

					public void onClick() {
						setResponsePage(new ViewProfile(getModelObject()));
					}

				};
				wallItemProfileLink.add(new Label("wallItemName", person
						.getDisplayName()));
				item.add(wallItemProfileLink);

				// TODO directly using ProfileStatus but this will be handled
				// by WallItemDataProvider if that's the approach we take
				ProfileStatus status = statusLogic.getUserStatus(person
						.getUuid());

				item.add(new Label("wallItemText", status.getMessage()));
				item.add(new Label("wallItemDate", ProfileUtils
						.convertDateToString(status.getDateAdded(),
								"dd MMMMM @ h:mm")));

			}
		};
		
		wallItemsDataView.setOutputMarkupId(true);
		//wallItemsDataView.setItemsPerPage(10);
		
		wallItemsContainer.add(wallItemsDataView);
		add(wallItemsContainer);
	}

}
