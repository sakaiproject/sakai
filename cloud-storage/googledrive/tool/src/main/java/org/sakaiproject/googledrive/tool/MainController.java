/****************************************************************************** 
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.googledrive.tool;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.googledrive.service.GoogleDriveService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 * @author Miguel Pellicer (mpellicer@edf.global)
 *
 */
@Slf4j
@Controller
public class MainController {

	@Inject
	private GoogleDriveService googleDriveService;
	@Inject
	private SessionManager sessionManager;

	@RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
	public String showIndex(@RequestParam(required=false) String code, Model model) {
		log.debug("GoogleDriveServlet : Called the main servlet.");
		String userId = sessionManager.getCurrentSessionUserId();
		Object pickerRedirectUrlObject = sessionManager.getCurrentSession().getAttribute(googleDriveService.GOOGLEDRIVE_REDIRECT_URI);
		sessionManager.getCurrentSession().removeAttribute(googleDriveService.GOOGLEDRIVE_REDIRECT_URI);
		String pickerRedirectUrl = pickerRedirectUrlObject != null ? pickerRedirectUrlObject.toString() : null;
		log.debug("GoogleDriveServlet : request code {}", code);
		log.debug("GoogleDriveServlet : sakai user {}", userId);
		log.debug("GoogleDriveServlet : pickerRedirectUrl {}", pickerRedirectUrl);
		boolean configured = false;
		if(StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(userId)) {
			configured = googleDriveService.token(userId, code);
		}
		log.debug("GoogleDriveServlet : configured token {} ", configured);
		model.addAttribute("pickerRedirectUrl", pickerRedirectUrl != null ? pickerRedirectUrl : "/portal");
		model.addAttribute("googledriveConfigured", configured);
		log.debug("GoogleDriveServlet : Finished action and returning.");
		return "index";
	}

}
