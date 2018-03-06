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

import lombok.extern.slf4j.Slf4j;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.filter.ParagraphFilter;

/**
 * @author ieb
 */
@Slf4j
public class ParagraphFilterTest extends FilterTestSupport
{

	public ParagraphFilterTest(String s)
	{
		super(s);
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
		log.info(":" + result + ":");
		assertEquals("" +
	   "<p class=\"paragraph\"><h1>test</h1>TextA</p><p class=\"paragraph\"> TextB\n <h2>Head2</h2></p>", result);
	}
	public void testParagraph2()
	{

		String result = filter.filter(
				"<h1>test</h1>\n\n TextB\n <h2>Head2</h2>", context);
		log.info(":" + result + ":");
		assertEquals("<p class=\"paragraph\"><h1>test</h1></p><p class=\"paragraph\"> TextB\n <h2>Head2</h2></p>", result);
	}
	public void testParagraph3()
	{

		String result = filter.filter(
				"TextA \n\n TextB\n <h2>Head2</h2>", context);
		log.info(":" + result + ":");
		assertEquals("<p class=\"paragraph\">TextA</p><p class=\"paragraph\"> TextB\n <h2>Head2</h2></p>", result);
	}
	
	public void testNoChangeParagraph()
	{

		String result = filter.filter(
				"Some __Simple__ Content", context);
		log.info(":" + result + ":");
		assertEquals("Some __Simple__ Content", result);
	}
	public void testDoubleParagraph()
	{

		String result = filter.filter(
				"\n\nSome __Simple__ Content", context);
		log.info(":" + result + ":");
		assertEquals("<p class=\"paragraph\"></p><p class=\"paragraph\">Some __Simple__ Content</p>", result);
	}
	public void testSingleParagraph()
	{

		String result = filter.filter(
				"\nSome __Simple__ Content", context);
		log.info(":" + result + ":");
		assertEquals("\nSome __Simple__ Content", result);
	}
	public void testSingleEmbededParagraph()
	{

		String result = filter.filter(
				"\nSome\n\n__Simple__ Content", context);
		log.info(":" + result + ":");
		assertEquals("<p class=\"paragraph\">\nSome</p><p class=\"paragraph\">__Simple__ Content</p>", result);
	}
	
	public void testEmbededLinkParagraph() 
	{
		String result = filter.filter(
				"sdfdgdfgdd dfgdf gdfg dfgd fgdgf dfg <span class=\"nobr\">\n"
        + "<img src=\"/sakai-rwiki-tool/images/icklearrow.gif\" alt=\"external link: \" title=\"external link\"/> "
        + "<a href=\"link\">link</a></span> part of the same paragraph\n"
				+"\n"
				+"Annother paragraph\n"
				+"\n"
				+"Annother paragraph\n", context);
		log.info(":" + result + ":");
		assertEquals(
				"<p class=\"paragraph\">sdfdgdfgdd dfgdf gdfg dfgd fgdgf dfg <span class=\"nobr\">\n"
				+"<img src=\"/sakai-rwiki-tool/images/icklearrow.gif\" alt=\"external link: \" title=\"external link\"/> <a href=\"link\">link</a></span> part of the same paragraph</p><p class=\"paragraph\">Annother paragraph</p><p class=\"paragraph\">Annother paragraph\n"
				+"</p>"
				, result);
	}

}
