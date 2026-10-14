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
package org.apache.myfaces.custom.navmenu;

import jakarta.faces.model.SelectItem;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * A node in a tree of menu items.<br>
 *
 * @author Thomas Spiegl (latest modification by $Author: grantsmith $)
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public class NavigationMenuItem extends SelectItem {
    private static final long serialVersionUID = 2801735314476639024L;
    private String _icon;
    private String _action;
    private String _actionListener;
    private boolean _open;
    private boolean _active;
    boolean _split;
    private boolean rendered = true;
    private List _navigationMenuItems = null;
    private String _target;
    private String _disabledStyle;
    private String _disabledStyleClass;
    private String _externalLink;
    private String _activeOnViewIds;

    // CONSTRUCTORS
    public NavigationMenuItem()
    {
        super();
    }

    public NavigationMenuItem(String label, String action) {
        super(label, label);
        _action = action;
        _icon = null;
        _split = false;
    }

    public NavigationMenuItem(String label, String action, String icon, boolean split) {
        super(label, label);
        _action = action;
        _icon = icon;
        _split = split;
    }

    public NavigationMenuItem(Object value,
                              String label,
                              String description,
                              boolean disabled,
                              String action,
                              String icon,
                              boolean split) {
        super(value, label, description, disabled);
        _action = action;
        _icon = icon;
        _split = split;
    }

    public NavigationMenuItem(Object value,
                              String label,
                              String description,
                              boolean disabled,
                              boolean rendered,
                              String action,
                              String icon,
                              boolean split) {
        this(value, label, description, disabled, action, icon, split);
        this.rendered = rendered;
    }

    public NavigationMenuItem(Object value,
                              String label,
                              String description,
                              boolean disabled,
                              boolean rendered,
                              String action,
                              String icon,
                              boolean split,
                              String target) {
        this(value, label, description, disabled, rendered, action, icon, split);
        this.setTarget(target);
    }

    public boolean isRendered() {
        return rendered;
    }

    public String getActiveOnViewIds() {
        return _activeOnViewIds;
    }

    public void setActiveOnViewIds(String activeOnViewIds) {
        _activeOnViewIds = activeOnViewIds;
    }


    public String getAction() {
        return _action;
    }

    public void setAction(String action) {
        _action = action;
    }

    public boolean isSplit() {
        return _split;
    }

    public void setSplit(boolean split) {
        _split = split;
    }

    public String getIcon() {
        return _icon;
    }

    public void setIcon(String icon) {
        _icon = icon;
    }

    /**
     * Relevant only for types of menus which can leave a menu in an "expanded"
     * state across requests. Returns true if the menu is currently open
     * (displaying its child items).
     */
    public boolean isOpen() {
        return _open;
    }

    public void setOpen(boolean open) {
        _open = open;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        _active = active;
    }

    public String getActionListener() {
        return _actionListener;
    }

    public void setActionListener(String actionListener) {
        _actionListener = actionListener;
    }

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public String getDisabledStyle() {
        return _disabledStyle;
    }

    public void setDisabledStyle(String disabledStyle) {
        _disabledStyle = disabledStyle;
    }

    public String getDisabledStyleClass() {
        return _disabledStyleClass;
    }

    public void setDisabledStyleClass(String disabledStyleClass) {
        _disabledStyleClass = disabledStyleClass;
    }

    public String getExternalLink() {
        return _externalLink;
    }

    public void setExternalLink(String externalLink) {
        _externalLink = externalLink;
    }

    /**
     * Get the array of child nodes of this menu item. If this node has
     * no children then an empty array is returned. The array is a copy
     * of the internal data of this object, so changes to the array will
     * not affect the state of this object. The members of the array
     * are the actual children of this object, however (the copy is not
     * a "deep clone").
     */
    public NavigationMenuItem[] getNavigationMenuItems() {
        if (_navigationMenuItems == null) {
            return new NavigationMenuItem[0];
        }
        return (NavigationMenuItem[]) _navigationMenuItems.toArray(
            new NavigationMenuItem[_navigationMenuItems.size()]);
    }

    /**
     * Set the child nodes of this menu item.
     */
    public void setNavigationMenuItems(NavigationMenuItem[] navigationMenuItems) {
        _navigationMenuItems = Arrays.asList(navigationMenuItems);
    }

    /**
     * Set the child nodes of this menu item.
     */
    public void setNavigationMenuItems(List list) {
        _navigationMenuItems = list;
    }

    /**
     * Add another node to the end of the list of child nodes of
     * this menu item.
     * <p/>
     * Note that if setNavigationMenuItems(array) was called previously,
     * then this method will throw an exception as the child list will
     * be of fixed length.
     */
    public void add(NavigationMenuItem navigationMenuItem) {
        if (_navigationMenuItems == null)
            _navigationMenuItems = new ArrayList();
        _navigationMenuItems.add(navigationMenuItem);
    }
}
