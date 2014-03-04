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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Matthew Buckett
 */
public class ChainedInputStreamTest {

	@Test
	public void testSingleStreamRead() throws IOException {
		byte[] bytes = "hello world".getBytes();
		ChainedInputStream chained = new ChainedInputStream(new ByteArrayInputStream(bytes));
		for(int i = 0; i< bytes.length; i++) {
			assertEquals(bytes[i], chained.read());
		}
		assertEquals(0, chained.available());
	}

	@Test
	public void testSingleStreamReadBytes() throws IOException {
		byte[] bytes = "hello world".getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		ChainedInputStream chained = new ChainedInputStream(stream);
		byte[] read = new byte[bytes.length];
		assertEquals(bytes.length, chained.read(read));
		assertArrayEquals(bytes, read);
		assertEquals(0, chained.available());
		assertEquals(0, stream.available());
	}

	@Test
	public void testMultipleStreamRead() throws IOException {
		byte[] bytes = "hello world".getBytes();
		byte[] hello = "hello".getBytes();
		byte[] space = " ".getBytes();
		byte[] world = "world".getBytes();
		ChainedInputStream chained = new ChainedInputStream(
				new ByteArrayInputStream(hello),
				new ByteArrayInputStream(space),
				new ByteArrayInputStream(world)
		);
		for(int i = 0; i< bytes.length; i++) {
			assertEquals(bytes[i], chained.read());
		}
		assertEquals(0, chained.available());
	}


	@Test
	public void testMultipleStreamReadBytes() throws IOException {
		byte[] bytes = "hello world".getBytes();
		byte[] hello = "hello".getBytes();
		byte[] space = " ".getBytes();
		byte[] world = "world".getBytes();
		ChainedInputStream chained = new ChainedInputStream(
				new ByteArrayInputStream(hello),
				new ByteArrayInputStream(space),
				new ByteArrayInputStream(world)
		);
		byte[] read = new byte[bytes.length];
		assertEquals(bytes.length, chained.read(read));
		assertArrayEquals(bytes, read);
		assertEquals(0, chained.available());
	}

	@Test(expected = IOException.class)
	public void testReset() throws IOException {
		byte[] bytes = "hello world".getBytes();
		ChainedInputStream chained = new ChainedInputStream(new ByteArrayInputStream(bytes));
		chained.reset();
	}

	@Test
	public void testSkip() throws IOException {
		byte[] bytes = "hello world".getBytes();
		byte[] hello = "hello".getBytes();
		byte[] space = " ".getBytes();
		byte[] world = "world".getBytes();
		ChainedInputStream chained = new ChainedInputStream(
				new ByteArrayInputStream(hello),
				new ByteArrayInputStream(space),
				new ByteArrayInputStream(world)
		);
		assertEquals(5, chained.skip(5));
		assertEquals(4, chained.skip(4));
		assertEquals(2, chained.skip(Integer.MAX_VALUE));
	}
}
