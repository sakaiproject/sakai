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

import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.controller.auxiliar.SiteSynchronizationRequest;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class SiteSynchronizationController {
	
	private static ResourceLoader rb = new ResourceLoader("Messages");
	
	@Autowired
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	@Autowired
	MicrosoftConfigurationService microsoftConfigurationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String NEW_SITE_SYNCH_TEMPLATE = "newSiteSynchronization";
	private static final String REDIRECT_NEW_SITE_SYNCH_TEMPLATE = "redirect:/newSiteSynchronization";

	private static final String NEW = "NEW";
	
	@GetMapping(value = {"/newSiteSynchronization"})
	public String newSiteSynchronization(Model model) throws MicrosoftGenericException {
		log.debug("NEW site synchronization");

		model.addAttribute("sitesList", sakaiProxy.getSakaiSites());
		model.addAttribute("teamsMap", microsoftCommonService.getTeams());

		return NEW_SITE_SYNCH_TEMPLATE;
	}
	

	@PostMapping(value = {"/fiterSites"})
	public String fiterSites(@RequestBody SakaiSiteFilter filter, Model model) throws MicrosoftGenericException {
		log.debug("Filter Sites");
		model.addAttribute("sitesList", sakaiProxy.getSakaiSites(filter));

		return NEW_SITE_SYNCH_TEMPLATE + " :: sites";
		
	}
	
	@GetMapping(value = {"/refreshTeams"})
	public String refreshTeams(Model model) throws MicrosoftGenericException {
		log.debug("Refresh teams");
		model.addAttribute("teamsMap", microsoftCommonService.getTeams(true));

		return NEW_SITE_SYNCH_TEMPLATE + " :: teams";
	}
	
	@PostMapping(path = {"/save-siteSynchronization"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String saveSiteSynchronization(@ModelAttribute SiteSynchronizationRequest payload,  Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		//case NEW
		if(payload.getSelectedSiteIds().isEmpty()) {
			log.debug("ERROR: No site selected");
			redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.no_site_selected"));
			
			return REDIRECT_NEW_SITE_SYNCH_TEMPLATE;
		}
		
		if(payload.getSelectedTeamIds().isEmpty()) {
			log.debug("ERROR: No team selected");
			redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.no_team_selected"));
			
			return REDIRECT_NEW_SITE_SYNCH_TEMPLATE;
		}
		
		String teamCreatedId = null;
		for(String siteId: payload.getSelectedSiteIds()) {
			for(String teamId: payload.getSelectedTeamIds()) {
				if(teamId.equals(NEW) && teamCreatedId == null) {
					teamCreatedId = microsoftCommonService.createTeam(payload.getNewTeamName(), microsoftConfigurationService.getCredentials().getEmail());
					if(teamCreatedId == null) {
						redirectAttributes.addFlashAttribute("exception_error", MessageFormat.format(rb.getString("error.creating_team_param"), payload.getNewTeamName()));
						continue;
					}
				}
				
				SiteSynchronization ss = SiteSynchronization.builder()
					.siteId(siteId)
					.teamId(teamId.equals(NEW) ? teamCreatedId : teamId)
					.forced(payload.isForced())
				.build();
				
				SiteSynchronization aux_ss = microsoftSynchronizationService.getSiteSynchronization(ss);
				if(aux_ss != null) {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.site_synchronization_already_exists"));
					
					continue;
				}
				
				//if not forced -> check if exists some forced synchronization
				//if forced -> check if exists any
				if(microsoftSynchronizationService.countSiteSynchronizationsByTeamId(ss.getTeamId(), !ss.isForced()) > 0) {
					redirectAttributes.addFlashAttribute("exception_error", rb.getString(ss.isForced() ? "error.site_synchronization_impossible_forced" : "error.site_synchronization_already_forced"));
					
					continue;
				}
				
				log.debug("saving: siteId={}, teamId={}", siteId, teamId);
				microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
			}
		}

		return REDIRECT_INDEX;
	}
}
