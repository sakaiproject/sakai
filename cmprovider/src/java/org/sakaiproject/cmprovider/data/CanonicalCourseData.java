/**
 * Copyright (c) 2015 The Apereo Foundation
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
package org.sakaiproject.cmprovider.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.sakaiproject.coursemanagement.api.CanonicalCourse;

/**
 * Represents a canonical course.
 * @see CanonicalCourse
 *
 * Example json:
 *
 * { "eid": "CS101",
 *   "title": "CS101",
 *   "description": "Introduction to computer science" } 
 *
 * @author Christopher Schauer
 */
public class CanonicalCourseData implements CmEntityData {
  @NotNull
  @Size(min=1)
  public String eid;
  
  @NotNull
  public String title = "";
  
  @NotNull
  public String description = "";

  public String getId() { return eid; }

  public CanonicalCourseData() {}

  public CanonicalCourseData(CanonicalCourse course) {
    eid = course.getEid();
    title = course.getTitle();
    description = course.getDescription();
  }
}
