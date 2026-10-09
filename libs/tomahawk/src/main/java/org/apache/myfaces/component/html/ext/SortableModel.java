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

package org.apache.myfaces.component.html.ext;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.model.DataModel;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class SortableModel extends BaseSortableModel
{    
    private static final Log log = LogFactory.getLog(SortableModel.class);
    
    private SortCriterion _sortCriterion = null;
    
    /**
     * Create a new SortableModel from the given instance.
     * @param model This will be converted into a {@link DataModel}
     * @see #setWrappedData
     */
    public SortableModel(Object model) 
    {
        super(model);
    }
    
    /**
     * No arg constructor for use as a managed-bean.
     * Must call setWrappedData before using this instance.
     */
    public SortableModel(){}
    
    /**
     * Sets the underlying data being managed by this instance.
     * @param data This Object will be converted into a
     * {@link DataModel}.
     */
    public void setWrappedData(Object data) 
    {
        super.setWrappedData(data);
        setSortCriteria(null);
    }
    
    /**
     * Checks to see if the underlying collection is sortable by the given property.
     * @param property The name of the property to sort the underlying collection by.
     * @return true, if the property implements java.lang.Comparable
     */
    public boolean isSortable(String property) 
    {
        final int oldIndex = _model.getRowIndex();
        try 
        {
            _model.setRowIndex(0);
            if (!_model.isRowAvailable())
                return false; // if there is no data in the table then nothing is sortable
            
            try 
            {   
                Object propertyValue = PropertyUtils.getProperty(_model.getRowData(),property);                
                
                // when the value is null, we don't know if we can sort it.
                // by default let's support sorting of null values, and let the user
                // turn off sorting if necessary:
                return (propertyValue instanceof Comparable) || (propertyValue == null);
            } 
            catch (RuntimeException e) 
            {
                // don't propagate this exception out. This is because it might break
                // the VE.
                log.warn(e);
                return false;
            }
            catch (Exception e) {
                log.warn(e);
                return false;
            }
        } 
        finally 
        {
            _model.setRowIndex(oldIndex);
        }
    }
    
    public List getSortCriteria() 
    {
        return (_sortCriterion == null) ? Collections.EMPTY_LIST : Collections.singletonList(_sortCriterion);
    }
    
    public void setSortCriteria(List criteria) 
    {
        if ((criteria == null) || (criteria.isEmpty())) 
        {
            _sortCriterion = null;
            setComparator(null);
        } 
        else 
        {
            SortCriterion sc = (SortCriterion) criteria.get(0);
            if ((_sortCriterion == null) || (!_sortCriterion.equals(sc))) 
            {
                _sortCriterion = sc;
                
                /*
                 * Sorts the underlying collection by the given property, in the
                 * given direction.
                 * @param property The name of the property to sort by. The value of this
                 * property must implement java.lang.Comparable.
                 * @param isAscending true if the collection is to be sorted in
                 * ascending order.
                 * @todo support -1 for rowCount
                 */
               Comparator comp = new Comp(_sortCriterion.getProperty());                       
                
                if (!_sortCriterion.isAscending())
                    comp = new Inverter(comp);
                setComparator(comp);
            }
        }
    }
    
    public String toString() 
    {
        return "SortableModel[" + _model + "]";
    }
    
    private final class Comp implements Comparator 
    {
        private final String _prop;
        
        private Collator _collator;
        
        private Map _collationKeys;
        
        public Comp(String property) 
        {            
            _prop = property;
            _collator = Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()); 
            _collationKeys = new HashMap();
        }               
        
        public int compare(Object o1, Object o2) 
        {
            Object value1 = null;
            Object value2 = null;
            try {
                value1 = PropertyUtils.getProperty(o1,_prop);  
                value2 = PropertyUtils.getProperty(o2,_prop);  
            }
            catch (Exception exc) {    
                log.error(exc);
            }
                                    
            if (value1 == null)
                return (value2 == null) ? 0 : -1;
            
            if (value2 == null)
                return 1;
            
            // ?? Sometimes, isSortable returns true
            // even if the underlying object is not a Comparable.
            // This happens if the object at rowIndex zero is null.
            // So test before we cast:
            
            if (value1 instanceof String) {
                //if the object is a String we best compare locale-sesitive
                CollationKey collationKey1 = getCollationKey((String)value1);
                CollationKey collationKey2 = getCollationKey((String)value2);
                
                return collationKey1.compareTo(collationKey2);
            }
            else if (value1 instanceof Comparable) 
            {
                return ((Comparable) value1).compareTo(value2);
            } 
            else 
            {
                // if the object is not a Comparable, then
                // the best we can do is string comparison:
                return value1.toString().compareTo(value2.toString());
            }
        }         
        
        private CollationKey getCollationKey(String propertyValue) {
            CollationKey key = (CollationKey)_collationKeys.get(propertyValue);
            if (key == null) {
                key = _collator.getCollationKey(propertyValue);
                _collationKeys.put(propertyValue, key);
            }
                
            return key;
        }
    }
    /**
     *
     */
    private static final class Inverter implements Comparator 
    {
        private final Comparator _comp;
        
        public Inverter(Comparator comp) 
        {
            _comp = comp;
        }
        
        public int compare(Object o1, Object o2) 
        {
            return _comp.compare(o2, o1);
        }              
    }      
}
