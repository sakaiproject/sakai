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
package org.sakaiproject.scorm.ui.player.behaviors;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.markup.ComponentTag;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.player.decorators.SjaxCallDecorator;
import org.sakaiproject.scorm.ui.player.util.Utils;

/**
 * This class is right at the center of all the action. It provides a wrapper for Synchronous
 * Javascript and XML (SJAX) calls from the client's browser directly into the Java SCORM API. 
 * That is, by constructing an SjaxCall and passing it the name of an API method and the number
 * of arguments in that method, and then adding this behavior to a Wicket Component, you will 
 * expose a Javascript method with that same name to the browser. The associated javascript file
 * will automatically (thanks to Wicket) be added to the html page, and everything should fall
 * into place from there. 
 * 
 * @author jrenfro
 */
public abstract class SjaxCall extends AjaxEventBehavior {	
	public static final String ARG_COMPONENT_ID = "arg";
	public static final String RESULT_COMPONENT_ID = "result";
	public static final String SCO_COMPONENT_ID = "scoId";
		
	private static Log log = LogFactory.getLog(SjaxCall.class);
	private static final long serialVersionUID = 1L;
	protected static final String APIClass = "APIAdapter";
	
	protected String event;
	protected int numArgs;
		
	private String js = null;
	
	/**
	 * Constructor
	 * 
	 * @param event : the name of the function from the api
	 * @param numArgs : the number of arguments that this function takes
	 * @param sessionBean : the omnipresent SessionBean, where we store all the state information
	 */
	public SjaxCall(String event, int numArgs) {
		super(event);
		this.event = event;
		this.numArgs = numArgs;
	}

	protected abstract INavigable getNavigationAgent();
	
	protected abstract LearningManagementSystem lms();
	
	/**
	 * Since Wicket only injects Spring annotations for classes that extend Component, we can't use
	 * it inside the SjaxCall itself, therefore, we abstract the getter here to avoid having to 
	 * create a member variable that would then be serialized.
	 * 
	 * @return API Service dependency injected at the Component level
	 */
	protected abstract ScormApplicationService applicationService();
	
	/**
	 * Since Wicket only injects Spring annotations for classes that extend Component, we can't use
	 * it inside the SjaxCall itself, therefore, we abstract the getter here to avoid having to 
	 * create a member variable that would then be serialized.
	 * 
	 * @return Resource Service dependency injected at the Component level
	 */
	protected abstract ScormResourceService resourceService();
	
	/**
	 * Since Wicket only injects Spring annotations for classes that extend Component, we can't use
	 * it inside the SjaxCall itself, therefore, we abstract the getter here to avoid having to 
	 * create a member variable that would then be serialized.
	 * 
	 * @return Sequencing Service dependency injected at the Component level
	 */
	protected abstract ScormSequencingService sequencingService();
	

	/**
	 * Although the SessionBean doesn't need to be handled this way -- since it's serializable, it
	 * seems cleaner not to constantly be persisting it in every object
	 * 
	 * @return the omnipresent SessionBean, where we store all the state information
	 */
	protected abstract SessionBean getSessionBean();
	
	
	/**
	 * This is the core magic of the SjaxCall class. Since we want to map a Javascript call to 
	 * an API method, we use method reflection to pass that function name around -- i.e. this is the
	 * 'event' member variable. The ScoBean and AjaxRequestTarget parameters are only interesting 
	 * for the CommunicationPanel, where we have to override callMethod and do some extra processing.
	 * @param scoBean
	 * @param target
	 * @param args
	 * 
	 * 
	 * @return String result code or value to browser
	 */
	protected String callMethod(final ScoBean scoBean, final AjaxRequestTarget target, Object... args) {
		String result = "";
		
		SCORM13API api = new SCORM13API() {

			@Override
			public INavigable getAgent() {
				return getNavigationAgent();
			}

			@Override
			public ScormApplicationService getApplicationService() {
				return SjaxCall.this.applicationService();
			}

			@Override
			public ScoBean getScoBean() {
				return scoBean;
			}

			@Override
			public ScormSequencingService getSequencingService() {
				return SjaxCall.this.sequencingService();
			}

			@Override
			public SessionBean getSessionBean() {
				return SjaxCall.this.getSessionBean();
			}

			@Override
			public Object getTarget() {
				return target;
			}
			
		};
		
		try {
			Class[] argClasses = new Class[numArgs];
			
			for (int i=0;i<numArgs;i++) {
				argClasses[i] = String.class;
			}
			
			Method method = api.getClass().getMethod(event, argClasses);
			result = (String)method.invoke(api, args);
		} catch (Exception e) {
			log.error("Unable to execute api method through reflection -- method may not exist or some other exception may be being trapped", e);
		}
		return result;
	}
	
