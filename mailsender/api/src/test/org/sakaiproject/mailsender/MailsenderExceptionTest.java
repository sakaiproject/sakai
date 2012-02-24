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
package org.sakaiproject.mailsender;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * General exception for Mailsender. This exception can store messages to be
 * sent up to the UI for display to the user. This is terrible design but
 * somewhat necessary until the updated Email API is more widely in place.
 */
@RunWith(value = MockitoJUnitRunner.class)
public class MailsenderExceptionTest {
	MailsenderException ex;

	@Before
	public void setUp() {
		ex = new MailsenderException();
	}

	@Test
	public void constructorMessages() {
		ArrayList<Map<String, Object[]>> messages = new ArrayList<Map<String,Object[]>>();
		ex = new MailsenderException(messages);
		assertFalse(ex.hasMessages());

		HashMap<String, Object[]> message = new HashMap<String, Object[]>();
		message.put("code.123", new Object[] {"value.123"});
		messages.add(message);
		assertTrue(ex.hasMessages());
	}

	@Test
	public void constructorCodeValue() {
		ex = new MailsenderException("code.123", "value.123");
	}

	@Test
	public void constructorMessageCause() {
		IOException ioe = new IOException("ioexception is the cause");
		ex = new MailsenderException("great message", ioe);
		assertEquals("great message", ex.getMessage());
		assertEquals(ioe, ex.getCause());
	}

	@Test
	public void addMessageCode() {
		ex.addMessage("code.123");
		assertTrue(ex.hasMessages());

		List<Map<String, Object[]>> messages = ex.getMessages();
		assertEquals(1, messages.size());

		for (Map<String, Object[]> message : messages) {
			assertTrue(message.containsKey("code.123"));

			Object[] vals = message.get("code.123");
			assertEquals(1, vals.length);
			assertEquals("", (String) vals[0]);
		}
	}

	@Test
	public void addMessageCodeValue() {
		ex.addMessage("code.123", "value.123");
		assertTrue(ex.hasMessages());

		List<Map<String, Object[]>> messages = ex.getMessages();
		assertEquals(1, messages.size());

		for (Map<String, Object[]> message : messages) {
			assertTrue(message.containsKey("code.123"));

			Object[] vals = message.get("code.123");
			assertEquals(1, vals.length);
			assertEquals("value.123", (String) vals[0]);
		}
	}

	@Test
	public void addMessageCodeValues() {
		ex.addMessage("code.123", new Object[] {"value.123", "value.456"});
		assertTrue(ex.hasMessages());

		List<Map<String, Object[]>> messages = ex.getMessages();
		assertEquals(1, messages.size());

		for (Map<String, Object[]> message : messages) {
			assertTrue(message.containsKey("code.123"));

			Object[] vals = message.get("code.123");
			assertEquals(2, vals.length);
			assertEquals("value.123", (String) vals[0]);
			assertEquals("value.456", (String) vals[1]);
		}
	}

	@Test
	public void addMessagesCodeValue() {
		ex.addMessage("code.123", "value.123");
		ex.addMessage("code.789", "value.012");
		assertTrue(ex.hasMessages());

		List<Map<String, Object[]>> messages = ex.getMessages();
		assertEquals(2, messages.size());

		Map<String, Object[]> message = messages.get(0);
		assertTrue(message.containsKey("code.123"));

		Object[] vals = message.get("code.123");
		assertEquals(1, vals.length);
		assertEquals("value.123", (String) vals[0]);

		message = messages.get(1);
		assertTrue(message.containsKey("code.789"));

		vals = message.get("code.789");
		assertEquals(1, vals.length);
		assertEquals("value.012", (String) vals[0]);
	}

}
