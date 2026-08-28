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
package org.apache.myfaces.custom.sortheader;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.HtmlCommandLink;
import org.apache.myfaces.component.html.ext.HtmlDataTable;

/**
 * Clickable sort column header. 
 * 
 * Must be nested inside an extended data_table tag. This tag is 
 * derived from the standard command_link tag and has the additional 
 * attributes columnName and arrow. 
 * 
 * Note: In contrast to normal command links, the default for the 
 * "immediate" attribute is "true". This is desirable as it avoids 
 * validating all input fields in the enclosing form when the column 
 * sort order changes. HOWEVER when the table contains input 
 * components "immediate" must be set to false; otherwise input 
 * fields will render blank after a sort, or will show their old 
 * values (ie will not appear to sort though output fields in the 
 * table will sort) when sort ordering is changed. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:commandSortHeader"
 *   class = "org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader"
 *   tagClass = "org.apache.myfaces.custom.sortheader.HtmlCommandSortHeaderTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractHtmlCommandSortHeader
        extends HtmlCommandLink
{
    private static final Log log = LogFactory.getLog(AbstractHtmlCommandSortHeader.class);

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlCommandSortHeader";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Command";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.SortHeader";

    /*
    public boolean isImmediate()
    {
        return true;
    }
    */

    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        if (event instanceof ActionEvent)
        {
            HtmlDataTable dataTable = findParentDataTable();
            if (dataTable == null)
            {
                log.error("CommandSortHeader has no MyFacesHtmlDataTable parent");
            }
            else
            {
                String colName = getColumnName();                                
                String currentSortColumn = dataTable.getSortColumn();                                
                
                boolean currentAscending = dataTable.isSortAscending();
                
                if (colName.equals(currentSortColumn))
                {
                    String propName = getPropertyName();                       
                    if (propName != null)
                        dataTable.setSortProperty(getPropertyName());                        
                    
                    dataTable.setSortColumn(getColumnName()); 
                    dataTable.setSortAscending(!currentAscending);
                }
                else
                {
                    dataTable.setSortProperty(getPropertyName());
                    dataTable.setSortColumn(getColumnName());
                    dataTable.setSortAscending(true);
                }
            }
        }
        super.broadcast(event);
    }       

    public HtmlDataTable findParentDataTable()
    {
        UIComponent parent = getParent();
        while (parent != null)
        {
            if (parent instanceof HtmlDataTable)
            {
                return (HtmlDataTable)parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * The name of this column. This name must uniquely identify this 
     * column among all other (sortable) columns in the same 
     * data_table. The sortColumn attribute of the embedding 
     * data_table reflects the current sort column (see extended 
     * data_table).
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getColumnName();

    /**
     * The property name associated with this column. This name must 
     * be one of the properties of the row object by which the sorting 
     * should be performed. The sortProperty attribute of the 
     * embedding data_table reflects the current sort property 
     * (see extended data_table).
     * 
     * @JSFProperty
     */
    public abstract String getPropertyName();

    /**
     * Indicates whether an arrow, that shows the sort direction 
     * should be rendered. Default: false
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isArrow();
    
}
