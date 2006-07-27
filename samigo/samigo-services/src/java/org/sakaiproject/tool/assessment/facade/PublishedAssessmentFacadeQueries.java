/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/PublishedAssessmentFacadeQueries.java $
 * $Id: PublishedAssessmentFacadeQueries.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Vector;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;

public class PublishedAssessmentFacadeQueries
    extends HibernateDaoSupport implements PublishedAssessmentFacadeQueriesAPI {

  private static Log log = LogFactory.getLog(PublishedAssessmentFacadeQueries.class);


  public static String STARTDATE = "assessmentAccessControl.startDate";
  public static String DUEDATE = "assessmentAccessControl.dueDate";
  public static String RETRACTDATE = "assessmentAccessControl.retractDate";
  public static String RELEASETO = "assessmentAccessControl.releaseTo";

  public static String PUB_RELEASETO = "releaseTo";
  public static String PUB_STARTDATE = "startDate";
  public static String PUB_DUEDATE = "dueDate";
  public static String TITLE = "title";
  public static String GRADE = "finalScore";
  public static String DUE = "dueDate";
  public static String RAW = "totalAutoScore";
  public static String TIME = "timeElapsed";
  public static String SUBMITTED = "submittedDate";

  public PublishedAssessmentFacadeQueries()
  {
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


  public PublishedAssessmentData preparePublishedAssessment(AssessmentData a) {
    PublishedAssessmentData publishedAssessment = new PublishedAssessmentData(a.
        getTitle(), a.getDescription(),
        a.getComments(), TypeFacade.HOMEWORK, a.getInstructorNotification(),
        a.getTesteeNotification(), a.getMultipartAllowed(),
        a.getStatus(), AgentFacade.getAgentString(), new Date(),
        AgentFacade.getAgentString(), new Date());
    //publishedAssessment.setAssessment(a);
    publishedAssessment.setAssessmentId(a.getAssessmentBaseId());

    // section set
    Set publishedSectionSet = preparePublishedSectionSet(publishedAssessment,
        a.getSectionSet());
    publishedAssessment.setSectionSet(publishedSectionSet);

    // access control
    PublishedAccessControl publishedAccessControl =
        preparePublishedAccessControl(publishedAssessment,
                                      (AssessmentAccessControl) a.
                                      getAssessmentAccessControl());
    publishedAssessment.setAssessmentAccessControl(publishedAccessControl);

    // evaluation model
    PublishedEvaluationModel publishedEvaluationModel =
        preparePublishedEvaluationModel(publishedAssessment,
                                        (EvaluationModel) a.
                                        getEvaluationModel());
    publishedAssessment.setEvaluationModel(publishedEvaluationModel);

    // feedback
    PublishedFeedback publishedFeedback = preparePublishedFeedback(
        publishedAssessment,
        (AssessmentFeedback) a.getAssessmentFeedback());
    publishedAssessment.setAssessmentFeedback(publishedFeedback);

    // metadata
    Set publishedMetaDataSet = preparePublishedMetaDataSet(publishedAssessment,
        a.getAssessmentMetaDataSet());
    log.debug("******* metadata set" + a.getAssessmentMetaDataSet());
    log.debug("******* published metadata set" + publishedMetaDataSet);
    publishedAssessment.setAssessmentMetaDataSet(publishedMetaDataSet);

    // let's check if we need a publishedUrl
    String releaseTo = publishedAccessControl.getReleaseTo();
    if (releaseTo != null) {
      boolean anonymousAllowed = ( (releaseTo).indexOf(
          AuthoringConstantStrings.ANONYMOUS) > -1);
      if (anonymousAllowed) {
        // generate an alias to the pub assessment
        String alias = AgentFacade.getAgentString() + (new Date()).getTime();
        PublishedMetaData meta = new PublishedMetaData(publishedAssessment,
            "ALIAS", alias);
        publishedMetaDataSet.add(meta);
        publishedAssessment.setAssessmentMetaDataSet(publishedMetaDataSet);
      }
    }

    // IPAddress
    Set publishedIPSet = preparePublishedSecuredIPSet(publishedAssessment,
        a.getSecuredIPAddressSet());
    publishedAssessment.setSecuredIPAddressSet(publishedIPSet);

    return publishedAssessment;
  }

  public PublishedFeedback preparePublishedFeedback(PublishedAssessmentData p,
      AssessmentFeedback a) {
    if (a == null) {
      return null;
    }
    PublishedFeedback publishedFeedback = new PublishedFeedback(
        a.getFeedbackDelivery(),a.getFeedbackAuthoring(), a.getEditComponents(), a.getShowQuestionText(),
        a.getShowStudentResponse(), a.getShowCorrectResponse(),
        a.getShowStudentScore(),
        a.getShowStudentQuestionScore(),
        a.getShowQuestionLevelFeedback(), a.getShowSelectionLevelFeedback(),
        a.getShowGraderComments(), a.getShowStatistics());
    publishedFeedback.setAssessmentBase(p);
    return publishedFeedback;
  }

  public PublishedAccessControl preparePublishedAccessControl(
      PublishedAssessmentData p, AssessmentAccessControl a) {
    if (a == null) {
      return new PublishedAccessControl();
    }
    PublishedAccessControl publishedAccessControl = new PublishedAccessControl(
        a.getSubmissionsAllowed(), a.getSubmissionsSaved(),
        a.getAssessmentFormat(),
        a.getBookMarkingItem(), a.getTimeLimit(), a.getTimedAssessment(),
        a.getRetryAllowed(), a.getLateHandling(), a.getStartDate(),
        a.getDueDate(), a.getScoreDate(), a.getFeedbackDate());
    publishedAccessControl.setRetractDate(a.getRetractDate());
    publishedAccessControl.setAutoSubmit(a.getAutoSubmit());
    publishedAccessControl.setItemNavigation(a.getItemNavigation());
    publishedAccessControl.setItemNumbering(a.getItemNumbering());
    publishedAccessControl.setSubmissionMessage(a.getSubmissionMessage());
    publishedAccessControl.setReleaseTo(a.getReleaseTo());
    publishedAccessControl.setUsername(a.getUsername());
    publishedAccessControl.setPassword(a.getPassword());
    publishedAccessControl.setFinalPageUrl(a.getFinalPageUrl());
    publishedAccessControl.setUnlimitedSubmissions(a.getUnlimitedSubmissions());
    publishedAccessControl.setAssessmentBase(p);
    return publishedAccessControl;
  }

  public PublishedEvaluationModel preparePublishedEvaluationModel(
      PublishedAssessmentData p, EvaluationModel e) {
    if (e == null) {
      return null;
    }
    PublishedEvaluationModel publishedEvaluationModel = new
        PublishedEvaluationModel(
        e.getEvaluationComponents(), e.getScoringType(),
        e.getNumericModelId(), e.getFixedTotalScore(),
        e.getGradeAvailable(), e.getIsStudentIdPublic(),
        e.getAnonymousGrading(), e.getAutoScoring(),
        e.getToGradeBook());
    publishedEvaluationModel.setAssessmentBase(p);
    return publishedEvaluationModel;
  }

  public Set preparePublishedMetaDataSet(PublishedAssessmentData p,
                                         Set metaDataSet) {
    HashSet h = new HashSet();
    Iterator i = metaDataSet.iterator();
    while (i.hasNext()) {
      AssessmentMetaData metaData = (AssessmentMetaData) i.next();
      PublishedMetaData publishedMetaData = new PublishedMetaData(
          p, metaData.getLabel(), metaData.getEntry());
      h.add(publishedMetaData);
    }
    return h;
  }

  public Set preparePublishedSecuredIPSet(PublishedAssessmentData p, Set ipSet) {
    HashSet h = new HashSet();
    Iterator i = ipSet.iterator();
    if (ipSet != null) {
      while (i.hasNext()) {
        SecuredIPAddress ip = (SecuredIPAddress) i.next();
        PublishedSecuredIPAddress publishedIP = new PublishedSecuredIPAddress(
            p, ip.getHostname(), ip.getIpAddress());
        h.add(publishedIP);
      }
    }
    return h;
  }


  public Set preparePublishedSectionSet(PublishedAssessmentData
                                        publishedAssessment, Set sectionSet) {
    log.debug("**published section size = " + sectionSet.size());
    HashSet h = new HashSet();
    Iterator i = sectionSet.iterator();
    while (i.hasNext()) {
      SectionData section = (SectionData) i.next();

//TODO note: 4/28  need to check if a part is random draw , if it is
// then need to add questions from pool to this section, at this point,

      PublishedSectionData publishedSection = new PublishedSectionData(
          section.getDuration(), section.getSequence(), section.getTitle(),
          section.getDescription(), section.getTypeId(), section.getStatus(),
          section.getCreatedBy(), section.getCreatedDate(),
          section.getLastModifiedBy(),
          section.getLastModifiedDate());
      Set publishedItemSet = preparePublishedItemSet(publishedSection,
          section.getItemSet());
      publishedSection.setItemSet(publishedItemSet);
      Set publishedMetaDataSet = preparePublishedSectionMetaDataSet(publishedSection, section.getSectionMetaDataSet());
      publishedSection.setSectionMetaDataSet(publishedMetaDataSet);
      publishedSection.setAssessment(publishedAssessment);
      h.add(publishedSection);
    }
    return h;
  }

  public Set preparePublishedSectionMetaDataSet(PublishedSectionData publishedSection,
                                             Set metaDataSet) {
    HashSet h = new HashSet();
    Iterator n = metaDataSet.iterator();
    while (n.hasNext()) {
      SectionMetaData sectionMetaData = (SectionMetaData) n.next();
      PublishedSectionMetaData publishedSectionMetaData = new PublishedSectionMetaData(
          publishedSection, sectionMetaData.getLabel(), sectionMetaData.getEntry());
      h.add(publishedSectionMetaData);
    }
    return h;
  }

  public Set preparePublishedItemSet(PublishedSectionData publishedSection,
                                     Set itemSet) {
    log.debug("**published item size = " + itemSet.size());
    HashSet h = new HashSet();
    Iterator j = itemSet.iterator();
    while (j.hasNext()) {
      ItemData item = (ItemData) j.next();
      PublishedItemData publishedItem = new PublishedItemData(
          publishedSection, item.getSequence(), item.getDuration(),
          item.getInstruction(),
          item.getDescription(), item.getTypeId(), item.getGrade(),
          item.getScore(),
          item.getHint(), item.getHasRationale(), item.getStatus(),
          item.getCreatedBy(),
          item.getCreatedDate(), item.getLastModifiedBy(),
          item.getLastModifiedDate(),
          null, null, null, // set ItemTextSet, itemMetaDataSet and itemFeedbackSet later
          item.getTriesAllowed());
      Set publishedItemTextSet = preparePublishedItemTextSet(publishedItem,
          item.getItemTextSet());
      Set publishedItemMetaDataSet = preparePublishedItemMetaDataSet(
          publishedItem, item.getItemMetaDataSet());
      Set publishedItemFeedbackSet = preparePublishedItemFeedbackSet(
          publishedItem, item.getItemFeedbackSet());
      publishedItem.setItemTextSet(publishedItemTextSet);
      publishedItem.setItemMetaDataSet(publishedItemMetaDataSet);
      publishedItem.setItemFeedbackSet(publishedItemFeedbackSet);
      h.add(publishedItem);
    }
    return h;
  }

  public Set preparePublishedItemTextSet(PublishedItemData publishedItem,
                                         Set itemTextSet) {
    log.debug("**published item text size = " + itemTextSet.size());
    HashSet h = new HashSet();
    Iterator k = itemTextSet.iterator();
    while (k.hasNext()) {
      ItemText itemText = (ItemText) k.next();
      log.debug("**item text id =" + itemText.getId());
      PublishedItemText publishedItemText = new PublishedItemText(
          publishedItem, itemText.getSequence(), itemText.getText(), null);
      Set publishedAnswerSet = preparePublishedAnswerSet(publishedItemText,
          itemText.getAnswerSet());
      publishedItemText.setAnswerSet(publishedAnswerSet);
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
      PublishedItemMetaData publishedItemMetaData = new PublishedItemMetaData(
          publishedItem, itemMetaData.getLabel(), itemMetaData.getEntry());
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
          publishedItem, itemFeedback.getTypeId(), itemFeedback.getText());
      h.add(publishedItemFeedback);
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
          answer.getLabel(),
          answer.getIsCorrect(), answer.getGrade(), answer.getScore(), null);
      Set publishedAnswerFeedbackSet = preparePublishedAnswerFeedbackSet(
          publishedAnswer, answer.getAnswerFeedbackSet());
      publishedAnswer.setAnswerFeedbackSet(publishedAnswerFeedbackSet);
      h.add(publishedAnswer);
    }
    return h;
  }

  public Set preparePublishedAnswerFeedbackSet(PublishedAnswer publishedAnswer,
                                               Set answerFeedbackSet) {
    HashSet h = new HashSet();
    Iterator m = answerFeedbackSet.iterator();
    while (m.hasNext()) {
      AnswerFeedback answerFeedback = (AnswerFeedback) m.next();
      PublishedAnswerFeedback publishedAnswerFeedback = new
          PublishedAnswerFeedback(
          publishedAnswer, answerFeedback.getTypeId(), answerFeedback.getText());
      h.add(publishedAnswerFeedback);
    }
    return h;
  }

  public PublishedAssessmentFacade getPublishedAssessment(Long assessmentId) {
    PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
    a.setSectionSet(getSectionSetForAssessment(a));
    PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
    return f;
  }

  public Long getPublishedAssessmentId(Long assessmentId) {
    List list = getHibernateTemplate().find("from PublishedAssessmentData as p where p.assessmentId=? order by p.createdDate desc",assessmentId);
    Long publishedId = new Long(0);
    if (!list.isEmpty())
    {
     PublishedAssessmentData f = (PublishedAssessmentData)list.get(0);
     publishedId = f.getPublishedAssessmentId();
    }
    return publishedId;

  }

  public PublishedAssessmentFacade publishAssessment(AssessmentFacade
      assessment) throws Exception {
    boolean addedToGradebook = false;
    PublishedAssessmentData publishedAssessment = preparePublishedAssessment( (
        AssessmentData) assessment.getData());

    try{
      saveOrUpdate(publishedAssessment);
    }
    catch (Exception e){
      throw e;
    }

    // add to gradebook
    if (publishedAssessment.getEvaluationModel() != null){
      String toGradebook = publishedAssessment.getEvaluationModel().getToGradeBook();

      boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
      GradebookService g = null;
      if (integrated)
      {
        g = (GradebookService) SpringBeanLocator.getInstance().
          getBean("org.sakaiproject.service.gradebook.GradebookService");
      }

      GradebookServiceHelper gbsHelper =
        IntegrationContextFactory.getInstance().getGradebookServiceHelper();

      if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g) &&
          toGradebook !=null &&
          toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
        try {
            addedToGradebook = gbsHelper.addToGradebook(publishedAssessment, g);
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
    // because it will be deleted after preview is done, and shouldn't talk to gradebook.
  public PublishedAssessmentFacade publishPreviewAssessment(AssessmentFacade
      assessment) {
    boolean addedToGradebook = false;
    PublishedAssessmentData publishedAssessment = preparePublishedAssessment( (
        AssessmentData) assessment.getData());
    publishedAssessment.setStatus(PublishedAssessmentIfc.DEAD_STATUS);
    try{
      saveOrUpdate(publishedAssessment);
    }
    catch (Exception e){
      log.warn(e.getMessage());
    }
    // write authorization
    createAuthorization(publishedAssessment);
    return new PublishedAssessmentFacade(publishedAssessment);
  }


  public void createAuthorization(PublishedAssessmentData p) {
    String qualifierIdString = p.getPublishedAssessmentId().toString();
    Vector v = new Vector();

    //1. get all possible publishing targets (agentKey, agentId)
    PublishingTargetHelper ptHelper =
      IntegrationContextFactory.getInstance().getPublishingTargetHelper();

    HashMap targets = ptHelper.getTargets();

    // 2. get the key of the target selected, it is stored in accessControl.releaseTo
    AssessmentAccessControlIfc control = p.getAssessmentAccessControl();
    String releaseTo = control.getReleaseTo();
    if (releaseTo != null) {
      String[] targetSelected = releaseTo.split(",");
      for (int i = 0; i < targetSelected.length; i++) {
        String agentKey = targetSelected[i].trim();
        // add agentId into v
        if (targets.get(agentKey) != null) {
          v.add( (String) targets.get(agentKey));
        }
      }
    }
    // 3. give selected site right to view Published Assessment
    PersistenceService.getInstance().getAuthzQueriesFacade().
          createAuthorization(AgentFacade.getCurrentSiteId(),
                              "OWN_PUBLISHED_ASSESSMENT", qualifierIdString);
    // 4. create authorization for all the agentId in v
    for (int i = 0; i < v.size(); i++) {
      String agentId = (String) v.get(i);
      log.debug("** agentId=" + agentId);
      PersistenceService.getInstance().getAuthzQueriesFacade().
          createAuthorization(agentId,
                              "TAKE_PUBLISHED_ASSESSMENT", qualifierIdString);
      PersistenceService.getInstance().getAuthzQueriesFacade().
          createAuthorization(agentId,
                              "VIEW_PUBLISHED_ASSESSMENT_FEEDBACK", qualifierIdString);
      PersistenceService.getInstance().getAuthzQueriesFacade().
          createAuthorization(agentId,
                              "GRADE_PUBLISHED_ASSESSMENT", qualifierIdString);
      PersistenceService.getInstance().getAuthzQueriesFacade().
          createAuthorization(agentId,
                              "VIEW_PUBLISHED_ASSESSMENT", qualifierIdString);
    }
  }

  public AssessmentData loadAssessment(Long assessmentId) {
    return (AssessmentData) getHibernateTemplate().
        load(AssessmentData.class, assessmentId);
  }

  public PublishedAssessmentData loadPublishedAssessment(Long assessmentId) {
    return (PublishedAssessmentData) getHibernateTemplate().
        load(PublishedAssessmentData.class, assessmentId);
  }

  public ArrayList getAllTakeableAssessments(String orderBy, boolean ascending,
                                             final Integer status) {

    String query =
        "from PublishedAssessmentData as p where p.status=? order by p." +
        orderBy;

    if (ascending) {
        query += " asc";
    }
    else {
      query += " desc";
    }
    log.debug("Order by " + orderBy);

    final String hql = query;
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(hql);
    		q.setInteger(0, status.intValue());
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(query, new Object[] {status}
//                                            ,
//                                            new org.hibernate.type.Type[] {
//                                            Hibernate.
//                                            INTEGER});
    ArrayList assessmentList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      PublishedAssessmentData a = (PublishedAssessmentData) list.get(i);
      log.debug("Title: " + a.getTitle());
      // Don't need sections for list of assessments
      //a.setSectionSet(getSectionSetForAssessment(a));
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
      assessmentList.add(f);
    }
    return assessmentList;
  }

  public Integer getNumberOfSubmissions(final String publishedAssessmentId,
                                        final String agentId) {
    final String query = "select count(a) from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=?";
//    Object[] objects = new Object[3];
//    objects[0] = new Long(publishedAssessmentId);
//    objects[1] = agentId;
//    objects[2] = new Boolean(true);
//    Type[] types = new Type[3];
//    types[0] = Hibernate.LONG;
//    types[1] = Hibernate.STRING;
//    types[2] = Hibernate.BOOLEAN;

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, Long.parseLong(publishedAssessmentId));
    		q.setString(1, agentId);
    		q.setBoolean(2, true);
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(query, objects, types);
    return (Integer) list.get(0);
  }

  public List getNumberOfSubmissionsOfAllAssessmentsByAgent(final String agentId) {
    final String query = "select new AssessmentGradingData(" +
        " a.publishedAssessmentId, count(a)) " +
        " from AssessmentGradingData as a where a.agentId=? and a.forGrade=?" +
        " group by a.publishedAssessmentId";
//    Object[] objects = new Object[2];
//    objects[0] = agentId;
//    objects[1] = new Boolean(true);
//    Type[] types = new Type[2];
//    types[0] = Hibernate.STRING;
//    types[1] = Hibernate.BOOLEAN;

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setString(0, agentId);
    		q.setBoolean(1, true);
    		return q.list();
    	};
    };
    return getHibernateTemplate().executeFind(hcb);

