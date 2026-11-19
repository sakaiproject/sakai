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

package org.apache.myfaces.custom.toggle;

import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.UniversalProperties;

/**
 * Container class allows user to toggle between view/edit mode.
 * 
 * Extends PanelGroup. Allows user to toggle between 'view' mode and 'edit' mode. 
 * In the togglePanel, include a toggleLink. When the toggleLink is clicked, 
 * the rest of the group is shown, and the link is hidden.
 * 
 * @JSFComponent
 *   name = "t:togglePanel"
 *   class = "org.apache.myfaces.custom.toggle.TogglePanel"
 *   tagClass = "org.apache.myfaces.custom.toggle.TogglePanelTag"
 *   
 * @author Sharath
 * 
 */
public abstract class AbstractTogglePanel extends HtmlPanelGroup
    implements EventAware, UniversalProperties, ClientBehaviorHolder
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.TogglePanel";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.TogglePanel";

    public static final boolean DEFAULT_TOGGLED = false;

    public static final boolean DEFAULT_DISABLED = false;

    /**
     * You can set toggled to true to force the toggleGroup to always be in toggle 
     * mode. Default is false.
     * 
     * @JSFProperty
     *   defaultValue="false"
     * @return
     */
    public abstract boolean isToggled();
    
    public abstract void setToggled(boolean toggleMode);
    
    /**
     * @JSFProperty
     *   defaultValue="false"
     * @return
     */
    public abstract boolean isDisabled();
    
    public void processDecodes(FacesContext context)
    {
        super.processDecodes(context);

        String hiddenFieldId = this.getClientId(context) + "_hidden";
        String toggleMode = (String) context.getExternalContext().getRequestParameterMap().get(hiddenFieldId);

        if (toggleMode != null && toggleMode.trim().equals("1")) {
            this.setToggled(true);
        }
    }

    public void processUpdates(FacesContext context)
    {
        super.processUpdates(context);
        this.setToggled(false);
    }
}
