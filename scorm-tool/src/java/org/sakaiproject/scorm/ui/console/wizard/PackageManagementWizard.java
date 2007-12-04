package org.sakaiproject.scorm.ui.console.wizard;

import java.io.File;

import org.adl.validator.IValidator;
import org.adl.validator.IValidatorOutcome;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.wizard.IWizardStep;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.console.components.FileUploadForm;
import org.sakaiproject.scorm.ui.console.components.NotificationPanel;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;

public class PackageManagementWizard extends Wizard {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(PackageManagementWizard.class);
	
	private File file;
	private IValidator validator;
	private IValidatorOutcome validatorOutcome;
	
	private boolean isInitialized = false;
	private boolean isUploadStep = true;
	
	private ChooseFileStep chooseFileStep;
	private ValidateStep validateStep;
	private UploadFileStep uploadFileStep;
	
	private FileUploadForm form;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	public PackageManagementWizard(String id) {
		super(id, false);
		WizardModel model = new PackageManagementWizardModel();
		
		chooseFileStep = new ChooseFileStep(this);
		uploadFileStep = new UploadFileStep(this);
		validateStep = new ValidateStep(this);
		//PersistStep persistStep = new PersistStep(this);
		
		setForm(chooseFileStep.getForm());
		
		model.add(chooseFileStep);
		model.add(uploadFileStep);
		model.add(validateStep);
		//model.add(persistStep);
		
		// Initialize wizard
		init(model);
			
		isInitialized = true;
	}
	
	public final void notify(String key) {
		String message = getLocalizer().getString(key, this);
		Session.get().getFeedbackMessages().warn(this, message);
	}
	
	@Override
	protected Component newButtonBar(String id)
	{
		return new ButtonBar(id, this);
	}
	
	@Override
	protected Form newForm(String id)
	{
		if (isUploadStep) {
			form = new FileUploadForm(id);
			chooseFileStep.setForm(form);
			return form;
		}
		return new Form(id);
	}
		
	@Override
	protected FeedbackPanel newFeedbackPanel(String id)
	{
		return new NotificationPanel(id, new ContainerFeedbackMessageFilter(this));
	}
	
	@Override
    public void onCancel()
    {
		clearState();
        setResponsePage(PackageListPage.class);
    }

    @Override
    public void onFinish()
    {
    	clearState();
        setResponsePage(PackageListPage.class);
    }
    
    public void clearState() {
    	this.file = null;
    	this.validator = null;
    	this.validatorOutcome = null;
    }
    
    // Accessors
    
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		uploadFileStep.setFile(file);
		this.validator = null;
		this.validatorOutcome = null;
	}


	public FileUploadForm getForm() {
		return form;
	}


	public void setForm(FileUploadForm form) {
		this.form = form;
	}


	public boolean isInitialized() {
		return isInitialized;
	}


	public boolean isUploadStep() {
		return isUploadStep;
	}


	public void setUploadStep(boolean isUploadStep) {
		this.isUploadStep = isUploadStep;
	}


	public IValidator getValidator() {
		return validator;
	}


	public void setValidator(IValidator validator) {
		this.validator = validator;
	}


	public IValidatorOutcome getValidatorOutcome() {
		return validatorOutcome;
	}


	public void setValidatorOutcome(IValidatorOutcome validatorOutcome) {
		this.validatorOutcome = validatorOutcome;
	}


	public ValidateStep getDisplayStep() {
		return validateStep;
	}

	
	public class PackageManagementWizardModel extends WizardModel {

		private static final long serialVersionUID = 1L;

		public IWizardStep getNextStep() {
			IWizardStep step = null;
			
			try {
				step = findNextVisibleStep();
			} catch (IllegalStateException ise) {
				step = null;
			}
			
			return step;
		}
	}

}
