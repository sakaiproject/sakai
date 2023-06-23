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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.controller.auxiliar.AjaxResponse;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;


/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@Slf4j
@Controller
public class MainController {
	
	private static ResourceLoader rb = new ResourceLoader("Messages");
	
	@Autowired
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	@Autowired
	MicrosoftConfigurationService microsoftConfigurationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	private static final String INDEX_TEMPLATE = "index";
	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String ROW_SITE_SYNCH_FRAGMENT = "fragments/synchronizationRow :: siteRow";
	private static final String LIST_GROUP_SYNCH_FRAGMENT = "fragments/synchronizationRow :: groupRows";

	private static final Integer DEFAULT_PAGE_SIZE = 50;
	
	@GetMapping(value = {"/", "/index"})
	public String index(
			@RequestParam(defaultValue = "status") String sortBy,
			@RequestParam(defaultValue = "ASC") String sortOrder,
			@RequestParam(defaultValue = "0") Integer pageNum,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String search,
			Model model
	)  throws MicrosoftGenericException {		
		if(pageSize == null || pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		
		List<SiteSynchronization> list = microsoftSynchronizationService.getAllSiteSynchronizations(true);
		Map<String, MicrosoftTeam> map = microsoftCommonService.getTeams();
		
		//filter elements
		if(StringUtils.isNotBlank(search)) {
			String lcSearch = search.toLowerCase();
			list = list.stream()
					.filter(ss -> ss.getSiteId().contains(lcSearch) ||
								  ss.getTeamId().contains(lcSearch) ||
								  ss.getSite().getTitle().toLowerCase().contains(lcSearch) ||
								  map.get(ss.getTeamId()).getName().toLowerCase().contains(lcSearch))
					.collect(Collectors.toList());
		}

		//sort elements
		if(StringUtils.isNotBlank(sortBy)) {
			Collections.sort(list, (i1, i2) -> {
				if("DESC".equals(sortOrder)) {
					SiteSynchronization aux = i1;
					i1 = i2;
					i2 = aux;
				}
				switch(sortBy) {
					case "siteId":
						return i1.getSiteId().compareTo(i2.getSiteId());
					case "teamId":
						return i1.getTeamId().compareTo(i2.getTeamId());
					case "teamTitle":
						return map.get(i1.getTeamId()).getName().compareToIgnoreCase(map.get(i2.getTeamId()).getName());
					case "siteTitle":
						return i1.getSite().getTitle().compareToIgnoreCase(i2.getSite().getTitle());
					case "syncDateFrom":
						return i1.getSyncDateFrom().compareTo(i2.getSyncDateFrom());
					case "syncDateTo":
						return i1.getSyncDateTo().compareTo(i2.getSyncDateTo());
					case "status":
					default:
						return i1.getStatus().getCode().compareTo(i2.getStatus().getCode());
						
				}
			});
			
		}
		
		int totalPages = (list.size() + pageSize - 1) / pageSize;
		//int totalPages = (list.size() > pageSize) ? (int)Math.ceil((double)list.size()/pageSize) : 0;
		int maxPage = Math.max(0, totalPages - 1);
		pageNum = Math.max(0, Math.min(maxPage, pageNum));
		
		model.addAttribute("totalElements", list.size());
		
		//limit number of elements
		long skipCount = (long)(pageNum * pageSize);
		list = list.stream()
			.skip(skipCount)
			.limit(pageSize)
			.collect(Collectors.toList());
		
		model.addAttribute("siteSynchronizations", list);
		model.addAttribute("teamsMap", map);
		
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("sortOrder", sortOrder);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("maxPage", maxPage);
		model.addAttribute("search", search);

		return INDEX_TEMPLATE;
	}
	
	//called by AJAX
	@GetMapping(value = {"/listGroupSynchronizations/{siteSynchronizationId}"})
	public String index(@PathVariable String siteSynchronizationId, Model model)  throws MicrosoftGenericException {
		log.debug("List group synchronizations for siteSynchronizationId={}", siteSynchronizationId);
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(siteSynchronizationId).build(), true);
		if(ss != null) {
			model.addAttribute("groupsMap", ss.getSite().getGroups().stream().collect(Collectors.toMap(Group::getId, Function.identity())));
			model.addAttribute("channelsMap", microsoftCommonService.getTeamPrivateChannels(ss.getTeamId()));
			model.addAttribute("siteRow", ss);
		}

		return LIST_GROUP_SYNCH_FRAGMENT;
	}
	
	@GetMapping(value = {"/resetGraphClient"})
	public String resetGraphClient() throws Exception {
		log.debug("Reset GraphClient");
		
		microsoftCommonService.resetGraphClient();

		return REDIRECT_INDEX;
	}
	
