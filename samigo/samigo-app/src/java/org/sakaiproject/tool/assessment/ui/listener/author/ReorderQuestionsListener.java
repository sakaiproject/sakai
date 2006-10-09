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

import java.util.Iterator;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class ReorderQuestionsListener implements ValueChangeListener
{

    private static Log log = LogFactory.getLog(ReorderQuestionsListener.class);

  /**
   * Standard process action method.
   * @param ae ValueChangeEvent
   * @throws AbortProcessingException
   */
  public void processValueChange(ValueChangeEvent ae) throws AbortProcessingException
  {
    log.info("ReorderQuestionsListener valueChangeLISTENER.");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");

    FacesContext context = FacesContext.getCurrentInstance();

    String oldPos= ae.getOldValue().toString();
    String newPos= ae.getNewValue().toString();

//    String itemParam = String.valueOf( ((Integer)ae.getOldValue()).intValue()-1) + ":currItemId";
    String pulldownId  = ae.getComponent().getClientId(context);
    String itemParam = pulldownId.replaceFirst("number","currItemId");
    String itemId= ContextUtil.lookupParam(itemParam);

    if (itemId !=null) {
      // somehow ae.getOldValue() keeps the old value, thus we get itemId==null
    ItemService delegate = new ItemService();
    ItemFacade itemf = delegate.getItem(new Long(itemId), AgentFacade.getAgentString());

    SectionFacade  sectFacade = (SectionFacade) itemf.getSection();
    reorderSequences(sectFacade, new Integer(oldPos), new Integer(newPos));

   // goto editAssessment.jsp, so reset assessmentBean
    AssessmentService assessdelegate = new AssessmentService();
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
    assessmentBean.setAssessment(assessment);

    itemauthorbean.setOutcome("editAssessment");

    }

  }


 /**
  ** shift sequence number down when inserting or reordering
  **/

  private void reorderSequences(SectionFacade sectfacade, Integer oldPos, Integer newPos){

    ItemService delegate = new ItemService();
    Set itemset = sectfacade.getItemFacadeSet();
    Iterator iter = itemset.iterator();
    while (iter.hasNext()) {
      ItemFacade  itemfacade = (ItemFacade) iter.next();
      Integer itemfacadeseq = itemfacade.getSequence();
      if ( (oldPos.compareTo(newPos) < 0) &&  (itemfacadeseq.compareTo(oldPos) > 0) && (itemfacadeseq.compareTo(newPos) <= 0)  ){
        itemfacade.setSequence(new Integer(itemfacadeseq.intValue()-1) );
        delegate.saveItem(itemfacade);
      }
      if ( (oldPos.compareTo(newPos) > 0) &&  (itemfacadeseq.compareTo(newPos) >= 0) && (itemfacadeseq.compareTo(oldPos) < 0)  ){
        itemfacade.setSequence(new Integer(itemfacadeseq.intValue()+1) );
        delegate.saveItem(itemfacade);
      }
      if ( itemfacadeseq.compareTo(oldPos) == 0) {
        itemfacade.setSequence(newPos);
        delegate.saveItem(itemfacade);
      }



    }
  }



}
