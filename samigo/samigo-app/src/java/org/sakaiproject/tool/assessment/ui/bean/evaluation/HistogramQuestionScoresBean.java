/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.util.ResourceLoader;

/* For evaluation: Histogram Question Scores backing bean. */
@Slf4j
@ManagedBean(name="histogramquestionscores")
@SessionScoped
public class HistogramQuestionScoresBean implements Serializable {
  private String assessmentName;
  private String title;

  private static final ResourceLoader evaluationMessages = new ResourceLoader(SamigoConstants.EVAL_BUNDLE);

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -584855389374044609L;
  private String assessmentId;
  private String publishedId;
  private Long itemDataId;
  private Collection agents;
  private String agent;
  private String groupName;
  private String maxScore; // heighest score
  private String totalScore; //total possible score
  private String adjustedScore;
  private boolean allSubmissions;
  private String questionLabelFormat;
  private String questionNumber;
  private String questionText;
  private String questionType;
  private String poolName;
  private String percentCorrect;
  private String partNumber;
  private String mean;
  private String median;
  private String mode;
  private String highestRange;
  private String standDev;
  private String lowerQuartile; //medidan of lowest-median
  private String upperQuartile; //median of median-highest
  private int interval; // number interval breaks down
  private Collection info;
  private int[] numStudentCollection =
                                       {};
  private String[] rangeCollection =
                                     {};
  private int[] columnHeight =
                               {};
  private int arrayLength; //length of array
  private String range; // range of student Score lowest-highest
  private int numResponses;
  private String q1;
  private String q2;
  private String q3;
  private String q4;
  String correctAnswer;
  private int totalResponses;
  private HistogramBarBean[] histogramBars;

  private boolean randomType;   // this part is a random draw part

  private Long subQuestionSequence;
  private boolean showIndividualAnswersInDetailedStatistics;
  
  private String objectives;
  private String keywords;
  @Getter @Setter
  private Integer difficulty;
  @Getter @Setter
  private Long numberOfStudentsWithCorrectAnswers;
  @Getter @Setter
  private Long numberOfStudentsWithIncorrectAnswers;

  /**
   * Creates a new HistogramQuestionScoresBean object.
   */
  public HistogramQuestionScoresBean()
  {
  }

  /**
   * set the agent
   *
   * @param pagent the agent
   */
  public void setAgent(String pagent)
  {
    agent = pagent;
  }

  /**
   * get the agent
   *
   * @return the agent
   */
  public String getAgent()
  {
    return Validator.check(agent, "N/A");
  }

  /**
   * get the assessment name
   *
   * @return the name
   */
  public String getAssessmentName()
  {
    return Validator.check(assessmentName, "N/A");
  }

  /**
   * set the assessment name
   *
   * @param passessmentName the name
   */
  public void setAssessmentName(String passessmentName)
  {
    assessmentName = passessmentName;
  }

  /**
   * get the question title
   *
   * @return the title
   */
  public String getTitle()
  {
    return Validator.check(title, "N/A");
  }

  /**
   * set the question title
   *
   * @param ptitle The title.
   */
  public void setTitle(String ptitle)
  {
    title = ptitle;
  }

  /**
   * get the published id
   *
   * @return the id
   */
  public String getPublishedId()
  {
    return Validator.check(publishedId, "0");
  }

  /**
   * set the publishedId id
   *
   * @param publishedId the id
   */
  public void setPublishedId(String publishedId)
  {
    this.publishedId = publishedId;
  }
  
  /**
   * get the agents id
   *
   * @return the id
   */
  public Collection getAgents()
  {
    return agents;
  }

  /**
   * set the agents id
   *
   * @param agents the id
   */
  public void setAgents(Collection agents)
  {
    this.agents = agents;
  }

  public Long getItemDataId() {
	  return itemDataId;
  }

  public void setItemDataId(Long itemDataId) {
	  this.itemDataId = itemDataId;
  }

  /**
   * get the assessment id
   *
   * @return the id
   */
  public String getAssessmentId()
  {
    return Validator.check(assessmentId, "0");
  }

  /**
   * set the assessment id
   *
   * @param passessmentId the id
   */
  public void setAssessmentId(String passessmentId)
  {
    assessmentId = passessmentId;
  }

