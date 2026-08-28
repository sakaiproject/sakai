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
package org.apache.myfaces.custom.navmenu.htmlnavmenu;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.navmenu.htmlnavmenu.AbstractHtmlCommandNavigationItem.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlCommandNavigationItemTag
    extends org.apache.myfaces.generated.taglib.html.ext.HtmlCommandLinkTag
{
    public HtmlCommandNavigationItemTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlCommandNavigationItem";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.NavigationMenu";
    }

    private ValueExpression _open;
    
    public void setOpen(ValueExpression open)
    {
        _open = open;
    }
    private ValueExpression _active;
    
    public void setActive(ValueExpression active)
    {
        _active = active;
    }
    private ValueExpression _activeOnViewIds;
    
    public void setActiveOnViewIds(ValueExpression activeOnViewIds)
    {
        _activeOnViewIds = activeOnViewIds;
    }
    private ValueExpression _externalLink;
    
    public void setExternalLink(ValueExpression externalLink)
    {
        _externalLink = externalLink;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem");
        }
        
        org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem comp = (org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_open != null)
        {
            comp.setValueExpression("open", _open);
        } 
        if (_active != null)
        {
            comp.setValueExpression("active", _active);
        } 
        if (_activeOnViewIds != null)
        {
            comp.setValueExpression("activeOnViewIds", _activeOnViewIds);
        } 
        if (_externalLink != null)
        {
            comp.setValueExpression("externalLink", _externalLink);
        } 
    }

    public void release()
    {
        super.release();
        _open = null;
        _active = null;
        _activeOnViewIds = null;
        _externalLink = null;
    }
}
