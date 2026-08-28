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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.faces.component.UIComponent;

/**
 * @author Leonardo Uribe
 * @since 1.1.10
 */
class _DetailStampFacetAndChildrenIterator implements Iterator<UIComponent>
{
    private UIComponent _detailStamp;
    private Iterator<UIComponent> _childrenIterator;

    _DetailStampFacetAndChildrenIterator(UIComponent detailStamp, List<UIComponent> childrenList)
    {
        _detailStamp = detailStamp;
        _childrenIterator = childrenList != null ? childrenList.iterator() : null;
    }

    public boolean hasNext()
    {
        boolean hasNext = (_detailStamp != null)
                || (_childrenIterator != null && _childrenIterator.hasNext());
        if (!hasNext)
        {
            _detailStamp = null;
            _childrenIterator = null;
        }
        
        return hasNext;
    }

    public UIComponent next()
    {
        if (_detailStamp != null)
        {
            UIComponent detailStamp = _detailStamp;
            _detailStamp = null;
            return detailStamp;
        }
        else if (_childrenIterator != null && _childrenIterator.hasNext())
        {
            return _childrenIterator.next();
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
    }
}
