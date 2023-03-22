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

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;


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
	
	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String REDIRECT_EDIT_GROUP_SYNCH = "redirect:/editGroupSynchronization";
	private static final String EDIT_GROUP_SYNCH_TEMPLATE = "editGroupSynchronization";

	private static final String NEW = "NEW";
	
	@GetMapping(value = {"/editGroupSynchronization/{siteSynchronizationId}"})
	public String editGroupSynchronization(@PathVariable String siteSynchronizationId, Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(siteSynchronizationId).build(), true);
		if(ss != null) {
			model.addAttribute("siteSynchronizationId", siteSynchronizationId);
			model.addAttribute("groupsMap", ss.getSite().getGroups().stream().collect(Collectors.toMap(Group::getId, Function.identity())));
			model.addAttribute("channelsMap", microsoftCommonService.getTeamPrivateChannels(ss.getTeamId()));
			model.addAttribute("siteTitle", ss.getSite().getTitle());
			model.addAttribute("teamTitle", microsoftCommonService.getTeam(ss.getTeamId()).getName());
			
			List<GroupSynchronization> list = microsoftSynchronizationService.getAllGroupSynchronizationsBySiteSynchronizationId(siteSynchronizationId);
			if(list != null && list.size() > 0) {
				model.addAttribute("groupSynchronizations", list);
			}
			
	
			return EDIT_GROUP_SYNCH_TEMPLATE;
		}
		
		redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.site_synchronization_not_found"));
		return REDIRECT_INDEX;
	}
	
	@PostMapping(path = {"/add-groupSynchronization/{siteSynchronizationId}"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String saveGroupSynchronization(@PathVariable String siteSynchronizationId, @ModelAttribute GroupSynchronizationRequest payload,  Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		String createdChannelId = null;
		//get parent object
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(siteSynchronizationId).build());
		if(ss != null) {
			if(StringUtils.isNotBlank(payload.getSelectedGroupId()) && StringUtils.isNotBlank(payload.getSelectedChannelId())) {
				String groupId = payload.getSelectedGroupId();
				String channelId = payload.getSelectedChannelId();
				
				//TODO: do the same to create a site???
				if(channelId.equals(NEW) && createdChannelId == null) {
					//create new channel
					if(StringUtils.isBlank(payload.getNewChannelName())) {
						redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.new_channel_empty"));
						
						return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
					}
					createdChannelId = microsoftCommonService.createChannel(ss.getTeamId(), payload.getNewChannelName(), microsoftConfigurationService.getCredentials().getEmail());
	
					if(createdChannelId == null) {
						redirectAttributes.addFlashAttribute("exception_error", MessageFormat.format(rb.getString("error.creating_channel"), payload.getNewChannelName()));
						
						return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
					}
				}
				
				GroupSynchronization gs = GroupSynchronization.builder()
					.siteSynchronization(ss)
					.groupId(groupId)
					.channelId(channelId.equals(NEW) ? createdChannelId : channelId)
				.build();
				
				GroupSynchronization aux_gs = microsoftSynchronizationService.getGroupSynchronization(gs);
				if(aux_gs != null) {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.group_synchronization_already_exists"));
					
					return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
				}
				
				//check if parent is forced and selected channel is duplicated
				//TODO: at this point, if parent is not forcing, we will allow this relationship. But we don't check if the parent starts forcing after that
				if(ss.isForced() && microsoftSynchronizationService.countGroupSynchronizationsByChannelId(gs.getChannelId()) > 0) {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.group_synchronization_already_forced"));
					
					return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
				}
				
				log.debug("saving: groupId={}, channelId={}", groupId, channelId.equals(NEW) ? createdChannelId : channelId);
				microsoftSynchronizationService.saveOrUpdateGroupSynchronization(gs);
			}
		} else {
			redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.site_synchronization_not_found"));
		}
		
		return REDIRECT_EDIT_GROUP_SYNCH + "/" + siteSynchronizationId;
	}
	
	
	@GetMapping(path = {"/delete-groupSynchronization/{groupSynchronizationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Boolean deleteGroupSynchronization(@PathVariable String groupSynchronizationId, Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		boolean ok = false;
		GroupSynchronization gs = microsoftSynchronizationService.getGroupSynchronization(GroupSynchronization.builder().id(groupSynchronizationId).build());
		if(gs != null) {
			ok = microsoftSynchronizationService.deleteGroupSynchronization(groupSynchronizationId);
		}
		
		return ok;
	}
}
