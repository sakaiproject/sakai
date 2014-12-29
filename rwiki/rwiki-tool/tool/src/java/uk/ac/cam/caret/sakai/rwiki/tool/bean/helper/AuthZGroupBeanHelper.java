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

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.site.api.SiteService;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;


public class AuthZGroupBeanHelper
{

	public static AuthZGroupBean createRealmBean(
			AuthzGroupService realmService, SiteService siteService, RWikiObject rwikiObject,
			ErrorBean errorBean, ViewBean vb, String siteId)
	{
		AuthZGroupBean rb = new AuthZGroupBean(vb.getPageName(), vb
				.getLocalSpace());

		String realmId = rwikiObject.getRealm();
		rb.setRealmId(realmId);
		try
		{
			AuthzGroup realm = realmService.getAuthzGroup(realmId);
			rb.setCurrentRealm(realm);
		}
		catch (GroupNotDefinedException e)
		{
			ResourceLoaderBean rlb = ResourceLoaderHelperBean.getResourceLoaderBean();
			errorBean.addError(rlb.getString("auzgroup.groupnotdef1","Realm")+": " + realmId
					+ rlb.getString("auzgroup.groupnotdef2"," is not recognised in the system."));
		}
		boolean update = realmService.allowUpdate(realmId);
		boolean siteUpdate = siteService.allowUpdateSite(siteId);
		

		rb.setSiteUpdateAllowed(update && siteUpdate);

		return rb;
	}

}
