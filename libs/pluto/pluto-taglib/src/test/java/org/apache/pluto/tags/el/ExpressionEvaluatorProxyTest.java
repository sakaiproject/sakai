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

import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspFactory;
import jakarta.servlet.jsp.PageContext;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Exercise the public Pluto evaluator with Tomcat 10's real JSP and EL implementations. */
public class ExpressionEvaluatorProxyTest {
    private static JspFactory previousFactory;
    private PageContext pageContext;

    @BeforeClass
    public static void installTomcatJspFactory() {
        previousFactory = JspFactory.getDefaultFactory();
        JspFactory.setDefaultFactory(new JspFactoryImpl());
    }

    @AfterClass
    public static void restoreJspFactory() {
        JspFactory.setDefaultFactory(previousFactory);
    }

    @Before
    public void setupPageContext() {
        ServletContext servletContext = mock(ServletContext.class);
        ExpressionFactory factory = JspFactory.getDefaultFactory()
                .getJspApplicationContext(servletContext).getExpressionFactory();
        StandardELContext context = new StandardELContext(factory);
        context.getVariableMapper().setVariable("who", factory.createValueExpression("Sakai", String.class));
        pageContext = mock(PageContext.class);
        when(pageContext.getServletContext()).thenReturn(servletContext);
        when(pageContext.getELContext()).thenReturn(context);
    }

    @Test
    public void evaluatesJakartaElVariables() throws Exception {
        assertEquals("Hello Sakai", ExpressionEvaluatorProxy.getProxy().evaluate("Hello ${who}", pageContext));
    }

    @Test
    public void preservesLiteralAttributeValues() throws Exception {
        assertEquals("Literal value", ExpressionEvaluatorProxy.getProxy().evaluate("Literal value", pageContext));
    }
}
