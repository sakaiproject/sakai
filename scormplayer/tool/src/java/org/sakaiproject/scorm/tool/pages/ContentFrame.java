package org.sakaiproject.scorm.tool.pages;

import java.util.LinkedList;
import java.util.List;

import org.adl.api.ecmascript.APIErrorManager;
import org.sakaiproject.scorm.client.utils.ApiAjaxBean;
import org.sakaiproject.scorm.client.utils.ApiAjaxCallDecorator;
import org.sakaiproject.scorm.client.utils.ApiAjaxMethod;
import org.sakaiproject.scorm.tool.ScormTool;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitButton;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.JavascriptUtils;

public class ContentFrame extends WebPage {
	private static final ResourceReference API = new CompressedResourceReference(
			ContentFrame.class, "API.js");
	private static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			ContentFrame.class, "APIWrapper.js");
	private static final ResourceReference AJAX = new CompressedResourceReference(
			ContentFrame.class, "modified-wicket-ajax.js");
	
	private ApiAjaxBean bean = new ApiAjaxBean();
	
	public ContentFrame() {
		this(null);
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
		
		/*AjaxSubmitButton submitButton = new AjaxSubmitButton("ajax-submit-button", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				System.out.println("SUBMITTED!");
			}
			
		};
		submitButton.setOutputMarkupId(true);
		form.add(submitButton);*/
		
		final ResourceReference[] references = new ResourceReference[] { API };
		
		/*form.add(new AjaxFormSubmitBehavior(form, "GetDiagnostic") {
			int numArgs = 1;
			
			protected String callMethod(List<String> argumentValues) {
				return "hello new world!";
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				System.out.println("ApiAjaxMethod: onSubmit");
				
				List<String> argumentValues = new LinkedList<String>(); 
				for (int i=0;i<numArgs;i++) {
					String argumentName = "arg" + (i+1);
					
					FormComponent argumentComponent = (FormComponent)form.get(argumentName);
					String argumentValue = (String)argumentComponent.getConvertedInput();
					
					argumentValues.add(argumentValue);
				}
				
				String resultValue = callMethod(argumentValues);
				
				FormComponent resultComponent = (FormComponent)form.get("result");
				bean.setResult(resultValue);
				target.addComponent(resultComponent);
			}
			
			@Override
			protected String getImplementationId() {
				return getEvent();
			}
			
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				// Do nothing -- we don't want to add the javascript to the component.
			}
			
			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				FormComponent resultComponent = (FormComponent)form.get("result");
				return new ApiAjaxCallDecorator(resultComponent.getMarkupId());
			}
			
			@Override
			protected CharSequence getEventHandler()
			{
				final String formId = form.getMarkupId();
				final CharSequence url = getCallbackUrl(true, true);

				AppendingStringBuffer call = new AppendingStringBuffer("apiCall('").append(
						formId).append("', '").append(url).append("'");

				return getCallbackScript(call, null, null) + ";";
			}
			
			@Override
			protected void onRenderHeadInitContribution(Response response)
			{
				super.onRenderHeadInitContribution(response);
				if (null != references) {
					for (int i=0;i<references.length;i++) 
						writeJsReference(response, references[i]);
				}

				StringBuffer script = new StringBuffer().append(APIClass)
					.append(".")
					.append(getEvent())
					.append(" = function(");
				
				for (int i=0;i<numArgs;i++) {
					script.append("arg").append(i+1);
					if (i+1<numArgs)
						script.append(", ");
				}
					
				script.append(") { ");
				
				for (int i=0;i<numArgs;i++) {
					String argumentName = "arg" + (i+1);
					FormComponent argumentComponent = (FormComponent)form.get(argumentName);
					
					script.append("document.getElementById('")
							.append(argumentComponent.getMarkupId()).append("').value")
							.append(" = arg").append(i+1).append(";\n");
				}
				
				script.append(getEventHandler())
					.append("};");
				
				JavascriptUtils.writeJavascript(response, script.toString(), getEvent());
			}
			
		});*/
		
		form.add(new ApiAjaxMethod(form, "GetDiagnostic", references, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);
				//String pageMapName = getRequestCycle().getRequest().getRequestParameters().getPageMapName();
				//System.out.println("PAGE MAP = " + pageMapName);
				return errorManager.getErrorDiagnostic(arg);
			}
		});
		
		/*form.add(new ApiAjaxMethod(form, "GetErrorString", null, 1, bean) {	
			private static final long serialVersionUID = 2L;
			
			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);
				return errorManager.getErrorDescription(arg); 
			}
		});*/
		
		/*form.add(new ApiAjaxMethod(form, "GetLastError", null, 0, bean) {	
			private static final long serialVersionUID = 3L;
			
			protected String callMethod(List<String> argumentValues) {
				APIErrorManager errorManager = ((ScormTool)getApplication()).getErrorManager();
				return errorManager.getCurrentErrorCode(); 
			}
		});*/
				
				
		/*this.add(new AbstractAjaxBehavior()
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
		});*/
	}
	
	
	private String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";
		
		return argumentValues.get(0);
	}
	
	
}
