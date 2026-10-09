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
package org.apache.myfaces.custom.selectOneRow;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.selectOneRow.AbstractSelectOneRow.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class SelectOneRowTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public SelectOneRowTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.SelectOneRow";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.SelectOneRow";
    }

    private ValueExpression _groupName;
    
    public void setGroupName(ValueExpression groupName)
    {
        _groupName = groupName;
    }
    private ValueExpression _disabled;
    
    public void setDisabled(ValueExpression disabled)
    {
        _disabled = disabled;
    }
    private ValueExpression _readonly;
    
    public void setReadonly(ValueExpression readonly)
    {
        _readonly = readonly;
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
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }
    private ValueExpression _immediate;
    
    public void setImmediate(ValueExpression immediate)
    {
        _immediate = immediate;
    }
    private ValueExpression _required;
    
    public void setRequired(ValueExpression required)
    {
        _required = required;
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
    private jakarta.el.MethodExpression _validator;
    
    public void setValidator(jakarta.el.MethodExpression validator)
    {
        _validator = validator;
    }
    private ValueExpression _validatorMessage;
    
    public void setValidatorMessage(ValueExpression validatorMessage)
    {
        _validatorMessage = validatorMessage;
    }
    private jakarta.el.MethodExpression _valueChangeListener;
    
    public void setValueChangeListener(jakarta.el.MethodExpression valueChangeListener)
    {
        _valueChangeListener = valueChangeListener;
    }
    private ValueExpression _value;
    
    public void setValue(ValueExpression value)
    {
        _value = value;
    }
    private ValueExpression _converter;
    
    public void setConverter(ValueExpression converter)
    {
        _converter = converter;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.selectOneRow.SelectOneRow))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.selectOneRow.SelectOneRow");
        }
        
        org.apache.myfaces.custom.selectOneRow.SelectOneRow comp = (org.apache.myfaces.custom.selectOneRow.SelectOneRow) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_groupName != null)
        {
            comp.setValueExpression("groupName", _groupName);
        } 
        if (_disabled != null)
        {
            comp.setValueExpression("disabled", _disabled);
        } 
        if (_readonly != null)
        {
            comp.setValueExpression("readonly", _readonly);
        } 
        if (_onblur != null)
        {
            comp.setValueExpression("onblur", _onblur);
        } 
        if (_onfocus != null)
        {
            comp.setValueExpression("onfocus", _onfocus);
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
        if (_onchange != null)
        {
            comp.setValueExpression("onchange", _onchange);
        } 
        if (_onselect != null)
        {
            comp.setValueExpression("onselect", _onselect);
        } 
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
        if (_immediate != null)
        {
            comp.setValueExpression("immediate", _immediate);
        } 
        if (_required != null)
        {
            comp.setValueExpression("required", _required);
        } 
        if (_converterMessage != null)
        {
            comp.setValueExpression("converterMessage", _converterMessage);
        } 
        if (_requiredMessage != null)
        {
            comp.setValueExpression("requiredMessage", _requiredMessage);
        } 
        if (_validator != null)
        {
            comp.addValidator(new MethodExpressionValidator(_validator));
        }
        if (_validatorMessage != null)
        {
            comp.setValueExpression("validatorMessage", _validatorMessage);
        } 
        if (_valueChangeListener != null)
        {
            comp.addValueChangeListener(new MethodExpressionValueChangeListener(_valueChangeListener));
        }
        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        } 
        if (_converter != null)
        {
            if (!_converter.isLiteralText())
            {
                comp.setValueExpression("converter", _converter);
            }
            else
            {
                String s = _converter.getExpressionString();
                if (s != null)
                {            
                    Converter converter = getFacesContext().getApplication().createConverter(s);
                    comp.setConverter(converter);
                }
            }
        }
    }

    public void release()
    {
        super.release();
        _groupName = null;
        _disabled = null;
        _readonly = null;
        _onblur = null;
        _onfocus = null;
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
        _onchange = null;
        _onselect = null;
        _align = null;
        _immediate = null;
        _required = null;
        _converterMessage = null;
        _requiredMessage = null;
        _validator = null;
        _validatorMessage = null;
        _valueChangeListener = null;
        _value = null;
        _converter = null;
    }
}
