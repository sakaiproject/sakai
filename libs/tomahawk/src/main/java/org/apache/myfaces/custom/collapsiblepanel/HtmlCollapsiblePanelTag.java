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
package org.apache.myfaces.custom.collapsiblepanel;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.collapsiblepanel.AbstractHtmlCollapsiblePanel.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlCollapsiblePanelTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlCollapsiblePanelTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlCollapsiblePanel";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.CollapsiblePanel";
    }

    private ValueExpression _var;
    
    public void setVar(ValueExpression var)
    {
        _var = var;
    }
    private ValueExpression _titleVar;
    
    public void setTitleVar(ValueExpression titleVar)
    {
        _titleVar = titleVar;
    }
    private ValueExpression _indicatorStyleClass;
    
    public void setIndicatorStyleClass(ValueExpression indicatorStyleClass)
    {
        _indicatorStyleClass = indicatorStyleClass;
    }
    private ValueExpression _indicatorStyle;
    
    public void setIndicatorStyle(ValueExpression indicatorStyle)
    {
        _indicatorStyle = indicatorStyle;
    }
    private ValueExpression _titleStyleClass;
    
    public void setTitleStyleClass(ValueExpression titleStyleClass)
    {
        _titleStyleClass = titleStyleClass;
    }
    private ValueExpression _titleStyle;
    
    public void setTitleStyle(ValueExpression titleStyle)
    {
        _titleStyle = titleStyle;
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
    private ValueExpression _converterMessage;
    
    public void setConverterMessage(ValueExpression converterMessage)
    {
        _converterMessage = converterMessage;
    }
    private ValueExpression _requiredMessage;
    
    public void setRequiredMessage(ValueExpression requiredMessage)
    {
        _requiredMessage = requiredMessage;
    }
    private ValueExpression _validatorMessage;
    
    public void setValidatorMessage(ValueExpression validatorMessage)
    {
        _validatorMessage = validatorMessage;
    }
    private ValueExpression _value;
    
    public void setValue(ValueExpression value)
    {
        _value = value;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.collapsiblepanel.HtmlCollapsiblePanel))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.collapsiblepanel.HtmlCollapsiblePanel");
        }
        
        org.apache.myfaces.custom.collapsiblepanel.HtmlCollapsiblePanel comp = (org.apache.myfaces.custom.collapsiblepanel.HtmlCollapsiblePanel) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_var != null)
        {
            comp.setValueExpression("var", _var);
        } 
        if (_titleVar != null)
        {
            comp.setValueExpression("titleVar", _titleVar);
        } 
        if (_indicatorStyleClass != null)
        {
            comp.setValueExpression("indicatorStyleClass", _indicatorStyleClass);
        } 
        if (_indicatorStyle != null)
        {
            comp.setValueExpression("indicatorStyle", _indicatorStyle);
        } 
        if (_titleStyleClass != null)
        {
            comp.setValueExpression("titleStyleClass", _titleStyleClass);
        } 
        if (_titleStyle != null)
        {
            comp.setValueExpression("titleStyle", _titleStyle);
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
        if (_style != null)
        {
            comp.setValueExpression("style", _style);
        } 
        if (_styleClass != null)
        {
            comp.setValueExpression("styleClass", _styleClass);
        } 
        if (_converterMessage != null)
        {
            comp.setValueExpression("converterMessage", _converterMessage);
        } 
        if (_requiredMessage != null)
        {
            comp.setValueExpression("requiredMessage", _requiredMessage);
        } 
        if (_validatorMessage != null)
        {
            comp.setValueExpression("validatorMessage", _validatorMessage);
        } 
        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        } 
    }

    public void release()
    {
        super.release();
        _var = null;
        _titleVar = null;
        _indicatorStyleClass = null;
        _indicatorStyle = null;
        _titleStyleClass = null;
        _titleStyle = null;
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
        _style = null;
        _styleClass = null;
        _converterMessage = null;
        _requiredMessage = null;
        _validatorMessage = null;
        _value = null;
    }
}
