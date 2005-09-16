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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.gradebook.GradebookServiceHelper;
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
      List list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, submittedDate DESC", objects, types);
      if (which.equals("true"))
        return list;
      else {
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

  public HashMap getItemScores(Long publishedId, Long itemId, String which)
  {
    try {
      ArrayList scores = (ArrayList)
        getTotalScores(publishedId.toString(), which);
      HashMap map = new HashMap();
      List list = new ArrayList();
      Iterator iter = scores.iterator();
      while (iter.hasNext())
      {
        AssessmentGradingData data = (AssessmentGradingData) iter.next();
        List temp = null;
        if (itemId.equals(new Long(0)))
          temp = getHibernateTemplate().find("from ItemGradingData a where a.assessmentGrading.assessmentGradingId=? order by agentId ASC, submittedDate DESC", data.getAssessmentGradingId(), Hibernate.LONG);
        else
        {
          Object[] objects = new Object[2];
          objects[0] = data.getAssessmentGradingId();
          objects[1] = itemId;
          Type[] types = new Type[2];
          types[0] = Hibernate.LONG;
          types[1] = Hibernate.LONG;
          temp = getHibernateTemplate().find("from ItemGradingData a where a.assessmentGrading.assessmentGradingId=? and a.publishedItem.itemId=? order by agentId ASC, submittedDate DESC", objects, types);

          // To avoid lazy loading, load them with the objects that have
          // the sections filled in already from total scores
          Iterator tmp = temp.iterator();
          while (tmp.hasNext())
          {
            ItemGradingData idata = (ItemGradingData) tmp.next();
            idata.setAssessmentGrading(data);
          }
        }
        list.addAll(temp);
      }
      iter = list.iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItem().getItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItem().getItemId(), thisone);
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
      if (gdata.getForGrade().booleanValue())
        return new HashMap();
      Iterator iter = gdata.getItemGradingSet().iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItem().getItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItem().getItemId(), thisone);
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
      log.debug("****#6, gdata="+gdata);
      log.debug("****#7, item size="+gdata.getItemGradingSet().size());
      Iterator iter = gdata.getItemGradingSet().iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItem().getItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItem().getItemId(), thisone);
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
      Iterator iter = gdata.getItemGradingSet().iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItem().getItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItem().getItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  public void saveTotalScores(ArrayList data) {
    try {
      Iterator iter = data.iterator();
      while (iter.hasNext())
      {
        AssessmentGradingData gdata = (AssessmentGradingData) iter.next();
        getHibernateTemplate().saveOrUpdate(gdata);
        // no need to notify gradebook if this submission is not for grade
        if ((Boolean.TRUE).equals(gdata.getForGrade()))
          notifyGradebook(gdata);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveItemScores(ArrayList data) {
    try {
      Iterator iter = data.iterator();
      while (iter.hasNext())
      {
        ItemGradingData gdata = (ItemGradingData) iter.next();
        if (gdata.getItemGradingId() == null)
          gdata.setItemGradingId(new Long(0));
        if (gdata.getPublishedItemText() == null)
        {
          //log.debug("Didn't save -- error in item.");
        }
        else
        {
          // This bit gets around a Hibernate weirdness -- even though
          // we got the assessmentgradingid, and changed info in the
          // itemgradingid, the assessmentgrading still has the original
          // set of itemgradingid's (from before we modified it) in
          // memory.  So we need to either get it all from the database
          // again, which is a heavyweight solution, or just manually
          // swap out the obsolete data before going forward. -- RMG
          Iterator iter2 = gdata.getAssessmentGrading().getItemGradingSet()
            .iterator();
          while (iter2.hasNext())
          {
            ItemGradingData idata = (ItemGradingData) iter2.next();
            if (idata.getItemGradingId().equals(gdata.getItemGradingId()))
            {
              gdata.getAssessmentGrading().getItemGradingSet().remove(idata);
              gdata.getAssessmentGrading().getItemGradingSet().add(gdata);
              break;
            }
          }

          // Now we can move on.
          getHibernateTemplate().saveOrUpdate(gdata);
          storeGrades(gdata.getAssessmentGrading(), true);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingIfc data)
  {
    storeGrades(data, false);
  }

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  public void storeGrades(AssessmentGradingIfc data, boolean regrade) {
    try {
      String agent = data.getAgentId();
      if (!regrade)
      {
        //data.setAssessmentGradingId(new Long(0)); // Always create a new one
        setIsLate(data);
      }
      Set itemgrading = data.getItemGradingSet();
      Iterator iter = itemgrading.iterator();
      float totalAutoScore = 0;
      while (iter.hasNext())
      {
        ItemGradingIfc itemdata = (ItemGradingIfc) iter.next();
        ItemDataIfc item = (ItemDataIfc) itemdata.getPublishedItem();
        float autoScore = (float) 0;
        if (!regrade)
        {
          itemdata.setAssessmentGrading(data);
          itemdata.setSubmittedDate(new Date());
          itemdata.setAgentId(agent);
          //itemdata.setItemGradingId(new Long(0)); // Always create a new one
          itemdata.setOverrideScore(new Float(0));

          // Autograde
          if (item.getTypeId().intValue() == 1 || // MCSS
              item.getTypeId().intValue() == 3 || // MCSS
              item.getTypeId().intValue() == 4) // True/False
            autoScore = getAnswerScore(itemdata);
          if (item.getTypeId().intValue() == 2) // MCMS
	  {
	    ArrayList answerArray = itemdata.getPublishedItemText().getAnswerArray();
            int correctAnswers = 0;
            if (answerArray != null) {
               for (int i =0; i<answerArray.size(); i++)
	       {
                 PublishedAnswer a = (PublishedAnswer) answerArray.get(i);
                 if (a.getIsCorrect().booleanValue()) {
                  correctAnswers++;
                 }
               }
            }
            autoScore = getAnswerScore(itemdata)/ correctAnswers;
          }
          if (item.getTypeId().intValue() == 9) // Matching
              autoScore = (getAnswerScore(itemdata) / (float) item.getItemTextSet().size());
            // Skip 5/6/7, since they can't be autoscored
          else if (item.getTypeId().intValue() == 8) // FIB
            autoScore = getFIBScore(itemdata) /
              (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();

         }
         else
         {
           autoScore = itemdata.getAutoScore().floatValue();
         }
        if (itemdata.getOverrideScore() == null)
          totalAutoScore += autoScore;
        else
          totalAutoScore +=
            autoScore + itemdata.getOverrideScore().floatValue();
        itemdata.setAutoScore(new Float(autoScore));
      }
      data.setTotalAutoScore(new Float(totalAutoScore));
      data.setFinalScore(new Float(totalAutoScore +
        data.getTotalOverrideScore().floatValue()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    getHibernateTemplate().saveOrUpdate(data);
    // no need to notify gradebook if this submission is not for grade
    if ((Boolean.TRUE).equals(data.getForGrade()))
      notifyGradebook(data);
  }


  /**
   * Notifies the gradebook that scores have been changed
   *
   * @param data The AssesmentGradingIfc representing the new score
   */
  private void notifyGradebook(AssessmentGradingIfc data) {
    // If the assessment is published to the gradebook, make sure to update the scores in the gradebook
    String toGradebook = data.getPublishedAssessment().getEvaluationModel().getToGradeBook();
    if (GradebookServiceHelper.gradebookExists(GradebookFacade.getGradebookUId()) && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
        if(log.isDebugEnabled()) log.debug("Attempting to update a score in the gradebook");
        try {
            GradebookServiceHelper.updateExternalAssessmentScore(data);
        } catch (Exception e) {
            // TODO Handle this exception in the UI rather than swallowing it!
            e.printStackTrace();
        }
    } else {
       if(log.isDebugEnabled()) log.debug("Not updating the gradebook.  toGradebook = " + toGradebook);
    }
  }

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingIfc for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public float getAnswerScore(ItemGradingIfc data)
  {
    AnswerIfc answer = (AnswerIfc) data.getPublishedAnswer();
    if (answer == null || answer.getScore() == null)
      return (float) 0;
    if (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())
      return (float) 0;
    return answer.getScore().floatValue();
  }

  public float getFIBScore(ItemGradingIfc data)
  {
    String answertext = data.getPublishedAnswer().getText();
    float totalScore = (float) 0;
    if (answertext != null)
    {
     StringTokenizer st = new StringTokenizer(answertext, "|");
     while (st.hasMoreTokens())
     {
      String answer = st.nextToken().trim();
      if (data.getAnswerText() != null &&
          data.getAnswerText().trim().equalsIgnoreCase(answer))
        totalScore += data.getPublishedAnswer().getScore().floatValue();
     }
    }
    return totalScore;
  }

  public static void main(String[] args) throws DataFacadeException {
    AssessmentGradingFacadeQueriesAPI instance = new AssessmentGradingFacadeQueries ();
    if (args[0].equals("submit")) {
      PublishedAssessmentFacadeQueriesAPI pafQ = new PublishedAssessmentFacadeQueries ();
      PublishedAssessmentData p = pafQ.loadPublishedAssessment(new Long(args[1]));
      print(p);
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

  public Long add(AssessmentGradingData a) {
    getHibernateTemplate().save(a);
    return a.getAssessmentGradingId();
  }

  public static void print(AssessmentBaseIfc a) {
    //log.debug("**assessmentId #" + a.getAssessmentBaseId());
    //log.debug("**assessment is template? " + a.getIsTemplate());
    if (a.getIsTemplate().equals(Boolean.FALSE)){
      //log.debug("**assessmentTemplateId #" + ((AssessmentData)a).getAssessmentTemplateId());
      //log.debug("**section: " +
      //               ((AssessmentData)a).getSectionSet());
    }
    /**
    log.debug("**assessment due date: " +
                     a.getAssessmentAccessControl().getDueDate());
    log.debug("**assessment control #" +
                       a.getAssessmentAccessControl());
    log.debug("**assessment metadata" +
                       a.getAssessmentMetaDataSet());
    log.debug("**Objective not lazy = " +
                   a.getAssessmentMetaDataByLabel("ASSESSMENT_OBJECTIVE"));
    */
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
        "from ItemGradingData i where i.publishedItem.itemId=? and i.agentId=?",
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
        "from ItemGradingData i where i.assessmentGrading.assessmentGradingId = ? and i.publishedItem.itemId=?",
        new Object[] { assessmentGradingId, publishedItemId },
        new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.LONG });
    if (itemGradings.size() == 0)
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public AssessmentGradingData load(Long id) {
    return (AssessmentGradingData) getHibernateTemplate().load(AssessmentGradingData.class, id);
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
      List assessmentGradings = getHibernateTemplate().find(
        "from AssessmentGradingData a where a.publishedAssessment.assessmentBaseId=? and a.agentId=?",
         new Object[] { publishedAssessmentId, agentIdString },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
      if (assessmentGradings.size() == 0)
	  return null;
      return (AssessmentGradingData) assessmentGradings.get(0);
  }

  public void saveItemGrading(ItemGradingIfc item) {
    try {
        getHibernateTemplate().saveOrUpdate((ItemGradingData)item);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    data.setTotalOverrideScore(new Float(0));
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment) {
    setIsLate(assessment);
    try {
        getHibernateTemplate().saveOrUpdate((AssessmentGradingData)assessment);
    } catch (Exception e) {
      e.printStackTrace();
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


}
