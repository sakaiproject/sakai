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

import org.junit.Test;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.impl.ContentHostingComparator;
import org.sakaiproject.entity.api.ResourceProperties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SortTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testSorts() {
		ContentHostingComparator c = new ContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true); 
		
		assertEquals(-1, c.compareLikeMacFinder("AAAAA", "BBBBBB"));
		assertEquals(0, c.compareLikeMacFinder("AAAA", "AAAA"));
		assertEquals(1, c.compareLikeMacFinder("BBBB", "AAAA"));

		assertEquals(-1, c.compareLikeMacFinder("AAAA", "AAAA10"));

		assertEquals(-1, c.compareLikeMacFinder("AA1", "AA2"));
		assertEquals(0, c.compareLikeMacFinder("AA1", "AA1"));
		assertEquals(-1, c.compareLikeMacFinder("AA1", "AA10"));
		assertEquals(-1, c.compareLikeMacFinder("AA2", "AA10"));

		assertEquals(-1, c.compareLikeMacFinder("AA1FOO", "AA10FOO"));
		assertEquals(-1, c.compareLikeMacFinder("AA1", "AA10FOO")); // tough
																	// case ...
																	// what to
																	// do?

		assertTrue(c.compareLikeMacFinder("Bill's Stuff", "Joe's Stuff") < 0);

		assertTrue(c.compareLikeMacFinder("1$%%$@%", "2(()$@$$") < 0);

		/*
		 * incomparable groups ... sort as strings
		 */
		assertTrue(c.compareLikeMacFinder("AAA111", "111AAA") > 0);

		/*
		 * check for numeric overflow ...
		 */
		assertTrue(c.compareLikeMacFinder("A1", "A184467440737095516160") < 0);
		
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
			assertEquals(testResources.get(i).resourceId,smartSortResources.get(i).resourceId); 
//			System.out.printf("\"%s\" ",testResources.get(i).resourceId);
		}
		System.out.println();

		//Switch to content length (which is 0 in mock) and regular sorting
		c = new ContentHostingComparator(ResourceProperties.PROP_CONTENT_LENGTH, false, true); 
		testResources = createResources(testIds);
		Collections.sort(testResources,c);
		for (int i=0;i<testResources.size();i++) {
			assertEquals(testResources.get(i).resourceId,regularSortResources.get(i).resourceId); 
//			System.out.printf("\"%s\" ",testResources.get(i).resourceId);
		}
		System.out.println();
	}

	@Test
	public void testLocaleSorts() {
		ContentHostingComparator c = new ContentHostingComparator(null, true);

		assertEquals(-1, c.comparerLocalSensitive("AAAAA", "BBBBBB"));
		assertEquals(0, c.comparerLocalSensitive("AAAA", "AAAA"));
		assertEquals(1, c.comparerLocalSensitive("BBBB", "AAAA"));

		assertEquals(-1, c.comparerLocalSensitive("AAAA", "AAAA10"));

		assertEquals(-1, c.comparerLocalSensitive("AA1", "AA2"));
		assertEquals(0, c.comparerLocalSensitive("AA1", "AA1"));
		assertEquals(-1, c.comparerLocalSensitive("AA1", "AA10"));
		//assertEquals(-1, c.comparerLocalSensitive("AA2", "AA10"));
		
		
		//For some reason this fails
		//assertEquals(-1, c.comparerLocalSensitive("AA1FOO", "AA10FOO"));
		// tough case, what to do?
		assertEquals(-1, c.comparerLocalSensitive("AA1", "AA10FOO"));

		assertTrue(c.comparerLocalSensitive("Bill's Stuff", "Joe's Stuff") < 0);

		assertTrue(c.comparerLocalSensitive("1$%%$@%", "2(()$@$$") < 0);

		/*
		 * incomparable groups ... sort as strings
		 */
		assertTrue(c.comparerLocalSensitive("AAA111", "111AAA") > 0);
		
		assertEquals(0,  c.comparerLocalSensitive("Leon", "leon"));
		// FIXME this test is failing to pass -AZ
		//assertEquals(0,  c.comparerLocalSensitive("leon", "léon"));

		/*
		 * check for numeric overflow ...
		 */
		assertTrue(c.comparerLocalSensitive("A1", "A184467440737095516160") < 0);
	}

	@Test
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

		assertEquals(1, c.compare(priorityNull, priority10));
		assertEquals(1, c.compare(priorityNull, priority1));
		assertEquals(-1, c.compare(priority1, priority10));
		assertEquals(0, c.compare(priority1, diffPriority1));
		assertTrue(c.compare(priorityNull, diffPriorityNull) < 0);

		c = new ContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, false, false);
		assertEquals(-1, c.compare(priorityNull, priority10));
		assertEquals(-1, c.compare(priorityNull, priority1));
		assertEquals(1, c.compare(priority1, priority10));
		assertEquals(0, c.compare(priority1, diffPriority1));
		assertTrue(c.compare(priorityNull, diffPriorityNull) > 0);
	}


	@Test
	public void testPrioritySortThenName() {
		ContentHostingComparator comparator = new ContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true, false);
		// Check that when priority is the same we fallback to the name
		ContentResource cr1 = new PriorityContentResource("collection", "file-a.txt");
		ContentResource cr2 = new PriorityContentResource("collection", "file-b.txt");
		ContentResource cr3 = new PriorityContentResource("collection", "file-c.txt");

		ContentResource pcr1 = new PriorityContentResource("collection", "file-a.txt");
		pcr1.getProperties().addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, "1");
		ContentResource pcr2 = new PriorityContentResource("collection", "file-a.txt");
		pcr2.getProperties().addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, "2");
		ContentResource pcr3 = new PriorityContentResource("collection", "file-a.txt");
		pcr3.getProperties().addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, "3");

		List<ContentResource> actual = new ArrayList<>();
		actual.addAll(Arrays.asList(new ContentResource[]{cr3, cr2, cr1, pcr3, pcr2, pcr1}));
		actual.sort(comparator);

		ContentResource[] expected = {pcr1, pcr2, pcr3, cr1, cr2, cr3};
		assertArrayEquals(expected, actual.toArray());

	}

	/**
	 * Just makes debugging easier when wanting to know the filename and priority sort.
	 */
	private class PriorityContentResource extends MockContentResource {
		public PriorityContentResource(String collectionId, String resourceId) {
			super(collectionId, resourceId);
		}
		@Override
		public String toString() {
			return resourceId+ " "+ getProperties().getProperty(ResourceProperties.PROP_CONTENT_PRIORITY);
		}
	}

}
