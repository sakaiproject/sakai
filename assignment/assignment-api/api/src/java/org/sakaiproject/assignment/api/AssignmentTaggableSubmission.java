/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.api;

import org.sakaiproject.assignment.taggable.api.TaggableItem;

/**
 * <p>
 * TaggableSubmission represents an assignment submission for an individual user.
 * Since ratable items require a single author per item, we create one of these
 * per submitter for each assignment submission.
 * </p>
 */
public interface AssignmentTaggableSubmission extends TaggableItem {

	/**
	 * Method to get the assignment submission for which this taggable submission
	 * was created.
	 * 
	 * @return The assignment submission.
	 */
	public AssignmentSubmission getAssignmentSubmission();
}
