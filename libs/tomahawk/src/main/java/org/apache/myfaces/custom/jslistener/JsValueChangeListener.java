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
package org.apache.myfaces.custom.jslistener;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;


// Generated from class org.apache.myfaces.custom.jslistener.AbstractJsValueChangeListener.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class JsValueChangeListener extends org.apache.myfaces.custom.jslistener.AbstractJsValueChangeListener
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Output";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.JsValueChangeListener";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.JsValueChangeListener";


    public JsValueChangeListener()
    {
        setRendererType("org.apache.myfaces.JsValueChangeListener");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: for
    public String getFor()
    {
        return (String) getStateHelper().eval(PropertyKeys.forVal);
    }
    
    public void setFor(String forParam)
    {
        getStateHelper().put(PropertyKeys.forVal, forParam ); 
    }    
    // Property: expressionValue
    public String getExpressionValue()
    {
        return (String) getStateHelper().eval(PropertyKeys.expressionValue);
    }
    
    public void setExpressionValue(String expressionValue)
    {
        getStateHelper().put(PropertyKeys.expressionValue, expressionValue ); 
    }    
    // Property: property
    public String getProperty()
    {
        return (String) getStateHelper().eval(PropertyKeys.property);
    }
    
    public void setProperty(String property)
    {
        getStateHelper().put(PropertyKeys.property, property ); 
    }    
    // Property: bodyTagEvent
    public String getBodyTagEvent()
    {
        return (String) getStateHelper().eval(PropertyKeys.bodyTagEvent);
    }
    
    public void setBodyTagEvent(String bodyTagEvent)
    {
        getStateHelper().put(PropertyKeys.bodyTagEvent, bodyTagEvent ); 
    }    

    protected enum PropertyKeys
    {
         forVal("for")
        , expressionValue
        , property
        , bodyTagEvent
        ;
        String c;
        
        PropertyKeys()
        {
        }
        
        //Constructor needed by "for" property
        PropertyKeys(String c)
        { 
            this.c = c;
        }
        
        public String toString()
        {
            return ((this.c != null) ? this.c : super.toString());
        }
    }

 }
