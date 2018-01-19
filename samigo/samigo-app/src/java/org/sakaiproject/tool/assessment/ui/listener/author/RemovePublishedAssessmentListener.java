/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class RemovePublishedAssessmentListener
    implements ActionListener
{
  private static final GradebookServiceHelper gbsHelper = IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
  private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();

  public RemovePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    PublishedAssessmentBean pulishedAssessment = (PublishedAssessmentBean) ContextUtil.lookupBean("publishedassessment");
    String assessmentId = pulishedAssessment.getAssessmentId();
    if (assessmentId != null)
    {
      log.debug("assessmentId = " + assessmentId); 	    
      PublishedAssessmentService assessmentService = new PublishedAssessmentService();
      //get assessment to see if it has a calendar event
      PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(assessmentId.toString());

      AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization"); 
      if (!authzBean.isUserAllowedToDeleteAssessment(assessmentId, assessment.getCreatedBy(), true)) {
        throw new IllegalArgumentException("User does not have permission to delete assessmentId " + assessmentId);
      }

      assessmentService.removeAssessment(assessmentId, "remove");
      removeFromGradebook(assessmentId);
      
      String calendarDueDateEventId = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID);
      if(calendarDueDateEventId != null){
    	  calendarService.removeCalendarEvent(AgentFacade.getCurrentSiteId(), calendarDueDateEventId);
      }
      EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REMOVE, "siteId=" + AgentFacade.getCurrentSiteId() + ", publisedAssessmentId=" + assessmentId, true));
      Iterator<PublishedSectionData> sectionDataIterator = assessment.getSectionSet().iterator();
        while (sectionDataIterator.hasNext()){
            PublishedSectionData sectionData = sectionDataIterator.next();
            Iterator<ItemDataIfc> itemDataIfcIterator = sectionData.getItemSet().iterator();
            while (itemDataIfcIterator.hasNext()){
                ItemDataIfc itemDataIfc = itemDataIfcIterator.next();
                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_UNINDEXITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/unindexed, publishedItemId=" + itemDataIfc.getItemIdString(), true));
            }
        }
      
      AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
      List publishedAssessmentList = author.getPublishedAssessments();
      List list = new ArrayList();
      for (int i=0; i<publishedAssessmentList.size();i++){
    	  PublishedAssessmentFacade pa = (PublishedAssessmentFacade) publishedAssessmentList.get(i);
        if (!(assessmentId).equals(pa.getPublishedAssessmentId().toString())) {
          list.add(pa);
        }
      }
      author.setPublishedAssessments(list);
      
      List inactivePublishedAssessmentList = author.getInactivePublishedAssessments();
      List inactiveList = new ArrayList();
      for (int i=0; i<inactivePublishedAssessmentList.size();i++){
    	  PublishedAssessmentFacade pa = (PublishedAssessmentFacade) inactivePublishedAssessmentList.get(i);
        if (!(assessmentId).equals(pa.getPublishedAssessmentId().toString())) {
        	inactiveList.add(pa);
        }
      }
      author.setInactivePublishedAssessments(inactiveList);
      boolean isAnyAssessmentRetractForEdit = false;
	  Iterator iter = inactiveList.iterator();
	  while (iter.hasNext()) {
		  PublishedAssessmentFacade publishedAssessmentFacade = (PublishedAssessmentFacade) iter.next();
			if (Integer.valueOf(3).equals(publishedAssessmentFacade.getStatus())) {
			  isAnyAssessmentRetractForEdit = true;
			  break;
		  }
	  }
	  if (isAnyAssessmentRetractForEdit) {
		  author.setIsAnyAssessmentRetractForEdit(true);
	  }
	  else {
		  author.setIsAnyAssessmentRetractForEdit(false);
	  }
    }
    else {
    	log.warn("Could not remove published assessment - assessment id is null");
    }
  }
  
  private void removeFromGradebook(String assessmentId) {
	  GradebookExternalAssessmentService g = null;
	  if (integrated)
	  {
		  g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().
		  getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
	  }
	  try {
		  log.debug("before gbsHelper.removeGradebook()");
		  gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessmentId, g);
	  } catch (Exception e1) {
		  // Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
		  log.info("Exception thrown in updateGB():" + e1.getMessage());
	  }
  }
}
