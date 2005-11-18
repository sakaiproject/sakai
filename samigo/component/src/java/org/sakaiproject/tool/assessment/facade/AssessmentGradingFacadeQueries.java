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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
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

      List list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, finalScore DESC", objects, types);

/*
      // highest score	
      if (which.equals(EvaluationModelIfc.HIGHEST_SCORE.toString())) {
      list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, finalScore DESC", objects, types);
      }

*/
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
          List tempList;
  		  for (int i = 0; i < gradingIdList.size(); i += 50){
  		    if (i + 50 > gradingIdList.size()){
  	          tempList = gradingIdList.subList(i, gradingIdList.size());
  	          disjunction.add(Expression.in("assessmentGrading.assessmentGradingId", tempList));      
  		    }
  		    else{
  		      tempList = gradingIdList.subList(i, i + 50);
  		      disjunction.add(Expression.in("assessmentGrading.assessmentGradingId", tempList));
  		    }
  		  }                                                          
          
          Criterion pubCriterion = Expression.eq("publishedItem.itemId", itemId);          
          
          /** create logical and between the pubCriterion and the disjunction criterion */
          criteria.add(Expression.and(pubCriterion, disjunction));
                       
          criteria.addOrder(Order.asc("agentId"));
          criteria.addOrder(Order.desc("submittedDate"));                    
          return criteria.list();
        }
      };
      List temp = (List) getHibernateTemplate().execute(hcb);
        
      Iterator iter2 = temp.iterator();
      while (iter2.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter2.next();
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

  public void saveTotalScores(ArrayList gdataList) {
    try {
      AssessmentGradingData gdata = null;
      Iterator iter = gdataList.iterator();
      while (iter.hasNext())
      {
        gdata = (AssessmentGradingData) iter.next();
        getHibernateTemplate().saveOrUpdate(gdata);
      }
      // no need to notify gradebook if this submission is not for grade
      if (gdata !=null && (Boolean.TRUE).equals(gdata.getForGrade()))
        updateAllGradebookEntries(gdata);
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
      
      //change algorithm based on each question (SAK-1930 & IM271559) -cwen
      HashMap totalItems = new HashMap();
      while(iter.hasNext())
      {
        ItemGradingIfc itemdata = (ItemGradingIfc) iter.next();
        ItemDataIfc item = (ItemDataIfc) itemdata.getPublishedItem();
        Long itemId = item.getItemId();
        float autoScore = (float) 0;
        
        if (!regrade)
        {
          itemdata.setAssessmentGrading(data);
          itemdata.setSubmittedDate(new Date());
          itemdata.setAgentId(agent);
          itemdata.setOverrideScore(new Float(0));
          
          if (item.getTypeId().intValue() == 1 || // MCSS
              item.getTypeId().intValue() == 3 || // MCSS
              item.getTypeId().intValue() == 4) // True/False
          {
            autoScore = getAnswerScore(itemdata);
            
            //overridescore
            if (itemdata.getOverrideScore() != null)
            {
              autoScore += itemdata.getOverrideScore().floatValue();
            }
            
            totalItems.put(itemId, new Float(autoScore));
          }
          else if (item.getTypeId().intValue() == 2) // MCMS
          {
            ArrayList answerArray = itemdata.getPublishedItemText().getAnswerArray();
            int correctAnswers = 0;
            if (answerArray != null) 
            {
              for (int i =0; i<answerArray.size(); i++)
              {
                PublishedAnswer a = (PublishedAnswer) answerArray.get(i);
                if (a.getIsCorrect().booleanValue()) 
                {
                  correctAnswers++;
                }
              }
            }
            float initScore = getAnswerScore(itemdata);
            if(initScore > 0)
            {
              autoScore = initScore / correctAnswers;
            }
            else
            {
              autoScore = (getTotalCorrectScore(itemdata) / correctAnswers) * ((float) -1);
            }
            
            //overridescore?
            if (itemdata.getOverrideScore() != null)
            {
              autoScore += itemdata.getOverrideScore().floatValue();
            }
            
            if(!totalItems.containsKey(itemId))
            {
              totalItems.put(itemId, new Float(autoScore));
            }
            else
            {
              float accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
              accumelateScore += autoScore;
              totalItems.put(itemId, new Float(accumelateScore));
            }
          }
          else if (item.getTypeId().intValue() == 9) // Matching
          {
            float initScore = getAnswerScore(itemdata);
            if(initScore > 0)
            {
              autoScore = initScore / ((float) item.getItemTextSet().size());
            }
            else
            {
              autoScore = (getTotalCorrectScore(itemdata) / ((float) item.getItemTextSet().size())) * ((float) -1);
            }
            
            //overridescore?
            if (itemdata.getOverrideScore() != null)
            {
              autoScore += itemdata.getOverrideScore().floatValue();
            }
            
            if(!totalItems.containsKey(itemId))
            {
              totalItems.put(itemId, new Float(autoScore));
            }
            else
            {
              float accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
              accumelateScore += autoScore;
              totalItems.put(itemId, new Float(accumelateScore));
            }
            //Skip 5/6/7, since they can't be autoscored
          }
          else if (item.getTypeId().intValue() == 8) // FIB
          {
            autoScore = getFIBScore(itemdata) / (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();

            //overridescore - cwen
            if (itemdata.getOverrideScore() != null)
            {
              autoScore += itemdata.getOverrideScore().floatValue();
            }
            
            if(!totalItems.containsKey(itemId))
            {
              totalItems.put(itemId, new Float(autoScore));
            }
            else
            {
              float accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
              accumelateScore += autoScore;
              totalItems.put(itemId, new Float(accumelateScore));
            }
          }
          else
          {
             //overridescore - cwen
            if (itemdata.getOverrideScore() != null)
            {
              autoScore += itemdata.getOverrideScore().floatValue();
            }	
            if(!totalItems.containsKey(itemId))
            {
              totalItems.put(itemId, new Float(autoScore));
            }
            else
            {
              float accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
              accumelateScore += autoScore;
              totalItems.put(itemId, new Float(accumelateScore));
            }            
          }
        }
        else
        {
          autoScore = itemdata.getAutoScore().floatValue();
         
          //overridescore - cwen
          if (itemdata.getOverrideScore() != null)
          {
            autoScore += itemdata.getOverrideScore().floatValue();
          }

          if(!totalItems.containsKey(itemId))
          {
            totalItems.put(itemId, new Float(autoScore));
          }
          else
          {
            float accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
            accumelateScore += autoScore;
            totalItems.put(itemId, new Float(accumelateScore));
          }            
        }
        itemdata.setAutoScore(new Float(autoScore));
      }
      
      Set keySet = totalItems.keySet();
      Iterator keyIter = keySet.iterator();
      while(keyIter.hasNext())
      {
        float eachItemScore = ((Float) totalItems.get((Long)keyIter.next())).floatValue();
        if(eachItemScore > 0)
        {
          totalAutoScore += eachItemScore;
        }
      }
      
      iter = itemgrading.iterator();
      while(iter.hasNext())
      {
        ItemGradingIfc itemdata = (ItemGradingIfc) iter.next();
        ItemDataIfc item = (ItemDataIfc) itemdata.getPublishedItem();
        Long itemId = item.getItemId();
        float autoScore = (float) 0;

        float eachItemScore = ((Float) totalItems.get(itemId)).floatValue();
        if(eachItemScore < 0)
        {
          itemdata.setAutoScore(new Float(0));
        }
      }
      
      data.setTotalAutoScore(new Float(totalAutoScore));
      data.setFinalScore(new Float(totalAutoScore + data.getTotalOverrideScore().floatValue()));

    } catch (Exception e) {
      e.printStackTrace();
    }
    getHibernateTemplate().saveOrUpdate(data);
   
    notifyGradebookByScoringType(data);
  }

  private void notifyGradebookByScoringType(AssessmentGradingIfc data){
    Integer scoringType = getScoringType(data); 
    if (updateGradebook(data)){
	AssessmentGradingIfc d = data; // data is the last submission
      // need to decide what to tell gradebook
      if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE))
        d = getHighestAssessmentGrading(
            data.getPublishedAssessment().getPublishedAssessmentId(), 
            data.getAgentId());
      notifyGradebook(d);
    }
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

  private void updateAllGradebookEntries(AssessmentGradingIfc data){

    if (!updateGradebook(data))
      return;

    Integer scoringType = getScoringType(data); 
    ArrayList l = new ArrayList();
    // get the list of highest score
    if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
      l = getHighestAssessmentGradingList(
          data.getPublishedAssessment().getPublishedAssessmentId());
    }
    // get the list of last score
    else {
      l = getLastAssessmentGradingList(
          data.getPublishedAssessment().getPublishedAssessmentId());
    }    
    notifyGradebook(l);
  }

  private void notifyGradebook(ArrayList l){
    for (int i=0; i<l.size(); i++){
      notifyGradebook((AssessmentGradingData)l.get(i));
    }
  }


  /**
   * Notifies the gradebook that scores have been changed
   *
   * @param data The AssesmentGradingIfc representing the new score
   */
  private void notifyGradebook(AssessmentGradingIfc data) {
    // If the assessment is published to the gradebook, make sure to update the scores in the gradebook
    String toGradebook = data.getPublishedAssessment().getEvaluationModel().getToGradeBook();

    GradebookService g = (GradebookService) SpringBeanLocator.getInstance().
    getBean("org.sakaiproject.service.gradebook.GradebookService");
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

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
      List assessmentGradings = getHibernateTemplate().find(
        "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc",
         new Object[] { publishedAssessmentId, agentIdString, Boolean.FALSE },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.BOOLEAN });
      if (assessmentGradings.size() == 0)
	  return null;
      return (AssessmentGradingData) assessmentGradings.get(0);
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
      List assessmentGradings = getHibernateTemplate().find(
        "from AssessmentGradingData a where a.publishedAssessment.publishedAssessmentId=? and a.agentId=? order by a.submittedDate desc",
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

  public float getTotalCorrectScore(ItemGradingIfc data)
  {
    AnswerIfc answer = (AnswerIfc) data.getPublishedAnswer();
    if (answer == null || answer.getScore() == null)
      return (float) 0;
    return answer.getScore().floatValue();
  }

  public List getAssessmentGradingIds(Long publishedItemId){
    return getHibernateTemplate().find(
         "select g.assessmentGrading.assessmentGradingId from "+
         " ItemGradingData g where g.publishedItem.itemId=?",
         new Object[] { publishedItemId },
         new net.sf.hibernate.type.Type[] { Hibernate.LONG });
  }

  public AssessmentGradingIfc getHighestAssessmentGrading(
         Long publishedAssessmentId, String agentId)
  {
    String query ="from AssessmentGradingData a "+ 
                  " where a.publishedAssessment.publishedAssessmentId=? and "+ 
                  " a.agentId=? order by a.finalScore desc";
    List l = getHibernateTemplate().find(query,
        new Object[] { publishedAssessmentId, agentId },
        new net.sf.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (l.size() >0) 
      return ((AssessmentGradingData)l.get(0));
    else
      return null;
  }

  public ArrayList getLastAssessmentGradingList(Long publishedAssessmentId){
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

  public ArrayList getHighestAssessmentGradingList(Long publishedAssessmentId){
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
                   " i.assessmentGrading = a and i.publishedItem = p and "+ 
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
                   " i.assessmentGrading = a and i.publishedItem = p and "+ 
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


}
