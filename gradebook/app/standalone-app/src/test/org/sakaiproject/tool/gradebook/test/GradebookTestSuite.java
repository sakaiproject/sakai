/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GradebookTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// This test must run first to be useful, since
		// it tests automatic updating of a blank database.

		suite.addTestSuite(GradebookManagerOPCTest.class);
		suite.addTestSuite(AssignmentSortingTest.class);
		suite.addTestSuite(CalculationsTest.class);
		suite.addTestSuite(GradeCommentTest.class);
		suite.addTestSuite(SpreadsheetTest.class);
		suite.addTestSuite(GradeMappingTest.class);
		suite.addTestSuite(GradebookServiceTest.class);
		//suite.addTestSuite(GradebookServiceInternalTest.class);
		suite.addTestSuite(GradebookServiceNewTest.class);
		suite.addTestSuite(GradebookManagerTest.class);
		suite.addTestSuite(GradeManagerTest.class);
		suite.addTestSuite(GradableObjectManagerTest.class);
		suite.addTestSuite(GradeMappingConfigTest.class);
		suite.addTestSuite(FinalizeGradebookTest.class);
		
		return suite;
	}
}
