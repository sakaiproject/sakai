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

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;

/**
 *
 * @author casong
 * @author esmiley@stanford.edu added agentState
 * $Id$
 *
 * Used to be org.navigoproject.ui.web.asi.delivery.XmlDeliveryForm.java
 */
@Slf4j
public class DeliveryBeanie
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 3740101653033385370L;

  private String assessmentId;
  private String assessmentTitle;
  private String timeElapse;
  private String feedback;
  private boolean assessmentUpdatedNeedResubmit;
  private boolean assessmentUpdated;
  private String statistics;
  private boolean submitted;
  private String grade;
  private java.util.Date submissionDate;
  private long subTime;
  private long raw;
  private String rawScore;
  private java.util.Date feedbackDate;
  private String feedbackDelivery;
  private String feedbackComponentOption;
  private String showScore;
  private String submissionHours; 
  private String submissionMinutes; 
  private java.util.Date dueDate;
  private boolean pastDue;
  private boolean timeRunning;
  private int timeLimit_hour;
  private int timeLimit_minute;
  private String finalScore;
  private boolean isAssessmentBeanie=false;

  // display * and notes for multiple submissions 
  private boolean multipleSubmissions;
  private String scoringOption;
   
  private boolean hasRandomDrawPart;
  private boolean isAssessmentRetractForEdit;
  private boolean hasAssessmentBeenModified;
  
  //Allow students to view all submissions of the same assessment
  private Long assessmentGradingId;
  private boolean isRecordedAssessment;
  
  /**
   * Creates a new DeliveryBean object.
   */
  public DeliveryBeanie()
  {
  }

  public String getAssessmentId()
  {
    return assessmentId;
  }

  public void setAssessmentId(String assessmentId)
  {
    this.assessmentId = assessmentId;
  }

  public String getAssessmentTitle()
  {
    return assessmentTitle;
  }

  public void setAssessmentTitle(String assessmentTitle)
  {
    this.assessmentTitle = assessmentTitle;
  }

  public String getTimeElapse()
  {
    return timeElapse;
  }

  public void setTimeElapse(String timeElapse)
  {
    this.timeElapse = timeElapse;
  }

  public String getFeedback()
  {
    return feedback;
  }

  public void setFeedback(String feedback)
  {
    this.feedback = feedback;
  }

  public boolean getAssessmentUpdatedNeedResubmit()
  {
    return assessmentUpdatedNeedResubmit;
  }

  public void setAssessmentUpdatedNeedResubmit(boolean assessmentUpdatedNeedResubmit)
  {
    this.assessmentUpdatedNeedResubmit = assessmentUpdatedNeedResubmit;
  }
  
  public boolean getAssessmentUpdated()
  {
    return assessmentUpdated;
  }

  public void setAssessmentUpdated(boolean assessmentUpdated)
  {
    this.assessmentUpdated = assessmentUpdated;
  }
  
  public String getStatistics()
  {
    return statistics;
  }

  public void setStatistics(String statistics)
  {
    this.statistics = statistics;
  }

  public long getRaw()
  {
    return raw;
  }

  public void setRaw(long raw)
  {
    this.raw = raw;
  }

  public String getRawScore()
  {
    return rawScore;
  }

  public void setRawScore(String rawScore)
  {
    this.rawScore = rawScore;
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
    return submissionHours;
  }

  public void setSubmissionHours(String newHours)
  {
    submissionHours = newHours;
  }

  public String getSubmissionMinutes()
  {
    return submissionMinutes;
  }

  public void setSubmissionMinutes(String newMinutes)
  {
    submissionMinutes = newMinutes;
  }

  public java.util.Date getFeedbackDate()
  {
    return feedbackDate;
  }

  public String getFeedbackDateString()
  {
    String dateString = "";
    if (feedbackDate== null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getIsoDateWithLocalTime(feedbackDate);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
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

  public String getFeedbackComponentOption()
  {
    return feedbackComponentOption;
  }

  public void setFeedbackComponentOption(String feedbackComponentOption)
  {
    this.feedbackComponentOption = feedbackComponentOption;
  }

  public String getShowScore()
  {
    return showScore;
  }

  public void setShowScore(String showScore)
  {
    this.showScore = showScore;
  }

  public boolean getSubmitted()
  {
    return submitted;
  }

  public void setSubmitted(boolean submitted)
  {
    this.submitted = submitted;
  }

  public boolean isSubmitted()
  {
    return submitted;
  }

  public java.util.Date getDueDate()
  {
    return dueDate;
  }

  public String getDueDateString()
  {
    String dateString = "";
    if (dueDate == null) {
      return dateString;
    }

    try {
    	TimeUtil tu = new TimeUtil();
    	return tu.getIsoDateWithLocalTime(dueDate);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }

  public void setDueDate(java.util.Date dueDate)
  {
    this.dueDate = dueDate;
  }

  public boolean getPastDue()
  {
    return pastDue;
  }

  public void setPastDue(boolean pastDue)
  {
    this.pastDue = pastDue;
  }

  public boolean isTimeRunning()
  {
    return timeRunning;
  }

  public boolean getTimeRunning()
  {
    return timeRunning;
  }

  public void setTimeRunning(boolean timeRunning)
  {
    this.timeRunning = timeRunning;
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

  public String getRoundedRawScore() {
   try {
      String newscore=rawScore;
      return Validator.check(newscore, "N/A");
    }
    catch (Exception e) {
      // encountered some weird number format/locale
      return Validator.check(rawScore, "0");
    }

  }
  
  public String getRoundedRawScoreToDisplay() {
   try {
      String newscore= ContextUtil.getRoundedValue(rawScore, 2);	      
      return Validator.check(newscore, "N/A");
   }
   catch (Exception e) {
     // encountered some weird number format/locale
     return Validator.check(rawScore, "0");
   }
  }

  public String getSubmissionDateString()
  {
    String dateString = "";
    if (submissionDate== null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getIsoDateWithLocalTime(submissionDate);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }

  /**
	 * @return Returns the multipleSubmissions.
	 */
	public boolean isMultipleSubmissions() {
		return multipleSubmissions;
	}

	/**
	 * @param multipleSubmissions
	 *            The multipleSubmissions to set.
	 */
	public void setMultipleSubmissions(boolean multipleSubmissions) {
		this.multipleSubmissions = multipleSubmissions;
	}

	/**
	 * @return Returns the scoringOption.
	 */
	public String getScoringOption() {
		return scoringOption;
	}

	/**
	 * @param scoringOption
	 *            The scoringOption to set.
	 */
	public void setScoringOption(String scoringOption) {
		this.scoringOption = scoringOption;
	}

	public boolean getHasRandomDrawPart() {
		return this.hasRandomDrawPart;
	}

	public void setHasRandomDrawPart(boolean param) {
		this.hasRandomDrawPart = param;
	}
	
	public boolean getIsAssessmentRetractForEdit() {
		return this.isAssessmentRetractForEdit;
	}

	public void setIsAssessmentRetractForEdit(boolean isAssessmentRetractForEdit) {
		this.isAssessmentRetractForEdit = isAssessmentRetractForEdit;
	}
	
	public boolean getHasAssessmentBeenModified() {
		return this.hasAssessmentBeenModified;
	}

	public void setHasAssessmentBeenModified(boolean hasAssessmentBeenModified) {
		this.hasAssessmentBeenModified = hasAssessmentBeenModified;
	}

	/**
	 * @return the assessmentGradingId
	 */
	public Long getAssessmentGradingId() {
		return assessmentGradingId;
	}

	/**
	 * @param assessmentGradingId the assessmentGradingId to set
	 */
	public void setAssessmentGradingId(Long assessmentGradingId) {
		this.assessmentGradingId = assessmentGradingId;
	}

	/**
	 * @return the recordedAssessment
	 */
	public boolean getIsRecordedAssessment() {
		return isRecordedAssessment;
	}

	/**
	 * @param recordedAssessment the recordedAssessment to set
	 */
	public void setIsRecordedAssessment(boolean isRecordedAssessment) {
		this.isRecordedAssessment = isRecordedAssessment;
	}
	
	public String getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(String finalScore) {
		this.finalScore = finalScore;
	}
	public boolean getIsAssessmentBeanie() {
		return isAssessmentBeanie;
	}

	public void setIsAssessmentBeanie(boolean isAssessmentBeanie) {
		this.isAssessmentBeanie = isAssessmentBeanie;
	}
}
