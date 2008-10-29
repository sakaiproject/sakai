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

public class MyContactEdit extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoEdit.class);
	
	
	private WebMarkupContainer formFeedback;

	
	public MyContactEdit(String id, final IModel userProfileModel) {
		super(id, userProfileModel);
		
		//this panel stuff
		final Component thisPanel = this;
		final String thisPanelId = "myContact"; //wicket:id not markupId
				
		//get userProfile from userProfileModel
		UserProfile userProfile = (UserProfile) this.getModelObject();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.contact.edit")));
		
		//setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
	    form.setModel(new CompoundPropertyModel(userProfileModel));
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
	   		
		//nickname
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("profile.email")));
		TextField email = new TextField("email", new PropertyModel(userProfile, "email"));
		emailContainer.add(email);
		form.add(emailContainer);
		
		
		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel

				if(save(form)) {
					Component newPanel = new MyContactDisplay(thisPanelId, userProfileModel);
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
            	Component newPanel = new MyContactDisplay(thisPanelId, userProfileModel);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
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
		
		sakaiPerson.setMail(userProfile.getEmail());

		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	 	
	}

	
}
