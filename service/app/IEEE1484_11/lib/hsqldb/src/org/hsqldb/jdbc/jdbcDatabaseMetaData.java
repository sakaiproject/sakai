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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.Column;
import org.hsqldb.Library;
import org.hsqldb.Trace;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.persist.HsqlDatabaseProperties;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// boucherb@users 20020509 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.DatabaseMetaData interface,
// updated generic documentation to JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020409/20020505 extensive review and update
// of docs and behaviour to comply with previous and latest java.sql
// specification
// boucherb@users 2002-20030121 - extensive rewrite to support new
// 1.7.2 metadata features.
// boucherb@users 20040422 - doc 1.7.2 - javadoc updates toward 1.7.2 final
// boucherb@users 200404xx - misc changes
// fredt@users 20050505 - patch 1.8.0 - enforced JDBC rules for non-pattern params

/** Comprehensive information about the database as a whole.
 * <P>
 * This interface is implemented by driver vendors to let users know the
 * capabilities of a Database Management System (DBMS) in combination with
 * the driver based on JDBC<sup><font size=-2>TM</font></sup> technology
 * ("JDBC driver") that is used with it.  Different relational DBMSs often
 * support different features, implement features in different ways, and use
 * different data types.  In addition, a driver may implement a feature on
 * top of what the DBMS offers.  Information returned by methods in this
 * interface applies to the capabilities of a particular driver and a
 * particular DBMS working together. Note that as used in this documentation,
 * the term "database" is used generically to refer to both the driver and DBMS.
 * <P>
 * A user for this interface is commonly a tool that needs to discover how to
 * deal with the underlying DBMS.  This is especially true for applications
 * that are intended to be used with more than one DBMS. For example, a tool
 * might use the method <code>getTypeInfo</code> to find out what data types
 * can be used in a <code>CREATE TABLE</code> statement.  Or a user might call
 * the method <code>supportsCorrelatedSubqueries</code> to see if it is possible
 * to use a correlated subquery or <code>supportsBatchUpdates</code> to see if
 * it is possible to use batch updates.
 * <P>
 * Some <code>DatabaseMetaData</code> methods return lists of information
 * in the form of <code>ResultSet</code> objects. Regular <code>ResultSet</code>
 * methods, such as <code>getString</code> and <code>getInt</code>, can be used
 * to retrieve the data from these <code>ResultSet</code> objects.  If a given
 * form of metadata is not available, the <code>ResultSet</code> getter methods
 * throw an <code>SQLException</code>.
 * <P>
 * Some <code>DatabaseMetaData</code> methods take arguments that are
 * String patterns.  These arguments all have names such as fooPattern.
 * Within a pattern String, "%" means match any substring of 0 or more
 * characters, and "_" means match any one character. Only metadata
 * entries matching the search pattern are returned. If a search pattern
 * argument is set to <code>null</code>, that argument's criterion will
 * be dropped from the search.
 * <P>
 * A method that gets information about a feature that the driver does not
 * support will throw an <code>SQLException</code>.
 * In the case of methods that return a <code>ResultSet</code>
 * object, either a <code>ResultSet</code> object (which may be empty) is
 * returned or an <code>SQLException</code> is thrown.<p>
 *
 * <!-- start release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h3>HSQLDB-Specific Information:</h3> <p>
 *
 * Starting with HSQLDB 1.7.2, an option is provided to allow alternate
 * system table production implementations.  In this distribution, there are
 * three implementations whose behaviour ranges from producing no system
 * tables at all to producing a richer and more complete body of information
 * about an HSQLDB database than was previously available. The information
 * provided through the default implementation is, unlike previous
 * versions, accessible to all database users, regardless of admin status.
 * This is now possible because the table content it produces for each
 * user is pre-filtered, based on the user's access rights. That is, each
 * system table now acts like a security-aware View.<p>
 *
 * The process of installing a system table production class is transparent and
 * occurs dynamically at runtime during the opening sequence of a
 * <code>Database</code> instance, in the newDatabaseInformation() factory
 * method of the revised DatabaseInformation class, using the following
 * steps: <p>
 *
 * <div class="GeneralExample">
 * <ol>
 * <li>If a class whose fully qualified name is org.hsqldb.DatabaseInformationFull
 *     can be found and it has an accesible constructor that takes an
 *     org.hsqldb.Database object as its single parameter, then an instance of
 *     that class is reflectively instantiated and is used by the database
 *     instance to produce its system tables. <p>
 *
 * <li>If 1.) fails, then the process is repeated, attempting to create an
 *     instance of org.hsqldb.DatabaseInformationMain (which provides just the
 *     core set of system tables required to service this class, but now does
 *     so in a more security aware and comprehensive fashion). <p>
 *
 * <li>If 2.) fails, then an instance of org.hsqldb.DatabaseInformation is
 *     installed (that, by default, produces no system tables, meaning that
 *     calls to all related methods in this class will fail, throwing an
 *     SQLException stating that a required system table is not found). <p>
 *
 * </ol>
 * </div> <p>
 *
 * The process of searching for alternate implementations of database
 * support classes, ending with the installation of a minimal but functional
 * default will be refered to henceforth as <i>graceful degradation</i>.
 * This process is advantageous in that it allows developers and administrators
 * to easily choose packaging options, simply by adding to or deleting concerned
 * classes from an  HSQLDB installation, without worry over providing complex
 * initialization properties or disrupting the core operation of the engine.
 * In this particular context, <i>graceful degradation</i> allows easy choices
 * regarding database metadata, spanning the range of full (design-time),
 * custom-written, minimal (production-time) or <CODE>null</CODE>
 * (space-constrained) system table production implementations. <p>
 *
 * In the default full implementation, a number of new system tables are
 * provided that, although not used directly by this class, present previously
 * unavailable information about the database, such as about its triggers and
 * aliases. <p>
 *
 * In order to better support graphical database exploration tools and as an
 * experimental intermediate step toward more fully supporting SQL9n and
 * SQL200n, the default installed DatabaseInformation implementation
 * is also capable of reporting pseudo name space information, such as
 * the catalog (database URI) of database objects. <p>
 *
 * The catalog reporting feature is turned off by default but
 * can be turned on by providing the appropriate entries in the database
 * properties file (see the advanced topics section of the product
 * documentation). <p>
 *
 * When the feature is turned on, catalog is reported using
 * the following conventions: <p>
 *
 * <ol>
 * <li>All objects are reported as having a catalog equal to the URI of the
 *     database, which is equivalent to the catenation of the
 *     <b>&lt;type&gt;</b> and <b>&lt;path&gt;</b> portions of the HSQLDB
 *     internal JDBC connection URL.<p>
 *
 *     Examples: <p>
 *
 *     <pre class="JavaCodeExample">
 *     <span class="JavaStringLiteral">&quot;jdbc:hsqldb:file:test&quot;</span>      => <span class="JavaStringLiteral">&quot;file:test&quot;</span>
 *     <span class="JavaStringLiteral">&quot;jdbc:hsqldb:mem:.&quot;</span>          => <span class="JavaStringLiteral">&quot;mem:.&quot;</span>
 *     <span class="JavaStringLiteral">&quot;jdbc:hsqldb:hsql:/host/<alias>...&quot;</span> => URI of aliased database
 *     <span class="JavaStringLiteral">&quot;jdbc:hsqldb:http:/host/<alias>...&quot;</span> => URI of aliased database
 *     </pre>
 *
 *     <b>Note:</b> No provision is made for qualifying database objects
 *     by catalog in DML or DDL SQL.  This feature is functional only with
 *     respect to browsing the database through the DatabaseMetaData and system
 *     table interfaces. <p>
 *
 * </ol>
 *
 * Again, it should be well understood that this feature provide an
 * <i>emulation</i> of catalog support and is intended only
 * as an experimental implementation to enhance the browsing experience
 * when using graphical database explorers and to make a first foray
 * into tackling the issue of implementing true catalog support
 * in the future. <p>
 *
 * Due the nature of the new database system table production process, fewer
 * assumptions can be made by this class about what information is made
 * available in the system tables supporting <code>DatabaseMetaData</code>
 * methods. Because of this, the SQL queries behind the <code>ResultSet</code>
 * producing methods have been cleaned up and made to adhere more strictly to
 * the JDBC contracts specified in relation to the method parameters. <p>
 *
 * One of the remaining assumptions concerns the <code>approximate</code>
 * argument of {@link #getIndexInfo getIndexInfo()}. This parameter is still
 * ignored since there is not yet any process in place to internally gather
 * and persist table and index statistics.  A primitive version of a statistics
 * gathering and reporting subsystem <em>may</em> be introduced some time in the
 * 1.7.x series of releases, but no hard decision has yet been made. <p>
 *
 * Another assumption is that simple select queries against certain system
 * tables will return rows in JDBC contract order in the absence of an
 * &quot;ORDER BY&quot; clause.  The reason for this is that results
 * come back much faster when no &quot;ORDER BY&quot; clause is used.
 * Developers wishing to extend or replace an existing system table production
 * class should be aware of this, either adding the contract
 * &quot;ORDER BY&quot; clause to the SQL in corresponding methods in this class,
 * or, better, by maintaing rows in the correct order in the underlying
 * system tables, prefereably by creating appropriate primary indices. <p>
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
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of the
 * {@link java.sql.ResultSet ResultSet} interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined in
 * {@link jdbcResultSet jdbcResultSet}.<p>
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
 * // etc
 * </pre>
 *
 * However, please note that code written in such a manner will not be
 * compatible for use with other JDBC 2 drivers, since they expect and use
 * <code>ResultSet</code>, rather than <code>jdbcResultSet</code>.  Also
 * note, this feature is offered solely as a convenience to developers
 * who must work under JDK 1.1.x due to operating constraints, yet wish to
 * use some of the more advanced features available under the JDBC 2
 * specification.<p>
 *
 * (fredt@users)<br>
 * (boucherb@users)
 * </div>
 * <!-- end release-specific documentation -->
 *
 * @author boucherb@users
 * @author fredt@users
 * @version 1.7.2
 * @see org.hsqldb.DatabaseInformation
 * @see org.hsqldb.DatabaseInformationMain
 * @see org.hsqldb.DatabaseInformationFull
 */
public class jdbcDatabaseMetaData implements DatabaseMetaData {

    /** Used by getBestRowIdentifier to avoid extra object construction */
    static final Integer INT_COLUMNS_NO_NULLS = new Integer(columnNoNulls);

    // -----------------------------------------------------------------------
    // private attributes
    // -----------------------------------------------------------------------

    /**
     * The connection this object uses to retrieve database instance-specific
     * metadata.
     */
    private jdbcConnection connection;

    /**
     * Connection property for schema reporting.
     */
    private boolean useSchemaDefault;

    /**
     * A CSV list representing the SQL IN list to use when generating
     * queries for <code>getBestRowIdentifier</code> when the
     * <code>scope</code> argument is <code>bestRowSession</code>.
     * @since HSQLDB 1.7.2
     */
    private static final String BRI_SESSION_SCOPE_IN_LIST = "("
        + bestRowSession + ")";

    /**
     * A CSV list representing the SQL IN list to use when generating
     * queries for <code>getBestRowIdentifier</code> when the
     * <code>scope</code> argument is <code>bestRowTemporary</code>.
     * @since HSQLDB 1.7.2
     */
    private static final String BRI_TEMPORARY_SCOPE_IN_LIST = "("
        + bestRowTemporary + "," + bestRowTransaction + "," + bestRowSession
        + ")";

    /**
     * A CSV list representing the SQL IN list to use when generating
     * queries for <code>getBestRowIdentifier</code> when the
     * <code>scope</code> argument is <code>bestRowTransaction</code>.
     * @since HSQLDB 1.7.2
     */
    private static final String BRI_TRANSACTION_SCOPE_IN_LIST = "("
        + bestRowTransaction + "," + bestRowSession + ")";