//    return getHibernateTemplate().find(query, objects, types);
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

  public ArrayList getAllPublishedAssessments(String sortString, final Integer status) {
    final String orderBy = getOrderBy(sortString);

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("from PublishedAssessmentData as p where p.status=? order by p." +
    		        orderBy);
    		q.setInteger(0, status.intValue());
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(
//        "from PublishedAssessmentData as p where p.status=? order by p." +
//        orderBy,
//        new Object[] {status}
//        , new org.hibernate.type.Type[] {Hibernate.INTEGER});
    ArrayList assessmentList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      PublishedAssessmentData a = (PublishedAssessmentData) list.get(i);
      a.setSectionSet(getSectionSetForAssessment(a));
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
      assessmentList.add(f);
    }
    return assessmentList;
  }

  public ArrayList getAllPublishedAssessments(
      int pageSize, int pageNumber, String sortString, Integer status) {
    String orderBy = getOrderBy(sortString);
    String queryString = "from PublishedAssessmentData p order by p." + orderBy;
    if (!status.equals(PublishedAssessmentFacade.ANY_STATUS)) {
      queryString = "from PublishedAssessmentData p where p.status =" +
          status.intValue() + " order by p." + orderBy;
    }
    PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().
        getPagingUtilQueries();
    List pageList = pagingUtilQueries.getAll(pageSize, pageNumber, queryString);
    log.debug("**** pageList=" + pageList);
    ArrayList assessmentList = new ArrayList();
    for (int i = 0; i < pageList.size(); i++) {
      PublishedAssessmentData a = (PublishedAssessmentData) pageList.get(i);
      a.setSectionSet(getSectionSetForAssessment(a));
      log.debug("****  published assessment=" + a.getTitle());
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(a);
      log.debug("**** published assessment title=" + f.getTitle());
      assessmentList.add(f);
    }
    return assessmentList;
  }

  public void removeAssessment(Long assessmentId) {
    PublishedAssessmentData assessment = (PublishedAssessmentData)
        getHibernateTemplate().load(PublishedAssessmentData.class, assessmentId);
    // if AssessmentGradingData exist, simply set pub assessment to inactive
    // else delete assessment
    List count = getHibernateTemplate().find(
        "select count(g) from AssessmentGradingData g where g.publishedAssessmentId=?",
        assessment.getPublishedAssessmentId());
    log.debug("no. of Assessment Grading =" + count.size());
    Iterator iter = count.iterator();
    int i = ( (Integer) iter.next()).intValue();
    if (i > 0) {
      assessment.setStatus(PublishedAssessmentIfc.DEAD_STATUS);
      try{
        saveOrUpdate(assessment);
      }
      catch (Exception e){
        log.warn(e.getMessage());
      }
    }
    else {
      delete(assessment);
      // remove authorization
      PersistenceService.getInstance().getAuthzQueriesFacade().
          removeAuthorizationByQualifier(assessment.getPublishedAssessmentId().toString(), true);
    }
  }

  private String getOrderBy(String sortString) {
    String startDate = (PublishedAssessmentFacadeQueries.STARTDATE).substring(
        (PublishedAssessmentFacadeQueries.STARTDATE).lastIndexOf(".") + 1);
    String dueDate = (PublishedAssessmentFacadeQueries.DUEDATE).substring(
        (PublishedAssessmentFacadeQueries.DUEDATE).lastIndexOf(".") + 1);
    String releaseTo = (PublishedAssessmentFacadeQueries.RELEASETO).substring(
        (PublishedAssessmentFacadeQueries.RELEASETO).lastIndexOf(".") + 1);

    if ( (sortString).equals(startDate)) {
      return PublishedAssessmentFacadeQueries.STARTDATE;
    }
    else if ( (sortString).equals(dueDate)) {
      return PublishedAssessmentFacadeQueries.DUEDATE;
    }
    else if ( (sortString).equals(releaseTo)) {
      return PublishedAssessmentFacadeQueries.RELEASETO;
    }
    else {
      return PublishedAssessmentFacadeQueries.TITLE;
    }
  }

  public void saveOrUpdate(PublishedAssessmentIfc assessment) throws Exception{
    PublishedAssessmentData data;
    if (assessment instanceof PublishedAssessmentFacade)
      data = (PublishedAssessmentData)((PublishedAssessmentFacade) assessment).getData();
    else
      data = (PublishedAssessmentData) assessment;

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().saveOrUpdate(data);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem save or update assessment: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
        if (retryCount==0) throw e;
      }
    }
  }

  public ArrayList getBasicInfoOfAllActivePublishedAssessments(String
      sortString, final String siteAgentId, boolean ascending) {
    Date currentDate = new Date();
    String orderBy = getOrderBy(sortString);

    String query =
        "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "+
        " c.releaseTo, c.startDate, c.dueDate, c.retractDate) " +
        " from PublishedAssessmentData p, PublishedAccessControl c, AuthorizationData z  " +
        " where c.assessment = p and p.status=1 and " +
        " p.publishedAssessmentId=z.qualifierId and z.functionId='OWN_PUBLISHED_ASSESSMENT' " +
        " and z.agentIdString= ? order by p." + orderBy;
    if (ascending == true)
      query += " asc";
    else
      query += " desc";

    final String hql = query;
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(hql);
    		q.setString(0, siteAgentId);
    		return q.list();
    	};
    };
    List l = getHibernateTemplate().executeFind(hcb);

