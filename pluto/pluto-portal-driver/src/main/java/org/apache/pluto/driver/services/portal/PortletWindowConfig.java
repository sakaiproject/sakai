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
package org.apache.pluto.driver.services.portal;


/**
 * Configuration of a portlet window on the portal page.
 */
public class PortletWindowConfig {

	// Private Member Variables ------------------------------------------------

    /** The context path of the associated portlet. */
    private String contextPath;

    /** The portlet name. */
    private String portletName;

    /** The metaInfo that provides uniqueness across window ids. */
    private String metaInfo;

    // Constructor -------------------------------------------------------------

    /**
     * No-arg constructor.
     */
    public PortletWindowConfig() {

    }

    private PortletWindowConfig(String contextPath, String portletName, String metaInfo) {
        this.contextPath = contextPath;
        this.portletName = portletName;
        this.metaInfo = metaInfo;
    }

    // Public Methods ----------------------------------------------------------

    public String getId() {
    	return createPortletId(contextPath, portletName, metaInfo);
    }

    public String getContextPath() {
        return contextPath;
    }

   public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    public String getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(String metaInfo) {
        this.metaInfo = metaInfo;
    }

    // Public Static Methods ---------------------------------------------------

    /**
     * Creates the portlet ID from context path and portlet name. The portlet ID
     * is constructed by concatinating the context path and the portlet name
     * using a dot ('.').
     *
     * The method checks that the portlet name parameter does not have a dot. This check
     * is not done for the portlet ID.
     *
     * @param contextPath  the portlet context path.
     * @param portletName  the portlet name.
     * @throws IllegalArgumentException if the portletName has a dot
     * @throws NullPointerException if the portlet Name or context path is null.
     */
    public static String createPortletId(String contextPath, String portletName, String metaInfo)
    	throws NullPointerException, IllegalArgumentException {

    	if (contextPath == null) {
    		throw new NullPointerException("Context path must not be null.");
    	}
    	if (portletName == null) {
    		throw new NullPointerException("Portlet name must not be null.");
    	}
    	if (portletName.indexOf('.') != -1) {
    		throw new IllegalArgumentException("Portlet name must not have a dot(period). Please remove the dot from the value of the portlet-name element ("+ portletName + ") in portlet.xml");
    	}

        if(metaInfo == null) {
            metaInfo = "";
        }
        return contextPath + "." + portletName + "!" + metaInfo;
    }

    /**
     * Parses out the portlet context path from the portlet ID.
     * @param portletId  the portlet ID to parse.
     * @return the portlet context path.
     */
    public static String parseContextPath(String portletId) {
    	int index = getSeparatorIndex(portletId);
        return portletId.substring(0, index);
    }

    /**
     * Parses out the portlet context path from the portlet ID.
     * @param portletId  the portlet ID to parse.
     * @return the portlet context path.
     */
    public static String parsePortletName(String portletId) {
    	int index = getSeparatorIndex(portletId);
        String postfix = portletId.substring(index + 1);
        index = postfix.indexOf("!");
        if(index > -1) {
            return postfix.substring(0, index);
        }
        return postfix;
    }

    public static String parseMetaInfo(String portletId) {
        int index = portletId.indexOf("!");
        if(index > -1) {
            return portletId.substring(index+1);
        }
        return "";
    }


    // Private Static Method ---------------------------------------------------

    /**
     * Parses the portlet ID and returns the separator (".") index. The portlet
     * ID passed in should be a valid ID: not null and contains ".". The portlet 
     * ID can have more than one dot, but the last one is taken to be the separator.
     *
     * @param portletId  the portlet ID to parse.
     * @return the separator index.
     * @throws IllegalArgumentException if portlet ID does not contain a dot.
     * @throws NullPointerException if the portlet ID is null
     */
    private static int getSeparatorIndex(String portletId)
    	throws NullPointerException, IllegalArgumentException {
    	if (portletId == null) {
    		throw new NullPointerException("Portlet ID is null");
    	}

        int index = portletId.lastIndexOf(".");
        if (index < 0 || index == (portletId.length() - 1)) {
        	throw new IllegalArgumentException("Portlet ID '" + portletId + "' does not contain a dot");
        }
    	return index;
    }

   public static PortletWindowConfig fromId(String portletWindowId) {
       String contextPath = parseContextPath(portletWindowId);
       String portletName = parsePortletName(portletWindowId);
       String metaInfo = parseMetaInfo(portletWindowId);
       return new PortletWindowConfig(contextPath, portletName, metaInfo);
   }
}

