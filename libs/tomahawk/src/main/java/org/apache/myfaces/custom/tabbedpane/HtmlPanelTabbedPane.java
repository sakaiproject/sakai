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
package org.apache.myfaces.custom.tabbedpane;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tabbedpane.AbstractHtmlPanelTabbedPane.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlPanelTabbedPane extends org.apache.myfaces.custom.tabbedpane.AbstractHtmlPanelTabbedPane
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Panel";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlPanelTabbedPane";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.TabbedPane";


    public HtmlPanelTabbedPane()
    {
        setRendererType("org.apache.myfaces.TabbedPane");
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

    
    // Property: activeTabVar
    public String getActiveTabVar()
    {
        return (String) getStateHelper().eval(PropertyKeys.activeTabVar);
    }
    
    public void setActiveTabVar(String activeTabVar)
    {
        getStateHelper().put(PropertyKeys.activeTabVar, activeTabVar ); 
    }    
    // Property: activePanelTabVar
    public Boolean getActivePanelTabVar()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.activePanelTabVar);
    }
    
    public void setActivePanelTabVar(Boolean activePanelTabVar)
    {
        getStateHelper().put(PropertyKeys.activePanelTabVar, activePanelTabVar ); 
    }    
    // Property: selectedIndex
    public int getSelectedIndex()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.selectedIndex, 0);
    }
    
    public void setSelectedIndex(int selectedIndex)
    {
        getStateHelper().put(PropertyKeys.selectedIndex, selectedIndex ); 
    }    
    // Property: activeTabStyleClass
    public String getActiveTabStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.activeTabStyleClass);
    }
    
    public void setActiveTabStyleClass(String activeTabStyleClass)
    {
        getStateHelper().put(PropertyKeys.activeTabStyleClass, activeTabStyleClass ); 
    }    
    // Property: inactiveTabStyleClass
    public String getInactiveTabStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.inactiveTabStyleClass);
    }
    
    public void setInactiveTabStyleClass(String inactiveTabStyleClass)
    {
        getStateHelper().put(PropertyKeys.inactiveTabStyleClass, inactiveTabStyleClass ); 
    }    
    // Property: activeSubStyleClass
    public String getActiveSubStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.activeSubStyleClass);
    }
    
    public void setActiveSubStyleClass(String activeSubStyleClass)
    {
        getStateHelper().put(PropertyKeys.activeSubStyleClass, activeSubStyleClass ); 
    }    
    // Property: inactiveSubStyleClass
    public String getInactiveSubStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.inactiveSubStyleClass);
    }
    
    public void setInactiveSubStyleClass(String inactiveSubStyleClass)
    {
        getStateHelper().put(PropertyKeys.inactiveSubStyleClass, inactiveSubStyleClass ); 
    }    
    // Property: tabContentStyleClass
    public String getTabContentStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.tabContentStyleClass);
    }
    
    public void setTabContentStyleClass(String tabContentStyleClass)
    {
        getStateHelper().put(PropertyKeys.tabContentStyleClass, tabContentStyleClass ); 
    }    
    // Property: disabledTabStyleClass
    public String getDisabledTabStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.disabledTabStyleClass);
    }
    
    public void setDisabledTabStyleClass(String disabledTabStyleClass)
    {
        getStateHelper().put(PropertyKeys.disabledTabStyleClass, disabledTabStyleClass ); 
    }    
    // Property: serverSideTabSwitch
    public boolean isServerSideTabSwitch()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.serverSideTabSwitch, false);
    }
    
    public void setServerSideTabSwitch(boolean serverSideTabSwitch)
    {
        getStateHelper().put(PropertyKeys.serverSideTabSwitch, serverSideTabSwitch ); 
    }    
    // Property: immediateTabChange
    public boolean isImmediateTabChange()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediateTabChange, true);
    }
    
    public void setImmediateTabChange(boolean immediateTabChange)
    {
        getStateHelper().put(PropertyKeys.immediateTabChange, immediateTabChange ); 
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
    // Property: bgcolor
    public String getBgcolor()
    {
        return (String) getStateHelper().eval(PropertyKeys.bgcolor);
    }
    
    public void setBgcolor(String bgcolor)
    {
        getStateHelper().put(PropertyKeys.bgcolor, bgcolor ); 
    }    
    // Property: border
    public int getBorder()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.border, Integer.MIN_VALUE);
    }
    
    public void setBorder(int border)
    {
        getStateHelper().put(PropertyKeys.border, border ); 
    }    
    // Property: cellpadding
    public String getCellpadding()
    {
        return (String) getStateHelper().eval(PropertyKeys.cellpadding);
    }
    
    public void setCellpadding(String cellpadding)
    {
        getStateHelper().put(PropertyKeys.cellpadding, cellpadding ); 
    }    
    // Property: cellspacing
    public String getCellspacing()
    {
        return (String) getStateHelper().eval(PropertyKeys.cellspacing);
    }
    
    public void setCellspacing(String cellspacing)
    {
        getStateHelper().put(PropertyKeys.cellspacing, cellspacing ); 
    }    
    // Property: frame
    public String getFrame()
    {
        return (String) getStateHelper().eval(PropertyKeys.frame);
    }
    
    public void setFrame(String frame)
    {
        getStateHelper().put(PropertyKeys.frame, frame ); 
    }    
    // Property: rules
    public String getRules()
    {
        return (String) getStateHelper().eval(PropertyKeys.rules);
    }
    
    public void setRules(String rules)
    {
        getStateHelper().put(PropertyKeys.rules, rules ); 
    }    
    // Property: summary
    public String getSummary()
    {
        return (String) getStateHelper().eval(PropertyKeys.summary);
    }
    
    public void setSummary(String summary)
    {
        getStateHelper().put(PropertyKeys.summary, summary ); 
    }    
    // Property: width
    public String getWidth()
    {
        return (String) getStateHelper().eval(PropertyKeys.width);
    }
    
    public void setWidth(String width)
    {
        getStateHelper().put(PropertyKeys.width, width ); 
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
         activeTabVar
        , activePanelTabVar
        , selectedIndex
        , activeTabStyleClass
        , inactiveTabStyleClass
        , activeSubStyleClass
        , inactiveSubStyleClass
        , tabContentStyleClass
        , disabledTabStyleClass
        , serverSideTabSwitch
        , immediateTabChange
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
        , enabledOnUserRole
        , visibleOnUserRole
        , dir
        , lang
        , title
        , datafld
        , datasrc
        , dataformatas
        , bgcolor
        , border
        , cellpadding
        , cellspacing
        , frame
        , rules
        , summary
        , width
        , align
        , style
        , styleClass
    }

 }