//    List l = getHibernateTemplate().find(query,
//        new Object[] {siteAgentId},
//        new org.hibernate.type.Type[] {Hibernate.STRING});

    // we will filter the one that is past duedate & retract date
    ArrayList list = new ArrayList();
    for (int j = 0; j < l.size(); j++) {
      PublishedAssessmentData p = (PublishedAssessmentData) l.get(j);
      if ( (p.getDueDate() == null || (p.getDueDate()).after(currentDate))
          &&
          (p.getRetractDate() == null || (p.getRetractDate()).after(currentDate))) {
        list.add(p);
      }
    }

    ArrayList pubList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      PublishedAssessmentData p = (PublishedAssessmentData) list.get(i);
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(
          p.getPublishedAssessmentId(), p.getTitle(),
          p.getReleaseTo(), p.getStartDate(), p.getDueDate());
      pubList.add(f);
    }
    return pubList;
  }

  /**
   * According to Marc inactive means either the dueDate or the retractDate has
   * passed for 1.5 release (IM on 12/17/04)
   * @param sortString
   * @return
   */
  public ArrayList getBasicInfoOfAllInActivePublishedAssessments(String
      sortString, final String siteAgentId, boolean ascending) {
    String orderBy = getOrderBy(sortString);
    Date currentDate = new Date();
    long currentTime = currentDate.getTime();
    String query = "select new PublishedAssessmentData(p.publishedAssessmentId, p.title,"+
                   " c.releaseTo, c.startDate, c.dueDate, c.retractDate) from PublishedAssessmentData p,"+
                   " PublishedAccessControl c, AuthorizationData z  " +
                   " where c.assessment=p and (p.status=0 or c.dueDate< ? or  c.retractDate< ?)" +
                   " and p.publishedAssessmentId=z.qualifierId and z.functionId='OWN_PUBLISHED_ASSESSMENT' " +
                   " and z.agentIdString= ? order by p." + orderBy;

    if (ascending)
      query += " asc";
    else
      query += " desc";

    final String hql = query;
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(hql);
    		q.setTimestamp(0, new Date());
    		q.setTimestamp(1, new Date());
    		q.setString(2, siteAgentId);
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(query,
//        new Object[] {new Date(), new Date(),siteAgentId} ,
//        new org.hibernate.type.Type[] {Hibernate.TIMESTAMP, Hibernate.TIMESTAMP, Hibernate.STRING});

    ArrayList pubList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      PublishedAssessmentData p = (PublishedAssessmentData) list.get(i);
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(
          p.getPublishedAssessmentId(), p.getTitle(),
          p.getReleaseTo(), p.getStartDate(), p.getDueDate());
      pubList.add(f);
    }
    return pubList;
  }

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
  public HashSet getSectionSetForAssessment(PublishedAssessmentIfc assessment) {
    List sectionList = getHibernateTemplate().find(
        "from PublishedSectionData s where s.assessment.publishedAssessmentId=" +
        assessment.getPublishedAssessmentId());
    HashSet set = new HashSet();
    for (int j = 0; j < sectionList.size(); j++) {
      set.add( (PublishedSectionData) sectionList.get(j));
    }
    return set;
  }

  // IMPORTANT:
  // 1. we do not want any Section info, so set loadSection to false
  // 2. We have also declared SectionData as lazy loading. If loadSection is set
  // to true, we will see null pointer
  public PublishedAssessmentFacade getSettingsOfPublishedAssessment(Long
      assessmentId) {
    PublishedAssessmentData a = loadPublishedAssessment(assessmentId);
    Boolean loadSection = Boolean.FALSE;
    PublishedAssessmentFacade f = new PublishedAssessmentFacade(a, loadSection);
    return f;
  }

  public PublishedItemData loadPublishedItem(Long itemId) {
    return (PublishedItemData) getHibernateTemplate().
        load(PublishedItemData.class, itemId);
  }

  public PublishedItemText loadPublishedItemText(Long itemTextId) {
    return (PublishedItemText) getHibernateTemplate().
        load(PublishedItemText.class, itemTextId);
  }

  // added by daisy - please check the logic - I based this on the getBasicInfoOfAllActiveAssessment
  public ArrayList getBasicInfoOfAllPublishedAssessments(String orderBy,
      boolean ascending, final Integer status) {

    String query =
        "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, " +
        " c.releaseTo, c.startDate, c.dueDate, c.retractDate, " +
        " c.feedbackDate, f.feedbackDelivery,  f.feedbackAuthoring, c.lateHandling, " +
        " c.unlimitedSubmissions, c.submissionsAllowed) " +
        " from PublishedAssessmentData as p, PublishedAccessControl as c," +
        " PublishedFeedback as f" +
        " where c.assessment.publishedAssessmentId=p.publishedAssessmentId " +
        " and p.publishedAssessmentId = f.assessment.publishedAssessmentId " +
        " and p.status=? order by ";

    if (ascending == false) {

      if (orderBy.equals(DUE)) {
        query += " c." + orderBy + " desc";
      }
      else {
        query += " p." + orderBy + " desc";
      }
    }
    else {
      if (orderBy.equals(DUE)) {
        query += " c." + orderBy + " asc";
      }
      else {
        query += " p." + orderBy + " asc";
      }
    }

    final String hql = query;
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(hql);
    		q.setInteger(0, status.intValue());
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(query, new Object[] {status}
//                                            ,
//                                            new org.hibernate.type.Type[] {
//                                            Hibernate.INTEGER});

    ArrayList pubList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      PublishedAssessmentData p = (PublishedAssessmentData) list.get(i);
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(
          p.getPublishedAssessmentId(), p.getTitle(),
          p.getReleaseTo(), p.getStartDate(), p.getDueDate(),
          p.getRetractDate(), p.getFeedbackDate(), p.getFeedbackDelivery(), p.getFeedbackAuthoring(),
          p.getLateHandling(),
          p.getUnlimitedSubmissions(), p.getSubmissionsAllowed());
      pubList.add(f);
    }
    return pubList;
  }

  /**
       * return an array list of the last AssessmentGradingFacade per assessment that
   * a user has submitted for grade.
   * @param agentId
   * @param orderBy
   * @param ascending
   * @return
   */
  public ArrayList getBasicInfoOfLastSubmittedAssessments(final String agentId,
      String orderBy, boolean ascending) {
    // 1. get total no. of submission per assessment by the given agent
    HashMap h = getTotalSubmissionPerAssessment(agentId);
    final String query = "select new AssessmentGradingData(" +
        " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId," +
        " a.submittedDate, a.isLate," +
        " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore," +
        " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate," +
        " a.timeElapsed) " +
        " from AssessmentGradingData a, PublishedAssessmentData p" +
        " where a.publishedAssessmentId = p.publishedAssessmentId  and a.forGrade=1 and a.agentId=?" +
        " order by p.publishedAssessmentId DESC, a.submittedDate DESC";

    /* The sorting for each type will be done in the action listener.
         if (orderBy.equals(TITLE))
         {
      query += ", p." + orderBy;
         }
         else if (!orderBy.equals(SUBMITTED))
         {
     query += ", a." + orderBy;
         }
         if (!orderBy.equals(SUBMITTED))
         {
        if (ascending == false)
      {
       query += " desc";
      }
         else
      {
         query += " asc";
      }
         }
     */

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setString(0, agentId);
    		return q.list();
    	};
    };
    ArrayList list = (ArrayList) getHibernateTemplate().executeFind(hcb);
    
