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
/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2006, 2008 The Sakai Foundation, The MIT Corporation
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
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.db.api.SqlService;

import org.sakaiproject.service.gradebook.shared.GradebookConfiguration;

/**
 * Sakai-specific add-on to the vanilla configuration bean.
 * The main addition is to handle automatic database upgrades which lie outside
 * the scope of Hibernate's normal SchemaUpdate. In versions of Hibernate through
 * at least 3.1.3, this includes any index definitions. (See Hibernate JIRA
 * issue HHH-1012.)
 */
public class GradebookConfigurationSakai extends GradebookConfiguration {
    private static final Logger log = LoggerFactory.getLogger(GradebookConfigurationSakai.class);

    private SqlService sqlService;
    private boolean autoDdl;

	public void init() {
		String sqlUpdateScriptName = "sakai_gradebook_post_schemaupdate";
		if (autoDdl && (sqlService != null)) {
			if (log.isInfoEnabled()) log.info("About to call sqlService.ddl with " + sqlUpdateScriptName);
			sqlService.ddl(this.getClass().getClassLoader(), sqlUpdateScriptName);
		}
		super.init();
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}
}
