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
package org.apache.myfaces.custom.sortheader;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.sortheader.AbstractHtmlCommandSortHeader.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlCommandSortHeaderTag
    extends org.apache.myfaces.generated.taglib.html.ext.HtmlCommandLinkTag
{
    public HtmlCommandSortHeaderTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlCommandSortHeader";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.SortHeader";
    }

    private ValueExpression _columnName;
    
    public void setColumnName(ValueExpression columnName)
    {
        _columnName = columnName;
    }
    private ValueExpression _propertyName;
    
    public void setPropertyName(ValueExpression propertyName)
    {
        _propertyName = propertyName;
    }
    private ValueExpression _arrow;
    
    public void setArrow(ValueExpression arrow)
    {
        _arrow = arrow;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader");
        }
        
        org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader comp = (org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_columnName != null)
        {
            comp.setValueExpression("columnName", _columnName);
        } 
        if (_propertyName != null)
        {
            comp.setValueExpression("propertyName", _propertyName);
        } 
        if (_arrow != null)
        {
            comp.setValueExpression("arrow", _arrow);
        } 
    }

    public void release()
    {
        super.release();
        _columnName = null;
        _propertyName = null;
        _arrow = null;
    }
}
