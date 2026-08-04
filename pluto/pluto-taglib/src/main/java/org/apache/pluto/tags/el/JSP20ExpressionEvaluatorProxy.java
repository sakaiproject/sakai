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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class JSP20ExpressionEvaluatorProxy extends ExpressionEvaluatorProxy {

    public static Method expressionEvaluatorGetter;

    public static Method variableResolverGetter;

    public static Method evalMethod;

    static {
        try {
            expressionEvaluatorGetter = PageContext.class.getMethod("getExpressionEvaluator", new Class[0]);

            variableResolverGetter = PageContext.class.getMethod("getVariableResolver", new Class[0]);

            evalMethod = expressionEvaluatorGetter.getReturnType().getMethod("evaluate",
                new Class[]{String.class, Class.class, variableResolverGetter.getReturnType(), Class.forName("javax.servlet.jsp.el.FunctionMapper")});

        } catch (Exception e) {
            throw new RuntimeException("Unable to located JSP20 Methods.", e);
        }
    }

    public String evaluate(String value, PageContext pageContext) throws JspException {
        try {
            Object evaluator = expressionEvaluatorGetter.invoke(pageContext, new Object[0]);
            Object resolver = variableResolverGetter.invoke(pageContext, new Object[0]);

            Object evaluated = evalMethod.invoke(evaluator, new Object[]{value, Object.class, resolver, null});

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
    
    /*
        ExpressionEvaluator eval = pageContext.getExpressionEvaluator();

        try {
            Object evaluated = eval.evaluate(
                    value,
                    Object.class,
                    pageContext.getVariableResolver(),
                    null
            );

            if(evaluated != null) {
                value = evaluated.toString();
            }

        }
        catch(ELException el) {
            throw new JspException(el);
        }

        return value;
    }
    */
}
