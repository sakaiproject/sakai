/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
           http://opensource.org/licenses/ecl2
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/

package org.sakaiproject.datemanager.tool;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.sakaiproject.datemanager.api.DateManagerConstants;
import org.sakaiproject.datemanager.api.DateManagerService;
import org.sakaiproject.datemanager.api.model.DateManagerError;
import org.sakaiproject.datemanager.api.model.DateManagerValidation;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;

/**
 * MainController
 *
 * This is the controller used by Spring MVC to handle requests
 *
 */
@Slf4j
@Controller
public class MainController {

    @Inject private DateManagerService dateManagerService;
	
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private PreferencesService preferencesService;

    @GetMapping(value = {"/", "/index"})
    public String showIndex(@RequestParam(required=false) String code, Model model, HttpServletRequest request, HttpServletResponse response) {

        String userId = sessionManager.getCurrentSessionUserId();
        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);

        String siteId = dateManagerService.getCurrentSiteId();

		model.addAttribute("userCountry", loc.getCountry());
		model.addAttribute("userLanguage", loc.getLanguage());
		model.addAttribute("userLocale", loc.toString());

		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ASSIGNMENTS)) {
			JSONArray assignmentsJson = dateManagerService.getAssignmentsForContext(siteId);
			model.addAttribute("assignments", assignmentsJson);
			model.addAttribute("assignmentsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_ASSIGNMENTS));
			log.debug("assignments {}", assignmentsJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ASSESSMENTS)) {
			JSONArray assessmentsJson = dateManagerService.getAssessmentsForContext(siteId);
			model.addAttribute("assessments", assessmentsJson);
			model.addAttribute("assessmentsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_ASSESSMENTS));
			log.debug("assessments {}", assessmentsJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_GRADEBOOK)) {
			JSONArray gradebookItemsJson = dateManagerService.getGradebookItemsForContext(siteId);
			model.addAttribute("gradebookItems", gradebookItemsJson);
			model.addAttribute("gradebookItemsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_GRADEBOOK));
			log.debug("gradebookItemsJson {}", gradebookItemsJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_SIGNUP)) {
			JSONArray signupMeetingsJson = dateManagerService.getSignupMeetingsForContext(siteId);
			model.addAttribute("signupMeetings", signupMeetingsJson);
			model.addAttribute("signupMeetingsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_SIGNUP));
			log.debug("signupMeetingsJson {}", signupMeetingsJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_RESOURCES)) {
			JSONArray resourcesJson = dateManagerService.getResourcesForContext(siteId);
			model.addAttribute("resources", resourcesJson);
			model.addAttribute("resourcesToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_RESOURCES));
			log.debug("resourcesJson {}", resourcesJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_CALENDAR)) {
			JSONArray calendarJson = dateManagerService.getCalendarEventsForContext(siteId);
			model.addAttribute("calendarEvents", calendarJson);
			model.addAttribute("calendarEventsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_CALENDAR));
			log.debug("calendarJson {}", calendarJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_FORUMS)) {
			JSONArray forumsJson = dateManagerService.getForumsForContext(siteId);
			model.addAttribute("forums", forumsJson);
			model.addAttribute("forumsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_FORUMS));
			log.debug("forums {}", forumsJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS)) {
			JSONArray announcementsJson = dateManagerService.getAnnouncementsForContext(siteId);
			model.addAttribute("announcements", announcementsJson);
			model.addAttribute("announcementsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS));
			log.debug("announcementsJson {}", announcementsJson);
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_LESSONS)) {
			JSONArray lessonsJson = dateManagerService.getLessonsForContext(siteId);
			model.addAttribute("lessons", lessonsJson);
			model.addAttribute("lessonsToolTitle", dateManagerService.getToolTitle(DateManagerConstants.COMMON_ID_LESSONS));
			log.debug("lessonsJson {}", lessonsJson);
		}

		return "index";
	}

	@RequestMapping(value = {"/date-manager/update"}, method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String dateManagerUpdate(HttpServletRequest req, Model model, @RequestBody String requestString) {
		String jsonResponse = "";
		try {
			String siteId = req.getRequestURI().split("/")[3];

			String jsonParam = requestString;
			if (StringUtils.isBlank(jsonParam)) jsonParam = "[]";
			Object json = new JSONParser().parse(jsonParam);

			if (!(json instanceof JSONObject)) {
				log.error("Error no json object");
				jsonResponse = String.format("{\"status\": \"ERROR\", \"error\": \"%s\"}", dateManagerService.getMessage("error.uncaught"));
				return jsonResponse;
			}

			JSONArray assignments = (JSONArray) ((JSONObject) json).get("assignments");
			DateManagerValidation assignmentValidate = dateManagerService.validateAssignments(siteId, assignments);
			JSONArray assessments = (JSONArray) ((JSONObject) json).get("assessments");
			DateManagerValidation assessmentValidate = dateManagerService.validateAssessments(siteId, assessments);
			JSONArray gradebookItems = (JSONArray) ((JSONObject) json).get("gradebookItems");
			DateManagerValidation gradebookValidate = dateManagerService.validateGradebookItems(siteId, gradebookItems);
			JSONArray signupMeetings = (JSONArray) ((JSONObject) json).get("signupMeetings");
			DateManagerValidation signupValidate = dateManagerService.validateSignupMeetings(siteId, signupMeetings);
			JSONArray resources = (JSONArray) ((JSONObject) json).get("resources");
			DateManagerValidation resourcesValidate = dateManagerService.validateResources(siteId, resources);
			JSONArray calendarEvents = (JSONArray) ((JSONObject) json).get("calendarEvents");
			DateManagerValidation calendarValidate = dateManagerService.validateCalendarEvents(siteId, calendarEvents);
			JSONArray forums = (JSONArray) ((JSONObject) json).get("forums");
			DateManagerValidation forumValidate = dateManagerService.validateForums(siteId, forums);
			JSONArray announcements = (JSONArray) ((JSONObject) json).get("announcements");
			DateManagerValidation announcementValidate = dateManagerService.validateAnnouncements(siteId, announcements);
			JSONArray lessons = (JSONArray) ((JSONObject) json).get("lessons");
			DateManagerValidation lessonsValidate = dateManagerService.validateLessons(siteId, lessons);

			if (assignmentValidate.getErrors().isEmpty() &&
					assessmentValidate.getErrors().isEmpty() &&
					gradebookValidate.getErrors().isEmpty() &&
					signupValidate.getErrors().isEmpty() &&
					resourcesValidate.getErrors().isEmpty() &&
					calendarValidate.getErrors().isEmpty() &&
					forumValidate.getErrors().isEmpty() &&
					announcementValidate.getErrors().isEmpty() &&
					lessonsValidate.getErrors().isEmpty()) {

				dateManagerService.updateAssignments(assignmentValidate);
				dateManagerService.updateAssessments(assessmentValidate);
				dateManagerService.updateGradebookItems(gradebookValidate);
				dateManagerService.updateSignupMeetings(signupValidate);
				dateManagerService.updateResources(resourcesValidate);
				dateManagerService.updateCalendarEvents(calendarValidate);
				dateManagerService.updateForums(forumValidate);
				dateManagerService.updateAnnouncements(announcementValidate);
				dateManagerService.updateLessons(lessonsValidate);
				jsonResponse = "{\"status\": \"OK\"}";
			} else {
				JSONArray errorReport = new JSONArray();
				List<DateManagerError> errors = new ArrayList<>();
				errors.addAll(assignmentValidate.getErrors());
				errors.addAll(assessmentValidate.getErrors());
				errors.addAll(gradebookValidate.getErrors());
				errors.addAll(signupValidate.getErrors());
				errors.addAll(resourcesValidate.getErrors());
				errors.addAll(calendarValidate.getErrors());
				errors.addAll(forumValidate.getErrors());
				errors.addAll(announcementValidate.getErrors());
				errors.addAll(lessonsValidate.getErrors());

				for (DateManagerError error : errors) {
					JSONObject jsonError = new JSONObject();
					jsonError.put("field", error.field);
					jsonError.put("msg", error.msg);
					jsonError.put("toolId", error.toolId);
					jsonError.put("toolTitle", error.toolTitle);
					jsonError.put("idx", error.idx);
					errorReport.add(jsonError);
				}

				jsonResponse = String.format("{\"status\": \"ERROR\", \"errors\": %s}", errorReport.toJSONString());
			}

		} catch (Exception e) {
			log.error("Error updating dates", e);
			jsonResponse = String.format("{\"status\": \"ERROR\", \"error\": \"%s\"}", dateManagerService.getMessage("error.uncaught") + ": " + e.getMessage());
		}
		return jsonResponse;
	}
}
