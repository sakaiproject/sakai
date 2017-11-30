/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * <p>
 * This handles the updating of the Question Score page.
 *  </p>
 * <p>Description: Action Listener Evaluation Updating Question Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class QuestionScorePagerListener
  implements ActionListener, ValueChangeListener
{
  //private static EvaluationListenerUtil util;
  //private static BeanSort bs;
  //private static ContextUtil cu;

  /**
   * Standard process action method.
   * @param event ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent event) throws
    AbortProcessingException
  {
    log.debug("processAction");
    QuestionScoresBean bean = (QuestionScoresBean)ContextUtil.lookupBean("questionScores");
    setMaxDisplayedScoreRows(bean, false);
  }
  
  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    log.debug("processValueChange");
    QuestionScoresBean bean = (QuestionScoresBean)ContextUtil.lookupBean("questionScores");
    setMaxDisplayedScoreRows(bean, true);
  }

  private void setMaxDisplayedScoreRows(QuestionScoresBean bean, boolean isValueChange) {
	  PublishedAssessmentService pubService  = new PublishedAssessmentService();
      String itemId = ContextUtil.lookupParam("itemId");
      if (ContextUtil.lookupParam("newItemId") != null && !ContextUtil.lookupParam("newItemId").trim().equals("")) {
    	  itemId = ContextUtil.lookupParam("newItemId");
      }
      Long itemType = pubService.getItemType(itemId);
      // For audiio question, default the paging number to 5
	  if (isValueChange) {
		  if (itemType.equals(Long.valueOf("7"))){
			  bean.setAudioMaxDisplayedScoreRows(bean.getMaxDisplayedRows());
			  bean.setHasAudioMaxDisplayedScoreRowsChanged(true);
		  }
		  else {
			  bean.setOtherMaxDisplayedScoreRows(bean.getMaxDisplayedRows());
		  }
	  }
	  else {
		  if (itemType.equals(Long.valueOf("7"))){
			  if (bean.getHasAudioMaxDisplayedScoreRowsChanged()) {
				  bean.setMaxDisplayedRows(bean.getAudioMaxDisplayedScoreRows());
			  }
			  else {
				  bean.setMaxDisplayedRows(5);
				  bean.setAudioMaxDisplayedScoreRows(5);
			  }
		  }
		  else {
			  bean.setMaxDisplayedRows(bean.getOtherMaxDisplayedScoreRows());
		  }
	  }
  }
}
