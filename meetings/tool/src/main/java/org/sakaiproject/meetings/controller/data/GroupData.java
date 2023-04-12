package org.sakaiproject.meetings.controller.data;

import java.io.Serializable;

import lombok.Data;

@Data
public class GroupData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String groupId;
    private String groupName;

}