  /**
   * get the group name
   *
   * @return the name
   */
  public String getGroupName()
  {
    return Validator.check(groupName, "N/A");
  }

  /**
   * all submissions
   *
   * @return all submissions true or false
   */
  public boolean getAllSubmissions()
  {
    return allSubmissions;
  }

  /**
   * all submissions
   *
   * @param pallSubmissions all submissions true or false
   */
  public void setAllSubmissions(boolean pallSubmissions)
  {
    allSubmissions = pallSubmissions;
  }

  /**
   * set the group name
   *
   * @param pgroupName the name
   */
  public void setGroupName(String pgroupName)
  {
    groupName = pgroupName;
  }

  /**
   * get the range collection
   *
   * @return the range collection
   */
  public String[] getRangeCollection()
  {
    if (rangeCollection == null)
      return new String[0];
    return rangeCollection;
  }

  /**
   * set the range collection
   *
   * @param prange the range collection
   */
  public void setRangeCollection(String[] prange)
  {
    rangeCollection = prange;
  }

  /**
   * get the number of students for each bar
   *
   * @return the number array
   */
  public int[] getNumStudentCollection()
  {
    if (numStudentCollection == null)
      return new int[0];
    return numStudentCollection;
  }

  /**
   * set the number of students for each bar
   *
   * @param pnumStudent the nunber array
   */
  public void setNumStudentCollection(int[] pnumStudent)
  {
    numStudentCollection = pnumStudent;
  }

  /**
   * get  column height, or width if horizontal
   *
   * @return the array of height for each bar
   */
  public int[] getColumnHeight()
  {
    if (columnHeight == null)
      return new int[0];
    return columnHeight;
  }

  /**
   * set the column heigh array
   *
   * @param pcolumnHeight the column height array
   */
  public void setColumnHeight(int[] pcolumnHeight)
  {
    columnHeight = pcolumnHeight;
  }

  /**
   * get the number of bars
   *
   * @return the number
   */
  public int getArrayLength()
  {
    return arrayLength;
  }

  /**
   * set the number of bars
   *
   * @param parrayLength number
   */
  public void setArrayLength(int parrayLength)
  {
    arrayLength = parrayLength;
  }

  /**
   * get  the interval
   *
   * @return the interval
   */
  public int getInterval()
  {
    return interval;
  }

  /**
   * set the interval
   *
   * @param pinterval the interval
   */
  public void setInterval(int pinterval)
  {
    interval = pinterval;
  }

  /**
   * get info
   *
   * @return info
   */
  public Collection getInfo()
  {
    if (info == null)
      return new ArrayList();
    return info;
  }

  /**
   * set info
   *
   * @param pinfo info
   */
  public void setInfo(Collection pinfo)
  {
    info = pinfo;
  }

  /**
   * set the maximum score
   *
   * @return score
   */
  public String getMaxScore()
  {
    return Validator.check(maxScore, "N/A");
  }

  /**
   * set the maximum score
   *
   * @param pmaxScore maxscore
   */
  public void setMaxScore(String pmaxScore)
  {
    maxScore = pmaxScore;
  }

  /**
   * get the total score
   *
   * @return the total score
   */
  public String getTotalScore()
  {
    return Validator.check(totalScore, "N/A");
  }

  /**
   * set the total score
   *
   * @param ptotalScore  total score
   */
  public void setTotalScore(String ptotalScore)
  {
    totalScore = ptotalScore;
  }

  /**
   * get the adjusted score
   *
   * @return the adjusted score
   */
  public String getAdjustedScore()
  {
    return Validator.check(adjustedScore, "N/A");
  }

  /**
   * set the adjusted score
   *
   * @param padjustedScoret the adjusted score
   */
  public void setAdjustedScore(String padjustedScore)
  {
    adjustedScore = padjustedScore;
  }

  /**
   * get the question number
   *
   * @return the question number
   */
  public String getQuestionNumber()
  {
    return Validator.check(questionNumber, "N/A");
  }

  /**
   * set the question number
   *
   * @param pquestionNumber the question number
   */
  public void setQuestionNumber(String pquestionNumber)
  {
    questionNumber = pquestionNumber;
  }

