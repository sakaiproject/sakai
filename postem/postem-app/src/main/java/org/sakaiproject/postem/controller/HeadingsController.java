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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.postem.constants.PostemToolConstants;
import org.sakaiproject.postem.service.PostemSakaiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
public class HeadingsController {

    @Autowired
    private PostemSakaiService postemSakaiService;

    @GetMapping(value = {"/title/{ascendingTitle}"})
    public String sortByTitle(@PathVariable("ascendingTitle") boolean ascendingTitle, Model model) {
        log.debug("Postem sortByTitle");

        boolean toggleAscending = toggleSort(ascendingTitle);
        List<Gradebook> gradebooksList = postemSakaiService.getGradebooks(Gradebook.SORT_BY_TITLE, toggleAscending);

        model.addAttribute("gradebooksList", gradebooksList);
        model.addAttribute("sortedByTitle", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingTitle", String.valueOf(toggleAscending));
        model.addAttribute("ascendingCreator", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingModifiedBy", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingLastMod", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingReleased", PostemToolConstants.POSTEM_FALSE_CONSTANT);

        String visible = Boolean.toString(postemSakaiService.checkAccess());
        model.addAttribute("visible", visible);

        return PostemToolConstants.INDEX_TEMPLATE;
    }

    @GetMapping(value = {"/creator/{ascendingCreator}"})
    public String sortByCreator(@PathVariable("ascendingCreator") boolean ascendingCreator, Model model) {
        log.debug("Postem sortByCreator");

        boolean toggleAscending = toggleSort(ascendingCreator);

        List<Gradebook> gradebooksList = postemSakaiService.getGradebooks(Gradebook.SORT_BY_CREATOR, toggleAscending);

        model.addAttribute("gradebooksList", gradebooksList);
        model.addAttribute("sortedByCreator", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingCreator", String.valueOf(toggleAscending));
        model.addAttribute("ascendingTitle", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingModifiedBy", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingLastMod", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingReleased", PostemToolConstants.POSTEM_FALSE_CONSTANT);

        String visible = Boolean.toString(postemSakaiService.checkAccess());
        model.addAttribute("visible", visible);
        return PostemToolConstants.INDEX_TEMPLATE;
    }

    @GetMapping(value = {"/modifiedBy/{ascendingModifiedBy}"})
    public String sortByModifiedBy(@PathVariable("ascendingModifiedBy") boolean ascendingModifiedBy, Model model) {
        log.debug("Postem sortByModifiedBy");

        boolean toggleAscending = toggleSort(ascendingModifiedBy);
        List<Gradebook> gradebooksList = postemSakaiService.getGradebooks(Gradebook.SORT_BY_MOD_BY, toggleAscending);

        model.addAttribute("gradebooksList", gradebooksList);
        model.addAttribute("sortedByModifiedBy", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingModifiedBy", String.valueOf(toggleAscending));
        model.addAttribute("ascendingTitle", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingCreator", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingLastMod", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingReleased", PostemToolConstants.POSTEM_FALSE_CONSTANT);

        String visible = Boolean.toString(postemSakaiService.checkAccess());
        model.addAttribute("visible", visible);
        return PostemToolConstants.INDEX_TEMPLATE;
    }

    @GetMapping(value = {"/lastModified/{ascendingLastMod}"})
    public String sortByLastModified(@PathVariable("ascendingLastMod") boolean ascendingLastMod, Model model) {
        log.debug("Postem sortByLastModified");

        boolean toggleAscending = toggleSort(ascendingLastMod);
        List<Gradebook> gradebooksList = postemSakaiService.getGradebooks(Gradebook.SORT_BY_MOD_DATE, toggleAscending);

        model.addAttribute("gradebooksList", gradebooksList);
        model.addAttribute("sortedByLastModified", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingLastMod", String.valueOf(toggleAscending));
        model.addAttribute("ascendingTitle", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingCreator", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingModifiedBy", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingReleased", PostemToolConstants.POSTEM_FALSE_CONSTANT);

        String visible = Boolean.toString(postemSakaiService.checkAccess());
        model.addAttribute("visible", visible);

        return PostemToolConstants.INDEX_TEMPLATE;
    }

    @GetMapping(value = {"/released/{ascendingReleased}"})
    public String sortByReleased(@PathVariable("ascendingReleased") boolean ascendingReleased, Model model) {
        log.debug("Postem sortByReleased");

        boolean toggleAscending = toggleSort(ascendingReleased);
        List<Gradebook> gradebooksList = postemSakaiService.getGradebooks(Gradebook.SORT_BY_RELEASED, toggleAscending);

        model.addAttribute("gradebooksList", gradebooksList);
        model.addAttribute("sortedByReleased", PostemToolConstants.POSTEM_TRUE_CONSTANT);
        model.addAttribute("ascendingReleased", String.valueOf(toggleAscending));
        model.addAttribute("ascendingTitle", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingCreator", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingModifiedBy", PostemToolConstants.POSTEM_FALSE_CONSTANT);
        model.addAttribute("ascendingLastMod", PostemToolConstants.POSTEM_FALSE_CONSTANT);

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));

        return PostemToolConstants.INDEX_TEMPLATE;
    }

    public boolean toggleSort(boolean isAscending) {
        return !isAscending;
    }
}
