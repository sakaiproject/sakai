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

import java.sql.SQLException;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
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
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PublishedAssessmentFacadeQueries extends HibernateDaoSupport
		implements PublishedAssessmentFacadeQueriesAPI {

	private Log log = LogFactory
			.getLog(PublishedAssessmentFacadeQueries.class);

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
						.getRetryAllowed(), a.getLateHandling(), a
						.getStartDate(), a.getDueDate(), a.getScoreDate(), a
						.getFeedbackDate());
		publishedAccessControl.setRetractDate(a.getRetractDate());
		publishedAccessControl.setAutoSubmit(a.getAutoSubmit());
		publishedAccessControl.setItemNavigation(a.getItemNavigation());
		publishedAccessControl.setItemNumbering(a.getItemNumbering());
		publishedAccessControl.setSubmissionMessage(a.getSubmissionMessage());
		publishedAccessControl.setReleaseTo(a.getReleaseTo());
		publishedAccessControl.setUsername(a.getUsername());
		publishedAccessControl.setPassword(a.getPassword());
		publishedAccessControl.setFinalPageUrl(a.getFinalPageUrl());
		publishedAccessControl.setUnlimitedSubmissions(a
				.getUnlimitedSubmissions());
		publishedAccessControl.setMarkForReview(a.getMarkForReview());
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
							.getTypeId(), item.getGrade(), item.getScore(), item.getDiscount(),
					item.getHint(), item.getHasRationale(), item.getStatus(),
					item.getCreatedBy(), item.getCreatedDate(), item
							.getLastModifiedBy(), item.getLastModifiedDate(),
					null, null, null, // set ItemTextSet, itemMetaDataSet and
					// itemFeedbackSet later
					item.getTriesAllowed(), item.getPartialCreditFlag());
			Set publishedItemTextSet = preparePublishedItemTextSet(
					publishedItem, item.getItemTextSet(), protocol);
			Set publishedItemMetaDataSet = preparePublishedItemMetaDataSet(
					publishedItem, item.getItemMetaDataSet());
			Set publishedItemFeedbackSet = preparePublishedItemFeedbackSet(
					publishedItem, item.getItemFeedbackSet());
			Set publishedItemAttachmentSet = preparePublishedItemAttachmentSet(
					publishedItem, item.getItemAttachmentSet(), protocol);
			publishedItem.setItemTextSet(publishedItemTextSet);
			publishedItem.setItemMetaDataSet(publishedItemMetaDataSet);
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

	public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId) {
		return getPublishedAssessment(assessmentId, true);
	}

	public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId, boolean withGroupsInfo) {
		PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
		a.setSectionSet(getSectionSetForAssessment(a));
		String releaseToGroups = "";
		if (withGroupsInfo) {
			//TreeMap groupsForSite = getGroupsForSite();
			
			// SAM-799
            String siteId = getPublishedAssessmentSiteId(assessmentId.toString());
            TreeMap groupsForSite = getGroupsForSite(siteId);
             
			releaseToGroups = getReleaseToGroupsAsString(groupsForSite, assessmentId);
		}
		
		PublishedAssessmentFacade f = new PublishedAssessmentFacade(a, releaseToGroups);
		return f;
	}
	
	public Long getPublishedAssessmentId(Long assessmentId) {
		List list = getHibernateTemplate()
				.find(
						"from PublishedAssessmentData as p where p.assessmentId=? order by p.createdDate desc",
						assessmentId);
		Long publishedId =  Long.valueOf(0);
		if (!list.isEmpty()) {
			PublishedAssessmentData f = (PublishedAssessmentData) list.get(0);
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
					gbsHelper.addToGradebook(publishedAssessment, g);
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

	public ArrayList getAllTakeableAssessments(String orderBy,
			boolean ascending, final Integer status) {

		String query = "from PublishedAssessmentData as p where p.status=? order by p."
				+ orderBy;

		if (ascending) {
			query += " asc";
		} else {
			query += " desc";
		}
		log.debug("Order by " + orderBy);

		final String hql = query;
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setInteger(0, status.intValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query, new Object[] {status}
		// ,
		// new org.hibernate.type.Type[] {
		// Hibernate.
		// INTEGER});
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			PublishedAssessmentData a = (PublishedAssessmentData) list.get(i);
			log.debug("Title: " + a.getTitle());
			// Don't need sections for list of assessments
			// a.setSectionSet(getSectionSetForAssessment(a));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public Integer getNumberOfSubmissions(final String publishedAssessmentId,
			final String agentId) {
		final String query = "select count(a) from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=?";
		// Object[] objects = new Object[3];
		// objects[0] = new Long(publishedAssessmentId);
		// objects[1] = agentId;
		// objects[2] = new Boolean(true);
		// Type[] types = new Type[3];
		// types[0] = Hibernate.LONG;
		// types[1] = Hibernate.STRING;
		// types[2] = Hibernate.BOOLEAN;

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, Long.parseLong(publishedAssessmentId));
				q.setString(1, agentId);
				q.setBoolean(2, true);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query, objects, types);
		return (Integer) list.get(0);
	}

	public List getNumberOfSubmissionsOfAllAssessmentsByAgent(
			final String agentId) {
		final String query = "select new AssessmentGradingData("
				+ " a.publishedAssessmentId, count(a)) "
				+ " from AssessmentGradingData as a where a.agentId=? and a.forGrade=?"
				+ " group by a.publishedAssessmentId";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setString(0, agentId);
				q.setBoolean(1, true);
				return q.list();
			};
		};
		return getHibernateTemplate().executeFind(hcb);
	}

	public List getNumberOfSubmissionsOfAllAssessmentsByAgent(
			final String agentId, final String siteId) {

		final ArrayList groupIds = getSiteGroupIdsForSubmittingAgent(agentId, siteId);

		if (groupIds.size() > 0) {
			final String query = "select new AssessmentGradingData("
				+ " a.publishedAssessmentId, count(a)) "
				+ " from AssessmentGradingData as a, AuthorizationData as az "
				+ " where a.agentId=:agentId and a.forGrade=:forGrade "
				+ " and (az.agentIdString=:siteId or az.agentIdString in (:groupIds)) "
				+ " and az.functionId=:functionId and az.qualifierId=a.publishedAssessmentId"
				+ " group by a.publishedAssessmentId";

			final HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
				throws HibernateException, SQLException {
					Query q = session.createQuery(query);
					q.setString("agentId", agentId);
					q.setBoolean("forGrade", true);
					q.setString("siteId", siteId);
					q.setParameterList("groupIds", groupIds);
					q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
					return q.list();
				};
			};
			return getHibernateTemplate().executeFind(hcb);
		}
		else {
			final String query = "select new AssessmentGradingData("
				+ " a.publishedAssessmentId, count(a)) "
				+ " from AssessmentGradingData as a, AuthorizationData as az "
				+ " where a.agentId=:agentId and a.forGrade=:forGrade "
				+ " and az.agentIdString=:siteId "
				+ " and az.functionId=:functionId and az.qualifierId=a.publishedAssessmentId"
				+ " group by a.publishedAssessmentId";

			final HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
				throws HibernateException, SQLException {
					Query q = session.createQuery(query);
					q.setString("agentId", agentId);
					q.setBoolean("forGrade", true);
					q.setString("siteId", siteId);
					q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
					return q.list();
				};
			};
			return getHibernateTemplate().executeFind(hcb);
		}
	}

	public ArrayList getAllPublishedAssessments(String sortString) {
		String orderBy = getOrderBy(sortString);
		List list = getHibernateTemplate().find(
				"from PublishedAssessmentData p order by p." + orderBy);
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			PublishedAssessmentData a = (PublishedAssessmentData) list.get(i);
			a.setSectionSet(getSectionSetForAssessment(a));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getAllPublishedAssessments(String sortString,
			final Integer status) {
		final String orderBy = getOrderBy(sortString);

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("from PublishedAssessmentData as p where p.status=? order by p."
								+ orderBy);
				q.setInteger(0, status.intValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(
		// "from PublishedAssessmentData as p where p.status=? order by p." +
		// orderBy,
		// new Object[] {status}
		// , new org.hibernate.type.Type[] {Hibernate.INTEGER});
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			PublishedAssessmentData a = (PublishedAssessmentData) list.get(i);
			a.setSectionSet(getSectionSetForAssessment(a));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getAllPublishedAssessments(int pageSize, int pageNumber,
			String sortString, Integer status) {
		String orderBy = getOrderBy(sortString);
		String queryString = "from PublishedAssessmentData p order by p."
				+ orderBy;
		if (!status.equals(PublishedAssessmentFacade.ANY_STATUS)) {
			queryString = "from PublishedAssessmentData p where p.status = ? "
					+ " order by p." + orderBy;
		}
		PagingUtilQueriesAPI pagingUtilQueries = PersistenceService
				.getInstance().getPagingUtilQueries();
		List pageList = pagingUtilQueries.getAll(pageSize, pageNumber,
				queryString, status);
		log.debug("**** pageList=" + pageList);
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < pageList.size(); i++) {
			PublishedAssessmentData a = (PublishedAssessmentData) pageList
					.get(i);
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				Long assessmentId = assessment.getPublishedAssessmentId();
				List ip = getHibernateTemplate()
						.find(
								"from PublishedSecuredIPAddress s where s.assessment.publishedAssessmentId=?",
								assessmentId);
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
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}
	
	public void saveOrUpdate(PublishedAssessmentIfc assessment)
			throws Exception {
		PublishedAssessmentData data;
		if (assessment instanceof PublishedAssessmentFacade)
			data = (PublishedAssessmentData) ((PublishedAssessmentFacade) assessment)
					.getData();
		else
			data = (PublishedAssessmentData) assessment;

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(data);
				retryCount = 0;
			} catch (Exception e) {
				log
						.warn("problem save or update assessment: "
								+ e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
				if (retryCount == 0)
					throw e;
			}
		}
	}

	public ArrayList getBasicInfoOfAllActivePublishedAssessments(
			String sortString, final String siteAgentId, boolean ascending) {
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
		if (ascending == true)
			query += " asc";
		else
			query += " desc";

		final String hql = query;
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setInteger("status", 1);
				q.setString("functionId", "OWN_PUBLISHED_ASSESSMENT");
				q.setString("siteId", siteAgentId);
				//q.setParameterList("groupIds", groupIds);
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);

		
		// List l = getHibernateTemplate().find(query,
		// new Object[] {siteAgentId},
		// new org.hibernate.type.Type[] {Hibernate.STRING});

		// we will filter the one that is past duedate & retract date
		ArrayList list = new ArrayList();
		for (int j = 0; j < l.size(); j++) {
			PublishedAssessmentData p = (PublishedAssessmentData) l.get(j);
			if ((p.getDueDate() == null || (p.getDueDate()).after(currentDate))
					&& (p.getRetractDate() == null || (p.getRetractDate())
							.after(currentDate))) {
				list.add(p);
			}
		}

		ArrayList pubList = new ArrayList();
		TreeMap groupsForSite = null;
		String releaseToGroups;
		String lastModifiedBy = "";
		AgentFacade agent = null;

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
	public ArrayList getBasicInfoOfAllInActivePublishedAssessments(
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
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setInteger("activeStatus", 1);
				q.setTimestamp("today", new Date());
				q.setInteger("editStatus", 3);
				q.setString("functionId", "OWN_PUBLISHED_ASSESSMENT");
				q.setString("siteId", siteAgentId);
				//q.setParameterList("groupIds", groupIds);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		
		
		// List list = getHibernateTemplate().find(query,
		// new Object[] {new Date(), new Date(),siteAgentId} ,
		// new org.hibernate.type.Type[] {Hibernate.TIMESTAMP,
		// Hibernate.TIMESTAMP,
		// Hibernate.STRING});

		ArrayList pubList = new ArrayList();
		TreeMap groupsForSite = null;
		String releaseToGroups;
		String lastModifiedBy = "";
		AgentFacade agent = null;
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

	/**
	 * return a set of PublishedSectionData IMPORTANT: 1. we have declared
	 * SectionData as lazy loading, so we need to initialize it using
	 * getHibernateTemplate().initialize(java.lang.Object). Unfortunately, we
	 * are using Spring 1.0.2 which does not support this Hibernate feature. I
	 * tried upgrading Spring to 1.1.3. Then it failed to load all the OR maps
	 * correctly. So for now, I am just going to initialize it myself. I will
	 * take a look at it again next year. - daisyf (12/13/04)
	 */
	public HashSet getSectionSetForAssessment(PublishedAssessmentIfc assessment) {
		List sectionList = getHibernateTemplate().find(
				"from PublishedSectionData s where s.assessment.publishedAssessmentId=? ", 
				assessment.getPublishedAssessmentId());
		HashSet set = new HashSet();
		for (int j = 0; j < sectionList.size(); j++) {
			set.add((PublishedSectionData) sectionList.get(j));
		}
		return set;
	}

	// IMPORTANT:
	// 1. we do not want any Section info, so set loadSection to false
	// 2. We have also declared SectionData as lazy loading. If loadSection is
	// set
	// to true, we will see null pointer
	public PublishedAssessmentFacade getSettingsOfPublishedAssessment(
			Long assessmentId) {
		PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
		Boolean loadSection = Boolean.FALSE;
		PublishedAssessmentFacade f = new PublishedAssessmentFacade(a,
				loadSection);
		return f;
	}

	public PublishedItemData loadPublishedItem(Long itemId) {
		return (PublishedItemData) getHibernateTemplate().load(
				PublishedItemData.class, itemId);
	}

	public PublishedItemText loadPublishedItemText(Long itemTextId) {
		return (PublishedItemText) getHibernateTemplate().load(
				PublishedItemText.class, itemTextId);
	}

	
	// added by daisy - please check the logic - I based this on the
	// getBasicInfoOfAllActiveAssessment
	// to include release to selected groups
	/**
	 * 
	 * @param orderBy
	 * @param ascending
	 * @param status
	 * @param siteId
	 * @return
	 */
	public ArrayList getBasicInfoOfAllPublishedAssessments(String orderBy,
			boolean ascending, final String siteId) {

		final ArrayList groupIds = getSiteGroupIdsForCurrentUser(siteId);
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
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
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
		};
		List list = getHibernateTemplate().executeFind(hcb);
		ArrayList pubList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			PublishedAssessmentData p = (PublishedAssessmentData) list.get(i);
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(p
					.getPublishedAssessmentId(), p.getTitle(),
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
	public ArrayList getBasicInfoOfAllPublishedAssessments2(
			String sortString, boolean ascending, final String siteAgentId) {
		String orderBy = getOrderBy(sortString);
		
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, p.status, p.lastModifiedDate, p.lastModifiedBy, "
				+ "c.lateHandling, c.unlimitedSubmissions, c.submissionsAllowed) "
				+ " from PublishedAssessmentData p, PublishedAccessControl c, AuthorizationData z  "
				+ " where c.assessment.publishedAssessmentId = p.publishedAssessmentId "
				+ " and p.publishedAssessmentId=z.qualifierId and z.functionId=:functionId "
				+ " and z.agentIdString=:siteId and (p.status=:activeStatus or p.status=:editStatus) "
				+ " order by p." + orderBy;
		if (ascending == true)
			query += " asc";
		else
			query += " desc";

		final String hql = query;
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setString("functionId", "OWN_PUBLISHED_ASSESSMENT");
				q.setString("siteId", siteAgentId);
				q.setInteger("activeStatus", 1);
				q.setInteger("editStatus", 3);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		ArrayList pubList = new ArrayList();
		TreeMap groupsForSite = null;
		String releaseToGroups;
		String lastModifiedBy = "";
		AgentFacade agent = null;

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
	public ArrayList getBasicInfoOfLastSubmittedAssessments(
			final String agentId, String orderBy, boolean ascending) {
		// 1. get total no. of submission per assessment by the given agent
		// HashMap h = getTotalSubmissionPerAssessment(agentId);
		final String query = "select new AssessmentGradingData("
				+ " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
				+ " a.submittedDate, a.isLate,"
				+ " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
				+ " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
				+ " a.timeElapsed) "
				+ " from AssessmentGradingData a, PublishedAssessmentData p"
				+ " where a.publishedAssessmentId = p.publishedAssessmentId  and a.forGrade=? and a.agentId=?"
				+ " order by p.publishedAssessmentId DESC, a.submittedDate DESC";

		/*
		 * The sorting for each type will be done in the action listener. if
		 * (orderBy.equals(TITLE)) { query += ", p." + orderBy; } else if
		 * (!orderBy.equals(SUBMITTED)) { query += ", a." + orderBy; } if
		 * (!orderBy.equals(SUBMITTED)) { if (ascending == false) { query += "
		 * desc"; } else { query += " asc"; } }
		 */

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setBoolean(0, true);
				q.setString(1, agentId);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// ArrayList list = (ArrayList) getHibernateTemplate().find(query,
		// new Object[] {agentId}
		// ,
		// new org.hibernate.type.Type[] {Hibernate.STRING});

		ArrayList assessmentList = new ArrayList();
		Long current = new Long("0");
		// Date currentDate = new Date();
		for (int i = 0; i < list.size(); i++) {
			AssessmentGradingData a = (AssessmentGradingData) list.get(i);
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
	public HashMap getTotalSubmissionPerAssessment(String agentId) {
		List l = getNumberOfSubmissionsOfAllAssessmentsByAgent(agentId);
		HashMap h = new HashMap();
		for (int i = 0; i < l.size(); i++) {
			AssessmentGradingData d = (AssessmentGradingData) l.get(i);
			h.put(d.getPublishedAssessmentId(),  Integer.valueOf(d
					.getTotalSubmitted()));
			log.debug("pId=" + d.getPublishedAssessmentId() + " submitted="
					+ d.getTotalSubmitted());
		}
		return h;
	}

	public HashMap getTotalSubmissionPerAssessment(String agentId, String siteId) {
		List l = getNumberOfSubmissionsOfAllAssessmentsByAgent(agentId, siteId);
		HashMap h = new HashMap();
		for (int i = 0; i < l.size(); i++) {
			AssessmentGradingData d = (AssessmentGradingData) l.get(i);
			h.put(d.getPublishedAssessmentId(),  Integer.valueOf(d
					.getTotalSubmitted()));
			log.debug("pId=" + d.getPublishedAssessmentId() + " submitted="
					+ d.getTotalSubmitted());
		}
		return h;
	}

	public Integer getTotalSubmission(final String agentId,
			final Long publishedAssessmentId) {
		final String query = "select count(a) from AssessmentGradingData a where a.forGrade=? "
				+ " and a.agentId=? and a.publishedAssessmentId=?";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setBoolean(0, true);
				q.setString(1, agentId);
				q.setLong(2, publishedAssessmentId.longValue());
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);

		// List l = getHibernateTemplate().find(query,
		// new Object[] {agentId,
		// publishedAssessmentId}
		// ,
		// new org.hibernate.type.Type[] {
		// Hibernate.STRING, Hibernate.LONG});
		return (Integer) l.get(0);
	}

	public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(
			String alias) {
		return getPublishedAssessmentIdByMetaLabel("ALIAS", alias);
	}

	public PublishedAssessmentFacade getPublishedAssessmentIdByMetaLabel(
			final String label, final String entry) {
		final String query = "select p " + " from PublishedAssessmentData p, "
				+ " PublishedMetaData m where p=m.assessment "
				+ " and m.label=? and m.entry=?";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setString(0, label);
				q.setString(1, entry);
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);

		// List l = getHibernateTemplate().find(query,
		// new Object[] {label, entry}
		// ,
		// new org.hibernate.type.Type[] {
		// Hibernate.STRING, Hibernate.STRING});
		if (l.size() > 0) {
			PublishedAssessmentData p = (PublishedAssessmentData) l.get(0);
			p.setSectionSet(getSectionSetForAssessment(p));
			PublishedAssessmentFacade f = new PublishedAssessmentFacade(p);
			f.setFeedbackComponentOption(p.getAssessmentFeedback().getFeedbackComponentOption());
			return f;
		} else {
			return null;
		}
	}

	public void saveOrUpdateMetaData(PublishedMetaData meta) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(meta);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update meta data: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public HashMap getFeedbackHash() {
		final List listAgentId = new ArrayList();
		String siteId = AgentFacade.getCurrentSiteId();
		listAgentId.add(siteId);

		try {
			Site site = SiteService.getSite(siteId);
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

		HashMap h = new HashMap();
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
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setParameterList("agentIdString", listAgentId);
				q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
				return q.list();
			};
		};
		
		List l = getHibernateTemplate().executeFind(hcb);
		for (int i = 0; i < l.size(); i++) {
			PublishedFeedback f = (PublishedFeedback) l.get(i);
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
	public HashMap getAllAssessmentsReleasedToAuthenticatedUsers() {
		HashMap h = new HashMap();
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate) "
				+ " from PublishedAssessmentData p, PublishedAccessControl c  "
				+ " where c.assessment = p and c.releaseTo like '%Authenticated Users%'";
		List l = getHibernateTemplate().find(query);
		for (int i = 0; i < l.size(); i++) {
			PublishedAssessmentData p = (PublishedAssessmentData) l.get(i);
			h.put(p.getPublishedAssessmentId(),
					new PublishedAssessmentFacade(p));
		}
		return h;
	}

	public String getPublishedAssessmentOwner(String publishedAssessmentId) {
		// HashMap h = new HashMap();
		String query = "select a from AuthorizationData a where "
				+ " a.functionId=? and a.qualifierId=? ";
		Object [] values = {"OWN_PUBLISHED_ASSESSMENT", publishedAssessmentId};
	    List l = getHibernateTemplate().find(query, values);
		if (l.size() > 0) {
			AuthorizationData a = (AuthorizationData) l.get(0);
			return a.getAgentIdString();
		} else
			return null;
	}

	public boolean publishedAssessmentTitleIsUnique(
			final Long assessmentBaseId, final String title) {
		final String currentSiteId = AgentFacade.getCurrentSiteId();
		// String agentString = AgentFacade.getAgentString();
		// List list;
		boolean isUnique = true;
		final String query = "select new PublishedAssessmentData(a.publishedAssessmentId, a.title, a.lastModifiedDate)"
				+ " from PublishedAssessmentData a, AuthorizationData z where "
				+ " a.title=? and a.publishedAssessmentId!=? and a.status!=? and "
				+ " z.functionId=? and "
				+ " a.publishedAssessmentId=z.qualifierId and z.agentIdString=?";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setString(0, title);
				q.setLong(1, assessmentBaseId.longValue());
				q.setInteger(2, 2);
				q.setString(3, "OWN_PUBLISHED_ASSESSMENT");
				q.setString(4, currentSiteId);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[]{title,assessmentBaseId,currentSiteId},
		// new org.hibernate.type.Type[] {Hibernate.STRING, Hibernate.LONG,
		// Hibernate.STRING});
		if (list.size() > 0)
			isUnique = false;
		return isUnique;
	}

	public boolean hasRandomPart(final Long publishedAssessmentId) {
		boolean hasRandomPart = false;
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL
				.toString();
		final String query = "select s from PublishedSectionData s, PublishedSectionMetaData m where "
				+ " s = m.section and s.assessment.publishedAssessmentId=? and "
				+ " m.label=? and m.entry=?";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, publishedAssessmentId.longValue());
				q.setString(1, key);
				q.setString(2, value);
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);

		// List l = getHibernateTemplate().find(query,
		// new Object[]{ publishedAssessmentId, key, value},
		// new org.hibernate.type.Type[] {Hibernate.LONG, Hibernate.STRING,
		// Hibernate.STRING});
		if (l.size() > 0)
			hasRandomPart = true;
		return hasRandomPart;
	}
	
	public List getContainRandomPartAssessmentIds(final Collection assessmentIds) {
        if (assessmentIds == null || assessmentIds.size() < 1)
		    return new ArrayList<Long>();	
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL
				.toString();
		final String query = "select s.assessment.publishedAssessmentId "
				+ "from PublishedSectionData s, PublishedSectionMetaData m " 
				+ "where s.assessment.publishedAssessmentId in (:ids) and s = m.section and m.label=:label and m.entry=:entry " 
				+ "group by s.assessment.publishedAssessmentId";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setString("label", key);
				q.setString("entry", value);
				q.setParameterList("ids", assessmentIds);
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);
		return l;
	}

	public PublishedItemData getFirstPublishedItem(
			final Long publishedAssessmentId) {
		final String query = "select i from PublishedAssessmentData p, PublishedSectionData s, "
				+ " PublishedItemData i where p.publishedAssessmentId=? and"
				+ " p.publishedAssessmentId=s.assessment.publishedAssessmentId and "
				+ " s=i.section";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, publishedAssessmentId.longValue());
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);

		//final String key = SectionDataIfc.AUTHOR_TYPE;
		//final String value = SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString();
		final String query2 = "select s from PublishedAssessmentData p, PublishedSectionData s "
				+ " where p.publishedAssessmentId=? and "
				+ " p.publishedAssessmentId=s.assessment.publishedAssessmentId ";

		final HibernateCallback hcb2 = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query2);
				q.setLong(0, publishedAssessmentId.longValue());
				//q.setString(1, key);
				//q.setString(2, value);
				return q.list();
			};
		};
		List sec = getHibernateTemplate().executeFind(hcb2);

		PublishedItemData returnItem = null;
		if (sec.size() > 0 && l.size() > 0) {
			Collections.sort(sec, new SecComparator());
			for (int i = 0; i < sec.size(); i++) {
				PublishedSectionData thisSec = (PublishedSectionData) sec
						.get(i);
				ArrayList itemList = new ArrayList();
				for (int j = 0; j < l.size(); j++) {
					PublishedItemData compItem = (PublishedItemData) l.get(j);
					if (compItem.getSection().getSectionId().equals(
							thisSec.getSectionId())) {
						itemList.add(compItem);
					}
				}
				if (itemList.size() > 0) {
					Collections.sort(itemList, new ItemComparator());
					returnItem = (PublishedItemData) itemList.get(0);
					break;
				}
			}
		}
		return returnItem;
	}

	public List getPublishedItemIds(final Long publishedAssessmentId) {
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select i.itemId from PublishedItemData i, PublishedSectionData s, "
								+ " PublishedAssessmentData p where p.publishedAssessmentId=? and "
								+ " p = s.assessment and i.section = s");
				q.setLong(0, publishedAssessmentId.longValue());
				return q.list();
			};
		};
		return getHibernateTemplate().executeFind(hcb);

		// return getHibernateTemplate().find(
		// "select i.itemId from PublishedItemData i, PublishedSectionData s, "+
		// " PublishedAssessmentData p where p.publishedAssessmentId=? and "+
		// " p = s.assessment and i.section = s",
		// new Object[] { publishedAssessmentId },
		// new org.hibernate.type.Type[] { Hibernate.LONG });
	}
	
	public HashSet getPublishedItemSet(final Long publishedAssessmentId, final Long sectionId) {
		HashSet itemSet = new HashSet();
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select i from PublishedItemData i, PublishedSectionData s, "
								+ " PublishedAssessmentData p where p.publishedAssessmentId=? "
								+ " and i.section.id=? and p = s.assessment and i.section = s");
				q.setLong(0, publishedAssessmentId.longValue());
				q.setLong(1, sectionId.longValue());
				return q.list();
			};
		};
		List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    Iterator iter = assessmentGradings.iterator();
	    PublishedItemData publishedItemData;
	    while(iter.hasNext()) {
	    	publishedItemData = (PublishedItemData) iter.next();
	    	log.debug("itemId = " + publishedItemData.getItemId());
	    	itemSet.add(publishedItemData);
	    }
	    return itemSet;

	}

	public Long getItemType(final Long publishedItemId) {
		final String query = "select p.typeId " + " from PublishedItemData p "
				+ " where p.itemId=?";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, publishedItemId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[] { publishedItemId },
		// new org.hibernate.type.Type[] { Hibernate.LONG });
		if (list.size() > 0)
			return (Long) list.get(0);
		else
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
			data = (PublishedAssessmentData) ((PublishedAssessmentFacade) assessment)
					.getData();
		else
			data = (PublishedAssessmentData) assessment;

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().delete(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem removing publishedAssessment: "
						+ e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public HashSet getSectionSetForAssessment(Long publishedAssessmentId) {
		List sectionList = getHibernateTemplate().find(
				"from PublishedSectionData s where s.assessment.publishedAssessmentId=?", 
				 publishedAssessmentId);
		HashSet set = new HashSet();
		for (int j = 0; j < sectionList.size(); j++) {
			set.add((PublishedSectionData) sectionList.get(j));
		}
		return set;
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

	public boolean isRandomDrawPart(final Long publishedAssessmentId,
			final Long sectionId) {
		boolean isRandomDrawPart = false;
		final String key = SectionDataIfc.AUTHOR_TYPE;
		final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL
				.toString();
		final String query = "select s from PublishedSectionData s, PublishedSectionMetaData m where "
				+ " s = m.section and s.assessment.publishedAssessmentId=? and "
				+ " s.id=? and m.label=? and m.entry=?";

		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, publishedAssessmentId.longValue());
				q.setLong(1, sectionId.longValue());
				q.setString(2, key);
				q.setString(3, value);
				return q.list();
			};
		};
		List l = getHibernateTemplate().executeFind(hcb);

		if (l.size() > 0)
			isRandomDrawPart = true;
		return isRandomDrawPart;
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
	public ArrayList getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(
			final String agentId, final String siteId, boolean allAssessments) {
		
		// take account of group release
		final ArrayList groupIds = getSiteGroupIdsForCurrentUser(siteId);
		// sorted by submittedData DESC
		final String order_last = " order by p.publishedAssessmentId DESC, a.submittedDate DESC";
		// sorted by finalScore DESC
		final String order_highest = " order by p.publishedAssessmentId DESC, a.finalScore DESC, a.submittedDate DESC";

		List last_list;
		List highest_list;
		
		// Get total no. of submission per assessment by the given agent
		if (groupIds.size() > 0) {
			final String hql = "select distinct new AssessmentGradingData("
				+ " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
				+ " a.submittedDate, a.isLate,"
				+ " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
				+ " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
				+ " a.timeElapsed) "
				+ " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
				+ " where a.publishedAssessmentId = p.publishedAssessmentId"
				+ " and a.forGrade=:forGrade and a.agentId=:agentId"
				+ " and (az.agentIdString=:siteId or az.agentIdString in (:groupIds)) "
				+ " and az.functionId=:functionId and az.qualifierId=p.publishedAssessmentId"
				+ " and (p.status=:activeStatus or p.status=:editStatus) ";

			final HibernateCallback hcb_last = new HibernateCallback() {
				public Object doInHibernate(Session session)
				throws HibernateException, SQLException {
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
			};

			// this list is sorted by submittedDate desc.
			last_list = getHibernateTemplate().executeFind(hcb_last);

			final HibernateCallback hcb_highest = new HibernateCallback() {
				public Object doInHibernate(Session session)
				throws HibernateException, SQLException {
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
			};

			// this list is sorted by finalScore desc.

			highest_list = getHibernateTemplate().executeFind(hcb_highest);
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

			final HibernateCallback hcb_last = new HibernateCallback() {
				public Object doInHibernate(Session session)
				throws HibernateException, SQLException {
					Query q = session.createQuery(hql + order_last);
					q.setBoolean("forGrade", true);
					q.setString("agentId", agentId);
					q.setString("siteId", siteId);
					q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
					return q.list();
				};
			};

			// this list is sorted by submittedDate desc.
			last_list = getHibernateTemplate().executeFind(hcb_last);

			final HibernateCallback hcb_highest = new HibernateCallback() {
				public Object doInHibernate(Session session)
				throws HibernateException, SQLException {
					Query q = session.createQuery(hql + order_highest);
					q.setBoolean("forGrade", true);
					q.setString("agentId", agentId);
					q.setString("siteId", siteId);
					q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
					return q.list();
				};
			};

			// this list is sorted by finalScore desc.
			highest_list = getHibernateTemplate().executeFind(hcb_highest);
		}
		
		//getEvaluationModel();
		final String query = "select e.assessment.publishedAssessmentId, e.scoringType, ac.submissionsAllowed  " +
		"from PublishedEvaluationModel e, PublishedAccessControl ac, AuthorizationData az " +
		"where e.assessment.publishedAssessmentId = ac.assessment.publishedAssessmentId " +
		"and az.qualifierId = ac.assessment.publishedAssessmentId and az.agentIdString in (:agentIdString) and az.functionId=:functionId";

		groupIds.add(siteId);
		
		final HibernateCallback eval_model = new HibernateCallback() {
			public Object doInHibernate(Session session)
			throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setParameterList("agentIdString", groupIds);
				q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
				return q.list();
			};
		};
			
		List l = getHibernateTemplate().executeFind(eval_model);
		HashMap scoringTypeMap = new HashMap();
		HashMap subissionAllowedMap = new HashMap();
		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next(); 
			scoringTypeMap.put(o[0], o[1]);
			subissionAllowedMap.put(o[0], o[2]);
		}
		
		// The sorting for each column will be done in the action listener.
		ArrayList assessmentList = new ArrayList();
		Long currentid = new Long("0");
		Integer scoringOption = EvaluationModelIfc.LAST_SCORE; // use Last as
		Integer submissionAllowed = null;
		boolean multiSubmissionAllowed = false;

		// now go through the last_list, and get the first entry in the list for
		// each publishedAssessment, if
		// not

		for (int i = 0; i < last_list.size(); i++) {
			AssessmentGradingData a = (AssessmentGradingData) last_list.get(i);

			// get the scoring option
			if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				scoringOption = (Integer) scoringTypeMap.get(a.getPublishedAssessmentId());
			}
			else {
				// I use Last as default because it is what set above
				scoringOption = EvaluationModelIfc.LAST_SCORE; 
			}
			if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				submissionAllowed = (Integer) subissionAllowedMap.get(a.getPublishedAssessmentId());
			}
			else {
				submissionAllowed = null;
			}
			if (submissionAllowed != null) {
				if (submissionAllowed.intValue() == 1) {
					scoringOption = EvaluationModelIfc.LAST_SCORE;
				}
			}
			
			if (EvaluationModelIfc.LAST_SCORE.equals(scoringOption)) {
				if (!a.getPublishedAssessmentId().equals(currentid) || allAssessments) {
					AssessmentGradingData f = a;
					if (!a.getPublishedAssessmentId().equals(currentid)) {
						f.setIsRecorded(true);
					}
					assessmentList.add(f);
					currentid = a.getPublishedAssessmentId();
				}	
			}
		}

		// now go through the highest_list ,and get the first entry in the list
		// for each publishedAssessment.

		for (int i = 0; i < highest_list.size(); i++) {
			AssessmentGradingData a = (AssessmentGradingData) highest_list
					.get(i);
			
			// get the scoring option
			if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				scoringOption = (Integer) scoringTypeMap.get(a.getPublishedAssessmentId());
			}
			else {
				// I use Last as default because it is what set above
				scoringOption = EvaluationModelIfc.LAST_SCORE; 
			}
			if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				submissionAllowed = (Integer) subissionAllowedMap.get(a.getPublishedAssessmentId());
			}
			else {
				submissionAllowed = null;
			}
			if (submissionAllowed != null) {
				if (submissionAllowed.intValue() > 1) {
					multiSubmissionAllowed = true;
				}
				else {
					multiSubmissionAllowed = false;
				}
			}
			else {
				multiSubmissionAllowed = true;
			}
			
			if (multiSubmissionAllowed && (EvaluationModelIfc.HIGHEST_SCORE.equals(scoringOption))) {
				if (!a.getPublishedAssessmentId().equals(currentid) || allAssessments) {
					AssessmentGradingData f = a;
					if (!a.getPublishedAssessmentId().equals(currentid)) {
						f.setIsRecorded(true);
					}
					assessmentList.add(f);
					currentid = a.getPublishedAssessmentId();
				}	
			}

			if (EvaluationModelIfc.AVERAGE_SCORE.equals(scoringOption)) {
				AssessmentGradingData f = a;
				assessmentList.add(f);
			}

		}

		return assessmentList;
	}
	  
	public PublishedAssessmentData getBasicInfoOfPublishedAssessment(
			final Long publishedId) {
		String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "
				+ " c.releaseTo, c.startDate, c.dueDate, c.retractDate, "
				+ " c.feedbackDate, f.feedbackDelivery, f.feedbackComponentOption, f.feedbackAuthoring, c.lateHandling, "
				+ " c.unlimitedSubmissions, c.submissionsAllowed) "
				+ " from PublishedAssessmentData as p, PublishedAccessControl as c,"
				+ " PublishedFeedback as f"
				+ " where c.assessment.publishedAssessmentId=p.publishedAssessmentId "
				+ " and p.publishedAssessmentId = f.assessment.publishedAssessmentId "
				+ " and p.publishedAssessmentId=?";
		final String hql = query;
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setLong(0, publishedId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		return (PublishedAssessmentData) list.get(0);
	}

	  public String getPublishedAssessmentSiteId(String publishedAssessmentId) {
		    String query = "select a from AuthorizationData a " +
		    		"where a.functionId = ? and " 
		    		+ "a.qualifierId = ? ";
		    Object [] values = {"TAKE_PUBLISHED_ASSESSMENT", publishedAssessmentId};
		    List l = getHibernateTemplate().find(query, values);

		    PublishedAssessmentData publishedAssessment = 
	    		loadPublishedAssessment(Long.valueOf(publishedAssessmentId));
		    boolean releaseToGroups = AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(publishedAssessment.getAssessmentAccessControl().getReleaseTo());
		    for (int i = 0; i < l.size(); i++) {
		    	AuthorizationData a = (AuthorizationData) l.get(i);
		    	if (releaseToGroups) {
		    		String agentId = a.getAgentIdString();
		    		if (siteService.findGroup(agentId) != null && siteService.findGroup(agentId).getContainingSite() != null) {
		    			return siteService.findGroup(a.getAgentIdString()).getContainingSite().getId();
		    		}
		    	}
		    	else {
		    		return a.getAgentIdString();
		    	}
		    }
		    
		    return "";
	}
	  
	/**
	 * to take account of difference in obtaining question count
	 * between randomized and non-randomized questions
	 */  
	public Integer getPublishedItemCount(final Long publishedAssessmentId) {
		return getPublishedItemCountForNonRandomSections(publishedAssessmentId) +
			getPublishedItemCountForRandomSections(publishedAssessmentId);
/*		
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select count(i) from PublishedItemData i, PublishedSectionData s, "
								+ " PublishedAssessmentData p where p.publishedAssessmentId=? and "
								+ " p = s.assessment and i.section = s");
				q.setLong(0, publishedAssessmentId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		return (Integer) list.get(0);
*/		
	}

	/**
	 * @param publishedAssessmentId
	 * @return
	 */
	public Integer getPublishedItemCountForRandomSections(final Long publishedAssessmentId) {
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select m.entry from PublishedSectionData s, "
								+ " PublishedAssessmentData p, PublishedSectionMetaData m " 
								+ " where p.publishedAssessmentId=:publishedAssessmentId and m.label=:metaDataLabel and "
								+ " p = s.assessment and m.section = s ");
				q.setLong("publishedAssessmentId", publishedAssessmentId.longValue());
				q.setString("metaDataLabel", SectionDataIfc.NUM_QUESTIONS_DRAWN);
				//q.setString("metaDataEntry", SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		
		int sum = 0;
		for (int i=0; i<list.size(); i++) {
			if (list.get(i) != null) {
				sum += Integer.valueOf((String)list.get(i));
			}
		}
		return sum;
	}

	/**
	 * @param publishedAssessmentId
	 * @return
	 */
	public Integer getPublishedItemCountForNonRandomSections(final Long publishedAssessmentId) {
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select count(i) from PublishedItemData i, PublishedSectionData s, "
								+ " PublishedAssessmentData p, PublishedSectionMetaData m " 
								+ " where p.publishedAssessmentId=:publishedAssessmentId and m.label=:metaDataLabel and "
								+ " p = s.assessment and i.section = s and m.section = s and m.entry=:metaDataEntry ");

				q.setLong("publishedAssessmentId", publishedAssessmentId.longValue());
				q.setString("metaDataLabel", SectionDataIfc.AUTHOR_TYPE);
				q.setString("metaDataEntry", SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
				//q.setLong(0, publishedAssessmentId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		return (Integer) list.get(0);
	}
	
	public Integer getPublishedSectionCount(final Long publishedAssessmentId) {
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select count(s) from PublishedSectionData s, "
								+ " PublishedAssessmentData p where p.publishedAssessmentId=? and "
								+ " p = s.assessment");
				q.setLong(0, publishedAssessmentId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		return (Integer) list.get(0);
	}
		
	public PublishedAttachmentData getPublishedAttachmentData(Long attachmentId) {
		String query = "select a from PublishedAttachmentData a where a.attachmentId = ?";
		List l = getHibernateTemplate().find(query, attachmentId);
		if (l.size() > 0) {
			PublishedAttachmentData a = (PublishedAttachmentData) l.get(0);
			return a;
		} else
			return null;
	}

	public void updateAssessmentLastModifiedInfo(
			PublishedAssessmentFacade publishedAssessmentFacade) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		AssessmentBaseIfc data = publishedAssessmentFacade.getData();
		data.setLastModifiedBy(AgentFacade.getAgentString());
		data.setLastModifiedDate(new Date());
		retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(section.getData());
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update section: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public void removeItemAttachment(Long itemAttachmentId) {
		PublishedItemAttachment itemAttachment = (PublishedItemAttachment) getHibernateTemplate()
				.load(PublishedItemAttachment.class, itemAttachmentId);
		ItemDataIfc item = itemAttachment.getItem();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				if (item != null) { // need to dissociate with item before
					// deleting in Hibernate 3
					Set set = item.getItemAttachmentSet();
					set.remove(itemAttachment);
					getHibernateTemplate().delete(itemAttachment);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem delete itemAttachment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
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
		Set sectionSet = getSectionSetForAssessment(publishedAssessmentId);
		assessment.setSectionSet(sectionSet);

		// #2 - will called the section "Section d" here d is the total no. of
		// section in this assessment
		// #2 section has no default name - per Marc's new mockup
		PublishedSectionData section = new PublishedSectionData(
				null,
				 Integer.valueOf(sectionSet.size() + 1), // NEXT section
				"", "", TypeD.DEFAULT_SECTION, SectionData.ACTIVE_STATUS,
				AgentFacade.getAgentString(), new Date(), AgentFacade
						.getAgentString(), new Date());
		section.setAssessment(assessment);
		section.setAssessmentId(assessment.getAssessmentId());

		// add default part type, and question Ordering
		section.addSectionMetaData(SectionDataIfc.AUTHOR_TYPE,
				SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
		section.addSectionMetaData(SectionDataIfc.QUESTIONS_ORDERING,
				SectionDataIfc.AS_LISTED_ON_ASSESSMENT_PAGE.toString());

		sectionSet.add(section);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(section);
				retryCount = 0;
			} catch (Exception e) {
				log
						.warn("problem save or update assessment: "
								+ e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
		return new PublishedSectionFacade(section);
	}

	public PublishedSectionFacade getSection(Long sectionId) {
		PublishedSectionData publishedSection = (PublishedSectionData) getHibernateTemplate()
				.load(PublishedSectionData.class, sectionId);
		return new PublishedSectionFacade(publishedSection);
	}

	public AssessmentAccessControlIfc loadPublishedAccessControl(
			Long publishedAssessmentId) {
		List list = getHibernateTemplate()
				.find(
						"select c from PublishedAssessmentData as p, PublishedAccessControl as c "
								+ " where c.assessment.publishedAssessmentId=p.publishedAssessmentId "
								+ " and p.publishedAssessmentId = ?",
						publishedAssessmentId);

		return (PublishedAccessControl) list.get(0);
	}

	public void saveOrUpdatePublishedAccessControl(
			AssessmentAccessControlIfc publishedAccessControl) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(publishedAccessControl);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update publishedAccessControl data: "
						+ e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	private SecurityService securityService;
	private SiteService siteService;
	

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private ArrayList getSiteGroupIdsForSubmittingAgent(String agentId, String siteId) {

		final ArrayList<String> groupIds = new ArrayList<String>();
		// To accomodate the problem with Hibernate and empty array parameters 
		// TODO: this should probably be handled in a more efficient way
		groupIds.add("none");  
		
		if (siteId == null)
			return groupIds;
		
		Collection siteGroups = null;
		
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
			Iterator groupsIter = siteGroups.iterator();
			
			while (groupsIter.hasNext()) {
				Group group = (Group) groupsIter.next(); 
				groupIds.add(group.getId());
			}
		}
		return groupIds;
	}
	
	private ArrayList getSiteGroupIdsForCurrentUser(final String siteId) {
		String currentUserId = UserDirectoryService.getCurrentUser().getId();
		return getSiteGroupIdsForSubmittingAgent(currentUserId, siteId);
	}
		
	/**
	 * 
	 * @param assessmentId
	 * @return
	 */
	private String getReleaseToGroupsAsString(TreeMap groupsForSite, Long assessmentId) {
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
	private TreeMap getGroupsForSite(String siteId){
		TreeMap sortedGroups = new TreeMap();
		Site site = null;
		try {
			site = SiteService.getSite(siteId);
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
	   * @return
	   */
	  public TreeMap getGroupsForSite(){
		  String siteId = ToolManager.getCurrentPlacement().getContext();
	      return getGroupsForSite(siteId);
	  }

	
	  /**
	   * @param publishedAssessmentId
	   * @return
	   */
 	  public List getReleaseToGroupIdsForPublishedAssessment(
				final String publishedAssessmentId) {
			
 			final String query = "select agentIdString from AuthorizationData az where az.functionId=:functionId and az.qualifierId=:publishedAssessmentId";
			final HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query q = session.createQuery(query);
					q.setString("publishedAssessmentId", publishedAssessmentId);
					q.setString("functionId", "TAKE_PUBLISHED_ASSESSMENT");
					return q.list();
				};
			};
			return getHibernateTemplate().executeFind(hcb);
	}

	public Integer getPublishedAssessmentStatus(Long publishedAssessmentId) {
		String query = "select p.status from PublishedAssessmentData p where p.publishedAssessmentId = ?";
		List l = getHibernateTemplate().find(query, publishedAssessmentId);
		if (l.size() > 0) {
			Integer status = (Integer) l.get(0);
			return status;
		} else {
			// just set to AssessmentBaseIfc.DEAD_STATUS
			return Integer.valueOf(2);
		}
	}
	
	public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol) {
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}
		return attach;
	}
	
	private long fileSizeInKB(long fileSize) {
		return fileSize / 1024;
	}
	
	public void removeAssessmentAttachment(Long assessmentAttachmentId) {
		PublishedAssessmentAttachment assessmentAttachment = (PublishedAssessmentAttachment) getHibernateTemplate()
				.load(PublishedAssessmentAttachment.class, assessmentAttachmentId);
		AssessmentIfc assessment = assessmentAttachment.getAssessment();
		// String resourceId = assessmentAttachment.getResourceId();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
				log.warn("problem delete publishedAssessmentAttachment: "
						+ e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}

		return attach;
	}

	public void removeSectionAttachment(Long sectionAttachmentId) {
		PublishedSectionAttachment sectionAttachment = (PublishedSectionAttachment) getHibernateTemplate()
				.load(PublishedSectionAttachment.class, sectionAttachmentId);
		SectionDataIfc section = sectionAttachment.getSection();
		// String resourceId = sectionAttachment.getResourceId();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
				log.warn("problem delete sectionAttachment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}
	
	public void saveOrUpdateAttachments(List list) {
		getHibernateTemplate().saveOrUpdateAll(list);
	}
	
	  public PublishedAssessmentFacade getPublishedAssessmentInfoForRemove(Long publishedAssessmentId) {
		  PublishedAssessmentData a = (PublishedAssessmentData) getHibernateTemplate().load(
				  PublishedAssessmentData.class, publishedAssessmentId);
		  PublishedAssessmentFacade f = new PublishedAssessmentFacade(a.getAssessmentId(), a.getTitle(), a.getCreatedBy());
		  return f;
	  }  

	  public HashMap getToGradebookPublishedAssessmentSiteIdMap() {
		  String query = "select em.assessment.publishedAssessmentId, a.agentIdString " +
		  "from PublishedEvaluationModel em, AuthorizationData a " +
		  "where a.functionId = 'OWN_PUBLISHED_ASSESSMENT' " +
		  "and em.assessment.publishedAssessmentId = a.qualifierId " +
		  "and em.toGradeBook = ?";

		  List l = getHibernateTemplate().find(query, "1");
		  HashMap toGradebookPublishedAssessmentSiteIdMap = new HashMap();
		  Iterator iter = l.iterator();
		  while (iter.hasNext()) {
			  Object o[] = (Object[]) iter.next(); 
			  toGradebookPublishedAssessmentSiteIdMap.put(o[0], o[1]);
		  }
		  return toGradebookPublishedAssessmentSiteIdMap;
	  }	  
	  
	  

	  /**
	   * return an array list of the AssessmentGradingData that a user has
	   * submitted for grade. one per published assessment. If an assessment
	   * allows multiple submissions and its grading option is to send highest,
	   * then return only the submission with highest finalScore. If an assessment
	   * allows multiple submissions and its grading option is to send last, then
	   * return only the last submission.
	   * @author Mustansar Mehmood mustansar@rice.edu
	   * @param agentId
	   * @param siteId
	   * @return
	   */
	  /*
	  public ArrayList getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(
			  final String agentId, final String siteId) {
		  // Get total no. of submission per assessment by the given agent
		  // sorted by submittedData DESC
		  final String last_query = "select new AssessmentGradingData("
			  + " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
			  + " a.submittedDate, a.isLate,"
			  + " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
			  + " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
			  + " a.timeElapsed) "
			  + " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
			  + " where a.publishedAssessmentId = p.publishedAssessmentId"
			  + " and a.forGrade=? and a.agentId=? and az.agentIdString=? "
			  + " and az.functionId=? and az.qualifierId=p.publishedAssessmentId"
			  + " order by p.publishedAssessmentId DESC, a.submittedDate DESC";

		  // Get total no. of submission per assessment by the given agent
		  // sorted by finalScore DESC

		  final String highest_query = "select new AssessmentGradingData("
			  + " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
			  + " a.submittedDate, a.isLate,"
			  + " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
			  + " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
			  + " a.timeElapsed) "
			  + " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
			  + " where a.publishedAssessmentId = p.publishedAssessmentId"
			  + " and a.forGrade=? and a.agentId=? and az.agentIdString=? "
			  + " and az.functionId=? and az.qualifierId=p.publishedAssessmentId"
			  + " order by p.publishedAssessmentId DESC, a.finalScore DESC, a.submittedDate DESC";

		  final HibernateCallback hcb_last = new HibernateCallback() {
			  public Object doInHibernate(Session session)
			  throws HibernateException, SQLException {
				  Query q = session.createQuery(last_query);
				  q.setBoolean(0, true);
				  q.setString(1, agentId);
				  q.setString(2, siteId);
				  q.setString(3, "TAKE_PUBLISHED_ASSESSMENT");
				  return q.list();
			  };
		  };

		  // this list is sorted by submittedDate desc.
		  List last_list = getHibernateTemplate().executeFind(hcb_last);

		  final HibernateCallback hcb_highest = new HibernateCallback() {
			  public Object doInHibernate(Session session)
			  throws HibernateException, SQLException {
				  Query q = session.createQuery(highest_query);
				  q.setBoolean(0, true);
				  q.setString(1, agentId);
				  q.setString(2, siteId);
				  q.setString(3, "TAKE_PUBLISHED_ASSESSMENT");
				  return q.list();
			  };
		  };

		  // this list is sorted by finalScore desc.

		  List highest_list = getHibernateTemplate().executeFind(hcb_highest);

		  //getEvaluationModel();
		  String query = "select a.publishedAssessmentId, e.scoringType, ac.submissionsAllowed  " +
		  "from PublishedEvaluationModel e, PublishedAccessControl ac, PublishedAssessmentData a " +
		  "where e.assessment.publishedAssessmentId = a.publishedAssessmentId " +
		  "and ac.assessment.publishedAssessmentId = a.publishedAssessmentId ";

		  List l = getHibernateTemplate().find(query);
		  HashMap scoringTypeMap = new HashMap();
		  HashMap subissionAllowedMap = new HashMap();
		  Iterator iter = l.iterator();
		  while (iter.hasNext()) {
			  Object o[] = (Object[]) iter.next(); 
			  scoringTypeMap.put(o[0], o[1]);
			  subissionAllowedMap.put(o[0], o[2]);
		  }		

		  // The sorting for each column will be done in the action listener.
		  ArrayList assessmentList = new ArrayList();
		  Long currentid = new Long("0");
		  Integer scoringOption = EvaluationModelIfc.LAST_SCORE; // use Last as
		  Integer submissionAllowed = null;
		  boolean multiSubmissionAllowed = false;

		  // now go through the last_list, and get the first entry in the list for
		  // each publishedAssessment, if
		  // not

		  for (int i = 0; i < last_list.size(); i++) {
			  AssessmentGradingData a = (AssessmentGradingData) last_list.get(i);

			  // get the scoring option
			  if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				  scoringOption = (Integer) scoringTypeMap.get(a.getPublishedAssessmentId());
			  }
			  else {
				  // I use Last as default because it is what set above
				  scoringOption = EvaluationModelIfc.LAST_SCORE; 
			  }
			  if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				  submissionAllowed = (Integer) subissionAllowedMap.get(a.getPublishedAssessmentId());
			  }
			  else {
				  submissionAllowed = null;
			  }
			  if (submissionAllowed != null) {
				  if (submissionAllowed.intValue() == 1) {
					  scoringOption = EvaluationModelIfc.LAST_SCORE;
				  }
			  }


			  if (EvaluationModelIfc.LAST_SCORE.equals(scoringOption) && !a.getPublishedAssessmentId().equals(currentid)) {
				  currentid = a.getPublishedAssessmentId();
				  AssessmentGradingData f = new AssessmentGradingData(a);
				  assessmentList.add(f);
			  }

		  }

		  // now go through the highest_list ,and get the first entry in the list
		  // for each publishedAssessment.

		  for (int i = 0; i < highest_list.size(); i++) {
			  AssessmentGradingData a = (AssessmentGradingData) highest_list
			  .get(i);

			  // get the scoring option
			  if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				  scoringOption = (Integer) scoringTypeMap.get(a.getPublishedAssessmentId());
			  }
			  else {
				  // I use Last as default because it is what set above
				  scoringOption = EvaluationModelIfc.LAST_SCORE; 
			  }
			  if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				  submissionAllowed = (Integer) subissionAllowedMap.get(a.getPublishedAssessmentId());
			  }
			  else {
				  submissionAllowed = null;
			  }
			  if (submissionAllowed != null) {
				  if (submissionAllowed.intValue() > 1) {
					  multiSubmissionAllowed = true;
				  }
				  else {
					  multiSubmissionAllowed = false;
				  }
			  }
			  else {
				  multiSubmissionAllowed = true;
			  }

			  if ((multiSubmissionAllowed)
					  && (EvaluationModelIfc.HIGHEST_SCORE.equals(scoringOption))
					  && (!a.getPublishedAssessmentId().equals(currentid))) {
				  currentid = a.getPublishedAssessmentId();
				  AssessmentGradingData f = new AssessmentGradingData(a);
				  assessmentList.add(f);
			  }

		  }
		  //*assessments with average grades 

		  for (int i = 0; i < highest_list.size(); i++) {
			  AssessmentGradingData a = (AssessmentGradingData) highest_list.get(i);

			  // get the scoring option
			  if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				  scoringOption = (Integer) scoringTypeMap.get(a.getPublishedAssessmentId());
			  }
			  else {
				  // I use Last as default because it is what set above --mustansar
				  scoringOption = EvaluationModelIfc.LAST_SCORE; 
			  }
			  if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				  submissionAllowed = (Integer) subissionAllowedMap.get(a.getPublishedAssessmentId());
			  }
			  else {
				  submissionAllowed = null;
			  }
			  if (submissionAllowed != null) {
				  if (submissionAllowed.intValue() > 1) {
					  multiSubmissionAllowed = true;
				  }
				  else {
					  multiSubmissionAllowed = false;
				  }
			  }
			  else {
				  multiSubmissionAllowed = true;
			  }

			  if ((multiSubmissionAllowed)
					  && (EvaluationModelIfc.AVERAGE_SCORE.equals(scoringOption))
					  && (!a.getPublishedAssessmentId().equals(currentid))) {
				  currentid = a.getPublishedAssessmentId();

				  AssessmentGradingData f = new AssessmentGradingData(a);
				  assessmentList.add(f);
			  }

		  }
		  //end of finding assessments with average grades
		  //return assessmentList;
		  return null;
	  }
	  */

	  /**
	   * return an array list of the AssessmentGradingData that a user has
	   * submitted for grade. one per published assessment. If an assessment
	   * allows multiple submissions and its grading option is to send highest,
	   * then return only the submission with highest finalScore. If an assessment
	   * allows multiple submissions and its grading option is to send last, then
	   * return only the last submission.
	   * @author Mustansar Mehmood mustansar@rice.edu
	   * @param agentId
	   * @param siteId
	   * @param allAssessments
	   * @return
	   */
	  /*
	  public ArrayList getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption(
			  final String agentId, final String siteId, boolean allAssessments) {
		  // Get total no. of submission per assessment by the given agent
		  // sorted by submittedData DESC
		  final String last_query = "select new AssessmentGradingData("
			  + " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
			  + " a.submittedDate, a.isLate,"
			  + " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
			  + " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
			  + " a.timeElapsed) "
			  + " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
			  + " where a.publishedAssessmentId = p.publishedAssessmentId"
			  + " and a.forGrade=? and a.agentId=? and az.agentIdString=? "
			  + " and az.functionId=? and az.qualifierId=p.publishedAssessmentId"
			  + " order by p.publishedAssessmentId DESC, a.submittedDate DESC";

		  // Get total no. of submission per assessment by the given agent
		  // sorted by finalScore DESC

		  final String highest_query = "select new AssessmentGradingData("
			  + " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId,"
			  + " a.submittedDate, a.isLate,"
			  + " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore,"
			  + " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate,"
			  + " a.timeElapsed) "
			  + " from AssessmentGradingData a, PublishedAssessmentData p, AuthorizationData az"
			  + " where a.publishedAssessmentId = p.publishedAssessmentId"
			  + " and a.forGrade=? and a.agentId=? and az.agentIdString=? "
			  + " and az.functionId=? and az.qualifierId=p.publishedAssessmentId"
			  + " order by p.publishedAssessmentId DESC, a.finalScore DESC, a.submittedDate DESC";

		  final HibernateCallback hcb_last = new HibernateCallback() {
			  public Object doInHibernate(Session session)
			  throws HibernateException, SQLException {
				  Query q = session.createQuery(last_query);
				  q.setBoolean(0, true);
				  q.setString(1, agentId);
				  q.setString(2, siteId);
				  q.setString(3, "TAKE_PUBLISHED_ASSESSMENT");
				  return q.list();
			  };
		  };

		  // this list is sorted by submittedDate desc.
		  List last_list = getHibernateTemplate().executeFind(hcb_last);

		  final HibernateCallback hcb_highest = new HibernateCallback() {
			  public Object doInHibernate(Session session)throws HibernateException, SQLException {
				  Query q = session.createQuery(highest_query);
				  q.setBoolean(0, true);
				  q.setString(1, agentId);
				  q.setString(2, siteId);
				  q.setString(3, "TAKE_PUBLISHED_ASSESSMENT");
				  return q.list();
			  };
		  };

		  // this list is sorted by finalScore desc.
		  List highest_list = getHibernateTemplate().executeFind(hcb_highest);
		  String query = "select a.publishedAssessmentId, e.scoringType, ac.submissionsAllowed  "
			  + "from PublishedEvaluationModel e, PublishedAccessControl ac, PublishedAssessmentData a "
			  + "where e.assessment.publishedAssessmentId = a.publishedAssessmentId "
			  + "and ac.assessment.publishedAssessmentId = a.publishedAssessmentId ";

		  List l = getHibernateTemplate().find(query);
		  HashMap scoringTypeMap = new HashMap();
		  HashMap subissionAllowedMap = new HashMap();
		  Iterator iter = l.iterator();
		  while (iter.hasNext()) {
			  Object o[] = (Object[]) iter.next();
			  scoringTypeMap.put(o[0], o[1]);
			  subissionAllowedMap.put(o[0], o[2]);
		  }
		  // The sorting for each column will be done in the action listener.
		  ArrayList assessmentList = new ArrayList();
		  Long currentid = new Long("0");
		  Integer scoringOption = EvaluationModelIfc.LAST_SCORE; // use Last as defaut
		  Integer submissionAllowed = null;
		  boolean multiSubmissionAllowed = false;

		  // now go through the last_list, and get the first entry in the list for
		  // each publishedAssessment, if not

		  for (int i = 0; i < last_list.size(); i++) {
			  AssessmentGradingData a = (AssessmentGradingData) last_list.get(i);

			  // get the scoring option
			  if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				  scoringOption = (Integer) scoringTypeMap.get(a
						  .getPublishedAssessmentId());
			  } else {
				  // I use Last as default because it is what set above
				  scoringOption = EvaluationModelIfc.LAST_SCORE;
			  }
			  if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				  submissionAllowed = (Integer) subissionAllowedMap.get(a
						  .getPublishedAssessmentId());
			  } else {
				  submissionAllowed = null;
			  }
			  if (submissionAllowed != null) {
				  if (submissionAllowed.intValue() == 1) {
					  scoringOption = EvaluationModelIfc.LAST_SCORE;
				  }
			  }
			  if (EvaluationModelIfc.LAST_SCORE.equals(scoringOption)
					  && (!a.getPublishedAssessmentId().equals(currentid)
							  || allAssessments)) {
				  currentid = a.getPublishedAssessmentId();
				  AssessmentGradingData f = new AssessmentGradingData(a);
				  assessmentList.add(f);
			  }

		  }

		  // now go through the highest_list ,and get the first entry in the list
		  // for each publishedAssessment.

		  for (int i = 0; i < highest_list.size(); i++) {
			  AssessmentGradingData a = (AssessmentGradingData) highest_list.get(i);

			  // get the scoring option
			  if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				  scoringOption = (Integer) scoringTypeMap.get(a
						  .getPublishedAssessmentId());
			  } else {
				  // I use Last as default because it is what set above
				  scoringOption = EvaluationModelIfc.LAST_SCORE;
			  }
			  if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				  submissionAllowed = (Integer) subissionAllowedMap.get(a
						  .getPublishedAssessmentId());
			  } else {
				  submissionAllowed = null;
			  }
			  if (submissionAllowed != null) {
				  if (submissionAllowed.intValue() > 1) {
					  multiSubmissionAllowed = true;
				  } else {
					  multiSubmissionAllowed = false;
				  }
			  } else {
				  multiSubmissionAllowed = true;
			  }

			  if ((multiSubmissionAllowed)
					  && (EvaluationModelIfc.HIGHEST_SCORE.equals(scoringOption))
					  && ((!a.getPublishedAssessmentId().equals(currentid))
							  || allAssessments)) {
				  currentid = a.getPublishedAssessmentId();
				  AssessmentGradingData f = new AssessmentGradingData(a);
				  assessmentList.add(f);
			  }
		  }
		  
		   // assessments with average grades
		   
		  for (int i = 0; i < last_list.size(); i++) {
			  AssessmentGradingData a = (AssessmentGradingData) last_list.get(i);
			  // get the scoring option
			  if (scoringTypeMap.get(a.getPublishedAssessmentId()) != null) {
				  scoringOption = (Integer) scoringTypeMap.get(a.getPublishedAssessmentId());
			  } else {
				  // I use Last as default because it is what set above
				  // --mustansar
				  scoringOption = EvaluationModelIfc.LAST_SCORE;
			  }
			  if (subissionAllowedMap.get(a.getPublishedAssessmentId()) != null) {
				  submissionAllowed = (Integer) subissionAllowedMap.get(a.getPublishedAssessmentId());
			  } else {
				  submissionAllowed = null;
			  }
			  if (submissionAllowed != null) {
				  if (submissionAllowed.intValue() > 1) {
					  multiSubmissionAllowed = true;
				  } else {
					  multiSubmissionAllowed = false;
				  }
			  } else {
				  multiSubmissionAllowed = true;
			  }
			  if (
					  (multiSubmissionAllowed)
					  && (EvaluationModelIfc.AVERAGE_SCORE.equals(scoringOption))&& (!a.getPublishedAssessmentId().equals(currentid)|| allAssessments)) {
				  currentid = a.getPublishedAssessmentId();
				  AssessmentGradingData f = new AssessmentGradingData(a);
				  assessmentList.add(f);
			  }
		  }
		  
		  // end of finding assessments with average grades
		  
		  return assessmentList;
	  }
	  */
}
