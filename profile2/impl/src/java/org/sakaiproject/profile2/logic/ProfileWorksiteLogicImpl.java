/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.profile2.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileWorksiteLogic API
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Slf4j
public class ProfileWorksiteLogicImpl implements ProfileWorksiteLogic {

	/**
	 * Profile2 creates <code>project</code> type worksites.
	 */
	private static final String SITE_TYPE_PROJECT = "project";
	
	/**
	 * Connections invited to worksites are initially given the
	 * <code>access</code> role.
	 */
	private static final String ROLE_ACCESS = "access";
	
	/**
	 * Users who create worksites are initially given the <code>maintain</code>
	 * role.
	 */
	private static final String ROLE_MAINTAIN = "maintain";
	
	/**
	 * The id of the worksite home page.
	 */
	private static final String TOOL_ID_HOME = "home";

	/**
	 * The id of the iframe tool.
	 */
	private static final String TOOL_ID_IFRAME = "sakai.iframe";
		
	/**
	 * The id of the synoptic calendar tool.
	 */
	private static final String TOOL_ID_SUMMARY_CALENDAR = "sakai.summary.calendar";
	
	/**
	 * The id of the synoptic announcements tool.
	 */
	private static final String TOOL_ID_SYNOPTIC_ANNOUNCEMENT = "sakai.synoptic.announcement";

	/**
	 * The id of the synoptic chat tool.
	 */
	private static final String TOOL_ID_SYNOPTIC_CHAT = "sakai.synoptic.chat";

	/**
	 * The id of the synoptic discussions tool.
	 */
	private static final String TOOL_ID_SYNOPTIC_DISCUSSION = "sakai.synoptic.discussion";
	
	/**
	 * The id of the synoptic message center tool.
	 */
	private static final String TOOL_ID_SYNOPTIC_MESSAGECENTER = "sakai.synoptic.messagecenter";
	
