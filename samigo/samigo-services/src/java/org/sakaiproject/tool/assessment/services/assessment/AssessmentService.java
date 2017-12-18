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

package org.sakaiproject.tool.assessment.services.assessment;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.entity.api.CoreAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.entity.api.PublishedAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * The AssessmentService calls the service locator to reach the manager on the
 * back end.
 * 
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
@Slf4j
public class AssessmentService {
	public static final int UPDATE_SUCCESS = 0;
	public static final int UPDATE_ERROR_DRAW_SIZE_TOO_LARGE = 1;
	private SecurityService securityService = ComponentManager.get(SecurityService.class);

	/**
	 * Creates a new QuestionPoolService object.
	 */
	public AssessmentService() {
	}

	public AssessmentTemplateFacade getAssessmentTemplate(
			String assessmentTemplateId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAssessmentTemplate(
							new Long(assessmentTemplateId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade getAssessment(String assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAssessment(
							Long.valueOf(assessmentId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	public AssessmentIfc getAssessment(Long assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAssessment(assessmentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade getBasicInfoOfAnAssessment(String assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getBasicInfoOfAnAssessment(
							new Long(assessmentId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade getBasicInfoOfAnAssessmentFromSectionId(Long sectionId) {
		try {
			return PersistenceService.getInstance().getAssessmentFacadeQueries().getBasicInfoOfAnAssessmentFromSectionId(sectionId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public List<AssessmentTemplateFacade> getAllAssessmentTemplates() {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAllAssessmentTemplates();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public List<AssessmentTemplateFacade> getAllActiveAssessmentTemplates() {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries()
					.getAllActiveAssessmentTemplates();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public List<AssessmentTemplateFacade> getTitleOfAllActiveAssessmentTemplates() {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries()
					.getTitleOfAllActiveAssessmentTemplates();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public List<AssessmentFacade> getAllAssessments(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getAllAssessments(orderBy); // signalling all & no paging
	}

	public List<AssessmentFacade> getAllActiveAssessments(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getAllActiveAssessments(orderBy); // signalling all & no
													// paging
	}

	/**
	 * @param orderBy
	 * @return an ArrayList of AssessmentFacade. It is IMPORTANT to note that
	 *         the object is a partial object which contains no SectionFacade
	 */
	public List<AssessmentFacade> getSettingsOfAllActiveAssessments(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getSettingsOfAllActiveAssessments(orderBy); // signalling
																// all & no
																// paging
	}

	/**
	 * @param orderBy
	 * @return an ArrayList of AssessmentFacade. It is IMPORTANT to note that
	 *         the object is a partial object which contains only Assessment
	 *         basic info such as title, lastModifiedDate. This method is used
	 *         by Authoring Front Door
	 */
	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessments(String orderBy,
			boolean ascending) {
		String siteAgentId = AgentFacade.getCurrentSiteId();
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getBasicInfoOfAllActiveAssessmentsByAgent(orderBy,
						siteAgentId, ascending); // signalling all & no
													// paging
	}

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessments(String orderBy) {
		String siteAgentId = AgentFacade.getCurrentSiteId();
		return PersistenceService
				.getInstance()
				.getAssessmentFacadeQueries()
				.getBasicInfoOfAllActiveAssessmentsByAgent(orderBy, siteAgentId); // signalling
																					// all
																					// & no
																					// paging
	}

	public List<AssessmentFacade> getAllAssessments(int pageSize, int pageNumber,
			String orderBy) {
		try {
			if (pageSize > 0 && pageNumber > 0) {
				return PersistenceService.getInstance()
						.getAssessmentFacadeQueries().getAllAssessments(
								pageSize, pageNumber, orderBy);
			} else {
				return PersistenceService.getInstance()
						.getAssessmentFacadeQueries()
						.getAllAssessments(orderBy);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade createAssessment(String title, String description,
			String typeId, String templateId) throws Exception {
		return createAssessment(title, description, typeId, templateId, null);
	}

	public AssessmentFacade createAssessment(String title, String description,
			String typeId, String templateId, String siteId) throws Exception {
		AssessmentFacade assessment = null;
		try {
			AssessmentTemplateFacade assessmentTemplate = null;
			// #1 - check templateId and prepared it in Long
			Long templateIdLong = AssessmentTemplateFacade.DEFAULTTEMPLATE;
			if (StringUtils.isNotBlank(templateId)) {
				templateIdLong = new Long(templateId);
			}

			// #2 - check typeId and prepared it in Long
			Long typeIdLong = TypeFacade.HOMEWORK;
			if (StringUtils.isNotBlank(typeId)) {
				typeIdLong = new Long(typeId);
			}

			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			log.debug("**** AssessmentFacadeQueries=" + queries);
			assessment = queries.createAssessment(title, description,
					typeIdLong, templateIdLong, siteId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception(e);
		}
		return assessment;
	}

	public int getQuestionSize(String assessmentId) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getQuestionSize(new Long(assessmentId));
	}

	public List getQuestionsIdList(long assessmentId) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getQuestionsIdList(assessmentId);
	}

	public void update(AssessmentFacade assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdate(assessment);
	}

	public void save(AssessmentTemplateData template) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdate(template);
	}

	public void deleteAllSecuredIP(AssessmentIfc assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.deleteAllSecuredIP(assessment);
	}

	public void saveAssessment(AssessmentFacade assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdate(assessment);
	}

	public void deleteAssessmentTemplate(Long assessmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.deleteTemplate(assessmentId);
	}

	public void removeAssessment(String assessmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeAssessment(new Long(assessmentId));
	}

	/**
	 * public int checkDelete(long assessmentId){ return
	 * assessmentService.checkDelete(assessmentId); }
	 * 
	 * public void deleteAssessment(Id assessmentId) throws
	 * osid.assessment.AssessmentException {
	 * assessmentService.deleteAssessment(assessmentId); }
	 * 
	 * public AssessmentIterator getAssessments() throws
	 * osid.assessment.AssessmentException { return
	 * assessmentService.getAssessments(); }
	 * 
	 */

	public SectionFacade addSection(String assessmentId) {
		SectionFacade section = null;
		try {
			Long assessmentIdLong = new Long(assessmentId);
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			section = queries.addSection(assessmentIdLong);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return section;
	}

	public void removeSection(String sectionId) {
		try {
			Long sectionIdLong = new Long(sectionId);
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			queries.removeSection(sectionIdLong);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	public SectionFacade getSection(String sectionId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getSection(
							new Long(sectionId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void saveOrUpdateSection(SectionFacade section) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.saveOrUpdateSection(section);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void moveAllItems(String sourceSectionId, String destSectionId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.moveAllItems(new Long(sourceSectionId),
						new Long(destSectionId)); // signalling all & no
													// paging
	}

	public void removeAllItems(String sourceSectionId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeAllItems(new Long(sourceSectionId));
	}

	public boolean verifyItemsDrawSize(SectionFacade section){
		if (section != null && section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE) != null
				&& StringUtils.equals(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE), SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
			QuestionPoolService qpService = new QuestionPoolService();
			List itemlist = qpService
			.getAllItems(Long.valueOf(section
					.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW)));
			return verifyItemsDrawSize(itemlist.size(), section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
		}else{
			return true;
		}
	}

	private boolean verifyItemsDrawSize(int itemcount, String numberDrawn){
		try{
			int numberDrawnInt = Integer.parseInt(numberDrawn);
			if(numberDrawnInt <=0 || numberDrawnInt>itemcount){
				return false;
			}
		} catch(NumberFormatException e){
			return false;
		}

		return true;
	}

	public int updateRandomPoolQuestions(SectionFacade section){
		return updateRandomPoolQuestions(section, false);
	}
	
	public int updateRandomPoolQuestions(SectionFacade section, boolean publishing){
		if (section != null && section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE) != null
				&& StringUtils.equals(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE), SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {

			QuestionPoolService qpService = new QuestionPoolService();
			List itemlist = qpService.getAllItems(Long.valueOf(section
					.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW)));

			if(verifyItemsDrawSize(itemlist.size(), section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN))){
				// random section:
				removeAllItems(section.getSectionId().toString());

				ItemService itemService = new ItemService();
				String agentId = AgentFacade.getAgentString();

				Iterator itemIter = section.getItemSet().iterator();
				while (itemIter.hasNext()) {
					ItemDataIfc item = (ItemDataIfc) itemIter.next();
					List poolIds = qpService.getPoolIdsByItem(item.getItemId()
							.toString());
					if (poolIds.size() == 0) {
						Long deleteId = item.getItemId();
						itemService.deleteItem(deleteId, agentId);
						EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_ITEM_DELETE, "/sam/" +AgentFacade.getCurrentSiteId() + "/removed itemId=" + deleteId, true));
						itemIter.remove();
					}
				}
				// need to reload
				section = getSection(section.getSectionId().toString());

				// ItemService itemservice = new ItemService();
				boolean hasRandomPartScore = false;
				Double score = null;
				String requestedScore = (section.getSectionMetaDataByLabel(SectionDataIfc.POINT_VALUE_FOR_QUESTION) != null) ? 
						                 section.getSectionMetaDataByLabel(SectionDataIfc.POINT_VALUE_FOR_QUESTION)	: "";
						                 
				if (StringUtils.isNotBlank(requestedScore)) {
					hasRandomPartScore = true;
					score = new Double(requestedScore);
				}
				boolean hasRandomPartDiscount = false;
				Double discount = null;
				String requestedDiscount = (section.getSectionMetaDataByLabel(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION) != null) ? 
											section.getSectionMetaDataByLabel(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION) : "";

				if (StringUtils.isNotBlank(requestedDiscount)) {
					hasRandomPartDiscount = true;
					discount = new Double(requestedDiscount);
				}

				int i = 0;
				Iterator iter = itemlist.iterator();
				while (iter.hasNext()) {
					ItemFacade item = (ItemFacade) iter.next();
					// copy item so we can have it in more than one assessment
					item = qpService.copyItemFacade2(item);
					item.setSection(section);
					item.setSequence(Integer.valueOf(i + 1));
//					if (hasRandomPartScore || hasRandomPartDiscount) {
						if (hasRandomPartScore)
							item.setScore(score);
						long itemTypeId = item.getTypeId().longValue();
						String mcmsPartialCredit = item.getItemMetaDataByLabel(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT);
						if (hasRandomPartDiscount &&
								(itemTypeId == TypeFacade.MULTIPLE_CHOICE.longValue() || 
								itemTypeId == TypeFacade.TRUE_FALSE.longValue() || 
								itemTypeId == TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.longValue() ||
								(itemTypeId == TypeFacade.MULTIPLE_CORRECT.longValue() && "false".equals(mcmsPartialCredit))))
							item.setDiscount(discount);
						ItemDataIfc data = item.getData();
						Set itemTextSet = data.getItemTextSet();
						if (itemTextSet != null) {
							Iterator iterITS = itemTextSet.iterator();
							while (iterITS.hasNext()) {
								ItemTextIfc itemText = (ItemTextIfc) iterITS.next();
								if(publishing){
									itemText.setText(copyContentHostingAttachments(itemText.getText(), AgentFacade.getCurrentSiteId()));
								}
								Set answerSet = itemText.getAnswerSet();
								if (answerSet != null) {
									Iterator iterAS = answerSet.iterator();
									while (iterAS.hasNext()) {
										AnswerIfc answer = (AnswerIfc) iterAS
										.next();
										if(publishing){
											answer.setText(copyContentHostingAttachments(answer.getText(), AgentFacade.getCurrentSiteId()));
										}
										if (hasRandomPartScore)
											answer.setScore(score);
										if (hasRandomPartDiscount && 
											(itemTypeId == TypeFacade.MULTIPLE_CHOICE.longValue() || 
											itemTypeId == TypeFacade.TRUE_FALSE.longValue() || 
											itemTypeId == TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.longValue() ||
											(itemTypeId == TypeFacade.MULTIPLE_CORRECT.longValue() && "false".equals(mcmsPartialCredit))))
											answer.setDiscount(discount);
									}
								}
							}
						}
//					}
					section.addItem(item);
					EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved  itemId=" + item.getItemId().toString(), true));
					i = i + 1;
				}

				//update meta data for date:
				//We need this in a standard format so it can be parsed later. This is ISO8601 format -DH
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
				section.addSectionMetaData(SectionDataIfc.QUESTIONS_RANDOM_DRAW_DATE, df.format(new Date()));

				saveOrUpdateSection(section);
			}else{
				return UPDATE_ERROR_DRAW_SIZE_TOO_LARGE;
			}
		}

		return UPDATE_SUCCESS;		
	}

	public int updateAllRandomPoolQuestions(AssessmentFacade assessment){
		return updateAllRandomPoolQuestions(assessment, false);
	}
	
	public int updateAllRandomPoolQuestions(AssessmentFacade assessment, boolean publishing){		
		//verify that we can update the sections first:
		for(SectionFacade section : (List<SectionFacade>) assessment.getSectionArray()){			
			if(!verifyItemsDrawSize(section)){
				return UPDATE_ERROR_DRAW_SIZE_TOO_LARGE;
			}
		}

		//passed all tests, so update pool questions:
		for(SectionFacade section : (List<SectionFacade>) assessment.getSectionArray()){
			updateRandomPoolQuestions(section, publishing);
		}

		return UPDATE_SUCCESS;
	}

	public List<AssessmentTemplateFacade> getBasicInfoOfAllActiveAssessmentTemplates(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getBasicInfoOfAllActiveAssessmentTemplates(orderBy); // signalling
																		// all &
																		// no
																		// paging
	}

	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, String typeId, String templateId) throws Exception {
		return createAssessmentWithoutDefaultSection(title, description, typeId, templateId, null);
	}


	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, String typeId, String templateId, String siteId) throws Exception {
		AssessmentFacade assessment = null;
		try {
			AssessmentTemplateFacade assessmentTemplate = null;
			// #1 - check templateId and prepared it in Long
			Long templateIdLong = AssessmentTemplateFacade.DEFAULTTEMPLATE;
			if (StringUtils.isNotBlank(templateId)) {
				templateIdLong = new Long(templateId);
			}

			// #2 - check typeId and prepared it in Long
			Long typeIdLong = TypeFacade.HOMEWORK;
			if (StringUtils.isNotBlank(typeId)) {
				typeIdLong = new Long(typeId);
			}

			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			assessment = queries.createAssessmentWithoutDefaultSection(title,
					description, typeIdLong, templateIdLong, siteId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception(e);
		}
		return assessment;
	}

	public boolean assessmentTitleIsUnique(String assessmentBaseId,
			String title, boolean isTemplate) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.assessmentTitleIsUnique(new Long(assessmentBaseId), title,
						Boolean.valueOf(isTemplate));
	}

	public List getAssessmentByTemplate(String templateId) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getAssessmentByTemplate(new Long(templateId));
	}

	public List getDefaultMetaDataSet() {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getDefaultMetaDataSet();
	}

	public void deleteAllMetaData(AssessmentBaseIfc assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.deleteAllMetaData(assessment);
	}

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item,
			String resourceId, String filename, String protocol) {
		return createItemAttachment(item, resourceId,
					filename, protocol, true);
	}

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item,
			String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
		ItemAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createItemAttachment(item, resourceId,
					filename, protocol, isEditPendingAssessmentFlow);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}

	public ItemTextAttachmentIfc createItemTextAttachment(ItemTextIfc itemText,
			String resourceId, String filename, String protocol) {
		return createItemTextAttachment(itemText, resourceId,
					filename, protocol, true);
	}

	public ItemTextAttachmentIfc createItemTextAttachment(ItemTextIfc itemText,
			String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
		ItemTextAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createItemTextAttachment(itemText, resourceId,
					filename, protocol, isEditPendingAssessmentFlow);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}

	public void updateAssessmentLastModifiedInfo(
			AssessmentIfc assessment) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.updateAssessmentLastModifiedInfo((AssessmentFacade) assessment);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
			String resourceId, String filename, String protocol) {
		SectionAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createSectionAttachment(section, resourceId,
					filename, protocol);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}

	public void removeSectionAttachment(String attachmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeSectionAttachment(new Long(attachmentId));
	}

	public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol) {
		AssessmentAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createAssessmentAttachment(assessment,
					resourceId, filename, protocol);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}

	public void removeAssessmentAttachment(String attachmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeAssessmentAttachment(new Long(attachmentId));
	}

	public AttachmentData createEmailAttachment(String resourceId,
			String filename, String protocol) {
		AttachmentData attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createEmailAttachment(resourceId, filename,
					protocol);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return attachment;
	}

	public List getAssessmentResourceIdList(AssessmentIfc pub) {
		List resourceIdList = new ArrayList();
		List list = pub.getAssessmentAttachmentList();
		if (list != null) {
			resourceIdList = getResourceIdList(list);
		}
		Set sectionSet = pub.getSectionSet();
		Iterator iter = sectionSet.iterator();
		while (iter.hasNext()) {
			SectionDataIfc section = (SectionDataIfc) iter.next();
			List sectionAttachments = getSectionResourceIdList(section);
			if (sectionAttachments != null) {
				resourceIdList.addAll(sectionAttachments);
			}
		}
		log.debug("*** resource size=" + resourceIdList.size());
		return resourceIdList;
	}

	public List getSectionResourceIdList(SectionDataIfc section) {
		List resourceIdList = new ArrayList();
		List list = section.getSectionAttachmentList();
		if (list != null) {
			resourceIdList = getResourceIdList(list);
		}
		Set itemSet = section.getItemSet();
		Iterator iter1 = itemSet.iterator();
		while (iter1.hasNext()) {
			ItemDataIfc item = (ItemDataIfc) iter1.next();
			List itemAttachments = getItemResourceIdList(item);
			if (itemAttachments != null) {
				resourceIdList.addAll(itemAttachments);
			}
		}
		return resourceIdList;
	}

	public List getItemResourceIdList(ItemDataIfc item) {
		List resourceIdList = new ArrayList();
		List list = item.getItemAttachmentList();
		if (list != null) {
			resourceIdList = getResourceIdList(list);
		}
		return resourceIdList;
	}

	private List getResourceIdList(List list) {
		List resourceIdList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AttachmentIfc attach = (AttachmentIfc) list.get(i);
			resourceIdList.add(attach.getResourceId());
		}
		return resourceIdList;
	}

	public void deleteResources(List resourceIdList) {
		if (resourceIdList == null)
			return;
		for (int i = 0; i < resourceIdList.size(); i++) {
			String resourceId = (String) resourceIdList.get(i);
			resourceId = resourceId.trim();
			if (resourceId.toLowerCase().startsWith("/attachment")) {
				try {
					log.debug("removing=" + resourceId);
					AssessmentService.getContentHostingService().removeResource(resourceId);
				} catch (PermissionException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("PermissionException from ContentHostingService:"
							+ e.getMessage());
				} catch (IdUnusedException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("IdUnusedException from ContentHostingService:"
							+ e.getMessage());
				} catch (TypeException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("TypeException from ContentHostingService:"
							+ e.getMessage());
				} catch (InUseException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("InUseException from ContentHostingService:"
							+ e.getMessage());
				}
			}
		}
	}

	public void saveOrUpdateAttachments(List list) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdateAttachments(list);
	}

	public ContentResource createCopyOfContentResource(String resourceId,
			String filename, String toContext) {
		// trouble using Validator, so use string replacement instead
		// java.lang.NoClassDefFoundError: org/sakaiproject/util/Validator
		filename = filename.replaceAll("http://","http:__");
		ContentResource cr_copy = null;
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			@Override
			public SecurityAdvice isAllowed(String arg0, String arg1,
					String arg2) {
				if(ContentHostingService.AUTH_RESOURCE_READ.equals(arg1)){
					return SecurityAdvice.ALLOWED;
				}else{
					return SecurityAdvice.PASS;
				}
			}
		};
		try {
			securityService.pushAdvisor(securityAdvisor);
			// create a copy of the resource
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			String escapedName = escapeResourceName(filename);
			if (StringUtils.isNotBlank(toContext)) {
				cr_copy = AssessmentService.getContentHostingService().addAttachmentResource(escapedName, 
						toContext, 
						ToolManager.getTool("sakai.samigo").getTitle(), cr
						.getContentType(), cr.streamContent(), cr.getProperties());
			}
			else {
				cr_copy = AssessmentService.getContentHostingService().addAttachmentResource(escapedName, 
						ToolManager.getCurrentPlacement().getContext(), 
						ToolManager.getTool("sakai.samigo").getTitle(), cr
						.getContentType(), cr.streamContent(), cr.getProperties());
			}
		} catch (Exception e) {
			log.warn("Could not copy resource " + resourceId + ", " + e.getMessage());
		} finally{
			securityService.popAdvisor(securityAdvisor);
		}
		return cr_copy;
	}
	
	public ContentResource createCopyOfContentResource(String resourceId,
			String filename) {
		return createCopyOfContentResource(resourceId, filename, null);
	}

	/** These characters are not allowed in a resource id */
	public static final String INVALID_CHARS_IN_RESOURCE_ID = "^/\\{}[]()%*?#&=\n\r\t\b\f";

	protected static final String MAP_TO_A = "?";

	protected static final String MAP_TO_B = "§§";

	protected static final String MAP_TO_C = "?¢¢";

	protected static final String MAP_TO_E = "??¾®®";

	protected static final String MAP_TO_I = "";

	protected static final String MAP_TO_L = "££";

	protected static final String MAP_TO_N = "";

	protected static final String MAP_TO_O = "";

	protected static final String MAP_TO_U = "?";

	protected static final String MAP_TO_Y = "Ø´??";

	protected static final String MAP_TO_X = "???¤©»¨±?«µ¦À?";

	/**
	 * These characters are allowed; but if escapeResourceName() is called, they are escaped (actually, removed) Certain characters cause problems with filenames in certain OSes - so get rid of these characters in filenames
	 */
	protected static final String ESCAPE_CHARS_IN_RESOURCE_ID = ";'\"";
	
	/**
	 * Return a string based on id that is valid according to Resource name validity rules.
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped using Resource name validity rules.
	 */
	public static String escapeResourceName(String id)
	{
		if (id == null) return "";
		id = id.trim();
		try
		{
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < id.length(); i++)
			{
				char c = id.charAt(i);
				if (MAP_TO_A.indexOf(c) >= 0)
				{
					buf.append('a');
				}
				else if (MAP_TO_E.indexOf(c) >= 0)
				{
					buf.append('e');
				}
				else if (MAP_TO_I.indexOf(c) >= 0)
				{
					buf.append('i');
				}
				else if (MAP_TO_O.indexOf(c) >= 0)
				{
					buf.append('o');
				}
				else if (MAP_TO_U.indexOf(c) >= 0)
				{
					buf.append('u');
				}
				else if (MAP_TO_Y.indexOf(c) >= 0)
				{
					buf.append('y');
				}
				else if (MAP_TO_N.indexOf(c) >= 0)
				{
					buf.append('n');
				}
				else if (MAP_TO_B.indexOf(c) >= 0)
				{
					buf.append('b');
				}
				else if (MAP_TO_C.indexOf(c) >= 0)
				{
					buf.append('c');
				}
				else if (MAP_TO_L.indexOf(c) >= 0)
				{
					buf.append('l');
				}
				else if (MAP_TO_X.indexOf(c) >= 0)
				{
					buf.append('x');
				}
				else if (c < '\040')	// Remove any ascii control characters
				{
					buf.append('_');
				}
				else if (INVALID_CHARS_IN_RESOURCE_ID.indexOf(c) >= 0 || ESCAPE_CHARS_IN_RESOURCE_ID.indexOf(c) >= 0)
				{
					buf.append('_');
				}
				else
				{
					buf.append(c);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			log.warn("escapeResourceName: ", e);
			return id;
		}

	} // escapeResourceName
	
	public void copyAllAssessments(String fromContext, String toContext, Map<String, String>transversalMap) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
				.copyAllAssessments(fromContext, toContext, transversalMap);
			List<PublishedAssessmentFacade> publist =
			    PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
			    .getBasicInfoOfAllPublishedAssessments(PublishedAssessmentFacadeQueries.DUE, true, fromContext);
			for (PublishedAssessmentFacade facade: publist) {
			    PublishedAssessmentData data = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().loadPublishedAssessment(facade.getPublishedAssessmentId());
			    if (data != null) {
				String oldRef = PublishedAssessmentEntityProvider.ENTITY_PREFIX + "/" + data.getPublishedAssessmentId();
				String oldCore = CoreAssessmentEntityProvider.ENTITY_PREFIX + "/" + data.getAssessmentId();
				String newCore = transversalMap.get(oldCore);
				if (oldRef != null && newCore != null)
				    transversalMap.put(oldRef, newCore);
			    }
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public void copyAssessment(String assessmentId, String apepndCopyTitle) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.copyAssessment(assessmentId, apepndCopyTitle);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public List getAllActiveAssessmentsbyAgent(String fromContext) {
		try {
			return PersistenceService.getInstance().getAssessmentFacadeQueries()
					.getAllActiveAssessmentsByAgent(fromContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}
		/**
		 * Get the siteid for the given assesment
		 * @param assessmentId
		 * @return
		 */
	  public String getAssessmentSiteId(String assessmentId){
		    return PersistenceService.getInstance().getAssessmentFacadeQueries().getAssessmentSiteId(assessmentId);
	  }

	  public String getAssessmentCreatedBy(String assessmentId){
		    return PersistenceService.getInstance().getAssessmentFacadeQueries().getAssessmentCreatedBy(assessmentId);
	  }
	  
	  public Set copyItemAttachmentSet(ItemData newItem, Set itemAttachmentSet){
		    return PersistenceService.getInstance().getAssessmentFacadeQueries().copyItemAttachmentSet(newItem, itemAttachmentSet);
	  }

	  public static ContentHostingService getContentHostingService(){
		  return (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
	  }
	  
	  public List getFavoriteColChoicesbyAgent(String fromContext) {
		  try {
			  return PersistenceService.getInstance().getFavoriteColChoicesFacadeQueries()
			  .getFavoriteColChoicesByAgent(fromContext);
		  } catch (Exception e) {
			  log.error(e.getMessage(), e);
			  throw new RuntimeException(e);
		  }

	  }
	  
	  public String copyContentHostingAttachments(String text, String toContext) {
			if(text != null){
				ContentResource cr = null;

				String[] sources = StringUtils.splitByWholeSeparator(text, "src=\"");

				Set<String> attachments = new HashSet<String>();
				for (String source : sources) {
					String theHref = StringUtils.substringBefore(source, "\"");
					if (StringUtils.contains(theHref, "/access/content/")) {
						attachments.add(theHref);
					}
				}
				if (attachments.size() > 0) {
					log.info("Found " + attachments.size() + " attachments buried in question or answer text");
					SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
						@Override
						public SecurityAdvice isAllowed(String arg0, String arg1,
								String arg2) {
							if(ContentHostingService.AUTH_RESOURCE_READ.equals(arg1)){
								return SecurityAdvice.ALLOWED;
							}else{
								return SecurityAdvice.PASS;
							}
						}
					};
					try{
						securityService.pushAdvisor(securityAdvisor);
						for (String attachment : attachments) {
							String resourceIdOrig = "/" + StringUtils.substringAfter(attachment, "/access/content/");
							String resourceId = URLDecoder.decode(resourceIdOrig);
							String filename = StringUtils.substringAfterLast(attachment, "/");

							try {
								cr = AssessmentService.getContentHostingService().getResource(resourceId);
							} catch (IdUnusedException e) {
								log.warn("Could not find resource (" + resourceId + ") that was embedded in a question or answer");
							} catch (TypeException e) {
								log.warn("TypeException for resource (" + resourceId + ") that was embedded in a question or answer", e);
							} catch (PermissionException e) {
								log.warn("No permission for resource (" + resourceId + ") that was embedded in a question or answer");
							}

							if (cr != null && StringUtils.isNotEmpty(filename)) {

								ContentResource crCopy = createCopyOfContentResource(cr.getId(), filename, toContext);
								text = StringUtils.replace(text, resourceIdOrig, StringUtils.substringAfter(crCopy.getReference(), "/content"));
							}
						}
					}
					catch(Exception e){
						log.error(e.getMessage());
					}
					finally{
						securityService.popAdvisor(securityAdvisor);
					}
				}
			}
			return text;
		}


	/**
	 * Exports an assessment to mark up text
	 * 
	 * @param assessment
	 * @param bundle
	 * @return
	 */
	public String exportAssessmentToMarkupText(AssessmentFacade assessment, Map<String,String> bundle) {
		StringBuilder markupText = new StringBuilder(); 
		int nQuestion = 1;
		
		for (Object sectionObj : assessment.getSectionArray()) {
			SectionFacade section = (SectionFacade)sectionObj;
			List<ItemDataIfc> items = null;
			boolean hasRandomPartScore = false;
			Double score = null;
			boolean hasRandomPartDiscount = false;
			Double discount = null;
			
			if (StringUtils.equals(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE), SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString()))
  			{
				items = section.getItemArray();
  			}
			else 
			{
				String requestedScore = StringUtils.trimToEmpty(section.getSectionMetaDataByLabel(SectionDataIfc.POINT_VALUE_FOR_QUESTION));
						                 
				if (StringUtils.isNotEmpty(requestedScore)) {
					hasRandomPartScore = true;
					try {
						score = new Double(requestedScore);
					} catch (NumberFormatException e) {
						log.error("NumberFormatException converting to Double: " + requestedScore);
					}
				}
				
				String requestedDiscount = StringUtils.trimToEmpty(section.getSectionMetaDataByLabel(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION));

				if (StringUtils.isNotEmpty(requestedDiscount)) {
					hasRandomPartDiscount = true;
					try {
						discount = new Double(requestedDiscount);
					} catch (NumberFormatException e) {
						log.error("NumberFormatException converting to Double: " + requestedDiscount);
					}
				}
				
				QuestionPoolService qpService = new QuestionPoolService();
				try {
					Long sectionId = Long.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW));
					items = qpService.getAllItems(sectionId);
				} catch (NumberFormatException e) {
					log.error("NumberFormatException converting to Long: " + section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW));
				}
			}
  				
			for (ItemDataIfc item : items) 
			{
				// only exports these questions types
				if (!isQuestionTypeExportable2MarkupText(item.getTypeId())) {
					continue;
				}
				
				markupText.append(nQuestion).append(". ");
				if (hasRandomPartScore) {
					markupText.append("(").append(score).append(" ").append(bundle.get("points")).append(")");
				}
				else {
					markupText.append("(").append(item.getScore()).append(" ").append(bundle.get("points")).append(")");
				}
				if (hasRandomPartDiscount && discount != null && discount > 0) {
					markupText.append(" (").append(discount).append(" ").append(bundle.get("discount")).append(")");
				}
				else if (!hasRandomPartDiscount && item.getDiscount() != null && item.getDiscount() > 0) {
					markupText.append(" (").append(item.getDiscount()).append(" ").append(bundle.get("discount")).append(")");
				}
				
				for (ItemTextIfc itemText : item.getItemTextArray()) {
					markupText.append("\n");
					if (TypeIfc.FILL_IN_BLANK.intValue() == item.getTypeId() 
							|| TypeIfc.FILL_IN_NUMERIC.intValue() == item.getTypeId()) {
						markupText.append(itemText.getText().replaceAll("\\{\\}", ""));
					}
					else {
						markupText.append(itemText.getText());
					}
					
					// Answer in Essay question's doesn't need to be exported  
					if (TypeIfc.ESSAY_QUESTION.intValue() == item.getTypeId()) {
						continue;
					}

					for (AnswerIfc answer : itemText.getAnswerArray()) {
						markupText.append("\n");
						
						if (answer.getIsCorrect()) {
							markupText.append("*");
						}
    					if (TypeIfc.MULTIPLE_CHOICE.intValue() == item.getTypeId() 
    							|| TypeIfc.MULTIPLE_CORRECT.intValue() == item.getTypeId()) {
    						markupText.append(answer.getLabel()).append(". ");
    					}
    					
    					if (TypeIfc.FILL_IN_NUMERIC.intValue() == item.getTypeId()) {
    						markupText.append("{").append(answer.getText()).append("}");
    					}
    					else if (TypeIfc.TRUE_FALSE.intValue() == item.getTypeId()) {
    						String boolText = bundle.get("false");
    						if (Boolean.parseBoolean(answer.getText())) {
    							boolText = bundle.get("true");
    						}
    						markupText.append(boolText);
    					}
    					else {
    						markupText.append(answer.getText());
    					}
					}
				}
				
				String randomized = item.getItemMetaDataByLabel(ItemMetaDataIfc.RANDOMIZE);
				if (randomized != null && Boolean.valueOf(randomized)) {
					markupText.append("\n");
					markupText.append(bundle.get("randomize"));
				}
				
				if (item.getHasRationale() != null && item.getHasRationale()) {
					markupText.append("\n");
					markupText.append(bundle.get("rationale"));
				}
				
				if (StringUtils.isNotEmpty(item.getCorrectItemFeedback())) {
					markupText.append("\n");
					markupText.append("#FBOK:").append(item.getCorrectItemFeedback());
				}
				
				if (StringUtils.isNotEmpty(item.getInCorrectItemFeedback())) {
					markupText.append("\n");
					markupText.append("#FBNOK:").append(item.getInCorrectItemFeedback());
				}
				markupText.append("\n");
				
				nQuestion++;
			}
		}
		
		return markupText.toString();
	}	  
    
	/**
	 * Check if there are questions not exportable to markup text
	 * 
	 * @param assessment
	 * @return
	 */
	public boolean isExportable(AssessmentFacade assessment) {
		boolean exportToMarkupText = false;
		
		for (Object sectionObj : assessment.getSectionArray()) {
			SectionFacade section = (SectionFacade)sectionObj;
			List<ItemDataIfc> items = null;
			if (section != null) {
				if (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE) == null || StringUtils.equals(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE), SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString()))
				{
					items = section.getItemArray();
				}
				else if (StringUtils.equals(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE), SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))
				{
					QuestionPoolService qpService = new QuestionPoolService();
					try {
						Long qpId = Long.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW));
						items = qpService.getAllItems(qpId);
					} catch (NumberFormatException e) {
						log.error("NumberFormatException converting to Long: " + section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW));
					}
				}
			}
			if (items == null) {
				log.info("Items for assessment {} section {} is null in isExportable", assessment.getAssessmentId(), section.getSectionId());
			}
			else {
				for (ItemDataIfc item : items) 
				{
					// only exports these questions types
					if (isQuestionTypeExportable2MarkupText(item.getTypeId())) {
						exportToMarkupText = true;
						break;
					}
				}
			}
			if (exportToMarkupText) {
				break;
			}
		}
		
		return exportToMarkupText;
	}
	
	/**
	 * Return true if the item can be exportable to mark up text
	 * 
	 * @param itemTypeId
	 * @return
	 */
	public boolean isQuestionTypeExportable2MarkupText(Long itemTypeId) {
		boolean exportable = false;
		
		exportable = exportable || (TypeIfc.MULTIPLE_CHOICE.intValue() == itemTypeId.intValue());
		exportable = exportable || (TypeIfc.MULTIPLE_CORRECT.intValue() == itemTypeId.intValue());
		exportable = exportable || (TypeIfc.FILL_IN_BLANK.intValue() == itemTypeId.intValue());
		exportable = exportable || (TypeIfc.FILL_IN_NUMERIC.intValue() == itemTypeId.intValue());
		exportable = exportable || (TypeIfc.TRUE_FALSE.intValue() == itemTypeId.intValue());
		exportable = exportable || (TypeIfc.ESSAY_QUESTION.intValue() == itemTypeId.intValue());
				
		return exportable;
	}

	public static String copyStringAttachment(String stringWithAttachment) {
		AssessmentService assessmentService = new AssessmentService();
		
		if(AgentFacade.getCurrentSiteId()!=null){
			return assessmentService.copyContentHostingAttachments(stringWithAttachment, AgentFacade.getCurrentSiteId());
		}
		
		return stringWithAttachment;
	}
}
