/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.coursemanagement.api;

import java.util.Date;

/**
 * The official relationship of a student to something that gets a final grade
 * (or equivalent).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface Enrollment {
	public String getUserId();
	public void setUserId(String userId);

	public String getAuthority();
	public void setAuthority(String authority);
	
	public String getEnrollmentStatus();
	public void setEnrollmentStatus(String enrollmentStatus);
	
	public String getCredits();
	public void setCredits(String credits);
	
	public String getGradingScheme();
	public void setGradingScheme(String gradingScheme);
	
	public boolean isDropped();
	public void setDropped(boolean dropped);

	public Date getDropDate();
	public void setDropDate(Date dropDate);
}
