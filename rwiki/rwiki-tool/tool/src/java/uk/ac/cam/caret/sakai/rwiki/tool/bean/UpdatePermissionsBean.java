/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiPermissions;

// FIXME: Tool

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
