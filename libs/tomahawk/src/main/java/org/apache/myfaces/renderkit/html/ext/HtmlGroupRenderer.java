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
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.component.html.ext.HtmlPanelGroup;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.CommonEventUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlGroupRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;


/**
 * 
 * @author Martin Marinschek (latest modification by $Author: mmarinschek $)
 * @version $Revision: 167446 $ $Date: 2004-12-23 13:03:09Z $
 */
@JSFRenderer(
   renderKitId = "HTML_BASIC",
   family = "jakarta.faces.Panel",
   type = "org.apache.myfaces.Group")
public class HtmlGroupRenderer
    extends HtmlGroupRendererBase
{

    @Override
    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    @Override
    protected boolean isCommonEventsOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);
        
        HtmlRendererUtils.decodeClientBehaviors(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        boolean span = false;
        String element = getHtmlElement(component);
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (component instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(context, writer);
            }
        }

        if (behaviors != null && !behaviors.isEmpty())
        {
            //Render element and id to make javascript work
            span = true;
            writer.startElement(element, component);
            writer.writeAttribute(HTML.ID_ATTR, component.getClientId(context),null);
            if (isCommonPropertiesOptimizationEnabled(context))
            {
                long commonPropertiesMarked = CommonPropertyUtils.getCommonPropertiesMarked(component);
                
                CommonPropertyUtils.renderUniversalProperties(writer, commonPropertiesMarked, component);
                CommonPropertyUtils.renderStyleProperties(writer, commonPropertiesMarked, component);

                if (isCommonEventsOptimizationEnabled(context))
                {
                    Long commonEventsMarked = CommonEventUtils.getCommonEventsMarked(component);
                    CommonEventUtils.renderBehaviorizedEventHandlers(context, writer, 
                            commonPropertiesMarked, commonEventsMarked, component, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(context, writer, component, behaviors);
                }
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.UNIVERSAL_ATTRIBUTES);
                HtmlRendererUtils.renderBehaviorizedEventHandlers(context, writer, component, behaviors);
            }
        }
        else
        {
            //No behaviors, do it as usual
            if(component.getId()!=null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
            {
                span = true;

                writer.startElement(element, component);

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
                        writer.startElement(element, component);
                        HtmlRendererUtils.writeIdIfNecessary(writer, component, context);

                        CommonPropertyUtils.renderCommonPassthroughProperties(writer, commonPropertiesMarked, component);
                    }
                }
                else
                {
                    span=HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(writer,
                                                                                     component,
                                                                                     element,
                                                                                     HTML.COMMON_PASSTROUGH_ATTRIBUTES);
                }
            }
        }

        RendererUtils.renderChildren(context, component);
        if (span)
        {
            writer.endElement(element);
        }
    }
    
    private String getHtmlElement(UIComponent component) {
        if (component instanceof HtmlPanelGroup) {
            HtmlPanelGroup group = (HtmlPanelGroup) component;
            if (HtmlPanelGroup.BLOCK_LAYOUT.equals(group.getLayout())) {
                return HTML.DIV_ELEM;
            }
        }
        return HTML.SPAN_ELEM;
    }
}