    /**
     * "SELECT * FROM ". <p>
     *
     * This attribute is in support of methods that use SQL SELECT statements to
     * generate returned <code>ResultSet</code> objects. <p>
     *
     * @since HSQLDB 1.7.2
     */
    private static final String selstar = "SELECT * FROM INFORMATION_SCHEMA.";

    /**
     * " WHERE 1=1 ". <p>
     *
     * This attribute is in support of methods that use SQL SELECT statements to
     * generate returned <code>ResultSet</code> objects. <p>
     *
     * A good optimizer will simply drop this when parsing a condition
     * expression. And it makes our code much easier to write, since we don't
     * have to check our "WHERE" clause productions as strictly for proper
     * conjunction:  we just stick additional conjunctive predicates on the
     * end of this and Presto! Everything works :-) <p>
     * @since HSQLDB 1.7.2
     */
    private static final String whereTrue = " WHERE 1=1";

    //----------------------------------------------------------------------
    // First, a variety of minor information about the target database.

    /**
     * Retrieves whether the current user can call all the procedures
     * returned by the method <code>getProcedures</code>.  <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * This method still <em>always</em> returns
     * <code>true</code>. <p>
     *
     * In a future release, the plugin interface may be modified to allow
     * implementors to report different values here, based on their
     * implementations.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean allProceduresAreCallable() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether the current user can use all the tables returned
     * by the method <code>getTables</code> in a <code>SELECT</code>
     * statement.  <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB always reports <code>true</code>.<p>
     *
     * Please note that the default 1.7.2 <code>getTables</code> behaviour is
     * omit from the list of <em>requested</em> tables only those to which the
     * invoking user has <em>no</em> access of any kind. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    }

    /**
     * Retrieves the URL for this DBMS.
     *
     *
     * @return the URL for this DBMS or <code>null</code> if it cannot be
     *         generated
     * @exception SQLException if a database access error occurs
     */
    public String getURL() throws SQLException {
        return connection.getURL();
    }

    /**
     * Retrieves the user name as known to this database.
     *
     *
     * @return the database user name
     * @exception SQLException if a database access error occurs
     */
    public String getUserName() throws SQLException {

        ResultSet rs = execute("CALL USER()");

        rs.next();

        String result = rs.getString(1);

        rs.close();

        return result;
    }

    /**
     * Retrieves whether this database is in read-only mode. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, this makes
     * an SQL call to the new {@link Library#isReadOnlyDatabase} method
     * which provides correct determination of the read-only status for
     * both local and remote database instances.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {

        ResultSet rs =
            execute("CALL \"org.hsqldb.Library.isReadOnlyDatabase\"()");

        rs.next();

        boolean result = rs.getBoolean(1);

        rs.close();

        return result;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted high.
     * Sorted high means that <code>NULL</code> values
     * sort higher than any other value in a domain.  In an ascending order,
     * if this method returns <code>true</code>,  <code>NULL</code> values
     * will appear at the end. By contrast, the method
     * <code>nullsAreSortedAtEnd</code> indicates whether <code>NULL</code> values
     * are sorted at the end regardless of sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted low.
     * Sorted low means that <code>NULL</code> values
     * sort lower than any other value in a domain.  In an ascending order,
     * if this method returns <code>true</code>,  <code>NULL</code> values
     * will appear at the beginning. By contrast, the method
     * <code>nullsAreSortedAtStart</code> indicates whether <code>NULL</code> values
     * are sorted at the beginning regardless of sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedLow() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted at the start regardless
     * of sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted at the end regardless of
     * sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    /**
     * Retrieves the name of this database product. <p>
     *
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this value is retrieved through an
     * SQL call to the new {@link Library#getDatabaseProductName} method
     * which allows correct determination of the database product name
     * for both local and remote database instances.
     * </div> <p>
     *
     * @return database product name
     * @exception SQLException if a database access error occurs
     */
    public String getDatabaseProductName() throws SQLException {

        ResultSet rs =
            execute("call \"org.hsqldb.Library.getDatabaseProductName\"()");

        rs.next();

        String result = rs.getString(1);

        rs.close();

        return result;
    }

    /**
     * Retrieves the version number of this database product. <p>
     *
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this value is retrieved through an
     * SQL call to the new {@link Library#getDatabaseProductVersion} method
     * which allows correct determination of the database product name
     * for both local and remote database instances.
     * </div> <p>
     *
     * @return database version number
     * @exception SQLException if a database access error occurs
     */
    public String getDatabaseProductVersion() throws SQLException {

        ResultSet rs = execute(
            "call \"org.hsqldb.Library.getDatabaseProductVersion\"()");

        rs.next();

        String result = rs.getString(1);

        rs.close();

        return result;
    }

    /**
     * Retrieves the name of this JDBC driver.
     *
     * @return JDBC driver name
     * @exception SQLException if a database access error occurs
     */
    public String getDriverName() throws SQLException {
        return HsqlDatabaseProperties.PRODUCT_NAME + " Driver";
    }

    /**
     * Retrieves the version number of this JDBC driver as a <code>String</code>.
     *
     * @return JDBC driver version
     * @exception SQLException if a database access error occurs
     */
    public String getDriverVersion() throws SQLException {
        return HsqlDatabaseProperties.THIS_VERSION;
    }

    /**
     * Retrieves this JDBC driver's major version number.
     *
     * @return JDBC driver major version
     */
    public int getDriverMajorVersion() {
        return HsqlDatabaseProperties.MAJOR;
    }

    /**
     * Retrieves this JDBC driver's minor version number.
     *
     * @return JDBC driver minor version number
     */
    public int getDriverMinorVersion() {
        return HsqlDatabaseProperties.MINOR;
    }

    /**
     * Retrieves whether this database stores tables in a local file. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * From HSQLDB 1.7.2 it is assumed that this refers to data being stored
     * by the JDBC client. This method always returns false.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database uses a file for each table. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not use a file for each table.
     * This method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if this database uses a local file for each table;
     *      <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case sensitive and as a result stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in upper case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in lower case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case sensitive and as a result stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in upper case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in lower case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    /**
     * Retrieves the string used to quote SQL identifiers.
     * This method returns a space " " if identifier quoting is not supported. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB uses the standard SQL identifier quote character
     * (the double quote character); this method always returns <b>"</b>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return the quoting string or a space if quoting is not supported
     * @exception SQLException if a database access error occurs
     */
    public String getIdentifierQuoteString() throws SQLException {
        return "\"";
    }

//fredt@users 20020429 - JavaDoc comment - in 1.7.1 there are keywords such
// as TEMP, TEXT, CACHED that are not SQL 92 keywords

    /**
     * Retrieves a comma-separated list of all of this database's SQL keywords
     * that are NOT also SQL92 keywords. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * The list returned contains HSQLDB keywords that are not in the list
     * of reserved words. Some of these are in the list reserved
     * words for SQL 2003 but are not SQL92 keywords.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the list of this database's keywords that are not also
     *       SQL92 keywords
     * @exception SQLException if a database access error occurs
     */
    public String getSQLKeywords() throws SQLException {

        return "BEFORE,BIGINT,BINARY,CACHED,DATETIME,"
               + "LIMIT,LONGVARBINARY,LONGVARCHAR,OBJECT,OTHER,SAVEPOINT,"
               + "TEMP,TEXT,TOP,TRIGGER,TINYINT,VARBINARY,VARCHAR_IGNORECASE";
    }

    /**
     * Retrieves a comma-separated list of math functions available with
     * this database.  These are the Open Group CLI math function names used in
     * the JDBC function escape clause.
     * @return the list of math functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getNumericFunctions() throws SQLException {
        return StringUtil.getList(Library.sNumeric, ",", "");
    }

    /**
     * Retrieves a comma-separated list of string functions available with
     * this database.  These are the  Open Group CLI string function names used
     * in the JDBC function escape clause.
     * @return the list of string functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getStringFunctions() throws SQLException {
        return StringUtil.getList(Library.sString, ",", "");
    }

    /**
     * Retrieves a comma-separated list of system functions available with
     * this database.  These are the  Open Group CLI system function names used
     * in the JDBC function escape clause.
     * @return a list of system functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getSystemFunctions() throws SQLException {
        return StringUtil.getList(Library.sSystem, ",", "");
    }

    /**
     * Retrieves a comma-separated list of the time and date functions available
     * with this database.
     * @return the list of time and date functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getTimeDateFunctions() throws SQLException {
        return StringUtil.getList(Library.sTimeDate, ",", "");
    }

    /**
     * Retrieves the string that can be used to escape wildcard characters.
     * This is the string that can be used to escape '_' or '%' in
     * the catalog search parameters that are a pattern (and therefore use one
     * of the wildcard characters).
     *
     * <P>The '_' character represents any single character;
     * the '%' character represents any sequence of zero or
     * more characters. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB uses the "\" character to escape wildcard characters.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the string used to escape wildcard characters
     * @exception SQLException if a database access error occurs
     */
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    /**
     * Retrieves all the "extra" characters that can be used in unquoted
     * identifier names (those beyond a-z, A-Z, 0-9 and _). <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not support using any "extra" characters in unquoted
     * identifier names; this method always returns the empty String.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the string containing the extra characters
     * @exception SQLException if a database access error occurs
     */
    public String getExtraNameCharacters() throws SQLException {
        return "";
    }

    //--------------------------------------------------------------------
    // Functions describing which features are supported.

