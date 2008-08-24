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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.test;

import org.sakaiproject.content.impl.ContentHostingComparator;

import junit.framework.TestCase;

public class SortTest extends TestCase {

	public SortTest(String name) {
		super(name);
	}

	public void testSorts() {
		ContentHostingComparator c = new ContentHostingComparator(null, true);

		assertEquals(-1, c.compareLikeMacFinder("AAAAA", "BBBBBB"));
		assertEquals(0, c.compareLikeMacFinder("AAAA", "AAAA"));
		assertEquals(1, c.compareLikeMacFinder("BBBB", "AAAA"));

		assertEquals(-1, c.compareLikeMacFinder("AAAA", "AAAA10"));

		assertEquals(-1, c.compareLikeMacFinder("AA1", "AA2"));
		assertEquals(0, c.compareLikeMacFinder("AA1", "AA1"));
		assertEquals(-1, c.compareLikeMacFinder("AA1", "AA10"));

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
	}
}
