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

import org.mockito.Mockito;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class UserAuditTestConfiguration extends SakaiTestConfiguration {

	@Autowired
	@Qualifier("org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.userauditservice")
	private AdditionalHibernateMappings additionalHibernateMappings;

	@Override
	protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
		return additionalHibernateMappings;
	}

	@Bean(name = "org.sakaiproject.event.api.EventTrackingService")
	public EventTrackingService eventTrackingService() {
		return Mockito.mock(EventTrackingService.class);
	}

	@Bean(name = "org.sakaiproject.site.api.SiteService")
	public SiteService siteService() {
		return Mockito.mock(SiteService.class);
	}
}
