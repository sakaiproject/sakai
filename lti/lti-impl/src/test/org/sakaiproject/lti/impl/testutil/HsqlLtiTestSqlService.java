/**
 * Copyright (c) 2026 The Apereo Foundation
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
 */
package org.sakaiproject.lti.impl.testutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.sakaiproject.db.api.SqlService;

import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * Minimal in-memory HSQL {@link SqlService} for LTI Foorm DAO tests.
 */
public class HsqlLtiTestSqlService {

    private final DataSource dataSource;

    public HsqlLtiTestSqlService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SqlService getSqlService() {
        return mock(SqlService.class, withSettings().defaultAnswer(invocation -> {
            String method = invocation.getMethod().getName();
            switch (method) {
                case "getVendor":
                    return "hsqldb";
                case "borrowConnection":
                    return dataSource.getConnection();
                case "returnConnection":
                    closeConnection((Connection) invocation.getArgument(0));
                    return null;
                case "dbInsert":
                    return dbInsert(
                            (Connection) invocation.getArgument(0),
                            (String) invocation.getArgument(1),
                            (Object[]) invocation.getArgument(2),
                            (String) invocation.getArgument(3));
                case "dbWriteCount":
                    return dbWriteCount(
                            (String) invocation.getArgument(0),
                            (Object[]) invocation.getArgument(1));
                default:
                    return RETURNS_DEFAULTS.answer(invocation);
            }
        }));
    }

    private Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn) throws SQLException {
        boolean localConnection = callerConnection == null;
        Connection conn = localConnection ? dataSource.getConnection() : callerConnection;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bindFields(pstmt, fields);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return null;
        } finally {
            if (localConnection) {
                closeConnection(conn);
            }
        }
    }

    private int dbWriteCount(String sql, Object[] fields) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindFields(pstmt, fields);
            return pstmt.executeUpdate();
        }
    }

    private static void bindFields(PreparedStatement pstmt, Object[] fields) throws SQLException {
        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.length; i++) {
            pstmt.setObject(i + 1, fields[i]);
        }
    }

    private static void closeConnection(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }
}
