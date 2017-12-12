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

package org.radeox.filter.interwiki;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.radeox.util.Encoder;

/**
 * Stores information and links to other wikis forming a InterWiki
 * 
 * @author Stephan J. Schmidt
 * @version $Id$
 */
@Slf4j
public class InterWiki
{
	private static InterWiki instance;

	private Map interWiki;

	public static synchronized InterWiki getInstance()
	{
		if (null == instance)
		{
			instance = new InterWiki();
		}
		return instance;
	}

	public InterWiki()
	{
		interWiki = new HashMap();
		interWiki.put("LCOM", "http://www.langreiter.com/space/");
		interWiki.put("ESA", "http://earl.strain.at/space/");
		interWiki.put("C2", "http://www.c2.com/cgi/wiki?");
		interWiki
				.put("WeblogKitchen", "http://www.weblogkitchen.com/wiki.cgi?");
		interWiki.put("Meatball", "http://www.usemod.com/cgi-bin/mb.pl?");
		interWiki.put("SnipSnap", "http://snipsnap.org/space/");

		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("conf/intermap.txt")));
			addInterMap(br);
		}
		catch (IOException e)
		{
			log.warn("Unable to read conf/intermap.txt");
		}
	}

	public void addInterMap(BufferedReader reader) throws IOException
	{
		String line;
		while ((line = reader.readLine()) != null)
		{
			int index = line.indexOf(" ");
			interWiki.put(line.substring(0, index), Encoder.escape(line
					.substring(index + 1)));
		};
	}

	public Writer appendTo(Writer writer) throws IOException
	{
		Iterator iterator = interWiki.entrySet().iterator();
		writer.write("{table}\n");
		writer.write("Wiki|Url\n");
		while (iterator.hasNext())
		{
			Map.Entry entry = (Map.Entry) iterator.next();
			writer.write((String) entry.getKey());
			writer.write("|");
			writer.write((String) entry.getValue());
			writer.write("\n");
		}
		writer.write("{table}");
		return writer;
	}

	public boolean contains(String external)
	{
		return interWiki.containsKey(external);
	}

	public String getWikiUrl(String wiki, String name)
	{
		return ((String) interWiki.get(wiki)) + name;
	}

	public Writer expand(Writer writer, String wiki, String name, String view,
			String anchor) throws IOException
	{
		writer.write("<a href=\"");
		writer.write((String) interWiki.get(wiki));
		writer.write(name);
		if (!"".equals(anchor))
		{
			writer.write("#");
			writer.write(anchor);
		}
		writer.write("\">");
		writer.write(view);
		writer.write("</a>");
		return writer;
	}

	public Writer expand(Writer writer, String wiki, String name, String view)
			throws IOException
	{
		return expand(writer, wiki, name, view, "");
	}
}
