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

import java.util.List;
import java.util.Vector;


/**
 * This holds the values of all the items in a gradebook and the users/scores
 * Allows for better control over the data being input and output (than the core Sakai classes)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)
 */
public class Gradebook {
    public String id;
    public String courseId;
    public String averageCourseGrade;

    public List<Student> students = new Vector<Student>();
    public List<GradebookItem> items = new Vector<GradebookItem>();
    public List<Category> category = new Vector<Category>();
    public boolean displayReleasedGradeItemsToStudents =false;
    public String gradebookScale=null;
    public boolean isPointFlag=false;
    public boolean isPercentFlag=false;
    public boolean isLetterGradeFlag=false;

    public Gradebook(String id) {
        this.id = id;
        this.courseId = id;
    }

    @Override
    public String toString() {
        return id+":("+students+"):"+items;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Gradebook other = (Gradebook) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
