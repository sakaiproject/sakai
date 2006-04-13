/**********************************************************************************
*
* $Id: IteratorTag.java 2818 2005-10-21 22:08:57Z ray@media.berkeley.edu $
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
