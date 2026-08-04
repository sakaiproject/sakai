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
package org.apache.pluto.spi.optional;

/**
 * Listener interface implemented by observers of
 * the PortletRegistry which intend to be notified
 * of new portlet application registrations.
 *
 * @since 1.1.0
 */
public interface PortletRegistryListener {

    /**
     * Recieve notification of a newly registered application.
     *
     * @param event registry event information
     */
    void portletApplicationRegistered(PortletRegistryEvent event);

    /**
     * Recieve notification of an application which is
     * removed from service.
     *
     * @param event registry event information
     */
    void portletApplicationRemoved(PortletRegistryEvent event);

}
