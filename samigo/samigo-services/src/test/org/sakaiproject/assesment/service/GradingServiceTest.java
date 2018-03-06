/**
 * Copyright (c) 2005-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
			Assert.fail();
		}

		try {
			Map map = gradingService.validate("1+9i");
			Assert.assertTrue(map.containsKey(GradingService.ANSWER_TYPE_COMPLEX));
		} catch (Exception e) {
			Assert.fail();
		}
	}
}
