package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.service.ProfileImageService;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.CloseButton;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

public class ChangeProfilePictureUpload extends Panel{
    
	private static final long serialVersionUID = 1L;
	private FileUploadField uploadField;
	private transient SakaiProxy sakaiProxy;

    private static final Logger log = Logger.getLogger(ChangeProfilePictureUpload.class);

    /**
	 * Default constructor if modifying own
	 */
	public ChangeProfilePictureUpload(String id)   {
		super(id);
		log.warn("ChangeProfilePictureUpload()");

		sakaiProxy = getSakaiProxy();
		
		//get user for this profile and render it
		String userUuid = sakaiProxy.getCurrentUserId();
		renderChangeProfilePictureUpload(userUuid);
	}
    
	/**
	 * This constructor only called if we were a superuser editing someone else's picture.
	 * An additional catch is also in place.
	 * @param parameters 
	 */
	public ChangeProfilePictureUpload(String id, String userUuid)   {
		super(id);
		log.warn("ChangeProfilePictureUpload(" + userUuid +")");
		
		sakaiProxy = getSakaiProxy();
		
		//double check only super users
		if(!sakaiProxy.isSuperUser()) {
			log.error("ChangeProfilePictureUpload: user " + sakaiProxy.getCurrentUserId() + " attempted to access ChangeProfilePictureUpload for " + userUuid + ". Redirecting...");
			throw new RestartResponseException(new MyProfile());
		}
		//render for given user
		renderChangeProfilePictureUpload(userUuid);
	}
	
	/**
	 * Does the actual rendering of the panel
	 * @param userUuid
	 */
	private void renderChangeProfilePictureUpload(final String userUuid) {  
        
        //setup form	
		Form form = new Form("form") {
			private static final long serialVersionUID = 1L;

			public void onSubmit(){
				
				//get file that was uploaded
				FileUpload upload = uploadField.getFileUpload();
				
				if (upload == null) {
					log.error("Profile.ChangeProfilePicture.onSubmit: upload was null.");
					error(new StringResourceModel("error.no.file.uploaded", this, null).getString());
				    return;
				} else if (upload.getSize() == 0) {
				    log.error("Profile.ChangeProfilePicture.onSubmit: upload was empty.");
					error(new StringResourceModel("error.empty.file.uploaded", this, null).getString());
					return;
				} else if (!ProfileUtils.checkContentTypeForProfileImage(upload.getContentType())) {
					log.error("Profile.ChangeProfilePicture.onSubmit: invalid file type uploaded for profile picture");
					error(new StringResourceModel("error.invalid.image.type", this, null).getString());
				    return;
				} else {
					
					String mimeType = upload.getContentType();
					String fileName = upload.getClientFileName();
					
					//ok so get bytes of file uploaded
					byte[] imageBytes = upload.getBytes();
					
					//add image using ProfileImageService which scales and sets up CHS automatically
					//note that this has changed so it uses the service. if needs to be changed back so there is no dependency on the PIS,
					//just grab the bits from the methods in PIS that do what is required, remove from applicationContext.xml, Locator.java and ProfileApplication.java
					//likewise for ChangeProfilePictureUrl.java
					if(getProfileImageService().setProfileImage(userUuid, imageBytes, mimeType, null)) {
						
						//log it
						log.info("User " + userUuid + " successfully changed profile picture by upload.");
						
						//post update event
						getSakaiProxy().postEvent(ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_UPLOAD, "/profile/"+userUuid, true);
						
						//refresh image data
						if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)){
							setResponsePage(new MyProfile(userUuid));
						} else {
							setResponsePage(new MyProfile());
						}
					} else {
						error(new StringResourceModel("error.file.save.failed", this, null).getString());
						return;
					}
										
				}
				
			}
		};
		
		//get the max upload size from Sakai
		int maxSize = getSakaiProxy().getMaxProfilePictureSize();
		
		//setup form
		form.setMaxSize(Bytes.megabytes(maxSize));	
		form.setOutputMarkupId(true);
		form.setMultiPart(true);
		
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
		Label textSelectImage = new Label("textSelectImage", new StringResourceModel("text.upload.image.file", null, new Object[]{ maxSize } ));
		form.add(textSelectImage);
		
		//feedback
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		form.add(feedback);
		
		// filteredErrorLevels will not be shown in the FeedbackPanel
		//this way we can control them. see the onSubmit method for the form
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
		
		//upload
		uploadField = new FileUploadField("picture");
		form.add(uploadField);
		
		//file feedback will be redirected here
        final FeedbackLabel fileFeedback = new FeedbackLabel("fileFeedback", form);
        fileFeedback.setOutputMarkupId(true);
        form.add(fileFeedback);
		
		//submit button
		//TODO form indicator on this button, but requires an AJAX button which can't handle file uploads.
		Button submitButton = new Button("submit", new ResourceModel("button.upload"));
		
		form.add(submitButton);
		
		//add form to page
		add(form);
    }
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ChangeProfilePictureUpload has been deserialized.");
		//re-init our transient objects
		//profileImageService = getProfileImageService();
		//sakaiProxy = getSakaiProxy();
	}
	
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

	private ProfileImageService getProfileImageService() {
		return Locator.getProfileImageService();
	}
	
}


