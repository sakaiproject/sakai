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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Exercises column mapping through a real in-memory JDBC database.
 * MySQL-specific DATETIME conversion still requires verification with Connector/J.
 */
public class FoormMapRowMapperTest {

    private EmbeddedDatabase database;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        database = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
        jdbcTemplate = new JdbcTemplate(database);
        jdbcTemplate.execute("CREATE TABLE lti_dates (created_at TIMESTAMP, updated_at DATE, title VARCHAR(255))");
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.shutdown();
        }
    }

    @Test
    public void testTimestampColumnReturnsTimestamp() {
        Timestamp expectedTimestamp = Timestamp.from(Instant.parse("2026-09-01T12:34:56.123456Z"));
        jdbcTemplate.update("INSERT INTO lti_dates (created_at) VALUES (?)", expectedTimestamp);

        Map<String, Object> result = jdbcTemplate.queryForObject(
                "SELECT created_at FROM lti_dates", new FoormMapRowMapper(new String[] {"created_at"}));

        assertTrue(result.get("created_at") instanceof Timestamp);
        assertEquals(expectedTimestamp, result.get("created_at"));
    }

    @Test
    public void testDateColumnReturnsTimestamp() {
        jdbcTemplate.execute("INSERT INTO lti_dates (updated_at) VALUES (DATE '2026-09-01')");

        Map<String, Object> result = jdbcTemplate.queryForObject(
                "SELECT updated_at FROM lti_dates", new FoormMapRowMapper(new String[] {"updated_at"}));

        assertTrue(result.get("updated_at") instanceof Timestamp);
        assertEquals(Timestamp.valueOf("2026-09-01 00:00:00"), result.get("updated_at"));
    }

    @Test
    public void testNonDateColumnPreservesValue() {
        jdbcTemplate.update("INSERT INTO lti_dates (title) VALUES (?)", "My LTI Tool");

        Map<String, Object> result = jdbcTemplate.queryForObject(
                "SELECT title FROM lti_dates", new FoormMapRowMapper(new String[] {"title"}));

        assertEquals("My LTI Tool", result.get("title"));
    }

    @Test
    public void testNullDatesArePreserved() {
        jdbcTemplate.execute("INSERT INTO lti_dates (created_at, updated_at) VALUES (NULL, NULL)");

        Map<String, Object> result = jdbcTemplate.queryForObject(
                "SELECT created_at, updated_at FROM lti_dates",
                new FoormMapRowMapper(new String[] {"created_at", "updated_at"}));

        assertTrue(result.containsKey("created_at"));
        assertTrue(result.containsKey("updated_at"));
        assertNull(result.get("created_at"));
        assertNull(result.get("updated_at"));
    }
}
