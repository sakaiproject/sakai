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
package org.apache.myfaces.component.html.ext;

import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.component.NewspaperTable;

/**
 * This component is used to render one row in a t:dataTable, when
 * ajaxRowRender="true" is set.
 * 
 * @author Leonardo Uribe
 */
@JSFComponent
public class HtmlTableRow extends UIPanel
{
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.HtmlTableRow"; 
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTableRow";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTableRow";

    private String _alternateClientId;
    
    public static final String DEFAULT_ID="row";
    
    public HtmlTableRow()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }
    
    @Override
    public String getClientId(FacesContext context)
    {
        String inheritedClientId = super.getClientId(context);
        if (getParent() instanceof HtmlDataTable)
        {
            HtmlDataTable dataTable = (HtmlDataTable) getParent();
            if (dataTable.getNewspaperColumns() > 1)
            {
                if (_alternateClientId == null)
                {
                    char separator = UINamingContainer.getSeparatorChar(context); 
                    _alternateClientId = dataTable.getClientId() + separator + 
                                         getNewspaperRowIndex(dataTable) + separator + getId(); 
                    return _alternateClientId;
                }
                else
                {
                    return _alternateClientId;
                }
            }
            else
            {
                return inheritedClientId;
            }
        }
        else
        {
            return inheritedClientId;
        }
    }
    
    public int getNewspaperRowIndex(HtmlDataTable dataTable)
    {
        int rowCount = dataTable.getRowCount();
        
        int first = dataTable.getFirst();
        int rows = dataTable.getRows();
        int last;

        if (rows <= 0)
        {
           last = rowCount;
        }
        else
        {
           last = first + rows;
           if (last > rowCount)
           {
               last = rowCount;
           }
        }

        int newspaperColumns = dataTable.getNewspaperColumns();
        int newspaperRows;
        if((last - first) % newspaperColumns == 0)
            newspaperRows = (last - first) / newspaperColumns;
        else newspaperRows = ((last - first) / newspaperColumns) + 1;

        int currentRow = dataTable.getRowIndex();
        int nr = NewspaperTable.NEWSPAPER_HORIZONTAL_ORIENTATION.equals(dataTable.getNewspaperOrientation()) ? 
                (currentRow-first)/newspaperColumns :
                (currentRow-first) % newspaperRows;
        
        return nr;
    }
    
    @Override
    public void setId(String id)
    {
        super.setId(id);
        _alternateClientId = null;
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
