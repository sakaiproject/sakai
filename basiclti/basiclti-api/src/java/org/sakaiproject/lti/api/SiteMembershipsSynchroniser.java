package org.sakaiproject.lti.api;


public interface SiteMembershipsSynchroniser {

    public void synchroniseSiteMemberships(String siteId, String membershipsId, String membershipsUrl, String oauth_consumer_key, String callbackType);
}