  /**
   * get the question text
   *
   * @return the question text
   */
  public String getQuestionText()
  {
    return Validator.check(questionText, "N/A");
  }

  /**
   * set the question text
   *
   * @param pquestionText the question text
   */
  public void setQuestionText(String pquestionText)
  {
    questionText = pquestionText;
  }

  /**
   * get correct answer
   *
   * @return the correct answer
   */
  public String getCorrectAnswer()
  {
    return Validator.check(correctAnswer, "N/A");
  }

  /**
   * set correct answer
   *
   * @param pcorrectAnswer the correct answer
   */
  public void setCorrectAnswer(String pcorrectAnswer)
  {
    correctAnswer = pcorrectAnswer;
  }

  /**
   * get the question type
   *
   * @return the question type
   */
  public String getQuestionType()
  {
    return Validator.check(questionType, "0");
  }

  /**
   * set the the question type
   *
   * @param pquestionType the question type
   */
  public void setQuestionType(String pquestionType)
  {
    questionType = pquestionType;
  }

  /**
   * get the pool name
   *
   * @return the pool name
   */
  public String getPoolName()
  {
    return poolName;
  }

  /**
   * set the the pool name
   *
   * @param qpoolName the pool name
   */
  public void setPoolName(String qpoolName)
  {
    poolName = qpoolName;
  }
  /**
   * get the percent correct
   *
   * @return the percent correct
   */
  public String getPercentCorrect()
  {
    return Validator.check(percentCorrect, "N/A");
  }

  /**
   * set the percent correct
   *
   * @param ppercentCorrect the percent correct
   */
  public void setPercentCorrect(String ppercentCorrect)
  {
    percentCorrect = ppercentCorrect;
  }

  /**
   * get the part number
   *
   * @return the part number
   */
  public String getPartNumber()
  {
    return Validator.check(partNumber, "N/A");
  }

  /**
   * set the part number
   *
   * @param ppartNumber the part number
   */
  public void setPartNumber(String ppartNumber)
  {
    partNumber = ppartNumber;
  }

  /**
   * get mean
   *
   * @return mean
   */
  public String getMean()
  {
    return Validator.check(mean, "N/A");
  }

  /**
   * set mean
   *
   * @param pmean mean
   */
  public void setMean(String pmean)
  {
    mean = pmean;
  }

  /**
   * get median
   *
   * @return median
   */
  public String getMedian()
  {
    return Validator.check(median, "N/A");
  }

  /**
   * set median
   *
   * @param pmedian median
   */
  public void setMedian(String pmedian)
  {
    median = pmedian;
  }

  /**
   * get mode
   *
   * @return mode
   */
  public String getMode()
  {
    return Validator.check(mode, "N/A");
  }

  /**
   * set mode
   *
   * @param pmode mode
   */
  public void setMode(String pmode)
  {
    mode = pmode;
  }

  /**
   * get the standard deviation
   *
   * @return the standard deviation
   */
  public String getStandDev()
  {
    return Validator.check(standDev, "N/A");
  }

  /**
   * set the standard deviation
   *
   * @param pstandDev the standard deviation
   */
  public void setStandDev(String pstandDev)
  {
    standDev = pstandDev;
  }

  /**
   * get the lower quartile
   *
   * @return the lower quartile
   */
  public String getLowerQuartile()
  {
    return Validator.check(lowerQuartile, "N/A");
  }

  /**
   * set the lower quartile
   *
   * @param plowerQuartile the lower quartile
   */
  public void setLowerQuartile(String plowerQuartile)
  {
    lowerQuartile = plowerQuartile;
  }

  /**
   * get the upper quartile
   *
   * @return the upper quartile
   */
  public String getUpperQuartile()
  {
    return Validator.check(upperQuartile, "N/A");
  }

  /**
   * set the upper quartile
   *
   * @param pupperQuartile the upper quartile
   */
  public void setUpperQuartile(String pupperQuartile)
  {
    upperQuartile = pupperQuartile;
  }

  /**
   * get the first quartile
   *
   * @return the first quartile
   */
  public String getQ1()
  {
    return Validator.check(q1, "N/A");
  }

