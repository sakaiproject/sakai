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

package org.radeox.filter;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.filter.context.FilterContext;

/*
 * The paragraph filter finds any text between two empty lines and inserts a
 * <p/> @author stephan @team sonicteam
 * 
 * @version $Id: ParagraphFilter.java 4158 2005-11-25 23:25:19Z
 *          ian@caret.cam.ac.uk $
 */

@Slf4j
public class ParagraphFilter implements Filter, CacheFilter
{
	private InitialRenderContext initialContext;

	private String breaksRE;

	private String replaceFirst;

	private String replaceLast;

	private String patternFristRE;

	private String patternLastRE;

	private String replaceAll;

	public String filter(String input, FilterContext context)
	{
		return simpleFilter(input,context);
	}

	public String simpleFilter(String input, FilterContext context)
	{

		log.debug("Paragraph Filter Input " + input);
		Pattern patternBreaks = Pattern.compile(breaksRE);

		// attempts to locate lin breaks in the content with ([ \t\r]*[\n]){2}
		String[] p = patternBreaks.split(input);
		if (p.length == 1)
		{
			// only 1, therefor no embeded paragraphs
			return input;
		}

		StringBuffer sb = new StringBuffer();
		int nsplits = 0;
		for (int i = 0; i < p.length; i++)
		{
			if (nsplits == 0)
			{
				sb.append(replaceFirst);
				nsplits++;
			}
			else
			{
				sb.append(replaceAll);
				nsplits++;
			}
			sb.append(p[i]);
		}

		if (nsplits > 0)
		{
			sb.append(replaceLast);
			nsplits++;
		}
		String output = sb.toString();
		log.debug("Paragraph Filter Input " + output);

		return output;
	}

	public String complexFilter(String input, FilterContext context)
	{

		log.debug("Paragraph Filter Input " + input);
		Pattern patternBreaks = Pattern.compile(breaksRE);

		// attempts to locate lin breaks in the content with ([ \t\r]*[\n]){2}
		String[] p = patternBreaks.split(input);
		if (p.length == 1)
		{
			// only 1, therefor no embeded paragraphs
			return input;
		}

		StringBuffer sb = new StringBuffer();
		int nsplits = 0;
		// find the last > in the first paragraph
		int ins = p[0].lastIndexOf(">");
		if (ins > 0 && ins + 1 < p[0].length())
		{
			// add all upto the last > into the buffer
			sb.append(p[0].substring(0, ins + 1));
			// then put <p class=\"paragraph\">
			sb.append(replaceFirst);
			nsplits++;
			// then append the remainder
			sb.append(p[0].substring(ins + 1));
		}
		else
		{
			sb.append(p[0]);

		}

		for (int i = 1; i < p.length - 1; i++)
		{
			// for all the following, add in </p><p class=\"paragraph\">
			if (nsplits == 0)
			{
				sb.append(replaceFirst);
				nsplits++;
			}
			else
			{
				sb.append(replaceAll);
				nsplits++;
			}
			sb.append(p[i]);
		}

		// in the last block find fidn the first <
		ins = p[p.length - 1].indexOf("<");
		if (ins > 0)
		{
			// put the standard line para blreak block in </p><p
			// class=\"paragraph\">
			if (nsplits == 0)
			{
				sb.append(replaceFirst);
				nsplits++;
			}
			else
			{
				sb.append(replaceAll);
				nsplits++;
			}
			// append the first part of the last block
			sb.append(p[p.length - 1].substring(0, ins - 1));
			// append the last seperator </p>
			if (nsplits > 0)
			{
				sb.append(replaceLast);
				nsplits++;
			}
			// append the remainder block
			sb.append(p[p.length - 1].substring(ins - 1));
		}
		else if (ins == 0)
		{
			// found "<" inposition found in last block so do </p>
			if (nsplits > 0)
			{
				sb.append(replaceLast);
				nsplits++;
			}
			sb.append(p[p.length - 1]);
		}
		else
		{
			// append the last </p>
			if (nsplits > 0)
			{
				sb.append(replaceLast);
				nsplits++;
			}
			sb.append(p[p.length - 1]);
		}
		String output = sb.toString();
		log.debug("Paragraph Filter Input " + output);

		return output;
	}

	public String[] replaces()
	{
		return FilterPipe.NO_REPLACES;
	}

	public String[] before()
	{
		return FilterPipe.EMPTY_BEFORE;
	}

	public void setInitialContext(InitialRenderContext context)
	{
		initialContext = context;
		ResourceBundle outputMessages = getOutputBundle();
		ResourceBundle inputMessages = getInputBundle();

		breaksRE = inputMessages.getString("filter.paragraph.breaks.match");
		replaceAll = outputMessages.getString("filter.paragraph.breaks.print");
		replaceFirst = outputMessages.getString("filter.paragraph.first.print");
		replaceLast = outputMessages.getString("filter.paragraph.last.print");
		patternFristRE = inputMessages
				.getString("filter.paragraph.first.match");
		patternLastRE = inputMessages.getString("filter.paragraph.last.match");
	}

	public String getDescription()
	{
		return "Hand Coded paragraph filter";
	}

	protected ResourceBundle getInputBundle()
	{
		Locale inputLocale = (Locale) initialContext
				.get(RenderContext.INPUT_LOCALE);
		String inputName = (String) initialContext
				.get(RenderContext.INPUT_BUNDLE_NAME);
		return ResourceBundle.getBundle(inputName, inputLocale);
	}

	protected ResourceBundle getOutputBundle()
	{
		String outputName = (String) initialContext
				.get(RenderContext.OUTPUT_BUNDLE_NAME);
		Locale outputLocale = (Locale) initialContext
				.get(RenderContext.OUTPUT_LOCALE);
		return ResourceBundle.getBundle(outputName, outputLocale);
	}

}
