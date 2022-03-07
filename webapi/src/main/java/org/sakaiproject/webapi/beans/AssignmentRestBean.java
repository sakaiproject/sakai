/******************************************************************************
 * Copyright 2022 sakaiproject.org Licensed under the Educational
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

import org.sakaiproject.assignment.api.model.Assignment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentRestBean {

    private String id;
    private String title;
    private String status;
    private String access;
    private String scale;
    private Long release;
    private Long retract;
    private int totalSubmissions;
    private int newSubmissions;
    private boolean hasAttachment;

    public AssignmentRestBean(Assignment assignment) {
        id = assignment.getId();
        title = assignment.getTitle();
        hasAttachment = assignment.getAttachments().size() > 0;
        retract = assignment.getDueDate().toEpochMilli();
        release = assignment.getOpenDate().toEpochMilli();
    }
}
