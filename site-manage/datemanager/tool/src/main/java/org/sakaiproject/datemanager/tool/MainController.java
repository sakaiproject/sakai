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

import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Arrays;
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
import org.springframework.http.HttpStatus;
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
import org.sakaiproject.component.api.ServerConfigurationService;
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

    private static final String BOM = "\uFEFF";

    private String[] columnsCsvStrings = {"id", "title", "open.date.required", "open.date.optional", "available.date", "available.date.required", 
            "start.date", "start.date.required", "start.date.optional", "show.from.date.optional","hide.until.optional", "due.date",
			"due.date.required", "due.date.optional", "end.date", "end.date.required", "end.date.optional", "assessments.accept.until",
            "accept.until.required", "show.until.optional", "close.date.optional", "feedback.start.date","feedback.end.date", "signup.begins.date",
			"signup.deadline.date", "extra.info"};

    private String[][] columnsNames = {{"id", "title", "open_date", "due_date", "accept_until"},
             {"id", "title", "open_date", "due_date", "accept_until", "feedback_start", "feedback_end"},
             {"id", "title", "due_date"}, 
             {"id", "title", "open_date", "due_date", "signup_begins", "signup_deadline"},
             {"id", "title", "open_date", "due_date"},
             {"id", "title", "open_date", "due_date", "extraInfo"},
             {"id", "title", "open_date"}};
    
    private static final ResourceLoader rb = new ResourceLoader("Messages");

    @Inject private DateManagerService dateManagerService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SiteService siteService;
    
    @Autowired
    private PreferencesService preferencesService;
    
    @Autowired
    private ServerConfigurationService serverConfigurationService;

    private ArrayList<String[]> toolsInfoArray;

    private ArrayList tools;

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
	 * Function that export the current information into a csv file
	 */
	@GetMapping(value = {"/date-manager/export"})
	public ResponseEntity<byte[]> exportCsv(HttpServletRequest req) {

		String siteId = req.getRequestURI().split("/")[3];

		ByteArrayOutputStream gradesBAOS = new ByteArrayOutputStream();
		char csvSep = getCsvSeparatorChar();
		try (
			OutputStreamWriter osw = new OutputStreamWriter(gradesBAOS, StandardCharsets.UTF_8);
			CSVWriter gradesBuffer = new CSVWriter(osw, csvSep, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END)
		) {
			osw.write(BOM);
			this.addRow(gradesBuffer, "Date Manager");

			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ASSIGNMENTS)) {
				JSONArray assignmentsJson = dateManagerService.getAssignmentsForContext(siteId);
				if (assignmentsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 2, 12, 18};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_ASSIGNMENTS, columnsIndex, assignmentsJson, columnsNames[0]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ASSESSMENTS)) {
				JSONArray assessmentsJson = dateManagerService.getAssessmentsForContext(siteId);
				if (assessmentsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 5, 11, 17, 21, 22};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_ASSESSMENTS, columnsIndex, assessmentsJson, columnsNames[1]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_GRADEBOOK)) {
				JSONArray gradebookItemsJson = dateManagerService.getGradebookItemsForContext(siteId);
				if (gradebookItemsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 13};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_GRADEBOOK, columnsIndex, gradebookItemsJson, columnsNames[2]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_SIGNUP)) {
				JSONArray signupMeetingsJson = dateManagerService.getSignupMeetingsForContext(siteId);
				if (signupMeetingsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 6, 14, 23, 24};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_SIGNUP, columnsIndex, signupMeetingsJson, columnsNames[3]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_RESOURCES)) {
				JSONArray resourcesJson = dateManagerService.getResourcesForContext(siteId);
				if (resourcesJson.size() > 0) {
					int[] columnsIndex = {0, 1, 9, 19, 25};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_RESOURCES, columnsIndex, resourcesJson, columnsNames[5]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_CALENDAR)) {
				JSONArray calendarJson = dateManagerService.getCalendarEventsForContext(siteId);
				if (calendarJson.size() > 0) {
					int[] columnsIndex = {0, 1, 7, 15};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_CALENDAR, columnsIndex, calendarJson, columnsNames[4]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_FORUMS)) {
				JSONArray forumsJson = dateManagerService.getForumsForContext(siteId);
				if (forumsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 3, 20, 25};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_FORUMS, columnsIndex, forumsJson, columnsNames[5]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS)) {
				JSONArray announcementsJson = dateManagerService.getAnnouncementsForContext(siteId);
				if (announcementsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 8, 16};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_ANNOUNCEMENTS, columnsIndex, announcementsJson, columnsNames[4]);
				}
			}
			if (dateManagerService.currentSiteContainsTool(DateManagerConstants.COMMON_ID_LESSONS)) {
				JSONArray lessonsJson = dateManagerService.getLessonsForContext(siteId);
				if (lessonsJson.size() > 0) {
					int[] columnsIndex = {0, 1, 10};
					this.createCsvSection(gradesBuffer, DateManagerConstants.COMMON_ID_LESSONS, columnsIndex, lessonsJson, columnsNames[6]);
				}
			}
		} catch (Exception ex) {
			log.error("Cannot create the csv file", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
	
	/**
	 * Void function to add a row to a csv file using the sent values
	 * 
	 * @param gradesBuffer - CSVWriter - csv 'file'
	 * @param values - String (single and array) - values to add
	 */
	public void addRow(CSVWriter gradesBuffer, String... values) {
		gradesBuffer.writeNext(values);
	}
	
	/**
	 * Helper method to get the configured CSV separator
	 * 
	 * @return String - the CSV separator character as string
	 */
	private String getCsvSeparator() {
		return serverConfigurationService.getString("csv.separator", ",");
	}
	
	/**
	 * Helper method to get the configured CSV separator as char
	 * 
	 * @return char - the CSV separator character
	 */
	private char getCsvSeparatorChar() {
		return getCsvSeparator().charAt(0);
	}

	/**
	 * Void function to add a section of rows to a csv file using the sent values
	 * 
	 * @param gradesBuffer - CSVWriter - csv 'file'
	 * @param toolId - String - the toolId
	 * @param columnsIndex - int[] - the indexes to get the "columnsCsvStrings" String of the Resource Loader of the Title
	 * @param toolJson - JSONArray - tool information
	 * @param columnsNames - String[] - the 'indexes' to get the information from the toolJson
	 */
	public void createCsvSection(CSVWriter gradesBuffer, String toolId, int[] columnsIndex, JSONArray toolJson, String[] columnsNames) {
		String toolTitle = dateManagerService.getToolTitle(toolId);
		this.addRow(gradesBuffer, "");
		this.addRow(gradesBuffer, toolId + "(" + toolTitle + ")");
		String[] columnsStringArray = new String[columnsIndex.length];
		for (int i = 0; i < columnsIndex.length; i++) {
			String columnMessage = rb.getString("column." + columnsCsvStrings[columnsIndex[i]]);
			columnsStringArray[i] = columnMessage;
		}
		this.addRow(gradesBuffer, columnsStringArray);
		for (int i = 0; i < toolJson.size(); i++) {
			String[] toolColumns = new String[columnsIndex.length];
			for (int j = 0; j < columnsNames.length; j++){
				Object toolInfoObject = ((JSONObject) toolJson.get(i)).get(columnsNames[j]);
				if (toolInfoObject != null && toolInfoObject.getClass().getName().equals("java.lang.Long")) {
					String toolInfoString = String.valueOf(toolInfoObject);
					toolColumns[j] = toolInfoString;
				} else if (toolInfoObject instanceof Timestamp){
					String toolInfoString = ((Timestamp) toolInfoObject).toString();
					toolColumns[j] = toolInfoString != null? toolInfoString : "";
				} else {
					String toolInfoString = ((String) toolInfoObject);
					if (columnsNames[j].equals("title")) {
						toolInfoString = toolInfoString.replaceAll("[;,\"]", "_");
					}
					String extraInfo = (String) ((JSONObject) toolJson.get(i)).get(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME);
					if (columnsNames[j].equals("title") && extraInfo != null && extraInfo.contains(rb.getString("itemtype.draft"))) {
						toolInfoString += " (" + rb.getString("itemtype.draft") + ")";
					}
					if (DateManagerConstants.COMMON_ID_GRADEBOOK.equals(toolId) && !columnsNames[j].equals("title")) {
						toolInfoString = toolInfoString.split("T")[0];
					}
					toolColumns[j] = toolInfoString != null? toolInfoString : "";
				}
			}
			this.addRow(gradesBuffer, toolColumns);
		}
	}

	/**
	 * Function to show the import page
	 * 
	 * @param Model - model
	 * @param HttpServletRequest - request
	 * @param HttpServletResponse - response
	 * 
	 * @return String - the page to show
	 */
	@GetMapping(value = {"/date-manager/page/import"})
	public String showImportPage(Model model, HttpServletRequest request, HttpServletResponse response) {
		String siteId = dateManagerService.getCurrentSiteId();
		String userId = sessionManager.getCurrentSessionUserId();

		model = getModelWithLocale(model, request, response);

		return "import_page";
	}

	/**
	 * Function to import the sent Dates using the sent csv file content
	 * 
	 * @param String - requestCsvContent - the csv file content
	 * @param Model - model
	 * @param HttpServletRequest - request
	 * 
	 * @return ResponseEntity<String> - the status code and String (only fo the failed cases)
	 */
	@PostMapping(value = {"/import/dates"}, consumes = "multipart/form-data")
	public String importDates(Model model, HttpServletRequest request, HttpServletResponse response) {
		FileItem uploadedFileItem = (FileItem) request.getAttribute("file");
		toolsInfoArray = new ArrayList<String[]>();
		try (
			// Create CSVReader with the configured separator
			InputStreamReader inputReader = new InputStreamReader(uploadedFileItem.getInputStream(), StandardCharsets.UTF_8);
			CSVReader reader = new CSVReaderBuilder(inputReader)
				.withCSVParser(new CSVParserBuilder().withSeparator(getCsvSeparatorChar()).build())
				.build()
		) {
			tools = new ArrayList<>();
			
			ArrayList tool = new ArrayList<>();
			ArrayList toolHeader = new ArrayList<>();
			ArrayList toolContent = new ArrayList<>();
	
			boolean isHeader = false;
			boolean hasChanged = false;
			String currentToolId = "";
			int idx = 0;

			String[] nextLine;
			
			// Skip the first line ("Date Manager" title)
			reader.readNext();
			
			while ((nextLine = reader.readNext()) != null) {
				// Handle empty lines (tool separators)
				if (nextLine.length == 1 && StringUtils.isBlank(nextLine[0])) {
					// Empty line indicates new tool section
					if (hasChanged && !toolHeader.isEmpty() && !toolContent.isEmpty()) {
						tool.add(dateManagerService.getToolTitle(currentToolId));
						tool.add(toolHeader);
						tool.add(toolContent);
						tools.add(tool);
					}
					
					// Reset for next tool
					tool = new ArrayList<>();
					toolHeader = new ArrayList<>();
					toolContent = new ArrayList<>();
					hasChanged = false;
					continue;
				}
				
				// Handle tool title lines (e.g., "sakai.assignment(Assignments)")
				if (nextLine.length == 1 && nextLine[0].contains("(") && nextLine[0].contains(")")) {
					String toolLine = nextLine[0];
					currentToolId = toolLine.substring(0, toolLine.indexOf("("));
					isHeader = true;
					continue;
				}
				
				// Handle data rows (header or content)
				if (nextLine.length > 1) {
					String[] toolColumns = new String[nextLine.length - 1]; // Skip first column (ID)
					
					// Copy all columns except the first (ID column)
					toolColumns = Arrays.copyOfRange(nextLine, 1, nextLine.length);

					// Check if this row has changes (skip for header rows)
					boolean isChanged = true;
					if (!isHeader) {
						try {
							isChanged = dateManagerService.isChanged(currentToolId, nextLine);
						} catch (Exception ex) {
							log.error("Cannot identify if it is changed or not in {}", currentToolId);
							isChanged = false;
						}
						if (isChanged) {
							// Store original line info for later processing in confirmUpdate
							StringBuilder csvLine = new StringBuilder();
							for (int i = 0; i < nextLine.length; i++) {
								if (i > 0) csvLine.append(getCsvSeparator());
								csvLine.append("\"").append(nextLine[i]).append("\"");
							}
							String[] toolInfoArray = {currentToolId, String.valueOf(idx), csvLine.toString(), String.valueOf(nextLine.length)};
							toolsInfoArray.add(toolInfoArray);
						}
					}
					
					idx++;
					if (isChanged) {
						if (isHeader) {
							isHeader = false;
							toolHeader = new ArrayList<>();
							toolHeader.add(toolColumns);
						} else {
							hasChanged = true;
							toolContent.add(toolColumns);
						}
					}
				}
			}
			
			// Handle the last tool if it exists
			if (hasChanged && !toolHeader.isEmpty() && !toolContent.isEmpty()) {
				tool.add(dateManagerService.getToolTitle(currentToolId));
				tool.add(toolHeader);
				tool.add(toolContent);
				tools.add(tool);
			}
		} catch (Exception ex) {
			model.addAttribute("errorMessage", rb.getString("page.import.error.no.csv.file"));
			log.error("Cannot identify the file received", ex);
		}
		model = getModelWithLocale(model, request, response);
		if (!tools.isEmpty()) {
			model.addAttribute("tools", tools);
			return "confirm_import";
		} else {
			model.addAttribute("errorMessage", rb.getString("page.import.error.any.date"));
			return "import_page";
		}
	}

	/**
	 * Function to show the confirm import page
	 * 
	 * @param Model - model
	 * @param HttpServletRequest - request
	 * 
	 * @return String - the page to show
	 */
	@GetMapping(value = {"/date-manager/page/import/confirm"}) 
	public String showConfirmImport(Model model, HttpServletRequest request, HttpServletResponse response) {
		model = getModelWithLocale(model, request, response);
		if (toolsInfoArray.size() > 0) {
			model.addAttribute("tools", tools);
			return "confirm_import";
		} else {
			model.addAttribute("errorMessage", rb.getString("page.import.error.no.file"));
			return "import_page";
		}
	}

	/**
	 * Function to update the information of the sent tools
	 * 
	 * @param Model - model
	 * @param HttpServletRequest - request
	 * @param HttpServletResponse - response
	 * 
	 * @return String - the page to show
	 */
	@PostMapping(value = {"/import/dates/confirm"})
	public String confirmUpdate(Model model, HttpServletRequest request, HttpServletResponse response) {
		List errors = new ArrayList<>();
		ArrayList dateValidationsByToolId = new ArrayList<>();
		for (String[] toolInfoArray : toolsInfoArray) {
			String currentToolId = toolInfoArray[0];
			int idx = Integer.parseInt(toolInfoArray[1]);
			String csvLine = toolInfoArray[2];
			
			// Parse the CSV line properly using CSVReader
			String[] toolColumnsAux;
			try (
				StringReader stringReader = new StringReader(csvLine);
				CSVReader lineReader = new CSVReaderBuilder(stringReader)
					.withCSVParser(new CSVParserBuilder().withSeparator(getCsvSeparatorChar()).build())
					.build()
			) {
				toolColumnsAux = lineReader.readNext();
			} catch (Exception e) {
				log.error("Error parsing CSV line: {}", csvLine, e);
				continue;
			}
			
			DateManagerValidation dateValidation = dateManagerService.validateTool(currentToolId, idx, columnsNames, toolColumnsAux);
			if (dateValidation != null) {
				if (!dateValidation.getErrors().isEmpty()) {
					List error = new ArrayList<>();
					String id = toolColumnsAux[0];
					String title = toolColumnsAux[1];
					error.add(dateValidation);
					error.add(id);
					error.add(title);
					errors.add(error);
				} else {
					ArrayList dateValidationArray = new ArrayList<>();
					dateValidationArray.add(currentToolId);
					dateValidationArray.add(dateValidation);
					dateValidationsByToolId.add(dateValidationArray);
				}
			}
		}
		
		model = getModelWithLocale(model, request, response);
		if (errors.isEmpty()){
			for (Object dateValidationObject : dateValidationsByToolId) {
				String currentToolId = (String) ((ArrayList) dateValidationObject).get(0);
				DateManagerValidation dateValidation = (DateManagerValidation) ((ArrayList) dateValidationObject).get(1);
				
				dateManagerService.updateTool(currentToolId, dateValidation);
			}
			return this.showIndex("", model, request, response);
		} else {
			model.addAttribute("errors", errors);
			model.addAttribute("warn_message", rb.getString("warn.message"));
			return "confirm_import";
		}
	}
	
}
