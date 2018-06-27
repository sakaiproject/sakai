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

package org.sakaiproject.tool.assessment.services;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.tool.assessment.facade.*;
import org.sakaiproject.tool.assessment.facade.authz.AuthorizationFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;

/**
 * @author jlannan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@Slf4j
public class PersistenceService{

	private QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries;
	private TypeFacadeQueriesAPI typeFacadeQueries;
	private SectionFacadeQueriesAPI sectionFacadeQueries;
	private ItemFacadeQueriesAPI itemFacadeQueries;
	private AssessmentFacadeQueriesAPI assessmentFacadeQueries;
	private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
	private PublishedSectionFacadeQueriesAPI publishedSectionFacadeQueries;
	private PublishedItemFacadeQueriesAPI publishedItemFacadeQueries;
	private AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries;
	private AuthorizationFacadeQueriesAPI authorizationFacadeQueries;
	private PagingUtilQueriesAPI pagingUtilQueries;
	private AuthzQueriesFacadeAPI authzQueriesFacade;
	private SectionAwareness sectionAwareness;
	private FavoriteColChoicesFacadeQueriesAPI favoriteColChoicesFacadeQueries;
	private PersistenceHelper persistenceHelper;
	private ExtendedTimeFacade extendedTimeFacade;
	
	
        private EventLogFacadeQueriesAPI eventLogFacadeQueries;  

	public static PersistenceService getInstance(){
	    return (PersistenceService)ComponentManager.get("PersistenceService");
	}

       private Integer deadlockInterval = 1000; // in ms

        public void setDeadlockInterval(Integer deadlockInterval){
          this.deadlockInterval = deadlockInterval;
        }
        public Integer getDeadlockInterval(){
          return deadlockInterval;
        } 

        
	private Integer retryCount = 1000; // in ms
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

	public PersistenceHelper getPersistenceHelper() {
		return persistenceHelper;
	}

	public void setPersistenceHelper(PersistenceHelper persistenceHelper) {
		this.persistenceHelper = persistenceHelper;
	}
	
	public AssessmentFacadeQueriesAPI getAssessmentFacadeQueries(){
	    return assessmentFacadeQueries;
	    //return (AssessmentFacadeQueriesAPI)ComponentManager.get(org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI.class);
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


	public void setPublishedSectionFacadeQueries(PublishedSectionFacadeQueriesAPI publishedSectionFacadeQueries){
	    this.publishedSectionFacadeQueries = publishedSectionFacadeQueries;
	}

	public PublishedSectionFacadeQueriesAPI getPublishedSectionFacadeQueries(){
		return publishedSectionFacadeQueries;
	}
	

	public void setPublishedItemFacadeQueries(PublishedItemFacadeQueriesAPI publishedItemFacadeQueries){
	    this.publishedItemFacadeQueries = publishedItemFacadeQueries;
	}

	public PublishedItemFacadeQueriesAPI getPublishedItemFacadeQueries(){
		return publishedItemFacadeQueries;
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
        log.warn("Error saving to db...retry again....");
        String errorMessage = e.getMessage();
        log.warn(errorMessage);
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
      
      public EventLogFacadeQueriesAPI getEventLogFacadeQueries() {
    	  return eventLogFacadeQueries;
      }

      public void setEventLogFacadeQueries(
    		  EventLogFacadeQueriesAPI eventLogFacadeQueries) {
    	  this.eventLogFacadeQueries = eventLogFacadeQueries;
      }
        

	public void setFavoriteColChoicesFacadeQueries(FavoriteColChoicesFacadeQueriesAPI favoriteColChoicesFacadeQueries){
		this.favoriteColChoicesFacadeQueries = favoriteColChoicesFacadeQueries;
	}

	public FavoriteColChoicesFacadeQueriesAPI getFavoriteColChoicesFacadeQueries(){
		return favoriteColChoicesFacadeQueries;
	}

	public void setExtendedTimeFacade(ExtendedTimeFacade extendedTimeFacade) {
		this.extendedTimeFacade = extendedTimeFacade;
	}

	public ExtendedTimeFacade getExtendedTimeFacade() {
		return extendedTimeFacade;
	}
}




