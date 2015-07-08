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

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collection;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;

public interface PublishedAssessmentFacadeQueriesAPI
{

  public IdImpl getId(String id);

  public IdImpl getId(Long id);

  public IdImpl getId(long id);

  public IdImpl getAssessmentId(String id);

  public IdImpl getAssessmentId(Long id);

  public IdImpl getAssessmentId(long id);

  public IdImpl getAssessmentTemplateId(String id);

  public IdImpl getAssessmentTemplateId(Long id);

  public IdImpl getAssessmentTemplateId(long id);

  public PublishedAssessmentData preparePublishedAssessment(AssessmentData a);

  public PublishedFeedback preparePublishedFeedback(PublishedAssessmentData p,
      AssessmentFeedback a);

  public PublishedAccessControl preparePublishedAccessControl(
      PublishedAssessmentData p, AssessmentAccessControl a);

  public PublishedEvaluationModel preparePublishedEvaluationModel(
      PublishedAssessmentData p, EvaluationModel e);

  public Set preparePublishedMetaDataSet(PublishedAssessmentData p,
      Set metaDataSet);

  public Set preparePublishedSecuredIPSet(PublishedAssessmentData p, Set ipSet);

  public Set preparePublishedSectionSet(
      PublishedAssessmentData publishedAssessment, Set sectionSet, String protocol);

  public Set preparePublishedSectionMetaDataSet(
      PublishedSectionData publishedSection, Set metaDataSet);

  public Set preparePublishedItemSet(PublishedSectionData publishedSection,
      Set itemSet, String protocol);

  public Set preparePublishedItemTextSet(PublishedItemData publishedItem,
      Set itemTextSet, String protocol);

  public Set preparePublishedItemMetaDataSet(PublishedItemData publishedItem,
      Set itemMetaDataSet);

  public Set preparePublishedItemFeedbackSet(PublishedItemData publishedItem,
      Set itemFeedbackSet);

  public Set preparePublishedAnswerSet(PublishedItemText publishedItemText,
      Set answerSet);

  public Set preparePublishedAnswerFeedbackSet(PublishedAnswer publishedAnswer,
      Set answerFeedbackSet);

