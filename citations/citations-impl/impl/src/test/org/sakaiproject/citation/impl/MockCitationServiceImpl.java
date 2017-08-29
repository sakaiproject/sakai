/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.citation.impl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.sakaiproject.citation.api.ConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.id.api.IdManager;

public class MockCitationServiceImpl extends BasicCitationService {

	private Mockery context = new Mockery();
	
	public void init() {
		
		final ContentHostingService chsMock = context.mock(ContentHostingService.class);
		final EntityManager emMock = context.mock(EntityManager.class);
		final ConfigurationService csMock = context.mock(ConfigurationService.class);
		final ServerConfigurationService scsMock = context.mock(ServerConfigurationService.class);
		final IdManager imMock = context.mock(IdManager.class);
		
		// Just mocking them up so thing startup.
		context.checking(new Expectations(){
			{
				ignoring(chsMock);
				ignoring(emMock);
				ignoring(csMock);
				ignoring(scsMock);
				ignoring(imMock);
			}
		});

		setContentHostingService(chsMock);
		setEntityManager(emMock);
		setConfigurationService(csMock);
		setServerConfigurationService(scsMock);
		setIdManager(imMock);
		
		super.init();
	}

}
