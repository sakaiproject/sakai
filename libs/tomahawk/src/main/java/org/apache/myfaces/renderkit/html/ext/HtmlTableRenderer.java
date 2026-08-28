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

package org.apache.myfaces.renderkit.html.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.component.NewspaperTable;
import org.apache.myfaces.component.html.ext.AbstractHtmlDataTable;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.column.HtmlColumn;
import org.apache.myfaces.custom.column.HtmlSimpleColumn;
import org.apache.myfaces.custom.crosstable.UIColumns;
import org.apache.myfaces.renderkit.html.util.ColumnInfo;
import org.apache.myfaces.renderkit.html.util.RowInfo;
import org.apache.myfaces.renderkit.html.util.TableContext;
import org.apache.myfaces.shared_tomahawk.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase;
import org.apache.myfaces.shared_tomahawk.util.ArrayUtils;

/**
 * Renderer for the Tomahawk extended HtmlDataTable component.
 *
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691079 $ $Date: 2008-09-01 17:55:44 -0500 (lun, 01 sep 2008) $
 */
@JSFRenderer(
   renderKitId = "HTML_BASIC",
   family = "jakarta.faces.Data",
   type = "org.apache.myfaces.Table")
public class HtmlTableRenderer extends HtmlTableRendererBase {
    private static final Log log = LogFactory.getLog(HtmlTableRenderer.class);

    /**
     * DetailStamp facet name.
     */
    public static final String DETAIL_STAMP_FACET_NAME = "detailStamp";
    private static final String BODY_STYLE_CLASS = "bodyStyleClass";
    private static final String BODY_STYLE = "bodyStyle";
    
    //Client events
    public static final String ROW_CLICK = "rowClick";
    public static final String ROW_DBLCLICK = "rowDblClick";
    
    public static final String ROW_KEYDOWN = "rowKeyDown";
    public static final String ROW_KEYPRESS = "rowKeyPress";
    public static final String ROW_KEYUP = "rowKeyUp";
    
    public static final String ROW_MOUSEDOWN = "rowMouseDown";
    public static final String ROW_MOUSEMOVE = "rowMouseMove";
    public static final String ROW_MOUSEOUT = "rowMouseOut";
    public static final String ROW_MOUSEOVER = "rowMouseOver";
    public static final String ROW_MOUSEUP = "rowMouseUp";
    
    //Attributes
    public static final String ROW_ONCLICK_ATTR = "rowOnClick";
    public static final String ROW_ONDBLCLICK_ATTR = "rowOnDblClick";
    
    public static final String ROW_ONKEYDOWN_ATTR = "rowOnKeyDown";
    public static final String ROW_ONKEYPRESS_ATTR = "rowOnKeyPress";
    public static final String ROW_ONKEYUP_ATTR = "rowOnKeyUp";
    
    public static final String ROW_ONMOUSEDOWN_ATTR = "rowOnMouseDown";
    public static final String ROW_ONMOUSEMOVE_ATTR = "rowOnMouseMove";
    public static final String ROW_ONMOUSEOUT_ATTR = "rowOnMouseOut";
    public static final String ROW_ONMOUSEOVER_ATTR = "rowOnMouseOver";
    public static final String ROW_ONMOUSEUP_ATTR = "rowOnMouseUp";
    
    private static final String[] EVENTS = {
        ClientBehaviorEvents.CLICK,
        ClientBehaviorEvents.DBLCLICK,
        ClientBehaviorEvents.KEYDOWN,
        ClientBehaviorEvents.KEYPRESS,
        ClientBehaviorEvents.KEYUP,
        ClientBehaviorEvents.MOUSEDOWN,
        ClientBehaviorEvents.MOUSEMOVE,
        ClientBehaviorEvents.MOUSEOUT,
        ClientBehaviorEvents.MOUSEOVER,
        ClientBehaviorEvents.MOUSEUP
    };
    
    private static final String[] EVENTS_ATTRIBUTES =
    {
        HTML.ONCLICK_ATTR,
        HTML.ONDBLCLICK_ATTR,
        HTML.ONKEYDOWN_ATTR,
        HTML.ONKEYPRESS_ATTR,
        HTML.ONKEYUP_ATTR,
        HTML.ONMOUSEDOWN_ATTR,
        HTML.ONMOUSEMOVE_ATTR,
        HTML.ONMOUSEOUT_ATTR,
        HTML.ONMOUSEOVER_ATTR,
        HTML.ONMOUSEUP_ATTR,
    };
    
    private static final String[] COLUMN_ATTRIBUTES_WITHOUT_EVENTS = (String [])
        ArrayUtils.concat(HTML.UNIVERSAL_ATTRIBUTES_WITHOUT_STYLE, new String[]{HTML.COLSPAN_ATTR});

    private static final String[] COLUMN_ATTRIBUTES = (String [])
        (String[]) ArrayUtils.concat(HTML.COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE, new String[]{HTML.COLSPAN_ATTR});

    @Override
    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    @Override
    protected boolean isCommonEventsOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    /**
     * @param component dataTable
     * @return number of layout columns
     */
    protected int getNewspaperColumns(UIComponent component) {
        if (component instanceof NewspaperTable) {
            // the number of slices to break the table up into */
            NewspaperTable newspaperTable = (NewspaperTable) component;
            return newspaperTable.getNewspaperColumns();
        }
        return super.getNewspaperColumns(component);
    }

