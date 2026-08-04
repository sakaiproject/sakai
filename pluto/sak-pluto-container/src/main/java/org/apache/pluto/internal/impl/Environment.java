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

import java.util.ResourceBundle;

/**
 * Utility class used to retrieve environment information from the
 * <code>environment.properties</code> file packaged with pluto.
 */
final class Environment {

    /**
     * Properties Resource Bundle containing pluto environment information.
     */
    public static final ResourceBundle PROPS;

    static {
        PROPS = ResourceBundle.getBundle("org.apache.pluto.environment");
    }


    /**
     * Retrieve the name of the container.
     * @return the container name.
     */
    public static final String getPortletContainerName() {
        return PROPS.getString("pluto.container.name");
    }

    /**
     * Retrieve the major version number.
     * @return the major version number.
     * @deprecated
     */
    public static final String getPortletContainerMajorVersion() {
        String version = getPortletContainerVersion();
        return version.substring(0, version.indexOf("."));
    }

    /**
     * Retrieve the minor version number.
     * @return the minor version number.
     * @deprecated
     */
    public static final String getPortletContainerMinorVersion() {
        String version = getPortletContainerVersion();
        return version.substring(version.indexOf("."));
    }

    /**
     * Retrieve the portlet container version.
     *
     * @return container version
     */
    public static final String getPortletContainerVersion() {
        return PROPS.getString("pluto.container.version");
    }

    /**
     * Retrieve the major version number of the specification which this version
     * of pluto supports.
     * @return te major specification version.
     */
    public static final int getMajorSpecificationVersion() {
        return Integer.parseInt(PROPS.getString("javax.portlet.version.major"));
    }

    /**
     * Retrieve the minor version number of the specification which this version
     * of pluto supports.
     * @return the minor specification version.
     */
    public static final int getMinorSpecificationVersion() {
        return Integer.parseInt(PROPS.getString("javax.portlet.version.minor"));
    }

    /**
     * Retrieve the formatted server info String required to be returned by the
     * PortletContext.
     * @return the server info.
     */
    public static final String getServerInfo() {
        StringBuffer sb = new StringBuffer(getPortletContainerName())
            .append("/")
            .append(getPortletContainerVersion());
        return sb.toString();
    }

}
