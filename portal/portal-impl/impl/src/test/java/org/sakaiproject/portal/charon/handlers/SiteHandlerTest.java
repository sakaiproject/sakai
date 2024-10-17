/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.charon.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.portal.charon.PortalTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.sakaiproject.portal.charon.handlers.SiteHandler.PageParts;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PortalTestConfiguration.class})
public class SiteHandlerTest {

    public static boolean setupOnceCompleted = false;

    @Autowired private ApplicationContext applicationContext;

    private SiteHandler siteHandler;

    @Before
    public void setUp() {

        if (!setupOnceCompleted) {
            // Setup a fake webapp so spring injection will happen
            ServletContext servletContext = mock(ServletContext.class);
            Mockito.when(servletContext.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
            Mockito.when(servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());

            WebApplicationContext webApplicationContext = new GenericWebApplicationContext(servletContext);
            ((AbstractApplicationContext) webApplicationContext).setParent(applicationContext);
            ContextLoader contextLoader = new ContextLoader(webApplicationContext);
            contextLoader.initWebApplicationContext(servletContext);

            setupOnceCompleted = true;
        }

        this.siteHandler = new SiteHandler();
    }

    @Test
    public void testParseHtmlSimple() {
        PageParts pp = siteHandler.parseHtmlParts("<html><head><title>Hello</title></head><body><h1>Hello World</h1></body></html>");
        Assert.assertNotNull(pp);
        Assert.assertEquals("", pp.head);
        Assert.assertEquals("<h1>Hello World</h1>", pp.body);
    }

    @Test
    public void testParseHtmlStyle() {
        PageParts pp = siteHandler.parseHtmlParts("<html><head><style>h1 { font: larger; }</style><title>Hello</title></head><body><h1>Hello World</h1></body></html>");
        Assert.assertNotNull(pp);
        Assert.assertEquals("<style>h1 { font: larger; }</style>", pp.head);
        Assert.assertEquals("<h1>Hello World</h1>", pp.body);
    }

    @Test
    public void testParseHtmlNoHead() {
        PageParts pp = siteHandler.parseHtmlParts("<html><body><h1>Hello World</h1></body></html>");
        Assert.assertNull(pp);
    }

    @Test
    public void testParseHtmlNoBody() {
        PageParts pp = siteHandler.parseHtmlParts("<html><head><title>Hello</title></head></html>");
        Assert.assertNull(pp);
    }

    @Test
    public void testParseHtmlLowercaseUnicode() {
        // When this HTML fragment is lowercased it becomes longer (more bytes in the string) so the offsets can
        // become incorrect causing a stack trace (stack overflow). The test has enough extra bytes to overflow
        // past the end of the string.
        PageParts pp = siteHandler.parseHtmlParts("<html><head></head><body>İİİİİİİİİİİİİİİİ</body></html>");
        Assert.assertNotNull(pp);
        Assert.assertEquals("", pp.head);
        Assert.assertEquals("İİİİİİİİİİİİİİİİ", pp.body);
    }
}

