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

import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.webservices.interceptor.NoIPRestriction;

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

import lombok.extern.slf4j.Slf4j;

/**
 * A set of web services for i18n
 *
 * @author Unicon
 */

@Slf4j
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class SakaiI18n extends AbstractWebService {

    /**
     *Returns the content of the specified properties file in a defined language
     * and returns the default value if the key doesn't exists in that lanaguage.
     *
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
    @NoIPRestriction
    public String getI18nProperties(
            @WebParam(name = "locale", partName = "locale") @QueryParam("locale") String locale,
            @WebParam(name = "resourceclass", partName = "resourceclass") @QueryParam("resourceclass") String resourceClass,
            @WebParam(name = "resourcebundle", partName = "resourcebundle") @QueryParam("resourcebundle") String resourceBundle) {

        log.debug("locale: {}", locale);
        log.debug("resourceClass: {}", resourceClass);
        log.debug("resourceBundle: {}", resourceBundle);

        try {
            ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);
            rb.setContextLocale(Locale.forLanguageTag(locale));
            Iterator keys = rb.keySet().iterator();
            StringBuilder lines = new StringBuilder();
            while (keys.hasNext()){
                String key = keys.next().toString();
                lines.append(key + "=" + rb.getString(key) + "\n");
            }
            return lines.toString();
        } catch (Exception e) {
            log.warn("WS getI18nProperties(): {} : {}", e.getClass().getName(), e.getMessage());
            return "";
        }

    }

}
