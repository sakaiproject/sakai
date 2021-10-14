/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.event.impl.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import static org.mockito.Mockito.when;
import org.sakaiproject.event.api.EventTrackingService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LearningResourceTestConfiguration.class})
public class LearningResourceStoreServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired private LearningResourceStoreService lrss;
	@Autowired private ServerConfigurationService serverConfigurationService;
	@Autowired private EventTrackingService eventTrackingService;
	
    @Before
    public void setUp() {
    	when(serverConfigurationService.getServerName()).thenReturn("localhost");
    	when(serverConfigurationService.getServerId()).thenReturn("localhost");
    	when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080/");
    	when(serverConfigurationService.getPortalUrl()).thenReturn("http://localhost:8080/portal");
    	AopTestUtils.getTargetObject(lrss);
    }
	
    @Test
    public void LearningResourceStoreServiceIsValid() {
        Assert.assertNotNull(lrss);
    }

    @Test
    public void LearningResourceStoreService() {
    	try {
    		eventTrackingService.post(eventTrackingService.newEvent("user.login", null, true));
    		Assert.assertTrue(Boolean.TRUE);
    	}catch (Exception e){
    		Assert.assertTrue(Boolean.FALSE);
    	}
    }
}
