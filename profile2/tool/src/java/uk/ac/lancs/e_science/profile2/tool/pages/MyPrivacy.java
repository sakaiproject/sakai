package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.ProfileException;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;


public class MyPrivacy extends BasePage {

	private transient Logger log = Logger.getLogger(MyPrivacy.class);
	
	private static HashMap<Integer, String> privacySettings = new HashMap<Integer, String>();
	private transient ProfilePrivacy profilePrivacy;
		
	public MyPrivacy() {
		
		if(log.isDebugEnabled()) log.debug("MyPrivacy()");
		
		privacySettings.put(0, "Everyone");
		privacySettings.put(1, "Only Friends");
		privacySettings.put(2, "Only Me");
		
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		//get current user
		String userId = sakaiProxy.getCurrentUserId();

		//get the privacy object for this user from the database
		profilePrivacy = profile.getPrivacyRecordForUser(userId);
		
		//if null, create one
		if(profilePrivacy == null) {
			//create a default privacy record for this user
			if(!profile.createDefaultPrivacyRecord(userId)) {
				throw new ProfileException("Couldn't create default privacy record for " + userId);
			}
		}
		
		//System.out.println(profilePrivacy.toString());

		
		Label heading = new Label("heading", new ResourceModel("heading.privacy"));
		add(heading);
		
		//create model
		CompoundPropertyModel privacyModel = new CompoundPropertyModel(profilePrivacy);
		
		//setup form		
		Form form = new Form("form", privacyModel);
		form.setOutputMarkupId(true);
		
		List TEMP = Arrays.asList(new String[] { "Only me", "Only friends", "Everyone" });

		
		//profile privacy
		WebMarkupContainer profileContainer = new WebMarkupContainer("profileContainer");
		profileContainer.add(new Label("profileLabel", new ResourceModel("privacy.profile")));
		DropDownChoice profileChoice = new DropDownChoice("profile", TEMP);
		profileContainer.add(profileChoice);
		form.add(profileContainer);
		
		//basicInfo privacy
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("basicInfoContainer");
		basicInfoContainer.add(new Label("basicInfoLabel", new ResourceModel("privacy.basicinfo")));
		DropDownChoice basicInfoChoice = new DropDownChoice("basicInfo", TEMP);
		basicInfoContainer.add(basicInfoChoice);
		form.add(basicInfoContainer);
		
		
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.settings"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				if(save(form)){
					System.out.println("privacy saved");
				}
				
				
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
				setResponsePage(new MyProfile());
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
        
        add(form);
		
	}
	
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
        System.out.println(form.getModelObject());
/*
		//get the backing model
		ProfilePrivacy userProfile = (UserProfile) form.getModelObject();
		
		//get sakaiProxy, then get userId from sakaiProxy, then get sakaiperson for that userId
		SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		String userId = sakaiProxy.getCurrentUserId();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
	
		//set the attributes from userProfile that this form dealt with, into sakaiPerson
		//this WILL fail if there is no sakaiPerson for the user however this should have been caught already
		//as a new Sakaiperson for a user is created in MyProfile if they don't have one.
		
		sakaiPerson.setNickname(userProfile.getNickname());
		//sakaiPerson.setDateOfBirth(userProfile.getDateOfBirth());
		//ned handle on Profile and to process the date here.
		

		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	 	
	}	
*/	
        return false;
	}
	
	
}
