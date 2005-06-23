/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Iterator;
import java.util.Map;
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
    //System.out.println("lydiatest BEGIN ReorderQuestionsListener processValueChange ------  ");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");

    FacesContext context = FacesContext.getCurrentInstance();

//debugging
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    //System.out.println("debugging ActionEvent: " + ae);
    //System.out.println("debug requestParams: " + requestParams);
    //System.out.println("debug reqMap: " + reqMap);

//debugging




    String oldPos= ae.getOldValue().toString();
    //System.out.println("lydiatest ae.getOldValue : " + oldPos);
    String newPos= ae.getNewValue().toString();
    //System.out.println("lydiatest ae.getNewValue : " + newPos);

//    String itemParam = String.valueOf( ((Integer)ae.getOldValue()).intValue()-1) + ":currItemId";
    String pulldownId  = ae.getComponent().getClientId(context);
    String itemParam = pulldownId.replaceFirst("number","currItemId");
    //System.out.println("lydiatest itemParam =" + itemParam);
    String itemId= ContextUtil.lookupParam(itemParam);
    //System.out.println("lydiatest itemid : " + itemId);

    if (itemId !=null) {
      // somehow ae.getOldValue() keeps the old value, thus we get itemId==null

    ItemFacade itemf = new ItemFacade();
    ItemService delegate = new ItemService();
    itemf = delegate.getItem(new Long(itemId), AgentFacade.getAgentString());

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
    //System.out.println("lydiatest item itemset size is " + itemset.size());
    Iterator iter = itemset.iterator();
    while (iter.hasNext()) {
      ItemFacade  itemfacade = (ItemFacade) iter.next();
      Integer itemfacadeseq = itemfacade.getSequence();
      //System.out.println("lydiatest shifting orig seq = " + itemfacadeseq);
      if ( (oldPos.compareTo(newPos) < 0) &&  (itemfacadeseq.compareTo(oldPos) > 0) && (itemfacadeseq.compareTo(newPos) <= 0)  ){
        itemfacade.setSequence(new Integer(itemfacadeseq.intValue()-1) );
        //System.out.println("lydiatest shift to = " + itemfacade.getSequence());
        delegate.saveItem(itemfacade);
      }
      if ( (oldPos.compareTo(newPos) > 0) &&  (itemfacadeseq.compareTo(newPos) >= 0) && (itemfacadeseq.compareTo(oldPos) < 0)  ){
        itemfacade.setSequence(new Integer(itemfacadeseq.intValue()+1) );
        //System.out.println("lydiatest shift to = " + itemfacade.getSequence());
        delegate.saveItem(itemfacade);
      }
      if ( itemfacadeseq.compareTo(oldPos) == 0) {
        itemfacade.setSequence(newPos);
        //System.out.println("lydiatest shift to = " + itemfacade.getSequence());
        delegate.saveItem(itemfacade);
      }



    }
  }



}
