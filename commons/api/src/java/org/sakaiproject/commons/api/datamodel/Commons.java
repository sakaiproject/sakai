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
