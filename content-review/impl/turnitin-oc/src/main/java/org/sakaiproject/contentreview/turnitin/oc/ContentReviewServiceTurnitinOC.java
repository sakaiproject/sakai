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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
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
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Slf4j
public class ContentReviewServiceTurnitinOC extends BaseContentReviewService {

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
	
	private static final String GENERATE_REPORTS_IMMEDIATELY_AND_ON_DUE_DATE= "1";
	private static final String GENERATE_REPORTS_ON_DUE_DATE = "2";	
	private static final String PLACEHOLDER_STRING_FLAG = "_placeholder";
	private static final Integer PLACEHOLDER_ITEM_REVIEW_SCORE = -10;

	private static final String COMPLETE_STATUS = "COMPLETE";
	private static final String CREATED_STATUS = "CREATED";
	private static final String PROCESSING_STATUS = "PROCESSING";

	private static final String SUBMISSION_COMPLETE_EVENT_TYPE = "SUBMISSION_COMPLETE";
	private static final String SIMILARITY_COMPLETE_EVENT_TYPE = "SIMILARITY_COMPLETE";

	private String serviceUrl;
	private String apiKey;
	private String sakaiVersion;

	private HashMap<String, String> BASE_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SUBMISSION_REQUEST_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SIMILARITY_REPORT_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> CONTENT_UPLOAD_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> WEBHOOK_SETUP_HEADERS = new HashMap<String, String>();

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
		
