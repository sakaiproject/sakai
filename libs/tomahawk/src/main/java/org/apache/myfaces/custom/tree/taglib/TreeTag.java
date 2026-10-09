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
import org.apache.myfaces.custom.tree.model.TreeModel;


// Generated from class org.apache.myfaces.custom.tree.HtmlTree.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class TreeTag
    extends org.apache.myfaces.custom.tree.taglib.AbstractTreeTag
{
    public TreeTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlTree";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.HtmlTree";
    }

    private ValueExpression _var;
    
    public void setVar(ValueExpression var)
    {
        _var = var;
    }
    private ValueExpression _iconLine;
    
    public void setIconLine(ValueExpression iconLine)
    {
        _iconLine = iconLine;
    }
    private ValueExpression _iconNoline;
    
    public void setIconNoline(ValueExpression iconNoline)
    {
        _iconNoline = iconNoline;
    }
    private ValueExpression _iconChildFirst;
    
    public void setIconChildFirst(ValueExpression iconChildFirst)
    {
        _iconChildFirst = iconChildFirst;
    }
    private ValueExpression _iconChildMiddle;
    
    public void setIconChildMiddle(ValueExpression iconChildMiddle)
    {
        _iconChildMiddle = iconChildMiddle;
    }
    private ValueExpression _iconChildLast;
    
    public void setIconChildLast(ValueExpression iconChildLast)
    {
        _iconChildLast = iconChildLast;
    }
    private ValueExpression _iconNodeOpen;
    
    public void setIconNodeOpen(ValueExpression iconNodeOpen)
    {
        _iconNodeOpen = iconNodeOpen;
    }
    private ValueExpression _iconNodeOpenFirst;
    
    public void setIconNodeOpenFirst(ValueExpression iconNodeOpenFirst)
    {
        _iconNodeOpenFirst = iconNodeOpenFirst;
    }
    private ValueExpression _iconNodeOpenMiddle;
    
    public void setIconNodeOpenMiddle(ValueExpression iconNodeOpenMiddle)
    {
        _iconNodeOpenMiddle = iconNodeOpenMiddle;
    }
    private ValueExpression _iconNodeOpenLast;
    
    public void setIconNodeOpenLast(ValueExpression iconNodeOpenLast)
    {
        _iconNodeOpenLast = iconNodeOpenLast;
    }
    private ValueExpression _iconNodeClose;
    
    public void setIconNodeClose(ValueExpression iconNodeClose)
    {
        _iconNodeClose = iconNodeClose;
    }
    private ValueExpression _iconNodeCloseFirst;
    
    public void setIconNodeCloseFirst(ValueExpression iconNodeCloseFirst)
    {
        _iconNodeCloseFirst = iconNodeCloseFirst;
    }
    private ValueExpression _iconNodeCloseMiddle;
    
    public void setIconNodeCloseMiddle(ValueExpression iconNodeCloseMiddle)
    {
        _iconNodeCloseMiddle = iconNodeCloseMiddle;
    }
    private ValueExpression _iconNodeCloseLast;
    
    public void setIconNodeCloseLast(ValueExpression iconNodeCloseLast)
    {
        _iconNodeCloseLast = iconNodeCloseLast;
    }
    private ValueExpression _nodeClass;
    
    public void setNodeClass(ValueExpression nodeClass)
    {
        _nodeClass = nodeClass;
    }
    private ValueExpression _rowClasses;
    
    public void setRowClasses(ValueExpression rowClasses)
    {
        _rowClasses = rowClasses;
    }
    private ValueExpression _columnClasses;
    
    public void setColumnClasses(ValueExpression columnClasses)
    {
        _columnClasses = columnClasses;
    }
    private ValueExpression _selectedNodeClass;
    
    public void setSelectedNodeClass(ValueExpression selectedNodeClass)
    {
        _selectedNodeClass = selectedNodeClass;
    }
    private ValueExpression _iconClass;
    
    public void setIconClass(ValueExpression iconClass)
    {
        _iconClass = iconClass;
    }
    private ValueExpression _expireListeners;
    
    public void setExpireListeners(ValueExpression expireListeners)
    {
        _expireListeners = expireListeners;
    }
    private ValueExpression _headerClass;
    
    public void setHeaderClass(ValueExpression headerClass)
    {
        _headerClass = headerClass;
    }
    private ValueExpression _footerClass;
    
    public void setFooterClass(ValueExpression footerClass)
    {
        _footerClass = footerClass;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.tree.HtmlTree))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.tree.HtmlTree");
        }
        
        org.apache.myfaces.custom.tree.HtmlTree comp = (org.apache.myfaces.custom.tree.HtmlTree) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_var != null)
        {
            comp.setValueExpression("var", _var);
        } 
        if (_iconLine != null)
        {
            comp.setValueExpression("iconLine", _iconLine);
        } 
        if (_iconNoline != null)
        {
            comp.setValueExpression("iconNoline", _iconNoline);
        } 
        if (_iconChildFirst != null)
        {
            comp.setValueExpression("iconChildFirst", _iconChildFirst);
        } 
        if (_iconChildMiddle != null)
        {
            comp.setValueExpression("iconChildMiddle", _iconChildMiddle);
        } 
        if (_iconChildLast != null)
        {
            comp.setValueExpression("iconChildLast", _iconChildLast);
        } 
        if (_iconNodeOpen != null)
        {
            comp.setValueExpression("iconNodeOpen", _iconNodeOpen);
        } 
        if (_iconNodeOpenFirst != null)
        {
            comp.setValueExpression("iconNodeOpenFirst", _iconNodeOpenFirst);
        } 
        if (_iconNodeOpenMiddle != null)
        {
            comp.setValueExpression("iconNodeOpenMiddle", _iconNodeOpenMiddle);
        } 
        if (_iconNodeOpenLast != null)
        {
            comp.setValueExpression("iconNodeOpenLast", _iconNodeOpenLast);
        } 
        if (_iconNodeClose != null)
        {
            comp.setValueExpression("iconNodeClose", _iconNodeClose);
        } 
        if (_iconNodeCloseFirst != null)
        {
            comp.setValueExpression("iconNodeCloseFirst", _iconNodeCloseFirst);
        } 
        if (_iconNodeCloseMiddle != null)
        {
            comp.setValueExpression("iconNodeCloseMiddle", _iconNodeCloseMiddle);
        } 
        if (_iconNodeCloseLast != null)
        {
            comp.setValueExpression("iconNodeCloseLast", _iconNodeCloseLast);
        } 
        if (_nodeClass != null)
        {
            comp.setValueExpression("nodeClass", _nodeClass);
        } 
        if (_rowClasses != null)
        {
            comp.setValueExpression("rowClasses", _rowClasses);
        } 
        if (_columnClasses != null)
        {
            comp.setValueExpression("columnClasses", _columnClasses);
        } 
        if (_selectedNodeClass != null)
        {
            comp.setValueExpression("selectedNodeClass", _selectedNodeClass);
        } 
        if (_iconClass != null)
        {
            comp.setValueExpression("iconClass", _iconClass);
        } 
        if (_expireListeners != null)
        {
            comp.setValueExpression("expireListeners", _expireListeners);
        } 
        if (_headerClass != null)
        {
            comp.setValueExpression("headerClass", _headerClass);
        } 
        if (_footerClass != null)
        {
            comp.setValueExpression("footerClass", _footerClass);
        } 
    }

    public void release()
    {
        super.release();
        _var = null;
        _iconLine = null;
        _iconNoline = null;
        _iconChildFirst = null;
        _iconChildMiddle = null;
        _iconChildLast = null;
        _iconNodeOpen = null;
        _iconNodeOpenFirst = null;
        _iconNodeOpenMiddle = null;
        _iconNodeOpenLast = null;
        _iconNodeClose = null;
        _iconNodeCloseFirst = null;
        _iconNodeCloseMiddle = null;
        _iconNodeCloseLast = null;
        _nodeClass = null;
        _rowClasses = null;
        _columnClasses = null;
        _selectedNodeClass = null;
        _iconClass = null;
        _expireListeners = null;
        _headerClass = null;
        _footerClass = null;
    }
}
