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
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class ItemFacadeQueries implements ItemFacadeQueriesAPI {

  @Setter private ItemHashUtil itemHashUtil;
  @Setter private SessionFactory sessionFactory;

  public IdImpl getItemId(String id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(Long id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(long id){
    return new IdImpl(id);
  }

  public List<ItemData> list() {
    Session session = sessionFactory.getCurrentSession();
    Query<ItemData> q = session.createQuery("from ItemData", ItemData.class);
    return q.list();
  }

  public void show(Long itemId) {
    sessionFactory.getCurrentSession().get(ItemData.class, itemId);
  }

  public ItemFacade getItem(Long itemId, String agent) {
	return getItem(itemId);
  }

    public void deleteItem(Long itemId, String agent) {
        Session session = sessionFactory.getCurrentSession();
        ItemData item = session.get(ItemData.class, itemId);
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
            session.remove(item);
        }
    }

    public void deleteItemContent(Long itemId, String agent) {
        Session session = sessionFactory.getCurrentSession();
        ItemData item = session.get(ItemData.class, itemId);

        if (item != null) {
            item.getItemTextSet().clear();
            item.getItemMetaDataSet().clear();
            item.getItemFeedbackSet().clear();
            session.merge(item);
        }
    }

    public void deleteItemMetaData(final Long itemId, final String label) {
        Session session = sessionFactory.getCurrentSession();
        // delete metadata by label
        ItemData item = session.get(ItemData.class, itemId);

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ItemMetaDataIfc> cq = cb.createQuery(ItemMetaDataIfc.class);
        Root<ItemMetaData> root = cq.from(ItemMetaData.class);
        cq.where(
           cb.equal(root.get("item").get("itemId"), itemId),
           cb.equal(root.get("label"), label));

        List<ItemMetaDataIfc> itemmetadatalist = (List<ItemMetaDataIfc>) session.createQuery(cq).list();

        item.getItemMetaDataSet().removeAll(itemmetadatalist);
        session.merge(item);
    }

    public void addItemMetaData(Long itemId, String label, String value) {
        Session session = sessionFactory.getCurrentSession();
        ItemData item = (ItemData) session.get(ItemData.class, itemId);
        if (item != null) {
            log.debug("**Id = {}, **score = {}, **grade = {}, **CorrectFeedback is lazy = {}, **Objective not lazy = {}",
                    item.getItemId(),
                    item.getScore(),
                    item.getGrade(),
                    item.getCorrectItemFeedback(),
                    item.getItemMetaDataByLabel("ITEM_OBJECTIVE")
            );
            item.getItemMetaDataSet().add(new ItemMetaData(item, label, value));
            session.merge(item);
        }
    }

 public ItemFacade saveItem(ItemFacade item) throws DataFacadeException {
    List<ItemFacade> list = new ArrayList<>(1);
    list.add(item);
    list = saveItems(list);
    return list.isEmpty() ? null : list.get(0);
 }

    public void removeItemAttachment(Long itemAttachmentId) {
        Session session = sessionFactory.getCurrentSession();
        ItemAttachment itemAttachment = session.get(ItemAttachment.class, itemAttachmentId);
        ItemDataIfc item = itemAttachment.getItem();
        if (item != null) {
            item.getItemAttachmentSet().remove(itemAttachment);
            session.merge(item);
        }
    }

    public List<ItemFacade> saveItems(final List<ItemFacade> items) throws DataFacadeException {
        log.debug("Persist items: {}", items);
        Session session = sessionFactory.getCurrentSession();
        try {
            for (ItemFacade item : items) {
                ItemDataIfc itemData = item.getData();
                itemData.setLastModifiedDate(new Date());
                itemData.setLastModifiedBy(AgentFacade.getAgentString());
                itemData.setHash(itemHashUtil.hashItem(itemData));
                itemData = session.merge(itemData);
                item.setData(itemData);
                item.setItemId(itemData.getItemId());

                if (itemData.getSection() != null) {
                    AssessmentIfc assessment = itemData.getSection().getAssessment();
                    assessment.setLastModifiedBy(AgentFacade.getAgentString());
                    assessment.setLastModifiedDate(new Date());
                    session.merge(assessment);
                }
            }
            return items;
        } catch (Exception e) {
            log.warn("Could not save items, {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public BackfillItemHashResult backfillItemHashes(int batchSize) {
        return itemHashUtil.backfillItemHashes(
                batchSize,
                false,
                ItemData.class,
                i -> {
                    final String hash = itemHashUtil.hashItemUnchecked(i);
                    i.setHash(hash);
                    return i;
                });
    }

  public ItemFacade getItem(Long itemId) {
	  ItemData item;
	  Session session = sessionFactory.getCurrentSession();
	  try {
		  item = session.get(ItemData.class, itemId);
		  if (item == null) {
			  log.warn("unable to retrieve item [{}] because it does not exist", itemId);
			  return null;
		  }
	  } catch (DataAccessException e) {
		  log.warn("unable to retrieve item [{}] due to:", itemId, e);
		  return null;
	  }
	  return new ItemFacade(item);
  }

    public Boolean itemExists(Long itemId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            if (session.get(ItemData.class, itemId)==null){
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

  public Map<String, ItemFacade> getItemsByHash(String hash) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ItemData> cq = cb.createQuery(ItemData.class);
        Root<ItemData> root = cq.from(ItemData.class);
        cq.where(cb.equal(root.get("hash"), hash));

        List<ItemData> list1 = session.createQuery(cq).getResultList();

        Map<String, ItemFacade> itemFacadeMap = new HashMap();

        for (int i = 0; i < list1.size(); i++) {
            ItemData a = (ItemData) list1.get(i);
            ItemFacade f = new ItemFacade(a);
            itemFacadeMap.put(f.getItemIdString(),f);
        }
        return itemFacadeMap;
  }


  public Map<String, ItemFacade> getItemsByKeyword(final String keyword) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ItemData> cq1 = cb.createQuery(ItemData.class);
        Root<ItemData> root1 = cq1.from(ItemData.class);
        Join<ItemData, ItemText> join1 = root1.join("itemTextSet");
        cq1.select(root1).where(cb.like(join1.get("text"), keyword));
        List<ItemData> list1 = session.createQuery(cq1).getResultList();

        CriteriaQuery<ItemData> cq2 = cb.createQuery(ItemData.class);
        Root<ItemData> root2 = cq2.from(ItemData.class);
        Join<ItemData, ItemText> joinText2 = root2.join("itemTextSet");
        Join<ItemText, Answer> joinAnswer2 = joinText2.join("answerSet");
        cq2.select(root2).distinct(true).where(cb.like(joinAnswer2.get("text"), keyword));
        List<ItemData> list2 = session.createQuery(cq2).getResultList();

        CriteriaQuery<ItemData> cq3 = cb.createQuery(ItemData.class);
        Root<ItemData> root3 = cq3.from(ItemData.class);
        Join<ItemData, ItemMetaData> join3 = root3.join("itemMetaDataSet");
        cq3.select(root3).where(
                cb.like(join3.get("entry"), keyword),
                cb.equal(join3.get("label"), "KEYWORD")
        );
        List<ItemData> list3 = session.createQuery(cq3).getResultList();

        CriteriaQuery<ItemData> cq4 = cb.createQuery(ItemData.class);
        Root<ItemData> root4 = cq4.from(ItemData.class);
        cq4.select(root4).where(cb.like(root4.get("instruction"), keyword));
        List<ItemData> list4 = session.createQuery(cq4).getResultList();

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
	    Session session = sessionFactory.getCurrentSession();
	    CriteriaBuilder cb = session.getCriteriaBuilder();
	    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<PublishedItemText> root = cq.from(PublishedItemText.class);
	    cq.select(root.get("id"));
	    cq.where(cb.equal(root.get("item").get("itemId"), publishedItemId));
	    List<Long> list = session.createQuery(cq).list();

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
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ItemTag> cu = cb.createCriteriaUpdate(ItemTag.class);
        Root<ItemTag> root = cu.from(ItemTag.class);

        cu.set(root.get("tagLabel"), tagView.tagLabel);
        cu.set(root.get("tagCollectionId"), tagView.tagCollectionId);
        cu.set(root.get("tagCollectionName"), tagView.tagCollectionName);
        cu.where(cb.equal(root.get("tagId"), tagView.tagId));

        session.createMutationQuery(cu).executeUpdate();
    }

    @Override
    public void deleteItemTagBindingsHavingTagId(String tagId) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<ItemTag> cd = cb.createCriteriaDelete(ItemTag.class);
        Root<ItemTag> root = cd.from(ItemTag.class);

        cd.where(cb.equal(root.get("tagId"), tagId));

        session.createMutationQuery(cd).executeUpdate();
    }

    @Override
    public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ItemTag> cu = cb.createCriteriaUpdate(ItemTag.class);
        Root<ItemTag> root = cu.from(ItemTag.class);

        cu.set(root.get("tagCollectionName"), tagCollectionView.tagCollectionName);
        cu.where(cb.equal(root.get("tagCollectionId"), tagCollectionView.tagCollectionId));

        session.createMutationQuery(cu).executeUpdate();
    }

    @Override
    public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId) {
        // TODO when we add item search indexing, this is going to have to change to
        // first read in all the affected item IDs so we can generate events for each
        // (similar to what we do in the tag service)
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<ItemTag> cd = cb.createCriteriaDelete(ItemTag.class);
        Root<ItemTag> root = cd.from(ItemTag.class);

        cd.where(cb.equal(root.get("tagCollectionId"), tagCollectionId));

        session.createMutationQuery(cd).executeUpdate();
    }


    @Override
    public List<Long> getItemsIdsByHash(String hash) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ItemData> root = cq.from(ItemData.class);
        cq.select(root.get("itemId"));
        cq.where(cb.equal(root.get("hash"), hash));

        List<Long> list1 = session.createQuery(cq).list();

        return list1;
    }



    @Override
    public Long getAssessmentId(Long itemId) {
    	Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ItemData> itemRoot = cq.from(ItemData.class);
        Join<ItemData, SectionData> sectionJoin = itemRoot.join("section");
        cq.select(sectionJoin.get("assessment").get("assessmentBaseId"));
        cq.where(cb.equal(itemRoot.get("itemId"), itemId));

        List<Long> list1 = session.createQuery(cq).list();

        if (list1.isEmpty()) {
            return -1L;
        } else {
            return list1.get(0).longValue();
        }
    }
}
