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

import java.text.MessageFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.filter.Filter;
import org.radeox.filter.HeadingFilter;
import org.radeox.filter.context.BaseFilterContext;
import org.radeox.filter.context.FilterContext;
import org.radeox.filter.regex.RegexReplaceFilter;
import org.radeox.filter.regex.RegexTokenFilter;
import org.radeox.macro.code.XmlCodeFilter;
import org.radeox.regex.MatchResult;
import org.radeox.regex.Matcher;
import org.radeox.regex.Pattern;

public class BasicRegexTest extends TestCase
{
	private static String BOLD_TEST_REGEX = "(^|>|[[:space:]]+)__(.*?)__([[:space:]]+|<|$)";

	private org.radeox.regex.Compiler compiler;

	public BasicRegexTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		compiler = org.radeox.regex.Compiler.create();
		compiler.setMultiline(true);
	}

	public static Test suite()
	{
		return new TestSuite(BasicRegexTest.class);
	}

	// filter.heading.match=^[[:space:]]*(1(\\.1)*)[[:space:]]+(.*?)$
	// filter.heading.print=<h3 class=\"heading-{0}\">{1}</h3>

	public void testStartEnd()
	{
		Pattern p = compiler.compile("^A.*B$");
		Matcher m = Matcher.create("A1234567B", p);
		assertTrue("^...$ pattern found", m.matches());
	}

	public void testHeading()
	{
		FilterContext context = new BaseFilterContext();
		context.setRenderContext(new BaseRenderContext());
		Filter filter = new HeadingFilter();
		filter.setInitialContext(new BaseInitialRenderContext());
		assertEquals("Heading replaced", "<h3 class=\"heading-1\">test</h3>",
				filter.filter("1 test", context));
	}

	public void testByHandHeading()
	{
		RegexTokenFilter filter = new RegexTokenFilter()
		{
			public void handleMatch(StringBuffer buffer, MatchResult result,
					FilterContext context)
			{
				String outputTemplate = "<h3 class=\"heading-{0}\">{1}</h3>";
				MessageFormat formatter = new MessageFormat("");
				formatter.applyPattern(outputTemplate);
				buffer.append(formatter.format(new Object[] {
						result.group(1).replace('.', '-'), result.group(3) }));
			}
		};
		filter.addRegex("^[\\p{Space}]*(1(\\.1)*)[\\p{Space}]+(.*?)$", "");
		FilterContext context = new BaseFilterContext();
		context.setRenderContext(new BaseRenderContext());
		assertEquals("Heading replaced",
				"<h3 class=\"heading-1\">testHand</h3>", filter.filter(
						"1 testHand", context));
	}

	public void testWordBorders()
	{
		Pattern p = compiler.compile("\\bxsl\\b");
		Matcher m = Matcher.create("test xsl test", p);
		assertTrue("Word found", m.contains());
		m = Matcher.create("testxsltest", p);
		assertTrue("Word not found", !m.contains());
	}

	public void testByHandUrl()
	{
		// ([^\"'=]|^)
		// Pattern p =
		// Pattern.compile("((http|ftp)s?://(%[[:digit:]A-Fa-f][[:digit:]A-Fa-f]|[-_.!~*';/?:@#&=+$,[:alnum:]])+)",
		// Pattern.MULTILINE);
		Pattern p = compiler
				.compile("(http|ftp)s?://([-_.!~*';/?:@#&=+$,\\p{Alnum}])+");
		Matcher m = Matcher.create("http://snipsnap.org", p);
		assertTrue("A Url found", m.matches());
	}

	public void testXmlCodeFilter()
	{
		Pattern p = compiler.compile("\"(([^\"\\\\]|\\.)*)\"");
		Matcher m = Matcher.create("<xml attr=\"attr\"/>", p);

		assertEquals("Quote replaced",
				"<xml attr=<span class=\"xml-quote\">\"attr\"</span>/>", m
						.substitute("<span class=\"xml-quote\">\"$1\"</span>"));

		XmlCodeFilter xmlCodeFilter = new XmlCodeFilter();
		FilterContext context = new BaseFilterContext();
		context.setRenderContext(new BaseRenderContext());
		assertEquals(
				"XmlCodeFilter works",
				"<xml a=<span class=\"xml-quote\">\"attr\"</span>><node>text</node></xml>",
				xmlCodeFilter.filter("<xml a=\"attr\"><node>text</node></xml>",
						context));
	}

	public void testBackreference()
	{
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(
				"\\{([^:}]+)(?::([^\\}]*))?\\}(.*?)\\{\\1\\}",
				java.util.regex.Pattern.MULTILINE);
		java.util.regex.Matcher matcher = p
				.matcher("{code:xml}<xml a=\"attr\"><node>text</node></xml>{code}");
		assertTrue("A Backreference Regex found", matcher.find());
		assertNotNull("Content not null", matcher.group(3));
		assertEquals("Content found",
				"<xml a=\"attr\"><node>text</node></xml>", matcher.group(3));
	}

	public void testRegexBasic()
	{
		Pattern p = compiler.compile("A");
		Matcher m = Matcher.create("AB", p);

		assertTrue("A Regex found", m.contains());
	}

	public void testMultiline()
	{
		compiler.setMultiline(false);
		Pattern p = compiler.compile("A.*B");
		Matcher m = Matcher.create("A123\n456B", p);

		assertTrue("Multiline Regex found", m.matches());
	}

	public void testByHandBold()
	{
		Pattern p = compiler.compile(BOLD_TEST_REGEX);
		Matcher m = Matcher.create("__test__", p);

		assertEquals("Bold replaced by hand", "<b>test</b>", m
				.substitute("$1<b>$2</b>$3"));
	}

	public void testRegexFilterBold()
	{
		RegexReplaceFilter filter = new RegexReplaceFilter();
		filter.addRegex(BOLD_TEST_REGEX, "$1<b>$2</b>$3");
		FilterContext context = new BaseFilterContext();
		context.setRenderContext(new BaseRenderContext());
		assertEquals("Bold replaced with RegexFilter", "<b>test</b>", filter
				.filter("__test__", context));

	}
}
