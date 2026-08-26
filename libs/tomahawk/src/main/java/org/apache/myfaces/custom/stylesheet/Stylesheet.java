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
package org.apache.myfaces.custom.stylesheet;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.stylesheet.AbstractStylesheet.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class Stylesheet extends org.apache.myfaces.custom.stylesheet.AbstractStylesheet
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Output";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.Stylesheet";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.Stylesheet";


    public Stylesheet()
    {
        setRendererType("org.apache.myfaces.Stylesheet");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: path
    public String getPath()
    {
        return (String) getStateHelper().eval(PropertyKeys.path);
    }
    
    public void setPath(String path)
    {
        getStateHelper().put(PropertyKeys.path, path ); 
    }    
    // Property: inline
    public boolean isInline()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.inline, false);
    }
    
    public void setInline(boolean inline)
    {
        getStateHelper().put(PropertyKeys.inline, inline ); 
    }    
    // Property: filtered
    public boolean isFiltered()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.filtered, false);
    }
    
    public void setFiltered(boolean filtered)
    {
        getStateHelper().put(PropertyKeys.filtered, filtered ); 
    }    
    // Property: media
    public String getMedia()
    {
        return (String) getStateHelper().eval(PropertyKeys.media);
    }
    
    public void setMedia(String media)
    {
        getStateHelper().put(PropertyKeys.media, media ); 
    }    
    // Property: enabledOnUserRole
    public String getEnabledOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.enabledOnUserRole);
    }
    
    public void setEnabledOnUserRole(String enabledOnUserRole)
    {
        getStateHelper().put(PropertyKeys.enabledOnUserRole, enabledOnUserRole ); 
    }    
    // Property: visibleOnUserRole
    public String getVisibleOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.visibleOnUserRole);
    }
    
    public void setVisibleOnUserRole(String visibleOnUserRole)
    {
        getStateHelper().put(PropertyKeys.visibleOnUserRole, visibleOnUserRole ); 
    }    

    protected enum PropertyKeys
    {
         path
        , inline
        , filtered
        , media
        , enabledOnUserRole
        , visibleOnUserRole
    }

 }
