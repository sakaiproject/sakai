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

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.assessment.ui.servlet.evaluation.ExportReportServlet;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

/* For evaluation: Histogram Scores backing bean. */
@Slf4j
@ManagedBean(name="histogramScores")
@SessionScoped
public class HistogramScoresBean implements Serializable {

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 3465442692662950563L;

    @Setter
    @Getter
    private String assessmentName;

    @Setter
    @Getter
    private String assessmentId;

    @Setter
    @Getter
    private String publishedId;

    @Setter
    @Getter
    private String itemId;

    @Getter
    @Setter
    private String agent;

    @Setter
    @Getter
    private String hasNav;

    @Setter
    @Getter
    private String groupName;

    @Setter
    @Getter
    private String maxScore; // heighest score

    @Setter
    @Getter
    private String totalScore;

    @Setter
    @Getter
    private String totalPossibleScore; //total possible score

    @Setter
    @Getter
    private String adjustedScore;

    @Setter
    @Getter
    private String questionNumber;

    @Setter
    @Getter
    private String allSubmissions;

    @Getter
    private String partNumber;//Note: this is sequence number

    @Setter
    @Getter
    private String mean;

    @Setter
    @Getter
    private String median;

    @Setter
    @Getter
    private String mode;

    @Setter
    @Getter
    private String standDev;

    @Getter
    @Setter
    private String skewnessCoefficient;

    @Setter
    @Getter
    private String lowerQuartile; //medidan of lowest-median

    @Setter
    @Getter
    private String upperQuartile; //median of median-highest

    @Setter
    @Getter
    private int interval; // number interval breaks down

    @Getter
    private Collection<HistogramQuestionScoresBean> info; //HistogramQuestionScoresBean

    @Getter
    private Collection<HistogramQuestionScoresBean> partInfo;

    @Setter
    @Getter
    private int[] numStudentCollection = {  };

    @Setter
    @Getter
    private String[] rangeCollection = {  };

    @Setter
    @Getter
    private int[] columnHeight = {  };

    @Setter
    @Getter
    private int arrayLength; //length of array

    @Getter
    @Setter
    private String range; // range of student Score lowest-highest

    @Setter
    @Getter
    private int numResponses;

    @Setter
    @Getter
    private String q1;

    @Setter
    @Getter
    private String q2;

    @Setter
    @Getter
    private String q3;

    @Setter
    @Getter
    private String q4;

    @Setter
    private HistogramBarBean[] histogramBars;

    @Setter
    @Getter
    private HistogramQuestionScoresBean[] histogramQuestions;

    @Setter
    @Getter
    private boolean randomType;   // true = has at least one random draw part

    @Getter
    private List<PublishedSectionData> assesmentParts = new ArrayList<PublishedSectionData>();
    @Getter
    private List<SelectItem> selectItemParts = new ArrayList<SelectItem>();
    @Setter
    private boolean showObjectivesColumn;
    @Setter
    @Getter
    private List<Entry<String, Double>> objectives;
    @Setter
    @Getter
    private List<Entry<String, Double>> keywords;

  private static final ResourceLoader evaluationMessages = new ResourceLoader(SamigoConstants.EVAL_BUNDLE);
  private final int[] PERCENTS_SCORE_STATISTICS = {50, 70, 90};

  /**
   * Creates a new HistogramScoresBean object.
   */
  public HistogramScoresBean()
  {
  }

    public void setInfo(Collection<HistogramQuestionScoresBean> pinfo) {
        info = pinfo;
        filterInfo();
    }

    public String getRoundedTotalPossibleScore() {
        try {
            String newscore = ContextUtil.getRoundedValue(totalPossibleScore, 2);
            return Validator.check(newscore, "N/A");
        } catch (Exception e) {
            // encountered some weird number format/locale
            return Validator.check(totalPossibleScore, "0");
        }
    }

  public void setPartNumber(String ppartNumber) {
    partNumber = ppartNumber;
    filterInfo();
  }

