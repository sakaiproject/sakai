/**********************************************************************************
 * Copyright 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.tool.api.ToolManager;

@RunWith(value = MockitoJUnitRunner.class)
public class ConfigLogicImplTest {

	@Mock
	ExternalLogic externalLogic;

	@Mock
	ServerConfigurationService configService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	ToolManager toolManager;

	Properties props;
	ConfigLogicImpl logic;

	@Before
	public void setUp() {
		props = new Properties();

		when(toolManager.getCurrentPlacement().getConfig()).thenReturn(props);

		logic = new ConfigLogicImpl();
		logic.setExternalLogic(externalLogic);
		logic.setServerConfigurationService(configService);
		logic.setToolManager(toolManager);
	}

	@Test
	public void getConfig() {
		logic.getConfig();
	}
}
