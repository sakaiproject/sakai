/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.service.impl;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.service.api.ScormPermissionService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

public abstract class ScormPermissionServiceImpl implements ScormPermissionService, ScormConstants {

	protected abstract SecurityService securityService();
	protected abstract ServerConfigurationService serverConfigurationService();
	protected abstract ToolManager toolManager();
	protected abstract SiteService siteService();
	
	public boolean canModify() {
		return canConfigure() || canDelete() || canViewResults();
	}
	
	public boolean canConfigure() {
		return hasPermission("scorm.configure");
	}

	public boolean canDelete() {
		return hasPermission("scorm.delete");
	}

	public boolean canLaunch() {
		return hasPermission("scorm.launch");
	}

	public boolean canUpload() {
		return hasPermission("scorm.upload");
	}

	public boolean canValidate() {
		return hasPermission("scorm.validate");
	}

	public boolean canViewResults() {
		return hasPermission("scorm.grade");
	}
	
	public boolean isOwner() {
		return true;
	}
	
	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative -
	 *        if true, form within the access path only (i.e. starting with /msg)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + REFERENCE_ROOT;

	} // getAccessPoint
	
	
	protected boolean hasPermission(String lock) {
		String siteId = toolManager().getCurrentPlacement().getContext();
		String reference = siteService().siteReference(siteId);
		
		return unlockCheck(lock, reference);
	}
	
	protected boolean unlockCheck(String lock, String id)
	{
		boolean isAllowed = securityService().isSuperUser();
		if(! isAllowed)
		{
			// make a reference from the resource id, if specified
			String ref = null;
			if (id != null)
			{
				ref = getReference(id);
			}
			
			isAllowed = ref != null && securityService().unlock(lock, ref);
		}
		
		return isAllowed;
		
	}
	
	protected String getReference(String id) {
		return new StringBuilder().append(getAccessPoint(false)).append(Entity.SEPARATOR).append(id).toString();
	}

}
