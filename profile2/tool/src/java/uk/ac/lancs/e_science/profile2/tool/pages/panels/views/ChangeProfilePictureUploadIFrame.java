package uk.ac.lancs.e_science.profile2.tool.pages.panels.views;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;


public abstract class ChangeProfilePictureUploadIFrame extends WebPage {
	
	private boolean uploaded = false;  
    private FileUploadField uploadField;  
    private String newFileUrl;  
  
    public ChangeProfilePictureUploadIFrame() {  
        add(new UploadForm("form"));  
        addOnUploadedCallback();  
    }  
    
    /** 
     * return the callback url when upload is finished 
     * @return callback url when upload is finished 
     */  
    protected abstract String getOnUploadedCallback();  
  
    /** 
     * Called when the input stream has been uploaded and when it is available 
     * on server side 
     * return the url of the uploaded file 
     * @param upload fileUpload 
     */  
    protected abstract String manageInputStream(FileUpload upload);  
    
    private class UploadForm extends Form {  
        public UploadForm(String id) {  
            super(id);  
            uploadField = new FileUploadField("file");  
            add(uploadField);  
            add(new AjaxButton("submit"){  
                @Override  
                public void onSubmit(AjaxRequestTarget target, Form form) { 
                	System.out.println("submit button clicked");
                    target.appendJavascript("showProgressWheel()");  
                }  
            });  
        }  
  
        @Override  
        public void onSubmit() {  
        	System.out.println("submit method called");

            FileUpload upload = uploadField.getFileUpload();
            if(upload != null) {
            	System.out.println("upload is not null");
            }
            newFileUrl = manageInputStream(upload);  
            //file is now uploaded, and the IFrame will be reloaded, during  
            //reload we need to run the callback  
            uploaded = true;  
        }  
  
    } 
    
    private void addOnUploadedCallback() {  
        //a hacked component to run the callback on the parent  
        add(new WebComponent("onUploaded") {  
            @Override  
            protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {  
                if (uploaded) {  
                    if (uploadField.getFileUpload() != null){  
                        replaceComponentTagBody(markupStream, openTag,  
                                "window.parent." + getOnUploadedCallback() + "('" +  
                                uploadField.getFileUpload().getClientFileName() + "','" +  
                                newFileUrl +"')");  
                    }  
                    uploaded = false;  
                }  
            }  
        });  
    }
}
