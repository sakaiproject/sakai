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
package org.apache.myfaces.custom.selectOneRow;

import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.ChangeSelectProperties;
import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.FocusBlurProperties;

/**
 * Enhancement for a data-table to select one Row with a radio button. The row-index is stored in the vealu-binding
 * 
 * @JSFComponent
 *   name = "t:selectOneRow"
 *   class = "org.apache.myfaces.custom.selectOneRow.SelectOneRow"
 *   tagClass = "org.apache.myfaces.custom.selectOneRow.SelectOneRowTag"
 * @since 1.1.7
 */
public abstract class AbstractSelectOneRow extends UIInput 
    implements AlignProperty, EventAware, FocusBlurProperties, ChangeSelectProperties, ClientBehaviorHolder
{

    public static final String COMPONENT_TYPE = "org.apache.myfaces.SelectOneRow";

    public static final String COMPONENT_FAMILY = "org.apache.myfaces.SelectOneRow";

    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.SelectOneRow";

    /**
     * The Name of the radio-button-group to use. If EL expressions are used,
     * note that every time this is evaluated should lead to the same value
     * in the scope used, that means the UIData instance used, otherwise
     * it could lead to unwanted side effects.
     * 
     * @JSFProperty
     */
    public abstract String getGroupName();
    
    /**
     * HTML: When true, this element cannot receive focus.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isDisabled();   
    
    /**
     * HTML: When true, indicates that this component cannot be modified by the user.
     * The element may receive focus unless it has also been disabled.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isReadonly();    
    
}