	@GetMapping(value = {"/resetCaches"})
	public String resetCaches() throws Exception {
		log.debug("Reset Caches");
		
		microsoftCommonService.resetCache();

		return REDIRECT_INDEX;
	}
	
	//called by AJAX - returns FRAGMENT
	@GetMapping(value = {"/runSiteSynchronization/{id}"})
	public String runSiteSynchronization(@PathVariable String id, Model model) throws Exception {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(id).build(), true);
		if(ss != null) {
			microsoftSynchronizationService.runSiteSynchronization(ss);

			model.addAttribute("row", ss);
			model.addAttribute("teamsMap", microsoftCommonService.getTeams());
		}
		return ROW_SITE_SYNCH_FRAGMENT;
	}
	
	//called by AJAX - returns JSON
	@GetMapping(path = {"/setForced-siteSynchronization/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public AjaxResponse updateSiteSynchronizationForced(@PathVariable String id, @RequestParam Boolean forced,  Model model) {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(id).build());
		AjaxResponse ret = new AjaxResponse();
		ret.setStatus(false);
		ret.setError(rb.getString("error.set_forced_synchronization"));
		if(ss != null) {
			//exclude current synch ==> "> 1"
			if(forced && microsoftSynchronizationService.countSiteSynchronizationsByTeamId(ss.getTeamId(), false) > 1) {
				ret.setError(rb.getString("error.site_synchronization_impossible_forced"));
				return ret;
			}
			
			ss.setForced(forced);
			
			microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
			ret.setStatus(true);
			ret.setError("");
		}

		return ret;
	}
	
	//called by AJAX - returns JSON
	@GetMapping(path = {"/setDate-siteSynchronization/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public AjaxResponse updateSiteSynchronizationDate(
			@PathVariable String id,
			@RequestParam String name,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, 
			Model model
	) {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(id).build());
		AjaxResponse ret = new AjaxResponse();
		ret.setStatus(false);
		ret.setError(rb.getString("error.set_dates"));
		
		if(ss != null) {
			try {
				switch(name) {
					case "from":
						ss.setSyncDateFrom(date.atStartOfDay(sakaiProxy.getUserTimeZoneId()));
						break;
					case "to":
						ss.setSyncDateTo(date.atStartOfDay(sakaiProxy.getUserTimeZoneId()).plusHours(23).plusMinutes(59));
						break;
					default:
						ret.setError(rb.getString("error.dates"));
						return ret;
				}
				
				//validate dates
				if(ss.getSyncDateFrom().isAfter(ss.getSyncDateTo())) {
					ret.setError(rb.getString("error.dates_order"));
					return ret;
				}
			
				microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
				ret.setStatus(true);
				ret.setError("");
				//run button is enabled based on current date
				ret.setBody(Boolean.toString(ss.onDate()));
			} catch(Exception e) {
				ret.setError(rb.getString("error.dates"));
			}
		}

		return ret;
	}
	
	@PostMapping(path = {"/update-siteSynchronizations"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String updateSiteSynchronizations(
			@RequestParam(required = false) List<String> selectedIds,
			@RequestParam String action,
			Model model,
			RedirectAttributes redirectAttributes
	) throws MicrosoftCredentialsException {
		if(selectedIds != null && selectedIds.size() > 0) {
			switch(action) {
				case "delete":
					int count = microsoftSynchronizationService.deleteSiteSynchronizations(selectedIds);
					
					if(count != selectedIds.size()) {
						redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.deleting_site_synchronizations"));
					}
				break;
				
				case "clean":
					boolean ok = true;
					for(String id : selectedIds) {
						SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(id).build(), true);
						if(ss != null) {
							//remove all users from team
							ok = ok && microsoftSynchronizationService.removeUsersFromSynchronization(ss);
						}
					}
					if(!ok) {
						redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.cleaning_team"));
					}
				break;
				
				default:
				break;
			}
		}
		return REDIRECT_INDEX;
	}
	
	//called by AJAX - returns FRAGMENT
	//now, this end-point is never called by the GUI. Maybe we can remove this in the future
	@GetMapping(value = {"/checkSiteSynchronizationStatus/{id}"})
	public String checkSiteSynchronizationStatus(@PathVariable String id, Model model) throws Exception {
		SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().id(id).build(), true);
		if(ss != null) {
			microsoftSynchronizationService.checkStatus(ss);
			
			model.addAttribute("row", ss);
			model.addAttribute("teamsMap", microsoftCommonService.getTeams());
		}
		return ROW_SITE_SYNCH_FRAGMENT;
	}
}
