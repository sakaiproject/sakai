package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SiteStatsCsrfInterceptorTest {

    private static final String TOKEN = "csrf-token";

    private SakaiCsrfTokens csrfTokens;
    private SiteStatsCsrfInterceptor interceptor;

    @Before
    public void setUp() {
        csrfTokens = mock(SakaiCsrfTokens.class);
        interceptor = new SiteStatsCsrfInterceptor(csrfTokens);
    }

    @Test
    public void allowsSafeRequestsWithoutAToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reports");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void allowsUnsafeRequestsWithTheSessionToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/save");
        request.addParameter(SakaiCsrfTokens.REQUEST_PARAMETER, TOKEN);
        when(csrfTokens.matches(TOKEN)).thenReturn(true);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void rejectsUnsafeRequestsWithoutAToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/save");
        assertThrows(InvalidSakaiCsrfTokenException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void rejectsUnsafeRequestsWithTheWrongToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/reports/1");
        request.addParameter(SakaiCsrfTokens.REQUEST_PARAMETER, "wrong-token");

        assertThrows(InvalidSakaiCsrfTokenException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
