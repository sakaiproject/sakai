/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.newspaper;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIData;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.shared_tomahawk.util.ArrayUtils;
import org.apache.myfaces.shared_tomahawk.util.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renderer for a table in multiple balanced columns.
 *
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Data"
 *   type = "org.apache.myfaces.HtmlNewspaperTable"
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class HtmlNewspaperTableRenderer
        extends HtmlTableRendererBase
{
    private static final Log log = LogFactory.getLog(HtmlNewspaperTableRenderer.class);

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        RendererUtils.checkParamValidity(facesContext, uiComponent, UIData.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        HtmlNewspaperTable newspaperTable = (HtmlNewspaperTable)uiComponent;
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }
        
        // write out the table start tag
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TABLE_ELEM, newspaperTable);
        writer.writeAttribute(HTML.ID_ATTR, newspaperTable.getClientId(facesContext), null);
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, newspaperTable, behaviors);
            HtmlRendererUtils.renderHTMLAttributes(writer, newspaperTable, HTML.TABLE_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS); 
            
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, newspaperTable, HTML.TABLE_PASSTHROUGH_ATTRIBUTES);
        }

        // render the header
        renderFacet(facesContext, writer, newspaperTable, true);
    }
    
    public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        RendererUtils.checkParamValidity(facesContext, uiComponent, UIData.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        HtmlNewspaperTable newspaperTable = (HtmlNewspaperTable)uiComponent;
        
        // begin the table
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TBODY_ELEM, newspaperTable);
        
        // get the CSS styles
        Styles styles = getStyles(newspaperTable);

        // count the number of actual rows
        int first = newspaperTable.getFirst();
        int rows = newspaperTable.getRows();
        int rowCount = newspaperTable.getRowCount();
        if(rows <= 0) {
            rows = rowCount - first;
        }
        int last = first + rows;
        if(last > rowCount) last = rowCount;
        
        // the number of slices to break the table up into */
        int newspaperColumns = newspaperTable.getNewspaperColumns();
        int newspaperRows;
        if((last - first) % newspaperColumns == 0) newspaperRows = (last - first) / newspaperColumns;
        else newspaperRows = ((last - first) / newspaperColumns) + 1;

        // walk through the newspaper rows
        for(int nr = 0; nr < newspaperRows; nr++) {

            // write the row start
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.startElement(HTML.TR_ELEM, newspaperTable);
            if(styles.hasRowStyle()) {
                String rowStyle = styles.getRowStyle(nr);
                writer.writeAttribute(HTML.CLASS_ATTR, rowStyle, null);
            }

            // walk through the newspaper columns
            for(int nc = 0; nc < newspaperColumns; nc++) {

                // the current row in the 'real' table
                int currentRow = nc * newspaperRows + nr + first;
                
                // if this row is not to be rendered
                if(currentRow >= last) continue;

                // bail if any row does not exist
                newspaperTable.setRowIndex(currentRow);
                if(!newspaperTable.isRowAvailable()) {
                    log.error("Row is not available. Rowindex = " + currentRow);
                    return;
                }
    
                // write each cell
                List children = newspaperTable.getChildren();
                for(int j = 0; j < newspaperTable.getChildCount(); j++) {
                    // skip this child if its not a rendered column 
                    UIComponent child = (UIComponent)children.get(j);
                    if(!(child instanceof UIColumn)) continue;
                    if(!child.isRendered()) continue;
                    // draw the element's cell
                    writer.startElement(HTML.TD_ELEM, newspaperTable);
                    if(styles.hasColumnStyle()) writer.writeAttribute(HTML.CLASS_ATTR, styles.getColumnStyle(nc * newspaperTable.getChildCount() + j), null);
                    RendererUtils.renderChild(facesContext, child);
                    writer.endElement(HTML.TD_ELEM);
                }

                // draw the spacer facet
                if(nc < newspaperColumns - 1) renderSpacerCell(facesContext, writer, newspaperTable);
            }
            // write the row end
            writer.endElement(HTML.TR_ELEM);
        }
        
        // write the end of the table's body
        writer.endElement(HTML.TBODY_ELEM);
    }
    
    /** 
     * Fetch the number of columns to divide the table into.
     */
    private int getNewspaperColumns() {
        return 3;
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        RendererUtils.checkParamValidity(facesContext, uiComponent, UIData.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        HtmlNewspaperTable newspaperTable = (HtmlNewspaperTable)uiComponent;
        
        // render the footer
        renderFacet(facesContext, writer, newspaperTable, false);
        
        // write out the table end tag
        writer.endElement(HTML.TABLE_ELEM);
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
    }

    
    /**
     * Count the number of columns in the speicifed Newspaper table..
     */
    private int countColumns(HtmlNewspaperTable newspaperTable) {
        int columnCount = 0;
        for(Iterator it = newspaperTable.getChildren().iterator(); it.hasNext(); ) {
            UIComponent uiComponent = (UIComponent)it.next();
            if (uiComponent instanceof UIColumn && ((UIColumn)uiComponent).isRendered()) {
                columnCount++;
            }
        }
        return columnCount;
    }
    
    /**
     * Tests if the specified facet exists for the specified newspaper table.
     */
    private boolean hasFacet(HtmlNewspaperTable newspaperTable, boolean header) {
        for(Iterator it = newspaperTable.getChildren().iterator(); it.hasNext(); ) {
            // get the column
            UIComponent uiComponent = (UIComponent)it.next();
            if(!(uiComponent instanceof UIColumn)) continue;
            UIColumn column = (UIColumn)uiComponent;
            if(!column.isRendered()) continue;
            
            // test the facet
            if(header && ((UIColumn)uiComponent).getHeader() != null) return true;
            if(!header && ((UIColumn)uiComponent).getFooter() != null) return true;
        }
        return false;
    }

    /**
     * Render table headers and footers.
     */
    private void renderFacet(FacesContext facesContext, ResponseWriter writer, HtmlNewspaperTable newspaperTable, boolean header) throws IOException {
        int columnCount = countColumns(newspaperTable);
        boolean hasColumnFacet = hasFacet(newspaperTable, header);
        UIComponent facet = header ? newspaperTable.getHeader() : newspaperTable.getFooter();
        
        // quit if there's nothing to draw
        if(facet == null && !hasColumnFacet) return;
        
        // start the row block
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        String elemName = header ? HTML.THEAD_ELEM : HTML.TFOOT_ELEM;
        writer.startElement(elemName, newspaperTable);
        
        // fetch the style
        String styleClass;
        if(header) styleClass = getHeaderClass(newspaperTable);
        else styleClass = getFooterClass(newspaperTable);
        
        // write the header row and column headers
        if(header) {
            if (facet != null) renderTableHeaderOrFooterRow(facesContext, writer, newspaperTable, facet, styleClass, HTML.TD_ELEM, columnCount);
            if (hasColumnFacet) renderColumnHeaderOrFooterRow(facesContext, writer, newspaperTable, styleClass, header);
        // write the footer row and column footers
        } else {
            if (hasColumnFacet) renderColumnHeaderOrFooterRow(facesContext, writer, newspaperTable, styleClass, header);
            if (facet != null) renderTableHeaderOrFooterRow(facesContext, writer, newspaperTable, facet, styleClass, HTML.TD_ELEM, columnCount);
        }
        
        // end the row block
        writer.endElement(elemName);
    }
    
    /**
     * Renders the table header or footer row. This is one giant cell that spans
     * the entire table header or footer.
     */
    private void renderTableHeaderOrFooterRow(FacesContext facesContext, ResponseWriter writer,
        HtmlNewspaperTable newspaperTable, UIComponent facet, String styleClass, String colElementName, int tableColumns)
        throws IOException {

        // start the row
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TR_ELEM, newspaperTable);
        writer.startElement(colElementName, newspaperTable);
        if(styleClass != null) writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
        if(colElementName.equals(HTML.TH_ELEM)) writer.writeAttribute(HTML.SCOPE_ATTR, HTML.SCOPE_COLGROUP_VALUE, null);

        // span all the table's columns
        int totalColumns = newspaperTable.getNewspaperColumns() * tableColumns;
        if(newspaperTable.getSpacer() != null) totalColumns = totalColumns + getNewspaperColumns() - 1;
        writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(totalColumns), null);

        // write the actual cell contents
        if(facet != null) RendererUtils.renderChild(facesContext, facet);
        
        // finish
        writer.endElement(colElementName);
        writer.endElement(HTML.TR_ELEM);
    }


    /**
     * Renders the column header or footer row.
     */
    private void renderColumnHeaderOrFooterRow(FacesContext facesContext,
        ResponseWriter writer, HtmlNewspaperTable newspaperTable, String styleClass, boolean header)
        throws IOException {

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TR_ELEM, newspaperTable);
        int newspaperColumns = newspaperTable.getNewspaperColumns();
        for(int nc = 0; nc < newspaperColumns; nc++) {
            for(Iterator it = newspaperTable.getChildren().iterator(); it.hasNext(); ) {
                UIComponent uiComponent = (UIComponent)it.next();
                if(!(uiComponent instanceof UIColumn)) continue;
                UIColumn column = (UIColumn)uiComponent;
                if(!column.isRendered()) continue;
                // get the component to render
                UIComponent facet = null;
                if(header) facet = column.getHeader();
                else facet = column.getFooter();
                // do the rendering of the cells
                renderColumnHeaderOrFooterCell(facesContext, writer, column, styleClass, facet);
            }

            // draw the spacer facet
            if(nc < newspaperColumns - 1) renderSpacerCell(facesContext, writer, newspaperTable);
        }
        writer.endElement(HTML.TR_ELEM);
    }

    /**
     * Renders a cell in the column header or footer.
     */
    private void renderColumnHeaderOrFooterCell(FacesContext facesContext, ResponseWriter writer,
        UIColumn uiColumn, String styleClass, UIComponent facet) throws IOException {

        writer.startElement(HTML.TH_ELEM, uiColumn);
        if(styleClass != null) writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
        if(facet != null) RendererUtils.renderChild(facesContext, facet);
        writer.endElement(HTML.TH_ELEM);
    }
    
    /**
     * Renders a spacer between adjacent newspaper columns.
     */
    private void renderSpacerCell(FacesContext facesContext, ResponseWriter writer,
        HtmlNewspaperTable newspaperTable) throws IOException {
        if(newspaperTable.getSpacer() == null) return;
        
        writer.startElement(HTML.TD_ELEM, newspaperTable);
        RendererUtils.renderChild(facesContext, newspaperTable.getSpacer());
        writer.endElement(HTML.TD_ELEM);
    }

    /**
     * Gets the style class for the table header.
     */
    private static String getHeaderClass(HtmlNewspaperTable newspaperTable) {
        if(newspaperTable instanceof HtmlDataTable) {
            return ((HtmlDataTable)newspaperTable).getHeaderClass();
        } else {
            return (String)newspaperTable.getAttributes().get(JSFAttr.HEADER_CLASS_ATTR);
        }
    }
    /**
     * Gets the style class for the table footer.
     */
    private static String getFooterClass(HtmlNewspaperTable newspaperTable) {
        if(newspaperTable instanceof HtmlDataTable) {
            return ((HtmlDataTable)newspaperTable).getFooterClass();
        } else {
            return (String)newspaperTable.getAttributes().get(JSFAttr.FOOTER_CLASS_ATTR);
        }
    }

    /**
     * Gets styles for the specified component.
     */
    public static Styles getStyles(HtmlNewspaperTable newspaperTable) {
        String rowClasses;
        String columnClasses;
        if(newspaperTable instanceof HtmlDataTable) {
            rowClasses = ((HtmlDataTable)newspaperTable).getRowClasses();
            columnClasses = ((HtmlDataTable)newspaperTable).getColumnClasses();
        } else {
            rowClasses = (String)newspaperTable.getAttributes().get(JSFAttr.ROW_CLASSES_ATTR);
            columnClasses = (String)newspaperTable.getAttributes().get(JSFAttr.COLUMN_CLASSES_ATTR);
        }
        return new Styles(rowClasses, columnClasses);
    }

    /**
     * Class manages the styles from String lists.
     */
    private static class Styles {

        private String[] _columnStyle;
        private String[] _rowStyle;

        Styles(String rowStyles, String columnStyles) {
            _rowStyle = (rowStyles == null)
                ? ArrayUtils.EMPTY_STRING_ARRAY
                : StringUtils.trim(
                    StringUtils.splitShortString(rowStyles, ','));
            _columnStyle = (columnStyles == null)
                ? ArrayUtils.EMPTY_STRING_ARRAY
                : StringUtils.trim(
                    StringUtils.splitShortString(columnStyles, ','));
        }

        public String getRowStyle(int idx) {
            if(!hasRowStyle()) {
                return null;
            }
            return _rowStyle[idx % _rowStyle.length];
        }

        public String getColumnStyle(int idx) {
            if(!hasColumnStyle()) {
                return null;
            }
            return _columnStyle[idx % _columnStyle.length];
        }

        public boolean hasRowStyle() {
            return _rowStyle.length > 0;
        }

        public boolean hasColumnStyle() {
            return _columnStyle.length > 0;
        }
    }
}