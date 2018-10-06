/**********************************************************************************
 *
 * Copyright (c) 2016 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.compilatio;

import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.advisors.ContentReviewSiteAdvisor;
import org.sakaiproject.contentreview.compilatio.util.CompilatioAPIUtil;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.service.BaseContentReviewService;
import org.sakaiproject.contentreview.service.ContentReviewQueueService;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

public class CompilatioReviewServiceImpl extends BaseContentReviewService {
	
	private static final Log log = LogFactory.getLog(CompilatioReviewServiceImpl.class);
	
	public static final String COMPILATIO_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String SERVICE_NAME = "Compilatio";
	
	// Site property to enable or disable use of Compilatio for the site
	private static final String COMPILATIO_SITE_PROPERTY = "compilatio";
	
	// Define Compilatio's acceptable file extensions and MIME types, order of these arrays DOES matter
	private final String[] DEFAULT_ACCEPTABLE_FILE_EXTENSIONS = new String[] {
			".doc", 
			".docx", 
			".xls", 
			".xls", 
			".xls", 
			".xls", 
			".xlsx", 
			".ppt", 
			".ppt", 
			".ppt", 
			".ppt", 
			".pptx", 
			".pps", 
			".pps", 
			".ppsx", 
			".pdf", 
			".ps", 
			".eps", 
			".txt", 
			".html", 
			".htm", 
			".wpd", 
			".wpd", 
			".odt", 
			".rtf", 
			".rtf", 
			".rtf", 
			".rtf"
	};
	private final String[] DEFAULT_ACCEPTABLE_MIME_TYPES = new String[] {
			"application/msword", 
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
			"application/excel", 
			"application/vnd.ms-excel", 
			"application/x-excel", 
			"application/x-msexcel", 
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
			"application/mspowerpoint", 
			"application/powerpoint", 
			"application/vnd.ms-powerpoint", 
			"application/x-mspowerpoint", 
			"application/vnd.openxmlformats-officedocument.presentationml.presentation", 
			"application/mspowerpoint", 
			"application/vnd.ms-powerpoint", 
			"application/vnd.openxmlformats-officedocument.presentationml.slideshow", 
			"application/pdf", 
			"application/postscript", 
			"application/postscript", 
			"text/plain", 
			"text/html", 
			"text/html", 
			"application/wordperfect", 
			"application/x-wpwin", 
			"application/vnd.oasis.opendocument.text", 
			"text/rtf", 
			"application/rtf", 
			"application/x-rtf", 
			"text/richtext"
	};
	
	// Sakai.properties overriding the arrays above
	private final String PROP_ACCEPT_ALL_FILES = "compilatio.accept.all.files";

	private final String PROP_ACCEPTABLE_FILE_EXTENSIONS = "compilatio.acceptable.file.extensions";
	private final String PROP_ACCEPTABLE_MIME_TYPES = "compilatio.acceptable.mime.types";

	// A list of the displayable file types (ie. "Microsoft Word", "WordPerfect document", "Postscript", etc.)
	private final String PROP_ACCEPTABLE_FILE_TYPES = "compilatio.acceptable.file.types";
	
	private final String PROP_MAX_FILENAME_LENGTH = "compilatio.filename.max.length";
	private final int DEFAULT_MAX_FILENAME_LENGTH = -1;

	private final String KEY_FILE_TYPE_PREFIX = "file.type";

	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO = "report_gen_speed";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY = "0";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE = "2";	
	
	final static long LOCK_PERIOD = 12000000;
	private Long maxRetry = 20L;
	
	private String defaultAssignmentName = null;
	
	public enum CompilatioError {
		INVALID_ID_FOLDER(1), NOT_ENOUGH_SPACE(2), TEMPORARY_UNAVAILABLE(3), INVALID_KEY(4), NOT_ENOUGH_CREDITS(5), ANALYSE_ALREADY_STARTED(6), INVALID_FILE_TYPE(10), NO_CONTENT_FOUND(11), TEXT_EXTRACTION_FAILED(12), NO_TEXT_FOUND(13), UNANALYSABLE_TEXT(14);
	
		private int errorCode;
		public int getErrorCode() {
			return errorCode;
		}
		private CompilatioError(int errorCode) {
			this.errorCode = errorCode;
		}
		
		public static CompilatioError find(int faultCodeInt) {
			CompilatioError errorReturn = null;
			for(CompilatioError error : CompilatioError.values()) {
				if(error.getErrorCode() == faultCodeInt) {
					errorReturn = error;
				}
			}
			
			return errorReturn ;
		}
		
	}
	
	@Setter protected ToolManager toolManager;
	@Setter protected UserDirectoryService userDirectoryService;
	@Setter protected ContentHostingService contentHostingService;
	@Setter protected EntityManager entityManager;
	@Setter protected AssignmentService assignmentService;
	@Setter protected CompilatioAccountConnection compilatioConn;
	@Setter protected CompilatioContentValidator compilatioContentValidator;
	@Setter protected ContentReviewSiteAdvisor siteAdvisor;
	@Setter	ContentReviewQueueService crqs;
	
	public void init() {		
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

		log.debug("Method called queueContent(" + userId + "," + siteId + "," + taskId + ")");

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

		log.debug("Adding content from site " + siteId + " and user: " + userId + " for task: " + taskId
				+ " to submission queue");
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

	//TODO : check SUBMITTED_REPORT_AVAILABLE_CODE
	@Override
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return crqs.getContentReviewItems(getProviderId(), siteId, taskId);
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
		ContentReviewItem item = null;
		try {
			Optional<ContentReviewItem> matchingItem = getItemByContentId(contentId);
			if (!matchingItem.isPresent()) {
				log.debug("Content " + contentId + " has not been queued previously");
			}
			item = matchingItem.get();
			if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
				log.debug("Report not available: " + item.getStatus());
			}
		} catch (Exception e) {
			log.error("(getReviewScore)" + e);
		}

		return item.getReviewScore().intValue();
	}

	@Override
	public String getReviewReport(String contentId, String assignmentRef, String userId) throws QueueException, ReportException {
		
		Optional<ContentReviewItem> matchingItem = getItemByContentId(contentId);

		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		// check that the report is available
		// TODO if the database record does not show report available check with
		// compilatio (maybe)

		ContentReviewItem item = matchingItem.get();

		if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
			log.debug("Report not available: " + item.getStatus());
			throw new ReportException("Report not available: " + item.getStatus());
		}

		// report is available - generate the URL to display
		Map<String, String> params = CompilatioAPIUtil.packMap("action", "getDocumentReportURL", "idDocument", item.getExternalId());

		String reportURL = null;
		try {
			Document reportURLDoc = compilatioConn.callCompilatioReturnDocument(params);
			boolean successQuery = reportURLDoc.getElementsByTagName("sucess") != null;
			if (successQuery) {
				reportURL = getNodeValue("success", reportURLDoc);
			}

		} catch (TransientSubmissionException | SubmissionException e) {
			log.error("Error retrieving Compilatio report URL", e);
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

	// ---------------------- Process queue ----------------------
	@Override
	public void processQueue() {

		log.info("Processing submission queue");
		int errors = 0;
		int success = 0;

		Optional<ContentReviewItem> nextItem = null;
		while ((nextItem = getNextItemInSubmissionQueue()).isPresent()) {
			ContentReviewItem currentItem = nextItem.get();
			
			//if document has no external id, we need to add it to Compilatio
			if(StringUtils.isBlank(currentItem.getExternalId())) {
				
				if(!processItem(currentItem)){
					errors++;
					continue;
				}
				
				//check if we have added it correctly
				if(addDocumentToCompilatio(currentItem) == false){
					errors++;
					continue;
				}
			}
			
			try {
				//check if current item has to be processed after the assignment due date
				String assignmentId = assignmentService.getEntity(entityManager.newReference(currentItem.getTaskId())).getId();
				Assignment a = assignmentService.getAssignment(assignmentId);

				if(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE.equals(a.getProperties().get(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO))) {
					Instant dueDate = a.getDueDate();
					if(dueDate.isAfter(Instant.now())) {
						log.debug("assignment due time not yet reached for item: " + currentItem.getId());
						currentItem.setNextRetryTime(this.getNextRetryTime(0));
						crqs.update(currentItem);
						continue;
					}
				}
			} catch (IdUnusedException | PermissionException e) {
				log.error("Error getting assignment for item "+currentItem.getId(), e);
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, null, null);
				errors++;
				continue;
			}

			log.debug("Attempting to submit content (status:"+currentItem.getStatus()+"): " + currentItem.getContentId() + " for user: "
					+ currentItem.getUserId() + " and site: " + currentItem.getSiteId());						

			if(!processItem(currentItem)){
				errors++;
				continue;
			}
			
			Document document = null;
			// Start Compilation Analyse
			try {
				Map<String, String> params = CompilatioAPIUtil.packMap("action", "startDocumentAnalyse", "idDocument", currentItem.getExternalId());

				document = compilatioConn.callCompilatioReturnDocument(params);

			} catch (TransientSubmissionException | SubmissionException e) {
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Error Submitting Assignment for Submission: " + e.getMessage() + ". Assume unsuccessful", null);
				errors++;
				continue;
			}

			Element root = document.getDocumentElement();

			boolean successQuery = root.getElementsByTagName("sucess") != null;
			if (successQuery) {
				log.debug("Submission successful");
				currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
				currentItem.setRetryCount(Long.valueOf(0));
				currentItem.setNextRetryTime(new Date());
				currentItem.setLastError(null);
				currentItem.setErrorCode(null);
				currentItem.setDateSubmitted(new Date());
				success++;
				crqs.update(currentItem);
			} else {
				String rMessage = getNodeValue("faultstring", root);
				String rCode = getNodeValue("faultcode", root);
				
				//TODO : check this
				log.debug("Submission not successful: " + rMessage + "(" + rCode + ")");
				if (CompilatioError.ANALYSE_ALREADY_STARTED.equals(CompilatioError.valueOf(rCode))) {
					log.debug("ContentReview id " + currentItem.getId() + ", externalId : " + currentItem.getExternalId() + " has new status : " + ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT);
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
					currentItem.setRetryCount(Long.valueOf(0));
					currentItem.setLastError(null);
					currentItem.setErrorCode(null);
					currentItem.setDateSubmitted(new Date());
				} else {
					log.warn("Submission not successful. It will be retried.");
					int errorCodeInt = -1;
					if (CompilatioError.valueOf(rCode) != null) {
						errorCodeInt = CompilatioError.valueOf(rCode).getErrorCode();
					}
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Submission Error: " + rMessage + "(" + rCode + ")", errorCodeInt);
					errors++;
				}
	
				crqs.update(currentItem);
			}
		}

		log.info("Submission queue run completed: " + success + " items submitted, " + errors + " errors.");
	}

	// ---------------------- Check for reports ----------------------
	@SuppressWarnings({ "deprecation" })
	@Override
	public void checkForReports() {
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
		dform.applyPattern(COMPILATIO_DATETIME_FORMAT);

		log.info("Fetching reports from Compilatio");
		
		// get the list of all items that are waiting for reports
		List<ContentReviewItem> awaitingReport = crqs.getAwaitingReports(getProviderId());

		Iterator<ContentReviewItem> listIterator = awaitingReport.iterator();

		log.debug("There are " + awaitingReport.size() + " submissions awaiting reports");

		int errors = 0;
		int success = 0;
		int inprogress = 0;
		ContentReviewItem currentItem;
		while (listIterator.hasNext()) {
			currentItem = (ContentReviewItem) listIterator.next();

			// has the item reached its next retry time?
			if (currentItem.getNextRetryTime() == null) {
				currentItem.setNextRetryTime(new Date());
			}

			if (currentItem.getNextRetryTime().after(new Date())) {
				// we haven't reached the next retry time
				log.info("checkForReports :: next retry time not yet reached for item: " + currentItem.getId());
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

			// get the list from compilatio and see if the review is
			// available

			log.debug("Attempting to update hashtable with reports for site " + currentItem.getSiteId());

			Map<String, String> params = CompilatioAPIUtil.packMap("action", "getDocument", "idDocument", currentItem.getExternalId());

			Document document = null;
			try {
				document = compilatioConn.callCompilatioReturnDocument(params);
			} catch (TransientSubmissionException | SubmissionException e) {
				log.warn("Update failed : " + e.toString(), e);
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE, e.getMessage(), null);
				errors++;
				continue;
			}

			Element root = document.getDocumentElement();
			if (root.getElementsByTagName("documentStatus").item(0) != null) {
				log.debug("Report list returned successfully");

				NodeList objects = root.getElementsByTagName("documentStatus");
				log.debug(objects.getLength() + " objects in the returned list");
				
				String status = getNodeValue("status", root);

				if ("ANALYSE_NOT_STARTED".equals(status)) {
					//send back to the process queue, we need no analyze it again
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "ANALYSE_NOT_STARTED", null);
					errors++;
					continue;
				} else if ("ANALYSE_COMPLETE".equals(status)) {
					String reportVal = getNodeValue("indice", root);
					currentItem.setReviewScore((int) Math.round(Double.parseDouble(reportVal)));
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
					success++;
				} else {
					String progression = getNodeValue("progression", root);
					if (StringUtils.isNotBlank(progression)) {
						currentItem.setReviewScore((int) Double.parseDouble(progression));
						inprogress++;
					}
				}
				currentItem.setDateReportReceived(new Date());
				crqs.update(currentItem);
				log.debug("new report received: " + currentItem.getExternalId() + " -> " + currentItem.getReviewScore());

			} else {
				log.debug("Report list request not successful");
				log.debug(document.getTextContent());
				errors++;
			}
		}

		log.info("Finished fetching reports from Compilatio : "+success+" success items, "+inprogress+" in progress, "+errors+" errors");
	}
	
	@Override
	public boolean allowAllContent() {
		return serverConfigurationService.getBoolean(PROP_ACCEPT_ALL_FILES, false);
	}

	@Override
	public boolean isAcceptableContent(ContentResource resource) {
		return compilatioContentValidator.isAcceptableContent(resource);
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
			ResourceLoader resourceLoader = new ResourceLoader("compilatio");
			for( String fileExtension : acceptableFileExtensions )
			{
				String key = KEY_FILE_TYPE_PREFIX + fileExtension;
				if (!resourceLoader.getIsValid(key))
				{
					log.warn("While resolving acceptable file types for Compilatio, the sakai.property " + PROP_ACCEPTABLE_FILE_TYPES + " is not set, and the message bundle " + key + " could not be resolved. Displaying [missing key ...] to the user");
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

		log.debug("isSiteAcceptable ("+siteAdvisor+"): " + site.getId() + " / " + site.getTitle());

		// Delegated to another bean
		if (siteAdvisor != null) {
			return siteAdvisor.siteCanUseReviewService(site);
		}

		// Check site property
		ResourceProperties properties = site.getProperties();

		String prop = (String) properties.get(COMPILATIO_SITE_PROPERTY);
		if (StringUtils.isNotBlank(prop)) {
			log.debug("Using site property: " + prop);
			return Boolean.parseBoolean(prop);
		}

		// No property set, no restriction on site types, so allow
		return true;
	}

	@Override
	public String getIconCssClassforScore(int score, String contentId) {
		if (score == 0) {
			return "contentReviewIconThreshold-5";
		} else if (score < 25) {
			return "contentReviewIconThreshold-4";
		} else if (score < 50) {
			return "contentReviewIconThreshold-3";
		} else if (score < 75) {
			return "contentReviewIconThreshold-2";
		} else {
			return "contentReviewIconThreshold-1";
		}
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, String userRef) {
		String userId = EntityReference.getIdFromRef(userRef);
		ResourceLoader resourceLoader = new ResourceLoader(userId, "compilatio");
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

	/* --------------------------------------------------------------------
	 * Extra methods
	 * --------------------------------------------------------------------
	 */
	private String getLocalizedReviewErrorMessage(String contentId) {
		
		log.debug("Returning review error for content: " + contentId);

		Optional<ContentReviewItem> item = crqs.getQueuedItem(getProviderId(), contentId);

		if (item.isPresent()) {
			// its possible the error code column is not populated
			Integer errorCode = item.get().getErrorCode();
			if (errorCode != null) {
				return getLocalizedStatusMessage(errorCode.toString());
			}
			return item.get().getLastError();
		}

		log.debug("Content " + contentId + " has not been queued previously");
		return null;
	}
	
	/**
	 * find the next time this item should be tried
	 * 
	 * @param retryCount
	 * @return
	 */
	private Date getNextRetryTime(long retryCount) {
		int offset = 5;

		if (retryCount > 9 && retryCount < 20) {

			offset = 10;

		} else if (retryCount > 19 && retryCount < 30) {
			offset = 20;
		} else if (retryCount > 29 && retryCount < 40) {
			offset = 40;
		} else if (retryCount > 39 && retryCount < 50) {
			offset = 80;
		} else if (retryCount > 49 && retryCount < 60) {
			offset = 160;
		} else if (retryCount > 59) {
			offset = 220;
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, offset);
		return cal.getTime();
	}
	
	
	private boolean addDocumentToCompilatio(ContentReviewItem currentItem) {
		// to get the name of the initial submited file we need the title
		ContentResource resource = null;
		String fileName = null;
		try {
			try {
				resource = contentHostingService.getResource(currentItem.getContentId());
				
				//this never should happen, user can not add to queue invalid files
				if(!compilatioContentValidator.isAcceptableContent(resource)){
					log.error("Not valid extension: resource with id " + currentItem.getContentId());
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "Not valid extension: resource with id " + currentItem.getContentId(), null);
					return false;
				}

			} catch (TypeException e4) {

				log.warn("TypeException: resource with id " + currentItem.getContentId());
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "TypeException: resource with id " + currentItem.getContentId(), null);
				return false;
			} catch (IdUnusedException e) {
				log.warn("IdUnusedException: no resource with id " + currentItem.getContentId());
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE, "IdUnusedException: no resource with id " + currentItem.getContentId(), null);
				return false;
			}
			ResourceProperties resourceProperties = resource.getProperties();
			fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
			fileName = escapeFileName(fileName, resource.getId());
		} catch (PermissionException e2) {
			log.error("Submission failed due to permission error.", e2);
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Permission exception: " + e2.getMessage(), null);
			return false;
		}
		
		fileName = truncateFileName(fileName);

		Document document = null;
		try {
			Map<String, String> params = CompilatioAPIUtil.packMap("action", "addDocumentBase64", "filename",
					URLEncoder.encode(fileName, "UTF-8"), "mimetype", resource.getContentType(), "content",
					Base64.encodeBase64String(resource.getContent()));

			document = compilatioConn.callCompilatioReturnDocument(params);

			//TEMPORARY_UNAVAILABLE
			if (document == null) {
				return false;
			}
			
			Element root = document.getDocumentElement();
			
			String externalId = null;
			if (root.getElementsByTagName("idDocument").item(0) != null) {
				externalId = getNodeValue("idDocument", root);
			}
			
			if (externalId != null) {
				if (externalId.length() > 0) {
					log.debug("Submission successful");
					currentItem.setExternalId(externalId);
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE);
					currentItem.setRetryCount(Long.valueOf(0));
					currentItem.setLastError(null);
					currentItem.setErrorCode(null);
					currentItem.setDateSubmitted(new Date());
					crqs.update(currentItem);
				} else {
					log.warn("invalid external id");
					processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Submission error: no external id received", null);
					return false;
				}
			} else {
				String rMessage = getNodeValue("faultstring", root);
				String rCode = getNodeValue("faultcode", root);
				
				log.debug("Add Document To compilatio not successful: " + rMessage + "(" + rCode + ")");
				int errorCodeInt = -1;
				CompilatioError errorCode = CompilatioError.valueOf(rCode);
				if (errorCode != null) {
					errorCodeInt = errorCode.getErrorCode();
				}
				processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Add Document To compilatio Error: " + rMessage + "(" + rCode + ")", errorCodeInt);
				return false;
			}
		} catch (Exception e) {
			processError(currentItem, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE, "Error Submitting Assignment for Submission: " + e.getMessage() + ". Assume unsuccessful", null);
			return false;
		}

		return true;
	}
	
	public String escapeFileName(String fileName, String contentId) {
		log.debug("original filename is: " + fileName);
		if (fileName == null) {
			// use the id
			fileName = contentId;
		}
		log.debug("fileName is :" + fileName);
		try {
			fileName = URLDecoder.decode(fileName, "UTF-8");
			// in rare cases it seems filenames can be double encoded
			while (fileName.indexOf("%20") > 0 || fileName.contains("%2520")) {
				fileName = URLDecoder.decode(fileName, "UTF-8");
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException eae) {
			log.warn("Unable to decode fileName: " + fileName, eae);
			return contentId;
		}

		fileName = fileName.replace(' ', '_');
		// its possible we have double _ as a result of this lets do some
		// cleanup
		fileName = StringUtils.replace(fileName, "__", "_");

		log.debug("fileName is :" + fileName);
		return fileName;
	}

	private String truncateFileName(String fileName) {
		
		int i = serverConfigurationService.getInt(PROP_MAX_FILENAME_LENGTH, DEFAULT_MAX_FILENAME_LENGTH);
		
		if(StringUtils.isBlank(fileName)) {
			return "noname";
		}
		if(i < 0) {
			return fileName;
		}
		if(fileName.length() < i) {
			return fileName;
		}
		
		// get the extension for later re-use
		String extension = "";
		if (fileName.contains(".")) {
			extension = fileName.substring(fileName.lastIndexOf("."));
		}

		fileName = fileName.substring(0, i - extension.length());
		fileName = fileName + extension;

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
	
	private String getNodeValue(String key, Document document) {
		NodeList nodeList = document.getElementsByTagName(key);
		return getNodeValue(key, nodeList);
	}
	
	private String getNodeValue(String key, Element root) {
		NodeList nodeList = root.getElementsByTagName(key);
		return getNodeValue(key, nodeList);
	}
	
	private String getNodeValue(String key, NodeList nodeList) {
		String ret = "";
		
		if(nodeList != null && nodeList.item(0) != null && nodeList.item(0).getFirstChild() != null) {
			ret = nodeList.item(0).getFirstChild().getNodeValue();
		}
		
		if(ret == null) {
			ret = "";
		}
		
		return ret.trim();
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

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId){
		Optional<ContentReviewItem> cri = crqs.getQueuedItem(getProviderId(), contentId);
		if(cri.isPresent()){
			ContentReviewItem item = cri.get();

			//Compilatio specific work would be here

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
