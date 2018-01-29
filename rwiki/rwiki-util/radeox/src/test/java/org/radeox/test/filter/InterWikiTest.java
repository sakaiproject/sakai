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

import java.io.IOException;
import java.io.StringWriter;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.filter.LinkTestFilter;
import org.radeox.filter.interwiki.InterWiki;
import org.radeox.test.filter.mock.MockInterWikiRenderEngine;

@Slf4j
public class InterWikiTest extends FilterTestSupport
{
	public InterWikiTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new LinkTestFilter();
		context.getRenderContext().setRenderEngine(
				(RenderEngine) new MockInterWikiRenderEngine());
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(InterWikiTest.class);
	}

	public void testAnchorInterWiki()
	{
		assertEquals(
				"<a href=\"http://www.c2.com/cgi/wiki?foo#anchor\">foo@C2</a>",
				filter.filter("[foo@C2#anchor]", context));
	}

	public void testInterWiki()
	{
		assertEquals(
				"<a href=\"http://snipsnap.org/space/stephan\">stephan@SnipSnap</a>",
				filter.filter("[stephan@SnipSnap]", context));
	}

	public void testGoogle()
	{
		assertEquals(
				"<a href=\"http://www.google.com/search?q=stephan\">stephan@Google</a>",
				filter.filter("[stephan@Google]", context));
	}

	public void testInterWikiAlias()
	{
		assertEquals(
				"<a href=\"http://snipsnap.org/space/AliasStephan\">Alias</a>",
				filter.filter("[Alias|AliasStephan@SnipSnap]", context));
	}

	public void testInterWikiExpander()
	{
		InterWiki interWiki = InterWiki.getInstance();
		StringWriter writer = new StringWriter();
		try
		{
			interWiki.expand(writer, "Google", "stephan", "StephanAlias");
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		assertEquals(
				"<a href=\"http://www.google.com/search?q=stephan\">StephanAlias</a>",
				writer.toString());
	}

}
