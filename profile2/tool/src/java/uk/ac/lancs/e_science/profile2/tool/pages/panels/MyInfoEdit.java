package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyInfoEdit extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoEdit.class);
	
	
	private WebMarkupContainer formFeedback;

	
	public MyInfoEdit(final String id, final UserProfile userProfile) {
		super(id);
		
		//this panel stuff
		final Component thisPanel = this;
				
		//create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		//heading
		add(new Label("heading", new ResourceModel("heading.basic.edit")));
		
		//setup form		
		Form form = new Form("form", userProfileModel);
		form.setOutputMarkupId(true);
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
	   		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		TextField nickname = new TextField("nickname", new PropertyModel(userProfile, "nickname"));
		nicknameContainer.add(nickname);
		form.add(nicknameContainer);
		
		//birthday
		WebMarkupContainer birthdayContainer = new WebMarkupContainer("birthdayContainer");
		birthdayContainer.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		TextField birthday = new TextField("birthday", new PropertyModel(userProfile, "birthday"));
		birthdayContainer.add(birthday);
		form.add(birthdayContainer);
		
		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel

				if(save(form)) {
					Component newPanel = new MyInfoDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.addComponent(newPanel);
					}
				
				} else {
					String js = "alert('crap!');";
					target.prependJavascript(js);
				}
				
				
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	//System.out.println("cancel clicked");
            	Component newPanel = new MyInfoDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
				}
            	
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
        //feedback stuff - make this a class and insance it with diff params
        //WebMarkupContainer formFeedback = new WebMarkupContainer("formFeedback");
		//formFeedback.add(new Label("feedbackMsg", "some message"));
		//formFeedback.add(new AjaxIndicator("feedbackImg"));
		//form.add(formFeedback);
        
        
		
		//add form to page
		add(form);
		
	}
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
        //System.out.println(getModelObject());

		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get sakaiProxy, then get userId from sakaiProxy, then get sakaiperson for that userId
		SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		String userId = sakaiProxy.getCurrentUserId();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
	
		//set the attributes from userProfile that this form dealt with, into sakaiPerson
		//this WILL fail if there is no sakaiPerson for the user however this should have been caught already
		//as a new Sakaiperson for a user is created in MyProfile if they don't have one.
		
		sakaiPerson.setNickname(userProfile.getNickname());
		log.info("UserProfile nickname is: " + userProfile.getNickname());
		log.info("SakaiPerson nickanem is: " + sakaiPerson.getNickname());

		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	 	
	}

	
}
