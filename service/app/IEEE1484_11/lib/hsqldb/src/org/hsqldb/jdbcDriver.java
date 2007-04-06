/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.hsqldb.jdbc.jdbcConnection;
import org.hsqldb.persist.HsqlDatabaseProperties;
import org.hsqldb.persist.HsqlProperties;

// fredt@users 20011220 - patch 1.7.0 by fredt
// new version numbering scheme
// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// fredt@users 20030528 - patch 1.7.2 suggested by Gerhard Hiller - support for properties in URL

/**
 * The following comments are from the http://ldbc.sourceforge.net project.
 * These are the issues with HSQLDB JDBC implementation that are currently
 * not resolved. Other issues stated there have been resolved in 1.8.0.
 *
 * Time format error: the following statement should work, but throws an exception:
 * CREATE TABLE Test ( ID INT , Current DATETIME )
 * insert into test values(1,'2002-01-01 0:0:0.0')
 * This works:
 * insert into test values(1,'2002-01-01 00:00:00.0')
 *
 * ABS(4) returns data type DECIMAL, should return data type INTEGER
 *
 * Should throw a exception if PreparedStatement.setObject(1,null) is called.
 * See also JDBC API Tutorial and Reference, Second Edition,
 * page 544, 24.1.5 Sending JDBC NULL as an IN parameter
 *
 * Statement / PreparedStatement setMaxFieldSize is ignored.
 *
 * The database should support at least Connection.TRANSACTION_READ_COMMITTED.
 * Currently, only TRANSACTION_READ_UNCOMMITTED is supported.
 *
 * Statement.getQueryTimeout doesn't return the value set before.
 *
 * When autocommit is on, executing a query on a statement is
 * supposed to close the old resultset. This is not implemented.
 */

/**
 *  Each JDBC driver must supply a class that implements the Driver
 *  interface. <p>
 *
 *  The Java SQL framework allows for multiple database drivers. <p>
 *
 *  The DriverManager will try to load as many drivers as it can find and
 *  then for any given connection request, it will ask each driver in turn
 *  to try to connect to the target URL. <p>
 *
 *  The application developer will normally not need
 *  to call any function of the Driver directly. All required calls are made
 *  by the DriverManager. <p>
 *
 * <!-- start release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h3>HSQLDB-Specific Information:</h3> <p>
 *  When the HSQL Database Engine Driver class is loaded, it creates an
 *  instance of itself and register it with the DriverManager. This means
 *  that a user can load and register the HSQL Database Engine driver by
 *  calling <pre>
 * <code>Class.forName("org.hsqldb.jdbcDriver")</code> </pre> For more
 *  information about how to connect to a HSQL Database Engine database,
 *  please see jdbcConnection. </font><p>
 *
 *  As of version 1.7.0 all JDBC 2 methods can be
 *  called with jdk 1.1.x. Some of these method calls require int values
 *  that are defined in JDBC 2 version of ResultSet. These values are
 *  defined in the jdbcResultSet class when it is compiled with jdk 1.1.x.
 *  When using the JDBC 2 methods that require those values as parameters or
 *  return one of those values, refer to them as follows: (The code will not
 *  be compatible with other JDBC 2 driver, which require ResultSet to be
 *  used instead of jdbcResultSet) (fredt@users)<p>
 * </div> <!-- end release-specific documentation -->
 *
 *  jdbcResultSet.FETCH_FORWARD<br>
 *  jdbcResultSet.TYPE_FORWARD_ONLY<br>
 *  jdbcResultSet TYPE_SCROLL_INSENSITIVE<br>
 *  jdbcResultSet.CONCUR_READ_ONLY</font><p>
 *
 *
 * @see  jdbcConnection
 */
public class jdbcDriver implements Driver {

    /**
     *  Attempts to make a database connection to the given URL. The driver
     *  returns "null" if it realizes it is the wrong kind of driver to
     *  connect to the given URL. This will be common, as when the JDBC
     *  driver manager is asked to connect to a given URL it passes the URL
     *  to each loaded driver in turn.<p>
     *
     *  The driver raises a SQLException if it is the right driver to
     *  connect to the given URL, but has trouble connecting to the
     *  database.<p>
     *
     *  The java.util.Properties argument can be used to passed arbitrary
     *  string tag/value pairs as connection arguments.<p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *  For HSQL Database Engine, at least "user" and
     *  "password" properties must be included in the Properties. From
     *  version 1.7.1 two optional properties are supported:<p>
     *  <code>get_column_name</code> if set to false, a
     *  ResultSetMetaData.getColumnName() call will return the user defined
     *  label instead of the column name.
     *  <code>strict_md</code> if set to true, some ResultSetMetaData methods
     *  return more strict values for compatibility reasons.
     * </div> <!-- end release-specific documentation -->
     *
     * @param  url the URL of the database to which to connect
     * @param  info a list of arbitrary string tag/value pairs as connection
     *      arguments. Normally at least a "user" and "password" property
     *      should be included.
     * @return  a <code>Connection</code> object that represents a
     *      connection to the URL
     * @exception  SQLException if a database access error occurs
     */
    public Connection connect(String url,
                              Properties info) throws SQLException {
        return getConnection(url, info);
    }

