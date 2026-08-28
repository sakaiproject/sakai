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
package org.apache.myfaces.component.html.ext;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.component.html.ext.AbstractHtmlInputHidden.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlInputHidden extends org.apache.myfaces.component.html.ext.AbstractHtmlInputHidden
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Input";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlInputHidden";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.Hidden";


    public HtmlInputHidden()
    {
        setRendererType("org.apache.myfaces.Hidden");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: forceId
    public boolean isForceId()
    {
        Object value = getStateHelper().get(PropertyKeys.forceId);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;        
    }
    
    public void setForceId(boolean forceId)
    {
        getStateHelper().put(PropertyKeys.forceId, forceId ); 
    }    
    // Property: forceIdIndex
    public boolean isForceIdIndex()
    {
        Object value = getStateHelper().get(PropertyKeys.forceIdIndex);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return true;        
    }
    
    public void setForceIdIndex(boolean forceIdIndex)
    {
        getStateHelper().put(PropertyKeys.forceIdIndex, forceIdIndex ); 
    }    

    protected enum PropertyKeys
    {
         forceId
        , forceIdIndex
    }

 }
