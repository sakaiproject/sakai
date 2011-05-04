/**
 * 
 */
package org.sakaiproject.profile2.logic;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of ProfileWorksiteLogic API
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class ProfileWorksiteLogicImpl implements ProfileWorksiteLogic {

	public static final String SITE_TYPE = "project";
	
	public static final String ROLE_ACCESS = "access";
	public static final String ROLE_MAINTAIN = "maintain";
	
	private static final Logger log = Logger.getLogger(ProfileWorksiteLogicImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public boolean createWorksite(String siteTitle, String ownerId,
			Collection<Person> members, boolean notifyByEmail) {

		// double-check
		if (false == sakaiProxy.isUserAllowedAddSite(ownerId)) {
			log .warn("user " + ownerId + " tried to create worksite without site.add");
			return false;
		}

		// ensure site id is unique
		String siteId = idManager.createUuid();
		while (true == siteService.siteExists(siteId)) {
			siteId = idManager.createUuid();
		}

		try {
			Site site = siteService.addSite(siteId, SITE_TYPE);
			
			// TODO false == provided.
			// Where can this be obtained? User and UserDirectoryService don't expose this info.
			try {
				User user = userDirectoryService.getUser(ownerId);
				if (null != user) {
					site.addMember(ownerId, ROLE_MAINTAIN, true, false);					
				}
			} catch (UserNotDefinedException e) {
				log .warn("unknown user " + ownerId + " tried to create worksite");
				e.printStackTrace();
				return false;
			}

			// user could create worksite without any connections
			if (null != members) {
				for (Person member : members) {
					try {
						User user = userDirectoryService.getUser(member.getUuid());
						if (null != user) {
							
							// TODO privacy/preference check if/when added?
							
							// TODO false == provided
							site.addMember(member.getUuid(), ROLE_ACCESS, true, false);
						}
					} catch (UserNotDefinedException e) {
						log .warn("attempt to add unknown user " + member.getUuid() + " to worksite");
						e.printStackTrace();
					}
				}
			}

			// finishing setting up site
			site.setTitle(siteTitle);
			// TODO this description could contain instructions for adding
			// tools to the site etc.
			site.setDescription("");
						
			// tools (TODO these could be passed in, or site could be created
			// from a template site)
			// add home page for the site first, then the tools
			SitePage homePage = site.addPage();
			// this is the tool column title
			homePage.setTitle("Home");// TODO localization from bundles
			homePage.setTitleCustom(true);
			
			// essentially a blank page which can hold the site title/description
			ToolConfiguration homeTool = homePage
					.addTool("sakai.synoptic.messagecenter");
			homeTool.setTitle(siteTitle);
			
			SitePage siteInfoPage = site.addPage();
			siteInfoPage.addTool("sakai.siteinfo");
			
			// membership page so users can leave site
			SitePage siteMembershipPage = site.addPage();
			siteMembershipPage.addTool("sakai.membership");
						
			site.setPublished(true);
			siteService.save(site);
			
			return true;
			
		} catch (IdInvalidException e) {
			e.printStackTrace();
		} catch (IdUsedException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}
		
		// if we get here then site creation failed.
		return false;
	}

	// API injections
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private IdManager idManager;
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
}
