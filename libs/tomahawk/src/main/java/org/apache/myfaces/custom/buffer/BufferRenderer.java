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
package org.apache.myfaces.custom.buffer;

import java.io.IOException;
import java.util.Iterator;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.util.DummyFormUtils;
import org.apache.myfaces.renderkit.html.util.HtmlBufferResponseWriterWrapper;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

/**
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.Data"
 *   type = "org.apache.myfaces.Buffer" 
 * 
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (Sat, 24 May 2008) $
 */
public class BufferRenderer extends Renderer {
    private static final Log log = LogFactory.getLog(BufferRenderer.class);

    public static final String RENDERER_TYPE = "org.apache.myfaces.Buffer";

    private HtmlBufferResponseWriterWrapper getResponseWriter(FacesContext context) {
        return HtmlBufferResponseWriterWrapper.getInstance(context.getResponseWriter());
    }

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) {
        RendererUtils.checkParamValidity(facesContext, uiComponent, Buffer.class);
        facesContext.setResponseWriter( getResponseWriter(facesContext) );
    }

    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException{
        RendererUtils.checkParamValidity(facesContext, component, Buffer.class);
        RendererUtils.renderChildren(facesContext, component);
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) {
        Buffer buffer = (Buffer)uiComponent;
        HtmlBufferResponseWriterWrapper writer = (HtmlBufferResponseWriterWrapper) facesContext.getResponseWriter();
        buffer.fill(writer.toString(), facesContext);

        facesContext.setResponseWriter( writer.getInitialWriter() );

        if( DummyFormUtils.getDummyFormParameters(facesContext) != null ){
            try{ // Attempt to add the dummy form params (will not work with Sun RI)
                if( DummyFormUtils.isWriteDummyForm(facesContext) )
                    DummyFormUtils.setWriteDummyForm(facesContext, true );
                for(Iterator i = DummyFormUtils.getDummyFormParameters(facesContext).iterator() ; i.hasNext() ;)
                    DummyFormUtils.addDummyFormParameter(facesContext, i.next().toString() );
            } catch (Exception e) {
                log.warn("Dummy form parameters are not supported by this JSF implementation.");
            }
        }
    }

}