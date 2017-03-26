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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public interface AssessmentFacadeQueriesAPI {

	public IdImpl getId(String id);

	public IdImpl getId(Long id);

	public IdImpl getId(long id);

	public IdImpl getAssessmentId(String id);

	public IdImpl getAssessmentId(Long id);

	public IdImpl getAssessmentId(long id);

	public IdImpl getAssessmentTemplateId(String id);

	public IdImpl getAssessmentTemplateId(Long id);

	public IdImpl getAssessmentTemplateId(long id);

	public Long addTemplate();

	public void removeTemplate(Long assessmentId);

	public Long addAssessment(Long assessmentTemplateId);

	public AssessmentBaseData load(Long id);

	public AssessmentTemplateData loadTemplate(Long assessmentTemplateId);

	public AssessmentData loadAssessment(Long assessmentId);

	/*
	 * The following methods are real
	 * 
	 */
	public AssessmentTemplateFacade getAssessmentTemplate(
			Long assessmentTemplateId);

	public List<AssessmentTemplateFacade> getAllAssessmentTemplates();

	public List<AssessmentTemplateFacade> getAllActiveAssessmentTemplates();

	/**
	 * 
	 * @return a list of AssessmentTemplateFacade. However, it is IMPORTANT to
	 *         note that it is not a full object, it contains merely
	 *         assessmentBaseId (which is the templateId) & title. This methods
	 *         is used when a list of template titles is required for displaying
	 *         purposes.
	 */
	public List<AssessmentTemplateFacade> getTitleOfAllActiveAssessmentTemplates();

	/**
	 * 
	 * @param assessmentId
	 * @return the assessment or null if not found
	 */
	public AssessmentFacade getAssessment(Long assessmentId);

	public void removeAssessment(Long assessmentId);

	public AssessmentData cloneAssessmentFromTemplate(AssessmentTemplateData t);

	/**
	 * This method is the same as createAssessment() except that no default
	 * section will be created with the assessment.
	 */
	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, Long typeId, Long templateId) throws Exception;

	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, Long typeId, Long templateId, String siteId) throws Exception;


	public AssessmentFacade createAssessment(String title, String description,
			Long typeId, Long templateId) throws Exception;

	public AssessmentFacade createAssessment(String title, String description,
			Long typeId, Long templateId, String siteId) throws Exception;

	public List<AssessmentFacade> getAllAssessments(String orderBy);

	public List<AssessmentFacade> getAllActiveAssessments(String orderBy);

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessments(String orderBy,
																	 boolean ascending);

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessmentsByAgent(String orderBy,
																			String siteAgentId, boolean ascending);

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessmentsByAgent(String orderBy,
																			String siteAgentId);

	public AssessmentFacade getBasicInfoOfAnAssessment(Long assessmentId);
	
	public AssessmentFacade getBasicInfoOfAnAssessmentFromSectionId(Long sectionId);

	public List<AssessmentFacade> getSettingsOfAllActiveAssessments(String orderBy);

	public List<AssessmentFacade> getAllAssessments(int pageSize, int pageNumber,
                                                    String orderBy);

	public int getQuestionSize(final Long assessmentId);

	public List getQuestionsIdList(final Long assessmentId);

	public void deleteAllSecuredIP(AssessmentIfc assessment);

	public void saveOrUpdate(AssessmentFacade assessment);

	public void saveOrUpdate(AssessmentTemplateData template);

	public void deleteTemplate(Long templateId);

	public SectionFacade addSection(Long assessmentId);

	public SectionFacade getSection(Long sectionId);

	public void removeSection(Long sectionId);

	public SectionData loadSection(Long sectionId);

	public void saveOrUpdateSection(SectionFacade section);

	/**
	 * This method move a set of questions form one section to another
	 * 
	 * @param sourceSectionId
	 * @param destSectionId
	 */
	public void moveAllItems(Long sourceSectionId, Long destSectionId);

	public List<AssessmentTemplateFacade> getBasicInfoOfAllActiveAssessmentTemplates(String orderBy);

	public void checkForQuestionPoolItem(AssessmentData assessment, Map qpItemHash);

	public void checkForQuestionPoolItem(SectionData section, Map qpItemHash);

	public void removeAllItems(Long sourceSectionId);

	public boolean assessmentTitleIsUnique(Long assessmentBaseId, String title,
			Boolean isTemplate);

	public List getAssessmentByTemplate(Long templateId);

	public List getDefaultMetaDataSet();

	public void deleteAllMetaData(AssessmentBaseIfc assessment);

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item,
			String resourceId, String filename, String protocolboolean, boolean isEditPendingAssessmentFlow);

	public ItemTextAttachmentIfc createItemTextAttachment(ItemTextIfc itemText,
			String resourceId, String filename, String protocolboolean, boolean isEditPendingAssessmentFlow);
	
	public void updateAssessmentLastModifiedInfo(
			AssessmentFacade assessment);

	public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
			String resourceId, String filename, String protocol);

	public void removeSectionAttachment(Long sectionAttachmentId);

	public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol);

	public void removeAssessmentAttachment(Long assessmentAttachmentId);

	public AttachmentData createEmailAttachment(String resourceId,
			String filename, String protocol);

	public void saveOrUpdateAttachments(List<AttachmentIfc> list);

    public String getAssessmentSiteId (String assessmentId);
  
    public String getAssessmentCreatedBy(String assessmentId);

    public void copyAllAssessments(String fromContext, String toContext, Map<String,String> transversalMap);
	
	public void copyAssessment(String assessmentId, String apepndCopyTitle);
	
	public List getAllActiveAssessmentsByAgent(String fromContext);

	public Set copyItemAttachmentSet(ItemData newItem, Set itemAttachmentSet);

	public Set prepareItemSet(SectionData newSection, Set itemSet, String protocol, String toContext);

	public Set prepareItemTextSet(ItemData newItem, Set itemTextSet, String protocol, String toContext);

	public Set prepareItemMetaDataSet(ItemData newItem, Set itemMetaDataSet);

	public Set prepareItemTagSet(ItemData newItem, Set itemTagSet);

	public Set prepareItemFeedbackSet(ItemData newItem, Set itemFeedbackSet);

	public Set prepareItemAttachmentSet(ItemData newItem,
										Set itemAttachmentSet, String protocol, String toContext);

}
