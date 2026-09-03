/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;

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

@RunWith(MockitoJUnitRunner.class)
public class PublishedAssesmentFacadeTest {

	@InjectMocks
	private PublishedAssessmentFacadeQueries queries;

	@Mock
	private SessionFactory sessionFactory;

	@Mock
	private Session session;

	@Before
	public void setUp() {
		when(sessionFactory.getCurrentSession()).thenReturn(session);
	}

	@Test
	public void testGetAssesment() {
		/*
		 * We expect a item that doesn't exist to return null
		 * not to escalate an exception
		 */
		try {
			when(session.get(eq(PublishedAssessmentData.class), eq(999999L))).thenReturn(null);

			PublishedAssessmentData item = queries.loadPublishedAssessment(999999L);
			Assert.assertNull(item);
		} catch (Exception e) {
			Assert.fail("unexpected exception");
		}
	}
}
