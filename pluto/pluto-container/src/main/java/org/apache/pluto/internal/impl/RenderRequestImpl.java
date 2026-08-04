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
package org.apache.pluto.internal.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.Constants;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalRenderRequest;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Implementation of the <code>javax.portlet.RenderRequest</code> interface.
 *
 */
public class RenderRequestImpl extends PortletRequestImpl
implements RenderRequest, InternalRenderRequest {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(RenderRequestImpl.class);


    // Private Member Variables ------------------------------------------------

    /** True if we are in an include call. */
    private boolean included = false;

    /** The parameters including parameters appended to the dispatching URI. */
    private Map parameters;

    /** The portlet preferences. */
    private PortletPreferences portletPreferences;


    // Constructors ------------------------------------------------------------

    public RenderRequestImpl(InternalPortletRequest request) {
        super(request);
    }

    public RenderRequestImpl(PortletContainer container,
                             InternalPortletWindow internalPortletWindow,
                             HttpServletRequest servletRequest) {
        super(container, internalPortletWindow, servletRequest);
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Created render request for: " + internalPortletWindow);
        }
    }


    // RenderRequest Impl ------------------------------------------------------

    public PortletPreferences getPreferences() {
        if (portletPreferences == null) {
            portletPreferences = new PortletPreferencesImpl(
            		getPortletContainer(),
            		getInternalPortletWindow(),
            		this,
            		Constants.METHOD_RENDER);
        }
        return portletPreferences;
    }


    /**
     * Checks the included flag and returns the content type. If the included
     * flag is set to true, this method returns null.
     */
    public String getContentType() {
        return included ? null : super.getContentType();
    }

    /**
     * Checks the included flag and returns the content length. If the included
     * flag is set to true, this method returns 0.
     */
    public int getContentLength() {
        return included ? 0 : super.getContentLength();
    }

    /**
     * Checks the included flag and returns the reader to this rende response.
     * If the included flag is set to true, this method returns null.
     */
    public BufferedReader getReader()
    throws UnsupportedEncodingException, IOException {
        return included ? null : super.getReader();
    }

    /**
     * Checks the included flag and returns the input stream to this render
     * response. If the included flag is set to true, this method returns null.
     */
    public ServletInputStream getInputStream() throws IOException {
        return included ? null : super.getInputStream();
    }


    // PortletRequestImpl Overwriting ------------------------------------------

    protected Map baseGetParameterMap() {
    	if (included && parameters != null) {
    		super.setBodyAccessed();
    		return parameters;
    	} else {
    		return super.baseGetParameterMap();
    	}
    }


    // InternalRenderRequest Impl ----------------------------------------------

    public void setIncluded(boolean included) {
    	this.included = included;
        if (!included) {
        	this.parameters = null;
        }
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Render request's included mode: " + included);
        }
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncludedQueryString(String queryString)
    throws IllegalStateException {
    	if (!included) {
    		throw new IllegalStateException("Parameters cannot be appended to "
    				+ "render request which is not included in a dispatch.");
    	}
    	if (queryString != null && queryString.trim().length() > 0) {
    		// Copy all the original render parameters.
    		parameters = new HashMap(super.getParameterMap());
    		// Merge the appended parameters to the render parameter map.
    		// The original render parameters should not be overwritten.
    		mergeQueryString(parameters, queryString);
    		// Log the new render parameter map.
    		if (LOG.isDebugEnabled()) {
    			LOG.debug("Merged parameters: " + parameters.toString());
    		}
    	} else {
    		if (LOG.isDebugEnabled()) {
    			LOG.debug("No query string appended to the included request.");
    		}
    	}
    }


    // Included HttpServletRequest (Limited) Impl ------------------------------

    /*
     * -------------------------------------------------------------------------
     * (non-javadoc)
     * Portlet Spec. PLT. 16.3.3.
     * The following methods of the HttpServletRequest must return the path and
     * query string information used to obtain the PortletRequestDispatcher
     * object:
     *   getPathInfo
     *   getPathTranslated
     *   getQueryString
     *   getRequestURI
     *   getServletPath
     * -------------------------------------------------------------------------
     */

    public String getPathInfo() {
    	String attr = (String) super.getAttribute(
    			"javax.servlet.include.path_info");
    	return (included && attr != null) ? attr : super.getPathInfo();
    }

    public String getQueryString() {
    	String attr = (String) super.getAttribute(
    			"javax.servlet.include.query_string");
    	return (included && attr != null) ? attr : super.getQueryString();
    }

    /**
     * TODO: check PLT.16.3.3. page 67, line 10.
     */
    public String getPathTranslated() {
    	// TODO:
        return null;
    }

    public String getRequestURI() {
    	String attr = (String) super.getAttribute(
    			"javax.servlet.include.request_uri");
        return (included && attr != null) ? attr : super.getRequestURI();
    }

    public String getServletPath() {
        String attr = (String) super.getAttribute(
                "javax.servlet.include.servlet_path");
        return (included && attr != null) ? attr : super.getServletPath();
    }

    /*
     * -------------------------------------------------------------------------
     * (non-Javadoc)
     * Portlet Spec. PLT. 16.3.3.
     * The following methods of the HttpServletRequest must return null:
     *   getProtocol
     *   getRemoteAddr
     *   getRemoteHost
     *   getRealPath
     *   getRequestURL
     * -------------------------------------------------------------------------
     */

    public String getProtocol() {
        return included ? null : super.getProtocol();
    }

    public String getRemoteAddr() {
        return included ? null : super.getRemoteAddr();
    }

    public String getRemoteHost() {
        return included ? null : super.getRemoteHost();
    }

    public String getRealPath(String path) {
        return included ? null : super.getRealPath(path);
    }

    public StringBuffer getRequestURL() {
        return included ? null : super.getRequestURL();
    }

    /*
     * -------------------------------------------------------------------------
     * (non-Javadoc)
     * Portlet Spec. PLT. 16.3.3.
     * The following methods of the HttpServletRequest must do no operations
     * and return null:
     *   getCharacterEncoding
     *   setCharacterEncoding
     *   getContentType
     *   getInputStream
     *   getReader
     * The getContentLength method of the HttpServletRequest must return 0.
     * -------------------------------------------------------------------------
     */

    public String getCharacterEncoding() {
        return included ? null : super.getCharacterEncoding();
    }

    public void setCharacterEncoding(String encoding)
    throws UnsupportedEncodingException {
        if (!included) {
        	super.setCharacterEncoding(encoding);
        }
    }

    /*
     * -------------------------------------------------------------------------
     * (non-javadoc)
     * Portlet Spec. PLT. 16.3.3.
     * The getMethod method of the HttpServletRequest must always return 'GET'.
     * -------------------------------------------------------------------------
     */

    public String getMethod() {
    	return "GET";
    }


    // Private Methods ---------------------------------------------------------

    /**
     * Parses the appended query string and merges the appended parameters to
     * the original parameters. Query parameters are name-value pairs separated
     * by the '<code>&amp;</code>' character.
     * @param parameters  the original parameters map.
     * @param queryString  the appended query string.
     */
    private void mergeQueryString(Map parameters, String queryString) {

    	// Create the appended parameters map:
    	//   key is the parameter name as a string,
    	//   value is a List of parameter values (List of String).
        Map appendedParameters = new HashMap();

        // Parse the appended query string.
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Parsing appended query string: " + queryString);
    	}
        StringTokenizer st = new StringTokenizer(queryString, "&", false);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int equalIndex = token.indexOf("=");
            if (equalIndex > 0) {
                String key = token.substring(0, equalIndex);
                String value = null;
                if (equalIndex < token.length() - 1) {
                	value = token.substring(equalIndex + 1);
                } else {                	value = "";
                }
                List values = (List) appendedParameters.get(key);
                if (values == null) {
                	values = new ArrayList();
                }
                values.add(value);
                appendedParameters.put(key, values);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(appendedParameters.size() + " parameters appended.");
        }

        // Merge the appended parameters and the original parameters.
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Merging appended parameters and original parameters...");
        }
    	for (Iterator it = appendedParameters.keySet().iterator();
    			it.hasNext(); ) {
    		String key = (String) it.next();
    		List values = (List) appendedParameters.get(key);
    		// If the parameter name (key) exists, merge parameter values.
    		if (parameters.containsKey(key)) {
    			String[] originalValues = (String[]) parameters.get(key);
    			if (originalValues != null) {
    				for (int i = 0; i < originalValues.length; i++) {
    					values.add(originalValues[i]);
    				}
    			}
    		}
    		parameters.put(key, values.toArray(new String[values.size()]));
    	}
    }

}
