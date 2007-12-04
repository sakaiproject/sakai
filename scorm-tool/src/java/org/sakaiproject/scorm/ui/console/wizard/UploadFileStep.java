package org.sakaiproject.scorm.ui.console.wizard;

import java.io.File;

import org.adl.validator.IValidator;
import org.adl.validator.IValidatorOutcome;
import org.apache.wicket.extensions.wizard.IWizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.service.api.ScormContentService;

public class UploadFileStep extends WizardStep {

	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormContentService contentService;
	
	private PackageManagementWizard wizard;
	
	public UploadFileStep(PackageManagementWizard wizard) {
		super(new Model("Upload File"), new Model(""));
		this.wizard = wizard;
		
		add(new Label("file-name", ""));
		add(new Label("file-size", ""));
	}

	public void setFile(File file) {
		if (file != null) {
			addOrReplace(new Label("file-name", file.getName()));
			addOrReplace(new Label("file-size", "" + file.length() + " bytes"));
		}
	}
	
	
	@Override
	public void onInit(IWizardModel model) {
		if (wizard.isInitialized()) {
			wizard.setUploadStep(false);
		}
	}
	
	@Override
	public void applyState() {
		if (wizard.isInitialized()) {
			setComplete(false);
			File contentPackage = wizard.getFile();
			if (contentPackage != null) 
				if (doValidate(contentPackage, false, true))
					setComplete(true);
			else
				wizard.notify("noFile");
		}
	}

	
	private boolean doValidate(File contentPackage, boolean iManifestOnly, boolean isValidateToSchema) {
		IValidator validator = contentService.validate(contentPackage, iManifestOnly, isValidateToSchema);
		wizard.setValidator(validator);
		IValidatorOutcome validatorOutcome = validator.getADLValidatorOutcome();
		wizard.setValidatorOutcome(validatorOutcome);
		
		if (!contentPackage.exists()) {
			wizard.notify("noFile");
			return false;
		}
		
		if (!validatorOutcome.getDoesIMSManifestExist()) {
			wizard.notify("noManifest");
			return false;
		}
		
		if (!validatorOutcome.getIsWellformed()) {
			wizard.notify("notWellFormed");
			return false;
		}
		
		if (!validatorOutcome.getIsValidRoot()) {
			wizard.notify("notValidRoot");
			return false;
		}
		
		if (isValidateToSchema) {
			if (!validatorOutcome.getIsValidToSchema()) {
				wizard.notify("notValidSchema");
				return false;
			}
			
			if (!validatorOutcome.getIsValidToApplicationProfile()) {
				wizard.notify("notValidApplicationProfile");
				return false;
			}
			
			if (!validatorOutcome.getDoRequiredCPFilesExist()) {
				wizard.notify("notExistingRequiredFiles");
				return false;
			}
		}

		return true;
	}
	
	
	
	
}
