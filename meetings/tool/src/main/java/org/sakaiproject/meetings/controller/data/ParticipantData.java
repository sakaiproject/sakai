package org.sakaiproject.meetings.controller.data;

import java.io.Serializable;

import lombok.Data;

@Data
public class ParticipantData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String userid;
    private String text;
    private String name;
    
}
