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
package org.apache.myfaces.renderkit.html.ext;

import java.io.IOException;
import java.util.logging.Logger;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.component.html.ext.HtmlDataTable;

/**
 *
 * @author Leonardo Uribe
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "org.apache.myfaces.HtmlTableBody", type = "org.apache.myfaces.HtmlTableBody")
public class HtmlTableBodyRenderer extends HtmlTableRenderer
{
    private static final Logger log = Logger.getLogger(HtmlTableBodyRenderer.class.getName());
    
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {
        if (context == null)
            throw new NullPointerException("context");
        if (component == null)
            throw new NullPointerException("component");
    }
    
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException
    {
        // HtmlTableBodyElem is always inside a facet of t:dataTable.
        UIComponent parent = component.getParent();
        if (parent instanceof HtmlDataTable)
        {
            HtmlDataTable htmlDataTable = (HtmlDataTable) parent;
            if (htmlDataTable.isRenderedIfEmpty() || htmlDataTable.getRowCount() > 0)
            {
                encodeInnerHtml(facesContext, parent);
            }
        }
        else
        {
            encodeInnerHtml(facesContext, parent);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException
    {
        if (context == null)
            throw new NullPointerException("context");
        if (component == null)
            throw new NullPointerException("component");
    }
    
    public boolean getRendersChildren()
    {
        return true;
    }
    
    public void decode(FacesContext context, UIComponent component)
    {
        if (context == null)
            throw new NullPointerException("context");
        if (component == null)
            throw new NullPointerException("component");
    }

    public String convertClientId(FacesContext context, String clientId)
    {
        if (context == null)
            throw new NullPointerException("context");
        if (clientId == null)
            throw new NullPointerException("clientId");
        return clientId;
    }

    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
        throws ConverterException
    {
        if (context == null)
            throw new NullPointerException("context");
        if (component == null)
            throw new NullPointerException("component");
        return submittedValue;
    }
}
