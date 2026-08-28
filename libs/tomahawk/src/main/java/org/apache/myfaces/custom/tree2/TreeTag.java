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
package org.apache.myfaces.custom.tree2;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tree2.AbstractHtmlTree.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class TreeTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public TreeTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlTree2";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.HtmlTree2";
    }

    private ValueExpression _clientSideToggle;
    
    public void setClientSideToggle(ValueExpression clientSideToggle)
    {
        _clientSideToggle = clientSideToggle;
    }
    private java.lang.String _varNodeToggler;
    
    public void setVarNodeToggler(java.lang.String varNodeToggler)
    {
        _varNodeToggler = varNodeToggler;
    }
    private ValueExpression _showNav;
    
    public void setShowNav(ValueExpression showNav)
    {
        _showNav = showNav;
    }
    private ValueExpression _showLines;
    
    public void setShowLines(ValueExpression showLines)
    {
        _showLines = showLines;
    }
    private ValueExpression _showRootNode;
    
    public void setShowRootNode(ValueExpression showRootNode)
    {
        _showRootNode = showRootNode;
    }
    private ValueExpression _preserveToggle;
    
    public void setPreserveToggle(ValueExpression preserveToggle)
    {
        _preserveToggle = preserveToggle;
    }
    private ValueExpression _javascriptLocation;
    
    public void setJavascriptLocation(ValueExpression javascriptLocation)
    {
        _javascriptLocation = javascriptLocation;
    }
    private ValueExpression _imageLocation;
    
    public void setImageLocation(ValueExpression imageLocation)
    {
        _imageLocation = imageLocation;
    }
    private ValueExpression _styleLocation;
    
    public void setStyleLocation(ValueExpression styleLocation)
    {
        _styleLocation = styleLocation;
    }
    private ValueExpression _javascriptLibrary;
    
    public void setJavascriptLibrary(ValueExpression javascriptLibrary)
    {
        _javascriptLibrary = javascriptLibrary;
    }
    private ValueExpression _imageLibrary;
    
    public void setImageLibrary(ValueExpression imageLibrary)
    {
        _imageLibrary = imageLibrary;
    }
    private ValueExpression _styleLibrary;
    
    public void setStyleLibrary(ValueExpression styleLibrary)
    {
        _styleLibrary = styleLibrary;
    }
    private ValueExpression _value;
    
    public void setValue(ValueExpression value)
    {
        _value = value;
    }
    private ValueExpression _var;
    
    public void setVar(ValueExpression var)
    {
        _var = var;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.tree2.HtmlTree))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.tree2.HtmlTree");
        }
        
        org.apache.myfaces.custom.tree2.HtmlTree comp = (org.apache.myfaces.custom.tree2.HtmlTree) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_clientSideToggle != null)
        {
            comp.setValueExpression("clientSideToggle", _clientSideToggle);
        } 
        if (_varNodeToggler != null)
        {
            comp.getAttributes().put("varNodeToggler", _varNodeToggler);
        } 
        if (_showNav != null)
        {
            comp.setValueExpression("showNav", _showNav);
        } 
        if (_showLines != null)
        {
            comp.setValueExpression("showLines", _showLines);
        } 
        if (_showRootNode != null)
        {
            comp.setValueExpression("showRootNode", _showRootNode);
        } 
        if (_preserveToggle != null)
        {
            comp.setValueExpression("preserveToggle", _preserveToggle);
        } 
        if (_javascriptLocation != null)
        {
            comp.setValueExpression("javascriptLocation", _javascriptLocation);
        } 
        if (_imageLocation != null)
        {
            comp.setValueExpression("imageLocation", _imageLocation);
        } 
        if (_styleLocation != null)
        {
            comp.setValueExpression("styleLocation", _styleLocation);
        } 
        if (_javascriptLibrary != null)
        {
            comp.setValueExpression("javascriptLibrary", _javascriptLibrary);
        } 
        if (_imageLibrary != null)
        {
            comp.setValueExpression("imageLibrary", _imageLibrary);
        } 
        if (_styleLibrary != null)
        {
            comp.setValueExpression("styleLibrary", _styleLibrary);
        } 
        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        } 
        if (_var != null)
        {
            comp.setValueExpression("var", _var);
        } 
    }

    public void release()
    {
        super.release();
        _clientSideToggle = null;
        _varNodeToggler = null;
        _showNav = null;
        _showLines = null;
        _showRootNode = null;
        _preserveToggle = null;
        _javascriptLocation = null;
        _imageLocation = null;
        _styleLocation = null;
        _javascriptLibrary = null;
        _imageLibrary = null;
        _styleLibrary = null;
        _value = null;
        _var = null;
    }
}
