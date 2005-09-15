/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.section.jsf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for custom message renderers.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public abstract class DivMessageRendererBase extends Renderer {
	private static final Log logger = LogFactory.getLog(DivMessageRendererBase.class);

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