    /**
     * Retrieves whether this database supports <code>ALTER TABLE</code>
     * with add column. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * From 1.7.0, HSQLDB supports this type of
     * <code>ALTER TABLE</code> statement; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports <code>ALTER TABLE</code>
     * with drop column. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * From 1.7.0, HSQLDB supports this type of
     * <code>ALTER TABLE</code> statement; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports column aliasing.
     *
     * <P>If so, the SQL AS clause can be used to provide names for
     * computed columns or to provide alias names for columns as
     * required. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports column aliasing; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports concatenations between
     * <code>NULL</code> and non-<code>NULL</code> values being
     * <code>NULL</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports this; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports the <code>CONVERT</code>
     * function between SQL types. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports conversions; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsConvert() throws SQLException {
        return true;
    }

// fredt@users - JD comment - I think this is unsupported at the moment
// because SQL92 says conversion is implementation dependent, so if
// conversion from DOUBLE to INTEGER were possbible we would return the
// whole number part, but we currently throw. I'm not so sure about
// conversions between string and numeric where it is logically possible
// only if the string represents a numeric value

    /**
     * Retrieves whether this database supports the <code>CONVERT</code>
     * for two given SQL types. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports conversion though String intermediates, so everything
     * should be possible, short of number format errors (all Java objects
     * have a toString method); this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @param fromType the type to convert from; one of the type codes from
     *       the class <code>java.sql.Types</code>
     * @param toType the type to convert to; one of the type codes from
     *       the class <code>java.sql.Types</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public boolean supportsConvert(int fromType,
                                   int toType) throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports table correlation names. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports table correlation names; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether, when table correlation names are supported, they
     * are restricted to being different from the names of the tables. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB requires that table correlation names are different from the
     * names of the tables; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDifferentTableCorrelationNames()
    throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports expressions in
     * <code>ORDER BY</code> lists. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports expressions in <code>ORDER BY</code> lists; this
     * method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports using a column that is
     * not in the <code>SELECT</code> statement in an
     * <code>ORDER BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports using a column that is not in the <code>SELECT</code>
     * statement in an <code>ORDER BY</code> clause; this method always
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports some form of
     * <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports using the <code>GROUP BY</code> clause; this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports using a column that is
     * not in the <code>SELECT</code> statement in a
     * <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports using a column that is
     * not in the <code>SELECT</code> statement in a
     * <code>GROUP BY</code> clause; this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports using columns not included in
     * the <code>SELECT</code> statement in a <code>GROUP BY</code> clause
     * provided that all of the columns in the <code>SELECT</code> statement
     * are included in the <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports using columns not included in
     * the <code>SELECT</code> statement in a <code>GROUP BY</code> clause
     * provided that all of the columns in the <code>SELECT</code> statement
     * are included in the <code>GROUP BY</code> clause; this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports specifying a
     * <code>LIKE</code> escape clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports specifying a
     * <code>LIKE</code> escape clause; this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports getting multiple
     * <code>ResultSet</code> objects from a single call to the
     * method <code>execute</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support getting multiple
     * <code>ResultSet</code> objects from a single call to the
     * method <code>execute</code>; this method
     * always returns <code>false</code>. <p>
     *
     * This behaviour <i>may</i> change in a future release.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database allows having multiple
     * transactions open at once (on different connections). <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB allows having multiple
     * transactions open at once (on different connections); this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether columns in this database may be defined as
     * non-nullable. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports the specification of non-nullable columns; this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports the ODBC Minimum SQL grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support the ODBC
     * Minimum SQL grammar; this method
     * always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports the ODBC Core SQL grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * From 1.7.2 this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports the ODBC Extended SQL grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support the ODBC
     * Extended SQL grammar; this method
     * always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 entry level SQL
     * grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support the ANSI92 entry
     * level SQL grammar; this method
     * always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 intermediate SQL
     * grammar supported. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support the ANSI92
     * intermediate SQL grammar; this method always returns
     * <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 full SQL
     * grammar supported. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support the ANSI92
     * full SQL grammar; this method always returns
     * <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

// fredt@users 20030413 - return value change to support OpenOffice.org

    /**
     * Retrieves whether this database supports the SQL Integrity
     * Enhancement Facility. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * From 1.7.2, this method always returns
     * <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsIntegrityEnhancementFacility()
    throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports some form of outer join. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports outer joins; this method always returns
     * <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports full nested outer joins. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB  does not support full nested outer
     * joins; this method always returns <code>false</code>. <p>
     *
     * This behaviour may change in a future release.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database provides limited support for outer
     * joins.  (This will be <code>true</code> if the method
     * <code>supportsFullOuterJoins</code> returns <code>true</code>). <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB support the LEFT OUTER join syntax;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    /**
     * Retrieves the database vendor's preferred term for "schema". <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.8.0, HSQLDB provides support for schemas.
     * </div>
     * <!-- end release-specific documentation -->
     * @return the vendor term for "schema"
     * @exception SQLException if a database access error occurs
     */
    public String getSchemaTerm() throws SQLException {
        return "SCHEMA";
    }

    /**
     * Retrieves the database vendor's preferred term for "procedure". <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support declaration of
     * functions or procedures directly in SQL but instead relies on the
     * HSQLDB-specific CLASS grant mechanism to make public static
     * Java methods available as SQL routines; this method always returns
     * an empty <code>String</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return the vendor term for "procedure"
     * @exception SQLException if a database access error occurs
     */
    public String getProcedureTerm() throws SQLException {
        return "";
    }

