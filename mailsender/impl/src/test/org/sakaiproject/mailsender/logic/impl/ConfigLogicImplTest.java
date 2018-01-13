/**********************************************************************************
 * Copyright 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.ConfigParams;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;
import org.sakaiproject.tool.api.ToolManager;

@RunWith(value = MockitoJUnitRunner.class)
@Slf4j
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
		ConfigEntry config = logic.getConfig();
		assertNotNull(config);
	}

	@Test
	public void allowSubjectPrefixChange() {
		assertFalse(logic.allowSubjectPrefixChange());

		when(
				configService.getBoolean(ConfigLogic.ALLOW_PREFIX_CHANGE_PROP, 
						false)).thenReturn(true);
		assertTrue(logic.allowSubjectPrefixChange());
	}

	@Test
	public void isEmailTestMode() {
		assertFalse(logic.isEmailTestMode());

		when(configService.getBoolean(ConfigLogic.EMAIL_TEST_MODE_PROP, false))
				.thenReturn(true);
		assertTrue(logic.isEmailTestMode());
	}

	@Test
	public void getUploadDirectory() {
		String tmp = System.getProperty("java.io.tmpdir");
		// trim ending / to match what is done in ConfigLogicImpl
		if (tmp.endsWith("/")) {
			tmp = tmp.substring(0, tmp.length() - 1);
		}

		String newDir = tmp + "/configLogicImplTest";
		File f = new File(newDir);
		if (!f.exists()) {
			if (f.mkdir()) {
				f.deleteOnExit();
			} else {
				log.error("Unable to create test directory.");
			}
		}

		assertEquals(tmp, logic.getUploadDirectory());

		when(configService.getString(ConfigLogic.UPLOAD_DIRECTORY_PROP))
				.thenReturn("/missing/dir").thenReturn(newDir);
		assertEquals(tmp, logic.getUploadDirectory());
		assertEquals(newDir, logic.getUploadDirectory());
	}

	@Test
	public void saveConfigNoPrefix() {
		ConfigEntry ce = logic.getConfig();

		Properties props = new Properties();
		when(toolManager.getCurrentPlacement().getPlacementConfig()).thenReturn(props);

		assertEquals(ConfigLogic.CONFIG_SAVED, logic.saveConfig(ce));
		assertEquals(props.getProperty(ConfigParams.displayinvalidemailaddrs.name()),
				Boolean.toString(ce.isDisplayInvalidEmails()));
		assertEquals(props.getProperty(ConfigParams.emailarchive.name()),
				Boolean.toString(ce.isAddToArchive()));
		assertEquals(props.getProperty(ConfigParams.replyto.name()),
				StringUtils.trimToEmpty(ce.getReplyTo()));
		assertEquals(props.getProperty(ConfigParams.sendmecopy.name()),
				Boolean.toString(ce.isSendMeACopy()));
		assertEquals(props.getProperty(ConfigParams.displayemptygroups.name()),
				Boolean.toString(ce.isDisplayEmptyGroups()));
		assertNull(props.getProperty(ConfigParams.subjectprefix.name()));

		verify(toolManager.getCurrentPlacement()).save();
	}
	
	@Test
	public void saveConfig() {
		ConfigEntry ce = logic.getConfig();

		Properties props = new Properties();
		when(toolManager.getCurrentPlacement().getPlacementConfig()).thenReturn(props);
		when(configService.getBoolean(ConfigLogic.ALLOW_PREFIX_CHANGE_PROP, false)).thenReturn(true);

		assertEquals(ConfigLogic.CONFIG_SAVED, logic.saveConfig(ce));
		assertEquals(props.getProperty(ConfigParams.displayinvalidemailaddrs.name()),
				Boolean.toString(ce.isDisplayInvalidEmails()));
		assertEquals(props.getProperty(ConfigParams.emailarchive.name()),
				Boolean.toString(ce.isAddToArchive()));
		assertEquals(props.getProperty(ConfigParams.replyto.name()),
				StringUtils.trimToEmpty(ce.getReplyTo()));
		assertEquals(props.getProperty(ConfigParams.sendmecopy.name()),
				Boolean.toString(ce.isSendMeACopy()));
		assertEquals(props.getProperty(ConfigParams.displayemptygroups.name()),
				Boolean.toString(ce.isDisplayEmptyGroups()));
		assertEquals(props.getProperty(ConfigParams.subjectprefix.name()), "");

		verify(toolManager.getCurrentPlacement()).save();
	}

	@Test
	public void saveConfigCustomPrefix() {
		ConfigEntry ce = logic.getConfig();
		ce.setSubjectPrefixType(SubjectPrefixType.custom.name());
		ce.setSubjectPrefix("MRMR");

		Properties props = new Properties();
		when(toolManager.getCurrentPlacement().getPlacementConfig()).thenReturn(props);
		when(configService.getBoolean(ConfigLogic.ALLOW_PREFIX_CHANGE_PROP, false)).thenReturn(true);
	
		assertEquals(ConfigLogic.CONFIG_SAVED, logic.saveConfig(ce));
		assertEquals(props.getProperty(ConfigParams.displayinvalidemailaddrs.name()),
				Boolean.toString(ce.isDisplayInvalidEmails()));
		assertEquals(props.getProperty(ConfigParams.emailarchive.name()),
				Boolean.toString(ce.isAddToArchive()));
		assertEquals(props.getProperty(ConfigParams.replyto.name()),
				StringUtils.trimToEmpty(ce.getReplyTo()));
		assertEquals(props.getProperty(ConfigParams.sendmecopy.name()),
				Boolean.toString(ce.isSendMeACopy()));
		assertEquals(props.getProperty(ConfigParams.displayemptygroups.name()),
				Boolean.toString(ce.isDisplayEmptyGroups()));
		assertEquals(props.getProperty(ConfigParams.subjectprefix.name()), "MRMR");
	
		verify(toolManager.getCurrentPlacement()).save();
	}
}
