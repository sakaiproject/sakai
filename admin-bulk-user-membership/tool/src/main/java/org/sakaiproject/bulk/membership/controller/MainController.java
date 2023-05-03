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

package org.sakaiproject.bulk.membership;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.bulk.membership.service.BulkUserMembershipToolService;
import org.sakaiproject.bulk.membership.exception.UsersByEmailException;
import org.sakaiproject.bulk.membership.model.Summary;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@Slf4j
@Controller
public class MainController {

    @Autowired
    private BulkUserMembershipToolService bulkUserMembershipToolService;

    private final String AUTO_MEMBERSHIP_STEP_1 = "auto_membership_step_1";
    private final String AUTO_MEMBERSHIP_STEP_1_5 = "auto_membership_step_1_5";
    private final String AUTO_MEMBERSHIP_STEP_2 = "auto_membership_step_2";
    private final String AUTO_MEMBERSHIP_STEP_3 = "auto_membership_step_3";

    private final String REDIRECT = "redirect:/";
    private final String ADD_ACTION = "add";

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayList<Site> sites = new ArrayList<Site>();
    private String[] roles = null;

    private enum STEP {ZERO, FIRST, SECOND, THIRD};

    private int currentStep = STEP.FIRST.ordinal();
    private String action = "";

    @ModelAttribute("locale")
    public Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        Locale loc = bulkUserMembershipToolService.getLocaleForCurrentUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        log.debug("Starting the Bulk User Membership tool");
        resetWizard(model);
        return usePage(model);
    }

    @GetMapping(value = {"/step/{step}", "/auto_membership_step_1", "/auto_membership_step_1_5", "/auto_membership_step_2", "/auto_membership_step_3"})
    public String usePage(Model model) {
        String nextPage = "";
        model.addAttribute("users", StringUtils.join(users, "\n"));

        String[] sitesIds = new String[sites.size()];
        for (int index = 0; index < sites.size(); index++) {
            sitesIds[index] = sites.get(index).getId();
        }

        model.addAttribute("sitesIds", StringUtils.join(sitesIds, "\n"));
        model.addAttribute("action", action);
        switch (currentStep) {
            case 0:
                model.addAttribute("roles", roles);
                model.addAttribute("sites", sites);
                nextPage = AUTO_MEMBERSHIP_STEP_1_5;
                break;
            case 1:
                nextPage = AUTO_MEMBERSHIP_STEP_1;
                break;
            case 2:
                nextPage = AUTO_MEMBERSHIP_STEP_2;
                break;
            case 3:
                nextPage = AUTO_MEMBERSHIP_STEP_3;
                break;
            default: 
                nextPage = AUTO_MEMBERSHIP_STEP_1;
                log.error("Page number: {} not found", currentStep);
                break;
        }
        return nextPage;
    }

    @PostMapping(value = {"/step/1", "/auto_membership_step_1"})
    public String verifyFistStep(@RequestParam(value="users", required = false) String usersCriteria, 
                                 @RequestParam(value="sites", required = false) String sitesIds,
                                 @RequestParam(value="action", required = false) String action,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (StringUtils.isAnyEmpty(usersCriteria, sitesIds, action)) {
            redirectAttributes.addFlashAttribute("noUsers", StringUtils.isBlank(usersCriteria));
            redirectAttributes.addFlashAttribute("noSites", StringUtils.isBlank(sitesIds));
            redirectAttributes.addFlashAttribute("noAction", StringUtils.isBlank(action));
            redirectAttributes.addFlashAttribute("users", usersCriteria);
            redirectAttributes.addFlashAttribute("sites", sitesIds);
            return REDIRECT + AUTO_MEMBERSHIP_STEP_1;
        }
        users = new ArrayList<String>();
        sites = new ArrayList<Site>();
        this.action = action;

        ArrayList<String> failedUsers = new ArrayList<String>();
        ArrayList<String> duplicatedUsersError = new ArrayList<String>();
        String[] usersCriteriaAux = usersCriteria.split("\r\\n");
        for (String userCriteriaAux : usersCriteriaAux) {
            User user = null;
            boolean duplicatedUser = false;
            try {
                user = bulkUserMembershipToolService.getUser(userCriteriaAux);
            } catch (UsersByEmailException usExc) {
                duplicatedUsersError.add(userCriteriaAux);
                duplicatedUser = true;
            }
            if (user != null && !StringUtils.isBlank(userCriteriaAux)) {
                boolean repeatedUser = false;
                for (String userCriteria : users) {
                    if (StringUtils.equalsAny(userCriteria, user.getEid(), user.getEmail())) {
                        repeatedUser = true;
                    }
                }
                if (!repeatedUser) {
                    users.add(userCriteriaAux);
                }
            } else if (!duplicatedUser) {
                failedUsers.add(userCriteriaAux);
                log.warn("Failing when getting the user: " + userCriteriaAux );
            }
        }
        ArrayList<String> failedSites = new ArrayList<String>();
        String[] sitesIdsAux = sitesIds.split("\r\\n");
        ArrayList<Site> sitesAux = new ArrayList<Site>();
        for (String siteId : sitesIdsAux) {
            Site site = bulkUserMembershipToolService.getSite(siteId);
            if (!StringUtils.isBlank(siteId)) {
                if (site != null) {
                    boolean repeatedSite = false;
                    for (Site siteAux : sites) {
                        if (StringUtils.equals(siteId, siteAux.getId())) {
                            repeatedSite = true;
                        }
                    }
                    if (!repeatedSite) {
                        sites.add(site);
                    }
                } else {
                    failedSites.add(siteId);
                    log.warn("Failing when getting the site: " + siteId);
                }
            }
        }
        if (failedSites.size() > 0 || failedUsers.size() > 0 || duplicatedUsersError.size() > 0) {
            redirectAttributes.addFlashAttribute("failedUsers", failedUsers);
            redirectAttributes.addFlashAttribute("duplicatedUsers", duplicatedUsersError);
            redirectAttributes.addFlashAttribute("failedSites", failedSites);
            return REDIRECT + usePage(model);
        } else if (StringUtils.equals(ADD_ACTION, action)) {
            currentStep = STEP.ZERO.ordinal();
            return REDIRECT + usePage(model);
        } else {
            currentStep = STEP.SECOND.ordinal();
            return REDIRECT + usePage(model);
        }
    }

    @PostMapping(value = {"/step/2", "/auto_membership_step_2"})
    public String checkToFinalStep(Model model, RedirectAttributes redirectAttributes) {
        int count;
        ArrayList<Summary> summaries = new ArrayList<Summary>();
        int userCount = 1;
        for (String userCriteria : users) {
            Summary summary = new Summary(userCriteria);
            User user = null;
            try {
                user = bulkUserMembershipToolService.getUser(userCriteria);
            } catch (UsersByEmailException usEx) {
                log.error("Something went wrong when getting the user from the email: " + userCriteria + ". It has 2 or more related users");
            }
            String userName = userCount + "-" + user.getEid();
            count = 0;
            summary.setUserName(userName);
            for (Site site : sites) {
                String role = ((roles != null && roles.length > 0) ? roles[count] : "");
                try {
                    bulkUserMembershipToolService.applyAction(action, site, user, role);
                    summary.addWorkedSite(site.getId());
                } catch (Exception ex) {
                    summary.addFailedSite(site.getId());
                }
                count++;
            }
            summaries.add(summary);
            userCount++;
        }
        currentStep = STEP.THIRD.ordinal();
        redirectAttributes.addFlashAttribute("summaries", summaries);
        return REDIRECT + usePage(model);
    }

    @PostMapping(value = {"/step/1_5", "/auto_membership_step_1_5"})
    public String selectRole(@RequestParam(value="roles[]", required = false) String[] selectedRoles,
                             Model model) {
        roles = selectedRoles;
        currentStep = STEP.SECOND.ordinal();
        return REDIRECT + usePage(model);
    }

    @PostMapping(path = {"/cancel", "/step/3"})
    public String resetWizard(Model model) {
        sites = new ArrayList<Site>();
        users = new ArrayList<String>();
        roles = null;
        action = "";
        currentStep = STEP.FIRST.ordinal();
        return REDIRECT + usePage(model);
    }

    @PostMapping(path = {"/back"})
    public String back(Model model, RedirectAttributes redirectAttributes) {
        if (StringUtils.equals(ADD_ACTION, action)) {
            currentStep = (currentStep != STEP.ZERO.ordinal())? STEP.ZERO.ordinal() : STEP.FIRST.ordinal();
        } else {
            roles = null;
            currentStep = STEP.FIRST.ordinal();
        }
        return REDIRECT + usePage(model);
    }
    
}
