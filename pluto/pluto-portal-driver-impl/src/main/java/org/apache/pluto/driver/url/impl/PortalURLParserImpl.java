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
package org.apache.pluto.driver.url.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.driver.url.PortalURLParameter;
import org.apache.pluto.driver.url.PortalURLParser;
import org.apache.pluto.util.StringUtils;

/**
 * @version 1.0
 * @since Sep 30, 2004
 */
public class PortalURLParserImpl implements PortalURLParser {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(PortalURLParserImpl.class);

    /** The singleton parser instance. */
    private static final PortalURLParser PARSER = new PortalURLParserImpl();


    // Constants used for Encoding/Decoding ------------------------------------

    private static final String PREFIX = "__";
    private static final String DELIM = "_";
    private static final String PORTLET_ID = "pd";
    private static final String ACTION = "ac";
    private static final String RENDER_PARAM = "rp";
    private static final String WINDOW_STATE = "ws";
    private static final String PORTLET_MODE = "pm";
    private static final String VALUE_DELIM = "0x0";

    //This is a list of characters that need to be encoded  to be protected
    //The ? is necessary to protect URI's with a query portion that is being passed as a parameter
    private static final String[][] ENCODINGS = new String[][] {
    		new String[] { "_",  "0x1" },
            new String[] { ".",  "0x2" },
            new String[] { "/",  "0x3" },
            new String[] { "\r", "0x4" },
            new String[] { "\n", "0x5" },
            new String[] { "<",  "0x6" },
            new String[] { ">",  "0x7" },
            new String[] { " ",  "0x8" },
            new String[] { "#",  "0x9" },
            new String[] { "?",  "0xa" },
            new String[] { "\\", "0xb" },
            new String[] { "%",  "0xc" },
    };

    // Constructor -------------------------------------------------------------

    /**
     * Private constructor that prevents external instantiation.
     */
    private PortalURLParserImpl() {
    	// Do nothing.
    }

    /**
     * Returns the singleton parser instance.
     * @return the singleton parser instance.
     */
    public static PortalURLParser getParser() {
    	return PARSER;
    }


    // Public Methods ----------------------------------------------------------

    /**
     * Parse a servlet request to a portal URL.
     * @param request  the servlet request to parse.
     * @return the portal URL.
     */
    public PortalURL parse(HttpServletRequest request) {

    	if (LOG.isDebugEnabled()) {
            LOG.debug("Parsing URL: " + request.getRequestURI());
        }

        String contextPath = request.getContextPath();
        String servletName = request.getServletPath();

        // Construct portal URL using info retrieved from servlet request.
        PortalURL portalURL =  new RelativePortalURLImpl(contextPath, servletName, this);

        // Support added for filter.  Should we seperate into a different impl?
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            if((servletName.indexOf(".jsp") != -1) && !servletName.endsWith(".jsp")) {
                int idx = servletName.indexOf(".jsp")+".jsp".length();
                pathInfo = servletName.substring(idx);
                servletName = servletName.substring(0, idx);
                portalURL = new RelativePortalURLImpl(contextPath, servletName, this);
            } else {
                return portalURL;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parsing request pathInfo: " + pathInfo);
        }
        StringBuffer renderPath = new StringBuffer();
        StringTokenizer st = new StringTokenizer(pathInfo, "/", false);
        while (st.hasMoreTokens()) {

        	String token = st.nextToken();

        	// Part of the render path: append to renderPath.
        	if (!token.startsWith(PREFIX)) {
//        		renderPath.append(token);
        		//Fix for PLUTO-243
        		renderPath.append('/').append(token);
        	}
        	// Action window definition: portalURL.setActionWindow().
        	else if (token.startsWith(PREFIX + ACTION)) {
        		portalURL.setActionWindow(decodeControlParameter(token)[0]);
        	}
        	// Window state definition: portalURL.setWindowState().
        	else if (token.startsWith(PREFIX + WINDOW_STATE)) {
        		String[] decoded = decodeControlParameter(token);
        		portalURL.setWindowState(decoded[0], new WindowState(decoded[1]));
        	}
        	// Portlet mode definition: portalURL.setPortletMode().
        	else if (token.startsWith(PREFIX + PORTLET_MODE)) {
        		String[] decoded = decodeControlParameter(token);
        		portalURL.setPortletMode(decoded[0], new PortletMode(decoded[1]));
        	}
        	// Portal URL parameter: portalURL.addParameter().
        	else {
        		String value = null;
        		if (st.hasMoreTokens()) {
        			value = st.nextToken();
        		}

        		// Defect PLUTO-361
        		// ADDED
        		PortalURLParameter param = decodeParameter( token, value );
        		if( param != null )
        		{
        			portalURL.addParameter( param );
        		}
        		// REMOVED
        		// portalURL.addParameter(decodeParameter(token, value));
        	}
        }
        if (renderPath.length() > 0) {
            portalURL.setRenderPath(renderPath.toString());
        }

        // Return the portal URL.
        return portalURL;
    }


