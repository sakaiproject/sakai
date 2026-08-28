// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.generated.taglib.html.ext;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.component.html.ext.AbstractHtmlDataTable.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlDataTableTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlDataTableTag
{
    public HtmlDataTableTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlDataTable";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Table";
    }

    private ValueExpression _first;
    
    public void setFirst(ValueExpression first)
    {
        _first = first;
    }
    private ValueExpression _rows;
    
    public void setRows(ValueExpression rows)
    {
        _rows = rows;
    }
    private ValueExpression _forceIdIndexFormula;
    
    public void setForceIdIndexFormula(ValueExpression forceIdIndexFormula)
    {
        _forceIdIndexFormula = forceIdIndexFormula;
    }
    private ValueExpression _sortColumn;
    
    public void setSortColumn(ValueExpression sortColumn)
    {
        _sortColumn = sortColumn;
    }
    private ValueExpression _sortAscending;
    
    public void setSortAscending(ValueExpression sortAscending)
    {
        _sortAscending = sortAscending;
    }
    private ValueExpression _sortable;
    
    public void setSortable(ValueExpression sortable)
    {
        _sortable = sortable;
    }
    private ValueExpression _embedded;
    
    public void setEmbedded(ValueExpression embedded)
    {
        _embedded = embedded;
    }
    private ValueExpression _detailStampExpandedDefault;
    
    public void setDetailStampExpandedDefault(ValueExpression detailStampExpandedDefault)
    {
        _detailStampExpandedDefault = detailStampExpandedDefault;
    }
    private ValueExpression _detailStampLocation;
    
    public void setDetailStampLocation(ValueExpression detailStampLocation)
    {
        _detailStampLocation = detailStampLocation;
    }
    private ValueExpression _rowOnMouseOver;
    
    public void setRowOnMouseOver(ValueExpression rowOnMouseOver)
    {
        _rowOnMouseOver = rowOnMouseOver;
    }
    private ValueExpression _rowOnMouseOut;
    
    public void setRowOnMouseOut(ValueExpression rowOnMouseOut)
    {
        _rowOnMouseOut = rowOnMouseOut;
    }
    private ValueExpression _rowOnClick;
    
    public void setRowOnClick(ValueExpression rowOnClick)
    {
        _rowOnClick = rowOnClick;
    }
    private ValueExpression _rowOnDblClick;
    
    public void setRowOnDblClick(ValueExpression rowOnDblClick)
    {
        _rowOnDblClick = rowOnDblClick;
    }
    private ValueExpression _rowOnKeyDown;
    
    public void setRowOnKeyDown(ValueExpression rowOnKeyDown)
    {
        _rowOnKeyDown = rowOnKeyDown;
    }
    private ValueExpression _rowOnKeyPress;
    
    public void setRowOnKeyPress(ValueExpression rowOnKeyPress)
    {
        _rowOnKeyPress = rowOnKeyPress;
    }
    private ValueExpression _rowOnKeyUp;
    
    public void setRowOnKeyUp(ValueExpression rowOnKeyUp)
    {
        _rowOnKeyUp = rowOnKeyUp;
    }
    private ValueExpression _rowStyleClass;
    
    public void setRowStyleClass(ValueExpression rowStyleClass)
    {
        _rowStyleClass = rowStyleClass;
    }
    private ValueExpression _rowStyle;
    
    public void setRowStyle(ValueExpression rowStyle)
    {
        _rowStyle = rowStyle;
    }
    private ValueExpression _rowOnMouseDown;
    
    public void setRowOnMouseDown(ValueExpression rowOnMouseDown)
    {
        _rowOnMouseDown = rowOnMouseDown;
    }
    private ValueExpression _rowOnMouseMove;
    
    public void setRowOnMouseMove(ValueExpression rowOnMouseMove)
    {
        _rowOnMouseMove = rowOnMouseMove;
    }
    private ValueExpression _rowOnMouseUp;
    
    public void setRowOnMouseUp(ValueExpression rowOnMouseUp)
    {
        _rowOnMouseUp = rowOnMouseUp;
    }
    private ValueExpression _varDetailToggler;
    
    public void setVarDetailToggler(ValueExpression varDetailToggler)
    {
        _varDetailToggler = varDetailToggler;
    }
    private ValueExpression _rowGroupStyle;
    
    public void setRowGroupStyle(ValueExpression rowGroupStyle)
    {
        _rowGroupStyle = rowGroupStyle;
    }
    private ValueExpression _rowGroupStyleClass;
    
    public void setRowGroupStyleClass(ValueExpression rowGroupStyleClass)
    {
        _rowGroupStyleClass = rowGroupStyleClass;
    }
    private ValueExpression _bodyStyle;
    
    public void setBodyStyle(ValueExpression bodyStyle)
    {
        _bodyStyle = bodyStyle;
    }
    private ValueExpression _bodyStyleClass;
    
    public void setBodyStyleClass(ValueExpression bodyStyleClass)
    {
        _bodyStyleClass = bodyStyleClass;
    }
    private ValueExpression _newspaperColumns;
    
    public void setNewspaperColumns(ValueExpression newspaperColumns)
    {
        _newspaperColumns = newspaperColumns;
    }
    private ValueExpression _newspaperOrientation;
    
    public void setNewspaperOrientation(ValueExpression newspaperOrientation)
    {
        _newspaperOrientation = newspaperOrientation;
    }
    private ValueExpression _preserveDataModel;
    
    public void setPreserveDataModel(ValueExpression preserveDataModel)
    {
        _preserveDataModel = preserveDataModel;
    }
    private ValueExpression _preserveSort;
    
    public void setPreserveSort(ValueExpression preserveSort)
    {
        _preserveSort = preserveSort;
    }
    private ValueExpression _renderedIfEmpty;
    
    public void setRenderedIfEmpty(ValueExpression renderedIfEmpty)
    {
        _renderedIfEmpty = renderedIfEmpty;
    }
    private ValueExpression _rowIndexVar;
    
    public void setRowIndexVar(ValueExpression rowIndexVar)
    {
        _rowIndexVar = rowIndexVar;
    }
    private ValueExpression _rowCountVar;
    
    public void setRowCountVar(ValueExpression rowCountVar)
    {
        _rowCountVar = rowCountVar;
    }
    private ValueExpression _previousRowDataVar;
    
    public void setPreviousRowDataVar(ValueExpression previousRowDataVar)
    {
        _previousRowDataVar = previousRowDataVar;
    }
    private ValueExpression _sortedColumnVar;
    
    public void setSortedColumnVar(ValueExpression sortedColumnVar)
    {
        _sortedColumnVar = sortedColumnVar;
    }
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }
    private ValueExpression _rowId;
    
    public void setRowId(ValueExpression rowId)
    {
        _rowId = rowId;
    }
    private ValueExpression _datafld;
    
    public void setDatafld(ValueExpression datafld)
    {
        _datafld = datafld;
    }
    private ValueExpression _datasrc;
    
    public void setDatasrc(ValueExpression datasrc)
    {
        _datasrc = datasrc;
    }
    private ValueExpression _dataformatas;
    
    public void setDataformatas(ValueExpression dataformatas)
    {
        _dataformatas = dataformatas;
    }
    private ValueExpression _valueType;
    
    public void setValueType(ValueExpression valueType)
    {
        _valueType = valueType;
    }
    private ValueExpression _ajaxRowRender;
    
    public void setAjaxRowRender(ValueExpression ajaxRowRender)
    {
        _ajaxRowRender = ajaxRowRender;
    }
    private ValueExpression _ajaxBodyRender;
    
    public void setAjaxBodyRender(ValueExpression ajaxBodyRender)
    {
        _ajaxBodyRender = ajaxBodyRender;
    }
    private ValueExpression _enabledOnUserRole;
    
    public void setEnabledOnUserRole(ValueExpression enabledOnUserRole)
    {
        _enabledOnUserRole = enabledOnUserRole;
    }
    private ValueExpression _visibleOnUserRole;
    
    public void setVisibleOnUserRole(ValueExpression visibleOnUserRole)
    {
        _visibleOnUserRole = visibleOnUserRole;
    }
    private ValueExpression _preserveRowStates;
    
    public void setPreserveRowStates(ValueExpression preserveRowStates)
    {
        _preserveRowStates = preserveRowStates;
    }
    private String _forceId;
    
    public void setForceId(String forceId)
    {
        _forceId = forceId;
    }
    private String _forceIdIndex;
    
    public void setForceIdIndex(String forceIdIndex)
    {
        _forceIdIndex = forceIdIndex;
    }
    private String _preserveRowComponentState;
    
    public void setPreserveRowComponentState(String preserveRowComponentState)
    {
        _preserveRowComponentState = preserveRowComponentState;
    }
    private ValueExpression _rowKey;
    
    public void setRowKey(ValueExpression rowKey)
    {
        _rowKey = rowKey;
    }
    private ValueExpression _derivedRowKeyPrefix;
    
    public void setDerivedRowKeyPrefix(ValueExpression derivedRowKeyPrefix)
    {
        _derivedRowKeyPrefix = derivedRowKeyPrefix;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.component.html.ext.HtmlDataTable))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.component.html.ext.HtmlDataTable");
        }
        
        org.apache.myfaces.component.html.ext.HtmlDataTable comp = (org.apache.myfaces.component.html.ext.HtmlDataTable) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_first != null)
        {
            comp.setValueExpression("first", _first);
        } 
        if (_rows != null)
        {
            comp.setValueExpression("rows", _rows);
        } 
        if (_forceIdIndexFormula != null)
        {
            comp.setValueExpression("forceIdIndexFormula", _forceIdIndexFormula);
        } 
        if (_sortColumn != null)
        {
            comp.setValueExpression("sortColumn", _sortColumn);
        } 
        if (_sortAscending != null)
        {
            comp.setValueExpression("sortAscending", _sortAscending);
        } 
        if (_sortable != null)
        {
            comp.setValueExpression("sortable", _sortable);
        } 
        if (_embedded != null)
        {
            comp.setValueExpression("embedded", _embedded);
        } 
        if (_detailStampExpandedDefault != null)
        {
            comp.setValueExpression("detailStampExpandedDefault", _detailStampExpandedDefault);
        } 
        if (_detailStampLocation != null)
        {
            comp.setValueExpression("detailStampLocation", _detailStampLocation);
        } 
        if (_rowOnMouseOver != null)
        {
            comp.setValueExpression("rowOnMouseOver", _rowOnMouseOver);
        } 
        if (_rowOnMouseOut != null)
        {
            comp.setValueExpression("rowOnMouseOut", _rowOnMouseOut);
        } 
        if (_rowOnClick != null)
        {
            comp.setValueExpression("rowOnClick", _rowOnClick);
        } 
        if (_rowOnDblClick != null)
        {
            comp.setValueExpression("rowOnDblClick", _rowOnDblClick);
        } 
        if (_rowOnKeyDown != null)
        {
            comp.setValueExpression("rowOnKeyDown", _rowOnKeyDown);
        } 
        if (_rowOnKeyPress != null)
        {
            comp.setValueExpression("rowOnKeyPress", _rowOnKeyPress);
        } 
        if (_rowOnKeyUp != null)
        {
            comp.setValueExpression("rowOnKeyUp", _rowOnKeyUp);
        } 
        if (_rowStyleClass != null)
        {
            comp.setValueExpression("rowStyleClass", _rowStyleClass);
        } 
        if (_rowStyle != null)
        {
            comp.setValueExpression("rowStyle", _rowStyle);
        } 
        if (_rowOnMouseDown != null)
        {
            comp.setValueExpression("rowOnMouseDown", _rowOnMouseDown);
        } 
        if (_rowOnMouseMove != null)
        {
            comp.setValueExpression("rowOnMouseMove", _rowOnMouseMove);
        } 
        if (_rowOnMouseUp != null)
        {
            comp.setValueExpression("rowOnMouseUp", _rowOnMouseUp);
        } 
        if (_varDetailToggler != null)
        {
            comp.setValueExpression("varDetailToggler", _varDetailToggler);
        } 
        if (_rowGroupStyle != null)
        {
            comp.setValueExpression("rowGroupStyle", _rowGroupStyle);
        } 
        if (_rowGroupStyleClass != null)
        {
            comp.setValueExpression("rowGroupStyleClass", _rowGroupStyleClass);
        } 
        if (_bodyStyle != null)
        {
            comp.setValueExpression("bodyStyle", _bodyStyle);
        } 
        if (_bodyStyleClass != null)
        {
            comp.setValueExpression("bodyStyleClass", _bodyStyleClass);
        } 
        if (_newspaperColumns != null)
        {
            comp.setValueExpression("newspaperColumns", _newspaperColumns);
        } 
        if (_newspaperOrientation != null)
        {
            comp.setValueExpression("newspaperOrientation", _newspaperOrientation);
        } 
        if (_preserveDataModel != null)
        {
            comp.setValueExpression("preserveDataModel", _preserveDataModel);
        } 
        if (_preserveSort != null)
        {
            comp.setValueExpression("preserveSort", _preserveSort);
        } 
        if (_renderedIfEmpty != null)
        {
            comp.setValueExpression("renderedIfEmpty", _renderedIfEmpty);
        } 
        if (_rowIndexVar != null)
        {
            comp.setValueExpression("rowIndexVar", _rowIndexVar);
        } 
        if (_rowCountVar != null)
        {
            comp.setValueExpression("rowCountVar", _rowCountVar);
        } 
        if (_previousRowDataVar != null)
        {
            comp.setValueExpression("previousRowDataVar", _previousRowDataVar);
        } 
        if (_sortedColumnVar != null)
        {
            comp.setValueExpression("sortedColumnVar", _sortedColumnVar);
        } 
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
        if (_rowId != null)
        {
            comp.setValueExpression("rowId", _rowId);
        } 
        if (_datafld != null)
        {
            comp.setValueExpression("datafld", _datafld);
        } 
        if (_datasrc != null)
        {
            comp.setValueExpression("datasrc", _datasrc);
        } 
        if (_dataformatas != null)
        {
            comp.setValueExpression("dataformatas", _dataformatas);
        } 
        if (_valueType != null)
        {
            comp.setValueExpression("valueType", _valueType);
        } 
        if (_ajaxRowRender != null)
        {
            comp.setValueExpression("ajaxRowRender", _ajaxRowRender);
        } 
        if (_ajaxBodyRender != null)
        {
            comp.setValueExpression("ajaxBodyRender", _ajaxBodyRender);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_preserveRowStates != null)
        {
            comp.setValueExpression("preserveRowStates", _preserveRowStates);
        } 
        if (_forceId != null)
        {
            comp.getAttributes().put("forceId", Boolean.valueOf(_forceId));
        } 
        if (_forceIdIndex != null)
        {
            comp.getAttributes().put("forceIdIndex", Boolean.valueOf(_forceIdIndex));
        } 
        if (_preserveRowComponentState != null)
        {
            comp.getAttributes().put("preserveRowComponentState", Boolean.valueOf(_preserveRowComponentState));
        } 
        if (_rowKey != null)
        {
            comp.setValueExpression("rowKey", _rowKey);
        } 
        if (_derivedRowKeyPrefix != null)
        {
            comp.setValueExpression("derivedRowKeyPrefix", _derivedRowKeyPrefix);
        } 
    }

    public void release()
    {
        super.release();
        _first = null;
        _rows = null;
        _forceIdIndexFormula = null;
        _sortColumn = null;
        _sortAscending = null;
        _sortable = null;
        _embedded = null;
        _detailStampExpandedDefault = null;
        _detailStampLocation = null;
        _rowOnMouseOver = null;
        _rowOnMouseOut = null;
        _rowOnClick = null;
        _rowOnDblClick = null;
        _rowOnKeyDown = null;
        _rowOnKeyPress = null;
        _rowOnKeyUp = null;
        _rowStyleClass = null;
        _rowStyle = null;
        _rowOnMouseDown = null;
        _rowOnMouseMove = null;
        _rowOnMouseUp = null;
        _varDetailToggler = null;
        _rowGroupStyle = null;
        _rowGroupStyleClass = null;
        _bodyStyle = null;
        _bodyStyleClass = null;
        _newspaperColumns = null;
        _newspaperOrientation = null;
        _preserveDataModel = null;
        _preserveSort = null;
        _renderedIfEmpty = null;
        _rowIndexVar = null;
        _rowCountVar = null;
        _previousRowDataVar = null;
        _sortedColumnVar = null;
        _align = null;
        _rowId = null;
        _datafld = null;
        _datasrc = null;
        _dataformatas = null;
        _valueType = null;
        _ajaxRowRender = null;
        _ajaxBodyRender = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _preserveRowStates = null;
        _forceId = null;
        _forceIdIndex = null;
        _preserveRowComponentState = null;
        _rowKey = null;
        _derivedRowKeyPrefix = null;
    }
}
