/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.springframework.orm.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.ClassicAvgFunction;
import org.hibernate.dialect.function.ClassicCountFunction;
import org.hibernate.dialect.function.ClassicSumFunction;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * AddableSessionFactoryBean is the way sakai configures hibernate with all the mappings that
 * are configured implementing {@link AdditionalHibernateMappings}.
 *
 */
@Slf4j
public class AddableSessionFactoryBean extends LocalSessionFactoryBean implements ApplicationContextAware
{
	@Setter private ApplicationContext applicationContext;
	@Setter private ServerConfigurationService serverConfigurationService;

	/**
	 * This method is called after the LocalSessionFactory is instantiated
	 */
	public void init() {
		// Provide backwards compatibility with Hibernate 3.1.x behavior for aggregate functions.
		Configuration config = getConfiguration();
		config.addSqlFunction("count", new ClassicCountFunction());
		config.addSqlFunction("avg", new ClassicAvgFunction());
		config.addSqlFunction("sum", new ClassicSumFunction());
	}

	@Override
	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
		List<AdditionalHibernateMappings> mappings = new ArrayList<>();
		String[] names = applicationContext.getBeanNamesForType(AdditionalHibernateMappings.class, false, false);

		try {
			for (String name : names) {
				mappings.add((AdditionalHibernateMappings) applicationContext.getBean(name));
			}

			Collections.sort(mappings);

			for (AdditionalHibernateMappings mapping : mappings) {
				mapping.processAdditionalMappings(sfb);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		AssignableUUIDGenerator.setServerConfigurationService(serverConfigurationService);
		sfb.getIdentifierGeneratorFactory().register("uuid2", AssignableUUIDGenerator.class);

		return sfb.buildSessionFactory();
	}
}
