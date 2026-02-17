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
import org.hibernate.query.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublishedItemFacadeQueries extends HibernateDaoSupport implements
		PublishedItemFacadeQueriesAPI {

	@Setter private ItemHashUtil itemHashUtil;

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
		PublishedItemData item = (PublishedItemData) getHibernateTemplate()
				.get(PublishedItemData.class, itemId);
		return new PublishedItemFacade(item);
	}
	
	public PublishedItemFacade getItem(String itemId) {
		PublishedItemData item = (PublishedItemData) getHibernateTemplate()
				.get(PublishedItemData.class, Long.valueOf(itemId));
		return new PublishedItemFacade(item);
	}

	public Boolean itemExists(String itemId) {
		try {
			if (getHibernateTemplate().get(PublishedItemData.class,  Long.valueOf(itemId))==null){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public Map<String, ItemFacade> getPublishedItemsByHash(String hash) {
		final HibernateCallback<List<PublishedItemData>> hcb = session -> {
				Query q = session.createQuery("from PublishedItemData where hash = ? ");
				q.setParameter(0, hash);
				return q.list();
		};
		List<PublishedItemData> list1 = getHibernateTemplate().execute(hcb);

		Map<String, ItemFacade> itemFacadeMap = new HashMap();

		for (int i = 0; i < list1.size(); i++) {
			PublishedItemData a = (PublishedItemData) list1.get(i);
			ItemFacade f = new ItemFacade(a);
			itemFacadeMap.put(f.getItemIdString(),f);
		}
		return itemFacadeMap;
	}

	public void deleteItemContent(Long itemId, String agent) {
		PublishedItemData item = getHibernateTemplate().get(PublishedItemData.class, itemId);

		if (item != null) { // need to dissociate with item before deleting in Hibernate 3
			item.getItemTextSet().clear();
			item.getItemMetaDataSet().clear();
			item.getItemFeedbackSet().clear();
			getHibernateTemplate().merge(item);
		}
	}

	@Override
	public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		getHibernateTemplate().bulkUpdate("update PublishedItemTag it " +
				"set it.tagLabel = ?, it.tagCollectionId = ?, it.tagCollectionName = ? " +
				"where it.tagId = ?",
				tagView.tagLabel, tagView.tagCollectionId, tagView.tagCollectionName, tagView.tagId);
	}

	@Override
	public void deleteItemTagBindingsHavingTagId(String tagId) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		getHibernateTemplate().bulkUpdate("delete PublishedItemTag it where it.tagId = ?", tagId);
	}

	@Override
	public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		getHibernateTemplate().bulkUpdate("update PublishedItemTag it " +
						"set it.tagCollectionName = ? " +
						"where it.tagCollectionId = ?",
				tagCollectionView.tagCollectionName, tagCollectionView.tagCollectionId);
	}

	@Override
	public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId) {
		// TODO when we add item search indexing, this is going to have to change to
		// first read in all the affected item IDs so we can generate events for each
		// (similar to what we do in the tag service)
		getHibernateTemplate().bulkUpdate("delete PublishedItemTag it where it.tagCollectionId = ?", tagCollectionId);
	}

	@Override
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
		final HibernateCallback<List<Long>> hcb = session -> {
			Query q = session.createQuery("select s.assessment.publishedAssessmentId from PublishedSectionData s, PublishedItemData i where s.id = i.section AND i.itemId = ?");
			q.setParameter(0, itemId);
			return q.list();
		};
		List<Long> list1 = getHibernateTemplate().execute(hcb);
		if (list1.isEmpty()) {
			return -1L;
		} else {
			return (Long) list1.get(0);
		}
	}

	@Override
 	public void removeItemAttachment(Long itemAttachmentId) {
		PublishedItemAttachment itemAttachment = getHibernateTemplate().get(PublishedItemAttachment.class, itemAttachmentId);
		ItemDataIfc item = itemAttachment.getItem();
		int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
		while (retryCount > 0) {
			try {
				if (item != null) {
					Set<ItemAttachmentIfc> itemAttachmentSet = item.getItemAttachmentSet();
					itemAttachmentSet.remove(itemAttachment);
					getHibernateTemplate().merge(item);
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("Error while trying to delete PublishedItemAttachment: " + e.getMessage());
				retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
			}
		}
	}
}
