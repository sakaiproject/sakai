package org.sakaiproject.scorm.ui.console.wizard;

import org.apache.wicket.extensions.wizard.IWizard;
import org.apache.wicket.extensions.wizard.IWizardModel;
import org.apache.wicket.extensions.wizard.IWizardStep;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.sakaiproject.scorm.ui.console.wizard.PackageManagementWizard.PackageManagementWizardModel;

public class NextActionButton extends Button {

	private static final long serialVersionUID = 1L;

	private final IWizard wizard;
	
	/**
	 * Construct.
	 * 
	 * @param id
	 * @param wizard
	 */
	public NextActionButton(String id, IWizard wizard)
	{
		super(id);
		this.wizard = wizard;
	}

	/**
	 * @see org.apache.wicket.Component#isEnabled()
	 */
	public final boolean isEnabled()
	{
		IWizardStep activeStep = getWizardModel().getActiveStep();
		return getWizardModel().isNextAvailable() ||
			(activeStep != null && getWizardModel().isLastStep(activeStep));
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.WizardButton#onClick()
	 */
	public final void onClick()
	{
		IWizardModel wizardModel = getWizardModel();
		IWizardStep step = wizardModel.getActiveStep();

		// let the step apply any state
		step.applyState();

		// if the step completed after applying the state, move the
		// model onward
		if (step.isComplete())
		{
			if (wizardModel.isNextAvailable())
				wizardModel.next();
			else
				wizardModel.finish();
		}
		else
		{
			//error(getLocalizer().getString(
			//		"org.apache.wicket.extensions.wizard.NextButton.step.did.not.complete", this));
		}
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	protected final void onBeforeRender()
	{
		super.onBeforeRender();
		
		PackageManagementWizardModel wizardModel = (PackageManagementWizardModel)getWizardModel();
		String title = new StringBuffer()
			.append(wizardModel.getNextStep() == null ? "Save" : ((WizardStep)wizardModel.getNextStep()).getTitle())
			.append(" >").toString();
		
		setModel(new Model(title));
		
		getForm().setDefaultButton(this);
	}
	
	
	/**
	 * Gets the {@link IWizard}.
	 * 
	 * @return The wizard
	 */
	protected final IWizard getWizard()
	{
		return wizard;
	}

	/**
	 * Gets the {@link IWizardModel wizard model}.
	 * 
	 * @return The wizard model
	 */
	protected final IWizardModel getWizardModel()
	{
		return getWizard().getWizardModel();
	}

	/**
	 * @see org.apache.wicket.markup.html.form.Button#onSubmit()
	 */
	public final void onSubmit()
	{
		onClick();
	}

}
