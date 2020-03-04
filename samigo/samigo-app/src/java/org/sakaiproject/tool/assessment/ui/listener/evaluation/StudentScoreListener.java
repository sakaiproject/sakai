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



package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.rubrics.logic.RubricsService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

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

@Slf4j
 public class StudentScoreListener
  implements ActionListener
{
  private static EvaluationListenerUtil util;
  private static BeanSort bs;

  private RubricsService rubricsService = ComponentManager.get(RubricsService.class);

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
      bean.setEmail(agent.getEmail());
      
      DeliveryBean dbean = (DeliveryBean) ContextUtil.lookupBean("delivery");
      dbean.setActionString("gradeAssessment");

      DeliveryActionListener listener = new DeliveryActionListener();
      listener.processAction(null);
      
      // Added for SAK-13930
      DeliveryBean updatedDeliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
      List<SectionContentsBean> parts = updatedDeliveryBean.getPageContents().getPartsContents();
      for (SectionContentsBean part : parts) {
        List<ItemContentsBean> items = part.getItemContents();
        for (ItemContentsBean question : items) {
          question.setRubricStateDetails("");
          if (question.getGradingComment() != null && !question.getGradingComment().equals("")) {
            question.setGradingComment(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(question.getGradingComment()));
          }
        }
      } // End of SAK-13930

      GradingService service = new GradingService();
      AssessmentGradingData adata= (AssessmentGradingData) service.load(bean.getAssessmentGradingId(), false);
      bean.setComments(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(adata.getComments()));
      buildItemContentsMap(dbean, publishedId);

      return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }
  
  private void buildItemContentsMap(DeliveryBean dbean, String publishedId) {
	  Map<Long, ItemContentsBean> itemContentsMap = new HashMap<>();

      dbean.getPageContents().getPartsContents().stream()
              .filter(Objects::nonNull)
              .forEach(p -> p.getItemContents().stream()
                      .filter(Objects::nonNull)
                      .forEach(i -> {
                          i.setHasAssociatedRubric(rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_SAMIGO, RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX + publishedId + "." + i.getItemData().getItemId()));
                          i.getItemGradingDataArray()
                                  .forEach(d -> itemContentsMap.put(d.getItemGradingId(), i));
                      }));

	  dbean.setItemContentsMap(itemContentsMap);
  }
}
