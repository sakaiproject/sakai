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

import org.apache.pluto.driver.config.DriverConfigurationException;

import javax.servlet.ServletContext;

/**
 * Abstract interface defining lifecycle methods for
 * Driver configuration services.
 *
 * @since Aug 10, 2005
 */
public interface DriverConfigurationService {

    /**
     * Initialize the service for use by the driver.
     * This method allows the service to retrieve required
     * resources from the context and instantiate any required
     * services.
     *
     * @param ctx the Portal's servlet context in which the
     * service will be executing.
     *
     */
    void init(ServletContext ctx) throws DriverConfigurationException;

    /**
     * Destroy the service, notifying it of shutdown.
     */
    void destroy() throws DriverConfigurationException;
}
