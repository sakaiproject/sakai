/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakai.search.index.impl.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;

import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.component.adapter.contenthosting.RegexParser;

import junit.framework.TestCase;

/**
 * @author ieb
 *
 */
public class RegexParserTest extends TestCase
{

	/**
	 * @param arg0
	 */
	public RegexParserTest(String arg0)
	{
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testParser() throws Exception {
		Properties p = new Properties();
		InputStream inStream = getClass().getResourceAsStream("parsertest.properties");
		p.load(inStream);
		inStream.close();
		for ( Iterator tests = p.keySet().iterator(); tests.hasNext(); ) {
			
			String tname = (String)tests.next();
			StringBuffer sb = new StringBuffer();
			for ( Iterator<String> i = new RegexParser(loadFile(p.getProperty(tname))); i.hasNext();) {
				SearchUtils.filterWordLength(i.next(), sb, 3);
			}
			String result = sb.toString();
			System.err.println("Result is "+result);
			if ( p.containsKey(tname+".result") ) {
				assertEquals("Tokens dont match ", loadFile(p.getProperty(tname+".result")), sb.toString());
			}
		}
	}

	/**
	 * @param property
	 * @return
	 * @throws IOException 
	 */
	private String loadFile(String property) throws IOException
	{
		System.err.println("Loading :"+property+":");
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(property)));
		StringBuffer sb = new StringBuffer();
		for ( String s = br.readLine(); s != null; s = br.readLine()) {
			sb.append(s).append("\n");
		}
		br.close();
		return sb.toString();
	}

}