    /**
     * Converts a portal URL to a URL string.
     * @param portalURL  the portal URL to convert.
     * @return a URL string representing the portal URL.
     */
    public String toString(PortalURL portalURL) {

    	StringBuffer buffer = new StringBuffer();

        // Append the server URI and the servlet path.
    	buffer.append(portalURL.getServletPath().startsWith("/")?"":"/")
            .append(portalURL.getServletPath());

        // Start the pathInfo with the path to the render URL (page).
        if (portalURL.getRenderPath() != null) {
        	buffer.append("/").append(portalURL.getRenderPath());
        }

        // Append the action window definition, if it exists.
        if (portalURL.getActionWindow() != null) {
        	buffer.append("/");
        	buffer.append(PREFIX).append(ACTION)
        			.append(encodeCharacters(portalURL.getActionWindow()));
        }

        // Append portlet mode definitions.
        for (Iterator it = portalURL.getPortletModes().entrySet().iterator();
        		it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            buffer.append("/").append(
            		encodeControlParameter(PORTLET_MODE, entry.getKey().toString(),
                       entry.getValue().toString()));
        }

        // Append window state definitions.
        for (Iterator it = portalURL.getWindowStates().entrySet().iterator();
        		it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            buffer.append("/").append(
            		encodeControlParameter(WINDOW_STATE, entry.getKey().toString(),
                       entry.getValue().toString()));
        }

        // Append action and render parameters.
        StringBuffer query = new StringBuffer("?");
        boolean firstParam = true;
        for (Iterator it = portalURL.getParameters().iterator();
        		it.hasNext(); ) {

        	PortalURLParameter param = (PortalURLParameter) it.next();

            // Encode action params in the query appended at the end of the URL.
            if (portalURL.getActionWindow() != null
            		&& portalURL.getActionWindow().equals(param.getWindowId())) {
                for (int i = 0; i < param.getValues().length; i++) {
                    // FIX for PLUTO-247
                    if ( firstParam ) {
                        firstParam = false;
                    } else {
                        query.append("&");
                    }
                    query.append(encodeQueryParam(param.getName())).append("=")
                    		.append(encodeQueryParam(param.getValues()[i]));
                }
            }

            // Encode render params as a part of the URL.
            else if (param.getValues() != null
            		&& param.getValues().length > 0) {
                String valueString = encodeMultiValues(param.getValues());
                if (valueString.length() > 0) {
                	buffer.append("/").append(
                			encodeControlParameter(RENDER_PARAM, param.getWindowId(),
                               param.getName()));
                	buffer.append("/").append(valueString);
                }
            }
        }

        // Construct the string representing the portal URL.
        // Fix for PLUTO-247 - check if query string contains parameters
        if ( query.length() > 1 ) {
            return buffer.append(query).toString();
        }

        return buffer.toString();
    }

