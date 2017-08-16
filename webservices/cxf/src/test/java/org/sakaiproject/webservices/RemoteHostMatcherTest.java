package org.sakaiproject.webservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.webservices.interceptor.RemoteHostMatcher;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Created by enietzel on 7/21/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteHostMatcherTest {

    @Mock
    private ServerConfigurationService serverConfigurationService;
    private MockHttpServletRequest request;
    private RemoteHostMatcher matcher;

    @Before
    public void setup() {
        request = new MockHttpServletRequest();

        matcher = new RemoteHostMatcher();
        matcher.setServerConfigurationService(serverConfigurationService);
    }

    private void configureFilter(Map<String, List<String>> params) {
        if (params.containsKey("webservices.allow-request")) {
            matcher.setAllowRequests(params.get("webservices.allow-request"));
            Mockito.when(serverConfigurationService.getString("webservices.allow-request", null)).thenReturn(null);
        } else {
            List<String> allowed = new ArrayList<String>() {{
                add("/sakai-ws/rest/i18n/getI18nProperties");
                add("/sakai-ws/soap/i18n/getI18nProperties");
            }};
            Mockito.when(serverConfigurationService.getStringList("webservices.allow-request", null)).thenReturn(allowed);
        }

        if (params.containsKey("webservices.allow")) {
            matcher.setAllow(params.get("webservices.allow"));
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
            matcher.setDeny(params.get("webservices.deny"));
            // Remove the allow stubbing, otherwise it'll override our deny rule.
            Mockito.when(serverConfigurationService.getPatternList("webservices.allow", null)).thenReturn(new ArrayList<Pattern>());
        }
        matcher.init();
    }

    private void configureRequest(String uri, Map<String, String> requestParams) {
        requestParams.forEach((k, v) -> request.setParameter(k, v));
        request.setRequestURI(uri);
    }

    private boolean fire(String uri, Map<String, List<String>> params, Map<String, String> requestParams) {
        configureFilter(params != null ? params : new HashMap<>());
        configureRequest(uri, requestParams != null ? requestParams : new HashMap<>());
        return matcher.isAllowed(request);
    }

    @Test
    public void testAllowRequestURI() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("webservices.allow", Collections.EMPTY_LIST);
        Assert.assertTrue(fire("/sakai-ws/rest/i18n/getI18nProperties", params, null));
    }

    @Test
    public void testImplicitDeniedRequest() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("webservices.allow", Collections.EMPTY_LIST);
        Assert.assertFalse(fire("/sakai-ws/rest/login/login", params, null));
    }

    @Test
    public void testExplicitDeniedRequest() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("webservices.deny", Arrays.asList("localhost"));
        Assert.assertFalse(fire("/sakai-ws/rest/login/login", params, null));
    }

    @Test
    public void testExplicitAllowRequest() {
        Assert.assertTrue(fire("/sakai-ws/rest/login/login", null, null));
    }
}
