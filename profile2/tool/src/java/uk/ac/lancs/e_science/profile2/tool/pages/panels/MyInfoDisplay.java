package uk.ac.lancs.e_science.profile2.tool.pages.panels;



import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyInfoDisplay extends Panel {
	
	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(MyInfoDisplay.class);
	
	private int visibleFieldCount = 0;
	private String birthday = ""; 
	private String birthdayDisplay = "";

	
	public MyInfoDisplay(final String id, final UserProfile userProfile) {
		super(id);
		
		//this panel stuff
		final Component thisPanel = this;
		
		//get Profile API
		Profile profile = ProfileApplication.get().getProfile();
		
		//get userId of this profile
		String userId = userProfile.getUserId();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel
		String nickname = userProfile.getNickname();
		Date dateOfBirth = userProfile.getDateOfBirth();
		if(dateOfBirth != null) {
			
			//full value contains year regardless of privacy settings
			birthday = profile.convertDateToString(dateOfBirth, ProfileUtilityManager.DEFAULT_DATE_FORMAT);
			
			//get privacy on display of birthday year and format accordingly
			if(profile.isBirthYearVisible(userId)) {
				birthdayDisplay = birthday;
			} else {
				birthdayDisplay = profile.convertDateToString(dateOfBirth, ProfileUtilityManager.DEFAULT_DATE_FORMAT_HIDE_YEAR);
			}
			
			//set both values as they are used differently
			userProfile.setBirthdayDisplay(birthdayDisplay);
			userProfile.setBirthday(birthday);

		}
		
		//heading
		add(new Label("heading", new ResourceModel("heading.basic")));
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		nicknameContainer.add(new Label("nickname", nickname));
		add(nicknameContainer);
		if("".equals(nickname) || nickname == null) {
			nicknameContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//birthday
		WebMarkupContainer birthdayContainer = new WebMarkupContainer("birthdayContainer");
		birthdayContainer.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		birthdayContainer.add(new Label("birthday", birthdayDisplay));
		add(birthdayContainer);
		if("".equals(birthdayDisplay) || birthdayDisplay == null) {
			birthdayContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
				
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
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
		editButton.setOutputMarkupId(true);
		add(editButton);
		
		//no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel("text.no.fields"));
		add(noFieldsMessage);
		if(visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
		
	}
	
}
