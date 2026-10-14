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

import jakarta.faces.component.StateHolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Manfred Geiler (latest modification by $Author: grantsmith $)
 * @version $Revision: 472630 $ $Date: 2006-11-08 15:40:03 -0500 (Wed, 08 Nov 2006) $
 * %Log$
 */
class _SerializableListDataModel
        extends _SerializableDataModel
{
    private static final long serialVersionUID = 2579712878688635627L;
    //private static final Log log = LogFactory.getLog(_SerializableDataModel.class);

    public _SerializableListDataModel(int first, int rows, List list)
    {
        _first = first;
        _rows = rows;
        _rowCount = list.size();
        if (_rows <= 0)
        {
            _rows = _rowCount - first;
        }

        if (_rows == _rowCount)
        {
            //whole list must be saved
            if (list instanceof Serializable || list instanceof StateHolder)
            {
                _list = list;
            }
            else
            {
                //copy list
                _list = new ArrayList(list);
            }
        }
        else
        {
            int size = _rows > 0 && _rows < _rowCount ? _rows : _rowCount;
            _list = new ArrayList(size);
            if (size > _rowCount - _first)
            {
                size = _rowCount - _first;
            }
            for (int i = 0; i < size; i++)
            {
                _list.add(list.get(_first + i));
            }
        }
    }
}
