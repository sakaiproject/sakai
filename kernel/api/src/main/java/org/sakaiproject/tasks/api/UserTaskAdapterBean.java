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
