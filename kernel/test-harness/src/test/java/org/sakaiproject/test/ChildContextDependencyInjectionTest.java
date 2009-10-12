/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.test;

import junit.framework.Assert;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 *
 */
public class ChildContextDependencyInjectionTest extends SakaiDependencyInjectionTests {
	private ServerConfigurationService serverConfigurationService;
	private SomeBean someBean;
	
	static {
		setSakaiHome("childcontext");
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] {"childcontext/test-spring.xml"};
	}

	public void testLocalContext() throws Exception {
		Assert.assertNotNull(serverConfigurationService);
		Assert.assertNotNull(someBean);
		Assert.assertNotNull(someBean.getSiteService());
		
		// Make sure the Component Manager can't see locally defined beans.
		SomeBean anAttempt = (SomeBean)ComponentContainerEmulator.getService("someBean");
		Assert.assertNull(anAttempt);
	}

	public void testLocalConfiguration() throws Exception {
		// Make sure that our test-specific Sakai configuration was used.
		Assert.assertEquals("successful.test", serverConfigurationService.getServerId());
	}
	
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	public void setSomeBean(SomeBean someBean) {
		this.someBean = someBean;
	}
}
