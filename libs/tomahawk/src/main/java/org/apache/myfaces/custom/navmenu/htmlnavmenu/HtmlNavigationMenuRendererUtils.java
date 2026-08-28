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
package org.apache.myfaces.custom.navmenu.htmlnavmenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.navmenu.NavigationMenuUtils;
import org.apache.myfaces.custom.navmenu.UINavigationMenuItem;
import org.apache.myfaces.shared_tomahawk.el.SimpleActionMethodBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Thomas Spiegl
 * @author Manfred Geiler
 */
class HtmlNavigationMenuRendererUtils {
    private static final Log log = LogFactory.getLog(HtmlNavigationMenuRendererUtils.class);

    private static final Class[] ACTION_LISTENER_ARGS = {ActionEvent.class};

    private HtmlNavigationMenuRendererUtils() {
    }

    public static void renderChildrenListLayout(FacesContext facesContext,
                                                ResponseWriter writer,
                                                HtmlPanelNavigationMenu panelNav,
                                                List children,
                                                int level) throws IOException {
        for (Iterator it = children.iterator(); it.hasNext();) {
            UIComponent child = (UIComponent) it.next();
            if (!child.isRendered()) continue;

            if (child instanceof UINavigationMenuItem) {
                renderChildrenListLayout(facesContext, writer, panelNav, child.getChildren(), level);
            }
            if (child instanceof HtmlCommandNavigationItem) {
                //navigation item
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);

                HtmlCommandNavigationItem navItem = (HtmlCommandNavigationItem) child;

                String externalLink = navItem.getExternalLink();

                String style = HtmlNavigationMenuRendererUtils.getNavigationItemStyle(panelNav, navItem);
                String styleClass = HtmlNavigationMenuRendererUtils.getNavigationItemClass(panelNav, navItem);

                writer.startElement(HTML.LI_ELEM, panelNav);
                HtmlNavigationMenuRendererUtils.writeStyleAttributes(writer, style, styleClass);

                Object value = navItem.getValue();
                boolean renderAsOutputLink = externalLink != null && value != null;

                if (!renderAsOutputLink) {
                    //if there is an external link specified don't render the command nav item, its content
                    //will be wrapped by a output link in the renderChildren() method
                    if (externalLink == null) {
                        navItem.setValue(null); // unset value, value must not be rendered
                        navItem.encodeBegin(facesContext);
                    }
                    HtmlNavigationMenuRendererUtils.renderChildren(facesContext, navItem, panelNav);
                    if (externalLink == null) {
                        navItem.encodeEnd(facesContext);
                        navItem.setValue(value); // restore value
                    }
                }
                else {
                    //there is an external link value and display value exists, so, just render its children
                    renderChildren(facesContext, navItem, panelNav);
                }

                if (hasCommandNavigationItemChildren(navItem)) {
                    writer.startElement(HTML.UL_ELEM, panelNav);

                    if (panelNav.isRenderAll())
                        HtmlNavigationMenuRendererUtils.writeStyleAttributes(writer, navItem.getStyle(), navItem.getStyleClass());

                    HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.UL_PASSTHROUGH_ATTRIBUTES);
                    renderChildrenListLayout(facesContext, writer, panelNav, child.getChildren(), level + 1);
                    writer.endElement(HTML.UL_ELEM);
                }

                writer.endElement(HTML.LI_ELEM);
            }
        }
    }

    private static boolean hasCommandNavigationItemChildren(HtmlCommandNavigationItem item) {
        List children = item.getChildren();
        for (int i = 0, sizei = children.size(); i < sizei; i++) {
            if (children.get(i) instanceof HtmlCommandNavigationItem) {
                return true;
            }
        }
        return false;
    }

    public static void renderChildrenTableLayout(FacesContext facesContext,
                                                 ResponseWriter writer,
                                                 HtmlPanelNavigationMenu panelNav,
                                                 List children,
                                                 int level) throws IOException {
        for (Iterator it = children.iterator(); it.hasNext();) {
            UIComponent child = (UIComponent) it.next();
            if (!child.isRendered()) continue;
            if (child instanceof HtmlCommandNavigationItem) {
                //navigation item
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);

                String style = getNavigationItemStyle(panelNav, (HtmlCommandNavigationItem) child);
                String styleClass = getNavigationItemClass(panelNav, (HtmlCommandNavigationItem) child);

                writer.startElement(HTML.TR_ELEM, panelNav);
                writer.startElement(HTML.TD_ELEM, panelNav);
                writeStyleAttributes(writer, style, styleClass);

                if (style != null || styleClass != null) {
                    writer.startElement(HTML.SPAN_ELEM, panelNav);
                    writeStyleAttributes(writer, style, styleClass);
                }
                indent(writer, level);
                child.encodeBegin(facesContext);

                child.encodeEnd(facesContext);
                if (style != null || styleClass != null) {
                    writer.endElement(HTML.SPAN_ELEM);
                }

                writer.endElement(HTML.TD_ELEM);
                writer.endElement(HTML.TR_ELEM);

                if (child.getChildCount() > 0) {
                    renderChildrenTableLayout(facesContext, writer, panelNav, child.getChildren(), level + 1);
                }
            }
            else {
                //separator
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);

                String style = panelNav.getSeparatorStyle();
                String styleClass = panelNav.getSeparatorClass();

                writer.startElement(HTML.TR_ELEM, panelNav);
                writer.startElement(HTML.TD_ELEM, panelNav);
                writeStyleAttributes(writer, style, styleClass);

                if (style != null || styleClass != null) {
                    writer.startElement(HTML.SPAN_ELEM, panelNav);
                    writeStyleAttributes(writer, style, styleClass);
                }
                indent(writer, level);
                RendererUtils.renderChild(facesContext, child);
                if (style != null || styleClass != null) {
                    writer.endElement(HTML.SPAN_ELEM);
                }

                writer.endElement(HTML.TD_ELEM);
                writer.endElement(HTML.TR_ELEM);
            }
        }
    }

    public static void indent(ResponseWriter writer, int level) throws IOException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < level; i++) {
            buf.append("&#160;&#160;&#160;&#160;");
        }
        writer.write(buf.toString());
    }

    public static String getNavigationItemStyle(HtmlPanelNavigationMenu navPanel, HtmlCommandNavigationItem navItem) {
        if (navItem.isActive()) {
            return navPanel.getActiveItemStyle();
        }
        else if (navItem.isOpen()) {
            return navPanel.getOpenItemStyle();
        }
        else {
            return navPanel.getItemStyle();
        }
    }

    public static String getNavigationItemClass(HtmlPanelNavigationMenu navPanel,
                                                HtmlCommandNavigationItem navItem) {
        // MYFACES-117, if a styleClass is supplied for a HtmlCommandNavigationItem,
        // panelNavigation active/open/normal styles for items will be overriden                       
        if (navItem.getStyleClass() != null) {
            return navItem.getStyleClass();
        }
        if (navItem.isActive()) {
            return navPanel.getActiveItemClass();
        }
        else if (navItem.isOpen()) {
            return navPanel.getOpenItemClass();
        }
        else {
            return navPanel.getItemClass();
        }
    }

    public static void writeStyleAttributes(ResponseWriter writer,
                                            String style,
                                            String styleClass) throws IOException {
        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_ATTR, HTML.STYLE_ATTR, style);
        HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_CLASS_ATTR, HTML.STYLE_CLASS_ATTR, styleClass);
    }

    public static UIComponent getPanel(UIComponent link) {
        UIComponent navPanel = link.getParent();
        while (navPanel != null && !(navPanel instanceof HtmlPanelNavigationMenu)) {
            navPanel = navPanel.getParent();
        }
        if (navPanel == null) {
            throw new IllegalStateException("HtmlCommandNavigationItem not nested in HtmlPanelNavigation!?");
        }
        return navPanel;
    }

    public static boolean isListLayout(HtmlPanelNavigationMenu panelNav) {
        return !"Table".equalsIgnoreCase(panelNav.getLayout());
    }

    public static void renderChildren(FacesContext facesContext, HtmlCommandNavigationItem component, HtmlPanelNavigationMenu parentPanelNav) throws IOException {
        if (component.getChildCount() > 0) {
            //if there is an external link value, wrapp the content with an output link
            if (component.getExternalLink() != null) {
                ResponseWriter writer = facesContext.getResponseWriter();

                writer.startElement(HTML.ANCHOR_ELEM, null);
                writer.writeAttribute(HTML.HREF_ATTR, component.getExternalLink(), null);
                if (component.getTarget() != null)
                    writer.writeAttribute(HTML.TARGET_ATTR, component.getTarget(), null);

                //the style attributes need to be taken from the parent panel nav, because the command panel navigation item
                //is not rendered in this case which would have render them
                String style = HtmlNavigationMenuRendererUtils.getNavigationItemStyle(parentPanelNav, component);
                String styleClass = HtmlNavigationMenuRendererUtils.getNavigationItemClass(parentPanelNav, component);
                HtmlNavigationMenuRendererUtils.writeStyleAttributes(writer, style, styleClass);
            }

            for (Iterator it = component.getChildren().iterator(); it.hasNext();) {
                UIComponent child = (UIComponent) it.next();
                if (!(child instanceof HtmlCommandNavigationItem)) {
                    RendererUtils.renderChild(facesContext, child);
                }
            }

            //end wrapper output link
            if (component.getExternalLink() != null) {
                ResponseWriter writer = facesContext.getResponseWriter();
                writer.endElement(HTML.ANCHOR_ELEM);
            }
        }
    }

    public static void debugTree(Log log, FacesContext facesContext, List children, int level) {
        for (Iterator it = children.iterator(); it.hasNext();) {
            UIComponent child = (UIComponent) it.next();
            if (child instanceof UINavigationMenuItem) {
                UINavigationMenuItem item = (UINavigationMenuItem) child;
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < level * 4; i++) buf.append(' ');
                log.debug(buf.toString() + "--> " + item.getItemLabel() + " id:" + item.getClientId(facesContext));
                debugTree(log, facesContext, child.getChildren(), level + 1);
            }
            else if (child instanceof HtmlCommandNavigationItem) {
                HtmlCommandNavigationItem item = (HtmlCommandNavigationItem) child;
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < level * 4; i++) buf.append(' ');
                String value;
                if (item.getChildren().size() > 0 && item.getChildren().get(0) instanceof UIOutput) {
                    UIOutput uiOutput = (UIOutput) item.getChildren().get(0);
                    value = uiOutput.getValue() != null ? uiOutput.getValue().toString() : "?";
                }
                else {
                    value = item.getValue() != null ? item.getValue().toString() : "";
                }
                log.debug(buf.toString() + value + " id:" + item.getClientId(facesContext));
                debugTree(log, facesContext, child.getChildren(), level + 1);
            }
        }
    }

    public static HtmlCommandNavigationItem findPreviousItem(UIViewRoot previousViewRoot, String clientId) {
        HtmlCommandNavigationItem previousItem = null;
        if (previousViewRoot != null) {
            UIComponent previousComp = previousViewRoot.findComponent(clientId);
            if (previousComp instanceof HtmlCommandNavigationItem) {
                previousItem = (HtmlCommandNavigationItem) previousComp;
            }
        }
        return previousItem;
    }

    public static MethodBinding getMethodBinding(FacesContext facesContext, String value, boolean actionListener) {
        MethodBinding mb;
        if (NavigationMenuUtils.isValueReference(value)) {
            mb = facesContext.getApplication().createMethodBinding(value, actionListener ? ACTION_LISTENER_ARGS : null);
        }
        else {
            if (actionListener) {
                log.error("Invalid actionListener value " + value + " (has to be ValueReference!)");
                mb = null;
            }
            else {
                mb = new SimpleActionMethodBinding(value);
            }
        }
        return mb;
    }

    public static void setAttributeValue(FacesContext facesContext, UIComponent comp, String attribute, String value) {
        if (value == null)
            return;
        if (NavigationMenuUtils.isValueReference(value)) {
            ValueBinding vb = facesContext.getApplication().createValueBinding(value);
            comp.setValueBinding(attribute, vb);
        }
        else {
            comp.getAttributes().put(attribute, value);
        }
    }
   
}
