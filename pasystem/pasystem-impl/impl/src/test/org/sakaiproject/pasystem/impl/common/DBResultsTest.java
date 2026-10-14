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

public class DBResultsTest {
    @Test
    public void closesStatementEvenWhenResultSetCloseFails() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        SQLException resultFailure = new SQLException("result close failed");
        SQLException statementFailure = new SQLException("statement close failed");
        doThrow(resultFailure).when(resultSet).close();
        doThrow(statementFailure).when(statement).close();
        DBResults results = new DBResults(resultSet, statement);

        SQLException actual = assertThrows(SQLException.class, results::close);

        assertSame(resultFailure, actual);
        assertArrayEquals(new Throwable[] {statementFailure}, actual.getSuppressed());
        verify(statement).close();
    }
}
