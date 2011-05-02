/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/content/api/ContentEntity.java $
 * $Id: ContentEntity.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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
package org.sakaiproject.content.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import junit.framework.TestCase;

public class CheckingOutputStreamTest extends TestCase {

	
	public void testSmall() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		ServletOutputStream servletStream = getServletOutputStream(array);
		CheckingOutputStream stream = new CheckingOutputStream("header ", " footer", servletStream);
		stream.print("hello");
		stream.close();
		assertEquals("header hello footer", new String(array.toByteArray()));
	}
	
	public void testEmpty() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		ServletOutputStream servletStream = getServletOutputStream(array);
		CheckingOutputStream stream = new CheckingOutputStream("header ", " footer", servletStream);
		stream.close();
		assertEquals("header  footer", new String(array.toByteArray()));
	}
	
	public void testFull() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		ServletOutputStream servletStream = getServletOutputStream(array);
		CheckingOutputStream stream = new CheckingOutputStream("header ", " footer", servletStream);
		for (int i = 0; i < 1000; i++) {
			stream.print('a');
			stream.print("a");
		}
		stream.close();
		String output = new String(array.toByteArray());
		assertTrue(output.startsWith("header aa"));
		assertTrue(output.endsWith("aaa footer"));
		assertTrue(output.contains("aaaaaaaaa"));		
	}
	
	public void testSmallHtml() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		ServletOutputStream servletStream = getServletOutputStream(array);
		CheckingOutputStream stream = new CheckingOutputStream("header ", " footer", servletStream);
		stream.print("<html><head><title>Hello</title></head></body>Hello</body></html>");
		stream.close();
		assertEquals("<html><head><title>Hello</title></head></body>Hello</body></html>", new String(array.toByteArray()));
	}
	
	public void testLength() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		ServletOutputStream servletStream = getServletOutputStream(array);
		CheckingOutputStream stream = new CheckingOutputStream("header ", " footer", servletStream);
		for (int i = 0; i < 1000; i++) {
			stream.print('a');
			stream.print("a");
		}
		stream.close();
		String output = new String(array.toByteArray());
		assertEquals(1000*2+"header ".length() + " footer".length(), output.length());	
	}
	
	private ServletOutputStream getServletOutputStream(final OutputStream array) {
		ServletOutputStream servletStream = new ServletOutputStream() {
			
			@Override
			public void write(int b) throws IOException {
					array.write(b);
				
			}
		};
		return servletStream;
	}
}
