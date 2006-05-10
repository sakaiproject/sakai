/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.component.section;

import java.io.Serializable;

import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.LearningContext;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;

/**
 * A detachable EnrollmentRecord for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class EnrollmentRecordImpl extends ParticipationRecordImpl implements EnrollmentRecord, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String status;

	/**
	 * No-arg constructor needed for hibernate
	 */
	public EnrollmentRecordImpl() {		
	}
	
	public EnrollmentRecordImpl(LearningContext learningContext, String status, User user) {
		this.learningContext = learningContext;
		this.status = status;
		this.user = user;
	}

	public Role getRole() {
		return Role.STUDENT;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}