    private String encodeQueryParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // If this happens, we've got bigger problems.
            throw new RuntimeException(e);
        }
    }

    // Private Encoding/Decoding Methods ---------------------------------------

    /**
     * Encode a control parameter.
     * @param type  the type of the control parameter, which may be:
     *              portlet mode, window state, or render parameter.
     * @param windowId  the portlet window ID.
     * @param name  the name to encode.
     */
    private String encodeControlParameter(String type,
                                          String windowId,
                                          String name) {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(PREFIX).append(type)
    			.append(encodeCharacters(windowId))
    			.append(DELIM).append(name);
    	return buffer.toString();
    }

    /**
     * Encode a string array containing multiple values into a single string.
     * This method is used to encode multiple render parameter values.
     * @param values  the string array to encode.
     * @return a single string containing all the values.
     */
    private String encodeMultiValues(String[] values) {
    	StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
        	buffer.append(values[i] != null ? values[i] : "");
            if (i + 1 < values.length) {
            	buffer.append(VALUE_DELIM);
            }
        }
        return encodeCharacters(buffer.toString());
    }

    /**
     * Encode special characters contained in the string value.
     * @param string  the string value to encode.
     * @return the encoded string.
     */
    private String encodeCharacters(String string) {
        for (int i = 0; i < ENCODINGS.length; i++) {
            string = StringUtils.replace(string,
                                         ENCODINGS[i][0],
                                         ENCODINGS[i][1]);
        }
        return string;
    }


    /**
     * Decode a control parameter.
     * @param control  the control parameter to decode.
     * @return values  a pair of decoded values.
     */
    private String[] decodeControlParameter(String control) {
        String[] valuePair = new String[2];
        control = control.substring((PREFIX + PORTLET_ID).length());
        int index = control.indexOf(DELIM);
        if (index >= 0) {
        	valuePair[0] = control.substring(0, index);
        	valuePair[0] = decodeCharacters(valuePair[0]);
        	if (index + 1 <= control.length()) {
        		valuePair[1] = control.substring(index + 1);
        		valuePair[1] = decodeCharacters(valuePair[1]);
        	} else {
        		valuePair[1] = "";
        	}
        } else {
        	valuePair[0] = decodeCharacters(control);
        }
        return valuePair;
    }

    /**
     * Decode a name-value pair into a portal URL parameter.
     * @param name  the parameter name.
     * @param value  the parameter value.
     * @return the decoded portal URL parameter.
     */
    private PortalURLParameter decodeParameter(String name, String value) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Decoding parameter: name=" + name
            		+ ", value=" + value);
        }

        // Defect PLUTO-361
        // ADDED: if the length is less than this, there is no parameter...
        if( name.length() < (PREFIX + PORTLET_ID).length() )
        {
        	return null;
        }

    	// Decode the name into window ID and parameter name.
        String noPrefix = name.substring((PREFIX + PORTLET_ID).length());
        String windowId = noPrefix.substring(0, noPrefix.indexOf(DELIM));
        String paramName = noPrefix.substring(noPrefix.indexOf(DELIM) + 1);

        // Decode special characters in window ID and parameter value.
        windowId = decodeCharacters(windowId);
        if (value != null) {
        	value = decodeCharacters(value);
        }

        // Split multiple values into a value array.
        String[] paramValues = value.split(VALUE_DELIM);

        // Construct portal URL parameter and return.
        return new PortalURLParameter(windowId, paramName, paramValues);
    }

    /**
     * Decode special characters contained in the string value.
     * @param string  the string value to decode.
     * @return the decoded string.
     */
    private String decodeCharacters(String string) {
        for (int i = 0; i < ENCODINGS.length; i++) {
        	string = StringUtils.replace(string,
        	                             ENCODINGS[i][1],
        	                             ENCODINGS[i][0]);
        }
        return string;
    }

}

