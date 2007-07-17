/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.portal.charon.test;

import junit.framework.TestCase;

/**
 * A unit test that performs a template render test, checking for XMTML
 * compliance. You should check the transcript for resutls. The test will not
 * fail.
 * 
 * @author ieb
 */
public class PortalRenderTest extends TestCase
{

	public static void main(String[] args)
	{
	}

	public PortalRenderTest(String arg0) throws Exception
	{
		super(arg0);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testAllTemplates() throws Exception
	{
		try
		{
			System.err.println("It is not possible to unit test the portal while there are  static covers in org.sakaiproject.util ");
			//MockCharonPortal mock = new MockCharonPortal();
			// mock.doError();
			//mock.doGalleryTabs();
			//mock.doGallery();
			//mock.doNavLogin();
			//mock.doNavLoginGallery();
			//mock.doPage();
			//mock.doSite();
			//mock.doSiteTabs();
			//mock.doWorksite();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
