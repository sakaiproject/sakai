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
package org.apache.myfaces.component.html.ext;

import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.DisplayValueOnlyAware;
import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.ForceIdAware;
import org.apache.myfaces.component.UniversalProperties;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.util.HtmlComponentUtils;
import org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable;

/**
 * Extends standard panelGroup with user role support. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:panelGroup"
 *   class = "org.apache.myfaces.component.html.ext.HtmlPanelGroup"
 *   tagClass = "org.apache.myfaces.generated.taglib.html.ext.HtmlPanelGroupTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlPanelGroup
        extends jakarta.faces.component.html.HtmlPanelGroup
        implements UserRoleAware, DisplayValueOnlyCapable,
        DisplayValueOnlyAware, ForceIdAware, EventAware, UniversalProperties,
        ClientBehaviorHolder
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlPanelGroup";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Group";

    public static final int DEFAULT_COLSPAN = Integer.MIN_VALUE;
    public static final String BLOCK_LAYOUT = "block";

    public String getClientId(FacesContext context)
    {
        String clientId = HtmlComponentUtils.getClientId(this, getRenderer(context), context);
        if (clientId == null)
        {
            clientId = super.getClientId(context);
        }

        return clientId;
    }

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }
    
    public boolean isSetDisplayValueOnly(){
        return getDisplayValueOnly() != null ? true : false;  
    }
    
    public boolean isDisplayValueOnly(){
        return getDisplayValueOnly() != null ? getDisplayValueOnly().booleanValue() : false;
    }
    
    public void setDisplayValueOnly(boolean displayValueOnly){
        this.setDisplayValueOnly((Boolean) Boolean.valueOf(displayValueOnly));
    }

    /**
     * Determines the type of layout that is used when rendering a 
     * panelGroup: when 'block' is specified, an HTML div is rendered 
     * instead of the default HTML span.
     * 
     * @JSFProperty
     *   defaultValue = "block" 
     */
    public abstract String getLayout(); 

    /**
     * standard html colspan attribute for table cell
     * 
     * @JSFProperty
     *   defaultValue = "Integer.MIN_VALUE"
     */
    public abstract int getColspan();

}
