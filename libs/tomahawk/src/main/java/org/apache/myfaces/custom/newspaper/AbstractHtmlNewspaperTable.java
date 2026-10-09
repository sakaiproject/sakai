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
package org.apache.myfaces.custom.newspaper;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlDataTable;

import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.DataProperties;

/**
 * Model for a table in multiple balanced columns.
 * 
 * The newspaperTable tag allows a long, narrow table to be wrapped
 * so that it becomes a short, wide table. This allows more information
 * to be shown on a single screen. This is commonly used to present
 * checkboxes for a long list of items.
 *  
 * A data table for rendering long skinny tables as short wide 
 * table by wrapping the table over a specified number of columns. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 *
 * @JSFComponent
 *   name = "t:newspaperTable"
 *   class = "org.apache.myfaces.custom.newspaper.HtmlNewspaperTable"
 *   tagClass = "org.apache.myfaces.custom.newspaper.HtmlNewspaperTableTag"
 * @since 1.1.7
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class AbstractHtmlNewspaperTable
        extends HtmlDataTable implements AlignProperty, DataProperties
{
    /** the component's renderers and type */
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlNewspaperTable";
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlNewspaperTable";

    /** the property names */
    public static final String NEWSPAPER_COLUMNS_PROPERTY = "newspaperColumns";
    public static final String SPACER_FACET_NAME = "spacer";
    
    private static final Integer DEFAULT_NEWSPAPER_COLUMNS = new Integer(1);

    /**
     * Set the number of columns the table will be divided over.
     * In other words, the number of columns to wrap the table over. Default: 1
     * 
     * @JSFProperty
     *   defaultValue = "1"
     */
    public abstract int getNewspaperColumns();
    
    /**
     * The orientation of the newspaper columns in the newspaper 
     * table - "horizontal" or "vertical". Default: vertical
     * 
     * @JSFProperty
     */
    public abstract String getNewspaperOrientation();    
    
    /**
     * Gets the spacer facet, between each pair of newspaper columns.
     */
    public UIComponent getSpacer() {
        return (UIComponent)getFacets().get(SPACER_FACET_NAME);
    }
    public void setSpacer(UIComponent spacer) {
        getFacets().put(SPACER_FACET_NAME, spacer);
    }

}