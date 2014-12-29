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


public class AuthZGroupEditBean extends ViewBean
{
	/**
	 * Value of the save parameter that indicates we wish to save the content
	 */
	public static final String SAVE_VALUE = "save";

	/**
	 * Value of the save parameter that indicates we wish cancel this edit
	 */
	public static final String CANCEL_VALUE = "cancel";

	private AuthzGroup realmEdit;

	private List roleBeans;

	public AuthZGroupEditBean()
	{
	}

	public AuthZGroupEditBean(String pageName, String localSpace)
	{
		super(pageName, localSpace);
	}

	public String getRealmEditUrl()
	{
		return getPageUrl(getPageName(), WikiPageAction.EDIT_REALM_ACTION
				.getName());
	}

	public AuthzGroup getRealmEdit()
	{
		return realmEdit;
	}

	public void setRealmEdit(AuthzGroup realmEdit)
	{
		this.realmEdit = realmEdit;

		this.roleBeans = null;
	}

	public List getRoles()
	{
		if (roleBeans == null)
		{
			Set roleset = realmEdit.getRoles();

			if (roleset == null)
			{
				return new ArrayList();
			}

			Role[] roles = (Role[]) roleset.toArray(new Role[roleset.size()]);

			Arrays.sort(roles);
			roleBeans = new ArrayList(roles.length);
			for (int i = 0; i < roles.length; i++)
			{
				roleBeans.add(new RoleBean(roles[i]));
			}
		}
		return roleBeans;
	}
}
