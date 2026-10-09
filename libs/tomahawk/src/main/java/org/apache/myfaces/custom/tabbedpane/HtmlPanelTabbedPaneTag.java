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
package org.apache.myfaces.custom.tabbedpane;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tabbedpane.AbstractHtmlPanelTabbedPane.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlPanelTabbedPaneTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlPanelGroupTag
{
    public HtmlPanelTabbedPaneTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlPanelTabbedPane";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.TabbedPane";
    }

    private ValueExpression _activePanelTabVar;
    
    public void setActivePanelTabVar(ValueExpression activePanelTabVar)
    {
        _activePanelTabVar = activePanelTabVar;
    }
    private ValueExpression _selectedIndex;
    
    public void setSelectedIndex(ValueExpression selectedIndex)
    {
        _selectedIndex = selectedIndex;
    }
    private ValueExpression _activeTabStyleClass;
    
    public void setActiveTabStyleClass(ValueExpression activeTabStyleClass)
    {
        _activeTabStyleClass = activeTabStyleClass;
    }
    private ValueExpression _inactiveTabStyleClass;
    
    public void setInactiveTabStyleClass(ValueExpression inactiveTabStyleClass)
    {
        _inactiveTabStyleClass = inactiveTabStyleClass;
    }
    private ValueExpression _activeSubStyleClass;
    
    public void setActiveSubStyleClass(ValueExpression activeSubStyleClass)
    {
        _activeSubStyleClass = activeSubStyleClass;
    }
    private ValueExpression _inactiveSubStyleClass;
    
    public void setInactiveSubStyleClass(ValueExpression inactiveSubStyleClass)
    {
        _inactiveSubStyleClass = inactiveSubStyleClass;
    }
    private ValueExpression _tabContentStyleClass;
    
    public void setTabContentStyleClass(ValueExpression tabContentStyleClass)
    {
        _tabContentStyleClass = tabContentStyleClass;
    }
    private ValueExpression _disabledTabStyleClass;
    
    public void setDisabledTabStyleClass(ValueExpression disabledTabStyleClass)
    {
        _disabledTabStyleClass = disabledTabStyleClass;
    }
    private ValueExpression _serverSideTabSwitch;
    
    public void setServerSideTabSwitch(ValueExpression serverSideTabSwitch)
    {
        _serverSideTabSwitch = serverSideTabSwitch;
    }
    private ValueExpression _immediateTabChange;
    
    public void setImmediateTabChange(ValueExpression immediateTabChange)
    {
        _immediateTabChange = immediateTabChange;
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
    private ValueExpression _onkeydown;
    
    public void setOnkeydown(ValueExpression onkeydown)
    {
        _onkeydown = onkeydown;
    }
    private ValueExpression _onkeypress;
    
    public void setOnkeypress(ValueExpression onkeypress)
    {
        _onkeypress = onkeypress;
    }
    private ValueExpression _onkeyup;
    
    public void setOnkeyup(ValueExpression onkeyup)
    {
        _onkeyup = onkeyup;
    }
    private ValueExpression _onmousedown;
    
    public void setOnmousedown(ValueExpression onmousedown)
    {
        _onmousedown = onmousedown;
    }
    private ValueExpression _onmousemove;
    
    public void setOnmousemove(ValueExpression onmousemove)
    {
        _onmousemove = onmousemove;
    }
    private ValueExpression _onmouseout;
    
    public void setOnmouseout(ValueExpression onmouseout)
    {
        _onmouseout = onmouseout;
    }
    private ValueExpression _onmouseover;
    
    public void setOnmouseover(ValueExpression onmouseover)
    {
        _onmouseover = onmouseover;
    }
    private ValueExpression _onmouseup;
    
    public void setOnmouseup(ValueExpression onmouseup)
    {
        _onmouseup = onmouseup;
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
    private ValueExpression _bgcolor;
    
    public void setBgcolor(ValueExpression bgcolor)
    {
        _bgcolor = bgcolor;
    }
    private ValueExpression _border;
    
    public void setBorder(ValueExpression border)
    {
        _border = border;
    }
    private ValueExpression _cellpadding;
    
    public void setCellpadding(ValueExpression cellpadding)
    {
        _cellpadding = cellpadding;
    }
    private ValueExpression _cellspacing;
    
    public void setCellspacing(ValueExpression cellspacing)
    {
        _cellspacing = cellspacing;
    }
    private ValueExpression _frame;
    
    public void setFrame(ValueExpression frame)
    {
        _frame = frame;
    }
    private ValueExpression _rules;
    
    public void setRules(ValueExpression rules)
    {
        _rules = rules;
    }
    private ValueExpression _summary;
    
    public void setSummary(ValueExpression summary)
    {
        _summary = summary;
    }
    private ValueExpression _width;
    
    public void setWidth(ValueExpression width)
    {
        _width = width;
    }
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane");
        }
        
        org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane comp = (org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_activePanelTabVar != null)
        {
            comp.setValueExpression("activePanelTabVar", _activePanelTabVar);
        } 
        if (_selectedIndex != null)
        {
            comp.setValueExpression("selectedIndex", _selectedIndex);
        } 
        if (_activeTabStyleClass != null)
        {
            comp.setValueExpression("activeTabStyleClass", _activeTabStyleClass);
        } 
        if (_inactiveTabStyleClass != null)
        {
            comp.setValueExpression("inactiveTabStyleClass", _inactiveTabStyleClass);
        } 
        if (_activeSubStyleClass != null)
        {
            comp.setValueExpression("activeSubStyleClass", _activeSubStyleClass);
        } 
        if (_inactiveSubStyleClass != null)
        {
            comp.setValueExpression("inactiveSubStyleClass", _inactiveSubStyleClass);
        } 
        if (_tabContentStyleClass != null)
        {
            comp.setValueExpression("tabContentStyleClass", _tabContentStyleClass);
        } 
        if (_disabledTabStyleClass != null)
        {
            comp.setValueExpression("disabledTabStyleClass", _disabledTabStyleClass);
        } 
        if (_serverSideTabSwitch != null)
        {
            comp.setValueExpression("serverSideTabSwitch", _serverSideTabSwitch);
        } 
        if (_immediateTabChange != null)
        {
            comp.setValueExpression("immediateTabChange", _immediateTabChange);
        } 
        if (_onclick != null)
        {
            comp.setValueExpression("onclick", _onclick);
        } 
        if (_ondblclick != null)
        {
            comp.setValueExpression("ondblclick", _ondblclick);
        } 
        if (_onkeydown != null)
        {
            comp.setValueExpression("onkeydown", _onkeydown);
        } 
        if (_onkeypress != null)
        {
            comp.setValueExpression("onkeypress", _onkeypress);
        } 
        if (_onkeyup != null)
        {
            comp.setValueExpression("onkeyup", _onkeyup);
        } 
        if (_onmousedown != null)
        {
            comp.setValueExpression("onmousedown", _onmousedown);
        } 
        if (_onmousemove != null)
        {
            comp.setValueExpression("onmousemove", _onmousemove);
        } 
        if (_onmouseout != null)
        {
            comp.setValueExpression("onmouseout", _onmouseout);
        } 
        if (_onmouseover != null)
        {
            comp.setValueExpression("onmouseover", _onmouseover);
        } 
        if (_onmouseup != null)
        {
            comp.setValueExpression("onmouseup", _onmouseup);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
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
        if (_bgcolor != null)
        {
            comp.setValueExpression("bgcolor", _bgcolor);
        } 
        if (_border != null)
        {
            comp.setValueExpression("border", _border);
        } 
        if (_cellpadding != null)
        {
            comp.setValueExpression("cellpadding", _cellpadding);
        } 
        if (_cellspacing != null)
        {
            comp.setValueExpression("cellspacing", _cellspacing);
        } 
        if (_frame != null)
        {
            comp.setValueExpression("frame", _frame);
        } 
        if (_rules != null)
        {
            comp.setValueExpression("rules", _rules);
        } 
        if (_summary != null)
        {
            comp.setValueExpression("summary", _summary);
        } 
        if (_width != null)
        {
            comp.setValueExpression("width", _width);
        } 
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
    }

    public void release()
    {
        super.release();
        _activePanelTabVar = null;
        _selectedIndex = null;
        _activeTabStyleClass = null;
        _inactiveTabStyleClass = null;
        _activeSubStyleClass = null;
        _inactiveSubStyleClass = null;
        _tabContentStyleClass = null;
        _disabledTabStyleClass = null;
        _serverSideTabSwitch = null;
        _immediateTabChange = null;
        _onclick = null;
        _ondblclick = null;
        _onkeydown = null;
        _onkeypress = null;
        _onkeyup = null;
        _onmousedown = null;
        _onmousemove = null;
        _onmouseout = null;
        _onmouseover = null;
        _onmouseup = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _dir = null;
        _lang = null;
        _title = null;
        _datafld = null;
        _datasrc = null;
        _dataformatas = null;
        _bgcolor = null;
        _border = null;
        _cellpadding = null;
        _cellspacing = null;
        _frame = null;
        _rules = null;
        _summary = null;
        _width = null;
        _align = null;
    }
}
