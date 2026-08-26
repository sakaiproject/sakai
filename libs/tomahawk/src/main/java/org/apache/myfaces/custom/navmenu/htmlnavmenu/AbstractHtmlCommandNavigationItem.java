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

import java.util.Iterator;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.HtmlCommandLink;

/**
 * Must be nested inside a panel_navigation action and renders a 
 * clickable navigation item. 
 * 
 * This action is derived from the standard command_link action 
 * and has equal attributes. (Replaces former "navigation_item" 
 * tag.) 
 * 
 * Unless otherwise specified, all attributes accept static 
 * values or EL expressions.
 * 
 * Many thanks to the guys from Swiss Federal Institute of Intellectual Property and Marc Bouquet
 * for helping to develop this component.
 *
 * @JSFComponent
 *   name = "t:commandNavigation2"
 *   class = "org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem"
 *   tagClass = "org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItemTag"
 * @since 1.1.7
 * @author Manfred Geiler
 * @author Thomas Spiegl
 */
public abstract class AbstractHtmlCommandNavigationItem extends HtmlCommandLink {
    private static final Log log = LogFactory.getLog(AbstractHtmlCommandNavigationItem.class);

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlCommandNavigationItem";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Command";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.NavigationMenu";
    
    /**
     * @JSFProperty
     *   defaultValue="true" 
     *   tagExcluded="true"
     */
    public boolean isImmediate() {
        //always immediate
        return true;
    }

    public void setImmediate(Boolean immediate) {
        if (log.isWarnEnabled()) log.warn("Immediate property of HtmlCommandNavigation cannot be set --> ignored.");
    }

    /**
     * Menu node is open.
     * 
     * @JSFProperty
     *   localMethod="true"
     *   setMethod="true"
     *   defaultValue="false";
     */
    public abstract boolean isOpen();
    
    protected abstract boolean isSetOpen();
    
    protected abstract boolean isLocalOpen();
    
    public abstract void setOpen(boolean open);
    
    public Boolean getOpenDirectly() {
        if (isSetOpen())
            return Boolean.valueOf(isLocalOpen());
        else
            return null;
    }
    
    public Boolean getActiveDirectly() {
        if (isSetActive())
            return Boolean.valueOf(isLocalActive());
        else
            return null;
    }    

    /**
     * Menu node is active.
     * 
     * @JSFProperty
     *   localMethod="true"
     *   setMethod="true"
     *   defaultValue="false";
     */
    public abstract boolean isActive();
    
    protected abstract boolean isSetActive();
    
    protected abstract boolean isLocalActive();
    
    public abstract void setActive(boolean active);
    
    /**
     * A comma separated list of viewIds for which this item 
     * should be active.
     * 
     * @JSFProperty
     */
    public abstract String getActiveOnViewIds();
    
    /**
     * The external link where to redirect when this is clicked.
     * 
     * @JSFProperty
     */    
    public abstract String getExternalLink();
    
    /**
     * @return false, if this item is child of another HtmlCommandNavigation, which is closed
     */
    public boolean isRendered() {
        if (! super.isRendered()) {
            return false;
        }

        HtmlPanelNavigationMenu parentPanelNavMenu = getParentPanelNavigation();
        if (parentPanelNavMenu != null && parentPanelNavMenu.isRenderAll())
            return true;

        UIComponent parent = getParent();
        while (parent != null) {
            if (parent instanceof AbstractHtmlCommandNavigationItem) {
                if (!((AbstractHtmlCommandNavigationItem) parent).isOpen()) {
                    return false;
                }
            }

            if (parent instanceof HtmlPanelNavigationMenu) {
                break;
            }
            else {
                parent = parent.getParent();
            }
        }

        return true;
    }

    private HtmlPanelNavigationMenu getParentPanelNavigation() {
        UIComponent parent = getParent();

        // search HtmlPanelNavigation
        UIComponent p = parent;
        while (p != null && !(p instanceof HtmlPanelNavigationMenu)) {
            p = p.getParent();
        }
        // p is now the HtmlPanelNavigation
        if (!(p instanceof HtmlPanelNavigationMenu)) {
            log.error("HtmlCommandNavigation without parent HtmlPanelNavigation ?!");
            return null;
        }

        return (HtmlPanelNavigationMenu) p;
    }

