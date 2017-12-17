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

package org.radeox.macro.book;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.radeox.Messages;
import org.radeox.util.Encoder;

/**
 * Manages links to keys, mapping is read from a text file
 * 
 * @author Stephan J. Schmidt
 * @version $Id: TextFileUrlMapper.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public abstract class TextFileUrlMapper implements UrlMapper
{
	private Map services;

	public abstract String getFileName();

	public abstract String getKeyName();

	public TextFileUrlMapper(Class klass)
	{
		services = new HashMap();

		boolean fileNotFound = false;
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(getFileName())));
			addMapping(br);
		}
		catch (IOException e)
		{
			log.warn("Unable to read " + getFileName()); //$NON-NLS-1$
			fileNotFound = true;
		}

		if (fileNotFound)
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new InputStreamReader(klass
						.getResourceAsStream("/" + getFileName()))); //$NON-NLS-1$
				addMapping(br);
			}
			catch (Exception e)
			{
				log.warn("Unable to read /" + getFileName() + " from jar"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public void addMapping(BufferedReader reader) throws IOException
	{
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (!line.startsWith("#")) //$NON-NLS-1$
			{
				int index = line.indexOf(" "); //$NON-NLS-1$
				services.put(line.substring(0, index), Encoder.escape(line
						.substring(index + 1)));
			}
		}
	}

	public Writer appendTo(Writer writer) throws IOException
	{
		Iterator iterator = services.entrySet().iterator();
		writer.write("{table}\n"); //$NON-NLS-1$
		writer.write(Messages.getString("TextFileUrlMapper.7")); //$NON-NLS-1$
		while (iterator.hasNext())
		{
			Map.Entry entry = (Map.Entry) iterator.next();
			writer.write((String) entry.getKey());
			writer.write("|"); //$NON-NLS-1$
			writer.write((String) entry.getValue());
			writer.write("\n"); //$NON-NLS-1$
		}
		writer.write("{table}"); //$NON-NLS-1$
		return writer;
	}

	public boolean contains(String external)
	{
		return services.containsKey(external);
	}

	public Writer appendUrl(Writer writer, String key) throws IOException
	{
		if (services.size() == 0)
		{
			writer.write(getKeyName());
			writer.write(":"); //$NON-NLS-1$
			writer.write(key);
		}
		else
		{
			// SnipLink.appendImage(writer, "external-link", "&gt;&gt;");
			writer.write("("); //$NON-NLS-1$
			Iterator iterator = services.entrySet().iterator();
			while (iterator.hasNext())
			{
				Map.Entry entry = (Map.Entry) iterator.next();
				writer.write("<a href=\""); //$NON-NLS-1$
				writer.write((String) entry.getValue());
				writer.write(key);
				writer.write("\">"); //$NON-NLS-1$
				writer.write((String) entry.getKey());
				writer.write("</a>"); //$NON-NLS-1$
				if (iterator.hasNext())
				{
					writer.write(" &#x7c; "); //$NON-NLS-1$
				}
			}
			writer.write(")"); //$NON-NLS-1$
		}
		return writer;
	}
}
