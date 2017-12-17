/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.sakaiproject.content.impl.ContentHostingComparator;
import org.sakaiproject.entity.api.ResourceProperties;

public class SortTest {
	@Test
	public void testSorts() {
		ContentHostingComparator c = new ContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true); 
		
		Assert.assertEquals(-1, c.compareLikeMacFinder("AAAAA", "BBBBBB"));
		Assert.assertEquals(0, c.compareLikeMacFinder("AAAA", "AAAA"));
		Assert.assertEquals(1, c.compareLikeMacFinder("BBBB", "AAAA"));

		Assert.assertEquals(-1, c.compareLikeMacFinder("AAAA", "AAAA10"));

		Assert.assertEquals(-1, c.compareLikeMacFinder("AA1", "AA2"));
		Assert.assertEquals(0, c.compareLikeMacFinder("AA1", "AA1"));
		Assert.assertEquals(-1, c.compareLikeMacFinder("AA1", "AA10"));
		Assert.assertEquals(-1, c.compareLikeMacFinder("AA2", "AA10"));

		Assert.assertEquals(-1, c.compareLikeMacFinder("AA1FOO", "AA10FOO"));
		Assert.assertEquals(-1, c.compareLikeMacFinder("AA1", "AA10FOO")); // tough
																	// case ...
																	// what to
																	// do?

		Assert.assertTrue(c.compareLikeMacFinder("Bill's Stuff", "Joe's Stuff") < 0);

		Assert.assertTrue(c.compareLikeMacFinder("1$%%$@%", "2(()$@$$") < 0);

		/*
		 * incomparable groups ... sort as strings
		 */
		Assert.assertTrue(c.compareLikeMacFinder("AAA111", "111AAA") > 0);

		/*
		 * check for numeric overflow ...
		 */
		Assert.assertTrue(c.compareLikeMacFinder("A1", "A184467440737095516160") < 0);
		
		/**
		 * It should be case sensitive but these both fail -DH
		 */
		//assertEquals(0,  c.compareLikeMacFinder("Leon", "leon"));
		//assertEquals(0,  c.compareLikeMacFinder("leon", "léon"));
		
		
	}
	
	private List createResources(List <String>resources) {
		List <MockContentResource> testList = new ArrayList<MockContentResource>();
		for (String resource:resources) {
			testList.add(new MockContentResource("test",resource));
		}
		return testList;
	}

	@Test
	public void testNameCompare() {
		ContentHostingComparator c = new ContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true, false); 
		//Test ids to use
		List <String> testIds = Arrays.asList("11","10","10x","1","1x","2x","2","4","4","4x","4x","6");
		//Expected sort after the "smart sort"
		List <String> smartSortIds = Arrays.asList("1","2","4","10","10x","6","11","1x","2x","4","4x","4x");
		List <String> regularSortIds = Arrays.asList("11","10","10x","1","1x","2x","2","4","4","4x","4x","6");

		List <MockContentResource> testResources = createResources(testIds);
		List <MockContentResource> smartSortResources = createResources(smartSortIds);
		List <MockContentResource> regularSortResources = createResources(regularSortIds);
		Collections.sort(testResources,c);
		for (int i=0;i<testResources.size();i++) {
			Assert.assertEquals(testResources.get(i).resourceId,smartSortResources.get(i).resourceId); 
		}

		//Switch to content length (which is 0 in mock) and regular sorting
		c = new ContentHostingComparator(ResourceProperties.PROP_CONTENT_LENGTH, false, true); 
		testResources = createResources(testIds);
		Collections.sort(testResources,c);
		for (int i=0;i<testResources.size();i++) {
			Assert.assertEquals(testResources.get(i).resourceId,regularSortResources.get(i).resourceId); 
		}
	}
	
	@Test
	public void testLocaleSorts() {
		ContentHostingComparator c = new ContentHostingComparator(null, true);

		Assert.assertEquals(-1, c.comparerLocalSensitive("AAAAA", "BBBBBB"));
		Assert.assertEquals(0, c.comparerLocalSensitive("AAAA", "AAAA"));
		Assert.assertEquals(1, c.comparerLocalSensitive("BBBB", "AAAA"));

		Assert.assertEquals(-1, c.comparerLocalSensitive("AAAA", "AAAA10"));

		Assert.assertEquals(-1, c.comparerLocalSensitive("AA1", "AA2"));
		Assert.assertEquals(0, c.comparerLocalSensitive("AA1", "AA1"));
		Assert.assertEquals(-1, c.comparerLocalSensitive("AA1", "AA10"));
		//assertEquals(-1, c.comparerLocalSensitive("AA2", "AA10"));
		
		
		//For some reason this fails
		//assertEquals(-1, c.comparerLocalSensitive("AA1FOO", "AA10FOO"));
		// tough case, what to do?
		Assert.assertEquals(-1, c.comparerLocalSensitive("AA1", "AA10FOO"));

		Assert.assertTrue(c.comparerLocalSensitive("Bill's Stuff", "Joe's Stuff") < 0);

		Assert.assertTrue(c.comparerLocalSensitive("1$%%$@%", "2(()$@$$") < 0);

		/*
		 * incomparable groups ... sort as strings
		 */
		Assert.assertTrue(c.comparerLocalSensitive("AAA111", "111AAA") > 0);
		
		Assert.assertEquals(0,  c.comparerLocalSensitive("Leon", "leon"));
		// FIXME this test is failing to pass -AZ
		//assertEquals(0,  c.comparerLocalSensitive("leon", "léon"));

		/*
		 * check for numeric overflow ...
		 */
		Assert.assertTrue(c.comparerLocalSensitive("A1", "A184467440737095516160") < 0);
	}

	public void testPrioritySorts() {
		ContentHostingComparator c = new ContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true, false);

		MockContentResource priority10 = new MockContentResource("test", "itema");
		priority10.getPropertiesEdit().addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, "10");

		MockContentResource priorityNull = new MockContentResource("test", "itemb");
		MockContentResource diffPriorityNull = new MockContentResource("test", "iteme");

		MockContentResource priority1 = new MockContentResource("test", "itemc");
		MockContentResource diffPriority1 = new MockContentResource("test", "itemd");
		priority1.getPropertiesEdit().addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, "1");
		diffPriority1.getPropertiesEdit().addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, "1");

		Assert.assertEquals(1, c.compare(priorityNull, priority10));
		Assert.assertEquals(1, c.compare(priorityNull, priority1));
		Assert.assertEquals(-1, c.compare(priority1, priority10));
		Assert.assertEquals(0, c.compare(priority1, diffPriority1));
		Assert.assertTrue(c.compare(priorityNull, diffPriorityNull) < 0);

		c = new ContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, false, false);
		Assert.assertEquals(-1, c.compare(priorityNull, priority10));
		Assert.assertEquals(-1, c.compare(priorityNull, priority1));
		Assert.assertEquals(1, c.compare(priority1, priority10));
		Assert.assertEquals(0, c.compare(priority1, diffPriority1));
		Assert.assertTrue(c.compare(priorityNull, diffPriorityNull) > 0);
	}
	
}
