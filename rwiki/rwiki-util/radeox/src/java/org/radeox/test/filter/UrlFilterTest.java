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

import org.radeox.filter.UrlFilter;

public class UrlFilterTest extends FilterTestSupport
{
	public UrlFilterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new UrlFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(UrlFilterTest.class);
	}

	public void testHttp()
	{
		assertEquals(
				"<span class=\"nobr\"><a href=\"http://radeox.org\">&#104;ttp://radeox.org</a></span>",
				filter.filter("http://radeox.org", context));
	}

	public void testHttps()
	{
		assertEquals(
				"<span class=\"nobr\"><a href=\"https://radeox.org\">&#104;ttps://radeox.org</a></span>",
				filter.filter("https://radeox.org", context));
	}

	public void testFtp()
	{
		assertEquals(
				"<span class=\"nobr\"><a href=\"ftp://radeox.org\">&#102;tp://radeox.org</a></span>",
				filter.filter("ftp://radeox.org", context));
	}
}
