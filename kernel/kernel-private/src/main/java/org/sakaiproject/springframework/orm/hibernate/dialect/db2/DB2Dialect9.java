package org.sakaiproject.springframework.orm.hibernate.dialect.db2;

import org.hibernate.dialect.DB2Dialect;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: May 23, 2007
 * Time: 4:20:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class DB2Dialect9 extends DB2Dialect {

    public DB2Dialect9() {
       super();
       registerColumnType( Types.VARBINARY, "LONG VARCHAR FOR BIT DATA" );
       registerColumnType( Types.VARCHAR, "clob(1000000000)" );
       registerColumnType( Types.VARCHAR, 1000000000, "clob($l)" );
       registerColumnType( Types.VARCHAR, 3999, "varchar($l)" );


       //according to the db2 docs the max for clob and blob should be 2147438647, but this isn't working for me
       // possibly something to do with how my database is configured ?
       registerColumnType( Types.CLOB, "clob(1000000000)" );
       registerColumnType( Types.CLOB, 1000000000, "clob($l)" );
       registerColumnType( Types.BLOB, "blob(1000000000)" );
       registerColumnType( Types.BLOB, 1000000000, "blob($l)" );

    }
}
