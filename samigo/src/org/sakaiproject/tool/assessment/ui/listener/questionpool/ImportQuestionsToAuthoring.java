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

package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class ImportQuestionsToAuthoring implements ActionListener
{
  private static Log log = LogFactory.getLog(ImportQuestionsToAuthoring.class);
  private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.info("ImportQuestionsToAuthoring:");
    //System.out.println("lydiatest in ImportQuestionsToAuthoring");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) cu.lookupBean("questionpool");
    if (!importItems(qpoolbean))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }
    qpoolbean.setImportToAuthoring(false);

  }


  public boolean importItems(QuestionPoolBean qpoolbean){
    //System.out.println("lydiatest in importItems");
    try {
      AssessmentService assessdelegate = new AssessmentService();
      ItemService delegate = new ItemService();
      SectionService sectiondelegate = new SectionService();
      AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean("assessmentBean");
      ItemAuthorBean itemauthor = (ItemAuthorBean) cu.lookupBean("itemauthor");
      int itempos= 0;
      SectionFacade section = null;
      ItemFacade itemfacade = new ItemFacade();

      //System.out.println("lydiatest before cloning itemfacade's  address for itemfacade = " + itemfacade);
      String itemId= "";

      ArrayList destItems= ContextUtil.paramArrayValueLike("importCheckbox");

      if (destItems.size() > 0) {

      //System.out.println("lydiatest destItem.size() = " + destItems.size());
      List items= new ArrayList();
      Iterator iter = destItems.iterator();
      while(iter.hasNext())
      {
        itemId = (String) iter.next();
        //System.out.println("lydiatest itemId = " + itemId);
        ItemFacade poolitemfacade= delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
        //System.out.println("lydiatest poolitemfacade's itemId = " + itemId);
        //System.out.println("lydiatest printing out address for poolitemfacade = " + poolitemfacade);
        //System.out.println("lydiatest printing out address for poolitemfacade's itemtext = " + poolitemfacade.getItemTextSet());

        itemfacade = (ItemFacade) poolitemfacade.clone();
        //System.out.println("lydiatest after cloning , itemfacade's itemId = " + itemfacade.getItemId());
        //System.out.println("lydiatest after cloning printing out address for itemfacade = " + itemfacade);
        //System.out.println("lydiatest printing out address for itemfacade's itemtext = " + itemfacade.getItemTextSet());


    	//System.out.println("lydiatest  assessmment bean id " + assessmentBean.getAssessmentId() );
        AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
	section = sectiondelegate.getSection(new Long(qpoolbean.getSelectedSection()), AgentFacade.getAgentString());
        if (section!=null) {
    	  //System.out.println("lydiatest  section id != null" + section.getSectionId() );
          itemfacade.setSection(section);


          if ( (itemauthor.getInsertPosition() ==null) || ("".equals(itemauthor.getInsertPosition())) ) {
//System.out.println("lydiatest add at the end "+  itemauthor.getInsertPosition()+ "." );
                // if adding to the end
                itemfacade.setSequence(new Integer(section.getItemSet().size()+1));
              }
              else {
//System.out.println("lydiatest insert,needs shifting "+  itemauthor.getInsertPosition()+ "." );
                // if inserting or a question
                String insertPos = itemauthor.getInsertPosition();
                ItemAddListener itemAddListener = new ItemAddListener();
                int insertPosIntvalue = new Integer(insertPos).intValue() + itempos;
                itemAddListener.shiftSequences(section, new Integer(insertPosIntvalue));
                int insertPosInt= insertPosIntvalue + 1 ;
                itemfacade.setSequence(new Integer(insertPosInt));
              }


          delegate.saveItem(itemfacade);
        //System.out.println("lydiatest after saveItem , itemfacade's itemId = " + itemfacade.getItemId());
          // remove POOLID metadata if any,
          delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.POOLID, AgentFacade.getAgentString());
          delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID, AgentFacade.getAgentString());

          //itemfacade.addItemMetaData(ItemMetaData.PARTID, section.getSectionId().toString());
          //delegate.saveItem(itemfacade);

          delegate.addItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID,section.getSectionId().toString(),  AgentFacade.getAgentString());

        }
	itempos= itempos+1;   // for next item in the destItem.
    	//System.out.println("lydiatest  finished addItem" );
      }

      // reset InsertPosition
      itemauthor.setInsertPosition("");

   //TODO need to reset assessments.
      AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
      assessmentBean.setAssessment(assessment);

      qpoolbean.setOutcome("editAssessment");
      }
      else {
      // nothing is checked
      qpoolbean.setOutcome("editPool");
      }
    }
    catch (Exception e) {
	e.printStackTrace();
	return false;
    }

    return true;
  }

}
