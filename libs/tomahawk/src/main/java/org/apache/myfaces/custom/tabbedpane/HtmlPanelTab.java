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
package org.apache.myfaces.custom.tabbedpane;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.tabbedpane.AbstractHtmlPanelTab.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlPanelTab extends org.apache.myfaces.custom.tabbedpane.AbstractHtmlPanelTab
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Panel";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlPanelTab";
    static public final String DEFAULT_RENDERER_TYPE = 
        "jakarta.faces.Group";


    public HtmlPanelTab()
    {
        setRendererType("jakarta.faces.Group");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    static private final java.util.Collection<String> CLIENT_EVENTS_LIST = 
        java.util.Collections.unmodifiableCollection(
            java.util.Arrays.asList(
             "click"
            , "dblclick"
            , "keydown"
            , "keypress"
            , "keyup"
            , "mousedown"
            , "mousemove"
            , "mouseout"
            , "mouseover"
            , "mouseup"
        ));

    public java.util.Collection<String> getEventNames()
    {
        return CLIENT_EVENTS_LIST;
    }

    @Override
    public void addClientBehavior(String eventName, jakarta.faces.component.behavior.ClientBehavior behavior)
    {
        super.addClientBehavior(eventName, behavior);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonEventConstants.markEvent(this, eventName);
    }

    
    // Property: label
    public String getLabel()
    {
        return (String) getStateHelper().eval(PropertyKeys.label);
    }
    
    public void setLabel(String label)
    {
        getStateHelper().put(PropertyKeys.label, label ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.LABEL_PROP);
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
    // Property: onclick
    public String getOnclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.onclick);
    }
    
    public void setOnclick(String onclick)
    {
        getStateHelper().put(PropertyKeys.onclick, onclick ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONCLICK_PROP);
    }    
    // Property: ondblclick
    public String getOndblclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.ondblclick);
    }
    
    public void setOndblclick(String ondblclick)
    {
        getStateHelper().put(PropertyKeys.ondblclick, ondblclick ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONDBLCLICK_PROP);
    }    
    // Property: onkeydown
    public String getOnkeydown()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeydown);
    }
    
    public void setOnkeydown(String onkeydown)
    {
        getStateHelper().put(PropertyKeys.onkeydown, onkeydown ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYDOWN_PROP);
    }    
    // Property: onkeypress
    public String getOnkeypress()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeypress);
    }
    
    public void setOnkeypress(String onkeypress)
    {
        getStateHelper().put(PropertyKeys.onkeypress, onkeypress ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYPRESS_PROP);
    }    
    // Property: onkeyup
    public String getOnkeyup()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeyup);
    }
    
    public void setOnkeyup(String onkeyup)
    {
        getStateHelper().put(PropertyKeys.onkeyup, onkeyup ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYUP_PROP);
    }    
    // Property: onmousedown
    public String getOnmousedown()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmousedown);
    }
    
    public void setOnmousedown(String onmousedown)
    {
        getStateHelper().put(PropertyKeys.onmousedown, onmousedown ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEDOWN_PROP);
    }    
    // Property: onmousemove
    public String getOnmousemove()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmousemove);
    }
    
    public void setOnmousemove(String onmousemove)
    {
        getStateHelper().put(PropertyKeys.onmousemove, onmousemove ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEMOVE_PROP);
    }    
    // Property: onmouseout
    public String getOnmouseout()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseout);
    }
    
    public void setOnmouseout(String onmouseout)
    {
        getStateHelper().put(PropertyKeys.onmouseout, onmouseout ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEOUT_PROP);
    }    
    // Property: onmouseover
    public String getOnmouseover()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseover);
    }
    
    public void setOnmouseover(String onmouseover)
    {
        getStateHelper().put(PropertyKeys.onmouseover, onmouseover ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEOVER_PROP);
    }    
    // Property: onmouseup
    public String getOnmouseup()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseup);
    }
    
    public void setOnmouseup(String onmouseup)
    {
        getStateHelper().put(PropertyKeys.onmouseup, onmouseup ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEUP_PROP);
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
    // Property: dir
    public String getDir()
    {
        return (String) getStateHelper().eval(PropertyKeys.dir);
    }
    
    public void setDir(String dir)
    {
        getStateHelper().put(PropertyKeys.dir, dir ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.DIR_PROP);
    }    
    // Property: lang
    public String getLang()
    {
        return (String) getStateHelper().eval(PropertyKeys.lang);
    }
    
    public void setLang(String lang)
    {
        getStateHelper().put(PropertyKeys.lang, lang ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.LANG_PROP);
    }    
    // Property: title
    public String getTitle()
    {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }
    
    public void setTitle(String title)
    {
        getStateHelper().put(PropertyKeys.title, title ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.TITLE_PROP);
    }    
    // Property: style
    public String getStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.style);
    }
    
    public void setStyle(String style)
    {
        getStateHelper().put(PropertyKeys.style, style ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLE_PROP);
    }    
    // Property: styleClass
    public String getStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }
    
    public void setStyleClass(String styleClass)
    {
        getStateHelper().put(PropertyKeys.styleClass, styleClass ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLECLASS_PROP);
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
         label
        , disabled
        , onclick
        , ondblclick
        , onkeydown
        , onkeypress
        , onkeyup
        , onmousedown
        , onmousemove
        , onmouseout
        , onmouseover
        , onmouseup
        , enabledOnUserRole
        , visibleOnUserRole
        , dir
        , lang
        , title
        , style
        , styleClass
    }

 }
