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
