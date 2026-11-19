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
package org.apache.myfaces.custom.newspaper;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.newspaper.AbstractHtmlNewspaperTable.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlNewspaperTableTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlDataTableTag
{
    public HtmlNewspaperTableTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlNewspaperTable";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.HtmlNewspaperTable";
    }

    private ValueExpression _newspaperColumns;
    
    public void setNewspaperColumns(ValueExpression newspaperColumns)
    {
        _newspaperColumns = newspaperColumns;
    }
    private ValueExpression _newspaperOrientation;
    
    public void setNewspaperOrientation(ValueExpression newspaperOrientation)
    {
        _newspaperOrientation = newspaperOrientation;
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
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.newspaper.HtmlNewspaperTable))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.newspaper.HtmlNewspaperTable");
        }
        
        org.apache.myfaces.custom.newspaper.HtmlNewspaperTable comp = (org.apache.myfaces.custom.newspaper.HtmlNewspaperTable) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_newspaperColumns != null)
        {
            comp.setValueExpression("newspaperColumns", _newspaperColumns);
        } 
        if (_newspaperOrientation != null)
        {
            comp.setValueExpression("newspaperOrientation", _newspaperOrientation);
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
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
    }

    public void release()
    {
        super.release();
        _newspaperColumns = null;
        _newspaperOrientation = null;
        _datafld = null;
        _datasrc = null;
        _dataformatas = null;
        _align = null;
    }
}