  /**
   * set the first quartile
   *
   * @param pq1 the first quartile
   */
  public void setQ1(String pq1)
  {
    q1 = pq1;
  }

  /**
   * get the second quartile
   *
   * @return the second quartile
   */
  public String getQ2()
  {
    return Validator.check(q2, "N/A");
  }

  /**
   * set the second quartile
   *
   * @param pq2 the second quartile
   */
  public void setQ2(String pq2)
  {
    q2 = pq2;
  }

  /**
   * get the third quartile
   *
   * @return the third quartile
   */
  public String getQ3()
  {
    return Validator.check(q3, "N/A");
  }

  /**
   * set the third quartile
   *
   * @param pq3 the third quartile
   */
  public void setQ3(String pq3)
  {
    q3 = pq3;
  }

  /**
   * get the fourth quartile
   *
   * @return the fourth quartile
   */
  public String getQ4()
  {
    return Validator.check(q4, "N/A");
  }

  /**
   * set the fourth quartile
   *
   * @param pq4 the fourth quartile
   */
  public void setQ4(String pq4)
  {
    q4 = pq4;
  }

  /**
   * get range
   *
   * @return range
   */
  public String getRange()
  {
    return Validator.check(range, "N/A");
  }

  /**
   * set range
   *
   * @param prange range
   */
  public void setRange(String prange)
  {
    range = prange;
  }

  /**
   * get the number of responses
   *
   * @return the number of responses
   */
  public int getNumResponses()
  {
    return numResponses;
  }

  /**
   * set the number of responses
   *
   * @param pnumResponses the number of responses
   */
  public void setNumResponses(int pnumResponses)
  {
    numResponses = pnumResponses;
  }

  /**
   * get the total number of responses
   *
   * @return the total number of responses
   */
  public int getTotalResponses()
  {
    return totalResponses;
  }

  /**
   * set the total number of responses
   *
   * @param ptotalResponses the total number of responses
   */
  public void setTotalResponses(int ptotalResponses)
  {
    totalResponses = ptotalResponses;
  }

  /**
   * HistogramBar arrray
   *
   * @return HistogramBar arrray
   */
  public HistogramBarBean[] getHistogramBars()
  {
    if (histogramBars != null)
    {
      return histogramBars;
    }

    // note we cache this value, calc only once.
    int length = getColumnHeight().length;
    int[] height = getColumnHeight();
    int[] nums = getNumStudentCollection();
    String[] range = getRangeCollection();

    histogramBars = new HistogramBarBean[length];

    for (int i = 0; i < length; i++)
    {
      histogramBars[i] = new HistogramBarBean();
      histogramBars[i].setColumnHeight(Integer.toString(height[i]));
      histogramBars[i].setNumStudents(nums[i]);
      histogramBars[i].setRangeInfo(range[i]);
    }

    return histogramBars;
  }

  /**
   * HistogramBar array
   *
   * @param bars HistogramBar arrray
   */
  public void setHistogramBars(HistogramBarBean[] bars)
  {
    histogramBars = bars;
  }

  /**
   * Do we show detailed statistics for this question
   * @return boolean true if we do
   */
  public boolean getDetailedStats()
  {
    if ("True False".equals(questionType) ||
      "Multiple Choice".equals(questionType) ||
      "Multiple Correct Answer".equals(questionType) ||
      "Multiple Correct Single Selection".equals(questionType) 
      )
    {
      return false; //shouldn't happen
    }
    else
    {
      return true;
    }
  }

  public void setDetailedStats(boolean ignored)
  {
  }


  public boolean getRandomType()
  {
    return randomType;
  }

  public void setRandomType(boolean param)
  {
    randomType= param;
  }

  /**
   * 
   * The set of students with all answers correct for this question
   */
  private Set studentsWithAllCorrect;

  
  /**
   * @param studentsWithAllCorrect
   */
  public void setStudentsWithAllCorrect(Set studentsWithAllCorrect) {
	this.studentsWithAllCorrect = studentsWithAllCorrect;
  }



/**
   * 
   * The set of students who responded to this question
   */
  private Set studentsResponded;
  
  
  /**
   * @param studentsResponded
   */
  public void setStudentsResponded(Set studentsResponded) {
	this.studentsResponded = studentsResponded;
  }



/**
   * 
   * Percentage of students in the overall upper 25%
   * who got this question right
   */
  private String percentCorrectFromUpperQuartileStudents;
  
