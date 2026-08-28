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

import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.ForceIdAware;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.util.HtmlComponentUtils;


/**
 * Extends standard commandLink by user role support and the HTML 
 * target attribute. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:commandLink"
 *   class = "org.apache.myfaces.component.html.ext.HtmlCommandLink"
 *   tagClass = "org.apache.myfaces.generated.taglib.html.ext.HtmlCommandLinkTag"
 * @since 1.1.7
 * @author Thomas Spiegl (latest modification by $Author: lu4242 $)
 * @author Manfred Geiler
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractHtmlCommandLink
        extends jakarta.faces.component.html.HtmlCommandLink
        implements UserRoleAware, ForceIdAware
{
        
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlCommandLink";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Link";

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

    /**
     *  Comma separated list of subForm-ids for which validation 
     *  and model update should take place when this command is 
     *  executed. You need to wrap your input components in 
     *  org.apache.myfaces.custom.subform.SubForm instances for 
     *  this to work.
     *  
     * @JSFProperty
     */
    public abstract String getActionFor();

    /**
     * When set instead of a Hyperlink a span tag is rendered in the corresponding Component
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isDisabled();

    /**
     * CSS-Style Attribute to render when disabled is true
     * 
     * @JSFProperty
     */
    public abstract String getDisabledStyle();

    /**
     * CSS-Style Class to use when disabled is true
     * 
     * @JSFProperty
     */
    public abstract String getDisabledStyleClass();

}
