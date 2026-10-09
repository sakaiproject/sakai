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
package org.apache.myfaces.custom.tree2;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tree2.AbstractHtmlTree.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlTree extends org.apache.myfaces.custom.tree2.AbstractHtmlTree
{

    static public final String COMPONENT_FAMILY =
        "org.apache.myfaces.HtmlTree2";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlTree2";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.HtmlTree2";


    public HtmlTree()
    {
        setRendererType("org.apache.myfaces.HtmlTree2");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: showNav
    public boolean isShowNav()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.showNav, true);
    }
    
    public void setShowNav(boolean showNav)
    {
        getStateHelper().put(PropertyKeys.showNav, showNav ); 
    }    
    // Property: showLines
    public boolean isShowLines()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.showLines, true);
    }
    
    public void setShowLines(boolean showLines)
    {
        getStateHelper().put(PropertyKeys.showLines, showLines ); 
    }    
    // Property: showRootNode
    public boolean isShowRootNode()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.showRootNode, true);
    }
    
    public void setShowRootNode(boolean showRootNode)
    {
        getStateHelper().put(PropertyKeys.showRootNode, showRootNode ); 
    }    
    // Property: preserveToggle
    public boolean isPreserveToggle()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.preserveToggle, true);
    }
    
    public void setPreserveToggle(boolean preserveToggle)
    {
        getStateHelper().put(PropertyKeys.preserveToggle, preserveToggle ); 
    }    
    // Property: javascriptLocation
    public String getJavascriptLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.javascriptLocation);
    }
    
    public void setJavascriptLocation(String javascriptLocation)
    {
        getStateHelper().put(PropertyKeys.javascriptLocation, javascriptLocation ); 
    }    
    // Property: imageLocation
    public String getImageLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.imageLocation);
    }
    
    public void setImageLocation(String imageLocation)
    {
        getStateHelper().put(PropertyKeys.imageLocation, imageLocation ); 
    }    
    // Property: styleLocation
    public String getStyleLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleLocation);
    }
    
    public void setStyleLocation(String styleLocation)
    {
        getStateHelper().put(PropertyKeys.styleLocation, styleLocation ); 
    }    
    // Property: javascriptLibrary
    public String getJavascriptLibrary()
    {
        return (String) getStateHelper().eval(PropertyKeys.javascriptLibrary);
    }
    
    public void setJavascriptLibrary(String javascriptLibrary)
    {
        getStateHelper().put(PropertyKeys.javascriptLibrary, javascriptLibrary ); 
    }    
    // Property: imageLibrary
    public String getImageLibrary()
    {
        return (String) getStateHelper().eval(PropertyKeys.imageLibrary);
    }
    
    public void setImageLibrary(String imageLibrary)
    {
        getStateHelper().put(PropertyKeys.imageLibrary, imageLibrary ); 
    }    
    // Property: styleLibrary
    public String getStyleLibrary()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleLibrary);
    }
    
    public void setStyleLibrary(String styleLibrary)
    {
        getStateHelper().put(PropertyKeys.styleLibrary, styleLibrary ); 
    }    

    protected enum PropertyKeys
    {
         showNav
        , showLines
        , showRootNode
        , preserveToggle
        , javascriptLocation
        , imageLocation
        , styleLocation
        , javascriptLibrary
        , imageLibrary
        , styleLibrary
    }

 }
