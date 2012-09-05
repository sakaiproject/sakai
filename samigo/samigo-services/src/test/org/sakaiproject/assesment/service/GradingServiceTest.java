package org.sakaiproject.assesment.service;

import java.util.Map;

import org.sakaiproject.tool.assessment.services.GradingService;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class GradingServiceTest extends AbstractTransactionalSpringContextTests {
	
	
	public void testValidate() {
		GradingService gradingService = new GradingService();
		
		try {
			gradingService.validate("2.1");
		
		} catch (Exception e) {
			fail();
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
			fail();
		} catch (Exception e) {
			
		}
		
		
		try {
			Map map = gradingService.validate("6.022E23");
			assertTrue(map.containsKey(GradingService.ANSWER_TYPE_REAL));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		
		try {
			Map map = gradingService.validate("1+9i");
			assertTrue(map.containsKey(GradingService.ANSWER_TYPE_COMPLEX));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
