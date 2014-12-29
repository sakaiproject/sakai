/**
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.api;

import java.util.Map;

import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
public interface SiteMembershipUpdater {

    /**
     *  @param payload The LTI launch parameters map. The roles key is pulled
     *                  from this.
     *  @param trustedConsumer This indicates whether or not all the Sakai
     *              sites and users are already mirrored onto the launching
     *              consumer.
     *  @param user The Sakai user we are updating.
     *  @param site The updated site who's membership we are updating.
     *  @return The updated site.
     */
    public Site addOrUpdateSiteMembership(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException;
}
