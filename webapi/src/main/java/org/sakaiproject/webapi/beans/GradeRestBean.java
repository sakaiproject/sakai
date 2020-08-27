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

import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeRestBean {

    private Long id;
    private String name;
    private double averageScore;
    private int ungraded;
    private String url;
    private String siteTitle;

    public GradeRestBean(Assignment assignment) {

        id = assignment.getId();
        name = assignment.getName();
    }
}
