/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

//#ifdef JAVA2
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.util.Map;

//#endif JAVA2
import org.hsqldb.Column;
import org.hsqldb.HsqlDateTime;
import org.hsqldb.HsqlException;
import org.hsqldb.Record;
import org.hsqldb.Result;
import org.hsqldb.ResultConstants;
import org.hsqldb.Trace;
import org.hsqldb.Types;
import org.hsqldb.lib.AsciiStringInputStream;
import org.hsqldb.lib.StringInputStream;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.types.Binary;
import org.hsqldb.types.JavaObject;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// SCROLL_INSENSITIVE and FORWARD_ONLY types for ResultSet are now supported
// fredt@users 20020315 - patch 497714 by lakuhns@users - scrollable ResultSet
// all absolute and relative positioning methods defined
// boucherb@users 20020409 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.ResultSet and
// java.sql.ResultSetMetaData interfaces, updated generic documentation to
// JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020505 extensive review and update
// of docs and behaviour to comply with java.sql specification
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg
// fredt@users 20030622 - patch 1.7.2 - columns and labels are case sensitive
// boucherb@users 200404xx - javadoc updates

/**
 * <!-- start generic documentation -->
 * A table of data representing a database result set, which
 * is usually generated by executing a statement that queries the database.
 *
 * <P>A <code>ResultSet</code> object  maintains a cursor pointing
 * to its current row of data.  Initially the cursor is positioned
 * before the first row. The <code>next</code> method moves the
 * cursor to the next row, and because it returns <code>false</code>
 * when there are no more rows in the <code>ResultSet</code> object,
 * it can be used in a <code>while</code> loop to iterate through
 * the result set.
 * <P>
 * A default <code>ResultSet</code> object is not updatable and
 * has a cursor that moves forward only.  Thus, you can
 * iterate through it only once and only from the first row to the
 * last row. It is possible to
 * produce <code>ResultSet</code> objects that are scrollable and/or
 * updatable.  The following code fragment, in which <code>con</code>
 * is a valid <code>Connection</code> object, illustrates how to make
 * a result set that is scrollable and insensitive to updates by others,
 * and that is updatable. See <code>ResultSet</code> fields for other
 * options.
 * <PRE>
 *
 * Statement stmt = con.createStatement(
 *                            ResultSet.TYPE_SCROLL_INSENSITIVE,
 *                            ResultSet.CONCUR_UPDATABLE);
 * ResultSet rs = stmt.executeQuery("SELECT a, b FROM TABLE2");
 * // rs will be scrollable, will not show changes made by others,
 * // and will be updatable
 *
 * </PRE>
 * The <code>ResultSet</code> interface provides
 * <i>getter</i> methods (<code>getBoolean</code>, <code>getLong</code>,
 * and so on) for retrieving column values from the current row.
 * Values can be retrieved using either the index number of the
 * column or the name of the column.  In general, using the
 * column index will be more efficient.  Columns are numbered from 1.
 * For maximum portability, result set columns within each row should be
 * read in left-to-right order, and each column should be read only once.
 *
 * <P>For the getter methods, a JDBC driver attempts
 * to convert the underlying data to the Java type specified in the
 * getter method and returns a suitable Java value.  The JDBC specification
 * has a table showing the allowable mappings from SQL types to Java types
 * that can be used by the <code>ResultSet</code> getter methods.
 * <P>
 * <P>Column names used as input to getter methods are case
 * insensitive.  When a getter method is called  with
 * a column name and several columns have the same name,
 * the value of the first matching column will be returned.
 * The column name option is
 * designed to be used when column names are used in the SQL
 * query that generated the result set.
 * For columns that are NOT explicitly named in the query, it
 * is best to use column numbers. If column names are used, there is
 * no way for the programmer to guarantee that they actually refer to
 * the intended columns.
 * <P>
 * A set of updater methods were added to this interface
 * in the JDBC 2.0 API (Java<sup><font size=-2>TM</font></sup> 2 SDK,
 * Standard Edition, version 1.2). The comments regarding parameters
 * to the getter methods also apply to parameters to the
 * updater methods.
 * <P>
 * The updater methods may be used in two ways:
 * <ol>
 * <LI>to update a column value in the current row.  In a scrollable
 * <code>ResultSet</code> object, the cursor can be moved backwards
 * and forwards, to an absolute position, or to a position
 * relative to the current row.
 * The following code fragment updates the <code>NAME</code> column
 * in the fifth row of the <code>ResultSet</code> object
 * <code>rs</code> and then uses the method <code>updateRow</code>
 * to update the data source table from which <code>rs</code> was
 * derived.
 * <PRE>
 *
 * rs.absolute(5); // moves the cursor to the fifth row of rs
 * rs.updateString("NAME", "AINSWORTH"); // updates the
 * // <code>NAME</code> column of row 5 to be <code>AINSWORTH</code>
 * rs.updateRow(); // updates the row in the data source
 *
 * </PRE>
 * <LI>to insert column values into the insert row.  An updatable
 * <code>ResultSet</code> object has a special row associated with
 * it that serves as a staging area for building a row to be inserted.
 * The following code fragment moves the cursor to the insert row, builds
 * a three-column row, and inserts it into <code>rs</code> and into
 * the data source table using the method <code>insertRow</code>.
 * <PRE>
 *
 * rs.moveToInsertRow(); // moves cursor to the insert row
 * rs.updateString(1, "AINSWORTH"); // updates the
 * // first column of the insert row to be <code>AINSWORTH</code>
 * rs.updateInt(2,35); // updates the second column to be <code>35</code>
 * rs.updateBoolean(3, true); // updates the third row to <code>true</code>
 * rs.insertRow();
 * rs.moveToCurrentRow();
 *
 * </PRE>
 * </ol>
 * <P>A <code>ResultSet</code> object is automatically closed when the
 * <code>Statement</code> object that
 * generated it is closed, re-executed, or used
 * to retrieve the next result from a sequence of multiple results.
 *
 * <P>The number, types and properties of a <code>ResultSet</code>
 * object's columns are provided by the <code>ResulSetMetaData</code>
 * object returned by the <code>ResultSet.getMetaData</code> method. <p>
 * <!-- end generic documentation -->
 *
 * <!-- start release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h3>HSQLDB-Specific Information:</h3> <p>
 *
 * A <code>ResultSet</code> object generated by HSQLDB is by default of
 * <code>ResultSet.TYPE_FORWARD_ONLY</code> (as is standard JDBC behavior)
 * and does not allow the use of absolute and relative positioning
 * methods.  However, since 1.7.0, if a statement is created
 * with:<p>
 *
 * <pre class="JavaCodeExample">
 * Statement stmt = conn.<b>createStatement</b>(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 * </pre>
 *
 * then the <code>ResultSet</code> objects it produces support
 * using all of  the absolute and relative positioning methods of JDBC2
 * to set the position of the current row, for example:<p>
 *
 * <pre class="JavaCodeExample">
 * rs.<b>absolute</b>(<span class="JavaNumericLiteral">5</span>);
 * String fifthRowValue = rs.<b>getString</b>(<span class="JavaNumericLiteral">1</span>);
 * rs.<b>relative</b>(<span class="JavaNumericLiteral">4</span>);
 * String ninthRowValue = rs.<b>getString</b>(<span class="JavaNumericLiteral">1</span>);
 * </pre>
 *
 * Note: An HSQLDB <code>ResultSet</code> object persists, even after its
 * connection is closed.  This is regardless of the operational mode of
 * the {@link Database Database} from which it came.  That is, they
 * persist whether originating from a <code>Server</code>,
 * <code>WebServer</code> or in-process mode <code>Database.</code>
 * <p>
 *
 * Including HSQLDB 1.7.2, there is no support for any of the methods
 * introduced in JDBC 2 relating to updateable result sets. These methods
 * include all updateXXX methods, as well as the {@link #insertRow},
 * {@link #updateRow}, {@link #deleteRow}, {@link #moveToInsertRow} (and so on)
 * methods; invoking any such method throws an <code>SQLException</code>
 * stating that the operation is not supported.
 *
 * <b>JRE 1.1.x Notes:</b> <p>
 *
 * In general, JDBC 2 support requires Java 1.2 and above, and JDBC 3 requires
 * Java 1.4 and above. In HSQLDB, support for methods introduced in different
 * versions of JDBC depends on the JDK version used for compiling and building
 * HSQLDB.<p>
 *
 * Since 1.7.0, it is possible to build the product so that
 * all JDBC 2 methods can be called while executing under the version 1.1.x
 * <em>Java Runtime Environment</em><sup><font size="-2">TM</font></sup>.
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of the
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <code>ResultSet</code></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined here, in this
 * class. <p>
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
 * However, please note that code written in such a manner will not be
 * compatible for use with other JDBC 2 drivers, since they expect and use
 * <code>ResultSet</code>, rather than <code>jdbcResultSet</code>.  Also
 * note, this feature is offered solely as a convenience to developers
 * who must work under JDK 1.1.x due to operating constraints, yet wish to
 * use some of the more advanced features available under the JDBC 2
 * specification.<p>
 *
 * (fredt@users) <br>
 * (boucherb@users)<p>
 *
 * </div>
 * @see jdbcStatement#executeQuery
 * @see jdbcStatement#getResultSet
 * @see <a href=
 * "http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSetMetaData.html">
 * <code>ResultSetMetaData</code></a>
 *
 * Extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class jdbcResultSet implements ResultSet {

// fredt@users 20020320 - patch 497714 by lakuhns@users - scrollable ResultSet
// variable values in different states
// Condition definitions
//                  bInit  iCurrentRow  nCurrent  nCurrent.next
//                  -----  -----------  --------  -------------
// beforeFirst      false       0         N/A          N/A
// first            true        1        !null    next or null
// last             true    last row #   !null        null
// afterLast        true   last row + 1   N/A         N/A
//------------------------ Private Attributes --------------------------
/*
 * Campbell's comments
 * Future Development Information for Developers and Contributors<p>
 * Providing a
 * full and robust implementation guaranteeing consistently accurate
 * results and behaviour depends upon introducing several new engine
 * features for which the internals of the product currently have no
 * infrastructure: <p>
 *
 * <OL>
 * <LI>a unique rowid for each row in the database which lasts the life
 *  of a row, independent of any updates made to that row</LI>
 * <LI>the ability to explicitly lock either the tables or the
 *  individual rows of an updateable result, for the duration that
 *  the result is open</LI>
 * <LI>the ability to choose between transactions supporting repeatable
 *  reads, committed reads, and uncommitted reads
 * <LI>the ability to map an updated result row's columns back to
 *  specific updateable objects on the database.<p>
 *
 *  <B>Note:</B> Typically, it is easy to do this mapping if all the
 *  rows of a result consist of columns from a single table.  And it
 *  is especially easy if the result's columns are a superset of the
 *  primary key columns of that table.  The ability to
 *  update a result consisting of any combintation of join, union,
 *  intersect, difference and grouping operations, however, is much more
 *  complex to implement and often impossible, especially under
 *  grouping and non-natural joins.  Also, it is not even guaranteed
 *  that the columns of a result map back to *any* updateable object
 *  on the database, for instance in the cases where the
 *  result's column values are general expressions or the result
 *  comes from a stored procedure where the data may not even come,
 *  directly or indirectly, from updateable database objects such as
 *  columns in table rows.
 * </OL>
 *
 * For developers working under a JDBC3 environment,
 * it is gently recommended to take a look at Sun's early access
 * <a href="http://developer.java.sun.com/developer/earlyAccess/crs/">
 * <code>RowSet</code></a> implementation, as this can be used to add
 * JDBC driver independent scrollablility and updateability.
 * However, as a driver independent implementation, it obviously cannot
 * guarantee to use the traditional table and/or row locking features
 * that many DBMS make available to ensure the success of all
 * valid updates against updateable results sets.  As such, performing
 * updates through Sun's early access <code>RowSet</code> implementation
 * may not always succeed, even when it is generally expected that they
 * should.  This is because the condition used to find the original row
 * on the database to update (which, for a driver independent
 * implementation, would have to be equality on all columns values of
 * the originally retrieved row) can become invalid if another
 * transaction modifies or deletes that row on the database at some
 * point between the time the row was last retrieved or refreshed in
 * the RowSet and the time the RowSet attempts to make its next
 * update to that row.  Also, any driver independent implementation
 * of RowSet is still dependent on each driver guaranteeing that its
 * <code>ResultSet</code> objects return completely accurate
 * <code>ResultSetMetaData</code> that fulfills all of the
 * JDBC <code>ResultSetMetaData</code> contracts under all circumstances.
 * However, up to and including 1.7.0, HSQLDB does not make such guarantees
 * under all conditions. See the discussion at {@link #getMetaData}.
 * (boucherb@users) (version 1.7.0)<p>
*/