    public static Connection getConnection(String url,
                                           Properties info)
                                           throws SQLException {

        HsqlProperties props = DatabaseURL.parseURL(url, true);

        if (props == null) {

            // supposed to be an HSQLDB driver url but has errors
            throw new SQLException(
                Trace.getMessage(Trace.INVALID_JDBC_ARGUMENT));
        } else if (props.isEmpty()) {

            // is not an HSQLDB driver url
            return null;
        }

        props.addProperties(info);

        return new jdbcConnection(props);
    }

    /**
     *  Returns true if the driver thinks that it can open a connection to
     *  the given URL. Typically drivers will return true if they understand
     *  the subprotocol specified in the URL and false if they don't.
     *
     * @param  url the URL of the database
     * @return  true if this driver can connect to the given URL
     */

    // fredt@users - patch 1.7.0 - allow mixedcase url's
    public boolean acceptsURL(String url) {

        return url != null
               && url.regionMatches(true, 0, DatabaseURL.S_URL_PREFIX, 0,
                                    DatabaseURL.S_URL_PREFIX.length());
    }

    /**
     *  Gets information about the possible properties for this driver. <p>
     *
     *  The getPropertyInfo method is intended to allow a generic GUI tool
     *  to discover what properties it should prompt a human for in order to
     *  get enough information to connect to a database. Note that depending
     *  on the values the human has supplied so far, additional values may
     *  become necessary, so it may be necessary to iterate though several
     *  calls to getPropertyInfo.<p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     * HSQLDB 1.7.2 uses the values submitted in info to set the value for
     * each DriverPropertyInfo object returned. It does not use the default
     * value that it would use for the property if the value is null.
     * </div> <!-- end release-specific documentation -->
     *
     * @param  url the URL of the database to which to connect
     * @param  info a proposed list of tag/value pairs that will be sent on
     *      connect open
     * @return  an array of DriverPropertyInfo objects describing possible
     *      properties. This array may be an empty array if no properties
     *      are required.
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {

        String[]             choices = new String[] {
            "true", "false"
        };
        DriverPropertyInfo[] pinfo   = new DriverPropertyInfo[6];
        DriverPropertyInfo   p;

        p          = new DriverPropertyInfo("user", null);
        p.value    = info.getProperty("user");
        p.required = true;
        pinfo[0]   = p;
        p          = new DriverPropertyInfo("password", null);
        p.value    = info.getProperty("password");
        p.required = true;
        pinfo[1]   = p;
        p          = new DriverPropertyInfo("get_column_name", null);
        p.value    = info.getProperty("get_column_name", "true");
        p.required = false;
        p.choices  = choices;
        pinfo[2]   = p;
        p          = new DriverPropertyInfo("ifexists", null);
        p.value    = info.getProperty("ifexists");
        p.required = false;
        p.choices  = choices;
        pinfo[3]   = p;
        p          = new DriverPropertyInfo("default_schema", null);
        p.value    = info.getProperty("default_schema");
        p.required = false;
        p.choices  = choices;
        pinfo[4]   = p;
        p          = new DriverPropertyInfo("shutdown", null);
        p.value    = info.getProperty("shutdown");
        p.required = false;
        p.choices  = choices;
        pinfo[5]   = p;

        return pinfo;
    }

    /**
     *  Gets the driver's major version number.
     *
     * @return  this driver's major version number
     */
    public int getMajorVersion() {
        return HsqlDatabaseProperties.MAJOR;
    }

    /**
     *  Gets the driver's minor version number.
     *
     * @return  this driver's minor version number
     */
    public int getMinorVersion() {
        return HsqlDatabaseProperties.MINOR;
    }

    /**
     *  Reports whether this driver is a genuine JDBC COMPLIANT<sup><font
     *  size=-2>TM</font> </sup> driver. A driver may only report true here
     *  if it passes the JDBC compliance tests; otherwise it is required to
     *  return false. JDBC compliance requires full support for the JDBC API
     *  and full support for SQL 92 Entry Level. It is expected that JDBC
     *  compliant drivers will be available for all the major commercial
     *  databases. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *  HSQL Database Engine currently does not yet
     *  support all required SQL 92 Entry Level functionality and thus
     *  returns false. It looks like other drivers return true but do not
     *  support all features.<p>
     * </div> <!-- end release-specific documentation -->
     *
     *  This method is not intended to encourage the development of non-JDBC
     *  compliant drivers, but is a recognition of the fact that some
     *  vendors are interested in using the JDBC API and framework for
     *  lightweight databases that do not support full database
     *  functionality, or for special databases such as document information
     *  retrieval where a SQL implementation may not be feasible.
     *
     * @return  Description of the Return Value
     */
    public boolean jdbcCompliant() {

/** @todo fredt - we should aim to be able to report true */
        return false;
    }

    static {
        try {
            DriverManager.registerDriver(new jdbcDriver());
        } catch (Exception e) {}
    }
}
