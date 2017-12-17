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
package org.sakaiproject.profile2.tool.pages.panels;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;

@Slf4j
public class MyContactDisplay extends Panel {

	private static final long serialVersionUID = 1L;
	private int visibleFieldCount = 0;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	public MyContactDisplay(final String id, final UserProfile userProfile) {
		super(id);
				
		//this panel stuff
		final Component thisPanel = this;
			
		//get info from userProfile since we need to validate it and turn things off if not set.
		String email = userProfile.getEmail();
		String homepage = userProfile.getHomepage();
		String workphone = userProfile.getWorkphone();
		String homephone = userProfile.getHomephone();
		String mobilephone = userProfile.getMobilephone();
		String facsimile = userProfile.getFacsimile();


		//heading
		add(new Label("heading", new ResourceModel("heading.contact")));
		
		//email
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("profile.email")));
		emailContainer.add(new Label("email", email));
		add(emailContainer);
		if(StringUtils.isBlank(email)) {
			emailContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//homepage
		WebMarkupContainer homepageContainer = new WebMarkupContainer("homepageContainer");
		homepageContainer.add(new Label("homepageLabel", new ResourceModel("profile.homepage")));
		homepageContainer.add(new ExternalLink("homepage", homepage, homepage));
		add(homepageContainer);
		if(StringUtils.isBlank(homepage)) {
			homepageContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//work phone
		WebMarkupContainer workphoneContainer = new WebMarkupContainer("workphoneContainer");
		workphoneContainer.add(new Label("workphoneLabel", new ResourceModel("profile.phone.work")));
		workphoneContainer.add(new Label("workphone", workphone));
		add(workphoneContainer);
		if(StringUtils.isBlank(workphone)) {
			workphoneContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//home phone
		WebMarkupContainer homephoneContainer = new WebMarkupContainer("homephoneContainer");
		homephoneContainer.add(new Label("homephoneLabel", new ResourceModel("profile.phone.home")));
		homephoneContainer.add(new Label("homephone", homephone));
		add(homephoneContainer);
		if(StringUtils.isBlank(homephone)) {
			homephoneContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//mobile phone
		WebMarkupContainer mobilephoneContainer = new WebMarkupContainer("mobilephoneContainer");
		mobilephoneContainer.add(new Label("mobilephoneLabel", new ResourceModel("profile.phone.mobile")));
		mobilephoneContainer.add(new Label("mobilephone", mobilephone));
		add(mobilephoneContainer);
		if(StringUtils.isBlank(mobilephone)) {
			mobilephoneContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//facsimile
		WebMarkupContainer facsimileContainer = new WebMarkupContainer("facsimileContainer");
		facsimileContainer.add(new Label("facsimileLabel", new ResourceModel("profile.phone.facsimile")));
		facsimileContainer.add(new Label("facsimile", facsimile));
		add(facsimileContainer);
		if(StringUtils.isBlank(facsimile)) {
			facsimileContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton") {
			
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyContactEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}
			}
						
		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.setOutputMarkupId(true);
		
		if(userProfile.isLocked() && !sakaiProxy.isSuperUser()) {
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
}
