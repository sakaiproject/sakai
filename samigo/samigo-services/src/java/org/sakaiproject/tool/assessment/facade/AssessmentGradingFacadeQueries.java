/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/AssessmentGradingFacadeQueries.java $
 * $Id: AssessmentGradingFacadeQueries.java 9348 2006-05-13 06:14:57Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class AssessmentGradingFacadeQueries extends HibernateDaoSupport implements AssessmentGradingFacadeQueriesAPI{
  private static Log log = LogFactory.getLog(AssessmentGradingFacadeQueries.class);

  public AssessmentGradingFacadeQueries () {
  }

  public List getTotalScores(final String publishedId, String which) {
	  return getTotalScores(publishedId, which, true);
  }

  public List getTotalScores(final String publishedId, String which, final boolean getSubmittedOnly) {
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
//      Object[] objects = new Object[2];
//      objects[0] = new Long(publishedId);
//      objects[1] = new Boolean(true);
//      Type[] types = new Type[2];
//      types[0] = Hibernate.LONG;
//      types[1] = Hibernate.BOOLEAN;

      final HibernateCallback hcb = new HibernateCallback(){
      	public Object doInHibernate(Session session) throws HibernateException, SQLException {
      		Query q = null;
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
      			q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
      		}
      		return q.list();
      	};
      };
      List list = getHibernateTemplate().executeFind(hcb);

//      List list = getHibernateTemplate().find(
//    		  "from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, finalScore DESC, submittedDate DESC", 
//    		  objects, types);

      // last submission
      if (which.equals(EvaluationModelIfc.LAST_SCORE.toString())) {
    	  final HibernateCallback hcb2 = new HibernateCallback(){
    		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
    	    	Query q = null;
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
	      			q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
	      		}
	    		return q.list();
    	    };
    	  };
    	    list = getHibernateTemplate().executeFind(hcb2);

