package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.pages.panels.views.ChangeProfilePicture;

public class SakaiModalWindow extends ModalWindow {

	public SakaiModalWindow(String id) {
		super(id);
		
		setOutputMarkupId(true);  

        //init and minimum sizes
        setInitialWidth(600);
        setInitialHeight(250);
        setMinimalWidth(600);
        setMinimalHeight(250);

        //will resize to height of content panel
        setUseInitialHeight(false); 
        
        //style
        setCssClassName(CSS_CLASS_BLUE);
        
        //window title
        setTitle(new ResourceModel("title.change.profile.picture"));

        //when we need to use this modal window for more than one thing, add in an if statement around this, and the title fields.
        setContent(new ChangeProfilePicture("content"){  
            @Override  
            public String onFileUploaded(FileUpload upload) {  
                if (upload != null){  
                    System.out.println("upload is ok");
                }  
                return "";  
            }  
          
            @Override  
            public void onUploadFinished(AjaxRequestTarget target, String filename, String newFileUrl) {  
                //when upload is finished, will be called  
                //messageLabel.setModelObject(newFileUrl);  
                //target.addComponent(messageLabel);  
          
                //avatarImage.setImageResource(new MyBlobImageResource(((WebSession)getSession()).getUser().getUsername()));  
                //target.addComponent(avatarImage);  
                //((BasePage)parent).onAvatarChanged(target); 
            	System.out.println("upload finished");
            }  
        });
        
	}

}
