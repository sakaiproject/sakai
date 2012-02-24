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


package org.sakaiproject.tool.assessment.shared.api.assessment;

import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

/**
 * The ItemServiceAPI declares a shared interface to get/set item
 * information.
 */
public interface ItemServiceAPI
{
  /**
   * Get a particular item from the backend, with all questions.
   *
   * @param itemId
   * @param agentId
   * @return
   */
  public ItemDataIfc getItem(Long itemId, String agentId);

  /**
   * Delete a item
   *
   * @param itemId
   * @param agentId
   */
  public void deleteItem(Long itemId, String agentId);

  /**
   * Delete itemtextset for an item, used for modify
   *
   * @param itemId
   * @param agentId
   */
  public void deleteItemContent(Long itemId, String agentId);

  /**
   * Delete metadata for an item, used for modify
   * param:  itemid, label, agentId
   *
   * @param itemId
   * @param label
   * @param agentId
   */
  public void deleteItemMetaData(Long itemId, String label, String agentId);

  /**
   * Add metadata for an item, used for modify
   * param:  itemid, label, value, agentId
   *
   * @param itemId
   * @param label
   * @param value
   * @param agentId
   */
  public void addItemMetaData(Long itemId, String label, String value,
                              String agentId);

  /**
   * Save item.
   * @param item interface
   * @return item interface
   */
  public ItemDataIfc saveItem(ItemDataIfc item);

  /**
   * Get item.
   * @param itemId
   * @return item interface
   */
  public ItemDataIfc getItem(String itemId);

  /**
   * Search for items.
   * @param keyword
   * @return Map of ItemDataIfcs with item idstrings as keys
   */
  public Map getItemsByKeyword(String keyword);

}
