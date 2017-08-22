/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
