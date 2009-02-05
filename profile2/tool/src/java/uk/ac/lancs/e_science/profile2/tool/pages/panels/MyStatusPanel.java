package uk.ac.lancs.e_science.profile2.tool.pages.panels;


import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyStatusPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(MyStatusPanel.class);
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;
    private transient ProfileStatus profileStatus;
    
    //get default text that fills the textField
	String defaultStatus = new ResourceModel("text.no.status", "Say something").getObject().toString();


	public MyStatusPanel(String id, UserProfile userProfile) {
		super(id);
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
				
		//get info
		String displayName = userProfile.getDisplayName();
		final String userId = sakaiProxy.getCurrentUserId();
		
		//status container - here so we can work on it if the status is not set
		final WebMarkupContainer statusContainer = new WebMarkupContainer("statusContainer");
		statusContainer.setOutputMarkupPlaceholderTag(true); //we need a placeholder for if we have no status
		
		//setup ProfileStatus object loaded with the current values from the DB for the page to use initially.
		profileStatus = profile.getUserStatus(userId);
		
		//if no status, initialise
		if(profileStatus == null) {
			statusContainer.setVisible(false); //hide status section
			profileStatus = new ProfileStatus();
		}
		
		
		//the message and date fields get their values from the ProfileStatus object
		//when the status form is submitted, Hibernate persists the data in the background
		//and the message and date fields get their values again, via these models from the ProfileStatus object.
		
		//setup model for status message
		LoadableDetachableModel statusMessageModel = new LoadableDetachableModel() {
			private static final long serialVersionUID = 1L;
			
			private String message = "";
			
			protected Object load() {
				profileStatus = profile.getUserStatus(userId);
				message = profileStatus.getMessage(); //get from hibernate
				if("".equals(message) || message == null){
					log.warn("No status message for: " + userId);
				} 
				return message;
			}
			
		};
		
		//setup model for status date
		LoadableDetachableModel statusDateModel = new LoadableDetachableModel() {
			private static final long serialVersionUID = 1L;
			
			private Date date;
			private String dateStr = "";
			
			protected Object load() {
				date = profile.getUserStatusDate(userId);
				if(date == null) {
					log.warn("No status date for: " + userId);
				} else {
					//transform the date
					dateStr = profile.convertDateForStatus(date);
				}
				return dateStr;
			}
			
		};
	
				
		//create model
		CompoundPropertyModel profileStatusModel = new CompoundPropertyModel(profileStatus);
	
		//name
		Label profileName = new Label("profileName", displayName);
		add(profileName);
		
		
		//status
		Label statusMessageLabel = new Label("statusMessage", statusMessageModel);
		statusMessageLabel.setOutputMarkupId(true);
		statusContainer.add(statusMessageLabel);
		
		//status last updated
		Label statusDateLabel = new Label("statusDate", statusDateModel);
		statusDateLabel.setOutputMarkupId(true);
		statusContainer.add(statusDateLabel);
		
		//status update link
		AjaxFallbackLink statusClearLink = new AjaxFallbackLink("statusClearLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				//clear status, hide and repaint
				if(profile.clearUserStatus(userId)) {
					statusContainer.setVisible(false);
					target.addComponent(statusContainer);
				}
				
			}
						
		};
		statusClearLink.setOutputMarkupId(true);
		statusClearLink.add(new Label("statusClearLabel",new ResourceModel("link.status.clear")));
		statusContainer.add(statusClearLink);
		
		//add status container
		add(statusContainer);
				
		//status form
		Form form = new Form("form", profileStatusModel);
		form.setOutputMarkupId(true);
        		
		//status field
        TextField statusField = new TextField("message", new PropertyModel(profileStatus, "message"));
        statusField.setOutputMarkupId(true);
        form.add(statusField);
        
        //link the status textfield field with the focus/blur function via this dynamic js 
		StringHeaderContributor statusJavascript = new StringHeaderContributor(
				"<script type=\"text/javascript\">" +
					"$(document).ready( function(){" +
					"autoFill($('#" + statusField.getMarkupId() + "'), '" + defaultStatus + "');" +
					"});" +
				"</script>");
		add(statusJavascript);
        
        
        //submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//get the backing model
				ProfileStatus profileStatus = (ProfileStatus) form.getModelObject();
				
				//get userId from sakaiProxy
				String userId = sakaiProxy.getCurrentUserId();
				
				//get the status. if its the default text, do not update, although we should clear the model
				String statusMessage = profileStatus.getMessage().trim();
				if(statusMessage.equals(defaultStatus)) {
					profileStatus.setMessage("");
					log.warn("Status update for userId: " + userId + " was not updated because they didn't enter anything.");
					return;
				}

				//save status from userProfile
				if(profile.setUserStatus(userId, statusMessage)) {
					log.info("Saved status for: " + userId);
					
					updateTwitter(userId, statusMessage);
					
					// make status panel container visible and repaint
					statusContainer.setVisible(true);
					target.addComponent(statusContainer);
				} else {
					log.error("Couldn't save status for: " + userId);
					String js = "alert('Failed to save status. If the problem persists, contact your system administrator.');";
					target.prependJavascript(js);	
				}
				
            }
		};
		form.add(submitButton);
		
        //add form
		add(form);
		
	}
	

	public void updateTwitter(String userId, String message) {
		
		boolean isTwitterEnabled = profile.isTwitterIntegrationEnabled(userId);
		
		if(isTwitterEnabled) {
			profile.sendMessageToTwitter(userId, message);
		}
		
	}
	
}
