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
 * This represents the basic information for a gradebook item. See {@link GradebookItem} for additional
 * properties
 */
public class SparseGradebookItem {
    public Long id;
    public String name;

    protected SparseGradebookItem() {}
    public SparseGradebookItem(Long id, String name) {
        this.id = id;
        
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name must be set");
        }
        
        this.name = name;
    }

}
