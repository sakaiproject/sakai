/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.select;

import java.io.Serializable;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 *
 * Used to be org.navigoproject.ui.web.asi.select.SelectAssessmentForm.java
 */
public class SelectAssessmentBean
implements Serializable
{
	private List takeableAssessments;

	/** Use serialVersionUID for interoperability. */
	private final static long serialVersionUID = 7401578412639293693L;
	private List lateHandlingAssessments;
	private List reviewableAssessments;
	private List nonReviewableAssessments;
	private String reviewableSortOrder="title";
	private String takeableSortOrder ="title";
	private boolean takeableAscending = true;
	private boolean reviewableAscending = true;
	private org.sakaiproject.tool.assessment.ui.model.PagingModel reviewPager;
	private org.sakaiproject.tool.assessment.ui.model.PagingModel takePager;
	private boolean hasHighestMultipleSubmission = false;  // this is used to display the message on the bottom if there are any highest multiple submissions. 
	private boolean hasAnyAssessmentBeenModified = false;  // this is used to display the message on the bottom if there is any assessment been modified after submitted.
	private Boolean warnUserOfModification;
	private boolean hasAnyAssessmentRetractForEdit = false;  // this is used to display the message on the bottom if there is any assessment retracted for edit.
	private String displayAllAssessments = "2"; // display all
	private boolean hasAverageMultipleSubmissions=false;
	private String secureDeliveryHTMLFragments;
	private static final ServerConfigurationService serverConfigurationService= (ServerConfigurationService) ComponentManager.get( ServerConfigurationService.class );
	
	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @return ArrayLists of DeliveryBean objects
	 */
	public List getTakeableAssessments()
	{
		return takeableAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @param takeableAssessments ArrayLists of DeliveryBean objects
	 */
	public void setTakeableAssessments(List takeableAssessments)
	{
		this.takeableAssessments = takeableAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @return ArrayLists of DeliveryBean objects
	 */
	public List getLateHandlingAssessments()
	{
		return lateHandlingAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @param lateHandlingAssessments ArrayLists of DeliveryBean objects
	 */
	public void setLateHandlingAssessments(List lateHandlingAssessments)
	{
		this.lateHandlingAssessments = lateHandlingAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @return ArrayLists of DeliveryBean objects
	 */
	public List getReviewableAssessments()
	{
		return reviewableAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @param reviewAssessments ArrayLists should be lists of DeliveryBean objects
	 */
	public void setReviewableAssessments(List reviewableAssessments)
	{
		this.reviewableAssessments = reviewableAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @return ArrayLists of DeliveryBean objects
	 */
	public List getNonReviewableAssessments()
	{
		return this.nonReviewableAssessments;
	}

	/**
	 * ArrayLists should be lists of DeliveryBean objects
	 *
	 * @param nonReviewableAssessments ArrayLists should be lists of DeliveryBean objects
	 */
	public void setNonReviewableAssessments(List nonReviewableAssessments)
	{
		this.nonReviewableAssessments = nonReviewableAssessments;
	}

	////////////////////////////////////////////////////////////////
	// not used right now
	public org.sakaiproject.tool.assessment.ui.model.PagingModel getReviewPager() {
		return reviewPager;
	}
	public void setReviewPager(org.sakaiproject.tool.assessment.ui.model.PagingModel reviewPager) {
		this.reviewPager = reviewPager;
	}
	public org.sakaiproject.tool.assessment.ui.model.PagingModel getTakePager() {
		return takePager;
	}
	public void setTakePager(org.sakaiproject.tool.assessment.ui.model.PagingModel takePager) {
		this.takePager = takePager;
	}

	// sorting model
	/**
	 * sort order for review table
	 * @return
	 */
	public String getReviewableSortOrder()
	{
		return reviewableSortOrder;
	}

	/**
	 * ascending descending for review table
	 * @return
	 */
	public boolean isReviewableAscending()
	{
		return reviewableAscending;
	}


	/**
	 * sort for take assessment table
	 * @return
	 */


	public String getTakeableSortOrder()
	{
		return takeableSortOrder;
	}

	/**
	 * sort for take assessment table
	 * @param sort
	 */
	public void setTakeableSortOrder(String takeableSortOrder)
	{
		this.takeableSortOrder = takeableSortOrder;
	}

	/**
	 * sort for review assessment table
	 * @param sort
	 */
	public void setReviewableSortOrder(String reviewableSortOrder)
	{
		this.reviewableSortOrder = reviewableSortOrder;
	}
	/**
	 *
	 * @param reviewableAscending
	 */
	public void setReviewableAscending(boolean reviewableAscending)
	{
		this.reviewableAscending = reviewableAscending;
	}
	/**
	 * is takable table sorted in ascending order
	 * @return true if it is
	 */
	public boolean isTakeableAscending()
	{
		return takeableAscending;
	}

	/**
	 *
	 * @param takeableAscending is takable table sorted in ascending order
	 */
	public void setTakeableAscending(boolean takeableAscending)
	{
		this.takeableAscending = takeableAscending;
	}

	public boolean getIsThereAssessmentToTake()
	{
		if(takeableAssessments==null || takeableAssessments.size()==0)

			return false;
		else
			return true;
	}

	public boolean getIsThereAssessmentToReview()
	{

		if(reviewableAssessments==null || reviewableAssessments.size()==0)

			return false;
		else
			return true;
	}

	/**
	 * @return Returns the hasHighestMultipleSubmission.
	 */
	public boolean isHasHighestMultipleSubmission() {
		return hasHighestMultipleSubmission;
	}

	/**
	 * @param hasHighestMultipleSubmission The hasHighestMultipleSubmission to set.
	 */
	public void setHasHighestMultipleSubmission(boolean hasHighestMultipleSubmission) {
		this.hasHighestMultipleSubmission = hasHighestMultipleSubmission;
	}

	public boolean getHasAnyAssessmentBeenModified() {
		return hasAnyAssessmentBeenModified;
	}

	public void setHasAnyAssessmentBeenModified(boolean hasAnyAssessmentBeenModified) {
		this.hasAnyAssessmentBeenModified = hasAnyAssessmentBeenModified;
	}
	
	public boolean getHasAnyAssessmentRetractForEdit() {
		return hasAnyAssessmentRetractForEdit;
	}

	public void setHasAnyAssessmentRetractForEdit(boolean hasAnyAssessmentRetractForEdit) {
		this.hasAnyAssessmentRetractForEdit = hasAnyAssessmentRetractForEdit;
	}

	public String getDisplayAllAssessments() {
		return displayAllAssessments;
	}

	public void setDisplayAllAssessments(String displayAllAssessments) {
		this.displayAllAssessments = displayAllAssessments;
	}

	/**
	 * @return Returns the hasAvergeMultipleSubmission.
	 */
	public boolean isHasAverageMultipleSubmissions() {
		return hasAverageMultipleSubmissions;
	}

	/**
	 * @param Average tMultipleSubmission The hasAverageMultipleSubmission to set.
	 */
	public void setHasAverageMultipleSubmissions(boolean hasAverageMultipleSubmissions) {
		this.hasAverageMultipleSubmissions = hasAverageMultipleSubmissions;
	}

	public Boolean getWarnUserOfModification() {
		if(warnUserOfModification == null){
			warnUserOfModification = serverConfigurationService.getBoolean("samigo.SelectAssessmentBean.warnUserOfModification", true);
		}
		return warnUserOfModification;
	}

	public void setWarnUserOfModification(Boolean warnUserOfModification) {
		this.warnUserOfModification = warnUserOfModification;
	}       
	
	public String getSecureDeliveryHTMLFragments() {
		return secureDeliveryHTMLFragments;
	}
		  
	public void setSecureDeliveryHTMLFragments(String secureDeliveryHTMLFragments) {
		this.secureDeliveryHTMLFragments = secureDeliveryHTMLFragments;
	}
}
