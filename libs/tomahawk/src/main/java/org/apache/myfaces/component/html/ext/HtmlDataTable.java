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
package org.apache.myfaces.component.html.ext;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.component.html.ext.AbstractHtmlDataTable.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlDataTable extends org.apache.myfaces.component.html.ext.AbstractHtmlDataTable
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Data";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlDataTable";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.Table";


    public HtmlDataTable()
    {
        setRendererType("org.apache.myfaces.Table");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    static private final java.util.Collection<String> CLIENT_EVENTS_LIST = 
        java.util.Collections.unmodifiableCollection(
            java.util.Arrays.asList(
             "rowMouseOver"
            , "rowMouseOut"
            , "rowClick"
            , "rowDblClick"
            , "rowKeyDown"
            , "rowKeyPress"
            , "rowKeyUp"
            , "rowMouseDown"
            , "rowMouseMove"
            , "rowMouseUp"
            , "click"
            , "dblclick"
            , "keydown"
            , "keypress"
            , "keyup"
            , "mousedown"
            , "mousemove"
            , "mouseout"
            , "mouseover"
            , "mouseup"
        ));

    public java.util.Collection<String> getEventNames()
    {
        return CLIENT_EVENTS_LIST;
    }

    @Override
    public void addClientBehavior(String eventName, jakarta.faces.component.behavior.ClientBehavior behavior)
    {
        super.addClientBehavior(eventName, behavior);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonEventConstants.markEvent(this, eventName);
    }

    
    // Property: sortProperty
    public String getSortProperty()
    {
        return (String) getStateHelper().get(PropertyKeys.sortProperty);        
    }
    
    public void setSortProperty(String sortProperty)
    {
        getStateHelper().put(PropertyKeys.sortProperty, sortProperty ); 
    }    
    // Property: sortable
    public boolean isSortable()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.sortable, false);
    }
    
    public void setSortable(boolean sortable)
    {
        getStateHelper().put(PropertyKeys.sortable, sortable ); 
    }    
    // Property: embedded
    public boolean isEmbedded()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.embedded, false);
    }
    
    public void setEmbedded(boolean embedded)
    {
        getStateHelper().put(PropertyKeys.embedded, embedded ); 
    }    
    // Property: detailStampExpandedDefault
    public boolean isDetailStampExpandedDefault()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.detailStampExpandedDefault, false);
    }
    
    public void setDetailStampExpandedDefault(boolean detailStampExpandedDefault)
    {
        getStateHelper().put(PropertyKeys.detailStampExpandedDefault, detailStampExpandedDefault ); 
    }    
    // Property: detailStampLocation
    public String getDetailStampLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.detailStampLocation, "after");
    }
    
    public void setDetailStampLocation(String detailStampLocation)
    {
        getStateHelper().put(PropertyKeys.detailStampLocation, detailStampLocation ); 
    }    
    // Property: rowOnMouseOver
    public String getRowOnMouseOver()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnMouseOver);
    }
    
    public void setRowOnMouseOver(String rowOnMouseOver)
    {
        getStateHelper().put(PropertyKeys.rowOnMouseOver, rowOnMouseOver ); 
    }    
    // Property: rowOnMouseOut
    public String getRowOnMouseOut()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnMouseOut);
    }
    
    public void setRowOnMouseOut(String rowOnMouseOut)
    {
        getStateHelper().put(PropertyKeys.rowOnMouseOut, rowOnMouseOut ); 
    }    
    // Property: rowOnClick
    public String getRowOnClick()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnClick);
    }
    
    public void setRowOnClick(String rowOnClick)
    {
        getStateHelper().put(PropertyKeys.rowOnClick, rowOnClick ); 
    }    
    // Property: rowOnDblClick
    public String getRowOnDblClick()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnDblClick);
    }
    
    public void setRowOnDblClick(String rowOnDblClick)
    {
        getStateHelper().put(PropertyKeys.rowOnDblClick, rowOnDblClick ); 
    }    
    // Property: rowOnKeyDown
    public String getRowOnKeyDown()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnKeyDown);
    }
    
    public void setRowOnKeyDown(String rowOnKeyDown)
    {
        getStateHelper().put(PropertyKeys.rowOnKeyDown, rowOnKeyDown ); 
    }    
    // Property: rowOnKeyPress
    public String getRowOnKeyPress()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnKeyPress);
    }
    
    public void setRowOnKeyPress(String rowOnKeyPress)
    {
        getStateHelper().put(PropertyKeys.rowOnKeyPress, rowOnKeyPress ); 
    }    
    // Property: rowOnKeyUp
    public String getRowOnKeyUp()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnKeyUp);
    }
    
    public void setRowOnKeyUp(String rowOnKeyUp)
    {
        getStateHelper().put(PropertyKeys.rowOnKeyUp, rowOnKeyUp ); 
    }    
    // Property: rowStyleClass
    public String getRowStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowStyleClass);
    }
    
    public void setRowStyleClass(String rowStyleClass)
    {
        getStateHelper().put(PropertyKeys.rowStyleClass, rowStyleClass ); 
    }    
    // Property: rowStyle
    public String getRowStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowStyle);
    }
    
    public void setRowStyle(String rowStyle)
    {
        getStateHelper().put(PropertyKeys.rowStyle, rowStyle ); 
    }    
    // Property: rowOnMouseDown
    public String getRowOnMouseDown()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnMouseDown);
    }
    
    public void setRowOnMouseDown(String rowOnMouseDown)
    {
        getStateHelper().put(PropertyKeys.rowOnMouseDown, rowOnMouseDown ); 
    }    
    // Property: rowOnMouseMove
    public String getRowOnMouseMove()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnMouseMove);
    }
    
    public void setRowOnMouseMove(String rowOnMouseMove)
    {
        getStateHelper().put(PropertyKeys.rowOnMouseMove, rowOnMouseMove ); 
    }    
    // Property: rowOnMouseUp
    public String getRowOnMouseUp()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowOnMouseUp);
    }
    
    public void setRowOnMouseUp(String rowOnMouseUp)
    {
        getStateHelper().put(PropertyKeys.rowOnMouseUp, rowOnMouseUp ); 
    }    
    // Property: rowGroupStyle
    public String getRowGroupStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowGroupStyle);
    }
    
    public void setRowGroupStyle(String rowGroupStyle)
    {
        getStateHelper().put(PropertyKeys.rowGroupStyle, rowGroupStyle ); 
    }    
    // Property: rowGroupStyleClass
    public String getRowGroupStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowGroupStyleClass);
    }
    
    public void setRowGroupStyleClass(String rowGroupStyleClass)
    {
        getStateHelper().put(PropertyKeys.rowGroupStyleClass, rowGroupStyleClass ); 
    }    
    // Property: bodyStyle
    public String getBodyStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.bodyStyle);
    }
    
    public void setBodyStyle(String bodyStyle)
    {
        getStateHelper().put(PropertyKeys.bodyStyle, bodyStyle ); 
    }    
    // Property: bodyStyleClass
    public String getBodyStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.bodyStyleClass);
    }
    
    public void setBodyStyleClass(String bodyStyleClass)
    {
        getStateHelper().put(PropertyKeys.bodyStyleClass, bodyStyleClass ); 
    }    
    // Property: newspaperColumns
    public int getNewspaperColumns()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.newspaperColumns, 1);
    }
    
    public void setNewspaperColumns(int newspaperColumns)
    {
        getStateHelper().put(PropertyKeys.newspaperColumns, newspaperColumns ); 
    }    
    // Property: newspaperOrientation
    public String getNewspaperOrientation()
    {
        return (String) getStateHelper().eval(PropertyKeys.newspaperOrientation, "vertical");
    }
    
    public void setNewspaperOrientation(String newspaperOrientation)
    {
        getStateHelper().put(PropertyKeys.newspaperOrientation, newspaperOrientation ); 
    }    
    // Property: preserveDataModel
    public boolean isPreserveDataModel()
    {
        Object value = (Boolean) getStateHelper().eval(PropertyKeys.preserveDataModel);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;
    }
    
    public void setPreserveDataModel(boolean preserveDataModel)
    {
        getStateHelper().put(PropertyKeys.preserveDataModel, preserveDataModel ); 
    }    
    // Property: preserveSort
    protected boolean isSetPreserveSort()
    {
        return getStateHelper().get(PropertyKeys.preserveSort) != null;
    }
    public boolean isPreserveSort()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.preserveSort, true);
    }
    
    public void setPreserveSort(boolean preserveSort)
    {
        getStateHelper().put(PropertyKeys.preserveSort, preserveSort ); 
    }    
    // Property: renderedIfEmpty
    public boolean isRenderedIfEmpty()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderedIfEmpty, true);
    }
    
    public void setRenderedIfEmpty(boolean renderedIfEmpty)
    {
        getStateHelper().put(PropertyKeys.renderedIfEmpty, renderedIfEmpty ); 
    }    
    // Property: rowIndexVar
    public String getRowIndexVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowIndexVar);
    }
    
    public void setRowIndexVar(String rowIndexVar)
    {
        getStateHelper().put(PropertyKeys.rowIndexVar, rowIndexVar ); 
    }    
    // Property: rowCountVar
    public String getRowCountVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowCountVar);
    }
    
    public void setRowCountVar(String rowCountVar)
    {
        getStateHelper().put(PropertyKeys.rowCountVar, rowCountVar ); 
    }    
    // Property: previousRowDataVar
    public String getPreviousRowDataVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.previousRowDataVar);
    }
    
    public void setPreviousRowDataVar(String previousRowDataVar)
    {
        getStateHelper().put(PropertyKeys.previousRowDataVar, previousRowDataVar ); 
    }    
    // Property: sortedColumnVar
    public String getSortedColumnVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.sortedColumnVar);
    }
    
    public void setSortedColumnVar(String sortedColumnVar)
    {
        getStateHelper().put(PropertyKeys.sortedColumnVar, sortedColumnVar ); 
    }    
    // Property: align
    public String getAlign()
    {
        return (String) getStateHelper().eval(PropertyKeys.align);
    }
    
    public void setAlign(String align)
    {
        getStateHelper().put(PropertyKeys.align, align ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ALIGN_PROP);
    }    
    // Property: rowId
    public String getRowId()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowId);
    }
    
    public void setRowId(String rowId)
    {
        getStateHelper().put(PropertyKeys.rowId, rowId ); 
    }    
    // Property: datafld
    public String getDatafld()
    {
        return (String) getStateHelper().eval(PropertyKeys.datafld);
    }
    
    public void setDatafld(String datafld)
    {
        getStateHelper().put(PropertyKeys.datafld, datafld ); 
    }    
    // Property: datasrc
    public String getDatasrc()
    {
        return (String) getStateHelper().eval(PropertyKeys.datasrc);
    }
    
    public void setDatasrc(String datasrc)
    {
        getStateHelper().put(PropertyKeys.datasrc, datasrc ); 
    }    
    // Property: dataformatas
    public String getDataformatas()
    {
        return (String) getStateHelper().eval(PropertyKeys.dataformatas);
    }
    
    public void setDataformatas(String dataformatas)
    {
        getStateHelper().put(PropertyKeys.dataformatas, dataformatas ); 
    }    
    // Property: valueType
    public String getValueType()
    {
        return (String) getStateHelper().eval(PropertyKeys.valueType);
    }
    
    public void setValueType(String valueType)
    {
        getStateHelper().put(PropertyKeys.valueType, valueType ); 
    }    
    // Property: ajaxRowRender
    public boolean isAjaxRowRender()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.ajaxRowRender, false);
    }
    
    public void setAjaxRowRender(boolean ajaxRowRender)
    {
        getStateHelper().put(PropertyKeys.ajaxRowRender, ajaxRowRender ); 
    }    
    // Property: ajaxBodyRender
    public boolean isAjaxBodyRender()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.ajaxBodyRender, false);
    }
    
    public void setAjaxBodyRender(boolean ajaxBodyRender)
    {
        getStateHelper().put(PropertyKeys.ajaxBodyRender, ajaxBodyRender ); 
    }    
    // Property: enabledOnUserRole
    public String getEnabledOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.enabledOnUserRole);
    }
    
    public void setEnabledOnUserRole(String enabledOnUserRole)
    {
        getStateHelper().put(PropertyKeys.enabledOnUserRole, enabledOnUserRole ); 
    }    
    // Property: visibleOnUserRole
    public String getVisibleOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.visibleOnUserRole);
    }
    
    public void setVisibleOnUserRole(String visibleOnUserRole)
    {
        getStateHelper().put(PropertyKeys.visibleOnUserRole, visibleOnUserRole ); 
    }    
    // Property: style
    public String getStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.style);
    }
    
    public void setStyle(String style)
    {
        getStateHelper().put(PropertyKeys.style, style ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLE_PROP);
    }    
    // Property: styleClass
    public String getStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }
    
    public void setStyleClass(String styleClass)
    {
        getStateHelper().put(PropertyKeys.styleClass, styleClass ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLECLASS_PROP);
    }    
    // Property: onclick
    public String getOnclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.onclick);
    }
    
    public void setOnclick(String onclick)
    {
        getStateHelper().put(PropertyKeys.onclick, onclick ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONCLICK_PROP);
    }    
    // Property: ondblclick
    public String getOndblclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.ondblclick);
    }
    
    public void setOndblclick(String ondblclick)
    {
        getStateHelper().put(PropertyKeys.ondblclick, ondblclick ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONDBLCLICK_PROP);
    }    
    // Property: onkeydown
    public String getOnkeydown()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeydown);
    }
    
    public void setOnkeydown(String onkeydown)
    {
        getStateHelper().put(PropertyKeys.onkeydown, onkeydown ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYDOWN_PROP);
    }    
    // Property: onkeypress
    public String getOnkeypress()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeypress);
    }
    
    public void setOnkeypress(String onkeypress)
    {
        getStateHelper().put(PropertyKeys.onkeypress, onkeypress ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYPRESS_PROP);
    }    
    // Property: onkeyup
    public String getOnkeyup()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeyup);
    }
    
    public void setOnkeyup(String onkeyup)
    {
        getStateHelper().put(PropertyKeys.onkeyup, onkeyup ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYUP_PROP);
    }    
    // Property: onmousedown
    public String getOnmousedown()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmousedown);
    }
    
    public void setOnmousedown(String onmousedown)
    {
        getStateHelper().put(PropertyKeys.onmousedown, onmousedown ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEDOWN_PROP);
    }    
    // Property: onmousemove
    public String getOnmousemove()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmousemove);
    }
    
    public void setOnmousemove(String onmousemove)
    {
        getStateHelper().put(PropertyKeys.onmousemove, onmousemove ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEMOVE_PROP);
    }    
    // Property: onmouseout
    public String getOnmouseout()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseout);
    }
    
    public void setOnmouseout(String onmouseout)
    {
        getStateHelper().put(PropertyKeys.onmouseout, onmouseout ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEOUT_PROP);
    }    
    // Property: onmouseover
    public String getOnmouseover()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseover);
    }
    
    public void setOnmouseover(String onmouseover)
    {
        getStateHelper().put(PropertyKeys.onmouseover, onmouseover ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEOVER_PROP);
    }    
    // Property: onmouseup
    public String getOnmouseup()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseup);
    }
    
    public void setOnmouseup(String onmouseup)
    {
        getStateHelper().put(PropertyKeys.onmouseup, onmouseup ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEUP_PROP);
    }    
    // Property: dir
    public String getDir()
    {
        return (String) getStateHelper().eval(PropertyKeys.dir);
    }
    
    public void setDir(String dir)
    {
        getStateHelper().put(PropertyKeys.dir, dir ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.DIR_PROP);
    }    
    // Property: lang
    public String getLang()
    {
        return (String) getStateHelper().eval(PropertyKeys.lang);
    }
    
    public void setLang(String lang)
    {
        getStateHelper().put(PropertyKeys.lang, lang ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.LANG_PROP);
    }    
    // Property: title
    public String getTitle()
    {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }
    
    public void setTitle(String title)
    {
        getStateHelper().put(PropertyKeys.title, title ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.TITLE_PROP);
    }    

    public void setValueBinding(String name, jakarta.faces.el.ValueBinding binding)
    {
        super.setValueBinding(name, binding);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    public void setValueExpression(String name, ValueExpression expression)
    {
        super.setValueExpression(name, expression);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    protected enum PropertyKeys
    {
         sortProperty
        , sortable
        , embedded
        , detailStampExpandedDefault
        , detailStampLocation
        , rowOnMouseOver
        , rowOnMouseOut
        , rowOnClick
        , rowOnDblClick
        , rowOnKeyDown
        , rowOnKeyPress
        , rowOnKeyUp
        , rowStyleClass
        , rowStyle
        , rowOnMouseDown
        , rowOnMouseMove
        , rowOnMouseUp
        , rowGroupStyle
        , rowGroupStyleClass
        , bodyStyle
        , bodyStyleClass
        , newspaperColumns
        , newspaperOrientation
        , preserveDataModel
        , preserveSort
        , renderedIfEmpty
        , rowIndexVar
        , rowCountVar
        , previousRowDataVar
        , sortedColumnVar
        , align
        , rowId
        , datafld
        , datasrc
        , dataformatas
        , valueType
        , ajaxRowRender
        , ajaxBodyRender
        , enabledOnUserRole
        , visibleOnUserRole
        , style
        , styleClass
        , onclick
        , ondblclick
        , onkeydown
        , onkeypress
        , onkeyup
        , onmousedown
        , onmousemove
        , onmouseout
        , onmouseover
        , onmouseup
        , dir
        , lang
        , title
    }

 }
