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
package org.sakaiproject.contentreview.vericite;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.sakaiproject.authz.api.SecurityAdvisor;
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
import org.sakaiproject.contentreview.vericite.client.ApiClient;
import org.sakaiproject.contentreview.vericite.client.ApiException;
import org.sakaiproject.contentreview.vericite.client.api.DefaultApi;
import org.sakaiproject.contentreview.vericite.client.model.AssignmentData;
import org.sakaiproject.contentreview.vericite.client.model.ExternalContentData;
import org.sakaiproject.contentreview.vericite.client.model.ExternalContentUploadInfo;
import org.sakaiproject.contentreview.vericite.client.model.ReportMetaData;
import org.sakaiproject.contentreview.vericite.client.model.ReportScoreReponse;
import org.sakaiproject.contentreview.vericite.client.model.ReportURLLinkReponse;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class ContentReviewServiceVeriCite implements ContentReviewService {

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

	private static final String PARAM_USER_ROLE_INSTRUCTOR = "Instructor";
	private static final String PARAM_USER_ROLE_LEARNER = "Learner";
	private static final String SERVICE_NAME = "VeriCite";
	private static final String VERICITE_API_VERSION = "v1";
	private static final int VERICITY_RETRY_TIME_MINS = 30;
	private static final int VERICITE_MAX_RETRY = 30;
	private static final int VERICITE_SERVICE_CALL_THROTTLE_MINS = 2;
	private static final String VERICITE_CACHE_PLACEHOLDER = "VERICITE_LAST_CHECKED";
	
	private String serviceUrl;
	private String consumer;
	private String consumerSecret;
	
	private MemoryService memoryService;
	//Caches requests for instructors so that we don't have to send a request for every student
	Cache userUrlCache, contentScoreCache, assignmentTitleCache;
	private static final int CACHE_EXPIRE_URLS_MINS = 20;
	
	private static final int CONTENT_SCORE_CACHE_MINS = 5;
	
	public void init(){
		serviceUrl = serverConfigurationService.getString("vericite.serviceUrl", "");
		consumer = serverConfigurationService.getString("vericite.consumer", "");
		consumerSecret = serverConfigurationService.getString("vericite.consumerSecret", "");
		userUrlCache = memoryService.createCache("org.sakaiproject.contentreview.vericite.ContentReviewServiceVeriCite.userUrlCache", new SimpleConfiguration<>(10000, CACHE_EXPIRE_URLS_MINS * 60, -1));
		contentScoreCache = memoryService.createCache("org.sakaiproject.contentreview.vericite.ContentReviewServiceVeriCite.contentScoreCache", new SimpleConfiguration<>(10000, CONTENT_SCORE_CACHE_MINS * 60, -1));
		assignmentTitleCache = memoryService.getCache("org.sakaiproject.contentreview.vericite.ContentReviewServiceVeriCite.assignmentTitleCache");
	}
	
	public boolean allowResubmission() {
		return true;
	}

	public void checkForReports() {
		
	}

	public void createAssignment(final String contextId, final String assignmentRef, final Map opts)
			throws SubmissionException, TransientSubmissionException {
		new Thread(){
			public void run() {
				boolean isA2 = isA2(null, assignmentRef);
				String assignmentId = getAssignmentId(assignmentRef, isA2);
				Map<String, ContentResource> attachmentsMap = new HashMap<String, ContentResource>();
				if(assignmentId != null){
					AssignmentData assignmentData = new AssignmentData();
					if(opts != null){
						if(opts.containsKey("title")){
							assignmentData.setAssignmentTitle(opts.get("title").toString());
						}else if(!isA2){
							//we can find the title from the assignment ref for A1
							String assignmentTitle = getAssignmentTitle(assignmentRef);
							if(assignmentTitle != null){
								assignmentData.setAssignmentTitle(assignmentTitle);
							}
						}
						if(opts.containsKey("instructions")){
							assignmentData.setAssignmentInstructions(opts.get("instructions").toString());
						}
						if(opts.containsKey("exclude_quoted")){
							assignmentData.setAssignmentExcludeQuotes("1".equals(opts.get("exclude_quoted").toString()));
						}
						if(opts.containsKey("dtdue")){
							SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
					        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
					        try {
								Date dueDate = dform.parse(opts.get("dtdue").toString());
								if(dueDate != null){
									assignmentData.setAssignmentDueDate(dueDate.getTime());
								}
							} catch (ParseException e) {
								log.error(e.getMessage(), e);
							}
						}
						//Pass in 0 to delete a grade, otherwise, set the grade.
						assignmentData.setAssignmentGrade(0);
						if(opts.containsKey("points")){
							//points are stored as integers and multiplied by 100 (i.e. 5.5 = 550; 1 = 100, etc)
							try{
								Integer points = Integer.parseInt(opts.get("points").toString())/100;
								assignmentData.setAssignmentGrade(points);
							}catch(Exception e){
								log.error(e.getMessage(), e);
							}
						}
						if(opts.containsKey("attachments") && opts.get("attachments") instanceof List){
							SecurityAdvisor yesMan = new SecurityAdvisor(){
								public SecurityAdvice isAllowed(String arg0, String arg1, String arg2) {
									return SecurityAdvice.ALLOWED;
								}
							};
							securityService.pushAdvisor(yesMan);
							try{
								List<ExternalContentData> attachments = new ArrayList<ExternalContentData>();
								for(String refStr : (List<String>) opts.get("attachments")){
									try {
										Reference ref = entityManager.newReference(refStr);
										ContentResource res = (ContentResource) ref.getEntity();
										if(res != null){
											ExternalContentData attachment = new ExternalContentData();
											String fileName = res.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
											attachment.setFileName(FilenameUtils.getBaseName(fileName));
											attachment.setExternalContentID(getAssignmentAttachmentId(consumer, contextId, assignmentId, res.getId()));
											attachment.setUploadContentLength((int) res.getContentLength());
											attachment.setUploadContentType(FilenameUtils.getExtension(fileName));
											attachments.add(attachment);
											attachmentsMap.put(attachment.getExternalContentID(), res);
										}
									} catch (Exception e){
										log.error(e.getMessage(), e);
									}
								}
								if(attachments.size() > 0){
									assignmentData.setAssignmentAttachmentExternalContent(attachments);
								}
							}catch(Exception e){
								log.error(e.getMessage(), e);
							}finally{
								securityService.popAdvisor(yesMan);
							}
						}
					}
					DefaultApi vericiteApi = getVeriCiteAPI();
					try {
						List<ExternalContentUploadInfo> uploadInfo = vericiteApi.assignmentsContextIDAssignmentIDPost(contextId, assignmentId, consumer, consumerSecret, assignmentData);
						//see if there are any attachment presigned urls to upload to
						if(uploadInfo != null){
							//see if this attachment needs uploaded:
							for(ExternalContentUploadInfo info : uploadInfo){
								if(attachmentsMap.containsKey(info.getExternalContentId())){
									//upload this attachment
									ContentResource res = attachmentsMap.get(info.getExternalContentId());
									try {
										uploadExternalContent(info.getUrlPost(), res.getContent());
									} catch (ServerOverloadException e) {
										log.error(e.getMessage(), e);
									}
								}
							}
						}
					} catch (ApiException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}.start();
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

	public String getIconUrlforScore(Long score) {
		String urlBase = "/library/content-review/";
		String suffix = ".png";

		if (score.compareTo(Long.valueOf(0)) < 0) {
			return urlBase + "greyflag" + suffix;
		}else if (score.equals(Long.valueOf(0))) {
			return urlBase + "blueflag" + suffix;
		} else if (score.compareTo(Long.valueOf(25)) < 0 ) {
			return urlBase + "greenflag" + suffix;
		} else if (score.compareTo(Long.valueOf(50)) < 0  ) {
			return urlBase + "yellowflag" + suffix;
		} else if (score.compareTo(Long.valueOf(75)) < 0 ) {
			return urlBase + "orangeflag" + suffix;
		} else {
			return urlBase + "redflag" + suffix;
		}
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
		//assignmentRef: /assignment/a/f7d8c921-7d5a-4116-8781-9b61a7c92c43/cbb993da-ea12-4e74-bab1-20d16185a655
		String context = getSiteIdFromConentId(contentId);
		if(context != null){
			String externalContentId = getAttachmentId(contentId);
			String cacheKey = context + ":" + userId;
			String returnUrl = null;
			String assignmentId = getAssignmentId(assignmentRef, isA2(contentId, assignmentRef));
			//first check if cache already has the URL for this contentId and user
			if(userUrlCache.containsKey(cacheKey)){
				Map<String, Object[]> userUrlCacheObj = (Map<String, Object[]>) userUrlCache.get(cacheKey);
				if(userUrlCacheObj.containsKey(externalContentId)){
					//check if cache has expired:
					Object[] cacheItem = userUrlCacheObj.get(externalContentId);
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					//subtract the exipre time (currently set to 20 while the plag token is set to 30, leaving 10 mins in worse case for instructor to use token)
					cal.add(Calendar.MINUTE, CACHE_EXPIRE_URLS_MINS * -1);
					if(((Date) cacheItem[1]).after(cal.getTime())){
						//token hasn't expired, use it
						returnUrl = (String) cacheItem[0];
					}else{
						//token is expired, remove it
						userUrlCacheObj.remove(externalContentId);
						userUrlCache.put(cacheKey, userUrlCacheObj);
					}
				}
			}
			
			if(StringUtils.isEmpty(returnUrl)){
				//instructors get all URLs at once, so only check VC every 2 minutes to avoid multiple calls in the same thread:
				boolean skip = false;
				if(instructor && userUrlCache.containsKey(cacheKey)){
					Map<String, Object[]> userUrlCacheObj = (Map<String, Object[]>) userUrlCache.get(cacheKey);
					if(userUrlCacheObj.containsKey(VERICITE_CACHE_PLACEHOLDER)){
						Object[] cacheItem = userUrlCacheObj.get(VERICITE_CACHE_PLACEHOLDER);
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date());
						//only check vericite every 2 mins to prevent subsequent calls from the same thread
						cal.add(Calendar.MINUTE, VERICITE_SERVICE_CALL_THROTTLE_MINS * -1);
						if(((Date) cacheItem[1]).after(cal.getTime())){
							//we just checked VC, skip asking again
							skip = true;
						}
					}
				}
				if(!skip){
					//we couldn't find the URL in the cache, so look it up (if instructor, look up all URLs so reduce the number of calls to the API)
					DefaultApi vericiteApi = getVeriCiteAPI();
					String tokenUserRole = PARAM_USER_ROLE_LEARNER;
					String externalContentIDFilter = null;
					if(instructor){
						tokenUserRole = PARAM_USER_ROLE_INSTRUCTOR;
						//keep track of last call to make sure we don't call VC too much
						Object cacheObject = userUrlCache.get(cacheKey);
						if(cacheObject == null){
							cacheObject = new HashMap<String, Object[]>();
						}
						((Map<String, Object[]>) cacheObject).put(VERICITE_CACHE_PLACEHOLDER, new Object[]{VERICITE_CACHE_PLACEHOLDER, new Date()});
						userUrlCache.put(cacheKey, cacheObject);
					}else{
						//since students will only be able to see their own content, make sure to filter it:
						externalContentIDFilter = externalContentId;
					}
					List<ReportURLLinkReponse> urls = null;
					try {
						urls = vericiteApi.reportsUrlsContextIDGet(context, assignmentId, consumer, consumerSecret,  userId, tokenUserRole, null, externalContentIDFilter);
					} catch (ApiException e) {
						log.error(e.getMessage(), e);
					}
					if(urls != null){
						for(ReportURLLinkReponse url : urls){
							if(externalContentId.equals(url.getExternalContentID())){
								//this is the current url requested
								returnUrl = url.getUrl();
							}
							//store in cache for later
							Object cacheObject = userUrlCache.get(cacheKey);
							if(cacheObject == null){
								cacheObject = new HashMap<String, Object[]>();
							}
							((Map<String, Object[]>) cacheObject).put(url.getExternalContentID(), new Object[]{url.getUrl(), new Date()});
							userUrlCache.put(cacheKey, cacheObject);
						}
					}
				}
			}
			if(StringUtils.isNotEmpty(returnUrl)){
				//we either found the url in the cache or from the API, return it
				return returnUrl;
			}
		}
		//shouldn't get here is all went well:
		throw new ReportException("Url was null or contentId wasn't correct: " + contentId);
	}

	public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException, Exception {
		/**
		 * contentId: /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
		 * assignmentRef: /assignment/a/f7d8c921-7d5a-4116-8781-9b61a7c92c43/cbb993da-ea12-4e74-bab1-20d16185a655
		 */
		String externalContentId = getAttachmentId(contentId);
		//first check if contentId already exists in cache:
		boolean isA2 = isA2(contentId, null);
		String context = getSiteIdFromConentId(contentId);
		Integer score = null;
		String assignment = getAssignmentId(assignmentRef, isA2);
		if(StringUtils.isNotEmpty(assignment)){
			if(contentScoreCache.containsKey(assignment)){
				Map<String, Map<String, Object[]>> contentScoreCacheObject = (Map<String, Map<String, Object[]>>) contentScoreCache.get(assignment);
				if(contentScoreCacheObject.containsKey(userId) 
						&& contentScoreCacheObject.get(userId).containsKey(externalContentId)){
					Object[] cacheItem = contentScoreCacheObject.get(userId).get(externalContentId);
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					//subtract the exipre time
					cal.add(Calendar.MINUTE, CONTENT_SCORE_CACHE_MINS * -1);
					if(((Date) cacheItem[1]).after(cal.getTime())){
						//token hasn't expired, use it
						score = (Integer) cacheItem[0];
					}else{
						//token is expired, remove it
						contentScoreCacheObject.remove(userId);
						contentScoreCache.put(assignment, contentScoreCacheObject);
					}
				}
			}
		}
		if(score == null){
			//wasn't in cache
			//make sure we didn't just check VC:
			boolean skip = false;
			if(StringUtils.isNotEmpty(assignment)
					&& contentScoreCache.containsKey(assignment)){ 
				Map<String, Map<String, Object[]>> contentScoreCacheObject = (Map<String, Map<String, Object[]>>) contentScoreCache.get(assignment);
				if(contentScoreCacheObject.containsKey(VERICITE_CACHE_PLACEHOLDER)
						&& contentScoreCacheObject.get(VERICITE_CACHE_PLACEHOLDER).containsKey(VERICITE_CACHE_PLACEHOLDER)){
					Object[] cacheItem = contentScoreCacheObject.get(VERICITE_CACHE_PLACEHOLDER).get(VERICITE_CACHE_PLACEHOLDER);
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					//only check vericite every 2 mins to prevent subsequent calls from the same thread
					cal.add(Calendar.MINUTE, VERICITE_SERVICE_CALL_THROTTLE_MINS * -1);
					if(((Date) cacheItem[1]).after(cal.getTime())){
						//we just checked VC, skip asking again
						skip = true;
					}
				}
			}			
			//look up score in VC 
			if(context != null && !skip){
				DefaultApi vericiteApi = getVeriCiteAPI();
				String externalContentID = null;			
				if(assignmentRef == null){
					externalContentID = externalContentId;
				}			
				List<ReportScoreReponse> scores = vericiteApi.reportsScoresContextIDGet(context, consumer, consumerSecret, assignment, null, externalContentID);
				if(scores != null){
					for(ReportScoreReponse scoreResponse : scores){
						if(externalContentId.equals(scoreResponse.getExternalContentId())){
							score = scoreResponse.getScore();
						}
						//only cache the score if it is > 0
						if(scoreResponse.getScore() != null && scoreResponse.getScore().intValue() >= 0){
							Object userCacheMap = contentScoreCache.get(scoreResponse.getAssignment());
							if(userCacheMap == null){
								userCacheMap = new HashMap<String, Map<String, Object[]>>();
							}
							Map<String, Object[]> cacheMap = ((Map<String, Map<String, Object[]>>) userCacheMap).get(scoreResponse.getUser());
							if(cacheMap == null){
								cacheMap = new HashMap<String, Object[]>();
							}
							cacheMap.put(scoreResponse.getExternalContentId(), new Object[]{scoreResponse.getScore(), new Date()});
							((Map<String, Map<String, Object[]>>) userCacheMap).put(scoreResponse.getUser(), cacheMap);								
							contentScoreCache.put(scoreResponse.getAssignment(), userCacheMap);
						}
					}
				}
				//keep track of last call to make sure we don't call VC too much
				if(StringUtils.isNotEmpty(assignment)){
					Object userCacheMap = contentScoreCache.get(assignment);
					if(userCacheMap == null){
						userCacheMap = new HashMap<String, Map<String, Object[]>>();
					}
					Map<String, Object[]> cacheMap = ((Map<String, Map<String, Object[]>>) userCacheMap).get(VERICITE_CACHE_PLACEHOLDER);
					if(cacheMap == null){
						cacheMap = new HashMap<String, Object[]>();
					}
					cacheMap.put(VERICITE_CACHE_PLACEHOLDER, new Object[]{0, new Date()});
					((Map<String, Map<String, Object[]>>) userCacheMap).put(VERICITE_CACHE_PLACEHOLDER, cacheMap);								
					contentScoreCache.put(assignment, userCacheMap);
				}
			}
		}

		Optional<ContentReviewItem> reviewItem = crqs.getQueuedItem(getProviderId(), contentId);
		if(score == null){
			//nothing was found, throw exception for this contentId
			//remove from queue so that we can start again.
			if(needsRequeue(reviewItem)){
				if(reviewItem.isPresent()){
					//in order to requeue, we need to delete the old queue:
					crqs.removeFromQueue(getProviderId(), contentId);
				}
				throw new QueueException("No report was found for contentId: " + contentId);
			}else{
				return -1;
			}
		}else{
			//update the score in the db so admins can run reports:
			if(reviewItem.isPresent()){
				if(reviewItem.get().getReviewScore() == null || reviewItem.get().getReviewScore().intValue() != score.intValue()){
					//score has changed, update it and save it in the db
					reviewItem.get().setReviewScore(score);
					crqs.update(reviewItem.get());
				}
			}
			//score wasn't null and there should have only been one score, so just return that value
			return score;
		}
	}
	
	private boolean needsRequeue(Optional<ContentReviewItem> reviewItem){
		boolean requeue = false;
		if(reviewItem.isPresent()){
			//see whether the queue is old or reties failed:
			Date submitted = reviewItem.get().getDateSubmitted();
			if(submitted == null){
				//check if there is another date
				submitted = reviewItem.get().getDateQueued();
			}
			if(submitted != null){
				//see how long it has been since queue status
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR, -2);
				//if it has been over 3 hours, try again
				if(cal.getTime().after(submitted)){
					requeue = true;
				}
			}else{
				//something is weird here since dates are missing, just requeue:
				requeue = true;
			}
		}else{
			//there is no queue item, so create one!
			requeue = true;
		}
		return requeue;
	}

	public Long getReviewStatus(String contentId) throws QueueException {
		return crqs.getReviewStatus(getProviderId(), contentId);
	}

	public String getServiceName() {
		return SERVICE_NAME;
	}

	public boolean isAcceptableContent(ContentResource arg0) {
		return true;
	}

	public boolean isSiteAcceptable(Site arg0) {
		return true;
	}

	public void processQueue() {
		log.info("Processing VeriCite submission queue");
		int errors = 0;
		int success = 0;
		DefaultApi veriCiteApi = getVeriCiteAPI();
		Optional<ContentReviewItem> nextItem = null;
		while ((nextItem = crqs.getNextItemInQueueToSubmit(getProviderId())).isPresent()) {
			ContentReviewItem item = nextItem.get();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, VERICITY_RETRY_TIME_MINS);
			if (item.getRetryCount() == null) {
				item.setRetryCount(Long.valueOf(0));
				item.setNextRetryTime(cal.getTime());
				crqs.update(item);
			} else if (item.getRetryCount().intValue() > VERICITE_MAX_RETRY) {
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
			String userFirstNameParam = null;
			String userLastNameParam = null;
			String userEmailParam = null;
			try {
				User user = userDirectoryService.getUser(item.getUserId());
				userFirstNameParam = user.getFirstName();
				userLastNameParam = user.getLastName();
				userEmailParam = user.getEmail();
			} catch (UserNotDefinedException e1) {
				//VC doesn't really require a user, so as long as the user ID exists then it will succeed
				log.error("User not found for item: " + item.getId() + ", user: " + item.getUserId(), e1);
			}
			//it doesn't matter, all users are learners in the Sakai Integration
			final String userRoleParam = PARAM_USER_ROLE_LEARNER;
			ReportMetaData reportMetaData = new ReportMetaData();
			
			//get assignment title
			String assignmentTitle = getAssignmentTitle(item.getTaskId());
			if(assignmentTitle != null){
				reportMetaData.setAssignmentTitle(assignmentTitle);
			}
			//get site title
			try{
				Site site = siteService.getSite(item.getSiteId());
				if(site != null){
					reportMetaData.setContextTitle(site.getTitle());
				}
			}catch(Exception e){
				//no worries, just log it
				log.error("Site not found for item: " + item.getId() + ", site: " + item.getSiteId(), e);
			}
			reportMetaData.setUserEmail(userEmailParam);
			reportMetaData.setUserFirstName(userFirstNameParam);
			reportMetaData.setUserLastName(userLastNameParam);
			reportMetaData.setUserRole(userRoleParam);
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
			List<ExternalContentData> externalContentDataList = new ArrayList<ExternalContentData>();
			String externalContentId = getAttachmentId(resource.getId());
			ExternalContentData externalContentData = new ExternalContentData();
			externalContentData.setExternalContentID(externalContentId);
			String fileName = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			externalContentData.setFileName(FilenameUtils.getBaseName(fileName));
			externalContentData.setUploadContentType(FilenameUtils.getExtension(fileName));
			externalContentData.setUploadContentLength((int) resource.getContentLength());
			externalContentDataList.add(externalContentData);
			reportMetaData.setExternalContentData(externalContentDataList);

			List<ExternalContentUploadInfo> uploadInfo = null;
			try {
				String assignmentParam = getAssignmentId(item.getTaskId(), isA2(item.getContentId(), null));
				uploadInfo = veriCiteApi.reportsSubmitRequestContextIDAssignmentIDUserIDPost(item.getSiteId(), assignmentParam, item.getUserId(), consumer, consumerSecret, reportMetaData);
			} catch (ApiException e) {
				log.error(e.getMessage()  + ", id: " + item.getContentId(), e);
				item.setLastError(e.getMessage());
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			}
			//see if there are any attachment presigned urls to upload to (if VC already has the attachment, this will be empty)
			if(uploadInfo != null){
				//see if this attachment needs uploaded:
				for(ExternalContentUploadInfo info : uploadInfo){
					if(externalContentId.equals(info.getExternalContentId())){
							try {
								uploadExternalContent(info.getUrlPost(), resource.getContent());
							} catch (Exception e) {
								log.warn("ServerOverloadException: " + item.getContentId(), e);
								item.setLastError(e.getMessage());
								item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
								crqs.update(item);
								errors++;
								continue;
							}
					}
				}
			}
			//Success
			log.debug("Submission successful");
			item.setExternalId(externalContentId);
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

//	private void queue(final String userId, String siteId, final String assignmentReference, final List<FileSubmission> fileSubmissions){
//		/**
//		 * Example call:
//		 * userId: 124124124
//		 * siteId: 452351421
//		 * assignmentReference: /assignment/a/04bad844-493c-45a1-95b4-af70129d54d1/fa40eac1-5396-4a71-9951-d7d64b8a7710
//		 * contentId: /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
//		 */
//		
//		if(fileSubmissions != null && fileSubmissions.size() > 0){
//			final String contextParam = getSiteIdFromConentId(fileSubmissions.get(0).contentId);
//			final String assignmentParam = getAssignmentId(assignmentReference, isA2(fileSubmissions.get(0).contentId, null));
//			if(contextParam != null && assignmentParam != null){
//				User u;
//				try {
//					u = userDirectoryService.getUser(userId);
//					final String userFirstNameParam = u.getFirstName();
//					final String userLastNameParam = u.getLastName();
//					final String userEmailParam = u.getEmail();
//					//it doesn't matter, all users are learners in the Sakai Integration
//					final String userRoleParam = PARAM_USER_ROLE_LEARNER;
//
//					new Thread(){
//						public void run() {
//
//							DefaultApi veriCiteApi = getVeriCiteAPI();
//							ReportMetaData reportMetaData = new ReportMetaData();
//							//get assignment title
//							String assignmentTitle = getAssignmentTitle(assignmentReference);
//							if(assignmentTitle != null){
//								reportMetaData.setAssignmentTitle(assignmentTitle);
//							}
//							//get site title
//							try{
//								Site site = siteService.getSite(contextParam);
//								if(site != null){
//									reportMetaData.setContextTitle(site.getTitle());
//								}
//							}catch(Exception e){
//								//no worries								
//							}
//							reportMetaData.setUserEmail(userEmailParam);
//							reportMetaData.setUserFirstName(userFirstNameParam);
//							reportMetaData.setUserLastName(userLastNameParam);
//							reportMetaData.setUserRole(userRoleParam);
//							List<ExternalContentData> externalContentDataList = new ArrayList<ExternalContentData>();
//							if(fileSubmissions != null){
//								for(FileSubmission f : fileSubmissions){						
//									ExternalContentData externalContentData = new ExternalContentData();
//									externalContentData.setExternalContentID(f.contentId);
//									externalContentData.setFileName(FilenameUtils.getBaseName(f.fileName));
//									externalContentData.setUploadContentType(FilenameUtils.getExtension(f.fileName));
//									externalContentData.setUploadContentLength((int) f.contentLength);
//									externalContentDataList.add(externalContentData);
//								}
//							}
//							reportMetaData.setExternalContentData(externalContentDataList);
//
//							List<ExternalContentUploadInfo> uploadInfo = null;
//							try {
//								uploadInfo = veriCiteApi.reportsSubmitRequestContextIDAssignmentIDUserIDPost(contextParam, assignmentParam, userId, consumer, consumerSecret, reportMetaData);
//							} catch (ApiException e) {
//								log.error(e.getMessage(), e);
//							}
//							//see if there are any attachment presigned urls to upload to
//							if(uploadInfo != null){
//								//see if this attachment needs uploaded:
//								for(ExternalContentUploadInfo info : uploadInfo){
//									if(fileSubmissions != null){
//										for(FileSubmission f : fileSubmissions){
//											if(f.contentId.equals(info.getExternalContentId())){
//												uploadExternalContent(info.getUrlPost(), f.data);
//												break;
//											}
//										}
//									}
//								}
//							}
//						}
//					}.start();
//				} catch (UserNotDefinedException e) {
//					log.error(e.getMessage(), e);
//				}
//			}
//		}
//	}

	public void removeFromQueue(String contentId) {
		crqs.removeFromQueue(getProviderId(), contentId);
	}

	public void resetUserDetailsLockedItems(String arg0) {
		// TODO Auto-generated method stub
		
	}
	

	public String getReviewError(String contentId){
		return null;
	}

	public static String getContent(HttpResponse response) throws IOException {
	    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	    String body = "";
	    String content = "";

	    while ((body = rd.readLine()) != null) 
	    {
	        content += body + "\n";
	    }
	    return content.trim();
	}
		
	private String getAssignmentTitle(String assignmentRef){
		if(assignmentTitleCache.containsKey(assignmentRef)){
			return (String) assignmentTitleCache.get(assignmentRef);
		}else{
			String assignmentTitle = null;
			if (assignmentRef.startsWith("/assignment/")) {
				try {
					Reference ref = entityManager.newReference(assignmentRef);
					EntityProducer ep = ref.getEntityProducer();
					Entity ent = ep.getEntity(ref);
					if(ent != null){
						assignmentTitle = URLDecoder.decode(ent.getClass().getMethod("getTitle").invoke(ent).toString(),"UTF-8");
						assignmentTitleCache.put(assignmentRef, assignmentTitle);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			return assignmentTitle;
		}
	}
		
	private class FileSubmission{
		public String contentId;
		public byte[] data;
		public String fileName;
		public long contentLength = 0;
		
		public FileSubmission(String contentId, String fileName, byte[] data, long contentLength){
			this.contentId = contentId;
			this.fileName = fileName;
			this.data = data;
			this.contentLength = contentLength;
		}
	}

	private boolean isA2(String contentId, String assignmentRef){
		if(contentId != null && contentId.contains("/Assignment2/")){
			return true;
		}
		if(assignmentRef != null && assignmentRef.startsWith("/asnn2contentreview/")){
			return true;
		}
		return false;
	}
	
	private String getSiteIdFromConentId(String contentId){
		//contentId: /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
		if(contentId != null){
			String[] split = contentId.split("/");
			if(split.length > 2){
				return split[2];
			}
		}
		return null;
	}
	
	private String getAssignmentId(String assignmentRef, boolean isA2){
		if(assignmentRef != null){
			String[] split = assignmentRef.split("/");
			if(isA2){
				if(split.length > 2){
					return split[2];
				}
			}else{
				if(split.length > 4){
					return split[4];
				}
			}
		}
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
	
	private DefaultApi getVeriCiteAPI(){
		ApiClient apiClient = new ApiClient();
		String apiUrl = serviceUrl;
		if(StringUtils.isEmpty(apiUrl) || !apiUrl.endsWith("/")){
			apiUrl += "/";
		}
		apiUrl += VERICITE_API_VERSION;
		apiClient.setBasePath(apiUrl);
		return new DefaultApi(apiClient);
	}
	
	private String getAssignmentAttachmentId(String consumer, String contextId, String assignmentId, String attachmentId){
		return "/" + consumer + "/" + contextId + "/" + assignmentId + "/" + attachmentId;
	}
	
	private String getAttachmentId(String resourceid){
		return "/" + consumer + resourceid;
	}
	
	private void uploadExternalContent(String urlString, byte[] data){
		URL url = null;
		HttpURLConnection connection = null;
		DataOutputStream out = null;
		try {
			url = new URL(urlString);

			connection=(HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			out = new DataOutputStream(connection.getOutputStream());
			out.write(data);
			out.close();
			int responseCode = connection.getResponseCode();
			if(responseCode < 200 || responseCode >= 300){
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

	public MemoryService getMemoryService() {
		return memoryService;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
}
