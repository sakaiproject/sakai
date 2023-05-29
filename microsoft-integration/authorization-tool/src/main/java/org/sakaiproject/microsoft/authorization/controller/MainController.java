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

package org.sakaiproject.microsoft.authorization.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.microsoft.api.data.MicrosoftRedirectURL;
import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.SakaiProxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;


@Slf4j
@Controller
public class MainController {

	@Autowired
	private MicrosoftAuthorizationService microsoftAuthorizationService;
	
	@Autowired
	private MicrosoftConfigurationService microsoftConfigurationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	private static final String INDEX_TEMPLATE = "index";
	private static final String TOKEN_TEMPLATE = "token";
	
	@ModelAttribute("locale")
	public Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        Locale loc = sakaiProxy.getLocaleForCurrentUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }

	@RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
	public String showIndex(Model model) {
		model.addAttribute("pickerRedirectUrl", "/portal");

		return INDEX_TEMPLATE;
	}
	
	@RequestMapping(value = {"/token"}, method = RequestMethod.GET)
	public String doToken(@RequestParam(required=false) String code, @RequestParam(required=false) String state, Model model) {
		log.debug("Authorization Token endpoint");
		
		String userId = sakaiProxy.getCurrentUserId();

		
		MicrosoftRedirectURL sessionRedirect = (MicrosoftRedirectURL)sakaiProxy.getCurrentSession().getAttribute(MicrosoftAuthorizationService.MICROSOFT_SESSION_REDIRECT);
		sakaiProxy.getCurrentSession().removeAttribute(MicrosoftAuthorizationService.MICROSOFT_SESSION_REDIRECT);
		
		//check if stored state is equal to the received one
		if(sessionRedirect == null || state == null || !state.equals(sessionRedirect.getState())) {
			model.addAttribute("pickerRedirectUrl", "/portal");
			return INDEX_TEMPLATE;
		}
		
		boolean configured = false;
		if(StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(userId)) {
			configured = microsoftAuthorizationService.processAuthorizationCode(userId, code);
		}
		
		String returnTo = StringUtils.isNotBlank(sessionRedirect.getURL()) ? sessionRedirect.getURL() : "/portal";
		
		log.debug("Token configured: {} ", configured);
		log.debug("Returning to: {} ", returnTo);
		log.debug("Returning auto-mode: {} ", sessionRedirect.isAuto());
		
		if(configured && sessionRedirect.isAuto()) {
			return "redirect:" + returnTo;
		}
		
		model.addAttribute("pickerRedirectUrl", returnTo);
		model.addAttribute("onedriveConfigured", configured);

		return TOKEN_TEMPLATE;
	}
}
