package org.sakaiproject.scorm.ui.console.wizard;

import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.model.Model;

public class PersistStep extends WizardStep {

	private static final long serialVersionUID = 1L;

	public PersistStep(PackageManagementWizard wizard) {
		super(new Model("Persist"), new Model(""));
		
	}
	
}
