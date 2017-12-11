/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.charon.test;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import junit.framework.TestCase;

/**
 * A unit test that performs a template render test, checking for XMTML
 * compliance. You should check the transcript for resutls. The test will not
 * fail.
 * 
 * @author ieb
 */
@Slf4j
public class PortalRenderTest extends TestCase
{
	public static void main(String[] args)
	{
	}

	private File baseDirectory;
	private MockCharonPortal mock;

	public PortalRenderTest(String arg0) throws Exception
	{
		super(arg0);
		baseDirectory = new File("target","PortalRenderTest");
		if ( baseDirectory.exists() ) {
		PortalTestFileUtils.deleteAll(baseDirectory);
		}
		baseDirectory.mkdirs();
	}

	@Override
	protected void setUp() throws Exception
	{
		mock = new MockCharonPortal(baseDirectory);
		mock.setResourceLoader(new MockResourceLoader());
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	public void testErrorNoSession() throws Exception
	{
		try
		{
			log.info("========= Testing testErrorNoSession");
			mock.setOutputFile("errorNoSession");
			mock.doError(false,false);
			log.info("========= PASSED Testing testErrorNoSession");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testErrorNoSession");
			fail(ex.getMessage());
		}
	}
	public void testErrorWithSession() throws Exception
	{
		try
		{
			log.info("========= Testing testErrorWithSession");
			mock.setOutputFile("errorWithSession");
			mock.doError(true,false);
			log.info("========= PASSED Testing testErrorWithSession");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testErrorWithSession");
			fail(ex.getMessage());

		}
	}
	public void testErrorWithToolSession() throws Exception
	{
		try
		{
			log.info("========= Testing testErrorWithToolSession");
			mock.setOutputFile("errorWithToolSession");
			mock.doError(false,true);
			log.info("========= PASSED Testing testErrorWithToolSession");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testErrorWithToolSession");
			fail(ex.getMessage());

		}
	}
	public void testErrorWithAll() throws Exception
	{
		try
		{
			log.info("========= Testing testErrorWithAll");
			mock.setOutputFile("errorWithAll");
			mock.doError(true,true);
			log.info("========= PASSED Testing testErrorWithAll");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testErrorWithAll");
			fail(ex.getMessage());

		}
	}

	public void testPage() throws Exception
	{
		try
		{
			log.info("========= Testing testPage");
			mock.doPage();
			log.info("========= PASSED Testing testPage");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testPage");
			fail(ex.getMessage());
		}
	}

	public void testSite() throws Exception
	{
		try
		{
			log.info("========= Testing testSite");
			mock.doSite();
			log.info("========= PASSED Testing testSite");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testSite");
			fail(ex.getMessage());
		}
	}
	public void testWorksite() throws Exception
	{
		try
		{
			log.info("========= Testing testWorksite");
			mock.doWorksite();
			log.info("========= PASSED Testing testWorksite");
		}
		catch (Exception ex)
		{
			log.info("========= FAILED Testing testWorksite");
			fail(ex.getMessage());
		}
	}
}
