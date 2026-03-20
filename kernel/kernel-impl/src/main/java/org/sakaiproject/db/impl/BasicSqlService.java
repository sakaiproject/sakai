/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.db.impl;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlServiceDeadlockException;
import org.sakaiproject.db.api.SqlServiceUniqueViolationException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * BasicSqlService implements the SqlService.
 * </p>
 */
@Slf4j
public class BasicSqlService implements SqlService {
    /** Key name in thread local to find the current transaction connection. */
    protected static final String TRANSACTION_CONNECTION = "sqlService:transaction_connection";
    protected DataSource defaultDataSource; // The "shared", "common" database connection pool
    protected boolean commitAfterRead = false; // Should we do a commit after a single statement read
    protected boolean showSql = false; // if true, debug each sql command with timing
    protected int deadlockRetries = 5; // Configuration: number of on-deadlock retries for save
    protected boolean autoDdl = false; // Configuration: to run the ddl on init or not
    protected SqlServiceSql sqlServiceSql; // The db handler we are using
    @Getter protected String vendor = "hsqldb"; // Database vendor used; possible values are oracle, mysql, hsqldb (default)
    @Setter protected Map<String, SqlServiceSql> databaseBeans; // contains a map of the database dependent handlers
    @Setter protected ThreadLocalManager threadLocalManager;

    /**
     * Configuration: should we do a commit after each single SQL read?
     *
     * @param value
     *        the setting (true of false) string.
     */
    public void setCommitAfterRead(String value) {
        log.debug("setCommitAfterRead(String {})", value);
        commitAfterRead = Boolean.parseBoolean(value);
    }

    /**
     * Configuration: Database vendor used; possible values are oracle, mysql, hsqldb.
     *
     * @param value
     *        the Database vendor used.
     */
    public void setVendor(String value) {
        log.debug("setVendor(String {})", value);
        vendor = (value != null) ? value.toLowerCase().trim() : null;
    }

    /**
     * Configuration: to show each sql command in the logs or not.
     *
     * @param value
     *        the showSql setting.
     */
    public void setShowSql(String value) {
        log.debug("setShowSql(String {})", value);
        showSql = Boolean.parseBoolean(value);
    }

    /**
     * Configuration: number of on-deadlock retries for save.
     *
     * @param value
     *        the number of on-deadlock retries for save.
     */
    public void setDeadlockRetries(String value) {
        deadlockRetries = Integer.parseInt(value);
    }

    /**
     * Configuration: to run the ddl on init or not.
     *
     * @param value
     *        the auto ddl value.
     */
    public void setAutoDdl(String value) {
        log.debug("setAutoDdl(String {})", value);
        autoDdl = Boolean.parseBoolean(value);
    }

    /**
     * sets which bean containing database dependent code should be used depending on the database vendor.
     */
    public void setSqlServiceSql(String vendor) {
        this.sqlServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
    }

    public void init() {
        setSqlServiceSql(getVendor());

        // if we are auto-creating our schema, check and create
        if (autoDdl) ddl(getClass().getClassLoader(), "sakai_locks");
        log.info("init(): vendor: {} autoDDL: {} deadlockRetries: {}", vendor, autoDdl, deadlockRetries);
    }

    public void destroy() {
        log.info("destroy()");
    }

    @Override
    public Connection borrowConnection() throws SQLException {
        log.debug("borrowConnection()");

        if (defaultDataSource != null) {
            return defaultDataSource.getConnection();
        } else {
            throw new SQLException("no default pool.");
        }
    }

