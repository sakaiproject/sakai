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

/**
 * A student in the course gradebook
 * only works in the context of a course or a gradebook
 * Allows for better control over the data being input and output (than the core Sakai classes)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)
 */
public class Student extends User {

    protected Student() {}

    public Student(String userId, String username, String name) {
        super(userId, username, name, null, null);
    }

    public Student(String userId, String username, String name, String sortName, String email) {
        super(userId, username, name, sortName, email);
    }
    // student course grade
    public String courseGrade;
    
}
