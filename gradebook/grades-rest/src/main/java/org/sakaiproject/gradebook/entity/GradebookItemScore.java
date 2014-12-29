/**
 * Copyright 2013 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.gradebook.entity;

import java.util.Date;

/**
 * This represents an individual score in a gradebook grade item for a user
 * Allows for better control over the data being input and output (than the core Sakai classes)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)
 */
public class GradebookItemScore {
    public String id;
    public String itemId;
    public String userId;
    public String username;
    public String graderUserId;
    public Date recorded;
    public String grade;
    public String comment;

    /**
     * Used to indicate the failure that occurred when saving this score (null if no error)
     */
    public String error;

    protected GradebookItemScore() {}
    public GradebookItemScore(String itemId, String userId, String grade) {
        this(itemId, userId, grade, null, null, null, null);
    }
    public GradebookItemScore(String itemId, String userId, String grade, String username,
            String graderUserId, Date recorded, String comment) {
        if (itemId == null || "".equals(itemId)) {
            throw new IllegalArgumentException("itemName must be set");
        }
        if (grade == null || "".equals(grade)) {
            throw new IllegalArgumentException("grade must be set");
        }
        if ( (userId == null || "".equals(userId))
                && (username == null || "".equals(username) ) ) {
            throw new IllegalArgumentException("userId or username must be set");
        }
        assignId(itemId, userId != null ? userId : username);
        this.username = username;
        this.graderUserId = graderUserId;
        if (recorded == null) {
            this.recorded = new Date();
        } else {
            this.recorded = new Date(recorded.getTime());
        }
        this.grade = grade;
        this.comment = comment;
    }

    public void assignId(Long itemId, String userId) {
        assignId( (itemId != null ? itemId.toString() : "00"), userId); // avoid NPE
    }

    public void assignId(String itemName, String userId) {
        this.id = itemName+":"+userId;
        this.itemId = itemName;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "("+id+":"+username+":"+grade+":"+recorded+")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((grade == null) ? 0 : grade.hashCode());
        result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GradebookItemScore other = (GradebookItemScore) obj;
        if (grade == null) {
            if (other.grade != null)
                return false;
        } else if (!grade.equals(other.grade))
            return false;
        if (itemId == null) {
            if (other.itemId != null)
                return false;
        } else if (!itemId.equals(other.itemId))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

}
