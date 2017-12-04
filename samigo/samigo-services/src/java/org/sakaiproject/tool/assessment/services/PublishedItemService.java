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

package org.sakaiproject.tool.assessment.services;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.BackfillItemHashResult;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;

@Slf4j
public class PublishedItemService extends ItemService {

	public ItemFacade getItem(Long itemId, String agentId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId, agentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return item;
	}
	
	public ItemFacade getItem(String itemId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return item;
	}

	public Map<String,ItemFacade> getPublishedItemsByHash(String hash) {
		try{
			return PersistenceService.getInstance().getPublishedItemFacadeQueries().getPublishedItemsByHash(hash);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e); throw new RuntimeException(e);
		}
	}

	public void deleteItemContent(Long itemId, String agentId) {
		try {
			PersistenceService.getInstance().getPublishedItemFacadeQueries()
					.deleteItemContent(itemId, agentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

    @Override
    public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView) {
        // prevailing log-and-throw exception handling pattern is nuts
        // we're not going to perpetuate that
        PersistenceService.getInstance().getPublishedItemFacadeQueries().updateItemTagBindingsHavingTag(tagView);
    }

    @Override
    public void deleteItemTagBindingsHavingTagId(String tagId) {
        // intentionally not perpetuating the prevailing log-and-throw exception handling pattern
        PersistenceService.getInstance().getPublishedItemFacadeQueries().deleteItemTagBindingsHavingTagId(tagId);
    }

    @Override
    public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView) {
        // intentionally not perpetuating the prevailing log-and-throw exception handling pattern.
        // at best that just results in duplicated logging
        PersistenceService.getInstance().getPublishedItemFacadeQueries().updateItemTagBindingsHavingTagCollection(tagCollectionView);
    }

    @Override
    public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId) {
        // intentionally not perpetuating the prevailing log-and-throw exception handling pattern.
        // at best that just results in duplicated logging
        PersistenceService.getInstance().getPublishedItemFacadeQueries().deleteItemTagBindingsHavingTagCollectionId(tagCollectionId);
    }

    @Override
    public BackfillItemHashResult backfillItemHashes(int batchSize, boolean backfillBaselineHashes) {
        return PersistenceService.getInstance().getPublishedItemFacadeQueries().backfillItemHashes(batchSize, backfillBaselineHashes);
    }
}
