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

import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.filter.EscapeFilter;
import org.radeox.filter.Filter;
import org.radeox.filter.FilterPipe;
import org.radeox.filter.context.FilterContext;
import org.radeox.util.Encoder;

public class EscapeFilterTest extends FilterTestSupport
{
	public EscapeFilterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new EscapeFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(EscapeFilterTest.class);
	}

	public void testEscapeH()
	{
		assertEquals("h is escaped", "&#104;", filter.filter("\\h", context));
	}

	public void testBackslash()
	{
		// test "\\"
		assertEquals("\\\\ is kept escaped", "\\\\", filter.filter("\\\\",
				context));
	}

	public void testBeforeEscape()
	{
		FilterPipe fp = new FilterPipe();
		Filter f = new Filter()
		{
			public String[] replaces()
			{
				return new String[0];
			}

			public void setInitialContext(InitialRenderContext context)
			{
			}

			public String[] before()
			{
				return FilterPipe.EMPTY_BEFORE;
			}

			public String filter(String input, FilterContext context)
			{
				return null;
			}

			public String getDescription()
			{
				return "";
			}
		};

		fp.addFilter(f);
		fp.addFilter(filter);
		assertEquals("EscapeFilter is first", fp.getFilter(0), filter);
	}

	public void testHTMLEncoderEscape()
	{
		assertEquals("&#60;link&#62;", Encoder.escape("<link>"));
	}

	public void testHTMLEncoderUnescape()
	{
		assertEquals("<link>", Encoder.unescape("&#60;link&#62;"));
	}

	public void testAmpersandEscape()
	{
		assertEquals("&#38;", filter.filter("&", context));
	}

}
