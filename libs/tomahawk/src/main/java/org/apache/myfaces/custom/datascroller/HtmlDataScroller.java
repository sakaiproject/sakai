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

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.datascroller.AbstractHtmlDataScroller.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlDataScroller extends org.apache.myfaces.custom.datascroller.AbstractHtmlDataScroller
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Panel";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlDataScroller";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.DataScroller";


    public HtmlDataScroller()
    {
        setRendererType("org.apache.myfaces.DataScroller");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    static private final java.util.Collection<String> CLIENT_EVENTS_LIST = 
        java.util.Collections.unmodifiableCollection(
            java.util.Arrays.asList(
             "click"
            , "dblclick"
            , "action"
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

    //ClientBehaviorHolder default: action
    public String getDefaultEventName()
    {
        return "action";
    }

    
    // Property: layout
    public String getLayout()
    {
        return (String) getStateHelper().eval(PropertyKeys.layout, "table");
    }
    
    public void setLayout(String layout)
    {
        getStateHelper().put(PropertyKeys.layout, layout ); 
    }    
    // Property: colspan
    public int getColspan()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.colspan, Integer.MIN_VALUE);
    }
    
    public void setColspan(int colspan)
    {
        getStateHelper().put(PropertyKeys.colspan, colspan ); 
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
    // Property: for
    public String getFor()
    {
        return (String) getStateHelper().eval(PropertyKeys.forVal);
    }
    
    public void setFor(String forParam)
    {
        getStateHelper().put(PropertyKeys.forVal, forParam ); 
    }    
    // Property: fastStep
    public int getFastStep()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.fastStep, Integer.MIN_VALUE);
    }
    
    public void setFastStep(int fastStep)
    {
        getStateHelper().put(PropertyKeys.fastStep, fastStep ); 
    }    
    // Property: pageIndexVar
    public String getPageIndexVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.pageIndexVar);
    }
    
    public void setPageIndexVar(String pageIndexVar)
    {
        getStateHelper().put(PropertyKeys.pageIndexVar, pageIndexVar ); 
    }    
    // Property: pageCountVar
    public String getPageCountVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.pageCountVar);
    }
    
    public void setPageCountVar(String pageCountVar)
    {
        getStateHelper().put(PropertyKeys.pageCountVar, pageCountVar ); 
    }    
    // Property: rowsCountVar
    public String getRowsCountVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.rowsCountVar);
    }
    
    public void setRowsCountVar(String rowsCountVar)
    {
        getStateHelper().put(PropertyKeys.rowsCountVar, rowsCountVar ); 
    }    
    // Property: displayedRowsCountVar
    public String getDisplayedRowsCountVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.displayedRowsCountVar);
    }
    
    public void setDisplayedRowsCountVar(String displayedRowsCountVar)
    {
        getStateHelper().put(PropertyKeys.displayedRowsCountVar, displayedRowsCountVar ); 
    }    
    // Property: firstRowIndexVar
    public String getFirstRowIndexVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.firstRowIndexVar);
    }
    
    public void setFirstRowIndexVar(String firstRowIndexVar)
    {
        getStateHelper().put(PropertyKeys.firstRowIndexVar, firstRowIndexVar ); 
    }    
    // Property: lastRowIndexVar
    public String getLastRowIndexVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.lastRowIndexVar);
    }
    
    public void setLastRowIndexVar(String lastRowIndexVar)
    {
        getStateHelper().put(PropertyKeys.lastRowIndexVar, lastRowIndexVar ); 
    }    
    // Property: paginator
    public boolean isPaginator()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.paginator, false);
    }
    
    public void setPaginator(boolean paginator)
    {
        getStateHelper().put(PropertyKeys.paginator, paginator ); 
    }    
    // Property: paginatorMaxPages
    public int getPaginatorMaxPages()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.paginatorMaxPages, Integer.MIN_VALUE);
    }
    
    public void setPaginatorMaxPages(int paginatorMaxPages)
    {
        getStateHelper().put(PropertyKeys.paginatorMaxPages, paginatorMaxPages ); 
    }    
    // Property: paginatorTableClass
    public String getPaginatorTableClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.paginatorTableClass);
    }
    
    public void setPaginatorTableClass(String paginatorTableClass)
    {
        getStateHelper().put(PropertyKeys.paginatorTableClass, paginatorTableClass ); 
    }    
    // Property: paginatorTableStyle
    public String getPaginatorTableStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.paginatorTableStyle);
    }
    
    public void setPaginatorTableStyle(String paginatorTableStyle)
    {
        getStateHelper().put(PropertyKeys.paginatorTableStyle, paginatorTableStyle ); 
    }    
    // Property: paginatorColumnClass
    public String getPaginatorColumnClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.paginatorColumnClass);
    }
    
    public void setPaginatorColumnClass(String paginatorColumnClass)
    {
        getStateHelper().put(PropertyKeys.paginatorColumnClass, paginatorColumnClass ); 
    }    
    // Property: paginatorColumnStyle
    public String getPaginatorColumnStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.paginatorColumnStyle);
    }
    
    public void setPaginatorColumnStyle(String paginatorColumnStyle)
    {
        getStateHelper().put(PropertyKeys.paginatorColumnStyle, paginatorColumnStyle ); 
    }    
    // Property: paginatorActiveColumnClass
    public String getPaginatorActiveColumnClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.paginatorActiveColumnClass);
    }
    
    public void setPaginatorActiveColumnClass(String paginatorActiveColumnClass)
    {
        getStateHelper().put(PropertyKeys.paginatorActiveColumnClass, paginatorActiveColumnClass ); 
    }    
    // Property: paginatorRenderLinkForActive
    public boolean isPaginatorRenderLinkForActive()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.paginatorRenderLinkForActive, true);
    }
    
    public void setPaginatorRenderLinkForActive(boolean paginatorRenderLinkForActive)
    {
        getStateHelper().put(PropertyKeys.paginatorRenderLinkForActive, paginatorRenderLinkForActive ); 
    }    
    // Property: firstStyleClass
    public String getFirstStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.firstStyleClass);
    }
    
    public void setFirstStyleClass(String firstStyleClass)
    {
        getStateHelper().put(PropertyKeys.firstStyleClass, firstStyleClass ); 
    }    
    // Property: lastStyleClass
    public String getLastStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.lastStyleClass);
    }
    
    public void setLastStyleClass(String lastStyleClass)
    {
        getStateHelper().put(PropertyKeys.lastStyleClass, lastStyleClass ); 
    }    
    // Property: previousStyleClass
    public String getPreviousStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.previousStyleClass);
    }
    
    public void setPreviousStyleClass(String previousStyleClass)
    {
        getStateHelper().put(PropertyKeys.previousStyleClass, previousStyleClass ); 
    }    
    // Property: nextStyleClass
    public String getNextStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.nextStyleClass);
    }
    
    public void setNextStyleClass(String nextStyleClass)
    {
        getStateHelper().put(PropertyKeys.nextStyleClass, nextStyleClass ); 
    }    
    // Property: fastfStyleClass
    public String getFastfStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.fastfStyleClass);
    }
    
    public void setFastfStyleClass(String fastfStyleClass)
    {
        getStateHelper().put(PropertyKeys.fastfStyleClass, fastfStyleClass ); 
    }    
    // Property: fastrStyleClass
    public String getFastrStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.fastrStyleClass);
    }
    
    public void setFastrStyleClass(String fastrStyleClass)
    {
        getStateHelper().put(PropertyKeys.fastrStyleClass, fastrStyleClass ); 
    }    
    // Property: paginatorActiveColumnStyle
    public String getPaginatorActiveColumnStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.paginatorActiveColumnStyle);
    }
    
    public void setPaginatorActiveColumnStyle(String paginatorActiveColumnStyle)
    {
        getStateHelper().put(PropertyKeys.paginatorActiveColumnStyle, paginatorActiveColumnStyle ); 
    }    
    // Property: renderFacetsIfSinglePage
    public boolean isRenderFacetsIfSinglePage()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderFacetsIfSinglePage, true);
    }
    
    public void setRenderFacetsIfSinglePage(boolean renderFacetsIfSinglePage)
    {
        getStateHelper().put(PropertyKeys.renderFacetsIfSinglePage, renderFacetsIfSinglePage ); 
    }    
    // Property: immediate
    public boolean isImmediate()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);
    }
    
    public void setImmediate(boolean immediate)
    {
        getStateHelper().put(PropertyKeys.immediate, immediate ); 
    }    
    // Property: disableFacetLinksIfFirstPage
    public boolean isDisableFacetLinksIfFirstPage()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.disableFacetLinksIfFirstPage, false);
    }
    
    public void setDisableFacetLinksIfFirstPage(boolean disableFacetLinksIfFirstPage)
    {
        getStateHelper().put(PropertyKeys.disableFacetLinksIfFirstPage, disableFacetLinksIfFirstPage ); 
    }    
    // Property: disableFacetLinksIfLastPage
    public boolean isDisableFacetLinksIfLastPage()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.disableFacetLinksIfLastPage, false);
    }
    
    public void setDisableFacetLinksIfLastPage(boolean disableFacetLinksIfLastPage)
    {
        getStateHelper().put(PropertyKeys.disableFacetLinksIfLastPage, disableFacetLinksIfLastPage ); 
    }    
    // Property: renderFacetLinksIfFirstPage
    public boolean isRenderFacetLinksIfFirstPage()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderFacetLinksIfFirstPage, true);
    }
    
    public void setRenderFacetLinksIfFirstPage(boolean renderFacetLinksIfFirstPage)
    {
        getStateHelper().put(PropertyKeys.renderFacetLinksIfFirstPage, renderFacetLinksIfFirstPage ); 
    }    
    // Property: renderFacetLinksIfLastPage
    public boolean isRenderFacetLinksIfLastPage()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderFacetLinksIfLastPage, true);
    }
    
    public void setRenderFacetLinksIfLastPage(boolean renderFacetLinksIfLastPage)
    {
        getStateHelper().put(PropertyKeys.renderFacetLinksIfLastPage, renderFacetLinksIfLastPage ); 
    }    
    // Property: displayValueOnly
    public Boolean getDisplayValueOnly()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.displayValueOnly);
    }
    
    public void setDisplayValueOnly(Boolean displayValueOnly)
    {
        getStateHelper().put(PropertyKeys.displayValueOnly, displayValueOnly ); 
    }    
    // Property: displayValueOnlyStyle
    public String getDisplayValueOnlyStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.displayValueOnlyStyle);
    }
    
    public void setDisplayValueOnlyStyle(String displayValueOnlyStyle)
    {
        getStateHelper().put(PropertyKeys.displayValueOnlyStyle, displayValueOnlyStyle ); 
    }    
    // Property: displayValueOnlyStyleClass
    public String getDisplayValueOnlyStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.displayValueOnlyStyleClass);
    }
    
    public void setDisplayValueOnlyStyleClass(String displayValueOnlyStyleClass)
    {
        getStateHelper().put(PropertyKeys.displayValueOnlyStyleClass, displayValueOnlyStyleClass ); 
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
    // Property: forceId
    public boolean isForceId()
    {
        Object value = getStateHelper().get(PropertyKeys.forceId);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;        
    }
    
    public void setForceId(boolean forceId)
    {
        getStateHelper().put(PropertyKeys.forceId, forceId ); 
    }    
    // Property: forceIdIndex
    public boolean isForceIdIndex()
    {
        Object value = getStateHelper().get(PropertyKeys.forceIdIndex);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return true;        
    }
    
    public void setForceIdIndex(boolean forceIdIndex)
    {
        getStateHelper().put(PropertyKeys.forceIdIndex, forceIdIndex ); 
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
         layout
        , colspan
        , onclick
        , ondblclick
        , forVal("for")
        , fastStep
        , pageIndexVar
        , pageCountVar
        , rowsCountVar
        , displayedRowsCountVar
        , firstRowIndexVar
        , lastRowIndexVar
        , paginator
        , paginatorMaxPages
        , paginatorTableClass
        , paginatorTableStyle
        , paginatorColumnClass
        , paginatorColumnStyle
        , paginatorActiveColumnClass
        , paginatorRenderLinkForActive
        , firstStyleClass
        , lastStyleClass
        , previousStyleClass
        , nextStyleClass
        , fastfStyleClass
        , fastrStyleClass
        , paginatorActiveColumnStyle
        , renderFacetsIfSinglePage
        , immediate
        , disableFacetLinksIfFirstPage
        , disableFacetLinksIfLastPage
        , renderFacetLinksIfFirstPage
        , renderFacetLinksIfLastPage
        , displayValueOnly
        , displayValueOnlyStyle
        , displayValueOnlyStyleClass
        , enabledOnUserRole
        , visibleOnUserRole
        , forceId
        , forceIdIndex
        , dir
        , lang
        , title
        , style
        , styleClass
        ;
        String c;
        
        PropertyKeys()
        {
        }
        
        //Constructor needed by "for" property
        PropertyKeys(String c)
        { 
            this.c = c;
        }
        
        public String toString()
        {
            return ((this.c != null) ? this.c : super.toString());
        }
    }

 }