    /**
     * @param component dataTable
     * @return component to display between layout columns
     */
    protected UIComponent getNewspaperTableSpacer(UIComponent component) {
        if (component instanceof NewspaperTable) {
            // the number of slices to break the table up into */
            NewspaperTable newspaperTable = (NewspaperTable) component;
            return newspaperTable.getSpacer();
        }
        return super.getNewspaperTableSpacer(component);
    }

    /**
     * @param component dataTable
     * @return whether dataTable has component to display between layout columns
     */
    protected boolean hasNewspaperTableSpacer(UIComponent component) {
        if (null != getNewspaperTableSpacer(component)) {
            return true;
        }
        return super.hasNewspaperTableSpacer(component);
    }

    /**
     * @param component dataTable
     * @return if the orientation of the has newspaper columns is horizontal
     */
    protected boolean isNewspaperHorizontalOrientation(UIComponent component) {
        if (component instanceof NewspaperTable) {
            // get the value of the newspaperOrientation attribute, any value besides horizontal
            // means vertical, the default
            NewspaperTable newspaperTable = (NewspaperTable) component;
            return NewspaperTable.NEWSPAPER_HORIZONTAL_ORIENTATION.equals(newspaperTable.getNewspaperOrientation());
        }
        return super.isNewspaperHorizontalOrientation(component);
    }

    protected void startTable(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        boolean embedded = isEmbeddedTable(uiComponent);

        if (!embedded) {
            super.startTable(facesContext, uiComponent);
        }
    }

    protected String determineHeaderFooterTag(FacesContext facesContext, UIComponent component, boolean header) {
        if (isEmbeddedTable(component)) {
            // we are embedded, so do not render the tfoot/thead stuff
            return null;
        }

        return super.determineHeaderFooterTag(facesContext, component, header);
    }

    protected String determineHeaderCellTag(FacesContext facesContext, UIComponent component) {
        if (isEmbeddedTable(component)) {
            return HTML.TD_ELEM;
        }

        return HTML.TH_ELEM;
    }

    protected void renderTableHeaderOrFooterRow(FacesContext facesContext, ResponseWriter writer, UIComponent component, UIComponent facet, String styleClass, String colElementName, int colspan, boolean isHeader) throws IOException {
        if (isEmbeddedTable(component)) {
            // embedded tables render the header/footer stuff using TD only
            colElementName = HTML.TD_ELEM;
        }

        super.renderTableHeaderOrFooterRow(facesContext, writer, component, facet, styleClass, colElementName, colspan, isHeader);
    }

    protected boolean isEmbeddedTable(UIComponent uiComponent) {
        boolean embedded = false;
        if (uiComponent instanceof HtmlDataTable) {
            HtmlDataTable table = (HtmlDataTable) uiComponent;
            embedded = table.isEmbedded();
        }
        return embedded;
    }

    protected boolean isDetailStampAfterRow(UIComponent uiComponent) {
        if (uiComponent instanceof HtmlDataTable) {
            return "after".equals(((HtmlDataTable) uiComponent).getDetailStampLocation());
        }

        return true;
    }

    protected void endTable(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        boolean embedded = isEmbeddedTable(uiComponent);

        if (!embedded) {
            super.endTable(facesContext, uiComponent);
        }
    }

    protected void beforeRow(FacesContext facesContext, UIData uiData) throws IOException {
        super.beforeRow(facesContext, uiData);

        if (!isDetailStampAfterRow(uiData)) {
            renderDetailRow(facesContext, uiData);
        }
    }

    protected void afterRow(FacesContext facesContext, UIData uiData) throws IOException {
        super.afterRow(facesContext, uiData);

        if (isDetailStampAfterRow(uiData)) {
            renderDetailRow(facesContext, uiData);
        }
    }

