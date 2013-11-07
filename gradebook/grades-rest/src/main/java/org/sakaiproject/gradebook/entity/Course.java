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

/**
 * Represents a course (really a site) in Sakai
 * Allows for better control over the data being input and output (than the core Sakai classes)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)
 */
public class Course {

    public String id;
    public String title;
    public String description;
    /**
     * This is the timecode (seconds) of the time when this course was created
     */
    private long createdTime;
    public boolean published;

    public List<Student> students = null;

    public Course(String id, String title) {
        this(id, title, null);
    }

    public Course(String id, String title, String description) {
        this(id, title, description, (System.currentTimeMillis() / 1000), true);
    }

    /**
     * @param id the course id
     * @param title the title
     * @param description
     * @param createdTime the timecode (seconds) of the time when this course was created (not in milliseconds)
     * @param published true if the course is published/available to students, false otherwise
     */
    public Course(String id, String title, String description, long createdTime, boolean published) {
        this.id = id;
        this.title = title;
        this.description = description;
        if (createdTime > (System.currentTimeMillis() / 1000)) {
            // must have used the milliseconds version instead
            createdTime = (createdTime / 1000);
        }
        this.createdTime = createdTime;
        this.published = published;
    }

    @Override
    public String toString() {
        return id + ":" + title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /**
     * NOTE: This is the unix timecode in seconds and NOT the milliseconds returned in java normally
     * 
     * @return the unix timecode (seconds) when this course was created
     */
    public long getCreatedTime() {
        return createdTime;
    }

    public boolean isPublished() {
        return published;
    }

    public List<Student> getStudents() {
        return students;
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
        Course other = (Course) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
