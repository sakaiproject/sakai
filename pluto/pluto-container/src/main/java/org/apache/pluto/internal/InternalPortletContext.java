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

import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.apache.pluto.descriptors.portlet.PortletAppDD;

public interface InternalPortletContext extends PortletContext {

    /**
     * Retrieve the unique identifier for the portlet context.
     * @return unique identifier.
     */
    public String getApplicationId();

    /**
     *
     * @return servlet context within which we belong
     */
    public ServletContext getServletContext();

    /**
     *
     * @return application config
     */
    public PortletAppDD getPortletApplicationDefinition();
}


