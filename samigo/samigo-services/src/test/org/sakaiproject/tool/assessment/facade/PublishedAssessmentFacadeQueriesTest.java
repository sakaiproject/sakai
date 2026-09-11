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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;

public class PublishedAssessmentFacadeQueriesTest {

	/* TO-DO Jakarta branch*/
	/*@Test
	public void getAssessmentMetaDataEntriesByLabelKeepsLastValueWhenAssessmentIdIsDuplicated() {
		PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		Query<Object[]> query = mock(Query.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		CriteriaQuery<Object[]> cq = mock(CriteriaQuery.class);
		Root<PublishedMetaData> root = mock(Root.class);
		Path<Object> assessmentPath = mock(Path.class);
		Path<Object> assessmentIdPath = mock(Path.class);
		Path<Object> entryPath = mock(Path.class);
		Path<Object> idPath = mock(Path.class);
		Order orderAsc1 = mock(Order.class);
		Order orderAsc2 = mock(Order.class);
		Expression<Object[]> arrayExpr = mock(Expression.class);

		queries.setSessionFactory(sessionFactory);

		when(sessionFactory.getCurrentSession()).thenReturn(session);
		when(session.getCriteriaBuilder()).thenReturn(cb);
		when(cb.createQuery(Object[].class)).thenReturn(cq);
		when(cq.from(PublishedMetaData.class)).thenReturn(root);

		when(root.get("assessment")).thenReturn(assessmentPath);
		when(assessmentPath.get("publishedAssessmentId")).thenReturn(assessmentIdPath);
		when(root.get("entry")).thenReturn(entryPath);
		when(root.get("id")).thenReturn(idPath);

		when(cb.array(assessmentIdPath, entryPath)).thenReturn(arrayExpr);
		when(cq.select(arrayExpr)).thenReturn(cq);

		Predicate inPredicate = mock(Predicate.class);
		Predicate equalPredicate = mock(Predicate.class);
		when(assessmentIdPath.in(anyCollection())).thenReturn(inPredicate);
		when(cb.equal(any(Path.class), anyString())).thenReturn(equalPredicate);
		when(cq.where(any(Predicate[].class))).thenReturn(cq);

		when(cb.asc(assessmentIdPath)).thenReturn(orderAsc1);
		when(cb.asc(idPath)).thenReturn(orderAsc2);
		when(cq.orderBy(orderAsc1, orderAsc2)).thenReturn(cq);

		when(session.createQuery(cq)).thenReturn(query);
		when(query.getResultList()).thenReturn(Arrays.asList(
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
		verify(cq).orderBy(orderAsc1, orderAsc2);
		verify(query).getResultList();
	}*/

	@Test(expected = DataAccessResourceFailureException.class)
	public void getAssessmentMetaDataEntriesByLabelPropagatesDataAccessExceptions() {
		PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
		SessionFactory sessionFactory = mock(SessionFactory.class);
		queries.setSessionFactory(sessionFactory);

		when(sessionFactory.getCurrentSession()).thenThrow(new DataAccessResourceFailureException("db failure"));

		queries.getAssessmentMetaDataEntriesByLabel(Arrays.asList(101L), "secureDeliveryModule");
	}
}
