package org.sakaiproject.scorm.helper.pages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.components.DetailsPanel;
import org.sakaiproject.scorm.client.components.UploadForm;

import org.adl.validator.IValidatorOutcome;
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
		final UploadForm form = new UploadForm("uploadForm") {
			private static final long serialVersionUID = 1L;

			protected void onSubmit() {
				FileItem fileItem = getFileItem();		
				File contentPackage = getFile(fileItem);
				
				if (contentPackage == null)
					return;
				
				boolean onlyValidateManifest = getDontValidateSchema();
				IValidatorOutcome outcome = validate(this, contentPackage, onlyValidateManifest);
				
				if (null == outcome)
					return;
				
				String url = clientFacade.getCompletionURL();		
				exit(url);
			}
		
			private IValidatorOutcome validate(UploadForm form, File contentPackage, boolean onlyValidateManifest) {
				IValidatorOutcome validatorOutcome = clientFacade.validateContentPackage(contentPackage, onlyValidateManifest);

				if (!contentPackage.exists()) {
					form.notify("noFile");
					return null;
				}
				
				if (!validatorOutcome.getDoesIMSManifestExist()) {
					form.notify("noManifest");
					return null;
				}
				
				if (!validatorOutcome.getIsWellformed()) {
					form.notify("notWellFormed");
					return null;
				}
				
				if (!validatorOutcome.getIsValidRoot()) {
					form.notify("notValidRoot");
					return null;
				}
				
				if (!onlyValidateManifest) {
					if (!validatorOutcome.getIsValidToSchema()) {
						form.notify("notValidSchema");
						return null;
					}
					
					if (!validatorOutcome.getIsValidToApplicationProfile()) {
						form.notify("notValidApplicationProfile");
						return null;
					}
					
					if (!validatorOutcome.getDoRequiredCPFilesExist()) {
						form.notify("notExistingRequiredFiles");
						return null;
					}
				}
				
				return validatorOutcome;
			}
		
		
		};
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
		form.add(new CheckBox("dontValidateSchema"));
		form.add(newResourceLabel("validateSchemaCaption", this));
	}
	
	
	
}
