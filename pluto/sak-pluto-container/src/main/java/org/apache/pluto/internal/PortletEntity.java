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

import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;

import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.servlet.ServletDD;

/**
 * The PortletEntity encapsulates all data pertaining to a single portlet
 * instance.  This instance may appear zero or more times per user. The
 * PortletEntity consists of two primary peices of information, the Portlet
 * Definition as defined by the {@link PortletDD} and the Wrapping Servlet
 * information as defined by the{@link ServletDD}
 *
 */
public interface PortletEntity {

    /**
     * Returns the URI to the controller servlet that wraps this portlet.
     * <p>
     * Note: this method has been deprecated as of Pluto 1.1.2.  Future versions 
     * of Pluto will use the <code>PortletInvokerService</code> for resolving
     * the invoker url pattern.
     * 
     * @return the URI to the controller servlet that wraps this portlet.
     * @deprecated
     */
    String getControllerServletUri();

    /**
     * Returns an array of default preferences of this portlet. The default
     * preferences are retrieved from the portlet application descriptor.
     * <p>
     * Data retrieved from <code>portlet.xml</code> are injected into the domain
     * object <code>PortletPreferenceDD</code>. This method converts the domain
     * objects into <code>PortletPreference</code> objects.
     * </p>
     * <p>
     * Note that if no value is bound to a given preference key,
     * <code>PortletPreferenceDD.getValues()</code> will return an empty string
     * list, but the value array of <code>PortletPreference</code> should be set
     * to null (instead of an empty array).
     * </p>
     * <p>
     * This method never returns null, but the values held by PortletPreference
     * may be null.
     * </p>
     * @return the preference set
     *
     * @see org.apache.pluto.descriptors.portlet.PortletPreferenceDD
     */
    InternalPortletPreference[] getDefaultPreferences();

    /**
     * Returns the portlet description. The return value cannot be NULL.
     * @return the portlet description.
     */
    PortletDD getPortletDefinition();

    /**
     * Returns the preferences validator instance for this portlet.
     * One validator instance is created per portlet definition.
     * @return the preferences validator instance for this portlet.
     * @throws ValidatorException  if fail to instantiate the validator.
     */
    PreferencesValidator getPreferencesValidator()
    throws ValidatorException;

}



