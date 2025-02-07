/*
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.portal.api;

import java.util.Collection;

public interface PortalSubPageNavProvider {
    /**
     * Each provider should return a unique name, a good choice is the ENTITY_PREFIX
     * @return a name that uniquely identifies this provider
     */
    String getSubPageProviderName();

    /**
     * Each provider must provide its sub-page data in the following way
     * @param siteId the site
     * @param userId the user
     * @param pageIds the pages
     * @return sub page data for the given site, user, and pages
     */
    void getSubPageData(PortalSubPageData data, Collection<String> pageIds);
}
