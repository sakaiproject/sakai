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

import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * A simple output stream that just adds a header and footer to the output.
 * @author buckett
 *
 */
public class WrappedServletOutputStream extends ServletOutputStream {

	protected ServletOutputStream wrapped;
	private String footer;
	private String header;
	private boolean needsHeader = true;

	public WrappedServletOutputStream(String header, String footer, ServletOutputStream wrapped) {
		super();
		this.wrapped = wrapped;
		this.header = header;
		this.footer = footer;
	}
	
	public void close() throws IOException {
		try {
			wrapped.print(footer);
			wrapped.flush();
		} finally {
			wrapped.close();
		}
	}
	
	/**
	 * We don't write the header in the constructor as then we would need to throw an IOExcpetion.
	 * The compiler should make the cost of this near zero. 
	 * @throws IOException
	 */
	private void init() throws IOException {
		if (needsHeader) {
			needsHeader = false;
			wrapped.print(header);
		}
	}

	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}

	public void flush() throws IOException {
		wrapped.flush();
	}

	public int hashCode() {
		return wrapped.hashCode();
	}

	public void print(boolean b) throws IOException {
		init();
		wrapped.print(b);
	}

	public void print(char c) throws IOException {
		init();
		wrapped.print(c);
	}

	public void print(double d) throws IOException {
		init();
		wrapped.print(d);
	}

	public void print(float f) throws IOException {
		init();
		wrapped.print(f);
	}

	public void print(int i) throws IOException {
		init();
		wrapped.print(i);
	}

	public void print(long l) throws IOException {
		init();
		wrapped.print(l);
	}

	public void print(String s) throws IOException {
		init();
		wrapped.print(s);
	}

	public void println() throws IOException {
		init();
		wrapped.println();
	}

	public void println(boolean b) throws IOException {
		init();
		wrapped.println(b);
	}

	public void println(char c) throws IOException {
		init();
		wrapped.println(c);
	}

	public void println(double d) throws IOException {
		init();
		wrapped.println(d);
	}

	public void println(float f) throws IOException {
		init();
		wrapped.println(f);
	}

	public void println(int i) throws IOException {
		init();
		wrapped.println(i);
	}

	public void println(long l) throws IOException {
		init();
		wrapped.println(l);
	}

	public void println(String s) throws IOException {
		init();
		wrapped.println(s);
	}

	public String toString() {
		return wrapped.toString();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		init();
		wrapped.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		init();
		wrapped.write(b);
	}

	public void write(int b) throws IOException {
		init();
		wrapped.write(b);
	}

}
