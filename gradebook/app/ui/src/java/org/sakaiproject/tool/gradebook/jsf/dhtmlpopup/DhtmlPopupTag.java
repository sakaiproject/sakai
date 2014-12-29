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

package org.sakaiproject.tool.gradebook.jsf.dhtmlpopup;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.sakaiproject.tool.gradebook.jsf.iterator.IteratorTag;

public class DhtmlPopupTag extends IteratorTag {
	private String popupId = null;
	private String columns = null;
	private String titleText = null;
	private String styleClass = null;
	private String titleBarClass = null;
	private String closeClass = null;
	private String dataRowClass = null;
	private String closeIconUrl = null;

	public void setPopupId(String popupId) {
		this.popupId = popupId;
	}
	public void setColumns(String columns) {
		this.columns = columns;
	}
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	public void setCloseIconUrl(String closeIconUrl) {
		this.closeIconUrl = closeIconUrl;
	}
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	public void setTitleBarClass(String titleBarClass) {
		this.titleBarClass = titleBarClass;
	}
	public void setCloseClass(String closeClass) {
		this.closeClass = closeClass;
	}
	public void setDataRowClass(String dataRowClass) {
		this.dataRowClass = dataRowClass;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);

		DhtmlPopupComponent dhtmlPopupComponent = (DhtmlPopupComponent)component;
		Application application =  FacesContext.getCurrentInstance().getApplication();

		if (popupId != null) {
			if (isValueReference(popupId)) {
				dhtmlPopupComponent.setValueBinding("popupId", application.createValueBinding(popupId));
			} else {
				dhtmlPopupComponent.setPopupId(popupId);
			}
		}
		if (columns != null) {
			if (isValueReference(columns)) {
				dhtmlPopupComponent.setValueBinding("columns", application.createValueBinding(columns));
			} else {
				dhtmlPopupComponent.setNumberOfColumns(new Integer(columns));
			}
		}
		if (titleText != null) {
			if (isValueReference(titleText)) {
				dhtmlPopupComponent.setValueBinding("titleText", application.createValueBinding(titleText));
			} else {
				dhtmlPopupComponent.setTitleText(titleText);
			}
		}
		if (closeIconUrl != null) {
			if (isValueReference(closeIconUrl)) {
				dhtmlPopupComponent.setValueBinding("closeIconUrl", application.createValueBinding(closeIconUrl));
			} else {
				dhtmlPopupComponent.setCloseIconUrl(closeIconUrl);
			}
		}
		if (styleClass != null) {
			dhtmlPopupComponent.setStyleClass(styleClass);
		}
		if (titleBarClass != null) {
			dhtmlPopupComponent.setTitleBarClass(titleBarClass);
		}
		if (closeClass != null) {
			dhtmlPopupComponent.setCloseClass(closeClass);
		}
		if (dataRowClass != null) {
			dhtmlPopupComponent.setDataRowClass(dataRowClass);
		}
	}

	public String getComponentType() {
		return DhtmlPopupComponent.COMPONENT_TYPE;
	}
}
