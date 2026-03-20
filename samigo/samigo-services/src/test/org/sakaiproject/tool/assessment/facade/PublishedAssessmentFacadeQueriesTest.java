/**
 * Copyright (c) 2026 The Apereo Foundation
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

import java.util.Arrays;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublishedAssessmentFacadeQueriesTest {

	@Test
	public void getAssessmentMetaDataEntriesByLabelKeepsLastValueWhenAssessmentIdIsDuplicated() {
		PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
		HibernateTemplate hibernateTemplate = mock(HibernateTemplate.class);
		Session session = mock(Session.class);
		Query<Object[]> query = mock(Query.class);
		queries.setHibernateTemplate(hibernateTemplate);

		when(hibernateTemplate.execute(any(HibernateCallback.class))).thenAnswer(invocation -> {
			HibernateCallback<Map<Long, String>> callback = invocation.getArgument(0);
			return callback.doInHibernate(session);
		});
		when(session.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
		when(query.setParameterList(eq("publishedAssessmentIds"), anyCollection())).thenReturn(query);
		when(query.setParameter(eq("label"), anyString())).thenReturn(query);
		when(query.list()).thenReturn(Arrays.asList(
			new Object[] { 101L, "module-a" },
			new Object[] { 202L, "module-b" },
			new Object[] { 101L, "module-c" }
		));

		Map<Long, String> entries = queries.getAssessmentMetaDataEntriesByLabel(
			Arrays.asList(101L, 202L), "secureDeliveryModule");

		ArgumentCaptor<String> hqlCaptor = ArgumentCaptor.forClass(String.class);
		verify(session).createQuery(hqlCaptor.capture(), eq(Object[].class));

		Assert.assertEquals(2, entries.size());
		Assert.assertEquals("module-c", entries.get(101L));
		Assert.assertEquals("module-b", entries.get(202L));
		Assert.assertTrue(hqlCaptor.getValue().contains("order by m.assessment.publishedAssessmentId asc, m.id asc"));
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void getAssessmentMetaDataEntriesByLabelPropagatesDataAccessExceptions() {
		PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
		HibernateTemplate hibernateTemplate = mock(HibernateTemplate.class);
		queries.setHibernateTemplate(hibernateTemplate);

		when(hibernateTemplate.execute(any(HibernateCallback.class)))
			.thenThrow(new DataAccessResourceFailureException("db failure"));

		queries.getAssessmentMetaDataEntriesByLabel(Arrays.asList(101L), "secureDeliveryModule");
	}
}