// boucherb@users/hiep256@users 20010829 - patch 1.7.2 - allow expression to
// return Results as Object, where object is Result or jdbcResultSet.
// - rResult access changed to allow getting internal result object
// from Parser.processCall()

    /** Statement is closed when its result set is closed */
    boolean autoClose;

    /** The internal representation. */
    public Result rResult;

    /**
     * The current record containing the data for the row
     */
    private Record nCurrent;

    /** The row upon which this ResultSet is currently positioned. */
    private int iCurrentRow;

    /** When the result of updating the database, the number of updated rows. */
    private int iUpdateCount;

    /** Is current row before the first row? */
    private boolean bInit;    // false if before first row

    /** How many columns does this ResultSet have? */
    int iColumnCount;

    /** Did the last getXXX method encounter a null value? */
    private boolean bWasNull;

    /** The ResultSetMetaData object for this ResultSet */
    private ResultSetMetaData rsmd;

    /** Properties of this ResultSet's parent Connection. */
    private HsqlProperties connProperties;

    /** is the connection via network */
    private boolean isNetConn;

    /**
     * The Statement that generated this result. Null if the result is
     * from DatabaseMetaData<p>
     */
    jdbcStatement sqlStatement;

    //------------------------ Package Attributes --------------------------

    /**
     * The scrollability / scroll sensitivity type of this result.
     */
    int rsType = TYPE_FORWARD_ONLY;

    /**
     * <!-- start generic documentation -->
     * Moves the cursor down one row from its current position.
     * A <code>ResultSet</code> cursor is initially positioned
     * before the first row; the first call to the method
     * <code>next</code> makes the first row the current row; the
     * second call makes the second row the current row, and so on.
     *
     * <P>If an input stream is open for the current row, a call
     * to the method <code>next</code> will
     * implicitly close it. A <code>ResultSet</code> object's
     * warning chain is cleared when a new row is read. <p>
     *
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the new current row is valid;
     * <code>false</code> if there are no more rows
     * @exception SQLException if a database access error occurs
     */
    public boolean next() throws SQLException {

        bWasNull = false;

        // Have an empty resultset so exit with false
        if (rResult == null || rResult.isEmpty()) {
            return false;
        }

        if (!bInit) {

            // The resultset has not been traversed, so set the cursor
            // to the first row (1)
            nCurrent    = rResult.rRoot;
            bInit       = true;
            iCurrentRow = 1;
        } else {

            // The resultset has been traversed, if afterLast, return false
            if (nCurrent == null) {
                return false;
            }

            // On a valid row so go to next
            nCurrent = nCurrent.next;

            iCurrentRow++;
        }

        // finally test to see if we are in an afterLast situation
        if (nCurrent == null) {

            // Yes, set the current row to after last and exit with false
            iCurrentRow = rResult.getSize() + 1;

            return false;
        } else {

            // Not afterLast, so success
            return true;
        }
    }

    /**
     * <!-- start generic documentation -->
     * Releases this <code>ResultSet</code> object's database and
     * JDBC resources immediately instead of waiting for
     * this to happen when it is automatically closed.
     *
     * <P><B>Note:</B> A <code>ResultSet</code> object
     * is automatically closed by the
     * <code>Statement</code> object that generated it when
     * that <code>Statement</code> object is closed,
     * re-executed, or is used to retrieve the next result from a
     * sequence of multiple results. A <code>ResultSet</code> object
     * is also automatically closed when it is garbage collected. <p>
     * <!-- end generic documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() throws SQLException {

        iUpdateCount = -1;
        rResult      = null;

        if (autoClose) {
            sqlStatement.close();
        }
    }

    /**
     * <!-- start generic documentation -->
     * Reports whether
     * the last column read had a value of SQL <code>NULL</code>.
     * Note that you must first call one of the getter methods
     * on a column to try to read its value and then call
     * the method <code>wasNull</code> to see if the value read was
     * SQL <code>NULL</code>. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the last column value read was SQL
     *     <code>NULL</code> and <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean wasNull() throws SQLException {
        return bWasNull;
    }

    //======================================================================
    // Methods for accessing results by column index
    //======================================================================

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public String getString(int columnIndex) throws SQLException {
        return (String) getColumnInType(columnIndex, Types.CHAR);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>false</code>
     * @exception SQLException if a database access error occurs
     */
    public boolean getBoolean(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.BOOLEAN);

        return o == null ? false
                         : ((Boolean) o).booleanValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public byte getByte(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.TINYINT);

        return o == null ? 0
                         : ((Number) o).byteValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public short getShort(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.SMALLINT);

        return o == null ? 0
                         : ((Number) o).shortValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public int getInt(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.INTEGER);

        return o == null ? 0
                         : ((Number) o).intValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public long getLong(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.BIGINT);

        return o == null ? 0
                         : ((Number) o).longValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public float getFloat(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.REAL);

        return o == null ? (float) 0.0
                         : ((Number) o).floatValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public double getDouble(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.DOUBLE);

        return o == null ? 0.0
                         : ((Number) o).doubleValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.BigDecimal</code> in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Beginning with 1.7.0, HSQLDB converts the result and sets the scale
     * with BigDecimal.ROUND_HALF_DOWN.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated by java.sun.com as of JDK 1.2
     */

