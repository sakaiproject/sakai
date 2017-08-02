package org.sakaiproject.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Created by enietzel on 7/21/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteHostFilterTest {

    @Mock
    private ServerConfigurationService serverConfigurationService;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    private MockFilterConfig config;
    private RemoteHostFilter filter;

    @Before
    public void setup() {
        chain = new MockFilterChain();
        config = new MockFilterConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        Mockito.when(serverConfigurationService.getBoolean("webservices.log-allowed", true)).thenReturn(true);
        Mockito.when(serverConfigurationService.getBoolean("webservices.log-denied", true)).thenReturn(true);

        filter = new RemoteHostFilter();
        filter.setServerConfigurationService(serverConfigurationService);
    }

    private void configureFilter(Map<String, List<String>> params) {
        if (params.containsKey("webservices.allow-request")) {
            filter.setAllowRequests(params.get("webservices.allow-request"));
            Mockito.when(serverConfigurationService.getString("webservices.allow-request", null)).thenReturn(null);
        } else {
            List<String> allowed = new ArrayList<String>() {{
                add("/sakai-ws/rest/i18n/getI18nProperties");
                add("/sakai-ws/soap/i18n/getI18nProperties");
            }};
            Mockito.when(serverConfigurationService.getStringList("webservices.allow-request", null)).thenReturn(allowed);
        }

        if (params.containsKey("webservices.allow")) {
            filter.setAllow(params.get("webservices.allow"));
        } else {
            List<Pattern> allowed = new ArrayList<Pattern>() {{
                add(Pattern.compile("localhost"));
                add(Pattern.compile("127\\.0\\.0\\.1"));
                add(Pattern.compile("192\\.168\\.[0-9.]+"));
                add(Pattern.compile("10\\.[0-9.]+"));
                add(Pattern.compile("172\\.1[6-9]\\.[0-9.]+"));
                add(Pattern.compile("172\\.2[0-9]\\.[0-9.]+"));
                add(Pattern.compile("172\\.3[0-1]\\.[0-9.]+"));
            }};
            Mockito.when(serverConfigurationService.getPatternList("webservices.allow", null)).thenReturn(allowed);
        }

        if (params.containsKey("webservices.deny")) {
            filter.setDeny(params.get("webservices.deny"));
            // Remove the allow stubbing, otherwise it'll override our deny rule.
            Mockito.when(serverConfigurationService.getPatternList("webservices.allow", null)).thenReturn(new ArrayList<Pattern>());
        }

        try {
            filter.init(config);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    private void configureRequest(String uri, Map<String, String> requestParams) {
        requestParams.forEach((k, v) -> request.setParameter(k, v));
        request.setRequestURI(uri);
    }

    private void fire(String uri, Map<String, List<String>> params, Map<String, String> requestParams) {
        configureFilter(params != null ? params : new HashMap<>());
        configureRequest(uri, requestParams != null ? requestParams : new HashMap<>());

        try {
            filter.doFilter(request, response, chain);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testAllowRequestURI() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("webservices.allow", Collections.EMPTY_LIST);
        fire("/sakai-ws/rest/i18n/getI18nProperties", params, null);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testImplicitDeniedRequest() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("webservices.allow", Collections.EMPTY_LIST);
        fire("/sakai-ws/rest/login/login", params, null);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    public void testExplicitDeniedRequest() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("webservices.deny", Arrays.asList("localhost"));
        fire("/sakai-ws/rest/login/login", params, null);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    public void testExplicitAllowRequest() {
        fire("/sakai-ws/rest/login/login", null, null);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
}
