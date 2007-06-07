package org.sakaiproject.ziparchive.helper.pages;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.components.UploadForm;

public class UploadZipArchive extends ClientPage {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadZipArchive.class);
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	public UploadZipArchive(PageParameters parameters) {		
		final UploadForm form = new UploadForm("uploadForm") {
			protected void onSubmit() {
				FileItem fileItem = getFileItem();	
				
				ResourceToolActionPipe pipe = clientFacade.getResourceToolActionPipe();
				
				InputStream stream = null;
				try {
					byte[] bytes = fileItem.get();
					
					if (null != bytes) {
						pipe.setRevisedContent(bytes);
					} else {
						stream = fileItem.getInputStream();
						pipe.setRevisedContentStream(stream);
					}
								
					String contentType = fileItem.getContentType();
		            pipe.setRevisedMimeType(contentType);
					
		            String filename = fileItem.getName();
		            
		            if (null != getDisplayName() && getDisplayName().trim().length() > 0)
		            	filename = getDisplayName();
		            
		            pipe.setFileName(filename);
		            pipe.setRevisedResourceProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.ziparchive.api.ContentHostingHandler");        
		            
		            pipe.setActionCanceled(false);
		            pipe.setErrorEncountered(false);
		            pipe.setActionCompleted(true); 
				} catch (IOException ioe) {
					log.error("Caught an io exception trying to upload file!", ioe);
					info("Unable to save this file...");
				} finally {
					if (null != pipe)
						clientFacade.closePipe(pipe);
					if (null != stream)
						try { 
							stream.close();
						} catch (IOException nioe) {
							log.info("Caught an io exception trying to close stream!", nioe);
						}
				}
				
				String url = clientFacade.getCompletionURL();
				this.exit(url);
			}
		};
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
	}
	
}
