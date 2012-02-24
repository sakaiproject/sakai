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
package org.sakaiproject.coursemanagement.impl;

import java.io.Serializable;
import java.util.Set;

/**
 * Models a cross listing between two CrossListableCmImpl entities.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CrossListingCmImpl extends AbstractPersistentCourseManagementObjectCmImpl
	implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * The set of canonicalCourses that are associated together in this CrossListingCmImpl
	 */
	private Set canonicalCourses;

	/**
	 * The set of courseOfferings that are associated together in this CrossListingCmImpl
	 */
	private Set courseOfferings;

	/**
	 * Whether this CrossListingCmImpl is defined by the enterprise
	 */
	private boolean enterpriseManaged;

	public Set getCanonicalCourses() {
		return canonicalCourses;
	}
	public void setCanonicalCourses(Set canonicalCourses) {
		this.canonicalCourses = canonicalCourses;
	}
	public Set getCourseOfferings() {
		return courseOfferings;
	}
	public void setCourseOfferings(Set courseOfferings) {
		this.courseOfferings = courseOfferings;
	}
	public boolean isEnterpriseManaged() {
		return enterpriseManaged;
	}
	public void setEnterpriseManaged(boolean enterpriseManaged) {
		this.enterpriseManaged = enterpriseManaged;
	}
}
