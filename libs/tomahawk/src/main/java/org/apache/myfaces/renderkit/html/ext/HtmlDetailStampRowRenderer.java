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
import java.util.Iterator;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.component.html.ext.AbstractHtmlDataTable;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.crosstable.UIColumns;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;

@JSFRenderer(renderKitId = "HTML_BASIC", family = "org.apache.myfaces.HtmlDetailStampRow", type = "org.apache.myfaces.HtmlDetailStampRow")
public class HtmlDetailStampRowRenderer extends Renderer
{

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        super.encodeEnd(facesContext, component);
        UIData uiData = (UIData) component.getParent();
        UIComponent detailStampFacet = uiData.getFacet(AbstractHtmlDataTable.DETAIL_STAMP_FACET_NAME);
        if (uiData instanceof HtmlDataTable) {
            HtmlDataTable htmlDataTable = (HtmlDataTable) uiData;

            if (htmlDataTable.isCurrentDetailExpanded()) {

                boolean embedded = false;
                if (detailStampFacet != null) {
                    embedded = isEmbeddedTable(detailStampFacet);
                }

                ResponseWriter writer = facesContext.getResponseWriter();

                if (!embedded) {
                    writer.startElement(HTML.TR_ELEM, uiData);
                    writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext), null);
                    writer.startElement(HTML.TD_ELEM, uiData);
                    //TOMAHAWK-1087 datatable dont renders a detail correct 
                    //if a UIColumns is used we have to count UIColumns 
                    //elements as component.getRowCount()
                    //instead of just get the number of children available,
                    //so the colspan could be assigned correctly.
                    int childCount = 0;
                    for (Iterator childIter = uiData.getChildren().iterator();
                        childIter.hasNext();)
                    {
                        UIComponent childComp = (UIComponent) childIter.next();
                        if (childComp instanceof UIColumns)
                        {
                            UIColumns v = (UIColumns) childComp;
                            childCount += v.getRowCount();
                        }
                        else
                        {
                            childCount++;
                        }
                    }
                    writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(
                            childCount), null);
                }

                if (detailStampFacet != null) {
                    RendererUtils.renderChild(facesContext, detailStampFacet);
                }

                if (!embedded) {
                    writer.endElement(HTML.TD_ELEM);
                    writer.endElement(HTML.TR_ELEM);
                }
            }
            else
            {
                boolean embedded = false;
                if (detailStampFacet != null) {
                    embedded = isEmbeddedTable(detailStampFacet);
                }

                ResponseWriter writer = facesContext.getResponseWriter();

                if (!embedded) {
                    writer.startElement(HTML.TR_ELEM, uiData);
                    writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext), null);
                    writer.writeAttribute(HTML.STYLE_ATTR, "display:none", null);
                    writer.endElement(HTML.TR_ELEM);
                }
            }
        }
    }
    
    protected boolean isEmbeddedTable(UIComponent uiComponent) {
        boolean embedded = false;
        if (uiComponent instanceof HtmlDataTable) {
            HtmlDataTable table = (HtmlDataTable) uiComponent;
            embedded = table.isEmbedded();
        }
        return embedded;
    }

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }
    
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
    }
}
