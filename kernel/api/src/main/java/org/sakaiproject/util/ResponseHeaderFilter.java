/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/util/ResponseHeaderFilter.java $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * ResponseHeaderFilter is responsible for adding response headers to the HttpServletResponses.
 * e.g. It can be used to cache resources like *.css and *.js
 * 
 * To use the filter: 
 * Add the filter definition to the web.xml and specify the response header information 
 * in the init parameters and add the appropriate filter mapping.
 * <code>
 * 	<filter>
 *		<description>Response Header Filter to enable JS caching</description>
 *		<display-name>Cache Filter For One Week</display-name>
 *		<filter-name>CacheFilterForWeek</filter-name>
 *		<filter-class>org.sakaiproject.util.ResponseHeaderFilter</filter-class>
 *		<init-param>
 *			<param-name>Cache-Control</param-name>
 *			<param-value>max-age=604800, public</param-value>
 *		</init-param>
 *	</filter>
 *	<filter-mapping>
 *		<filter-name>CacheFilterForWeek</filter-name>
 *		<url-pattern>/js/*</url-pattern>
 *	</filter-mapping>	
 * </code> 
 */
@Slf4j
public class ResponseHeaderFilter implements Filter {
    private Map<String,String> headerMap = new ConcurrentHashMap<String, String>();

    public Map<String, String> getHeaderMap() {
        return new HashMap<String, String>(this.headerMap);
    }

    /**
     * Adds a header or replaces an existing one or clears a header if the value is null
     * 
     * @param name the header name
     * @param value the header value
     * @return the previous value associated with the name OR null if none
     * @throws IllegalArgumentException if the name is null or empty
     */
    public String addHeader(String name, String value) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("header name cannot be null or blank");
        }
        if (this.headerMap != null) {
            if (value == null) {
                // remove the header value
                return this.headerMap.remove(name);
            } else {
                // add / update the header value
                return this.headerMap.put(name, value);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void init(FilterConfig filterConfig) throws ServletException {
        String webappName = filterConfig.getServletContext().getServletContextName();
        // storing the header information in a local map
        for (Enumeration<String> paramNames = filterConfig.getInitParameterNames(); paramNames.hasMoreElements(); ) {
            String paramName = paramNames.nextElement();
            String paramValue = filterConfig.getInitParameter(paramName);
            if (paramName != null && paramValue != null) {
                this.headerMap.put(paramName, paramValue);
            }
        }
        // adding the configured ones from sakai config
        ServerConfigurationService serverConfigurationService = org.sakaiproject.component.cover.ServerConfigurationService.getInstance();
        if (serverConfigurationService != null) {
            String[] headerStrings = serverConfigurationService.getStrings("response.headers");
            if (headerStrings != null) {
                for (String headerString : headerStrings) {
                    if (headerString != null && ! "".equals(headerString)) {
                        int loc = headerString.indexOf("::");
                        if (loc <= 0) {
                            log.warn("Invalid header string in sakai config (must contain '::', e.g. key::value): " + headerString);
                            continue;
                        }
                        String name = headerString.substring(0, loc);
                        if (name == null || "".equals(name)) {
                            log.warn("Invalid header string in sakai config (name must not be empty): " + headerString);
                            continue;
                        }
                        String value = null;
                        if (headerString.length() > loc+2) {
                            value = headerString.substring(loc+2);
                        }
                        addHeader(name, value);
                        if (value == null) {
                            log.info("Removing header ("+name+") from all responses for current webapp: " + webappName);
                        } else {
                            log.info("Adding header ("+name+" -> "+value+") to all responses for current webapp: " + webappName);
                        }
                    }
                }
            }
        }
        log.info("INIT: for webapp " + webappName);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // If the response is a http servlet response add the header information 
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            for (Entry<String, String> headerEntry: this.headerMap.entrySet()) {
                httpResponse.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        if (this.headerMap != null) {
            this.headerMap.clear();
        }
        log.info("DESTROY");
    }

}
