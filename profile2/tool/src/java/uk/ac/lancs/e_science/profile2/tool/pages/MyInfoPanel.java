package uk.ac.lancs.e_science.profile2.tool.pages;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;

public class MyInfoPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoPanel.class);
	protected WebMarkupContainer profileNickname;
	protected WebMarkupContainer profileBirthday;
	protected Form form;
	
	public MyInfoPanel(String id, SakaiProxy sakaiProxy, Profile profile, SakaiPerson sakaiPerson) {
		super(id);
		
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
		
		//get details about this person
		String nickname = null;
		String birthday = null;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy"); //need this localised
		
		if(sakaiPerson != null) {
			nickname = sakaiPerson.getNickname();
			birthday = dateFormat.format(sakaiPerson.getDateOfBirth());
		}
		
		nickname="stevo";
		birthday="3rd January";
		
	
		//nickname
		profileNickname = new WebMarkupContainer("profileNickname");
		profileNickname.add(new Label("profileNicknameLabel", new ResourceModel("profile.nickname")));
		profileNickname.add(new Label("profileNicknameContent", nickname));
		add(profileNickname);
		if(nickname == null) {
			profileNickname.setVisible(false);
		}
		
		//birthday
		profileBirthday = new WebMarkupContainer("profileBirthday");
		profileBirthday.add(new Label("profileBirthdayLabel", new ResourceModel("profile.birthday")));
		profileBirthday.add(new Label("profileBirthdayContent", birthday));
		add(profileBirthday);
		if(birthday == null) {
			profileBirthday.setVisible(false);
		}
		
		//labels
		//add(new Label("profileBirthdayLabel", new ResourceModel("profile.birthday")));
		//add(new Label("profilePositionLabel", new ResourceModel("profile.position")));
		//add(new Label("profileDepartmentLabel", new ResourceModel("profile.department")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.department")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.school")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.room")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.phone.work")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.phone.home")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.phone.mobile")));
		

		//content
		//add(new Label("profileBirthdayContent", dateOfBirth));

		
		
		
		
		
		
		
		//build the form
		form = new Form("myInfoForm");
		
		//nickname
		form.add(new Label("profileNicknameLabel", new ResourceModel("profile.nickname")));
		TextField myNicknameField = new TextField("myNicknameField");
		myNicknameField.setRequired(true);
		myNicknameField.add(new AttributeAppender("value", new Model(nickname), " "));
		form.add(myNicknameField);
		
		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	//send the data from the form to the method that will handle it
            	//then method then needs to update the SakaiPerson object and call the save method
            	//then hide this panel and show the other one  which iwll ahve the new info
            	
            }

            protected void onError(AjaxRequestTarget target, Form form){
                // repaint the feedback panel so errors are shown
                //target.addComponent(feedback);
            }
        };
        submitButton.setLabel(new ResourceModel("button.save.changes"));
        form.add(submitButton);
		
        
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
        form.add(cancelButton);

		
		
		//add the form
		add(form);
		
		
		
		
	}
	

	
	
	
	
	
	
}
