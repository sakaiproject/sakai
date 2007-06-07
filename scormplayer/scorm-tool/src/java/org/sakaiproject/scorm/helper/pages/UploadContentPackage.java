package org.sakaiproject.scorm.helper.pages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.components.DetailsPanel;
import org.sakaiproject.scorm.client.components.UploadForm;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.file.Folder;

public class UploadContentPackage extends ClientPage {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadContentPackage.class);
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	public UploadContentPackage(PageParameters parameters) {
		
		//FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");
		//final FileUploadField fileUploadField = new FileUploadField("fileUploadField");
		//final TextField displayNameInput = new TextField("displayName");
		//Button submitButton = new Button("submitButton"); 
		//submitButton.setModel(new StringResourceModel("submitButton", this, null));
			
		//temporaryFolder = new Folder(System.getProperty("java.io.tmpdir"), "ContentPackages");
		//temporaryFolder.mkdirs();
		
		// Add folder view
		/*add(new Label("dir", uploadFolder.getAbsolutePath()));
		files.addAll(Arrays.asList(uploadFolder.listFiles()));
		fileListView = new FileListView("fileList", files);
		add(fileListView);

		// Add upload form with ajax progress bar
		final FileUploadForm ajaxSimpleUploadForm = new FileUploadForm("ajax-simpleUpload");
		ajaxSimpleUploadForm.add(new UploadProgressBar("progress", ajaxSimpleUploadForm));
		add(ajaxSimpleUploadForm);*/
		
		final UploadForm form = new UploadForm("uploadForm");
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
		form.add(new CheckBox("dontValidateSchema"));
		form.add(newResourceLabel("validateSchemaCaption", this));
	}
	
	/*protected void uploadFile() {
		FileItem fileItem = (FileItem)((WebRequest)getRequest()).getHttpServletRequest().getAttribute("fileInput");
		
		ResourceToolActionPipe pipe = clientFacade.getResourceToolActionPipe();
		InputStream stream = null;
		try {			
			stream = fileItem.getInputStream();
			
			if (stream == null) {
				byte[] bytes = fileItem.get();
				pipe.setRevisedContent(bytes);
				info("saved bytes " + fileItem.getName());
			} else {
				pipe.setRevisedContentStream(stream);
				info("saved file stream " + fileItem.getName());
			}
			
			String contentType = fileItem.getContentType();
            pipe.setRevisedMimeType(contentType);
			
            pipe.setFileName(fileItem.getName());
            
		} catch (IOException ioe) {
			log.error("Caught an io exception trying to upload file!", ioe);
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
	}*/
	
	
	/*protected void uploadFile(FileUploadField fileUploadField) {
		//FileUploadField fileUploadField = getFileUploadField();
		
		if (null == fileUploadField) {
			log.error("File upload field object is null!");
			return;
		}
		
		FileUpload fileUpload = fileUploadField.getFileUpload();
		
		if (null == fileUpload) {
			log.error("File upload object is null!");
			return;
		}
		
		ResourceToolActionPipe pipe = clientFacade.getResourceToolActionPipe();
		
		if (null == pipe) {
			log.error("No pipe found when uploading file!");
			return;
		}
		
		String fileName = "Test"; //getDisplayName();
		if (null == fileName || fileName.trim().length() <= 0) {
			fileName = fileUpload.getClientFileName();
		
			if (null == fileName || fileName.trim().length() <= 0) {
				log.error("No file name provided!");
				return;
			}
		}
				
		try {
			// First, let's check to see if the data is available via the stream class
			InputStream inputStream = fileUpload.getInputStream();
			// If not, grab it from bytes
			if (null == inputStream) {
				byte[] bytes = fileUpload.getBytes();
				pipe.setRevisedContent(bytes);
			} else {
				pipe.setRevisedContentStream(inputStream);
			}
			String contentType = fileUpload.getContentType();
			pipe.setRevisedMimeType(contentType);
			pipe.setFileName(fileName);
			//pipe.setFileName((String)fileNameInput.getValue());
		} catch (IOException ioe) {
			log.warn("Caught an io exception trying to write input stream to content api", ioe);
		} finally {
			fileUpload.closeStreams();
		}
		
		File file = new File(temporaryFolder, fileUpload.getClientFileName());

		//checkFileExists(file);
		try {
			file.createNewFile();
			fileUpload.writeTo(file);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to write file");
		}

		// refresh the file list view
		//refreshFiles();
	}*/
	
	
}
