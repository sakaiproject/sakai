/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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


}




