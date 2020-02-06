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
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This represents an item in a gradebook and the associated scores
 * Allows for better control over the data being input and output (than the core Sakai classes)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)
 */
public class GradebookItem extends SparseGradebookItem {
    public String eid;
 // gradebookId==siteId in this context. There is another gradebookID which is Long Id that is the id of the gradebook entry in the Database.
    public String gradebookId;  
    public Double pointsPossible;
    public Date dueDate;
    public String type = "internal"; // this is the externalAppName or "internal"
    public boolean released = false;
    public boolean includedInCourseGrade = false;
    public boolean deleted = false;

    public List<GradebookItemScore> scores = new Vector<GradebookItemScore>();

    public String getIdStr() {
        return id.toString();
    }

    public Long getId() {
        return id;
    }

    /**
     * map of score id -> error_key,
     * these are recorded when this item is saved
     * (errors also recorded in the scores themselves)
     */
    public Map<String, String> scoreErrors;
    /**
     * General failure message if one occurs during save
     */
    public String failure;
    /**
     * @return true if this assignment save failed (failure will contain the error message)
     */
    public boolean isFailed() {
        return failure != null;
    }

    protected GradebookItem() {}
    public GradebookItem(String gradebookId, String name) {
        this(gradebookId, name, null, null, null, false, false);
    }
    public GradebookItem(String gradebookId, String name, Double pointsPossible,
            Date dueDate, String type, boolean released, boolean includedInCourseGrade) { 
        super(null, name);

        if (gradebookId == null || "".equals(gradebookId)) {
            throw new IllegalArgumentException("gradebookId must be set");
        }
        this.gradebookId = gradebookId;
        
        if (pointsPossible != null && pointsPossible > 0d) {
            this.pointsPossible = new Double(pointsPossible.doubleValue());
        }
        if (dueDate != null) {
            this.dueDate = new Date(dueDate.getTime());
        }
        if (type != null && ! "".equals(type)) {
            this.type = type;
        }
        this.released = released;
        this.includedInCourseGrade = includedInCourseGrade;
    }

    @Override
    public String toString() {
        return "{"+name+" ["+id+", "+eid+"] "+pointsPossible+":"+dueDate+":"+type+"::"+scores+"}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gradebookId == null) ? 0 : gradebookId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        GradebookItem other = (GradebookItem) obj;
        if (gradebookId == null) {
            if (other.gradebookId != null)
                return false;
        } else if (!gradebookId.equals(other.gradebookId))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