    @Override
    public void returnConnection(Connection conn) {
        log.debug("returnConnection(Connection {})", conn);

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new AssertionError(e);
            }
        }
    }

    @Override
    public boolean transact(Runnable callback, String tag) {
        // if we are already in a transaction, stay in it (don't start a new one), and just run the callback (no retries, let the outside transaction
        // code handle that)
        if (threadLocalManager.get(TRANSACTION_CONNECTION) != null) {
            callback.run();
            return true;
        }

        // in case of deadlock we might retry
        for (int i = 0; i <= deadlockRetries; i++) {
            if (i > 0) {
                // make a little fuss
                log.warn("transact: deadlock: retrying ({} / {}): {}", i, deadlockRetries, tag);

                // do a little wait, longer for each retry
                // TODO: randomize?
                try {
                    Thread.sleep(i * 100L);
                } catch (Exception ignore) {
                }
            }

            Connection connection = null;
            boolean wasCommit = true;
            try {
                connection = borrowConnection();
                wasCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);

                // store the connection in the thread
                threadLocalManager.set(TRANSACTION_CONNECTION, connection);

                callback.run();

                connection.commit();

                return true;
            } catch (SqlServiceDeadlockException e) {
                // rollback
                if (connection != null) {
                    try {
                        connection.rollback();
                        log.warn("transact: deadlock: rolling back: {}", tag);
                    } catch (Exception ee) {
                        log.warn("transact: (deadlock: rollback): {} : {}", tag, ee.toString());
                    }
                }

                // if this was the last attempt, throw to abort
                if (i == deadlockRetries) {
                    log.warn("transact: deadlock: retry failure: {}", tag);
                    throw e;
                }
            } catch (RuntimeException e) {
                // rollback
                if (connection != null) {
                    try {
                        connection.rollback();
                        log.warn("transact: rolling back: {}", tag);
                    } catch (Exception ee) {
                        log.warn("transact: (rollback): {} : {}", tag, ee.toString());
                    }
                }
                log.warn("transact: failure: {}", e.toString());
                throw e;
            } catch (SQLException e) {
                // rollback
                if (connection != null) {
                    try {
                        connection.rollback();
                        log.warn("transact: rolling back: {}", tag);
                    } catch (Exception ee) {
                        log.warn("transact: (rollback): {} : {}", tag, ee.toString());
                    }
                }
                log.warn("transact: failure: {}", e);
                throw new RuntimeException("SqlService.transact failure", e);
            } finally {
                if (connection != null) {
                    // clear the connection from the thread
                    threadLocalManager.set(TRANSACTION_CONNECTION, null);

                    try {
                        connection.setAutoCommit(wasCommit);
                    } catch (Exception e) {
                        log.warn("transact: (setAutoCommit): {} : {}", tag, e.toString());
                    }
                    returnConnection(connection);
                }
            }
        }

        return false;
    }

    @Override
    public List<String> dbRead(String sql) {
        log.debug("dbRead(String {})", sql);
        return dbRead(sql, null, null);
    }

    @Override
    public <T> List<T> dbRead(String sql, Object[] fields, SqlReader<T> reader) {
        log.debug("dbRead(String {}, Object[] {}, SqlReader {})", sql, fields, reader);
        return dbRead(null, sql, fields, reader);
    }

    @Override
    public <T> List<T> dbRead(Connection callerConn, String sql, Object[] fields, SqlReader<T> reader) {
        // check for a transaction conncetion
        if (callerConn == null) {
            callerConn = (Connection) threadLocalManager.get(TRANSACTION_CONNECTION);
        }

        log.debug("dbRead(Connection {}, String {}, Object[] {}, SqlReader {})", callerConn, sql, fields, reader);

        // for DEBUG
        long start = 0;
        long connectionTime = 0;
        long stmtTime = 0;
        long resultsTime = 0;
        int count = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet result = null;
        List rv = new ArrayList<>(); // can return types <String> or <T> :(

        try {
            if (showSql) {
                start = System.currentTimeMillis();
            }

            // borrow a new connection if we are not provided with one to use
            if (callerConn != null) {
                conn = callerConn;
            } else {
                conn = borrowConnection();
                threadLocalManager.set(TRANSACTION_CONNECTION, conn);
            }
            if (showSql) {
                connectionTime = System.currentTimeMillis() - start;
            }
            if (showSql) {
                start = System.currentTimeMillis();
            }
            pstmt = conn.prepareStatement(sql);

            // put in all the fields
            prepareStatement(pstmt, fields);

            result = pstmt.executeQuery();

            if (showSql) {
                stmtTime = System.currentTimeMillis() - start;
            }
            if (showSql) {
                start = System.currentTimeMillis();
            }

            while (result.next()) {
                if (showSql) {
                    count++;
                }

                // without a reader, we read the first String from each record
                if (reader == null) {
                    String s;
                    ResultSetMetaData metadataResult = result.getMetaData();

                    if (metadataResult != null && Types.CLOB == metadataResult.getColumnType(1)) {
                        Clob clobResult = result.getClob(1);
                        s = clobResult.getSubString(1, (int) clobResult.length());
                    } else {
                        s = result.getString(1);
                    }
                    if (s != null) {
                        rv.add(s);
                    }
                } else {
                    try {
                        T obj = reader.readSqlResultRecord(result);
                        if (obj != null) {
                            rv.add(obj);
                        }
                    } catch (SqlReaderFinishedException e) {
                        break;
                    }
                }
            }
        } catch (SQLException | UnsupportedEncodingException e) {
            log.warn("Sql.dbRead: sql: {} {}", sql, fields, e);
        } finally {
            if (showSql) {
                resultsTime = System.currentTimeMillis() - start;
            }
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbRead: sql: {} {}", sql, fields, e);
                }
            }
            if (null != pstmt) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbRead: sql: {} {}", sql, fields, e);
                }
            }

            // return the connection only if we have borrowed a new one for this call
            if (callerConn == null) {
                if (null != conn) {
                    // if we commit on read
                    if (commitAfterRead) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            log.warn("Sql.dbRead: sql: {} {}", sql, fields, e);
                        }
                    }
                    threadLocalManager.set(TRANSACTION_CONNECTION, null);
                    returnConnection(conn);
                }
            }
        }

        if (showSql) log.info("Sql.dbRead: time: {} / {} / {} #: {} binds: {} sql: {}",
                connectionTime, stmtTime, resultsTime, count, fields, sql);

        return rv;
    }

    @Override
    public void dbReadBinary(String sql, Object[] fields, byte[] value) {
        log.debug("dbReadBinary(String {}, Object[] {})", sql, fields);
        dbReadBinary(null, sql, fields, value);
    }

    @Override
    public void dbReadBinary(Connection callerConn, String sql, Object[] fields, byte[] value) {
        // check for a transaction conncetion
        if (callerConn == null) {
            callerConn = (Connection) threadLocalManager.get(TRANSACTION_CONNECTION);
        }

        log.debug("dbReadBinary(Connection {}, String {}, Object[] {})", callerConn, sql, fields);

        // for DEBUG
        long start = 0;
        long connectionTime = 0;
        int lenRead = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet result = null;

        try {
            if (showSql) start = System.currentTimeMillis();
            if (callerConn != null) {
                conn = callerConn;
            } else {
                conn = borrowConnection();
            }
            if (showSql) connectionTime = System.currentTimeMillis() - start;
            if (showSql) start = System.currentTimeMillis();

            pstmt = conn.prepareStatement(sql);

            // put in all the fields
            prepareStatement(pstmt, fields);

            result = pstmt.executeQuery();

            int index = 0;
            while (result.next() && (index < value.length)) {
                InputStream stream = result.getBinaryStream(1);
                int len = stream.read(value, index, value.length - index);
                stream.close();
                index += len;
                if (showSql) lenRead += len;
            }
        } catch (Exception e) {
            log.warn("Sql.dbReadBinary(): {}", e.toString());
        } finally {
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): result close fail: {}", e.toString());
                }
            }
            if (null != pstmt) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): pstmt close fail: {}", e.toString());
                }
            }
            // return the connection only if we have borrowed a new one for this call
            if (callerConn == null) {
                if (null != conn) {
                    // if we commit on read
                    if (commitAfterRead) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            log.warn("Sql.dbReadBinary(): conn commit fail: {}", e.toString());
                        }
                    }
                    returnConnection(conn);
                }
            }
        }

        if (showSql) {
            log.info("sql read binary: len: {} time: {} / {} binds: {} sql: {}",
                    lenRead, connectionTime, System.currentTimeMillis() - start, sql, fields);
        }
    }

    @Override
    public InputStream dbReadBinary(String sql, Object[] fields, boolean big) throws ServerOverloadException {
        // Note: does not support TRANSACTION_CONNECTION -ggolden
        log.debug("dbReadBinary(String {}, Object[] {}, boolean {})", sql, fields, big);

        InputStream rv = null;

        // for DEBUG
        long start = 0;
        long connectionTime = 0;
        int lenRead = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet result = null;

        try {
            if (showSql) {
                start = System.currentTimeMillis();
            }
            if (!big) {
                conn = borrowConnection();
            } else {
                // get a connection if it's available, else throw
                conn = borrowConnection();
                if (conn == null) {
                    throw new ServerOverloadException(null);
                }
            }
            if (showSql) {
                connectionTime = System.currentTimeMillis() - start;
            }
            if (showSql) {
                start = System.currentTimeMillis();
            }
            pstmt = conn.prepareStatement(sql);
            // put in all the fields
            prepareStatement(pstmt, fields);
            result = pstmt.executeQuery();

            if (result.next()) {
                InputStream stream = result.getBinaryStream(1);
                rv = new StreamWithConnection(stream, result, pstmt, conn);
            }
        } catch (SQLException | UnsupportedEncodingException e) {
            log.warn("Sql.dbReadBinary(): {}", e.toString());
        } finally {
            // ONLY if we didn't make the rv - else let the rv hold these OPEN!
            if (rv == null) {
                if (null != result) {
                    try {
                        result.close();
                    } catch (SQLException e) {
                        log.warn("Sql.dbReadBinary(): {}", e.toString());
                    }
                }
                if (null != pstmt) {
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                        log.warn("Sql.dbReadBinary(): {}", e.toString());
                    }
                }
                if (null != conn) {
                    // if we commit on read
                    if (commitAfterRead) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            log.warn("Sql.dbReadBinary(): {}", e.toString());
                        }
                    }
                    returnConnection(conn);
                }
            }
        }

        if (showSql) log.info("sql read binary: len: {} time: {} / {} binds: {} sql: {}", 
                lenRead, connectionTime, System.currentTimeMillis() - start, sql, fields);
        
        return rv;
    }

    @Override
    public boolean dbWrite(String sql) {
        log.debug("dbWrite(String {})", sql);
        return dbWrite(sql, null, null, null, false);
    }

    @Override
    public boolean dbWrite(String sql, String var) {
        log.debug("dbWrite(String {}, String {})", sql, var);
        return dbWrite(sql, null, var, null, false);
    }

    @Override
    public boolean dbWriteBinary(String sql, Object[] fields, byte[] var, int offset, int len) {
        // Note: does not support TRANSACTION_CONNECTION -ggolden
        log.debug("dbWriteBinary(String {}, Object[] {}, byte[] size {}, int {}, int {})", sql, fields, var.length, offset, len);

        // for DEBUG
        long start = 0;
        long connectionTime = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean autoCommit = false;
        boolean resetAutoCommit = false;

        // stream from the var
        InputStream varStream = new ByteArrayInputStream(var, offset, len);

        boolean success = false;

        try {
            if (showSql) start = System.currentTimeMillis();
            conn = borrowConnection();
            if (showSql) connectionTime = System.currentTimeMillis() - start;

            // make sure we do not have auto commit - will change and reset if needed
            autoCommit = conn.getAutoCommit();
            if (autoCommit) {
                conn.setAutoCommit(false);
                resetAutoCommit = true;
            }

            if (showSql) start = System.currentTimeMillis();
            pstmt = conn.prepareStatement(sql);

            // put in all the fields
            int pos = prepareStatement(pstmt, fields);

            // last, put in the binary
            pstmt.setBinaryStream(pos, varStream, len);

            // int result =
            pstmt.executeUpdate();

            // commit and indicate success
            conn.commit();
            success = true;
        } catch (SQLException e) {
            // On mysql unless you allow serverside prepared statements then the maximum size possible is configured
            // by max_allowed_packet. The error codes below are:
            // 1105 max_allowed_packet too small
            // 1118 redo log size not at least 10 times max_allowed_packet
            if ("mysql".equals(vendor) && (e.getErrorCode() == 1105 || e.getErrorCode() == 1118)) {
                log.warn("SQL '{}' failed, consider useServerPrepStmts=true on JDBC connection.", sql, e);
            }
            // this is likely due to a key constraint problem...
            return false;
        } catch (Exception e) {
            log.warn("Sql.dbWriteBinary(): {}", e.toString());
            return false;
        } finally {
            if (null != pstmt) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbWriteBinary(): {}", e.toString());
                }
            }
            try {
                varStream.close();
            } catch (IOException e) {
                log.warn("Sql.dbWriteBinary(): {}", e.toString());
            }

            if (null != conn) {
                // rollback on failure
                if (!success) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        log.warn("Sql.dbWriteBinary(): {}", e.toString());
                    }
                }

                // if we changed the auto commit, reset here
                if (resetAutoCommit) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException e) {
                        log.warn("Sql.dbWriteBinary(): {}", e.toString());
                    }
                }
                returnConnection(conn);
            }

        }

        if (showSql) log.info("sql write binary: len: {} time: {} / {} binds: {} sql: {}",
                len, connectionTime, System.currentTimeMillis() - start, sql, fields);


        return true;
    }

    @Override
    public boolean dbWrite(String sql, Object[] fields) {
        log.debug("dbWrite(String {}, Object[] {})", sql, fields);
        return dbWrite(sql, fields, null, null, false);
    }

    @Override
    public boolean dbWrite(Connection connection, String sql, Object[] fields) {
        log.debug("dbWrite(Connection {}, String {}, Object[] {})", connection, sql, fields);
        return dbWrite(sql, fields, null, connection, false);
    }

    @Override
    public boolean dbWriteFailQuiet(Connection connection, String sql, Object[] fields) {
        log.debug("dbWriteFailQuiet(Connection {}, String {}, Object[] {})", connection, sql, fields);
        return dbWrite(sql, fields, null, connection, true);
    }

    @Override
    public boolean dbWrite(String sql, Object[] fields, String lastField) {
        log.debug("dbWrite(String {}, Object[] {}, String {})", sql, fields, lastField);
        return dbWrite(sql, fields, lastField, null, false);
    }

    /**
     * @see org.sakaiproject.db.api.SqlService#dbWriteCount(String, Object[], String, Connection, boolean)
     */
    protected boolean dbWrite(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet) {
        return (dbWriteCount(sql, fields, lastField, callerConnection, failQuiet) >= 0);
    }

    /**
     * @see org.sakaiproject.db.api.SqlService#dbWriteCount(String, Object[], String, Connection, int)
     */
    protected boolean dbWrite(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet) {
        return (dbWriteCount(sql, fields, lastField, callerConnection, failQuiet) >= 0);
    }

    @Override
    public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet) {
        return dbWriteCount(sql, fields, lastField, callerConnection, failQuiet ? 1 : 0);
    }

    @Override
    public boolean dbWriteBatch(Connection callerConnection, String sql, List<Object[]> fieldsList) {
        boolean success = false;

        try (PreparedStatement pstmt = callerConnection.prepareStatement(sql)) {
            try {
                for (Object[] fields : fieldsList) {
                    prepareStatement(pstmt, fields);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                success = true;
            } catch (UnsupportedEncodingException e) {
                log.warn("Sql.dbWriteBatch()", e);
            } catch (SQLException e) {
                log.warn("Sql.dbWriteBatch(): error code: {} sql: {} {}", e.getErrorCode(), sql, e);
            }
        } catch (Exception e) {
            log.warn("Sql.dbWriteBatch(): {}", e.toString());
            throw new RuntimeException("SqlService.dbWriteBatch failure", e);
        }

        return success;
    }

    @Override
    public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet) {
        int retval = -1;
        // check for a transaction connection
        if (callerConnection == null) {
            callerConnection = (Connection) threadLocalManager.get(TRANSACTION_CONNECTION);
        }

        log.debug("dbWrite(String {}, Object[] {}, String {}, Connection {}, boolean {})", sql, fields, lastField, callerConnection, failQuiet);
        // for DEBUG
        long start = 0;
        long connectionTime = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean autoCommit = false;
        boolean resetAutoCommit = false;

        boolean success = false;

        try {
            if (callerConnection != null) {
                conn = callerConnection;
            } else {
                if (showSql) start = System.currentTimeMillis();
                conn = borrowConnection();
                if (showSql) connectionTime = System.currentTimeMillis() - start;

                // make sure we have do not have auto commit - will change and reset if needed
                autoCommit = conn.getAutoCommit();
                if (autoCommit) {
                    conn.setAutoCommit(false);
                    resetAutoCommit = true;
                }
            }

            if (showSql) start = System.currentTimeMillis();
            pstmt = conn.prepareStatement(sql);

            // put in all the fields
            int pos = prepareStatement(pstmt, fields);

            // last, put in the string value
            if (lastField != null) {
                sqlServiceSql.setBytes(pstmt, lastField, pos);
            }

            retval = pstmt.executeUpdate();

            // commit unless we are in a transaction (provided with a connection)
            if (callerConnection == null) {
                conn.commit();
            }

            // indicate success
            success = true;
        } catch (SQLException e) {
            // is this due to a key constraint problem?... check each vendor's error codes
            boolean recordAlreadyExists = sqlServiceSql.getRecordAlreadyExists(e);

            if (showSql) {
                log.warn("Sql.dbWrite(): error code: {} sql: {} binds: {} {}", e.getErrorCode(), sql, fields, e);
            }

            // if asked to fail quietly, just return -1 if we find this error.
            if (recordAlreadyExists || failQuiet != 0) {
                // If failQuiet is 1 then print this, otherwise it's in ddl mode so just ignore
                if (failQuiet == 1) {
                    log.warn("Sql.dbWrite(): recordAlreadyExists: {}, failQuiet: {}, error code: {}, sql: {}, binds: {}, error: {}",
                            recordAlreadyExists, failQuiet, e.getErrorCode(), sql, fields, e.toString());
                }
                return -1;
            }

            // perhaps due to a mysql deadlock?
            if (sqlServiceSql.isDeadLockError(e.getErrorCode())) {
                // just a little fuss
                log.warn("Sql.dbWrite(): deadlock: error code: {}, sql: {}, binds: {}, error: {}", e.getErrorCode(), sql, fields, e.toString());
                throw new SqlServiceDeadlockException(e);
            } else if (recordAlreadyExists) {
                // just a little fuss
                log.warn("Sql.dbWrite(): unique violation: error code: {}, sql: {}, binds: {}, error: {}", e.getErrorCode(), sql, fields, e.toString());
                throw new SqlServiceUniqueViolationException(e);
            } else {
                // something ELSE went wrong, so lest make a fuss
                log.warn("Sql.dbWrite(): error code: {} sql: {} binds: {} ", e.getErrorCode(), sql, fields, e);
                throw new RuntimeException("SqlService.dbWrite failure", e);
            }
        } catch (Exception e) {
            log.warn("Sql.dbWrite(): {}", e.toString());
            throw new RuntimeException("SqlService.dbWrite failure", e);
        } finally {
            try {
                if (null != pstmt) pstmt.close();
                if ((null != conn) && (callerConnection == null)) {
                    // rollback on failure
                    if (!success) {
                        conn.rollback();
                    }

                    // if we changed the auto commit, reset here
                    if (resetAutoCommit) {
                        conn.setAutoCommit(autoCommit);
                    }
                    returnConnection(conn);
                }
            } catch (Exception e) {
                log.warn("Sql.dbWrite(): {}", e.toString());
                throw new RuntimeException("SqlService.dbWrite failure", e);
            }
        }

        if (showSql) log.info("Sql.dbWrite(): len: {} time: {} / {} binds: {} sql: {}",
                lastField != null ? lastField.length() : "null", connectionTime, System.currentTimeMillis() - start, sql, fields);

        return retval;
    }

    @Override
    public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn) {
        return dbInsert(callerConnection, sql, fields, autoColumn, null, 0);
    }

    @Override
    public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn, InputStream last, int lastLength) {
        // check for a transaction connection
        if (callerConnection == null) {
            callerConnection = (Connection) threadLocalManager.get(TRANSACTION_CONNECTION);
        }

        log.debug("dbInsert(String {}, Object[] {}, Connection {})", sql, fields, callerConnection);

        // for DEBUG
        long start = 0;
        long connectionTime = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean autoCommit = false;
        boolean resetAutoCommit = false;
        int result = -1;

        boolean success = false;
        Long rv = null;

        try {
            if (callerConnection != null) {
                conn = callerConnection;
            } else {
                if (showSql) start = System.currentTimeMillis();
                conn = borrowConnection();
                if (showSql) connectionTime = System.currentTimeMillis() - start;

                // make sure we have do not have auto commit - will change and reset if needed
                autoCommit = conn.getAutoCommit();
                if (autoCommit) {
                    conn.setAutoCommit(false);
                    resetAutoCommit = true;
                }
            }

            if (showSql) start = System.currentTimeMillis();

            pstmt = sqlServiceSql.prepareAutoColumn(conn, sql, autoColumn);

            // put in all the fields
            int pos = prepareStatement(pstmt, fields);

            // and the last one
            if (last != null) {
                pstmt.setBinaryStream(pos, last, lastLength);
            }

            result = pstmt.executeUpdate();

            rv = sqlServiceSql.getGeneratedKey(pstmt, sql);

            // commit unless we are in a transaction (provided with a connection)
            if (callerConnection == null) {
                conn.commit();
            }

            // indicate success
            success = true;
        } catch (SQLException e) {
            // is this due to a key constraint problem... check each vendor's error codes
            boolean recordAlreadyExists = sqlServiceSql.getRecordAlreadyExists(e);

            if (showSql) {
                log.warn("Sql.dbInsert(): error code: {} sql: {} binds: {} {}", e.getErrorCode(), sql, fields, e);
            }

            if (recordAlreadyExists) return null;

            // perhaps due to a mysql deadlock?
            if (("mysql".equals(vendor)) && (e.getErrorCode() == 1213)) {
                // just a little fuss
                log.warn("Sql.dbInsert(): deadlock: error code: {} sql: {} binds: {} {}", e.getErrorCode(), sql, fields, e.toString());
                throw new SqlServiceDeadlockException(e);
            } else if (recordAlreadyExists) {
                // just a little fuss
                log.warn("Sql.dbInsert(): unique violation: error code: {}, sql: {}, binds: {}, error: {}", e.getErrorCode(), sql, fields, e.toString());
                throw new SqlServiceUniqueViolationException(e);
            } else {
                // something ELSE went wrong, so lest make a fuss
                log.warn("Sql.dbInsert(): error code: {} sql: {} binds: {} ", e.getErrorCode(), sql, fields, e);
                throw new RuntimeException("SqlService.dbInsert failure", e);
            }
        } catch (Exception e) {
            log.warn("Sql.dbInsert(): {}", e.toString());
            throw new RuntimeException("SqlService.dbInsert failure", e);
        } finally {
            try {
                if (null != pstmt) pstmt.close();
                if ((null != conn) && (callerConnection == null)) {
                    // rollback on failure
                    if (!success) {
                        conn.rollback();
                    }

                    // if we changed the auto commit, reset here
                    if (resetAutoCommit) {
                        conn.setAutoCommit(autoCommit);
                    }
                    returnConnection(conn);
                }
            } catch (Exception e) {
                log.warn("Sql.dbInsert(): {}", e.toString());
                throw new RuntimeException("SqlService.dbInsert failure", e);
            }
        }

        if (showSql) log.info("Sql.dbWrite(): rows: {} time: {} / {} binds: {} sql: {}",
                result, connectionTime, System.currentTimeMillis() - start, sql, fields);

        return rv;
    }

    @Override
    public void dbReadBlobAndUpdate(String sql, byte[] content) {
        // Note: does not support TRANSACTION_CONNECTION -ggolden

        log.debug("dbReadBlobAndUpdate(String {}, byte[] {})", sql, content);

        if (!sqlServiceSql.canReadAndUpdateBlob()) {
            throw new UnsupportedOperationException("BasicSqlService.dbReadBlobAndUpdate() is not supported by the " + getVendor() + " database.");
        }

        // for DEBUG
        long start = 0;
        long connectionTime = 0;
        int lenRead = 0;

        log.debug("Sql.dbReadBlobAndUpdate(): {}", sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        Object blob = null;
        OutputStream os = null;

        try {
            if (showSql) start = System.currentTimeMillis();
            conn = borrowConnection();
            if (showSql) connectionTime = System.currentTimeMillis() - start;
            if (showSql) start = System.currentTimeMillis();
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);
            if (result.next()) {
                blob = result.getBlob(1);
            }
            if (blob != null) {
                // %%% not supported? b.truncate(0);
                // int len = b.setBytes(0, content);
                try {
                    // Use reflection to remove compile time dependency on oracle driver
                    Class[] paramsClasses = new Class[0];
                    Method getBinaryOutputStreamMethod = blob.getClass().getMethod("getBinaryOutputStream", paramsClasses);
                    Object[] params = new Object[0];
                    os = (OutputStream) getBinaryOutputStreamMethod.invoke(blob, params);
                    os.write(content);

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
                    log.warn("Oracle driver error: {}", e.toString());
                }
            }
        } catch (SQLException e) {
            log.warn("Sql.dbReadBlobAndUpdate(): {}", e.toString());
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.warn("Sql.dbRead(): {}", e.toString());
                }
            }
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbRead(): {}", e.toString());
                }
            }
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbRead(): {}", e.toString());
                }
            }
            if (null != conn) {
                // if we commit on read
                if (commitAfterRead) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        log.warn("Sql.dbRead(): {}", e.toString());
                    }
                }

                returnConnection(conn);
            }

        }

        if (showSql) log.info("sql dbReadBlobAndUpdate: len: {} time: {} / {} sql: {}",
                lenRead, connectionTime, System.currentTimeMillis() - start, sql);
    }

    @Override
    public Connection dbReadLock(String sql, StringBuilder field) {
        // Note: does not support TRANSACTION_CONNECTION -ggolden

        log.debug("dbReadLock(String {}, StringBuilder {})", sql, field);

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        boolean autoCommit = false;
        boolean resetAutoCommit = false;
        boolean closeConn = false;

        try {
            // get a new connection
            conn = borrowConnection();

            // adjust to turn off auto commit - we need a transaction
            autoCommit = conn.getAutoCommit();
            if (autoCommit) {
                conn.setAutoCommit(false);
                resetAutoCommit = true;
            }

            log.debug("Sql.dbReadLock(): {}", sql);

            // create a statement and execute
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);

            // if we have a result record
            if (result.next()) {
                // get the result and pack into the return buffer
                String rv = result.getString(1);
                if ((field != null) && (rv != null)) field.append(rv);
            }

            // otherwise we fail
            else {
                closeConn = true;
            }
        }

        // this is likely the error when the record is otherwise locked - we fail
        catch (SQLException e) {
            // Note: ORA-00054 gives an e.getErrorCode() of 54, if anyone cares...
            log.warn("Sql.dbUpdateLock(): {} - {}", e.getErrorCode(), e);
            closeConn = true;
        } finally {
            // close the result and statement
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): {}", e.toString());
                }
            }
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): {}", e.toString());
                }
            }

            // if we are failing, restore and release the connection
            if ((closeConn) && (conn != null)) {
                // just in case we got a lock
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): {}", e.toString());
                }
                if (resetAutoCommit)
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException e) {
                        log.warn("Sql.dbReadBinary(): {}", e.toString());
                    }

            }

            if (conn != null) {
                returnConnection(conn);
            }
        }

        return conn;
    }

    @Override
    public <T> Connection dbReadLock(String sql, SqlReader<T> reader) {
        // Note: does not support TRANSACTION_CONNECTION -ggolden

        log.debug("dbReadLock(String {})", sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        boolean autoCommit = false;
        boolean resetAutoCommit = false;
        boolean closeConn = false;

        try {
            // get a new conncetion
            conn = borrowConnection();

            // adjust to turn off auto commit - we need a transaction
            autoCommit = conn.getAutoCommit();
            if (autoCommit) {
                conn.setAutoCommit(false);
                resetAutoCommit = true;
            }

            log.debug("Sql.dbReadLock(): {}", sql);

            // create a statement and execute
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);

            // if we have a result record
            if (result.next()) {
                reader.readSqlResultRecord(result);
            }

            // otherwise we fail
            else {
                closeConn = true;
            }
        }

        // this is likely the error when the record is otherwise locked - we fail
        catch (SQLException e) {
            // Note: ORA-00054 gives an e.getErrorCode() of 54, if anyone cares...
            // log.warn("Sql.dbUpdateLock(): " + e.getErrorCode() + " - " + e);
            closeConn = true;
        } catch (SqlReaderFinishedException e) {
            log.warn("Sql.dbReadLock(): {}", e.toString());
            closeConn = true;
        } finally {
            // close the result and statement
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): {}", e.toString());
                }
            }
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): {}", e.toString());
                }
            }

            // if we are failing, restore and release the connectoin
            if ((closeConn) && (conn != null)) {
                // just in case we got a lock
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    log.warn("Sql.dbReadBinary(): {}", e.toString());
                }
                if (resetAutoCommit)
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException e) {
                        log.warn("Sql.dbReadBinary(): {}", e.toString());
                    }

            }

            if (conn != null) {
                returnConnection(conn);
            }
        }

        return conn;
    }

    @Override
    public void dbUpdateCommit(String sql, Object[] fields, String var, Connection conn) {
        // Note: does not support TRANSACTION_CONNECTION -ggolden

        log.debug("dbUpdateCommit(String {}, Object[] {}, String {}, Connection {})", sql, fields, var, conn);

        PreparedStatement pstmt = null;

        try {
            log.debug("Sql.dbUpdateCommit(): {}", sql);

            pstmt = conn.prepareStatement(sql);

            // put in all the fields
            int pos = prepareStatement(pstmt, fields);

            // prepare the update statement and fill with the last variable (if any)
            if (var != null) {
                sqlServiceSql.setBytes(pstmt, var, pos);
            }

            // run the SQL statement
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            // commit
            conn.commit();
        } catch (SQLException | UnsupportedEncodingException e) {
            log.warn("Sql.dbUpdateCommit(): {}", e.toString());
        } finally {
            try {
                // close the statemenet and restore / release the connection
                if (null != pstmt) pstmt.close();
                if (null != conn) {
                    // we don't really know what this should be, but we assume the default is not
                    conn.setAutoCommit(false);
                    returnConnection(conn);
                }
            } catch (Exception e) {
                log.warn("Sql.dbUpdateCommit(): {}", e.toString());
            }
        }
    }

    @Override
    public void dbCancel(Connection conn) {
        // Note: does not support TRANSACTION_CONNECTION -ggolden

        log.debug("dbCancel(Connection {})", conn);

        try {
            // cancel any changes, release any locks
            conn.rollback();

            // we don't really know what this should be, but we assume the default is not
            conn.setAutoCommit(false);
            returnConnection(conn);
        } catch (Exception e) {
            log.warn("Sql.dbCancel(): {}", e.toString());
        }
    }

    @Override
    public void ddl(ClassLoader loader, String resource) {
        log.debug("ddl(ClassLoader {}, String {})", loader, resource);

        // add the vender string path, and extension
        resource = vendor + '/' + resource + ".sql";

        // find the resource from the loader
        InputStream in = loader.getResourceAsStream(resource);

        try (in) {
            if (in == null) {
                log.warn("Sql.ddl: missing resource: {}", resource);
                return;
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
                try {
                    // read the first line, skipping any '--' comment lines
                    boolean firstLine = true;
                    StringBuilder buf = new StringBuilder();
                    for (String line = r.readLine(); line != null; line = r.readLine()) {
                        line = line.trim();
                        if (line.startsWith("--")) continue;
                        if (line.isEmpty()) continue;

                        // add the line to the buffer
                        buf.append(' ');
                        buf.append(line);

                        // process if the line ends with a ';'
                        boolean process = line.endsWith(";");

                        if (!process) continue;

                        // remove trailing ';'
                        buf.setLength(buf.length() - 1);

                        // run the first line as the test - if it fails, we are done
                        if (firstLine) {
                            firstLine = false;
                            if (!dbWrite(buf.toString(), null, null, null, 2)) {
                                return;
                            }
                        }

                        // run other lines, until done - any one can fail (we will report it)
                        else {
                            dbWrite(null, buf.toString(), null);
                        }

                        // clear the buffer for next
                        buf.setLength(0);
                    }
                } catch (IOException any) {
                    log.warn("Sql.ddl: resource: {} : {}", resource, any);
                }
            } catch (IOException any) {
                log.warn("Sql.ddl: resource: {} : {}", resource, any);
            }
        } catch (IOException any) {
            log.warn("Sql.ddl: resource: {} : {}", resource, any);
        }
    }

    /**
     * Prepare a prepared statement with fields.
     *
     * @param pstmt
     *        The prepared statement to fill in.
     * @param fields
     *        The Object array of values to fill in.
     * @return the next pos that was not filled in.
     * @throws UnsupportedEncodingException
     */
    protected int prepareStatement(PreparedStatement pstmt, Object[] fields) throws SQLException, UnsupportedEncodingException {
        if (log.isDebugEnabled()) {
            log.debug("pstmt = {}, fields = {}", pstmt, Arrays.toString(fields));
        }

        // put in all the fields
        int pos = 1;
        if (fields != null) {
            for (Object field : fields) {
                if (field == null) {
                    // Treat a Java null as an SQL null.
                    // This makes sure that Oracle vs MySQL use the same value for null.
                    sqlServiceSql.setNull(pstmt, pos);
                } else if (field instanceof String s) {
                    if (s.isEmpty()) {
                        // Treat a zero-length Java string as an SQL null
                        sqlServiceSql.setNull(pstmt, pos);
                    } else {
                        pstmt.setString(pos, s);
                    }
                } else if (field instanceof Time t) {
                    sqlServiceSql.setTimestamp(pstmt, new Timestamp(t.getTime()), pos);
                } else if (field instanceof Date d) {
                    sqlServiceSql.setTimestamp(pstmt, new Timestamp(d.getTime()), pos);
                } else if (field instanceof Instant instant) {
                    sqlServiceSql.setTimestamp(pstmt, new Timestamp(instant.toEpochMilli()), pos);
                } else if (field instanceof Long) {
                    long l = (Long) field;
                    pstmt.setLong(pos, l);
                } else if (field instanceof Integer) {
                    int n = (Integer) field;
                    pstmt.setInt(pos, n);
                } else if (field instanceof Float) {
                    float f = (Float) field;
                    pstmt.setFloat(pos, f);
                } else if (field instanceof Boolean) {
                    pstmt.setBoolean(pos, (Boolean) field);
                } else if (field instanceof byte[]) {
                    sqlServiceSql.setBytes(pstmt, (byte[]) field, pos);
                } else {
                    // %%% support any other types specially?
                    String value = field.toString();
                    sqlServiceSql.setBytes(pstmt, value, pos);
                }
                pos++;
            }
        }

        return pos;
    }

    /**
     * @param defaultDataSource
     *        The defaultDataSource to set.
     */
    public void setDefaultDataSource(DataSource defaultDataSource) {
        log.debug("setDefaultDataSource(DataSource {})", defaultDataSource);
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public Long getNextSequence(String tableName, Connection conn) {
        String sql = sqlServiceSql.getNextSequenceSql(tableName);
        return (sql == null ? null : Long.valueOf((String) (dbRead(conn, sql, null, null).get(0))));
    }

    @Override
    public String getBooleanConstant(boolean value) {
        return sqlServiceSql.getBooleanConstant(value);
    }

    /**
     * <p>
     * StreamWithConnection is a cover over a stream that comes from a statement result in a connection, holding all these until closed.
     * </p>
     */
    public class StreamWithConnection extends InputStream {
        protected Connection m_conn = null;
        protected PreparedStatement m_pstmt = null;
        protected ResultSet m_result = null;
        protected InputStream m_stream;

        public StreamWithConnection(InputStream stream, ResultSet result, PreparedStatement pstmt, Connection conn) {
            log.debug("new StreamWithConnection(InputStream {}, ResultSet {}, PreparedStatement {}, Connection {})", stream, result, pstmt, conn);

            m_conn = conn;
            m_result = result;
            m_pstmt = pstmt;
            m_stream = stream;
        }

        @Override
        public void close() throws IOException {
            log.debug("close()");
            try {
                if (m_stream != null) {
                    m_stream.close();
                    m_stream = null;
                }
            } catch (Exception e) {
                log.warn("failure closing stream", e);
            }
            try {
                if (m_result != null) {
                    m_result.close();
                    m_result = null;
                }
            } catch (Exception e) {
                log.warn("failure closing result", e);
            }
            try {
                if (m_pstmt != null) {
                    m_pstmt.close();
                    m_pstmt = null;
                }
            } catch (Exception e) {
                log.warn("failure closing statement", e);
            }
            if (m_conn != null) {
                returnConnection(m_conn);
                m_conn = null;
            }
        }

        @Override
        protected void finalize() {
            log.debug("finalize()");
            try {
                close();
            } catch (IOException ioe) {
                log.error("Finalization failure", ioe);
            }
        }

        @Override
        public int read() throws IOException {
            log.debug("read()");
            return m_stream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            log.debug("read(byte {})", Arrays.toString(b));
            return m_stream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            log.debug("read(byte {}, int {}, int {})", Arrays.toString(b), off, len);
            return m_stream.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            log.debug("skip(long {})", n);
            return m_stream.skip(n);
        }

        @Override
        public int available() throws IOException {
            log.debug("available()");
            return m_stream.available();
        }

        @Override
        public synchronized void mark(int readlimit) {
            log.debug("mark(int {})", readlimit);
            m_stream.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            log.debug("reset()");
            m_stream.reset();
        }

        @Override
        public boolean markSupported() {
            log.debug("markSupported()");
            return m_stream.markSupported();
        }
    }
}
