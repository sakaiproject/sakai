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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemHistorical;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTextAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTextAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemHistoricalIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.entity.api.CoreAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.entity.api.ItemEntityProvider;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateQueryException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssessmentFacadeQueries extends HibernateDaoSupport implements AssessmentFacadeQueriesAPI {

	// private ResourceBundle rb =
	// ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.Messages");

	public static final String TITLE = "title";

	public AssessmentFacadeQueries() {
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
	
	public Long addTemplate() {
		AssessmentTemplateData assessmentTemplate = new AssessmentTemplateData(
			 Long.valueOf(0), "title", "description", "comments",
				TypeD.HOMEWORK, Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1),
				Integer.valueOf(1), "1", new Date(), "1", new Date());
		AssessmentAccessControl s = new AssessmentAccessControl(Integer.valueOf(0),
				Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0),
				Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), SamigoConstants.NOTI_PREF_INSTRUCTOR_EMAIL_DEFAULT,
				new Date(),	new Date(), new Date(), new Date(), new Date(), Integer.valueOf(1),
				Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), "Thanks for submitting",
				"anonymous");
		s.setAssessmentBase(assessmentTemplate);
		assessmentTemplate
				.setAssessmentAccessControl((AssessmentAccessControlIfc) s);
		assessmentTemplate.addAssessmentMetaData(
				"ASSESSMENTTEMPLATE_OBJECTIVES",
				" assesmentT: the objective is to ...");
		// take default submission model
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().save(assessmentTemplate);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem saving template: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
		return assessmentTemplate.getAssessmentTemplateId();
	}

	public void removeTemplate(Long assessmentId) {
		AssessmentTemplateData assessment = (AssessmentTemplateData) getHibernateTemplate()
				.load(AssessmentTemplateData.class, assessmentId);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().delete(assessment);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem delete template: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public Long addAssessment(Long assessmentTemplateId) {

		AssessmentData assessment = new AssessmentData( Long.valueOf(0),
				"assessment title", "assessment description",
				"assessment acomments", assessmentTemplateId, TypeD.HOMEWORK,
				Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1),
				"1", new Date(), "1", new Date());
		AssessmentAccessControl s = new AssessmentAccessControl(Integer.valueOf(1),
				Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1),
				Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), SamigoConstants.NOTI_PREF_INSTRUCTOR_EMAIL_DEFAULT,
				new Date(), new Date(), new Date(), new Date(), new Date(), Integer.valueOf(1),
				Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1),"Thanks for submitting",
				"anonymous");

		s.setAssessmentBase(assessment);
		assessment.setAssessmentAccessControl((AssessmentAccessControlIfc) s);
		assessment.addAssessmentMetaData("ASSESSMENT_OBJECTIVES",
				" assesment: the objective is to ...");
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().save(assessment);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem saving assessment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
		return assessment.getAssessmentId();
	}

	public AssessmentBaseData load(Long id) {
		AssessmentBaseData a = (AssessmentBaseData) getHibernateTemplate()
				.load(AssessmentBaseData.class, id);
		if (a.getIsTemplate().equals(Boolean.TRUE)) {
			return (AssessmentTemplateData) a;
		} else {
			return (AssessmentData) a;
		}
	}

	public AssessmentTemplateData loadTemplate(Long assessmentTemplateId) {
		return (AssessmentTemplateData) getHibernateTemplate().load(
				AssessmentTemplateData.class, assessmentTemplateId);
	}

	public AssessmentData loadAssessment(Long assessmentId) {
		return (AssessmentData) getHibernateTemplate().load(
				AssessmentData.class, assessmentId);
	}

	/*
	 * The following methods are real
	 * 
	 */
	public AssessmentTemplateFacade getAssessmentTemplate(
			Long assessmentTemplateId) {
		AssessmentTemplateData template = (AssessmentTemplateData) getHibernateTemplate()
				.load(AssessmentTemplateData.class, assessmentTemplateId);
		return new AssessmentTemplateFacade(template);
	}

	// sakai2.0 we want to scope it by creator, users can only see their
	// templates plus the "Default Template"
	public List<AssessmentTemplateFacade> getAllAssessmentTemplates() {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		HibernateCallback<List<AssessmentTemplateData>> hcb = session -> {
            Query q = session.createQuery(
                    "select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.typeId)"
                            + " from AssessmentTemplateData a where a.assessmentBaseId = :id or"
                            + " a.createdBy = :agent or a.typeId = :type order by a.title"
            );
            q.setParameter("id", 1L);
            q.setParameter("agent", agent);
            q.setParameter("type", typeId);
            return q.list();
        };
		List<AssessmentTemplateData> list = getHibernateTemplate().execute(hcb);
		List<AssessmentTemplateFacade> templateList = new ArrayList<>();
		for (AssessmentTemplateData a : list) {
			AssessmentTemplateFacade f = new AssessmentTemplateFacade(a);
			templateList.add(f);
		}
		return templateList;
	}

	// sakai2.0 we want to scope it by creator, users can only see their
	// templates plus the "Default Template"
	public List<AssessmentTemplateFacade> getAllActiveAssessmentTemplates() {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		HibernateCallback<List<AssessmentTemplateData>> hcb = session -> {
            Query q = session.createQuery(
            		"select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.typeId)"
					+ " from AssessmentTemplateData a where a.status = :status and (a.assessmentBaseId = :id or"
					+ " a.createdBy = :agent or a.typeId = :type) order by a.title");
            q.setParameter("status", 1);
            q.setParameter("id", 1L);
            q.setParameter("agent", agent);
            q.setParameter("type", typeId);
            return q.list();
        };
		List<AssessmentTemplateData> list = getHibernateTemplate().execute(hcb);

		List<AssessmentTemplateFacade> templateList = new ArrayList<>();
		for (AssessmentTemplateData a : list) {
			AssessmentTemplateFacade f = new AssessmentTemplateFacade(a);
			templateList.add(f);
		}
		return templateList;
	}

	/**
	 * 
	 * @return a list of AssessmentTemplateFacade. However, it is IMPORTANT to
	 *         note that it is not a full object, it contains merely
	 *         assessmentBaseId (which is the templateId) & title. This methods
	 *         is used when a list of template titles is required for displaying
	 *         purposes. In Sakai2.0, template are scoped by creator, i.e. users
	 *         can only see their own template plus the "Default Template"
	 */
	public List<AssessmentTemplateFacade> getTitleOfAllActiveAssessmentTemplates() {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		HibernateCallback<List<AssessmentTemplateData>> hcb = session -> {
            Query q = session.createQuery(
					"select new AssessmentTemplateData(a.assessmentBaseId, a.title) "
							+ " from AssessmentTemplateData a where a.status = :status and "
							+ " (a.assessmentBaseId = :id or a.createdBy = :agent or typeId = :type) order by a.title"
			);
            q.setParameter("status", 1);
            q.setParameter("id", Long.valueOf(1));
            q.setParameter("agent", agent);
            q.setParameter("type", typeId.longValue());
            return q.list();
        };
		List<AssessmentTemplateData> list = getHibernateTemplate().execute(hcb);

		List<AssessmentTemplateFacade> templateList = new ArrayList<>();
		for (AssessmentTemplateData a : list) {
			a.setAssessmentTemplateId(a.getAssessmentBaseId());
			AssessmentTemplateFacade f = new AssessmentTemplateFacade(a.getAssessmentBaseId(), a.getTitle());
			templateList.add(f);
		}
		return templateList;
	}

	public AssessmentFacade getAssessment(Long assessmentId) {
		try {
			AssessmentData assessment = getHibernateTemplate().get(AssessmentData.class, assessmentId);
			if (assessment != null) {
				assessment.setSectionSet(getSectionSetForAssessment(assessment));
				return new AssessmentFacade(assessment);
			}
		} catch (DataAccessException dae) {
			log.warn("Could not retrieve assessment: {}", assessmentId, dae);
		}
		return null;
	}

	private Set<SectionData> getSectionSetForAssessment(AssessmentData assessment) {
		List<SectionData> sectionList = (List<SectionData>) getHibernateTemplate().findByNamedParam("from SectionData s where s.assessment.assessmentBaseId = :id", "id", assessment.getAssessmentBaseId());
		Hibernate.initialize(sectionList);
		return new HashSet<>(sectionList);
	}

	public void removeAssessment(final Long assessmentId) {
		// if pubAssessment exist, simply set assessment to inactive else delete assessment
		List<PublishedAssessmentData> count = (List<PublishedAssessmentData>) getHibernateTemplate()
				.findByNamedParam("select count(p) from PublishedAssessmentData p where p.assessmentId = :id", "id", assessmentId);

		log.debug("removeAssesment: no. of pub Assessment = {}", count.size());
		Iterator iter = count.iterator();
		int i = ((Long) iter.next()).intValue();
		AssessmentData assessment = (AssessmentData) getHibernateTemplate().load(AssessmentData.class, assessmentId);
		// SAK-42943 Commented because attachments aren't removed on soft deletion, but this will be handy whenever hard deletion is added
		/*if (i < 1) {
			AssessmentService s = new AssessmentService();
			List resourceIdList = s.getAssessmentResourceIdList(assessment);
			if (log.isDebugEnabled()) log.debug("*** we have no. of resource in assessment=" + resourceIdList.size());
			s.deleteResources(resourceIdList);
		}*/

		RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
		rubricsService.softDeleteRubricAssociationsByItemIdPrefix(assessmentId + ".", RubricsConstants.RBCS_TOOL_SAMIGO);

		assessment.setLastModifiedBy(AgentFacade.getAgentString());
		assessment.setLastModifiedDate(new Date());
		assessment.setStatus(AssessmentIfc.DEAD_STATUS);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().update(assessment);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem updating asssessment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper()
						.retryDeadlock(e, retryCount);
			}
		}
	}

	/* this assessment comes with a default section */
	public AssessmentData cloneAssessmentFromTemplate(AssessmentTemplateData t) {
		AssessmentData assessment = new AssessmentData(t.getParentId(),
				"Assessment created with" + t.getTitle(), t.getDescription(), t
						.getComments(), t.getAssessmentTemplateId(),
				TypeD.HOMEWORK, // by default for now
				t.getInstructorNotification(), t.getTesteeNotification(), t
						.getMultipartAllowed(), t.getStatus(), AgentFacade
						.getAgentString(), new Date(), AgentFacade
						.getAgentString(), new Date());
		try {
			// deal with Access Control
			AssessmentAccessControl controlOrig = (AssessmentAccessControl) t
					.getAssessmentAccessControl();
			if (controlOrig != null) {
				AssessmentAccessControl control = (AssessmentAccessControl) controlOrig
						.clone();
				control.setAssessmentBase(assessment);
				assessment.setAssessmentAccessControl(control);
			}
			// deal with feedback
			AssessmentFeedback feedbackOrig = (AssessmentFeedback) t
					.getAssessmentFeedback();
			if (feedbackOrig != null) {
				AssessmentFeedback feedback = (AssessmentFeedback) feedbackOrig
						.clone();
				feedback.setAssessmentBase(assessment);
				assessment.setAssessmentFeedback(feedback);
			}
			// deal with evaluation
			EvaluationModel evalOrig = (EvaluationModel) t.getEvaluationModel();
			if (evalOrig != null) {
				EvaluationModel eval = (EvaluationModel) evalOrig.clone();
				eval.setAssessmentBase(assessment);
				assessment.setEvaluationModel(eval);
			}
			// deal with MetaData
			HashSet h = new HashSet();
			Set s = t.getAssessmentMetaDataSet();
			Iterator iter = s.iterator();
			while (iter.hasNext()) {
				AssessmentMetaData mOrig = (AssessmentMetaData) iter.next();
				if (mOrig.getLabel() != null) {
					AssessmentMetaData m = new AssessmentMetaData(assessment,
							mOrig.getLabel(), mOrig.getEntry());
					h.add(m);
				}
			}
			assessment.setAssessmentMetaDataSet(h);
			// we need to add the FIRST section to an assessment
			// it is a requirement that each assesment must have at least one
			// section
			HashSet sh = new HashSet();
			SectionData section = new SectionData(
					null,
					new Integer("1"), // FIRST section
					"Default", "", TypeD.DEFAULT_SECTION,
					SectionData.ACTIVE_STATUS, AgentFacade.getAgentString(),
					new Date(), AgentFacade.getAgentString(), new Date());
			section.setAssessment(assessment);

			// add default part type, and question Ordering
			section.addSectionMetaData(SectionDataIfc.AUTHOR_TYPE,
					SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
			section.addSectionMetaData(SectionDataIfc.QUESTIONS_ORDERING,
					SectionDataIfc.AS_LISTED_ON_ASSESSMENT_PAGE.toString());

			sh.add(section);
			assessment.setSectionSet(sh);
		} catch (CloneNotSupportedException ex) {
			log.error(ex.getMessage(), ex);
		}
		return assessment;
	}

	/**
	 * This method is the same as createAssessment() except that no default
	 * section will be created with the assessment.
	 */
	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, Long typeId, Long templateId) throws Exception {
		return createAssessmentWithoutDefaultSection(title, description, typeId, templateId, null);
	}


	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, Long typeId, Long templateId, String siteId) throws Exception {

		// this assessment came with one default section
		AssessmentData assessment = null;
		try {
			assessment = prepareAssessment(title, description, typeId,
					templateId, siteId);
		} catch (Exception e) {
			throw new Exception(e);
		}
		assessment.setSectionSet(new HashSet());
		getHibernateTemplate().save(assessment);

		// register assessment with current site
		registerWithSite(assessment.getAssessmentId().toString(), siteId);
		return new AssessmentFacade(assessment);
	}


	private AssessmentData prepareAssessment(String title, String description,
			Long typeId, Long templateId, String siteId) throws Exception {
		// #1 - get the template (a facade) and create Assessment based on it
		AssessmentTemplateFacade template = getAssessmentTemplate(templateId);
		AssessmentData assessment = cloneAssessmentFromTemplate((AssessmentTemplateData) template
				.getData());
		assessment.setTitle(title);
		assessment.setDescription(description);
		assessment.setTypeId(typeId);
		AssessmentAccessControl control = (AssessmentAccessControl) assessment
			.getAssessmentAccessControl();
		if (control == null) {
			control = new AssessmentAccessControl();
		}
		
		// Set default value for timed assessment
		control.setTimedAssessment(0);
		control.setTimeLimit(0);
		
		// set accessControl.releaseTo based on default setting in metaData
		String defaultReleaseTo = template.getAssessmentMetaDataByLabel("releaseTo");
		if (("ANONYMOUS_USERS").equals(defaultReleaseTo)) {
			control.setReleaseTo("Anonymous Users");
		} else {
			if (siteId == null || siteId.isEmpty()) {
				control.setReleaseTo(AgentFacade.getCurrentSiteName());
			} else {
				control.setReleaseTo(AgentFacade.getSiteName(siteId));
			}
		}

		EvaluationModel evaluation = (EvaluationModel) assessment
			.getEvaluationModel();
		if (evaluation == null) {
			evaluation = new EvaluationModel();
		}
		org.sakaiproject.grading.api.GradingService g = null;
		boolean integrated = IntegrationContextFactory.getInstance()
			.isIntegrated();
		try {
			if (integrated) {
				g = (org.sakaiproject.grading.api.GradingService) SpringBeanLocator.getInstance().getBean(
						"org.sakaiproject.grading.api.GradingService");
			}

			GradebookServiceHelper gbsHelper = IntegrationContextFactory
			.getInstance().getGradebookServiceHelper();
		} catch (HibernateQueryException e) {
			log.warn("Gradebook Error: " + e.getMessage());
			evaluation
				.setToGradeBook(EvaluationModelIfc.GRADEBOOK_NOT_AVAILABLE.toString());
			throw new Exception(e);
		}

		return assessment;
	}

	public AssessmentFacade createAssessment(String title, String description,
			Long typeId, Long templateId) throws Exception {
		return createAssessment(title, description, typeId, templateId, null);
	}

	public AssessmentFacade createAssessment(String title, String description,
			Long typeId, Long templateId, String siteId) throws Exception {

		// this assessment comes with a default section
		AssessmentData assessment = null;
		try {
			assessment = prepareAssessment(title, description, typeId,
					templateId, siteId);
		} catch (Exception e) {
			throw new Exception(e);
		}

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().save(assessment);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem saving assessment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
		// register assessmnet with current site
		registerWithSite(assessment.getAssessmentId().toString(), siteId);
		return new AssessmentFacade(assessment);
	}

	private void registerWithSite(String qualifierIdString, String siteId) {
		if (siteId == null || siteId.length() == 0) {
			PersistenceService.getInstance().getAuthzQueriesFacade()
			.createAuthorization(AgentFacade.getCurrentSiteId(),
					"EDIT_ASSESSMENT", qualifierIdString);
		} else {

			PersistenceService.getInstance().getAuthzQueriesFacade()
			.createAuthorization(siteId,
					"EDIT_ASSESSMENT", qualifierIdString);
		}
	}

	private void registerWithCurrentSite(String qualifierIdString) {
		registerWithSite(qualifierIdString, null);
	}

	public List<AssessmentFacade> getAllAssessments(String orderBy) {
		List<AssessmentData> list = (List<AssessmentData>) getHibernateTemplate().find("from AssessmentData a order by a." + orderBy);
		List<AssessmentFacade> assessmentList = new ArrayList<>();
		for (AssessmentData a : list) {;
			assessmentList.add(new AssessmentFacade(a));
		}
		return assessmentList;
	}

	public List<AssessmentFacade> getAllActiveAssessments(String orderBy) {
		List<AssessmentData> list = (List<AssessmentData>) getHibernateTemplate().findByNamedParam(
				"from AssessmentData a where a.status = :status order by a." + orderBy, "status", 1);
		List<AssessmentFacade> assessmentList = new ArrayList<>();
		for (AssessmentData a : list) {
			a.setSectionSet(getSectionSetForAssessment(a));
			AssessmentFacade f = new AssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessments(String orderBy, boolean ascending) {
		String query = "select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate)from AssessmentData a where a.status = :status order by a." + orderBy;
		if (ascending) {
			query += " asc";
		} else {
			query += " desc";
		}

		List<AssessmentData> list = (List<AssessmentData>) getHibernateTemplate().findByNamedParam(query, "status", 1);

		List<AssessmentFacade> assessmentList = new ArrayList<>();
		for (AssessmentData a : list) {
			assessmentList.add(new AssessmentFacade(a.getAssessmentBaseId(), a.getTitle(), a.getLastModifiedDate()));
		}
		return assessmentList;
	}

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessmentsByAgent(String orderBy, final String siteAgentId, boolean ascending) {
		// Get the list of assessment
		StringBuilder sb = new StringBuilder("select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.lastModifiedBy, ac.startDate, ac.dueDate, ac.releaseTo) ");
		sb.append("from AssessmentData a, AuthorizationData z, AssessmentAccessControl ac where a.status = :status and ");
		sb.append("a.assessmentBaseId=z.qualifierId and z.functionId = :fid ");
		sb.append("and z.agentIdString = :site ");
		sb.append("and ac.assessmentBase.assessmentBaseId = a.assessmentBaseId order by a.");
		sb.append(orderBy);
		
		String query = sb.toString();
		if (ascending)
			query += " asc";
		else
			query += " desc";

		final String hql = query;
		HibernateCallback<List<AssessmentData>> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setParameter("status", 1);
            q.setParameter("fid", "EDIT_ASSESSMENT");
            q.setParameter("site", siteAgentId);
            return q.list();
        };
		List<AssessmentData> list = getHibernateTemplate().execute(hcb);

		// Get the number of question in each assessment
		HibernateCallback<List<Object[]>> hcb2 = session -> {
            Query q2 = session.createQuery(
            		"select a.assessmentBaseId, count(*) from ItemData i, SectionData s,  AssessmentData a, AuthorizationData z " +
							"where a = s.assessment and s = i.section and a.assessmentBaseId = z.qualifierId and z.functionId = :fid and z.agentIdString = :site " +
							"group by a.assessmentBaseId ");
            q2.setParameter("fid", "EDIT_ASSESSMENT");
            q2.setParameter("site", siteAgentId);
            return q2.list();
        };
		List<Object[]> questionSizeList = getHibernateTemplate().execute(hcb2);
		Map<Object, Object> questionSizeMap = new HashMap<>();
		for (Object[] o : questionSizeList) {
			questionSizeMap.put(o[0], o[1]);
		}
		
		List<AssessmentFacade> assessmentList = new ArrayList<>();
		Long assessmentId;
		String userId = AgentFacade.getAnonymousId();
		GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
		Site site = null;
		Collection<Group> siteGroups = new ArrayList<>();
		Set<String> keysGroupIdsMap = new HashSet<>();
		try {
			site = SiteService.getSite(siteAgentId);
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
		for (AssessmentData a : list) {
			Map<String, String> releaseToGroups = null;
			if (AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(a.getReleaseTo())) {
				assessmentId = a.getAssessmentBaseId();
				releaseToGroups = getReleaseToGroups(siteAgentId, assessmentId);
			}

			AgentFacade agent = new AgentFacade(a.getLastModifiedBy());
			String lastModifiedBy = agent.getDisplayName();
			int questionSize = 0;
			if (questionSizeMap.get(a.getAssessmentBaseId()) != null) {
				questionSize = ((Long) questionSizeMap.get(a.getAssessmentBaseId())).intValue();
			}

			if (releaseToGroups != null) {
				Set<String> keysReleaseToGroups = releaseToGroups.keySet();

				Set<String> commonKeys = new HashSet<>(keysReleaseToGroups);
				commonKeys.retainAll(keysGroupIdsMap);

				if (!commonKeys.isEmpty() || (siteGroups.isEmpty() && service.isUserAbleToGradeAll(site.getId(), userId))) {
					AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(), a.getTitle(), a.getLastModifiedDate(), a.getStartDate(), a.getDueDate(), a.getReleaseTo(), releaseToGroups, lastModifiedBy, questionSize);
					assessmentList.add(f);
				}
			} else {
				AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(), a.getTitle(), a.getLastModifiedDate(), a.getStartDate(), a.getDueDate(), a.getReleaseTo(), releaseToGroups, lastModifiedBy, questionSize);
				assessmentList.add(f);
			}
		}
		return assessmentList;
	}

	public List<AssessmentFacade> getBasicInfoOfAllActiveAssessmentsByAgent(String orderBy, final String siteAgentId) {
		HibernateCallback<List<AssessmentData>> hcb = session -> {
			Query q = session.createQuery(
					"select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate) "
							+ " from AssessmentData a, AuthorizationData z where a.status = :status and "
							+ " a.assessmentBaseId=z.qualifierId and z.functionId = :fid "
							+ " and z.agentIdString = :site order by a." + orderBy

			);
			q.setParameter("status", 1);
            q.setParameter("fid", "EDIT_ASSESSMENT");
            q.setParameter("site", siteAgentId);
            return q.list();
        };
		List<AssessmentData> list = getHibernateTemplate().execute(hcb);

		List<AssessmentFacade> assessmentList = new ArrayList<>();
		for (AssessmentData a : list) {
			assessmentList.add(new AssessmentFacade(a.getAssessmentBaseId(), a.getTitle(), a.getLastModifiedDate()));
		}
		return assessmentList;
	}

	public AssessmentFacade getBasicInfoOfAnAssessment(Long assessmentId) {
		AssessmentData a = (AssessmentData) getHibernateTemplate().load(
				AssessmentData.class, assessmentId);
		AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(), a
				.getTitle(), a.getLastModifiedDate());
		f.setCreatedBy(a.getCreatedBy());
		return f;
	}

	public AssessmentFacade getBasicInfoOfAnAssessmentFromSectionId(Long sectionId) {
		SectionData section = loadSection(sectionId);
		AssessmentData a = (AssessmentData) section.getAssessment();
		AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(), a.getTitle(), a.getLastModifiedDate());
		f.setCreatedBy(a.getCreatedBy());
		return f;
	}

	public List<AssessmentFacade> getSettingsOfAllActiveAssessments(String orderBy) {
		List<AssessmentData> list = (List<AssessmentData>) getHibernateTemplate().findByNamedParam(
				"from AssessmentData a where a.status = :status order by a." + orderBy, "status", 1);
		// IMPORTANT:
		// 1. we do not want any Section info, so set loadSection to false
		// 2. We have also declared SectionData as lazy loading. If loadSection
		// is set
		// to true, we will see null pointer
		List<AssessmentFacade> assessmentList = new ArrayList<>();
		Boolean loadSection = Boolean.FALSE;
		for (AssessmentData a : list) {
			assessmentList.add(new AssessmentFacade(a, loadSection));
		}
		return assessmentList;
	}

	public List<AssessmentFacade> getAllAssessments(int pageSize, int pageNumber, String orderBy) {
		PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().getPagingUtilQueries();
		List<AssessmentData> pageList = pagingUtilQueries.getAll(pageSize, pageNumber, "from AssessmentData a order by a." + orderBy);
		List<AssessmentFacade> assessmentList = new ArrayList<>();
		for (AssessmentData a : pageList) {
			assessmentList.add(new AssessmentFacade(a));
		}
		return assessmentList;
	}

	public int getQuestionSize(final Long assessmentId) {
		HibernateCallback<Number> hcb = session -> (Number) session
				.createQuery("select count(i) from ItemData i, SectionData s,  AssessmentData a where a = s.assessment and s = i.section and a.assessmentBaseId = :id")
				.setParameter("id", assessmentId)
				.uniqueResult();
		return getHibernateTemplate().execute(hcb).intValue();
	}

	public List getQuestionsIdList(final Long assessmentId) {
		HibernateCallback<List<Long>> hcb = session -> session
				.createQuery("select i.itemId from ItemData i, SectionData s,  AssessmentData a where a = s.assessment and s = i.section and a.assessmentBaseId = :id")
				.setParameter("id", assessmentId.longValue())
				.list();
		return getHibernateTemplate().execute(hcb);
	}

	public void deleteAllSecuredIP(AssessmentIfc assessment) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				Long assessmentId = assessment.getAssessmentId();
				List ip = getHibernateTemplate()
						.findByNamedParam("from SecuredIPAddress s where s.assessment.assessmentBaseId = :id", "id", assessmentId);
				if (ip.size() > 0) {
					SecuredIPAddress s = (SecuredIPAddress) ip.get(0);
					AssessmentData a = (AssessmentData) s.getAssessment();
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

	public void saveOrUpdate(AssessmentFacade assessment) {
		AssessmentData data = (AssessmentData) assessment.getData();
		data.setLastModifiedBy(AgentFacade.getAgentString());
		data.setLastModifiedDate(new Date());
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		data.setCategoryId(assessment.getCategoryId());
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save new settings: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public void deleteAllMetaData(AssessmentBaseIfc t) {

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				List metadatas = getHibernateTemplate()
						.findByNamedParam("from AssessmentMetaData a where a.assessment.assessmentBaseId = :id", "id", t.getAssessmentBaseId());
				if (metadatas.size() > 0) {
					AssessmentMetaDataIfc m = (AssessmentMetaDataIfc) metadatas.get(0);
					AssessmentBaseIfc a = (AssessmentBaseIfc) m.getAssessment();
					a.setAssessmentMetaDataSet(new HashSet());
					getHibernateTemplate().deleteAll(metadatas);
					retryCount = 0;
				} else
					retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleting metadata: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public void saveOrUpdate(final AssessmentTemplateData template) {
		template.setLastModifiedBy(AgentFacade.getAgentString());
		template.setLastModifiedDate(new Date());
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(template);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem save or update template: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public void deleteTemplate(Long templateId) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().delete(
						getAssessmentTemplate(templateId).getData());
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem delete template: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public SectionFacade addSection(Long assessmentId) {
		// #1 - get the assessment and attach teh new section to it
		// we are working with Data instead of Facade in this method but should
		// return
		// SectionFacade at the end
		AssessmentData assessment = loadAssessment(assessmentId);
		// lazy loading on sectionSet, so need to initialize it
		Set sectionSet = getSectionSetForAssessment(assessment);
		assessment.setSectionSet(sectionSet);

		// #2 - will called the section "Section d" here d is the total no. of
		// section in
		// this assessment

		// #2 section has no default name - per Marc's new mockup

		SectionData section = new SectionData(null,
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
		return new SectionFacade(section);
	}

	public SectionFacade getSection(Long sectionId) {
		SectionData section = (SectionData) getHibernateTemplate().load(
				SectionData.class, sectionId);
		return new SectionFacade(section);
	}

	public void removeSection(Long sectionId) {
		SectionData section = loadSection(sectionId);
		if (section != null) {
			// need to check that items in the selected section is not
			// associated
			// with any pool
			QuestionPoolService qpService = new QuestionPoolService();
			Map h = qpService.getQuestionPoolItemMap();
			checkForQuestionPoolItem(section, h);

			AssessmentData assessment = (AssessmentData) section
					.getAssessment();
			assessment.setLastModifiedBy(AgentFacade.getAgentString());
			assessment.setLastModifiedDate(new Date());

			Set itemSet = section.getItemSet();
			Iterator iter1 = itemSet.iterator();
			RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
			while (iter1.hasNext()) {
				ItemDataIfc item = (ItemDataIfc) iter1.next();
				// delete rubric association
				String associationId = assessment.getAssessmentId() + "." + item.getItemId();
				rubricsService.deleteRubricAssociationsByItemIdPrefix(associationId, RubricsConstants.RBCS_TOOL_SAMIGO);
			}

			// lazy loading on sectionSet, so need to initialize it
			Set sectionSet = getSectionSetForAssessment(assessment);
			assessment.setSectionSet(sectionSet);
			List sections = assessment.getSectionArraySorted();
			// need to reorder the remaining section
			Set set = new HashSet();
			int count = 1;
			for (int i = 0; i < sections.size(); i++) {
				SectionData s = (SectionData) sections.get(i);
				if (!(s.getSectionId()).equals(section.getSectionId())) {
					s.setSequence(Integer.valueOf(count++));
					set.add(s);
				}
			}
			assessment.setSectionSet(set);
			int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
			while (retryCount > 0) {
				try {
					getHibernateTemplate().update(assessment); // sections
					// reordered
					retryCount = 0;
				} catch (Exception e) {
					log.warn("problem updating asssessment: " + e.getMessage());
					retryCount = PersistenceService.getInstance().getPersistenceHelper()
							.retryDeadlock(e, retryCount);
				}
			}

			// get list of attachment in section
			AssessmentService service = new AssessmentService();
			List sectionAttachmentList = service
					.getSectionResourceIdList(section);
			service.deleteResources(sectionAttachmentList);

			// remove section
			retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
			while (retryCount > 0) {
				try {
					getHibernateTemplate().delete(section);
					retryCount = 0;
				} catch (Exception e) {
					log.warn("problem deletint section: " + e.getMessage());
					retryCount = PersistenceService.getInstance().getPersistenceHelper()
							.retryDeadlock(e, retryCount);
				}
			}
		}
	}

	public SectionData loadSection(Long sectionId) {
		return (SectionData) getHibernateTemplate().load(SectionData.class,
				sectionId);
	}

	public void saveOrUpdateSection(SectionFacade section) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
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

	/**
	 * This method return a list of ItemData belings to the section with the
	 * given sectionId
	 * 
	 * @param sectionId
	 * @return
	 */
	private List loadAllItems(Long sectionId) {
		return getHibernateTemplate().find(
				"from ItemData i where i.section.sectionId=" + sectionId);
	}

	/**
	 * This method move a set of questions form one section to another
	 * 
	 * @param sourceSectionId
	 * @param destSectionId
	 */
	public void moveAllItems(Long sourceSectionId, Long destSectionId) {
		SectionData destSection = loadSection(destSectionId);
		List list = loadAllItems(sourceSectionId);
		Set set = destSection.getItemSet();
		if (set == null) {
			set = new HashSet();
		}
		int itemNum = set.size();
		for (int i = 0; i < list.size(); i++) {
			ItemDataIfc a = (ItemDataIfc) list.get(i);
			a.setSection(destSection);
			a.setSequence(Integer.valueOf(++itemNum));
			set.add(a);
		}
		destSection.setItemSet(set);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().update(destSection);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem updating section: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	/**
	 * This method remove a set of questions form one section that is random
	 * draw
	 * 
	 * @param sourceSectionId
	 */
	public void removeAllItems(Long sourceSectionId) {
		SectionData section = loadSection(sourceSectionId);

		AssessmentData assessment = (AssessmentData) section.getAssessment();
		assessment.setLastModifiedBy(AgentFacade.getAgentString());
		assessment.setLastModifiedDate(new Date());

		Set itemSet = section.getItemSet();
		// HashSet newItemSet = new HashSet();
		Iterator iter = itemSet.iterator();
		while (iter.hasNext()) {
			ItemData item = (ItemData) iter.next();
			// item belongs to a pool, set section=null so
			// item won't get deleted during section deletion
			item.setSection(null);
			int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
			while (retryCount > 0) {
				try {
					getHibernateTemplate().update(item);
					retryCount = 0;
				} catch (Exception e) {
					log.warn("problem updating item: " + e.getMessage());
					retryCount = PersistenceService.getInstance().getPersistenceHelper()
							.retryDeadlock(e, retryCount);
				}
			}
		}
		// update assessment info (LastModifiedBy and LastModifiedDate)
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().update(assessment); // sections
				// reordered
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem updating asssessment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public List<AssessmentTemplateFacade> getBasicInfoOfAllActiveAssessmentTemplates(String orderBy) {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;

		HibernateCallback<List<AssessmentTemplateData>> hcb = session -> {
            Query q = session.createQuery(
            		"select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.typeId)" +
							" from AssessmentTemplateData a where a.status = 1 and (a.assessmentBaseId = :id or" +
							" a.createdBy = :agent or typeId = :type) order by a." + orderBy);
            q.setParameter("id", 1L);
            q.setParameter("agent", agent);
            q.setParameter("type", typeId);
            return q.list();
        };
		List<AssessmentTemplateData> list = getHibernateTemplate().execute(hcb);

		List<AssessmentTemplateFacade> assessmentList = new ArrayList<>();
		for (AssessmentTemplateData a : list) {
			assessmentList.add(new AssessmentTemplateFacade(a.getAssessmentBaseId(), a.getTitle(), a.getLastModifiedDate(), a.getTypeId()));
		}
		return assessmentList;
	}

	public void checkForQuestionPoolItem(AssessmentData assessment, Map qpItemHash) {
		Set sectionSet = getSectionSetForAssessment(assessment);
		Iterator iter = sectionSet.iterator();
		while (iter.hasNext()) {
			SectionData s = (SectionData) iter.next();
			checkForQuestionPoolItem(s, qpItemHash);
		}
	}

	public void checkForQuestionPoolItem(SectionData section, Map qpItemHash) {
		Set itemSet = section.getItemSet();
		Set newItemSet = new HashSet();
		Iterator iter = itemSet.iterator();
		while (iter.hasNext()) {
			ItemData item = (ItemData) iter.next();
			if (qpItemHash.get(item.getItemId()) != null) {
				// item belongs to a pool, in this case, set section=null so
				// item won't get deleted during section deletion
				item.setSection(null);
				int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
				while (retryCount > 0) {
					try {
						getHibernateTemplate().update(item);
						retryCount = 0;
					} catch (Exception e) {
						log.warn("problem updating item: " + e.getMessage());
						retryCount = PersistenceService.getInstance().getPersistenceHelper()
								.retryDeadlock(e, retryCount);
					}
				}
			} else {
				newItemSet.add(item);
			}
		}
		section.setItemSet(newItemSet);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().update(section);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem updating section: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public boolean assessmentTitleIsUnique(final Long assessmentBaseId, String title, Boolean isTemplate) {
		return assessmentTitleIsUnique(assessmentBaseId, title, isTemplate, AgentFacade.getCurrentSiteId());
	}

	public boolean assessmentTitleIsUnique(final Long assessmentBaseId, String title, Boolean isTemplate, final String siteId) {
		title = title.trim();
		final String agentString = AgentFacade.getAgentString();
		List<AssessmentBaseData> list;
		boolean isUnique = true;
		if (isTemplate) { // templates are person scoped
			final String titlef = title;
			HibernateCallback<List<AssessmentBaseData>> hcb = session -> {
                Query q = session.createQuery(
                		"select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate) " +
								"from AssessmentTemplateData a, AuthorizationData z where " +
								"a.title = :title and a.assessmentBaseId != :id and a.createdBy = :agent and a.status = :status");
                q.setParameter("title", titlef);
                q.setParameter("id", assessmentBaseId);
                q.setParameter("agent", agentString);
                q.setParameter("status", 1);
                return q.list();
            };
			list = getHibernateTemplate().execute(hcb);
		} else { // assessments are site scoped
			final String titlef = title;
			HibernateCallback<List<AssessmentBaseData>> hcb = session -> {
                Query q = session.createQuery(
                		"select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate)" +
								" from AssessmentData a, AuthorizationData z where " +
								" a.title = :title and a.assessmentBaseId != :id and z.functionId = :fid and " +
								" a.assessmentBaseId = z.qualifierId and z.agentIdString = :site and a.status = :status");
                q.setParameter("title", titlef);
                q.setParameter("id", assessmentBaseId);
                q.setParameter("fid", "EDIT_ASSESSMENT");
                q.setParameter("site", siteId);
                q.setParameter("status", 1);
                return q.list();
            };
			list = getHibernateTemplate().execute(hcb);
		}
		for (AssessmentBaseData a : list) {
			if ((title).equals(a.getTitle().trim())) {
				isUnique = false;
				break;
			}
		}
		return isUnique;
	}

	public List<AssessmentData> getAssessmentByTemplate(final Long templateId) {
		HibernateCallback<List<AssessmentData>> hcb = session -> session
				.createQuery("select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate) "
					+ "from AssessmentData a where a.assessmentTemplateId = :id")
					.setParameter("id", templateId)
					.list();
		return getHibernateTemplate().execute(hcb);
	}

	public List getDefaultMetaDataSet() {
		HibernateCallback<List<AssessmentMetaData>> hcb = session -> session.createQuery("from AssessmentMetaData m where m.assessment.assessmentBaseId = :id")
				.setParameter("id", 1L)
				.list();
		return getHibernateTemplate().execute(hcb);
	}

	public long fileSizeInKB(long fileSize) {
		return fileSize / 1024;
	}

	public String getRelativePath(String url, String protocol) {
		// replace whitespace with %20
		url = replaceSpace(url);
		int index = url.lastIndexOf(protocol);
		if (index == 0) {
			url = url.substring(protocol.length());
		}
		return url;
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

	public void updateAssessmentLastModifiedInfo(
			AssessmentFacade assessment) {
		AssessmentData data = (AssessmentData) assessment.getData();
		data.setLastModifiedBy(AgentFacade.getAgentString());
		data.setLastModifiedDate(new Date());
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().update(data);
				retryCount = 0;
			} catch (Exception e) {
				log.warn("problem update assessment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item, String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
		ItemAttachmentIfc attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				if (isEditPendingAssessmentFlow) {
					attach = new ItemAttachment();
				}
				else {
					attach = new PublishedItemAttachment();
				}
				attach.setItem(item);
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
				attach.setStatus(ItemAttachmentIfc.ACTIVE_STATUS);
				attach.setCreatedBy(p.getProperty(p.getNamePropCreator()));
				attach.setCreatedDate(new Date());
				attach.setLastModifiedBy(p.getProperty(p
						.getNamePropModifiedBy()));
				attach.setLastModifiedDate(new Date());
				attach.setLocation(getRelativePath(cr.getUrl(), protocol));
				// getHibernateTemplate().save(attach);
			}
		} catch (PermissionException | IdUnusedException | TypeException pe) {
			log.warn(pe.getMessage(), pe);
		}
		return attach;
	}

	public ItemTextAttachmentIfc createItemTextAttachment(ItemTextIfc itemText, String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
		ItemTextAttachmentIfc attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				if (isEditPendingAssessmentFlow) {
					attach = new ItemTextAttachment();
				}
				else {
					attach = new PublishedItemTextAttachment();
				}
				attach.setItemText(itemText);
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
				attach.setStatus(ItemAttachmentIfc.ACTIVE_STATUS);
				attach.setCreatedBy(p.getProperty(p.getNamePropCreator()));
				attach.setCreatedDate(new Date());
				attach.setLastModifiedBy(p.getProperty(p
						.getNamePropModifiedBy()));
				attach.setLastModifiedDate(new Date());
				attach.setLocation(getRelativePath(cr.getUrl(), protocol));
				// getHibernateTemplate().save(attach);
			}
		} catch (PermissionException | IdUnusedException | TypeException pe) {
			log.warn(pe.getMessage(), pe);
		}
		return attach;
	}

	public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section, String resourceId, String filename, String protocol) {
		SectionAttachment attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				attach = new SectionAttachment();
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
			log.warn(pe.getMessage(), pe);
		}

		return attach;
	}

	public void removeSectionAttachment(Long sectionAttachmentId) {
		SectionAttachment sectionAttachment = getHibernateTemplate().load(SectionAttachment.class, sectionAttachmentId);
		SectionDataIfc section = sectionAttachment.getSection();
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
				log.warn("problem delete sectionAttachment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public AssessmentAttachmentIfc createAssessmentAttachment(AssessmentIfc assessment, String resourceId, String filename, String protocol) {
		AssessmentAttachment attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				attach = new AssessmentAttachment();
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
			log.warn(pe.getMessage(), pe);
		}
		return attach;
	}

	public void removeAssessmentAttachment(Long assessmentAttachmentId) {
		AssessmentAttachment assessmentAttachment = getHibernateTemplate().load(AssessmentAttachment.class, assessmentAttachmentId);
		AssessmentIfc assessment = assessmentAttachment.getAssessment();
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
				log.warn("problem delete assessmentAttachment: {}", e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}

	public AttachmentData createEmailAttachment(String resourceId, String filename, String protocol) {
		AttachmentData attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();
				attach = new AttachmentData();
				attach.setResourceId(resourceId);
				attach.setMimeType(cr.getContentType());
				// we want to display kb, so divide by 1000 and round
				// the result
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
			}
		} catch (PermissionException | IdUnusedException | TypeException pe) {
			log.warn(pe.getMessage(), pe);
		}
		return attach;
	}

	public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
	    for (AttachmentIfc attachment : list) {
	        getHibernateTemplate().saveOrUpdate(attachment);
	    }
	}

	public List<AssessmentData> getAllActiveAssessmentsByAgent(final String siteAgentId) {
		HibernateCallback<List<AssessmentData>> hcb = session -> session.createQuery(
            		"select a from AssessmentData a,AuthorizationData z where a.status = :status and " +
							"a.assessmentBaseId=z.qualifierId and z.functionId = :fid and z.agentIdString = :site")
				.setParameter("status", 1)
				.setParameter("fid", "EDIT_ASSESSMENT")
				.setParameter("site", siteAgentId)
				.list();
		return getHibernateTemplate().execute(hcb);
	}

	@Override
	public void copyAllAssessments(String fromContext, String toContext, List<String> ids, Map<String,String> transversalMap) {
		List<AssessmentData> list = getAllActiveAssessmentsByAgent(fromContext);

		if (CollectionUtils.isNotEmpty(ids)) {
			list = list.stream().filter(ad -> ids.contains(ad.getAssessmentId().toString()))
				.collect(Collectors.toList());
		}
		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		// Parent method has a SecurityAdvisor to allow this operation to complete regardless of whether user has rubrics.editor permission
		RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
		List<RubricTransferBean> siteRubricList = rubricsService.getRubricsForSite(fromContext);
		List<String> rubricsInUseAssociationList = new ArrayList<>();
		for (RubricTransferBean rubric : siteRubricList) {
			Long rubricId = rubric.getId();
			List<ToolItemRubricAssociation> rubricAssociationList = rubricsService.getRubricAssociationsByRubricAndTool(rubricId, RubricsConstants.RBCS_TOOL_SAMIGO);
			for (ToolItemRubricAssociation itemRubricAssociation : rubricAssociationList) {
				log.debug("Rubric association found {} for the rubric {}.", itemRubricAssociation.getItemId(), rubricId);
				rubricsInUseAssociationList.add(itemRubricAssociation.getItemId());
			}
		}

		// Preload source assessment ids so release-to-groups authz can be fetched in one query.
		List<String> sourceAssessmentIds = list.stream()
			.map(AssessmentData::getAssessmentBaseId)
			.filter(id -> id != null)
			.map(String::valueOf)
			.collect(Collectors.toList());
		Map<String, Map<String, String>> releaseToGroupsByAssessmentId = getReleaseToGroupsByAssessmentIds(fromContext, sourceAssessmentIds);
		Set<String> usedTitles = getActiveAssessmentTitlesForSite(toContext);

		Site sourceSite = null;
		Site targetSite = null;
		try {
			sourceSite = SiteService.getSite(fromContext);
			targetSite = SiteService.getSite(toContext);
		} catch (IdUnusedException ex) {
			log.error("Cannot find a site with id {} or {}", fromContext, toContext);
			return;
		}
		AuthzQueriesFacadeAPI authzQueriesFacadeAPI = PersistenceService.getInstance().getAuthzQueriesFacade();

		int copiedCount = 0;
		for (AssessmentData sourceAssessment : list) {
			AssessmentData copiedAssessment = prepareAssessment(sourceAssessment, ServerConfigurationService.getServerUrl(), toContext, true);
			String uniqueTitle = getUniqueImportedTitle(copiedAssessment.getTitle(), usedTitles);
			copiedAssessment.setTitle(uniqueTitle);
			getHibernateTemplate().saveOrUpdate(copiedAssessment);

			String sourceAssessmentId = (sourceAssessment.getAssessmentBaseId() == null) ? null : sourceAssessment.getAssessmentBaseId().toString();
			Map<String, String> releaseToGroups = releaseToGroupsByAssessmentId.getOrDefault(sourceAssessmentId, Collections.emptyMap());

			// authorization
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.createAuthorization(toContext, "EDIT_ASSESSMENT", copiedAssessment.getAssessmentId().toString());

			Map assessmentMetaDataMap = copiedAssessment.getAssessmentMetaDataMap();
			if (!assessmentMetaDataMap.containsKey("markForReview_isInstructorEditable")) {
				copiedAssessment.addAssessmentMetaData("markForReview_isInstructorEditable", "true");
				copiedAssessment.getAssessmentAccessControl().setMarkForReview(1);
			}

			if (!releaseToGroups.isEmpty()) {
				boolean siteChanged = false;
				Collection<Group> targetGroups = targetSite.getGroups();

				Long assessmentId = copiedAssessment.getAssessmentBaseId();
				Set<String> groupsAuthorized = new HashSet<>();

				for (Entry<String, String> groupId : releaseToGroups.entrySet()) {
					Group sourceGroup = sourceSite.getGroup(groupId.getKey());
					if (sourceGroup == null) {
						continue;
					}
					Optional<Group> existingGroup = targetGroups.stream().filter(g -> StringUtils.equals(g.getTitle(), sourceGroup.getTitle())).findAny();
					Group targetGroup;
					if (existingGroup.isPresent()) {
						groupsAuthorized.add(existingGroup.get().getId());
                    } else {
						// create group
						targetGroup = targetSite.addGroup();
						targetGroup.setTitle(sourceGroup.getTitle());
						targetGroup.setDescription(sourceGroup.getDescription());
						targetGroup.getProperties().addProperty("group_prop_wsetup_created", Boolean.TRUE.toString());
						groupsAuthorized.add(targetGroup.getId());
						siteChanged = true;
                    }
                }
				if (siteChanged) {
					try {
						SiteService.save(targetSite);
					} catch (IdUnusedException ex) {
						log.error("Cannot find site [{}] while saving imported groups", toContext);
					} catch (PermissionException ex) {
						log.error("No permission to save site [{}] while importing groups", toContext);
					}
				}
				for (String group : groupsAuthorized) {
					authzQueriesFacadeAPI.createAuthorization(group, "TAKE_ASSESSMENT", assessmentId.toString());
				}
			}

			// reset PARTID in ItemMetaData to the section of the newly created section
			Set<SectionData> sectionSet = copiedAssessment.getSectionSet();
			Iterator sectionIter = sectionSet.iterator();
			while (sectionIter.hasNext()) {
				SectionData section = (SectionData) sectionIter.next();
				Set itemSet = section.getItemSet();
				Iterator itemIter = itemSet.iterator();
				while (itemIter.hasNext()) {
					ItemData item = (ItemData) itemIter.next();
					//We use this place to add the saveItem Events used by the search index to index all the new questions
					EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + toContext + "/saved itemId=" + item.getItemId().toString(), true));
					if (sourceAssessmentId != null) {
						String associationId = sourceAssessmentId + "." + item.getOriginalItemId();
						if (rubricsInUseAssociationList.contains(associationId)) {
							log.debug("Rubric association matched with a question by item {}.", associationId);
							transversalMap.put(
								ItemEntityProvider.ENTITY_PREFIX + "/" + associationId,
								ItemEntityProvider.ENTITY_PREFIX + "/" + copiedAssessment.getAssessmentBaseId() + "." + item.getItemId()
							);
						}
					}
					Set<ItemMetaDataIfc> itemMetaDataSet = item.getItemMetaDataSet();
                    for (ItemMetaDataIfc itemMetaDataIfc : itemMetaDataSet) {
                        ItemMetaData itemMetaData = (ItemMetaData) itemMetaDataIfc;
                        if (itemMetaData.getLabel() != null && itemMetaData.getLabel().equals(ItemMetaDataIfc.PARTID)) {
                            log.debug("itemMetaData sectionId = {}", section.getSectionId());
                            itemMetaData.setEntry(section.getSectionId().toString());
                        }
                    }
				}
			}

			if (sourceAssessment.getAssessmentBaseId() != null && copiedAssessment.getAssessmentBaseId() != null) {
				String oldRef = CoreAssessmentEntityProvider.ENTITY_PREFIX + "/" + sourceAssessment.getAssessmentBaseId();
				transversalMap.put(oldRef, CoreAssessmentEntityProvider.ENTITY_PREFIX + "/" + copiedAssessment.getAssessmentBaseId());
			}

			copiedCount++;
			if (copiedCount % 10 == 0) {
				getHibernateTemplate().flush();
			}
		}
		getHibernateTemplate().flush();

	}
	
	public void copyAssessment(String assessmentId, String appendCopyTitle) {
		AssessmentData assessmentData = loadAssessment(Long.valueOf(assessmentId));
		assessmentData.setSectionSet(getSectionSetForAssessment(assessmentData));
		RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
		AssessmentData newAssessmentData = prepareAssessment(assessmentData, ServerConfigurationService.getServerUrl(), AgentFacade.getCurrentSiteId(), false);
		updateTitleForCopy(newAssessmentData, appendCopyTitle);
		getHibernateTemplate().saveOrUpdate(newAssessmentData);
		
		// authorization
		PersistenceService.getInstance().getAuthzQueriesFacade()
		.createAuthorization(AgentFacade.getCurrentSiteId(), "EDIT_ASSESSMENT", newAssessmentData.getAssessmentId().toString());

		// reset PARTID in ItemMetaData to the section of the newly created section
		Set sectionSet = newAssessmentData.getSectionSet();
		Iterator sectionIter = sectionSet.iterator();
		while (sectionIter.hasNext()) {
			SectionData section = (SectionData) sectionIter.next();
			Set itemSet = section.getItemSet();
			Iterator itemIter = itemSet.iterator();
			while (itemIter.hasNext()) {
				ItemData item = (ItemData) itemIter.next();
				
				//copy rubrics
				try {
					String associationId = assessmentId + "." + item.getOriginalItemId();
					
					Optional<ToolItemRubricAssociation> rubricAssociation = rubricsService.getRubricAssociation(RubricsConstants.RBCS_TOOL_SAMIGO, associationId);
					if (rubricAssociation.isPresent()) {
						rubricsService.saveRubricAssociation(RubricsConstants.RBCS_TOOL_SAMIGO, newAssessmentData.getAssessmentId()+ "." + item.getItemId(), rubricAssociation.get().getFormattedAssociation());
					}
				} catch(Exception e){
					log.error("Error while trying to duplicate Rubrics: {} ", e.getMessage());
				}
					
				//We use this place to add the saveItem Events used by the search index to index all the new questions
				EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved itemId=" + item.getItemId().toString(), true));
				Set itemMetaDataSet = item.getItemMetaDataSet();
				Iterator itemMetaDataIter = itemMetaDataSet.iterator();
				while (itemMetaDataIter.hasNext()) {
					ItemMetaData itemMetaData = (ItemMetaData) itemMetaDataIter.next();
					if (itemMetaData.getLabel() != null && itemMetaData.getLabel().equals(ItemMetaDataIfc.PARTID)) {
						log.debug("sectionId = " + section.getSectionId());
						itemMetaData.setEntry(section.getSectionId().toString());
					}
				}
			}
		}
		getHibernateTemplate().saveOrUpdate(newAssessmentData);
	}

    private void updateTitleForCopy(AssessmentData assessmentData, String appendCopyTitle){
    	StringBuffer sb = new StringBuffer(assessmentData.getTitle());
    	sb.append(" ");
    	sb.append(appendCopyTitle);
    	if(sb.length() >= assessmentData.TITLE_LENGTH){ //title max size
    		String appendCopyText = "... "+appendCopyTitle;
    		String titleCut = sb.substring(0, assessmentData.TITLE_LENGTH-appendCopyText.length()-1); //cut until size needed to add ellipsis and copyTitle without exceed DB field size
    		log.debug("titleCut = "+titleCut);
    		sb = new StringBuffer(titleCut);
    		sb.append(appendCopyText);
    	}
    	String newTitle = getNewAssessmentTitleForCopy(sb.toString());
    	assessmentData.setTitle(newTitle);
	  }
	
    private String getNewAssessmentTitleForCopy(String title) {
    	title = title.trim();
    	log.debug(title);
    	final String currentSiteId = AgentFacade.getCurrentSiteId();
    	final String titlef = title + "%";
    	HibernateCallback<List<String>> hcb = session -> {
            Query q = session.createQuery(
            		"select a.title from AssessmentData a, AuthorizationData z " +
							"where a.title like :title and z.functionId='EDIT_ASSESSMENT' " +
							"and a.assessmentBaseId=z.qualifierId and z.agentIdString = :site"
			);
            q.setParameter("title", titlef);
            q.setParameter("site", currentSiteId);
            return q.list();
        };
    	List<String> list = getHibernateTemplate().execute(hcb);

    	int startIndex = title.length();
    	int maxNumCopy = 0;
    	if (!list.isEmpty()) {
    		// query in mysql & hsqldb are not case sensitive, check that title
    		// found is indeed what we
    		// are looking
    		for (String existingTitle : list) {
    			if (existingTitle.startsWith(title)) {
    				try{
    					int numCopy = Integer.parseInt(existingTitle.substring(startIndex));
    					if (numCopy > maxNumCopy) {
    						maxNumCopy = numCopy;
    					}
    				}
    				catch(NumberFormatException e){
    					log.error("existingTitle = {}, title = {}, startIndex = {}, error message: {}", existingTitle, title, startIndex, e.getMessage());
    				}
    			}
    		}
    	}
    	log.debug("maxNumCopy = {}", maxNumCopy);
    	int nextNumCopy = maxNumCopy + 1;
		return title + nextNumCopy;
    }
    
    public AssessmentData prepareAssessment(AssessmentData a, String protocol, String toContext, boolean originalDateAndModifiedBy) {

		AssessmentData newAssessment = null;
		if (originalDateAndModifiedBy) {
			newAssessment = new AssessmentData(new Long("0"), a
				.getTitle(), a.getDescription(), a.getComments(), a.getAssessmentTemplateId(),
				TypeFacade.HOMEWORK, a.getInstructorNotification(), a
						.getTesteeNotification(), a.getMultipartAllowed(), a
						.getStatus(), a.getCreatedBy(), a.getCreatedDate(), a
						.getLastModifiedBy(), a.getLastModifiedDate());
		} else {
			newAssessment = new AssessmentData(new Long("0"), a
				.getTitle(), a.getDescription(), a.getComments(), a.getAssessmentTemplateId(),
				TypeFacade.HOMEWORK, a.getInstructorNotification(), a
						.getTesteeNotification(), a.getMultipartAllowed(), a
						.getStatus(), AgentFacade.getAgentString(), new Date(), 
						AgentFacade.getAgentString(), new Date());
		}
		// section set
		Set newSectionSet = prepareSectionSet(newAssessment, a.getSectionSet(),
				protocol, toContext);
		newAssessment.setSectionSet(newSectionSet);
		// access control
		AssessmentAccessControl newAccessControl = prepareAssessmentAccessControl(
				newAssessment, (AssessmentAccessControl) a
						.getAssessmentAccessControl());
		newAssessment.setAssessmentAccessControl(newAccessControl);
		// evaluation model
		EvaluationModel newEvaluationModel = prepareEvaluationModel(
				newAssessment, (EvaluationModel) a.getEvaluationModel());
		newAssessment.setEvaluationModel(newEvaluationModel);
		// feedback
		AssessmentFeedback newFeedback = prepareAssessmentFeedback(
				newAssessment, (AssessmentFeedback) a.getAssessmentFeedback());
		newAssessment.setAssessmentFeedback(newFeedback);
		// metadata
		Set<AssessmentMetaData> newMetaDataSet = prepareAssessmentMetaDataSet(newAssessment, a.getAssessmentMetaDataSet());

		final String releaseTo = newAccessControl.getReleaseTo();
		switch (releaseTo) {
			case AssessmentAccessControl.ANONYMOUS_USERS:
				break;
			// case AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS:
			default:
				// if not anonymous or group then set releaseTo to the site title
				if (toContext != null) {
					try {
						Site site = SiteService.getSite(toContext);
						newAccessControl.setReleaseTo(site.getTitle());
					} catch (IdUnusedException e) {
						log.debug("Site not found [{}], {}", toContext, e.toString());
					}
				}
				break;
		}

		log.debug(" metadata set" + a.getAssessmentMetaDataSet());
		log.debug(" new metadata set" + newMetaDataSet);
		newAssessment.setAssessmentMetaDataSet(newMetaDataSet);

		// IPAddress
		Set newIPSet = prepareSecuredIPSet(newAssessment, a
				.getSecuredIPAddressSet());
		newAssessment.setSecuredIPAddressSet(newIPSet);
		// attachmentSet
		Set newAssessmentAttachmentSet = prepareAssessmentAttachmentSet(
				newAssessment, a.getAssessmentAttachmentSet(), protocol, toContext);
		newAssessment.setAssessmentAttachmentSet(newAssessmentAttachmentSet);

		return newAssessment;
	}

	public AssessmentData prepareAssessment(AssessmentData a, String protocol) {
		return prepareAssessment(a, protocol, null, true);
	}
	
	public AssessmentFeedback prepareAssessmentFeedback(AssessmentData p,
			AssessmentFeedback a) {
		if (a == null) {
			return null;
		}
		AssessmentFeedback newFeedback = new AssessmentFeedback(a
				.getFeedbackDelivery(), a.getFeedbackComponentOption(),a.getFeedbackAuthoring(), a
				.getEditComponents(), a.getShowQuestionText(), a
				.getShowStudentResponse(), a.getShowCorrectResponse(), a
				.getShowStudentScore(), a.getShowStudentQuestionScore(), a
				.getShowQuestionLevelFeedback(), a
				.getShowSelectionLevelFeedback(), a.getShowGraderComments(), a
				.getShowStatistics(), a.getShowCorrection());
		newFeedback.setAssessmentBase(p);
		return newFeedback;
	}

	public AssessmentAccessControl prepareAssessmentAccessControl(
			AssessmentData p, AssessmentAccessControl a) {
		if (a == null) {
			return new AssessmentAccessControl();
		}
		AssessmentAccessControl newAccessControl = new AssessmentAccessControl(
				a.getSubmissionsAllowed(), a.getSubmissionsSaved(), a
						.getAssessmentFormat(), a.getBookMarkingItem(), a
						.getTimeLimit(), a.getTimedAssessment(), a
						.getRetryAllowed(), a.getLateHandling(), a.getInstructorNotification(),a
						.getStartDate(), a.getDueDate(), a.getScoreDate(), a
						.getFeedbackDate(), a.getRetractDate(), a
						.getAutoSubmit(), a.getItemNavigation(), a
						.getItemNumbering(), a.getDisplayScoreDuringAssessments(), a.getSubmissionMessage(), a
						.getReleaseTo());
		newAccessControl.setPassword(a.getPassword());
		newAccessControl.setFinalPageUrl(a.getFinalPageUrl());
		newAccessControl.setUnlimitedSubmissions(a.getUnlimitedSubmissions());
		newAccessControl.setAssessmentBase(p);
		newAccessControl.setMarkForReview(a.getMarkForReview());
		newAccessControl.setHonorPledge(a.getHonorPledge());
		newAccessControl.setFeedbackEndDate(a.getFeedbackEndDate());
		newAccessControl.setFeedbackScoreThreshold(a.getFeedbackScoreThreshold());
		return newAccessControl;
	}

	public EvaluationModel prepareEvaluationModel(AssessmentData p,
			EvaluationModel e) {
		if (e == null) {
			return null;
		}
		EvaluationModel newEvaluationModel = new EvaluationModel(e
				.getEvaluationComponents(), e.getScoringType(), e
				.getNumericModelId(), e.getFixedTotalScore(), e
				.getGradeAvailable(), e.getIsStudentIdPublic(), e
				.getAnonymousGrading(), e.getAutoScoring(), e.getToGradeBook());
		newEvaluationModel.setAssessmentBase(p);
		return newEvaluationModel;
	}

	public Set<AssessmentMetaData> prepareAssessmentMetaDataSet(AssessmentData p, Set<AssessmentMetaData> metaDataSet) {
		return metaDataSet.stream().map(m -> new AssessmentMetaData(p, m.getLabel(), m.getEntry())).collect(Collectors.toSet());
	}

	public Set prepareSecuredIPSet(AssessmentData p, Set ipSet) {
		HashSet h = new HashSet();
		Iterator i = ipSet.iterator();
		while (i.hasNext()) {
			SecuredIPAddress ip = (SecuredIPAddress) i.next();
			SecuredIPAddress newIP = new SecuredIPAddress(p, ip.getHostname(),
					ip.getIpAddress());
			h.add(newIP);
		}
		return h;
	}

	public Set prepareSectionSet(AssessmentData newAssessment, Set sectionSet,
			String protocol, String toContext) {
		log.debug("new section size = " + sectionSet.size());
		HashSet h = new HashSet();
		Iterator i = sectionSet.iterator();
		while (i.hasNext()) {
			SectionData section = (SectionData) i.next();

			// TODO note: 4/28 need to check if a part is random draw , if it is
			// then need to add questions from pool to this section, at this
			// point,

			SectionData newSection = new SectionData(section.getDuration(),
					section.getSequence(), section.getTitle(), section
							.getDescription(), section.getTypeId(), section
							.getStatus(), section.getCreatedBy(), section
							.getCreatedDate(), section.getLastModifiedBy(),
					section.getLastModifiedDate());
			Set newSectionAttachmentSet = prepareSectionAttachmentSet(
					newSection, section.getSectionAttachmentSet(), protocol, toContext);
			newSection.setSectionAttachmentSet(newSectionAttachmentSet);
			Set newItemSet = prepareItemSet(newSection, section.getItemSet(),
					protocol, toContext);
			newSection.setItemSet(newItemSet);
			Set newMetaDataSet = prepareSectionMetaDataSet(newSection, section
					.getSectionMetaDataSet());
			newSection.setSectionMetaDataSet(newMetaDataSet);
			newSection.setAssessment(newAssessment);
			h.add(newSection);
		}
		return h;
	}
	
	public Set prepareSectionSet(AssessmentData newAssessment, Set sectionSet,
			String protocol) {
		return prepareSectionSet(newAssessment, sectionSet, protocol, null);
	}

	public Set prepareSectionMetaDataSet(SectionData newSection, Set metaDataSet) {
		HashSet h = new HashSet();
		Iterator n = metaDataSet.iterator();
		while (n.hasNext()) {
			SectionMetaData sectionMetaData = (SectionMetaData) n.next();
			SectionMetaData newSectionMetaData = new SectionMetaData(
					newSection, sectionMetaData.getLabel(), sectionMetaData
							.getEntry());
			h.add(newSectionMetaData);
		}
		return h;
	}

	public Set prepareItemSet(SectionData newSection, Set itemSet,
			String protocol, String toContext) {
		log.debug("new item size = " + itemSet.size());
		HashSet h = new HashSet();
		Iterator j = itemSet.iterator();
		while (j.hasNext()) {
			ItemData item = (ItemData) j.next();
			ItemData newItem = new ItemData(newSection, item.getSequence(),
					item.getDuration(), item.getInstruction(), item
							.getDescription(), item.getTypeId(), item
							.getGrade(), item.getScore(), item.getScoreDisplayFlag(), item.getDiscount(), item.getMinScore(), item.getHint(), item
							.getHasRationale(), item.getStatus(), item
							.getCreatedBy(), item.getCreatedDate(), item
							.getLastModifiedBy(), item.getLastModifiedDate(),
					null, null, null,// set ItemTextSet, itemMetaDataSet and
					// itemFeedbackSet later
					null, //itemHistoricalSet later
					item.getTriesAllowed(), item.getPartialCreditFlag(),item.getHash(), item.getItemId());
			Set newItemTextSet = prepareItemTextSet(newItem, item
					.getItemTextSet(), protocol, toContext);
			Set newItemMetaDataSet = prepareItemMetaDataSet(newItem, item
					.getItemMetaDataSet());
			Set newItemTagSet = prepareItemTagSet(newItem, item
					.getItemTagSet());
			Set newItemFeedbackSet = prepareItemFeedbackSet(newItem, item
					.getItemFeedbackSet());
			Set newItemAttachmentSet = prepareItemAttachmentSet(newItem, item
					.getItemAttachmentSet(), protocol, toContext);
			Set<ItemHistoricalIfc> newItemHistoricalSet = prepareItemHistoricalSet(newItem,
					item.getItemHistoricalSet());
			newItem.setItemTextSet(newItemTextSet);
			newItem.setItemMetaDataSet(newItemMetaDataSet);
			newItem.setItemTagSet(newItemTagSet);
			newItem.setItemFeedbackSet(newItemFeedbackSet);
			newItem.setItemAttachmentSet(newItemAttachmentSet);
			newItem.setItemHistoricalSet(newItemHistoricalSet);
			newItem.setAnswerOptionsRichCount(item.getAnswerOptionsRichCount());
			newItem.setAnswerOptionsSimpleOrRich(item.getAnswerOptionsSimpleOrRich());
			newItem.setIsExtraCredit(item.getIsExtraCredit());
			newItem.setIsFixed(item.getIsFixed());
			h.add(newItem);
		}
		return h;
	}
	
	public Set prepareItemSet(SectionData newSection, Set itemSet,
			String protocol) {
		return prepareItemSet(newSection, itemSet, protocol, null);
	}
	
	public Set prepareItemTextSet(ItemData newItem, Set itemTextSet, String protocol, String toContext) {
		log.debug("new item text size = " + itemTextSet.size());
		HashSet h = new HashSet();
		Iterator k = itemTextSet.iterator();
		while (k.hasNext()) {
			ItemText itemText = (ItemText) k.next();
			log.debug("item text id =" + itemText.getId());
			ItemText newItemText = new ItemText(newItem,
					itemText.getSequence(), itemText.getText(), null);
			Set newAnswerSet = prepareAnswerSet(newItemText, itemText
					.getAnswerSet());
			newItemText.setAnswerSet(newAnswerSet);
			
			Set itemTextAttachmentSet = this.prepareItemTextAttachmentSet(newItemText, 
					itemText.getItemTextAttachmentSet(), protocol, toContext);
			newItemText.setItemTextAttachmentSet(itemTextAttachmentSet);
			newItemText.setRequiredOptionsCount(itemText.getRequiredOptionsCount());
			newItemText.setAddedButNotExtracted(itemText.isAddedButNotExtracted());

			h.add(newItemText);
		}
		return h;
	}

	public Set prepareItemMetaDataSet(ItemData newItem, Set itemMetaDataSet) {
		HashSet h = new HashSet();
		Iterator n = itemMetaDataSet.iterator();
		while (n.hasNext()) {
			ItemMetaData itemMetaData = (ItemMetaData) n.next();
			ItemMetaData newItemMetaData = new ItemMetaData(newItem,
					itemMetaData.getLabel(), itemMetaData.getEntry());
			h.add(newItemMetaData);
		}
		return h;
	}

	public Set prepareItemTagSet(ItemData newItem, Set itemTagSet) {
		HashSet h = new HashSet();
		Iterator n = itemTagSet.iterator();
		while (n.hasNext()) {
			ItemTag itemTag = (ItemTag) n.next();
			ItemTag newItemTag = new ItemTag(newItem,
					itemTag.getTagId(), itemTag.getTagLabel(),
					itemTag.getTagCollectionId(),
					itemTag.getTagCollectionName());
			h.add(newItemTag);
		}
		return h;
	}

	public Set prepareItemFeedbackSet(ItemData newItem, Set itemFeedbackSet) {
		HashSet h = new HashSet();
		Iterator o = itemFeedbackSet.iterator();
		while (o.hasNext()) {
			ItemFeedback itemFeedback = (ItemFeedback) o.next();
			ItemFeedback newItemFeedback = new ItemFeedback(newItem,
					itemFeedback.getTypeId(), itemFeedback.getText(), itemFeedback.getTextValue());
			h.add(newItemFeedback);
		}
		return h;
	}

	public Set<ItemHistoricalIfc> prepareItemHistoricalSet(ItemData newItem, Set<ItemHistoricalIfc> itemMHistoricalSet) {
		HashSet h = new HashSet();
		Iterator<ItemHistoricalIfc> n = itemMHistoricalSet.iterator();
		while (n.hasNext()) {
			ItemHistoricalIfc itemHistorical = n.next();
			ItemHistoricalIfc newItemMetaData = new ItemHistorical(newItem,
					itemHistorical.getModifiedBy(), itemHistorical.getModifiedDate());
			h.add(newItemMetaData);
		}
		return h;
	}

	public Set prepareItemAttachmentSet(ItemData newItem,
			Set itemAttachmentSet, String protocol, String toContext) {
		HashSet h = new HashSet();
		Iterator o = itemAttachmentSet.iterator();
		while (o.hasNext()) {
			ItemAttachment itemAttachment = (ItemAttachment) o.next();
			try {
				// create a copy of the resource
				AssessmentService service = new AssessmentService();
				ContentResource cr_copy = service.createCopyOfContentResource(
						itemAttachment.getResourceId(), itemAttachment
								.getFilename(), toContext);
				if (cr_copy == null) {
					log.error("Error copying the attachment of the item with id {}.", itemAttachment.getItem().getItemId());
					continue;
				}
				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				ItemAttachment newItemAttachment = new ItemAttachment(null,
						newItem, cr_copy.getId(), itemAttachment.getFilename(),
						itemAttachment.getMimeType(), itemAttachment
								.getFileSize(),
						itemAttachment.getDescription(), url, itemAttachment
								.getIsLink(), itemAttachment.getStatus(),
						itemAttachment.getCreatedBy(), itemAttachment
								.getCreatedDate(), itemAttachment
								.getLastModifiedBy(), itemAttachment
								.getLastModifiedDate());
				h.add(newItemAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}
	
	// EMI ItemText attachments
	public Set prepareItemTextAttachmentSet(ItemText newItemText,
			Set itemTextAttachmentSet, String protocol, String toContext) {
		HashSet h = new HashSet();
		Iterator o = itemTextAttachmentSet.iterator();
		while (o.hasNext()) {
			ItemTextAttachment itemTextAttachment = (ItemTextAttachment) o.next();
			try {
				// create a copy of the resource
				AssessmentService service = new AssessmentService();
				ContentResource cr_copy = service.createCopyOfContentResource(
						itemTextAttachment.getResourceId(), itemTextAttachment
								.getFilename(), toContext);
				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				ItemTextAttachment newItemTextAttachment = new ItemTextAttachment(null,
						newItemText, cr_copy.getId(), itemTextAttachment.getFilename(),
						itemTextAttachment.getMimeType(), itemTextAttachment
								.getFileSize(),
						itemTextAttachment.getDescription(), url, itemTextAttachment
								.getIsLink(), itemTextAttachment.getStatus(),
						itemTextAttachment.getCreatedBy(), itemTextAttachment
								.getCreatedDate(), itemTextAttachment
								.getLastModifiedBy(), itemTextAttachment
								.getLastModifiedDate());
				h.add(newItemTextAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}
	
	public Set prepareItemAttachmentSet(ItemData newItem,
			Set itemAttachmentSet, String protocol) {
		return prepareItemAttachmentSet(newItem, itemAttachmentSet, protocol, null);
	}

	public Set prepareSectionAttachmentSet(SectionData newSection,
			Set sectionAttachmentSet, String protocol, String toContext) {
		HashSet h = new HashSet();
		Iterator o = sectionAttachmentSet.iterator();
		while (o.hasNext()) {
			SectionAttachment sectionAttachment = (SectionAttachment) o.next();
			// create a copy of the resource
			AssessmentService service = new AssessmentService();
			ContentResource cr_copy = service.createCopyOfContentResource(
					sectionAttachment.getResourceId(), sectionAttachment
							.getFilename(), toContext);

			// get relative path
			String url = getRelativePath(cr_copy.getUrl(), protocol);

			SectionAttachment newSectionAttachment = new SectionAttachment(
					null, newSection, cr_copy.getId(), sectionAttachment
							.getFilename(), sectionAttachment.getMimeType(),
					sectionAttachment.getFileSize(), sectionAttachment
							.getDescription(), url, sectionAttachment
							.getIsLink(), sectionAttachment.getStatus(),
					sectionAttachment.getCreatedBy(), sectionAttachment
							.getCreatedDate(), sectionAttachment
							.getLastModifiedBy(), sectionAttachment
							.getLastModifiedDate());
			h.add(newSectionAttachment);
		}
		return h;
	}

	public Set<AssessmentAttachment> prepareSectionAttachmentSet(SectionData newSection, Set sectionAttachmentSet, String protocol) {
		return prepareSectionAttachmentSet(newSection, sectionAttachmentSet, protocol, null);
	}
	
	public Set<AssessmentAttachment> prepareAssessmentAttachmentSet(AssessmentData newAssessment,
			Set<AssessmentAttachment> assessmentAttachmentSet, String protocol, String toContext) {
		Set<AssessmentAttachment> h = new HashSet<>();

		for (AssessmentAttachment assessmentAttachment : assessmentAttachmentSet) {
			// create a copy of the resource
			AssessmentService service = new AssessmentService();
			ContentResource cr_copy = service.createCopyOfContentResource(
					assessmentAttachment.getResourceId(), assessmentAttachment
							.getFilename(), toContext);

			if (cr_copy != null) {
				// get relative path
				String url = getRelativePath(cr_copy.getUrl(), protocol);

				AssessmentAttachment newAssessmentAttachment = new AssessmentAttachment(
						null, newAssessment, cr_copy.getId(), assessmentAttachment
						.getFilename(), assessmentAttachment.getMimeType(),
						assessmentAttachment.getFileSize(), assessmentAttachment
						.getDescription(), url, assessmentAttachment
						.getIsLink(), assessmentAttachment.getStatus(),
						assessmentAttachment.getCreatedBy(), assessmentAttachment
						.getCreatedDate(), assessmentAttachment
						.getLastModifiedBy(), assessmentAttachment
						.getLastModifiedDate());
				h.add(newAssessmentAttachment);
			}
		}
		return h;
	}

	public Set prepareAssessmentAttachmentSet(AssessmentData newAssessment,
			Set assessmentAttachmentSet, String protocol) {
		return prepareAssessmentAttachmentSet(newAssessment, assessmentAttachmentSet, protocol, null);
	}
	public Set prepareAnswerSet(ItemText newItemText, Set answerSet) {
		log.debug("new answer size = " + answerSet.size());
		HashSet h = new HashSet();
		Iterator l = answerSet.iterator();
		while (l.hasNext()) {
			Answer answer = (Answer) l.next();
			Answer newAnswer = new Answer(newItemText, answer.getText(), answer
					.getSequence(), answer.getLabel(), answer.getIsCorrect(),
					answer.getGrade(), answer.getScore(), answer.getPartialCredit(), answer.getDiscount(), 
					//answer.getCorrectOptionLabels(), 
					null);
			Set newAnswerFeedbackSet = prepareAnswerFeedbackSet(newAnswer,
					answer.getAnswerFeedbackSet());
			newAnswer.setAnswerFeedbackSet(newAnswerFeedbackSet);
			h.add(newAnswer);
		}
		return h;
	}

	public Set prepareAnswerFeedbackSet(Answer newAnswer, Set answerFeedbackSet) {
		HashSet h = new HashSet();
		Iterator m = answerFeedbackSet.iterator();
		while (m.hasNext()) {
			AnswerFeedback answerFeedback = (AnswerFeedback) m.next();
			AnswerFeedback newAnswerFeedback = new AnswerFeedback(newAnswer,
					answerFeedback.getTypeId(), answerFeedback.getText());
			h.add(newAnswerFeedback);
		}
		return h;
	}

  
  public String getAssessmentSiteId (String assessmentId){
	    List<AuthorizationData> l = (List<AuthorizationData>) getHibernateTemplate()
				.findByNamedParam("select a from AuthorizationData a where a.functionId = :fid and a.qualifierId = :id",
						new String[] {"fid", "id"},
						new Object[] {"EDIT_ASSESSMENT", assessmentId});
	    if (!l.isEmpty()) {
	      AuthorizationData a = l.get(0);
	      return a.getAgentIdString();
	    }
	    return null;
  }
  
  public String getAssessmentCreatedBy(String assessmentId) {
    List<AssessmentData> l = (List<AssessmentData>) getHibernateTemplate().findByNamedParam(
    		"select a from AssessmentData a where a.assessmentBaseId = :id", "id", Long.parseLong(assessmentId));
    if (!l.isEmpty()){
    	AssessmentData a = l.get(0);
      return a.getCreatedBy();
    }
    else return null;
  }
  
	public Set copyItemAttachmentSet(ItemData newItem, Set itemAttachmentSet) {
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
				String url = getRelativePath(cr_copy.getUrl(), ServerConfigurationService.getServerUrl());

				ItemAttachment newItemAttachment = new ItemAttachment(null,
						newItem, cr_copy.getId(), itemAttachment.getFilename(),
						itemAttachment.getMimeType(), itemAttachment.getFileSize(),
						itemAttachment.getDescription(), url, itemAttachment.getIsLink(), 
						itemAttachment.getStatus(),	AgentFacade.getAgentString(), new Date(), 
						AgentFacade.getAgentString(), new Date());
				h.add(newItemAttachment);
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return h;
	}

	private Map<String, Map<String, String>> getReleaseToGroupsByAssessmentIds(final String siteId, final List<String> assessmentIds) {
		Map<String, Map<String, String>> releaseToGroupsByAssessmentId = new HashMap<>();
		if (CollectionUtils.isEmpty(assessmentIds)) {
			return releaseToGroupsByAssessmentId;
		}

		HibernateCallback<List<AuthorizationData>> hcb = session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<AuthorizationData> cq = cb.createQuery(AuthorizationData.class);
			Root<AuthorizationData> root = cq.from(AuthorizationData.class);

			Predicate functionIdPredicate = cb.equal(root.get("functionId"), "TAKE_ASSESSMENT");
			Predicate qualifierIdPredicate =
					HibernateCriterionUtils.PredicateInSplitter(cb, root.<String>get("qualifierId"), assessmentIds);

			cq.select(root).where(cb.and(functionIdPredicate, qualifierIdPredicate));
			return session.createQuery(cq).getResultList();
		};

		List<AuthorizationData> authorizations = getHibernateTemplate().execute(hcb);
		if (CollectionUtils.isEmpty(authorizations)) {
			return releaseToGroupsByAssessmentId;
		}

		Map<String, String> allGroupsInSite = getAllGroupsForSite(siteId);
		for (AuthorizationData authorization : authorizations) {
			String groupId = authorization.getAgentIdString();
			if (!allGroupsInSite.containsKey(groupId)) {
				continue;
			}
			releaseToGroupsByAssessmentId
				.computeIfAbsent(authorization.getQualifierId(), id -> new HashMap<>())
				.put(groupId, allGroupsInSite.get(groupId));
		}
		return releaseToGroupsByAssessmentId;
	}

	private Set<String> getActiveAssessmentTitlesForSite(final String siteId) {
		HibernateCallback<List<String>> hcb = session -> session.createQuery(
				"select a.title from AssessmentData a,AuthorizationData z where a.status = :status and " +
						"a.assessmentBaseId=z.qualifierId and z.functionId = :fid and z.agentIdString = :site")
				.setParameter("status", 1)
				.setParameter("fid", "EDIT_ASSESSMENT")
				.setParameter("site", siteId)
				.list();

		Set<String> titles = new HashSet<>();
		List<String> existingTitles = getHibernateTemplate().execute(hcb);
		if (CollectionUtils.isNotEmpty(existingTitles)) {
			existingTitles.stream().filter(title -> title != null).forEach(title -> titles.add(title.trim()));
		}
		return titles;
	}

	private String getUniqueImportedTitle(String title, Set<String> usedTitles) {
		String candidate = (title == null) ? "" : title.trim();
		String originalTitle = candidate;
		int count = 0;
		while (usedTitles.contains(candidate) && count < 100) {
			candidate = AssessmentService.renameDuplicate(candidate);
			count++;
		}
		if (usedTitles.contains(candidate)) {
			String exhaustedCandidate = candidate;
			do {
				candidate = exhaustedCandidate + "-" + Long.toHexString(System.nanoTime());
			} while (usedTitles.contains(candidate));
			log.warn("getUniqueImportedTitle exhausted AssessmentService.renameDuplicate attempts and generated random fallback; "
					+ "original title='{}', exhausted candidate='{}', final candidate='{}', attempt count={}",
					originalTitle, exhaustedCandidate, candidate, count);
		}
		usedTitles.add(candidate);
		return candidate;
	}

	private Map<String, String> getReleaseToGroups(final String siteId, final Long assessmentId) {
		Map<String, String> releaseToGroups = new HashMap<>();
		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
		List<AuthorizationData> authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_ASSESSMENT", assessmentId.toString());
		if (authorizations != null && !authorizations.isEmpty()) {
			Map<String, String> allGroupsInSite = getAllGroupsForSite(siteId);
			authorizations.stream().filter(a -> allGroupsInSite.containsKey(a.getAgentIdString()))
					.forEach(a -> releaseToGroups.put(a.getAgentIdString(), allGroupsInSite.get(a.getAgentIdString())));
		}
		return releaseToGroups;
	}

	private Map<String, String> getAllGroupsForSite(final String siteId) {
		Map<String, String> groupInfo = new HashMap<>();
		try {
			Site site = SiteService.getSite(siteId);
			Collection<Group> groups = site.getGroups();
			if (groups != null) {
				groups.forEach(g -> groupInfo.put(g.getId(), g.getTitle()));
			}
		} catch (Exception e) {
			log.warn("Site [{}] not found while attempting to get its groups, {}", siteId, e.toString());
		}
		return groupInfo;
	}

    public List<AssessmentData> getDeletedAssessments(final String siteAgentId) {
        final HibernateCallback<List<AssessmentData>> hcb = session -> session.createQuery(
            "select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate) " +
                "from AssessmentData a, AuthorizationData z " +
                "where a.assessmentBaseId=z.qualifierId and z.functionId=:functionId " +
                "and z.agentIdString=:siteId and a.status=:inactiveStatus ")
                .setParameter("functionId", "EDIT_ASSESSMENT")
                .setParameter("siteId", siteAgentId)
                .setParameter("inactiveStatus", AssessmentIfc.DEAD_STATUS)
                .list();
        return getHibernateTemplate().execute(hcb);
    }

    public void restoreAssessment(Long assessmentId) {
    	AssessmentData assessment = (AssessmentData) getHibernateTemplate().load(AssessmentData.class, assessmentId);
    	assessment.setLastModifiedBy(AgentFacade.getAgentString());
    	assessment.setLastModifiedDate(new Date());
    	assessment.setStatus(AssessmentIfc.ACTIVE_STATUS);

    	RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");
    	rubricsService.restoreRubricAssociationsByItemIdPrefix(assessmentId + ".", RubricsConstants.RBCS_TOOL_SAMIGO);

    	int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    	while (retryCount > 0) {
    		try {
    			getHibernateTemplate().update(assessment);
    			retryCount = 0;
    		} catch (Exception e) {
    			log.warn("problem updating asssessment: " + e.getMessage());
    			retryCount = PersistenceService.getInstance().getPersistenceHelper()
    					.retryDeadlock(e, retryCount);
    		}
    	}
    }
	
	public Set<String> getDuplicateItemHashesForAssessmentIds(Collection<Long> assessmentIds) {
		if (assessmentIds.isEmpty()) {
			return Collections.emptySet();
		}

		Session session = currentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<ItemData> root = cq.from(ItemData.class);
		Join<ItemData, SectionData> sectionJoin = root.join("section");
		Join<SectionData, AssessmentData> assessmentJoin = sectionJoin.join("assessment");

		cq.select(root.get("hash"))
				.where(assessmentJoin.get("assessmentBaseId").in(assessmentIds))
				.groupBy(root.get("hash"))
				// Item count with same hash must be greater then one
				.having(cb.gt(cb.count(root), 1));

		return session.createQuery(cq).getResultStream().collect(Collectors.toSet());
	}

}
