/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;


/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class EditPartListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(EditPartListener.class);
  private static ContextUtil cu;

  public EditPartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {

    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
                                                           "assessmentBean");
    SectionBean sectionBean = (SectionBean) cu.lookupBean("sectionBean");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) cu.lookupBean("itemauthor");
    String sectionId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("sectionId");

    //log.info("**SectionId = "+sectionId);
    // #1a. prepare sectionBean
    AssessmentService assessmentService = new AssessmentService();
    SectionFacade section = assessmentService.getSection(sectionId);
    section.setAssessment(assessmentBean.getAssessment());
    sectionBean.setSection(section);
    sectionBean.setSectionTitle(section.getTitle());
    sectionBean.setSectionDescription(section.getDescription());

    sectionBean.setNoOfItems(String.valueOf(section.getItemSet().size()));
    populateMetaData(section, sectionBean);
// todo: get poolsavailable and then add the current pool used, because we need to show it as one of the choices.

/* Huong moved to getPoolsAvailable in SectionBean.java 
    ArrayList poolidlist = sectionBean.getPoolsAvailable();
      String currpoolid= sectionBean.getSelectedPool();   // current pool used for random draw
    if (!("".equals(currpoolid)) && (currpoolid !=null)) {
	//System.out.println("current pool id not null or empty");
    //now we need to get the poolid and displayName
      QuestionPoolService delegate = new QuestionPoolService();//
      QuestionPoolFacade pool= delegate.getPool(new Long(currpoolid), AgentFacade.getAgentString());//
    // now add the current pool used  to the list, so it's available in the pulldown 
      poolidlist.add(new SelectItem((pool.getQuestionPoolId().toString()), pool.getDisplayName()));//
      //System.out.println("added editing pool: "+ pool.getDisplayName());//
      sectionBean.setPoolsAvailable(poolidlist);//
    }
*/

    boolean hideRandom = false;
    if ((sectionBean.getType() == null) || sectionBean.getType().equals(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString()))
{
      int itemsize = Integer.parseInt(sectionBean.getNoOfItems());
      if( itemsize > 0) {
        hideRandom = true;
      }
    }
    sectionBean.setHideRandom(hideRandom);

  }

  private void populateMetaData(SectionFacade section, SectionBean bean)  {

    Set metaDataSet= section.getSectionMetaDataSet();
    Iterator iter = metaDataSet.iterator();
    while (iter.hasNext()){
       SectionMetaData meta= (SectionMetaData) iter.next();
       if (meta.getLabel().equals(SectionMetaDataIfc.OBJECTIVES)){
         bean.setObjective(meta.getEntry());
       }
       if (meta.getLabel().equals(SectionMetaDataIfc.KEYWORDS)){
         bean.setKeyword(meta.getEntry());
       }
       if (meta.getLabel().equals(SectionMetaDataIfc.RUBRICS)){
         bean.setRubric(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.AUTHOR_TYPE)){
         bean.setType(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.QUESTIONS_ORDERING)){
         bean.setQuestionOrdering(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.POOLID_FOR_RANDOM_DRAW)){
         bean.setSelectedPool(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.NUM_QUESTIONS_DRAWN)){
         bean.setNumberSelected(meta.getEntry());
       }
    }
  }

 }

