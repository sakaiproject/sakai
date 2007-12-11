package org.sakaiproject.scorm.ui.console.wizard;

import java.io.File;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.wizard.IWizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.scorm.ui.console.components.FileUploadForm;

public class ChooseFileStep extends WizardStep {

	private static final long serialVersionUID = 1L;

	private FileUploadForm form;
	
	private PackageManagementWizard wizard;
	
	public ChooseFileStep(final PackageManagementWizard wizard) {
		super(new Model("Select File"), new Model(""));

		this.wizard = wizard;
	}
	
	@Override
	protected void onInit(IWizardModel wizardModel)
	{
		form.addFields(this);
		if (wizard.isInitialized()) {
			wizard.setUploadStep(true);
		}
	}
	
	@Override
	public void applyState()
	{
		if (wizard.isInitialized()) {
			wizard.clearState();
			File file = form.doUpload();
			if (file != null) 
				wizard.setFile(file);
			else
				wizard.setFile(null);
		}
	}

	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}

	public FileUploadForm getForm() {
		return form;
	}
	
	public void setForm(FileUploadForm form) {
		this.form = form;
	}
}
