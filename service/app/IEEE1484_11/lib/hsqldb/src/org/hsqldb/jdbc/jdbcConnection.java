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


package org.hsqldb.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

//#ifdef JDBC3
import java.sql.Savepoint;

//#endif JDBC3
//#ifdef JAVA2
import java.util.Map;

//#endif JAVA2
import java.util.Locale;

import org.hsqldb.DatabaseManager;
import org.hsqldb.DatabaseURL;
import org.hsqldb.HSQLClientConnection;
import org.hsqldb.HTTPClientConnection;
import org.hsqldb.HsqlException;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.Result;
import org.hsqldb.ResultConstants;
import org.hsqldb.Session;
import org.hsqldb.SessionInterface;
import org.hsqldb.Trace;
import org.hsqldb.lib.StringUtil;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// boucherb@users 20020509 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.Connection interface,
// updated generic documentation to JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020505 - extensive review and update
// of docs and behaviour to comply with java.sql specification
// fredt@users 20020830 - patch 487323 by xclayl@users - better synchronization
// fredt@users 20020930 - patch 1.7.1 - support for connection properties
// kneedeepincode@users 20021110 - patch 635816 - correction to properties
// unsaved@users 20021113 - patch 1.7.2 - SSL support
// boucherb@users 2003 ??? - patch 1.7.2 - SSL support moved to factory interface
// fredt@users 20030620 - patch 1.7.2 - reworked to use a SessionInterface
// boucherb@users 20030801 - JavaDoc updates to reflect new connection urls
// boucherb@users 20030819 - patch 1.7.2 - partial fix for broken nativeSQL method
// boucherb@users 20030819 - patch 1.7.2 - SQLWarning cases implemented

/**
 * <!-- start generic documentation -->
 * A connection (session) with a specific database. Within the context
 * of a Connection, SQL statements are executed and results
 * are returned. <p>
 *
 * A Connection's database is able to provide information describing
 * its tables, its supported SQL grammar, its stored procedures, the
 * capabilities of this connection, and so on. This information is
 * obtained with the <code>getMetaData</code> method. <p>
 *
 * <B>Note:</B> By default the Connection automatically commits
 * changes after executing each statement. If auto commit has been
 * disabled, an explicit commit must be done or database changes will
 * not be saved. <p>
 *
 * <!-- end generic documentation -->
 * <!-- start release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 *
 * <hr>
 *
 * <b>HSQLDB-Specific Information:</b> <p>
 *
 * To get a <code>Connection</code> to an HSQLDB database, the
 * following code may be used (updated to reflect the most recent
 * recommendations): <p>
 *
 * <hr>
 *
 * When using HSQLDB, the database connection <b>&lt;url&gt;</b> must start with
 * <b>'jdbc:hsqldb:'</b><p>
 *
 * Since 1.7.2, connection properties (&lt;key-value-pairs&gt;) may be appended
 * to the database connection <b>&lt;url&gt;</b>, using the form: <p>
 *
 * <blockquote>
 *      <b>'&lt;url&gt;[;key=value]*'</b>
 * </blockquote> <p>
 *
 * Also since 1.7.2, the allowable forms of the HSQLDB database connection
 * <b>&lt;url&gt;</b> have been extended.  However, all legacy forms continue
 * to work, with unchanged semantics.  The extensions are as described in the
 * following material. <p>
 *
 * <hr>
 *
 * <b>Network Server Database Connections:</b> <p>
 *
 * The 1.7.2 {@link Server Server} database connection <b>&lt;url&gt;</b> has
 * changed to take one of the two following forms: <p>
 *
 * <div class="GeneralExample">
 * <ol>
 * <li> <b>'jdbc:hsqldb:hsql://host[:port][/&lt;alias&gt;][&lt;key-value-pairs&gt;]'</b>
 *
 * <li> <b>'jdbc:hsqldb:hsqls://host[:port][/&lt;alias&gt;][&lt;key-value-pairs&gt;]'</b>
 *         (with TLS).
 * </ol>
 * </div> <p>
 *
 * The 1.7.2 {@link WebServer WebServer} database connection <b>&lt;url&gt;</b>
 * also changes to take one of two following forms: <p>
 *
 * <div class="GeneralExample">
 * <ol>
 * <li> <b>'jdbc:hsqldb:http://host[:port][/&lt;alias&gt;][&lt;key-value-pairs&gt;]'</b>
 *
 * <li> <b>'jdbc:hsqldb:https://host[:port][/&lt;alias&gt;][&lt;key-value-pairs&gt;]'</b>
 *      (with TLS).
 * </ol>
 * </div><p>
 *
 * In both network server database connection <b>&lt;url&gt;</b> forms, the
 * optional <b>&lt;alias&gt;</b> component is used to identify one of possibly
 * several database instances available at the indicated host and port.  If the
 * <b>&lt;alias&gt;</b> component is omitted, then a connection is made to the
 * network server's default database instance. <p>
 *
 * For more information on server configuration regarding mounting multiple
 * databases and assigning them <b>&lt;alias&gt;</b> values, please read the
 * Java API documentation for {@link org.hsqldb.Server Server} and related
 * chapters in the general documentation, especially the Advanced Users
 * Guide. <p>
 *
 * <hr>
 *
 * <b>Transient, In-Process Database Connections:</b> <p>
 *
 * The 1.7.2 100% in-memory (transient, in-process) database connection
 * <b>&lt;url&gt;</b> takes one of the two following forms: <p>
 *
 * <div class="GeneralExample">
 * <ol>
 * <li> <b>'jdbc:hsqldb:.[&lt;key-value-pairs&gt;]'</b>
 *     (the legacy form, extended)
 *
 * <li> <b>'jdbc:hsqldb:mem:&lt;alias&gt;[&lt;key-value-pairs&gt;]'</b>
 *      (the new form)
 * </ol>
 * </div> <p>
 *
 * With the 1.7.2 transient, in-process database connection <b>&lt;url&gt;</b>,
 * the <b>&lt;alias&gt;</b> component is the key used to look up a transient,
 * in-process database instance amongst the collection of all such instances
 * already in existence within the current class loading context in the
 * current JVM. If no such instance exists, one <em>may</em> be automatically
 * created and mapped to the <b>&lt;alias&gt;</b>, as governed by the
 * <b>'ifexists=true|false'</b> connection property. <p>
 *
 * <hr>
 *
 * <b>Persistent, In-Process Database Connections:</b> <p>
 *
 * The 1.7.2 standalone (persistent, in-process) database connection
 * <b>&lt;url&gt;</b> takes one of the three following forms: <p>
 *
 * <div class="GeneralExample">
 * <ol>
 * <li> <b>'jdbc:hsqldb:&lt;path&gt;[&lt;key-value-pairs&gt;]'</b>
 *      (the legacy form, extended)
 *
 * <li> <b>'jdbc:hsqldb:file:&lt;path&gt;[&lt;key-value-pairs&gt;]'</b>
 *      (same semantics as the legacy form)
 *
 * <li> <b>'jdbc:hsqldb:res:&lt;path&gt;[&lt;key-value-pairs&gt;]'</b>
 *      (new form with 'files_in_jar' semantics)
 * </ol>
 * </div> <p>
 *
 * For the persistent, in-process database connection <b>&lt;url&gt;</b>,
 * the <b>&lt;path&gt;</b> component is the path prefix common to all of
 * the files that compose the database. <p>
 *
 * As of 1.7.2, although other files may be involved (such as transient working
 * files and/or TEXT table CSV data source files), the essential set that may,
 * at any particular point in time, compose an HSQLDB database are: <p>
 *
 * <div class="GeneralExample">
 * <ul>
 * <li>&lt;path&gt;.properties
 * <li>&lt;path&gt;.script
 * <li>&lt;path&gt;.log
 * <li>&lt;path&gt;.data
 * <li>&lt;path&gt;.backup
 * <li>&lt;path&gt;.lck
 * </ul>
 * </div> <p>
 *
 * For example: <b>'jdbc:hsqldb:file:test'</b> connects to a database
 * composed of some subset of the files listed above, where the expansion
 * of <b>&lt;path&gt;</b> is <b>'test'</b> prefixed with the path of the
 * working directory fixed at the time the JVM is started. <p>
 *
 * Under <em>Windows</em> <sup><font size="-2">TM</font> </sup>, <b>
 * 'jdbc:hsqldb:file:c:\databases\test'</b> connects to a database located
 * on drive <b>'C:'</b> in the directory <b>'databases'</b>, composed
 * of some subset of the files: <p>
 *
 * <pre class="GeneralExample">
 * C:\
 * +--databases\
 *    +--test.properties
 *    +--test.script
 *    +--test.log
 *    +--test.data
 *    +--test.backup
 *    +--test.lck
 * </pre>
 *
 * Under most variations of UNIX, <b>'jdbc:hsqldb:file:/databases/test'</b>
 * connects to a database located in the directory <b>'databases'</b> directly
 * under root, once again composed of some subset of the files: <p>
 *
 * <pre class="GeneralExample">
 * /
 * +--databases/
 *    +--test.properties
 *    +--test.script
 *    +--test.log
 *    +--test.data
 *    +--test.backup
 *    +--test.lck
 * </pre>
 *
 * <b>Some Guidelines:</b> <p>
 *
 * <ol>
 * <li> Both relative and absolute database file paths are supported. <p>
 *
 * <li> Relative database file paths can be specified in a platform independent
 *      manner as: <b>'[dir1/dir2/.../dirn/]&lt;file-name-prefix&gt;'</b>. <p>
 *
 * <li> Specification of absolute file paths is operating-system specific.<br>
 *      Please read your OS file system documentation. <p>
 *
 * <li> Specification of network mounts may be operating-system specific.<br>
 *      Please read your OS file system documentation. <p>
 *
 * <li> Special care may be needed w.r.t. file path specifications
 *      containing whitespace, mixed-case, special characters and/or
 *      reserved file names.<br>
 *      Please read your OS file system documentation. <p>
 * </ol> <p>
 *
 * <b>Note:</b> Versions of HSQLDB previous to 1.7.0 did not support creating
 * directories along the file path specified in the persistent, in-process mode
 * database connection <b>&lt;url&gt;</b> form, in the case that they did
 * not already exist.  Starting with HSQLDB 1.7.0, directories <i>will</i>
 * be created if they do not already exist., but only if HSQLDB is built under
 * a version of the compiler greater than JDK 1.1.x. <p>
 *
 * <b>res: Connections</b><p>
 *
 * The new <b>'jdbc:hsqldb:res:&lt;path&gt;'</b> database connection
 * <b>&lt;url&gt;</b> has different semantics than the
 * <b>'jdbc:hsqldb:file:&lt;path&gt;'</b> form. The semantics are similar to
 * those of a <b>'files_readonly'</b> database, but with some additional
 * points to consider. <p>
 *
 * Specifically, the <b>'&lt;path&gt;'</b> component of a <b>res:</b> type
 * database connection <b>&lt;url&gt;</b> is used to obtain resource URL
 * objects and thereby read the database files as resources on the class path.
 * Moreover, the URL objects <i>must</i> point only to resources contained
 * in one or more jars on the class path (must be jar protocol). <p>
 *
 * This restriction is enforced to avoid the unfortunate situation in which,
 * because <b>res:</b> database instances do not create a &lt;path&gt;.lck file
 * (they are strictly files-read-only) and because the <b>&lt;path&gt;</b>
 * components of <b>res:</b> and <b>file:</b> database URIs are not checked
 * for file system equivalence, it is possible for the same database files to
 * be accessed concurrently by both <b>file:</b> and <b>res:</b> database
 * instances. That is, without this restriction, it is possible that
 * &lt;path&gt;.data and &lt;path&gt;.properties file content may be written
 * by a <b>file:</b> database instance without the knowlege or cooperation
 * of a <b>res:</b> database instance open on the same files, potentially
 * resulting in unexpected database errors, inconsistent operation
 * and/or data corruption. <p>
 *
 * In short, a <b>res:</b> type database connection <b>&lt;url&gt;</b> is
 * designed specifically to connect to a <b>'files_in_jar'</b> mode database
 * instance, which in turn is designed specifically to operate under
 * <em>Java WebStart</em><sup><font size="-2">TM</font></sup> and
 * <em>Java Applet</em><sup><font size="-2">TM</font></sup>configurations,
 * where co-locating the database files in the jars that make up the
 * <em>WebStart</em> application or Applet avoids the need for special security
 * configuration or code signing. <p>
 *
 * <b>Note:</b> Since it is difficult and often nearly impossible to determine
 * or control at runtime from where all classes are being loaded or which class
 * loader is doing the loading under <b>'files_in_jar'</b> semantics, the
 * <b>&lt;path&gt;</b> component of the res: database connection
 * <b>&lt;url&gt;</b> is always taken to be relative to the default package.
 * That is, if the <b>&lt;path&gt;</b> component does not start with '/', then
 * '/' is prepended when obtaining the resource URLs used to read the database
 * files. <p>
 *
 * <hr>
 *
 * For more information about HSQLDB file structure, various database modes
 * and other attributes such as those controlled through the HSQLDB properties
 * files, please read the general documentation, especially the Advanced Users
 * Guide. <p>
 *
 * <hr>
 *
 * <b>JRE 1.1.x Notes:</b> <p>
 *
 * In general, JDBC 2 support requires Java 1.2 and above, and JDBC3 requires
 * Java 1.4 and above. In HSQLDB, support for methods introduced in different
 * versions of JDBC depends on the JDK version used for compiling and building
 * HSQLDB.<p>
 *
 * Since 1.7.0, it is possible to build the product so that
 * all JDBC 2 methods can be called while executing under the version 1.1.x
 * <em>Java Runtime Environment</em><sup><font size="-2">TM</font></sup>.
 * However, in addition to this technique requiring explicit casts to the
 * org.hsqldb.jdbcXXX classes, some of the method calls also require
 * <code>int</code> values that are defined only in the JDBC 2 or greater
 * version of
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <code>ResultSet</code></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined
 * in {@link org.hsqldb.jdbc.jdbcResultSet jdbcResultSet}. <p>
 *
 * In a JRE 1.1.x environment, calling JDBC 2 methods that take or return the
 * JDBC2-only <code>ResultSet</code> values can be achieved by referring
 * to them in parameter specifications and return value comparisons,
 * respectively, as follows: <p>
 *
 * <pre class="JavaCodeExample">
 * jdbcResultSet.FETCH_FORWARD
 * jdbcResultSet.TYPE_FORWARD_ONLY
 * jdbcResultSet.TYPE_SCROLL_INSENSITIVE
 * jdbcResultSet.CONCUR_READ_ONLY
 * // etc.
 * </pre>
 *
 * However, please note that code written to use HSQLDB JDBC 2 features under
 * JDK 1.1.x will not be compatible for use with other JDBC 2 drivers. Please
 * also note that this feature is offered solely as a convenience to developers
 * who must work under JDK 1.1.x due to operating constraints, yet wish to
 * use some of the more advanced features available under the JDBC 2
 * specification. <p>
 *
 * <hr>
 *
 * (fredt@users)<br>
 * (boucherb@users)<p>
 *
 * </div> <!-- end release-specific documentation -->
 * @author boucherb@users
 * @author fredt@users
 * @version 1.7.2
 * @see org.hsqldb.jdbcDriver
 * @see jdbcStatement
 * @see jdbcPreparedStatement
 * @see jdbcCallableStatement
 * @see jdbcResultSet
 * @see jdbcDatabaseMetaData
 */