    /**
     * Retrieves the database vendor's preferred term for "catalog". <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support catalogs in DDL or DML;
     * this method <ewm>always</em> returns the empty String.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the vendor term for "catalog"
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogTerm() throws SQLException {
        return "";
    }

    /**
     * Retrieves whether a catalog appears at the start of a fully qualified
     * table name.  If not, the catalog appears at the end. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalogs in DDL or DML;
     * this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if the catalog name appears at the beginning
     *        of a fully qualified table name; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }

    /**
     * Retrieves the <code>String</code> that this database uses as the
     * separator between a catalog and table name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support catalogs in DDL or DML;
     * this method <em>always</em> returns an empty <code>String</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the separator string
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogSeparator() throws SQLException {
        return "";
    }

    /**
     * Retrieves whether a schema name can be used in a data
     * manipulation statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * In 1.8.0, HSQLDB supports schemas in table names but not in column names;
     * this method always returns <code>false</code>.
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInDataManipulation() throws SQLException {

        // false for OOo client server compatibility
        // otherwise schema name is used by OOo in column references
        return false;
    }

    /**
     * Retrieves whether a schema name can be used in a procedure call
     * statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support schema-qualified
     * procedure identifiers; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a schema name can be used in a table
     * definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * In 1.8.0, HSQLDB supports schemas;
     * this method always returns <code>true</code>.
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return !useSchemaDefault;
    }

    /**
     * Retrieves whether a schema name can be used in an index
     * definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * In 1.8.0, HSQLDB supports schemas;
     * this method always returns <code>true</code>.
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return !useSchemaDefault;
    }

    /**
     * Retrieves whether a schema name can be used in a privilege
     * definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * In 1.8.0, HSQLDB supports schemas;
     * this method always returns <code>true</code>.
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInPrivilegeDefinitions()
    throws SQLException {
        return !useSchemaDefault;
    }

    /**
     * Retrieves whether a catalog name can be used in a data
     * manipulation statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalog-qualified;
     * data manipulation; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a
     * procedure call statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalog-qualified
     * procedure calls; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a
     * table definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalog-qualified
     * table definitions; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in an
     * index definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalog-qualified
     * index definitions; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a
     * privilege definition statement.  <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalog-qualified
     * privilege definitions; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInPrivilegeDefinitions()
    throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports positioned <code>DELETE</code>
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports positioned <code>UPDATE</code>
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports <code>SELECT FOR UPDATE</code>
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support explicit locking;
     * this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports stored procedure calls
     * that use the stored procedure escape syntax. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB supports calling public static
     * Java methods in the context of SQL Stored Procedures; this method
     * always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcPreparedStatement
     * @see jdbcConnection#prepareCall
     */
    public boolean supportsStoredProcedures() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in comparison
     * expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB has always supported subqueries in comparison expressions;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in
     * <code>EXISTS</code> expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB has always supported subqueries in <code>EXISTS</code>
     * expressions; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }

    /**
     * JDBC4 correction: Retrieves whether this database supports subqueries in
     * <code>IN</code> expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB has always supported subqueries in <code>IN</code>
     * statements; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in quantified
     * expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB has always supported subqueries in quantified
     * expressions; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports correlated subqueries. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB has always supported correlated subqueries;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports SQL <code>UNION</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports SQL <code>UNION</code>;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsUnion() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports SQL <code>UNION ALL</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports SQL <code>UNION ALL</code>;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsUnionAll() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports keeping cursors open
     * across commits. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support keeping
     * cursors open across commits; this method always returns
     * <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if cursors always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports keeping cursors open
     * across rollbacks. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support keeping
     * cursors open across rollbacks;
     * this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if cursors always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports keeping statements open
     * across commits. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports keeping statements open
     * across commits;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if statements always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports keeping statements open
     * across rollbacks. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports keeping statements open
     * across commits;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if statements always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenStatementsAcrossRollback()
    throws SQLException {
        return true;
    }

    //----------------------------------------------------------------------
    // The following group of methods exposes various limitations
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    /**
     * Retrieves the maximum number of hex characters this database allows in an
     * inline binary literal. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return max the maximum length (in hex characters) for a binary literal;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxBinaryLiteralLength() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows
     * for a character literal. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed for a character literal;
     *     a result of zero means that there is no limit or the limit is
     *     not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxCharLiteralLength() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows
     * for a column name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed for a column name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnNameLength() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a
     * <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInGroupBy() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in
     * an index. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInIndex() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in an
     * <code>ORDER BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInOrderBy() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a
     * <code>SELECT</code> list. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInSelect() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in
     * a table. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInTable() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of concurrent connections to this
     * database that are possible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of active connections possible at one time;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxConnections() throws SQLException {

        // hard limit is (probably) Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * cursor name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed in a cursor name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of bytes this database allows for an
     * index, including all of the parts of the index. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit;
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of bytes allowed; this limit includes the
     *     composite of all the constituent parts of the index;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * schema name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * 1.8.0 supports schema names with no known limit imposed,
     * so this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return the maximum number of characters allowed in a schema name;
     *    a result of zero means that there is no limit or the limit
     *    is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * procedure name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed in a procedure name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * catalog name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support catalogs in
     * DDL or DML; this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed in a catalog name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of bytes this database allows in
     * a single row. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit;
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of bytes allowed for a row; a result of
     *        zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxRowSize() throws SQLException {
        return 0;
    }

    /**
     * Retrieves whether the return value for the method
     * <code>getMaxRowSize</code> includes the SQL data types
     * <code>LONGVARCHAR</code> and <code>LONGVARBINARY</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Indormation:</h3><p>
     *
     * Including 1.7.2, {@link #getMaxRowSize} <em>always</em> returns
     * 0, indicating that the maximum row size is unknown or has no limit.
     * This applies to the above types as well; this method <em>always</em>
     * returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * an SQL statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed for an SQL statement;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of active statements to this database
     * that can be open at the same time. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit;
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of statements that can be open at one time;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * a table name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed for a table name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxTableNameLength() throws SQLException {
        return 0;
    }

    /**
     * Retrieves the maximum number of tables this database allows in a
     * <code>SELECT</code> statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of tables allowed in a <code>SELECT</code>
     *        statement; a result of zero means that there is no limit or
     *        the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxTablesInSelect() throws SQLException {

        // - soft limit is >>> than will ever be seen in any real stmnt
        // - exists a fixed (non statement dependent) hard limit?  No.
        // - depends totally on number of table idents that can fit in
        // Integer.MAX_VALUE characters, minus the rest of the stmnt
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * a user name. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the maximum number of characters allowed for a user name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxUserNameLength() throws SQLException {

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    //----------------------------------------------------------------------

    /**
     * Retrieves this database's default transaction isolation level.  The
     * possible values are defined in <code>java.sql.Connection</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information</h3>
     *
     * Including 1.7.2, HSQLDB supports only TRANSACTION_READ_UNCOMMITED
     * and <em>always</em> returns this value here.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the default isolation level
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     */
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_READ_UNCOMMITTED;
    }

    /**
     * Retrieves whether this database supports transactions. If not, invoking the
     * method <code>commit</code> is a noop, and the isolation level is
     * <code>TRANSACTION_NONE</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports transactions;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if transactions are supported;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsTransactions() throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database supports the given transaction
     * isolation level. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information</h3>
     * HSQLDB supports <code>TRANSACTION_READ_UNCOMMITED</code> in all cases
     * and the rest of the isolation levels where there is only one connection
     * to the database.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @param level one of the transaction isolation levels defined in
     *         <code>java.sql.Connection</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     */
    public boolean supportsTransactionIsolationLevel(int level)
    throws SQLException {

        return level == Connection.TRANSACTION_READ_UNCOMMITTED
               || level == Connection.TRANSACTION_READ_COMMITTED
               || level == Connection.TRANSACTION_REPEATABLE_READ
               || level == Connection.TRANSACTION_SERIALIZABLE;
    }

    /**
     * Retrieves whether this database supports both data definition and
     * data manipulation statements within a transaction. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB does not support a mix of both data definition and
     * data manipulation statements within a transaction.  DDL commits the
     * current transaction before proceding;
     * this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions()
    throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports only data manipulation
     * statements within a transaction. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB supports only data manipulation
     * statements within a transaction.  DDL commits the
     * current transaction before proceeding, while DML does not;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDataManipulationTransactionsOnly()
    throws SQLException {
        return true;
    }

    /**
     * Retrieves whether a data definition statement within a transaction forces
     * the transaction to commit. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, a data definition statement within a transaction forces
     * the transaction to commit; this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionCausesTransactionCommit()
    throws SQLException {
        return true;
    }

    /**
     * Retrieves whether this database ignores a data definition statement
     * within a transaction. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, a data definition statement is not ignored within a
     * transaction.  Rather, a data definition statement within a
     * transaction forces the transaction to commit; this method
     * <em>always</em> returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    /**
     * Retrieves a description of the stored procedures available in the given
     * catalog.
     * <P>
     * Only procedure descriptions matching the schema and
     * procedure name criteria are returned.  They are ordered by
     * <code>PROCEDURE_SCHEM</code> and <code>PROCEDURE_NAME</code>.
     *
     * <P>Each procedure description has the the following columns:
     * <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be <code>null</code>)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be <code>null</code>)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     * <LI> reserved for future use
     * <LI> reserved for future use
     * <LI> reserved for future use
     *  <LI><B>REMARKS</B> String => explanatory comment on the procedure
     *  <LI><B>PROCEDURE_TYPE</B> short => kind of procedure:
     *    <UL>
     *    <LI> procedureResultUnknown - May return a result
     *    <LI> procedureNoResult - Does not return a result
     *    <LI> procedureReturnsResult - Returns a result
     *    </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *      is stored in the database; "" retrieves those without a catalog;
     *      <code>null</code> means that the catalog name should not be used
     *      to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *      as it is stored in the database; "" retrieves those without a
     *      schema; <code>null</code> means that the schema name should not be
     *      used to narrow the search
     * @param procedureNamePattern a procedure name pattern; must match the
     *      procedure name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a procedure description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getProcedures(String catalog, String schemaPattern,
                                   String procedureNamePattern)
                                   throws SQLException {

        if (wantsIsNull(procedureNamePattern)) {
            return executeSelect("SYSTEM_PROCEDURES", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select = toQueryPrefix("SYSTEM_PROCEDURES").append(
            and("PROCEDURE_CAT", "=", catalog)).append(
            and("PROCEDURE_SCHEM", "LIKE", schemaPattern)).append(
            and("PROCEDURE_NAME", "LIKE", procedureNamePattern));

        // By default, query already returns the result ordered by
        // PROCEDURE_SCHEM, PROCEDURE_NAME...
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the given catalog's stored procedure parameter
     * and result columns.
     *
     * <P>Only descriptions matching the schema, procedure and
     * parameter name criteria are returned.  They are ordered by
     * PROCEDURE_SCHEM and PROCEDURE_NAME. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> is a parameter description or
     * column description with the following fields:
     * <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be <code>null</code>)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be <code>null</code>)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     *  <LI><B>COLUMN_NAME</B> String => column/parameter name
     *  <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
     *     <UL>
     *     <LI> procedureColumnUnknown - nobody knows
     *     <LI> procedureColumnIn - IN parameter
     *     <LI> procedureColumnInOut - INOUT parameter
     *     <LI> procedureColumnOut - OUT parameter
     *     <LI> procedureColumnReturn - procedure return value
     *     <LI> procedureColumnResult - result column in <code>ResultSet</code>
     *     </UL>
     * <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
     * type name is fully qualified
     *  <LI><B>PRECISION</B> int => precision
     *  <LI><B>LENGTH</B> int => length in bytes of data
     *  <LI><B>SCALE</B> short => scale
     *  <LI><B>RADIX</B> short => radix
     *  <LI><B>NULLABLE</B> short => can it contain NULL.
     *     <UL>
     *     <LI> procedureNoNulls - does not allow NULL values
     *     <LI> procedureNullable - allows NULL values
     *     <LI> procedureNullableUnknown - nullability unknown
     *     </UL>
     *  <LI><B>REMARKS</B> String => comment describing parameter/column
     * </OL>
     *
     * <P><B>Note:</B> Some databases may not return the column
     * descriptions for a procedure. Additional columns beyond
     * REMARKS can be defined by the database. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param procedureNamePattern a procedure name pattern; must match the
     *       procedure name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *       name as it is stored in the database
     * @return <code>ResultSet</code> - each row describes a stored procedure
     *       parameter or column
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getProcedureColumns(String catalog,
                                         String schemaPattern,
                                         String procedureNamePattern,
                                         String columnNamePattern)
                                         throws SQLException {

        if (wantsIsNull(procedureNamePattern)
                || wantsIsNull(columnNamePattern)) {
            return executeSelect("SYSTEM_PROCEDURECOLUMNS", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select = toQueryPrefix("SYSTEM_PROCEDURECOLUMNS").append(
            and("PROCEDURE_CAT", "=", catalog)).append(
            and("PROCEDURE_SCHEM", "LIKE", schemaPattern)).append(
            and("PROCEDURE_NAME", "LIKE", procedureNamePattern)).append(
            and("COLUMN_NAME", "LIKE", columnNamePattern));

        // By default, query already returns result ordered by
        // PROCEDURE_SCHEM and PROCEDURE_NAME...
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the tables available in the given catalog.
     * Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
     * <P>
     * Each table description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *                  "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  <LI><B>REMARKS</B> String => explanatory comment on the table
     * <LI><B>TYPE_CAT</B> String => the types catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String => the types schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String => type name (may be <code>null</code>)
     * <LI><B>SELF_REFERENCING_COL_NAME</B> String => name of the designated
     *                 "identifier" column of a typed table (may be <code>null</code>)
     *  <LI><B>REF_GENERATION</B> String => specifies how values in
     *                 SELF_REFERENCING_COL_NAME are created. Values are
     *                 "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
     * </OL>
     *
     * <P><B>Note:</B> Some databases may not return information for
     * all tables. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.0, HSQLDB returns extra information on TEXT tables
     * in the REMARKS column. <p>
     *
     * Since 1.7.0, HSQLDB includes the new JDBC3 columns TYPE_CAT,
     * TYPE_SCHEM, TYPE_NAME and SELF_REFERENCING_COL_NAME in anticipation
     * of JDBC3 compliant tools. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param tableNamePattern a table name pattern; must match the
     *       table name as it is stored in the database
     * @param types a list of table types to include; <code>null</code> returns
     *       all types
     * @return <code>ResultSet</code> - each row is a table description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getTables(String catalog, String schemaPattern,
                               String tableNamePattern,
                               String[] types) throws SQLException {

        if (wantsIsNull(tableNamePattern)
                || (types != null && types.length == 0)) {
            return executeSelect("SYSTEM_TABLES", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select =
            toQueryPrefix("SYSTEM_TABLES").append(and("TABLE_CAT", "=",
                catalog)).append(and("TABLE_SCHEM", "LIKE",
                                     schemaPattern)).append(and("TABLE_NAME",
                                         "LIKE", tableNamePattern));

        if (types == null) {

            // do not use to narrow search
        } else {
            select.append(" AND TABLE_TYPE IN (").append(
                StringUtil.getList(types, ",", "'")).append(')');
        }

        // By default, query already returns result ordered by
        // TABLE_TYPE, TABLE_SCHEM and TABLE_NAME...
        return execute(select.toString());
    }

    /**
     * Retrieves the schema names available in this database.  The results
     * are ordered by schema name.
     *
     * <P>The schema column is:
     * <OL>
     * <LI><B>TABLE_SCHEM</B> String => schema name
     * <LI><B>TABLE_CATALOG</B> String => catalog name (may be <code>null</code>)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * In 1.8.0, the list of schemas is returned.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return a <code>ResultSet</code> object in which each row is a
     *        schema decription
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getSchemas() throws SQLException {

        // By default, query already returns the result in contract order
        return executeSelect("SYSTEM_SCHEMAS", null);
    }

    /**
     * Retrieves the catalog names available in this database.  The results
     * are ordered by catalog name.
     *
     * <P>The catalog column is:
     * <OL>
     *    <LI><B>TABLE_CAT</B> String => catalog name
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return a <code>ResultSet</code> object in which each row has a
     *        single <code>String</code> column that is a catalog name
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getCatalogs() throws SQLException {
        return executeSelect("SYSTEM_CATALOGS", null);
    }

    /**
     * Retrieves the table types available in this database.  The results
     * are ordered by table type.
     *
     * <P>The table type is:
     * <OL>
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *                  "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Since 1.7.1, HSQLDB reports: "TABLE", "VIEW" and "GLOBAL TEMPORARY"
     * types.
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     * @return a <code>ResultSet</code> object in which each row has a
     *        single <code>String</code> column that is a table type
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getTableTypes() throws SQLException {

        // system table producer returns rows in contract order
        return executeSelect("SYSTEM_TABLETYPES", null);
    }

    /**
     * Retrieves a description of table columns available in
     * the specified catalog.
     *
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * <code>TABLE_SCHEM</code>, <code>TABLE_NAME</code>, and
     * <code>ORDINAL_POSITION</code>.
     *
     * <P>Each column description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     * for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
     *      types this is the maximum number of characters, for numeric or
     *      decimal types this is precision.
     *  <LI><B>BUFFER_LENGTH</B> is not used.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => is NULL allowed.
     *    <UL>
     *    <LI> columnNoNulls - might not allow <code>NULL</code> values
     *    <LI> columnNullable - definitely allows <code>NULL</code> values
     *    <LI> columnNullableUnknown - nullability unknown
     *    </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be <code>null</code>)
     *  <LI><B>COLUMN_DEF</B> String => default value (may be <code>null</code>)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *     maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int => index of column in table
     *    (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
     *    does not allow NULL values; "YES" means the column might
     *    allow NULL values.  An empty string means nobody knows.
     * <LI><B>SCOPE_CATLOG</B> String => catalog of table that is the scope
     *    of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_SCHEMA</B> String => schema of table that is the scope
     *    of a reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SCOPE_TABLE</B> String => table name that this the scope
     *    of a reference attribure (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SOURCE_DATA_TYPE</B> short => source type of a distinct type or user-generated
     *    Ref type, SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     *    isn't DISTINCT or user-generated REF)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.0, HSQLDB includes the new JDBC 3 columns SCOPE_CATLOG,
     * SCOPE_SCHEMA, SCOPE_TABLE and SOURCE_DATA_TYPE in anticipation
     * of JDBC 3 compliant tools.  However, these columns are never filled in;
     * the engine does not support the related features. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *      is stored in the database; "" retrieves those without a catalog;
     *      <code>null</code> means that the catalog name should not be used
     *      to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *      as it is stored in the database; "" retrieves those without a
     *      schema; <code>null</code> means that the schema name should not be
     *      used to narrow the search
     * @param tableNamePattern a table name pattern; must match the
     *      table name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *      name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
                                String tableNamePattern,
                                String columnNamePattern)
                                throws SQLException {

        if (wantsIsNull(tableNamePattern) || wantsIsNull(columnNamePattern)) {
            return executeSelect("SYSTEM_COLUMNS", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select = toQueryPrefix("SYSTEM_COLUMNS").append(
            and("TABLE_CAT", "=", catalog)).append(
            and("TABLE_SCHEM", "LIKE", schemaPattern)).append(
            and("TABLE_NAME", "LIKE", tableNamePattern)).append(
            and("COLUMN_NAME", "LIKE", columnNamePattern));

        // by default, query already returns the result ordered
        // by TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the access rights for a table's columns.
     *
     * <P>Only privileges matching the column name criteria are
     * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>GRANTOR</B> => grantor of access (may be <code>null</code>)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT,
     *     INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
     *     to grant to others; "NO" if not; <code>null</code> if unknown
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schema a schema name; must match the schema name as it is
     *       stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used
     *       to narrow the search
     * @param table a table name; must match the table name as it is
     *       stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *       name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column privilege
     *       description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumnPrivileges(String catalog, String schema,
                                         String table,
                                         String columnNamePattern)
                                         throws SQLException {

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        if (wantsIsNull(columnNamePattern)) {
            return executeSelect("SYSTEM_COLUMNPRIVILEGES", "0=1");
        }

        schema = translateSchema(schema);

        StringBuffer select = toQueryPrefix("SYSTEM_COLUMNPRIVILEGES").append(
            and("TABLE_CAT", "=", catalog)).append(
            and("TABLE_SCHEM", "=", schema)).append(
            and("TABLE_NAME", "=", table)).append(
            and("COLUMN_NAME", "LIKE", columnNamePattern));

        // By default, the query already returns the result
        // ordered by column name, privilege...
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the access rights for each table available
     * in a catalog. Note that a table privilege applies to one or
     * more columns in the table. It would be wrong to assume that
     * this privilege applies to all columns (this may be true for
     * some systems but is not true for all.)
     *
     * <P>Only privileges matching the schema and table name
     * criteria are returned.  They are ordered by TABLE_SCHEM,
     * TABLE_NAME, and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>GRANTOR</B> => grantor of access (may be <code>null</code>)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT,
     *     INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
     *     to grant to others; "NO" if not; <code>null</code> if unknown
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param tableNamePattern a table name pattern; must match the
     *       table name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a table privilege
     *       description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                        String tableNamePattern)
                                        throws SQLException {

        if (wantsIsNull(tableNamePattern)) {
            return executeSelect("SYSTEM_TABLEPRIVILEGES", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select = toQueryPrefix("SYSTEM_TABLEPRIVILEGES").append(
            and("TABLE_CAT", "=", catalog)).append(
            and("TABLE_SCHEM", "LIKE", schemaPattern)).append(
            and("TABLE_NAME", "LIKE", tableNamePattern));

        // By default, the query already returns a result ordered by
        // TABLE_SCHEM, TABLE_NAME, and PRIVILEGE...
        return execute(select.toString());
    }

    /**
     * Retrieves a description of a table's optimal set of columns that
     * uniquely identifies a row. They are ordered by SCOPE.
     *
     * <P>Each column description has the following columns:
     * <OL>
     *  <LI><B>SCOPE</B> short => actual scope of result
     *     <UL>
     *     <LI> bestRowTemporary - very temporary, while using row
     *     <LI> bestRowTransaction - valid for remainder of current transaction
     *     <LI> bestRowSession - valid for remainder of current session
     *     </UL>
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     * for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => not used
     *  <LI><B>DECIMAL_DIGITS</B> short  => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column
     *     like an Oracle ROWID
     *     <UL>
     *     <LI> bestRowUnknown - may or may not be pseudo column
     *     <LI> bestRowNotPseudo - is NOT a pseudo column
     *     <LI> bestRowPseudo - is a pseudo column
     *     </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not
     *       be used to narrow the search
     * @param table a table name; must match the table name as it is stored
     *       in the database
     * @param scope the scope of interest; use same values as SCOPE
     * @param nullable include columns that are nullable.
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getBestRowIdentifier(String catalog, String schema,
                                          String table, int scope,
                                          boolean nullable)
                                          throws SQLException {

        String scopeIn;

        switch (scope) {

            case bestRowTemporary :
                scopeIn = BRI_TEMPORARY_SCOPE_IN_LIST;
                break;

            case bestRowTransaction :
                scopeIn = BRI_TRANSACTION_SCOPE_IN_LIST;
                break;

            case bestRowSession :
                scopeIn = BRI_SESSION_SCOPE_IN_LIST;
                break;

            default :
                throw Util.sqlException(Trace.ASSERT_FAILED,
                                        Trace.JDBC_INVALID_BRI_SCOPE, null);
        }

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        schema = translateSchema(schema);

        Integer Nullable = (nullable) ? null
                                      : INT_COLUMNS_NO_NULLS;
        StringBuffer select = toQueryPrefix(
            "SYSTEM_BESTROWIDENTIFIER").append(
            and("TABLE_CAT", "=", catalog)).append(
            and("TABLE_SCHEM", "=", schema)).append(
            and("TABLE_NAME", "=", table)).append(
            and("NULLABLE", "=", Nullable)).append(
            " AND SCOPE IN " + scopeIn);

        // By default, query already returns rows in contract order.
        // However, the way things are set up, there should never be
        // a result where there is > 1 distinct scope value:  most requests
        // will want only one table and the system table producer (for
        // now) guarantees that a maximum of one BRI scope column set is
        // produced for each table
        return execute(select.toString());
    }

    /**
     * Retrieves a description of a table's columns that are automatically
     * updated when any value in a row is updated.  They are
     * unordered.
     *
     * <P>Each column description has the following columns:
     * <OL>
     *  <LI><B>SCOPE</B> short => is not used
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from <code>java.sql.Types</code>
     *  <LI><B>TYPE_NAME</B> String => Data source-dependent type name
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => length of column value in bytes
     *  <LI><B>DECIMAL_DIGITS</B> short  => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => whether this is pseudo column
     *     like an Oracle ROWID
     *     <UL>
     *     <LI> versionColumnUnknown - may or may not be pseudo column
     *     <LI> versionColumnNotPseudo - is NOT a pseudo column
     *     <LI> versionColumnPseudo - is a pseudo column
     *     </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param table a table name; must match the table name as it is stored
     *       in the database
     * @return a <code>ResultSet</code> object in which each row is a
     *        column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getVersionColumns(String catalog, String schema,
                                       String table) throws SQLException {

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        schema = translateSchema(schema);

        StringBuffer select =
            toQueryPrefix("SYSTEM_VERSIONCOLUMNS").append(and("TABLE_CAT",
                "=", catalog)).append(and("TABLE_SCHEM", "=",
                                          schema)).append(and("TABLE_NAME",
                                              "=", table));

        // result does not need to be ordered
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the given table's primary key columns.  They
     * are ordered by COLUMN_NAME.
     *
     * <P>Each primary key column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within primary key
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used
     *        to narrow the search
     * @param schema a schema name; must match the schema name
     *        as it is stored in the database; "" retrieves those without a
     *        schema; <code>null</code> means that the schema name should not
     *        be used to narrow the search
     * @param table a table name; must match the table name as it is stored
     *        in the database
     * @return <code>ResultSet</code> - each row is a primary key column
     *        description
     * @exception SQLException if a database access error occurs
     * @see #supportsMixedCaseQuotedIdentifiers
     * @see #storesUpperCaseIdentifiers
     */

