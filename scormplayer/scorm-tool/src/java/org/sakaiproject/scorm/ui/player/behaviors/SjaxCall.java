/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.behaviors;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.string.StringValue;

import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.wicket.util.Utils;

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
@Slf4j
public abstract class SjaxCall extends AjaxEventBehavior
{
	public static final String ARG_COMPONENT_ID = "arg";
	public static final String RESULT_COMPONENT_ID = "result";
	public static final String SCO_COMPONENT_ID = "scoId";
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
	 */
	public SjaxCall(String event, int numArgs)
	{
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
	 * @return String result code or value to browser
	 */
	protected String callMethod(final ScoBean scoBean, final AjaxRequestTarget target, Object... args)
	{
		String result = "";
		SCORM13API api = new SCORM13API()
		{
			@Override
			public INavigable getAgent()
			{
				return getNavigationAgent();
			}

			@Override
			public ScormApplicationService getApplicationService()
			{
				return SjaxCall.this.applicationService();
			}

			@Override
			public ScoBean getScoBean()
			{
				return scoBean;
			}

			@Override
			public ScormSequencingService getSequencingService()
			{
				return SjaxCall.this.sequencingService();
			}

			@Override
			public SessionBean getSessionBean()
			{
				return SjaxCall.this.getSessionBean();
			}

			@Override
			public Object getTarget()
			{
				return target;
			}
		};

		try
		{
			Class[] argClasses = new Class[numArgs];
			for (int i = 0; i < numArgs; i++)
			{
				argClasses[i] = String.class;
				args[i] = ((StringValue) args[i]).toString();
			}

			Method method = api.getClass().getMethod(event, argClasses);
			result = (String) method.invoke(api, args);
		}
		catch (Exception e)
		{
			log.error("Unable to execute api method through reflection -- method may not exist or some other exception may be being trapped", e);
		}

		return result;
	}

	@Override
	protected void onEvent(final AjaxRequestTarget target)
	{
		try
		{
			String callNumber = this.getComponent().getRequest().getRequestParameters().getParameterValue("callNumber").toString();
			final ScoBean scoBean = getSessionBean().getDisplayingSco();

			log.debug("Processing {}", callNumber);
			if (scoBean != null)
			{
				log.debug("Sco: {}", scoBean.getScoId());
			}
				
			Object[] args = new Object[numArgs];
			for (int i = 0; i < numArgs; i++)
			{
				String paramName = new StringBuilder("arg").append(i+1).toString();
				args[i] = this.getComponent().getRequest().getRequestParameters().getParameterValue(paramName);
			}

			String resultValue = callMethod(scoBean, target, args);
			String result = new StringBuffer().append("scormresult=").append(resultValue).append(";").toString();

			target.appendJavaScript(result);
		}
		catch (Exception e)
		{
			log.error("Caught a fatal exception during scorm api communication", e);
		}
	}

	public void prependJavascript(String js)
	{
		this.js = js;
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new AjaxCallListener().onAfter(getCallbackScript()));
	}

	public String getCallbackScript()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("\n")
				.append("var sjaxCallContainer = document.getElementById('")
				.append(event.toLowerCase())
				.append("call');\n")
				.append("var url = undefined;\n")
				.append("if (sjaxCallContainer != undefined) {\n")
				.append("    url = sjaxCallContainer.value; \n")
				.append("} \n")
				.append("var wcall=ScormSjax.sjaxCall(url, ");

		if (numArgs == 0)
		{
			buffer.append("'', ''");
		}
		else if (numArgs == 1)
		{
			buffer.append("arg1, ''");
		}
		else
		{
			buffer.append("arg1, arg2");
		}

		buffer.append(",null,null, null, '1|s'); \n return wcall;\n");
		return buffer.toString();
	}

	public CharSequence getCallUrl()
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
		}

		return Utils.generateUrl(this, getComponent(), lms().canUseRelativeUrls());
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// only add the event handler when the component is enabled.
		if (getComponent().isEnabled())
		{
			tag.put(event, getCallUrl());
		}
	}

	public String getJavascriptCode()
	{
		StringBuffer script = new StringBuffer().append(APIClass).append(".").append(getEvent()).append(" = function(");
		for (int i = 0; i < numArgs; i++)
		{
			script.append("arg").append(i + 1);
			if (i + 1 < numArgs)
			{
				script.append(", ");
			}
		}

		script.append(") { ").append(getCallbackScript()).append("};");
		return script.toString();
	}
}