  /**
   * 
   * Percentage of students in the overall lower 25%
   * who got this question right
   */
  private String percentCorrectFromLowerQuartileStudents;

  /**
   * 
   * Discrimination value of this question
   */
  private String discrimination;
  
  /**
   * 
   * Count of selected answer frequencies 
   * plus numberOfStudentsWithZeroAnswers
   */
  private String n;
  
  /**
   * 
   * Number of students who selected no answer 
   */
  private int numberOfStudentsWithZeroAnswers = 0;
  
  /**
   * 
   * The published item (question) id
   */
  private Long itemId;

  
  /**
   * 
   * The published item (question) id
   */
  private int numberOfParts;
  
  
  public String getQuestionLabel() {
      if(questionLabelFormat == null){
        String label = "Q" + questionNumber;
        if(randomType && poolName != null){
            label = label + "-Pool:" + poolName;
        }
	  if (getNumberOfParts() > 1) {
		  return "P" + partNumber + "-" + label;
	  }
	  else {
		  return label;
	  }
      }else{
          return MessageFormat.format(questionLabelFormat, questionNumber, partNumber, poolName);
      }
  }

  public void setQuestionLabelFormat(String questionLabelFormat){
      this.questionLabelFormat = questionLabelFormat;
  }
  
  public void addStudentWithAllCorrect(String agentId) {
	  if (studentsWithAllCorrect == null) {
		  studentsWithAllCorrect = new TreeSet();
	  }
	  studentsWithAllCorrect.add(agentId);
  }
  
  public void addStudentResponded(String agentId) {
	  if (studentsResponded == null) {
		  studentsResponded = new TreeSet();
	  }
	  studentsResponded.add(agentId);
  }
  
  public void clearStudentsWithAllCorrect() {
	  studentsWithAllCorrect = null;
  }
  
  public void clearStudentsResponded() {
	  studentsResponded = null;
  }

  
  public Set getStudentsWithAllCorrect() {
	return studentsWithAllCorrect;
  }
	
  public Set getStudentsResponded() {
	return studentsResponded;
  }


  public String getDiscrimination() {
	  return discrimination;
  }

  public void setDiscrimination(String discrimination) {
	  this.discrimination = discrimination;
  }

  public String getPercentCorrectFromUpperQuartileStudents() {
	  return percentCorrectFromUpperQuartileStudents;
  }

  public void setPercentCorrectFromUpperQuartileStudents(
		  String percentCorrectFromUpperQuartileStudents) {
	  this.percentCorrectFromUpperQuartileStudents = percentCorrectFromUpperQuartileStudents;
  }

  public String getPercentCorrectFromLowerQuartileStudents() {
	  return percentCorrectFromLowerQuartileStudents;
  }

  public void setPercentCorrectFromLowerQuartileStudents(
		  String percentCorrectFromLowerQuartileStudents) {
	  this.percentCorrectFromLowerQuartileStudents = percentCorrectFromLowerQuartileStudents;
  }

  public String getN() {
	/*
		if (histogramBars == null) return "0";
		int numberOfStudents = 0;
		for (int i=0; i<histogramBars.length; i++) {
			numberOfStudents += histogramBars[i].getNumStudents();
		}
		int n = numberOfStudents + getNumberOfStudentsWithZeroAnswers();
	*/
	/*  
		int n = getNumResponses() + getNumberOfStudentsWithZeroAnswers();
		return "" + n;
	*/
	  return n;
  }
  
  public void setN(String n) {
	  this.n = n;
  }

  public int getNumberOfStudentsWithZeroAnswers() {
	  return numberOfStudentsWithZeroAnswers;
  }

  public void setNumberOfStudentsWithZeroAnswers(
		  int numberOfStudentsWithZeroAnswers) {
	  this.numberOfStudentsWithZeroAnswers = numberOfStudentsWithZeroAnswers;
  }

  public Long getItemId() {
	  return itemId;
  }

  public void setItemId(Long itemId) {
	  this.itemId = itemId;
  }

