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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.filter.XHTMLFilter;

/**
 * @author ieb
 */
public class XHTMLFilterTest extends FilterTestSupport
{

	public XHTMLFilterTest(String s)
	{
		super(s);
	}

	protected void setUp() throws Exception
	{
		filter = new XHTMLFilter();
		super.setUp();
	}

	public static Test suite()
	{
		return new TestSuite(XHTMLFilterTest.class);
	}

	public void testXHTMLFilter()
	{

		for (int i = 0;; i++)
		{
			String teststring = getTestPattern("/testpatterns/xhtmltest" + i
					+ "_in.xml");
			if (teststring == null) break;
			String resultstring = getTestPattern("/testpatterns/xhtmltest" + i
					+ "_out.xml");
			String result = filter.filter(teststring, context);
			System.err.println("IN:" + teststring + ":IN");
			System.err.println("OUT:" + result + ":OUT");
			if (resultstring != null)
			{
				assertEquals(resultstring, result);
			}
		}
	}

	private String getTestPattern(String path)
	{
		try
		{
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream(path)));
			StringBuffer sb = new StringBuffer();
			String line = bis.readLine();
			while (line != null)
			{
				sb.append(line).append("\n");
				line = bis.readLine();
			}
			return sb.toString();
		}
		catch (Exception ex)
		{

		}
		return null;
	}
}
