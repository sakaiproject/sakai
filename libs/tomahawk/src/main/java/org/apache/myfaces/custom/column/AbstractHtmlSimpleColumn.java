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
package org.apache.myfaces.custom.column;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

/**
 * A tag that extend h:column to provide HTML passthrough attributes. 
 * Tag t:column can be used instead of h:column in a t:datatable. 
 * It provides HTML passthrough attributes for header (th), footer 
 * (td) and row cells (td). Unless otherwise specified, all 
 * attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:column"
 *   class = "org.apache.myfaces.custom.column.HtmlSimpleColumn"
 *   tagClass = "org.apache.myfaces.custom.column.HtmlColumnTag"
 * @since 1.1.7
 * @author Mathias Broekelmann (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlSimpleColumn extends UIColumn implements HtmlColumn,
    ClientBehaviorHolder
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlColumn";

    
    public boolean isGroupByValueSet()
    {
        return getGroupByValue() != null;
    }
    
    /**
     * This attribute tells the datatable to group by data in this column
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isGroupBy();


    /**
     *  Optional - Allows you configure where to get the value to 
     *  check for the group change condition. Default: all children 
     *  of the column cell will be checked
     * 
     * @JSFProperty
     */
    public abstract Object getGroupByValue();

    /**
     * This attribute tells the datatable to make this column the 
     * default sorted, when sortable=true
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isDefaultSorted();

    /**
     * This attribute makes this column automaticaly sortable 
     * by a row object's property
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isSortable();

    /**
     *  This attribute tells row object's property by which 
     *  sorting will be performed on this column
     * 
     * @JSFProperty
     */    
    public abstract String getSortPropertyName();

}
