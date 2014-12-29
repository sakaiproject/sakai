
/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.assessment.facade.test;

import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.springframework.test.AbstractTransactionalSpringContextTests;


public class AssesmentFacadeTest  extends AbstractTransactionalSpringContextTests{

	protected String[] getConfigLocations() {
		return new String[] {"/spring-hibernate.xml"};
	}

	//our object
	AssessmentFacadeQueries queries = null;

	protected void onSetUpInTransaction() throws Exception {
		queries = new AssessmentFacadeQueries();
		queries.setSessionFactory((SessionFactory)applicationContext.getBean("sessionFactory"));



	}

	public void testGetAssesment() {
		/*
		 * We expect a item that doesn't exist to return null
		 * not to escalate an exception
		 */
		try {
			AssessmentFacade item = queries.getAssessment(999999L);
			assertNull(item);
		} catch (Exception e) {
			fail("unexpected exception");
		}

	}



}
