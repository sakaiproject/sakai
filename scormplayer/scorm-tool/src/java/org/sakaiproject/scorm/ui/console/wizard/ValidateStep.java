package org.sakaiproject.scorm.ui.console.wizard;

import java.io.File;

import org.adl.validator.IValidator;
import org.adl.validator.IValidatorOutcome;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.w3c.dom.Document;


public class ValidateStep extends WizardStep {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(ValidateStep.class);
	
	@SpringBean
	ScormContentService contentService;
	
	private PackageManagementWizard wizard;
	
	public ValidateStep(PackageManagementWizard wizard) {
		super(new Model("Validate"), new Model(""));
		
		this.wizard = wizard;
	}
	
	@Override
	protected final void onBeforeRender()
	{
		super.onBeforeRender();
		
		IValidatorOutcome outcome = wizard.getValidatorOutcome();
		Document document = outcome.getDocument();
		String title = contentService.getContentPackageTitle(document);
		
		addOrReplace(new Label("content-package-title", title));
	}
	
	@Override
	public void applyState() {
		if (wizard.isInitialized()) {
			setComplete(false);
			File contentPackage = wizard.getFile();
			IValidator validator = wizard.getValidator();
			IValidatorOutcome outcome = wizard.getValidatorOutcome();
			try {
				contentService.addContentPackage(contentPackage, validator, outcome);
				setComplete(true);
			} catch (PermissionException e) {
				wizard.notify("not-allowed-to-add-cp");
				log.debug("Caught a permission exception adding content package", e);
			} catch (IdUsedException e) {
				wizard.notify("id-already-in-use");
				log.debug("Id already in use", e);
			} catch (IdInvalidException e) {
				wizard.notify("id-invalid");
				log.warn("Invalid id generated for content package ", e);
			} catch (InconsistentException e) {
				wizard.notify("containing-collection-does-not-exist");
				log.error("Containing collection does not exist ", e);
			} catch (ServerOverloadException e) {
				wizard.notify("filesystem-write-failed");
				log.error("File system write failed ", e);
			} catch (Exception e) {
				wizard.notify("generic-save-exception");
				log.error("Caught a generic exception adding content package ", e);
			}
		}
	}
	
}
