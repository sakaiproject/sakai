/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/GradingService.java $
 * $Id: GradingService.java 9784 2006-05-22 19:33:28Z daisyf@stanford.edu $
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexFormat;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.util.SamigoExpressionError;
import org.sakaiproject.tool.assessment.util.SamigoExpressionParser;
import org.sakaiproject.tool.assessment.util.TextFormat;


/**
 * The GradingService calls the back end to get/store grading information. 
 * It also calculates scores for autograded types.
 */
public class GradingService
{
  private final String OPEN_BRACKET = "\\{";
  private final String CLOSE_BRACKET = "\\}";
  
  /**
   * regular expression for matching the contents of a variable or formula name 
   * in Calculated Questions
   * TODO - this regular expression is way too complicated.  There must be 
   * a better way to include any character except for the curly braces inside 
   * the contents of the group.
   */
  private final String CALCQ_VAR_FORM_NAME_EXPRESSION = "([\\w\\s\\.\\-\\^\\$\\!\\&\\@\\?\\*\\%\\(\\)\\+=#`~&:;|,/<>\\[\\]\\\\\\'\"]+?)";
  private static Log log = LogFactory.getLog(GradingService.class);

  /**
   * Get all scores for a published assessment from the back end.
   */
  public ArrayList getTotalScores(String publishedId, String which)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getTotalScores(publishedId,
             which));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public ArrayList getTotalScores(String publishedId, String which, boolean getSubmittedOnly)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getTotalScores(publishedId,
             which, getSubmittedOnly));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
 /**
  * Get all submissions for a published assessment from the back end.
  */
  public List getAllSubmissions(String publishedId)
  {
    List results = null;
    try {
      results = PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllSubmissions(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
    return results;
  }

  public ArrayList getHighestAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestAssessmentGradingList(publishedId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public List getHighestSubmittedOrGradedAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestSubmittedOrGradedAssessmentGradingList(publishedId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  public ArrayList getLastAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastAssessmentGradingList(publishedId));
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
      e.printStackTrace();
    }
    return results;
  }
  
  public void saveTotalScores(ArrayList gdataList, PublishedAssessmentIfc pub)
  {
      //log.debug("**** GradingService: saveTotalScores");
    try {
     AssessmentGradingData gdata = null;
      if (gdataList.size()>0)
        gdata = (AssessmentGradingData) gdataList.get(0);
      else return;

      Integer scoringType = getScoringType(pub);
      ArrayList oldList = getAssessmentGradingsByScoringType(
          scoringType, gdata.getPublishedAssessmentId());
      for (int i=0; i<gdataList.size(); i++){
        AssessmentGradingData ag = (AssessmentGradingData)gdataList.get(i);
        saveOrUpdateAssessmentGrading(ag);
        EventTrackingService.post(EventTrackingService.newEvent("sam.total.score.update", 
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
      ArrayList newList = getAssessmentGradingsByScoringType(
        scoringType, gdata.getPublishedAssessmentId());
      ArrayList l = getListForGradebookNotification(newList, oldList);
      
      notifyGradebook(l, pub);
    } catch (GradebookServiceException ge) {
      log.error("GradebookServiceException" + ge);
      throw ge;
    } 
  }


  private ArrayList getListForGradebookNotification(
       ArrayList newList, ArrayList oldList){
    ArrayList l = new ArrayList();
    HashMap h = new HashMap();
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
            && !a.getFinalScore().equals(b.getFinalScore()))
          l.add(a);
      }
    }
    return l;
  }

  public ArrayList getAssessmentGradingsByScoringType(
       Integer scoringType, Long publishedAssessmentId){
    List l = null;
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
    if ( e!=null ){
      String toGradebookString = e.getToGradeBook();
      toGradebook = toGradebookString.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
    }
    return (forGrade && toGradebook);
  }

  private void notifyGradebook(ArrayList l, PublishedAssessmentIfc pub){
    for (int i=0; i<l.size(); i++){
      notifyGradebook((AssessmentGradingData)l.get(i), pub);
    }
  }


  /**
   * Get the score information for each item from the assessment score.
   */
  public HashMap getItemScores(Long publishedId, Long itemId, String which)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(publishedId, itemId, which);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  public HashMap getItemScores(Long itemId, List scores)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(itemId, scores);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }
  
  /**
   * Get the last set of itemgradingdata for a student per assessment
   */
  public HashMap getLastItemGradingData(String publishedId, String agentId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getLastItemGradingData(Long.valueOf(publishedId), agentId);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  /**
   * Get the grading data for a given submission
   */
  public HashMap getStudentGradingData(String assessmentGradingId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getStudentGradingData(assessmentGradingId);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  /**
   * Get the last submission for a student per assessment
   */
  public HashMap getSubmitData(String publishedId, String agentId, Integer scoringoption, String assessmentGradingId)
  {
    try {
      Long gradingId = null;
      if (assessmentGradingId != null) gradingId = Long.valueOf(assessmentGradingId);
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().getSubmitData(Long.valueOf(publishedId), agentId, scoringoption, gradingId);
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
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

  public ArrayList getMediaArray(String itemGradingId){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(Long.valueOf(itemGradingId));
  }
  
  public ArrayList getMediaArray2(String itemGradingId){
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	        getMediaArray2(Long.valueOf(itemGradingId));
  }

  public ArrayList getMediaArray(ItemGradingData i){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(i);
  }
  
  public HashMap getMediaItemGradingHash(Long assessmentGradingId) {
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	    getMediaItemGradingHash(assessmentGradingId);
  }
  
  public List getMediaArray(String publishedId, String publishItemId, String which){
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	    getMediaArray(Long.valueOf(publishedId), Long.valueOf(publishItemId), which);
  }
  
  public ItemGradingData getLastItemGradingDataByAgent(String publishedItemId, String agentId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastItemGradingDataByAgent(Long.valueOf(publishedItemId), agentId);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public ItemGradingData getItemGradingData(String assessmentGradingId, String publishedItemId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGradingData(Long.valueOf(assessmentGradingId), Long.valueOf(publishedItemId));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public AssessmentGradingData load(String assessmentGradingId) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          load(Long.valueOf(assessmentGradingId));
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public ItemGradingData getItemGrading(String itemGradingId) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGrading(Long.valueOf(itemGradingId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastAssessmentGradingByAgentId(Long.valueOf(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastSavedAssessmentGradingByAgentId(Long.valueOf(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
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
		  log.error(e); throw new RuntimeException(e);
	  }

	  return assessmentGranding;
  }
  
  public void saveItemGrading(ItemGradingData item)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveItemGrading(item);
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
    	  e.printStackTrace();
      }
      finally {
    	  // Restore the original itemGradingSet back
    	  assessment.setItemGradingSet(h);
    	  size = assessment.getItemGradingSet().size();
		  log.debug("after persist to db: size = " + size);
      }
  }

  public List getAssessmentGradingIds(String publishedItemId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
         getAssessmentGradingIds(Long.valueOf(publishedItemId));
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public AssessmentGradingData getHighestAssessmentGrading(String publishedAssessmentId, String agentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	      getHighestAssessmentGrading(Long.valueOf(publishedAssessmentId), agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
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
		  log.error(e); throw new RuntimeException(e);
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
      log.error(e); throw new RuntimeException(e);
    }
  }

  public HashMap getAssessmentGradingByItemGradingId(String publishedAssessmentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getAssessmentGradingByItemGradingId(Long.valueOf(publishedAssessmentId));
    }
    catch(Exception e){
      log.error(e); throw new RuntimeException(e);
    }
  }

  public void updateItemScore(ItemGradingData gdata, float scoreDifference, PublishedAssessmentIfc pub){
    try {
      AssessmentGradingData adata = load(gdata.getAssessmentGradingId().toString());
      adata.setItemGradingSet(getItemGradingSet(adata.getAssessmentGradingId().toString()));

      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      float totalAutoScore = 0;
      float totalOverrideScore = adata.getTotalOverrideScore().floatValue();
      while (iter.hasNext()){
        ItemGradingData i = (ItemGradingData)iter.next();
        if (i.getItemGradingId().equals(gdata.getItemGradingId())){
	  i.setAutoScore(gdata.getAutoScore());
          i.setComments(gdata.getComments());
          i.setGradedBy(AgentFacade.getAgentString());
          i.setGradedDate(new Date());
	}
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore().floatValue();
      }
      
      adata.setTotalAutoScore( Float.valueOf(totalAutoScore));
      if (Float.compare((totalAutoScore+totalOverrideScore),Float.valueOf("0").floatValue())<0){
    	  adata.setFinalScore(Float.valueOf("0"));
      }else{
    	  adata.setFinalScore(Float.valueOf(totalAutoScore+totalOverrideScore));
      }
      saveOrUpdateAssessmentGrading(adata);
      if (scoreDifference != 0){
        notifyGradebookByScoringType(adata, pub);
      }
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingData data, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash, HashMap invalidFINMap, ArrayList invalidSALengthList) throws GradebookServiceException, FinFormatException
  {
	  log.debug("storeGrades: data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true, invalidFINMap, invalidSALengthList);
  }
  
  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingData data, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash, boolean persistToDB, HashMap invalidFINMap, ArrayList invalidSALengthList) throws GradebookServiceException, FinFormatException
  {
	  log.debug("storeGrades (not persistToDB) : data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, persistToDB, invalidFINMap, invalidSALengthList);
  }
  
  public void storeGrades(AssessmentGradingData data, boolean regrade, PublishedAssessmentIfc pub,
		  HashMap publishedItemHash, HashMap publishedItemTextHash,
		  HashMap publishedAnswerHash, boolean persistToDB) throws GradebookServiceException, FinFormatException {
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
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash, boolean persistToDB, HashMap invalidFINMap, ArrayList invalidSALengthList) 
         throws GradebookServiceException, FinFormatException {
    log.debug("****x1. regrade ="+regrade+" "+(new Date()).getTime());
    try {
      String agent = data.getAgentId();
      
      // note that this itemGradingSet is a partial set of answer submitted. it contains only 
      // newly submitted answers, updated answers and MCMR/FIB/FIN answers ('cos we need the old ones to
      // calculate scores for new ones)
      Set itemGradingSet = data.getItemGradingSet();
      if (itemGradingSet == null)
        itemGradingSet = new HashSet();
      log.debug("****itemGrading size="+itemGradingSet.size());
      
      List<Object> tempItemGradinglist = new ArrayList<Object>(itemGradingSet);
      
      // CALCULATED_QUESTION - if this is a calc question. Carefully sort the list of answers
      if (isCalcQuestion(tempItemGradinglist, publishedItemHash)) {
	      Collections.sort(tempItemGradinglist, new Comparator(){
	    	  public int compare(Object o1, Object o2) {
	    		  ItemGradingData gradeData1 = (ItemGradingData) o1;
	    		  ItemGradingData gradeData2 = (ItemGradingData) o2;
	    		  
	    		  // protect against blank ones in samigo initial setup.
	    		  if (gradeData1 == null) return -1; 
	    		  if (gradeData2 == null) return 1;
	    		  if (gradeData1.getPublishedAnswerId() == null) return -1; 
	    		  if (gradeData2.getPublishedAnswerId() == null) return 1; 
	    		  return gradeData1.getPublishedAnswerId().compareTo(gradeData2.getPublishedAnswerId());
	    	  }
	      });
      }
      
      Iterator iter = tempItemGradinglist.iterator();

      // fibAnswersMap contains a map of HashSet of answers for a FIB item,
      // key =itemid, value= HashSet of answers for each item.  
      // This is used to keep track of answers we have already used for 
      // mutually exclusive multiple answer type of FIB, such as 
      // The flag of the US is {red|white|blue},{red|white|blue}, and {red|white|blue}.
      // so if the first blank has an answer 'red', the 'red' answer should 
      // not be included in the answers for the other mutually exclusive blanks. 
      HashMap fibAnswersMap= new HashMap();
      
      //change algorithm based on each question (SAK-1930 & IM271559) -cwen
      HashMap totalItems = new HashMap();
      log.debug("****x2. "+(new Date()).getTime());
      float autoScore = (float) 0;
      Long itemId = (long)0;
      int calcQuestionAnswerSequence = 1; // sequence of answers for CALCULATED_QUESTION
      while(iter.hasNext())
      {
        ItemGradingData itemGrading = (ItemGradingData) iter.next();
        
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
        	log.error("unable to retrive itemDataIfc for: " + publishedItemHash.get(itemId));
        	continue;
        }
        Long itemType = item.getTypeId();  
    	autoScore = (float) 0;

        itemGrading.setAssessmentGradingId(data.getAssessmentGradingId());
        //itemGrading.setSubmittedDate(new Date());
        itemGrading.setAgentId(agent);
        itemGrading.setOverrideScore(Float.valueOf(0));
        
        if (itemType == 5 && itemGrading.getAnswerText() != null) {
        	String processedAnswerText = itemGrading.getAnswerText().replaceAll("\r", "").replaceAll("\n", "");
        	if (processedAnswerText.length() > 60000) {
        		if (invalidSALengthList != null) {
        			invalidSALengthList.add(item.getItemId());
        		}
        	}
        }
        
        if (itemType == 8 && itemGrading.getAnswerText() != null) {
        	String processedAnswerText = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, itemGrading.getAnswerText().trim());
        	itemGrading.setAnswerText(processedAnswerText);
        }
        
        // note that totalItems & fibAnswersMap would be modified by the following method
        try {
        	autoScore = getScoreByQuestionType(itemGrading, item, itemType, publishedItemTextHash, 
                               totalItems, fibAnswersMap, publishedAnswerHash, regrade, calcQuestionAnswerSequence );
        }
        catch (FinFormatException e) {
        	autoScore = 0f;
        	if (invalidFINMap != null) {
        		if (invalidFINMap.containsKey(itemId)) {
        			ArrayList list = (ArrayList) invalidFINMap.get(itemId);
        			list.add(itemGrading.getItemGradingId());
        		}
        		else {
        			ArrayList list = new ArrayList();
        			list.add(itemGrading.getItemGradingId());
        			invalidFINMap.put(itemId, list);
        		}
        	}
        }
        
        log.debug("**!regrade, autoScore="+autoScore);
        if (!(TypeIfc.MULTIPLE_CORRECT).equals(itemType))
          totalItems.put(itemId, Float.valueOf(autoScore));
        
        if (regrade && TypeIfc.AUDIO_RECORDING.equals(itemType))
        	itemGrading.setAttemptsRemaining(item.getTriesAllowed());
	
        itemGrading.setAutoScore(Float.valueOf(autoScore));
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
      
      log.debug("****x3. "+(new Date()).getTime());
      // the following procedure ensure total score awarded per question is no less than 0
      // this probably only applies to MCMR question type - daisyf
      iter = itemGradingSet.iterator();
      float totalAutoScoreCheck = 0;
      //get item information to check if it's MCMS and Not Partial Credit
      Long itemType2 = -1l;
      String mcmsPartialCredit = "";
      float itemScore = -1;
      while(iter.hasNext())
      {
        ItemGradingData itemGrading = (ItemGradingData) iter.next();
        itemId = itemGrading.getPublishedItemId();
        ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
        itemType2 = item.getTypeId();
        //get item information to check if it's MCMS and Not Partial Credit
        mcmsPartialCredit = item.getItemMetaDataByLabel(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT);
        itemScore = item.getScore();
        //float autoScore = (float) 0;

        float eachItemScore = ((Float) totalItems.get(itemId)).floatValue();
        if((eachItemScore < 0) && !((TypeIfc.MULTIPLE_CHOICE).equals(itemType2)||(TypeIfc.TRUE_FALSE).equals(itemType2)))
        {
        	itemGrading.setAutoScore( Float.valueOf(0));
        }
        totalAutoScoreCheck += itemGrading.getAutoScore();
      }
      //if it's MCMS and Not Partial Credit and the score isn't 100%, that means the user didn't
      //answer all of the correct answers only.  We need to set their score to 0 for all ItemGrading items
      if(TypeIfc.MULTIPLE_CORRECT.equals(itemType2) && "false".equals(mcmsPartialCredit) && totalAutoScoreCheck != itemScore){
    	  //reset all scores to 0 since the user didn't get all correct answers
    	  iter = itemGradingSet.iterator();
    	  while(iter.hasNext()){
    		  ItemGradingData itemGrading = (ItemGradingData) iter.next();
    		  itemGrading.setAutoScore(Float.valueOf(0));
    	  }
      }
      
      log.debug("****x4. "+(new Date()).getTime());

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
      float totalAutoScore = getTotalAutoScore(fullItemGradingSet);
      data.setTotalAutoScore( Float.valueOf(totalAutoScore));
      //log.debug("**#1 total AutoScore"+totalAutoScore);
      if (Float.compare((totalAutoScore + data.getTotalOverrideScore().floatValue()),new Float("0").floatValue())<0){
    	  data.setFinalScore( Float.valueOf("0"));
      }else{
    	  data.setFinalScore(Float.valueOf(totalAutoScore + data.getTotalOverrideScore().floatValue()));
      }
      log.debug("****x6. "+(new Date()).getTime());
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } 
    catch (Exception e) {
      e.printStackTrace();
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
    	log.debug("****x7. "+(new Date()).getTime());	
    	if (!regrade) {
    		notifyGradebookByScoringType(data, pub);
    	}
    }
    log.debug("****x8. "+(new Date()).getTime());

    // I am not quite sure what the following code is doing... I modified this based on my assumption:
    // If this happens dring regrade, we don't want to clean these data up
    // We only want to clean them out in delivery
    if (!regrade && Boolean.TRUE.equals(data.getForGrade())) {
    	// remove the assessmentGradingData created during gradiing (by updatding total score page)
    	removeUnsubmittedAssessmentGradingData(data);
    }
  }

  private float getTotalAutoScore(Set itemGradingSet){
      //log.debug("*** no. of itemGrading="+itemGradingSet.size());
    float totalAutoScore =0;
    Iterator iter = itemGradingSet.iterator();
    while (iter.hasNext()){
      ItemGradingData i = (ItemGradingData)iter.next();
      //log.debug(i.getItemGradingId()+"->"+i.getAutoScore());
      if (i.getAutoScore()!=null)
	totalAutoScore += i.getAutoScore().floatValue();
    }
    return totalAutoScore;
  }

  private void notifyGradebookByScoringType(AssessmentGradingData data, PublishedAssessmentIfc pub){
    Integer scoringType = pub.getEvaluationModel().getScoringType();
    if (updateGradebook(data, pub)){
      AssessmentGradingData d = data; // data is the last submission
      // need to decide what to tell gradebook
      if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE))
        d = getHighestSubmittedAssessmentGrading(pub.getPublishedAssessmentId().toString(), data.getAgentId());
      notifyGradebook(d, pub);
    }
  }
  
  private float getScoreByQuestionType(ItemGradingData itemGrading, ItemDataIfc item,
                                       Long itemType, HashMap publishedItemTextHash, 
                                       HashMap totalItems, HashMap fibAnswersMap,
                                       HashMap publishedAnswerHash, boolean regrade,
                                       int calcQuestionAnswerSequence) throws FinFormatException {
    //float score = (float) 0;
    float initScore = (float) 0;
    float autoScore = (float) 0;
    float accumelateScore = (float) 0;
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
    		autoScore += itemGrading.getOverrideScore().floatValue();
    	totalItems.put(itemId, new Float(autoScore));
    	break;// MC Single Correct
      case 12: // MC Multiple Correct Single Selection    	  
      case 3: // MC Survey
      case 4: // True/False     	  
              autoScore = getAnswerScore(itemGrading, publishedAnswerHash);
              //overridescore
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
	      totalItems.put(itemId,  Float.valueOf(autoScore));
              break;
      case 2: // MC Multiple Correct
              ItemTextIfc itemText = (ItemTextIfc) publishedItemTextHash.get(itemGrading.getPublishedItemTextId());
              ArrayList answerArray = itemText.getAnswerArray();
              int correctAnswers = 0;
              if (answerArray != null){
                for (int i =0; i<answerArray.size(); i++){
                  AnswerIfc a = (AnswerIfc) answerArray.get(i);
                  if (a.getIsCorrect().booleanValue())
                    correctAnswers++;
                }
              }
              initScore = getAnswerScore(itemGrading, publishedAnswerHash);
              if (initScore > 0)
                autoScore = initScore / correctAnswers;
              else
                autoScore = (getTotalCorrectScore(itemGrading, publishedAnswerHash) / correctAnswers) * ((float) -1);

              //overridescore?
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
              if (!totalItems.containsKey(itemId)){
                totalItems.put(itemId,  Float.valueOf(autoScore));
                //log.debug("****0. first answer score = "+autoScore);
              }
              else{
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                //log.debug("****1. before adding new score = "+accumelateScore);
                //log.debug("****2. this answer score = "+autoScore);
                accumelateScore += autoScore;
                //log.debug("****3. add 1+2 score = "+accumelateScore);
                totalItems.put(itemId,  Float.valueOf(accumelateScore));
                //log.debug("****4. what did we put in = "+((Float)totalItems.get(itemId)).floatValue());
              }
              break;

      case 9: // Matching     
              initScore = getAnswerScore(itemGrading, publishedAnswerHash);
              if (initScore > 0) {
            	  	int nonDistractors = 0;
          	    	Iterator<ItemTextIfc> itemIter = item.getItemTextArraySorted().iterator();
          	    	while (itemIter.hasNext()) {
          	    		ItemTextIfc curItem = itemIter.next();
          	    		if (!isDistractor(curItem)) {
          	    			nonDistractors++;
          	    		}
          	    	}            	  
                    autoScore = initScore / nonDistractors;
              	}
              //overridescore?
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId,  Float.valueOf(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId,  Float.valueOf(accumelateScore));
              }
              break;

      case 8: // FIB
              autoScore = getFIBScore(itemGrading, fibAnswersMap, item, publishedAnswerHash) / (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
              //overridescore - cwen
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, Float.valueOf(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, Float.valueOf(accumelateScore));
              }
              break;
      case 15:  // CALCULATED_QUESTION
      case 11: // FIN
    	  try {
    	      if (type == 15) {  // CALCULATED_QUESTION
	              HashMap calculatedAnswersMap = getCalculatedAnswersMap(itemGrading, item);
	              int numAnswers = calculatedAnswersMap.size();
	              autoScore = getCalcQScore(itemGrading, item, calculatedAnswersMap, calcQuestionAnswerSequence ) / (float) numAnswers;
	          } else {
	              autoScore = getFINScore(itemGrading, item, publishedAnswerHash) / (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
	          }
    	  }
    	  catch (FinFormatException e) {
    		  throw e;
    	  }
    	  //overridescore - cwen
          if (itemGrading.getOverrideScore() != null)
            autoScore += itemGrading.getOverrideScore().floatValue();

          if (!totalItems.containsKey(itemId))
            totalItems.put(itemId, Float.valueOf(autoScore));
          else {
            accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
            accumelateScore += autoScore;
            totalItems.put(itemId, Float.valueOf(accumelateScore));
          }
          break;

      case 5: // SAQ
      case 6: // file upload
      case 7: // audio recording
              //overridescore - cwen
    	  	  if (regrade && itemGrading.getAutoScore() != null) {
    	  	    autoScore = itemGrading.getAutoScore();
    	  	  }
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, Float.valueOf(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, Float.valueOf(accumelateScore));
              }
              break;
    }
    return autoScore;
  }

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingData for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public float getAnswerScore(ItemGradingData data, HashMap publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null) {
    	return (float) 0;
    }
    ItemDataIfc item = (ItemDataIfc) answer.getItem();
    Long itemType = item.getTypeId();
    if (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())
    {
    	// return (float) 0;
    	// Para que descuente (For discount)
    	if ((TypeIfc.MULTIPLE_CHOICE).equals(itemType)||(TypeIfc.TRUE_FALSE).equals(itemType)){
    		return (Math.abs(answer.getDiscount().floatValue()) * ((float) -1));
    	}else{
    		return (float) 0;
    	}
    }
    return answer.getScore().floatValue();
  }

  public void notifyGradebook(AssessmentGradingData data, PublishedAssessmentIfc pub) throws GradebookServiceException {
    // If the assessment is published to the gradebook, make sure to update the scores in the gradebook
    String toGradebook = pub.getEvaluationModel().getToGradeBook();

    GradebookService g = null;
    boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
    if (integrated)
    {
      g = (GradebookService) SpringBeanLocator.getInstance().
        getBean("org.sakaiproject.service.gradebook.GradebookService");
    }

    GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();

    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	String currentSiteId = publishedAssessmentService.getPublishedAssessmentSiteId(pub.getPublishedAssessmentId().toString());
    if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(currentSiteId), g)
        && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
        if(log.isDebugEnabled()) log.debug("Attempting to update a score in the gradebook");

    // add retry logic to resolve deadlock problem while sending grades to gradebook

    Float originalFinalScore = data.getFinalScore();
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount().intValue();
    while (retryCount > 0){
    	try {
    		// Send the average score if average was selected for multiple submissions
    		Integer scoringType = pub.getEvaluationModel().getScoringType();
    		if (scoringType.equals(EvaluationModelIfc.AVERAGE_SCORE)) {
    			// status = 5: there is no submission but grader update something in the score page
    			if(data.getStatus() ==5) {
    				data.setFinalScore(data.getFinalScore());
    			} else {
    				Float averageScore = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
    				getAverageSubmittedAssessmentGrading(Long.valueOf(pub.getPublishedAssessmentId()), data.getAgentId());
    				data.setFinalScore(averageScore);
    			}
    		}
    		gbsHelper.updateExternalAssessmentScore(data, g);
    		retryCount = 0;
    	}
      catch (org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException ante) {
    	  log.warn("problem sending grades to gradebook: " + ante.getMessage());
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

    //change the final score back to the original score since it may set to average score.
    if(data.getFinalScore() != originalFinalScore ) {
	data.setFinalScore(originalFinalScore);
     }
    } else {
       if(log.isDebugEnabled()) log.debug("Not updating the gradebook.  toGradebook = " + toGradebook);
    }
  }

  private int retry(int retryCount, Exception e, PublishedAssessmentIfc pub, boolean retractForEditStatus) {
	  log.warn("retrying...sending grades to gradebook. ");
	  log.warn("retry....");
      retryCount--;
      try {
    	  int deadlockInterval = PersistenceService.getInstance().getPersistenceHelper().getDeadlockInterval().intValue();
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
  
  public float getFIBScore(ItemGradingData data, HashMap fibmap,  ItemDataIfc itemdata, HashMap publishedAnswerHash)
  {
    String studentanswer = "";
    boolean matchresult = false;
    float totalScore = (float) 0;
    
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
    //Set answerSet = new HashSet();

    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
            matchresult = fibmatch(answer, studentanswer, true);
             
          }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
    	    matchresult = fibmatch(answer, studentanswer, false);
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
              totalScore += ((AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId())).getScore().floatValue();
            }

            // SAK-3005: quit if answer is correct, e.g. if you answered A for {a|A}, you already scored
            break;
          }
      
     }
    }
    return totalScore;
  }

  public boolean getFIBResult(ItemGradingData data, HashMap fibmap,  ItemDataIfc itemdata, HashMap publishedAnswerHash)
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
    //Set answerSet = new HashSet();


    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
            matchresult = fibmatch(answer, studentanswer, true);
           }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
    	    matchresult = fibmatch(answer, studentanswer, false);
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
  
  
  public float getFINScore(ItemGradingData data,  ItemDataIfc itemdata, HashMap publishedAnswerHash) throws FinFormatException
  {
	  float totalScore = (float) 0;
	  boolean matchresult = getFINResult(data, itemdata, publishedAnswerHash);
	  if (matchresult){
		  totalScore += ((AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId())).getScore().floatValue();
	  }	
	  return totalScore;
	  
  }
	  
  public boolean getFINResult (ItemGradingData data,  ItemDataIfc itemdata, HashMap publishedAnswerHash) throws FinFormatException
  {
	  String studentanswer = "";
	  boolean range;
	  boolean matchresult = false;
	  ComplexFormat complexFormat = new ComplexFormat();
	  Complex answerComplex = null;
	  Complex studentAnswerComplex = null;
	  BigDecimal answerNum = null, answer1Num = null, answer2Num = null, studentAnswerNum = null;

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
			  studentAnswerText = data.getAnswerText().trim();
		  }

		  if (range) {

			  String answer1 = st.nextToken().trim();
			  String answer2 = st.nextToken().trim();

			  try {
				  answer1Num = new BigDecimal(answer1);
				  answer2Num = new BigDecimal(answer2);
			  } catch (Exception e) {
				  log.debug("Number is not BigDecimal: " + answer1 + " or " + answer2);
			  }

			  HashMap map = validate(studentAnswerText);
			  studentAnswerNum = (BigDecimal) map.get("REAL");

			  matchresult = (answer1Num != null && answer2Num != null && studentAnswerNum != null &&
					  (answer1Num.compareTo(studentAnswerNum) <= 0) && (answer2Num.compareTo(studentAnswerNum) >= 0));
		  }
		  else { // not range
			  String answer = st.nextToken().trim();

			  try {
				  answerNum = new BigDecimal(answer); 
			  } catch(NumberFormatException ex) {
				  log.debug("Number is not BigDecimal: " + answer);
			  }

			  try {
				  answerComplex = complexFormat.parse(answer);
			  } catch(ParseException ex) {
				  log.debug("Number is not Complex: " + answer);
			  }

			  if (data.getAnswerText() != null) {  
				  HashMap map = validate(studentAnswerText);

				  if (answerNum != null) {
					  studentAnswerNum = (BigDecimal) map.get("REAL");
					  matchresult = (studentAnswerNum != null && answerNum.compareTo(studentAnswerNum) == 0);
				  }
				  else if (answerComplex != null) {
					  studentAnswerComplex = (Complex) map.get("COMPLEX");
					  matchresult = (studentAnswerComplex != null && answerComplex.equals(studentAnswerComplex));
				  }
			  }
		  }
	  }
	  return matchresult;
  }  

  private HashMap validate(String value) {
	  HashMap map = new HashMap();
	  if (value == null || value.trim().equals("")) {
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

			  // This is because there is a bug parsing complex number. 9i is parsed as 9
			  if (studentAnswerComplex.getImaginary() == 0 && trimmedValue.contains("i")) {
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
		  map.put("REAL", studentAnswerReal);
	  }
	  else if (isComplex) {
		  map.put("COMPLEX", studentAnswerComplex);
	  }

	  return map;
  }
  /**
   * CALCULATED_QUESTION
   * Returns a float score value for the ItemGrading element being scored for a Calculated Question
   * 
   * @param calcQuestionAnswerSequence the order of answers in the list
   * @return score for the item.
   */
  public float getCalcQScore(ItemGradingData data,  ItemDataIfc itemdata, HashMap calculatedAnswersMap, int calcQuestionAnswerSequence)
  {
	  float totalScore = (float) 0;
	  
	  if (data.getAnswerText() == null) return totalScore; // zero for blank
	  
	  // this variable should look something like this "42.1|2,2"
	  String allAnswerText = calculatedAnswersMap.get(calcQuestionAnswerSequence).toString();
	  
	  double correctAnswer = Double.valueOf(getAnswerExpression(allAnswerText));
	  
	  // Determine if the acceptable variance is a constant or a % of the answer
	  String varianceString = allAnswerText.substring(allAnswerText.indexOf("|")+1, allAnswerText.indexOf(","));
	  double acceptableVariance = (double)0;
	  if (varianceString.contains("%")){
		  double percentage = Double.valueOf(varianceString.substring(0, varianceString.indexOf("%")));
		  acceptableVariance = (percentage/100) * correctAnswer;
	  }
	  else {
		  acceptableVariance = Double.valueOf(varianceString);
	  }
	  
	  String userAnswerString = data.getAnswerText().replaceAll(",", "").trim();
	  double userAnswer;
	  try {
		  userAnswer = Double.parseDouble(userAnswerString);
	  } catch(NumberFormatException nfe) {
		  return totalScore; // zero because it's not even a number!
	  }
	  //double userAnswer = Double.valueOf(userAnswerString);
	  
	  
	  // this compares the correctAnswer against the userAnsewr
	  boolean closeEnough = (Math.abs(correctAnswer - userAnswer) <= Math.abs(acceptableVariance));
	  if (closeEnough){
		  totalScore += itemdata.getScore(); 
	  }	
	  return totalScore;
	  
  }
  
  
  public float getTotalCorrectScore(ItemGradingData data, HashMap publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null)
      return (float) 0;
    return answer.getScore().floatValue();
  }

  private void setIsLate(AssessmentGradingData data, PublishedAssessmentIfc pub){
    if (pub.getAssessmentAccessControl() != null
      && pub.getAssessmentAccessControl().getDueDate() != null &&
          pub.getAssessmentAccessControl().getDueDate().before(new Date()))
          data.setIsLate(Boolean.TRUE);
    else
      data.setIsLate( Boolean.valueOf(false));
    if (data.getForGrade().booleanValue())
      data.setStatus( Integer.valueOf(1));
    
    data.setTotalOverrideScore(Float.valueOf(0));
  }

  public void deleteAll(Collection c)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().deleteAll(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* Note:
   * assessmentGrading contains set of itemGrading that are not saved in the DB yet
   */
  public void updateAssessmentGradingScore(AssessmentGradingData adata, PublishedAssessmentIfc pub){
    try {
      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      float totalAutoScore = 0;
      float totalOverrideScore = adata.getTotalOverrideScore().floatValue();
      while (iter.hasNext()){
        ItemGradingData i = (ItemGradingData)iter.next();
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore().floatValue();
        }
        float oldAutoScore = adata.getTotalAutoScore().floatValue();
        float scoreDifference = totalAutoScore - oldAutoScore;
        adata.setTotalAutoScore(Float.valueOf(totalAutoScore));
        if (Float.compare((totalAutoScore+totalOverrideScore),Float.valueOf("0").floatValue())<0){
        	adata.setFinalScore(Float.valueOf("0"));
        }else{
        	adata.setFinalScore(Float.valueOf(totalAutoScore+totalOverrideScore));
        }
        saveOrUpdateAssessmentGrading(adata);
        if (scoreDifference != 0){
          notifyGradebookByScoringType(adata, pub);
        }
     } catch (GradebookServiceException ge) {
       ge.printStackTrace();
       throw ge;
     } catch (Exception e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
  }

  public void saveOrUpdateAll(Collection c)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAll(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(String id){
    PublishedAssessmentIfc pub = null;
    try {
      pub = PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().getPublishedAssessmentByAssessmentGradingId(Long.valueOf(id));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return pub;
  }
  
  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(String publishedItemId){
	    PublishedAssessmentIfc pub = null;
	    try {
	      pub = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getPublishedAssessmentByPublishedItemId(Long.valueOf(publishedItemId));
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return pub;
	  }
  
  public ArrayList getLastItemGradingDataPosition(Long assessmentGradingId, String agentId) {
	  	ArrayList results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getLastItemGradingDataPosition(assessmentGradingId, agentId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
	  }
  
  public List getPublishedItemIds(Long assessmentGradingId) {
	  	List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getPublishedItemIds(assessmentGradingId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
	  }
  
  public HashSet getItemSet(Long publishedAssessmentId, Long sectionId) {
	  	HashSet results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getItemSet(publishedAssessmentId, sectionId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }
  
  public Long getTypeId(Long itemGradingId) {
	  	Long typeId = null;
	    try {
	    	typeId = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getTypeId(itemGradingId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return typeId;
  }
  
  public boolean fibmatch(String answer, String input, boolean casesensitive) {

	  
		try {
 		 StringBuilder regex_quotebuf = new StringBuilder();
		 
		 String REGEX = answer.replaceAll("\\*", "|*|");
		 String[] oneblank = REGEX.split("\\|");
		 for (int j = 0; j < oneblank.length; j++) {
			 if ("*".equals(oneblank[j])) {
				 regex_quotebuf.append(".+");
			 }
			 else {
				 regex_quotebuf.append(Pattern.quote(oneblank[j]));
			 }
		 }

		 String regex_quote = regex_quotebuf.toString();
		 Pattern p;
		 if (casesensitive){
		 p = Pattern.compile(regex_quote );
		 }
		 else {
		 p = Pattern.compile(regex_quote,Pattern.CASE_INSENSITIVE );
		 }
		 Matcher m = p.matcher(input);
		 boolean result = m.matches();
 		 return result;
		  
		
		}
		catch (Exception e){
			return false;
		}
	}

  public List getAllAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
	  	List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getAllAssessmentGradingByAgentId(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }
  
  public HashMap getSiteSubmissionCountHash(String siteId) {
	  HashMap results = new HashMap();
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteSubmissionCountHash(siteId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }  
  
  public HashMap getSiteInProgressCountHash(final String siteId) {
	  HashMap results = new HashMap();
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteInProgressCountHash(siteId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }

  public int getActualNumberRetake(Long publishedAssessmentId, String agentIdString) {
	  	int actualNumberReatke = 0;
	    try {
	    	actualNumberReatke = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getActualNumberRetake(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return actualNumberReatke;
  }
  
  public HashMap getActualNumberRetakeHash(String agentIdString) {
	  HashMap actualNumberReatkeHash = new HashMap();
	    try {
	    	actualNumberReatkeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getActualNumberRetakeHash(agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return actualNumberReatkeHash;
  }
    
  public HashMap getSiteActualNumberRetakeHash(String siteIdString) {
	  HashMap numberRetakeHash = new HashMap();
	    try {
	    	numberRetakeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteActualNumberRetakeHash(siteIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetakeHash;
  }
    
  public List getStudentGradingSummaryData(Long publishedAssessmentId, String agentIdString) {
	  List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getStudentGradingSummaryData(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }
  

  public int getNumberRetake(Long publishedAssessmentId, String agentIdString) {
	  int numberRetake = 0;
	    try {
	    	numberRetake = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getNumberRetake(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetake;
  }
  
  public HashMap getNumberRetakeHash(String agentIdString) {
	  HashMap numberRetakeHash = new HashMap();
	    try {
	    	numberRetakeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getNumberRetakeHash(agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetakeHash;
  }
  
  public HashMap getSiteNumberRetakeHash(String siteIdString) {
	  HashMap siteActualNumberRetakeList = new HashMap();
	    try {
	    	siteActualNumberRetakeList = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteNumberRetakeHash(siteIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return siteActualNumberRetakeList;
  }
    
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
	    try {
	    	PersistenceService.getInstance().getAssessmentGradingFacadeQueries().saveStudentGradingSummaryData(studentGradingSummaryData);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
  }
  

  public int getLateSubmissionsNumberByAgentId(Long publishedAssessmentId, String agentIdString, Date dueDate) {
	  int numberRetake = 0;
	    try {
	    	numberRetake = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getLateSubmissionsNumberByAgentId(publishedAssessmentId, agentIdString, dueDate);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetake;
  }
  
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String rationaleString, String itemGradingCommentsString, Map useridMap) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getExportResponsesData(publishedAssessmentId, anonymous,audioMessage, fileUploadMessage, noSubmissionMessage, showPartAndTotalScoreSpreadsheetColumns, poolString, partString, questionString, textString, rationaleString, itemGradingCommentsString, useridMap);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return list;
  }
  
  private void removeUnsubmittedAssessmentGradingData(AssessmentGradingData data){
	  try {
	      PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().removeUnsubmittedAssessmentGradingData(data);
	    } catch (Exception e) {
	      //e.printStackTrace();
	      log.error("Exception thrown from removeUnsubmittedAssessmentGradingData" + e.getMessage());
	    }
  }
  
  public boolean getHasGradingData(Long publishedAssessmentId) {
	  boolean hasGradingData = false;
	    try {
	    	hasGradingData = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getHasGradingData(publishedAssessmentId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return hasGradingData;
  }
  

  /**
   * CALCULATED_QUESTION
   * @param itemGrading
   * @param item
   * @return map of calc answers
   */
  private HashMap getCalculatedAnswersMap(ItemGradingData itemGrading, ItemDataIfc item) {
	  HashMap calculatedAnswersMap = new HashMap();
	  
	  List<String> texts = extractCalcQAnswersArray(calculatedAnswersMap, item, itemGrading.getAssessmentGradingId(), itemGrading.getAgentId());
	  
	  return calculatedAnswersMap;
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
	  List<String> formulas = new ArrayList<String>();
	  if (text == null || text.length() == 0) {
		  return formulas;
	  }
	  
      Pattern formulaPattern = Pattern.compile(OPEN_BRACKET + OPEN_BRACKET + 
              CALCQ_VAR_FORM_NAME_EXPRESSION +
              CLOSE_BRACKET + CLOSE_BRACKET);
      Matcher formulaMatcher = formulaPattern.matcher(text);
      while (formulaMatcher.find()) {
          String formula = formulaMatcher.group(1);
		  formulas.add(formula);
      }
      return formulas;
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
	  List<String> variables = new ArrayList<String>();
	  if (text == null || text.length() == 0) {
		  return variables;
	  }
      Pattern variablePattern = Pattern.compile(OPEN_BRACKET + 
              CALCQ_VAR_FORM_NAME_EXPRESSION +
              CLOSE_BRACKET);        
      Matcher variableMatcher = variablePattern.matcher(text);
      while (variableMatcher.find()) {
          String variable = variableMatcher.group(1);
          
          // first character before matching group
          int start = variableMatcher.start(1) - 2;
          
          // first character after matching group
          int end = variableMatcher.end(1) + 1; // first character after the matching group
          
          // if matching group is not wrapped by {}, it's a variable
          if (start < 0 || text.charAt(start) != '{' || end >= text.length() || text.charAt(end) != '}') {
              variables.add(variable);                
          }
      }
      return variables;	  
  }
  
  private String replaceFormulaNameWithFormula(ItemDataIfc item, String formulaName) {
	  String result = "";
	  List<ItemTextIfc> items = item.getItemTextArray();
	  for (ItemTextIfc itemText : items) {
		  if (itemText.getText().equals(formulaName)) {
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
   * takes the instructions and breaks it into segments, based on the location 
   * of formula names.  One formula would give two segments, two formulas gives
   * three segments, etc.
   * <p>Note - in this context, it would probably be easier if any variable value
   * substitutions have occurred before the breakup is done; otherwise,
   * each segment will need to have substitutions done.
   * @param instructions string to be broken up
   * @return the original string, broken up based on the formula name delimiters
   */
  private List<String> extractInstructionSegments(String instructions) {
      final String FUNCTION_BEGIN = "{{";
      final String FUNCTION_END = "}}";
      List<String> segments = new ArrayList<String>();
      while (instructions.indexOf(FUNCTION_BEGIN) > -1 && instructions.indexOf(FUNCTION_END) > -1) {
          String segment = instructions.substring(0, instructions.indexOf(FUNCTION_BEGIN));
          instructions = instructions.substring(instructions.indexOf(FUNCTION_END) + FUNCTION_END.length());
          segments.add(segment);
      }
      segments.add(instructions);
      return segments;
  }
  
  /**
   * applyPrecisionToNumberString() takes a string representation of a number and returns
   * a string representation of that number, rounded to the specified number of
   * decimal places, including trimming decimal places if needed.
   * @param numberStr
   * @param decimalPlaces
   * @return
   */
  private String applyPrecisionToNumberString(String numberStr, int decimalPlaces) {
      Double calculatedAnswer = Double.valueOf(numberStr);
      
      // Trim off excess decimal points based on decimalPlaces value
      BigDecimal bd = new BigDecimal(calculatedAnswer);
      bd = bd.setScale(decimalPlaces,BigDecimal.ROUND_HALF_UP);
      calculatedAnswer = bd.doubleValue();

      String displayAnswer = calculatedAnswer.toString();
      if (decimalPlaces == 0) { // Remove ".0" if decimalPlaces ==0
          displayAnswer = displayAnswer.replace(".0", "");
      }
      return displayAnswer;
  }
  
  /**
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
   * Samgio expression parser, which should never happen as this is validated
   * when the question is saved, or if a divide by zero error occurs.
   */
  private Map<Integer, String> calculateFormulaValues(Map<String, String> variables, ItemDataIfc item) throws Exception {
      Map<Integer, String> values = new HashMap<Integer, String>();
      String instructions = item.getInstruction();
      List<String> formulaNames = this.extractFormulas(instructions);
      for (int i = 0; i < formulaNames.size(); i++) {
          String formulaName = formulaNames.get(i);
          String longFormula = replaceFormulaNameWithFormula(item, formulaName); // {a}+{b}|0.1,1
          longFormula = defaultVarianceAndDecimal(longFormula); // sets defaults, in case tolerance or precision isn't set
          
          String formula = getAnswerExpression(longFormula); // returns just the formula
          String answerData = getAnswerData(longFormula); // returns just tolerance and precision
          int decimalPlaces = getAnswerDecimalPlaces(answerData);
          
          String substitutedFormula = replaceMappedVariablesWithNumbers(formula,variables);
          SamigoExpressionParser parser = new SamigoExpressionParser(); // this will turn the expression into a number in string form
          String numericAnswerString;
          try {
              numericAnswerString = parser.parse(substitutedFormula);
              if (this.isAnswerValid(numericAnswerString)) {
                  String displayAnswer = applyPrecisionToNumberString(numericAnswerString, decimalPlaces);                
                  values.put(i + 1, displayAnswer + answerData); // later answerData will be used for scoring
              } else {
                  throw new Exception("invalid answer, try again");
              }
          } catch (SamigoExpressionError e) {
              throw e;
          }          
      }
      return values;
  }
  
  /**
   * CALCULATED_QUESTION
   * This is a busy method. It does three things:
   * <br>1. It removes the answer expressions ie. {{x+y}} from the question text. This value is
   *    returned in the ArrayList texts. This format is necessary so that input boxes can be
   *    placed in the text where the {{..}}'s appear.
   * <br>2. It will call methods to swap out the defined variables with randomly generated values
   *    within the ranges defined by the user.
   * <br>3. It updates the HashMap answerList with the calculated answers in sequence. It will 
   *    parse and calculate what each answer needs to be.
   * <p>Note: If a divide by zero occurs. We change the random values and try again. It gets limited chances to
   *    get valid values and then will return "infinity" as the answer.
   * @param answerList will enter the method empty and be filled with sequential answers to the question
   * @return ArrayList of the pieces of text to display surrounding input boxes
   */
  public ArrayList extractCalcQAnswersArray(HashMap answerList, ItemDataIfc item, Long gradingId, String agentId) {
      final int MAX_ERROR_TRIES = 100;
      boolean hasErrors = true;
      Map<String, String> variableRangeMap = buildVariableRangeMap(item);
      List<String> instructionSegments = new ArrayList<String>();
      
      int attemptCount = 1;
      while (hasErrors && attemptCount <= MAX_ERROR_TRIES) {
          instructionSegments.clear();
          Map<String, String> variablesWithValues = determineRandomValuesForRanges(variableRangeMap,item.getItemId(), gradingId, agentId, attemptCount);
          String instructions = item.getInstruction();
          String instructionsWithSubstitutions  = replaceMappedVariablesWithNumbers(instructions, variablesWithValues);
          
          instructionSegments = extractInstructionSegments(instructionsWithSubstitutions);
          try {
              Map<Integer, String> evaluatedFormulas = calculateFormulaValues(variablesWithValues, item);
              answerList.putAll(evaluatedFormulas);
              hasErrors = false;
          } catch (Exception e) {
              attemptCount++;
          }
      }
      return (ArrayList) instructionSegments;
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
      String answerData = allAnswerText.substring(allAnswerText.indexOf("|"), allAnswerText.length());
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
	  String answerExpression = allAnswerText.substring(0, allAnswerText.indexOf("|"));
	  return answerExpression;
  }

  /**
   * CALCULATED_QUESTION
   * Default acceptable variance and decimalPlaces. An asnwer is defined by an expression
   * such as {x+y|1,2} if the variance and decimal places are left off. We have to default
   * them to something.
   */
  private String defaultVarianceAndDecimal(String allAnswerText) {
	  String defaultVariance = "0.001";
	  String defaultDecimal = "3";
	  
	  if (!allAnswerText.contains("|")) {
		  if (!allAnswerText.contains(","))
			  allAnswerText = allAnswerText.concat("|"+defaultVariance+","+defaultDecimal);
		  else
			  allAnswerText = allAnswerText.replace(",","|"+defaultVariance+",");
      }
	  if (!allAnswerText.contains(","))
		  allAnswerText = allAnswerText.concat(","+defaultDecimal);
	  
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
	  if (answer.equals(NaN)) return false;	  
	  return true;
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
  public String replaceMappedVariablesWithNumbers(String expression, Map<String, 
          String> variables) {
      
      if (expression == null) {
          expression = "";
      }
        
      if (variables == null) {
          variables = new HashMap<String, String>();
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
   * Takes a map of ranges and randomly chooses values for those ranges and stores them in a new map.
   */
   public Map<String, String> determineRandomValuesForRanges(Map<String, String> variableRangeMap, long itemId, long gradingId, String agentId, int validAnswersAttemptCount) {
	  Map<String, String> variableValueMap = new HashMap();
	  
	  // seed random number generator
	  long seed = getCalcuatedQuestionSeed(itemId, gradingId, agentId, validAnswersAttemptCount);
	  Random generator = new Random(seed);
	  
	  Iterator i = variableRangeMap.entrySet().iterator();
	  while(i.hasNext())
	  {
		  Map.Entry<String, String>entry = (Map.Entry)i.next();
		  
		  String delimRange = entry.getValue().toString(); // ie. "-100|100,2"
		  		  
		  float minVal = Float.valueOf(delimRange.substring(0, delimRange.indexOf('|')));
		  float maxVal = Float.valueOf(delimRange.substring(delimRange.indexOf('|')+1, delimRange.indexOf(',')));
		  int decimalPlaces = Integer.valueOf(delimRange.substring(delimRange.indexOf(',')+1, delimRange.length()));
		  		  
		  // This line does the magic of creating the random variable value within the range.
		  Double randomValue = minVal + (maxVal - minVal) * generator.nextDouble();
		  
		  // Trim off excess decimal points based on decimalPlaces value
		  BigDecimal bd = new BigDecimal(randomValue);
		  bd = bd.setScale(decimalPlaces,BigDecimal.ROUND_HALF_UP);
		  randomValue = bd.doubleValue();
		  
		  String displayNumber = randomValue.toString();
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
  private HashMap buildVariableRangeMap(ItemDataIfc item) {
	  HashMap variableRangeMap = new HashMap();
	  
	  String instructions = item.getInstruction();
	  List<String> variables = this.extractVariables(instructions);
	  
      // Loop through each VarName
	  List<ItemTextIfc> itemTextList = item.getItemTextArraySorted();
	  for (ItemTextIfc varName : itemTextList) {
		  // only look at variables for substitution, ignore formulas
		  if (variables.contains(varName.getText())) {
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
  private boolean isCalcQuestion(List tempItemGradinglist, HashMap publishedItemHash) {
	  if (tempItemGradinglist == null) return false;
	  if (tempItemGradinglist.size() == 0) return false;
	  
	  Iterator iter = tempItemGradinglist.iterator();
	  ItemGradingData itemCheck = (ItemGradingData) iter.next();
	  Long itemId = itemCheck.getPublishedItemId();
      ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
      if (item.getTypeId().equals(TypeIfc.CALCULATED_QUESTION)) {
    	  return true;
      }
          
      return false;
  }

  
  public ArrayList getHasGradingDataAndHasSubmission(Long publishedAssessmentId) {
	  ArrayList al = new ArrayList();
	    try {
	    	al = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getHasGradingDataAndHasSubmission(publishedAssessmentId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return al;
  }
  
  public String getFileName(Long itemGradingId, String agentId, String filename) {
	  String name = "";
	    try {
	    	name = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getFilename(itemGradingId, agentId, filename);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return name;
  }
  
  public List getUpdatedAssessmentList(String agentId, String siteId) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getUpdatedAssessmentList(agentId, siteId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return list;
  }
  
  public List getSiteNeedResubmitList(String siteId) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getSiteNeedResubmitList(siteId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return list;
  }
  
  public void autoSubmitAssessments() {
	  try {
		  PersistenceService.getInstance().
		  getAssessmentGradingFacadeQueries().autoSubmitAssessments();
	  } catch (Exception e) {
		  e.printStackTrace();
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
			e.printStackTrace();
		}
		return attachment;
	}

  public void removeItemGradingAttachment(String attachmentId) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .removeItemGradingAttachment(Long.valueOf(attachmentId));
  }

  public void saveOrUpdateAttachments(List list) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .saveOrUpdateAttachments(list);
  }
  
  public HashMap getInProgressCounts(String siteId)  {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      getInProgressCounts(siteId);
  }
  
  public HashMap getSubmittedCounts(String siteId)  {
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
  public float getAnswerScoreMCQ(ItemGradingData data, HashMap publishedAnswerHash)
  {
	  AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
	  if (answer == null || answer.getScore() == null) {
		  return 0f;
	  }
	  else if (answer.getIsCorrect().booleanValue()){ // instead of using answer score Item score needs to be used here 
		  return (answer.getItem().getScore().floatValue()); //--mustansar 
	  }
	  return (answer.getItem().getScore().floatValue()*answer.getPartialCredit().floatValue())/100f;
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
		ArrayList<AnswerIfc> answers = itemText.getAnswerArray();
		Iterator<AnswerIfc> answerIter = answers.iterator();
		while (answerIter.hasNext()) {
			AnswerIfc answer = answerIter.next();
			if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue()) {
				hasCorrectAnswer = true;
				break;
			}
		}
		return !hasCorrectAnswer;	  
	}
}


