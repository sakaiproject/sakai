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
package org.apache.myfaces.custom.div;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.custom.htmlTag.HtmlTagRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Output"
 *   type = "org.apache.myfaces.DivRenderer"
 * @since 1.1.7
 * @author bdudney (latest modification by $Author: lu4242 $)
 * @version $Revision: 691871 $ $Date: 2008-09-03 23:32:08 -0500 (mié, 03 sep 2008) $
 */
public class DivRenderer extends HtmlTagRenderer
{
    public static final String RENDERER_TYPE = "org.apache.myfaces.HtmlTagRenderer";

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);
        
        HtmlRendererUtils.decodeClientBehaviors(context, component);
    }

    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
        if ((context == null) || (component == null))
        {
            throw new NullPointerException();
        }
        
        Div div = (Div) component;
        Map<String, List<ClientBehavior>> behaviors = div.getClientBehaviors();
        if (behaviors != null && !behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(context, context.getResponseWriter());
        }

        super.encodeBegin(context, component);

        if (div.isRendered())
        {
            ResponseWriter writer = context.getResponseWriter();
            //Previously, style and styleClass was rendered,
            //so on div renderer we need to add event and universal
            //attributes only
            if (behaviors != null && !behaviors.isEmpty())
            {
                HtmlRendererUtils.writeIdAndName(writer, component, context);
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.UNIVERSAL_ATTRIBUTES_WITHOUT_STYLE);
                HtmlRendererUtils.renderBehaviorizedEventHandlers(context, writer, component, behaviors);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, div, 
                        HTML.COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE);
            }
        }
    }

}