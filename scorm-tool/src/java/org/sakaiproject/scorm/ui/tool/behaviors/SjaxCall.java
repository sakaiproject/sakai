/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.tool.behaviors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.ui.tool.decorators.SjaxCallDecorator;

public abstract class SjaxCall extends AjaxEventBehavior {	
	public static final String ARG_COMPONENT_ID = "arg";
	public static final String RESULT_COMPONENT_ID = "result";
	public static final String SCO_COMPONENT_ID = "scoId";
	
	private static final ResourceReference SJAX = new JavascriptResourceReference(SjaxCall.class, "res/scorm-sjax.js");
	
	private static Log log = LogFactory.getLog(SjaxCall.class);
	private static final long serialVersionUID = 1L;
	protected static final String APIClass = "APIAdapter";
	
	protected int numArgs;
	
	private String js = null;
	
	public SjaxCall(String event, int numArgs) {
		super(event);
		this.numArgs = numArgs;
	}

	protected abstract String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target);
	
	@Override
	protected void onEvent(AjaxRequestTarget target) {
		try {
			String callNumber = this.getComponent().getRequest().getParameter("callNumber");
			String scoValue = this.getComponent().getRequest().getParameter("scoId");
			String arg1 = this.getComponent().getRequest().getParameter("arg1");
			String arg2 = this.getComponent().getRequest().getParameter("arg2");
			
			if (log.isDebugEnabled())
				log.debug("Processing " + scoValue + " (" + arg1 + ", " + arg2 + ") as " + callNumber);
			
			String resultValue = callMethod(scoValue, arg1, arg2, target);
			
			StringBuffer resultBuffer = new StringBuffer();
			resultBuffer.append("api_result[").append(callNumber).append("] = ");
			resultBuffer.append("'").append(resultValue).append("'");
			resultBuffer.append(";");
			
			
				
			if (log.isDebugEnabled())
				log.debug("Result is " + resultBuffer.toString());
			
			target.appendJavascript(resultBuffer.toString());
			//target.appendJavascript("alert(api_result);");
			//FormComponent resultComponent = (FormComponent)form.get(RESULT_COMPONENT_ID);
			//bean.setResult(resultValue);
			//target.addComponent(resultComponent);
		} catch (Exception e) {
			log.error("Caught a fatal exception during scorm api communication", e);
		}
		
	}
	
	public void prependJavascript(String js) {
		this.js = js;
	}
	
	@Override
	protected CharSequence getPreconditionScript()
	{
		return null;
	}

	
	@Override
	protected CharSequence getFailureScript()
	{
		return null;
	}

	@Override
	protected CharSequence getSuccessScript()
	{
		return null;
	}
	
	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return new SjaxCallDecorator(js);
	}
	
	@Override
	protected CharSequence getCallbackScript(boolean onlyTargetActivePage)
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("sjaxCall(sco, '").append(getCallUrl()).append("', ");
		
		if (numArgs == 0)
			buffer.append("'', ''");
		else if (numArgs == 1)
			buffer.append("arg1, ''");
		else
			buffer.append("arg1, arg2");
		
		
		return generateCallbackScript(buffer.toString());
	}
	
	public CharSequence getCallUrl()
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException(
					"Behavior must be bound to a component to create the URL");
		}
		
		final RequestListenerInterface rli;
		
		rli = IBehaviorListener.INTERFACE;
		
		WebRequest webRequest = (WebRequest)getComponent().getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
		String toolUrl = servletRequest.getContextPath();
		
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/");
		url.append(getComponent().urlFor(this, rli));
		
		return url;
	}
	
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		// Do nothing -- we don't want to add the javascript to the component.
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
	    super.renderHead(response);

	    response.renderJavascriptReference(SJAX);
	    
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
	    
		/*for (int i=0;i<numArgs;i++) {
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
		}*/
	    
	    //FormComponent scoComponent = (FormComponent)form.get(SCO_COMPONENT_ID);
		
	    //script.append("var scoId = document.getElementById('")
	    //	.append(scoComponent.getMarkupId()).append("').value;\n");
	    
		script.append(getCallbackScript(false))
			.append("};");
		
		response.renderJavascript(script.toString(), getEvent());
	    
	}
	
	

}