    /**
     * @param facesContext
     * @param uiData
     * @throws IOException
     */
    private void renderDetailRow(FacesContext facesContext, UIData uiData) throws IOException {
        UIComponent detailStampRow = uiData.getFacet(AbstractHtmlDataTable.DETAIL_STAMP_ROW_FACET_NAME);
        UIComponent detailStampFacet = uiData.getFacet(DETAIL_STAMP_FACET_NAME);
        
        if (detailStampRow != null && detailStampFacet != null)
        {
            detailStampRow.encodeAll(facesContext);
        }
        else
        {
            if (uiData instanceof HtmlDataTable) {
                HtmlDataTable htmlDataTable = (HtmlDataTable) uiData;
    
                if (htmlDataTable.isCurrentDetailExpanded()) {
    
                    boolean embedded = false;
                    if (detailStampFacet != null) {
                        embedded = isEmbeddedTable(detailStampFacet);
                    }
    
                    ResponseWriter writer = facesContext.getResponseWriter();
    
                    if (!embedded) {
                        writer.startElement(HTML.TR_ELEM, uiData);
                        writer.startElement(HTML.TD_ELEM, uiData);
                        //TOMAHAWK-1087 datatable dont renders a detail correct 
                        //if a UIColumns is used we have to count UIColumns 
                        //elements as component.getRowCount()
                        //instead of just get the number of children available,
                        //so the colspan could be assigned correctly.
                        int childCount = 0;
                        for (Iterator childIter = uiData.getChildren().iterator();
                            childIter.hasNext();)
                        {
                            UIComponent childComp = (UIComponent) childIter.next();
                            if (childComp instanceof UIColumns)
                            {
                                UIColumns v = (UIColumns) childComp;
                                childCount += v.getRowCount();
                            }
                            else
                            {
                                childCount++;
                            }
                        }
                        writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(
                                childCount), null);
                    }
    
                    if (detailStampFacet != null) {
                        RendererUtils.renderChild(facesContext, detailStampFacet);
                    }
    
                    if (!embedded) {
                        writer.endElement(HTML.TD_ELEM);
                        writer.endElement(HTML.TR_ELEM);
                    }
                }
            }
        }
    }

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#encodeBegin(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent)
     */
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        if (uiComponent instanceof HtmlDataTable) {
            HtmlDataTable htmlDataTable = (HtmlDataTable) uiComponent;
            if (htmlDataTable.isRenderedIfEmpty() || htmlDataTable.getRowCount() > 0) {
                super.encodeBegin(facesContext, uiComponent);
            }
        }
        else {
            super.encodeBegin(facesContext, uiComponent);
        }
    }

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#encodeChildren(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent)
     */
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
        if (component instanceof HtmlDataTable) {
            HtmlDataTable htmlDataTable = (HtmlDataTable) component;
            if (htmlDataTable.isRenderedIfEmpty() || htmlDataTable.getRowCount() > 0) {
                super.encodeChildren(facesContext, component);
            }
        }
        else {
            super.encodeChildren(facesContext, component);
        }
    }

    private boolean isGroupedTable(UIData uiData) {
        if (uiData instanceof HtmlDataTable) {
            List children = getChildren(uiData);
            for (int j = 0, size = getChildCount(uiData); j < size; j++) {
                UIComponent child = (UIComponent) children.get(j);
                if (child instanceof HtmlSimpleColumn) {
                    HtmlSimpleColumn column = (HtmlSimpleColumn) child;
                    if (column.isGroupBy()) {
                        return true;
                    }
                }
            }
        }

        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    protected void beforeBody(FacesContext facesContext, UIData uiData) throws IOException {
        if (isGroupedTable(uiData)) {
            createColumnInfos((HtmlDataTable) uiData, facesContext);
        }
        super.beforeBody(facesContext, uiData);
    }

    private void createColumnInfos(HtmlDataTable htmlDataTable, FacesContext facesContext)
        throws IOException {
        int first = htmlDataTable.getFirst();
        int rows = htmlDataTable.getRows();
        int last;
        int currentRowSpan = -1;
        int currentRowInfoIndex = -1;

        TableContext tableContext = htmlDataTable.getTableContext();
        RowInfo rowInfo = null;
        ColumnInfo columnInfo = null;
        HtmlSimpleColumn currentColumn = null;
        Map groupHashTable = new HashMap();

        if (rows <= 0) {
            last = htmlDataTable.getRowCount();
        }
        else {
            last = first + rows;
        }

        //Loop over the Children Columns to find the Columns with groupBy Attribute true
        List children = getChildren(htmlDataTable);
        int nChildren = getChildCount(htmlDataTable);

        for (int j = 0, size = nChildren; j < size; j++) {
            UIComponent child = (UIComponent) children.get(j);
            if (child instanceof HtmlSimpleColumn) {
                currentColumn = (HtmlSimpleColumn) child;
                if (currentColumn.isGroupBy()) {
                    groupHashTable.put(new Integer(j), null);
                }
            }
        }

        boolean groupEndReached = false;

        for (int rowIndex = first; last == -1 || rowIndex < last; rowIndex++) {
            htmlDataTable.setRowIndex(rowIndex);
            rowInfo = new RowInfo();
            //scrolled past the last row
            if (!htmlDataTable.isRowAvailable()) {
                break;
            }

            Set groupIndexList = groupHashTable.keySet();
            List currentColumnContent = null;
            for (Iterator it = groupIndexList.iterator(); it.hasNext();) {
                currentColumnContent = new ArrayList();
                Integer currentIndex = (Integer) it.next();
                currentColumn = (HtmlSimpleColumn) children.get(currentIndex.intValue());

                if (currentColumn.isGroupByValueSet()) {
                    currentColumnContent.add(currentColumn.getGroupByValue());
                }
                else {
                    // iterate the children - this avoids to add the column facet too
                    List currentColumnChildren = currentColumn.getChildren();
                    if (currentColumnChildren != null) {
                        collectChildrenValues(currentColumnContent, currentColumnChildren.iterator());
                    }
                }

                if (!isListEqual(currentColumnContent, (List) groupHashTable.get(currentIndex)) &&
                    currentRowInfoIndex > -1) {
                    groupEndReached = true;
                    groupHashTable.put(currentIndex, currentColumnContent);
                }
                else if (currentRowInfoIndex == -1) {
                    groupHashTable.put(currentIndex, currentColumnContent);
                }
            }
            currentRowSpan++;


            for (int j = 0, size = nChildren; j < size; j++) {
                columnInfo = new ColumnInfo();
                if (groupHashTable.containsKey(new Integer(j)))  // Column is groupBy
                {
                    if (currentRowSpan > 0) {
                        if (groupEndReached) {
                            ((ColumnInfo)
                                ((RowInfo)
                                    tableContext.getRowInfos().get(currentRowInfoIndex - currentRowSpan + 1)).
                                    getColumnInfos().get(j)).
                                setRowSpan(currentRowSpan);
                            columnInfo.setStyle(htmlDataTable.getRowGroupStyle());
                            columnInfo.setStyleClass(htmlDataTable.getRowGroupStyleClass());
                        }
                        else {
                            columnInfo.setRendered(false);
                        }
                    }
                    else {
                        columnInfo.setStyle(htmlDataTable.getRowGroupStyle());
                        columnInfo.setStyleClass(htmlDataTable.getRowGroupStyleClass());
                    }

                }
                else    // Column  is not group by
                {
                    if (groupEndReached) {
                        ((ColumnInfo)
                            ((RowInfo)
                                tableContext.getRowInfos().get(currentRowInfoIndex)).
                                getColumnInfos().get(j)).
                            setStyle(htmlDataTable.getRowGroupStyle());
                        ((ColumnInfo)
                            ((RowInfo)
                                tableContext.getRowInfos().get(currentRowInfoIndex)).
                                getColumnInfos().get(j)).
                            setStyleClass(htmlDataTable.getRowGroupStyleClass());
                    }
                }
                rowInfo.getColumnInfos().add(columnInfo);
            }
            if (groupEndReached) {
                currentRowSpan = 0;
                groupEndReached = false;
            }
            tableContext.getRowInfos().add(rowInfo);
            currentRowInfoIndex++;
        }

        // do further processing if we've found at least one row
        if (currentRowInfoIndex > -1) {
            for (int j = 0, size = nChildren; j < size; j++) {
                if (groupHashTable.containsKey(new Integer(j)))  // Column is groupBy
                {
                    ((ColumnInfo)
                        ((RowInfo)
                            tableContext.getRowInfos().get(currentRowInfoIndex - currentRowSpan)).
                            getColumnInfos().get(j)).
                        setRowSpan(currentRowSpan + 1);
                }
                else    // Column  is not group by
                {
                    ((ColumnInfo)
                        ((RowInfo)
                            tableContext.getRowInfos().get(currentRowInfoIndex)).
                            getColumnInfos().get(j)).
                        setStyle(htmlDataTable.getRowGroupStyle());
                    ((ColumnInfo)
                        ((RowInfo)
                            tableContext.getRowInfos().get(currentRowInfoIndex)).
                            getColumnInfos().get(j)).
                        setStyleClass(htmlDataTable.getRowGroupStyleClass());
                }
            }
        }

        htmlDataTable.setRowIndex(-1);
    }

    protected void collectChildrenValues(List container, Iterator iterChildren) {
        while (iterChildren.hasNext()) {
            UIComponent child = (UIComponent) iterChildren.next();
            if (child.isRendered()) {
                if (child instanceof ValueHolder) {
                    Object value = ((ValueHolder) child).getValue();
                    container.add(value);
                }

                // we lave the column ... now iterate the facets too
                collectChildrenValues(container, child.getFacetsAndChildren());
            }
        }
    }

    /**
     * checks if the contenta of both lists are the same.<br />
     * <b>Notice:</b> In case both lists are null or empty they are
     * considered NOT being equal
     */
    protected boolean isListEqual(List list1, List list2) {
        if (list1 == list2) {
            return list1 != null && list1.size() > 0;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            Object o1 = list1.get(i);
            Object o2 = list2.get(i);

            if (o1 != null && !o1.equals(o2)) {
                return false;
            }
            if (o2 != null && !o2.equals(o1)) {
                return false;
            }
        }

        return true;
    }


    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#encodeEnd(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent)
     */
    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        if (uiComponent instanceof HtmlDataTable) {
            HtmlDataTable htmlDataTable = (HtmlDataTable) uiComponent;
            if (htmlDataTable.isRenderedIfEmpty() || htmlDataTable.getRowCount() > 0) {
                super.encodeEnd(facesContext, uiComponent);
            }
        }
        else {
            super.encodeEnd(facesContext, uiComponent);
        }
    }

    protected void renderRowStart(FacesContext facesContext,
                                  ResponseWriter writer, UIData uiData, Styles styles, int rowStyleIndex)
        throws IOException {
        super.renderRowStart(facesContext, writer, uiData, styles, rowStyleIndex);

        // get event handlers from component
        HtmlDataTable table = (HtmlDataTable) uiData;
        
        if (table.isAjaxRowRender())
        {
            UIComponent row = table.getFacet("row");
            if (row != null)
            {
                writer.writeAttribute(HTML.ID_ATTR, row.getClientId(facesContext), null);
            }
        }
        
        Map<String, List<ClientBehavior>> clientBehaviors = ((ClientBehaviorHolder) uiData).getClientBehaviors();

        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONCLICK_ATTR, uiData,
                ROW_CLICK, clientBehaviors, HTML.ONCLICK_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONDBLCLICK_ATTR, uiData,
                ROW_DBLCLICK, clientBehaviors, HTML.ONDBLCLICK_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONKEYDOWN_ATTR, uiData,
                ROW_KEYDOWN, clientBehaviors, HTML.ONKEYDOWN_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONKEYPRESS_ATTR, uiData,
                ROW_KEYPRESS, clientBehaviors, HTML.ONKEYPRESS_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONKEYUP_ATTR, uiData,
                ROW_KEYUP, clientBehaviors, HTML.ONKEYUP_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONMOUSEDOWN_ATTR, uiData,
                ROW_MOUSEDOWN, clientBehaviors, HTML.ONMOUSEDOWN_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONMOUSEMOVE_ATTR, uiData,
                ROW_MOUSEMOVE, clientBehaviors, HTML.ONMOUSEMOVE_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONMOUSEOUT_ATTR, uiData,
                ROW_MOUSEOUT, clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONMOUSEOVER_ATTR, uiData,
                ROW_MOUSEOVER, clientBehaviors, HTML.ONMOUSEOVER_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, ROW_ONMOUSEUP_ATTR, uiData,
                ROW_MOUSEUP, clientBehaviors, HTML.ONMOUSEUP_ATTR);
        
        /*
        renderRowAttribute(writer, HTML.ONCLICK_ATTR, table.getRowOnClick());
        renderRowAttribute(writer, HTML.ONDBLCLICK_ATTR, table.getRowOnDblClick());
        renderRowAttribute(writer, HTML.ONKEYDOWN_ATTR, table.getRowOnKeyDown());
        renderRowAttribute(writer, HTML.ONKEYPRESS_ATTR, table.getRowOnKeyPress());
        renderRowAttribute(writer, HTML.ONKEYUP_ATTR, table.getRowOnKeyUp());
        renderRowAttribute(writer, HTML.ONMOUSEDOWN_ATTR, table.getRowOnMouseDown());
        renderRowAttribute(writer, HTML.ONMOUSEMOVE_ATTR, table.getRowOnMouseMove());
        renderRowAttribute(writer, HTML.ONMOUSEOUT_ATTR, table.getRowOnMouseOut());
        renderRowAttribute(writer, HTML.ONMOUSEOVER_ATTR, table.getRowOnMouseOver());
        renderRowAttribute(writer, HTML.ONMOUSEUP_ATTR, table.getRowOnMouseUp());
        */
    }

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#renderRowStyle(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, jakarta.faces.component.UIData, org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase.Styles, int)
     */
    protected void renderRowStyle(FacesContext facesContext, ResponseWriter writer, UIData uiData, Styles styles, int rowStyleIndex) throws IOException {
        String rowStyleClass;
        String rowStyle;
        if (uiData instanceof HtmlDataTable) {
            HtmlDataTable datatable = (HtmlDataTable) uiData;
            rowStyleClass = datatable.getRowStyleClass();
            rowStyle = datatable.getRowStyle();
        }
        else {
            rowStyleClass = (String) uiData.getAttributes().get(JSFAttr.ROW_STYLECLASS_ATTR);
            rowStyle = (String) uiData.getAttributes().get(JSFAttr.ROW_STYLE_ATTR);
        }
        if (rowStyleClass == null) {
            super.renderRowStyle(facesContext, writer, uiData, styles, rowStyleIndex);
        }
        else if (rowStyleClass != null && rowStyleClass.length() <= 0)
        {
            // TOMAHAWK-1628 rowStyleClass can hold an EL that return null and is coerced to empty string
            super.renderRowStyle(facesContext, writer, uiData, styles, rowStyleIndex);
        }
        else {
            writer.writeAttribute(HTML.CLASS_ATTR, rowStyleClass, null);
        }
        if (rowStyle != null) {
            writer.writeAttribute(HTML.STYLE_ATTR, rowStyle, null);
        }
    }

    protected void renderRowAttribute(ResponseWriter writer,
                                      String htmlAttribute, Object value) throws IOException {
        if (value != null) {
            writer.writeAttribute(htmlAttribute, value, null);
        }
    }

    /**
     * Render the specified column object using the current row data.
     * <p/>
     * When the component is a UIColumn object, the inherited method is
     * invoked to render a single table cell.
     * <p/>
     * In addition to the inherited functionality, support is implemented
     * here for UIColumns children. When a UIColumns child is encountered:
     * <pre>
     * For each dynamic column in that UIColumns child:
     *   * Select the column (which sets variable named by the var attribute
     *     to refer to the current column object)
     *   * Call this.renderColumnBody passing the UIColumns object.
     * </pre>
     * The renderColumnBody method eventually:
     * <ul>
     * <li>emits TD
     * <li>calls encodeBegin on the UIColumns (which does nothing)
     * <li>calls rendering methods on all children of the UIColumns
     * <li>calls encodeEnd on the UIColumns (which does nothing)
     * <li> emits /TD
     * </ul>
     * If the children of the UIColumns access the variable named by the var
     * attribute on the UIColumns object, then they end up rendering content
     * that is extracted from the current column object.
     *
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#encodeColumnChild(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, jakarta.faces.component.UIData, jakarta.faces.component.UIComponent, org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase.Styles, int)
     */
    protected void encodeColumnChild(FacesContext facesContext,
                                     ResponseWriter writer, UIData uiData,
                                     UIComponent component, Styles styles, int columnStyleIndex)
        throws IOException {        
        //columnStyleIndex param does not takes into
        //consideration UIColumns, since it is called from HtmlTableRendererBase.
        //So we need to recalculate its index taking into account that
        //UIColumns component count as ((UIColumns) child).getRowCount() instead 1 
        columnStyleIndex = getColumnStyleIndex(uiData, columnStyleIndex);
        super.encodeColumnChild(facesContext, writer, uiData, component,
            styles, columnStyleIndex);
        if (component instanceof UIColumns)
        {
            UIColumns columns = (UIColumns) component;
            for (int k = 0, colSize = columns.getRowCount(); k < colSize; k++)
            {
                columns.setRowIndex(k);
                renderColumnBody(facesContext, writer, uiData, component,
                    styles, columnStyleIndex + k);
            }
            columns.setRowIndex(-1);
        }
    }
    
    /**
      * This method calculates the correct columnStyleIndex taking the dynamic columns
      * of <b>UIColumns</b> into consideration
      * 
      * @param uiData
      * @param columnStyleIndex
      * @return the correct columnStyleIndex 
      */
    private int getColumnStyleIndex(UIData uiData, int columnStyleIndex)
    {
        int colStyleIndex = 0;
        for (int i = 0; i < columnStyleIndex; i++)
        {
            UIComponent child = (UIComponent) uiData.getChildren().get(i % uiData.getChildCount());
            if (child instanceof UIColumns)
            {
                colStyleIndex += ((UIColumns) child).getRowCount();
                continue;
            }
            colStyleIndex++;
        }
        return colStyleIndex;
    }
    

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#renderColumnBody(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, jakarta.faces.component.UIData, jakarta.faces.component.UIComponent, org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase.Styles, int)
     */
    protected void renderColumnBody(FacesContext facesContext,
                                    ResponseWriter writer, UIData uiData,
                                    UIComponent component, Styles styles, int columnStyleIndex)
        throws IOException {
        if (isGroupedTable(uiData)) {

            HtmlDataTable htmlDataTable = (HtmlDataTable) uiData;
            List tableChildren = htmlDataTable.getChildren();

            int first = htmlDataTable.getFirst();
            int rowInfoIndex = htmlDataTable.getRowIndex() - first;
            int columnInfoIndex = tableChildren.indexOf(component);

            RowInfo rowInfo = (RowInfo) htmlDataTable.getTableContext().getRowInfos().get(rowInfoIndex);
            ColumnInfo columnInfo = (ColumnInfo) rowInfo.getColumnInfos().get(columnInfoIndex);

            if (!columnInfo.isRendered()) {
                return;
            }

            if (component instanceof HtmlColumn && amISpannedOver(null, component)) {
                return;
            }
            writer.startElement(HTML.TD_ELEM, uiData);
            String styleClass = null;
            if (component instanceof HtmlColumn) {
                styleClass = ((HtmlColumn) component).getStyleClass();
            }
            if (columnInfo.getStyleClass() != null) {
                styleClass = columnInfo.getStyleClass();
            }

            if (styles.hasColumnStyle()) {
                if (styleClass == null) {
                    styleClass = styles.getColumnStyle(columnStyleIndex);
                }
            }
            if (styleClass != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
            }

            if (columnInfo.getStyle() != null) {
                writer.writeAttribute(HTML.STYLE_ATTR, columnInfo.getStyle(), null);
            }

            if (columnInfo.getRowSpan() > 1) {
                writer.writeAttribute("rowspan", new Integer(columnInfo.getRowSpan()).toString(), null);
                if (columnInfo.getStyle() == null) {
                    writer.writeAttribute(HTML.STYLE_ATTR, "vertical-align:top", null);
                }
            }

            renderHtmlColumnAttributes(facesContext, writer, component, null);

            RendererUtils.renderChild(facesContext, component);
            writer.endElement(HTML.TD_ELEM);
        }
        else if (component instanceof HtmlColumn) {
            if (amISpannedOver(null, component)) {
                return;
            }
            writer.startElement(HTML.TD_ELEM, uiData);
            String styleClass = ((HtmlColumn) component).getStyleClass();
            if (styles.hasColumnStyle()) {
                if (styleClass == null) {
                    styleClass = styles.getColumnStyle(columnStyleIndex);
                }
            }
            if (styleClass != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
            }
            renderHtmlColumnAttributes(facesContext, writer, component, null);

            RendererUtils.renderChild(facesContext, component);
            writer.endElement(HTML.TD_ELEM);
        }
        else {
            super.renderColumnBody(facesContext, writer, uiData, component,
                styles, columnStyleIndex);
        }
    }

    /**
     * Render the header or footer of the specified column object.
     * <p/>
     * When the component is a UIColumn object, the inherited method is
     * invoked to render a single header cell.
     * <p/>
     * In addition to the inherited functionality, support is implemented
     * here for UIColumns children. When a UIColumns child is encountered:
     * <pre>
     * For each dynamic column in that UIColumns child:
     *   * Select the column (which sets variable named by the var attribute
     *     to refer to the current column object)
     *   * Call this.renderColumnHeaderCell or this.renderColumnFooterCell
     *     passing the header or footer facet of the UIColumns object.
     * </pre>
     * If the facet of the UIColumns accesses the variable named by the var
     * attribute on the UIColumns object, then it ends up rendering content
     * that is extracted from the current column object.
     *
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#renderColumnChildHeaderOrFooterRow(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, jakarta.faces.component.UIComponent, java.lang.String, boolean)
     */
    protected void renderColumnChildHeaderOrFooterRow(
        FacesContext facesContext, ResponseWriter writer,
        UIComponent uiComponent, String styleClass, boolean header)
        throws IOException {
        super.renderColumnChildHeaderOrFooterRow(facesContext, writer,
            uiComponent, styleClass, header);
        if (uiComponent instanceof UIColumns) {
            UIColumns columns = (UIColumns) uiComponent;
            for (int i = 0, size = columns.getRowCount(); i < size; i++) {
                columns.setRowIndex(i);
                if (header) {
                    renderColumnHeaderCell(facesContext, writer, columns,
                        columns.getHeader(), styleClass, 0);
                }
                else {
                    renderColumnFooterCell(facesContext, writer, columns,
                        columns.getFooter(), styleClass, 0);
                }
            }
            columns.setRowIndex(-1);
        }
    }

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#renderColumnHeaderCell(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, jakarta.faces.component.UIComponent, jakarta.faces.component.UIComponent, java.lang.String, int)
     */
    protected void renderColumnHeaderCell(FacesContext facesContext,
                                          ResponseWriter writer, UIComponent uiComponent,
                                          UIComponent facet, String headerStyleClass, int colspan)
        throws IOException {
        if (uiComponent instanceof HtmlColumn) {

            HtmlColumn column = (HtmlColumn) uiComponent;

            if (amISpannedOver("header", uiComponent)) {
                return;
            }

            writer.startElement(determineHeaderCellTag(facesContext, uiComponent.getParent()), uiComponent);

            String columnId = column.getColumnId();
            if (columnId != null)
            {
                writer.writeAttribute(HTML.ID_ATTR, columnId, null);
            }

            if (colspan > 1) {
                writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(colspan),
                    null);
            }
            String styleClass = ((HtmlColumn) uiComponent)
                .getHeaderstyleClass();
            if (styleClass == null) {
                styleClass = headerStyleClass;
            }
            if (styleClass != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
            }
            renderHtmlColumnAttributes(facesContext, writer, uiComponent, "header");
            if (facet != null) {
                RendererUtils.renderChild(facesContext, facet);
            }
            writer.endElement(determineHeaderCellTag(facesContext, uiComponent.getParent()));
        }
        else {
            super.renderColumnHeaderCell(facesContext, writer, uiComponent,
                facet, headerStyleClass, colspan);
        }
    }

    /**
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#renderColumnFooterCell(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, jakarta.faces.component.UIComponent, jakarta.faces.component.UIComponent, java.lang.String, int)
     */
    protected void renderColumnFooterCell(FacesContext facesContext,
                                          ResponseWriter writer, UIComponent uiComponent,
                                          UIComponent facet, String footerStyleClass, int colspan)
        throws IOException {
        if (uiComponent instanceof HtmlColumn) {
            if (amISpannedOver("footer", uiComponent)) {
                return;
            }
            writer.startElement(HTML.TD_ELEM, uiComponent);
            if (colspan > 1) {
                writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(colspan),
                    null);
            }
            String styleClass = ((HtmlColumn) uiComponent)
                .getFooterstyleClass();
            if (styleClass == null) {
                styleClass = footerStyleClass;
            }
            if (styleClass != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
            }
            renderHtmlColumnAttributes(facesContext, writer, uiComponent, "footer");
            if (facet != null) {
                RendererUtils.renderChild(facesContext, facet);
            }
            writer.endElement(HTML.TD_ELEM);
        }
        else {
            super.renderColumnFooterCell(facesContext, writer, uiComponent,
                facet, footerStyleClass, colspan);
        }
    }

    protected void renderHtmlColumnAttributes(FacesContext facesContext, ResponseWriter writer,
                                              UIComponent uiComponent, String prefix) throws IOException {
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
        }
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            for (int i = 0, size = COLUMN_ATTRIBUTES_WITHOUT_EVENTS.length; i < size; i++) {
                String attributeName = COLUMN_ATTRIBUTES_WITHOUT_EVENTS[i];
                String compAttrName = prefix != null ? prefix + attributeName : attributeName;
                HtmlRendererUtils.renderHTMLAttribute(writer, uiComponent,
                    compAttrName, attributeName);
            }
            
            for (int i = 0, size = EVENTS.length; i < size; i++)
            {
                String attributeName = EVENTS_ATTRIBUTES[i];
                String eventName = prefix != null ? prefix + EVENTS[i] : EVENTS[i];
                String compAttrName = prefix != null ? prefix + attributeName : attributeName;
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer,
                        compAttrName, uiComponent, eventName, behaviors, attributeName);
            }
        }
        else
        {
            for (int i = 0, size = COLUMN_ATTRIBUTES.length; i < size; i++) {
                String attributeName = COLUMN_ATTRIBUTES[i];
                String compAttrName = prefix != null ? prefix + attributeName : attributeName;
                HtmlRendererUtils.renderHTMLAttribute(writer, uiComponent,
                    compAttrName, attributeName);
            }
        }
        String compAttrName = prefix != null ? prefix + HTML.STYLE_ATTR : HTML.STYLE_ATTR;
        HtmlRendererUtils.renderHTMLAttribute(writer, uiComponent,
            compAttrName, HTML.STYLE_ATTR);

        HtmlRendererUtils.renderHTMLAttribute(writer, uiComponent,
            HTML.WIDTH_ATTR, HTML.WIDTH_ATTR);
    }

    /**
     * Return the number of columns spanned by the specified component.
     * <p/>
     * For normal components, use the inherited implementation.
     * For UIColumns children, return the number of dynamic columns rendered
     * by that child.
     *
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase
     *      #determineChildColSpan(jakarta.faces.component.UIComponent)
     */
    protected int determineChildColSpan(UIComponent uiComponent) {
        int result = super.determineChildColSpan(uiComponent);
        if (uiComponent instanceof UIColumns) {
            result += ((UIColumns) uiComponent).getRowCount();
        }
        return result;
    }

    /**
     * Return true if the specified component has a facet that needs to be
     * rendered in a THEAD or TFOOT section.
     *
     * @see org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase#hasFacet(boolean, jakarta.faces.component.UIComponent)
     */
    protected boolean hasFacet(boolean header, UIComponent uiComponent) {
        boolean result = super.hasFacet(header, uiComponent);
        if (!result && uiComponent instanceof UIColumns) {
            // Why is this necessary? It seems to me that the inherited
            // implementation will work fine with a UIColumns component...
            UIColumns columns = (UIColumns) uiComponent;
            result = header ? columns.getHeader() != null : columns.getFooter() != null;
        }
        return result;
    }

    /**
     * Renders the column footer.
     * Rendering will be supressed if all of the facets have rendered="false"
     */
    protected void renderColumnFooterRow(FacesContext facesContext, ResponseWriter writer, UIComponent component, String footerStyleClass) throws IOException {
        if (determineRenderFacet(component, false)) {
            super.renderColumnFooterRow(facesContext, writer, component, footerStyleClass);
        }
    }

    /**
     * Renders the column header.
     * Rendering will be supressed if all of the facets have rendered="false"
     */
    protected void renderColumnHeaderRow(FacesContext facesContext, ResponseWriter writer, UIComponent component, String headerStyleClass) throws IOException {
        if (determineRenderFacet(component, true)) {
            super.renderColumnHeaderRow(facesContext, writer, component, headerStyleClass);
        }
    }

    /**
     * determine if the header or footer should be rendered.
     */
    protected boolean determineRenderFacet(UIComponent component, boolean header) {
        for (Iterator it = getChildren(component).iterator(); it.hasNext();) {
            UIComponent uiComponent = (UIComponent) it.next();
            if (uiComponent.isRendered() && determineChildColSpan(uiComponent) > 0) {
                UIComponent facet = header ? (UIComponent) uiComponent.getFacets().get(HEADER_FACET_NAME)
                    : (UIComponent) uiComponent.getFacets().get(FOOTER_FACET_NAME);

                if (facet != null && facet.isRendered()) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void beforeColumn(FacesContext facesContext, UIData uiData, int columnIndex) throws IOException {
        super.beforeColumn(facesContext, uiData, columnIndex);

        if (uiData instanceof HtmlDataTable) {
            HtmlDataTable dataTable = (HtmlDataTable) uiData;

            putSortedReqScopeParam(facesContext, dataTable, columnIndex);
        }
    }

    protected void beforeColumnHeaderOrFooter(FacesContext facesContext, UIData uiData, boolean header, int columnIndex) throws IOException {
        super.beforeColumnHeaderOrFooter(facesContext, uiData, header, columnIndex);

        if (header && uiData instanceof HtmlDataTable) {
            HtmlDataTable dataTable = (HtmlDataTable) uiData;

            putSortedReqScopeParam(facesContext, dataTable, columnIndex);
        }
    }

    protected void putSortedReqScopeParam(FacesContext facesContext, HtmlDataTable dataTable, int columnIndex) {
        String sortedColumnVar = dataTable.getSortedColumnVar();
        Map requestMap = facesContext.getExternalContext().getRequestMap();

        if (columnIndex == dataTable.getSortColumnIndex()) {
            if (sortedColumnVar != null) {
                requestMap.put(sortedColumnVar, Boolean.TRUE);
            }
        }
        else if (sortedColumnVar != null) {
            requestMap.remove(sortedColumnVar);
        }
    }

    /**
     * specify if the header, footer or <td> is spanned over (not shown) because
     * of a colspan in a cell in a previous column
     *
     * @param prefix    header, footer or null
     * @param component
     */
    protected boolean amISpannedOver(String prefix, UIComponent component) {
        UIComponent table = component.getParent();
        int span = 0;
        for (Iterator it = table.getChildren().iterator(); it.hasNext();) {
            UIComponent columnComponent = (UIComponent) it.next();
            if (!(columnComponent instanceof HtmlColumn)) {
                continue;
            }
            if (span > 0) {
                span--;
            }
            if (columnComponent == component) {
                return span > 0;
            }
            if (span == 0) {
                try {
                    if (prefix == null && ((HtmlColumn) columnComponent).getColspan() != null) {
                        span = Integer.parseInt(((HtmlColumn) columnComponent).getColspan());
                    }
                    if ("header".equals(prefix) && ((HtmlColumn) columnComponent).getHeadercolspan() != null) {
                        span = Integer.parseInt(((HtmlColumn) columnComponent).getHeadercolspan());
                    }
                    if ("footer".equals(prefix) && ((HtmlColumn) columnComponent).getFootercolspan() != null) {
                        span = Integer.parseInt(((HtmlColumn) columnComponent).getFootercolspan());
                    }
                }
                catch (NumberFormatException ex) {
                    log.warn("Invalid " + ((prefix == null) ? "" : prefix) + "colspan attribute ignored");
                }
            }
        }
        return false;
    }


    /**
     * Perform any operations necessary in the TBODY start tag.
     *
     * @param facesContext the <code>FacesContext</code>.
     * @param uiData       the <code>UIData</code> being rendered.
     */
    protected void inBodyStart(FacesContext facesContext, UIData uiData) throws IOException {
        String bodyStyleClass;
        String bodyStyle;

        if (uiData instanceof HtmlDataTable) {
            bodyStyleClass = ((HtmlDataTable) uiData).getBodyStyleClass();
            bodyStyle = ((HtmlDataTable) uiData).getBodyStyle();
        }
        else {
            bodyStyleClass = (String) uiData.getAttributes().get(BODY_STYLE_CLASS);
            bodyStyle = (String) uiData.getAttributes().get(BODY_STYLE);
        }

        ResponseWriter writer = facesContext.getResponseWriter();
        if (bodyStyleClass != null) {
            writer.writeAttribute(HTML.CLASS_ATTR, bodyStyleClass,
                BODY_STYLE_CLASS);
        }
        if (bodyStyle != null) {
            writer.writeAttribute(HTML.STYLE_ATTR, bodyStyle, BODY_STYLE);
        }
    }


}