  public int getNumberOfParts() {
	  return numberOfParts;
  }

  public void setNumberOfParts(int numberOfParts) {
	  this.numberOfParts = numberOfParts;
  }
  
  public boolean getShowPercentageCorrectAndDiscriminationFigures() {
	  return !getQuestionType().equals("3");
  }

  private boolean isQuestionType(Long typeId) {
	  return typeId != null && typeId.toString().equals(getQuestionType());
  }

  public boolean getDisplaysAnswerStatsWithCorrectnessColumn() {
	  return isQuestionType(TypeIfc.MULTIPLE_CHOICE)
			  || isQuestionType(TypeIfc.MULTIPLE_CORRECT)
			  || isQuestionType(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)
			  || isQuestionType(TypeIfc.TRUE_FALSE)
			  || isQuestionType(TypeIfc.CALCULATED_QUESTION);
  }

  public boolean getDisplaysFillInBlankStats() {
	  return isQuestionType(TypeIfc.FILL_IN_BLANK);
  }

  public boolean getDisplaysAnswerStatsWithoutCorrectnessColumn() {
	  return isQuestionType(TypeIfc.MATCHING)
			  || isQuestionType(TypeIfc.FILL_IN_NUMERIC)
			  || isQuestionType(TypeIfc.EXTENDED_MATCHING_ITEMS)
			  || isQuestionType(TypeIfc.IMAGEMAP_QUESTION);
  }

  public boolean getDisplaysScoreStats() {
	  return StatisticsService.supportsScoreStatistics(getQuestionType());
  }

  public boolean getDisplaysMultipleChoiceSurveyStats() {
	  return isQuestionType(TypeIfc.MULTIPLE_CHOICE_SURVEY);
  }

  public boolean getDisplaysMatrixSurveyStats() {
	  return isQuestionType(TypeIfc.MATRIX_CHOICES_SURVEY);
  }

  public boolean getDisplaysSurveySummary() {
	  return StatisticsService.isSurveyQuestionType(getQuestionType());
  }

  public boolean getDisplaysAnswerStatsSummary() {
	  return StatisticsService.supportsAnswerStatistics(getQuestionType())
			  && !StatisticsService.isSurveyQuestionType(getQuestionType());
  }

  public int getSumOfStudentResponsesInUndisplayedItemAnalysisColumns() {
	  if (histogramBars==null || histogramBars.length<13) {
		  return 0;
	  }
	  else {
		  int sum = 0;
		  for (int i=12; i<histogramBars.length; i++) {
			  sum += histogramBars[i].getNumStudents();
		  }
		  return sum;
	  }
  }
  
  public String getStudentResponsesInUndisplayedItemAnalysisColumns() {
	  if (histogramBars==null || histogramBars.length<13) {
		  return "";
	  }
	  else {
		  String sep = " | ";
		  StringBuilder responsesbuf = new StringBuilder(sep);
		  
		  for (int i=12; i<histogramBars.length; i++) {
			  if (histogramBars[i].getIsCorrect()) {
				  responsesbuf.append("(" + histogramBars[i].getNumStudents() + ")" + sep);
			  }
			  else {
				  responsesbuf.append(histogramBars[i].getNumStudents() + sep);
			  }
		  }
		  String responses = responsesbuf.toString();
		  return responses;
	  }
  }

  public Long getSubQuestionSequence() {
	return subQuestionSequence;
  }

  public void setSubQuestionSequence(Long subQuestionSequence) {
	this.subQuestionSequence = subQuestionSequence;
  }
  
  public boolean getShowIndividualAnswersInDetailedStatistics() {
	return showIndividualAnswersInDetailedStatistics;
  }

  public void setShowIndividualAnswersInDetailedStatistics(
		boolean showIndividualAnswersInDetailedStatistics) {
	this.showIndividualAnswersInDetailedStatistics = showIndividualAnswersInDetailedStatistics;
  }
  
  public String getObjectives() {
      return objectives;
  }
   
  public void setObjectives(String objectives) {
      this.objectives = objectives;
  }
    
  public String getKeywords() {
      return keywords;
  }
 
  public void setKeywords(String keywords) {
      this.keywords = keywords;
  }

