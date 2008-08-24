/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author ieb
 */
public class TestAll extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.sakaiproject.jcr.test");
		// $JUnit-BEGIN$
		suite.addTestSuite(LoadRepository.class);

		suite.addTestSuite(QueryTestData.class);
		suite.addTestSuite(NodeTestData.class);
		suite.addTestSuite(PropertyTestData.class);
		suite.addTestSuite(ExportDocViewTestData.class);

		suite.addTest(org.apache.jackrabbit.test.api.TestAll.suite());
		suite.addTest(org.apache.jackrabbit.test.api.query.TestAll.suite());
		suite.addTest(org.apache.jackrabbit.test.api.nodetype.TestAll.suite());
		suite.addTest(org.apache.jackrabbit.test.api.util.TestAll.suite());
		suite.addTest(org.apache.jackrabbit.test.api.lock.TestAll.suite());
		suite.addTest(org.apache.jackrabbit.test.api.version.TestAll.suite());
		suite.addTest(org.apache.jackrabbit.test.api.observation.TestAll.suite());
		
		// $JUnit-END$
		return suite;
	}

}
