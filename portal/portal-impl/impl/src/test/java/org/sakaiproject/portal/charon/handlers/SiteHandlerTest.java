/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.charon.handlers;

import static org.mockito.Mockito.mockStatic;
import static org.sakaiproject.portal.charon.handlers.SiteHandler.PageParts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.time.api.TimeService;

import junit.framework.Assert;

/**
 * Tests for SiteHandler
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteHandlerTest {

    private boolean debug;
    private SiteHandler siteHandler;

    @Before
    public void setup() {
        debug = false;
        try (MockedStatic<ComponentManager> cm = mockStatic(ComponentManager.class)) {
            cm.when(() -> ComponentManager.get(TimeService.class)).thenReturn(Mockito.mock(TimeService.class));
            cm.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(Mockito.mock(ServerConfigurationService.class));
            siteHandler = new SiteHandler();
        }
    }

    @Test
    public void testParseHtmlSimple() {
        PageParts pp = siteHandler.parseHtmlParts("<html><head><title>Hello</title></head><body><h1>Hello World</h1></body></html>", debug);
        Assert.assertNotNull(pp);
        Assert.assertEquals("", pp.head);
        Assert.assertEquals("<h1>Hello World</h1>", pp.body);
    }

    @Test
    public void testParseHtmlStyle() {
        PageParts pp = siteHandler.parseHtmlParts("<html><head><style>h1 { font: larger; }</style><title>Hello</title></head><body><h1>Hello World</h1></body></html>", debug);
        Assert.assertNotNull(pp);
        Assert.assertEquals("<style>h1 { font: larger; }</style>", pp.head);
        Assert.assertEquals("<h1>Hello World</h1>", pp.body);
    }

    @Test
    public void testParseHtmlNoHead() {
        PageParts pp = siteHandler.parseHtmlParts("<html><body><h1>Hello World</h1></body></html>", debug);
        Assert.assertNull(pp);
    }

    @Test
    public void testParseHtmlNoBody() {
        PageParts pp = siteHandler.parseHtmlParts("<html><head><title>Hello</title></head></html>", debug);
        Assert.assertNull(pp);
    }

    @Test
    public void testParseHtmlLowercaseUnicode() {
        // When this HTML fragment is lowercased it becomes longer (more bytes in the string) so the offsets can
        // become incorrect causing a stack trace (stack overflow). The test has enough extra bytes to overflow
        // past the end of the string.
        PageParts pp = siteHandler.parseHtmlParts("<html><head></head><body>İİİİİİİİİİİİİİİİ</body></html>", debug);
        Assert.assertNotNull(pp);
        Assert.assertEquals("", pp.head);
        Assert.assertEquals("İİİİİİİİİİİİİİİİ", pp.body);
    }
}

