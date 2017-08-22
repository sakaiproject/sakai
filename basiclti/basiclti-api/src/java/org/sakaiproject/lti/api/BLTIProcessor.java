/**
 * Copyright (c) 2011-2012 The Apereo Foundation
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

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/15/12
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BLTIProcessor {
    public int getOrder();
    public void beforeValidation(Map payload, boolean trustedConsumer) throws LTIException;
    public void afterValidation(Map payload, boolean trustedConsumer) throws LTIException;
    public void afterUserCreation(Map payload, User user) throws LTIException;
    public void afterLogin(Map payload, boolean trustedConsumer, User user) throws LTIException;
    public void afterSiteCreation(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException;
    public void afterSiteMembership(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException;
    public void beforeLaunch(Map payload, boolean trustedConsumer, User user, Site site, String toolPlacementId) throws LTIException;
}
