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
package org.apache.myfaces.shared_tomahawk.renderkit.html;

import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import java.io.IOException;

/**
 * @author Martin Marinschek
 * @version $Revision: 1187700 $ $Date: 2011-10-22 07:19:37 -0500 (Sat, 22 Oct 2011) $
 *          <p/>
 *          $Log: $
 */
public class HtmlGroupRendererBase
        extends HtmlRenderer 
{
    private static final String LAYOUT_BLOCK_VALUE = "block";

    public boolean getRendersChildren()
    {
        return true;
    }

    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
    }

    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException
    {
    }

    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException
    {
        ResponseWriter writer = context.getResponseWriter();
        boolean span = false;

        // will be SPAN or DIV, depending on the layout attribute value
        String layoutElement = HTML.SPAN_ELEM;

        HtmlPanelGroup panelGroup = (HtmlPanelGroup) component;

        // if layout is 'block', render DIV instead SPAN
        String layout = panelGroup.getLayout();
        if (layout != null && layout.equals(LAYOUT_BLOCK_VALUE))
        {
            layoutElement = HTML.DIV_ELEM;
        }

        if(component.getId()!=null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        {
            span = true;

            writer.startElement(layoutElement, component);

            HtmlRendererUtils.writeIdIfNecessary(writer, component, context);

            if (isCommonPropertiesOptimizationEnabled(context))
            {
                CommonPropertyUtils.renderCommonPassthroughProperties(writer, 
                        CommonPropertyUtils.getCommonPropertiesMarked(component), component);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }
        }
        else
        {
            if (isCommonPropertiesOptimizationEnabled(context))
            {
                long commonPropertiesMarked = CommonPropertyUtils.getCommonPropertiesMarked(component);
                if (commonPropertiesMarked > 0)
                {
                    span = true;
                    writer.startElement(layoutElement, component);
                    HtmlRendererUtils.writeIdIfNecessary(writer, component, context);

                    CommonPropertyUtils.renderCommonPassthroughProperties(writer, commonPropertiesMarked, component);
                }
            }
            else
            {
                span=HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(writer,
                                                                                 component,
                                                                                 layoutElement,
                                                                                 HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }
        }

        RendererUtils.renderChildren(context, component);
        if (span)
        {
            writer.endElement(layoutElement);
        }
    }

}
