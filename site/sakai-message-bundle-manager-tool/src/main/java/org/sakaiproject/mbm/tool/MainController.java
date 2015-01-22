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
package org.sakaiproject.mbm.tool;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.apachecommons.CommonsLog;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mbm.tool.entities.AllFilterEntity;
import org.sakaiproject.mbm.tool.entities.MessageEntity;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 * @author Earle Nietzel
 *         (earle.nietzel@gmail.com)
 *
 */
@CommonsLog
@Controller
public class MainController {

	@Resource(name="org.sakaiproject.messagebundle.api.MessageBundleService")
	private MessageBundleService messageBundleService;
	
	@Autowired
	private ServerConfigurationService serverConfigurationService;
	
	@Autowired
	private SecurityService securityService;

	@ModelAttribute("listMessageProperties")
	public List<MessageBundleProperty> listMessageProperties() {
		return messageBundleService.getAllProperties(null, null);
	}

	@ModelAttribute("listModifiedMessageProperties")
	public List<MessageBundleProperty> listModifiedMessageProperties() {
		return messageBundleService.getModifiedProperties(0, 0, 0, -1);
	}

	@ModelAttribute("countMessageProperties")
	public int countMessageBundleProperties() {
		return messageBundleService.getAllPropertiesCount();
	}
	
	@ModelAttribute("countModifiedMessageProperties")
	public int countModifiedMessageProperties() {
		return messageBundleService.getModifiedPropertiesCount();
	}
	
	@ModelAttribute("listMessagePropertyModules")
	public List<String> listMessagePropertyModules() {
		return messageBundleService.getAllModuleNames();
	}

	@ModelAttribute("listMessagePropertyLocales")
	public List<String> listMessagePropertyLocales() {
		return messageBundleService.getLocales();
	}
	
	@ModelAttribute("isLoadBundlesFromDb")
	public boolean isLoadBundlesFromDb() {
		return serverConfigurationService.getBoolean("load.bundles.from.db", false);
	}

	@ModelAttribute("isAdmin")
	public boolean isAdmin() {
		return securityService.isSuperUser();
	}
	
	@RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
	public String showIndex(Model model) {
		log.debug("showIndex()");
	    return "index";
	}

	@RequestMapping(value = "/modified", method = RequestMethod.GET)
	public String showModified(Model model) {
		log.debug("showModified()");
	    return "modified";
	}
	
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public String showAll(Model model) {
		log.debug("showAll()");
		
		List<MessageBundleProperty> properties = Collections.emptyList();
		model.addAttribute("properties", properties);
		
		AllFilterEntity filter = new AllFilterEntity();
		model.addAttribute(filter);
		
	    return "all";
	}

	@RequestMapping(value = "/all", params={"module","locale"}, method = RequestMethod.GET)
	public String showAllFiltered(@RequestParam String module, @RequestParam String locale, Model model) {
		if(log.isDebugEnabled()) { log.debug("showAllFiltered(): module=" + module + ", locale=" + locale); }
	    
		model.addAttribute("properties", messageBundleService.getAllProperties(locale, module));

	    AllFilterEntity filter = new AllFilterEntity(module, locale);
		model.addAttribute(filter);
		
	    return "all";
	}

	@RequestMapping(value = "/all", params={"filter"}, method = RequestMethod.POST)
	public String showAllFiltered(final AllFilterEntity allFilterEntity, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "all";
	    }
	    log.debug("showAllFiltered(): AllFilteredEntity = " + allFilterEntity);
	    model.clear();
	    return "redirect:/all?module=" + allFilterEntity.getModule() + "&locale=" + allFilterEntity.getLocale();
	}
	
	@RequestMapping(value = "/edit", params = { "id" }, method = RequestMethod.GET)
	public String showEdit(@RequestParam long id, Model model) {
		log.debug("showEdit()");
		
		MessageBundleProperty property = messageBundleService.getMessageBundleProperty(id);
		model.addAttribute(property);
		
		MessageEntity message = new MessageEntity(id, property.getValue());
		model.addAttribute(message);
		
	    return "edit";
	}
	
	@RequestMapping(value = "/edit", params={"save"}, method = RequestMethod.POST)
	public String saveMessageProperty(final MessageEntity messageEntity, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "modified";
	    }
	    log.debug("saveMessageProperty: MessageEntity = " + messageEntity);
	    MessageBundleProperty property = messageBundleService.getMessageBundleProperty(messageEntity.getId());
	    property.setValue(messageEntity.getValue());
	    if (securityService.isSuperUser()) {
	    	messageBundleService.updateMessageBundleProperty(property);
	    } else {
	    	throw new SecurityException("Only an admin type user is allowed to update message bundle properties");
	    }
	    model.clear();
	    return "redirect:/modified";
	}
	
	@RequestMapping(value = "/edit", params={"revert"}, method = RequestMethod.POST)
	public String revertMessageProperty(final MessageEntity messageEntity, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "modified";
	    }
	    log.debug("revertMessageProperty: MessageEntity = " + messageEntity);
	    MessageBundleProperty property = messageBundleService.getMessageBundleProperty(messageEntity.getId());
	    if (securityService.isSuperUser()) {
	    	messageBundleService.revert(property);
	    } else {
	    	throw new SecurityException("Only an admin type user is allowed to revert message bundle properties");
	    }
	    model.clear();
	    return "redirect:/modified";
	}
}
