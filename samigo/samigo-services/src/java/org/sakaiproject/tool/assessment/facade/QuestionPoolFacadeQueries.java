/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/QuestionPoolFacadeQueries.java $
 * $Id: QuestionPoolFacadeQueries.java 9343 2006-05-12 23:30:02Z lydial@stanford.edu $
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolAccessData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class QuestionPoolFacadeQueries
    extends HibernateDaoSupport implements QuestionPoolFacadeQueriesAPI {
  private static Log log = LogFactory.getLog(QuestionPoolFacadeQueries.class);

  public QuestionPoolFacadeQueries() {
  }

  public IdImpl getQuestionPoolId(String id) {
    return new IdImpl(id);
  }

  public IdImpl getQuestionPoolId(Long id) {
    return new IdImpl(id);
  }

  public IdImpl getQuestionPoolId(long id) {
    return new IdImpl(id);
  }

  /**
   * Get a list of all the pools in the site. Note that questions in each pool will not
   * be populated. We must keep this list updated.
   */
  public List getAllPools() {
    return getHibernateTemplate().find("from QuestionPoolData");
  }


  public List getAllPoolsByAgent(final String agentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("from QuestionPoolData a  where a.questionPoolId in (select ac.questionPoolId from QuestionPoolAccessData ac where agentId= ?) ");
	    		q.setString(0, agentId);
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

//	  List list = getHibernateTemplate().find(
//        "from QuestionPoolData a where a.ownerId= ? ",
//        new Object[] {agentId}
//        , new org.hibernate.type.Type[] {Hibernate.STRING});
    return list;

  }
  /**
       * Get all the pools that the agent has access to. The easiest way seems to be
   * #1. get all the existing pool
   * #2. get all the QuestionPoolAccessData record of the agent
   * #3. go through the existing pools and check it against the QuestionPoolAccessData (qpa) record to see if
   * the agent is granted access to it. qpa record (if exists) always trumps the default access right set
   * up for a pool
   * e.g. if the defaultAccessType for a pool is ACCESS_DENIED but the qpa record say ADMIN, then access=ADMIN
   * e.g. if the defaultAccessType for a pool is ADMIN but the qpa record say ACCESS_DENIED, then access=ACCESS_DENIED
   * e.g. if no qpa record exists, then access rule will follow the defaultAccessType set by the pool
   */
  public QuestionPoolIteratorFacade getAllPools(String agentId) {
    ArrayList qpList = new ArrayList();

    // #1.
    // lydial: 9/22/05 we are not really using QuestionPoolAccessData, so filter by ownerid 
    //List poolList = getAllPools(); 
    List poolList = getHibernateTemplate().find(
    		"from QuestionPoolData a where a.ownerId= ? ",
    		new Object[] {agentId}); 
/*
    // #2. get all the QuestionPoolAccessData record belonging to the agent
    List questionPoolAccessList = getHibernateTemplate().find(
        "from QuestionPoolAccessData as qpa where qpa.agentId=?",
        new Object[] {agentId}
        , new org.hibernate.type.Type[] {Hibernate.STRING});
    HashMap h = new HashMap(); // prepare a hashMap with (poolId, qpa)
    Iterator i = questionPoolAccessList.iterator();
    while (i.hasNext()) {
      QuestionPoolAccessData qpa = (QuestionPoolAccessData) i.next();
      h.put(qpa.getQuestionPoolId(), qpa);
    }

    // #3. We need to go through the existing QuestionPool and the QuestionPoolAccessData record
    // to determine the access type
*/

    try {
    	// counts is a hashmap going from poolid to number of subpools. It is significantly
    	// faster to build this with a single SQL query and then look up data in it.
    	HashMap counts = new HashMap();

    	// hibernate returns a list of arrays, the arrays being the values
    	// returned by the query, in this case poolid and count. Both are
    	// returned as BigInteger. We need Long and Integer.
    	Iterator i1 = getSubPoolSizes(agentId).iterator();
    	while (i1.hasNext()) {
    		Object[]result = (Object [])i1.next();
    		//counts.put( Long.valueOf(((Integer)result[0]).longValue()), Integer.valueOf(((Integer)result[1]).intValue()));
    		counts.put((Long) result[0], (Integer)result[1]);
        	
    	}    	

    	Iterator j = poolList.iterator();
    	while (j.hasNext()) {
    		QuestionPoolData qpp = (QuestionPoolData) j.next();
    		// I really wish we don't need to populate  the questionpool size & subpool size for JSF
    		// watch this for performance. hope Hibernate is smart enough not to load the entire question
    		// - daisy, 10/04/04
    		// populateQuestionPoolItemDatas(qpp);
    		// lookup number of subpools for this pool in our handy hash table
    		Integer subPoolSize = (Integer)counts.get( Long.valueOf(qpp.getQuestionPoolId()));
    		if (subPoolSize == null)
    			qpp.setSubPoolSize( Integer.valueOf(0));
    		else
    			qpp.setSubPoolSize(subPoolSize);

    		qpList.add(getQuestionPool(qpp));
    	}
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
    return new QuestionPoolIteratorFacade(qpList);
  }

  public QuestionPoolIteratorFacade getAllPoolsWithAccess(String agentId) {
	  ArrayList qpList = new ArrayList();
	  List poolList = getAllPoolsByAgent(agentId); 

	  try {
		  Iterator j = poolList.iterator();
		  while (j.hasNext()) {
			  QuestionPoolData qpp = (QuestionPoolData) j.next();
			  // I really wish we don't need to populate  the questionpool size & subpool size for JSF
			  // watch this for performance. hope Hibernate is smart enough not to load the entire question
			  // - daisy, 10/04/04

			  // The comment below is to not recover all questions of items
			  //populateQuestionPoolItemDatas(qpp);

			  // I do this call, after it did in populateQuestionPoolItemData, to recover the number of subpools that will be show in the root of pools.
			  qpp.setSubPoolSize( Integer.valueOf(getSubPoolSize(qpp.getQuestionPoolId())));

			  qpList.add(getQuestionPool(qpp));
		  }
	  }
	  catch (Exception e) {
		  log.warn(e.getMessage());
		  log.warn(e.getStackTrace());
	  }
	  return new QuestionPoolIteratorFacade(qpList);
  }
  
  public ArrayList getBasicInfoOfAllPools(final String agentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select new QuestionPoolData(a.questionPoolId, a.title)from QuestionPoolData a where a.questionPoolId  " +
	    		                              "in (select ac.questionPoolId from QuestionPoolAccessData ac where agentId= ?)");
	    		q.setString(0, agentId);
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

//	  List list = getHibernateTemplate().find(
//        "select new QuestionPoolData(a.questionPoolId, a.title)from QuestionPoolData a where a.ownerId= ? ",
//        new Object[] {agentId}
//        , new org.hibernate.type.Type[] {Hibernate.STRING});
    ArrayList poolList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      QuestionPoolData a = (QuestionPoolData) list.get(i);
      QuestionPoolFacade f = new QuestionPoolFacade(a.getQuestionPoolId(),
          a.getTitle());
      poolList.add(f);
    }
    return poolList;
  }

  private QuestionPoolFacade getQuestionPool(QuestionPoolData qpp) {
    try {
      return new QuestionPoolFacade(qpp);
    }
    catch (Exception e) {
      log.warn(e.getMessage());
      return null;
    }
  }

  private List getAllItemsInThisPoolOnlyAndDetachFromAssessment(final Long questionPoolId) {
  // return items that belong to this pool and this pool only.  These items can not be part of any assessment either.
    List list = getAllItemsInThisPoolOnly(questionPoolId);
    ArrayList newlist = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      if (itemdata.getSection()==null ) {
      // these items do not belong to any assessments, so add them to the list
       newlist.add(itemdata);
      }
      else {
      // do not add these items to the list, but we need to remove the POOLID metadata

       // this item still links to an assessment 
       // remove this item's POOLID itemmetadata
       itemdata.removeMetaDataByType(ItemMetaDataIfc.POOLID);
       getHibernateTemplate().saveOrUpdate(itemdata);  //save itemdata after removing metadata 
      }
    }
    return newlist;
  }


  private List getAllItemsInThisPoolOnly(final Long questionPoolId) {
  // return items that belong to this pool and this pool only.  
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?");
	    		q.setLong(0, questionPoolId.longValue());
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

    ArrayList newlist = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      String itemId = itemdata.getItemId().toString();
     if (getPoolIdsByItem(itemId).size() == 1) {
       newlist.add(itemdata);
     }
     else {
       // this item still links to other pool(s)
     } 
    }
    return newlist;
  }


  public List getAllItems(final Long questionPoolId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?");
	    		q.setLong(0, questionPoolId.longValue());
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?",
//                                            new Object[] {questionPoolId}
//                                            ,
//                                            new org.hibernate.type.Type[] {Hibernate.
//                                            LONG});
    return list;

  }

  	public List getAllItemFacadesOrderByItemText(final Long questionPoolId,
						     final String orderBy, final String ascending) {
	    
	  	// Fixed for bug 3559
	    log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: orderBy=" + orderBy);  
	    List list = getAllItems(questionPoolId);

	    log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: size = " + list.size());
	    HashMap hp = new HashMap();
	    Vector origValueV;
	    ItemData itemData;
	    ItemFacade itemFacade;
	    Vector facadeVector = new Vector();
	    String text;
	    for (int i = 0; i < list.size(); i++) {
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: i = " + i);
	    	itemData = (ItemData) list.get(i);
	    	itemFacade = new ItemFacade(itemData);
	    	facadeVector.add(itemFacade);
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: getItemId = " + itemData.getItemId());
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: getText = " + itemData.getText());
	    	text = itemFacade.getTextHtmlStrippedAll();
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: getTextHtmlStrippedAll = '" + text + "'");
	    	
	    	origValueV = (Vector) hp.get(text);
	    	if (origValueV == null) {
	    		log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: origValueV is null ");
	    		origValueV = new Vector();
	    	}
	    	origValueV.add( Integer.valueOf(i));
	    	hp.put(text, origValueV);
	    }
    
	    Vector v = new Vector(hp.keySet());
	    Collections.sort(v, String.CASE_INSENSITIVE_ORDER);
	    ArrayList itemList = new ArrayList();
    
	    Iterator it = v.iterator();
	    Vector orderdValueV;
	    Integer value;
            String key;
            if( (ascending!=null)&&("false").equals(ascending))
		{//sort descending
		for(int l=v.size()-1;l>=0;l--){
		    key =  (String)v.get(l);
		    orderdValueV = (Vector) hp.get(key);
		    Iterator iter = orderdValueV.iterator();
		    while (iter.hasNext()) {
			value =  (Integer)iter.next();
    	 
			ItemData itemdata = (ItemData) list.get(value.intValue());
			ItemFacade f = new ItemFacade(itemdata);
			itemList.add(f);
		    }
		}
		}
	    else{//sort ascending
		while (it.hasNext()) {
		    key =  (String)it.next();
		    orderdValueV = (Vector) hp.get(key);
		    Iterator iter = orderdValueV.iterator();
		    while (iter.hasNext()) {
	    		value =  (Integer)iter.next();
	    		log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: sorted (value) = " + value);
	    		itemFacade = (ItemFacade) facadeVector.get(value.intValue());
	    		itemList.add(itemFacade);
		    }
		}
	    }
	    return itemList;
  	}

  public List getAllItemFacadesOrderByItemType(final Long questionPoolId,
                                               final String orderBy, final String ascending) {
	  log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemType:: orderBy=" + orderBy);
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
		    Query q;
                    if("false".equals(ascending)){
		         q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi, TypeD t where ab.itemId=qpi.itemId and ab.typeId=t.typeId and qpi.questionPoolId = ? order by t." +
                        orderBy + " desc");
		    }
		    else{
	    	     	q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi, TypeD t where ab.itemId=qpi.itemId and ab.typeId=t.typeId and qpi.questionPoolId = ? order by t." +
                        orderBy);
		    }
                       
	    	   q.setLong(0, questionPoolId.longValue());
	    	   log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemType:: getQueryString() = " + q.getQueryString());
	    	   return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi, TypeD t where ab.itemId=qpi.itemId and ab.typeId=t.typeId and qpi.questionPoolId = ? order by t." +
