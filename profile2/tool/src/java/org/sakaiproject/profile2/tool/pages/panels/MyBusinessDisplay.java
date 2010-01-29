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

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.Locator;

public class MyBusinessDisplay extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyInfoDisplay.class);

	public MyBusinessDisplay(final String id, final UserProfile userProfile) {
		super(id);

		log.debug("MyBusinessDisplay()");
		
		int visibleFieldCount = 0;
				
		// heading
		add(new Label("heading", new ResourceModel("heading.business")));

		// business biography
		WebMarkupContainer businessBiographyContainer = new WebMarkupContainer("businessBiographyContainer");
		businessBiographyContainer.add(new Label("businessBiographyLabel", new ResourceModel("profile.business.bio")));
		businessBiographyContainer.add(new Label("businessBiography", userProfile.getBusinessBiography()));
		add(businessBiographyContainer);
		
		if (StringUtils.isBlank(userProfile.getBusinessBiography())) {
			businessBiographyContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}

		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton",
				new ResourceModel("button.edit")) {

			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyBusinessEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MyBusinessDisplay.this.replaceWith(newPanel);
				if (target != null) {
					target.addComponent(newPanel);
					// resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}

			}

		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.setOutputMarkupId(true);
		
		if(userProfile.isLocked() && !Locator.getSakaiProxy().isSuperUser()) {
			editButton.setVisible(false);
		}
		
		add(editButton);
		
		//no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel("text.no.fields"));
		add(noFieldsMessage);
		if(visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
	}

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyBusinessDisplay has been deserialized.");
	}
}
