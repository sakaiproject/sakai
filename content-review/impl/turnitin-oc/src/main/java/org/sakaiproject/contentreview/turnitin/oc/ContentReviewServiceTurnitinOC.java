/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.contentreview.turnitin.oc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.sakaiproject.contentreview.service.ContentReviewQueueService;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Slf4j
public class ContentReviewServiceTurnitinOC implements ContentReviewService {
	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter
	private EntityManager entityManager;

	@Setter
	private SecurityService securityService;

	@Setter
	private SiteService siteService;

	@Setter
	private ContentReviewQueueService crqs;

	@Setter
	private ContentHostingService contentHostingService;

	private static final String SERVICE_NAME = "TurnitinOC";
	private static final String TURNITIN_OC_API_VERSION = "v1";
	// private static final int TURNITIN_OC_RETRY_TIME_MINS = 30;
	private static final int TURNITIN_MAX_RETRY = 8;
	private static final String INTEGRATION_VERSION = "1.0";
	private static final String INTEGRATION_FAMILY = "sakai";
	// API key
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String CONTENT_TYPE_BINARY = "application/octet-stream";
	private static final String HEADER_NAME = "X-Turnitin-Integration-Name";
	private static final String HEADER_VERSION = "X-Turnitin-Integration-Version";
	private static final String HEADER_AUTH = "Authorization";
	private static final String HEADER_CONTENT = "Content-Type";
	private static final String HEADER_DISP = "Content-Disposition";
	private static final String FAKE_URL = "https://test.turnitin.com/api/v1/";

	private String serviceUrl;
	private String apiKey;
	
	HashMap<String, String> baseHeaders = new HashMap<String, String>();
	HashMap<String, String> getIdHeaders = new HashMap<String, String>();	

