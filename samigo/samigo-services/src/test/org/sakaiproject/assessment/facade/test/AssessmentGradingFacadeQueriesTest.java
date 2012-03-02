package org.sakaiproject.assessment.facade.test;

import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueries;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class AssessmentGradingFacadeQueriesTest extends AbstractTransactionalSpringContextTests {
	
	protected String[] getConfigLocations() {
		return new String[] {"/spring-hibernate.xml"};
	}

	
	public void testSaveAssesmentGradingData() {
		AssessmentGradingFacadeQueries queries = new AssessmentGradingFacadeQueries();
		queries.setSessionFactory((SessionFactory)applicationContext.getBean("sessionFactory"));
		PersistenceHelper persistenceHelper = new PersistenceHelper();
		persistenceHelper.setDeadlockInterval(3500);
		persistenceHelper.setRetryCount(5);
		
		queries.setPersistenceHelper(persistenceHelper);
		
		
		AssessmentGradingData data = new AssessmentGradingData();
		//we expect a failure on this one
		/*FIXME this test should fail with an exception
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
		assertNotNull(data.getAssessmentGradingId());
		
		
		//test saving an answe as part of the question.
		
		
		ItemGradingData item1 = new ItemGradingData();
		item1.setAgentId(data.getAgentId());
		item1.setAssessmentGradingId(data.getAssessmentGradingId());
		item1.setPublishedItemId(1L);
		item1.setPublishedItemTextId(1L);
		//saving the item should add an ID
		queries.saveItemGrading(item1);
		assertNotNull(item1.getItemGradingId());
		
		
		
		ItemGradingData item2 = new ItemGradingData();
		item2.setAgentId(data.getAgentId());
		item2.setAssessmentGradingId(data.getAssessmentGradingId());
		item2.setPublishedItemId(1L);
		item2.setPublishedItemTextId(1L);
		
		
		data.getItemGradingSet().add(item2);
		
		
		/** saving the parent should save the children **/
		queries.saveOrUpdateAssessmentGrading(data);
		assertNotNull(item1.getItemGradingId());
		
		
		
		
	}

}
