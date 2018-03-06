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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.*;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTag;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

@Slf4j
public class PublishedAssessmentFacadeQueries extends HibernateDaoSupport implements PublishedAssessmentFacadeQueriesAPI {

	private SecurityService securityService;
	private SiteService siteService;

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
		log.debug("******* metadata set" + a.getAssessmentMetaDataSet());
		log.debug("******* published metadata set" + publishedMetaDataSet);
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
				.getShowStatistics());
		publishedFeedback.setAssessmentBase(p);
		return publishedFeedback;
	}

	public PublishedAccessControl preparePublishedAccessControl(
			PublishedAssessmentData p, AssessmentAccessControl a) {
		if (a == null) {
			return new PublishedAccessControl();
		}
		PublishedAccessControl publishedAccessControl = new PublishedAccessControl(
				a.getSubmissionsAllowed(), a.getSubmissionsSaved(), a
						.getAssessmentFormat(), a.getBookMarkingItem(), a
						.getTimeLimit(), a.getTimedAssessment(), a
						.getRetryAllowed(), a.getLateHandling(), a.getInstructorNotification(), a
						.getStartDate(), a.getDueDate(), a.getScoreDate(), a
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
		HashSet h = new HashSet();
		Iterator i = metaDataSet.iterator();
		while (i.hasNext()) {
			AssessmentMetaData metaData = (AssessmentMetaData) i.next();
			PublishedMetaData publishedMetaData = new PublishedMetaData(p,
					metaData.getLabel(), metaData.getEntry());
			h.add(publishedMetaData);
		}
		return h;
	}

	public Set preparePublishedSecuredIPSet(PublishedAssessmentData p, Set ipSet) {
		HashSet h = new HashSet();
		Iterator i = ipSet.iterator();
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
		log.debug("**published section size = " + sectionSet.size());
		HashSet h = new HashSet();
		Iterator i = sectionSet.iterator();
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
		HashSet h = new HashSet();
		Iterator n = metaDataSet.iterator();
		while (n.hasNext()) {
			SectionMetaData sectionMetaData = (SectionMetaData) n.next();
			PublishedSectionMetaData publishedSectionMetaData = new PublishedSectionMetaData(
					publishedSection, sectionMetaData.getLabel(),
					sectionMetaData.getEntry());
			h.add(publishedSectionMetaData);
		}
		return h;
	}

	public Set preparePublishedItemSet(PublishedSectionData publishedSection,
			Set itemSet, String protocol) {
		log.debug("**published item size = " + itemSet.size());
		HashSet h = new HashSet();
		Iterator j = itemSet.iterator();
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
					item.getTriesAllowed(), item.getPartialCreditFlag(),item.getHash(),item.getHash());
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
			
			h.add(publishedItem);
		}
		return h;
	}

	public Set preparePublishedItemTextSet(PublishedItemData publishedItem,
			Set itemTextSet, String protocol) {
		log.debug("**published item text size = " + itemTextSet.size());
		HashSet h = new HashSet();
		Iterator k = itemTextSet.iterator();
		while (k.hasNext()) {
			ItemText itemText = (ItemText) k.next();
			log.debug("**item text id =" + itemText.getId());
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
			h.add(publishedItemText);
		}
		return h;
	}

	public Set preparePublishedItemMetaDataSet(PublishedItemData publishedItem,
			Set itemMetaDataSet) {
		HashSet h = new HashSet();
		Iterator n = itemMetaDataSet.iterator();
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
		HashSet h = new HashSet();
		Iterator o = itemFeedbackSet.iterator();
		while (o.hasNext()) {
			ItemFeedback itemFeedback = (ItemFeedback) o.next();
			PublishedItemFeedback publishedItemFeedback = new PublishedItemFeedback(
					publishedItem, itemFeedback.getTypeId(), itemFeedback
							.getText());
			h.add(publishedItemFeedback);
		}
		return h;
	}

	public Set preparePublishedItemAttachmentSet(
			PublishedItemData publishedItem, Set itemAttachmentSet,
			String protocol) {
		HashSet h = new HashSet();
		Iterator o = itemAttachmentSet.iterator();
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
        HashSet h = new HashSet();
        Iterator n = itemTagSet.iterator();
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
		HashSet h = new HashSet();
		Iterator o = itemTextAttachmentSet.iterator();
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
		HashSet h = new HashSet();
		Iterator o = sectionAttachmentSet.iterator();
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
		HashSet h = new HashSet();
		Iterator o = assessmentAttachmentSet.iterator();
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
		log.debug("**published answer size = " + answerSet.size());
		HashSet h = new HashSet();
		Iterator l = answerSet.iterator();
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
		HashSet h = new HashSet();
		Iterator m = answerFeedbackSet.iterator();
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
	 * This was created for GradebookExternalAssessmentService.
	 * We just want a quick answer whether Samigo is responsible for an id.
	 */
	public boolean isPublishedAssessmentIdValid(Long publishedAssessmentId) {
		List<PublishedAssessmentData> list = (List<PublishedAssessmentData>) getHibernateTemplate()
				.findByNamedParam("from PublishedAssessmentData where publishedAssessmentId = :id", "id", publishedAssessmentId);

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
		a.setSectionSet(getSectionSetForAssessment(a)); // this is making things slow -pbd
		String releaseToGroups = "";
		if (withGroupsInfo) {
			//TreeMap groupsForSite = getGroupsForSite();
			
			// SAM-799
            String siteId = getPublishedAssessmentSiteId(assessmentId.toString());
            Map groupsForSite = getGroupsForSite(siteId);
             
			releaseToGroups = getReleaseToGroupsAsString(groupsForSite, assessmentId);
		}
		
		PublishedAssessmentFacade f = new PublishedAssessmentFacade(a, releaseToGroups);
		return f;
	}
	
	public Long getPublishedAssessmentId(Long assessmentId) {
		List<PublishedAssessmentData> list = (List<PublishedAssessmentData>) getHibernateTemplate()
				.findByNamedParam("from PublishedAssessmentData as p where p.assessmentId = :id order by p.createdDate desc", "id", assessmentId);
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
		// add to gradebook
		if (publishedAssessment.getEvaluationModel() != null) {
			String toGradebook = publishedAssessment.getEvaluationModel()
					.getToGradeBook();

			boolean integrated = IntegrationContextFactory.getInstance()
					.isIntegrated();
			GradebookExternalAssessmentService g = null;
			if (integrated) {
				g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().getBean(
						"org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
			}

			GradebookServiceHelper gbsHelper = IntegrationContextFactory
					.getInstance().getGradebookServiceHelper();

			if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)
					&& toGradebook != null
					&& toGradebook
							.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK
									.toString())) {
				try {
					gbsHelper.addToGradebook(publishedAssessment, null, g);
				} catch (Exception e) {
					log.error("Removing published assessment: " + e);
					delete(publishedAssessment);
					throw e;
				}
			}
		}

		// write authorization
		createAuthorization(publishedAssessment);
		return new PublishedAssessmentFacade(publishedAssessment);
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

	public void createAuthorization(PublishedAssessmentData p) {
		// conditional processing
		if (p.getAssessmentAccessControl().getReleaseTo()!= null 
				&& p.getAssessmentAccessControl().getReleaseTo()
				.equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
			createAuthorizationForSelectedGroups(p);
			return;
		}
		
		String qualifierIdString = p.getPublishedAssessmentId().toString();
		Vector v = new Vector();

		// 1. get all possible publishing targets (agentKey, agentId)
		PublishingTargetHelper ptHelper = IntegrationContextFactory
				.getInstance().getPublishingTargetHelper();

		HashMap targets = ptHelper.getTargets();
		// Fixed for SAK-7251
		HashMap trimedTargets = new HashMap();

		for (Iterator it = targets.entrySet().iterator(); it.hasNext();) {
			   Map.Entry entry = (Map.Entry) it.next();
			   String key = (String)entry.getKey();
			   String value = (String)entry.getValue();
			   trimedTargets.put(key.trim(), value);
 			}
		
		// 2. get the key of the target selected, it is stored in
		// accessControl.releaseTo
		AssessmentAccessControlIfc control = p.getAssessmentAccessControl();
		String releaseTo = control.getReleaseTo();
		if (releaseTo != null) {
			String [] targetSelected = new String[1];
			targetSelected[0] = releaseTo;
			for (int i = 0; i < targetSelected.length; i++) {
				String agentKey = targetSelected[i].trim();
				// add agentId into v
				if (trimedTargets.get(agentKey) != null) {
					v.add((String) trimedTargets.get(agentKey));
				}
			}
		}
		// 3. give selected site right to view Published Assessment
		PersistenceService.getInstance().getAuthzQueriesFacade()
				.createAuthorization(AgentFacade.getCurrentSiteId(),
						"OWN_PUBLISHED_ASSESSMENT", qualifierIdString);
		// 4. create authorization for all the agentId in v
		for (int i = 0; i < v.size(); i++) {
			String agentId = (String) v.get(i);
			log.debug("** agentId=" + agentId);
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.createAuthorization(agentId, "TAKE_PUBLISHED_ASSESSMENT",
							qualifierIdString);
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.createAuthorization(agentId,
							"VIEW_PUBLISHED_ASSESSMENT_FEEDBACK",
							qualifierIdString);
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.createAuthorization(agentId, "GRADE_PUBLISHED_ASSESSMENT",
							qualifierIdString);
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.createAuthorization(agentId, "VIEW_PUBLISHED_ASSESSMENT",
							qualifierIdString);
		}
		
	}
	
	/**
	 * Creates Authorizations for Selected Groups
	 * @param p
	 */
	public void createAuthorizationForSelectedGroups(PublishedAssessmentData publishedAssessment) {
	    AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
		String qualifierIdString = publishedAssessment.getPublishedAssessmentId().toString();
		authz.createAuthorization(AgentFacade.getCurrentSiteId(), "OWN_PUBLISHED_ASSESSMENT", qualifierIdString);
		authz.createAuthorization(AgentFacade.getCurrentSiteId(), "VIEW_PUBLISHED_ASSESSMENT", qualifierIdString);

	    List authorizationsToCopy = authz.getAuthorizationByFunctionAndQualifier("TAKE_ASSESSMENT", publishedAssessment.getAssessmentId().toString());
	    if (authorizationsToCopy != null && authorizationsToCopy.size()>0) {
			 Iterator authsIter = authorizationsToCopy.iterator();
			 while (authsIter.hasNext()) {
				 AuthorizationData adToCopy = (AuthorizationData) authsIter.next();
     			 authz.createAuthorization(adToCopy.getAgentIdString(), "TAKE_PUBLISHED_ASSESSMENT", publishedAssessment.getPublishedAssessmentId().toString());
			 }
	    }
	}
	

	public AssessmentData loadAssessment(Long assessmentId) {
		return (AssessmentData) getHibernateTemplate().load(
				AssessmentData.class, assessmentId);
	}

	public PublishedAssessmentData loadPublishedAssessment(Long assessmentId) {
		PublishedAssessmentData ret = null;
		try {
			ret = (PublishedAssessmentData) getHibernateTemplate().load(
					PublishedAssessmentData.class, assessmentId);
		} catch (DataAccessException e) {
			log.warn("Error accessing Published Assesment: " + assessmentId + " storage returned: " + e);
		}
		return ret;
	}

	public List<PublishedAssessmentFacade> getAllTakeableAssessments(String orderBy, boolean ascending, final Integer status) {

		String query = "from PublishedAssessmentData as p where p.status = :status order by p." + orderBy;
		query += (ascending ? " asc" : " desc");
		log.debug("Order by " + orderBy);

		final String hql = query;
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setInteger("status", status);
            return q.list();
        };
		List<PublishedAssessmentData> list = getHibernateTemplate().execute(hcb);

		List<PublishedAssessmentFacade> assessmentList = new ArrayList<>();
		for (PublishedAssessmentData a : list) {
			log.debug("Title: " + a.getTitle());
			assessmentList.add(new PublishedAssessmentFacade(a));
		}
		return assessmentList;
	}

	public Integer getNumberOfSubmissions(final String publishedAssessmentId, final String agentId) {
		final HibernateCallback<List<Number>> hcb = session -> session.createQuery(
				"select count(a) from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade")
				.setLong("id", Long.parseLong(publishedAssessmentId))
				.setString("agent", agentId)
				.setBoolean("forgrade", true)
				.list();
		List<Number> list = getHibernateTemplate().execute(hcb);

		return list.get(0).intValue();
	}

	public List<AssessmentGradingData> getNumberOfSubmissionsOfAllAssessmentsByAgent(final String agentId) {
		final HibernateCallback<List<AssessmentGradingData>> hcb = session -> session.createQuery(
				"select new AssessmentGradingData(a.publishedAssessmentId, count(a)) " +
						"from AssessmentGradingData as a where a.agentId = :agent and a.forGrade= :forgrade " +
						"group by a.publishedAssessmentId")
				.setString("agent", agentId)
				.setBoolean("forgrade", true)
				.list();
		return getHibernateTemplate().execute(hcb);
	}

	public List<AssessmentGradingData> getNumberOfSubmissionsOfAllAssessmentsByAgent(final String agentId, final String siteId) {

		final List groupIds = getSiteGroupIdsForSubmittingAgent(agentId, siteId);

		if (groupIds.size() > 0) {
			final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
                Query q = session.createQuery(
						"select new AssessmentGradingData("
								+ " a.publishedAssessmentId, count(a)) "
								+ " from AssessmentGradingData as a, AuthorizationData as az "
								+ " where a.agentId=:agentId and a.forGrade=:forGrade "
								+ " and (az.agentIdString=:siteId or az.agentIdString in (:groupIds)) "
								+ " and az.functionId=:functionId and az.qualifierId=a.publishedAssessmentId"
								+ " group by a.publishedAssessmentId");
                q.setString("agentId", agentId);
                q.setBoolean("forGrade", true);
                q.setString("siteId", siteId);
                q.setParameterList("groupIds", groupIds);
                q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
                return q.list();
            };
			return getHibernateTemplate().execute(hcb);
		}
		else {
			final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
                Query q = session.createQuery(
						"select new AssessmentGradingData("
								+ " a.publishedAssessmentId, count(a)) "
								+ " from AssessmentGradingData as a, AuthorizationData as az "
								+ " where a.agentId=:agentId and a.forGrade=:forGrade "
								+ " and az.agentIdString=:siteId "
								+ " and az.functionId=:functionId and az.qualifierId=a.publishedAssessmentId"
								+ " group by a.publishedAssessmentId");
                q.setString("agentId", agentId);
                q.setBoolean("forGrade", true);
                q.setString("siteId", siteId);
                q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
                return q.list();
            };
			return getHibernateTemplate().execute(hcb);
		}
	}

	public List<PublishedAssessmentFacade> getAllPublishedAssessments(String sortString) {
		String orderBy = getOrderBy(sortString);
		List<PublishedAssessmentData> list = (List<PublishedAssessmentData>) getHibernateTemplate().find("from PublishedAssessmentData p order by p." + orderBy);
		List<PublishedAssessmentFacade> assessmentList = new ArrayList<>();
		for (PublishedAssessmentData a : list) {
			a.setSectionSet(getSectionSetForAssessment(a));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public List<PublishedAssessmentFacade> getAllPublishedAssessments(String sortString, final Integer status) {
		final String orderBy = getOrderBy(sortString);

		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery("from PublishedAssessmentData as p where p.status = :status order by p." + orderBy);
            q.setInteger("status", status);
            return q.list();
        };
		List<PublishedAssessmentData> list = getHibernateTemplate().execute(hcb);

		List<PublishedAssessmentFacade> assessmentList = new ArrayList<>();
		for (PublishedAssessmentData a : list) {
			a.setSectionSet(getSectionSetForAssessment(a));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
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
		PublishedAssessmentData assessment = (PublishedAssessmentData) getHibernateTemplate()
				.load(PublishedAssessmentData.class, assessmentId);
		// for preview, delete assessment
		// for others, simply set pub assessment to inactive
		if (action == null || action.equals("preview")) {
			delete(assessment);
			// remove authorization
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.removeAuthorizationByQualifier(
							assessment.getPublishedAssessmentId().toString(),
							true);
		}
		else {
			assessment.setStatus(PublishedAssessmentIfc.DEAD_STATUS);
			try {
				saveOrUpdate(assessment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}			
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
				List ip = getHibernateTemplate()
						.findByNamedParam("from PublishedSecuredIPAddress s where s.assessment.publishedAssessmentId = :id", "id", assessmentId);
				if (ip.size() > 0) {
					PublishedSecuredIPAddress s = (PublishedSecuredIPAddress) ip.get(0);
					PublishedAssessmentData a = (PublishedAssessmentData) s.getAssessment();
					a.setSecuredIPAddressSet(new HashSet());
					getHibernateTemplate().deleteAll(ip);
					retryCount = 0;
				} else
					retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleting ip address: " + e.getMessage());
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
				getHibernateTemplate().saveOrUpdate(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update assessment: {}", e.getMessage());
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
		
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, p.lastModifiedDate, p.lastModifiedBy) "
				+ " from PublishedAssessmentData p, PublishedAccessControl c, AuthorizationData z  "
				+ " where c.assessment.publishedAssessmentId = p.publishedAssessmentId and p.status=:status and "
				+ " p.publishedAssessmentId=z.qualifierId and z.functionId=:functionId "
				//+ " and (z.agentIdString=:siteId or z.agentIdString in (:groupIds)) "
				+ " and z.agentIdString=:siteId "
				+ " order by p." + orderBy;
		query += (ascending ? " asc" : " desc");

		final String hql = query;
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setInteger("status", 1);
            q.setString("functionId", "OWN_PUBLISHED_ASSESSMENT");
            q.setString("siteId", siteAgentId);
            return q.list();
        };
		List<PublishedAssessmentData> l = getHibernateTemplate().execute(hcb);

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
		Map groupsForSite = null;
		String releaseToGroups;
		String lastModifiedBy = "";
		AgentFacade agent = null;

		for (PublishedAssessmentData p : list) {
			releaseToGroups = null;
			if (p.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
				if (groupsForSite == null) {
					groupsForSite = getGroupsForSite(siteAgentId);
				}
				Long assessmentId = p.getPublishedAssessmentId();
				releaseToGroups = getReleaseToGroupsAsString(groupsForSite, assessmentId);
			}
			

			agent = new AgentFacade(p.getLastModifiedBy());
			lastModifiedBy = agent.getDisplayName();

			PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
					p.getReleaseTo(), p.getStartDate(), p.getDueDate(), releaseToGroups, p.getLastModifiedDate(), lastModifiedBy);
			pubList.add(f);
		}
		return pubList;
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
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title,"
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, p.status, p.lastModifiedDate, p.lastModifiedBy) from PublishedAssessmentData p,"
				+ " PublishedAccessControl c, AuthorizationData z  "
				+ " where c.assessment.publishedAssessmentId=p.publishedAssessmentId "
				+ " and ((p.status=:activeStatus and (c.dueDate<=:today or c.retractDate<=:today)) or p.status=:editStatus)"
				+ " and p.publishedAssessmentId=z.qualifierId and z.functionId=:functionId "
				//+ " and (z.agentIdString=:siteId or z.agentIdString in (:groupIds)) "
				+ " and z.agentIdString=:siteId "
				+ " order by p." + orderBy;

		if (ascending)
			query += " asc";
		else
			query += " desc";

		final String hql = query;
		final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setInteger("activeStatus", 1);
            q.setTimestamp("today", new Date());
            q.setInteger("editStatus", 3);
            q.setString("functionId", "OWN_PUBLISHED_ASSESSMENT");
            q.setString("siteId", siteAgentId);
            //q.setParameterList("groupIds", groupIds);
            return q.list();
        };
		List list = getHibernateTemplate().execute(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[] {new Date(), new Date(),siteAgentId} ,
		// new org.hibernate.type.Type[] {Hibernate.TIMESTAMP,
		// Hibernate.TIMESTAMP,
		// Hibernate.STRING});

		List pubList = new ArrayList();
		Map groupsForSite = null;
		String releaseToGroups;
		String lastModifiedBy = "";
		AgentFacade agent;
		for (int i = 0; i < list.size(); i++) {
			PublishedAssessmentData p = (PublishedAssessmentData) list.get(i);
			releaseToGroups = null;
			if (p.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
				if (groupsForSite == null) {
					groupsForSite = getGroupsForSite(siteAgentId);
				}
				Long assessmentId = p.getPublishedAssessmentId();
				releaseToGroups = getReleaseToGroupsAsString(groupsForSite, assessmentId);
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
	}

	public Set<PublishedSectionData> getSectionSetForAssessment(PublishedAssessmentIfc assessment) {
		List<PublishedSectionData> sectionList = (List<PublishedSectionData>) getHibernateTemplate().findByNamedParam(
				"from PublishedSectionData s where s.assessment.publishedAssessmentId = :id", "id", assessment.getPublishedAssessmentId());
		Hibernate.initialize(sectionList);
		return new HashSet<>(sectionList);
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
		return getHibernateTemplate().load(PublishedItemData.class, itemId);
	}

	public PublishedItemText loadPublishedItemText(Long itemTextId) {
		return getHibernateTemplate().load(PublishedItemText.class, itemTextId);
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

		final List<String> groupIds = getSiteGroupIdsForCurrentUser(siteId);
		String query = "";
		if (groupIds.size() > 0) {
			query = "select distinct new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, "
				+ " c.feedbackDate, f.feedbackDelivery, f.feedbackComponentOption, f.feedbackAuthoring, c.lateHandling, "
				+ " c.unlimitedSubmissions, c.submissionsAllowed, em.scoringType, p.status, p.lastModifiedDate, c.timeLimit) "
				+ " from PublishedAssessmentData as p, PublishedAccessControl as c,"
				+ " PublishedFeedback as f, AuthorizationData as az, PublishedEvaluationModel as em"
				+ " where c.assessment.publishedAssessmentId=p.publishedAssessmentId "
				+ " and p.publishedAssessmentId = f.assessment.publishedAssessmentId "
				+ " and p.publishedAssessmentId = em.assessment.publishedAssessmentId "
				+ " and (p.status=:activeStatus or p.status=:editStatus) and (az.agentIdString=:siteId or az.agentIdString in (:groupIds)) "
				+ " and az.functionId=:functionId and az.qualifierId=p.publishedAssessmentId"
				+ " order by ";
		}
		else {
			query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, "
				+ " c.feedbackDate, f.feedbackDelivery, f.feedbackComponentOption, f.feedbackAuthoring, c.lateHandling, "
				+ " c.unlimitedSubmissions, c.submissionsAllowed, em.scoringType, p.status, p.lastModifiedDate, c.timeLimit) "
				+ " from PublishedAssessmentData as p, PublishedAccessControl as c,"
				+ " PublishedFeedback as f, AuthorizationData as az, PublishedEvaluationModel as em"
				+ " where c.assessment.publishedAssessmentId=p.publishedAssessmentId "
				+ " and p.publishedAssessmentId = f.assessment.publishedAssessmentId "
				+ " and p.publishedAssessmentId = em.assessment.publishedAssessmentId "
				+ " and (p.status=:activeStatus or p.status=:editStatus) and az.agentIdString=:siteId "
				+ " and az.functionId=:functionId and az.qualifierId=p.publishedAssessmentId"
				+ " order by ";
		}
		if (ascending == false) {

			if (orderBy.equals(DUE)) {
				query += " c." + orderBy + " desc";
			} else {
				query += " p." + orderBy + " desc";
			}
		} else {
			if (orderBy.equals(DUE)) {
				query += " c." + orderBy + " asc";
			} else {
				query += " p." + orderBy + " asc";
			}
		}

		final String hql = query;
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setInteger("activeStatus", 1);
            q.setInteger("editStatus", 3);
            q.setString("siteId", siteId);
            if (groupIds.size() > 0) {
                q.setParameterList("groupIds", groupIds);
            }
            q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
            return q.list();
        };
		List<PublishedAssessmentData> list = getHibernateTemplate().execute(hcb);
		List<PublishedAssessmentFacade> pubList = new ArrayList<>();
		for (PublishedAssessmentData p : list) {
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
					p.getReleaseTo(), p.getStartDate(), p.getDueDate(), p
							.getRetractDate(), p.getFeedbackDate(), p
							.getFeedbackDelivery(), p.getFeedbackComponentOption(), p.getFeedbackAuthoring(), p
							.getLateHandling(), p.getUnlimitedSubmissions(), p
							.getSubmissionsAllowed(), p.getScoringType(), p.getStatus(), p.getLastModifiedDate(), p.getTimeLimit());
			pubList.add(f);
		}
		return pubList;
	}

	// This is for instructors view (author index page)
	public List<PublishedAssessmentFacade> getBasicInfoOfAllPublishedAssessments2(String sortString, boolean ascending, final String siteAgentId) {
		String orderBy = getOrderBy(sortString);
		
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, p.status, p.lastModifiedDate, p.lastModifiedBy, "
				+ "c.lateHandling, c.unlimitedSubmissions, c.submissionsAllowed) "
				+ " from PublishedAssessmentData p, PublishedAccessControl c, AuthorizationData z  "
				+ " where c.assessment.publishedAssessmentId = p.publishedAssessmentId "
				+ " and p.publishedAssessmentId=z.qualifierId and z.functionId=:functionId "
				+ " and z.agentIdString=:siteId and (p.status=:activeStatus or p.status=:editStatus) "
				+ " order by p." + orderBy;
		query += (ascending ? " asc" : " desc");

		final String hql = query;
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setString("functionId", "OWN_PUBLISHED_ASSESSMENT");
            q.setString("siteId", siteAgentId);
            q.setInteger("activeStatus", 1);
            q.setInteger("editStatus", 3);
            return q.list();
        };
		List<PublishedAssessmentData> list = getHibernateTemplate().execute(hcb);

		List<PublishedAssessmentFacade> pubList = new ArrayList<>();
		Map groupsForSite = null;
		String releaseToGroups;
		String lastModifiedBy = "";
		AgentFacade agent = null;

		for (PublishedAssessmentData p : list) {
			releaseToGroups = null;
			if (p.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
				if (groupsForSite == null) {
					groupsForSite = getGroupsForSite(siteAgentId);
				}
				Long assessmentId = p.getPublishedAssessmentId();
				releaseToGroups = getReleaseToGroupsAsString(groupsForSite, assessmentId);
			}
			

			agent = new AgentFacade(p.getLastModifiedBy());
			if (agent != null) {
				lastModifiedBy = agent.getDisplayName();
			}

			PublishedAssessmentFacade f = new PublishedAssessmentFacade(p.getPublishedAssessmentId(), p.getTitle(),
					p.getReleaseTo(), p.getStartDate(), p.getDueDate(), p.getRetractDate(), p.getStatus(), releaseToGroups, 
					p.getLastModifiedDate(), lastModifiedBy, p.getLateHandling(), p.getUnlimitedSubmissions(), p.getSubmissionsAllowed());
			pubList.add(f);
		}
		return pubList;
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
		final String query = "select new AssessmentGradingData("
				+ " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
				+ " a.submittedDate, a.isLate,"
				+ " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
				+ " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
				+ " a.timeElapsed) "
				+ " from AssessmentGradingData a, PublishedAssessmentData p"
				+ " where a.publishedAssessmentId = p.publishedAssessmentId  and a.forGrade = :forgrade and a.agentId = :agent"
				+ " order by p.publishedAssessmentId DESC, a.submittedDate DESC";

		/*
		 * The sorting for each type will be done in the action listener. if
		 * (orderBy.equals(TITLE)) { query += ", p." + orderBy; } else if
		 * (!orderBy.equals(SUBMITTED)) { query += ", a." + orderBy; } if
		 * (!orderBy.equals(SUBMITTED)) { if (ascending == false) { query += "
		 * desc"; } else { query += " asc"; } }
		 */

		final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(query);
            q.setBoolean("forgrade", true);
            q.setString("agent", agentId);
            return q.list();
        };
		List<AssessmentGradingData> list = getHibernateTemplate().execute(hcb);

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
		final HibernateCallback<List<Number>> hcb = session -> session
				.createQuery("select count(a) from AssessmentGradingData a where a.forGrade = :forgrade and a.publishedAssessmentId = :id")
				.setBoolean("forgrade", true)
				.setLong("id", publishedAssessmentId)
				.list();
		List<Number> l = getHibernateTemplate().execute(hcb);

		return l.get(0).intValue();
	}

	public Integer getTotalSubmission(final String agentId, final Long publishedAssessmentId) {
		final HibernateCallback<List<Number>> hcb = session -> session
				.createQuery("select count(a) from AssessmentGradingData a where a.forGrade = :forgrade and a.agentId = :agent and a.publishedAssessmentId = :id")
				.setBoolean("forgrade", true)
				.setString("agent", agentId)
				.setLong("id", publishedAssessmentId)
				.list();
		List<Number> l = getHibernateTemplate().execute(hcb);

		return l.get(0).intValue();
	}

	public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(String alias) {
		return getPublishedAssessmentIdByMetaLabel("ALIAS", alias);
	}

	public PublishedAssessmentFacade getPublishedAssessmentIdByMetaLabel(final String label, final String entry) {
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> session
				.createQuery("select p from PublishedAssessmentData p, PublishedMetaData m where p=m.assessment and m.label = :label and m.entry = :entry")
				.setString("label", label)
				.setString("entry", entry)
				.list();
		List<PublishedAssessmentData> l = getHibernateTemplate().execute(hcb);

		if (!l.isEmpty()) {
			PublishedAssessmentData p = l.get(0);
			p.setSectionSet(getSectionSetForAssessment(p));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(p);
			f.setFeedbackComponentOption(p.getAssessmentFeedback().getFeedbackComponentOption());
			return f;
		}
		return null;
	}

	public void saveOrUpdateMetaData(PublishedMetaData meta) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(meta);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update meta data: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public Map<Long, PublishedFeedback> getFeedbackHash() {
		final List listAgentId = new ArrayList();
		String siteId = AgentFacade.getCurrentSiteId();
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

		final String query = "select new PublishedFeedback("
				+ " p.assessment.publishedAssessmentId,"
				+ " p.feedbackDelivery,p.feedbackComponentOption,  p.feedbackAuthoring, p.editComponents, p.showQuestionText,"
				+ " p.showStudentResponse, p.showCorrectResponse,"
				+ " p.showStudentScore," + " p.showStudentQuestionScore,"
				+ " p.showQuestionLevelFeedback, p.showSelectionLevelFeedback,"
				+ " p.showGraderComments, p.showStatistics)"
				+ " from PublishedFeedback p, AuthorizationData az"
				+ " where az.qualifierId = p.assessment.publishedAssessmentId "
				+ " and (az.agentIdString in (:agentIdString)) "
				+ " and az.functionId=:functionId ";
		final HibernateCallback<List<PublishedFeedback>> hcb = session -> {
            Query q = session.createQuery(query);
            q.setParameterList("agentIdString", listAgentId);
            q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
            return q.list();
        };

		List<PublishedFeedback> l = getHibernateTemplate().execute(hcb);
		Map<Long, PublishedFeedback> h = new HashMap<>();
		for (PublishedFeedback f : l) {
			h.put(f.getAssessmentId(), f);
		}
		return h;
	}

	/**
	 * this return a HashMap containing (Long publishedAssessmentId,
	 * PublishedAssessmentFacade publishedAssessment) Note that the
	 * publishedAssessment is a partial object used for display only. do not use
	 * it for persisting. It only contains title, releaseTo, startDate, dueDate &
	 * retractDate
	 */
	public Map<Long, PublishedAssessmentFacade> getAllAssessmentsReleasedToAuthenticatedUsers() {
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate) "
				+ " from PublishedAssessmentData p, PublishedAccessControl c  "
				+ " where c.assessment = p and c.releaseTo like '%Authenticated Users%'";
		List<PublishedAssessmentData> l = (List<PublishedAssessmentData>) getHibernateTemplate().find(query);
		Map<Long, PublishedAssessmentFacade> h = new HashMap<>();
		for (PublishedAssessmentData p : l) {
			h.put(p.getPublishedAssessmentId(), new PublishedAssessmentFacade(p));
		}
		return h;
	}

	public String getPublishedAssessmentOwner(String publishedAssessmentId) {
	    List<AuthorizationData> l = (List<AuthorizationData>) getHibernateTemplate()
				.findByNamedParam("select a from AuthorizationData a where a.functionId = :fid and a.qualifierId = :id",
						new String[] {"fid", "id"},
						new Object[] {"OWN_PUBLISHED_ASSESSMENT", publishedAssessmentId});
		if (!l.isEmpty()) {
			AuthorizationData a = l.get(0);
			return a.getAgentIdString();
		}
		return null;
	}

	public boolean publishedAssessmentTitleIsUnique(final Long assessmentBaseId, final String title) {
		final String currentSiteId = AgentFacade.getCurrentSiteId();
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(
            		"select new PublishedAssessmentData(a.publishedAssessmentId, a.title, a.lastModifiedDate) " +
							"from PublishedAssessmentData a, AuthorizationData z " +
							"where a.title = :title and a.publishedAssessmentId != :id and a.status != :status " +
							"and z.functionId = :fid and a.publishedAssessmentId = z.qualifierId and z.agentIdString = :site"
			);
            q.setString("title", title);
            q.setLong("id", assessmentBaseId.longValue());
            q.setInteger("status", 2);
            q.setString("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setString("site", currentSiteId);
            return q.list();
        };
		List<PublishedAssessmentData> list = getHibernateTemplate().execute(hcb);

		if (!list.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean hasRandomPart(final Long publishedAssessmentId) {
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();

		final HibernateCallback<List<PublishedSectionData>> hcb = session -> session
				.createQuery("select s from PublishedSectionData s, PublishedSectionMetaData m " +
						"where s = m.section and s.assessment.publishedAssessmentId = :id and m.label = :key and m.entry = :value")
				.setLong("id", publishedAssessmentId.longValue())
				.setString("key", key)
				.setString("value", value)
				.list();
		List<PublishedSectionData> l = getHibernateTemplate().execute(hcb);

		if (!l.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public List<Long> getContainRandomPartAssessmentIds(final Collection assessmentIds) {
        if (assessmentIds == null || assessmentIds.size() < 1) {
			return new ArrayList<>();
		}
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();

		final HibernateCallback<List<Long>> hcb = session -> session
				.createQuery("select s.assessment.publishedAssessmentId " +
						"from PublishedSectionData s, PublishedSectionMetaData m " +
						"where s.assessment.publishedAssessmentId in (:ids) and s = m.section and m.label = :label and m.entry = :entry " +
						"group by s.assessment.publishedAssessmentId")
				.setString("label", key)
				.setString("entry", value)
				.setParameterList("ids", assessmentIds)
				.list();
		return getHibernateTemplate().execute(hcb);
	}

	public PublishedItemData getFirstPublishedItem(final Long publishedAssessmentId) {
		final HibernateCallback<List<PublishedItemData>> hcb = session -> session
				.createQuery("select i from PublishedAssessmentData p, PublishedSectionData s, " +
						" PublishedItemData i where p.publishedAssessmentId = :id and" +
						" p.publishedAssessmentId = s.assessment.publishedAssessmentId and s = i.section")
				.setLong("id", publishedAssessmentId)
				.list();
		List<PublishedItemData> l = getHibernateTemplate().execute(hcb);

		final HibernateCallback<List<PublishedSectionData>> hcb2 = session -> session
				.createQuery("select s from PublishedAssessmentData p, PublishedSectionData s " +
						" where p.publishedAssessmentId = :id and p.publishedAssessmentId = s.assessment.publishedAssessmentId")
				.setLong("id", publishedAssessmentId)
				.list();
		List<PublishedSectionData> sec = getHibernateTemplate().execute(hcb2);

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
	}

	public List<Long> getPublishedItemIds(final Long publishedAssessmentId) {
		final HibernateCallback<List<Long>> hcb = session -> session
				.createQuery("select i.itemId from PublishedItemData i, PublishedSectionData s, PublishedAssessmentData p " +
						"where p.publishedAssessmentId = :id and p = s.assessment and i.section = s")
				.setLong("id", publishedAssessmentId)
				.list();
		return getHibernateTemplate().execute(hcb);
	}
	
	public Set<PublishedItemData> getPublishedItemSet(final Long publishedAssessmentId, final Long sectionId) {
		final HibernateCallback<List<PublishedItemData>> hcb = session -> session
				.createQuery("select i from PublishedItemData i, PublishedSectionData s, PublishedAssessmentData p " +
						"where p.publishedAssessmentId = :id and i.section.id = :section and p = s.assessment and i.section = s")
				.setLong("id", publishedAssessmentId)
				.setLong("section", sectionId)
				.list();
		List<PublishedItemData> assessmentGradings = getHibernateTemplate().execute(hcb);

		Set<PublishedItemData> itemSet = new HashSet<>();
	    for (PublishedItemData publishedItemData : assessmentGradings) {
	    	log.debug("itemId = {}", publishedItemData.getItemId());
	    	itemSet.add(publishedItemData);
	    }
	    return itemSet;

	}

	public Long getItemType(final Long publishedItemId) {
		final HibernateCallback<List<Long>> hcb = session -> session
				.createQuery("select p.typeId from PublishedItemData p where p.itemId = :id")
				.setLong("id", publishedItemId)
				.list();
		List<Long> list = getHibernateTemplate().execute(hcb);

		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
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
				getHibernateTemplate().delete(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem removing publishedAssessment: {}", e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public Set<PublishedSectionData> getSectionSetForAssessment(Long publishedAssessmentId) {
		HibernateCallback<List<PublishedSectionData>> hcb = session -> session
				.createQuery("from PublishedSectionData s where s.assessment.publishedAssessmentId = :id")
				.setLong("id", publishedAssessmentId)
				.list();
		List<PublishedSectionData> sectionList = getHibernateTemplate().execute(hcb);
		return new HashSet<>(sectionList);
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

		final HibernateCallback<List<PublishedSectionData>> hcb = session -> session
				.createQuery("select s from PublishedSectionData s, PublishedSectionMetaData m " +
						" where s = m.section and s.assessment.publishedAssessmentId = :id and s.id = :section and m.label = :key and m.entry = :value")
				.setLong("id", publishedAssessmentId)
				.setLong("section", sectionId)
				.setString("key", key)
				.setString("value", value)
				.list();
		List<PublishedSectionData> l = getHibernateTemplate().execute(hcb);

		if (!l.isEmpty()) {
			return true;
		}
		return false;
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
		// sorted by submittedData DESC
		final String order_last = " order by p.publishedAssessmentId DESC, a.submittedDate DESC";
		// sorted by finalScore DESC
		final String order_highest = " order by p.publishedAssessmentId DESC, a.finalScore DESC, a.submittedDate DESC";

		List<AssessmentGradingData> last_list;
		List<AssessmentGradingData> highest_list;

		// Get total no. of submission per assessment by the given agent
		if (groupIds.size() > 0) {
			final String hql = "select distinct new AssessmentGradingData("
				+ " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
				+ " a.submittedDate, a.isLate,"
				+ " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
				+ " '', a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
				+ " a.timeElapsed) "
				+ " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
				+ " where a.publishedAssessmentId = p.publishedAssessmentId"
				+ " and a.forGrade=:forGrade and a.agentId=:agentId"
				+ " and (az.agentIdString=:siteId or az.agentIdString in (:groupIds)) "
				+ " and az.functionId=:functionId and az.qualifierId=p.publishedAssessmentId"
				+ " and (p.status=:activeStatus or p.status=:editStatus) ";

			final HibernateCallback<List<AssessmentGradingData>> hcb_last = session -> {
                Query q = session.createQuery(hql + order_last);
                q.setBoolean("forGrade", true);
                q.setString("agentId", agentId);
                q.setString("siteId", siteId);
                q.setParameterList("groupIds", groupIds);
                q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
                q.setInteger("activeStatus", 1);
                q.setInteger("editStatus", 3);
                return q.list();
            };

			// this list is sorted by submittedDate desc.
			last_list = getHibernateTemplate().execute(hcb_last);

			final HibernateCallback<List<AssessmentGradingData>> hcb_highest = session -> {
                Query q = session.createQuery(hql + order_highest);
                q.setBoolean("forGrade", true);
                q.setString("agentId", agentId);
                q.setString("siteId", siteId);
                q.setParameterList("groupIds", groupIds);
                q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
                q.setInteger("activeStatus", 1);
                q.setInteger("editStatus", 3);
                return q.list();
            };

			// this list is sorted by finalScore desc.

			highest_list = getHibernateTemplate().execute(hcb_highest);
		}
		else {
			final String hql = "select new AssessmentGradingData("
				+ " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
				+ " a.submittedDate, a.isLate,"
				+ " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
				+ " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
				+ " a.timeElapsed) "
				+ " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
				+ " where a.publishedAssessmentId = p.publishedAssessmentId"
				+ " and a.forGrade=:forGrade and a.agentId=:agentId"
				+ " and az.agentIdString=:siteId "
				+ " and az.functionId=:functionId and az.qualifierId=p.publishedAssessmentId"
				+ " order by p.publishedAssessmentId DESC, a.submittedDate DESC";

			final HibernateCallback<List<AssessmentGradingData>> hcb_last = session -> {
                Query q = session.createQuery(hql + order_last);
                q.setBoolean("forGrade", true);
                q.setString("agentId", agentId);
                q.setString("siteId", siteId);
                q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
                return q.list();
            };

			// this list is sorted by submittedDate desc.
			last_list = getHibernateTemplate().execute(hcb_last);

			final HibernateCallback<List<AssessmentGradingData>> hcb_highest = session -> {
                Query q = session.createQuery(hql + order_highest);
                q.setBoolean("forGrade", true);
                q.setString("agentId", agentId);
                q.setString("siteId", siteId);
                q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
                return q.list();
            };

			// this list is sorted by finalScore desc.
			highest_list = getHibernateTemplate().execute(hcb_highest);
		}
		
		//getEvaluationModel();
		final String query = "select e.assessment.publishedAssessmentId, e.scoringType, ac.submissionsAllowed  " +
		"from PublishedEvaluationModel e, PublishedAccessControl ac, AuthorizationData az " +
		"where e.assessment.publishedAssessmentId = ac.assessment.publishedAssessmentId " +
		"and az.qualifierId = ac.assessment.publishedAssessmentId and az.agentIdString in (:agentIdString) and az.functionId=:functionId";

		groupIds.add(siteId);
		
		final HibernateCallback<List<Object[]>> eval_model = session -> {
            Query q = session.createQuery(query);
            q.setParameterList("agentIdString", groupIds);
            q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
            return q.list();
        };
			
		List<Object[]> l = getHibernateTemplate().execute(eval_model);
		Map<Long, Integer> scoringTypeMap = new HashMap<>();
		for	(Object o[] : l) {
			scoringTypeMap.put((Long) o[0], (Integer) o[1]);
		}
		
		// The sorting for each column will be done in the action listener.
		List<AssessmentGradingData> assessmentList = new ArrayList<>();
		Long currentid = new Long("0");
		Integer scoringOption;

		// now go through the last_list, and get the first entry in the list for
		// each publishedAssessment, if
		// not

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

		// now go through the highest_list ,and get the first entry in the list
		// for each publishedAssessment.

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
	}
	  
	public PublishedAssessmentData getBasicInfoOfPublishedAssessment(final Long publishedId) {
		final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> session.createQuery(
					"select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
							+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, "
							+ " c.feedbackDate, f.feedbackDelivery, f.feedbackComponentOption, f.feedbackAuthoring, c.lateHandling, "
							+ " c.unlimitedSubmissions, c.submissionsAllowed) "
							+ " from PublishedAssessmentData as p, PublishedAccessControl as c,"
							+ " PublishedFeedback as f"
							+ " where c.assessment.publishedAssessmentId=p.publishedAssessmentId "
							+ " and p.publishedAssessmentId = f.assessment.publishedAssessmentId "
							+ " and p.publishedAssessmentId = :id")
				.setLong("id", publishedId.longValue())
				.list();
		List<PublishedAssessmentData> list = getHibernateTemplate().execute(hcb);
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public String getPublishedAssessmentSiteId(String publishedAssessmentId) {
		HibernateCallback<List<AuthorizationData>> hcb = session -> session
				.createQuery("select a from AuthorizationData a where a.functionId = :fid and a.qualifierId = :id")
				.setString("fid", "TAKE_PUBLISHED_ASSESSMENT")
				.setString("id", publishedAssessmentId)
				.list();
		List<AuthorizationData> l = getHibernateTemplate().execute(hcb);

		PublishedAssessmentData publishedAssessment = loadPublishedAssessment(Long.valueOf(publishedAssessmentId));
		boolean releaseToGroups = AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo());
		for (AuthorizationData a : l) {
			if (releaseToGroups) {
				String agentId = a.getAgentIdString();
				if (siteService.findGroup(agentId) != null && siteService.findGroup(agentId).getContainingSite() != null) {
					return siteService.findGroup(a.getAgentIdString()).getContainingSite().getId();
				}
			}
			return a.getAgentIdString();
		}
		return "";
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
		final HibernateCallback<List<String>> hcb = session -> session
				.createQuery("select m.entry from PublishedSectionData s, PublishedAssessmentData p, PublishedSectionMetaData m " +
						"where p.publishedAssessmentId=:publishedAssessmentId and m.label=:metaDataLabel and p = s.assessment and m.section = s")
				.setLong("publishedAssessmentId", publishedAssessmentId)
				.setString("metaDataLabel", SectionDataIfc.NUM_QUESTIONS_DRAWN)
				.list();
		List<String> list = getHibernateTemplate().execute(hcb);
		
		int sum = 0;
		for (String entry : list) {
			if (entry != null) {
				sum += Integer.valueOf(entry);
			}
		}
		return sum;
	}

	/**
	 * @param publishedAssessmentId
	 * @return
	 */
	public Integer getPublishedItemCountForNonRandomSections(final Long publishedAssessmentId) {
		final HibernateCallback<List<Number>> hcb = session -> session
				.createQuery("select count(i) from PublishedItemData i, PublishedSectionData s, PublishedAssessmentData p, PublishedSectionMetaData m " +
						"where p.publishedAssessmentId=:publishedAssessmentId and m.label=:metaDataLabel " +
						"and p = s.assessment and i.section = s and m.section = s and m.entry=:metaDataEntry ")
				.setLong("publishedAssessmentId", publishedAssessmentId)
				.setString("metaDataLabel", SectionDataIfc.AUTHOR_TYPE)
				.setString("metaDataEntry", SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString())
				.list();
		List<Number> list = getHibernateTemplate().execute(hcb);
		return list.get(0).intValue();
	}
	
	public Integer getPublishedSectionCount(final Long publishedAssessmentId) {
		final HibernateCallback<List<Number>> hcb = session -> session
				.createQuery("select count(s) from PublishedSectionData s, PublishedAssessmentData p " +
						"where p.publishedAssessmentId = :id and p = s.assessment")
				.setLong("id", publishedAssessmentId)
				.list();
		List<Number> list = getHibernateTemplate().execute(hcb);
		return list.get(0).intValue();
	}
		
	public PublishedAttachmentData getPublishedAttachmentData(Long attachmentId) {
		final HibernateCallback<List<PublishedAttachmentData>> hcb = session -> session
				.createQuery("select a from PublishedAttachmentData a where a.attachmentId = :id")
				.setLong("id", attachmentId)
				.list();
		List<PublishedAttachmentData> l = getHibernateTemplate().execute(hcb);
		if (!l.isEmpty()) {
			return l.get(0);
		}
		return null;
	}

	public void updateAssessmentLastModifiedInfo(PublishedAssessmentFacade publishedAssessmentFacade) {
		AssessmentBaseIfc data = publishedAssessmentFacade.getData();
		data.setLastModifiedBy(AgentFacade.getAgentString());
		data.setLastModifiedDate(new Date());
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().update(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem update assessment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public void saveOrUpdateSection(SectionFacade section) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(section.getData());
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update section: " + e.getMessage());
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
				getHibernateTemplate().saveOrUpdate(section);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update assessment: {}", e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
		return new PublishedSectionFacade(section);
	}

	public PublishedSectionFacade getSection(Long sectionId) {
		PublishedSectionData publishedSection = getHibernateTemplate().load(PublishedSectionData.class, sectionId);
		return new PublishedSectionFacade(publishedSection);
	}

	public AssessmentAccessControlIfc loadPublishedAccessControl(Long publishedAssessmentId) {
		final HibernateCallback<List<PublishedAccessControl>> hcb = session -> session
				.createQuery("select c from PublishedAssessmentData as p, PublishedAccessControl as c " +
								"where c.assessment.publishedAssessmentId=p.publishedAssessmentId " +
								"and p.publishedAssessmentId = :id")
				.setLong("id", publishedAssessmentId)
				.list();
		List<PublishedAccessControl> list = getHibernateTemplate().execute(hcb);

		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public void saveOrUpdatePublishedAccessControl(AssessmentAccessControlIfc publishedAccessControl) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(publishedAccessControl);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update publishedAccessControl data: {}", e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
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
		String currentUserId = UserDirectoryService.getCurrentUser().getId();
		return getSiteGroupIdsForSubmittingAgent(currentUserId, siteId);
	}
		
	/**
	 * 
	 * @param assessmentId
	 * @return
	 */
	private String getReleaseToGroupsAsString(Map groupsForSite, Long assessmentId) {
		 List releaseToGroups = new ArrayList();
		 String releaseToGroupsAsString = null;
	     AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
		 List authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", assessmentId.toString());
		 if (authorizations != null && authorizations.size()>0) {
			 Iterator authsIter = authorizations.iterator();
			 while (authsIter.hasNext()) {
				 AuthorizationData ad = (AuthorizationData) authsIter.next();
				 Object group = groupsForSite.get(ad.getAgentIdString());
				 if (group != null) {
					 releaseToGroups.add(group);
				 }
			 }			 
			 Collections.sort(releaseToGroups);
			 StringBuilder releaseToGroupsAsStringbuf = new StringBuilder();
			  
			  if (releaseToGroups != null && releaseToGroups.size()!=0 ) {
				 String lastGroup = (String) releaseToGroups.get(releaseToGroups.size()-1);
				 Iterator releaseToGroupsIter = releaseToGroups.iterator();
				 while (releaseToGroupsIter.hasNext()) {
					 String group = (String) releaseToGroupsIter.next();
					 //releaseToGroupsAsString += group;
					 releaseToGroupsAsStringbuf.append(group);
					 if (!group.equals(lastGroup) ) {
						 //releaseToGroupsAsString += ", ";
						 releaseToGroupsAsStringbuf.append(", ");

					 }
				 }
			 }
			 releaseToGroupsAsString = releaseToGroupsAsStringbuf.toString();
		 }
		 
		 return releaseToGroupsAsString;
	}
	
	/**
	 * added by Sam Ottenhoff Feb 2010
	 * Returns all groups for site
	 * @param siteId
	 * @return
	 */
	private Map getGroupsForSite(String siteId){
		Map sortedGroups = new TreeMap();
		Site site;
		try {
			site = siteService.getSite(siteId);
			Collection groups = site.getGroups();
			if (groups != null && groups.size() > 0) {
				Iterator groupIter = groups.iterator();
				while (groupIter.hasNext()) {
					Group group = (Group) groupIter.next();
					sortedGroups.put(group.getId(), group.getTitle());
				}
			}
		}
		catch (IdUnusedException ex) {
			// No site available
		}
		return sortedGroups;
	}

	/**
	 * Returns all groups for site
	 */
	public Map getGroupsForSite() {
		String siteId = ToolManager.getCurrentPlacement().getContext();
		return getGroupsForSite(siteId);
	}

	public List<String> getReleaseToGroupIdsForPublishedAssessment(final String publishedAssessmentId) {
		final HibernateCallback<List<String>> hcb = session -> session
				.createQuery(
						"select agentIdString from AuthorizationData az where az.functionId=:functionId and az.qualifierId=:publishedAssessmentId")
				.setString("publishedAssessmentId", publishedAssessmentId)
				.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT")
				.list();
		return getHibernateTemplate().execute(hcb);
	}

	public Integer getPublishedAssessmentStatus(Long publishedAssessmentId) {
		final HibernateCallback<List<Integer>> hcb = session -> session
				.createQuery("select p.status from PublishedAssessmentData p where p.publishedAssessmentId = :id")
				.setLong("id", publishedAssessmentId)
				.list();
		List<Integer> l = getHibernateTemplate().execute(hcb);
		if (!l.isEmpty()) {
			return l.get(0);
		}
		return AssessmentBaseIfc.DEAD_STATUS;
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
		PublishedAssessmentAttachment assessmentAttachment = getHibernateTemplate().load(PublishedAssessmentAttachment.class, assessmentAttachmentId);
		AssessmentIfc assessment = assessmentAttachment.getAssessment();
		// String resourceId = assessmentAttachment.getResourceId();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				if (assessment != null) { // need to dissociate with
					// assessment before deleting in
					// Hibernate 3
					Set set = assessment.getAssessmentAttachmentSet();
					set.remove(assessmentAttachment);
					getHibernateTemplate().delete(assessmentAttachment);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem delete publishedAssessmentAttachment: {}", e.getMessage());
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
		PublishedSectionAttachment sectionAttachment = getHibernateTemplate().load(PublishedSectionAttachment.class, sectionAttachmentId);
		SectionDataIfc section = sectionAttachment.getSection();
		// String resourceId = sectionAttachment.getResourceId();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				if (section != null) { // need to dissociate with section
					// before deleting in Hibernate 3
					Set set = section.getSectionAttachmentSet();
					set.remove(sectionAttachment);
					getHibernateTemplate().delete(sectionAttachment);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem delete sectionAttachment: {}", e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}
	
	public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
	    for (AttachmentIfc attachment : list) {
	        getHibernateTemplate().saveOrUpdate(attachment);
        }
	}
	
	  public PublishedAssessmentFacade getPublishedAssessmentInfoForRemove(Long publishedAssessmentId) {
		  PublishedAssessmentData a = getHibernateTemplate().load(PublishedAssessmentData.class, publishedAssessmentId);
		  PublishedAssessmentFacade f = new PublishedAssessmentFacade(a.getAssessmentId(), a.getTitle(), a.getCreatedBy());
		  return f;
	  }  

	  public Map<Long, String> getToGradebookPublishedAssessmentSiteIdMap() {
		  final HibernateCallback<List<Object[]>> hcb = session -> session
				  .createQuery("select em.assessment.publishedAssessmentId, a.agentIdString " +
						  "from PublishedEvaluationModel em, AuthorizationData a " +
						  "where a.functionId = 'OWN_PUBLISHED_ASSESSMENT' " +
						  "and em.assessment.publishedAssessmentId = a.qualifierId " +
						  "and em.toGradeBook = '1'")
				  .list();

		  List<Object[]> l = getHibernateTemplate().execute(hcb);
		  Map<Long, String> map = new HashMap<>();
		  for (Object[] o : l) {
			  map.put((Long) o[0], (String) o[1]);
		  }
		  return map;
	  }	  
	  
	public List<AssessmentGradingData> getAllAssessmentsGradingDataByAgentAndSiteId(final String agentId, final String siteId) {
		final HibernateCallback<List<AssessmentGradingData>> hcb = session -> session.createQuery(
            		"select a " + " from AssessmentGradingData as a, AuthorizationData as az " +
							"where a.agentId=:agentId and a.forGrade=:forGrade " +
							"and az.agentIdString=:siteId " +
							"and az.functionId=:functionId and az.qualifierId=a.publishedAssessmentId")
				.setString("agentId", agentId)
				.setBoolean("forGrade", true)
				.setString("siteId", siteId)
				.setString("functionId", "OWN_PUBLISHED_ASSESSMENT")
				.list();
		return getHibernateTemplate().execute(hcb);
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
}
