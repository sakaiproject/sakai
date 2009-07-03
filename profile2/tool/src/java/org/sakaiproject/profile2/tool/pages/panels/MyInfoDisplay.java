package org.sakaiproject.profile2.tool.pages.panels;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.models.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

public class MyInfoDisplay extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyInfoDisplay.class);
	private transient ProfileLogic profileLogic;
	private int visibleFieldCount = 0;
	private String birthday = ""; 
	private String birthdayDisplay = "";
	
	public MyInfoDisplay(final String id, final UserProfile userProfile) {
		super(id);
		
		log.debug("MyInfoDisplay()");
		
		//this panel stuff
		final Component thisPanel = this;
		
		//get API's
		profileLogic = getProfileLogic();
		
		//get userId of this profile
		String userId = userProfile.getUserId();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel
		/*
		String firstName = userProfile.getFirstName();
		String middleName = userProfile.getMiddleName();
		String lastName = userProfile.getLastName();
		*/
		String nickname = userProfile.getNickname();
		Date dateOfBirth = userProfile.getDateOfBirth();
		if(dateOfBirth != null) {
			
			//full value contains year regardless of privacy settings
			birthday = ProfileUtils.convertDateToString(dateOfBirth, ProfileConstants.DEFAULT_DATE_FORMAT);
			
			//get privacy on display of birthday year and format accordingly
			if(profileLogic.isBirthYearVisible(userId)) {
				birthdayDisplay = birthday;
			} else {
				birthdayDisplay = ProfileUtils.convertDateToString(dateOfBirth, ProfileConstants.DEFAULT_DATE_FORMAT_HIDE_YEAR);
			}
			
			//set both values as they are used differently
			userProfile.setBirthdayDisplay(birthdayDisplay);
			userProfile.setBirthday(birthday);

		}
		
		//heading
		add(new Label("heading", new ResourceModel("heading.basic")));
		
		//firstName
		/*
		WebMarkupContainer firstNameContainer = new WebMarkupContainer("firstNameContainer");
		firstNameContainer.add(new Label("firstNameLabel", new ResourceModel("profile.name.first")));
		firstNameContainer.add(new Label("firstName", firstName));
		add(firstNameContainer);
		if(StringUtils.isBlank(firstName)) {
			firstNameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		*/
		
		//middleName
		/*
		WebMarkupContainer middleNameContainer = new WebMarkupContainer("middleNameContainer");
		middleNameContainer.add(new Label("middleNameLabel", new ResourceModel("profile.name.middle")));
		middleNameContainer.add(new Label("middleName", middleName));
		add(middleNameContainer);
		if(StringUtils.isBlank(middleName)) {
			middleNameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		*/
		
		//lastName
		/*
		WebMarkupContainer lastNameContainer = new WebMarkupContainer("lastNameContainer");
		lastNameContainer.add(new Label("lastNameLabel", new ResourceModel("profile.name.last")));
		lastNameContainer.add(new Label("lastName", lastName));
		add(lastNameContainer);
		if(StringUtils.isBlank(lastName)) {
			lastNameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		*/
		
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		nicknameContainer.add(new Label("nickname", nickname));
		add(nicknameContainer);
		if(StringUtils.isBlank(nickname)) {
			nicknameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//birthday
		WebMarkupContainer birthdayContainer = new WebMarkupContainer("birthdayContainer");
		birthdayContainer.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		birthdayContainer.add(new Label("birthday", birthdayDisplay));
		add(birthdayContainer);
		if(StringUtils.isBlank(birthdayDisplay)) {
			birthdayContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
				
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyInfoEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
			}
						
		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.setOutputMarkupId(true);
		
		if(userProfile.isLocked()) {
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
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyInfoDisplay has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
	}
	
	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}
	
}
