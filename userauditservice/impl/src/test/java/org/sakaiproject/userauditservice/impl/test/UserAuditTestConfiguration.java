/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.userauditservice.impl.test;

import org.hibernate.SessionFactory;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
import org.sakaiproject.userauditservice.api.repository.UserAuditLogRepository;
import org.sakaiproject.userauditservice.impl.repository.UserAuditLogRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@PropertySource("classpath:/hibernate.properties")
public class UserAuditTestConfiguration extends SakaiTestConfiguration {

	@Override
	protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
		AdditionalHibernateMappingsImpl mappings = new AdditionalHibernateMappingsImpl();
		mappings.setAnnotatedClasses(new Class<?>[] { UserAuditLog.class });
		return mappings;
	}

	@Bean
	public UserAuditLogRepository userAuditLogRepository(SessionFactory sessionFactory) {
		UserAuditLogRepositoryImpl repository = new UserAuditLogRepositoryImpl();
		repository.setSessionFactory(sessionFactory);
		return repository;
	}
}
