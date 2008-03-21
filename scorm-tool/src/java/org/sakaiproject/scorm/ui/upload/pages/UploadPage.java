package org.sakaiproject.scorm.ui.upload.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

public class UploadPage extends ConsoleBasePage implements ScormConstants {

	private static final long serialVersionUID = 1L;
	
	private static ResourceReference PAGE_ICON = new ResourceReference(ConsoleBasePage.class, "res/table_add.png");
	
	private static Log log = LogFactory.getLog(FileUploadForm.class);
	
	@SpringBean
	ScormContentService contentService;
	@SpringBean
	ScormResourceService resourceService;
	
	public UploadPage(PageParameters params) {
		add(new FileUploadForm("uploadForm"));
	}
	
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	public class FileUploadForm extends Form {
		
		private static final long serialVersionUID = 1L;
		
		private FileUploadField fileUploadField;
		private boolean fileHidden = true;
		private boolean fileValidated = false;
		
		public FileUploadForm(String id) {
			super(id);
			
			IModel model = new CompoundPropertyModel(this);
			this.setModel(model);
			
			// We need to establish the largest file allowed to be uploaded
			setMaxSize(Bytes.megabytes(resourceService.getMaximumUploadFileSize()));
			
			setMultiPart(true);
			
			add(fileUploadField = new FileUploadField("fileInput"));
			add(new CheckBox("fileValidated"));
			
			add(new CancelButton("cancel", PackageListPage.class));
		}

		protected void onSubmit() {
			if (fileUploadField != null) {
				final FileUpload upload = fileUploadField.getFileUpload();
		        if (upload != null) {
		            try {
		            	String resourceId = resourceService.putArchive(upload.getInputStream(), upload.getClientFileName(), upload.getContentType(), isFileHidden());
		            	
		            	int status = contentService.validate(resourceId, false, isFileValidated());
		            	
		            	if (status == VALIDATION_SUCCESS)
		            		setResponsePage(PackageListPage.class);
		            	else {
			            	PageParameters params = new PageParameters();
			            	params.add("resourceId", resourceId);
			            	params.put("status", status);
			            	
			            	setResponsePage(ConfirmPage.class, params);
		            	}
		            } catch (Exception e) {
		            	UploadPage.this.warn(getLocalizer().getString("upload.failed", UploadPage.this));
		                log.error("Failed to upload file", e);
		            }
		        }
			}
		}

		public boolean isFileHidden() {
			return fileHidden;
		}

		public void setFileHidden(boolean fileHidden) {
			this.fileHidden = fileHidden;
		}

		public boolean isFileValidated() {
			return fileValidated;
		}

		public void setFileValidated(boolean fileValidated) {
			this.fileValidated = fileValidated;
		}
	}
	
	
	
}
