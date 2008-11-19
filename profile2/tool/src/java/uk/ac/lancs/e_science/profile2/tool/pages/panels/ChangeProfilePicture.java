package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.components.CloseButton;
import uk.ac.lancs.e_science.profile2.tool.components.ComponentVisualErrorBehavior;
import uk.ac.lancs.e_science.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import uk.ac.lancs.e_science.profile2.tool.components.FeedbackLabel;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;

public class ChangeProfilePicture extends Panel{
    
    private FileUploadField uploadField;
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;
	private transient Logger log = Logger.getLogger(ChangeProfilePicture.class);

	private final int MAX_IMAGE_XY = 400; //one side will be scaled to this if larger. 400 is large enough
    
	public ChangeProfilePicture(String id, UserProfile userProfile) {  
        super(id);  
        
        //create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		
		System.out.println("---" + profile.getFriendsForUser("1", false).toString());

		        
        //setup form	
		Form form = new Form("form", userProfileModel) {
			public void onSubmit(){
				
				//get the backing model
				UserProfile userProfile = (UserProfile) this.getModelObject();
				
				boolean proceed = true;
				
				//get userid and sakaiperson for this user
				String userId = sakaiProxy.getCurrentUserId();
				SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
				/*
				//get uploaded file, get the bytes and set into sakaiPerson
				if (uploadField != null) {
					FileUpload upload = uploadField.getFileUpload();
					if(upload != null) {
						//get bytes, set into sakaiperson directly
						byte[] photoBytes = upload.getBytes();
						sakaiPerson.setJpegPhoto(photoBytes);
					} else {
						proceed=false;
						log.error("upload was null.");
						error(new ResourceModel("error.no.file.uploaded"));
					}
				}  else {
					proceed=false;
					//PRINT MESSAGE INTO A DIV
					log.error("uploadField was null.");
					error(new ResourceModel("error.no.file.uploaded"));
				}
				*/
				
				FileUpload upload = uploadField.getFileUpload();
				
				if (upload == null) {
					log.error("upload was null.");
					error(new StringResourceModel("error.no.file.uploaded", this, null).getString());
				    return;
				} else if (upload.getSize() == 0) {
				    log.error("upload was empty.");
					error(new StringResourceModel("error.empty.file.uploaded", this, null).getString());
					return;
				 } else if (!profile.checkContentTypeForProfileImage(upload.getContentType())) {
					 error(new StringResourceModel("error.invalid.image.type", this, null).getString());
				     return;
				 } else {
					 //get bytes, set into sakaiperson directly
					 byte[] photoBytes = upload.getBytes();
					 
					 photoBytes = profile.scaleImage(photoBytes, MAX_IMAGE_XY);
					 
					 sakaiPerson.setJpegPhoto(photoBytes);
					 
					 //save
					 if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
						 log.info("SakaiPerson save ok");
						 setResponsePage(new MyProfile()); //to refresh the image data
					} else {
						log.error("SakaiPerson save failed");
					}
				 }
				
				
				
			}
		};
		
		//get the max upload size from Sakai
		form.setMaxSize(Bytes.megabytes(sakaiProxy.getMaxProfilePictureSize()));	
		form.setOutputMarkupId(true);
		form.setMultiPart(true);
       
        
        //close button - this needs to be a component -TODO
        form.add(new CloseButton("closeButton"));
      
        //text
		Label textSelectImage = new Label("textSelectImage", new ResourceModel("text.upload.image.file"));
		form.add(textSelectImage);
		
		//feedback
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		form.add(feedback);
		
		// filteredErrorLevels will not be shown in the FeedbackPanel
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
		
		//upload
		uploadField = new FileUploadField("picture");
		form.add(uploadField);
		
		//file feedback will be redirected here
        final FeedbackLabel fileFeedback = new FeedbackLabel("fileFeedback", form);
        fileFeedback.setOutputMarkupId(true);
        uploadField.add(new ComponentVisualErrorBehavior("onblur", fileFeedback));
        form.add(fileFeedback);
		
		//submit button
		Button submitButton = new Button("submit", new ResourceModel("button.upload"));
			
		
		form.add(submitButton);
		
		//form indicator - show when the submit button has been clicked - TODO
		//requires AJAX submit button
		AjaxIndicator indicator = new AjaxIndicator("indicator");
		indicator.setOutputMarkupPlaceholderTag(true);
		indicator.setVisible(false);
		form.add(indicator);
		
		//add form to page
		add(form);
    }
	
	
	
	
	//called when the form is to be saved
	private boolean save() {
		
        
		//System.out.println(userProfile.getNickname());
		return false;
	}
	
	
}


