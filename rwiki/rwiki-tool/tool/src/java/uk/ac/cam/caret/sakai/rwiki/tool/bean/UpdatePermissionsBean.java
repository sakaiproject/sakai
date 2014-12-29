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

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiPermissions;


public class UpdatePermissionsBean
{

	public static final String OWNER_READ_PARAM = "ownerRead";

	public static final String OWNER_WRITE_PARAM = "ownerWrite";

	public static final String OWNER_ADMIN_PARAM = "ownerAdmin";

	public static final String GROUP_READ_PARAM = "groupRead";

	public static final String GROUP_WRITE_PARAM = "groupWrite";

	public static final String GROUP_ADMIN_PARAM = "groupAdmin";

	public static final String PUBLIC_READ_PARAM = "publicRead";

	public static final String PUBLIC_WRITE_PARAM = "publicWrite";

	public static final String OVERWRITE_OWNER_READ_PARAM = "overwriteOwnerRead";

	public static final String OVERWRITE_OWNER_WRITE_PARAM = "overwriteOwnerWrite";

	public static final String OVERWRITE_OWNER_ADMIN_PARAM = "overwriteOwnerAdmin";

	public static final String OVERWRITE_GROUP_READ_PARAM = "overwriteGroupRead";

	public static final String OVERWRITE_GROUP_WRITE_PARAM = "overwriteGroupWrite";

	public static final String OVERWRITE_GROUP_ADMIN_PARAM = "overwriteGroupAdmin";

	public static final String OVERWRITE_PUBLIC_READ_PARAM = "overwritePublicRead";

	public static final String OVERWRITE_PUBLIC_WRITE_PARAM = "overwritePublicWrite";

	public static final String NEW_OWNER_PARAM = "newOwner";

	public static final String NEW_REALM_PARAM = "newRealm";

	public static final String UPDATE_PERMISSIONS_PARAM = "updatePermissions";

	public static final String UPDATE_VALUE = "update";

	public static final String OVERWRITE_VALUE = "overwrite";

	private String owner, realm, updatePermissionsMethod;

	private RWikiPermissions permissions, overwritePermissions;

	public UpdatePermissionsBean()
	{
		// EMPTY
	}

	public RWikiPermissions getOverwritePermissions()
	{
		return overwritePermissions;
	}

	public void setOverwritePermissions(RWikiPermissions overwritePermissions)
	{
		this.overwritePermissions = overwritePermissions;
	}

	public RWikiPermissions getPermissions()
	{
		return permissions;
	}

	public void setPermissions(RWikiPermissions permissions)
	{
		this.permissions = permissions;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getRealm()
	{
		return realm;
	}

	public void setRealm(String realm)
	{
		this.realm = realm;
	}

	public String getUpdatePermissionsMethod()
	{
		return updatePermissionsMethod;
	}

	public void setUpdatePermissionsMethod(String updatePermissionsMethod)
	{
		this.updatePermissionsMethod = updatePermissionsMethod;
	}

}
