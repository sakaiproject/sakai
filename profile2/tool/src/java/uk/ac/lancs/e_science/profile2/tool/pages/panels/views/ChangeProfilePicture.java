package uk.ac.lancs.e_science.profile2.tool.pages.panels.views;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class ChangeProfilePicture extends Panel {

	private InlineFrame uploadIFrame = null;  
	  
    public ChangeProfilePicture(String id) {  
        super(id);  
        addOnUploadedCallback();  
        setOutputMarkupId(true);  
    }  
	
    /** 
     * Called when the upload load is uploaded and ready to be used 
     * Return the url of the new uploaded resource 
     * @param upload {@link FileUpload} 
     */  
    public abstract String onFileUploaded(FileUpload upload);  
    
    /** 
     * Called once the upload is finished and the traitment of the 
     * {@link FileUpload} has been done in {@link UploadPanel#onFileUploaded} 
     * @param target an {@link AjaxRequestTarget} 
     * @param fileName name of the file on the client side 
     * @param newFileUrl Url of the uploaded file 
     */  
    public abstract void onUploadFinished(AjaxRequestTarget target, String filename, String newFileUrl);  
  
    @Override  
    protected void onBeforeRender() {  
        super.onBeforeRender();  
        if (uploadIFrame == null) {  
            // the iframe should be attached to a page to be able to get its pagemap,  
            // that's why i'm adding it in onBeforRender  
            addUploadIFrame();  
        }  
    } 
    
    /** 
     * Create the iframe containing the upload widget 
     * 
     */  
    private void addUploadIFrame() {  
        IPageLink iFrameLink = new IPageLink() {  
        	
        	//@Override  
            public Page getPage() {  
                return new ChangeProfilePictureUploadIFrame() {  
                    @Override  
                    protected String getOnUploadedCallback() {  
                        return "onUpload_" + ChangeProfilePicture.this.getMarkupId();  
                    }  
  
                    @Override  
                    protected String manageInputStream(FileUpload upload) {  
                        return ChangeProfilePicture.this.onFileUploaded(upload);  
                    }  
                };  
            }  
            //@Override  
            public Class<ChangeProfilePictureUploadIFrame> getPageIdentity() {  
                return ChangeProfilePictureUploadIFrame.class;  
            }  
        };  
        uploadIFrame = new InlineFrame("upload", getPage().getPageMap(), iFrameLink);  
        add(uploadIFrame);  
    }  
    
    /** 
     * Hackie method allowing to add a javascript in the page defining the 
     * callback called by the innerIframe 
     * 
     */  
    private void addOnUploadedCallback() {  
        final OnUploadedBehavior onUploadBehavior = new OnUploadedBehavior();  
        add(onUploadBehavior);  
        add(new WebComponent("onUploaded") {  
            @Override  
            protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {  
                // calling it through setTimeout we ensure that the callback is called  
                // in the proper execution context, that is the parent frame  
                replaceComponentTagBody(markupStream, openTag,  
                        "function onUpload_" + ChangeProfilePicture.this.getMarkupId() +  
                        "(clientFileName, newFileUrl) {window.setTimeout(function() { " +  
                        onUploadBehavior.getCallback() + " }, 0 )}");  
            }  
        });  
    } 
    
    private class OnUploadedBehavior extends AbstractDefaultAjaxBehavior {  
        public String getCallback() {  
            return generateCallbackScript(  
                    "wicketAjaxGet('" + getCallbackUrl(false) +  
                    "&amp;amp;newFileUrl=' + encodeURIComponent(newFileUrl)" +  
                    " + '&amp;amp;clientFileName=' + encodeURIComponent(clientFileName)").toString();  
        }  
        @Override  
        protected void respond(AjaxRequestTarget target) {  
        	ChangeProfilePicture.this.onUploadFinished(target, getRequest().getParameter("clientFileName"), getRequest().getParameter("newFileUrl"));  
        }  
    };
	
}
