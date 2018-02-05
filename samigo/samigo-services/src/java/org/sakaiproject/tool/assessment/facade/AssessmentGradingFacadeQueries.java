
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.RuleBasedCollator;
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
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.GradingAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.AutoSubmitAssessmentsJob;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.ServerOverloadException;

public class AssessmentGradingFacadeQueries extends HibernateDaoSupport implements AssessmentGradingFacadeQueriesAPI{
  private static final Logger log = LoggerFactory.getLogger(AssessmentGradingFacadeQueries.class);

  /**
   * Default empty Constructor
   */
  public AssessmentGradingFacadeQueries () {
  }

  /**
   * Injected Services
   */
  private ContentHostingService contentHostingService; 
  
  public void setContentHostingService(ContentHostingService contentHostingService) {
	  this.contentHostingService = contentHostingService;
  }

  private SecurityService securityService;

  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

  private UserDirectoryService userDirectoryService;


  public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
	  this.userDirectoryService = userDirectoryService;
  }

  
  private PersistenceHelper persistenceHelper;
  public void setPersistenceHelper(PersistenceHelper persistenceHelper) {
	this.persistenceHelper = persistenceHelper;
  }


  /**
   * 
   * @param publishedId
   * @param which
   * @return 
   */
  public List getTotalScores(final String publishedId, String which) {
	  return getTotalScores(publishedId, which, true);
  }

  public List getTotalScores(final String publishedId, String which, final boolean getSubmittedOnly) {
    try {
      // sectionSet of publishedAssessment is defined as lazy loading in
      // Hibernate OR map, so we need to initialize them. Unfortunately our
      // spring-1.0.2.jar does not support HibernateTemplate.intialize(Object)
      // so we need to do it ourselves
      //PublishedAssessmentData assessment =PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().loadPublishedAssessment(new Long(publishedId));
      //HashSet sectionSet = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(assessment);
      //assessment.setSectionSet(sectionSet);
      // proceed to get totalScores
//      Object[] objects = new Object[2];
//      objects[0] = new Long(publishedId);
//      objects[1] = new Boolean(true);
//      Type[] types = new Type[2];
//      types[0] = Hibernate.LONG;
//      types[1] = Hibernate.BOOLEAN;

      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		Query q;
      		if (getSubmittedOnly) {
      			q = session.createQuery(
      					"from AssessmentGradingData a where a.publishedAssessmentId=? " +
      					"and a.forGrade=? " +
      					"order by a.agentId ASC, a.finalScore DESC, a.submittedDate DESC");
      			q.setLong(0, Long.parseLong(publishedId));
          		q.setBoolean(1, true);
      		}
      		else {
      			q = session.createQuery(
          				"from AssessmentGradingData a where a.publishedAssessmentId=? " +
          				"and (a.forGrade=? or (a.forGrade=? and a.status=?)) " +
          				"order by a.agentId ASC, a.finalScore DESC, a.submittedDate DESC");
      		
      			q.setLong(0, Long.parseLong(publishedId));
      			q.setBoolean(1, true);
      			q.setBoolean(2, false);
      			q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
      		}
      		return q.list();
      	};
      };
      List list = getHibernateTemplate().executeFind(hcb);
      Map<Long, List<AssessmentGradingAttachment>> attachmentMap;
      //if (loadItemGradingAttachment) {
    	  attachmentMap = getAssessmentGradingAttachmentMap(Long.valueOf(publishedId));
      //}
      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
    	  AssessmentGradingData data = (AssessmentGradingData) iter.next();
    	  //if (loadItemGradingAttachment) {
    		  if (attachmentMap.get(data.getAssessmentGradingId()) != null) {
    			  data.setAssessmentGradingAttachmentList(attachmentMap.get(data.getAssessmentGradingId()));
    		  }
    		  else {
    			  data.setAssessmentGradingAttachmentList(new ArrayList<AssessmentGradingAttachment>());
    		  }
    	  //}
      }

//      List list = getHibernateTemplate().find(
//    		  "from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, finalScore DESC, submittedDate DESC", 
//    		  objects, types);

      // last submission
      if (which.equals(EvaluationModelIfc.LAST_SCORE.toString())) {
    	  final HibernateCallback hcb2 = new HibernateCallback(){
    		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
    	    	Query q;
	      		if (getSubmittedOnly) {
	    			q = session.createQuery(
	    					"from AssessmentGradingData a where a.publishedAssessmentId=? " +
	    					"and a.forGrade=? " +
	    					"order by a.agentId ASC, a.submittedDate DESC");
	    			q.setLong(0, Long.parseLong(publishedId));
	          		q.setBoolean(1, true);
	      		}
	      		else {
	      			q = session.createQuery(
    	    				"from AssessmentGradingData a where a.publishedAssessmentId=? " +
    	    				"and (a.forGrade=? or (a.forGrade=? and a.status=?)) " +
    	    				"order by a.agentId ASC, a.submittedDate DESC");
	      			q.setLong(0, Long.parseLong(publishedId));
	      			q.setBoolean(1, true);
	      			q.setBoolean(2, false);
	      			q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
	      		}
	    		return q.list();
    	    };
    	  };
    	    list = getHibernateTemplate().executeFind(hcb2);

//    	  list = getHibernateTemplate().find(
//    		  "from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, submittedDate DESC", 
//    		  objects, types);
      }

      if (which.equals(EvaluationModelIfc.ALL_SCORE.toString()) || which.equals(EvaluationModelIfc.AVERAGE_SCORE.toString())) {
        return list;
      }
      else {
        if(list.isEmpty()) {
             return new ArrayList();
        }
        // only take highest or latest
        Iterator items = list.iterator();
        ArrayList newlist = new ArrayList();
        String agentid;
        AssessmentGradingData data = (AssessmentGradingData) items.next();
        // daisyf add the following line on 12/15/04
        data.setPublishedAssessmentId(Long.valueOf(publishedId));
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
    } catch (RuntimeException e) {
      log.warn(e.getMessage(), e);
      return new ArrayList();
    }
  }

  @SuppressWarnings("unchecked")
  public List getAllSubmissions(final String publishedId)
  {
//      Object[] objects = new Object[1];
//      objects[0] = new Long(publishedId);
//      Type[] types = new Type[1];
//      types[0] = Hibernate.LONG;

      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		Query q = session.createQuery(
      				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=?");
      		q.setLong(0, Long.parseLong(publishedId));
      		q.setBoolean(1, true);
      		return q.list();
      	};
      };
      return getHibernateTemplate().executeFind(hcb);

//      List list = getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=1", objects, types);
//      return list;
  }
  
  public List getAllAssessmentGradingData(final Long publishedId)
  {
      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		Query q = session.createQuery(
      				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.status <> ? order by a.agentId asc, a.submittedDate desc");
      		q.setLong(0, publishedId);
    		q.setInteger(1, AssessmentGradingData.NO_SUBMISSION);
      		return q.list();
      	};
      };
      List list = getHibernateTemplate().executeFind(hcb);
      Iterator iter = list.iterator();
      while (iter.hasNext()) {
    	  AssessmentGradingData adata = (AssessmentGradingData) iter.next();
    	  Set itemGradingSet = getItemGradingSet(adata.getAssessmentGradingId());
    	  adata.setItemGradingSet(itemGradingSet);
      }
      
      return list;
  }

  public HashMap getItemScores(Long publishedId, final Long itemId, String which)
  {
	  List scores = getTotalScores(publishedId.toString(), which);
	  return getItemScores(itemId, scores, false);
  }
  
  public HashMap getItemScores(Long publishedId, final Long itemId, String which, boolean loadItemGradingAttachment)
  {
	  List scores = getTotalScores(publishedId.toString(), which);
	  return getItemScores(itemId, scores, loadItemGradingAttachment);
  }
  
  public HashMap getItemScores(final Long itemId, List scores, boolean loadItemGradingAttachment)
  {
    try {
      HashMap map = new HashMap();
      //List list = new ArrayList();

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
              disjunction.add(Expression.in("assessmentGradingId", tempList));
          }
          else{
            tempList = gradingIdList.subList(i, i + 50);
            disjunction.add(Expression.in("assessmentGradingId", tempList));
          }
        }

        if (itemId.equals( Long.valueOf(0))) {
          criteria.add(disjunction);
          //criteria.add(Expression.isNotNull("submittedDate"));
        }
        else {

          /** create logical and between the pubCriterion and the disjunction criterion */
          //Criterion pubCriterion = Expression.eq("publishedItem.itemId", itemId);
          Criterion pubCriterion = Expression.eq("publishedItemId", itemId);
          criteria.add(Expression.and(pubCriterion, disjunction));
          //criteria.add(Expression.isNotNull("submittedDate"));
        }
          criteria.addOrder(Order.asc("agentId"));
          criteria.addOrder(Order.desc("submittedDate"));
          return criteria.list();
          //large list cause out of memory error (java heap space)
          //return criteria.setMaxResults(10000).list();
        }
      };
      List temp = (List) getHibernateTemplate().execute(hcb);

      HashMap<Long,ArrayList<ItemGradingAttachment>> attachmentMap = new HashMap<> ();
      if (loadItemGradingAttachment) {
    	  attachmentMap = getItemGradingAttachmentMap(itemId);
      }
      Iterator iter2 = temp.iterator();
      while (iter2.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter2.next();
        if (loadItemGradingAttachment) {
        	if (attachmentMap.get(data.getItemGradingId()) != null) {
        		data.setItemGradingAttachmentList((ArrayList<ItemGradingAttachment>) attachmentMap.get(data.getItemGradingId()));
			}
			else {
				data.setItemGradingAttachmentList(new ArrayList<ItemGradingAttachment>());
			}
        }
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return new HashMap();
    }
  }

  /**
   * This returns a hashmap of all the latest item entries, keyed by
   * item id for easy retrieval.
   * @param publishedId
   * @param agentId
   * @return 
   */
  public HashMap getLastItemGradingData(final Long publishedId, final String agentId)
  {
    try {
//      Object[] objects = new Object[2];
//      objects[0] = publishedId;
//      objects[1] = agentId;
//      Type[] types = new Type[2];
//      types[0] = Hibernate.LONG;
//      types[1] = Hibernate.STRING;

      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		// I am debating should I use (a.forGrade=false and a.status=NO_SUBMISSION)
      		// or attemptDate is not null
      		Query q = session.createQuery("from AssessmentGradingData a where a.publishedAssessmentId=? " +
      				"and a.agentId=? and a.forGrade=? and a.status<>? " +
      				"order by a.submittedDate DESC");
      		q.setLong(0, publishedId);
      		q.setString(1, agentId);
      		q.setBoolean(2, false);
    		q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
      		return q.list();
      	};
      };
      List scores = getHibernateTemplate().executeFind(hcb);