//#ifdef DEPRECATEDJDBC
    public BigDecimal getBigDecimal(int columnIndex,
                                    int scale) throws SQLException {

        // boucherb@users 20020502 - added conversion
        BigDecimal bd = (BigDecimal) getColumnInType(columnIndex,
            Types.DECIMAL);

        if (scale < 0) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }

        if (bd != null) {
            bd = bd.setScale(scale, BigDecimal.ROUND_HALF_DOWN);
        }

        return bd;
    }

//#endif

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB returns correct values for columns of type <code>BINARY</code>,
     * <code>CHAR</code> and their variations. For other types, it returns
     * the <code>byte[]</code> for the <code>String</code> representation
     * of the value.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public byte[] getBytes(int columnIndex) throws SQLException {

        Object x = getObject(columnIndex);

        if (x == null) {
            return null;
        }

        if (x instanceof byte[]) {
            return (byte[]) x;
        }

        if (x instanceof java.lang.String) {
            return ((String) x).getBytes();
        }

        x = getColumnInType(columnIndex, Types.BINARY);

        return (byte[]) x;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Date</code> object in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public Date getDate(int columnIndex) throws SQLException {
        return (Date) getColumnInType(columnIndex, Types.DATE);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public Time getTime(int columnIndex) throws SQLException {
        return (Time) getColumnInType(columnIndex, Types.TIME);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.Timestamp</code> object in the Java programming
     * language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return (Timestamp) getColumnInType(columnIndex, Types.TIMESTAMP);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a stream of ASCII characters. The value can then be read in chunks
     * from the stream. This method is particularly
     * suitable for retrieving large <char>LONGVARCHAR</char> values.
     * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * The limitation noted above does not apply to HSQLDB.<p>
     *
     * In 1.6.1 and previous, getAsciiStream was identical to
     * getUnicodeStream and both simply returned a byte stream
     * constructed from the raw {@link #getBytes(int) getBytes}
     * representation.
     *
     * Starting with 1.7.0, this has been updated to comply with the
     * java.sql specification.
     *
     * When the column is of type CHAR and its variations, it requires no
     * conversion since it is represented internally already as a
     * Java String object. When the column is not of type CHAR and its
     * variations, the returned stream is based on a conversion to the
     * Java <code>String</code> representation of the value. In either case,
     * the obtained stream is always equivalent to a stream of the low order
     * bytes from the value's String representation. <p>
     *
     * HSQLDB SQL <code>CHAR</code> and its variations are all Unicode strings
     * internally, so the recommended alternatives to this method are
     * {@link #getString(int) getString},
     * {@link #getUnicodeStream(int) getUnicodeStream} (<b>deprecated</b>)
     * and new to 1.7.0: {@link #getCharacterStream(int) getCharacterStream}
     * (now prefered over the deprecated getUnicodeStream alternative).
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of one-byte ASCII characters;
     * if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.io.InputStream getAsciiStream(int columnIndex)
    throws SQLException {

        String s = getString(columnIndex);

        if (s == null) {
            return null;
        }

        return new AsciiStringInputStream(s);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * as a stream of two-byte Unicode characters. The first byte is
     * the high byte; the second byte is the low byte.
     *
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code>values.  The
     * JDBC driver will do any necessary conversion from the database
     * format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.
     * Also, a stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called, whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * The limitation noted above does not apply to HSQLDB.<p>
     *
     * Up to and including 1.6.1, getUnicodeStream (and getAsciiStream)
     * both simply returned a byte stream constructed from the
     * raw {@link #getBytes(int) getBytes} representation.
     *
     * Starting with 1.7.0, this has been corrected to comply with the
     * java.sql specification.
     *
     * When the column is of type CHAR and its variations, it requires no
     * conversion since it is represented internally already as
     * Java Strings. When the column is not of type CHAR and its variations,
     * the returned stream is based on a conversion to the
     * Java <code>String</code> representation of the value. In either case,
     * the obtained stream is always equivalent to a stream of
     * bytes from the value's String representation, with high-byte first.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *   as a stream of two-byte Unicode characters;
     *   if the value is SQL <code>NULL</code>, the value returned is
     *   <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated use <code>getCharacterStream</code> in place of
     *        <code>getUnicodeStream</code>
     */

//#ifdef DEPRECATEDJDBC
    public java.io.InputStream getUnicodeStream(int columnIndex)
    throws SQLException {

        String s = getString(columnIndex);

        if (s == null) {
            return null;
        }

        return new StringInputStream(s);
    }

//#endif

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a binary stream of
     * uninterpreted bytes. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code> values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *     as a stream of uninterpreted bytes;
     *     if the value is SQL <code>NULL</code>, the value returned is
     *     <code>null</code>
     * @exception SQLException if a database access error occurs
     */
// fredt@users 20020215 - patch 485704 by boucherb@users
    public java.io.InputStream getBinaryStream(int columnIndex)
    throws SQLException {

        byte[] b = getBytes(columnIndex);

        return wasNull() ? null
                         : new ByteArrayInputStream(b);
    }

    //======================================================================
    // Methods for accessing results by column name
    //======================================================================

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public String getString(String columnName) throws SQLException {
        return getString(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>false</code>
     * @exception SQLException if a database access error occurs
     */
    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public byte getByte(String columnName) throws SQLException {
        return getByte(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public short getShort(String columnName) throws SQLException {
        return getShort(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public int getInt(String columnName) throws SQLException {
        return getInt(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public long getLong(String columnName) throws SQLException {
        return getLong(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public float getFloat(String columnName) throws SQLException {
        return getFloat(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public double getDouble(String columnName) throws SQLException {
        return getDouble(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Beginning with 1.7.0, HSQLDB converts the result and sets the scale
     * with BigDecimal.ROUND_HALF_DOWN.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @param scale the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated by java.sun.com as of JDK 1.2
     */

//#ifdef DEPRECATEDJDBC
    public BigDecimal getBigDecimal(String columnName,
                                    int scale) throws SQLException {
        return getBigDecimal(findColumn(columnName), scale);
    }

//#endif

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public byte[] getBytes(String columnName) throws SQLException {
        return getBytes(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Date</code> object in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public Date getDate(String columnName) throws SQLException {
        return getDate(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public Time getTime(String columnName) throws SQLException {
        return getTime(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.Timestamp</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public Timestamp getTimestamp(String columnName) throws SQLException {
        return getTimestamp(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a stream of
     * ASCII characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
     * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of one-byte ASCII characters.
     * If the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #getAsciiStream(int)
     */
    public java.io.InputStream getAsciiStream(String columnName)
    throws SQLException {
        return getAsciiStream(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a stream of two-byte
     * Unicode characters. The first byte is the high byte; the second
     * byte is the low byte.
     *
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
     * The JDBC technology-enabled driver will
     * do any necessary conversion from the database format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.
     * Also, a stream may return <code>0</code> when the method
     * <code>InputStream.available</code> is called, whether there
     * is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     *    as a stream of two-byte Unicode characters.
     *    If the value is SQL <code>NULL</code>, the value returned
     *    is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @deprecated use <code>getCharacterStream</code> instead
     * @see #getUnicodeStream(int)
     */

//#ifdef DEPRECATEDJDBC
    public java.io.InputStream getUnicodeStream(String columnName)
    throws SQLException {
        return getUnicodeStream(findColumn(columnName));
    }

//#endif

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a stream of uninterpreted
     * <code>byte</code>s.
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code>
     * values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes;
     * if the value is SQL <code>NULL</code>, the result is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.io.InputStream getBinaryStream(String columnName)
    throws SQLException {
        return getBinaryStream(findColumn(columnName));
    }

    //=====================================================================
    // Advanced features:
    //=====================================================================

    /**
     * <!-- start generic documentation -->
     * Retrieves the first warning reported by calls on this
     * <code>ResultSet</code> object.
     * Subsequent warnings on this <code>ResultSet</code> object
     * will be chained to the <code>SQLWarning</code> object that
     * this method returns.
     *
     * <P>The warning chain is automatically cleared each time a new
     * row is read.  This method may not be called on a <code>ResultSet</code>
     * object that has been closed; doing so will cause an
     * <code>SQLException</code> to be thrown.
     * <P>
     * <B>Note:</B> This warning chain only covers warnings caused
     * by <code>ResultSet</code> methods.  Any warning caused by
     * <code>Statement</code> methods
     * (such as reading OUT parameters) will be chained on the
     * <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not produce
     * <code>SQLWarning</code> objects. This method always returns
     * <code>null</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the first <code>SQLWarning</code> object reported or
     *    <code>null</code> if there are none <p>
     *
     * Up to and including 1.7.1, HSQLDB always returns null. <p>
     * @exception SQLException if a database access error occurs or this
     *    method is called on a closed result set
     */
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    /**
     * <!-- start generic documentation -->
     * Clears all warnings reported on this <code>ResultSet</code> object.
     * After this method is called, the method <code>getWarnings</code>
     * returns <code>null</code> until a new warning is
     * reported for this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.1, HSQLDB does not produce <code>SQLWarning</code>
     * objects on any ResultSet object warning chain; calls to this method
     * are ignored.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() throws SQLException {}

    /**
     * <!-- start generic documentation -->
     * Retrieves the name of the SQL cursor used by this
     * <code>ResultSet</code> object.
     *
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result set can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name. To insure that the cursor has the proper isolation
     * level to support update, the cursor's <code>SELECT</code> statement
     * should be of the form <code>SELECT FOR UPDATE</code>. If
     * <code>FOR UPDATE</code> is omitted, the positioned updates may fail.
     *
     * <P>The JDBC API supports this SQL feature by providing the name of the
     * SQL cursor used by a <code>ResultSet</code> object.
     * The current row of a <code>ResultSet</code> object
     * is also the current row of this SQL cursor.
     *
     * <P><B>Note:</B> If positioned update is not supported, a
     * <code>SQLException</code> is thrown. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws an <code>SQLException</code>,
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the SQL name for this <code>ResultSet</code> object's cursor
     * @exception SQLException if a database access error occurs
     */
    public String getCursorName() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the  number, types and properties of
     * this <code>ResultSet</code> object's columns. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * <B>Example:</B> <p>
     *
     * The following code fragment creates a <code>ResultSet</code> object rs,
     * creates a <code>ResultSetMetaData</code> object rsmd, and uses rsmd
     * to find out how many columns rs has and whether the first column
     * in rs can be used in a <code>WHERE</code> clause. <p>
     *
     * <pre class="JavaCodeExample">
     * ResultSet         rs              = stmt.<b>executeQuery</b>(<span class="JavaStringLiteral">"SELECT a, b, c FROM TABLE2"</span>);
     * ResultSetMetaData rsmd = rs.<b>getMetaData</b>();<br>
     * int numberOfColumns = rsmd.<b>getColumnCount</b>();<br>
     * boolean b = rsmd.<b>isSearchable</b>(1);<br>
     * </pre>
     *
     * <hr>
     *
     * <B>Warning:</B> <p>
     *
     * Including 1.7.1, HSQLDB did not generate accurate
     * <code>ResultSetMetaData</code>.  Below were the the most important
     * methods to consider: <p>
     *
     * <ol>
     * <li>isAutoIncrement(int) <i>always</i> returned <code>false</code></li>
     * <li>isCurrency(int) <i>always</i> returned <code>false</code></li>
     * <li>isNullable(int) <i>always</i> returned
     *     <code>columnNullableUnknown</code></li>
     * <li>getColumnDisplaySize(int) returned zero for all valid column
     *     numbers</li>
     * <li>getSchemaName(int) <i>always</i> returned
     *  <span class="JavaStringLiteral">""</span></li>
     * <li>getPrecision(int) <i>always</i> returned zero</li>
     * <li>getScale(int) <i>always</i> returned zero</li>
     * <li>getCatalogName(int) <i>always</i> returned
     *  <span class="JavaStringLiteral">""</span></li>
     * </ol> <p>
     *
     * <hr>
     *
     * Starting with 1.7.2, ResultSetMetaData has been split out into its own
     * interface implemenation (jdbcResultSetMetaData), support has been
     * improved considerably for a number of methods and behaviour has
     * been altered slightly in many areas.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the description of this <code>ResultSet</code> object's columns
     * @exception SQLException if a database access error occurs
     * @see jdbcResultSetMetaData
     */
    public ResultSetMetaData getMetaData() throws SQLException {

        if (rsmd == null) {
            rsmd = new jdbcResultSetMetaData(this, connProperties);
        }

        return rsmd;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * In the JDBC 2.0 API, the behavior of method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a <code>java.lang.Object</code> holding the column value
     * @exception SQLException if a database access error occurs
     */
    public Object getObject(int columnIndex) throws SQLException {

        checkAvailable();

        Object o;
        int    t;

        try {
            o = nCurrent.data[--columnIndex];
            t = rResult.metaData.colTypes[columnIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw Util.sqlException(Trace.COLUMN_NOT_FOUND,
                                    String.valueOf(++columnIndex));
        }

        // use checknull because getColumnInType is not used
        if (checkNull(o)) {
            return null;
        }

        switch (t) {

            case Types.DATE :
                return new Date(((Date) o).getTime());

            case Types.TIME :
                return new Time(((Time) o).getTime());

            case Types.TIMESTAMP :
                long      m  = ((Timestamp) o).getTime();
                int       n  = ((Timestamp) o).getNanos();
                Timestamp ts = new Timestamp(m);

                ts.setNanos(n);

                return ts;

            case Types.OTHER :
            case Types.JAVA_OBJECT :
                try {
                    return ((JavaObject) o).getObject();
                } catch (HsqlException e) {
                    throw Util.sqlException(
                        Trace.error(Trace.SERIALIZATION_FAILURE));
                }
            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                return ((Binary) o).getClonedBytes();

            default :
                return o;
        }
    }

    /**
     * <!-- start generic documentation -->
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * <P>
     * This method may also be used to read datatabase-specific
     * abstract data types.
     * <P>
     * In the JDBC 2.0 API, the behavior of the method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a <code>java.lang.Object</code> holding the column value
     * @exception SQLException if a database access error occurs
     */
    public Object getObject(String columnName) throws SQLException {
        return getObject(findColumn(columnName));
    }

    //----------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Maps the given <code>ResultSet</code> column name to its
     * <code>ResultSet</code> column index. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the name of the column
     * @return the column index of the given column name
     * @exception SQLException if the <code>ResultSet</code> object does not
     *    contain <code>columnName</code> or a database access error occurs
     */
    public int findColumn(String columnName) throws SQLException {

        for (int i = 0; i < iColumnCount; i++) {
            String name = rResult.metaData.colLabels[i];

            if (columnName.equalsIgnoreCase(name)) {
                return i + 1;
            }
        }

        throw Util.sqlException(Trace.COLUMN_NOT_FOUND, columnName);
    }

    //--------------------------JDBC 2.0-----------------------------------
    //---------------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.0. HSQLDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return a <code>java.io.Reader</code> object that contains the column
     *   value; if the value is SQL <code>NULL</code>, the value returned
     *   is <code>null</code> in the Java programming language.
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public java.io.Reader getCharacterStream(int columnIndex)
    throws SQLException {

        String s = getString(columnIndex);

        if (s == null) {
            return null;
        }

        return new StringReader(s);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.0, HSQLDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public java.io.Reader getCharacterStream(String columnName)
    throws SQLException {
        return getCharacterStream(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> with full precision. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value (full precision);
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return (BigDecimal) getColumnInType(columnIndex, Types.DECIMAL);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> with full precision. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the column name
     * @return the column value (full precision);
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getBigDecimal(findColumn(columnName));
    }

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is before the first row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the cursor is before the first row;
     * <code>false</code> if the cursor is at any other position or the
     * result set contains no rows
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean isBeforeFirst() throws SQLException {

        // bInit indicates whether the resultset has not been traversed or not
        // true - it has ---- false it hasn't
        checkClosed();

        return rResult.rRoot != null &&!bInit;

        // End New Cose
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is after the last row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the cursor is after the last row;
     * <code>false</code> if the cursor is at any other position or the
     * result set contains no rows
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean isAfterLast() throws SQLException {

        // At afterLast condition exists when resultset has been traversed and
        // the current row is null.  iCurrentRow should also be set to
        // afterlast but no need to test
        checkClosed();

        return rResult.rRoot != null && bInit && nCurrent == null;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is on the first row of
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the cursor is on the first row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean isFirst() throws SQLException {

        checkClosed();

        return iCurrentRow == 1;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is on the last row of
     * this <code>ResultSet</code> object.
     * Note: Calling the method <code>isLast</code> may be expensive
     * because the JDBC driver
     * might need to fetch ahead one row in order to determine
     * whether the current row is the last row in the result set. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, this method is not terribly expensive;
     * the entire result is fetched internally before this object
     * is returned to a caller.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is on the last row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public boolean isLast() throws SQLException {

        checkClosed();

        // If the resultset has not been traversed, then exit with false
        // At the last row if the next row is null
        return rResult.rRoot != null && bInit && nCurrent != null
               && nCurrent.next == null;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the front of
     * this <code>ResultSet</code> object, just before the
     * first row. This method has no effect if the result set contains
     * no rows.<p>
     * <!-- end generic documentation -->
     *
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public void beforeFirst() throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        // Set to beforeFirst status
        bInit       = false;
        nCurrent    = null;
        iCurrentRow = 0;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the end of
     * this <code>ResultSet</code> object, just after the last row. This
     * method has no effect if the result set contains no rows. <p>
     * <!-- end generic documentation -->
     *
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public void afterLast() throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult != null && rResult.rRoot != null) {

            // not an empty resultset, so set the afterLast status
            bInit       = true;
            iCurrentRow = rResult.getSize() + 1;
            nCurrent    = null;
        }
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the first row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean first() throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        bInit = false;

        if (rResult.rRoot != null) {
            bInit       = true;
            nCurrent    = rResult.rRoot;
            iCurrentRow = 1;
        }

        return bInit;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the last row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean last() throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        if (rResult.rRoot == null) {
            return false;
        }

        // it resultset not traversed yet, set to first row
        if (!bInit || nCurrent == null) {
            first();
        }

        // go to the last row
        while (nCurrent.next != null) {
            iCurrentRow++;

            nCurrent = nCurrent.next;
        }

        return true;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on. <p>
     * <!-- end generic documentation -->
     *
     * @return the current row number; <code>0</code> if there is no current
     *    row
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public int getRow() throws SQLException {

        checkClosed();

        return iCurrentRow;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the given row number in
     * this <code>ResultSet</code> object.
     *
     * <p>If the row number is positive, the cursor moves to
     * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on.
     *
     * <p>If the given row number is negative, the cursor moves to
     * an absolute row position with respect to
     * the end of the result set.  For example, calling the method
     * <code>absolute(-1)</code> positions the
     * cursor on the last row; calling the method <code>absolute(-2)</code>
     * moves the cursor to the next-to-last row, and so on.
     *
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before the first row or after
     * the last row.
     *
     * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
     * as calling <code>first()</code>. Calling <code>absolute(-1)</code>
     * is the same as calling <code>last()</code>. <p>
     * <!-- end generic documentation -->
     *
     * @param row the number of the row to which the cursor should move.
     *    A positive number indicates the row number counting from the
     *    beginning of the result set; a negative number indicates the
     *    row number counting from the end of the result set
     * @return <code>true</code> if the cursor is on the result set;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error
     * occurs, or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean absolute(int row) throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        if (rResult.rRoot == null || row == 0) {

            // No rows in the resultset or tried to execute absolute(0)
            // which is not valid
            return false;
        }

        // A couple of special cases
        switch (row) {

            case 1 :
                return first();    // absolute(1) is same as first()

            case -1 :
                return last();     // absolute(-1) is same as last()
        }

        // If the row variable is negative, calculate the target
        // row from the end of the resultset.
        if (row < 0) {

            // we know there are rows in resultset, so get the last
            last();

            // calculate the target row
            row = iCurrentRow + row + 1;

            // Exit if the target row is before the beginning of the resultset
            if (row <= 0) {
                beforeFirst();

                return false;
            }
        }

        if (row < iCurrentRow || iCurrentRow == 0) {

            // Need to go back and start from the beginning of the resultset
            beforeFirst();
        }

        // go to the tagget row;
        while (row > iCurrentRow) {
            next();

            if (nCurrent == null) {
                break;
            }
        }

        return nCurrent != null;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor a relative number of rows, either positive or
     * negative. Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling the method <code>relative(1)</code>
     * is identical to calling the method <code>next()</code> and
     * calling the method <code>relative(-1)</code> is identical
     * to calling the method <code>previous()</code>. <p>
     * <!-- end generic documentation -->
     *
     * @param rows an <code>int</code> specifying the number of rows to
     *    move from the current row; a positive number moves the cursor
     *    forward; a negative number moves the cursor backward
     * @return <code>true</code> if the cursor is on a row;
     *     <code>false</code> otherwise
     * @exception SQLException if a database access error occurs,
     *        there is no current row, or the result set type is
     *        <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean relative(int rows) throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        if (rResult.rRoot == null) {
            return false;
        }

        // if the direction is backward calculate the target row
        if (rows < 0) {
            rows = iCurrentRow + rows;

            // set status to beforeFirst status
            beforeFirst();

            // Exit if the target row is before the beginning of the resultset
            if (rows <= 0) {
                return false;
            }
        }

        while (rows-- > 0) {
            next();

            if (nCurrent == null) {
                break;
            }
        }

        // if nCurrent is null, the postion will be afterLast
        return nCurrent != null;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the previous row in this
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if it is off the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean previous() throws SQLException {

        checkClosed();

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Util.sqlException(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null || rResult.rRoot == null || iCurrentRow == 0) {

            // Empty resultset or no valid row
            return false;
        }

        if (bInit && nCurrent == null) {

            // Special condition: in an afterlast condition so go to last
            // row in the resultset
            return last();
        }

        int targetRow = iCurrentRow - 1;

        if (targetRow == 0) {

            // Have gone to a beforeFirst status. Not sure if the
            // beforeFirst status should be set or not.
            // The spec is not very clear.
            beforeFirst();

            return false;
        }

        // Go to the target row.  We always have to start from the first row
        // since the resultset is a forward direction list only
        first();

        while (targetRow != iCurrentRow) {
            nCurrent = nCurrent.next;

            iCurrentRow++;
        }

        return nCurrent != null;
    }

    //---------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------
// fredt@users - 20020902 - patch 1.7.1 - fetch size and direction
// We now interpret fetch size and direction as irrelevent to HSQLDB because
// the result set is built and returned as one whole data structure.
// Exceptions thrown are adjusted to mimimal and the javadoc updated.

    /**
     * <!-- start generic documentation -->
     * Gives a hint as to the direction in which the rows in this
     * <code>ResultSet</code> object will be processed.
     * The initial value is determined by the
     * <code>Statement</code> object
     * that produced this <code>ResultSet</code> object.
     * The fetch direction may be changed at any time. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB builds and returns result sets as a whole;
     * this method does nothing. However, as mandated by the JDBC standard,
     * an SQLException is thrown if the result set type is TYPE_FORWARD_ONLY
     * and a fetch direction other than FETCH_FORWARD is requested.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param direction an <code>int</code> specifying the suggested
     *  fetch direction; one of <code>ResultSet.FETCH_FORWARD</code>,
     *  <code>ResultSet.FETCH_REVERSE</code>, or
     *  <code>ResultSet.FETCH_UNKNOWN</code>
     * @exception SQLException if a database access error occurs or
     *  the result set type is <code>TYPE_FORWARD_ONLY</code> and the
     *  fetch direction is not <code>FETCH_FORWARD</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     * @see jdbcStatement#setFetchDirection
     * @see #getFetchDirection
     */
    public void setFetchDirection(int direction) throws SQLException {

        checkClosed();

        if (rsType == TYPE_FORWARD_ONLY && direction != FETCH_FORWARD) {
            throw Util.notSupported();
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the fetch direction for this
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB builds and returns result sets as a whole;
     * this method always returns <code>FETCH_FORWARD</code>, but the value
     * has no real meaning.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the current fetch direction for this <code>ResultSet</code>
     *   object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     * @see #setFetchDirection
     */
    public int getFetchDirection() throws SQLException {

        checkClosed();

        return FETCH_FORWARD;
    }

    /**
     * <!-- start generic documentation -->
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed for this
     * <code>ResultSet</code> object.
     * If the fetch size specified is zero, the JDBC driver
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the
     * <code>Statement</code> object
     * that created the result set.  The fetch size may be changed at any
     * time. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB builds and returns result sets
     * as a whole; this method does nothing.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs or the
     * condition <code>0 <= rows <= this.getMaxRows()</code> is not satisfied
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     * @see #getFetchSize
     * @see jdbcStatement#setFetchSize
     * @see jdbcStatement#getFetchSize
     */
    public void setFetchSize(int rows) throws SQLException {

        if (rows < 0) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the fetch size for this
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB builds and returns result sets
     * as a whole; the value returned (always 1) has no significance.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the current fetch size for this <code>ResultSet</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     * @see #setFetchSize
     * @see jdbcStatement#getFetchSize
     * @see jdbcStatement#setFetchSize
     */
    public int getFetchSize() throws SQLException {

        checkClosed();

        return 1;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the type of this <code>ResultSet</code> object.
     * The type is determined by the <code>Statement</code> object
     * that created the result set. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support and thus
     * never returns <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
     *     or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not supported)
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public int getType() throws SQLException {

        checkClosed();

        return rsType;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the concurrency mode of this <code>ResultSet</code> object.
     * The concurrency used is determined by the
     * <code>Statement</code> object that created the result set. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB supports only <code>CONCUR_READ_ONLY</code>;
     * this method always returns <code>CONCUR_READ_ONLY</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return the concurrency type, either
     *    <code>ResultSet.CONCUR_READ_ONLY</code>
     *    or <code>ResultSet.CONCUR_UPDATABLE</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public int getConcurrency() throws SQLException {

        checkClosed();

        return CONCUR_READ_ONLY;
    }

    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the current row has been updated.  The value returned
     * depends on whether or not the result set can detect updates. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always returns false.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if both (1) the row has been visibly updated
     *    by the owner or another and (2) updates are detected
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#updatesAreDetected
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public boolean rowUpdated() throws SQLException {

        checkClosed();

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the current row has had an insertion.
     * The value returned depends on whether or not this
     * <code>ResultSet</code> object can detect visible inserts. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always returns false.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if a row has had an insertion
     * and insertions are detected; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#insertsAreDetected
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public boolean rowInserted() throws SQLException {

        checkClosed();

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether
     * or not this <code>ResultSet</code> object can detect deletions. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always returns false.
     * </div>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if a row was deleted and deletions are
     *      detected; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#deletesAreDetected
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public boolean rowDeleted() throws SQLException {

        checkClosed();

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Gives a nullable column a null value.
     *
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code>
     * or <code>insertRow</code> methods are called to update the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.1, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public void updateNull(int columnIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>boolean</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public void updateBoolean(int columnIndex,
                              boolean x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>byte</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>short</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>int</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>long</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>float</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>double</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.math.BigDecimal</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateBigDecimal(int columnIndex,
                                 BigDecimal x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>String</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable results. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateString(int columnIndex, String x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>byte</code> array value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateTimestamp(int columnIndex,
                                Timestamp x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an ascii stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateAsciiStream(int columnIndex, java.io.InputStream x,
                                  int length) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a binary stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateBinaryStream(int columnIndex, java.io.InputStream x,
                                   int length) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a character stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateCharacterStream(int columnIndex, java.io.Reader x,
                                      int length) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMA</code>
     * or <code>java.sql.Types.NUMERIC</code> types,
     * this is the number of digits after the decimal point.  For all other
     * types this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateObject(int columnIndex, Object x,
                             int scale) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>null</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateNull(String columnName) throws SQLException {
        updateNull(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>boolean</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateBoolean(String columnName,
                              boolean x) throws SQLException {
        updateBoolean(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>byte</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateByte(String columnName, byte x) throws SQLException {
        updateByte(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>short</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateShort(String columnName, short x) throws SQLException {
        updateShort(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>int</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateInt(String columnName, int x) throws SQLException {
        updateInt(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>long</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateLong(String columnName, long x) throws SQLException {
        updateLong(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>float</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateFloat(String columnName, float x) throws SQLException {
        updateFloat(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>double</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateDouble(String columnName,
                             double x) throws SQLException {
        updateDouble(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.BigDecimal</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateBigDecimal(String columnName,
                                 BigDecimal x) throws SQLException {
        updateBigDecimal(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>String</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateString(String columnName,
                             String x) throws SQLException {
        updateString(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a byte array value.
     *
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateBytes(String columnName, byte[] x) throws SQLException {
        updateBytes(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateDate(String columnName, Date x) throws SQLException {
        updateDate(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateTime(String columnName, Time x) throws SQLException {
        updateTime(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateTimestamp(String columnName,
                                Timestamp x) throws SQLException {
        updateTimestamp(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an ascii stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateAsciiStream(String columnName, java.io.InputStream x,
                                  int length) throws SQLException {
        updateAsciiStream(findColumn(columnName), x, length);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a binary stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateBinaryStream(String columnName, java.io.InputStream x,
                                   int length) throws SQLException {
        updateBinaryStream(findColumn(columnName), x, length);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a character stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param reader the <code>java.io.Reader</code> object containing
     *   the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateCharacterStream(String columnName,
                                      java.io.Reader reader,
                                      int length) throws SQLException {
        updateCharacterStream(findColumn(columnName), reader, length);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMAL</code>
     * or <code>java.sql.Types.NUMERIC</code> types,
     * this is the number of digits after the decimal point.  For all other
     * types this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateObject(String columnName, Object x,
                             int scale) throws SQLException {
        updateObject(findColumn(columnName), x, scale);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateObject(String columnName,
                             Object x) throws SQLException {
        updateObject(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Inserts the contents of the insert row into this
     * <code>ResultSet</code> object and into the database.
     * The cursor must be on the insert row when this method is called. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs,
     * if this method is called when the cursor is not on the insert row,
     * or if not all of non-nullable columns in
     * the insert row have been given a value
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void insertRow() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the underlying database with the new contents of the
     * current row of this <code>ResultSet</code> object.
     * This method cannot be called when the cursor is on the insert row. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs or
     * if this method is called when the cursor is on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateRow() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Deletes the current row from this <code>ResultSet</code> object
     * and from the underlying database.  This method cannot be called when
     * the cursor is on the insert row. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * or if this method is called when the cursor is on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void deleteRow() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Refreshes the current row with its most recent value in
     * the database.  This method cannot be called when
     * the cursor is on the insert row.
     *
     * <P>The <code>refreshRow</code> method provides a way for an
     * application to
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver
     * may actually refresh multiple rows at once if the fetch size is
     * greater than one.
     *
     * <P> All values are refetched subject to the transaction isolation
     * level and cursor sensitivity.  If <code>refreshRow</code> is called
     * after calling an updater method, but before calling
     * the method <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method
     * <code>refreshRow</code> frequently will likely slow performance. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error
     * occurs or if this method is called when the cursor is on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public void refreshRow() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Cancels the updates made to the current row in this
     * <code>ResultSet</code> object.
     * This method may be called after calling an
     * updater method(s) and before calling
     * the method <code>updateRow</code> to roll back
     * the updates made to a row.  If no updates have been made or
     * <code>updateRow</code> has already been called, this method has no
     * effect. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error
     *       occurs or if this method is called when the cursor is
     *       on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void cancelRowUpdates() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the insert row.  The current cursor position is
     * remembered while the cursor is positioned on the insert row.
     *
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the updater methods prior to
     * inserting the row into the result set.
     *
     * Only the updater, getter,
     * and <code>insertRow</code> methods may be
     * called when the cursor is on the insert row.  All of the columns in
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.
     * An updater method must be called before a
     * getter method can be called on a column value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void moveToInsertRow() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on
     * the insert row. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method is ignored.
     * </div>
     * <!-- end release-specific documentation -->
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void moveToCurrentRow() throws SQLException {}

    /**
     * <!-- start generic documentation -->
     * Retrieves the <code>Statement</code> object that produced this
     * <code>ResultSet</code> object.
     * If the result set was generated some other way, such as by a
     * <code>DatabaseMetaData</code> method, this method returns
     * <code>null</code>. <p>
     * <!-- end generic documentation -->
     *
     * @return the <code>Statment</code> object that produced
     * this <code>ResultSet</code> object or <code>null</code>
     * if the result set was produced some other way
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public Statement getStatement() throws SQLException {
        return (Statement) sqlStatement;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Object</code>
     * in the Java programming language.
     * If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>,
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @param map a <code>java.util.Map</code> object that contains the
     *  mapping from SQL type names to classes in the Java programming
     *  language
     * @return an <code>Object</code> in the Java programming language
     * representing the SQL value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public Object getObject(int i, Map map) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Ref</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Ref</code> object representing an SQL <code>REF</code>
     *  value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public Ref getRef(int i) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Blob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, this feature is supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Blob</code> object representing the SQL
     *  <code>BLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */

//#ifdef JAVA2
    public Blob getBlob(int i) throws SQLException {

        byte[] b = getBytes(i);

        return b == null ? null
                         : new jdbcBlob(b);
    }

//#endif JAVA2

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Clob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, this feature is supported. <p>
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Clob</code> object representing the SQL
     *   <code>CLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
//#ifdef JAVA2
    public Clob getClob(int i) throws SQLException {

        String s = getString(i);

        return s == null ? null
                         : new jdbcClob(s);
    }

//#endif JAVA2

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Array</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return an <code>Array</code> object representing the SQL
     *   <code>ARRAY</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Array getArray(int i) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Object</code>
     * in the Java programming language.
     * If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * This method uses the specified <code>Map</code> object for
     * custom mapping if appropriate. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @param map a <code>java.util.Map</code> object that contains the
     *   mapping from SQL type names to classes in the Java programming
     *   language
     * @return an <code>Object</code> representing the SQL value in the
     *   specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Object getObject(String colName, Map map) throws SQLException {

        // MODIFIED:
        // made this consistent with all other
        // column name oriented methods
        // boucherb@users 2002013
        return getObject(findColumn(colName), map);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Ref</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>,
     * stating that the operartion is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     * @param colName the column name
     * @return a <code>Ref</code> object representing the SQL <code>REF</code>
     *   value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Ref getRef(String colName) throws SQLException {
        return getRef(findColumn(colName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Blob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, this feature is supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Blob</code> object representing the
     *   SQL <code>BLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */

//#ifdef JAVA2
    public Blob getBlob(String colName) throws SQLException {
        return getBlob(findColumn(colName));
    }

//#endif JAVA2

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Clob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, this feature is supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Clob</code> object representing the SQL
     *   <code>CLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
//#ifdef JAVA2
    public Clob getClob(String colName) throws SQLException {
        return getClob(findColumn(colName));
    }

//#endif JAVA2

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Array</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @return an <code>Array</code> object representing the SQL
     *   <code>ARRAY</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Array getArray(String colName) throws SQLException {
        return getArray(findColumn(colName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Date</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the date if the underlying database does
     * not store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {

        Date date = getDate(columnIndex);

        if (date == null) {
            return null;
        }

        if (cal == null) {
            return date;
        }

        cal.setTime(date);
        HsqlDateTime.resetToDate(cal);

        return new Date(cal.getTime().getTime());
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Date</code>
     * object in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond
     * value for the date if the underlying database does not store
     * timezone information. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column from which to retrieve the
     *   value
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return getDate(findColumn(columnName), cal);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code>
     * object in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the time if the underlying database does not
     * store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {

        Time t = getTime(columnIndex);

        if (t == null) {
            return null;
        }

        if (cal == null) {
            return t;
        }

        cal.setTime(t);
        HsqlDateTime.resetToTime(cal);

        return new Time(cal.getTime().getTime());
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.Time</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond
     * value for the time if the underlying database does not store
     * timezone information. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     *   if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return getTime(findColumn(columnName), cal);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Timestamp</code> object in the Java programming
     * anguage.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the timestamp if the underlying database does
     * not store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the timestamp
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Timestamp getTimestamp(int columnIndex,
                                  Calendar cal) throws SQLException {

        Timestamp ts = getTimestamp(columnIndex);

        if (cal != null && ts != null) {
            ts.setTime(HsqlDateTime.getTimeInMillis(ts, null, cal));
        }

        return ts;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Timestamp</code> object in the Java programming
     * language.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the timestamp if the underlying database does
     * not store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the date
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Timestamp getTimestamp(String columnName,
                                  Calendar cal) throws SQLException {
        return getTimestamp(findColumn(columnName), cal);
    }

    //-------------------------- JDBC 3.0 ----------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.net.URL</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature. <p>
     *
     * This method always throws an <code>SQLException</code>
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the index of the column 1 is the first, 2
     *    is the second,...
     * @return the column value as a <code>java.net.URL</code> object;
     *    if the value is SQL <code>NULL</code>, the value returned
     *    is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs,
     *    or if a URL is malformed
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public java.net.URL getURL(int columnIndex) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.net.URL</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support this feature.  <p>
     *
     * This method always throws an <code>SQLException</code>
     * stating that the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value as a <code>java.net.URL</code> object;
     * if the value is SQL <code>NULL</code>, the value returned
     * is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     *       or if a URL is malformed
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public java.net.URL getURL(String columnName) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Ref</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateRef(int columnIndex,
                          java.sql.Ref x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Ref</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateRef(String columnName,
                          java.sql.Ref x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Blob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateBlob(int columnIndex,
                           java.sql.Blob x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Blob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateBlob(String columnName,
                           java.sql.Blob x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Clob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateClob(int columnIndex,
                           java.sql.Clob x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Clob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateClob(String columnName,
                           java.sql.Clob x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Array</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateArray(int columnIndex,
                            java.sql.Array x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Array</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Including 1.7.2, HSQLDB does not support updateable result sets. <p>
     *
     * This method always throws an SQLException, stating that
     * the operation is not supported.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void updateArray(String columnName,
                            java.sql.Array x) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3
    //-------------------- Internal Implementation -------------------------
// Support for JDBC 2 from JRE 1.1.x

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int FETCH_FORWARD = 1000;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int FETCH_REVERSE = 1001;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int FETCH_UNKNOWN = 1002;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int TYPE_FORWARD_ONLY = 1003;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int TYPE_SCROLL_INSENSITIVE = 1004;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int TYPE_SCROLL_SENSITIVE = 1005;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int CONCUR_READ_ONLY = 1007;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int CONCUR_UPDATABLE = 1008;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int HOLD_CURSORS_OVER_COMMIT = 1;

    /** Copy of java.sql.ResultSet constant, for JDK 1.1 clients. */
    public static final int CLOSE_CURSORS_AT_COMMIT = 2;

    //---------------------------- Private ---------------------------------

    /**
     * Internal row data availability check.
     *
     * @throws  SQLException when no row data is available
     */
    private void checkAvailable() throws SQLException {

        if (rResult == null ||!bInit || nCurrent == null) {
            throw Util.sqlException(Trace.NO_DATA_IS_AVAILABLE);
        }
    }

    /**
     * Internal closed state check.
     *
     * @throws SQLException when this result set is closed
     */
    private void checkClosed() throws SQLException {

        if (rResult == null
                || (sqlStatement != null && sqlStatement.isClosed)) {
            throw Util.sqlException(Trace.JDBC_RESULTSET_IS_CLOSED);
        }
    }

    /**
     * Internal column index validity check.
     *
     * @param columnIndex to check
     * @throws SQLException when this ResultSet has no such column
     */
    void checkColumn(int columnIndex) throws SQLException {

        if (columnIndex < 1 || columnIndex > iColumnCount) {
            throw Util.sqlException(Trace.COLUMN_NOT_FOUND,
                                    String.valueOf(columnIndex));
        }
    }

    /**
     * Internal wasNull tracker.
     *
     * @param  o the Object to track
     */
    private boolean checkNull(Object o) {

        if (o == null) {
            bWasNull = true;

            return true;
        } else {
            bWasNull = false;

            return false;
        }
    }

    /**
     * Internal value converter. <p>
     *
     * All trivially successful getXXX methods eventually go through this
     * method, converting if neccessary from the hsqldb-native representation
     * of a column's value to the requested representation.  <p>
     *
     * @return an Object of the requested type, representing the value of the
     *       specified column
     * @param columnIndex of the column value for which to perform the
     *                 conversion
     * @param type the org.hsqldb.Types code for type
     * @throws SQLException when there is no data, the column index is
     *    invalid, or the conversion cannot be performed
     */
    private Object getColumnInType(int columnIndex,
                                   int type) throws SQLException {

        checkAvailable();

        int    t;
        Object o;

        try {
            t = rResult.metaData.colTypes[--columnIndex];
            o = nCurrent.data[columnIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw Util.sqlException(Trace.COLUMN_NOT_FOUND,
                                    String.valueOf(++columnIndex));
        }

        if (checkNull(o)) {
            return null;
        }

        if (t != type) {
            if (o instanceof Binary && type != Types.CHAR) {
                throw Util.sqlException(Trace.WRONG_DATA_TYPE);
            }

            // try to convert
            try {
                o = Column.convertObject(o, type);
            } catch (Exception e) {
                String s = "type: " + Types.getTypeString(t) + " (" + t
                           + ") expected: " + Types.getTypeString(type)
                           + " value: " + o.toString();

                throw Util.sqlException(Trace.WRONG_DATA_TYPE, s);
            }
        }

        // treat datetime stuff
        switch (type) {

            case Types.DATE :
                return new Date(((Date) o).getTime());

            case Types.TIME :
                return new Time(((Time) o).getTime());

            case Types.TIMESTAMP :
                long      m  = ((Timestamp) o).getTime();
                int       n  = ((Timestamp) o).getNanos();
                Timestamp ts = new Timestamp(m);

                ts.setNanos(n);

                return ts;
        }

        return o;
    }

    //-------------------------- Package Private ---------------------------

    /**
     * Constructs a new <code>jdbcResultSet</code> object using the specified
     * <code>org.hsqldb.Result</code>. <p>
     *
     * @param s the statement
     * @param r the internal result form that the new
     *      <code>jdbcResultSet</code> represents
     * @param props the connection properties
     * @exception SQLException when the supplied Result is of type
     * org.hsqldb.Result.ERROR
     */
    jdbcResultSet(jdbcStatement s, Result r, HsqlProperties props,
                  boolean isNetConnection) throws SQLException {

        sqlStatement   = s;
        connProperties = props;
        this.isNetConn = isNetConnection;

        if (r.mode == ResultConstants.UPDATECOUNT) {
            iUpdateCount = r.getUpdateCount();
        } else if (r.isError()) {
            Util.throwError(r);
        } else {
            if (s != null) {
                this.rsType = s.rsType;
            }

            iUpdateCount = -1;
            rResult      = r;
            iColumnCount = r.getColumnCount();
        }

        bWasNull = false;
    }

    /**
     * If executing the statement updated rows on the database, how many were
     * affected?
     *
     * @return the number of rows affected by executing my statement
     */
    int getUpdateCount() {
        return iUpdateCount;
    }

    /**
     * Does this Result contain actual row data? <p>
     *
     * Not all results have row data.  Some are ERROR results
     * (an execption occured while executing my statement), and
     * some are UPDATE results, in which case updates occured to rows
     * on the database, but no rows were actually returned.
     *
     * @return true if Result has row data, false if not.
     */
    boolean isResult() {
        return rResult == null ? false
                               : true;
    }
}
