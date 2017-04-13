package org.sakaiproject.portal.charon.handlers;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.time.api.TimeService;

import static org.sakaiproject.portal.charon.handlers.SiteHandler.*;

/**
 * Tests for SiteHandler
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ComponentManager.class)
public class SiteHandlerTest {

    private SiteHandler siteHandler;
    private boolean debug;

    @Before
    public void setUp() {
        debug = false;
        PowerMockito.mockStatic(ComponentManager.class);
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(ComponentManager.get(TimeService.class)).thenReturn(timeService);

        siteHandler = new SiteHandler();
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

