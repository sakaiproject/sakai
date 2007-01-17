/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;

import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * @author andrew
 */
public class AuthZGroupBean extends ViewBean
{

	private AuthzGroup currentRealm = null;

	private String realmId = null;

	private boolean siteUpdateAllowed;

	public AuthZGroupBean(String pageName, String localSpace)
	{
		super(pageName, localSpace);
	}

	public AuthZGroupBean()
	{
	}

	public List getRoles()
	{
		if (currentRealm == null)
		{
			return new ArrayList();
		}
		else
		{
			Set roleset = currentRealm.getRoles();

			if (roleset == null)
			{
				return new ArrayList();
			}

			Role[] roles = (Role[]) roleset.toArray(new Role[roleset.size()]);

			Arrays.sort(roles);
			ArrayList roleBeans = new ArrayList(roles.length);
			for (int i = 0; i < roles.length; i++)
			{
				roleBeans.add(new RoleBean(roles[i]));
			}

			return roleBeans;
		}

	}

	public boolean isActiveAuthZGroup()
	{
		return (currentRealm != null);
	}

	public String getEditRealmUrl()
	{
		return this.getPageUrl(getPageName(), WikiPageAction.EDIT_REALM_ACTION
				.getName());
	}

	public String getEditRealmManyUrl()
	{
		return this.getPageUrl(getPageName(),
				WikiPageAction.EDIT_REALM_MANY_ACTION.getName());
	}


	public AuthzGroup getRealmEdit()
	{
		return currentRealm;
	}

	public String getRealmId()
	{
		return realmId;
	}

	public String getRealmReference()
	{
		return currentRealm.getReference();
	}

	public void setCurrentRealm(AuthzGroup currentRealm)
	{
		this.currentRealm = currentRealm;
		this.setLocalSpace(currentRealm.getId());

	}

	public void setRealmId(String realmId)
	{
		this.realmId = realmId;
	}

	public boolean isSiteUpdateAllowed()
	{
		return siteUpdateAllowed;
	}

	public void setSiteUpdateAllowed(boolean siteUpdateAllowed)
	{
		this.siteUpdateAllowed = siteUpdateAllowed;
	}

	public String getEscapedId()
	{
		return realmId.replaceAll("_", "__");
	}
}