	public void init() {
		serviceUrl = serverConfigurationService.getString("turnitin.oc.serviceUrl", "");
		apiKey = serverConfigurationService.getString("turnitin.oc.apiKey", "");
		
		baseHeaders.put(HEADER_NAME, INTEGRATION_FAMILY);
		baseHeaders.put(HEADER_VERSION, INTEGRATION_VERSION);
		baseHeaders.put(HEADER_AUTH, "Bearer " + apiKey);
		
		getIdHeaders.putAll(baseHeaders);
		getIdHeaders.put(HEADER_CONTENT, CONTENT_TYPE_JSON);
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

	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return getAccessUrl(contentId, assignmentRef, userId, false);
	}

	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		/**
		 * contentId:
		 * /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
		 */
		return getAccessUrl(contentId, assignmentRef, userId, true);
	}

	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return getAccessUrl(contentId, assignmentRef, userId, false);
	}

	private String getAccessUrl(String contentId, String assignmentRef, String userId, boolean instructor)
			throws QueueException, ReportException {
		
		try {
			User user = userDirectoryService.getUser(userId);
			JSONObject body = new JSONObject();
			body.put("given_name", user.getFirstName());
			body.put("family_name", user.getLastName());
			
			// TODO: FIGURE OUT HOW TO GET LOCALE!!!
			// body.put("locale", locale);
			body.put("locale", "en");

			OkHttpClient client = new OkHttpClient();
			Headers.Builder builder = new Headers.Builder();

			String headerString = Objects.toString(getIdHeaders, "");
			// {x-amz-server-side-encryption=AES256}
			headerString = headerString.replace("{", "").replace("}", "");
			String[] headerPairs = headerString.split(",");

			for (String headerPair : headerPairs) {
				headerPair = headerPair.trim();
				String[] pairKeyValue = headerPair.split("=", 2);
				if (pairKeyValue.length == 2) {
					builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
				}
			}
			
			// TODO: GET EXTERNAL ID FROM CONTENT ID
			String externalId = "";

			Headers headers = builder.build();
			Request request = new Request.Builder()
					.url(getNormalizedServiceUrl() + "submissions/" + externalId + "/viewer-url")
					.put(RequestBody.create(com.squareup.okhttp.MediaType.parse(MediaType.APPLICATION_JSON), body.toString()))
					.headers(headers)				
					.build();

			com.squareup.okhttp.Response response = client.newCall(request).execute();
			ResponseBody responseBody = response.body();
			JSONObject responseJSON = JSONObject.fromObject(responseBody.string());
			responseBody.close();
			
			if ((response.code() >= 200) && (response.code() < 300)) {
				log.info("Successfully retrieved viewer url");
				
				if (responseJSON.containsKey("viewer_url")) {
					return responseJSON.getString("viewer_url");
				} else {
					throw new Error("Failed to retrieve report viewer url (1)");
				}
			} else {
				throw new Error("Failed to retrieve report viewer url (2)");
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
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
	
	private boolean getSimilarityReport(String reportId)
			throws MalformedURLException, IOException {
		JSONObject body = new JSONObject();
		JSONObject genSettings = new JSONObject();
		JSONObject viewSettings = new JSONObject();
		JSONArray searchList = new JSONArray();
		
		searchList.add("INTERNET");
		searchList.add("PRIVATE");
		
		genSettings.put("search_repositories", searchList);
		
		viewSettings.put("exclude_quotes", true);
		viewSettings.put("exclude_bibliography", true);
		
		body.put("generation_settings", genSettings);
		body.put("view_settings", viewSettings);

		OkHttpClient client = new OkHttpClient();
		Headers.Builder builder = new Headers.Builder();

		String headerString = Objects.toString(getIdHeaders, "");
		// {x-amz-server-side-encryption=AES256}
		headerString = headerString.replace("{", "").replace("}", "");
		String[] headerPairs = headerString.split(",");

		for (String headerPair : headerPairs) {
			headerPair = headerPair.trim();
			String[] pairKeyValue = headerPair.split("=", 2);
			if (pairKeyValue.length == 2) {
				builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
			}
		}

		Headers headers = builder.build();
		Request request = new Request.Builder()
				.url(getNormalizedServiceUrl() + "submissions/" + reportId + "/similarity")
				.put(RequestBody.create(com.squareup.okhttp.MediaType.parse(MediaType.APPLICATION_JSON), body.toString()))
				.headers(headers)				
				.build();

		com.squareup.okhttp.Response response = client.newCall(request).execute();
		ResponseBody responseBody = response.body();

		try {
			if ((response.code() >= 200) && (response.code() < 300)) {
				log.info("Successfully initiated Similarity Report generation!!!!!" );
				return true;
			} else if ((response.code() == 409)) {
				log.info("A Similarity Report is already generating for this submission");
				return false;
			}
			
			else {
				throw new Error("Submission failed to initiate (2)");
			}
		} finally {
			responseBody.close();
		}
	}

	private boolean getSubmissionStatus(String reportId)
			throws MalformedURLException, IOException {		

		OkHttpClient client = new OkHttpClient();
		Headers.Builder builder = new Headers.Builder();

		String headerString = Objects.toString(baseHeaders, "");
		// {x-amz-server-side-encryption=AES256}
		headerString = headerString.replace("{", "").replace("}", "");
		String[] headerPairs = headerString.split(",");

		for (String headerPair : headerPairs) {
			headerPair = headerPair.trim();
			String[] pairKeyValue = headerPair.split("=", 2);
			if (pairKeyValue.length == 2) {
				builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
			}
		}

		Headers headers = builder.build();
		Request request = new Request.Builder()
				.url(getNormalizedServiceUrl() + "submissions/" + reportId)
				.headers(headers)				
				.build();

		com.squareup.okhttp.Response response = client.newCall(request).execute();
		ResponseBody responseBody = response.body();
		
		if ((response.code() >= 200) && (response.code() < 300)) {
			JSONObject responseJSON = JSONObject.fromObject(responseBody.string());
			responseBody.close();
			log.info("STATUS " + responseJSON.getString("status"));
			// TODO add error catches for too little text, etc
			if (responseJSON.containsKey("status") && responseJSON.getString("status").equals("COMPLETE")) {
				// Only call getSimilarityReport if the report doesn't already exist
				return true;
			} else if (responseJSON.containsKey("status") && responseJSON.getString("status").equals("PENDING")) {
				return false;
			} else {
				throw new Error("Report is not complete or pending");
			}
		} else {
			throw new Error(response.message());
		}
	}
	
	private int getSimilarityReportStatus(String reportId)
			throws MalformedURLException, IOException {		

		OkHttpClient client = new OkHttpClient();
		Headers.Builder builder = new Headers.Builder();

		String headerString = Objects.toString(baseHeaders, "");
		// {x-amz-server-side-encryption=AES256}
		headerString = headerString.replace("{", "").replace("}", "");
		String[] headerPairs = headerString.split(",");

		for (String headerPair : headerPairs) {
			headerPair = headerPair.trim();
			String[] pairKeyValue = headerPair.split("=", 2);
			if (pairKeyValue.length == 2) {
				builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
			}
		}

		Headers headers = builder.build();
		Request request = new Request.Builder()
				.url(getNormalizedServiceUrl() + "submissions/" + reportId + "/similarity")
				.headers(headers)				
				.build();

		com.squareup.okhttp.Response response = client.newCall(request).execute();
		ResponseBody responseBody = response.body();
		JSONObject responseJSON = JSONObject.fromObject(responseBody.string());
		responseBody.close();
		
		if ((response.code() >= 200) && (response.code() < 300)) {
			// See if report is complete or pending. If pending, ignore, if complete, get score and viewer url
			log.info("SIMILARITY REPORT EXISTS!!!!!");
			
			if (responseJSON.containsKey("status") && responseJSON.getString("status").equals("COMPLETE")) {
				log.info("Report is complete!!");
				log.debug("Submission successful");
				if (responseJSON.containsKey("overall_match_percentage")) {
					return responseJSON.getInt("overall_match_percentage");
				} else {
					log.warn("Report complete but no overall match percentage, defaulting to 0");
					return 0;
				}
				
			} else if (responseJSON.containsKey("status") && responseJSON.getString("status").equals("PROCESSING")) {
				log.info("REPORT IS PROCESSING...");
				return -1;
			} else {
				throw new Error("Something went wrong in the similarity report process: reportId " + reportId);
			}
		} else {
			throw new Error(response.message());
		}
	}

	private String getSubmissionId(String userID, String fileName)
			throws MalformedURLException, IOException {
		JSONObject body = new JSONObject();
		body.put("owner", userID);
		body.put("title", fileName);

		OkHttpClient client = new OkHttpClient();
		Headers.Builder builder = new Headers.Builder();

		String headerString = Objects.toString(getIdHeaders, "");
		// {x-amz-server-side-encryption=AES256}
		headerString = headerString.replace("{", "").replace("}", "");
		String[] headerPairs = headerString.split(",");

		for (String headerPair : headerPairs) {
			headerPair = headerPair.trim();
			String[] pairKeyValue = headerPair.split("=", 2);
			if (pairKeyValue.length == 2) {
				builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
			}
		}

		Headers headers = builder.build();
		Request request = new Request.Builder()
				.url(getNormalizedServiceUrl() + "submissions").headers(headers).post(RequestBody
						.create(com.squareup.okhttp.MediaType.parse(MediaType.APPLICATION_JSON), body.toString()))
				.build();

		com.squareup.okhttp.Response response = client.newCall(request).execute();
		ResponseBody responseBody = response.body();
		JSONObject responseJSON = JSONObject.fromObject(responseBody.string());
		responseBody.close();

		if ((response.code() >= 200) && (response.code() < 300)) {
			if (responseJSON.containsKey("status") && responseJSON.getString("status").equals("CREATED")
					&& responseJSON.containsKey("id")) {
				return responseJSON.getString("id");
			} else {
				throw new Error("Submission not created, or has no ID");
			}

		} else {
			throw new Error(response.message());
		}
	}

	public void processQueue() {
		log.info("Processing Turnitin OC submission queue");
		int errors = 0;
		int success = 0;		
		Optional<ContentReviewItem> nextItem = null;		
		
		// NOT SUBMITTED, CREATE SUBMISSION AND UPLOAD CONTENTS (STAGE !)
		while ((nextItem = crqs.getNextItemInQueueToSubmit(getProviderId())).isPresent()) {
			ContentReviewItem item = nextItem.get();
			Long status = item.getStatus();
			log.info("ITEM STATUS: " + status);
			Calendar cal = Calendar.getInstance();
			if (item.getRetryCount() == null) {
				item.setRetryCount(Long.valueOf(0));
				item.setNextRetryTime(cal.getTime());
				crqs.update(item);
			} else if (item.getRetryCount().intValue() > TURNITIN_MAX_RETRY) {
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE);
				crqs.update(item);
				errors++;
				continue;
			} else {
				long l = item.getRetryCount().longValue();
				l++;
				item.setRetryCount(Long.valueOf(l));
				cal.add(Calendar.MINUTE, getDelayTime(l));
				item.setNextRetryTime(cal.getTime());
				crqs.update(item);
			}

			ContentResource resource = null;
			try {
				resource = contentHostingService.getResource(item.getContentId());
			} catch (IdUnusedException e4) {
				log.error("IdUnusedException: no resource with id " + item.getContentId());
				item.setLastError("IdUnusedException: no resource with id " + item.getContentId());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			} catch (PermissionException e) {
				log.error("PermissionException: no resource with id " + item.getContentId());
				item.setLastError("PermissionException: no resource with id " + item.getContentId());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			} catch (TypeException e) {
				log.error("TypeException: no resource with id " + item.getContentId());
				item.setLastError("TypeException: no resource with id " + item.getContentId());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			}
			String fileName = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if (StringUtils.isEmpty(fileName)) {
				// set default file name:
				fileName = "submission_" + item.getUserId() + "_" + item.getSiteId();
				log.info("Using Default Filename " + fileName);
			}

			String userId = item.getUserId();

			if ((item.getExternalId() == null) || item.getExternalId().trim().isEmpty()) {
				try {
					log.info("Submission starting...");
					
					HashMap<String, String> uploadHeaders = new HashMap<String, String>();
					uploadHeaders.putAll(baseHeaders);
					uploadHeaders.put(HEADER_DISP, "inline; filename=\"" + fileName + "\"");
					uploadHeaders.put(HEADER_CONTENT, CONTENT_TYPE_BINARY);
					
					String reportId = getSubmissionId(userId, fileName);

					uploadExternalContent(reportId, resource.getContent(), uploadHeaders);

					item.setExternalId(reportId);
					item.setRetryCount(new Long(0));
					item.setNextRetryTime(new Date(cal.getTimeInMillis() + 120000));
					item.setDateSubmitted(new Date());
					crqs.update(item);
					success++;
				} catch (Exception e) {
					log.error(e.getMessage());
					log.warn("ServerOverloadException: " + item.getContentId(), e);
					item.setLastError(e.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE);
					crqs.update(item);
					errors++;
				}
			} else {
				String external_id = item.getExternalId();
				log.info("ID AWAITING REPORT " + external_id);
				
				try {
					if (getSubmissionStatus(external_id)) {
						if (getSimilarityReport(external_id)) {
							item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
							item.setRetryCount(new Long(0));
							item.setNextRetryTime(new Date(cal.getTimeInMillis() + 120000));
							crqs.update(item);
							success++;
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					item.setLastError(e.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
					crqs.update(item);
					errors++;
				}	
			}

		}
		
		// UPLOADED CONTENTS, AWAITING REPORT (STAGE 2)
		for(ContentReviewItem item : crqs.getAwaitingReports(getProviderId())) {
			// Make sure it's after the next retry time
			if (item.getNextRetryTime().getTime() > new Date().getTime()) {
				continue;
			}
			
			try {
				int status = getSimilarityReportStatus(item.getExternalId());
				if (status > -1) {
					// SUCCESS
					log.info("Report complete! Score: " + status);
					// status is report score
					item.setReviewScore(status);
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
					item.setDateReportReceived(new Date());
					item.setRetryCount(Long.valueOf(0));
					item.setLastError(null);
					item.setErrorCode(null);
					crqs.update(item);
				} else if (status == -1) {
					// PROCESSING, SKIP
					log.info("Processing report " + item.getExternalId() + "...");
				} else {
					throw new Error("Report score returned negative value");
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				item.setLastError(e.getMessage());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
			}
		}
				
		log.info("Submission Turnitin queue run completed: " + success + " items submitted, " + errors + " errors.");
	}
	
	public int getDelayTime(long l) {
		// Pattern:
		double d = l == TURNITIN_MAX_RETRY ? 2 : (double) l;
		return (int) Math.round(Math.pow(2, d));
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

	private void uploadExternalContent(String reportId, byte[] data, Object headersMap) throws IOException {
		OkHttpClient client = new OkHttpClient();

		Headers.Builder builder = new Headers.Builder();

		String headerString = Objects.toString(headersMap, "");
		// {x-amz-server-side-encryption=AES256}
		headerString = headerString.replace("{", "").replace("}", "");
		String[] headerPairs = headerString.split(",");

		for (String headerPair : headerPairs) {
			headerPair = headerPair.trim();
			String[] pairKeyValue = headerPair.split("=", 2);
			if (pairKeyValue.length == 2) {
				builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
			}
		}

		Headers headers = builder.build();
		Request request = new Request.Builder()
				.url(getNormalizedServiceUrl() + "submissions/" + reportId + "/original/").headers(headers)
				.put(RequestBody.create(com.squareup.okhttp.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM),
						data))
				.build();

		com.squareup.okhttp.Response response = client.newCall(request).execute();

		int responseCode = response.code();
		if (responseCode < 200 || responseCode >= 300) {
			throw new Error("Turnitin upload content failed with code: " + responseCode);
		} else {
			log.info("SUCCESS!!!");
		}
	}

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId) {
		Optional<ContentReviewItem> cri = crqs.getQueuedItem(getProviderId(), contentId);
		if (cri.isPresent()) {
			ContentReviewItem item = cri.get();

			// Turnitin specific work
			try {
				int score = getReviewScore(contentId, item.getTaskId(), null);
				log.debug(" getReviewScore returned a score of: {} ", score);
			} catch (Exception e) {
				log.error(
						"Turnitin - getReviewScore error called from getContentReviewItemByContentId with content {} - {}",
						contentId, e.getMessage());
			}

			return item;
		}
		return null;
	}
}
