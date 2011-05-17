/**
 * 
 */
package org.sakaiproject.profile2.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of ProfileWorksiteLogic API
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class ProfileWorksiteLogicImpl implements ProfileWorksiteLogic {

	/**
	 * Profile2 creates <code>project</code> type worksites.
	 */
	public static final String SITE_TYPE_PROJECT = "project";
	
	/**
	 * Connections invited to worksites are initially given the
	 * <code>access</code> role.
	 */
	public static final String ROLE_ACCESS = "access";
	
	/**
	 * Users who create worksites are initially given the <code>maintain</code>
	 * role.
	 */
	public static final String ROLE_MAINTAIN = "maintain";
	
	// the id of the worksite home page
	private static final String TOOL_ID_HOME = "home";
	// the id of synoptic tools
	private static final String TOOL_ID_SYNOPTIC = "sakai.synoptic.";
	// the tool to place on the home page
	private static final String HOME_TOOL = "sakai.iframe.site";
	// the tool used to modify the worksite after creation
	private static final String SITEINFO_TOOL = "sakai.siteinfo";
	// the tool used to unjoin worksites
	private static final String MEMBERSHIP_TOOL = "sakai.membership";
	
	private static final Logger log = Logger.getLogger(ProfileWorksiteLogicImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public boolean createWorksite(final String siteTitle, final String ownerId,
			final Collection<Person> members, boolean notifyByEmail) {

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
			final Site site = siteService.addSite(siteId, SITE_TYPE_PROJECT);
			
			// TODO false == provided (not if sure this matters)
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
							
							// TODO false == provided (not if sure this matters)
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
			// add description for editing the worksite
			site.setDescription(Messages.getString("worksite.help",
					new Object[] { toolManager.getTool(SITEINFO_TOOL).getTitle(),
					toolManager.getTool(MEMBERSHIP_TOOL).getTitle(),
					sakaiProxy.getUserDisplayName(ownerId),
					sakaiProxy.getUserEmail(ownerId),
					siteTitle}));
						
			// we will always have a home page
			SitePage homePage = site.addPage();
			homePage.getPropertiesEdit().addProperty(
					SitePage.IS_HOME_PAGE, Boolean.TRUE.toString());
			
			Tool homeTool = toolManager.getTool(HOME_TOOL);
			
			ToolConfiguration homeToolConfig = homePage.addTool();
			homeToolConfig.setTool(TOOL_ID_HOME, homeTool);
			homeToolConfig.setTitle(homeTool.getTitle());
			
			// for synoptic tools
			homePage.setLayout(SitePage.LAYOUT_DOUBLE_COL);
			
			List<String> toolIds = serverConfigurationService.getToolsRequired(SITE_TYPE_PROJECT);
			
			int synopticToolIndex = 0;
			for (String toolId : toolIds) {
								
				if (toolId.contains(TOOL_ID_SYNOPTIC)) {
					
					ToolConfiguration toolConfig = homePage.addTool(toolId);
					toolConfig.setLayoutHints(synopticToolIndex + ",1");
					
					synopticToolIndex++;
					
				} else {
					if (null != toolManager.getTool(toolId)) {
						SitePage toolPage = site.addPage();
						toolPage.addTool(toolId);
					}
				}
			}
			
			site.setPublished(true);
			siteService.save(site);
			
			if (true == notifyByEmail) {
				
				Thread thread = new Thread() {
					public void run() {
						emailSiteMembers(siteTitle, site.getUrl(), ownerId, members);		
					}
				};
				thread.start();
				
			}
			
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

	private void emailSiteMembers(String siteTitle, String siteUrl, String ownerId,
			Collection<Person> members) {
		
		for (Person member : members) {
			if (true == member.getPreferences().isWorksiteNewEmailEnabled()) {
				emailSiteMember(siteTitle, siteUrl, ownerId, member);
			}
		}
	}

	private void emailSiteMember(String siteTitle, String siteUrl, String ownerId, Person member) {
		
		// create the map of replacement values for this email template
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(ownerId));
		replacementValues.put("worksiteTitle", siteTitle);
		replacementValues.put("worksiteLink", siteUrl);
		replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
		replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
		replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());
		replacementValues.put("displayName", member.getDisplayName());
		
		sakaiProxy.sendEmail(member.getUuid(),
				ProfileConstants.EMAIL_TEMPLATE_KEY_WORKSITE_NEW, replacementValues);
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
	
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
}
