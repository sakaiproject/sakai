/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.service;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import lombok.extern.slf4j.Slf4j;

/**
 * When a site is updated we check that all the pages have aliases.
 * We want to keep existing aliases as they may have been sent to someone in an email.
 * If the title changes though we should generate a new alias.
 * Should sort by date as newest alias should be used.
 * @author buckett
 *
 */
@Slf4j
public class AliasingSiteAdvisor implements Observer
{
	/**
	 * Configuration parameter for sakai.properties to control if we generate page aliases.
	 */
	private static final String PORTAL_USE_PAGE_ALIASES = "portal.use.page.aliases";

	private static String PAGE_ALIAS = Entity.SEPARATOR+ "pagealias"+ Entity.SEPARATOR;
	
	private AliasService aliasService;
	
	private SiteService siteService;
	
	private ServerConfigurationService serverConfigurationService;
	
	private EntityManager entityManager;
	
	private EventTrackingService eventTrackingService;

	/**
	 * Maximum length of a page alias.
	 */
	private int maxLength;

	public int getMaxLength()
	{
		return maxLength;
	}

	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	public void init()
	{
		if (serverConfigurationService.getBoolean(PORTAL_USE_PAGE_ALIASES, false))
		{
			log.info("Page aliases will be generated.");
			// Only want a local observer so the node that performs the site save updates the aliases.
			getEventTrackingService().addLocalObserver(this);
		}
	}
	

	public void update(Observable o, Object arg) {
		if (arg instanceof Event) {
			Event event = (Event)arg;
			if (SiteService.SECURE_UPDATE_SITE.equals(event.getEvent()) || SiteService.SECURE_ADD_SITE.equals(event.getEvent()))
			{
				Reference ref = entityManager.newReference(event.getResource());
				Entity entity = ref.getEntity();
				if (entity instanceof Site) {
					update((Site)entity);
				} else {
					log.warn("Couldn't find site that has just been updated: "+ entity);
				}
				
			}

		}
	}
	
	public void update(Site site)
	{
		for (SitePage page : (List<SitePage>)site.getPages())
		{
			List<Alias> aliases = (List<Alias>)aliasService.getAliases(page.getReference());
			String shortName = resolvePageName(site, page, page.getTitle());
			if (shortName == null || shortName.length() == 0)
			{
				continue;
			}
			
			String possibleAlias = PAGE_ALIAS+ page.getSiteId()+ Entity.SEPARATOR+ shortName;
			
			boolean exists = false;
			if (!aliases.isEmpty()) 
			{			
				for (Alias alias: aliases)
				{
					if (possibleAlias.equals(alias.getId()))
					{
						exists = true;
						continue;
					}
				}
				
			}
			if (!exists)
			{
				// Create a new alias.
				try
				{
					aliasService.setAlias(possibleAlias, page.getReference());
				}
				catch (IdUsedException e)
				{
					log.debug("Alias already exists for: {}", possibleAlias);
				}
				catch (IdInvalidException e)
				{
					log.warn("Failed to generate a sensible alias: "+ possibleAlias);
				}
				catch (PermissionException e)
				{
					// Logger and there isn't any point in carrying on.
					log.warn("Lack of permission to create alias: "+ e.getMessage());
					break;
				}
			}
				
		}

	}
	
	/**
	 * Attempt to generate a short name for the page. This could use tool names. 
	 * @param site The site in which the page is.
	 * @param page The page.
	 * @param title The title of the page.
	 * @return
	 */
	private String resolvePageName(Site site, SitePage page, String title)
	{
		String alias = title.toLowerCase();
		alias = alias.replaceAll("[^a-z,0-9,_ ]", ""); // Replace everything but good characters
		alias = alias.replaceAll(" ", "_"); // Translate spaces to underscores
		alias = alias.replaceAll("_+", "_"); // Trim multiple underscores to one
		if (alias.length() > maxLength) // Trim if longer than maxlength
		{
			alias = alias.substring(0, maxLength); 
		}
		if (alias.endsWith("_")) { // Trim trailing underscore
			alias = alias.substring(0, alias.length()-1);
		}
		return alias;
	}

	public AliasService getAliasService()
	{
		return aliasService;
	}

	public void setAliasService(AliasService aliasService)
	{
		this.aliasService = aliasService;
	}

	public SiteService getSiteService()
	{
		return siteService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}


	public EntityManager getEntityManager() {
		return entityManager;
	}


	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}


	public EventTrackingService getEventTrackingService() {
		return eventTrackingService;
	}
}
