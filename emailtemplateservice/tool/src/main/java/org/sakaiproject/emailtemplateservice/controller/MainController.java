/** ****************************************************************************
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
 ***************************************************************************** */
package org.sakaiproject.emailtemplateservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.sakaiproject.emailtemplateservice.constants.EmailTemplateConstants;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private MessageSource messageSource;

    private Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        String userId = sessionManager.getCurrentSessionUserId();
        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }

    @RequestMapping(value = {"/", "/index","/index/{success}/{templateKey}/{templateLocale}"})
    public String showIndex(Model model, HttpServletRequest request, HttpServletResponse response,
            @PathVariable(value = "success", required = false) boolean success,
            @PathVariable(value = "templateKey", required = false) String templateKey,
            @PathVariable(value = "templateLocale", required = false) String templateLocale) {

        localeResolver(request, response);
        List<EmailTemplate> templates = emailTemplateService.getEmailTemplates(0, 0);
        templates.sort((p1, p2) -> p1.getKey().compareTo(p2.getKey()));
        model.addAttribute("templates", templates);
        model.addAttribute("success", success);
        model.addAttribute("templateKey", templateKey);
        model.addAttribute("templateLocale", templateLocale);
        return EmailTemplateConstants.INDEX_TEMPLATE;
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET, produces = "application/json")
    public String showEdit(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "id", required = true) Long id) throws IOException {
        localeResolver(request, response);
        EmailTemplate emailTemplate = emailTemplateService.getEmailTemplateById(id);
        model.addAttribute("emailTemplate", emailTemplate);
        return EmailTemplateConstants.EDIT_TEMPLATE;
    }

    @RequestMapping(value = "/new")
    public String showNew(Model model, HttpServletRequest request, HttpServletResponse response) {
        localeResolver(request, response);
        EmailTemplate emailTemplate = new EmailTemplate();
        model.addAttribute("emailTemplate", emailTemplate);
        return EmailTemplateConstants.EDIT_TEMPLATE;
    }

    @RequestMapping(value = {"/new/formsubmit", "/edit/{id}/formsubmit"}, method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    ResponseEntity<String> emailTemplateSubmit(HttpServletRequest req, Model model, @PathVariable(value = "id", required = false) Long id, @RequestBody String requestString, HttpServletResponse response) {
        ResponseEntity<String> responseBody;

        String userId = sessionManager.getCurrentSessionUserId();
        Locale loc = localeResolver(req, response);
        JSONObject jsonResponse = new JSONObject();
        try {
            String jsonParam = requestString;
            if (StringUtils.isBlank(jsonParam)) {
                jsonParam = "[]";
            }

            ObjectMapper objectMapper = new ObjectMapper();
            EmailTemplate emailTemplate;
            if (id != null) {
                emailTemplate = emailTemplateService.getEmailTemplateById(id);
            } else {
                emailTemplate = new EmailTemplate();
                emailTemplate.setOwner(userId);
            }
            String jsonString = objectMapper.writeValueAsString(emailTemplate);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonString);

            JSONObject modifiedJson = (JSONObject) parser.parse(jsonParam);

            Set<Object> modifiedJsonKeys = modifiedJson.keySet();
            modifiedJsonKeys.forEach(dynamicKey -> {
                Object line = modifiedJson.get(dynamicKey);
                json.put(dynamicKey, line);
            });

            emailTemplate = objectMapper.readValue(json.toJSONString(), EmailTemplate.class);

            List<String> errors = getErrors(emailTemplate, req, response);
            if (errors.isEmpty()) {
                emailTemplateService.saveTemplate(emailTemplate);
                jsonResponse.put("status", "SUCCESS");
                jsonResponse.put("message", messageSource.getMessage("template.saved.message", new String[]{emailTemplate.getKey(), emailTemplate.getLocale()}, loc));
                jsonResponse.put("title",messageSource.getMessage("modifyemail.modify.template.header",  new String[]{emailTemplate.getKey(), emailTemplate.getLocale()}, loc));
                responseBody = ResponseEntity.status(HttpStatus.OK).body(jsonResponse.toJSONString());

            } else {
                JSONArray errorsArray = new JSONArray();
                for (String error : errors) {
                    errorsArray.add(error);
                }
                jsonResponse.put("status", "ERROR");
                jsonResponse.put("errors", errorsArray);
                responseBody = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResponse.toJSONString());
            }

        } catch (Exception e) {
            JSONArray errorsArray = new JSONArray();
            errorsArray.add(messageSource.getMessage("GeneralActionError", null, loc));
            jsonResponse.put("status", "ERROR");
            jsonResponse.put("errors", errorsArray);
            responseBody = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResponse.toJSONString());
        }

        return responseBody;
    }

    private List<String> getErrors(EmailTemplate emailTemplate, HttpServletRequest request, HttpServletResponse response) {
        List<String> errors = new ArrayList<>();
        Locale loc = localeResolver(request, response);
        Locale templateLocale = null;
        
        if (StringUtils.isBlank(emailTemplate.getLocale())) {
            emailTemplate.setLocale(EmailTemplate.DEFAULT_LOCALE);
        }

        // check to see if this template already exists
        if (!StringUtils.equals(emailTemplate.getLocale(), EmailTemplate.DEFAULT_LOCALE)) {
            try {
                templateLocale = LocaleUtils.toLocale(emailTemplate.getLocale());
            } catch (IllegalArgumentException ie) {
                errors.add(messageSource.getMessage("error.invalidlocale", null, loc));
            }
        }

        //key can't be null
        if (StringUtils.isBlank(emailTemplate.getKey())) {
            errors.add(messageSource.getMessage("error.nokey", null, loc));
        } else if (emailTemplateService.templateExists(emailTemplate.getKey(), templateLocale)) {
            errors.add(messageSource.getMessage("error.duplicatekey", null, loc));
        }

        if (StringUtils.isBlank(emailTemplate.getSubject())) {
            errors.add(messageSource.getMessage("error.nosubject", null, loc));
        }

        if (StringUtils.isBlank(emailTemplate.getMessage())) {
            errors.add(messageSource.getMessage("error.nomessage", null, loc));
        }
        return errors;
    }
}
