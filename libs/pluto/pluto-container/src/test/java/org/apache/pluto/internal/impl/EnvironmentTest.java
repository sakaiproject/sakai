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

import java.io.InputStream;
import java.util.Properties;

import org.apache.pluto.util.PlutoTestCase;

/**
 * Test Class
 *
 * @version 1.0
 * @since June 1, 2005
 */
public class EnvironmentTest extends PlutoTestCase {

    private Properties props;

    public void setUp() throws Exception {
        super.setUp();
        props = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/sakaiproject/pluto/environment.properties");
        assertNotNull("environment.properties missing from test classpath", in);
        try {
            props.load(in);
        } finally {
            in.close();
        }
    }

    public void testContainerMajorVersion() {
        String version = Environment.getPortletContainerVersion();
        String expectedMajor;
        int dot = version.indexOf('.');
        if (dot >= 0) {
            expectedMajor = version.substring(0, dot);
        } else {
            int dash = version.indexOf('-');
            expectedMajor = dash < 0 ? version : version.substring(0, dash);
        }
        assertEquals(expectedMajor, Environment.getPortletContainerMajorVersion());
    }

    public void testContainerMinorVersion() {
        String minor = Environment.getPortletContainerMinorVersion();
        if (minor.length() == 0) {
            assertTrue(Environment.getPortletContainerVersion().indexOf('.') < 0
                    && Environment.getPortletContainerVersion().indexOf('-') < 0);
        } else {
            assertTrue(Environment.getPortletContainerVersion().endsWith(minor));
        }
    }

    public void testContainerVersion() {
        assertEquals(props.getProperty("pluto.container.version"), Environment.getPortletContainerVersion());
    }

    public void testContainerName() {
        assertEquals(props.getProperty("pluto.container.name"), Environment.getPortletContainerName());
    }

    public void testSpecVersion() {
        assertEquals(Integer.parseInt(props.getProperty("javax.portlet.version.major")), Environment.getMajorSpecificationVersion());
    }

    public void testSpecMinorVersion() {
        assertEquals(Integer.parseInt(props.getProperty("javax.portlet.version.minor")), Environment.getMinorSpecificationVersion());
    }

    public void testServerInfo() {
        assertContains("Server Info does not contain container name.", props.getProperty("pluto.container.name"), Environment.getServerInfo());
        assertContains("Server Info does not contain container name.", props.getProperty("pluto.container.version"), Environment.getServerInfo());
    }
}
