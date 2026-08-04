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
package org.apache.pluto.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import java.net.URL;
import java.net.MalformedURLException;


public class DefaultApplicationIdResolver implements ApplicationIdResolver {

    private static final Log LOG = LogFactory.getLog(DefaultApplicationIdResolver.class);

    private static final String WEB_XML = "/WEB-INF/web.xml";
	private static final String JNDI_PREFIX = "jndi:/";

    public String resolveApplicationId(ServletContext context) {
        try {
            URL webXmlUrl = context.getResource(WEB_XML);
            String path = webXmlUrl.toExternalForm();
            path = path.substring(0, path.indexOf(WEB_XML));

			int slash = path.lastIndexOf('/');
			if ((slash < JNDI_PREFIX.length()) && path.startsWith(JNDI_PREFIX)) {
				// Tomcat resources look like "jndi:/hostname/contextPath/WEB-INF/web.xml"
				// where "/contextPath" is "" for the ROOT context.
				// So if the last slash is the one in "jndi:/", the correct
				// result is "" and not "/hostname"
				path = "";
			} else {
				path = path.substring(slash);
			}

            int id = path.indexOf(".war");
            if(id > 0) {
                path = path.substring(0, id);
            }
            return path;
        } catch (MalformedURLException e) {
            LOG.warn("Error retrieving web.xml from ServletContext. Unable to derive contextPath.");
            return null;
        }
    }

    public int getAuthority() {
        return DEFAULT;
    }
}
