/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
public class GradingEvent implements Comparable<Object>, Serializable {
    
	private static final long serialVersionUID = 1L;
	
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

	public Date getDateGraded() {
		return dateGraded;
	}
	
	public void setDateGraded(Date dateGraded) {
		this.dateGraded = dateGraded;
	}
	
	public GradableObject getGradableObject() {
		return gradableObject;
	}
	
	public void setGradableObject(GradableObject gradableObject) {
		this.gradableObject = gradableObject;
	}
	
	public String getGrade() {
		return grade;
	}
	
	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	public String getGraderId() {
		return graderId;
	}
	
	public void setGraderId(String graderId) {
		this.graderId = graderId;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getStudentId() {
		return studentId;
	}
	
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
	public int compareTo(Object o) {
        return dateGraded.compareTo(((GradingEvent)o).dateGraded);
    }
}



