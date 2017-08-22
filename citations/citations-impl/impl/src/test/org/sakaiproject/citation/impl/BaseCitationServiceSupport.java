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
import org.jmock.integration.junit3.MockObjectTestCase;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.ConfigurationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.impl.openurl.BookConverter;
import org.sakaiproject.citation.impl.openurl.ContextObject;
import org.sakaiproject.citation.impl.openurl.ContextObject.Entity;
import org.sakaiproject.citation.impl.openurl.ContextObjectEntity;
import org.sakaiproject.citation.impl.openurl.KEVFormat;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.id.api.IdManager;

public class BaseCitationServiceSupport extends MockObjectTestCase {

	public BaseCitationService createCitationService() {
		final ContentHostingService chsMock = mock(ContentHostingService.class);
		final EntityManager emMock = mock(EntityManager.class);
		final ConfigurationService csMock = mock(ConfigurationService.class);
		final ServerConfigurationService scsMock = mock(ServerConfigurationService.class);
		final IdManager imMock = mock(IdManager.class);
		
		// Just mocking them up so thing startup.
		checking(new Expectations(){
			{
				ignoring(chsMock);
				ignoring(emMock);
				ignoring(csMock);
				ignoring(scsMock);
				ignoring(imMock);
			}
		});

		BaseCitationService api = new BasicCitationService();
		api.setContentHostingService(chsMock);
		api.setEntityManager(emMock);
		api.setConfigurationService(csMock);
		api.setServerConfigurationService(scsMock);
		api.setIdManager(imMock);
		
		api.init();
		return api;
	}
	
	
}
