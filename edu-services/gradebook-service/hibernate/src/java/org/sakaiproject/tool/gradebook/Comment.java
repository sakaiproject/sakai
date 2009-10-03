/*******************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation, The MIT Corporation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;
import java.io.Serializable;

/**
 * Comment Object
 *
 *
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Oct 20, 2006
 * Time: 10:56:34 AM
 */
public class Comment implements Serializable {

    private Long id;
    private String studentId;
    private String graderId;
    private int version;
    private Date dateRecorded;
    private String commentText;
    private GradableObject gradableObject;


    public Comment(String studentId, String comment, GradableObject gradableObject) {
        this.gradableObject = gradableObject;
        this.studentId = studentId;
        this.commentText = comment;
    }


    public Comment() {
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

    public String getGraderId() {
        return graderId;
    }

    public void setGraderId(String graderId) {
        this.graderId = graderId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getDateRecorded() {
        return dateRecorded;
    }

    public void setDateRecorded(Date dateRecorded) {
        this.dateRecorded = dateRecorded;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public GradableObject getGradableObject() {
        return gradableObject;
    }

    public void setGradableObject(GradableObject gradableObject) {
        this.gradableObject = gradableObject;
    }


    public String toString() {
        return new ToStringBuilder(this).
                append("id", id).
                append("grader", graderId).
                append("comment",commentText).
                append("studentid",studentId).toString();

    }

    public boolean equals(Object other) {
        if (!(other instanceof Comment)) {
            return false;
        }
        Comment comment = (Comment)other;
        return new EqualsBuilder()
            .append(gradableObject, comment.getGradableObject())
            .append(id, comment.getId())
            .append(commentText, comment.getCommentText()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
          append(gradableObject).
          append(id).
          append(commentText).
          toHashCode();
	}

}

