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

import jakarta.faces.component.UIColumn;

/**
 * Renders a HTML input of type "treeColumn". 
 * <p>
 * This tag outlines the column where the tree structure will be 
 * render as part of the tree table. Unless otherwise specified, 
 * all attributes accept static values or EL expressions.
 * </p>
 * <p>
 * Tree column model. This column is used to provide the place holder for the
 * tree.  This is used in conjunction with the table format display.
 * </p>
 * 
 * @JSFComponent
 *   name = "t:treeColumn"
 *   tagClass = "org.apache.myfaces.custom.tree.taglib.TreeColumnTag"
 * 
 * @author <a href="mailto:dlestrat@apache.org">David Le Strat</a>
 */
public class HtmlTreeColumn extends UIColumn
{
    /** The component type. */
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTreeColumn";
    
    /** The component family. */
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.HtmlTreeColumn";
    
    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public HtmlTreeColumn()
    {
        super();
    }
    
    /**
     * @see jakarta.faces.component.UIComponent#getFamily()
     */
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
