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
package org.apache.myfaces.custom.checkbox;

import jakarta.faces.component.UIComponentBase;

import org.apache.myfaces.component.UserRoleAware;

/**
 * Renders a HTML input of type "checkbox". 
 * The associated SelectItem comes from an extended selectManyCheckbox 
 * component with layout "spread". The selectManyCheckbox is 
 * referenced by the "for" attribute.
 * 
 * All HTML pass-through attributes for this input 
 * are taken from the associated selectManyCheckbox. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:checkbox"
 *   class = "org.apache.myfaces.custom.checkbox.HtmlCheckbox"
 *   tagClass = "org.apache.myfaces.custom.checkbox.HtmlCheckboxTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractHtmlCheckbox
    extends UIComponentBase implements UserRoleAware
{

    public static final String FOR_ATTR = "for".intern();
    public static final String INDEX_ATTR = "index".intern();

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlCheckbox";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.Checkbox";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Checkbox";

    /**
     * id of the referenced extended selectManyCheckbox component
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getFor();
    
    /**
     * n-th SelectItem of referenced UISelectMany starting with 0.
     * 
     * @JSFProperty
     *   defaultValue = "Integer.MIN_VALUE"
     *   required="true"
     */
    public abstract int getIndex();

}
