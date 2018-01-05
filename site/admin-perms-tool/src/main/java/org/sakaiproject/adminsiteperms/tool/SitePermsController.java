/**
 * Copyright 2008 Sakaiproject Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.adminsiteperms.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import org.sakaiproject.adminsiteperms.service.SitePermsService;
import org.sakaiproject.util.api.FormattedText;

/**
 * Handles the processing related to the permissions handler
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Slf4j
public class SitePermsController extends AbstractController {

    public static String[] templates = {
        "!site.template",
        "!site.template.course",
        "!site.template.portfolio",
        "!site.user"
    };

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (! sitePermsService.isSuperAdmin()) {
            throw new SecurityException("This is only accessible by super users");
        }
        Map<String,Object> model = new HashMap<String,Object>();

        // get the status message if there is one
        String statusMsg = sitePermsService.getCurrentStatusMessage();
        if (statusMsg != null) {
            addMessage(model, false, statusMsg);
        }

        /** The types of sites to add perms to (course/project/workspace/etc.) */
        String[] types = null;
        /** The perms to add/remove to the sites */
        String[] perms = null;
        /** The roles to have the perms added or removed */
        String[] roles = null;

        if ("POST".equals(request.getMethod().toUpperCase())) {
            if ("perms".equals(request.getParameter("action"))) {
                // this is a permissions update
                perms = request.getParameterValues("site-perm");
                types = request.getParameterValues("site-type");
                roles = request.getParameterValues("site-role");
                try {
                    if (ArrayUtils.isEmpty(perms)) {
                        // missing a setting so we can't actually process anything
                        throw new IllegalArgumentException("Invalid perms POST - no perms to add or remove");
                    } else if (ArrayUtils.isEmpty(types)) {
                        // missing a setting so we can't actually process anything
                        throw new IllegalArgumentException("Invalid perms POST - no site types to apply permissions to");
                    } else if (ArrayUtils.isEmpty(roles)) {
                        // missing a setting so we can't actually process anything
                        throw new IllegalArgumentException("Invalid perms POST - no roles to apply permissions to");
                    } else {
                        // OK, we have the data we need to process the update
                        boolean add;
                        if (request.getParameter("addPerms") != null) {
                            add = true;
                        } else if (request.getParameter("removePerms") != null) {
                            add = false;
                        } else {
                            throw new RuntimeException("Invalid perms POST - no addPerms or removePerms");
                        }
                        // triggers the permissions update
                        sitePermsService.setSiteRolePerms(perms, types, roles, add);
                        // add the frontend message and log
                        String msg = addMessage(model, false, "siterole.message.processing."+(add?"add":"remove"), 
                                new Object[] {a2es(perms), a2es(types), a2es(roles), 0});
                        log.info(msg);
                    }
                } catch (IllegalArgumentException e) {
                    // translate and pass the message to the frontend
                    String msg = addMessage(model, true, "siterole.message.illegal.submission", null);
                    log.warn(msg);
                } catch (IllegalStateException e) {
                    // translate and pass the message to the frontend
                    String msg = addMessage(model, true, "siterole.message.cannot.update", null);
                    log.warn(msg);
                }
            } else {
                throw new RuntimeException("Invalid POST - action is not set to a valid value");
            }
        }

        model.put("siteTypes", sitePermsService.getSiteTypes());
        model.put("roles", sitePermsService.getValidRoles());
        model.put("permissions", sitePermsService.getPermissions());
        model.put("additionalRoles", sitePermsService.getAdditionalRoles());

        return new ModelAndView("sitePerms", model);
    }

    private String a2es(String[] array) {
        return formattedText.escapeHtml(SitePermsService.makeStringFromArray(array));
    }

    /**
     * Method for adding translated messages to the model
     * 
     * @param model
     * @param error if true this is an error message, otherwise an info message
     * @param code the i18n key
     * @param args replacement args for the string
     * @return the added message
     */
    private String addMessage(Map<String,Object> model, boolean error, String code, Object[] args) {
        String msg = sitePermsService.getMessage(code, args);
        addMessage(model, error, msg);
        return msg;
    }

    /**
     * Method to add already translated message to the model
     * 
     * @param model
     * @param error if true this is an error message, otherwise an info message
     * @param message already translated message
     */
    @SuppressWarnings("unchecked")
    private void addMessage(Map<String,Object> model, boolean error, String message) {
        List<String> messages;
        String type = "messages";
        if (error) {
            type = "errors";
        }
        if (!model.containsKey(type)) {
            messages = new ArrayList<String>();
            model.put(type, messages);
        } else {
            messages = (List<String>) model.get(type);
        }
        messages.add(message);
    }


    private FormattedText formattedText;
    public void setFormattedText(FormattedText formattedText) {
        this.formattedText = formattedText;
    }

    private SitePermsService sitePermsService;
    public void setSitePermsService(SitePermsService sitePermsService) {
        this.sitePermsService = sitePermsService;
    }

}
