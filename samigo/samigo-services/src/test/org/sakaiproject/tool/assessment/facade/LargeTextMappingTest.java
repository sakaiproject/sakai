/*
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
package org.sakaiproject.tool.assessment.facade;

import java.sql.Types;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Verify dialect DDL using Samigo's production mappings, without a database. */
public class LargeTextMappingTest {
    @Test
    public void preservesLongTextOnMySql() {
        assertLargeTextType("org.hibernate.dialect.MySQLDialect", "longtext");
    }

    @Test
    public void preservesLongTextOnMariaDb() {
        assertLargeTextType("org.hibernate.dialect.MariaDBDialect", "longtext");
    }

    @Test
    public void preservesClobOnOracle() {
        assertLargeTextType("org.hibernate.dialect.OracleDialect", "clob");
    }

    private void assertLargeTextType(String dialect, String expectedType) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", dialect)
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            String base = "org/sakaiproject/tool/assessment/data/dao/";
            String[] mappings = {
                "assessment/AssessmentBase.hbm.xml", "assessment/ItemData.hbm.xml",
                "shared/TypeData.hbm.xml", "questionpool/QuestionPoolData.hbm.xml",
                "assessment/PublishedAssessment.hbm.xml", "assessment/PublishedItemData.hbm.xml",
                "grading/GradingData.hbm.xml", "grading/MediaData.hbm.xml",
                "authz/AuthorizationData.hbm.xml", "assessment/FavoriteColChoices.hbm.xml"
            };
            for (String mapping : mappings) {
                sources.addResource(base + mapping);
            }
            Metadata metadata = sources.buildMetadata();
            int checked = 0;
            for (org.hibernate.boot.model.relational.Namespace namespace : metadata.getDatabase().getNamespaces()) {
                for (Table table : namespace.getTables()) {
                    for (Column column : table.getColumns()) {
                        if (column.getSqlTypeCode(metadata) == Types.CLOB) {
                            assertEquals(table.getName() + "." + column.getName(), expectedType, column.getSqlType(metadata));
                            checked++;
                        }
                    }
                }
            }
            assertTrue("Production mappings must include large text columns", checked >= 14);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
