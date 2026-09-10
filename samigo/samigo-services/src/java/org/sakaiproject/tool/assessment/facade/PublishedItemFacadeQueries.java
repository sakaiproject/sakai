/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class PublishedItemFacadeQueries implements PublishedItemFacadeQueriesAPI {

	@Setter private ItemHashUtil itemHashUtil;
	@Setter private SessionFactory sessionFactory;

	public IdImpl getItemId(String id) {
		return new IdImpl(id);
	}

	public IdImpl getItemId(Long id) {
		return new IdImpl(id);
	}

	public IdImpl getItemId(long id) {
		return new IdImpl(id);
	}

	public PublishedItemFacade getItem(Long itemId, String agent) {
		PublishedItemData item = (PublishedItemData) sessionFactory.getCurrentSession()
				.get(PublishedItemData.class, itemId);
		return new PublishedItemFacade(item);
	}
	
	public PublishedItemFacade getItem(String itemId) {
		PublishedItemData item = (PublishedItemData) sessionFactory.getCurrentSession()
				.get(PublishedItemData.class, Long.valueOf(itemId));
		return new PublishedItemFacade(item);
	}

	public Boolean itemExists(String itemId) {
		try {
			if (sessionFactory.getCurrentSession().get(PublishedItemData.class,  Long.valueOf(itemId))==null){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public Map<String, ItemFacade> getPublishedItemsByHash(String hash) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<PublishedItemData> cq = cb.createQuery(PublishedItemData.class);
		Root<PublishedItemData> root = cq.from(PublishedItemData.class);

		cq.select(root).where(cb.equal(root.get("hash"), hash));

		List<PublishedItemData> list1 = session.createQuery(cq).getResultList();

		Map<String, ItemFacade> itemFacadeMap = new HashMap();

		for (int i = 0; i < list1.size(); i++) {
			PublishedItemData a = (PublishedItemData) list1.get(i);
			ItemFacade f = new ItemFacade(a);
			itemFacadeMap.put(f.getItemIdString(),f);
		}
		return itemFacadeMap;
	}

	public void deleteItemContent(Long itemId, String agent) {
		Session session = sessionFactory.getCurrentSession();
		PublishedItemData item = session.get(PublishedItemData.class, itemId);

		if (item != null) { // need to dissociate with item before deleting in Hibernate 3
			item.getItemTextSet().clear();
			item.getItemMetaDataSet().clear();
			item.getItemFeedbackSet().clear();
			session.merge(item);
		}
	}

	@Override
	public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaUpdate<PublishedItemTag> update = cb.createCriteriaUpdate(PublishedItemTag.class);
		Root<PublishedItemTag> root = update.from(PublishedItemTag.class);

		update.set(root.get("tagLabel"), tagView.tagLabel)
			.set(root.get("tagCollectionId"), tagView.tagCollectionId)
			.set(root.get("tagCollectionName"), tagView.tagCollectionName)
			.where(cb.equal(root.get("tagId"), tagView.tagId));

		session.createQuery(update).executeUpdate();
	}

	@Override
	public void deleteItemTagBindingsHavingTagId(String tagId) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<PublishedItemTag> delete = cb.createCriteriaDelete(PublishedItemTag.class);
		Root<PublishedItemTag> root = delete.from(PublishedItemTag.class);

		delete.where(cb.equal(root.get("tagId"), tagId));

		session.createQuery(delete).executeUpdate();
	}

	@Override
	public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaUpdate<PublishedItemTag> update = cb.createCriteriaUpdate(PublishedItemTag.class);
		Root<PublishedItemTag> root = update.from(PublishedItemTag.class);

		update.set(root.get("tagCollectionName"), tagCollectionView.tagCollectionName)
			.where(cb.equal(root.get("tagCollectionId"), tagCollectionView.tagCollectionId));

		session.createQuery(update).executeUpdate();
	}

	@Override
	public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<PublishedItemTag> delete = cb.createCriteriaDelete(PublishedItemTag.class);
		Root<PublishedItemTag> root = delete.from(PublishedItemTag.class);

		delete.where(cb.equal(root.get("tagCollectionId"), tagCollectionId));

		session.createQuery(delete).executeUpdate();
	}

	@Override
	@Transactional(propagation = Propagation.NEVER)
	public BackfillItemHashResult backfillItemHashes(int batchSize, boolean backfillBaselineHashes) {
		return itemHashUtil.backfillItemHashes(
				batchSize,
				backfillBaselineHashes,
				PublishedItemData.class,
				i -> {
					final String hash = itemHashUtil.hashItemUnchecked(i);
					if ( StringUtils.isEmpty(i.getHash()) ) {
						i.setHash(hash);
					}
					if ( backfillBaselineHashes ) {
						if (StringUtils.isEmpty(((PublishedItemData) i).getItemHash())) {
							((PublishedItemData) i).setItemHash(hash);
						}
					}
					return i;
				});
	}

	@Override
	public Long getPublishedAssessmentId(Long itemId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);

		Root<PublishedSectionData> s = cq.from(PublishedSectionData.class);
		Root<PublishedItemData>   i = cq.from(PublishedItemData.class);

		cq.select(s.get("assessment").get("publishedAssessmentId"))
			.where(cb.and(
				cb.equal(s.get("id"), i.get("section")),
				cb.equal(i.get("itemId"), itemId)
			));

		List<Long> list1 = session.createQuery(cq)
			.setMaxResults(1)
			.getResultList();

		if (list1.isEmpty()) {
			return -1L;
		} else {
			return (Long) list1.get(0);
		}
	}

	@Override
 	public void removeItemAttachment(Long itemAttachmentId) {
		Session session = sessionFactory.getCurrentSession();
		PublishedItemAttachment itemAttachment = session.get(PublishedItemAttachment.class, itemAttachmentId);
		ItemDataIfc item = itemAttachment.getItem();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				if (item != null) {
					Set<ItemAttachmentIfc> itemAttachmentSet = item.getItemAttachmentSet();
					itemAttachmentSet.remove(itemAttachment);
					session.merge(item);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("Error while trying to delete PublishedItemAttachment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}
}
