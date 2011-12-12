/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Add the ability to look in sakai.properties for properties
import org.sakaiproject.component.cover.ServerConfigurationService;

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
public class RemoteHostFilter implements Filter {

    // Our logger
	private static Log M_log = LogFactory.getLog(RemoteHostFilter.class);
	
    /**
     * Define an empty pattern to save re-constructing a null array multiple
     * times
     */
    private static final Pattern[] EMPTY_PATTERN = new Pattern[0];

    /**
     * The comma-delimited set of allowed hosts (hostnames/addresses).
     */
    protected String allowList = null;

    /**
     * The comma-delimited set of denied hosts (hostnames/addresses)
     */
    protected String denyList = null;

    /**
     * The set of allowed hosts/addresses expressed as regular expressions
     */

    protected Pattern[] allow = EMPTY_PATTERN;

    /**
     * The set of denied hosts/addresses expressed as regular expressions
     */
    protected Pattern[] deny = EMPTY_PATTERN;

    /**
     * Should allowed requests be logged
     */
    protected boolean logAllowed = false;

    /**
     * Should denied requests be logged
     */
    protected boolean logDenied = true;

    /**
     * Read the allow/deny parameters and initialize patterns
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        allowList = getParameter(config,"allow");
        if ( ":empty:".equals(allowList) ) allowList = null;
        allow = getRegExPatterns(allowList);
        logAllowed = Boolean.valueOf(getParameter(config,"log-allowed"))
                .booleanValue();

        denyList = getParameter(config,"deny");
        if ( ":empty:".equals(denyList) ) denyList = null;
        deny = getRegExPatterns(denyList);
        logDenied = Boolean.valueOf(getParameter(config,"log-denied"))
                .booleanValue();
    }

    private String getParameter(FilterConfig config, String parmName)
    {
	String retval = ServerConfigurationService.getString("webservices."+parmName, null);
	if ( retval != null ) return retval;
        return config.getInitParameter(parmName);
    }

    /*
     * See class description above.
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest sreq, ServletResponse sres,
            FilterChain chain) throws IOException, ServletException {

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

        // Check if explicit denied ...
        for (int i = 0; i < deny.length; i++) {
            if (deny[i].matcher(host).matches()
                || deny[i].matcher(addr).matches()) {
                if (logDenied && M_log.isInfoEnabled())
                	M_log.info("Access denied (" + deny[i].pattern() + "): " + host + "/" + addr);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // Check if explicitly allowed ...
        for (int i = 0; i < allow.length; i++) {
            if (allow[i].matcher(host).matches()
                    || allow[i].matcher(addr).matches()) {
                if (logAllowed && M_log.isInfoEnabled())
                    M_log.info("Access granted (" + allow[i].pattern() + "): " + host + "/" + addr);
                chain.doFilter(sreq, sres);
                return;
            }
        }

        // Allow if allows is null, but denied is not
        if ((deny.length > 0) && (allow.length == 0)) {
            if (logAllowed && M_log.isInfoEnabled())
            	M_log.info("Access granted (implicit): " + host + "/" + addr);
            chain.doFilter(sreq, sres);
            return;
        }

        // Deny this request
        if (logDenied && M_log.isInfoEnabled())
        	M_log.info("Access denied (implicit): " + host + "/" + addr);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // Do nothing
    }

    /**
     * Converts the given list of comma-delimited regex patterns to an array of
     * Pattern objects
     * 
     * @param list
     *            The comma-separated list of patterns
     * 
     * @exception IllegalArgumentException
     *                if one of the patterns has invalid regular expression
     *                syntax
     */
    protected Pattern[] getRegExPatterns(String list) {

        if (list == null)
            return EMPTY_PATTERN;

        list = list.trim();
        if (list.length() < 1)
            return EMPTY_PATTERN;

        StringTokenizer st = new StringTokenizer(list, ",");

        ArrayList<Pattern> patterns = new ArrayList<Pattern>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            try {
                // Host names are case insensitive
                patterns.add(Pattern.compile(token, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(
                        "Illegal Regular Expression Syntax: [" + token + "] - "
                                + e.getMessage());
            }
        }
        return ((Pattern[]) patterns.toArray(EMPTY_PATTERN));
    }
}
