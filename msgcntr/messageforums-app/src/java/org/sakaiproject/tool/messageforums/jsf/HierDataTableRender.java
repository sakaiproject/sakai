/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/HierDataTableRender.java $
 * $Id: HierDataTableRender.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;

/**
 * @author cwen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
@Slf4j
public class HierDataTableRender extends HtmlBasicRenderer 
{

	private static final String RESOURCE_PATH = "/messageforums-tool";
	private static final String BARIMG = RESOURCE_PATH + "/" + "images/collapse.gif";
	private static final String EXPIMG = RESOURCE_PATH + "/" + "images/expand.gif";
	private static final String CURSOR = "cursor:pointer";


	private class RenderData {
		public List<UIColumn> uiColumns = new ArrayList<UIColumn>();
		public int facetCountHeaders = 0;
		public int facetCountFooters = 0;
	}

	public boolean getRendersChildren() {
		return true;
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException(Util.getExceptionMessageString(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
		}
		if (log.isTraceEnabled()) {
			log.trace("Begin encoding component " + component.getId());
		}

		// suppress rendering if "rendered" property on the component is false.
		if (!component.isRendered()) {
			if (log.isTraceEnabled()) {
				log.trace("No encoding necessary " + component.getId() + " since " + "rendered attribute is set to false ");
			}
			return;
		}
		// prepare the data for processing
		UIData data = (UIData) component;
		RenderData theData = organizeTheKids(data);
		data.setRowIndex(-1);
		// Render the beginning of the table
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("table", data);
		writeIdAttributeIfNecessary(context, writer, component);
		String noArrows = (String) data.getAttributes().get("noarrows");
		if (noArrows == null) {
			noArrows = "";
		}
		String styleClass = (String) data.getAttributes().get("styleClass");
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}
		Util.renderPassThruAttributes(writer, component, new String[] { "rows" });
		writer.writeText("\n", null);
		// Render the header facets (if any)
		UIComponent header = getFacet(data, "header");
		String headerClass = (String) data.getAttributes().get("headerClass");
		if ((header != null) || (theData.facetCountHeaders > 0)) {
			writer.startElement("thead", data);
			writer.writeText("\n", null);
		}
		if (header != null) {
			writer.startElement("tr", header);
			writer.startElement("th", header);
			if (headerClass != null) {
				writer.writeAttribute("class", headerClass, "headerClass");
			}
			writer.writeAttribute("colspan", theData.uiColumns.size(), null);
			writer.writeAttribute("scope", "colgroup", null);
			encodeRecursive(context, header);
			writer.endElement("th");
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if (theData.facetCountHeaders > 0) {
			writer.startElement("tr", data);
			writer.writeText("\n", null);
			Iterator columns = theData.uiColumns.iterator();
			UIColumn oldColumn = null;
			while (columns.hasNext()) {
				UIColumn column = (UIColumn) columns.next();
				// write column for arrows... only if last column did not specify arrows
				if (column.getId().endsWith("_msg_subject") && !oldColumn.getId().endsWith("_toggle") && !"true".equals(noArrows)) {
					writer.startElement("th", null);
					writer.writeAttribute("scope", "col", null);
					writer.endElement("th");
					writer.writeText("\n", null);
				}
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
				oldColumn = column;
			}
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if ((header != null) || (theData.facetCountHeaders > 0)) {
			writer.endElement("thead");
			writer.writeText("\n", null);
		}
		// Render the footer facets (if any)
		UIComponent footer = getFacet(data, "footer");
		String footerClass = (String) data.getAttributes().get("footerClass");
		if ((footer != null) || (theData.facetCountFooters > 0)) {
			writer.startElement("tfoot", data);
			writer.writeText("\n", null);
		}
		if (footer != null) {
			writer.startElement("tr", footer);
			writer.startElement("td", footer);
			if (footerClass != null) {
				writer.writeAttribute("class", footerClass, "footerClass");
			}
			writer.writeAttribute("colspan", theData.uiColumns.size(), null);
			encodeRecursive(context, footer);
			writer.endElement("td");
			writer.endElement("tr");
			writer.writeText("\n", null);
		}
		if (theData.facetCountFooters > 0) {
			writer.startElement("tr", data);
			writer.writeText("\n", null);
			Iterator columns = theData.uiColumns.iterator();
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
		if ((footer != null) || (theData.facetCountFooters > 0)) {
			writer.endElement("tfoot");
			writer.writeText("\n", null);
		}
	}

	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException(Util.getExceptionMessageString(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
		}
		if (log.isTraceEnabled()) {
			log.trace("Begin encoding children " + component.getId());
		}
		if (!component.isRendered()) {
			if (log.isTraceEnabled()) {
				log.trace("No encoding necessary " + component.getId() + " since " + "rendered attribute is set to false ");
			}
			return;
		}
		UIData data = (UIData) component;
		RenderData theData = organizeTheKids(data);
		String columnClasses[] = getColumnClasses(data);
		String rowClasses[] = getRowClasses(data);
		Map<Long, List<Long>> msgChildren = new HashMap<Long, List<Long>>();

		ResponseWriter writer = context.getResponseWriter();

		int currRowClass = 0;
		boolean noArrows = "true".equals(data.getAttributes().get("noarrows"));
		ValueBinding expandedBinding = component.getValueBinding("expanded");
		boolean expanded = (expandedBinding != null && "true".equalsIgnoreCase((String) expandedBinding.getValue(context)));
		// these variables will be used to track progress in the loops
		Message currentThread = null;
		boolean displayToggle = false;
		boolean checkExpanded = false;

		writer.startElement("tbody", component);
		writer.writeText("\n", null);

		for (data.setRowIndex(data.getFirst()); data.isRowAvailable(); data.setRowIndex(data.getRowIndex() + 1)) {
			DiscussionMessageBean dmb = (DiscussionMessageBean) data.getRowData();

//			// if this row has been deleted... skip it!
//			if (dmb.getDeleted()) {
//				continue;
//			}

			// walk up the messages to get the parent "thread"
			Message tmpMsg = dmb.getMessage();
			while (tmpMsg.getInReplyTo() != null) {
				tmpMsg = tmpMsg.getInReplyTo();
			}

			checkExpanded = false;

			writer.startElement("tr", data);

			boolean display_moveCheckbox  = false;
			// if this row should be hidden initially, setup those styles/classes
			if (currentThread == null || !tmpMsg.getId().equals(currentThread.getId())) {
				writer.writeAttribute("class", "hierItemBlock", null);
				currentThread = tmpMsg;
				displayToggle = !noArrows && dmb.getChildCount() > 0;
				display_moveCheckbox  = true;
				dmb.setDepth(0);
			} else if (!noArrows) {
				writer.writeAttribute("style", "display: none", null);
				writer.writeAttribute("id", "_id_" + dmb.getMessage().getId() + "__hide_division_", null);
				checkExpanded = true;
				display_moveCheckbox  = false;
			}

			if (!noArrows && dmb.getMessage().getInReplyTo() != null) {
				List<Long> tmpMsgChildren = msgChildren.get(tmpMsg.getId());
				if (tmpMsgChildren != null) tmpMsgChildren.add(dmb.getMessage().getId());
			}

			if (rowClasses.length > 0) {
				writer.writeAttribute("class", rowClasses[currRowClass++], "rowClasses");
				if (currRowClass >= rowClasses.length)
					currRowClass = 0;
			}
			writer.writeText("\n", null);

			// now process each of the columns
			int currColumnClass = 0;
			Iterator columns = theData.uiColumns.iterator();
			while (columns.hasNext()) {
				UIColumn column = (UIColumn) columns.next();

				writer.startElement("td", null);
				if (columnClasses.length > 0) {
					writer.writeAttribute("class", columnClasses[currColumnClass++], "columnClasses");
					if (currColumnClass >= columnClasses.length) currColumnClass = 0;
				}

				// if hierItemBlock
				if ((display_moveCheckbox) && (column.getId().endsWith("_checkbox")) && 
						(dmb.getRevise()) && (!dmb.getDeleted())) {
				writer.startElement("input", null);
                                writer.writeAttribute("id", "moveCheckbox", null);
                                writer.writeAttribute("type", "checkbox", null);
                                writer.writeAttribute("name", "moveCheckbox", null);
                                writer.writeAttribute("onclick", "enableDisableMoveThreadLink();", null);
                                writer.writeAttribute("value", dmb.getMessage().getId(), null);
                                writer.endElement("input");
				writer.endElement("td");
				continue;
				}
				else {
					if (column.getId().endsWith("_checkbox")) {
						writer.endElement("td");
						continue;
					}
				}

				if (displayToggle) {
					// write the toggle td if necessary
					if (dmb.getChildCount() > 0) {

						writer.startElement("img", null);
						writer.writeAttribute("alt", "", null);
						writer.writeAttribute("src", BARIMG, null);
						writer.writeAttribute("style", CURSOR, null);
						writer.writeAttribute("id", "_id_" + dmb.getMessage().getId() + "__img_hide_division_", null);

						writer.writeAttribute("onclick", "displayChildren('" + dmb.getMessage().getId() + "'); " +
								"mySetMainFrameHeight('Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId().replace("-", "x") + "');" +
								"if (msgExpanded['" + dmb.getMessage().getId() + "']) { this.src='" + BARIMG + "'; } else { this.src='" + EXPIMG + "'; }" +
								"msgExpanded['" + dmb.getMessage().getId() + "'] = !msgExpanded['" + dmb.getMessage().getId() + "'];", null);

						msgChildren.put(dmb.getMessage().getId(), new ArrayList<Long>());
					}

					displayToggle = false;

					if (column.getId().endsWith("_toggle")) {
						writer.endElement("td");
						continue;
					}
				}

				if (column.getId().endsWith("_msg_subject")) {
					writer.writeAttribute("style", "padding-left: " + dmb.getDepth() + "em;", "style");
				}

				// Render the contents of this cell by iterating over the kids of our kids
				encodeRecursive(context, column);

				writer.endElement("td");
				writer.writeText("\n", null);
			}

			writer.endElement("tr");
			writer.writeText("\n", null);

			if (checkExpanded && expanded) {
				writer.write(
						"<script type=\"text/javascript\">\n" +
						"  showHideDiv('_id_" + dmb.getMessage().getId() + "', '" + RESOURCE_PATH + "');\n" +
				"</script>\n");
			}

		}

		writer.endElement("tbody");
		writer.writeText("\n", null);

		// write the javascript that will allow the threads to be expanded and minimized
		if (!noArrows) {
			StringBuilder javascript = new StringBuilder();
			javascript
			.append("<script type=\"text/javascript\">\n")
			.append("  var msgChildren = new Array();\n")
			.append("  var msgExpanded = new Array();\n");

			for (Entry<Long, List<Long>> entry: msgChildren.entrySet()) {
				if (entry.getValue().size() > 0) {
					javascript.append("  msgChildren['").append(entry.getKey()).append("'] = new Array(");
					for (Long id: entry.getValue()) {
						javascript.append("'_id_").append(id).append("',");
					}
					javascript.setLength(javascript.length() - 1); // remove that last extra comma
					javascript
					.append(");\n")
					.append("  msgExpanded['").append(entry.getKey()).append("'] = false;\n");
				}
			}

			javascript
			.append("\n  function displayChildren(msgId) {\n")
			.append("    for (var i=0; i < msgChildren[msgId].length; i++) {\n")
			.append("      showHideDiv(msgChildren[msgId][i], '").append(RESOURCE_PATH).append("');\n")
			.append("    }\n")
			.append("  }\n")
			.append("</script>");
			writer.write(javascript.toString());
		}

		// Clean up after ourselves
		data.setRowIndex(-1);
		if (log.isTraceEnabled()) {
			log.trace("End encoding children " + component.getId());
		}
	}

	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException(Util.getExceptionMessageString(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
		}
		if (!component.isRendered()) {
			if (log.isTraceEnabled()) {
				log.trace("No encoding necessary " + component.getId() + " since " + "rendered attribute is set to false ");
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

	/**
	 * Processes the child {@link UIColumn} components that are nested in the data parameter and returns the data gleaned from the process as an object of RenderData.
	 * 
	 * @param data
	 *          The component being rendered.
	 * @return Returns an instance of RenderData whose three components contain the following data:
	 *         <ul>
	 *         <li>uiColumns - After this function is executed, will contain only the rendered UIColumn components in data</li>
	 *         <li>facetCountHeaders - After this function is executed, will contain only the number of "header" facets in data</li>
	 *         <li>facetCountFooters - After this fucntion is executed, will contain only the number of "footer" facets in data</li>
	 *         </ul>
	 */
	private RenderData organizeTheKids(UIData data) {
		RenderData returnVal = new RenderData();
		returnVal.uiColumns = new ArrayList<UIColumn>();
		returnVal.facetCountHeaders = 0;
		returnVal.facetCountFooters = 0;

		Iterator kids = data.getChildren().iterator();
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			if ((kid instanceof UIColumn) && kid.isRendered()) {
				returnVal.uiColumns.add((UIColumn) kid);
				if (getFacet(kid, "header") != null) returnVal.facetCountHeaders++;
				if (getFacet(kid, "footer") != null) returnVal.facetCountFooters++;
			}
		}

		return returnVal;
	}

	/**
	 * Return an array of stylesheet classes to be applied to each column in the table in the order specified. Every column may or may not have a stylesheet.
	 * 
	 * @param data
	 *          {@link UIData} component being rendered
	 */
	private String[] getColumnClasses(UIData data) {
		String values = (String) data.getAttributes().get("columnClasses");
		if (values == null) {
			return (new String[0]);
		}
		values = values.trim();
		ArrayList<String> list = new ArrayList<String>();
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
	 * Return an array of stylesheet classes to be applied to each row in the table, in the order specified. Every row may or may not have a stylesheet.
	 * 
	 * @param data
	 *          {@link UIData} component being rendered
	 */
	private String[] getRowClasses(UIData data) {
		String values = (String) data.getAttributes().get("rowClasses");
		if (values == null) {
			return (new String[0]);
		}
		values = values.trim();
		ArrayList<String> list = new ArrayList<String>();
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

}







	
	
	
	
	
	
	
	
	
	
	
	
	
