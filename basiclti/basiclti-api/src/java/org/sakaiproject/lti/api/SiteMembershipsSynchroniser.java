/**
 * Copyright (c) 2011-2015 The Apereo Foundation
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


public interface SiteMembershipsSynchroniser {

    public void synchroniseSiteMemberships(String siteId, String membershipsId, String membershipsUrl, String oauth_consumer_key, boolean isEmailTrustedConsumer, String callbackType);
}
