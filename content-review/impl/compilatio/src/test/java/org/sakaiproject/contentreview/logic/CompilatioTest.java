/**********************************************************************************
 *
 * Copyright (c) 2016 Sakai Foundation
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
package org.sakaiproject.contentreview.logic;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.contentreview.compilatio.CompilatioReviewServiceImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration({"/hibernate-test.xml", "/spring-hibernate.xml"})
public class CompilatioTest extends AbstractTransactionalJUnit4SpringContextTests {
	private static final Log log = LogFactory.getLog(CompilatioTest.class);

	@Test
	public void testFileEscape() {
		CompilatioReviewServiceImpl compilatioService = new CompilatioReviewServiceImpl();
		String someEscaping = compilatioService.escapeFileName("Practical%203.docx", "contentId");
		Assert.assertEquals("Practical_3.docx", someEscaping);
		
		someEscaping = compilatioService.escapeFileName("Practical%203%.docx", "contentId");
		Assert.assertEquals("contentId", someEscaping);
		
		someEscaping = compilatioService.escapeFileName("Practical3.docx", "contentId");
		Assert.assertEquals("Practical3.docx", someEscaping);
		
		
	}
	
	
}
