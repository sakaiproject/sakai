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
package org.apache.myfaces.custom.navigation;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.navigation.AbstractHtmlPanelNavigation.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlPanelNavigationTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlPanelGroupTag
{
    public HtmlPanelNavigationTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlPanelNavigation";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Navigation";
    }

    private ValueExpression _itemClass;
    
    public void setItemClass(ValueExpression itemClass)
    {
        _itemClass = itemClass;
    }
    private ValueExpression _openItemClass;
    
    public void setOpenItemClass(ValueExpression openItemClass)
    {
        _openItemClass = openItemClass;
    }
    private ValueExpression _activeItemClass;
    
    public void setActiveItemClass(ValueExpression activeItemClass)
    {
        _activeItemClass = activeItemClass;
    }
    private ValueExpression _separatorClass;
    
    public void setSeparatorClass(ValueExpression separatorClass)
    {
        _separatorClass = separatorClass;
    }
    private ValueExpression _itemStyle;
    
    public void setItemStyle(ValueExpression itemStyle)
    {
        _itemStyle = itemStyle;
    }
    private ValueExpression _openItemStyle;
    
    public void setOpenItemStyle(ValueExpression openItemStyle)
    {
        _openItemStyle = openItemStyle;
    }
    private ValueExpression _activeItemStyle;
    
    public void setActiveItemStyle(ValueExpression activeItemStyle)
    {
        _activeItemStyle = activeItemStyle;
    }
    private ValueExpression _separatorStyle;
    
    public void setSeparatorStyle(ValueExpression separatorStyle)
    {
        _separatorStyle = separatorStyle;
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
        if (!(component instanceof org.apache.myfaces.custom.navigation.HtmlPanelNavigation))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.navigation.HtmlPanelNavigation");
        }
        
        org.apache.myfaces.custom.navigation.HtmlPanelNavigation comp = (org.apache.myfaces.custom.navigation.HtmlPanelNavigation) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_itemClass != null)
        {
            comp.setValueExpression("itemClass", _itemClass);
        } 
        if (_openItemClass != null)
        {
            comp.setValueExpression("openItemClass", _openItemClass);
        } 
        if (_activeItemClass != null)
        {
            comp.setValueExpression("activeItemClass", _activeItemClass);
        } 
        if (_separatorClass != null)
        {
            comp.setValueExpression("separatorClass", _separatorClass);
        } 
        if (_itemStyle != null)
        {
            comp.setValueExpression("itemStyle", _itemStyle);
        } 
        if (_openItemStyle != null)
        {
            comp.setValueExpression("openItemStyle", _openItemStyle);
        } 
        if (_activeItemStyle != null)
        {
            comp.setValueExpression("activeItemStyle", _activeItemStyle);
        } 
        if (_separatorStyle != null)
        {
            comp.setValueExpression("separatorStyle", _separatorStyle);
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
        _itemClass = null;
        _openItemClass = null;
        _activeItemClass = null;
        _separatorClass = null;
        _itemStyle = null;
        _openItemStyle = null;
        _activeItemStyle = null;
        _separatorStyle = null;
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
