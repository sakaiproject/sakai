/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib;

import java.lang.reflect.Array;

// fredt@users - 1.8.0 - enhancements

/**
 * Intended as an asynchronous alternative to Vector.  Use HsqlLinkedList
 * instead if its better suited.
 *
 * @author dnordahl@users
 * @version 1.8.0
 * @since 1.7.0
 */
public class HsqlArrayList extends BaseList implements HsqlList {

//fredt@users
/*
    private static Reporter reporter = new Reporter();

    private static class Reporter {

        private static int initCounter   = 0;
        private static int updateCounter = 0;

        Reporter() {

            try {
                System.runFinalizersOnExit(true);
            } catch (SecurityException e) {}
        }

        protected void finalize() {

            System.out.println("HsqlArrayList init count: " + initCounter);
            System.out.println("HsqlArrayList update count: "
                               + updateCounter);
        }
    }
*/
    private static final int   DEFAULT_INITIAL_CAPACITY = 10;
    private static final float DEFAULT_RESIZE_FACTOR    = 2.0f;
    private Object[]           elementData;
    private boolean            minimizeOnClear;

    /** Creates a new instance of HsqlArrayList */
    public HsqlArrayList() {

//        reporter.initCounter++;
        elementData = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * Creates a new instance of HsqlArrayList that minimizes the size when
     * empty
     */
    public HsqlArrayList(boolean minimize) {

//        reporter.initCounter++;
        elementData     = new Object[DEFAULT_INITIAL_CAPACITY];
        minimizeOnClear = minimize;
    }

    /** Creates a new instance with the given initial capacity */
    public HsqlArrayList(int initialCapacity) {

//        reporter.initCounter++;
        if (initialCapacity < 0) {
            throw new NegativeArraySizeException(
                "Invalid initial capacity given");
        }

        if (initialCapacity == 0) {
            elementData = new Object[1];
        } else {
            elementData = new Object[initialCapacity];
        }
    }

    /** Inserts an element at the given index */
    public void add(int index, Object element) {

//        reporter.updateCounter++;
        if (index > elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + ">" + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        if (elementCount >= elementData.length) {
            increaseCapacity();
        }

        for (int i = elementCount; i > index; i--) {
            elementData[i] = elementData[i - 1];
        }

        elementData[index] = element;

        elementCount++;
    }

    /** Appends an element to the end of the list */
    public boolean add(Object element) {

//        reporter.updateCounter++;
        if (elementCount >= elementData.length) {
            increaseCapacity();
        }

        elementData[elementCount] = element;

        elementCount++;

        return true;
    }

    /** Gets the element at given position */
    public Object get(int index) {

        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " >= "
                                                + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        return elementData[index];
    }

    /** returns the index of given object or -1 if nt found */
    public int indexOf(Object o) {

        for (int i = 0; i < elementCount; i++) {
            if (elementData[i].equals(o)) {
                return i;
            }
        }

        return -1;
    }

    /** Removes and returns the element at given position */
    public Object remove(int index) {

        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " >= "
                                                + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        Object removedObj = elementData[index];

        for (int i = index; i < elementCount - 1; i++) {
            elementData[i] = elementData[i + 1];
        }

        elementCount--;

        elementData[elementCount] = null;

        if (minimizeOnClear && elementCount == 0) {
            elementData = new Object[DEFAULT_INITIAL_CAPACITY];
        }

        return removedObj;
    }

    /** Replaces the element at given position */
    public Object set(int index, Object element) {

        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " >= "
                                                + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        Object replacedObj = elementData[index];

        elementData[index] = element;

        return replacedObj;
    }

    /** Returns the number of elements in the array list */
    public final int size() {
        return elementCount;
    }

    private void increaseCapacity() {

        int baseSize = elementData.length == 0 ? 1
                                               : elementData.length;
        Object[] newArray =
            new Object[(int) (baseSize * DEFAULT_RESIZE_FACTOR)];

        System.arraycopy(elementData, 0, newArray, 0, elementData.length);

        elementData = newArray;
        newArray    = null;
    }

    /**
     * Returns an Enumeration of the elements of the list.  The Enumerator will
     * NOT throw a concurrent modification exception if the list is modified
     * during the enumeration.
     */
/*
    public Enumeration elements() {

        Enumeration en = new Enumeration() {

            private int pos = 0;

            public Object nextElement() {

                if (!hasMoreElements()) {
                    throw new NoSuchElementException("Enumeration complete");
                }

                pos++;

                return elementData[pos - 1];
            }

            public boolean hasMoreElements() {
                return (pos < elementCount);
            }
        };

        return en;
    }
*/

    /** Trims the array to be the same size as the number of elements. */
    public void trim() {

        // 0 size array is possible
        Object[] newArray = new Object[elementCount];

        System.arraycopy(elementData, 0, newArray, 0, elementCount);

        elementData = newArray;
        newArray    = null;
    }

    // fredt@users
    public void clear() {

        if (minimizeOnClear
                && elementData.length > DEFAULT_INITIAL_CAPACITY) {
            elementData  = new Object[DEFAULT_INITIAL_CAPACITY];
            elementCount = 0;

            return;
        }

        for (int i = 0; i < elementCount; i++) {
            elementData[i] = null;
        }

        elementCount = 0;
    }

    public void setSize(int newSize) {

        if (newSize < elementCount) {
            if (minimizeOnClear && newSize == 0
                    && elementData.length > DEFAULT_INITIAL_CAPACITY) {
                elementData  = new Object[DEFAULT_INITIAL_CAPACITY];
                elementCount = 0;

                return;
            }

            for (int i = newSize; i < elementCount; i++) {
                elementData[i] = null;
            }
        }

        elementCount = newSize;

        for (; elementCount > elementData.length; ) {
            increaseCapacity();
        }
    }

// fredt@users
    public Object[] toArray() {

        Object[] a = new Object[elementCount];

        System.arraycopy(elementData, 0, a, 0, elementCount);

        return a;
    }

    public Object[] toArray(int start, int limit) {

        Object[] a = new Object[elementCount - limit];

        System.arraycopy(elementData, start, a, 0, elementCount - limit);

        return a;
    }

    /**
     * Copies all elements of the list to a[]. It is assumed a[] is of the
     * correct type. If a[] is too small, a new array or the same type is
     * returned. If a[] is larger, only the list elements are copied and no
     * other change is made to the array.
     * Differs from the implementation in java.util.ArrayList in the second
     * aspect.
     */
    public Object toArray(Object a) {

        if (Array.getLength(a) < elementCount) {
            a = Array.newInstance(a.getClass().getComponentType(),
                                  elementCount);
        }

        System.arraycopy(elementData, 0, a, 0, elementCount);

        return a;
    }

    public void sort(ObjectComparator c) {

        if (elementCount < 2) {
            return;
        }

        Sort.sort(elementData, c, 0, elementCount - 1);
    }

    public Object[] getArray() {
        return elementData;
    }
}
