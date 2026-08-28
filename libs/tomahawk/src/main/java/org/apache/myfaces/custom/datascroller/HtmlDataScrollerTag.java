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
package org.apache.myfaces.custom.datascroller;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.datascroller.AbstractHtmlDataScroller.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlDataScrollerTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlDataScrollerTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlDataScroller";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.DataScroller";
    }

    private ValueExpression _layout;
    
    public void setLayout(ValueExpression layout)
    {
        _layout = layout;
    }
    private ValueExpression _colspan;
    
    public void setColspan(ValueExpression colspan)
    {
        _colspan = colspan;
    }
    private ValueExpression _onclick;
    
    public void setOnclick(ValueExpression onclick)
    {
        _onclick = onclick;
    }
    private ValueExpression _ondblclick;
    
    public void setOndblclick(ValueExpression ondblclick)
    {
        _ondblclick = ondblclick;
    }
    private MethodExpression _actionExpression;
    
    public void setAction(MethodExpression actionExpression)
    {
        _actionExpression = actionExpression;
    }
    private jakarta.el.MethodExpression _actionListener;
    
    public void setActionListener(jakarta.el.MethodExpression actionListener)
    {
        _actionListener = actionListener;
    }
    private ValueExpression _for;
    
    public void setFor(ValueExpression forParam)
    {
        _for = forParam;
    }
    private ValueExpression _fastStep;
    
    public void setFastStep(ValueExpression fastStep)
    {
        _fastStep = fastStep;
    }
    private ValueExpression _pageIndexVar;
    
    public void setPageIndexVar(ValueExpression pageIndexVar)
    {
        _pageIndexVar = pageIndexVar;
    }
    private ValueExpression _pageCountVar;
    
    public void setPageCountVar(ValueExpression pageCountVar)
    {
        _pageCountVar = pageCountVar;
    }
    private ValueExpression _rowsCountVar;
    
    public void setRowsCountVar(ValueExpression rowsCountVar)
    {
        _rowsCountVar = rowsCountVar;
    }
    private ValueExpression _displayedRowsCountVar;
    
    public void setDisplayedRowsCountVar(ValueExpression displayedRowsCountVar)
    {
        _displayedRowsCountVar = displayedRowsCountVar;
    }
    private ValueExpression _firstRowIndexVar;
    
    public void setFirstRowIndexVar(ValueExpression firstRowIndexVar)
    {
        _firstRowIndexVar = firstRowIndexVar;
    }
    private ValueExpression _lastRowIndexVar;
    
    public void setLastRowIndexVar(ValueExpression lastRowIndexVar)
    {
        _lastRowIndexVar = lastRowIndexVar;
    }
    private ValueExpression _paginator;
    
    public void setPaginator(ValueExpression paginator)
    {
        _paginator = paginator;
    }
    private ValueExpression _paginatorMaxPages;
    
    public void setPaginatorMaxPages(ValueExpression paginatorMaxPages)
    {
        _paginatorMaxPages = paginatorMaxPages;
    }
    private ValueExpression _paginatorTableClass;
    
    public void setPaginatorTableClass(ValueExpression paginatorTableClass)
    {
        _paginatorTableClass = paginatorTableClass;
    }
    private ValueExpression _paginatorTableStyle;
    
    public void setPaginatorTableStyle(ValueExpression paginatorTableStyle)
    {
        _paginatorTableStyle = paginatorTableStyle;
    }
    private ValueExpression _paginatorColumnClass;
    
    public void setPaginatorColumnClass(ValueExpression paginatorColumnClass)
    {
        _paginatorColumnClass = paginatorColumnClass;
    }
    private ValueExpression _paginatorColumnStyle;
    
    public void setPaginatorColumnStyle(ValueExpression paginatorColumnStyle)
    {
        _paginatorColumnStyle = paginatorColumnStyle;
    }
    private ValueExpression _paginatorActiveColumnClass;
    
    public void setPaginatorActiveColumnClass(ValueExpression paginatorActiveColumnClass)
    {
        _paginatorActiveColumnClass = paginatorActiveColumnClass;
    }
    private ValueExpression _paginatorRenderLinkForActive;
    
    public void setPaginatorRenderLinkForActive(ValueExpression paginatorRenderLinkForActive)
    {
        _paginatorRenderLinkForActive = paginatorRenderLinkForActive;
    }
    private ValueExpression _firstStyleClass;
    
    public void setFirstStyleClass(ValueExpression firstStyleClass)
    {
        _firstStyleClass = firstStyleClass;
    }
    private ValueExpression _lastStyleClass;
    
    public void setLastStyleClass(ValueExpression lastStyleClass)
    {
        _lastStyleClass = lastStyleClass;
    }
    private ValueExpression _previousStyleClass;
    
    public void setPreviousStyleClass(ValueExpression previousStyleClass)
    {
        _previousStyleClass = previousStyleClass;
    }
    private ValueExpression _nextStyleClass;
    
    public void setNextStyleClass(ValueExpression nextStyleClass)
    {
        _nextStyleClass = nextStyleClass;
    }
    private ValueExpression _fastfStyleClass;
    
    public void setFastfStyleClass(ValueExpression fastfStyleClass)
    {
        _fastfStyleClass = fastfStyleClass;
    }
    private ValueExpression _fastrStyleClass;
    
    public void setFastrStyleClass(ValueExpression fastrStyleClass)
    {
        _fastrStyleClass = fastrStyleClass;
    }
    private ValueExpression _paginatorActiveColumnStyle;
    
    public void setPaginatorActiveColumnStyle(ValueExpression paginatorActiveColumnStyle)
    {
        _paginatorActiveColumnStyle = paginatorActiveColumnStyle;
    }
    private ValueExpression _renderFacetsIfSinglePage;
    
    public void setRenderFacetsIfSinglePage(ValueExpression renderFacetsIfSinglePage)
    {
        _renderFacetsIfSinglePage = renderFacetsIfSinglePage;
    }
    private ValueExpression _immediate;
    
    public void setImmediate(ValueExpression immediate)
    {
        _immediate = immediate;
    }
    private ValueExpression _disableFacetLinksIfFirstPage;
    
    public void setDisableFacetLinksIfFirstPage(ValueExpression disableFacetLinksIfFirstPage)
    {
        _disableFacetLinksIfFirstPage = disableFacetLinksIfFirstPage;
    }
    private ValueExpression _disableFacetLinksIfLastPage;
    
    public void setDisableFacetLinksIfLastPage(ValueExpression disableFacetLinksIfLastPage)
    {
        _disableFacetLinksIfLastPage = disableFacetLinksIfLastPage;
    }
    private ValueExpression _renderFacetLinksIfFirstPage;
    
    public void setRenderFacetLinksIfFirstPage(ValueExpression renderFacetLinksIfFirstPage)
    {
        _renderFacetLinksIfFirstPage = renderFacetLinksIfFirstPage;
    }
    private ValueExpression _renderFacetLinksIfLastPage;
    
    public void setRenderFacetLinksIfLastPage(ValueExpression renderFacetLinksIfLastPage)
    {
        _renderFacetLinksIfLastPage = renderFacetLinksIfLastPage;
    }
    private ValueExpression _displayValueOnly;
    
    public void setDisplayValueOnly(ValueExpression displayValueOnly)
    {
        _displayValueOnly = displayValueOnly;
    }
    private ValueExpression _displayValueOnlyStyle;
    
    public void setDisplayValueOnlyStyle(ValueExpression displayValueOnlyStyle)
    {
        _displayValueOnlyStyle = displayValueOnlyStyle;
    }
    private ValueExpression _displayValueOnlyStyleClass;
    
    public void setDisplayValueOnlyStyleClass(ValueExpression displayValueOnlyStyleClass)
    {
        _displayValueOnlyStyleClass = displayValueOnlyStyleClass;
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
    private ValueExpression _dir;
    
    public void setDir(ValueExpression dir)
    {
        _dir = dir;
    }
    private ValueExpression _lang;
    
    public void setLang(ValueExpression lang)
    {
        _lang = lang;
    }
    private ValueExpression _title;
    
    public void setTitle(ValueExpression title)
    {
        _title = title;
    }
    private ValueExpression _style;
    
    public void setStyle(ValueExpression style)
    {
        _style = style;
    }
    private ValueExpression _styleClass;
    
    public void setStyleClass(ValueExpression styleClass)
    {
        _styleClass = styleClass;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.datascroller.HtmlDataScroller))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.datascroller.HtmlDataScroller");
        }
        
        org.apache.myfaces.custom.datascroller.HtmlDataScroller comp = (org.apache.myfaces.custom.datascroller.HtmlDataScroller) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_layout != null)
        {
            comp.setValueExpression("layout", _layout);
        } 
        if (_colspan != null)
        {
            comp.setValueExpression("colspan", _colspan);
        } 
        if (_onclick != null)
        {
            comp.setValueExpression("onclick", _onclick);
        } 
        if (_ondblclick != null)
        {
            comp.setValueExpression("ondblclick", _ondblclick);
        } 
        if (_actionExpression != null)
        {
            comp.setActionExpression(_actionExpression);
        }        
        if (_actionListener != null)
        {
            comp.addActionListener(new MethodExpressionActionListener(_actionListener));
        }
        if (_for != null)
        {
            comp.setValueExpression("for", _for);
        } 
        if (_fastStep != null)
        {
            comp.setValueExpression("fastStep", _fastStep);
        } 
        if (_pageIndexVar != null)
        {
            comp.setValueExpression("pageIndexVar", _pageIndexVar);
        } 
        if (_pageCountVar != null)
        {
            comp.setValueExpression("pageCountVar", _pageCountVar);
        } 
        if (_rowsCountVar != null)
        {
            comp.setValueExpression("rowsCountVar", _rowsCountVar);
        } 
        if (_displayedRowsCountVar != null)
        {
            comp.setValueExpression("displayedRowsCountVar", _displayedRowsCountVar);
        } 
        if (_firstRowIndexVar != null)
        {
            comp.setValueExpression("firstRowIndexVar", _firstRowIndexVar);
        } 
        if (_lastRowIndexVar != null)
        {
            comp.setValueExpression("lastRowIndexVar", _lastRowIndexVar);
        } 
        if (_paginator != null)
        {
            comp.setValueExpression("paginator", _paginator);
        } 
        if (_paginatorMaxPages != null)
        {
            comp.setValueExpression("paginatorMaxPages", _paginatorMaxPages);
        } 
        if (_paginatorTableClass != null)
        {
            comp.setValueExpression("paginatorTableClass", _paginatorTableClass);
        } 
        if (_paginatorTableStyle != null)
        {
            comp.setValueExpression("paginatorTableStyle", _paginatorTableStyle);
        } 
        if (_paginatorColumnClass != null)
        {
            comp.setValueExpression("paginatorColumnClass", _paginatorColumnClass);
        } 
        if (_paginatorColumnStyle != null)
        {
            comp.setValueExpression("paginatorColumnStyle", _paginatorColumnStyle);
        } 
        if (_paginatorActiveColumnClass != null)
        {
            comp.setValueExpression("paginatorActiveColumnClass", _paginatorActiveColumnClass);
        } 
        if (_paginatorRenderLinkForActive != null)
        {
            comp.setValueExpression("paginatorRenderLinkForActive", _paginatorRenderLinkForActive);
        } 
        if (_firstStyleClass != null)
        {
            comp.setValueExpression("firstStyleClass", _firstStyleClass);
        } 
        if (_lastStyleClass != null)
        {
            comp.setValueExpression("lastStyleClass", _lastStyleClass);
        } 
        if (_previousStyleClass != null)
        {
            comp.setValueExpression("previousStyleClass", _previousStyleClass);
        } 
        if (_nextStyleClass != null)
        {
            comp.setValueExpression("nextStyleClass", _nextStyleClass);
        } 
        if (_fastfStyleClass != null)
        {
            comp.setValueExpression("fastfStyleClass", _fastfStyleClass);
        } 
        if (_fastrStyleClass != null)
        {
            comp.setValueExpression("fastrStyleClass", _fastrStyleClass);
        } 
        if (_paginatorActiveColumnStyle != null)
        {
            comp.setValueExpression("paginatorActiveColumnStyle", _paginatorActiveColumnStyle);
        } 
        if (_renderFacetsIfSinglePage != null)
        {
            comp.setValueExpression("renderFacetsIfSinglePage", _renderFacetsIfSinglePage);
        } 
        if (_immediate != null)
        {
            comp.setValueExpression("immediate", _immediate);
        } 
        if (_disableFacetLinksIfFirstPage != null)
        {
            comp.setValueExpression("disableFacetLinksIfFirstPage", _disableFacetLinksIfFirstPage);
        } 
        if (_disableFacetLinksIfLastPage != null)
        {
            comp.setValueExpression("disableFacetLinksIfLastPage", _disableFacetLinksIfLastPage);
        } 
        if (_renderFacetLinksIfFirstPage != null)
        {
            comp.setValueExpression("renderFacetLinksIfFirstPage", _renderFacetLinksIfFirstPage);
        } 
        if (_renderFacetLinksIfLastPage != null)
        {
            comp.setValueExpression("renderFacetLinksIfLastPage", _renderFacetLinksIfLastPage);
        } 
        if (_displayValueOnly != null)
        {
            comp.setValueExpression("displayValueOnly", _displayValueOnly);
        } 
        if (_displayValueOnlyStyle != null)
        {
            comp.setValueExpression("displayValueOnlyStyle", _displayValueOnlyStyle);
        } 
        if (_displayValueOnlyStyleClass != null)
        {
            comp.setValueExpression("displayValueOnlyStyleClass", _displayValueOnlyStyleClass);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_forceId != null)
        {
            comp.getAttributes().put("forceId", Boolean.valueOf(_forceId));
        } 
        if (_forceIdIndex != null)
        {
            comp.getAttributes().put("forceIdIndex", Boolean.valueOf(_forceIdIndex));
        } 
        if (_dir != null)
        {
            comp.setValueExpression("dir", _dir);
        } 
        if (_lang != null)
        {
            comp.setValueExpression("lang", _lang);
        } 
        if (_title != null)
        {
            comp.setValueExpression("title", _title);
        } 
        if (_style != null)
        {
            comp.setValueExpression("style", _style);
        } 
        if (_styleClass != null)
        {
            comp.setValueExpression("styleClass", _styleClass);
        } 
    }

    public void release()
    {
        super.release();
        _layout = null;
        _colspan = null;
        _onclick = null;
        _ondblclick = null;
        _actionExpression = null;
        _actionListener = null;
        _for = null;
        _fastStep = null;
        _pageIndexVar = null;
        _pageCountVar = null;
        _rowsCountVar = null;
        _displayedRowsCountVar = null;
        _firstRowIndexVar = null;
        _lastRowIndexVar = null;
        _paginator = null;
        _paginatorMaxPages = null;
        _paginatorTableClass = null;
        _paginatorTableStyle = null;
        _paginatorColumnClass = null;
        _paginatorColumnStyle = null;
        _paginatorActiveColumnClass = null;
        _paginatorRenderLinkForActive = null;
        _firstStyleClass = null;
        _lastStyleClass = null;
        _previousStyleClass = null;
        _nextStyleClass = null;
        _fastfStyleClass = null;
        _fastrStyleClass = null;
        _paginatorActiveColumnStyle = null;
        _renderFacetsIfSinglePage = null;
        _immediate = null;
        _disableFacetLinksIfFirstPage = null;
        _disableFacetLinksIfLastPage = null;
        _renderFacetLinksIfFirstPage = null;
        _renderFacetLinksIfLastPage = null;
        _displayValueOnly = null;
        _displayValueOnlyStyle = null;
        _displayValueOnlyStyleClass = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _forceId = null;
        _forceIdIndex = null;
        _dir = null;
        _lang = null;
        _title = null;
        _style = null;
        _styleClass = null;
    }
}
