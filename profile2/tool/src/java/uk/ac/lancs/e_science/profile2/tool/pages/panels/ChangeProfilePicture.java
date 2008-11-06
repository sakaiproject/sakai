package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class ChangeProfilePicture extends Panel{
    
    private FileUploadField uploadField;  
    
    
	public ChangeProfilePicture(String id, UserProfile userProfile) {  
        super(id);  
        
        //create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
        
        //setup form	
		Form form = new Form("form", userProfileModel) {
			public void onSubmit(){
				
				//get the backing model
				UserProfile userProfile = (UserProfile) this.getModelObject();
				
				//get sakaiProxy, then get userId from sakaiProxy, then get sakaiperson for that userId
				SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
				String userId = sakaiProxy.getCurrentUserId();
				SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
				
				//get uploaded file, get the bytes and set into sakaiPerson.
				if (uploadField != null) {
					System.out.println("1 - uploadField not null");
					FileUpload upload = uploadField.getFileUpload();
					if(upload != null) {
						System.out.println("2 - upload not null");
						byte[] photoBytes = upload.getBytes();
						sakaiPerson.setJpegPhoto(photoBytes);
						System.out.println("3 - " + photoBytes.toString());
					}
				}
				
				
				//set the photo bytes
				//sakaiPerson.setJpegPhoto(userProfile.getPicture());
				if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
					System.out.println("4 - saved ok");
				} else {
					System.out.println("4 - save failed");
				}
				
			}
		};
				
		//form.setOutputMarkupId(true);
		//form.setMultiPart(true);
       
        
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


