package org.sakaiproject.scorm.ui.helper.zip.pages;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.components.UploadForm;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class UploadZipArchive extends SakaiPortletWebPage {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadZipArchive.class);
	
	@SpringBean
	ScormClientFacade clientFacade;

	public UploadZipArchive(PageParameters parameters) {		
		final UploadForm form = new UploadForm("uploadForm") {
			private static final long serialVersionUID = 1L;
			
			protected void onSubmit() {
				System.out.println("Display name: " + getDisplayName());
				
				FileItem fileItem = getFileItem();	
				
				File zipArchive = getFile(fileItem);
				
				if (zipArchive != null && zipArchive.exists()) {
					try {
						clientFacade.uploadZipArchive(zipArchive);
					} catch (Exception e) {
						notify("upload." + e.getClass().getName());
					}
						
						
					String url = clientFacade.getCompletionURL();
					System.out.println("Redirecting to " + url);
					this.exit(url);
				} 
			}
		};
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
	}
	
}
