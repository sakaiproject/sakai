/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.ItemFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.SectionFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.authz.AuthorizationFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.api.section.SectionAwareness;

/**
 * @author jlannan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PersistenceService{

        private static Log log = LogFactory.getLog(PersistenceService.class);
	private QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries;
	private TypeFacadeQueriesAPI typeFacadeQueries;
	private SectionFacadeQueriesAPI sectionFacadeQueries;
	private ItemFacadeQueriesAPI itemFacadeQueries;
	private AssessmentFacadeQueriesAPI assessmentFacadeQueries;
	private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
	private AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries;
        private AuthorizationFacadeQueriesAPI authorizationFacadeQueries;
        private PagingUtilQueriesAPI pagingUtilQueries;
        private AuthzQueriesFacadeAPI authzQueriesFacade;
        private SectionAwareness sectionAwareness;

	private static PersistenceService INSTANCE;

	public static PersistenceService getInstance(){
          if (INSTANCE != null){
            return INSTANCE;
          }
          else{
            SpringBeanLocator locator = SpringBeanLocator.getInstance();
            return INSTANCE = (PersistenceService)locator.getBean("PersistenceService");
	    //return INSTANCE = (PersistenceService) ApplicationContextLocator.getInstance().getBean("PersistenceService");
          }
	}

        private Integer deadlockInterval; // in ms

        public void setDeadlockInterval(Integer deadlockInterval){
          this.deadlockInterval = deadlockInterval;
        }

        public Integer getDeadlockInterval(){
          return deadlockInterval;
        }

        private Integer retryCount; // in ms

        public void setRetryCount(Integer retryCount){
          this.retryCount = retryCount;
        }

        public Integer getRetryCount(){
          return retryCount;
        }


	public QuestionPoolFacadeQueriesAPI getQuestionPoolFacadeQueries(){
		return questionPoolFacadeQueries;
	}

	public void setQuestionPoolFacadeQueries(QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries){
	        this.questionPoolFacadeQueries = questionPoolFacadeQueries;
	}

	public TypeFacadeQueriesAPI getTypeFacadeQueries(){
		return typeFacadeQueries;
	}

	public void setTypeFacadeQueries(TypeFacadeQueriesAPI typeFacadeQueries){
	    this.typeFacadeQueries = typeFacadeQueries;
	}

	public SectionFacadeQueriesAPI getSectionFacadeQueries(){
		return sectionFacadeQueries;
	}

	public void setSectionFacadeQueries(SectionFacadeQueriesAPI sectionFacadeQueries){
	    this.sectionFacadeQueries = sectionFacadeQueries;
	}

	public ItemFacadeQueriesAPI getItemFacadeQueries(){
		return itemFacadeQueries;
	}

	public void setItemFacadeQueries(ItemFacadeQueriesAPI itemFacadeQueries){
	    this.itemFacadeQueries = itemFacadeQueries;
	}

	public AssessmentFacadeQueriesAPI getAssessmentFacadeQueries(){
		return assessmentFacadeQueries;
	}

	public void setAssessmentFacadeQueries(AssessmentFacadeQueriesAPI assessmentFacadeQueries){
	    this.assessmentFacadeQueries = assessmentFacadeQueries;
	}

	public PublishedAssessmentFacadeQueriesAPI getPublishedAssessmentFacadeQueries(){
		return publishedAssessmentFacadeQueries;
	}

	public void setPublishedAssessmentFacadeQueries(PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries){
	    this.publishedAssessmentFacadeQueries = publishedAssessmentFacadeQueries;
	}

	public AssessmentGradingFacadeQueriesAPI getAssessmentGradingFacadeQueries(){
		return assessmentGradingFacadeQueries;
	}

	public void setAssessmentGradingFacadeQueries(AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries){
	    this.assessmentGradingFacadeQueries = assessmentGradingFacadeQueries;
	}

        public AuthorizationFacadeQueriesAPI getAuthorizationFacadeQueries(){
	  return authorizationFacadeQueries;
        }

        public void setAuthorizationFacadeQueries(AuthorizationFacadeQueriesAPI authorizationFacadeQueries){
	  this.authorizationFacadeQueries = authorizationFacadeQueries;
        }

        public PagingUtilQueriesAPI getPagingUtilQueries(){
	  return pagingUtilQueries;
        }

        public void setPagingUtilQueries(PagingUtilQueriesAPI pagingUtilQueries){
	  this.pagingUtilQueries = pagingUtilQueries;
        }

        public AuthzQueriesFacadeAPI getAuthzQueriesFacade(){
	  return authzQueriesFacade;
        }

        public void setAuthzQueriesFacade(AuthzQueriesFacadeAPI authzQueriesFacade){
	  this.authzQueriesFacade = authzQueriesFacade;
        }

	public SectionAwareness getSectionAwareness()
	{
	  return sectionAwareness;

 	}


	public void setSectionAwareness(SectionAwareness sectionAwareness)
	{
	  this.sectionAwareness = sectionAwareness;
	}


      public int retryDeadlock(Exception e, int retryCount){
        log.warn("calling retryDeadlock() ....");
        String errorMessage = e.getMessage();
        int index = errorMessage.indexOf("ORA-00060"); // deadlock
        int index2 = errorMessage.indexOf("SQL state [61000]"); // oracle deadlock
        int index3 = errorMessage.indexOf("SQL state [41000]"); // mysql deadlock
        if (index > -1 || index2 > -1 || index3 > -1){
          retryCount--;
          try {
            int ideadlockInterval = deadlockInterval.intValue();
            Thread.currentThread().sleep(ideadlockInterval);
          }
          catch(InterruptedException ex){
            log.warn(ex.getMessage());
          }
        }
        else retryCount = 0;
     return retryCount;
   }
        

}




