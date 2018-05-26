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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.ALL_HASH_BACKFILLABLE_ITEM_IDS_HQL;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.ID_PARAMS_PLACEHOLDER;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.ITEMS_BY_ID_HQL;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.TOTAL_HASH_BACKFILLABLE_ITEM_COUNT_HQL;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.TOTAL_ITEM_COUNT_HQL;

@Slf4j
public class ItemFacadeQueries extends HibernateDaoSupport implements ItemFacadeQueriesAPI {

    private ItemHashUtil itemHashUtil;

    public void setItemHashUtil(ItemHashUtil itemHashUtil) {
        this.itemHashUtil = itemHashUtil;
    }

  public ItemFacadeQueries() {
  }

  public IdImpl getItemId(String id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(Long id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(long id){
    return new IdImpl(id);
  }



  public List getQPItems(final Long questionPoolId) {
	    final HibernateCallback<List<ItemData>> hcb = session -> {
            Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItem qpi where qpi.itemId=ab.itemIdString and qpi.questionPoolId = :id");
            q.setLong("id", questionPoolId);
            return q.list();
        };
	    return getHibernateTemplate().execute(hcb);
  }

  public List list() {
    return getHibernateTemplate().find("from ItemData");
  }

  public void show(Long itemId) {
    getHibernateTemplate().load(ItemData.class, itemId);
  }

  public ItemFacade getItem(Long itemId, String agent) {
	return getItem(itemId);
  }

  public void showType(Long typeId) {
    getHibernateTemplate().load(TypeD.class, typeId);
  }

  public void listType() {
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    TypeFacade f = typeFacadeQueries.getTypeFacadeById(1L);
    log.debug("***facade: "+f.getAuthority());
  }

  public void remove(Long itemId) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);

    // get list of attachment in section
    AssessmentService service = new AssessmentService();
    List itemAttachmentList = service.getItemResourceIdList(item);
    service.deleteResources(itemAttachmentList);

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem deleting item : "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
      if (item != null) {
        printItem(item);
      }
  }

  public void deleteItem(Long itemId, String agent) {
	ItemData item = null;
    try { 
    	item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId); 
    } catch (DataAccessException e) {
    	log.warn("unable to retrieve item " + itemId + " due to:" + e);
    	return; 
    }
    // get list of attachment in item
    AssessmentService service = new AssessmentService();
    List itemAttachmentList = service.getItemResourceIdList(item);
    service.deleteResources(itemAttachmentList);

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
	SectionDataIfc section = item.getSection();
        // section might be null if you are deleting an item created inside a pool, that's not linked to any assessment. 
        if (section !=null) {
          Set set = section.getItemSet();
          set.remove(item);
        }
        getHibernateTemplate().delete(item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem deleting item: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }



