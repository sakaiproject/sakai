/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.util;

import java.io.IOException;
import java.io.Writer;

/**
 * The same as StringWriter, but takes an existing StringBuffer in its
 * constructor.
 * 
 * @author Stephan J. Schmidt
 * @version $Id: StringBufferWriter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class StringBufferWriter extends Writer
{

	private StringBuffer buffer;

	private boolean closed = false;

	public StringBufferWriter(StringBuffer buffer)
	{
		this.buffer = buffer;
		this.lock = buffer;
	}

	public StringBufferWriter()
	{
		this.buffer = new StringBuffer();
		this.lock = buffer;
	}

	public StringBufferWriter(int initialSize)
	{
		if (initialSize < 0)
		{
			throw new IllegalArgumentException("Negative buffer size");
		}
		buffer = new StringBuffer(initialSize);
		lock = buffer;
	}

	public void write(int c)
	{
		buffer.append((char) c);
	}

	public void write(char cbuf[], int off, int len)
	{
		if ((off < 0) || (off > cbuf.length) || (len < 0)
				|| ((off + len) > cbuf.length) || ((off + len) < 0))
		{
			throw new IndexOutOfBoundsException();
		}
		else if (len == 0)
		{
			return;
		}
		buffer.append(cbuf, off, len);
	}

	public void write(String str)
	{
		buffer.append(str);
	}

	public void write(String str, int off, int len)
	{
		buffer.append(str.substring(off, off + len));
	}

	public String toString()
	{
		return buffer.toString();
	}

	public StringBuffer getBuffer()
	{
		return buffer;
	}

	public void flush()
	{
	}

	public void close() throws IOException
	{
		closed = true;
	}

}
