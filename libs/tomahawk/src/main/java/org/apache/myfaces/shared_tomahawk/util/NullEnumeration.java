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
package org.apache.myfaces.shared_tomahawk.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Enumeration without elements
 *
 * @author Anton Koinov (latest modification by $Author: lu4242 $)
 * @version $Revision: 1202947 $ $Date: 2011-11-16 19:16:48 -0500 (Wed, 16 Nov 2011) $
 */
public final class NullEnumeration implements Enumeration
{
    private static final NullEnumeration NULL_ENUMERATION = new NullEnumeration();

    public static final NullEnumeration instance()
    {
        return NULL_ENUMERATION;
    }

    public boolean hasMoreElements()
    {
        return false;
    }

    public Object nextElement()
    {
        throw new NoSuchElementException("NullEnumeration has no elements");
    }
}
