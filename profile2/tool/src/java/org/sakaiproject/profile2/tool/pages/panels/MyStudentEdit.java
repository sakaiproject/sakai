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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyStudentEdit extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	public MyStudentEdit(final String id, final UserProfile userProfile) {
		
		super(id);

		//heading
		add(new Label("heading", new ResourceModel("heading.student.edit")));
		
		//setup form
		Form form = new Form("form", new Model(userProfile));
		form.setOutputMarkupId(true);
		
		//form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
		//add warning message if superUser and not editing own profile
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if(sakaiProxy.isSuperUserAndProxiedToUser(userProfile.getUserUuid())) {
			editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{ userProfile.getDisplayName() } ));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
		//course
		WebMarkupContainer courseContainer = new WebMarkupContainer("courseContainer");
		courseContainer.add(new Label("courseLabel", new ResourceModel("profile.course")));
		TextField course = new TextField("course", new PropertyModel(userProfile, "course"));
		course.setMarkupId("courseinput");
		course.setOutputMarkupId(true);
		courseContainer.add(course);
		form.add(courseContainer);
		
		//subjects
		WebMarkupContainer subjectsContainer = new WebMarkupContainer("subjectsContainer");
		subjectsContainer.add(new Label("subjectsLabel", new ResourceModel("profile.subjects")));
		TextField subjects = new TextField("subjects", new PropertyModel(userProfile, "subjects"));
		subjects.setMarkupId("subjectsinput");
		subjects.setOutputMarkupId(true);
		subjectsContainer.add(subjects);
		form.add(subjectsContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				// save() form, show message, then load display panel
				if (save(form)) {

					// post update event
					sakaiProxy.postEvent(
							ProfileConstants.EVENT_PROFILE_STUDENT_UPDATE,
							"/profile/" + userProfile.getUserUuid(), true);

					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userProfile.getUserUuid())) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_STUDENT_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					// repaint panel
					Component newPanel = new MyStudentDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					MyStudentEdit.this.replaceWith(newPanel);
					if (target != null) {
						target.add(newPanel);
						// resize iframe
						target
								.appendJavaScript("setMainFrameHeight(window.name);");
					}

				} else {
					// String js =
					// "alert('Failed to save information. Contact your system administrator.');";
					// target.prependJavascript(js);

					formFeedback.setDefaultModel(new ResourceModel(
							"error.profile.save.academic.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("save-failed-error")));
					target.add(formFeedback);
				}
			}
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
            	Component newPanel = new MyStudentDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MyStudentEdit.this.replaceWith(newPanel);
				if(target != null) {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
					//need a scrollTo action here, to scroll down the page to the section
				}
            	
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
       		
		//add form to page
		add(form);
	}
	
	private boolean save(Form form) {

		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userProfile.getUserUuid());
		sakaiPerson.setEducationCourse(userProfile.getCourse());
		sakaiPerson.setEducationSubjects(userProfile.getSubjects());
		
		//update SakaiPerson
		if(profileLogic.saveUserProfile(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userProfile.getUserUuid() );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userProfile.getUserUuid());
			return false;
		}
	
	}
}
