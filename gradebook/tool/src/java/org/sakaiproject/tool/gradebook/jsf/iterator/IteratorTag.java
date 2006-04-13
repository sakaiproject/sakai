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

package org.sakaiproject.tool.gradebook.jsf.iterator;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

public class IteratorTag extends UIComponentTag {
	private String var;
	private String value;
	private String rowIndexVar;

	public void setVar(String var) {
		this.var = var;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setRowIndexVar(String rowIndexVar) {
		this.rowIndexVar = rowIndexVar;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);

		IteratorComponent iteratorComponent = (IteratorComponent)component;

		if (var != null) {
			iteratorComponent.setVar(var);
		}
		if (rowIndexVar != null) {
			iteratorComponent.setRowIndexVar(rowIndexVar);
		}

		Application application =  FacesContext.getCurrentInstance().getApplication();
		iteratorComponent.setValueBinding("value", application.createValueBinding(value));
	}

	public String getComponentType() {
		return IteratorComponent.COMPONENT_TYPE;
	}
	public String getRendererType() {
		return null;
	}
}
