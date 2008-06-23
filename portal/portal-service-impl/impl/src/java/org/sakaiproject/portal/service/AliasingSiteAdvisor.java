package org.sakaiproject.portal.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteAdvisor;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;

/**
 * When a site is updated we check that all the pages have aliases.
 * We want to keep existing aliases as they may have been sent to someone in an email.
 * If the title changes though we should generate a new alias.
 * Should sort by date as newest alias should be used.
 * @author buckett
 *
 */
public class AliasingSiteAdvisor implements SiteAdvisor
{
	/**
	 * Configuration parameter for sakai.properties to control if we generate page aliases.
	 */
	private static final String PORTAL_USE_PAGE_ALIASES = "portal.use.page.aliases";

	private static Log log = LogFactory.getLog(AliasingSiteAdvisor.class);
	
	private static String PAGE_ALIAS = Entity.SEPARATOR+ "pagealias"+ Entity.SEPARATOR;
	
	private AliasService aliasService;
	
	private SiteService siteService;
	
	private ServerConfigurationService serverConfigurationService;

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
			siteService.addSiteAdvisor(this);
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
					if (log.isDebugEnabled())
					{
						log.debug("Alias already exists for: "+possibleAlias);
					}
				}
				catch (IdInvalidException e)
				{
					log.warn("Failed to generate a sensible alias: "+ possibleAlias);
				}
				catch (PermissionException e)
				{
					// Log and there isn't any point in carrying on.
					log.warn("Failed to create alias, lack of permission.", e);
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
	


}
