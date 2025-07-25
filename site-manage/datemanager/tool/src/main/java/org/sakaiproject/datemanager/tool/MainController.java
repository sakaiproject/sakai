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
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.sakaiproject.datemanager.api.model.ImportPreviewResult;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;


/**
 * MainController
 *
 * This is the controller used by Spring MVC to handle requests
 *
 */
@Slf4j
@Controller
public class MainController {

    private static final ResourceLoader rb = new ResourceLoader("Messages");

    @Inject private DateManagerService dateManagerService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SiteService siteService;
    
    @Autowired
    private PreferencesService preferencesService;

    private Map<String, List<String[]>> toolsToImport = new HashMap<>();


    /**
     * Sets the locale for the current site and user in the model.
     *
     * @param model    The model.
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @return The updated model.
     */
    public Model getModelWithLocale(Model model, HttpServletRequest request, HttpServletResponse response) {
        final Locale loc = dateManagerService.getLocaleForCurrentSiteAndUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);

        String siteId = dateManagerService.getCurrentSiteId();

        model.addAttribute("userCountry", loc.getCountry());
        model.addAttribute("userLanguage", loc.getLanguage());
        model.addAttribute("userLocale", loc.toString());

        return model;
    }

    /**
     * Shows the main page, listing all the items with dates for the current site.
     *
     * @param code     Optional request parameter.
     * @param model    The model.
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @return The name of the index view.
     */
    @GetMapping(value = {"/", "/index"})
    public String showIndex(@RequestParam(required=false) String code, Model model, HttpServletRequest request, HttpServletResponse response) {
		String siteId = dateManagerService.getCurrentSiteId();
		model = getModelWithLocale(model, request, response);

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

	/**
	 * Handles the update of dates from the main UI.
	 *
	 * @param req           The HTTP request.
	 * @param model         The model.
	 * @param requestString The JSON string with the date updates.
	 * @return A JSON string with the status of the operation.
	 */
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
				// Unlock any locks initiated by Resources, Calendar, and Announcements tools
				dateManagerService.clearUpdateResourceLocks(resourcesValidate);
				dateManagerService.clearUpdateCalendarLocks(calendarValidate);
				dateManagerService.clearUpdateAnnouncementLocks(announcementValidate);
			}

		} catch (Exception e) {
			log.error("Error updating dates", e);
			jsonResponse = String.format("{\"status\": \"ERROR\", \"error\": \"%s\"}", dateManagerService.getMessage("error.uncaught") + ": " + e.getMessage());
		}
		return jsonResponse;
	}

	/**
	 * Exports the current date information into a CSV file.
	 *
	 * @param req The HTTP request.
	 * @return A {@link ResponseEntity} containing the CSV file as a byte array.
	 */
	@GetMapping(value = {"/date-manager/export"})
	public ResponseEntity<byte[]> exportCsv(HttpServletRequest req) {
		String siteId = dateManagerService.getCurrentSiteId();

		byte[] csvData = dateManagerService.exportToCsv(siteId);

		HttpHeaders headers = new HttpHeaders();
		String siteName;
		try {
			Site site = siteService.getSite(siteId);
			siteName = site.getTitle();
		} catch (Exception e) {
			siteName = "";
		}
		String filename = siteName + "_date_manager_export.csv";
		headers.setContentDispositionFormData("filename", filename.replaceAll(" ", "_"));

		return ResponseEntity
				.ok()
				.contentType(MediaType.valueOf("text/csv"))
				.headers(headers)
				.body(csvData);
		}

	/**
	 * Shows the import page.
	 *
	 * @param model The model.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @return The name of the import page view.
	 */
	@GetMapping(value = {"/date-manager/page/import"})
	public String showImportPage(Model model, HttpServletRequest request, HttpServletResponse response) {
		String siteId = dateManagerService.getCurrentSiteId();
		String userId = sessionManager.getCurrentSessionUserId();

		model = getModelWithLocale(model, request, response);

		return "import_page";
	}

	/**
	 * Imports dates from the provided CSV file content.
	 *
	 * @param model The model.
	 * @param request The HTTP request containing the uploaded file.
	 * @param response The HTTP response.
	 * @return The name of the view to show (confirmation or back to import page with errors).
	 */
	@PostMapping(value = {"/import/dates"}, consumes = "multipart/form-data")
	public String importDates(Model model, HttpServletRequest request, HttpServletResponse response) {
		FileItem uploadedFileItem = (FileItem) request.getAttribute("file");
		model = getModelWithLocale(model, request, response);

		try {
			if (uploadedFileItem == null || uploadedFileItem.getSize() == 0) {
				model.addAttribute("errorMessage", rb.getString("page.import.error.any.date"));
				return "import_page";
			}

			String siteId = dateManagerService.getCurrentSiteId();
			ImportPreviewResult previewResult = dateManagerService.previewCsvImport(siteId, uploadedFileItem.getInputStream());
			
			if (previewResult.hasErrors()) {
				model.addAttribute("errorMessage", previewResult.getErrorMessage());
				return "import_page";
			}
			
			if (!previewResult.hasChanges()) {
				model.addAttribute("errorMessage", rb.getString("page.import.error.any.date"));
				return "import_page";
			}
			
			// Store the import data for confirmation
			toolsToImport = previewResult.getToolsToImport();
			
			// Show confirmation page
			return showConfirmImport(model, request, response);
			
		} catch (Exception e) {
			log.error("Error processing file upload", e);
			model.addAttribute("errorMessage", rb.getString("page.import.error.any.date"));
			return "import_page";
		}
	}

	/**
	 * Shows the confirmation page for CSV import.
	 *
	 * @param model The model.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @return The name of the confirmation view.
	 */
	public String showConfirmImport(Model model, HttpServletRequest request, HttpServletResponse response) {
		model = getModelWithLocale(model, request, response);
		
		List<ToolImportData> importDataList = new ArrayList<>();
		for (Map.Entry<String, List<String[]>> entry : toolsToImport.entrySet()) {
			importDataList.add(new ToolImportData(entry.getKey(), entry.getValue()));
		}
		
		model.addAttribute("toolsToImport", importDataList);
		
		return "confirm_import";
	}

	/**
	 * Confirms and applies the CSV import changes.
	 *
	 * @param model The model.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @return The name of the view to show after confirmation.
	 */
	@PostMapping(value = {"/confirm/update"})
	public String confirmUpdate(Model model, HttpServletRequest request, HttpServletResponse response) {
		model = getModelWithLocale(model, request, response);
		
		try {
			// Apply the import using the stored toolsToImport data
			for (Map.Entry<String, List<String[]>> entry : toolsToImport.entrySet()) {
				String toolId = entry.getKey();
				List<String[]> toolData = entry.getValue();
				
				if (toolData.size() > 1) { // Must have header + data rows
					// Convert to array format expected by service
					String[][] toolColumns = new String[toolData.size()][50];
					String[] toolColumnsAux = new String[50];
					
					for (int i = 0; i < toolData.size(); i++) {
						String[] row = toolData.get(i);
						for (int j = 0; j < row.length && j < 50; j++) {
							toolColumns[i][j] = row[j];
							if (i == 0) {
								toolColumnsAux[j] = row[j]; // Store headers
							}
						}
					}
					
					// Validate and update the tool
					DateManagerValidation validation = dateManagerService.validateTool(toolId, toolData.size() - 1, toolColumns, toolColumnsAux);
					if (validation != null) {
						dateManagerService.updateTool(toolId, validation);
					}
				}
			}
			
			// Clear the import data
			toolsToImport.clear();
			
			return this.showIndex("", model, request, response);
			
		} catch (Exception e) {
			log.error("Error confirming CSV import", e);
			model.addAttribute("errorMessage", rb.getString("page.import.error.any.date"));
			return "import_page";
		}
	}

	
}