	@Override
	protected void onEvent(final AjaxRequestTarget target) {
		try {
			String callNumber = this.getComponent().getRequest().getParameter("callNumber");
			//String scoValue = this.getComponent().getRequest().getParameter("scoId");
			
			//final ScoBean scoBean = applicationService().produceScoBean(scoValue, getSessionBean());

			final ScoBean scoBean = getSessionBean().getDisplayingSco();
			
			if (log.isDebugEnabled()) {
				log.debug("Processing " + callNumber);
				if (scoBean != null)
					log.debug("Sco: " + scoBean.getScoId());
			}
				
			Object[] args = new Object[numArgs];
			
			for (int i=0;i<numArgs;i++) {
				String paramName = new StringBuilder("arg").append(i+1).toString();
				args[i] = this.getComponent().getRequest().getParameter(paramName);
			}
			
			String resultValue = callMethod(scoBean, target, args);
			
			String result = new StringBuffer().append("scormresult=")
				.append(resultValue).append(";").toString();

			target.appendJavascript(result);

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
		
		buffer.append("\n");
		buffer.append("var sjaxCallContainer = document.getElementById('").append(event.toLowerCase())
			.append("call');\n");
		buffer.append("var url = undefined;\n");
				//'").append(getCallUrl()).append("';\n");
				
		buffer.append("if (sjaxCallContainer != undefined) {\n")
			  .append("    url = sjaxCallContainer.value; \n")
			  .append("} \n");
		
		//buffer.append("var d = '").append(new Date().toString()).append("';");
		
		//buffer.append("alert(url);");
		
		//buffer.append("ScormSjax.sjaxCall(sco, '").append(getCallUrl())
		//	.append("', ");
		
		buffer.append("var wcall=ScormSjax.sjaxCall(url, ");
		
		if (numArgs == 0)
			buffer.append("'', ''");
		else if (numArgs == 1)
			buffer.append("arg1, ''");
		else
			buffer.append("arg1, arg2");
		
		buffer.append(",null,null, null, '1|s'); \n return wcall;\n");
		
		return buffer.toString();
		//return generateCallbackScript(buffer.toString());
	}
	
	public CharSequence getCallUrl()
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException(
					"Behavior must be bound to a component to create the URL");
		}
		
		
		/*String relativePagePath= getComponent().urlFor(this, rli).toString();
		
		String url = null;
		
		if (!lms().canUseRelativeUrls()) {
			WebRequest webRequest = (WebRequest)getComponent().getRequest();
			HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
			//url.append(servletRequest.getContextPath()).append("/");
			url = RequestUtils.toAbsolutePath(servletRequest.getRequestURL().toString(), relativePagePath);
		} else {
			url = relativePagePath;
		}*/
			
		return Utils.generateUrl(this, null, getComponent(), lms().canUseRelativeUrls());
	}
	
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		// only add the event handler when the component is enabled.
		if (getComponent().isEnabled())
		{
			tag.put(event, getCallUrl());
		}
	}
	
	/*@Override
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
	    
	    //script.append(" var elem = getElementById('comPanel'); \n ");
	    //script.append(" return elem.").append(getEvent().toLowerCase()).append("(); \n");
	    
	    
		script.append(getCallbackScript(false));
		script.append("};");
		
		response.renderJavascript(script.toString(), getEvent());
	    
	}*/
	
	
	public String getJavascriptCode() {
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

		script.append(getCallbackScript(false));
		script.append("};");
	
		return script.toString();
	}
	
}
