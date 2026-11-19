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
package org.apache.myfaces.custom.radio;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.radio.AbstractHtmlRadio.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlRadioTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlRadioTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlRadio";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Radio";
    }

    private ValueExpression _for;
    
    public void setFor(ValueExpression forParam)
    {
        _for = forParam;
    }
    private ValueExpression _index;
    
    public void setIndex(ValueExpression index)
    {
        _index = index;
    }
    private ValueExpression _renderLogicalId;
    
    public void setRenderLogicalId(ValueExpression renderLogicalId)
    {
        _renderLogicalId = renderLogicalId;
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
    private ValueExpression _onblur;
    
    public void setOnblur(ValueExpression onblur)
    {
        _onblur = onblur;
    }
    private ValueExpression _onfocus;
    
    public void setOnfocus(ValueExpression onfocus)
    {
        _onfocus = onfocus;
    }
    private ValueExpression _accesskey;
    
    public void setAccesskey(ValueExpression accesskey)
    {
        _accesskey = accesskey;
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
    private ValueExpression _disabledClass;
    
    public void setDisabledClass(ValueExpression disabledClass)
    {
        _disabledClass = disabledClass;
    }
    private ValueExpression _enabledClass;
    
    public void setEnabledClass(ValueExpression enabledClass)
    {
        _enabledClass = enabledClass;
    }
    private ValueExpression _alt;
    
    public void setAlt(ValueExpression alt)
    {
        _alt = alt;
    }
    private ValueExpression _tabindex;
    
    public void setTabindex(ValueExpression tabindex)
    {
        _tabindex = tabindex;
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
    private ValueExpression _onchange;
    
    public void setOnchange(ValueExpression onchange)
    {
        _onchange = onchange;
    }
    private ValueExpression _onselect;
    
    public void setOnselect(ValueExpression onselect)
    {
        _onselect = onselect;
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
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.radio.HtmlRadio))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.radio.HtmlRadio");
        }
        
        org.apache.myfaces.custom.radio.HtmlRadio comp = (org.apache.myfaces.custom.radio.HtmlRadio) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_for != null)
        {
            comp.setValueExpression("for", _for);
        } 
        if (_index != null)
        {
            comp.setValueExpression("index", _index);
        } 
        if (_renderLogicalId != null)
        {
            comp.setValueExpression("renderLogicalId", _renderLogicalId);
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
        if (_onblur != null)
        {
            comp.setValueExpression("onblur", _onblur);
        } 
        if (_onfocus != null)
        {
            comp.setValueExpression("onfocus", _onfocus);
        } 
        if (_accesskey != null)
        {
            comp.setValueExpression("accesskey", _accesskey);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_disabledClass != null)
        {
            comp.setValueExpression("disabledClass", _disabledClass);
        } 
        if (_enabledClass != null)
        {
            comp.setValueExpression("enabledClass", _enabledClass);
        } 
        if (_alt != null)
        {
            comp.setValueExpression("alt", _alt);
        } 
        if (_tabindex != null)
        {
            comp.setValueExpression("tabindex", _tabindex);
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
        if (_onchange != null)
        {
            comp.setValueExpression("onchange", _onchange);
        } 
        if (_onselect != null)
        {
            comp.setValueExpression("onselect", _onselect);
        } 
        if (_style != null)
        {
            comp.setValueExpression("style", _style);
        } 
        if (_styleClass != null)
        {
            comp.setValueExpression("styleClass", _styleClass);
        } 
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
    }

    public void release()
    {
        super.release();
        _for = null;
        _index = null;
        _renderLogicalId = null;
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
        _onblur = null;
        _onfocus = null;
        _accesskey = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _disabledClass = null;
        _enabledClass = null;
        _alt = null;
        _tabindex = null;
        _dir = null;
        _lang = null;
        _title = null;
        _onchange = null;
        _onselect = null;
        _style = null;
        _styleClass = null;
        _align = null;
    }
}
