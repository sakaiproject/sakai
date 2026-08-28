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
package org.apache.myfaces.custom.navigation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.ext.HtmlLinkRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.Navigation"
 *   
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Command"
 *   type = "org.apache.myfaces.Navigation"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (sáb, 24 may 2008) $
 */
public class HtmlNavigationRenderer
        extends HtmlLinkRenderer
{
    private static final Log log = LogFactory.getLog(HtmlNavigationRenderer.class);

    private static final Integer ZERO_INTEGER = new Integer(0);

    public boolean getRendersChildren()
    {
        return true;
    }

    public void decode(FacesContext facesContext, UIComponent component)
    {
        if (component instanceof HtmlCommandNavigation)
        {
            //HtmlCommandNavigation
            super.decode(facesContext, component);
        }
    }

    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        if (component instanceof HtmlCommandNavigation)
        {
            //HtmlCommandNavigation
            super.encodeBegin(facesContext, component);
        }
    }

    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException
    {
        if (component instanceof HtmlCommandNavigation)
        {
            //HtmlCommandNavigation
            super.encodeChildren(facesContext, component);
        }
    }

    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        if (component instanceof HtmlCommandNavigation)
        {
            //HtmlCommandNavigation
            super.encodeEnd(facesContext, component);
            return;
        }

        RendererUtils.checkParamValidity(facesContext, component, HtmlPanelNavigation.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        HtmlPanelNavigation panelNav = (HtmlPanelNavigation)component;
        
        Map<String, List<ClientBehavior>> behaviors = panelNav.getClientBehaviors();
        if (!behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
        }

        if (panelNav.getChildCount() > 0)
        {
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.startElement(HTML.TABLE_ELEM, component);
            
            if (behaviors != null && !behaviors.isEmpty())
            {
                writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext),null);
                HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, panelNav, behaviors);
                HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.TABLE_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS); 
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.TABLE_PASSTHROUGH_ATTRIBUTES);            
            }            
            
            boolean isBorderAlreadyDefined = component.getAttributes().containsKey(HTML.BORDER_ATTR);
            
            if (panelNav.getStyle() == null && panelNav.getStyleClass() == null && !isBorderAlreadyDefined)
            {
                writer.writeAttribute(HTML.BORDER_ATTR, ZERO_INTEGER, null);
            }

            renderChildren(facesContext, writer, panelNav, panelNav.getChildren(), 0);

            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.endElement(HTML.TABLE_ELEM);
        }
        else
        {
            if (log.isWarnEnabled()) log.warn("Navigation panel without children.");
        }
    }


    protected void renderChildren(FacesContext facesContext,
                                  ResponseWriter writer,
                                  HtmlPanelNavigation panelNav,
                                  List children,
                                  int level)
            throws IOException
    {
        for (Iterator it = children.iterator(); it.hasNext(); )
        {
            UIComponent child = (UIComponent)it.next();
            if (!child.isRendered()) continue;
            if (child instanceof HtmlCommandNavigation)
            {
                //navigation item
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);

                String style = getNavigationItemStyle(panelNav, (HtmlCommandNavigation)child);
                String styleClass = getNavigationItemClass(panelNav, (HtmlCommandNavigation)child);

                writer.startElement(HTML.TR_ELEM, panelNav);
                writer.startElement(HTML.TD_ELEM, panelNav);
                writeStyleAttributes(writer, style, styleClass);

                if (style != null || styleClass != null)
                {
                    writer.startElement(HTML.SPAN_ELEM, panelNav);
                    writeStyleAttributes(writer, style, styleClass);
                }
                indent(writer, level);
                child.encodeBegin(facesContext);
                child.encodeEnd(facesContext);
                if (style != null || styleClass != null)
                {
                    writer.endElement(HTML.SPAN_ELEM);
                }

                writer.endElement(HTML.TD_ELEM);
                writer.endElement(HTML.TR_ELEM);

                if (child.getChildCount() > 0)
                {
                    renderChildren(facesContext, writer, panelNav, child.getChildren(), level + 1);
                }
            }
            else
            {
                //separator
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);

                String style = panelNav.getSeparatorStyle();
                String styleClass = panelNav.getSeparatorClass();

                writer.startElement(HTML.TR_ELEM, panelNav);
                writer.startElement(HTML.TD_ELEM, panelNav);
                writeStyleAttributes(writer, style, styleClass);

                if (style != null || styleClass != null)
                {
                    writer.startElement(HTML.SPAN_ELEM, panelNav);
                    writeStyleAttributes(writer, style, styleClass);
                }
                indent(writer, level);
                RendererUtils.renderChild(facesContext, child);
                if (style != null || styleClass != null)
                {
                    writer.endElement(HTML.SPAN_ELEM);
                }

                writer.endElement(HTML.TD_ELEM);
                writer.endElement(HTML.TR_ELEM);
            }

        }
    }


    protected void indent(ResponseWriter writer, int level)
        throws IOException
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < level; i++)
        {
            buf.append("&#160;&#160;&#160;&#160;");
        }
        writer.write(buf.toString());
    }



    protected String getNavigationItemStyle(HtmlPanelNavigation navPanel,
                                            HtmlCommandNavigation navItem)
    {
        if (navItem.isActive())
        {
            return navPanel.getActiveItemStyle();
        }
        else if (navItem.isOpen())
        {
            return navPanel.getOpenItemStyle();
        }
        else
        {
            return navPanel.getItemStyle();
        }
    }

    protected String getNavigationItemClass(HtmlPanelNavigation navPanel,
                                            HtmlCommandNavigation navItem)
    {
        // MYFACES-117, if a styleClass is supplied for a HtmlCommandNavigation,
        // panelNavigation active/open/normal styles for items will be overriden
        if (navItem.getStyleClass() != null) 
        {
            return navItem.getStyleClass();
        }
        
        if (navItem.isActive())
        {
            return navPanel.getActiveItemClass();
        }
        else if (navItem.isOpen())
        {
            return navPanel.getOpenItemClass();
        }
        else
        {
            return navPanel.getItemClass();
        }
    }



    protected void writeStyleAttributes(ResponseWriter writer,
                                        String style,
                                        String styleClass)
            throws IOException
    {
        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_ATTR, HTML.STYLE_ATTR, style);
        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_CLASS_ATTR, HTML.STYLE_CLASS_ATTR, styleClass);
    }


    protected String getStyle(FacesContext facesContext, UIComponent link)
    {
        if (!(link instanceof HtmlCommandNavigation))
        {
            throw new IllegalArgumentException();
        }

        UIComponent navPanel = link.getParent();
        while (navPanel != null && !(navPanel instanceof HtmlPanelNavigation))
        {
            navPanel = navPanel.getParent();
        }
        if (navPanel == null)
        {
            throw new IllegalStateException("HtmlCommandNavigation not nested in HtmlPanelNavigation!?");
        }

        HtmlCommandNavigation navItem = (HtmlCommandNavigation)link;
        if (navItem.isActive())
        {
            return ((HtmlPanelNavigation)navPanel).getActiveItemStyle();
        }
        else if (navItem.isOpen())
        {
            return ((HtmlPanelNavigation)navPanel).getOpenItemStyle();
        }
        else
        {
            return ((HtmlPanelNavigation)navPanel).getItemStyle();
        }
    }


    protected String getStyleClass(FacesContext facesContext, UIComponent link)
    {
        if (!(link instanceof HtmlCommandNavigation))
        {
            throw new IllegalArgumentException();
        }

        UIComponent navPanel = link.getParent();
        while (navPanel != null && !(navPanel instanceof HtmlPanelNavigation))
        {
            navPanel = navPanel.getParent();
        }
        if (navPanel == null)
        {
            throw new IllegalStateException("HtmlCommandNavigation not nested in HtmlPanelNavigation!?");
        }

        HtmlCommandNavigation navItem = (HtmlCommandNavigation)link;
        if (navItem.isActive())
        {
            return ((HtmlPanelNavigation)navPanel).getActiveItemClass();
        }
        else if (navItem.isOpen())
        {
            return ((HtmlPanelNavigation)navPanel).getOpenItemClass();
        }
        else
        {
            return ((HtmlPanelNavigation)navPanel).getItemClass();
        }
    }



}
