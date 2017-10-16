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
package org.sakaiproject.commons.api.datamodel;

import org.sakaiproject.commons.api.CommonsConstants;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Commons {

    private String id;
    private String siteId;
    private String embedder;

    public Commons(ResultSet rs) throws SQLException {

        this.id = rs.getString("ID");
        this.siteId = rs.getString("SITE_ID");
        this.embedder = rs.getString("EMBEDDER");
    }

    public boolean isSocial() {
        return embedder.equals(CommonsConstants.SOCIAL);
    }
}
