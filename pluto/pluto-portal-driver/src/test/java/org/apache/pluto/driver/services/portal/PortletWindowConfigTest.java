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

import junit.framework.TestCase;

/**
 *
 * @version 1.0
 * @since Dec 1, 2005
 */
public class PortletWindowConfigTest extends TestCase {

    public void testCreatePortletId() {
        assertEquals("context.PortletName!", PortletWindowConfig.createPortletId("context", "PortletName", ""));
        assertEquals("c.PortletName!1234567", PortletWindowConfig.createPortletId("c", "PortletName", "1234567"));
        assertEquals("context.P!", PortletWindowConfig.createPortletId("context", "P", ""));
        assertEquals("c.P!", PortletWindowConfig.createPortletId("c", "P", ""));
    }

    public void testParsePortletName() {
        assertEquals("PortletName", PortletWindowConfig.parsePortletName("context.PortletName!1234567"));
        assertEquals("PortletName", PortletWindowConfig.parsePortletName("c.PortletName!1234567"));
        assertEquals("P", PortletWindowConfig.parsePortletName("context.P!"));
        assertEquals("P", PortletWindowConfig.parsePortletName("c.P!ABC"));
        assertEquals("PortletName", PortletWindowConfig.parsePortletName("context.PortletName"));
    }

    public void testParseContextPath() {
        assertEquals("context", PortletWindowConfig.parseContextPath("context.PortletName!aasdf"));
        assertEquals("c", PortletWindowConfig.parseContextPath("c.PortletName!1234"));
        assertEquals("context", PortletWindowConfig.parseContextPath("context.P"));
        assertEquals("c", PortletWindowConfig.parseContextPath("c.P"));
    }

    public void testParseMetaInfo() {
        assertEquals("A", PortletWindowConfig.parseMetaInfo("c.p!A"));
        assertEquals("", PortletWindowConfig.parseMetaInfo("c.p"));
    }

    public void testParseInvalidId() {
        testParseInvalid("Use/Slash");
        testParseInvalid("NoPortlet.");
    }

    private void testParseInvalid(String id) {
        try {
            PortletWindowConfig.parseContextPath(id);
            fail("Exception should have been thrown.");
        }
        catch(IllegalArgumentException iae) {

        }
    }

}
