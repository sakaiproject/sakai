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
package org.apache.myfaces.custom.tree;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tree.AbstractHtmlTreeCheckbox.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlTreeCheckbox extends org.apache.myfaces.custom.tree.AbstractHtmlTreeCheckbox
{

    static public final String COMPONENT_FAMILY =
        "org.apache.myfaces.HtmlTreeCheckbox";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlTreeCheckbox";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.HtmlTreeCheckbox";


    public HtmlTreeCheckbox()
    {
        setRendererType("org.apache.myfaces.HtmlTreeCheckbox");
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

    protected enum PropertyKeys
    {
         forVal("for")
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
