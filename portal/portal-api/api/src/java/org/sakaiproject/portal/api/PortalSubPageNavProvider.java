package org.sakaiproject.portal.api;

import java.util.Collection;

public interface PortalSubPageNavProvider {
    /**
     * Each provider should return a unique name, a good choice is the ENTITY_PREFIX
     * @return a name that uniquely identifies this provider
     */
    String getName();

    /**
     * Each provider must provide its sub-page data in the following way
     * @param siteId the site
     * @param userId the user
     * @param pageIds the pages
     * @return sub page data for the given site, user, and pages
     */
    String getData(String siteId, String userId, Collection<String> pageIds);
}
