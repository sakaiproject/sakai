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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCompoundSelection;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaOrder;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaRoot;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.springframework.dao.DataAccessResourceFailureException;

public class PublishedAssessmentFacadeQueriesTest {

	@Test
	public void getAssessmentMetaDataEntriesByLabelKeepsLastValueWhenAssessmentIdIsDuplicated() {
		PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		Query<Object[]> query = mock(Query.class);
		HibernateCriteriaBuilder cb = mock(HibernateCriteriaBuilder.class);
		JpaCriteriaQuery<Object[]> cq = mock(JpaCriteriaQuery.class);
		JpaRoot<PublishedMetaData> root = mock(JpaRoot.class);
		JpaPath<Object> assessmentPath = mock(JpaPath.class);
		JpaPath<Object> assessmentIdPath = mock(JpaPath.class);
		JpaPath<Object> entryPath = mock(JpaPath.class);
		JpaPath<Object> idPath = mock(JpaPath.class);
		JpaPath<Object> labelPath = mock(JpaPath.class);
		JpaOrder orderAsc1 = mock(JpaOrder.class);
		JpaOrder orderAsc2 = mock(JpaOrder.class);
		JpaCompoundSelection<Object[]> arrayExpr = mock(JpaCompoundSelection.class);

		queries.setSessionFactory(sessionFactory);

		when(sessionFactory.getCurrentSession()).thenReturn(session);
		when(session.getCriteriaBuilder()).thenReturn(cb);
		when(cb.createQuery(Object[].class)).thenReturn(cq);
		when(cq.from(PublishedMetaData.class)).thenReturn(root);

		when(root.get("assessment")).thenReturn(assessmentPath);
		when(assessmentPath.get("publishedAssessmentId")).thenReturn(assessmentIdPath);
		when(root.get("entry")).thenReturn(entryPath);
		when(root.get("id")).thenReturn(idPath);
		when(root.get("label")).thenReturn(labelPath);

		when(cb.array(assessmentIdPath, entryPath)).thenReturn(arrayExpr);
		when(cq.select(arrayExpr)).thenReturn(cq);

		JpaPredicate inPredicate = mock(JpaPredicate.class);
		JpaPredicate equalPredicate = mock(JpaPredicate.class);
		when(assessmentIdPath.in(Arrays.asList(101L, 202L))).thenReturn(inPredicate);
		when(cb.equal(labelPath, "secureDeliveryModule")).thenReturn(equalPredicate);
		when(cq.where(inPredicate, equalPredicate)).thenReturn(cq);

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

		verify(session).createQuery(cq);

		Assert.assertEquals(2, entries.size());
		Assert.assertEquals("module-c", entries.get(101L));
		Assert.assertEquals("module-b", entries.get(202L));
		verify(cq).where(inPredicate, equalPredicate);
		verify(cq).orderBy(orderAsc1, orderAsc2);
		verify(query).getResultList();
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void getAssessmentMetaDataEntriesByLabelPropagatesDataAccessExceptions() {
		PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
		SessionFactory sessionFactory = mock(SessionFactory.class);
		queries.setSessionFactory(sessionFactory);

		when(sessionFactory.getCurrentSession()).thenThrow(new DataAccessResourceFailureException("db failure"));

		queries.getAssessmentMetaDataEntriesByLabel(Arrays.asList(101L), "secureDeliveryModule");
	}
}
