package org.sakaiproject.ziparchive.helper.pages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.content.api.ContentHostingHandler;
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
				System.out.println("Display name: " + getDisplayName());
				
				FileItem fileItem = getFileItem();	
				
				File zipArchive = getFile(fileItem);
				
				if (zipArchive != null) {
					clientFacade.uploadZipArchive(zipArchive);
					
					String url = clientFacade.getCompletionURL();
					this.exit(url);
				}
			}
		};
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
	}
	
}
