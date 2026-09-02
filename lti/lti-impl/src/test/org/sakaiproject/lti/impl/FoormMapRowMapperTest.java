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

package org.sakaiproject.lti.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the FoormMapRowMapper to ensure it correctly delegates DATETIME/TIMESTAMP
 * columns to ResultSet.getTimestamp() to prevent MySQL 8 LocalDateTime timezone issues.
 */
public class FoormMapRowMapperTest {

    private ResultSet rs;
    private ResultSetMetaData metaData;

    @Before
    public void setUp() throws SQLException {
        rs = Mockito.mock(ResultSet.class);
        metaData = Mockito.mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
    }

    @Test
    public void testTimestampColumnReturnsTimestamp() throws SQLException {
        String[] columns = {"created_at"};
        FoormMapRowMapper mapper = new FoormMapRowMapper(columns);

        Timestamp expectedTimestamp = Timestamp.from(Instant.parse("2026-09-01T00:00:00Z"));

        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("created_at");
        when(metaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(rs.getTimestamp(1)).thenReturn(expectedTimestamp);

        Map<String, Object> result = mapper.mapRow(rs, 1);

        assertEquals(expectedTimestamp, result.get("created_at"));
        assertTrue(result.get("created_at") instanceof Date);
        // Verify getObject was never called for this column
        verify(rs, never()).getObject(1);
    }

    @Test
    public void testDateColumnReturnsTimestamp() throws SQLException {
        String[] columns = {"updated_at"};
        FoormMapRowMapper mapper = new FoormMapRowMapper(columns);

        Timestamp expectedTimestamp = Timestamp.from(Instant.parse("2026-09-01T00:00:00Z"));

        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("updated_at");
        when(metaData.getColumnType(1)).thenReturn(Types.DATE);
        when(rs.getTimestamp(1)).thenReturn(expectedTimestamp);

        Map<String, Object> result = mapper.mapRow(rs, 1);

        assertEquals(expectedTimestamp, result.get("updated_at"));
        // Verify getObject was never called for this column
        verify(rs, never()).getObject(1);
    }

    @Test
    public void testNonDateColumnUsesGetObject() throws SQLException {
        String[] columns = {"title"};
        FoormMapRowMapper mapper = new FoormMapRowMapper(columns);

        String expectedValue = "My LTI Tool";

        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("title");
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(rs.getObject(1)).thenReturn(expectedValue);

        Map<String, Object> result = mapper.mapRow(rs, 1);

        assertEquals(expectedValue, result.get("title"));
        // Verify getTimestamp was never called for this column
        verify(rs, never()).getTimestamp(1);
    }

    @Test
    public void testNullTimestampIsHandled() throws SQLException {
        String[] columns = {"created_at"};
        FoormMapRowMapper mapper = new FoormMapRowMapper(columns);

        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("created_at");
        when(metaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(rs.getTimestamp(1)).thenReturn(null);

        Map<String, Object> result = mapper.mapRow(rs, 1);

        assertNull(result.get("created_at"));
    }
}