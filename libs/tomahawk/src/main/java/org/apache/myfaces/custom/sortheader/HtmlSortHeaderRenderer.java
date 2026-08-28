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

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.ext.HtmlLinkRenderer;

/**
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (Sat, 24 May 2008) $
 * $Log: HtmlSortHeaderRenderer.java,v $
 * Revision 1.6  2004/10/13 11:50:58  matze
 * renamed packages to org.apache
 *
 * Revision 1.5  2004/07/01 21:53:10  mwessendorf
 * ASF switch
 *
 * Revision 1.4  2004/06/04 12:10:35  royalts
 * added check on isArrow
 *
 * Revision 1.3  2004/05/18 14:31:38  manolito
 * user role support completely moved to components source tree
 *
 * Revision 1.2  2004/04/22 09:20:55  manolito
 * derive from HtmlLinkRendererBase instead of HtmlLinkRenderer
 *
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Command"
 *   type = "org.apache.myfaces.SortHeader"
 *
 */
public class HtmlSortHeaderRenderer
        extends HtmlLinkRenderer
{
    private static final String FACET_ASCENDING = "ascending";
    private static final String FACET_DESCENDING = "descending";

    //private static final Log log = LogFactory.getLog(HtmlSortHeaderRenderer.class);

    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, HtmlCommandSortHeader.class);
        if (UserRoleUtils.isEnabledOnUserRole(component))
        {
            HtmlCommandSortHeader sortHeader = (HtmlCommandSortHeader)component;
            HtmlDataTable dataTable = sortHeader.findParentDataTable();
            
            if (sortHeader.getColumnName().equals(dataTable.getSortColumn()))
            {
                UIComponent img = (dataTable.isSortAscending())
                                  ? sortHeader.getFacet(FACET_ASCENDING)
                                  : sortHeader.getFacet(FACET_DESCENDING);
                // render directional image
                if (img != null)
                {
                    RendererUtils.renderChild(facesContext, img);
                }
                // render directional character
                if (sortHeader.isArrow())
                {
                    ResponseWriter writer = facesContext.getResponseWriter();
                    writer.write((dataTable.isSortAscending()) ? "&#x2191;" : "&#x2193;");
                }
            }            
        }
        super.encodeEnd(facesContext, component);
    }

}
