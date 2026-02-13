/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.Instant;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexFormat;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.util.Precision;
import org.mariuszgromada.math.mxparser.parsertokens.ParserSymbol;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.grading.SectionGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.tool.assessment.util.SamigoExpressionError;
import org.sakaiproject.tool.assessment.util.SamigoExpressionParser;
import org.sakaiproject.tool.assessment.util.comparator.ImageMapGradingItemComparator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The GradingService calls the back end to get/store grading information. 
 * It also calculates scores for autograded types.
 */
@Slf4j
public class GradingService
{
	/**
	 *	Key for a complext numeric answer e.g. 9+9i 
	 */
  public static final String ANSWER_TYPE_COMPLEX = "COMPLEX";

  /**
   * key for a real number representation e.g 1 or 10E5 
   */
  public static final String ANSWER_TYPE_REAL = "REAL";

  // CALCULATED_QUESTION
  public static final String OPEN_BRACKET = "\\{";
  public static final String CLOSE_BRACKET = "\\}";
  public static final String AT = "@";
  public static final String OPEN_PARENTHESIS = "(";
  public static final String CLOSE_PARENTHESIS = ")";
  public static final String CALCULATION_OPEN = "[["; // not regex safe
  public static final String CALCULATION_CLOSE = "]]"; // not regex safe
  public static final char CALCULATION_AUX_OPEN = '└';
  public static final char CALCULATION_AUX_CLOSE = '┐';
  public static final String FORMAT_MASK = "0E0";
  public static final String GLOBAL_VAR_FORMAT_SUFFIX = "|0,0";
  public static final BigDecimal DEFAULT_MAX_THRESHOLD = BigDecimal.valueOf(1.0e+11);
  public static final BigDecimal DEFAULT_MIN_THRESHOLD = BigDecimal.valueOf(0.0001);
  /**
   * regular expression for matching the contents of a variable or formula name 
   * in Calculated Questions
   * NOTE: Old regex: ([\\w\\s\\.\\-\\^\\$\\!\\&\\@\\?\\*\\%\\(\\)\\+=#`~&:;|,/<>\\[\\]\\\\\\'\"]+?)
   * was way too complicated.
   */
  public static final String CALCQ_VAR_FORM_NAME = "[a-zA-ZÀ-ÿ\\u00f1\\u00d1][^\\{\\}]*?"; // non-greedy (must start wtih alpha)
  public static final String CALCQ_VAR_FORM_NAME_EXPRESSION = "("+CALCQ_VAR_FORM_NAME+")";
  public static final String CALCQ_VAR_FORM_NAME_EXPRESSION_FORMATTED = OPEN_BRACKET + CALCQ_VAR_FORM_NAME_EXPRESSION + CLOSE_BRACKET;
  public static final String CALCQ_SOLUTION_EXPRESSION = OPEN_BRACKET + OPEN_BRACKET + "|" + CLOSE_BRACKET + CLOSE_BRACKET;

  // variable match - (?<!\{)\{([^\{\}]+?)\}(?!\}) - means any sequence inside braces without a braces before or after
  public static final Pattern CALCQ_ANSWER_PATTERN = Pattern.compile("(?<!\\{)" + CALCQ_VAR_FORM_NAME_EXPRESSION_FORMATTED + "(?!\\})", Pattern.UNICODE_CHARACTER_CLASS);
  public static final Pattern CALCQ_FORMULA_PATTERN = Pattern.compile(OPEN_BRACKET + CALCQ_VAR_FORM_NAME_EXPRESSION_FORMATTED + CLOSE_BRACKET, Pattern.UNICODE_CHARACTER_CLASS);
  public static final Pattern CALCQ_GLOBAL_VARIABLE_PATTERN = Pattern.compile(AT + CALCQ_VAR_FORM_NAME_EXPRESSION + AT, Pattern.UNICODE_CHARACTER_CLASS);
  public static final Pattern CALCQ_FORMULA_SPLIT_PATTERN = Pattern.compile("(" + OPEN_BRACKET + OPEN_BRACKET + CALCQ_VAR_FORM_NAME + CLOSE_BRACKET + CLOSE_BRACKET + ")");
  public static final Pattern CALCQ_CALCULATION_PATTERN = Pattern.compile("\\" + CALCULATION_AUX_OPEN + "([^\\" + CALCULATION_AUX_OPEN + "\\" + CALCULATION_AUX_CLOSE + "]+)\\" + CALCULATION_AUX_CLOSE);
  // SAK-39922 - Support (or at least watch for support) for binary/unary calculated question (-1--1)
  public static final Pattern CALCQ_ANSWER_AVOID_DOUBLE_MINUS = Pattern.compile("--");
  public static final Pattern CALCQ_ANSWER_AVOID_PLUS_MINUS = Pattern.compile("\\+-");
  // SAK-40942 - Error in calculated questions: the decimal representation .n or n. (where n is a number) does not work
  public static final Pattern CALCQ_FORMULA_ALLOW_POINT_NUMBER = Pattern.compile("([^\\d]|^)([\\.])([\\d])");
  public static final Pattern CALCQ_FORMULA_ALLOW_NUMBER_POINT = Pattern.compile("([\\d])([\\.])([^\\d]|$)");
  public static final Pattern CALCQ_DOUBLE_OPEN_BRACKET = Pattern.compile(OPEN_BRACKET + OPEN_BRACKET);
  public static final Pattern CALCQ_DOUBLE_CLOSE_BRACKET = Pattern.compile(CLOSE_BRACKET + CLOSE_BRACKET);
  
  private static final int WRONG_IMAGE_MAP_ANSWER_NON_PARCIAL = -123456789;
  
  private static final String NBSP = "&#160;";

  @Getter @Setter
  private List<String> texts;
  @Getter @Setter
  private HashMap<Integer, String> answersMap = new HashMap<Integer, String>();
  @Getter @Setter
  private LinkedHashMap<String, String> answersMapValues = new LinkedHashMap<String, String>();
  @Getter @Setter
  private LinkedHashMap<String, String> globalanswersMapValues = new LinkedHashMap<String, String>();
  @Getter @Setter
  private LinkedHashMap<String, String> mainvariablesWithValues = new LinkedHashMap<String, String>();
  private static final int MAX_ERROR_TRIES = 100;
	  
  /**
   * Get all scores for a published assessment from the back end.
   */
  public List<AssessmentGradingData> getTotalScores(String publishedId, String which)
  {
    return getTotalScores(publishedId, which, true);
  }
  
