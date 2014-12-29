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
