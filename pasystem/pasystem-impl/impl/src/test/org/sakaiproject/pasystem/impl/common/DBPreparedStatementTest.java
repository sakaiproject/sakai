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
package org.sakaiproject.pasystem.impl.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DBPreparedStatementTest {
    @Test
    public void queryFailureClosesStatementAndPreservesOriginalException() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException queryFailure = new SQLException("query failed");
        SQLException closeFailure = new SQLException("close failed");
        when(statement.executeQuery()).thenThrow(queryFailure);
        doThrow(closeFailure).when(statement).close();
        DBPreparedStatement query = new DBPreparedStatement(statement,
                new DBConnection(mock(Connection.class)));

        SQLException actual = assertThrows(SQLException.class, query::executeQuery);

        assertSame(queryFailure, actual);
        assertArrayEquals(new Throwable[] {closeFailure}, actual.getSuppressed());
        verify(statement).close();
    }

    @Test
    public void successfulQueryTransfersCleanupToResults() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        DBPreparedStatement query = new DBPreparedStatement(statement,
                new DBConnection(mock(Connection.class)));

        try (DBResults results = query.executeQuery()) {
            // The returned results own the JDBC resources.
        }

        verify(resultSet).close();
        verify(statement).close();
    }
}
