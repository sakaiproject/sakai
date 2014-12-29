/**********************************************************************************
 * Copyright 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.model;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author chall
 *
 */
public class EmailEntryTest {
	EmailEntry entry;

	@Mock
	ConfigEntry config;

	@Before
	public void setUp() {
		entry = new EmailEntry(config);
	}

	@Test
	public void getDefaults() {
		assertSame(config, entry.getConfig());
		assertTrue(entry.getAttachments().isEmpty());
		assertEquals("", entry.getContent());
		assertEquals(null, entry.getFrom());
		assertTrue(entry.getGroupIds().isEmpty());
		assertTrue(entry.getOtherRecipients().isEmpty());
		assertTrue(entry.getRoleIds().isEmpty());
		assertTrue(entry.getSectionIds().isEmpty());
		assertEquals("", entry.getSubject());
		assertTrue(entry.getUserIds().isEmpty());
	}

	@Test
	public void setConfig() {
		ConfigEntry newConfig = Mockito.mock(ConfigEntry.class);
		entry.setConfig(newConfig);
		assertSame(newConfig, entry.getConfig());
		assertNotSame(config, entry.getConfig());
	}

	@Test
	public void setAttachments() {
		ArrayList<String> attachments = new ArrayList<String>();
		attachments.add("attachment1");
		attachments.add("attachment2");
		entry.setAttachments(attachments);
		assertFalse(entry.getAttachments().isEmpty());
		assertEquals(2, entry.getAttachments().size());
		assertEquals("attachment1", entry.getAttachments().get(0));
		assertEquals("attachment2", entry.getAttachments().get(1));
	}

	@Test
	public void setContent() {
		entry.setContent("new content");
		assertEquals("new content", entry.getContent());

		entry.setContent(null);
		assertEquals("", entry.getContent());

		entry.setContent("");
		assertEquals("", entry.getContent());
	}

	@Test
	public void setFrom() {
		entry.setFrom("nobody@example.com");
		assertEquals("nobody@example.com", entry.getFrom());
	}

	@Test
	public void setGroupIds() {
		HashMap<String, String> groups = new HashMap<String, String>();
		groups.put("group1", "id1");
		groups.put("group2", "id2");
		entry.setGroupIds(groups);
		assertFalse(entry.getGroupIds().isEmpty());
		assertEquals(2, entry.getGroupIds().size());
		assertEquals("id1", entry.getGroupIds().get("group1"));
		assertEquals("id2", entry.getGroupIds().get("group2"));
	}

	@Test
	public void setOtherRecipients() {
		String rcpts = "nobody@example.com;someone@example.com,whatever@example.com";

		entry.setOtherRecipients(rcpts);
		assertFalse(entry.getOtherRecipients().isEmpty());
		assertEquals(3, entry.getOtherRecipients().size());
		assertEquals("nobody@example.com", entry.getOtherRecipients().get(0));
		assertEquals("someone@example.com", entry.getOtherRecipients().get(1));
		assertEquals("whatever@example.com", entry.getOtherRecipients().get(2));

		entry.setOtherRecipients("");

	}

	@Test
	public void setOtherRecipientsList() {
		List<String> otherRecipients = new ArrayList<String>();
		otherRecipients.add("nobody@example.com");
		otherRecipients.add("someone@example.com");
		otherRecipients.add("whatever@example.com");

		entry.setOtherRecipients(otherRecipients);
		assertFalse(entry.getOtherRecipients().isEmpty());
		assertEquals(3, entry.getOtherRecipients().size());
		assertEquals("nobody@example.com", entry.getOtherRecipients().get(0));
		assertEquals("someone@example.com", entry.getOtherRecipients().get(1));
		assertEquals("whatever@example.com", entry.getOtherRecipients().get(2));
	}

	@Test
	public void setRoleIds() {
		HashMap<String, String> roles = new HashMap<String, String>();
		roles.put("role1", "id1");
		roles.put("role2", "id2");
		entry.setRoleIds(roles);
		assertFalse(entry.getRoleIds().isEmpty());
		assertEquals(2, entry.getRoleIds().size());
		assertEquals("id1", entry.getRoleIds().get("role1"));
		assertEquals("id2", entry.getRoleIds().get("role2"));
	}

	@Test
	public void setSectionIds() {
		HashMap<String, String> sections = new HashMap<String, String>();
		sections.put("section1", "id1");
		sections.put("section2", "id2");
		entry.setSectionIds(sections);
		assertFalse(entry.getSectionIds().isEmpty());
		assertEquals(2, entry.getSectionIds().size());
		assertEquals("id1", entry.getSectionIds().get("section1"));
		assertEquals("id2", entry.getSectionIds().get("section2"));
	}

	@Test
	public void setSubject() {
		entry.setSubject("new subject");
		assertEquals("new subject", entry.getSubject());

		entry.setSubject(null);
		assertEquals("", entry.getSubject());

		entry.setSubject("");
		assertEquals("", entry.getSubject());
	}

	@Test
	public void setUserIds() {
		HashMap<String, String> users = new HashMap<String, String>();
		users.put("user1", "id1");
		users.put("user2", "id2");
		entry.setUserIds(users);
		assertFalse(entry.getUserIds().isEmpty());
		assertEquals(2, entry.getUserIds().size());
		assertEquals("id1", entry.getUserIds().get("user1"));
		assertEquals("id2", entry.getUserIds().get("user2"));
	}
}
