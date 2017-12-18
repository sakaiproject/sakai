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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.sakaiproject.tool.gradebook.jsf.iterator.IteratorComponent;

/*
 * <gbx:dhtmlPopup popupId="#{scoreRowIndex}"
 *    columns="2"
 *    value="#{scoreRow.events}" var="eventRow"
 *    titleText="#{scoreRow.eventsLogTitle}"
 *    closeIconUrl="dhtmlpopup/dhtmlPopClose.gif"
 *    styleClass="dhtmlPopup" titleBarClass="dhtmlPopupTitleBar" closeClass="dhtmlPopupClose" dataRowClass="dhtmlPopupDataRow">
 *        <h:outputText value="#{eventRow.date}/>
 *        <h:outputText value="#{eventRow.description}/>
 * </gbx:dhtmlPopup>
 */

public class DhtmlPopupComponent extends IteratorComponent implements NamingContainer {
	public final static String COMPONENT_TYPE = "org.sakaiproject.tool.gradebook.jsf.dhtmlpopup";

	// These must agree with the JavaScript logic.
	public final static String POPUP_ID_DIV_PREFIX = "dhtmlPopup_";

	private String popupId = null;
	private Integer numberOfColumns = null;
	private String titleText = null;
	private String closeIconUrl = null;
	private String styleClass = null;
	private String titleBarClass = null;
	private String closeClass = null;
	private String dataRowClass = null;

	public void encodeBegin(FacesContext context) throws IOException {
		String popupId = getPopupId();
		String titleText = getTitleText();
		String closeIconUrl = getCloseIconUrl();

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("div", null);
		writer.writeAttribute("id", POPUP_ID_DIV_PREFIX + popupId, null);
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}
		writer.writeAttribute("style", "visibility:hidden;", null);
		startBareTable(writer);

		// The title bar is an inner table.
		writer.startElement("tr", null);
		writer.startElement("td", null);
		if (numberOfColumns != null) {
			writer.writeAttribute("colspan", numberOfColumns, "columns");
		}

		startBareTable(writer);
		writer.writeAttribute("width", "100%", null);
		writer.startElement("tr", null);
		writer.startElement("td", null);
		writer.writeAttribute("width", "100%", null);
		writer.startElement("div", null);
		if (titleBarClass != null) {
			writer.writeAttribute("class", titleBarClass, "titleBarClass");
		}
		writer.writeAttribute("onmouseover", "javascript:dhtmlPopupMouseover('" + popupId + "', event);", null);
		writer.writeAttribute("onmouseout", "javascript:dhtmlPopupMouseout(event);", null);
		if (titleText != null) {
			writer.writeText(titleText, "titleText");
		}
		writer.endElement("div");
		writer.endElement("td");

		writer.startElement("td", null);
		writer.startElement("div", null);
		if (closeClass != null) {
			writer.writeAttribute("class", closeClass, "closeClass");
		}
		writer.startElement("a", null);
		writer.writeAttribute("href", "#", null);
		writer.writeAttribute("title", "Close", null);
		writer.writeAttribute("onClick", "javascript:dhtmlPopupHide('" + popupId + "', event); return false;", null);
		if (closeIconUrl != null) {
			writer.startElement("img", null);
			writer.writeAttribute("src", closeIconUrl, "closeIconUrl");
			writer.writeAttribute("alt", "Close", null);
			writer.writeAttribute("border", "0", null);
			writer.endElement("img");
		}
		writer.endElement("a");
		writer.endElement("div");
		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("table");

		writer.endElement("td");
		writer.endElement("tr");
	}

	private void startBareTable(ResponseWriter writer) throws IOException {
		writer.startElement("table", null);
		writer.writeAttribute("border", "0", null);
		writer.writeAttribute("cellspacing", "0", null);
		writer.writeAttribute("cellpadding", "0", null);
	}

	public void encodeEnd(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("table");
		writer.endElement("div");
		writer.flush();
		return;
	}

	protected void renderRowChildren(FacesContext context) throws IOException {
		List children = getChildren();
		if (children.size() > 0) {
			ResponseWriter writer = context.getResponseWriter();
			writer.startElement("tr", null);
			if (dataRowClass != null) {
				writer.writeAttribute("class", dataRowClass, "dataRowClass");
			}
			for (Iterator iter = children.iterator(); iter.hasNext(); ) {
				writer.startElement("td", null);
				encodeRecursive(context, (UIComponent)iter.next());
				writer.endElement("td");
			}
			writer.endElement("tr");
		}
	}

	public String getPopupId() {
		return getFieldOrBinding(popupId, "popupId").toString();
	}
	public void setPopupId(String popupId) {
		this.popupId = popupId;
	}
	public Integer getNumberOfColumns() {
		return (Integer)getFieldOrBinding(numberOfColumns, "columns");
	}
	public void setNumberOfColumns(Integer numberOfColumns) {
		this.numberOfColumns = numberOfColumns ;
	}
	public String getTitleText() {
		return (String)getFieldOrBinding(titleText, "titleText");
	}
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	public String getCloseIconUrl() {
		return (String)getFieldOrBinding(closeIconUrl, "closeIconUrl");
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

	public Object saveState(FacesContext context) {
		Object values[] = new Object[9];
		values[0] = super.saveState(context);
		values[1] = popupId;
		values[2] = titleText;
		values[3] = numberOfColumns;
		values[4] = styleClass;
		values[5] = titleBarClass;
		values[6] = closeClass;
		values[7] = closeIconUrl;
		values[8] = dataRowClass;
		return values;
	}
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(context, values[0]);
		popupId = (String)values[1];
		titleText = (String)values[2];
		numberOfColumns = (Integer)values[3];
		styleClass = (String)values[4];
		titleBarClass = (String)values[5];
		closeClass = (String)values[6];
		closeIconUrl = (String)values[7];
		dataRowClass = (String)values[8];
	}
}
