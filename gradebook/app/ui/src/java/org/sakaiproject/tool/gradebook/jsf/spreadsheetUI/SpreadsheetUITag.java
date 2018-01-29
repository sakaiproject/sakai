/*******************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.jsf.spreadsheetUI;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;

import org.apache.myfaces.taglib.html.HtmlDataTableTag;
import org.apache.myfaces.component.html.ext.HtmlDataTable;

import org.sakaiproject.jsf.util.TagUtil;

public class SpreadsheetUITag extends HtmlDataTableTag
{
	private String colLock;
	private String rowLock;
	private String sortColumn;
	private String sortAscending;
	private String initialHeight;
	private java.lang.String value;
	private java.lang.String _var;
	
	
	public String getRendererType() { return "SpreadsheetUIRenderer"; }
	public String getComponentType() { return HtmlDataTable.COMPONENT_TYPE; }
  
	protected void setProperties(UIComponent component)
	{

		super.setProperties(component);

		UIData data = null;
		data = (UIData)component;
				
		FacesContext context = getFacesContext();
		TagUtil.setString(component, "colLock", colLock);
		TagUtil.setString(component, "rowLock", rowLock);
		TagUtil.setString(component, "sortColumn", sortColumn);
		TagUtil.setString(component, "sortAscending", sortAscending);
		TagUtil.setString(component, "initialHeight", initialHeight);
		
		TagUtil.setString(component, "value", value);
		if (_var != null) {
			data.setVar(_var);
		}
		Application application =  FacesContext.getCurrentInstance().getApplication();
		data.setValueBinding("value", application.createValueBinding(value));
	}

	public String getColLock() {
	    return colLock;
	}
	
	public void setColLock(String colLock) {
	     this.colLock = colLock;
	}
	
	public String getRowLock() {
	    return rowLock;
	}
	
	public void setRowLock(String rowLock) {
	     this.rowLock = rowLock;
	}
	
	public String getSortColumn() {
	    return sortColumn;
	}
	
	public void setSortColumn(String sortColumn) {
	     this.sortColumn = sortColumn;
	}
	
	public String getSortAscending() {
	    return sortAscending;
	}
	
	public void setSortAscending(String sortAscending) {
	     this.sortAscending = sortAscending;
	}
	
	public String getInitialHeight(){
		return initialHeight;
	}
	
	public void setInitialHeight(String initialHeight){
		this.initialHeight = initialHeight;
	}
	
	public void setValue(java.lang.String value) {
		this.value = value;
	}
	
	public void setVar(java.lang.String _var) {
		this._var = _var;
	}
}
