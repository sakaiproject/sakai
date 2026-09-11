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

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.samigo.api.SamigoReferenceReckoner;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTextAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTextAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class PublishedAssessmentFacadeQueries implements PublishedAssessmentFacadeQueriesAPI {

	@Setter private SiteService siteService;
	@Setter private ToolManager toolManager;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private SessionFactory sessionFactory;

	public static final String STARTDATE = "assessmentAccessControl.startDate";

	public static final String DUEDATE = "assessmentAccessControl.dueDate";

	public static final String RETRACTDATE = "assessmentAccessControl.retractDate";

	public static final String RELEASETO = "assessmentAccessControl.releaseTo";

	public static final String PUB_RELEASETO = "releaseTo";

	public static final String PUB_STARTDATE = "startDate";

	public static final String PUB_DUEDATE = "dueDate";

	public static final String TITLE = "title";

	public static final String GRADE = "finalScore";

	public static final String DUE = "dueDate";

	public static final String RAW = "totalAutoScore";

	public static final String TIME = "timeElapsed";

	public static final String SUBMITTED = "submittedDate";

	public static final String SITECONTENTPATH = "/access/content/group/";

	public PublishedAssessmentFacadeQueries() {
	}

	public IdImpl getId(String id) {
		return new IdImpl(id);
	}

	public IdImpl getId(Long id) {
		return new IdImpl(id);
	}

	public IdImpl getId(long id) {
		return new IdImpl(id);
	}

	public IdImpl getAssessmentId(String id) {
		return new IdImpl(id);
	}

	public IdImpl getAssessmentId(Long id) {
		return new IdImpl(id);
	}

	public IdImpl getAssessmentId(long id) {
		return new IdImpl(id);
	}

	public IdImpl getAssessmentTemplateId(String id) {
		return new IdImpl(id);
	}

	public IdImpl getAssessmentTemplateId(Long id) {
		return new IdImpl(id);
	}

	public IdImpl getAssessmentTemplateId(long id) {
		return new IdImpl(id);
	}

	public PublishedAssessmentData preparePublishedAssessment(AssessmentData a){
		String protocol = ServerConfigurationService.getServerUrl();
		PublishedAssessmentData publishedAssessment = new PublishedAssessmentData(
				a.getTitle(), a.getDescription(), a.getComments(),
				TypeFacade.HOMEWORK, a.getInstructorNotification(), a
						.getTesteeNotification(), a.getMultipartAllowed(), a
						.getStatus(), AgentFacade.getAgentString(), new Date(),
				AgentFacade.getAgentString(), new Date());
		// publishedAssessment.setAssessment(a);
		publishedAssessment.setAssessmentId(a.getAssessmentBaseId());

		// section set
		Set publishedSectionSet = preparePublishedSectionSet(
				publishedAssessment, a.getSectionSet(), protocol);
		publishedAssessment.setSectionSet(publishedSectionSet);

		// access control
		PublishedAccessControl publishedAccessControl = preparePublishedAccessControl(
				publishedAssessment, (AssessmentAccessControl) a
						.getAssessmentAccessControl());
		publishedAssessment.setAssessmentAccessControl(publishedAccessControl);

		// evaluation model
		PublishedEvaluationModel publishedEvaluationModel = preparePublishedEvaluationModel(
				publishedAssessment, (EvaluationModel) a.getEvaluationModel());
		publishedAssessment.setEvaluationModel(publishedEvaluationModel);

		// feedback
		PublishedFeedback publishedFeedback = preparePublishedFeedback(
				publishedAssessment, (AssessmentFeedback) a
						.getAssessmentFeedback());
		publishedAssessment.setAssessmentFeedback(publishedFeedback);

		// metadata
		Set publishedMetaDataSet = preparePublishedMetaDataSet(
				publishedAssessment, a.getAssessmentMetaDataSet());
		log.debug("******* metadata set: {}", a.getAssessmentMetaDataSet());
		log.debug("******* published metadata set: {}", publishedMetaDataSet);
		publishedAssessment.setAssessmentMetaDataSet(publishedMetaDataSet);

		// IPAddress
		Set publishedIPSet = preparePublishedSecuredIPSet(publishedAssessment,
				a.getSecuredIPAddressSet());
		publishedAssessment.setSecuredIPAddressSet(publishedIPSet);

		// attachmentSet
		Set publishedAssessmentAttachmentSet = preparePublishedAssessmentAttachmentSet(
				publishedAssessment, a.getAssessmentAttachmentSet(), protocol);
		publishedAssessment
				.setAssessmentAttachmentSet(publishedAssessmentAttachmentSet);

		publishedAssessment.setCategoryId(a.getCategoryId());

		return publishedAssessment;
	}

	public PublishedFeedback preparePublishedFeedback(
			PublishedAssessmentData p, AssessmentFeedback a) {
		if (a == null) {
			return null;
		}
		PublishedFeedback publishedFeedback = new PublishedFeedback(a
				.getFeedbackDelivery(), a.getFeedbackComponentOption(),a.getFeedbackAuthoring(), a
				.getEditComponents(), a.getShowQuestionText(), a
				.getShowStudentResponse(), a.getShowCorrectResponse(), a
				.getShowStudentScore(), a.getShowStudentQuestionScore(), a
				.getShowQuestionLevelFeedback(), a
				.getShowSelectionLevelFeedback(), a.getShowGraderComments(), a
				.getShowStatistics(), a.getShowCorrection());
		publishedFeedback.setAssessmentBase(p);
		return publishedFeedback;
	}

	public PublishedAccessControl preparePublishedAccessControl(
			PublishedAssessmentData p, AssessmentAccessControl a) {
		if (a == null) {
			return new PublishedAccessControl();
		}

		// If instructor does an instant-publish without viewing settings, we may not have a start date
		final Date startDate = a.getStartDate() != null ? a.getStartDate() : new Date();
		PublishedAccessControl publishedAccessControl = new PublishedAccessControl(
				a.getSubmissionsAllowed(), a.getSubmissionsSaved(), a
						.getAssessmentFormat(), a.getBookMarkingItem(), a
						.getTimeLimit(), a.getTimedAssessment(), a
						.getRetryAllowed(), a.getLateHandling(), a.getInstructorNotification(),
						 startDate, a.getDueDate(), a.getScoreDate(), a
						.getFeedbackDate());
		publishedAccessControl.setRetractDate(a.getRetractDate());
		publishedAccessControl.setAutoSubmit(a.getAutoSubmit());
		publishedAccessControl.setItemNavigation(a.getItemNavigation());
		publishedAccessControl.setItemNumbering(a.getItemNumbering());
		publishedAccessControl.setDisplayScoreDuringAssessments(a.getDisplayScoreDuringAssessments());
		publishedAccessControl.setSubmissionMessage(a.getSubmissionMessage());
		publishedAccessControl.setReleaseTo(a.getReleaseTo());
		publishedAccessControl.setPassword(a.getPassword());
		publishedAccessControl.setFinalPageUrl(a.getFinalPageUrl());
		publishedAccessControl.setUnlimitedSubmissions(a
				.getUnlimitedSubmissions());
		publishedAccessControl.setMarkForReview(a.getMarkForReview());
		publishedAccessControl.setHonorPledge(a.getHonorPledge());
		publishedAccessControl.setFeedbackEndDate(a.getFeedbackEndDate());
		publishedAccessControl.setFeedbackScoreThreshold(a.getFeedbackScoreThreshold());
		publishedAccessControl.setAssessmentBase(p);
		return publishedAccessControl;
	}

	public PublishedEvaluationModel preparePublishedEvaluationModel(
			PublishedAssessmentData p, EvaluationModel e) {
		if (e == null) {
			return null;
		}
		PublishedEvaluationModel publishedEvaluationModel = new PublishedEvaluationModel(
				e.getEvaluationComponents(), e.getScoringType(), e
						.getNumericModelId(), e.getFixedTotalScore(), e
						.getGradeAvailable(), e.getIsStudentIdPublic(), e
						.getAnonymousGrading(), e.getAutoScoring(), e
						.getToGradeBook());
		publishedEvaluationModel.setAssessmentBase(p);
		return publishedEvaluationModel;
	}

	public Set preparePublishedMetaDataSet(PublishedAssessmentData p,
			Set metaDataSet) {
		Set<PublishedMetaData> h = new HashSet<>();
		Iterator<AssessmentMetaData> i = metaDataSet.iterator();
		while (i.hasNext()) {
			AssessmentMetaData metaData = (AssessmentMetaData) i.next();
			PublishedMetaData publishedMetaData = new PublishedMetaData(p,
					metaData.getLabel(), metaData.getEntry());
			h.add(publishedMetaData);
		}
		return h;
	}

	public Set preparePublishedSecuredIPSet(PublishedAssessmentData p, Set ipSet) {
		Set<PublishedSecuredIPAddress> h = new HashSet<>();
		Iterator<SecuredIPAddress> i = ipSet.iterator();
		while (i.hasNext()) {
			SecuredIPAddress ip = (SecuredIPAddress) i.next();
			PublishedSecuredIPAddress publishedIP = new PublishedSecuredIPAddress(
					p, ip.getHostname(), ip.getIpAddress());
			h.add(publishedIP);
		}
		return h;
	}

	public Set preparePublishedSectionSet(
			PublishedAssessmentData publishedAssessment, Set sectionSet,
			String protocol) {
		log.debug("**published section size = {}", sectionSet.size());
		Set<PublishedSectionData> h = new HashSet<>();
		Iterator<SectionData> i = sectionSet.iterator();
		while (i.hasNext()) {
			SectionData section = (SectionData) i.next();

			// TODO note: 4/28 need to check if a part is random draw , if it is
			// then need to add questions from pool to this section, at this
			// point,

			PublishedSectionData publishedSection = new PublishedSectionData(
					section.getDuration(), section.getSequence(), section
							.getTitle(), section.getDescription(), section
							.getTypeId(), section.getStatus(), section
							.getCreatedBy(), section.getCreatedDate(), section
							.getLastModifiedBy(), section.getLastModifiedDate());
			Set publishedSectionAttachmentSet = preparePublishedSectionAttachmentSet(
					publishedSection, section.getSectionAttachmentSet(),
					protocol);
			publishedSection
					.setSectionAttachmentSet(publishedSectionAttachmentSet);
			Set publishedItemSet = preparePublishedItemSet(publishedSection,
					section.getItemSet(), protocol);
			publishedSection.setItemSet(publishedItemSet);
			Set publishedMetaDataSet = preparePublishedSectionMetaDataSet(
					publishedSection, section.getSectionMetaDataSet());
			publishedSection.setSectionMetaDataSet(publishedMetaDataSet);
			publishedSection.setAssessment(publishedAssessment);
			h.add(publishedSection);
		}
		return h;
	}

	public Set preparePublishedSectionMetaDataSet(
			PublishedSectionData publishedSection, Set metaDataSet) {
		Set<PublishedSectionMetaData> h = new HashSet<>();
		Iterator<SectionMetaData> n = metaDataSet.iterator();
		while (n.hasNext()) {
			SectionMetaData sectionMetaData = (SectionMetaData) n.next();
			PublishedSectionMetaData publishedSectionMetaData = new PublishedSectionMetaData(
					publishedSection, sectionMetaData.getLabel(),
					sectionMetaData.getEntry());
			h.add(publishedSectionMetaData);
		}
		// Persist the random seed in the section to use it and preserve the order.
		h.add(new PublishedSectionMetaData(publishedSection, SectionDataIfc.RANDOMIZATION_SEED, String.valueOf(UUID.randomUUID().hashCode())));
		return h;
	}

	public Set preparePublishedItemSet(PublishedSectionData publishedSection,
			Set itemSet, String protocol) {
		log.debug("**published item size = {}", itemSet.size());
		Set<PublishedItemData> h = new HashSet<>();
		Iterator<ItemData> j = itemSet.iterator();
		while (j.hasNext()) {
			ItemData item = (ItemData) j.next();
			PublishedItemData publishedItem = new PublishedItemData(
					publishedSection, item.getSequence(), item.getDuration(),
					item.getInstruction(), item.getDescription(), item
							.getTypeId(), item.getGrade(), item.getScore(), item.getScoreDisplayFlag(), item.getDiscount(), item.getMinScore(),
					item.getHint(), item.getHasRationale(), item.getStatus(),
					item.getCreatedBy(), item.getCreatedDate(), item
							.getLastModifiedBy(), item.getLastModifiedDate(),
					null, null, null, // set ItemTextSet, itemMetaDataSet and
					// itemFeedbackSet later
					item.getTriesAllowed(), item.getPartialCreditFlag(),item.getHash(),item.getHash(),
					item.getItemId());
			Set publishedItemTextSet = preparePublishedItemTextSet(
					publishedItem, item.getItemTextSet(), protocol);
			Set publishedItemMetaDataSet = preparePublishedItemMetaDataSet(
					publishedItem, item.getItemMetaDataSet());
			Set publishedItemTagSet = preparePublishedItemTagSet(
					publishedItem, item.getItemTagSet());
			Set publishedItemFeedbackSet = preparePublishedItemFeedbackSet(
					publishedItem, item.getItemFeedbackSet());
			Set publishedItemAttachmentSet = preparePublishedItemAttachmentSet(
					publishedItem, item.getItemAttachmentSet(), protocol);
			publishedItem.setItemTextSet(publishedItemTextSet);
			publishedItem.setItemMetaDataSet(publishedItemMetaDataSet);
			publishedItem.setItemTagSet(publishedItemTagSet);
			publishedItem.setItemFeedbackSet(publishedItemFeedbackSet);
			publishedItem.setItemAttachmentSet(publishedItemAttachmentSet);
			publishedItem.setAnswerOptionsRichCount(item.getAnswerOptionsRichCount());
			publishedItem.setAnswerOptionsSimpleOrRich(item.getAnswerOptionsSimpleOrRich());
			publishedItem.setIsExtraCredit(item.getIsExtraCredit());
			publishedItem.setIsFixed(item.getIsFixed());

			h.add(publishedItem);
		}
		return h;
	}

	public Set preparePublishedItemTextSet(PublishedItemData publishedItem,
			Set itemTextSet, String protocol) {
		log.debug("**published item text size = {}", itemTextSet.size());
		Set<PublishedItemText> h = new HashSet<>();
		Iterator<ItemText> k = itemTextSet.iterator();
		while (k.hasNext()) {
			ItemText itemText = (ItemText) k.next();
			log.debug("**item text id = {}", itemText.getId());
			PublishedItemText publishedItemText = new PublishedItemText(
					publishedItem, itemText.getSequence(), itemText.getText(),
					null);
			Set publishedAnswerSet = preparePublishedAnswerSet(
					publishedItemText, itemText.getAnswerSet());
			publishedItemText.setAnswerSet(publishedAnswerSet);
			
			Set publishedItemTextAttachmentSet = this.preparePublishedItemTextAttachmentSet(publishedItemText, 
					itemText.getItemTextAttachmentSet(), protocol);
			publishedItemText.setItemTextAttachmentSet(publishedItemTextAttachmentSet);
			publishedItemText.setRequiredOptionsCount(itemText.getRequiredOptionsCount());
			publishedItemText.setAddedButNotExtracted(itemText.isAddedButNotExtracted());
			h.add(publishedItemText);
		}
		return h;
	}

	public Set preparePublishedItemMetaDataSet(PublishedItemData publishedItem,
			Set itemMetaDataSet) {
		Set<PublishedItemMetaData> h = new HashSet<>();
		Iterator<ItemMetaData> n = itemMetaDataSet.iterator();
		while (n.hasNext()) {
			ItemMetaData itemMetaData = (ItemMetaData) n.next();
			// The itemMetaData.getEntry() is actually the pending/core part id. 
			// What should be used is the published part id.
			// However, the published part id has not been created at this point.
			// Therefore, we have to update it later.
			// I really don't think this is good. I would like to remove PARTID
			// from the ItemMetaData. However, there are lots of changes involved and
			// I don't have time for this now. Will do it in later release. 
			PublishedItemMetaData publishedItemMetaData = new PublishedItemMetaData(
					publishedItem, itemMetaData.getLabel(), itemMetaData
							.getEntry());
			h.add(publishedItemMetaData);
		}
		return h;
	}

	public Set preparePublishedItemFeedbackSet(PublishedItemData publishedItem,
			Set itemFeedbackSet) {
		Set<PublishedItemFeedback> h = new HashSet<>();
		Iterator<ItemFeedback> o = itemFeedbackSet.iterator();
		while (o.hasNext()) {
			ItemFeedback itemFeedback = (ItemFeedback) o.next();
			PublishedItemFeedback publishedItemFeedback = new PublishedItemFeedback(
					publishedItem, itemFeedback.getTypeId(), itemFeedback.getText(), itemFeedback.getTextValue());
			h.add(publishedItemFeedback);
		}
		return h;
	}

	public Set preparePublishedItemAttachmentSet(
			PublishedItemData publishedItem, Set itemAttachmentSet,
			String protocol) {
		Set<PublishedItemAttachment> h = new HashSet<>();
		Iterator<ItemAttachment> o = itemAttachmentSet.iterator();
		while (o.hasNext()) {
			ItemAttachment itemAttachment = (ItemAttachment) o.next();
			try {
				// create a copy of the resource
				AssessmentService service = new AssessmentService();
				ContentResource cr_copy = service.createCopyOfContentResource(
						itemAttachment.getResourceId(), itemAttachment
								.getFilename());
				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				PublishedItemAttachment publishedItemAttachment = new PublishedItemAttachment(
						null, publishedItem, cr_copy.getId(), itemAttachment
								.getFilename(), itemAttachment.getMimeType(),
						itemAttachment.getFileSize(), itemAttachment
								.getDescription(), url, itemAttachment
								.getIsLink(), itemAttachment.getStatus(),
						itemAttachment.getCreatedBy(), itemAttachment
								.getCreatedDate(), itemAttachment
								.getLastModifiedBy(), itemAttachment
								.getLastModifiedDate());
				h.add(publishedItemAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}

    public Set preparePublishedItemTagSet(PublishedItemData publishedItem,
                                          Set itemTagSet) {
        Set<PublishedItemTag> h = new HashSet<>();
        Iterator<ItemTag> n = itemTagSet.iterator();
        while (n.hasNext()) {
            ItemTag itemTag = (ItemTag) n.next();
            PublishedItemTag publishedItemTag = new PublishedItemTag(publishedItem,
                    itemTag.getTagId(), itemTag.getTagLabel(),
                    itemTag.getTagCollectionId(), itemTag.getTagCollectionName());
            h.add(publishedItemTag);
        }
        return h;
    }

	public Set preparePublishedItemTextAttachmentSet(
			PublishedItemText publishedItemText, Set itemTextAttachmentSet,
			String protocol) {
		Set<PublishedItemTextAttachment> h = new HashSet<>();
		Iterator<ItemTextAttachment> o = itemTextAttachmentSet.iterator();
		while (o.hasNext()) {
			ItemTextAttachment itemTextAttachment = (ItemTextAttachment) o.next();
			try {
				// create a copy of the resource
				AssessmentService service = new AssessmentService();
				ContentResource cr_copy = service.createCopyOfContentResource(
						itemTextAttachment.getResourceId(), itemTextAttachment
								.getFilename());
				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				PublishedItemTextAttachment publishedItemTextAttachment = new PublishedItemTextAttachment(
						null, publishedItemText, cr_copy.getId(), itemTextAttachment
								.getFilename(), itemTextAttachment.getMimeType(),
						itemTextAttachment.getFileSize(), itemTextAttachment
								.getDescription(), url, itemTextAttachment
								.getIsLink(), itemTextAttachment.getStatus(),
						itemTextAttachment.getCreatedBy(), itemTextAttachment
								.getCreatedDate(), itemTextAttachment
								.getLastModifiedBy(), itemTextAttachment
								.getLastModifiedDate());
				h.add(publishedItemTextAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}
	
	public String getRelativePath(String url, String protocol) {
		// replace whitespace with %20
		url = replaceSpace(url);
		String location = url;
		int index = url.lastIndexOf(protocol);
		if (index == 0) {
			location = url.substring(protocol.length());
		}
		return location;
	}

	public Set preparePublishedSectionAttachmentSet(
			PublishedSectionData publishedSection, Set sectionAttachmentSet,
			String protocol) {
		Set<PublishedSectionAttachment> h = new HashSet<>();
		Iterator<SectionAttachment> o = sectionAttachmentSet.iterator();
		while (o.hasNext()) {
			SectionAttachment sectionAttachment = (SectionAttachment) o.next();
			try {
				// create a copy of the resource
				AssessmentService service = new AssessmentService();
				ContentResource cr_copy = service.createCopyOfContentResource(
						sectionAttachment.getResourceId(), sectionAttachment
								.getFilename());

				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				PublishedSectionAttachment publishedSectionAttachment = new PublishedSectionAttachment(
						null, publishedSection, cr_copy.getId(),
						sectionAttachment.getFilename(), sectionAttachment
								.getMimeType(),
						sectionAttachment.getFileSize(), sectionAttachment
								.getDescription(), url, sectionAttachment
								.getIsLink(), sectionAttachment.getStatus(),
						sectionAttachment.getCreatedBy(), sectionAttachment
								.getCreatedDate(), sectionAttachment
								.getLastModifiedBy(), sectionAttachment
								.getLastModifiedDate());
				h.add(publishedSectionAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}

	public Set preparePublishedAssessmentAttachmentSet(
			PublishedAssessmentData publishedAssessment,
			Set assessmentAttachmentSet, String protocol) {
		Set<PublishedAssessmentAttachment> h = new HashSet<>();
		Iterator<AssessmentAttachment> o = assessmentAttachmentSet.iterator();
		while (o.hasNext()) {
			AssessmentAttachment assessmentAttachment = (AssessmentAttachment) o
					.next();
			try {
				// create a copy of the resource
				AssessmentService service = new AssessmentService();
				ContentResource cr_copy = service.createCopyOfContentResource(
						assessmentAttachment.getResourceId(),
						assessmentAttachment.getFilename());

				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				PublishedAssessmentAttachment publishedAssessmentAttachment = new PublishedAssessmentAttachment(
						null, publishedAssessment, cr_copy.getId(),
						assessmentAttachment.getFilename(),
						assessmentAttachment.getMimeType(),
						assessmentAttachment.getFileSize(),
						assessmentAttachment.getDescription(), url,
						assessmentAttachment.getIsLink(), assessmentAttachment
								.getStatus(), assessmentAttachment
								.getCreatedBy(), assessmentAttachment
								.getCreatedDate(), assessmentAttachment
								.getLastModifiedBy(), assessmentAttachment
								.getLastModifiedDate());
				h.add(publishedAssessmentAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}

	public Set preparePublishedAnswerSet(PublishedItemText publishedItemText,
			Set answerSet) {
		log.debug("**published answer size = {}", answerSet.size());
		Set<PublishedAnswer> h = new HashSet<>();
		Iterator<Answer> l = answerSet.iterator();
		while (l.hasNext()) {
			Answer answer = (Answer) l.next();
			PublishedAnswer publishedAnswer = new PublishedAnswer(
					publishedItemText, answer.getText(), answer.getSequence(),
					answer.getLabel(), answer.getIsCorrect(),
					answer.getGrade(), answer.getScore(), answer.getPartialCredit(), answer.getDiscount(), 
					//answer.getCorrectOptionLabels(), 
					null);
			Set publishedAnswerFeedbackSet = preparePublishedAnswerFeedbackSet(
					publishedAnswer, answer.getAnswerFeedbackSet());
			publishedAnswer.setAnswerFeedbackSet(publishedAnswerFeedbackSet);
			h.add(publishedAnswer);
		}
		return h;
	}

	public Set preparePublishedAnswerFeedbackSet(
			PublishedAnswer publishedAnswer, Set answerFeedbackSet) {
		Set<PublishedAnswerFeedback> h = new HashSet<>();
		Iterator<AnswerFeedback> m = answerFeedbackSet.iterator();
		while (m.hasNext()) {
			AnswerFeedback answerFeedback = (AnswerFeedback) m.next();
			PublishedAnswerFeedback publishedAnswerFeedback = new PublishedAnswerFeedback(
					publishedAnswer, answerFeedback.getTypeId(), answerFeedback
							.getText());
			h.add(publishedAnswerFeedback);
		}
		return h;
	}

	/**
	 * This was created for org.sakaiproject.grading.api.GradingService.
	 * We just want a quick answer whether Samigo is responsible for an id.
	 */
	public boolean isPublishedAssessmentIdValid(Long publishedAssessmentId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);
		Root<PublishedAssessmentData> root = cq.from(PublishedAssessmentData.class);

		cq.select(root)
			.where(cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId));

		List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

		if (!list.isEmpty()) {
			PublishedAssessmentData f = list.get(0);
			return f.getPublishedAssessmentId() > 0;
		}
		return false;
	}

	public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId) {
		return getPublishedAssessment(assessmentId, true);
	}

	/**
	 * This was created for extended time because the code to get the sections
	 * was causing slow performance and we don't need that info for extended
	 * time.
	 */
	public PublishedAssessmentFacade getPublishedAssessmentQuick(Long assessmentId) {
		PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
		PublishedAssessmentFacade f = new PublishedAssessmentFacade(a, false);
		f.setStartDate(a.getStartDate());
		f.setDueDate(a.getDueDate());
		f.setRetractDate(a.getRetractDate());
		f.setTimeLimit(a.getTimeLimit());
		return f;
	}

	public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId, boolean withGroupsInfo) {
		PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
		if (a == null) {
			log.debug("Published assessment not found for assessmentId: {}", assessmentId);
			return null;
		}
		a.setSectionSet(getSectionSetForAssessment(a)); // this is making things slow -pbd
		Map<String, String> releaseToGroups = new HashMap<>();
		Set<String> groupReferences = new HashSet<>();
		if (withGroupsInfo) {
			//TreeMap groupsForSite = getGroupsForSite();
			
			// SAM-799
            String siteId = getPublishedAssessmentSiteId(assessmentId.toString());
            Map groupsForSite = getGroupsForSite(siteId);
			releaseToGroups = getReleaseToGroups(groupsForSite, assessmentId);
			groupReferences = releaseToGroups.keySet().stream().map(id -> siteService.siteGroupReference(siteId, id)).collect(Collectors.toSet());
		}
		
		PublishedAssessmentFacade f = new PublishedAssessmentFacade(a, releaseToGroups);
		f.setGroupReferences(groupReferences);
		return f;
	}
	
	public Long getPublishedAssessmentId(Long assessmentId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);
		Root<PublishedAssessmentData> root = cq.from(PublishedAssessmentData.class);

		cq.select(root)
			.where(cb.equal(root.get("assessmentId"), assessmentId))
			.orderBy(cb.desc(root.get("createdDate")));

		List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();
		Long publishedId = 0L;
		if (!list.isEmpty()) {
			PublishedAssessmentData f = list.get(0);
			publishedId = f.getPublishedAssessmentId();
		}
		return publishedId;
	}

	public PublishedAssessmentFacade publishAssessment(
			AssessmentFacade assessment) throws Exception {
		PublishedAssessmentData publishedAssessment = preparePublishedAssessment(
				(AssessmentData) assessment.getData());

		try {
			saveOrUpdate(publishedAssessment);
		} catch (Exception e) {
			throw e;
		}

		// reset PARTID in ItemMetaData to the section of the newly created section
		// I really don't think PARTID should be in ItemMetaData. However, there will
		// be lots of changes invloved if I remove PARTID from ItemMetaData. I need
		// to spend time to evaulate and make the changes - not able to do this at
		// this point.
		Set sectionSet = publishedAssessment.getSectionSet();
		Iterator sectionIter = sectionSet.iterator();
		while (sectionIter.hasNext()) {
			PublishedSectionData section = (PublishedSectionData) sectionIter.next();
			Set itemSet = section.getItemSet();
			Iterator itemIter = itemSet.iterator();
			while (itemIter.hasNext()) {
				PublishedItemData item = (PublishedItemData) itemIter.next();
				replaceEmbeddedSiteIdsForItem(item);
				Set itemMetaDataSet = item.getItemMetaDataSet();
				Iterator itemMetaDataIter = itemMetaDataSet.iterator();
				while (itemMetaDataIter.hasNext()) {
					PublishedItemMetaData itemMetaData = (PublishedItemMetaData) itemMetaDataIter.next();
					if (itemMetaData.getLabel() != null && itemMetaData.getLabel().equals(ItemMetaDataIfc.PARTID)) {
						log.debug("sectionId = " + section.getSectionId());
						itemMetaData.setEntry(section.getSectionId().toString());
					}
				}
			}
		}

		PublishedAssessmentFacade publishedAssessmentFacade = new PublishedAssessmentFacade(publishedAssessment);

		// add to gradebook
		if (publishedAssessment.getEvaluationModel() != null) {
			String toGradebook = publishedAssessment.getEvaluationModel()
					.getToGradeBook();

			boolean integrated = IntegrationContextFactory.getInstance()
					.isIntegrated();
			org.sakaiproject.grading.api.GradingService g = null;
			if (integrated) {
				g = (org.sakaiproject.grading.api.GradingService) SpringBeanLocator.getInstance().getBean(
						"org.sakaiproject.grading.api.GradingService");
			}

			// write authorization
			createAuthorization(publishedAssessment);

			GradebookServiceHelper gbsHelper = IntegrationContextFactory
					.getInstance().getGradebookServiceHelper();

			if (toGradebook != null && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
				try {
                    Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
                    String ref = SamigoReferenceReckoner.reckoner().site(site.getId()).subtype("p")
                                    .id(publishedAssessmentFacade.getPublishedAssessmentId().toString()).reckon().getReference();
                    publishedAssessment.setReference(ref);

					Map groupsForSite = getGroupsForSite(AgentFacade.getCurrentSiteId());
					Map<String, String> groupMap = getReleaseToGroups(groupsForSite, publishedAssessment.getPublishedAssessmentId());
					List<String> selectedGroups = groupMap.keySet().stream().collect(Collectors.toList());

					gbsHelper.buildItemToGradebook(publishedAssessment, selectedGroups, g);
				} catch (Exception e) {
					log.error("Removing published assessment: " + e);
					delete(publishedAssessment);
					throw e;
				}
			}
		}


		return publishedAssessmentFacade;
	}

	// This method is specific for publish an assessment for preview assessment,
	// because it will be deleted after preview is done, and shouldn't talk to
	// gradebook.
	public PublishedAssessmentFacade publishPreviewAssessment(
			AssessmentFacade assessment) {
		// boolean addedToGradebook = false;
		PublishedAssessmentData publishedAssessment = preparePublishedAssessment(
				(AssessmentData) assessment.getData());
		publishedAssessment.setStatus(PublishedAssessmentIfc.DEAD_STATUS);
		try {
			saveOrUpdate(publishedAssessment);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		// write authorization
		createAuthorization(publishedAssessment);
		
		return new PublishedAssessmentFacade(publishedAssessment);
	}

	@Override
    public void createAuthorization(PublishedAssessmentData p) {
		// conditional processing for groups
		if (p.getAssessmentAccessControl().getReleaseTo() != null
				&& p.getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
			createAuthorizationForSelectedGroups(p);
			return;
		}

		// We are only dealing with ANON or the site here! Groups is handled above!
		AssessmentAccessControlIfc control = p.getAssessmentAccessControl();
		String s = (control.getReleaseTo() != null && control.getReleaseTo().trim().equalsIgnoreCase("Anonymous Users")) ? "ANONYMOUS_USERS" : AgentFacade.getCurrentSiteId();

		// 3. give selected site right to view Published Assessment
		final String qualifierIdString = p.getPublishedAssessmentId().toString();
		PersistenceService.getInstance().getAuthzQueriesFacade()
				.createAuthorization(AgentFacade.getCurrentSiteId(), "OWN_PUBLISHED_ASSESSMENT", qualifierIdString);

		// 4. create authorization for the target
		log.debug("** agentId={}", s);
		PersistenceService.getInstance().getAuthzQueriesFacade()
				.createAuthorization(s, "TAKE_PUBLISHED_ASSESSMENT", qualifierIdString);
		PersistenceService.getInstance().getAuthzQueriesFacade()
				.createAuthorization(s, "VIEW_PUBLISHED_ASSESSMENT_FEEDBACK", qualifierIdString);
		PersistenceService.getInstance().getAuthzQueriesFacade()
				.createAuthorization(s, "GRADE_PUBLISHED_ASSESSMENT", qualifierIdString);
		PersistenceService.getInstance().getAuthzQueriesFacade()
				.createAuthorization(s, "VIEW_PUBLISHED_ASSESSMENT", qualifierIdString);
	}
	
	/**
	 * Creates Authorizations for Selected Groups
	 */
	public void createAuthorizationForSelectedGroups(PublishedAssessmentData publishedAssessment) {
	    AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
		String qualifierIdString = publishedAssessment.getPublishedAssessmentId().toString();
		authz.createAuthorization(AgentFacade.getCurrentSiteId(), "OWN_PUBLISHED_ASSESSMENT", qualifierIdString);
		authz.createAuthorization(AgentFacade.getCurrentSiteId(), "VIEW_PUBLISHED_ASSESSMENT", qualifierIdString);

	    List<AuthorizationData> authorizationsToCopy = authz.getAuthorizationByFunctionAndQualifier("TAKE_ASSESSMENT", publishedAssessment.getAssessmentId().toString());
	    if (authorizationsToCopy != null && !authorizationsToCopy.isEmpty()) {
            for (AuthorizationData adToCopy : authorizationsToCopy) {
                authz.createAuthorization(adToCopy.getAgentIdString(), "TAKE_PUBLISHED_ASSESSMENT", publishedAssessment.getPublishedAssessmentId().toString());
            }
	    }
	}
	

	public AssessmentData loadAssessment(Long assessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.get(AssessmentData.class, assessmentId);
		} catch (Exception e) {
			log.warn("Error loading assessment with ID {}: {}", assessmentId, e.toString());
			return null;
		}
	}

	public PublishedAssessmentData loadPublishedAssessment(Long assessmentId) {
		PublishedAssessmentData ret = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			ret = session.get(PublishedAssessmentData.class, assessmentId);
		} catch (DataAccessException e) {
			log.warn("could not access published assessment [{}], {}", assessmentId, e.toString());
		} catch (Exception e) {
			log.warn("Error loading published assessment with ID {}: {}", assessmentId, e.toString());
		}
		return ret;
	}

	public List<PublishedAssessmentFacade> getAllTakeableAssessments(String orderBy, boolean ascending, final Integer status) {

		String query = "from PublishedAssessmentData as p where p.status = :status order by p." + orderBy;
		query += (ascending ? " asc" : " desc");
		log.debug("Order by " + orderBy);

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);
			Root<PublishedAssessmentData> root = cq.from(PublishedAssessmentData.class);

			cq.select(root)
				.where(cb.equal(root.get("status"), status));

			Path<Object> orderPath = root.get(orderBy);
			cq.orderBy(ascending ? cb.asc(orderPath) : cb.desc(orderPath));

			List<PublishedAssessmentData> list = session.createQuery(cq).list();

			List<PublishedAssessmentFacade> assessmentList = new ArrayList<>();
			for (PublishedAssessmentData a : list) {
				log.debug("Title: {}", a.getTitle());
				assessmentList.add(new PublishedAssessmentFacade(a));
			}
			return assessmentList;
		} catch (IllegalArgumentException e) {
			log.warn("Invalid orderBy property '{}': {}", orderBy, e.toString());
			return new ArrayList<>();
		} catch (Exception e) {
			log.warn("Error getting all takeable assessments: {}", e.toString());
			return new ArrayList<>();
		}
	}

	public Integer getNumberOfSubmissions(final String publishedAssessmentId, final String agentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

			cq.select(cb.count(root));

			cq.where(
				cb.equal(root.get("publishedAssessmentId"), Long.parseLong(publishedAssessmentId)),
				cb.equal(root.get("agentId"), agentId),
				cb.isTrue(root.get("forGrade")),
				cb.greaterThan(root.get("status"), AssessmentGradingData.REMOVED)
			);

			List<Long> list = session.createQuery(cq).getResultList();
			if (!list.isEmpty()) {
				return list.get(0).intValue();
			}
			return 0;
		} catch (Exception e) {
			log.warn("Error getting number of submissions for assessment {} and agent {}: {}", publishedAssessmentId, agentId, e.toString());
			return 0;
		}
	}

	public List<AssessmentGradingData> getNumberOfSubmissionsOfAllAssessmentsByAgent(final String agentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AssessmentGradingData> cq = cb.createQuery(AssessmentGradingData.class);
			Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

			cq.select(cb.construct(AssessmentGradingData.class,
				root.get("publishedAssessmentId"),
				cb.count(root)
			));

			cq.where(
				cb.equal(root.get("agentId"), agentId),
				cb.isTrue(root.get("forGrade")),
				cb.greaterThan(root.get("status"), AssessmentGradingData.REMOVED)
			);

			cq.groupBy(root.get("publishedAssessmentId"));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting number of submissions for agent {}: {}", agentId, e.toString());
			return new ArrayList<>();
		}
	}

	public List<AssessmentGradingData> getNumberOfSubmissionsOfAllAssessmentsByAgent(final String agentId, final String siteId) {

		final List groupIds = getSiteGroupIdsForSubmittingAgent(agentId, siteId);

		try {
			Session session = sessionFactory.getCurrentSession();

			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AssessmentGradingData> cq = cb.createQuery(AssessmentGradingData.class);

			Root<AssessmentGradingData> aRoot = cq.from(AssessmentGradingData.class);
			Root<AuthorizationData> azRoot = cq.from(AuthorizationData.class);

			List<Predicate> commonPredicates = new ArrayList<>();
			commonPredicates.add(cb.equal(aRoot.get("agentId"), agentId));
			commonPredicates.add(cb.isTrue(aRoot.get("forGrade")));
			commonPredicates.add(cb.greaterThan(aRoot.get("status"), AssessmentGradingData.REMOVED));
			commonPredicates.add(cb.equal(azRoot.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));
			commonPredicates.add(cb.equal(aRoot.get("publishedAssessmentId"), azRoot.get("qualifierId")));

			Predicate sitePredicate = cb.equal(azRoot.get("agentIdString"), siteId);
			Predicate groupPredicate = null;
			if (groupIds != null && !groupIds.isEmpty()) {
				groupPredicate = azRoot.get("agentIdString").in(groupIds);
			}
			Predicate agentPredicate = (groupPredicate == null) ? sitePredicate : cb.or(sitePredicate, groupPredicate);
			commonPredicates.add(agentPredicate);

			Expression<Long> countExpression = (groupIds != null && !groupIds.isEmpty()) ? cb.countDistinct(aRoot) : cb.count(aRoot);

			cq.select(cb.construct(AssessmentGradingData.class,
				aRoot.get("publishedAssessmentId"),
				countExpression
			));

			cq.where(commonPredicates.toArray(new Predicate[0]));
			cq.groupBy(aRoot.get("publishedAssessmentId"));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting number of submissions for agent {} and site {}: {}", agentId, siteId, e.toString());
			return new ArrayList<>();
	    	}
	}

	public List<PublishedAssessmentFacade> getAllPublishedAssessments(String sortString) {
		String orderBy = getOrderBy(sortString);
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);
			Root<PublishedAssessmentData> root = cq.from(PublishedAssessmentData.class);

			cq.select(root);

			if (orderBy != null && !orderBy.isEmpty()) {
				cq.orderBy(cb.asc(root.get(orderBy)));
			}

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			List<PublishedAssessmentFacade> assessmentList = new ArrayList<>();
			for (PublishedAssessmentData a : list) {
				a.setSectionSet(getSectionSetForAssessment(a));
				PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
				assessmentList.add(f);
			}
			return assessmentList;
		} catch (Exception e) {
			log.warn("Error getting all published assessments: {}", e.toString());
			return new ArrayList<>();
		}
	}

	public List<PublishedAssessmentFacade> getAllPublishedAssessments(String sortString, final Integer status) {
		final String orderBy = getOrderBy(sortString);

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);
			Root<PublishedAssessmentData> root = cq.from(PublishedAssessmentData.class);

			cq.select(root)
				.where(cb.equal(root.get("status"), status));

			if (orderBy != null && !orderBy.isEmpty()) {
				cq.orderBy(cb.asc(root.get(orderBy)));
			}

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			List<PublishedAssessmentFacade> assessmentList = new ArrayList<>();
			for (PublishedAssessmentData a : list) {
				a.setSectionSet(getSectionSetForAssessment(a));
				PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
				assessmentList.add(f);
			}
			return assessmentList;
		} catch (Exception e) {
			log.warn("Error getting all published assessments with status {}: {}", status, e.toString());
			return new ArrayList<>();
		}
	}

	public List<PublishedAssessmentFacade> getAllPublishedAssessments(int pageSize, int pageNumber, String sortString, Integer status) {
		String orderBy = getOrderBy(sortString);
		String queryString = "from PublishedAssessmentData p order by p." + orderBy;
		if (!status.equals(PublishedAssessmentFacade.ANY_STATUS)) {
			queryString = "from PublishedAssessmentData p where p.status = :status order by p." + orderBy;
		}
		PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().getPagingUtilQueries();
		List<PublishedAssessmentData> pageList = pagingUtilQueries.getAll(pageSize, pageNumber, queryString, status);
		log.debug("**** pageList=" + pageList);
		List<PublishedAssessmentFacade> assessmentList = new ArrayList();
		for (PublishedAssessmentData a : pageList) {
			a.setSectionSet(getSectionSetForAssessment(a));
			log.debug("****  published assessment=" + a.getTitle());
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
			log.debug("**** published assessment title=" + f.getTitle());
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public void removeAssessment(Long assessmentId, String action) {
		try {
			Session session = sessionFactory.getCurrentSession();
			PublishedAssessmentData assessment = session.get(PublishedAssessmentData.class, assessmentId);

			if (assessment == null) {
				log.warn("Assessment with ID {} not found", assessmentId);
				return;
			}

			// for preview, delete assessment
			// for others, simply set pub assessment to inactive
			if (action == null || action.equals("preview")) {
				delete(assessment);
				// remove authorization
				PersistenceService.getInstance().getAuthzQueriesFacade()
					.removeAuthorizationByQualifier(
						assessment.getPublishedAssessmentId().toString(),
						true);
			} else {
				assessment.setLastModifiedBy(AgentFacade.getAgentString());
				assessment.setLastModifiedDate(new Date());
				assessment.setStatus(PublishedAssessmentIfc.DEAD_STATUS);
				try {
					saveOrUpdate(assessment);
					RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
					rubricsService.softDeleteRubricAssociationsByItemIdPrefix(RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX + assessmentId + ".", RubricsConstants.RBCS_TOOL_SAMIGO);
				} catch (Exception e) {
					log.warn("Error updating assessment or rubrics: {}", e.toString());
				}
			}
		} catch (Exception e) {
			log.warn("Error removing assessment with ID {}: {}", assessmentId, e.toString());
		}
	}

	private String getOrderBy(String sortString) {
		String startDate = (PublishedAssessmentFacadeQueries.STARTDATE)
				.substring((PublishedAssessmentFacadeQueries.STARTDATE)
						.lastIndexOf(".") + 1);
		String dueDate = (PublishedAssessmentFacadeQueries.DUEDATE)
				.substring((PublishedAssessmentFacadeQueries.DUEDATE)
						.lastIndexOf(".") + 1);
		String releaseTo = (PublishedAssessmentFacadeQueries.RELEASETO)
				.substring((PublishedAssessmentFacadeQueries.RELEASETO)
						.lastIndexOf(".") + 1);

		if ((sortString).equals(startDate)) {
			return PublishedAssessmentFacadeQueries.STARTDATE;
		} else if ((sortString).equals(dueDate)) {
			return PublishedAssessmentFacadeQueries.DUEDATE;
		} else if ((sortString).equals(releaseTo)) {
			return PublishedAssessmentFacadeQueries.RELEASETO;
		} else {
			return PublishedAssessmentFacadeQueries.TITLE;
		}
	}

	public void deleteAllSecuredIP(PublishedAssessmentIfc assessment) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Long assessmentId = assessment.getPublishedAssessmentId();
				Session session = sessionFactory.getCurrentSession();

				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<PublishedSecuredIPAddress> cq = cb.createQuery(PublishedSecuredIPAddress.class);
				Root<PublishedSecuredIPAddress> root = cq.from(PublishedSecuredIPAddress.class);

				cq.select(root)
					.where(cb.equal(root.get("assessment").get("publishedAssessmentId"), assessmentId));

				List<PublishedSecuredIPAddress> ip = session.createQuery(cq).getResultList();

				if (!ip.isEmpty()) {
					PublishedSecuredIPAddress s = ip.get(0);
					PublishedAssessmentData a = (PublishedAssessmentData) s.getAssessment();
					a.setSecuredIPAddressSet(new HashSet<>());

					for (PublishedSecuredIPAddress address : ip) {
						session.remove(address);
					}
					retryCount = 0;
				} else {
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem deleting ip address: " + e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}
	
	public void saveOrUpdate(PublishedAssessmentIfc assessment) throws Exception {
		PublishedAssessmentData data;
		if (assessment instanceof PublishedAssessmentFacade) {
			data = (PublishedAssessmentData) ((PublishedAssessmentFacade) assessment).getData();
		} else {
			data = (PublishedAssessmentData) assessment;
		}

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				session.merge(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update assessment: {}", e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
				if (retryCount == 0) {
					throw e;
				}
			}
		}
	}

	public List<PublishedAssessmentFacade> getBasicInfoOfAllActivePublishedAssessments(String sortString, final String siteAgentId, boolean ascending) {
		Date currentDate = new Date();
		String orderBy = getOrderBy(sortString);
		
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Join<PublishedAssessmentData, PublishedAccessControl> cJoin = pRoot.join("accessControl");
			Join<PublishedAssessmentData, AuthorizationData> zJoin = pRoot.join("authorizations");

			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				cJoin.get("releaseTo"),
				cJoin.get("startDate"),
				cJoin.get("dueDate"),
				cJoin.get("retractDate"),
				pRoot.get("lastModifiedDate"),
				pRoot.get("lastModifiedBy")
					));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(pRoot.get("status"), 1));
			predicates.add(cb.equal(zJoin.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(zJoin.get("agentIdString"), siteAgentId));
			cq.where(predicates.toArray(new Predicate[0]));

			if (orderBy != null && !orderBy.isEmpty()) {
				Path<Object> orderPath = pRoot.get(orderBy);
				if (ascending) {
					cq.orderBy(cb.asc(orderPath));
				} else {
					cq.orderBy(cb.desc(orderPath));
				}
			}

			List<PublishedAssessmentData> l = session.createQuery(cq).getResultList();

			// we will filter the one that is past duedate & late submission date
			List<PublishedAssessmentData> list = new ArrayList<>();
			for (PublishedAssessmentData p : l) {
				if ((p.getDueDate() == null || (p.getDueDate()).after(currentDate))
						&& (p.getRetractDate() == null || (p.getRetractDate())
							.after(currentDate))) {
					list.add(p);
				}
			}

			List<PublishedAssessmentFacade> pubList = new ArrayList<>();
			Map<String, String> groupsForSite = null;
			Map<String, String> releaseToGroups;
			String lastModifiedBy = "";
			AgentFacade agent = null;

			for (PublishedAssessmentData p : list) {
				releaseToGroups = null;
				if (p.getReleaseTo() != null && p.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
					if (groupsForSite == null) {
						groupsForSite = getGroupsForSite(siteAgentId);
					}
					Long assessmentId = p.getPublishedAssessmentId();
					releaseToGroups = getReleaseToGroups(groupsForSite, assessmentId);
				}

				agent = new AgentFacade(p.getLastModifiedBy());
				if (agent != null) {
					lastModifiedBy = agent.getDisplayName();
				}

				PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
						p.getReleaseTo(), p.getStartDate(), p.getDueDate(), releaseToGroups, p.getLastModifiedDate(), lastModifiedBy);
				pubList.add(f);
			}
			return pubList;
		} catch (Exception e) {
			log.warn("Error getting basic info of active published assessments for site {}: {}", siteAgentId, e.toString());
			return new ArrayList<>();
		}
	}

	/**
	 * According to Marc inactive means either the dueDate or the retractDate
	 * has passed for 1.5 release (IM on 12/17/04)
	 * 
	 * @param sortString
	 * @return
	 */
	public List getBasicInfoOfAllInActivePublishedAssessments(
			String sortString, final String siteAgentId, boolean ascending) {
		
		String orderBy = getOrderBy(sortString);
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<PublishedAccessControl> cRoot = cq.from(PublishedAccessControl.class);
			Root<AuthorizationData> zRoot = cq.from(AuthorizationData.class);

			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				cRoot.get("releaseTo"),
				cRoot.get("startDate"),
				cRoot.get("dueDate"),
				cRoot.get("retractDate"),
				pRoot.get("status"),
				pRoot.get("lastModifiedDate"),
				pRoot.get("lastModifiedBy")
			));

			Predicate joinC = cb.equal(cRoot.get("assessment").get("publishedAssessmentId"), pRoot.get("publishedAssessmentId"));
			Predicate joinZ = cb.equal(zRoot.get("qualifierId"), pRoot.get("publishedAssessmentId"));

			Predicate statusActive = cb.equal(pRoot.get("status"), 1);
			Predicate dueDatePassed = cb.lessThanOrEqualTo(cRoot.get("dueDate"), new Date());
			Predicate retractDatePassed = cb.lessThanOrEqualTo(cRoot.get("retractDate"), new Date());
			Predicate dueOrRetract = cb.or(dueDatePassed, retractDatePassed);
			Predicate activeWithDate = cb.and(statusActive, dueOrRetract);

			Predicate statusEdit = cb.equal(pRoot.get("status"), 3);
			Predicate orPredicate = cb.or(activeWithDate, statusEdit);

			Predicate functionPredicate = cb.equal(zRoot.get("functionId"), "OWN_PUBLISHED_ASSESSMENT");
			Predicate sitePredicate = cb.equal(zRoot.get("agentIdString"), siteAgentId);

			cq.where(cb.and(joinC, joinZ, orPredicate, functionPredicate, sitePredicate));

			if (orderBy != null && !orderBy.isEmpty()) {
				Path<Object> orderPath = pRoot.get(orderBy);
				if (ascending) {
					cq.orderBy(cb.asc(orderPath));
				} else {
					cq.orderBy(cb.desc(orderPath));
				}
			}

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			List<PublishedAssessmentFacade> pubList = new ArrayList<>();
			Map<String, String> groupsForSite = null;
			Map<String, String> releaseToGroups;
			String lastModifiedBy = "";
			AgentFacade agent;
			for (int i = 0; i < list.size(); i++) {
				PublishedAssessmentData p = list.get(i);
				releaseToGroups = null;
				if (p.getReleaseTo() != null && p.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
					if (groupsForSite == null) {
						groupsForSite = getGroupsForSite(siteAgentId);
					}
					Long assessmentId = p.getPublishedAssessmentId();
					releaseToGroups = getReleaseToGroups(groupsForSite, assessmentId);
				}

				agent = new AgentFacade(p.getLastModifiedBy());
				if (agent != null) {
					lastModifiedBy = agent.getDisplayName();
				}
				PublishedAssessmentFacade f = new PublishedAssessmentFacade(p
						.getPublishedAssessmentId(), p.getTitle(),
						p.getReleaseTo(), p.getStartDate(), p.getDueDate(), p.getStatus(), releaseToGroups, p.getLastModifiedDate(), lastModifiedBy);
				pubList.add(f);
			}
			return pubList;
		} catch (Exception e) {
			log.warn("Error getting inactive published assessments for site {}: {}", siteAgentId, e.toString());
			return new ArrayList<>();
		}
	}

	public Set<PublishedSectionData> getSectionSetForAssessment(PublishedAssessmentIfc assessment) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedSectionData> cq = cb.createQuery(PublishedSectionData.class);
			Root<PublishedSectionData> root = cq.from(PublishedSectionData.class);

			cq.select(root)
				.where(cb.equal(root.get("assessment").get("publishedAssessmentId"), assessment.getPublishedAssessmentId()));

			List<PublishedSectionData> sectionList = session.createQuery(cq).getResultList();

			for (PublishedSectionData section : sectionList) {
				Hibernate.initialize(section.getItemSet());
			}

			return new HashSet<>(sectionList);
		} catch (Exception e) {
			log.warn("Error getting section set for assessment {}: {}", assessment.getPublishedAssessmentId(), e.toString());
			return new HashSet<>();
		}
	}

	// IMPORTANT:
	// 1. we do not want any Section info, so set loadSection to false
	// 2. We have also declared SectionData as lazy loading. If loadSection is
	// set
	// to true, we will see null pointer
	public PublishedAssessmentFacade getSettingsOfPublishedAssessment(Long assessmentId) {
		PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
		Boolean loadSection = Boolean.FALSE;
		PublishedAssessmentFacade f = new PublishedAssessmentFacade(a, loadSection);
		return f;
	}

	public PublishedItemData loadPublishedItem(Long itemId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.load(PublishedItemData.class, itemId);
		} catch (Exception e) {
			log.warn("Error loading published item with ID {}: {}", itemId, e.toString());
			return null;
		}
	}

	public PublishedItemText loadPublishedItemText(Long itemTextId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.load(PublishedItemText.class, itemTextId);
		} catch (Exception e) {
			log.warn("Error loading published item text with ID {}: {}", itemTextId, e.toString());
			return null;
		}
	}

	
	// added by daisy - please check the logic - I based this on the
	// getBasicInfoOfAllActiveAssessment
	// to include release to selected groups
	/**
	 * 
	 * @param orderBy
	 * @param ascending
	 * @param siteId
	 * @return
	 */
	public List<PublishedAssessmentFacade> getBasicInfoOfAllPublishedAssessments(String orderBy, boolean ascending, final String siteId) {
		String orderField = orderBy;
		boolean ascendingOrder = ascending;
		final List<String> groupIds = getSiteGroupIdsForCurrentUser(siteId);

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);
	
			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Join<PublishedAssessmentData, PublishedAccessControl> cJoin = pRoot.join("accessControl");
			Join<PublishedAssessmentData, PublishedFeedback> fJoin = pRoot.join("feedback");
			Join<PublishedAssessmentData, PublishedEvaluationModel> emJoin = pRoot.join("evaluationModel");
			Join<PublishedAssessmentData, AuthorizationData> azJoin = pRoot.join("authorizations");
	
			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				cJoin.get("releaseTo"),
				cJoin.get("startDate"),
				cJoin.get("dueDate"),
				cJoin.get("retractDate"),
				cJoin.get("feedbackDate"),
				fJoin.get("feedbackDelivery"),
				fJoin.get("feedbackComponentOption"),
				fJoin.get("feedbackAuthoring"),
				cJoin.get("lateHandling"),
				cJoin.get("unlimitedSubmissions"),
				cJoin.get("submissionsAllowed"),
				emJoin.get("scoringType"),
				pRoot.get("status"),
				pRoot.get("lastModifiedDate"),
				cJoin.get("timeLimit"),
				cJoin.get("feedbackEndDate"),
				cJoin.get("feedbackScoreThreshold")
			));
	
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.or(cb.equal(pRoot.get("status"), 1), cb.equal(pRoot.get("status"), 3)));
			predicates.add(cb.equal(azJoin.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(azJoin.get("qualifierId"), pRoot.get("publishedAssessmentId")));
	
			Predicate sitePredicate = cb.equal(azJoin.get("agentIdString"), siteId);
			Predicate groupPredicate = null;
			if (groupIds != null && !groupIds.isEmpty()) {
				groupPredicate = azJoin.get("agentIdString").in(groupIds);
			}
			if (groupPredicate != null) {
				predicates.add(cb.or(sitePredicate, groupPredicate));
			} else {
				predicates.add(sitePredicate);
			}
	
			cq.where(predicates.toArray(new Predicate[0]));
	
			if (orderField != null && !orderField.isEmpty()) {
				Path<Object> orderPath;
				if (orderField.equals("dueDate")) {
					orderPath = cJoin.get(orderField);
				} else {
					orderPath = pRoot.get(orderField);
				}
				if (ascendingOrder) {
					cq.orderBy(cb.asc(orderPath));
				} else {
					cq.orderBy(cb.desc(orderPath));
				}
			}
	
			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();
			List<PublishedAssessmentFacade> pubList = new ArrayList<>();
				for (PublishedAssessmentData p : list) {
					PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
							p.getReleaseTo(), p.getStartDate(), p.getDueDate(), p
								.getRetractDate(), p.getFeedbackDate(), p
								.getFeedbackDelivery(), p.getFeedbackComponentOption(), p.getFeedbackAuthoring(), p
								.getLateHandling(), p.getUnlimitedSubmissions(), p
								.getSubmissionsAllowed(), p.getScoringType(), p.getStatus(), p.getLastModifiedDate(), p.getTimeLimit(), p.getFeedbackEndDate(), p.getFeedbackScoreThreshold());
					pubList.add(f);
				}
				return pubList;
		} catch (Exception e) {
			log.warn("Error getting basic info of all published assessments for site {}: {}", siteId, e.toString());
			return new ArrayList<>();
		}
	}

	// This is for instructors view (author index page)
	public List<PublishedAssessmentFacade> getBasicInfoOfAllPublishedAssessments2(String sortString, boolean ascending, final String siteAgentId) {
		String orderBy = getOrderBy(sortString);
		
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<PublishedAccessControl> cRoot = cq.from(PublishedAccessControl.class);
			Root<AuthorizationData> zRoot = cq.from(AuthorizationData.class);

			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				cRoot.get("releaseTo"),
				cRoot.get("startDate"),
				cRoot.get("dueDate"),
				cRoot.get("retractDate"),
				pRoot.get("status"),
				pRoot.get("lastModifiedDate"),
				pRoot.get("lastModifiedBy"),
				cRoot.get("lateHandling"),
				cRoot.get("unlimitedSubmissions"),
				cRoot.get("submissionsAllowed")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(cRoot.get("assessment").get("publishedAssessmentId"), pRoot.get("publishedAssessmentId")));
			predicates.add(cb.equal(zRoot.get("qualifierId"), pRoot.get("publishedAssessmentId")));
			predicates.add(cb.equal(zRoot.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(zRoot.get("agentIdString"), siteAgentId));
			predicates.add(cb.or(
				cb.equal(pRoot.get("status"), 1),
				cb.equal(pRoot.get("status"), 3)
			));

			cq.where(predicates.toArray(new Predicate[0]));

			if (orderBy != null && !orderBy.isEmpty()) {
				Path<Object> orderPath = pRoot.get(orderBy);
				if (ascending) {
					cq.orderBy(cb.asc(orderPath));
				} else {
					cq.orderBy(cb.desc(orderPath));
				}
			}

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			List<PublishedAssessmentFacade> pubList = new ArrayList<>();
			Map<String, String> groupsForSite = null;
			Map<String, String> releaseToGroups = new HashMap<>();
			String lastModifiedBy = "";
			AgentFacade agent = null;
			Long assessmentId;
			String userId = AgentFacade.getAnonymousId();
			GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
			Site site = null;
			Collection<Group> siteGroups = new ArrayList<>();
			Set<String> keysGroupIdsMap = new HashSet<>();
			try {
				site = siteService.getSite(siteAgentId);
				if (service.isUserAbleToGradeAll(site.getId(), userId)) {
					siteGroups = site.getGroups();
				} else {
					siteGroups = site.getGroupsWithMember(userId);
				}
				Map<String, String> groupIdsMap = siteGroups.stream()
					.collect(Collectors.toMap(Group::getId, Group::getId));
				keysGroupIdsMap = groupIdsMap.keySet();
			} catch (IdUnusedException ex) {
				// no site found, just log a warning
				log.warn("Unable to find a site with id ({}) in order to get the enrollments, will return 0 enrollments", siteAgentId);
			}
			for (PublishedAssessmentData p : list) {
				releaseToGroups = null;
				if (p.getReleaseTo() != null && p.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
					if (groupsForSite == null) {
						groupsForSite = getGroupsForSite(siteAgentId);
					}
					assessmentId = p.getPublishedAssessmentId();
					releaseToGroups = getReleaseToGroups(groupsForSite, assessmentId);
				}

				agent = new AgentFacade(p.getLastModifiedBy());
				if (agent != null) {
					lastModifiedBy = agent.getDisplayName();
				}

				if (releaseToGroups != null) {
					Set<String> keysReleaseToGroups = releaseToGroups.keySet();

					Set<String> commonKeys = new HashSet<>(keysReleaseToGroups);
					commonKeys.retainAll(keysGroupIdsMap);

					if (!commonKeys.isEmpty() || (siteGroups.isEmpty() && service.isUserAbleToGradeAll(site.getId(), userId))) {
						PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
								p.getReleaseTo(), p.getStartDate(), p.getDueDate(), p.getRetractDate(), p.getStatus(), releaseToGroups, 
								p.getLastModifiedDate(), lastModifiedBy, p.getLateHandling(), p.getUnlimitedSubmissions(), p.getSubmissionsAllowed());
						pubList.add(f);
					}
				} else {
					PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
							p.getReleaseTo(), p.getStartDate(), p.getDueDate(), p.getRetractDate(), p.getStatus(), releaseToGroups, 
							p.getLastModifiedDate(), lastModifiedBy, p.getLateHandling(), p.getUnlimitedSubmissions(), p.getSubmissionsAllowed());
					pubList.add(f);
				}
			}
			return pubList;
		} catch (Exception e) {
			log.warn("Error getting basic info for instructor view (author index page) for site {}: {}", siteAgentId, e.toString());
			return new ArrayList<>();
		}
	}

	
	/**
	 * return an array list of the last AssessmentGradingFacade per assessment
	 * that a user has submitted for grade.
	 * 
	 * @param agentId
	 * @param orderBy
	 * @param ascending
	 * @return
	 */
	public List<AssessmentGradingData> getBasicInfoOfLastSubmittedAssessments(final String agentId, String orderBy, boolean ascending) {
		// 1. get total no. of submission per assessment by the given agent
		// HashMap h = getTotalSubmissionPerAssessment(agentId);
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AssessmentGradingData> cq = cb.createQuery(AssessmentGradingData.class);
			Root<AssessmentGradingData> aRoot = cq.from(AssessmentGradingData.class);
			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);

			cq.select(cb.construct(AssessmentGradingData.class,
				aRoot.get("assessmentGradingId"),
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				aRoot.get("agentId"),
				aRoot.get("submittedDate"),
				aRoot.get("isLate"),
				aRoot.get("forGrade"),
				aRoot.get("totalAutoScore"),
				aRoot.get("totalOverrideScore"),
				aRoot.get("finalScore"),
				aRoot.get("comments"),
				aRoot.get("status"),
				aRoot.get("gradedBy"),
				aRoot.get("gradedDate"),
				aRoot.get("attemptDate"),
				aRoot.get("timeElapsed")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(aRoot.get("publishedAssessmentId"), pRoot.get("publishedAssessmentId")));
			predicates.add(cb.isTrue(aRoot.get("forGrade")));
			predicates.add(cb.equal(aRoot.get("agentId"), agentId));
			predicates.add(cb.greaterThan(aRoot.get("status"), AssessmentGradingData.REMOVED));

			cq.where(predicates.toArray(new Predicate[0]));

			cq.orderBy(
				cb.desc(pRoot.get("publishedAssessmentId")),
				cb.desc(aRoot.get("submittedDate"))
			);

			/*
			 * The sorting for each type will be done in the action listener. if
			 * (orderBy.equals(TITLE)) { query += ", p." + orderBy; } else if
			 * (!orderBy.equals(SUBMITTED)) { query += ", a." + orderBy; } if
			 * (!orderBy.equals(SUBMITTED)) { if (ascending == false) { query += "
			 * desc"; } else { query += " asc"; } }
			 */

			List<AssessmentGradingData> list = session.createQuery(cq).getResultList();

			List<AssessmentGradingData> assessmentList = new ArrayList<>();
			Long current = 0L;
			// Date currentDate = new Date();
			for (AssessmentGradingData a : list) {
				// criteria: only want the most recently submitted assessment from a
				// given user.
				if (!a.getPublishedAssessmentId().equals(current)) {
					current = a.getPublishedAssessmentId();
					AssessmentGradingData f = a;
					assessmentList.add(f);
				}
			}
			return assessmentList;
		} catch (Exception e) {
			log.warn("Error getting basic info of last submitted assessments for agent {}: {}", agentId, e.toString());
			return new ArrayList<>();
		}
	}

	/**
	 * total submitted for grade returns HashMap (Long publishedAssessmentId,
	 * Integer totalSubmittedForGrade);
	 */
	public Map<Long, Integer> getTotalSubmissionPerAssessment(String agentId) {
		List<AssessmentGradingData> l = getNumberOfSubmissionsOfAllAssessmentsByAgent(agentId);
		Map<Long, Integer> h = new HashMap<>();
		for (AssessmentGradingData d : l) {
			h.put(d.getPublishedAssessmentId(), d.getTotalSubmitted());
			log.debug("pId={} submitted={}", d.getPublishedAssessmentId(), d.getTotalSubmitted());
		}
		return h;
	}

	public Map<Long, Integer> getTotalSubmissionPerAssessment(String agentId, String siteId) {
		List<AssessmentGradingData> l = getNumberOfSubmissionsOfAllAssessmentsByAgent(agentId, siteId);
		Map<Long, Integer> h = new HashMap<>();
		for (AssessmentGradingData d : l) {
			h.put(d.getPublishedAssessmentId(), d.getTotalSubmitted());
			log.debug("pId={} submitted={}", d.getPublishedAssessmentId(), d.getTotalSubmitted());
		}
		return h;
	}

    /**
     * Get submission number for the assessment by giving the publishedAssessmentId
     * for assessment deletion safe check
     * @param publishedAssessmentId
     * @return number of submissions
     */
	public Integer getTotalSubmissionForEachAssessment(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

			cq.select(cb.count(root));

			cq.where(
				cb.isTrue(root.get("forGrade")),
				cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId),
				cb.greaterThan(root.get("status"), AssessmentGradingData.REMOVED)
			);

			List<Long> l = session.createQuery(cq).getResultList();

			if (!l.isEmpty()) {
				return l.get(0).intValue();
			}
			return 0;
		} catch (Exception e) {
			log.warn("Error getting total submissions for assessment {}: {}", publishedAssessmentId, e.toString());
			return 0;
		}
	}

	public Integer getTotalSubmission(final String agentId, final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

			cq.select(cb.count(root));

			cq.where(
				cb.isTrue(root.get("forGrade")),
				cb.equal(root.get("agentId"), agentId),
				cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId),
				cb.greaterThan(root.get("status"), AssessmentGradingData.REMOVED)
			);

			Long count = session.createQuery(cq).uniqueResult();
			return count != null ? count.intValue() : 0;
		} catch (Exception e) {
			log.warn("Error getting total submissions for agent {} and assessment {}: {}", agentId, publishedAssessmentId, e.toString());
			return 0;
		}
	}

	public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(String alias) {
		return getPublishedAssessmentIdByMetaLabel("ALIAS", alias);
	}

	public PublishedAssessmentFacade getPublishedAssessmentIdByMetaLabel(final String label, final String entry) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<PublishedMetaData> mRoot = cq.from(PublishedMetaData.class);

			cq.select(pRoot)
				.where(
					cb.equal(pRoot, mRoot.get("assessment")),
					cb.equal(mRoot.get("label"), label),
					cb.equal(mRoot.get("entry"), entry)
			);

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			switch (list.size()) {
				case 0:
					log.warn("No matching assessment where {} = {}", label, entry);
					break;
				case 1:
					PublishedAssessmentData data = list.get(0);
					data.setSectionSet(getSectionSetForAssessment(data));
					PublishedAssessmentFacade assessment = new PublishedAssessmentFacade(data);
					if (data.getAssessmentFeedback() != null) {
						assessment.setFeedbackComponentOption(data.getAssessmentFeedback().getFeedbackComponentOption());
					}
					return assessment;
				default:
					log.warn("More than 1 assessment found with the same {} = {}, this should be unique.", label, entry);
					break;
			}
			return null;
		} catch (Exception e) {
			log.warn("Error getting published assessment by meta label {} and entry {}: {}", label, entry, e.toString());
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Long, String> getAssessmentMetaDataEntriesByLabel(List<Long> publishedAssessmentIds, String label) {
		if (publishedAssessmentIds == null || publishedAssessmentIds.isEmpty() || org.apache.commons.lang3.StringUtils.isBlank(label)) {
			return Collections.emptyMap();
		}

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

			Root<PublishedMetaData> mRoot = cq.from(PublishedMetaData.class);

			cq.select(cb.array(
				mRoot.get("assessment").get("publishedAssessmentId"),
				mRoot.get("entry")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(mRoot.get("assessment").get("publishedAssessmentId").in(publishedAssessmentIds));
			predicates.add(cb.equal(mRoot.get("label"), label));

			cq.where(predicates.toArray(new Predicate[0]));

			cq.orderBy(
				cb.asc(mRoot.get("assessment").get("publishedAssessmentId")),
				cb.asc(mRoot.get("id"))
			);

			List<Object[]> list = session.createQuery(cq).getResultList();

			return list.stream()
				.collect(Collectors.toMap(
					row -> (Long) row[0],
					row -> (String) row[1],
					// Keep the latest value to preserve prior behavior and avoid page failures when historical duplicate rows exist.
					(existing, replacement) -> replacement,
					LinkedHashMap::new
				));
		} catch (Exception e) {
			throw new DataAccessResourceFailureException("Failed to get assessment meta data entries", e);
		}
	}

	public void saveOrUpdateMetaData(PublishedMetaData meta) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				session.merge(meta);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update meta data: " + e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public Map<Long, PublishedFeedback> getFeedbackHash(String siteId) {
		final List listAgentId = new ArrayList();
		listAgentId.add(siteId);

		try {
			Site site = siteService.getSite(siteId);
				Collection groups = site.getGroups();
				if (groups != null && groups.size() > 0) {
					Iterator groupIter = groups.iterator();
					while (groupIter.hasNext()) {
						Group group = (Group) groupIter.next();
						listAgentId.add(group.getId());
					}
				}
			}
		catch (IdUnusedException ex) {
			// No site available
		}

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedFeedback> cq = cb.createQuery(PublishedFeedback.class);

			Root<PublishedFeedback> pRoot = cq.from(PublishedFeedback.class);
			Root<AuthorizationData> azRoot = cq.from(AuthorizationData.class);

			cq.select(cb.construct(PublishedFeedback.class,
				pRoot.get("assessment").get("publishedAssessmentId"),
				pRoot.get("feedbackDelivery"),
				pRoot.get("feedbackComponentOption"),
				pRoot.get("feedbackAuthoring"),
				pRoot.get("editComponents"),
				pRoot.get("showQuestionText"),
				pRoot.get("showStudentResponse"),
				pRoot.get("showCorrectResponse"),
				pRoot.get("showStudentScore"),
				pRoot.get("showStudentQuestionScore"),
				pRoot.get("showQuestionLevelFeedback"),
				pRoot.get("showSelectionLevelFeedback"),
				pRoot.get("showGraderComments"),
				pRoot.get("showStatistics")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(azRoot.get("qualifierId"), pRoot.get("assessment").get("publishedAssessmentId")));
			predicates.add(azRoot.get("agentIdString").in(listAgentId));
			predicates.add(cb.equal(azRoot.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedFeedback> l = session.createQuery(cq).getResultList();

			Map<Long, PublishedFeedback> h = new HashMap<>();
			for (PublishedFeedback f : l) {
				h.put(f.getAssessmentId(), f);
			}
			return h;
		} catch (Exception e) {
			log.warn("Error getting feedback hash for site {}: {}", siteId, e.toString());
			return new HashMap<>();
		}
	}

	/**
	 * this return a HashMap containing (Long publishedAssessmentId,
	 * PublishedAssessmentFacade publishedAssessment) Note that the
	 * publishedAssessment is a partial object used for display only. do not use
	 * it for persisting. It only contains title, releaseTo, startDate, dueDate &
	 * retractDate
	 */
	public Map<Long, PublishedAssessmentFacade> getAllAssessmentsReleasedToAuthenticatedUsers() {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<PublishedAccessControl> cRoot = cq.from(PublishedAccessControl.class);

			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				cRoot.get("releaseTo"),
				cRoot.get("startDate"),
				cRoot.get("dueDate"),
				cRoot.get("retractDate")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(cRoot.get("assessment"), pRoot));
			predicates.add(cb.like(cRoot.get("releaseTo"), "%Authenticated Users%"));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedAssessmentData> l = session.createQuery(cq).getResultList();

			Map<Long, PublishedAssessmentFacade> h = new HashMap<>();
			for (PublishedAssessmentData p : l) {
				h.put(p.getPublishedAssessmentId(), new PublishedAssessmentFacade(p));
			}
			return h;
		} catch (Exception e) {
			log.warn("Error getting assessments released to authenticated users: {}", e.toString());
			return new HashMap<>();
		}
	}

	public String getPublishedAssessmentOwner(String publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AuthorizationData> cq = cb.createQuery(AuthorizationData.class);
			Root<AuthorizationData> root = cq.from(AuthorizationData.class);

			cq.select(root)
				.where(
					cb.equal(root.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"),
					cb.equal(root.get("qualifierId"), publishedAssessmentId)
				);

			List<AuthorizationData> l = session.createQuery(cq).getResultList();

			if (!l.isEmpty()) {
				AuthorizationData a = l.get(0);
				return a.getAgentIdString();
			}
			return null;
		} catch (Exception e) {
			log.warn("Error getting published assessment owner for assessment {}: {}", publishedAssessmentId, e.toString());
			return null;
		}
	}

	public boolean publishedAssessmentTitleIsUnique(final Long assessmentBaseId, final String title) {
		final String currentSiteId = AgentFacade.getCurrentSiteId();
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> aRoot = cq.from(PublishedAssessmentData.class);
			Root<AuthorizationData> zRoot = cq.from(AuthorizationData.class);

			cq.select(cb.construct(PublishedAssessmentData.class,
				aRoot.get("publishedAssessmentId"),
				aRoot.get("title"),
				aRoot.get("lastModifiedDate")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(aRoot.get("title"), title));
			predicates.add(cb.notEqual(aRoot.get("publishedAssessmentId"), assessmentBaseId));
			predicates.add(cb.notEqual(aRoot.get("status"), 2));
			predicates.add(cb.equal(zRoot.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(aRoot.get("publishedAssessmentId"), zRoot.get("qualifierId")));
			predicates.add(cb.equal(zRoot.get("agentIdString"), currentSiteId));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			return list.isEmpty();
		} catch (Exception e) {
			log.warn("Error checking if published assessment title is unique for title {}: {}", title, e.toString());
			return false;
		}
	}

	public boolean hasRandomPart(final Long publishedAssessmentId) {
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();
		final String valueMultiple = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString();

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedSectionData> cq = cb.createQuery(PublishedSectionData.class);

			Root<PublishedSectionData> sRoot = cq.from(PublishedSectionData.class);
			Root<PublishedSectionMetaData> mRoot = cq.from(PublishedSectionMetaData.class);

			cq.select(sRoot);

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(sRoot, mRoot.get("section")));
			predicates.add(cb.equal(sRoot.get("assessment").get("publishedAssessmentId"), publishedAssessmentId));
			predicates.add(cb.equal(mRoot.get("label"), key));
			predicates.add(cb.or(
				cb.equal(mRoot.get("entry"), value),
				cb.equal(mRoot.get("entry"), valueMultiple)
			));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedSectionData> l = session.createQuery(cq).getResultList();

			return !l.isEmpty();
		} catch (Exception e) {
			log.warn("Error checking if assessment {} has random part: {}", publishedAssessmentId, e.toString());
			return false;
		}
	}

	public List<Long> getContainRandomPartAssessmentIds(final Collection assessmentIds) {
		if (assessmentIds == null || assessmentIds.size() < 1) {
			return new ArrayList<>();
		}
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();
		final String entryMultiple = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString();

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);

			Root<PublishedSectionData> sRoot = cq.from(PublishedSectionData.class);
			Root<PublishedSectionMetaData> mRoot = cq.from(PublishedSectionMetaData.class);

			cq.select(sRoot.get("assessment").get("publishedAssessmentId")).distinct(true);

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(sRoot.get("assessment").get("publishedAssessmentId").in(assessmentIds));
			predicates.add(cb.equal(sRoot, mRoot.get("section")));
			predicates.add(cb.equal(mRoot.get("label"), key));
			predicates.add(cb.or(
				cb.equal(mRoot.get("entry"), value),
				cb.equal(mRoot.get("entry"), entryMultiple)
			));

			cq.where(predicates.toArray(new Predicate[0]));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting assessment IDs with random parts: {}", e.toString());
			return new ArrayList<>();
		}
	}

	public PublishedItemData getFirstPublishedItem(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();

			CriteriaBuilder cb = session.getCriteriaBuilder();

			CriteriaQuery<PublishedItemData> cq1 = cb.createQuery(PublishedItemData.class);
			Root<PublishedAssessmentData> pRoot1 = cq1.from(PublishedAssessmentData.class);
			Root<PublishedSectionData> sRoot1 = cq1.from(PublishedSectionData.class);
			Root<PublishedItemData> iRoot = cq1.from(PublishedItemData.class);

			cq1.select(iRoot);
			cq1.where(
				cb.equal(pRoot1.get("publishedAssessmentId"), publishedAssessmentId),
				cb.equal(pRoot1.get("publishedAssessmentId"), sRoot1.get("assessment").get("publishedAssessmentId")),
				cb.equal(sRoot1, iRoot.get("section"))
			);
			List<PublishedItemData> l = session.createQuery(cq1).getResultList();

			CriteriaQuery<PublishedSectionData> cq2 = cb.createQuery(PublishedSectionData.class);
			Root<PublishedAssessmentData> pRoot2 = cq2.from(PublishedAssessmentData.class);
			Root<PublishedSectionData> sRoot2 = cq2.from(PublishedSectionData.class);

			cq2.select(sRoot2);
			cq2.where(
				cb.equal(pRoot2.get("publishedAssessmentId"), publishedAssessmentId),
				cb.equal(pRoot2.get("publishedAssessmentId"), sRoot2.get("assessment").get("publishedAssessmentId"))
			);
			List<PublishedSectionData> sec = session.createQuery(cq2).getResultList();

			PublishedItemData returnItem = null;
			if (sec.size() > 0 && l.size() > 0) {
				sec.sort(new SecComparator());
				for (PublishedSectionData thisSec : sec) {
					List<PublishedItemData> itemList = new ArrayList<>();
					for (PublishedItemData aL : l) {
						PublishedItemData compItem = aL;
						if (compItem.getSection().getSectionId().equals(thisSec.getSectionId())) {
							itemList.add(compItem);
						}
					}
					if (itemList.size() > 0) {
						itemList.sort(new ItemComparator());
						returnItem = itemList.get(0);
						break;
					}
				}
			}
			return returnItem;
		} catch (Exception e) {
			log.warn("Error getting first published item for assessment {}: {}", publishedAssessmentId, e.toString());
			return null;
		}
	}

	public List<Long> getPublishedItemIds(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);

			Root<PublishedItemData> iRoot = cq.from(PublishedItemData.class);
			Join<PublishedItemData, PublishedSectionData> sJoin = iRoot.join("section");
			Join<PublishedSectionData, PublishedAssessmentData> pJoin = sJoin.join("assessment");

			cq.select(iRoot.get("itemId"));
			cq.where(cb.equal(pJoin.get("publishedAssessmentId"), publishedAssessmentId));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting published item IDs for assessment {}: {}", publishedAssessmentId, e.toString());
			return new ArrayList<>();
		}
	}

	public Set<PublishedItemData> getPublishedItemSet(final Long publishedAssessmentId, final Long sectionId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedItemData> cq = cb.createQuery(PublishedItemData.class);

			Root<PublishedItemData> iRoot = cq.from(PublishedItemData.class);
			Join<PublishedItemData, PublishedSectionData> sJoin = iRoot.join("section");
			Join<PublishedSectionData, PublishedAssessmentData> pJoin = sJoin.join("assessment");

			cq.select(iRoot)
				.where(
					cb.equal(pJoin.get("publishedAssessmentId"), publishedAssessmentId),
					cb.equal(iRoot.get("section").get("id"), sectionId)
				);

			List<PublishedItemData> assessmentGradings = session.createQuery(cq).getResultList();

			Set<PublishedItemData> itemSet = new HashSet<>();
			for (PublishedItemData publishedItemData : assessmentGradings) {
				log.debug("itemId = {}", publishedItemData.getItemId());
				itemSet.add(publishedItemData);
			}
			return itemSet;
		} catch (Exception e) {
			log.warn("Error getting published item set for assessment {} and section {}: {}", publishedAssessmentId, sectionId, e.toString());
			return new HashSet<>();
		}
	}

	public Long getItemType(final Long publishedItemId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<PublishedItemData> root = cq.from(PublishedItemData.class);

			cq.select(root.get("typeId"))
				.where(cb.equal(root.get("itemId"), publishedItemId));

			List<Long> list = session.createQuery(cq).getResultList();

			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		} catch (Exception e) {
			log.warn("Error getting item type for item {}: {}", publishedItemId, e.toString());
			return null;
		}
	}

	class SecComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			return ((PublishedSectionData) arg0).getSequence().compareTo(
					((PublishedSectionData) arg1).getSequence());
		}
	}

	class ItemComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			return ((PublishedItemData) arg0).getSequence().compareTo(
					((PublishedItemData) arg1).getSequence());
		}
	}

	public void delete(PublishedAssessmentIfc assessment) {
		PublishedAssessmentData data;
		if (assessment instanceof PublishedAssessmentFacade)
			data = (PublishedAssessmentData) ((PublishedAssessmentFacade) assessment).getData();
		else
			data = (PublishedAssessmentData) assessment;

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
				if (extendedTimeFacade != null) {
					extendedTimeFacade.deleteEntriesForPub(data);
				}
				Session session = sessionFactory.getCurrentSession();
				session.remove(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem removing publishedAssessment: {}", e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public Set<PublishedSectionData> getSectionSetForAssessment(Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedSectionData> cq = cb.createQuery(PublishedSectionData.class);
			Root<PublishedSectionData> root = cq.from(PublishedSectionData.class);

			cq.select(root)
				.where(cb.equal(root.get("assessment").get("publishedAssessmentId"), publishedAssessmentId));

			List<PublishedSectionData> sectionList = session.createQuery(cq).getResultList();
			return new HashSet<>(sectionList);
		} catch (Exception e) {
			log.warn("Error getting section set for assessment {}: {}", publishedAssessmentId, e.toString());
			return new HashSet<>();
		}
	}

	private String replaceSpace(String tempString) {
		String newString = "";
		char[] oneChar = new char[1];
		for (int i = 0; i < tempString.length(); i++) {
			if (tempString.charAt(i) != ' ') {
				oneChar[0] = tempString.charAt(i);
				String concatString = new String(oneChar);
				newString = newString.concat(concatString);
			} else {
				newString = newString.concat("%20");
			}
		}
		return newString;
	}

	public boolean isRandomDrawPart(final Long publishedAssessmentId, final Long sectionId) {
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();
		final String valueMultiple = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString();

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedSectionData> cq = cb.createQuery(PublishedSectionData.class);

			Root<PublishedSectionData> sRoot = cq.from(PublishedSectionData.class);
			Root<PublishedSectionMetaData> mRoot = cq.from(PublishedSectionMetaData.class);

			cq.select(sRoot);

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(sRoot, mRoot.get("section")));
			predicates.add(cb.equal(sRoot.get("assessment").get("publishedAssessmentId"), publishedAssessmentId));
			predicates.add(cb.equal(sRoot.get("id"), sectionId));
			predicates.add(cb.equal(mRoot.get("label"), key));
			predicates.add(cb.or(
				cb.equal(mRoot.get("entry"), value),
				cb.equal(mRoot.get("entry"), valueMultiple)
			));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedSectionData> l = session.createQuery(cq).getResultList();

			return !l.isEmpty();
		} catch (Exception e) {
			log.warn("Error checking if assessment {} section {} is random draw part: {}", publishedAssessmentId, sectionId, e.toString());
			return false;
		}
	}

	public boolean isFixedRandomDrawPart(final Long publishedAssessmentId, final Long sectionId) {
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString();

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedSectionData> cq = cb.createQuery(PublishedSectionData.class);

			Root<PublishedSectionData> sRoot = cq.from(PublishedSectionData.class);
			Join<PublishedSectionData, PublishedSectionMetaData> mJoin = sRoot.join("section");

			cq.select(sRoot);

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(sRoot.get("assessment").get("publishedAssessmentId"), publishedAssessmentId));
			predicates.add(cb.equal(sRoot.get("id"), sectionId));
			predicates.add(cb.equal(mJoin.get("label"), key));
			predicates.add(cb.equal(mJoin.get("entry"), value));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedSectionData> l = session.createQuery(cq).getResultList();

			return !l.isEmpty();
		} catch (Exception e) {
			log.warn("Error checking if assessment {} section {} is fixed random draw part: {}", publishedAssessmentId, sectionId, e.toString());
			return false;
		}
	}

	/**
	 * return an array list of the AssessmentGradingData that a user has
	 * submitted for grade. one per published assessment, when allAssessments is false,
	 * and all submissions per published assessment when allAssesments is true. 
	 * If an assessment allows multiple submissions and its grading option is to send highest,
	 * then return only the submission with highest finalScore. If an assessment
	 * allows multiple submissions and its grading option is to send last, then
	 * return only the last submission.
	 * 
	 * @param agentId
	 * @param siteId
	 * @param allAssessments
	 * @return
	 */
	public List<AssessmentGradingData> getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(final String agentId, final String siteId, boolean allAssessments) {
		
		// take account of group release
		final List<String> groupIds = getSiteGroupIdsForSubmittingAgent(agentId, siteId);

		List<AssessmentGradingData> last_list;
		List<AssessmentGradingData> highest_list;

		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();

			// Get total no. of submission per assessment by the given agent
			if (groupIds.size() > 0) {
				CriteriaQuery<AssessmentGradingData> cqLast = cb.createQuery(AssessmentGradingData.class);
				Root<AssessmentGradingData> aRootLast = cqLast.from(AssessmentGradingData.class);
				Root<PublishedAssessmentData> pRootLast = cqLast.from(PublishedAssessmentData.class);
				Root<AuthorizationData> azRootLast = cqLast.from(AuthorizationData.class);

				cqLast.select(cb.construct(AssessmentGradingData.class,
						aRootLast.get("assessmentGradingId"), pRootLast.get("publishedAssessmentId"), pRootLast.get("title"), aRootLast.get("agentId"),
						aRootLast.get("submittedDate"), aRootLast.get("isLate"),
						aRootLast.get("forGrade"), aRootLast.get("totalAutoScore"), aRootLast.get("totalOverrideScore"), aRootLast.get("finalScore"),
						cb.literal(""), aRootLast.get("status"), aRootLast.get("gradedBy"), aRootLast.get("gradedDate"), aRootLast.get("attemptDate"),
						aRootLast.get("timeElapsed")
				)).distinct(true);

				List<Predicate> predicatesLast = new ArrayList<>();
				predicatesLast.add(cb.equal(aRootLast.get("publishedAssessmentId"), pRootLast.get("publishedAssessmentId")));
				predicatesLast.add(cb.isTrue(aRootLast.get("forGrade")));
				predicatesLast.add(cb.greaterThan(aRootLast.get("status"), AssessmentGradingData.REMOVED));
				predicatesLast.add(cb.equal(aRootLast.get("agentId"), agentId));
				predicatesLast.add(cb.or(
					cb.equal(azRootLast.get("agentIdString"), siteId),
					azRootLast.get("agentIdString").in(groupIds)
				));
				predicatesLast.add(cb.equal(azRootLast.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));
				predicatesLast.add(cb.equal(azRootLast.get("qualifierId"), pRootLast.get("publishedAssessmentId")));
				predicatesLast.add(cb.or(
					cb.equal(pRootLast.get("status"), 1),
					cb.equal(pRootLast.get("status"), 3)
				));

				cqLast.where(predicatesLast.toArray(new Predicate[0]));
				// sorted by submittedData DESC
				cqLast.orderBy(
					cb.desc(pRootLast.get("publishedAssessmentId")),
					cb.desc(aRootLast.get("submittedDate"))
				);

				last_list = session.createQuery(cqLast).getResultList();

				CriteriaQuery<AssessmentGradingData> cqHighest = cb.createQuery(AssessmentGradingData.class);
				Root<AssessmentGradingData> aRootHighest = cqHighest.from(AssessmentGradingData.class);
				Root<PublishedAssessmentData> pRootHighest = cqHighest.from(PublishedAssessmentData.class);
				Root<AuthorizationData> azRootHighest = cqHighest.from(AuthorizationData.class);

				cqHighest.select(cb.construct(AssessmentGradingData.class,
					aRootHighest.get("assessmentGradingId"), pRootHighest.get("publishedAssessmentId"), pRootHighest.get("title"), aRootHighest.get("agentId"),
					aRootHighest.get("submittedDate"), aRootHighest.get("isLate"),
					aRootHighest.get("forGrade"), aRootHighest.get("totalAutoScore"), aRootHighest.get("totalOverrideScore"), aRootHighest.get("finalScore"),
					cb.literal(""), aRootHighest.get("status"), aRootHighest.get("gradedBy"), aRootHighest.get("gradedDate"), aRootHighest.get("attemptDate"),
					aRootHighest.get("timeElapsed")
				)).distinct(true);

				List<Predicate> predicatesHighest = new ArrayList<>();
				predicatesHighest.add(cb.equal(aRootHighest.get("publishedAssessmentId"), pRootHighest.get("publishedAssessmentId")));
				predicatesHighest.add(cb.isTrue(aRootHighest.get("forGrade")));
				predicatesHighest.add(cb.greaterThan(aRootHighest.get("status"), AssessmentGradingData.REMOVED));
				predicatesHighest.add(cb.equal(aRootHighest.get("agentId"), agentId));
				predicatesHighest.add(cb.or(
					cb.equal(azRootHighest.get("agentIdString"), siteId),
					azRootHighest.get("agentIdString").in(groupIds)
				));
				predicatesHighest.add(cb.equal(azRootHighest.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));
				predicatesHighest.add(cb.equal(azRootHighest.get("qualifierId"), pRootHighest.get("publishedAssessmentId")));
				predicatesHighest.add(cb.or(
					cb.equal(pRootHighest.get("status"), 1),
					cb.equal(pRootHighest.get("status"), 3)
				));

				cqHighest.where(predicatesHighest.toArray(new Predicate[0]));
				// sorted by finalScore DESC
				cqHighest.orderBy(
					cb.desc(pRootHighest.get("publishedAssessmentId")),
					cb.desc(aRootHighest.get("finalScore")),
					cb.desc(aRootHighest.get("submittedDate"))
				);

				highest_list = session.createQuery(cqHighest).getResultList();
			} else {
				CriteriaQuery<AssessmentGradingData> cqLast = cb.createQuery(AssessmentGradingData.class);
				Root<AssessmentGradingData> aRootLast = cqLast.from(AssessmentGradingData.class);
				Root<PublishedAssessmentData> pRootLast = cqLast.from(PublishedAssessmentData.class);
				Root<AuthorizationData> azRootLast = cqLast.from(AuthorizationData.class);

				cqLast.select(cb.construct(AssessmentGradingData.class,
					aRootLast.get("assessmentGradingId"), pRootLast.get("publishedAssessmentId"), pRootLast.get("title"), aRootLast.get("agentId"),
					aRootLast.get("submittedDate"), aRootLast.get("isLate"),
					aRootLast.get("forGrade"), aRootLast.get("totalAutoScore"), aRootLast.get("totalOverrideScore"), aRootLast.get("finalScore"),
					aRootLast.get("comments"), aRootLast.get("status"), aRootLast.get("gradedBy"), aRootLast.get("gradedDate"), aRootLast.get("attemptDate"),
					aRootLast.get("timeElapsed")
				));

				List<Predicate> predicatesLast = new ArrayList<>();
				predicatesLast.add(cb.equal(aRootLast.get("publishedAssessmentId"), pRootLast.get("publishedAssessmentId")));
				predicatesLast.add(cb.isTrue(aRootLast.get("forGrade")));
				predicatesLast.add(cb.greaterThan(aRootLast.get("status"), AssessmentGradingData.REMOVED));
				predicatesLast.add(cb.equal(aRootLast.get("agentId"), agentId));
				predicatesLast.add(cb.equal(azRootLast.get("agentIdString"), siteId));
				predicatesLast.add(cb.equal(azRootLast.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));
				predicatesLast.add(cb.equal(azRootLast.get("qualifierId"), pRootLast.get("publishedAssessmentId")));

				cqLast.where(predicatesLast.toArray(new Predicate[0]));
				// sorted by submittedData DESC
				cqLast.orderBy(
					cb.desc(pRootLast.get("publishedAssessmentId")),
					cb.desc(aRootLast.get("submittedDate"))
				);

				last_list = session.createQuery(cqLast).getResultList();

				CriteriaQuery<AssessmentGradingData> cqHighest = cb.createQuery(AssessmentGradingData.class);
				Root<AssessmentGradingData> aRootHighest = cqHighest.from(AssessmentGradingData.class);
				Root<PublishedAssessmentData> pRootHighest = cqHighest.from(PublishedAssessmentData.class);
				Root<AuthorizationData> azRootHighest = cqHighest.from(AuthorizationData.class);

				cqHighest.select(cb.construct(AssessmentGradingData.class,
					aRootHighest.get("assessmentGradingId"), pRootHighest.get("publishedAssessmentId"), pRootHighest.get("title"), aRootHighest.get("agentId"),
					aRootHighest.get("submittedDate"), aRootHighest.get("isLate"),
					aRootHighest.get("forGrade"), aRootHighest.get("totalAutoScore"), aRootHighest.get("totalOverrideScore"), aRootHighest.get("finalScore"),
					aRootHighest.get("comments"), aRootHighest.get("status"), aRootHighest.get("gradedBy"), aRootHighest.get("gradedDate"), aRootHighest.get("attemptDate"),
					aRootHighest.get("timeElapsed")
				));

				List<Predicate> predicatesHighest = new ArrayList<>();
				predicatesHighest.add(cb.equal(aRootHighest.get("publishedAssessmentId"), pRootHighest.get("publishedAssessmentId")));
				predicatesHighest.add(cb.isTrue(aRootHighest.get("forGrade")));
				predicatesHighest.add(cb.greaterThan(aRootHighest.get("status"), AssessmentGradingData.REMOVED));
				predicatesHighest.add(cb.equal(aRootHighest.get("agentId"), agentId));
				predicatesHighest.add(cb.equal(azRootHighest.get("agentIdString"), siteId));
				predicatesHighest.add(cb.equal(azRootHighest.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));
				predicatesHighest.add(cb.equal(azRootHighest.get("qualifierId"), pRootHighest.get("publishedAssessmentId")));

				cqHighest.where(predicatesHighest.toArray(new Predicate[0]));
				// sorted by finalScore DESC
				cqHighest.orderBy(
					cb.desc(pRootHighest.get("publishedAssessmentId")),
					cb.desc(aRootHighest.get("finalScore")),
					cb.desc(aRootHighest.get("submittedDate"))
				);

				highest_list = session.createQuery(cqHighest).getResultList();
			}

			//getEvaluationModel();
			CriteriaQuery<Object[]> cqEval = cb.createQuery(Object[].class);
			Root<PublishedEvaluationModel> eRoot = cqEval.from(PublishedEvaluationModel.class);
			Root<PublishedAccessControl> acRoot = cqEval.from(PublishedAccessControl.class);
			Root<AuthorizationData> azRootEval = cqEval.from(AuthorizationData.class);

			cqEval.select(cb.array(eRoot.get("assessment").get("publishedAssessmentId"), eRoot.get("scoringType"), acRoot.get("submissionsAllowed")));

			groupIds.add(siteId);

			List<Predicate> predicatesEval = new ArrayList<>();
			predicatesEval.add(cb.equal(eRoot.get("assessment").get("publishedAssessmentId"), acRoot.get("assessment").get("publishedAssessmentId")));
			predicatesEval.add(cb.equal(azRootEval.get("qualifierId"), acRoot.get("assessment").get("publishedAssessmentId")));
			predicatesEval.add(azRootEval.get("agentIdString").in(groupIds));
			predicatesEval.add(cb.equal(azRootEval.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"));

			cqEval.where(predicatesEval.toArray(new Predicate[0]));

			List<Object[]> l = session.createQuery(cqEval).getResultList();

			Map<Long, Integer> scoringTypeMap = new HashMap<>();
			for (Object[] o : l) {
				scoringTypeMap.put((Long) o[0], (Integer) o[1]);
			}

			// The sorting for each column will be done in the action listener.
			List<AssessmentGradingData> assessmentList = new ArrayList<>();
			Long currentid = 0L;
			Integer scoringOption;

			// now go through the last_list, and get the first entry in the list for
			// each publishedAssessment
			for (AssessmentGradingData a : last_list) {
				// get the scoring option
				if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
					scoringOption = scoringTypeMap.get(a.getPublishedAssessmentId());
				} else {
					// I use Last as default because it is what set above
					scoringOption = EvaluationModelIfc.LAST_SCORE;
				}

				if (EvaluationModelIfc.LAST_SCORE.equals(scoringOption)) {
					if (!a.getPublishedAssessmentId().equals(currentid) || allAssessments) {

						if (!a.getPublishedAssessmentId().equals(currentid)) {
							a.setIsRecorded(true);
						}
						assessmentList.add(a);
						currentid = a.getPublishedAssessmentId();
					}
				}
			}

			// now go through the highest_list, and get the first entry in the list
			// for each publishedAssessment
			currentid = 0L;
			for (AssessmentGradingData a : highest_list) {
				// get the scoring option
				if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
					scoringOption = scoringTypeMap.get(a.getPublishedAssessmentId());
				} else {
					// I use Last as default because it is what set above
					scoringOption = EvaluationModelIfc.LAST_SCORE;
				}

				if (EvaluationModelIfc.HIGHEST_SCORE.equals(scoringOption)) {
					if (!a.getPublishedAssessmentId().equals(currentid) || allAssessments) {

						if (!a.getPublishedAssessmentId().equals(currentid)) {
							a.setIsRecorded(true);
						}
						assessmentList.add(a);
						currentid = a.getPublishedAssessmentId();
					}
				}

				if (EvaluationModelIfc.AVERAGE_SCORE.equals(scoringOption)) {
					assessmentList.add(a);
				}
			}

			return assessmentList;
		} catch (Exception e) {
			log.warn("Error getting basic info of last/highest/average submitted assessments for agent {} and site {}: {}", agentId, siteId, e.toString());
			return new ArrayList<>();
		}
	}

	public PublishedAssessmentData getBasicInfoOfPublishedAssessment(final Long publishedId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<PublishedAccessControl> cRoot = cq.from(PublishedAccessControl.class);
			Root<PublishedFeedback> fRoot = cq.from(PublishedFeedback.class);

			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				cRoot.get("releaseTo"),
				cRoot.get("startDate"),
				cRoot.get("dueDate"),
				cRoot.get("retractDate"),
				cRoot.get("feedbackDate"),
				fRoot.get("feedbackDelivery"),
				fRoot.get("feedbackComponentOption"),
				fRoot.get("feedbackAuthoring"),
				cRoot.get("lateHandling"),
				cRoot.get("unlimitedSubmissions"),
				cRoot.get("submissionsAllowed"),
				cRoot.get("feedbackEndDate"),
				cRoot.get("feedbackScoreThreshold")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(cRoot.get("assessment").get("publishedAssessmentId"), pRoot.get("publishedAssessmentId")));
			predicates.add(cb.equal(pRoot.get("publishedAssessmentId"), fRoot.get("assessment").get("publishedAssessmentId")));
			predicates.add(cb.equal(pRoot.get("publishedAssessmentId"), publishedId));

			cq.where(predicates.toArray(new Predicate[0]));

			List<PublishedAssessmentData> list = session.createQuery(cq).getResultList();

			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		} catch (Exception e) {
			log.warn("Error getting basic info for assessment {}: {}", publishedId, e.toString());
			return null;
		}
	}

	public String getPublishedAssessmentSiteId(String publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AuthorizationData> cq = cb.createQuery(AuthorizationData.class);
			Root<AuthorizationData> root = cq.from(AuthorizationData.class);

			cq.select(root)
				.where(
					cb.equal(root.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"),
					cb.equal(root.get("qualifierId"), publishedAssessmentId)
			);

			List<AuthorizationData> l = session.createQuery(cq).getResultList();

			PublishedAssessmentData publishedAssessment = loadPublishedAssessment(Long.valueOf(publishedAssessmentId));
			Boolean releaseToGroups = null;
			if (publishedAssessment != null && publishedAssessment.getAssessmentAccessControl() != null) {
				releaseToGroups = AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(
						publishedAssessment.getAssessmentAccessControl().getReleaseTo());
			}
			for (AuthorizationData a : l) {
				String agentId = a.getAgentIdString();
				if (Boolean.TRUE.equals(releaseToGroups)) {
					Group group = siteService.findGroup(agentId);
					if (group != null && group.getContainingSite() != null) {
						return group.getContainingSite().getId();
					}
					continue;
				}
				if (Boolean.FALSE.equals(releaseToGroups)) {
					return agentId;
				}
				try {
					Site site = siteService.getSite(agentId);
					if (site != null) {
						return site.getId();
					}
				} catch (IdUnusedException ex) {
					// not a site id
				}
				Group group = siteService.findGroup(agentId);
				if (group != null && group.getContainingSite() != null) {
					return group.getContainingSite().getId();
				}
			}
			return "";
		} catch (Exception e) {
			log.warn("Error getting published assessment site ID for assessment {}: {}", publishedAssessmentId, e.toString());
			return "";
		}
	}
	  
	/**
	 * to take account of difference in obtaining question count
	 * between randomized and non-randomized questions
	 */  
	public Integer getPublishedItemCount(final Long publishedAssessmentId) {
		return getPublishedItemCountForNonRandomSections(publishedAssessmentId) + getPublishedItemCountForRandomSections(publishedAssessmentId);
	}

	/**
	 * @param publishedAssessmentId
	 * @return
	 */
	public Integer getPublishedItemCountForRandomSections(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);

			Root<PublishedSectionData> sRoot = cq.from(PublishedSectionData.class);
			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<PublishedSectionMetaData> mRoot = cq.from(PublishedSectionMetaData.class);

			cq.select(mRoot.get("entry"));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(pRoot.get("publishedAssessmentId"), publishedAssessmentId));
			predicates.add(cb.equal(mRoot.get("label"), SectionDataIfc.NUM_QUESTIONS_DRAWN));
			predicates.add(cb.equal(pRoot, sRoot.get("assessment")));
			predicates.add(cb.equal(mRoot.get("section"), sRoot));

			cq.where(predicates.toArray(new Predicate[0]));

			List<String> list = session.createQuery(cq).getResultList();

			int sum = 0;
			for (String entry : list) {
				if (entry != null && !entry.isEmpty()) {
					try {
						sum += Integer.parseInt(entry);
					} catch (NumberFormatException e) {
						log.warn("Invalid number format for entry: {}", entry);
					}
				}
			}
			return sum;
		} catch (Exception e) {
			log.warn("Error getting item count for random sections for assessment {}: {}", publishedAssessmentId, e.toString());
			return 0;
		}
	}

	/**
	 * @param publishedAssessmentId
	 * @return
	 */
	public Integer getPublishedItemCountForNonRandomSections(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);

			Root<PublishedItemData> iRoot = cq.from(PublishedItemData.class);
			Join<PublishedItemData, PublishedSectionData> sJoin = iRoot.join("section");
			Join<PublishedSectionData, PublishedAssessmentData> pJoin = sJoin.join("assessment");
			Join<PublishedSectionData, PublishedSectionMetaData> mJoin = sJoin.join("sectionMetaData");

			cq.select(cb.count(iRoot));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(pJoin.get("publishedAssessmentId"), publishedAssessmentId));
			predicates.add(cb.equal(mJoin.get("label"), SectionDataIfc.AUTHOR_TYPE));
			predicates.add(cb.equal(mJoin.get("entry"), SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString()));

			cq.where(predicates.toArray(new Predicate[0]));

			List<Long> list = session.createQuery(cq).getResultList();
			if (!list.isEmpty()) {
				return list.get(0).intValue();
			}
			return 0;
		} catch (Exception e) {
			log.warn("Error getting item count for non-random sections for assessment {}: {}", publishedAssessmentId, e.toString());
			return 0;
		}
	}

	public Integer getPublishedSectionCount(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<PublishedSectionData> sRoot = cq.from(PublishedSectionData.class);

			cq.select(cb.count(sRoot))
				.where(cb.equal(sRoot.get("assessment").get("publishedAssessmentId"), publishedAssessmentId));

			List<Long> list = session.createQuery(cq).getResultList();

			if (!list.isEmpty()) {
				return list.get(0).intValue();
			}
			return 0;
		} catch (Exception e) {
			log.warn("Error getting section count for assessment {}: {}", publishedAssessmentId, e.toString());
			return 0;
		}
	}

	public PublishedAttachmentData getPublishedAttachmentData(Long attachmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAttachmentData> cq = cb.createQuery(PublishedAttachmentData.class);
			Root<PublishedAttachmentData> root = cq.from(PublishedAttachmentData.class);

			cq.select(root)
				.where(cb.equal(root.get("attachmentId"), attachmentId));

			List<PublishedAttachmentData> l = session.createQuery(cq).getResultList();

			if (!l.isEmpty()) {
				return l.get(0);
			}
			return null;
		} catch (Exception e) {
			log.warn("Error getting published attachment data for ID {}: {}", attachmentId, e.getMessage());
			return null;
		}
	}

	public void updateAssessmentLastModifiedInfo(PublishedAssessmentFacade publishedAssessmentFacade) {
		AssessmentBaseIfc data = publishedAssessmentFacade.getData();
		data.setLastModifiedBy(AgentFacade.getAgentString());
		data.setLastModifiedDate(new Date());
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				session.merge(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem update assessment: " + e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public void saveOrUpdateSection(SectionFacade section) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				session.merge(section.getData());
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update section: " + e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public PublishedSectionFacade addSection(Long publishedAssessmentId) {
		// #1 - get the assessment and attach teh new section to it
		// we are working with Data instead of Facade in this method but should
		// return
		// SectionFacade at the end
		PublishedAssessmentData assessment = loadPublishedAssessment(publishedAssessmentId);
		// lazy loading on sectionSet, so need to initialize it
		Set<PublishedSectionData> sectionSet = getSectionSetForAssessment(publishedAssessmentId);
		assessment.setSectionSet(sectionSet);

		// #2 - will called the section "Section d" here d is the total no. of
		// section in this assessment
		// #2 section has no default name - per Marc's new mockup
		PublishedSectionData section = new PublishedSectionData(
				null,
				sectionSet.size() + 1, // NEXT section
				"", "", TypeD.DEFAULT_SECTION, SectionData.ACTIVE_STATUS,
				AgentFacade.getAgentString(), new Date(), AgentFacade.getAgentString(), new Date());
		section.setAssessment(assessment);
		section.setAssessmentId(assessment.getAssessmentId());

		// add default part type, and question Ordering
		section.addSectionMetaData(SectionDataIfc.AUTHOR_TYPE, SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
		section.addSectionMetaData(SectionDataIfc.QUESTIONS_ORDERING, SectionDataIfc.AS_LISTED_ON_ASSESSMENT_PAGE.toString());

		sectionSet.add(section);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				session.merge(section);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update assessment: {}", e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
		return new PublishedSectionFacade(section);
	}

	public PublishedSectionFacade getSection(Long sectionId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			PublishedSectionData publishedSection = session.get(PublishedSectionData.class, sectionId);

			if (publishedSection == null) {
				log.warn("Section with ID {} not found", sectionId);
				return null;
			}

			return new PublishedSectionFacade(publishedSection);
		} catch (Exception e) {
			log.warn("Error getting section with ID {}: {}", sectionId, e.toString());
			return null;
		}
	}

	public AssessmentAccessControlIfc loadPublishedAccessControl(Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAccessControl> cq = cb.createQuery(PublishedAccessControl.class);
			Root<PublishedAccessControl> cRoot = cq.from(PublishedAccessControl.class);
			Join<PublishedAccessControl, PublishedAssessmentData> pJoin = cRoot.join("assessment");

			cq.select(cRoot)
				.where(cb.equal(pJoin.get("publishedAssessmentId"), publishedAssessmentId));

			List<PublishedAccessControl> list = session.createQuery(cq).getResultList();

			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		} catch (Exception e) {
			log.warn("Error loading published access control for assessment {}: {}", publishedAssessmentId, e.toString());
			return null;
		}
	}

	public void saveOrUpdatePublishedAccessControl(AssessmentAccessControlIfc publishedAccessControl) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				session.merge(publishedAccessControl);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update publishedAccessControl data: {}", e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	private List<String> getSiteGroupIdsForSubmittingAgent(String agentId, String siteId) {

		final List<String> groupIds = new ArrayList<>();
		// To accommodate the problem with Hibernate and empty array parameters 
		// TODO: this should probably be handled in a more efficient way
		groupIds.add("none");  
		
		if (siteId == null)
			return groupIds;
		
		Collection<Group> siteGroups = null;
		
		try {
			Site s = siteService.getSite(siteId);
			if (s != null)
				siteGroups = s.getGroupsWithMember(agentId);
		}
		catch (IdUnusedException ex) {
			// no site found
			log.debug("No site found for siteid: " + siteId + "agentid: " + agentId);
		}

		if (siteGroups != null) {
			Iterator<Group> groupsIter = siteGroups.iterator();
			
			while (groupsIter.hasNext()) {
				Group group = groupsIter.next(); 
				groupIds.add(group.getId());
			}
		}
		return groupIds;
	}
	
	private List<String> getSiteGroupIdsForCurrentUser(final String siteId) {
		String currentUserId = userDirectoryService.getCurrentUser().getId();
		return getSiteGroupIdsForSubmittingAgent(currentUserId, siteId);
	}
		
	/**
	 * 
	 * @param assessmentId
	 * @return
	 */
	private Map<String, String> getReleaseToGroups(Map groupsForSite, Long assessmentId) {
		Map<String, String> releaseToGroups = new HashMap();
		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
		List authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", assessmentId.toString());
		if (authorizations != null && authorizations.size()>0) {
			Iterator authsIter = authorizations.iterator();
			while (authsIter.hasNext()) {
				AuthorizationData ad = (AuthorizationData) authsIter.next();
				if (groupsForSite.containsKey(ad.getAgentIdString())) {
					String group = groupsForSite.get(ad.getAgentIdString()).toString();
					if (group != null) {
						releaseToGroups.put(ad.getAgentIdString(), group);
					}
				}
			}
			releaseToGroups.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				(oldValue, newValue) -> oldValue, LinkedHashMap::new));
		}
		return releaseToGroups;
	}
	
	/**
	 * added by Sam Ottenhoff Feb 2010
	 * Returns all groups for site
	 * @param siteId
	 * @return
	 */
	private Map<String, String> getGroupsForSite(String siteId){

		try {
			return siteService.getSite(siteId).getGroups()
				.stream().collect(Collectors.toMap(Group::getId, Group::getTitle));
		} catch (IdUnusedException ex) {
			log.warn("No site for id {}", siteId);
		}
		return Collections.EMPTY_MAP;
	}

	/**
	 * Returns all groups for site
	 */
	public Map getGroupsForSite() {
		String siteId = toolManager.getCurrentPlacement().getContext();
		return getGroupsForSite(siteId);
	}

	public List<String> getReleaseToGroupIdsForPublishedAssessment(final String publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);
			Root<AuthorizationData> root = cq.from(AuthorizationData.class);

			cq.select(root.get("agentIdString"))
				.where(
					cb.equal(root.get("functionId"), "TAKE_PUBLISHED_ASSESSMENT"),
					cb.equal(root.get("qualifierId"), publishedAssessmentId)
				);

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting release to group IDs for assessment {}: {}", publishedAssessmentId, e.toString());
			return new ArrayList<>();
		}
	}

	public Integer getPublishedAssessmentStatus(Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
			Root<PublishedAssessmentData> root = cq.from(PublishedAssessmentData.class);

			cq.select(root.get("status"))
				.where(cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId));

			List<Integer> l = session.createQuery(cq).getResultList();

			if (!l.isEmpty()) {
				return l.get(0);
			}
			return AssessmentBaseIfc.DEAD_STATUS;
		} catch (Exception e) {
			log.warn("Error getting status for assessment {}: {}", publishedAssessmentId, e.toString());
			return AssessmentBaseIfc.DEAD_STATUS;
		}
	}

	public AssessmentAttachmentIfc createAssessmentAttachment(AssessmentIfc assessment, String resourceId, String filename, String protocol) {
		PublishedAssessmentAttachment attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				attach = new PublishedAssessmentAttachment();
				attach.setAssessment(assessment);
				attach.setResourceId(resourceId);
				attach.setFilename(filename);
				attach.setMimeType(cr.getContentType());
				// we want to display kb, so divide by 1000 and round the result
				attach.setFileSize(fileSizeInKB(cr.getContentLength()));
				if (cr.getContentType().lastIndexOf("url") > -1) {
					isLink = Boolean.TRUE;
					if (!filename.toLowerCase().startsWith("http")) {
						String adjustedFilename = "http://" + filename;
						attach.setFilename(adjustedFilename);
					} else {
						attach.setFilename(filename);
					}
				} else {
					attach.setFilename(filename);
				}
				attach.setIsLink(isLink);
				attach.setStatus(AssessmentAttachmentIfc.ACTIVE_STATUS);
				attach.setCreatedBy(p.getProperty(p.getNamePropCreator()));
				attach.setCreatedDate(new Date());
				attach.setLastModifiedBy(p.getProperty(p
						.getNamePropModifiedBy()));
				attach.setLastModifiedDate(new Date());
				attach.setLocation(getRelativePath(cr.getUrl(), protocol));
				// getHibernateTemplate().save(attach);
			}
		} catch (PermissionException | IdUnusedException | TypeException pe) {
			log.warn(pe.getMessage());
		}
		return attach;
	}
	
	private long fileSizeInKB(long fileSize) {
		return fileSize / 1024;
	}
	
	public void removeAssessmentAttachment(Long assessmentAttachmentId) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				PublishedAssessmentAttachment assessmentAttachment = session.get(PublishedAssessmentAttachment.class, assessmentAttachmentId);

				if (assessmentAttachment == null) {
					log.warn("Assessment attachment with ID {} not found", assessmentAttachmentId);
					retryCount = 0;
					return;
				}

				AssessmentIfc assessment = assessmentAttachment.getAssessment();
				if (assessment != null) {
					Set<AssessmentAttachmentIfc> set = assessment.getAssessmentAttachmentSet();
					if (set != null && set.contains(assessmentAttachment)) {
						set.remove(assessmentAttachment);
						session.merge(assessment);
					}
				}

				session.remove(assessmentAttachment);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleting assessment attachment with ID {}: {}", assessmentAttachmentId, e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
			String resourceId, String filename, String protocol) {
		PublishedSectionAttachment attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				attach = new PublishedSectionAttachment();
				attach.setSection(section);
				attach.setResourceId(resourceId);
				attach.setMimeType(cr.getContentType());
				// we want to display kb, so divide by 1000 and round the result
				attach.setFileSize(fileSizeInKB(cr.getContentLength()));
				if (cr.getContentType().lastIndexOf("url") > -1) {
					isLink = Boolean.TRUE;
					if (!filename.toLowerCase().startsWith("http")) {
						String adjustedFilename = "http://" + filename;
						attach.setFilename(adjustedFilename);
					} else {
						attach.setFilename(filename);
					}
				} else {
					attach.setFilename(filename);
				}
				attach.setIsLink(isLink);
				attach.setStatus(SectionAttachmentIfc.ACTIVE_STATUS);
				attach.setCreatedBy(p.getProperty(p.getNamePropCreator()));
				attach.setCreatedDate(new Date());
				attach.setLastModifiedBy(p.getProperty(p
						.getNamePropModifiedBy()));
				attach.setLastModifiedDate(new Date());
				attach.setLocation(getRelativePath(cr.getUrl(), protocol));
				// getHibernateTemplate().save(attach);
			}
		} catch (PermissionException | IdUnusedException | TypeException pe) {
			log.warn(pe.getMessage());
		}
		return attach;
	}

	public void removeSectionAttachment(Long sectionAttachmentId) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Session session = sessionFactory.getCurrentSession();
				PublishedSectionAttachment sectionAttachment = session.get(PublishedSectionAttachment.class, sectionAttachmentId);

				if (sectionAttachment == null) {
					log.warn("Section attachment with ID {} not found", sectionAttachmentId);
					retryCount = 0;
					return;
				}

				SectionDataIfc section = sectionAttachment.getSection();
				if (section != null) {
					Set<SectionAttachmentIfc> set = section.getSectionAttachmentSet();
					set.remove(sectionAttachment);
					session.merge(section);
				}

				session.remove(sectionAttachment);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleting section attachment with ID {}: {}", sectionAttachmentId, e.toString());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		try {
			Session session = sessionFactory.getCurrentSession();
			for (AttachmentIfc attachment : list) {
				session.merge(attachment);
			}
		} catch (Exception e) {
			log.warn("Error saving or updating attachments: {}", e.toString());
			throw new DataAccessResourceFailureException("Failed to save or update attachments", e);
		}
	}

	public PublishedAssessmentFacade getPublishedAssessmentInfoForRemove(Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			PublishedAssessmentData a = session.get(PublishedAssessmentData.class, publishedAssessmentId);

			if (a == null) {
				log.warn("Assessment with ID {} not found", publishedAssessmentId);
				return null;
			}

			return new PublishedAssessmentFacade(a.getAssessmentId(), a.getTitle(), a.getCreatedBy());
		} catch (Exception e) {
			log.warn("Error getting published assessment info for removal with ID {}: {}", publishedAssessmentId, e.toString());
			return null;
		}
	}

	public Map<Long, String> getToGradebookPublishedAssessmentSiteIdMap() {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

			Root<PublishedEvaluationModel> emRoot = cq.from(PublishedEvaluationModel.class);
			Root<AuthorizationData> aRoot = cq.from(AuthorizationData.class);

			cq.select(cb.array(
				emRoot.get("assessment").get("publishedAssessmentId"),
				aRoot.get("agentIdString")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(aRoot.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(emRoot.get("assessment").get("publishedAssessmentId"), aRoot.get("qualifierId")));
			predicates.add(cb.or(
				cb.equal(emRoot.get("toGradeBook"), "1"),
				cb.equal(emRoot.get("toGradeBook"), EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString())
			));

			cq.where(predicates.toArray(new Predicate[0]));

			List<Object[]> l = session.createQuery(cq).getResultList();
			Map<Long, String> map = new HashMap<>();
			for (Object[] o : l) {
				if (o.length >= 2 && o[0] != null) {
					map.put((Long) o[0], (String) o[1]);
				}
			}
			return map;
		} catch (Exception e) {
			log.warn("Error getting to gradebook published assessment site ID map: {}", e.toString());
			return new HashMap<>();
		}
	}

	public List<AssessmentGradingData> getAllAssessmentsGradingDataByAgentAndSiteId(final String agentId, final String siteId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AssessmentGradingData> cq = cb.createQuery(AssessmentGradingData.class);

			Root<AssessmentGradingData> aRoot = cq.from(AssessmentGradingData.class);
			Root<AuthorizationData> azRoot = cq.from(AuthorizationData.class);

			cq.select(aRoot);

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(aRoot.get("agentId"), agentId));
			predicates.add(cb.isTrue(aRoot.get("forGrade")));
			predicates.add(cb.greaterThan(aRoot.get("status"), AssessmentGradingData.REMOVED));
			predicates.add(cb.equal(azRoot.get("agentIdString"), siteId));
			predicates.add(cb.equal(azRoot.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(azRoot.get("qualifierId"), aRoot.get("publishedAssessmentId")));

			cq.where(predicates.toArray(new Predicate[0]));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting assessment grading data for agent {} and site {}: {}", agentId, siteId, e.toString());
			return new ArrayList<>();
		}
	}

	/**
	 * Replaces embedded site ids for Items.
	 * Helper method for replaceEmbeddedSiteIds(); handles the question (item) level. This
	 * includes the item-level feedback and itemText objects containing more item data.
	 * Called by replaceEmbeddedSiteIds()
	 * Calls into replaceEmbeddedSiteIdsForItemText() in order to access more detailed item data.
	 * 
	 * @param item
	 */
	private void replaceEmbeddedSiteIdsForItem(PublishedItemData item) {
		String toContext = AgentFacade.getCurrentSiteId();		
		
		replaceEmbeddedSiteIdsForItemText(item.getItemTextSet(), toContext);
		
		// Replace the hint (model answer) site ids
		String itemHint = item.getHint();
		if (itemHint != null) {
			item.setHint(replaceSiteIdsForString(itemHint,toContext));
		}
		
		replaceEmbeddedSiteIdsForFeedback(item.getItemFeedbackSet(), toContext);
	}
	
	/**
	 * Replaces embedded site ids for ItemText objects.
	 * Helper method for replaceEmbeddedSiteIds(); handles the ItemText objects which contain
	 * text and answers for each question (item).  
	 * Called by replaceEmbeddedSiteIdsForItems()
	 * Calls into replaceEmbeddedSiteIdsForItemText() to replace more detailed item information.
	 * 
	 * @param itemTextSet
	 * @param toContext Site Id
	 */
	private void replaceEmbeddedSiteIdsForItemText(Set itemTextSet, String toContext) {
		
		Iterator itemTextSetIter = itemTextSet.iterator();
		while (itemTextSetIter.hasNext()) {
			PublishedItemText itemText = (PublishedItemText) itemTextSetIter.next();
			String itemTextString = itemText.getText();
			if (itemTextString != null) {
				itemText.setText(replaceSiteIdsForString(itemTextString,toContext)); // text for the question (item)
			}
								
			// Go through each answer (A,B,C,etc) object and replace the Site Ids
			replaceEmbeddedSiteIdsForAnswers(itemText.getAnswerSet(), toContext);
			
		}
	}
	
	/**
	 * Replaces embedded site ids for Item Answers.
	 * Helper method for replaceEmbeddedSiteIds(); handles the Answers from items.
	 * Called by replaceEmbeddedSiteIdsForItemText()
	 * Calls into replaceEmbeddedSiteIdsForAnswerFeedback() to access each question-level
	 * feedback.
	 * 
	 * @param answerSet
	 * @param toContext Site Id
	 */
	private void replaceEmbeddedSiteIdsForAnswers(Set answerSet, String toContext) {
		
		Iterator answerSetIter = answerSet.iterator();
		while (answerSetIter.hasNext()) {
			PublishedAnswer answer = (PublishedAnswer) answerSetIter.next();
			String answerText = answer.getText();
			if (answerText != null) {
				answer.setText(replaceSiteIdsForString(answerText,toContext)); // each answer text (A,B,C,etc)
			}
			
			// Go through the answer-level feedback and replace site ids
			replaceEmbeddedSiteIdsForAnswerFeedback(answer.getAnswerFeedbackSet(), toContext);
			
		}
	}
	
	/**
	 * Replaces embedded site ids for Answer-level feedback
	 * Helper method for replaceEmbeddedSiteIds(); handles the answer-level feedback.
	 * Called by replaceEmbeddedSiteIdsForAnswers()
	 * 
	 * @param answerFeedbackSet
	 * @param toContext Site Id
	 */
	private void replaceEmbeddedSiteIdsForAnswerFeedback(Set answerFeedbackSet, String toContext) {
		
		Iterator answerFeedbackSetIter = answerFeedbackSet.iterator();
		while (answerFeedbackSetIter.hasNext()) {
			PublishedAnswerFeedback answerFeedback = (PublishedAnswerFeedback) answerFeedbackSetIter.next();
			String answerFeedbackText = answerFeedback.getText();
			if (answerFeedbackText != null) {
				answerFeedback.setText(replaceSiteIdsForString(answerFeedbackText,toContext)); // answer-level
			}
		}
	}
	
	/**
	 * Replaces embedded site ids for ItemFeedback objects
	 * Helper method for replaceEmbeddedSiteIds(); hanldes the itemfeedback objects which contain
	 * text for question-level feedback.
	 * Called by replaceEmbeddedSiteIdsForItems()
	 * 
	 * @param feedbackSet
	 * @param toContext Site Id
	 */
	private void replaceEmbeddedSiteIdsForFeedback(Set feedbackSet, String toContext) {
		
		Iterator feedbackSetIter = feedbackSet.iterator();
		while (feedbackSetIter.hasNext()) {
			PublishedItemFeedback feedback = (PublishedItemFeedback) feedbackSetIter.next();
			String feedbackString = feedback.getText();
			if (feedbackString != null) {
				feedback.setText(replaceSiteIdsForString(feedbackString,toContext));
			}
		}
	}
	
	/**
	 * Replaces embedded site ids for individual strings contained in 
	 * assessment objects
	 * Helper method for replaceEmbeddedSiteIds();
	 * 
	 * @param assessmentStringData text contained in any assessment item object (question text, feedback text, etc)
	 * @param toContext Site Id
	 * @return updatedAssessmentStringData
	 */
	private String replaceSiteIdsForString(String assessmentStringData, String toContext) {
		boolean doReplaceSiteIds = ServerConfigurationService.getBoolean("samigo.publish.update.siteids", true);
		if (doReplaceSiteIds == false) {
			return assessmentStringData;
		}
		String updatedAssessmentStringData = null;
		
		//if contains "..getServerUrl()/access/content/group/" then it's a standard site content file
		if (assessmentStringData != null) {
			String sakaiSiteResourcePath = ServerConfigurationService.getServerUrl() + SITECONTENTPATH;
			int beginIndex = assessmentStringData.indexOf(sakaiSiteResourcePath);
			
			if (beginIndex > 0) {
				// have to loop because there may be more than one site of origin for the content
				while (beginIndex > 0) {
					int siteIdIndex = beginIndex + sakaiSiteResourcePath.length();
					int endSiteIdIndex = assessmentStringData.indexOf('/', siteIdIndex);
					String fromContext = assessmentStringData.substring(siteIdIndex, endSiteIdIndex);
					updatedAssessmentStringData = assessmentStringData.replaceAll(fromContext, toContext);
										
					beginIndex = assessmentStringData.indexOf(sakaiSiteResourcePath, endSiteIdIndex);
				} // end while
			}
			else {
				// It's not a standard site url. It's either a 'My Workspace',
				// external site url, or something else, so leave it alone.
				updatedAssessmentStringData = assessmentStringData;
			}
			
		} // end:if (assessmentStringData != null)
		
		return updatedAssessmentStringData;
	}

	public List getQuestionsIdList(final Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);

			Root<PublishedItemData> iRoot = cq.from(PublishedItemData.class);
			Join<PublishedItemData, PublishedSectionData> sJoin = iRoot.join("section");
			Join<PublishedSectionData, PublishedAssessmentData> aJoin = sJoin.join("assessment");

			cq.select(iRoot.get("itemId"))
				.where(cb.equal(aJoin.get("publishedAssessmentId"), publishedAssessmentId));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting question IDs for assessment {}: {}", publishedAssessmentId, e.getMessage());
			return new ArrayList<>();
		}
	}


	public List<PublishedAssessmentData> getPublishedDeletedAssessments(final String siteAgentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PublishedAssessmentData> cq = cb.createQuery(PublishedAssessmentData.class);

			Root<PublishedAssessmentData> pRoot = cq.from(PublishedAssessmentData.class);
			Root<AuthorizationData> zRoot = cq.from(AuthorizationData.class);

			cq.select(cb.construct(PublishedAssessmentData.class,
				pRoot.get("publishedAssessmentId"),
				pRoot.get("title"),
				pRoot.get("lastModifiedDate")
			));

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(pRoot.get("publishedAssessmentId"), zRoot.get("qualifierId")));
			predicates.add(cb.equal(zRoot.get("functionId"), "OWN_PUBLISHED_ASSESSMENT"));
			predicates.add(cb.equal(zRoot.get("agentIdString"), siteAgentId));
			predicates.add(cb.equal(pRoot.get("status"), AssessmentIfc.DEAD_STATUS));

			cq.where(predicates.toArray(new Predicate[0]));

			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.warn("Error getting deleted assessments for site {}: {}", siteAgentId, e.toString());
			return new ArrayList<>();
		}
	}

	public void restorePublishedAssessment(Long publishedAssessmentId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			PublishedAssessmentData assessment = session.get(PublishedAssessmentData.class, publishedAssessmentId);

			if (assessment == null) {
				log.warn("Assessment with ID {} not found", publishedAssessmentId);
				return;
			}

			assessment.setLastModifiedBy(AgentFacade.getAgentString());
			assessment.setLastModifiedDate(new Date());
			assessment.setStatus(AssessmentIfc.ACTIVE_STATUS);

			RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
			rubricsService.restoreRubricAssociationsByItemIdPrefix(RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX + publishedAssessmentId + ".", RubricsConstants.RBCS_TOOL_SAMIGO);

			int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
			while (retryCount > 0) {
				try {
					session.merge(assessment);
					retryCount = 0;
				} catch (Exception e) {
					log.warn("problem updating assessment: " + e.getMessage());
					retryCount = PersistenceService.getInstance().getPersistenceHelper()
							.retryDeadlock(e, retryCount);
				}
			}
		} catch (Exception e) {
			log.warn("Error restoring published assessment with ID {}: {}", publishedAssessmentId, e.toString());
		}
	}
}