//      ArrayList scores = (ArrayList) getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? order by submittedDate DESC", objects, types);
      HashMap map = new HashMap();
      if (scores.isEmpty())
        return new HashMap();
      AssessmentGradingData gdata = (AssessmentGradingData) scores.toArray()[0];
      // initialize itemGradingSet
      gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
      if (gdata.getForGrade())
        return new HashMap();
      for( ItemGradingData data : gdata.getItemGradingSet() )
      {
        ArrayList thisone = (ArrayList) map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return new HashMap();
    }
  }



  /**
   * This returns a hashmap of all the submitted items, keyed by
   * item id for easy retrieval.
   * @param assessmentGradingId
   * @return 
   */
  public HashMap getStudentGradingData(String assessmentGradingId)
  {
	  return getStudentGradingData(assessmentGradingId, true);
  }
  
  public HashMap getStudentGradingData(String assessmentGradingId, boolean loadGradingAttachment)
  {
    try {
      HashMap map = new HashMap();
      AssessmentGradingData gdata = load(new Long(assessmentGradingId), loadGradingAttachment);
      log.debug("****#6, gdata="+gdata);
      //log.debug("****#7, item size="+gdata.getItemGradingSet().size());
      for( ItemGradingData data : gdata.getItemGradingSet() )
      {
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return new HashMap();
    }
  }

 
  public HashMap getSubmitData(final Long publishedId, final String agentId, final Integer scoringoption, final Long assessmentGradingId)
  {
    try {
//      Object[] objects = new Object[3];
//      objects[0] = publishedId;
//      objects[1] = agentId;
//      objects[2] = new Boolean(true);
//      Type[] types = new Type[3];
//      types[0] = Hibernate.LONG;
//      types[1] = Hibernate.STRING;
//      types[2] = Hibernate.BOOLEAN;
    	
    		
      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		log.debug("scoringoption = " + scoringoption);
      		if (EvaluationModelIfc.LAST_SCORE.equals(scoringoption)){
      			// last submission
      			Query q;
      			if (assessmentGradingId == null) {
      				q = session.createQuery("from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate DESC");
      				q.setLong(0, publishedId);
      				q.setString(1, agentId);
      				q.setBoolean(2, true);
      			}
      			else {
      				q = session.createQuery("from AssessmentGradingData a where a.assessmentGradingId=? ");
      				q.setLong(0, assessmentGradingId);
      			}
      			return q.list();
      		}
      		else {
      			//highest submission
      			Query q1 = null;
      			if (assessmentGradingId == null) {
      				q1 = session.createQuery("from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.finalScore DESC, a.submittedDate DESC");
      				q1.setLong(0, publishedId);
      				q1.setString(1, agentId);
      				q1.setBoolean(2, true);
      			}
      			else {
      				q1 = session.createQuery("from AssessmentGradingData a where a.assessmentGradingId=? ");
      				q1.setLong(0, assessmentGradingId);
      			}
      			return q1.list();          			
      		}
      	};
      };
      List scores = getHibernateTemplate().executeFind(hcb);

//      ArrayList scores = (ArrayList) getHibernateTemplate().find("from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by submittedDate DESC", objects, types);
      HashMap map = new HashMap();
      if (scores.isEmpty())
        return new HashMap();
      AssessmentGradingData gdata = (AssessmentGradingData) scores.toArray()[0];
      HashMap attachmentMap = getItemGradingAttachmentMapByAssessmentGradingId(gdata.getAssessmentGradingId());
      gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
      for( ItemGradingData data : gdata.getItemGradingSet() )
      {
        if (attachmentMap.get(data.getItemGradingId()) != null) {
    		data.setItemGradingAttachmentList((ArrayList<ItemGradingAttachment>) attachmentMap.get(data.getItemGradingId()));
    	}
    	else {
    		data.setItemGradingAttachmentList(new ArrayList<ItemGradingAttachment>());
    	}
        
        ArrayList thisone = (ArrayList)
          map.get(data.getPublishedItemId());
        if (thisone == null)
          thisone = new ArrayList();
        thisone.add(data);
        map.put(data.getPublishedItemId(), thisone);
      }
      return map;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return new HashMap();
    }
  }

  public Long add(AssessmentGradingData a) {
    int retryCount = persistenceHelper.getRetryCount();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().save(a);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem adding assessmentGrading: "+e.getMessage());
        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
      }
    }
    return a.getAssessmentGradingId();
  }

  public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId){
	Object [] values = {true, publishedAssessmentId};
	List size = getHibernateTemplate().find(
        "select count(a) from AssessmentGradingData a where a.forGrade=? and a.publishedAssessmentId=?", values);
    Iterator iter = size.iterator();
    if (iter.hasNext()){
      int i = ((Integer)iter.next());
      return i;
    }
    else{
      return 0;
    }
  }
  
  public Long saveMedia(byte[] media, String mimeType){
    MediaData mediaData = new MediaData(media, mimeType);
    mediaData.setFileSize((long)media.length);
    return saveMedia(mediaData);
  }

	protected void pushAdvisor() {
		securityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	protected void popAdvisor() {
		securityService.popAdvisor();
	}

	protected boolean checkMediaCollection(String id) {
		pushAdvisor();
		try {
			contentHostingService.checkCollection(id);
		} catch (IdUnusedException | TypeException | PermissionException e) {
			return false;
		} finally {
			popAdvisor();
		}
		return true;
	}

	protected boolean ensureMediaCollection(String id) {
		pushAdvisor();
		try {
			ContentCollection coll = contentHostingService.getCollection(id);
		} catch (IdUnusedException ie) {
			log.debug("Creating collection: " + id);
			String name = id;
			if (name.endsWith("/")) {
				name = id.substring(0, id.length() - 1);
			}
			name = name.substring(name.lastIndexOf('/') + 1);

			try {
				ContentCollectionEdit edit = contentHostingService.addCollection(id);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				contentHostingService.commitCollection(edit);
			} catch (IdUsedException | IdInvalidException | PermissionException | InconsistentException collex) {
				log.warn("[Samigo Media Attachments] Exception while creating collection (" + id + "): " + collex.toString());
				return false;
			}
		} catch (TypeException | PermissionException e) {
			log.warn("[Samigo Media Attachments] General exception while ensuring collection: " + e.toString());
		} finally {
			popAdvisor();
		}
		return true;
	}

	protected boolean ensureMediaPath(String path) {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("[Samigo Media Attachments] Relative media paths are not acceptable. (" + path + ")");
		}

		int lastSlash = path.lastIndexOf("/");

		// Fast track already existing collections
		if (lastSlash != 0 && checkMediaCollection(path.substring(0, lastSlash+1))) {
			return true;
		}

		// Ensure everything exists from the root
		int slash = 1;
		while (slash != lastSlash) {
			slash = path.indexOf("/", slash+1);
			if (!ensureMediaCollection(path.substring(0, slash+1))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Create or update a ContentResource for the media payload of this MediaData.
	 * @param mediaData the complete MediaData item to save if the media byte array is not null
	 * @return the ID in Content Hosting of the stored item; null on failure
	 */
	protected String saveMediaToContent(MediaData mediaData) {
		String mediaPath = getMediaPath(mediaData);
		if (mediaData.getMedia() != null && ensureMediaPath(mediaPath)) {
			log.debug("=====> Saving media: " + mediaPath);
			pushAdvisor();
			boolean newResource = true;

			try {
				contentHostingService.checkResource(mediaPath);
				newResource = false;
			} catch (PermissionException | IdUnusedException | TypeException e) {
				// Just a check, no handling
			}

			try {
				ContentResource chsMedia;
				if (newResource) {
					ContentResourceEdit edit = contentHostingService.addResource(mediaPath);
					edit.setContentType(mediaData.getMimeType());
					edit.setContent(mediaData.getMedia());
					ResourcePropertiesEdit props = edit.getPropertiesEdit();
					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, mediaData.getFilename());
					contentHostingService.commitResource(edit);
					chsMedia = contentHostingService.getResource(mediaPath);
				} else {
					chsMedia = contentHostingService.updateResource(mediaPath, mediaData.getMimeType(), mediaData.getMedia());
				}
				// Free the byte array since it has been stored in content. Hold the new ContentResource
				mediaData.setDbMedia(null);
				mediaData.setContentResource(chsMedia);
				return mediaPath;
			} catch (PermissionException | IdUsedException | IdInvalidException | InconsistentException | ServerOverloadException | OverQuotaException | VirusFoundException | IdUnusedException | TypeException | InUseException e) {
				log.warn("Exception while saving media to content: " + e.toString());
			} finally {
				popAdvisor();
			}
		}
		return null;
	}

	protected ContentResource getMediaContentResource(MediaData mediaData) {
		if (mediaData.getContentResource() != null) {
			return mediaData.getContentResource();
		}

		String id = getMediaPath(mediaData);
		log.debug("=====> Reading media: " + id);
		if (id != null) {
			pushAdvisor();
			try {
				ContentResource res = contentHostingService.getResource(id);
				return res;
			} catch (IdUnusedException ie) {
				log.info("Nonexistent resource when trying to load media (id: " + mediaData.getMediaId() + "): " + id);
			} catch (PermissionException | TypeException e) {
				log.debug("Exception while reading media from content ("+ mediaData.getMediaId() + "):" + e.toString());
			} finally {
				popAdvisor();
			}
		}
		return null;
	}

	protected String getMediaPath(MediaData mediaData) {
		String mediaBase = "/private/samigo/";
		String mediaPath = null;

		ItemGradingData itemGrading = mediaData.getItemGradingData();

		if (itemGrading != null) {
			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
			PublishedAssessmentIfc assessment = getPublishedAssessmentByAssessmentGradingId(
					itemGrading.getAssessmentGradingId());
			String assessmentId = assessment.getPublishedAssessmentId().toString();
			String siteId = publishedAssessmentService.getPublishedAssessmentSiteId(assessmentId);
			String userId = itemGrading.getAgentId();
			String questionId = itemGrading.getPublishedItemId().toString();

			if (questionId != null && assessmentId != null) {
				mediaPath = mediaBase + siteId + "/" + assessmentId + "/" + userId + "/" + questionId + "_"
						+ mediaData.getFilename();
			}
		}

		return mediaPath;
	}

  public Long saveMedia(MediaData mediaData){
    log.debug("****"+mediaData.getFilename()+" saving media...size="+mediaData.getFileSize()+" "+(new Date()));
    int retryCount = persistenceHelper.getRetryCount();

    getMediaPath(mediaData);

    while (retryCount > 0){ 
      try {
        saveMediaToContent(mediaData);
        getHibernateTemplate().saveOrUpdate(mediaData);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving media: "+e.getMessage());
        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
      }
    }
    log.debug("****"+mediaData.getFilename()+" saved media."+(new Date()));
    return mediaData.getMediaId();
  }

  public void removeMediaById(Long mediaId){
	  removeMediaById(mediaId, null);
  }
  public void removeMediaById(Long mediaId, Long itemGradingId){
    String mediaLocation = null;
    Session session = null;
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statement = null;
    PreparedStatement statement0 = null;
    try{
      session = getSessionFactory().openSession();
      conn = session.connection();
      log.debug("****Connection="+conn);
      String query0="select LOCATION from SAM_MEDIA_T where MEDIAID=?";
      statement0 = conn.prepareStatement(query0);
      statement0.setLong(1, mediaId);
      rs =statement0.executeQuery();
      if (rs.next()){
        mediaLocation = rs.getString("LOCATION");
      }
      log.debug("****mediaLocation="+mediaLocation);

      String query="delete from SAM_MEDIA_T where MEDIAID=?";
      statement = conn.prepareStatement(query);
      statement.setLong(1, mediaId);
      statement.executeUpdate();
      if (!conn.getAutoCommit()) {
    	  conn.commit();
      }
    }
    catch(HibernateException | SQLException e){
      log.warn(e.getMessage());
    }
    finally{
    	if (session !=null){
    		try {
    			session.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}
    	if (rs !=null){
    		try {
    			rs.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}
    	if (statement !=null){
    		try {
    			statement.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}
       	if (statement0 !=null){
    		try {
    			statement0.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}

    	if (conn !=null){
    		try {
    			conn.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	} 
    }
    if (mediaLocation != null){
        File mediaFile = new File(mediaLocation);
        if (mediaFile.delete()) {
        	log.warn("problem removing file. mediaLocation = {}", mediaLocation);
        }
    }
    
    if (itemGradingId != null) {
    	ItemGradingData itemGradingData = getItemGrading(itemGradingId);
    	itemGradingData.setAutoScore(Double.valueOf(0));
    	saveItemGrading(itemGradingData);
    }
  }

  public MediaData getMedia(Long mediaId){

    MediaData mediaData = (MediaData) getHibernateTemplate().load(MediaData.class, mediaId);

    // Only try to read from Content Hosting if this isn't a link and
    // there is no media content in the database
    if (mediaData.getLocation() == null && mediaData.getDbMedia() == null) {
        mediaData.setContentResource(getMediaContentResource(mediaData));
    }
    return mediaData;
  }

  public ArrayList getMediaArray(final Long itemGradingId){
    log.debug("*** itemGradingId ="+itemGradingId);
    ArrayList a = new ArrayList();

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("from MediaData m where m.itemGradingData.itemGradingId=?");
    		q.setLong(0, itemGradingId);
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

    for (int i=0;i<list.size();i++){
        MediaData mediaData = (MediaData)list.get(i);
        mediaData.setContentResource(getMediaContentResource(mediaData));
        a.add(mediaData);
    }
    log.debug("*** no. of media = {}", a.size());
    return a;
  }
  
  public ArrayList getMediaArray2(final Long itemGradingId){
	    log.debug("*** itemGradingId ="+itemGradingId);
	    ArrayList a = new ArrayList();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select new MediaData(m.mediaId, m.filename, m.fileSize, m.duration, m.createdDate) "+
	    		         " from MediaData m where m.itemGradingData.itemGradingId=?");
	    		q.setLong(0, itemGradingId);
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

	    for (int i=0;i<list.size();i++){
	      MediaData mediaData = (MediaData)list.get(i);
	      mediaData.setContentResource(getMediaContentResource(mediaData));
	      a.add(mediaData);
	    }
	    log.debug("*** no. of media = {}", a.size());
	    return a;
  }

  public HashMap getMediaItemGradingHash(final Long assessmentGradingId){
	  log.debug("*** assessmentGradingId = {}", assessmentGradingId);
	  HashMap map = new HashMap();

	  final HibernateCallback hcb = new HibernateCallback(){
		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
			  Query q = session.createQuery("select i from MediaData m, ItemGradingData i " +
			  		"where m.itemGradingData.itemGradingId = i.itemGradingId " +
			  		"and i.assessmentGradingId = ? ");
			  q.setLong(0, assessmentGradingId);
			  return q.list();
		  };
	  };
	  List list = getHibernateTemplate().executeFind(hcb);

	  for (int i=0;i<list.size();i++){
		  ItemGradingData itemGradingData = (ItemGradingData)list.get(i);
		  ArrayList al = new ArrayList();
		  al.add(itemGradingData);
		  // There might be duplicate. But we just overwrite it with the same itemGradingData
		  map.put(itemGradingData.getPublishedItemId(), al);
	  }
	  log.debug("*** no. of media = {}", map.size());
	  return map;
  }

  public ArrayList getMediaArray(ItemGradingData item){
    ArrayList a = new ArrayList();
    List list = getHibernateTemplate().find(
        "from MediaData m where m.itemGradingData=?", item );
    for (int i=0;i<list.size();i++){
        MediaData mediaData = (MediaData)list.get(i);
        mediaData.setContentResource(getMediaContentResource(mediaData));
        a.add(mediaData);
    }
    log.debug("*** no. of media = {}", a.size());
    return a;
  }

  public List getMediaArray(Long publishedId, final Long publishedItemId, String which)
  {
    try {
    	HashMap itemScores = (HashMap) getItemScores(publishedId, publishedItemId, which);
    	final List list = (List) itemScores.get(publishedItemId);
    	log.debug("list size list.size() = " + list.size());
    	
    	HibernateCallback hcb = new HibernateCallback()
    	{
    		public Object doInHibernate(Session session) throws HibernateException, SQLException
    		{
    			Criteria criteria = session.createCriteria(MediaData.class);
    			Disjunction disjunction = Expression.disjunction();

    			/** make list from AssessmentGradingData ids */
    			List itemGradingIdList = new ArrayList();
    			for (int i = 0; i < list.size() ; i++) {
    				ItemGradingData itemGradingData = (ItemGradingData) list.get(i);
    				itemGradingIdList.add(itemGradingData.getItemGradingId());
    			}

    			/** create or disjunctive expression for (in clauses) */
    			List tempList;
    			for (int i = 0; i < itemGradingIdList.size(); i += 50){
    				if (i + 50 > itemGradingIdList.size()){
    					tempList = itemGradingIdList.subList(i, itemGradingIdList.size());
    					disjunction.add(Expression.in("itemGradingData.itemGradingId", tempList));
    				}
    				else{
    					tempList = itemGradingIdList.subList(i, i + 50);
    					disjunction.add(Expression.in("itemGradingData.itemGradingId", tempList));
    				}
    			}
    			criteria.add(disjunction);
    			return criteria.list();
    	        //large list cause out of memory error (java heap space)
    			//return criteria.setMaxResults(10000).list();
    		}
    	};

       ArrayList a = new ArrayList();
       List hbmList = (List) getHibernateTemplate().execute(hcb);
       for (int i=0;i<hbmList.size();i++){
               MediaData mediaData = (MediaData)hbmList.get(i);
               mediaData.setContentResource(getMediaContentResource(mediaData));
               a.add(mediaData);
       }
       return a;

    	} catch (Exception e) {
    		log.warn(e.getMessage(), e);
    		return new ArrayList();
    	}
  }
  
	public List<Long> getMediaConversionBatch() {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			public List<Long> doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery("SELECT id FROM MediaData WHERE dbMedia IS NOT NULL AND location IS NULL");
				q.setMaxResults(10);
				return q.list();
			}
		};
		return getHibernateTemplate().execute(hcb);
	}

	public boolean markMediaForConversion(final List<Long> mediaIds) {
		final HibernateCallback hcb = new HibernateCallback() {
			public Integer doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery("UPDATE MediaData SET location = 'CONVERTING' WHERE id in (:ids)");
				q.setParameterList("ids", mediaIds);
				return q.executeUpdate();
			}
		};
		return getHibernateTemplate().execute(hcb).equals(mediaIds.size());
	}

	public List<Long> getMediaWithDataAndLocation() {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			public List<Long> doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery("SELECT id FROM MediaData WHERE dbMedia IS NOT NULL AND location IS NOT NULL");
				return q.list();
			}
		};
		return getHibernateTemplate().execute(hcb);
	}

	public List<Long> getMediaInConversion() {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			public List<Long> doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery("SELECT id FROM MediaData WHERE location = 'CONVERTING'");
				return q.list();
			}
		};
		return getHibernateTemplate().execute(hcb);
	}

  public ItemGradingData getLastItemGradingDataByAgent(
      final Long publishedItemId, final String agentId)
  {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("from ItemGradingData i where i.publishedItemId=? and i.agentId=?");
	    		q.setLong(0, publishedItemId);
	    		q.setString(1, agentId);
	    		return q.list();
	    	};
	    };
	    List itemGradings = getHibernateTemplate().executeFind(hcb);

//	  List itemGradings = getHibernateTemplate().find(
//        "from ItemGradingData i where i.publishedItemId=? and i.agentId=?",
//        new Object[] { publishedItemId, agentId },
//        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (itemGradings.isEmpty())
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public ItemGradingData getItemGradingData(final Long itemGradingId) {
    final HibernateCallback hcb = new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query q = session.createQuery("from ItemGradingData i where i.itemGradingId=?");
        q.setLong(0, itemGradingId);
        return q.list();
      };
    };
    List itemGradings = getHibernateTemplate().executeFind(hcb);
    if (itemGradings.isEmpty())
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public ItemGradingData getItemGradingData(
      final Long assessmentGradingId, final Long publishedItemId)
  {
    log.debug("****assessmentGradingId={}", assessmentGradingId);
    log.debug("****publishedItemId={}", publishedItemId);

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(
    				"from ItemGradingData i where i.assessmentGradingId = ? and i.publishedItemId=?");
    		q.setLong(0, assessmentGradingId);
    		q.setLong(1, publishedItemId);
    		return q.list();
    	};
    };
    List itemGradings = getHibernateTemplate().executeFind(hcb);

