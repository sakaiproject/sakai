/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.User;

public class EnrollmentDecorator implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EnrollmentDecorator.class);

	protected EnrollmentRecord enrollment;

	// A Map of categoryId -> Enrolled Section
	protected Map categoryToSectionMap;
	
	public EnrollmentDecorator(EnrollmentRecord enrollment, Map categoryToSectionMap) {
		this.enrollment = enrollment;
		this.categoryToSectionMap = categoryToSectionMap;
	}
	
	public static final Comparator getNameComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				EnrollmentDecorator enr1 = (EnrollmentDecorator)o1;
				EnrollmentDecorator enr2 = (EnrollmentDecorator)o2;
				int comparison = enr1.getUser().getSortName().compareTo(enr2.getUser().getSortName());
				return sortAscending ? comparison : (-1 * comparison);
			}
		};
	}

	public static final Comparator getDisplayIdComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				EnrollmentDecorator enr1 = (EnrollmentDecorator)o1;
				EnrollmentDecorator enr2 = (EnrollmentDecorator)o2;
				int comparison = enr1.getUser().getDisplayId().compareTo(enr2.getUser().getDisplayId());
				return sortAscending ? comparison : (-1 * comparison);
			}
		};
	}

	public static final Comparator getCategoryComparator(final String categoryId, final boolean sortAscending) {
		if(log.isDebugEnabled()) log.debug("Comparing enrollment decorators by " + categoryId);
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				EnrollmentDecorator enr1 = (EnrollmentDecorator)o1;
				EnrollmentDecorator enr2 = (EnrollmentDecorator)o2;
				CourseSection section1 = (CourseSection)enr1.getCategoryToSectionMap().get(categoryId);
				CourseSection section2 = (CourseSection)enr2.getCategoryToSectionMap().get(categoryId);
				if(section1 == null && section2 != null) {
					return sortAscending ? -1 : 1;
				}
				if(section1 != null && section2 == null) {
					return sortAscending ? 1 : -1;
				}
				if(section1 == null && section2 == null) {
					return getNameComparator(sortAscending).compare(o1, o2);
				}
				
				int comparison = section1.getTitle().compareTo(section2.getTitle());
				return sortAscending ? comparison : (-1 * comparison);
			}
		};
	}
	
	public String getStatus() {
		return enrollment.getStatus();
	}

	public User getUser() {
		return enrollment.getUser();
	}
	
	public Map getCategoryToSectionMap() {
		return categoryToSectionMap;
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
