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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.jmock.MockObjectTestCase;

/**
 * Test Class
 *
 * @version 1.0
 * @since June 1, 2005
 */
public abstract class PlutoTestCase extends MockObjectTestCase {

    public void setUp() throws Exception {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "ERROR");
    }

    protected void assertException(Object target, String methodName,
                                 Object[] parameters, Class exceptionType) {
            Class[] parameterClasses = new Class[parameters.length];
            for(int i=0;i<parameters.length;i++) {
                parameterClasses[i] = parameters[i]==null?Object.class:parameters[i].getClass();
            }
        assertException(target, methodName, parameterClasses, parameters, exceptionType);
    }

    protected void assertException(Object target, String methodName,
                                 Class[] parameterClasses,
                                 Object[] parameters, Class exceptionType) {
        try {
            Class targetClass = target.getClass();
            Method method = targetClass.getMethod(methodName, parameterClasses);
            method.invoke(target, parameters);
        }
        catch(InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if(!t.getClass().equals(exceptionType)) {
                fail("Incorrect Exception thrown.  Expected: "+exceptionType.getName()+", recieved "+t.getClass().getName());
            }
        }
        catch(Throwable t) {
            fail("Invalid Test.  Reflection invocation and setup failed.");
        }
    }

    protected void assertContains(String message, String expectedSubstring,
                                  String testString) {
        if (testString.indexOf(expectedSubstring) < 0) {
            fail(message);
        }
    }
}
