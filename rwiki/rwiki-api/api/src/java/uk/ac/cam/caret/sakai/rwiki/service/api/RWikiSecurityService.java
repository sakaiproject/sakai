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

package uk.ac.cam.caret.sakai.rwiki.service.api;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;

// FIXME: Service

public interface RWikiSecurityService
{

	/**
	 * Get the current site reference, must be called in a request cycle to work
	 * 
	 * @return
	 */
	String getSiteReference();

	/**
	 * Check for get permission (rwiki.read) on the reference which must point
	 * to a resolvable entity. This check is performed for the current user
	 * 
	 * @param reference
	 * @return
	 */
	boolean checkGetPermission(String reference);

	/**
	 * Check for update permission (rwiki.update) on the reference which must
	 * point to a resolvable entity. This check is performed for the current
	 * user
	 * 
	 * @param reference
	 * @return
	 */
	boolean checkUpdatePermission(String reference);

	/**
	 * Check for admin permission (rwiki.admin) on the reference which must
	 * point to a resolvable entity. This check is performed for the current
	 * user
	 * 
	 * @param reference
	 * @return
	 */
	boolean checkAdminPermission(String reference);

	/**
	 * Check for super admin permission (rwiki.superadmin) on the reference
	 * which must point to a resolvable entity. This check is performed for the
	 * current user
	 * 
	 * @param reference
	 * @return
	 */
	boolean checkSuperAdminPermission(String reference);

	/**
	 * Check for super create permission (rwiki.create) on the reference which
	 * must point to a resolvable entity. This check is performed for the
	 * current user
	 * 
	 * @param reference
	 * @return
	 */
	boolean checkCreatePermission(String reference);

	/**
	 * Check for search permission (rwiki.search) on the reference which must
	 * point to a resolvable entity. This check is performed for the current
	 * user
	 * 
	 * @param reference
	 * @return
	 */
	boolean checkSearchPermission(String reference);

	/**
	 * Get the current site ID
	 * 
	 * @return
	 */
	String getSiteId();

	/**
	 * generate a default reference to a permission from the page space URL
	 * 
	 * @param pageSpace
	 * @return
	 */
	String createPermissionsReference(String pageSpace);

	/** Security function name for create. required to create */
	public static final String SECURE_CREATE = "rwiki.create";

	/** Security function name for read. required to read */
	public static final String SECURE_READ = "rwiki.read";

	/** Security function name for update. required to update */
	public static final String SECURE_UPDATE = "rwiki.update";

	/** Security function name for delete. required to delete */
	public static final String SECURE_DELETE = "rwiki.delete";

	/** Security function name for admin. Having this overrides for the site */
	public static final String SECURE_SUPER_ADMIN = "rwiki.superadmin";

	/**
	 * Security function name for admin. Having this is required to do admin on
	 * objects
	 */
	public static final String SECURE_ADMIN = "rwiki.admin";

	/**
	 * Check for read permission
	 * 
	 * @param rwo
	 * @param user
	 * @return
	 */
	boolean checkRead(RWikiEntity rwe);

	/**
	 * check for update permission
	 * 
	 * @param rwo
	 * @param user
	 * @return
	 */
	boolean checkUpdate(RWikiEntity rwe);

	/**
	 * check for create permission
	 * 
	 * @param rwo
	 * @param user
	 * @return
	 */
	boolean checkCreate(RWikiEntity rwe);

	/**
	 * check for admin permission
	 * 
	 * @param rwo
	 * @param user
	 * @return
	 */
	boolean checkAdmin(RWikiEntity rwe);

}
