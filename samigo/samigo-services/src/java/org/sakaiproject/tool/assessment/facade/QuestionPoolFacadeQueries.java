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

import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolAccessData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class QuestionPoolFacadeQueries
    implements QuestionPoolFacadeQueriesAPI {
  
  @Setter private SessionFactory sessionFactory;

  // SAM-2499
  private final FormattedText formattedText = (FormattedText) ComponentManager.get( FormattedText.class );
  private final TagService tagService = ComponentManager.get(TagService.class);

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
  public List<QuestionPoolData> getAllPools() {
    CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
    Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
    poolQuery.select(poolRoot);
    return sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();
  }

  private List<QuestionPoolData> getAllPoolsByAgent(final String agentId) {
      CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
      Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
      Subquery<Long> accessiblePools = poolQuery.subquery(Long.class);
      Root<QuestionPoolAccessData> accessRoot = accessiblePools.from(QuestionPoolAccessData.class);
      accessiblePools.select(accessRoot.get("questionPoolId")).where(poolQueryBuilder.equal(accessRoot.get("agentId"), agentId));
      poolQuery.select(poolRoot).where(poolRoot.get("questionPoolId").in(accessiblePools));
      return sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();
  }

  public QuestionPoolIteratorFacade getAllPoolsWithAccess(String agentId, Long access) {
      List<QuestionPoolFacade> qpList = new ArrayList<>();

      // counts is a hashmap going from poolid to number of subpools. It is significantly
      // faster to build this with a single SQL query and then look up data in it.
      Map<Long, Long> counts = getSubPoolSizes(agentId).stream().collect(Collectors.toMap(pool -> pool[0], pool -> pool[1]));

      List<QuestionPoolData> poolList = this.getAllPoolsByAgent(agentId); 

      try {

          for (QuestionPoolData qpp : poolList) {
              // lookup number of subpools for this pool in our handy hash table
              Long subPoolSize = counts.get(qpp.getQuestionPoolId());
              qpp.setSubPoolSize(subPoolSize == null ? 0L : subPoolSize);
              Set<QuestionPoolAccessData> questionPoolAccessData = (Set<QuestionPoolAccessData>) qpp.getQuestionPoolAccess();
              for (QuestionPoolAccessData qpa : questionPoolAccessData) {
                  boolean isSameAgent = agentId.equals(qpa.getAgentId());
                  boolean isPoolOwner = (access.longValue() == QuestionPoolAccessFacade.ADMIN && qpp.getOwnerId().equals(agentId));
                  boolean hasAccess = (access.longValue() != QuestionPoolAccessFacade.ADMIN && qpa.getAccessTypeId().longValue() >= access.longValue());
                  if (isSameAgent && (isPoolOwner || hasAccess)) {
                      qpList.add(this.getQuestionPool(qpp));
                  }
              }
          }
      } catch (Exception e) {
          log.warn("Error in getAllPoolsWithAccess: " + e.getMessage(), e);
      }
      return new QuestionPoolIteratorFacade(qpList);
  }

  public List<QuestionPoolFacade> getBasicInfoOfAllPools(final String agentId) {
      CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
      Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
      Subquery<Long> accessiblePools = poolQuery.subquery(Long.class);
      Root<QuestionPoolAccessData> accessRoot = accessiblePools.from(QuestionPoolAccessData.class);
      accessiblePools.select(accessRoot.get("questionPoolId")).where(poolQueryBuilder.equal(accessRoot.get("agentId"), agentId));
      poolQuery.select(poolQueryBuilder.construct(QuestionPoolData.class, poolRoot.get("questionPoolId"), poolRoot.get("title"), poolRoot.get("parentPoolId"))).where(poolRoot.get("questionPoolId").in(accessiblePools));
      List list = sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();

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
      QuestionPoolFacade questionPool = new QuestionPoolFacade(qpp);
      addTags(questionPool);
      return questionPool;
    }
    catch (Exception e) {
      log.warn(e.getMessage());
      return null;
    }
  }

  private void addTags(QuestionPoolFacade facade) {
    Long poolId = facade.getQuestionPoolId();
    String ownerUserId = StringUtils.trimToNull(facade.getOwnerId());

    if (ObjectUtils.allNotNull(poolId, ownerUserId)) {
      String poolReference = SamigoConstants.REFERENCE_PREFIX_QUESTIONPOOL + "/" + poolId;

      Set<QuestionPoolTag> tags = tagService.getAssociatedTagsForItem(ownerUserId, poolReference).stream()
          .map(QuestionPoolTag::of)
          .collect(Collectors.toSet());

      facade.setTags(tags);
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
       sessionFactory.getCurrentSession().saveOrUpdate(itemdata);  //save itemdata after removing metadata
      }
    }
    return newlist;
  }


  private List getAllItemsInThisPoolOnly(final Long questionPoolId) {
  // return items that belong to this pool and this pool only.  
        CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<ItemData> itemQuery = itemQueryBuilder.createQuery(ItemData.class);
        Root<ItemData> itemRoot = itemQuery.from(ItemData.class);
        Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
        itemQuery.select(itemRoot).where(
            itemQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
            itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), questionPoolId));
        List list = sessionFactory.getCurrentSession().createQuery(itemQuery).getResultList();

    List newlist = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
     if (getPoolIdsByItem(itemdata.getItemId()).size() == 1) {
       newlist.add(itemdata);
     }
     else {
       // this item still links to other pool(s)
     } 
    }
    return newlist;
  }


  public List getAllItems(final Long questionPoolId) {
        CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<ItemData> itemQuery = itemQueryBuilder.createQuery(ItemData.class);
        Root<ItemData> itemRoot = itemQuery.from(ItemData.class);
        Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
        itemQuery.select(itemRoot).where(
            itemQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
            itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), questionPoolId.longValue()));
        itemQuery.orderBy(itemQueryBuilder.asc(itemRoot.get("itemId")));
        List list = sessionFactory.getCurrentSession().createQuery(itemQuery).getResultList();
        return list;
  }

    public List getAllItemsIds(final Long questionPoolId) {
        CriteriaBuilder itemIdQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Long> itemIdQuery = itemIdQueryBuilder.createQuery(Long.class);
        Root<QuestionPoolItemData> poolItemRoot = itemIdQuery.from(QuestionPoolItemData.class);
        itemIdQuery.select(poolItemRoot.get("itemId")).where(
            itemIdQueryBuilder.equal(poolItemRoot.get("questionPoolId"), questionPoolId.longValue()));
        List list = sessionFactory.getCurrentSession().createQuery(itemIdQuery).getResultList();
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
      CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<ItemData> itemQuery = itemQueryBuilder.createQuery(ItemData.class);
      Root<ItemData> itemRoot = itemQuery.from(ItemData.class);
      Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
      Root<TypeD> typeRoot = itemQuery.from(TypeD.class);
      itemQuery.select(itemRoot).where(
          itemQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
          itemQueryBuilder.equal(itemRoot.get("typeId"), typeRoot.get("typeId")),
          itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), questionPoolId));
      itemQuery.orderBy("false".equals(ascending) ? itemQueryBuilder.desc(typeRoot.get(orderBy)) : itemQueryBuilder.asc(typeRoot.get(orderBy)));
      List list = sessionFactory.getCurrentSession().createQuery(itemQuery).getResultList();

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
    CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<ItemData> itemQuery = itemQueryBuilder.createQuery(ItemData.class);
    Root<ItemData> itemRoot = itemQuery.from(ItemData.class);
    Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
    itemQuery.select(itemRoot).where(
        itemQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
        itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), questionPoolId.longValue()));
    itemQuery.orderBy(itemQueryBuilder.asc(itemRoot.get("itemId")));
    List list = sessionFactory.getCurrentSession().createQuery(itemQuery).getResultList();

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
    ItemData item = (ItemData) sessionFactory.getCurrentSession().getReference(ItemData.class, id);
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
      QuestionPoolData qpp = (QuestionPoolData) sessionFactory.getCurrentSession().getReference(
          QuestionPoolData.class, poolId);
      // setAccessType
      if (StringUtils.isNotBlank(agent)) {
        setPoolAccessType(qpp, agent);
      }
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
        CriteriaBuilder accessQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<QuestionPoolAccessData> accessQuery = accessQueryBuilder.createQuery(QuestionPoolAccessData.class);
        Root<QuestionPoolAccessData> accessRoot = accessQuery.from(QuestionPoolAccessData.class);
        accessQuery.select(accessRoot).where(
            accessQueryBuilder.equal(accessRoot.get("questionPoolId"), poolId),
            accessQueryBuilder.equal(accessRoot.get("agentId"), agentId));
        List list = sessionFactory.getCurrentSession().createQuery(accessQuery).getResultList();
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
        sessionFactory.getCurrentSession().persist(qpi);
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
        QuestionPoolData questionPool = (QuestionPoolData) sessionFactory.getCurrentSession().getReference(QuestionPoolData.class, poolId);

      // #1. delete all questions which mean AssetBeanie (not ItemImpl) 'cos AssetBeanie
      // is the one that is associated with the DB
      // lydial:  getting list of items that only belong to this pool and not linked to any assessments. 
      List itemList = getAllItemsInThisPoolOnlyAndDetachFromAssessment(poolId);

      int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          itemList.forEach(sessionFactory.getCurrentSession()::remove); // delete all AssetBeanie
          retryCount = 0;
        }
        catch (DataAccessException | PersistenceException e) {
          log.warn("problem delete all items in pool: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }


      // #2. delete question and questionpool map.
      retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          CriteriaBuilder poolItemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
          CriteriaQuery<QuestionPoolItemData> poolItemQuery = poolItemQueryBuilder.createQuery(QuestionPoolItemData.class);
          Root<QuestionPoolItemData> poolItemRoot = poolItemQuery.from(QuestionPoolItemData.class);
          poolItemQuery.select(poolItemRoot).where(
              poolItemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), poolId));
          List list = sessionFactory.getCurrentSession().createQuery(poolItemQuery).getResultList();

          // a. delete item and pool association in SAM_ITEMMETADATA_T - this is the primary
          // pool that item is attached to
          List<ItemMetaDataIfc> metaList = new ArrayList<>();
          for (int j=0; j<list.size(); j++){
            Long itemId = ((QuestionPoolItemData)list.get(j)).getItemId();
            CriteriaBuilder metadataQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<ItemMetaData> metadataQuery = metadataQueryBuilder.createQuery(ItemMetaData.class);
            Root<ItemMetaData> metadataRoot = metadataQuery.from(ItemMetaData.class);
            metadataQuery.select(metadataRoot).where(
                metadataQueryBuilder.equal(metadataRoot.get("item").get("itemId"), itemId),
                metadataQueryBuilder.equal(metadataRoot.get("label"), ItemMetaDataIfc.POOLID));
            List<ItemMetaData> m = sessionFactory.getCurrentSession().createQuery(metadataQuery).getResultList();
            if (m.size()>0){
              ItemMetaDataIfc meta = (ItemMetaDataIfc)m.get(0);
              meta.setEntry(null);
              metaList.add(meta);
            }
          }
          try{
            for (ItemMetaDataIfc meta : metaList) {
              sessionFactory.getCurrentSession().saveOrUpdate(meta);
            }
            retryCount = 0;
          }
          catch (DataAccessException | PersistenceException e) {
            log.warn("problem delete question and questionpool map inside itemMetaData: {}", e.getMessage());
            retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
          }

          // b. delete item and pool association in SAM_QUESTIONPOOLITEM_T
          if (list.size() > 0) {
            questionPool.setQuestionPoolItems(new HashSet());
            list.forEach(sessionFactory.getCurrentSession()::remove);
            retryCount = 0;
          }
          else retryCount = 0;
        }
        catch (DataAccessException | PersistenceException e) {
          log.warn("problem delete question and questionpool map: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      // #3. Pool is owned by one but can be shared by multiple agents. So need to
      // delete all QuestionPoolAccessData record first. This seems to be missing in Navigo, nope? - daisyf
      // Actually, I don't think we have ever implemented sharing between agents. So we may wnat to
      // clean up this bit of code - daisyf 07/07/06
      // #3a. Delete all shared pool by him sons
      CriteriaBuilder accessQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<QuestionPoolAccessData> accessQuery = accessQueryBuilder.createQuery(QuestionPoolAccessData.class);
      Root<QuestionPoolAccessData> accessRoot = accessQuery.from(QuestionPoolAccessData.class);
      Root<QuestionPoolData> poolRoot = accessQuery.from(QuestionPoolData.class);
      accessQuery.select(accessRoot).where(
          accessQueryBuilder.equal(accessRoot.get("questionPoolId"), poolRoot.get("questionPoolId")),
          accessQueryBuilder.or(accessQueryBuilder.equal(poolRoot.get("questionPoolId"), poolId), accessQueryBuilder.equal(poolRoot.get("parentPoolId"), poolId)));
      List qpaList = sessionFactory.getCurrentSession().createQuery(accessQuery).getResultList();
      retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          qpaList.forEach(sessionFactory.getCurrentSession()::remove);
          retryCount = 0;
        }
        catch (DataAccessException | PersistenceException e) {
          log.warn("problem delete question pool access data: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      // #4. Ready! delete pool now
      CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
      Root<QuestionPoolData> deletedPoolRoot = poolQuery.from(QuestionPoolData.class);
      poolQuery.select(deletedPoolRoot).where(
          poolQueryBuilder.equal(deletedPoolRoot.get("questionPoolId"), poolId));
      List qppList = sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();
      retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
      while (retryCount > 0){
        try {
          qppList.forEach(sessionFactory.getCurrentSession()::remove);
          retryCount = 0;
        }
        catch (DataAccessException | PersistenceException e) {
          log.warn("problem delete all pools: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      // #5. delete all subpools if any, this is recursive
      if (tree != null) {
        Iterator citer = (tree.getChildList(poolId)).iterator();
        while (citer.hasNext()) {
          deletePool( (Long) citer.next(), agent, tree);
        }
      }
    }
    catch (DataAccessException | PersistenceException e) {
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
      Set access = sourcePool.getData().getQuestionPoolAccess();
      Iterator iter = access.iterator();
      while (iter.hasNext()) {
        QuestionPoolAccessData accessPool = (QuestionPoolAccessData)iter.next();
        if (QuestionPoolAccessFacade.ADMIN.equals(accessPool.getAccessTypeId()) && !accessPool.getAgentId().equals(sourcePool.getOwnerId())) {
          sessionFactory.getCurrentSession().remove(accessPool);
          QuestionPoolAccessData qpad = new QuestionPoolAccessData(accessPool.getQuestionPoolId(), accessPool.getAgentId(), QuestionPoolAccessFacade.READ_WRITE);
          sessionFactory.getCurrentSession().saveOrUpdate(qpad);
        }
      }

      if (destPoolId.equals(QuestionPoolFacade.ROOT_POOL) &&
          !sourcePoolId.equals(QuestionPoolFacade.ROOT_POOL)) {
        sourcePool.setParentPoolId(QuestionPoolFacade.ROOT_POOL);
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        sessionFactory.getCurrentSession().update( (QuestionPoolData) sourcePool.getData());
        retryCount = 0;
      }
      catch (DataAccessException | PersistenceException e) {
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
        sessionFactory.getCurrentSession().update( (QuestionPoolData) sourcePool.getData());
        retryCount = 0;
      }
      catch (DataAccessException | PersistenceException e) {
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
        sessionFactory.getCurrentSession().remove(sessionFactory.getCurrentSession().merge(qpi));
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
        sessionFactory.getCurrentSession().remove(sessionFactory.getCurrentSession().merge(qpi));
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
        sessionFactory.getCurrentSession().persist(qpi2);
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
          sessionFactory.getCurrentSession().saveOrUpdate(qpp);
          retryCount = 0;
        }
        catch (DataAccessException | PersistenceException e) {
          log.warn("problem saving Or Update pool: "+e.getMessage());
          retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
        }
      }

      if (insert) {
        // add a QuestionPoolAccessData record for the owner who should have ADMIN access to the pool
        QuestionPoolAccessData qpa = new QuestionPoolAccessData(qpp.
            getQuestionPoolId(), qpp.getOwnerId(), qpp.getAccessTypeId());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
        while (retryCount > 0){
          try {
            sessionFactory.getCurrentSession().persist(qpa);
            retryCount = 0;
          }
          catch (DataAccessException | PersistenceException e) {
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
                CriteriaBuilder sharedAccessQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
                CriteriaQuery<QuestionPoolAccessData> sharedAccessQuery = sharedAccessQueryBuilder.createQuery(QuestionPoolAccessData.class);
                Root<QuestionPoolAccessData> sharedAccessRoot = sharedAccessQuery.from(QuestionPoolAccessData.class);
                sharedAccessQuery.select(sharedAccessRoot).where(
                    sharedAccessQueryBuilder.equal(sharedAccessRoot.get("questionPoolId"), parentPoolId),
                    sharedAccessQueryBuilder.notEqual(sharedAccessRoot.get("agentId"), ownerId));
                listSubpool = sessionFactory.getCurrentSession().createQuery(sharedAccessQuery).getResultList();
        	} catch (Exception e1) {
        		log.warn("problem finding pool: "+e1.getMessage());
        	}
        	for (QuestionPoolAccessData questioPoolData : listSubpool) {
        		qpa = new
        		QuestionPoolAccessData(qpp.getQuestionPoolId(),
        				questioPoolData.getAgentId(), questioPoolData.getAccessTypeId());
        		retryCount =
                        PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
        		while (retryCount > 0){
        			try {
                        sessionFactory.getCurrentSession().persist(qpa);
        				retryCount = 0;
        			}
                    catch (DataAccessException | PersistenceException e) {
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
        CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
        Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
        poolQuery.select(poolRoot).where(
            poolQueryBuilder.equal(poolRoot.get("parentPoolId"), poolId.longValue()));
        return sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();
  }

  /**
   * get number of subpools for each pool in a single query.
   * returns a List of Long arrays. Each array is 0: poolid, 1: count of subpools
   *
   * @param agent
   * @return List<Long[]>
   */
  private List<Long[]> getSubPoolSizes(final String agent) {
      CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<Object[]> poolQuery = poolQueryBuilder.createQuery(Object[].class);
      Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
      Subquery<Long> accessiblePools = poolQuery.subquery(Long.class);
      Root<QuestionPoolAccessData> accessRoot = accessiblePools.from(QuestionPoolAccessData.class);
      accessiblePools.select(accessRoot.get("questionPoolId")).where(poolQueryBuilder.equal(accessRoot.get("agentId"), agent));
      Subquery<Long> childCount = poolQuery.subquery(Long.class);
      Root<QuestionPoolData> childRoot = childCount.from(QuestionPoolData.class);
      childCount.select(poolQueryBuilder.count(childRoot)).where(poolQueryBuilder.equal(childRoot.get("parentPoolId"), poolRoot.get("questionPoolId")));
      poolQuery.multiselect(poolRoot.get("questionPoolId"), childCount).where(poolRoot.get("questionPoolId").in(accessiblePools));
      List<Object[]> objectResult = sessionFactory.getCurrentSession().createQuery(poolQuery).setCacheable(true).getResultList();
      List<Long[]> longResult = new ArrayList<>(objectResult.size());
      for (Object[] array : objectResult) {
          longResult.add(new Long[]{((Number) array[0]).longValue(), ((Number) array[1]).longValue()});
      }
      return longResult;
  }

  //number of subpools for this pool. But consider getSubPoolSizes if you're going to 
  // need this for all the pools.
  public long getSubPoolSize(final Long poolId) {
      CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<Long> poolQuery = poolQueryBuilder.createQuery(Long.class);
      Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
      poolQuery.select(poolQueryBuilder.count(poolRoot)).where(
          poolQueryBuilder.equal(poolRoot.get("parentPoolId"), poolId));

      return sessionFactory.getCurrentSession().createQuery(poolQuery).setCacheable(true).uniqueResult().longValue();
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
      return poolSize >= 0;
  }

  public boolean poolIsUnique(final Long questionPoolId, final String title, final Long parentPoolId, final String agentId) {
    CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
    Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
    poolQuery.select(poolQueryBuilder.construct(QuestionPoolData.class, poolRoot.get("questionPoolId"), poolRoot.get("title"), poolRoot.get("parentPoolId"))).where(
        poolQueryBuilder.notEqual(poolRoot.get("questionPoolId"), questionPoolId),
        poolQueryBuilder.equal(poolRoot.get("title"), title),
        poolQueryBuilder.equal(poolRoot.get("parentPoolId"), parentPoolId),
        poolQueryBuilder.equal(poolRoot.get("ownerId"), agentId));
    List list = sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();

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

    CriteriaBuilder accessQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolAccessData> accessQuery = accessQueryBuilder.createQuery(QuestionPoolAccessData.class);
    Root<QuestionPoolAccessData> accessRoot = accessQuery.from(QuestionPoolAccessData.class);
    accessQuery.select(accessRoot).where(
        accessQueryBuilder.equal(accessRoot.get("agentId"), agentId));
    List qpaList = sessionFactory.getCurrentSession().createQuery(accessQuery).getResultList();

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

  public List getPoolIdsByItem(final Long itemId) {
    List idList = new ArrayList();
    
    CriteriaBuilder poolItemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolItemData> poolItemQuery = poolItemQueryBuilder.createQuery(QuestionPoolItemData.class);
    Root<QuestionPoolItemData> poolItemRoot = poolItemQuery.from(QuestionPoolItemData.class);
    poolItemQuery.select(poolItemRoot).where(
        poolItemQueryBuilder.equal(poolItemRoot.get("itemId"), itemId));
    List qpiList = sessionFactory.getCurrentSession().createQuery(poolItemQuery).getResultList();

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
      newPool.setAccessTypeId(QuestionPoolAccessFacade.ADMIN);

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
    sessionFactory.getCurrentSession().persist(questionPool);
    return questionPool.getQuestionPoolId();
  }

  public QuestionPoolFacade getPoolById(Long questionPoolId) {
    QuestionPoolFacade questionPoolFacade = null;
    try {
      if (!questionPoolId.equals(QuestionPoolFacade.ROOT_POOL)) {
        QuestionPoolData questionPool = (QuestionPoolData) sessionFactory.getCurrentSession().getReference(QuestionPoolData.class, questionPoolId);
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
    CriteriaBuilder poolItemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolItemData> poolItemQuery = poolItemQueryBuilder.createQuery(QuestionPoolItemData.class);
    Root<QuestionPoolItemData> poolItemRoot = poolItemQuery.from(QuestionPoolItemData.class);
    poolItemQuery.select(poolItemRoot);
    List<QuestionPoolItemData> l = sessionFactory.getCurrentSession().createQuery(poolItemQuery).getResultList();
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
      item.setMinScore(itemData.getMinScore());
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
      item.setIsExtraCredit(itemData.getIsExtraCredit());
      item.setPartialCreditFlag(itemData.getPartialCreditFlag());

      item.setItemTextSet(copyItemText(item.getData(), itemData));
      item.setItemMetaDataSet(copyMetaData(item.getData(), itemData));
      item.setItemTagSet(copyTags(item.getData(), itemData));
      item.setItemAttachmentSet(copyAttachment(item.getData(), itemData));
      item.setInstruction(AssessmentService.copyStringAttachment(itemData.getInstruction()));

      if (StringUtils.isNotEmpty(itemData.getCorrectItemFeedback())) {
    	  item.setCorrectItemFeedback(AssessmentService.copyStringAttachment(itemData.getCorrectItemFeedback()), AssessmentService.copyStringAttachment(itemData.getCorrectItemFeedbackValue()));
      }
      if (StringUtils.isNotEmpty(itemData.getInCorrectItemFeedback())) {
    	  item.setInCorrectItemFeedback(AssessmentService.copyStringAttachment(itemData.getInCorrectItemFeedback()), AssessmentService.copyStringAttachment(itemData.getInCorrectItemFeedbackValue()));
      }
      if (StringUtils.isNotEmpty(itemData.getGeneralItemFeedback())) {
    	  item.setGeneralItemFeedback(AssessmentService.copyStringAttachment(itemData.getGeneralItemFeedback()), AssessmentService.copyStringAttachment(itemData.getGeneralItemFeedback()));
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
	    	  toItemText.setAddedButNotExtracted(fromItemText.isAddedButNotExtracted());
	    	  Set toAnswerSet = new HashSet();
	    	  Set fromAnswerSet = fromItemText.getAnswerSet();
	    	  Iterator answerIter = fromAnswerSet.iterator();
	    	  while (answerIter.hasNext()) {
	    		  Answer fromAnswer = (Answer) answerIter.next();
	    		  Answer toAnswer = new Answer(toItemText, AssessmentService.copyStringAttachment(fromAnswer.getText()), fromAnswer.getSequence(), fromAnswer.getLabel(),
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
      CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<Long> itemQuery = itemQueryBuilder.createQuery(Long.class);
      Root<ItemData> itemRoot = itemQuery.from(ItemData.class);
      Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
      itemQuery.select(itemQueryBuilder.count(itemRoot)).where(
          itemQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
          itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), questionPoolId));
	  	    
      return sessionFactory.getCurrentSession().createQuery(itemQuery).setCacheable(true).uniqueResult().intValue();
  }
  
  /**
   * Fetch a HashMap of question pool ids and counts for all pools that a user has access to.
   * We inner join the QuestionPoolAccessData table because the user may have access to pools
   * that are being shared by other users. We can't simply look for the ownerId on QuestionPoolData.
   * This was originally written for SAM-2463 to speed up these counts. 
   * @param agentId Sakai internal user id. Most likely the currently logged in user
   */
  public Map<Long, Integer> getCountItemFacadesForUser(final String agentId) {
      CriteriaBuilder countQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<Object[]> countQuery = countQueryBuilder.createQuery(Object[].class);
      Root<ItemData> itemRoot = countQuery.from(ItemData.class);
      Root<QuestionPoolItemData> poolItemRoot = countQuery.from(QuestionPoolItemData.class);
      Root<QuestionPoolData> poolRoot = countQuery.from(QuestionPoolData.class);
      Root<QuestionPoolAccessData> accessRoot = countQuery.from(QuestionPoolAccessData.class);
      countQuery.multiselect(poolItemRoot.get("questionPoolId"), countQueryBuilder.count(itemRoot)).where(
          countQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
          countQueryBuilder.equal(poolItemRoot.get("questionPoolId"), poolRoot.get("questionPoolId")),
          countQueryBuilder.equal(poolRoot.get("questionPoolId"), accessRoot.get("questionPoolId")),
          countQueryBuilder.equal(accessRoot.get("agentId"), agentId),
          countQueryBuilder.notEqual(accessRoot.get("accessTypeId"), QuestionPoolData.ACCESS_DENIED));
      countQuery.groupBy(poolItemRoot.get("questionPoolId"));

	  Map<Long, Integer> counts = new HashMap<>();
      List<Object[]> list = sessionFactory.getCurrentSession().createQuery(countQuery).setCacheable(true).getResultList();

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

      sessionFactory.getCurrentSession().saveOrUpdate(qpad);
	  Iterator citer = (tree.getChildList(questionPoolId)).iterator();
	  while (citer.hasNext()) {
		  Long childPoolId = (Long) citer.next();
	      addQuestionPoolAccess(tree, user, childPoolId, accessTypeId);
	  }
  }

  public void removeQuestionPoolAccess(Tree tree, final String user, final Long questionPoolId) {
    CriteriaBuilder accessQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolAccessData> accessQuery = accessQueryBuilder.createQuery(QuestionPoolAccessData.class);
    Root<QuestionPoolAccessData> accessRoot = accessQuery.from(QuestionPoolAccessData.class);
    accessQuery.select(accessRoot).where(
        accessQueryBuilder.equal(accessRoot.get("questionPoolId"), questionPoolId.longValue()),
        accessQueryBuilder.equal(accessRoot.get("agentId"), user));
    List<QuestionPoolAccessData> qpaList = (List<QuestionPoolAccessData>)sessionFactory.getCurrentSession().createQuery(accessQuery).getResultList();

    qpaList.forEach(sessionFactory.getCurrentSession()::remove);

    Iterator citer = (tree.getChildList(questionPoolId)).iterator();
    while (citer.hasNext()) {
      Long childPoolId = (Long) citer.next();
      removeQuestionPoolAccess(tree, user, childPoolId);
    }
  }

  public List<QuestionPoolAccessFacade> getAgentsWithAccess(final Long questionPoolId) {
    CriteriaBuilder accessQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<QuestionPoolAccessData> accessQuery = accessQueryBuilder.createQuery(QuestionPoolAccessData.class);
    Root<QuestionPoolAccessData> accessRoot = accessQuery.from(QuestionPoolAccessData.class);
    accessQuery.select(accessRoot).where(
        accessQueryBuilder.equal(accessRoot.get("questionPoolId"), questionPoolId));
    List<QuestionPoolAccessData> qpaList = sessionFactory.getCurrentSession().createQuery(accessQuery).getResultList();

    List<QuestionPoolAccessFacade> pqds = new ArrayList();
    for (QuestionPoolAccessData pool : qpaList) {
      QuestionPoolAccessFacade qpd = new QuestionPoolAccessFacade(pool.getQuestionPoolId(), pool.getAccessTypeId(), pool.getAgentId());
      pqds.add(qpd);
    }

    return pqds;
  }

  // **********************************************
  // ****************** SAM-2049 ******************
  // **********************************************
  
  public List<QuestionPoolData> getAllPoolsForTransfer(final List<Long> selectedPoolIds) {  
      CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
      CriteriaQuery<QuestionPoolData> poolQuery = poolQueryBuilder.createQuery(QuestionPoolData.class);
      Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
      poolQuery.select(poolRoot).where(
          poolRoot.get("questionPoolId").in(selectedPoolIds));
      return sessionFactory.getCurrentSession().createQuery(poolQuery).getResultList();
  }
	
  private void updatePool(QuestionPoolData pooldata) {
	  try {
          sessionFactory.getCurrentSession().update(pooldata);
	  } catch (Exception e) {
		  log.warn("problem update the pool name" + e.getMessage());
	  }	  
  }

  public void transferPoolsOwnership(String ownerId, final List<Long> transferPoolIds) {

  	  // Get all pools to be transferred
  	  List<QuestionPoolData> transferPoolsData = getAllPoolsForTransfer(transferPoolIds);
  
  	  // Get poolId which need to remove child-parent relationship
  	  List<Long> needUpdatedPoolParentIdList = new ArrayList<Long>();
  	  List<Long> updatePoolOwnerIdList = new ArrayList<Long>();
  
  	  for (QuestionPoolData poolTransfer : transferPoolsData) {

        if(poolTransfer.getOwnerId().equals(ownerId)) {
          return;
        }

  		  Long poolId = poolTransfer.getQuestionPoolId();	
  		  updatePoolOwnerIdList.add(poolId);
  		  
  		  // Get remove child-parent relationship list
  		  Long poolIdRemoveParent = poolTransfer.getParentPoolId();
  		  if (!poolIdRemoveParent.equals(new Long("0")) && !transferPoolIds.contains(poolIdRemoveParent)) {
  			  needUpdatedPoolParentIdList.add(poolId);
  		  }
  	  }
  
      // Keep the transfer in its own transaction, including changes to composite access identifiers.
      try {
          sessionFactory.inTransaction(session -> {
              CriteriaBuilder cb = session.getCriteriaBuilder();
              if (!updatePoolOwnerIdList.isEmpty()) {
                  CriteriaDelete<QuestionPoolAccessData> deleteAccess = cb.createCriteriaDelete(QuestionPoolAccessData.class);
                  Root<QuestionPoolAccessData> deletedAccess = deleteAccess.from(QuestionPoolAccessData.class);
                  deleteAccess.where(deletedAccess.get("questionPoolId").in(updatePoolOwnerIdList),
                          cb.equal(deletedAccess.get("agentId"), ownerId));
                  session.createMutationQuery(deleteAccess).executeUpdate();

                  CriteriaUpdate<QuestionPoolAccessData> updateAccess = cb.createCriteriaUpdate(QuestionPoolAccessData.class);
                  Root<QuestionPoolAccessData> accessRoot = updateAccess.from(QuestionPoolAccessData.class);
                  updateAccess.set("agentId", ownerId).where(accessRoot.get("questionPoolId").in(updatePoolOwnerIdList),
                          cb.equal(accessRoot.get("accessTypeId"), QuestionPoolAccessFacade.ADMIN));
                  session.createMutationQuery(updateAccess).executeUpdate();

                  CriteriaUpdate<QuestionPoolData> updateOwner = cb.createCriteriaUpdate(QuestionPoolData.class);
                  Root<QuestionPoolData> ownerRoot = updateOwner.from(QuestionPoolData.class);
                  updateOwner.set("ownerId", ownerId).where(ownerRoot.get("questionPoolId").in(updatePoolOwnerIdList));
                  session.createMutationQuery(updateOwner).executeUpdate();
                  session.flush();
              }

              if (!needUpdatedPoolParentIdList.isEmpty()) {
                  CriteriaUpdate<QuestionPoolData> updateParent = cb.createCriteriaUpdate(QuestionPoolData.class);
                  Root<QuestionPoolData> parentRoot = updateParent.from(QuestionPoolData.class);
                  updateParent.set("parentPoolId", 0L).where(parentRoot.get("questionPoolId").in(needUpdatedPoolParentIdList));
                  session.createMutationQuery(updateParent).executeUpdate();
                  session.flush();
              }
          });
      } catch (Exception ex) {
          log.warn("Could not transfer question pools to owner {}", ownerId, ex);
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
					  title = AssessmentService.renameDuplicate(title);
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
              if (needUpdatedPoolParentIdList.contains(poolId)) {
				  pooldata.setParentPoolId(new Long("0"));
			  }
			  updatePool(pooldata);
		  }		  
	  } 	  
  }

	public Set<String> getAllItemHashes(@NonNull Long poolId) {
        CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<String> itemQuery = itemQueryBuilder.createQuery(String.class);
        Root<ItemData> itemRoot = itemQuery.from(ItemData.class);
        Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
        itemQuery.select(itemRoot.get("hash")).where(
            itemQueryBuilder.equal(itemRoot.get("itemId"), poolItemRoot.get("itemId")),
            itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), poolId.longValue()));

        return sessionFactory.getCurrentSession().createQuery(itemQuery).getResultList().stream().collect(Collectors.toSet());
	}

	public Long getItemCount(@NonNull Long poolId) {
        CriteriaBuilder itemQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Long> itemQuery = itemQueryBuilder.createQuery(Long.class);
        Root<QuestionPoolItemData> poolItemRoot = itemQuery.from(QuestionPoolItemData.class);
        itemQuery.select(itemQueryBuilder.count(poolItemRoot)).where(
            itemQueryBuilder.equal(poolItemRoot.get("questionPoolId"), poolId.longValue()));

        return sessionFactory.getCurrentSession().createQuery(itemQuery).getSingleResult();
	}

  public Long getSubPoolCount(@NonNull Long poolId) {
        CriteriaBuilder poolQueryBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Long> poolQuery = poolQueryBuilder.createQuery(Long.class);
        Root<QuestionPoolData> poolRoot = poolQuery.from(QuestionPoolData.class);
        poolQuery.select(poolQueryBuilder.count(poolRoot)).where(
            poolQueryBuilder.equal(poolRoot.get("parentPoolId"), poolId.longValue()));

        return sessionFactory.getCurrentSession().createQuery(poolQuery).getSingleResult();
	}
}
