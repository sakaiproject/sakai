/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tags.impl;

import java.util.UUID;
import javax.sql.DataSource;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.junit.Assert.*;

/** Runs the shipped conversion against the legacy database schema. */
public class TagsConversionTest {
    @Test
    public void convertsMessagesBySiteWithoutChangingSharedPoolOrQuestionTagIds() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:" + UUID.randomUUID(), "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE SAKAI_REALM_FUNCTION (FUNCTION_NAME VARCHAR(255))");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/hsqldb.sql")).execute(dataSource);
        jdbc.execute("CREATE TABLE tagservice_tagassociation (id VARCHAR(99) PRIMARY KEY, tag_id VARCHAR(255), item_id VARCHAR(255), UNIQUE(tag_id, item_id))");
        jdbc.execute("CREATE TABLE SAKAI_SITE (SITE_ID VARCHAR(99) PRIMARY KEY)");
        jdbc.execute("CREATE TABLE SAKAI_USER_ID_MAP (USER_ID VARCHAR(99))");
        jdbc.execute("CREATE TABLE MFR_PVT_MSG_USR_T (messageSurrogateKey BIGINT, CONTEXT_ID VARCHAR(99))");
        jdbc.execute("CREATE TABLE SAKAI_SITE_PAGE (PAGE_ID VARCHAR(99), SITE_ID VARCHAR(99), TITLE VARCHAR(99), LAYOUT VARCHAR(99), SITE_ORDER INTEGER, POPUP VARCHAR(99))");
        jdbc.execute("CREATE TABLE SAKAI_SITE_TOOL (TOOL_ID VARCHAR(99), PAGE_ID VARCHAR(99), SITE_ID VARCHAR(99), REGISTRATION VARCHAR(99), PAGE_ORDER INTEGER, TITLE VARCHAR(99), LAYOUT_HINTS VARCHAR(99))");
        jdbc.update("INSERT INTO SAKAI_SITE VALUES ('site1'), ('site2'), ('!admin')");
        jdbc.update("INSERT INTO SAKAI_USER_ID_MAP VALUES ('user1')");
        jdbc.update("INSERT INTO SAKAI_SITE_PAGE VALUES ('home', '!admin', 'Home', '0', 1, '0')");
        jdbc.update("INSERT INTO tagservice_collection (tagcollectionid, name) VALUES ('user1', 'Personal'), ('site1', 'site1'), ('global', 'Global')");
        jdbc.update("INSERT INTO tagservice_tag (tagid, tagcollectionid, taglabel, creationdate) VALUES ('personal', 'user1', 'Competency', NULL), ('unused', 'user1', 'Unused', NULL), ('system', 'global', 'System', 100)");
        jdbc.update("INSERT INTO tagservice_tagassociation VALUES ('a1', 'personal', '101'), ('a2', 'personal', '102'), ('pool', 'personal', '/samigo/pool/1'), ('global', 'system', '101')");
        jdbc.update("INSERT INTO MFR_PVT_MSG_USR_T VALUES (101, 'site1'), (101, 'site1'), (102, 'site2')");

        new ResourceDatabasePopulator(new ClassPathResource("db/conversion/SAK-52046/hsqldb.sql")).execute(dataSource);

        assertEquals("~user1", jdbc.queryForObject("SELECT siteid FROM tagservice_collection WHERE tagcollectionid = 'user1'", String.class));
        assertEquals("site1", jdbc.queryForObject("SELECT siteid FROM tagservice_collection WHERE tagcollectionid = 'site1'", String.class));
        assertNull(jdbc.queryForObject("SELECT siteid FROM tagservice_collection WHERE tagcollectionid = 'global'", String.class));
        assertEquals(Integer.valueOf(2), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tag WHERE taglabel = 'Competency' AND tagcollectionid IN ('site1', 'site2')", Integer.class));
        assertEquals(Integer.valueOf(2), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tagassociation a JOIN tagservice_tag t ON t.tagid = a.tag_id WHERE t.tagcollectionid IN ('site1', 'site2')", Integer.class));
        assertEquals(Integer.valueOf(0), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tagassociation WHERE tag_id = 'personal' AND item_id IN ('101', '102')", Integer.class));
        assertEquals("personal", jdbc.queryForObject("SELECT tag_id FROM tagservice_tagassociation WHERE item_id = '/samigo/pool/1'", String.class));
        assertEquals(Integer.valueOf(3), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tag WHERE tagid IN ('personal', 'unused', 'system')", Integer.class));
        assertEquals(Integer.valueOf(2), jdbc.queryForObject("SELECT COUNT(*) FROM tagservice_tag WHERE tagcollectionid IN ('site1', 'site2') AND creationdate IS NULL", Integer.class));
        assertEquals(Integer.valueOf(1), jdbc.queryForObject("SELECT COUNT(*) FROM SAKAI_SITE_TOOL WHERE REGISTRATION = 'sakai.tagservice' AND SITE_ID = '!admin'", Integer.class));
        jdbc.execute("SHUTDOWN");
    }
}
