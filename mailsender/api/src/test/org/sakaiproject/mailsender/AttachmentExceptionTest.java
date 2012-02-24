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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(value = MockitoJUnitRunner.class)
public class AttachmentExceptionTest {

	@Test
	public void defaultConstructor() {
		AttachmentException ex = new AttachmentException();
		assertNull(ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	public void constructorMessage() {
		AttachmentException ex = new AttachmentException("great message");
		assertEquals("great message", ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	public void constructorCause() {
		IOException ioe = new IOException("ioexception is the cause");
		AttachmentException ex = new AttachmentException(ioe);
		assertNotNull(ex.getMessage());
		assertEquals(ioe, ex.getCause());
	}

	@Test
	public void constructorMessageCause() {
		IOException ioe = new IOException("ioexception is the cause");
		AttachmentException ex = new AttachmentException("great message", ioe);
		assertEquals("great message", ex.getMessage());
		assertEquals(ioe, ex.getCause());
	}
}
