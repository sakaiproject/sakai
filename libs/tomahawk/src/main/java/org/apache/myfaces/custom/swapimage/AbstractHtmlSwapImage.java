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
package org.apache.myfaces.custom.swapimage;

import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.StyleAware;
import org.apache.myfaces.component.UniversalProperties;
import org.apache.myfaces.component.UserRoleUtils;

/**
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:swapImage"
 *   class = "org.apache.myfaces.custom.swapimage.HtmlSwapImage"
 *   tagClass = "org.apache.myfaces.custom.swapimage.HtmlSwapImageTag"
 * @since 1.1.7
 * @author Thomas Spiegl
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlSwapImage extends UIGraphic
    implements UniversalProperties, AlignProperty, StyleAware, EventAware, ClientBehaviorHolder
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlSwapImage";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Graphic";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.SwapImage";
    private static final boolean DEFAULT_ISMAP = false;

    /**
     * HTML: Specifies the width of the border of this element, in pixels. Deprecated in HTML 4.01.
     * 
     * @JSFProperty
     *   defaultValue="Integer.MIN_VALUE"
     */
    public abstract String getBorder();

    /**
     * HTML: The amount of white space to be inserted to the left and 
     * right of this element, in undefined units. 
     * Deprecated in HTML 4.01.
     * 
     * @JSFProperty
     */
    public abstract String getHspace();
    
    /**
     *  HTML: The amount of white space to be inserted above and 
     *  below this element, in undefined units. 
     *  Deprecated in HTML 4.01.
     * 
     * @JSFProperty
     */
    public abstract String getVspace();
    
    /**
     * the url of the image displayed onmouseover
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getSwapImageUrl();

    /**
     * activeImage will be rendered if: swapImage is a direct 
     * child of commandNavigation and the commandNavigation.isActive
     * 
     * @JSFProperty
     */
    public abstract String getActiveImageUrl();

    /**
     * HTML: Specifies alternative text that can be used by a browser 
     * that can't show this element.
     * 
     * @JSFProperty
     */
    public abstract String getAlt();

    /**
     * HTML: Overrides the natural height of this image, by specifying 
     * height in pixels.
     * 
     * @JSFProperty
     */
    public abstract String getHeight();

    /**
     * HTML: Specifies server-side image map handling for this image.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isIsmap();

    /**
     * HTML: A link to a long description of the image.
     * 
     * @JSFProperty
     */
    public abstract String getLongdesc();

    /**
     * HTML: Specifies an image map to use with this image.
     * 
     * @JSFProperty
     */
    public abstract String getUsemap();

    /**
     * HTML: Overrides the natural width of this image, by 
     * specifying width in pixels.
     * 
     * @JSFProperty
     */
    public abstract String getWidth();

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }

}
