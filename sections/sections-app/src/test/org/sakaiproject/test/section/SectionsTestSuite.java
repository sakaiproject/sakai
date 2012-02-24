/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.test.section;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SectionsTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(AuthzTest.class);
		suite.addTestSuite(CourseManagerTest.class);
		suite.addTestSuite(SectionAwarenessTest.class);
		suite.addTestSuite(SectionManagerTest.class);
		suite.addTestSuite(SectionSortTest.class);
		suite.addTestSuite(TimeConversionTest.class);
		return suite;
	}
}
