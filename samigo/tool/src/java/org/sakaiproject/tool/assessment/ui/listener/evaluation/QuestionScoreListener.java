/**********************************************************************************
* $URL$
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
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
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.info("QuestionScore LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean)
      cu.lookupBean("questionScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");

    log.info("Calling questionScores.");
    if (!questionScores(publishedId, bean, false))
    {
      //throw new RuntimeException("failed to call questionScores.");
    }

  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    log.info("QuestionScore CHANGE LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean)
      cu.lookupBean("questionScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");

    log.info("Calling questionScores.");
    if (!questionScores(publishedId, bean, true))
    {
      //throw new RuntimeException("failed to call questionScores.");
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
      GradingService delegate =	new GradingService();

      if (cu.lookupParam("sortBy") != null &&
          !cu.lookupParam("sortBy").trim().equals(""))
        bean.setSortType(cu.lookupParam("sortBy"));
      String itemId = cu.lookupParam("itemId");
      if (cu.lookupParam("newItemId") != null &&
          !cu.lookupParam("newItemId").trim().equals(""))
        itemId = cu.lookupParam("newItemId");
      String which = cu.lookupParam("allSubmissions");
      //log.info("Rachel: publishedId = " + publishedId + ", itemId = " + itemId + ", allSubmissions = " + which);
      if (which == null)
        which = "false";
      bean.setAllSubmissions(which);
      bean.setPublishedId(publishedId);
      Date dueDate = null;

      TotalScoresBean totalBean =
        (TotalScoresBean) cu.lookupBean("totalScores");

      ArrayList scores = new ArrayList();
      HashMap map = delegate.getItemScores(new Long(publishedId),
        new Long(itemId), which);
      Iterator iter = map.keySet().iterator();
      while (iter.hasNext())
      {
        scores.addAll((ArrayList) map.get(iter.next()));
      }

      // List them by item and assessmentgradingid, so we can
      // group answers by item and save them for update use.
      iter = scores.iterator();
      HashMap scoresByItem = new HashMap();
      while (iter.hasNext())
      {
        ItemGradingData idata = (ItemGradingData) iter.next();
        ArrayList temp = (ArrayList) scoresByItem.get
          (idata.getAssessmentGrading().getAssessmentGradingId() + ":" +
            idata.getPublishedItem().getItemId());
        if (temp == null)
          temp = new ArrayList();

        // Very small numbers, so bubblesort is fast
        Iterator iter2 = temp.iterator();
        ArrayList newList = new ArrayList();
        boolean added = false;
        while (iter2.hasNext())
        {
          ItemGradingData tmpData = (ItemGradingData) iter2.next();
          if (idata.getPublishedAnswer() != null &&
              tmpData.getPublishedAnswer() != null &&
              !added &&
              (idata.getPublishedItemText().getSequence().intValue() <
               tmpData.getPublishedItemText().getSequence().intValue() ||
               (idata.getPublishedItemText().getSequence().intValue() ==
                tmpData.getPublishedItemText().getSequence().intValue() &&
                idata.getPublishedAnswer().getSequence().intValue() <
                tmpData.getPublishedAnswer().getSequence().intValue())))
          {
            newList.add(idata);
            added = true;
          }
          newList.add(tmpData);
        }
        if (!added)
          newList.add(idata);
        scoresByItem.put(idata.getAssessmentGrading().getAssessmentGradingId()
         + ":" + idata.getPublishedItem().getItemId(), newList);
      }
      bean.setScoresByItem(scoresByItem);

      iter = scores.iterator();
      //log.info("Has this many agents: " + scores.size());
      if (!iter.hasNext())
        return false;
      Object next = iter.next();
      //log.info("Next is: " + next);

      // Okay, here we get the first result set, which has a summary of
      // information and a pointer to the graded assessment we should be
      // displaying.  We get the graded assessment.
      ItemGradingData data = (ItemGradingData) next;

      if (data.getAssessmentGrading().getPublishedAssessment() != null)
      {
	  //bean.setAssessmentName(data.getAssessmentGrading().getPublishedAssessment().getAssessment().getTitle());
	bean.setAssessmentName(totalBean.getAssessmentName()); // get name from totalScoreBean, instead of from the backend.(the name should be same). bug fix for sam-255.
        bean.setAssessmentId(data.getAssessmentGrading().getPublishedAssessment().getAssessment().getAssessmentBaseId().toString());

        // if section set is null, initialize it - daisyf , 01/31/05
        PublishedAssessmentData pub = (PublishedAssessmentData)data.getAssessmentGrading().getPublishedAssessment();
        HashSet sectionSet = PersistenceService.getInstance().
            getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(pub);
        data.getAssessmentGrading().getPublishedAssessment().setSectionSet(sectionSet);

        try {
          bean.setAnonymous((data.getAssessmentGrading().getPublishedAssessment().getEvaluationModel(). getAnonymousGrading().equals(EvaluationModel.ANONYMOUS_GRADING)?"true":"false"));
        } catch (Exception e) {
          //log.info("No evaluation model.");
          bean.setAnonymous("false");
        }
        try {
          bean.setLateHandling(data.getAssessmentGrading().getPublishedAssessment().getAssessmentAccessControl().getLateHandling().toString());
        } catch (Exception e) {
          //log.info("No access control model.");
          bean.setLateHandling(AssessmentAccessControl.NOT_ACCEPT_LATE_SUBMISSION.toString());
        }
        try {
          bean.setDueDate(data.getAssessmentGrading().getPublishedAssessment().getAssessmentAccessControl().getDueDate().toString());
          dueDate = data.getAssessmentGrading().getPublishedAssessment().getAssessmentAccessControl().getDueDate();
        } catch (Exception e) {
          //log.info("No due date.");
          bean.setDueDate(new Date().toString());
        }
        try {
          bean.setMaxScore(data.getAssessmentGrading().getPublishedAssessment().getEvaluationModel().getFixedTotalScore().toString());
        } catch (Exception e) {
          float score = (float) 0.0;
          Iterator iter2 = data.getAssessmentGrading().getPublishedAssessment().getSectionArraySorted().iterator();
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
      }

      ArrayList sections = new ArrayList();
      iter = data.getAssessmentGrading().getPublishedAssessment()
        .getSectionArraySorted().iterator();
      int i=1;
      while (iter.hasNext())
      {
        SectionDataIfc section = (SectionDataIfc) iter.next();
        ArrayList items = new ArrayList();
        PartData part = new PartData();
        part.setPartNumber("Section " + i + ":");
        part.setId(section.getSectionId().toString());
        Iterator iter2 = section.getItemArraySortedForGrading().iterator();
        int j = 1;
        while (iter2.hasNext())
        {
          ItemDataIfc item = (ItemDataIfc) iter2.next();
          PartData partitem = new PartData();
          partitem.setPartNumber("Q" + j);
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

      ItemDataIfc item = data.getPublishedItem();
      bean.setTypeId(item.getTypeId().toString());
      bean.setItemId(item.getItemId().toString());
      bean.setItemName("Section " + item.getSection().getSequence().toString()
        + ", Item " + item.getSequence().toString());
      //log.info("Rachel: TYpe id = " + item.getTypeId().toString());
      item.setHint("***"); // Keyword to not show student answer

      ArrayList deliveryItems = new ArrayList(); // so we can use the var
      deliveryItems.add(item);
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
      ArrayList agents = new ArrayList();
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

          // This all just gets the text of the answer to display
          String answerText = "N/A";
          String rationale = "";
          if (bean.getTypeId().equals("1") || bean.getTypeId().equals("2") ||
              bean.getTypeId().equals("3") || bean.getTypeId().equals("4") ||
              bean.getTypeId().equals("9"))
          {
            if (gdata.getPublishedAnswer() != null)
              answerText = gdata.getPublishedAnswer().getText();
          }
          else
          {
            answerText = gdata.getAnswerText();
          }

          if (bean.getTypeId().equals("9"))
            answerText = gdata.getPublishedItemText().getSequence() + ":" +
              answerText;

          if (bean.getTypeId().equals("8"))
            answerText = gdata.getPublishedAnswer().getSequence() + ":" +
              answerText;

          if (answerText == null)
            answerText = "N/A";
          else
          {
            if (gdata.getRationale() != null &&
               !gdata.getRationale().trim().equals(""))
              rationale = "\nRationale: " + gdata.getRationale();
          }
          answerText = answerText.replaceAll("<.*?>", "");
          rationale = rationale.replaceAll("<.*?>", "");
          if (answerText.length() > 35)
            answerText = answerText.substring(0, 35) + "...";
          if (rationale.length() > 35)
            rationale = rationale.substring(0, 35) + "...";

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
            results.setAssessmentGradingId(gdata.getAssessmentGrading()
              .getAssessmentGradingId());
            results.setTotalAutoScore(gdata.getAutoScore().toString());
            results.setComments(gdata.getComments());
            results.setAnswer(answerText);
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
              results.setLastInitial("A");
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

}
