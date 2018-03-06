/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums;

/**
 * A user's anonymous ID must remain consistent across a site (so they can be graded consistently). 
 * But the anonymous ID should be different in other sites to prevent any way to deduce identities based on mutual enrollments.
 * So, each row maps siteIds to userIds to anonIds
 * @author bbailla2
 */
public interface AnonymousMapping
{
	public String getSiteId();
	public void setSiteId(String siteId);

	public String getUserId();
	public void setUserId(String userId);

	public String getAnonId();
	public void setAnonId(String anonId);
}
