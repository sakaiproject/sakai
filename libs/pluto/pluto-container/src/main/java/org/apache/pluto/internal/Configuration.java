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
package org.apache.pluto.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Central location for Configuration info.
 *
 * @since Jul 2, 2005
 */
public class Configuration {

    private static final Log LOG =
        LogFactory.getLog(Configuration.class);

    public static final ResourceBundle BUNDLE =
        PropertyResourceBundle.getBundle("org.apache.pluto.core.pluto-configuration");

    private static final String DESCRIPTOR_SERVICE =
        "org.apache.pluto.descriptors.services.PortletAppDescriptorService";


    /**
     * org.apache.pluto.ALLOW_BUFFER
     */
    private static final String BUFFER_SUPPORT =
        "org.apache.pluto.ALLOW_BUFFER";

    /**
     * org.apache.pluto.PREVENT_UNECESSARY_CROSS_CONTEXT
     */
    private static final String PREVENT_UNECESSARY_CROSS_CONTEXT =
        "org.apache.pluto.PREVENT_UNECESSARY_CROSS_CONTEXT";


    public static String getPortletAppDescriptorServiceImpl() {
        String impl = BUNDLE.getString(DESCRIPTOR_SERVICE);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using Descriptor Service Impl: " + impl);
        }
        return impl;
    }

    private static Boolean buffering;
    public static boolean isBufferingSupported() {
        if (buffering == null) {
            try {
                String buffer = BUNDLE.getString(BUFFER_SUPPORT);
                buffering = new Boolean(buffer);
            } catch (MissingResourceException mre) {
                buffering = Boolean.FALSE;
            }
        }
        return buffering.booleanValue();
    }

    private static Boolean prevent;

    public static boolean preventUnecessaryCrossContext() {
        if (prevent == null) {
            try {
                String test = BUNDLE.getString(PREVENT_UNECESSARY_CROSS_CONTEXT);
                prevent = new Boolean(test);
            } catch (MissingResourceException mre) {
                prevent = Boolean.FALSE;
            }
        }
        return prevent.booleanValue();
    }
}