//    ArrayList list = (ArrayList) getHibernateTemplate().find(query,
//        new Object[] {agentId}
//        ,
//        new org.hibernate.type.Type[] {Hibernate.STRING});

    ArrayList assessmentList = new ArrayList();
    Long current = new Long("0");
//    Date currentDate = new Date();
    for (int i = 0; i < list.size(); i++) {
      AssessmentGradingData a = (AssessmentGradingData) list.get(i);
      // criteria: only want the most recently submitted assessment from a given user.
      if (!a.getPublishedAssessmentId().equals(current)) {
        current = a.getPublishedAssessmentId();
        AssessmentGradingFacade f = new AssessmentGradingFacade(a);
        assessmentList.add(f);
      }
    }
    return assessmentList;
  }

  /** total submitted for grade
       * returns HashMap (Long publishedAssessmentId, Integer totalSubmittedForGrade);
   */
  public HashMap getTotalSubmissionPerAssessment(String agentId) {
    List l = getNumberOfSubmissionsOfAllAssessmentsByAgent(agentId);
    HashMap h = new HashMap();
    for (int i = 0; i < l.size(); i++) {
      AssessmentGradingData d = (AssessmentGradingData) l.get(i);
      h.put(d.getPublishedAssessmentId(), new Integer(d.getTotalSubmitted()));
      log.debug("pId=" + d.getPublishedAssessmentId() + " submitted=" +
                         d.getTotalSubmitted());
    }
    return h;
  }

  public Integer getTotalSubmission(final String agentId, final Long publishedAssessmentId) {
    final String query =
        "select count(a) from AssessmentGradingData a where a.forGrade=1 " +
        " and a.agentId=? and a.publishedAssessmentId=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setString(0, agentId);
    		q.setLong(1, publishedAssessmentId.longValue());
    		return q.list();
    	};
    };
    List l = getHibernateTemplate().executeFind(hcb);

