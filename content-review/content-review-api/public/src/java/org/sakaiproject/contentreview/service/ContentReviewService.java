/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.contentreview.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.site.api.Site;

import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.model.ContentReviewItem;

/**
 *  ContentReview Service manages submission to the Content review queue and retrieving reports from the service
 *  
 *  @author David Jaka, David Horwitz
 */
public interface ContentReviewService {
	
	/**
	 *  Add an item to the Queue for Submission to Turnitin
	 *  
	 *  @param userID if nulll current user is used
	 *  @param SiteId is null current site is used
	 *  @param Entity reference to the task this is for
	 *  @param Reference to the content object that should be submitted
	 *  
	 */
	public void queueContent(String userId, String siteId, String taskId, String contentId)
	throws QueueException;
	
	/**
	 *  Retrieve a score for a specific item
	 * @param contentId
	 * @return the origionality score
	 * @throws QueueException
	 * @throws ReportException
	 * @throws Exception
	  */
	public int getReviewScore(String contentId)
	throws QueueException, ReportException, Exception;
	
	/**
	 *  Get the URL of the report
	 * @param contentId
	 * @return the url
	 * @throws QueueException
	 * @throws ReportException
	 * * * @deprecated since Nov 2007, use {@link getReviewReportInstructor(String contentId)} or {@link getReviewReportInstructor(String contentId)}
	 */
	public String getReviewReport(String contentId)
	throws QueueException, ReportException;
	
	/**
	 * Get the URL of a report constructed for a student
	 * @param contentId
	 * @return
	 * @throws QueueException
	 * @throws ReportException
	 * */
	public String getReviewReportStudent(String contentId)
	throws QueueException, ReportException;
	
	/**
	 * Get the URL for a report constructed for an Instructor
	 * 
	 * @param contentId
	 * @return
	 * @throws QueueException
	 * @throws ReportException
	 */
	public String getReviewReportInstructor(String contentId)
	throws QueueException, ReportException;
	
	
	/**
	 * Get the status of a submission
	 * @param contentId
	 * @return
	 * @throws QueueException
	 */
	public Long getReviewStatus(String contentId)
	throws QueueException;
	
	/**
	 * The date an item was queued
	 * @param contextId
	 * @return
	 * @throws QueueException
	 */
	public Date getDateQueued(String contextId)
	throws QueueException;
	
	/**
	 * The date an item was submitted to the queue
	 * @param contextId
	 * @return
	 * @throws QueueException
	 * @throws SubmissionException
	 */
	public Date getDateSubmitted(String contextId)
	throws QueueException, SubmissionException;
	
	/**
	 *  Proccess all pending jobs in the Queue
	 */
	public void processQueue();
	
	/**
	 *  Check for reports for all submitted items that don't have reports yet 
	 */
	public void checkForReports();
	
	
	/**
	 *  Get a list of reports for a task
	 * @param siteId
	 * @param taskId
	 * @return
	 * @throws QueueException
	 * @throws SubmissionException
	 * @throws ReportException
	 */
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
	throws QueueException, SubmissionException, ReportException;
	
	
	/**
	 *  Get a list of reports for all tasks in a site
	 *  
	 * @param siteId
	 * @return
	 * @throws QueueException
	 * @throws SubmissionException
	 * @throws ReportException
	 */
	public List<ContentReviewItem> getReportList(String siteId)
	throws QueueException, SubmissionException, ReportException;
	
	/**
	 * This is a complement to getReportList, except that it returns all
	 * ContentReviewItems for a site and task, rather than just the ones 
	 * whose reports have been completed. 
	 * 
	 * This is the result of running into leaky abstraction problems while
	 * working on Assignments 2, namely that we need to make the pretty
	 * little color coded bars for an entire class for a given assignment,
	 * and if some of them had issues we need to present a fine grained 
	 * error message (such as, your paper was less than 2 paragraphs, or 
	 * your paper was the wrong file type). This requires another property
	 * method, but rather than add a getErrorCode(String contentId) method
	 * it's more efficient to add this so we can get the whole lot in one
	 * DB query, rather than lookup the special case failures.
	 * 
	 * @param siteId
	 * @param taskId
	 * @return
	 */
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
	throws QueueException, SubmissionException, ReportException;
	
	
	/**
	 * Return the Name of the Service Implementation for Display Purposes
	 * 
	 */
	public String getServiceName();
	
	/**
	 *  Reset the Items for a specific user that where locked because of incomplete user details
	 * @param userId
	 */
	
	public void resetUserDetailsLockedItems(String userId);
	
	/**
	 * Is the content resource of an type that can be accepted by the service implementation
	 * @param resource
	 * @return
	 */
	public boolean isAcceptableContent(ContentResource resource);
	
	/**
	 *  Can this site make use of the content review service
	 * 
	 * @param site
	 * @return
	 * 
	 */
	public boolean isSiteAcceptable(Site site);
	
	/**
	 *  Get a icon URL that for a specific score
	 * @param score
	 * @return
	 */
	public String getIconUrlforScore(Long score);
	
	/**
	 *  Does the service support resubmissions?
	 * @return
	 */
	public boolean allowResubmission();
	
	/**
	 *  Remove an item from the review Queue
	 * @param ContentId
	 */
	public void removeFromQueue(String ContentId);
	
	/**
	 * Get a status message for a submission in the locale of the specified user
	 * @param messageCode
	 * @param userRef
	 * @return
	 */
	public String getLocalizedStatusMessage(String messageCode, String userRef);
	
	/**
	 * Get a status message for a submission in the locale of the current user
	 * @param messageCode
	 * @return
	 */
	public String getLocalizedStatusMessage(String messageCode);
	
	/**
	 * Get a error report for a Specific method
	 * @param contentId
	 * @return
	 * @deprecated use {@link #getLocalizedStatusMessage(String)}
	 */
	public String getReviewError(String contentId);
	/**
	 * Get a status message for a submission in the locale specified
	 * @param messageCode
	 * @param locale
	 * @return
	 */
	public String getLocalizedStatusMessage(String messageCode, Locale locale);
	
	/**
	 * This is a vendor specific method to allow getting information about
	 * a particular assignment in an external plagiarism checking system.
	 * The method returns a Map of keys and properties since they may differ
	 * between implementations.
	 * 
	 * In the Turnitin implementation this provides all the return information
	 * that comes over the wire from their Fid4 Fcmd7 function which can 
	 * be referenced from their API Documentation.
	 * 
	 * This method may be necessary for deeper integrations (A2), but could
	 * tie your code to a particular implementation.
	 * 
	 * @param siteId
	 * @param taskId
	 * @return
	 * @throws SubmissionException
         * @throws TransientSubmissionException
	 */
	public Map getAssignment(String siteId, String taskId)
	throws SubmissionException, TransientSubmissionException;
	
	/**
	 * This is a vendor specific method needed for some deep integrations
	 * (such as A2) to pre provision assignments on an external content
	 * checking system.  The method takes in a Map which can take varying
	 * keys and values depending on implementation.
	 * 
	 * For the Turnitin implementation these keys map to some input 
	 * parameters for Fid4 Fcmd 2/3. These can be seen in Turnitin's API
	 * documentation.
	 * 
	 * Using this method will likely tie you to a particular Content Review
	 * implementation.
	 * 
	 * @param siteId
	 * @param taskId
	 * @param extraAsnnOpts
	 * @throws SubmissionException
	 * @throws TransientSubmissionException
	 */
	public void createAssignment(String siteId, String taskId, Map extraAsnnOpts)
	throws SubmissionException, TransientSubmissionException;
}
