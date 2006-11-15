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

import org.sakaiproject.section.api.coursemanagement.LearningContext;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;

/**
 * A detachable ParticipationRecord for persistent storage.  The original design for
 * Section Info did not include Groups, and consequently, the Role enumeration
 * doesn't fit well for groups.  This class uses a bit of an ugly hack to map the role
 * enumeration into a persistable int.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class GroupParticipantImpl extends ParticipationRecordImpl implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * No-arg constructor needed for hibernate
	 */
	public GroupParticipantImpl() {		
	}
	
	public GroupParticipantImpl(String uuid, LearningContext learningContext, User user) {
		this.uuid = uuid;
		this.learningContext = learningContext;
		this.user = user;
	}
	
	public Role getRole() {
		// Section Info doesn't care about group roles
		return null;
	}
}