//    List itemGradings = getHibernateTemplate().find(
//        "from ItemGradingData i where i.assessmentGradingId = ? and i.publishedItemId=?",
//        new Object[] { assessmentGradingId, publishedItemId },
//        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.LONG });
    if (itemGradings.isEmpty())
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }
  
  public AssessmentGradingData load(Long id) {
	  return load(id, true);
  }

  public AssessmentGradingData load(Long id, boolean loadGradingAttachment) {
    AssessmentGradingData gdata = (AssessmentGradingData) getHibernateTemplate().load(AssessmentGradingData.class, id);
    Set<ItemGradingData> itemGradingSet = new HashSet();

    // Get (ItemGradingId, ItemGradingData) pair
    HashMap<Long, ItemGradingData> itemGradingMap = getItemGradingMap(gdata.getAssessmentGradingId());
    if (itemGradingMap.keySet().size() > 0) {
    	Collection<ItemGradingData> itemGradingCollection = itemGradingMap.values();
    	
    	if (loadGradingAttachment) {
    		// Get (ItemGradingId, ItemGradingAttachment) pair
    		HashMap attachmentMap = getItemGradingAttachmentMap(itemGradingMap.keySet());
    		
    		Iterator<ItemGradingData> iter = itemGradingCollection.iterator();
    		while (iter.hasNext()) {
    			ItemGradingData itemGradingData = iter.next();
    			if (attachmentMap.get(itemGradingData.getItemGradingId()) != null) {
    				itemGradingData.setItemGradingAttachmentList((ArrayList<ItemGradingAttachment>) attachmentMap.get(itemGradingData.getItemGradingId()));
    			}
    			else {
    				itemGradingData.setItemGradingAttachmentList(new ArrayList<ItemGradingAttachment>());
    			}
    			itemGradingSet.add(itemGradingData);
    		}
        }
        else {
    			itemGradingSet.addAll(itemGradingCollection);
        }
    }
    
    gdata.setItemGradingSet(itemGradingSet);
    return gdata;
  }

  public ItemGradingData getItemGrading(Long id) {
    return (ItemGradingData) getHibernateTemplate().load(ItemGradingData.class, id);
  }

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
    AssessmentGradingData ag = null;
    // don't pick the assessmentGradingData that is created by instructor entering comments/scores
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(
    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? and a.status<>? order by a.submittedDate desc");
    		q.setLong(0, publishedAssessmentId);
    		q.setString(1, agentIdString);
    		q.setBoolean(2, false);
    		q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(
//        "from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc",
//         new Object[] { publishedAssessmentId, agentIdString, Boolean.FALSE },
//         new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.BOOLEAN });
    if (!assessmentGradings.isEmpty()){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }

  public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString, Long assessmentGradingId) {
	    AssessmentGradingData ag = null;

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc");
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    if (assessmentGradingId == null) {
	    	if (assessmentGradings.size() > 0) {
	    		ag = (AssessmentGradingData) assessmentGradings.get(0);
	    	}
	    }
	    else {
	    	for (int i=0; i<assessmentGradings.size(); i++) {
	    		AssessmentGradingData agd = (AssessmentGradingData) assessmentGradings.get(i);
	    		if (agd.getAssessmentGradingId().compareTo(assessmentGradingId) == 0) {
	    			ag = agd;
	    			ag.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId()));
	    			break;
	    		}
	    	}   
	    }
	    
	    // get AssessmentGradingAttachments
	    Map<Long, List<AssessmentGradingAttachment>> map = getAssessmentGradingAttachmentMap(publishedAssessmentId);
	    List<AssessmentGradingAttachment> attachments = map.get(ag.getAssessmentGradingId());
	    if (attachments != null) {
	    	ag.setAssessmentGradingAttachmentList(attachments);
	    }
	    else {
	    	ag.setAssessmentGradingAttachmentList(new ArrayList<AssessmentGradingAttachment>());
	    }
	    
	    return ag;
  }
  
  public AssessmentGradingData getLastAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
    AssessmentGradingData ag = null;

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(
    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? order by a.submittedDate desc");
    		q.setLong(0, publishedAssessmentId);
    		q.setString(1, agentIdString);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(
//        "from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? order by a.submittedDate desc",
//         new Object[] { publishedAssessmentId, agentIdString },
//         new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (!assessmentGradings.isEmpty()){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }


  public void saveItemGrading(ItemGradingData item) {
    int retryCount = persistenceHelper.getRetryCount();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().saveOrUpdate((ItemGradingData)item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving itemGrading: "+e.getMessage());
        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
      }
    }
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingData assessment) {
    int retryCount = persistenceHelper.getRetryCount();
    while (retryCount > 0){ 
      try {
        if (assessment.getAssessmentGradingId() != null) {
            getHibernateTemplate().merge((AssessmentGradingData)assessment);
        }
        else {
            getHibernateTemplate().save((AssessmentGradingData)assessment);
        }
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem inserting/updating assessmentGrading: {}", e.getMessage());
        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
      }
    }
  }

  private byte[] getMediaStream(Long mediaId){
    byte[] b = new byte[4000];
    Session session = null;
    Connection conn = null;
    InputStream in = null; 
    ResultSet rs = null;
    PreparedStatement statement = null;
    try{
      session = getSessionFactory().openSession();
      conn = session.connection();
      log.debug("****Connection={}", conn);
      String query="select MEDIA from SAM_MEDIA_T where MEDIAID=?";
      statement = conn.prepareStatement(query);
      statement.setLong(1, mediaId);
      rs = statement.executeQuery();
      if (rs.next()){
        java.lang.Object o = rs.getObject("MEDIA");
        if (o!=null){
          in = rs.getBinaryStream("MEDIA");
          in.mark(0);
          int ch;
          int len=0;
          while ((ch=in.read())!=-1){
            len++;
	  }
          b = new byte[len];
          in.reset();
          in.read(b,0,len);
        }
      }
    }
    catch(HibernateException | SQLException | IOException e){
      log.warn(e.getMessage());
    }
    
    finally{
    	if (session !=null){
    		try {
    			session.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}
    	if (rs !=null){
    		try {
    			rs.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}
    	if (statement !=null){
    		try {
    			statement.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	}
       	if (in !=null){
    		try {
    			in.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	} 
    	if (conn !=null){
    		try {
    			conn.close();
    		} catch (Exception e1) {
    			log.warn(e1.getMessage(), e1);
    		}
    	} 
    }

    return b;
  }

  public List getAssessmentGradingIds(final Long publishedItemId){
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select g.assessmentGradingId from "+
	    		         " ItemGradingData g where g.publishedItemId=?");
	    		q.setLong(0, publishedItemId);
	    		return q.list();
	    	};
	    };
	    return getHibernateTemplate().executeFind(hcb);

//	  return getHibernateTemplate().find(
//         "select g.assessmentGradingId from "+
//         " ItemGradingData g where g.publishedItemId=?",
//         new Object[] { publishedItemId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });
  }

  public AssessmentGradingData getHighestAssessmentGrading(
         final Long publishedAssessmentId, final String agentId)
  {
    AssessmentGradingData ag = null;
    final String query ="from AssessmentGradingData a "+
						" where a.publishedAssessmentId=? and "+
						" a.agentId=? order by a.finalScore desc, a.submittedDate desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId);
    		q.setString(1, agentId);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//        new Object[] { publishedAssessmentId, agentId },
//        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (!assessmentGradings.isEmpty()){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }
  
  public AssessmentGradingData getHighestSubmittedAssessmentGrading(
	         final Long publishedAssessmentId, final String agentId, Long assessmentGradingId)
	  {
	    AssessmentGradingData ag = null;
	    final String query ="from AssessmentGradingData a "+
        					" where a.publishedAssessmentId=? and a.agentId=? and "+
        					" a.forGrade=?  order by a.finalScore desc, a.submittedDate desc";
	    
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentId);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    if (assessmentGradingId == null) {
	    	if (assessmentGradings.size() > 0) {
	    		ag = (AssessmentGradingData) assessmentGradings.get(0);
	    	}
	    }
	    else {
	    	for (int i=0; i<assessmentGradings.size(); i++) {
	    		AssessmentGradingData agd = (AssessmentGradingData) assessmentGradings.get(i);
	    		if (agd.getAssessmentGradingId().compareTo(assessmentGradingId) == 0) {
	    			ag = agd;
	    			ag.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId()));
	    			break;
	    		}
	    	}   
	    }
	    
	    // get AssessmentGradingAttachments
	    List<AssessmentGradingAttachment> attachments = new ArrayList<AssessmentGradingAttachment>();
	    if (ag != null) {
	    	Map<Long, List<AssessmentGradingAttachment>> map = getAssessmentGradingAttachmentMap(publishedAssessmentId);
	    	if (map != null && map.containsKey(ag.getAssessmentGradingId())) {
	    		attachments = map.get(ag.getAssessmentGradingId());
	    	}
	        ag.setAssessmentGradingAttachmentList(attachments);
	    }
	    return ag;
	  }

  public List getLastAssessmentGradingList(final Long publishedAssessmentId){
    final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? order by a.agentId asc, a.submittedDate desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//         new Object[] { publishedAssessmentId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });

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

  public List getLastSubmittedAssessmentGradingList(final Long publishedAssessmentId){
	    final String query = "select a from AssessmentGradingData a left join fetch a.assessmentGradingAttachmentSet where a.publishedAssessmentId=? and a.forGrade=? order by a.agentId asc, a.submittedDate desc";

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedAssessmentId);
	    		q.setBoolean(1, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

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
  
  public List getLastSubmittedOrGradedAssessmentGradingList(final Long publishedAssessmentId){
	    final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? and (a.forGrade=? or (a.forGrade=? and a.status=?)) order by a.agentId asc, a.submittedDate desc";

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedAssessmentId);
	    		q.setBoolean(1, true);
	    		q.setBoolean(2, false);
	    		q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

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
  
  public List getHighestAssessmentGradingList(final Long publishedAssessmentId){
    final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? order by a.agentId asc, a.finalScore desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//         new Object[] { publishedAssessmentId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });

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

  
  public List getHighestSubmittedOrGradedAssessmentGradingList(final Long publishedAssessmentId){
	    final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? and (a.forGrade=? or (a.forGrade=? and a.status=?)) order by a.agentId asc, a.finalScore desc";

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedAssessmentId);
	    		q.setBoolean(1, true);
	    		q.setBoolean(2, false);
	    		q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

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
  public HashMap getLastAssessmentGradingByPublishedItem(final Long publishedAssessmentId){
    HashMap h = new HashMap();
    final String query = "select new AssessmentGradingData("+
                   " a.assessmentGradingId, p.itemId, "+
                   " a.agentId, a.finalScore, a.submittedDate) "+
                   " from ItemGradingData i, AssessmentGradingData a,"+
                   " PublishedItemData p where "+
                   " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and "+
                   " a.publishedAssessmentId=? " +
                   " order by a.agentId asc, a.submittedDate desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//         new Object[] { publishedAssessmentId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });

//    ArrayList l = new ArrayList();
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
      if (currentAgent.equals(g.getAgentId())
          && ((submittedDate==null && g.getSubmittedDate()==null)
              || (submittedDate!=null && submittedDate.equals(g.getSubmittedDate())))){
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
  public HashMap getHighestAssessmentGradingByPublishedItem(final Long publishedAssessmentId){
    HashMap h = new HashMap();
    final String query = "select new AssessmentGradingData("+
                   " a.assessmentGradingId, p.itemId, "+
                   " a.agentId, a.finalScore, a.submittedDate) "+
                   " from ItemGradingData i, AssessmentGradingData a, "+
                   " PublishedItemData p where "+
                   " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and "+
                   " a.publishedAssessmentId=? " +
                   " order by a.agentId asc, a.finalScore desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//         new Object[] { publishedAssessmentId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });

//    ArrayList l = new ArrayList();
    String currentAgent="";
    Double finalScore = null;
    for (int i=0; i<assessmentGradings.size(); i++){
      AssessmentGradingData g = (AssessmentGradingData)assessmentGradings.get(i);
      Long itemId = g.getPublishedItemId();
      Long gradingId = g.getAssessmentGradingId();
      log.debug("**** itemId="+itemId+", gradingId="+gradingId+", agentId="+g.getAgentId()+", score="+g.getFinalScore());
      if ( i==0 ){
        currentAgent = g.getAgentId();
        finalScore = g.getFinalScore();
      }
      if (currentAgent.equals(g.getAgentId()) 
        && ((finalScore==null && g.getFinalScore()==null)
            || (finalScore!=null &&  finalScore.equals(g.getFinalScore())))){
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

  public Set getItemGradingSet(final Long assessmentGradingId){
    final String query = "from ItemGradingData i where i.assessmentGradingId=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, assessmentGradingId);
    		return q.list();
    	};
    };
    List itemGradings = getHibernateTemplate().executeFind(hcb);
    HashSet s = new HashSet();
    for (int i=0; i<itemGradings.size();i++){
      s.add(itemGradings.get(i));
    }
    return s;
  }
  
  public HashMap<Long, ItemGradingData> getItemGradingMap(final Long assessmentGradingId){
	    final String query = "from ItemGradingData i where i.assessmentGradingId=?";

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, assessmentGradingId);
	    		return q.list();
	    	};
	    };
	    List itemGradingList = getHibernateTemplate().executeFind(hcb);
	    HashMap<Long, ItemGradingData> m = new HashMap<>();
	    for (int i=0; i<itemGradingList.size();i++){
	      m.put(((ItemGradingData)itemGradingList.get(i)).getItemGradingId(), (ItemGradingData) itemGradingList.get(i));
	    }
	    return m;
  }

  public HashMap getAssessmentGradingByItemGradingId(final Long publishedAssessmentId){
    List aList = getAllSubmissions(publishedAssessmentId.toString());
    HashMap aHash = new HashMap();
    for (int j=0; j<aList.size();j++){
      AssessmentGradingData a = (AssessmentGradingData)aList.get(j);
      aHash.put(a.getAssessmentGradingId(), a);
    }

    final String query = "select new ItemGradingData(i.itemGradingId, a.assessmentGradingId) "+
                   " from ItemGradingData i, AssessmentGradingData a "+
                   " where i.assessmentGradingId=a.assessmentGradingId "+
                   " and a.publishedAssessmentId=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId);
    		return q.list();
    	};
    };
    List l = getHibernateTemplate().executeFind(hcb);

//    List l = getHibernateTemplate().find(query,
//             new Object[] { publishedAssessmentId },
//             new org.hibernate.type.Type[] { Hibernate.LONG });
    //System.out.println("****** assessmentGradinghash="+l.size());
    HashMap h = new HashMap();
    for (int i=0; i<l.size();i++){
      ItemGradingData o = (ItemGradingData)l.get(i);
      h.put(o.getItemGradingId(), (AssessmentGradingData)aHash.get(o.getAssessmentGradingId()));
    }
    return h;
  }

  public void deleteAll(Collection c){
    int retryCount = persistenceHelper.getRetryCount();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().deleteAll(c);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem inserting assessmentGrading: "+e.getMessage());
        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
      }
    }
  }


  public void saveOrUpdateAll(Collection<ItemGradingData> c) {
    int retryCount = persistenceHelper.getRetryCount();
    
    c.removeAll(Collections.singleton(null));
    while (retryCount > 0){ 
      try {
          for (ItemGradingData itemGradingData : c) {
              getHibernateTemplate().merge(itemGradingData);
          }
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem inserting assessmentGrading: "+e.getMessage());
        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
      }
    }
  }

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(final Long assessmentGradingId){
    PublishedAssessmentIfc pub = null;
    final String query = "select p from PublishedAssessmentData p, AssessmentGradingData a "+
                   " where a.publishedAssessmentId=p.publishedAssessmentId and a.assessmentGradingId=?";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, assessmentGradingId);
    		return q.list();
    	};
    };
    List pubList = getHibernateTemplate().executeFind(hcb);

