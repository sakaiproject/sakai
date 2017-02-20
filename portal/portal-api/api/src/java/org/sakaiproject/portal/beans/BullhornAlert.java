package org.sakaiproject.portal.beans;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BullhornAlert {

    public long id = 0L;
    public String from = "";
    public String fromDisplayName = "";
    public String to = "";
    public String event = "";
    public String ref = "";
    public String title = "";
    public String siteId = "";
    public String siteTitle = "";
    public String url = "";
    public long eventDate = 0L;

    public BullhornAlert() { }

    public BullhornAlert(ResultSet rs) {

        try {
            id = rs.getLong("ID");
            from = rs.getString("FROM_USER");
            to = rs.getString("TO_USER");
            event = rs.getString("EVENT");
            ref = rs.getString("REF");
            title = rs.getString("TITLE");
            siteId = rs.getString("SITE_ID");
            url = rs.getString("URL");
            eventDate = rs.getTimestamp("EVENT_DATE").getTime();
        } catch (SQLException sqle) {
            log.error("Failed to build BullhornAlert from db record", sqle);
        }
    }
}
