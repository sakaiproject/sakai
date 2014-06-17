package org.sakaiproject.lti.api;

import java.util.Map;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;

public interface LTIRoleMapper {

    /**
     *  Pulls the roles value from the payload map and attempts to map that
     *  onto a role from the supplied site.
     *
     *  @param payload The LTI launch parameters map. The roles key is pulled
     *                  from this.
     *  @param user The Sakai user we are mapping a role for.
     *  @param site The site who's roles we are trying to map onto.
     *  @param trustedConsumer This indicates whether or not all the Sakai
     *              sites and users are already mirrored onto the launching
     *              consumer.
     *  @return A tuple of ltirole and site role.
     *  @author Adrian Fish <a.fish@lancaster.ac.uk>
     */
    public Map.Entry<String, String> mapLTIRole(Map payload, User user, Site site, boolean trustedConsumer) throws LTIException;
}
