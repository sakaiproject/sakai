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
package org.apache.pluto.driver;

import org.apache.pluto.driver.services.portal.admin.DriverAdministrationException;
import org.apache.pluto.driver.services.portal.admin.PortletRegistryAdminService;
import org.apache.pluto.driver.config.AdminConfiguration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;


/**
 * Publishing administrative servlet.
 * Allows external clients to connect and notify the portal
 * of available portlet applications.
 *
 * @version 1.0
 * @since Nov 23, 2005
 */
public class PublishServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
    throws ServletException, IOException {

        String context = req.getParameter("context");
        try {
            doPublish(context);
        }
        catch(Throwable t) {
            StringBuffer sb = new StringBuffer();
            sb.append("Unable to publish portlet application bound to context '"+context+"'.");
            sb.append("Reason: ").append(t.getMessage());
            res.getWriter().println(sb.toString());
        }
    }

    private void doPublish(String context) throws DriverAdministrationException {
        AdminConfiguration adminConfig = (AdminConfiguration)getServletContext()
            .getAttribute(AttributeKeys.DRIVER_ADMIN_CONFIG);

        PortletRegistryAdminService admin = adminConfig.getPortletRegistryAdminService();

        admin.addPortletApplication(context);
    }
}
