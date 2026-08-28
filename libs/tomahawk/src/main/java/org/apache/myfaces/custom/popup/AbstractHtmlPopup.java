/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.popup;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;

import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.UniversalProperties;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;

/**
 * Renders a popup which displays on a mouse event. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:popup"
 *   class = "org.apache.myfaces.custom.popup.HtmlPopup"
 *   tagClass = "org.apache.myfaces.custom.popup.HtmlPopupTag"
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlPopup
        extends UIComponentBase implements 
        UniversalProperties, EventAware, UserRoleAware
{
    //private static final Log log = LogFactory.getLog(HtmlPopup.class);
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlPopup";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Panel";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Popup";

    private static final String POPUP_FACET_NAME            = "popup";

    public void setPopup(UIComponent popup)
    {
        getFacets().put(POPUP_FACET_NAME, popup);
    }

    public UIComponent getPopup()
    {
        return (UIComponent)getFacets().get(POPUP_FACET_NAME);
    }

    public boolean getRendersChildren()
    {
        return true;
    }
    
    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }

    /**
     * HTML: CSS styling instructions.
     * 
     * @JSFProperty
     */
    public abstract String getStyle();

    /**
     *  The CSS class for this element. Corresponds to the HTML 'class' attribute.
     * 
     * @JSFProperty
     */
    public abstract String getStyleClass();

    /**
     * Pop the panel up in horizontal distance of x pixels from event.
     * 
     * @JSFProperty
     */
    public abstract Integer getDisplayAtDistanceX();

    /**
     * Pop the panel up in vertical distance of y pixels from event.
     * 
     * @JSFProperty
     */
    public abstract Integer getDisplayAtDistanceY();

    /**
     * Close the popup when the triggering element is left.
     * 
     * @JSFProperty
     */
    public abstract Boolean getClosePopupOnExitingElement();

    /**
     * Close the popup when the popup itself is left.
     * 
     * @JSFProperty
     */
    public abstract Boolean getClosePopupOnExitingPopup();
    
    /**
     * The type of layout markup to use when rendering this group. If the value is "block"
     * the renderer must produce an HTML "div" element. If the value is "none", no tag is
     * rendered on the output and instead, onmouseover and onmouseout properties are modified
     * for children components. Otherwise HTML "span" element must be produced.
     *
     * @JSFProperty
     * @return  the new layout value
     */
    public abstract String getLayout();
    
}
