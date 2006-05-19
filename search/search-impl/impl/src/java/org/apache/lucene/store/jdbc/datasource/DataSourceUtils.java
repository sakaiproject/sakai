/*
 * Copyright 2004-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc.datasource;

import java.sql.*;
import javax.sql.DataSource;
import org.apache.lucene.store.jdbc.JdbcStoreException;

/**
 * A set of Jdbc <code>DataSource</code> utilities.
 * 
 * @author kimchy
 */
public abstract class DataSourceUtils {

    /**
     * Returns <code>true</code> if the connection was created by the {@link TransactionAwareDataSourceProxy} and it
     * controls the connection (i.e. it is the most outer connection created).
     */
    public static boolean controlConnection(Connection connection) {
        return ((connection instanceof ConnectionProxy) && ((ConnectionProxy) connection).controlConnection());
    }

    /**
     * Returns a jdbc connection, and in case of failure, wraps the sql
     * exception with a Jdbc device exception.
     */
    public static Connection getConnection(DataSource dataSource) throws JdbcStoreException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new JdbcStoreException("Failed to open jdbc connection", e);
        }
    }

    /**
     * Close the given JDBC connection and ignore any thrown exception. This is
     * useful for typical finally blocks in manual JDBC code.
     * <p/>
     * Will only close the connection under two conditions:
     * If the connection was not created by the {@link TransactionAwareDataSourceProxy}, or if it was created
     * by {@link TransactionAwareDataSourceProxy}, and the connection controls the connection
     * (i.e. it is the most outer connection created).
     */
    public static void releaseConnection(Connection con) {
        if (con == null) {
            return;
        }
        if (!(con instanceof ConnectionProxy) || controlConnection(con)) {
            try {
                con.close();
            } catch (SQLException ex) {
                //do nothing
            } catch (RuntimeException ex) {
                // do nothing
            }
        }
    }

    /**
     * Commits the connection only if the connection is controlled by us. The connection is controlled if it is
     * the <code>TransactionAwareDataSourceProxy</code> and it is the most outer connection in the tree of connections
     * the <code>TransactionAwareDataSourceProxy</code> returned.
     */
    public static void commitConnectionIfPossible(Connection con) throws JdbcStoreException {
        try {
            if (con != null && controlConnection(con)) {
                con.commit();
            }
        } catch (SQLException e) {
            throw new JdbcStoreException("Failed to commit jdbc connection", e);
        }
    }

    /**
     * Same as {@link #commitConnectionIfPossible(java.sql.Connection)}, only does not throw an exception
     */
    public static void safeCommitConnectionIfPossible(Connection con) {
        try {
            if (con != null && controlConnection(con)) {
                con.commit();
            }
        } catch (SQLException e) {
            safeRollbackConnectionIfPossible(con);
        }
    }

    /**
     * Tollbacks the connection only if the connection is controlled by us. The connection is controlled if it is
     * the <code>TransactionAwareDataSourceProxy</code> and it is the most outer connection in the tree of connections
     * the <code>TransactionAwareDataSourceProxy</code> returned.
     */
    public static void rollbackConnectionIfPossible(Connection con) throws JdbcStoreException {
        try {
            if (con != null && controlConnection(con)) {
                con.rollback();
            }
        } catch (SQLException e) {
            throw new JdbcStoreException("Failed to rollback jdbc connection", e);
        }
    }

    /**
     * Same as {@link #rollbackConnectionIfPossible(java.sql.Connection)}, only does not throw an exception.
     */
    public static void safeRollbackConnectionIfPossible(Connection con) {
        try {
            if (con != null && controlConnection(con)) {
                con.rollback();
            }
        } catch (SQLException e) {
            // do nothing
        }
    }

    /**
     * Close the given JDBC Statement and ignore any thrown exception. This is
     * useful for typical finally blocks in manual JDBC code.
     * 
     * @param stmt
     *            the JDBC Statement to close
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                // do nothing
            } catch (RuntimeException ex) {
                // do nothing
            }
        }
    }

    /**
     * Close the given JDBC ResultSet and ignore any thrown exception. This is
     * useful for typical finally blocks in manual JDBC code.
     * 
     * @param rs
     *            the JDBC ResultSet to close
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                // do nothing
            } catch (RuntimeException ex) {
                // do nothing
            }
        }
    }

    /**
     * Returns the column index for the guven column name. Note that if there
     * are two columns with the same name, the first onde index will be
     * returned.
     * <p>
     * <code>-1</code> is returned if none is found.
     * 
     * @param metaData
     * @param columnName
     * @return Column index for the given column name
     * @throws java.sql.SQLException
     */
    public static int getColumnIndexFromColumnName(ResultSetMetaData metaData, String columnName) throws SQLException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String tmpName = metaData.getColumnLabel(i);
            if (tmpName.equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    public static Connection getTargetConnection(Connection conn) {
        if (conn instanceof ConnectionProxy ) {
            return ((ConnectionProxy) conn).getTargetConnection();
        }
        return conn;
    }
}
