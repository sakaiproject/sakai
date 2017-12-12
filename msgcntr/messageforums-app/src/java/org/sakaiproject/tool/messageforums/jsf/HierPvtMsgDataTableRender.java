/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/HierPvtMsgDataTableRender.java $
 * $Id: HierPvtMsgDataTableRender.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import com.sun.faces.renderkit.html_basic.HtmlBasicRenderer;
import com.sun.faces.util.Util;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;

/**
 * @author cwen
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@Slf4j
public class HierPvtMsgDataTableRender extends HtmlBasicRenderer {

	private static final String RESOURCE_PATH;

	private static final String BARIMG;

	private static final String CURSOR;

	static {
		RESOURCE_PATH = "/messageforums-tool";
		BARIMG = RESOURCE_PATH + "/" + "images/collapse.gif";
		CURSOR = "cursor:pointer";
	}

	public boolean getRendersChildren() {
		return true;
	}

	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {

		if ((context == null) || (component == null)) {
			throw new NullPointerException(
					Util
							.getExceptionMessageString(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
		}
		if (log.isTraceEnabled()) {
			log.trace("Begin encoding component " + component.getId());
		}

		// suppress rendering if "rendered" property on the component is
		// false.
		if (!component.isRendered()) {
			if (log.isTraceEnabled()) {
				log.trace("No encoding necessary " + component.getId()
						+ " since " + "rendered attribute is set to false ");
			}
			return;
		}
		UIData data = (UIData) component;
		data.setRowIndex(-1);

		// Render the beginning of the table
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("table", data);
		writeIdAttributeIfNecessary(context, writer, component);
		String styleClass = (String) data.getAttributes().get("styleClass");
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}
		Util.renderPassThruAttributes(writer, component,
				new String[] { "rows" });
		writer.writeText("\n", null);

		// Render the header facets (if any)
		UIComponent header = getFacet(data, "header");
		int headerFacets = getFacetCount(data, "header");
		String headerClass = (String) data.getAttributes().get("headerClass");
		if ((header != null) || (headerFacets > 0)) {
			writer.startElement("thead", data);
			writer.writeText("\n", null);
		}
		if (header != null) {
			writer.startElement("tr", header);
			writer.startElement("th", header);
			if (headerClass != null) {
				writer.writeAttribute("class", headerClass, "headerClass");
			}
			writer.writeAttribute("colspan", "" + getColumnCount(data), null);
			writer.writeAttribute("scope", "colgroup", null);
			encodeRecursive(context, header);
			writer.endElement("th");
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if (headerFacets > 0) {
			writer.startElement("tr", data);
			writer.writeText("\n", null);
			Iterator columns = getColumns(data);
			while (columns.hasNext()) {
				UIColumn column = (UIColumn) columns.next();
				writer.startElement("th", column);
				if (headerClass != null) {
					writer.writeAttribute("class", headerClass, "headerClass");
				}
				writer.writeAttribute("scope", "col", null);
				UIComponent facet = getFacet(column, "header");
				if (facet != null) {
					encodeRecursive(context, facet);
				}
				writer.endElement("th");
				writer.writeText("\n", null);
			}
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if ((header != null) || (headerFacets > 0)) {
			writer.endElement("thead");
			writer.writeText("\n", null);
		}

		// Render the footer facets (if any)
		UIComponent footer = getFacet(data, "footer");
		int footerFacets = getFacetCount(data, "footer");
		String footerClass = (String) data.getAttributes().get("footerClass");
		if ((footer != null) || (footerFacets > 0)) {
			writer.startElement("tfoot", data);
			writer.writeText("\n", null);
		}
		if (footer != null) {
			writer.startElement("tr", footer);
			writer.startElement("td", footer);
			if (footerClass != null) {
				writer.writeAttribute("class", footerClass, "footerClass");
			}
			writer.writeAttribute("colspan", "" + getColumnCount(data), null);
			encodeRecursive(context, footer);
			writer.endElement("td");
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if (footerFacets > 0) {
			writer.startElement("tr", data);
			writer.writeText("\n", null);
			Iterator columns = getColumns(data);
			while (columns.hasNext()) {
				UIColumn column = (UIColumn) columns.next();
				writer.startElement("td", column);
				if (footerClass != null) {
					writer.writeAttribute("class", footerClass, "footerClass");
				}
				UIComponent facet = getFacet(column, "footer");
				if (facet != null) {
					encodeRecursive(context, facet);
				}
				writer.endElement("td");
				writer.writeText("\n", null);
			}
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if ((footer != null) || (footerFacets > 0)) {
			writer.endElement("tfoot");
			writer.writeText("\n", null);
		}

	}

	public void encodeChildren(FacesContext context, UIComponent component)
			throws IOException {

		if ((context == null) || (component == null)) {
			throw new NullPointerException(
					Util
							.getExceptionMessageString(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
		}
		if (log.isTraceEnabled()) {
			log.trace("Begin encoding children " + component.getId());
		}
		if (!component.isRendered()) {
			if (log.isTraceEnabled()) {
				log.trace("No encoding necessary " + component.getId()
						+ " since " + "rendered attribute is set to false ");
			}
			return;
		}
		UIData data = (UIData) component;

		ValueBinding msgsBinding = component.getValueBinding("value");
		List msgBeanList = (List) msgsBinding.getValue(context);

		// Set up variables we will need
		String columnClasses[] = getColumnClasses(data);
		int columnStyle = 0;
		int columnStyles = columnClasses.length;
		String rowClasses[] = getRowClasses(data);
		int rowStyles = rowClasses.length;
		ResponseWriter writer = context.getResponseWriter();
		Iterator kids = null;
		Iterator grandkids = null;

		// Iterate over the rows of data that are provided
		int processed = 0;
		int rowIndex = data.getFirst() - 1;
		int rows = data.getRows();
		int rowStyle = 0;

		writer.startElement("tbody", component);
		writer.writeText("\n", null);
		int hideDivNo = 0;
		while (true) {
			//			PrivateMessageDecoratedBean dmb = null;
			//			if(msgBeanList !=null && msgBeanList.size()>(rowIndex+1) &&
			// rowIndex>=-1)
			//			{
			//				dmb = (PrivateMessageDecoratedBean)msgBeanList.get(rowIndex+1);
			//			}
			//			boolean hasChildBoolean = false;
			//			if(dmb != null)
			//			{
			//				for(int i=0; i<msgBeanList.size(); i++)
			//				{
			//					PrivateMessageDecoratedBean tempDmb =
			// (PrivateMessageDecoratedBean)msgBeanList.get(i);
			//					if(tempDmb.getUiInReply() != null &&
			// tempDmb.getUiInReply().getId().equals(dmb.getMsg().getId()))
			//					{
			//						hasChildBoolean = true;
			//						break;
			//					}
			//				}
			//			}
			// Have we displayed the requested number of rows?
			if ((rows > 0) && (++processed > rows)) {
				break;
			}
			// Select the current row
			data.setRowIndex(++rowIndex);
			if (!data.isRowAvailable()) {
				break; // Scrolled past the last row
			}
			
			PrivateMessageDecoratedBean dmb = null;
			dmb = (PrivateMessageDecoratedBean) data.getRowData();
			boolean hasChildBoolean = false;
			if(dmb != null)
			{
				// if dmb has depth = 0, check for children
				if (dmb.getDepth() == 0)
				{
					// first, get the index of the dmb
					int index = -1;

					for(int i=0; i<msgBeanList.size(); i++)
					{
						PrivateMessageDecoratedBean tempDmb = (PrivateMessageDecoratedBean)msgBeanList.get(i);
						if (dmb.getMsg().getId().equals(tempDmb.getMsg().getId()))
						{
							index = i;
							break;
						}
					}
					if (index < (msgBeanList.size() - 1) && index >= 0)
					{
						PrivateMessageDecoratedBean nextDmb = (PrivateMessageDecoratedBean) msgBeanList.get(index+1);

						if(nextDmb.getDepth() > 0)
						{
							hasChildBoolean = true;
						}
					}
				}
			}

			if(dmb != null && dmb.getDepth() > 0)
			{
				writer.write("<tr style=\"display:none\" id=\"_id_" + new
						Integer(hideDivNo).toString() + "__hide_division_" + "\">");
			}
			else
			{
				writer.write("<tr>");
			}

			if (rowStyles > 0) {
				writer.writeAttribute("class", rowClasses[rowStyle++],
						"rowClasses");
				if (rowStyle >= rowStyles) {
					rowStyle = 0;
				}
			}
			writer.writeText("\n", null);

			// Iterate over the child UIColumn components for each row
			columnStyle = 0;
			kids = getColumns(data);
			while (kids.hasNext()) {

				// Identify the next renderable column
				UIColumn column = (UIColumn) kids.next();

				// Render the beginning of this cell
				writer.startElement("td", column);
				if (columnStyles > 0) {
					writer.writeAttribute("class",
							columnClasses[columnStyle++], "columnClasses");
					if (columnStyle >= columnStyles) {
						columnStyle = 0;
					}
				}

				if(dmb != null && dmb.getDepth() > 0)
				{
					if(column.getId().endsWith("_msg_subject"))
					{
						StringBuilder indent = new StringBuilder();
						int indentInt = dmb.getDepth() * 4;
						for(int i=0; i<indentInt; i++)
						{
							indent.append("&nbsp;");
						}
						writer.write(indent.toString());
					}
				}
				else
				{
					if(column.getId().endsWith("_msg_subject"))
					{
						if(hasChildBoolean && dmb.getDepth()==0)
						{
							writer.write(" <img src=\"" + BARIMG + "\" style=\"" + CURSOR
									+ "\" id=\"_id_" + Integer.valueOf(hideDivNo).toString() +
									"__img_hide_division_\"" +
							" onclick=\"");
							int childNo = getTotalChildNo(dmb, msgBeanList);
							String hideTr = "";
							for(int i=0; i<childNo; i++)
							{
								hideTr += "javascript:showHideDiv('_id_" + (hideDivNo+i) + "', '" + RESOURCE_PATH +
								"');";
							}
							writer.write(hideTr);
							writer.write("\" />");
						}
					}
				}
				// Render the contents of this cell by iterating over
				// the kids of our kids
				grandkids = getChildren(column);
				while (grandkids.hasNext()) {
					encodeRecursive(context, (UIComponent) grandkids.next());
				}

				// Render the ending of this cell
				writer.endElement("td");
				writer.writeText("\n", null);

			}

			// Render the ending of this row
			writer.endElement("tr");
			writer.writeText("\n", null);
			if(dmb !=null && dmb.getDepth()>0)
			{
////
				/*ValueBinding expandedBinding =
					component.getValueBinding("expanded");
				String expanded = "";
				if(expandedBinding != null)
					expanded = (String)expandedBinding.getValue(context);
				
				if(expanded.equalsIgnoreCase("true"))
				{*/
					writer.write("<script type=\"text/javascript\">");
					writer.write(" showHideDiv('_id_" + hideDivNo +
							"', '" + RESOURCE_PATH + "');");
					writer.write("</script>");
