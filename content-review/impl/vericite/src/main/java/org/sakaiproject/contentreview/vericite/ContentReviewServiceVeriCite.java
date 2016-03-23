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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import net.sf.json.JSONObject;

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

    private static final String PARAM_CONSUMER = "consumer";
	private static final String PARAM_CONSUMER_SECRET = "consumerSecret";
	private static final String PARAM_TOKEN = "token";
	private static final String PARAM_USER_FIRST_NAME = "userFirstName";
	private static final String PARAM_USER_LAST_NAME = "userLastName";
	private static final String PARAM_USER_EMAIL = "userEmail";
	private static final String PARAM_USER_ROLE = "userRole";
	private static final String PARAM_USER_ROLE_INSTRUCTOR = "Instructor";
	private static final String PARAM_USER_ROLE_LEARNER = "Learner";
	private static final String PARAM_CONTEXT_TITLE = "contextTitle";
	private static final String PARAM_VIEW_REPORT = "viewReport";
	private static final String PARAM_TOKEN_REQUEST = "tokenRequest";
	private static final String PARAM_FILE_DATA = "filedata";
	private static final String PARAM_EXTERNAL_CONTENT_ID = "externalContentId";
	private static final String PARAM_ASSIGNMENT_TITLE = "assignmentTitle";
	private static final String PARAM_ASSIGNMENT_INSTRUCTIONS = "assignmentInstructions";
	private static final String PARAM_ASSIGNMENT_ATTACHMENT_DATA = "assignmentAttachmentData";
	private static final String PARAM_ASSIGNMENT_ATTACHMENT_EXTERNAL_ID = "assignmentAttachmentExternalId";
	private static final String PARAM_ASSIGNMENT_EXCLUDE_QUOTES = "assignmentExcludeQuotes";
	private static final String PARAM_UPDATE_ASSIGNMNET_DETAILS = "updateAssignmentDetails";
	private static final String ASN1_GRADE_PERM = "asn.grade";
	private static final String SERVICE_NAME = "VeriCite";

	private String serviceUrl;
	private String consumer;
	private String consumerSecret;
	
	//Caches token requests for instructors so that we don't have to send a request for every student
	// ContextId -> Object{token, date}
	private Map<String, Object[]> instructorSiteTokenCache = new HashMap<String, Object[]>();
	private static final int CACHE_EXPIRE_MINS = 20;
	
	//Caches the content review item scores
	// Assignment -> {user -> {contentId - > Object{score, date}}}
	private Map<String, Map<String, Map<String, Object[]>>> contentScoreCache = new HashMap<String, Map<String, Map<String, Object[]>>>();
	private Map<String, String> assignmentTitleCache = new HashMap<String, String>();
	private static final int CONTENT_SCORE_CACHE_MINS = 5;
	
	public void init(){
		serviceUrl = serverConfigurationService.getString("vericite.serviceUrl", "");
		consumer = serverConfigurationService.getString("vericite.consumer", "");
		consumerSecret = serverConfigurationService.getString("vericite.consumerSecret", "");
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
					if(assignmentId != null){
						HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
						HttpPost post = new HttpPost(generateUrl(contextId, assignmentId, null));
						MultipartEntityBuilder builder = MultipartEntityBuilder.create();
						builder.setCharset(Charset.forName("UTF-8"));
						builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
						try {
							builder.addTextBody(PARAM_CONSUMER, URLEncoder.encode(consumer, "UTF-8"));

							builder.addTextBody(PARAM_CONSUMER_SECRET, URLEncoder.encode(consumerSecret, "UTF-8"));
							builder.addTextBody(PARAM_UPDATE_ASSIGNMNET_DETAILS, URLEncoder.encode("true", "UTF-8"));
							if(opts != null){
								if(opts.containsKey("title")){
									builder.addTextBody(PARAM_ASSIGNMENT_TITLE, URLEncoder.encode(opts.get("title").toString(), "UTF-8"));
								}else if(!isA2){
									//we can find the title from the assignment ref for A1
									String assignmentTitle = getAssignmentTitle(assignmentRef);
									if(assignmentTitle != null){
										builder.addTextBody(PARAM_ASSIGNMENT_TITLE, URLEncoder.encode(assignmentTitle, "UTF-8"));
									}
								}
								if(opts.containsKey("instructions")){
									try {
										builder.addTextBody(PARAM_ASSIGNMENT_INSTRUCTIONS, URLEncoder.encode(opts.get("instructions").toString(), "UTF-8"));
									} catch (UnsupportedEncodingException e) {
										log.error(e.getMessage(), e);
									}
								}
								if(opts.containsKey("exclude_quoted")){
									try {
										builder.addTextBody(PARAM_ASSIGNMENT_EXCLUDE_QUOTES, URLEncoder.encode(opts.get("exclude_quoted").toString(), "UTF-8"));
									} catch (UnsupportedEncodingException e) {
										log.error(e.getMessage(), e);
									}
								}
								if(opts.containsKey("attachments") && opts.get("attachments") instanceof List){
									int i = 1;
									SecurityAdvisor yesMan = new SecurityAdvisor(){
										public SecurityAdvice isAllowed(String arg0, String arg1, String arg2) {
											return SecurityAdvice.ALLOWED;
										}
									};
									securityService.pushAdvisor(yesMan);
									try{
										for(String refStr : (List<String>) opts.get("attachments")){
											try {
												Reference ref = entityManager.newReference(refStr);
												ContentResource res = (ContentResource) ref.getEntity();
												if(res != null){
													String fileName = res.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
													ContentBody bin = new ByteArrayBody(res.getContent(), fileName);
													builder.addPart(PARAM_ASSIGNMENT_ATTACHMENT_DATA + i, bin);
													builder.addTextBody(PARAM_ASSIGNMENT_ATTACHMENT_EXTERNAL_ID + i, URLEncoder.encode(res.getId(), "UTF-8"));
													i++;
												}
											} catch (Exception e){
												log.error(e.getMessage(), e);
											}
										}
									}catch(Exception e){
										log.error(e.getMessage(), e);
									}finally{
										securityService.popAdvisor(yesMan);
									}
								}
							}
							final HttpEntity entity = builder.build();
							post.setEntity(entity);
							try {
								HttpResponse response = client.execute(post);
							} catch (ClientProtocolException e) {
								log.error(e.getMessage(), e);
							} catch (IOException e) {
								log.error(e.getMessage(), e);
							}
						} catch (UnsupportedEncodingException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}.start();
	}

	public List<ContentReviewItem> getAllContentReviewItems(String arg0,
			String arg1) throws QueueException, SubmissionException,
			ReportException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getAssignment(String arg0, String arg1)
			throws SubmissionException, TransientSubmissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDateQueued(String arg0) throws QueueException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDateSubmitted(String arg0) throws QueueException,
			SubmissionException {
		// TODO Auto-generated method stub
		return null;
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
			String assignmentId = getAssignmentId(assignmentRef, isA2(contentId, assignmentRef));
			Map<String, String> params = new HashMap<String, String>();
			params.put(PARAM_CONSUMER, consumer);
			String token = null;
			if(instructor){
				//see if token already exist and isn't expired (we'll expire it after 1 minute so the user has enough time to use the token)
				if(instructorSiteTokenCache.containsKey(context)){
					Object[] cacheItem = instructorSiteTokenCache.get(context);
					Calendar cal = Calendar.getInstance();
				    cal.setTime(new Date());
				    //subtract the exipre time (currently set to 20 while the plag token is set to 30, leaving 10 mins in worse case for instructor to use token)
				    cal.add(Calendar.MINUTE, CACHE_EXPIRE_MINS * -1);
				    if(((Date) cacheItem[1]).after(cal.getTime())){
				    	//token hasn't expired, use it
				    	token = (String) cacheItem[0];
				    }else{
				    	//token is expired, remove it
				    	instructorSiteTokenCache.remove(context);
				    }
				}
				//this is an instructor, give them instructor role when viewing a report 
				params.put(PARAM_USER_ROLE, PARAM_USER_ROLE_INSTRUCTOR); 
			}else{
				//if the user is not an instructor, make sure the content id is set for the token request and access url
				params.put(PARAM_EXTERNAL_CONTENT_ID, contentId);
				//make sure to set the token to expect the learner role only for non-instructors
				params.put(PARAM_USER_ROLE, PARAM_USER_ROLE_LEARNER);
			}
			
			if(token == null){
				//token wasn't cached, let's look it up
				params.put(PARAM_CONSUMER_SECRET, consumerSecret);
				params.put(PARAM_TOKEN_REQUEST, "true");
				JSONObject results;
				try {
					String url = "";
					if(instructor){
						//use a generic url (no assignment or user) since this is an instructor and we want any instructor in this site
						//to user this cached token for any assignment in this site
						url = generateUrl(context, null, null);
					}else{
						url = generateUrl(context, assignmentId, userId);
					}
					results = getResults(url, params);
					if(results != null){
						//check for error message:
						String errorMsg = getErrorMessage(results);
						if(errorMsg == null){
							token = results.getString(PARAM_TOKEN);
						}else{
							throw new ReportException(errorMsg);
						}
					}

				} catch (Exception e) {
					throw new ReportException(e.getMessage(), e);
				}
			}
			if(token != null){
				//if token doesn't already exist in the cache, store it and set the date
				if(instructor && !instructorSiteTokenCache.containsKey(context)){
					instructorSiteTokenCache.put(context, new Object[]{token, new Date()});
				}
				if(!instructor){
					//make sure the user role learner is passed in because the token expects this
					params.put(PARAM_USER_ROLE, PARAM_USER_ROLE_LEARNER);
				}
				//we have a request token instead of the secret so that a user can see it
				params.remove(PARAM_CONSUMER_SECRET);
				params.put(PARAM_TOKEN, token);
				//get rid of the request for the token
				params.remove(PARAM_TOKEN_REQUEST);
				//now tell the service we want to view the report
				params.put(PARAM_VIEW_REPORT, "true");
				//since we could have left this parameter out for the instructor, put it back 
				//in so we know what content the user wants to view
				params.put(PARAM_EXTERNAL_CONTENT_ID, contentId);
				String urlParameters = "";
				if(params != null){
					for(Entry<String, String> entry : params.entrySet()){
						if(!"".equals(urlParameters)){
							urlParameters += "&";
						}
						try {
							urlParameters += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							throw new ReportException(e);
						}
					}
				}
				//use a specific user and content access for this url so that VeriCite knows who is visitng and what they want to see
				String url = generateUrl(context, assignmentId, userId);
				return url + "?" + urlParameters;
			}
		}
		//shouldn't get here is all went well:
		throw new ReportException("Token was null or contentId wasn't correct: " + contentId);
	}

	public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException, Exception {
		/**
		 * contentId: /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
		 * assignmentRef: /assignment/a/f7d8c921-7d5a-4116-8781-9b61a7c92c43/cbb993da-ea12-4e74-bab1-20d16185a655
		 */
		
		//first check if contentId already exists in cache:
		boolean isA2 = isA2(contentId, null);
		String context = getSiteIdFromConentId(contentId);
		Integer score = null;
		String assignment = getAssignmentId(assignmentRef, isA2);
		if(assignment != null){
			if(contentScoreCache.containsKey(assignment) 
					&& contentScoreCache.get(assignment).containsKey(userId) 
					&& contentScoreCache.get(assignment).get(userId).containsKey(contentId)){
				Object[] cacheItem = contentScoreCache.get(assignment).get(userId).get(contentId);
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				//subtract the exipre time
				cal.add(Calendar.MINUTE, CONTENT_SCORE_CACHE_MINS * -1);
				if(((Date) cacheItem[1]).after(cal.getTime())){
					//token hasn't expired, use it
					score = (Integer) cacheItem[0];
				}else{
					//token is expired, remove it
					contentScoreCache.get(assignment).remove(userId);
				}

			}
		}
		
		if(score == null){
			//wasn't in cache
			if(context != null){
				Map<String, String> params = new HashMap<String, String>();
				params.put(PARAM_CONSUMER, consumer);
				params.put(PARAM_CONSUMER_SECRET, consumerSecret);
				if(assignmentRef == null){
					params.put(PARAM_EXTERNAL_CONTENT_ID, contentId);
				}else if(assignment != null){
					String assignmentTitle = getAssignmentTitle(assignmentRef);
					if(assignmentTitle != null){
						params.put(PARAM_ASSIGNMENT_TITLE, assignmentTitle);
					}
				}
				//returns a map of {userId -> {assignmentId -> {contentId -> score}}} for all users
				JSONObject results = getResults(generateUrl(context, assignment, null), params);
				if(results != null){
					String errorMsg = getErrorMessage(results);
					if(errorMsg != null){
						throw new ReportException(errorMsg);
					}
					for (Iterator iterator = results.keys(); iterator.hasNext();) {
						String userIdKey = (String) iterator.next();
						JSONObject userAssignments = results.getJSONObject(userIdKey);
						if(userAssignments != null){
							for (Iterator iterator2 = userAssignments.keys(); iterator2.hasNext();) {
								String assignmentId = (String) iterator2.next();
								JSONObject userPapers = userAssignments.getJSONObject(assignmentId);
								for(Iterator iterator3 = userPapers.keys(); iterator3.hasNext();){
									String paperContentId = (String) iterator3.next();
									if(contentId.equals(paperContentId)){
										score = userPapers.getInt(paperContentId);
									}
									Map<String, Map<String, Object[]>> userCacheMap = contentScoreCache.get(assignmentId);
									if(userCacheMap == null){
										userCacheMap = new HashMap<String, Map<String, Object[]>>();
									}
									Map<String, Object[]> cacheMap = userCacheMap.get(userIdKey);
									if(cacheMap == null){
										cacheMap = new HashMap<String, Object[]>();
									}
									cacheMap.put(paperContentId, new Object[]{userPapers.getInt(paperContentId), new Date()});
									userCacheMap.put(userIdKey, cacheMap);								
									contentScoreCache.put(assignmentId, userCacheMap);
								}
							}
						}
					}
				}
				if(score == null){
					//nothing was found, throw exception for this contentId
					throw new QueueException("No report was found for contentId: " + contentId);
				}else{
					if(assignmentRef == null){
						//score wasn't null and there should have only been one score, so just return that value
						return score;
					}else{
						//grab the score from the map if it exists, if not, then there could have been an error:
						if(contentScoreCache.containsKey(assignment) && contentScoreCache.get(assignment).containsKey(userId)
								&& contentScoreCache.get(assignment).get(userId).containsKey(contentId)){
							return (Integer) contentScoreCache.get(assignment).get(userId).get(contentId)[0];
						}else{
							throw new QueueException("No report was found for contentId: " + contentId);		
						}
					}
				}
			}else{
				//content id is bad
				throw new ReportException("Couldn't find report score for contentId: " + contentId);
			}
		}else{
			return score;
		}
	}

	public Long getReviewStatus(String contentId) throws QueueException {
		//dont worry about implementing this, our status is always ready
		return null;
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
		// TODO Auto-generated method stub
		
	}
	
	public void queueContent(String userId, String siteId, String assignmentReference, List<ContentResource> content) throws QueueException{
		List<FileSubmission> fileSubmissions = new ArrayList<FileSubmission>();
		if(content != null){
			for(ContentResource res : content){
				try {
					if(res != null){
						if(userId == null || "".equals(userId.trim())){
							userId = res.getProperties().getProperty(ResourceProperties.PROP_CREATOR);
						}
						String fileName = res.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						fileSubmissions.add(new FileSubmission(res.getId(), fileName, res.getContent()));
					}
				}catch(Exception e){
					throw new QueueException(e);
				}
			}
		}
		if(fileSubmissions.size() > 0){
			queue(userId, siteId, assignmentReference, fileSubmissions);
		}
	}

	private void queue(final String userId, String siteId, final String assignmentReference, final List<FileSubmission> fileSubmissions){
		/**
		 * Example call:
		 * userId: 124124124
		 * siteId: 452351421
		 * assignmentReference: /assignment/a/04bad844-493c-45a1-95b4-af70129d54d1/fa40eac1-5396-4a71-9951-d7d64b8a7710
		 * contentId: /attachment/04bad844-493c-45a1-95b4-af70129d54d1/Assignments/b9872422-fb24-4f85-abf5-2fe0e069b251/plag.docx
		 */
		
		if(fileSubmissions != null && fileSubmissions.size() > 0){
				final String contextParam = getSiteIdFromConentId(fileSubmissions.get(0).contentId);
				final String assignmentParam = getAssignmentId(assignmentReference, isA2(fileSubmissions.get(0).contentId, null));
				if(contextParam != null && assignmentParam != null){
					User u;
					try {
						u = userDirectoryService.getUser(userId);
						final String userFirstNameParam = u.getFirstName();
						final String userLastNameParam = u.getLastName();
						final String userEmailParam = u.getEmail();
						//it doesn't matter, all users are learners in the Sakai Integration
						final String userRoleParam = PARAM_USER_ROLE_LEARNER;

						new Thread(){
							public void run() {
								HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
								HttpPost post = new HttpPost(generateUrl(contextParam, assignmentParam, userId));
								try {
									MultipartEntityBuilder builder = MultipartEntityBuilder.create();      
									builder.setCharset(Charset.forName("UTF-8"));
									builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
									builder.addTextBody(PARAM_CONSUMER, URLEncoder.encode(consumer, "UTF-8"));
									builder.addTextBody(PARAM_CONSUMER_SECRET, URLEncoder.encode(consumerSecret, "UTF-8"));
									builder.addTextBody(PARAM_USER_FIRST_NAME, URLEncoder.encode(userFirstNameParam, "UTF-8"));
									builder.addTextBody(PARAM_USER_LAST_NAME, URLEncoder.encode(userLastNameParam, "UTF-8"));
									builder.addTextBody(PARAM_USER_EMAIL, URLEncoder.encode(userEmailParam, "UTF-8"));
									builder.addTextBody(PARAM_USER_ROLE, URLEncoder.encode(userRoleParam, "UTF-8"));
									String assignmentTitle = getAssignmentTitle(assignmentReference);
									if(assignmentTitle != null){
										builder.addTextBody(PARAM_ASSIGNMENT_TITLE, URLEncoder.encode(assignmentTitle, "UTF-8"));
									}
									if(fileSubmissions != null){
										int i = 1;
										for(FileSubmission f : fileSubmissions){
											ContentBody bin = new ByteArrayBody(f.data, f.fileName);
											builder.addPart(PARAM_FILE_DATA + i, bin);
											builder.addTextBody(PARAM_EXTERNAL_CONTENT_ID + i, URLEncoder.encode(f.contentId, "UTF-8"));
											i++;
										}
									}
									
									final HttpEntity entity = builder.build();
									post.setEntity(entity);
									HttpResponse response = client.execute(post);
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}
							}
						}.start();
					} catch (UserNotDefinedException e) {
						log.error(e.getMessage(), e);
					}
				}
		}
		
	}

	public void removeFromQueue(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void resetUserDetailsLockedItems(String arg0) {
		// TODO Auto-generated method stub
		
	}
	

	public String getReviewError(String contentId){
		return null;
	}
	
	/**
	 * returns a map of {User => Score}
	 * @return
	 */
	public JSONObject getResults(String url, Map<String, String> params) throws Exception{

		HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
		HttpPost post = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		for(Entry<String, String> entry : params.entrySet()){
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			HttpResponse response = client.execute(post);
			return JSONObject.fromObject(getContent(response));
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	private String generateUrl(String context, String assignment, String user){
		String url = serviceUrl;
		if(!serviceUrl.endsWith("/")){
			serviceUrl += "/";
		}
		if(context != null){
			url += context + "/";
		}
		if(assignment != null){
			url += assignment + "/";
		}
		if(user != null){
			url += user + "/";
		}
		
		return url;
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
			return assignmentTitleCache.get(assignmentRef);
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
		
		public FileSubmission(String contentId, String fileName, byte[] data){
			this.contentId = contentId;
			this.fileName = fileName;
			this.data = data;
		}
	}
	
	/**
	 * return null if no error was found
	 * @param results
	 * @return
	 */
	public String getErrorMessage(JSONObject results){
		if(results != null && results.containsKey("result") && "failed".equals(results.get("result"))){
			if(results.containsKey("message")){
				return results.getString("message");
			}else{
				return "An error has occurred.";
			}
		}
		return null;
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
}
