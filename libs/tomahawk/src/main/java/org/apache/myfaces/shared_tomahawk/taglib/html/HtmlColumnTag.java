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


// Generated from class jakarta.faces.component.html._HtmlColumn.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlColumnTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlColumnTag()
    {    
    }
    
    public String getComponentType()
    {
        return "jakarta.faces.HtmlColumn";
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _headerClass;
    
    public void setHeaderClass(ValueExpression headerClass)
    {
        _headerClass = headerClass;
    }
    private ValueExpression _footerClass;
    
    public void setFooterClass(ValueExpression footerClass)
    {
        _footerClass = footerClass;
    }
    private ValueExpression _rowHeader;
    
    public void setRowHeader(ValueExpression rowHeader)
    {
        _rowHeader = rowHeader;
    }
    private ValueExpression _rendered;
    
    public void setRendered(ValueExpression rendered)
    {
        _rendered = rendered;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof jakarta.faces.component.html.HtmlColumn))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no jakarta.faces.component.html.HtmlColumn");
        }
        
        jakarta.faces.component.html.HtmlColumn comp = (jakarta.faces.component.html.HtmlColumn) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_headerClass != null)
        {
            comp.setValueExpression("headerClass", _headerClass);
        } 
        if (_footerClass != null)
        {
            comp.setValueExpression("footerClass", _footerClass);
        } 
        if (_rowHeader != null)
        {
            comp.setValueExpression("rowHeader", _rowHeader);
        } 
        if (_rendered != null)
        {
            comp.setValueExpression("rendered", _rendered);
        } 
    }

    public void release()
    {
        super.release();
        _headerClass = null;
        _footerClass = null;
        _rowHeader = null;
        _rendered = null;
    }
}
