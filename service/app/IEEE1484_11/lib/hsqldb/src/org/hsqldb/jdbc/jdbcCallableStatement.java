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

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Calendar;

//#ifdef JAVA2
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.util.Map;

//#endif JAVA2
import org.hsqldb.HsqlException;
import org.hsqldb.Trace;
import org.hsqldb.lib.IntValueHashMap;

// boucherb@users patch 1.7.2 - CallableStatement impl removed
// from jdbcPreparedStatement and moved here; sundry changes elsewhere to
// comply
// TODO: 1.7.2 Alpha N :: DONE
//       maybe implement set-by-parameter-name.  We have an informal spec,
//       being "@p1" => 1, "@p2" => 2, etc.  Problems: return value is "@p0"
//       and there is no support for registering the return value as an out
//       parameter.
// TODO: 1.8.x
//       engine and client-side mechanisms for adding, retrieving,
//       navigating (and perhaps controlling holdability of) multiple
//       results generated from a single execution.
// boucherb@users 2004-03/04-xx - patch 1.7.2 - some minor code cleanup
//                                            - parameter map NPE correction
//                                            - embedded SQL/SQLCLI client usability
//                                              (parameter naming changed from @n to @pn)
// boucherb@users 2004-04-xx - doc 1.7.2 - javadocs added/updated

