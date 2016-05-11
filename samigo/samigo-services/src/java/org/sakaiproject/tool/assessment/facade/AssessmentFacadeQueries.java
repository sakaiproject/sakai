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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
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
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTextAttachment;
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
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.entity.api.CoreAssessmentEntityProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateQueryException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class AssessmentFacadeQueries extends HibernateDaoSupport implements
		AssessmentFacadeQueriesAPI {
	private Logger log = LoggerFactory.getLogger(AssessmentFacadeQueries.class);

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
	public ArrayList getAllAssessmentTemplates() {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		final String query = "select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.typeId)"
				+ " from AssessmentTemplateData a where a.assessmentBaseId=? or"
				+ " a.createdBy=? or a.typeId=? order by a.title";
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, Long.valueOf(1));
				q.setString(1, agent);
				q.setLong(2, typeId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		// List list = getHibernateTemplate().find(query,
		// new Object[]{agent},
		// new org.hibernate.type.Type[] {Hibernate.STRING});
		ArrayList templateList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentTemplateData a = (AssessmentTemplateData) list.get(i);
			AssessmentTemplateFacade f = new AssessmentTemplateFacade(a);
			templateList.add(f);
		}
		return templateList;
	}

	// sakai2.0 we want to scope it by creator, users can only see their
	// templates plus the "Default Template"
	public ArrayList getAllActiveAssessmentTemplates() {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		final String query = "select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.typeId)"
				+ " from AssessmentTemplateData a where a.status=? and (a.assessmentBaseId=? or"
				+ " a.createdBy=? or a.typeId=?) order by a.title";
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setInteger(0, 1);
				q.setLong(1, Long.valueOf(1));
				q.setString(2, agent);
				q.setLong(3, typeId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[]{agent},
		// new org.hibernate.type.Type[] {Hibernate.STRING});
		ArrayList templateList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentTemplateData a = (AssessmentTemplateData) list.get(i);
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
	public ArrayList getTitleOfAllActiveAssessmentTemplates() {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		final String query = "select new AssessmentTemplateData(a.assessmentBaseId, a.title) "
				+ " from AssessmentTemplateData a where a.status=? and "
				+ " (a.assessmentBaseId=? or a.createdBy=? or typeId=?) order by a.title";
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setInteger(0, 1);
				q.setLong(1, Long.valueOf(1));
				q.setString(2, agent);
				q.setLong(3, typeId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[]{agent},
		// new org.hibernate.type.Type[] {Hibernate.STRING});
		ArrayList templateList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentTemplateData a = (AssessmentTemplateData) list.get(i);
			a.setAssessmentTemplateId(a.getAssessmentBaseId());
			AssessmentTemplateFacade f = new AssessmentTemplateFacade(a
					.getAssessmentBaseId(), a.getTitle());
			templateList.add(f);
		}
		return templateList;
	}

	public AssessmentFacade getAssessment(Long assessmentId) {
		try {
			AssessmentData assessment = (AssessmentData) getHibernateTemplate()
			.load(AssessmentData.class, assessmentId);
			assessment.setSectionSet(getSectionSetForAssessment(assessment));
			return new AssessmentFacade(assessment);
		}
		catch (DataAccessException e) {
			log.warn("error retieving assemement: " + assessmentId.toString() + " " +  e.getMessage());
		}
		return null;
	}

	/**
	 * IMPORTANT: 1. we have declared SectionData as lazy loading, so we need to
	 * initialize it using getHibernateTemplate().initialize(java.lang.Object).
	 * Unfortunately, we are using Spring 1.0.2 which does not support this
	 * Hibernate feature. I tried upgrading Spring to 1.1.3. Then it failed to
	 * load all the OR maps correctly. So for now, I am just going to initialize
	 * it myself. I will take a look at it again next year. - daisyf (12/13/04)
	 */
	private HashSet getSectionSetForAssessment(AssessmentData assessment) {
		List sectionList = getHibernateTemplate().find(
				"from SectionData s where s.assessment.assessmentBaseId=?",
						assessment.getAssessmentBaseId());
		HashSet set = new HashSet();
		for (int j = 0; j < sectionList.size(); j++) {
			set.add((SectionData) sectionList.get(j));
		}
		return set;
	}

	public void removeAssessment(final Long assessmentId) {
		// if pubAssessment exist, simply set assessment to inactive else delete assessment
		List count = getHibernateTemplate()
				.find("select count(p) from PublishedAssessmentData p where p.assessmentId=?",assessmentId);

		if (log.isDebugEnabled()) log.debug("removeAssesment: no. of pub Assessment =" + count.size());
		Iterator iter = count.iterator();
		int i = ((Integer) iter.next()).intValue();
		if (i < 1) {
			AssessmentData assessment = (AssessmentData) getHibernateTemplate().load(AssessmentData.class, assessmentId);

			AssessmentService s = new AssessmentService();
			List resourceIdList = s.getAssessmentResourceIdList(assessment);
			if (log.isDebugEnabled()) log.debug("*** we have no. of resource in assessment=" + resourceIdList.size());
			s.deleteResources(resourceIdList);
		}
		
		final String softDeleteQuery = "update AssessmentData set status=? WHERE assessmentBaseId=?";

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery(softDeleteQuery);
				q.setInteger(0, AssessmentIfc.DEAD_STATUS);
				q.setLong(1, assessmentId);
				return q.executeUpdate();
			};
		};
		
		getHibernateTemplate().execute(hcb);
	}

	/* this assessment comes with a default section */
	public AssessmentData cloneAssessmentFromTemplate(AssessmentTemplateData t) {
		// log.debug("**** DEFAULT templateId inside clone" +
		// t.getAssessmentTemplateId());
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
			ex.printStackTrace();
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
		control.setTimedAssessment(Integer.valueOf(0));
		control.setTimeLimit(Integer.valueOf(0));
		
		// set accessControl.releaseTo based on default setting in metaData
		String defaultReleaseTo = template
			.getAssessmentMetaDataByLabel("releaseTo");
		if (("ANONYMOUS_USERS").equals(defaultReleaseTo)) {
			control.setReleaseTo("Anonymous Users");
		} else {
			if (siteId == null || siteId.length() == 0) {
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
		GradebookExternalAssessmentService g = null;
		boolean integrated = IntegrationContextFactory.getInstance()
			.isIntegrated();
		try {
			if (integrated) {
				g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().getBean(
						"org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
			}

			GradebookServiceHelper gbsHelper = IntegrationContextFactory
			.getInstance().getGradebookServiceHelper();
			if (!gbsHelper
					.gradebookExists(GradebookFacade.getGradebookUId(siteId), g))
				evaluation
					.setToGradeBook(EvaluationModelIfc.GRADEBOOK_NOT_AVAILABLE
						.toString());
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

	public ArrayList getAllAssessments(String orderBy) {
		List list = getHibernateTemplate().find(
				"from AssessmentData a order by a." + orderBy);
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			AssessmentFacade f = new AssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getAllActiveAssessments(String orderBy) {
		List list = getHibernateTemplate().find(
				"from AssessmentData a where a.status=? order by a." + orderBy, Integer.valueOf(1));
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			a.setSectionSet(getSectionSetForAssessment(a));
			AssessmentFacade f = new AssessmentFacade(a);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getBasicInfoOfAllActiveAssessments(String orderBy,
			boolean ascending) {

		String query = "select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate)from AssessmentData a where a.status=? order by a."
				+ orderBy;

		if (ascending) {
			query += " asc";
		} else {
			query += " desc";
		}

		List list = getHibernateTemplate().find(query, Integer.valueOf(1));

		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(),
					a.getTitle(), a.getLastModifiedDate());
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getBasicInfoOfAllActiveAssessmentsByAgent(String orderBy,
			final String siteAgentId, boolean ascending) {
		// Get the list of assessment 
		StringBuilder sb = new StringBuilder("select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.lastModifiedBy) ");
		sb.append("from AssessmentData a, AuthorizationData z where a.status=? and ");
		sb.append("a.assessmentBaseId=z.qualifierId and z.functionId=? ");
		sb.append("and z.agentIdString=? order by a.");
		sb.append(orderBy);
		
		String query = sb.toString();
		if (ascending)
			query += " asc";
		else
			query += " desc";

		final String hql = query;
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setInteger(0, 1);
				q.setString(1, "EDIT_ASSESSMENT");
				q.setString(2, siteAgentId);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// Get the number of question in each assessment
		HashMap questionSizeMap = new HashMap();
		HibernateCallback hcb2 = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				StringBuilder sb2 = new StringBuilder("select a.assessmentBaseId, count(*) ");
				sb2.append("from ItemData i, SectionData s,  AssessmentData a, AuthorizationData z ");
				sb2.append("where a = s.assessment and s = i.section and a.assessmentBaseId = z.qualifierId ");
				sb2.append("and z.functionId=? and z.agentIdString=? ");
				sb2.append("group by a.assessmentBaseId ");
				Query q2 = session.createQuery(sb2.toString());
				q2.setString(0, "EDIT_ASSESSMENT");
				q2.setString(1, siteAgentId);
				return q2.list();
			};
		};
		List questionSizeList = getHibernateTemplate().executeFind(hcb2);
		Iterator iter = questionSizeList.iterator();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next();
			questionSizeMap.put(o[0], o[1]);
		}
		
		ArrayList assessmentList = new ArrayList();
		String lastModifiedBy = "";
		AgentFacade agent = null;
		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			agent = new AgentFacade(a.getLastModifiedBy());
			if (agent != null) {
				lastModifiedBy = agent.getDisplayName();
			}
			int questionSize = 0;
			if (questionSizeMap.get(a.getAssessmentBaseId()) != null) {
				questionSize = (Integer) questionSizeMap.get(a.getAssessmentBaseId());
			}
			AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(),
					a.getTitle(), a.getLastModifiedDate(), lastModifiedBy, questionSize);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getBasicInfoOfAllActiveAssessmentsByAgent(String orderBy,
			final String siteAgentId) {
		final String query = "select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate) "
				+ " from AssessmentData a, AuthorizationData z where a.status=? and "
				+ " a.assessmentBaseId=z.qualifierId and z.functionId=? "
				+ " and z.agentIdString=? order by a." + orderBy;

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setInteger(0, 1);
				q.setString(1, "EDIT_ASSESSMENT");
				q.setString(2, siteAgentId);
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[] {siteAgentId},
		// new org.hibernate.type.Type[] {Hibernate.STRING});
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			AssessmentFacade f = new AssessmentFacade(a.getAssessmentBaseId(),
					a.getTitle(), a.getLastModifiedDate());
			assessmentList.add(f);
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

	public ArrayList getSettingsOfAllActiveAssessments(String orderBy) {
		List list = getHibernateTemplate().find(
				"from AssessmentData a where a.status=? order by a." + orderBy, 1);
		ArrayList assessmentList = new ArrayList();
		// IMPORTANT:
		// 1. we do not want any Section info, so set loadSection to false
		// 2. We have also declared SectionData as lazy loading. If loadSection
		// is set
		// to true, we will see null pointer
		Boolean loadSection = Boolean.FALSE;
		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			AssessmentFacade f = new AssessmentFacade(a, loadSection);
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public ArrayList getAllAssessments(int pageSize, int pageNumber,
			String orderBy) {
		String queryString = "from AssessmentData a order by a." + orderBy;
		PagingUtilQueriesAPI pagingUtilQueries = PersistenceService
				.getInstance().getPagingUtilQueries();
		List pageList = pagingUtilQueries.getAll(pageSize, pageNumber,
				queryString);
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < pageList.size(); i++) {
			AssessmentData a = (AssessmentData) pageList.get(i);
			AssessmentFacade f = new AssessmentFacade(a);
			// log.debug("**** assessment facade Id=" + f.getAssessmentId());
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public int getQuestionSize(final Long assessmentId) {
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session
						.createQuery("select count(i) from ItemData i, SectionData s,  AssessmentData a where a = s.assessment and s = i.section and a.assessmentBaseId=?");
				q.setLong(0, assessmentId.longValue());
				return q.list();
			};
		};
		List size = getHibernateTemplate().executeFind(hcb);

		// List size = getHibernateTemplate().find(
		// "select count(i) from ItemData i, SectionData s, AssessmentData a
		// where a = s.assessment and s = i.section and a.assessmentBaseId=?",
		// new Object[] {assessmentId}
		// , new org.hibernate.type.Type[] {Hibernate.LONG});
		Iterator iter = size.iterator();
		if (iter.hasNext()) {
			int i = ((Integer) iter.next()).intValue();
			return i;
		} else {
			return 0;
		}
	}

	public void deleteAllSecuredIP(AssessmentIfc assessment) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				Long assessmentId = assessment.getAssessmentId();
				List ip = getHibernateTemplate()
						.find(
								"from SecuredIPAddress s where s.assessment.assessmentBaseId=?",
								assessmentId);
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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

		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				List metadatas = getHibernateTemplate()
						.find(
								"from AssessmentMetaData a where a.assessment.assessmentBaseId = ?",
								t.getAssessmentBaseId());
				if (metadatas.size() > 0) {
					AssessmentMetaDataIfc m = (AssessmentMetaDataIfc) metadatas
							.get(0);
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
			HashMap h = qpService.getQuestionPoolItemMap();
			checkForQuestionPoolItem(section, h);

			AssessmentData assessment = (AssessmentData) section
					.getAssessment();
			assessment.setLastModifiedBy(AgentFacade.getAgentString());
			assessment.setLastModifiedDate(new Date());

			// lazy loading on sectionSet, so need to initialize it
			Set sectionSet = getSectionSetForAssessment(assessment);
			assessment.setSectionSet(sectionSet);
			ArrayList sections = assessment.getSectionArraySorted();
			// need to reorder the remaining section
			HashSet set = new HashSet();
			int count = 1;
			for (int i = 0; i < sections.size(); i++) {
				SectionData s = (SectionData) sections.get(i);
				if (!(s.getSectionId()).equals(section.getSectionId())) {
					s.setSequence(Integer.valueOf(count++));
					set.add(s);
				}
			}
			assessment.setSectionSet(set);
			int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
					.intValue();
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

			// remove assessment
			retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
					.intValue();
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
		// log.debug("***itemSet before=" + itemSet.size());
		while (iter.hasNext()) {
			ItemData item = (ItemData) iter.next();
			// item belongs to a pool, set section=null so
			// item won't get deleted during section deletion
			item.setSection(null);
			int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
					.intValue();
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
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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
		// need to reload the section again.
		// section= loadSection(sourceSectionId);
		// section.setItemSet(newItemSet);
		// getHibernateTemplate().update(section);
	}

	// sakai2.0 we want to scope it by creator, users can only see their
	// templates plus the "Default Template"
	public ArrayList getBasicInfoOfAllActiveAssessmentTemplates(String orderBy) {
		final String agent = AgentFacade.getAgentString();
		final Long typeId = TypeD.TEMPLATE_SYSTEM_DEFINED;
		final String query = "select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate, a.typeId)"
				+ " from AssessmentTemplateData a where a.status=1 and (a.assessmentBaseId=? or"
				+ " a.createdBy=? or typeId=?) order by a." + orderBy;

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, Long.valueOf(1));
				q.setString(1, agent);
				q.setLong(2, typeId.longValue());
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);

		// List list = getHibernateTemplate().find(query,
		// new Object[]{agent},
		// new org.hibernate.type.Type[] {Hibernate.STRING});
		ArrayList assessmentList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AssessmentTemplateData a = (AssessmentTemplateData) list.get(i);
			AssessmentTemplateFacade f = new AssessmentTemplateFacade(a
					.getAssessmentBaseId(), a.getTitle(), a
					.getLastModifiedDate(), a.getTypeId());
			assessmentList.add(f);
		}
		return assessmentList;
	}

	public void checkForQuestionPoolItem(AssessmentData assessment,
			HashMap qpItemHash) {
		Set sectionSet = getSectionSetForAssessment(assessment);
		Iterator iter = sectionSet.iterator();
		while (iter.hasNext()) {
			SectionData s = (SectionData) iter.next();
			checkForQuestionPoolItem(s, qpItemHash);
		}
	}

	public void checkForQuestionPoolItem(SectionData section, HashMap qpItemHash) {
		Set itemSet = section.getItemSet();
		HashSet newItemSet = new HashSet();
		Iterator iter = itemSet.iterator();
		// log.debug("***itemSet before=" + itemSet.size());
		while (iter.hasNext()) {
			ItemData item = (ItemData) iter.next();
			if (qpItemHash.get(item.getItemId().toString()) != null) {
				// item belongs to a pool, in this case, set section=null so
				// item won't get deleted during section deletion
				item.setSection(null);
				int retryCount = PersistenceService.getInstance().getPersistenceHelper()
						.getRetryCount().intValue();
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
		// log.debug("***itemSet after=" + newItemSet.size());
		section.setItemSet(newItemSet);
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
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

	public boolean assessmentTitleIsUnique(final Long assessmentBaseId,
			String title, Boolean isTemplate) {
		title = title.trim();
		final String currentSiteId = AgentFacade.getCurrentSiteId();
		final String agentString = AgentFacade.getAgentString();
		List list;
		boolean isUnique = true;
		String query = "";
		if (isTemplate.booleanValue()) { // templates are person scoped
			query = "select new AssessmentTemplateData(a.assessmentBaseId, a.title, a.lastModifiedDate)"
					+ " from AssessmentTemplateData a, AuthorizationData z where "
					+ " a.title=? and a.assessmentBaseId!=? and a.createdBy=? and a.status=?";

			final String hql = query;
			final String titlef = title;
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString(0, titlef);
					q.setLong(1, assessmentBaseId.longValue());
					q.setString(2, agentString);
					q.setInteger(3,1);
					return q.list();
				};
			};
			list = getHibernateTemplate().executeFind(hcb);

			// list = getHibernateTemplate().find(query,
			// new Object[]{title,assessmentBaseId,agentString},
			// new org.hibernate.type.Type[] {Hibernate.STRING, Hibernate.LONG,
			// Hibernate.STRING});
		} else { // assessments are site scoped
			query = "select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate)"
					+ " from AssessmentData a, AuthorizationData z where "
					+ " a.title=? and a.assessmentBaseId!=? and z.functionId=? and "
					+ " a.assessmentBaseId=z.qualifierId and z.agentIdString=? and a.status=?";

			final String hql = query;
			final String titlef = title;
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString(0, titlef);
					q.setLong(1, assessmentBaseId.longValue());
					q.setString(2, "EDIT_ASSESSMENT");
					q.setString(3, currentSiteId);
					q.setInteger(4,1);
					return q.list();
				};
			};
			list = getHibernateTemplate().executeFind(hcb);

			// list = getHibernateTemplate().find(query,
			// new Object[]{title,assessmentBaseId,currentSiteId},
			// new org.hibernate.type.Type[] {Hibernate.STRING, Hibernate.LONG,
			// Hibernate.STRING});
		}
		if (list.size() > 0) {
			// query in mysql & hsqldb are not case sensitive, check that title
			// found is indeed what we
			// are looking
			for (int i = 0; i < list.size(); i++) {
				AssessmentBaseIfc a = (AssessmentBaseIfc) list.get(i);
				if ((title).equals(a.getTitle().trim())) {
					isUnique = false;
					break;
				}
			}
		}
		return isUnique;
	}

	public List getAssessmentByTemplate(final Long templateId) {
		final String query = "select new AssessmentData(a.assessmentBaseId, a.title, a.lastModifiedDate) "
				+ " from AssessmentData a where a.assessmentTemplateId=?";

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, templateId.longValue());
				return q.list();
			};
		};
		return getHibernateTemplate().executeFind(hcb);

		// return getHibernateTemplate().find(query,
		// new Object[]{ templateId },
		// new org.hibernate.type.Type[] { Hibernate.LONG });
	}

	public List getDefaultMetaDataSet() {
		final String query = " from AssessmentMetaData m where m.assessment.assessmentBaseId=?";

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setLong(0, 1L);
				return q.list();
			};
		};
		return getHibernateTemplate().executeFind(hcb);

		// return getHibernateTemplate().find(query,
		// new Object[]{ new Long(1) },
		// new org.hibernate.type.Type[] { Hibernate.LONG });

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

	public void removeItemAttachment(Long itemAttachmentId) {
		ItemAttachment itemAttachment = (ItemAttachment) getHibernateTemplate()
				.load(ItemAttachment.class, itemAttachmentId);
		ItemDataIfc item = itemAttachment.getItem();
		// String resourceId = itemAttachment.getResourceId();
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

	public void removeItemTextAttachment(Long itemTextAttachmentId) {
		ItemTextAttachment itemTextAttachment = (ItemTextAttachment) getHibernateTemplate()
				.load(ItemTextAttachment.class, itemTextAttachmentId);
		ItemTextIfc itemText = itemTextAttachment.getItemText();
		// String resourceId = itemAttachment.getResourceId();
		int retryCount = PersistenceService.getInstance().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				if (itemText != null) { // need to dissociate with item before
					// deleting in Hibernate 3
					Set set = itemText.getItemTextAttachmentSet();
					set.remove(itemTextAttachment);
					getHibernateTemplate().delete(itemTextAttachment);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem delete itemAttachment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}
	}
	
	public void updateAssessmentLastModifiedInfo(
			AssessmentFacade assessment) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount()
				.intValue();
		AssessmentData data = (AssessmentData) assessment.getData();
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

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item,
			String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}
		return attach;
	}

	public ItemTextAttachmentIfc createItemTextAttachment(ItemTextIfc itemText,
			String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}
		return attach;
	}

	public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
			String resourceId, String filename, String protocol) {
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
		SectionAttachment sectionAttachment = (SectionAttachment) getHibernateTemplate()
				.load(SectionAttachment.class, sectionAttachmentId);
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

	public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol) {
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}
		return attach;
	}

	public void removeAssessmentAttachment(Long assessmentAttachmentId) {
		AssessmentAttachment assessmentAttachment = (AssessmentAttachment) getHibernateTemplate()
				.load(AssessmentAttachment.class, assessmentAttachmentId);
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
				log.warn("problem delete assessmentAttachment: "
						+ e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

	public AttachmentData createEmailAttachment(String resourceId,
			String filename, String protocol) {
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}
		return attach;
	}

	public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
	    for (AttachmentIfc attachment : list) {
	        getHibernateTemplate().saveOrUpdate(attachment);
	    }
	}

	public List getAllActiveAssessmentsByAgent(final String siteAgentId) {
		final String query = "select a "
				+ " from AssessmentData a,AuthorizationData z where a.status=? and "
				+ "a.assessmentBaseId=z.qualifierId and z.functionId=? "
				+ " and z.agentIdString=?";

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setInteger(0, 1);
				q.setString(1, "EDIT_ASSESSMENT");
				q.setString(2, siteAgentId);
				return q.list();
			};
		};
		return getHibernateTemplate().executeFind(hcb);
	}

	public void copyAllAssessments(String fromContext, String toContext, Map<String,String> transversalMap) {
		List list = getAllActiveAssessmentsByAgent(fromContext);
		ArrayList<AssessmentData> newList = new ArrayList<AssessmentData>();
		Map<AssessmentData, String> assessmentMap = new HashMap<AssessmentData, String>();

		for (int i = 0; i < list.size(); i++) {
			AssessmentData a = (AssessmentData) list.get(i);
			log.debug("****protocol:"
					+ ServerConfigurationService.getServerUrl());
			AssessmentData new_a = prepareAssessment(a,
					ServerConfigurationService.getServerUrl(), toContext);
			newList.add(new_a);
			assessmentMap.put(new_a, CoreAssessmentEntityProvider.ENTITY_PREFIX + "/" + a.getAssessmentBaseId());
		}
		for (AssessmentData assessmentData : newList) {
		    getHibernateTemplate().saveOrUpdate(assessmentData); // write
        }
		
		// authorization
		for (int i = 0; i < newList.size(); i++) {
			AssessmentData a = (AssessmentData) newList.get(i);
			PersistenceService.getInstance().getAuthzQueriesFacade()
					.createAuthorization(toContext, "EDIT_ASSESSMENT",
							a.getAssessmentId().toString());
			
			HashMap assessmentMetaDataMap = a.getAssessmentMetaDataMap();
			if (!assessmentMetaDataMap.containsKey("markForReview_isInstructorEditable")) {
				a.addAssessmentMetaData("markForReview_isInstructorEditable", "true");
				a.getAssessmentAccessControl().setMarkForReview(1);
			}
			
			// reset PARTID in ItemMetaData to the section of the newly created section
			Set sectionSet = a.getSectionSet();
			Iterator sectionIter = sectionSet.iterator();
			while (sectionIter.hasNext()) {
				SectionData section = (SectionData) sectionIter.next();
				Set itemSet = section.getItemSet();
				Iterator itemIter = itemSet.iterator();
				while (itemIter.hasNext()) {
					ItemData item = (ItemData) itemIter.next();
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
		}
		for (AssessmentData assessmentData : newList) {
		    getHibernateTemplate().saveOrUpdate(assessmentData); // write
		}
		for (AssessmentData data: newList) {
		    String oldRef = assessmentMap.get(data);
		    if (oldRef != null && data.getAssessmentBaseId() != null)
			transversalMap.put(oldRef, CoreAssessmentEntityProvider.ENTITY_PREFIX + "/" + data.getAssessmentBaseId());
		}

	}
	
	public void copyAssessment(String assessmentId, String apepndCopyTitle) {
		AssessmentData assessmentData = loadAssessment(Long.valueOf(assessmentId));
		assessmentData.setSectionSet(getSectionSetForAssessment(assessmentData));
		
		AssessmentData newAssessmentData = prepareAssessment(assessmentData, ServerConfigurationService.getServerUrl(), AgentFacade.getCurrentSiteId());
		updateTitleForCopy(newAssessmentData, apepndCopyTitle);
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

    private void updateTitleForCopy(AssessmentData assessmentData, String apepndCopyTitle){
    	//String apepndCopyTitle = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "append_copy_title");
    	StringBuffer sb = new StringBuffer(assessmentData.getTitle());
    	sb.append(" ");
    	sb.append(apepndCopyTitle);
    	if(sb.length() >= assessmentData.TITLE_LENGTH){ //title max size
    		String appendCopyText = "... "+apepndCopyTitle;
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
    	String query = "select a.title"
    		+ " from AssessmentData a, AuthorizationData z where "
    		+ " a.title like ? and z.functionId='EDIT_ASSESSMENT' and "
    		+ " a.assessmentBaseId=z.qualifierId and z.agentIdString=?";

    	final String hql = query;
    	final String titlef = title + "%";
    	HibernateCallback hcb = new HibernateCallback() {
    		public Object doInHibernate(Session session)
    		throws HibernateException, SQLException {
    			Query q = session.createQuery(hql);
    			q.setString(0, titlef);
    			q.setString(1, currentSiteId);
    			return q.list();
    		};
    	};
    	List list = getHibernateTemplate().executeFind(hcb);

    	int startIndex = title.length();
    	int maxNumCopy = 0;
    	if (list.size() > 0) {
    		// query in mysql & hsqldb are not case sensitive, check that title
    		// found is indeed what we
    		// are looking
    		for (int i = 0; i < list.size(); i++) {
    			String existingTitle = ((String) list.get(i)).trim();
    			if (existingTitle.startsWith(title)) {
    				try{
    					int numCopy = Integer.parseInt(existingTitle.substring(startIndex));
    					if (numCopy > maxNumCopy) {
    						maxNumCopy = numCopy;
    					}
    				}
    				catch(NumberFormatException e){
    					log.error("existingTitle = " + existingTitle + ", title = " + title + ", startIndex = " + startIndex + ", error message: " + e.getMessage());
    				}
    			}
    		}
    	}
    	log.debug("maxNumCopy = " + maxNumCopy);
    	int nextNumCopy = maxNumCopy + 1;
    	String newAssessmentTitle = title + nextNumCopy;
    	return newAssessmentTitle;
    }
    
    public AssessmentData prepareAssessment(AssessmentData a, String protocol, String toContext) {
		AssessmentData newAssessment = new AssessmentData(new Long("0"), a
				.getTitle(), a.getDescription(), a.getComments(), a.getAssessmentTemplateId(),
				TypeFacade.HOMEWORK, a.getInstructorNotification(), a
						.getTesteeNotification(), a.getMultipartAllowed(), a
						.getStatus(), AgentFacade.getAgentString(), new Date(), 
						AgentFacade.getAgentString(), new Date());
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
		Set newMetaDataSet = prepareAssessmentMetaDataSet(newAssessment, a
				.getAssessmentMetaDataSet());
		log.debug(" metadata set" + a.getAssessmentMetaDataSet());
		log.debug(" new metadata set" + newMetaDataSet);
		newAssessment.setAssessmentMetaDataSet(newMetaDataSet);
		// let's check if we need a newUrl
		String releaseTo = newAccessControl.getReleaseTo();
		if (releaseTo != null) {
			boolean anonymousAllowed = ((releaseTo)
					.indexOf(AuthoringConstantStrings.ANONYMOUS) > -1);
			if (anonymousAllowed) {
				// generate an alias to the pub assessment
				String alias = AgentFacade.getAgentString()
						+ (new Date()).getTime();
				AssessmentMetaData meta = new AssessmentMetaData(newAssessment,
						"ALIAS", alias);
				newMetaDataSet.add(meta);
				newAssessment.setAssessmentMetaDataSet(newMetaDataSet);
			}
			else {
				// if it's not anonymous, then set it to the whole site (removes group access too)
				if(toContext != null){
					releaseTo = toContext;
					try{
						Site toSite = SiteService.getSite(toContext);
						releaseTo = toSite.getTitle();
					}catch (IdUnusedException e) {
						log.debug("IdUnusedException: " + e.getMessage());
					}
					newAccessControl.setReleaseTo(releaseTo);
				}
			}
		}
		else {
			releaseTo = toContext;
			try{
				Site toSite = SiteService.getSite(toContext);
				releaseTo = toSite.getTitle();
			}catch (IdUnusedException e) {
				log.debug("IdUnusedException: " + e.getMessage());
			}
			newAccessControl.setReleaseTo(releaseTo);
		}
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
		return prepareAssessment(a, protocol, null);
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
				.getShowStatistics());
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

	public Set prepareAssessmentMetaDataSet(AssessmentData p, Set metaDataSet) {
		HashSet h = new HashSet();
		Iterator i = metaDataSet.iterator();
		while (i.hasNext()) {
			AssessmentMetaData metaData = (AssessmentMetaData) i.next();
			AssessmentMetaData newMetaData = new AssessmentMetaData(p, metaData
					.getLabel(), metaData.getEntry());
			h.add(newMetaData);
		}
		return h;
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
					null, null, null, // set ItemTextSet, itemMetaDataSet and
					// itemFeedbackSet later
					item.getTriesAllowed(), item.getPartialCreditFlag());
			Set newItemTextSet = prepareItemTextSet(newItem, item
					.getItemTextSet(), protocol, toContext);
			Set newItemMetaDataSet = prepareItemMetaDataSet(newItem, item
					.getItemMetaDataSet());
			Set newItemFeedbackSet = prepareItemFeedbackSet(newItem, item
					.getItemFeedbackSet());
			Set newItemAttachmentSet = prepareItemAttachmentSet(newItem, item
					.getItemAttachmentSet(), protocol, toContext);
			newItem.setItemTextSet(newItemTextSet);
			newItem.setItemMetaDataSet(newItemMetaDataSet);
			newItem.setItemFeedbackSet(newItemFeedbackSet);
			newItem.setItemAttachmentSet(newItemAttachmentSet);
			newItem.setAnswerOptionsRichCount(item.getAnswerOptionsRichCount());
			newItem.setAnswerOptionsSimpleOrRich(item.getAnswerOptionsSimpleOrRich());
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
	

	public Set prepareItemFeedbackSet(ItemData newItem, Set itemFeedbackSet) {
		HashSet h = new HashSet();
		Iterator o = itemFeedbackSet.iterator();
		while (o.hasNext()) {
			ItemFeedback itemFeedback = (ItemFeedback) o.next();
			ItemFeedback newItemFeedback = new ItemFeedback(newItem,
					itemFeedback.getTypeId(), itemFeedback.getText());
			h.add(newItemFeedback);
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

	public Set prepareSectionAttachmentSet(SectionData newSection,
			Set sectionAttachmentSet, String protocol) {
		return prepareSectionAttachmentSet(newSection, sectionAttachmentSet, protocol, null);
	}
	
	public Set prepareAssessmentAttachmentSet(AssessmentData newAssessment,
			Set assessmentAttachmentSet, String protocol, String toContext) {
		HashSet h = new HashSet();
		Iterator o = assessmentAttachmentSet.iterator();
		while (o.hasNext()) {
			AssessmentAttachment assessmentAttachment = (AssessmentAttachment) o
					.next();
			// create a copy of the resource
			AssessmentService service = new AssessmentService();
			ContentResource cr_copy = service.createCopyOfContentResource(
					assessmentAttachment.getResourceId(), assessmentAttachment
							.getFilename(), toContext);

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
	    String query =
	        "select a from AuthorizationData a where "+
	        " a.functionId=? and a.qualifierId=?";
	    Object [] values = {"EDIT_ASSESSMENT", assessmentId};
	    List l = getHibernateTemplate().find(query, values);
	    if (l.size()>0){
	      AuthorizationData a = (AuthorizationData) l.get(0);
	      return a.getAgentIdString();
	    }
	    else return null;
  }
  
  public String getAssessmentCreatedBy(String assessmentId) {
    String query = "select a from AssessmentData a where a.assessmentBaseId = ?";
    List l = getHibernateTemplate().find(query, Long.valueOf(assessmentId));
    if (l.size()>0){
    	AssessmentData a = (AssessmentData) l.get(0);
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
}
