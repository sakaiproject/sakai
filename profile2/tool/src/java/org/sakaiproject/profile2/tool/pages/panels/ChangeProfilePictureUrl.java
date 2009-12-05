package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.UrlValidator;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.service.ProfileImageService;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.CloseButton;
import org.sakaiproject.profile2.tool.models.SimpleText;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

public class ChangeProfilePictureUrl extends Panel{
    
	private static final long serialVersionUID = 1L;
    private transient SakaiProxy sakaiProxy;
    private transient ProfileLogic profileLogic;
	private static final Logger log = Logger.getLogger(ChangeProfilePictureUrl.class);

	/**
	 * Default constructor if modifying own
	 */
	public ChangeProfilePictureUrl(String id)   {
		super(id);
		log.warn("ChangeProfilePictureUpload()");

		sakaiProxy = getSakaiProxy();
		
		//get user for this profile and render it
		String userUuid = sakaiProxy.getCurrentUserId();
		renderChangeProfilePictureUrl(userUuid);
	}
    
	/**
	 * This constructor is only called if we were a superuser editing someone else's picture.
	 * An additional catch is also in place.
	 * @param parameters 
	 */
	public ChangeProfilePictureUrl(String id, String userUuid)   {
		super(id);
		log.warn("ChangeProfilePictureUpload(" + userUuid +")");
		
		sakaiProxy = getSakaiProxy();
		
		//double check only super users
		if(!sakaiProxy.isSuperUser()) {
			log.error("ChangeProfilePictureUrl: user " + sakaiProxy.getCurrentUserId() + " attempted to access ChangeProfilePictureUrl for " + userUuid + ". Redirecting...");
			throw new RestartResponseException(new MyProfile());
		}
		//render for given user
		renderChangeProfilePictureUrl(userUuid);
	}
	
	
	
	
	/**
	 * Does the actual rendering of the panel
	 * @param userUuid
	 */
	private void renderChangeProfilePictureUrl(final String userUuid) {  
                
        //get API's
		sakaiProxy = getSakaiProxy();
		profileLogic = getProfileLogic();
			
		//setup SimpleText object to back the single form field 
		SimpleText simpleText = new SimpleText();
		
		//do they already have a URL that should be loaded in here?
		String externalUrl = profileLogic.getExternalImageUrl(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
		
		if(externalUrl != null) {
			simpleText.setText(externalUrl);
		}
		
		
        //setup form	
		Form form = new Form("form", new Model(simpleText));
		form.setOutputMarkupId(true);
        
		//add warning message if superUser and not editing own image
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
			editWarning.setModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{ sakaiProxy.getUserDisplayName(userUuid) } ));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
        //close button component
        CloseButton closeButton = new CloseButton("closeButton", this);
        closeButton.setOutputMarkupId(true);
		form.add(closeButton);
      
        //text
		Label textEnterUrl = new Label("textEnterUrl", new ResourceModel("text.image.url"));
		form.add(textEnterUrl);
		
		//upload
		TextField urlField = new TextField("urlField", new PropertyModel(simpleText, "text"));
		urlField.setRequired(true);
		urlField.add(new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_2_SLASHES));
		form.add(urlField);
		
		//feedback (styled to remove the list)
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);
		
		//submit button
        IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
        	
        	protected void onSubmit(AjaxRequestTarget target, Form form) {

				//get the model (already validated)
        		SimpleText simpleText = (SimpleText) form.getModelObject();
        		
        		//get the url
        		String url = simpleText.getText();
        		
        		//save via ProfileImageService
				if(getProfileImageService().setProfileImage(userUuid, url, null)) {
					//log it
					log.info("User " + userUuid + " successfully changed profile picture by url.");
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_URL, "/profile/"+userUuid, true);
					
					//refresh image data
					if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)){
						setResponsePage(new MyProfile(userUuid));
					} else {
						setResponsePage(new MyProfile());
					}
				} else {
					error(new StringResourceModel("error.url.save.failed", this, null).getString());
        			return;
				}
        		
        	};
        	
        	// update feedback panel if validation failed
        	protected void onError(AjaxRequestTarget target, Form form) { 
        		log.debug("ChangeProfilePictureUrl.onSubmit validation failed.");
        	    target.addComponent(feedback); 
        	} 
    		
        };
        submitButton.setModel(new ResourceModel("button.url.add"));
		form.add(submitButton);
		
		
		//add form to page
		add(form);
    }

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ChangeProfilePictureUrl has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
		sakaiProxy = getSakaiProxy();
	}
	
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}
	
	private ProfileImageService getProfileImageService() {
		return Locator.getProfileImageService();
	}
	
	
}


