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



package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.PartData;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
// end testing

/**
 * <p>
 * This handles the selection of the Question Score entry page.
 *  </p>
 * <p>Description: Action Listener for Evaluation Question Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class QuestionScoreListener
  implements ActionListener, ValueChangeListener
{
  private static Log log = LogFactory.getLog(QuestionScoreListener.class);
  private static EvaluationListenerUtil util;
  private static BeanSort bs;
  private static ContextUtil cu;

  /**
   * Standard process action method.
   * @param event ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent event) throws
    AbortProcessingException
  {
    log.debug("QuestionScore LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean)
      cu.lookupBean("questionScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");

    if (!questionScores(publishedId, bean, false))
    {
      throw new RuntimeException("failed to call questionScores.");
    }

  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    log.debug("QuestionScore CHANGE LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean)
      cu.lookupBean("questionScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");
    boolean toggleSubmissionSelection = false;

    String selectedvalue= (String) event.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      if (event.getComponent().getId().indexOf("sectionpicker") >-1 )
      {
        bean.setSelectedSectionFilterValue(selectedvalue);   // changed section pulldown
      }
      else
      {
        bean.setAllSubmissions(selectedvalue);    // changed submission pulldown
        toggleSubmissionSelection = true;
      }
    }

    if (!questionScores(publishedId, bean, toggleSubmissionSelection))
    {
      throw new RuntimeException("failed to call questionScores.");
    }
  }

  /**
   * This will populate the QuestionScoresBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * @todo Some of this code will change when we move this to Hibernate persistence.
   * @param publishedId String
   * @param bean QuestionScoresBean
   * @return boolean
   */
  public boolean questionScores(
    String publishedId, QuestionScoresBean bean, boolean isValueChange)
  {
    log.debug("questionScores()");
    try
    {
      PublishedAssessmentService pubService  = new PublishedAssessmentService();
      // get the PublishedAssessment based on publishedId
      QuestionScoresBean questionBean = (QuestionScoresBean) cu.lookupBean("questionScores");
      PublishedAssessmentIfc publishedAssessment = questionBean.getPublishedAssessment();
      if (publishedAssessment == null){
        publishedAssessment = pubService.getPublishedAssessment(publishedId);
        questionBean.setPublishedAssessment(publishedAssessment);
      }
      //build a hashMap (publishedItemId, publishedItem)
      HashMap publishedItemHash = pubService.preparePublishedItemHash(publishedAssessment);
      //build a hashMap (publishedItemTextId, publishedItemText)
      HashMap publishedItemTextHash = pubService.preparePublishedItemTextHash(publishedAssessment);
      HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);

      GradingService delegate =	new GradingService();

      TotalScoresBean totalBean =
        (TotalScoresBean) cu.lookupBean("totalScores");

      if (cu.lookupParam("sortBy") != null &&
          !cu.lookupParam("sortBy").trim().equals(""))
        bean.setSortType(cu.lookupParam("sortBy"));
      String itemId = cu.lookupParam("itemId");
      if (cu.lookupParam("newItemId") != null &&
          !cu.lookupParam("newItemId").trim().equals(""))
        itemId = cu.lookupParam("newItemId");

      String which = bean.getAllSubmissions();
      if (which == null && totalBean.getAllSubmissions() != null)
      {
        // use totalscore's selection 
        which = totalBean.getAllSubmissions();
        bean.setAllSubmissions(which);
      }

      totalBean.setSelectedSectionFilterValue(bean.getSelectedSectionFilterValue());   // set section pulldown

      if ("true".equalsIgnoreCase(totalBean.getAnonymous())){
      //reset sectionaware pulldown to -1 all sections
      totalBean.setSelectedSectionFilterValue(totalBean.ALL_SECTIONS_SELECT_VALUE);
      }


      bean.setPublishedId(publishedId);
      Date dueDate = null;

      HashMap map = getItemScores(new Long(publishedId), new Long(itemId), which, isValueChange);
      ArrayList allscores = new ArrayList();
      Iterator keyiter = map.keySet().iterator();
      while (keyiter.hasNext())
      {
        allscores.addAll((ArrayList) map.get(keyiter.next()));
      }


///

      // now we need filter by sections selected
      ArrayList scores = new ArrayList();  // filtered list
      Map useridMap= totalBean.getUserIdMap();

/*
    if ("true".equalsIgnoreCase(totalBean.getAnonymous())){
    // skip section filter if it is anonymous grading, SAK-4395, 
      scores.addAll(allscores);
    }
*/
    if (totalBean.getReleaseToAnonymous()) {
    // skip section filter if it's published to anonymous users
      scores.addAll(allscores);
    }
    else { 
        Iterator allscores_iter = allscores.iterator();
        // get the Map of all users(keyed on userid) belong to the selected sections
        while (allscores_iter.hasNext())
        {
          // AssessmentGradingData data = (AssessmentGradingData) allscores_iter.next();
          ItemGradingData idata = (ItemGradingData) allscores_iter.next();
          //String agentid = idata.getAssessmentGrading().getAgentId();
          String agentid = idata.getAgentId();
          // now we only include scores of users belong to the selected sections
          if (useridMap.containsKey(agentid) ) {
            scores.add(idata);
          }
        }
      }


      Iterator iter = scores.iterator();
      ArrayList agents = new ArrayList();

      if (!iter.hasNext())
      {
        // this section has no students
      bean.setAgents(agents);
      bean.setTotalPeople(new Integer(bean.getAgents().size()).toString());
      return true;
      }

      // List them by item and assessmentgradingid, so we can
      // group answers by item and save them for update use.

      HashMap scoresByItem = new HashMap();
      while (iter.hasNext())
      {
        ItemGradingData idata = (ItemGradingData) iter.next();
        ItemTextIfc pubItemText = (ItemTextIfc) publishedItemTextHash.get(idata.getPublishedItemTextId());
        AnswerIfc pubAnswer = (AnswerIfc) publishedAnswerHash.get(idata.getPublishedAnswerId());

        ArrayList temp = (ArrayList) scoresByItem.get
          (idata.getAssessmentGradingId() + ":" +
            idata.getPublishedItemId());
        if (temp == null)
          temp = new ArrayList();

        // Very small numbers, so bubblesort is fast
        Iterator iter2 = temp.iterator();
        ArrayList newList = new ArrayList();
        boolean added = false;
        while (iter2.hasNext())
        {
          ItemGradingData tmpData = (ItemGradingData) iter2.next();
          ItemTextIfc tmpPublishedText = (ItemTextIfc) publishedItemTextHash.get(tmpData.getPublishedItemTextId());
 	  AnswerIfc tmpAnswer = (AnswerIfc) publishedAnswerHash.get(tmpData.getPublishedAnswerId());

          if (pubAnswer != null && tmpAnswer != null && !added &&
              (pubItemText.getSequence().intValue() <
               tmpPublishedText.getSequence().intValue() ||
               (pubItemText.getSequence().intValue() ==
                tmpPublishedText.getSequence().intValue() &&
                pubAnswer.getSequence().intValue() <
                tmpPublishedText.getSequence().intValue())))
          {
            newList.add(idata);
            added = true;
          }
          newList.add(tmpData);
        }
        if (!added)
          newList.add(idata);
        scoresByItem.put(idata.getAssessmentGradingId()
         + ":" + idata.getPublishedItemId(), newList);
      }
      bean.setScoresByItem(scoresByItem);

      iter = scores.iterator();
      if (!iter.hasNext())
        return false;
      Object next = iter.next();

      // Okay, here we get the first result set, which has a summary of
      // information and a pointer to the graded assessment we should be
      // displaying.  We get the graded assessment.
      ItemGradingData data = (ItemGradingData) next;

      try {
        bean.setAnonymous(publishedAssessment.getEvaluationModel(). getAnonymousGrading().equals(EvaluationModel.ANONYMOUS_GRADING)?"true":"false");
      } catch (Exception e) {
        //log.info("No evaluation model.");
        bean.setAnonymous("false");
      }
      try {
        bean.setLateHandling(publishedAssessment.getAssessmentAccessControl().getLateHandling().toString());
      } catch (Exception e) {
        //log.info("No access control model.");
        bean.setLateHandling(AssessmentAccessControl.NOT_ACCEPT_LATE_SUBMISSION.toString());
      }
      try {
        bean.setDueDate(publishedAssessment.getAssessmentAccessControl().getDueDate().toString());
        dueDate = publishedAssessment.getAssessmentAccessControl().getDueDate();
      } catch (Exception e) {
        //log.info("No due date.");
        bean.setDueDate(new Date().toString());
      }
      try {
        bean.setMaxScore(publishedAssessment.getEvaluationModel().getFixedTotalScore().toString());
      } 
      catch (Exception e) {
        float score = (float) 0.0;
        Iterator iter2 = publishedAssessment.getSectionArraySorted().iterator();
        while (iter2.hasNext())
        {
          SectionDataIfc sdata = (SectionDataIfc) iter2.next();
          Iterator iter3 = sdata.getItemArraySortedForGrading().iterator();
          while (iter3.hasNext())
          {
            ItemDataIfc idata = (ItemDataIfc) iter3.next();
            if (idata.getItemId().equals(new Long(itemId)))
              score = idata.getScore().floatValue();
          }
        }
        bean.setMaxScore(new Float(score).toString());
      }

      ArrayList sections = new ArrayList();
      iter = publishedAssessment.getSectionArraySorted().iterator();
      int i=1;
      while (iter.hasNext())
      {
        SectionDataIfc section = (SectionDataIfc) iter.next();
        ArrayList items = new ArrayList();
        PartData part = new PartData();
	
	part.setPartNumber(""+i);
    
        part.setId(section.getSectionId().toString());
        Iterator iter2 = section.getItemArraySortedForGrading().iterator();
        int j = 1;
        while (iter2.hasNext())
        {
          ItemDataIfc item = (ItemDataIfc) iter2.next();
          PartData partitem = new PartData();
	  
          partitem.setPartNumber(""+j);
          partitem.setId(item.getItemId().toString());
          if (totalBean.getAnsweredItems().get(item.getItemId()) != null)
            partitem.setLinked(true);
          else
            partitem.setLinked(false);
          Iterator iter3 = scores.iterator();
          items.add(partitem);
          j++;
        }
        part.setQuestionNumberList(items);
        sections.add(part);
        i++;
      }
      bean.setSections(sections);

      ItemDataIfc item = (ItemDataIfc)publishedItemHash.get(data.getPublishedItemId());

      if (item!=null){
        bean.setTypeId(item.getTypeId().toString());
        bean.setItemId(item.getItemId().toString());
	bean.setPartName(item.getSection().getSequence().toString()); 
        bean.setItemName(item.getSequence().toString());
        item.setHint("***"); // Keyword to not show student answer
      }


      ArrayList deliveryItems = new ArrayList(); // so we can use the var
      if (item!=null)  deliveryItems.add(item);
      bean.setDeliveryItem(deliveryItems);

      if (cu.lookupParam("roleSelection") != null)
      {
        bean.setRoleSelection(cu.lookupParam("roleSelection"));
      }

      if (bean.getSortType() == null)
      {
        if (bean.getAnonymous().equals("true"))
        {
          bean.setSortType("totalAutoScore");
        }
        else
        {
          bean.setSortType("lastName");
        }
      }

      // recordingData encapsulates the inbeanation needed for recording.
      // set recording agent, agent assessmentId,
      // set course_assignment_context value
      // set max tries (0=unlimited), and 30 seconds max length
      String courseContext = bean.getAssessmentName() + " total ";
// Note this is HTTP-centric right now, we can't use in Faces
//      AuthoringHelper authoringHelper = new AuthoringHelper();
//      authoringHelper.getRemoteUserID() needs servlet stuff
//      authoringHelper.getRemoteUserName() needs servlet stuff

      String userId = "";
      String userName = "";
      RecordingData recordingData =
        new RecordingData( userId, userName,
        courseContext, "0", "30");
      // set this value in the requestMap for sound recorder
      bean.setRecordingData(recordingData);

      /* Dump the grading and agent information into AgentResults */
      //ArrayList agents = new ArrayList();
      iter = scoresByItem.values().iterator();
      while (iter.hasNext())
      {
        AgentResults results = new AgentResults();

        // Get all the answers for this question to put in one grading row
        ArrayList answerList = (ArrayList) iter.next();
        results.setItemGradingArrayList(answerList);

        Iterator iter2 = answerList.iterator();
        while (iter2.hasNext())
        {
          ItemGradingData gdata = (ItemGradingData) iter2.next();
          ItemTextIfc gdataPubItemText = (ItemTextIfc) publishedItemTextHash.get(gdata.getPublishedItemTextId());          
	  AnswerIfc gdataAnswer = (AnswerIfc) publishedAnswerHash.get(gdata.getPublishedAnswerId());

          // This all just gets the text of the answer to display
          String answerText = "N/A";
          String rationale = "";
          String fullAnswerText = "N/A";
          if (bean.getTypeId().equals("1") || bean.getTypeId().equals("2") ||
              bean.getTypeId().equals("3") || bean.getTypeId().equals("4") ||
              bean.getTypeId().equals("9"))
          {
            if (gdataAnswer != null)
              answerText = gdataAnswer.getText();
          }
          else
          {
            answerText = gdata.getAnswerText();
          }

          if (bean.getTypeId().equals("9"))
            answerText = gdataPubItemText.getSequence() + ":" +
              answerText;

          if (bean.getTypeId().equals("8"))
            answerText = gdataAnswer.getSequence() + ":" +
              answerText;

          if (answerText == null)
            answerText = "N/A";
          else
          {
            if (gdata.getRationale() != null &&
               !gdata.getRationale().trim().equals(""))
              rationale = "\nRationale: " + gdata.getRationale();
          }
	  //Huong's temp commandout
	  //answerText = answerText.replaceAll("<.*?>", "");
	  answerText = answerText.replaceAll("\r\n", "<br/>");
          rationale = rationale.replaceAll("<.*?>", "");
          fullAnswerText = answerText;  // this is the non-abbreviated answers for essay questions
	 
          if (answerText.length() > 35)
            answerText = answerText.substring(0, 35) + "...";
/*
	  // no need to shorten it 
          if (rationale.length() > 35)
            rationale = rationale.substring(0, 35) + "...";
*/

          //  -- Got the answer text --

          if (!answerList.get(0).equals(gdata))
          { // We already have an agentResults for this one
            results.setAnswer(results.getAnswer() + "<br/>" + answerText);
            results.setTotalAutoScore(new Float
              ((new Float(results.getTotalAutoScore())).floatValue() +
               gdata.getAutoScore().floatValue()).toString());
          }
          else
          {
            results.setItemGradingId(gdata.getItemGradingId());
            results.setAssessmentGradingId(gdata.getAssessmentGradingId());
            if (gdata.getAutoScore() != null) {
              // for example, if an assessment has one fileupload question, the autoscore = null
              results.setTotalAutoScore(gdata.getAutoScore().toString());
            }
	    else {
              results.setTotalAutoScore(new Float(0).toString());
            }
            results.setComments(gdata.getComments());
            results.setAnswer(answerText);
            results.setFullAnswer(fullAnswerText);
            results.setRationale(rationale);
            results.setSubmittedDate(gdata.getSubmittedDate());

            if (dueDate == null || gdata.getSubmittedDate().before(dueDate))
              results.setIsLate(new Boolean(false));
            else
              results.setIsLate(new Boolean(true));

            AgentFacade agent = new AgentFacade(gdata.getAgentId());
            //log.info("Rachel: agentid = " + gdata.getAgentId());
            results.setLastName(agent.getLastName());
            results.setFirstName(agent.getFirstName());
            if (results.getLastName() != null &&
                results.getLastName().length() > 0)
              results.setLastInitial(results.getLastName().substring(0,1));
            else if (results.getFirstName() != null &&
                     results.getFirstName().length() > 0)
              results.setLastInitial(results.getFirstName().substring(0,1));
            else
	      results.setLastInitial("Anonymous");
            results.setIdString(agent.getIdString());
            results.setRole(agent.getRole());
            agents.add(results);
          }
        }
      }

      //log.info("Sort type is " + bean.getSortType() + ".");
      bs = new BeanSort(agents, bean.getSortType());
      if (
        (bean.getSortType()).equals("assessmentGradingId") ||
        (bean.getSortType()).equals("totalAutoScore") ||
        (bean.getSortType()).equals("totalOverrideScore") ||
        (bean.getSortType()).equals("finalScore"))
      {
        bs.toNumericSort();
      } else {
        bs.toStringSort();
      }


      bs.sort();
      //log.info("Listing agents.");
      bean.setAgents(agents);
      bean.setTotalPeople(new Integer(bean.getAgents().size()).toString());
    }

    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /** getting a list of itemGrading for a publishedItemId is a lot of work,
   * read the code in GradingService.getItemScores()
   * after we get the list, we are saving it in QuestionScoreBean.itemScoresMap
   * itemScoresMap = (publishedItemId, HashMap) 
                   = (Long publishedItemId, (Long publishedItemId, Array itemGradings))
   * itemScoresMap will be refreshed when the next QuestionScore link is click
   */
  private HashMap getItemScores(Long publishedId, Long itemId, String which, boolean isValueChange){
    GradingService delegate = new GradingService();
    QuestionScoresBean questionScoresBean =
      (QuestionScoresBean) cu.lookupBean("questionScores");
    HashMap itemScoresMap = questionScoresBean.getItemScoresMap();
    if (itemScoresMap == null || isValueChange){
      itemScoresMap = new HashMap();
      questionScoresBean.setItemScoresMap(itemScoresMap);
    }
    HashMap map = (HashMap) itemScoresMap.get(itemId);
    if (map == null){
      map = delegate.getItemScores(publishedId, itemId, which);
      itemScoresMap.put(itemId, map);
    }
    return map;
  }

}