    public String getHistogramChartOptions() {
    String chartOptions = "[[";
    for (HistogramBarBean hb : getHistogramBars()) {
      chartOptions =
          chartOptions.concat("['" + hb.getRangeInfo() + "'," + hb.getNumStudents() + "],");
    }
    chartOptions = chartOptions.substring(0, chartOptions.lastIndexOf(","));
    chartOptions = chartOptions.concat("]]");
    return chartOptions;
  }

  public String getHistogramChartReader() {
    String reader_at =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_at");
    String reader_through =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_through");
    String reader_after =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_after");
    String reader_lt =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_lt");
    String reader_end =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_end");
    String reader_earned =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_earned");
    String reader_starting =
        ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "reader_starting");
    String chartReader = "";

    for (HistogramBarBean hb : getHistogramBars()) {
      String range = hb.getRangeInfo();
      boolean side = true;
      String firstVal = "";
      String lastVal = "";
      String atOrAfter = "";
      String throughOrLess = "";

      for (int i = 0; i < range.length(); i++) {
        switch (range.charAt(i)) {
          case '[':
            atOrAfter = reader_at;
            break;
          case ']':
            throughOrLess = reader_through;
            break;
          case '(':
            atOrAfter = reader_after;
            break;
          case ')':
            throughOrLess = reader_lt;
            break;
          case ',':
            side = false;
            break;
          default:
            if (side) {
              firstVal = firstVal.concat(Character.toString(range.charAt(i)));
            } else {
              lastVal = lastVal.concat(Character.toString(range.charAt(i)));
            }
	    break;
        }
      }
      String resString = "students";
      String scoreOrScores = "scores";
      int responses = hb.getNumStudents();
      if (responses == 1) {
        resString = "student";
        scoreOrScores = "a score";
      }
      chartReader =
          chartReader.concat(
              "<span>"
                  + responses
                  + " "
                  + resString
                  + " "
                  + reader_earned
                  + " "
                  + scoreOrScores
                  + " "
                  + reader_starting
                  + " "
                  + atOrAfter
                  + " "
                  + firstVal
                  + " "
                  + throughOrLess
                  + " "
                  + lastVal
                  + " "
                  + reader_end
                  + "</span>");
    }
    return chartReader;
  }

  public HistogramBarBean[] getHistogramBars()
  {
    if (histogramBars != null)
    {
      return histogramBars;
    }

    // note we cache this value, calc only once.
    int length = getArrayLength();
    int[] height = getColumnHeight();
    int[] nums = getNumStudentCollection();
    String[] range = getRangeCollection();

    histogramBars = new HistogramBarBean[length];

    for (int i = 0; i < length; i++) {
      histogramBars[i] = new HistogramBarBean();
      histogramBars[i].setColumnHeight(Integer.toString(height[i]));
      histogramBars[i].setNumStudents(nums[i]);
      histogramBars[i].setRangeInfo(range[i]);
    }

    return histogramBars;
  }

    /**
   * Students in the upper 25%
   */
  private Map upperQuartileStudents;
  
  /**
   * Students in the lower 25%
   */
  private Map lowerQuartileStudents;

  /**
   * The maximum number of answers per question
   * for this assessment (for detailed stats layout)
   */
  @Setter
  @Getter
  private int maxNumberOfAnswers = 0;

  
  /**
   * The HistogramQuestionScores for detailed Statistics for 
   */
  @Setter
  @Getter
  private List<HistogramQuestionScoresBean> detailedStatistics;

  public void addToUpperQuartileStudents(String agentId) {
	  if (upperQuartileStudents == null) {
		  upperQuartileStudents = new HashMap();
	  }
	  upperQuartileStudents.put(agentId, agentId);
  }
  
  public boolean isUpperQuartileStudent(String agentId) {
	  if (upperQuartileStudents == null) {
		  return false;
	  }
	  else {
          return upperQuartileStudents.get(agentId) != null;
	  }
  }

  public Map getUpperQuartileStudents(){
      if (upperQuartileStudents == null) {
          upperQuartileStudents = new HashMap();
      }
      return upperQuartileStudents;
  }
  
  public int getNumberOfUpperQuartileStudents() {
	  if (upperQuartileStudents == null) {
		  return 0;
	  }
	  else {
		  return upperQuartileStudents.size();
	  }
  }
  
  public void addToLowerQuartileStudents(String agentId) {
	  if (lowerQuartileStudents == null) {
		  lowerQuartileStudents = new HashMap();
	  }
	  lowerQuartileStudents.put(agentId, agentId);
  }
  
  
  public boolean isLowerQuartileStudent(String agentId) {
	  if (lowerQuartileStudents == null) {
		  return false;
	  }
	  else {
          return lowerQuartileStudents.get(agentId) != null;
	  }
  }

  public Map getLowerQuartileStudents(){
      if (lowerQuartileStudents == null) {
          lowerQuartileStudents = new HashMap();
      }
      return lowerQuartileStudents;
  }

  public int getNumberOfLowerQuartileStudents() {
	  if (lowerQuartileStudents == null) {
		  return 0;
	  }
	  else {
		  return lowerQuartileStudents.size();
	  }
  }
  
  public void clearUpperQuartileStudents() {
	  upperQuartileStudents = null;
  }
  
  public void clearLowerQuartileStudents() {
	  lowerQuartileStudents = null;
  }


    public boolean getShowObjectivesColumn()
  {
    return showObjectivesColumn;
  }

    public boolean getShowDiscriminationColumn() {
	  try {
              if(String.valueOf(EvaluationModelIfc.ALL_SCORE).equals(allSubmissions)){
                  return false;
              }
              return getTotalScore() != null && Double.parseDouble(getTotalScore()) != 0.0d;
	  }
	  catch (NumberFormatException ex) {
		  return false;
	  }
  }
  
  public boolean getShowPartAndTotalScoreSpreadsheetColumns() {
	  try {
		  return getTotalScore() != null && Double.parseDouble(getTotalScore()) != 0.0d;
	  }
	  catch (NumberFormatException ex) {
		  return false;
	  }
  }
  
  
  public String getUndisplayedStudentResponseInItemAnalysisColumnHeader() {
	  
	  if (getMaxNumberOfAnswers()<13) {
		  return "";
	  }
	  else {
		  int first = 65+12;
		  char firstUndisplayed = (char)first;
		  int last = 65+getMaxNumberOfAnswers()-1;
		  char lastUndisplayed = (char)last;
		  if (first==last) {
			  return String.valueOf(firstUndisplayed);
		  }
		  else {
			  return String.valueOf(firstUndisplayed) + " - " + String.valueOf(lastUndisplayed);
		  }
	  }
  }

    public void setAssesmentParts(List<PublishedSectionData> assesmentParts) {
        this.assesmentParts = assesmentParts;
        selectItemParts.clear();
        setPartNumber(assesmentParts.get(0).getSequence().toString());
        /*
         * For parts from pools:
         * Part <part.sequence>: <part.title>, Pool: <part.poolname>
         * UNLESS part.title = "Default" in which case omit it, i.e. use:
         * Part <part.sequence>, Pool: <part.poolname>
         * For parts not from pools
         * Part <part.sequence>: <part.title>
         * UNLESS part.title = "Default" in which case omit it, i.e. use:
         * Part <part.sequence>
         */
        String defaultStr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.CommonMessages","default");
        String partStr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","part") + " ";
        String poolStr = ", " + ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","pool") + ": ";
        for(PublishedSectionData section: assesmentParts){
            StringBuilder text = new StringBuilder();
            text.append(partStr + String.valueOf(section.getSequence()));
            if(!defaultStr.equals(section.getTitle())){
                text.append(": " + section.getTitle());
            }
            if(section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_FIXED_AND_RANDOM_DRAW) != null){
                text.append(poolStr + section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_FIXED_AND_RANDOM_DRAW));
            }
            if(section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW) != null){
                String poolname = section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
                if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.equals(Integer.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE))) && 
                        section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT) != null) {
                    Integer count = Integer.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT));
                    for (int i = 1; i < count; i++) {
                        poolname += SectionDataIfc.SEPARATOR_COMMA + section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW + SectionDataIfc.SEPARATOR_MULTI + i);
                    }
                }
                text.append(poolStr + poolname);
            }
            selectItemParts.add(new SelectItem(String.valueOf(section.getSequence()), text.toString()));
        }
    }

    public int getAssesmentPartCount(){
        return (assesmentParts==null?0:assesmentParts.size());
    }

    /**
     * This method will filter the info (HistogramQuestionScoresBean)
     * to only show the questions for the active part.
     */
    private void filterInfo(){
        if(partInfo == null){
            partInfo = new ArrayList<HistogramQuestionScoresBean>();
        }else{
            partInfo.clear();
        }
        if(info == null){
            return;
        }else if(partNumber == null || partNumber.length() == 0){
            partInfo.addAll(info);
        }else{
            for(HistogramQuestionScoresBean question: info){
                if(partNumber.equals(question.getPartNumber())){
                    partInfo.add(question);
                }
            }
        }
    }

    public String getExportItemAnalysisXlsx() {
      return exportReportUrl(ExportReportServlet.EXPORT_TYPE_ITEM_ANALYSIS, ExportReportServlet.EXPORT_FORMAT_XLSX);
    }

    public String getExportItemAnalysisPdf() {
      return exportReportUrl(ExportReportServlet.EXPORT_TYPE_ITEM_ANALYSIS, ExportReportServlet.EXPORT_FORMAT_PDF);
    }

    public String getExportStatisticsXlsx() {
      return exportReportUrl(ExportReportServlet.EXPORT_TYPE_STATISTICS, ExportReportServlet.EXPORT_FORMAT_XLSX);
    }

    public String getExportStatisticsPdf() {
      return exportReportUrl(ExportReportServlet.EXPORT_TYPE_STATISTICS, ExportReportServlet.EXPORT_FORMAT_PDF);
    }

    private String exportReportUrl(String type, String format) {
      return ExportReportServlet.exportReportUrl(assessmentId, type, format);
    }

    public String[] getTimeStats() {
      boolean firstTimeInside = true;
      int timeElapsedInSeconds = 0;
      int timeMin = 0;
      int timeAvg = 0;
      int timeMax = 0;
      int counter = 0;
      GradingService gradingService = new GradingService();
      if (this.getAllSubmissions().equals("3")) {
        List allSubmissionsList = gradingService.getAllSubmissions(this.getPublishedId());
        for (Object submission : allSubmissionsList) {
          AssessmentGradingData assessmentGradingAux = (AssessmentGradingData) submission;
          try {
            timeElapsedInSeconds = ((int) assessmentGradingAux.getSubmittedDate().getTime()) / 1000 - ((int) assessmentGradingAux.getAttemptDate().getTime()) / 1000;
          } catch (Exception ex) {
            timeElapsedInSeconds = 0;
            log.error("Cannot resolve the submittedDate or the attemptDate, so it's a unanswered question, setting time as 0");
          }
          timeAvg += timeElapsedInSeconds;
          if (timeElapsedInSeconds > 0) {
            if (firstTimeInside || timeElapsedInSeconds < timeMin) {
              timeMin = timeElapsedInSeconds;
              firstTimeInside = false;
            } 
            if (timeElapsedInSeconds > timeMax) {
              timeMax = timeElapsedInSeconds;
            }
            counter++;
          }
        }
      } else {
        TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
        Collection agents = totalBean.getAgents();
        for (Object agent : agents) {
          AgentResults agentResults = (AgentResults) agent;
          try {
            timeElapsedInSeconds = ((int) agentResults.getSubmittedDate().getTime()) / 1000 - ((int) agentResults.getAttemptDate().getTime()) / 1000;
          } catch (Exception ex) {
            timeElapsedInSeconds = 0;
            log.error("Cannot resolve the submittedDate or the attemptDate, so it's a unanswered question, setting time as 0");
          }
          timeAvg += timeElapsedInSeconds;
          if (timeElapsedInSeconds > 0) {
            if (firstTimeInside || timeElapsedInSeconds < timeMin) {
              timeMin = timeElapsedInSeconds;
              firstTimeInside = false;
            } 
            if (timeElapsedInSeconds > timeMax) {
              timeMax = timeElapsedInSeconds;
            }
            counter++;
          }
        }
      }
      if (timeAvg > 0) {
        timeAvg = timeAvg / counter;
      }

      return new String[]{TimeUtil.getFormattedTime(timeMin), TimeUtil.getFormattedTime(timeAvg), TimeUtil.getFormattedTime(timeMax)};
    }

    /**
     * Get the Time stats for the variation as array based on bean.getAllSubmissions() getter:
     *  - 1 for only best submissions (highest puntuation)
     *  - 3 for all submissions type
     * 
     * @return timeStringArray (minimum time, average time and maximum time)
     */
    public ArrayList getTimeStatsVariationArray() {
      ArrayList timeStatsVariationArray = new ArrayList<>();
      Double totalPossibleScoreDouble = 0.0;
      try {
        totalPossibleScoreDouble = Double.parseDouble(totalPossibleScore);
      } catch (NumberFormatException e) {
        totalPossibleScoreDouble = 0.0;
      }
      if (totalPossibleScoreDouble != 0) {

        for (int minimumScore : PERCENTS_SCORE_STATISTICS) {
          int timeElapsedInSeconds = 0;
          int timeAvg = 0;
          int counter = 0;
          GradingService gradingService = new GradingService();
          if (this.getAllSubmissions().equals("3")) {
            TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
            List allSubmissionsList = gradingService.getAllSubmissions(this.getPublishedId());
            for (Object submission : allSubmissionsList) {
              AssessmentGradingData assessmentGradingAux = (AssessmentGradingData) submission;
              if (assessmentGradingAux.getFinalScore() >= totalPossibleScoreDouble * minimumScore / 100){
                try {
                  timeElapsedInSeconds = ((int) assessmentGradingAux.getSubmittedDate().getTime()) / 1000 - ((int) assessmentGradingAux.getAttemptDate().getTime()) / 1000;
                } catch (Exception ex) {
                  timeElapsedInSeconds = 0;
                  log.error("Cannot resolve the submittedDate or the attemptDate, so it's a unanswered question, setting time as 0");
                }
                timeAvg += timeElapsedInSeconds;
                if (timeElapsedInSeconds > 0) {
                  counter++;
                }
              }
              
            }
          } else {
            TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
            Collection agents = totalBean.getAgents();
            for (Object agent : agents) {
              AgentResults agentResults = (AgentResults) agent;
              if (agentResults.getAssessmentGradingId() != -1) {
                AssessmentGradingData assessmentGradingAux = gradingService.load(agentResults.getAssessmentGradingId().toString());
                if (assessmentGradingAux.getFinalScore() >= totalPossibleScoreDouble * minimumScore / 100){
                  try {
                    timeElapsedInSeconds = ((int) agentResults.getSubmittedDate().getTime()) / 1000 - ((int) agentResults.getAttemptDate().getTime()) / 1000;
                  } catch (Exception ex) {
                    timeElapsedInSeconds = 0;
                  }
                  timeAvg += timeElapsedInSeconds;
                  if (timeElapsedInSeconds > 0) {
                    counter++;
                  }
                }
              }
            }
          }
          if (timeAvg > 0) {
            timeAvg = timeAvg / counter;
          }

          ArrayList timeStringArray = new ArrayList<>();
          timeStringArray.add(new String[]{evaluationMessages.getString("time_avg"), TimeUtil.getFormattedTime(timeAvg)});

          ArrayList timeStatsStringArray = new ArrayList<>();
          timeStatsStringArray.add(counter);
          timeStatsStringArray.add(evaluationMessages.getFormattedMessage("questionVariation_title", (Object[]) new String[]{minimumScore + "%", counter + ""}));
          timeStatsStringArray.add(timeStringArray);
          timeStatsVariationArray.add(timeStatsStringArray);
        }
      }
      return timeStatsVariationArray;
    }

    public boolean isTrackingQuestion() {
      PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
      PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(this.getPublishedId());
      return Boolean.parseBoolean(publishedAssessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.TRACK_QUESTIONS));
    }
}
