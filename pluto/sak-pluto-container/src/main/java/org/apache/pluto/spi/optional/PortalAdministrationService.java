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

import java.util.List;

/**
 * Provides callback for executing portal administration
 * tasks within the portlet environment.  This is key for
 * when the portal needs to accessing items such as the
 * session.
 *
 * Example Use Cases for the PortalAdministrationService:
 * <ul>
 *   <li>PortletException
 *   <p>The portal wants to ensure that portlets never
 *      become stuck in an unusable state.  To make sure
 *      this happens, they want the ability to clear the
 *      session if they repetedly catch exception from
 *      container invocations
 *   </p>
 *   <p>By implementing this services and registering
 *      and administrative request handler, the portal
 *      can invoke the doAdmin method of the container
 *      and then receive the request through the handler
 *      executing within the portlet environment (as
 *      opposed to the portal environment)
 *   </p>
 *   </li>
 * </ul>
 */
public interface PortalAdministrationService {

    List getAdministrativeRequestListeners();

    List getPortletInvocationListeners();

}
