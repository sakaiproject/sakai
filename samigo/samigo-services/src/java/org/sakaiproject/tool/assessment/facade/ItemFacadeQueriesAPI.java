/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;

public interface ItemFacadeQueriesAPI
{

  public IdImpl getItemId(String id);

  public IdImpl getItemId(Long id);

  public IdImpl getItemId(long id);

  public List getQPItems(Long questionPoolId);

  public List list();

  public void show(Long itemId);

  public void showType(Long typeId);

  public void listType();

  // DELETEME
  public void remove(Long itemId);

  public void deleteItem(Long itemId, String agent);

  public void deleteItemContent(Long itemId, String agent);

  public void deleteItemMetaData(Long itemId, String label);

  public void addItemMetaData(Long itemId, String label, String value);

  public void ifcShow(Long itemId);

  public ItemFacade saveItem(ItemFacade item) throws DataFacadeException;

  public List<ItemFacade> saveItems(List<ItemFacade> items) throws DataFacadeException;

  /**
   * Retrieve an item from storage
   * @param itemId the item id
   * @return the item or null if not found
   */
  public ItemFacade getItem(Long itemId);

  /**
   * Retrieve an item from storage
   * @param itemId the itemId
   * @param agent agentId not used
   * @return the item or null if not found
   * @deprecated use {@link getItem}
   */
  public ItemFacade getItem(Long itemId, String agent);

  public Map<String, ItemFacade> getItemsByHash(String hash);

  public Map<String, ItemFacade> getItemsByKeyword(String keyword);

  public Long getItemTextId(Long publishedItemId);
  
  public void deleteSet(Set s);

  public void updateItemTagBindingsHavingTag(TagServiceHelper.TagView tagView);

  public void deleteItemTagBindingsHavingTagId(String tagId);

  public void updateItemTagBindingsHavingTagCollection(TagServiceHelper.TagCollectionView tagCollectionView);

  public void deleteItemTagBindingsHavingTagCollectionId(String tagCollectionId);

  public BackfillItemHashResult backfillItemHashes(int batchSize);

  public List<Long> getItemsIdsByHash(String hash);

  public Long getAssessmentId(Long itemId);

  public Boolean itemExists(Long itemId);

}
