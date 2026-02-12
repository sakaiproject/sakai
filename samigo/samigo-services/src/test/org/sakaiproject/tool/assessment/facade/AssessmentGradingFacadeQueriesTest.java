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


package org.sakaiproject.tool.assessment.facade;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueries;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations={"/spring-hibernate.xml"})
public class AssessmentGradingFacadeQueriesTest extends AbstractJUnit4SpringContextTests {

	/** our query object */
	@Autowired
	@Qualifier("assessmentGradingFacadeQueries")
	private AssessmentGradingFacadeQueries queries;

	Long savedId = null;
	Long item1Id = null;
	Long item2Id = null;
	
	@Before
	public void onSetUpInTransaction() throws Exception {
		//Set the persistance helper
		PersistenceHelper persistenceHelper = new PersistenceHelper();
		persistenceHelper.setDeadlockInterval(3500);
		persistenceHelper.setRetryCount(5);
		queries.setPersistenceHelper(persistenceHelper);
	}

	@Test
	public void testSaveAssesmentGradingData() {
		//A AssemementGradingData to work with
		AssessmentGradingData data = new AssessmentGradingData();

		//we expect a failure on this one
		/*FIXME this test should fail with an exception
		 * currently the exceptions are quietly swallowed
		try {
			queries.saveOrUpdateAssessmentGrading(data);
			fail();
		}
		catch (Exception e) {
			//we expect this 
		}
		*/

		data.setPublishedAssessmentId(Long.valueOf(1));
		data.setAgentId("agent");
		data.setIsLate(false);
		data.setForGrade(false);
		data.setStatus(Integer.valueOf(0));

		queries.saveOrUpdateAssessmentGrading(data);
		Assert.assertNotNull(data.getAssessmentGradingId());

		//test saving an answer as part of the question.

		ItemGradingData item1 = new ItemGradingData();
		item1.setAgentId(data.getAgentId());
		item1.setAssessmentGradingId(data.getAssessmentGradingId());
		item1.setPublishedItemId(1L);
		item1.setPublishedItemTextId(1L);
		//saving the item should add an ID
		queries.saveItemGrading(item1);
		Assert.assertNotNull(item1.getItemGradingId());

		ItemGradingData item2 = new ItemGradingData();
		item2.setAgentId(data.getAgentId());
		item2.setAssessmentGradingId(data.getAssessmentGradingId());
		item2.setPublishedItemId(1L);
		item2.setPublishedItemTextId(1L);

		data.getItemGradingSet().add(item2);

		/** saving the parent should save the children **/
		queries.saveOrUpdateAssessmentGrading(data);
		Assert.assertNotNull(item1.getItemGradingId());
		
		Set set = data.getItemGradingSet();
		set.add(null);
		try {
		queries.saveOrUpdateAll(set);
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testLoad() {
		loadData();

		AssessmentGradingData result = queries.load(savedId);
		Assert.assertNotNull(result);
		Assert.assertEquals(result.getItemGradingSet().size(), 2);

		List<AssessmentGradingData> subs = queries.getAllSubmissions("1");
		Assert.assertNotNull(subs);
		Assert.assertEquals(2, subs.size());
	}

	/**
	 * Load some test data
	 */
	private void loadData() {
		//set up some data
		AssessmentGradingData data = new AssessmentGradingData();
		data.setPublishedAssessmentId(Long.valueOf(1));
		data.setAgentId("agent");
		data.setIsLate(false);
		data.setForGrade(false);
		data.setStatus(Integer.valueOf(0));
		queries.saveOrUpdateAssessmentGrading(data);

		AssessmentGradingData data2 = new AssessmentGradingData();
		data2.setPublishedAssessmentId(Long.valueOf(1));
		data2.setAgentId("agent2");
		data2.setIsLate(false);
		data2.setForGrade(true);
		data2.setStatus(0);
		queries.saveOrUpdateAssessmentGrading(data2);

		AssessmentGradingData data3 = new AssessmentGradingData();
		data3.setPublishedAssessmentId(Long.valueOf(1));
		data3.setAgentId("agent3");
		data3.setIsLate(false);
		data3.setForGrade(true);
		data3.setStatus(0);
		queries.saveOrUpdateAssessmentGrading(data3);

		ItemGradingData item1 = new ItemGradingData();
		item1.setAgentId(data.getAgentId());
		item1.setAssessmentGradingId(data.getAssessmentGradingId());
		item1.setPublishedItemId(1L);
		item1.setPublishedItemTextId(1L);

		ItemGradingData item2 = new ItemGradingData();
		item2.setAgentId(data.getAgentId());
		item2.setAssessmentGradingId(data.getAssessmentGradingId());
		item2.setPublishedItemId(2L);
		item2.setPublishedItemTextId(2L);

		data.getItemGradingSet().add(item2);
		data.getItemGradingSet().add(item1);

		queries.saveOrUpdateAssessmentGrading(data);

		savedId = data.getAssessmentGradingId();
	}
	
	@Test
	public void testGetAllItemGradingDataForItemInGrading() {
		try {
			queries.getAllItemGradingDataForItemInGrading(null , null);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			//we expect this
		} catch (Exception ex) {
			Assert.fail();
		}

		try {
			queries.getAllItemGradingDataForItemInGrading(999l , null);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			//we expect this
		} catch (Exception ex) {
			Assert.fail();
		}

		try {
			queries.getAllItemGradingDataForItemInGrading(null , 999l);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			//we expect this
		} catch (Exception ex) {
			Assert.fail();
		}

		List<ItemGradingData> vals = queries.getAllItemGradingDataForItemInGrading(999l , 999l);
		Assert.assertNotNull(vals);
		Assert.assertEquals(0, vals.size());

		loadData();

		vals = queries.getAllItemGradingDataForItemInGrading(savedId , 2L);
		Assert.assertNotNull(vals);
		Assert.assertEquals(1, vals.size());
	}

	@Test
	public void testResolveOneSelectionCorrectnessPrefersAnswerFlag() {
		PublishedAnswer answer = new PublishedAnswer();
		answer.setIsCorrect(Boolean.TRUE);

		ItemGradingData grade = new ItemGradingData();
		grade.setIsCorrect(Boolean.FALSE);
		grade.setAutoScore(0d);

		Boolean result = queries.resolveOneSelectionCorrectness(answer, grade);
		Assert.assertEquals(Boolean.TRUE, result);
	}

	@Test
	public void testResolveOneSelectionCorrectnessFallsBackToGradeFlag() {
		PublishedAnswer answer = new PublishedAnswer();
		answer.setIsCorrect(null);

		ItemGradingData grade = new ItemGradingData();
		grade.setIsCorrect(Boolean.FALSE);

		Boolean result = queries.resolveOneSelectionCorrectness(answer, grade);
		Assert.assertEquals(Boolean.FALSE, result);
	}

	@Test
	public void testResolveOneSelectionCorrectnessFallsBackToAutoScore() {
		PublishedAnswer answer = new PublishedAnswer();
		answer.setIsCorrect(null);

		ItemGradingData grade = new ItemGradingData();
		grade.setIsCorrect(null);
		grade.setAutoScore(1d);

		Boolean result = queries.resolveOneSelectionCorrectness(answer, grade);
		Assert.assertEquals(Boolean.TRUE, result);
	}

	@Test
	public void testResolveOneSelectionCorrectnessReturnsNullWhenUnknown() {
		PublishedAnswer answer = new PublishedAnswer();
		answer.setIsCorrect(null);

		ItemGradingData grade = new ItemGradingData();
		grade.setIsCorrect(null);
		grade.setAutoScore(null);

		Boolean result = queries.resolveOneSelectionCorrectness(answer, grade);
		Assert.assertNull(result);
	}
}
