/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.content.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Matthew Buckett
 */
@RunWith(MockitoJUnitRunner.class)
public class WrappedContentResourceTest {

	@Mock
	private ContentResource resource;

	@Test
	public void testWrapping() throws Exception {
		// Check the header and footer get correctly appended.
		when(resource.streamContent()).then(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				return new ByteArrayInputStream("body".getBytes());
			}
		});
		when(resource.getContentLength()).thenReturn(Long.valueOf("body".length()));
		WrappedContentResource wrapped = new WrappedContentResource(resource, "header", "footer", false);
		String expected = "headerbodyfooter";
		assertEquals(expected, new String(wrapped.getContent()));
		assertEquals(expected.length(), wrapped.getContentLength());
		byte[] source = new byte[1024];
		byte[] output = new byte[1024];
		System.arraycopy(expected.getBytes(), 0, source, 0, expected.length());
		assertEquals(expected.length(), wrapped.streamContent().read(output));
		assertArrayEquals(source, output);
	}

	@Test(expected = ServerOverloadException.class)
	public void testWrappingContentException() throws Exception {
		when(resource.streamContent()).thenThrow(ServerOverloadException.class);
		WrappedContentResource wrapped = new WrappedContentResource(resource, "header", "footer", false);
		wrapped.getContent();
	}

	@Test(expected = ServerOverloadException.class)
	public void testWrappingStreamException() throws Exception {
		when(resource.streamContent()).thenThrow(ServerOverloadException.class);
		WrappedContentResource wrapped = new WrappedContentResource(resource, "header", "footer", false);
		wrapped.streamContent();
	}

	@Test
	public void testWrappingLengthException() throws Exception {
		// We just get 0 as you can't throw an exception from getContentLength and when you
		// attempt to get the body you will get an exception then.
		when(resource.streamContent()).thenThrow(ServerOverloadException.class);
		WrappedContentResource wrapped = new WrappedContentResource(resource, "header", "footer", false);
		assertEquals(0, wrapped.getContentLength());
	}


}