  /**
   * Get the Time stats as array based on bean.getAllSubmissions() getter:
   *  - 1 for only best submissions (highest puntuation)
   *  - 3 for all submissions type
   * 
   * @return timeStringArray (minimum time, average time and maximum time)
   */
  public ArrayList getTimeStatsArray() {
    GradingService gradingService = new GradingService();
    HistogramScoresBean bean = (HistogramScoresBean) ContextUtil.lookupBean("histogramScores");
    ArrayList<Integer> timeStatsArray = new ArrayList<>(Collections.nCopies(4, 0));

    if (bean.getAllSubmissions().equals("3")) {
      List allSubmissionsList = gradingService.getAllSubmissions(this.getPublishedId());
      for (Object submission : allSubmissionsList) {
        AssessmentGradingData assessmentGradingAux = (AssessmentGradingData) submission;
        timeStatsArray = this.getTimeStatsInSeconds(assessmentGradingAux.getItemGradingSet().toArray(new ItemGradingData[assessmentGradingAux.getItemGradingSet().size()]), timeStatsArray);
      }
    } else {
      for (Object object : this.getAgents()) {
        AgentResults agentResults = (AgentResults) object;
        if (agentResults.getAssessmentGradingId() != -1) {
          AssessmentGradingData assessmentGradingAux = gradingService.load(agentResults.getAssessmentGradingId().toString());
          timeStatsArray = this.getTimeStatsInSeconds(assessmentGradingAux.getItemGradingSet().toArray(new ItemGradingData[assessmentGradingAux.getItemGradingSet().size()]), timeStatsArray);
        }
      }
    }
    if (timeStatsArray.get(0) > 0) {
      timeStatsArray.set(0, timeStatsArray.get(0) / timeStatsArray.get(3));
    }

    ArrayList timeStringArray = new ArrayList<>();
    timeStringArray.add(new String[]{evaluationMessages.getString("time_min") + ":", TimeUtil.getFormattedTime(timeStatsArray.get(1))});
    timeStringArray.add(new String[]{evaluationMessages.getString("time_avg") + ":", TimeUtil.getFormattedTime(timeStatsArray.get(0))});
    timeStringArray.add(new String[]{evaluationMessages.getString("time_max") + ":", TimeUtil.getFormattedTime(timeStatsArray.get(2))});
    return timeStringArray;
  }

  /**
   * Return the time Stats In Seconds:
   *  - 0: for the average time (or the sum of all times in seconds)
   *  - 1: for the minimum time
   *  - 2: for the maximum time
   *  - 3: for the counter (to get the average time)
   * 
   * @param itemGradingDataArray - ItemGradingData[]
   * @param timeStatsArray - ArrayList<Integer>
   * @return statsArray - ArrayList<Integer>
   */
  public ArrayList getTimeStatsInSeconds(ItemGradingData[] itemGradingDataArray, ArrayList<Integer> timeStatsArray) {
    ArrayList<Integer> statsArray = timeStatsArray;
    boolean firstTimeInside = timeStatsArray.get(0) == 0;

    for (ItemGradingData itemGrading : itemGradingDataArray) {
      if (Long.compare(this.getItemDataId(), itemGrading.getPublishedItemId()) == 0) {
        int timeElapsedInSeconds;
        try {
          timeElapsedInSeconds = ((int) itemGrading.getSubmittedDate().getTime()) / 1000 - ((int) itemGrading.getAttemptDate().getTime()) / 1000;
        } catch (Exception ex) {
          log.error("Cannot resolve the submittedDate or the attemptDate, so it's a unanswered question, setting time as 0");
          timeElapsedInSeconds = 0;
        }
        statsArray.set(0, statsArray.get(0) + timeElapsedInSeconds);
        if (timeElapsedInSeconds > 0) {
          if (firstTimeInside || timeElapsedInSeconds < statsArray.get(1)) {
            statsArray.set(1, timeElapsedInSeconds);
            firstTimeInside = false;
          } 
          if (timeElapsedInSeconds > statsArray.get(2)) {
            statsArray.set(2, timeElapsedInSeconds);
          }
          statsArray.set(3, statsArray.get(3) + 1);
        }
      }
    }
    return statsArray;
  }

}