	/**
	 *  Map of synoptic tool and the related tool ids.
	 */
	private final static Map<String, List<String>> SYNOPTIC_TOOL_ID_MAP;
	static
	{
		SYNOPTIC_TOOL_ID_MAP = new HashMap<String, List<String>>();
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SUMMARY_CALENDAR, new ArrayList<String>(Arrays.asList("sakai.schedule")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_ANNOUNCEMENT, new ArrayList<String>(Arrays.asList("sakai.announcements")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_CHAT, new ArrayList<String>(Arrays.asList("sakai.chat")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_DISCUSSION, new ArrayList<String>(Arrays.asList("sakai.discussion")));
		SYNOPTIC_TOOL_ID_MAP.put(TOOL_ID_SYNOPTIC_MESSAGECENTER, new ArrayList<String>(Arrays.asList("sakai.messages", "sakai.forums", "sakai.messagecenter")));
	}
	
	/**
	 * Map of tools and the related synoptic tool ids.
	 */
	private final static Map<String, String> TOOLS_WITH_SYNOPTIC_ID_MAP;
	static
	{
		TOOLS_WITH_SYNOPTIC_ID_MAP = new HashMap<String, String>();
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.schedule", TOOL_ID_SUMMARY_CALENDAR);
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.announcements", TOOL_ID_SYNOPTIC_ANNOUNCEMENT);
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.chat", TOOL_ID_SYNOPTIC_CHAT);
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.discussion", TOOL_ID_SYNOPTIC_DISCUSSION);
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.messages", TOOL_ID_SYNOPTIC_MESSAGECENTER);
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.forums", TOOL_ID_SYNOPTIC_MESSAGECENTER);
		TOOLS_WITH_SYNOPTIC_ID_MAP.put("sakai.messagecenter", TOOL_ID_SYNOPTIC_MESSAGECENTER);
	}
	
	/**
	 * The tool to place on the home page.
	 */
	private static final String HOME_TOOL = "sakai.iframe.site";
	
	/**
	 * The tool used to modify the worksite after creation.
	 */
	private static final String SITEINFO_TOOL = "sakai.siteinfo";

	/**
	 * The tool used to unjoin worksites.
	 */
	private static final String MEMBERSHIP_TOOL = "sakai.membership";
	
	/**
	 * Worksite setup tools.
	 */
	private static final String WORKSITE_SETUP_TOOLS = "wsetup.home.toolids";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createWorksite(final String siteTitle, final String ownerId,
			final Collection<Person> members, boolean notifyByEmail) {

		// double-check permission
		if (false == sakaiProxy.isUserAllowedAddSite(ownerId)) {
			log .warn("user " + ownerId + " tried to create worksite without site.add");
			return false;
		}

		// ensure site id is unique
		String siteId = sakaiProxy.createUuid();
		while (true == sakaiProxy.checkForSite(siteId)) {
			siteId = sakaiProxy.createUuid();
		}

		final Site site = sakaiProxy.addSite(siteId, SITE_TYPE_PROJECT);
		
		if (null == site) {
			log.warn("unable to create new worksite from Profile2");
			return false;
		} else {
			// set initial site maintainer to Profile2 user creating worksite
			User owner = sakaiProxy.getUserById(ownerId);
			if (null != owner) {
				// false == provided
				site.addMember(ownerId, site.getMaintainRole(), true, false);
			} else {
				log.warn("unknown user " + ownerId + " tried to create worksite");
				return false;
			}

			addSiteMembers(members, site);
			
			addTitleAndDescription(siteTitle, ownerId, site);
			
			addHomePageAndTools(site);
			
			site.setPublished(true);
			
			if (false == sakaiProxy.saveSite(site)) {
				log.warn("unable to save new worksite from Profile2");
				return false;
			}
			
			emailSiteMembers(siteTitle, ownerId, members, notifyByEmail, site);
			
			return true;
		}
	}
	
	private void addHomePageAndTools(final Site site) {
		
		// we will always have a home page
		SitePage homePage = site.addPage();
		homePage.getPropertiesEdit().addProperty(
				SitePage.IS_HOME_PAGE, Boolean.TRUE.toString());
		
		Tool homeTool = sakaiProxy.getTool(HOME_TOOL);
		
		ToolConfiguration homeToolConfig = homePage.addTool();
		homeToolConfig.setTool(TOOL_ID_HOME, homeTool);
		homeToolConfig.setTitle(homeTool.getTitle());
					
		// normally brings in sakai.siteinfo
		List<String> toolIds = sakaiProxy.getToolsRequired(SITE_TYPE_PROJECT);
					
		int synopticToolIndex = addRequiredToolsForWorksite(site, homePage, toolIds);
		
		// for synoptic tools
		if (synopticToolIndex > 0) {
			homePage.setLayout(SitePage.LAYOUT_DOUBLE_COL);
		}
		
		addRequiredHomeTools(homePage, toolIds, synopticToolIndex);
	}
		
	private int addRequiredToolsForWorksite(final Site site,
			SitePage homePage, List<String> toolIds) {
		
		int synopticToolIndex = 0;
		
		for (String toolId : toolIds) {
			
			if (isToolToIgnore(toolId)) {
				continue;
			} else if (isToolWithSynopticTool(toolId)) {
				
				// add tool
				SitePage toolPage = site.addPage();
				toolPage.addTool(toolId);
				
				// add corresponding synoptic tool if not already added
				if (false == isToolAlreadyAdded(homePage, TOOLS_WITH_SYNOPTIC_ID_MAP.get(toolId))) {
					
					ToolConfiguration toolConfig = homePage.addTool(TOOLS_WITH_SYNOPTIC_ID_MAP.get(toolId));
					if (null != toolConfig) {
						toolConfig.setLayoutHints(synopticToolIndex + ",1");

						for (int i = 0; i < synopticToolIndex; i++) {
							toolConfig.moveUp();
						}
						
						synopticToolIndex++;
					}
				}
			} else if (null != sakaiProxy.getTool(toolId)) {
											
					SitePage toolPage = site.addPage();
					toolPage.addTool(toolId);
			}
		}
		return synopticToolIndex;
	}
	
	private void addRequiredHomeTools(SitePage homePage, List<String> toolIds,
			int synopticToolIndex) {
		
		// home tools specified in sakai.properties or default set of home tools
		List<String> homeToolIds = getHomeToolIds();
		
		for (String homeToolId : homeToolIds) {
			
			if (isToolToIgnore(homeToolId)) {
				continue;
			} else {
				
				// check for corresponding tool
				if (SYNOPTIC_TOOL_ID_MAP.get(homeToolId) != null &&
						CollectionUtils.containsAny(SYNOPTIC_TOOL_ID_MAP.get(homeToolId), toolIds)) {
											
					// check it hasn't been added already
					if (false == isToolAlreadyAdded(homePage, homeToolId)) {
													
						ToolConfiguration toolConfig = homePage.addTool(homeToolId);
						if (null != toolConfig) {
							toolConfig.setLayoutHints(synopticToolIndex + ",1");
			
							for (int i = 0; i < synopticToolIndex; i++) {
								toolConfig.moveUp();
							}
							
							synopticToolIndex++;
						}
					}
				}
			}
		}
	}
	
	private List<String> getHomeToolIds() {
		
		List<String> homeToolIds;

		if (null != sakaiProxy.getServerConfigurationParameter(WORKSITE_SETUP_TOOLS + "." + SITE_TYPE_PROJECT, null)) {
			homeToolIds = new ArrayList<String>(Arrays.asList(
					sakaiProxy.getServerConfigurationParameter(WORKSITE_SETUP_TOOLS + "." + SITE_TYPE_PROJECT, null)));
		} else if (null != sakaiProxy.getServerConfigurationParameter(WORKSITE_SETUP_TOOLS, null)) {
			homeToolIds = new ArrayList<String>(Arrays.asList(
					sakaiProxy.getServerConfigurationParameter(WORKSITE_SETUP_TOOLS, null)));
		} else {
			homeToolIds = new ArrayList<String>();
		}
		return homeToolIds;
	}
	
	private void addTitleAndDescription(final String siteTitle,
			final String ownerId, final Site site) {
		
		// finishing setting up site
		site.setTitle(siteTitle);
		// add description for editing the worksite
		site.setDescription(Messages.getString("worksite.help",
				new Object[] { sakaiProxy.getTool(SITEINFO_TOOL).getTitle(),
				sakaiProxy.getTool(MEMBERSHIP_TOOL).getTitle(),
				sakaiProxy.getUserDisplayName(ownerId),
				sakaiProxy.getUserEmail(ownerId),
				siteTitle}));
	}
	
	private void addSiteMembers(final Collection<Person> members,
			final Site site) {
		
		// user could create worksite without any connections (that's okay)
		if (null != members) {
			for (Person member : members) {
				User user = sakaiProxy.getUserById(member.getUuid());
				if (null != user) {						
					// false == provided
					site.addMember(member.getUuid(), sakaiProxy.getServerConfigurationParameter("profile2.worksite.access.role",ROLE_ACCESS), true, false);
				} else {
					log .warn("attempt to add unknown user " + member.getUuid() + " to worksite");
				}
			}
		}
	}

	private boolean isToolAlreadyAdded(SitePage homePage, String homeToolId) {
		for (ToolConfiguration tool : homePage.getTools()) {
			if (StringUtils.equals(tool.getToolId(), homeToolId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isToolWithSynopticTool(String toolId) {
		return TOOLS_WITH_SYNOPTIC_ID_MAP.containsKey(toolId);
	}
	
	private boolean isToolToIgnore(String toolId) {
		return StringUtils.equals(toolId, TOOL_ID_IFRAME) || StringUtils.equals(toolId, HOME_TOOL);
	}

	private void emailSiteMembers(final String siteTitle, final String ownerId,
			final Collection<Person> members, boolean notifyByEmail,
			final Site site) {
		
		if (true == notifyByEmail) {
			
			Thread thread = new Thread() {
				@Override
				public void run() {
					emailSiteMembers(siteTitle, site.getUrl(), ownerId, members);		
				}
			};
			thread.start();
		}
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
		
		sakaiProxy.sendEmail(member.getUuid(),
				ProfileConstants.EMAIL_TEMPLATE_KEY_WORKSITE_NEW, replacementValues);
	}

	@Setter
	private SakaiProxy sakaiProxy;
		
}