		// Populate webhook generation headers used in setupWebhook
		WEBHOOK_SETUP_HEADERS.putAll(BASE_HEADERS);
		WEBHOOK_SETUP_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_JSON);

		// Populate content upload headers used in uploadExternalContent
		CONTENT_UPLOAD_HEADERS.putAll(BASE_HEADERS);
		CONTENT_UPLOAD_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_BINARY);

		try {
			// Get the webhook url
			String webhookUrl = getWebhookUrl(Optional.empty());
			boolean webhooksSetup = false;
			// Check to see if any webhooks have already been set up for this url
			for (Webhook webhook : getWebhooks()) {
				if (StringUtils.isNotEmpty(webhook.getUrl()) && webhook.getUrl().equals(webhookUrl)) {
					webhooksSetup = true;
					break;
				}
			}

			if (!webhooksSetup) {
				// No webhook set up for this url, set one up
				setupWebhook(webhookUrl);
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	public String setupWebhook(String webhookUrl) throws Exception {
		String id;
		Map<String, Object> data = new HashMap<String, Object>();
		List<String> types = new ArrayList<>();
		types.add("SIMILARITY_COMPLETE");
		types.add("SUBMISSION_COMPLETE");

		data.put("signing_secret", base64Encode(apiKey));
		data.put("url", webhookUrl);
		data.put("description", "Sakai " + sakaiVersion);
		data.put("allow_insecure", false);
		data.put("event_types", types);

		HashMap<String, Object> response = makeHttpCall("POST",
				getNormalizedServiceUrl() + "webhooks",
				WEBHOOK_SETUP_HEADERS,
				data,
				null);

		// Get response:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
		String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
		String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);

		if ((responseCode >= 200) && (responseCode < 300) && (responseBody != null)) {
			// create JSONObject from responseBody
			JSONObject responseJSON = JSONObject.fromObject(responseBody);
			if (responseJSON.has("id")) {
				id = responseJSON.getString("id");
			} else {
				throw new Error("Error setting up webhook - returned with no ID");
			}
		} else {
			throw new Error(responseMessage);
		}

		return id;
	}
	
	public ArrayList<Webhook> getWebhooks() throws Exception {
		ArrayList<Webhook> webhooks = new ArrayList<>();

		HashMap<String, Object> response = makeHttpCall("GET",
				getNormalizedServiceUrl() + "webhooks",
				BASE_HEADERS,
				null,
				null);

		// Get response:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);
		String responseMessage = !response.containsKey(RESPONSE_MESSAGE) ? "" : (String) response.get(RESPONSE_MESSAGE);
		String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);

		if ((responseCode < 200) || (responseCode >= 300) || (responseBody == null)) {
			throw new Error(responseMessage);
		} else if (StringUtils.isNotEmpty(responseBody) && !"[]".equals(responseBody)) {
			// Loop through response via JSON, convert objects to Webhooks
			JSONArray webhookList = JSONArray.fromObject(responseBody);
			for (int i=0; i < webhookList.size(); i++) {
				JSONObject webhookJSON = webhookList.getJSONObject(i);
				if (webhookJSON.has("id") && webhookJSON.has("url")) {
					webhooks.add(new Webhook(webhookJSON.getString("id"), webhookJSON.getString("url")));
				}
			}
		}
		
		return webhooks;
	}

	public void deleteWebhook(String id) throws Exception {
		HashMap<String, Object> response = makeHttpCall("DELETE",
				getNormalizedServiceUrl() + "webhooks/" + id,
				BASE_HEADERS,
				null,
				null);

		// Get response:
		int responseCode = !response.containsKey(RESPONSE_CODE) ? 0 : (int) response.get(RESPONSE_CODE);

		if ((responseCode < 200) || (responseCode >= 300)) {
			throw new Error("Could not delete webhook: " + id);
		}
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
		Optional<ContentReviewItem> optionalItem = crqs.getQueuedItem(getProviderId(), contentId);
		if(optionalItem.isPresent()) {
			return optionalItem.get().getReviewScore();
		}else {
			throw new ReportException("Could not find content item: " + contentId);
		}
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
	
	public SecurityAdvisor pushAdvisor() {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
        };
        securityService.pushAdvisor(advisor);
        return advisor;
    }

    public void popAdvisor(SecurityAdvisor advisor) {
        securityService.popAdvisor(advisor);
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

	private void generateSimilarityReport(String reportId, String assignmentRef, boolean isDraft) throws Exception {
		
		Assignment assignment = assignmentService.getAssignment(entityManager.newReference(assignmentRef));
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
		
		Map<String, Object> indexingSettings = new HashMap<String, Object>();
		//Drafts are not added to index to avoid self plagiarism
		indexingSettings.put("add_to_index", !isDraft);
		reportData.put("indexing_settings", indexingSettings);

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
	// processUnsubmitted contains stage one and two, checkForReport contains stage three
	public void processQueue() {
		log.info("Processing Turnitin OC submission queue");
		// Create new session object to ensure permissions are carried correctly to each new thread
		final Session session = sessionManager.getCurrentSession();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				sessionManager.setCurrentSession(session);
				processUnsubmitted();
			}
		});
		executor.execute(new Runnable() {
			@Override
			public void run() {
				sessionManager.setCurrentSession(session);
				checkForReport();
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
	
	public void checkForReport() {
		// Original file has been uploaded, and similarity report has been requested
		// Check for status of report and return score
		int errors = 0;
		int success = 0;

		for (ContentReviewItem item : crqs.getAwaitingReports(getProviderId())) {
			try {
				// Make sure it's after the next retry time
				if (item.getNextRetryTime().getTime() > new Date().getTime()) {
					continue;
				}
				if (!incrementItem(item)) {
					errors++;
					continue;
				}
				// Check if any placeholder items need to regenerate report after due date
				if (PLACEHOLDER_ITEM_REVIEW_SCORE.equals(item.getReviewScore())) {	
					// Get assignment associated with current item's task Id
					Assignment assignment = assignmentService.getAssignment(entityManager.newReference(item.getTaskId()));
					Date assignmentDueDate = Date.from(assignment.getDueDate());
					if(assignment != null && assignmentDueDate != null ) {
						// Make sure due date is past						
						if (assignmentDueDate.before(new Date())) {
							//Lookup reference item
							String referenceItemContentId = item.getContentId().substring(0, item.getContentId().indexOf(PLACEHOLDER_STRING_FLAG));							
							Optional<ContentReviewItem> quededReferenceItem = crqs.getQueuedItem(item.getProviderId(), referenceItemContentId);
							ContentReviewItem referenceItem = quededReferenceItem.isPresent() ? quededReferenceItem.get() : null;							
							if (referenceItem != null) {
								// Regenerate similarity request for reference id
								// Report is recalled after due date, no need to account for draft
								generateSimilarityReport(referenceItem.getExternalId(), referenceItem.getTaskId(), false);
								//reschedule reference item by setting score to null, reset retry time and set status to awaiting report
								referenceItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
								referenceItem.setRetryCount(Long.valueOf(0));
								referenceItem.setReviewScore(null);
								referenceItem.setNextRetryTime(new Date());
								crqs.update(referenceItem);
								// Report regenerated for reference item, placeholder item is no longer needed
								crqs.delete(item);
								success++;
								continue;
							}
							else {
								// Reference item no longer exists
								// Placeholder item is no longer needed
								crqs.delete(item);
								errors++;
								continue;
							}
						} else {
							// We don't want placeholder items to exceed retry count maximum
							// Reset retry count to zero
							item.setRetryCount(Long.valueOf(0));
							item.setNextRetryTime(getDueDateRetryTime(assignmentDueDate));
							crqs.update(item);
							continue;
						}
					}else {
						// Assignment or due date no longer exist
						// placeholder item is no longer needed
						crqs.delete(item);
						errors++;
						continue;
					}
				}
				// Get status of similarity report
				// Returns -1 if report is still processing
				// Returns -2 if an error occurs
				// Else returns reports score as integer																	
				int status = getSimilarityReportStatus(item.getExternalId());
				if (status >= 0) {
					success++;
				}
				handleReportStatus(item, status);

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
	
	public void processUnsubmitted() {
		// Submission process phase 1
		// 1. Establish submission object, get ID
		// 2. Upload original file to submission
		// 3. Start originality report
		int errors = 0;
		int success = 0;
		Optional<ContentReviewItem> nextItem = null;

		while ((nextItem = crqs.getNextItemInQueueToSubmit(getProviderId())).isPresent()) {
			try {
				ContentReviewItem item = nextItem.get();
				if (!incrementItem(item)) {
					errors++;
					continue;
				}						
				// Handle items that only generate reports on due date				
				// Get assignment associated with current item's task Id
				Assignment assignment = assignmentService.getAssignment(entityManager.newReference(item.getTaskId()));
				Date assignmentDueDate = null;
				String reportGenSpeed = null;
				if(assignment != null) {
					assignmentDueDate = Date.from(assignment. getDueDate());					
					reportGenSpeed = assignment.getProperties().get("report_gen_speed");
					// If report gen speed is set to due date, and it's before the due date right now, do not process item
					if (assignmentDueDate != null && GENERATE_REPORTS_ON_DUE_DATE.equals(reportGenSpeed) 
							&& assignmentDueDate.after(new Date())) {
						log.info("Report generate speed is 2, skipping for now. ItemID: " + item.getId());
						// We don't items with gen speed 2 items to exceed retry count maximum
						// Reset retry count to zero
						item.setRetryCount(Long.valueOf(0));
						item.setNextRetryTime(getDueDateRetryTime(assignmentDueDate));
						crqs.update(item);
						continue;
					}
				}
				//Paper is ready to be submitted
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
							Calendar cal = Calendar.getInstance();
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

						if (COMPLETE_STATUS.equals(submissionStatus)) {
							success++;
						} else if (CREATED_STATUS.equals(submissionStatus) || PROCESSING_STATUS.equals(submissionStatus)) {
							// do nothing item is still being processes
						} else {
							// returned with an error status
							errors++;
						}

						handleSubmissionStatus(submissionStatus, item, assignment);

					} catch (Exception e) {
						log.error(e.getMessage(), e);
						item.setLastError(e.getMessage());
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
						crqs.update(item);
						errors++;
					}
				}
			} catch (Exception e) {				
				log.error(e.getMessage(), e);
			}
		}		
		log.info("Turnitin submission queue completed: " + success + " items submitted, " + errors + " errors.");		
	}
	
	private Date getDueDateRetryTime(Date dueDate) {
		// Set retry time to every 4 hours
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 4);
		// If due date is less than 4 hours away, set retry time to due date + five minutes
		if (dueDate != null && cal.getTime().after(dueDate)) {
			cal.setTime(dueDate);
			cal.add(Calendar.MINUTE, 5);
		}
		return cal.getTime();
	}

	private boolean checkForDraft(ContentReviewItem item, Assignment assignment) {
		// Checks if current item is a draft or submitted
		try {
			AssignmentSubmission currentSubmission = assignmentService.getSubmission(assignment.getId(), item.getUserId());
			return Optional.ofNullable(!currentSubmission.getSubmitted()).orElse(false);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// Error retrieving draft, process item as final submission
			return false;
		}
	}

	private void createPlaceholderItem(ContentReviewItem item, Date dueDate) {
		log.info("Creating placeholder item for when due date is passed for ItemID: " + item.getId());						
		ContentReviewItem placeholderItem = new ContentReviewItem();
		// Review score is used as flag for placeholder items in checkForReport
		placeholderItem.setReviewScore(PLACEHOLDER_ITEM_REVIEW_SCORE); 
		// Content Id must be original
		placeholderItem.setContentId(item.getContentId() + PLACEHOLDER_STRING_FLAG);
		// This is needed for webhook call, without it external id query does not return a single item
		placeholderItem.setExternalId(item.getExternalId() + PLACEHOLDER_STRING_FLAG);
		placeholderItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
		placeholderItem.setNextRetryTime(getDueDateRetryTime(dueDate));
		placeholderItem.setDateQueued(new Date());
		placeholderItem.setDateSubmitted(new Date());
		placeholderItem.setRetryCount(new Long(0));	
		// All other fields are copied from original item
		placeholderItem.setProviderId(item.getProviderId());						
		placeholderItem.setUserId(item.getUserId());
		placeholderItem.setSiteId(item.getSiteId());
		placeholderItem.setTaskId(item.getTaskId());																	
		crqs.update(placeholderItem);
	}

	private void handleSubmissionStatus(String submissionStatus, ContentReviewItem item, Assignment assignment) {
		try {

			Date assignmentDueDate = Date.from(assignment.getDueDate());
			String reportGenSpeed = assignment.getProperties().get("report_gen_speed");

			// Handle possible error status
			String errorStr = null;

			switch (submissionStatus) {
			case "COMPLETE":
				// Check if current item is a draft submission
				boolean submissionIsDraft = checkForDraft(item, assignment);
				// If submission status is complete, start similarity report process
				generateSimilarityReport(item.getExternalId(), item.getTaskId(), submissionIsDraft);
				// Update item status for loop 2
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
				// Reset retry count
				item.setRetryCount(new Long(0));
				Calendar cal = Calendar.getInstance();
				// Reset cal to current time
				cal.setTime(new Date());
				// Reset delay time
				cal.add(Calendar.MINUTE, getDelayTime(item.getRetryCount()));
				// Schedule next retry time
				item.setNextRetryTime(cal.getTime());
				crqs.update(item);
				// Check for items that generate reports both immediately and on due date or draft items
				// Create a placeholder item that will regenerate and index report after due date
				if (assignmentDueDate != null && assignmentDueDate.after(new Date())
						&& GENERATE_REPORTS_IMMEDIATELY_AND_ON_DUE_DATE.equals(reportGenSpeed) || submissionIsDraft) {
					createPlaceholderItem(item, assignmentDueDate);
				}
				break;
			case "PROCESSING":
				// do nothing... try again
				break;
			case "CREATED":
				// do nothing... try again
				break;
			case "UNSUPPORTED_FILETYPE":
				errorStr = "The uploaded filetype is not supported";
				break;
				//break on all
			case "PROCESSING_ERROR":
				errorStr = "An unspecified error occurred while processing the submissions";
				break;
			case "TOO_LITTLE_TEXT":
				errorStr = "The submission does not have enough text to generate a Similarity Report (a submission must contain at least 20 words)";
				break;
			case "TOO_MUCH_TEXT":
				errorStr = "The submission has too much text to generate a Similarity Report (after extracted text is converted to UTF-8, the submission must contain less than 2MB of text)";
				break;
			case "TOO_MANY_PAGES":
				errorStr = "The submission has too many pages to generate a Similarity Report (a submission cannot contain more than 400 pages)";
				break;
			case "FILE_LOCKED":
				errorStr = "The uploaded file requires a password in order to be opened";
				break;
			case "CORRUPT_FILE":
				errorStr = "The uploaded file appears to be corrupt";
				break;
			case "ERROR":
				errorStr = "Submission returned with ERROR status";
				break;
			default:
				log.info("Unknown submission status, will retry: " + submissionStatus);
			}
			if(StringUtils.isNotEmpty(errorStr)) {
				item.setLastError(errorStr);
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
			}
		}  catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void handleReportStatus(ContentReviewItem item, int status) throws Exception {
		// Any status above -1 is the report score
		if (status > -1) {
			log.info("Report complete! Score: " + status);
			// Status value is report score
			item.setReviewScore(status);
			item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
			item.setDateReportReceived(new Date());
			item.setRetryCount(Long.valueOf(0));
			item.setLastError(null);
			item.setErrorCode(null);
			crqs.update(item);
		} else if (status == -1) {
			// Similarity report is still generating, will try again
			log.info("Processing report " + item.getExternalId() + "...");
		} else if(status == -2){
			throw new Exception("Unknown error during report status call");
		}
	}
	
	public boolean incrementItem(ContentReviewItem item) {
		// If retry count is null set to 0
		Calendar cal = Calendar.getInstance();
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
		Optional<ContentReviewItem> optionalItem = crqs.getQueuedItem(getProviderId(), contentId);
		ContentReviewItem item = optionalItem.isPresent() ? optionalItem.get() : null;
		if(item != null && ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE.equals(item.getStatus())) {
			//user initiated this request but the report timed out, let's requeue this report and try again:
			item.setStatus(StringUtils.isEmpty(item.getExternalId()) ? ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE : ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE);
			item.setRetryCount(0l);
			item.setLastError(null);
			item.setNextRetryTime(Calendar.getInstance().getTime());
			crqs.update(item);
		}
		return item;
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

	public static String getSigningSignature(byte[] key, String data) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
		sha256_HMAC.init(secret_key);
		return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
	}

	public static String base64Encode(String src) {
		return Base64.getEncoder().encodeToString(src.getBytes());
	}
	
	@Override
	public void webhookEvent(HttpServletRequest request, String providerName, Optional<String> customParam) {
		log.info("providerName: " + providerName + ", custom: " + (customParam.isPresent() ? customParam.get() : ""));
		int errors = 0;
		int success = 0;
		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			errors++;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					errors++;
				}
			}
		}

		body = stringBuilder.toString();
		JSONObject webhookJSON = JSONObject.fromObject(body);
		String eventType = request.getHeader("X-Turnitin-Eventtype");
		String signature_header = request.getHeader("X-Turnitin-Signature");


		try {
			// Make sure cb is signed correctly
			String secrete_key_encoded = getSigningSignature(apiKey.getBytes(), body);
			if (StringUtils.isNotEmpty(secrete_key_encoded) && signature_header.equals(secrete_key_encoded)) {
				if (SUBMISSION_COMPLETE_EVENT_TYPE.equals(eventType)) {
					if (webhookJSON.has("id") && STATUS_COMPLETE.equals(webhookJSON.get("status"))) {
						// Allow cb to access assignment settings, needed for draft check
						SecurityAdvisor advisor = pushAdvisor();
						log.info("Submission complete webhook cb received");
						log.info(webhookJSON.toString());
						Optional<ContentReviewItem> optionalItem = crqs.getQueuedItemByExternalId(getProviderId(), webhookJSON.getString("id"));
						ContentReviewItem item = optionalItem.isPresent() ? optionalItem.get() : null;
						Assignment assignment = assignmentService.getAssignment(entityManager.newReference(item.getTaskId()));
						handleSubmissionStatus(webhookJSON.getString("status"), item, assignment);
						// Remove advisor override
						popAdvisor(advisor);
						success++;
					} else {
						log.warn("Callback item received without needed information");
						errors++;
					}
				} else if (SIMILARITY_COMPLETE_EVENT_TYPE.equals(eventType)) {
					if (webhookJSON.has("submission_id") && STATUS_COMPLETE.equals(webhookJSON.get("status"))) {
						log.info("Similarity complete webhook cb received");
						log.info(webhookJSON.toString());
						Optional<ContentReviewItem> optionalItem = crqs.getQueuedItemByExternalId(getProviderId(), webhookJSON.getString("submission_id"));
						ContentReviewItem item = optionalItem.isPresent() ? optionalItem.get() : null;
						handleReportStatus(item, webhookJSON.getInt("overall_match_percentage"));
						success++;
					} else {
						log.warn("Callback item received without needed information");
						errors++;
					}
				}
			} else {
				log.warn("Callback signatures did not match");
				errors++;
			}
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		}
		log.info("Turnitin webhook received: " + success + " items processed, " + errors + " errors.");
	}
	
	@Getter
	@AllArgsConstructor
	private class Webhook {
		private String id;
		private String url;
	}
}
