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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.faces.model.ArrayDataModel;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.ResultDataModel;
import jakarta.faces.model.ResultSetDataModel;
import jakarta.faces.model.ScalarDataModel;
import jakarta.servlet.jsp.jstl.sql.Result;

/**
 * BaseSortableModel provides a DataModel that is automatically sorted by the specified Comparator.
 * Each time the Comparator is set, the model will be resorted.
 * @since 1.1.7
 * @version $Revision: 691871 $
 */
public class BaseSortableModel extends DataModel
{    
    private static final Class OBJECT_ARRAY_CLASS = (new Object[0]).getClass();
    
    protected DataModel _model = null;
    private Object _wrappedData = null;
    
    private IntList _sortedIndicesList = null,  // from baseIndex to sortedIndex
                    _baseIndicesList = null;    // from sortedIndex to baseIndex
    
    private Comparator _comparator = null;
    

    public Comparator getComparator() {
        return _comparator;
    }

    public void setComparator(Comparator comparator) {
        this._comparator = comparator;
        sort();
    }

    /**
     * Create a new SortableModel from the given instance.
     * @param model This will be converted into a {@link DataModel}
     * @see #setWrappedData
     */
    public BaseSortableModel(Object model) 
    {
        setWrappedData(model);
    }
    
    /**
     * No arg constructor for use as a managed-bean.
     * Must call setWrappedData before using this instance.
     */
    public BaseSortableModel(){}
    
    public Object getRowData() 
    {
        return _model.getRowData();
    }
    
    public Object getWrappedData() 
    {
        return _wrappedData;
    }
    
    public boolean isRowAvailable() 
    {
        return _model.isRowAvailable();
    }
    
    /**
     * Sets the underlying data being managed by this instance.
     * @param data This Object will be converted into a
     * {@link DataModel}.
     */
    public void setWrappedData(Object data) 
    {
        _baseIndicesList = null;
        _model = toDataModel(data);
        _sortedIndicesList = null;
        _wrappedData = data;
    }
    
    protected DataModel toDataModel(Object data) 
    {
        if (data == null)
        {
            return EMPTY_DATA_MODEL;
        }
        else if (data instanceof DataModel)
        {
            return (DataModel) data;
        }
        else if (data instanceof List)
        {
            return new ListDataModel((List) data);
        }
        // accept a Collection is not supported in the Spec
        else if (data instanceof Collection)
        {
            return new ListDataModel(new ArrayList((Collection) data));
        }
        else if (OBJECT_ARRAY_CLASS.isAssignableFrom(data.getClass()))
        {
            return new ArrayDataModel((Object[]) data);
        }
        else if (data instanceof ResultSet)
        {
            return new ResultSetDataModel((ResultSet) data);
        }
        else if (data instanceof Result)
        {
            return new ResultDataModel((Result) data);
        }
        else
        {
            return new ScalarDataModel(data);
        }
    }
    
    public int getRowCount() 
    {
        return _model.getRowCount();
    }
    
    public void setRowIndex(int rowIndex) 
    {
        int baseIndex = _toBaseIndex(rowIndex);
        _model.setRowIndex(baseIndex);
    }
    
    public int getRowIndex() 
    {
        int baseIndex = _model.getRowIndex();
        return _toSortedIndex(baseIndex);
    }       
    
    public String toString() 
    {
        return "BaseSortableModel[" + _model + "]";
    }
    
    /**
     * Sorts the underlying collection by the Comparator.
     * @todo support -1 for rowCount
     */
    public void sort() 
    {
        Comparator comparator = getComparator();
        if (null == comparator)
        {
            // restore unsorted order:
            _baseIndicesList = _sortedIndicesList = null;
            return;
        } 
        
        //TODO: support -1 for rowCount:
        int sz = getRowCount();
        if ((_baseIndicesList == null) || (_baseIndicesList.size() != sz)) 
        {
            // we do not want to mutate the original data.
            // however, instead of copying the data and sorting the copy,
            // we will create a list of indices into the original data, and
            // sort the indices. This way, when certain rows are made current
            // in this Collection, we can make them current in the underlying
            // DataModel as well.            
            _baseIndicesList = new IntList(sz);
        }
        
        final int rowIndex = _model.getRowIndex();
        
        _model.setRowIndex(0);
        // Make sure the model has that row 0! (It could be empty.)
        if (_model.isRowAvailable()) 
        {            
            Collections.sort(_baseIndicesList, new RowDataComparator(comparator, _model));
            _sortedIndicesList = null;
        }
        
        _model.setRowIndex(rowIndex);
    }
    
    private int _toSortedIndex(int baseIndex) 
    {
        if ((_sortedIndicesList == null) && (_baseIndicesList != null)) 
        {
            _sortedIndicesList = (IntList) _baseIndicesList.clone();
            for(int i=0; i<_baseIndicesList.size(); i++) 
            {
                Integer base = (Integer) _baseIndicesList.get(i);
                _sortedIndicesList.set(base.intValue(), new Integer(i));
            }
        }
        
        return _convertIndex(baseIndex, _sortedIndicesList);
    }
    
    private int _toBaseIndex(int sortedIndex) 
    {
        return _convertIndex(sortedIndex, _baseIndicesList);
    }
    
    private int _convertIndex(int index, List indices) 
    {
        if (index < 0) // -1 is special
            return index;
        
        if ((indices != null) && (indices.size() > index)) 
        {
            index = ((Integer) indices.get(index)).intValue();
        }
        return index;
    }              
    
    private static final class IntList extends ArrayList implements Cloneable 
    {
        public IntList(int size) 
        {
            super(size);
            _expandToSize(size);
        }
        
        private void _expandToSize(int desiredSize) 
        {
            for(int i=0; i<desiredSize; i++) 
                add(new Integer(i));            
        }
    }
    
    protected static class RowDataComparator implements Comparator
    {
        private Comparator dataComparator = null;
        private DataModel dataModel = null;
        
        public RowDataComparator(Comparator comparator, DataModel model)
        {
            this.dataComparator = comparator;
            this.dataModel = model;
        }
        
        public int compare(Object arg1, Object arg2) {
            Integer r1 = (Integer)arg1;
            Integer r2 = (Integer)arg2;
            dataModel.setRowIndex(r1.intValue());
            Object rowData1 = dataModel.getRowData();
            dataModel.setRowIndex(r2.intValue());
            Object rowData2 = dataModel.getRowData();
            
            return dataComparator.compare(rowData1, rowData2);
        }
    }
    
    private static final DataModel EMPTY_DATA_MODEL = new _SerializableDataModel()
    {
        public boolean isRowAvailable()
        {
            return false;
        }

        public int getRowCount()
        {
            return 0;
        }

        public Object getRowData()
        {
            throw new IllegalArgumentException();
        }

        public int getRowIndex()
        {
            return -1;
        }

        public void setRowIndex(int i)
        {
            if (i < -1)
                throw new IndexOutOfBoundsException("Index < 0 : " + i);
        }

        public Object getWrappedData()
        {
            return null;
        }

        public void setWrappedData(Object obj)
        {
            if (obj == null)
                return; //Clearing is allowed
            throw new UnsupportedOperationException(this.getClass().getName()
                            + " UnsupportedOperationException");
        }
    };
}
