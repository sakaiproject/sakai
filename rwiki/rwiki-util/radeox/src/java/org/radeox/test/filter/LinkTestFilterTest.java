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
package org.radeox.test.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.api.engine.context.RenderContext;
import org.radeox.filter.LinkTestFilter;
import org.radeox.test.filter.mock.MockWikiRenderEngine;

public class LinkTestFilterTest extends FilterTestSupport
{
	public LinkTestFilterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new LinkTestFilter();
		context.getRenderContext().setRenderEngine(new MockWikiRenderEngine());
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(LinkTestFilterTest.class);
	}

	public void testUrlInLink()
	{
		assertEquals("Url is reported",
				"<div class=\"error\">Do not surround URLs with [...].</div>",
				filter.filter("[http://radeox.org]", context));
	}

	public void testCreate()
	{
		assertEquals("'Roller' - 'Roller'", filter.filter("[Roller]", context));
	}

	public void testLink()
	{
		assertEquals("link:SnipSnap|SnipSnap", filter.filter("[SnipSnap]",
				context));
	}

	public void testLinkLower()
	{
		assertEquals("link:stephan|stephan", filter
				.filter("[stephan]", context));
	}

	public void testLinkAlias()
	{
		assertEquals("link:stephan|alias", filter.filter("[alias|stephan]",
				context));
	}

	public void testLinkAliasAnchor()
	{
		assertEquals("link:stephan|alias#hash", filter.filter(
				"[alias|stephan#hash]", context));
	}

	public void testLinkAliasAnchorType()
	{
		assertEquals("link:stephan|alias#hash", filter.filter(
				"[alias|type:stephan#hash]", context));
	}

	public void testLinksWithEscapedChars()
	{
		assertEquals("'<link>' - '&#60;link&#62;'", filter.filter("[<link>]",
				context));
	}
}