//    List l = getHibernateTemplate().find(query,
//                                         new Object[] {agentId,
//                                         publishedAssessmentId}
//                                         ,
//                                         new org.hibernate.type.Type[] {
//                                         Hibernate.STRING, Hibernate.LONG});
    return (Integer) l.get(0);
  }

  public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(String alias) {
    return getPublishedAssessmentIdByMetaLabel("ALIAS", alias);
  }

  public PublishedAssessmentFacade getPublishedAssessmentIdByMetaLabel(
      final String label, final String entry) {
    final String query = "select p " +
        " from PublishedAssessmentData p, " +
        " PublishedMetaData m where p=m.assessment " +
        " and m.label=? and m.entry=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setString(0, label);
    		q.setString(1, entry);
    		return q.list();
    	};
    };
    List l = getHibernateTemplate().executeFind(hcb);

//    List l = getHibernateTemplate().find(query,
//                                         new Object[] {label, entry}
//                                         ,
//                                         new org.hibernate.type.Type[] {
//                                         Hibernate.STRING, Hibernate.STRING});
    if (l.size() > 0) {
      PublishedAssessmentData p = (PublishedAssessmentData) l.get(0);   
      p.setSectionSet(getSectionSetForAssessment(p));
      PublishedAssessmentFacade f = new PublishedAssessmentFacade(p);
      return f;
    }
    else {
      return null;
    }
  }

  public void saveOrUpdateMetaData(PublishedMetaData meta) {
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().saveOrUpdate(meta);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem save or update meta data: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  public HashMap getFeedbackHash() {
    HashMap h = new HashMap();
    String query = "select new PublishedFeedback(" +
        " p.assessment.publishedAssessmentId," +
        " p.feedbackDelivery,  p.feedbackAuthoring, p.editComponents, p.showQuestionText," +
        " p.showStudentResponse, p.showCorrectResponse," +
        " p.showStudentScore," +
        " p.showStudentQuestionScore," +
        " p.showQuestionLevelFeedback, p.showSelectionLevelFeedback," +
        " p.showGraderComments, p.showStatistics)" +
        " from PublishedFeedback p";
    List l = getHibernateTemplate().find(query);
    for (int i = 0; i < l.size(); i++) {
      PublishedFeedback f = (PublishedFeedback) l.get(i);
      h.put(f.getAssessmentId(), f);
    }
    return h;
  }


  /** this return a HashMap containing
   *  (Long publishedAssessmentId, PublishedAssessmentFacade publishedAssessment)
   *  Note that the publishedAssessment is a partial object used for display only.
   *  do not use it for persisting. It only contains title, releaseTo, startDate, dueDate
   *  & retractDate
   */
  public HashMap getAllAssessmentsReleasedToAuthenticatedUsers(){
    HashMap h = new HashMap();
    String query =
        "select new PublishedAssessmentData(p.publishedAssessmentId, p.title, "+
        " c.releaseTo, c.startDate, c.dueDate, c.retractDate) " +
        " from PublishedAssessmentData p, PublishedAccessControl c  " +
        " where c.assessment = p and c.releaseTo like '%Authenticated Users%'";
    List l = getHibernateTemplate().find(query);
    for (int i = 0; i < l.size(); i++) {
      PublishedAssessmentData p = (PublishedAssessmentData) l.get(i);
      h.put(p.getPublishedAssessmentId(), new PublishedAssessmentFacade(p));
    }
    return h;
  }


  public String getPublishedAssessmentOwner(String publishedAssessmentId){
    //HashMap h = new HashMap();
    String query =
        "select a from AuthorizationData a where "+
        " a.functionId='OWN_PUBLISHED_ASSESSMENT' and a.qualifierId="+publishedAssessmentId;
    List l = getHibernateTemplate().find(query);
    if (l.size()>0){
      AuthorizationData a = (AuthorizationData) l.get(0);
      return a.getAgentIdString();
    }
    else return null;
  }

  public boolean publishedAssessmentTitleIsUnique(final Long assessmentBaseId, final String title) {
    final String currentSiteId = AgentFacade.getCurrentSiteId();
//    String agentString = AgentFacade.getAgentString();
//    List list;
    boolean isUnique = true;
    final String query = "select new PublishedAssessmentData(a.publishedAssessmentId, a.title, a.lastModifiedDate)"+
            " from PublishedAssessmentData a, AuthorizationData z where "+
            " a.title=? and a.publishedAssessmentId!=? and a.status!=2 and "+
            " z.functionId='OWN_PUBLISHED_ASSESSMENT' and " +
            " a.publishedAssessmentId=z.qualifierId and z.agentIdString=?";
    //System.out.println("query" + query);

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setString(0, title);
    		q.setLong(1, assessmentBaseId.longValue());
    		q.setString(2, currentSiteId);
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(query,
//           new Object[]{title,assessmentBaseId,currentSiteId},
//           new org.hibernate.type.Type[] {Hibernate.STRING, Hibernate.LONG, Hibernate.STRING});
    if (list.size()>0)
      isUnique = false;
    //System.out.println("*** list size="+list.size());
    return isUnique;
  }

  public boolean hasRandomPart(final Long publishedAssessmentId){
    boolean hasRandomPart = false;
    final String key = SectionDataIfc.AUTHOR_TYPE;
    final String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();
    final String query =
        "select s from PublishedSectionData s, PublishedSectionMetaData m where "+
        " s = m.section and s.assessment.publishedAssessmentId=? and " +
        " m.label=? and m.entry=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId.longValue());
    		q.setString(1, key);
    		q.setString(2, value);
    		return q.list();
    	};
    };
    List l = getHibernateTemplate().executeFind(hcb);

//    List l = getHibernateTemplate().find(query,
//      new Object[]{ publishedAssessmentId, key, value},
//      new org.hibernate.type.Type[] {Hibernate.LONG, Hibernate.STRING, Hibernate.STRING});
    if (l.size()>0)
      hasRandomPart=true;
    return hasRandomPart;
  }

  public PublishedItemData getFirstPublishedItem(final Long publishedAssessmentId)
  {
  	final String query =
  		"select i from PublishedAssessmentData p, PublishedSectionData s, "+
			" PublishedItemData i where p.publishedAssessmentId=? and"+
			" p.publishedAssessmentId=s.assessment.publishedAssessmentId and " +
			" s=i.section";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId.longValue());
    		return q.list();
    	};
    };
    List l = getHibernateTemplate().executeFind(hcb);