  public List<AssessmentGradingData> getTotalScores(String publishedId, String which, boolean getSubmittedOnly)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().getTotalScores(NumberUtils.createLong(publishedId), which, getSubmittedOnly);
    } catch (Exception e) {
      log.warn("Could not retrieve total scores for published assessment {}", publishedId, e);
    }
    return Collections.emptyList();
  }
  
 /**
  * Get all submissions for a published assessment from the back end.
  */
  public List<AssessmentGradingData> getAllSubmissions(String publishedId)
  {
    List<AssessmentGradingData> results = null;
    try {
      results = PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllSubmissions(publishedId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }
  
  public List getAllAssessmentGradingData(Long publishedId)
  {
    List results = null;
    try {
      results = PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllAssessmentGradingData(publishedId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }

  public List getHighestAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestAssessmentGradingList(publishedId));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }
  
  public List getHighestSubmittedOrGradedAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestSubmittedOrGradedAssessmentGradingList(publishedId));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }

  public List getLastAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastAssessmentGradingList(publishedId));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }

  public List getLastSubmittedAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results = 
    	  PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastSubmittedAssessmentGradingList(publishedId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }
  
  public List getLastSubmittedOrGradedAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results = 
    	  PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastSubmittedOrGradedAssessmentGradingList(publishedId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }
  
  public void saveTotalScores(List gdataList, PublishedAssessmentIfc pub)
  {
    try {
     AssessmentGradingData gdata;
      if (gdataList.size()>0)
        gdata = (AssessmentGradingData) gdataList.get(0);
      else return;

      Integer scoringType = getScoringType(pub);
      List oldList = getAssessmentGradingsByScoringType(scoringType, gdata.getPublishedAssessmentId());
      for (int i=0; i<gdataList.size(); i++){
        AssessmentGradingData ag = (AssessmentGradingData)gdataList.get(i);
        saveOrUpdateAssessmentGrading(ag);
        EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TOTAL_SCORE_UPDATE, 
        		"siteId=" + AgentFacade.getCurrentSiteId() +
        		", gradedBy=" + AgentFacade.getAgentString() + 
        		", assessmentGradingId=" + ag.getAssessmentGradingId() + 
          		", totalAutoScore=" + ag.getTotalAutoScore() + 
          		", totalOverrideScore=" + ag.getTotalOverrideScore() + 
          		", FinalScore=" + ag.getFinalScore() + 
          		", comments=" + ag.getComments() , true));
      }

      // no need to notify gradebook if this submission is not for grade
      // we only want to notify GB when there are changes
      List newList = getAssessmentGradingsByScoringType(
        scoringType, gdata.getPublishedAssessmentId());
      List l = getListForGradebookNotification(newList, oldList);
      
      notifyGradebook(l, pub);
    } catch (GradebookServiceException ge) {
      log.error("GradebookServiceException" + ge);
      throw ge;
    } 
  }

  /**
   * This method is added to update student's total score to gradebook
   * after submit attept is deleted in assessment.
   * @param gdataList  a list of AssessmentGradingData
   * @param pub   PublishedAssessment object
   * @param studentId
   */

  public void notifyDeleteToGradebook(List gdataList, PublishedAssessmentIfc pub, String studentId){
    try {
      AssessmentGradingData gdata;
      if (gdataList.size()>0) {
        gdata = (AssessmentGradingData) gdataList.get(0);
      } 
      else {
        return;
      }

      Integer scoringType = getScoringType(pub);
      List fullList = getAssessmentGradingsByScoringType(
          scoringType, gdata.getPublishedAssessmentId());

      List l = new ArrayList();
      for (int i=0; i< fullList.size(); i++){
         AssessmentGradingData ag = (AssessmentGradingData)fullList.get(i);
         if (ag.getAgentId().equals(studentId))
               l.add(ag);
      }
      
      //When there is no more submission left for this student, update
      //this student's grade on gradebook as null(the initial state).
      if(l.isEmpty())
    	  l.add(gdata);

      notifyGradebook(l, pub);
    } catch (GradebookServiceException ge) {
         log.error(ge.getMessage(), ge);
         throw ge;
    } catch (Exception e) {
         log.error(e.getMessage(), e);
         throw new RuntimeException(e);
    }

  }

  private List getListForGradebookNotification(List newList, List oldList) {
    List l = new ArrayList();
    Map h = new HashMap();
    for (int i=0; i<oldList.size(); i++){
      AssessmentGradingData ag = (AssessmentGradingData)oldList.get(i);
      h.put(ag.getAssessmentGradingId(), ag);
    }

    for (int i=0; i<newList.size(); i++){
      AssessmentGradingData a = (AssessmentGradingData) newList.get(i);
      Object o = h.get(a.getAssessmentGradingId());
      if (o == null){ // this does not exist in old list, so include it for update
        l.add(a);
      }
      else{ // if new is different from old, include it for update
        AssessmentGradingData b = (AssessmentGradingData) o;
        if ((a.getFinalScore()!=null && b.getFinalScore()!=null) 
            && !a.getFinalScore().equals(b.getFinalScore())) {
            l.add(a);
        }
        // if scores are not modified but comments are added, include it for update
        Optional<String> commentsA = Optional.ofNullable(a.getComments());
        Optional<String> commentsB = Optional.ofNullable(b.getComments());

        if ( !commentsA.orElse("").equals(commentsB.orElse("")) ){
        	l.add(a);
        }
      }
    }
    return l;
  }

  public List getAssessmentGradingsByScoringType(
       Integer scoringType, Long publishedAssessmentId){
    List l;
    // get the list of highest score
    if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
      l = getHighestSubmittedOrGradedAssessmentGradingList(publishedAssessmentId);
    }
    // get the list of last score
    else  if ((scoringType).equals(EvaluationModelIfc.LAST_SCORE)) {
      l = getLastSubmittedOrGradedAssessmentGradingList(publishedAssessmentId);
    }
    else {
      l = getTotalScores(publishedAssessmentId.toString(), "3", false);
    }
    return new ArrayList(l);
  }

  public Integer getScoringType(PublishedAssessmentIfc pub){
    Integer scoringType = null;
    EvaluationModelIfc e = pub.getEvaluationModel();
    if ( e!=null ){
      scoringType = e.getScoringType();
    }
    return scoringType;
  }

  private boolean updateGradebook(AssessmentGradingData data, PublishedAssessmentIfc pub){
    // no need to notify gradebook if this submission is not for grade
    boolean forGrade = (Boolean.TRUE).equals(data.getForGrade());

    boolean toGradebook = false;
    EvaluationModelIfc e = pub.getEvaluationModel();
    if (e != null) {
      toGradebook = StringUtils.equalsAny(e.getToGradeBook(), EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString(), EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString());
    }
    return (forGrade && toGradebook);
  }

  private void notifyGradebook(List l, PublishedAssessmentIfc pub){
    for (int i=0; i<l.size(); i++){
      notifyGradebook((AssessmentGradingData)l.get(i), pub);
    }
  }


  /**
   * Get the score information for each item from the assessment score.
   */
  public Map getItemScores(Long publishedId, Long itemId, String which)
  {
    try {
      return PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(publishedId, itemId, which);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new HashMap();
    }
  }
  
  public Map getItemScores(Long publishedId, Long itemId, String which, boolean loadItemGradingAttachment)
  {
    try {
      return PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(publishedId, itemId, which, loadItemGradingAttachment);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new HashMap();
    }
  }

  public Map getItemScores(Long itemId, List scores, boolean loadItemGradingAttachment)
  {
    try {
      return PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(itemId, scores, loadItemGradingAttachment);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new HashMap();
    }
  }
  
  /**
   * Get the last set of itemgradingdata for a student per assessment
   */
  public Map getLastItemGradingData(String publishedId, String agentId)
  {
    try {
      return PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getLastItemGradingData(Long.valueOf(publishedId), agentId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new HashMap();
    }
  }

  /**
   * Get the grading data for a given submission
   */
  public Map getStudentGradingData(String assessmentGradingId)
  {
    try {
      return PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getStudentGradingData(assessmentGradingId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new HashMap();
    }
  }

  /**
   * Get the last submission for a student per assessment
   */
  public Map getSubmitData(String publishedId, String agentId, Integer scoringoption, String assessmentGradingId)
  {
    try {
      Long gradingId = null;
      if (assessmentGradingId != null) gradingId = Long.valueOf(assessmentGradingId);
      return PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().getSubmitData(Long.valueOf(publishedId), agentId, scoringoption, gradingId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new HashMap();
    }
  }

  public String getTextForId(Long typeId)
  {
    TypeFacadeQueriesAPI typeFacadeQueries =
      PersistenceService.getInstance().getTypeFacadeQueries();
    TypeFacade type = typeFacadeQueries.getTypeFacadeById(typeId);
    return (type.getKeyword());
  }

  public int getSubmissionSizeOfPublishedAssessment(String publishedAssessmentId)
  {
    try{
      return PersistenceService.getInstance().
          getAssessmentGradingFacadeQueries()
          .getSubmissionSizeOfPublishedAssessment(Long.valueOf(
          publishedAssessmentId));
    } catch(Exception e) {
      log.error(e.getMessage(), e);
      return 0;
    }
  }

  public Long saveMedia(byte[] media, String mimeType){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        saveMedia(media, mimeType);
  }

  public Long saveMedia(MediaData mediaData){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        saveMedia(mediaData);
  }

  public MediaData getMedia(String mediaId){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMedia(Long.valueOf(mediaId));
  }

  public List<MediaData> getMediaArray(String itemGradingId){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(Long.valueOf(itemGradingId));
  }
  
  public List<MediaData> getMediaArray2(String itemGradingId){
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	        getMediaArray2(Long.valueOf(itemGradingId));
  }

  public List<MediaData> getMediaArray(ItemGradingData i){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(i);
  }
  
  public Map<Long, List<ItemGradingData>> getMediaItemGradingHash(Long assessmentGradingId) {
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	    getMediaItemGradingHash(assessmentGradingId);
  }
  
  public List<MediaData> getMediaArray(String publishedId, String publishItemId, String which){
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	    getMediaArray(Long.valueOf(publishedId), Long.valueOf(publishItemId), which);
  }
  
  public ItemGradingData getLastItemGradingDataByAgent(String publishedItemId, String agentId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastItemGradingDataByAgent(Long.valueOf(publishedItemId), agentId);
    } catch (Exception e) {
        log.error(e.getMessage(), e);
      return null;
    }
  }

  public ItemGradingData getItemGradingData(String assessmentGradingId, String publishedItemId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGradingData(Long.valueOf(assessmentGradingId), Long.valueOf(publishedItemId));
    } catch (Exception e) {
        log.error(e.getMessage(), e);
      return null;
    }
  }

  public AssessmentGradingData load(String assessmentGradingId) {
	    return load(assessmentGradingId, true);
  }
  
  public AssessmentGradingData load(String assessmentGradingId, boolean loadGradingAttachment) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          load(Long.valueOf(assessmentGradingId), loadGradingAttachment);
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }

  public ItemGradingData getItemGrading(String itemGradingId) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGrading(Long.valueOf(itemGradingId));
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
        throw new Error(e);
    }
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastAssessmentGradingByAgentId(Long.valueOf(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastSavedAssessmentGradingByAgentId(Long.valueOf(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }
  
  public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString, String assessmentGradingId) {

	  AssessmentGradingData assessmentGranding = null;
	  try {
		  if (assessmentGradingId != null) {
			  assessmentGranding = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
			  getLastSubmittedAssessmentGradingByAgentId(Long.valueOf(publishedAssessmentId), agentIdString, Long.valueOf(assessmentGradingId));
		  }
		  else {
			  assessmentGranding = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
			  getLastSubmittedAssessmentGradingByAgentId(Long.valueOf(publishedAssessmentId), agentIdString, null);
		  }
	  }
	  catch(Exception e)
	  {
          log.error(e.getMessage(), e);
          throw new RuntimeException(e);
	  }

	  return assessmentGranding;
  }
  
  public void saveItemGrading(ItemGradingData item)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveItemGrading(item);
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingData assessment)
  {
    try {
      /*	
      // Comment out the whole IF section because the only thing we do here is to 
      // update the itemGradingSet. However, this update is redundant as it will
      // be updated in saveOrUpdateAssessmentGrading(assessment).
      if (assessment.getAssessmentGradingId()!=null 
          && assessment.getAssessmentGradingId().longValue()>0){
	    //1. if assessmentGrading contain itemGrading, we want to insert/update itemGrading first
        Set itemGradingSet = assessment.getItemGradingSet(); 
        Iterator iter = itemGradingSet.iterator();
        while (iter.hasNext()) {
        	ItemGradingData itemGradingData = (ItemGradingData) iter.next();
        	log.debug("date = " + itemGradingData.getSubmittedDate());
        }
        // The following line seems redundant. I cannot see a reason why we need to save the itmeGradingSet
        // here and then again in following saveOrUpdateAssessmentGrading(assessment). Comment it out.    
	    //saveOrUpdateAll(itemGradingSet);
      }
      */
      // this will update itemGradingSet and assessmentGrading. May as well, otherwise I would have
      // to reload assessment again
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAssessmentGrading(assessment);
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }

  }
  
  // This API only touch SAM_ASSESSMENTGRADING_T. No data gets inserted/updated in SAM_ITEMGRADING_T
  public void saveOrUpdateAssessmentGradingOnly(AssessmentGradingData assessment)
  {
	  Set origItemGradingSet = assessment.getItemGradingSet();
	  HashSet h = new HashSet(origItemGradingSet);
	  
	  // Clear the itemGradingSet so no data gets inserted/updated in SAM_ITEMGRADING_T;
	  origItemGradingSet.clear();
      int size = assessment.getItemGradingSet().size();
      log.debug("before persist to db: size = " + size);
      try {
    	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries().saveOrUpdateAssessmentGrading(assessment);
      } catch (Exception e) {
          log.error(e.getMessage(), e);
      }
      finally {
    	  // Restore the original itemGradingSet back
    	  assessment.setItemGradingSet(h);
    	  size = assessment.getItemGradingSet().size();
		  log.debug("after persist to db: size = {}", size);
      }
  }

  public List getAssessmentGradingIds(String publishedItemId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
         getAssessmentGradingIds(Long.valueOf(publishedItemId));
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }

  public AssessmentGradingData getHighestAssessmentGrading(String publishedAssessmentId, String agentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	      getHighestAssessmentGrading(Long.valueOf(publishedAssessmentId), agentId);
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }
  
  public AssessmentGradingData getHighestSubmittedAssessmentGrading(String publishedAssessmentId, String agentId, String assessmentGradingId){
	  AssessmentGradingData assessmentGrading = null;
	  try {
		  if (assessmentGradingId != null) {
			  assessmentGrading = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
			  getHighestSubmittedAssessmentGrading(Long.valueOf(publishedAssessmentId), agentId, Long.valueOf(assessmentGradingId));
		  }
		  else {
			  assessmentGrading = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
			  getHighestSubmittedAssessmentGrading(Long.valueOf(publishedAssessmentId), agentId, null);
		  }
	  }
	  catch(Exception e)
	  {
          log.error(e.getMessage(), e);
          throw new RuntimeException(e);
	  }

	  return  assessmentGrading;
  }
  
  public AssessmentGradingData getHighestSubmittedAssessmentGrading(String publishedAssessmentId, String agentId){
	  return getHighestSubmittedAssessmentGrading(publishedAssessmentId, agentId, null);
  }
  
  public Set getItemGradingSet(String assessmentGradingId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getItemGradingSet(Long.valueOf(assessmentGradingId));
    }
    catch(Exception e){
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }

  public Map<Long, AssessmentGradingData> getAssessmentGradingByItemGradingId(String publishedAssessmentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getAssessmentGradingByItemGradingId(Long.valueOf(publishedAssessmentId));
    }
    catch(Exception e){
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }
  }

  public void updateItemScore(ItemGradingData gdata, double scoreDifference, PublishedAssessmentIfc pub){
    try {
      AssessmentGradingData adata = load(gdata.getAssessmentGradingId().toString());
      adata.setItemGradingSet(getItemGradingSet(adata.getAssessmentGradingId().toString()));

      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      double totalAutoScore = 0;
      double totalOverrideScore = adata.getTotalOverrideScore();
      while (iter.hasNext()){
        ItemGradingData i = (ItemGradingData)iter.next();
        if (i.getItemGradingId().equals(gdata.getItemGradingId())){
	  i.setAutoScore(gdata.getAutoScore());
          i.setComments(gdata.getComments());
          i.setGradedBy(AgentFacade.getAgentString());
          i.setGradedDate(new Date());
	}
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore();
      }
      
      adata.setTotalAutoScore(totalAutoScore);
      if (Double.compare((totalAutoScore+totalOverrideScore),Double.valueOf("0"))<0){
    	  adata.setFinalScore(Double.valueOf("0"));
      }else{
    	  adata.setFinalScore(totalAutoScore+totalOverrideScore);
      }
      saveOrUpdateAssessmentGrading(adata);
      if (scoreDifference != 0){
        notifyGradebookByScoringType(adata, pub);
      }
    } catch (GradebookServiceException ge) {
      log.error(ge.getMessage(), ge);
      throw ge;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingData data, PublishedAssessmentIfc pub,
                          Map publishedItemHash, Map publishedItemTextHash,
                          Map publishedAnswerHash, Map invalidFINMap, List invalidSALengthList) throws GradebookServiceException, FinFormatException
  {
	  log.debug("storeGrades: data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true, invalidFINMap, invalidSALengthList);
  }
  
  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingData data, PublishedAssessmentIfc pub,
                          Map publishedItemHash, Map publishedItemTextHash,
                          Map publishedAnswerHash, boolean persistToDB, Map invalidFINMap, List invalidSALengthList) throws GradebookServiceException, FinFormatException
  {
	  log.debug("storeGrades (not persistToDB) : data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, persistToDB, invalidFINMap, invalidSALengthList);
  }
  
  public void storeGrades(AssessmentGradingData data, boolean regrade, PublishedAssessmentIfc pub,
		  Map publishedItemHash, Map publishedItemTextHash,
		  Map publishedAnswerHash, boolean persistToDB) throws GradebookServiceException, FinFormatException {
	  log.debug("storeGrades (not persistToDB) : data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, regrade, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, persistToDB, null, null);
  }

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  public void storeGrades(AssessmentGradingData data, boolean regrade, PublishedAssessmentIfc pub,
                          Map publishedItemHash, Map publishedItemTextHash,
                          Map publishedAnswerHash, boolean persistToDB, Map invalidFINMap, List invalidSALengthList)
         throws GradebookServiceException, FinFormatException {
    log.debug("****x1. regrade ="+regrade+" "+(new Date()).getTime());
    try {
      String agent = data.getAgentId();
      
      // note that this itemGradingSet is a partial set of answer submitted. it contains only 
      // newly submitted answers, updated answers and MCMR/FIB/FIN answers ('cos we need the old ones to
      // calculate scores for new ones)
      Set<ItemGradingData> itemGradingSet = data.getItemGradingSet();
      if (itemGradingSet == null)
        itemGradingSet = new HashSet<>();
      log.debug("****itemGrading size="+itemGradingSet.size());
      
      List<ItemGradingData> tempItemGradinglist = new ArrayList<>(itemGradingSet);
      
      // CALCULATED_QUESTION - if this is a calc question. Carefully sort the list of answers
      if (isCalcQuestion(tempItemGradinglist, publishedItemHash)) {
	      Collections.sort(tempItemGradinglist, new Comparator<ItemGradingData>(){
	    	  public int compare(ItemGradingData o1, ItemGradingData o2) {
	    		  ItemGradingData gradeData1 = o1;
	    		  ItemGradingData gradeData2 = o2;
	    		  
	    		  // protect against blank ones in samigo initial setup.
	    		  if (gradeData1 == null) return -1; 
	    		  if (gradeData2 == null) return 1;
	    		  if (gradeData1.getPublishedAnswerId() == null) return -1; 
	    		  if (gradeData2.getPublishedAnswerId() == null) return 1; 
	    		  return gradeData1.getPublishedAnswerId().compareTo(gradeData2.getPublishedAnswerId());
	    	  }
	      });
      }
      
    //IMAGEMAP_QUESTION - order by itemGradingId if it is an imageMap question
      if (isImageMapQuestion(tempItemGradinglist, publishedItemHash)) {
	      tempItemGradinglist.sort(new ImageMapGradingItemComparator());
      }
      
      Iterator<ItemGradingData> iter = tempItemGradinglist.iterator();

      // fibEmiAnswersMap contains a map of HashSet of answers for a FIB or EMI item,
      // key =itemid, value= HashSet of answers for each item.  
      // For FIB: This is used to keep track of answers we have already used for
      // mutually exclusive multiple answer type of FIB, such as 
      // The flag of the US is {red|white|blue},{red|white|blue}, and {red|white|blue}.
      // so if the first blank has an answer 'red', the 'red' answer should 
      // not be included in the answers for the other mutually exclusive blanks.
      // For EMI: This keeps track of how many answers were given so we don't give
      // extra marks for to many answers.
      Map fibEmiAnswersMap = new HashMap();
      Map<Long, Map<Long,Set<EMIScore>>> emiScoresMap = new HashMap<>();
      
      //change algorithm based on each question (SAK-1930 & IM271559) -cwen
      Map totalItems = new HashMap();
      log.debug("****x2. {}", (new Date()).getTime());
      double autoScore;
      Long itemId = (long)0;
      Long previousItemId = (long)0;
      int calcQuestionAnswerSequence = 1; // sequence of answers for CALCULATED_QUESTION
      boolean imageMapAlreadyOk = true;
      boolean neededAllOk = false;
      for(ItemGradingData itemGrading: tempItemGradinglist){
        
        // CALCULATED_QUESTION - We increment this so we that calculated 
        // questions can know where we are in the sequence of answers.
        if (itemGrading.getPublishedItemId().equals(itemId)) {
        	calcQuestionAnswerSequence++;
        }
        else {
        	calcQuestionAnswerSequence = 1;
        }
        
        itemId = itemGrading.getPublishedItemId();
        ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
        if (item == null) {
        	//this probably shouldn't happen
        	log.error("unable to retrive itemDataIfc for: {}", publishedItemHash.get(itemId));
        	continue;
        }
        
        Long itemType = item.getTypeId(); 
        /*
         * IMAGEMAP_QUESTIONS can have some items for same question, so we need to remember
         * between iterations if we have some wrong items in a question that has more than one item.
         */
        if(TypeIfc.IMAGEMAP_QUESTION.equals(itemType) && !itemId.equals(previousItemId)) {
        	previousItemId = itemId;
        	imageMapAlreadyOk = true;
        }
        
        for (ItemMetaDataIfc meta : item.getItemMetaDataSet())
        {
          if (meta.getLabel().equals(ItemMetaDataIfc.REQUIRE_ALL_OK))
          {
            if (meta.getEntry().equals("true"))
            {
          	  neededAllOk = true;
              break;
            }
            if (meta.getEntry().equals("false"))
            {
          	  neededAllOk = false;
              break;
            }
          }
        }
        itemGrading.setAssessmentGradingId(data.getAssessmentGradingId());
        //itemGrading.setSubmittedDate(new Date());
        itemGrading.setAgentId(agent);
        itemGrading.setOverrideScore(Double.valueOf(0));

        if (itemType == 5 && itemGrading.getAnswerText() != null) {
        	String processedAnswerText = itemGrading.getAnswerText().replaceAll("\r", "").replaceAll("\n", "");
        	if (processedAnswerText.length() > 32000) {
        		if (invalidSALengthList != null) {
        			invalidSALengthList.add(item.getItemId());
        		}
        	}
        }
        
        // note that totalItems & fibAnswersMap would be modified by the following method
        try {
        	autoScore = getScoreByQuestionType(itemGrading, item, itemType, publishedItemTextHash, 
                               totalItems, fibEmiAnswersMap, emiScoresMap, publishedAnswerHash, regrade, calcQuestionAnswerSequence);
        }
        catch (FinFormatException e) {
        	log.warn("Fin Format Exception while processing response. ", e);
        	autoScore = 0d;
        	if (invalidFINMap != null) {
        		if (invalidFINMap.containsKey(itemId)) {
        			List list = (ArrayList) invalidFINMap.get(itemId);
        			list.add(itemGrading.getItemGradingId());
        		}
        		else {
        			List list = new ArrayList();
        			list.add(itemGrading.getItemGradingId());
        			invalidFINMap.put(itemId, list);
        		}
        	}
        }
        
        if (TypeIfc.IMAGEMAP_QUESTION.equals(itemType) && neededAllOk){
        	if(!imageMapAlreadyOk) {
        		autoScore = 0;
        	}else if(autoScore == WRONG_IMAGE_MAP_ANSWER_NON_PARCIAL){
	        	autoScore = 0;
	        	imageMapAlreadyOk = false;
	        	//itemGradingSet have items with positive score if the actual answer isnt the first of the question
	        	for(ItemGradingData imageMapItem: itemGradingSet) {
	        		if(imageMapItem.getPublishedItemId().equals(itemId))
	        			imageMapItem.setAutoScore(0.0);
	        	}
        	}
        } 
        
        log.debug("**!regrade, autoScore="+autoScore);
        if (!(TypeIfc.MULTIPLE_CORRECT).equals(itemType) && !(TypeIfc.EXTENDED_MATCHING_ITEMS).equals(itemType))
          totalItems.put(itemId, autoScore);
        
        if (regrade && TypeIfc.AUDIO_RECORDING.equals(itemType))
        	itemGrading.setAttemptsRemaining(item.getTriesAllowed());
	
        itemGrading.setAutoScore(autoScore);
      }

      if ((invalidFINMap != null && invalidFINMap.size() > 0) || (invalidSALengthList != null && invalidSALengthList.size() > 0)) {
    	  return;
      }
      // Added persistToDB because if we don't save data to DB later, we shouldn't update the assessment
      // submittedDate either. The date should be sync in delivery bean and DB
      // This is for DeliveryBean.checkDataIntegrity()
      if (!regrade && persistToDB)
      {
    	data.setSubmittedDate(new Date());
        setIsLate(data, pub);
      }
      
      log.debug("****x3. {}", (new Date()).getTime());
      
      List<ItemGradingData> emiItemGradings = new ArrayList<>();
      // the following procedure ensure total score awarded per question is no less than 0
      // this probably only applies to MCMR question type - daisyf
      iter = itemGradingSet.iterator();
      //since the itr goes through each answer (multiple answers for a signle mc question), keep track
      //of its total score by itemId -> autoScore[]{user's score, total possible}
      Map<Long, Double[]> mcmcAllOrNothingCheck = new HashMap<>();
      
      //collect min score information to determine if the auto score will need to be adjusted
      //since there can be multiple questions store in map: itemId -> {user's score, minScore, # of answers}
      Map<Long, Double[]> minScoreCheck = new HashMap<>();
      Map<Long, Boolean> minScoreAnswered = new HashMap<>();
      double totalAutoScoreCheck = 0;
      Map<Long, Integer> countMcmcAllItemGradings = new HashMap<>();
      //get item information to check if it's MCMS and Not Partial Credit
      Long itemType2;
      String mcmsPartialCredit;
      double itemScore = -1;
      while(iter.hasNext())
      {
        ItemGradingData itemGrading = iter.next();
        itemId = itemGrading.getPublishedItemId();
        ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
      //SAM-1724 it's possible the item is not in the hash -DH
        if (item == null) {
        	log.error("unable to retrive itemDataIfc for: " + publishedItemHash.get(itemId));
        	continue;
        }

        itemType2 = item.getTypeId();
        //get item information to check if it's MCMS and Not Partial Credit
        mcmsPartialCredit = item.getItemMetaDataByLabel(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT);
        itemScore = item.getScore();
        //double autoScore = (double) 0;
        
        // this does not apply to EMI
        // just create a short-list and handle differently below
        if ((TypeIfc.EXTENDED_MATCHING_ITEMS).equals(itemType2)) {
            emiItemGradings.add(itemGrading);
        	continue;
        }

        double eachItemScore = ((Double) totalItems.get(itemId));
        if((eachItemScore < 0) && !((TypeIfc.MULTIPLE_CHOICE).equals(itemType2)||(TypeIfc.TRUE_FALSE).equals(itemType2)||(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION).equals(itemType2)))
        {
        	itemGrading.setAutoScore( Double.valueOf(0));
        }
        //keep track of MCMC answer's total score in order to check for all or nothing
        if(TypeIfc.MULTIPLE_CORRECT.equals(itemType2)  && "false".equals(mcmsPartialCredit)){
        	Double accumulatedScore = itemGrading.getAutoScore();
        	if(mcmcAllOrNothingCheck.containsKey(itemId)){
        		Double[] accumulatedScoreArr = mcmcAllOrNothingCheck.get(itemId);
        		accumulatedScore += accumulatedScoreArr[0];
        	}
        	mcmcAllOrNothingCheck.put(itemId, new Double[]{accumulatedScore, item.getScore()});
        	int count = 0;
        	if(countMcmcAllItemGradings.containsKey(itemId))
        		count = (countMcmcAllItemGradings.get(itemId));
        	countMcmcAllItemGradings.put(itemId, ++count);
        }
        //min score check
        if(item.getMinScore() != null){
        	Double accumulatedScore = itemGrading.getAutoScore();
        	Double itemParts = 1d;
        	boolean answered = isAnswered(itemGrading, itemType2);
        	if(minScoreCheck.containsKey(itemId)){
        		Double[] accumulatedScoreArr = minScoreCheck.get(itemId);
        		accumulatedScore += accumulatedScoreArr[0];
        		itemParts += accumulatedScoreArr[2];
        		answered = answered || minScoreAnswered.get(itemId);
        	}
        	minScoreCheck.put(itemId, new Double[]{accumulatedScore, item.getMinScore(), itemParts});
        	minScoreAnswered.put(itemId, answered);
        }
      }
      
      log.debug("****x3.1 {}", (new Date()).getTime());

      // Loop 1: this procedure ensure total score awarded per EMI item
      // is correct
      // For emi's there are multiple gradings per item per question,
      // for the grading we only know scores after grading so we need
      // to reset the grading score here to the correct scores
      // this currently only applies to EMI question type
      if (!emiItemGradings.isEmpty()) {
    	  Map<Long, Map<Long, Map<Long, EMIScore>>> emiOrderedScoresMap = reorderEMIScoreMap(emiScoresMap);
    	  iter = emiItemGradings.iterator();
    	  while (iter.hasNext()) {
    		  ItemGradingData itemGrading = iter.next();

    		  //SAM-2016 check for Nullity
    		  if (itemGrading == null) {
    			  log.warn("Map contains null itemgrading!");
    			  continue;
    		  }
    		  
    		  Map<Long, Map<Long, EMIScore>> innerMap = emiOrderedScoresMap
    				  .get(itemGrading.getPublishedItemId()); 
    		  
    		  if (innerMap == null) {
    			  log.warn("Inner map is empty!");
    			  continue;
    		  }
    		  
    		  Map<Long, EMIScore> scoreMap = innerMap
    				  .get(itemGrading.getPublishedItemTextId());
    		  
    		  if (scoreMap == null) {
    			  log.warn("Score map is empty!");
    			  continue;
    		  }
    		  
     		  EMIScore score = scoreMap
    				  .get(itemGrading.getPublishedAnswerId());
    		if (score == null) {
    			//its possible! SAM-2016 
    			log.warn("we can't find a score for answer: {}", itemGrading.getPublishedAnswerId());
    			continue;
    		}
    		  itemGrading.setAutoScore(emiOrderedScoresMap
    				  .get(itemGrading.getPublishedItemId())
    				  .get(itemGrading.getPublishedItemTextId())
    				  .get(itemGrading.getPublishedAnswerId()).effectiveScore);
    	  }
    	  totalAutoScoreCheck = 0;
      }
      
      // if it's MCMS and Not Partial Credit and the score isn't 100% (totalAutoScoreCheck != itemScore),
      // that means the user didn't answer all of the correct answers only.  
      // We need to set their score to 0 for all ItemGrading items
      for(Entry<Long, Double[]> entry : mcmcAllOrNothingCheck.entrySet()){
    	  if(!Precision.equalsIncludingNaN(entry.getValue()[0], entry.getValue()[1], 0.001d)) {	  
    		  //reset all scores to 0 since the user didn't get all correct answers
    		  iter = itemGradingSet.iterator();
    		  while(iter.hasNext()){
    			  ItemGradingData itemGrading = iter.next();
    			  Long itemId2 = entry.getKey();
    			  if(itemGrading.getPublishedItemId().equals(itemId2)){
    				  AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(itemGrading.getPublishedAnswerId());
    			      if (answer == null) {
      					    itemGrading.setAutoScore(Double.valueOf(0));
      			        	log.error("unable to retrieve answerIfc for: " + itemId2);
      			        	continue;
      			      }
      				  if(!countMcmcAllItemGradings.containsKey(itemId2)){
      					  	itemGrading.setAutoScore(Double.valueOf(0));
      					  	log.error("unable to retrieve itemGrading's counter for: " + itemId2);
      					  	continue;
      				  }	  
      				  double discount = (Math.abs(answer.getDiscount()) * ((double) -1));
      				  int count = (countMcmcAllItemGradings.get(itemId2));
      				  double itemGrDisc = discount/count;
      				  itemGrading.setAutoScore(itemGrDisc);
    			  }
    		  }
    	  }
      }

        // If there is a minimum score value, then make sure the auto score is at least the minimum
        // entry.getValue()[0] = total score for the question
        // entry.getValue()[1] = min score
        // entry.getValue()[2] = how many question answers to divide minScore across
        for (Entry<Long, Double[]> entry : minScoreCheck.entrySet()) {
            if (entry.getValue()[0] <= entry.getValue()[1]) {
                //reset all scores to 0 since the user didn't get all correct answers
                for (ItemGradingData itemGrading : itemGradingSet) {
                    ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemGrading.getPublishedItemId());
                    if (!Boolean.valueOf(item.getPartialCreditFlag())) {
                        // We should only set the autoScore to the min score
                        // if partial credit is not in effect
                        if (minScoreAnswered.get(entry.getKey()) && itemGrading.getPublishedItemId().equals(entry.getKey())) {
                            itemGrading.setAutoScore(entry.getValue()[1]/entry.getValue()[2]);
                        }
                    }
                }
            }
        }
      
      log.debug("****x4. {}", Instant.now().toEpochMilli());

      // save#1: this itemGrading Set is a partial set of answers submitted. it contains new answers and
      // updated old answers and FIB answers ('cos we need the old answer to calculate the score for new
      // ones). we need to be cheap, we don't want to update record that hasn't been
      // changed. Yes, assessmentGrading's total score will be out of sync at this point, I am afraid. It
      // would be in sync again once the whole method is completed sucessfully. 
      if (persistToDB) {
    	  saveOrUpdateAll(itemGradingSet);
      }
      log.debug("****x5. "+(new Date()).getTime());

      

      // save#2: now, we need to get the full set so we can calculate the total score accumulate for the
      // whole assessment.
      Set fullItemGradingSet = getItemGradingSet(data.getAssessmentGradingId().toString());
      double totalAutoScore = getTotalAutoScore(fullItemGradingSet);
      data.setTotalAutoScore(totalAutoScore);
     
      if (Double.compare((totalAutoScore + data.getTotalOverrideScore()), new Double("0"))<0){
    	  data.setFinalScore( Double.valueOf("0"));
      }else{
    	  data.setFinalScore(totalAutoScore + data.getTotalOverrideScore());
      }
      log.debug("****x6. "+(new Date()).getTime());
    } catch (GradebookServiceException ge) {
      log.error(ge.getMessage(), ge);
      throw ge;
    } 
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    // save#3: itemGradingSet has been saved above so just need to update assessmentGrading
    // therefore setItemGradingSet as empty first - daisyf
    // however, if we do not persit to DB, we want to keep itemGradingSet with data for later use
    // Because if itemGradingSet is not saved to DB, we cannot go to DB to get it. We have to 
    // get it through data.
    if (persistToDB) {
        data.setItemGradingSet(new HashSet());
    	saveOrUpdateAssessmentGrading(data);
    	log.debug("****x7. {}", (new Date()).getTime());	
    	if (!regrade) {
    		notifyGradebookByScoringType(data, pub);
    	}
    }
    log.debug("****x8. {}", (new Date()).getTime());

    // I am not quite sure what the following code is doing... I modified this based on my assumption:
    // If this happens dring regrade, we don't want to clean these data up
    // We only want to clean them out in delivery
    if (!regrade && Boolean.TRUE.equals(data.getForGrade())) {
    	// remove the assessmentGradingData created during gradiing (by updatding total score page)
    	removeUnsubmittedAssessmentGradingData(data);
    }
  }

  private double getTotalAutoScore(Set itemGradingSet){
    BigDecimal totalAutoScore = BigDecimal.ZERO;
    for(ItemGradingData itemGradingData : (Set<ItemGradingData>) itemGradingSet){
        if (itemGradingData.getAutoScore()!=null){
            totalAutoScore = totalAutoScore.add(new BigDecimal(itemGradingData.getAutoScore()));
        }
    }
    return totalAutoScore.doubleValue();
  }

  private boolean isAnswered(ItemGradingData data, Long type) {
    if(TypeIfc.MATCHING.equals(type)) {
        if (data.getPublishedAnswerId() != null) {
          return true;
        }
    } else if(TypeIfc.ESSAY_QUESTION.equals(type) || TypeIfc.FILL_IN_BLANK.equals(type) || TypeIfc.FILL_IN_NUMERIC.equals(type)) {
        if (StringUtils.isNotEmpty(data.getAnswerText())) {
          return true;
        }
    } else if(TypeIfc.IMAGEMAP_QUESTION.equals(type)) {
        if (StringUtils.isNotEmpty(data.getAnswerText()) && data.getAnswerText().matches("\\{\"x\":-?\\d+,\"y\":-?\\d+\\}")) {
          return true;
        }
    } else {
        if (data.getPublishedAnswerId() != null || StringUtils.isNotEmpty(data.getAnswerText())) {
          return true;
        }
	}
	return false;
  }

  public void notifyGradebookByScoringType(AssessmentGradingData data, PublishedAssessmentIfc pub){
    if (pub == null || pub.getEvaluationModel() == null) {
      // should not come to here
      log.warn("publishedAssessment is null or publishedAssessment.getEvaluationModel() is null");
      return;
    }

    if (StringUtils.equalsAny(pub.getEvaluationModel().getToGradeBook(), EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString(), EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString())) {
      Integer scoringType = pub.getEvaluationModel().getScoringType();
      if (updateGradebook(data, pub)) {
        AssessmentGradingData d = data; // data is the last submission
        // need to decide what to tell gradebook
        if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)) {
          // If this next call comes back null, don't overwrite our real AG with a null one
          final AssessmentGradingData highestAG = getHighestSubmittedAssessmentGrading(pub.getPublishedAssessmentId().toString(), data.getAgentId());
          if (highestAG != null) {
            d = highestAG;
          }
        }
        // Send the average score if average was selected for multiple submissions
        else if (scoringType.equals(EvaluationModelIfc.AVERAGE_SCORE)) {
          // status = 5: there is no submission but grader update something in the score page
          if(data.getStatus() == AssessmentGradingData.NO_SUBMISSION) {
            d.setFinalScore(data.getFinalScore());
          } else {
            Double averageScore = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
              getAverageSubmittedAssessmentGrading(pub.getPublishedAssessmentId(), data.getAgentId());
            d.setFinalScore(averageScore);
          }
        }
        notifyGradebook(d, pub);
      }
    }
  }

  private double getScoreByQuestionType(ItemGradingData itemGrading, ItemDataIfc item,
                                       Long itemType, Map publishedItemTextHash, 
                                       Map totalItems, Map fibAnswersMap, Map<Long, Map<Long,Set<EMIScore>>> emiScoresMap,
                                       Map publishedAnswerHash, boolean regrade,
                                       int calcQuestionAnswerSequence) throws FinFormatException {
    //double score = (double) 0;
    double initScore;
    double autoScore = (double) 0;
    double accumelateScore;
    Long itemId = item.getItemId();
    int type = itemType.intValue();
    switch (type){ 
    case 1: // MC Single Correct
    	if(item.getPartialCreditFlag())
    		autoScore = getAnswerScoreMCQ(itemGrading, publishedAnswerHash);
    	else{
    		autoScore = getAnswerScore(itemGrading, publishedAnswerHash);
    	}
    	//overridescore
    	if (itemGrading.getOverrideScore() != null)
    		autoScore += itemGrading.getOverrideScore();
    	totalItems.put(itemId, autoScore);
    	break;// MC Single Correct
      case 12: // MC Multiple Correct Single Selection    	  
      case 3: // MC Survey
      case 4: // True/False     	  
              autoScore = getAnswerScore(itemGrading, publishedAnswerHash);
              //overridescore
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore();
	      totalItems.put(itemId, autoScore);
              break;
      case 2: // MC Multiple Correct
              ItemTextIfc itemText = (ItemTextIfc) publishedItemTextHash.get(itemGrading.getPublishedItemTextId());
              List answerArray = itemText.getAnswerArray();
              int correctAnswers = 0;
              if (answerArray != null){
                for (int i =0; i<answerArray.size(); i++){
                  AnswerIfc a = (AnswerIfc) answerArray.get(i);
                  if (a.getIsCorrect())
                    correctAnswers++;
                }
              }

              initScore = getAnswerScore(itemGrading, publishedAnswerHash);
              BigDecimal initialScore = new BigDecimal(initScore);
              BigDecimal automaticScore = BigDecimal.ZERO;

              if (initialScore.compareTo(BigDecimal.ZERO) == 1){
                  automaticScore = initialScore.divide(new BigDecimal(correctAnswers), MathContext.DECIMAL128);
              } else{
                  automaticScore = new BigDecimal(getTotalCorrectScore(itemGrading, publishedAnswerHash))
                      .divide(new BigDecimal(correctAnswers), MathContext.DECIMAL128);
                  automaticScore = automaticScore.multiply(new BigDecimal(-1.0d), MathContext.DECIMAL128);
              }

              //overridescore?
              if (itemGrading.getOverrideScore() != null){
                autoScore = automaticScore.add(new BigDecimal(itemGrading.getOverrideScore())).doubleValue();
              }

              if (!totalItems.containsKey(itemId)){
                  totalItems.put(itemId, autoScore);
              } else{
                  BigDecimal accumulatedScore = new BigDecimal(((Double)totalItems.get(itemId)));
                  accumulatedScore = accumulatedScore.add(new BigDecimal(autoScore));
                  accumelateScore = accumulatedScore.doubleValue();
                  totalItems.put(itemId, accumelateScore);
              }
              break;

      case 9: // Matching
              initScore = isThisItemDistractor(item, itemGrading) ? getScoreDistractor(item, itemGrading) : getAnswerScore(itemGrading, publishedAnswerHash);
              if (initScore > 0) {
                autoScore = initScore / item.getItemTextArraySorted().size();
              } else {
                autoScore = initScore;
              }
              //overridescore?
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, autoScore);
              else {
                accumelateScore = ((Double)totalItems.get(itemId));
                accumelateScore += autoScore;
                totalItems.put(itemId, accumelateScore);
              }
              break;

      case 8: // FIB
              autoScore = getFIBScore(itemGrading, fibAnswersMap, item, publishedAnswerHash) / (double) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
              //overridescore - cwen
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, autoScore);
              else {
                accumelateScore = ((Double)totalItems.get(itemId));
                accumelateScore += autoScore;
                totalItems.put(itemId, accumelateScore);
              }
              break;
      case 15:  // CALCULATED_QUESTION
      case 11: // FIN
    	  try {
    	      if (type == 15) {  // CALCULATED_QUESTION
	              Map<Integer, String> calculatedAnswersMap = getCalculatedAnswersMap(itemGrading, item, calcQuestionAnswerSequence);
	              int numAnswers = calculatedAnswersMap.size();
	              autoScore = getCalcQScore(itemGrading, item, calculatedAnswersMap, calcQuestionAnswerSequence ) / (double) numAnswers;
	          } else {
	              autoScore = getFINScore(itemGrading, item, publishedAnswerHash) / (double) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
	          }
    	  }
    	  catch (FinFormatException e) {
    		  throw e;
    	  }
    	  //overridescore - cwen
          if (itemGrading.getOverrideScore() != null)
            autoScore += itemGrading.getOverrideScore();

          if (!totalItems.containsKey(itemId))
            totalItems.put(itemId, autoScore);
          else {
            accumelateScore = (Double) totalItems.get(itemId);
            accumelateScore += autoScore;
            totalItems.put(itemId, accumelateScore);
          }
          break;

      case 14: // EMI
    	  autoScore = getEMIScore(itemGrading, itemId, totalItems, emiScoresMap, publishedItemTextHash, publishedAnswerHash);
          break;
      case 5: // SAQ
      case 6: // file upload
      case 7: // audio recording
              //overridescore - cwen
    	  	  if (regrade && itemGrading.getAutoScore() != null) {
    	  	    autoScore = itemGrading.getAutoScore();
    	  	  }
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore();
              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, autoScore);
              else {
                accumelateScore = (Double) totalItems.get(itemId);
                accumelateScore += autoScore;
                totalItems.put(itemId, accumelateScore);
              }
              break;
      case 16:    	  
    	  initScore = getImageMapScore(itemGrading,item, publishedItemTextHash,publishedAnswerHash);
    	  //if one answer is 0 or negative, and need all OK to be scored, then autoScore=WRONG_IMAGE_MAP_ANSWER_NON_PARCIAL
    	  //and we break the case...
    	  
    	  boolean neededAllOk = false;
    	  Iterator i = item.getItemMetaDataSet().iterator();
          while (i.hasNext())
          {
            ItemMetaDataIfc meta = (ItemMetaDataIfc) i.next();
            if (meta.getLabel().equals(ItemMetaDataIfc.REQUIRE_ALL_OK))
            {
              if (meta.getEntry().equals("true"))
              {
            	  neededAllOk = true;
                break;
    }
            }
          }
    	  if (neededAllOk && (initScore <= 0)){
    		  autoScore = WRONG_IMAGE_MAP_ANSWER_NON_PARCIAL;
    		  break;
    	  }
          //if (initScore > 0) {
      	         autoScore += initScore ;
          //}
    	  
          //overridescore?
          if (itemGrading.getOverrideScore() != null)
            autoScore += itemGrading.getOverrideScore();
          
          if (!totalItems.containsKey(itemId)){
            totalItems.put(itemId, autoScore);
          }else {
            accumelateScore = (Double) totalItems.get(itemId);
            accumelateScore += autoScore;
            totalItems.put(itemId, accumelateScore);
          }
          
          break;
    }
    
    return autoScore;
  }

  private boolean isThisItemDistractor(ItemDataIfc item, ItemGradingData thisItemGradingData) {
    for (ItemTextIfc curItem : item.getItemTextArraySorted()) {
      if (isDistractor(curItem) && curItem.getId().equals(thisItemGradingData.getPublishedItemTextId())) {
        return true;
      }
    }
    return false;
  }

  private double getScoreDistractor(ItemDataIfc item, ItemGradingData thisItemGradingData) {
    if (thisItemGradingData.getPublishedAnswerId() == null) {
      return 0;
    } else if (thisItemGradingData.getPublishedAnswerId() < 0) {
      return item.getScore();
    } else {
      return 0;
    }
  }

