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

import java.util.Map;
import java.util.Iterator;

/**
 * Test Class
 *
 * @version 1.0
 * @since June 1, 2005
 */
public class StringUtilsTest extends PlutoTestCase {

    public void testReplaceAtBegin() {
        assertEquals("ReplacedValue", StringUtils.replace("___lacedValue", "___", "Rep"));
    }

    public void testReplaceAtEnd() {
        assertEquals("ReplacedValue", StringUtils.replace("ReplacedVa***", "***", "lue"));
    }

    public void testReplaceInMiddle() {
        assertEquals("ReplacedValue", StringUtils.replace("Rep(###)Value", "(###)", "laced"));
    }

    public void testReplaceMultiples() {
        assertEquals("ReplacedValueReplacedValue", StringUtils.replace("Rep(###)ValueRep(###)Value", "(###)", "laced"));
    }

    public void testCopy() {
        String[] original  = new String[] {"one", "two", "three", "four", "five"};
        String[] results = StringUtils.copy(original);
        for(int i=0;i<original.length;i++) {
            assertEquals(original[i], results[i]);
        }
    }

    public void testCopyMap() {
        Map original = new java.util.HashMap();
        original.put("one", new String[] { "two"});
        original.put("three", new String[] { "four"});
        original.put("five", new String[] { "six"});
        original.put("seven", new String[] { String.valueOf(8), String.valueOf(9) } );

        Map results = StringUtils.copyParameters(original);
        assertEquals("Map sizes are inconsistent", original.size(), results.size());
        Iterator it = original.keySet().iterator();
        while(it.hasNext()) {
            Object key = it.next();
            String[] originalValue = (String[])original.get(key);
            String[] newValue = (String[])results.get(key);

            assertNotNull(originalValue);
            assertNotNull(newValue);
            assertEquals(originalValue.length, newValue.length);
            for(int i=0;i<i++;i++) {
                assertEquals(originalValue[i], newValue[i]);
            }
        }
    }
}
