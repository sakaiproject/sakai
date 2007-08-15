package org.sakaiproject.ziparchive.helper.pages;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.components.UploadForm;
import org.sakaiproject.scorm.content.api.Addable;

public class UploadZipEntry extends ClientPage {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadZipArchive.class);
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	@SpringBean (name="org.sakaiproject.scorm.content.api.ZipCHH")
	Addable contentHostingHandler;

	public UploadZipEntry(PageParameters parameters) {		
		final UploadForm form = new UploadForm("uploadForm") {
			protected void onSubmit() {
				FileItem fileItem = getFileItem();	
				
				File zipEntry = getFile(fileItem);
				
				String id = clientFacade.identifyZipArchive();
				
				contentHostingHandler.add(zipEntry, id);
				
				String url = clientFacade.getCompletionURL();
				this.exit(url);
			}
		};
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
	}
}
