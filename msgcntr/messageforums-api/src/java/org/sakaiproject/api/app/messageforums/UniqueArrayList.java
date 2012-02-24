/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/UniqueArrayList.java $
 * $Id: UniqueArrayList.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UniqueArrayList implements List {
    private List _list = new ArrayList();
    
    public int size() {
        return _list.size();
    }

    public boolean isEmpty() {
        return _list.isEmpty();
    }

    public boolean contains(Object o) {
        return _list.contains(o);
    }

    public Iterator iterator() {
        return _list.iterator();
    }

    public Object[] toArray() {
        return _list.toArray();
    }

    public Object[] toArray(Object[] arg0) {
        return _list.toArray(arg0);
    }

    public boolean add(Object arg0) {
        if (_list.contains(arg0)) {
            return false;
        }
        return _list.add(arg0);
    }

    public boolean remove(Object o) {
        return _list.remove(o);
    }

    public boolean containsAll(Collection arg0) {
        return _list.containsAll(arg0);
    }

    public boolean addAll(Collection arg0) {
        boolean collectionMutated = false;
        for (Iterator iter = arg0.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (!_list.contains(element)) {
                _list.add(element);
                collectionMutated = true;
            }
        }
        return collectionMutated;
    }

    public boolean retainAll(Collection arg0) {
        return _list.retainAll(arg0);
    }

    public boolean removeAll(Collection arg0) {
        return _list.removeAll(arg0);
    }

    public void clear() {
        _list.clear();
    }

    public boolean addAll(int arg0, Collection arg1) {
        throw new UnsupportedOperationException();
    }

    public Object get(int index) {
        return _list.get(index);
    }

    public Object set(int arg0, Object arg1) {
        Object o = _list.get(arg0);
        if (!_list.contains(arg1)) {
            _list.set(arg0, arg1);
        }
        return o;
    }

    public void add(int arg0, Object arg1) {
        if (!_list.contains(arg1)) {
            _list.add(arg0, arg1);
        }
    }

    public Object remove(int index) {
        return _list.remove(index);
    }

    public int indexOf(Object o) {
        return _list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return _list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return _list.listIterator();
    }

    public ListIterator listIterator(int index) {
        return _list.listIterator(index);
    }

    public List subList(int fromIndex, int toIndex) {
        return _list.subList(fromIndex, toIndex);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return _list.equals(obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return _list.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return _list.toString();
    }

}
