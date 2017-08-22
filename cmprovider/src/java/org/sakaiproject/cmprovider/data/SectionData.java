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

import org.sakaiproject.coursemanagement.api.Section;

/**
 * Represents a section.
 * @see Section
 *
 * Example json:
 *
 * { "eid": "term1.CS101.1",
 *   "courseOffering": "term1.CS101",
 *   "enrollmentSet": "term1.CS101.1",
 *   "title": "CS 101 Term 1 Section 1",
 *   "description": "Introduction to computer science",
 *   "category": "lecture",
 *   "maxSize": 100 }
 *
 * @author Christopher Schauer
 */
public class SectionData implements CmEntityData {
  @NotNull
  @Size(min=1)
  public String eid;
  
  @NotNull
  @Size(min=1)
  public String title;
  
  @NotNull
  public String description = "";
  
  @NotNull
  public String category = "";
  
  @NotNull
  @Size(min=1)
  public String courseOffering;
  
  @NotNull
  @Size(min=1)
  public String enrollmentSet;
  
  public Integer maxSize;
  public String parent;

  public String getId() { return eid; }
  
  public SectionData() {}

  public SectionData(Section section) {
    eid = section.getEid();
    title = section.getTitle();
    description = section.getDescription();
    category = section.getCategory();
    courseOffering = section.getCourseOfferingEid();
    enrollmentSet = section.getEnrollmentSet().getEid();
    maxSize = section.getMaxSize();

    if (section.getParent() != null) parent = section.getParent().getEid();
  }
}
