package org.sakaiproject.sitemanage.impl;

import org.sakaiproject.portal.api.PortalSubPageNavProvider;

public class PortalSubPageNavProviderImpl implements PortalSubPageNavProvider {

    @Override
    public String getName() {
      return "sakai.siteinfo";
    }
  
    @Override
    public String getData(String siteId, String userId, Collection<String> pageIds) {
      
    }
}