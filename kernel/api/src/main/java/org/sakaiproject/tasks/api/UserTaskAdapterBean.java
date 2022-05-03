/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.tasks.api;

import java.time.Instant;
import org.springframework.beans.BeanUtils;
import lombok.Data;

import org.sakaiproject.time.api.MillisToInstantJsonDeserializer;
import org.sakaiproject.time.api.InstantToMillisJsonSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Data
public class UserTaskAdapterBean {

    private Long userTaskId;
    private Long taskId;
    private String userId;
    private String siteId;
    private String siteTitle;
    private String description;
    private String reference;
    private Boolean system;
    private Boolean complete;
    private String owner;

    @JsonSerialize(using = InstantToMillisJsonSerializer.class)
    @JsonDeserialize(using = MillisToInstantJsonDeserializer.class)
    private Instant starts;

    @JsonSerialize(using = InstantToMillisJsonSerializer.class)
    @JsonDeserialize(using = MillisToInstantJsonDeserializer.class)
    private Instant due;

    private Integer priority;
    private String notes;
    private String url;
    private Boolean softDeleted;
    private String assignationType;
    private String[] selectedGroups;
    private String taskAssignedTo;
    
    public static UserTaskAdapterBean from(UserTask userTask) {

        UserTaskAdapterBean bean = new UserTaskAdapterBean();
        BeanUtils.copyProperties(userTask, bean);
        BeanUtils.copyProperties(userTask.getTask(), bean);
        bean.setUserTaskId(userTask.getId());
        bean.setTaskId(userTask.getTask().getId());
        bean.setDue(userTask.getTask().getDue());
        return bean;
    }
    
}
