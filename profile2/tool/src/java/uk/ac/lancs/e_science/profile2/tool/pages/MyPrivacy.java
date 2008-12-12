package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.ProfileException;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;


public class MyPrivacy extends BasePage {

	private transient Logger log = Logger.getLogger(MyPrivacy.class);
	

	private transient ProfilePrivacy profilePrivacy;
		
	public MyPrivacy() {
		
		if(log.isDebugEnabled()) log.debug("MyPrivacy()");
		
			
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
			profilePrivacy = profile.createDefaultPrivacyRecord(userId);
			//if its still null, throw exception
			if(profilePrivacy == null) {
				throw new ProfileException("Couldn't create default privacy record for " + userId);
			}
			
		}
		
		System.out.println(profilePrivacy.toString());

		
		Label heading = new Label("heading", new ResourceModel("heading.privacy"));
		add(heading);
		
		//create model
		CompoundPropertyModel privacyModel = new CompoundPropertyModel(profilePrivacy);
		
		//setup form		
		Form form = new Form("form", privacyModel);
		form.setOutputMarkupId(true);
		
		
		//default DDC constructor will use this list with the key/value in the right order
		//List TEMP = Arrays.asList(new String[] { "Everyone", "Only friends", "Only Me" });
		//List TEMP = Arrays.asList(new Integer[] { 0,1,2 });

		//SelectOption[] TEMP = new SelectOption[] {new SelectOption("0", "Everyone"), new SelectOption("1", "Only friends"), new SelectOption("2", "Only Me")};
		//ChoiceRenderer choiceRenderer = new ChoiceRenderer("value", "key");

		//profilePrivacy.setProfile(1);
		
		//when using DDC with a compoundPropertyModel we use this constructor: DDC<T>(String,IModel<List<T>>,IChoiceRenderer<T>)
		//and the ID of the DDC field maps to the field in the CompoundPropertyModel
		
		//profile privacy
		WebMarkupContainer profileContainer = new WebMarkupContainer("profileContainer");
		profileContainer.add(new Label("profileLabel", new ResourceModel("privacy.profile")));
		//DropDownChoice profileChoice = new DropDownChoice("profile", privacyModel, Arrays.asList(options), choiceRenderer);
		//DropDownChoice profileChoice = new DropDownChoice("profile", new PropertyModel(privacyModel, "profile"), Arrays.asList(TEMP), choiceRenderer);
		/*
		DropDownChoice profileChoice = new DropDownChoice("profile", new PropertyModel(privacyModel,"profile"),Arrays.asList(TEMP), new ChoiceRenderer("value", "key"))
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
			
			protected void onSelectionChanged(Object newSelection)
			{
				//setResponsePage(new Faces((String) newSelection));
			}
		};
		*/
		final Map<String, String> privacySettings = new HashMap<String, String>();

		
		IModel dropDownModel = new Model() {
			
			public Object getObject() {
				
				privacySettings.put("0", "some choice");
				privacySettings.put("1", "some other choice");
				privacySettings.put("2", "and another choice");
				
				return privacySettings; //get the HashMap
			} 
		};
		
		
		
		
		
		DropDownChoice profileChoice = new DropDownChoice("profile", dropDownModel, new IChoiceRenderer() {
               
			public String getDisplayValue(Object object) {
				return privacySettings.get(object);
			}

			public String getIdValue(Object object, int index) {
				return object.toString();
			}
        });
		
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
		
		
		profileContainer.add(profileChoice);
		form.add(profileContainer);
		
		
		
		
		
		
		
		
		
		
		
		
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
		
		//get the backing model
		ProfilePrivacy profilePrivacy = (ProfilePrivacy) form.getModelObject();
		
		System.out.println(profilePrivacy.getProfile());
	 	

	
        return false;
	}
	
}


class SelectOption {
	private String key;
	private String value;

	public SelectOption(String key, String value) {
		this.setKey(key);
		this.setValue(value);
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}


