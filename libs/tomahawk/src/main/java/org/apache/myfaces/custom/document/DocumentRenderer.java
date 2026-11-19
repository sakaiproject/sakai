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
package org.apache.myfaces.custom.document;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.util.renderkit.HTML;

/**
 * Document to enclose the whole document. If not otherwise possible you can use
 * state="start|end" to demarkate the document boundaries
 *
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.Data"
 *   type = "org.apache.myfaces.Document"
 *
 * @author Mario Ivankovits (latest modification by $Author: hoersch#his.de $)
 * @version $Revision: 1.1 $ $Date: 2012-06-23 17:38:52 $
 */
public class DocumentRenderer extends AbstractDocumentRenderer
{
    public static final String RENDERER_TYPE = "org.apache.myfaces.Document";

    private String[] ATTRS = new String[] {"xmlns", "lang"};

    private static final String LINE_SEPARATOR = System.getProperty(
                    "line.separator", "\r\n"); // make HtmlRendererUtils.LINE_SEPARATOR public?

    enum ConditionalCommentParts {
        IE6(    "<!--[if lt IE 7 ]>",             "ie ie6", "<![endif]-->"),
        IE7(    "<!--[if IE 7 ]>",                "ie ie7", "<![endif]-->"),
        IE8(    "<!--[if IE 8 ]>",                "ie ie8", "<![endif]-->"),
        IE9(    "<!--[if IE 9 ]>",                "ie ie9", "<![endif]-->"),
        DEFAULT("<!--[if (gt IE 9)|!(IE)]><!-->", null,     "<!--<![endif]-->");

        private final String _opening;
        private final String _styleClass;
        private final String _closing;

        private ConditionalCommentParts(String opening, String styleClass, String closing)
        {
            this._opening = opening;
            this._styleClass = styleClass;
            this._closing = closing;
        }

        String opening()
        {
            return _opening;
        }

        String styleClass()
        {
            return _styleClass;
        }

        String closing()
        {
            return _closing;
        }
    }

    @Override
    protected String getHtmlTag()
    {
        return "html";
    }

    @Override
    protected Class getDocumentClass()
    {
        return Document.class;
    }

    @Override
    protected void openTag(FacesContext facesContext, ResponseWriter writer,
            UIComponent uiComponent) throws IOException {

        Document document = (Document) uiComponent;
        if (document.isIncludeBrowserSelectors())
        {
            for (ConditionalCommentParts part : ConditionalCommentParts.values())
            {
                writer.append(part.opening());
                openTagIntern(facesContext, writer, uiComponent);
                if (part.styleClass() != null)
                {
                    writer.writeAttribute(HTML.CLASS_ATTR, part.styleClass(), null);
                }
                writer.append(part.closing());
                writer.append(LINE_SEPARATOR);
            }
        }
        else
        {
            openTagIntern(facesContext, writer, uiComponent);
        }
    }

    private void openTagIntern(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent) throws IOException
    {
        super.openTag(facesContext, writer, uiComponent);
        HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, ATTRS);
    }
}