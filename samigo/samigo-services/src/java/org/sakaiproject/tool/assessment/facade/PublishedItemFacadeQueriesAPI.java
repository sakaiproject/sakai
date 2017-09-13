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

import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

import java.util.List;
import java.util.Map;

public interface PublishedItemFacadeQueriesAPI {

	public IdImpl getItemId(String id);

	public IdImpl getItemId(Long id);

	public IdImpl getItemId(long id);

	public PublishedItemFacade getItem(Long itemId, String agent);
	
	public PublishedItemFacade getItem(String itemId);

	public Map<String, ItemFacade> getPublishedItemsByHash(String hash);
	
	public void deleteItemContent(Long itemId, String agent);

	public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView);

	public void deleteItemTagBindingsHavingTagId(String tagId);

	public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView);

	public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId);

	BackfillItemHashResult backfillItemHashes(int batchSize, boolean backfillBaselineHashes);

	public List<Long> getPublishedItemsIdsByHash(String hash);

	public Long getPublishedAssessmentId(Long itemId);

	public Boolean itemExists(String itemId);
}
