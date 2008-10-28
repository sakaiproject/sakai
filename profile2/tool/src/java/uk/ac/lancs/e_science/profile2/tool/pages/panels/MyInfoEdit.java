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

	
	public MyInfoEdit(String id, final IModel userProfileModel) {
		super(id, userProfileModel);
		
		//get handle on this panel
		final Component thisPanel = this;
				
		//get userProfile from userProfileModel
		UserProfile userProfile = (UserProfile) this.getModelObject();
		
		//setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
	    form.setModel(new CompoundPropertyModel(userProfileModel));
		
		//We don't need to get the info from userProfile, we load it into the form with a property model

		//nickname
		WebMarkupContainer nickname = new WebMarkupContainer("nickname");
		nickname.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		TextField nicknameField = new TextField("nicknameField", new PropertyModel(userProfile, "nickname"));
		nicknameField.setRequired(true);
		nickname.add(nicknameField);
		form.add(nickname);
		
		//nickname
		WebMarkupContainer birthday = new WebMarkupContainer("birthday");
		birthday.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		TextField birthdayField = new TextField("birthdayField", new PropertyModel(userProfile, "dateOfBirth"));
		birthdayField.setRequired(true);
		birthday.add(birthdayField);
		form.add(birthday);
		
		
		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//call save() method, show message, then load display panel
				
				String js = "alert('here');";
            	target.prependJavascript(js);
				
				
            }
		};
		submitButton.setDefaultFormProcessing(false); //or use the onsubmit of the form???
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	System.out.println("cancel clicked");
            	Component newPanel = new MyInfoDisplay("myInfo", userProfileModel);
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
	protected boolean save() {
		
/*		
		//get the backing model
		UserProfile userProfile = (UserProfile) getModelObject();
		
		//and access the attributes via the getters - the form sets them to the object
		String nickname = userProfile.getNickname();
		
		//get sakaiProxy, then get userId from sakaiProxy, then get sakaiperson for that userId
		SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		String userId = sakaiProxy.getCurrentUserId();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		
		//set the attributes from userProfile that this form dealt with, into sakaiPerson
		//this WILL fail if there is no sakaiPerson for the user so it needs to be dealt with
		sakaiPerson.setNickname(nickname);
		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			System.out.println("ok!");
			return true;
		} else {
			System.out.println("nah");
			return false;
		}
	 	
	 	*/
		return false;
	}

	
}
