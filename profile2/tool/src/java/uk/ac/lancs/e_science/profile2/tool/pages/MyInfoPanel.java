package uk.ac.lancs.e_science.profile2.tool.pages;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyInfoPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoPanel.class);
	
	private String nickname = null;
	private Date dateOfBirth = null;
	private String birthday = null;

	
	//setup the form object
	private class InputForm extends Form {
		
		public InputForm(String id, IModel userProfileModel) {
			
			super(id, userProfileModel);
			
		    
		    //get userProfile from userProfileModel
			UserProfile userProfile = (UserProfile) getModelObject();
			
			//back the form with the model, if we name the form fields the same as in the object, they will automatically populate
		    setModel(new CompoundPropertyModel(userProfileModel));
			
			//nickname
			add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
			TextField nicknameField = new TextField("nickname");
			nicknameField.setRequired(true);
			add(nicknameField);
			
			//submit button - ajax so it doesnt refresh. normal button will refresh
			AjaxButton submitButton = new AjaxButton("submit") {
				protected void onSubmit(AjaxRequestTarget target, Form form) {
					//do nothing, the onSubmit() of the form does the processing	
					
	            }
			};
			submitButton.setLabel(new ResourceModel("button.save.changes"));
	        add(submitButton);
			
	        
			//cancel button
			AjaxButton cancelButton = new AjaxButton("cancel") {
	            protected void onSubmit(AjaxRequestTarget target, Form form) {
	            	// setup JS that will be executed
	            	String js = 
	            		"$(document).ready(function(){ " +
	            		"$('#myInfoEdit').slideUp('normal', function() {" +
	            		"$('#myInfo').slideDown();" +
	            		"});" +
	            		"});";
	            	target.prependJavascript(js);
	            }
	        };
	        cancelButton.setLabel(new ResourceModel("button.cancel"));
	        cancelButton.setDefaultFormProcessing(false);
	        add(cancelButton);
	        
		}
		
		//using the submit handler of the form we get the data
		
		public void onSubmit() {
			
			//THIS COULD BE ABSTRACTED TO THE PROFILE.CLASS
			
			
			//get the backing model
			UserProfile userProfile = (UserProfile) getModelObject();
			
			//and access the attributes via the getters - the form has set them to the object
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
			} else {
				System.out.println("nah");
			}
		 	
		}
		
		
		
	
		
		
	}
	
	
	//setup the profile display object
	private class InfoContainer extends WebMarkupContainer {
	
		//constructor
		public InfoContainer(String id, IModel userProfileModel) {
			
			super(id, userProfileModel);
			
			//get userProfile from userProfileModel
			UserProfile userProfile = (UserProfile)this.getModelObject();
			
			//get info from userProfile
			String nickname = userProfile.getNickname();
			String birthday = null;

	
			//edit button
			AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
	            public void onClick(AjaxRequestTarget target) {
	                // setup JS that will be executed
	            	String js = 
	            		"$(document).ready(function(){ " +
	            		"$('#myInfo').slideUp('normal', function() {" +
	            		"$('#myInfoEdit').slideDown();" +
	            		"});" +
	            		"});";
	            	target.prependJavascript(js);
	            }
	        };
	        add(editButton);
	        
			
			//nickname
			WebMarkupContainer profileNickname = new WebMarkupContainer("profileNickname");
			profileNickname.add(new Label("profileNicknameLabel", new ResourceModel("profile.nickname")));
			profileNickname.add(new Label("profileNicknameContent", nickname));
			add(profileNickname);
			if(nickname == null) {
				profileNickname.setVisible(false);
			}
			
			//birthday
			WebMarkupContainer profileBirthday = new WebMarkupContainer("profileBirthday");
			profileBirthday.add(new Label("profileBirthdayLabel", new ResourceModel("profile.birthday")));
			profileBirthday.add(new Label("profileBirthdayContent", birthday));
			add(profileBirthday);
			if(birthday == null) {
				profileBirthday.setVisible(false);
			}
		}
		
	}
	
	
	
	
	
	//panel constructor
	public MyInfoPanel(String id, IModel userProfileModel) {
		super(id, userProfileModel);
		
		//userProfileModel comes from MyProfile and is passed to the constructors for each child panel

		//add the panel
		add(new InfoContainer("myInfo", userProfileModel));
		
		//add the form
		add(new InputForm("myInfoForm", userProfileModel));

		
		
		
	}
	

	
	
	
	
	
	
}
