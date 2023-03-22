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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.model.MicrosoftConfigItem;
import org.sakaiproject.microsoft.controller.auxiliar.ConfigRequest;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;


/**
 * ConfigController
 * 
 * This is the controller used by Spring MVC to handle config related requests
 * 
 */
@Slf4j
@Controller
public class ConfigController {
	
	private static ResourceLoader rb = new ResourceLoader("Messages");
	
	@Autowired
	private MicrosoftConfigurationService microsoftConfigurationService;
	
	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String CONFIG_TEMPLATE = "config";
	
	@GetMapping(value = {"/config"})
	public String showConfig(Model model) throws Exception {
		List<MicrosoftConfigItem> synchConfigItems = new ArrayList<>(microsoftConfigurationService.getAllSynchronizationConfigItems().values());

		//sort elements by index
		Collections.sort(synchConfigItems, (i1, i2) -> i1.getIndex().compareTo(i2.getIndex()));
		model.addAttribute("mapped_sakai_user_id", microsoftConfigurationService.getMappedSakaiUserId());
		model.addAttribute("mapped_microsoft_user_id", microsoftConfigurationService.getMappedMicrosoftUserId());
		model.addAttribute("synch_config_items", synchConfigItems);
		model.addAttribute("siteFilter", microsoftConfigurationService.getNewSiteFilter());
		
		return CONFIG_TEMPLATE;
	}
	
	@PostMapping(path = {"/save-config"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String saveConfig(@ModelAttribute ConfigRequest payload, Model model) {
		Map<String, MicrosoftConfigItem> default_items = microsoftConfigurationService.getDefaultSynchronizationConfigItems();
		payload.getSynch_config_items().stream().forEach(item -> default_items.get(item).setValue(Boolean.TRUE.toString()));
		microsoftConfigurationService.saveConfigItems(new ArrayList<MicrosoftConfigItem>(default_items.values()));
		
		microsoftConfigurationService.saveMappedSakaiUserId(payload.getMapped_sakai_user_id());
		microsoftConfigurationService.saveMappedMicrosoftUserId(payload.getMapped_microsoft_user_id());
		microsoftConfigurationService.saveNewSiteFilter(payload.getSiteFilter());
		return REDIRECT_INDEX;
	}
}
