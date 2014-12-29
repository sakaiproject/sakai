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
