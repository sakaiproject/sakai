/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.impl;

import javax.sql.DataSource;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@ImportResource("classpath:/WEB-INF/tags-service.xml")
@PropertySource("classpath:/hibernate.properties")
public class TagServiceTestConfiguration extends SakaiTestConfiguration {
    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.tagservice")
    private AdditionalHibernateMappings mappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return mappings;
    }

    @Override
    @Bean(name = "javax.sql.DataSource")
    public DataSource dataSource() {
        DataSource dataSource = super.dataSource();
        // Use the pre-JPA schema: this must work without recreating existing tables.
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/hsqldb.sql")).execute(dataSource);
        new JdbcTemplate(dataSource).execute("CREATE TABLE tagservice_tagassociation (id VARCHAR(99) PRIMARY KEY, "
            + "tag_id VARCHAR(255), item_id VARCHAR(255), UNIQUE(tag_id, item_id))");
        return dataSource;
    }
}
