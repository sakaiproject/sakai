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
import uk.ac.lancs.e_science.profile2.tool.pages.models.InfoModel;

public class MyInfoPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoPanel.class);
	
	private SakaiPerson sakaiPerson = null;
	private String nickname = null;
	private Date dateOfBirth = null;
	private String birthday = null;

	
	//setup the form object
	private class InputForm extends Form {
		
		public InputForm(String id) {
			
			super(id);
			
			//assign this form the model which wil lhold the user entered fields
			InfoModel infoModel = new InfoModel();
		    setModel(new CompoundPropertyModel(infoModel));
			
			//nickname
			add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
			TextField nicknameField = new TextField("nickname");
			nicknameField.setRequired(true);
			nicknameField.add(new AttributeAppender("value", new Model(nickname), " "));
			add(nicknameField);
			
			//submit button
			AjaxButton submitButton = new AjaxButton("submit") {
	            protected void onSubmit(AjaxRequestTarget target, Form form) {
	            	//process the form submit
	            	InfoModel infoModel = (InfoModel) getModelObject();
	            	
	            	//we can now access the params via infoModel.getNickname() etc
	            	System.out.println(infoModel.getNickname().toString());
	            	
	            	
	            	
	            }
	
	            protected void onError(AjaxRequestTarget target, Form form){
	                // repaint the feedback panel so errors are shown
	                //target.addComponent(feedback);
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
	}
	
	
	//setup the profile display object
	private class InfoContainer extends WebMarkupContainer {
	
		//constructor
		public InfoContainer(String name) {
			
			super(name);
	
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
	
	
	
	
	
	//constructor for the panel to bring the elements together
	public MyInfoPanel(String id, SakaiProxy sakaiProxy, Profile profile, SakaiPerson sakaiPerson) {
		super(id);
		
		//setup data about this person from the objects passed into the constructor
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy"); //need this localised
		if(sakaiPerson != null) {
			nickname = sakaiPerson.getNickname();
			dateOfBirth = sakaiPerson.getDateOfBirth();
			if(dateOfBirth != null) {
				birthday = dateFormat.format(dateOfBirth);
			}
		}
		
		nickname="stevo";
		birthday = "3rd January";
	

		
		
		//add the panel
		add(new InfoContainer("myInfo"));
		
		//add the form
		add(new InputForm("myInfoForm"));

		
		
		
	}
	

	
	
	
	
	
	
}
