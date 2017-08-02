/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This Servlet Filter allows/denies requests based on comparing the remote
 * hostname and/or ip-address against a set of regular expressions configured in
 * the init parameters.
 * <p>
 * The <code>allow</code> and/or <code>deny</code> properties are expected
 * to be comma-delimited list of regular expressions indicating hostnames and/or
 * ip addresses of allowed/denied hosts. Here is the evaluation logic:
 * <ul>
 * <li>The hostname and address are first compared to the deny expressions
 * configured. If a match is found, the request is rejected with a "Forbidden"
 * HTTP response.</li>
 * 
 * <li>Next the hostname and address are compared to allow expressions. If a
 * match is found, this request will be allowed.</li>
 * 
 * <li>If one or more deny expressions were specified but no allow expressions
 * were, allow this request to pass through (because none of the deny
 * expressions matched it).
 * <li>The request will be rejected with a "Forbidden" HTTP response.</li>
 * </ul>
 * 
 * To summarize, the pseudo-code looks like: <pre>
 *      if (explicitly denied) "Forbidden";
 *      else if (explicitly allowed) "Pass";
 *      else if (allow set is null, but deny is not) "Pass";
 *      else "Forbidden";
 * </pre>
 * 
 * <code>log-allowed</code> and <code>log-denied</code> may be specified to
 * true/false to log allowed/denied requests. <code>log-allowed</code>
 * defaults to false, and <code>log-denied</code> defaults to true;
 * 
 * @author <a href="vgoenka@sungardsct.com">Vishal Goenka</a>
 * 
 */
@Slf4j
public class RemoteHostFilter implements Filter {

    // The list of explicitly allowed request URI, these are not pattern matched
    @Setter
    private List<String> allowRequests;

    // The list of allowed hosts/addresses expressed as regular expressions
    @Setter
    private List<String> allow;
    private List<Pattern> allowPattern;

    // The list of denied hosts/addresses expressed as regular expressions
    @Setter
    private List<String> deny;
    private List<Pattern> denyPattern;

    // Should allowed requests be logged
    @Setter
    private boolean logAllowed = true;

    // Should denied requests be logged
    @Setter
    private boolean logDenied = true;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    public void init() {
        allowRequests = serverConfigurationService.getStringList("webservices.allow-request", allowRequests);
        allowPattern = serverConfigurationService.getPatternList("webservices.allow", allow);
        denyPattern = serverConfigurationService.getPatternList("webservices.deny", deny);

        logAllowed = serverConfigurationService.getBoolean("webservices.log-allowed", logAllowed);
        logDenied = serverConfigurationService.getBoolean("webservices.log-denied", logDenied);
    }

    /**
     * Note in a org.springframework.web.filter.DelegatingFilterProxy Filter.init(FilterConfig config) is never
     * called.
     *
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        init();
    }

    /*
     * See class description above.
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {

        // we are expecting HTTP
        if (!((sreq instanceof HttpServletRequest) && (sres instanceof HttpServletResponse))) {
            // if not, just pass it through
            chain.doFilter(sreq, sres);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) sreq;
        HttpServletResponse response = (HttpServletResponse) sres;

        String host = request.getRemoteHost();
        String addr = request.getRemoteAddr();

        // Check requests that are always allowed ...
        String uri = request.getRequestURI();
        for (String allowedUri : allowRequests) {
            if (StringUtils.startsWith(uri, allowedUri)) {
                if (logAllowed) log.info("Access granted for request ({}): {}/{}", uri, host, addr);
                chain.doFilter(sreq, sres);
                return;
            }
        }

        // Check if explicit denied ...
        for (Pattern pattern : denyPattern) {
            if (pattern.matcher(host).matches() || pattern.matcher(addr).matches()) {
                if (logDenied) log.info("Access denied ({}): {}/{}", pattern.pattern(), host, addr);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // Check if explicitly allowed ...
        for (Pattern pattern : allowPattern) {
            if (pattern.matcher(host).matches() || pattern.matcher(addr).matches()) {
                if (logAllowed) log.info("Access granted ({}): {}/{}", pattern.pattern(), host, addr);
                chain.doFilter(sreq, sres);
                return;
            }
        }

        // Allow if allows is null, but denied is not
        if ((!denyPattern.isEmpty()) && (allowPattern.isEmpty())) {
            if (logAllowed) log.info("Access granted (implicit): {}/{}", host, addr);
            chain.doFilter(sreq, sres);
            return;
        }

        // Deny this request
        if (logDenied) log.info("Access denied (implicit): {}/{}", host, addr);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
}
