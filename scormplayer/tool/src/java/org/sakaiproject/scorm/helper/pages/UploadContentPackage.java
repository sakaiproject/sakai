package org.sakaiproject.scorm.helper.pages;

import java.io.File;

import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.helper.UploadForm;
import org.sakaiproject.scorm.helper.panels.DetailsPanel;

import wicket.PageParameters;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.SubmitLink;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.upload.FileUpload;
import wicket.markup.html.form.upload.FileUploadField;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.PropertyModel;
import wicket.model.StringResourceModel;
import wicket.util.file.Folder;

public class UploadContentPackage extends ClientPage {
	private static final long serialVersionUID = 1L;
	
	private Folder temporaryFolder;
	
	public UploadContentPackage(PageParameters parameters) {
		FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");
		final FileUploadField fileUploadField = new FileUploadField("contentPackageFile");
		final FormComponent displayNameInput = new TextField("displayName");
		final DetailsPanel detailsPanel = new DetailsPanel("details");
		FormComponent submitLink = new SubmitLink("submitButton"); 
		//submitLink.setModel(new StringResourceModel("submitButton", this, null));
		
		// Control visibility
		detailsPanel.setVisible(false);
		
		temporaryFolder = new Folder(System.getProperty("java.io.tmpdir"), "ContentPackages");
		temporaryFolder.mkdirs();
		
		// Add folder view
		/*add(new Label("dir", uploadFolder.getAbsolutePath()));
		files.addAll(Arrays.asList(uploadFolder.listFiles()));
		fileListView = new FileListView("fileList", files);
		add(fileListView);

		// Add upload form with ajax progress bar
		final FileUploadForm ajaxSimpleUploadForm = new FileUploadForm("ajax-simpleUpload");
		ajaxSimpleUploadForm.add(new UploadProgressBar("progress", ajaxSimpleUploadForm));
		add(ajaxSimpleUploadForm);*/
		
		final UploadForm form = new UploadForm("uploadForm") {
			protected void onSubmit()
			{
				final FileUpload fileUpload = fileUploadField.getFileUpload();
				
				if (null != fileUpload) {
					File file = new File(temporaryFolder, fileUpload.getClientFileName());

					//checkFileExists(file);
					try
					{
						file.createNewFile();
						fileUpload.writeTo(file);
					}
					catch (Exception e)
					{
						throw new IllegalStateException("Unable to write file");
					}

					// refresh the file list view
					//refreshFiles();
				}
			}
		};

		AjaxFallbackLink showDetailsLink = new AjaxFallbackLink("showDetails") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				detailsPanel.setVisible(true);
				target.addComponent(form);
			}
		};
		
		form.add(uploadFeedback);
		form.add(newResourceLabel("fileToUploadLabel", this));
		form.add(newResourceLabel("displayNameLabel", this));
		form.add(displayNameInput);
		form.add(fileUploadField);
		form.add(detailsPanel);
		form.add(showDetailsLink);
		form.add(submitLink);
		
		add(form); 

	}
	
}
