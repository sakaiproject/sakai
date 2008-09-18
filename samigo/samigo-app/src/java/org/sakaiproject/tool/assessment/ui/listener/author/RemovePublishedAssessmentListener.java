/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemovePublishedAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(RemovePublishedAssessmentListener.class);
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
      RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(assessmentId, "remove");
      thread.start();
      AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
      ArrayList publishedAssessmentList = author.getPublishedAssessments();
      ArrayList list = new ArrayList();
      for (int i=0; i<publishedAssessmentList.size();i++){
    	  PublishedAssessmentFacade pa = (PublishedAssessmentFacade) publishedAssessmentList.get(i);
        if (!(assessmentId).equals(pa.getPublishedAssessmentId().toString())) {
          list.add(pa);
        }
      }
      author.setPublishedAssessments(list);
      
      ArrayList inactivePublishedAssessmentList = author.getInactivePublishedAssessments();
      ArrayList inactiveList = new ArrayList();
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
}
