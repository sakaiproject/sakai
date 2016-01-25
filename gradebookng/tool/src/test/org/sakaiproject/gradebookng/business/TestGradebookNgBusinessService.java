package org.sakaiproject.gradebookng.business;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TestGradebookNgBusinessService {

	@InjectMocks
	GradebookNgBusinessService service;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void injectionOk() {
		Assert.assertNotNull(service);
	}
}
