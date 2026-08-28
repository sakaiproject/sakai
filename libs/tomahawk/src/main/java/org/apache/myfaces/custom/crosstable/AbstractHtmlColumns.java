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
package org.apache.myfaces.custom.crosstable;

import jakarta.faces.component.behavior.ClientBehaviorHolder;

import org.apache.myfaces.custom.column.HtmlColumn;


/**
 * The tag allows dynamic columns in a datatable. 
 * 
 * The UIColumns component is used below a t:datatable to create a 
 * dynamic count of columns. It is used like a UIData component 
 * which iterates through a datamodel to create the columns. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:columns" 
 *   class = "org.apache.myfaces.custom.crosstable.HtmlColumns"
 *   tagClass = "org.apache.myfaces.custom.crosstable.HtmlColumnsTag"
 *   implements = "org.apache.myfaces.custom.column.HtmlColumn"
 *   defaultRendererType = ""
 * @since 1.1.7
 * @author Mathias Broekelmann (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlColumns extends UIColumns 
    implements HtmlColumn, ClientBehaviorHolder 
{
    
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlColumns";

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

    // the following are not implemented, but are in the HtmlColumn interface
    public String getColspan() {return null;}
    public void setColspan(String colspan) {}
    public String getHeadercolspan() {return null;}
    public void setHeadercolspan(String headercolspan) {}
    public String getFootercolspan() {return null;}
    public void setFootercolspan(String footercolspan) {}

    public String getColumnId() {
        return null;
    }

    public void setColumnId(String columnId) {
    }

}
