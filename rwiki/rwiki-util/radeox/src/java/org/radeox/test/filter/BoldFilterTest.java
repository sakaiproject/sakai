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

import org.radeox.filter.BoldFilter;

public class BoldFilterTest extends FilterTestSupport
{
	public BoldFilterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		filter = new BoldFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(BoldFilterTest.class);
	}

	public void testBold()
	{
		assertEquals("<b class=\"bold\">Text</b>", filter.filter("__Text__",
				context));
	}

	public void testBoldMustStartAndEndWithSpace()
	{
		assertEquals("Test__Text__Test", filter.filter("Test__Text__Test",
				context));
	}

	public void testBoldWithPunctuation()
	{
		assertEquals("<b class=\"bold\">Text</b>:", filter.filter("__Text__:",
				context));
	}
}
