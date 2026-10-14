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



/**
 * Test Class
 *
 * @version 1.0
 * @since June 1, 2005
 */
public class ArgumentUtilityTest extends PlutoTestCase {

    public void testValidateNotNullWhenNull()
    throws Exception {
        Object[] parameters = new Object[] {"arg", null};
        assertException(new ArgumentUtility(), "validateNotNull",
                        parameters, IllegalArgumentException.class);
    }

    public void testValidateNotNullWhenNotNull() {
        ArgumentUtility.validateNotNull("arg", "notnull");
    }

    public void testValidateNotNullOrEmptyWhenNull() {

        Object[] parameters = new Object[] {"arg", null};
        Class[] parameterTypes = new Class[] { String.class, String.class };
        assertException(new ArgumentUtility(), "validateNotEmpty",
                        parameterTypes,
                        parameters, IllegalArgumentException.class);
    }

    public void testValidateNotNullOrEmptyWhenEmpty() {

        Object[] parameters = new Object[] {"arg", ""};
        assertException(new ArgumentUtility(), "validateNotEmpty",
                        parameters, IllegalArgumentException.class);
    }

    public void testValidateNotNullOrEmptyWhenValid() {
        ArgumentUtility.validateNotEmpty("arg", "notempty");
    }

}
