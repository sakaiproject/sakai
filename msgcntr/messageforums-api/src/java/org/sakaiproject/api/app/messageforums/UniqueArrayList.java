/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
