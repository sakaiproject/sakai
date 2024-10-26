package org.sakaiproject.portal.api;

import java.util.Collection;

public interface PortalSubPageNavProvider {
    String getName();
    String getData(String siteId, String userId, Collection<String> pageIds);
}
