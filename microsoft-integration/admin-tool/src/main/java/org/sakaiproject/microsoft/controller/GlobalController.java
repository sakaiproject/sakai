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
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.exceptions.NoAdminException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@Slf4j
@ControllerAdvice
public class GlobalController {

	private static ResourceLoader rb = new ResourceLoader("Messages");

	@Autowired
	private SakaiProxy sakaiProxy;

	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String REDIRECT_CREDENTIALS = "redirect:/credentials";
	private static final String ERROR_ADMIN_TEMPLATE = "errorNoAdmin";

	@ModelAttribute("isAdmin")
	public boolean isAdmin() throws NoAdminException {
		if(sakaiProxy.isAdmin()) {
			return true;
		} else {
			throw new NoAdminException();
		}
	}
	
	@ModelAttribute("locale")
	public Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        Locale loc = sakaiProxy.getLocaleForCurrentUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }
	
	@ModelAttribute("today")
	public ZonedDateTime today() {
		return ZonedDateTime.now();
	}

	@ExceptionHandler(MicrosoftGenericException.class)
	public String handleCredentialsError(HttpServletRequest req, Exception ex, RedirectAttributes redirectAttributes) {

		redirectAttributes.addFlashAttribute("exception_error", rb.getString(ex.getMessage()));

		if (ex instanceof MicrosoftCredentialsException) {
			return REDIRECT_CREDENTIALS;
		}
		if (ex instanceof NoAdminException) {
			return ERROR_ADMIN_TEMPLATE;
		}
		return REDIRECT_INDEX;
	}
}
