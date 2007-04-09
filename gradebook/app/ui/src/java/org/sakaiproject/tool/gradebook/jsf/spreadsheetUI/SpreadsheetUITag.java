/*******************************************************************************
 * Copyright (c) 2007 The Indiana University
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.jsf.spreadsheetUI;


import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.taglib.html.HtmlDataTableTag;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlLinkRendererBase;

import javax.faces.application.Application;
import javax.faces.component.UIData;
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;


public class SpreadsheetUITag extends HtmlDataTableTag
{
	private String colLock;
	private String rowLock;
	private String renderHandle;
	private String sortColumn;
	private String sortAscending;
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
		TagUtil.setString(component, "renderHandle", renderHandle);
		TagUtil.setString(component, "sortColumn", sortColumn);
		TagUtil.setString(component, "sortAscending", sortAscending);
		//if(value != null) {
		//	data.setValue(value);
		//}
		
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
	
	public String getRenderHandle() {
	    return renderHandle;
	}
	
	public void setRenderHandle(String renderHandle) {
	     this.renderHandle = renderHandle;
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
	
	public void setValue(java.lang.String value) {
		this.value = value;
	}
	
	public void setVar(java.lang.String _var) {
		this._var = _var;
	}
}