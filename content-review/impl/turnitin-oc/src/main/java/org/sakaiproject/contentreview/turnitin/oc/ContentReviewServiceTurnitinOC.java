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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;

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
import org.sakaiproject.user.api.UserDirectoryService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
	private static final int TURNITIN_OC_MAX_RETRY_MINUTES = 240; //4 hours
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

	private String serviceUrl;
	private String apiKey;
	
	private HashMap<String, String> BASE_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SUBMISSION_REQUEST_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SIMILARITY_REPORT_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> CONTENT_UPLOAD_HEADERS = new HashMap<String, String>();
	
	public void init() {
		//Retrieve Service URL and API key
		serviceUrl = serverConfigurationService.getString("turnitin.oc.serviceUrl", "");
		apiKey = serverConfigurationService.getString("turnitin.oc.apiKey", "");
		
		//Populate base headers that are needed for all calls to TCA
		BASE_HEADERS.put(HEADER_NAME, INTEGRATION_FAMILY);
		BASE_HEADERS.put(HEADER_VERSION, INTEGRATION_VERSION);
		BASE_HEADERS.put(HEADER_AUTH, "Bearer " + apiKey);

		//Populate submission request headers used in getSubmissionId
		SUBMISSION_REQUEST_HEADERS.putAll(BASE_HEADERS);
		SUBMISSION_REQUEST_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_JSON);
		
		//Populate similarity report headers used in generateSimilarityReport
		SIMILARITY_REPORT_HEADERS.putAll(BASE_HEADERS);
		SIMILARITY_REPORT_HEADERS.put(HEADER_CONTENT, CONTENT_TYPE_JSON);
		
		//Populate content upload headers used in uploadExternalContent
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

	private String getAccessUrl(String reportId, String assignmentRef, String userId, boolean instructor)
			throws QueueException, ReportException {
		
//		try {
//			User user = userDirectoryService.getUser(userId);
//			JSONObject body = new JSONObject();
//			body.put("given_name", user.getFirstName());
//			body.put("family_name", user.getLastName());
//			
//			// TODO: FIGURE OUT HOW TO GET LOCALE!!!
//			// body.put("locale", locale);
//			body.put("locale", "en");
//
//			OkHttpClient client = new OkHttpClient();
//			Headers.Builder builder = new Headers.Builder();
//
//			String headerString = Objects.toString(SUBMISSION_REQUEST_HEADERS, "");
//			// {x-amz-server-side-encryption=AES256}
//			headerString = headerString.replace("{", "").replace("}", "");
//			String[] headerPairs = headerString.split(",");
//
//			for (String headerPair : headerPairs) {
//				headerPair = headerPair.trim();
//				String[] pairKeyValue = headerPair.split("=", 2);
//				if (pairKeyValue.length == 2) {
//					builder.add(pairKeyValue[0].trim(), pairKeyValue[1].trim());
//				}
//			}
//						
//			Headers headers = builder.build();
//			Request request = new Request.Builder()
//					.url(getNormalizedServiceUrl() + "submissions/" + reportId + "/viewer-url")
//					.put(RequestBody.create(MediaType.parse(MediaType.APPLICATION_JSON), body.toString()))
//					.headers(headers)				
//					.build();
//
//			Response response = client.newCall(request).execute();
//			ResponseBody responseBody = response.body();
//			JSONObject responseJSON = JSONObject.fromObject(responseBody.string());
//			responseBody.close();
//			
//			if ((response.code() >= 200) && (response.code() < 300)) {
//				log.info("Successfully retrieved viewer url");
//				
//				if (responseJSON.containsKey("viewer_url")) {
//					return responseJSON.getString("viewer_url");
//				} else {
//					throw new Error("Failed to retrieve report viewer url (1)");
//				}
//			} else {
//				throw new Error("Failed to retrieve report viewer url (2)");
//			}
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
			return null;
		
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
	
	private void generateSimilarityReport(String reportId) throws Exception {
				
		// set variables
		HttpURLConnection connection = null;
		DataOutputStream wr = null;
		URL url = null;
			
		//Construct URL
		url = new URL(getNormalizedServiceUrl() + "submissions/" + reportId + "/similarity");

		//Open connection and set HTTP method
		connection=(HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		
		// Build JSON header maps
		Map<String, Object> reportData = new HashMap<String, Object>();
		Map<String, Object> generationSearchSettings = new HashMap<String, Object>();
		generationSearchSettings.put("search_repositories", Arrays.asList("INTERNET", "PRIVATE"));
		reportData.put("generation_settings", generationSearchSettings);		
		
		Map<String, Object> viewSettings = new HashMap<String, Object>();
		viewSettings.put("exclude_quotes", Boolean.TRUE);
		viewSettings.put("exclude_bibliography", Boolean.TRUE);
		reportData.put("view_settings", viewSettings);
		
		// Set headers 
		for(Entry<String, String> entry : SIMILARITY_REPORT_HEADERS.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		//Convert data to JSON:
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(reportData);

		//Set Post body:
		connection.setDoOutput(true);
		wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(json);
		wr.flush();
		wr.close();
		
		//Send request:
		int responseCode = connection.getResponseCode();
		String responseMessage = connection.getResponseMessage();			
		String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);						        
		
		if ((responseCode >= 200) && (responseCode < 300)) {
			log.info("Successfully initiated Similarity Report generation!!!!!" );
		} else if ((responseCode == 409)) {
			log.info("A Similarity Report is already generating for this submission");
		} else {
			throw new Error("Submission failed to initiate: " + responseCode + ", " + responseMessage + ", " + responseBody);
		}
	}

	private String getSubmissionStatus(String reportId) throws Exception {		
		
		// set variables
		URL url = null;
		HttpURLConnection connection = null;		
		String status = null;
		
		//Construct URL
		url = new URL(getNormalizedServiceUrl() + "submissions/" + reportId);

		//Open connection and set HTTP method
		connection=(HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		
		// Set headers 
		for(Entry<String, String> entry : BASE_HEADERS.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}		
		
		//Send request:
		int responseCode = connection.getResponseCode();
		String responseMessage = connection.getResponseMessage();					
		String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);				
		
		// create JSONObject from response	
		JSONObject responseJSON = JSONObject.fromObject(responseBody);
									
		if ((responseCode >= 200) && (responseCode < 300)) {
			log.info("STATUS " + responseJSON.getString("status"));
			// TODO add error catches for too little text, etc
			if (responseJSON.containsKey("status")) {
				// Only call getSimilarityReport if the report doesn't already exist
				status = responseJSON.getString("status");
			}
		}else {
			throw new Exception("getSubmissionStatus invalid request: " + responseCode + ", " + responseMessage  + ", " + responseBody);
		}
		
		return status;
	}
	
	private int getSimilarityReportStatus(String reportId) throws Exception {		

		//Set variables
		URL url = null;
		HttpURLConnection connection = null;				
		
		//Construct URL
		url = new URL(getNormalizedServiceUrl() + "submissions/" + reportId + "/similarity");

		//Open connection and set HTTP method
		connection=(HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		
		// Set headers 
		for(Entry<String, String> entry : BASE_HEADERS.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}		
		
		//Send request:
		int responseCode = connection.getResponseCode();
		String responseMessage = connection.getResponseMessage();					
		String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);				
		
		// create JSONObject from response	
		JSONObject responseJSON = JSONObject.fromObject(responseBody);				
		
		if ((responseCode >= 200) && (responseCode < 300)) {
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
			throw new Error(responseMessage);
		}
	}

	private String getSubmissionId(String userID, String fileName){
		
		//Set variables
		String submissionId = null;		
		URL url = null;
		HttpURLConnection connection = null;
		DataOutputStream wr = null;
		
		
		try {			
			
			// Construct URL
			url = new URL(getNormalizedServiceUrl() + "submissions");
			
			//Open connection and set HTTP method
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);		
			connection.setRequestMethod("POST");						
				
			// Build JSON header maps
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("owner", userID);
			data.put("title", fileName);
													
			// Set headers 
			for(Entry<String, String> entry : SUBMISSION_REQUEST_HEADERS.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
			
			//Convert data to JSON:
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(data);
			
			//Set Post body:
			connection.setDoOutput(true);
			wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(json);
			wr.flush();
			wr.close();
			
			//Send request:
			int responseCode = connection.getResponseCode();
			String responseMessage = connection.getResponseMessage();			
			String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
						
			
			// create JSONObject from responseBody
			JSONObject responseJSON = JSONObject.fromObject(responseBody);			
																	
			if ((responseCode >= 200) && (responseCode < 300)) {
				//TODO: make CREATED a constant
				if (responseJSON.containsKey("status") && responseJSON.getString("status").equals("CREATED")
						&& responseJSON.containsKey("id")) {
					submissionId = responseJSON.getString("id");
				} else {
					//TODO: need better error handling for different statuses returned from TII
					
					log.error("getSubmissionId response: " + responseJSON);
				}
			}else {
				log.error("getSubmissionId response code: " + responseCode + ", " + responseMessage + ", " + responseJSON);
			}
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		return submissionId;
				
	}
	
	// Queue service for processing student submissions
	// Stage one creates a submission, uploads submission and sets item externalId 
	// Stage two starts similarity report process
	// Stage three checks status of similarity reports and retrieves report score
	// Loop 1 contains stage one and two, Loop 2 contains stage three
	public void processQueue() {
		log.info("Processing Turnitin OC submission queue");
		int errors = 0;
		int success = 0;		
		Optional<ContentReviewItem> nextItem = null;		
		
		// LOOP 1
		while ((nextItem = crqs.getNextItemInQueueToSubmit(getProviderId())).isPresent()) {
			ContentReviewItem item = nextItem.get();
			log.info("ITEM STATUS: " + item.getStatus());
			// Create new Calendar instance used for adding delay time to current time
			Calendar cal = Calendar.getInstance();
			// If retry count is null set to 0
			if (item.getRetryCount() == null) {
				item.setRetryCount(Long.valueOf(0));
				item.setNextRetryTime(cal.getTime());
				crqs.update(item);
			// If retry count is above maximum increment error count, set status to nine and stop retrying
			} else if (item.getRetryCount().intValue() > TURNITIN_MAX_RETRY) {
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE);
				crqs.update(item);
				errors++;
				continue;
			// Increment retry count, adjust delay time, schedule next retry attempt 
			} else {
				long retryCount = item.getRetryCount().longValue();
				retryCount++;
				item.setRetryCount(Long.valueOf(retryCount));
				cal.add(Calendar.MINUTE, getDelayTime(retryCount));
				item.setNextRetryTime(cal.getTime());
				crqs.update(item);
			}

			ContentResource resource = null;
			try {
				//Get resource with current item's content Id 
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
			// Get filename of submission 
			String fileName = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if (StringUtils.isEmpty(fileName)) {
				// set default file name:
				fileName = "submission_" + item.getUserId() + "_" + item.getSiteId();
				log.info("Using Default Filename " + fileName);
			}
			// EXTERNAL ID DOES NOT EXIST, CREATE SUBMISSION AND UPLOAD CONTENTS TO TCA (STAGE 1)
			if (StringUtils.isEmpty(item.getExternalId())) {
				try {
					log.info("Submission starting...");	
					// Retrieve submissionId from TCA and set to externalId
					String externalId = getSubmissionId(item.getUserId(), fileName);
					if(StringUtils.isEmpty(externalId)) {
						throw new Error("submission id is missing");

					}else {						
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
			// EXTERNAL ID EXISTS, START SIMILARITY REPORT GENERATION PROCESS (STAGE 2)
			} else {
				log.info("ID AWAITING REPORT " + item.getExternalId());
				try {
					// Get submission status, returns the state of the submission as string
					String submissionStatus = getSubmissionStatus(item.getExternalId());
					// Handle submission status
					if("COMPLETE".equals(submissionStatus)) {
						// If submission status is complete, start similarity report process
						generateSimilarityReport(item.getExternalId());
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
					}else if("PROCESSING".equals(submissionStatus)) {
						//do nothing... try again
						continue;
					}else if("CREATED".equals(submissionStatus)) {
						//do nothing... try again
						//TODO does this need to be handled differently?
						continue;
					}else if("ERROR".equals(submissionStatus)) {						
						throw new Error("Submission returned with ERROR status");						
					}else {
						item.setLastError("SubmissionStatus " + submissionStatus);
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
						crqs.update(item);
						errors++;
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					item.setLastError(e.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					crqs.update(item);
					errors++;
				}	
			}
		}
		
		// LOOP 2
		// UPLOADED CONTENTS, AWAITING SIMILAIRTY REPORT (STAGE 3)
		for(ContentReviewItem item : crqs.getAwaitingReports(getProviderId())) {
			// Make sure it's after the next retry time
			if (item.getNextRetryTime().getTime() > new Date().getTime()) {
				continue;
			}			
			try {
				// Get status of similarity report
				// Returns -1 if report is still processing
				// Returns -2 if an error occurs
				// Else returns reports score as integer
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
					// Similarity report is still generating, will try again
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
	
	public int getDelayTime(long retries) {
		// exponential retry algorithm that caps the retries off at 36 hours (checking once every 4 hours max)
		int minutes = (int) Math.pow(2, retries < TURNITIN_MAX_RETRY ? retries : 1); //built in check for max retries to fail quicker
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
		
		//Set variables
		URL url = null;
		HttpURLConnection connection = null;
		DataOutputStream wr = null;
		
		//Construct URL
		url = new URL(getNormalizedServiceUrl() + "submissions/" + reportId + "/original/");

		//Open connection and set HTTP method
		connection=(HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		
		// Set headers 
		for(Entry<String, String> entry : CONTENT_UPLOAD_HEADERS.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}

		//Set Post body:
		wr = new DataOutputStream(connection.getOutputStream());
		wr.write(data);
		wr.close();		
		
		//Send request:
		int responseCode = connection.getResponseCode();
		
		if (responseCode < 200 || responseCode >= 300) {
			throw new Error(responseCode + ": " + connection.getResponseMessage());
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
