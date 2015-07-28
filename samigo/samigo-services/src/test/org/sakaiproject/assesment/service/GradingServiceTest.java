
/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-services/src/test/org/sakaiproject/assessment/facade/test/AssesmentFacadeTest.java $
 * $Id: AssesmentFacadeTest.java 106463 2012-04-02 12:20:09Z david.horwitz@uct.ac.za $
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

package org.sakaiproject.assesment.service;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.tool.assessment.services.GradingService;

public class GradingServiceTest {
	
	@Test
	public void testValidate() {
		GradingService gradingService = new GradingService();
		
		try {
			gradingService.validate("2.1");
		
		} catch (Exception e) {
			Assert.fail();
		}
		
		/* FIX me this should work
		try {
			gradingService.validate("2,1");
		
		} catch (Exception e) {
			fail();
		}
	*/
		try {
			gradingService.validate("not a number");
			Assert.fail();
		} catch (Exception e) {
			
		}
		
		
		try {
			Map map = gradingService.validate("6.022E23");
			Assert.assertTrue(map.containsKey(GradingService.ANSWER_TYPE_REAL));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		
		try {
			Map map = gradingService.validate("1+9i");
			Assert.assertTrue(map.containsKey(GradingService.ANSWER_TYPE_COMPLEX));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
