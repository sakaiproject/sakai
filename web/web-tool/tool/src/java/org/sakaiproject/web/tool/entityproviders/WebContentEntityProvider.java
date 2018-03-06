/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.web.tool.entityproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Entity provider for the Web Content tool
 */
@Slf4j
public class WebContentEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable
{

	//there may be multiple tool registrations that need to be served by this provider, list them here
	private static final String[] WEB_CONTENT_TOOL_IDS = {"sakai.iframe"};

	private static final String WEB_CONTENT_URL_PROP = "source";

	public final static String ENTITY_PREFIX = "webcontent";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<WebContentToolItem> getWebContentItemsForSite(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("Web content for site " + siteId);
		}

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the web content items for a site, via the URL /webcontent/site/siteId");
		}

		//user being logged in and having access to the site is handled in the API
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}

		Collection<ToolConfiguration> webContentTools = site.getTools(WEB_CONTENT_TOOL_IDS);

		List<WebContentToolItem> result = new ArrayList<WebContentToolItem>();
		for (ToolConfiguration t : webContentTools) {
			WebContentToolItem item = new WebContentToolItem(t.getTitle(), t.getConfig().getProperty(WEB_CONTENT_URL_PROP));
			result.add(item);
		}

		return result;
	}


	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON};
	}

	@Setter
	private SiteService siteService;

	@AllArgsConstructor
	public static class WebContentToolItem {
		@Getter
		private String title;
		@Getter
		private String url;

	}

}
