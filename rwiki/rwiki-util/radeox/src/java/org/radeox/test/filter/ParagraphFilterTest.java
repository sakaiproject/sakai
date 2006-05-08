/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.radeox.test.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.filter.ParagraphFilter;

/**
 * @author ieb
 */
public class ParagraphFilterTest extends FilterTestSupport
{

	public ParagraphFilterTest(String s)
	{
		super(s);
		// TODO Auto-generated constructor stub
	}

	protected void setUp() throws Exception
	{
		filter = new ParagraphFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(ParagraphFilterTest.class);
	}

	public void testParagraph()
	{

		String result = filter.filter(
				"<h1>test</h1>TextA \n\n TextB\n <h2>Head2</h2>", context);
		System.err.println(":" + result + ":");
		assertEquals("<h1>test</h1><p class=\"paragraph\">TextA</p><p class=\"paragraph\"> TextB\n</p> <h2>Head2</h2>", result);
	}
	
	public void testNoChangeParagraph()
	{

		String result = filter.filter(
				"Some __Simple__ Content", context);
		System.err.println(":" + result + ":");
		assertEquals("Some __Simple__ Content", result);
	}
	public void testDoubleParagraph()
	{

		String result = filter.filter(
				"\n\nSome __Simple__ Content", context);
		System.err.println(":" + result + ":");
		assertEquals("<p class=\"paragraph\"></p><p class=\"paragraph\">Some __Simple__ Content</p>", result);
	}
	public void testSingleParagraph()
	{

		String result = filter.filter(
				"\nSome __Simple__ Content", context);
		System.err.println(":" + result + ":");
		assertEquals("\nSome __Simple__ Content", result);
	}
	public void testSingleEmbededParagraph()
	{

		String result = filter.filter(
				"\nSome\n\n__Simple__ Content", context);
		System.err.println(":" + result + ":");
		assertEquals("<p class=\"paragraph\">\nSome</p><p class=\"paragraph\">__Simple__ Content</p>", result);
	}

}
