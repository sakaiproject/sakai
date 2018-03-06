/**
 * Copyright (c) 2005-2015 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.io.Serializable;

import org.sakaiproject.api.app.messageforums.AnonymousMapping;

/**
 * A user's anonymous ID must remain consistent across a site (so they can be graded consistently). 
 * But the anonymous ID should be different in other sites to prevent any way to deduce identities based on mutual enrollments.
 * So, each row maps siteIds to userIds to anonIds
 * @author bbailla2
 */
public class AnonymousMappingImpl implements AnonymousMapping, Serializable
{
	private String siteId;
	private String userId;
	private String anonId;

	public AnonymousMappingImpl()
	{

	}

	/**
	 * CTOR
	 * @param siteId the siteId
	 * @param userId the userId
	 * @param anonId the anonymous ID
	 */
	public AnonymousMappingImpl(String siteId, String userId, String anonId)
	{
		this.siteId = siteId;
		this.userId = userId;
		this.anonId = anonId;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getAnonId()
	{
		return anonId;
	}

	public void setAnonId(String anonId)
	{
		this.anonId = anonId;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof AnonymousMapping)
		{
			AnonymousMapping am = (AnonymousMapping) obj;
			return isEqual(getSiteId(), am.getSiteId())
				&& isEqual(getUserId(), am.getUserId())
				&& isEqual(getAnonId(), am.getAnonId());
		}

		return false;
	}

	private boolean isEqual(Object o1, Object o2)
	{
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 37 * hash + (this.siteId != null ? this.siteId.hashCode() : 0);
		hash = 37 * hash + (this.userId != null ? this.userId.hashCode() : 0);
		hash = 37 * hash + (this.anonId != null ? this.anonId.hashCode() : 0);
		return hash;
	}
}
