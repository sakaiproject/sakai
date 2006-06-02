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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class ReorderPartsListener
    implements ValueChangeListener
{
    private static Log log = LogFactory.getLog(ReorderPartsListener.class);

  /**
   * Standard process action method.
   * @param ae ValueChangeEvent
   * @throws AbortProcessingException
   */
  public void processValueChange(ValueChangeEvent ae) throws AbortProcessingException
  {
    //log.info("ReorderQuestionsListener valueChangeLISTENER.");
    SectionContentsBean partBean  = (SectionContentsBean) ContextUtil.lookupBean("partBean");
    AssessmentBean assessmentBean  = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

    FacesContext context = FacesContext.getCurrentInstance();

    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    String oldPos= ae.getOldValue().toString();
    //log.info("**** ae.getOldValue : " + oldPos);
    String newPos= ae.getNewValue().toString();
    //log.info("**** ae.getNewValue : " + newPos);

    // get sections with oldPos
    AssessmentFacade assessment = assessmentBean.getAssessment();
    SectionFacade section1 = (SectionFacade) assessment.getSection(new Long(oldPos));
    SectionFacade section2 = (SectionFacade) assessment.getSection(new Long(newPos));
    if (section1!=null && section2!=null){
      section1.setSequence(new Integer(newPos));
      section2.setSequence(new Integer(oldPos));
      AssessmentService service = new AssessmentService();
      service.saveOrUpdateSection(section1);
      service.saveOrUpdateSection(section2);
    }

   // goto editAssessment.jsp, so reset assessmentBean
    assessmentBean.setAssessment(assessment);
  }
}