// fredt@users 20020226 - comment - changed query to exact name
    public ResultSet getPrimaryKeys(String catalog, String schema,
                                    String table) throws SQLException {

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        schema = translateSchema(schema);

        StringBuffer select =
            toQueryPrefix("SYSTEM_PRIMARYKEYS").append(and("TABLE_CAT", "=",
                catalog)).append(and("TABLE_SCHEM", "=",
                                     schema)).append(and("TABLE_NAME", "=",
                                         table));

        // By default, query already returns result in contract order
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the primary key columns that are
     * referenced by a table's foreign key columns (the primary keys
     * imported by a table).  They are ordered by PKTABLE_CAT,
     * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each primary key column description has the following columns:
     * <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog
     *     being imported (may be <code>null</code>)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema
     *     being imported (may be <code>null</code>)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *     being imported
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *     being imported
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be <code>null</code>)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be <code>null</code>)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within a foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to a
     *      foreign key when the primary key is updated:
     *     <UL>
     *     <LI> importedNoAction - do not allow update of primary
     *              key if it has been imported
     *     <LI> importedKeyCascade - change imported key to agree
     *              with primary key update
     *     <LI> importedKeySetNull - change imported key to <code>NULL</code>
     *              if its primary key has been updated
     *     <LI> importedKeySetDefault - change imported key to default values
     *              if its primary key has been updated
     *     <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                (for ODBC 2.x compatibility)
     *     </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *     the foreign key when primary is deleted.
     *     <UL>
     *     <LI> importedKeyNoAction - do not allow delete of primary
     *              key if it has been imported
     *     <LI> importedKeyCascade - delete rows that import a deleted key
     *     <LI> importedKeySetNull - change imported key to NULL if
     *              its primary key has been deleted
     *     <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                (for ODBC 2.x compatibility)
     *     <LI> importedKeySetDefault - change imported key to default if
     *              its primary key has been deleted
     *     </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be <code>null</code>)
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *     constraints be deferred until commit
     *     <UL>
     *     <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *     <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *     <LI> importedKeyNotDeferrable - see SQL92 for definition
     *     </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param table a table name; must match the table name as it is stored
     *       in the database
     * @return <code>ResultSet</code> - each row is a primary key column
     *       description
     * @exception SQLException if a database access error occurs
     * @see #getExportedKeys
     * @see #supportsMixedCaseQuotedIdentifiers
     * @see #storesUpperCaseIdentifiers
     */
    public ResultSet getImportedKeys(String catalog, String schema,
                                     String table) throws SQLException {

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        schema = translateSchema(schema);

        StringBuffer select = toQueryPrefix("SYSTEM_CROSSREFERENCE").append(
            and("FKTABLE_CAT", "=", catalog)).append(
            and("FKTABLE_SCHEM", "=", schema)).append(
            and("FKTABLE_NAME", "=", table)).append(
            " ORDER BY PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME, KEY_SEQ");

        return execute(select.toString());
    }

    /**
     * Retrieves a description of the foreign key columns that reference the
     * given table's primary key columns (the foreign keys exported by a
     * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
     * FKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     * <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be <code>null</code>)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be <code>null</code>)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *    being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *    being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *     foreign key when primary is updated:
     *    <UL>
     *    <LI> importedNoAction - do not allow update of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - change imported key to agree
     *             with primary key update
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been updated
     *    <LI> importedKeySetDefault - change imported key to default values
     *             if its primary key has been updated
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *    the foreign key when primary is deleted.
     *    <UL>
     *    <LI> importedKeyNoAction - do not allow delete of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - delete rows that import a deleted key
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been deleted
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    <LI> importedKeySetDefault - change imported key to default if
     *             its primary key has been deleted
     *    </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be <code>null</code>)
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *    constraints be deferred until commit
     *    <UL>
     *    <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *    <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *    <LI> importedKeyNotDeferrable - see SQL92 for definition
     *    </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *      is stored in this database; "" retrieves those without a catalog;
     *      <code>null</code> means that the catalog name should not be used
     *      to narrow the search
     * @param schema a schema name; must match the schema name
     *      as it is stored in the database; "" retrieves those without a
     *      schema; <code>null</code> means that the schema name should not be
     *      used to narrow the search
     * @param table a table name; must match the table name as it is stored
     *      in this database
     * @return a <code>ResultSet</code> object in which each row is a
     *       foreign key column description
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys
     * @see #supportsMixedCaseQuotedIdentifiers
     * @see #storesUpperCaseIdentifiers
     */
    public ResultSet getExportedKeys(String catalog, String schema,
                                     String table) throws SQLException {

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        schema = translateSchema(schema);

        StringBuffer select =
            toQueryPrefix("SYSTEM_CROSSREFERENCE").append(and("PKTABLE_CAT",
                "=", catalog)).append(and("PKTABLE_SCHEM", "=",
                                          schema)).append(and("PKTABLE_NAME",
                                              "=", table));

        // By default, query already returns the table ordered by
        // FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and KEY_SEQ.
        return execute(select.toString());
    }

    /**
     * Retrieves a description of the foreign key columns in the given foreign key
     * table that reference the primary key columns of the given primary key
     * table (describe how one table imports another's key). This
     * should normally return a single foreign key/primary key pair because
     * most tables import a foreign key from a table only once.  They
     * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
     * KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     * <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be <code>null</code>)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be <code>null</code>)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *    being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *    being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *     foreign key when primary is updated:
     *    <UL>
     *    <LI> importedNoAction - do not allow update of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - change imported key to agree
     *             with primary key update
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been updated
     *    <LI> importedKeySetDefault - change imported key to default values
     *             if its primary key has been updated
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *    the foreign key when primary is deleted.
     *    <UL>
     *    <LI> importedKeyNoAction - do not allow delete of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - delete rows that import a deleted key
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been deleted
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    <LI> importedKeySetDefault - change imported key to default if
     *             its primary key has been deleted
     *    </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be <code>null</code>)
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *    constraints be deferred until commit
     *    <UL>
     *    <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *    <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *    <LI> importedKeyNotDeferrable - see SQL92 for definition
     *    </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param primaryCatalog a catalog name; must match the catalog name
     * as it is stored in the database; "" retrieves those without a
     *      catalog; <code>null</code> means drop catalog name from the
     *      selection criteria
     * @param primarySchema a schema name; must match the schema name as
     * it is stored in the database; "" retrieves those without a schema;
     * <code>null</code> means drop schema name from the selection criteria
     * @param primaryTable the name of the table that exports the key; must
     *      match the table name as it is stored in the database
     * @param foreignCatalog a catalog name; must match the catalog name as
     * it is stored in the database; "" retrieves those without a
     *      catalog; <code>null</code> means drop catalog name from the
     *      selection criteria
     * @param foreignSchema a schema name; must match the schema name as it
     * is stored in the database; "" retrieves those without a schema;
     * <code>null</code> means drop schema name from the selection criteria
     * @param foreignTable the name of the table that imports the key; must
     *      match the table name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a foreign key column
     *      description
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys
     * @see #supportsMixedCaseQuotedIdentifiers
     * @see #storesUpperCaseIdentifiers
     */
    public ResultSet getCrossReference(String primaryCatalog,
                                       String primarySchema,
                                       String primaryTable,
                                       String foreignCatalog,
                                       String foreignSchema,
                                       String foreignTable)
                                       throws SQLException {

        if (primaryTable == null || foreignTable == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        primarySchema = translateSchema(primarySchema);
        foreignSchema = translateSchema(foreignSchema);

        StringBuffer select = toQueryPrefix("SYSTEM_CROSSREFERENCE").append(
            and("PKTABLE_CAT", "=", primaryCatalog)).append(
            and("PKTABLE_SCHEM", "=", primarySchema)).append(
            and("PKTABLE_NAME", "=", primaryTable)).append(
            and("FKTABLE_CAT", "=", foreignCatalog)).append(
            and("FKTABLE_SCHEM", "=", foreignSchema)).append(
            and("FKTABLE_NAME", "=", foreignTable));

        // by default, query already returns the table ordered by
        // FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and KEY_SEQ.
        return execute(select.toString());
    }

    /**
     * Retrieves a description of all the standard SQL types supported by
     * this database. They are ordered by DATA_TYPE and then by how
     * closely the data type maps to the corresponding JDBC SQL type.
     *
     * <P>Each type description has the following columns:
     * <OL>
     *  <LI><B>TYPE_NAME</B> String => Type name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>PRECISION</B> int => maximum precision
     *  <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
     *     (may be <code>null</code>)
     *  <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
     * (may be <code>null</code>)
     *  <LI><B>CREATE_PARAMS</B> String => parameters used in creating
     *     the type (may be <code>null</code>)
     *  <LI><B>NULLABLE</B> short => can you use NULL for this type.
     *     <UL>
     *     <LI> typeNoNulls - does not allow NULL values
     *     <LI> typeNullable - allows NULL values
     *     <LI> typeNullableUnknown - nullability unknown
     *     </UL>
     *  <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive.
     *  <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
     *     <UL>
     *     <LI> typePredNone - No support
     *     <LI> typePredChar - Only supported with WHERE .. LIKE
     *     <LI> typePredBasic - Supported except for WHERE .. LIKE
     *     <LI> typeSearchable - Supported for all WHERE ..
     *     </UL>
     *  <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned.
     *  <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value.
     *  <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
     *     auto-increment value.
     *  <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
     *     (may be <code>null</code>)
     *  <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
     *  <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return a <code>ResultSet</code> object in which each row is an SQL
     *        type description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getTypeInfo() throws SQLException {

        // system table producer returns rows in contract order
        return executeSelect("SYSTEM_TYPEINFO", null);
    }

    /**
     * Retrieves a description of the given table's indices and statistics. They are
     * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
     *
     * <P>Each index column description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>NON_UNIQUE</B> boolean => Can index values be non-unique.
     *     false when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_QUALIFIER</B> String => index catalog (may be <code>null</code>);
     *     <code>null</code> when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_NAME</B> String => index name; <code>null</code> when TYPE is
     *     tableIndexStatistic
     *  <LI><B>TYPE</B> short => index type:
     *     <UL>
     *     <LI> tableIndexStatistic - this identifies table statistics that are
     *          returned in conjuction with a table's index descriptions
     *     <LI> tableIndexClustered - this is a clustered index
     *     <LI> tableIndexHashed - this is a hashed index
     *     <LI> tableIndexOther - this is some other style of index
     *     </UL>
     *  <LI><B>ORDINAL_POSITION</B> short => column sequence number
     *     within index; zero when TYPE is tableIndexStatistic
     *  <LI><B>COLUMN_NAME</B> String => column name; <code>null</code> when TYPE is
     *     tableIndexStatistic
     *  <LI><B>ASC_OR_DESC</B> String => column sort sequence, "A" => ascending,
     *     "D" => descending, may be <code>null</code> if sort sequence is not supported;
     *     <code>null</code> when TYPE is tableIndexStatistic
     *  <LI><B>CARDINALITY</B> int => When TYPE is tableIndexStatistic, then
     *     this is the number of rows in the table; otherwise, it is the
     *     number of unique values in the index.
     *  <LI><B>PAGES</B> int => When TYPE is  tableIndexStatisic then
     *     this is the number of pages used for the table, otherwise it
     *     is the number of pages used for the current index.
     *  <LI><B>FILTER_CONDITION</B> String => Filter condition, if any.
     *     (may be <code>null</code>)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in this database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in this database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param table a table name; must match the table name as it is stored
     *       in this database
     * @param unique when true, return only indices for unique values;
     *    when false, return indices regardless of whether unique or not
     * @param approximate when true, result is allowed to reflect approximate
     *    or out of date values; when false, results are requested to be
     *    accurate
     * @return <code>ResultSet</code> - each row is an index column description
     * @exception SQLException if a database access error occurs
     * @see #supportsMixedCaseQuotedIdentifiers
     * @see #storesUpperCaseIdentifiers
     */

// fredt@users 20020526 - comment - changed to exact table name
    public ResultSet getIndexInfo(String catalog, String schema,
                                  String table, boolean unique,
                                  boolean approximate) throws SQLException {

        if (table == null) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        schema = translateSchema(schema);

        Boolean nu = (unique) ? Boolean.FALSE
                              : null;
        StringBuffer select =
            toQueryPrefix("SYSTEM_INDEXINFO").append(and("TABLE_CAT", "=",
                catalog)).append(and("TABLE_SCHEM", "=",
                                     schema)).append(and("TABLE_NAME", "=",
                                         table)).append(and("NON_UNIQUE",
                                             "=", nu));

        // By default, this query already returns the table ordered by
        // NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION...
        return execute(select.toString());
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * Retrieves whether this database supports the given result set type. <p>
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     * @since  JDK 1.2 (JDK 1.1.x developers: read the new overview
     *      for jdbcDatabaseMetaData)
     */
    public boolean supportsResultSetType(int type) throws SQLException {
        return (type == jdbcResultSet.TYPE_FORWARD_ONLY
                || type == jdbcResultSet.TYPE_SCROLL_INSENSITIVE);
    }

    /**
     * Retrieves whether this database supports the given concurrency type
     * in combination with the given result set type. <p>
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @param concurrency type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     * @since  JDK 1.2 (JDK 1.1.x developers: read the new overview
     *      for jdbcDatabaseMetaData)
     */
    public boolean supportsResultSetConcurrency(int type,
            int concurrency) throws SQLException {
        return supportsResultSetType(type)
               && concurrency == jdbcResultSet.CONCUR_READ_ONLY;
    }

    /**
     * Retrieves whether for the given type of <code>ResultSet</code> object,
     * the result set's own updates are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a result set's own deletes are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether a result set's own inserts are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether updates made by others are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates made by others
     *       are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether deletes made by others are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes made by others
     *       are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether inserts made by others are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts made by others
     *        are visible for the given result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether or not a visible row update can be detected by
     * calling the method <code>ResultSet.rowUpdated</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if changes are detected by the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether or not a visible row delete can be detected by
     * calling the method <code>ResultSet.rowDeleted</code>.  If the method
     * <code>deletesAreDetected</code> returns <code>false</code>, it means that
     * deleted rows are removed from the result set. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable
     * result sets; this method <em>always</em> returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes are detected by the given result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether or not a visible row insert can be detected
     * by calling the method <code>ResultSet.rowInserted</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if changes are detected by the specified result
     *        set type; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    /**
     * Retrieves whether this database supports batch updates. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSQLDB supports batch updates;
     * this method always returns <code>true</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if this database supports batch upcates;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }

    /**
     * Retrieves a description of the user-defined types (UDTs) defined
     * in a particular schema.  Schema-specific UDTs may have type
     * <code>JAVA_OBJECT</code>, <code>STRUCT</code>,
     * or <code>DISTINCT</code>.
     *
     * <P>Only types matching the catalog, schema, type name and type
     * criteria are returned.  They are ordered by DATA_TYPE, TYPE_SCHEM
     * and TYPE_NAME.  The type name parameter may be a fully-qualified
     * name.  In this case, the catalog and schemaPattern parameters are
     * ignored.
     *
     * <P>Each type description has the following columns:
     * <OL>
     *  <LI><B>TYPE_CAT</B> String => the type's catalog (may be <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => type's schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name
     * <LI><B>CLASS_NAME</B> String => Java class name
     *  <LI><B>DATA_TYPE</B> String => type value defined in java.sql.Types.
     *    One of JAVA_OBJECT, STRUCT, or DISTINCT
     *  <LI><B>REMARKS</B> String => explanatory comment on the type
     * <LI><B>BASE_TYPE</B> short => type code of the source type of a
     *    DISTINCT type or the type that implements the user-generated
     *    reference type of the SELF_REFERENCING_COLUMN of a structured
     *    type as defined in java.sql.Types (<code>null</code> if DATA_TYPE is not
     *    DISTINCT or not STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     * </OL>
     *
     * <P><B>Note:</B> If the driver does not support UDTs, an empty
     * result set is returned. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support UDTs and
     * thus produces an empty result. <p>
     *
     * Starting with 1.7.2, there is an option to support this feature
     * to greater or lesser degrees.  See the documentation specific to the
     * selected system table provider implementation. The default implementation
     * is org.hsqldb.DatabaseInformationFull.
     * </div>
     * <!-- end release-specific documentation -->
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schemaPattern a schema pattern name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param typeNamePattern a type name pattern; must match the type name
     *       as it is stored in the database; may be a fully qualified name
     * @param types a list of user-defined types (JAVA_OBJECT,
     *       STRUCT, or DISTINCT) to include; <code>null</code> returns
     *      all types
     * @return <code>ResultSet</code> object in which each row describes a UDT
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public ResultSet getUDTs(String catalog, String schemaPattern,
                             String typeNamePattern,
                             int[] types) throws SQLException {

        if (wantsIsNull(typeNamePattern)
                || (types != null && types.length == 0)) {
            executeSelect("SYSTEM_UDTS", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select =
            toQueryPrefix("SYSTEM_UDTS").append(and("TYPE_CAT", "=",
                catalog)).append(and("TYPE_SCHEM", "LIKE",
                                     schemaPattern)).append(and("TYPE_NAME",
                                         "LIKE", typeNamePattern));

        if (types == null) {

            // do not use to narrow search
        } else {
            select.append(" AND DATA_TYPE IN (").append(
                StringUtil.getList(types, ",", "'")).append(')');
        }

        // By default, the query already returns a result ordered by
        // DATA_TYPE, TYPE_SCHEM, and TYPE_NAME...
        return execute(select.toString());
    }

    /**
     * Retrieves the connection that produced this metadata object. <p>
     *
     * @return the connection that produced this metadata object
     * @exception SQLException if a database access error occurs
     * @since  JDK 1.2 (JDK 1.1.x developers: read the new overview
     *      for jdbcDatabaseMetaData)
     */
    public Connection getConnection() throws SQLException {
        return connection;
    }

// boucherb@users 20020426 - javadocs for all JDBC 3 methods
// boucherb@users 20020426 - todos
    // ------------------- JDBC 3.0 -------------------------

    /**
     * Retrieves whether this database supports savepoints. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Beginning with 1.7.2, this SQL feature is supported
     * through JDBC as well as SQL. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if savepoints are supported;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean supportsSavepoints() throws SQLException {
        return true;
    }

//#endif JDBC3

    /**
     * Retrieves whether this database supports named parameters to callable
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSQLDB supports JDBC named parameters to
     * callable statements; this method returns true. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if named parameters are supported;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean supportsNamedParameters() throws SQLException {
        return true;
    }

//#endif JDBC3

    /**
     * Retrieves whether it is possible to have multiple <code>ResultSet</code>
     * objects returned from a <code>CallableStatement</code> object
     * simultaneously. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support multiple ResultSet
     * objects returned from a <code>CallableStatement</code> object at all;
     * this method always returns <code>false</code>. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if a <code>CallableStatement</code> object
     *        can return multiple <code>ResultSet</code> objects
     *        simultaneously; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

//#endif JDBC3

    /**
     * Retrieves whether auto-generated keys can be retrieved after
     * a statement has been executed. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support retrieving
     * autogenerated keys through the JDBC interface at all, although
     * it is possible to retrieve them in a proprietary fashion;
     * this method always returns <code>false</code>. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if auto-generated keys can be retrieved
     *        after a statement has executed; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }

//#endif JDBC3

    /**
     * Retrieves a description of the user-defined type (UDT) hierarchies defined in a
     * particular schema in this database. Only the immediate super type
     * sub type relationship is modeled.
     * <P>
     * Only supertype information for UDTs matching the catalog,
     * schema, and type name is returned. The type name parameter
     * may be a fully-qualified name. When the UDT name supplied is a
     * fully-qualified name, the catalog and schemaPattern parameters are
     * ignored.
     * <P>
     * If a UDT does not have a direct super type, it is not listed here.
     * A row of the <code>ResultSet</code> object returned by this method
     * describes the designated UDT and a direct supertype. A row has the following
     * columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String => the UDT's catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String => UDT's schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String => type name of the UDT
     * <LI><B>SUPERTYPE_CAT</B> String => the direct super type's catalog
     *                          (may be <code>null</code>)
     * <LI><B>SUPERTYPE_SCHEM</B> String => the direct super type's schema
     *                            (may be <code>null</code>)
     * <LI><B>SUPERTYPE_NAME</B> String => the direct super type's name
     * </OL>
     *
     * <P><B>Note:</B> If the driver does not support type hierarchies, an
     * empty result set is returned. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Including 1.7.1, this JDBC feature is not supported; calling
     * this method throws a SQLException stating that the operation
     * is not supported. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; "" retrieves those without a catalog;
     *       <code>null</code> means drop catalog name from the selection
     *       criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     *       without a schema
     * @param typeNamePattern a UDT name pattern; may be a fully-qualified
     *       name
     * @return a <code>ResultSet</code> object in which a row gives information
     *        about the designated UDT
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public ResultSet getSuperTypes(String catalog, String schemaPattern,
                                   String typeNamePattern)
                                   throws SQLException {

        if (wantsIsNull(typeNamePattern)) {
            return executeSelect("SYSTEM_SUPERTYPES", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select =
            toQueryPrefix("SYSTEM_SUPERTYPES").append(and("TYPE_CAT", "=",
                catalog)).append(and("TYPE_SCHEM", "LIKE",
                                     schemaPattern)).append(and("TYPE_NAME",
                                         "LIKE", typeNamePattern));

        return execute(select.toString());
    }

//#endif JDBC3

    /**
     * Retrieves a description of the table hierarchies defined in a particular
     * schema in this database.
     *
     * <P>Only supertable information for tables matching the catalog, schema
     * and table name are returned. The table name parameter may be a fully-
     * qualified name, in which case, the catalog and schemaPattern parameters
     * are ignored. If a table does not have a super table, it is not listed here.
     * Supertables have to be defined in the same catalog and schema as the
     * sub tables. Therefore, the type description does not need to include
     * this information for the supertable.
     *
     * <P>Each type description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String => the type's catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String => type's schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String => type name
     * <LI><B>SUPERTABLE_NAME</B> String => the direct super type's name
     * </OL>
     *
     * <P><B>Note:</B> If the driver does not support type hierarchies, an
     * empty result set is returned. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; "" retrieves those without a catalog;
     *       <code>null</code> means drop catalog name from the selection
     *       criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     *       without a schema
     * @param tableNamePattern a table name pattern; may be a fully-qualified
     *       name
     * @return a <code>ResultSet</code> object in which each row is a type
     *       description
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public ResultSet getSuperTables(String catalog, String schemaPattern,
                                    String tableNamePattern)
                                    throws SQLException {

        if (wantsIsNull(tableNamePattern)) {
            return executeSelect("SYSTEM_SUPERTABLES", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select =
            toQueryPrefix("SYSTEM_SUPERTABLES").append(and("TABLE_CAT", "=",
                catalog)).append(and("TABLE_SCHEM", "LIKE",
                                     schemaPattern)).append(and("TABLE_NAME",
                                         "LIKE", tableNamePattern));

        return execute(select.toString());
    }

//#endif JDBC3

    /**
     * Retrieves a description of the given attribute of the given type
     * for a user-defined type (UDT) that is available in the given schema
     * and catalog.
     * <P>
     * Descriptions are returned only for attributes of UDTs matching the
     * catalog, schema, type, and attribute name criteria. They are ordered by
     * TYPE_SCHEM, TYPE_NAME and ORDINAL_POSITION. This description
     * does not contain inherited attributes.
     * <P>
     * The <code>ResultSet</code> object that is returned has the following
     * columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String => type catalog (may be <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => type schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name
     *  <LI><B>ATTR_NAME</B> String => attribute name
     *  <LI><B>DATA_TYPE</B> short => attribute type SQL type from java.sql.Types
     *  <LI><B>ATTR_TYPE_NAME</B> String => Data source dependent type name.
     * For a UDT, the type name is fully qualified. For a REF, the type name is
     * fully qualified and represents the target type of the reference type.
     *  <LI><B>ATTR_SIZE</B> int => column size.  For char or date
     *      types this is the maximum number of characters; for numeric or
     *      decimal types this is precision.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => whether NULL is allowed
     *     <UL>
     *     <LI> attributeNoNulls - might not allow NULL values
     *     <LI> attributeNullable - definitely allows NULL values
     *     <LI> attributeNullableUnknown - nullability unknown
     *     </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be <code>null</code>)
     *  <LI><B>ATTR_DEF</B> String => default value (may be <code>null</code>)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *      maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int => index of column in table
     *     (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
     *     does not allow NULL values; "YES" means the column might
     *     allow NULL values.  An empty string means unknown.
     * <LI><B>SCOPE_CATALOG</B> String => catalog of table that is the
     *     scope of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_SCHEMA</B> String => schema of table that is the
     *     scope of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_TABLE</B> String => table name that is the scope of a
     *     reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SOURCE_DATA_TYPE</B> short => source type of a distinct type or user-generated
     *     Ref type,SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     *     isn't DISTINCT or user-generated REF)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores
     * them in upper case; it treats quoted identifiers as case sensitive and
     * stores them verbatim. All jdbcDatabaseMetaData methods perform
     * case-sensitive comparison between name (pattern) arguments and the
     * corresponding identifier values as they are stored in the database.
     * Therefore, care must be taken to specify name arguments precisely
     * (including case) as they are stored in the database. <p>
     *
     * Including 1.7.1, this JDBC feature is not supported; calling
     * this method throws a SQLException stating that the operation
     * is not supported. <p>
     *
     * Since 1.7.2, this feature is supported by default. If the jar is
     * compiled without org.hsqldb.DatabaseInformationFull or
     * org.hsqldb.DatabaseInformationMain, the feature is
     * not supported. The default implementation is
     * {@link org.hsqldb.DatabaseInformationFull}.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used
     *       to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a
     *       schema; <code>null</code> means that the schema name should not be
     *       used to narrow the search
     * @param typeNamePattern a type name pattern; must match the
     *       type name as it is stored in the database
     * @param attributeNamePattern an attribute name pattern; must match the
     *       attribute name as it is declared in the database
     * @return a <code>ResultSet</code> object in which each row is an
     *        attribute description
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public ResultSet getAttributes(String catalog, String schemaPattern,
                                   String typeNamePattern,
                                   String attributeNamePattern)
                                   throws SQLException {

        if (wantsIsNull(typeNamePattern)
                || wantsIsNull(attributeNamePattern)) {
            return executeSelect("SYSTEM_UDTATTRIBUTES", "0=1");
        }

        schemaPattern = translateSchema(schemaPattern);

        StringBuffer select = toQueryPrefix("SYSTEM_UDTATTRIBUTES").append(
            and("TYPE_CAT", "=", catalog)).append(
            and("TYPE_SCHEM", "LIKE", schemaPattern)).append(
            and("TYPE_NAME", "LIKE", typeNamePattern)).append(
            and("ATTR_NAME", "LIKE", attributeNamePattern));

        return execute(select.toString());
    }

//#endif JDBC3

    /**
     * Retrieves whether this database supports the given result
     * set holdability. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSQLDB returns true for
     * HOLD_CURSORS_OVER_COMMIT, else false. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     * @param holdability one of the following constants:
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean supportsResultSetHoldability(int holdability)
    throws SQLException {
        return holdability == jdbcResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

//#endif JDBC3

    /**
     * Retrieves the default holdability of this <code>ResultSet</code>
     * object. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with HSQLDB 1.7.2, this JDBC feature is supported. <p>
     *
     * Calling this method returns HOLD_CURSORS_OVER_COMMIT, since HSQLDB
     * ResultSet objects are never closed as the result of an implicit
     * or explicit commit operation. <p>
     *
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the default holdability; either
     *        <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *        <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public int getResultSetHoldability() throws SQLException {

// JDBC 3.0 fr spec:
// 14.1.3 ResultSet Holdability
//
// Calling the method Connection.commit can close the ResultSet objects that
// have been created during the current transaction. In some cases, however,
// this may not be the desired behaviour. The ResultSet property holdability
// gives the application control over whether ResultSet objects (cursors) are
// closed when a commit operation is implicity or explictly performed.
// The following ResultSet constants may be supplied to the Connection methods
// createStatement, prepareStatement, and prepareCall:
//
// 1. HOLD_CURSORS_OVER_COMMIT
//
// * ResultSet objects (cursors) are not closed; they are held open when a
//   commit operation is implicity or explicity performed.
//
// 2. CLOSE_CURSORS_AT_COMMIT
//
// * ResultSet objects (cursors) are closed when a commit operation is
//   implicity or explicity performed. Closing cursors at commit can result
//   in better performance for some applications.
//
//   The default holdability of ResultSet objects is implementation defined.
//   The DatabaseMetaData method getResultSetHoldability can be called to
//   determine the default holdability of result sets returned by the
//   underlying data source.
// --
// boucherb@users 20030819
// Our ResultSet objects are never closed as the result of a commit
        return jdbcResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

//#endif JDBC3

    /**
     * Retrieves the major version number of the underlying database. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, the feature is supported under JDK14 builds. <p>
     *
     * This value is retrieved through an SQL call to the new
     * {@link Library#getDatabaseMajorVersion} method which allows
     * correct determination of the database major version for both local
     * and remote database instances.
     * </div>
     * <!-- end release-specific documentation -->
     * @return the underlying database's major version
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public int getDatabaseMajorVersion() throws SQLException {

        ResultSet rs =
            execute("call \"org.hsqldb.Library.getDatabaseMajorVersion\"()");

        rs.next();

        int result = rs.getInt(1);

        rs.close();

        return result;
    }

//#endif JDBC3

    /**
     * Retrieves the minor version number of the underlying database. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, the feature is supported under JDK14 builds. <p>
     *
     * This value is retrieved through an SQL call to the new
     * {@link Library#getDatabaseMinorVersion} method which allows
     * correct determination of the database minor version for both local
     * and remote database instances.
     * </div>
     * <!-- end release-specific documentation -->
     * @return underlying database's minor version
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public int getDatabaseMinorVersion() throws SQLException {

        ResultSet rs =
            execute("call \"org.hsqldb.Library.getDatabaseMinorVersion\"()");

        rs.next();

        int result = rs.getInt(1);

        rs.close();

        return result;
    }

//#endif JDBC3

    /**
     * Retrieves the major JDBC version number for this
     * driver. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, the feature is supported under JDK14 builds.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return JDBC version major number
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public int getJDBCMajorVersion() throws SQLException {
        return 3;
    }

//#endif JDBC3

    /**
     * Retrieves the minor JDBC version number for this
     * driver. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, the feature is supported under JDK14 builds.
     * </div>
     * <!-- end release-specific documentation -->
     * @return JDBC version minor number
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

//#endif JDBC3

    /**
     * @todo fredt@users we haven't checked it all but should aim at
     *  sqlStateSQL99; Need to review the codes.
     */

    /**
     * Indicates whether the SQLSTATEs returned by
     * <code>SQLException.getSQLState</code> is X/Open (now known as Open Group)
     * SQL CLI or SQL99. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSQLDB returns <code>sqlStateSQL99</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @return the type of SQLSTATEs, one of:
     *       sqlStateXOpen or
     *       sqlStateSQL99
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public int getSQLStateType() throws SQLException {
        return sqlStateSQL99;
    }

//#endif JDBC3

    /**
     * Indicates whether updates made to a LOB are made on a copy or directly
     * to the LOB. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB updates the LOB directly. This
     * method return false.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if updates are made to a copy of the LOB;
     *        <code>false</code> if updates are made directly to the LOB
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }

//#endif JDBC3

    /**
     * Retrieves whether this database supports statement pooling. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.2, HSQLDB does not support statement pooling.
     * This method returns false.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> is so;
     * <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

//#endif JDBC3
    //----------------------- Internal Implementation --------------------------

    /**
     * Constructs a new <code>jdbcDatabaseMetaData</code> object using the
     * specified connection.  This contructor is used by <code>jdbcConnection</code>
     * when producing a <code>DatabaseMetaData</code> object from a call to
     * {@link jdbcConnection#getMetaData() getMetaData}.
     * @param c the connection this object will use to retrieve
     *         instance-specific metadata
     * @throws SQLException never - reserved for future use
     */
    jdbcDatabaseMetaData(jdbcConnection c) throws SQLException {

        // PRE: is non-null and not closed
        connection       = c;
        useSchemaDefault = c.connProperties.isPropertyTrue("default_schema");
    }

    /**
     * Retrieves an "AND" predicate based on the (column) <code>id</code>,
     * <code>op</code>(erator) and<code>val</code>(ue) arguments to be
     * included in an SQL "WHERE" clause, using the conventions laid out for
     * JDBC DatabaseMetaData filter parameter values. <p>
     *
     * @return an "AND" predicate built from the arguments
     * @param id the simple, non-quoted identifier of a system table
     *      column upon which to filter. <p>
     *
     *      No checking is done for column name validity. <br>
     *      It is assumed the system table column name is correct. <p>
     *
     * @param op the conditional operation to perform using the system table
     *      column name value and the <code>val</code> argument. <p>
     *
     * @param val an object representing the value to use in some conditional
     *      operation, op, between the column identified by the id argument
     *      and this argument. <p>
     *
     *      <UL>
     *          <LI>null causes the empty string to be returned. <p>
     *
     *          <LI>toString().length() == 0 causes the returned expression
     *              to be built so that the IS NULL operation will occur
     *              against the specified column. <p>
     *
     *          <LI>instanceof String causes the returned expression to be
     *              built so that the specified operation will occur between
     *              the specified column and the specified value, converted to
     *              an SQL string (single quoted, with internal single quotes
     *              escaped by doubling). If <code>op</code> is "LIKE" and
     *              <code>val</code> does not contain any "%" or "_" wild
     *              card characters, then <code>op</code> is silently
     *              converted to "=". <p>
     *
     *          <LI>!instanceof String causes an expression to built so that
     *              the specified operation will occur between the specified
     *              column and <code>String.valueOf(val)</code>. <p>
     *
     *      </UL>
     */
    private static String and(String id, String op, Object val) {

        // The JDBC standard for pattern arguments seems to be:
        //
        // - pass null to mean ignore (do not include in query),
        // - pass "" to mean filter on <column-ident> IS NULL,
        // - pass "%" to filter on <column-ident> IS NOT NULL.
        // - pass sequence with "%" and "_" for wildcard matches
        // - when searching on values reported directly from DatabaseMetaData
        //   results, typically an exact match is desired.  In this case, it
        //   is the client's responsibility to escape any reported "%" and "_"
        //   characters using whatever DatabaseMetaData returns from
        //   getSearchEscapeString(). In our case, this is the standard escape
        //   character: '\'. Typically, '%' will rarely be encountered, but
        //   certainly '_' is to be expected on a regular basis.
        // - checkme:  what about the (silly) case where an identifier
        //   has been declared such as:  'create table "xxx\_yyy"(...)'?
        //   Must the client still escape the Java string like this:
        //   "xxx\\\\_yyy"?
        //   Yes: because otherwise the driver is expected to
        //   construct something like:
        //   select ... where ... like 'xxx\_yyy' escape '\'
        //   which will try to match 'xxx_yyy', not 'xxx\_yyy'
        //   Testing indicates that indeed, higher quality popular JDBC
        //   database browsers do the escapes "properly."
        if (val == null) {
            return "";
        }

        StringBuffer sb    = new StringBuffer();
        boolean      isStr = (val instanceof String);

        if (isStr && ((String) val).length() == 0) {
            return sb.append(" AND ").append(id).append(
                " IS NULL").toString();
        }

        String v = isStr ? Column.createSQLString((String) val)
                         : String.valueOf(val);

        sb.append(" AND ").append(id).append(' ');

        // add the escape to like if required
        if (isStr && "LIKE".equalsIgnoreCase(op)) {
            if (v.indexOf('_') < 0 && v.indexOf('%') < 0) {

                // then we can optimize.
                sb.append("=").append(' ').append(v);
            } else {
                sb.append("LIKE").append(' ').append(v);

                if ((v.indexOf("\\_") >= 0) || (v.indexOf("\\%") >= 0)) {

                    // then client has requested at least one escape.
                    sb.append(" ESCAPE '\\'");
                }
            }
        } else {
            sb.append(op).append(' ').append(v);
        }

        return sb.toString();
    }

    /**
     * The main SQL statement executor.  All SQL destined for execution
     * ultimately goes through this method. <p>
     *
     * The sqlStatement field for the result is set autoClose to comply with
     * ResultSet.getStatement() semantics for result sets that are not from
     * a user supplied Statement object. (fredt) <p>
     *
     * @param sql SQL statement to execute
     * @return the result of issuing the statement
     * @throws SQLException is a database error occurs
     */
    private ResultSet execute(String sql) throws SQLException {

        // NOTE:
        // Need to create a jdbcStatement here so jdbcResultSet can return
        // its Statement object on call to getStatement().
        // The native jdbcConnection.execute() method does not
        // automatically assign a Statement object for the ResultSet, but
        // jdbcStatement does.  That is, without this, there is no way for the
        // jdbcResultSet to find its way back to its Connection (or Statement)
        // Also, cannot use single, shared jdbcStatement object, as each
        // fetchResult() closes any old jdbcResultSet before fetching the
        // next, causing the jdbcResultSet's Result object to be nullified
        final int scroll = jdbcResultSet.TYPE_SCROLL_INSENSITIVE;
        final int concur = jdbcResultSet.CONCUR_READ_ONLY;
        ResultSet r = connection.createStatement(scroll,
            concur).executeQuery(sql);

        ((jdbcResultSet) r).autoClose = true;

        return r;
    }

    /**
     * An SQL statement executor that knows how to create a "SELECT
     * * FROM" statement, given a table name and a <em>where</em> clause.<p>
     *
     *  If the <em>where</em> clause is null, it is ommited.  <p>
     *
     *  It is assumed that the table name is non-null, since this is a private
     *  method.  No check is performed. <p>
     *
     * @return the result of executing "SELECT * FROM " + table " " + where
     * @param table the name of a table to "select * from"
     * @param where the where condition for the select
     * @throws SQLException if database error occurs
     */
    private ResultSet executeSelect(String table,
                                    String where) throws SQLException {

        String select = selstar + table;

        if (where != null) {
            select += " WHERE " + where;
        }

        return execute(select);
    }

    /**
     * Retrieves "SELECT * FROM &lt;table&gt; WHERE 1=1" in string
     * buffer form. <p>
     *
     * This is a convenience method provided because, for most
     * <code>DatabaseMetaData</code> queries, this is the most suitable
     * thing upon which to start building. <p>
     *
     * @return an StringBuffer whose content is:
     *      "SELECT * FROM &lt;table&gt; WHERE 1=1"
     * @param t the name of the table
     */
    private StringBuffer toQueryPrefix(String t) {

        StringBuffer sb = new StringBuffer(255);

        return sb.append(selstar).append(t).append(whereTrue);
    }

    /**
     * Retrieves whether the JDBC <code>DatabaseMetaData</code> contract
     * specifies that the argument <code>s</code>code> is filter parameter
     * value that requires a corresponding IS NULL predicate. <p>
     *
     * @param s the filter parameter to test
     * @return true if the argument, s, is filter paramter value that
     *        requires a corresponding IS NULL predicate
     */
    private static boolean wantsIsNull(String s) {
        return (s != null && s.length() == 0);
    }

    /**
     * For compatibility, when the connection property "default_schame=true"
     * is present, any DatabaseMetaData call with an empty string as the
     * schema parameter will use the default schema (noramlly "PUBLIC").
     */
    private String translateSchema(String schemaName) throws SQLException {

        if (useSchemaDefault && schemaName != null
                && schemaName.length() == 0) {
            ResultSet rs = executeSelect("SYSTEM_SCHEMAS", "IS_DEFAULT=TRUE");

            if (rs.next()) {
                return rs.getString(1);
            }

            return schemaName;
        }

        return schemaName;
    }
}
