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

package org.radeox.macro.xref;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;

/**
 * Stores information and links to xref Java source code e.g.
 * http://nanning.sourceforge.net/xref/com/tirsen/nanning/MixinInstance.html#83
 * 
 * @author Stephan J. Schmidt
 * @version $Id$
 */
@Slf4j
public class XrefMapper
{

	private final static String FILENAME = "conf/xref.txt";

	private static XrefMapper instance;

	private Map xrefMap;

	public static synchronized XrefMapper getInstance()
	{
		if (null == instance)
		{
			instance = new XrefMapper();
		}
		return instance;
	}

	public XrefMapper()
	{
		xrefMap = new HashMap();

		boolean fileNotFound = false;
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(FILENAME)));
			addXref(br);
		}
		catch (IOException e)
		{
			log.warn("Unable to read " + FILENAME);
			fileNotFound = true;
		}

		if (fileNotFound)
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new InputStreamReader(XrefMapper.class
						.getResourceAsStream("/" + FILENAME)));
				addXref(br);
			}
			catch (Exception e)
			{
				log.warn("Unable to read " + FILENAME);
			}
		}
	}

	public void addXref(BufferedReader reader) throws IOException
	{
		String line;
		while ((line = reader.readLine()) != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(line, " ");
			String site = tokenizer.nextToken();
			String baseUrl = tokenizer.nextToken();
			xrefMap.put(site.toLowerCase(), baseUrl);
		}
	}

	public boolean contains(String external)
	{
		return xrefMap.containsKey(external);
	}

	public Writer expand(Writer writer, String className, String site,
			int lineNumber) throws IOException
	{
		site = site.toLowerCase();
		if (xrefMap.containsKey(site))
		{
			writer.write("<a href=\"");
			writer.write((String) xrefMap.get(site));
			writer.write("/");
			writer.write(className.replace('.', '/'));
			writer.write(".html");
			if (lineNumber > 0)
			{
				writer.write("#");
				writer.write("" + lineNumber);
			}
			writer.write("\">");
			writer.write(className);
			writer.write("</a>");
		}
		else
		{
			log.debug("Xrefs : " + xrefMap);
			log.warn(site + " not found");
		}
		return writer;
	}

	public Writer appendTo(Writer writer) throws IOException
	{
		writer.write("{table}\n");
		writer.write("Binding|Site\n");
		Iterator iterator = xrefMap.entrySet().iterator();
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

}
