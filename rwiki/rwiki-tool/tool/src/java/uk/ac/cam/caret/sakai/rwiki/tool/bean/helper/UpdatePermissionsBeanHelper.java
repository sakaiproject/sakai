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

package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.UpdatePermissionsBean;

/**
 * @author andrew
 */
public class UpdatePermissionsBeanHelper
{

	public static UpdatePermissionsBean createUpdatePermissionsBean(
			HttpServletRequest req, RWikiObjectService objectService)
	{
		UpdatePermissionsBean ub = new UpdatePermissionsBean();

		if (ub.getPermissions() == null)
		{
			ub.setPermissions(objectService.createNewRWikiPermissionsImpl());
		}

		if (ub.getOverwritePermissions() == null)
		{
			ub.setOverwritePermissions(objectService
					.createNewRWikiPermissionsImpl());
		}

		String permission;
		permission = req.getParameter(UpdatePermissionsBean.OWNER_READ_PARAM);
		ub.getPermissions().setOwnerRead(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.OWNER_WRITE_PARAM);
		ub.getPermissions().setOwnerWrite(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.OWNER_ADMIN_PARAM);
		ub.getPermissions().setOwnerAdmin(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.GROUP_READ_PARAM);
		ub.getPermissions().setGroupRead(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.GROUP_WRITE_PARAM);
		ub.getPermissions().setGroupWrite(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.GROUP_ADMIN_PARAM);
		ub.getPermissions().setGroupAdmin(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.PUBLIC_READ_PARAM);
		ub.getPermissions().setPublicRead(convertPermission(permission));

		permission = req.getParameter(UpdatePermissionsBean.PUBLIC_WRITE_PARAM);
		ub.getPermissions().setPublicWrite(convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_OWNER_READ_PARAM);
		ub.getOverwritePermissions()
				.setOwnerRead(convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_OWNER_WRITE_PARAM);
		ub.getOverwritePermissions().setOwnerWrite(
				convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_OWNER_ADMIN_PARAM);
		ub.getOverwritePermissions().setOwnerAdmin(
				convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_GROUP_READ_PARAM);
		ub.getOverwritePermissions()
				.setGroupRead(convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_GROUP_WRITE_PARAM);
		ub.getOverwritePermissions().setGroupWrite(
				convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_GROUP_ADMIN_PARAM);
		ub.getOverwritePermissions().setGroupAdmin(
				convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_PUBLIC_READ_PARAM);
		ub.getOverwritePermissions().setPublicRead(
				convertPermission(permission));

		permission = req
				.getParameter(UpdatePermissionsBean.OVERWRITE_PUBLIC_WRITE_PARAM);
		ub.getOverwritePermissions().setPublicWrite(
				convertPermission(permission));

		ub.setOwner(req.getParameter(UpdatePermissionsBean.NEW_OWNER_PARAM));
		ub.setRealm(req.getParameter(UpdatePermissionsBean.NEW_REALM_PARAM));

		ub.setUpdatePermissionsMethod(req
				.getParameter(UpdatePermissionsBean.UPDATE_PERMISSIONS_PARAM));

		return ub;
	}

	private static boolean convertPermission(String permission)
	{
		return (permission != null);
	}

}
