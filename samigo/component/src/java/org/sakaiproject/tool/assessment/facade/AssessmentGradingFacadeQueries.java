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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Disjunction;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class AssessmentGradingFacadeQueries extends HibernateDaoSupport implements AssessmentGradingFacadeQueriesAPI{
  private static Log log = LogFactory.getLog(AssessmentGradingFacadeQueries.class);

  public AssessmentGradingFacadeQueries () {
  }

  public List getTotalScores(String publishedId, String which) {
    try {
      // sectionSet of publishedAssessment is defined as lazy loading in
      // Hibernate OR map, so we need to initialize them. Unfortunately our
      // spring-1.0.2.jar does not support HibernateTemplate.intialize(Object)
      // so we need to do it ourselves
      PublishedAssessmentData assessment =PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
        loadPublishedAssessment(new Long(publishedId));
      HashSet sectionSet = PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(assessment);
      assessment.setSectionSet(sectionSet);
      // proceed to get totalScores
      Object[] objects = new Object[2];
      objects[0] = new Long(publishedId);
      objects[1] = new Boolean(true);
      Type[] types = new Type[2];
      types[0] = Hibernate.LONG;
      types[1] = Hibernate.BOOLEAN;

      List list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, finalScore DESC, submittedDate DESC", objects, types);

      // last submission
      if (which.equals(EvaluationModelIfc.LAST_SCORE.toString())) {
      list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, submittedDate DESC", objects, types);
      }

      if (which.equals(EvaluationModelIfc.ALL_SCORE.toString())) {
        return list;
      }
      else {
        // only take highest or latest
        Iterator items = list.iterator();
        ArrayList newlist = new ArrayList();
        String agentid = null;
        AssessmentGradingData data = (AssessmentGradingData) items.next();
        // daisyf add the following line on 12/15/04
        data.setPublishedAssessment(assessment);
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
        return newlist;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList();
    }
  }

  public List getAllSubmissions(String publishedId)
  {
      Object[] objects = new Object[1];
      objects[0] = new Long(publishedId);
      Type[] types = new Type[1];
      types[0] = Hibernate.LONG;
      List list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.forGrade=1", objects, types);
      return list;
  }



  public HashMap getItemScores(Long publishedId, final Long itemId, String which)
  {
    try {
      ArrayList scores = (ArrayList)
        getTotalScores(publishedId.toString(), which);
      HashMap map = new HashMap();
      List list = new ArrayList();

      // make final for callback to access
      final Iterator iter = scores.iterator();

      HibernateCallback hcb = new HibernateCallback()
      {
        public Object doInHibernate(Session session) throws HibernateException,
          SQLException
        {
          Criteria criteria = session.createCriteria(ItemGradingData.class);
          Disjunction disjunction = Expression.disjunction();

          /** make list from AssessmentGradingData ids */
          List gradingIdList = new ArrayList();
          while (iter.hasNext()){
            AssessmentGradingData data = (AssessmentGradingData) iter.next();
            gradingIdList.add(data.getAssessmentGradingId());
          }

          /** create or disjunctive expression for (in clauses) */
          List tempList = new ArrayList();
        for (int i = 0; i < gradingIdList.size(); i += 50){
          if (i + 50 > gradingIdList.size()){
              tempList = gradingIdList.subList(i, gradingIdList.size());
              disjunction.add(Expression.in("assessmentGradingId", tempList));
          }
          else{
            tempList = gradingIdList.subList(i, i + 50);
            disjunction.add(Expression.in("assessmentGradingId", tempList));
          }
        }

        if (itemId.equals(new Long(0))) {
          criteria.add(disjunction);
        }
        else {

          /** create logical and between the pubCriterion and the disjunction criterion */
          //Criterion pubCriterion = Expression.eq("publishedItem.itemId", itemId);
          Criterion pubCriterion = Expression.eq("publishedItemId", itemId);
          criteria.add(Expression.and(pubCriterion, disjunction));
        }
          criteria.addOrder(Order.asc("agentId"));
          criteria.addOrder(Order.desc("submittedDate"));
          //return criteria.list();
          //large list cause out of memory error (java heap space)
          return criteria.setMaxResults(10000).list();
        }
      };
      List temp = (List) getHibernateTemplate().execute(hcb);

      Iterator iter2 = temp.iterator();
      while (iter2.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter2.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  /**
   * This returns a hashmap of all the latest item entries, keyed by
   * item id for easy retrieval.
   * return (Long publishedItemId, ArrayList itemGradingData)
   */
  public HashMap getLastItemGradingData(Long publishedId, String agentId)
  {
    try {
      Object[] objects = new Object[2];
      objects[0] = publishedId;
      objects[1] = agentId;
      Type[] types = new Type[2];
      types[0] = Hibernate.LONG;
      types[1] = Hibernate.STRING;
      ArrayList scores = (ArrayList) getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? order by submittedDate DESC", objects, types);
      HashMap map = new HashMap();
      if (scores.isEmpty())
        return new HashMap();
      AssessmentGradingData gdata = (AssessmentGradingData) scores.toArray()[0];
      // initialize itemGradingSet
      gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
      if (gdata.getForGrade().booleanValue())
        return new HashMap();
      Iterator iter = gdata.getItemGradingSet().iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList) map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }



  /**
   * This returns a hashmap of all the submitted items, keyed by
   * item id for easy retrieval.
   */
  public HashMap getStudentGradingData(String assessmentGradingId)
  {
    try {
      HashMap map = new HashMap();
      AssessmentGradingData gdata = load(new Long(assessmentGradingId));
      gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
      log.debug("****#6, gdata="+gdata);
      log.debug("****#7, item size="+gdata.getItemGradingSet().size());
      Iterator iter = gdata.getItemGradingSet().iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  public HashMap getSubmitData(Long publishedId, String agentId)
  {
    try {
      Object[] objects = new Object[3];
      objects[0] = publishedId;
      objects[1] = agentId;
      objects[2] = new Boolean(true);
      Type[] types = new Type[3];
      types[0] = Hibernate.LONG;
      types[1] = Hibernate.STRING;
      types[2] = Hibernate.BOOLEAN;
      ArrayList scores = (ArrayList) getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by submittedDate DESC", objects, types);
      HashMap map = new HashMap();
      if (scores.isEmpty())
        return new HashMap();
      AssessmentGradingData gdata = (AssessmentGradingData) scores.toArray()[0];
      gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
      Iterator iter = gdata.getItemGradingSet().iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

/*
  public void saveTotalScores(ArrayList gdataList) {
    try {
      AssessmentGradingData gdata = null;
      if (gdataList.size()>0)
        gdata = (AssessmentGradingData) gdataList.get(0);
      else return;

      Integer scoringType = getScoringType(gdata);
      ArrayList oldList = getAssessmentGradingsByScoringType(
          scoringType, gdata.getPublishedAssessment().getPublishedAssessmentId());
      getHibernateTemplate().saveOrUpdateAll(gdataList);

      // no need to notify gradebook if this submission is not for grade
      if (updateGradebook(gdata)){
        // we only want to notify GB when there are changes
        ArrayList newList = getAssessmentGradingsByScoringType(
          scoringType, gdata.getPublishedAssessment().getPublishedAssessmentId());
        ArrayList l = getListForGradebookNotification(newList, oldList);
        notifyGradebook(l);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private ArrayList getListForGradebookNotification(
       ArrayList newList, ArrayList oldList){
    ArrayList l = new ArrayList();
    HashMap h = new HashMap();
    for (int i=0; i<oldList.size(); i++){
      AssessmentGradingData ag = (AssessmentGradingData)oldList.get(i);
      h.put(ag.getAssessmentGradingId(), ag);
    }

    for (int i=0; i<newList.size(); i++){
      AssessmentGradingData a = (AssessmentGradingData) newList.get(i);
      Object o = h.get(a.getAssessmentGradingId());
      if (o == null){ // this does not exist in old list, so include it for update
        l.add(a);
      }
      else{ // if new is different from old, include it for update
        AssessmentGradingData b = (AssessmentGradingData) o;
        if (!a.getFinalScore().equals(b.getFinalScore()))
          l.add(a);
      }
    }
    return l;
  }

  private ArrayList getAssessmentGradingsByScoringType(
       Integer scoringType, Long publishedAssessmentId){
    ArrayList l = new ArrayList();
    // get the list of highest score
    if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
      l = getHighestAssessmentGradingList(publishedAssessmentId);
    }
    // get the list of last score
    else {
      l = getLastAssessmentGradingList(publishedAssessmentId);
    }
    return l;
  }

  private Integer getScoringType(AssessmentGradingIfc data){
    Integer scoringType = null;
    EvaluationModelIfc e = data.getPublishedAssessment().getEvaluationModel();
    if ( e!=null ){
      scoringType = e.getScoringType();
    }
    return scoringType;
  }

  private boolean updateGradebook(AssessmentGradingIfc data){
    // no need to notify gradebook if this submission is not for grade
    boolean forGrade = (Boolean.TRUE).equals(data.getForGrade());

    boolean toGradebook = false;
    EvaluationModelIfc e = data.getPublishedAssessment().getEvaluationModel();
    if ( e!=null ){
      String toGradebookString = e.getToGradeBook();
      toGradebook = toGradebookString.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
    }
    return (forGrade && toGradebook);
  }

  private void notifyGradebook(ArrayList l){
    for (int i=0; i<l.size(); i++){
      notifyGradebook((AssessmentGradingData)l.get(i));
    }
  }

*/
  /**
   * Notifies the gradebook that scores have been changed
   *
   * @param data The AssesmentGradingIfc representing the new score
   */

/**
  private void notifyGradebook(AssessmentGradingIfc data) {
    // If the assessment is published to the gradebook, make sure to update the scores in the gradebook
    String toGradebook = data.getPublishedAssessment().getEvaluationModel().getToGradeBook();

    GradebookService g = null;
    boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
    if (integrated)
    {
      g = (GradebookService) SpringBeanLocator.getInstance().
        getBean("org.sakaiproject.service.gradebook.GradebookService");
    }

    GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();

    if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)
        && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
        if(log.isDebugEnabled()) log.debug("Attempting to update a score in the gradebook");
        try {
            gbsHelper.updateExternalAssessmentScore(data, g);
        } catch (Exception e) {
            // TODO Handle this exception in the UI rather than swallowing it!
            e.printStackTrace();
        }
    } else {
       if(log.isDebugEnabled()) log.debug("Not updating the gradebook.  toGradebook = " + toGradebook);
    }
  }
*/

    /*
  public static void main(String[] args) throws DataFacadeException {
    AssessmentGradingFacadeQueriesAPI instance = new AssessmentGradingFacadeQueries ();
    if (args[0].equals("submit")) {
      PublishedAssessmentFacadeQueriesAPI pafQ = new PublishedAssessmentFacadeQueries ();
      PublishedAssessmentData p = pafQ.loadPublishedAssessment(new Long(args[1]));
      Boolean forGrade = new Boolean("true");
      AssessmentGradingData r = instance.prepareRealizedAssessment(p, forGrade);
      Long rAssessmentId = instance.add(r);
    }
    System.exit(0);
  }

   public AssessmentGradingData prepareRealizedAssessment(PublishedAssessmentData p, Boolean forGrade){
    Float totalAutoScore = new Float("0");
    AssessmentGradingData a = new AssessmentGradingData();
    Set itemSet = getAllItems(p.getSectionSet());
    Set itemGradingSet = createItemGradingSet(a, itemSet, totalAutoScore);
    a.setAgentId("1");
    a.setForGrade(forGrade);
    if (p.getAssessmentAccessControl().getDueDate().before(new Date()))
      a.setIsLate(new Boolean ("true"));
    else
      a.setIsLate(new Boolean ("false"));
    a.setItemGradingSet(itemGradingSet);
    a.setPublishedAssessment(p);
    a.setTotalAutoScore(totalAutoScore);
    return a;
  }

  public Set getAllItems(Set sectionSet){
    HashSet h = new HashSet();
    Iterator i = sectionSet.iterator();
    while (i.hasNext()) {
      PublishedSectionData section = (PublishedSectionData) i.next();
      Set itemSet = section.getItemSet();
      h.addAll(itemSet);
    }
    return h;
  }

  public Set createItemGradingSet(AssessmentGradingData a, Set itemSet, Float totalAutoScore){
    HashSet h = new HashSet();
    Iterator i = itemSet.iterator();
    while (i.hasNext()) {
      PublishedItemData item = (PublishedItemData) i.next();
      ItemGradingData d = createItemGrading(a, item,
        item.getItemTextSet(), totalAutoScore);
      h.add(d);
    }
    return h;
  }

  public ItemGradingData createItemGrading(AssessmentGradingData a, PublishedItemData p, Set itemTextSet, Float totalAutoScore){
    ItemGradingData d = new ItemGradingData();
    String answerText = null;
    Iterator i = itemTextSet.iterator();
    while (i.hasNext()) {
      PublishedItemText itemText = (PublishedItemText) i.next();
      Set answerSet = itemText.getAnswerSet();
      if (answerSet == null || answerSet.iterator() == null){
        d.setAnswerText("hello Daisy!!");
      }
      else{
        PublishedAnswer ans = (PublishedAnswer)answerSet.iterator().next();
        d.setPublishedAnswer(ans);
        d.setRationale("this is the rationale");
        if (ans.getIsCorrect() != null &&
            ans.getIsCorrect().equals(new Boolean("true")))
          d.setAutoScore(ans.getScore());
          totalAutoScore = new Float(totalAutoScore.floatValue() + ans.getScore().floatValue());
      }
    }
    d.setPublishedItem(p);
    d.setAgentId("1");
    d.setSubmittedDate(new Date());
    d.setAssessmentGrading(a);
    return d;
  }

*/

  public Long add(AssessmentGradingData a) {
    getHibernateTemplate().save(a);
    return a.getAssessmentGradingId();
  }

  public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId){
    List size = getHibernateTemplate().find(
        "select count(i) from AssessmentGradingData a where a.forGrade=1 and a.publishedAssessment.publishedAssessmentId=?"+ publishedAssessmentId);
    Iterator iter = size.iterator();
    if (iter.hasNext()){
      int i = ((Integer)iter.next()).intValue();
      return i;
    }
    else{
      return 0;
    }
  }

  public HashMap getSubmissionSizeOfAllPublishedAssessments(){
    HashMap h = new HashMap();
    List list = getHibernateTemplate().find(
        "select new PublishedAssessmentData(a.publishedAssessment.publishedAssessmentId, count(a)) from AssessmentGradingData a where a.forGrade=1 group by a.publishedAssessment.publishedAssessmentId");
    Iterator iter = list.iterator();
    while (iter.hasNext()){
      PublishedAssessmentData o = (PublishedAssessmentData)iter.next();
      h.put(o.getPublishedAssessmentId(), new Integer(o.getSubmissionSize()));
    }
    return h;
  }

  public Long saveMedia(byte[] media, String mimeType){
    MediaData mediaData = new MediaData(media, mimeType);
    getHibernateTemplate().save(mediaData);
    return mediaData.getMediaId();
  }

  public Long saveMedia(MediaData mediaData){
    getHibernateTemplate().save(mediaData);
    return mediaData.getMediaId();
  }

  public void removeMediaById(Long mediaId){
    MediaData media = (MediaData) getHibernateTemplate().load(MediaData.class, mediaId);
    getHibernateTemplate().delete(media);
  }

  public MediaData getMedia(Long mediaId){
    MediaData mediaData = (MediaData) getHibernateTemplate().load(MediaData.class,mediaId);
    mediaData.setMedia(getMediaStream(mediaId));
    return mediaData;
  }

  public ArrayList getMediaArray(Long itemGradingId){
    log.debug("*** itemGradingId ="+itemGradingId);
    ArrayList a = new ArrayList();
    List list = getHibernateTemplate().find(
        "from MediaData m where m.itemGradingData.itemGradingId=?",
        new Object[] { itemGradingId },
        new net.sf.hibernate.type.Type[] { Hibernate.LONG });
    for (int i=0;i<list.size();i++){
      a.add((MediaData)list.get(i));
    }
    log.debug("*** no. of media ="+a.size());
    return a;
  }

  public ArrayList getMediaArray(ItemGradingData item){
    ArrayList a = new ArrayList();
    List list = getHibernateTemplate().find(
        "from MediaData m where m.itemGradingData=?", item );
    for (int i=0;i<list.size();i++){
      a.add((MediaData)list.get(i));
    }
    log.debug("*** no. of media ="+a.size());
    return a;
  }

  public ItemGradingData getLastItemGradingDataByAgent(
      Long publishedItemId, String agentId)
  {
    List itemGradings = getHibernateTemplate().find(
        "from ItemGradingData i where i.publishedItemId=? and i.agentId=?",
        new Object[] { publishedItemId, agentId },
        new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (itemGradings.size() == 0)
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public ItemGradingData getItemGradingData(
      Long assessmentGradingId, Long publishedItemId)
  {
    log.debug("****assessmentGradingId="+assessmentGradingId);
    log.debug("****publishedItemId="+publishedItemId);
    List itemGradings = getHibernateTemplate().find(
        "from ItemGradingData i where i.assessmentGradingId = ? and i.publishedItemId=?",
        new Object[] { assessmentGradingId, publishedItemId },
        new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.LONG });
    if (itemGradings.size() == 0)
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public AssessmentGradingData load(Long id) {
    return (AssessmentGradingData) getHibernateTemplate().load(AssessmentGradingData.class, id);
  }

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
    AssessmentGradingData ag = null;
    List assessmentGradings = getHibernateTemplate().find(
        "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc",
         new Object[] { publishedAssessmentId, agentIdString, Boolean.FALSE },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.BOOLEAN });
    if (assessmentGradings.size() != 0){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
    AssessmentGradingData ag = null;
    List assessmentGradings = getHibernateTemplate().find(
        "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? order by a.submittedDate desc",
         new Object[] { publishedAssessmentId, agentIdString },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (assessmentGradings.size() != 0){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }


  public void saveItemGrading(ItemGradingIfc item) {
    try {
        getHibernateTemplate().saveOrUpdate((ItemGradingData)item);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment) {
    if (assessment.getAssessmentGradingId()!=null && assessment.getAssessmentGradingId().intValue() > 0)
      updateAssessmentGrading(assessment);
    else
      saveAssessmentGrading(assessment);
  }


  public void saveAssessmentGrading(AssessmentGradingIfc assessment) {
    try {
        getHibernateTemplate().save((AssessmentGradingData)assessment);
    } catch (Exception e) {
      log.warn("problem inserting assessmentGrading: "+e.getMessage());
    }
  }

  public void updateAssessmentGrading(AssessmentGradingIfc assessment) {
    try {
      getHibernateTemplate().update((AssessmentGradingData)assessment);
    } catch (Exception e) {
      log.warn("problem updating assessmentGrading: "+e.getMessage());
    }
  }

  private byte[] getMediaStream(Long mediaId){
    byte[] b = new byte[4000];
    Session session = null;
    try{
      session = getSessionFactory().openSession();
      Connection conn = session.connection();
      log.debug("****Connection="+conn);
      String query="select MEDIA from SAM_MEDIA_T where MEDIAID=?";
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setLong(1, mediaId.longValue());
      ResultSet rs = statement.executeQuery();
      if (rs.next()){
        java.lang.Object o = rs.getObject("MEDIA");
        if (o!=null){
          InputStream in = rs.getBinaryStream("MEDIA");
          in.mark(0);
          int ch;
          int len=0;
          while ((ch=in.read())!=-1)
        len++;

          b = new byte[len];
          in.reset();
          in.read(b,0,len);
          in.close();
  }
      }
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
    finally{
      try{
        if (session !=null) session.close();
      }
      catch(Exception ex){
        log.warn(ex.getMessage());
      }
    }
    return b;
  }

  public List getAssessmentGradingIds(Long publishedItemId){
    return getHibernateTemplate().find(
         "select g.assessmentGradingId from "+
         " ItemGradingData g where g.publishedItemId=?",
         new Object[] { publishedItemId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });
  }

  public AssessmentGradingIfc getHighestAssessmentGrading(
         Long publishedAssessmentId, String agentId)
  {
    AssessmentGradingData ag = null;
    String query ="from AssessmentGradingData a "+
                  " where a.publishedAssessment.publishedAssessmentId=? and "+
                  " a.agentId=? order by a.finalScore desc";
    List assessmentGradings = getHibernateTemplate().find(query,
        new Object[] { publishedAssessmentId, agentId },
        new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (assessmentGradings.size() != 0){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }

  public List getLastAssessmentGradingList(Long publishedAssessmentId){
    String query = "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? order by agentId asc, a.submittedDate desc";
    List assessmentGradings = getHibernateTemplate().find(query,
         new Object[] { publishedAssessmentId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });

    ArrayList l = new ArrayList();
    String currentAgent="";
    for (int i=0; i<assessmentGradings.size(); i++){
      AssessmentGradingData g = (AssessmentGradingData)assessmentGradings.get(i);
      if (!currentAgent.equals(g.getAgentId())){
        l.add(g);
        currentAgent = g.getAgentId();
      }
    }
    return l;
  }

  public List getHighestAssessmentGradingList(Long publishedAssessmentId){
    String query = "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? order by agentId asc, a.finalScore desc";
    List assessmentGradings = getHibernateTemplate().find(query,
         new Object[] { publishedAssessmentId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });

    ArrayList l = new ArrayList();
    String currentAgent="";
    for (int i=0; i<assessmentGradings.size(); i++){
      AssessmentGradingData g = (AssessmentGradingData)assessmentGradings.get(i);
      if (!currentAgent.equals(g.getAgentId())){
        l.add(g);
        currentAgent = g.getAgentId();
      }
    }
    return l;
  }

  // build a Hashmap (Long publishedItemId, ArrayList assessmentGradingIds)
  // containing the item submission of the last AssessmentGrading
  // (regardless of users who submitted it) of a given published assessment
  public HashMap getLastAssessmentGradingByPublishedItem(Long publishedAssessmentId){
    HashMap h = new HashMap();
    String query = "select new AssessmentGradingData("+
                   " a.assessmentGradingId, p.itemId, "+
                   " a.agentId, a.finalScore, a.submittedDate) "+
                   " from ItemGradingData i, AssessmentGradingData a,"+
                   " PublishedItemData p where "+
                   " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and "+
                   " a.publishedAssessment.publishedAssessmentId=? " +
                   " order by a.agentId asc, a.submittedDate desc";
    List assessmentGradings = getHibernateTemplate().find(query,
         new Object[] { publishedAssessmentId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });

    ArrayList l = new ArrayList();
    String currentAgent="";
    Date submittedDate = null;
    for (int i=0; i<assessmentGradings.size(); i++){
      AssessmentGradingData g = (AssessmentGradingData)assessmentGradings.get(i);
      Long itemId = g.getPublishedItemId();
      Long gradingId = g.getAssessmentGradingId();
      log.debug("**** itemId="+itemId+", gradingId="+gradingId+", agentId="+g.getAgentId()+", score="+g.getFinalScore());
      if ( i==0 ){
        currentAgent = g.getAgentId();
        submittedDate = g.getSubmittedDate();
      }
      if (currentAgent.equals(g.getAgentId()) && submittedDate.equals(g.getSubmittedDate())){
        Object o = h.get(itemId);
        if (o != null)
          ((ArrayList) o).add(gradingId);
        else{
          ArrayList gradingIds = new ArrayList();
          gradingIds.add(gradingId);
          h.put(itemId, gradingIds);
  }
      }
      if (!currentAgent.equals(g.getAgentId())){
        currentAgent = g.getAgentId();
        submittedDate = g.getSubmittedDate();
      }
    }
    return h;
  }

  // build a Hashmap (Long publishedItemId, ArrayList assessmentGradingIds)
  // containing the item submission of the highest AssessmentGrading
  // (regardless of users who submitted it) of a given published assessment
  public HashMap getHighestAssessmentGradingByPublishedItem(Long publishedAssessmentId){
    HashMap h = new HashMap();
    String query = "select new AssessmentGradingData("+
                   " a.assessmentGradingId, p.itemId, "+
                   " a.agentId, a.finalScore, a.submittedDate) "+
                   " from ItemGradingData i, AssessmentGradingData a, "+
                   " PublishedItemData p where "+
                   " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and "+
                   " a.publishedAssessment.publishedAssessmentId=? " +
                   " order by a.agentId asc, a.finalScore desc";
    List assessmentGradings = getHibernateTemplate().find(query,
         new Object[] { publishedAssessmentId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });

    ArrayList l = new ArrayList();
    String currentAgent="";
    Float finalScore = null;
    for (int i=0; i<assessmentGradings.size(); i++){
      AssessmentGradingData g = (AssessmentGradingData)assessmentGradings.get(i);
      Long itemId = g.getPublishedItemId();
      Long gradingId = g.getAssessmentGradingId();
      log.debug("**** itemId="+itemId+", gradingId="+gradingId+", agentId="+g.getAgentId()+", score="+g.getFinalScore());
      if ( i==0 ){
        currentAgent = g.getAgentId();
        finalScore = g.getFinalScore();
      }
      if (currentAgent.equals(g.getAgentId()) && finalScore.equals(g.getFinalScore())){
        Object o = h.get(itemId);
        if (o != null)
          ((ArrayList) o).add(gradingId);
        else{
          ArrayList gradingIds = new ArrayList();
          gradingIds.add(gradingId);
          h.put(itemId, gradingIds);
  }
      }
      if (!currentAgent.equals(g.getAgentId())){
        currentAgent = g.getAgentId();
        finalScore = g.getFinalScore();
      }
    }
    return h;
  }

  public Set getItemGradingSet(Long assessmentGradingId){
    String query = "from ItemGradingData i where i.assessmentGradingId=?";
    List itemGradings = getHibernateTemplate().find(query,
                                                    new Object[] { assessmentGradingId },
                                                    new net.sf.hibernate.type.Type[] { Hibernate.LONG });
    HashSet s = new HashSet();
    for (int i=0; i<itemGradings.size();i++){
      s.add(itemGradings.get(i));
    }
    return s;
  }

  public HashMap getAssessmentGradingByItemGradingId(Long publishedAssessmentId){
    List aList = getAllSubmissions(publishedAssessmentId.toString());
    HashMap aHash = new HashMap();
    for (int j=0; j<aList.size();j++){
      AssessmentGradingData a = (AssessmentGradingData)aList.get(j);
      aHash.put(a.getAssessmentGradingId(), a);
    }

    String query = "select new ItemGradingData(i.itemGradingId, a.assessmentGradingId) "+
                   " from ItemGradingData i, AssessmentGradingData a "+
                   " where i.assessmentGradingId=a.assessmentGradingId "+
                   " and a.publishedAssessment.publishedAssessmentId=?";
    List l = getHibernateTemplate().find(query,
             new Object[] { publishedAssessmentId },
             new net.sf.hibernate.type.Type[] { Hibernate.LONG });
    System.out.println("****** assessmentGradinghash="+l.size());
    HashMap h = new HashMap();
    for (int i=0; i<l.size();i++){
      ItemGradingData o = (ItemGradingData)l.get(i);
      h.put(o.getItemGradingId(), (AssessmentGradingData)aHash.get(o.getAssessmentGradingId()));
    }
    return h;
  }

    public void setIsLate(AssessmentGradingIfc data){
	data.setSubmittedDate(new Date());
	if (data.getPublishedAssessment().getAssessmentAccessControl() != null
	    && data.getPublishedAssessment().getAssessmentAccessControl()
	    .getDueDate() != null &&
	    data.getPublishedAssessment().getAssessmentAccessControl()
	    .getDueDate().before(new Date()))
	    data.setIsLate(new Boolean(true));
	else
	    data.setIsLate(new Boolean(false));
	if (data.getForGrade().booleanValue())
	    data.setStatus(new Integer(1));
	else
	    data.setStatus(new Integer(0));

        // I don't know why we need to set OverrideScore to 0 here,
        // but at least we shouldn't set it to 0 if there is an existing value
        // so I added to check null before setting - daisyf for v2.1.2
        if (data.getTotalOverrideScore()==null)
          data.setTotalOverrideScore(new Float(0));
    }  

  public void deleteAll(Collection c){
    getHibernateTemplate().deleteAll(c);
  }


  public void saveOrUpdateAll(Collection c) {
    try {
        getHibernateTemplate().saveOrUpdateAll(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(Long assessmentGradingId){
    PublishedAssessmentIfc pub = null;
    String query = "select p from PublishedAssessmentData p, AssessmentGradingData a "+
                   " where a.publishedAssessment=p and a.assessmentGradingId=?";
    List pubList = getHibernateTemplate().find(query,
                                                    new Object[] { assessmentGradingId },
                                                    new net.sf.hibernate.type.Type[] { Hibernate.LONG });
    if (pubList!=null && pubList.size()>0)
      pub = (PublishedAssessmentIfc) pubList.get(0);

    return pub; 
  }

}
