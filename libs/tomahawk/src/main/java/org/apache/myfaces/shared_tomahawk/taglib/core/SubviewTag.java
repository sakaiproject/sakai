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
package org.apache.myfaces.shared_tomahawk.taglib.core;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class jakarta.faces.component.UINamingContainer.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class SubviewTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public SubviewTag()
    {    
    }
    
    public String getComponentType()
    {
        return "jakarta.faces.NamingContainer";
    }

    public String getRendererType()
    {
        return null;
    }


    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof jakarta.faces.component.UINamingContainer))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no jakarta.faces.component.UINamingContainer");
        }
        
        jakarta.faces.component.UINamingContainer comp = (jakarta.faces.component.UINamingContainer) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

    }

    public void release()
    {
        super.release();
    }
}
