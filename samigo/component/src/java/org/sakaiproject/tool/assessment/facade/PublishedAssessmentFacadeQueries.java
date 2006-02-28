/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;

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
    ////AssessmentData assessment = (AssessmentData) getHibernateTemplate().load(AssessmentData.class, assessmentId);
    List list = getHibernateTemplate().find("from PublishedAssessmentData as p where p.assessmentId=? order by p.createdDate desc",assessmentId);
    Long publishedId = new Long(0);
    if (!list.isEmpty())
    {
     PublishedAssessmentData f = (PublishedAssessmentData)list.get(0);
     publishedId = f.getPublishedAssessmentId();
    }
    return publishedId;

  }


  public static void print(AssessmentBaseIfc a) {
    log.debug("**assessment base Id #" + a.getAssessmentBaseId());
    log.debug("**assessment title #" + a.getTitle());
    log.debug("**assessment is template? " + a.getIsTemplate());
    if (a.getIsTemplate().equals(Boolean.FALSE)) {
  log.debug("**assessmentTemplateId #" +
                         ( (AssessmentData) a).getAssessmentTemplateId());
  log.debug("**section: " +
                         ( (AssessmentData) a).getSectionSet());
    }
    if (a.getAssessmentAccessControl() != null) {
  log.debug("**assessment due date: " +
                         a.getAssessmentAccessControl().getDueDate());
    }
    if (a.getAssessmentMetaDataSet() != null) {
  log.debug("**assessment metadata" +
                         a.getAssessmentMetaDataSet());
  log.debug("**Objective not lazy = " +
                         a.getAssessmentMetaDataByLabel("ASSESSMENT_OBJECTIVE"));
    }

  }

  public static void printPublished(PublishedAssessmentData a) {
      log.debug("**assessment published Id #" +
                       a.getPublishedAssessmentId());
      log.debug("**assessment title #" + a.getTitle());
    if (a.getAssessmentAccessControl() != null) {
      log.debug("**assessment due date: " +
                       a.getAssessmentAccessControl().getDueDate());
    }
    if (a.getAssessmentMetaDataSet() != null) {
      log.debug("**assessment metadata" +
                       a.getAssessmentMetaDataSet());
      log.debug("**Objective not lazy = " +
                       a.getAssessmentMetaDataByLabel("ASSESSMENT_OBJECTIVE"));
    }

  }

  public PublishedAssessmentFacade publishAssessment(AssessmentFacade
      assessment) throws Exception {
    boolean addedToGradebook = false;
    PublishedAssessmentData publishedAssessment = preparePublishedAssessment( (
        AssessmentData) assessment.getData());

    getHibernateTemplate().save(publishedAssessment);
    log.debug("**** save publish Assessment="+publishedAssessment);

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
            getHibernateTemplate().delete(publishedAssessment);
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
    getHibernateTemplate().save(publishedAssessment);
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
                                             Integer status) {

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
    List list = getHibernateTemplate().find(query, new Object[] {status}
                                            ,
                                            new net.sf.hibernate.type.Type[] {
                                            Hibernate.
                                            INTEGER});
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
                                        String agentId) {
    String query = "select count(a) from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? and a.forGrade=?";
    Object[] objects = new Object[3];
    objects[0] = new Long(publishedAssessmentId);
    objects[1] = agentId;
    objects[2] = new Boolean(true);
    Type[] types = new Type[3];
    types[0] = Hibernate.LONG;
    types[1] = Hibernate.STRING;
    types[2] = Hibernate.BOOLEAN;
    List list = getHibernateTemplate().find(query, objects, types);
    return (Integer) list.get(0);
  }

  public List getNumberOfSubmissionsOfAllAssessmentsByAgent(String agentId) {
    String query = "select new AssessmentGradingData(" +
        " a.publishedAssessment.publishedAssessmentId, count(a)) " +
        " from AssessmentGradingData as a where a.agentId=? and a.forGrade=?" +
        " group by a.publishedAssessment.publishedAssessmentId";
    Object[] objects = new Object[2];
    objects[0] = agentId;
    objects[1] = new Boolean(true);
    Type[] types = new Type[2];
    types[0] = Hibernate.STRING;
    types[1] = Hibernate.BOOLEAN;
    return getHibernateTemplate().find(query, objects, types);
  }

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
              log.debug("Added new submission " +
                                 data.getAssessmentGradingId());
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

  public ArrayList getAllPublishedAssessments(String sortString, Integer status) {
    String orderBy = getOrderBy(sortString);
    List list = getHibernateTemplate().find(
        "from PublishedAssessmentData as p where p.status=? order by p." +
        orderBy,
        new Object[] {status}
        , new net.sf.hibernate.type.Type[] {Hibernate.INTEGER});
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
        "select count(g) from AssessmentGradingData g where g.publishedAssessment=?",
        assessment);
    log.debug("no. of Assessment Grading =" + count.size());
    Iterator iter = count.iterator();
    int i = ( (Integer) iter.next()).intValue();
    if (i > 0) {
      assessment.setStatus(PublishedAssessmentIfc.DEAD_STATUS);
      getHibernateTemplate().update(assessment);
    }
    else {
      getHibernateTemplate().delete(assessment);
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

  public void saveOrUpdate(PublishedAssessmentFacade assessment) {
    PublishedAssessmentData data = (PublishedAssessmentData) assessment.getData();
    getHibernateTemplate().saveOrUpdate(data);
  }

  public ArrayList getBasicInfoOfAllActivePublishedAssessments(String
      sortString, String siteAgentId, boolean ascending) {
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
    List l = getHibernateTemplate().find(query,
        new Object[] {siteAgentId},
        new net.sf.hibernate.type.Type[] {Hibernate.STRING});

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
      sortString, String siteAgentId, boolean ascending) {
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

    List list = getHibernateTemplate().find(query,
        new Object[] {new Date(), new Date(),siteAgentId} ,
        new net.sf.hibernate.type.Type[] {Hibernate.TIMESTAMP, Hibernate.TIMESTAMP, Hibernate.STRING});

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
  public HashSet getSectionSetForAssessment(PublishedAssessmentData assessment) {
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
      boolean ascending, Integer status) {

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

    List list = getHibernateTemplate().find(query, new Object[] {status}
                                            ,
                                            new net.sf.hibernate.type.Type[] {
                                            Hibernate.INTEGER});

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
  public ArrayList getBasicInfoOfLastSubmittedAssessments(String agentId,
      String orderBy, boolean ascending) {
    // 1. get total no. of submission per assessment by the given agent
    HashMap h = getTotalSubmissionPerAssessment(agentId);
    String query = "select new AssessmentGradingData(" +
        " a.assessmentGradingId, p.publishedAssessmentId, p.title, a.agentId," +
        " a.submittedDate, a.isLate," +
        " a.forGrade, a.totalAutoScore, a.totalOverrideScore,a.finalScore," +
        " a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate," +
        " a.timeElapsed) " +
        " from AssessmentGradingData a, PublishedAssessmentData p" +
        " where a.publishedAssessment = p  and a.forGrade=1 and a.agentId=?" +
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

    ArrayList list = (ArrayList) getHibernateTemplate().find(query,
        new Object[] {agentId}
        ,
        new net.sf.hibernate.type.Type[] {Hibernate.STRING});

    ArrayList assessmentList = new ArrayList();
    Long current = new Long("0");
    Date currentDate = new Date();
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

  public Integer getTotalSubmission(String agentId, Long publishedAssessmentId) {
    String query =
        "select count(a) from AssessmentGradingData a where a.forGrade=1 " +
        " and a.agentId=? and a.publishedAssessment.publishedAssessmentId=?";
    List l = getHibernateTemplate().find(query,
                                         new Object[] {agentId,
                                         publishedAssessmentId}
                                         ,
                                         new net.sf.hibernate.type.Type[] {
                                         Hibernate.STRING, Hibernate.LONG});
    return (Integer) l.get(0);
  }

  public PublishedAssessmentFacade getPublishedAssessmentIdByAlias(String alias) {
    return getPublishedAssessmentIdByMetaLabel("ALIAS", alias);
  }

  public PublishedAssessmentFacade getPublishedAssessmentIdByMetaLabel(
      String label, String entry) {
    /**
         String query = "select new PublishedAssessmentData("+
      " p.publishedAssessmentId, p.title, "+
      " c.releaseTo, c.startDate, c.dueDate, c.retractDate) " +
      " from PublishedAssessmentData p, PublishedAccessControl c, " +
      " PublishedMetaData m where p=c.assessment and p=m.assessment "+
      " and m.label=? and m.entry=?";
     */
    String query = "select p " +
        " from PublishedAssessmentData p, " +
        " PublishedMetaData m where p=m.assessment " +
        " and m.label=? and m.entry=?";
    List l = getHibernateTemplate().find(query,
                                         new Object[] {label, entry}
                                         ,
                                         new net.sf.hibernate.type.Type[] {
                                         Hibernate.STRING, Hibernate.STRING});
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
    getHibernateTemplate().saveOrUpdate(meta);
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
    HashMap h = new HashMap();
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

  public boolean publishedAssessmentTitleIsUnique(Long assessmentBaseId, String title) {
    String currentSiteId = AgentFacade.getCurrentSiteId();
    String agentString = AgentFacade.getAgentString();
    List list;
    boolean isUnique = true;
    String query="";
    query = "select new PublishedAssessmentData(a.publishedAssessmentId, a.title, a.lastModifiedDate)"+
            " from PublishedAssessmentData a, AuthorizationData z where "+
            " a.title=? and a.publishedAssessmentId!=? and a.status!=2 and "+
            " z.functionId='OWN_PUBLISHED_ASSESSMENT' and " +
            " a.publishedAssessmentId=z.qualifierId and z.agentIdString=?";
    System.out.println("query" + query);
    list = getHibernateTemplate().find(query,
           new Object[]{title,assessmentBaseId,currentSiteId},
           new net.sf.hibernate.type.Type[] {Hibernate.STRING, Hibernate.LONG, Hibernate.STRING});
    if (list.size()>0)
      isUnique = false;
    System.out.println("*** list size="+list.size());
    return isUnique;
  }

  public boolean hasRandomPart(Long publishedAssessmentId){
    boolean hasRandomPart = false;
    String key = SectionDataIfc.AUTHOR_TYPE;
    String value = SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString();
    String query =
        "select s from PublishedSectionData s, PublishedSectionMetaData m where "+
        " s = m.section and s.assessment.publishedAssessmentId=? and " +
        " m.label=? and m.entry=?";
    List l = getHibernateTemplate().find(query,
      new Object[]{ publishedAssessmentId, key, value},
      new net.sf.hibernate.type.Type[] {Hibernate.LONG, Hibernate.STRING, Hibernate.STRING});
    if (l.size()>0)
      hasRandomPart=true;
    return hasRandomPart;
  }

  public PublishedItemData getFirstPublishedItem(Long publishedAssessmentId){
    String query =
        "select i from PublishedAssessmentData p, PublishedSectionData s, "+
        " PublishedItemData i where p.publishedAssessmentId=? and"+
        " p.publishedAssessmentId=s.assessment.publishedAssessmentId and " +
        " s=i.section and s.sequence=? and i.sequence=?";
    List l = getHibernateTemplate().find(query,
      new Object[]{ publishedAssessmentId, new Integer("1"), new Integer("1")},
      new net.sf.hibernate.type.Type[] {Hibernate.LONG, Hibernate.INTEGER, Hibernate.INTEGER});
    if(l.size()>0)
    	return (PublishedItemData)l.get(0);
    else
    	return null;
  }

  public List getPublishedItemIds(Long publishedAssessmentId){
    return getHibernateTemplate().find(
         "select i.itemId from PublishedItemData i, PublishedSectionData s, "+
         " PublishedAssessmentData p where p.publishedAssessmentId=? and "+
         " p = s.assessment and i.section = s",
         new Object[] { publishedAssessmentId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });
  }

  public Integer getItemType(Long publishedItemId){
    String query = "select p.typeId "+
                   " from PublishedItemData p "+
	" where p.publishedItemId=?";

    List list = getHibernateTemplate().find(query,
					    new Object[] { publishedItemId },
					    new net.sf.hibernate.type.Type[] { Hibernate.LONG });
    if (list.size()>0)
	return (Integer)list.get(0);
    else
	return null;
    }


}
