/**
 * Copyright (c) 2024 The Apereo Foundation
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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftLoggingService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.AutoConfigProcessStatus;
import org.sakaiproject.microsoft.api.data.CreationStatus;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.model.MicrosoftLog;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftLoggingRepository;
import org.sakaiproject.microsoft.controller.auxiliar.AutoConfigConfirmRequest;
import org.sakaiproject.microsoft.controller.auxiliar.AutoConfigRequest;
import org.sakaiproject.microsoft.controller.auxiliar.AutoConfigSessionBean;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.sakaiproject.microsoft.api.MicrosoftCommonService.MAX_ADD_CHANNELS;
import static org.sakaiproject.microsoft.api.MicrosoftCommonService.MAX_CHANNELS;


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
	@Setter
	private MicrosoftLoggingRepository microsoftLoggingRepository;
	@Autowired
	private MicrosoftLoggingService microsoftLoggingService;
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
				final String finalTitle = microsoftCommonService.processMicrosoftTeamName(title);

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
			Model model) throws Exception {

		if (CollectionUtils.isEmpty(payload.getSiteIdList()) ||
				payload.getSyncDateFrom() == null || payload.getSyncDateTo() == null) {
			return false;
		}
		
		ZonedDateTime syncDateFrom = payload.getSyncDateFrom().atStartOfDay(sakaiProxy.getUserTimeZoneId());
		ZonedDateTime syncDateTo = payload.getSyncDateTo().atStartOfDay(sakaiProxy.getUserTimeZoneId()).plusHours(23).plusMinutes(59);
		
		HttpSession session = request.getSession();
		MicrosoftCredentials credentials = microsoftConfigurationService.getCredentials();

		new Thread(() -> {
			AutoConfigSessionBean autoConfigSessionBean;
			synchronized (session) {
				autoConfigSessionBean = (AutoConfigSessionBean) session.getAttribute("AutoConfigSessionBean");
				if (autoConfigSessionBean == null) {
					autoConfigSessionBean = new AutoConfigSessionBean();
					session.setAttribute("AutoConfigSessionBean", autoConfigSessionBean);
				}
			}

			if (!autoConfigSessionBean.isRunning()) {
				autoConfigSessionBean.startRunning(payload.getSiteIdList().size());
				autoConfigSessionBean.addStatus(AutoConfigProcessStatus.START_RUNNING, "");

				Map<String, Object> map = autoConfigSessionBean.getConfirmMap();

				for (String siteId : payload.getSiteIdList()) {
					Site site = autoConfigSessionBean.getSitesMap().get(siteId);
					if (site == null) {
						continue;
					}

					Object o = map.get(siteId);
					if (o == null) {
						continue;
					}

					try {
						if (o instanceof String) {
							autoConfigSessionBean.addStatus(AutoConfigProcessStatus.CREATING_TEAM, site.getTitle());
							handleNewTeamCreation(autoConfigSessionBean, site, (String) o, syncDateFrom, syncDateTo, credentials);
						} else if (o instanceof SiteSynchronization) {
							autoConfigSessionBean.addStatus(AutoConfigProcessStatus.BINDING_TEAM, site.getTitle());
							handleExistingTeamBinding(autoConfigSessionBean, site, (SiteSynchronization) o, syncDateFrom, syncDateTo, credentials);
						}
					} catch (Exception e) {
						log.error("Error handling site " + siteId, e);
						autoConfigSessionBean.addError(siteId, site.getTitle(), e.getMessage());
					} finally {
						autoConfigSessionBean.increaseCounter();
					}
				}

				if (autoConfigSessionBean.getCount() >= autoConfigSessionBean.getTotal()) {
					autoConfigSessionBean.addStatus(AutoConfigProcessStatus.END_RUNNING, "");
					autoConfigSessionBean.finishRunning();
				}
			}
		}).start();
		
		return true;
	}


	public void handleNewTeamCreation(AutoConfigSessionBean autoConfigSessionBean, Site site, String teamName, ZonedDateTime syncDateFrom, ZonedDateTime syncDateTo, MicrosoftCredentials credentials) throws Exception {
		String teamId = microsoftCommonService.createTeam(teamName, credentials.getEmail());
		SiteSynchronization ss = SiteSynchronization.builder()
				.siteId(site.getId())
				.teamId(teamId != null ? teamId : "")
				.forced(false)
				.syncDateFrom(syncDateFrom)
				.syncDateTo(syncDateTo)
				.creationStatus(CreationStatus.OK)
				.build();

		if (teamId == null) {
			ss.setStatus(SynchronizationStatus.NOT_AVAILABLE);
			ss.setCreationStatus(CreationStatus.KO);

			autoConfigSessionBean.addError(site.getId(), site.getTitle(), rb.getString("error.creating_team"));
			microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
			microsoftLoggingService.saveLog(MicrosoftLog.builder()
					.event(MicrosoftLog.ERROR_TEAM_ID_NULL)
					.status(MicrosoftLog.Status.KO)
					.addData("teamId", teamId)
					.addData("siteId", site.getId())
					.addData("siteTitle", site.getTitle())
					.addData("teamTitle", teamName)
					.build());
			return;
		}

		microsoftLoggingService.saveLog(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_CREATE_TEAM_FROM_SITE)
				.status(MicrosoftLog.Status.OK)
				.addData("siteId", site.getId())
				.addData("siteTitle", site.getTitle())
				.addData("teamId", teamId)
				.addData("teamTitle", teamName)
				.build());

		boolean limitExceeded = site.getGroups().size() > MAX_CHANNELS;
		List<Group> groupsToProcess = limitGroups(site.getGroups().stream().filter(g -> !g.getTitle().startsWith("Access:")).collect(Collectors.toList()));

		if (limitExceeded) {
			ss.setCreationStatus(CreationStatus.PARTIAL_OK);
		}

		microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);

		List<MicrosoftChannel> channels = microsoftCommonService.createChannels(groupsToProcess, teamId, credentials.getEmail());

		for (Group g : groupsToProcess) {
			Optional<MicrosoftChannel> channelOpt = channels.stream()
					.filter(c -> c.getName().equalsIgnoreCase(microsoftCommonService.processMicrosoftChannelName(g.getTitle()))).findFirst();

			channelOpt.ifPresent(channel -> {
				GroupSynchronization gs = GroupSynchronization.builder()
						.siteSynchronization(ss)
						.groupId(g.getId())
						.channelId(channel.getId())
						.build();
				microsoftSynchronizationService.saveOrUpdateGroupSynchronization(gs);
			});
		}
		String groupsIds = groupsToProcess.stream()
				.map(group -> group.getId().toString())
				.collect(Collectors.joining(","));

		String groupsNames = groupsToProcess.stream()
				.map(group -> group.getTitle().toString())
				.collect(Collectors.joining(","));

		String channelIds = channels.stream()
				.map(channel -> channel.getId().toString())
				.collect(Collectors.joining(","));

		String channelNames = channels.stream()
				.map(channel -> channel.getName().toString())
				.collect(Collectors.joining(","));

		microsoftLoggingService.saveLog(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_CHANNEL_PRESENT_ON_GROUP)
				.status(MicrosoftLog.Status.OK)
				.addData("siteId", site.getId())
				.addData("siteTitle", site.getTitle())
				.addData("processGroupsIds", groupsIds)
				.addData("processGroupsNames", groupsNames)
				.addData("numberGroupsOnSite", String.valueOf(site.getGroups().size()))
				.addData("numberLimitedGroups", String.valueOf(groupsToProcess.size()))
				.addData("numberNonProcessedGroups", String.valueOf(site.getGroups().size() - groupsToProcess.size()))
				.addData("teamId", teamId)
				.addData("teamTitle", teamName)
				.addData("createChannelsId", channelIds)
				.addData("createChannelsName", channelNames)
				.build());

	}

	public void handleExistingTeamBinding(AutoConfigSessionBean autoConfigSessionBean, Site site, SiteSynchronization ss, ZonedDateTime syncDateFrom, ZonedDateTime syncDateTo, MicrosoftCredentials credentials) throws Exception {

		microsoftLoggingService.saveLog(MicrosoftLog.builder()
				.event(MicrosoftLog.BINDING_TEAM_FROM_SITE)
				.status(!site.getId().isBlank() ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("siteId", site.getId())
				.addData("siteTitle", site.getTitle())
				.addData("teamId", ss.getTeamId())
				.build());

		boolean limitExceeded = site.getGroups().size() > MAX_CHANNELS;
		ss.setSyncDateFrom(syncDateFrom);
		ss.setSyncDateTo(syncDateTo);
		ss.setCreationStatus(limitExceeded ? CreationStatus.PARTIAL_OK : CreationStatus.OK);

		microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);

		Map<String, MicrosoftChannel> channelsMap = microsoftCommonService.getTeamPrivateChannels(ss.getTeamId(), true);
		List<Group> groupsToProcess = limitGroups(site.getGroups().stream().filter(g -> !g.getTitle().startsWith("Access:")).collect(Collectors.toList()));

		List<Group> nonExistingGroups = groupsToProcess.stream()
				.filter(g -> channelsMap.values().stream().noneMatch(c -> c.getName().equalsIgnoreCase(microsoftCommonService.processMicrosoftChannelName(g.getTitle()))))
				.collect(Collectors.toList());

		if (nonExistingGroups.size() > 0 && autoConfigSessionBean.isNewChannel()) {
			List<MicrosoftChannel> channels = microsoftCommonService.createChannels(nonExistingGroups, ss.getTeamId(), credentials.getEmail());
			channels.forEach(c -> channelsMap.put(c.getId(), c));
		}

		for (Group g : groupsToProcess) {
			MicrosoftChannel channel = channelsMap.values().stream()
					.filter(c -> c.getName().equalsIgnoreCase(microsoftCommonService.processMicrosoftChannelName(g.getTitle())))
					.findAny()
					.orElse(null);

			String channelId = (channel != null) ? channel.getId() : null;

			if (channel == null && autoConfigSessionBean.isNewChannel()) {
				channelId = microsoftCommonService.createChannel(ss.getTeamId(), g.getTitle(), credentials.getEmail());
			}

			if (StringUtils.isNotBlank(channelId)) {
				GroupSynchronization gs = GroupSynchronization.builder()
						.siteSynchronization(ss)
						.groupId(g.getId())
						.channelId(channelId)
						.build();

				if (microsoftSynchronizationService.getGroupSynchronization(gs) == null) {
					microsoftSynchronizationService.saveOrUpdateGroupSynchronization(gs);
				}
			}
		}
		String groupsIds = groupsToProcess.stream()
				.map(group -> group.getId().toString())
				.collect(Collectors.joining(","));

		String groupsNames = groupsToProcess.stream()
				.map(group -> group.getTitle().toString())
				.collect(Collectors.joining(","));

		String channelIds = channelsMap.values().stream()
				.map(channel -> channel.getId().toString())
				.collect(Collectors.joining(","));

		String channelNames = channelsMap.values().stream()
				.map(channel -> channel.getName().toString())
				.collect(Collectors.joining(","));

		microsoftLoggingService.saveLog(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_CHANNEL_PRESENT_ON_GROUP)
				.status(MicrosoftLog.Status.OK)
				.addData("siteId", site.getId())
				.addData("siteTitle", site.getTitle())
				.addData("processGroupsIds", groupsIds)
				.addData("processGroupsNames", groupsNames)
				.addData("numberGroupsOnSite", String.valueOf(site.getGroups().size()))
				.addData("numberLimitedGroups", String.valueOf(groupsToProcess.size()))
				.addData("numberNonProcessedGroups", String.valueOf(site.getGroups().size() - groupsToProcess.size()))
				.addData("teamId", ss.getTeamId())
				.addData("createChannelsId", channelIds)
				.addData("createChannelsName", channelNames)
				.build());
	}

	private List<Group> limitGroups(Collection<Group> groups) {
		return groups.size() > MAX_CHANNELS ?
				groups.stream().limit(MAX_ADD_CHANNELS).collect(Collectors.toList()) :
				new ArrayList<>(groups);
	}


	@GetMapping(value = {"/autoConfig-status"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public AutoConfigSessionBean autoConfigStatus(Model model, HttpServletRequest request) throws Exception {
		HttpSession session = request.getSession();
		AutoConfigSessionBean autoConfigSessionBean = (AutoConfigSessionBean) session.getAttribute("AutoConfigSessionBean");

		return autoConfigSessionBean;
	}
}
