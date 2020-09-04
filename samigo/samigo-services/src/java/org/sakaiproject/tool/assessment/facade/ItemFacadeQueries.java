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

import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.ALL_HASH_BACKFILLABLE_ITEM_IDS_HQL;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.ID_PARAMS_PLACEHOLDER;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.ITEMS_BY_ID_HQL;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.TOTAL_HASH_BACKFILLABLE_ITEM_COUNT_HQL;
import static org.sakaiproject.tool.assessment.facade.ItemHashUtil.TOTAL_ITEM_COUNT_HQL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.query.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemFacadeQueries extends HibernateDaoSupport implements ItemFacadeQueriesAPI {

  @Setter private ItemHashUtil itemHashUtil;

  public IdImpl getItemId(String id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(Long id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(long id){
    return new IdImpl(id);
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

    public void deleteItem(Long itemId, String agent) {
        ItemData item = getHibernateTemplate().get(ItemData.class, itemId);
        // get list of attachment in item
        if (item != null) {
            AssessmentService service = new AssessmentService();
            List itemAttachmentList = service.getItemResourceIdList(item);
            service.deleteResources(itemAttachmentList);

            SectionDataIfc section = item.getSection();
            // section might be null if you are deleting an item created inside a pool, that's not linked to any assessment.
            if (section != null) {
                section.getItemSet().remove(item);
            }
            getHibernateTemplate().delete(item);
        }
    }

    public void deleteItemContent(Long itemId, String agent) {
        ItemData item = getHibernateTemplate().get(ItemData.class, itemId);

        if (item != null) {
            item.getItemTextSet().clear();
            item.getItemMetaDataSet().clear();
            item.getItemFeedbackSet().clear();
            getHibernateTemplate().merge(item);
        }
    }

    public void deleteItemMetaData(final Long itemId, final String label) {
        // delete metadata by label
        ItemData item = getHibernateTemplate().get(ItemData.class, itemId);

        List<ItemMetaDataIfc> itemmetadatalist = (List<ItemMetaDataIfc>) getHibernateTemplate()
                .findByNamedParam("from ItemMetaData imd where imd.item.itemId = :id and imd.label = :label",
                        new String[] {"id", "label"},
                        new Object[] {itemId, label});

        item.getItemMetaDataSet().removeAll(itemmetadatalist);
        getHibernateTemplate().merge(item);
    }

    public void addItemMetaData(Long itemId, String label, String value) {
        ItemData item = (ItemData) getHibernateTemplate().get(ItemData.class, itemId);
        if (item != null) {
            log.debug("**Id = {}, **score = {}, **grade = {}, **CorrectFeedback is lazy = {}, **Objective not lazy = {}",
                    item.getItemId(),
                    item.getScore(),
                    item.getGrade(),
                    item.getCorrectItemFeedback(),
                    item.getItemMetaDataByLabel("ITEM_OBJECTIVE")
            );
            item.getItemMetaDataSet().add(new ItemMetaData(item, label, value));
            getHibernateTemplate().merge(item);
        }
    }

 public ItemFacade saveItem(ItemFacade item) throws DataFacadeException {
    List<ItemFacade> list = new ArrayList<>(1);
    list.add(item);
    list = saveItems(list);
    return list.isEmpty() ? null : list.get(0);
 }

    public void removeItemAttachment(Long itemAttachmentId) {
        ItemAttachment itemAttachment = getHibernateTemplate().load(ItemAttachment.class, itemAttachmentId);
        ItemDataIfc item = itemAttachment.getItem();
        if (item != null) {
            item.getItemAttachmentSet().remove(itemAttachment);
            getHibernateTemplate().merge(item);
        }
    }

    public List<ItemFacade> saveItems(final List<ItemFacade> items) throws DataFacadeException {
        log.debug("Persist items: {}", items);
        try {
            for (ItemFacade item : items) {
                ItemDataIfc itemData = item.getData();
                itemData.setLastModifiedDate(new Date());
                itemData.setLastModifiedBy(AgentFacade.getAgentString());
                itemData.setHash(itemHashUtil.hashItem(itemData));
                itemData = getHibernateTemplate().merge(itemData);
                item.setData(itemData);
                item.setItemId(itemData.getItemId());

                if (itemData.getSection() != null) {
                    AssessmentIfc assessment = itemData.getSection().getAssessment();
                    assessment.setLastModifiedBy(AgentFacade.getAgentString());
                    assessment.setLastModifiedDate(new Date());
                    getHibernateTemplate().merge(assessment);
                }
            }
            return items;
        } catch (Exception e) {
            log.warn("Could not save items, {}", e.getMessage(), e);
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

        List<ItemData> list1 = (List<ItemData>) getHibernateTemplate()
                .findByNamedParam("from ItemData where hash = :hash", "hash", hash);

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
      List<Long> list = getHibernateTemplate().execute(session -> session
            .createQuery("select i.id from PublishedItemText i where i.item.itemId = :id")
            .setParameter("id", publishedItemId)
            .list());

	    log.debug("list.size() = {}", list.size());
	    Long itemTextId = -1l;
	    if (!list.isEmpty()) itemTextId = list.get(0);
	    log.debug("itemTextId {}", itemTextId);
	    return itemTextId;
  }

    @Override
    public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().execute(session -> session
                .createQuery("update ItemTag it set it.tagLabel = ?1, it.tagCollectionId = ?2, it.tagCollectionName = ?3 where it.tagId = ?4")
                .setParameter(1, tagView.tagLabel)
                .setParameter(2, tagView.tagCollectionId)
                .setParameter(3, tagView.tagCollectionName)
                .setParameter(4, tagView.tagId)
                .executeUpdate());
    }

    @Override
    public void deleteItemTagBindingsHavingTagId(String tagId) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().execute(session -> session
                .createQuery("delete ItemTag it where it.tagId = ?1")
                .setParameter(1, tagId)
                .executeUpdate());
    }

    @Override
    public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().execute(session -> session
                .createQuery("update ItemTag it set it.tagCollectionName = ?1 where it.tagCollectionId = ?2")
                .setParameter(1, tagCollectionView.tagCollectionName)
                .setParameter(2, tagCollectionView.tagCollectionId)
                .executeUpdate());
    }

    @Override
    public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        getHibernateTemplate().execute(session -> session
                .createQuery("delete ItemTag it where it.tagCollectionId = ?1")
                .setParameter(1, tagCollectionId)
                .executeUpdate());
    }


    @Override
    public List<Long> getItemsIdsByHash(String hash) {
        List<Long> list1 = getHibernateTemplate().execute(session -> session
                .createQuery("select ab.itemId from ItemData ab where ab.hash = ?1 ")
                .setParameter(1, hash)
                .list());

        return list1;
    }



    @Override
    public Long getAssessmentId(Long itemId) {
        List<Number> list1 = getHibernateTemplate().execute(session -> session
            .createQuery("select s.assessment.assessmentBaseId from SectionData s, ItemData i where s.id = i.section AND i.itemId = ?1")
            .setParameter(1, itemId)
            .list());

        if (list1.isEmpty()) {
            return -1L;
        } else {
            return list1.get(0).longValue();
        }
    }
}
