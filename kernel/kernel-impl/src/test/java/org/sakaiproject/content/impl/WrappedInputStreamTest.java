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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthew Buckett
 */
public class WrappedInputStreamTest {

	private byte[] readAll(InputStream stream) throws IOException {
		byte[] output = new byte[0];
		byte[] buffer = new byte[1024];
		int read;
		while(( read = stream.read(buffer)) > 0) {
			byte[] tmp = new byte[output.length+read];
			System.arraycopy(output, 0, tmp, 0, output.length);
			System.arraycopy(buffer, 0, tmp, output.length, read);
			output = tmp;
		}
		return output;
	}

	@Test
	public void testNoDetect() throws IOException {
		InputStream stream = new WrappedInputStream(new ByteArrayInputStream("body".getBytes()), "header", "footer", false);
		byte[] output = readAll(stream);
		assertEquals("headerbodyfooter", new String(output));
	}

	@Test
	public void testDetectHTML() throws IOException {
		String htmlDoc = "<html><head><title>My Doc</title></head><body>The Body</body></html>";
		InputStream stream = new WrappedInputStream(new ByteArrayInputStream(htmlDoc.getBytes()), "header", "footer", true);
		byte[] output = readAll(stream);
		assertEquals(htmlDoc, new String(output));
	}

	@Test
	public void testDetectFragment() throws IOException {
		String header = "<html><head><title>My Doc</title></head><body>";
		String body = "The Body";
		String footer = "</body></html>";
		InputStream stream = new WrappedInputStream(new ByteArrayInputStream(body.getBytes()), header, footer, true);
		byte[] output = readAll(stream);
		assertEquals(header+body+footer, new String(output));
	}
}
