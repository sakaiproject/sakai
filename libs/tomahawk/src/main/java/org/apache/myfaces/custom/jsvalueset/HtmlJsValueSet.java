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
package org.apache.myfaces.custom.jsvalueset;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;


// Generated from class org.apache.myfaces.custom.jsvalueset.AbstractHtmlJsValueSet.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlJsValueSet extends org.apache.myfaces.custom.jsvalueset.AbstractHtmlJsValueSet
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Output";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlJsValueSet";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.JsValueSet";


    public HtmlJsValueSet()
    {
        setRendererType("org.apache.myfaces.JsValueSet");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: name
    public String getName()
    {
        return (String) getStateHelper().eval(PropertyKeys.name);
    }
    
    public void setName(String name)
    {
        getStateHelper().put(PropertyKeys.name, name ); 
    }    

    protected enum PropertyKeys
    {
         name
    }

 }
