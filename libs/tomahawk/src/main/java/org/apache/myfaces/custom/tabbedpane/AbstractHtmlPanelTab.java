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
package org.apache.myfaces.custom.tabbedpane;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlPanelGroup;

import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.UniversalProperties;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;

/**
 * TODO: Document this component.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:panelTab"
 *   class = "org.apache.myfaces.custom.tabbedpane.HtmlPanelTab"
 *   tagClass = "org.apache.myfaces.custom.tabbedpane.HtmlPanelTabTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlPanelTab
        extends HtmlPanelGroup  implements NamingContainer, 
        UserRoleAware, EventAware, UniversalProperties, ClientBehaviorHolder
{
    //private static final Log log = LogFactory.getLog(HtmlPanelTab.class);

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlPanelTab";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Panel";
    private static final String DEFAULT_RENDERER_TYPE = "jakarta.faces.Group";
    private static final boolean DEFAULT_DISABLED = false;

    /**
     * Label of this tab.
     * 
     * @JSFProperty
     */
    public abstract String getLabel();

    /**
     * HTML: When true, this element cannot receive focus.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isDisabled();

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) {
            return false;
        }
        return super.isRendered();
    }
}
