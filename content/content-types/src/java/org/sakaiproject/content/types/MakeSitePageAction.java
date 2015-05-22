/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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
package org.sakaiproject.content.types;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceAction;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

/**
 * Page action to allow creation of a Page in the site that links to content in resources.
 * We need our own subclass to allow the action to be disabled.
 */
public class MakeSitePageAction extends BaseServiceLevelAction {

	public MakeSitePageAction(String id, ActionType actionType, String typeId)
	{
		super(id, actionType, typeId, false);
	}

	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
	private static final String RESOURCECLASS = "resource.class.type";
	private static final String RESOURCEBUNDLE = "resource.bundle.type";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);

	public boolean available(ContentEntity entity) {;
		return ServerConfigurationService.getBoolean("content.make.site.page",true)
				// Not in a dropbox
				&& !ContentHostingService.isInDropbox(entity.getId())
				// Not in a user site.
				&& !SiteService.isUserSite(EntityManager.newReference(entity.getReference()).getContext());
	}
}