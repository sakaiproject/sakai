/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/assessment/PublishedAssessmentService.java $
 * $Id: PublishedAssessmentService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
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


package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;

/**
 * The QuestionPoolService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class PublishedAssessmentService {
  private static Log log = LogFactory.getLog(PublishedAssessmentService.class);

  /**
   * Creates a new QuestionPoolService object.
   */
  public PublishedAssessmentService() {
  }

  /**
   * rachelgollub: So takeable is that you have *not* reached the number of
   * submissions and you're either before the due date or (you're after the due
   * date, you haven't submitted yet, and late handling is enabled).
   * - quoted from IM on 1/31/05
   * Marc said some of teh assessment do not have any due date, e.g. survey
   */
  public ArrayList getBasicInfoOfAllPublishedAssessments(String agentId, String orderBy,
                                             boolean ascending) {

    // 2. get all takeable assessment available
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllPublishedAssessments(orderBy, ascending,
                                             PublishedAssessmentFacade.
                                             ACTIVE_STATUS);
  }

/**
  public ArrayList getAllReviewableAssessments(String agentId, String orderBy,
                                               boolean ascending) {

    // 1. get total no. of submission per assessment by the given agent
    HashMap h = getTotalSubmissionPerAssessment(agentId);

    ArrayList assessmentList = PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getAllReviewableAssessments(orderBy, ascending);
             assessmentList.size());
    ArrayList reviewableAssessmentList = new ArrayList();
    for (int i = 0; i < assessmentList.size(); i++) {
      AssessmentGradingFacade f = (AssessmentGradingFacade) assessmentList.get(
          i);

      Integer NumberOfSubmissions = (Integer) h.get(
          f.getPublishedAssessment().getPublishedAssessmentId());
      if (NumberOfSubmissions == null) {
        NumberOfSubmissions = new Integer(0);
      }
      try {
        if (!PersistenceService.getInstance().getAuthzQueriesFacade().isAuthorized(null, "VIEW_PUBLISHED_ASSESSMENT",
                                              f.getPublishedAssessment().
                                              getPublishedAssessmentId().
                                              toString())) {
          break;
        }
      }
      catch (Exception e1) {
        log.fatal("Wrapping Error around unhandled Exception: "
                  + e1.getMessage());
        throw new RuntimeException(e1.getMessage());
      }
      // for testing only
      reviewableAssessmentList.add(f);
    }
    return reviewableAssessmentList;
  }
*/

  public ArrayList getAllActivePublishedAssessments(String orderBy) {
    return getAllPublishedAssessments(orderBy,
                                      PublishedAssessmentFacade.ACTIVE_STATUS);
  }

  public ArrayList getAllActivePublishedAssessments(
      int pageSize, int pageNumber, String orderBy) {
    return getAllPublishedAssessments(
        pageSize, pageNumber, orderBy, PublishedAssessmentFacade.ACTIVE_STATUS);
  }

  public ArrayList getAllInActivePublishedAssessments(String orderBy) {
    return getAllPublishedAssessments(orderBy,
                                      PublishedAssessmentFacade.INACTIVE_STATUS);
  }

  public ArrayList getAllInActivePublishedAssessments(
      int pageSize, int pageNumber, String orderBy) {
    return getAllPublishedAssessments(
        pageSize, pageNumber, orderBy,
        PublishedAssessmentFacade.INACTIVE_STATUS);
  }

  public ArrayList getAllPublishedAssessments(String orderBy, Integer status) {
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        getAllPublishedAssessments(orderBy, status); // signalling all & no paging
  }

  public ArrayList getAllPublishedAssessments(
      int pageSize, int pageNumber, String orderBy, Integer status) {
    try {
      if (pageSize > 0 && pageNumber > 0) {
        return PersistenceService.getInstance().
            getPublishedAssessmentFacadeQueries().
            getAllPublishedAssessments(pageSize, pageNumber, orderBy, status);
      }
      else {
        return PersistenceService.getInstance().
            getPublishedAssessmentFacadeQueries().
            getAllPublishedAssessments(orderBy, status);
      }
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public PublishedAssessmentFacade getPublishedAssessment(String assessmentId) {
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          getPublishedAssessment(new Long(assessmentId));
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public Long getPublishedAssessmentId(String assessmentId) {
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          getPublishedAssessmentId(new Long(assessmentId));
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public PublishedAssessmentFacade publishAssessment(AssessmentFacade
      assessment) throws Exception {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          publishAssessment(assessment);
  }

  public PublishedAssessmentFacade publishPreviewAssessment(AssessmentFacade
      assessment) {
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          publishPreviewAssessment(assessment);
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public void saveAssessment(PublishedAssessmentFacade assessment) {
    try{
      PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        saveOrUpdate(assessment);
    }
    catch (Exception e) {
      log.error(e);
    }
  }

  public void removeAssessment(String assessmentId) {
    PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        removeAssessment(new Long(assessmentId));
  }

  public ArrayList getBasicInfoOfAllActivePublishedAssessments(String orderBy,boolean ascending) {
    String siteAgentId = AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllActivePublishedAssessments(orderBy, siteAgentId, ascending); // signalling all & no paging
  }

  public ArrayList getBasicInfoOfAllInActivePublishedAssessments(String orderBy,boolean ascending) {
    String siteAgentId = AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllInActivePublishedAssessments(orderBy, siteAgentId, ascending); // signalling all & no paging
  }

  public PublishedAssessmentFacade getSettingsOfPublishedAssessment(String
      assessmentId) {
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          getSettingsOfPublishedAssessment(new Long(assessmentId));
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public PublishedItemData loadPublishedItem(String itemId) {
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          loadPublishedItem(new Long(itemId));
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  public PublishedItemText loadPublishedItemText(String itemTextId) {
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          loadPublishedItemText(new Long(itemTextId));
    }
    catch (Exception e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * return an array list of the last AssessmentGradingFacade per assessment that
   * a user has submitted for grade.
   * @param agentId
   * @param orderBy
   * @param ascending
   * @return
   */
  public ArrayList getBasicInfoOfLastSubmittedAssessments(String agentId,
      String orderBy, boolean ascending) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
         getBasicInfoOfLastSubmittedAssessments(agentId, orderBy, ascending);
  }

  /** total submitted for grade
   * returns HashMap (Long publishedAssessmentId, Integer totalSubmittedForGrade);
   */
  public HashMap getTotalSubmissionPerAssessment(String agentId) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getTotalSubmissionPerAssessment(agentId);
  }

  public Integer getTotalSubmission(String agentId, String publishedAssessmentId) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getTotalSubmission(agentId, new Long(publishedAssessmentId));
  }

  public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(String alias) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getPublishedAssessmentIdByAlias(alias);
  }

  public void saveOrUpdateMetaData(PublishedMetaData meta) {
   PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        saveOrUpdateMetaData(meta);
  }

  public HashMap getFeedbackHash(){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
         getFeedbackHash();
  }
  public HashMap getAllAssessmentsReleasedToAuthenticatedUsers(){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
         getAllAssessmentsReleasedToAuthenticatedUsers();
  }

  public String getPublishedAssessmentOwner(Long publishedAssessmentId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
         getPublishedAssessmentOwner(publishedAssessmentId.toString());
  }

 public boolean publishedAssessmentTitleIsUnique(String assessmentBaseId, String title) {
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
           publishedAssessmentTitleIsUnique(new Long(assessmentBaseId), title);
  }

  public boolean hasRandomPart(String publishedAssessmentId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	  hasRandomPart(new Long(publishedAssessmentId));
  }

  public PublishedItemData getFirstPublishedItem(String publishedAssessmentId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getFirstPublishedItem(new Long(publishedAssessmentId));
  }

  public List getPublishedItemIds(String publishedAssessmentId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getPublishedItemIds(new Long(publishedAssessmentId));

  }

  public Integer getItemType(String publishedItemId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getItemType(new Long(publishedItemId));
  }

  public HashMap preparePublishedItemTextHash(PublishedAssessmentIfc publishedAssessment){
    HashMap map = new HashMap();
    ArrayList sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
      ArrayList itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = (ItemDataIfc)itemArray.get(j);
        ArrayList itemTextArray = item.getItemTextArray();
        for (int k=0;k<itemTextArray.size(); k++){
          ItemTextIfc itemText = (ItemTextIfc)itemTextArray.get(k);
          map.put(itemText.getId(), itemText);
        }
      }
    }
    return map;
  }


  public HashMap preparePublishedItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap map = new HashMap();
    ArrayList sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
      ArrayList itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = (ItemDataIfc)itemArray.get(j);
        map.put(item.getItemId(), item);
      }
    }
    return map;
  }

  public HashMap preparePublishedAnswerHash(PublishedAssessmentIfc publishedAssessment){
    HashMap map = new HashMap();
    ArrayList sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
      ArrayList itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = (ItemDataIfc)itemArray.get(j);
        ArrayList itemTextArray = item.getItemTextArray();
        for (int k=0;k<itemTextArray.size(); k++){
          ItemTextIfc itemText = (ItemTextIfc)itemTextArray.get(k);
          ArrayList answerArray = itemText.getAnswerArraySorted();
          for (int m=0;m<answerArray.size(); m++){
            AnswerIfc answer = (AnswerIfc)answerArray.get(m);
            map.put(answer.getId(), answer);
	  }  
        }
      }
    }
    return map;
  }

  public HashMap prepareFIBItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap map = new HashMap();
    ArrayList sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
      ArrayList itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = (ItemDataIfc)itemArray.get(j);
        if (item.getTypeId().equals(new Long(8))) // FIB question
          map.put(item.getItemId(), item);
      }
    }
    return map;
  }

  public HashMap prepareMCMRItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap map = new HashMap();
    ArrayList sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
      ArrayList itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = (ItemDataIfc)itemArray.get(j);
        if (item.getTypeId().equals(new Long(2))) // MCMR question
          map.put(item.getItemId(), item);
      }
    }
    return map;
  }

  public HashSet getSectionSetForAssessment(Long publishedAssessmentId){
	    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	    getSectionSetForAssessment(publishedAssessmentId);
  }
}
