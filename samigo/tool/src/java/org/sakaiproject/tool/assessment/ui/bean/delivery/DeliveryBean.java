/*
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
 */

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.MimeTypesLocator;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;

//cwen
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.api.kernel.tool.Placement;
// note: we should wrap above dependency in a backend service--esmiley

/**
 *
 * @author casong
 * @author esmiley@stanford.edu added agentState
 * $Id$
 *
 * Used to be org.navigoproject.ui.web.asi.delivery.XmlDeliveryForm.java
 */
public class DeliveryBean
  implements Serializable
{
  private static Log log = LogFactory.getLog(DeliveryBean.class);

  private String assessmentId;
  private String assessmentTitle;
  private ArrayList markedForReview;
  private ArrayList blankItems;
  private ArrayList markedForReviewIdents;
  private ArrayList blankItemIdents;
  private boolean reviewMarked;
  private boolean reviewAll;
  private boolean reviewBlank;
  private int itemIndex;
  private int size;
  private String action;
  private Date beginTime;
  private String endTime;
  private String currentTime;
  private String multipleAttempts;
  private String timeOutSubmission;
  private String submissionTicket;
  private String timeElapse;
  private String username;
  private int sectionIndex;
  private boolean previous;
  private String duration;
  private String url;
  private String confirmation;
  private String outcome;

  //Settings
  private String questionLayout;
  private String navigation;
  private String numbering;
  private String feedback;
  private String noFeedback;
  private String statistics;
  private String creatorName;

  private FeedbackComponent feedbackComponent;
  private boolean feedbackOnDate;
  private String errorMessage;
  private SettingsDeliveryBean settings;
  private java.util.Date dueDate;
  private boolean statsAvailable;
  private boolean submitted;
  private boolean graded;
  private String graderComment;
  private String rawScore;
  private String grade;
  private java.util.Date submissionDate;
  private java.util.Date submissionTime;
  private String image;
  private boolean hasImage;
  private String instructorMessage;
  private String courseName;
  private String timeLimit;
  private int timeLimit_hour;
  private int timeLimit_minute;
  private ContentsDeliveryBean tableOfContents;
  private String submissionId;
  private String submissionMessage;
  private String instructorName;
  private ContentsDeliveryBean pageContents;
  private int submissionsRemaining;
  private boolean forGrade;
  private String password;

  // For paging
  private int partIndex;
  private int questionIndex;
  private boolean next_page;
  private boolean reload = true;

  // daisy added these for SelectActionListener
  private boolean notTakeable = true;
  private boolean pastDue;
  private long subTime;
  private long raw;
  private String takenHours;
  private String takenMinutes;
  private AssessmentGradingData adata;
  private PublishedAssessmentFacade publishedAssessment;
  private java.util.Date feedbackDate;
  private String feedbackDelivery;
  private String showScore;
  private boolean hasTimeLimit;

  // daisyf added for servlet Login.java, to support anonymous login with
  // publishedUrl
  private boolean anonymousLogin = false;
  private String contextPath;
  private boolean initAgentAccessString = false;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -1090852048737428722L;
  private boolean showStudentScore;

  // SAM-387
  // esmiley added to track if timer has been started in timed assessments
  private boolean timeRunning;
  // SAM-535
  // esmiley added to track JavaScript
  private String javaScriptEnabledCheck;

  //cwen
  private String siteId;

  private boolean beginAssessment;

  // this instance tracks if the Agent is taking a test via URL, as well as
  // current agent string (if assigned). SAK-1927: esmiley
  private AgentFacade deliveryAgent;

  /**
   * Creates a new DeliveryBean object.
   */
  public DeliveryBean()
  {
    deliveryAgent = new AgentFacade();
  }

  /**
   *
   *
   * @return
   */
  public int getItemIndex()
  {
    return this.itemIndex;
  }

  /**
   *
   *
   * @param itemIndex
   */
  public void setItemIndex(int itemIndex)
  {
    this.itemIndex = itemIndex;
  }

  /**
   *
   *
   * @return
   */
  public int getSize()
  {
    return this.size;
  }

  /**
   *
   *
   * @param size
   */
  public void setSize(int size)
  {
    this.size = size;
  }

  /**
   *
   *
   * @return
   */
  public String getAssessmentId()
  {
    return assessmentId;
  }

  /**
   *
   *
   * @param assessmentId
   */
  public void setAssessmentId(String assessmentId)
  {
    this.assessmentId = assessmentId;
  }

  /**
   *
   *
   * @return
   */
  public ArrayList getMarkedForReview()
  {
    return markedForReview;
  }

  /**
   *
   *
   * @param markedForReview
   */
  public void setMarkedForReview(ArrayList markedForReview)
  {
    this.markedForReview = markedForReview;
  }

  /**
   *
   *
   * @return
   */
  public boolean getReviewMarked()
  {
    return this.reviewMarked;
  }

  /**
   *
   *
   * @param reviewMarked
   */
  public void setReviewMarked(boolean reviewMarked)
  {
    this.reviewMarked = reviewMarked;
  }

  /**
   *
   *
   * @return
   */
  public boolean getReviewAll()
  {
    return this.reviewAll;
  }

  /**
   *
   *
   * @param reviewAll
   */
  public void setReviewAll(boolean reviewAll)
  {
    this.reviewAll = reviewAll;
  }

  /**
   *
   *
   * @return
   */
  public String getAction()
  {
    return this.action;
  }

  /**
   *
   *
   * @param action
   */
  public void setAction(String action)
  {
    this.action = action;
  }

  /**
   *
   *
   * @return
   */
  public Date getBeginTime()
  {
    return beginTime;
  }

  /**
   *
   *
   * @param beginTime
   */
  public void setBeginTime(Date beginTime)
  {
    this.beginTime = beginTime;
  }

  /**
   *
   *
   * @return
   */
  public String getEndTime()
  {
    return endTime;
  }

  /**
   *
   *
   * @param endTime
   */
  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  /**
   *
   *
   * @return
   */
  public String getCurrentTime()
  {
    return this.currentTime;
  }

  /**
   *
   *
   * @param currentTime
   */
  public void setCurrentTime(String currentTime)
  {
    this.currentTime = currentTime;
  }

  /**
   *
   *
   * @return
   */
  public String getMultipleAttempts()
  {
    return this.multipleAttempts;
  }

  /**
   *
   *
   * @param multipleAttempts
   */
  public void setMultipleAttempts(String multipleAttempts)
  {
    this.multipleAttempts = multipleAttempts;
  }

  /**
   *
   *
   * @return
   */
  public String getTimeOutSubmission()
  {
    return this.timeOutSubmission;
  }

  /**
   *
   *
   * @param timeOutSubmission
   */
  public void setTimeOutSubmission(String timeOutSubmission)
  {
    this.timeOutSubmission = timeOutSubmission;
  }

  /**
   *
   *
   * @return
   */
  public java.util.Date getSubmissionTime()
  {
    return submissionTime;
  }

  /**
   *
   *
   * @param submissionTime
   */
  public void setSubmissionTime(java.util.Date submissionTime)
  {
    this.submissionTime = submissionTime;
  }

  /**
   *
   *
   * @return
   */
  public String getTimeElapse()
  {
    return timeElapse;
  }

  /**
   *
   *
   * @param timeElapse
   */
  public void setTimeElapse(String timeElapse)
  {
    try{
      float limit = (new Float(getTimeLimit())).floatValue();
      float elapsed = (new Float(timeElapse)).floatValue();
      if (limit > elapsed)
        this.timeElapse = timeElapse;
      else
        this.timeElapse = getTimeLimit();
    }
    catch (Exception e){
      log.warn("setTimeElapse error:"+e.getMessage());
    }
  }

  /**
   *
   *
   * @return
   */
  public String getSubmissionTicket()
  {
    return submissionTicket;
  }

  /**
   *
   *
   * @param submissionTicket
   */
  public void setSubmissionTicket(String submissionTicket)
  {
    this.submissionTicket = submissionTicket;
  }

  /**
   *
   *
   * @return
   */
  public int getDisplayIndex()
  {
    return this.itemIndex + 1;
  }

  /**
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  /**
   *
   *
   * @param username
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   *
   *
   * @return
   */
  public String getAssessmentTitle()
  {
    return assessmentTitle;
  }

  /**
   *
   *
   * @param assessmentTitle
   */
  public void setAssessmentTitle(String assessmentTitle)
  {
    this.assessmentTitle = assessmentTitle;
  }

  /**
   *
   *
   * @return
   */
  public ArrayList getBlankItems()
  {
    return this.blankItems;
  }

  /**
   *
   *
   * @param blankItems
   */
  public void setBlankItems(ArrayList blankItems)
  {
    this.blankItems = blankItems;
  }

  /**
   *
   *
   * @return
   */
  public boolean getReviewBlank()
  {
    return reviewBlank;
  }

  /**
   *
   *
   * @param reviewBlank
   */
  public void setReviewBlank(boolean reviewBlank)
  {
    this.reviewBlank = reviewBlank;
  }

  /**
   *
   *
   * @return
   */
  public ArrayList getMarkedForReviewIdents()
  {
    return markedForReviewIdents;
  }

  /**
   *
   *
   * @param markedForReviewIdents
   */
  public void setMarkedForReviewIdents(ArrayList markedForReviewIdents)
  {
    this.markedForReviewIdents = markedForReviewIdents;
  }

  /**
   *
   *
   * @return
   */
  public ArrayList getBlankItemIdents()
  {
    return blankItemIdents;
  }

  /**
   *
   *
   * @param blankItemIdents
   */
  public void setBlankItemIdents(ArrayList blankItemIdents)
  {
    this.blankItemIdents = blankItemIdents;
  }

  /**
   *
   *
   * @return
   */
  public int getSectionIndex()
  {
    return this.sectionIndex;
  }

  /**
   *
   *
   * @param sectionIndex
   */
  public void setSectionIndex(int sectionIndex)
  {
    this.sectionIndex = sectionIndex;
  }

  /**
   *
   *
   * @return
   */
  public boolean getPrevious()
  {
    return previous;
  }

  /**
   *
   *
   * @param previous
   */
  public void setPrevious(boolean previous)
  {
    this.previous = previous;
  }

  //Settings
  public String getQuestionLayout()
  {
    return questionLayout;
  }

  /**
   *
   *
   * @param questionLayout
   */
  public void setQuestionLayout(String questionLayout)
  {
    this.questionLayout = questionLayout;
  }

  /**
   *
   *
   * @return
   */
  public String getNavigation()
  {
    return navigation;
  }

  /**
   *
   *
   * @param navigation
   */
  public void setNavigation(String navigation)
  {
    this.navigation = navigation;
  }

  /**
   *
   *
   * @return
   */
  public String getNumbering()
  {
    return numbering;
  }

  /**
   *
   *
   * @param numbering
   */
  public void setNumbering(String numbering)
  {
    this.numbering = numbering;
  }

  /**
   *
   *
   * @return
   */
  public String getFeedback()
  {
    return feedback;
  }

  /**
   *
   *
   * @param feedback
   */
  public void setFeedback(String feedback)
  {
    this.feedback = feedback;
  }

  /**
   *
   *
   * @return
   */
  public String getNoFeedback()
  {
    return noFeedback;
  }

  /**
   *
   *
   * @param noFeedback
   */
  public void setNoFeedback(String noFeedback)
  {
    this.noFeedback = noFeedback;
  }

  /**
   *
   *
   * @return
   */
  public String getStatistics()
  {
    return statistics;
  }

  /**
   *
   *
   * @param statistics
   */
  public void setStatistics(String statistics)
  {
    this.statistics = statistics;
  }

  /**
   *
   *
   * @return
   */
  public FeedbackComponent getFeedbackComponent()
  {
    return feedbackComponent;
  }

  /**
   *
   *
   * @param feedbackComponent
   */
  public void setFeedbackComponent(FeedbackComponent feedbackComponent)
  {
    this.feedbackComponent = feedbackComponent;
  }

  /**
   * Types of feedback in FeedbackComponent:
   *
   * SHOW CORRECT SCORE
   * SHOW STUDENT SCORE
   * SHOW ITEM LEVEL
   * SHOW SECTION LEVEL
   * SHOW GRADER COMMENT
   * SHOW STATS
   * SHOW QUESTION
   * SHOW RESPONSE
   **/

  /**
   * @return
   */
  public SettingsDeliveryBean getSettings()
  {
    return settings;
  }

  /**
   * @param bean
   */
  public void setSettings(SettingsDeliveryBean settings)
  {
    this.settings = settings;
  }

  /**
   * @return
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }

  /**
   * @param string
   */
  public void setErrorMessage(String string)
  {
    errorMessage = string;
  }

  /**
   * @return
   */
  public String getDuration()
  {
    return duration;
  }

  /**
   * @param string
   */
  public void setDuration(String string)
  {
    duration = string;
  }

  /**
   * @return
   */
  public String getCreatorName()
  {
    return creatorName;
  }

  /**
   * @param string
   */
  public void setCreatorName(String string)
  {
    creatorName = string;
  }

  public java.util.Date getDueDate()
  {
    return dueDate;
  }

  public void setDueDate(java.util.Date dueDate)
  {
    this.dueDate = dueDate;
  }

  public boolean isStatsAvailable()
  {
    return statsAvailable;
  }

  public void setStatsAvailable(boolean statsAvailable)
  {
    this.statsAvailable = statsAvailable;
  }

  public boolean isSubmitted()
  {
    return submitted;
  }

  public void setSubmitted(boolean submitted)
  {
    this.submitted = submitted;
  }

  public boolean isGraded()
  {
    return graded;
  }

  public void setGraded(boolean graded)
  {
    this.graded = graded;
  }

  public boolean getFeedbackOnDate()
  {
    return feedbackOnDate;
  }

  public void setFeedbackOnDate(boolean feedbackOnDate)
  {
    this.feedbackOnDate = feedbackOnDate;
  }

  public String getGraderComment()
  {
    if (graderComment == null)
    {
      return "";
    }
    return graderComment;
  }

  public void setGraderComment(String newComment)
  {
    graderComment = newComment;
  }

  public String getRawScore()
  {
    return rawScore;
  }

  public void setRawScore(String rawScore)
  {
    this.rawScore = rawScore;
  }

  public long getRaw()
  {
    return raw;
  }

  public void setRaw(long raw)
  {
    this.raw = raw;
  }

  public String getGrade()
  {
    return grade;
  }

  public void setGrade(String grade)
  {
    this.grade = grade;
  }

  public java.util.Date getSubmissionDate()
  {
    return submissionDate;
  }

  public void setSubmissionDate(java.util.Date submissionDate)
  {
    this.submissionDate = submissionDate;
  }

  public String getImage()
  {
    return image;
  }

  public void setImage(String image)
  {
    this.image = image;
  }

  public boolean isHasImage()
  {
    return hasImage;
  }

  public void setHasImage(boolean hasImage)
  {
    this.hasImage = hasImage;
  }

  public String getInstructorMessage()
  {
    return instructorMessage;
  }

  public void setInstructorMessage(String instructorMessage)
  {
    this.instructorMessage = instructorMessage;
  }

  public String getCourseName()
  {
    return courseName;
  }

  public void setCourseName(String courseName)
  {
    this.courseName = courseName;
  }

  public String getTimeLimit()
  {
    return timeLimit;
  }

  public void setTimeLimit(String timeLimit)
  {
    this.timeLimit = timeLimit;
  }

  public int getTimeLimit_hour()
  {
    return timeLimit_hour;
  }

  public void setTimeLimit_hour(int timeLimit_hour)
  {
    this.timeLimit_hour = timeLimit_hour;
  }

  public int getTimeLimit_minute()
  {
    return timeLimit_minute;
  }

  public void setTimeLimit_minute(int timeLimit_minute)
  {
    this.timeLimit_minute = timeLimit_minute;
  }

  /**
   * Bean with table of contents information and
   * a list of all the sections in the assessment
   * which in  turn has a list of all the item contents.
   * @return table of contents
   */
  public ContentsDeliveryBean getTableOfContents()
  {
    return tableOfContents;
  }

  /**
   * Bean with table of contents information and
   * a list of all the sections in the assessment
   * which in  turn has a list of all the item contents.
   * @param tableOfContents table of contents
   */
  public void setTableOfContents(ContentsDeliveryBean tableOfContents)
  {
    this.tableOfContents = tableOfContents;
  }

  /**
   * Bean with a list of all the sections in the current page
   * which in turn has a list of all the item contents for the page.
   *
   * This is like the table of contents, but the selections are restricted to
   * that on one page.
   *
   *  Since these properties are on a page delivery basis--if:
   *  1. there is only one item per page the list of items will
   *  contain one item and the list of parts will return one part, or if--
   *
   *  2. there is one part per page the list of items will be that
   *  for that part only and there will only be one part, however--
   *
   *  3. if it is all parts and items on a single page there
   *  will be a list of all parts and items.
   *
   * @return ContentsDeliveryBean
   */
  public ContentsDeliveryBean getPageContents()
  {
    return pageContents;
  }

  /**
   * Bean with a list of all the sections in the current page
   * which in turn has a list of all the item contents for the page.
   *
   *  Since these properties are on a page delivery basis--if:
   *  1. there is only one item per page the list of items will
   *  contain one item and the list of parts will return one part, or if--
   *
   *  2. there is one part per page the list of items will be that
   *  for that part only and there will only be one part, however--
   *
   *  3. if it is all parts and items on a single page there
   *  will be a list of all parts and items.
   *
   * @param pageContents ContentsDeliveryBean
   */
  public void setPageContents(ContentsDeliveryBean pageContents)
  {
    this.pageContents = pageContents;
  }

  public String getSubmissionId()
  {
    return submissionId;
  }

  public void setSubmissionId(String submissionId)
  {
    this.submissionId = submissionId;
  }

  public String getSubmissionMessage()
  {
    return submissionMessage;
  }

  public void setSubmissionMessage(String submissionMessage)
  {
    this.submissionMessage = submissionMessage;
  }

  public int getSubmissionsRemaining()
  {
    return submissionsRemaining;
  }

  public void setSubmissionsRemaining(int submissionsRemaining)
  {
    this.submissionsRemaining = submissionsRemaining;
  }

  public String getInstructorName()
  {
    return instructorName;
  }

  public void setInstructorName(String instructorName)
  {
    this.instructorName = instructorName;
  }

  public boolean getForGrade()
  {
    return forGrade;
  }

  public void setForGrade(boolean newfor)
  {
    forGrade = newfor;
  }

  public String submitForGrade()
  {
    if (isTimeRunning() && timeExpired()) // is timed assessment? and time has expired?
      return "timeExpired";
    recordTimeElapsed();
    SessionUtil.setSessionTimeout(FacesContext.getCurrentInstance(), this, false);

    forGrade = true;
    SubmitToGradingActionListener listener =
      new SubmitToGradingActionListener();
    listener.processAction(null);

    String returnValue="submitAssessment";
    if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) // this is for accessing via published url
    {
      returnValue="anonymousThankYou";
    }
    forGrade = false;
    SelectActionListener l2 = new SelectActionListener();
    l2.processAction(null);
    reload = true;

    // finish within time limit, clean timedAssessment from queue
    removeTimedAssessmentFromQueue();
    return returnValue;
  }

  public String saveAndExit()
  {
    if (isTimeRunning() && timeExpired())
      return "timeExpired";
    recordTimeElapsed();
    FacesContext context = FacesContext.getCurrentInstance();
    SessionUtil.setSessionTimeout(context, this, false);
    log.debug("***DeliverBean.saveAndEXit face context =" + context);

    forGrade = false;
    SubmitToGradingActionListener listener =
      new SubmitToGradingActionListener();
    listener.processAction(null);

    String returnValue = "select";
    if (this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    { // if this is access via url, display quit message
      log.debug("**anonymous login, go to quit");
      returnValue = "anonymousQuit";
    }

    SelectActionListener l2 = new SelectActionListener();
    l2.processAction(null);
    reload = true;

    // quit within time limit, clean timedAssessment from queue
    removeTimedAssessmentFromQueue();
    return returnValue;
  }

  public String next_page()
  {
    if (isTimeRunning() && timeExpired())
      return "timeExpired";
    recordTimeElapsed();
    if (getSettings().isFormatByPart())
    {
      partIndex++;
    }
    if (getSettings().isFormatByQuestion())
    {
      questionIndex++;

    }
    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
        || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    {
      SubmitToGradingActionListener listener =
        new SubmitToGradingActionListener();
      listener.processAction(null);
    }

    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    reload = false;
    return "takeAssessment";
  }

  public String previous()
  {
    if (isTimeRunning() && timeExpired())
      return "timeExpired";
    recordTimeElapsed();
    if (getSettings().isFormatByPart())
    {
      partIndex--;
    }
    if (getSettings().isFormatByQuestion())
    {
      questionIndex--;

    }
    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
        || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    {
      SubmitToGradingActionListener listener =
        new SubmitToGradingActionListener();
      listener.processAction(null);
    }
    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    reload = false;
    return "takeAssessment";
  }

  // this is the PublishedAccessControl.finalPageUrl
  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getConfirmation()
  {
    return confirmation;
  }

  public void setConfirmation(String confirmation)
  {
    this.confirmation = confirmation;
  }

  /**
   * if required, assessment password
   * @return password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * if required, assessment password
   * @param string assessment password
   */
  public void setPassword(String string)
  {
    password = string;
  }

  public String validatePassword()
  {
    log.debug("**** username=" + username);
    log.debug("**** password=" + password);
    log.debug("**** setting username=" + getSettings().getUsername());
    log.debug("**** setting password=" + getSettings().getPassword());
    if (password == null || username == null)
    {
      return "passwordAccessError";
    }
    if (password.equals(getSettings().getPassword()) &&
        username.equals(getSettings().getUsername()))
    {
      // in post 2.1, clicking at Begin Assessment takes users to the
      // 1st question.
      return "takeAssessment";
    }
    else
    {
      return "passwordAccessError";
    }
  }

  public String validateIP()
  {
    String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.
                     getCurrentInstance().getExternalContext().getRequest()).
      getRemoteAddr();
    Iterator addresses = getSettings().getIpAddresses().iterator();
    while (addresses.hasNext())
    {
      String next = ( (PublishedSecuredIPAddress) addresses.next()).
        getIpAddress();
      if (next != null && next.indexOf("*") > -1)
      {
        next = next.substring(0, next.indexOf("*"));
      }
      if (next == null || next.trim().equals("") ||
          thisIp.trim().startsWith(next.trim()))
      {
        // in post 2.1, clicking at Begin Assessment takes users to the
        // 1st question.
        return "takeAssessment";
      }
    }
    return "ipAccessError";
  }

  public String validate()
  {
    try
    {
      String results = "";
      if (!getSettings().getUsername().equals(""))
      {
        results = validatePassword();
      }
      if (!results.equals("passwordAccessError") &&
          getSettings().getIpAddresses() != null &&
          !getSettings().getIpAddresses().isEmpty())
      {
        results = validateIP();
      }
      if (results.equals(""))
      {
        // in post 2.1, clicking at Begin Assessment takes users to the
        // 1st question.
        return "takeAssessment";
      }
      return results;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return "accessError";
    }
  }

  public String pvalidate()
  {
    // in post 2.1, clicking at Begin Assessment takes users to the
    // 1st question.
    return "takeAssessment";
  }

  // Skipped paging methods
  public int getPartIndex()
  {
    return partIndex;
  }

  public void setPartIndex(int newindex)
  {
    partIndex = newindex;
  }

  public int getQuestionIndex()
  {
    return questionIndex;
  }

  public void setQuestionIndex(int newindex)
  {
    questionIndex = newindex;
  }

  public boolean getContinue()
  {
    return next_page;
  }

  public void setContinue(boolean docontinue)
  {
    next_page = docontinue;
  }

  public boolean getReload()
  {
    return reload;
  }

  public void setReload(boolean doreload)
  {
    reload = doreload;
  }

  // Store for paging
  public AssessmentGradingData getAssessmentGrading()
  {
    return adata;
  }

  public void setAssessmentGrading(AssessmentGradingData newdata)
  {
    adata = newdata;
  }

  private byte[] getMediaStream(String mediaLocation)
  {
    FileInputStream mediaStream = null;
    FileInputStream mediaStream2 = null;
    byte[] mediaByte = new byte[0];
    try
    {
      int i = 0;
      int size = 0;
      mediaStream = new FileInputStream(mediaLocation);
      if (mediaStream != null)
      {
        while ( (i = mediaStream.read()) != -1)
        {
          size++;
        }
      }
      mediaStream2 = new FileInputStream(mediaLocation);
      mediaByte = new byte[size];
      mediaStream2.read(mediaByte, 0, size);

      FileOutputStream out = new FileOutputStream("/tmp/test.txt");
      out.write(mediaByte);
    }
    catch (FileNotFoundException ex)
    {
      log.debug("file not found=" + ex.getMessage());
    }
    catch (IOException ex)
    {
      log.debug("io exception=" + ex.getMessage());
    }
    finally
    {
      try
      {
        mediaStream.close();
      }
      catch (IOException ex1)
      {
      }
    }
    return mediaByte;
  }

  /**
   * This method is used by jsf/delivery/deliveryFileUpload.jsp
   *   <corejsf:upload
   *     target="/jsf/upload_tmp/assessment#{delivery.assessmentId}/
   *             question#{question.itemData.itemId}/admin"
   *     valueChangeListener="#{delivery.addMediaToItemGrading}" />
   */
  public String addMediaToItemGrading(javax.faces.event.ValueChangeEvent e)
  {
    String mediaLocation = (String) e.getNewValue();
    log.debug("***2a. addMediaToItemGrading, new value =" + mediaLocation);
    return addMediaToItemGrading(mediaLocation);
  }
    /**
     * This method is used by jsf/delivery/deliverAudioRecording.jsp and
     * is called by addMediaToItemGrading(javax.faces.event.ValueChangeEvent e)
     *
     * @param mediaLocation the  media location
     * @return the action string
     */
    public String addMediaToItemGrading(String mediaLocation)
    {
    GradingService gradingService = new GradingService();
    PublishedAssessmentService publishedService = new
      PublishedAssessmentService();
    PublishedAssessmentFacade publishedAssessment = publishedService.
      getPublishedAssessment(assessmentId);
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
    String agent = person.getId();

    // 1. create assessmentGrading if it is null
    if (this.adata == null)
    {
      adata = new AssessmentGradingData();
      adata.setAgentId(agent);
      adata.setPublishedAssessment(publishedAssessment.getData());
      log.debug("***1a. addMediaToItemGrading, getForGrade()=" + getForGrade());
      adata.setForGrade(new Boolean(getForGrade()));
      adata.setAttemptDate(getBeginTime());
      gradingService.saveOrUpdateAssessmentGrading(adata);
    }
    log.info("***1b. addMediaToItemGrading, adata=" + adata);

    // 2. format of the media location is: assessmentXXX/questionXXX/agentId/myfile
    // 3. get the questionId (which is the PublishedItemData.itemId)
    int assessmentIndex = mediaLocation.indexOf("assessment");
    int questionIndex = mediaLocation.indexOf("question");
    int agentIndex = mediaLocation.indexOf("/", questionIndex + 8);
    //cwen
    if(agentIndex < 0 )
    {
      agentIndex = mediaLocation.indexOf("\\", questionIndex + 8);
    }
    String pubAssessmentId = mediaLocation.substring(assessmentIndex + 10,
      questionIndex - 1);
    String questionId = mediaLocation.substring(questionIndex + 8, agentIndex);
    log.debug("***3a. addMediaToItemGrading, questionId =" + questionId);
    log.debug("***3b. addMediaToItemGrading, assessmentId =" + assessmentId);

    // 4. prepare itemGradingData and attach it to assessmentGarding
    PublishedItemData item = publishedService.loadPublishedItem(questionId);
    log.debug("***4a. addMediaToItemGrading, item =" + item);
    log.debug("***4b. addMediaToItemGrading, itemTextArray =" +
              item.getItemTextArray());
    log.debug("***4c. addMediaToItemGrading, itemText(0) =" +
              item.getItemTextArray().get(0));
    // there is only one text in audio question
    PublishedItemText itemText = (PublishedItemText) item.
      getItemTextArraySorted().get(0);
    ItemGradingData itemGradingData = getItemGradingData(questionId);
    boolean newItemGradingData = false;
    if (itemGradingData == null)
    {
      newItemGradingData = true;
      itemGradingData = new ItemGradingData();
      itemGradingData.setAssessmentGrading(adata);
      itemGradingData.setPublishedItem(item);
      itemGradingData.setPublishedItemText(itemText);
      itemGradingData.setSubmittedDate(new Date());
      itemGradingData.setAgentId(agent);
      itemGradingData.setOverrideScore(new Float(0));
    }
    setAssessmentGrading(adata);

    // 5. save AssessmentGardingData with ItemGardingData
    Set itemDataSet = adata.getItemGradingSet();
    log.debug("***5a. addMediaToItemGrading, itemDataSet=" + itemDataSet);
    if (itemDataSet == null)
    {
      itemDataSet = new HashSet();
    }
    itemDataSet.add(itemGradingData);
    adata.setItemGradingSet(itemDataSet);
    gradingService.saveOrUpdateAssessmentGrading(adata);
    log.debug("***5b. addMediaToItemGrading, saved=" + adata);

    //if media is uploaded, create media record and attach to itemGradingData
    if (mediaIsValid())
      saveMedia(agent, mediaLocation, itemGradingData, gradingService);

    // 8. do whatever need doing
    DeliveryActionListener dlistener = new DeliveryActionListener();
    // false => do not reset the entire current delivery.pageContents.
    // we will do it ourselves and only update the question that this media
    // is attached to
    dlistener.processAction(null, false);
    if (newItemGradingData)
      attachToItemContentBean(itemGradingData, questionId);

    // 9. do the timer thing
    Integer timeLimit = null;
    if (adata != null && adata.getPublishedAssessment() != null
        && adata.getPublishedAssessment().getAssessmentAccessControl() != null)
    {
      timeLimit = adata.getPublishedAssessment().getAssessmentAccessControl().
        getTimeLimit();
    }
    if (timeLimit != null && timeLimit.intValue() > 0)
    {
      UpdateTimerListener l3 = new UpdateTimerListener();
      l3.processAction(null);
    }

    reload = true;
    return "takeAssessment";

  }

  public void saveMedia(String agent, String mediaLocation, ItemGradingData itemGradingData,
                        GradingService gradingService){
    // 1. create a media record
    File media = new File(mediaLocation);
    byte[] mediaByte = getMediaStream(mediaLocation);
    String mimeType = MimeTypesLocator.getInstance().getContentType(media);
    //boolean SAVETODB = MediaData.saveToDB();
    boolean SAVETODB = getSaveToDb();
    log.debug("**** SAVETODB=" + SAVETODB);
    MediaData mediaData = null;
    log.debug("***6a. addMediaToItemGrading, itemGradinDataId=" +
              itemGradingData.getItemGradingId());
    log.debug("***6b. addMediaToItemGrading, publishedItemId=" +
              ( (PublishedItemData) itemGradingData.getPublishedItem()).
              getItemId());

    if (SAVETODB)
    { // put the byte[] in
      mediaData = new MediaData(itemGradingData, mediaByte,
                                new Long(mediaByte.length + ""),
                                mimeType, "description", null,
                                media.getName(), false, false, new Integer(1),
                                agent, new Date(),
                                agent, new Date());
    }
    else
    { // put the location in
      mediaData = new MediaData(itemGradingData, null,
                                new Long(mediaByte.length + ""),
                                mimeType, "description", mediaLocation,
                                media.getName(), false, false, new Integer(1),
                                agent, new Date(),
                                agent, new Date());

    }
    Long mediaId = gradingService.saveMedia(mediaData);
    log.debug("mediaId=" + mediaId);
    log.debug("***6c. addMediaToItemGrading, media.itemGradinDataId=" +
              ( (ItemGradingData) mediaData.getItemGradingData()).
              getItemGradingId());
    log.debug("***6d. addMediaToItemGrading, mediaId=" + mediaData.getMediaId());

    // 2. store mediaId in itemGradingRecord.answerText
    log.debug("***7. addMediaToItemGrading, adata=" + adata);
    itemGradingData.setAnswerText(mediaId + "");
    gradingService.saveItemGrading(itemGradingData);
  }


  public boolean mediaIsValid()
  {
    boolean returnValue =true;
    // check if file is too big
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext external = context.getExternalContext();
    Long fileSize = (Long)((ServletContext)external.getContext()).getAttribute("TEMP_FILEUPLOAD_SIZE");
    Long maxSize = (Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX");
    log.info("**** filesize is ="+fileSize);
    log.info("**** maxsize is ="+maxSize);
    ((ServletContext)external.getContext()).removeAttribute("TEMP_FILEUPLOAD_SIZE");
    if (fileSize!=null){
      float fileSize_float = fileSize.floatValue()/1024;
      int tmp = Math.round(fileSize_float * 10.0f);
      fileSize_float = (float)tmp / 10.0f;
      float maxSize_float = maxSize.floatValue()/1024;
      int tmp0 = Math.round(maxSize_float * 10.0f);
      maxSize_float = (float)tmp0 / 10.0f;

      String err1=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "file_upload_error");
      String err2=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "file_uploaded");
      String err3=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "max_size_allowed");
      String err4=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "upload_again");
      String err = err2 + fileSize_float + err3 + maxSize_float + err4;
      context.addMessage("file_upload_error",new FacesMessage(err1));
      context.addMessage("file_upload_error",new FacesMessage(err));
      returnValue = false;
    }
    return returnValue;
  }

  public boolean getNotTakeable()
  {
    return notTakeable;
  }

  public void setNotTakeable(boolean notTakeable)
  {
    this.notTakeable = notTakeable;
  }

  public boolean getPastDue()
  {
    return pastDue;
  }

  public void setPastDue(boolean pastDue)
  {
    this.pastDue = pastDue;
  }

  public long getSubTime()
  {
    return subTime;
  }

  public void setSubTime(long newSubTime)
  {
    subTime = newSubTime;
  }

  public String getSubmissionHours()
  {
    return takenHours;
  }

  public void setSubmissionHours(String newHours)
  {
    takenHours = newHours;
  }

  public String getSubmissionMinutes()
  {
    return takenMinutes;
  }

  public void setSubmissionMinutes(String newMinutes)
  {
    takenMinutes = newMinutes;
  }

  public PublishedAssessmentFacade getPublishedAssessment()
  {
    return publishedAssessment;
  }

  public void setPublishedAssessment(PublishedAssessmentFacade
                                     publishedAssessment)
  {
    this.publishedAssessment = publishedAssessment;
  }

  public java.util.Date getFeedbackDate()
  {
    return feedbackDate;
  }

  public void setFeedbackDate(java.util.Date feedbackDate)
  {
    this.feedbackDate = feedbackDate;
  }

  public String getFeedbackDelivery()
  {
    return feedbackDelivery;
  }

  public void setFeedbackDelivery(String feedbackDelivery)
  {
    this.feedbackDelivery = feedbackDelivery;
  }

  public String getShowScore()
  {
    return showScore;
  }

  public void setShowScore(String showScore)
  {
    this.showScore = showScore;
  }

  public boolean getHasTimeLimit()
  {
    return hasTimeLimit;
  }

  public void setHasTimeLimit(boolean hasTimeLimit)
  {
    this.hasTimeLimit = hasTimeLimit;
  }

  public String getOutcome()
  {
    return outcome;
  }

  public void setOutcome(String outcome)
  {
    this.outcome = outcome;
  }

  public String doit()
  {
    return outcome;
  }

  /*
    public ItemGradingData getItemGradingData(String publishedItemId){
      ItemGradingData itemGradingData = new ItemGradingData();
      if (adata != null){
        GradingService service = new GradingService();
        itemGradingData = service.getItemGradingData(adata.getAssessmentGradingId().toString(), publishedItemId);
        if (itemGradingData == null)
          itemGradingData = new ItemGradingData();
      }
      return itemGradingData;
    }
   */

  public boolean getAnonymousLogin()
  {
    return anonymousLogin;
  }

  public void setAnonymousLogin(boolean anonymousLogin)
  {
    this.anonymousLogin = anonymousLogin;
  }

  public ItemGradingData getItemGradingData(String publishedItemId)
  {
    ItemGradingData selected = null;
    if (adata != null)
    {
      Set items = adata.getItemGradingSet();
      if (items != null)
      {
        Iterator iter = items.iterator();
        while (iter.hasNext())
        {
          ItemGradingData itemGradingData = (ItemGradingData) iter.next();
          String itemPublishedId = itemGradingData.getPublishedItem().getItemId().
            toString();
          if ( (publishedItemId).equals(itemPublishedId))
          {
            log.debug("*** addMediaToItemGrading, same : found it");
            selected = itemGradingData;
          }
          else
          {
            log.debug("*** addMediaToItemGrading, not the same");
          }
        }
        log.debug("*** addMediaToItemGrading, publishedItemId =" +
                  publishedItemId);
        if (selected != null)
        {
          log.debug(
            "*** addMediaToItemGrading, itemGradingData.publishedItemId =" +
            selected.getPublishedItem().getItemId().toString());
        }
      }
    }
    return selected;
  }

  public String getContextPath()
  {
    return contextPath;
  }

  public void setContextPath(String contextPath)
  {
    this.contextPath = contextPath;
  }

  public boolean isShowStudentScore()
  {
    return showStudentScore;
  }

  public void setShowStudentScore(boolean showStudentScore)
  {
    this.showStudentScore = showStudentScore;
  }

  public boolean isTimeRunning()
  {
    return timeRunning;
  }

  public void setTimeRunning(boolean timeRunning)
  {
    this.timeRunning = timeRunning;
  }

  /**
   * Used for a JavaScript enable check.
   */
  public String getJavaScriptEnabledCheck()
  {
    return this.javaScriptEnabledCheck;
  }

  /**
   * Used for a JavaScript enable check.
   */
  public void setJavaScriptEnabledCheck(String javaScriptEnabledCheck)
  {
    this.javaScriptEnabledCheck = javaScriptEnabledCheck;
  }
  //cwen
  public void setSiteId(String siteId)
  {
    this.siteId = siteId;
  }

  public String getSiteId()
  {
    siteId = null;
    Placement currentPlacement = ToolManager.getCurrentPlacement();
    if(currentPlacement != null)
      siteId = currentPlacement.getContext();
    return siteId;
  }

  public String getAgentAccessString()
  {
    return deliveryAgent.getAgentInstanceString();
  }
  public void setAgentAccessString(String agentString)
  {
    deliveryAgent.setAgentInstanceString(agentString);
  }

  public boolean getSaveToDb(){
    String saveToDb=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.ui.bean.delivery.resource.MediaConstants", "SAVETODB");
    if (saveToDb.equals("true"))
      return true;
    else
      return false;
  }

  public void attachToItemContentBean(ItemGradingData itemGradingData, String questionId){
    ArrayList list = new ArrayList();
    list.add(itemGradingData);
    //find out sectionId from questionId
    PublishedAssessmentService publishedService = new
  PublishedAssessmentService();
    PublishedItemData publishedItem = publishedService.
  loadPublishedItem(questionId);
    PublishedSectionData publishedSection = (PublishedSectionData) publishedItem.getSection();
    String sectionId = publishedSection.getSectionId().toString();
    SectionContentsBean partSelected = null;

    //get all partContents
    ArrayList parts = getPageContents().getPartsContents();
    for (int i=0; i<parts.size(); i++){
      SectionContentsBean part = (SectionContentsBean)parts.get(i);
      log.debug("**** question's sectionId"+sectionId);
      log.debug("**** partId"+part.getSectionId());
      if (sectionId.equals(part.getSectionId())){
        partSelected = part;
        break;
      }
    }
    //locate the itemContentBean - the hard way, sigh...
    ArrayList items = new ArrayList();
    if (partSelected!=null)
      items = partSelected.getItemContents();
    for (int j=0; j<items.size(); j++){
      ItemContentsBean item = (ItemContentsBean)items.get(j);
      if ((publishedItem.getItemId()).equals(item.getItemData().getItemId())){ // comparing itemId not object
        item.setItemGradingDataArray(list);
        break;
      }
    }
  }

  // delivery action
  public static int TAKE_ASSESSMENT = 1;
  public static int PREVIEW_ASSESSMENT = 2;
  public static int REVIEW_ASSESSMENT = 3;
  public static int GRADE_ASSESSMENT = 4;
  public static int TAKE_ASSESSMENT_VIA_URL = 5;
  private int actionMode;
  private String actionString;

  public void setActionString(String actionString){
    this.actionString = actionString;
    // the follwoing two values will be evaluated when reviewing assessment
    // based on PublishedFeedback settings
    setFeedback("false");
    setNoFeedback("true");

    if (("previewAssessment").equals(actionString)){
      setActionMode(PREVIEW_ASSESSMENT);
    }
    else if (("reviewAssessment").equals(actionString)){
      setActionMode(REVIEW_ASSESSMENT);
    }
    else if (("gradeAssessment").equals(actionString)){
      setFeedback("true");
      setNoFeedback("false");
      setActionMode(GRADE_ASSESSMENT);
    }
    else if (("takeAssessment").equals(actionString)){
      setActionMode(TAKE_ASSESSMENT);
    }
    else if (("takeAssessmentViaUrl").equals(actionString)){
      setActionMode(TAKE_ASSESSMENT_VIA_URL);
    }
  }

  public String getActionString(){
    return actionString;
  }

  private void setActionMode(int actionMode){
    this.actionMode = actionMode;
  }

  public int getActionMode(){
    return actionMode;
  }

  private long time=0;
  public void setLastTimer(long time){
    this.time = time;
  }

  public long getLastTimer(){
    return time;
  }

  public void recordTimeElapsed(){
    AssessmentGradingData adata = getAssessmentGrading();
    if (adata != null){ // adata would be null during preview
      float f = (new Float(getTimeElapse())).floatValue();
      int i = Math.round(f);
      adata.setTimeElapsed(new Integer(i));
      GradingService gradingService = new GradingService();
      gradingService.saveOrUpdateAssessmentGrading(adata);
      System.out.println("**** recording Time Elapsed. sum="+adata.getTimeElapsed());
    }
  }

  public void recordTimeElapsedTOC(){
    AssessmentGradingData adata = getAssessmentGrading();
    if (adata != null){ // adata would be null during preview
      long currentTime = (new Date()).getTime();
      if (isTimeRunning() && adata != null){ // adata can't be null at this point
        float diff = (currentTime - getLastTimer())/1000;

        int diff_int = Math.round(diff);
        adata.setTimeElapsed(new Integer(diff_int));
        GradingService gradingService = new GradingService();
        gradingService.saveOrUpdateAssessmentGrading(adata);
        setTimeElapse(diff+"");
        System.out.println("**** recording Time Elapsed. sum="+adata.getTimeElapsed());
      }
    }
  }

  public boolean getBeginAssessment(){
    return beginAssessment;
  }

  public void setBeginAssessment(boolean beginAssessment){
    this.beginAssessment = beginAssessment;
  }

  public boolean timeExpired(){
    boolean timeExpired = false;
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)queue.
                                             get(adata.getAssessmentGradingId());
    if (timedAG != null){ 
      // if server already submit the assessment, this happen if JScript latency is very long
      // and assessment passed the time left + latency buffer
      // in this case, we will display the time expired message.
      if (timedAG.getSubmittedForGrade()){
        timeExpired = true;
        queue.remove(timedAG);
      } 
    }
    else{ 
      // null => not only does the assessment miss the latency buffer, it also missed the
      // transaction buffer
      timeExpired = true;
    }
    return timeExpired;
  }

  private void removeTimedAssessmentFromQueue(){
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)queue.
                                             get(adata.getAssessmentGradingId());
    if (timedAG != null)
      queue.remove(timedAG);
  }


}
