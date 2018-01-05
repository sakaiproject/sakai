/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.jsf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

public abstract class DivMessageRendererBase extends Renderer {
	public static String INNER_TAG = "div";

	public static Map severityToStyleAttr, severityToClassAttr;
	{
		severityToStyleAttr = new HashMap();
		severityToStyleAttr.put(FacesMessage.SEVERITY_INFO, "infoStyle");
		severityToStyleAttr.put(FacesMessage.SEVERITY_WARN, "warnStyle");
		severityToStyleAttr.put(FacesMessage.SEVERITY_ERROR, "errorStyle");
		severityToStyleAttr.put(FacesMessage.SEVERITY_FATAL, "fatalStyle");
		severityToClassAttr = new HashMap();
		severityToClassAttr.put(FacesMessage.SEVERITY_INFO, "infoClass");
		severityToClassAttr.put(FacesMessage.SEVERITY_WARN, "warnClass");
		severityToClassAttr.put(FacesMessage.SEVERITY_ERROR, "errorClass");
		severityToClassAttr.put(FacesMessage.SEVERITY_FATAL, "fatalClass");
	}

	public String getMessageStyle(UIComponent component, FacesMessage message) {
		String messageStyle = (String)component.getAttributes().get("style");
		FacesMessage.Severity severity = message.getSeverity();
		if (severity != null) {
			String severitySpecific = (String)component.getAttributes().get((String)severityToStyleAttr.get(severity));
			if ((severitySpecific != null) && (severitySpecific.length() > 0)) {
				messageStyle = severitySpecific;
			}
		}
		return messageStyle;
	}

	public String getMessageClass(UIComponent component, FacesMessage message) {
		String messageClass = (String)component.getAttributes().get("styleClass");
		FacesMessage.Severity severity = message.getSeverity();
		if (severity != null) {
			String severitySpecific = (String)component.getAttributes().get((String)severityToClassAttr.get(severity));
			if ((severitySpecific != null) && (severitySpecific.length() > 0)) {
				messageClass = severitySpecific;
			}
		}
		return messageClass;
	}

	public void renderMessage(FacesContext context, UIComponent component, FacesMessage message) throws IOException {
		ResponseWriter out = context.getResponseWriter();
		out.startElement(INNER_TAG, component);
		String msgClass = getMessageClass(component, message);
		if (msgClass != null) {
			out.writeAttribute("class", msgClass, "styleClass");
		}
		String msgStyle = getMessageStyle(component, message);
		if (msgStyle != null) {
			out.writeAttribute("style", msgStyle, "style");
		}

		// In this first implementation, all we write is the summary.
		out.writeText(message.getSummary(), null);

		out.endElement(INNER_TAG);
	}
}
