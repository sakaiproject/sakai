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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
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
import org.sakaiproject.util.api.FormattedText;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

@Slf4j
public class QuestionPoolFacadeQueries
    extends HibernateDaoSupport implements QuestionPoolFacadeQueriesAPI {
  
  // SAM-2049
  private static final String VERSION_START = " - ";
  
  // SAM-2499
  private final FormattedText formattedText = (FormattedText) ComponentManager.get( FormattedText.class );

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
	    final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("from QuestionPoolData a  where a.questionPoolId in (select ac.questionPoolId from QuestionPoolAccessData ac where agentId = :agent) ");
            q.setString("agent", agentId);
            return q.list();
        };
	    List list = getHibernateTemplate().execute(hcb);

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
    List qpList = new ArrayList();

    // #1.
    // lydial: 9/22/05 we are not really using QuestionPoolAccessData, so filter by ownerid 
    //List poolList = getAllPools(); 
    HibernateCallback<List<QuestionPoolData>> hcb = session -> session
            .createQuery("from QuestionPoolData a where a.ownerId = :id")
            .setString("id", agentId)
            .list();
    List<QuestionPoolData> poolList = getHibernateTemplate().execute(hcb);
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
        Map<Long, Long> counts = getSubPoolSizes(agentId).stream().collect(Collectors.toMap(pool -> pool[0], pool -> pool[1]));

        for (QuestionPoolData qpp : poolList) {
    		// I really wish we don't need to populate  the questionpool size & subpool size for JSF
    		// watch this for performance. hope Hibernate is smart enough not to load the entire question
    		// - daisy, 10/04/04
    		// populateQuestionPoolItemDatas(qpp);
    		// lookup number of subpools for this pool in our handy hash table
    		Long subPoolSize = counts.get(qpp.getQuestionPoolId());
            qpp.setSubPoolSize(subPoolSize == null ? 0L : subPoolSize);

    		qpList.add(getQuestionPool(qpp));
    	}
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
    return new QuestionPoolIteratorFacade(qpList);
  }

  public QuestionPoolIteratorFacade getAllPoolsWithAccess(String agentId) {
	  List qpList = new ArrayList();

	  // First get the size of all pools in one query
      Map<Long, Long> counts = getSubPoolSizes(agentId).stream().collect(Collectors.toMap(pool -> pool[0], pool -> pool[1]));

	  List poolList = getAllPoolsByAgent(agentId); 

	  try {
		  Iterator j = poolList.iterator();
		  while (j.hasNext()) {
			  QuestionPoolData qpp = (QuestionPoolData) j.next();
			  qpp.setSubPoolSize(counts.get(qpp.getQuestionPoolId()));

			  qpList.add(getQuestionPool(qpp));
		  }
	  }
	  catch (Exception e) {
		  log.warn("Error in getAllPoolsWithAccess: " + e.getMessage(), e);
	  }
	  return new QuestionPoolIteratorFacade(qpList);
  }
  
  public List<QuestionPoolFacade> getBasicInfoOfAllPools(final String agentId) {
      final HibernateCallback<List> hcb = session -> {
          Query q = session.createQuery(
                  "select new QuestionPoolData(a.questionPoolId, a.title, a.parentPoolId)from QuestionPoolData a where a.questionPoolId  " +
                          "in (select ac.questionPoolId from QuestionPoolAccessData ac where agentId = :agent)");
          q.setString("agent", agentId);
          return q.list();
      };
      List list = getHibernateTemplate().execute(hcb);

    List<QuestionPoolFacade> poolList = new ArrayList<QuestionPoolFacade>();
    for (int i = 0; i < list.size(); i++) {
      QuestionPoolData a = (QuestionPoolData) list.get(i);
      QuestionPoolFacade f = new QuestionPoolFacade(a.getQuestionPoolId(), a.getTitle(), a.getParentPoolId());
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
    List newlist = new ArrayList();
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
	    final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = :id");
            q.setLong("id", questionPoolId);
            return q.list();
        };
	    List list = getHibernateTemplate().execute(hcb);

    List newlist = new ArrayList();
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
	    final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = :id order by ab.itemId");
            q.setLong("id", questionPoolId.longValue());
            return q.list();
        };
	    List list = getHibernateTemplate().execute(hcb);
        return list;
  }

    public List getAllItemsIds(final Long questionPoolId) {
        final HibernateCallback<List> hcb = session -> {
                Query q = session.createQuery("select qpi.itemId from QuestionPoolItemData qpi where qpi.questionPoolId = ?");
                q.setLong(0, questionPoolId.longValue());
                return q.list();
        };
        List list = getHibernateTemplate().execute(hcb);
        return list;

    }



  	public List getAllItemFacadesOrderByItemText(final Long questionPoolId,
						     final String orderBy, final String ascending) {
	    
	  	// Fixed for bug 3559
	    log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: orderBy = {}", orderBy);
	    List list = getAllItems(questionPoolId);

	    log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: size = {}", list.size());
	    Map hp = new HashMap();
	    Vector origValueV;
	    ItemData itemData;
	    ItemFacade itemFacade;
	    Vector facadeVector = new Vector();
	    String text;
	    for (int i = 0; i < list.size(); i++) {
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: i = {}", i);
	    	itemData = (ItemData) list.get(i);
	    	itemFacade = new ItemFacade(itemData);
	    	facadeVector.add(itemFacade);
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: getItemId = {}", itemData.getItemId());
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: getText = {}", itemData.getText());
	    	
	    	// SAM-2499
	    	text = formattedText.stripHtmlFromText(itemFacade.getText(), false, true);
	    	
	    	log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: getTextHtmlStrippedAll = '{}'", text);
	    	
	    	origValueV = (Vector) hp.get(text);
	    	if (origValueV == null) {
	    		log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: origValueV is null");
	    		origValueV = new Vector();
	    	}
	    	origValueV.add(i);
	    	hp.put(text, origValueV);
	    }
    
	    Vector v = new Vector(hp.keySet());
	    Collections.sort(v, String.CASE_INSENSITIVE_ORDER);
	    List itemList = new ArrayList();
    
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
    	 
			ItemData itemdata = (ItemData) list.get(value);
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
	    		log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemText:: sorted (value) = {}", value);
	    		itemFacade = (ItemFacade) facadeVector.get(value);
	    		itemList.add(itemFacade);
		    }
		}
	    }
	    return itemList;
  	}

  public List getAllItemFacadesOrderByItemType(final Long questionPoolId,
                                               final String orderBy, final String ascending) {
	  log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemType:: orderBy=" + orderBy);
      final HibernateCallback<List> hcb = session -> {
          Query q;
          if ("false".equals(ascending)) {
              q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi, TypeD t where ab.itemId=qpi.itemId and ab.typeId=t.typeId and qpi.questionPoolId = :id order by t." +
                      orderBy + " desc");
          } else {
              q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi, TypeD t where ab.itemId=qpi.itemId and ab.typeId=t.typeId and qpi.questionPoolId = :id order by t." +
                      orderBy);
          }

          q.setLong("id", questionPoolId);
          log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemType:: getQueryString() = " + q.getQueryString());
          return q.list();
      };
      List list = getHibernateTemplate().execute(hcb);

	    log.debug("QuestionPoolFacadeQueries: getAllItemFacadesOrderByItemType:: size = {}", list.size());
    List itemList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      ItemFacade f = new ItemFacade(itemdata);
      itemList.add(f);
    }
    return itemList;
  }

  public List getAllItemFacades(final Long questionPoolId) {
    final HibernateCallback<List> hcb = session -> {
        Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = :id order by ab.itemId");
        q.setLong("id", questionPoolId.longValue());
        return q.list();
    };
    List list = getHibernateTemplate().execute(hcb);

    List itemList = new ArrayList();
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
        List itemList = getAllItems(qpp.getQuestionPoolId());
        qpp.setQuestions(itemList);
        qpp.setSubPoolSize(getSubPoolSize(qpp.getQuestionPoolId()));
      }
    }
    catch (Exception e) {
        log.error(e.getMessage(), e);
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
      log.error(e.getMessage(), e);
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
	    final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("from QuestionPoolAccessData as qpa where qpa.questionPoolId = :id and qpa.agentId = :agent");
            q.setLong("id", poolId);
            q.setString("agent", agentId);
            return q.list();
        };
	    List list = getHibernateTemplate().execute(hcb);
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
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(qpi);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving item to pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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

      int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          getHibernateTemplate().deleteAll(itemList); // delete all AssetBeanie
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete all items in pool: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }


      // #2. delete question and questionpool map.
      retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("select qpi from QuestionPoolItemData as qpi where qpi.questionPoolId = :id");
            q.setLong("id", poolId);
            return q.list();
          };
          List list = getHibernateTemplate().execute(hcb);

          // a. delete item and pool association in SAM_ITEMMETADATA_T - this is the primary
          // pool that item is attached to
          List<ItemMetaDataIfc> metaList = new ArrayList<>();
          for (int j=0; j<list.size(); j++){
            Long itemId = ((QuestionPoolItemData)list.get(j)).getItemId();
            String query = "from ItemMetaData as meta where meta.item.itemId = :id and meta.label = :label";
    	    List m = getHibernateTemplate().findByNamedParam(query, new String[] {"id", "label"}, new Object[] {itemId, ItemMetaDataIfc.POOLID});
            if (m.size()>0){
              ItemMetaDataIfc meta = (ItemMetaDataIfc)m.get(0);
              meta.setEntry(null);
              metaList.add(meta);
            }
          }
          try{
            for (ItemMetaDataIfc meta : metaList) {
              getHibernateTemplate().saveOrUpdate(meta);	
            }
            retryCount = 0;
          }
          catch (DataAccessException e) {
            log.warn("problem delete question and questionpool map inside itemMetaData: {}", e.getMessage());
            retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      // #3. Pool is owned by one but can be shared by multiple agents. So need to
      // delete all QuestionPoolAccessData record first. This seems to be missing in Navigo, nope? - daisyf
      // Actually, I don't think we have ever implemented sharing between agents. So we may wnat to
      // clean up this bit of code - daisyf 07/07/06
      // #3a. Delete all shared pool by him sons
      final HibernateCallback<List> hcb = session -> {
        Query q = session.createQuery("select qpa from QuestionPoolAccessData as qpa, QuestionPoolData as qpp " +
                "where qpa.questionPoolId = qpp.questionPoolId and (qpp.questionPoolId = :qid or qpp.parentPoolId = :pid) ");
        q.setLong("qid", poolId);
        q.setLong("pid", poolId);
        return q.list();
      };
      List qpaList = getHibernateTemplate().execute(hcb);
      retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          getHibernateTemplate().deleteAll(qpaList);
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete question pool access data: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      // #4. Ready! delete pool now
      final HibernateCallback<List> hcb2 = session -> {
          Query q = session.createQuery("select qp from QuestionPoolData as qp where qp.id = :id");
          q.setLong("id", poolId);
          return q.list();
      };
      List qppList = getHibernateTemplate().execute(hcb2);
      retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          getHibernateTemplate().deleteAll(qppList);
          retryCount = 0;
        }
        catch (DataAccessException e) {
          log.warn("problem delete all pools: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().update( (QuestionPoolData) sourcePool.getData());
        retryCount = 0;
      }
      catch (DataAccessException e) {
        log.warn("problem moving pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
      }
      else {
        QuestionPoolFacade destPool = getPool(destPoolId, agentId);
        sourcePool.setParentPoolId(destPool.getQuestionPoolId());
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().update( (QuestionPoolData) sourcePool.getData());
        retryCount = 0;
      }
      catch (DataAccessException e) {
        log.warn("problem update source pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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
  public void removeItemFromPool(Long itemId, Long poolId) {
    QuestionPoolItemData qpi = new QuestionPoolItemData(poolId, itemId);
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(qpi);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete item from pool: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void moveItemToPool(Long itemId, Long sourceId, Long destId) {
    QuestionPoolItemData qpi = new QuestionPoolItemData(sourceId, itemId);
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(qpi);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete old mapping: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
    QuestionPoolItemData qpi2 = new QuestionPoolItemData(destId, itemId);
    retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(qpi2);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving new mapping: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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
      int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
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
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      if (insert) {
        // add a QuestionPoolAccessData record for the owner who should have ADMIN access to the pool
        QuestionPoolAccessData qpa = new QuestionPoolAccessData(qpp.
            getQuestionPoolId(), qpp.getOwnerId(), QuestionPoolData.ADMIN);
        retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
        while (retryCount > 0){
          try {
            getHibernateTemplate().save(qpa);
            retryCount = 0;
          }
          catch (DataAccessException e) {
            log.warn("problem saving pool: "+e.getMessage());
            retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
          }
        }
        
        // add a QuestionPoolAccessData record for all users who are sharing the subpool
        final long parentPoolId = qpp.getParentPoolId();
        final String ownerId = qpp.getOwnerId();

        if (parentPoolId != 0) {
        	List<QuestionPoolAccessData> listSubpool = new ArrayList();
        	try {
        		listSubpool = (List<QuestionPoolAccessData>) getHibernateTemplate()
                        .findByNamedParam("from QuestionPoolAccessData as qpa where qpa.questionPoolId = :id and qpa.agentId <> :agent",
                                new String[] {"id", "agent"},
                                new Object[] { Long.valueOf(parentPoolId), ownerId});
        	} catch (Exception e1) {
        		log.warn("problem finding pool: "+e1.getMessage());
        	}
        	for (QuestionPoolAccessData questioPoolData : listSubpool) {
        		qpa = new
        		QuestionPoolAccessData(qpp.getQuestionPoolId(),
        				questioPoolData.getAgentId(), QuestionPoolData.READ_COPY);
        		retryCount =
                        PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
        		while (retryCount > 0){
        			try {
        				getHibernateTemplate().save(qpa);
        				retryCount = 0;
        			}
        			catch (DataAccessException e) {
        				log.warn("problem saving pool: "+e.getMessage());
        				retryCount =
        					PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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
	    final HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("from QuestionPoolData as qpp where qpp.parentPoolId = :id");
            q.setLong("id", poolId.longValue());
            return q.list();
        };
	    return getHibernateTemplate().execute(hcb);
  }

  @Override
  public List<Long[]> getSubPoolSizes(final String agent) {
	  final HibernateCallback<List<Object[]>> hcb = session -> {
          Query q = session.createQuery("select a.questionPoolId, (select count(*) from QuestionPoolData b where b.parentPoolId=a.questionPoolId) " +
                  "from QuestionPoolData a where a.ownerId = :id");
          q.setCacheable(true);
          q.setString("id", agent);
          return q.list();
      };
	  List<Object[]> objectResult = getHibernateTemplate().execute(hcb);
	  List<Long[]> longResult = new ArrayList<>(objectResult.size());
	  for (Object[] array : objectResult) {
	      longResult.add(new Long[]{((Number) array[0]).longValue(), ((Number) array[1]).longValue()});
      }
      return longResult;
  }

  //number of subpools for this pool. But consider getSubPoolSizes if you're going to 
  // need this for all the pools.
  public long getSubPoolSize(final Long poolId) {
	  final HibernateCallback<Number> hcb = session -> {
          Query q = session.createQuery("select count(qpp) from QuestionPoolData qpp where qpp.parentPoolId = :id");
          q.setCacheable(true);
          q.setLong("id", poolId);
          return (Number) q.uniqueResult();
      };
	  
	  return getHibernateTemplate().execute(hcb).longValue();
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
	  long poolSize = getSubPoolSize(poolId);
	  if (poolSize >= 0) 
		  return true;
	  else
		  return false;
  }

  public boolean poolIsUnique(final Long questionPoolId, final String title, final Long parentPoolId, final String agentId) {
    final HibernateCallback<List> hcb = session -> {
        Query q = session.createQuery("select new QuestionPoolData(a.questionPoolId, a.title, a.parentPoolId)from QuestionPoolData a " +
                "where a.questionPoolId != :qid and a.title = :title and a.parentPoolId = :pid and a.ownerId = :agent");
        q.setLong("qid", questionPoolId);
        q.setString("title", title);
        q.setLong("pid", parentPoolId);
        q.setString("agent", agentId);
        return q.list();
    };
    List list = getHibernateTemplate().execute(hcb);

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

  public List<Long> getPoolIdsByAgent(final String agentId) {
    List<Long> idList = new ArrayList<Long>();

    final HibernateCallback<List> hcb = session -> {
        Query q = session.createQuery("select qpa from QuestionPoolAccessData as qpa where qpa.agentId = :id");
        q.setString("id", agentId);
        return q.list();
    };
    List qpaList = getHibernateTemplate().execute(hcb);

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
    List idList = new ArrayList();
    
    final HibernateCallback<List> hcb = session -> {
        Query q = session.createQuery("select qpi from QuestionPoolItemData as qpi where qpi.itemId = :id");
        q.setString("id", itemId);
        return q.list();
    };
    List qpiList = getHibernateTemplate().execute(hcb);

    try {
      Iterator iter = qpiList.iterator();
      while (iter.hasNext()) {
        QuestionPoolItemData qpa = (QuestionPoolItemData) iter.next();
        idList.add(qpa.getQuestionPoolId()); // return a list of poolId (java.lang.Long)
      }
      return idList;
    }
    catch (Exception e) {
        log.error(e.getMessage(), e);
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
          (tree.isDescendantOf(destId,sourceId))) {
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
      List itemDataArray = new ArrayList();
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

        //Update the questions index
        Set<QuestionPoolItemData> qpItems = newPool.getQuestionPoolItems();
        Iterator<QuestionPoolItemData> qpItemsIterator = qpItems.iterator();
        while (qpItemsIterator.hasNext()){
            QuestionPoolItemData qpItem = qpItemsIterator.next();
            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/copied, itemId=" + qpItem.getItemId(), true));
        }

      // Get the SubPools of oldPool
      Iterator citer = (tree.getChildList(sourceId)).iterator();
      while (citer.hasNext()) {
        Long childPoolId = (Long) citer.next();
        copyPool(tree, agentId, childPoolId, newPool.getQuestionPoolId(), prependString1, prependString2);
      }
    }
    catch (Exception e) {
        log.error(e.getMessage(), e);
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
        log.error(e.getMessage(), e);
    }
    return questionPoolFacade;
  }

  public Map getQuestionPoolItemMap(){
    Map h = new HashMap();
    String query = "from QuestionPoolItemData";
    List l = getHibernateTemplate().find(query);
    for (int i = 0; i < l.size(); i++) {
      QuestionPoolItemData q = (QuestionPoolItemData) l.get(i);
      h.put(q.getItemId(), q);
    }
    return h;
  }

  public Set prepareQuestions(Long questionPoolId, List itemDataArray){
    Set set = new HashSet();
    Iterator iter = itemDataArray.iterator();
    while (iter.hasNext()){
      ItemDataIfc itemData = (ItemDataIfc) iter.next();
      set.add(new QuestionPoolItemData(questionPoolId, itemData.getItemId(), (ItemData) itemData));
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
      item.setHasRationale(itemData.getHasRationale());
      item.setTriesAllowed(itemData.getTriesAllowed());
      item.setDuration(itemData.getDuration());
      item.setAnswerOptionsRichCount(itemData.getAnswerOptionsRichCount());
      item.setAnswerOptionsSimpleOrRich(itemData.getAnswerOptionsSimpleOrRich());
      item.setDescription(itemData.getDescription());

      item.setItemTextSet(copyItemText(item.getData(), itemData));
      item.setItemMetaDataSet(copyMetaData(item.getData(), itemData));
      item.setItemTagSet(copyTags(item.getData(), itemData));
      item.setItemAttachmentSet(copyAttachment(item.getData(), itemData));
      item.setInstruction(AssessmentService.copyStringAttachment(itemData.getInstruction()));

      if (itemData.getCorrectItemFeedback() != null && !itemData.getCorrectItemFeedback().equals("")) {
    	  item.setCorrectItemFeedback(AssessmentService.copyStringAttachment(itemData.getCorrectItemFeedback()));
      }
      if (itemData.getInCorrectItemFeedback() != null && !itemData.getInCorrectItemFeedback().equals("")) {
    	  item.setInCorrectItemFeedback(AssessmentService.copyStringAttachment(itemData.getInCorrectItemFeedback()));
      }
      if (itemData.getGeneralItemFeedback() != null && !itemData.getGeneralItemFeedback().equals("")) {
    	  item.setGeneralItemFeedback(AssessmentService.copyStringAttachment(itemData.getGeneralItemFeedback()));
      }
      
      return item;
  }

  private Set copyItemText(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	    Set toItemTextSet = new HashSet();
	    Set fromItemTextSet = fromItemData.getItemTextSet();
	    Iterator itemTextIter = fromItemTextSet.iterator();
	      while (itemTextIter.hasNext()) {
	    	  ItemText fromItemText = (ItemText) itemTextIter.next();
	    	  ItemText toItemText = new ItemText();
	    	  toItemText.setItem(toItemData);
	    	  toItemText.setSequence(fromItemText.getSequence());
	    	  toItemText.setText(fromItemText.getText());
                  toItemText.setRequiredOptionsCount(fromItemText.getRequiredOptionsCount());
	    	  
	    	  Set toAnswerSet = new HashSet();
	    	  Set fromAnswerSet = fromItemText.getAnswerSet();
	    	  Iterator answerIter = fromAnswerSet.iterator();
	    	  while (answerIter.hasNext()) {
	    		  Answer fromAnswer = (Answer) answerIter.next();
	    		  Answer toAnswer = new Answer(toItemText, fromAnswer.getText(), fromAnswer.getSequence(), fromAnswer.getLabel(),
	    				  fromAnswer.getIsCorrect(), fromAnswer.getGrade(), fromAnswer.getScore(), fromAnswer.getPartialCredit(), fromAnswer.getDiscount(), 
	    				  //fromAnswer.getCorrectOptionLabels(), 
	    				  null);
	    		  
	    		  Set toAnswerFeedbackSet = new HashSet();
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
  
  private Set copyMetaData(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	    Set toSet = new HashSet();
	    Set fromSet = fromItemData.getItemMetaDataSet();
	    Iterator iter = fromSet.iterator();
	    while (iter.hasNext()) {
	    	ItemMetaData itemMetaData = (ItemMetaData) iter.next();
	    	toSet.add(new ItemMetaData(toItemData, itemMetaData.getLabel(), itemMetaData.getEntry()));
	    }
	    return toSet;
  }

  private HashSet copyTags(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	    HashSet toSet = new HashSet();
	    Set fromSet = fromItemData.getItemTagSet();
	    Iterator iter = fromSet.iterator();
	    while (iter.hasNext()) {
	    	ItemTag itemTag = (ItemTag) iter.next();
	    	toSet.add(new ItemTag(toItemData, itemTag.getTagId(), itemTag.getTagLabel(), itemTag.getTagCollectionId(), itemTag.getTagCollectionName()));
	    }
	    return toSet;
  }
  
  private Set copyAttachment(ItemDataIfc toItemData, ItemDataIfc fromItemData) {
	  AssessmentService assessmentService = new AssessmentService();
	  Set toSet = assessmentService.copyItemAttachmentSet((ItemData) toItemData, fromItemData.getItemAttachmentSet());
	    
	  return toSet;
  }
  
  public Integer getCountItemFacades(final Long questionPoolId) {
      final HibernateCallback<Number> hcb = session -> {
          Query q = session.createQuery("select count(ab) from ItemData ab, QuestionPoolItemData qpi where ab.itemId = qpi.itemId and qpi.questionPoolId = :id");
          q.setLong("id", questionPoolId);
          q.setCacheable(true);
          return (Number) q.uniqueResult();
      };
	  	    
	  return getHibernateTemplate().execute(hcb).intValue();
  }
  
  /**
   * Fetch a HashMap of question pool ids and counts for all pools that a user has access to.
   * We inner join the QuestionPoolAccessData table because the user may have access to pools
   * that are being shared by other users. We can't simply look for the ownerId on QuestionPoolData.
   * This was originally written for SAM-2463 to speed up these counts. 
   * @param agentId Sakai internal user id. Most likely the currently logged in user
   */
  public Map<Long, Integer> getCountItemFacadesForUser(final String agentId) {
	  final HibernateCallback<List<Object[]>> hcb = session -> {
          Query q = session.createQuery(
                  "select qpi.questionPoolId, count(ab) from ItemData ab, QuestionPoolItemData qpi, QuestionPoolData qpd, QuestionPoolAccessData qpad " +
                  "where ab.itemId = qpi.itemId and qpi.questionPoolId = qpd.questionPoolId AND qpd.questionPoolId = qpad.questionPoolId AND qpad.agentId = :agent AND qpad.accessTypeId != :access " +
                  "group by qpi.questionPoolId");
          q.setString("agent", agentId);
          q.setLong("access", QuestionPoolData.ACCESS_DENIED);
          q.setCacheable(true);
          return q.list();
      };

	  Map<Long, Integer> counts = new HashMap<>();
	  List<Object[]> list = getHibernateTemplate().execute(hcb);

	  for (Object[] result : list) {
		  counts.put(((Number) result[0]).longValue(), ((Number) result[1]).intValue());
	  }

	  return counts;
  }

  /**
   * Shared Pools with other user
   */
  public void addQuestionPoolAccess(Tree tree, String user, final Long questionPoolId, Long accessTypeId) {	  
	  QuestionPoolAccessData qpad = new QuestionPoolAccessData(questionPoolId, user, accessTypeId);

	  getHibernateTemplate().saveOrUpdate(qpad);
	  Iterator citer = (tree.getChildList(questionPoolId)).iterator();
	  while (citer.hasNext()) {
		  Long childPoolId = (Long) citer.next();
	      addQuestionPoolAccess(tree, user, childPoolId, accessTypeId);
	  }
  }

  public void removeQuestionPoolAccess(Tree tree, String user, final Long questionPoolId, Long accessTypeId) {	  
	  QuestionPoolAccessData qpad = new QuestionPoolAccessData(questionPoolId, user, accessTypeId);

	  getHibernateTemplate().delete(qpad);

	  Iterator citer = (tree.getChildList(questionPoolId)).iterator();
	  while (citer.hasNext()) {
		  Long childPoolId = (Long) citer.next();
		  removeQuestionPoolAccess(tree, user, childPoolId, accessTypeId);
	  }
  }

  public List<AgentFacade> getAgentsWithAccess(final Long questionPoolId) {
	  final HibernateCallback<List<QuestionPoolAccessData>> hcb = session -> {
          Query q = session.createQuery("select qpa from QuestionPoolAccessData as qpa where qpa.questionPoolId = :id");
          q.setLong("id", questionPoolId);
          return q.list();
      };
	  List<QuestionPoolAccessData> qpaList = getHibernateTemplate().execute(hcb);

	  List<AgentFacade> agents = new ArrayList<>();
	  for (QuestionPoolAccessData pool : qpaList) {
		  AgentFacade agent = new AgentFacade(pool.getAgentId());
		  agents.add(agent);
	  }

	  return agents;
  }
  
  // **********************************************
  // ****************** SAM-2049 ******************
  // **********************************************
  
  public List<QuestionPoolData> getAllPoolsForTransfer(final List<Long> selectedPoolIds) {  
	  final HibernateCallback<List> hcb = session -> {
          Query q = session.createQuery("FROM QuestionPoolData a WHERE a.questionPoolId IN (:ids)");
          q. setParameterList("ids", selectedPoolIds);
          return q.list();
      };
	  return getHibernateTemplate().execute(hcb);
  }
	
  private String createQueryString(List<Long> poolIds) {
	  String poolIdQueryString ="";
	  String prefix = "";
	  for (Long poolId: poolIds) {
		  poolIdQueryString += prefix + poolId.toString();
		  prefix = ",";
	  }
	  
  	  return poolIdQueryString;
  }
	    
  private void updatePool(QuestionPoolData pooldata) {
	  try {
		  getHibernateTemplate().update(pooldata);
	  } catch (Exception e) {
		  log.warn("problem update the pool name" + e.getMessage());
	  }	  
  }
  
  private String renameDuplicate(String title) {
	  if (title == null) {
		  title = "";
	  }
  
	  String rename = "";
	  int index = title.lastIndexOf(VERSION_START);

	  // If it is versioned
	  if (index > -1) {
		  String mainPart = "";
		  String versionPart = title.substring(index);
		  if(index > 0) {
			  mainPart = title.substring(0, index);
		  }
  
		  int nIndex = index + VERSION_START.length();
		  String version = title.substring(nIndex);
  
		  int versionNumber = 0;
		  try {
			  versionNumber = Integer.parseInt(version);
			  if (versionNumber < 2) {
				  versionNumber = 2;
			  }
			  versionPart = VERSION_START + (versionNumber + 1);
  			  rename = mainPart + versionPart;
  		  } catch (NumberFormatException ex) {
  			  rename = title + VERSION_START + "2";
  		  }
	  } else {
		  rename = title + VERSION_START + "2";
	  }

	  return rename;
  }
	  
  public void transferPoolsOwnership(String ownerId, final List<Long> transferPoolIds) {
  	  Session session = null;

  	  // Get all pools to be transferred
  	  List<QuestionPoolData> transferPoolsData = getAllPoolsForTransfer(transferPoolIds);
  
  	  // Get poolId which need to remove child-parent relationship
  	  List<Long> needUpdatedPoolParentIdList = new ArrayList<Long>();
  	  List<Long> updatePoolOwnerIdList = new ArrayList<Long>();
  
  	  for (QuestionPoolData poolTransfer : transferPoolsData) {
  		  Long poolId = poolTransfer.getQuestionPoolId();	
  		  updatePoolOwnerIdList.add(poolId);
  		  
  		  // Get remove child-parent relationship list
  		  Long poolIdRemoveParent = poolTransfer.getParentPoolId();
  		  if (!poolIdRemoveParent.equals(new Long("0")) && !transferPoolIds.contains(poolIdRemoveParent)) {
  			  needUpdatedPoolParentIdList.add(poolId);
  		  }
  	  }
  
  	  // updatePoolOwnerIdList will not be empty, so no need to check the size
  	  String updateOwnerIdInPoolTableQueryString = createQueryString(updatePoolOwnerIdList);
  
  	  // If all parent-children structure transfer, needUpdatedPoolParentIdList will be empty.	  
  	  String removeParentPoolString = "";
  	  if (needUpdatedPoolParentIdList.size() > 0) {
  		  removeParentPoolString = createQueryString(needUpdatedPoolParentIdList);
  	  }
  
  	  // I used jdbc update here since I met difficulties using hibernate to update SAM_QUESTIONPOOLACCESS_T. (it used composite-id there)
  	  // For updating SAM_QUESTIONPOOL_T, I can use hibernate but it will have many db calls. (I didn't find an efficient way to bulk update.) So used jdbc here again.
  	  try {
  		  session = getSessionFactory().openSession();
          session.beginTransaction();
  		  String query = "";
  		  if (!"".equals(updateOwnerIdInPoolTableQueryString)) {
  			  query = "UPDATE SAM_QUESTIONPOOLACCESS_T SET agentid = :id WHERE questionpoolid IN (" + updateOwnerIdInPoolTableQueryString + ") AND accesstypeid = 34";
  			  session.createSQLQuery(query).setString("id", ownerId).executeUpdate();

  			  query = "UPDATE SAM_QUESTIONPOOL_T SET ownerid = :id WHERE questionpoolid IN (" + updateOwnerIdInPoolTableQueryString + ")";
			  session.createSQLQuery(query).setString("id", ownerId).executeUpdate();
              session.flush();
  		  }
  
  		  // if the pool has parent but the parent doesn't transfer, need to remove the child-parent relationship.
  		  if (!"".equals(removeParentPoolString)) {
  			  query = "UPDATE SAM_QUESTIONPOOL_T SET parentpoolid = 0 WHERE questionpoolid IN (" + removeParentPoolString + ")";
  			  session.createSQLQuery(query).executeUpdate();
              session.flush();
  		  }
  		  session.getTransaction().commit();
  	  } catch (Exception ex) {
  		  log.warn(ex.getMessage());
	  } finally {
  		  if (session != null) {
  			  try {
  				  session.close();
			  } catch (Exception ex) {
				  log.warn("Could not close session", ex);
			  }
  		  }
  	  }
  
  	  // Update pool name if there is a duplicate one.	  
	  for (QuestionPoolData pooldata : transferPoolsData) {
		  Long poolId = pooldata.getQuestionPoolId();
		  String title = pooldata.getTitle();
		  boolean isUnique = poolIsUnique(poolId, title, new Long("0"), ownerId);
		  if (!isUnique) {
			  synchronized (title) {
				  log.debug("Questionpool " + title + " is not unique.");
				  int count = 0; // Alternate exit condition
	
				  while (!isUnique) {
					  title = renameDuplicate(title);
					  log.debug("renameDuplicate (title): " + title);
					  
					  // Recheck to confirm that new title is not a dplicate too
					  isUnique = poolIsUnique(poolId, title, new Long("0"), ownerId);	      	  
					  if (count++ > 99) {
						  break; // Exit condition in case bug is introduced
					  }
				  }
			  }
			  
			  pooldata.setTitle(title);
			  pooldata.setOwnerId(ownerId);
			  if (!"".equals(removeParentPoolString) && needUpdatedPoolParentIdList.contains(poolId)) {
				  pooldata.setParentPoolId(new Long("0"));
			  }
			  updatePool(pooldata);
		  }		  
	  } 	  
  }
}