//    List pubList = getHibernateTemplate().find(query,
//                                                    new Object[] { assessmentGradingId },
//                                                    new org.hibernate.type.Type[] { Hibernate.LONG });
    if (pubList!=null && pubList.size()>0)
      pub = (PublishedAssessmentIfc) pubList.get(0);

    return pub; 
  }

  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(final Long publishedItemId){
	    PublishedAssessmentIfc pub = null;
	    final String query = "select p from PublishedAssessmentData p, PublishedItemData i "+
	                   " where p.publishedAssessmentId=i.section.assessment.publishedAssessmentId and i.itemId=?";

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedItemId);
	    		return q.list();
	    	};
	    };
	    List pubList = getHibernateTemplate().executeFind(hcb);

	    if (pubList!=null && pubList.size()>0)
	      pub = (PublishedAssessmentIfc) pubList.get(0);

	    return pub; 
  }

  public ArrayList getLastItemGradingDataPosition(final Long assessmentGradingId, final String agentId)
  {
	  ArrayList position = new ArrayList();  
	  try {
		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery("select s.sequence " +
						  " from ItemGradingData i, PublishedItemData pi, PublishedSectionData s " +
						  " where i.agentId = ? and i.assessmentGradingId = ? " +
						  " and pi.itemId = i.publishedItemId " +
						  " and pi.section.id = s.id " +
						  " group by i.publishedItemId, s.sequence, pi.sequence " + 
						  " order by s.sequence desc , pi.sequence desc");
				  q.setString(0, agentId);
				  q.setLong(1, assessmentGradingId);
				  return q.list();
			  };
		  };
		  List list = getHibernateTemplate().executeFind(hcb);
		  if ( list.isEmpty()) {
			  position.add(0);
			  position.add(0);
		  }
		  else {
			  Integer sequence = (Integer) list.get(0);
			  Integer nextSequence;
			  int count = 1;
			  for (int i = 1; i < list.size(); i++) {
				  log.debug("i = {}", i);
				  nextSequence = (Integer) list.get(i);
				  if (sequence.equals(nextSequence)) {
					  log.debug("equal");
					  count++;
				  }
				  else {
					  break;
				  }
			  }
			  log.debug("sequence = " + sequence);
			  log.debug("count = " + count);
			  position.add(sequence);
			  position.add(count);
		  }
		  return position;
	  } 
	  catch (Exception e) {
		  log.warn(e.getMessage(), e);
		  position.add(0);
		  position.add(0);
		  return position;
	  }
  }
  
  public List getPublishedItemIds(final Long assessmentGradingId){
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select i.publishedItemId from "+
	    		         " ItemGradingData i where i.assessmentGradingId=?");
	    		q.setLong(0, assessmentGradingId);
	    		return q.list();
	    	};
	    };
	    return getHibernateTemplate().executeFind(hcb);
  }
  
  public List getItemGradingIds(final Long assessmentGradingId){
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select i.itemGradingId from "+
	    		         " ItemGradingData i where i.assessmentGradingId=?");
	    		q.setLong(0, assessmentGradingId);
	    		return q.list();
	    	};
	    };
	    return getHibernateTemplate().executeFind(hcb);
}
  
  public HashSet getItemSet(final Long publishedAssessmentId, final Long sectionId) {
	  HashSet itemSet = new HashSet();

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select distinct p.itemId " +
	    				"from PublishedItemData p, AssessmentGradingData a, ItemGradingData i " +
	    				"where a.publishedAssessmentId=? and a.forGrade=? and p.section.id=? " +
	    				"and i.assessmentGradingId = a.assessmentGradingId " +
	    				"and p.itemId = i.publishedItemId ");

	    		q.setLong(0, publishedAssessmentId);
	    		q.setBoolean(1, true);
	    		q.setLong(2, sectionId);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    final Collection<Long> itemIds = new ArrayList<>();
	    Iterator iter = assessmentGradings.iterator();
	    while(iter.hasNext()) {
    		itemIds.add((Long) iter.next());
	    }

	    if(itemIds.isEmpty()) return itemSet;
	    
	    final HibernateCallback hcb2 = new HibernateCallback() {
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    			    		
	    		final Criteria criteria = session.createCriteria( PublishedItemData.class );
	    		if( itemIds.size() > 1000 ) {
	    			final Set<Long> ids = new HashSet<>();
	    			Disjunction disjunction = Restrictions.disjunction();
	    			
	    			for( Long id : itemIds ) {
	    				if( ids.size() < 1000 ) {
	    					ids.add( id );
	    				}
	    				else {
	    					criteria.add( disjunction.add( Restrictions.in( "itemId", ids ) ) );
	    					ids.clear();
	    				}
	    			}
	    		}
	    		else {
	    			criteria.add( Restrictions.in( "itemId", itemIds ) );
	    		}
	    		
	    		return criteria.list();
	    	};
	    };

	    List publishedItems = getHibernateTemplate().executeFind(hcb2);
	    Iterator pubIter = publishedItems.iterator();
	    PublishedItemData publishedItemData2;
	    while(pubIter.hasNext()) {
	    	publishedItemData2 = (PublishedItemData) pubIter.next();
	    	log.debug("itemId = " + publishedItemData2.getItemId());
	    	itemSet.add(publishedItemData2);
	    }
	    
	    return itemSet;
  }

  public Long getTypeId(final Long itemGradingId) {
	  Long typeId = Long.valueOf(-1);

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select p.typeId " +
	    				"from PublishedItemData p, ItemGradingData i " +
	    				"where i.itemGradingId=? " +
	    				"and p.itemId = i.publishedItemId ");
	    		q.setLong(0, itemGradingId);
	    		return q.list();
	    	};
	    };
	    List typeIdList = getHibernateTemplate().executeFind(hcb);

	    Iterator iter = typeIdList.iterator();
	    while(iter.hasNext()) {
	    	typeId = (Long) iter.next();
	    	log.debug("typeId = {}", typeId);
	    }
	    return typeId;
  }

  public List getAllAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc");
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    return assessmentGradings;
  }
  
	public List<ItemGradingData> getAllItemGradingDataForItemInGrading(
			final Long assesmentGradingId,  final Long publishedItemId) {
		if (assesmentGradingId == null) {
			throw new IllegalArgumentException("assesmentGradingId cant' be null");
		}
		
		if (publishedItemId == null) {
			throw new IllegalArgumentException("publishedItemId cant' be null");
		}
		
		List assessmentGradings = getSession().createCriteria(ItemGradingData.class)
		.add(Restrictions.eq("assessmentGradingId", assesmentGradingId))
		.add(Restrictions.eq("publishedItemId", publishedItemId)).list();
		
		return assessmentGradings;
	}
  
  public HashMap getSiteSubmissionCountHash(final String siteId) {
	  HashMap siteSubmissionCountHash = new HashMap();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select a.publishedAssessmentId, a.agentId, count(*) " +
	    				"from AssessmentGradingData a, AuthorizationData au  " +
	    				"where a.forGrade=? and au.functionId = ? and au.agentIdString = ? and a.publishedAssessmentId = au.qualifierId " +
	    				"group by a.publishedAssessmentId, a.agentId " +
	    				"order by a.publishedAssessmentId, a.agentId ");
	    		q.setBoolean(0, true);
	    		q.setString(1, "OWN_PUBLISHED_ASSESSMENT");
	    		q.setString(2, siteId);
	    		return q.list();
	    	};
	    };
	    
	    List countList = getHibernateTemplate().executeFind(hcb);
		Iterator iter = countList.iterator();
		Long lastPublishedAssessmentId = -1l;
		HashMap numberSubmissionPerStudentHash = new HashMap();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next(); 
			Long publishedAssessmentid = (Long) o[0];
			
			if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
				numberSubmissionPerStudentHash.put(o[1], o[2]);
			}
			else {
				numberSubmissionPerStudentHash = new HashMap();
				numberSubmissionPerStudentHash.put(o[1], o[2]);
				siteSubmissionCountHash.put(publishedAssessmentid, numberSubmissionPerStudentHash);
				lastPublishedAssessmentId = publishedAssessmentid;
			}
		}
	    
	    return siteSubmissionCountHash;
}
  
  public HashMap getSiteInProgressCountHash(final String siteId) {
	  HashMap siteInProgressCountHash = new HashMap();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select a.publishedAssessmentId, a.agentId, count(*) " +
	    				"from AssessmentGradingData a, AuthorizationData au  " +
	    				"where a.forGrade=? and au.functionId = ? and au.agentIdString = ? " +
	    				"and a.publishedAssessmentId = au.qualifierId and (a.status=? or a.status=?) " +
	    				"group by a.publishedAssessmentId, a.agentId " +
	    				"order by a.publishedAssessmentId, a.agentId ");
	    		q.setBoolean(0, false);
	    		q.setString(1, "OWN_PUBLISHED_ASSESSMENT");
	    		q.setString(2, siteId);
	    		q.setInteger(3, 0);
	    		q.setInteger(4, 6);
	    		return q.list();
	    	};
	    };
	    
	    List countList = getHibernateTemplate().executeFind(hcb);
		Iterator iter = countList.iterator();
		Long lastPublishedAssessmentId = -1l;
		HashMap numberInProgressPerStudentHash = new HashMap();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next(); 
			Long publishedAssessmentid = (Long) o[0];
			
			if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
				numberInProgressPerStudentHash.put(o[1], o[2]);
			}
			else {
				numberInProgressPerStudentHash = new HashMap();
				numberInProgressPerStudentHash.put(o[1], o[2]);
				siteInProgressCountHash.put(publishedAssessmentid, numberInProgressPerStudentHash);
				lastPublishedAssessmentId = publishedAssessmentid;
			}
		}
	    
	    return siteInProgressCountHash;
  }
  
  public int getActualNumberRetake(final Long publishedAssessmentId, final String agentIdString) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
	    				" where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? " +
	    				" and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
	    				" and a.submittedDate > s.createdDate");
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List countList = getHibernateTemplate().executeFind(hcb);
	    Iterator iter = countList.iterator();
	    if (iter.hasNext()){
	      int i = ((Integer)iter.next());
	      return i;
	    }
	    else{
	      return 0;
	    }
  }
  
  public HashMap getSiteActualNumberRetakeHash(final String siteId) {
		HashMap actualNumberRetakeHash = new HashMap();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select a.publishedAssessmentId, a.agentId, count(*) " +
	    				" from AssessmentGradingData a, StudentGradingSummaryData s, AuthorizationData au, PublishedAssessmentData p " +
	    				" where a.forGrade=? and au.functionId = ? and au.agentIdString = ? and a.publishedAssessmentId = au.qualifierId" +
	    				" and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
	    				" and a.submittedDate > s.createdDate" +
	    				" and a.publishedAssessmentId = p.publishedAssessmentId" +
	    				" and p.status != 2" +
	    				" group by a.publishedAssessmentId, a.agentId" +
	    				" order by a.publishedAssessmentId");
	    		q.setBoolean(0, true);
	    		q.setString(1, "OWN_PUBLISHED_ASSESSMENT");
	    		q.setString(2, siteId);
	    		return q.list();
	    	};
	    };
	    List countList = getHibernateTemplate().executeFind(hcb);
		Iterator iter = countList.iterator();
		Long lastPublishedAssessmentId = -1l;
		HashMap actualNumberRetakePerStudentHash = new HashMap();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next(); 
			Long publishedAssessmentid = (Long) o[0];

			if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
				actualNumberRetakePerStudentHash.put(o[1], o[2]);
			}
			else {
				actualNumberRetakePerStudentHash = new HashMap();
				actualNumberRetakePerStudentHash.put(o[1], o[2]);
				actualNumberRetakeHash.put(publishedAssessmentid, actualNumberRetakePerStudentHash);
				lastPublishedAssessmentId = publishedAssessmentid;
			}
		}
		
		return actualNumberRetakeHash;
  }
  
  public HashMap getActualNumberRetakeHash(final String agentIdString) {
		HashMap actualNumberRetakeHash = new HashMap();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select a.publishedAssessmentId, count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
	    				" where a.agentId=? and a.forGrade=? " +
	    				" and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
	    				" and a.submittedDate > s.createdDate" +
	    				" group by a.publishedAssessmentId");
	    		q.setString(0, agentIdString);
	    		q.setBoolean(1, true);
	    		return q.list();
	    	};
	    };
	    List countList = getHibernateTemplate().executeFind(hcb);
		Iterator iter = countList.iterator();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next(); 
			actualNumberRetakeHash.put(o[0], o[1]);
		}
		return actualNumberRetakeHash;
  }
  
  public List getStudentGradingSummaryData(final Long publishedAssessmentId, final String agentIdString) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select s " +
	    				"from StudentGradingSummaryData s " +
	    				"where s.publishedAssessmentId=? and s.agentId=?");
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentIdString);
	    		return q.list();
	    	};
	    };
	    List studentGradingSummaryDataList = getHibernateTemplate().executeFind(hcb);

	    return studentGradingSummaryDataList;
  }
  
  public int getNumberRetake(final Long publishedAssessmentId, final String agentIdString) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select s.numberRetake " +
	    				"from StudentGradingSummaryData s " +
	    				"where s.publishedAssessmentId=? and s.agentId=?");
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentIdString);
	    		return q.list();
	    	};
	    };
	    List numberRetakeList = getHibernateTemplate().executeFind(hcb);

	    if (numberRetakeList.isEmpty()) {
	    	return 0;
	    }
	    else {
	    	Integer numberRetake = (Integer) numberRetakeList.get(0);
	    	return numberRetake;
	    }
  }
  
  public HashMap getNumberRetakeHash(final String agentIdString) {
	  HashMap h = new HashMap();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select s " +
	    				"from StudentGradingSummaryData s " +
	    				"where s.agentId=? ");
	    		q.setString(0, agentIdString);
	    		return q.list();
	    	};
	    };
	    List numberRetakeList = getHibernateTemplate().executeFind(hcb);
	    for (int i = 0; i < numberRetakeList.size(); i++) {
	    	StudentGradingSummaryData s = (StudentGradingSummaryData) numberRetakeList.get(i);
			h.put(s.getPublishedAssessmentId(), s);
		}
		return h;
  }
  
  public HashMap getSiteNumberRetakeHash(final String siteId) {
	  HashMap siteNumberRetakeHash = new HashMap();
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select s " +
	    				"from StudentGradingSummaryData s, AuthorizationData au " +
	    				"where au.functionId = ? and au.agentIdString = ? " +
	    				"and s.publishedAssessmentId = au.qualifierId " +
	    				"order by s.publishedAssessmentId, s.agentId");
	    		q.setString(0, "OWN_PUBLISHED_ASSESSMENT");
	    		q.setString(1, siteId);
	    		return q.list();
	    	};
	    };
	    List countList = getHibernateTemplate().executeFind(hcb);
	    Iterator iter = countList.iterator();
		Long lastPublishedAssessmentId = -1l;
		HashMap numberRetakePerStudentHash = null;
		while (iter.hasNext()) {
			StudentGradingSummaryData s = (StudentGradingSummaryData) iter.next();
			Long publishedAssessmentid = s.getPublishedAssessmentId();
			
			if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
				numberRetakePerStudentHash.put(s.getAgentId(), s.getNumberRetake());
			}
			else {
				numberRetakePerStudentHash = new HashMap();
				numberRetakePerStudentHash.put(s.getAgentId(), s.getNumberRetake());
				siteNumberRetakeHash.put(publishedAssessmentid, numberRetakePerStudentHash);
				lastPublishedAssessmentId = publishedAssessmentid;
			}
		}
		
		return siteNumberRetakeHash;
  }
  
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
	    int retryCount = persistenceHelper.getRetryCount();
	    while (retryCount > 0){ 
	      try {
	        getHibernateTemplate().saveOrUpdate((StudentGradingSummaryData) studentGradingSummaryData);
	        retryCount = 0;
	      }
	      catch (Exception e) {
	        log.warn("problem saving studentGradingSummaryData: "+e.getMessage());
	        retryCount = persistenceHelper.retryDeadlock(e, retryCount);
	      }
	    }
	  }
  
  public int getLateSubmissionsNumberByAgentId(final Long publishedAssessmentId, final String agentIdString, final Date dueDate) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? and a.submittedDate>?");
	    		q.setLong(0, publishedAssessmentId);
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		q.setDate(3, dueDate);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    return assessmentGradings.size();
  }

  public List getAllOrderedSubmissions(final String publishedId)
  {
      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		Query q = session.createQuery("from AssessmentGradingData a " +
      				"where a.publishedAssessmentId=? and (a.forGrade=? or (a.forGrade=? and a.status=? and a.finalScore <> 0)) " +
			        "order by a.agentId ASC, a.submittedDate");
      		q.setLong(0, Long.parseLong(publishedId));
      		q.setBoolean(1, true);
      		q.setBoolean(2, false);
    		q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
      		return q.list();
      	};
      };
      return getHibernateTemplate().executeFind(hcb);
  }
  
  
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String rationaleString, String itemGradingCommentsString, Map useridMap, String responseCommentString) {
	  ArrayList dataList = new ArrayList();
	  ArrayList headerList = new ArrayList();
	  ArrayList finalList = new ArrayList(2);
	  PublishedAssessmentService pubService = new PublishedAssessmentService();
	  
	  HashSet publishedAssessmentSections = pubService.getSectionSetForAssessment(Long.valueOf(publishedAssessmentId));
	  Double zeroDouble = 0.0;
	  HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(pubService.getPublishedAssessment(publishedAssessmentId));
	  HashMap publishedItemTextHash = pubService.preparePublishedItemTextHash(pubService.getPublishedAssessment(publishedAssessmentId));
	  HashMap publishedItemHash = pubService.preparePublishedItemHash(pubService.getPublishedAssessment(publishedAssessmentId));

      //Get this sorted to add the blank gradings for the questions not answered later.
      Set publishItemSet = new TreeSet(new ItemComparator());
      publishItemSet.addAll(publishedItemHash.values());
          
	  int numSubmission = 1;
	  String numSubmissionText;
	  String lastAgentId = "";
	  String agentEid = "";
	  String firstName = "";
	  String lastName = "";
	  Set useridSet = new HashSet(useridMap.keySet());
	  ArrayList responseList;
	  boolean canBeExported;
	  boolean fistItemGradingData = true;
	  List list = getAllOrderedSubmissions(publishedAssessmentId);
	  Iterator assessmentGradingIter = list.iterator();	  
	  while(assessmentGradingIter.hasNext()) {
		  
		  // create new section-item-scores structure for this assessmentGrading
		  Iterator sectionsIter = publishedAssessmentSections.iterator();
		  HashMap sectionItems = new HashMap();
		  TreeMap sectionScores = new TreeMap();
		  while (sectionsIter.hasNext()) {
			  PublishedSectionData publishedSection = (PublishedSectionData) sectionsIter.next();
			  ArrayList itemsArray = publishedSection.getItemArraySortedForGrading();
			  Iterator itemsIter = itemsArray.iterator();
			  // Iterate through the assessment questions (items)
			  HashMap itemsForSection = new HashMap();
			  while (itemsIter.hasNext()) {
				ItemDataIfc item = (ItemDataIfc) itemsIter.next();
				itemsForSection.put(item.getItemId(), item.getItemId());
			  }
			  sectionItems.put(publishedSection.getSequence(), itemsForSection);
			  sectionScores.put(publishedSection.getSequence(), zeroDouble);
		  }
		  
		  AssessmentGradingData assessmentGradingData = (AssessmentGradingData) assessmentGradingIter.next();
		  String agentId = assessmentGradingData.getAgentId();
		  responseList = new ArrayList();
		  canBeExported = false;
		  if (anonymous) {
			  canBeExported = true;
			  responseList.add(assessmentGradingData.getAssessmentGradingId());
		  }
		  else {
			  if (useridMap.containsKey(assessmentGradingData.getAgentId())) {
				  useridSet.remove(assessmentGradingData.getAgentId());
				  canBeExported = true;
				  try {
					  agentEid = userDirectoryService.getUser(assessmentGradingData.getAgentId()).getEid();
					  firstName = userDirectoryService.getUser(assessmentGradingData.getAgentId()).getFirstName();
					  lastName = userDirectoryService.getUser(assessmentGradingData.getAgentId()).getLastName();
				  } catch (Exception e) {
					  log.error("Cannot get user");
				  }
				  responseList.add(lastName);
				  responseList.add(firstName);
				  responseList.add(agentEid);
				  if (assessmentGradingData.getForGrade()) {
					  if (lastAgentId.equals(agentId)) {
						  numSubmission++;
					  }
					  else {
						  numSubmission = 1;
						  lastAgentId = agentId;
					  }
				  }
				  else {
					  numSubmission = 0;
					  lastAgentId = agentId;
				  }
				  if (numSubmission == 0) {
					  numSubmissionText = noSubmissionMessage;
				  }
				  else {
					  numSubmissionText = String.valueOf(numSubmission);
				  }
				  responseList.add(numSubmissionText);
			  }
		  }

		  if (canBeExported) {
			  int sectionScoreColumnStart = responseList.size();
			  if (showPartAndTotalScoreSpreadsheetColumns) {
				  Double finalScore = assessmentGradingData.getFinalScore();
				  if (finalScore != null) {
                      responseList.add(finalScore); // gopal - cast for spreadsheet numerics
				  } else {
					  log.debug("finalScore is NULL");
					  responseList.add(0d); 
				  } 
			  }

			  String assessmentGradingComments = "";
			  if (assessmentGradingData.getComments() != null) {
				  assessmentGradingComments = assessmentGradingData.getComments().replaceAll("<br\\s*/>", "");
			  }
			  responseList.add(assessmentGradingComments);
			  
			  Long assessmentGradingId = assessmentGradingData.getAssessmentGradingId();

			  HashMap studentGradingMap = getStudentGradingData(assessmentGradingData.getAssessmentGradingId().toString(), false);
			  ArrayList grades = new ArrayList();
			  grades.addAll(studentGradingMap.values());

			  Collections.sort(grades, new QuestionComparator(publishedItemHash));

              //Add the blank gradings for the questions not answered in random pools.
              if(grades.size() < publishItemSet.size()){
              	int index = -1;
                for(Object pido: publishItemSet){
                	index++;
                    PublishedItemData pid = (PublishedItemData)pido;
                    if(index == grades.size() ||
                                ((ItemGradingData)((List)grades.get(index)).get(0)).getPublishedItemId().longValue() != pid.getItemId().longValue()){
						//have to add the placeholder
                        List newList = new ArrayList();
                        newList.add(new EmptyItemGrading(pid.getSection().getSequence(), pid.getItemId(), pid.getSequence()));
                        grades.add(index, newList);
                    }
                }
              }
                          
			  int questionNumber = 0;
			  for (Object oo: grades) {	   
				  // There can be more than one answer to a question, e.g. for
				  // FIB with more than one blank or matching questions. So sort
				  // by sequence number of answer. (don't bother to sort if just 1)

				  List l = (List)oo;
				  if (l.size() > 1)
					  Collections.sort(l, new AnswerComparator(publishedAnswerHash));

				  String maintext = "";
				  String rationale = "";
				  String responseComment= "";
					  
				  boolean addRationale = false;
				  boolean addResponseComment = false;

				  boolean matrixChoices = false;
				  TreeMap responsesMap = new TreeMap();
				  // loop over answers per question
				  int count = 0;
				  ItemGradingData grade = null;
				  //boolean isAudioFileUpload = false;
				  boolean isFinFib = false;

				  double itemScore = 0.0d;

                  //Add the missing sequences!
                                  //To manage emi answers, could help with others too
                                  Map<Long, String> emiAnswerText = new TreeMap<>();
				  for (Object ooo: l) {
					  grade = (ItemGradingData)ooo;
					  if (grade == null || EmptyItemGrading.class.isInstance(grade)) {
						  continue;
					  }
					  if (grade.getAutoScore()!=null) {
						  itemScore += grade.getAutoScore();
					  }

					  // now print answer data
					  log.debug("<br> "+ grade.getPublishedItemId() + " " + grade.getRationale() + " " + grade.getAnswerText() + " " + grade.getComments() + " " + grade.getReview());
					  Long publishedItemId = grade.getPublishedItemId();	    		   
					  ItemDataIfc publishedItemData = (ItemDataIfc) publishedItemHash.get(publishedItemId);
					  Long typeId = publishedItemData.getTypeId();
                      questionNumber = publishedItemData.getSequence();
					  if (typeId.equals(TypeIfc.FILL_IN_BLANK) || typeId.equals(TypeIfc.FILL_IN_NUMERIC) || typeId.equals(TypeIfc.CALCULATED_QUESTION)) {
						  log.debug("FILL_IN_BLANK, FILL_IN_NUMERIC");
						  isFinFib = true;
						  String thistext;

						  Long answerid = grade.getPublishedAnswerId();
						  Long sequence = null;
						  if (answerid != null) {
							  AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
							  if (answer != null) {
							      sequence = answer.getSequence();
							  }
						  }

						  String temptext = grade.getAnswerText();
						  if (temptext == null) {
							  temptext = "No Answer";
						  }
						  thistext = sequence + ": " + temptext;

                                                  if (count == 0)
							  maintext = thistext;
						  else
							  maintext = maintext + "|" + thistext;

						  count++;
					  }
					  else if (typeId.equals(TypeIfc.MATCHING)) {
						  log.debug("MATCHING");
						  String thistext;

						  // for some question types we have another text field
						  Long answerid = grade.getPublishedAnswerId();
						  String temptext = "No Answer";
						  Long sequence = null;
						  if (answerid != null) {
							  AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
							  if(answer != null) {
								temptext = answer.getText();
							  	if (temptext == null) {
								  temptext = "No Answer";
							  	}
							  	sequence = answer.getItemText().getSequence();
							  }	else if(answerid == -1) {
								  temptext = "None of the Above";
								  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
								  sequence = itemTextIfc.getSequence();
							  }
						  }
						  else {
							  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
							  sequence = itemTextIfc.getSequence();
						  }
						  thistext = sequence + ": " + temptext;

						  if (count == 0)
							  maintext = thistext;
						  else
							  maintext = maintext + "|" + thistext;

						  count++;
					  }
					  else if (typeId.equals(TypeIfc.IMAGEMAP_QUESTION)) {
						  log.debug("MATCHING");
						  
						  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
						  Long sequence = itemTextIfc.getSequence();
						  String temptext = (grade.getIsCorrect()) ? "OK" : "No OK";
						  
						  String thistext = sequence + ": " + temptext;

						  if (count == 0)
							  maintext = thistext;
						  else
							  maintext = maintext + "|" + thistext;

						  count++;
					  }
					  else if (typeId.equals(TypeIfc.IMAGEMAP_QUESTION)) {
						  log.debug("MATCHING");
						  
						  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
						  Long sequence = itemTextIfc.getSequence();
						  String temptext = (grade.getIsCorrect()) ? "OK" : "No OK";
						  
						  String thistext = sequence + ": " + temptext;

						  if (count == 0)
							  maintext = thistext;
						  else
							  maintext = maintext + "|" + thistext;

						  count++;
					  }
					  else if (typeId.equals(TypeIfc.IMAGEMAP_QUESTION)) {
						  log.debug("MATCHING");
						  
						  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
						  Long sequence = itemTextIfc.getSequence();
						  String temptext = (grade.getIsCorrect()) ? "OK" : "No OK";
						  
						  String thistext = sequence + ": " + temptext;

						  if (count == 0)
							  maintext = thistext;
						  else
							  maintext = maintext + "|" + thistext;

						  count++;
					  }
					  else if (typeId.equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) { 
						  log.debug("EXTENDED_MATCHING_ITEMS");
						  String thistext;

						  // for some question types we have another text field
						  Long answerid = grade.getPublishedAnswerId();
						  String temptext = "No Answer";
						  Long sequence = null;

						  if (answerid != null) {
							  AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
							  if (answer != null) {
							  	temptext = answer.getLabel();
							  	if (temptext == null) {
									  temptext = "No Answer";
							  	}
							  	sequence = answer.getItemText().getSequence();
							  }
						  }

						  if (sequence == null) {
							  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
							  if (itemTextIfc != null) {
							  	sequence = itemTextIfc.getSequence();
							  }
						  }

						  if (sequence != null) {
                              			  	thistext = emiAnswerText.get(sequence);
                              			  	if(thistext == null){
                                 				thistext = temptext;
                              			  	}else{
                              			 		thistext = thistext + temptext;
                              			  	}
                              			  	emiAnswerText.put(sequence, thistext);
						  } else {
							// Orphaned answer: the answer item to which it refers was removed after the assessment was taken,
							// as a result of editing the published assessment. This behaviour should be fixed, i.e. it should
							// not be possible to get orphaned answer item references in the database.
							sequence = new Long(99);
							emiAnswerText.put(sequence, "Item Removed");
						  }
					  }
					  else if (typeId.equals(TypeIfc.MATRIX_CHOICES_SURVEY)) {
						  log.debug("MATRIX_CHOICES_SURVEY");
						  // for this kind of question a responsesMap is generated
						  matrixChoices = true;
						  Long answerid = grade.getPublishedAnswerId();
						  String temptext;
						  Long sequence;
						  if (answerid != null) {
							  AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
							  temptext = answer.getText();
							  if (temptext == null) {
								  temptext = "No Answer";
								  }
							  sequence = answer.getItemText().getSequence();
							  }
						  else {
							  ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
							  sequence = itemTextIfc.getSequence();
							  log.debug("Answerid null for "+grade.getPublishedItemId()+". Adding "+sequence);
							  temptext = "No Answer";
							  }
						  responsesMap.put(sequence,temptext);                                                 
					  }
					  else if (typeId.equals(TypeIfc.AUDIO_RECORDING)) {
						  log.debug("AUDIO_RECORDING");
						  maintext = audioMessage;
						  //isAudioFileUpload = true;    			  
					  }
					  else if (typeId.equals(TypeIfc.FILE_UPLOAD)) {
						  log.debug("FILE_UPLOAD");
						  maintext = fileUploadMessage;
						  //isAudioFileUpload = true;
					  }
					  else if (typeId.equals(TypeIfc.ESSAY_QUESTION) ) {
						  log.debug("ESSAY_QUESTION");
						  if (grade.getAnswerText() != null) {
							  maintext = grade.getAnswerText();
						  }
					  }
					  else {
						  log.debug("other type");
						  String thistext = "";

						  // for some question types we have another text field
						  Long answerid = grade.getPublishedAnswerId();
						  if (answerid != null) {
							  AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
							  if (answer != null) { 
								  String temptext = answer.getText();
								  if (temptext != null)
									  thistext = temptext;
							  }
							  else {
								  log.warn("Published answer for " + answerid + " is null");
							  } 			
						  }

						  if (count == 0)
							  maintext = thistext;
						  else
							  maintext = maintext + "|" + thistext;

						  count++;
					  }

					  // taking care of rationale
					  if (!addRationale && (typeId.equals(TypeIfc.MULTIPLE_CHOICE) || typeId.equals(TypeIfc.MULTIPLE_CORRECT) || typeId.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) || typeId.equals(TypeIfc.TRUE_FALSE))) {
						  log.debug("MULTIPLE_CHOICE or MULTIPLE_CORRECT or MULTIPLE_CORRECT_SINGLE_SELECTION or TRUE_FALSE");
						  if (publishedItemData.getHasRationale() != null && publishedItemData.getHasRationale() ) { 
							  addRationale = true;
							  rationale = grade.getRationale();
							  if (rationale == null) {
								  rationale = "";
							  }
						  }
					  }
					  
					  //Survey - Matrix of Choices - Add Comment Field
					  if (typeId.equals(TypeIfc.MATRIX_CHOICES_SURVEY)) {
						  PublishedItemData pid = (PublishedItemData) publishedItemData;
						  if (pid.getAddCommentFlag()) {
							  addResponseComment = true;
							  if (responseComment.equals("") && grade.getAnswerText() != null) {
								  responseComment = grade.getAnswerText();
							  }
						  }
					  }
				  } // inner for - answers


                                  if(!emiAnswerText.isEmpty()){
                                    if(maintext == null){
                                        maintext = "";
                                    }
                                    for(Entry<Long, String> entry: emiAnswerText.entrySet()){
                                        maintext = maintext + "|" + entry.getKey().toString() + ":" + entry.getValue();
                                    }
                                    if(maintext.startsWith("|")){
                                        maintext = maintext.substring(1);
                                    }
                                  }
                  Integer sectionSequenceNumber;
                  if(grade == null || EmptyItemGrading.class.isInstance(grade)){
                  	sectionSequenceNumber = EmptyItemGrading.class.cast(grade).getSectionSequence();
                    questionNumber = EmptyItemGrading.class.cast(grade).getItemSequence();
                  	// indicate that the student was not presented with this question
                    maintext = "-";
                  }else{
                  	sectionSequenceNumber = updateSectionScore(sectionItems, sectionScores, grade.getPublishedItemId(), itemScore);
                  }
                  
				  if (isFinFib && maintext.contains( "No Answer" ) && count == 1) {
					  maintext = "No Answer";
				  }
				  else if ("".equals(maintext)) {
					  maintext = "No Answer";
				  }
				  String itemGradingComments = "";
				  // if question type is not matrix choices apply the original code
				  if (!matrixChoices) {
					  responseList.add(maintext);
					  if (grade.getComments() != null) {
						  itemGradingComments = grade.getComments().replaceAll("<br\\s*/>", "");
						  }
					  responseList.add(itemGradingComments);                          
					  }
				  else {
					  // if there are questions not answered, a no answer response is added to the map
					  ItemDataIfc correspondingPublishedItemData = (ItemDataIfc) publishedItemHash.get(grade.getPublishedItemId());
					  List correspondingItemTextArray = correspondingPublishedItemData.getItemTextArray();
					  log.debug("publishedItem is "+correspondingPublishedItemData.getText()+" and number of rows "+correspondingItemTextArray.size());
					  if (responsesMap.size() < correspondingItemTextArray.size()) {
						  Iterator itItemTextHash = correspondingItemTextArray.iterator();
						  while (itItemTextHash.hasNext()) {
							  ItemTextIfc itemTextIfc = (ItemTextIfc) itItemTextHash.next();
							  if (!responsesMap.containsKey(itemTextIfc.getSequence())) {
								  log.debug("responsesMap does not contain answer to "+itemTextIfc.getText());
								  responsesMap.put(itemTextIfc.getSequence(),"No Answer");
								  }
							  }
						  }
					  Iterator it = responsesMap.entrySet().iterator();
					  while(it.hasNext()){
						  Map.Entry e = (Map.Entry)it.next();
						  log.debug("Adding to response list "+e.getKey()+" and "+e.getValue());
						  responseList.add(e.getValue());
						  if (grade.getComments() != null) {
							  itemGradingComments = grade.getComments().replaceAll("<br\\s*/>", "");
							  }
						  responseList.add(itemGradingComments);
						  itemGradingComments = "";
						  }
					  }
				  
				  if (addRationale) {
					  responseList.add(rationale);
				  }
				  
				  if (addResponseComment) {
					  responseList.add(responseComment);
				  }

				  // Only set header based on the first item grading data
				  if (fistItemGradingData) {
                  	//get the pool name
                    String poolName = null;
                    for(Iterator i = publishedAssessmentSections.iterator(); i.hasNext();){
                    	PublishedSectionData psd = (PublishedSectionData)i.next();
                        if(psd.getSequence().intValue() == sectionSequenceNumber){
                        	poolName = psd.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
                        }
                    }
                    if (!matrixChoices) {
                    	headerList.add(makeHeader(partString, sectionSequenceNumber, questionString, textString, questionNumber, poolString, poolName));
                    	if (addRationale) {
                    		headerList.add(makeHeader(partString, sectionSequenceNumber, questionString, rationaleString, questionNumber, poolString, poolName));
                    		}
                    	if (addResponseComment) {
                    		headerList.add(makeHeader(partString, sectionSequenceNumber, questionString, responseCommentString, questionNumber, poolString, poolName));
                    		}
                    	headerList.add(makeHeader(partString, sectionSequenceNumber, questionString, itemGradingCommentsString, questionNumber, poolString, poolName));
                    	}
                    else {
                    	int numberRows = responsesMap.size();
                    	for(int i = 0; i < numberRows; i = i+1) {
                    		headerList.add(makeHeaderMatrix(partString, sectionSequenceNumber, questionString, textString, questionNumber, i+1, poolString, poolName));
                    		if (addRationale) {
                    			headerList.add(makeHeaderMatrix(partString, sectionSequenceNumber, questionString, rationaleString, questionNumber, i+1, poolString, poolName));
                    			}
                    		if (addResponseComment) {
                    			headerList.add(makeHeaderMatrix(partString, sectionSequenceNumber, questionString, responseCommentString, questionNumber, i+1, poolString, poolName));
                    			}
                    		headerList.add(makeHeaderMatrix(partString, sectionSequenceNumber, questionString, itemGradingCommentsString, questionNumber, i+1, poolString, poolName));
                    		}
                    	}
				  }	    		   
			  } // outer for - questions

			  if (showPartAndTotalScoreSpreadsheetColumns) {
				  if (sectionScores.size() > 1) {
					  Iterator keys = sectionScores.keySet().iterator();
					  while (keys.hasNext()) {
						  Double partScore = (Double) (sectionScores.get(keys.next()));
						  responseList.add(sectionScoreColumnStart++, partScore);
					  }
				  }
			  }

			  dataList.add(responseList);

			  if (fistItemGradingData) {
				  fistItemGradingData = false;
			  }
		  }
	  } // while

	  if (!anonymous && !useridSet.isEmpty()) {
		  Iterator iter = useridSet.iterator();
		  while (iter.hasNext()) {
			  String id = (String) iter.next();
			  try {
				  agentEid = userDirectoryService.getUser(id).getEid();
				  firstName = userDirectoryService.getUser(id).getFirstName();
				  lastName = userDirectoryService.getUser(id).getLastName();
			  } catch (Exception e) {
				  log.error("Cannot get user");
			  }
			  responseList = new ArrayList();
			  responseList.add(lastName);
			  responseList.add(firstName);
			  responseList.add(agentEid);
			  responseList.add(noSubmissionMessage);
			  dataList.add(responseList);
		  }
	  }
	  Collections.sort(dataList, new ResponsesComparator(anonymous));
	  finalList.add(dataList);
	  finalList.add(headerList);
	  return finalList;
  }
  
  
  /**
   * @param sectionItems
   * @param sectionScores
   * @param grade
   * @return The section sequence number, or zero if the section is not found(unlikely)
   */
  private int updateSectionScore(HashMap sectionItems, TreeMap sectionScores, Long publishedItemId, double itemScore) {

	  for (Iterator it = sectionItems.entrySet().iterator(); it.hasNext();) {
		  Map.Entry entry = (Map.Entry) it.next();
		  Object sectionSequence = entry.getKey();
		  HashMap itemsForSection = (HashMap) entry.getValue();

		  if (itemsForSection.get(publishedItemId)!=null) {
			  Double score = ((Double)sectionScores.get(sectionSequence)) + itemScore;
			  sectionScores.put(sectionSequence, score);
                          return ((Integer)sectionSequence);
		  }
	  }
          return 0;
  }
  
  
  
    /*
	 sort answers by sequence number within question if one is defined
	 normally it will be, but use id number if not
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published answer
	 */
  private static class AnswerComparator implements Comparator {

		HashMap publishedAnswerHash;

		public AnswerComparator(HashMap m) {
			publishedAnswerHash = m;
		}

		public int compare(Object a, Object b) {
			ItemGradingData agrade = (ItemGradingData) a;
			ItemGradingData bgrade = (ItemGradingData) b;

			Long aindex = agrade.getItemGradingId();
			Long bindex = bgrade.getItemGradingId();

			Long aanswerid = agrade.getPublishedAnswerId();
			Long banswerid = bgrade.getPublishedAnswerId();

			AnswerIfc aanswer;
			AnswerIfc banswer;
			
			if (aanswerid != null && banswerid != null) {
				aanswer = (AnswerIfc) publishedAnswerHash
						.get(aanswerid);
				banswer = (AnswerIfc) publishedAnswerHash
						.get(banswerid);

				if (aanswer == null || banswer == null) {
					return (aanswer == null ? -1: 1);
				}
				else {
					//For EMI, use this test
					if (aanswer.getItem() != null &&
							TypeIfc.EXTENDED_MATCHING_ITEMS.equals(aanswer.getItem().getTypeId()) &&
							banswer.getItem() != null &&
							TypeIfc.EXTENDED_MATCHING_ITEMS.equals(banswer.getItem().getTypeId())) {
						Long aTextSeq = aanswer.getItemText().getSequence();
						Long bTextSeq = banswer.getItemText().getSequence();
						if (!aTextSeq.equals(bTextSeq)) {
							return aTextSeq.compareTo(bTextSeq);
						}
						else {
							return aanswer.getLabel().compareToIgnoreCase(banswer.getLabel());
						}
					}
					
					aindex = aanswer.getSequence();
					bindex = banswer.getSequence();
				}
			}
			
			if (aindex < bindex){
				return -1;
			}else if (aindex > bindex){
				return 1;
			}else{
				return 0;
			}
		}
	}

	/*
	 sort questions in same order presented to users
	 first by section then by question within section
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published question
	 */
	private static class QuestionComparator implements Comparator {

		HashMap publishedItemHash;

		public QuestionComparator(HashMap m) {
			publishedItemHash = m;
		}

		public int compare(Object a, Object b) {
			ItemGradingData agrade = (ItemGradingData) ((List) a).get(0);
			ItemGradingData bgrade = (ItemGradingData) ((List) b).get(0);

			ItemDataIfc aitem = (ItemDataIfc) publishedItemHash.get(agrade
					.getPublishedItemId());
			ItemDataIfc bitem = (ItemDataIfc) publishedItemHash.get(bgrade
					.getPublishedItemId());

			Integer asectionseq = aitem.getSection().getSequence();
			Integer bsectionseq = bitem.getSection().getSequence();

			if (asectionseq < bsectionseq)
				return -1;
			else if (asectionseq > bsectionseq)
				return 1;

			Integer aitemseq = aitem.getSequence();
			Integer bitemseq = bitem.getSequence();

			if (aitemseq < bitemseq)
				return -1;
			else if (aitemseq > bitemseq)
				return 1;
			else
				return 0;

		}
	}
	
	/*
	 sort questions in same order presented to users
	 first by section then by question within section
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published question
	 */
	private static class ResponsesComparator implements Comparator {
		boolean anonymous;
		private static final Logger log = LoggerFactory.getLogger(ResponsesComparator.class);
		
		public ResponsesComparator(boolean anony) {
			anonymous = anony;
		}

		public int compare(Object a, Object b) {
			RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
			try{
				RuleBasedCollator collator = new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
				// For anonymous, it should return after the first element comparison
				if (anonymous) {
					Long aFirstElement = (Long) ((ArrayList) a).get(0);
					Long bFirstElement = (Long) ((ArrayList) b).get(0);
					if (aFirstElement.compareTo(bFirstElement) < 0)
						return -1;
					else if (aFirstElement.compareTo(bFirstElement) > 0)
						return 1;
					else
						return 0;
				}
				// For non-anonymous, it compares last names first, if it is the same,
				// compares first name, and then Eid
				else {
					String aFirstElement = (String) ((ArrayList) a).get(0);
					String bFirstElement = (String) ((ArrayList) b).get(0);
					if (collator.compare(aFirstElement, bFirstElement) < 0)
						return -1;
					else if (collator.compare(aFirstElement, bFirstElement) > 0)
						return 1;
					else {
						String aSecondElement = (String) ((ArrayList) a).get(1);
						String bSecondElement = (String) ((ArrayList) b).get(1);
						if (collator.compare(aSecondElement,bSecondElement) < 0)
							return -1;
						else if (collator.compare(aSecondElement,bSecondElement) > 0)
							return 1;
						else {
							String aThirdElement = (String) ((ArrayList) a).get(2);
							String bThirdElement = (String) ((ArrayList) b).get(2);
							if (collator.compare(aThirdElement,bThirdElement) < 0)
								return -1;
							else if (collator.compare(aThirdElement,bThirdElement) > 0)
								return 1;
						}
					}
					return 0;
				}
			} catch (ParseException e) {
	  			log.error("ERROR compare: ",e);
	  		}
			return Collator.getInstance().compare(a, b);	
		}
	}

        /**
         * A comparator to sort the items first by section sequence
         * and then by item sequence.
         */
        private static class ItemComparator implements Comparator {

            public int compare(Object o1, Object o2) {
                PublishedItemData a = (PublishedItemData) o1;
                PublishedItemData b = (PublishedItemData) o2;
                if (a.getSection().getSequence() < b.getSection().getSequence()) {
                    return -1;
                } else if (a.getSection().getSequence() > b.getSection().getSequence()) {
                    return 1;
                } else {
                    return a.getSequence() - b.getSequence();
                }
            }
        }

	  public void removeUnsubmittedAssessmentGradingData(final AssessmentGradingData data) {
		    final HibernateCallback hcb = new HibernateCallback(){
		    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
		    		Query q = session.createQuery(
		    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? " +
		    				"and a.forGrade=? and a.status=? " +
		    				"order by a.submittedDate desc");
		    		q.setLong(0, data.getPublishedAssessmentId());
		    		q.setString(1, data.getAgentId());
		    		q.setBoolean(2, false);
		    		q.setInteger(3, AssessmentGradingData.NO_SUBMISSION);
		    		return q.list();
		    	};
		    };
		    List assessmentGradings = getHibernateTemplate().executeFind(hcb);
		    if (!assessmentGradings.isEmpty()) { 
		    	deleteAll(assessmentGradings);
		    }
	  }

  public boolean getHasGradingData(final Long publishedAssessmentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? ");
	    		q.setLong(0, publishedAssessmentId);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);
	    return !assessmentGradings.isEmpty();
  }
  
  public ArrayList getHasGradingDataAndHasSubmission(final Long publishedAssessmentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? order by a.agentId asc, a.submittedDate desc");
	    		q.setLong(0, publishedAssessmentId);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);
	    // first element represents hasGradingData
	    // second element represents hasSubmission
	    ArrayList<Boolean> al = new ArrayList<>(); 
	    if (assessmentGradings.size() ==0) {
	    	al.add(Boolean.FALSE); // no gradingData
	    	al.add(Boolean.FALSE); // no submission
	    }
	    else {
	    	al.add(Boolean.TRUE); // yes gradingData
	    	String currentAgent = "";
	    	Iterator iter = assessmentGradings.iterator();
	    	boolean hasSubmission = false;
			while (iter.hasNext()) {
				AssessmentGradingData adata = (AssessmentGradingData) iter.next();
				if (!currentAgent.equals(adata.getAgentId())){
					if (adata.getForGrade()) {
						al.add(Boolean.TRUE); // has submission
						hasSubmission = true;
						break;
					}
					currentAgent = adata.getAgentId();
				}
			}
	    	if (!hasSubmission) {
	    		al.add(Boolean.FALSE);// no submission
	    	}
	    }
	    return al;
  }
	
	
	
	public String getFilename(Long itemGradingId, String agentId, String filename) {
		int dotIndex = filename.lastIndexOf(".");
   	    if (dotIndex < 0) {
   	    	return getFilenameWOExtesion(itemGradingId, agentId, filename);
   	    }
   	    else {
   	    	return getFilenameWExtesion(itemGradingId, agentId, filename, dotIndex);
   	    }
	}

	private String getFilenameWOExtesion(Long itemGradingId, String agentId, String filename) {
		StringBuilder bindVar = new StringBuilder(filename);
		bindVar.append("%");
		
   	    Object [] values = {itemGradingId, agentId, bindVar.toString()};
	    List list = getHibernateTemplate().find(
	    		"select filename from MediaData m where m.itemGradingData.itemGradingId=? and m.createdBy=? and m.filename like ?", values);
   	    if (list.isEmpty()) {
	    	return filename;
	    }
   	    
   	    HashSet hs = new HashSet();
   	    Iterator iter = list.iterator();
   	    String name;
   	    // Only add the filename which
   	    // 1. with no extension because the newly updated one has no extention
   	    // 2. name is same to filename or name like filename(...
   	    // For example, if the filename is ab. We only want ab, ab(1), ab(2)... and don't want abc to be in
   	    while(iter.hasNext()) {
   	    	name = ((String) iter.next()).trim();
   	    	if (!name.contains( "." ) && (name.equals(filename) || name.startsWith(filename + "("))) {
   	    		hs.add(name);
   	    	}
   	    }
   	    
   	    if (hs.isEmpty()) {
   	    	return filename;
   	    }
   	    
   	    StringBuilder testName = new StringBuilder(filename);
	    int i = 1;
	    while(true) {
	    	if (!hs.contains(testName.toString())) {
	    		return testName.toString();
	    	}
	    	else {
	    		i++;
	    		testName = new StringBuilder(filename);
	    	    testName.append("(");
	    		testName.append(i);
	    		testName.append(")");
	    	}
	    }
	}
	
	private String getFilenameWExtesion(Long itemGradingId, String agentId, String filename, int dotIndex) {
		String filenameWithoutExtension = filename.substring(0, dotIndex);
		StringBuilder bindVar = new StringBuilder(filenameWithoutExtension);
		bindVar.append("%");
		bindVar.append(filename.substring(dotIndex));
   	       	    
		Object [] values = {itemGradingId, agentId, bindVar.toString()};
	    List list = getHibernateTemplate().find(
	    		"select filename from MediaData m where m.itemGradingData.itemGradingId=? and m.createdBy=? and m.filename like ?", values);
   	    if (list.isEmpty()) {
	    	return filename;
	    }
   	    
   	    HashSet hs = new HashSet();
   	    Iterator iter = list.iterator();
   	    String name;
   	    int nameLenght;
   	    String extension = filename.substring(dotIndex);
   	    int extensionLength = extension.length();
   	    while(iter.hasNext()) {
   	    	name = ((String) iter.next()).trim();
   	    	if ((name.equals(filename) || name.startsWith(filenameWithoutExtension + "("))) {
   	    		nameLenght = name.length();
   	    		hs.add(name.substring(0, nameLenght - extensionLength));
   	    	}
   	    }
   	    
   	    if (hs.isEmpty()) {
   	    	return filename;
   	    }
   	    
	    StringBuffer testName = new StringBuffer(filenameWithoutExtension);
	    int i = 1;
   	    while(true) {
	    	if (!hs.contains(testName.toString())) {
	    		testName.append(extension);
	    		return testName.toString();
	    	}
	    	else {
	    		i++;
	    		testName = new StringBuffer(filenameWithoutExtension);
	    	    testName.append("(");
	    		testName.append(i);
	    		testName.append(")");
	    	}
   	    }
	}
	
	public List getUpdatedAssessmentList(String agentId, String siteId) {
		ArrayList finalList = new ArrayList();
		ArrayList updatedAssessmentList = new ArrayList();
		ArrayList updatedAssessmentNeedResubmitListList = new ArrayList();
		
		Object[] values = { agentId, siteId, "OWN_PUBLISHED_ASSESSMENT", false, AssessmentGradingData.ASSESSMENT_UPDATED, AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT};

		List list = getHibernateTemplate()
				.find("select a.publishedAssessmentId, a.status from AssessmentGradingData a, AuthorizationData az " +
						" where a.agentId=? and az.agentIdString=? and az.functionId=? " + 
						" and az.qualifierId=a.publishedAssessmentId and a.forGrade=? and (a.status=? or a.status=?) " +
						" order by a.status", values);
		
		if (list.isEmpty()) {
			return updatedAssessmentList;
		}

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next();
			if (AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT.compareTo((Integer) o[1]) == 0) {
				updatedAssessmentNeedResubmitListList.add(o[0]);
			}
			else {
				updatedAssessmentList.add(o[0]);
			}
		}
		finalList.add(updatedAssessmentNeedResubmitListList);
		finalList.add(updatedAssessmentList);
		return finalList;
	}
	
	public List getSiteNeedResubmitList(String siteId) {
		Object [] values = {"OWN_PUBLISHED_ASSESSMENT", siteId, false, 4};

		List list = getHibernateTemplate()
				.find("select distinct a.publishedAssessmentId from AssessmentGradingData a, AuthorizationData au " +
						"where au.functionId = ? and au.agentIdString = ? and a.publishedAssessmentId = au.qualifierId " +
						"and a.forGrade=? and a.status=? ", values);
		return list;
	}
	
	public void autoSubmitAssessments() {
        String hql = "select new AssessmentGradingData(a.assessmentGradingId, a.publishedAssessmentId, " +
                "a.agentId, a.submittedDate, a.isLate, a.forGrade, a.totalAutoScore, a.totalOverrideScore, " +
                "a.finalScore, a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate, a.timeElapsed) " +
                "from AssessmentGradingData a, PublishedAccessControl c " +
                "where a.publishedAssessmentId = c.assessment.publishedAssessmentId " +
                "and c.retractDate <= :now " +
                "and a.status != 5 " +
                "and (a.hasAutoSubmissionRun = 0 or a.hasAutoSubmissionRun is null) " +
                "and c.autoSubmit = 1 " +
                "and a.attemptDate is not null " +
                "and (a.attemptDate <= c.retractDate or (c.dueDate <= :now and c.lateHandling = 2)) " +
                "order by a.publishedAssessmentId, a.agentId, a.forGrade desc, a.assessmentGradingId";

        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();

        List<AssessmentGradingData> list = session.createQuery(hql).setTimestamp("now", new Date()).list();
        log.info("AutoSubmit found {} submissions to process", list.size());

	    Iterator iter = list.iterator();
	    String lastAgentId = "";
	    Long lastPublishedAssessmentId = Long.valueOf(0);
	    AssessmentGradingData adata = null;
	    HashMap sectionSetMap = new HashMap();
	    
	    // SAM-1088 getting the assessment so we can check to see if last user attempt was after due date
	    PublishedAssessmentFacade assessment = null;
	    
	    EventLogService eventService = new EventLogService();
	    EventLogFacade eventLogFacade = new EventLogFacade();
	    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	    
		GradebookExternalAssessmentService g = null;
		boolean updateGrades = false;
		HashMap toGradebookPublishedAssessmentSiteIdMap = null;
		GradebookServiceHelper gbsHelper = null;
		if (IntegrationContextFactory.getInstance() != null) {
			boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
			if (integrated) {
				g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
			}
			toGradebookPublishedAssessmentSiteIdMap = publishedAssessmentService.getToGradebookPublishedAssessmentSiteIdMap();
			gbsHelper = IntegrationContextFactory.getInstance().getGradebookServiceHelper();
			updateGrades = true;
		}
		boolean updateCurrentGrade;
	    while (iter.hasNext()) {
	    	updateCurrentGrade = false;
            Map<String, Object> notiValues = new HashMap<>();
	    	try{
	    		adata = (AssessmentGradingData) iter.next();
	    		adata.setHasAutoSubmissionRun(Boolean.TRUE);
	    		
				Date endDate = new Date();
				if (Boolean.FALSE.equals(adata.getForGrade())){

						adata.setForGrade(Boolean.TRUE);
						if (adata.getTotalAutoScore() == null) {
								adata.setTotalAutoScore(0d);
						}
						if (adata.getFinalScore() == null) {
								adata.setFinalScore(0d);
						}
						if (adata.getAttemptDate() != null && assessment != null && assessment.getDueDate() != null &&
										adata.getAttemptDate().after(assessment.getDueDate())) {
								adata.setIsLate(true);
						}
						// SAM-1088
						else if (adata.getSubmittedDate() != null && assessment != null && assessment.getDueDate() != null &&
										adata.getSubmittedDate().after(assessment.getDueDate())) {
								adata.setIsLate(true);
						}
						// SAM-2729 user probably opened assessment and then never submitted a question
						if (adata.getSubmittedDate() == null && adata.getAttemptDate() != null) {
								adata.setSubmittedDate(endDate);
						}

    				updateCurrentGrade = true;
    				adata.setIsAutoSubmitted(Boolean.TRUE);
    				if (lastPublishedAssessmentId.equals(adata.getPublishedAssessmentId()) 
    						&& lastAgentId.equals(adata.getAgentId())) {
    					adata.setStatus(AssessmentGradingData.AUTOSUBMIT_UPDATED);

        				// Check: needed updating gradebook
        				// If the assessment is configured with highest score and exists a previous submission with higher score 
        				// this submission doesn't have to be sent to gradebook
        				assessment = (PublishedAssessmentFacade)publishedAssessmentService.getAssessment(adata.getPublishedAssessmentId());
        				
        				if (assessment.getEvaluationModel().getScoringType().equals(EvaluationModel.HIGHEST_SCORE)) {
        					AssessmentGradingData assessmentGrading = 
        							getHighestSubmittedAssessmentGrading(adata.getPublishedAssessmentId(), adata.getAgentId(), null);
        					if (assessmentGrading.getTotalAutoScore() > adata.getTotalAutoScore()) {
        						updateCurrentGrade = false;
        					}
        				}
    				}
    				else {
    					adata.setStatus(AssessmentGradingData.SUBMITTED);
    				}
    				completeItemGradingData(adata, sectionSetMap);

    				List eventLogDataList = eventService.getEventLogData(adata.getAssessmentGradingId());
    				if (!eventLogDataList.isEmpty()) {
    					EventLogData eventLogData= (EventLogData) eventLogDataList.get(0);
    					//will do the i18n issue later.
    					eventLogData.setErrorMsg("No Errors (Auto submit)");
    					eventLogData.setEndDate(endDate);
    					if(eventLogData.getStartDate() != null) {
    						double minute= 1000*60;
    						int eclipseTime = (int)Math.ceil(((endDate.getTime() - eventLogData.getStartDate().getTime())/minute));
    						eventLogData.setEclipseTime(eclipseTime); 
    					} else {
    						eventLogData.setEclipseTime(null); 
    						eventLogData.setErrorMsg("Error during auto submit");
    					}
    					eventLogFacade.setData(eventLogData);
    					eventService.saveOrUpdateEventLog(eventLogFacade);
    				}

    				EventTrackingService.post(EventTrackingService.newEvent("sam.auto-submit.job", 
    						AutoSubmitAssessmentsJob.safeEventLength("publishedAssessmentId=" + adata.getPublishedAssessmentId() + 
    								", assessmentGradingId=" + adata.getAssessmentGradingId()), true));
    				
    				notiValues.put("publishedAssessmentID", adata.getPublishedAssessmentId());
    				notiValues.put("assessmentGradingID", adata.getAssessmentGradingId());
    				notiValues.put("userID", adata.getAgentId());
    				notiValues.put("submissionDate", adata.getSubmittedDate());

    				PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment( adata.getPublishedAssessmentId().toString() );
    				String confirmationNumber = adata.getAssessmentGradingId() + "-" + publishedAssessment.getPublishedAssessmentId() + "-"
    					+ adata.getAgentId() + "-" + adata.getSubmittedDate().toString();
    				notiValues.put( "confirmationNumber", confirmationNumber );

    				EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO, notiValues.toString(), AgentFacade.getCurrentSiteId(), false, SamigoConstants.NOTI_EVENT_ASSESSMENT_SUBMITTED));
    			}

	    		lastPublishedAssessmentId = adata.getPublishedAssessmentId();
    			lastAgentId = adata.getAgentId();

	    		//we only want to save one at a time to help the job continue when there's an error 
    			getHibernateTemplate().saveOrUpdate(adata);
    			//update grades
    			if(updateGrades && updateCurrentGrade && toGradebookPublishedAssessmentSiteIdMap.containsKey(adata.getPublishedAssessmentId())) {
    				String currentSiteId = (String) toGradebookPublishedAssessmentSiteIdMap.get(adata.getPublishedAssessmentId());
    				if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(currentSiteId), g)){
    					int retryCount = persistenceHelper.getRetryCount();
    					while (retryCount > 0){
    						try {
    							Map<String, Double> studentScore = new HashMap<>();
    							studentScore.put(adata.getAgentId(),adata.getFinalScore());
    							gbsHelper.updateExternalAssessmentScores(adata.getPublishedAssessmentId(), studentScore, g);
    							retryCount = 0;
    						}
    						catch (Exception e) {
    							if(adata != null){
    				    			log.error("Error while updating external assessment score during auto submitting assessment grade data id: " + adata.getAssessmentGradingId(), e);
    				    		}else{
    				    			log.error(e.getMessage(), e);
    				    		}
    							retryCount = persistenceHelper.retryDeadlock(e, retryCount);
    						}
    					}
    				}
    			}

    			adata = null;
	    	}catch (Exception e) {
	    		if(adata != null){
	    			log.error("Error while auto submitting assessment grade data id: " + adata.getAssessmentGradingId(), e);
	    		}else{
	    			log.error(e.getMessage(), e);
	    		}
			}
	    }
	    
	}

	private String makeHeader(String section, int sectionNumber, String question, String headerType, int questionNumber, String pool, String poolName) {
		StringBuilder sb = new StringBuilder(section);
                sb.append(" ");
                sb.append(sectionNumber);
                sb.append(", ");
                sb.append(question);
		sb.append(" ");
		sb.append(questionNumber);
		sb.append(", ");
                if(poolName != null){
                    sb.append(pool);
                    sb.append(" ");
                    sb.append(poolName);
                    sb.append(", ");
                }
		sb.append(headerType);
		return sb.toString();
	}
	
	private String makeHeaderMatrix(String section, int sectionNumber, String question, String headerType, int questionNumber, int questionRow, String pool, String poolName) {
		StringBuilder sb = new StringBuilder(section);
		sb.append(" ");
		sb.append(sectionNumber);
		sb.append(", ");
		sb.append(question);
		sb.append(" ");
		sb.append(questionNumber);
		sb.append(": ");
		sb.append(questionRow);
		sb.append(", ");
		if(poolName != null){
			sb.append(pool);
			sb.append(" ");
			sb.append(poolName);
			sb.append(", ");
			}
		sb.append(headerType);
		return sb.toString();
		}
    
	public ItemGradingAttachment createItemGradingtAttachment(ItemGradingData itemGrading, String resourceId, String filename, String protocol) {
		GradingAttachmentData attach = createGradingtAttachment(resourceId, filename, protocol);
		ItemGradingAttachment itemAttach = new ItemGradingAttachment(attach, itemGrading);
		itemAttach.setItemGrading(itemGrading);
		return itemAttach;
	}
	
	public AssessmentGradingAttachment createAssessmentGradingtAttachment(AssessmentGradingData assessmentGrading, String resourceId, String filename, String protocol) {
		GradingAttachmentData attach = createGradingtAttachment(resourceId, filename, protocol);
		AssessmentGradingAttachment assessAttach = new AssessmentGradingAttachment(attach, assessmentGrading);
		assessAttach.setAssessmentGrading(assessmentGrading);
		return assessAttach;
	}
	
	private GradingAttachmentData createGradingtAttachment(String resourceId, String filename, String protocol) {
		GradingAttachmentData attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = contentHostingService.getResource(resourceId);
			if (cr != null) {
				AssessmentFacadeQueries assessmentFacadeQueries = new AssessmentFacadeQueries();
				ResourceProperties p = cr.getProperties();
				attach = new GradingAttachmentData();
				attach.setResourceId(resourceId);
				attach.setFilename(filename);
				attach.setMimeType(cr.getContentType());
				// we want to display kb, so divide by 1000 and round the result
				attach.setFileSize(assessmentFacadeQueries.fileSizeInKB(cr.getContentLength()));
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
				attach.setLastModifiedBy(p.getProperty(p.getNamePropModifiedBy()));
				attach.setLastModifiedDate(new Date());
				attach.setLocation(assessmentFacadeQueries.getRelativePath(cr.getUrl(), protocol));
			}
		} catch (PermissionException | IdUnusedException | TypeException pe) {
			log.warn(pe.getMessage(), pe);
		}
		return attach;
	}

	  public void removeItemGradingAttachment(Long attachmentId) {
		  ItemGradingAttachment itemGradingAttachment = (ItemGradingAttachment) getHibernateTemplate()
				.load(ItemGradingAttachment.class, attachmentId);
		ItemGradingData itemGrading = itemGradingAttachment.getItemGrading();
		// String resourceId = assessmentAttachment.getResourceId();
		int retryCount = persistenceHelper.getRetryCount();
		while (retryCount > 0) {
			try {
				if (itemGrading != null) {
					Set set = itemGrading.getItemGradingAttachmentSet();
					set.remove(itemGradingAttachment);
					getHibernateTemplate().delete(itemGradingAttachment);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem delete assessmentAttachment: "
						+ e.getMessage());
				retryCount = persistenceHelper.retryDeadlock(e,
						retryCount);
			}
		}
	  }
	  
	  public void removeAssessmentGradingAttachment(Long attachmentId) {
		  AssessmentGradingAttachment assessmentGradingAttachment = (AssessmentGradingAttachment) getHibernateTemplate()
				.load(AssessmentGradingAttachment.class, attachmentId);
		AssessmentGradingData assessmentGrading = assessmentGradingAttachment.getAssessmentGrading();
		// String resourceId = assessmentAttachment.getResourceId();
		int retryCount = persistenceHelper.getRetryCount();
		while (retryCount > 0) {
			try {
				if (assessmentGrading != null) {
					Set set = assessmentGrading.getAssessmentGradingAttachmentSet();
					set.remove(assessmentGradingAttachment);
					getHibernateTemplate().delete(assessmentGradingAttachment);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem delete assessmentAttachment: "
						+ e.getMessage());
				retryCount = persistenceHelper.retryDeadlock(e,
						retryCount);
			}
		}
	  }

	  public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
		  for (AttachmentIfc attachment : list) {
		      getHibernateTemplate().saveOrUpdate(attachment);
		  }
	  }

	  public HashMap getInProgressCounts(String siteId) {
		  Object [] values = {"OWN_PUBLISHED_ASSESSMENT", siteId, false, 0, 6};

		  List list = getHibernateTemplate()
		  .find("select a.publishedAssessmentId, count(*) from AssessmentGradingData a, AuthorizationData au " +
				  "where au.functionId = ? and au.agentIdString = ? and a.publishedAssessmentId = au.qualifierId " +
				  "and a.forGrade=? and (a.status=? or a.status=?) group by a.publishedAssessmentId", values);
		  Iterator iter = list.iterator();
		  HashMap inProgressCountsMap = new HashMap();
		  while (iter.hasNext()) {
			  Object o[] = (Object[]) iter.next();
			  inProgressCountsMap.put(o[0], o[1]);
		  }
		  return inProgressCountsMap;
	  }

	  public HashMap getSubmittedCounts(String siteId) {
		  Object [] values = {"OWN_PUBLISHED_ASSESSMENT", siteId, true};

		  List list = getHibernateTemplate()
		  .find("select a.publishedAssessmentId, count(distinct a.agentId) " +
				  "from AssessmentGradingData a, AuthorizationData au, PublishedAssessmentData p " +
				  "where au.functionId = ? and au.agentIdString = ? and a.publishedAssessmentId = au.qualifierId " +
				  "and a.forGrade=? and a.publishedAssessmentId = p.publishedAssessmentId and " +
				  "(p.lastNeedResubmitDate is null or a.submittedDate >= p.lastNeedResubmitDate) group by a.publishedAssessmentId", values);
		  Iterator iter = list.iterator();
		  HashMap startedCountsMap = new HashMap();
		  while (iter.hasNext()) {
			  Object o[] = (Object[]) iter.next();
			  startedCountsMap.put(o[0], o[1]);
		  }
		  return startedCountsMap;
	  }

	  public void completeItemGradingData(AssessmentGradingData assessmentGradingData) {
		  completeItemGradingData(assessmentGradingData, null);
	  }
			
	  
	  public void completeItemGradingData(AssessmentGradingData assessmentGradingData, HashMap sectionSetMap) {
		  ArrayList answeredPublishedItemIdList = new ArrayList();
		  List publishedItemIds = getPublishedItemIds(assessmentGradingData.getAssessmentGradingId());
		  Iterator iter = publishedItemIds.iterator();
		  Long answeredPublishedItemId;
		  while (iter.hasNext()) {
			  answeredPublishedItemId = (Long) iter.next();
			  log.debug("answeredPublishedItemId = " + answeredPublishedItemId);
			  answeredPublishedItemIdList.add(answeredPublishedItemId);
		  }

		  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		  Long publishedAssessmentId = assessmentGradingData.getPublishedAssessmentId();
		  HashSet sectionSet;
		  if (sectionSetMap == null || !sectionSetMap.containsKey(publishedAssessmentId)) {
			  sectionSet = publishedAssessmentService.getSectionSetForAssessment(publishedAssessmentId);
			  if (sectionSetMap != null) {
				  sectionSetMap.put(publishedAssessmentId, sectionSet);
			  }
		  }
		  else {
			  sectionSet = (HashSet) sectionSetMap.get(publishedAssessmentId);
		  }

		  if (sectionSet == null) {
			  return;
		  }

		  PublishedSectionData publishedSectionData;
		  ArrayList itemArrayList;
		  Long publishedItemId;
		  PublishedItemData publishedItemData;
		  iter = sectionSet.iterator();
		  while (iter.hasNext()) {
			  publishedSectionData = (PublishedSectionData) iter.next();
			  log.debug("sectionId = " + publishedSectionData.getSectionId());
			  
			  String authorType = publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
			  if (authorType != null && authorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
				  log.debug("Random draw from questonpool");
				  itemArrayList = publishedSectionData.getItemArray();
				  long seed = (long) AgentFacade.getAgentString().hashCode();
				  if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE) != null && publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE).equals(SectionDataIfc.PER_SUBMISSION)) {
					  seed = (long) (assessmentGradingData.getAssessmentGradingId().toString() + "_" + publishedSectionData.getSectionId().toString()).hashCode();
				  }

				  Collections.shuffle(itemArrayList,  new Random(seed));

				  Integer numberToBeDrawn = 0;
				  if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) !=null ) {
					  numberToBeDrawn= Integer.valueOf(publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
				  }

				  int samplesize = numberToBeDrawn;
				  for (int i=0; i < samplesize; i++){
					  publishedItemData = (PublishedItemData) itemArrayList.get(i);
					  publishedItemId = publishedItemData.getItemId();
					  log.debug("publishedItemId = " + publishedItemId); 
					  if (!answeredPublishedItemIdList.contains(publishedItemId)) {
						  saveItemGradingData(assessmentGradingData, publishedItemId);
					  }
				  }
			  }
			  else {
				  log.debug("Not random draw from questonpool");
				  itemArrayList = publishedSectionData.getItemArray();
				  Iterator itemIter = itemArrayList.iterator();
				  while (itemIter.hasNext()) {
					  publishedItemData = (PublishedItemData) itemIter.next();
					  publishedItemId = publishedItemData.getItemId();
					  log.debug("publishedItemId = " + publishedItemId);
					  if (!answeredPublishedItemIdList.contains(publishedItemId)) {
						  saveItemGradingData(assessmentGradingData, publishedItemId);
					  }
				  }
			  }
		  }
	  }

	  private void saveItemGradingData(AssessmentGradingData assessmentGradingData, Long publishedItemId) {
		  log.debug("Adding one ItemGradingData...");
		  ItemGradingData itemGradingData = new ItemGradingData();
		  itemGradingData.setAssessmentGradingId(assessmentGradingData.getAssessmentGradingId());
		  itemGradingData.setAgentId(assessmentGradingData.getAgentId());
		  itemGradingData.setPublishedItemId(publishedItemId);
		  ItemService itemService = new ItemService();
		  Long itemTextId = itemService.getItemTextId(publishedItemId);
		  log.debug("itemTextId = {}", itemTextId);
		  if(itemTextId != -1){
			  itemGradingData.setPublishedItemTextId(itemTextId);
			  //we're in the DAO su we can use the DAO method directly
			  saveItemGrading(itemGradingData);
		  }
	  }
	  
	  /***
	   * 
	   *@author Mustansar Mehmood
	   * @param publishedAssessmentId
	   * @param agentId
	   * @return 
	   */
	  public Double getAverageSubmittedAssessmentGrading( final Long publishedAssessmentId, final String agentId)
	  {
		  Double averageScore= 0.0;
		  AssessmentGradingData ag = null;
		  final String query ="from AssessmentGradingData a "+
		  " where a.publishedAssessmentId=? and a.agentId=? and "+
		  " a.forGrade=?  order by  a.submittedDate desc";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setLong(0, publishedAssessmentId);
				  q.setString(1, agentId);
				  q.setBoolean(2, true);
				  return q.list();
			  };
		  };
		  List assessmentGradings = getHibernateTemplate().executeFind(hcb);

		  if (!assessmentGradings.isEmpty()){
			  AssessmentGradingData agd;
			  Double cumulativeScore=new Double(0);
			  Iterator i = assessmentGradings.iterator();

			  while(i.hasNext()){
				  agd= (AssessmentGradingData)i.next();
				  cumulativeScore+=agd.getFinalScore();	
			  }
			  averageScore= cumulativeScore/assessmentGradings.size();
			  
			  DecimalFormat df = new DecimalFormat("0.##");
			  DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			  dfs.setDecimalSeparator('.');
			  df.setDecimalFormatSymbols(dfs);
			  
			  averageScore= new Double(df.format((double)averageScore));
		  }  
		  return averageScore;
	  }

	  public List getHighestSubmittedAssessmentGradingList(final Long publishedAssessmentId){
		  final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=? order by a.agentId asc, a.finalScore desc";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setLong(0, publishedAssessmentId);
				  q.setBoolean(1, true);
				  return q.list();
			  };
		  };
		  List assessmentGradings = getHibernateTemplate().executeFind(hcb);

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

	  /**
	   * @author Mustansar Mehmood mustansar@rice.edu
	   * @param publishedAssessmentId
	   * @return 
	   * */
	  public HashMap getAverageAssessmentGradingByPublishedItem(final Long publishedAssessmentId){
		  HashMap h = new HashMap();
		  final String query = "select new AssessmentGradingData("+
		  " a.assessmentGradingId, p.itemId, "+
		  " a.agentId, a.finalScore, a.submittedDate) "+
		  " from ItemGradingData i, AssessmentGradingData a,"+
		  " PublishedItemData p where "+
		  " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and "+
		  " a.publishedAssessmentId=? " +
		  " order by a.agentId asc, a.submittedDate desc";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setLong(0, publishedAssessmentId);
				  return q.list();
			  };
		  };

		  List assessmentGradings = getHibernateTemplate().executeFind(hcb);

		  String currentAgent="";
		  Date submittedDate = null;
		  for (int i=0; i<assessmentGradings.size(); i++){
			  AssessmentGradingData g = (AssessmentGradingData)assessmentGradings.get(i);
			  Long itemId = g.getPublishedItemId();
			  Long gradingId = g.getAssessmentGradingId();
			  if ( i==0 ){
				  currentAgent = g.getAgentId();
				  submittedDate = g.getSubmittedDate();
			  }
			  if (currentAgent.equals(g.getAgentId())
					  && ((submittedDate==null && g.getSubmittedDate()==null)
							  || (submittedDate!=null && submittedDate.equals(g.getSubmittedDate())))){
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

	  public HashMap<Long,ArrayList<ItemGradingAttachment>> getItemGradingAttachmentMap(final Set itemGradingIds) {
		  final String query = "from ItemGradingAttachment a where a.itemGrading.itemGradingId in (:itemGradingIds)";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setParameterList("itemGradingIds", itemGradingIds);
				  return q.list();
			  };
		  };
		  List itemGradingAttachmentList = getHibernateTemplate().executeFind(hcb);
		  return processItemGradingAttachment(itemGradingAttachmentList);
	  }
	  
	  public HashMap<Long,ArrayList<ItemGradingAttachment>> getItemGradingAttachmentMap(final Long publishedItemId) {
		  final String query = "select a from ItemGradingAttachment a " +
			  		"where a.itemGrading.publishedItemId = :publishedItemId ";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setLong("publishedItemId", publishedItemId);
				  return q.list();
			  };
		  };
		  List itemGradingAttachmentList = getHibernateTemplate().executeFind(hcb);
		  return processItemGradingAttachment(itemGradingAttachmentList);
	  }
	  
	  public Map<Long, List<AssessmentGradingAttachment>> getAssessmentGradingAttachmentMap(final Long pubAssessmentId) {
		  final String query = "select a from AssessmentGradingAttachment a " +
			  		"where a.assessmentGrading.publishedAssessmentId = :pubAssessmentId ";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setLong("pubAssessmentId", pubAssessmentId);
				  return q.list();
			  };
		  };
		  List assessmentGradingAttachmentList = getHibernateTemplate().executeFind(hcb);
		  return processAssessmentGradingAttachment(assessmentGradingAttachmentList);
	  }
	  
	  public HashMap<Long,ArrayList<ItemGradingAttachment>> getItemGradingAttachmentMapByAssessmentGradingId(final Long assessmentGradingId) {
		  final String query = "select a from ItemGradingAttachment a, ItemGradingData i " +
		  		"where a.itemGrading.itemGradingId = i.itemGradingId " +
		  		"and i.assessmentGradingId = :assessmentGradingId";

		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(query);
				  q.setLong("assessmentGradingId", assessmentGradingId);
				  return q.list();
			  };
		  };
		    
		  List itemGradingAttachmentList = getHibernateTemplate().executeFind(hcb);
		  return processItemGradingAttachment(itemGradingAttachmentList);
	  }
	  
	  private HashMap<Long,ArrayList<ItemGradingAttachment>> processItemGradingAttachment(List itemGradingAttachmentList) {
		  HashMap<Long,ArrayList<ItemGradingAttachment>> itemGradingAttachmentMap = new HashMap();
		  for (int i=0; i<itemGradingAttachmentList.size(); i++){
			  ItemGradingAttachment attachment = (ItemGradingAttachment)itemGradingAttachmentList.get(i);
			  Long itemGrdingId = attachment.getItemGrading().getItemGradingId();
			  ArrayList attachmentList;
			  if (itemGradingAttachmentMap.containsKey(itemGrdingId)) {
				  attachmentList = (ArrayList<ItemGradingAttachment>) itemGradingAttachmentMap.get(itemGrdingId);  
			  }
			  else {
				  attachmentList = new ArrayList();
			  }
			  attachmentList.add(attachment);
			  itemGradingAttachmentMap.put(itemGrdingId, attachmentList);
		  }
	    
		  return itemGradingAttachmentMap;
	  }
	  
	  private Map<Long, List<AssessmentGradingAttachment>> processAssessmentGradingAttachment(
			  List<AssessmentGradingAttachment> assessmentGradingAttachmentList) {
		  
		  Map<Long, List<AssessmentGradingAttachment>> assessmentGradingAttachmentMap = new HashMap<>();
		  for (int i=0; i<assessmentGradingAttachmentList.size(); i++){
			  AssessmentGradingAttachment attachment = assessmentGradingAttachmentList.get(i);
			  Long assessGradingId = attachment.getAssessmentGrading().getAssessmentGradingId();
			  List<AssessmentGradingAttachment> attachmentList = new ArrayList<>();
			  if (assessmentGradingAttachmentMap.containsKey(assessGradingId)) {
				  attachmentList = assessmentGradingAttachmentMap.get(assessGradingId);  
			  }
			  
			  attachmentList.add(attachment);
			  assessmentGradingAttachmentMap.put(assessGradingId, attachmentList);
		  }
	    
		  return assessmentGradingAttachmentMap;
	  }
	  
    /**
     * This is a dummy class for sections that are made up of random questions
     * from a pool
     */
	  private static class EmptyItemGrading extends ItemGradingData {
		  /**
		 * 
		 */
		private static final long serialVersionUID = 1444166131103415747L;
		private Integer sectionSequence;
		  private Long publishedItemId;
		  private Integer itemSequence;

		  EmptyItemGrading(Integer sectionSequence, Long publishedItemId, Integer itemSequence){
			  this.sectionSequence = sectionSequence;
			  this.publishedItemId = publishedItemId;
			  this.itemSequence = itemSequence;
		  }

		  /**
		   * @return the itemSequence
		   */

		  public Integer getItemSequence() {
			  return itemSequence;
		  }

		  public Integer getSectionSequence(){
			  return sectionSequence;
		  }

	  }
	  
	  public List getUnSubmittedAssessmentGradingDataList(final Long publishedAssessmentId, final String agentIdString) {
		  final HibernateCallback hcb = new HibernateCallback(){
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.createQuery(
						  "from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.attemptDate desc");
				  q.setLong(0, publishedAssessmentId);
				  q.setString(1, agentIdString);
				  q.setBoolean(2, false);
				  return q.list();
			  };
		  };
		  List assessmentGradings = getHibernateTemplate().executeFind(hcb);

		  return assessmentGradings;
	  }
}
