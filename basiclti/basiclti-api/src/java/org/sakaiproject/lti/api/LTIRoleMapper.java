/**
 * Copyright (c) 2011-2014 The Apereo Foundation
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