//  	List l = getHibernateTemplate().find(query,
//  			new Object[]{ publishedAssessmentId},
//				new org.hibernate.type.Type[] {Hibernate.LONG});
  	final String query2 = "select s from PublishedAssessmentData p, PublishedSectionData s, "+
		" where p.publishedAssessmentId=? and"+
		" p.publishedAssessmentId=s.assessment.publishedAssessmentId ";

    final HibernateCallback hcb2 = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query2);
    		q.setLong(0, publishedAssessmentId.longValue());
    		return q.list();
    	};
    };
    List sec = getHibernateTemplate().executeFind(hcb2);

//  	List sec = getHibernateTemplate().find(query,
//  			new Object[]{ publishedAssessmentId },
//				new org.hibernate.type.Type[] {Hibernate.LONG});
  	PublishedItemData returnItem = null;
  	if(sec.size() > 0 && l.size() >0)
  	{
  		Collections.sort(sec, new SecComparator());
  		for(int i=0; i<sec.size(); i++)
  		{
  			PublishedSectionData thisSec = (PublishedSectionData)sec.get(i);
  			ArrayList itemList = new ArrayList();
  			for(int j=0; j<l.size(); j++)
  			{
  				PublishedItemData compItem = (PublishedItemData) l.get(j);
  				if(compItem.getSection().getSectionId().equals(thisSec.getSectionId()))
  				{
  					itemList.add(compItem);
  				}
  			}
  			if(itemList.size() > 0)
  			{
  				Collections.sort(itemList, new ItemComparator());
  				returnItem = (PublishedItemData)itemList.get(0);
  				break;
  			}
  		}
  	}
  	return returnItem;
  }

  public List getPublishedItemIds(final Long publishedAssessmentId){
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select i.itemId from PublishedItemData i, PublishedSectionData s, "+
	    		         " PublishedAssessmentData p where p.publishedAssessmentId=? and "+
	    		         " p = s.assessment and i.section = s");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		return q.list();
	    	};
	    };
	    return getHibernateTemplate().executeFind(hcb);

