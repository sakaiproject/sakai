package org.sakaiproject.scorm.client.pages;

import java.util.List;

import org.adl.api.ecmascript.APIErrorManager;
import org.sakaiproject.scorm.client.ScormTool;
import org.sakaiproject.scorm.client.utils.ApiAjaxBean;
import org.sakaiproject.scorm.client.utils.ApiAjaxMethod;

import wicket.PageParameters;
import wicket.ResourceReference;
import wicket.Response;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.HiddenField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.markup.html.resources.CompressedResourceReference;
import wicket.model.CompoundPropertyModel;

public class ContentFrame extends WebPage {
	private static final ResourceReference API = new CompressedResourceReference(
			ContentFrame.class, "API.js");
	private static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			ContentFrame.class, "APIWrapper.js");
	private static final ResourceReference AJAX = new CompressedResourceReference(
			ContentFrame.class, "modified-wicket-ajax.js");
	
	private ApiAjaxBean bean = new ApiAjaxBean();
	
	public ContentFrame() {
		super();
		init();
	}
	
	public ContentFrame(PageParameters params) {
		super();
		init();
	}

	public void init() {	
		// create feedback panel to show errors
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		// add form with markup id setter so it can be updated via ajax
		final Form form = new Form("form", new CompoundPropertyModel(bean));
		add(form);
		form.setOutputMarkupId(true);

		// add form components to the form as usual
		FormComponent value = new TextField("value");		
		value.setOutputMarkupId(true);
		form.add(value);
		
		FormComponent arg1 = new HiddenField("arg1");		
		arg1.setOutputMarkupId(true);
		form.add(arg1);
		
		FormComponent arg2 = new HiddenField("arg2");		
		arg2.setOutputMarkupId(true);
		form.add(arg2);
		
		FormComponent resultComponent = new HiddenField("result");
		resultComponent.setOutputMarkupId(true);
		form.add(resultComponent);
		
		ResourceReference[] references = new ResourceReference[] { AJAX, API };
		
		form.add(new ApiAjaxMethod(form, "GetDiagnostic", references, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);
				return errorManager.getErrorDiagnostic(arg);
			}
		});
		
		form.add(new ApiAjaxMethod(form, "GetErrorString", null, 1, bean) {	
			private static final long serialVersionUID = 2L;
			
			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);
				return errorManager.getErrorDescription(arg); 
			}
		});
		
		form.add(new ApiAjaxMethod(form, "GetLastError", null, 0, bean) {	
			private static final long serialVersionUID = 3L;
			
			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				return errorManager.getCurrentErrorCode(); 
			}
		});
				
				
		this.add(new AbstractAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			protected String getImplementationId()
			{
				return "APIWrapper";
			}

			protected void onRenderHeadInitContribution(Response response)
			{
				super.onRenderHeadInitContribution(response);
				writeJsReference(response, API_WRAPPER);
			}

			public void onRequest()
			{
			}
		});
	}
	
	
	private String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";
		
		return argumentValues.get(0);
	}
	
	
}
