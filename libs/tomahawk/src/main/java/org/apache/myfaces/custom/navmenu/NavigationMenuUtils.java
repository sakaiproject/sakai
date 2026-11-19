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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.UserRoleUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Thomas Spiegl (latest modification by $Author: grantsmith $)
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public class NavigationMenuUtils
{
    private static final Log log = LogFactory.getLog(NavigationMenuUtils.class);

    public static List getNavigationMenuItemList(UIComponent uiComponent)
    {
        List list = new ArrayList(uiComponent.getChildCount());
        for (Iterator children = uiComponent.getChildren().iterator(); children.hasNext(); )
        {
            UIComponent child = (UIComponent)children.next();
            if (child instanceof UINavigationMenuItem)
            {
                NavigationMenuItem item;
                Object value = ((UINavigationMenuItem)child).getValue();
                if (value != null)
                {
                    //get NavigationMenuItem from model via value binding
                    if (!(value instanceof NavigationMenuItem))
                    {
                        FacesContext facesContext = FacesContext.getCurrentInstance();
                        throw new IllegalArgumentException("Value binding of UINavigationMenuItem with id " + child.getClientId(facesContext) + " does not reference an Object of type NavigationMenuItem");
                    }
                    item = (NavigationMenuItem)value;
                }
                else
                {
                    UINavigationMenuItem uiItem = (UINavigationMenuItem)child;
                    String itemLabel = uiItem.getItemLabel();
                    Object itemValue = uiItem.getItemValue();
                    if (itemValue == null) itemValue = "";
                    if (itemLabel == null)
                    {
                        itemLabel = itemValue.toString();
                    }
                    String actionStr = null;
                    MethodBinding action = uiItem.getAction();
                    if (action != null)
                    {
                        if (action.getExpressionString() != null)
                        {
                            actionStr = action.getExpressionString();
                        }
                        else
                        {
                            actionStr = action.toString();
                        }
                    }
                    item = new NavigationMenuItem(itemValue,
                                                  itemLabel,
                                                  uiItem.getItemDescription(),
                                                  uiItem.isItemDisabled() || ! UserRoleUtils.isEnabledOnUserRole(uiItem),
                                                  uiItem.isRendered(),
                                                  actionStr,
                                                  uiItem.getIcon(),
                                                  uiItem.isSplit());
                    if (uiItem.getActionListener() != null)
                        item.setActionListener(uiItem.getActionListener().getExpressionString());
                }

                list.add(item);
                if (child.getChildCount() > 0)
                {
                    List l = getNavigationMenuItemList(child);
                    item.setNavigationMenuItems((NavigationMenuItem[]) l.toArray(new NavigationMenuItem[l.size()]));
                }
            }
            else if (child instanceof UISelectItems)
            {
                addNavigationMenuItems((UISelectItems) child, list);
            }
            else
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                log.error("Invalid child with id " + child.getClientId(facesContext) + "of component with id : "+uiComponent.getClientId(facesContext)
                        +" : must be UINavigationMenuItem or UINavigationMenuItems, is of type : "+((child==null)?"null":child.getClass().getName()));
            }
        }

        return list;
    }

    public static void addNavigationMenuItems(UISelectItems child, List list)
    {
        Object value = child.getValue();

        if (value == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            throw new NullPointerException("Value binding of UINavigationMenuItems with id " + child.getClientId(facesContext) + " is null");
        }

        if (value instanceof NavigationMenuItem)
        {
            list.add(value);
        }
        else if (value instanceof NavigationMenuItem[])
        {
            for (int i = 0; i < ((NavigationMenuItem[])value).length; i++)
            {
                list.add(((NavigationMenuItem[])value)[i]);
            }
        }
        else if (value instanceof Collection)
        {
            for (Iterator it = ((Collection)value).iterator(); it.hasNext();)
            {
                Object item = it.next();
                if (!(item instanceof NavigationMenuItem))
                {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    throw new IllegalArgumentException("Collection referenced by UINavigationMenuItems with id " + child.getClientId(facesContext) + " does not contain Objects of type NavigationMenuItem");
                }
                list.add(item);
            }
        }
        else
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            throw new IllegalArgumentException("Value binding of UINavigationMenuItems with id " + child.getClientId(facesContext) + " does not reference an Object of type NavigationMenuItem, NavigationMenuItem[], Collection or Map");
        }
    }
    
    
    /**
     * Return true if the specified string contains an EL expression.
     * <p>
     * This is taken almost verbatim from {@link jakarta.faces.webapp.UIComponentTag}
     * in order to remove JSP dependencies from the renderers.
     */
    public static boolean isValueReference(String value)
    {
        if (value == null) return false;

        int start = value.indexOf("#{");
        if (start < 0) return false;

        int end = value.lastIndexOf('}');
        return (end >=0 && start < end);
    }

}
