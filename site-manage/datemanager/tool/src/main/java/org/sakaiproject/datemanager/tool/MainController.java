/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.datemanager.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
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
import org.springframework.web.bind.annotation.PostMapping;
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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import org.springframework.http.HttpHeaders;

import org.sakaiproject.assignment.api.model.Assignment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
/**
 * MainController
 *
 * This is the controller used by Spring MVC to handle requests
 *
 */
@Slf4j
@Controller
public class MainController {

    private static final String BOM = "\uFEFF";

	private String[] columnsCsvStrings = {"id", "title", "open.date", "available.date", "start.date", "show.from.date", 
			"hide.until", "due.date", "end.date", "accept.until", "show.until", "close.date", "feedback.start.date", 
			"feedback.end.date", "signup.begins.date", "signup.deadline.date"};
	
	private static final ResourceLoader rb = new ResourceLoader("Messages");

    @Inject private DateManagerService dateManagerService;
	
    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SiteService siteService;
    
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

	@GetMapping(value = {"/date-manager/export"})
	public ResponseEntity<byte[]> exportCsv(HttpServletRequest req) {
		
		String siteId = req.getRequestURI().split("/")[3];

		ByteArrayOutputStream gradesBAOS = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(gradesBAOS, Charset.forName("UTF-8"));
        try {
            osw.write(BOM);
        } catch (IOException e) {
            // tried
        }
		char csvSep = ';';
		CSVWriter gradesBuffer = new CSVWriter(osw, csvSep, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
		this.addRow(gradesBuffer, "Date Manager");
		
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ASSIGNMENTS)) {
			JSONArray assignmentsJson = dateManagerService.getAssignmentsForContext(siteId);
			if (assignmentsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 2, 7, 9};
				String[] columnsNames = {"id", "title", "open_date", "due_date", "accept_until"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_ASSIGNMENTS, columnsIndex, assignmentsJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ASSESSMENTS)) {
			JSONArray assessmentsJson = dateManagerService.getAssessmentsForContext(siteId);
			if (assessmentsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 3, 7, 9, 12, 13};
				String[] columnsNames = {"id", "title", "open_date", "due_date", "accept_until", "feedback_start", "feedback_end"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_ASSESSMENTS, columnsIndex, assessmentsJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_GRADEBOOK)) {
			JSONArray gradebookItemsJson = dateManagerService.getGradebookItemsForContext(siteId);
			if (gradebookItemsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 7};
				String[] columnsNames = {"id", "title", "due_date"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_GRADEBOOK, columnsIndex, gradebookItemsJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_SIGNUP)) {
			JSONArray signupMeetingsJson = dateManagerService.getSignupMeetingsForContext(siteId);
			if (signupMeetingsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 4, 8, 14, 15};
				String[] columnsNames = {"id", "title", "open_date", "due_date", "signup_begins", "signup_deadline"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_SIGNUP, columnsIndex, signupMeetingsJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_RESOURCES)) {
			JSONArray resourcesJson = dateManagerService.getResourcesForContext(siteId);
			if (resourcesJson.size() > 0) {
				int[] columnsIndex = {0, 1, 5, 10};
				String[] columnsNames = {"id", "title", "open_date", "due_date"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_RESOURCES, columnsIndex, resourcesJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_CALENDAR)) {
			JSONArray calendarJson = dateManagerService.getCalendarEventsForContext(siteId);
			if (calendarJson.size() > 0) {
				int[] columnsIndex = {0, 1, 4, 8};
				String[] columnsNames = {"id", "title", "open_date", "due_date"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_CALENDAR, columnsIndex, calendarJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_FORUMS)) {
			JSONArray forumsJson = dateManagerService.getForumsForContext(siteId);
			if (forumsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 2, 11};
				String[] columnsNames = {"id", "title", "open_date", "due_date"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_FORUMS, columnsIndex, forumsJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS)) {
			JSONArray announcementsJson = dateManagerService.getAnnouncementsForContext(siteId);
			if (announcementsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 4, 8};
				String[] columnsNames = {"id", "title", "open_date", "due_date"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_ANNOUNCEMENTS, columnsIndex, announcementsJson, columnsNames);
			}
		}
		if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_LESSONS)) {
			JSONArray lessonsJson = dateManagerService.getLessonsForContext(siteId);
			if (lessonsJson.size() > 0) {
				int[] columnsIndex = {0, 1, 6};
				String[] columnsNames = {"id", "title", "open_date"};
				this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_LESSONS, columnsIndex, lessonsJson, columnsNames);
			}
		}

		try {
			gradesBuffer.close();
		} catch(Exception ex) {
            // tried
		}
        HttpHeaders headers = new HttpHeaders();
		String siteName;
		try {
			Site site = siteService.getSite(siteId);
			siteName = site.getTitle();
		} catch (Exception e) {
			siteName = "";
		}
		String name = siteName + "_date_manager_export.csv";
        headers.setContentDispositionFormData("filename", name.replaceAll(" ", "_"));

        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("text/csv"))
                .headers(headers)
                .body(gradesBAOS.toByteArray());
    }
	
    public void addRow(CSVWriter gradesBuffer, String... values) {
        gradesBuffer.writeNext(values);
    }

	// public void createCsvSection(CSVWriter gradesBuffer, String toolId, int[] columnsIndex) {
	public void createCsvSection(CSVWriter gradesBuffer, String toolId, int[] columnsIndex, JSONArray toolJson, String[] columnsNames) {
		String lessonsToolTitle = dateManagerService.getToolTitle(toolId);
		this.addRow(gradesBuffer, "");
		this.addRow(gradesBuffer, lessonsToolTitle);
		String[] columnsStringArray = new String[columnsIndex.length];
		for (int i = 0; i < columnsIndex.length; i++) {
			String columnMessage = rb.getString("column." + columnsCsvStrings[columnsIndex[i]]);
			columnsStringArray[i] = (i+1==columnsIndex.length)? columnMessage : columnMessage;
		}
		this.addRow(gradesBuffer, columnsStringArray);
		for (int i = 0; i < toolJson.size(); i++) {
			String[] toolColumns = new String[columnsIndex.length];
			for (int j = 0; j < columnsNames.length; j++){
				Object toolInfoObject = ((JSONObject) toolJson.get(i)).get(columnsNames[j]);
				if (toolInfoObject != null && toolInfoObject.getClass().getName().equals("java.lang.Long")) {
					String toolInfoString = String.valueOf(toolInfoObject);
					toolColumns[j] = toolInfoString;
				} else {
					String toolInfoString = ((String) toolInfoObject);
					toolColumns[j] = toolInfoString != null? toolInfoString : "";
				}
			}
			this.addRow(gradesBuffer, toolColumns);
		}
	}

	@GetMapping(value = {"/date-manager/page/import"})
    public String showImportPage(@RequestParam(required=false) String code, Model model, HttpServletRequest request, HttpServletResponse response) {
		// model.addAttribute("lessons", lessonsJson);
        String siteId = dateManagerService.getCurrentSiteId();
		String userId = sessionManager.getCurrentSessionUserId();

        final Locale loc = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
		
		System.out.println("loc.getCountry()");
		System.out.println(loc.getCountry());
		System.out.println(loc.getLanguage());
		System.out.println(loc.toString());
		model.addAttribute("userCountry", loc.getCountry());
		model.addAttribute("userLanguage", loc.getLanguage());
		model.addAttribute("userLocale", loc.toString());

		return "import_page";
	}
	
	@PostMapping(value = {"/import/dates"})
	// public String importDates(@RequestParam("file") MultipartFile file, Model model, HttpServletRequest request){
	public String importDates(Model model, HttpServletRequest request, HttpServletResponse response){
		System.out.println("String recibido: ");
		System.out.println("String recibido: ");
		System.out.println("String recibido: ");
		// System.out.println("String recibido: " + file.getName());
		// System.out.println("String recibido: " + file.getSize());
		System.out.println("String recibido: ");
		System.out.println("String recibido: ");
		System.out.println("String recibido: ");

		// if (file.getSize() > 0 && !file.isEmpty()) {
		if (1 == 1) {
			ArrayList tools = new ArrayList<>();
			for (int i = 0; i < 8; i++) {
				ArrayList tool = new ArrayList<>();
				tool.add("Titulo " + i);
				ArrayList toolContents = new ArrayList<>();
				int cantidad = (i % 2 == 0)? 7 : 9; 
				int cantidad2 = (i % 2 == 0)? 3 : 5; 
				ArrayList toolTitleContent = new ArrayList<>();
				for (int q = 0; q < cantidad2; q++) {
					toolTitleContent.add("Titulo contenido " + q);
				}
				tool.add(toolTitleContent);
				for (int j = 0; j < cantidad; j++) {
					ArrayList toolContent = new ArrayList<>();
					for (int k = 0; k < cantidad2; k++) {
						toolContent.add("Contenido " + k);
					}
					toolContents.add(toolContent);
				}
				tool.add(toolContents);
				tools.add(tool);
			}

			model.addAttribute("tools", tools);
			return "confirm_import";
		} else {
			model.addAttribute("errorMessage", "Cannot use an empty or inexistent file");
			return "import_page";
		}
	}
}
