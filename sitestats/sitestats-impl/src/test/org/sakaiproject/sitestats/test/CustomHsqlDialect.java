package org.sakaiproject.sitestats.test;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

/**
 * For HSQLDB to work around @Lob with a default precision of 255
 * @see <a href="https://hibernate.atlassian.net/browse/HHH-7541">HHH-7541</a>
 */
public class CustomHsqlDialect extends HSQLDialect {

    public CustomHsqlDialect() {
        super();
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CLOB, "clob");
    }
}
