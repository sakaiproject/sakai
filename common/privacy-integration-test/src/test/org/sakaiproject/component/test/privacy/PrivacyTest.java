/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.component.test.privacy;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.test.SakaiTestBase;

@Slf4j
public class PrivacyTest extends SakaiTestBase
{
	private PrivacyManager privacyManager;
	
	public static Test suite() 
	{
		TestSetup setup = new TestSetup(new TestSuite(PrivacyTest.class)) 
		{
			protected void setUp() throws Exception 
			{
				log.info("starting setup -- PrivacyTest");
				oneTimeSetup();
				log.info("finished setup -- PrivacyTest");
			}
			protected void tearDown() throws Exception 
			{
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void setUp() throws Exception 
	{
		log.info("Setting up an AuthzIntegrationTest test");

		privacyManager = (PrivacyManager)getService(PrivacyManager.class.getName());
	}
	
	public void tearDown() throws Exception 
	{
		log.info("Tearing down an PrivacyTest test");
		
		privacyManager = null;
	}
	
	public void testSetViewableState() throws Exception 
	{
//		privacyManager.setViewableState("main_page", "a", new Boolean(false), org.sakaiproject.api.privacy.PrivacyManager.USER_RECORD_TYPE);
//		privacyManager.setViewableState("main_page", "a", new Boolean(true), org.sakaiproject.api.privacy.PrivacyManager.SYSTEM_RECORD_TYPE);
//		
//		privacyManager.setViewableState("main_page1", "a", new Boolean(false), org.sakaiproject.api.privacy.PrivacyManager.USER_RECORD_TYPE);
//		privacyManager.setViewableState("main_page1", "a", new Boolean(true), org.sakaiproject.api.privacy.PrivacyManager.SYSTEM_RECORD_TYPE);
//
//		privacyManager.setViewableState("main_page", "b", new Boolean(false), org.sakaiproject.api.privacy.PrivacyManager.USER_RECORD_TYPE);		
//		privacyManager.setViewableState("main_page", "b", new Boolean(true), org.sakaiproject.api.privacy.PrivacyManager.SYSTEM_RECORD_TYPE);
//
//		privacyManager.setViewableState("main_page1", "b", new Boolean(true), org.sakaiproject.api.privacy.PrivacyManager.USER_RECORD_TYPE);		
//		privacyManager.setViewableState("main_page1", "b", new Boolean(true), org.sakaiproject.api.privacy.PrivacyManager.SYSTEM_RECORD_TYPE);
	}
	
	public void testFindViewable() throws Exception
	{
		Set inputSet = new TreeSet();
		inputSet.add("a");
		inputSet.add("b");
		inputSet.add("c");
		inputSet.add("d");
		Set resultSet = privacyManager.findViewable("main_page", inputSet);
		Iterator iter = resultSet.iterator();
		while(iter.hasNext())
		{
			String user = (String)iter.next();
			log.info("Hidden users -- maing_page: " + user);
		}
	}
	
	public void testGetViewableState() throws Exception
	{
		Set resultSet = privacyManager.getViewableState("main_page1", new Boolean(true), PrivacyManager.SYSTEM_RECORD_TYPE);
		Iterator iter = resultSet.iterator();
		while(iter.hasNext())
		{
			String user = (String)iter.next();
			log.info("PrivacyRecord -- main_page1:" + PrivacyManager.SYSTEM_RECORD_TYPE + ":" + user);
		}
	}
	
	public void testGetViewableState2() throws Exception
	{
		Map returnedMap = privacyManager.getViewableState("main_page", PrivacyManager.USER_RECORD_TYPE);
		Set keySet = returnedMap.keySet();
		Iterator iter = keySet.iterator();
		while(iter.hasNext())
		{
			String userId = (String)iter.next();
			Boolean bl = (Boolean)returnedMap.get(userId);
			log.info("testGetViewableState2 -- " + "main_page:" + PrivacyManager.USER_RECORD_TYPE + ":" + userId + ":" + bl.booleanValue());
		}
	}
	
	public void testSetViewableState2() throws Exception
	{
		Map inputMap = new HashMap();
		inputMap.put("a", new Boolean(false));
		inputMap.put("b", new Boolean(true));
		inputMap.put("c", new Boolean(true));
		privacyManager.setViewableState("main_page", inputMap, PrivacyManager.USER_RECORD_TYPE);
	}
	
	public void testIsViewable() throws Exception
	{
		log.info("Privacy Test -- testIsView:");
		log.info(new Boolean(privacyManager.isViewable("main_page", "a")).toString());
	}
}