public class jdbcConnection implements Connection {

// ---------------------------- Common Attributes --------------------------

    /**
     * Properties for the connection
     *
     */
    HsqlProperties connProperties;

    /**
     * This connection's interface to the corresponding Session
     * object in the database engine.
     */
    SessionInterface sessionProxy;

    /**
     * Is this an internal connection?
     */
    boolean isInternal;

    /** Is this connection to a network server instance. */
    protected boolean isNetConn;

    /**
     * Is this connection closed?
     */
    boolean isClosed;

    /** The first warning in the chain. Null if there are no warnings. */
    private SQLWarning rootWarning;

    /** Synchronizes concurrent modification of the warning chain */
    private Object rootWarning_mutex = new Object();

    /**
     * The set of open Statement objects returned by this Connection from
     * calls to createStatement, prepareCall and prepareStatement. This is
     * used solely for closing the statements when this Connection is closed.
     */
    /*
    private org.hsqldb.lib.HashSet statementSet =
        new org.hsqldb.lib.HashSet();
     */

// ----------------------------------- JDBC 1 -------------------------------

    /**
     * <!-- start generic documentation -->
     * Creates a <code>Statement</code>
     * object for sending SQL statements to the database. SQL
     * statements without parameters are normally executed using
     * <code>Statement</code> objects. If the same SQL statement is
     * executed many times, it may be more efficient to use a
     * <code>PreparedStatement</code> object.<p>
     *
     * Result sets created using the returned <code>Statement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, support for precompilation at the engine level
     * has been implemented, so it is now much more efficient and performant
     * to use a <code>PreparedStatement</code> object if the same SQL statement
     * is executed many times. <p>
     *
     * Up to 1.6.1, HSQLDB supported <code>TYPE_FORWARD_ONLY</code> -
     * <code>CONCUR_READ_ONLY</code> results only, so <code>ResultSet</code>
     * objects created using the returned <code>Statement</code>
     * object would <I>always</I> be type <code>TYPE_FORWARD_ONLY</code>
     * with <code>CONCUR_READ_ONLY</code> concurrency. <p>
     *
     * Starting with 1.7.0, HSQLDB also supports
     * <code>TYPE_SCROLL_INSENSITIVE</code> results. <p>
     *
     * <b>Notes:</b> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw a
     * <code>SQLException</code> if the connection is closed. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return a new default Statement object
     * @throws SQLException if a database access error occurs<p>
     * @see #createStatement(int,int)
     * @see #createStatement(int,int,int)
     */
    public synchronized Statement createStatement() throws SQLException {

        checkClosed();

        Statement stmt = new jdbcStatement(this,
                                           jdbcResultSet.TYPE_FORWARD_ONLY);

        return stmt;
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>PreparedStatement</code>
     * object for sending parameterized SQL statements to the
     * database. <p>
     *
     * A SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code>
     * object. This object can then be used to efficiently execute
     * this statement multiple times. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code> object is executed. This has no
     * direct effect on users; however, it does affect which methods
     * throw certain <code>SQLException</code> objects.<p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, support for precompilation at the engine level
     * has been implemented, so it is now much more efficient and performant
     * to use a <code>PreparedStatement</code> object if the same SQL statement
     * is executed many times. <p>
     *
     * Starting with 1.7.2, the support for and behaviour of
     * PreparedStatment has changed.  Please read the introductory section
     * of the documentation for org.hsqldb.jdbc.jdbcPreparedStatement. <P>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *    IN parameter placeholders
     * @return a new default <code>PreparedStatement</code> object
     *    containing the pre-compiled SQL statement
     * @exception SQLException if a database access error occurs <p>
     * @see #prepareStatement(String,int,int)
     */
    public synchronized PreparedStatement prepareStatement(String sql)
    throws SQLException {

        PreparedStatement stmt;

        checkClosed();

        try {
            stmt = new jdbcPreparedStatement(this, sql,
                                             jdbcResultSet.TYPE_FORWARD_ONLY);

            return stmt;
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>CallableStatement</code>
     * object for calling database stored procedures. The
     * <code>CallableStatement</code> object provides methods for setting up
     * its IN and OUT  parameters, and methods for executing the call to a
     * stored procedure. <p>
     *
     * <b>Note:</b> This method is optimized for handling stored
     * procedure call statements. Some drivers may send the call
     * statement to the database when the method <code>prepareCall</code>
     * is done; others may wait until the <code>CallableStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which method throws certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>CallableStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, the support for and behaviour of
     * CallableStatement has changed.  Please read the introductory section
     * of the documentation for org.hsqldb.jdbc.jdbcCallableStatement.
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql a String object that is the SQL statement to be
     *  sent to the database; may contain one or more ?
     *  parameters. <p>
     *
     *  <B>Note:</B> Typically the SQL statement is a JDBC
     *  function call escape string.
     * @return a new default <code>CallableStatement</code> object
     *  containing the pre-compiled SQL statement
     * @exception SQLException if a database access error occurs <p>
     * @see #prepareCall(String,int,int)
     */
    public synchronized CallableStatement prepareCall(String sql)
    throws SQLException {

        CallableStatement stmt;

        checkClosed();

        try {
            stmt = new jdbcCallableStatement(this, sql,
                                             jdbcResultSet.TYPE_FORWARD_ONLY);

            return stmt;
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Converts the given SQL statement
     * into the system's native SQL grammar. A driver may convert the
     * JDBC SQL grammar into its system's native SQL grammar prior to
     * sending it. This method returns the native form of the
     * statement that the driver would have sent. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB converts the JDBC SQL
     * grammar into the system's native SQL grammar prior to sending
     * it, if escape processing is set true; this method returns the
     * native form of the statement that the driver would send in place
     * of client-specified JDBC SQL grammar. <p>
     *
     * Before 1.7.2, escape processing was incomplete and
     * also broken in terms of support for nested escapes. <p>
     *
     * Starting with 1.7.2, escape processing is complete and handles nesting
     * to arbitrary depth, but enforces a very strict interpretation of the
     * syntax and does not detect or process SQL comments. <p>
     *
     * In essence, the HSQLDB engine directly handles the prescribed syntax
     * and date / time formats specified internal to the JDBC escapes.
     * It also directly offers the XOpen / ODBC extended scalar
     * functions specified available internal to the {fn ...} JDBC escape.
     * As such, the driver simply removes the curly braces and JDBC escape
     * codes in the simplest and fastest fashion possible, by replacing them
     * with whitespace.
     *
     * But to avoid a great deal of complexity, certain forms of input
     * whitespace are currently not recognised.  For instance,
     * the driver handles "{?= call ...}" but not "{ ?= call ...} or
     * "{? = call ...}" <p>
     *
     * Also, comments embedded in SQL are currently not detected or
     * processed and thus may have unexpected effects on the output
     * of this method, for instance causing otherwise valid SQL to become
     * invalid. It is especially important to be aware of this because escape
     * processing is set true by default for Statement objects and is always
     * set true when producing a PreparedStatement from prepareStatement()
     * or CallableStatement from prepareCall().  Currently, it is simply
     * recommended to avoid submitting SQL having comments containing JDBC
     * escape sequence patterns and/or single or double quotation marks,
     * as this will avoid any potential problems.
     *
     * It is intended to implement a less strict handling of whitespace and
     * proper processing of SQL comments at some point in the near future,
     * perhaps before the final 1.7.2 release.
     *
     * In any event, 1.7.2 now correctly processes the following JDBC escape
     * forms to arbitrary nesting depth, but only if the exact whitespace
     * layout described below is used: <p>
     *
     * <ol>
     * <li>{call ...}
     * <li>{?= call ...}
     * <li>{fn ...}
     * <li>{oj ...}
     * <li>{d ...}
     * <li>{t ...}
     * <li>{ts ...}
     * </ol> <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql a SQL statement that may contain one or more '?'
     *     parameter placeholders
     * @return the native form of this statement
     * @throws SQLException if a database access error occurs <p>
     */
    public synchronized String nativeSQL(final String sql)
    throws SQLException {

        // boucherb@users 20030405
        // FIXME: does not work properly for nested escapes
        //       e.g.  {call ...(...,{ts '...'},....)} does not work
        // boucherb@users 20030817
        // TESTME: First kick at the FIXME cat done.  Now lots of testing
        // and refinment
        checkClosed();

        // CHECKME:  Thow or return null if input is null?
        if (sql == null || sql.length() == 0 || sql.indexOf('{') == -1) {
            return sql;
        }

        // boolean   changed = false;
        int          state = 0;
        int          len   = sql.length();
        int          nest  = 0;
        StringBuffer sb    = new StringBuffer(sql.length());    //avoid 16 extra
        String       msg;

        //--
        final int outside_all                         = 0;
        final int outside_escape_inside_single_quotes = 1;
        final int outside_escape_inside_double_quotes = 2;

        //--
        final int inside_escape                      = 3;
        final int inside_escape_inside_single_quotes = 4;
        final int inside_escape_inside_double_quotes = 5;

        // TODO:
        // final int inside_single_line_comment          = 6;
        // final int inside_multi_line_comment           = 7;
        // Better than old way for large inputs and for avoiding GC overhead;
        // toString() reuses internal char[], reducing memory requirment
        // and garbage items 3:2
        sb.append(sql);

        for (int i = 0; i < len; i++) {
            char c = sb.charAt(i);

            switch (state) {

                case outside_all :                            // Not inside an escape or quotes
                    if (c == '\'') {
                        state = outside_escape_inside_single_quotes;
                    } else if (c == '"') {
                        state = outside_escape_inside_double_quotes;
                    } else if (c == '{') {
                        i = onStartEscapeSequence(sql, sb, i);

                        // changed = true;
                        nest++;

                        state = inside_escape;
                    }
                    break;

                case outside_escape_inside_single_quotes :    // inside ' ' only
                case inside_escape_inside_single_quotes :     // inside { } and ' '
                    if (c == '\'') {
                        state -= 1;
                    }
                    break;

                case outside_escape_inside_double_quotes :    // inside " " only
                case inside_escape_inside_double_quotes :     // inside { } and " "
                    if (c == '"') {
                        state -= 2;
                    }
                    break;

                case inside_escape :                          // inside { }
                    if (c == '\'') {
                        state = inside_escape_inside_single_quotes;
                    } else if (c == '"') {
                        state = inside_escape_inside_double_quotes;
                    } else if (c == '}') {
                        sb.setCharAt(i, ' ');

                        // changed = true;
                        nest--;

                        state = (nest == 0) ? outside_all
                                            : inside_escape;
                    } else if (c == '{') {
                        i = onStartEscapeSequence(sql, sb, i);

                        // changed = true;
                        nest++;

                        state = inside_escape;
                    }
            }
        }

        return sb.toString();
    }

    /**
     * <!-- start generic documentation -->
     * Sets this connection's auto-commit mode to the given state.
     * If a connection is in auto-commit mode, then all its SQL
     * statements will be executed and committed as individual transactions.
     * Otherwise, its SQL statements are grouped into transactions that
     * are terminated by a call to either the method <code>commit</code> or
     * the method <code>rollback</code>. By default, new connections are
     * in auto-commit mode. <p>
     *
     * The commit occurs when the statement completes or the next
     * execute occurs, whichever comes first. In the case of
     * statements returning a <code>ResultSet</code> object, the
     * statement completes when the last row of the <code>ResultSet</code>
     * object has been retrieved or the <code>ResultSet</code> object
     * has been closed. In advanced cases, a single statement may
     * return multiple results as well as output parameter values. In
     * these cases, the commit occurs when all results and output
     * parameter values have been retrieved. <p>
     *
     * <B>NOTE:</B> If this method is called during a transaction,
     * the transaction is committed. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including HSQLDB 1.7.2, <p>
     *
     * <ol>
     *   <li> All rows of a result set are retrieved internally <em>
     *   before</em> the first row can actually be fetched.<br>
     *   Therefore, a statement can be considered complete as soon as
     *   any XXXStatement.executeXXX method returns. </li>
     *
     *   <li> Multiple result sets and output parameters are not yet
     *   supported. </li>
     * </ol>
     * <p>
     *
     * (boucherb@users) </div> <!-- end release-specific
     * documentation -->
     *
     * @param autoCommit <code>true</code> to enable auto-commit
     *     mode; <code>false</code> to disable it
     * @exception SQLException if a database access error occurs
     * @see #getAutoCommit
     */
    public synchronized void setAutoCommit(boolean autoCommit)
    throws SQLException {

        checkClosed();

        try {
            sessionProxy.setAutoCommit(autoCommit);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     *  Gets the current auto-commit state.
     *
     * @return  the current state of auto-commit mode
     * @exception  SQLException Description of the Exception
     * @see  #setAutoCommit
     */
    public synchronized boolean getAutoCommit() throws SQLException {

        checkClosed();

        try {
            return sessionProxy.isAutoCommit();
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Makes all changes made since the
     * previous commit/rollback permanent and releases any database
     * locks currently held by the Connection. This method should be
     * used only when auto-commit mode has been disabled. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, savepoints are supported both
     * in SQL and via the JDBC interface. <p>
     *
     * Using SQL, savepoints may be set, released and used in rollback
     * as follows:
     *
     * <pre>
     * SAVEPOINT &lt;savepoint-name&gt;
     * RELEASE SAVEPOINT &lt;savepoint-name&gt;
     * ROLLBACK TO SAVEPOINT &lt;savepoint-name&gt;
     * </pre>
     *
     * </div><!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public synchronized void commit() throws SQLException {

        checkClosed();

        try {
            sessionProxy.commit();
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Drops all changes made since the
     * previous commit/rollback and releases any database locks
     * currently held by this Connection. This method should be used
     * only when auto- commit has been disabled. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, savepoints are fully supported both
     * in SQL and via the JDBC interface. <p>
     *
     * Using SQL, savepoints may be set, released and used in rollback
     * as follows:
     *
     * <pre>
     * SAVEPOINT &lt;savepoint-name&gt;
     * RELEASE SAVEPOINT &lt;savepoint-name&gt;
     * ROLLBACK TO SAVEPOINT &lt;savepoint-name&gt;
     * </pre>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public synchronized void rollback() throws SQLException {

        checkClosed();

        try {
            sessionProxy.rollback();
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Releases this <code>Connection</code>
     * object's database and JDBC resources immediately instead of
     * waiting for them to be automatically released.<p>
     *
     * Calling the method <code>close</code> on a <code>Connection</code>
     * object that is already closed is a no-op. <p>
     *
     * <B>Note:</B> A <code>Connection</code> object is automatically
     * closed when it is garbage collected. Certain fatal errors also
     * close a <code>Connection</code> object. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * In 1.7.2, <code>INTERNAL</code> <code>Connection</code>
     * objects are not closable from JDBC client code. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public synchronized void close() throws SQLException {

        // Changed to synchronized above because
        // we would not want a sessionProxy.close()
        // operation to occur concurrently with a
        // statementXXX.executeXXX operation.
        if (isInternal || isClosed) {
            return;
        }

        isClosed = true;

        if (sessionProxy != null) {
            sessionProxy.close();
        }

        sessionProxy   = null;
        rootWarning    = null;
        connProperties = null;
    }

    /**
     *  Tests to see if a Connection is closed.
     *
     * @return  true if the connection is closed; false if it's still
     *      open
     */
    public synchronized boolean isClosed() {
        return isClosed;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the metadata regarding this connection's database.
     * A Connection's database is able to  provide information describing
     * its tables, its supported SQL grammar, its stored procedures,
     * the capabilities of this connection, and so on. This information
     * is made available through a <code>DatabaseMetaData</code> object. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * JDBC <code>DatabaseMetaData</code> methods returning
     * <code>ResultSet</code> were not implemented fully before 1.7.2.
     * Some of these methods always returned empty result sets.
     * Other methods did not accurately
     * reflect all of the MetaData for the category.
     * Also, some method ignored the filters provided as
     * parameters, returning an unfiltered result each time. <p>
     *
     * Also, the majority of methods returning <code>ResultSet</code>
     * threw an <code>SQLException</code> when accessed by a non-admin
     * user.
     * <hr>
     *
     * Starting with HSQLDB 1.7.2, essentially full database metadata
     * is supported. <p>
     *
     * For discussion in greater detail, please follow the link to the
     * overview for jdbcDatabaseMetaData, below.
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return a DatabaseMetaData object for this Connection
     * @throws SQLException if a database access error occurs
     * @see jdbcDatabaseMetaData
     */
    public synchronized DatabaseMetaData getMetaData() throws SQLException {

        checkClosed();

        return new jdbcDatabaseMetaData(this);
    }

    /**
     * <!-- start generic documentation -->
     * Puts this connection in read-only mode as a hint to enable
     * database optimizations. <p>
     *
     * <B>Note:</B> This method should not be called while in the
     * middle of a transaction. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB will commit the current
     * transaction automatically when this method is called. <p>
     *
     * Additionally, HSQLDB provides a way to put a whole database in
     * read-only mode. This is done by manually adding the line
     * 'readonly=true' to the database's .properties file while the
     * database is offline. Upon restart, all connections will be
     * readonly, since the entire database will be readonly. To take
     * a database out of readonly mode, simply take the database
     * offline and remove the line 'readonly=true' from the
     * database's .properties file. Upon restart, the database will
     * be in regular (read-write) mode. <p>
     *
     * When a database is put in readonly mode, its files are opened
     * in readonly mode, making it possible to create CD-based
     * readonly databases. To create a CD-based readonly database
     * that has CACHED tables and whose .data file is suspected of
     * being highly fragmented, it is recommended that the database
     * first be SHUTDOWN COMPACTed before copying the database
     * files to CD. This will reduce the space required and may
     * improve access times against the .data file which holds the
     * CACHED table data. <p>
     *
     * Starting with 1.7.2, an alternate approach to opimizing the
     * .data file before creating a CD-based readonly database is to issue
     * the CHECKPOINT DEFRAG command followed by SHUTDOWN to take the
     * database offline in preparation to burn the database files to CD. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param readonly The new readOnly value
     * @exception SQLException if a database access error occurs
     */
    public synchronized void setReadOnly(boolean readonly)
    throws SQLException {

        checkClosed();

        try {
            sessionProxy.setReadOnly(readonly);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     *  Tests to see if the connection is in read-only mode.
     *
     * @return  true if connection is read-only and false otherwise
     * @exception  SQLException if a database access error occurs
     */
    public synchronized boolean isReadOnly() throws SQLException {

        try {
            return sessionProxy.isReadOnly();
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Sets a catalog name in order to
     * select a subspace of this Connection's database in which to
     * work. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not yet support catalogs and simply ignores this
     * request. <p>
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog the name of a catalog (subspace in this
     *     Connection object's database) in which to work (Ignored)
     * @throws SQLException if a database access error occurs <p>
     */
    public synchronized void setCatalog(String catalog) throws SQLException {
        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Returns the Connection's current catalog name. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not yet support catalogs and always returns null.
     * <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return the current catalog name or null <p>
     *
     *     For HSQLDB, this is always null.
     * @exception SQLException Description of the Exception
     */
    public synchronized String getCatalog() throws SQLException {

        checkClosed();

        return null;
    }

    /**
     * <!-- start generic documentation -->
     * Attempts to change the transaction isolation level for this
     * <code>Connection</code> object to the one given. The constants
     * defined in the interface <code>Connection</code> are the
     * possible transaction isolation levels. <p>
     *
     * <B>Note:</B> If this method is called during a transaction,
     * the result is implementation-defined. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * </div> <!-- end release-specific documentation -->
     *
     * @param level one of the following <code>Connection</code>
     *     constants: <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>
     *     , <code>Connection.TRANSACTION_READ_COMMITTED</code>,
     *     <code>Connection.TRANSACTION_REPEATABLE_READ</code>, or
     *     <code>Connection.TRANSACTION_SERIALIZABLE</code>. (Note
     *     that <code>Connection.TRANSACTION_NONE</code> cannot be
     *     used because it specifies that transactions are not
     *     supported.)
     * @exception SQLException if a database access error occurs or
     *     the given parameter is not one of the <code>Connection</code>
     *     constants <p>
     * @see jdbcDatabaseMetaData#supportsTransactionIsolationLevel
     * @see #getTransactionIsolation
     */
    public synchronized void setTransactionIsolation(int level)
    throws SQLException {

        checkClosed();

        switch (level) {

            case TRANSACTION_READ_UNCOMMITTED :
            case TRANSACTION_READ_COMMITTED :
            case TRANSACTION_REPEATABLE_READ :
            case TRANSACTION_SERIALIZABLE :
                break;

            default :
                throw Util.invalidArgument();
        }

        try {
            sessionProxy.setIsolation(level);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves this <code>Connection</code>
     * object's current transaction isolation level. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB always returns
     * <code>Connection.TRANSACTION_READ_UNCOMMITED</code>. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return the current transaction isolation level, which will be
     *    one of the following constants:
     *    <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>
     *    , <code>Connection.TRANSACTION_READ_COMMITTED</code>,
     *    <code>Connection.TRANSACTION_REPEATABLE_READ</code>,
     *    <code>Connection.TRANSACTION_SERIALIZABLE</code>, or
     *    <code>Connection.TRANSACTION_NONE</code> <p>
     *
     *    Up to and including 1.7.1, TRANSACTION_READ_UNCOMMITTED is
     *    always returned
     * @exception SQLException if a database access error occurs <p>
     * @see jdbcDatabaseMetaData#supportsTransactionIsolationLevel
     * @see #setTransactionIsolation setTransactionIsolation
     */
    public synchronized int getTransactionIsolation() throws SQLException {

        checkClosed();

        try {
            return sessionProxy.getIsolation();
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the first warning reported by calls on this
     * <code>Connection</code> object. If there is more than one
     * warning, subsequent warnings will be chained to the first
     * one and can be retrieved by calling the method
     * <code>SQLWarning.getNextWarning</code> on the warning
     * that was retrieved previously. <p>
     *
     * This method may not be called on a closed connection; doing so
     * will cause an <code>SQLException</code> to be thrown. <p>
     *
     * <B>Note:</B> Subsequent warnings will be chained to this
     * SQLWarning. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSQLDB produces warnings whenever a createStatement(),
     * prepareStatement() or prepareCall() invocation requests an unsupported
     * but defined combination of result set type, concurrency and holdability,
     * such that another set is substituted.
     *
     * </div> <!-- end release-specific documentation -->
     * @return the first <code>SQLWarning</code> object or <code>null</code>
     *     if there are none<p>
     * @exception SQLException if a database access error occurs or
     *     this method is called on a closed connection <p>
     * @see SQLWarning
     */
    public synchronized SQLWarning getWarnings() throws SQLException {

        checkClosed();

        synchronized (rootWarning_mutex) {
            return rootWarning;
        }
    }

    /**
     * <!-- start generic documentation -->
     * Clears all warnings reported for this <code>Connection</code>
     * object. After a call to this method, the method
     * <code>getWarnings</code> returns null until
     * a new warning is reported for this Connection. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Before HSQLDB 1.7.2, <code>SQLWarning</code> was not
     * supported, and calls to this method are simply ignored. <p>
     *
     * Starting with HSQLDB 1.7.2, the standard behaviour is implemented. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs <p>
     */
    public synchronized void clearWarnings() throws SQLException {

        checkClosed();

        synchronized (rootWarning_mutex) {
            rootWarning = null;
        }
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Creates a <code>Statement</code> object that will generate
     * <code>ResultSet</code> objects with the given type and
     * concurrency. This method is the same as the
     * <code>createStatement</code> method above, but it allows the
     * default result set type and result set concurrency type to be
     * overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to HSQLDB 1.6.1, support was provided only for type
     * <code>TYPE_FORWARD_ONLY</code>
     * and concurrency <code>CONCUR_READ_ONLY</code>. <p>
     *
     * Starting with HSQLDB 1.7.0, support is now provided for types
     * <code>TYPE_FORWARD_ONLY</code>, <I>and</I>
     * <code>TYPE_SCROLL_INSENSITIVE</code>,
     * with concurrency <code>CONCUR_READ_ONLY</code>.
     *
     * Starting with HSQLDB 1.7.2, the behaviour regarding the type and
     * concurrency values has changed to more closely conform to the
     * specification.  That is, if an unsupported combination is requested,
     * a SQLWarning is issued on this Connection and the closest supported
     * combination is used instead. <p>
     *
     * <B>Notes:</B> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed and a supported combination of type and
     * concurrency was specified. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw a
     * <code>SQLException</code> if the connection is closed.<p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param type a result set type; one of
     *  <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *  <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *  <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not
     *  supported)
     * @param concurrency a concurrency type; one of
     *  <code>ResultSet.CONCUR_READ_ONLY</code>
     *  or <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @return a new <code>Statement</code> object that will, within
     *  the release-specific documented limitations of support,
     *  generate <code>ResultSet</code> objects with the given
     *  type and concurrency
     * @exception SQLException if a database access error occurs or
     *  the given parameters are not ResultSet constants
     *  indicating a supported type and concurrency
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *  for jdbcConnection)
     */
    public synchronized Statement createStatement(int type,
            int concurrency) throws SQLException {

        checkClosed();

        type        = xlateRSType(type);
        concurrency = xlateRSConcurrency(concurrency);

        return new jdbcStatement(this, type);
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>PreparedStatement</code>  object that will
     * generate <code>ResultSet</code> objects with the given type
     * and concurrency. This method is the same as the
     * <code>prepareStatement</code> method above, but it allows the
     * default result set type and result set concurrency type to be
     * overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, the behaviour regarding the type and
     * concurrency values has changed to more closely conform to the
     * specification.  That is, if an unsupported combination is requested,
     * a SQLWarning is issued on this Connection and the closest supported
     * combination is used instead. <p>
     *
     * Also starting with 1.7.2, the support for and behaviour of
     * PreparedStatment has changed.  Please read the introductory section
     * of the documentation for org.hsqldb.jdbc.jdbcPreparedStatement.
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql a String object that is the SQL statement to be
     *  sent to the database; may contain one or more ? IN
     *  parameters
     * @param type a result set type; one of
     *  <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *  <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *  <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not
     *  supported)
     * @param concurrency a concurrency type; one of
     *  <code>ResultSet.CONCUR_READ_ONLY</code>
     *  or <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @return a new PreparedStatement object containing the
     *  pre-compiled SQL statement that will produce
     *  <code>ResultSet</code>
     *  objects with the given type and concurrency
     * @exception SQLException if a database access error occurs or
     *  the given parameters are not ResultSet constants
     *  indicating a supported type and concurrency
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *  for jdbcConnection)
     */
    public synchronized PreparedStatement prepareStatement(String sql,
            int type, int concurrency) throws SQLException {

        checkClosed();

        type        = xlateRSType(type);
        concurrency = xlateRSConcurrency(concurrency);

        try {
            return new jdbcPreparedStatement(this, sql, type);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>CallableStatement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type and concurrency. This method is the same as the
     * <code>prepareCall</code> method above, but it allows the
     * default result set type and result set concurrency type to be
     * overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, the behaviour regarding the type,
     * concurrency and holdability values has changed to more closely
     * conform to the specification.  That is, if an unsupported
     * combination is requrested, a SQLWarning is issued on this Connection
     * and the closest supported combination is used instead. <p>
     *
     * Also starting with 1.7.2, the support for and behaviour of
     * CallableStatement has changed.  Please read the introdutory section
     * of the documentation for org.hsqldb.jdbc.jdbcCallableStatement.
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql a String object that is the SQL statement to be
     * sent to the database; may contain one or more ? parameters
     * @param resultSetType a result set type; one of
     * <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, (not
     * supported) or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * (not supported)
     * @param resultSetConcurrency a concurrency type; one of
     * <code>ResultSet.CONCUR_READ_ONLY</code>
     * or <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @return a new CallableStatement object containing the
     * pre-compiled SQL statement
     * @exception SQLException if a database access error occurs or
     * the given parameters are not <code>ResultSet</code>
     * constants indicating a supported type and concurrency
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     * for jdbcConnection)
     */
    public synchronized CallableStatement prepareCall(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {

        checkClosed();

        resultSetType        = xlateRSType(resultSetType);
        resultSetConcurrency = xlateRSConcurrency(resultSetConcurrency);

        try {
            return new jdbcCallableStatement(this, sql, resultSetType);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Gets the type map object associated with this connection. Unless
     * the application has added an entry to the type map, the map
     * returned will be empty.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. Calling this
     * method always throws a <code>SQLException</code>, stating that the
     * function is not supported. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return the <code>java.util.Map</code> object associated with
     *     this <code>Connection</code> object
     * @exception SQLException if a database access error occurs
     *     (always, up to HSQLDB 1.7.0, inclusive)
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcConnection)
     */
    public synchronized Map getTypeMap() throws SQLException {

        checkClosed();

        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Installs the given <code>TypeMap</code>
     * object as the type map for this <code>Connection</code>
     * object. The type map will be used for the custom mapping of
     * SQL structured types and distinct types.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. Calling this
     * method always throws a <code>SQLException</code>, stating that
     * the function is not supported. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param map the <code>java.util.Map</code> object to install as
     *     the replacement for this <code>Connection</code> object's
     *     default type map
     * @exception SQLException if a database access error occurs or
     *     the given parameter is not a <code>java.util.Map</code>
     *     object (always, up to HSQLDB 1.7.0, inclusive)
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcConnection)
     * @see #getTypeMap
     */
    public synchronized void setTypeMap(Map map) throws SQLException {

        checkClosed();

        throw Util.notSupported();
    }

// boucherb@users 20020409 - javadocs for all JDBC 3 methods
// boucherb@users 20020509 - todo
// start adding implementations where it is easy:  Savepoints
    //--------------------------JDBC 3.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Changes the holdability of
     * <code>ResultSet</code> objects created using this
     * <code>Connection</code> object to the given holdability. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported. <p>
     *
     * As of 1.7.2, only HOLD_CURSORS_OVER_COMMIT is supported; supplying
     * any other value will throw an exception. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param holdability a <code>ResultSet</code> holdability
     *     constant; one of <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException if a database access occurs, the given
     *     parameter is not a <code>ResultSet</code> constant
     *     indicating holdability, or the given holdability is not
     *     supported
     * @see #getHoldability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized void setHoldability(int holdability)
    throws SQLException {

        checkClosed();

        switch (holdability) {

            case jdbcResultSet.HOLD_CURSORS_OVER_COMMIT :
                break;

            case jdbcResultSet.CLOSE_CURSORS_AT_COMMIT :
                String msg = "ResultSet holdability: " + holdability;    //NOI18N

                throw Util.sqlException(Trace.FUNCTION_NOT_SUPPORTED, msg);
            default :
                throw Util.invalidArgument();
        }
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the current
     * holdability of <code>ResultSet</code> objects created using
     * this <code>Connection</code> object.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported. <p>
     *
     * Calling this method always returns HOLD_CURSORS_OVER_COMMIT. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return the holdability, one of
     *     <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException if a database access occurs
     * @see #setHoldability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized int getHoldability() throws SQLException {

        checkClosed();

        return jdbcResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates an unnamed savepoint in
     * the current transaction and returns the new <code>Savepoint</code>
     * object that represents it.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * Use setSavepoint(String name) instead <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs or
     *     this <code>Connection</code> object is currently in
     *     auto-commit mode
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized Savepoint setSavepoint() throws SQLException {

        checkClosed();

        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a savepoint with the
     * given name in the current transaction and returns the new
     * <code>Savepoint</code> object that represents it. <p>
     *
     * <!-- end generic documentation -->
     *
     * @param name a <code>String</code> containing the name of the savepoint
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs or
     *     this <code>Connection</code> object is currently in
     *     auto-commit mode
     *
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized Savepoint setSavepoint(String name)
    throws SQLException {

        Result req;

        checkClosed();

        if (name == null) {
            String msg = "name is null";

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        req = Result.newSetSavepointRequest(name);

        try {
            sessionProxy.execute(req);
        } catch (HsqlException e) {
            Util.throwError(e);
        }

        return new jdbcSavepoint(name, this);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Undoes all changes made after
     * the given <code>Savepoint</code> object was set. <p>
     *
     * This method should be used only when auto-commit has been
     * disabled. <p>
     *
     * <!-- end generic documentation -->
     *
     * @param savepoint the <code>Savepoint</code> object to roll back to
     * @exception SQLException if a database access error occurs,
     *           the <code>Savepoint</code> object is no longer valid,
     *           or this <code>Connection</code> object is currently in
     *           auto-commit mode
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @see #rollback
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized void rollback(Savepoint savepoint)
    throws SQLException {

        String        msg;
        jdbcSavepoint sp;
        Result        req;

        checkClosed();

        if (savepoint == null) {
            msg = "savepoint is null";

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        try {
            if (sessionProxy.isAutoCommit()) {
                msg = "connection is autocommit";

                throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
            }
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }

// fredt - might someone call this with a Savepoint from a different driver???
        if (!(savepoint instanceof jdbcSavepoint)) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        sp = (jdbcSavepoint) savepoint;

        if (this != sp.connection) {
            msg = savepoint + " was not issued on this connection";

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        req = Result.newRollbackToSavepointRequest(sp.name);

        try {
            Result result = sessionProxy.execute(req);

            if (result.isError()) {
                Util.throwError(result);
            }
        } catch (HsqlException e) {
            Util.throwError(e);
        }
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Removes the given <code>Savepoint</code>
     * object from the current transaction. Any reference to the
     * savepoint after it have been removed will cause an
     * <code>SQLException</code> to be thrown. <p>
     *
     * <!-- end generic documentation -->
     *
     * @param savepoint the <code>Savepoint</code> object to be removed
     * @exception SQLException if a database access error occurs or
     *           the given <code>Savepoint</code> object is not a valid
     *           savepoint in the current transaction
     *
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized void releaseSavepoint(Savepoint savepoint)
    throws SQLException {

        String        msg;
        jdbcSavepoint sp;
        Result        req;

        checkClosed();

        if (savepoint == null) {
            msg = "savepoint is null";

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

// fredt - might someone call this with a Savepoint from a different driver???
        if (!(savepoint instanceof jdbcSavepoint)) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        sp = (jdbcSavepoint) savepoint;

        if (this != sp.connection) {
            msg = savepoint.getSavepointName()
                  + " was not issued on this connection";

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        req = Result.newReleaseSavepointRequest(sp.name);

        try {
            Result result = sessionProxy.execute(req);

            if (result.isError()) {
                Util.throwError(result);
            }
        } catch (HsqlException e) {
            Util.throwError(e);
        }
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a <code>Statement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type, concurrency, and holdability. This method is
     * the same as the <code>createStatement</code> method above, but
     * it allows the default result set type, concurrency, and
     * holdability to be overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported. <p>
     *
     * Starting with HSQLDB 1.7.2, the behaviour regarding the type,
     * concurrency and holdability values has changed to more closely conform
     * to the specification.  That is, if an unsupported combination is requested,
     * a SQLWarning is issued on this Connection and the closest supported
     * combination is used instead. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param resultSetType one of the following <code>ResultSet</code>
     *     constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
     *     or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
     *     <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following
     *     code>ResultSet</code>
     *     constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>Statement</code> object that will generate
     *     <code>ResultSet</code> objects with the given type,
     *     concurrency, and holdability
     * @exception SQLException if a database access error occurs or
     *     the given parameters are not <code>ResultSet</code>
     *     constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized Statement createStatement(int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {

        checkClosed();

        resultSetType        = xlateRSType(resultSetType);
        resultSetConcurrency = xlateRSConcurrency(resultSetConcurrency);
        resultSetHoldability = xlateRSHoldability(resultSetHoldability);

        return new jdbcStatement(this, resultSetType);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a <code>PreparedStatement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type, concurrency, and holdability. <p>
     *
     * This method is the same as the <code>prepareStatement</code>
     * method above, but it allows the default result set type,
     * concurrency, and holdability to be overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported. <p>
     *
     * Starting with HSQLDB 1.7.2, the behaviour regarding the type,
     * concurrency and holdability values has changed to more closely
     * conform to the specification.  That is, if an unsupported
     * combination is requested, a SQLWarning is issued on this Connection
     * and the closest supported combination is used instead. <p>
     *
     * Also starting with 1.7.2, the support for and behaviour of
     * PreparedStatment has changed.  Please read the introductory section
     * of the documentation for org.hsqldb.jdbc.jdbcPreparedStatement.
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql a <code>String</code> object that is the SQL
     *     statement to be sent to the database; may contain one or
     *     more ? IN parameters
     * @param resultSetType one of the following <code>ResultSet</code>
     *     constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
     *     or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
     *     <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled SQL statement, that will
     *     generate <code>ResultSet</code> objects with the given
     *     type, concurrency, and holdability
     * @exception SQLException if a database access error occurs or
     *     the given parameters are not <code>ResultSet</code>
     *     constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {

        checkClosed();

        resultSetType        = xlateRSType(resultSetType);
        resultSetConcurrency = xlateRSConcurrency(resultSetConcurrency);
        resultSetHoldability = xlateRSHoldability(resultSetHoldability);

        try {
            return new jdbcPreparedStatement(this, sql, resultSetType);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a <code>CallableStatement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type and concurrency. This method is the same as the
     * <code>prepareCall</code> method above, but it allows the
     * default result set type, result set concurrency type and
     * holdability to be overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported. <p>
     *
     * Starting with HSQLDB 1.7.2, the behaviour regarding the type,
     * concurrency and holdability values has changed to more closely
     * conform to the specification.  That is, if an unsupported
     * combination is requrested, a SQLWarning is issued on this Connection
     * and the closest supported combination is used instead. <p>
     *
     * Also starting with 1.7.2, the support for and behaviour of
     * CallableStatment has changed.  Please read the introdutory section
     * of the documentation for org.hsqldb.jdbc.jdbcCallableStatement.
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql a <code>String</code> object that is the SQL
     *     statement to be sent to the database; may contain on or
     *     more ? parameters
     * @param resultSetType one of the following <code>ResultSet</code>
     *     constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *     <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
     *     <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>CallableStatement</code> object,
     *     containing the pre-compiled SQL statement, that will
     *     generate <code>ResultSet</code> objects with the given
     *     type, concurrency, and holdability
     * @exception SQLException if a database access error occurs or
     *     the given parameters are not <code>ResultSet</code>
     *     constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized CallableStatement prepareCall(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {

        checkClosed();

        resultSetType        = xlateRSType(resultSetType);
        resultSetConcurrency = xlateRSConcurrency(resultSetConcurrency);
        resultSetHoldability = xlateRSHoldability(resultSetHoldability);

        try {
            return new jdbcCallableStatement(this, sql, resultSetType);
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a default <code>PreparedStatement</code>
     * object that has the capability to retrieve auto-generated
     * keys. The given constant tells the driver whether it should
     * make auto-generated keys available for retrieval. This
     * parameter is ignored if the SQL statement is not an
     * <code>INSERT</code>  statement. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which methods throw certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.
     * <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *     IN parameter placeholders
     * @param autoGeneratedKeys a flag indicating that auto-generated
     *     keys should be returned, one of
     *     code>Statement.RETURN_GENERATED_KEYS</code>
     *     or <code>Statement.NO_GENERATED_KEYS</code>.
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled SQL statement, that will have
     *     the capability of returning auto-generated keys
     * @exception SQLException if a database access error occurs or
     *     the given parameter is not a <code>Statement</code>
     *     constant indicating whether auto-generated keys should be
     *     returned
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized PreparedStatement prepareStatement(String sql,
            int autoGeneratedKeys) throws SQLException {

        checkClosed();

        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a default <code>PreparedStatement</code>
     * object capable of returning the auto-generated keys designated
     * by the given array. This array contains the indexes of the
     * columns in the target table that contain the auto-generated
     * keys that should be made available. This array is ignored if
     * the SQL statement is not an <code>INSERT</code> statement. <p>
     *
     * An SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code>
     * object. This object can then be used to efficiently execute
     * this statement multiple times. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which methods throw certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.
     * <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *     IN parameter placeholders
     * @param columnIndexes an array of column indexes indicating the
     *     columns that should be returned from the inserted row or
     *     rows
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled statement, that is capable of
     *     returning the auto-generated keys designated by the given
     *     array of column indexes
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized PreparedStatement prepareStatement(String sql,
            int[] columnIndexes) throws SQLException {

        checkClosed();

        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a default <code>PreparedStatement</code>
     * object capable of returning the auto-generated keys designated
     * by the given array. This array contains the names of the
     * columns in the target table that contain the auto-generated
     * keys that should be returned. This array is ignored if the SQL
     * statement is not an <code>INSERT</code> statement. <p>
     *
     * An SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code>
     * object. This object can then be used to efficiently execute
     * this statement multiple times. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which methods throw certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.
     * <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </div> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *     IN parameter placeholders
     * @param columnNames an array of column names indicating the
     *     columns that should be returned from the inserted row or
     *     rows
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled statement, that is capable of
     *     returning the auto-generated keys designated by the given
     *     array of column names
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.2
     */
//#ifdef JDBC3
    public synchronized PreparedStatement prepareStatement(String sql,
            String[] columnNames) throws SQLException {

        checkClosed();

        throw Util.notSupported();
    }

//#endif JDBC3
//---------------------- internal implementation ---------------------------

    /**
     * Constructs a new external <code>Connection</code> to an HSQLDB
     * <code>Database</code>. <p>
     *
     * This constructor is called on behalf of the
     * <code>java.sql.DriverManager</code> when getting a
     * <code>Connection</code> for use in normal (external)
     * client code. <p>
     *
     * Internal client code, that being code located in HSQLDB SQL
     * functions and stored procedures, receives an INTERNAL
     * connection constructed by the {@link #jdbcConnection(Session)
     * jdbcConnection(Session)} constructor. <p>
     *
     * @param props A <code>Properties</code> object containing the connection
     *      properties
     * @exception SQLException when the user/password combination is
     *     invalid, the connection url is invalid, or the
     *     <code>Database</code> is unavailable. <p>
     *
     *     The <code>Database</code> may be unavailable for a number
     *     of reasons, including network problems or the fact that it
     *     may already be in use by another process.
     */
    public jdbcConnection(HsqlProperties props) throws SQLException {

        String user     = props.getProperty("user");
        String password = props.getProperty("password");
        String connType = props.getProperty("connection_type");
        String host     = props.getProperty("host");
        int    port     = props.getIntegerProperty("port", 0);
        String path     = props.getProperty("path");
        String database = props.getProperty("database");
        boolean isTLS = (connType == DatabaseURL.S_HSQLS
                         || connType == DatabaseURL.S_HTTPS);

        if (user == null) {
            user = "SA";
        }

        if (password == null) {
            password = "";
        }

        user     = user.toUpperCase(Locale.ENGLISH);
        password = password.toUpperCase(Locale.ENGLISH);

        try {
            if (DatabaseURL.isInProcessDatabaseType(connType)) {

/** @todo fredt - this should be the only static reference to a core class in
                     *  the jdbc package - we may make it dynamic */
                sessionProxy = DatabaseManager.newSession(connType, database,
                        user, password, props);
            } else if (connType == DatabaseURL.S_HSQL
                       || connType == DatabaseURL.S_HSQLS) {
                sessionProxy = new HSQLClientConnection(host, port, path,
                        database, isTLS, user, password);
                isNetConn = true;
            } else if (connType == DatabaseURL.S_HTTP
                       || connType == DatabaseURL.S_HTTPS) {
                sessionProxy = new HTTPClientConnection(host, port, path,
                        database, isTLS, user, password);
                isNetConn = true;
            } else {    // alias: type not yet implemented
                throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
            }

            connProperties = props;
        } catch (HsqlException e) {
            throw Util.sqlException(e);
        }
    }

    /**
     * Constructs an <code>INTERNAL</code> <code>Connection</code>,
     * using the specified {@link Session Session}. <p>
     *
     * This constructor is called only on behalf of an existing
     * <code>Session</code> (the internal parallel of a
     * <code>Connection</code>), to be used as a parameter to a SQL
     * function or stored procedure that needs to execute in the context
     * of that <code>Session</code>. <p>
     *
     * When a Java SQL function or stored procedure is called and its
     * first parameter is of type <code>Connection</code>, HSQLDB
     * automatically notices this and constructs an <code>INTERNAL</code>
     * <code>Connection</code> using the current <code>Session</code>.
     * HSQLDB then passes this <code>Connection</code> in the first
     * parameter position, moving any other parameter values
     * specified in the SQL statement to the right by one position.
     * <p>
     *
     * To read more about this, see {@link Function#getValue()}. <p>
     *
     * <B>Notes:</B> <p>
     *
     * Starting with HSQLDB 1.7.2, <code>INTERNAL</code> connections are not
     * closed by a call to close() or by a SQL DISCONNECT.
     *
     * For HSQLDB developers not involved with writing database
     * internals, this change only applies to connections obtained
     * automatically from the database as the first parameter to
     * stored procedures and SQL functions. This is mainly an issue
     * to developers writing custom SQL function and stored procedure
     * libraries for HSQLDB. Presently, it is recommended that SQL function and
     * stored procedure code avoid depending on closing or issuing a
     * DISCONNECT on a connection obtained in this manner. <p>
     *
     * @param c the Session requesting the construction of this
     *     Connection
     * @exception HsqlException never (reserved for future use);
     * @see org.hsqldb.Function
     */
    public jdbcConnection(Session c) throws HsqlException {

        // PRE: Session is non-null
        isInternal   = true;
        sessionProxy = c;
    }

    /**
     *  The default implementation simply attempts to silently {@link
     *  #close() close()} this <code>Connection</code>
     */
    protected void finalize() {

        try {
            close();
        } catch (SQLException e) {}
    }

    /**
     * Retrieves this connection's JDBC url.
     *
     * This method is in support of the jdbcDatabaseMetaData.getURL() method.
     *
     * @return the database connection url with which this object was
     *      constructed
     */
    synchronized String getURL() throws SQLException {

        checkClosed();

        if (isInternal) {
            return ((Session) sessionProxy).getInternalConnectionURL();
        }

        return connProperties.getProperty("url");
    }

    /**
     * An internal check for closed connections. <p>
     *
     * @throws SQLException when the connection is closed
     */
    synchronized void checkClosed() throws SQLException {

        if (isClosed) {
            throw Util.sqlException(Trace.CONNECTION_IS_CLOSED);
        }
    }

    /**
     * Adds another SQLWarning to this Connection object's warning chain.
     *
     * @param w the SQLWarning to add to the chain
     */
    void addWarning(SQLWarning w) {

        // PRE:  w is never null
        synchronized (rootWarning_mutex) {
            if (rootWarning == null) {
                rootWarning = w;
            } else {
                rootWarning.setNextWarning(w);
            }
        }
    }

    /**
     * Clears the warning chain without checking if this Connection is closed.
     */
    void clearWarningsNoCheck() {

        synchronized (rootWarning_mutex) {
            rootWarning = null;
        }
    }

    /**
     * Translates <code>ResultSet</code> type, adding to the warning
     * chain if the requested type is downgraded. <p>
     *
     * Up to and including HSQLDB 1.7.2,  <code>TYPE_FORWARD_ONLY</code> and
     * <code>TYPE_SCROLL_INSENSITIVE</code> are passed through. <p>
     *
     * Starting with 1.7.2, while <code>TYPE_SCROLL_SENSITIVE</code> is
     * downgraded to <code>TYPE_SCROLL_INSENSITIVE</code> and an SQLWarning is
     * issued. <p>
     *
     * @param type of <code>ResultSet</code>; one of
     *     <code>jdbcResultSet.TYPE_XXX</code>
     * @return the actual type that will be used
     * @throws SQLException if type is not one of the defined values
     */
    int xlateRSType(int type) throws SQLException {

        SQLWarning w;
        String     msg;

        switch (type) {

            case jdbcResultSet.TYPE_FORWARD_ONLY :
            case jdbcResultSet.TYPE_SCROLL_INSENSITIVE : {
                return type;
            }
            case jdbcResultSet.TYPE_SCROLL_SENSITIVE : {
                msg = "TYPE_SCROLL_SENSITIVE => TYPE_SCROLL_SENSITIVE";
                w = new SQLWarning(msg, "SOO10", Trace.INVALID_JDBC_ARGUMENT);

                addWarning(w);

                return jdbcResultSet.TYPE_SCROLL_INSENSITIVE;
            }
            default : {
                msg = "ResultSet type: " + type;

                throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
            }
        }
    }

    /**
     * Translates <code>ResultSet</code> concurrency, adding to the warning
     * chain if the requested concurrency is downgraded. <p>
     *
     * Starting with HSQLDB 1.7.2, <code>CONCUR_READ_ONLY</code> is
     * passed through while <code>CONCUR_UPDATABLE</code> is downgraded
     * to <code>CONCUR_READ_ONLY</code> and an SQLWarning is issued.
     *
     * @param concurrency of <code>ResultSet</code>; one of
     *     <code>jdbcResultSet.CONCUR_XXX</code>
     * @return the actual concurrency that will be used
     * @throws SQLException if concurrency is not one of the defined values
     */
    int xlateRSConcurrency(int concurrency) throws SQLException {

        SQLWarning w;
        String     msg;

        switch (concurrency) {

            case jdbcResultSet.CONCUR_READ_ONLY : {
                return concurrency;
            }
            case jdbcResultSet.CONCUR_UPDATABLE : {
                msg = "CONCUR_UPDATABLE => CONCUR_READ_ONLY";
                w = new SQLWarning(msg, "SOO10", Trace.INVALID_JDBC_ARGUMENT);

                addWarning(w);

                return jdbcResultSet.CONCUR_READ_ONLY;
            }
            default : {
                msg = "ResultSet concurrency: " + concurrency;

                throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
            }
        }
    }

    /**
     * Translates <code>ResultSet</code> holdability, adding to the warning
     * chain if the requested holdability is changed from an unsupported to
     * a supported value. <p>
     *
     * Starting with HSQLDB 1.7.2, <code>HOLD_CURSORS_OVER_COMMIT</code> is
     * passed through while <code>CLOSE_CURSORS_AT_COMMIT</code> is changed
     * to <code>HOLD_CURSORS_OVER_COMMIT</code> and an SQLWarning is
     * issued. <p>
     *
     * @param holdability of <code>ResultSet</code>; one of
     *     <code>jdbcResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *     <code>jdbcResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return the actual holdability that will be used
     * @throws SQLException if holdability is not one of the defined values
     */
    int xlateRSHoldability(int holdability) throws SQLException {

        SQLWarning w;
        String     msg;

        switch (holdability) {

            case jdbcResultSet.HOLD_CURSORS_OVER_COMMIT : {
                return holdability;
            }
            case jdbcResultSet.CLOSE_CURSORS_AT_COMMIT : {
                msg = "CLOSE_CURSORS_AT_COMMIT => HOLD_CURSORS_OVER_COMMIT";
                w = new SQLWarning(msg, "SOO10", Trace.INVALID_JDBC_ARGUMENT);

                addWarning(w);

                return jdbcResultSet.HOLD_CURSORS_OVER_COMMIT;
            }
            default : {
                msg = "ResultSet holdability: " + holdability;

                throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
            }
        }
    }

    /**
     * Resets this connection so it can be used again. Used when connections are
     * returned to a connection pool.
     */
    public void reset() throws SQLException {

        try {
            this.sessionProxy.resetSession();
        } catch (HsqlException e) {
            throw new SQLException("Error resetting connection: "
                                   + e.getMessage());
        }
    }

    /**
     * is called from within nativeSQL when the start of an JDBC escape sequence is encountered
     */
    private int onStartEscapeSequence(String sql, StringBuffer sb,
                                      int i) throws SQLException {

        sb.setCharAt(i++, ' ');

        i = StringUtil.skipSpaces(sql, i);

        if (sql.regionMatches(true, i, "fn ", 0, 3)
                || sql.regionMatches(true, i, "oj ", 0, 3)
                || sql.regionMatches(true, i, "ts ", 0, 3)) {
            sb.setCharAt(i++, ' ');
            sb.setCharAt(i++, ' ');
        } else if (sql.regionMatches(true, i, "d ", 0, 2)
                   || sql.regionMatches(true, i, "t ", 0, 2)) {
            sb.setCharAt(i++, ' ');
        } else if (sql.regionMatches(true, i, "call ", 0, 5)) {
            i += 4;
        } else if (sql.regionMatches(true, i, "?= call ", 0, 8)) {
            sb.setCharAt(i++, ' ');
            sb.setCharAt(i++, ' ');

            i += 5;
        } else if (sql.regionMatches(true, i, "escape ", 0, 7)) {
            i += 6;
        } else {
            i--;

            throw new SQLException(
                Trace.getMessage(
                    Trace.jdbcConnection_nativeSQL, true, new Object[]{
                        sql.substring(i) }), "S0010",
                                             Trace.INVALID_JDBC_ARGUMENT);
        }

        return i;
    }
}