/**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingData for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public double getAnswerScore(ItemGradingData data, Map publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null) {
    	return (double) 0;
    }
    ItemDataIfc item = (ItemDataIfc) answer.getItem();
    Long itemType = item.getTypeId();
    if (answer.getIsCorrect() == null || !answer.getIsCorrect())
    {
    	// return (double) 0;
    	// Para que descuente (For discount)
    	double score = (double) 0;
    	if ((TypeIfc.EXTENDED_MATCHING_ITEMS).equals(itemType)||(TypeIfc.MULTIPLE_CHOICE).equals(itemType)||(TypeIfc.TRUE_FALSE).equals(itemType)||(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION).equals(itemType)){
    		score = Math.abs(answer.getDiscount()) * ((double) -1);
    	}

    	answer.setPartialCredit(score);
    	return score;
    }
    return answer.getScore();
  }

  public void updateAutosubmitEventLog(AssessmentGradingData adata) {
	  EventLogService eventService = new EventLogService();
	  EventLogFacade eventLogFacade = new EventLogFacade();
	  Long gradingId = adata.getAssessmentGradingId();

	  List<EventLogData> eventLogDataList = eventService.getEventLogData(gradingId);
	  if (!eventLogDataList.isEmpty()) {
		  EventLogData eventLogData= (EventLogData) eventLogDataList.get(0);
		  //will do the i18n issue later.
		  eventLogData.setErrorMsg("no_error_auto_submit");
		  final Date endDate = adata.getSubmittedDate() != null ? adata.getSubmittedDate() : new Date();
		  eventLogData.setEndDate(endDate);
		  if(eventLogData.getStartDate() != null) {
			  double minute= 1000*60;
			  int eclipseTime = (int)Math.ceil(((endDate.getTime() - eventLogData.getStartDate().getTime())/minute));
			  eventLogData.setEclipseTime(eclipseTime); 
		  } else {
			  eventLogData.setEclipseTime(null); 
			  eventLogData.setErrorMsg("error_auto_submit");
		  }
		  eventLogFacade.setData(eventLogData);
		  eventService.saveOrUpdateEventLog(eventLogFacade);
	  }

	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_AUTO_SUBMIT_JOB,
			  AutoSubmitAssessmentsJob.safeEventLength("publishedAssessmentId=" + adata.getPublishedAssessmentId() + 
					  ", assessmentGradingId=" + gradingId), true));

	  Map<String, Object> notiValues = new HashMap<>();
	  notiValues.put("publishedAssessmentID", adata.getPublishedAssessmentId());
	  notiValues.put("assessmentGradingID", gradingId);
	  notiValues.put("userID", adata.getAgentId());
	  notiValues.put("submissionDate", adata.getSubmittedDate());

	  String confirmationNumber = adata.getAssessmentGradingId() + "-" + adata.getPublishedAssessmentId() + "-"
			  + adata.getAgentId() + "-" + adata.getSubmittedDate().getTime();
	  notiValues.put( "confirmationNumber", confirmationNumber );

	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO, notiValues.toString(), AgentFacade.getCurrentSiteId(), false, SamigoConstants.NOTI_EVENT_ASSESSMENT_SUBMITTED));
  }
  
  private void notifyGradebook(AssessmentGradingData data, PublishedAssessmentIfc pub) throws GradebookServiceException {
    // If the assessment is published to the gradebook, make sure to update the scores in the gradebook
    String toGradebook = pub.getEvaluationModel().getToGradeBook();

    org.sakaiproject.grading.api.GradingService gradingService = null;
    boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
    if (integrated)
    {
      gradingService = (org.sakaiproject.grading.api.GradingService) SpringBeanLocator.getInstance().
        getBean("org.sakaiproject.grading.api.GradingService");
    }

    GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();

    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    String currentSiteId = publishedAssessmentService.getPublishedAssessmentSiteId(pub.getPublishedAssessmentId().toString());

    if (StringUtils.equalsAny(toGradebook, EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString(), EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString())) {
        if(log.isDebugEnabled()) log.debug("Attempting to update a score in the gradebook");

    // add retry logic to resolve deadlock problem while sending grades to gradebook

    Double originalFinalScore = data.getFinalScore();
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
    	try {
    		if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().equals(toGradebook))
    			gbsHelper.updateExternalAssessmentScore(data, gradingService);
    		else if (EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(toGradebook)) {
          if (gbsHelper.isGradebookGroupEnabled(gradingService)) {
            String gradebookItemString = pub.getAssessmentToGradebookNameMetaData();

            if (gradebookItemString != null && !StringUtils.isBlank(gradebookItemString)) {
              List<String> gradebookItemList = Arrays.asList(gradebookItemString.split(","));

              for (String gradebookItem : gradebookItemList) {
                gbsHelper.updateExternalAssessmentScore(data, gradingService, Long.parseLong(gradebookItem));
              }
            }
          } else {
            Long gradebookItemId = Long.valueOf(pub.getAssessmentToGradebookNameMetaData());
            gbsHelper.updateExternalAssessmentScore(data, gradingService, gradebookItemId);
          }
    		}
    		retryCount = 0;
    	}
      catch (org.sakaiproject.grading.api.AssessmentNotFoundException ante) {
    	  log.warn("problem sending grades to gradebook: {}", ante.getMessage());
          if (AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(pub.getStatus())) {
        	  retryCount = retry(retryCount, ante, pub, true);
          }
          else {
        	  // Otherwise, do the same exeption handling as others
        	  retryCount = retry(retryCount, ante, pub, false);
          }
      }
      catch (Exception e) {
    	  retryCount = retry(retryCount, e, pub, false);
      }
    }

    // change the final score back to the original score since it may set to average score.
    // if we're deleting the last submission, the score might be null bugid 5440
    if(originalFinalScore != null && data.getFinalScore() != null && !(Precision.equalsIncludingNaN(data.getFinalScore(), originalFinalScore, 0.0001))) {
    	data.setFinalScore(originalFinalScore);
    }
    
    try {
        	String agent = data.getAgentId();
        	String comment = data.getComments();
        	gbsHelper.updateExternalAssessmentComment(data, agent, comment, gradingService);
    }
    catch (Exception ex) {
          log.warn("Error sending comments to gradebook: {}", ex.getMessage());
          }
    } else {
       log.debug("Not updating the gradebook.  toGradebook = {}", toGradebook);
    }
  }

  private int retry(int retryCount, Exception e, PublishedAssessmentIfc pub, boolean retractForEditStatus) {
	  log.warn("retrying...sending grades to gradebook. ");
	  log.warn("retry....");
      retryCount--;
      try {
    	  int deadlockInterval = PersistenceService.getInstance().getPersistenceHelper().getDeadlockInterval();
    	  Thread.sleep(deadlockInterval);
      }
      catch(InterruptedException ex){
    	  log.warn(ex.getMessage());
      }
      if (retryCount==0) {
    	  if (retractForEditStatus) {
    		  // This happens in following scenario:
              // 1. The assessment is active and has "None" for GB setting
              // 2. Instructor retracts it for edit and update the to "Send to GB"
              // 3. Instructor updates something on the total Score page
              // Because the GB will not be created until the assessment gets republished,
              // "AssessmentNotFoundException" will be thrown here. Since, this is the expected
              // exception, we simply log a debug message without retrying or notifying the user.
              // Of course, you can argue about what if the assessment gets deleted by other cause.
              // But I would say the major cause would be this "retract" scenario. Also, without knowing 
        	  // the change history of the assessment, I think this is the best handling. 
        	  log.info("We quietly sallow the AssessmentNotFoundException excption here. Published Assessment Name: " + pub.getTitle());
    	  }
    	  else {
    		  // after retries, still failed updating gradebook
    		  log.warn("After all retries, still failed ...  Now throw error to UI");
    		  throw new GradebookServiceException(e);
    	  }
      }
      return retryCount;
  }
  
 /**
   * This grades Fill In Blank questions.  (see SAK-1685) 

   * There will be two valid cases for scoring when there are multiple fill 
   * in blanks in a question:

   * Case 1- There are different sets of answers (a set can contain one or more 
   * item) for each blank (e.g. The {dog|coyote|wolf} howls and the {lion|cougar} 
   * roars.) In this case each blank is tested for correctness independently. 

   * Case 2-There is the same set of answers for each blank: e.g.  The flag of the US 
   * is {red|white|blue},{red|white|blue}, and {red|white|blue}. 

   * These are the only two valid types of questions. When authoring, it is an 
   * ERROR to include: 

   * (1) a mixture of independent answer and common answer blanks 
   * (e.g. The {dog|coyote|wolf} howls at the {red|white|blue}, {red|white|blue}, 
   * and {red|white|blue} flag.)

   * (2) more than one set of blanks with a common answer ((e.g. The US flag 
   * is {red|white|blue}, {red|white|blue}, and {red|white|blue} and the Italian 
   * flag is {red|white|greem}, {red|white|greem}, and {red|white|greem}.)

   * These two invalid questions specifications should be authored as two 
   * separate questions.

Here are the definition and 12 cases I came up with (lydia, 01/2006):

 single answers : roses are {red} and vilets are {blue}
 multiple answers : {dogs|cats} have 4 legs 
 multiple answers , mutually exclusive, all answers must be identical, can be in diff. orders : US flag has {red|blue|white} and {red |white|blue} and {blue|red|white} colors
 multiple answers , mutually non-exclusive : {dogs|cats} have 4 legs and {dogs|cats} can be pets. 
 wildcard uses *  to mean one of more characters 


-. wildcard single answer, case sensitive
-. wildcard single answer, case insensitive
-. single answer, no wildcard , case sensitive
-. single answer, no wildcard , case insensitive
-. multiple answer, mutually non-exclusive, no wildcard , case sensitive
-. multiple answer, mutually non-exclusive, no wildcard , case in sensitive
-. multiple answer, mutually non-exclusive, wildcard , case sensitive
-. multiple answer, mutually non-exclusive, wildcard , case insensitive
-. multiple answer, mutually exclusive, no wildcard , case sensitive
-. multiple answer, mutually exclusive, no wildcard , case in sensitive
-. multiple answer, mutually exclusive, wildcard , case sensitive
-. multiple answer, mutually exclusive, wildcard , case insensitive

  */
  public double getFIBScore(ItemGradingData data, Map fibmap,  ItemDataIfc itemdata, Map publishedAnswerHash)
  {
    String studentanswer = "";
    boolean matchresult = false;
    double totalScore = (double) 0;
    data.setIsCorrect(Boolean.FALSE);
    
    if (data.getPublishedAnswerId() == null) {
    	return totalScore;
    }
    AnswerIfc answerIfc = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answerIfc == null) {
    	return totalScore;
    }
    String answertext = answerIfc.getText();
    Long itemId = itemdata.getItemId();

    String casesensitive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB);
    String mutuallyexclusive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB);
    String ignorespaces = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB);
    //Set answerSet = new HashSet();

    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        boolean ignoreSpaces = "true".equalsIgnoreCase(ignorespaces);
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
            matchresult = fibmatch(answer, studentanswer, true, ignoreSpaces);
             
          }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
    	    matchresult = fibmatch(answer, studentanswer, false, ignoreSpaces);
           }
        }  // else , case insensitive
 
        if (matchresult){

            boolean alreadyused=false;
// add check for mutual exclusive
            if ("true".equalsIgnoreCase(mutuallyexclusive))
            {
              // check if answers are already used.
              Set answer_used_sofar = (HashSet) fibmap.get(itemId);
              if ((answer_used_sofar!=null) && ( answer_used_sofar.contains(studentanswer.toLowerCase()))){
                // already used, so it's a wrong answer for mutually exclusive questions
                alreadyused=true;
              }
              else {
                // not used, it's a good answer, now add this to the already_used list.
                // we only store lowercase strings in the fibmap.
                if (answer_used_sofar==null) {
                  answer_used_sofar = new HashSet();
                }

                answer_used_sofar.add(studentanswer.toLowerCase());
                fibmap.put(itemId, answer_used_sofar);
              }
            }

            if (!alreadyused) {
              totalScore += ((AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId())).getScore();
              data.setIsCorrect(Boolean.TRUE);
            }

            // SAK-3005: quit if answer is correct, e.g. if you answered A for {a|A}, you already scored
            break;
          }
     }
    }
    return totalScore;
  }

  public boolean getFIBResult(ItemGradingData data, Map fibmap,  ItemDataIfc itemdata, Map publishedAnswerHash)
  {
	  // this method is similiar to getFIBScore(), except it returns true/false for the answer, not scores.  
	  // may be able to refactor code out to be reused, but totalscores for mutually exclusive case is a bit tricky. 
    String studentanswer = "";
    boolean matchresult = false;

    if (data.getPublishedAnswerId() == null) {
    	return matchresult;
    }
    
    AnswerIfc answerIfc = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answerIfc == null) {
    	return matchresult;
    }
    String answertext = answerIfc.getText();
    Long itemId = itemdata.getItemId();

    String casesensitive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB);
    String mutuallyexclusive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB);
    String ignorespaces = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB);
    //Set answerSet = new HashSet();


    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        boolean ignoreSpaces = "true".equalsIgnoreCase(ignorespaces);
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
            matchresult = fibmatch(answer, studentanswer, true, ignoreSpaces);
           }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
    	    matchresult = fibmatch(answer, studentanswer, false, ignoreSpaces);
           }
        }  // else , case insensitive
 
        if (matchresult){

            boolean alreadyused=false;
// add check for mutual exclusive
            if ("true".equalsIgnoreCase(mutuallyexclusive))
            {
              // check if answers are already used.
              Set answer_used_sofar = (HashSet) fibmap.get(itemId);
              if ((answer_used_sofar!=null) && ( answer_used_sofar.contains(studentanswer.toLowerCase()))){
                // already used, so it's a wrong answer for mutually exclusive questions
                alreadyused=true;
              }
              else {
                // not used, it's a good answer, now add this to the already_used list.
                // we only store lowercase strings in the fibmap.
                if (answer_used_sofar==null) {
                  answer_used_sofar = new HashSet();
                }

                answer_used_sofar.add(studentanswer.toLowerCase());
                fibmap.put(itemId, answer_used_sofar);
              }
            }

            if (alreadyused) {
              matchresult = false;
            }

             break;
          }
      
     }
    }
    return matchresult;
  }
  
  public double getFINScore(ItemGradingData data,  ItemDataIfc itemdata, Map publishedAnswerHash) throws FinFormatException
  {
	  data.setIsCorrect(Boolean.FALSE);
	  double totalScore = (double) 0;
	  boolean matchresult = getFINResult(data, itemdata, publishedAnswerHash);
	  if (matchresult){
		  totalScore += ((AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId())).getScore();
		  data.setIsCorrect(Boolean.TRUE);
	  }	
	  return totalScore;
	  
  }
	  
  public boolean getFINResult (ItemGradingData data,  ItemDataIfc itemdata, Map publishedAnswerHash) throws FinFormatException
  {
	  String studentanswer = "";
	  boolean range;
	  boolean matchresult = false;
	  ComplexFormat complexFormat = new ComplexFormat();
	  Complex answerComplex = null;
	  Complex studentAnswerComplex;
	  BigDecimal answerNum = null, answer1Num = null, answer2Num = null, studentAnswerNum;

	  if (data.getPublishedAnswerId() == null) {
		  return false;
	  }

	  AnswerIfc answerIfc = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
	  if (answerIfc == null) {
		  return matchresult;
	  }
	  String answertext = answerIfc.getText();
	  if (answertext != null)
	  {
		  StringTokenizer st = new StringTokenizer(answertext, "|");
		  range = false;
		  if (st.countTokens() > 1) {
			  range = true;
		  }

		  String studentAnswerText = null;
		  if (data.getAnswerText() != null) {
			  studentAnswerText = data.getAnswerText().replaceAll("\\s+", "").replace(',','.');    // in Spain, comma is used as a decimal point
			  //The UI syntax expects a format with curly braces for complex numbers, remove them for the Commons Math library.
			  if(StringUtils.contains(studentAnswerText, "{") && StringUtils.contains(studentAnswerText, "}")){
				  studentAnswerText = StringUtils.replace(studentAnswerText, "{", "");
				  studentAnswerText = StringUtils.replace(studentAnswerText, "}", "");
			  }
		  }

		  if (range) {
              String answer1 = st.nextToken().trim().replaceAll("\\s+", "").replace(',', '.');
              String answer2 = st.nextToken().trim().replaceAll("\\s+", "").replace(',', '.');
		 
			  try {
				  answer1Num = new BigDecimal(answer1);
				  answer2Num = new BigDecimal(answer2);
			  } catch (Exception e) {
				  log.debug("Number is not BigDecimal: " + answer1 + " or " + answer2);
			  }

			  Map map = validate(studentAnswerText);
			  studentAnswerNum = (BigDecimal) map.get(ANSWER_TYPE_REAL);

			  matchresult = (answer1Num != null && answer2Num != null && studentAnswerNum != null &&
					  (answer1Num.compareTo(studentAnswerNum) <= 0) && (answer2Num.compareTo(studentAnswerNum) >= 0));
		  }
		  else { // not range
			  String answer = st.nextToken().trim();

			  if (answer != null){ 	 
		             answer = answer.replaceAll("\\s+", "").replace(',','.');  // in Spain, comma is used as a decimal point 	 
		             //The UI syntax expects a format with curly braces, remove them for the Commons Math library.
		             if(StringUtils.contains(answer, "{") && StringUtils.contains(answer, "}")){
		                 answer = StringUtils.replace(answer, "{", "");
		                 answer = StringUtils.replace(answer, "}", "");
		             }
			  }	 
		 
			  try {
				  answerNum = new BigDecimal(answer); 
			  } catch(NumberFormatException ex) {
				  log.debug("Number is not BigDecimal: " + answer);
			  }

			  try {
				  answerComplex = complexFormat.parse(answer);
			  } catch(MathParseException ex) {
				  log.debug("Number is not Complex: " + answer);
			  }

			  if (data.getAnswerText() != null) {  
				  Map map = validate(studentAnswerText);

				  if (answerNum != null) {
					  studentAnswerNum = (BigDecimal) map.get(ANSWER_TYPE_REAL);
					  matchresult = (studentAnswerNum != null && answerNum.compareTo(studentAnswerNum) == 0);
				  }
				  else if (answerComplex != null) {
					  studentAnswerComplex = (Complex) map.get(ANSWER_TYPE_COMPLEX);
					  matchresult = (studentAnswerComplex != null && answerComplex.equals(studentAnswerComplex));
				  }
			  }
		  }
	  }
	  return matchresult;
  }  
  
  
  public double getImageMapScore(ItemGradingData data, ItemDataIfc itemdata, Map publishedItemTextHash, Map publishedAnswerHash)
  {
	  // Final score must be... 
	  // IF NOT PARTIALCREDIT THEN 0 or total
	  // IF PARTIALCREDIT EACH PART ADDED. 
	  
	  
	  data.setIsCorrect(Boolean.FALSE);
	  double totalScore; 
	 
	 Iterator iter = publishedAnswerHash.keySet().iterator();
	 int answerNumber = 0;
	 while (iter.hasNext()){
		 Long answerId = Long.valueOf(iter.next().toString());
		 AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(answerId);
		 
		 if (answer.getItem().getItemId().equals(data.getPublishedItemId())) 
			 answerNumber=answerNumber+1;
		 	 
	 }
	 
	 double answerScore=itemdata.getScore();
	 	 
	 if (answerNumber!=0){
		 answerScore=answerScore/answerNumber;
	 }
	 
	 ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(data.getPublishedItemTextId());
	 
	 List answerArray = (List) itemTextIfc.getAnswerArray();
	 AnswerIfc answerIfc= (AnswerIfc) answerArray.get(0); 
	 
	 try{
		 String area = answerIfc.getText();
		 Integer areax1=Integer.valueOf(area.substring(area.indexOf("\"x1\":")+5,area.indexOf(",", area.indexOf("\"x1\":"))));
		 Integer areay1=Integer.valueOf(area.substring(area.indexOf("\"y1\":")+5,area.indexOf(",", area.indexOf("\"y1\":"))));
		 Integer areax2=Integer.valueOf(area.substring(area.indexOf("\"x2\":")+5,area.indexOf(",", area.indexOf("\"x2\":"))));
		 Integer areay2=Integer.valueOf(area.substring(area.indexOf("\"y2\":")+5,area.indexOf("}", area.indexOf("\"y2\":"))));
		 
		 String point = data.getAnswerText();
		 Integer pointx=Integer.valueOf(point.substring(point.indexOf("\"x\":")+4,point.indexOf(",", point.indexOf("\"x\":"))));
		 Integer pointy=Integer.valueOf(point.substring(point.indexOf("\"y\":")+4,point.indexOf("}", point.indexOf("\"y\":"))));
		
				 
		 if (((pointx>=areax1)&&(pointx<=areax2))&&((pointy>=areay1)&&(pointy<=areay2))) {
			 totalScore=answerScore;
			 data.setIsCorrect(Boolean.TRUE);
		 }else{
			 totalScore=0;
		 }
	}catch(Exception ex){
		 totalScore=0;
	 }
	 	  
    
    return totalScore;
  }
  

  /**
   * Validate a students numeric answer 
   * @param value answer to validate
   * @return a Map containing either Real or Complex answer keyed by {@link #ANSWER_TYPE_REAL} or {@link #ANSWER_TYPE_COMPLEX} 
   */
  public Map validate(String value) {
	  Map map = new HashMap();
	  if (StringUtils.isEmpty(value)) {
		  return map;
	  }
	  String trimmedValue = value.trim();
	  boolean isComplex = true;
	  boolean isRealNumber = true;

	  BigDecimal studentAnswerReal = null;
	  try {
		  studentAnswerReal = new BigDecimal(trimmedValue);
	  } catch (Exception e) {
		  isRealNumber = false;
	  }

	  // Test for complex number only if it is not a BigDecimal
	  Complex studentAnswerComplex = null;
	  if (!isRealNumber) {
		  try {
			  DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.US);
			  df.setGroupingUsed(false);

			  // Numerical format ###.## (decimal symbol is the point)
			  ComplexFormat complexFormat = new ComplexFormat(df);
			  studentAnswerComplex = complexFormat.parse(trimmedValue);

			  // Only checks for complex numbers, not real numbers
			  if (studentAnswerComplex.getImaginary() == 0) {
				  isComplex = false;
			  }
		  } catch (Exception e) {
			  isComplex = false;
		  }
	  }

	  Boolean isValid = isComplex || isRealNumber;
	  if (!isValid) {
		  throw new FinFormatException("Not a valid FIN Input. studentanswer=" + trimmedValue);
	  }

	  if (isRealNumber) {
		  map.put(ANSWER_TYPE_REAL, studentAnswerReal);
	  }
	  else if (isComplex) {
		  map.put(ANSWER_TYPE_COMPLEX, studentAnswerComplex);
	  }

	  return map;
	}

  /**
   * EMI score processing
   * 
   */
	private double getEMIScore(ItemGradingData itemGrading, Long itemId,
			Map totalItems, Map<Long, Map<Long, Set<EMIScore>>> emiScoresMap,
			Map publishedItemTextHash, Map publishedAnswerHash) {

		log.debug("getEMIScore( " + itemGrading +", " + itemId);
		double autoScore;
		if (!totalItems.containsKey(itemId)) {
			totalItems.put(itemId, new HashMap());
			emiScoresMap.put(itemId, new HashMap<>());
		}

		autoScore = getAnswerScore(itemGrading, publishedAnswerHash);
		AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(itemGrading
				.getPublishedAnswerId());
		if (answer == null) {
			//its possible we have an orphaned object ...
			log.warn("could not find answer: " + itemGrading
					.getPublishedAnswerId() + ", for item " + itemGrading.getItemGradingId());
			return 0.0;
		}
		Long itemTextId = itemGrading.getPublishedItemTextId();

		// update the fibEmiAnswersMap so we can keep track
		// of how many answers were given
		Map<Long, Set<EMIScore>> emiItemScoresMap = emiScoresMap.get(itemId);
		// place the answer scores in a sorted set.
		// so now we can mark the correct ones and discount the extra incorrect
		// ones.
		Set<EMIScore> scores;
		if (emiItemScoresMap.containsKey(itemTextId)) {
			scores = emiItemScoresMap.get(itemTextId);
		} else {
			scores = new TreeSet<>();
			emiItemScoresMap.put(itemTextId, scores);
		}
		scores.add(new EMIScore(itemId, itemTextId, itemGrading
				.getPublishedAnswerId(), answer.getIsCorrect(), autoScore));

		ItemTextIfc itemText = (ItemTextIfc) publishedItemTextHash.get(itemTextId);
		int numberCorrectAnswers = itemText.getEmiCorrectOptionLabels()
				.length();
		Integer requiredCount = itemText.getRequiredOptionsCount();
		// re-calculate the scores over for the whole item
		autoScore = 0.0;
		int c = 0;
		for (EMIScore s : scores) {
			c++;
			s.effectiveScore = 0.0;
			if (c <= numberCorrectAnswers && c <= requiredCount) {
				// if correct and in count then add score
				s.effectiveScore = s.correct ? s.score : 0.0;
			} else if (c > numberCorrectAnswers) {
				// if incorrect and over count add discount
				s.effectiveScore = !s.correct ? s.score : 0.0;
			}
			if (autoScore + s.effectiveScore < 0.0) {
				// the current item tipped it to negative,
				// we cannot do this, so add zero
				s.effectiveScore = 0.0;
			}
			autoScore += s.effectiveScore;
		}

		// override score
		if (itemGrading.getOverrideScore() != null)
			autoScore += itemGrading.getOverrideScore();

		Map totalItemTextScores = (Map) totalItems.get(itemId);
		totalItemTextScores.put(itemTextId, autoScore);
		return autoScore;
	}
  /**
   * CALCULATED_QUESTION
   * Returns a double score value for the ItemGrading element being scored for a Calculated Question
   * 
   * @param calcQuestionAnswerSequence the order of answers in the list
   * @return score for the item.
   */
  public double getCalcQScore(ItemGradingData data,  ItemDataIfc itemdata, Map<Integer, String> calculatedAnswersMap, int calcQuestionAnswerSequence)
  {
	  double totalScore = (double) 0;
	  
	  if (data.getAnswerText() == null) return totalScore; // zero for blank
	  
	  if (!calculatedAnswersMap.containsKey(calcQuestionAnswerSequence)) {
		  return totalScore;
	  }
	  // this variable should look something like this "42.1|2,2"
	  String allAnswerText = calculatedAnswersMap.get(calcQuestionAnswerSequence);
	  
	  // NOTE: this correctAnswer will already have been trimmed to the appropriate number of decimals
	  BigDecimal correctAnswer = new BigDecimal(getAnswerExpression(allAnswerText));
	  
	  // Determine if the acceptable variance is a constant or a % of the answer
	  String varianceString = allAnswerText.substring(allAnswerText.lastIndexOf("|")+1, allAnswerText.lastIndexOf(","));
	  BigDecimal acceptableVariance;
	  if (varianceString.contains("%")){
		  double percentage = Double.valueOf(varianceString.substring(0, varianceString.indexOf("%")));
		  acceptableVariance = correctAnswer.multiply( new BigDecimal(percentage / 100) );
	  }
	  else {
		  acceptableVariance = new BigDecimal(varianceString);
	  }
	  
	  String userAnswerString = data.getAnswerText().replaceAll(",|[\\s]+", "");
	  BigDecimal userAnswer;
	  try {
		  userAnswer = new BigDecimal(userAnswerString);
	  } catch(NumberFormatException nfe) {
		  return totalScore; // zero because it's not even a number!
	  }
	  
	  // this compares the correctAnswer against the userAnsewr
	  BigDecimal answerDiff = (correctAnswer.subtract(userAnswer));
	  boolean closeEnough = (answerDiff.abs().compareTo(acceptableVariance.abs()) <= 0);
	  if (closeEnough){
		  totalScore += itemdata.getScore(); 
	  }	
	  return totalScore;
	  
  }

  public boolean getCalcQResult(ItemGradingData data,  ItemDataIfc itemdata, Map<Integer, String> calculatedAnswersMap, int calcQuestionAnswerSequence)
  {
	  boolean result = false;

	  if (data.getAnswerText() == null) return result;

	  if (!calculatedAnswersMap.containsKey(calcQuestionAnswerSequence)) {
		  return result;
	  }
	  // this variable should look something like this "42.1|2,2"
	  String allAnswerText = calculatedAnswersMap.get(calcQuestionAnswerSequence);

	  // NOTE: this correctAnswer will already have been trimmed to the appropriate number of decimals
	  BigDecimal correctAnswer = new BigDecimal(getAnswerExpression(allAnswerText));

	  // Determine if the acceptable variance is a constant or a % of the answer
	  String varianceString = allAnswerText.substring(allAnswerText.lastIndexOf("|")+1, allAnswerText.lastIndexOf(","));
	  BigDecimal acceptableVariance;
	  if (varianceString.contains("%")){
		  double percentage = Double.valueOf(varianceString.substring(0, varianceString.indexOf("%")));
		  acceptableVariance = correctAnswer.multiply( new BigDecimal(percentage / 100) );
	  }
	  else {
		  acceptableVariance = new BigDecimal(varianceString);
	  }

	  String userAnswerString = data.getAnswerText().replaceAll(",|[\\s]+", "");
	  BigDecimal userAnswer;
	  try {
		  userAnswer = new BigDecimal(userAnswerString);
	  } catch(NumberFormatException nfe) {
		  return result;
	  }

	  // this compares the correctAnswer against the userAnsewr
	  BigDecimal answerDiff = (correctAnswer.subtract(userAnswer));
	  boolean closeEnough = (answerDiff.abs().compareTo(acceptableVariance.abs()) <= 0);
	  if (closeEnough){
		  result = true;
	  }
	  return result;

  }

  public double getTotalCorrectScore(ItemGradingData data, Map publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null)
      return (double) 0;
    return answer.getScore();
  }

  private void setIsLate(AssessmentGradingData data, PublishedAssessmentIfc pub){
    Boolean isLate = Boolean.FALSE;
    AssessmentAccessControlIfc a = pub.getAssessmentAccessControl();
    if (a.getDueDate() != null && a.getDueDate().before(new Date())) {
      isLate = Boolean.TRUE;
    }

    if (isLate) {
      ExtendedTimeDeliveryService assessmentExtended = new ExtendedTimeDeliveryService((PublishedAssessmentFacade) pub, data.getAgentId());
      if (assessmentExtended.hasExtendedTime() && assessmentExtended.getDueDate() != null && assessmentExtended.getDueDate().after(new Date())) {
          isLate = Boolean.FALSE;
      }
    }

    data.setIsLate(isLate);

    if (data.getForGrade())
      data.setStatus(1);

    data.setTotalOverrideScore(Double.valueOf(0));
  }

  public void deleteAll(Collection c)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().deleteAll(c);
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
  }

  /* Note:
   * assessmentGrading contains set of itemGrading that are not saved in the DB yet
   */
  public void updateAssessmentGradingScore(AssessmentGradingData adata, PublishedAssessmentIfc pub, String newComment, String oldComment){
    try {
      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      double totalAutoScore = 0;
      double totalOverrideScore = adata.getTotalOverrideScore();
      while (iter.hasNext()){
        ItemGradingData i = (ItemGradingData)iter.next();
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore();
        }
        double oldAutoScore = adata.getTotalAutoScore();
        double scoreDifference = totalAutoScore - oldAutoScore;
        adata.setTotalAutoScore(totalAutoScore);
        if (Double.compare((totalAutoScore+totalOverrideScore),Double.valueOf("0"))<0){
        	adata.setFinalScore(Double.valueOf("0"));
        }else{
        	adata.setFinalScore(totalAutoScore+totalOverrideScore);
        }
        saveOrUpdateAssessmentGrading(adata);
        if (scoreDifference != 0 || !newComment.equals(oldComment)){
          notifyGradebookByScoringType(adata, pub);
        }
     } catch (GradebookServiceException ge) {
       log.error(ge.getMessage(), ge);
       throw ge;
     } catch (Exception e) {
       log.error(e.getMessage(), e);
       throw new RuntimeException(e);
     }
  }

  public void saveOrUpdateAll(Collection<ItemGradingData> c)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAll(c);
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
  }

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(String id){
    PublishedAssessmentIfc pub = null;
    try {
      pub = PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().getPublishedAssessmentByAssessmentGradingId(Long.valueOf(id));
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
    return pub;
  }
  
  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(String publishedItemId){
	    PublishedAssessmentIfc pub = null;
	    try {
	      pub = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getPublishedAssessmentByPublishedItemId(Long.valueOf(publishedItemId));
	    } catch (Exception e) {
            log.error(e.getMessage(), e);
	    }
	    return pub;
	  }
  
  public List<Integer> getLastItemGradingDataPosition(Long assessmentGradingId, String agentId) {
	  	List<Integer> results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getLastItemGradingDataPosition(assessmentGradingId, agentId);
	    } catch (Exception e) {
            log.error(e.getMessage(), e);
	    }
	    return results;
	  }
  
  public List getItemGradingIds(Long assessmentGradingId) {
	    List results = null;
	    try {
	         results = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().getItemGradingIds(assessmentGradingId);
	    } catch (Exception e) {
            log.error(e.getMessage(), e);
	    }
	    return results;
  }

  public List getPublishedItemIds(Long assessmentGradingId) {
	  	List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getPublishedItemIds(assessmentGradingId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return results;
	  }
  
  public Set<PublishedItemData> getItemSet(Long publishedAssessmentId, Long sectionId) {
	  	Set<PublishedItemData> results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getItemSet(publishedAssessmentId, sectionId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return results;
  }
  
  public Long getTypeId(Long itemGradingId) {
	  	Long typeId = null;
	    try {
	    	typeId = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getTypeId(itemGradingId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return typeId;
  }
  
	public boolean fibmatch(final String rawAnswer, final String rawInput, final boolean casesensitive, final boolean ignorespaces) {
		log.debug("Fill in the blank match called with inputs:\nAnswer={}\nInput={}\nCase Sensitive={}\nIgnore Spaces={}", rawAnswer, rawInput, casesensitive, ignorespaces);

		try {
		 // User on Mac will input &uuml; instead of ü
		 String answer = StringEscapeUtils.unescapeHtml4(rawAnswer);
		 String input = StringEscapeUtils.unescapeHtml4(rawInput);

		 // User on Mac will use "smart" curly apostrophes by default!!
		 answer = answer.replaceAll("[\u2018\u2019]", "'");
		 input = input.replaceAll("[\u2018\u2019]", "'");

		 // User on Mac will use “smart” quotes by default!
		 answer = answer.replaceAll("[\u201C\u201D]", "\"");
		 input = input.replaceAll("[\u201C\u201D]", "\"");

		 // Always trim trailing spaces
		 answer = answer.trim();
		 input = input.trim();

		 // Trim interior space including non-breaking spaces if instructor selects option
		 if (ignorespaces) {
			 answer = answer.replaceAll("\\p{javaSpaceChar}", "");
			 input = input.replaceAll("\\p{javaSpaceChar}", "");
		 }
 		 StringBuilder regex_quotebuf = new StringBuilder();

		 String[] oneblank = answer.replaceAll("\\*", "|*|").split("\\|");
		 for (String str : oneblank) {
			 if ("*".equals(str)) {
				 regex_quotebuf.append(".+");
			 }
			 else {
				 regex_quotebuf.append(Pattern.quote(str));
			 }
		 }

		 String regex_quote = regex_quotebuf.toString();
		 Pattern p;
		 if (casesensitive){
		 p = Pattern.compile(regex_quote );
		 }
		 else {
		 p = Pattern.compile(regex_quote,Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		 }
		 Matcher m = p.matcher(input);
		 boolean result = m.matches();
 		 return result;
		}
		catch (Exception e){
			log.warn("Fill in the blank match failed returning false, {}", e.toString());
			return false;
		}
	}

  public List getAllAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
	  	List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getAllAssessmentGradingByAgentId(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return results;
  }
  
  
  public List<ItemGradingData> getAllItemGradingDataForItemInGrading(Long assesmentGradingId, Long publihsedItemId) {
	  List<ItemGradingData> results = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().getAllItemGradingDataForItemInGrading(assesmentGradingId, publihsedItemId);
	  return results;
  }
  
  public Map<Long, Map<String, Integer>> getSiteSubmissionCountHash(String siteId) {
	    Map<Long, Map<String, Integer>> results = new HashMap<>();
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteSubmissionCountHash(siteId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return results;
  }  
  
  public Map<Long, Map<String, Long>> getSiteInProgressCountHash(final String siteId) {
        Map<Long, Map<String, Long>> results = new HashMap<>();
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteInProgressCountHash(siteId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return results;
  }

  public int getActualNumberRetake(Long publishedAssessmentId, String agentIdString) {
	  	int actualNumberReatke = 0;
	    try {
	    	actualNumberReatke = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getActualNumberRetake(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return actualNumberReatke;
  }
  
  public Map<Long, Integer> getActualNumberRetakeHash(String agentIdString) {
	    Map<Long, Integer> actualNumberRetakeHash = new HashMap<>();
	    try {
	    	actualNumberRetakeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getActualNumberRetakeHash(agentIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return actualNumberRetakeHash;
  }
    
  public Map<Long, Map<String, Long>> getSiteActualNumberRetakeHash(String siteIdString) {
        Map<Long, Map<String, Long>> numberRetakeHash = new HashMap<>();
	    try {
	    	numberRetakeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteActualNumberRetakeHash(siteIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return numberRetakeHash;
  }
    
  public List getStudentGradingSummaryData(Long publishedAssessmentId, String agentIdString) {
	  List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getStudentGradingSummaryData(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return results;
  }
  

  public int getNumberRetake(Long publishedAssessmentId, String agentIdString) {
	  int numberRetake = 0;
	    try {
	    	numberRetake = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getNumberRetake(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return numberRetake;
  }
  
  public Map<Long, StudentGradingSummaryData> getNumberRetakeHash(String agentIdString) {
	    Map<Long, StudentGradingSummaryData> numberRetakeHash = new HashMap<>();
	    try {
	    	numberRetakeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getNumberRetakeHash(agentIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return numberRetakeHash;
  }
  
  public Map<Long, Map<String, Integer>> getSiteNumberRetakeHash(String siteIdString) {
	    Map<Long, Map<String, Integer>> siteActualNumberRetakeList = new HashMap();
	    try {
	    	siteActualNumberRetakeList = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteNumberRetakeHash(siteIdString);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return siteActualNumberRetakeList;
  }
    
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
	    try {
	    	PersistenceService.getInstance().getAssessmentGradingFacadeQueries().saveStudentGradingSummaryData(studentGradingSummaryData);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
  }
  

  public int getLateSubmissionsNumberByAgentId(Long publishedAssessmentId, String agentIdString, Date dueDate) {
	  int numberRetake = 0;
	    try {
	    	numberRetake = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getLateSubmissionsNumberByAgentId(publishedAssessmentId, agentIdString, dueDate);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return numberRetake;
  }
  
  /**
   * 
   * @param publishedAssessmentId
   * @param anonymous
   * @param audioMessage
   * @param fileUploadMessage
   * @param noSubmissionMessage
   * @param showPartAndTotalScoreSpreadsheetColumns
   * @param poolString
   * @param partString
   * @param questionString
   * @param textString
   * @param responseString
   * @param pointsString
   * @param rationaleString
   * @param itemGradingCommentsString
   * @param useridMap
   * @param responseCommentString
   * @param isOneSelectionType
   * @return a list of responses or null if there are none
   */
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map useridMap, String responseCommentString, boolean isOneSelectionType) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getExportResponsesData(publishedAssessmentId, anonymous,audioMessage, fileUploadMessage, noSubmissionMessage, showPartAndTotalScoreSpreadsheetColumns, poolString, partString, questionString, textString, responseString, pointsString, rationaleString, itemGradingCommentsString, useridMap, responseCommentString, isOneSelectionType);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return list;
  }

  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map useridMap, String responseCommentString) {
    return this.getExportResponsesData(publishedAssessmentId, anonymous, audioMessage, fileUploadMessage, noSubmissionMessage, showPartAndTotalScoreSpreadsheetColumns, poolString, partString, questionString, textString, responseString, pointsString, rationaleString, itemGradingCommentsString, useridMap, responseCommentString, false);
  }
  
  private void removeUnsubmittedAssessmentGradingData(AssessmentGradingData data){
	  try {
	      PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().removeUnsubmittedAssessmentGradingData(data);
	    } catch (Exception e) {
	      log.error("Exception thrown from removeUnsubmittedAssessmentGradingData" + e.getMessage());
	    }
  }
  
  public void removeAssessmentGradingData(AssessmentGradingData data) {
	  try {
		  PersistenceService.getInstance().getAssessmentGradingFacadeQueries().removeAssessmentGradingData(data);
	  } catch (Exception e) {
		  log.error("Exception thrown from removeAssessmentGradingData", e);
	  }
  }

  public boolean getHasGradingData(Long publishedAssessmentId) {
	  boolean hasGradingData = false;
	    try {
	    	hasGradingData = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getHasGradingData(publishedAssessmentId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return hasGradingData;
  }


  /**
   * CALCULATED_QUESTION
   * @param itemGrading
   * @param item
   * @return map of calc answers
   */
  private Map<Integer, String> getCalculatedAnswersMap(ItemGradingData itemGrading, ItemDataIfc item, int calcQuestionAnswerSequence ) {
      // return value from extractCalcQAnswersArray is not used, calculatedAnswersMap is populated by this call
      if (calcQuestionAnswerSequence == 1) {
          extractCalcQAnswersArray(answersMap, answersMapValues, globalanswersMapValues, mainvariablesWithValues, item, itemGrading.getAssessmentGradingId(), itemGrading.getAgentId());
      }
      return answersMap;
  }

  /**
   * extractCalculations() is a utility function for Calculated Questions.  It takes
   * one parameter, which is a block of text, and looks for any calculations
   * that are encoded in the text.  A calculations is enclosed in [[ ]].
   * <p>For example, if the passed parameter is <code>{a} + {b} = {{c}}, [[{a}+{b}]]</code>, 
   * the resulting list would contain one entry: a string of "{a}+{b}"
   * <p>Formulas must contain at least one variable OR parens OR calculation symbol (*-+/)
   * @param text contents to be searched
   * @return a list of matching calculations.  If no calculations are found, the 
   * list will be empty.
   */
  public List<String> extractCalculations(String text) {
      text = text.replaceAll("\\[\\[", "" + CALCULATION_AUX_OPEN + CALCULATION_AUX_OPEN ).replaceAll("\\]\\](?!\\])", "" + CALCULATION_AUX_CLOSE + CALCULATION_AUX_CLOSE);
      List<String> calculations = extractCalculatedQuestionKeyFromItemText(text, CALCQ_CALCULATION_PATTERN);
      for (Iterator<String> iterator = calculations.iterator(); iterator.hasNext();) {
        String calc = iterator.next();
        if (!StringUtils.containsAny(calc, "{}()+-*/")) {
            //checking global variable pattern
            if (!CALCQ_GLOBAL_VARIABLE_PATTERN.matcher(calc).find()) {
                iterator.remove();
            }
        }
      }
      return calculations;
  }

  /**
   * extractFormulas() is a utility function for Calculated Questions.  It takes
   * one parameter, which is a block of text, and looks for any formula names
   * that are encoded in the text.  A formula name is enclosed in {{ }}.  The 
   * formula itself is encoded elsewhere.
   * <p>For example, if the passed parameter is <code>{a} + {b} = {{c}}</code>, 
   * the resulting list would contain one entry: a string of "c"
   * <p>Formulas must begin with an alpha, but subsequent character can be 
   * alpha-numeric
   * @param text contents to be searched
   * @return a list of matching formula names.  If no formulas are found, the 
   * list will be empty.
   */
  public List<String> extractFormulas(String text) {
      return extractCalculatedQuestionKeyFromItemText(text, CALCQ_FORMULA_PATTERN);
  }

  /**
   * extractVariables() is a utility function for Calculated Questions.  It 
   * takes one parameter, which is a block of text, and looks for any variable 
   * names that are encoded in the text.  A variable name is enclosed in { }.  
   * The values of the variable are encoded elsewhere.
   * <p>For example, if the passed parameter is <code>{a} + {b} = {{c}}</code>, 
   * the resulting list would contain two entries: strings of "a" and "b"
   * <p>Variables must begin with an alpha, but subsequent character can be 
   * alpha-numeric.
   * <p>Note - a formula, encoded as {{ }}, will not be mistaken for a variable.
   * @param text content to be searched
   * @return a list of matching variable names.  If no variables are found, the 
   * list will be empty
   */
  public List<String> extractVariables(String text) {
    return extractCalculatedQuestionKeyFromItemText(text, CALCQ_ANSWER_PATTERN);
  }

  /**
   * extractGlobalVariables() is a utility function for Calculated Questions.  It 
   * takes one parameter, which is a block of text, and looks for any variable 
   * names that are encoded in the text.  A global variable name is enclosed in @ @.  
   * The values of the variable are encoded elsewhere.
   * <p>For example, if the passed parameter is <code>[[@a@ + @b@]] = {{c}}</code>, 
   * the resulting list would contain two entries: strings of "a" and "b"
   * <p>Global Variables must begin with an alpha, but subsequent character can be 
   * alpha-numeric.
   * <p>Note - a formula, encoded as {{ }}, will not be mistaken for a variable.
   * @param text content to be searched
   * @return a list of matching variable names. If no global variables are found, the 
   * list will be empty
   */
  public List<String> extractGlobalVariables(String text) {
    return extractCalculatedQuestionKeyFromItemText(text, CALCQ_GLOBAL_VARIABLE_PATTERN);
  }

  /**
   * extractCalculatedQuestionKeyFromItemText() is a utility function for Calculated Questions.  It 
   * takes a block of item text, and uses a pattern to looks for keys 
   * that are encoded in the text.
   * @param itemText content to be searched
   * @param identifierPattern pattern to use to do the search
   * @return a list of matching key values OR empty if none are found
   */
  private List<String> extractCalculatedQuestionKeyFromItemText(String itemText, Pattern identifierPattern) {
      List<String> keys = new ArrayList<>();
      if (itemText != null && itemText.trim().length() > 0) {
          Matcher keyMatcher = identifierPattern.matcher(itemText);
          while (keyMatcher.find()) {
              String match = keyMatcher.group(1);
              keys.add(match);
              /*
              // first character before matching group
              int start = keyMatcher.start(1) - 2;
              // first character after matching group
              int end = keyMatcher.end(1) + 1; // first character after the matching group
              // if matching group is wrapped by {}, it's not what we are looking for (like another key or just some text)
              if (start < 0 || end >= itemText.length() || itemText.charAt(start) != '{' || itemText.charAt(end) != '}') {
                  keys.add(match);
              }*/
          }
      }
      return keys;
  }

  /**
   * CALCULATED_QUESTION
   * @param item the item which contains the formula
   * @param formulaName the name of the formula
   * @return the actual formula that matches this formula name OR "" (empty string) if it is not found
   */
  private String replaceFormulaNameWithFormula(ItemDataIfc item, String formulaName) {
      String result = "";
      @SuppressWarnings("unchecked")
      List<ItemTextIfc> items = item.getItemTextArray();
      for (ItemTextIfc itemText : items) {
          if (itemText.getText().equals(formulaName)) {
              @SuppressWarnings("unchecked")
              List<AnswerIfc> answers = itemText.getAnswerArray();
              for (AnswerIfc answer : answers) {
                  if (itemText.getSequence().equals(answer.getSequence())) {
                      result = answer.getText();
                      break;
                  }
              }
          }
      }
      return result;
  }

  /**
   * CALCULATED_QUESTION
   * Takes the instructions and breaks it into segments, based on the location 
   * of formula names.  One formula would give two segments, two formulas gives
   * three segments, etc.
   * <p>Note - in this context, it would probably be easier if any variable value
   * substitutions have occurred before the breakup is done; otherwise,
   * each segment will need to have substitutions done.
   * @param instructions string to be broken up
   * @return the original string, broken up based on the formula name delimiters
   */
  protected List<String> extractInstructionSegments(String instructions) {
      List<String> segments = new ArrayList<>();
      if (instructions != null && instructions.length() > 0) {
          String[] results = CALCQ_FORMULA_SPLIT_PATTERN.split(instructions); // only works because all variables and calculations are already replaced
          segments.addAll(Arrays.asList(results));
          if (segments.size() == 1) {
              // add in the trailing segment
              segments.add("");
          }
          /*
          final String FUNCTION_BEGIN = "{{";
          final String FUNCTION_END = "}}";
          while (instructions.indexOf(FUNCTION_BEGIN) > -1 && instructions.indexOf(FUNCTION_END) > -1) {
              String segment = instructions.substring(0, instructions.indexOf(FUNCTION_BEGIN));
              instructions = instructions.substring(instructions.indexOf(FUNCTION_END) + FUNCTION_END.length());
              segments.add(segment);
          }
          segments.add(instructions);
           */
      }
      return segments;
  }

  /**
   * CALCULATED_QUESTION
   * Takes the feedback and breaks it into segments, based on the location
   * of formula names. One formula would give two segments, two formulas gives
   * three segments, etc.
   * <p>Note - in this context, it would probably be easier if any variable value
   * substitutions have occurred before the breakup is done; otherwise,
   * each segment will need to have substitutions done.
   * @param feedback string to be broken up
   * @return the original string, broken up based on the formula name delimiters
   */
  protected List<String> extractFeedbackSegments(String feedback) {
      List<String> segments = new ArrayList<>();
      Matcher matcher = CALCQ_FORMULA_SPLIT_PATTERN.matcher(feedback);
      int lastIndex = 0;
      while (matcher.find()) {
          String segment = feedback.substring(lastIndex, matcher.start());
          segments.add(segment);
          segments.add(matcher.group().replaceAll(CALCQ_SOLUTION_EXPRESSION, ""));
          lastIndex = matcher.end();
      }
      segments.add(feedback.substring(lastIndex));

      return segments;
  }

  
  /**
   * CALCULATED_QUESTION
   * toScientificNotation() Takes a string representation of a number and returns
   * a string representation of that number, in scientific notation.
   * Numbers like 100, 0.01 will not be formatted (see values of MAX_THRESHOLD and MIN_THRESHOLD)
   * @param numberStr
   * @param decimalPlaces
   * @return processed number string
   */
  public String toScientificNotation(String numberStr,int decimalPlaces){
	  
	  BigDecimal bdx = new BigDecimal(numberStr);
	  bdx.setScale(decimalPlaces,RoundingMode.HALF_UP);	
	  
	  NumberFormat formatter;
	  
	  if ((bdx.abs().compareTo(DEFAULT_MAX_THRESHOLD) >= 0 || bdx.abs().compareTo(DEFAULT_MIN_THRESHOLD) <= 0
        || numberStr.contains("e") || numberStr.contains("E") ) 
	    && bdx.doubleValue() != 0) {
		  formatter = new DecimalFormat(FORMAT_MASK);
	  } else {
		  formatter = new DecimalFormat("0");
	  }	  
	  
	  formatter.setRoundingMode(RoundingMode.HALF_UP);	  
	  formatter.setMaximumFractionDigits(decimalPlaces);
	  
	  String formattedNumber = formatter.format(bdx);

	  return formattedNumber.replace(",",".");
  }

  /**
   * CALCULATED_QUESTION
   * toScientificNotation() Takes a string representation of a number and returns
   * a string representation of that number, in scientific notation.
   * Numbers like 100, 0.01 will not be formatted (see values of MAX_THRESHOLD and MIN_THRESHOLD)
   * @param numberStr
   * @param decimalPlaces
   * @param stringCalculate
   * @return processed number string
   */
  public String toScientificNotation(String numberStr, String stringCalculate, int decimalPlaces){

         BigDecimal bdx = new BigDecimal(numberStr);
         bdx.setScale(decimalPlaces,RoundingMode.HALF_UP);

         NumberFormat formatter;

         if ((bdx.abs().compareTo(DEFAULT_MAX_THRESHOLD) >= 0 || bdx.abs().compareTo(DEFAULT_MIN_THRESHOLD) <= 0
        || numberStr.contains("e") || numberStr.contains("E") )
           && bdx.doubleValue() != 0) {
                 formatter = new DecimalFormat(FORMAT_MASK);
         } else {
             if (!numberStr.equals(stringCalculate) && ("0".equals(stringCalculate))) {
                 formatter = new DecimalFormat(FORMAT_MASK);
             } else {
                 formatter = new DecimalFormat("0");
             }
         }

         formatter.setRoundingMode(RoundingMode.HALF_UP);
         formatter.setMaximumFractionDigits(decimalPlaces);

         String formattedNumber = formatter.format(bdx);

         return formattedNumber.replace(",",".");
  }
  
  /**
   * CALCULATED_QUESTION
   * applyPrecisionToNumberString() takes a string representation of a number and returns
   * a string representation of that number, rounded to the specified number of
   * decimal places, including trimming decimal places if needed.
   * Will also throw away the extra trailing zeros as well as removing a trailing decimal point.
   * @param numberStr
   * @param decimalPlaces
   * @return processed number string (will never be null or empty string)
   */
  public String applyPrecisionToNumberString(String numberStr, int decimalPlaces) {
      // Trim off excess decimal points based on decimalPlaces value
      BigDecimal bd = new BigDecimal(numberStr);
      bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_EVEN);

      String decimal = ".";
      // TODO handle localized decimal separator?
      //DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale);
      //char dec = dfs.getDecimalFormatSymbols().getDecimalSeparator();

      String displayAnswer = bd.toString();
      if (displayAnswer.length() > 2 && displayAnswer.contains(decimal)) {
          if (decimalPlaces == 0) { // Remove ".0" if decimalPlaces == 0
              displayAnswer = displayAnswer.replace(decimal+"0", "");
          } else {
              // trim away all the extra 0s from the end of the number
              if (displayAnswer.endsWith("0")) {
                  displayAnswer = StringUtils.stripEnd(displayAnswer, "0");
              }
              if (displayAnswer.endsWith(decimal)) {
                  displayAnswer = displayAnswer.substring(0, displayAnswer.length() - 1);
              }
          }
      }
      return displayAnswer;
  }

  /**
   * CALCULATED_QUESTION
   * calculateFormulaValues() evaluates all formulas referenced in the 
   * instructions.  For each formula name it finds, it retrieves the formula
   * for the name, substitutes the randomized value for the variables,
   * evaluates the formula to a real answer, and put the answer, along with
   * the needed precision and decimal places in the returning Map. 
   * @param variables a Map<String, String> of variables,  The key is the 
   * variable name, the value is the text representation, after randomization, 
   * of a number in the variable's defined range.
   * @param item The question itself, which is needed to provide additional 
   * information for called functions
   * @return a Map<Integer, String>.  the Integer is simply the sequence.  
   * Answers are returned in the order that the formulas are found.
   * The String is the result of the formula, encoded as (value)|(tolerance),(decimal places)
   * @throws Exception if either the formula expression fails to pass the 
   * Samigo expression parser, which should never happen as this is validated
   * when the question is saved, or if a divide by zero error occurs.
   */
  private ArrayList<Map> calculateFormulaValues(Map<String, String> variables, ItemDataIfc item) throws Exception {
      Map<Integer, String> values = new LinkedHashMap<>();
      LinkedHashMap<String, String> solutionVariables = new LinkedHashMap<>();
      LinkedHashMap<String, String> globalVariables = new LinkedHashMap<>();
      ArrayList<Map> maps = new ArrayList<Map>();
      String instructions = item.getInstruction();
      List<String> formulaNames = this.extractFormulas(instructions);
      // first adding from instructions
      List<String> globalVariableNames = this.extractGlobalVariables(instructions);
      // second adding from global variables addeButNotExtracted
      List<ItemTextIfc> items = item.getItemTextArray();
      for (ItemTextIfc itemText : items) {
          if(itemText.isAddedButNotExtracted()) {
              globalVariableNames.add(itemText.getText());
          }
      }

      for (int i = 0; i < globalVariableNames.size(); i++) {
          String globalVariableName = globalVariableNames.get(i);
          String longFormula = replaceFormulaNameWithFormula(item, globalVariableName);
          // remove "|0,0" from longFormula if present
          if (longFormula.endsWith(GradingService.GLOBAL_VAR_FORMAT_SUFFIX)) {
            longFormula = longFormula.substring(0, longFormula.length() - GradingService.GLOBAL_VAR_FORMAT_SUFFIX.length());
          }
          globalVariables.put(globalVariableName, longFormula);
      }

      for (int i = 0; i < formulaNames.size(); i++) {
          String formulaName = formulaNames.get(i);
          String longFormula = replaceFormulaNameWithFormula(item, formulaName); // {a}+{b}|0.1,1
          longFormula = defaultVarianceAndDecimal(longFormula); // sets defaults, in case tolerance or precision isn't set
          
          String formula = getAnswerExpression(longFormula); // returns just the formula
          String answerData = getAnswerData(longFormula); // returns just tolerance and precision
          int decimalPlaces = getAnswerDecimalPlaces(answerData);
          
          String substitutedFormula = replaceMappedVariablesWithNumbers(formula,variables);
          substitutedFormula = checkingEmptyGlobalVariables(substitutedFormula, variables, globalVariables);

          String formulaValue = processFormulaIntoValue(substitutedFormula, decimalPlaces);
          values.put(i + 1, formulaValue + answerData); // later answerData will be used for scoring
          solutionVariables.put(formulaName, formulaValue + answerData); // later answerData will be used for scoring
      }
      maps.add(values);
      maps.add(solutionVariables);
      maps.add(globalVariables);
      return maps;
  }

  /**
   * CALCULATED_QUESTION
   * This is a busy method. It does three things:
   * <br>1. It removes the answer expressions ie. {{x+y}} from the question text. This value is
   * <br>1. It removes the answer expressions ie. {{x+y}} from the question text and correct/incorrect feedback. These values are
   *    returned in the ArrayList texts. This format is necessary so that input boxes can be
   *    placed in the text where the {{..}}'s appear.
   * <br>2. It will call methods to swap out the defined variables with randomly generated values
   *    within the ranges defined by the user.
   *    within the ranges defined by the user, but before we check recursively if there are any global variable to change with its formula.
   * <br>3. It populates the HashMap answerList with the calculated answers in sequence. It will
   *    parse and calculate what each answer needs to be.
   * <p>Note: If a divide by zero occurs, we change the random values and try again. It gets limited chances to
   *    get valid values and then will return "infinity" as the answer.
   * @param answerList is cleared and filled with sequential answers to the question
   * @param answerListValues is cleared and filled with key answers to the question
   * @param globalanswersMapValues is cleared and filled with global variables to the question
   * @param mainvariablesWithValues is cleared and filled with the variables to the question
   * @return ArrayList of the pieces of text to display surrounding input boxes
   */
  public List<List<String>> extractCalcQAnswersArray(Map<Integer, String> answerList, Map<String, String> answerListValues, Map<String, String> globalanswersMapValues, Map<String, String> mainvariablesWithValues, ItemDataIfc item, Long gradingId, String agentId) {
      boolean hasErrors = true;
      Map<String, String> variableRangeMap = buildVariableRangeMap(item);
      List<String> instructionSegments = new ArrayList<>(0);
      List<String> correctFeedbackSegments = new ArrayList<>(0);
      List<String> incorrectFeedbackSegments = new ArrayList<>(0);

      answerList.clear();
      answerListValues.clear();
      globalanswersMapValues.clear();
      mainvariablesWithValues.clear();

      int attemptCount = 1;
      while (hasErrors && attemptCount <= MAX_ERROR_TRIES) {
          instructionSegments.clear();
          Map<String, String> variablesWithValues = determineRandomValuesForRanges(variableRangeMap,item.getItemId(), gradingId, agentId, attemptCount);
          mainvariablesWithValues.putAll(variablesWithValues);
          try {
              ArrayList<Map> evaluatedFormulas = calculateFormulaValues(variablesWithValues, item);
              answerList.putAll(evaluatedFormulas.get(0));
              answerListValues.putAll(evaluatedFormulas.get(1));
              globalanswersMapValues.putAll(evaluatedFormulas.get(2));
              // replace the variables in the text with values
              String instructions = item.getInstruction();
              String correctFeedback = item.getCorrectItemFeedback();
              String incorrectFeedback = item.getInCorrectItemFeedback();
              instructions = replaceMappedVariablesWithNumbers(instructions, variablesWithValues);
              // return instructions without any variable
              instructions = checkingEmptyGlobalVariables(instructions, variablesWithValues, globalanswersMapValues);
              String correctFeedbackNumbers = replaceMappedVariablesWithNumbers(correctFeedback, variablesWithValues);
              correctFeedbackNumbers = checkingEmptyGlobalVariables(correctFeedbackNumbers, variablesWithValues, globalanswersMapValues);
              String incorrectFeedbackNumbers = replaceMappedVariablesWithNumbers(incorrectFeedback, variablesWithValues);
              incorrectFeedbackNumbers = checkingEmptyGlobalVariables(incorrectFeedbackNumbers, variablesWithValues, globalanswersMapValues);
              // then replace the calculations with values (must happen AFTER the variable replacement)
              try {
                  instructions = replaceCalculationsWithValues(instructions, 5); // what decimal precision should we use here?
                  String correctFeedbackValue = replaceCalculationsWithValues(correctFeedbackNumbers, 5);
                  String incorrectFeedbackValue = replaceCalculationsWithValues(incorrectFeedbackNumbers, 5);
                  // if could not process the calculation into a result then throws IllegalStateException which will be caught below and cause the numbers to regenerate
                  // only pull out the segments if the formulas worked
                  instructionSegments = extractInstructionSegments(instructions);
                  correctFeedbackSegments = extractFeedbackSegments(correctFeedbackValue);
                  incorrectFeedbackSegments = extractFeedbackSegments(incorrectFeedbackValue);
                  item.updateFeedbackByType(PublishedItemFeedback.CORRECT_FEEDBACK, correctFeedback, correctFeedbackValue);
                  item.updateFeedbackByType(PublishedItemFeedback.INCORRECT_FEEDBACK, incorrectFeedback, incorrectFeedbackValue);
                  hasErrors = false;
              } catch (SamigoExpressionError e1) {
                  log.warn("Samigo calculated item ({}) calculation invalid: {}", item.getItemId(), e1.get());
                  attemptCount++;
              }
          } catch (Exception e) {
              // possible division by zero error
              attemptCount++;
          }
      }
      return List.of(instructionSegments, correctFeedbackSegments, incorrectFeedbackSegments);
  }

  /**
   * CALCULATED_QUESTION
   * Check if the solution is being used on the feedback. Replacing the solution {{w}} on feedback with numbers
   * @param answerListValues the key answers
   * @param item
   * @param texts ArrayList of the pieces of text
   */
  public void replaceSolutionOnFeedbackWithNumbers(LinkedHashMap<String, String> answerListValues, ItemDataIfc item, List<List<String>> texts) {
	  String correctFeedback = item.getCorrectItemFeedback();
	  String incorrectFeedback = item.getInCorrectItemFeedback();

	  for (int i=1; i<texts.size(); i++) {
		  List<String> parts = texts.get(i);
		  for (int j=0; j<parts.size(); j++) {
			  String map = answerListValues.get(parts.get(j));
			  if (map != null) {
				  String num = map.substring(0, map.lastIndexOf("|"));
				  parts.set(j, num);
			  }
		  }
		  if (i == 1) {
			  item.updateFeedbackByType(PublishedItemFeedback.CORRECT_FEEDBACK, correctFeedback, parts.stream().collect(Collectors.joining("")));
		  } else if (i == 2) {
			  item.updateFeedbackByType(PublishedItemFeedback.INCORRECT_FEEDBACK, incorrectFeedback, parts.stream().collect(Collectors.joining("")));
		  }
	  }
  }

  /**
   * CALCULATED_QUESTION
   * Recursive method for cleaning global variables and replace with numbers.
   * @param substitutedFormulaStr
   * @param variablesWithValues
   * @param globalAnswersMap
   * @return
   */
  public String checkingEmptyGlobalVariables (String substitutedFormulaStr, Map<String, String> variablesWithValues, Map<String, String> globalAnswersMap) {
      if (StringUtils.isEmpty(substitutedFormulaStr)) {
          return substitutedFormulaStr;
      }

      if (!globalAnswersMap.isEmpty()) {
          Matcher matcher = CALCQ_GLOBAL_VARIABLE_PATTERN.matcher(substitutedFormulaStr);
          while (matcher.find()) {
              substitutedFormulaStr = this.replaceGlobalVariablesWithFormulas(substitutedFormulaStr, globalAnswersMap);
              substitutedFormulaStr = checkingEmptyGlobalVariables(substitutedFormulaStr, variablesWithValues, globalAnswersMap);
              matcher = CALCQ_GLOBAL_VARIABLE_PATTERN.matcher(substitutedFormulaStr);
          }
      }
      substitutedFormulaStr = this.replaceMappedVariablesWithNumbers(substitutedFormulaStr, variablesWithValues);
      return substitutedFormulaStr;
  }

  /**
   * CALCULATED_QUESTION
   * This returns the decimal places value in the stored answer data.
   * @param allAnswerText
   * @return
   */
  private int getAnswerDecimalPlaces(String allAnswerText) {
	String answerData = getAnswerData(allAnswerText);
	int decimalPlaces = Integer.valueOf(answerData.substring(answerData.indexOf(",")+1, answerData.length()));
	return decimalPlaces;
  }

  /**
   * CALCULATED_QUESTION
   * This returns the "|2,2" (variance and decimal display) from the stored answer data.
   * @param allAnswerText
   * @return
   */
  private String getAnswerData(String allAnswerText) {
      String answerData = allAnswerText.substring(allAnswerText.lastIndexOf("|"), allAnswerText.length());
      return answerData;
  }

  /**
   * CALCULATED_QUESTION
   * This is just "(x+y)/z" or if values have been added to the expression it's the
   * calculated value as stored in the answer data.
   * @param allAnswerText
   * @return
   */
  private String getAnswerExpression(String allAnswerText) {
	  String answerExpression = allAnswerText.substring(0, allAnswerText.lastIndexOf("|"));
	  return answerExpression;
  }

  /**
   * CALCULATED_QUESTION
   * Default acceptable variance and decimalPlaces. An answer is defined by an expression
   * such as {x+y|1,2} if the variance and decimal places are left off. We have to default
   * them to something.
   */
  private String defaultVarianceAndDecimal(String allAnswerText) {
	  String defaultVariance = "0.001";
	  String defaultDecimal = "3";
	  
	  int lastIndex = allAnswerText.lastIndexOf("|");
	  if (lastIndex == -1) {
	      // No '|' found, check for ',' and add default values accordingly
	      if (!allAnswerText.contains(","))
	          allAnswerText = allAnswerText.concat("|"+defaultVariance+","+defaultDecimal);
	      else
	          allAnswerText = allAnswerText.replace(",","|"+defaultVariance+",");
	  } else {
	      // '|' found, check for ',' after the last '|'
	      String afterLastPipe = allAnswerText.substring(lastIndex + 1);
	      if (!afterLastPipe.contains(","))
	          allAnswerText = allAnswerText.concat(","+defaultDecimal);
	  }
	  
	  return allAnswerText;
  }
    
  /**
   * CALCULATED_QUESTION
   * Takes an answer string and checks for the value returned
   * is NaN or Infinity, indicating a Samigo formula parse error
   * Returns false if divide by zero is detected.
   */
  public boolean isAnswerValid(String answer) {
	  String INFINITY = "Infinity";
	  String NaN = "NaN";
	  if (answer.length() == 0) return false;
	  if (answer.equals(INFINITY)) return false;
	  return !answer.equals(NaN);
  }
  
  /**
   * CALCULATED_QUESTION
   * SAK-39922 - Support (or at least watch for support) for binary/unary calculated question (-1--1)
   * SAK-40942 - Error in calculated questions: the decimal representation .n or n. (where n is a number) does not work
   */
   private static String checkExpression(String expression) {
	   expression = CALCQ_ANSWER_AVOID_DOUBLE_MINUS.matcher(expression).replaceAll("+");
	   expression = CALCQ_ANSWER_AVOID_PLUS_MINUS.matcher(expression).replaceAll("-");
	   expression = CALCQ_FORMULA_ALLOW_POINT_NUMBER.matcher(expression).replaceAll("$10$2$3");
	   expression = CALCQ_FORMULA_ALLOW_NUMBER_POINT.matcher(expression).replaceAll("$1$3");
  
	   return expression;
  }
  
  /**
   * CALCULATED_QUESTION
   * replaceMappedVariablesWithNumbers() takes a string and substitutes any variable
   * names found with the value of the variable.  Variables look like {a}, the name of 
   * that variable is "a", and the value of that variable is in variablesWithValues
   * <p>Note - this function comprehends syntax like "5{x}".  If "x" is 37, the
   * result would be "5*37"
   * @param expression - the string being substituted into
   * @param variables - Map key is the variable name, value is what will be 
   * substituted into the expression.
   * @return a string with values substituted.  If answerExpression is null, 
   * returns a blank string (i.e "").  If variablesWithValues is null, returns
   * the original answerExpression 
   */
  public String replaceMappedVariablesWithNumbers(String expression, Map<String, String> variables) {
      if (StringUtils.isEmpty(expression)) {
          return expression;
      }
      
      if (variables == null) {
          variables = new HashMap<>();
      }

      for (Map.Entry<String, String> entry : variables.entrySet()) {
          String name = "{" + entry.getKey() + "}";
          String value = entry.getValue();

          // not doing string replace or replaceAll because the value being
          // substituted can change for each occurrence of the variable.
          int index = expression.indexOf(name);
          while (index > -1) {
              String prefix = expression.substring(0, index);
              String suffix = expression.substring(index + name.length());

              String replacementValue = value;

              // If replacement is a negative number, wrap in parentheses to avoid confused operations
              // For example, 3 {y} with -10 should be 3*-10= -30 not 3 -10 = -7
              if (value.charAt(0) == '-' && Character.isDigit(value.charAt(1))) {
                  replacementValue = ParserSymbol.LEFT_PARENTHESES_STR + value + ParserSymbol.RIGHT_PARENTHESES_STR;
              }

              // if last character of prefix is a number or the edge of parenthesis, multiply by the variable
              // if x = 37, 5{x} -> 5*37
              // if x = 37 (5+2){x} -> (5+2)*37 (prefix is (5+2)
              if (prefix.length() > 0 && (Character.isDigit(prefix.charAt(prefix.length() - 1)) || prefix.charAt(prefix.length() - 1) == ')')) {
                  replacementValue = "*" + replacementValue;
              }

              // if first character of suffix is a number or the edge of parenthesis, multiply by the variable
              // if x = 37, {x}5 -> 37*5
              // if x = 37, {x}(5+2) -> 37*(5+2) (suffix is (5+2)
              if (suffix.length() > 0 && (Character.isDigit(suffix.charAt(0)) || suffix.charAt(0) == '(')) {
                  replacementValue = replacementValue + "*";
              }

              // perform substitution, then look for the next instance of current variable
              expression = prefix + replacementValue + suffix;
              index = expression.indexOf(name);
          }
      }
      return expression;
  }

  /**
   * CALCULATED_QUESTION
   * replaceGlobalVariablesWithFormulas() takes a string and substitutes any global variable
   * names found with the value of the variable. Global variables look like @a@, the name of
   * that global variable is "a", and the value of that variable is in globalVariables
   * @param expression - the string being substituted into
   * @param globalVariables - Map key is the global variable name, value is what will be
   * substituted into the expression.
   * @return a string with values substituted. If expression is null,
   * returns a blank string (i.e ""). If globalVariables is null, returns
   * the original expression 
   */
  public String replaceGlobalVariablesWithFormulas(String expression, Map<String, String> globalVariables) {
      if (StringUtils.isEmpty(expression)) {
          return expression;
      }
      if (globalVariables == null) {
          globalVariables = new HashMap<>();
      }
      for (Map.Entry<String, String> entry : globalVariables.entrySet()) {
          String name = AT + entry.getKey() + AT;
          String value = entry.getValue();

          int index = expression.indexOf(name);
          while (index > -1) {
              String prefix = expression.substring(0, index);
              String suffix = expression.substring(index + name.length());
              // We should respect all global variable value as a compact unit
              // Ex: sumaX2= {x1}^2+{x2}^2+{x3}^2+{x4}^2+{x5}^2 then @sumaX2@/5 should be (@sumaX2@)/5
              String replacementValue = OPEN_PARENTHESIS + value + CLOSE_PARENTHESIS;
              expression = prefix + replacementValue + suffix;
              index = expression.indexOf(name);
          }
      }
      return expression;
  }

  /**
   * CALCULATED_QUESTION
   * replaceMappedVariablesWithNumbers() takes a string and substitutes any variable
   * names found with the value of the variable.  Variables look like {a}, the name of 
   * that variable is "a", and the value of that variable is in variablesWithValues
   * <p>Note - this function comprehends syntax like "5{x}".  If "x" is 37, the
   * result would be "5*37"
   * @param expression - the string which will be scanned for calculations
   * @return the input string with calculations replaced with number values. If answerExpression is null, 
   * returns a blank string (i.e "") and if no calculations are found then original string is returned.
   * @throws IllegalStateException if the formula value cannot be calculated
   * @throws SamigoExpressionError if the formula cannot be parsed
   */
  public String replaceCalculationsWithValues(String expression, int decimalPlaces) throws SamigoExpressionError {
      if (StringUtils.isEmpty(expression)) {
          expression = "";
      } else {
          String calculationAuxOpen = "" + CALCULATION_AUX_OPEN + CALCULATION_AUX_OPEN;
          String calculationAuxClose = "" + CALCULATION_AUX_CLOSE + CALCULATION_AUX_CLOSE;
          expression = expression.replaceAll("\\[\\[", calculationAuxOpen).replaceAll("\\]\\](?!\\])", calculationAuxClose);
          Matcher keyMatcher = CALCQ_CALCULATION_PATTERN.matcher(expression);
          List<String> toReplace = new ArrayList<>();
          while (keyMatcher.find()) {
              String match = keyMatcher.group(1);
              toReplace.add(match); // should be the formula
          }
          if (toReplace.size() > 0) {
              for (String formula : toReplace) {
                  String replace = calculationAuxOpen + formula + calculationAuxClose;
                  formula = StringEscapeUtils.unescapeHtml4(formula);
                  String formulaValue = processFormulaIntoValue(formula, decimalPlaces);
                  expression = StringUtils.replace(expression, replace, formulaValue);
              }
              expression = expression.replaceAll(calculationAuxOpen, "\\[\\[").replaceAll(calculationAuxClose, "\\]\\]");
          }
      }
      return expression;
  }

  /**
   * CALCULATED_QUESTION
   * Process a single formula into a final string representing the calculated value of the formula
   * 
   * @param formula the formula to process (e.g. 1 * 2 + 3 - 4), All variable replacement must have already happened
   * @param decimalPlaces number of decimals to include in the final output
   * @return the value of the formula OR empty string if there is nothing to process
   * @throws IllegalStateException if the formula value cannot be calculated (typically caused by 0 divisors and the like)
   * @throws SamigoExpressionError if the formula cannot be parsed
   */
  public String processFormulaIntoValue(String formula, int decimalPlaces) throws SamigoExpressionError {
      String value = "";
      if (StringUtils.isEmpty(formula)) {
          value = "";
      } else {
          if (decimalPlaces < 0) {
              decimalPlaces = 0;
          }
          formula = checkExpression(cleanFormula(formula));
          SamigoExpressionParser parser = new SamigoExpressionParser(); // this will turn the expression into a number in string form
          String numericString = parser.parse(formula, decimalPlaces+1);
          if (this.isAnswerValid(numericString)) {
              numericString = toScientificNotation(numericString, decimalPlaces);
              value = numericString;
          } else {
              throw new IllegalStateException("Invalid calculation formula ("+formula+") result ("+numericString+"), result could not be calculated");
          }
      }
      return value;
  }

  /**
   * Cleans up formula text so that whitespaces are normalized or removed
   * @param formula formula with variables or without
   * @return the cleaned formula
   */
  public static String cleanFormula(String formula) {
      if (StringUtils.isEmpty(formula)) {
          formula = "";
      } else {
          formula = StringUtils.trimToEmpty(formula).replaceAll("\\s+", " ");
      }

      return formula;
  }

  /**
   * CALCULATED_QUESTION
   * Takes a map of ranges and randomly chooses values for those ranges and stores them in a new map.
   */
   public Map<String, String> determineRandomValuesForRanges(Map<String, String> variableRangeMap, long itemId, long gradingId, String agentId, int validAnswersAttemptCount) {
	  Map<String, String> variableValueMap = new HashMap<>();
	  
	  // seed random number generator
	  long seed = getCalcuatedQuestionSeed(itemId, gradingId, agentId, validAnswersAttemptCount);
	  Random generator = new Random(seed);
	  
	  Iterator<Map.Entry<String, String>> i = variableRangeMap.entrySet().iterator();
	  while(i.hasNext())
	  {
		  Map.Entry<String, String>entry = i.next();
		  
		  String delimRange = entry.getValue(); // ie. "-100|100,2"
		  		  
		  BigDecimal minVal = new BigDecimal(delimRange.substring(0, delimRange.indexOf('|')));
		  BigDecimal maxVal = new BigDecimal(delimRange.substring(delimRange.indexOf('|')+1, delimRange.indexOf(',')));
		  int decimalPlaces = Integer.valueOf(delimRange.substring(delimRange.indexOf(',')+1, delimRange.length()));
		  		  
		  // This line does the magic of creating the random variable value within the range.
		  BigDecimal randomValue = maxVal.subtract(minVal).multiply(new BigDecimal(generator.nextDouble())).add(minVal);
		  
		  // Trim off excess decimal points based on decimalPlaces value
		  /*BigDecimal bd = new BigDecimal(randomValue);
		  bd = bd.setScale(decimalPlaces,BigDecimal.ROUND_HALF_UP);
		  randomValue = bd.doubleValue();
		  String displayNumber = randomValue.toString();*/
		  
		  String displayNumber = toScientificNotation(randomValue.toString(), decimalPlaces);
		  
		  // Remove ".0" if decimalPlaces ==0
		  if (decimalPlaces == 0) {
			  displayNumber = displayNumber.replace(".0", "");
		  }
		  
		  variableValueMap.put(entry.getKey(), displayNumber);
	  }
	  
	  return variableValueMap;
  }
  
  /**
   * CALCULATED_QUESTION
   * Accepts an ItemDataIfc and returns a HashMap with the pairs of 
   * variable names and variable ranges.
   */
   private Map<String, String> buildVariableRangeMap(ItemDataIfc item) {
       Map<String, String> variableRangeMap = new HashMap<>();

       String instructions = item.getInstruction();
       List<String> variables = this.extractVariables(instructions);

       // Loop through each VarName
       @SuppressWarnings("unchecked")
       List<ItemTextIfc> itemTextList = item.getItemTextArraySorted();
       for (ItemTextIfc varName : itemTextList) {
           // only look at variables for substitution, ignore formulas
           if (variables.contains(varName.getText())) {
               @SuppressWarnings("unchecked")
               List<AnswerIfc> answerList = varName.getAnswerArray();
               for (AnswerIfc range : answerList) {
                   if (!(range.getLabel() == null) ) { // answer records and variable records are in the same set
                       if (range.getSequence().equals(varName.getSequence()) && range.getText().contains("|")) {
                           variableRangeMap.put(varName.getText(), range.getText());
                       }
                   }
               }
           }
       }

       return variableRangeMap;
   }
  
  
  /**
   * CALCULATED_QUESTION
   * Make seed by combining user id, item (question) id, grading (submission) id, and attempt count (due to div by 0)
   */
  private long getCalcuatedQuestionSeed(long itemId, long gradingId, String agentId, int validAnswersAttemptCount) {
	  long userSeed = (long) agentId.hashCode();
	  return userSeed * itemId * gradingId * validAnswersAttemptCount;
  }

  /**
   * CALCULATED_QUESTION
   * Simple to check to see if this is a calculated question. It's used in storeGrades() to see if the sort is necessary.
   */
  private boolean isCalcQuestion(List tempItemGradinglist, Map publishedItemHash) {
	  if (tempItemGradinglist == null) return false;
	  if (tempItemGradinglist.isEmpty()) return false;
	  
	  Iterator iter = tempItemGradinglist.iterator();
	  while(iter.hasNext()){
		  ItemGradingData itemCheck = (ItemGradingData) iter.next();
		  Long itemId = itemCheck.getPublishedItemId();
		  ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
		  if (item != null && (TypeIfc.CALCULATED_QUESTION).equals(item.getTypeId())) {
	    	  return true;
	      }
	  }
	  return false;
  }
  
  /**
   * IMAGE_MAP QUESTION
   * Simple to check to see if this is a calculated question. It's used in storeGrades() to see if the sort is necessary.
   */
  private boolean isImageMapQuestion(List<ItemGradingData> tempItemGradinglist, Map publishedItemHash) {
	  if (tempItemGradinglist == null) return false;
	  if (tempItemGradinglist.isEmpty()) return false;
	  
	  
	  for(ItemGradingData itemCheck: tempItemGradinglist){
		  Long itemId = itemCheck.getPublishedItemId();
		  ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
		  if (item != null && (TypeIfc.IMAGEMAP_QUESTION).equals(item.getTypeId())) {
		    return true;
		  }
	  }
	  return false;
  }

  
  /**
   * FIB QUESTIONS
   * we ensure that answer text between brackets is always nonempty, also that
   * starting text is nonempty, we use a non-breaking space for this purpose
   * @param fib
   * @return
   */
  private static String padFibWithNonbreakSpacesText(String fib, String marker_left, String marker_right) {
	  String fibTmp=fib;

	  if (fibTmp.startsWith(marker_left)){
		  fibTmp = NBSP + fibTmp;
	  }
	  String regExp = Pattern.quote("" + marker_right) + Pattern.quote("" + marker_left);
	  return fibTmp.replaceAll(regExp, marker_right + NBSP + marker_left);
  }

  /**
   * FIB QUESTIONS
   *  Get list of form:
   *  {ans=red, text=Roses are},
   *  {ans=blue, text=and violets are},
   *  {ans=null, text=.}
   *  From String of form "Roses are {red} and violets are {blue}."
   *
   * @param input
   * @return list of Maps
   */
  public List<Map<String, String>> parseFillInBlank(String input, String markers_pair) {
	  String marker_left = "" + markers_pair.charAt(0);
	  String marker_right = "" + markers_pair.charAt(1);
	  String inputTmp = input;
	  inputTmp = padFibWithNonbreakSpacesText(inputTmp, marker_left, marker_right);

	  List<Map<String, String>> storeParts = new ArrayList<>();
	  if (inputTmp == null) {
		  return storeParts;
	  }

	  StringTokenizer st = new StringTokenizer(inputTmp, marker_right);
	  String tempToken = "";
	  String[] splitArray = null;

	  while (st.hasMoreTokens()){
		  tempToken = st.nextToken();
		  Map<String, String> tempMap = new HashMap<>();

		  //split out text and answer parts from token
		  String splitRegEx = Pattern.quote("" + marker_left);
		  splitArray = tempToken.trim().split(splitRegEx, 2);
		  tempMap.put("text", splitArray[0].trim());
		  if (splitArray.length > 1){

			  tempMap.put("ans", (splitArray[1]));
		  }
		  else{
			  tempMap.put("ans", null);
		  }

		  storeParts.add(tempMap);
	  }

	  return storeParts;
  }

  /**
   * FIB QUESTIONS
   * Check if every open marker has one to close the pair
   * @param text
   * @param markers_pair
   * @return true if the text if malformed
   */
  public boolean checkPairErrorsFIB(String text, String markers_pair) {
	  int index=0;
	  boolean FIBerror=false;
	  boolean hasOpen=false;
	  int opencount=0;
	  int closecount=0;
	  boolean notEmpty=false;
	  int indexOfOpen=-1;
	  while(index<text.length()){
		  char c=text.charAt(index);
		  if (c == markers_pair.charAt(0)){
			  opencount++;
			  if(hasOpen){
				  FIBerror=true;
				  break;
			  }
			  else{
				  hasOpen=true;
				  indexOfOpen=index;
			  }
		  }
		  else if (c == markers_pair.charAt(1)){
			  closecount++;
			  if(!hasOpen){
				  FIBerror=true;
				  break;
			  }
			  else{
				  if(notEmpty && (indexOfOpen+1 != index) && (!(text.substring(indexOfOpen+1,index).equals("</p><p>")))){
					  hasOpen=false;
					  notEmpty=false;
				  }
				  else{
					  //error for emptyString
					  FIBerror=true;
					  break;
				  }
			  }
		  }

		  else{
			  if(hasOpen && (!Character.isWhitespace(c))){
				  notEmpty=true;
			  }
		  }
		  index++;
	  }//end while
	  if(hasOpen || (opencount<1) || (opencount!=closecount) || FIBerror){
		  return true;
	  }
	  else{
		  return false;
	  }
  }

  /**
   * FIB QUESTIONS
   * Check if the markers_pair elected are valid markers.
   * @param markers_pair
   * @return an number that tell if markers are valid or which error they have
   */
  public int checkMarkersFIB(String markers_pair) {
	  // if markers length is lower or more than 2
	  if (markers_pair.length() < 2 || markers_pair.length() > 2) {
		  return 1;
	  }
	  // if both chars are the same
	  if (markers_pair.charAt(0) == markers_pair.charAt(1)) {
		  return 2;
	  }
	  // if markers are some forbidden chars
	  if (fibTextContainsIllegalCharacters(markers_pair)) {
		  return 3;
	  }

	  return 0;
  }
  /**
   * FIB QUESTIONS
   * @param toExamine marker_pair to determine if contain illegal chars
   * @return if contains illegals ["'.,&<>\s|*]
   */
  private boolean fibTextContainsIllegalCharacters(String toExamine) {
	  Pattern pattern = Pattern.compile("[\"\'.,&<>\\s|*]");
	  Matcher matcher = pattern.matcher(toExamine);
	  return matcher.find();
  }
  
  public List<Boolean> getHasGradingDataAndHasSubmission(Long publishedAssessmentId) {
	    List<Boolean> al = new ArrayList<>();
	    try {
	    	al = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getHasGradingDataAndHasSubmission(publishedAssessmentId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return al;
  }
  
  public String getFileName(Long itemGradingId, String agentId, String filename) {
	  String name = "";
	    try {
	    	name = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getFilename(itemGradingId, agentId, filename);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return name;
  }
  
  public List getUpdatedAssessmentList(String agentId, String siteId) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getUpdatedAssessmentList(agentId, siteId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return list;
  }
  
  public List getSiteNeedResubmitList(String siteId) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteNeedResubmitList(siteId);
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    return list;
  }
  
  public int autoSubmitAssessments() {
	  try {
		  return PersistenceService.getInstance().
		  getAssessmentGradingFacadeQueries().autoSubmitAssessments();
	  } catch (Exception e) {
		  log.error(e.getMessage(), e);
		  return 1;
	  }
  }
  
  public ItemGradingAttachment createItemGradingAttachment(
		  ItemGradingData itemGrading, String resourceId, String filename,
			String protocol) {
	  ItemGradingAttachment attachment = null;
		try {
			attachment = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().createItemGradingtAttachment(itemGrading,
					resourceId, filename, protocol);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}
  
  public AssessmentGradingAttachment createAssessmentGradingAttachment(
		  AssessmentGradingData assessmentGrading, String resourceId, String filename,
			String protocol) {
	  AssessmentGradingAttachment attachment = null;
		try {
			attachment = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().createAssessmentGradingtAttachment(assessmentGrading,
					resourceId, filename, protocol);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}

  public void removeItemGradingAttachment(String attachmentId) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .removeItemGradingAttachment(Long.valueOf(attachmentId));
  }
  
  public void removeAssessmentGradingAttachment(String attachmentId) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .removeAssessmentGradingAttachment(Long.valueOf(attachmentId));
  }

  public void saveOrUpdateAttachments(List list) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .saveOrUpdateAttachments(list);
  }
  
  public Map getInProgressCounts(String siteId)  {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      getInProgressCounts(siteId);
  }
  
  public Map getSubmittedCounts(String siteId)  {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      getSubmittedCounts(siteId);
  }
  
  public void completeItemGradingData(AssessmentGradingData assessmentGradingData)  {
      PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      completeItemGradingData(assessmentGradingData);
  }
  
  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingData for
   * each choice, they're graded the same way the single choice are.
   * BUT since we have Partial Credit stuff around we have to have a separate method here  --mustansar
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public double getAnswerScoreMCQ(ItemGradingData data, Map publishedAnswerHash)
  {
	  AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
	  if (answer == null || answer.getScore() == null) {
		  return 0d;
	  }
	  else if (answer.getIsCorrect()){ // instead of using answer score Item score needs to be used here 
		  return (answer.getItem().getScore()); //--mustansar 
	  }
	  return (answer.getItem().getScore()*answer.getPartialCredit())/100d;
  }
  
  /**
   *  Reorder a map of EMI scores
   * @param emiScoresMap
   * @return
   */
  private Map<Long, Map<Long, Map<Long, EMIScore>>> reorderEMIScoreMap(Map<Long, Map<Long,Set<EMIScore>>> emiScoresMap){
	  Map<Long, Map<Long, Map<Long, EMIScore>>> scoresMap = new HashMap<>();
	  for(Map<Long,Set<EMIScore>> emiItemScoresMap: emiScoresMap.values()){
		  for(Set<EMIScore> scoreSet: emiItemScoresMap.values()){
			  for(EMIScore s: scoreSet){
				  Map<Long, Map<Long, EMIScore>> scoresItem = scoresMap.get(s.itemId);
				  if(scoresItem == null){
					  scoresItem = new HashMap<>();
					  scoresMap.put(s.itemId, scoresItem);
				  }
				  Map<Long, EMIScore> scoresItemText = scoresItem.get(s.itemTextId);
				  if(scoresItemText == null){
					  scoresItemText = new HashMap<>();
					  scoresItem.put(s.itemTextId, scoresItemText);
				  }
				  scoresItemText.put(s.answerId, s);
			  }
		  }
	  }
	  return scoresMap;
  }
	
  	/**
  	 * hasDistractors looks at an itemData object for a Matching question and determines
  	 * if all of the choices have correct matches or not.
  	 * @param item
  	 * @return true if any of the choices do not have a correct answer (a distractor choice), or false
  	 * if all choices have at least one correct answer
  	 */
  	public boolean hasDistractors(ItemDataIfc item) {
		boolean hasDistractor = false;
		Iterator<ItemTextIfc> itemIter = item.getItemTextArraySorted().iterator();
		while (itemIter.hasNext()) {
			ItemTextIfc curItem = itemIter.next();
			if (isDistractor(curItem)) {
				hasDistractor = true;
				break;
			}
		}
		return hasDistractor;	  
	}

  	/**
  	 * determines if the passed parameter is a distractor
  	 * <p>For ItemTextIfc objects that hold data for matching type questions, a distractor 
  	 * is a choice that has no valid matches (i.e. no correct answers).  This function returns
  	 * if this ItemTextIfc object has any correct answers
  	 * @param itemText
  	 * @return true if itemtext has no correct answers (a distrator) or false if itemtext has at least
  	 * one correct answer
  	 */
	public boolean isDistractor(ItemTextIfc itemText) {
		// look for items that do not have any correct answers
		boolean hasCorrectAnswer = false;
		List<AnswerIfc> answers = itemText.getAnswerArray();
		Iterator<AnswerIfc> answerIter = answers.iterator();
		while (answerIter.hasNext()) {
			AnswerIfc answer = answerIter.next();
			if (answer.getIsCorrect() != null && answer.getIsCorrect()) {
				hasCorrectAnswer = true;
				break;
			}
		}
		return !hasCorrectAnswer;	  
	}

  public List getUnSubmittedAssessmentGradingDataList(Long publishedAssessmentId, String agentIdString)  {
	  return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
			  getUnSubmittedAssessmentGradingDataList(publishedAssessmentId, agentIdString);
  }

	public SectionGradingData getSectionGradingData(Long assessmentGradingId, Long sectionId, String agentId)
	{
		try {
			return PersistenceService.getInstance()
				.getAssessmentGradingFacadeQueries()
				.getSectionGradingData(assessmentGradingId, sectionId, agentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Error(e);
		}
	}

	public void saveSectionGrading(SectionGradingData sgd)
	{
		try {
			PersistenceService.getInstance().getAssessmentGradingFacadeQueries().saveSectionGrading(sgd);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}

/**
 * A EMI score
 * @author jsmith
 *
 */
class EMIScore implements Comparable<EMIScore>{
	long itemId = 0L;
	long itemTextId = 0L;
	long answerId = 0L;
	boolean correct = false;
	double score = 0.0;
	double effectiveScore = 0.0;
	
	/**
	 * Create an EMI Score object
	 * @param itemId
	 * @param itemTextId
	 * @param answerId
	 * @param correct
	 * @param score
	 */
	public EMIScore(Long itemId, Long itemTextId, Long answerId, boolean correct, Double score){
		this.itemId = itemId == null? 0L : itemId;
		this.itemTextId = itemTextId == null? 0L : itemTextId;
		this.answerId = answerId == null? 0L : answerId;
		this.correct = correct;
		this.score = score == null? 0L : score;
	}

	public int compareTo(EMIScore o) {
		//we want the correct higher scores first
		if(correct == o.correct){
			int c = Double.compare(o.score, score);
			if (c == 0){
				if(itemId != o.itemId){
					return (int)(itemId - o.itemId);
				}
				if(itemTextId != o.itemTextId){
					return (int)(itemTextId - o.itemTextId);
				}
				if(answerId != o.answerId){
					return (int)(answerId - o.answerId);
				}
				return hashCode() - o.hashCode();
			}else{
				return c;
			}
		}else{
			return correct?-1:1;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)itemId;
		result = prime * result + (int)itemTextId;
		result = prime * result + (int)answerId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (getClass() != obj.getClass()){
			return false;
		}
		EMIScore other = (EMIScore) obj;
		return (itemId == other.itemId && 
				itemTextId == other.itemTextId &&
				answerId == other.answerId);
	}
	
	@Override
	public String toString() {
		return itemId + ":" + itemTextId + ":" + answerId + "(" + correct + ":" + score + ":" + effectiveScore + ")";
	}
}
