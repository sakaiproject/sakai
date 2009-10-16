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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.util.FormattedText;



/**
 * <p>
 * This handles the selection of the Student Score page.
 *  </p>
 * <p>Description: Action Listener for Evaluation Student Score page</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Rachel Gollub
 * @version $Id$
 */

public class StudentScoreListener
  implements ActionListener
{
  private static Log log = LogFactory.getLog(StudentScoreListener.class);
  private static EvaluationListenerUtil util;
  private static BeanSort bs;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("StudentScore LISTENER.");
    StudentScoresBean bean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");

    // we probably want to change the poster to be consistent
    String publishedId = ContextUtil.lookupParam("publishedIdd");
    
    log.debug("Calling studentScores.");
    if (!studentScores(publishedId, bean, false))
    {
      throw new RuntimeException("failed to call studentScores.");
    }

  }

  /**
   * This will populate the StudentScoresBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * @param publishedId String
   * @param bean StudentScoresBean
   * @return boolean
   */
  public boolean studentScores(
    String publishedId, StudentScoresBean bean, boolean isValueChange)
  {
    log.debug("studentScores()");
    try
    {
//  SAK-4121, do not pass studentName as f:param, will cause javascript error if name contains apostrophe 
//    bean.setStudentName(cu.lookupParam("studentName"));


      bean.setPublishedId(publishedId);
      String studentId = ContextUtil.lookupParam("studentid");
      bean.setStudentId(studentId);
      AgentFacade agent = new AgentFacade(studentId);
      bean.setStudentName(agent.getFirstName() + " " + agent.getLastName());
      bean.setLastName(agent.getLastName());
      bean.setFirstName(agent.getFirstName());
      bean.setAssessmentGradingId(ContextUtil.lookupParam("gradingData"));
      bean.setItemId(ContextUtil.lookupParam("itemId"));
      String email = ContextUtil.lookupParam("email");
      bean.setEmail(email);
      
      DeliveryBean dbean = (DeliveryBean) ContextUtil.lookupBean("delivery");
      dbean.setActionString("gradeAssessment");

      GradingService service = new GradingService();
      AssessmentGradingData adata= (AssessmentGradingData) service.load(bean.getAssessmentGradingId());

      DeliveryActionListener listener = new DeliveryActionListener();
      listener.processAction(null);
      
      // Added for SAK-13930
      DeliveryBean updatedDeliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
      ArrayList parts = updatedDeliveryBean.getPageContents().getPartsContents();
      Iterator iter = parts.iterator();
      while (iter.hasNext())
      {
          ArrayList items = ((SectionContentsBean) iter.next()).getItemContents();
          Iterator iter2 = items.iterator();
          while (iter2.hasNext())
          {
        	  ItemContentsBean question = (ItemContentsBean) iter2.next();
        	  if (question.getGradingComment() != null && !question.getGradingComment().equals("")) {
        		  question.setGradingComment(FormattedText.convertFormattedTextToPlaintext(question.getGradingComment()));
        	  }
          }
      } // End of SAK-13930

      bean.setComments(FormattedText.convertFormattedTextToPlaintext(adata.getComments()));
      buildItemContentsMap(dbean);

      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  private void buildItemContentsMap(DeliveryBean dbean) {
	  HashMap itemContentsMap = new HashMap();
	  ArrayList partsContents = dbean.getPageContents().getPartsContents();
	  if (partsContents != null) {
		  Iterator iter = partsContents.iterator();
		  while (iter.hasNext()) {
			  SectionContentsBean sectionContentsBean = (SectionContentsBean) iter.next();
			  if (sectionContentsBean != null) {
				  ArrayList itemContents = sectionContentsBean.getItemContents();
				  Iterator iter2 = itemContents.iterator();
				  while (iter2.hasNext()) {
					  ItemContentsBean itemContentsBean = (ItemContentsBean) iter2.next();
					  if (itemContentsBean != null) {
						  ArrayList itemGradingDataArray = itemContentsBean.getItemGradingDataArray();
						  Iterator iter3 = itemGradingDataArray.iterator();
						  while (iter3.hasNext()) {
							  ItemGradingData itemGradingData = (ItemGradingData) iter3.next();
							  itemContentsMap.put(itemGradingData.getItemGradingId(), itemContentsBean);
						  }
					  }
				  }
			  }
		  }  
	  }
	  dbean.setItemContentsMap(itemContentsMap);
  }
}
