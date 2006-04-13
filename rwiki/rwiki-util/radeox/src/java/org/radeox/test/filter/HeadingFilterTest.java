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

import org.radeox.filter.HeadingFilter;

public class HeadingFilterTest extends FilterTestSupport
{
	public HeadingFilterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new HeadingFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(HeadingFilterTest.class);
	}

	public void testHeading()
	{
		assertEquals("<h3 class=\"heading-1\">Test</h3>", filter.filter(
				"1 Test", context));
	}

	public void testSubHeadings()
	{
		assertEquals("<h3 class=\"heading-1\">Test</h3>\n"
				+ "<h3 class=\"heading-1-1\">Test</h3>\n"
				+ "<h3 class=\"heading-1-1-1\">Test</h3>\n"
				+ "<h3 class=\"heading-1\">Test</h3>", filter.filter(
				"1 Test\n1.1 Test\n1.1.1 Test\n1 Test", context));
	}
}
