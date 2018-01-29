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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.ComponentVisualErrorBehaviour;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

@Slf4j
public class MyStaffEdit extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
    public MyStaffEdit(final String id, final UserProfile userProfile) {
		super(id);
		
		log.debug("MyStaffEdit()");
		
		//this panel
		final Component thisPanel = this;
				
		//get userId
		final String userId = userProfile.getUserUuid();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.staff.edit")));
		
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
		if(sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
			editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{ userProfile.getDisplayName() } ));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
				
		//position
		WebMarkupContainer positionContainer = new WebMarkupContainer("positionContainer");
		positionContainer.add(new Label("positionLabel", new ResourceModel("profile.position")));
		TextField position = new TextField("position", new PropertyModel(userProfile, "position"));
		position.setMarkupId("positioninput");
		position.setOutputMarkupId(true);
		positionContainer.add(position);
		form.add(positionContainer);
		
		//department
		WebMarkupContainer departmentContainer = new WebMarkupContainer("departmentContainer");
		departmentContainer.add(new Label("departmentLabel", new ResourceModel("profile.department")));
		TextField department = new TextField("department", new PropertyModel(userProfile, "department"));
		department.setMarkupId("departmentinput");
		department.setOutputMarkupId(true);
		departmentContainer.add(department);
		form.add(departmentContainer);
		
		//school
		WebMarkupContainer schoolContainer = new WebMarkupContainer("schoolContainer");
		schoolContainer.add(new Label("schoolLabel", new ResourceModel("profile.school")));
		TextField school = new TextField("school", new PropertyModel(userProfile, "school"));
		school.setMarkupId("schoolinput");
		school.setOutputMarkupId(true);
		schoolContainer.add(school);
		form.add(schoolContainer);
		
		//room
		WebMarkupContainer roomContainer = new WebMarkupContainer("roomContainer");
		roomContainer.add(new Label("roomLabel", new ResourceModel("profile.room")));
		TextField room = new TextField("room", new PropertyModel(userProfile, "room"));
		room.setMarkupId("roominput");
		room.setOutputMarkupId(true);
		roomContainer.add(room);
		form.add(roomContainer);
		
		//staffprofile
		WebMarkupContainer staffProfileContainer = new WebMarkupContainer("staffProfileContainer");
		staffProfileContainer.add(new Label("staffProfileLabel", new ResourceModel("profile.staffprofile")));
		TextArea staffProfile = new TextArea("staffProfile", new PropertyModel(userProfile, "staffProfile"));
		staffProfile.setMarkupId("staffprofileinput");
		staffProfile.setOutputMarkupId(true);
		staffProfileContainer.add(staffProfile);
		form.add(staffProfileContainer);
		
		//university profile URL
		WebMarkupContainer universityProfileUrlContainer = new WebMarkupContainer("universityProfileUrlContainer");
		universityProfileUrlContainer.add(new Label("universityProfileUrlLabel", new ResourceModel("profile.universityprofileurl")));
		TextField universityProfileUrl = new TextField("universityProfileUrl", new PropertyModel(userProfile, "universityProfileUrl")) {
			private static final long serialVersionUID = 1L;

			// add http:// if missing
			@Override
			protected void convertInput() {
				String input = getInput();

				if (StringUtils.isNotBlank(input)
						&& !(input.startsWith("http://") || input
								.startsWith("https://"))) {
					setConvertedInput("http://" + input);
				} else {
					setConvertedInput(StringUtils.isBlank(input) ? null : input);
				}
			}
		};
		universityProfileUrl.setMarkupId("universityprofileurlinput");
		universityProfileUrl.setOutputMarkupId(true);
		universityProfileUrl.add(new UrlValidator());
		universityProfileUrlContainer.add(universityProfileUrl);
		
		final FeedbackLabel universityProfileUrlFeedback = new FeedbackLabel(
				"universityProfileUrlFeedback", universityProfileUrl);
		universityProfileUrlFeedback.setMarkupId("universityProfileUrlFeedback");
		universityProfileUrlFeedback.setOutputMarkupId(true);
		universityProfileUrlContainer.add(universityProfileUrlFeedback);
		universityProfileUrl.add(new ComponentVisualErrorBehaviour("onblur",
				universityProfileUrlFeedback));
		
		form.add(universityProfileUrlContainer);
		
		//academic/research profile URL
		WebMarkupContainer academicProfileUrlContainer = new WebMarkupContainer("academicProfileUrlContainer");
		academicProfileUrlContainer.add(new Label("academicProfileUrlLabel", new ResourceModel("profile.academicprofileurl")));
		TextField academicProfileUrl = new TextField("academicProfileUrl", new PropertyModel(userProfile, "academicProfileUrl")) {
			private static final long serialVersionUID = 1L;

			// add http:// if missing
			@Override
			protected void convertInput() {
				String input = getInput();

				if (StringUtils.isNotBlank(input)
						&& !(input.startsWith("http://") || input
								.startsWith("https://"))) {
					setConvertedInput("http://" + input);
				} else {
					setConvertedInput(StringUtils.isBlank(input) ? null : input);
				}
			}
		};
		academicProfileUrl.setMarkupId("academicprofileurlinput");
		academicProfileUrl.setOutputMarkupId(true);
		academicProfileUrl.add(new UrlValidator());
		academicProfileUrlContainer.add(academicProfileUrl);
		
		final FeedbackLabel academicProfileUrlFeedback = new FeedbackLabel(
				"academicProfileUrlFeedback", academicProfileUrl);
		academicProfileUrlFeedback.setMarkupId("academicProfileUrlFeedback");
		academicProfileUrlFeedback.setOutputMarkupId(true);
		academicProfileUrlContainer.add(academicProfileUrlFeedback);
		academicProfileUrl.add(new ComponentVisualErrorBehaviour("onblur",
				academicProfileUrlFeedback));
		
		form.add(academicProfileUrlContainer);
		
		//publications
		WebMarkupContainer publicationsContainer = new WebMarkupContainer("publicationsContainer");
		publicationsContainer.add(new Label("publicationsLabel", new ResourceModel("profile.publications")));
		TextArea publications = new TextArea("publications", new PropertyModel(userProfile, "publications"));
 		publications.setMarkupId("publicationsinput");
		publications.setOutputMarkupId(true);
		publicationsContainer.add(publications);
		
		form.add(publicationsContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				if(save(form)) {

					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_STAFF_UPDATE, "/profile/"+userId, true);
					
					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userProfile.getUserUuid())) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_STAFF_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					//repaint panel
					Component newPanel = new MyStaffDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.add(newPanel);
						//resize iframe
						target.appendJavaScript("setMainFrameHeight(window.name);");
					}
				
				} else {
					//String js = "alert('Failed to save information. Contact your system administrator.');";
					//target.prependJavascript(js);
					
					formFeedback.setDefaultModel(new ResourceModel("error.profile.save.academic.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("save-failed-error")));	
					target.add(formFeedback);
				}
            }
			
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes){
			    super.updateAjaxAttributes(attributes);
			    AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
			        @Override
			        public CharSequence getBeforeHandler(Component component) {
			        	return "doUpdateCK()";
			        }
			    };
			    attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
			
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
            	Component newPanel = new MyStaffDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
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
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get userId from the UserProfile (because admin could be editing), then get existing SakaiPerson for that userId
		
		String userId = userProfile.getUserUuid();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);

		String tDepartment = ProfileUtils.truncate(userProfile.getDepartment(), 255, false);
		String tPosition = ProfileUtils.truncate(userProfile.getPosition(), 255, false);
		String tSchool = ProfileUtils.truncate(userProfile.getSchool(), 255, false);
		String tRoom = ProfileUtils.truncate(userProfile.getRoom(), 255, false);
		
		//get values and set into SakaiPerson
		sakaiPerson.setOrganizationalUnit(tDepartment);
		sakaiPerson.setTitle(tPosition);
		sakaiPerson.setCampus(tSchool);
		sakaiPerson.setRoomNumber(tRoom);
		sakaiPerson.setStaffProfile(userProfile.getStaffProfile());
		sakaiPerson.setUniversityProfileUrl(userProfile.getUniversityProfileUrl());
		sakaiPerson.setAcademicProfileUrl(userProfile.getAcademicProfileUrl());
		
		//PRFL-467 store as given, and process when it is retrieved.
		sakaiPerson.setPublications(userProfile.getPublications());
		
		//update SakaiPerson
		if(profileLogic.saveUserProfile(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}

}
