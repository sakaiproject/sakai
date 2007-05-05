package org.sakaiproject.scorm.client.utils;

import java.util.LinkedList;
import java.util.List;

import wicket.ResourceReference;
import wicket.Response;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.form.AjaxFormSubmitBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.util.string.JavascriptUtils;

public abstract class ApiAjaxMethod extends AjaxFormSubmitBehavior {
	private static final String APIClass = "API_1484_11";
	private static final long serialVersionUID = 1L;
	
	private Form form;
	private ResourceReference[] references;
	private int numArgs;
	private ApiAjaxBean bean;
	
	public ApiAjaxMethod(Form form, String event, ResourceReference[] references, 
			int numArgs, ApiAjaxBean bean) {
		super(form, event);
		this.form = form;
		this.references = references;
		this.numArgs = numArgs;
		this.bean = bean;
	}
	
	protected abstract String callMethod(List<String> argumentValues);
	
	
	@Override
	protected void onSubmit(AjaxRequestTarget target) {
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
	protected void onRenderHeadInitContribution(Response response)
	{
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
					.append(" = arg").append(i+1).append("\n");
			
			/*script.append("arg").append(i+1)
				.append(" = document.getElementById('")
				.append("arg").append(i+1).append("').value;\n");*/
		}
		
		script.append(getEventHandler())
			.append("};");
		
		JavascriptUtils.writeJavascript(response, script.toString(), getEvent());
	}

}
