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

import org.apache.log4j.Logger;
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
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileConstants;

public class MyAcademicEdit extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyInterestsEdit.class);
    private transient SakaiProxy sakaiProxy;

    public MyAcademicEdit(final String id, final UserProfile userProfile) {
		super(id);
		
		log.debug("MyInterestsEdit()");
		
		//get API's
		sakaiProxy = getSakaiProxy();
		
		//this panel
		final Component thisPanel = this;
				
		//get userId
		final String userId = userProfile.getUserUuid();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.academic.edit")));
		
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
		positionContainer.add(position);
		form.add(positionContainer);
		
		//department
		WebMarkupContainer departmentContainer = new WebMarkupContainer("departmentContainer");
		departmentContainer.add(new Label("departmentLabel", new ResourceModel("profile.department")));
		TextField department = new TextField("department", new PropertyModel(userProfile, "department"));
		departmentContainer.add(department);
		form.add(departmentContainer);
		
		//school
		WebMarkupContainer schoolContainer = new WebMarkupContainer("schoolContainer");
		schoolContainer.add(new Label("schoolLabel", new ResourceModel("profile.school")));
		TextField school = new TextField("school", new PropertyModel(userProfile, "school"));
		schoolContainer.add(school);
		form.add(schoolContainer);
		
		//room
		WebMarkupContainer roomContainer = new WebMarkupContainer("roomContainer");
		roomContainer.add(new Label("roomLabel", new ResourceModel("profile.room")));
		TextField room = new TextField("room", new PropertyModel(userProfile, "room"));
		roomContainer.add(room);
		form.add(roomContainer);
				
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				if(save(form)) {

					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_INTERESTS_UPDATE, "/profile/"+userId, true);
					
					//repaint panel
					Component newPanel = new MyAcademicDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.addComponent(newPanel);
						//resize iframe
						target.appendJavascript("setMainFrameHeight(window.name);");
					}
				
				} else {
					//String js = "alert('Failed to save information. Contact your system administrator.');";
					//target.prependJavascript(js);
					
					formFeedback.setDefaultModel(new ResourceModel("error.profile.save.academic.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("save-failed-error")));	
					target.addComponent(formFeedback);
				}
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
            	Component newPanel = new MyAcademicDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
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
		
		//get SakaiProxy, get userId from the UserProfile (because admin could be editing), then get existing SakaiPerson for that userId
		SakaiProxy sakaiProxy = getSakaiProxy();
		
		String userId = userProfile.getUserUuid();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);

		//get values and set into SakaiPerson
		sakaiPerson.setOrganizationalUnit(userProfile.getDepartment());
		sakaiPerson.setTitle(userProfile.getPosition());
		sakaiPerson.setCampus(userProfile.getSchool());
		sakaiPerson.setRoomNumber(userProfile.getRoom());

		//update SakaiPerson
		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyAcademicEdit has been deserialized.");
		//re-init our transient objects
		sakaiProxy = getSakaiProxy();
	}
	
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

	
}
