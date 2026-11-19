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

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.ext.HtmlCommandLink;
import org.apache.myfaces.renderkit.html.jsf.ExtendedHtmlLinkRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.Command"
 *   type = "org.apache.myfaces.Link" 
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 951635 $ $Date: 2010-06-04 21:19:57 -0500 (Fri, 04 Jun 2010) $
 */
public class HtmlLinkRenderer
        extends ExtendedHtmlLinkRenderer
{
    //private static final Log log = LogFactory.getLog(HtmlLinkRenderer.class);

    protected void renderCommandLinkStart(FacesContext facesContext,
                                          UIComponent component,
                                          String clientId,
                                          Object value,
                                          String style,
                                          String styleClass) throws IOException
    {
        //if link is disabled we render the nested components without the anchor
        if (UserRoleUtils.isEnabledOnUserRole(component) &&
                        !((HtmlCommandLink) component).isDisabled() )
        {
            super.renderCommandLinkStart(facesContext, component, clientId, value, style, styleClass);
        }
        else
        {
            //For a disabled HtmlCommandLink we want to
            //render a span-element instead of the Anchor

            //set disabledStyle if available
            if(((HtmlCommandLink) component).getDisabledStyle() != null)
            {
                style = ((HtmlCommandLink) component).getDisabledStyle();
            }

            if(((HtmlCommandLink) component).getDisabledStyleClass() != null)
            {
                styleClass = ((HtmlCommandLink) component).getDisabledStyleClass();
            }

            renderSpanStart(facesContext, component, clientId, value, style, styleClass);
            /*// render value as required by JSF 1.1 renderkitdocs
            if(value != null)
            {
                ResponseWriter writer = facesContext.getResponseWriter();

                writer.writeText(value.toString(), JSFAttr.VALUE_ATTR);
            }*/
        }
    }

    protected void renderOutputLinkStart(FacesContext facesContext, UIOutput output) throws IOException
    {
        //if link is disabled we render the nested components without the anchor
        if (UserRoleUtils.isEnabledOnUserRole(output))
        {
            super.renderOutputLinkStart(facesContext, output);
        }
    }

    protected void renderOutputLinkEnd(FacesContext facesContext,
            UIComponent component) throws IOException
    {
        if (UserRoleUtils.isEnabledOnUserRole(component))
        {
            super.renderOutputLinkEnd(facesContext, component);
        }
    }

    protected void renderCommandLinkEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        //if link is disabled we render the nested components without the anchor
        if (UserRoleUtils.isEnabledOnUserRole(component) &&
                        !((HtmlCommandLink) component).isDisabled() )
        {
            super.renderCommandLinkEnd(facesContext, component);
        }
        else
        {
            renderSpanEnd(facesContext, component);
        }
    }

    protected void renderSpanStart(FacesContext facesContext,
                                          UIComponent component,
                                          String clientId,
                                          Object value,
                                          String style,
                                          String styleClass) throws IOException
    {
    ResponseWriter writer = facesContext.getResponseWriter();

        String[] spanAttrsToRender;

        writer.startElement(HTML.SPAN_ELEM, component);

        spanAttrsToRender = HTML.COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE;

        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ID_ATTR, HTML.ID_ATTR, clientId);

        HtmlRendererUtils.renderHTMLAttributes(writer, component,
                                               spanAttrsToRender);
        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_ATTR, HTML.STYLE_ATTR,
                                              style);
        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_CLASS_ATTR, HTML.STYLE_CLASS_ATTR,
                                              styleClass);

        // render value as required by JSF 1.1 renderkitdocs
        if(value != null)
        {
            writer.writeText(value.toString(), JSFAttr.VALUE_ATTR);
        }
    }

    protected void renderSpanEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        // force separate end tag
        writer.writeText("", null);
        writer.endElement(HTML.SPAN_ELEM);
    }
}
