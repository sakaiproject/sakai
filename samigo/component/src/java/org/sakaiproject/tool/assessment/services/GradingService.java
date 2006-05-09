/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

/**
 * The GradingService calls the back end to get grading information from
 * the database.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class GradingService
{
  private static Log log = LogFactory.getLog(GradingService.class);

  /**
   * Get all scores for a published assessment from the back end.
   */
  public ArrayList getTotalScores(String publishedId, String which)
  {
    ArrayList results = null;
    try {
      results =
        (ArrayList) PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getTotalScores(publishedId,
             which);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

 /**
  * Get all submissions for a published assessment from the back end.
  */
  public ArrayList getAllSubmissions(String publishedId)
  {
    ArrayList results = null;
    try {
      results =
        (ArrayList) PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllSubmissions(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  private ArrayList getHighestAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        (ArrayList) PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestAssessmentGradingList(publishedId);
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
        (ArrayList) PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastAssessmentGradingList(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }


  public void saveTotalScores(ArrayList gdataList, PublishedAssessmentIfc pub)
  {
      //System.out.println("**** GradingService: saveTotalScores");
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
      }

      // no need to notify gradebook if this submission is not for grade
      // we only want to notify GB when there are changes
      ArrayList newList = getAssessmentGradingsByScoringType(
        scoringType, gdata.getPublishedAssessmentId());
      ArrayList l = getListForGradebookNotification(newList, oldList);
      
      notifyGradebook(l, pub);
      //}
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error(e);
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
        if (!a.getFinalScore().equals(b.getFinalScore()))
          l.add(a);
      }
    }
    return l;
  }


  private ArrayList getAssessmentGradingsByScoringType(
       Integer scoringType, Long publishedAssessmentId){
    ArrayList l = new ArrayList();
    // get the list of highest score
    if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
      l = getHighestAssessmentGradingList(publishedAssessmentId);
    }
    // get the list of last score
    else {
      l = getLastAssessmentGradingList(publishedAssessmentId);
    }
    return l;
  }

  private Integer getScoringType(PublishedAssessmentIfc pub){
    Integer scoringType = null;
    EvaluationModelIfc e = pub.getEvaluationModel();
    if ( e!=null ){
      scoringType = e.getScoringType();
    }
    return scoringType;
  }

  private boolean updateGradebook(AssessmentGradingIfc data, PublishedAssessmentIfc pub){
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

  /**
   * Get the last set of itemgradingdata for a student per assessment
   */
  public HashMap getLastItemGradingData(String publishedId, String agentId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getLastItemGradingData(new Long(publishedId), agentId);
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
  public HashMap getSubmitData(String publishedId, String agentId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getSubmitData(new Long(publishedId), agentId);
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
          .getSubmissionSizeOfPublishedAssessment(new Long(
          publishedAssessmentId));
    } catch(Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  public HashMap getSubmissionSizeOfAllPublishedAssessments()  {
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getSubmissionSizeOfAllPublishedAssessments();
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
        getMedia(new Long(mediaId));
  }

  public ArrayList getMediaArray(String itemGradingId){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(new Long(itemGradingId));
  }

  public ArrayList getMediaArray(ItemGradingData i){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(i);
  }

  public ItemGradingData getLastItemGradingDataByAgent(String publishedItemId, String agentId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastItemGradingDataByAgent(new Long(publishedItemId), agentId);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public ItemGradingData getItemGradingData(String assessmentGradingId, String publishedItemId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGradingData(new Long(assessmentGradingId), new Long(publishedItemId));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public AssessmentGradingData load(String assessmentGradingId) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          load(new Long(assessmentGradingId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastAssessmentGradingByAgentId(new Long(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastSavedAssessmentGradingByAgentId(new Long(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public void saveItemGrading(ItemGradingIfc item)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveItemGrading(item);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAssessmentGrading(assessment);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List getAssessmentGradingIds(String publishedItemId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
         getAssessmentGradingIds(new Long(publishedItemId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentGradingIfc getHighestAssessmentGrading(String publishedAssessmentId, String agentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	      getHighestAssessmentGrading(new Long(publishedAssessmentId), agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public Set getItemGradingSet(String assessmentGradingId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getItemGradingSet(new Long(assessmentGradingId));
    }
    catch(Exception e){
      log.error(e); throw new Error(e);
    }
  }

  public HashMap getAssessmentGradingByItemGradingId(String publishedAssessmentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getAssessmentGradingByItemGradingId(new Long(publishedAssessmentId));
    }
    catch(Exception e){
      log.error(e); throw new Error(e);
    }
  }

    /*
  public void saveItemScores(ArrayList data, HashMap assessmentGradingHash, PublishedAssessmentIfc pub) {
    try {
      Iterator iter = data.iterator();
      while (iter.hasNext())
      {
        ItemGradingData gdata = (ItemGradingData) iter.next();
        if (gdata.getItemGradingId() == null)
          gdata.setItemGradingId(new Long(0));
        if (gdata.getPublishedItemTextId().longValue() <=0)
        {
          //log.debug("Didn't save -- error in item.");
        }
        else
        {  
          AssessmentGradingData a = (AssessmentGradingData) assessmentGradingHash.get(gdata.getItemGradingId());
          a.setItemGradingSet(getItemGradingSet(a.getAssessmentGradingId().toString()));

          Iterator iter2 = a.getItemGradingSet().iterator();
          while (iter2.hasNext())
          {
            ItemGradingData idata = (ItemGradingData) iter2.next();
            if (idata.getItemGradingId().equals(gdata.getItemGradingId()))
            {
              a.getItemGradingSet().remove(idata);
              a.getItemGradingSet().add(gdata);
              break;
            }
          }

          // Now we can move on.
          saveItemGrading(gdata);
          storeGrades(load(gdata.getAssessmentGradingId().toString()), true, pub);
        }
      }
    } 
    catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
  }
    */

  public void updateItemScore(ItemGradingData gdata, float scoreDifference, PublishedAssessmentIfc pub){
    try {
      AssessmentGradingData adata = load(gdata.getAssessmentGradingId().toString());
      adata.setItemGradingSet(getItemGradingSet(adata.getAssessmentGradingId().toString()));

      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      float totalAutoScore = 0;
      float totalOverrideScore = adata.getTotalOverrideScore().floatValue();
      while (iter.hasNext()){
        ItemGradingIfc i = (ItemGradingIfc)iter.next();
        if (i.getItemGradingId().equals(gdata.getItemGradingId())){
	  i.setAutoScore(gdata.getAutoScore());
          i.setComments(gdata.getComments());
	}
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore().floatValue();
      }

      adata.setTotalAutoScore(new Float(totalAutoScore));
      adata.setFinalScore(new Float(totalAutoScore+totalOverrideScore));
      saveOrUpdateAssessmentGrading(adata);
      if (scoreDifference != 0){
        notifyGradebookByScoringType(adata, pub);
      }
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error(e);
    }
  }

  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingIfc data, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash) 
  {
    storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash);
  }

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  public void storeGrades(AssessmentGradingIfc data, boolean regrade, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash) 
         throws GradebookServiceException {
    System.out.println("****x1. regrade ="+regrade+" "+(new Date()).getTime());
    try {
      String agent = data.getAgentId();
      if (!regrade)
      {
	data.setSubmittedDate(new Date());
        setIsLate(data, pub);
      }
      // note that this itemGradingSet is a partial set of answer submitted. it contains only 
      // newly submitted answers, updated answers and MCMR/FIB answers ('cos we need the old ones to
      // calculate scores for new ones)
      Set itemGradingSet = data.getItemGradingSet();
      if (itemGradingSet == null)
        itemGradingSet = new HashSet();
      Iterator iter = itemGradingSet.iterator();

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
      System.out.println("****x2. "+(new Date()).getTime());
      while(iter.hasNext())
      {
        ItemGradingIfc itemGrading = (ItemGradingIfc) iter.next();
        Long itemId = itemGrading.getPublishedItemId();
        ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
        Long itemType = item.getTypeId();
        float autoScore = (float) 0;
        if (!regrade)
        {
          itemGrading.setAssessmentGradingId(data.getAssessmentGradingId());
          itemGrading.setSubmittedDate(new Date());
          itemGrading.setAgentId(agent);
          itemGrading.setOverrideScore(new Float(0));
          // note that totalItems & fibAnswersMap would be modified by the following method
          autoScore = getScoreByQuestionType(itemGrading, item, itemType, publishedItemTextHash, 
                                 totalItems, fibAnswersMap, publishedAnswerHash);
          //System.out.println("**!regrade, autoScore="+autoScore);
          totalItems.put(itemId, new Float(autoScore));
	}
        else{
          autoScore = itemGrading.getAutoScore().floatValue();
          //overridescore - cwen
          if (itemGrading.getOverrideScore() != null){
            autoScore += itemGrading.getOverrideScore().floatValue();
          }

          if(!totalItems.containsKey(itemId)){
            totalItems.put(itemId, new Float(autoScore));
          }
          else{
            float accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
            accumelateScore += autoScore;
            totalItems.put(itemId, new Float(accumelateScore));
          }
        }
        itemGrading.setAutoScore(new Float(autoScore));
      }

      System.out.println("****x3. "+(new Date()).getTime());
      // what does the following address? daisyf
      iter = itemGradingSet.iterator();
      while(iter.hasNext())
      {
        ItemGradingIfc itemGrading = (ItemGradingIfc) iter.next();
        Long itemId = itemGrading.getPublishedItemId();
        float autoScore = (float) 0;

        float eachItemScore = ((Float) totalItems.get(itemId)).floatValue();
        if(eachItemScore < 0)
        {
          itemGrading.setAutoScore(new Float(0));
        }
      }
      System.out.println("****x4. "+(new Date()).getTime());

      // save#1: this itemGrading Set is a partial set of answers submitted. it contains new answers and
      // updated old answers and FIB answers ('cos we need the old answer to calculate the score for new
      // ones). we need to be cheap, we don't want to update record that hasn't been
      // changed. Yes, assessmentGrading's total score will be out of sync at this point, I am afraid. It
      // would be in sync again once the whole method is completed sucessfully. 
      saveOrUpdateAll(itemGradingSet);
      System.out.println("****x5. "+(new Date()).getTime());

      // save#2: now, we need to get the full set so we can calculate the total score accumulate for the
      // whole assessment.
      Set fullItemGradingSet = getItemGradingSet(data.getAssessmentGradingId().toString());
      float totalAutoScore = getTotalAutoScore(fullItemGradingSet);
      data.setTotalAutoScore(new Float(totalAutoScore));
      //System.out.println("**#1 total AutoScore"+totalAutoScore);
      data.setFinalScore(new Float(totalAutoScore + data.getTotalOverrideScore().floatValue()));
      System.out.println("****x6. "+(new Date()).getTime());
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error(e);
    }

    // save#3: itemGradingSet has been saved above so just need to update assessmentGrading
    // therefore setItemGradingSet as empty first - daisyf
    data.setItemGradingSet(new HashSet());
    saveOrUpdateAssessmentGrading(data);
    System.out.println("****x7. "+(new Date()).getTime());

    notifyGradebookByScoringType(data, pub);
    System.out.println("****x8. "+(new Date()).getTime());
    //System.out.println("**#2 total AutoScore"+data.getTotalAutoScore());
  }

  private float getTotalAutoScore(Set itemGradingSet){
      //System.out.println("*** no. of itemGrading="+itemGradingSet.size());
    float totalAutoScore =0;
    Iterator iter = itemGradingSet.iterator();
    while (iter.hasNext()){
      ItemGradingIfc i = (ItemGradingIfc)iter.next();
      //System.out.println(i.getItemGradingId()+"->"+i.getAutoScore());
      if (i.getAutoScore()!=null)
	totalAutoScore += i.getAutoScore().floatValue();
    }
    return totalAutoScore;
  }

  private void notifyGradebookByScoringType(AssessmentGradingIfc data, PublishedAssessmentIfc pub){
    Integer scoringType = pub.getEvaluationModel().getScoringType();
    if (updateGradebook(data, pub)){
      AssessmentGradingIfc d = data; // data is the last submission
      // need to decide what to tell gradebook
      if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE))
        d = getHighestAssessmentGrading(pub.getPublishedAssessmentId().toString(), data.getAgentId());
      notifyGradebook(d, pub);
    }
  }

  private float getScoreByQuestionType(ItemGradingIfc itemGrading, ItemDataIfc item,
                                       Long itemType, HashMap publishedItemTextHash, 
                                       HashMap totalItems, HashMap fibAnswersMap,
                                       HashMap publishedAnswerHash){
    float score = (float) 0;
    float initScore = (float) 0;
    float autoScore = (float) 0;
    float accumelateScore = (float) 0;
    Long itemId = item.getItemId();
    int type = itemType.intValue();
    switch (type){ 
      case 1: // MC Single Correct
      case 3: // MC Survey
      case 4: // True/False 
              autoScore = getAnswerScore(itemGrading, publishedAnswerHash);
              //overridescore
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
	      totalItems.put(itemId, new Float(autoScore));
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
              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, new Float(autoScore));
              else{
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, new Float(accumelateScore));
              }
              break;

      case 9: // Matching     
              initScore = getAnswerScore(itemGrading, publishedAnswerHash);
              if (initScore > 0)
                autoScore = initScore / ((float) item.getItemTextSet().size());
              //overridescore?
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, new Float(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, new Float(accumelateScore));
              }
              break;

      case 8: // FIB
              autoScore = getFIBScore(itemGrading, fibAnswersMap, item, publishedAnswerHash) / (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
              //overridescore - cwen
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, new Float(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, new Float(accumelateScore));
              }
              break;

      case 5: // SAQ
      case 6: // file upload
      case 7: // audio recording
              //overridescore - cwen
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, new Float(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, new Float(accumelateScore));
              }
              break;
    }
    return autoScore;
  }

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingIfc for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public float getAnswerScore(ItemGradingIfc data, HashMap publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null)
      return (float) 0;
    if (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())
      return (float) 0;
    return answer.getScore().floatValue();
  }

  public void notifyGradebook(AssessmentGradingIfc data, PublishedAssessmentIfc pub) throws GradebookServiceException {
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

    if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)
        && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
        if(log.isDebugEnabled()) log.debug("Attempting to update a score in the gradebook");

    // add retry logic to resolve deadlock problem while sending grades to gradebook

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        /* for testing the catch block 
        if (retryCount >2)
          throw new Exception();
        */
        gbsHelper.updateExternalAssessmentScore(data, g);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem sending grades to gradebook: "+e.getMessage());
        log.warn("retrying...sending grades to gradebook. ");
        String errorMessage = e.getMessage();
          log.warn("retry....");
          retryCount--;
          try {
            int deadlockInterval = PersistenceService.getInstance().getDeadlockInterval().intValue();
            Thread.currentThread().sleep(deadlockInterval);
          }
          catch(InterruptedException ex){
            log.warn(ex.getMessage());
          }
         if (retryCount==0) {
            // after retries, still failed updating gradebook
            log.warn("After all retries, still failed ...  Now throw error to UI");
            throw new GradebookServiceException(e);
         }
      }
    }

////

/*
        try {
            gbsHelper.updateExternalAssessmentScore(data, g);
        } catch (Exception e) {
            // Got GradebookException from gradebook tool 
            e.printStackTrace();
            throw new GradebookServiceException(e);

        }
*/
    } else {
       if(log.isDebugEnabled()) log.debug("Not updating the gradebook.  toGradebook = " + toGradebook);
    }
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
  private float getFIBScore(ItemGradingIfc data, HashMap fibmap, ItemDataIfc itemdata, HashMap publishedAnswerHash)
  {
    String studentanswer = "";
    String REGEX;
    Pattern p;
    Matcher m;
    boolean matchresult = false;

    String answertext = ((AnswerIfc)publishedAnswerHash.get(data.getPublishedAnswerId())).getText();
    Long itemId = itemdata.getItemId();

    String casesensitive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB);
    String mutuallyexclusive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB);
    Set answerSet = new HashSet();

    float totalScore = (float) 0;

    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
            studentanswer= data.getAnswerText().trim();
    	    REGEX = answer.replaceAll("\\*", ".+");
            p = Pattern.compile(REGEX);   // by default it's case sensitive
            m = p.matcher(studentanswer);
            matchresult = m.matches();
          }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
    	    studentanswer= data.getAnswerText().trim();
            REGEX = answer.replaceAll("\\*", ".+");
	    p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
            m = p.matcher(studentanswer);
            matchresult= m.matches();
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

  public float getTotalCorrectScore(ItemGradingIfc data, HashMap publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null)
      return (float) 0;
    return answer.getScore().floatValue();
  }

  private void setIsLate(AssessmentGradingIfc data, PublishedAssessmentIfc pub){
    if (pub.getAssessmentAccessControl() != null
      && pub.getAssessmentAccessControl().getDueDate() != null &&
          pub.getAssessmentAccessControl().getDueDate().before(new Date()))
          data.setIsLate(new Boolean(true));
    else
      data.setIsLate(new Boolean(false));
    if (data.getForGrade().booleanValue())
      data.setStatus(new Integer(1));
    else
      data.setStatus(new Integer(0));
    data.setTotalOverrideScore(new Float(0));
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
  public void updateAssessmentGradingScore(AssessmentGradingIfc adata, PublishedAssessmentIfc pub){
    try {
      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      float totalAutoScore = 0;
      float totalOverrideScore = adata.getTotalOverrideScore().floatValue();
      while (iter.hasNext()){
        ItemGradingIfc i = (ItemGradingIfc)iter.next();
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore().floatValue();
        }
        float oldAutoScore = adata.getTotalAutoScore().floatValue();
        float scoreDifference = totalAutoScore - oldAutoScore;
        adata.setTotalAutoScore(new Float(totalAutoScore));
        adata.setFinalScore(new Float(totalAutoScore+totalOverrideScore));
        saveOrUpdateAssessmentGrading(adata);
        if (scoreDifference != 0){
          notifyGradebookByScoringType(adata, pub);
        }
     } catch (GradebookServiceException ge) {
       ge.printStackTrace();
       throw ge;
     } catch (Exception e) {
       e.printStackTrace();
       throw new Error(e);
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
        getAssessmentGradingFacadeQueries().getPublishedAssessmentByAssessmentGradingId(new Long(id));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return pub;
  }
}


