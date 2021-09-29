package org.sakaiproject.messaging.api;

import lombok.Data;

@Data
public class Message {

    private String id;
    private String type;
    private String payload;
}

