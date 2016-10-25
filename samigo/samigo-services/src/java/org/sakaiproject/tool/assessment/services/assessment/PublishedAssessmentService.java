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


package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedSectionFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;

/**
 * The QuestionPoolService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class PublishedAssessmentService extends AssessmentService{
  private Logger log = LoggerFactory.getLogger(PublishedAssessmentService.class);

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
							 boolean ascending, String siteId) {

    // 2. get all takeable assessment available
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllPublishedAssessments(orderBy, ascending, siteId);
  }
  
  public ArrayList getBasicInfoOfAllPublishedAssessments2(String orderBy,
		  boolean ascending, String siteId) {

	  // 2. get all takeable assessment available
	  return PersistenceService.getInstance().
	  getPublishedAssessmentFacadeQueries().
	  getBasicInfoOfAllPublishedAssessments2(orderBy, ascending, siteId);
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
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public PublishedAssessmentFacade getPublishedAssessment(String assessmentId) {
    //SAM-1995 if an empty or null id is passed throw and exception
    if (StringUtils.isBlank(assessmentId)) {
      throw new IllegalArgumentException("AssesmentId must be specified");
    }
    try {
      return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().
          getPublishedAssessment(Long.valueOf(assessmentId));
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
	}

	public PublishedAssessmentFacade getPublishedAssessmentQuick(String assessmentId) {
		// SAM-1995 if an empty or null id is passed throw and exception
		if (assessmentId == null || "".equals(assessmentId)) {
			throw new IllegalArgumentException("AssesmentId must be specified");
		}
		try {
			return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
					.getPublishedAssessmentQuick(Long.valueOf(assessmentId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

  public PublishedAssessmentFacade getPublishedAssessment(String assessmentId, boolean withGroupsInfo) {
	    try {
	      return PersistenceService.getInstance().
	          getPublishedAssessmentFacadeQueries().
	          getPublishedAssessment(Long.valueOf(assessmentId), withGroupsInfo);
	    }
	    catch (Exception e) {
	      log.error(e.getMessage(), e);
	      throw new RuntimeException(e);
	    }
  }
  
  public AssessmentIfc getAssessment(Long assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getPublishedAssessmentFacadeQueries().getPublishedAssessment(assessmentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
      log.error(e.getMessage(), e);
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
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
  
  public void deleteAllSecuredIP(PublishedAssessmentIfc assessment) {
		PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
				.deleteAllSecuredIP(assessment);
	}

  public void saveAssessment(PublishedAssessmentFacade assessment) {
    try{
      PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        saveOrUpdate(assessment);
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void removeAssessment(String assessmentId) {
	  this.removeAssessment(assessmentId, null);
  }

  public void removeAssessment(String assessmentId, String action) {
	    PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	        removeAssessment(new Long(assessmentId), action);
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
      log.error(e.getMessage(), e);
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
      log.error(e.getMessage(), e);
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
      log.error(e.getMessage(), e);
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

    public HashMap getTotalSubmissionPerAssessment(String agentId, String siteId) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getTotalSubmissionPerAssessment(agentId, siteId);
  }

  public Integer getTotalSubmission(String agentId, String publishedAssessmentId) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getTotalSubmission(agentId, new Long(publishedAssessmentId));
  }

  /**
   * @param publishedAssessmentId
   * @return number of submissions
   */
  public Integer getTotalSubmissionForEachAssessment(String publishedAssessmentId){
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getTotalSubmissionForEachAssessment(new Long(publishedAssessmentId));
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
  
  public List getContainRandomPartAssessmentIds(final Collection assessmentIds){
	    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	    getContainRandomPartAssessmentIds(assessmentIds);
  }

  public PublishedItemData getFirstPublishedItem(String publishedAssessmentId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getFirstPublishedItem(new Long(publishedAssessmentId));
  }

  public List getPublishedItemIds(String publishedAssessmentId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getPublishedItemIds(new Long(publishedAssessmentId));
  }
  
  public HashSet getPublishedItemSet(Long publishedAssessmentId, Long sectionId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
    getPublishedItemSet(publishedAssessmentId, sectionId);
  }

  public Long getItemType(String publishedItemId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getItemType(new Long(publishedItemId));
  }

  public HashMap<Long, ItemTextIfc> preparePublishedItemTextHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, ItemTextIfc> map = new HashMap<Long, ItemTextIfc>();
    List<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = sectionArray.get(i);
      List<ItemDataIfc> itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = itemArray.get(j);
        List<ItemTextIfc> itemTextArray = item.getItemTextArray();
        for (int k=0;k<itemTextArray.size(); k++){
          ItemTextIfc itemText = itemTextArray.get(k);
          map.put(itemText.getId(), itemText);
        }
      }
    }
    return map;
  }

  public HashMap<Long, ItemDataIfc> preparePublishedItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
    List<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = sectionArray.get(i);
      List<ItemDataIfc> itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = itemArray.get(j);
        map.put(item.getItemId(), item);
      }
    }
    return map;
  }

  public HashMap<Long, AnswerIfc> preparePublishedAnswerHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, AnswerIfc> map = new HashMap<Long, AnswerIfc>();
    ArrayList<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
     SectionDataIfc section = sectionArray.get(i);
      ArrayList<ItemDataIfc> itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = itemArray.get(j);
        List<ItemTextIfc> itemTextArray = item.getItemTextArray();
        for (int k=0;k<itemTextArray.size(); k++){
          ItemTextIfc itemText = itemTextArray.get(k);
          List answerArray = itemText.getAnswerArraySorted();
          for (int m=0;m<answerArray.size(); m++){
            AnswerIfc answer = (AnswerIfc)answerArray.get(m);
            // SAK-14820: Sync with the scores from item. 
            if (answer != null) {
            	// added following condition as this doesn't make sense for EMI questions
            	if (!item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {
                	answer.setScore(item.getScore());
                	answer.setDiscount(item.getDiscount());
            	}
            	map.put(answer.getId(), answer);
            }
            
	  }  
        }
      }
    }
    return map;
  }

  public HashMap<Long, ItemDataIfc> prepareFIBItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
    ArrayList<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = sectionArray.get(i);
      ArrayList<ItemDataIfc> itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = itemArray.get(j);
        if (item.getTypeId().equals( Long.valueOf(8))) // FIB question
          map.put(item.getItemId(), item);
      }
    }
    return map;
  }

  public HashMap<Long, ItemDataIfc> prepareFINItemHash(PublishedAssessmentIfc publishedAssessment){
	    HashMap<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
	    ArrayList<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
	    for (int i=0;i<sectionArray.size(); i++){
	      SectionDataIfc section = sectionArray.get(i);
	      ArrayList<ItemDataIfc> itemArray = section.getItemArray();
	      for (int j=0;j<itemArray.size(); j++){
	        ItemDataIfc item = itemArray.get(j);
	        if (item.getTypeId().equals( Long.valueOf(11))) // FIN question
	          map.put(item.getItemId(), item);
	      }
	    }
	    return map;
  }

  /**
   * CALCULATED_QUESTION
   * @param publishedAssessment
   * @return the map of item id -> item for calc questions in this map
   */
  public Map<Long, ItemDataIfc> prepareCalcQuestionItemHash(PublishedAssessmentIfc publishedAssessment){
      // CALCULATED_QUESTION
      Map<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
      List<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
      for (int i=0;i<sectionArray.size(); i++) {
          SectionDataIfc section = sectionArray.get(i);
          List<ItemDataIfc> itemArray = section.getItemArray();
          for (int j=0;j<itemArray.size(); j++) {
              ItemDataIfc item = itemArray.get(j);
              if (item.getTypeId().equals(TypeIfc.CALCULATED_QUESTION)) { // CALCULATED_QUESTION
                  map.put(item.getItemId(), item);
              }
          }
      }
      return map;
  }
  
  /**
   * IMAGEMAP_QUESTION
   * @param publishedAssessment
   * @return the map of item id -> item for image map questions in this map
   */
  public Map<Long, ItemDataIfc> prepareImagQuestionItemHash(PublishedAssessmentIfc publishedAssessment){
      // CALCULATED_QUESTION
      Map<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
      List<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
      for (int i=0;i<sectionArray.size(); i++) {
          SectionDataIfc section = sectionArray.get(i);
          List<ItemDataIfc> itemArray = section.getItemArray();
          for (int j=0;j<itemArray.size(); j++) {
              ItemDataIfc item = itemArray.get(j);
              if (item.getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION)) { // IMAGEMAP_QUESTION
                  map.put(item.getItemId(), item);
              }
          }
      }
      return map;
  }
  
  
  public HashMap<Long, ItemDataIfc> prepareMCMRItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
    ArrayList<SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
    for (int i=0;i<sectionArray.size(); i++){
      SectionDataIfc section = sectionArray.get(i);
      ArrayList<ItemDataIfc> itemArray = section.getItemArray();
      for (int j=0;j<itemArray.size(); j++){
        ItemDataIfc item = itemArray.get(j);
        if (item.getTypeId().equals( Long.valueOf(2))) // MCMR question
          map.put(item.getItemId(), item);
      }
    }
    return map;
  }
  
  public HashMap prepareEMIItemHash(PublishedAssessmentIfc publishedAssessment){
	    HashMap<Long, ItemDataIfc> map = new HashMap<Long, ItemDataIfc>();
	    List<? extends SectionDataIfc> sectionArray = publishedAssessment.getSectionArray();
	    for (int i=0;i<sectionArray.size(); i++){
	      SectionDataIfc section = sectionArray.get(i);
	      List<ItemDataIfc> itemArray = section.getItemArray();
	      for (int j=0;j<itemArray.size(); j++){
	        ItemDataIfc item = itemArray.get(j);
	        if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) // EMI question
	          map.put(item.getItemId(), item);
	      }
	    }
	    return map;
  }
  
  public HashSet getSectionSetForAssessment(Long publishedAssessmentId){
	    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	    getSectionSetForAssessment(publishedAssessmentId);
  }
  
  public HashSet getSectionSetForAssessment(PublishedAssessmentIfc assessment){
	    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	    getSectionSetForAssessment(assessment);
}

  public boolean isRandomDrawPart(Long publishedAssessmentId, Long publishedSectionId){
	    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	    isRandomDrawPart(publishedAssessmentId, publishedSectionId);
  }

   public PublishedAssessmentData getBasicInfoOfPublishedAssessment(String publishedId) {
	    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getBasicInfoOfPublishedAssessment(new Long(publishedId));
   }

   public String getPublishedAssessmentSiteId(String publishedAssessmentId) {
	    return PersistenceService.getInstance().
       getPublishedAssessmentFacadeQueries().
       getPublishedAssessmentSiteId(publishedAssessmentId);
  }

   public Integer getPublishedItemCount(Long publishedAssessmentId) {
	    return PersistenceService.getInstance().
      getPublishedAssessmentFacadeQueries().
      getPublishedItemCount(publishedAssessmentId);
   }
   
	public Integer getPublishedSectionCount(final Long publishedAssessmentId) {
	    return PersistenceService.getInstance().
	      getPublishedAssessmentFacadeQueries().
	      getPublishedSectionCount(publishedAssessmentId);
	}
   
   
   public PublishedAttachmentData getPublishedAttachmentData(Long attachmentId) {
	    return PersistenceService.getInstance().
     getPublishedAssessmentFacadeQueries().getPublishedAttachmentData(attachmentId);
   }
   
   public void updateAssessmentLastModifiedInfo(AssessmentIfc publishedAssessmentFacade) {
	  PersistenceService.getInstance().
      getPublishedAssessmentFacadeQueries().
      updateAssessmentLastModifiedInfo((PublishedAssessmentFacade) publishedAssessmentFacade);
   }
   
   public void saveOrUpdateSection(SectionFacade section) {
	   PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().saveOrUpdateSection(section);
	}
   
   public void removeItemAttachment(String itemAttachmentId) {
	   PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().removeItemAttachment(Long.valueOf(itemAttachmentId));
	}

   public PublishedSectionFacade addSection(Long publishedAssessmentId) {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().addSection(publishedAssessmentId);
	}
   
   public PublishedSectionFacade getSection(String publishedsectionId) {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getSection(Long.valueOf(publishedsectionId));
   }  
   
   public AssessmentAccessControlIfc loadPublishedAccessControl(Long publishedAssessmentId) {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().loadPublishedAccessControl(publishedAssessmentId);
   }
   
   public void saveOrUpdatePublishedAccessControl(AssessmentAccessControlIfc publishedAccessControl) {
	   PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().saveOrUpdatePublishedAccessControl(publishedAccessControl);
   }
   
   public boolean isReleasedToGroups(String publishedAssessmentId) {
	   if (publishedAssessmentId == null) {
		   return false;
	   }
	   // PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	   PublishedAssessmentIfc pub = getPublishedAssessment(publishedAssessmentId);
	   if (pub == null) {
		   return false;
	   }
	   if (pub.getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
		   return true;
	   }
	   else {
		   return false;
	   }
   }

   public Integer getPublishedAssessmentStatus(Long publishedAssessmentId) {
	   return PersistenceService.getInstance().
	     getPublishedAssessmentFacadeQueries().getPublishedAssessmentStatus(publishedAssessmentId);
   }
   
   public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol) {
		AssessmentAttachmentIfc attachment = null;
		try {
			PublishedAssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getPublishedAssessmentFacadeQueries();
			attachment = queries.createAssessmentAttachment(assessment,
					resourceId, filename, protocol);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
   }
   
   public void removeAssessmentAttachment(String attachmentId) {
		PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
				.removeAssessmentAttachment(new Long(attachmentId));
   }
   
   public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
		   String resourceId, String filename, String protocol) {
	   SectionAttachmentIfc attachment = null;
	   try {
		   PublishedAssessmentFacadeQueriesAPI queries = PersistenceService
		           .getInstance().getPublishedAssessmentFacadeQueries();
		   attachment = queries.createSectionAttachment(section, resourceId,
				   filename, protocol);
	   } catch (Exception e) {
		   log.error(e.getMessage(), e);
	   }
	   return attachment;
   }

   public void removeSectionAttachment(String attachmentId) {
	   PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
	   .removeSectionAttachment(new Long(attachmentId));
   }

   public void saveOrUpdateAttachments(List list) {
		PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
				.saveOrUpdateAttachments(list);
   }
   
   public TreeMap getGroupsForSite() {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
	   .getGroupsForSite();
   }

   public PublishedAssessmentFacade getPublishedAssessmentInfoForRemove(Long publishedAssessmentId) {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
	   .getPublishedAssessmentInfoForRemove(publishedAssessmentId);
   }
   
   public HashMap getToGradebookPublishedAssessmentSiteIdMap() {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
	   .getToGradebookPublishedAssessmentSiteIdMap();
   }
   
   public ArrayList getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(String agentId, String siteId,boolean allAssessments ){
	   return PersistenceService.getInstance().
	   getPublishedAssessmentFacadeQueries().
	   getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(agentId, siteId, allAssessments);
   }

	public List getAllAssessmentsGradingDataByAgentAndSiteId(String agentId, String siteId) {
		return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
				.getAllAssessmentsGradingDataByAgentAndSiteId(agentId, siteId);
	}
}
