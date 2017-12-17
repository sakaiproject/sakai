package org.sakaiproject.tool.gradebook.jsf.gradebookItemTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

import lombok.extern.slf4j.Slf4j;

import org.apache.myfaces.shared_impl.renderkit.html.HTML;

import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.tool.gradebook.ui.AssignmentGradeRow;

@Slf4j
public class GradebookItemTableRenderer extends Renderer {

	private static final String CURSOR = "cursor:pointer";
	private static final String EXPANDED_IMG = "/images/expand.gif";
	private static final String COLLAPSED_IMG = "/images/collapse.gif";
	
	private HtmlGraphicImage image;
	private boolean expanded;
	private String path;
	
	private static final String EXPAND_ALT = "cat_expanded";
	private static final String EXPAND_TITLE = "cat_view_collapse";
	private static final String COLLAPSE_ALT = "cat_collapsed";
	private static final String COLLAPSE_TITLE = "cat_view_expand";
	
	String imgExpandAlt;
	String imgExpandTitle;
	String imgCollapseAlt;
	String imgCollapseTitle;
	

	/**
	 * This component renders its children
	 * @return true
	 */
	public boolean getRendersChildren()
	{
		return true;
	}

	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof org.apache.myfaces.component.html.ext.HtmlDataTable);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{	
		if(!component.isRendered()){
			//tool_bar tag is not to be rendered, return now
			return;
		}
		UIData data = (UIData) component;

		//Begin Rendering
		ResponseWriter writer = context.getResponseWriter();

		//Render Header Facets
		writer.startElement("table", data);

		String styleClass = (String) data.getAttributes().get("styleClass");
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}
		String cellspacing = (String) data.getAttributes().get("cellspacing");
		if (cellspacing != null) {
			writer.writeAttribute("cellspacing", cellspacing, null);
		}
		String cellpadding = (String) data.getAttributes().get("cellpadding");
		if (cellpadding != null) {
			writer.writeAttribute("cellpadding", cellpadding, null);
		}
		
		String headerClasses[] = getHeaderClasses(data);
		int headerStyle = 0;
		int headerStyles = headerClasses.length;
		
		
		writer.writeText("\n", null);
		
		String expandedValue = (String) data.getAttributes().get("expanded");
		expanded = false;
		if(expandedValue != null && expandedValue.equals("true"))
			expanded = true;
		else
			expanded = false;

		path = context.getExternalContext().getRequestContextPath();
		
		imgExpandAlt = FacesUtil.getLocalizedString(EXPAND_ALT);
		imgExpandTitle = FacesUtil.getLocalizedString(EXPAND_TITLE);
		imgCollapseAlt = FacesUtil.getLocalizedString(COLLAPSE_ALT);
		imgCollapseTitle = FacesUtil.getLocalizedString(COLLAPSE_TITLE);

		image = new HtmlGraphicImage();
		if (expanded) {
			image.setValue(path + EXPANDED_IMG);
			image.setAlt(imgExpandAlt);
			image.setTitle(imgExpandTitle);
		}
		else {
			image.setValue(path + COLLAPSED_IMG);
			image.setAlt(imgCollapseAlt);
			image.setTitle(imgCollapseTitle);
		}

		// Render the header facets (if any)
		UIComponent header = data.getFacet("header");
		int headerFacets = getFacetCount(data, "header");
		if ((header != null) || (headerFacets > 0)) {
			writer.startElement("thead", data);
			writer.writeText("\n", null);
		}
		if (header != null) {
			writer.startElement("tr", header);
			writer.startElement("th", header);
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
				if (headerStyles > 0 && headerStyle < headerStyles) {
					writer.writeAttribute("class", headerClasses[headerStyle++], "headerClass");
				}
				writer.writeAttribute("scope", "col", null);
				if (column.getId().endsWith("_toggle")) {
					// get the number of toggles for "expand/collapse all" js
					int numItems = getNumDataItemsForToggle(context, component);
					String onclickText = "javascript:showHideAll('" + numItems + "', '" +  path + "', '" + imgExpandAlt  + "', '" + imgCollapseAlt  + "', '" + imgExpandTitle + "', '" + imgCollapseTitle + "');";
					
					writer.startElement(HTML.IMG_ELEM, image);
					writer.writeURIAttribute("src", image.getValue(), null);
					writer.writeURIAttribute("onclick", onclickText, null);
					writer.writeURIAttribute("style", CURSOR, null);
					writer.writeURIAttribute("alt", image.getAlt(), null);
					writer.writeURIAttribute("title", image.getTitle(), null);
					writer.writeURIAttribute("id", "expandCollapseAll", null);
					writer.endElement(HTML.IMG_ELEM);
				}
				UIComponent facet = column.getFacet("header");
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
		UIComponent footer = data.getFacet("footer");
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
				UIComponent facet = column.getFacet("footer");
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

	/**
	 * We put all our processing in the encodeChildren method
	 * @param context
	 * @param component
	 * @throws IOException
	 */
	public void encodeChildren(FacesContext context, UIComponent component)
	throws IOException
	{

		if ((context == null) || (component == null)) {
			return;
		}
		if (!component.isRendered()) {
			return;
		}

		UIData data = (UIData) component;

		ValueBinding gbItemsBinding = component.getValueBinding("value");
		List gbItemList = (List)gbItemsBinding.getValue(context);

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
			Category gbCategory = null;

			boolean isCategory = false;
			boolean isAssignment = false;
			boolean isCourseGrade = false;
			boolean isGradeRow = false;
			
			if(gbItemList !=null && gbItemList.size() > (rowIndex+1) && rowIndex > -2)
			{
				Object gbItem = gbItemList.get(rowIndex+1);
				if (gbItem instanceof Category) {
					isCategory = true;
					isCourseGrade = false;
					isAssignment = false;
					gbCategory = (Category) gbItem;
				}
				else if (gbItem instanceof GradableObject){
					isCategory = false;
					GradableObject go = (GradableObject) gbItem;
					if (go.isAssignment()) {
						isAssignment = true;
						isCourseGrade = true;
					}
					else {
						isAssignment = false;
						isCourseGrade = true;
					}
				}
				else if (gbItem instanceof AssignmentGradeRow) {
					isCategory = false;
					isCourseGrade = false;
					isAssignment = false;
					isGradeRow = true;
				}
				else {
					throw new IllegalArgumentException("Invalid class passed to renderer: " + gbItem.getClass());
				}
			}
			
			boolean hasChildBoolean = false;
			int childNo = 0;
			if(isCategory && gbCategory != null && gbItemList.size() > rowIndex + 2)
			{
				for (int i=rowIndex+2; i < gbItemList.size(); i++) {
					Object nextItem = gbItemList.get(i);
					if (nextItem instanceof GradableObject) {
						GradableObject nextGo = (GradableObject) nextItem;
						if (nextGo.isAssignment()) {
							hasChildBoolean = true;
							childNo++;
						} else {
							break;
						}
					} 
					else if (nextItem instanceof AssignmentGradeRow) {
						hasChildBoolean = true;
						childNo++;
					} else if (nextItem instanceof Category) {
						break;
					}
				}
			} 

			// Have we displayed the requested number of rows?
			if ((rows > 0) && (++processed > rows)) {
				break;
			}
			// Select the current row
			data.setRowIndex(++rowIndex);
			if (!data.isRowAvailable()) {
				break; // Scrolled past the last row
			}

			// Render the beginning of this row
			writer.startElement("tr", data);
			if(isAssignment || isGradeRow)
			{
				String assignId = "_id_" + new Integer(hideDivNo).toString() + "__hide_division_";
				writer.writeAttribute("id", assignId, null);
				if (!expanded) 
					writer.writeAttribute("style", "display:none;", null);
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
					writer.writeAttribute("class", columnClasses[columnStyle++],
					"columnClasses");
					if (columnStyle >= columnStyles) {
						columnStyle = 0;
					}
				}

				if((isAssignment || isCourseGrade || isCategory || isGradeRow) 
						&& column.getId().endsWith("_toggle"))
				{
					if(hasChildBoolean && isCategory)
					{
						String imgId = "_id_" + new Integer(hideDivNo).toString() + "__img_hide_division_";
						StringBuffer hideTr = new StringBuffer();
						for(int i=0; i<childNo; i++)
						{
							hideTr.append("javascript:showHideDiv('_id_").append(new Integer(hideDivNo+i).toString()).append("', '")
							      .append(path).append("', '").append(imgExpandAlt).append("', '").append(imgCollapseAlt).append("', '")
							      .append(imgExpandTitle).append("', '").append(imgCollapseTitle).append("');");
						}
						
						hideTr.append("mySetMainFrameHeight('Main").append(ToolManager.getCurrentPlacement().getId().replace('-','x')).append("');");

						writer.startElement(HTML.IMG_ELEM, image);
						writer.writeURIAttribute("src", image.getValue(), null);
						writer.writeURIAttribute("onclick", hideTr, null);
						writer.writeURIAttribute("style", CURSOR, null);
						writer.writeURIAttribute("alt", image.getAlt(), null);
						writer.writeURIAttribute("title", image.getTitle(), null);
						writer.writeURIAttribute("id", imgId, null);
						writer.endElement(HTML.IMG_ELEM);
					}
					
					writer.endElement("td");
					continue;
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

			if(isAssignment || isGradeRow)
			{
				if(expanded)
				{
					writer.write("<script type=\"text/javascript\">");
					writer.write("  showHideDiv('_id_" + new Integer(hideDivNo).toString() +
							"', '" +  path + "', '" + imgExpandAlt  + "', '" + imgCollapseAlt  + "', '" + imgExpandTitle + "', '" + imgCollapseTitle + "');");
					writer.write("</script>");
				}

				hideDivNo++;
			}
		}
		writer.endElement("tbody");
		writer.writeText("\n", null);

		// Clean up after ourselves
		data.setRowIndex(-1);
		if (log.isTraceEnabled()) {
			log.trace("End encoding children " +
					component.getId());
		}
	}

	public void encodeEnd(FacesContext context, UIComponent component)
	throws IOException {

		if ((context == null) || (component == null)) {
			return;
		}
		if (!component.isRendered()) {
			return;
		}
		UIData data = (UIData) component;
		data.setRowIndex(-1);
		ResponseWriter writer = context.getResponseWriter();

		// Render the ending of this table
		writer.endElement("table");
		writer.writeText("\n", null);
	}

	/**
	 * <p>Return an Iterator over the <code>UIColumn</code> children
	 * of the specified <code>UIData</code> that have a
	 * <code>rendered</code> property of <code>true</code>.</p>
	 *
	 * @param data <code>UIData</code> for which to extract children
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
	 * <p>Return the number of child <code>UIColumn</code> components
	 * nested in the specified <code>UIData</code> that have a facet with
	 * the specified name.</p>
	 *
	 * @param data <code>UIData</code> component being analyzed
	 * @param name Name of the facet being analyzed
	 */
	private int getFacetCount(UIData data, String name) {

		int n = 0;
		Iterator kids = getColumns(data);
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			if (kid.getFacet(name) != null) {
				n++;
			}
		}
		return (n);

	}

	protected Iterator getChildren(UIComponent oComponent){
		List oResults = new ArrayList();
		for(Iterator oKids = oComponent.getChildren().iterator(); oKids.hasNext();){
			UIComponent oKid = (UIComponent)oKids.next();
			if(oKid.isRendered())
				oResults.add(oKid);
		}
		return oResults.iterator();
	}

	private void encodeRecursive(FacesContext oContext,UIComponent oComponent) throws IOException{
		if (!oComponent.isRendered()){
			return;  
		}
		oComponent.encodeBegin(oContext);
		if (oComponent.getRendersChildren()){
			oComponent.encodeChildren(oContext); 
		}
		else{
			Iterator oChildren = getChildren(oComponent);
			for(;oChildren.hasNext();){
				UIComponent oChildComponent = (UIComponent)oChildren.next();
				encodeRecursive(oContext,oChildComponent);
			}
		}
		oComponent.encodeEnd(oContext);
	}

	/**
	 * <p>Return an array of stylesheet classes to be applied to
	 * each column in the table in the order specified. Every column may or
	 * may not have a class.</p>
	 *
	 * @param data {@link UIData} component being rendered
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
		
		// now iterate through the columns and remove the class for any
		// columns that aren't rendered
		List columns = data.getChildren();
		int listIndex = 0;
		for (int i=0; i < columns.size(); i++) {
			if (i >= list.size())
				break;
			UIComponent kid = (UIComponent) columns.get(i);
			if ((kid instanceof UIColumn) && !kid.isRendered()) {
				list.remove(listIndex);
			} else {
				listIndex++;
			}
		}
		
		String results[] = new String[list.size()];
		return ((String[]) list.toArray(results));
	}
	
	/**
	 * <p>Return an array of stylesheet classes to be applied to
	 * each header in the table in the order specified. Every column may or
	 * may not have a class.</p>
	 *
	 * @param data {@link UIData} component being rendered
	 */
	private String[] getHeaderClasses(UIData data) {

		String values = (String) data.getAttributes().get("headerClasses");
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
		
		// now iterate through the columns and remove the class for any
		// columns that aren't rendered
		List columns = data.getChildren();
		int listIndex = 0;
		for (int i=0; i < columns.size(); i++) {
			if (i >= list.size())
				break;
			UIComponent kid = (UIComponent) columns.get(i);
			if ((kid instanceof UIColumn) && !kid.isRendered()) {
				list.remove(listIndex);
				
			} else {
				listIndex++;
			}
		}
		
		String results[] = new String[list.size()];
		return ((String[]) list.toArray(results));
	}

	/**
	 * <p>Return the number of child <code>UIColumn</code> components
	 * that are nested in the specified {@link UIData}.</p>
	 *
	 * @param data {@link UIData} component being analyzed
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
	 * <p>Return an array of stylesheet classes to be applied to
	 * each row in the table, in the order specified.  Every row may or
	 * may not have a stylesheet.</p>
	 *
	 * @param data {@link UIData} component being rendered
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
	
	private int getNumDataItemsForToggle(FacesContext context, UIComponent component) {
		int numDataItems = 0;
		ValueBinding gbItemsBinding = component.getValueBinding("value");
		List gbItemList = (List)gbItemsBinding.getValue(context);
		if (gbItemList != null && !gbItemList.isEmpty()) {
			numDataItems = gbItemList.size();
		}
		return numDataItems;
	}
}
