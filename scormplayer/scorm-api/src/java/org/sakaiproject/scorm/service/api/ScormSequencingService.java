/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.service.api;

import javax.swing.tree.TreeModel;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;

public interface ScormSequencingService
{
	/**
	 * Called to get the TreeModel object that represents the 'choice' tree of activities
	 */
	public TreeModel getTreeModel(SessionBean sessionBean);

	/**
	 * Indicates if the user is allowed to 'continue' to the next sco
	 */
	public boolean isContinueEnabled(SessionBean sessionBean);

	/**
	 * Indicates if the user is allowed to 'continue' to the next sco, even if it means exiting
	 * the session
	 */
	public boolean isContinueExitEnabled(SessionBean sessionBean);

	/**
	 * Indicates that backward targets (in terms of Activity Tree traversal) are not permitted
	 * (True or False) from the children of this
	 * activity.
	 */
	public boolean isControlForwardOnly(SessionBean sessionBean);

	/**
	 * Indicates that a Choice navigation request is permitted (True or False) to target the
	 * children of the activity.
	 */
	public boolean isControlModeChoice(SessionBean sessionBean);

	/** 
	 * Indicates whether the activity is permitted to terminate (True or False) if a Choice
	 * navigation request is processed.
	 */
	public boolean isControlModeChoiceExit(SessionBean sessionBean);

	/**
	 * Indicates the Flow Subprocess may be applied (True or False) to the children of this
	 * activity.
	 */
	public boolean isControlModeFlow(SessionBean sessionBean);

	/**
	 * Indicates if the user is allowed to return to the previous sco
	 */
	public boolean isPreviousEnabled(SessionBean sessionBean);

	/**
	 * Indicates if the user is allowed to resume a suspended session
	 */
	public boolean isResumeEnabled(SessionBean sessionBean);

	/**
	 * Indicates if the user is allowed to start a given session
	 */
	public boolean isStartEnabled(SessionBean sessionBean);

	/**
	 * Indicates if the user is allowed to suspend a given session
	 */
	public boolean isSuspendEnabled(SessionBean sessionBean);

	/**
	 * This navigate method is used for start, next, previous, suspend, and quit requests
	 */
	public String navigate(int request, SessionBean sessionBean, INavigable agent, Object target);

	/**
	 * This navigate method is used for 'choice' requests, that is, ones that arise from a user
	 * clicking on the tree of available activities
	 */
	public void navigate(String choiceRequest, SessionBean sessionBean, INavigable agent, Object target);

	/**
	 * This navigate method is almost identical to the one above, except that it uses a different
	 * identifier to determine which activity has been clicked on.
	 */
	public void navigateToActivity(String activityId, SessionBean sessionBean, INavigable agent, Object target);

	/**
	 * This method is called once at the beginning of each user session to provide the bean
	 * where all state information will be stored
	 */
	public SessionBean newSessionBean(ContentPackage contentPackage);
}
