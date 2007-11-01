/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/postem/trunk/postem-hbm/src/java/org/sakaiproject/component/app/postem/data/StudentGradeDataImpl.java $
 * $Id: StudentGradeDataImpl.java 17140 2006-10-16 17:40:49Z wagnermr@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.app.postem.data;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.api.app.postem.data.StudentGradeData;


public class StudentGradeDataImpl implements StudentGradeData, Serializable {
	protected String gradeEntry;
	protected Integer location;
	protected Long studentId;

	public StudentGradeDataImpl() {

	}
	
	public StudentGradeDataImpl(Long studentId, String gradeEntry, Integer location) {
		this.studentId = studentId;
		this.gradeEntry = gradeEntry;
		this.location = location;
	}

	public boolean equals(Object other) {
        if (!(other instanceof StudentGradeData)) {
            return false;
        }
        StudentGradeData gradeData = (StudentGradeData)other;
        return new EqualsBuilder()
            .append(studentId, gradeData.getStudentId())
            .append(location, gradeData.getLocation()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
          append(studentId).
          append(location).
          toHashCode();
	}

	public String getGradeEntry() {
		return gradeEntry;
	}

	public void setGradeEntry(String gradeEntry) {
		this.gradeEntry = gradeEntry;
	}

	public Integer getLocation() {
		return location;
	}

	public void setLocation(Integer location) {
		this.location = location;
	}

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

}
