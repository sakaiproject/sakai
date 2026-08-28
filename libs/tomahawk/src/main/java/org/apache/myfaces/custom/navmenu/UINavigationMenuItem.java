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
package org.apache.myfaces.custom.navmenu;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.navmenu.AbstractUINavigationMenuItem.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class UINavigationMenuItem extends org.apache.myfaces.custom.navmenu.AbstractUINavigationMenuItem
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.SelectItem";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.NavigationMenuItem";


    public UINavigationMenuItem()
    {
        setRendererType(null);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: icon
    public String getIcon()
    {
        return (String) getStateHelper().eval(PropertyKeys.icon);
    }
    
    public void setIcon(String icon)
    {
        getStateHelper().put(PropertyKeys.icon, icon ); 
    }    
    // Property: split
    public boolean isSplit()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.split, false);
    }
    
    public void setSplit(boolean split)
    {
        getStateHelper().put(PropertyKeys.split, split ); 
    }    
    // Property: open
    public boolean isOpen()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.open, false);
    }
    
    public void setOpen(boolean open)
    {
        getStateHelper().put(PropertyKeys.open, open ); 
    }    
    // Property: active
    public boolean isActive()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.active, false);
    }
    
    public void setActive(boolean active)
    {
        getStateHelper().put(PropertyKeys.active, active ); 
    }    
    // Property: immediate
    public boolean isImmediate()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, true);
    }
    
    public void setImmediate(boolean immediate)
    {
        getStateHelper().put(PropertyKeys.immediate, immediate ); 
    }    
    // Property: externalLink
    public String getExternalLink()
    {
        return (String) getStateHelper().eval(PropertyKeys.externalLink);
    }
    
    public void setExternalLink(String externalLink)
    {
        getStateHelper().put(PropertyKeys.externalLink, externalLink ); 
    }    
    // Property: actionExpression
    public MethodExpression getActionExpression()
    {
        return (MethodExpression) getStateHelper().get(PropertyKeys.actionExpression);        
    }
    
    public void setActionExpression(MethodExpression actionExpression)
    {
        getStateHelper().put(PropertyKeys.actionExpression, actionExpression ); 
    }    
    // Property: actionListener
    public MethodBinding getActionListener()
    {
        return (MethodBinding) getStateHelper().get(PropertyKeys.actionListener);        
    }
    
    public void setActionListener(MethodBinding actionListener)
    {
        getStateHelper().put(PropertyKeys.actionListener, actionListener ); 
    }    
    // Property: target
    public String getTarget()
    {
        return (String) getStateHelper().eval(PropertyKeys.target);
    }
    
    public void setTarget(String target)
    {
        getStateHelper().put(PropertyKeys.target, target ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.TARGET_PROP);
    }    
    // Property: disabled
    public boolean isDisabled()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.disabled, false);
    }
    
    public void setDisabled(boolean disabled)
    {
        getStateHelper().put(PropertyKeys.disabled, disabled ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.DISABLED_PROP);
    }    
    // Property: disabledStyle
    public String getDisabledStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.disabledStyle);
    }
    
    public void setDisabledStyle(String disabledStyle)
    {
        getStateHelper().put(PropertyKeys.disabledStyle, disabledStyle ); 
    }    
    // Property: disabledStyleClass
    public String getDisabledStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.disabledStyleClass);
    }
    
    public void setDisabledStyleClass(String disabledStyleClass)
    {
        getStateHelper().put(PropertyKeys.disabledStyleClass, disabledStyleClass ); 
    }    
    // Property: activeOnViewIds
    final protected String getLocalActiveOnViewIds()
    {
        return (String) getStateHelper().get(PropertyKeys.activeOnViewIds);
    }
     
    public String getActiveOnViewIds()
    {
        return (String) getStateHelper().eval(PropertyKeys.activeOnViewIds);
    }
    
    public void setActiveOnViewIds(String activeOnViewIds)
    {
        getStateHelper().put(PropertyKeys.activeOnViewIds, activeOnViewIds ); 
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

    public void setValueBinding(String name, jakarta.faces.el.ValueBinding binding)
    {
        super.setValueBinding(name, binding);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    public void setValueExpression(String name, ValueExpression expression)
    {
        super.setValueExpression(name, expression);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    protected enum PropertyKeys
    {
         icon
        , split
        , open
        , active
        , immediate
        , externalLink
        , actionExpression
        , actionListener
        , target
        , disabled
        , disabledStyle
        , disabledStyleClass
        , activeOnViewIds
        , enabledOnUserRole
        , visibleOnUserRole
    }

 }