//    	  list = getHibernateTemplate().find(
//    		  "from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=? order by agentId ASC, submittedDate DESC", 
//    		  objects, types);
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
        data.setPublishedAssessmentId(assessment.getPublishedAssessmentId());
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
      e.printStackTrace();
      return new ArrayList();
    }
  }

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
      		q.setLong(0, publishedId.longValue());
    		q.setInteger(1, AssessmentGradingIfc.NO_SUBMISSION.intValue());
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
    try {
      List scores = getTotalScores(publishedId.toString(), which);
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
      		q.setLong(0, publishedId.longValue());
      		q.setString(1, agentId);
      		q.setBoolean(2, false);
    		q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
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
      log.debug("****#6, gdata="+gdata);
      //log.debug("****#7, item size="+gdata.getItemGradingSet().size());
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

 
  public HashMap getSubmitData(final Long publishedId, final String agentId, final Integer scoringoption)
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
      		Query q = session.createQuery("from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate DESC");
      		q.setLong(0, publishedId.longValue());
      		q.setString(1, agentId);
      		q.setBoolean(2, true);
      		return q.list();
      		}
      		else {
      			//highest submission
          		Query q1 = session.createQuery("from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.finalScore DESC, a.submittedDate DESC");
          		q1.setLong(0, publishedId.longValue());
          		q1.setString(1, agentId);
          		q1.setBoolean(2, true);
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

  public Long add(AssessmentGradingData a) {
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().save(a);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem adding assessmentGrading: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    return a.getAssessmentGradingId();
  }

  public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId){
	Object [] values = {Boolean.valueOf(true), publishedAssessmentId};
	List size = getHibernateTemplate().find(
        "select count(a) from AssessmentGradingData a where a.forGrade=? and a.publishedAssessmentId=?", values);
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

	// modified by gopalrc to take account of group release
	//ArrayList groupIds = getSiteGroupIds(AgentFacade.getCurrentSiteId());
	
	/*
	String groupsIdStr = "(";
	Iterator groupIdIter = groupIds.iterator();
	while (groupIdIter.hasNext()) {
		groupsIdStr += "'" + groupIdIter.next().toString() + "', ";
	}
	groupsIdStr += "'none')";
    */
	
	
	HashMap h = new HashMap();
	
    Object [] values = {"OWN_PUBLISHED_ASSESSMENT", AgentFacade.getCurrentSiteId(), Boolean.valueOf(true)};
    
    //List list = getHibernateTemplate().find("select new PublishedAssessmentData(a.publishedAssessmentId, count(a)) from AssessmentGradingData a where a.forGrade=? group by a.publishedAssessmentId", Boolean.valueOf(true));
    List list = getHibernateTemplate().find(
    		"select new PublishedAssessmentData(ag.publishedAssessmentId, count(ag.publishedAssessmentId)) " +
            "from AssessmentGradingData ag, AuthorizationData au " +
            "where au.functionId = ? and au.agentIdString = ? " +
            "and ag.publishedAssessmentId = au.qualifierId and ag.forGrade=? " +
            "group by ag.publishedAssessmentId", values);

    
    //"where au.functionId = ? and (au.agentIdString = ?  or au.agentIdString in (?)) " +

    
    Iterator iter = list.iterator();
    while (iter.hasNext()){
      PublishedAssessmentData o = (PublishedAssessmentData)iter.next();
      h.put(o.getPublishedAssessmentId(), Integer.valueOf(o.getSubmissionSize()));
    }
    return h;
  }

  public HashMap getAGDataSizeOfAllPublishedAssessments() {
		HashMap agDataSizeMap = new HashMap();
		List list = getHibernateTemplate()
				.find(
						"select a.publishedAssessmentId, count(a) from AssessmentGradingData a group by a.publishedAssessmentId");
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Object o[] = (Object[]) iter.next();
			agDataSizeMap.put(o[0], o[1]);
		}
		return agDataSizeMap;
  }
  
  public Long saveMedia(byte[] media, String mimeType){
    log.debug("****"+AgentFacade.getAgentString()+"saving media...size="+media.length+" "+(new Date()));
    MediaData mediaData = new MediaData(media, mimeType);
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().save(mediaData);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving media with mimeType: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    log.debug("****"+AgentFacade.getAgentString()+"saved media."+(new Date()));
    return mediaData.getMediaId();
  }

  public Long saveMedia(MediaData mediaData){
    log.debug("****"+mediaData.getFilename()+" saving media...size="+mediaData.getFileSize()+" "+(new Date()));
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().save(mediaData);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving media: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
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
      statement0.setLong(1, mediaId.longValue());
      rs =statement0.executeQuery();
      if (rs.next()){
        mediaLocation = rs.getString("LOCATION");
      }
      log.debug("****mediaLocation="+mediaLocation);

      String query="delete from SAM_MEDIA_T where MEDIAID=?";
      statement = conn.prepareStatement(query);
      statement.setLong(1, mediaId.longValue());
      statement.executeUpdate();
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
    finally{
    	if (session !=null){
    		try {
    			session.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}
    	if (rs !=null){
    		try {
    			rs.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}
    	if (statement !=null){
    		try {
    			statement.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}
       	if (statement0 !=null){
    		try {
    			statement0.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}

    	if (conn !=null){
    		try {
    			conn.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	} 
    }
    try{
      if (mediaLocation != null){
        File mediaFile = new File(mediaLocation);
        mediaFile.delete();
      }
    }
    catch (Exception e) {
      log.warn("problem removing file="+e.getMessage());
    }
    
    if (itemGradingId != null) {
    	ItemGradingData itemGradingData = getItemGrading(itemGradingId);
    	itemGradingData.setAutoScore(Float.valueOf(0));
    	saveItemGrading(itemGradingData);
    }
  }

  public MediaData getMedia(Long mediaId){

    MediaData mediaData = (MediaData) getHibernateTemplate().load(MediaData.class, mediaId);
    if (mediaData != null){
      String mediaLocation = mediaData.getLocation();
      if (mediaLocation == null || (mediaLocation.trim()).equals("")){
        mediaData.setMedia(getMediaStream(mediaId));
      }
    }
    return mediaData;
  }

  public ArrayList getMediaArray(final Long itemGradingId){
    log.debug("*** itemGradingId ="+itemGradingId);
    ArrayList a = new ArrayList();

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("from MediaData m where m.itemGradingData.itemGradingId=?");
    		q.setLong(0, itemGradingId.longValue());
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

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
    				ItemGradingIfc itemGradingData = (ItemGradingIfc) list.get(i);
    				itemGradingIdList.add(itemGradingData.getItemGradingId());
    			}

    			/** create or disjunctive expression for (in clauses) */
    			List tempList = new ArrayList();
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
    	return (List) getHibernateTemplate().execute(hcb);
    	} catch (Exception e) {
    		e.printStackTrace();
    		return new ArrayList();
    	}
  }
  
  public ItemGradingData getLastItemGradingDataByAgent(
      final Long publishedItemId, final String agentId)
  {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("from ItemGradingData i where i.publishedItemId=? and i.agentId=?");
	    		q.setLong(0, publishedItemId.longValue());
	    		q.setString(1, agentId);
	    		return q.list();
	    	};
	    };
	    List itemGradings = getHibernateTemplate().executeFind(hcb);

//	  List itemGradings = getHibernateTemplate().find(
//        "from ItemGradingData i where i.publishedItemId=? and i.agentId=?",
//        new Object[] { publishedItemId, agentId },
//        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (itemGradings.size() == 0)
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public ItemGradingData getItemGradingData(
      final Long assessmentGradingId, final Long publishedItemId)
  {
    log.debug("****assessmentGradingId="+assessmentGradingId);
    log.debug("****publishedItemId="+publishedItemId);

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(
    				"from ItemGradingData i where i.assessmentGradingId = ? and i.publishedItemId=?");
    		q.setLong(0, assessmentGradingId.longValue());
    		q.setLong(1, publishedItemId.longValue());
    		return q.list();
    	};
    };
    List itemGradings = getHibernateTemplate().executeFind(hcb);

//    List itemGradings = getHibernateTemplate().find(
//        "from ItemGradingData i where i.assessmentGradingId = ? and i.publishedItemId=?",
//        new Object[] { assessmentGradingId, publishedItemId },
//        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.LONG });
    if (itemGradings.size() == 0)
      return null;
    return (ItemGradingData) itemGradings.get(0);
  }

  public AssessmentGradingData load(Long id) {
    AssessmentGradingData gdata = (AssessmentGradingData) getHibernateTemplate().load(AssessmentGradingData.class, id);
    gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
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
    		q.setLong(0, publishedAssessmentId.longValue());
    		q.setString(1, agentIdString);
    		q.setBoolean(2, false);
    		q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(
//        "from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc",
//         new Object[] { publishedAssessmentId, agentIdString, Boolean.FALSE },
//         new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.BOOLEAN });
    if (assessmentGradings.size() != 0){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }

  public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
	    AssessmentGradingData ag = null;

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//	    List assessmentGradings = getHibernateTemplate().find(
//	        "from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc",
//	         new Object[] { publishedAssessmentId, agentIdString, Boolean.FALSE },
//	         new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.BOOLEAN });
	    if (assessmentGradings.size() != 0){
	      ag = (AssessmentGradingData) assessmentGradings.get(0);
	      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
	    }  
	    return ag;
	  }
  
  public AssessmentGradingIfc getLastAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
    AssessmentGradingData ag = null;

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(
    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? order by a.submittedDate desc");
    		q.setLong(0, publishedAssessmentId.longValue());
    		q.setString(1, agentIdString);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(
//        "from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? order by a.submittedDate desc",
//         new Object[] { publishedAssessmentId, agentIdString },
//         new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (assessmentGradings.size() != 0){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }


  public void saveItemGrading(ItemGradingIfc item) {
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().saveOrUpdate((ItemGradingData)item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving itemGrading: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment) {
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        /* for testing the catch block - daisyf 
        if (retryCount >2)
          throw new Exception("uncategorized SQLException for SQL []; SQL state [61000]; error code [60]; ORA-00060: deadlock detected while waiting for resource");
	*/ 
        getHibernateTemplate().saveOrUpdate((AssessmentGradingData)assessment);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem inserting/updating assessmentGrading: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
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
      log.debug("****Connection="+conn);
      String query="select MEDIA from SAM_MEDIA_T where MEDIAID=?";
      statement = conn.prepareStatement(query);
      statement.setLong(1, mediaId.longValue());
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
    catch(Exception e){
      log.warn(e.getMessage());
    }
    
    finally{
    	if (session !=null){
    		try {
    			session.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}
    	if (rs !=null){
    		try {
    			rs.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}
    	if (statement !=null){
    		try {
    			statement.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	}
       	if (in !=null){
    		try {
    			in.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
    	} 
    	if (conn !=null){
    		try {
    			conn.close();
    		} catch (Exception e1) {
    			e1.printStackTrace();
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
	    		q.setLong(0, publishedItemId.longValue());
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

  public AssessmentGradingIfc getHighestAssessmentGrading(
         final Long publishedAssessmentId, final String agentId)
  {
    AssessmentGradingData ag = null;
    final String query ="from AssessmentGradingData a "+
						" where a.publishedAssessmentId=? and "+
						" a.agentId=? order by a.finalScore desc, a.submittedDate desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId.longValue());
    		q.setString(1, agentId);
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//        new Object[] { publishedAssessmentId, agentId },
//        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
    if (assessmentGradings.size() != 0){
      ag = (AssessmentGradingData) assessmentGradings.get(0);
      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
    }  
    return ag;
  }
  
  public AssessmentGradingIfc getHighestSubmittedAssessmentGrading(
	         final Long publishedAssessmentId, final String agentId)
	  {
	    AssessmentGradingData ag = null;
	    final String query ="from AssessmentGradingData a "+
        					" where a.publishedAssessmentId=? and a.agentId=? and "+
        					" a.forGrade=?  order by a.finalScore desc, a.submittedDate desc";
	    
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setString(1, agentId);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//	    List assessmentGradings = getHibernateTemplate().find(query,
//	        new Object[] { publishedAssessmentId, agentId },
//	        new org.hibernate.type.Type[] { Hibernate.LONG, Hibernate.STRING });
	    if (assessmentGradings.size() != 0){
	      ag = (AssessmentGradingData) assessmentGradings.get(0);
	      ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
	    }  
	    return ag;
	  }

  public List getLastAssessmentGradingList(final Long publishedAssessmentId){
    final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? order by a.agentId asc, a.submittedDate desc";

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, publishedAssessmentId.longValue());
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
	    final String query = "from AssessmentGradingData a where a.publishedAssessmentId=? and a.forGrade=? order by a.agentId asc, a.submittedDate desc";

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(query);
	    		q.setLong(0, publishedAssessmentId.longValue());
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
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setBoolean(1, true);
	    		q.setBoolean(2, false);
	    		q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
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
    		q.setLong(0, publishedAssessmentId.longValue());
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
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setBoolean(1, true);
	    		q.setBoolean(2, false);
	    		q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
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
    		q.setLong(0, publishedAssessmentId.longValue());
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
    		q.setLong(0, publishedAssessmentId.longValue());
    		return q.list();
    	};
    };
    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

//    List assessmentGradings = getHibernateTemplate().find(query,
//         new Object[] { publishedAssessmentId },
//         new org.hibernate.type.Type[] { Hibernate.LONG });

//    ArrayList l = new ArrayList();
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
    		q.setLong(0, assessmentGradingId.longValue());
    		return q.list();
    	};
    };
    List itemGradings = getHibernateTemplate().executeFind(hcb);

//    List itemGradings = getHibernateTemplate().find(query,
//                                                    new Object[] { assessmentGradingId },
//                                                    new org.hibernate.type.Type[] { Hibernate.LONG });
    HashSet s = new HashSet();
    for (int i=0; i<itemGradings.size();i++){
      s.add(itemGradings.get(i));
    }
    return s;
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
    		q.setLong(0, publishedAssessmentId.longValue());
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
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().deleteAll(c);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem inserting assessmentGrading: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }


  public void saveOrUpdateAll(Collection c) {
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().saveOrUpdateAll(c);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem inserting assessmentGrading: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
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
    		q.setLong(0, assessmentGradingId.longValue());
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
	    		q.setLong(0, publishedItemId.longValue());
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
				  q.setLong(1, assessmentGradingId.longValue());
				  return q.list();
			  };
		  };
		  List list = getHibernateTemplate().executeFind(hcb);
		  if ( list.size() == 0) {
			  position.add(Integer.valueOf(0));
			  position.add(Integer.valueOf(0));
		  }
		  else {
			  Integer sequence = (Integer) list.get(0);
			  Integer nextSequence;
			  int count = 1;
			  for (int i = 1; i < list.size(); i++) {
				  log.debug("i = " + i);
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
			  position.add(Integer.valueOf(count));
		  }
		  return position;
	  } 
	  catch (Exception e) {
		  e.printStackTrace();
		  position.add(Integer.valueOf(0));
		  position.add(Integer.valueOf(0));
		  return position;
	  }
  }
  
  public List getItemGradingIds(final Long assessmentGradingId){
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select i.publishedItemId from "+
	    		         " ItemGradingData i where i.assessmentGradingId=?");
	    		q.setLong(0, assessmentGradingId.longValue());
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
	    				"select distinct p " +
	    				"from PublishedItemData p, AssessmentGradingData a, ItemGradingData i " +
	    				"where a.publishedAssessmentId=? and a.forGrade=? and p.section.id=? " +
	    				"and i.assessmentGradingId = a.assessmentGradingId " +
	    				"and p.itemId = i.publishedItemId ");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setBoolean(1, true);
	    		q.setLong(2, sectionId.longValue());
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

  public Long getTypeId(final Long itemGradingId) {
	  Long typeId = Long.valueOf(-1);

	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select p.typeId " +
	    				"from PublishedItemData p, ItemGradingData i " +
	    				"where i.itemGradingId=? " +
	    				"and p.itemId = i.publishedItemId ");
	    		q.setLong(0, itemGradingId.longValue());
	    		return q.list();
	    	};
	    };
	    List typeIdList = getHibernateTemplate().executeFind(hcb);

	    Iterator iter = typeIdList.iterator();
	    while(iter.hasNext()) {
	    	typeId = (Long) iter.next();
	    	log.debug("typeId = " + typeId);
	    }
	    return typeId;
  }

  public List getAllAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? order by a.submittedDate desc");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);

	    return assessmentGradings;
  }
  
  public int getActualNumberRetake(final Long publishedAssessmentId, final String agentIdString) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"select count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
	    				" where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? " +
	    				" and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
	    				" and a.submittedDate > s.createdDate");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setString(1, agentIdString);
	    		q.setBoolean(2, true);
	    		return q.list();
	    	};
	    };
	    List countList = getHibernateTemplate().executeFind(hcb);
	    Iterator iter = countList.iterator();
	    if (iter.hasNext()){
	      int i = ((Integer)iter.next()).intValue();
	      return i;
	    }
	    else{
	      return 0;
	    }
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
	    		q.setLong(0, publishedAssessmentId.longValue());
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
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		q.setString(1, agentIdString);
	    		return q.list();
	    	};
	    };
	    List numberRetakeList = getHibernateTemplate().executeFind(hcb);

	    if (numberRetakeList.size() == 0) {
	    	return 0;
	    }
	    else {
	    	Integer numberRetake = (Integer) numberRetakeList.get(0);
	    	return numberRetake.intValue();
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
  
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
	    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
	    while (retryCount > 0){ 
	      try {
	        getHibernateTemplate().saveOrUpdate((StudentGradingSummaryData) studentGradingSummaryData);
	        retryCount = 0;
	      }
	      catch (Exception e) {
	        log.warn("problem saving studentGradingSummaryData: "+e.getMessage());
	        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
	      }
	    }
	  }
  
  public int getLateSubmissionsNumberByAgentId(final Long publishedAssessmentId, final String agentIdString, final Date dueDate) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? and a.forGrade=? and a.submittedDate>?");
	    		q.setLong(0, publishedAssessmentId.longValue());
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
      				"where a.publishedAssessmentId=? and a.forGrade=? " +
			        "order by a.agentId ASC, a.submittedDate");
      		q.setLong(0, Long.parseLong(publishedId));
      		q.setBoolean(1, true);
      		return q.list();
      	};
      };
      return getHibernateTemplate().executeFind(hcb);
  }
  
  
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String questionString, String textString, String rationaleString) {
	  ArrayList dataList = new ArrayList();
	  ArrayList headerList = new ArrayList();
	  ArrayList finalList = new ArrayList(2);
	  PublishedAssessmentService pubService = new PublishedAssessmentService();
	  
	  // gopalrc - Nov 2007
	  HashSet publishedAssessmentSections = pubService.getSectionSetForAssessment(Long.valueOf(publishedAssessmentId));
	  Float zeroFloat = new Float(0.0);
	  
	  HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(pubService.getPublishedAssessment(publishedAssessmentId));
	  HashMap publishedItemTextHash = pubService.preparePublishedItemTextHash(pubService.getPublishedAssessment(publishedAssessmentId));
	  HashMap publishedItemHash = pubService.preparePublishedItemHash(pubService.getPublishedAssessment(publishedAssessmentId));
	  
	  List list = getAllOrderedSubmissions(publishedAssessmentId);
	  Iterator assessmentGradingIter = list.iterator();	  
	  int numSubmission = 1;
	  String lastAgentId = "";
	  boolean fistItemGradingData = true;
	  while(assessmentGradingIter.hasNext()) {
		  
		  // gopalrc Nov 2007
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
			  sectionScores.put(publishedSection.getSequence(), zeroFloat);
		  }
		  
		  
		  ArrayList responseList = new ArrayList();
		  AssessmentGradingData assessmentGradingData = (AssessmentGradingData) assessmentGradingIter.next();
		  String agentId = assessmentGradingData.getAgentId();
		  if (anonymous) {
			  responseList.add(assessmentGradingData.getAssessmentGradingId());
		  }
		  else {
			  String agentEid = "";
			  String firstName = "";
			  String lastName = "";
			  try {
				  agentEid = UserDirectoryService.getUser(assessmentGradingData.getAgentId()).getEid();
				  firstName = UserDirectoryService.getUser(assessmentGradingData.getAgentId()).getFirstName();
				  lastName = UserDirectoryService.getUser(assessmentGradingData.getAgentId()).getLastName();
			  } catch (Exception e) {
				  log.error("Cannot get user");
			  }
			  responseList.add(lastName);
			  responseList.add(firstName);
			  responseList.add(agentEid);
			  if (lastAgentId.equals(agentId)) {
				  numSubmission++;
			  }
			  else {
				  numSubmission = 1;
				  lastAgentId = agentId;
			  }
			  responseList.add(Double.parseDouble(""+numSubmission));
		  }

		  //gopalrc - Dec 2007
		  int sectionScoreColumnStart = responseList.size();
          if (showPartAndTotalScoreSpreadsheetColumns) {
			  Float finalScore = assessmentGradingData.getFinalScore();
			  responseList.add((Double)finalScore.doubleValue()); // gopal - cast for spreadsheet numerics
          }
          
		  Long assessmentGradingId = assessmentGradingData.getAssessmentGradingId();

		  HashMap studentGradingMap = getStudentGradingData(assessmentGradingData.getAssessmentGradingId().toString());
		  ArrayList grades = new ArrayList();
          grades.addAll(studentGradingMap.values());
          
	      Collections.sort(grades, new QuestionComparator(publishedItemHash));

   	   	  int questionNumber = 0;
	      for (Object oo: grades) {	   
	    	   questionNumber++;
	    	   // There can be more than one answer to a question, e.g. for
	    	   // FIB with more than one blank or matching questions. So sort
	    	   // by sequence number of answer. (don't bother to sort if just 1)

	    	   List l = (List)oo;
	    	   if (l.size() > 1)
	    		   Collections.sort(l, new AnswerComparator(publishedAnswerHash));

	    	   String maintext = "";
	    	   String rationale = "";
	    	   boolean addRationale = false;
	    	   
	    	   // loop over answers per question
	    	   int count = 0;
	    	   ItemGradingIfc grade = null;
	    	   //boolean isAudioFileUpload = false;
	    	   boolean isFinFib = false;
	    	   
	    	   // gopalrc - Dec 2007
	    	   float itemScore = 0.0f;
	    	   
	    	   for (Object ooo: l) {
	    		   grade = (ItemGradingIfc)ooo;
	    		   if (grade == null) {
	    			   continue;
	    		   }
	    		   // gopalrc - Dec 2007
	    		   if (grade!=null && grade.getAutoScore()!=null) {
	    			   itemScore += grade.getAutoScore().floatValue();
	    		   }
	    		   
	    		   // now print answer data
	    		   log.debug("<br> "+ grade.getPublishedItemId() + " " + grade.getRationale() + " " + grade.getAnswerText() + " " + grade.getComments() + " " + grade.getReview());
	    		   Long publishedItemId = grade.getPublishedItemId();	    		   
	    		   ItemDataIfc publishedItemData = (ItemDataIfc) publishedItemHash.get(publishedItemId);
	    		   Long typeId = publishedItemData.getTypeId();
	    		   if (typeId.equals(TypeIfc.FILL_IN_BLANK) || typeId.equals(TypeIfc.FILL_IN_NUMERIC)) {
	    			   log.debug("FILL_IN_BLANK, FILL_IN_NUMERIC");
	    			   isFinFib = true;
	    			   String thistext = "";
	    			   
	    			   Long answerid = grade.getPublishedAnswerId();
		    		   Long sequence = null;
		    		   if (answerid != null) {
		    			   AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
		    			   sequence = answer.getSequence();
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
	    			   String thistext = "";

		    		   // for some question types we have another text field
		    		   Long answerid = grade.getPublishedAnswerId();
		    		   String temptext = "No Answer";
		    		   Long sequence = null;
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
		    		   }
		    		   thistext = sequence + ": " + temptext;
		    		   
		    		   if (count == 0)
		    			   maintext = thistext;
		    		   else
		    			   maintext = maintext + "|" + thistext;

		    		   count++;
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
	    		   else {
	    			   log.debug("other type");
	    			   String thistext = "";
	    			   
		    		   // for some question types we have another text field
		    		   Long answerid = grade.getPublishedAnswerId();
		    		   if (answerid != null) {
		    			   AnswerIfc answer  = (AnswerIfc)publishedAnswerHash.get(answerid);
		    			   String temptext = answer.getText();
		    			   if (temptext != null)
		    				   thistext = temptext;
		    		   }

		    		   String temp2text = grade.getAnswerText();
		    		   if (temp2text != null)
		    			   thistext = temp2text;

		    		   if (count == 0)
		    			   maintext = thistext;
		    		   else
		    			   maintext = maintext + "|" + thistext;

		    		   count++;
	    		   }
	    		   
	    		   // taking care of rationale
	    		   if (!addRationale && (typeId.equals(TypeIfc.MULTIPLE_CHOICE) || typeId.equals(TypeIfc.MULTIPLE_CORRECT) || typeId.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) || typeId.equals(TypeIfc.TRUE_FALSE))) {
	    			   log.debug("MULTIPLE_CHOICE or MULTIPLE_CORRECT or MULTIPLE_CORRECT_SINGLE_SELECTION or TRUE_FALSE");
	    			   if (publishedItemData.getHasRationale()) {
	    				   addRationale = true;
	    				   rationale = grade.getRationale();
	    				   if (rationale == null) {
	    					   rationale = "";
	    				   }
	    			   }
	    		   }
	    	   } // inner for - answers

    		   updateSectionScore(sectionItems, sectionScores, grade.getPublishedItemId(), itemScore);
	    	   
	    	   if (isFinFib && maintext.indexOf("No Answer") >= 0 && count == 1) {
	    		   maintext = "No Answer";
	    	   }
	    	   else if ("".equals(maintext)) {
	    		   maintext = "No Answer";
	    	   }

	    	   responseList.add(maintext);
	    	   if (addRationale) {
	    		   responseList.add(rationale);
	    	   }
	    	   
	    	   // Only set header based on the first item grading data
	    	   if (fistItemGradingData) {
	    		   headerList.add(makeHeader(questionString, textString, questionNumber));
	    		   if (addRationale) {
	    			   headerList.add(makeHeader(questionString, rationaleString, questionNumber));
	    		   }
	    	   }	    		   
	       } // outer for - questions
	      
	   	   // gopalrc - Dec 2007
           if (showPartAndTotalScoreSpreadsheetColumns) {
		       if (sectionScores.size() > 1) {
			   	   Iterator keys = sectionScores.keySet().iterator();
			   	   while (keys.hasNext()) {
			   		   Double partScore = (Double) ((Float) sectionScores.get(keys.next())).doubleValue() ;
			   		   responseList.add(sectionScoreColumnStart++, partScore);
			   	   }
		       }
           }
	       
           dataList.add(responseList);
           
           if (fistItemGradingData) {
 			  fistItemGradingData = false;
 		  }
	  } // while
	  Collections.sort(dataList, new ResponsesComparator(anonymous));
	  finalList.add(dataList);
	  finalList.add(headerList);
	  return finalList;
  }
  
  
  /**
   * gopalrc - added Dec 2007
   * @param sectionItems
   * @param sectionScores
   * @param grade
   */
  private void updateSectionScore(HashMap sectionItems, TreeMap sectionScores, Long publishedItemId, float itemScore) {

	  for (Iterator it = sectionItems.entrySet().iterator(); it.hasNext();) {
		  Map.Entry entry = (Map.Entry) it.next();
		  Object sectionSequence = entry.getKey();
		  HashMap itemsForSection = (HashMap) entry.getValue();

		  if (itemsForSection.get(publishedItemId)!=null) {
			  Float score = Float.valueOf( ((Float)sectionScores.get(sectionSequence)).floatValue() + itemScore);
			  sectionScores.put(sectionSequence, score); 
		  }
	  }
  }
  
  
  
    /*
	 sort answers by sequence number within question if one is defined
	 normally it will be, but use id number if not
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published answer
	 */
	class AnswerComparator implements Comparator {

		HashMap publishedAnswerHash;

		public AnswerComparator(HashMap m) {
			publishedAnswerHash = m;
		}

		public int compare(Object a, Object b) {
			ItemGradingIfc agrade = (ItemGradingIfc) a;
			ItemGradingIfc bgrade = (ItemGradingIfc) b;

			Long aindex = agrade.getItemGradingId();
			Long bindex = bgrade.getItemGradingId();

			Long aanswerid = agrade.getPublishedAnswerId();
			Long banswerid = bgrade.getPublishedAnswerId();

			if (aanswerid != null && banswerid != null) {
				AnswerIfc aanswer = (AnswerIfc) publishedAnswerHash
						.get(aanswerid);
				AnswerIfc banswer = (AnswerIfc) publishedAnswerHash
						.get(banswerid);
				aindex = aanswer.getSequence();
				bindex = banswer.getSequence();
			}

			if (aindex < bindex)
				return -1;
			else if (aindex > bindex)
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
	class QuestionComparator implements Comparator {

		HashMap publishedItemHash;

		public QuestionComparator(HashMap m) {
			publishedItemHash = m;
		}

		public int compare(Object a, Object b) {
			ItemGradingIfc agrade = (ItemGradingIfc) ((List) a).get(0);
			ItemGradingIfc bgrade = (ItemGradingIfc) ((List) b).get(0);

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
	class ResponsesComparator implements Comparator {
		boolean anonymous;
		public ResponsesComparator(boolean anony) {
			anonymous = anony;
		}

		public int compare(Object a, Object b) {
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
				if (aFirstElement.compareTo(bFirstElement) < 0)
					return -1;
				else if (aFirstElement.compareTo(bFirstElement) > 0)
					return 1;
				else {
					String aSecondElement = (String) ((ArrayList) a).get(1);
					String bSecondElement = (String) ((ArrayList) b).get(1);
					if (aSecondElement.compareTo(bSecondElement) < 0)
						return -1;
					else if (aSecondElement.compareTo(bSecondElement) > 0)
						return 1;
					else {
						String aThirdElement = (String) ((ArrayList) a).get(2);
						String bThirdElement = (String) ((ArrayList) b).get(2);
						if (aThirdElement.compareTo(bThirdElement) < 0)
							return -1;
						else if (aThirdElement.compareTo(bThirdElement) > 0)
							return 1;
					}
				}
				return 0;
			}
		}
	}

	  public void removeUnsubmittedAssessmentGradingData(final AssessmentGradingIfc data) {
		    final HibernateCallback hcb = new HibernateCallback(){
		    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
		    		Query q = session.createQuery(
		    				"from AssessmentGradingData a where a.publishedAssessmentId=? and a.agentId=? " +
		    				"and a.forGrade=? and a.status=? " +
		    				"order by a.submittedDate desc");
		    		q.setLong(0, data.getPublishedAssessmentId().longValue());
		    		q.setString(1, data.getAgentId());
		    		q.setBoolean(2, false);
		    		q.setInteger(3, AssessmentGradingIfc.NO_SUBMISSION.intValue());
		    		return q.list();
		    	};
		    };
		    List assessmentGradings = getHibernateTemplate().executeFind(hcb);
		    if (assessmentGradings.size() != 0) { 
		    	deleteAll(assessmentGradings);
		    }
	  }

  public boolean getHasGradingData(final Long publishedAssessmentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? ");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);
	    if (assessmentGradings.size() == 0) {
	    	return false;
	    }
	    else {
	    	return true;
	    }
  }
  
  public ArrayList getHasGradingDataAndHasSubmission(final Long publishedAssessmentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery(
	    				"from AssessmentGradingData a where a.publishedAssessmentId=? order by a.agentId asc, a.submittedDate desc");
	    		q.setLong(0, publishedAssessmentId.longValue());
	    		return q.list();
	    	};
	    };
	    List assessmentGradings = getHibernateTemplate().executeFind(hcb);
	    // first element represents hasGradingData
	    // second element represents hasSubmission
	    ArrayList<Boolean> al = new ArrayList<Boolean>(); 
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
					if (adata.getForGrade().booleanValue()) {
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
	
	private SiteService siteService;
	
	/**
	 * added by gopalrc - Nov 2007
`	 * TODO: should perhaps be moved to SiteService
	 * @param siteId
	 * @return
	 */
/*	
	private ArrayList getSiteGroupIds(final String siteId) {
		Collection siteGroups = null;
		try {
			siteGroups = siteService.getSite(siteId).getGroups();
		}
		catch (IdUnusedException ex) {
			// no site found
		}
		Iterator groupsIter = siteGroups.iterator();
		final ArrayList groupIds = new ArrayList();
		// To accomodate the problem with Hibernate and empty array parameters 
		// TODO: this should probably be handled in a more efficient way
		groupIds.add("none");  
		while (groupsIter.hasNext()) {
			Group group = (Group) groupsIter.next(); 
			groupIds.add(group.getId());
		}
		return groupIds;
	}
*/

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
		StringBuffer bindVar = new StringBuffer(filename);
		bindVar.append("%");
		
   	    Object [] values = {itemGradingId.longValue(), agentId, bindVar.toString()};
	    List list = getHibernateTemplate().find(
	    		"select filename from MediaData m where m.itemGradingData.itemGradingId=? and m.createdBy=? and m.filename like ?", values);
   	    if (list.size() == 0) {
	    	return filename;
	    }
   	    
   	    HashSet hs = new HashSet();
   	    Iterator iter = list.iterator();
   	    String name = "";
   	    // Only add the filename which
   	    // 1. with no extension because the newly updated one has no extention
   	    // 2. name is same to filename or name like filename(...
   	    // For example, if the filename is ab. We only want ab, ab(1), ab(2)... and don't want abc to be in
   	    while(iter.hasNext()) {
   	    	name = ((String) iter.next()).trim();
   	    	if (name.indexOf(".") < 0 && (name.equals(filename) || name.startsWith(filename + "("))) {
   	    		hs.add(name);
   	    	}
   	    }
   	    
   	    if (hs.size() == 0) {
   	    	return filename;
   	    }
   	    
   	    StringBuffer testName = new StringBuffer(filename);
	    int i = 1;
	    while(true) {
	    	if (!hs.contains(testName.toString())) {
	    		return testName.toString();
	    	}
	    	else {
	    		i++;
	    		testName = new StringBuffer(filename);
	    	    testName.append("(");
	    		testName.append(i);
	    		testName.append(")");
	    	}
	    }
	}
	
	private String getFilenameWExtesion(Long itemGradingId, String agentId, String filename, int dotIndex) {
		String filenameWithoutExtension = filename.substring(0, dotIndex);
		StringBuffer bindVar = new StringBuffer(filenameWithoutExtension);
		bindVar.append("%");
		bindVar.append(filename.substring(dotIndex));
   	       	    
		Object [] values = {itemGradingId.longValue(), agentId, bindVar.toString()};
	    List list = getHibernateTemplate().find(
	    		"select filename from MediaData m where m.itemGradingData.itemGradingId=? and m.createdBy=? and m.filename like ?", values);
   	    if (list.size() == 0) {
	    	return filename;
	    }
   	    
   	    HashSet hs = new HashSet();
   	    Iterator iter = list.iterator();
   	    String name = "";
   	    int nameLenght = 0;
   	    String extension = filename.substring(dotIndex);
   	    int extensionLength = extension.length();
   	    while(iter.hasNext()) {
   	    	name = ((String) iter.next()).trim();
   	    	if ((name.equals(filename) || name.startsWith(filenameWithoutExtension + "("))) {
   	    		nameLenght = name.length();
   	    		hs.add(name.substring(0, nameLenght - extensionLength));
   	    	}
   	    }
   	    
   	    if (hs.size() == 0) {
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
	
	public List getNeedResubmitList(String agentId) {
		Object[] values = { agentId, false, 4};

		List list = getHibernateTemplate()
				.find("select a.publishedAssessmentId from AssessmentGradingData a where a.agentId=? and a.forGrade=? and a.status=?", values);
		return list;
	}
	
	public void autoSubmitAssessments() {
		Object[] values = { 1 };

		List list = getHibernateTemplate()
				.find("select new AssessmentGradingData(a.assessmentGradingId, a.publishedAssessmentId, " +
						" a.agentId, a.submittedDate, a.isLate, a.forGrade, a.totalAutoScore, a.totalOverrideScore, " +
						" a.finalScore, a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate, a.timeElapsed) " +
						" from AssessmentGradingData a, PublishedAccessControl c " +
						" where a.publishedAssessmentId = c.assessment.publishedAssessmentId " +
						" and current_timestamp() >= c.dueDate and a.status not in (4, 5) and c.autoSubmit = ? " +
						" order by a.publishedAssessmentId, a.agentId, a.forGrade desc ", values);
		
	    Iterator iter = list.iterator();
	    String lastAgentId = "";
	    Long lastPublishedAssessmentId = Long.valueOf(0);
	    ArrayList toBeAutoSubmittedList = new ArrayList();
	    while (iter.hasNext()) {
	    	AssessmentGradingData adata = (AssessmentGradingData) iter.next();
	    	if (lastPublishedAssessmentId.equals(adata.getPublishedAssessmentId())) {
	    		if (!lastAgentId.equals(adata.getAgentId())) {
    				lastAgentId = adata.getAgentId();
	    			if (Boolean.FALSE.equals(adata.getForGrade())) {
	    				adata.setForGrade(Boolean.TRUE);
	    				adata.setIsAutoSubmitted(Boolean.TRUE);
	    				adata.setSubmittedDate(new Date());
	    				adata.setStatus(Integer.valueOf(1));
	    				toBeAutoSubmittedList.add(adata);
	    			}
	    		}
	    	}
	    	else {
	    		lastPublishedAssessmentId = adata.getPublishedAssessmentId();
				lastAgentId = adata.getAgentId();
	    		if (Boolean.FALSE.equals(adata.getForGrade())) {
	    			adata.setForGrade(Boolean.TRUE);
	    			adata.setIsAutoSubmitted(Boolean.TRUE);
	    			adata.setSubmittedDate(new Date());
	    			adata.setStatus(Integer.valueOf(1));
	    			toBeAutoSubmittedList.add(adata);
	    		}
	    	}

	    }
	      
	    this.saveOrUpdateAll(toBeAutoSubmittedList);
	}

	private String makeHeader(String question, String headerType, int questionNumber) {
		StringBuffer sb = new StringBuffer(question);
		sb.append(" ");
		sb.append(questionNumber);
		sb.append(" ");
		sb.append(headerType);
		return sb.toString();
	}
	
	public ItemGradingAttachmentIfc createItemGradingtAttachment(ItemGradingIfc itemGrading, String resourceId, String filename, String protocol) {
		ItemGradingAttachment attach = null;
		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = ContentHostingService.getResource(resourceId);
			if (cr != null) {
				AssessmentFacadeQueries assessmentFacadeQueries = new AssessmentFacadeQueries();
				ResourceProperties p = cr.getProperties();
				attach = new ItemGradingAttachment();
				attach.setItemGrading(itemGrading);
				attach.setResourceId(resourceId);
				attach.setFilename(filename);
				attach.setMimeType(cr.getContentType());
				// we want to display kb, so divide by 1000 and round the result
				attach.setFileSize(new Long(assessmentFacadeQueries.fileSizeInKB(cr.getContentLength())));
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
		} catch (PermissionException pe) {
			pe.printStackTrace();
		} catch (IdUnusedException ie) {
			ie.printStackTrace();
		} catch (TypeException te) {
			te.printStackTrace();
		}
		return attach;
	}

	  public void removeItemGradingAttachment(Long attachmentId) {
		  ItemGradingAttachment itemGradingAttachment = (ItemGradingAttachment) getHibernateTemplate()
				.load(ItemGradingAttachment.class, attachmentId);
		ItemGradingIfc itemGrading = itemGradingAttachment.getItemGrading();
		// String resourceId = assessmentAttachment.getResourceId();
		int retryCount = PersistenceService.getInstance().getRetryCount()
				.intValue();
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
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}
	  }

	  public void saveOrUpdateAttachments(List list) {
		  getHibernateTemplate().saveOrUpdateAll(list);
	  }
}