/**
 * <!-- start generic documentation -->
 *
 * The interface used to execute SQL stored procedures.  The JDBC API
 * provides a stored procedure SQL escape syntax that allows stored
 * procedures to be called in a standard way for all RDBMSs. This escape
 * syntax has one form that includes a result parameter and one that does
 * not. If used, the result parameter must be registered as an OUT parameter.
 * The other parameters can be used for input, output or both. Parameters
 * are referred to sequentially, by number, with the first parameter being 1.
 * <PRE>
 *   {?= call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 *   {call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 * </PRE>
 * <P>
 * IN parameter values are set using the <code>set</code> methods inherited from
 * {@link PreparedStatement}.  The type of all OUT parameters must be
 * registered prior to executing the stored procedure; their values
 * are retrieved after execution via the <code>get</code> methods provided here.
 * <P>
 * A <code>CallableStatement</code> can return one {@link ResultSet} object or
 * multiple <code>ResultSet</code> objects.  Multiple
 * <code>ResultSet</code> objects are handled using operations
 * inherited from {@link Statement}.
 * <P>
 * For maximum portability, a call's <code>ResultSet</code> objects and
 * update counts should be processed prior to getting the values of output
 * parameters.
 * <P>
 * <!-- end generic documentation -->
 * <!-- start Release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h3>HSQLDB-Specific Information:</h3> <p>
 *
 * Since 1.7.2, the JDBC CallableStatement interface implementation has been
 * broken out of the jdbcPreparedStatement class into this one. <p>
 *
 * With 1.7.2, some of the previously unsupported features of this interface
 * are now supported, such as the parameterName-based setter methods. <p>
 *
 * More importantly, jdbcCallableStatement objects are now backed by a true
 * compiled parameteric representation. Hence, there are now significant
 * performance gains to be had by using a CallableStatement object instead of
 * a Statement object, if a short-running CALL statement is to be executed more
 * than a small number of times.  Moreover, the recent work lays the foundation
 * for work in a subsequenct release to support CallableStatement OUT and
 * IN OUT style parameters, as well as the generation and retrieval of multiple
 * results in response to the execution of a CallableStatement object. <p>
 *
 * For a more in-depth discussion of performance issues regarding 1.7.2
 * prepared and callable statement objects, please see overview section of
 * {@link jdbcPreparedStatement jdbcPreparedStatment}.
 *
 * <hr>
 *
 * As with many DBMS, HSQLDB support for stored procedures is not provided in
 * a completely standard fashion. <p>
 *
 * Beyond the XOpen/ODBC extended scalar functions, stored procedures are
 * typically supported in ways that vary greatly from one DBMS implementation
 * to the next.  So, it is almost guaranteed that the code for a stored
 * procedure written under a specific DBMS product will not work without
 * at least some modification in the context of another vendor's product
 * or even across a single vendor's product lines.  Moving stored procedures
 * from one DBMS product line to another almost invariably involves complex
 * porting issues and often may not be possible at all. <em>Be warned</em>. <p>
 *
 * At present, HSQLDB stored procedures map directly onto the methods of
 * compiled Java classes found on the classpath of the engine at runtime. This
 * is done in a non-standard but fairly efficient way by issuing a class
 * grant (and possibly method aliases) of the form: <p>
 *
 * <PRE class="SqlCodeExample">
 * GRANT ALL ON CLASS &quot;package.class&quot; TO [&lt;user-name&gt; | PUBLIC]
 * CREATE ALIAS &ltcall-alias&gt; FOR &quot;package.class.method&quot; -- optional
 * </PRE>
 *
 * This has the effect of allowing the specified user(s) to access the
 * set of uniquely named public static methods of the specified class,
 * in either the role of SQL functions or stored procedures.

 * For example: <p>
 *
 * <PRE class="SqlCodeExample">
 * CONNECT &lt;admin-user&gt; PASSWORD &lt;admin-user-password&gt;;
 * GRANT ALL ON CLASS &quot;org.myorg.MyClass&quot; TO PUBLIC;
 * CREATE ALIAS sp_my_method FOR &quot;org.myorg.MyClass.myMethod&quot;
 * CONNECT &lt;any-user&gt; PASSWORD &lt;any-user-password&gt;;
 * SELECT &quot;org.myorg.MyClass.myMethod&quot;(column_1) FROM table_1;
 * SELECT sp_my_method(column_1) FROM table_1;
 * CALL 2 + &quot;org.myorg.MyClass.myMethod&quot;(-5);
 * CALL 2 + sp_my_method(-5);
 * </PRE>
 *
 * Please note the use of the term &quot;uniquely named&quot; above. Including
 * 1.7.2, no support is provided to deterministically resolve overloaded
 * method names, and there can be issues with inherited methods as well;
 * currently, it is strongly recommended that developers creating stored
 * procedure library classes for HSQLDB simply avoid designs such that SQL
 * stored procedure calls attempt to resolve to: <p>
 *
 * <ol>
 * <li>inherited public static methods
 * <li>overloaded public static methods
 * </ol>
 *
 * Also, please note that <code>OUT</code> and <code>IN OUT</code> parameters
 * are not yet supported due to some unresolved low level support issues. <p>
 *
 * Including 1.7.2, the HSQLDB stored procedure call mechanism is essentially a
 * thin wrap of the HSQLDB SQL function call mechanism, extended to include the
 * more general HSQLDB SQL expression evaluation mechanism.  In addition to
 * stored procedure calls that resolve directly to Java method invocations, the
 * extention provides the ability to evaluate simple SQL expressions, possibly
 * containing Java method invocations, outside any <code>INSERT</code>,
 * <code>UPDATE</code>, <code>DELETE</code> or <code>SELECT</code> statement
 * context. <p>
 *
 * With HSQLDB, executing a <code>CALL</code> statement that produces an opaque
 * (OTHER) or known scalar object reference has virtually the same effect as:
 *
 * <PRE class="SqlCodeExample">
 * CREATE TABLE DUAL (dummy VARCHAR);
 * INSERT INTO DUAL VALUES(NULL);
 * SELECT &lt;simple-expression&gt; FROM DUAL;
 * </PRE>
 *
 * As a transitional measure, HSQLDB provides the ability to materialize a
 * general result set in response to stored procedure execution.  In this case,
 * the stored procedure's Java method descriptor must specify a return type of
 * java.lang.Object for external use (although at any point in the devlopment
 * cycle, other, proprietary return types may accepted internally for engine
 * development purposes).

 * When HSQLDB detects that the runtime class of the resulting Object is
 * elligible, an automatic internal unwrapping is performed to correctly
 * expose the underlying result set to the client, whether local or remote. <p>
 *
 * Additionally, HSQLDB automatically detects if java.sql.Connection is
 * the class of the first argument of any underlying Java method(s).  If so,
 * then the engine transparently supplies the internal Connection object
 * corresponding to the Session executing the call, adjusting the positions
 * of other arguments to suite the SQL context. <p>
 *
 * The features above are not intended to be permanent.  Rather, the intention
 * is to offer more general and powerful mechanisms in a future release;
 * it is recommend to use them only as a temporary convenience. <p>
 *
 * For instance, one might be well advised to future-proof by writing
 * HSQLDB-specific adapter methods that in turn call the real logic of an
 * underlying generalized JDBC stored procedure library. <p>
 *
 * Here is a very simple example of an HSQLDB stored procedure generating a
 * user-defined result set:
 *
 * <pre class="JavaCodeExample">
 * <span class="JavaKeyWord">package</span> mypackage;
 *
 * <span class="JavaKeyWord">class</span> MyClass {
 *
 *      <span class="JavaKeyWord">public static</span> Object <b>mySp</b>(Connection conn) <span class="JavaKeyWord">throws</span> SQLException {
 *          <span class="JavaKeyWord">return</span> conn.<b>createStatement</b>().<b>executeQuery</b>(<span class="JavaStringLiteral">"select * from my_table"</span>);
 *      }
 * }
 * </pre>
 *
 * Here is a refinement demonstrating no more than the bare essence of the idea
 * behind a more portable style:
 *
 * <pre class="JavaCodeExample">
 * <span class="JavaKeyWord">package</span> mypackage;
 *
 * <span class="JavaKeyWord">import</span> java.sql.ResultSet;
 * <span class="JavaKeyWord">import</span> java.sql.SQLException;
 *
 * <span class="JavaKeyWord">class</span> MyLibraryClass {
 *
 *      <span class="JavaKeyWord">public static</span> ResultSet <b>mySp()</b> <span class="JavaKeyWord">throws</span> SQLException {
 *          <span class="JavaKeyWord">return</span> ctx.<b>getConnection</b>().<b>createStatement</b>().<b>executeQuery</b>(<span class="JavaStringLiteral">"select * from my_table"</span>);
 *      }
 * }
 *
 * //--
 *
 * <span class="JavaKeyWord">package</span> myadaptorpackage;
 *
 * <span class="JavaKeyWord">import</span> java.sql.Connection;
 * <span class="JavaKeyWord">import</span> java.sql.SQLException;
 *
 * <span class="JavaKeyWord">class</span> MyAdaptorClass {
 *
 *      <span class="JavaKeyWord">public static</span> Object <b>mySp</b>(Connection conn) <span class="JavaKeyWord">throws</span> SQLException {
 *          MyLibraryClass.<b>getCtx()</b>.<b>setConnection</b>(conn);
 *          <span class="JavaKeyWord">return</span> MyLibraryClass.<b>mySp</b>();
 *      }
 * }
 * </pre>
 *
 * In a future release, it is intended to provided some new features
 * that will support writing fairly portable JDBC-based stored procedure
 * code: <P>
 *
 * <ul>
 *  <li> Support for the <span class="JavaStringLiteral">"jdbc:default:connection"</span>
 *       standard database connection url. <p>
 *
 *  <li> A well-defined specification of the behaviour of the HSQLDB execution
 *       stack under stored procedure calls. <p>
 *
 *  <li> A well-defined, pure JDBC specification for generating multiple
 *       results from HSQLDB stored procedures for client retrieval.
 * </ul>
 *
 * (boucherb@users)
 * </div>
 * <!-- end Release-specific documentation -->
 *
 * @author boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 * @see jdbcConnection#prepareCall
 * @see jdbcResultSet
 */
