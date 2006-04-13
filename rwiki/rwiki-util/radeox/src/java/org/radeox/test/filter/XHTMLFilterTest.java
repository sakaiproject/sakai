/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
		// TODO Auto-generated constructor stub
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
			System.err.println(":" + result + ":");
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
