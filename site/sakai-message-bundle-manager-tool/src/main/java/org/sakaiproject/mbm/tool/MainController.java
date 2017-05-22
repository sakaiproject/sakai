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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mbm.tool.entities.ModulesFilterEntity;
import org.sakaiproject.mbm.tool.entities.MessageEntity;
import org.sakaiproject.mbm.tool.entities.SearchEntity;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.github.dandelion.datatables.core.exception.ExportException;
import com.github.dandelion.datatables.core.export.CsvExport;
import com.github.dandelion.datatables.core.export.ExportConf;
import com.github.dandelion.datatables.core.export.ExportUtils;
import com.github.dandelion.datatables.core.export.HtmlTableBuilder;
import com.github.dandelion.datatables.core.export.ReservedFormat;
import com.github.dandelion.datatables.core.html.HtmlTable;
import com.github.dandelion.datatables.extras.spring3.ajax.DatatablesParams;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 * @author Earle Nietzel
 *         (earle.nietzel@gmail.com)
 *
 */
@Slf4j
@Controller
public class MainController {

	@Resource(name="org.sakaiproject.messagebundle.api.MessageBundleService")
	private MessageBundleService messageBundleService;
	
	@Autowired
	private ServerConfigurationService serverConfigurationService;
	
	@Autowired
	private SecurityService securityService;

	@Autowired
	@Qualifier("org.sakaiproject.mbm.tool.MessageSource")
	private MessageSource messageSource;

	@Autowired
	private PreferencesService preferencesService;

	@Autowired
	private SessionManager sessionManager;

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
	
    @RequestMapping(value = "/modified", params = {"dtf=csv","dti=modified"}, method = RequestMethod.GET, produces = "text/csv")
    public void csv(@DatatablesParams DatatablesCriterias criterias, HttpServletRequest request, HttpServletResponse response) 
            throws ExportException, IOException {
        log.debug("csv() export");

        String userId = sessionManager.getCurrentSessionUserId();
        Locale userLocale = preferencesService.getLocale(userId);
        List<MessageBundleProperty> list = listModifiedMessageProperties();

        ExportConf csvConf = new ExportConf.Builder(ReservedFormat.CSV)
            .fileName("messages-" + new DateTime().toString(messageSource.getMessage("modified.csv.date.format", null, userLocale)))
            .header(true)
            .exportClass(new CsvExport())
            .build();

        HtmlTable csvTable = new HtmlTableBuilder<MessageBundleProperty>().newBuilder("modified", list, request, csvConf)
                .column().fillWithProperty("id").title(messageSource.getMessage("modified.csv.id", null, userLocale))
                .column().fillWithProperty("moduleName").title(messageSource.getMessage("modified.csv.module", null, userLocale))
                .column().fillWithProperty("propertyName").title(messageSource.getMessage("modified.csv.property", null, userLocale))
                .column().fillWithProperty("value").title(messageSource.getMessage("modified.csv.value", null, userLocale))
                .column().fillWithProperty("defaultValue").title(messageSource.getMessage("modified.csv.default", null, userLocale))
                .column().fillWithProperty("locale").title(messageSource.getMessage("modified.csv.locale", null, userLocale))
                .build();

        ExportUtils.renderExport(csvTable, csvConf, response);
    }

	@RequestMapping(value = "/modules", method = RequestMethod.GET)
	public String showModules(Model model) {
		log.debug("showModules()");

		List<MessageBundleProperty> properties = Collections.emptyList();
		model.addAttribute("properties", properties);

		ModulesFilterEntity filter = new ModulesFilterEntity();
		model.addAttribute(filter);

	    return "modules";
	}

	@RequestMapping(value = "/modules", params={"filter"}, method = RequestMethod.POST)
	public String processModules(final ModulesFilterEntity modulesFilterEntity, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "modules";
	    }
	    log.debug("processModules(): ModulesFilteredEntity = " + modulesFilterEntity);
	    model.addAttribute("properties", messageBundleService.getAllProperties(modulesFilterEntity.getLocale(), modulesFilterEntity.getModule()));

	    return "modules";
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
	public String processSaveEdit(final MessageEntity messageEntity, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "modified";
	    }
	    log.debug("processSaveEdit(): MessageEntity = " + messageEntity);
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
	public String processRevertEdit(final MessageEntity messageEntity, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "modified";
	    }
	    log.debug("processRevertEdit(): MessageEntity = " + messageEntity);
	    MessageBundleProperty property = messageBundleService.getMessageBundleProperty(messageEntity.getId());
	    if (securityService.isSuperUser()) {
	    	messageBundleService.revert(property);
	    } else {
	    	throw new SecurityException("Only an admin type user is allowed to revert message bundle properties");
	    }
	    model.clear();
	    return "redirect:/modified";
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showSearch(Model model) {
        log.debug("showSearch()");

        List<MessageBundleProperty> properties = Collections.emptyList();
        model.addAttribute("properties", properties);

        SearchEntity search = new SearchEntity();
        model.addAttribute(search);

        return "search";
    }

    @RequestMapping(value = "/search", params={"search"}, method = RequestMethod.POST)
    public String processSearch(final SearchEntity searchEntity, final BindingResult bindingResult, final ModelMap model) {
        if (bindingResult.hasErrors()) {
            return "search"; 
        }
        log.debug("processSearch(): searchEntity = " + searchEntity);
        model.addAttribute("properties", messageBundleService.search(searchEntity.getValue(), null, null, searchEntity.getLocale()));

        return "search";
    }
}
