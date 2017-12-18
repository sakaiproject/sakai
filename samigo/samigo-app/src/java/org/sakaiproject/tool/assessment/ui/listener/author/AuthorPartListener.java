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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class AuthorPartListener implements ActionListener
{

  public AuthorPartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    // #1a. prepare sectionBean
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
                         "assessmentBean");
    SectionBean sectionBean = (SectionBean) ContextUtil.lookupBean(
                                          "sectionBean");
    // clean it
    sectionBean.setSectionTitle("");
    sectionBean.setAssessmentTitle(assessmentBean.getTitle());
    sectionBean.setSectionDescription("");
    sectionBean.setSectionId("");
    sectionBean.setSection(null);

    // #1b. goto editPart.jsp
    //sectionBean.setPoolsAvailable(itemauthorbean.getPoolSelectList());
    sectionBean.setHideRandom(false);
    sectionBean.setNumberSelected("");
    sectionBean.setRandomizationType(SectionDataIfc.PER_SUBMISSION);
    sectionBean.setSelectedPool("");
    
    sectionBean.setPointValueHasOverrided(false);
    sectionBean.setRandomPartScore(null);
    sectionBean.setDiscountValueHasOverrided(false);
    sectionBean.setRandomPartDiscount(null);
    // new part has no attachment, VERY IMPORTANT to clean up any leftover
    // before modifying a new part
    sectionBean.setResourceHash(null);
    sectionBean.setAttachmentList(null);
    sectionBean.setHasAttachment(false);
    sectionBean.setKeyword(null);
    sectionBean.setObjective(null);
    sectionBean.setRubric(null);
    // set default
    sectionBean.setType(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
    sectionBean.setQuestionOrdering(SectionDataIfc.AS_LISTED_ON_ASSESSMENT_PAGE.toString());
    log.debug("**** sectionBean.getTitle="+sectionBean.getSectionTitle());
  }

}
