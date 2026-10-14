/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.db.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.test.SakaiKernelTestBase;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SqlServiceCleanupTest extends SakaiKernelTestBase {
    private BasicSqlService service;
    private DataSource originalDataSource;
    private Connection connection;
    private PreparedStatement statement;

    @BeforeClass
    public static void setupKernel() throws Exception {
        oneTimeSetup(null, "src/test/resources/jdbc-cleanup-components.xml");
    }

    @Before
    public void setupJdbcBoundary() throws Exception {
        service = (BasicSqlService) getService(SqlService.class);
        originalDataSource = (DataSource) getService("javax.sql.DataSource");
        DataSource boundary = (DataSource) getService("jdbcCleanupDataSource");
        reset(boundary);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(boundary.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        service.setDefaultDataSource(boundary);
    }

    @After
    public void restoreDataSource() {
        if (service != null) {
            service.setDefaultDataSource(originalDataSource);
        }
    }

    @Test
    public void writeReturnsConnectionWhenStatementCloseFails() throws Exception {
        doThrow(new SQLException("statement close failed")).when(statement).close();
        assertThrows(RuntimeException.class,
                () -> service.dbWriteCount("UPDATE cleanup SET value = 1", null, null, null, false));
        verify(connection).close();
    }

    @Test
    public void writeReturnsConnectionWhenRollbackFails() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("write failed"));
        doThrow(new SQLException("rollback failed")).when(connection).rollback();
        assertThrows(RuntimeException.class,
                () -> service.dbWriteCount("UPDATE cleanup SET value = 1", null, null, null, false));
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    public void writeReturnsConnectionWhenAutoCommitResetFails() throws Exception {
        when(connection.getAutoCommit()).thenReturn(true);
        doThrow(new SQLException("reset failed")).when(connection).setAutoCommit(true);
        assertThrows(RuntimeException.class,
                () -> service.dbWriteCount("UPDATE cleanup SET value = 1", null, null, null, false));
        verify(connection).close();
    }

    @Test
    public void writeLeavesCallerOwnedConnectionOpenOnCloseFailure() throws Exception {
        doThrow(new SQLException("statement close failed")).when(statement).close();
        assertThrows(RuntimeException.class,
                () -> service.dbWriteCount("UPDATE cleanup SET value = 1", null, null, connection, false));
        verify(connection, never()).close();
    }

    @Test
    public void insertReturnsConnectionWhenStatementCloseFails() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("insert failed"));
        doThrow(new SQLException("statement close failed")).when(statement).close();
        assertThrows(RuntimeException.class,
                () -> service.dbInsert(null, "INSERT INTO cleanup VALUES (1)", null, "id"));
        verify(connection).close();
    }

    @Test
    public void updateCommitReturnsConnectionWhenAutoCommitResetFails() throws Exception {
        doThrow(new SQLException("reset failed")).when(connection).setAutoCommit(false);
        service.dbUpdateCommit("UPDATE cleanup SET value = 1", null, null, connection);
        verify(statement).close();
        verify(connection).close();
    }
}
