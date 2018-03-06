/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.component.test.dynamic;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Transactional
@Slf4j
public class DbPropertiesDao {
	private JdbcTemplate jdbcTemplate;
	private boolean autoDdl;
	private Map<String, String> initialDbProperties;
	
	public void init() {
		if (autoDdl) {
			boolean isTableCreated = false;
			try {
				// Check for table existence.
				int count = jdbcTemplate.queryForObject("select count(*) from TEST_DB_PROPERTIES_T", Integer.class);
				isTableCreated = true;
				if (log.isDebugEnabled()) log.debug("DB properties count=" + count);
			} catch (BadSqlGrammarException e) {
			}
			if (!isTableCreated) {
				createTable();
			}
		}
	}
	
	private void createTable() {
		jdbcTemplate.execute(
				"create table TEST_DB_PROPERTIES_T (ITEM_KEY varchar(255), ITEM_VALUE varchar(255), primary key (ITEM_KEY))"
			);
		for (String initialKey : initialDbProperties.keySet()) {
			jdbcTemplate.update(
				"insert into TEST_DB_PROPERTIES_T (ITEM_KEY, ITEM_VALUE) values(?, ?)", 
				initialKey, initialDbProperties.get(initialKey));
		}
	}
	
	public Properties getProperties() {
		Properties properties = new Properties();
		List <Map<String, Object>> rows = jdbcTemplate.queryForList(
			"select * from TEST_DB_PROPERTIES_T"
		);
		for (Map<String, Object> row : rows) {
			properties.put(row.get("ITEM_KEY"), row.get("ITEM_VALUE"));
		}
		return properties;
	}

	public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}

	public void setInitialDbProperties(Map<String, String> initialDbProperties) {
		this.initialDbProperties = initialDbProperties;
	}
}
