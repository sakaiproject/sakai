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
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class MyStaffDisplay extends Panel {
	
	private static final long serialVersionUID = 1L;
	private int visibleFieldCount = 0;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	public MyStaffDisplay(final String id, final UserProfile userProfile) {
		super(id);
		
		//this panel stuff
		final Component thisPanel = this;
		
		//get info from userProfile
		String department = userProfile.getDepartment();
		String position = userProfile.getPosition();
		String school = userProfile.getSchool();
		String room = userProfile.getRoom();
		String staffProfile = userProfile.getStaffProfile();
		String universityProfileUrl = userProfile.getUniversityProfileUrl();
		String academicProfileUrl = userProfile.getAcademicProfileUrl();
		String publications = userProfile.getPublications();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.staff")));
		
		//department
		WebMarkupContainer departmentContainer = new WebMarkupContainer("departmentContainer");
		departmentContainer.add(new Label("departmentLabel", new ResourceModel("profile.department")));
		departmentContainer.add(new Label("department", department));
		add(departmentContainer);
		if(StringUtils.isBlank(department)) {
			departmentContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//position
		WebMarkupContainer positionContainer = new WebMarkupContainer("positionContainer");
		positionContainer.add(new Label("positionLabel", new ResourceModel("profile.position")));
		positionContainer.add(new Label("position", position));
		add(positionContainer);
		if(StringUtils.isBlank(position)) {
			positionContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//school
		WebMarkupContainer schoolContainer = new WebMarkupContainer("schoolContainer");
		schoolContainer.add(new Label("schoolLabel", new ResourceModel("profile.school")));
		schoolContainer.add(new Label("school", school));
		add(schoolContainer);
		if(StringUtils.isBlank(school)) {
			schoolContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//room
		WebMarkupContainer roomContainer = new WebMarkupContainer("roomContainer");
		roomContainer.add(new Label("roomLabel", new ResourceModel("profile.room")));
		roomContainer.add(new Label("room", room));
		add(roomContainer);
		if(StringUtils.isBlank(room)) {
			roomContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}	
		
		//staff profile
		WebMarkupContainer staffProfileContainer = new WebMarkupContainer("staffProfileContainer");
		staffProfileContainer.add(new Label("staffProfileLabel", new ResourceModel("profile.staffprofile")));
		staffProfileContainer.add(new Label("staffProfile", ProfileUtils.processHtml(staffProfile)).setEscapeModelStrings(false));
		add(staffProfileContainer);
		if(StringUtils.isBlank(staffProfile)) {
			staffProfileContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//university profile URL
		WebMarkupContainer universityProfileUrlContainer = new WebMarkupContainer("universityProfileUrlContainer");
		universityProfileUrlContainer.add(new Label("universityProfileUrlLabel", new ResourceModel("profile.universityprofileurl")));
		universityProfileUrlContainer.add(new ExternalLink("universityProfileUrl", universityProfileUrl, universityProfileUrl));
		add(universityProfileUrlContainer);
		if(StringUtils.isBlank(universityProfileUrl)) {
			universityProfileUrlContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//academic/research profile URL
		WebMarkupContainer academicProfileUrlContainer = new WebMarkupContainer("academicProfileUrlContainer");
		academicProfileUrlContainer.add(new Label("academicProfileUrlLabel", new ResourceModel("profile.academicprofileurl")));
		academicProfileUrlContainer.add(new ExternalLink("academicProfileUrl", academicProfileUrl, academicProfileUrl));
		add(academicProfileUrlContainer);
		if(StringUtils.isBlank(academicProfileUrl)) {
			academicProfileUrlContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//publications
		WebMarkupContainer publicationsContainer = new WebMarkupContainer("publicationsContainer");
		publicationsContainer.add(new Label("publicationsLabel", new ResourceModel("profile.publications")));
		publicationsContainer.add(new Label("publications", ProfileUtils.processHtml(publications)).setEscapeModelStrings(false));
		add(publicationsContainer);
		if(StringUtils.isBlank(publications)) {
			publicationsContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyStaffEdit(id, userProfile);
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
