/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.component.test.dynamic;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Transactional
public class DbPropertiesDao {
	private static Log log = LogFactory.getLog(DbPropertiesDao.class);
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private boolean autoDdl;
	private Map<String, String> initialDbProperties;
	
	public void init() {
		if (autoDdl) {
			boolean isTableCreated = false;
			try {
				// Check for table existence.
				int count = simpleJdbcTemplate.queryForInt("select count(*) from TEST_DB_PROPERTIES_T");
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
		simpleJdbcTemplate.getJdbcOperations().execute(
				"create table TEST_DB_PROPERTIES_T (ITEM_KEY varchar(255), ITEM_VALUE varchar(255), primary key (ITEM_KEY))"
			);
		for (String initialKey : initialDbProperties.keySet()) {
			simpleJdbcTemplate.update(
				"insert into TEST_DB_PROPERTIES_T (ITEM_KEY, ITEM_VALUE) values(?, ?)", 
				initialKey, initialDbProperties.get(initialKey));
		}
	}
	
	public Properties getProperties() {
		Properties properties = new Properties();
		List <Map<String, Object>> rows = simpleJdbcTemplate.queryForList(
			"select * from TEST_DB_PROPERTIES_T"
		);
		for (Map<String, Object> row : rows) {
			properties.put(row.get("ITEM_KEY"), row.get("ITEM_VALUE"));
		}
		return properties;
	}

	public void setDataSource(DataSource dataSource) {
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}

	public void setInitialDbProperties(Map<String, String> initialDbProperties) {
		this.initialDbProperties = initialDbProperties;
	}
}
