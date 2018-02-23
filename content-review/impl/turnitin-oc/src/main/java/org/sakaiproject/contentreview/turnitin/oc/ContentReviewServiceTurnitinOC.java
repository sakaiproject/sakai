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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;

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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
	private static final int TURNITIN_OC_RETRY_TIME_MINS = 30;
	private static final int TURNITIN_MAX_RETRY = 30;

	private String serviceUrl;
	private String apiKey;

	public void init(){
		serviceUrl = serverConfigurationService.getString("turnitin.oc.serviceUrl", "");
		apiKey = serverConfigurationService.getString("turnitin.oc.apiKey", "");
	}

	public boolean allowResubmission() {
		return true;
	}

	public void checkForReports() {

	}

	public void createAssignment(final String contextId, final String assignmentRef, final Map opts)
			throws SubmissionException, TransientSubmissionException {
		
	}

	public List<ContentReviewItem> getAllContentReviewItems(String siteId,
			String taskId) throws QueueException, SubmissionException,
			ReportException {
		return crqs.getContentReviewItems(getProviderId(), siteId, taskId);
	}

	public Map getAssignment(String arg0, String arg1)
			throws SubmissionException, TransientSubmissionException {
		return null;
	}

	public Date getDateQueued(String contextId) throws QueueException {
		return crqs.getDateQueued(getProviderId(), contextId);
	}

	public Date getDateSubmitted(String contextId) throws QueueException,
			SubmissionException {
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

	public String getReviewReport(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException {
		return getAccessUrl(contentId, assignmentRef, userId, false);
	}

	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException {
		/**
		 * contentId: /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
		 */
		return getAccessUrl(contentId, assignmentRef, userId, true);
	}

	public String getReviewReportStudent(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException {
		return getAccessUrl(contentId, assignmentRef, userId, false);
	}

	private String getAccessUrl(String contentId, String assignmentRef, String userId, boolean instructor) throws QueueException, ReportException {
//		//assignmentRef: /assignment/a/f7d8c921-7d5a-4116-8781-9b61a7c92c43/cbb993da-ea12-4e74-bab1-20d16185a655
//		String context = getSiteIdFromConentId(contentId);
//		if(context != null){
//			String externalContentId = getAttachmentId(contentId);
//			String returnUrl = null;
//			String assignmentId = getAssignmentId(assignmentRef, isA2(contentId, assignmentRef));
//			String cacheKey = context + ":" + assignmentId + ":" + userId;
//			//first check if cache already has the URL for this contentId and user
//			if(userUrlCache.containsKey(cacheKey)){
//				Object cacheObj = userUrlCache.get(cacheKey);
//				if(cacheObj != null && cacheObj instanceof Map){
//					Map<String, Object[]> userUrlCacheObj = (Map<String, Object[]>) cacheObj;
//					if(userUrlCacheObj.containsKey(externalContentId)){
//						//check if cache has expired:
//						Object[] cacheItem = userUrlCacheObj.get(externalContentId);
//						Calendar cal = Calendar.getInstance();
//						cal.setTime(new Date());
//						//subtract the exipre time (currently set to 20 while the plag token is set to 30, leaving 10 mins in worse case for instructor to use token)
//						cal.add(Calendar.MINUTE, CACHE_EXPIRE_URLS_MINS * -1);
//						if(((Date) cacheItem[1]).after(cal.getTime())){
//							//token hasn't expired, use it
//							returnUrl = (String) cacheItem[0];
//						}else{
//							//token is expired, remove it
//							userUrlCacheObj.remove(externalContentId);
//							userUrlCache.put(cacheKey, userUrlCacheObj);
//						}
//					}
//				}
//			}
//
//			if(StringUtils.isEmpty(returnUrl)){
//				//instructors get all URLs at once, so only check VC every 2 minutes to avoid multiple calls in the same thread:
//				boolean skip = false;
//				if(instructor && userUrlCache.containsKey(cacheKey)){
//					Object cacheObj = userUrlCache.get(cacheKey);
//					if(cacheObj != null && cacheObj instanceof Map){
//						Map<String, Object[]> userUrlCacheObj = (Map<String, Object[]>) cacheObj;
//						if(userUrlCacheObj.containsKey(VERICITE_CACHE_PLACEHOLDER)){
//							Object[] cacheItem = userUrlCacheObj.get(VERICITE_CACHE_PLACEHOLDER);
//							Calendar cal = Calendar.getInstance();
//							cal.setTime(new Date());
//							//only check vericite every 2 mins to prevent subsequent calls from the same thread
//							cal.add(Calendar.MINUTE, VERICITE_SERVICE_CALL_THROTTLE_MINS * -1);
//							if(((Date) cacheItem[1]).after(cal.getTime())){
//								//we just checked VC, skip asking again
//								skip = true;
//							}
//						}
//					}
//				}
//				if(!skip){
//					//we couldn't find the URL in the cache, so look it up (if instructor, look up all URLs so reduce the number of calls to the API)
//					DefaultApi vericiteApi = getVeriCiteAPI();
//					String tokenUserRole = PARAM_USER_ROLE_LEARNER;
//					String externalContentIDFilter = null;
//					if(instructor){
//						tokenUserRole = PARAM_USER_ROLE_INSTRUCTOR;
//						//keep track of last call to make sure we don't call VC too much
//						Object cacheObject = userUrlCache.get(cacheKey);
//						if(cacheObject == null){
//							cacheObject = new HashMap<String, Object[]>();
//						}
//						((Map<String, Object[]>) cacheObject).put(VERICITE_CACHE_PLACEHOLDER, new Object[]{VERICITE_CACHE_PLACEHOLDER, new Date()});
//						userUrlCache.put(cacheKey, cacheObject);
//					}else{
//						//since students will only be able to see their own content, make sure to filter it:
//						externalContentIDFilter = externalContentId;
//					}
//					List<ReportURLLinkReponse> urls = null;
//					try {
//						String tokenUserFirstName = null, tokenUserLastName = null, tokenUserEmail = null;
//						User user = null;
//						try{
//							user = userDirectoryService.getUser(userId);
//							if(user != null){
//								tokenUserFirstName = user.getFirstName();
//								tokenUserLastName = user.getLastName();
//								tokenUserEmail = user.getEmail();
//							}
//						}catch(Exception e){
//							log.error(e.getMessage(), e);
//						}
//						urls = vericiteApi.reportsUrlsContextIDGet(context, assignmentId, consumer, consumerSecret,  userId, tokenUserRole, tokenUserFirstName, tokenUserLastName, tokenUserEmail, null, externalContentIDFilter);
//					} catch (ApiException e) {
//						log.error(e.getMessage(), e);
//					}
//					if(urls != null){
//						for(ReportURLLinkReponse url : urls){
//							if(externalContentId.equals(url.getExternalContentID())){
//								//this is the current url requested
//								returnUrl = url.getUrl();
//							}
//							//store in cache for later
//							Object cacheObject = userUrlCache.get(cacheKey);
//							if(cacheObject == null){
//								cacheObject = new HashMap<String, Object[]>();
//							}
//							((Map<String, Object[]>) cacheObject).put(url.getExternalContentID(), new Object[]{url.getUrl(), new Date()});
//							userUrlCache.put(cacheKey, cacheObject);
//						}
//					}
//				}
//			}
//			if(StringUtils.isNotEmpty(returnUrl)){
//				//we either found the url in the cache or from the API, return it
//				return returnUrl;
//			}
//		}
//		//shouldn't get here is all went well:
//		throw new ReportException("Url was null or contentId wasn't correct: " + contentId);
		
		return null;
	}

	public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException, Exception {		
		return crqs.getReviewScore(getProviderId(), contentId);
	}

	public Long getReviewStatus(String contentId) throws QueueException {
		return crqs.getReviewStatus(getProviderId(), contentId);
	}

	public String getServiceName() {
		return SERVICE_NAME;
	}

	public boolean isAcceptableContent(ContentResource arg0) {
		//TODO: what does TII accept?
		return true;
	}

	public boolean isSiteAcceptable(Site arg0) {
		return true;
	}

	public void processQueue() {
		log.info("Processing Turnitin OC submission queue");
		int errors = 0;
		int success = 0;
		Optional<ContentReviewItem> nextItem = null;
		while ((nextItem = crqs.getNextItemInQueueToSubmit(getProviderId())).isPresent()) {
			ContentReviewItem item = nextItem.get();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, TURNITIN_OC_RETRY_TIME_MINS);
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
			if(StringUtils.isEmpty(fileName)){
				//set default file name:
				//TODO
			}		
			
			//TODO:
			//Call "Create a Submission" with title and userid (item.getUserId()) and get the report ID
			//Store report ID and status in content review service
			String reportId = "";
			
			//Upload original paper to submission
			try {
				uploadExternalContent(reportId, resource.getContent(), null);
			} catch (Exception e) {
				log.warn("ServerOverloadException: " + item.getContentId(), e);
				item.setLastError(e.getMessage());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			}
			
			//TODO: update content review item status
			
			//TODO: call "Generate Similarity Report"
						
			//TODO: update content review item status
			
			//TODO: reschedule queue to check for score
			
			
			
			//Success
			log.debug("Submission successful");
			item.setExternalId(reportId);
			item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
			item.setRetryCount(Long.valueOf(0));
			item.setLastError(null);
			item.setErrorCode(null);
			item.setDateSubmitted(new Date());
			item.setDateReportReceived(new Date());
			success++;
			crqs.update(item);
		}
		log.info("Submission VeriCite queue run completed: " + success + " items submitted, " + errors + " errors.");
	}

	public void queueContent(String userId, String siteId, String assignmentReference, List<ContentResource> content) throws QueueException{
		crqs.queueContent(getProviderId(), userId, siteId, assignmentReference, content);
	}

	public void removeFromQueue(String contentId) {
		crqs.removeFromQueue(getProviderId(), contentId);
	}

	public void resetUserDetailsLockedItems(String arg0) {
		// TODO Auto-generated method stub

	}


	public String getReviewError(String contentId){
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
		return serviceUrl + ((StringUtils.isNotEmpty(serviceUrl) && serviceUrl.endsWith("/")) ? "" : "/") + TURNITIN_OC_API_VERSION + "/";
	}

	private void uploadExternalContent(String reportId, byte[] data, Object headers){
		URL url = null;
		HttpURLConnection connection = null;
		DataOutputStream out = null;
		try {
			url = new URL(getNormalizedServiceUrl() + "submissions/" + reportId  + "/original");

			connection=(HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");

			String headerString = Objects.toString(headers, "");
			log.debug("Headers: " + headerString);
			//{x-amz-server-side-encryption=AES256}
			headerString = headerString.replace("{", "").replace("}", "");
			String[] headerPairs = headerString.split(",");
			for (String headerPair : headerPairs) {
				headerPair = headerPair.trim();
				String[] pairKeyValue = headerPair.split("=");
				if (pairKeyValue.length == 2) {
					connection.setRequestProperty(pairKeyValue[0].trim(), pairKeyValue[1].trim());
				}
			}

			out = new DataOutputStream(connection.getOutputStream());
			out.write(data);
			out.close();
			int responseCode = connection.getResponseCode();
			if(responseCode < 200 || responseCode >= 300){
				//TODO: do not silently fail (including the catch exceptions
				log.error("VeriCite upload content failed with code: " + responseCode);
			}
		} catch (MalformedURLException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}finally {
			if(out != null){
				try{
					out.close();
				}catch(Exception e){
					log.error(e.getMessage(), e);
				}
			}
		}

	}

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId){
		Optional<ContentReviewItem> cri = crqs.getQueuedItem(getProviderId(), contentId);
		if(cri.isPresent()){
			ContentReviewItem item = cri.get();

			//Vericite specific work
			try {
				int score = getReviewScore(contentId, item.getTaskId(), null);
				log.debug(" getReviewScore returned a score of: {} ", score);
			} catch(Exception e) {
				log.error("Vericite - getReviewScore error called from getContentReviewItemByContentId with content {} - {}", contentId, e.getMessage());
			}

			return item;
		}
		return null;
	}
}
