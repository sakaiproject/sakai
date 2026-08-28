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
package org.apache.myfaces.custom.tree;

import jakarta.faces.component.UISelectItem;

/**
 * Renders a HTML input of type "treeCheckbox". 
 * 
 * The associated comes from the treeCheckbox itemLabel and itemValue. 
 * The selected items come from an extended selectManyCheckbox 
 * component with layout "spread". The selectManyCheckbox is 
 * referenced by the "for" attribute. 
 * 
 * All HTML pass-through attributes for this input are taken from 
 * the associated selectManyCheckbox. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:treeCheckbox"
 *   class = "org.apache.myfaces.custom.tree.HtmlTreeCheckbox"
 *   tagClass = "org.apache.myfaces.custom.tree.taglib.TreeCheckboxTag"
 * @since 1.1.7
 * @author <a href="mailto:dlestrat@yahoo.com">David Le Strat</a>
 */
public abstract class AbstractHtmlTreeCheckbox extends UISelectItem
{
    
    /** The for attribute declaration. */
    public static final String FOR_ATTR = "for".intern();
    
    /** The component type. */
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTreeCheckbox";
    
    /** The component family. */
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.HtmlTreeCheckbox";
    
    /** The default renderer type. */
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTreeCheckbox";
        
    /**
     * id of the referenced extended selectManyCheckbox component
     * 
     * @JSFProperty
     * @return The for attribute.
     */
    public abstract String getFor();
    
}