    public void toggleOpen() {
        HtmlPanelNavigationMenu menu = getParentPanelNavigation();
        if (isOpen() && menu != null && !menu.isExpandAll()) {
            if (getChildCount() > 0) {
                //item is a menu group --> close item
                setOpen(false);
            }
        }
        else {
            UIComponent parent = getParent();

            //close all siblings
            closeAllChildren(parent.getChildren().iterator(), this, true);

            //open all parents (to be sure) and search HtmlPanelNavigation
            UIComponent p = parent;
            AbstractHtmlCommandNavigationItem rootItem = null;
            while (p != null && !(p instanceof HtmlPanelNavigationMenu)) {
                if (p instanceof AbstractHtmlCommandNavigationItem) {
                    rootItem = (AbstractHtmlCommandNavigationItem) p;
                    rootItem.setOpen(true);
                }
                p = p.getParent();
            }
            if (rootItem != null) {
                List children = menu.getChildren();
                for (int i = 0, sizei = children.size(); i < sizei; i++) {
                    Object obj = children.get(i);
                    if (obj != rootItem && obj instanceof AbstractHtmlCommandNavigationItem) {
                        ((AbstractHtmlCommandNavigationItem)obj).setOpen(false);
                    }
                }
            }

            // p is now the HtmlPanelNavigation
            if (!(p instanceof HtmlPanelNavigationMenu)) {
                log.error("HtmlCommandNavigation without parent HtmlPanelNavigation ?!");
            }
            else {
                if (!hasCommandNavigationChildren() || ((HtmlPanelNavigationMenu) p).isExpandAll()) {
                    //item is an end node or Menu always expanded --> deactivate all other nodes, and then...

                    //deactivate all other items
                    deactivateAllChildren(p.getChildren().iterator());
                    //...activate this item
                    setActive(true);
                }
                else {
                    //open item
                    setOpen(true);
                }
            }
        }
    }

    private boolean hasCommandNavigationChildren() {
        if (getChildCount() == 0) {
            return false;
        }
        List list = getChildren();
        for (int i = 0, sizei = list.size(); i < sizei; i++) {
            if (list.get(i) instanceof AbstractHtmlCommandNavigationItem) {
                return true;
            }
        }
        return false;
    }


    private static void deactivateAllChildren(Iterator children) {
        while (children.hasNext()) {
            UIComponent ni = (UIComponent) children.next();
            if (ni instanceof AbstractHtmlCommandNavigationItem) {
                ((AbstractHtmlCommandNavigationItem) ni).setActive(false);
                if (ni.getChildCount() > 0) {
                    deactivateAllChildren(ni.getChildren().iterator());
                }
            }
        }
    }

    private static void closeAllChildren(Iterator children, AbstractHtmlCommandNavigationItem current, boolean resetActive) {
        while (children.hasNext()) {
            UIComponent ni = (UIComponent) children.next();
            if (ni instanceof AbstractHtmlCommandNavigationItem) {
                ((AbstractHtmlCommandNavigationItem) ni).setOpen(false);
                if (resetActive)
                    ((AbstractHtmlCommandNavigationItem) ni).setActive(false);
                if (ni.getChildCount() > 0) {
                    closeAllChildren(ni.getChildren().iterator(), current, current != ni);
                }
            }
        }
    }

    public String[] getActiveOnVieIds() {
        String value = getActiveOnViewIds();
        if (value == null)
            return new String[]{};
        return value.split(",");
    }

    public void deactivateAll() {
        UIComponent parent = this.getParent();
        while (!(parent instanceof HtmlPanelNavigationMenu) && parent != null) {
            parent = parent.getParent();
        }
        if (parent == null) {
            throw new IllegalStateException("no PanelNavigationMenu!");
        }

        HtmlPanelNavigationMenu root = (HtmlPanelNavigationMenu) parent;
        for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof AbstractHtmlCommandNavigationItem) {
                AbstractHtmlCommandNavigationItem navItem = (AbstractHtmlCommandNavigationItem) o;
                navItem.setActive(false);
                if (navItem.getChildCount() > 0) {
                    navItem.deactivateChildren();
                }
            }
        }
    }

    public void deactivateChildren() {
        for (Iterator it = this.getChildren().iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof AbstractHtmlCommandNavigationItem) {
                AbstractHtmlCommandNavigationItem current = (AbstractHtmlCommandNavigationItem) o;
                current.setActive(false);
                if (current.getChildCount() > 0) {
                    current.deactivateChildren();
                }
            }
        }
    }

    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof ActionEvent) {
            ActionEvent actionEvent = (ActionEvent) event;
            if (actionEvent.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
                AbstractHtmlCommandNavigationItem navItem = (AbstractHtmlCommandNavigationItem) actionEvent.getComponent();
                navItem.toggleOpen();
                FacesContext.getCurrentInstance().renderResponse();
            }
        }
        super.broadcast(event);
    }
}
