/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class QuestionPoolAccessFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    private String agentDisplayName;
    private Long questionPoolId;
    private String agentId;
    private Long accessTypeId;
    private String role;

    public static final Long ACCESS_DENIED =  Long.valueOf(30);
    public static final Long READ_ONLY = Long.valueOf(31);
    public static final Long MODIFY = Long.valueOf(32);
    public static final Long READ_WRITE = Long.valueOf(33);
    public static final Long ADMIN = Long.valueOf(34);

    public QuestionPoolAccessFacade(Long questionPoolId, Long accessTypeId, String agentId) {
        this.questionPoolId = questionPoolId;
        this.accessTypeId = accessTypeId;
        this.agentId = agentId;
        AgentFacade agent = new AgentFacade(this.agentId);
        this.agentDisplayName = agent.getDisplayName();
        this.role = agent.getRole();
    }

}
