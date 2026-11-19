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
package org.apache.myfaces.custom.transform;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.transform.AbstractXmlTransform.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class XmlTransformTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public XmlTransformTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.tomahawk.XmlTransform";
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _content;
    
    public void setContent(ValueExpression content)
    {
        _content = content;
    }
    private ValueExpression _contentLocation;
    
    public void setContentLocation(ValueExpression contentLocation)
    {
        _contentLocation = contentLocation;
    }
    private ValueExpression _stylesheet;
    
    public void setStylesheet(ValueExpression stylesheet)
    {
        _stylesheet = stylesheet;
    }
    private ValueExpression _contentStream;
    
    public void setContentStream(ValueExpression contentStream)
    {
        _contentStream = contentStream;
    }
    private ValueExpression _stylesheetLocation;
    
    public void setStylesheetLocation(ValueExpression stylesheetLocation)
    {
        _stylesheetLocation = stylesheetLocation;
    }
    private ValueExpression _styleStream;
    
    public void setStyleStream(ValueExpression styleStream)
    {
        _styleStream = styleStream;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.transform.XmlTransform))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.transform.XmlTransform");
        }
        
        org.apache.myfaces.custom.transform.XmlTransform comp = (org.apache.myfaces.custom.transform.XmlTransform) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_content != null)
        {
            comp.setValueExpression("content", _content);
        } 
        if (_contentLocation != null)
        {
            comp.setValueExpression("contentLocation", _contentLocation);
        } 
        if (_stylesheet != null)
        {
            comp.setValueExpression("stylesheet", _stylesheet);
        } 
        if (_contentStream != null)
        {
            comp.setValueExpression("contentStream", _contentStream);
        } 
        if (_stylesheetLocation != null)
        {
            comp.setValueExpression("stylesheetLocation", _stylesheetLocation);
        } 
        if (_styleStream != null)
        {
            comp.setValueExpression("styleStream", _styleStream);
        } 
    }

    public void release()
    {
        super.release();
        _content = null;
        _contentLocation = null;
        _stylesheet = null;
        _contentStream = null;
        _stylesheetLocation = null;
        _styleStream = null;
    }
}
