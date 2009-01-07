package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.ProfileException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.tool.components.HashMapChoiceRenderer;


public class MyPrivacy extends BasePage {

	private transient Logger log = Logger.getLogger(MyPrivacy.class);

	private transient ProfilePrivacy profilePrivacy;
		
	public MyPrivacy() {
		
		if(log.isDebugEnabled()) log.debug("MyPrivacy()");
		

		//get current user
		String userId = sakaiProxy.getCurrentUserId();

		//get the privacy object for this user from the database
		profilePrivacy = profile.getPrivacyRecordForUser(userId);
		
		//if null, create one
		if(profilePrivacy == null) {
			profilePrivacy = profile.createDefaultPrivacyRecord(userId);
			//if its still null, throw exception
			if(profilePrivacy == null) {
				throw new ProfileException("Couldn't create default privacy record for " + userId);
			}
			
		}
		
		Label heading = new Label("heading", new ResourceModel("heading.privacy"));
		add(heading);
		
		//feedback for form submit action
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		add(formFeedback);
		
		
		
		//create model
		CompoundPropertyModel privacyModel = new CompoundPropertyModel(profilePrivacy);
		
		//setup form		
		Form form = new Form("form", privacyModel);
		form.setOutputMarkupId(true);
		
		
		
		//setup LinkedHashMap of privacy options
		final LinkedHashMap<String, String> privacySettings = new LinkedHashMap<String, String>();
		privacySettings.put("0", new StringResourceModel("privacy.option.everyone", this,null).getString());
		privacySettings.put("1", new StringResourceModel("privacy.option.onlyfriends", this,null).getString());
		privacySettings.put("2", new StringResourceModel("privacy.option.onlyme", this,null).getString());
		
		//model that wraps our options
		IModel dropDownModel = new Model() {
			public Object getObject() {
				 return new ArrayList(privacySettings.keySet()); //via proxy
			} 
		};
		
		
		//when using DDC with a compoundPropertyModel we use this constructor: DDC<T>(String,IModel<List<T>>,IChoiceRenderer<T>)
		//and the ID of the DDC field maps to the field in the CompoundPropertyModel
		
		//profile privacy
		WebMarkupContainer profileContainer = new WebMarkupContainer("profileContainer");
		profileContainer.add(new Label("profileLabel", new ResourceModel("privacy.profile")));
		DropDownChoice profileChoice = new DropDownChoice("profile", dropDownModel, new HashMapChoiceRenderer(privacySettings));             
		profileContainer.add(profileChoice);
		form.add(profileContainer);
		
		//basicInfo privacy
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("basicInfoContainer");
		basicInfoContainer.add(new Label("basicInfoLabel", new ResourceModel("privacy.basicinfo")));
		DropDownChoice basicInfoChoice = new DropDownChoice("basicInfo", dropDownModel, new HashMapChoiceRenderer(privacySettings));
		basicInfoContainer.add(basicInfoChoice);
		form.add(basicInfoContainer);
		
		//basicInfo privacy
		WebMarkupContainer contactInfoContainer = new WebMarkupContainer("contactInfoContainer");
		contactInfoContainer.add(new Label("contactInfoLabel", new ResourceModel("privacy.contactinfo")));
		DropDownChoice contactInfoChoice = new DropDownChoice("contactInfo", dropDownModel, new HashMapChoiceRenderer(privacySettings));
		contactInfoContainer.add(contactInfoChoice);
		form.add(contactInfoContainer);
		
		//personalInfo privacy
		WebMarkupContainer personalInfoContainer = new WebMarkupContainer("personalInfoContainer");
		personalInfoContainer.add(new Label("personalInfoLabel", new ResourceModel("privacy.personalinfo")));
		DropDownChoice personalInfoChoice = new DropDownChoice("personalInfo", dropDownModel, new HashMapChoiceRenderer(privacySettings));
		personalInfoContainer.add(personalInfoChoice);
		form.add(personalInfoContainer);
		
		//birthYear privacy
		WebMarkupContainer birthYearContainer = new WebMarkupContainer("birthYearContainer");
		birthYearContainer.add(new Label("birthYearLabel", new ResourceModel("privacy.birthyear")));
		CheckBox birthYearCheckbox = new CheckBox("birthYear", new PropertyModel(privacyModel, "showBirthYear"));
		birthYearContainer.add(birthYearCheckbox);
		form.add(birthYearContainer);
		
		//search privacy
		WebMarkupContainer searchContainer = new WebMarkupContainer("searchContainer");
		searchContainer.add(new Label("searchLabel", new ResourceModel("privacy.search")));
		DropDownChoice searchChoice = new DropDownChoice("search", dropDownModel, new HashMapChoiceRenderer(privacySettings));
		searchContainer.add(searchChoice);
		form.add(searchContainer);
		
		/* 
		phoneVendorDDC.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                // Reset the phone model dropdown when the vendor changes
                _myModel.setPhoneModel(null);
                _phoneModelDDC.setChoices(getTerminalsByVendor(_myModel.getPhoneVendor()));
                target.addComponent(_phoneModelDDC);
            }
        });
		*/
		
		
		basicInfoContainer.add(basicInfoChoice);
		form.add(basicInfoContainer);
		
		
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.settings"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show feedback. perhaps redirect back to main page after a short while?
				if(save(form)){
					formFeedback.setModel(new ResourceModel("success.save.ok"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
					target.addComponent(formFeedback);
				} else {
					formFeedback.setModel(new ResourceModel("error.save.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));	
				}
				target.addComponent(formFeedback);
				
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
		
		//get the backing model - its elems have been updated with the form params
		ProfilePrivacy profilePrivacy = (ProfilePrivacy) form.getModelObject();

		if(profile.savePrivacyRecordForUser(profilePrivacy)) {
			log.info("Saved ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return true;
		} else {
			log.info("Couldn't save ProfilePrivacy for: " + profilePrivacy.getUserUuid());
			return false;
		}
	
	}
	
}



