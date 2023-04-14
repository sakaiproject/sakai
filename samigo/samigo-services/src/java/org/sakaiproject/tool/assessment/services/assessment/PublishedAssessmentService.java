/*
 * Copyright (c) 2016, The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedSectionFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.util.ItemCancellationUtil;

/**
 * The QuestionPoolService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
@Slf4j
public class PublishedAssessmentService extends AssessmentService{

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
  public List<PublishedAssessmentFacade> getBasicInfoOfAllPublishedAssessments(String agentId, String orderBy, boolean ascending, String siteId) {

    // 2. get all takeable assessment available
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllPublishedAssessments(orderBy, ascending, siteId);
  }
  
  public List<PublishedAssessmentFacade> getBasicInfoOfAllPublishedAssessments2(String orderBy, boolean ascending, String siteId) {

	  // 2. get all takeable assessment available
	  return PersistenceService.getInstance().
	  getPublishedAssessmentFacadeQueries().
	  getBasicInfoOfAllPublishedAssessments2(orderBy, ascending, siteId);
  }

  public List getAllActivePublishedAssessments(String orderBy) {
      return getAllPublishedAssessments(orderBy, PublishedAssessmentFacade.ACTIVE_STATUS);
  }

  public List getAllActivePublishedAssessments(int pageSize, int pageNumber, String orderBy) {
      return getAllPublishedAssessments(pageSize, pageNumber, orderBy, PublishedAssessmentFacade.ACTIVE_STATUS);
  }

  public List getAllInActivePublishedAssessments(String orderBy) {
      return getAllPublishedAssessments(orderBy, PublishedAssessmentFacade.INACTIVE_STATUS);
  }

  public List getAllInActivePublishedAssessments(int pageSize, int pageNumber, String orderBy) {
      return getAllPublishedAssessments(pageSize, pageNumber, orderBy, PublishedAssessmentFacade.INACTIVE_STATUS);
  }

  public List getAllPublishedAssessments(String orderBy, Integer status) {
      return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getAllPublishedAssessments(orderBy, status); // signalling all & no paging
  }

  public List getAllPublishedAssessments(
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
  
  public List<PublishedAssessmentFacade> getBasicInfoOfAllActivePublishedAssessments(String orderBy,boolean ascending) {
    String siteAgentId = AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllActivePublishedAssessments(orderBy, siteAgentId, ascending); // signalling all & no paging
  }

  public List getBasicInfoOfAllActivePublishedAssessmentsByAgentId(String orderBy,boolean ascending,String siteAgentId ) {
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
            getBasicInfoOfAllActivePublishedAssessments(orderBy, siteAgentId, ascending); // signalling all & no paging
  }

  public List getBasicInfoOfAllInActivePublishedAssessments(String orderBy,boolean ascending) {
    String siteAgentId = AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        getBasicInfoOfAllInActivePublishedAssessments(orderBy, siteAgentId, ascending); // signalling all & no paging
  }

  public List getBasicInfoOfAllInActivePublishedAssessmentsByAgentId(String orderBy,boolean ascending, String siteAgentId) {
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
  public List<AssessmentGradingData> getBasicInfoOfLastSubmittedAssessments(String agentId,
                                                                            String orderBy, boolean ascending) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
         getBasicInfoOfLastSubmittedAssessments(agentId, orderBy, ascending);
  }

  /** total submitted for grade
   * returns HashMap (Long publishedAssessmentId, Integer totalSubmittedForGrade);
   */
  public Map<Long, Integer> getTotalSubmissionPerAssessment(String agentId) {
    return PersistenceService.getInstance().
        getPublishedAssessmentFacadeQueries().
        getTotalSubmissionPerAssessment(agentId);
  }

    public Map<Long, Integer> getTotalSubmissionPerAssessment(String agentId, String siteId) {
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

  public Map<Long, PublishedFeedback> getFeedbackHash(){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getFeedbackHash();
  }
  public Map<Long, PublishedAssessmentFacade> getAllAssessmentsReleasedToAuthenticatedUsers(){
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
  
  public Set<PublishedItemData> getPublishedItemSet(Long publishedAssessmentId, Long sectionId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
    getPublishedItemSet(publishedAssessmentId, sectionId);
  }

  public Long getItemType(String publishedItemId){
    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
      getItemType(new Long(publishedItemId));
  }

  public Map<Long, ItemTextIfc> preparePublishedItemTextHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, ItemTextIfc> map = new HashMap<>();
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

  public Map<Long, ItemDataIfc> preparePublishedItemHash(PublishedAssessmentIfc publishedAssessment){
    Map<Long, ItemDataIfc> map = new HashMap<>();
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

  public Map<Long, AnswerIfc> preparePublishedAnswerHash(PublishedAssessmentIfc publishedAssessment){
    Map<Long, AnswerIfc> map = new HashMap<>();
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

  public Map<Long, ItemDataIfc> prepareFIBItemHash(PublishedAssessmentIfc publishedAssessment){
    HashMap<Long, ItemDataIfc> map = new HashMap<>();
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

  public Map<Long, ItemDataIfc> prepareFINItemHash(PublishedAssessmentIfc publishedAssessment){
	    Map<Long, ItemDataIfc> map = new HashMap<>();
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
      Map<Long, ItemDataIfc> map = new HashMap<>();
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
      Map<Long, ItemDataIfc> map = new HashMap<>();
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
  
  
  public Map<Long, ItemDataIfc> prepareMCMRItemHash(PublishedAssessmentIfc publishedAssessment){
    Map<Long, ItemDataIfc> map = new HashMap<>();
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
  
  public Map<Long, ItemDataIfc> prepareEMIItemHash(PublishedAssessmentIfc publishedAssessment){
	    Map<Long, ItemDataIfc> map = new HashMap<>();
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

  public PublishedAssessmentIfc preparePublishedItemCancellation(PublishedAssessmentIfc publishedAssessment) {
    // Get publishedItemMap and filter out EMI questions
    Map<Long, ItemDataIfc> publishedItemMap = preparePublishedItemHash(publishedAssessment).entrySet().stream()
        .filter(publishedItemEntry -> !TypeIfc.EXTENDED_MATCHING_ITEMS.equals(publishedItemEntry.getValue().getTypeId()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // Items to cancel by just setting the score to 0. Other items are unaffected, Total score is reduced.
    Set<ItemDataIfc> totalScoreItemsToCancel  = publishedItemMap.entrySet().stream()
        .map(Map.Entry::getValue)
        .filter(publishedItem -> publishedItem != null)
        .filter(publishedItem -> publishedItem.getCancellation() == ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL)
        .filter(publishedItem -> publishedItem.getScore() != null)
        .filter(publishedItem -> publishedItem.getScore() != 0.0)
        .collect(Collectors.toSet());

    // Items to cancel by setting the score to 0 and distributing the points to the other items. Total score is unaffected.
    Set<ItemDataIfc> distributedItemsToCancel = publishedItemMap.entrySet().stream()
        .map(Map.Entry::getValue)
        .filter(publishedItem -> publishedItem != null)
        .filter(publishedItem -> publishedItem.getCancellation() == ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL)
        .filter(publishedItem -> publishedItem.getScore() != null)
        .filter(publishedItem -> publishedItem.getScore() != 0.0)
        .collect(Collectors.toSet());

    // Total of distributed items sores
    Double distributedItemsToCancelTotal = distributedItemsToCancel.stream()
        .map(publishedItem -> publishedItem.getScore())
        .reduce(0.0, Double::sum);

    // Number of items that are cancelled already
    int cancelledItemsCount = publishedItemMap.values().stream()
        .filter(publishedItem -> ItemCancellationUtil.isCancelled(publishedItem))
        .collect(Collectors.counting()).intValue();

    // Number of items that are not cancelled or going to be cancelled
    int distributedItemsNotToCancelCount = publishedItemMap.size() - distributedItemsToCancel.size() - totalScoreItemsToCancel.size() - cancelledItemsCount;

    // Points that should be added to each not cancelled item
    Double pointsToDistribute = distributedItemsToCancelTotal > 0.0
        ? distributedItemsToCancelTotal / (double) distributedItemsNotToCancelCount
        : 0.0;

    for (ItemDataIfc publishedItem : publishedItemMap.values()) {
      int publishedItemCancellation = publishedItem.getCancellation() != null
          ? publishedItem.getCancellation()
          : ItemDataIfc.ITEM_NOT_CANCELED;

      // Define the score the item should receive, based on it's cancellation status and adjust the cancellation
      Double score;
      Double discount;
      switch (publishedItemCancellation) {
        default:
        case ItemDataIfc.ITEM_NOT_CANCELED:
          Double originalScore = publishedItem.getScore() != null ? publishedItem.getScore() : 0.0;
          score = originalScore + pointsToDistribute;
          // Adjust discount based on the weight compared to the original score
          discount = publishedItem.getDiscount() != null ? (publishedItem.getDiscount() / originalScore) * score : null;
          break;
        case ItemDataIfc.ITEM_TOTAL_SCORE_TO_CANCEL:
          publishedItem.setCancellation(ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED);
          score = 0.0;
          discount = 0.0;
          break;
        case ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL:
          publishedItem.setCancellation(ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED);
          score = 0.0;
          discount = 0.0;
          break;
        case ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED:
        case ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED:
          // Item is cancelled already, we can skip it
          saveItem(publishedItem);
          continue;
      }

      // Set score and discount for item in item map
      publishedItem.setScore(score);
      publishedItem.setDiscount(discount);;

      Set<ItemTextIfc> publishedItemTextSet = publishedItem.getItemTextSet();
      publishedItemTextSet.forEach(publishedItemText -> {
        Set<AnswerIfc> answerSet = publishedItemText.getAnswerSet();
        // Set score and discount for all the answers
        answerSet.forEach(answer -> {
          answer.setScore(score);
          answer.setDiscount(discount);
        });
        publishedItemText.setAnswerSet(answerSet);
      });
      publishedItem.setItemTextSet(publishedItemTextSet);

      // Save item
      log.debug("Saving item [{}] with cancellation [{}]", publishedItem.getItemId(), publishedItem.getCancellation());
      saveItem(publishedItem);
    }

    return getPublishedAssessment(publishedAssessment.getPublishedAssessmentId().toString());
  }

  // Moved here to a separate method to enable unit testing of preparePublishedItemCancellation
  public void saveItem(ItemDataIfc item) {
      ItemService itemService = new ItemService();
      itemService.saveItem(new ItemFacade(item));
  }

  public void regradePublishedAssessment(PublishedAssessmentIfc publishedAssessment, boolean updateMostCurrentSubmission) {
    Map<Long, ItemDataIfc> publishedItemHash = preparePublishedItemHash(publishedAssessment);
    Map<Long, ItemTextIfc> publishedItemTextHash = preparePublishedItemTextHash(publishedAssessment);
    Map<Long, AnswerIfc> publishedAnswerHash = preparePublishedAnswerHash(publishedAssessment);

    GradingService gradingService = new GradingService();
    List<AssessmentGradingData> gradingDataList = gradingService.getAllAssessmentGradingData(
        publishedAssessment.getPublishedAssessmentId());

    if (updateMostCurrentSubmission) {
      // Allow student to resubmit the last submission
      publishedAssessment.setLastNeedResubmitDate(new Date());

      String currentAgent = "";

      for (AssessmentGradingData gradingData : gradingDataList) {
        if (!currentAgent.equals(gradingData.getAgentId())) {
          if (gradingData.getForGrade().booleanValue()) {
            gradingData.setForGrade(Boolean.FALSE);
            gradingData.setStatus(AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT);
          } else {
            gradingData.setStatus(AssessmentGradingData.ASSESSMENT_UPDATED);
          }

          currentAgent = gradingData.getAgentId();
        }

        // Grade based on this data
        gradingService.storeGrades(gradingData, true, publishedAssessment, publishedItemHash,
            publishedItemTextHash, publishedAnswerHash, true);
      }
    } else {
      for (AssessmentGradingData gradingData : gradingDataList) {
        // Grade based on this data
        gradingService.storeGrades(gradingData, true, publishedAssessment, publishedItemHash,
            publishedItemTextHash, publishedAnswerHash, true);
      }
    }
  }

  public void updateGradebook(PublishedAssessmentIfc assessment) {
    GradebookServiceHelper gbsHelper = IntegrationContextFactory.getInstance().getGradebookServiceHelper();
    GradebookExternalAssessmentService gradebookExternalAssessmentService = IntegrationContextFactory.getInstance().isIntegrated()
      ? (GradebookExternalAssessmentService) SpringBeanLocator.getInstance()
          .getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService")
      : null;

    if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), gradebookExternalAssessmentService)) {
      PublishedEvaluationModel evaluation = (PublishedEvaluationModel) assessment.getEvaluationModel();
      if (evaluation == null) {
        evaluation = new PublishedEvaluationModel();
        evaluation.setAssessmentBase(assessment);
      }

      Integer scoringType = evaluation.getScoringType();
      if (evaluation.getToGradeBook() != null	&& evaluation.getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {

        try {
          try {
            log.debug("before gbsHelper.updateGradebook()");
            gbsHelper.updateGradebook(assessment, gradebookExternalAssessmentService);
          } catch (Exception ex) {
              log.warn("Gradebook item does not exist for assessment {}, creating a new gradebook item", assessment.getAssessmentId());
              gbsHelper.addToGradebook(assessment, null, gradebookExternalAssessmentService);
          }

          // any score to copy over? get all the assessmentGradingData and copy over
          GradingService gradingService = new GradingService();
          // need to decide what to tell gradebook
          List<AssessmentGradingData> gradingDataList;

          if (EvaluationModelIfc.HIGHEST_SCORE.equals(scoringType)) {
            gradingDataList = gradingService.getHighestSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
          } else {
            gradingDataList = gradingService.getLastSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
          }
          log.debug("grading data list size: {}", gradingDataList.size());

          for (int i = 0; i < gradingDataList.size(); i++) {
            try {
              AssessmentGradingData gradingData = gradingDataList.get(i);
              log.debug("gradingData.scores: {}", gradingData.getTotalAutoScore());
              // Send the average score if average was selected for multiple submissions
              if (EvaluationModelIfc.AVERAGE_SCORE.equals(scoringType)
                  && !AssessmentGradingData.NO_SUBMISSION.equals(gradingData.getStatus())) {
                Double averageScore = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
                getAverageSubmittedAssessmentGrading(Long.valueOf(assessment.getPublishedAssessmentId()), gradingData.getAgentId());
                gradingData.setFinalScore(averageScore);
              }
              gbsHelper.updateExternalAssessmentScore(gradingData, gradebookExternalAssessmentService);
            } catch (Exception e) {
              log.warn("Could not update score item #{}: {}", i, e.toString());
            }
          }
        } catch (Exception e2) {
          log.warn("Could not update gradebook: {}", e2.toString());
        }
      } else {
        // remove
        try {
          gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(),
              assessment.getPublishedAssessmentId().toString(), gradebookExternalAssessmentService);
        } catch (Exception e) {
          log.info("Nothing to remove: {}", e.toString());
        }
      }
    }
  }

  public Set<PublishedSectionData> getSectionSetForAssessment(Long publishedAssessmentId){
	    return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
	    getSectionSetForAssessment(publishedAssessmentId);
  }
  
  public Set<PublishedSectionData> getSectionSetForAssessment(PublishedAssessmentIfc assessment){
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
	   return pub.getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);
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
   
   public Map getGroupsForSite() {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getGroupsForSite();
   }

   public PublishedAssessmentFacade getPublishedAssessmentInfoForRemove(Long publishedAssessmentId) {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
	   .getPublishedAssessmentInfoForRemove(publishedAssessmentId);
   }
   
   public Map<Long, String> getToGradebookPublishedAssessmentSiteIdMap() {
	   return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
	   .getToGradebookPublishedAssessmentSiteIdMap();
   }
   
   public List<AssessmentGradingData> getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(String agentId, String siteId,boolean allAssessments ){
	   return PersistenceService.getInstance().
	   getPublishedAssessmentFacadeQueries().
	   getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(agentId, siteId, allAssessments);
   }

	public List<AssessmentGradingData> getAllAssessmentsGradingDataByAgentAndSiteId(String agentId, String siteId) {
		return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
				.getAllAssessmentsGradingDataByAgentAndSiteId(agentId, siteId);
	}

	public List getQuestionsIdList(long publishedAssessmentId) {
		return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
				.getQuestionsIdList(publishedAssessmentId);
	}

	public List<PublishedAssessmentData> getPublishedDeletedAssessments(String siteId) {
		return PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getPublishedDeletedAssessments(siteId);
	}

	public void restorePublishedAssessment(Long publishedAssessmentId) {
		PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().restorePublishedAssessment(publishedAssessmentId);
	}

}
