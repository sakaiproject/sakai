/******************************************************************************
 * Copyright (c) 2021 Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.sakaiproject.postem.controller;

import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.postem.constants.PostemToolConstants;
import org.sakaiproject.postem.form.GradebookForm;
import org.sakaiproject.postem.service.PostemSakaiService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.PreferencesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private PostemSakaiService postemSakaiService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @GetMapping(value = {"/", "/index"})
    public String showIndex(Model model, HttpServletRequest request, HttpServletResponse response) {
        log.debug("showIndex");

        String userId = sessionManager.getCurrentSessionUserId();
        final Locale locale = preferencesService.getLocale(userId);
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, locale);

        List<Gradebook> gradebooksList = postemSakaiService.getGradebooks(Gradebook.SORT_BY_TITLE, true);

        model.addAttribute("gradebooksList", gradebooksList);
        model.addAttribute("sortedByTitle", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingTitle", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingCreator", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingModifiedBy", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingLastMod", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingReleased", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));

        ToolSession toolSession = sessionManager.getCurrentToolSession();
        toolSession.setAttribute("currentGradebook", null);

        return PostemToolConstants.INDEX_TEMPLATE;
    }

    @GetMapping(value = {"/add"})
    public String addItem(@ModelAttribute("gradebookForm") GradebookForm gradebookForm, Model model) {
        log.debug("addItem");

        ToolSession toolSession = sessionManager.getCurrentToolSession();
        Gradebook currentGradebook = (Gradebook) toolSession.getAttribute("currentGradebook");

        if (null != currentGradebook) {
            String[] parts = currentGradebook.getFileReference().split("/");
            if (parts.length > 0) {
                String partFileReference = parts[parts.length - 1];
                gradebookForm.setFileReference(partFileReference);
            }
            gradebookForm.setTitle(currentGradebook.getTitle());
            gradebookForm.setReleased(currentGradebook.getReleased());
        }

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        model.addAttribute("gradebookForm", gradebookForm);
        model.addAttribute("fileReference", gradebookForm.getFileReference());
        String uploadMax = serverConfigurationService.getString(ContentHostingService.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
        if (null == uploadMax || uploadMax.isEmpty()) {
            uploadMax = "20"; //default MB
        }
        model.addAttribute("uploadMax", uploadMax);
        return PostemToolConstants.ADD_ITEM;
    }
}
