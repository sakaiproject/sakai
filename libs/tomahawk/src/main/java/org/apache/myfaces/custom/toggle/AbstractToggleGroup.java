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

import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.UniversalProperties;

/**
 * Container class allows user to toggle between view/edit mode.
 * 
 * Extends PanelGroup. Allows user to have several toggleLink in a group. 
 * When the togglePanel is toggled, the toggleGroup will be hidden.
 * 
 * @JSFComponent
 *   name = "t:toggleGroup"
 *   class = "org.apache.myfaces.custom.toggle.ToggleGroup"
 *   tagClass = "org.apache.myfaces.custom.toggle.ToggleGroupTag"
 *   
 * @author Sharath
 * 
 */
public abstract class AbstractToggleGroup extends HtmlPanelGroup
    implements EventAware, UniversalProperties, ClientBehaviorHolder
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.ToggleGroup";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.ToggleGroup";

    public AbstractToggleGroup()
    {
        super();
        setRendererType(AbstractToggleGroup.DEFAULT_RENDERER_TYPE);
    }
    
    /**
     * HTML: Flag to define the toggle status.
     * 
     * @JSFProperty
     */
     public abstract boolean isToggled();
    
     public abstract void setToggled(boolean toggleMode);
    
}
