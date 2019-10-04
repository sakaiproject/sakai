/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.wicket.util;

import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;

/**
 * Collection of static utilities.
 * @author bjones86
 */
public class Utils
{
    /**
     * Utility function to append the portal CDN version string to JavaScript references, if present.
     * @param fileReference the original file reference to append the version, if a version exists
     * @param portalCdnVersion the version string retrieved from sakai.properties, trimed to empty String if not found
     * @return the original javaScriptReference if no version is found, otherwise the version string is appended
     */
    public static String setCdnVersion(String fileReference, String portalCdnVersion)
    {
        return portalCdnVersion.isEmpty() ? fileReference : fileReference + "?version=" + portalCdnVersion;
    }

    public final static Comparator getPropertyComparator(SortParam sort)
    {
        Comparator propertyComparator = new Comparator()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                Object p1 = PropertyResolver.getValue(sort.getProperty().toString(), o1);
                Object p2 = PropertyResolver.getValue(sort.getProperty().toString(), o2);

                if (p1 != null && p2 != null & p1 instanceof Comparable)
                {
                    return ((Comparable) p1).compareTo((Comparable) p2);
                }

                return 0;
            }
        };

        return propertyComparator;
    }

    public final static String generateUrl(Behavior behavior, Component component, boolean isRelative)
    {
        if (component == null)
        {
            throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
        }

        String relativePagePath = component.urlForListener(behavior, component.getPage().getPageParameters()).toString();
        String url;

        if (!isRelative)
        {
            ServletWebRequest webRequest = (ServletWebRequest) component.getRequest();
            HttpServletRequest servletRequest = webRequest.getContainerRequest();
            String contextPath = servletRequest.getContextPath();
            String relativePath = relativePagePath.replaceAll("\\.\\.\\/", "");
            url = new StringBuilder(contextPath).append("/").append(relativePath).toString();
        }
        else
        {
            url = relativePagePath;
        }

        return url;
    }
}
