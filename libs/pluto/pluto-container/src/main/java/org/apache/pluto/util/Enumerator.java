/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * Uitlity class to wraps an <code>Enumeration</code> around a Collection, i.e.
 * <code>Iterator</code> classes.
 */

public final class Enumerator implements Enumeration {


    // Iterator over which the Enumeration takes place
    private Iterator iterator = null;


    /**
     * Returns an Enumeration over the specified Collection.
     * @param collection Collection with values that should be enumerated
     */
    public Enumerator(Collection collection) {
        this(collection.iterator());
    }


    /**
     * Returns an Enumeration over the values of the specified Iterator.
     * @param iterator Iterator to be wrapped
     */
    public Enumerator(Iterator iterator) {
        super();
        this.iterator = iterator;
    }


    /**
     * Returns an Enumeration over the values of the specified Map.
     * @param map Map with values that should be enumerated
     */
    public Enumerator(Map map) {
        this(map.values().iterator());
    }


    /**
     * Tests if this enumeration contains more elements.
     * @return <code>true</code> if this enumeration contains at least one more
     *         element to provide, <code>false</code> otherwise.
     */
    public boolean hasMoreElements() {
        return (iterator.hasNext());
    }


    /**
     * Returns the next element of this enumeration.
     * @return the next element of this enumeration
     * @throws NoSuchElementException if no more elements exist
     */
    public Object nextElement() throws NoSuchElementException {
        return (iterator.next());
    }


}
