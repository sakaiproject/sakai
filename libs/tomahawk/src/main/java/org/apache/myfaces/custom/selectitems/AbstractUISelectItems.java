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
package org.apache.myfaces.custom.selectitems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

/**
 * An extended version of the standard UISelectItems. Populates the 
 * SelectItem collection from the given value automatically using 
 * the itemLabel and itemValue attributes. By using the component 
 * there is no need to manually create a SelectItem collection 
 * because component automatically populates SelectItem objects 
 * from types like Collection, Map and etc..
 * 
 * @JSFComponent
 *   name = "t:selectItems"
 *   class = "org.apache.myfaces.custom.selectitems.UISelectItems"
 *   tagClass = "org.apache.myfaces.custom.selectitems.SelectItemsTag"
 * @since 1.1.7
 * @author cagatay (latest modification by $Author: lu4242 $)
 * @version $Revision: 891039 $ $Date: 2009-12-15 17:29:01 -0500 (Tue, 15 Dec 2009) $
 */
public abstract class AbstractUISelectItems extends jakarta.faces.component.UISelectItems {
    
    public static final String COMPONENT_TYPE = "org.apache.myfaces.UISelectItems";
    
    /**
     * name of the iterator
     * 
     * @JSFProperty
     */
    public abstract String getVar();
    
    /**
     * name of the selectitem
     * 
     * @JSFProperty
     */
    public abstract Object getItemLabel();

    /**
     * value of the selectitem
     * 
     * @JSFProperty
     */
    public abstract Object getItemValue();
    
    /**
     * indicate if the label should be escaped of the selectitem
     * 
     * @since 1.1.9
     * @JSFProperty
     */
    public abstract Object getItemLabelEscaped();
    
    /**
     * name of the selectitem
     * 
     * @since 1.1.9
     * @JSFProperty
     */
    public abstract Object getItemDescription();
    
    /**
     * disabled state of the selectitem
     * 
     * @since 1.1.9
     * @JSFProperty
     */
    public abstract Object getItemDisabled();
    
    /**
     * Only applies when value points to a map. Use the Entry instance instead
     * the value for resolve EL Expressions
     * 
     * @since 1.1.10
     * @JSFProperty
     *    defaultValue = "false"
     */
    public abstract boolean isUseEntryAsItem();
    
    public Object getValue() {
        Object value = super.getValue();
        String var = getVar(); 
        if (var != null && var.length() > 0)
        {
            return createSelectItems(value);
        }
        else
        {
            return value;
        }
    }

    private SelectItem[] createSelectItems(Object value) {
        List items = new ArrayList();
        
        if (value instanceof SelectItem[]) {
            return (SelectItem[]) value;
        }
        else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            for (Iterator iter = collection.iterator(); iter.hasNext();) {
                Object currentItem = (Object) iter.next();
                if (currentItem instanceof SelectItemGroup) {
                    SelectItemGroup itemGroup = (SelectItemGroup) currentItem;        
                    SelectItem[] itemsFromGroup = itemGroup.getSelectItems();
                    for (int i = 0; i < itemsFromGroup.length; i++) {
                        items.add(itemsFromGroup[i]);
                    }
                }
                else {
                    putIteratorToRequestParam(currentItem);
                    SelectItem selectItem = createSelectItem();
                    removeIteratorFromRequestParam();
                    items.add(selectItem);
                }
            }
        }
        else if (value instanceof Map) {
            Map map = (Map) value;
            if (isUseEntryAsItem())
            {
                for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                    Entry currentItem = (Entry) iter.next();
                    putIteratorToRequestParam(currentItem);
                    SelectItem selectItem = createSelectItem();
                    removeIteratorFromRequestParam();
                    items.add(selectItem);
                }
            }
            else
            {
                for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                    Entry currentItem = (Entry) iter.next();
                    putIteratorToRequestParam(currentItem.getValue());
                    SelectItem selectItem = createSelectItem();
                    removeIteratorFromRequestParam();
                    items.add(selectItem);
                }
            }
        }
        
        return (SelectItem[]) items.toArray(new SelectItem[0]);
    }

    private SelectItem createSelectItem() {
        SelectItem item = null;
        Object value = getItemValue();
        String label = getItemLabel() != null ? getItemLabel().toString() : null;
        String description = getItemDescription() != null ? getItemDescription().toString() : null;
        Boolean disabled = (Boolean) (getItemDisabled() != null ? getItemDisabled() : Boolean.FALSE);
        Boolean escaped = (Boolean) (getItemLabelEscaped() != null ? getItemLabelEscaped() : Boolean.TRUE);
        
        if(label != null)
            item = new SelectItem(value, label, description, disabled, escaped);
        else
            item = new SelectItem(value, value == null ? null : value.toString(), description, disabled, escaped);
        
        return item;
    }
    
    private void putIteratorToRequestParam(Object object) {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(getVar(), object);
    }
    
    private void removeIteratorFromRequestParam() {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().remove(getVar());
    }
    
}

