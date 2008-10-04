/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006 Sakai Foundation, the MIT Corporation
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

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.Date;

/**
 * A log of grading activity.  A GradingEvent should be saved any time a grade
 * record is added or modified.  GradingEvents should be added when the entered
 * value of a course grade record is added or modified, but not when the
 * autocalculated value changes.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradingEvent implements Comparable, Serializable {
    private Long id;
    private String graderId;
    private String studentId;
    private GradableObject gradableObject;
    private String grade;
    private Date dateGraded;

    public GradingEvent() {
        this.dateGraded = new Date();
    }

    public GradingEvent(GradableObject gradableObject, String graderId, String studentId, Object grade) {
        this.gradableObject = gradableObject;
        this.graderId = graderId;
        this.studentId = studentId;
        if (grade != null) {
        	this.grade = grade.toString();
        }
        this.dateGraded = new Date();
    }

	/**
	 * @return Returns the dateGraded.
	 */
	public Date getDateGraded() {
		return dateGraded;
	}
	/**
	 * @param dateGraded The dateGraded to set.
	 */
	public void setDateGraded(Date dateGraded) {
		this.dateGraded = dateGraded;
	}
	/**
	 * @return Returns the gradableObject.
	 */
	public GradableObject getGradableObject() {
		return gradableObject;
	}
	/**
	 * @param gradableObject The gradableObject to set.
	 */
	public void setGradableObject(GradableObject gradableObject) {
		this.gradableObject = gradableObject;
	}
	/**
	 * @return Returns the grade.
	 */
	public String getGrade() {
		return grade;
	}
	/**
	 * @param grade The grade to set.
	 */
	public void setGrade(String grade) {
		this.grade = grade;
	}
	/**
	 * @return Returns the graderId.
	 */
	public String getGraderId() {
		return graderId;
	}
	/**
	 * @param graderId The graderId to set.
	 */
	public void setGraderId(String graderId) {
		this.graderId = graderId;
	}
	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return Returns the studentId.
	 */
	public String getStudentId() {
		return studentId;
	}
	/**
	 * @param studentId The studentId to set.
	 */
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return dateGraded.compareTo(((GradingEvent)o).dateGraded);
    }
}



