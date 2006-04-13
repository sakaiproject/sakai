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

import org.radeox.filter.HtmlRemoveFilter;

public class HtmlRemoveFilterTest extends FilterTestSupport
{
	public HtmlRemoveFilterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new HtmlRemoveFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(HtmlRemoveFilterTest.class);
	}

	public void testHtmlRemove()
	{
		assertEquals("Test", filter.filter("<tag attr=\"Text\">Test</tag>",
				context));
	}
}
