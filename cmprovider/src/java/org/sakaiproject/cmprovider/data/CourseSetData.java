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

import org.sakaiproject.coursemanagement.api.CourseSet;

/**
 * Represents a Course Set.
 * @see CourseSet
 *
 * Example json: 
 * { "eid": "cs",
 *   "title": "Computer Science",
 *   "description": "Computer Science",
 *   "category": "Department of Computer Science"
 * }
 *
 * @author Christopher Schauer
 */
public class CourseSetData implements CmEntityData {

  @NotNull
  @Size(min=1)
  public String eid;
  
  @NotNull
  public String title = "";
  
  @NotNull
  public String description = "";
  
  @NotNull
  public String category = "";
  
  public String parent;

  public String getId() { return eid; }
  
  public CourseSetData() {}

  public CourseSetData(CourseSet set) {
    eid = set.getEid();
    title = set.getTitle();
    description = set.getDescription();
    category = set.getCategory();

    if (set.getParent() != null) parent = set.getParent().getEid();
  }
}