  public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId);
  
  public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId, boolean withGroupsInfo);

  public Long getPublishedAssessmentId(Long assessmentId);

  public PublishedAssessmentFacade publishAssessment(AssessmentFacade assessment) throws Exception;

  public PublishedAssessmentFacade publishPreviewAssessment(AssessmentFacade assessment);

  public void createAuthorization(PublishedAssessmentData p);

  public AssessmentData loadAssessment(Long assessmentId);

  /**
   *  Retrieve a published Assessment
   * @param assessmentId the id of the assessment
   * @return the Assessment object or null if none found
   */
  public PublishedAssessmentData loadPublishedAssessment(Long assessmentId);

  public ArrayList getAllTakeableAssessments(String orderBy, boolean ascending,
      Integer status);

  /**
   public ArrayList getAllPublishedAssessmentId() {

   ArrayList list = getBasicInfoOfAllActivePublishedAssessments("title", true);
   ArrayList publishedIds = new ArrayList();
   for (int i = 0; i < list.size(); i++) {
   PublishedAssessmentFacade f = (PublishedAssessmentFacade) list.get(i);
   Long publishedId = f.getPublishedAssessmentId();
   publishedIds.add(publishedId);
   }
   return publishedIds;

   }
   */

  public Integer getNumberOfSubmissions(String publishedAssessmentId,
      String agentId);

  public List getNumberOfSubmissionsOfAllAssessmentsByAgent(String agentId);

  /**
   public ArrayList getAllReviewableAssessments(String orderBy,
   boolean ascending) {

   ArrayList publishedIds = getAllPublishedAssessmentId();
   ArrayList newlist = new ArrayList();
   for (int i = 0; i < publishedIds.size(); i++) {
   String publishedId = ( (Long) publishedIds.get(i)).toString();
   String query = "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? order by agentId ASC," +
   orderBy;
   if (ascending) {
   query += " asc,";
   }
   else {
   query += " desc,";
   }
   query += "submittedDate DESC";
   List list = getHibernateTemplate().find(query, new Long(publishedId),
   Hibernate.LONG);
   if (!list.isEmpty()) {
   Iterator items = list.iterator();
   String agentid = null;
   AssessmentGradingData data = (AssessmentGradingData) items.next();
   agentid = data.getAgentId();
   newlist.add(data);
   while (items.hasNext()) {
   while (items.hasNext()) {
   data = (AssessmentGradingData) items.next();
   if (!data.getAgentId().equals(agentid)) {
   agentid = data.getAgentId();
   newlist.add(data);
   break;
   }
   }
   }
   }
   }
   ArrayList assessmentList = new ArrayList();
   for (int i = 0; i < newlist.size(); i++) {
   AssessmentGradingData a = (AssessmentGradingData) newlist.get(i);
   AssessmentGradingFacade f = new AssessmentGradingFacade(a);
   assessmentList.add(f);
   }
   return assessmentList;
   }
   */
  public ArrayList getAllPublishedAssessments(String sortString);

  public ArrayList getAllPublishedAssessments(String sortString, Integer status);

  public ArrayList getAllPublishedAssessments(int pageSize, int pageNumber,
      String sortString, Integer status);

  public void removeAssessment(Long assessmentId, String action);
  
  public void deleteAllSecuredIP(PublishedAssessmentIfc assessment);

  public void saveOrUpdate(PublishedAssessmentIfc assessment) throws Exception;

  public void delete(PublishedAssessmentIfc assessment);

  public ArrayList getBasicInfoOfAllActivePublishedAssessments(
      String sortString, String siteAgentId, boolean ascending);

  /**
   * According to Marc inactive means either the dueDate or the retractDate has
   * passed for 1.5 release (IM on 12/17/04)
   * @param sortString
   * @return
   */
  public ArrayList getBasicInfoOfAllInActivePublishedAssessments(
      String sortString, String siteAgentId, boolean ascending);

  /** return a set of PublishedSectionData
   * IMPORTANT:
   * 1. we have declared SectionData as lazy loading, so we need to
   * initialize it using getHibernateTemplate().initialize(java.lang.Object).
   * Unfortunately,  we are using Spring 1.0.2 which does not support this
   * Hibernate feature. I tried upgrading Spring to 1.1.3. Then it failed
   * to load all the OR maps correctly. So for now, I am just going to
   * initialize it myself. I will take a look at it again next year.
   * - daisyf (12/13/04)
   */
  public HashSet getSectionSetForAssessment(PublishedAssessmentIfc assessment);

  // IMPORTANT:
  // 1. we do not want any Section info, so set loadSection to false
  // 2. We have also declared SectionData as lazy loading. If loadSection is set
  // to true, we will see null pointer
  public PublishedAssessmentFacade getSettingsOfPublishedAssessment(
      Long assessmentId);

  public PublishedItemData loadPublishedItem(Long itemId);

  public PublishedItemText loadPublishedItemText(Long itemTextId);

  // added by daisy - please check the logic - I based this on the getBasicInfoOfAllActiveAssessment
  public ArrayList getBasicInfoOfAllPublishedAssessments(String orderBy, boolean ascending, String siteId);

  public ArrayList getBasicInfoOfAllPublishedAssessments2(String orderBy, boolean ascending, String siteId);
  
  /**
   * return an array list of the last AssessmentGradingFacade per assessment that
   * a user has submitted for grade.
   * @param agentId
   * @param orderBy
   * @param ascending
   * @return
   */
  public ArrayList getBasicInfoOfLastSubmittedAssessments(String agentId,
      String orderBy, boolean ascending);

  /** total submitted for grade
   * returns HashMap (Long publishedAssessmentId, Integer totalSubmittedForGrade);
   */
  public HashMap getTotalSubmissionPerAssessment(String agentId);

    public HashMap getTotalSubmissionPerAssessment(String agentId, String siteId);

  public Integer getTotalSubmission(String agentId, Long publishedAssessmentId);

  /**
   * Get submission number for the assessment by giving the publishedAssessmentId
   * for assessment deletion safe check
   * @param publishedAssessmentId
   * @return number of submissions
   */
  public Integer getTotalSubmissionForEachAssessment(Long publishedAssessmentId);

  public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(String alias);

  public PublishedAssessmentFacade getPublishedAssessmentIdByMetaLabel(
      String label, String entry);

  public void saveOrUpdateMetaData(PublishedMetaData meta);

  public HashMap getFeedbackHash();

  /** this return a HashMap containing
   *  (Long publishedAssessmentId, PublishedAssessmentFacade publishedAssessment)
   *  Note that the publishedAssessment is a partial object used for display only.
   *  do not use it for persisting. It only contains title, releaseTo, startDate, dueDate
   *  & retractDate
   */
  public HashMap getAllAssessmentsReleasedToAuthenticatedUsers();

  /* 
   * This function returns a site id that "owns" the assessment not a user id.
   * @return String the site that owns the assessment
   */
  public String getPublishedAssessmentOwner(String publishedAssessmentId);

  public boolean publishedAssessmentTitleIsUnique(Long assessmentBaseId, String title);

  public boolean hasRandomPart(Long publishedAssessmentId);

  public List getContainRandomPartAssessmentIds(Collection assessmentIds);
  
  public PublishedItemData getFirstPublishedItem(Long publishedAssessmentId);

  public List getPublishedItemIds(Long publishedAssessmentId);

  public HashSet getPublishedItemSet(Long publishedAssessmentId, Long sectionId);
  
  public Long getItemType(Long publishedItemId);
  
  public HashSet getSectionSetForAssessment(Long publishedAssessmentId);

  public boolean isRandomDrawPart(Long publishedAssessmentId, Long sectionId);
 
  public PublishedAssessmentData getBasicInfoOfPublishedAssessment(Long publishedId);
  
  public String getPublishedAssessmentSiteId(String publishedAssessmentId);
 
  public Integer getPublishedItemCount(Long publishedAssessmentId);
  
  public Integer getPublishedSectionCount(final Long publishedAssessmentId);
  
  public PublishedAttachmentData getPublishedAttachmentData(Long attachmentId);

  public void updateAssessmentLastModifiedInfo(PublishedAssessmentFacade publishedAssessmentFacade);

  public void saveOrUpdateSection(SectionFacade section);
  
  public void removeItemAttachment(Long itemAttachmentId);
  
  public PublishedSectionFacade addSection(Long publishedAssessmentId);

  public PublishedSectionFacade getSection(Long sectionId);

  public AssessmentAccessControlIfc loadPublishedAccessControl(Long publishedAssessmentId);
  
  public void saveOrUpdatePublishedAccessControl(AssessmentAccessControlIfc publishedAccessControl);

  public List getReleaseToGroupIdsForPublishedAssessment(final String publishedAssessmentId);
  
  public Integer getPublishedAssessmentStatus(Long publishedAssessmentId);
  
  public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol);

  public void removeAssessmentAttachment(Long assessmentAttachmentId);
  
  public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
		  String resourceId, String filename, String protocol);

  public void removeSectionAttachment(Long sectionAttachmentId);

  public void saveOrUpdateAttachments(List list);
  
  public TreeMap getGroupsForSite();
  public PublishedAssessmentFacade getPublishedAssessmentInfoForRemove(Long publishedAssessmentId);
  
  public HashMap getToGradebookPublishedAssessmentSiteIdMap();
  
  public ArrayList getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(	final String agentId, final String siteId, boolean allAssessments);
     
}
