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
package org.apache.pluto.tags.el;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


class ApacheTaglibStandardExpressionEvaluatorProxy extends ExpressionEvaluatorProxy {

    private static Method evaluateMethod;

    static {
        try {
            Class expressionEvaluatorManagerClass = Class.forName("org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager");
            evaluateMethod = expressionEvaluatorManagerClass.getMethod("evaluate", new Class[] { String.class, String.class,  Class.class, PageContext.class});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find ExpressionEvaluatorManager.  Make sure standard.jar is in your classpath");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to fine method 'evaluate' on ExpressionEvaluatorManager");
        }
    }

    public String evaluate(String value, PageContext pageContext) throws JspException {
        try {
            return (String)evaluateMethod.invoke(null, new Object[] { "attributeName", value, Object.class,  pageContext});
        } catch (IllegalAccessException e) {
            throw new JspException(e);
        } catch (InvocationTargetException e) {
            throw new JspException(e);
        }
    }
}
