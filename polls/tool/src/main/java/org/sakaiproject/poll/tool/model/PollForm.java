/**********************************************************************************
 * Copyright (c) 2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.sakaiproject.poll.api.model.Poll;

@Data
public class PollForm {
    private String pollId;
    private String text;
    private String details;
    private boolean isPublic;
    private Integer minOptions = 1;
    private Integer maxOptions = 1;
    private String displayResult = "open";
    private Poll.Access typeOfAccess = Poll.Access.SITE;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime openDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime closeDate;
    private Set<String> selectedGroupIds = new HashSet<>();
}
