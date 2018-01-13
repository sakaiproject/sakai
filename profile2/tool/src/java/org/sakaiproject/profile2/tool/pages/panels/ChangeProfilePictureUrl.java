/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.pages.panels;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;

import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.CloseButton;
import org.sakaiproject.profile2.tool.models.StringModel;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class ChangeProfilePictureUrl extends Panel{
    
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileImageLogic")
	private ProfileImageLogic imageLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	protected ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
    
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	/**
	 * Default constructor if modifying own
	 */
	public ChangeProfilePictureUrl(String id)   {
		super(id);
		log.debug("ChangeProfilePictureUpload()");

		//get user for this profile and render it
		String userUuid = sakaiProxy.getCurrentUserId();
		renderChangeProfilePictureUrl(userUuid);
	}
    
	/**
	 * This constructor is only called if we were a superuser editing someone else's picture.
	 * An additional catch is also in place.
	 * @param id		component id 
	 * @param userUuid	uuid of other user
	 */
	public ChangeProfilePictureUrl(String id, String userUuid)   {
		super(id);
		log.debug("ChangeProfilePictureUpload(" + userUuid +")");
		
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
                
     
			
		//setup SimpleText object to back the single form field 
		StringModel stringModel = new StringModel();
		
		ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(userUuid);
		ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(userUuid);
		
		//do they already have a URL that should be loaded in here?
		ProfileImage profileImage = imageLogic.getProfileImage(userUuid, prefs, privacy,  ProfileConstants.PROFILE_IMAGE_MAIN);		
		
		//if its not blank AND it's not equalt to the default image url, show it
		String externalUrl = profileImage.getExternalImageUrl();
		if(StringUtils.isNotBlank(externalUrl) && !StringUtils.equals(externalUrl, imageLogic.getUnavailableImageURL())) {
			stringModel.setString(profileImage.getExternalImageUrl());
		}
		
		
        //setup form	
		Form form = new Form("form", new Model(stringModel));
		form.setOutputMarkupId(true);
        
		//add warning message if superUser and not editing own image
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
			editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{ sakaiProxy.getUserDisplayName(userUuid) } ));
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
		TextField urlField = new TextField("urlField", new PropertyModel(stringModel, "string"));
		urlField.setMarkupId("pictureurl");
		urlField.setOutputMarkupId(true);
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
        		StringModel stringModel = (StringModel) form.getModelObject();
        		
        		//get the url
        		String url = stringModel.getString();
        		
        		//save via ProfileImageService
				if(imageLogic.setExternalProfileImage(userUuid, url, null, null)) {
					//log it
					log.info("User " + userUuid + " successfully changed profile picture by url.");
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_URL, "/profile/"+userUuid, true);
					
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_URL, sakaiProxy.getCurrentUserId());
					}
					
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
        	    target.add(feedback); 
        	} 
    		
        };
        submitButton.setModel(new ResourceModel("button.url.add"));
		form.add(submitButton);
		
		
		//add form to page
		add(form);
    }
	
}


