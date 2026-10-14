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
package org.apache.myfaces.custom.toggle;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlGroupRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.ToggleGroup"
 * 
 */
public class ToggleGroupRenderer extends HtmlGroupRendererBase {

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);
        
        HtmlRendererUtils.decodeClientBehaviors(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ToggleGroup toggleGroup = (ToggleGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        Map<String, List<ClientBehavior>> behaviors = null;
        behaviors = toggleGroup.getClientBehaviors();
        if (!behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(context, writer);
        }
        
        writer.startElement( org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SPAN_ELEM, component );
        writer.writeAttribute(HTML.ID_ATTR, component.getClientId(context), null);

        if (behaviors != null && !behaviors.isEmpty())
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.UNIVERSAL_ATTRIBUTES);
            HtmlRendererUtils.renderBehaviorizedEventHandlers(context, writer, component, behaviors);
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes( writer, component, HTML.COMMON_PASSTROUGH_ATTRIBUTES );
        }

        RendererUtils.renderChildren( context, component );

        writer.endElement( HTML.SPAN_ELEM );
    }
}
