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
package org.apache.myfaces.custom.tree.taglib;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tree.AbstractHtmlTreeCheckbox.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class TreeCheckboxTag
    extends org.apache.myfaces.shared_tomahawk.taglib.core.SelectItemTag
{
    public TreeCheckboxTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlTreeCheckbox";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.HtmlTreeCheckbox";
    }

    private ValueExpression _for;
    
    public void setFor(ValueExpression forParam)
    {
        _for = forParam;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.tree.HtmlTreeCheckbox))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.tree.HtmlTreeCheckbox");
        }
        
        org.apache.myfaces.custom.tree.HtmlTreeCheckbox comp = (org.apache.myfaces.custom.tree.HtmlTreeCheckbox) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_for != null)
        {
            comp.setValueExpression("for", _for);
        } 
    }

    public void release()
    {
        super.release();
        _for = null;
    }
}
