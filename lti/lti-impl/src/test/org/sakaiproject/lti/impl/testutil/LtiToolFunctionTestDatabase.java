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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDriver;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.impl.DBLTIService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.foorm.Foorm;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * In-memory HSQL database and {@link DBLTIService} wiring for tool-function tests.
 */
public class LtiToolFunctionTestDatabase {

    public static final String JDBC_URL = "jdbc:hsqldb:mem:ltiToolFunctionTest;sql.enforce_size=false";

    public static final List<String> REGISTERED_FUNCTIONS = Arrays.asList(
            "content.read",
            "content.write",
            "gradebook.read",
            "gradebook.write"
    );

    private final DataSource dataSource;
    private final SqlService sqlService;
    private final DBLTIService ltiService;

    public LtiToolFunctionTestDatabase() throws SQLException {
        ComponentManager.testingMode = true;
        ComponentManager.shutdown();

        dataSource = new SimpleDriverDataSource(new JDBCDriver(), JDBC_URL, "sa", "");
        createLtiTables();

        sqlService = new HsqlLtiTestSqlService(dataSource).getSqlService();
        registerFunctionManager();
        ComponentManager.loadComponent(javax.sql.DataSource.class, dataSource);

        ltiService = new DBLTIService();
        ltiService.setSqlService(sqlService);
        ltiService.setAutoDdl("false");
        ltiService.setSiteService(buildSiteService());
        ltiService.init();
    }

    public DBLTIService getLtiService() {
        return ltiService;
    }

    public void shutdown() {
        ComponentManager.shutdown();
        ComponentManager.testingMode = false;
    }

    private void createLtiTables() throws SQLException {
        Foorm foorm = new Foorm();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "");
                Statement statement = conn.createStatement()) {
            for (String sql : foorm.formSqlTable("lti_tools", LTIService.TOOL_MODEL, "hsqldb", true)) {
                statement.execute(sql);
            }
            for (String sql : foorm.formSqlTable("lti_tool_functions", LTIService.TOOL_FUNCTION_MODEL, "hsqldb", true)) {
                statement.execute(sql);
            }
        }
    }

    private static SiteService buildSiteService() {
        SiteService siteService = mock(SiteService.class);
        when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
        when(siteService.allowUpdateSite("site.example.com")).thenReturn(true);
        return siteService;
    }

    private static void registerFunctionManager() {
        FunctionManager functionManager = mock(FunctionManager.class);
        when(functionManager.getRegisteredFunctions()).thenReturn(REGISTERED_FUNCTIONS);
        ComponentManager.loadComponent(FunctionManager.class, functionManager);
    }
}
