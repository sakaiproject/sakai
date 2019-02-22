/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.content.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.Getter;

public class ZipWriter
{
	public static class Entry
	{
		@Getter String name;
		@Getter InputStream contentStream;

		public Entry(String name, InputStream contentStream)
		{
			this.name = name;
			this.contentStream = contentStream;
		}
	}

	private ZipInputStream inStream;
	private ZipOutputStream outStream;
	private Set<String> removeSet = new HashSet<>();
	private List<Entry> addList = new LinkedList<>();

	public ZipWriter(InputStream contentStream, OutputStream resultStream)
	{
		this.inStream = new ZipInputStream(contentStream);
		this.outStream = new ZipOutputStream(resultStream);
	}

	public void add(String name, InputStream entryStream)
	{
		addList.add(new Entry(name, entryStream));
	}

	private void addEntry(String entryName, InputStream contentStream) throws IOException
	{
		outStream.setMethod(ZipOutputStream.DEFLATED);
		outStream.putNextEntry(new ZipEntry(entryName));
		int len;
		byte[] buf = new byte[1024];

		while ((len = contentStream.read(buf)) > 0)
		{
			outStream.write(buf, 0, len);
		}
	}

	private void addEntry(ZipEntry entry, InputStream contentStream) throws IOException
	{
		try
		{
			outStream.setMethod(ZipOutputStream.STORED);
		}
		catch (IllegalArgumentException e)
		{
			throw new IOException("IllegalArgumentException in " + this.getClass().getName() + ".addEntry() for " + entry.toString());
		}

		outStream.putNextEntry(entry);
		int len;
		byte[] buf = new byte[1024];

		ZipInputStream zipContentStream = null;

		if (contentStream instanceof ZipInputStream)
		{
			zipContentStream = (ZipInputStream) contentStream;
		}
		else
		{
			zipContentStream = new ZipInputStream(contentStream);
		}

		while ((len = zipContentStream.read(buf)) > 0)
		{
			outStream.write(buf, 0, len);
		}
	}

	public void process() throws IOException
	{
		ZipEntry entry;
		entry = inStream.getNextEntry();
		while (entry != null)
		{
			if (!removeSet.contains(entry.getName()))
			{
				addEntry(new ZipEntry(entry), inStream);
			}

			entry = inStream.getNextEntry();
		}

		for (Entry e : addList)
		{
			addEntry(e.getName(), e.getContentStream());
			if (e.getContentStream() != null)
			{
				e.getContentStream().close();
			}
		}

		outStream.close();
	}

	public void remove(String name)
	{
		removeSet.add(name);
	}

	public void replace(String name, InputStream entryStream)
	{
		add(name, entryStream);
		remove(name);
	}
}
