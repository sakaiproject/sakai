/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.assessment.facade.test;


import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacadeQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations={"/spring-hibernate.xml"})
public class ItemFacadeTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	@Qualifier("itemFacadeQueries")
	private ItemFacadeQueries queries;

	@Test
	public void testGetIem() {
		/*
		 * We expect a item that doesn't exist to return null
		 * not to escalate an exception
		 */
		try {
			ItemFacade item = queries.getItem(99999999L);
			Assert.assertNull(item);
		} catch (Exception e) {
			Assert.fail("unexpected exception");
		}
	}
}
