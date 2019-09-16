package org.sakaiproject.portal.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class BullhornData {

    private String from;
    private String to;
    private String siteId;
    private String title;
    private String url;
}