//	  return getHibernateTemplate().find(
//         "select i.itemId from PublishedItemData i, PublishedSectionData s, "+
//         " PublishedAssessmentData p where p.publishedAssessmentId=? and "+
//         " p = s.assessment and i.section = s",
//         new Object[] { publishedAssessmentId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });
  }

  public Integer getItemType(final Long publishedItemId){
    final String query = "select p.typeId "+
                   " from PublishedItemData p "+
	" where p.publishedItemId=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedItemId.longValue());
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find(query,
//					    new Object[] { publishedItemId },
//					    new org.hibernate.type.Type[] { Hibernate.LONG });
    if (list.size()>0)
	return (Integer)list.get(0);
    else
	return null;
    }

    class SecComparator implements Comparator{
      public int compare(Object arg0, Object arg1){
       return ((PublishedSectionData)arg0).getSequence().compareTo(((PublishedSectionData)arg1).getSequence());
      }
    }

  class ItemComparator implements Comparator{
    public int compare(Object arg0, Object arg1){
      return ((PublishedItemData)arg0).getSequence().compareTo(((PublishedItemData)arg1).getSequence());
    }
  }

  public void delete(PublishedAssessmentIfc assessment){
    PublishedAssessmentData data;
    if (assessment instanceof PublishedAssessmentFacade)
      data = (PublishedAssessmentData)((PublishedAssessmentFacade) assessment).getData();
    else
      data = (PublishedAssessmentData) assessment;

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().delete(data);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem removing publishedAssessment: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

}
