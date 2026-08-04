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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class ExpressionEvaluatorProxy {

    private static final Log LOG = LogFactory.getLog(ExpressionEvaluatorProxy.class);

    private static final Map FACTORIES = new TreeMap();

    private static ExpressionEvaluatorProxy proxy;

    static {
        FACTORIES.put("getELContext", "JSP21ExpressionEvaluatorProxy");
        FACTORIES.put("getExpressionEvaluator", "JSP20ExpressionEvaluatorProxy");
        // And finally, a default that should always be provided, but for which an additional jar is needed.
        FACTORIES.put("getRequest", "ApacheTaglibStandardExpressionEvaluatorProxy");

        Iterator entrySetIterator = FACTORIES.entrySet().iterator();
        while(entrySetIterator.hasNext()) {
            Map.Entry entry = (Map.Entry)entrySetIterator.next();
            if(isPageContextMethodAvailable(entry.getKey().toString())) {
                try {
                    String className =
                        ExpressionEvaluatorProxy.class.getPackage().getName()+"."+entry.getValue();

                    if(LOG.isInfoEnabled()) {
                        LOG.info("Attempting to utilize expression evaluator proxy '"+className+"'");
                    }

                    Class proxyClass = Class.forName(className);
                    proxy = (ExpressionEvaluatorProxy)proxyClass.newInstance();

                    if(proxy != null) {
                        break;
                    }

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to find ExpressionEvaluatorProxy '"+entry.getValue()+"'");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to access ExpressionEvaluatorProxy '"+entry.getValue()+"'");
                } catch (InstantiationException e) {
                    throw new RuntimeException("Unable to instantiate ExpressionEvaluatorProxy '"+entry.getValue()+"'");
                }
            }
        }
        if(proxy == null) {
            throw new RuntimeException("Unable to find a supported proxy");
        } else {
            LOG.info("ExpressionEvaluator Proxy Found: "+proxy.getClass().getName());
        }
    }


    public static ExpressionEvaluatorProxy getProxy() {
        return proxy;
    }

    private static boolean isPageContextMethodAvailable(String signature) {
         try {
            Method method = PageContext.class.getMethod(signature, null);
            return method != null;
        } catch (NoSuchMethodException e) {
             return false;
        }
    }

    public abstract String evaluate(String value, PageContext pageContext)
        throws JspException;
}

