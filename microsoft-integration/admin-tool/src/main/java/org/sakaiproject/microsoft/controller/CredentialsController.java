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

import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;


/**
 * CredentialsController
 * 
 * This is the controller used by Spring MVC to handle credential related requests
 * 
 */
@Slf4j
@Controller
public class CredentialsController {
	
	private static ResourceLoader rb = new ResourceLoader("Messages");
	
	@Autowired
	private MicrosoftConfigurationService microsoftConfigurationService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	private static final String CREDENTIALS_TEMPLATE = "credentials";
	private static final String REDIRECT_CREDENTIALS = "redirect:/credentials";

	
	@GetMapping(value = {"/credentials"})
	public String showCredentials(Model model) {
		model.addAttribute("credentials", microsoftConfigurationService.getCredentials());
		return CREDENTIALS_TEMPLATE;
	}
	
	@PostMapping(path = {"/save-credentials"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String saveCredentials(@ModelAttribute  MicrosoftCredentials credentials,  Model model) {
		microsoftConfigurationService.saveCredentials(credentials);
		microsoftCommonService.resetGraphClient();
		return REDIRECT_CREDENTIALS;
	}
	
	@GetMapping(path = {"/test-credentials"})
	public String testCredentials(Model model, RedirectAttributes redirectAttributes) {
		try {
			MicrosoftCredentials credentials = microsoftConfigurationService.getCredentials();
			microsoftCommonService.checkConnection();
			MicrosoftUser mu = microsoftCommonService.getUserByEmail(credentials.getEmail());
			if(mu == null) {
				redirectAttributes.addFlashAttribute("exception_error", rb.getString("error.invalid_email"));
			} else {
				redirectAttributes.addFlashAttribute("test_message", rb.getString("credentials.ok"));
			}
		} catch (MicrosoftCredentialsException e) {
			redirectAttributes.addFlashAttribute("exception_error", rb.getString(e.getMessage()));
		}
		return REDIRECT_CREDENTIALS;
	}
}
