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
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AuthorPartListener implements ActionListener
{
  private static Log log = LogFactory.getLog(AuthorPartListener.class);
  private static ContextUtil cu;

  public AuthorPartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    // #1a. prepare sectionBean
    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
                         "assessmentBean");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) cu.lookupBean("itemauthor");

    SectionBean sectionBean = (SectionBean) cu.lookupBean(
                                          "sectionBean");
    // clean it
    sectionBean.setSectionTitle("");
    sectionBean.setAssessmentTitle(assessmentBean.getTitle());
    sectionBean.setSectionDescription("");
    sectionBean.setSectionId("");
    String assessmentId = assessmentBean.getAssessmentId();

    // #1b. goto editPart.jsp
    //sectionBean.setPoolsAvailable(itemauthorbean.getPoolSelectList());
    sectionBean.setHideRandom(false);
    sectionBean.setNumberSelected("");
    sectionBean.setSelectedPool("");
    // set default
    sectionBean.setType(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
    sectionBean.setQuestionOrdering(SectionDataIfc.AS_LISTED_ON_ASSESSMENT_PAGE.toString());
    log.debug("**** sectionBean.getTitle="+sectionBean.getSectionTitle());
  }

}
