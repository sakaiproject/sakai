/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/postem/branches/sakai_2-4-x/postem-hbm/src/java/org/sakaiproject/component/app/postem/data/StudentGradesImpl.java $
 * $Id: StudentGradesImpl.java 17140 2006-10-16 17:40:49Z wagnermr@iupui.edu $
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
import org.sakaiproject.api.app.postem.data.Heading;


public class HeadingImpl implements Heading, Serializable {
	protected String headingTitle;
	protected Integer location;
	protected Long gradebookId;

	public HeadingImpl() {

	}
	
	public HeadingImpl(Long gradebookId, String headingTitle, Integer location) {
		this.gradebookId = gradebookId;
		this.headingTitle = headingTitle;
		this.location = location;
	}

	public Long getGradebookId() {
		return gradebookId;
	}

	public void setGradebookId(Long gradebookId) {
		this.gradebookId = gradebookId;
	}

	public String getHeadingTitle() {
		return headingTitle;
	}

	public void setHeadingTitle(String headingTitle) {
		this.headingTitle = headingTitle;
	}

	public Integer getLocation() {
		return location;
	}

	public void setLocation(Integer location) {
		this.location = location;
	}
	
	public boolean equals(Object other) {
        if (!(other instanceof Heading)) {
            return false;
        }
        Heading heading = (Heading)other;
        return new EqualsBuilder()
            .append(gradebookId, heading.getGradebookId())
            .append(location, heading.getLocation()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
          append(gradebookId).
          append(location).
          toHashCode();
	}

}
