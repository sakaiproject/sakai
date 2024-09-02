/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import lombok.EqualsAndHashCode;
import org.sakaiproject.grading.api.Assignment;

import lombok.Data;

/**
 * This bean holds grading data that is later serialized to json.*
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GradeRestBean {

    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String score;
    private boolean notGradedYet;
    private int ungraded;
    private String url;
    private String siteTitle;
    private String siteRole;
    private String siteId;

    public GradeRestBean(Assignment assignment) {

        id = assignment.getId();
        name = assignment.getName();
        siteId = assignment.getContext();
    }
}
