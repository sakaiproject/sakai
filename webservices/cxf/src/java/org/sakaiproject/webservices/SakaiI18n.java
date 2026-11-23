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
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.webservices.interceptor.NoIPRestriction;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.util.List;
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

    private static final String SHARED_RESOURCE_CLASS = InternationalizedMessages.class.getName();
    private static final Set<String> PUBLIC_SHARED_BUNDLES = Set.of(
            "account", "announcements", "announcement", "asn", "assessment", "audio-recorder",
            "bundle.polls", "calendar", "calendar-wc", "card_game", "chat", "commons", "condition",
            "content", "conversations", "coursecard", "courselist", "courses", "dashboard", "dashboard-widget",
            "date-picker-wc", "dialog", "dialog-content", "document-viewer", "dynamic-criterion", "dynamic-rubric",
            "file-picker", "forums", "gb-selector", "grader", "grader-toggle", "grades", "group-picker",
            "image-editor", "jumptotop", "lti", "mailarchive", "mailsender", "maximise-button", "microsoft",
            "org.sakaiproject.api.app.messagecenter.bundle.Messages", "org.sakaiproject.api.podcasts.bundle.Messages",
            "pager", "permissions-wc", "profile-wc", "pronunciation-player", "roster", "rubrics",
            "sakai-notifications", "sakai-picture-changer", "search", "signup", "site-picker", "sitestats",
            "submission-messager", "tag-selector", "tasks", "timer-bar", "toolname-mappings", "toolnames",
            "user-picker", "widget-picker", "widgetpanel");

    private ResourceLoader getPublicResourceLoader(String resourceClass, String resourceBundle) {
        if (StringUtils.isAnyBlank(resourceClass, resourceBundle)) {
            return null;
        }
        if (SHARED_RESOURCE_CLASS.equals(resourceClass) && PUBLIC_SHARED_BUNDLES.contains(resourceBundle)) {
            return new ResourceLoader(resourceBundle, InternationalizedMessages.class.getClassLoader());
        }
        if (!serverConfigurationService.getStringList("webservices.i18n.public-bundles", List.of())
                .contains(resourceClass + ":" + resourceBundle)) {
            return null;
        }
        ResourceLoader loader = Resource.getResourceLoader(resourceClass, resourceBundle);
        return loader != null ? loader : new ResourceLoader(resourceBundle, InternationalizedMessages.class.getClassLoader());
    }

    /**
     *Returns the content of the specified properties file in a defined language
     * and returns the default value if the key doesn't exists in that lanaguage.
     *
     * @param locale            the language to return in  IETF BCP 47 language tag string (samples: es-ES, jap)
     * @param resourceClass   Where to find the properties files (Samples: org.sakaiproject.rubrics.api.RubricsService  or org.sakaiproject.sharedI18n.SharedProperties)
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

        if (StringUtils.isBlank(locale)) {
            return "";
        }

        Locale requestedLocale = Locale.forLanguageTag(locale);
        StringBuilder i18n = new StringBuilder();
        ResourceLoader loader = getPublicResourceLoader(resourceClass, resourceBundle);
        if (loader != null) {
            loader.setContextLocale(requestedLocale);
            loader.forEach((key, value) -> i18n.append(key).append("=").append(value).append("\n"));
        } else if (StringUtils.isNotBlank(resourceClass) && messageBundleService.getAllBaseNames().contains(resourceClass)) {
            List<MessageBundleProperty> bundle = messageBundleService.getAllProperties(requestedLocale.toString(), resourceClass, null);
            if (bundle != null) {
                bundle.forEach(property -> i18n.append(property.getPropertyName()).append("=")
                        .append(property.getValue() != null ? property.getValue() : property.getDefaultValue()).append("\n"));
            }
        }
        return i18n.toString();
    }
}
