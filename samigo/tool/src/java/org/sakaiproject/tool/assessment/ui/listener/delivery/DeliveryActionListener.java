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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
//import org.sakaiproject.tool.assessment.integration.delivery.SessionUtil;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ContentsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FibBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class DeliveryActionListener
  implements ActionListener
{

  static String alphabet = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
  private static Log log = LogFactory.getLog(DeliveryActionListener.class);
  private static ContextUtil cu;
  private boolean resetPageContents = true;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("DeliveryActionListener.processAction() ");

    try
    {
      // 1. get managed bean
      DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
      // a. set publishedId, note that id can be changed by isPreviewingMode()
      String id = getPublishedAssessmentId(delivery);
      String agent = getAgentString();

      // b. this is to be reset to true on the saveTime() JavaScript when submitted
      delivery.setJavaScriptEnabledCheck("false");
      // c. Clear elapsed time, set not timed out
      clearElapsedTime(delivery);

      // 3. get assessment from deliveryBean if id matches. otherwise, this is the 1st time
      // that DeliveryActionListener is called, so pull it from DB
      PublishedAssessmentFacade publishedAssessment = getPublishedAssessment(delivery, id);
      // e. set show student score
      setShowStudentScore(delivery, publishedAssessment);
      setDeliverySettings(delivery, publishedAssessment);

      // 2. there 3 types of navigation: by question (question will be displayed one at a time), 
      // by part (questions in a part will be displayed together) or by assessment (i.e. 
      // all questions will be displayed on one page). When navigating from TOC, a part number
      // signal that the navigation is either by question or by part. We then must set the 
      // delivery bean to the right place.
      goToRightQuestionFromTOC(delivery);

      // 4. this purpose of this listener is to integrated itemGradingData and 
      //    assessmentGradingData to the publishedAssessment during these 3 processes:
      //    taking assessment, reviewing assessment and grading assessment by question. 
      //    When taking or reviewing an assessment, BeginDeliveryActionListener is called
      //    by an event in the jsf page to retrieve the publishedAssessment. When grading
      //    assessment, StudentScoreListener is called to retrieve the published Assessment.
      int action = delivery.getActionMode();

      HashMap itemData = new HashMap();
      GradingService service = new GradingService();
      System.out.println("**** DeliveryActionListener:actionString"+delivery.getActionString());
      System.out.println("**** DeliveryActionListener:action"+delivery.getActionMode());

      switch (action){
      case 3: // Review assessment
              setFeedbackMode(delivery);
              itemData = new HashMap();
              System.out.println("**** revieAssessment: delivery.getFeedbackComponent().getShowResponse()="+delivery.getFeedbackComponent().getShowResponse());
              if (delivery.getFeedbackComponent().getShowResponse())
                itemData = service.getSubmitData(id, agent);
              System.out.println("**** itemData.size ="+itemData.size());
              System.out.println("**** feedback="+delivery.getFeedback());
              System.out.println("**** noFeedback="+delivery.getNoFeedback());
              setAssessmentGradingFromItemData(delivery, itemData, false);
              setDisplayByAssessment(delivery);
              break;
 
      case 4: // Grade assessment
              itemData = service.getStudentGradingData(cu.lookupParam("gradingData"));
              System.out.println("**** gradingData ="+cu.lookupParam("gradingData"));
              System.out.println("**** itemData.size ="+itemData.size());
              setAssessmentGradingFromItemData(delivery, itemData, false);
              setDisplayByAssessment(delivery);
              //delivery.setFeedback("true");
              setDeliveryFeedbackOnforEvaluation(delivery);
              break;

      case 1: // Take assessment
      case 5: // Take assessment via url
               itemData = service.getLastItemGradingData(id, agent);
               if (itemData!=null && itemData.size()>0)
                 setAssessmentGradingFromItemData(delivery, itemData, true);
               else{
                 AssessmentGradingData ag = service.getLastSavedAssessmentGradingByAgentId(id, agent);
                 delivery.setAssessmentGrading(ag);
               }
               setFeedbackMode(delivery);
               System.out.println("**** feedback="+delivery.getFeedback());
               System.out.println("**** noFeedback="+delivery.getNoFeedback());
               break;

      default: break;
      }



      // We're going to overload itemData with the sequence in case
      // renumbering is turned off.
      itemData.put("sequence", new Long(0));
      long items = 0;
      int sequenceno = 1;
      Iterator i1 = publishedAssessment.getSectionArraySorted().iterator();
      while (i1.hasNext())
      {
        SectionDataIfc section = (SectionDataIfc) i1.next();
        Iterator i2 = null;

        if (delivery.getActionMode()==delivery.GRADE_ASSESSMENT) {
          StudentScoresBean studentscorebean = (StudentScoresBean) cu.lookupBean("studentScores");
          long seed = (long) studentscorebean.getStudentId().hashCode();
          i2 = section.getItemArraySortedWithRandom(seed).iterator();
        }
        else {
          i2 = section.getItemArraySorted().iterator();
        }

        while (i2.hasNext()) {
          items = items + 1; // bug 464
          ItemDataIfc item = (ItemDataIfc) i2.next();
          itemData.put("sequence" + item.getItemId().toString(),
                       new Integer(sequenceno++));
        }
      }
      itemData.put("items", new Long(items));

      if (delivery.getAssessmentGrading() != null) {
        delivery.setGraderComment
          (delivery.getAssessmentGrading().getComments());
      }
      else {
        delivery.setGraderComment(null);
      }

      // Set the begin time if we're just starting
      if (delivery.getBeginTime() == null) {
        if (delivery.getAssessmentGrading() != null &&
            delivery.getAssessmentGrading().getAttemptDate() != null) {
          delivery.setBeginTime(delivery.getAssessmentGrading().getAttemptDate());
          // add the following line to fix SAK-1781
          if (delivery.getAssessmentGrading().getTimeElapsed() != null){
            delivery.setTimeElapse(delivery.getAssessmentGrading()
                                .getTimeElapsed().toString());
          }
          else{
            delivery.setTimeElapse("0");
          }
        }
        else
        {
          delivery.setBeginTime(new Date());
          delivery.setTimeElapse("0");  // fix SAK-1781
        }
      }

      log.debug("****Set begin time " + delivery.getBeginTime());
      log.debug("****Set elapsed time " + delivery.getTimeElapse());

      /** if taking assessment, modify session intactive interval */
      if (!("true".equals(cu.lookupParam("review")) || "true".equals(cu.lookupParam("previewAssessment"))))
      {
        SessionUtil.setSessionTimeout(FacesContext.getCurrentInstance(), delivery, true);
      }

      // get table of contents
      delivery.setTableOfContents(getContents(publishedAssessment, itemData,
                                              delivery));

      // get current page contents
      log.debug("**** resetPageContents="+this.resetPageContents);
      if (this.resetPageContents)
        delivery.setPageContents(getPageContents(publishedAssessment,
                                               delivery, itemData));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  /**
   * Look up item grading data and set assesment grading data from it or,
   * if there is none set null if setNullOK.
   * @param delivery the delivery bean
   * @param itemData the itemData hash map
   * @param setNullOK if there is none set null if true
   */
  private void setAssessmentGradingFromItemData(DeliveryBean delivery,
                      HashMap itemData, boolean setNullOK)
  {
    Iterator keys = itemData.keySet().iterator();
    if (keys.hasNext())
    {
      log.debug("itemData.keySet().iterator().hasNext()");
      ItemGradingData igd = (ItemGradingData) ( (ArrayList) itemData.get(
        keys.next())).toArray()[0];
      AssessmentGradingData agd =
        (AssessmentGradingData) igd.getAssessmentGrading();
      if (!agd.getForGrade().booleanValue()){
        delivery.setAssessmentGrading(agd);
        log.debug("setAssessmentGradingFromItemData agd.getTimeElapsed(): " + agd.getTimeElapsed());
        log.debug("setAssessmentGradingFromItemData delivery.getTimeElapse(): " + delivery.getTimeElapse());
      }
      else{ // if assessmentGradingData has been submitted for grade, then
    // we need to reset delivery.assessmentGrading - a problem review from fixing SAK-1781
        if (setNullOK) delivery.setAssessmentGrading(null);
      }
    }
    else
    {
      if (setNullOK) delivery.setAssessmentGrading(null);
    }
    log.info("**** delivery grdaing"+delivery.getAssessmentGrading());
  }

  /**
   * Put the setShows on.
   * @param delivery the delivery bean
   */
  private void setDeliveryFeedbackOnforEvaluation(DeliveryBean delivery)
  {
    delivery.getFeedbackComponent().setShowCorrectResponse(true);
    delivery.getFeedbackComponent().setShowGraderComment(true);
    delivery.getFeedbackComponent().setShowItemLevel(true);
    delivery.getFeedbackComponent().setShowQuestion(true);
    delivery.getFeedbackComponent().setShowResponse(true);
    delivery.getFeedbackComponent().setShowSelectionLevel(true);
    delivery.getFeedbackComponent().setShowStats(true);
    delivery.getFeedbackComponent().setShowStudentScore(true);
    delivery.getFeedbackComponent().setShowStudentQuestionScore(true);
  }

  /**
   * Sets the delivery bean to the right place when navigating from TOC
   * @param delivery
   * @throws java.lang.NumberFormatException
   */
  private void goToRightQuestionFromTOC(DeliveryBean delivery) throws
    NumberFormatException
  {
    if (cu.lookupParam("partnumber") != null &&
          !cu.lookupParam("partnumber").trim().equals(""))
    {
      if (delivery.getSettings().isFormatByPart() ||
        delivery.getSettings().isFormatByQuestion())
      {
        delivery.setPartIndex(new Integer
                            (cu.lookupParam("partnumber")).intValue() - 1);
      }
      if (delivery.getSettings().isFormatByQuestion())
      {
        delivery.setQuestionIndex(new Integer
          (cu.lookupParam("questionnumber")).intValue() - 1);
      }
    }
  }

  /**
   * Gets a table of contents bean
   * @param publishedAssessment the published assessment
   * @return
   */
  private ContentsDeliveryBean getContents(PublishedAssessmentFacade
                                           publishedAssessment,
                                           HashMap itemData,
                                           DeliveryBean delivery)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;

    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
    while (iter.hasNext())
    {
      SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                 itemData, delivery);
      partBean.setNumParts(new Integer(partSet.size()).toString());
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();
      partsContents.add(partBean);
    }

    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    return contents;
  }

  /**
   * Gets a contents bean for the current page.
   * Really, just a wrapper utility to delegate to whichever
   * method handles the format being used.
   *
   * @todo these should actually take a copy of contents and filter it
   * for the page unstead of doing a recompute, which is less efficient
   * @param publishedAssessment the published assessment
   * @return
   */
  public ContentsDeliveryBean getPageContents(
    PublishedAssessmentFacade publishedAssessment,
    DeliveryBean delivery, HashMap itemData)
  {

    if (delivery.getSettings().isFormatByAssessment())
    {
      return getPageContentsByAssessment(publishedAssessment, itemData,
                                         delivery);
    }

    int itemIndex = delivery.getQuestionIndex();
    int sectionIndex = delivery.getPartIndex();

    if (delivery.getSettings().isFormatByPart())
    {
      return getPageContentsByPart(publishedAssessment, itemIndex, sectionIndex,
                                   itemData, delivery);
    }
    else if (delivery.getSettings().isFormatByQuestion())
    {
      return getPageContentsByQuestion(publishedAssessment, itemIndex,
                                       sectionIndex, itemData, delivery);
    }

    // default... ...shouldn't get here :O
    log.warn("delivery.getSettings().isFormatBy... is NOT set!");
    return getPageContentsByAssessment(publishedAssessment, itemData, delivery);

  }

  /**
   * Gets a contents bean for the current page if is format by assessment.
   *
   * @param publishedAssessment the published assessment
   * @return ContentsDeliveryBean for page
   */
  private ContentsDeliveryBean getPageContentsByAssessment(
    PublishedAssessmentFacade publishedAssessment, HashMap itemData,
    DeliveryBean delivery)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;

    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
    while (iter.hasNext())
    {
      SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                 itemData, delivery);
      partBean.setNumParts(new Integer(partSet.size()).toString());
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();
      partsContents.add(partBean);
    }

    delivery.setPrevious(false);
    delivery.setContinue(false);
    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    contents.setShowStudentScore(delivery.isShowStudentScore());
    return contents;
  }

  /**
   * Gets a contents bean for the current page if is format by part.
   *
   * @param publishedAssessment the published assessment
   * @param itemIndex zero based item offset in part
   * @param sectionIndex zero based section offset in assessment
   * @return ContentsDeliveryBean for page
   */
  private ContentsDeliveryBean getPageContentsByPart(
    PublishedAssessmentFacade publishedAssessment,
    int itemIndex, int sectionIndex, HashMap itemData, DeliveryBean delivery)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;
    int sectionCount = 0;

    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
    while (iter.hasNext())
    {
      SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                 itemData, delivery);
      partBean.setNumParts(new Integer(partSet.size()).toString());
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();
      if (sectionCount++ == sectionIndex)
      {
        partsContents.add(partBean);
        if (iter.hasNext())
        {
          delivery.setContinue(true);
        }
        else
        {
          delivery.setContinue(false);
        }
        if (sectionCount > 1)
        {
          delivery.setPrevious(true);
        }
        else
        {
          delivery.setPrevious(false);
        }
      }
    }

    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    contents.setShowStudentScore(delivery.isShowStudentScore());
    return contents;
  }

  /**
   * Gets a contents bean for the current page if is format by question.
   *
   * @param publishedAssessment the published assessment
   * @param itemIndex zero based item offset in part
   * @param sectionIndex zero based section offset in assessment
   * @return ContentsDeliveryBean for page
   */
  private ContentsDeliveryBean getPageContentsByQuestion(
    PublishedAssessmentFacade publishedAssessment,
    int itemIndex, int sectionIndex, HashMap itemData, DeliveryBean delivery)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;
    int sectionCount = 0;
    int questionCount = 0; // This is to increment the part if we run
    // out of questions
    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
    if (itemIndex < 0)
    {
      sectionIndex--;
      delivery.setPartIndex(sectionIndex);
    }
    while (iter.hasNext())
    {
      SectionDataIfc secFacade = (SectionDataIfc) iter.next();
      SectionContentsBean partBean = getPartBean(secFacade, itemData, delivery);
      partBean.setNumParts(new Integer(partSet.size()).toString());
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();

      //questionCount = secFacade.getItemSet().size();
      // need to  get ItemArraySort, insteand of getItemSet, to return corr number for random draw parts
      questionCount = secFacade.getItemArraySorted().size();

      if (itemIndex > (questionCount - 1) && sectionCount == sectionIndex)
      {
        sectionIndex++;
        delivery.setPartIndex(sectionIndex);
        itemIndex = 0;
        delivery.setQuestionIndex(itemIndex);
      }
      if (itemIndex < 0 && sectionCount == sectionIndex)
      {
        itemIndex = questionCount - 1;
        delivery.setQuestionIndex(itemIndex);
      }

      if (sectionCount++ == sectionIndex)
      {
        SectionContentsBean partBeanWithQuestion =
          this.getPartBeanWithOneQuestion(secFacade, itemIndex, itemData,
                                          delivery);
        partBeanWithQuestion.setNumParts(new Integer(partSet.size()).toString());
        partsContents.add(partBeanWithQuestion);

        if (iter.hasNext() || itemIndex < (questionCount - 1))
        {
          delivery.setContinue(true);
        }
        else
        {
          delivery.setContinue(false);
        }
        if (itemIndex > 0 || sectionIndex > 0)
        {
          delivery.setPrevious(true);
        }
        else
        {
          delivery.setPrevious(false);
        }
      }
    }

    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    contents.setShowStudentScore(delivery.isShowStudentScore());
    return contents;
  }

  /**
     * Populate a SectionContentsBean properties and populate with ItemContentsBean
   * @param part this section
   * @return
   */
  private SectionContentsBean getPartBean(SectionDataIfc part, HashMap itemData,
                                          DeliveryBean delivery)
  {
    float maxPoints = 0;
    float points = 0;
    int unansweredQuestions = 0;

    //SectionContentsBean sec = new SectionContentsBean();
    //sec.setSectionId(part.getSectionId().toString()); 
    // daisy change to use this existing constructor instead 11/09/05
    SectionContentsBean sec = new SectionContentsBean(part);

    ArrayList itemSet = null;
    if (delivery.getActionMode()==delivery.GRADE_ASSESSMENT) {
      StudentScoresBean studentscorebean = (StudentScoresBean) cu.lookupBean("studentScores");
      long seed = (long) studentscorebean.getStudentId().hashCode();
      itemSet = part.getItemArraySortedWithRandom(seed);
    }
    else {
      itemSet = part.getItemArraySorted();
    }

    sec.setQuestions(itemSet.size());

    if (delivery.getSettings().getItemNumbering().equals
        (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
    {
      sec.setNumbering(itemSet.size());
    }
    else
    {
      sec.setNumbering( ( (Long) itemData.get("items")).intValue());
    }

    sec.setText(part.getTitle());
    sec.setDescription(part.getDescription());
    sec.setNumber("" + part.getSequence());

// check metadata for authoring type
    sec.setMetaData(part);

    Iterator iter = itemSet.iterator();
    ArrayList itemContents = new ArrayList();
    int i = 0;
    while (iter.hasNext())
    {
      ItemDataIfc thisitem = (ItemDataIfc) iter.next();
      ItemContentsBean itemBean = getQuestionBean(thisitem,
                                                  itemData, delivery);

      // Deal with numbering
      itemBean.setNumber(++i);
      if (delivery.getSettings().getItemNumbering().equals
          (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
      {
        itemBean.setSequence(new Integer(itemBean.getNumber()).toString());
      }
      else
      {
        itemBean.setSequence( ( (Integer) itemData.get("sequence" +
          thisitem.getItemId().toString())).toString());
      }

      // scoring
      maxPoints += itemBean.getMaxPoints();
      points += itemBean.getPoints();
      itemBean.setShowStudentScore(delivery.isShowStudentScore());

      if (itemBean.isUnanswered())
      {
        unansweredQuestions++;
      }
      itemContents.add(itemBean);
    }

    // scoring information
    sec.setMaxPoints(roundToTenths(maxPoints));
    sec.setPoints(roundToTenths(points));
    sec.setShowStudentScore(delivery.isShowStudentScore());
    sec.setUnansweredQuestions(unansweredQuestions);
    sec.setItemContents(itemContents);

    return sec;
  }

  /**
   * Populate a SectionContentsBean properties and populate with ItemContentsBean
   * @param part this section
   * @return
   */
  private SectionContentsBean getPartBeanWithOneQuestion(
    SectionDataIfc part, int itemIndex, HashMap itemData, DeliveryBean delivery)
  {
    float maxPoints = 0;
    float points = 0;
    int unansweredQuestions = 0;
    int itemCount = 0;

    //SectionContentsBean sec = new SectionContentsBean();
    SectionContentsBean sec = new SectionContentsBean(part);
    ArrayList itemSet = part.getItemArraySorted();

    sec.setQuestions(itemSet.size());

    if (delivery.getSettings().getItemNumbering().equals
        (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
    {
      sec.setNumbering(itemSet.size());
    }
    else
    {
      sec.setNumbering( ( (Long) itemData.get("items")).intValue());
    }

    sec.setText(part.getTitle());
    sec.setDescription(part.getDescription());
    sec.setNumber("" + part.getSequence());

    // get items
    Iterator iter = itemSet.iterator();
    ArrayList itemContents = new ArrayList();
    int i = 0;
    while (iter.hasNext())
    {
      ItemDataIfc thisitem = (ItemDataIfc) iter.next();
      ItemContentsBean itemBean = getQuestionBean(thisitem,
                                                  itemData, delivery);

      // Numbering
      itemBean.setNumber(++i);
      if (delivery.getSettings().getItemNumbering().equals
          (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
      {
        itemBean.setSequence(new Integer(itemBean.getNumber()).toString());
      }
      else
      {
        itemBean.setSequence( ( (Integer) itemData.get("sequence" +
          thisitem.getItemId().toString())).toString());
      }

      // scoring
      maxPoints += itemBean.getMaxPoints();
      points += itemBean.getPoints();
      itemBean.setShowStudentScore(delivery.isShowStudentScore());

      if (itemBean.isUnanswered())
      {
        unansweredQuestions++;
      }
      if (itemCount++ == itemIndex)
      {
        itemContents.add(itemBean);
      }
    }

    // scoring information
    sec.setMaxPoints(roundToTenths(maxPoints));
    sec.setPoints(roundToTenths(points));
    sec.setShowStudentScore(delivery.isShowStudentScore());
    sec.setUnansweredQuestions(unansweredQuestions);
    sec.setItemContents(itemContents);

    return sec;
  }

  /**
   * Helper method.
   * @param points
   * @return
   */
  private float roundToTenths(float points)
  {
    int tmp = Math.round(points * 10.0f);
    points = (float) tmp / 10.0f;
    return points;
  }

  /**
   * populate a single ItemContentsBean from an item for delivery
   * @param item  an Item
   * @return
   */
  private ItemContentsBean getQuestionBean(ItemDataIfc item, HashMap itemData,
                                           DeliveryBean delivery)
  {
    ItemContentsBean itemBean = new ItemContentsBean();
    itemBean.setItemData(item);
    itemBean.setMaxPoints(item.getScore().floatValue());
    itemBean.setPoints( (float) 0);

    // update maxNumAttempts for audio
    if (item.getTriesAllowed() != null)
    {
      itemBean.setTriesAllowed(item.getTriesAllowed());
    }

    // save timeallowed for audio recording
    if (item.getDuration() != null)
    {
      itemBean.setDuration(item.getDuration());
    }

    itemBean.setItemGradingDataArray
      ( (ArrayList) itemData.get(item.getItemId()));

    // Set comments and points
    Iterator i = itemBean.getItemGradingDataArray().iterator();
    while (i.hasNext())
    {
      ItemGradingData data = (ItemGradingData) i.next();
      // All itemgradingdata comments for the same item are identical
      itemBean.setGradingComment(data.getComments());
      if (data.getAutoScore() != null)
      {
        itemBean.setPoints(itemBean.getPoints() +
                           data.getAutoScore().floatValue());
      }
    }

    if (item.getTypeId().toString().equals("5") ||
        item.getTypeId().toString().equals("6") ||
        item.getTypeId().toString().equals("3") ||
        item.getTypeId().toString().equals("7"))
    {
      itemBean.setFeedback(item.getGeneralItemFeedback());
    }
    else if (itemBean.getPoints() >= itemBean.getMaxPoints())
    {
      itemBean.setFeedback(item.getCorrectItemFeedback());
    }
    else
    {
      itemBean.setFeedback(item.getInCorrectItemFeedback());

      // Do we randomize answer list?
    }
    boolean randomize = false;
    i = item.getItemMetaDataSet().iterator();
    while (i.hasNext())
    {
      ItemMetaDataIfc meta = (ItemMetaDataIfc) i.next();
      if (meta.getLabel().equals(ItemMetaDataIfc.RANDOMIZE))
      {
        if (meta.getEntry().equals("true"))
        {
          randomize = true;
          break;
        }
      }
    }

    ArrayList myanswers = new ArrayList();

    // Generate the answer key
    String key = "";
    Iterator key1 = item.getItemTextArraySorted().iterator();
    int j = 1;
    while (key1.hasNext())
    {
      // We need to store the answers in an arraylist in case they're
      // randomized -- we assign labels here, and then step through
      // them again later, and we have to make sure the order is the
      // same each time.
      myanswers = new ArrayList(); // Start over each time so we don't
      // get duplicates.
      ItemTextIfc text = (ItemTextIfc) key1.next();
      Iterator key2 = null;

      // Never randomize Fill-in-the-blank, always randomize matching
      if ( (randomize && !item.getTypeId().toString().equals("8")) ||
          item.getTypeId().toString().equals("9"))
      {
        ArrayList shuffled = new ArrayList();
        Iterator i1 = text.getAnswerArraySorted().iterator();
        while (i1.hasNext())
        {
          shuffled.add(i1.next());

          // Randomize matching the same way for each
        }
        if (item.getTypeId().toString().equals("9"))
        {
/*
          Collections.shuffle(shuffled,
                              new Random( (long) item.getText().hashCode()));
*/
          Collections.shuffle(shuffled,
                              new Random( (long) item.getText().hashCode() +
                getAgentString().hashCode()));
        }
        else
        {
/*
          Collections.shuffle(shuffled,
                              new Random( (long) item.getText().hashCode()));
*/
          Collections.shuffle(shuffled,
                              new Random( (long) item.getText().hashCode() +
                                         getAgentString().hashCode()));
        }
        key2 = shuffled.iterator();
      }
      else
      {
        key2 = text.getAnswerArraySorted().iterator();
      }
      int k = 0;
      while (key2.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) key2.next();

        // Don't save the answer if it has no text
        if ( (answer.getText() == null || answer.getText().trim().equals(""))
            && (item.getTypeId().toString().equals("1") ||
                item.getTypeId().toString().equals("2") ||
                item.getTypeId().toString().equals("3")))
        {
          // Ignore, it's a null answer
        }
        else
        {
          // Set the label and key
          if (item.getTypeId().toString().equals("1") ||
              item.getTypeId().toString().equals("2") ||
              item.getTypeId().toString().equals("9"))
          {
            answer.setLabel(new Character(alphabet.charAt(k++)).toString());
            if (answer.getIsCorrect() != null &&
                answer.getIsCorrect().booleanValue())
            {
              String addition = "";
              if (item.getTypeId().toString().equals("9"))
              {
                addition = new Integer(j++).toString() + ":";
              }
              if (key.equals(""))
              {
                key += addition + answer.getLabel();
              }
              else
              {
                key += ", " + addition + answer.getLabel();
              }
            }
          }
          if (item.getTypeId().toString().equals("4") &&
              answer.getIsCorrect() != null &&
              answer.getIsCorrect().booleanValue())
          {
            key = (answer.getText().equalsIgnoreCase("true") ? "True" : "False");
          }
          if (item.getTypeId().toString().equals("5") ||
              item.getTypeId().toString().equals("6") ||
              item.getTypeId().toString().equals("7"))
          {
            key += answer.getText();
          }
          if (item.getTypeId().toString().equals("8"))
          {
            if (key.equals(""))
            {
              key += answer.getText();
            }
            else
            {
              key += ", " + answer.getText();
            }
          }
          myanswers.add(answer);
        }
      }
    }
    itemBean.setKey(key);

    // Delete this
    itemBean.setShuffledAnswers(myanswers);

    // This creates the list of answers for an item
    ArrayList answers = new ArrayList();
    if (item.getTypeId().toString().equals("1") ||
        item.getTypeId().toString().equals("2") ||
        item.getTypeId().toString().equals("3") ||
        item.getTypeId().toString().equals("4") ||
        item.getTypeId().toString().equals("9"))
    {
      Iterator iter = myanswers.iterator();
      while (iter.hasNext())
      {
        SelectionBean selectionBean = new SelectionBean();
        selectionBean.setItemContentsBean(itemBean);
        AnswerIfc answer = (AnswerIfc) iter.next();
        selectionBean.setAnswer(answer);

        // It's saved lower case in the db -- this is a kludge
        if (item.getTypeId().toString().equals("4") && // True/False
            answer.getText().equals("true"))
        {
          answer.setText("True");
        }
        if (item.getTypeId().toString().equals("4") && // True/False
            answer.getText().equals("false"))
        {
          answer.setText("False");

        }
        String label = "";
        if (answer.getLabel() == null)
        {
          answer.setLabel("");

          // Delete this when everything works.
        }
        if (!answer.getLabel().equals(""))
        {
          label += answer.getLabel() + ". " + answer.getText();
        }
        else
        {
          label = answer.getText();

          // Set the response to true or false for each answer
        }
        selectionBean.setResponse(false);
        Iterator iter1 = itemBean.getItemGradingDataArray().iterator();
        while (iter1.hasNext())
        {
          ItemGradingData data = (ItemGradingData) iter1.next();
          if (data.getPublishedAnswer() != null &&
              (data.getPublishedAnswer().equals(answer) ||
               data.getPublishedAnswer().getId().equals(answer.getId())))
          {
            selectionBean.setItemGradingData(data);
            selectionBean.setResponse(true);
          }
        }

        if (delivery.getFeedbackComponent() != null &&
            delivery.getFeedback().equals("true") &&
            delivery.getFeedbackComponent().getShowSelectionLevel())
        {
          // If right answer, set feedback to correct, otherwise incorrect
          if (answer.getIsCorrect() == null)
          {
            selectionBean.setFeedback(answer.getGeneralAnswerFeedback());
          }
          else if (selectionBean.getResponse() &&
                   answer.getIsCorrect().booleanValue() ||
                   !selectionBean.getResponse() &&
                   !answer.getIsCorrect().booleanValue())
          {
            selectionBean.setFeedback(answer.getCorrectAnswerFeedback());
          }
          else
          {
            selectionBean.setFeedback(answer.getInCorrectAnswerFeedback());

          }
        }

        // Delete this
        String description = "";
        if (delivery.getFeedback().equals("true") &&
            delivery.getFeedbackComponent().getShowCorrectResponse() &&
            answer.getIsCorrect() != null)
        {
          description = answer.getIsCorrect().toString();

          // Delete this
        }
        SelectItem newItem =
          new SelectItem(answer.getId().toString(), label, description);

        if (item.getTypeId().toString().equals("4"))
        {
          answers.add(newItem);
        }
        else
        {
          answers.add(selectionBean);
        }
      }
    }
    // Delete this
    itemBean.setAnswers(answers);
    itemBean.setSelectionArray(answers);

    if (item.getTypeId().toString().equals("9")) // matching
    {
      populateMatching(item, itemBean);

    }
    if (item.getTypeId().toString().equals("8")) // fill in the blank
    {
      populateFib(item, itemBean);

      // round the points to the nearest tenth

    }

    return itemBean;
  }

  public void populateMatching(ItemDataIfc item, ItemContentsBean bean)
  {
    Iterator iter = item.getItemTextArraySorted().iterator();
    int j = 1;
    ArrayList beans = new ArrayList();
    ArrayList newAnswers = null;
    while (iter.hasNext())
    {
      ItemTextIfc text = (ItemTextIfc) iter.next();
      MatchingBean mbean = new MatchingBean();
      newAnswers = new ArrayList();
      mbean.setText(new Integer(j++).toString() + ". " + text.getText());
      mbean.setItemText(text);
      mbean.setItemContentsBean(bean);

      ArrayList choices = new ArrayList();
      ArrayList shuffled = new ArrayList();
      Iterator iter2 = text.getAnswerArraySorted().iterator();
      while (iter2.hasNext())
      {
        shuffled.add(iter2.next());

      }
      Collections.shuffle(shuffled,
  new Random( (long) item.getText().hashCode() +
  getAgentString().hashCode()));

/*
      Collections.shuffle
        (shuffled, new Random( (long) item.getText().hashCode()));
*/
      iter2 = shuffled.iterator();

      int i = 0;
      choices.add(new SelectItem("0", "select", "")); // default value for choice
      while (iter2.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) iter2.next();
        newAnswers.add(new Character(alphabet.charAt(i)).toString() +
                       ". " + answer.getText());
        choices.add(new SelectItem(answer.getId().toString(),
                                   new Character(alphabet.charAt(i++)).toString(),
                                   ""));
      }

      mbean.setChoices(choices); // Set the A/B/C... pulldown

      iter2 = bean.getItemGradingDataArray().iterator();
      while (iter2.hasNext())
      {

        ItemGradingData data = (ItemGradingData) iter2.next();

        if (data.getPublishedItemText().getId().equals(text.getId()))
        {
          // We found an existing grading data for this itemtext
          mbean.setItemGradingData(data);
          if (data.getPublishedAnswer() != null)
          {
            mbean.setResponse(data.getPublishedAnswer().getId()
                              .toString());
            if (data.getPublishedAnswer().getIsCorrect() != null &&
                data.getPublishedAnswer().getIsCorrect().booleanValue())
            {
              mbean.setFeedback(data.getPublishedAnswer()
                                .getCorrectAnswerFeedback());
            }
            else
            {
              mbean.setFeedback(data.getPublishedAnswer()
                                .getInCorrectAnswerFeedback());
            }
          }
          break;
        }
      }

      beans.add(mbean);
    }
    bean.setMatchingArray(beans);
    bean.setAnswers(newAnswers); // Change the answers to just text
  }

  public void populateFib(ItemDataIfc item, ItemContentsBean bean)
  {
    // Only one text in FIB
    ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
    ArrayList fibs = new ArrayList();
    String alltext = new String(text.getText());
    ArrayList texts = extractFIBTextArray(alltext);
    int i = 0;
    Iterator iter = text.getAnswerArraySorted().iterator();
    while (iter.hasNext())
    {
      AnswerIfc answer = (AnswerIfc) iter.next();
      FibBean fbean = new FibBean();
      fbean.setItemContentsBean(bean);
      fbean.setAnswer(answer);
      fbean.setText( (String) texts.toArray()[i++]);
      fbean.setHasInput(true);

      ArrayList datas = bean.getItemGradingDataArray();
      if (datas == null || datas.isEmpty())
      {
        fbean.setIsCorrect(false);
      }
      else
      {
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext())
        {
          ItemGradingData data = (ItemGradingData) iter2.next();
          if (data.getPublishedAnswer().getId().equals(answer.getId()))
          {
            fbean.setItemGradingData(data);
            fbean.setResponse(data.getAnswerText());
            fbean.setIsCorrect(false);
            if (answer.getText() == null)
            {
              answer.setText("");
            }
            StringTokenizer st2 = new StringTokenizer(answer.getText(), "|");
            while (st2.hasMoreTokens())
            {
              String nextT = st2.nextToken();
              if (data.getAnswerText() != null &&
                  data.getAnswerText().equalsIgnoreCase(nextT))
              {
                fbean.setIsCorrect(true);
              }
            }
          }
        }
      }
      fibs.add(fbean);
    }

    FibBean fbean = new FibBean();
    fbean.setText( (String) texts.toArray()[i]);
    fbean.setHasInput(false);
    fibs.add(fbean);

    bean.setFibArray(fibs);
  }

  private static ArrayList extractFIBTextArray(String alltext)
  {
    ArrayList texts = new ArrayList();

    while (alltext.indexOf("{") > -1)
    {
      int alltextLeftIndex = alltext.indexOf("{");
      int alltextRightIndex = alltext.indexOf("}");

      String tmp = alltext.substring(0, alltextLeftIndex);
      alltext = alltext.substring(alltextRightIndex + 1);
      texts.add(tmp);
      // there are no more "}", exit loop
      if (alltextRightIndex == -1)
      {
        break;
      }
    }
    texts.add(alltext);
    return texts;
  }

  /**
   * Tests that malformed FIB text does not create an excessive number of loops.
   * Quickie test, nice to have: refine, move to JUnit.
   * @param verbose
   * @return
   */
  private static boolean testExtractFIBTextArray(boolean verbose)
  {
    boolean status = true;
    String[] testsuite = {
      "aaa{bbb}ccc{ddd}eee", // correct
      "aaa{bbb}ccc{", //incorrect
      "aaa{bbb}ccc}", //incorrect
      "aaa{bbb{ccc}ddd}eee" //incorrect
    };

    ArrayList testResult;

    try
    {
      for (int i = 0; i < testsuite.length; i++)
      {
        testResult = extractFIBTextArray(testsuite[i]);
        if (verbose)
        {
          System.out.println("Extracting: " + testsuite[i]);
          for (int j = 0; j < testResult.size(); j++)
          {
            System.out.println("testResult.get(" + j +
                               ")="+testResult.get(j));
          }
        }
        if (testResult.size() > 10)
        {
          if (verbose)
          {
            System.out.println("Extraction failed: exceeded reasonable size.");
          }
          return false;
        }
      }
    }
    catch (Exception ex)
    {
      if (verbose)
      {
        System.out.println("Extraction failed: " + ex);
      }
      return false;
    }

    return status;
  }

  public static void main (String args[])
  {
    boolean verbose = true;
    if (args.length>0 && "false".equals(args[0]))
    {
      verbose = false;
    }

    System.out.println("testExtractFIBTextArray result="+testExtractFIBTextArray(verbose));;

  }

  public String getAgentString(){
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
    String agentString = person.getId();
    //log.info("***agentString="+agentString);
    return agentString;
  }

  //added by daisy, used by DeliverBean.addMediaToItemGrading
  public void processAction(ActionEvent ae, boolean resetPageContents) throws
    AbortProcessingException{
    this.resetPageContents = resetPageContents;
    processAction(null);
  }

  public String getPublishedAssessmentId(DeliveryBean delivery){
    String id = cu.lookupParam("publishedId");
    if (id == null){
      id = delivery.getAssessmentId();
    }
    return id;
  }

  private void clearElapsedTime(DeliveryBean delivery){
    if (!delivery.isTimeRunning()) {
      delivery.setTimeElapse(null);
    }
    delivery.setTimeOutSubmission("false");

    if (delivery.getTimeElapse() == null){
      delivery.setTimeElapse("0");
    }
  }

  private void setFeedbackMode(DeliveryBean delivery){
    int action = delivery.getActionMode();
    String showfeedbacknow = cu.lookupParam("showfeedbacknow");
    delivery.setFeedback("false");
    delivery.setNoFeedback("false");
    switch (action){
    case 1: // take assessment
    case 5: // take assessment via url
            if (showfeedbacknow != null && showfeedbacknow.equals("true")) {
              delivery.setFeedback("true");
            }
            break;

    case 3: // review assessment
    case 4: // grade assessment
            if (delivery.getFeedbackComponent()!=null 
                && (delivery.getFeedbackComponent().getShowImmediate() 
                    || (delivery.getFeedbackComponent().getShowDateFeedback())
                        && delivery.getSettings()!=null
                        && delivery.getSettings().getFeedbackDate()!=null
                        && delivery.getSettings().getFeedbackDate().before(new Date()))) {
              delivery.setFeedback("true");
            }
            break;

    default:break;
    }

    String nofeedback = cu.lookupParam("nofeedback");
    if (nofeedback != null && nofeedback.equals("true")) {
      delivery.setNoFeedback("true");
    }
  }

  public PublishedAssessmentFacade getPublishedAssessment(DeliveryBean delivery, String id){
    PublishedAssessmentFacade publishedAssessment = null;
    if (delivery.getPublishedAssessment() != null &&
        delivery.getPublishedAssessment().getPublishedAssessmentId().toString().
        equals(id)) {
      publishedAssessment = delivery.getPublishedAssessment();
    }
    else {
      publishedAssessment =
        (new PublishedAssessmentService()).getPublishedAssessment(id);
    }
    return publishedAssessment;
  }

  public void setShowStudentScore(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment){
    if (Boolean.TRUE.equals(
        publishedAssessment.getAssessmentFeedback().getShowStudentScore())) {
      if (delivery.getFeedbackComponent()!=null && 
          delivery.getFeedbackComponent().getShowDateFeedback() && !delivery.getFeedbackOnDate())
        delivery.setShowStudentScore(false);
      else
        delivery.setShowStudentScore(true);
    }
    else {
      delivery.setShowStudentScore(false);
    }
  }

  private void setDisplayByAssessment(DeliveryBean delivery){
    delivery.getSettings().setFormatByAssessment(true);
    delivery.getSettings().setFormatByPart(false);
    delivery.getSettings().setFormatByQuestion(false);
  }

  private void setDeliverySettings(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment){
    if (delivery.getSettings() == null){
      BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
      listener.populateBeanFromPub(delivery, publishedAssessment);
    }
  }


}