  // is this used by ItemAddListener to save item? -daisyf
  public void deleteItemContent(Long itemId, String agent) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
          Set set = item.getItemTextSet();
          item.setItemTextSet(new HashSet());
          getHibernateTemplate().deleteAll(set);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem deleteItemTextSet: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }

    retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
          Set set = item.getItemMetaDataSet();
          item.setItemMetaDataSet(new HashSet());
          getHibernateTemplate().deleteAll(set);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem deleteItemMetaDataSet: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }

    retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
          Set set = item.getItemFeedbackSet();
          item.setItemFeedbackSet(new HashSet());
          getHibernateTemplate().deleteAll(set);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem deleting ItemFeedbackSet: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }

  public void deleteItemMetaData(final Long itemId, final String label) {
    // delete metadata by label
    ItemData item = getHibernateTemplate().load(ItemData.class, itemId);

    final HibernateCallback<List<ItemMetaData>> hcb = session -> {
        Query q = session.createQuery("from ItemMetaData imd where imd.item.itemId = :id and imd.label = :label");
        q.setLong("id", itemId);
        q.setString("label", label);
        return q.list();
    };
    List<ItemMetaData> itemmetadatalist = getHibernateTemplate().execute(hcb);

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
	  Iterator iter = itemmetadatalist.iterator();
	  while (iter.hasNext()){
	    ItemMetaDataIfc meta= (ItemMetaDataIfc) iter.next();
            meta.setItem(null);
	  }
          
          Set set = item.getItemMetaDataSet();
          set.removeAll(itemmetadatalist);
          item.setItemMetaDataSet(set);
          getHibernateTemplate().deleteAll(itemmetadatalist);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem delete itemmetadatalist: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }


  public void addItemMetaData(Long itemId, String label, String value) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);
      if (item != null) {
        printItem(item);

    ItemMetaData itemmetadata = new ItemMetaData(item, label, value);
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(itemmetadata);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving itemmetadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
    //item.addItemMetaData(label, value);
    //getHibernateTemplate().saveOrUpdate(item);
      }
  }

  private void printItem(ItemData item) {
    log.debug("**Id = " + item.getItemId());
    log.debug("**score = " + item.getScore());
    log.debug("**grade = " + item.getGrade());
    log.debug("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    log.debug("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
  }

  public void ifcShow(Long itemId) {
      ItemDataIfc itemData = (ItemDataIfc) getHibernateTemplate().load(ItemData.class, itemId);
      if (itemData != null) {
        printIfcItem(itemData);
        printFacadeItem(itemData);
        //exportXml(itemData);
      }
  }


 public ItemFacade saveItem(ItemFacade item) throws DataFacadeException {
    List<ItemFacade> list = new ArrayList<>(1);
    list.add(item);
    list = saveItems(list);
    return list.isEmpty() ? null : list.get(0);
 }

  /**
   * Similar to saveItem(ItemFacade item), only we can process many items within a single transaction, thereby improving performance
   * @param items
   * @return
   */
  public List<ItemFacade> saveItems(List<ItemFacade> items) throws DataFacadeException {
    try {
      int retryCount;
      // Track assessments associated with each item
      List<AssessmentIfc> assessmentsToUpdate = new ArrayList<>();
      Set<Long> assessmentIds = new HashSet<>();

      for (ItemFacade item : items) {
        ItemDataIfc itemdata = (ItemDataIfc) item.getData();
        itemdata.setLastModifiedDate(new Date());
        itemdata.setLastModifiedBy(AgentFacade.getAgentString());
        itemdata.setHash(itemHashUtil.hashItem(itemdata));
        retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();

        while (retryCount > 0) {
          try {
            getHibernateTemplate().saveOrUpdate(itemdata);
            item.setItemId(itemdata.getItemId());
            retryCount = 0;
          } catch (Exception e) {
            log.warn("saveitems - problem save or update itemdata: {}", e.getMessage());
            retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
          }
        }

        // Update the assessment once after all items are updated. So just track them here
        if (item.getData() != null && item.getData().getSection() != null) {
          AssessmentIfc assessment = item.getData().getSection().getAssessment();
          if (!assessmentIds.contains(assessment.getAssessmentId())) {
            assessmentIds.add(assessment.getAssessmentId());
            assessmentsToUpdate.add(item.getData().getSection().getAssessment());
          }
        }
      }

      // All items are updated, now mark their associated assessments' "LastModified" properties
      for (AssessmentIfc assessment : assessmentsToUpdate) {
        assessment.setLastModifiedBy(AgentFacade.getAgentString());
        assessment.setLastModifiedDate(new Date());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();

        while (retryCount > 0) {
          try {
            getHibernateTemplate().update(assessment);
            retryCount = 0;
          } catch (Exception e) {
            log.warn("save items: problem updating assessment: {}", e.getMessage());
            retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
          }
        }
      }

      return items;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

    private static final Map<String,String> BACKFILL_HASHES_HQL = new HashMap<String,String>() {{
        this.put(TOTAL_ITEM_COUNT_HQL, "select count(*) from ItemData");
        this.put(TOTAL_HASH_BACKFILLABLE_ITEM_COUNT_HQL, "select count(*) from ItemData as item where item.hash is null");
        this.put(ALL_HASH_BACKFILLABLE_ITEM_IDS_HQL, "select item.id from ItemData as item where item.hash is null");
        this.put(ITEMS_BY_ID_HQL, "select item from ItemData as item where item.id in (" + ID_PARAMS_PLACEHOLDER + ")");
    }};

    @Override
    public BackfillItemHashResult backfillItemHashes(int batchSize) {
        return itemHashUtil.backfillItemHashes(
                batchSize,
                BACKFILL_HASHES_HQL,
                ItemData.class,
                i -> {
                    final String hash = itemHashUtil.hashItemUnchecked(i);
                    i.setHash(hash);
                    return i;
                },
                getHibernateTemplate());
    }

  private void printIfcItem(ItemDataIfc item) {
    log.debug("**Id = " + item.getItemId());
    log.debug("**score = " + item.getScore());
    log.debug("**grade = " + item.getGrade());
    log.debug("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    log.debug("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    log.debug("**createdDate = " +
                       item.getCreatedDate());
  }

  private void printFacadeItem(ItemDataIfc item) {
    ItemFacade f = new ItemFacade(item);
    log.debug("****Id = " + f.getItemId());
    log.debug("****score = " + f.getScore());
    log.debug("****grade = " + f.getGrade());
    log.debug("****CorrectFeedback is lazy = " +
                       f.getCorrectItemFeedback());
    log.debug("****Objective not lazy = " +
                       f.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    log.debug("****createdDate = " +
                       f.getCreatedDate());
    log.debug("****ItemType = " +
                       f.getItemType().getKeyword());
  }

  public ItemFacade getItem(Long itemId) {
	  ItemData item = null;
	  try {
		  item = (ItemData) getHibernateTemplate().load(ItemData.class, itemId);
	  } catch (DataAccessException e) {
		  log.warn("unable to retrieve item " + itemId + " due to:", e);
		  return null;
	  }
	  return new ItemFacade(item);
  }

    public Boolean itemExists(Long itemId) {
        try {
            if (getHibernateTemplate().get(ItemData.class, itemId)==null){
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

  public Map<String, ItemFacade> getItemsByHash(String hash) {
        final HibernateCallback<List<ItemData>> hcb = session -> {
            Query q = session.createQuery("from ItemData where hash = ? ");
            q.setString(0, hash);
            return q.list();

        };
        List<ItemData> list1 = getHibernateTemplate().execute(hcb);

        Map<String, ItemFacade> itemFacadeMap = new HashMap();

        for (int i = 0; i < list1.size(); i++) {
            ItemData a = (ItemData) list1.get(i);
            ItemFacade f = new ItemFacade(a);
            itemFacadeMap.put(f.getItemIdString(),f);
        }
        return itemFacadeMap;
  }


  public Map<String, ItemFacade> getItemsByKeyword(final String keyword) {
	    final HibernateCallback<List<ItemData>> hcb = session -> {
            Query q = session.createQuery("select ab from ItemData ab, ItemText itext where itext.item=ab and itext.text like :text");
            q.setString("text", keyword);
            return q.list();
        };
	    List<ItemData> list1 = getHibernateTemplate().execute(hcb);

	    final HibernateCallback<List<ItemData>> hcb2 = session -> {
            Query q = session.createQuery("select distinct ab from ItemData ab, Answer answer where answer.item=ab and answer.text like :text");
            q.setString("text", keyword);
            return q.list();
        };
	    List<ItemData> list2 = getHibernateTemplate().execute(hcb2);

	    final HibernateCallback<List<ItemData>> hcb3 = session -> {
            Query q = session.createQuery("select ab from ItemData ab, ItemMetaData md where md.item=ab and md.entry like :keyword and md.label = :label");
            q.setString("keyword", keyword);
            q.setString("label", "KEYWORD");
            return q.list();
        };
	    List<ItemData> list3 = getHibernateTemplate().execute(hcb3);

	    final HibernateCallback<List<ItemData>> hcb4 = session -> {
            Query q = session.createQuery("select ab from ItemData ab where ab.instruction like :keyword");
            q.setString("keyword", keyword);
            return q.list();
        };
	    List<ItemData> list4 = getHibernateTemplate().execute(hcb4);

    Map<String, ItemFacade> itemfacadeMap = new HashMap();

    for (int i = 0; i < list1.size(); i++) {
      ItemData a = list1.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }
    for (int i = 0; i < list2.size(); i++) {
      ItemData a = list2.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }
    for (int i = 0; i < list3.size(); i++) {
      ItemData a = list3.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }
    for (int i = 0; i < list4.size(); i++) {
      ItemData a = list4.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }

    log.debug("Search for keyword, found: " + itemfacadeMap.size());
    return itemfacadeMap;

  }

  /*
   * This API is for linear access to create a dummy record to indicate the student
   * has taken action on the item (question). Therefore, we just need one itemTextId
   * for recording - use the first one (index 0).
   */
  public Long getItemTextId(final Long publishedItemId) {
	    final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery("select i.id from PublishedItemText i where i.item.itemId = :id");
            q.setLong("id", publishedItemId);
            return q.list();
        };
	    List<Long> list = getHibernateTemplate().execute(hcb);
	    log.debug("list.size() = " + list.size());
	    Long itemTextId = -1l;
	    if (!list.isEmpty()) itemTextId = list.get(0);
	    log.debug("itemTextId" + itemTextId);
	    return itemTextId;
  }

  public void deleteSet(Set s) {
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				if (s != null) { // need to dissociate with item before deleting in Hibernate 3
					getHibernateTemplate().deleteAll(s);
					retryCount = 0;
				} else {
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem deleteSet: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e,
						retryCount);
			}
		}
	}

    @Override
    public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().bulkUpdate("update ItemTag it " +
                        "set it.tagLabel = ?, it.tagCollectionId = ?, it.tagCollectionName = ? " +
                        "where it.tagId = ?",
                tagView.tagLabel, tagView.tagCollectionId, tagView.tagCollectionName, tagView.tagId);
    }

    @Override
    public void deleteItemTagBindingsHavingTagId(String tagId) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().bulkUpdate("delete ItemTag it where it.tagId = ?", tagId);
    }

    @Override
    public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().bulkUpdate("update ItemTag it " +
                        "set it.tagCollectionName = ? " +
                        "where it.tagCollectionId = ?",
                tagCollectionView.tagCollectionName, tagCollectionView.tagCollectionId);
    }

    @Override
    public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().bulkUpdate("delete ItemTag it where it.tagCollectionId = ?", tagCollectionId);
    }


    @Override
    public List<Long> getItemsIdsByHash(String hash) {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery("select ab.itemId from ItemData ab where ab.hash = ? ");
            q.setString(0, hash);
            return q.list();

        };
        List<Long> list1 = getHibernateTemplate().execute(hcb);
        return list1;
    }



    @Override
    public Long getAssessmentId(Long itemId) {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery("select s.assessment.assessmentBaseId from SectionData s, ItemData i where s.id = i.section AND i.itemId = ?");
            q.setLong(0, itemId);
            return q.list();

        };
        List<Long> list1 = getHibernateTemplate().execute(hcb);
        if (list1.isEmpty()){
            return -1L;
        }else{
            return (Long) list1.get(0);
        }

    }


}
