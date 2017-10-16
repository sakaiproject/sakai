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
package org.sakaiproject.tool.podcasts.entityproviders;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
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
import org.sakaiproject.tool.api.ToolManager;

/**
 * Entity provider for the Podcast tool
 */
@Slf4j
public class PodcastEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

	public final static String ENTITY_PREFIX = "podcast";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<PodcastItem> getPodcastsForSite(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("Podcast for site " + siteId);
		}

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the podcast for a site, via the URL /podcast/site/siteId");
		}

		//check user can access this site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}
		
		//check user can access the tool, it might be hidden
		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.podcasts");
		if(toolConfig == null || !toolManager.isVisible(site, toolConfig)) {
			throw new EntityNotFoundException("No access to tool in site: " + siteId, siteId);
		}
		
		//get entire list of podcasts
		List allPodcasts;
    try {
	    allPodcasts = podcastService.getPodcasts(siteId);
    } catch (Exception e) {
			throw new EntityNotFoundException("Error retrieving podcasts for site: " + siteId, siteId);
    }
    
		if (allPodcasts == null || allPodcasts.isEmpty()) {
			throw new EntityNotFoundException("No podcasts for site: " + siteId, siteId);
		}
		
		//filter the list of podcasts to the visible set
		//must pass in siteId since we are external to the tool
		//this also filters out the ones we cant see depending on the user permissions. It's taken care of in the impl.
		List<ContentResource> podcastResources = podcastService.filterPodcasts(allPodcasts, siteId);
		
		List<PodcastItem> podcastItems = new ArrayList<PodcastItem>();
		
		for(ContentResource resource: podcastResources) {
				
			//convert to our simplified object 
			PodcastItem item = new PodcastItem();
			
			ResourceProperties props = resource.getProperties();
			
			item.setTitle(props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			item.setDescription(props.getProperty(ResourceProperties.PROP_DESCRIPTION));
			item.setType(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
			item.setSize(Long.parseLong(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)));
			item.setUrl(resource.getUrl());
			item.setReleaseDate(resource.getReleaseDate().getTime());
			
			podcastItems.add(item);
			
		}
		
		return podcastItems;
	}

	
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON};
	}

	@Setter
	private PodcastService podcastService;

	@Setter
	private SiteService siteService;
	
	@Setter
	private ToolManager toolManager;

	

	
	/**
	 * Simplified helper class to represent an individual podcast item in a site
	 */
	public static class PodcastItem {
		
		@Getter @Setter
		private String title;
		
		@Getter @Setter
		private String description;
		
		@Getter @Setter
		private String url;
		
		@Getter @Setter
		private String type;
		
		@Getter @Setter
		private long size;
		
		/* note that EB turns these into millis anyway, so we might as well return them as millis */
		@Getter @Setter
		private long releaseDate;
	
	}
	
}
