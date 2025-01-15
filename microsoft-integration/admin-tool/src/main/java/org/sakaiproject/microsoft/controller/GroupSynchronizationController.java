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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.controller.auxiliar.GroupSynchronizationRequest;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.sakaiproject.microsoft.api.MicrosoftCommonService.MAX_CHANNELS;


/**
 * GroupSynchronizationController
 * 
 * This is the controller used by Spring MVC to handle Group Synchronizations related requests
 * 
 */
@Slf4j
@Controller
public class GroupSynchronizationController {
	
	private static ResourceLoader rb = new ResourceLoader("Messages");
	
	@Autowired
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	@Autowired
	MicrosoftConfigurationService microsoftConfigurationService;

	@Autowired
	SakaiProxy sakaiProxy;

	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String REDIRECT_EDIT_GROUP_SYNCH = "redirect:/editGroupSynchronization";
	private static final String EDIT_GROUP_SYNCH_TEMPLATE = "editGroupSynchronization";

	private static final String NEW = "NEW";

	@GetMapping(value = {"/editGroupSynchronization/{siteSynchronizationId}"})
	public String editGroupSynchronization(@PathVariable String siteSynchronizationId, Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(siteSynchronizationId).build(), true);

		if (ss == null) {
			redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.site_synchronization_not_found"));
			return REDIRECT_INDEX;
		}

		List<Group> groups = (List<Group>) sakaiProxy.getSite(ss.getSiteId()).getGroups();

		model.addAttribute("siteSynchronizationId", siteSynchronizationId);
		model.addAttribute("groupsMap", groups.stream().collect(Collectors.toMap(Group::getId, Function.identity())));
		model.addAttribute("channelsMap", microsoftCommonService.getTeamPrivateChannels(ss.getTeamId()));
		model.addAttribute("siteTitle", ss.getSite().getTitle());
		model.addAttribute("teamTitle", microsoftCommonService.getTeam(ss.getTeamId()).getName());

		List<GroupSynchronization> list = microsoftSynchronizationService.getAllGroupSynchronizationsBySiteSynchronizationId(siteSynchronizationId);
		groups.stream()
				.filter(g -> list.stream().noneMatch(item -> item.getGroupId().equals(g.getId())))
				.map(g -> GroupSynchronization.builder()
						.groupId(g.getId())
						.channelId("")
						.siteSynchronization(ss)
						.build())
				.forEach(list::add);

		List<GroupSynchronization> sortedList = new ArrayList<>();
		groups.forEach(
				g -> sortedList.addAll(
						list.stream().filter(element -> element.getGroupId().equals(g.getId())).collect(Collectors.toList())
				)
		);

		if (list.size() > 0) {
			model.addAttribute("groupSynchronizations", sortedList);
		}

		return EDIT_GROUP_SYNCH_TEMPLATE;
	}

	@PostMapping(path = {"/add-groupSynchronization/{siteSynchronizationId}"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String saveGroupSynchronization(@PathVariable String siteSynchronizationId, @ModelAttribute GroupSynchronizationRequest payload, Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(siteSynchronizationId).build());
		if (ss != null) {
			Map<String, MicrosoftChannel> channelsMap = microsoftCommonService.getTeamPrivateChannels(ss.getTeamId());
			model.addAttribute("channelsMap", channelsMap);

			if (StringUtils.isNotBlank(payload.getSelectedGroupId()) && StringUtils.isNotBlank(payload.getSelectedChannelId())) {
				String groupId = payload.getSelectedGroupId();
				String channelId = payload.getSelectedChannelId();

				GroupSynchronization gs = GroupSynchronization.builder()
						.siteSynchronization(ss)
						.groupId(groupId)
						.channelId(channelId)
						.build();

				GroupSynchronization aux_gs = microsoftSynchronizationService.getGroupSynchronization(gs);
				if (aux_gs != null) {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.group_synchronization_already_exists"));

					return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
				}

				//check if parent is forced and selected channel is duplicated
				//TODO: at this point, if parent is not forcing, we will allow this relationship. But we don't check if the parent starts forcing after that
				if (ss.isForced() && microsoftSynchronizationService.countGroupSynchronizationsByChannelId(gs.getChannelId()) > 0) {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.group_synchronization_already_forced"));

					return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
				}

				log.debug("saving: groupId={}, channelId={}", groupId, channelId);
				microsoftSynchronizationService.saveOrUpdateGroupSynchronization(gs);
			}
		} else {
			redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.channel_number_more_than_30"));
		}

		return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
	}


	@GetMapping(path = {"/delete-groupSynchronization/{groupSynchronizationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Boolean deleteGroupSynchronization(@PathVariable String groupSynchronizationId, Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		boolean ok = false;
		GroupSynchronization gs = microsoftSynchronizationService.getGroupSynchronization(GroupSynchronization.builder().id(groupSynchronizationId).build());
		if (gs != null) {
			ok = microsoftSynchronizationService.deleteGroupSynchronization(groupSynchronizationId);
		}

		return ok;
	}

	@PostMapping(value = {"/channel"})
	public String createNewChannel(@RequestParam String siteId, @RequestParam String name, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		log.debug("NEW channel creating");
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(siteId).build());
		try {
			Map<String, MicrosoftChannel> channelsMap = microsoftCommonService.getTeamPrivateChannels(ss.getTeamId());
			Collection<MicrosoftChannel> channels = channelsMap.values();
			boolean channelExists = channels.stream()
					.anyMatch(channel -> channel.getName().equalsIgnoreCase(name));
			if (!channelExists) {
				if (channels.size() < MAX_CHANNELS) {
					microsoftCommonService.createChannel(ss.getTeamId(), name, microsoftConfigurationService.getCredentials().getEmail());
				} else {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.channel_number_more_than_30"));
				}
			} else {
				redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.new_channel_with_same_name"));
			}
		} catch (NullPointerException e) {
			log.error("MicrosoftCredentialsException in confirm thread");
		}
		return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteId;
	}
}
