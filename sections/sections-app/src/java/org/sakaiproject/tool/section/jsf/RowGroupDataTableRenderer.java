package org.sakaiproject.tool.section.jsf;

import java.util.List;
import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import lombok.extern.slf4j.Slf4j;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.renderkit.html.ext.HtmlTableRenderer;

@Slf4j
public class RowGroupDataTableRenderer extends HtmlTableRenderer {

	public static final String SECTION_STYLE_CLASS = "groupRow";
	public static final String CATEGORY_HEADER_STYLE_CLASS = "categoryHeader";
	public static final String FIRST_CATEGORY_HEADER_STYLE_CLASS = "firstCategoryHeader";

	public void encodeInnerHtml(FacesContext facesContext, UIComponent component)throws IOException {

		UIData uiData = (UIData) component;
		ResponseWriter writer = facesContext.getResponseWriter();

		Styles styles = getStyles(uiData);
		
		int first = uiData.getFirst();
		int rows = uiData.getRows();
		int rowCount = uiData.getRowCount();
		if (rows <= 0)
		{
			rows = rowCount - first;
		}
		int last = first + rows;
		if (last > rowCount)
			last = rowCount;

		for (int i = first; i < last; i++)
		{
			uiData.setRowIndex(i);
			if (!uiData.isRowAvailable())
			{
                log.warn("Row is not available. Rowindex = " + i);
				return;
			}

			int columns = component.getChildCount();
			renderCategoryRow(i, columns, uiData, writer, i==first);

			beforeRow(facesContext, uiData);
			HtmlRendererUtils.writePrettyLineSeparator(facesContext);
			renderRowStart(facesContext, writer, uiData, styles, i);

			List children = component.getChildren();
			for (int j = 0, size = component.getChildCount(); j < size; j++)
			{
				UIComponent child = (UIComponent) children.get(j);
				if(child.isRendered())
				{
					encodeColumnChild(facesContext, writer, uiData, child, styles, j);
				}
			}
			renderRowEnd(facesContext, writer, uiData);

			afterRow(facesContext, uiData);
		}
	}

	private void renderCategoryRow(int rowNumber, int columns, UIData uiData, ResponseWriter writer, boolean firstCategory) throws IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		// Cast the uiData into our custom component
		RowGroupDataTable rowGroupDataTable;
		try {
			rowGroupDataTable = (RowGroupDataTable)uiData;
		} catch (ClassCastException cce) {
			log.warn(cce.getMessage());
			return;
		}

		// Get the current section
		RowGroupable rowGroupable;
		List list = (List)uiData.getValue();
		try {
			rowGroupable = (RowGroupable)list.get(rowNumber);
		} catch (IndexOutOfBoundsException ioobe) {
			log.warn(ioobe.getMessage());
			return;
		}

		// For Daisy's CM Home tool
		if (rowNumber == 0){
			// reset rowGroupDataTable.category
			rowGroupDataTable.category = null;
		}

		// Is this section different from the previous RowGroup?
		if( ! rowGroupable.getRowGroupId().equals(rowGroupDataTable.category)) {
			// Update the SectionTable's current RowGroup
			rowGroupDataTable.category = rowGroupable.getRowGroupId();

			// Render a table row for the RowGroup header
			beforeRow(facesContext, uiData);
			HtmlRendererUtils.writePrettyLineSeparator(facesContext);

	        writer.startElement(HTML.TR_ELEM, uiData);
	    	if(firstCategory) {
	            writer.writeAttribute(HTML.CLASS_ATTR, FIRST_CATEGORY_HEADER_STYLE_CLASS, null);
	    	} else {
	            writer.writeAttribute(HTML.CLASS_ATTR, CATEGORY_HEADER_STYLE_CLASS, null);
	        }
	        
	        Object rowId = uiData.getAttributes().get(JSFAttr.ROW_ID);

	        if (rowId != null) {
	            writer.writeAttribute(HTML.ID_ATTR, rowId.toString(), null);
	        }

			// Render a single colspanned cell displaying the current RowGroup
			writer.startElement(HTML.TD_ELEM, uiData);
			writer.writeAttribute(HTML.COLSPAN_ATTR, columns, null);
			writer.write(JsfUtil.getLocalizedMessage("section_table_category_header", new String[] {rowGroupable.getRowGroupTitle()}));
			writer.endElement(HTML.TD_ELEM);

			renderRowEnd(facesContext, writer, uiData);
			afterRow(facesContext, uiData);
		}
	}
    
}
