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

import jakarta.faces.model.DataModel;
import jakarta.faces.model.DataModelEvent;
import jakarta.faces.model.DataModelListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide a serializable equivalent of the standard DataModel classes.
 * <p>
 * The standard JSF UIData components accept a DataModel as the "value" for
 * the ordered sequence of objects to be rendered in the table. Various
 * types (List, array, ResultSet) are also accepted and automatically
 * wrapped in one of the standard DataModel classes.
 * <p>  
 * The standard DataModel classes are not Serializable by default, because
 * there is no state in the class which needs to be preserved between render
 * and postback. And the standard UIData components don't serialize the
 * data model object, just the EL expression for the "value" attribute; the
 * data itself is refetched when needed by re-evaluating the EL expression.
 * <p>
 * However there can be good reasons to serialize the list of data that is
 * <i>wrapped</i> by the DataModel along with the UIData component. For these
 * cases, the tomahawk t:dataTable component offers a "preserveDataModel" flag
 * that will automatically serialize the data model along with the
 * HtmlDataTable component; it does this by invoking the "value" binding of
 * the t:dataTable then creating an instance of this class or one of its
 * subclasses instead of the standard JSF DataModels.
 * <p>
 * This class performs two roles. It is the base implementation for specialised
 * classes that wrap various datatypes that can be returned from the table's
 * "value" binding. It also implements the case where the value object
 * returned is of type DataModel.
 * <p>
 * When the UIData's "value" binding returns a DataModel instance, this class
 * extracts each rowData object from the wrapped data of the original
 * DataModel and adds these objects to an instance of this class which
 * <i>is</i> Serializable. Of course the rowdata objects must be serializable
 * for this to work. As a side-effect, however, the original DataModel object
 * will be discarded, and replaced by an instance of this class. This means
 * that any special optimisations or behaviour of the concrete DataModel
 * subclass will be lost.
 *
 * @author Manfred Geiler (latest modification by $Author: grantsmith $)
 * @version $Revision: 472630 $ $Date: 2006-11-08 15:40:03 -0500 (Wed, 08 Nov 2006) $
 */
class _SerializableDataModel
        extends DataModel
        implements Serializable
{
    private static final long serialVersionUID = -3511848078295893064L;
    //private static final Log log = LogFactory.getLog(_SerializableDataModel.class);
    protected int _first;
    protected int _rows;
    protected int _rowCount;
    protected List _list;
    private transient int _rowIndex = -1;

    public _SerializableDataModel(int first, int rows, DataModel dataModel)
    {
        _first = first;
        _rows = rows;
        _rowCount = dataModel.getRowCount();
        if (_rows <= 0)
        {
            _rows = _rowCount - first;
        }
        _list = new ArrayList(rows);
        for (int i = 0; i < _rows; i++)
        {
            dataModel.setRowIndex(_first + i);
            if (!dataModel.isRowAvailable()) break;
            _list.add(dataModel.getRowData());
        }
        _rowIndex = -1;

        DataModelListener[] dataModelListeners = dataModel.getDataModelListeners();
        for (int i = 0; i < dataModelListeners.length; i++)
        {
            DataModelListener dataModelListener = dataModelListeners[i];
            addDataModelListener(dataModelListener);
        }
    }

    protected _SerializableDataModel()
    {
    }

    public int getFirst()
    {
        return _first;
    }

    public void setFirst(int first)
    {
        _first = first;
    }

    public int getRows()
    {
        return _rows;
    }

    public void setRows(int rows)
    {
        _rows = rows;
    }

    public boolean isRowAvailable()
    {
        return _rowIndex >= _first &&
            _rowIndex < _first + _rows &&
            _rowIndex < _rowCount &&
            _list.size() > _rowIndex - _first;
    }

    public int getRowCount()
    {
        return _rowCount;
    }

    public Object getRowData()
    {
        if (!isRowAvailable())
        {
            throw new IllegalStateException("row not available");
        }
        return _list.get(_rowIndex - _first);
    }

    public int getRowIndex()
    {
        return _rowIndex;
    }

    public void setRowIndex(int rowIndex)
    {
        if (rowIndex < -1)
        {
            throw new IllegalArgumentException();
        }

        int oldRowIndex = _rowIndex;
        _rowIndex = rowIndex;
        if (oldRowIndex != _rowIndex)
        {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, _rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (int i = 0; i < listeners.length; i++)
            {
                listeners[i].rowSelected(event);
            }
        }
    }

    public Object getWrappedData()
    {
        return _list;
    }

    public void setWrappedData(Object obj)
    {
        if (obj != null)
        {
            throw new IllegalArgumentException("Cannot set wrapped data of _SerializableDataModel");
        }
    }



    /*
    // StateHolder interface

    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[4];
        values[0] = new Integer(_first);
        values[1] = new Integer(_rows);
        values[2] = new Integer(_rowCount);
        values[3] = _list;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        _first    = ((Integer)values[0]).intValue();
        _rows     = ((Integer)values[1]).intValue();
        _rowCount = ((Integer)values[2]).intValue();
        _list     = (List)values[3];
    }

    public boolean isTransient()
    {
        return false;
    }

    public void setTransient(boolean newTransientValue)
    {
        throw new UnsupportedOperationException();
    }
    */
}
