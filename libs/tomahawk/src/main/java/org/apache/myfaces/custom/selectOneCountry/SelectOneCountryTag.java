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
package org.apache.myfaces.custom.selectOneCountry;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.selectOneCountry.AbstractSelectOneCountry.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class SelectOneCountryTag
    extends org.apache.myfaces.generated.taglib.html.ext.HtmlSelectOneMenuTag
{
    public SelectOneCountryTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.SelectOneCountry";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.SelectOneCountryRenderer";
    }

    private ValueExpression _maxLength;
    
    public void setMaxLength(ValueExpression maxLength)
    {
        _maxLength = maxLength;
    }
    private ValueExpression _emptySelection;
    
    public void setEmptySelection(ValueExpression emptySelection)
    {
        _emptySelection = emptySelection;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.selectOneCountry.SelectOneCountry))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.selectOneCountry.SelectOneCountry");
        }
        
        org.apache.myfaces.custom.selectOneCountry.SelectOneCountry comp = (org.apache.myfaces.custom.selectOneCountry.SelectOneCountry) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_maxLength != null)
        {
            comp.setValueExpression("maxLength", _maxLength);
        } 
        if (_emptySelection != null)
        {
            comp.setValueExpression("emptySelection", _emptySelection);
        } 
    }

    public void release()
    {
        super.release();
        _maxLength = null;
        _emptySelection = null;
    }
}
