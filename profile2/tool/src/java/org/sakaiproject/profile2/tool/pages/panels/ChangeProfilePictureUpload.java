package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
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

import org.sakaiproject.profile2.api.Profile;
import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.profile2.api.SakaiProxy;
import org.sakaiproject.profile2.tool.ProfileApplication;
import org.sakaiproject.profile2.tool.components.CloseButton;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.tool.pages.MyProfile;

public class ChangeProfilePictureUpload extends Panel{
    
	private static final long serialVersionUID = 1L;
	private FileUploadField uploadField;
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;
	private static final Logger log = Logger.getLogger(ChangeProfilePictureUpload.class);

	public ChangeProfilePictureUpload(String id) {  
        super(id);  
        
        log.debug("ChangeProfilePictureUpload()");
        
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		   
        //setup form	
		Form form = new Form("form") {
			private static final long serialVersionUID = 1L;

			public void onSubmit(){
				
				//get userid and sakaiperson for this user
				String userId = sakaiProxy.getCurrentUserId();
				
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
				} else if (!profile.checkContentTypeForProfileImage(upload.getContentType())) {
					log.error("Profile.ChangeProfilePicture.onSubmit: invalid file type uploaded for profile picture");
					error(new StringResourceModel("error.invalid.image.type", this, null).getString());
				    return;
				} else {
					
					String mimeType = upload.getContentType();
					String fileName = upload.getClientFileName();
					
					//ok so get bytes of file uploaded
					byte[] imageBytes = upload.getBytes();
					
					//TODO: take a copy of the bytes and then scale each individually but need to monitor memory usage
					
					/*
					 * MAIN PROFILE IMAGE
					 */
					//scale image
					imageBytes = profile.scaleImage(imageBytes, ProfileConstants.MAX_IMAGE_XY);
					 
					//create resource ID
					String mainResourceId = sakaiProxy.getProfileImageResourcePath(userId, ProfileConstants.PROFILE_IMAGE_MAIN);
					log.debug("Profile.ChangeProfilePicture.onSubmit mainResourceId: " + mainResourceId);
					
					//save, if error, log and return.
					if(!sakaiProxy.saveFile(mainResourceId, userId, fileName, mimeType, imageBytes)) {
						error(new StringResourceModel("error.file.save.failed", this, null).getString());
					    return;
					}

					/*
					 * THUMBNAIL PROFILE IMAGE
					 */
					//scale image
					imageBytes = profile.scaleImage(imageBytes, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY);
					 
					//create resource ID
					String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userId, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
					log.debug("Profile.ChangeProfilePicture.onSubmit thumbnailResourceId: " + thumbnailResourceId);
					
					//save, if error, log and return.
					if(!sakaiProxy.saveFile(thumbnailResourceId, userId, fileName, mimeType, imageBytes)) {
						error(new StringResourceModel("error.file.save.failed", this, null).getString());
					    return;
					}
					
					/*
					 * SAVE IMAGE RESOURCE IDS
					 */
					//save
					if(profile.addNewProfileImage(userId, mainResourceId, thumbnailResourceId)) {
						
						//log it
						log.info("User " + userId + " successfully changed profile picture by upload.");
						
						//post update event
						sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_UPLOAD, "/profile/"+userId, true);
						
						//refresh image data
						setResponsePage(new MyProfile());
					} else {
						error(new StringResourceModel("error.file.save.failed", this, null).getString());
						return;
					}
					
					
				}
				
				
				
			}
		};
		
		//get the max upload size from Sakai
		int maxSize = sakaiProxy.getMaxProfilePictureSize();
		
		//setup form
		form.setMaxSize(Bytes.megabytes(maxSize));	
		form.setOutputMarkupId(true);
		form.setMultiPart(true);
        
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
		profile = ProfileApplication.get().getProfile();
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
	}

}


