/****************************************************************************** 
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.widget.tool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.widget.api.WidgetService;
import org.sakaiproject.widget.model.Widget;
import org.sakaiproject.widget.tool.entities.AddWidget;
import org.sakaiproject.widget.tool.exception.MissingSessionException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 *
 * The concept here is to only hand marshalling data to and from the templates.
 * All the business logic should exist in the service.
 * 
 */
@Slf4j
@Controller
public class MainController {

	@Resource(name = "org.sakaiproject.messagebundle.api.MessageBundleService")
	private MessageBundleService messageBundleService;
	
	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;
	
	@Resource(name = "org.sakaiproject.authz.api.SecurityService")
	private SecurityService securityService;

	@Resource(name = "org.sakaiproject.widget.tool.Messages")
	private MessageSource messageSource;

	@Resource(name = "org.sakaiproject.tool.api.SessionManager")
	private SessionManager sessionManager;

	@Resource(name = "org.sakaiproject.widget.api.WidgetService")
	private WidgetService widgetService;

	@Resource(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;

	@ModelAttribute("isAdmin")
	public boolean isAdmin() {
		return securityService.isSuperUser();
	}
	
	@RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
	public String pageIndex(Model model) {
		log.debug("page Index");
		checkSakaiSession();
		String context = toolManager.getCurrentPlacement().getContext();
		model.addAttribute("widgets", widgetService.getWidgetsForSiteWithStatus(context));
	    return "index";
	}

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String pageAdd(Model model) {
		log.debug("page Add");
        checkSakaiSession();
		model.addAttribute("widget", new AddWidget());
	    return "add";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String pageAddPost(final AddWidget addWidget, final BindingResult bindingResult, final ModelMap model) {
		log.debug("post new widget");
        checkSakaiSession();
		if (bindingResult.hasErrors()) {
			return "add";
		}
		Widget widget = new Widget();
		widget.setTitle(addWidget.getTitle());
		widget.setDescription(addWidget.getDescription());
		widget.setDateExpired(addWidget.getExpiration());
		widget.setContext(toolManager.getCurrentPlacement().getContext());
		widgetService.addWidget(widget);
		model.clear();
	    return "redirect:/index";
	}

	@RequestMapping(value = "/deleted", method = RequestMethod.GET)
	public String pageDeleted(Model model) {
		log.debug("page Deleted");
        checkSakaiSession();
        String context = toolManager.getCurrentPlacement().getContext();
        model.addAttribute("widgets", widgetService.getWidgetsForSiteWithStatus(context, Widget.STATUS.DELETED));
	    return "deleted";
	}

	@RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
	public String pageEdit(@RequestParam("id") String id, Model model) {
		log.debug("page Edit id={}", id);
        checkSakaiSession();
		model.addAttribute("widget", widgetService.getWidget(id));
	    return "edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String pageEdit(final Widget widget, final BindingResult bindingResult, Model model) {
		log.debug("post updated widget");
        checkSakaiSession();
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        Optional<Widget> previous = widgetService.getWidget(widget.getId());
        if (previous.isPresent()) {
            Widget w = previous.get();
            w.setTitle(widget.getTitle());
            w.setDescription(widget.getDescription());
            w.setDateExpired(widget.getDateExpired());
            w.setStatus(widget.getStatus());
            widgetService.updateWidget(w);
        }
	    return "redirect:/index";
	}

    /**
     * Check for a valid session
     * if not valid a 403 Forbidden will be returned
     */
	private void checkSakaiSession() {
	    try {
            Session session = sessionManager.getCurrentSession();
            if (StringUtils.isBlank(session.getUserId())) {
                log.error("Sakai user session is invalid");
                throw new MissingSessionException();
            }
        } catch (IllegalStateException e) {
	        log.error("Could not retrieve the sakai session");
            throw new MissingSessionException(e.getCause());
        }
    }
}