public class jdbcCallableStatement extends jdbcPreparedStatement
implements CallableStatement {

    /** parameter name => parameter index */
    private IntValueHashMap parameterNameMap;

    /** parameter index => registered OUT type */

    //    private IntKeyIntValueHashMap outRegistrationMap;

    /**
     * Constructs a new jdbcCallableStatement with the specified connection and
     * result type.
     *
     * @param  c the connection on which this statement will execute
     * @param sql the SQL statement this object represents
     * @param type the type of result this statement will produce
     * @throws HsqlException if the statement is not accepted by the database
     * @throws SQLException if preprocessing by driver fails
     */
    public jdbcCallableStatement(jdbcConnection c, String sql,
                                 int type)
                                 throws HsqlException, SQLException {

        super(c, sql, type);

        String[] names;
        String   name;

        // outRegistrationMap = new IntKeyIntValueHashMap();
        parameterNameMap = new IntValueHashMap();

        if (pmdDescriptor != null && pmdDescriptor.metaData != null) {
            names = pmdDescriptor.metaData.colNames;

            for (int i = 0; i < names.length; i++) {
                name = names[i];

                // PRE:  should never happen in practice
                if (name == null || name.length() == 0) {
                    continue;    // throw?
                }

                parameterNameMap.put(name, i);
            }
        }
    }

    /**
     * Retrieves the parameter index corresponding to the given
     * parameter name. <p>
     *
     * @param parameterName to look up
     * @throws SQLException if not found
     * @return index for name
     */
    int findParameterIndex(String parameterName) throws SQLException {

        checkClosed();

        int index = parameterNameMap.get(parameterName, -1);

        if (index >= 0) {
            return index + 1;
        }

        throw Util.sqlException(Trace.COLUMN_NOT_FOUND, parameterName);
    }

    /**
     * Does the specialized work required to free this object's resources and
     * that of it's parent classes. <p>
     *
     * @throws SQLException if a database access error occurs
     */
    public void close() throws SQLException {

        if (isClosed()) {
            return;
        }

        // outRegistrationMap = null;
        parameterNameMap = null;

        super.close();
    }

    /**
     * Performs an internal check for OUT or IN OUT column index validity. <p>
     *
     * @param i the one-based column index to check
     * @throws SQLException if there is no such OUT or IN OUT column
     */
    private void checkGetParameterIndex(int i) throws SQLException {

        checkClosed();

        if (i < 1 || i > parameterModes.length) {
            String msg = "Parameter index out of bounds: " + i;

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }
/*
        int mode = parameterModes[i - 1];

        switch (mode) {

            default :
                String msg = "Not OUT or IN OUT mode: " + mode
                             + " for parameter: " + i;

                throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
            case Expression.PARAM_IN_OUT :
            case Expression.PARAM_OUT :
                break;

            // this is OK
        }
 */
    }

    /**
     * Checks if the parameter of the given index has been successfully
     * registered as an OUT parameter. <p>
     *
     * @param parameterIndex to check
     * @throws SQLException if not registered
     */
    /*
    private void checkIsRegisteredParameterIndex(int parameterIndex)
    throws SQLException {

        int    type;
        String msg;

        checkClosed();

        type = outRegistrationMap.get(parameterIndex, Integer.MIN_VALUE);

        if (type == Integer.MIN_VALUE) {
            msg = "Parameter not registered: " + parameterIndex;

            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }
    }
    */

// ----------------------------------- JDBC 1 ----------------------------------

    /**
     * <!-- start generic documentation -->
     * Registers the OUT parameter in ordinal position
     * <code>parameterIndex</code> to the JDBC type
     * <code>sqlType</code>.  All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * If the JDBC type expected to be returned to this output parameter
     * is specific to this particular database, <code>sqlType</code>
     * should be <code>java.sql.Types.OTHER</code>.  The method
     * {@link #getObject} retrieves the value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @param sqlType the JDBC type code defined by <code>java.sql.Types</code>.
     *   If the parameter is of JDBC type <code>NUMERIC</code>
     *   or <code>DECIMAL</code>, the version of
     *   <code>registerOutParameter</code> that accepts a scale value
     *   should be used.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public void registerOutParameter(int parameterIndex,
                                     int sqlType) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Registers the parameter in ordinal position
     * <code>parameterIndex</code> to be of JDBC type
     * <code>sqlType</code>.  This method must be called
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * This version of <code>registerOutParameter</code> should be
     * used when the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param sqlType the SQL type code defined by <code>java.sql.Types</code>.
     * @param scale the desired number of digits to the right of the
     * decimal point.  It must be greater than or equal to zero.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public void registerOutParameter(int parameterIndex, int sqlType,
                                     int scale) throws SQLException {
        registerOutParameter(parameterIndex, sqlType);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the last OUT parameter read had the value of
     * SQL <code>NULL</code>.  Note that this method should be called only
     * after calling a getter method; otherwise, there is no value to use in
     * determining whether it is <code>null</code> or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the last parameter read was SQL
     * <code>NULL</code>; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean wasNull() throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>CHAR</code>,
     * <code>VARCHAR</code>, or <code>LONGVARCHAR</code> parameter as a
     * <code>String</code> in the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>CHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the (JDBC4 clarification:) SQL
     * <code>CHAR</code> value had in the
     * database, including any padding added by the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value. If the value is SQL <code>NULL</code>,
     *    the result
     *    is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setString
     */
    public String getString(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BIT</code> parameter
     * as a <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *  and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>false</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBoolean
     */
    public boolean getBoolean(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TINYINT</code>
     * parameter as a <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setByte
     */
    public byte getByte(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>SMALLINT</code>
     * parameter as a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setShort
     */
    public short getShort(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>INTEGER</code>
     * parameter as an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setInt
     */
    public int getInt(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BIGINT</code>
     * parameter as a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setLong
     */
    public long getLong(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>FLOAT</code>
     * parameter as a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *  and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the
     *   result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setFloat
     */
    public float getFloat(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DOUBLE</code>
     * parameter as a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDouble
     */
    public double getDouble(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>NUMERIC</code>
     * parameter as a <code>java.math.BigDecimal</code> object with
     * <i>scale</i> digits to the right of the decimal point. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *  and so on
     * @param scale the number of digits to the right of the decimal point
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @deprecated use <code>getBigDecimal(int parameterIndex)</code>
     *       or <code>getBigDecimal(String parameterName)</code>
     * @see #setBigDecimal
     */

//#ifdef DEPRECATEDJDBC
    public BigDecimal getBigDecimal(int parameterIndex,
                                    int scale) throws SQLException {
        throw Util.notSupported();
    }

//#endif

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BINARY</code> or
     * <code>VARBINARY</code> parameter as an array of <code>byte</code>
     * values in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBytes
     */
    public byte[] getBytes(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DATE</code> parameter
     * as a <code>java.sql.Date</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the
     *    result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     */
    public Date getDate(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIME</code> parameter
     * as a <code>java.sql.Time</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     */
    public Time getTime(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIMESTAMP</code>
     * parameter as a <code>java.sql.Timestamp</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     */
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated parameter as an <code>Object</code>
     * in the Java programming language. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * <p>
     * This method returns a Java object whose type corresponds to the JDBC
     * type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target JDBC
     * type as <code>java.sql.Types.OTHER</code>, this method can be used
     * to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return A <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject
     */
    public Object getObject(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

// ----------------------------------- JDBC 2 ----------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>NUMERIC</code>
     * parameter as a <code>java.math.BigDecimal</code> object with as many
     * digits to the right of the decimal point as the value contains. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value in full precision.  If the value is
     * SQL <code>NULL</code>, the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBigDecimal
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Returns an object representing the value of OUT parameter
     * <code>parameterIndex</code> and uses <code>map</code> for the custom
     * mapping of the parameter value.
     * <p>
     * This method returns a Java object whose type corresponds to the
     * JDBC type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target
     * JDBC type as <code>java.sql.Types.OTHER</code>, this method can
     * be used to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, and so on
     * @param map the mapping from SQL type names to Java classes
     * @return a <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @see #setObject
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */
    public Object getObject(int parameterIndex, Map map) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC
     * <code>REF(&lt;structured-type&gt;)</code> parameter as a
     * {@link java.sql.Ref} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value as a <code>Ref</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>,
     * the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */
    public Ref getRef(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BLOB</code>
     * parameter as a {@link java.sql.Blob} object in the Java
     * programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value as a <code>Blob</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>,
     * the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public Blob getBlob(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>CLOB</code>
     * parameter as a {@link java.sql.Clob} object in the Java programming
     * language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, and
     * so on
     * @return the parameter value as a <code>Clob</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>, the
     * value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public Clob getClob(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>ARRAY</code>
     * parameter as an {@link java.sql.Array} object in the Java programming
     * language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, and
     * so on
     * @return the parameter value as an <code>Array</code> object in
     * the Java programming language.  If the value was SQL <code>NULL</code>,
     * the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public Array getArray(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DATE</code>
     * parameter as a <code>java.sql.Date</code> object, using
     * the given <code>Calendar</code> object
     * to construct the date.
     * With a <code>Calendar</code> object, the driver
     * can calculate the date taking into account a custom timezone and
     * locale.  If no <code>Calendar</code> object is specified, the driver
     * uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *      and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the date
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *      jdbcPreparedStatement)
     */
    public Date getDate(int parameterIndex,
                        Calendar cal) throws SQLException {

        throw Util.notSupported();

//        try {
//            return HsqlDateTime.getDate(getString(parameterIndex), cal);
//        } catch (Exception e) {
//            throw Util.sqlException(Trace.INVALID_ESCAPE,
//                                          e.getMessage());
//        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIME</code>
     * parameter as a <code>java.sql.Time</code> object, using
     * the given <code>Calendar</code> object
     * to construct the time.
     * With a <code>Calendar</code> object, the driver
     * can calculate the time taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *        to construct the time
     * @return the parameter value; if the value is SQL <code>NULL</code>,
     *     the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcPreparedStatement)
     */
    public Time getTime(int parameterIndex,
                        Calendar cal) throws SQLException {

        throw Util.notSupported();

//        try {
//            return HsqlDateTime.getTime(getString(parameterIndex), cal);
//        } catch (Exception e) {
//            throw Util.sqlException(Trace.INVALID_ESCAPE,
//                                          e.getMessage());
//        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIMESTAMP</code>
     * parameter as a <code>java.sql.Timestamp</code> object, using
     * the given <code>Calendar</code> object to construct
     * the <code>Timestamp</code> object.
     * With a <code>Calendar</code> object, the driver
     * can calculate the timestamp taking into account a custom timezone and
     * locale. If no <code>Calendar</code> object is specified, the driver
     * uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *        to construct the timestamp
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *        the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcPreparedStatement)
     */
    public Timestamp getTimestamp(int parameterIndex,
                                  Calendar cal) throws SQLException {

        throw Util.notSupported();

//        try {
//            return HsqlDateTime.getTimestamp(getString(parameterIndex), cal);
//        } catch (Exception e) {
//            throw Util.sqlException(Trace.INVALID_ESCAPE,
//                                          e.getMessage());
//        }
    }

    /**
     * <!-- start generic documentation -->
     * Registers the designated output parameter.  This version of
     * the method <code>registerOutParameter</code>
     * should be used for a user-defined or <code>REF</code> output parameter.
     * Examples of user-defined types include: <code>STRUCT</code>,
     * <code>DISTINCT</code>, <code>JAVA_OBJECT</code>, and named array types.
     * <p>
     * (JDBC4 claraification:) All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p> For a user-defined parameter, the fully-qualified SQL
     * type name of the parameter should also be given, while a
     * <code>REF</code> parameter requires that the fully-qualified type name
     * of the referenced type be given.  A JDBC driver that does not need the
     * type code and type name information may ignore it.   To be portable,
     * however, applications should always provide these values for
     * user-defined and <code>REF</code> parameters.
     *
     * Although it is intended for user-defined and <code>REF</code> parameters,
     * this method may be used to register a parameter of any JDBC type.
     * If the parameter does not have a user-defined or <code>REF</code> type,
     * the <i>typeName</i> parameter is ignored.
     *
     * <P><B>Note:</B> When reading the value of an out parameter, you
     * must use the getter method whose Java type corresponds to the
     * parameter's registered SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,...
     * @param sqlType a value from {@link java.sql.Types}
     * @param typeName the fully-qualified name of an SQL structured type
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     *
     */
    public void registerOutParameter(int parameterIndex, int sqlType,
                                     String typeName) throws SQLException {
        registerOutParameter(parameterIndex, sqlType);
    }

// ----------------------------------- JDBC 3 ----------------------------------

    /**
     * <!-- start generic documentation -->
     * Registers the OUT parameter named
     * <code>parameterName</code> to the JDBC type
     * <code>sqlType</code>.  All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * If the JDBC type expected to be returned to this output parameter
     * is specific to this particular database, <code>sqlType</code>
     * should be <code>java.sql.Types.OTHER</code>.  The method
     * {@link #getObject} retrieves the value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType the JDBC type code defined by <code>java.sql.Types</code>.
     * If the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>, the version of
     * <code>registerOutParameter</code> that accepts a scale value
     * should be used.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     * @see java.sql.Types
     */
//#ifdef JDBC3
    public void registerOutParameter(String parameterName,
                                     int sqlType) throws SQLException {
        registerOutParameter(findParameterIndex(parameterName), sqlType);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Registers the parameter named
     * <code>parameterName</code> to be of JDBC type
     * <code>sqlType</code>.  (JDBC4 clarification:) All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * This version of <code>registerOutParameter</code> should be
     * used when the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType SQL type code defined by <code>java.sql.Types</code>.
     * @param scale the desired number of digits to the right of the
     * decimal point.  It must be greater than or equal to zero.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     * @see java.sql.Types
     */
//#ifdef JDBC3
    public void registerOutParameter(String parameterName, int sqlType,
                                     int scale) throws SQLException {
        registerOutParameter(findParameterIndex(parameterName), sqlType);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Registers the designated output parameter.  This version of
     * the method <code>registerOutParameter</code>
     * should be used for a user-named or REF output parameter.  Examples
     * of user-named types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types. <p>
     *
     * (JDBC4 clarification:) All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * For a user-named parameter the fully-qualified SQL
     * type name of the parameter should also be given, while a REF
     * parameter requires that the fully-qualified type name of the
     * referenced type be given.  A JDBC driver that does not need the
     * type code and type name information may ignore it.   To be portable,
     * however, applications should always provide these values for
     * user-named and REF parameters.
     *
     * Although it is intended for user-named and REF parameters,
     * this method may be used to register a parameter of any JDBC type.
     * If the parameter does not have a user-named or REF type, the
     * typeName parameter is ignored.
     *
     * <P><B>Note:</B> When reading the value of an out parameter, you
     * must use the <code>getXXX</code> method whose Java type XXX corresponds
     * to the parameter's registered SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType a value from {@link java.sql.Types}
     * @param typeName the fully-qualified name of an SQL structured type
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
    public void registerOutParameter(String parameterName, int sqlType,
                                     String typeName) throws SQLException {
        registerOutParameter(findParameterIndex(parameterName), sqlType);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DATALINK</code>
     * parameter as a <code>java.net.URL</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,...
     * @return a <code>java.net.URL</code> object that represents the
     *   JDBC <code>DATALINK</code> value used as the designated
     *   parameter
     * @exception SQLException if a database access error occurs,
     *      or if the URL being returned is
     *      not a valid URL on the Java platform
     * @see #setURL
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public java.net.URL getURL(int parameterIndex) throws SQLException {
        throw Util.notSupported();
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.net.URL</code>
     * object.  The driver converts this to an SQL <code>DATALINK</code>
     * value when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param val the parameter value
     * @exception SQLException if a database access error occurs,
     *      or if a URL is malformed
     * @see #getURL
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setURL(String parameterName,
                       java.net.URL val) throws SQLException {
        setURL(findParameterIndex(parameterName), val);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to SQL <code>NULL</code>.
     *
     * <P><B>Note:</B> You must specify the parameter's SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setNull(String parameterName,
                        int sqlType) throws SQLException {
        setNull(findParameterIndex(parameterName), sqlType);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>boolean</code> value.
     * (JDBC4 clarification:) The driver converts this
     * to an SQL <code>BIT</code> or <code>BOOLEAN</code> value when it sends
     * it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getBoolean
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setBoolean(String parameterName,
                           boolean x) throws SQLException {
        setBoolean(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>byte</code> value.
     * The driver converts this to an SQL <code>TINYINT</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getByte
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setByte(String parameterName, byte x) throws SQLException {
        setByte(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>short</code> value.
     * The driver converts this to an SQL <code>SMALLINT</code> value when
     * it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getShort
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setShort(String parameterName, short x) throws SQLException {
        setShort(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>int</code> value.
     * The driver converts this to an SQL <code>INTEGER</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getInt
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setInt(String parameterName, int x) throws SQLException {
        setInt(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>long</code> value.
     * The driver converts this to an SQL <code>BIGINT</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getLong
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setLong(String parameterName, long x) throws SQLException {
        setLong(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>float</code> value.
     * The driver converts this to an SQL <code>FLOAT</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getFloat
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setFloat(String parameterName, float x) throws SQLException {
        setFloat(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>double</code> value.
     * The driver converts this to an SQL <code>DOUBLE</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getDouble
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setDouble(String parameterName,
                          double x) throws SQLException {
        setDouble(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.math.BigDecimal</code> value.
     * The driver converts this to an SQL <code>NUMERIC</code> value when
     * it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getBigDecimal
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setBigDecimal(String parameterName,
                              BigDecimal x) throws SQLException {
        setBigDecimal(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>String</code>
     * value. The driver converts this to an SQL <code>VARCHAR</code>
     * or <code>LONGVARCHAR</code> value (depending on the argument's
     * size relative to the driver's limits on <code>VARCHAR</code> values)
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getString
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setString(String parameterName,
                          String x) throws SQLException {
        setString(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java array of bytes.
     * The driver converts this to an SQL <code>VARBINARY</code> or
     * <code>LONGVARBINARY</code> (depending on the argument's size relative
     * to the driver's limits on <code>VARBINARY</code> values) when it sends
     * it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getBytes
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        setBytes(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * (JDBC4 clarification:) Sets the designated parameter to the given <code>java.sql.Date</code> value
     * using the default time zone of the virtual machine that is running
     * the application.  The driver converts this to an SQL <code>DATE</code> value
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getDate
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setDate(String parameterName, Date x) throws SQLException {
        setDate(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Time</code>
     * value.  The driver converts this to an SQL <code>TIME</code> value
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getTime
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setTime(String parameterName, Time x) throws SQLException {
        setTime(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.sql.Timestamp</code> value. The driver
     * converts this to an SQL <code>TIMESTAMP</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getTimestamp
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setTimestamp(String parameterName,
                             Timestamp x) throws SQLException {
        setTimestamp(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which will
     * have the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setAsciiStream(String parameterName, java.io.InputStream x,
                               int length) throws SQLException {
        setAsciiStream(findParameterIndex(parameterName), x, length);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which will
     * have the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from
     * the stream as needed until end-of-file is reached.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setBinaryStream(String parameterName, java.io.InputStream x,
                                int length) throws SQLException {
        setBinaryStream(findParameterIndex(parameterName), x, length);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * The second argument must be an object type; for integral values, the
     * <code>java.lang</code> equivalent objects should be used.
     *
     * <p>The given Java object will be converted to the given targetSqlType
     * before being sent to the database.
     *
     * If the object has a custom mapping (is of a class implementing the
     * interface <code>SQLData</code>),
     * the JDBC driver should call the method <code>SQLData.writeSQL</code>
     * to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,
     * <code>Struct</code>, or <code>Array</code>, the driver should pass it
     * to the database as a value of the corresponding SQL type.
     * <P>
     * Note that this method may be used to pass datatabase-
     * specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     * sent to the database. The scale argument may further qualify this type.
     * @param scale for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types,
     *    this is the number of digits after the decimal point.  For all
     *    other types, this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #getObject
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setObject(String parameterName, Object x, int targetSqlType,
                          int scale) throws SQLException {
        setObject(findParameterIndex(parameterName), x, targetSqlType, scale);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * This method is like the method <code>setObject</code>
     * above, except that it assumes a scale of zero. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     *                 sent to the database
     * @exception SQLException if a database access error occurs
     * @see #getObject
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setObject(String parameterName, Object x,
                          int targetSqlType) throws SQLException {
        setObject(findParameterIndex(parameterName), x, targetSqlType);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * The second parameter must be of type <code>Object</code>; therefore,
     * the <code>java.lang</code> equivalent objects should be used for
     * built-in types.
     *
     * <p>The JDBC specification specifies a standard mapping from
     * Java <code>Object</code> types to SQL types.  The given argument
     * will be converted to the corresponding SQL type before being
     * sent to the database.
     *
     * <p>Note that this method may be used to pass datatabase-
     * specific abstract data types, by using a driver-specific Java
     * type.
     *
     * If the object is of a class implementing the interface
     * <code>SQLData</code>, the JDBC driver should call the method
     * <code>SQLData.writeSQL</code> to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,
     * <code>Struct</code>, or <code>Array</code>, the driver should pass it
     * to the database as a value of the corresponding SQL type.
     * <P>
     * This method throws an exception if there is an ambiguity, for example,
     * if the object is of a class implementing more than one of the
     * interfaces named above. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the object containing the input parameter value
     * @exception SQLException if a database access error occurs or if the given
     *      <code>Object</code> parameter is ambiguous
     * @see #getObject
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setObject(String parameterName,
                          Object x) throws SQLException {
        setObject(findParameterIndex(parameterName), x);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>Reader</code>
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param reader the <code>java.io.Reader</code> object that
     *  contains the UNICODE data used as the designated parameter
     * @param length the number of characters in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setCharacterStream(String parameterName,
                                   java.io.Reader reader,
                                   int length) throws SQLException {
        setCharacterStream(findParameterIndex(parameterName), reader, length);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Date</code>
     * value, using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>DATE</code>
     * value, which the driver then sends to the database.  With a
     * a <code>Calendar</code> object, the driver can calculate the date
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the date
     * @exception SQLException if a database access error occurs
     * @see #getDate
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setDate(String parameterName, Date x,
                        Calendar cal) throws SQLException {
        setDate(findParameterIndex(parameterName), x, cal);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Time</code>
     * value, using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>TIME</code>
     * value, which the driver then sends to the database.  With a
     * a <code>Calendar</code> object, the driver can calculate the time
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the time
     * @exception SQLException if a database access error occurs
     * @see #getTime
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setTime(String parameterName, Time x,
                        Calendar cal) throws SQLException {
        setTime(findParameterIndex(parameterName), x, cal);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.sql.Timestamp</code> value, using the given
     * <code>Calendar</code> object.  The driver uses the
     * <code>Calendar</code> object to construct an SQL
     * <code>TIMESTAMP</code> value, which the driver then sends to the
     * database.  With a <code>Calendar</code> object, the driver can
     * calculate the timestamp taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the timestamp
     * @exception SQLException if a database access error occurs
     * @see #getTimestamp
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setTimestamp(String parameterName, Timestamp x,
                             Calendar cal) throws SQLException {
        setTimestamp(findParameterIndex(parameterName), x, cal);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to SQL <code>NULL</code>.
     * This version of the method <code>setNull</code> should
     * be used for user-defined types and <code>REF</code> type parameters.
     * Examples of user-defined types include: <code>STRUCT</code>,
     * <code>DISTINCT</code>, <code>JAVA_OBJECT</code>, and
     * named array types.
     *
     * <P><B>Note:</B> To be portable, applications must give the
     * SQL type code and the fully-qualified SQL type name when specifying
     * a <code>NULL</code> user-defined or <code>REF</code> parameter.
     * In the case of a user-defined type the name is the type name of the
     * parameter itself.  For a <code>REF</code> parameter, the name is the
     * type name of the referenced type.  If a JDBC driver does not need
     * the type code or type name information, it may ignore it.
     *
     * Although it is intended for user-defined and <code>Ref</code>
     * parameters, this method may be used to set a null parameter of
     * any JDBC type. If the parameter does not have a user-defined or
     * <code>REF</code> type, the given <code>typeName</code> is ignored. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * Starting with 1.7.2, HSLQDB supports this.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType a value from <code>java.sql.Types</code>
     * @param typeName the fully-qualified name of an SQL user-defined type;
     *  ignored if the parameter is not a user-defined type or
     *  SQL <code>REF</code> value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public void setNull(String parameterName, int sqlType,
                        String typeName) throws SQLException {
        setNull(findParameterIndex(parameterName), sqlType, typeName);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>CHAR</code>, <code>VARCHAR</code>,
     * or <code>LONGVARCHAR</code> parameter as a <code>String</code> in
     * the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>CHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the (JDBC4 clarification:) SQL
     * <code>CHAR</code> value had in the
     * database, including any padding added by the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value. If the value is SQL <code>NULL</code>,
     * the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setString
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public String getString(String parameterName) throws SQLException {
        return getString(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * (JDBC4 modified:) Retrieves the value of a JDBC <code>BIT</code> or <code>BOOLEAN</code>
     * parameter as a
     * <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>false</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBoolean
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public boolean getBoolean(String parameterName) throws SQLException {
        return getBoolean(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TINYINT</code> parameter as a
     * <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setByte
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public byte getByte(String parameterName) throws SQLException {
        return getByte(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>SMALLINT</code> parameter as
     * a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setShort
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public short getShort(String parameterName) throws SQLException {
        return getShort(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>INTEGER</code> parameter as
     * an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setInt
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public int getInt(String parameterName) throws SQLException {
        return getInt(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BIGINT</code> parameter as
     * a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setLong
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public long getLong(String parameterName) throws SQLException {
        return getLong(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>FLOAT</code> parameter as
     * a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setFloat
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public float getFloat(String parameterName) throws SQLException {
        return getFloat(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DOUBLE</code> parameter as
     * a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDouble
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public double getDouble(String parameterName) throws SQLException {
        return getDouble(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BINARY</code> or
     * <code>VARBINARY</code> parameter as an array of <code>byte</code>
     * values in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBytes
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public byte[] getBytes(String parameterName) throws SQLException {
        return getBytes(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DATE</code> parameter as a
     * <code>java.sql.Date</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Date getDate(String parameterName) throws SQLException {
        return getDate(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Time getTime(String parameterName) throws SQLException {
        return getTime(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return getTimestamp(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a parameter as an <code>Object</code> in the Java
     * programming language. If the value is an SQL <code>NULL</code>, the
     * driver returns a Java <code>null</code>.
     * <p>
     * This method returns a Java object whose type corresponds to the JDBC
     * type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target JDBC
     * type as <code>java.sql.Types.OTHER</code>, this method can be used
     * to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return A <code>java.lang.Object</code> holding the OUT parameter value.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Object getObject(String parameterName) throws SQLException {
        return getObject(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>NUMERIC</code> parameter as a
     * <code>java.math.BigDecimal</code> object with as many digits to the
     * right of the decimal point as the value contains. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value in full precision.  If the value is
     * SQL <code>NULL</code>, the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBigDecimal
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public BigDecimal getBigDecimal(String parameterName)
    throws SQLException {
        return getBigDecimal(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Returns an object representing the value of OUT parameter
     * <code>parameterName</code> and uses <code>map</code> for the custom
     * mapping of the parameter value.
     * <p>
     * This method returns a Java object whose type corresponds to the
     * JDBC type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target
     * JDBC type as <code>java.sql.Types.OTHER</code>, this method can
     * be used to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param map the mapping from SQL type names to Java classes
     * @return a <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @see #setObject
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Object getObject(String parameterName,
                            Map map) throws SQLException {
        return getObject(findParameterIndex(parameterName), map);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>REF(&lt;structured-type&gt;)</code>
     * parameter as a {@link Ref} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>Ref</code> object in the
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Ref getRef(String parameterName) throws SQLException {
        return getRef(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BLOB</code> parameter as a
     * {@link java.sql.Blob} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>Blob</code> object in the
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Blob getBlob(String parameterName) throws SQLException {
        return getBlob(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>CLOB</code> parameter as a
     * {@link java.sql.Clob} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>Clob</code> object in the
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Clob getClob(String parameterName) throws SQLException {
        return getClob(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>ARRAY</code> parameter as an
     * {@link Array} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as an <code>Array</code> object in
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Array getArray(String parameterName) throws SQLException {
        return getArray(findParameterIndex(parameterName));
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DATE</code> parameter as a
     * <code>java.sql.Date</code> object, using
     * the given <code>Calendar</code> object
     * to construct the date.
     * With a <code>Calendar</code> object, the driver
     * can calculate the date taking into account a custom timezone and
     * locale.  If no <code>Calendar</code> object is specified, the d
     * river uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the date
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Date getDate(String parameterName,
                        Calendar cal) throws SQLException {
        return getDate(findParameterIndex(parameterName), cal);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code> object, using
     * the given <code>Calendar</code> object
     * to construct the time.
     * With a <code>Calendar</code> object, the driver
     * can calculate the time taking into account a custom timezone and
     * locale. If no <code>Calendar</code> object is specified, the driver
     * uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the time
     * @return the parameter value; if the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Time getTime(String parameterName,
                        Calendar cal) throws SQLException {
        return getTime(findParameterIndex(parameterName), cal);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object, using
     * the given <code>Calendar</code> object to construct
     * the <code>Timestamp</code> object.
     * With a <code>Calendar</code> object, the driver
     * can calculate the timestamp taking into account a custom timezone
     * and locale.  If no <code>Calendar</code> object is specified, the
     * driver uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     *
     * @param parameterName the name of the parameter
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the timestamp
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public Timestamp getTimestamp(String parameterName,
                                  Calendar cal) throws SQLException {
        return getTimestamp(findParameterIndex(parameterName), cal);
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DATALINK</code> parameter as a
     * <code>java.net.URL</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>java.net.URL</code> object in the
     *      Java programming language.  If the value was SQL
     *      <code>NULL</code>, the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs,
     *      or if there is a problem with the URL
     * @see #setURL
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
    public java.net.URL getURL(String parameterName) throws SQLException {
        return getURL(findParameterIndex(parameterName));
    }

//#endif JDBC3
}