//                                            orderBy,
//                                            new Object[] {questionPoolId}
//                                            ,
//                                            new org.hibernate.type.Type[] {Hibernate.
//                                            LONG});
	    log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemType:: size = " + list.size());
    ArrayList itemList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      ItemFacade f = new ItemFacade(itemdata);
      itemList.add(f);
    }
    return itemList;
  }

  public List getAllItemFacades(final Long questionPoolId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?");
	    		q.setLong(0, questionPoolId.longValue());
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?",
//                                            new Object[] {questionPoolId}
//                                            ,
//                                            new org.hibernate.type.Type[] {Hibernate.
//                                            LONG});
    ArrayList itemList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      ItemFacade f = new ItemFacade(itemdata);
      itemList.add(f);
    }
    return itemList;

  }

  private void populateQuestionPoolItemDatas(QuestionPoolData qpp) {
    try {
      Set questionPoolItems = qpp.getQuestionPoolItems();
      if (questionPoolItems != null) {
    	  
        // let's get all the items for the specified pool in one shot
        HashMap h = new HashMap();
        List itemList = getAllItems(qpp.getQuestionPoolId());

        Iterator j = itemList.iterator();
        while (j.hasNext()) {
          ItemData itemData = (ItemData) j.next();
          h.put(itemData.getItemIdString(), itemData);
        }
        ArrayList itemArrayList = new ArrayList();
        Iterator i = questionPoolItems.iterator();
        while (i.hasNext()) {
          QuestionPoolItemData questionPoolItem = (QuestionPoolItemData) i.next();
          ItemData itemData_0 = (ItemData) h.get(questionPoolItem.getItemId());
          /*
          Set itemTextSet = itemData_0.getItemTextSet();
          Iterator k = itemTextSet.iterator();
          while (k.hasNext()) {
            ItemText itemText = (ItemText) k.next();
          }
          */
          itemArrayList.add(itemData_0);
        }
        qpp.setQuestions(itemArrayList);
        qpp.setSubPoolSize( Integer.valueOf(getSubPoolSize(qpp.getQuestionPoolId())));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
       * This method returns an ItemFacade that we can use to construct our ItemImpl
   */
  public ItemFacade getItem(String id) {
    ItemData item = (ItemData) getHibernateTemplate().load(ItemData.class, id);
    return new ItemFacade(item);
  }

  /**
   * Get a pool based on poolId. I am not sure why agent is not used though is being parsed.
   *
   * @param poolid DOCUMENTATION PENDING
   * @param agent DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public QuestionPoolFacade getPool(Long poolId, String agent) {
    try {
      QuestionPoolData qpp = (QuestionPoolData) getHibernateTemplate().load(
          QuestionPoolData.class, poolId);
      // setAccessType
      setPoolAccessType(qpp, agent);
      // QuestionPoolItemData's identifier is a compsite identifier made up of
      // poolId and itemId <-- is regarded as "legacy DB" in Hibernate language.
      // We need to construct the properties for such as object ourselves.
      populateQuestionPoolItemDatas(qpp);
      return getQuestionPool(qpp);
    }
    catch (Exception e) {
      log.error(e);
      return null;
    }
  }

  public void setPoolAccessType(QuestionPoolData qpp, String agentId) {
    try {
      QuestionPoolAccessData qpa = getQuestionPoolAccessData(qpp.
          getQuestionPoolId(), agentId);
      if (qpa == null) {
        // if (qpa == null), take what is set for pool.
      }
      else {
        qpp.setAccessTypeId(qpa.getAccessTypeId());
      }
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
  }

  public QuestionPoolAccessData getQuestionPoolAccessData(final Long poolId,
      final String agentId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("from QuestionPoolAccessData as qpa where qpa.questionPoolId =? and qpa.agentId=?");
	    		q.setLong(0, poolId.longValue());
	    		q.setString(1, agentId);
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);

//    List list = getHibernateTemplate().find("from QuestionPoolAccessData as qpa where qpa.questionPoolId =? and qpa.agentId=?",
//                                            new Object[] {poolId, agentId}
//                                            ,
//                                            new org.hibernate.type.Type[] {Hibernate.
//                                            LONG, Hibernate.STRING});
    return (QuestionPoolAccessData) list.get(0);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ids DOCUMENTATION PENDING
   * @param sectionId DOCUMENTATION PENDING
   */
  public void addItemsToSection(Collection ids, long sectionId) {

  }

  /**
   * add a question to a pool
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void addItemToPool(QuestionPoolItemData qpi) {
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(qpi);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving item to pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }


  }

  /**
   * Delete pool and questions attached to it plus any subpool under it
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void deletePool(final Long poolId, final String agent, Tree tree) {	  
    try {
        QuestionPoolData questionPool = (QuestionPoolData) getHibernateTemplate().load(QuestionPoolData.class, poolId);

      // #1. delete all questions which mean AssetBeanie (not ItemImpl) 'cos AssetBeanie
      // is the one that is associated with the DB
      // lydial:  getting list of items that only belong to this pool and not linked to any assessments. 
      List itemList = getAllItemsInThisPoolOnlyAndDetachFromAssessment(poolId);

      int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
      while (retryCount > 0){
        try {
          getHibernateTemplate().deleteAll(itemList); // delete all AssetBeanie
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete all items in pool: "+e.getMessage());
          retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
        }
      }


      // #2. delete question and questionpool map.
      retryCount = PersistenceService.getInstance().getRetryCount().intValue();
      while (retryCount > 0){
        try {
          final HibernateCallback hcb = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.createQuery("select qpi from QuestionPoolItemData as qpi where qpi.questionPoolId= ?");
              q.setLong(0, poolId.longValue());
              return q.list();
    	    };
          };
          List list = getHibernateTemplate().executeFind(hcb);

          // a. delete item and pool association in SAM_ITEMMETADATA_T - this is the primary
          // pool that item is attached to
          ArrayList metaList = new ArrayList();
          for (int j=0; j<list.size(); j++){
            String itemId = ((QuestionPoolItemData)list.get(j)).getItemId();
            String query = "from ItemMetaData as meta where meta.item.itemId=? and meta.label=?";
            Object [] values = {Long.valueOf(itemId), ItemMetaDataIfc.POOLID};
    	    List m = getHibernateTemplate().find(query, values);
            if (m.size()>0){
              ItemMetaDataIfc meta = (ItemMetaDataIfc)m.get(0);
              meta.setEntry(null);
	    }
          }
          try{
            getHibernateTemplate().saveOrUpdateAll(metaList);
            retryCount = 0;
	  }
          catch (DataAccessException e) {
            log.warn("problem delete question and questionpool map inside itemMetaData: "+e.getMessage());
            retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
          }

          // b. delete item and pool association in SAM_QUESTIONPOOLITEM_T
          if (list.size() > 0) {
            questionPool.setQuestionPoolItems(new HashSet());
            getHibernateTemplate().deleteAll(list);
            retryCount = 0;
          }
          else retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete question and questionpool map: "+e.getMessage());
          retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
        }
      }

      // #3. Pool is owned by one but can be shared by multiple agents. So need to
      // delete all QuestionPoolAccessData record first. This seems to be missing in Navigo, nope? - daisyf
      // Actually, I don't think we have ever implemented sharing between agents. So we may wnat to
      // clean up this bit of code - daisyf 07/07/06
      // #3a. Delete all shared pool by him sons
      final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Query q = session.createQuery("select qpa from QuestionPoolAccessData as qpa, QuestionPoolData as qpp " +
        		  						"where qpa.questionPoolId = qpp.questionPoolId and (qpp.questionPoolId=? or qpp.parentPoolId=?) ");
          q.setLong(0, poolId.longValue());
          q.setLong(1, poolId.longValue());
          return q.list();
    	};
      };
      List qpaList = getHibernateTemplate().executeFind(hcb);
      retryCount = PersistenceService.getInstance().getRetryCount().intValue();
      while (retryCount > 0){
        try {
          getHibernateTemplate().deleteAll(qpaList);
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete question pool access data: "+e.getMessage());
          retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
        }
      }

      // #4. Ready! delete pool now
      final HibernateCallback hcb2 = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("select qp from QuestionPoolData as qp where qp.id= ?");
    		q.setLong(0, poolId.longValue());
    		return q.list();
    	};
      };
      List qppList = getHibernateTemplate().executeFind(hcb2);
      retryCount = PersistenceService.getInstance().getRetryCount().intValue();
      while (retryCount > 0){
        try {
          getHibernateTemplate().deleteAll(qppList);
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete all pools: "+e.getMessage());
          retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
        }
      }

      // #5. delete all subpools if any, this is recursive
      Iterator citer = (tree.getChildList(poolId)).iterator();
      while (citer.hasNext()) {
        deletePool( (Long) citer.next(), agent, tree);
      }
    }
    catch (DataAccessException e) {
      log.warn("error deleting pool. " + e.getMessage());
    }
  }

  /**
   * Move pool under another pool. The dest pool must not be the
   * descendant of the source nor can they be the same pool .
   */
  public void movePool(String agentId, Long sourcePoolId, Long destPoolId) {
    try {
      QuestionPoolFacade sourcePool = getPool(sourcePoolId, agentId);
      if (destPoolId.equals(QuestionPoolFacade.ROOT_POOL) &&
          !sourcePoolId.equals(QuestionPoolFacade.ROOT_POOL)) {
        sourcePool.setParentPoolId(QuestionPoolFacade.ROOT_POOL);
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().update( (QuestionPoolData) sourcePool.getData());
        retryCount = 0;
      }
      catch (DataAccessException e) {
        log.warn("problem moving pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
      }
      else {
        QuestionPoolFacade destPool = getPool(destPoolId, agentId);
        sourcePool.setParentPoolId(destPool.getQuestionPoolId());
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().update( (QuestionPoolData) sourcePool.getData());
        retryCount = 0;
      }
      catch (DataAccessException e) {
        log.warn("problem update source pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
      }
    }
    catch (RuntimeException e) {
      log.warn(e.getMessage());
    }
  }

  /**
   * Is destination a descendant of the source?
   */
  public boolean isDescendantOf(QuestionPoolFacade destPool,
                                QuestionPoolFacade sourcePool) {

    Long tempPoolId = destPool.getQuestionPoolId();
    try {
      while((tempPoolId != null) &&
        (!tempPoolId.equals(QuestionPoolFacade.ROOT_POOL)))
      {
        QuestionPoolFacade tempPool = getPoolById(tempPoolId);
        if (tempPool.getParentPoolId().equals(sourcePool.getQuestionPoolId())) {
          return true;
        }
      tempPoolId = tempPool.getParentPoolId();
      }
      return false;
    }
    catch (Exception e) {
      log.warn(e.getMessage());
      return false;
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void removeItemFromPool(String itemId, Long poolId) {
    QuestionPoolItemData qpi = new QuestionPoolItemData(poolId, itemId);
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(qpi);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete item from pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void moveItemToPool(String itemId, Long sourceId, Long destId) {
    QuestionPoolItemData qpi = new QuestionPoolItemData(sourceId, itemId);
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(qpi);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete old mapping: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    QuestionPoolItemData qpi2 = new QuestionPoolItemData(destId, itemId);
    retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(qpi2);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving new mapping: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pool DOCUMENTATION PENDING
   */
  public QuestionPoolFacade savePool(QuestionPoolFacade pool) {
    boolean insert = false;
    try {
      QuestionPoolData qpp = (QuestionPoolData) pool.getData();
      qpp.setLastModified(new Date());
      qpp.setLastModifiedById(AgentFacade.getAgentString());
      int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
      if (qpp.getQuestionPoolId() == null ||
          qpp.getQuestionPoolId().equals(new Long("0"))) { // indicate a new pool
        insert = true;
      }
      while (retryCount > 0){
        try {
          getHibernateTemplate().saveOrUpdate(qpp);
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem saving Or Update pool: "+e.getMessage());
          retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
        }
      }

      if (insert) {
        // add a QuestionPoolAccessData record for the owner who should have ADMIN access to the pool
        QuestionPoolAccessData qpa = new QuestionPoolAccessData(qpp.
            getQuestionPoolId(), qpp.getOwnerId(), QuestionPoolData.ADMIN);
        retryCount = PersistenceService.getInstance().getRetryCount().intValue();
        while (retryCount > 0){
          try {
            getHibernateTemplate().save(qpa);
            retryCount = 0;
          }
          catch (DataAccessException e) {
            log.warn("problem saving pool: "+e.getMessage());
            retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
          }
        }
        
        // add a QuestionPoolAccessData record for all users who are sharing the subpool
        final long parentPoolId = qpp.getParentPoolId();
        final String ownerId = qpp.getOwnerId();

        if (parentPoolId != 0) {
        	List<QuestionPoolAccessData> listSubpool = new ArrayList();
        	try {
        		listSubpool = getHibernateTemplate().find("from QuestionPoolAccessData as qpa where qpa.questionPoolId=? and qpa.agentId<>?", 
        				new Object[] { Long.valueOf(parentPoolId), ownerId});
        	} catch (Exception e1) {
        		log.warn("problem finding pool: "+e1.getMessage());
        	}
        	for (QuestionPoolAccessData questioPoolData : listSubpool) {
        		qpa = new
        		QuestionPoolAccessData(qpp.getQuestionPoolId(),
        				questioPoolData.getAgentId(), QuestionPoolData.READ_COPY);
        		retryCount =
        			PersistenceService.getInstance().getRetryCount().intValue();
        		while (retryCount > 0){
        			try {
        				getHibernateTemplate().save(qpa);
        				retryCount = 0;
        			}
        			catch (DataAccessException e) {
        				log.warn("problem saving pool: "+e.getMessage());
        				retryCount =
        					PersistenceService.getInstance().retryDeadlock(e, retryCount);
        			}
        		}
        	}
        }
      }
      return pool;
    }
    catch (RuntimeException e) {
      log.warn(e.getMessage());
      return null;
    }
  }

  /**
   * Get all the children pools of a pool. Return a list of QuestionPoolData
   * should return QuestionPool instead - need fixing, daisyf
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getSubPools(final Long poolId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("from QuestionPoolData as qpp where qpp.parentPoolId=?");
	    		q.setLong(0, poolId.longValue());
	    		return q.list();
	    	};
	    };
	    return getHibernateTemplate().executeFind(hcb);
  }

  // get number of subpools for each pool in a single query.
  // returns a List of arrays. Each array is 0: poolid, 1: count of subpools
  // both are BigInteger.
  public List getSubPoolSizes(final String agent) {
	  final HibernateCallback hcb = new HibernateCallback(){
		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
			  //SQLQuery q = session.createSQLQuery("select a.QUESTIONPOOLID,(select count(*) from SAM_QUESTIONPOOL_T b where b.PARENTPOOLID=a.QUESTIONPOOLID) from SAM_QUESTIONPOOL_T a where a.OWNERID=?");
			  Query q = session.createQuery("select a.questionPoolId, (select count(*) from QuestionPoolData b where b.parentPoolId=a.questionPoolId) " +
			  		"from QuestionPoolData a where a.ownerId=?");
			  q.setString(0, agent);
			  return q.list();
		  };
	  };
	  return getHibernateTemplate().executeFind(hcb);
  }

  //number of subpools for this pool. But consider getSubPoolSizes if you're going to 
  // need this for all the pools.
  public int getSubPoolSize(final Long poolId) {
	  final HibernateCallback hcb = new HibernateCallback(){
		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
			  Query q = session.createQuery("select count(qpp) from QuestionPoolData qpp where qpp.parentPoolId=?");
			  q.setLong(0, poolId.longValue());
			  return q.uniqueResult();
		  };
	  };
	  
	  Integer count = (Integer)getHibernateTemplate().execute(hcb);	    
	  return count.intValue();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  // Note that this is going to do a database query. If you need to do this
  // for lots of pools consider doing getSubPoolSizes, saving the results
  // and then testing.
  public boolean hasSubPools(final Long poolId) {
	  int poolSize = getSubPoolSize(poolId);
	  if (poolSize >= 0) 
		  return true;
	  else
		  return false;
  }

  public boolean poolIsUnique(final Long questionPoolId, final String title, final Long parentPoolId, final String agentId) {
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("select new QuestionPoolData(a.questionPoolId, a.title, a.parentPoolId)from QuestionPoolData a where a.questionPoolId!= ? and a.title=? and a.parentPoolId=? and a.ownerId = ? ");
    		q.setLong(0, questionPoolId.longValue());
    		q.setString(1, title);
    		q.setLong(2, parentPoolId.longValue());
    		q.setString(3, agentId);
    		return q.list();
    	};
    };
    List list = getHibernateTemplate().executeFind(hcb);

//     List list = getHibernateTemplate().find(
//        "select new QuestionPoolData(a.questionPoolId, a.title, a.parentPoolId)from QuestionPoolData a where a.questionPoolId!= ? and a.title=? and a.parentPoolId=?",
//        new Object[] {questionPoolId,title,parentPoolId}
//       , new org.hibernate.type.Type[] {Hibernate.LONG,Hibernate.STRING, Hibernate.LONG});
    boolean isUnique = true;
    if(list.size()>0) {
     // query in mysql & hsqldb are not case sensitive, check that title found is indeed what we
     // are looking (SAK-3110)
    	for (int i=0; i<list.size();i++){  
    		QuestionPoolData q = (QuestionPoolData) list.get(i);
    		if ((title).equals(q.getTitle().trim())){
    			isUnique = false;
    			break;
    		}
    	}
    }
    return isUnique;
  }


  /**
   * Return a list of questionPoolId (java.lang.Long)
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getPoolIdsByAgent(final String agentId) {
    ArrayList idList = new ArrayList();

    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("select qpa from QuestionPoolAccessData as qpa where qpa.agentId= ?");
    		q.setString(0, agentId);
    		return q.list();
    	};
    };
    List qpaList = getHibernateTemplate().executeFind(hcb);

//    List qpaList = getHibernateTemplate().find(
//        "select qpa from QuestionPoolAccessData as qpa where qpa.agentId= ?",
//        new Object[] {agentId}
//        , new org.hibernate.type.Type[] {Hibernate.STRING});
    try {
      Iterator iter = qpaList.iterator();
      while (iter.hasNext()) {
        QuestionPoolAccessData qpa = (QuestionPoolAccessData) iter.next();
        idList.add(qpa.getQuestionPoolId()); // return a list of poolId (java.lang.Long)
      }
      return idList;
    }
    catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Return a list of questionPoolId (java.lang.Long)
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getPoolIdsByItem(final String itemId) {
    ArrayList idList = new ArrayList();
    
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery("select qpi from QuestionPoolItemData as qpi where qpi.itemId= ?");
    		q.setString(0, itemId);
    		return q.list();
    	};
    };
    List qpiList = getHibernateTemplate().executeFind(hcb);

//    List qpiList = getHibernateTemplate().find(
//        "select qpi from QuestionPoolItemData as qpi where qpi.itemId= ?",
//        new Object[] {itemId}
//        , new org.hibernate.type.Type[] {Hibernate.STRING});
    try {
      Iterator iter = qpiList.iterator();
      while (iter.hasNext()) {
        QuestionPoolItemData qpa = (QuestionPoolItemData) iter.next();
        idList.add(qpa.getQuestionPoolId()); // return a list of poolId (java.lang.Long)
      }
      return idList;
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Copy a pool to a new location.
   */
  public void copyPool(Tree tree, String agentId, Long sourceId,
                       Long destId, String prependString1, String prependString2) {
    try {
      boolean haveCommonRoot = false;
      boolean duplicate = false;

      // Get the Pools
      QuestionPoolFacade oldPool = getPool(sourceId, agentId);
      String oldPoolName= oldPool.getDisplayName();

      // Are we creating a duplicate under the same parent?
      if (destId.equals(oldPool.getParentPoolId())) {
        duplicate = true;
      }

      // Determine if the Pools are in the same tree
      // If so, make sure the source level is not higher(up the tree)
      // than the dest. to avoid the endless loop.
      if (!duplicate) {
        haveCommonRoot = tree.haveCommonRoot(sourceId, destId);
      }

      if (haveCommonRoot &&
          (tree.poolLevel(sourceId) <=
           tree.poolLevel(destId))) {
        return; // Since otherwise it would cause an infinite loop.
        // We should revisit this.
      }

      QuestionPoolFacade newPool = (QuestionPoolFacade) oldPool.clone();
      newPool.setParentPoolId(destId);
      newPool.setQuestionPoolId( Long.valueOf(0));
      newPool.setOwnerId(AgentFacade.getAgentString());

      // If Pools in same trees,
      if (!haveCommonRoot) {
        // If Pools in different trees,
        // Copy to a Pool outside the same root
        // Copy *this* Pool first
        if (duplicate) 
          resetTitle(destId, newPool, oldPoolName, prependString1, prependString2);
        else 
          newPool.updateDisplayName(oldPoolName);
      }

      newPool = savePool(newPool);
      Iterator iter = oldPool.getQuestions().iterator();
      ArrayList itemDataArray = new ArrayList();
      while (iter.hasNext()) {
    	  ItemDataIfc itemData = (ItemDataIfc) iter.next();
    	  ItemFacade itemFacade = copyItemFacade2(itemData);
    	  ItemDataIfc newItemData = itemFacade.getData();
    	  itemDataArray.add(newItemData);
      }
      
      // then save question to pool
      newPool.setQuestionPoolItems(prepareQuestions(newPool.getQuestionPoolId(), itemDataArray));
      newPool.setQuestions(itemDataArray);
      newPool = savePool(newPool);

      // Get the SubPools of oldPool
      Iterator citer = (tree.getChildList(sourceId)).iterator();
      while (citer.hasNext()) {
        Long childPoolId = (Long) citer.next();
        copyPool(tree, agentId, childPoolId, newPool.getQuestionPoolId(), prependString1, prependString2);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
  public static void main(String[] args) throws DataFacadeException {
    QuestionPoolFacadeQueriesAPI instance = new QuestionPoolFacadeQueries();
    // add an item
    if (args[0].equals("add")) {
      Long questionPoolId = instance.add();
    }
    if (args[0].equals("getQPItems")) {
      List items = instance.getAllItems(new Long(args[1])); // poolId
      for (int i = 0; i < items.size(); i++) {
        ItemData item = (ItemData) items.get(i);
      }
    }
    System.exit(0);
  }
  */
  
  public Long add() {
    QuestionPoolData questionPool = new QuestionPoolData();
    questionPool.setTitle("Daisy Happy Pool");
    questionPool.setOwnerId("1");
    questionPool.setDateCreated(new Date());
    questionPool.setLastModifiedById("1");
    questionPool.setLastModified(new Date());
    getHibernateTemplate().save(questionPool);
    return questionPool.getQuestionPoolId();
  }

  public QuestionPoolFacade getPoolById(Long questionPoolId) {
    QuestionPoolFacade questionPoolFacade = null;
    try {
      if (!questionPoolId.equals(QuestionPoolFacade.ROOT_POOL)) {
        QuestionPoolData questionPool = (QuestionPoolData) getHibernateTemplate().
            load(QuestionPoolData.class, questionPoolId);
        if (questionPool != null) {
          questionPoolFacade = new QuestionPoolFacade(questionPool);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return questionPoolFacade;
  }

  public HashMap getQuestionPoolItemMap(){
    HashMap h = new HashMap();
    String query = "from QuestionPoolItemData";
    List l = getHibernateTemplate().find(query);
    for (int i = 0; i < l.size(); i++) {
      QuestionPoolItemData q = (QuestionPoolItemData) l.get(i);
      h.put(q.getItemId(), q);
    }
    return h;
  }

  public HashSet prepareQuestions(Long questionPoolId, ArrayList itemDataArray){
    HashSet set = new HashSet();
    Iterator iter = itemDataArray.iterator();
    while (iter.hasNext()){
      ItemDataIfc itemData = (ItemDataIfc) iter.next();
      set.add(new QuestionPoolItemData(questionPoolId, itemData.getItemIdString(), (ItemData) itemData));
    }
    return set;
  }
  /*
  public HashSet prepareQuestions(Long questionPoolId, Set questionSet){
    HashSet set = new HashSet();
    Iterator iter = questionSet.iterator();
    while (iter.hasNext()){
      QuestionPoolItemData i = (QuestionPoolItemData)iter.next();
      set.add(new QuestionPoolItemData(questionPoolId, i.getItemId()));
    }
    return set;
  }
  */

  private void resetTitle(Long destId, QuestionPoolFacade newPool, String oldPoolName, String prependString1, String prependString2){
    //find name by loop through sibslings
    List siblings=getSubPools(destId);
    int num=0;
    int startIndex = 0;
    int endIndex = 0;
    int maxNum=0;
	StringBuilder prependString = new StringBuilder(prependString1);
	prependString.append(" ");
	prependString.append(prependString2);
	prependString.append(" ");
    for (int l = 0; l < siblings.size(); l++) {
       QuestionPoolData a = (QuestionPoolData)siblings.get(l);
       String n=a.getTitle();
       if(n.startsWith(prependString.toString())){
         if(n.equals(prependString + oldPoolName)){
           if (maxNum<1) maxNum=1;
         }
       }
       if(n.startsWith(prependString1 + "(")){
    	 startIndex = n.indexOf("(");
    	 endIndex=n.indexOf(")");
    	 try{
    		 String partialPoolName = n.substring(endIndex + 2).replaceFirst(prependString2 + " ", "").trim();
    		 num = Integer.parseInt(n.substring(startIndex + 1, endIndex));
             if(oldPoolName.equals(partialPoolName)){
               if (num>maxNum) maxNum=num;
             }
    	 }
    	 catch(NumberFormatException e){
             log.warn("rename title of duplicate pool:"+ e.getMessage());
    	 }
       }
    }
 
   if(maxNum==0)
     newPool.updateDisplayName(prependString + oldPoolName);
   else
     newPool.updateDisplayName(prependString1 + "(" + (maxNum+1) + ") " + prependString2 + " " + oldPoolName);
  }
  
  public Long copyItemFacade(ItemDataIfc itemData) {
	  ItemFacade item = getItemFacade(itemData);
      ItemService itemService = new ItemService();
	  Long itemId = itemService.saveItem(item).getItemId();

      return itemId;
  }
  
  public ItemFacade copyItemFacade2(ItemDataIfc itemData) {
	  ItemFacade item = getItemFacade(itemData);
      ItemService itemService = new ItemService();
      return itemService.saveItem(item);
  }
	    
  private ItemFacade getItemFacade(ItemDataIfc itemData) {
	  ItemFacade item = new ItemFacade();
	  item.setScore(itemData.getScore());
	  item.setDiscount(itemData.getDiscount());
      item.setHint(itemData.getHint());
      item.setStatus(itemData.getStatus());
      item.setTypeId(itemData.getTypeId());
      item.setCreatedBy(AgentFacade.getAgentString());
      item.setCreatedDate(new Date());
      item.setLastModifiedBy(AgentFacade.getAgentString());
      item.setLastModifiedDate(new Date());
      item.setInstruction(itemData.getInstruction());
      item.setHasRationale(itemData.getHasRationale());
      item.setTriesAllowed(itemData.getTriesAllowed());
      item.setDuration(itemData.getDuration());

      item.setItemTextSet(copyItemText(item.getData(), itemData));
      item.setItemMetaDataSet(copyMetaData(item.getData(), itemData));
      item.setItemAttachmentSet(copyAttachment(item.getData(), itemData));

      if (itemData.getCorrectItemFeedback() != null && !itemData.getCorrectItemFeedback().equals("")) {
    	  item.setCorrectItemFeedback(itemData.getCorrectItemFeedback());
      }
      if (itemData.getInCorrectItemFeedback() != null && !itemData.getInCorrectItemFeedback().equals("")) {
    	  item.setInCorrectItemFeedback(itemData.getInCorrectItemFeedback());
      }
      if (itemData.getGeneralItemFeedback() != null && !itemData.getGeneralItemFeedback().equals("")) {
    	  item.setGeneralItemFeedback(itemData.getGeneralItemFeedback());
      }
      
      return item;
  }

  private HashSet copyItemText(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	    HashSet toItemTextSet = new HashSet();
	    Set fromItemTextSet = fromItemData.getItemTextSet();
	    Iterator itemTextIter = fromItemTextSet.iterator();
	      while (itemTextIter.hasNext()) {
	    	  ItemText fromItemText = (ItemText) itemTextIter.next();
	    	  ItemText toItemText = new ItemText();
	    	  toItemText.setItem(toItemData);
	    	  toItemText.setSequence(fromItemText.getSequence());
	    	  toItemText.setText(fromItemText.getText());
	    	  
	    	  HashSet toAnswerSet = new HashSet();
	    	  Set fromAnswerSet = fromItemText.getAnswerSet();
	    	  Iterator answerIter = fromAnswerSet.iterator();
	    	  while (answerIter.hasNext()) {
	    		  Answer fromAnswer = (Answer) answerIter.next();
	    		  Answer toAnswer = new Answer(toItemText, fromAnswer.getText(), fromAnswer.getSequence(), fromAnswer.getLabel(),
	    				  fromAnswer.getIsCorrect(), fromAnswer.getGrade(), fromAnswer.getScore(), fromAnswer.getPartialCredit(), fromAnswer.getDiscount());
	    		  
	    		  HashSet toAnswerFeedbackSet = new HashSet();
	    		  Set fromAnswerFeedbackSet = fromAnswer.getAnswerFeedbackSet();
	    		  Iterator answerFeedbackIter = fromAnswerFeedbackSet.iterator();
	    		  while (answerFeedbackIter.hasNext()) {
	    			  AnswerFeedback fromAnswerFeedback = (AnswerFeedback) answerFeedbackIter.next();
	    			  toAnswerFeedbackSet.add(new AnswerFeedback(toAnswer, fromAnswerFeedback.getTypeId(), fromAnswerFeedback.getText()));
	    			  toAnswer.setAnswerFeedbackSet(toAnswerFeedbackSet);
	    		  }
	    		  toAnswerSet.add(toAnswer);
	    		  toItemText.setAnswerSet(toAnswerSet);
	    	  }
	    	  toItemTextSet.add(toItemText);
	      }
	      return toItemTextSet;
}
  
  private HashSet copyMetaData(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	    HashSet toSet = new HashSet();
	    Set fromSet = fromItemData.getItemMetaDataSet();
	    Iterator iter = fromSet.iterator();
	    while (iter.hasNext()) {
	    	ItemMetaData itemMetaData = (ItemMetaData) iter.next();
	    	toSet.add(new ItemMetaData(toItemData, itemMetaData.getLabel(), itemMetaData.getEntry()));
	    }
	    return toSet;
  }
  
  private Set copyAttachment(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	  AssessmentService assessmentService = new AssessmentService();
	  Set toSet = assessmentService.copyItemAttachmentSet((ItemData) toItemData, fromItemData.getItemAttachmentSet());
	    
	  return toSet;
  }
  
  public Integer getCountItemFacades(final Long questionPoolId) {	    
	  final HibernateCallback hcb = new HibernateCallback(){
		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
			  Query q = session.createQuery("select count(ab) from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?");
			  q.setLong(0, questionPoolId.longValue());
			  return q.uniqueResult();
		  };
	  };
	  	    
	  Integer count = (Integer)getHibernateTemplate().execute(hcb);	    
	  return count;
  }

  /**
   * Shared Pools with other user
   */
  public void addQuestionPoolAccess(String user, final Long questionPoolId, Long accessTypeId) {	  
	  QuestionPoolAccessData qpad = new QuestionPoolAccessData(questionPoolId, user, accessTypeId);

	  getHibernateTemplate().saveOrUpdate(qpad);
	  // We need to share all subpools of the shared pool
	  final HibernateCallback hcb = new HibernateCallback() {
		  public Object doInHibernate(Session session) throws
		  HibernateException, SQLException {
			  Query q = session.createQuery("select qp from QuestionPoolData as qp where qp.parentPoolId= ?");
			  q.setLong(0, questionPoolId.longValue());
			  return q.list();
		  };
	  };
	  List<QuestionPoolData> qpList =
		  getHibernateTemplate().executeFind(hcb);
	  for (QuestionPoolData pool : qpList) {
		  qpad = new QuestionPoolAccessData(pool.getQuestionPoolId(),
				  user, accessTypeId);
		  getHibernateTemplate().saveOrUpdate(qpad);
	  }
  }

  public void removeQuestionPoolAccess(String user, final Long questionPoolId, Long accessTypeId) {	  
	  QuestionPoolAccessData qpad = new QuestionPoolAccessData(questionPoolId, user, accessTypeId);

	  getHibernateTemplate().delete(qpad);

	  // We need to remove all subpools of the shared pool
	  final HibernateCallback hcb = new HibernateCallback() {
		  public Object doInHibernate(Session session) throws
		  HibernateException, SQLException {
			  Query q = session.createQuery("select qp from QuestionPoolData as qp where qp.parentPoolId= ?");
			  q.setLong(0, questionPoolId.longValue());
			  return q.list();
		  };
	  };
	  List<QuestionPoolData> qpList =
		  getHibernateTemplate().executeFind(hcb);
	  for (QuestionPoolData pool : qpList) {
		  qpad = new QuestionPoolAccessData(pool.getQuestionPoolId(),
				  user, accessTypeId);
		  getHibernateTemplate().delete(qpad);
	  }
  }

  public List<AgentFacade> getAgentsWithAccess(final Long questionPoolId) {
	  final HibernateCallback hcb = new HibernateCallback(){
		  public Object doInHibernate(Session session) throws HibernateException, SQLException {
			  Query q = session.createQuery("select qpa from QuestionPoolAccessData as qpa where qpa.questionPoolId= ?");
			  q.setLong(0, questionPoolId.longValue());
			  return q.list();
		  };
	  };
	  List<QuestionPoolAccessData> qpaList = getHibernateTemplate().executeFind(hcb);

	  List<AgentFacade> agents = new ArrayList();
	  for (QuestionPoolAccessData pool : qpaList) {
		  AgentFacade agent = new AgentFacade(pool.getAgentId());
		  agents.add(agent);
	  }

	  return agents;
  }
}