//////				}
				
				hideDivNo++;
			}
		}
		writer.endElement("tbody");
		writer.writeText("\n", null);

		// Clean up after ourselves
		data.setRowIndex(-1);
		if (log.isTraceEnabled()) {
			log.trace("End encoding children " + component.getId());
		}
	}

	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

		if ((context == null) || (component == null)) {
			throw new NullPointerException(
					Util
							.getExceptionMessageString(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
		}
		if (!component.isRendered()) {
			if (log.isTraceEnabled()) {
				log.trace("No encoding necessary " + component.getId()
						+ " since " + "rendered attribute is set to false ");
			}
			return;
		}
		UIData data = (UIData) component;
		data.setRowIndex(-1);
		ResponseWriter writer = context.getResponseWriter();

		// Render the ending of this table
		writer.endElement("table");
		writer.writeText("\n", null);
		if (log.isTraceEnabled()) {
			log.trace("End encoding component " + component.getId());
		}

	}

	private int getTotalChildNo(PrivateMessageDecoratedBean dmb, List beanList) 
	{
		MessageForumsMessageManager messageManager = (MessageForumsMessageManager) ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
		List allChild = new ArrayList();
		messageManager.getChildMsgs(dmb.getMsg().getId(), allChild); 
		int no = 0;

		for(int i=0; i<beanList.size(); i++)
		{
			PrivateMessageDecoratedBean thisBean = (PrivateMessageDecoratedBean)beanList.get(i);
			for(int j=0; j<allChild.size(); j++)
			{
				Message child = (Message)allChild.get(j);
				if(thisBean.getMsg().getId().equals(child.getId()))
				{
					no++;
					break;
				}
			}
		}
		return no;
	}

	// --------------------------------------------------------- Private Methods

	/**
	 * <p>
	 * Return an array of stylesheet classes to be applied to each column in the
	 * table in the order specified. Every column may or may not have a
	 * stylesheet.
	 * </p>
	 * 
	 * @param data
	 *            {@link UIData}component being rendered
	 */
	private String[] getColumnClasses(UIData data) {

		String values = (String) data.getAttributes().get("columnClasses");
		if (values == null) {
			return (new String[0]);
		}
		values = values.trim();
		ArrayList list = new ArrayList();
		while (values.length() > 0) {
			int comma = values.indexOf(",");
			if (comma >= 0) {
				list.add(values.substring(0, comma).trim());
				values = values.substring(comma + 1);
			} else {
				list.add(values.trim());
				values = "";
			}
		}
		String results[] = new String[list.size()];
		return ((String[]) list.toArray(results));

	}

	/**
	 * <p>
	 * Return the number of child <code>UIColumn</code> components that are
	 * nested in the specified {@link UIData}.
	 * </p>
	 * 
	 * @param data
	 *            {@link UIData}component being analyzed
	 */
	private int getColumnCount(UIData data) {

		int columns = 0;
		Iterator kids = getColumns(data);
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			columns++;
		}
		return (columns);

	}

	/**
	 * <p>
	 * Return an Iterator over the <code>UIColumn</code> children of the
	 * specified <code>UIData</code> that have a <code>rendered</code>
	 * property of <code>true</code>.
	 * </p>
	 * 
	 * @param data
	 *            <code>UIData</code> for which to extract children
	 */
	private Iterator getColumns(UIData data) {

		List results = new ArrayList();
		Iterator kids = data.getChildren().iterator();
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			if ((kid instanceof UIColumn) && kid.isRendered()) {
				results.add(kid);
			}
		}
		return (results.iterator());

	}

	/**
	 * <p>
	 * Return the number of child <code>UIColumn</code> components nested in
	 * the specified <code>UIData</code> that have a facet with the specified
	 * name.
	 * </p>
	 * 
	 * @param data
	 *            <code>UIData</code> component being analyzed
	 * @param name
	 *            Name of the facet being analyzed
	 */
	private int getFacetCount(UIData data, String name) {

		int n = 0;
		Iterator kids = getColumns(data);
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			if (getFacet(kid, name) != null) {
				n++;
			}
		}
		return (n);

	}

	/**
	 * <p>
	 * Return an array of stylesheet classes to be applied to each row in the
	 * table, in the order specified. Every row may or may not have a
	 * stylesheet.
	 * </p>
	 * 
	 * @param data
	 *            {@link UIData}component being rendered
	 */
	private String[] getRowClasses(UIData data) {

		String values = (String) data.getAttributes().get("rowClasses");
		if (values == null) {
			return (new String[0]);
		}
		values = values.trim();
		ArrayList list = new ArrayList();
		while (values.length() > 0) {
			int comma = values.indexOf(",");
			if (comma >= 0) {
				list.add(values.substring(0, comma).trim());
				values = values.substring(comma + 1);
			} else {
				list.add(values.trim());
				values = "";
			}
		}
		String results[] = new String[list.size()];
		return ((String[]) list.toArray(results));

	}

	private void renderThread(ResponseWriter writer, UIData data, int rows,
			Integer processed, int rowIndex, int rowStyles, int rowStyle,
			int columnStyle, int columnStyles, String columnClasses[],
			String rowClasses[], FacesContext context, List msgs,
			Message currentMsg) {
		Iterator kids = null;
		Iterator grandkids = null;

		for (int i = 0; i < msgs.size(); i++) {
			Message thisMsg = (Message) msgs.get(i);
			if (thisMsg.getId().equals(currentMsg.getId())) {
				msgs.remove(i);
				break;
			}
		}

		try {
			int processedInt = processed.intValue();
			if ((rows > 0) && (++processedInt > rows)) {
				processed = Integer.valueOf(processedInt);
				return;
			}
			data.setRowIndex(++rowIndex);
			if (!data.isRowAvailable()) {
				return;
			}

			writer.startElement("tr", data);
			if (rowStyles > 0) {
				writer.writeAttribute("class", rowClasses[rowStyle++],
						"rowClasses");
				if (rowStyle >= rowStyles) {
					rowStyle = 0;
				}
			}
			writer.writeText("\n", null);

			columnStyle = 0;
			kids = getColumns(data);
			while (kids.hasNext()) {
				UIColumn column = (UIColumn) kids.next();

				writer.startElement("td", column);
				if (columnStyles > 0) {
					writer.writeAttribute("class",
							columnClasses[columnStyle++], "columnClasses");
					if (columnStyle >= columnStyles) {
						columnStyle = 0;
					}
				}

				grandkids = getChildren(column);
				while (grandkids.hasNext()) {
					encodeRecursive(context, (UIComponent) grandkids.next());
				}

				writer.endElement("td");
				writer.writeText("\n", null);

			}

			writer.endElement("tr");
			writer.writeText("\n", null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		MessageForumsMessageManager messageManager = (MessageForumsMessageManager) ComponentManager
				.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
		List replyMsgs = messageManager.getFirstLevelChildMsgs(currentMsg
				.getId());
		if (replyMsgs != null) {
			for (int i = 0; i < replyMsgs.size(); i++) {
				Message childM = (Message) replyMsgs.get(i);
				for (int j = 0; j < msgs.size(); j++) {
					Message remainMsg = (Message) msgs.get(j);
					if (childM.getId().equals(remainMsg.getId())) {
						renderThread(writer, data, rows, processed, rowIndex,
								rowStyles, rowStyle, columnStyle, columnStyles,
								columnClasses, rowClasses, context, msgs,
								remainMsg);
					}
				}
			}
		}
	}
}
