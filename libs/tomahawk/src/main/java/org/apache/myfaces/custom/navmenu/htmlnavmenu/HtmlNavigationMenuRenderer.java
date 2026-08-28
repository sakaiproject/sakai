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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.navmenu.NavigationMenuItem;
import org.apache.myfaces.custom.navmenu.NavigationMenuUtils;
import org.apache.myfaces.custom.navmenu.UINavigationMenuItem;
import org.apache.myfaces.renderkit.html.ext.HtmlLinkRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * Many thanks to the guys from Swiss Federal Institute of Intellectual Property & Marc Bouquet
 * for helping to develop this component.
 *
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.NavigationMenu"
 *   
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Command"
 *   type = "org.apache.myfaces.NavigationMenu"
 *
 * @author Thomas Spiegl
 * @author Manfred Geiler
 */
@ListenerFor(systemEventClass=PreRenderViewAddResourceEvent.class)
public class HtmlNavigationMenuRenderer extends HtmlLinkRenderer
    implements ComponentSystemEventListener
{
    private static final Log log = LogFactory.getLog(HtmlNavigationMenuRenderer.class);

    public static final String RENDERER_TYPE = "org.apache.myfaces.NavigationMenu";

    private static final Integer ZERO_INTEGER = new Integer(0);

    private static final String HORIZ_MENU_SCRIPT = "HMenuIEHover.js";

    public void processEvent(ComponentSystemEvent event)
    {
        if (event.getComponent() instanceof HtmlPanelNavigationMenu)
        {
            HtmlPanelNavigationMenu panelNav = (HtmlPanelNavigationMenu) event.getComponent();
            if (HtmlNavigationMenuRendererUtils.isListLayout(panelNav))
            {
                if (panelNav.isRenderAll())
                {
                    addResourcesToHeader(FacesContext.getCurrentInstance());
                }
            }
        }
    }

    public boolean getRendersChildren() {
        return true;
    }

    public void decode(FacesContext facesContext, UIComponent component) {
        if (component instanceof HtmlCommandNavigationItem) {
            //HtmlCommandNavigation
            super.decode(facesContext, component);
        }
        else
        {
            HtmlRendererUtils.decodeClientBehaviors(facesContext, component);
        }
    }

    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException {
        if (component instanceof HtmlCommandNavigationItem) {
            //HtmlCommandNavigationItem
            super.encodeBegin(facesContext, component);
        }
    }

    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
        if (component instanceof HtmlCommandNavigationItem) {
            //HtmlCommandNavigationItem
            super.encodeChildren(facesContext, component);
        }
    }

    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
        if (component instanceof HtmlCommandNavigationItem) {
            //HtmlCommandNavigationItem
            super.encodeEnd(facesContext, component);
            return;
        }
        else
        {
            Map<String, List<ClientBehavior>> behaviors = null;
            if (component instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
                if (!behaviors.isEmpty())
                {
                    ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
                }
            }
        }
        RendererUtils.checkParamValidity(facesContext, component, HtmlPanelNavigationMenu.class);
        HtmlPanelNavigationMenu panelNav = (HtmlPanelNavigationMenu) component;

        if (HtmlNavigationMenuRendererUtils.isListLayout(panelNav)) {
            boolean preprocess = true;
            boolean clientStateSaving = facesContext.getApplication().getStateManager().isSavingStateInClient(facesContext);
            if (clientStateSaving) {
                // client statesaving
                HtmlPanelNavigationMenu panelNavPrev = findPreviousPanelNav(facesContext, panelNav);
                if (panelNavPrev != null) {
                    preprocess = false;
                    if (!panelNavPrev.equals(panelNav)) {
                        // substitute panelnav
                        UIComponent parent = panelNav.getParent();
                        int insertPos = parent.getChildren().indexOf(panelNav);
                        parent.getChildren().set(insertPos, panelNavPrev);
                        panelNavPrev.setParent(parent);
                        panelNav.setParent(null);
                        panelNav = panelNavPrev;
                    }
                }
            }
            else {
                // server statesaving
                if (panelNav.getPreprocessed() != null && panelNav.getPreprocessed().booleanValue())
                    preprocess = false;
            }
            if (preprocess) {
                panelNav.setPreprocessed(Boolean.TRUE);
                preprocessNavigationItems(facesContext, panelNav, panelNav.getChildren(), new UniqueId());
                if (!clientStateSaving) {
                    HtmlPanelNavigationMenu panelNavPrev = findPreviousPanelNav(facesContext, panelNav);
                    if (panelNavPrev != null) {
                        restoreOpenActiveStates(facesContext, panelNav, panelNavPrev.getChildren());
                    }
                }
            }
            // render list
            if (log.isDebugEnabled())
                HtmlNavigationMenuRendererUtils.debugTree(log, facesContext, panelNav.getChildren(), 0);
            renderListLayout(facesContext, panelNav);
        }
        else {
            renderTableLayout(facesContext, panelNav);
        }
    }

    private void restoreOpenActiveStates(FacesContext facesContext,
                                         HtmlPanelNavigationMenu panelNav, List children) {
        for (int i = 0, size = children.size(); i < size; i++) {
            UIComponent uiComponent = (UIComponent) children.get(i);
            if (uiComponent instanceof HtmlCommandNavigationItem) {
                HtmlCommandNavigationItem prevItem = (HtmlCommandNavigationItem) uiComponent;
                if (prevItem.isOpen() || prevItem.isActive()) {
                    HtmlCommandNavigationItem item = (HtmlCommandNavigationItem) panelNav.findComponent(uiComponent.getClientId(facesContext));
                    if (item != null) {
                        if (item.getActiveDirectly() != null && item.getActiveDirectly().booleanValue()) {
                            item.setActive(prevItem.isActive());
                        }
                        else {
                            copyValueBinding(prevItem, item, "active");
                        }

                        if (item.getOpenDirectly() != null && item.getOpenDirectly().booleanValue()) {
                            item.setOpen(prevItem.isOpen());
                        }
                        else {
                            copyValueBinding(prevItem, item, "open");
                        }
                        if (!panelNav.isExpandAll() || prevItem.isActive())
                            item.toggleOpen();
                        if (prevItem.isOpen())
                            restoreOpenActiveStates(facesContext, panelNav, prevItem.getChildren());
                    }
                }
            }
        }
    }

    private HtmlPanelNavigationMenu findPreviousPanelNav(FacesContext facesContext, HtmlPanelNavigationMenu panelNav) {
        UIViewRoot previousViewRoot = findPreviousRoot(facesContext);
        if (previousViewRoot != null) {
            return (HtmlPanelNavigationMenu) previousViewRoot.findComponent(panelNav.getClientId(facesContext));
        }
        return null;
    }

    private UIViewRoot findPreviousRoot(FacesContext facesContext) {
        return (UIViewRoot) facesContext.getExternalContext().getRequestMap().get(HtmlPanelNavigationMenu.PREVIOUS_VIEW_ROOT);
    }

    protected void renderListLayout(FacesContext facesContext, HtmlPanelNavigationMenu panelNav) throws IOException {
        //if (panelNav.isRenderAll())
        //    addResourcesToHeader(facesContext);

        ResponseWriter writer = facesContext.getResponseWriter();
        if (panelNav.getChildCount() > 0) {
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.startElement(HTML.UL_ELEM, panelNav);

            Map<String, List<ClientBehavior>> behaviors = panelNav.getClientBehaviors();
            
            if (behaviors != null && !behaviors.isEmpty())
            {
                writer.writeAttribute(HTML.ID_ATTR, panelNav.getClientId(facesContext),null);
                HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, panelNav, behaviors);
                HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.UL_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS); 

            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, panelNav, facesContext);
                HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.UL_PASSTHROUGH_ATTRIBUTES);
            }

            //iterate over the tree and toggleOpen if viewId in item.getActiveOnVieIds()
            activeOnViewId(panelNav, facesContext.getViewRoot().getViewId());
            //iterate over the tree and set every item open if expandAll
            if (panelNav.isExpandAll()) {
                expandAll(panelNav);
            }

            HtmlNavigationMenuRendererUtils.renderChildrenListLayout(facesContext, writer, panelNav, panelNav.getChildren(), 0);

            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.endElement(HTML.UL_ELEM);
        }
        else {
            if (log.isWarnEnabled()) log.warn("PanelNavaigationMenu without children.");
        }
    }

    private void renderTableLayout(FacesContext facesContext, HtmlPanelNavigationMenu panelNav) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        if (panelNav.getChildCount() > 0) {
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.startElement(HTML.TABLE_ELEM, panelNav);
            Map<String, List<ClientBehavior>> behaviors = panelNav.getClientBehaviors();
            
            if (behaviors != null && !behaviors.isEmpty())
            {
                writer.writeAttribute(HTML.ID_ATTR, panelNav.getClientId(facesContext),null);
                HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, panelNav, behaviors);
                HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.TABLE_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS); 
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, panelNav, facesContext);
                HtmlRendererUtils.renderHTMLAttributes(writer, panelNav, HTML.TABLE_PASSTHROUGH_ATTRIBUTES);                
            }
            if (panelNav.getStyle() == null && panelNav.getStyleClass() == null) {
                writer.writeAttribute(HTML.BORDER_ATTR, ZERO_INTEGER, null);
            }

            HtmlNavigationMenuRendererUtils.renderChildrenTableLayout(facesContext, writer, panelNav, panelNav.getChildren(), 0);

            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.endElement(HTML.TABLE_ELEM);
        }
        else {
            if (log.isWarnEnabled()) log.warn("PanelNavaigationMenu without children.");
        }
    }

    private void addResourcesToHeader(FacesContext context) {
        //AddResource addResource = AddResourceFactory.getInstance(context);
        //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlPanelNavigationMenu.class, HORIZ_MENU_SCRIPT);
        TomahawkResourceUtils.addOutputScriptResource(context,
                "oam.custom.navmenu.htmlnavmenu", HORIZ_MENU_SCRIPT);
    }

    /**
     * look for UINavigationMenuItem && UISelectItems & create components
     */
    private void preprocessNavigationItems(FacesContext facesContext, UIComponent parent,
                                           List children, UniqueId uniqueId) {
        for (int i = 0; i < children.size(); i++) {
            UIComponent child = (UIComponent) children.get(i);

            if (child instanceof HtmlCommandNavigationItem) {
                HtmlCommandNavigationItem navMenuItem = (HtmlCommandNavigationItem) child;
                preprocessNavigationItems(facesContext, navMenuItem, navMenuItem.getChildren(), uniqueId);
            }
            else if (child instanceof UINavigationMenuItem) {
                UINavigationMenuItem uiNavMenuItem = (UINavigationMenuItem) child;
                createHtmlCommandNavigationItem(facesContext, parent, i, uiNavMenuItem, uniqueId);
            }
            else if (child instanceof UISelectItems) {
                List list = new ArrayList();
                if (child.getId() == null) {
                    child.setId("testit");
                }
                NavigationMenuUtils.addNavigationMenuItems((UISelectItems) child, list);
                addUINavigationMenuItems(facesContext, parent, children, i + 1, list);
            }
        }
    }

    private void addUINavigationMenuItems(FacesContext facesContext, UIComponent parent, List children, int startIndex, List menuItems) {
        String clientId = parent.getClientId(facesContext);
        clientId = clientId.replaceAll(":", "_");
        for (int j = 0, sizej = menuItems.size(); j < sizej; j++) {
            NavigationMenuItem navigationMenuItem = (NavigationMenuItem) menuItems.get(j);
            UINavigationMenuItem uiNavigationMenuItem =
                (UINavigationMenuItem) facesContext.getApplication().createComponent(UINavigationMenuItem.COMPONENT_TYPE);
            uiNavigationMenuItem.setId(clientId + "_uinavmitem" + (startIndex + j));
            uiNavigationMenuItem.getClientId(facesContext); // create clientid
            children.add(startIndex++, uiNavigationMenuItem);
            uiNavigationMenuItem.setParent(parent);
            if (navigationMenuItem.getAction() != null) {
                uiNavigationMenuItem.setAction(HtmlNavigationMenuRendererUtils.getMethodBinding(facesContext, navigationMenuItem.getAction(), false));
            }
            if (navigationMenuItem.getActionListener() != null) {
                uiNavigationMenuItem.setActionListener(HtmlNavigationMenuRendererUtils.getMethodBinding(facesContext,
                                                                                                        navigationMenuItem.getActionListener(), true));
            }
            uiNavigationMenuItem.setIcon(navigationMenuItem.getIcon());
            uiNavigationMenuItem.setRendered(navigationMenuItem.isRendered());
            uiNavigationMenuItem.setActiveOnViewIds(navigationMenuItem.getActiveOnViewIds());
            uiNavigationMenuItem.setSplit(navigationMenuItem.isSplit());
            uiNavigationMenuItem.setItemLabel(navigationMenuItem.getLabel());
            uiNavigationMenuItem.setOpen(navigationMenuItem.isOpen());
            uiNavigationMenuItem.setActive(navigationMenuItem.isActive());
            uiNavigationMenuItem.setValue(navigationMenuItem.getValue());
            HtmlNavigationMenuRendererUtils.setAttributeValue(facesContext, uiNavigationMenuItem,
                                                              "externalLink", navigationMenuItem.getExternalLink());
            //uiNavigationMenuItem.setExternalLink(navigationMenuItem.getExternalLink());
            uiNavigationMenuItem.setTransient(false);
            uiNavigationMenuItem.setTarget(navigationMenuItem.getTarget());
            uiNavigationMenuItem.setDisabled(navigationMenuItem.isDisabled());
            uiNavigationMenuItem.setDisabledStyle(navigationMenuItem.getDisabledStyle());
            uiNavigationMenuItem.setDisabledStyleClass(navigationMenuItem.getDisabledStyleClass());

            if (navigationMenuItem.getNavigationMenuItems() != null && navigationMenuItem.getNavigationMenuItems().length > 0)
            {
                addUINavigationMenuItems(facesContext, uiNavigationMenuItem, uiNavigationMenuItem.getChildren(), 0,
                                         Arrays.asList(navigationMenuItem.getNavigationMenuItems()));
            }
        }
    }

    private HtmlPanelNavigationMenu getParentPanelNavigation(UIComponent uiComponent) {
        if (uiComponent instanceof HtmlPanelNavigationMenu) {
            return (HtmlPanelNavigationMenu) uiComponent;
        }
        UIComponent parent = uiComponent.getParent();

        // search HtmlPanelNavigation
        UIComponent p = parent;
        while (p != null && !(p instanceof HtmlPanelNavigationMenu)) {
            p = p.getParent();
        }
        // p is now the HtmlPanelNavigation
        if (p == null) {
            log.error("HtmlCommandNavigation without parent HtmlPanelNavigation ?!");
            return null;
        }
        return (HtmlPanelNavigationMenu) p;
    }

    private void createHtmlCommandNavigationItem(FacesContext facesContext, UIComponent parent, int i,
                                                 UINavigationMenuItem uiNavMenuItem, UniqueId uniqueId) {
        HtmlPanelNavigationMenu menu = getParentPanelNavigation(parent);
        // Create HtmlCommandNavigationItem
        HtmlCommandNavigationItem newItem = (HtmlCommandNavigationItem)
            facesContext.getApplication().createComponent(HtmlCommandNavigationItem.COMPONENT_TYPE);
        String parentId = parent.getClientId(facesContext);
        parentId = parentId.replaceAll(":", "_");
        int id = uniqueId.next();
        newItem.setId(parentId + "_item" + id);
        newItem.getClientId(facesContext); // create clientid
        newItem.setRendererType(RENDERER_TYPE);
        parent.getChildren().add(i + 1, newItem);
        newItem.setParent(parent);
        // set action & actionListner
        newItem.setAction(uiNavMenuItem.getAction());
        newItem.setActionListener(uiNavMenuItem.getActionListener());
        ActionListener[] listeners = uiNavMenuItem.getActionListeners();
        for (int j = 0; j < listeners.length; j++) {
            newItem.addActionListener(listeners[j]);
        }
        // value
        newItem.setValue(uiNavMenuItem.getValue());
        // immeditate
        if (!copyValueBinding(uiNavMenuItem, newItem, "immediate"))
            newItem.setImmediate(uiNavMenuItem.isImmediate());
        // transient, rendered
        if (!copyValueBinding(uiNavMenuItem, newItem, "transient"))
            newItem.setTransient(uiNavMenuItem.isTransient());
        if (!copyValueBinding(uiNavMenuItem, newItem, "rendered"))
            newItem.setRendered(uiNavMenuItem.isRendered());
        if (!copyValueBinding(uiNavMenuItem, newItem, "externalLink"))
            newItem.setExternalLink(uiNavMenuItem.getExternalLink());
        if (!copyValueBinding(uiNavMenuItem, newItem, "activeOnViewIds"))
            newItem.setActiveOnViewIds(uiNavMenuItem.getActiveOnViewIds());

        if (uiNavMenuItem.isOpen() && ! menu.isExpandAll())
            newItem.toggleOpen();

        if (uiNavMenuItem.getActiveDirectly() != null) {
            newItem.setActive(uiNavMenuItem.isActive());
        }
        else {
            newItem.setValueBinding("active", uiNavMenuItem.getValueBinding("active"));
        }

        if (!copyValueBinding(uiNavMenuItem, newItem, "target"))
            newItem.setTarget(uiNavMenuItem.getTarget());
        if (!copyValueBinding(uiNavMenuItem, newItem, "disabled"))
            newItem.setDisabled(uiNavMenuItem.isDisabled());
        if (!copyValueBinding(uiNavMenuItem, newItem, "disabledStyle"))
            newItem.setDisabledStyle(uiNavMenuItem.getDisabledStyle());
        if (!copyValueBinding(uiNavMenuItem, newItem, "disabledStyleClass"))
            newItem.setDisabledStyleClass(uiNavMenuItem.getDisabledStyleClass());

        if (uiNavMenuItem.getActiveOnViewIdsDirectly() != null) {
            newItem.setActiveOnViewIds(uiNavMenuItem.getActiveOnViewIdsDirectly());
        }

        // If the parent-Element is disabled the child is disabled as well
        if (parent instanceof HtmlPanelNavigationMenu) {
            if (newItem.getDisabledStyle() == null) {
                newItem.setDisabledStyle(
                    ((HtmlPanelNavigationMenu) parent).getDisabledStyle()
                );
            }
            if (newItem.getDisabledStyleClass() == null) {
                newItem.setDisabledStyleClass(
                    ((HtmlPanelNavigationMenu) parent).getDisabledStyleClass()
                );
            }
            if (((HtmlPanelNavigationMenu) parent).isDisabled()) {
                newItem.setDisabled(true);
            }
        }
        if (parent instanceof HtmlCommandNavigationItem) {
            if (newItem.getDisabledStyle() == null) {
                newItem.setDisabledStyle(
                    ((HtmlCommandNavigationItem) parent).getDisabledStyle()
                );
            }
            if (newItem.getDisabledStyleClass() == null) {
                newItem.setDisabledStyleClass(
                    ((HtmlCommandNavigationItem) parent).getDisabledStyleClass()
                );
            }
            if (((HtmlCommandNavigationItem) parent).isDisabled()) {
                newItem.setDisabled(true);
            }
        }

        if (uiNavMenuItem.getIcon() != null) {
            UIGraphic uiGraphic = (UIGraphic) facesContext.getApplication().createComponent(UIGraphic.COMPONENT_TYPE);
            uiGraphic.setId(parentId + "_img" + id);
            uiGraphic.getClientId(facesContext);
            newItem.getChildren().add(uiGraphic);
            uiGraphic.setParent(newItem);
            if (NavigationMenuUtils.isValueReference(uiNavMenuItem.getIcon())) {
                uiGraphic.setValueBinding("value",
                                          facesContext.getApplication().createValueBinding(uiNavMenuItem.getIcon()));
            }
            else {
                uiGraphic.setValue(uiNavMenuItem.getIcon());
            }
        }

        else {
            // Create and add UIOutput
            UIOutput uiOutput = (UIOutput) facesContext.getApplication().createComponent(UIOutput.COMPONENT_TYPE);
            uiOutput.setId(parentId + "_txt" + id);
            uiOutput.getClientId(facesContext); // create clientid
            newItem.getChildren().add(uiOutput);
            uiOutput.setParent(newItem);
            if (uiNavMenuItem.getItemLabel() != null) {
                if (NavigationMenuUtils.isValueReference(uiNavMenuItem.getItemLabel())) {
                    uiOutput.setValueBinding("value",
                                             facesContext.getApplication().createValueBinding(uiNavMenuItem.getItemLabel()));
                }
                else {
                    uiOutput.setValue(uiNavMenuItem.getItemLabel());
                }
            }
            else {
                Object value = uiNavMenuItem.getValue();
                if (value != null &&
                    NavigationMenuUtils.isValueReference(value.toString())) {
                    uiOutput.setValueBinding("value",
                                             facesContext.getApplication().createValueBinding(value.toString()));
                }
                else {
                    uiOutput.setValue(uiNavMenuItem.getValue());
                }
            }
        }
        // process next level
        log.debug("Instance of UINavigationMenuItem, preprocess childrens");
        preprocessNavigationItems(facesContext, newItem, uiNavMenuItem.getChildren(), uniqueId);
    }

    private boolean copyValueBinding(UIComponent source, UIComponent target, String binding) {
        ValueBinding valueBinding = source.getValueBinding(binding);
        if (valueBinding == null)
            return false;
        target.setValueBinding(binding, valueBinding);
        return true;
    }

