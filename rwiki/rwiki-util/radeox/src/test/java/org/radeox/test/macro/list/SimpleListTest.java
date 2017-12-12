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
package org.radeox.test.macro.list;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.macro.list.SimpleList;
import org.radeox.util.Linkable;
import org.radeox.util.Nameable;

@Slf4j
public class SimpleListTest extends ListFormatterSupport
{
	public SimpleListTest(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(SimpleListTest.class);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		formatter = new SimpleList();
	}

	public void testNameable()
	{
		Collection c = Arrays.asList(new Nameable[] { new Nameable()
		{
			public String getName()
			{
				return "name:test";
			}
		} });
		try
		{
			formatter.format(writer, emptyLinkable, "", c, "", false);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}

		assertEquals(
				"Nameable is rendered",
				"<div class=\"list\"><div class=\"list-title\"></div><blockquote>name:test</blockquote></div>",
				writer.toString());
	}

	public void testLinkable()
	{
		Collection c = Arrays.asList(new Linkable[] { new Linkable()
		{
			public String getLink()
			{
				return "link:test";
			}
		} });
		try
		{
			formatter.format(writer, emptyLinkable, "", c, "", false);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}

		assertEquals(
				"Linkable is rendered",
				"<div class=\"list\"><div class=\"list-title\"></div><blockquote>link:test</blockquote></div>",
				writer.toString());
	}

	public void testSingeItem()
	{
		Collection c = Arrays.asList(new String[] { "test" });
		try
		{
			formatter.format(writer, emptyLinkable, "", c, "", false);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		assertEquals(
				"Single item is rendered",
				"<div class=\"list\"><div class=\"list-title\"></div><blockquote>test</blockquote></div>",
				writer.toString());
	}

	public void testSize()
	{
		Collection c = Arrays.asList(new String[] { "test" });
		try
		{
			formatter.format(writer, emptyLinkable, "", c, "", true);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		assertEquals(
				"Size is rendered",
				"<div class=\"list\"><div class=\"list-title\"> (1)</div><blockquote>test</blockquote></div>",
				writer.toString());
	}

	public void testEmpty()
	{
		Collection c = Arrays.asList(new String[] {});
		try
		{
			formatter.format(writer, emptyLinkable, "", c, "No items", false);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		assertEquals(
				"Empty list is rendered",
				"<div class=\"list\"><div class=\"list-title\"></div>No items</div>",
				writer.toString());
	}

	public void testTwoItems()
	{
		Collection c = Arrays.asList(new String[] { "test1", "test2" });
		try
		{
			formatter.format(writer, emptyLinkable, "", c, "", false);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		assertEquals(
				"Two items are rendered",
				"<div class=\"list\"><div class=\"list-title\"></div><blockquote>test1, test2</blockquote></div>",
				writer.toString());
	}

}
