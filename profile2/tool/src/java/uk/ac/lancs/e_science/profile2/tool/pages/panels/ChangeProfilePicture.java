package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;

public class ChangeProfilePicture extends Panel{
    
    private FileUploadField uploadField;
    private transient SakaiProxy sakaiProxy;
	private transient Logger log = Logger.getLogger(ChangeProfilePicture.class);

    
	public ChangeProfilePicture(String id, UserProfile userProfile) {  
        super(id);  
        
        //create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		//get SakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
        
        //setup form	
		Form form = new Form("form", userProfileModel) {
			public void onSubmit(){
				
				//get the backing model
				UserProfile userProfile = (UserProfile) this.getModelObject();
				
				boolean proceed = true;
				
				//get userid and sakaiperson for this user
				String userId = sakaiProxy.getCurrentUserId();
				SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
				
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
					log.error("uploadField was null.");
					error(new ResourceModel("error.no.file.uploaded"));
				}
				
				//save
				if(proceed) {
					if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
						log.info("SakaiPerson save ok");
						setResponsePage(new MyProfile()); //to refresh the image data
					} else {
						log.error("SakaiPerson save failed");
						error(new ResourceModel("error.save.failed"));
					}
				}
				
			}
		};
		
		//get the max upload size from Sakai
		form.setMaxSize(Bytes.megabytes(sakaiProxy.getMaxProfilePictureSize()));	
		form.setOutputMarkupId(true);
		form.setMultiPart(true);
       
        
        //close button - this needs to be a component -TODO
       // form.add(new CloseButton("closeButton"));
      
        //text
		Label textSelectImage = new Label("textSelectImage", new ResourceModel("text.upload.image.file"));
		form.add(textSelectImage);
		
		//feedback
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		form.add(feedback);
		
		//upload
		uploadField = new FileUploadField("picture");
		form.add(uploadField);
		
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