// protected

    protected String getStyle(FacesContext facesContext, UIComponent link) {
        if (!(link instanceof HtmlCommandNavigationItem)) {
            throw new IllegalArgumentException("expected instance of " + HtmlCommandNavigationItem.class.getName());
        }

        UIComponent navPanel = HtmlNavigationMenuRendererUtils.getPanel(link);

        HtmlCommandNavigationItem navItem = (HtmlCommandNavigationItem) link;
        if (navItem.isActive()) {
            return ((HtmlPanelNavigationMenu) navPanel).getActiveItemStyle();
        }
        else if (navItem.isOpen()) {
            return ((HtmlPanelNavigationMenu) navPanel).getOpenItemStyle();
        }
        else {
            return ((HtmlPanelNavigationMenu) navPanel).getItemStyle();
        }
    }

    protected String getStyleClass(FacesContext facesContext, UIComponent link) {
        if (!(link instanceof HtmlCommandNavigationItem)) {
            throw new IllegalArgumentException();
        }

        UIComponent navPanel = HtmlNavigationMenuRendererUtils.getPanel(link);

        HtmlCommandNavigationItem navItem = (HtmlCommandNavigationItem) link;
        if (navItem.isActive()) {
            return ((HtmlPanelNavigationMenu) navPanel).getActiveItemClass();
        }
        else if (navItem.isOpen()) {
            return ((HtmlPanelNavigationMenu) navPanel).getOpenItemClass();
        }
        else {
            return ((HtmlPanelNavigationMenu) navPanel).getItemClass();
        }
    }

    private static class UniqueId {
        private int _id;

        public int next() {
            return _id++;
        }

        public void decrease() {
            _id--;
        }
    }

    private void expandAll(UIComponent parent) {
        //Recurse over all Children setOpen if child is HtmlCommandNavigationItem
        if (parent instanceof HtmlCommandNavigationItem) {
            HtmlCommandNavigationItem navItem = (HtmlCommandNavigationItem) parent;
            navItem.setOpen(true);
        }
        List children = parent.getChildren();
        UIComponent child;
        for (int i = 0; i < children.size(); i++) {
            child = (UIComponent) children.get(i);
            expandAll(child);
        }
    }

    private void activeOnViewId(UIComponent parent, String viewId) {
        //Recurse over all Children setOpen if child is HtmlCommandNavigationItem
        if (parent instanceof HtmlCommandNavigationItem) {
            HtmlCommandNavigationItem navItem = (HtmlCommandNavigationItem) parent;
            String[] viewIds = navItem.getActiveOnVieIds();
            for (int i = 0; i < viewIds.length; i++) {
                if (viewId.equals(viewIds[i])) {
                    navItem.toggleOpen();
                    navItem.setActive(true);
                    return;
                }
            }
            ;
        }
        List children = parent.getChildren();
        UIComponent child;
        for (int i = 0; i < children.size(); i++) {
            child = (UIComponent) children.get(i);
            activeOnViewId(child, viewId);
        }
    }
}
