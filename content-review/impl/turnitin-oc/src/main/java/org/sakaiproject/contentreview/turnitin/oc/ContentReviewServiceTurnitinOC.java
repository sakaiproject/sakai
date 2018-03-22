/**
 * Copyright (c) 2003 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http:// Opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.contentreview.turnitin.oc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.service.BaseContentReviewService;
import org.sakaiproject.contentreview.service.ContentReviewQueueService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
public class ContentReviewServiceTurnitinOC extends BaseContentReviewService {
	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter
	private EntityManager entityManager;

	@Setter
	private SecurityService securityService;
	
	@Setter
	private AssignmentService assignmentService;

	@Setter
	private SiteService siteService;

	@Setter
	private ContentReviewQueueService crqs;

	@Setter
	private ContentHostingService contentHostingService;
	
	@Setter
	private SessionManager sessionManager;

	private static final String SERVICE_NAME = "TurnitinOC";
	private static final String TURNITIN_OC_API_VERSION = "v1";
	private static final int TURNITIN_OC_MAX_RETRY_MINUTES = 240; // 4 hours
	private static final int TURNITIN_MAX_RETRY = 16;
	private static final String INTEGRATION_VERSION = "1.0";
	private static final String INTEGRATION_FAMILY = "sakai";
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String CONTENT_TYPE_BINARY = "application/octet-stream";
	private static final String HEADER_NAME = "X-Turnitin-Integration-Name";
	private static final String HEADER_VERSION = "X-Turnitin-Integration-Version";
	private static final String HEADER_AUTH = "Authorization";
	private static final String HEADER_CONTENT = "Content-Type";
	private static final String HEADER_DISP = "Content-Disposition";
	
	private static final String HTML_EXTENSION = ".html";

	private static final String STATUS_CREATED = "CREATED";
	private static final String STATUS_COMPLETE = "COMPLETE";
	private static final String STATUS_PROCESSING = "PROCESSING";
	
	private static final String RESPONSE_CODE = "responseCode";
	private static final String RESPONSE_MESSAGE = "responseMessage";
	private static final String RESPONSE_BODY = "responseBody";
	private static final String GIVEN_NAME = "given_name";
	private static final String FAMILY_NAME = "family_name";
	
	private static final int PLACEHOLDER_ITEM_REVIEW_SCORE = -10;

	private String serviceUrl;
	private String apiKey;
	private String sakaiVersion;

	private HashMap<String, String> BASE_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SUBMISSION_REQUEST_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SIMILARITY_REPORT_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> CONTENT_UPLOAD_HEADERS = new HashMap<String, String>();

	public void init() {
		// Retrieve Service URL and API key
		serviceUrl = serverConfigurationService.getString("turnitin.oc.serviceUrl", "");
		apiKey = serverConfigurationService.getString("turnitin.oc.apiKey", "");
		// Retrieve Sakai Version if null set default 		
		sakaiVersion = Optional.ofNullable(serverConfigurationService.getString("version.sakai", "")).orElse("UNKNOWN");		

		// Populate base headers that are needed for all calls to TCA
		BASE_HEADERS.put(HEADER_NAME, INTEGRATION_FAMILY);
		BASE_HEADERS.put(HEADER_VERSION, sakaiVersion);
		BASE_HEADERS.put(HEADER_AUTH, "Bearer " + apiKey);

		// Populate submission request headers used in getSubmissionId
		SUBMISSION_REQUEST_HEADERS.putAll(BASE_HEADERS);
		SUBMISSION_REQUEST_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_JSON);

		// Populate similarity report headers used in generateSimilarityReport
		SIMILARITY_REPORT_HEADERS.putAll(BASE_HEADERS);
		SIMILARITY_REPORT_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_JSON);

		// Populate content upload headers used in uploadExternalContent
		CONTENT_UPLOAD_HEADERS.putAll(BASE_HEADERS);
		CONTENT_UPLOAD_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_BINARY);
	}

	public boolean allowResubmission() {
		return true;
	}

	public void checkForReports() {

	}

	public void createAssignment(final String contextId, final String assignmentRef, final Map opts)
			throws SubmissionException, TransientSubmissionException {

	}

	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return crqs.getContentReviewItems(getProviderId(), siteId, taskId);
	}

	public Map getAssignment(String arg0, String arg1) throws SubmissionException, TransientSubmissionException {
		return null;
	}

	public Date getDateQueued(String contextId) throws QueueException {
		return crqs.getDateQueued(getProviderId(), contextId);
	}

	public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
		return crqs.getDateSubmitted(getProviderId(), contextId);
	}

	public String getIconCssClassforScore(int score, String contentId) {
		String cssClass;
		if (score < 0) {
			cssClass = "contentReviewIconThreshold-6";
		} else if (score == 0) {
			cssClass = "contentReviewIconThreshold-5";
		} else if (score < 25) {
			cssClass = "contentReviewIconThreshold-4";
		} else if (score < 50) {
			cssClass = "contentReviewIconThreshold-3";
		} else if (score < 75) {
			cssClass = "contentReviewIconThreshold-2";
		} else {
			cssClass = "contentReviewIconThreshold-1";
		}

		return cssClass;
	}

	public String getLocalizedStatusMessage(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalizedStatusMessage(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalizedStatusMessage(String arg0, Locale arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return null;
	}
	
	public String getReviewReportRedirectUrl(String contentId, String assignmentRef, String userId, boolean isInstructor) {
		
		// Set variables
		String viewerUrl = null;
		Optional<ContentReviewItem> optionalItem = crqs.getQueuedItem(getProviderId(), contentId);
		ContentReviewItem item = optionalItem.isPresent() ? optionalItem.get() : null;
		if(item != null && ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE.equals(item.getStatus())) {
			try {
				//Get report owner user information
				String givenName = "", familyName = "";
				try{
					User user = userDirectoryService.getUser(item.getUserId());
					givenName = user.getFirstName();
					familyName = user.getLastName();
				}catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				Map<String, Object> data = new HashMap<String, Object>();
				// Set user name
				data.put(GIVEN_NAME, givenName);
				data.put(FAMILY_NAME, familyName);

				// Check user preference for locale			
				// If user has no preference set - get the system default
				Locale locale = Optional.ofNullable(preferencesService.getLocale(userId))
						.orElse(Locale.getDefault());

				// Set locale, getLanguage removes locale region
				data.put("locale", locale.getLanguage());

				HashMap<String, Object> response = makeHttpCall("GET",
						getNormalizedServiceUrl() + "submissions/" + item.getExternalId() + "/viewer-url",
						SUBMISSION_REQUEST_HEADERS,
						data,
						null);

				// Get response:
				int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
				String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
				String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);

				if ((responseCode >= 200) && (responseCode < 300) && (responseBody != null)) {
					// create JSONObject from responseBody
					JSONObject responseJSON = JSONObject.fromObject(responseBody);
					if (responseJSON.containsKey("viewer_url")) {
						viewerUrl = responseJSON.getString("viewer_url");
						log.debug("Successfully retrieved viewer url: " + viewerUrl);
						return viewerUrl;
					} else {
						throw new Error("Viewer URL not found. Response: " + responseMessage);
					}
				} else {
					throw new Error(responseMessage);
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
		}else {
			// Only generate viewerUrl if report is available
			log.info("Content review item is not ready for the report: " + contentId + ", " + (item != null ? item.getStatus() : ""));
		}
	
		return viewerUrl;
	}


	public int getReviewScore(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException, Exception {
		return crqs.getReviewScore(getProviderId(), contentId);
	}

	public Long getReviewStatus(String contentId) throws QueueException {
		return crqs.getReviewStatus(getProviderId(), contentId);
	}

	public String getServiceName() {
		return SERVICE_NAME;
	}

	public boolean isAcceptableContent(ContentResource arg0) {
		// TODO: what does TII accept?
		return true;
	}

	public boolean isSiteAcceptable(Site arg0) {
		return true;
	}
	
	private HashMap<String, Object> makeHttpCall(String method, String urlStr, Map<String, String> headers,  Map<String, Object> data, byte[] dataBytes) 
		throws IOException {
		// Set variables
		HttpURLConnection connection = null;
		DataOutputStream wr = null;
		URL url = null;

		// Construct URL
		url = new URL(urlStr);

		// Open connection and set HTTP method
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);

		// Set headers
		if (headers == null) {
			throw new Error("No headers present for call: " + method + ":" + urlStr);
		}
		for (Entry<String, String> entry : headers.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}

		// Set Post body:
		if (data != null) {
			// Convert data to string:
			connection.setDoOutput(true);
			wr = new DataOutputStream(connection.getOutputStream());
			ObjectMapper objectMapper = new ObjectMapper();
			String dataStr = objectMapper.writeValueAsString(data);
			wr.writeBytes(dataStr);
			wr.flush();
			wr.close();
		} else if (dataBytes != null) {
			connection.setDoOutput(true);
			wr = new DataOutputStream(connection.getOutputStream());
			wr.write(dataBytes);
			wr.close();
		}

		// Send request:
		int responseCode = connection.getResponseCode();
		String responseMessage = connection.getResponseMessage();
		String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
		
		HashMap<String, Object> response = new HashMap<String, Object>();
		response.put(RESPONSE_CODE, responseCode);
		response.put(RESPONSE_MESSAGE, responseMessage);
		response.put(RESPONSE_BODY, responseBody);
		
		return response;
	}

	private void generateSimilarityReport(String reportId, String assignmentId) throws Exception {
		
		Assignment assignment = assignmentService.getAssignment(assignmentId);
		Map<String, String> assignmentSettings = assignment.getProperties();
		
		List<String> repositories = new ArrayList<>();
		if ("true".equals(assignmentSettings.get("internet_check"))) {
			repositories.add("INTERNET");
		}
		if ("true".equals(assignmentSettings.get("institution_check"))) {
			repositories.add("PRIVATE");
		}
		if ("true".equals(assignmentSettings.get("journal_check"))) {
			repositories.add("JOURNAL");
		}
		
		if (repositories.size() == 0) {
			throw new Error("Cannot generate similarity report - at least one search repo must be selected");
		}

		// Build header maps
		Map<String, Object> reportData = new HashMap<String, Object>();
		Map<String, Object> generationSearchSettings = new HashMap<String, Object>();
		generationSearchSettings.put("search_repositories", repositories);
		reportData.put("generation_settings", generationSearchSettings);

		Map<String, Object> viewSettings = new HashMap<String, Object>();
		viewSettings.put("exclude_quotes", "true".equals(assignmentSettings.get("exclude_quoted")));
		viewSettings.put("exclude_bibliography", "true".equals(assignmentSettings.get("exclude_biblio")));
		reportData.put("view_settings", viewSettings);
		
		HashMap<String, Object> response = makeHttpCall("PUT",
			getNormalizedServiceUrl() + "submissions/" + reportId + "/similarity",
			SIMILARITY_REPORT_HEADERS,
			reportData,
			null);
		
		// Get response:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
		String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
		String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);
		
		if ((responseCode >= 200) && (responseCode < 300)) {
			log.debug("Successfully initiated Similarity Report generation.");
		} else if ((responseCode == 409)) {
			log.debug("A Similarity Report is already generating for this submission");
		} else {
			throw new Error(
					"Submission failed to initiate: " + responseCode + ", " + responseMessage + ", " + responseBody);
		}
	}

	private String getSubmissionStatus(String reportId) throws Exception {
		String status = null;
		HashMap<String, Object> response = makeHttpCall("GET",
				getNormalizedServiceUrl() + "submissions/" + reportId,
				BASE_HEADERS,
				null,
				null);
		// Get response data:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
		String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
		String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);

		// Create JSONObject from response
		JSONObject responseJSON = JSONObject.fromObject(responseBody);
		if ((responseCode >= 200) && (responseCode < 300)) {
			// Get submission status value
			if (responseJSON.containsKey("status")) {
				status = responseJSON.getString("status");
			}
		} else {
			throw new Exception("getSubmissionStatus invalid request: " + responseCode + ", " + responseMessage + ", "
					+ responseBody);
		}
		return status;
	}

	private int getSimilarityReportStatus(String reportId) throws Exception {

		HashMap<String, Object> response = makeHttpCall("GET",
				getNormalizedServiceUrl() + "submissions/" + reportId + "/similarity",
				BASE_HEADERS,
				null,
				null);

		// Get response:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
		String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
		String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);

		// create JSONObject from response
		JSONObject responseJSON = JSONObject.fromObject(responseBody);

		if ((responseCode >= 200) && (responseCode < 300)) {
			// See if report is complete or pending. If pending, ignore, if complete, get
			// score and viewer URL
			if (responseJSON.containsKey("status") && responseJSON.getString("status").equals(STATUS_COMPLETE)) {
				log.debug("Submission successful");
				if (responseJSON.containsKey("overall_match_percentage")) {
					return responseJSON.getInt("overall_match_percentage");
				} else {
					log.error("Report came back as complete, but without a score");
					return -2;
				}

			} else if (responseJSON.containsKey("status")
					&& responseJSON.getString("status").equals(STATUS_PROCESSING)) {
				log.debug("report is processing...");
				return -1;
			} else {
				log.error("Something went wrong in the similarity report process: reportId " + reportId);
				return -2;
			}
		} else {
			log.error("Submission status call failed: " + responseMessage);
			return -2;
		}
	}

	private String getSubmissionId(String userID, String fileName) {

		String submissionId = null;
		try {

			// Build header maps
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("owner", userID);
			data.put("title", fileName);

			HashMap<String, Object> response = makeHttpCall("POST",
					getNormalizedServiceUrl() + "submissions",
					SUBMISSION_REQUEST_HEADERS,
					data,
					null);

			// Get response:
			int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
			String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
			String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);

			// create JSONObject from responseBody
			JSONObject responseJSON = JSONObject.fromObject(responseBody);

			if ((responseCode >= 200) && (responseCode < 300)) {
				if (responseJSON.containsKey("status") && responseJSON.getString("status").equals(STATUS_CREATED)
						&& responseJSON.containsKey("id")) {
					submissionId = responseJSON.getString("id");
				} else {
					log.error("getSubmissionId response: " + responseMessage);
				}
			} else {
				log.error("getSubmissionId response code: " + responseCode + ", " + responseMessage + ", "
						+ responseJSON);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return submissionId;
	}

	// Queue service for processing student submissions
	// Stage one creates a submission, uploads submission contents to TCA and sets item externalId
	// Stage two starts similarity report process
	// Stage three checks status of similarity reports and retrieves report score
	// Loop 1 contains stage one and two, Loop 2 contains stage three
	public void processQueue() {
		log.info("Processing Turnitin OC submission queue");
		// Create new Calendar instance used for adding delay time to current time
		Calendar cal = Calendar.getInstance();
		// Create new session object to ensure permissions are carried correctly to each new thread
		final Session session = sessionManager.getCurrentSession();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				sessionManager.setCurrentSession(session);
				processUnsubmitted(cal);
			}
		});
		executor.execute(new Runnable() {
			@Override
			public void run() {
				sessionManager.setCurrentSession(session);
				checkForReport(cal);
			}
		});
		executor.shutdown();
		// wait:
		try {
			if(!executor.awaitTermination(30, TimeUnit.MINUTES)){
				log.error("ContentReviewServiceTurnitinOC.processQueue: time out waiting for executor to complete");
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void checkForReport(Calendar cal) {
		// Original file has been uploaded, and similarity report has been requested
		// Check for status of report and return score
		int errors = 0;
		int success = 0;

		for (ContentReviewItem item : crqs.getAwaitingReports(getProviderId())) {
			// Make sure it's after the next retry time
			if (item.getNextRetryTime().getTime() > new Date().getTime()) {
				continue;
			}
			if (!incrementItem(cal, item)) {
				errors++;
				continue;
			}
			// Check if any placeholder items need to regenerate report after due date
			if (item.getReviewScore() != null &&  item.getReviewScore().equals(PLACEHOLDER_ITEM_REVIEW_SCORE)) {
				// Get assignment due date			
				try {
					Assignment assignment = assignmentService.getAssignment(item.getTaskId().split("/")[4]);
					Date assignmentDueDate = Date.from(assignment.getDueDate());
					// Make sure due date is past
					if (assignmentDueDate.before(new Date())) {
						// Regenerate similarity request 
						generateSimilarityReport(item.getExternalId(), item.getTaskId().split("/")[4]);
						// Reset retry count
						item.setRetryCount(new Long(0));
						// Remove placeholder item score
						item.setReviewScore(null);
						// Reset cal to current time
						cal.setTime(new Date());
						// Reset delay time
						cal.add(Calendar.MINUTE, getDelayTime(item.getRetryCount()));
						// Schedule next retry time
						item.setNextRetryTime(cal.getTime());
						crqs.update(item);
						success++;
						continue;
					} else {
						// We don't want placeholder items to exceed retry count maximum
						// Reset retry count to zero
						item.setRetryCount(Long.valueOf(0));
						// Retry in one hour
						cal.setTime(new Date());
						cal.add(Calendar.HOUR_OF_DAY, 1);
						item.setNextRetryTime(cal.getTime());
						crqs.update(item);
						continue;
					}
				} catch (Exception e) {
					log.error(e.getLocalizedMessage(), e);
					item.setLastError(e.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE);
					crqs.update(item);
					errors++;												
				}
			}
						
			try {
				// Get status of similarity report
				// Returns -1 if report is still processing
				// Returns -2 if an error occurs
				// Else returns reports score as integer																	
					int status = getSimilarityReportStatus(item.getExternalId());
					if (status > -1) {					
						log.info("Report complete! Score: " + status);
						// Status value is report score
						item.setReviewScore(status);								
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
						item.setDateReportReceived(new Date());
						item.setRetryCount(Long.valueOf(0));
						item.setLastError(null);
						item.setErrorCode(null);
						success++;
						crqs.update(item);
					} else if (status == -1) {
						// Similarity report is still generating, will try again
						log.info("Processing report " + item.getExternalId() + "...");
					} else if(status == -2){
						throw new Error("Unknown error during report status call");
					}
								
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
				item.setLastError(e.getMessage());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE);
				crqs.update(item);
				errors++;
			}
		}

		log.info("Turnitin report queue run completed: " + success + " items submitted, " + errors + " errors.");
		
	}
	
	public void processUnsubmitted(Calendar cal) {
		// Submission process phase 1
		// 1. Establish submission object, get ID
		// 2. Upload original file to submission
		// 3. Start originality report
		int errors = 0;
		int success = 0;
		Optional<ContentReviewItem> nextItem = null;

		while ((nextItem = crqs.getNextItemInQueueToSubmit(getProviderId())).isPresent()) {
			ContentReviewItem item = nextItem.get();

			if (!incrementItem(cal, item)) {
				errors++;
				continue;
			}						
			// Handle items that only generate reports on due date				
			try {
				// Get assignment due date with current item's task Id
				Assignment assignment = assignmentService.getAssignment(item.getTaskId().split("/")[4]);
				Date assignmentDueDate = Date.from(assignment.getDueDate());					
				String reportGenSpeed = assignment.getProperties().get("report_gen_speed");
				// If report gen speed is set to due date, and it's before the due date right now, do not process item
				if (reportGenSpeed != null && assignmentDueDate != null && reportGenSpeed.equals("2")
						&& assignmentDueDate.after(new Date())) {
					log.info("Report generate speed is 2, skipping for now. ItemID: " + item.getId());
					// We don't items with gen speed 2 items to exceed retry count maximum
					// Reset retry count to zero
					item.setRetryCount(Long.valueOf(0));
					// Retry in one hour
					cal.setTime(new Date());
					cal.add(Calendar.HOUR_OF_DAY, 1);
					item.setNextRetryTime(cal.getTime());
					crqs.update(item);
					continue;
				}
			} catch (IdUnusedException | PermissionException e) {				
				log.error(e.getMessage(), e);
			}				

			ContentResource resource = null;
			try {
				// Get resource with current item's content Id
				resource = contentHostingService.getResource(item.getContentId());
			} catch (IdUnusedException e4) {
				log.error("IdUnusedException: no resource with id " + item.getContentId(), e4);
				item.setLastError("IdUnusedException: no resource with id " + item.getContentId());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			} catch (PermissionException e) {
				log.error("PermissionException: no resource with id " + item.getContentId(), e);
				item.setLastError("PermissionException: no resource with id " + item.getContentId());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			} catch (TypeException e) {
				log.error("TypeException: no resource with id " + item.getContentId(), e);
				item.setLastError("TypeException: no resource with id " + item.getContentId());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			}

			// EXTERNAL ID DOES NOT EXIST, CREATE SUBMISSION AND UPLOAD CONTENTS TO TCA
			// (STAGE 1)
			if (StringUtils.isEmpty(item.getExternalId())) {
				// Get filename of submission						
				String fileName = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);						
				// If fileName is empty set default
				if (StringUtils.isEmpty(fileName)) {
					fileName = "submission_" + item.getUserId() + "_" + item.getSiteId();
					log.info("Using Default Filename " + fileName);
				}				
				// Add .html for inline submissions				
				if ("true".equals(resource.getProperties().getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION))
						&& FilenameUtils.getExtension(fileName).isEmpty()) {
					fileName += HTML_EXTENSION;
				}							
				try {
					log.info("Submission starting...");
					// Retrieve submissionId from TCA and set to externalId
					String externalId = getSubmissionId(item.getUserId(), fileName);
					if (StringUtils.isEmpty(externalId)) {
						throw new Error("submission id is missing");
					} else {
						// Add filename to content upload headers
						CONTENT_UPLOAD_HEADERS.put(HEADER_DISP, "inline; filename=\"" + fileName + "\"");
						// Upload submission contents of to TCA
						uploadExternalContent(externalId, resource.getContent());
						// Set item externalId to externalId
						item.setExternalId(externalId);
						// Reset retry count
						item.setRetryCount(new Long(0));
						// Reset cal to current time
						cal.setTime(new Date());
						// Reset delay time
						cal.add(Calendar.MINUTE, getDelayTime(item.getRetryCount()));
						// Schedule next retry time
						item.setNextRetryTime(cal.getTime());
						item.setDateSubmitted(new Date());
						crqs.update(item);
						success++;
					}

				} catch (Exception e) {
					log.error(e.getMessage(), e);
					item.setLastError(e.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					crqs.update(item);
					errors++;
				}
			} else {
				// EXTERNAL ID EXISTS, START SIMILARITY REPORT GENERATION PROCESS (STAGE 2)
				try {
					// Get submission status, returns the state of the submission as string
					String submissionStatus = getSubmissionStatus(item.getExternalId());
					// Handle possible error status
					String errorStr = null;
					switch (submissionStatus) {
					case "COMPLETE":
						// If submission status is complete, start similarity report process
						generateSimilarityReport(item.getExternalId(), item.getTaskId().split("/")[4]);
						// Update item status for loop 2
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
						// Reset retry count
						item.setRetryCount(new Long(0));
						// Reset cal to current time
						cal.setTime(new Date());
						// Reset delay time
						cal.add(Calendar.MINUTE, getDelayTime(item.getRetryCount()));
						// Schedule next retry time
						item.setNextRetryTime(cal.getTime());
						crqs.update(item);
						success++;
						break;
					case "PROCESSING":
						// do nothing... try again
						break;
					case "CREATED":
						// do nothing... try again
						break;
					case "UNSUPPORTED_FILETYPE":
						errorStr = "The uploaded filetype is not supported";
					case "PROCESSING_ERROR":
						errorStr = "An unspecified error occurred while processing the submissions";
					case "TOO_LITTLE_TEXT":
						errorStr = "The submission does not have enough text to generate a Similarity Report (a submission must contain at least 20 words)";
					case "TOO_MUCH_TEXT":
						errorStr = "The submission has too much text to generate a Similarity Report (after extracted text is converted to UTF-8, the submission must contain less than 2MB of text)";
					case "TOO_MANY_PAGES":
						errorStr = "The submission has too many pages to generate a Similarity Report (a submission cannot contain more than 400 pages)";
					case "FILE_LOCKED":
						errorStr = "The uploaded file requires a password in order to be opened";
					case "CORRUPT_FILE":
						errorStr = "The uploaded file appears to be corrupt";
					case "ERROR":				
						errorStr = "Submission returned with ERROR status";
					default:
						log.info("Unknown submission status, will retry: " + submissionStatus);
					}
					if(StringUtils.isNotEmpty(errorStr)) {
						item.setLastError(errorStr);
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
						crqs.update(item);
						errors++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					item.setLastError(e.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					crqs.update(item);
					errors++;
				}

				try {
					// Check for items that generate reports both immediately and on due date
					// Create a placeholder item that will regenerate report score after due date
					Assignment assignment = assignmentService.getAssignment(item.getTaskId().split("/")[4]);
					Date assignmentDueDate = Date.from(assignment.getDueDate());					
					String reportGenSpeed = assignment.getProperties().get("report_gen_speed");
					if (reportGenSpeed != null && assignmentDueDate != null && reportGenSpeed.equals("1")
							&& assignmentDueDate.after(new Date())) {
						log.info("Creating placeholder item for when due date is passed for ItemID: " + item.getId());						
						ContentReviewItem placeholderItem = new ContentReviewItem();																		
						// Set placeholder review score, handled in checkForReport
						placeholderItem.setReviewScore(PLACEHOLDER_ITEM_REVIEW_SCORE);
						// Id must be unique for each row in content review table, assigning random long
						placeholderItem.setId(new Random().nextLong());
						// Content Id must be unique for each row in content review table, assigning random string 
						placeholderItem.setContentId(RandomStringUtils.randomAlphabetic(120));
						placeholderItem.setExternalId(item.getExternalId());
						placeholderItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);

						// Set retry time for placeholder item to one hour from current time
						cal.setTime(new Date());
						cal.add(Calendar.HOUR_OF_DAY, 1);																	
						placeholderItem.setNextRetryTime(cal.getTime());

						// Other required fields are copied from original item 
						placeholderItem.setProviderId(item.getProviderId());						
						placeholderItem.setUserId(item.getUserId());
						placeholderItem.setSiteId(item.getSiteId());
						placeholderItem.setTaskId(item.getTaskId());
						placeholderItem.setDateQueued(new Date());
						placeholderItem.setDateSubmitted(new Date());
						placeholderItem.setRetryCount(new Long(0));																		
						crqs.update(placeholderItem);
						success++;
					}
				} catch (IdUnusedException | PermissionException e) {
					log.error("Error creating placeholder item");
					log.error(e.getMessage(), e);
				}
			}

			log.info("Turnitin submission queue completed: " + success + " items submitted, " + errors + " errors.");

		}
	}

	public boolean incrementItem(Calendar cal, ContentReviewItem item) {
		// If retry count is null set to 0
		if (item.getRetryCount() == null) {
			item.setRetryCount(Long.valueOf(0));
			item.setNextRetryTime(cal.getTime());
			crqs.update(item);
			// If retry count is above maximum increment error count, set status to nine and stop retrying
		} else if (item.getRetryCount().intValue() > TURNITIN_MAX_RETRY) {
			item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE);
			crqs.update(item);
			return false;
			// Increment retry count, adjust delay time, schedule next retry attempt
		} else {
			long retryCount = item.getRetryCount().longValue();
			retryCount++;
			item.setRetryCount(Long.valueOf(retryCount));
			cal.add(Calendar.MINUTE, getDelayTime(retryCount));
			item.setNextRetryTime(cal.getTime());
			crqs.update(item);
		}
		return true;
	}

	public int getDelayTime(long retries) {
		// exponential retry algorithm that caps the retries off at 36 hours (checking once every 4 hours max)
		int minutes = (int) Math.pow(2, retries < TURNITIN_MAX_RETRY ? retries : 1); // built in check for max retries
																						// to fail quicker
		return minutes > TURNITIN_OC_MAX_RETRY_MINUTES ? TURNITIN_OC_MAX_RETRY_MINUTES : minutes;
	}

	public void queueContent(String userId, String siteId, String assignmentReference, List<ContentResource> content)
			throws QueueException {
		crqs.queueContent(getProviderId(), userId, siteId, assignmentReference, content);
	}

	public void removeFromQueue(String contentId) {
		crqs.removeFromQueue(getProviderId(), contentId);
	}

	public void resetUserDetailsLockedItems(String arg0) {
		// TODO Auto-generated method stub
	}

	public String getReviewError(String contentId) {
		return null;
	}

	public boolean allowAllContent() {
		return true;
	}

	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		return new HashMap<String, SortedSet<String>>();
	}

	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		return new HashMap<String, SortedSet<String>>();
	}

	private String getNormalizedServiceUrl() {
		return serviceUrl + ((StringUtils.isNotEmpty(serviceUrl) && serviceUrl.endsWith("/")) ? "" : "/")
				+ TURNITIN_OC_API_VERSION + "/";
	}

	private void uploadExternalContent(String reportId, byte[] data) throws Exception {
		
		HashMap<String, Object> response = makeHttpCall("PUT",
				getNormalizedServiceUrl() + "submissions/" + reportId + "/original/",
				CONTENT_UPLOAD_HEADERS,
				null,
				data);

		// Get response:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
		String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);

		if (responseCode < 200 || responseCode >= 300) {
			throw new Error(responseCode + ": " + responseMessage);
		}
	}

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId) {
		Optional<ContentReviewItem> cri = crqs.getQueuedItem(getProviderId(), contentId);
		return cri.isPresent() ? cri.get() : null;
	}
	
	@Override
	public String getEndUserLicenseAgreementLink() {
		return "https://www.vericite.com";
	}

	@Override
	public Instant getEndUserLicenseAgreementTimestamp() {
		return Instant.MIN;
	}

	@Override
	public String getEndUserLicenseAgreementVersion() {
		return "1.1";
	}
}
