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
package org.apache.myfaces.shared_tomahawk.taglib.html;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class jakarta.faces.component.html.HtmlInputHidden.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlInputHiddenTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlInputHiddenTag()
    {    
    }
    
    public String getComponentType()
    {
        return "jakarta.faces.HtmlInputHidden";
    }

    public String getRendererType()
    {
        return "jakarta.faces.Hidden";
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
        if (!(component instanceof jakarta.faces.component.html.HtmlInputHidden))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no jakarta.faces.component.html.HtmlInputHidden");
        }
        
        jakarta.faces.component.html.HtmlInputHidden comp = (jakarta.faces.component.html.HtmlInputHidden) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

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
