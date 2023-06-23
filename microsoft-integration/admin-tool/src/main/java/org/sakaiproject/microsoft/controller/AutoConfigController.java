/**
* Copyright (c) 2023 Apereo Foundation
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

package org.sakaiproject.microsoft.controller;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.controller.auxiliar.AutoConfigSessionBean;
import org.sakaiproject.microsoft.controller.auxiliar.AutoConfigConfirmRequest;
import org.sakaiproject.microsoft.controller.auxiliar.AutoConfigRequest;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;


/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@Slf4j
@Controller
public class AutoConfigController {
	
	private static ResourceLoader rb = new ResourceLoader("Messages");
	
	@Autowired
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	@Autowired
	MicrosoftConfigurationService microsoftConfigurationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	
	@PostMapping(path = {"/autoConfig"})
	public String autoConfig(
			@RequestBody AutoConfigRequest requestBody,
			HttpServletRequest request,
			Model model
	) throws Exception {
		List<String> excluded_sites = Arrays.asList(
			"!admin",
			"citationsAdmin",
			"mercury"
		);
		
		HttpSession session = request.getSession();
		
		AutoConfigSessionBean autoConfigSessionBean = autoConfigSessionBean = (AutoConfigSessionBean)session.getAttribute("AutoConfigSessionBean");
		if(autoConfigSessionBean == null) {
			autoConfigSessionBean = new AutoConfigSessionBean();
			session.setAttribute("AutoConfigSessionBean", autoConfigSessionBean);
		}
		
		if(!autoConfigSessionBean.isRunning()) {
			
			//get all synchronizations
			List<SiteSynchronization> ssList = microsoftSynchronizationService.getAllSiteSynchronizations(false);
			
			//get (filtered) sites
			List<Site> sitesList = sakaiProxy.getSakaiSites(requestBody.getFilter());
			//map sites by id
			Map<String, Site> sitesMap = sitesList.stream().collect(Collectors.toMap(Site::getId, Function.identity()));
			
			//get teams - avoid cache, force get from Microsoft
			Map<String, MicrosoftTeam> teamsMap = microsoftCommonService.getTeams(true);
			
			//exclude sites and teams that are already synch
			ssList.stream().forEach(ss -> {
				sitesMap.remove(ss.getSiteId());
				teamsMap.remove(ss.getTeamId());
			});
			//also exclude all known sites
			excluded_sites.forEach(id -> sitesMap.remove(id));
			
			Map<String, Object> confirmMap = new HashMap<>();
			
			//for each not-used site
			int count_new = 0;
			int count_link = 0;
			List<String> removedSites = new ArrayList<>();
			for(String siteId : sitesMap.keySet()) {
				Site site = sitesMap.get(siteId);
				String title = site.getTitle();
				//if have specified a pattern -> apply it
				if(StringUtils.isNotBlank(requestBody.getTeamPattern())) {
					title = requestBody.getTeamPattern().replace("${siteTitle}", site.getTitle());
					//just to display it in the confirmation table
					site.setTitle(title);
				}
				final String finalTitle = title;
				
				//check if there is a, not-used Team that matched the Site 
				MicrosoftTeam team = teamsMap.values().stream().filter(t -> t.getName().equalsIgnoreCase(finalTitle)).findAny().orElse(null);
				
				//match found
				if(team != null) {
					//create relationship
					SiteSynchronization ss = SiteSynchronization.builder()
							.siteId(siteId)
							.teamId(team.getId())
							.forced(false)
							.build();
					count_link++;
					
					confirmMap.put(siteId, ss);
				//no match found --> create NEW Team if configuration allows it
				} else {
					if(requestBody.isNewTeam()){
						confirmMap.put(siteId, title);
						count_new++;
					} else {
						//not allowed to create NEW team -> remove site from list
						removedSites.add(siteId);
					}
				}
			}
			
			//remove unwanted sites
			if(removedSites.size() > 0) {
				for(String siteId : removedSites) {
					sitesMap.remove(siteId);
				}
			}
			
			//save in session bean
			autoConfigSessionBean.setSitesMap(sitesMap);
			autoConfigSessionBean.setConfirmMap(confirmMap);
			autoConfigSessionBean.setNewChannel(requestBody.isNewChannel());
			
			model.addAttribute("confirmMap", confirmMap);
			
			List<Site> auxList = new ArrayList<>(sitesMap.values());
			//sort elements by title
			Collections.sort(auxList, (s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
			model.addAttribute("sitesList", auxList);
			
			model.addAttribute("countSites", sitesMap.size());
			model.addAttribute("countTeams", teamsMap.size());
			model.addAttribute("countLink", count_link);
			model.addAttribute("countNew", count_new);
			
			long syncDuration = microsoftConfigurationService.getSyncDuration();
			model.addAttribute("syncDateFrom", ZonedDateTime.now().toLocalDate());
			model.addAttribute("syncDateTo", ZonedDateTime.now().plusMonths(syncDuration).toLocalDate());
		}
		
		return "fragments/autoConfig :: confirm";
	}
	
	@PostMapping(path = {"/autoConfig-confirm"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Boolean autoConfigConfirm(
			@RequestBody AutoConfigConfirmRequest payload,
			HttpServletRequest request,
			Model model
	) throws Exception {
		if(payload.getSiteIdList() == null || payload.getSiteIdList().size() == 0 || payload.getSyncDateFrom() == null || payload.getSyncDateTo() == null) {
			return false;
		}
		
		ZonedDateTime syncDateFrom = payload.getSyncDateFrom().atStartOfDay(sakaiProxy.getUserTimeZoneId());
		ZonedDateTime syncDateTo = payload.getSyncDateTo().atStartOfDay(sakaiProxy.getUserTimeZoneId()).plusHours(23).plusMinutes(59);
		
		HttpSession session = request.getSession();
		MicrosoftCredentials credentials = microsoftConfigurationService.getCredentials();
		new Thread(() -> {
			AutoConfigSessionBean autoConfigSessionBean_aux = null;
			synchronized (session) {
				autoConfigSessionBean_aux = (AutoConfigSessionBean)session.getAttribute("AutoConfigSessionBean");
				if(autoConfigSessionBean_aux == null) {
					autoConfigSessionBean_aux = new AutoConfigSessionBean();
					session.setAttribute("AutoConfigSessionBean", autoConfigSessionBean_aux);
				}
			}
			AutoConfigSessionBean autoConfigSessionBean = autoConfigSessionBean_aux;
			
			if(!autoConfigSessionBean.isRunning()) {
				//start running
				autoConfigSessionBean.startRunning(payload.getSiteIdList().size());
				
				Map<String, Object> map = autoConfigSessionBean.getConfirmMap();
				
				for(String siteId : payload.getSiteIdList()) {
					//get stored site from session bean
					Site site = autoConfigSessionBean.getSitesMap().get(siteId);
					if(site != null) {
						Object o = map.get(siteId);
						if(o != null) {
							if(o instanceof String) {
								try {
									//--> create NEW Team
									String teamId = microsoftCommonService.createTeam((String)o, credentials.getEmail());
									if(teamId != null) {
										//create relationship
										SiteSynchronization ss = SiteSynchronization.builder()
												.siteId(siteId)
												.teamId(teamId)
												.forced(false)
												.syncDateFrom(syncDateFrom)
												.syncDateTo(syncDateTo)
												.build();
						
										log.debug("saving NEW: siteId={}, teamId={}", siteId, teamId);
										microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
										
										//check if given site has groups and configuration allows it 
										if(autoConfigSessionBean.isNewChannel() && site.getGroups().size() > 0) {
											for(Group g : site.getGroups()) {
												try {
													//exclude automatic lesson groups
													if(g.getTitle().startsWith("Access:")) {
														continue;
													}
														
													//as Team is new, create all Channels
													String createdChannelId = microsoftCommonService.createChannel(teamId, g.getTitle(), credentials.getEmail());
													
													if(StringUtils.isNotBlank(createdChannelId)) {
														//create relationship
														GroupSynchronization gs = GroupSynchronization.builder()
																.siteSynchronization(ss)
																.groupId(g.getId())
																.channelId(createdChannelId)
																.build();
														
														log.debug("saving NEW: groupId={}, channelId={}, title={}", g.getId(), createdChannelId, g.getTitle());
														microsoftSynchronizationService.saveOrUpdateGroupSynchronization(gs);
													}
												}catch(Exception e) {
													log.error("Unexpected exception creating channel: {}", e.getMessage());
												}
											}
										}
										autoConfigSessionBean.increaseCounter();
									} else {
										//mark this site as error
										autoConfigSessionBean.addError(siteId, site.getTitle(), rb.getString("error.creating_team"));
									}
	
								} catch (MicrosoftCredentialsException e) {
									autoConfigSessionBean.addError(siteId, site.getTitle(), rb.getString(e.getMessage()));
								}
							//Team already exists and matches Site's title
							} else if(o instanceof SiteSynchronization) {
								SiteSynchronization ss = (SiteSynchronization)o;
								
								SiteSynchronization aux_ss = microsoftSynchronizationService.getSiteSynchronization(ss);
								//check if ss already exists (this should never happen)
								if(aux_ss != null) {
									//mark this site as error
									autoConfigSessionBean.addError(siteId, site.getTitle(), rb.getString("error.site_synchronization_already_exists"));
									continue;
								}
								
								//not forced -> check if exists some forced synchronization
								if(microsoftSynchronizationService.countSiteSynchronizationsByTeamId(ss.getTeamId(), true) > 0) {
									//mark this site as error
									autoConfigSessionBean.addError(siteId, site.getTitle(), rb.getString("error.site_synchronization_already_forced"));
									continue;
								}
								
								//set dates
								ss.setSyncDateFrom(syncDateFrom);
								ss.setSyncDateTo(syncDateTo);
								
								log.debug("saving site-team: siteId={}, teamId={}", siteId, ss.getTeamId());
								microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
								
								//check groups-channels
								try {
									if(site.getGroups().size() > 0) {
										//get existing channels from Team
										Map<String, MicrosoftChannel> channelsMap = microsoftCommonService.getTeamPrivateChannels(ss.getTeamId(), true);
										
										//get existing groups from site
										for(Group g : site.getGroups()) {
											//exclude automatic lesson groups
											if(g.getTitle().startsWith("Access:")) {
												continue;
											}
											
											//check if any group matches any channel
											MicrosoftChannel channel = channelsMap.values().stream().filter(c -> c.getName().equalsIgnoreCase(g.getTitle())).findAny().orElse(null);
											String channelId = (channel != null) ? channel.getId() : null;
											
											//match NOT found --> Create channel (if configuration allows it)
											if(channel == null && autoConfigSessionBean.isNewChannel()) {
												channelId = microsoftCommonService.createChannel(ss.getTeamId(), g.getTitle(), credentials.getEmail());
											}
											
											if(StringUtils.isNotBlank(channelId)) {
												//create relationship
												GroupSynchronization gs = GroupSynchronization.builder()
														.siteSynchronization(ss)
														.groupId(g.getId())
														.channelId(channelId)
														.build();
												
												//check if Group Synchronization does not exist
												GroupSynchronization aux_gs = microsoftSynchronizationService.getGroupSynchronization(gs);
												if(aux_gs == null) {
													log.debug("saving group-channel: groupId={}, channelId={}", g.getId(), channelId);
													microsoftSynchronizationService.saveOrUpdateGroupSynchronization(gs);
												}
											}
										}
									}
								} catch (MicrosoftCredentialsException e) {
									log.error("MicrosoftCredentialsException in confirm thread");
								}
								autoConfigSessionBean.increaseCounter();
							}
						}
					}
				}
				
				//end running
				if(autoConfigSessionBean.getCount() >= autoConfigSessionBean.getTotal()) {
					autoConfigSessionBean.finishRunning();
				}
			}
		}).start();
		
		return true;
	}
	
	@GetMapping(value = {"/autoConfig-status"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public AutoConfigSessionBean autoConfigStatus(Model model, HttpServletRequest request) throws Exception {
		HttpSession session = request.getSession();
		AutoConfigSessionBean autoConfigSessionBean = (AutoConfigSessionBean)session.getAttribute("AutoConfigSessionBean");
		
		return autoConfigSessionBean;
	}
}
