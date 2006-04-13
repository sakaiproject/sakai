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
package org.radeox.test;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.radeox.EngineManager;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.test.filter.mock.MockWikiRenderEngine;

public class BaseRenderEngineTest extends TestCase
{
	RenderContext context;

	public BaseRenderEngineTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		context = new BaseRenderContext();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(BaseRenderEngineTest.class);
	}

	public void testBoldInList()
	{
		RenderEngine engine = EngineManager.getInstance();
		assertEquals(
				"<ul class=\"minus\">\n<li><b class=\"bold\">test</b></li>\n</ul>",
				engine.render("- __test__", context));
	}

	public void testRenderEngine()
	{
		String result = EngineManager.getInstance().render(
				"__SnipSnap__ {link:Radeox|http://radeox.org}", context);
		assertEquals(
				"<b class=\"bold\">SnipSnap</b> <span class=\"nobr\"><a href=\"http://radeox.org\">Radeox</a></span>",
				result);
	}

	public void testEmpty()
	{
		String result = EngineManager.getInstance().render("", context);
		assertEquals("", result);
	}

	public void testDefaultEngine()
	{
		RenderEngine engine = EngineManager.getInstance();
		RenderEngine engineDefault = EngineManager
				.getInstance(EngineManager.DEFAULT);
		assertEquals(engine.getName(), engineDefault.getName());
	}

	public void testWriter()
	{
		RenderEngine engine = new BaseRenderEngine();
		StringWriter writer = new StringWriter();
		try
		{
			engine.render(writer, "__SnipSnap__", context);
		}
		catch (IOException e)
		{
			// never reach
		}
		assertEquals("BaseRenderEngine writes to Writer",
				"<b class=\"bold\">SnipSnap</b>", writer.toString());
	}

	public void testFilterOrder()
	{
		RenderEngine engine = EngineManager.getInstance();
		context.setRenderEngine(new MockWikiRenderEngine());
		assertEquals("'<link>' - '&#60;link&#62;'", engine.render("[<link>]",
				context));
	}
}
