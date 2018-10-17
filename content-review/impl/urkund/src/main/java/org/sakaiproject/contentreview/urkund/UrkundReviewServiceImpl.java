/**********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.urkund;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.advisors.ContentReviewSiteAdvisor;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.service.BaseContentReviewService;
import org.sakaiproject.contentreview.service.ContentReviewQueueService;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class UrkundReviewServiceImpl extends BaseContentReviewService {
	private static final String STATE_SUBMITTED = "Submitted";
	private static final String STATE_ACCEPTED = "Accepted";
	private static final String STATE_REJECTED = "Rejected";
	private static final String STATE_ANALYZED = "Analyzed";
	private static final String STATE_ERROR = "Error";
	
	private static final String SERVICE_NAME = "Urkund";
	
	// Site property to enable or disable use of Urkund for the site
	private static final String URKUND_SITE_PROPERTY = "urkund";
	
	// 0 is unique user ID (must include friendly email address characters only)
	// 1 is unique site ID (must include friendly email address characters only)
	// 2 is integration context string (must be 2 to 10 characters)
	private static final String URKUND_SPOOFED_EMAIL_TEMPLATE = "%s_%s.%s@submitters.urkund.com";
	private String spoofEmailContext;
	
	// Define Urkund's acceptable file extensions and MIME types, order of these arrays DOES matter
	private final String[] DEFAULT_ACCEPTABLE_FILE_EXTENSIONS = new String[] {
			".doc", 
			".docx",
			".sxw",
			".ppt", 
			".pptx", 
			".pdf", 
			".txt", 
			".rtf", 
			".html", 
			".htm", 
			".wps",
			".odt"
	};
	private final String[] DEFAULT_ACCEPTABLE_MIME_TYPES = new String[] {
			"application/msword", 
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
			"application/vnd.sun.xml.writer",  
			"application/vnd.ms-powerpoint", 
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/pdf",
			"text/plain", 
			"application/rtf", 
			"text/html", 
			"text/html", 
			"application/vnd.ms-works", 
			"application/vnd.oasis.opendocument.text"
	};
	
	// Sakai.properties overriding the arrays above
	private final String PROP_ACCEPT_ALL_FILES = "urkund.accept.all.files";

	private final String PROP_ACCEPTABLE_FILE_EXTENSIONS = "urkund.acceptable.file.extensions";
	private final String PROP_ACCEPTABLE_MIME_TYPES = "urkund.acceptable.mime.types";

	// A list of the displayable file types (ie. "Microsoft Word", "WordPerfect document", "Postscript", etc.)
	private final String PROP_ACCEPTABLE_FILE_TYPES = "urkund.acceptable.file.types";

	private final String KEY_FILE_TYPE_PREFIX = "file.type";
	
	final static long LOCK_PERIOD = 12000000;
	private Long maxRetry = 20L;
	
	@Setter	protected UserDirectoryService userDirectoryService;
	@Setter	protected ToolManager toolManager;
	@Setter	protected ContentHostingService contentHostingService;
	@Setter	protected SakaiPersonManager sakaiPersonManager;
	@Setter	protected UrkundAccountConnection urkundConn;
	@Setter	protected UrkundContentValidator urkundContentValidator;
	@Setter protected ContentReviewSiteAdvisor siteAdvisor;
	@Setter	protected ContentReviewQueueService crqs;
	
	public void init() {
		maxRetry = Long.valueOf(serverConfigurationService.getInt("urkund.maxRetry", 20));
		spoofEmailContext = serverConfigurationService.getString("urkund.spoofemailcontext", null);
	}

	/* --------------------------------------------------------------------
	 * Implementing ContentReviewService methods
	 * --------------------------------------------------------------------
	 */
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	// ---------------------- Queue related methods ----------------------
	@Override
	public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content)
			throws QueueException {

		log.debug("Method called queueContent({}, {}, {})", userId, siteId, taskId);

		if (content == null || content.isEmpty()) {
			return;
		}

		if (userId == null) {
			log.debug("Using current user");
			userId = userDirectoryService.getCurrentUser().getId();
		}

		if (siteId == null) {
			log.debug("Using current site");
			siteId = toolManager.getCurrentPlacement().getContext();
		}

		if (taskId == null) {
			log.debug("Generating default taskId");
			taskId = siteId + " " + "defaultAssignment";
		}

		log.debug("Adding content from site: {} and user: {} for task: {} to submission queue", siteId, userId, taskId);
		crqs.queueContent(getProviderId(), userId, siteId, taskId, content);
	}
	
	@Override
	public Long getReviewStatus(String contentId) throws QueueException {
		return crqs.getReviewStatus(getProviderId(), contentId);
	}

	@Override
	public Date getDateQueued(String contextId) throws QueueException {
		return crqs.getDateQueued(getProviderId(), contextId);
	}

	@Override
	public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
		return crqs.getDateSubmitted(getProviderId(), contextId);
	}

	//TODO : query with filter for SUBMITTED_REPORT_AVAILABLE_CODE
	@Override
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		List<ContentReviewItem> items = crqs.getContentReviewItems(getProviderId(), siteId, taskId);
		List<ContentReviewItem> ret = new ArrayList<ContentReviewItem>();
		for(ContentReviewItem item : items){
			if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) == 0){
				ret.add(item);
			}
		}
		return ret;
	}
	
	@Override
	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		return getReportList(siteId, null);
	}

	@Override
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return crqs.getContentReviewItems(getProviderId(), siteId, taskId);
	}

	@Override
	public void resetUserDetailsLockedItems(String userId) {
		crqs.resetUserDetailsLockedItems(getProviderId(), userId);
	}

	@Override
	public void removeFromQueue(String contentId) {
		crqs.removeFromQueue(getProviderId(), contentId);
	}
	
	private Optional<ContentReviewItem> getItemByContentId(String contentId) {
		return crqs.getQueuedItem(getProviderId(), contentId);
	}
	
	/*
	 * Get the next item that needs to be submitted
	 *
	 */
	private Optional<ContentReviewItem> getNextItemInSubmissionQueue() {
		return crqs.getNextItemInQueueToSubmit(getProviderId());
	}
	
	// ---------------------- Score and Reports ----------------------
	@Override
	public boolean allowResubmission() {
		return true;
	}

	@Override
	public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException, ReportException, Exception {

		Optional<ContentReviewItem> matchingItem = getItemByContentId(contentId);
		if (!matchingItem.isPresent()) {
			log.debug("Content {} has not been queued previously", contentId);
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}
		ContentReviewItem item = matchingItem.get();
		if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
			log.debug("Report not available: {}", item.getStatus());
			throw new ReportException("Report not available: " + item.getStatus());
		}

		return item.getReviewScore().intValue();
	}

	@Override
	public String getReviewReport(String contentId, String assignmentRef, String userId) throws QueueException, ReportException {

		Optional<ContentReviewItem> matchingItem = getItemByContentId(contentId);

		if (!matchingItem.isPresent()) {
			log.debug("Content {} has not been queued previously", contentId);
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		ContentReviewItem item = matchingItem.get();

		// check that the report is available
		if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
			log.debug("Report not available: {}", item.getStatus());
			throw new ReportException("Report not available: " + item.getStatus());
		}

		// if the database record does not show report available check with urkund
		String reportURL = item.getProperties().get(ContentReviewConstants.URKUND_REPORT_URL);
		if(StringUtils.isBlank(reportURL)){

			List<UrkundSubmissionData> submissionDataList = urkundConn.getReports(item.getExternalId());
			
			for(UrkundSubmissionData submissionData : submissionDataList) {
				if (submissionData != null) {
					if(submissionData.getExternalId() != null && submissionData.getExternalId().equals(item.getExternalId())){
						if(STATE_ANALYZED.equals(submissionData.getStatus().get("State"))) {
							try{
								reportURL = (String)submissionData.getReport().get("ReportUrl");
								
								//store reportURL
								item.getProperties().put(ContentReviewConstants.URKUND_REPORT_URL, reportURL);
								
								//store OptOutURL
								Map<String, Object> optOutInfo = (Map)submissionData.getDocument().get("OptOutInfo");
								item.getProperties().put(ContentReviewConstants.URKUND_OPTOUT_URL, (String)optOutInfo.get("Url"));
	
								crqs.update(item);
							}catch(Exception e){
								throw new ReportException("Error getting data from response");
							}
						}
					}
				} else {
					log.error("Error retrieving Urkund report URL");
				}
			}
		}
		
		return reportURL;
	}

	@Override
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId) throws QueueException, ReportException {
		return getReviewReport(contentId, assignmentRef, userId);
	}
	
	@Override	
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId) throws QueueException, ReportException {
		return getReviewReport(contentId, assignmentRef, userId);
	}

	@Override
	public void processQueue() {

		log.info("Processing submission queue");
		int errors = 0;
		int success = 0;
		
		Optional<ContentReviewItem> nextItem = null;
		while ((nextItem = getNextItemInSubmissionQueue()).isPresent()) {
			ContentReviewItem currentItem = nextItem.get();
			
			//if document has no external id, we need to add it to urkund
			if(StringUtils.isBlank(currentItem.getExternalId())) {
				
				if(!processItem(currentItem)){
					errors++;
					continue;
				}
				
				//check if we have added it correctly
				if(addDocumentToUrkund(currentItem) == false){
					errors++;
					continue;
				}
			}
			
			
			if(!processItem(currentItem)){
				errors++;
				continue;
			}
			
			List<UrkundSubmissionData> submissionDataList = urkundConn.getReports(currentItem.getExternalId());
			
			//TODO : CHECK THIS : get the first matching report
			UrkundSubmissionData submissionData = null;
			for(UrkundSubmissionData sd : submissionDataList) {
				if (sd != null) {
					if(sd.getExternalId() != null && sd.getExternalId().equals(currentItem.getExternalId())){
						submissionData = sd;
						break;
					}
				}
			}

			if (submissionData != null) {
				try {
					if(STATE_SUBMITTED.equals(submissionData.getStatus().get("State"))) {
						success++;
					} else if(STATE_ACCEPTED.equals(submissionData.getStatus().get("State"))) {
						log.debug("Submission successful");
						currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
						currentItem.setRetryCount(Long.valueOf(0));
						currentItem.setNextRetryTime(new Date());
						currentItem.setLastError(null);
						currentItem.setErrorCode(null);
						success++;
						crqs.update(currentItem);
					} else if(STATE_ANALYZED.equals(submissionData.getStatus().get("State"))) {
						currentItem.setReviewScore((int) Math.round(submissionData.getSignificance()));
						currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
						success++;
						crqs.update(currentItem);
					} else if(STATE_REJECTED.equals(submissionData.getStatus().get("State"))) {
						processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "Submission Rejected : "+submissionData.getStatus().get("Message"), null);
						errors++;
					} else if(STATE_ERROR.equals(submissionData.getStatus().get("State"))) {
						processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "Submission Error : "+submissionData.getStatus().get("Message"), null);
						errors++;
					} else {
						processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Submission Unknown State ("+submissionData.getStatus().get("State")+") : "+submissionData.getStatus().get("Message"), null);
						errors++;
					}
				
				} catch(Exception e){
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Exception processing submission data : "+e.getMessage(), null);
					errors++;
				}
			} else {
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Submission Error (Submission Data is null)", null);
				errors++;
			}
		}

		log.info("Submission queue run completed: {} items submitted, {} errors.", success, errors);
	}

	// ---------------------- Check for reports ----------------------
	@SuppressWarnings({ "deprecation" })
	@Override
	public void checkForReports() {

		log.info("Fetching reports from Urkund");
		
		// get the list of all items that are waiting for reports
		List<ContentReviewItem> awaitingReport = crqs.getAwaitingReports(getProviderId());
		Iterator<ContentReviewItem> listIterator = awaitingReport.iterator();

		log.debug("There are {} submissions awaiting reports", awaitingReport.size());

		int errors = 0;
		int success = 0;
		int inprogress = 0;
		ContentReviewItem currentItem = null;
		while (listIterator.hasNext()) {
			currentItem = (ContentReviewItem) listIterator.next();

			// has the item reached its next retry time?
			if (currentItem.getNextRetryTime() == null) {
				currentItem.setNextRetryTime(new Date());
			}

			if (currentItem.getNextRetryTime().after(new Date())) {
				// we haven't reached the next retry time
				log.info("checkForReports :: next retry time not yet reached for item: {}", currentItem.getId());
				crqs.update(currentItem);
				continue;
			}

			if(!processItem(currentItem)){
				errors++;
				continue;
			}

			//back to analysis (this should not happen)
			if (StringUtils.isBlank(currentItem.getExternalId())) {
				currentItem.setStatus(Long.valueOf(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE));
				crqs.update(currentItem);
				errors++;
				continue;
			}

			List<UrkundSubmissionData> submissionDataList = urkundConn.getReports(currentItem.getExternalId());
			
			//TODO : CHECK THIS : get the first matching report
			UrkundSubmissionData submissionData = null;
			for(UrkundSubmissionData sd : submissionDataList) {
				if (sd != null) {
					if(sd.getExternalId() != null && sd.getExternalId().equals(currentItem.getExternalId())){
						submissionData = sd;
						break;
					}
				}
			}
			
			if (submissionData != null) {
				if(STATE_ANALYZED.equals(submissionData.getStatus().get("State"))) {
					currentItem.setReviewScore((int) Math.round(submissionData.getSignificance()));
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
					
					//store reportURL
					currentItem.getProperties().put(ContentReviewConstants.URKUND_REPORT_URL, (String)submissionData.getReport().get("ReportUrl"));
					
					//store OptOutURL
					Map<String, Object> optOutInfo = (Map)submissionData.getDocument().get("OptOutInfo");
					currentItem.getProperties().put(ContentReviewConstants.URKUND_OPTOUT_URL, (String)optOutInfo.get("Url"));

					crqs.update(currentItem);
					success++;
				} else if(STATE_ACCEPTED.equals(submissionData.getStatus().get("State"))) {
					inprogress++;
				} else if(STATE_ERROR.equals(submissionData.getStatus().get("State"))) {
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE, "Report Error : "+submissionData.getStatus().get("Message"), null);
					errors++;
				} else {
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Report Unknown State ("+submissionData.getStatus().get("State")+") : "+submissionData.getStatus().get("Message"), null);
					errors++;
				}
			} else {
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE, null, null);
				errors++;
				continue;
			}
		}

		log.info("Finished fetching reports from Urkund : {} success items, {} in progress, {} errors", success, inprogress, errors);
	}

	@Override
	public boolean allowAllContent() {
		return serverConfigurationService.getBoolean(PROP_ACCEPT_ALL_FILES, false);
	}

	@Override
	public boolean isAcceptableContent(ContentResource resource) {
		return urkundContentValidator.isAcceptableContent(resource);
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes()
	{
		Map<String, SortedSet<String>> acceptableExtensionsToMimeTypes = new HashMap<>();
		String[] acceptableFileExtensions = getAcceptableFileExtensions();
		String[] acceptableMimeTypes = getAcceptableMimeTypes();
		int min = Math.min(acceptableFileExtensions.length, acceptableMimeTypes.length);
		for (int i = 0; i < min; i++)
		{
			appendToMap(acceptableExtensionsToMimeTypes, acceptableFileExtensions[i], acceptableMimeTypes[i]);
		}

		return acceptableExtensionsToMimeTypes;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions()
	{
		Map<String, SortedSet<String>> acceptableFileTypesToExtensions = new LinkedHashMap<>();
		String[] acceptableFileTypes = getAcceptableFileTypes();
		String[] acceptableFileExtensions = getAcceptableFileExtensions();
		if (acceptableFileTypes != null && acceptableFileTypes.length > 0)
		{
			// The acceptable file types are listed in sakai.properties. Sakai.properties takes precedence.
			int min = Math.min(acceptableFileTypes.length, acceptableFileExtensions.length);
			for (int i = 0; i < min; i++)
			{
				appendToMap(acceptableFileTypesToExtensions, acceptableFileTypes[i], acceptableFileExtensions[i]);
			}
		}
		else
		{
			/*
			 * acceptableFileTypes not specified in sakai.properties (this is normal).
			 * Use ResourceLoader to resolve the file types.
			 * If the resource loader doesn't find the file extenions, log a warning and return the [missing key...] messages
			 */
			ResourceLoader resourceLoader = new ResourceLoader("urkund");
			for( String fileExtension : acceptableFileExtensions )
			{
				String key = KEY_FILE_TYPE_PREFIX + fileExtension;
				if (!resourceLoader.getIsValid(key))
				{
					log.warn("While resolving acceptable file types for Urkund, the sakai.property {} is not set, and the message bundle {} could not be resolved. Displaying [missing key ...] to the user", PROP_ACCEPTABLE_FILE_TYPES, key);
				}
				String fileType = resourceLoader.getString(key);
				appendToMap( acceptableFileTypesToExtensions, fileType, fileExtension );
			}
		}

		return acceptableFileTypesToExtensions;
	}

	@Override
	public boolean isSiteAcceptable(Site site) {
		if (site == null) {
			return false;
		}

		log.debug("isSiteAcceptable: {} / {}", site.getId(), site.getTitle());

		// Delegated to another bean
		if (siteAdvisor != null) {
			return siteAdvisor.siteCanUseReviewService(site);
		}

		// Check site property
		ResourceProperties properties = site.getProperties();

		String prop = (String) properties.get(URKUND_SITE_PROPERTY);
		if (StringUtils.isNotBlank(prop)) {
			log.debug("Using site property: {}", prop);
			return Boolean.parseBoolean(prop);
		}

		// No property set, no restriction on site types, so allow
		return true;
	}

	@Override
	public String getIconCssClassforScore(int score, String contentId) {
		if (score == 0) {
			return "contentReviewIconThreshold-4";
		} else if (score <= 39) {
			return "contentReviewIconThreshold-3";
		} else if (score <= 54) {
			return "contentReviewIconThreshold-2";
		} else {
			return "contentReviewIconThreshold-1";
		}
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, String userRef) {
		String userId = EntityReference.getIdFromRef(userRef);
		ResourceLoader resourceLoader = new ResourceLoader(userId, "urkund");
		return resourceLoader.getString(messageCode);
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode) {
		return getLocalizedStatusMessage(messageCode, userDirectoryService.getCurrentUser().getReference());
	}
	
	@Override
	public String getLocalizedStatusMessage(String messageCode, Locale locale) {
		// TODO not sure how to do this with the sakai resource loader
		return null;
	}

	@Override
	public String getReviewError(String contentId) {
		return getLocalizedReviewErrorMessage(contentId);
	}

	@Override
	public Map getAssignment(String siteId, String taskId) throws SubmissionException, TransientSubmissionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createAssignment(String siteId, String taskId, Map extraAsnnOpts) throws SubmissionException, TransientSubmissionException {
		// TODO Auto-generated method stub
		
	}

	
	//-----------------------------------------------------------------------------
	// Extra methods
	//-----------------------------------------------------------------------------
	//TODO : add error codes every time 'processError' is called, so we can set i18 messages (CARE : i18 messages do not accept parameters)
	private String getLocalizedReviewErrorMessage(String contentId) {
		log.debug("Returning review error for content: {}", contentId);

		Optional<ContentReviewItem> item = crqs.getQueuedItem(getProviderId(), contentId);

		if (item.isPresent()) {
			// its possible the error code column is not populated
			Integer errorCode = item.get().getErrorCode();
			if (errorCode != null) {
				return getLocalizedStatusMessage(errorCode.toString());
			}
			return item.get().getLastError();
		}

		log.debug("Content {} has not been queued previously", contentId);
		return null;
	}
	
	/**
	 * find the next time this item should be tried
	 * 
	 * @param retryCount
	 * @return
	 */
	private Date getNextRetryTime(long retryCount) {
		Double offset = 5.;

		if(retryCount > 0) {
			offset = 15 * Math.pow(2, retryCount-1);
		}
		if(offset > 480) {
			offset = 480.;
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, offset.intValue());
		return cal.getTime();
	}
	
	private boolean addDocumentToUrkund(ContentReviewItem currentItem) {
		// to get the name of the initial submited file we need the title
		ContentResource resource = null;
		String fileName = null;
		try {
			try {
				resource = contentHostingService.getResource(currentItem.getContentId());
				
				//this never should happen, user can not add to queue invalid files
				if(!urkundContentValidator.isAcceptableContent(resource)){
					log.error("Not valid extension: resource with id {}", currentItem.getContentId());
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "Not valid extension: resource with id " + currentItem.getContentId(), null);
					return false;
				}

			} catch (TypeException e4) {

				log.warn("TypeException: resource with id {}", currentItem.getContentId());
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "TypeException: resource with id " + currentItem.getContentId(), null);
				return false;
			} catch (IdUnusedException e) {
				log.warn("IdUnusedException: no resource with id {}", currentItem.getContentId());
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "IdUnusedException: no resource with id " + currentItem.getContentId(), null);
				return false;
			}
			ResourceProperties resourceProperties = resource.getProperties();
			fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
			fileName = escapeFileName(fileName, resource.getId());
			if("true".equals(resourceProperties.getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION))) {
				fileName += ".html";
			}
		} catch (PermissionException e2) {
			log.error("Submission failed due to permission error.", e2);
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Permission exception: " + e2.getMessage(), null);
			return false;
		}
		
		User user;
		try {
			user = userDirectoryService.getUser(currentItem.getUserId());
		} catch (UserNotDefinedException e1) {
			log.error("Submission attempt unsuccessful - User not found.", e1);
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "User not found : Contact Service desk for help", null);
			return false;
		}

		String submitterEmail = getEmail(user, currentItem.getSiteId());
		log.debug("Using email = {}, for user eid = {}, id = {}, site id = {}", submitterEmail, user.getEid(), user.getId(), currentItem.getSiteId());

		if (submitterEmail == null) {
			log.error("User: {} has no valid email", user.getEid());
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE, "Invalid user email : Contact Service desk for help", null);
			return false;
		}
		
		String externalId = contentHostingService.getUuid(resource.getId())+"-"+(new Date()).getTime();
		UrkundSubmissionData submissionData = null;
		try {
			submissionData = urkundConn.uploadFile(submitterEmail, externalId, fileName, resource.getContent(), resource.getContentType());
		} catch (ServerOverloadException e) {
			log.error("Submission failed.", e);
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Upload exception: " + e.getMessage(), null);
			return false;
		}
		
		if(submissionData != null){
			if(STATE_SUBMITTED.equals(submissionData.getStatus().get("State"))) {
				log.debug("Submission successful (addDocumentToUrkund)");
				currentItem.setExternalId(externalId);
				currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE);
				currentItem.setRetryCount(Long.valueOf(0));
				currentItem.setNextRetryTime(new Date());
				currentItem.setLastError(null);
				currentItem.setErrorCode(null);
				currentItem.setDateSubmitted(new Date());
				crqs.update(currentItem);
				return true;
			}
		}
		processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Add Document To Urkund Error", null);
		return false;
	}
	
	public String escapeFileName(String fileName, String contentId) {
		log.debug("original filename is: {}", fileName);
		if (fileName == null) {
			// use the id
			fileName = contentId;
		}
		log.debug("fileName is : {}", fileName);
		try {
			fileName = URLDecoder.decode(fileName, "UTF-8");
			// in rare cases it seems filenames can be double encoded
			while (fileName.indexOf("%20") > 0 || fileName.contains("%2520")) {
				fileName = URLDecoder.decode(fileName, "UTF-8");
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException eae) {
			log.warn("Unable to decode fileName: {}", fileName, eae);
			return contentId;
		}

		fileName = fileName.replace(' ', '_');
		// its possible we have double _ as a result of this lets do some
		// cleanup
		fileName = StringUtils.replace(fileName, "__", "_");

		log.debug("fileName is : {}", fileName);
		return fileName;
	}
	
	private String[] getAcceptableMimeTypes()
	{
		String[] mimeTypes = serverConfigurationService.getStrings(PROP_ACCEPTABLE_MIME_TYPES);
		if (mimeTypes != null && mimeTypes.length > 0)
		{
			return mimeTypes;
		}
		return DEFAULT_ACCEPTABLE_MIME_TYPES;
	}
	
	private String[] getAcceptableFileExtensions()
	{
		String[] extensions = serverConfigurationService.getStrings(PROP_ACCEPTABLE_FILE_EXTENSIONS);
		if (extensions != null && extensions.length > 0)
		{
			return extensions;
		}
		return DEFAULT_ACCEPTABLE_FILE_EXTENSIONS;
	}
	
	private String [] getAcceptableFileTypes()
	{
		return serverConfigurationService.getStrings(PROP_ACCEPTABLE_FILE_TYPES);
	}
	
	/**
	 * Inserts (key, value) into a Map<String, Set<String>> such that value is inserted into the value Set associated with key.
	 * The value set is implemented as a TreeSet, so the Strings will be in alphabetical order
	 * Eg. if we insert (a, b) and (a, c) into map, then map.get(a) will return {b, c}
	 */
	private void appendToMap(Map<String, SortedSet<String>> map, String key, String value)
	{
		SortedSet<String> valueList = map.get(key);
		if (valueList == null)
		{
			valueList = new TreeSet<>();
			map.put(key, valueList);
		}
		valueList.add(value);
	}
	
	private void processError( ContentReviewItem item, Long status, String error, Integer errorCode )
	{
		if( status == null )
		{
			IllegalArgumentException ex = new IllegalArgumentException( "Status is null; you must supply a valid status to update when calling processError()" );
			throw ex;
		}
		else
		{
			item.setStatus( status );
		}
		if( error != null )
		{
			item.setLastError(error);
		}
		if( errorCode != null )
		{
			item.setErrorCode( errorCode );
		}

		crqs.update( item );
	}
	
	private boolean processItem(ContentReviewItem currentItem){
		if (currentItem.getRetryCount() == null) {
			currentItem.setRetryCount(Long.valueOf(0));
			currentItem.setNextRetryTime(this.getNextRetryTime(0));
		} else if (currentItem.getRetryCount().intValue() > maxRetry) {
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE, null, null);
			return false;
		} else {
			long l = currentItem.getRetryCount().longValue();
			l++;
			currentItem.setRetryCount(Long.valueOf(l));
			currentItem.setNextRetryTime(this.getNextRetryTime(Long.valueOf(l)));
		}
		crqs.update(currentItem);
		
		return true;
	}
	
	// returns null if no valid email exists
	private String getEmail(User user, String siteId) {

		if (spoofEmailContext != null && spoofEmailContext.length() >= 2 && spoofEmailContext.length() <= 10) {
			return String.format(URKUND_SPOOFED_EMAIL_TEMPLATE, user.getId(), siteId, spoofEmailContext);
		}

		String ret = null;

		// Check account email address
		if (isValidEmail(user.getEmail())) {
			ret = user.getEmail().trim();
		}

		// Lookup system profile email address if necessary
		if (ret == null) {
				SakaiPerson sp = sakaiPersonManager.getSakaiPerson(user.getId(), sakaiPersonManager.getSystemMutableType());
				if (sp != null && isValidEmail(sp.getMail())) {
				ret = sp.getMail().trim();
			}
		}

		return ret;
	}

	/**
	* Is this a valid email the service will recognize
	*
	* @param email
	* @return
	*/
	private boolean isValidEmail(String email) {

		if (email == null || email.equals("")) {
			return false;
		}

		email = email.trim();
		//must contain @
		if (!email.contains("@")) {
			return false;
		}

		//an email can't contain spaces
		if (email.indexOf(" ") > 0) {
			return false;
		}

		//use commons-validator
		EmailValidator validator = EmailValidator.getInstance();
		return validator.isValid(email);
	}

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId){
		Optional<ContentReviewItem> cri = crqs.getQueuedItem(getProviderId(), contentId);
		if(cri.isPresent()){
			ContentReviewItem item = cri.get();
			//Urkund specific work
			return item;
		}
		return null;
	}

	@Override
	public String getEndUserLicenseAgreementLink(String userId) {
		return null;
	}

	@Override
	public Instant getEndUserLicenseAgreementTimestamp() {
		return null;
	}

	@Override
	public String getEndUserLicenseAgreementVersion() {
		return null;
	}

	@Override
	public void webhookEvent(HttpServletRequest request, int providerId, Optional<String> customParam) {
		// TODO Auto-generated method stub
		
	}
}
