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

package org.sakaiproject.tool.gradebook.jsf.flowstate;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlowStateTag extends UIComponentTag {
	private static final Log logger = LogFactory.getLog(FlowStateTag.class);

	private String bean;

	public void setBean(String bean) {
		this.bean = bean;
	}

	protected void setProperties(UIComponent component) {
		if (logger.isDebugEnabled()) logger.debug("setProperties " + bean);
		super.setProperties(component);

		FacesContext context = getFacesContext();

		if (bean != null) {
			if (UIComponentTag.isValueReference(bean)) {
				ValueBinding vb = context.getApplication().createValueBinding(bean);
				component.setValueBinding("bean", vb);
			} else {
				logger.error("Invalid expression " + bean);
			}
		}
	}

	public String getComponentType() {
		return "org.sakaiproject.tool.gradebook.FlowState";
	}

	public String getRendererType() {
		return null;
	}
}


