/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * A set of web services for i18n
 *
 * @author Unicon
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class SakaiI18n extends AbstractWebService {

    private static final Log LOG = LogFactory.getLog(SakaiI18n.class);

    /**
     *Returns the content of the specified properties file in a defined language
     * and returns the default value if the key doesn't exists in that lanaguage.
     *
     * @param sessionid        id of a valid session
     * @param locale            the language to return in  IETF BCP 47 language tag string (samples: es-ES, jap)
     * @param resourceClass   Where to find the properties files (Samples: org.sakaiproject.rubrics.logic.RubricsService  or org.sakaiproject.sharedI18n.SharedProperties)
     * @param resourceBundle  The bundle itself (Samples: rubricsMessages, or org.sakaiproject.sharedI18n.bundle.shared)
     * @return  a String containing a "properties" file in the desired language
     *
     */
    @WebMethod
    @Path("/getI18nProperties")
    @Produces("text/plain")
    @GET
    public String getI18nProperties(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "locale", partName = "locale") @QueryParam("locale") String locale,
            @WebParam(name = "resourceclass", partName = "resourceclass") @QueryParam("resourceclass") String resourceClass,
            @WebParam(name = "resourcebundle", partName = "resourcebundle") @QueryParam("resourcebundle") String resourceBundle) {

        Session session = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            LOG.warn("NonSuperUser trying to access to translations: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to access to translations: " + session.getUserId());
        }
        try {
            StringBuilder lines = new StringBuilder();
            //Convert the locale string in a Locale object
            Locale loc = Locale.forLanguageTag(locale);
            ResourceLoader rb = new Resource().getLoader(resourceClass,resourceBundle);
            rb.setContextLocale(loc);
            //Get all the properties and iterate to return the right value
            Set properties = rb.keySet();
            Iterator keys = properties.iterator();
            while (keys.hasNext()){
                String key = keys.next().toString();
                lines.append(key + "=" + rb.getString(key) + "\n");
            }
            return lines.toString();
        } catch (Exception e) {
            LOG.warn("WS getI18nProperties(): " + e.getClass().getName() + " : " + e.getMessage());
            return "";
        }

    }

}
