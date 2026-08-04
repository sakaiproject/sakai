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
import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class JSP21ExpressionEvaluatorProxy extends ExpressionEvaluatorProxy {

    public static Method jspApplicationContextGetter;

    public static Method expressionFactoryGetter;

    public static Method elContextGetter;

    public static Method valueExpressionGetter;

    public static Method evalMethod;

    private static boolean initialized;

    private static Object jspFactory;

    static {
        try {
            jspFactory = Class.forName("javax.servlet.jsp.JspFactory")
                .getMethod("getDefaultFactory", new Class[0]).invoke(null, null);
            jspApplicationContextGetter = 
                jspFactory.getClass().getMethod("getJspApplicationContext",
                    new Class[] { ServletContext.class });
            expressionFactoryGetter = 
                Class.forName("javax.servlet.jsp.JspApplicationContext")
                    .getMethod("getExpressionFactory", new Class[0]);
            elContextGetter = 
                PageContext.class.getMethod("getELContext", new Class[0]);
            valueExpressionGetter = 
                Class.forName("javax.el.ExpressionFactory").getMethod(
                    "createValueExpression", new Class[] 
                    { Class.forName("javax.el.ELContext"), String.class, Class.class });
            evalMethod = Class.forName("javax.el.ValueExpression").getMethod(
                    "getValue", new Class[] { Class.forName("javax.el.ELContext") });
        } catch (Exception e) {
            throw new RuntimeException("Unable to find JSP2.1 methods.", e);
        }
    }

    public String evaluate(String value, PageContext pageContext)
            throws JspException {
        try {
            Object jspApplicationContext = jspApplicationContextGetter.invoke(
                    jspFactory,
                    new Object[] { pageContext.getServletContext() });

            Object expressionFactory = expressionFactoryGetter.invoke(
                    jspApplicationContext, null);

            Object elContext = elContextGetter.invoke(pageContext, null);

            Object valueExpression = valueExpressionGetter.invoke(
                    expressionFactory, new Object[] { elContext, value,
                            Object.class });

            Object evaluated = evalMethod.invoke(valueExpression,
                    new Object[] { elContext });

            if (evaluated != null) {
                value = evaluated.toString();
            }
        } catch (IllegalAccessException e) {
            throw new JspException(e);
        } catch (InvocationTargetException e) {
            throw new JspException(e);
        }
        return value;
    }

}

