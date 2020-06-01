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

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
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
import java.util.List;
import java.util.Locale;

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

        StringBuilder i18n = new StringBuilder();
        ResourceLoader rb = null;

        if (StringUtils.isNotBlank(locale)) {
            if (StringUtils.isNotBlank(resourceClass) && StringUtils.isNotBlank(resourceBundle)) {
                try {
                    // load from spring using SakaiApplicationContext
                    rb = Resource.getResourceLoader(resourceClass, resourceBundle);
                    if (rb == null) {
                        // load from shared lib
                        rb = new ResourceLoader(resourceBundle, Class.forName(resourceClass).getClassLoader());
                    }
                } catch (Exception e) {
                    log.debug("Could not load i18n bundle: [{}|{}|{}], {}", resourceBundle, resourceClass, locale, e.getMessage());
                }
            }

            if (rb != null) {
                rb.setContextLocale(Locale.forLanguageTag(locale));
                rb.forEach((k, v) -> i18n.append(k).append("=").append(v).append("\n"));
            } else {
                // load from MessageBundleManager service
                if (StringUtils.isNotBlank(resourceClass) && messageBundleService.getAllBaseNames().contains(resourceClass)) {
                    // ensure that the requested bundle matches before performing a larger query
                    List<MessageBundleProperty> bundle = messageBundleService.getAllProperties(Locale.forLanguageTag(locale).toString(), resourceClass, null);
                    if (bundle != null && !bundle.isEmpty()) {
                        bundle.forEach(p -> i18n.append(p.getPropertyName()).append("=").append(p.getValue() != null ? p.getValue() : p.getDefaultValue()).append("\n"));
                    }
                }
            }
        }
        return i18n.toString();
    }
}
