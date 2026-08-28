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
import jakarta.faces.render.Renderer;

import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

/**
 * Base class to handle the document family
 *
 * @author Mario Ivankovits (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (jue, 03 jul 2008) $
 */
public abstract class AbstractDocumentRenderer extends Renderer
{
    protected abstract String getHtmlTag();
    protected abstract Class getDocumentClass();

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
            throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent,
                getDocumentClass());

        AbstractDocument document = (AbstractDocument) uiComponent;

        ResponseWriter writer = facesContext.getResponseWriter();

        if (document.hasState() && document.isEndState())
        {
            closeTag(facesContext, writer);
        }
        else
        {
            openTag(facesContext, writer, uiComponent);
        }
    }

    protected void openTag(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
        writer.startElement(getHtmlTag(), uiComponent);
    }

    protected void closeTag(FacesContext facesContext, ResponseWriter writer)
        throws IOException
    {
        writeBeforeEnd(facesContext);
        writer.endElement(getHtmlTag());
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
            throws IOException
    {
        AbstractDocument document = (AbstractDocument) uiComponent;

        ResponseWriter writer = facesContext.getResponseWriter();

        if (!document.hasState())
        {
            closeTag(facesContext, writer);
        }
    }

    protected void writeBeforeEnd(FacesContext facesContext) throws IOException
    {
    }
}