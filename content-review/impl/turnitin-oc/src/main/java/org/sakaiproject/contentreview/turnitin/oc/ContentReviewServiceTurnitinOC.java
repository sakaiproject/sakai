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
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.ContentReviewProviderException;
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
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

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
	
	@Setter
	private MemoryService memoryService;
	
	@Setter
	private AuthzGroupService authzGroupService;

	private static final String SERVICE_NAME = "Turnitin";
	private static final String TURNITIN_OC_API_VERSION = "v1";
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
	private static final String VIEWER_USER_ID = "viewer_user_id";
	private static final String AUTHOR_METADATA_OVERRIDE = "author_metadata_override";
	private static final String MATCH_OVERVIEW = "match_overview";
	private static final String ALL_SOURCES = "all_sources";
	private static final String MODES = "modes";
	private static final String SIMILARITY = "similarity";
	private static final String SAVE_CHANGES = "save_changes";
	private static final String VIEW_SETTINGS = "view_settings";
	private static final String VIEWER_PERMISSIONS = "viewer_permissions";
	private static final String VIEWER_PERMISSION_MAY_VIEW_SUBMISSIONS_FULL_SOURCE = "may_view_submission_full_source";
	private static final String VIEWER_PERMISSION_MAY_VIEW_MATCH_SUBMISSION_INFO = "may_view_match_submission_info";
	private static final String VIEWER_DEFAULT_PERMISSIONS = "viewer_default_permission_set";
	private enum ROLES{
		INSTRUCTOR(Arrays.asList("Faculty", "Instructor", "Mentor", "Staff", "maintain", "Teaching Assistant")),
		LEARNER(Arrays.asList("Learner", "Student", "access")),
		EDITOR(Arrays.asList()),
		USER(Arrays.asList("Alumni", "guest", "Member", "Observer", "Other")),
		APPLICANT(Arrays.asList("ProspectiveStudent")),
		ADMINISTRATOR(Arrays.asList("Administrator", "Admin")),
		UNDEFINED(Arrays.asList());
		
		List<String> mappedRoles;
		private ROLES(List<String> mappedRoles) {
			this.mappedRoles = mappedRoles;
		}
		private void setMappedRoles(List<String> mappedRoles) {
			this.mappedRoles = mappedRoles;
		}
		private List<String> getMappedRoles() {
			return mappedRoles;
		}
	};
	
	private static final String GENERATE_REPORTS_IMMEDIATELY_AND_ON_DUE_DATE= "1";
	private static final String GENERATE_REPORTS_ON_DUE_DATE = "2";	
	private static final String PLACEHOLDER_STRING_FLAG = "_placeholder";
	private static final Integer PLACEHOLDER_ITEM_REVIEW_SCORE = -10;

	private static final String COMPLETE_STATUS = "COMPLETE";
	private static final String CREATED_STATUS = "CREATED";
	private static final String PROCESSING_STATUS = "PROCESSING";

	private static final String SUBMISSION_COMPLETE_EVENT_TYPE = "SUBMISSION_COMPLETE";
	private static final String SIMILARITY_COMPLETE_EVENT_TYPE = "SIMILARITY_COMPLETE";
	private static final String SIMILARITY_UPDATED_EVENT_TYPE = "SIMILARITY_UPDATED";

	private String serviceUrl;
	private String apiKey;
	private String sakaiVersion;
	private int maxRetryMinutes;
	private int maxRetry;
	private boolean skipDelays;

	private HashMap<String, String> BASE_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SUBMISSION_REQUEST_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> SIMILARITY_REPORT_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> CONTENT_UPLOAD_HEADERS = new HashMap<String, String>();
	private HashMap<String, String> WEBHOOK_SETUP_HEADERS = new HashMap<String, String>();
	
	private enum AUTO_EXCLUDE_SELF_MATCHING_SCOPE{
		ALL,
		NONE,
		GROUP,
		GROUP_CONTEXT
	}
	
	//Caches requests for instructors so that we don't have to send a request for every student
	private Cache EULA_CACHE;
	private static final String EULA_LATEST_KEY = "latest";
	private static final String EULA_DEFAULT_LOCALE = "en-US";
	
	
	// Define Turnitin's acceptable file extensions and MIME types, order of these arrays DOES matter
	private final String[] DEFAULT_ACCEPTABLE_FILE_EXTENSIONS = new String[] {
			".pdf",
			".doc",
			".ppt",
			".pps",
			".xls",
			".doc",
			".docx",
			".ppt",
			".pptx",
			".ppsx",
			".pps",
			".pptx",
			".ppsx",
			".xlsx",
			".xls",
			".ps",
			".rtf",
			".doc",
			".rtf",
			".doc",
			".htm",
			".html",
			".wpd",
			".odt",
			".txt"
	};
	private final String[] DEFAULT_ACCEPTABLE_MIME_TYPES = new String[] {
			"application/pdf",
			"application/msword",
			"application/vnd.ms-powerpoint",
			"application/vnd.ms-powerpoint",
			"application/vnd.ms-excel",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
			"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
			"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"application/postscript",
			"text/rtf",
			"text/rtf",
			"application/rtf",
			"application/rtf",
			"text/html",
			"text/html",
			"application/wordperfect",
			"application/vnd.oasis.opendocument.text",
			"text/plain"
	};

	// Sakai.properties overriding the arrays above
	private final String PROP_ACCEPT_ALL_FILES = "turnitin.oc.accept.all.files";

	private final String PROP_ACCEPTABLE_FILE_EXTENSIONS = "turnitin.oc.acceptable.file.extensions";
	private final String PROP_ACCEPTABLE_MIME_TYPES = "turnitin.oc.acceptable.mime.types";

	// A list of the displayable file types (ie. "Microsoft Word", "WordPerfect document", "Postscript", etc.)
	private final String PROP_ACCEPTABLE_FILE_TYPES = "turnitin.oc.acceptable.file.types";

	private final String KEY_FILE_TYPE_PREFIX = "file.type";
	
	private String autoExcludeSelfMatchingScope;
	private Boolean mayViewSubmissionFullSourceOverrideStudent = null;
	private Boolean mayViewMatchSubmissionInfoOverrideStudent = null;
	private Boolean mayViewSubmissionFullSourceOverrideInstructor = null;
	private Boolean mayViewMatchSubmissionInfoOverrideInstructor = null;

	public void init() {
		EULA_CACHE = memoryService.createCache("org.sakaiproject.contentreview.turnitin.oc.ContentReviewServiceTurnitinOC.LATEST_EULA_CACHE", new SimpleConfiguration<>(10000, 24 * 60 * 60, -1));
		// Retrieve Service URL and API key
		serviceUrl = serverConfigurationService.getString("turnitin.oc.serviceUrl", "");
		apiKey = serverConfigurationService.getString("turnitin.oc.apiKey", "");
		// Retrieve Sakai Version if null set default 		
		sakaiVersion = serverConfigurationService.getString("version.sakai", "UNKNOWN");

		// Maximum delay between retries after recoverable errors
		maxRetryMinutes = serverConfigurationService.getInt("turnitin.oc.max.retry.minutes", 240); // 4 hours
		// Maximum number of retries for recoverable errors
		maxRetry = serverConfigurationService.getInt("turnitin.oc.max.retry", 16);
		// For local development only; do not set this in production:
		skipDelays = serverConfigurationService.getBoolean("turnitin.oc.skip.delays", false);

		autoExcludeSelfMatchingScope =Arrays.stream(AUTO_EXCLUDE_SELF_MATCHING_SCOPE.values())
				.filter(e -> e.name().equalsIgnoreCase(serverConfigurationService.getString("turnitin.oc.auto_exclude_self_matching_scope")))
				.findAny().orElse(AUTO_EXCLUDE_SELF_MATCHING_SCOPE.GROUP).name();
		log.info("Exclude Scope: " + autoExcludeSelfMatchingScope);
		
		// Find any permission overrides, if not set, set value to null to skip overrides
		mayViewSubmissionFullSourceOverrideStudent = StringUtils.isNotEmpty(serverConfigurationService.getString("turnitin.oc.may_view_submission_full_source.student")) 
				? serverConfigurationService.getBoolean("turnitin.oc.may_view_submission_full_source.student", false)
				: null;
		mayViewMatchSubmissionInfoOverrideStudent = StringUtils.isNotEmpty(serverConfigurationService.getString("turnitin.oc.may_view_match_submission_info.student")) 
				? serverConfigurationService.getBoolean("turnitin.oc.may_view_match_submission_info.student", false)
				: null;
		mayViewSubmissionFullSourceOverrideInstructor = StringUtils.isNotEmpty(serverConfigurationService.getString("turnitin.oc.may_view_submission_full_source.instructor")) 
				? serverConfigurationService.getBoolean("turnitin.oc.may_view_submission_full_source.instructor", false)
				: null;
		mayViewMatchSubmissionInfoOverrideInstructor = StringUtils.isNotEmpty(serverConfigurationService.getString("turnitin.oc.may_view_match_submission_info.instructor")) 
				? serverConfigurationService.getBoolean("turnitin.oc.may_view_match_submission_info.instructor", false)
				: null;
				
		// Override default Sakai->Turnitin roles mapping
		for(ROLES role : ROLES.values()) {
			role.setMappedRoles(serverConfigurationService.getStringList("turnitin.oc.roles." + role.name().toLowerCase() + ".mapping", role.getMappedRoles()));
		}

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

		if(StringUtils.isNotEmpty(apiKey) && StringUtils.isNotEmpty(serviceUrl)) {
			try {
				// Get the webhook url
				String webhookUrl = getWebhookUrl(Optional.empty());
				boolean webhooksSetup = false;
				// Check to see if any webhooks have already been set up for this url
				for (Webhook webhook : getWebhooks()) {
					log.info("Found webhook: " + webhook.getUrl());
					if (StringUtils.isNotEmpty(webhook.getUrl()) && webhook.getUrl().equals(webhookUrl)) {
						webhooksSetup = true;
						break;
					}
				}

				if (!webhooksSetup) {
					// No webhook set up for this url, set one up
					log.info("No matching webhook for " + webhookUrl);
					String id = setupWebhook(webhookUrl);
					if(StringUtils.isNotEmpty(id)) {
						log.info("successfully created webhook: " + id);
					}
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
		}
	}

	public String setupWebhook(String webhookUrl) throws Exception {
		String id = null;
		Map<String, Object> data = new HashMap<String, Object>();
		List<String> types = new ArrayList<>();
		types.add(SIMILARITY_UPDATED_EVENT_TYPE);
		types.add(SIMILARITY_COMPLETE_EVENT_TYPE);
		types.add(SUBMISSION_COMPLETE_EVENT_TYPE);

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
		String error = null;
		if ((responseCode >= 200) && (responseCode < 300) && (responseBody != null)) {
			// create JSONObject from responseBody
			JSONObject responseJSON = JSONObject.fromObject(responseBody);
			if (responseJSON.has("id")) {
				id = responseJSON.getString("id");
			} else {
				error = "returned with no ID: " + responseJSON;
			}
		}else {
			error = responseMessage;
		}
		
		if(StringUtils.isEmpty(id)) {
			log.info("Error setting up webhook: " + error);
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

		if(StringUtils.isNotEmpty(responseBody) 
				&& responseCode >= 200 
				&& responseCode < 300
				&& !"[]".equals(responseBody)) {
			// Loop through response via JSON, convert objects to Webhooks
			JSONArray webhookList = JSONArray.fromObject(responseBody);
			for (int i=0; i < webhookList.size(); i++) {
				JSONObject webhookJSON = webhookList.getJSONObject(i);
				if (webhookJSON.has("id") && webhookJSON.has("url")) {
					webhooks.add(new Webhook(webhookJSON.getString("id"), webhookJSON.getString("url")));
				}
			}
		}else {
			log.info("getWebhooks: " + responseMessage);
		}
		
		return webhooks;
	}

	public boolean allowResubmission() {
		return true;
	}

	@Override
	public void checkForReports() {
		// Auto-generated method stub
	}

	@Override
	public void syncRosters() {
		// Auto-generated method stub
	}

	@Override
	public void createAssignment(final String contextId, final String assignmentRef, final Map opts)
			throws SubmissionException, TransientSubmissionException {
		// Auto-generated method stub
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
		return null;
	}

	public String getLocalizedStatusMessage(String arg0, String arg1) {
		return null;
	}

	public String getLocalizedStatusMessage(String arg0, Locale arg1) {
		return null;
	}

	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		return null;
	}

	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return null;
	}
	
	@Override
	public String getReviewReportRedirectUrl(String contentId, String assignmentRef, String userId, String contextId, boolean isInstructor) {
		
		// Set variables
		String viewerUrl = null;
		Optional<ContentReviewItem> optionalItem = crqs.getQueuedItem(getProviderId(), contentId);
		ContentReviewItem item = optionalItem.isPresent() ? optionalItem.get() : null;
		if(item != null && ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE.equals(item.getStatus())) {
			try {
				//Get report owner user information
				String givenName = "";
				String familyName = "";
				try{
					User user = userDirectoryService.getUser(item.getUserId());
					givenName = user.getFirstName();
					familyName = user.getLastName();
				}catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				Map<String, Object> data = new HashMap<String, Object>();
				// Set user name
				Map<String, Object> authorMetaDataOverride = new HashMap<String, Object>();				
				authorMetaDataOverride.put(GIVEN_NAME, givenName);
				authorMetaDataOverride.put(FAMILY_NAME, familyName);
				data.put(AUTHOR_METADATA_OVERRIDE, authorMetaDataOverride);
				data.put(VIEWER_USER_ID, userId);
				Map<String, Object> similarity = new HashMap<String, Object>();
				Map<String, Object> modes = new HashMap<String, Object>();
				modes.put(MATCH_OVERVIEW, Boolean.TRUE);
				modes.put(ALL_SOURCES, Boolean.TRUE);
				similarity.put(MODES, modes);
				Map<String, Object> viewSettings = new HashMap<>();
				viewSettings.put(SAVE_CHANGES, Boolean.TRUE);
				similarity.put(VIEW_SETTINGS, viewSettings);
				data.put(SIMILARITY, similarity);
				data.put(VIEWER_DEFAULT_PERMISSIONS, mapUserRole(userId, contextId, isInstructor));
				//Check if there are any sakai.properties overrides for the default permissions
				Map<String, Object> viewerPermissionsOverride = new HashMap<String, Object>();
				if(!isInstructor && mayViewSubmissionFullSourceOverrideStudent != null) {
					viewerPermissionsOverride.put(VIEWER_PERMISSION_MAY_VIEW_SUBMISSIONS_FULL_SOURCE, mayViewSubmissionFullSourceOverrideStudent);
				}
				if(!isInstructor && mayViewMatchSubmissionInfoOverrideStudent != null) {
					viewerPermissionsOverride.put(VIEWER_PERMISSION_MAY_VIEW_MATCH_SUBMISSION_INFO, mayViewMatchSubmissionInfoOverrideStudent);
				}
				if(isInstructor && mayViewSubmissionFullSourceOverrideInstructor != null) {
					viewerPermissionsOverride.put(VIEWER_PERMISSION_MAY_VIEW_SUBMISSIONS_FULL_SOURCE, mayViewSubmissionFullSourceOverrideInstructor);
				}
				if(isInstructor && mayViewMatchSubmissionInfoOverrideInstructor != null) {
					viewerPermissionsOverride.put(VIEWER_PERMISSION_MAY_VIEW_MATCH_SUBMISSION_INFO, mayViewMatchSubmissionInfoOverrideInstructor);
				}
				if(viewerPermissionsOverride.size() > 0) {
					data.put(VIEWER_PERMISSIONS, viewerPermissionsOverride);
				}

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
					} else {
						log.error("Viewer URL not found. Response: " + responseMessage);
					}
				} else {
					log.error(responseMessage);
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
	
	@Override
	public Integer getProviderId() {
		//Since there is an already existing Turnitin integration, we can't use the same "namespace" for the provider ID
		return Math.abs("TurnitinOC".hashCode());
	}

	public boolean isAcceptableContent(ContentResource resource) {
		if (serverConfigurationService.getBoolean(PROP_ACCEPT_ALL_FILES, false)) {
			return true;
		}

		String mime = resource.getContentType();

		// Check the mime type
		Map<String, SortedSet<String>> acceptableExtensionsToMimeTypes = getAcceptableExtensionsToMimeTypes();
		if (acceptableExtensionsToMimeTypes.values().stream().anyMatch(set -> set.contains(mime))) {
			return true;
		}

		// Check the file extension
		ResourceProperties resourceProperties = resource.getProperties();
		String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
		if (fileName.indexOf(".") > 0) {
			String extension = fileName.substring(fileName.lastIndexOf("."));
			if (acceptableExtensionsToMimeTypes.containsKey(extension)) {
				return true;
			}
		}

		return false;
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
		throws Exception {
		// Set variables
		HttpURLConnection connection = null;
		URL url = null;

		// Construct URL
		url = new URL(urlStr);

		// Open connection and set HTTP method
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);

		// Set headers
		if (headers == null) {
			throw new Exception("No headers present for call: " + method + ":" + urlStr);
		}
		for (Entry<String, String> entry : headers.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}

		if (data != null || dataBytes != null) {
			connection.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
				// Set Post body:
				if (data != null) {
					// Convert data to string:
					try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(wr, StandardCharsets.UTF_8))) {
						ObjectMapper objectMapper = new ObjectMapper();
						String dataStr = objectMapper.writeValueAsString(data);
						br.write(dataStr);
						br.flush();
					}
				} else if (dataBytes != null) {
					wr.write(dataBytes);
				}
			}
		}

		// Send request:
		int responseCode = connection.getResponseCode();
		String responseMessage = connection.getResponseMessage();
		String responseBody;
		if (responseCode < 200 || responseCode >= 300)
		{
			InputStream inputStream = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
			// getInputStream() throws an exception in this case, but getErrorStream() has the information necessary for troubleshooting
			responseBody = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
			log.warn("Turnitin response code: " + responseCode + "; message: " + responseMessage + "; body:\n" + responseBody);
		}
		else
		{
			responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
			log.debug("Turnitin response code: " + responseCode + "; message: " + responseMessage + "; body:\n" + responseBody);
		}
		
		HashMap<String, Object> response = new HashMap<String, Object>();
		response.put(RESPONSE_CODE, responseCode);
		response.put(RESPONSE_MESSAGE, responseMessage);
		response.put(RESPONSE_BODY, responseBody);
		
		return response;
	}

	private void generateSimilarityReport(String reportId, String assignmentRef) throws Exception {
		
		Assignment assignment = assignmentService.getAssignment(entityManager.newReference(assignmentRef));
		Map<String, String> assignmentSettings = assignment.getProperties();
		
		List<String> repositories = Arrays.asList("INTERNET", "SUBMITTED_WORK");
		// Build header maps
		Map<String, Object> reportData = new HashMap<String, Object>();
		Map<String, Object> generationSearchSettings = new HashMap<String, Object>();
		generationSearchSettings.put("search_repositories", repositories);
		generationSearchSettings.put("auto_exclude_self_matching_scope", autoExcludeSelfMatchingScope);
		reportData.put("generation_settings", generationSearchSettings);

		Map<String, Object> viewSettings = new HashMap<String, Object>();
		viewSettings.put("exclude_quotes", "true".equals(assignmentSettings.get("exclude_quoted")));
		viewSettings.put("exclude_bibliography", "true".equals(assignmentSettings.get("exclude_biblio")));
		reportData.put("view_settings", viewSettings);
		
		Map<String, Object> indexingSettings = new HashMap<String, Object>();
		indexingSettings.put("add_to_index", "true".equals(assignmentSettings.get("store_inst_index")));
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
			throw new ContentReviewProviderException("Submission failed to initiate: " + responseCode + ", " + responseMessage + ", " + responseBody,
				createLastError(doc -> createFormattedMessageXML(doc, "report.error.unsuccessful", responseMessage, responseCode)));
		}
	}

	private JSONObject getSubmissionJSON(String reportId) throws Exception {
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
		if ((responseCode < 200) || (responseCode >= 300)) {
			throw new ContentReviewProviderException("getSubmissionJSON invalid request: " + responseCode + ", " + responseMessage + ", " + responseBody,
				createLastError(doc -> createFormattedMessageXML(doc, "submission.error.unexpected.response", responseMessage, responseCode)));
		}

		return responseJSON;
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

	private String getSubmissionId(ContentReviewItem item, String fileName, Site site, Assignment assignment) {
		String userID = item.getUserId();
		List<User> submissionOwners = new ArrayList<>();
		String submitterID = null;

		try {
			AssignmentSubmission currentSubmission = assignmentService.getSubmission(assignment.getId(), userID);

			Set<String> ownerIds = currentSubmission.getSubmitters().stream()
				.map(AssignmentSubmissionSubmitter::getSubmitter)
				.collect(Collectors.toSet());

			//find submitter by filtering submittee=true, if not found, then use the assignment property SUBMITTER_USER_ID
			submitterID = currentSubmission.getSubmitters().stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findAny()
					.map(AssignmentSubmissionSubmitter::getSubmitter)
					.orElseGet(() -> currentSubmission.getProperties().get(AssignmentConstants.SUBMITTER_USER_ID));

			if (userID.equals(submitterID) || StringUtils.isBlank(submitterID)) {
				//no need to keep track of the submitterID if it is the same as the ownerID
				submitterID = null;
			} else {
				ownerIds.add(submitterID);
			}

			submissionOwners.addAll(userDirectoryService.getUsers(ownerIds));
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		String submissionId = null;
		try {

			// Build header maps
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("owner", userID);
			if(site != null) {
				data.put("owner_default_permission_set", mapUserRole(userID, site.getId(), false));
			}
			if (StringUtils.isNotBlank(submitterID)) {
				data.put("submitter", submitterID);
				if(site != null) {
					data.put("submitter_default_permission_set", mapUserRole(submitterID, site.getId(), false));
				}
			}
			data.put("title", fileName);
			String eulaUserId = StringUtils.isNotEmpty(submitterID) ? submitterID : userID;
			Instant eulaTimestamp = getUserEULATimestamp(eulaUserId);
			String eulaVersion = getUserEULAVersion(eulaUserId);
			if(eulaTimestamp == null || StringUtils.isEmpty(eulaVersion)) {
				//best effort to make sure the user has a EULA acceptance timestamp, but if not
				//add a warning in the logs and continue so that the report will still generate
				eulaTimestamp = Instant.now();
				eulaVersion = getEndUserLicenseAgreementVersion();
				log.warn("EULA not found for user: " + eulaUserId + ", contentId: " + item.getId());
			}
			Map<String, Object> eula = new HashMap<>();
			eula.put("accepted_timestamp", eulaTimestamp.toString());
			eula.put("language", getUserEulaLocale(eulaUserId));
			eula.put("version", eulaVersion);
			data.put("eula", eula);
			Map<String, Object> metadata = new HashMap<>();
			if(assignment != null) {				
				Map<String, Object> group = new HashMap<>();
				group.put("id", assignment.getId());
				group.put("name", assignment.getTitle());
				group.put("type", "ASSIGNMENT");
				metadata.put("group", group);
				if(site != null) {
					Map<String, Object> groupContext = new HashMap<>();
					groupContext.put("id", site.getId());
					groupContext.put("name", site.getTitle());
					metadata.put("group_context", groupContext);
				}
			}
			//set submission owner metadata
			if (submissionOwners.size() > 0) {
				List<Map<String, Object>> submissionOwnersMetedata = new ArrayList<>();
				for(User owner : submissionOwners) {
					Map<String, Object> ownerMetadata = new HashMap<>();
					ownerMetadata.put("id", owner.getId());
					if(StringUtils.isNotEmpty(owner.getFirstName())) {
						ownerMetadata.put("given_name", owner.getFirstName());	
					}
					if(StringUtils.isNotEmpty(owner.getLastName())) {
						ownerMetadata.put("family_name", owner.getLastName());	
					}
					if(StringUtils.isNotEmpty(owner.getEmail())) {
						ownerMetadata.put("email", owner.getEmail());	
					}
					if(ownerMetadata.size() > 1) {
						submissionOwnersMetedata.add(ownerMetadata);
					}
				}
				if(submissionOwnersMetedata.size() > 0) {
					metadata.put("owners", submissionOwnersMetedata);
				}
			}
			if(metadata.size() > 0) {
				data.put("metadata", metadata);
			}
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
				String status = responseJSON.containsKey("status") ? responseJSON.getString("status") : null;
				if (STATUS_CREATED.equals(status) && responseJSON.containsKey("id")) {
					submissionId = responseJSON.getString("id");
				} else {
					log.error("getSubmissionId response: " + responseMessage);
					setLastError(item, doc-> {
						Object cause;
						if (!STATUS_CREATED.equals(status)) {
							cause = createFormattedMessageXML(doc, "submission.error.unexpected.response.wrong.status", status);
						}
						else {
							cause = createFormattedMessageXML(doc, "submission.error.unexpected.response.no.id");
						}
						return createFormattedMessageXML(doc, "submission.error.unexpected.response", cause, responseCode);
					});
				}
			} else {
				log.error("getSubmissionId response code: " + responseCode + ", " + responseMessage + ", "
						+ responseJSON);
				setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.unsuccessful.response", responseMessage, responseCode));
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.ioexception"));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.general"));
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
					if(assignment != null && assignment.getDueDate() != null ) {
						Date assignmentDueDate = Date.from(assignment.getDueDate());

						// Make sure due date is past						
						if (assignmentDueDate.before(new Date())) {
							//Lookup reference item
							String referenceItemContentId = item.getContentId().substring(0, item.getContentId().indexOf(PLACEHOLDER_STRING_FLAG));							
							Optional<ContentReviewItem> quededReferenceItem = crqs.getQueuedItem(item.getProviderId(), referenceItemContentId);
							ContentReviewItem referenceItem = quededReferenceItem.isPresent() ? quededReferenceItem.get() : null;							
							if (referenceItem != null && checkForContentItemInSubmission(referenceItem, assignment)) {
								// Regenerate similarity request for reference id
								// Report is recalled after due date
								generateSimilarityReport(referenceItem.getExternalId(), referenceItem.getTaskId());
								//reschedule reference item by setting score to null, reset retry time and set status to awaiting report
								referenceItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
								referenceItem.setRetryCount(Long.valueOf(0));
								referenceItem.setReviewScore(null);
								referenceItem.setNextRetryTime(new Date());
								crqs.update(referenceItem);
								// Report regenerated for reference item, placeholder item is no longer needed
								crqs.removeFromQueue(getProviderId(), item.getContentId());
								success++;
								continue;
							}
							else {
								// Reference item no longer exists
								// Placeholder item is no longer needed
								crqs.removeFromQueue(getProviderId(), item.getContentId());
								errors++;
								continue;
							}
						} else {
							// We don't want placeholder items to exceed retry count maximum
							// Reset retry count to zero
							item.setRetryCount(Long.valueOf(0));
							item.setNextRetryTime(getDueDateRetryTime(assignmentDueDate));
							crqs.removeFromQueue(getProviderId(), item.getContentId());
							continue;
						}
					}else {
						// Assignment or due date no longer exist
						// placeholder item is no longer needed
						crqs.removeFromQueue(getProviderId(), item.getContentId());
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

			} catch (IOException e) {
				log.error(e.getLocalizedMessage(), e);
				setLastError(item, doc->createFormattedMessageXML(doc, "report.error.ioexception"));
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE);
				crqs.update(item);
				errors++;
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
				setLastError(item, e);
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
				String reportGenSpeed = null;
				if(assignment != null) {
					Date assignmentDueDate = Date.from(assignment. getDueDate());					
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

				// EXTERNAL ID DOES NOT EXIST, CREATE SUBMISSION AND UPLOAD CONTENTS TO TCA
				// (STAGE 1)
				if (StringUtils.isEmpty(item.getExternalId())) {
					//Paper is ready to be submitted
					ContentResource resource = null;
					try {
						// Get resource with current item's content Id
						resource = contentHostingService.getResource(item.getContentId());
					} catch (IdUnusedException e4) {
						log.error("IdUnusedException: no resource with id " + item.getContentId(), e4);
						setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.idunusedexception"));
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
						crqs.update(item);
						errors++;
						continue;
					} catch (PermissionException e) {
						log.error("PermissionException: no resource with id " + item.getContentId(), e);
						setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.permissionexception"));
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
						crqs.update(item);
						errors++;
						continue;
					} catch (TypeException e) {
						log.error("TypeException: no resource with id " + item.getContentId(), e);
						setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.idunusedexception"));
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
						crqs.update(item);
						errors++;
						continue;
					}
					
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
					boolean updateLastError = true;
					try {
						log.info("Submission starting...");
						// Retrieve submissionId from TCA and set to externalId
						//get site title
						Site site = null;
						try{
							site = siteService.getSite(item.getSiteId());
						}catch(Exception e){
							//no worries, just log it
							log.error("Site not found for item: " + item.getId() + ", site: " + item.getSiteId(), e);
						}
						String externalId = getSubmissionId(item, fileName, site, assignment);
						if (StringUtils.isEmpty(externalId)) {
							// getSubmissionId sets the item's lastError accurately in accordance with the Turnitin response
							updateLastError = false;
							throw new Exception("Failed to obtain a submission ID from Turnitin");
						}
						else {
							// Add filename to content upload headers
							CONTENT_UPLOAD_HEADERS.put(HEADER_DISP, "inline; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
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
						if (updateLastError) {
							setLastError(item, e);
						}
						item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
						crqs.update(item);
						errors++;
					}
				} else {
					// EXTERNAL ID EXISTS, START SIMILARITY REPORT GENERATION PROCESS (STAGE 2)					
					try {
						// Get submission status, returns the state of the submission as string		
						JSONObject submissionJSON = getSubmissionJSON(item.getExternalId());
						if (!submissionJSON.containsKey("status")) {
							throw new ContentReviewProviderException("Response from Turnitin is missing expected data", createLastError(doc->createFormattedMessageXML(doc, "submission.error.missing.data")));
						}
						String submissionStatus = submissionJSON.getString("status");

						if (COMPLETE_STATUS.equals(submissionStatus)) {
							success++;
						} else if (CREATED_STATUS.equals(submissionStatus) || PROCESSING_STATUS.equals(submissionStatus)) {
							// do nothing item is still being processes
						} else {
							// returned with an error status
							errors++;
						}

						handleSubmissionStatus(submissionJSON, item, assignment);

					} catch (Exception e) {
						log.error(e.getMessage(), e);
						setLastError(item, e);
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

	private boolean checkForContentItemInSubmission(ContentReviewItem item, Assignment assignment) {
		try {
			AssignmentSubmission currentSubmission = assignmentService.getSubmission(assignment.getId(), item.getUserId());
			String referenceItemContentId = item.getContentId();
			if(referenceItemContentId.endsWith(PLACEHOLDER_STRING_FLAG)) {
				referenceItemContentId = referenceItemContentId.substring(0, referenceItemContentId.indexOf(PLACEHOLDER_STRING_FLAG));
			}
			return currentSubmission.getAttachments().contains(contentHostingService.getResource(referenceItemContentId).getReference());
		}catch(Exception e) {
			log.error(e.getMessage(), e);
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

	private void handleSubmissionStatus(JSONObject submissionJSON, ContentReviewItem item, Assignment assignment) {
		try {

			Date assignmentDueDate = Date.from(assignment.getDueDate());
			String reportGenSpeed = assignment.getProperties().get("report_gen_speed");

			String submissionStatus = submissionJSON.getString("status");

			switch (submissionStatus) {
			case "COMPLETE":
				// If submission status is complete, start similarity report process
				generateSimilarityReport(item.getExternalId(), item.getTaskId());
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
				// Check for items that generate reports both immediately and on due date
				// Create a placeholder item that will regenerate and index report after due date
				if (assignmentDueDate != null && assignmentDueDate.after(new Date()) && GENERATE_REPORTS_IMMEDIATELY_AND_ON_DUE_DATE.equals(reportGenSpeed)) {
					createPlaceholderItem(item, assignmentDueDate);
				}
				break;
			case "PROCESSING":
				// do nothing... try again
				break;
			case "CREATED":
				// do nothing... try again
				break;
			default:
				String errorCode = submissionJSON.containsKey("error_code") ? submissionJSON.getString("error_code") : submissionStatus;
				// Grab an appropriate message from turnitin.properties by the prefix "submission.terminal.status."
				// If a message with this key exists, it's considered a terminal error and the item should not be retried
				String terminalKey = "submission.terminal.status." + errorCode;
				ResourceLoader rb = getResourceLoader();
				if (rb.containsKey(terminalKey)) {
					setLastError(item, doc->createFormattedMessageXML(doc, terminalKey));
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				} else {
					log.info("Unknown submission status, will retry: " + submissionStatus);
					String recoverableKey = "submission.recoverable.status." + errorCode;
					if (rb.containsKey(recoverableKey)) {
						// Currently these don't exist, but this implementation is ready should recoverable errors become identified
						setLastError(item, doc->createFormattedMessageXML(doc, recoverableKey));
					} else {
						// Unknown submission status / error code. Assume it's recoverable; if it's terminal, the retry count will be exceeded eventually
						setLastError(item, doc->createFormattedMessageXML(doc, "submission.error.unhandled.status", errorCode));
					}
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				}
				crqs.update(item);
				break;
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
			throw new ContentReviewProviderException("Unknown error during report status call",
				createLastError(doc -> createFormattedMessageXML(doc, "report.error.unknown")));
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
		} else if (item.getRetryCount().intValue() > maxRetry) {
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
		if (skipDelays)
		{
			return 0;
		}
		// exponential retry algorithm that caps the retries off at 36 hours (checking once every 4 hours max)
		int minutes = (int) Math.pow(2, retries < maxRetry ? retries : 1); // built in check for max retries
																						// to fail quicker
		return minutes > maxRetryMinutes ? maxRetryMinutes : minutes;
	}

	public void queueContent(String userId, String siteId, String assignmentReference, List<ContentResource> content)
			throws QueueException {
		crqs.queueContent(getProviderId(), userId, siteId, assignmentReference, content);
	}

	public void removeFromQueue(String contentId) {
		crqs.removeFromQueue(getProviderId(), contentId);
	}

	@Override
	public void resetUserDetailsLockedItems(String userId) {
		//Auto-generated method stub
	}

	public String getReviewError(String contentId) {
		return null;
	}

	public boolean allowAllContent() {
		// Turntin reports errors when content is submitted that it can't check originality against. So we will block unsupported content.
		return serverConfigurationService.getBoolean(PROP_ACCEPT_ALL_FILES, false);
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		Map<String, SortedSet<String>> acceptableExtensionsToMimeTypes = new HashMap<>();
		String[] acceptableFileExtensions = getAcceptableFileExtensions();
		String[] acceptableMimeTypes = getAcceptableMimeTypes();
		int min = Math.min(acceptableFileExtensions.length, acceptableMimeTypes.length);
		for (int i = 0; i < min; i++) {
			appendToMap(acceptableExtensionsToMimeTypes, acceptableFileExtensions[i], acceptableMimeTypes[i]);
		}

		return acceptableExtensionsToMimeTypes;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		Map<String, SortedSet<String>> acceptableFileTypesToExtensions = new LinkedHashMap<>();
		String[] acceptableFileTypes = getAcceptableFileTypes();
		String[] acceptableFileExtensions = getAcceptableFileExtensions();
		if (acceptableFileTypes != null && acceptableFileTypes.length > 0) {
			// The acceptable file types are listed in sakai.properties. Sakai.properties takes precedence.
			int min = Math.min(acceptableFileTypes.length, acceptableFileExtensions.length);
			for (int i = 0; i < min; i++) {
				appendToMap(acceptableFileTypesToExtensions, acceptableFileTypes[i], acceptableFileExtensions[i]);
			}
		}
		else {
			/*
			 * acceptableFileTypes not specified in sakai.properties (this is normal).
			 * Use ResourceLoader to resolve the file types.
			 * If the resource loader doesn't find the file extenions, log a warning and return the [missing key...] messages
			 */
			for( String fileExtension : acceptableFileExtensions ) {
				String key = KEY_FILE_TYPE_PREFIX + fileExtension;
				ResourceLoader rb = getResourceLoader();
				if (!rb.getIsValid(key)) {
					log.warn("While resolving acceptable file types for Turnitin, the sakai.property " + PROP_ACCEPTABLE_FILE_TYPES + " is not set, and the message bundle " + key + " could not be resolved. Displaying [missing key ...] to the user");
				}
				String fileType = rb.getString(key);
				appendToMap( acceptableFileTypesToExtensions, fileType, fileExtension );
			}
		}

		return acceptableFileTypesToExtensions;
	}
	
	public String[] getAcceptableFileExtensions() {
		String[] extensions = serverConfigurationService.getStrings(PROP_ACCEPTABLE_FILE_EXTENSIONS);
		if (extensions != null && extensions.length > 0) {
			return extensions;
		}
		return DEFAULT_ACCEPTABLE_FILE_EXTENSIONS;
	}
	
	public String[] getAcceptableMimeTypes() {
		String[] mimeTypes = serverConfigurationService.getStrings(PROP_ACCEPTABLE_MIME_TYPES);
		if (mimeTypes != null && mimeTypes.length > 0) {
			return mimeTypes;
		}
		return DEFAULT_ACCEPTABLE_MIME_TYPES;
	}
	
	public String [] getAcceptableFileTypes() {
		return serverConfigurationService.getStrings(PROP_ACCEPTABLE_FILE_TYPES);
	}
	
	/**
	 * Inserts (key, value) into a Map<String, Set<String>> such that value is inserted into the value Set associated with key.
	 * The value set is implemented as a TreeSet, so the Strings will be in alphabetical order
	 * Eg. if we insert (a, b) and (a, c) into map, then map.get(a) will return {b, c}
	 */
	private void appendToMap(Map<String, SortedSet<String>> map, String key, String value) {
		SortedSet<String> valueList = map.get(key);
		if (valueList == null) {
			valueList = new TreeSet<>();
			map.put(key, valueList);
		}
		valueList.add(value);
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
			throw new TransientSubmissionException(responseCode + ": " + responseMessage);
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
	public String getEndUserLicenseAgreementLink(String userId) {
		String url = null;
		Map<String, Object> latestEula = getLatestEula();
		if(latestEula != null && latestEula.containsKey("url")) {
			url = latestEula.get("url").toString() + "?lang=" + getUserEulaLocale(userId);
		}
		return url;
	}

	@Override
	public Instant getEndUserLicenseAgreementTimestamp() {
		Instant validFrom = null;
		Map<String, Object> latestEula = getLatestEula();
		if(latestEula != null && latestEula.containsKey("valid_from")) {
			try {
				validFrom = Instant.parse(latestEula.get("valid_from").toString());
			}catch(Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return validFrom;
	}

	@Override
	public String getEndUserLicenseAgreementVersion() {
		String version = null;
		Map<String, Object> latestEula = getLatestEula();
		if(latestEula != null && latestEula.containsKey("version")) {
			version = latestEula.get("version").toString();
		}
		return version;
	}
	
	private Map<String, Object> getLatestEula(){
		Map<String, Object> eula = null;
		if(EULA_CACHE.containsKey(EULA_LATEST_KEY)) {
			//EULA is still cached, grab it:
			Object cacheObj = EULA_CACHE.get(EULA_LATEST_KEY);
			if(cacheObj != null && cacheObj instanceof Map && ((Map<String, Object>) cacheObj).containsKey("url")){
				eula = ((Map<String, Object>) cacheObj);
			}
		}
		if(eula == null) {
			//get Eula from API and cache it:
			try {
				Map<String, Object> response = makeHttpCall("GET", getNormalizedServiceUrl() + "eula/" + EULA_LATEST_KEY, BASE_HEADERS, null, null);
				String responseBody = !response.containsKey(RESPONSE_BODY) ? "" : (String) response.get(RESPONSE_BODY);
				if(StringUtils.isNotEmpty(responseBody)) {
					eula = new ObjectMapper().readValue(responseBody, Map.class);
				}
				if(eula != null && eula.containsKey("url")) {
					//store in cache:
					EULA_CACHE.put(EULA_LATEST_KEY, eula);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}			
		}
		return eula;
	}
	
	private String getUserEulaLocale(String userId) {
		String userLocale = null;
		// Check user preference for locale			
		// If user has no preference set - get the system default
		Locale locale = Optional.ofNullable(preferencesService.getLocale(userId))
				.orElse(Locale.getDefault());
		if(locale != null && StringUtils.isNotEmpty(locale.getCountry())) {
			StringBuilder sb = new StringBuilder();
			sb.append(locale.getLanguage());
			if(StringUtils.isNotEmpty(locale.getCountry())) {
				sb.append("-" + locale.getCountry());
			}
			userLocale = sb.toString();
		}
		//find available EULA langauges:
		boolean found = false;
		if(StringUtils.isNotEmpty(userLocale)) {
			Map<String, Object> eula = getLatestEula();
			if(eula != null && eula.containsKey("available_languages") && eula.get("available_languages") instanceof List) {

				for(String eula_locale : (List<String>) eula.get("available_languages")) {
					if(userLocale.equalsIgnoreCase(eula_locale)) {
						//found exact match
						userLocale = eula_locale;
						found = true;
						break;
					}
				}
				if(!found
						&& StringUtils.isNotEmpty(locale.getLanguage())
						&& locale.getLanguage().length() >= 2) {
					//if we do not find the exact match, find a match based on the country code
					String userLanguage = locale.getLanguage().substring(0, 2);
					for(String eula_locale : (List<String>) eula.get("available_languages")) {
						if(eula_locale.toLowerCase().startsWith(userLanguage.toLowerCase())) {					
							//found language match
							userLocale = eula_locale;
							found = true;
							break;
						}
					}
				}
			}
		}
		if(!found) {
			//user's locale was null or their langauge was not found, set default to english:
			userLocale = EULA_DEFAULT_LOCALE;
		}

		return userLocale;
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
	public void webhookEvent(HttpServletRequest request, int providerId, Optional<String> customParam) {
		log.info("providerId: " + providerId + ", custom: " + (customParam.isPresent() ? customParam.get() : ""));
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
		log.debug("webhookEvent body: " + body);

		try {
			// Make sure cb is signed correctly
			String secrete_key_encoded = getSigningSignature(apiKey.getBytes(), body);
			if (StringUtils.isNotEmpty(secrete_key_encoded) && signature_header.equals(secrete_key_encoded)) {
				if (SUBMISSION_COMPLETE_EVENT_TYPE.equals(eventType)) {
					if (webhookJSON.has("id") && webhookJSON.has("status")) {
						// Allow cb to access assignment settings, needed for due date check
						SecurityAdvisor advisor = pushAdvisor();
						try {
							log.info("Submission complete webhook cb received");
							log.info(webhookJSON.toString());
							Optional<ContentReviewItem> optionalItem = crqs.getQueuedItemByExternalId(getProviderId(), webhookJSON.getString("id"));
							ContentReviewItem item = optionalItem.isPresent() ? optionalItem.get() : null;
							Assignment assignment = assignmentService.getAssignment(entityManager.newReference(item.getTaskId()));
							handleSubmissionStatus(webhookJSON, item, assignment);
							success++;
						} catch (Exception e) {
							log.error(e.getMessage(), e);
							errors++;
						} finally {
							// Remove advisor override
							popAdvisor(advisor);
						}
					} else {
						log.warn("Callback item received without needed information");
						errors++;
					}
				} else if (SIMILARITY_COMPLETE_EVENT_TYPE.equals(eventType) || SIMILARITY_UPDATED_EVENT_TYPE.equals(eventType)) {
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
	
	private String mapUserRole(String userId, String contextId, boolean isInstructor) {
		String role = isInstructor ? ROLES.INSTRUCTOR.name() : ROLES.UNDEFINED.name();
		try
		{
			//get the user's role for this course
			String siteRole = securityService.isSuperUser(userId) ? ROLES.ADMINISTRATOR.name() : authzGroupService.getUserRole(userId, siteService.siteReference(contextId));
			role = Arrays.asList(ROLES.values()).stream()
				.filter(r -> r.getMappedRoles().stream().anyMatch(siteRole::equalsIgnoreCase))
				.map(r -> r.name())
				.findFirst()
				.orElse(role);
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		return role;
	}
	
	
	@Getter
	@AllArgsConstructor
	private class Webhook {
		private String id;
		private String url;
	}

	@Override
	protected String getResourceLoaderName() {
		return "turnitin-oc";
	}
	
	@Override
	public boolean allowSubmissionsOnBehalf() {
		return true;
	}
}
