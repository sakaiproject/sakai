/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.shared.impl.assessment;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentServiceException;
import org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI;

/**
 * AssessmentServiceImpl implements a shared interface to get/set item
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
// Our API just uses our internal service. If we want, we can always replace
// this internal service and use its implementation as our own.
// Note that ItemFacade implements ItemDataIfc.
public class ItemServiceImpl implements ItemServiceAPI
{

 /**
 * Get a particular item.
 *
 * @param itemId the item id
 * @param agentId the agent id
 * @return the item data interface
 */
  public ItemDataIfc getItem(Long itemId, String agentId)
  {
    ItemFacade item = null;
    try
    {
      ItemService service = new ItemService();
      item = service.getItem(itemId, agentId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }

    return item;
  }

  /**
   * Delete a item.
   *
   * @param itemId the item id
   * @param agentId the agent id
   */
  public void deleteItem(Long itemId, String agentId)
  {
    try
    {
      ItemService service = new ItemService();
      service.deleteItem(itemId, agentId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }
  }

  /**
   * Delete itemtextset for an item, used for modify
   *
   * @param itemId the item id
   * @param agentId the agent id
   */
  public void deleteItemContent(Long itemId, String agentId)
  {
    try
    {
      ItemService service = new ItemService();
      service.deleteItemContent(itemId, agentId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }
  }

  /**
   * Delete metadata for an item, used for modify.
   * param:  itemid, label, agentId
   *
   * @param itemId the item id
   * @param label the metadata label
   * @param agentId the agent id
   */
  public void deleteItemMetaData(Long itemId, String label, String agentId)
  {
    try
    {
      ItemService service = new ItemService();
      service.deleteItemMetaData(itemId, label, agentId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }
  }

  /**
   * Add metadata for an item, used for modify
   * param:  itemid, label, value, agentId
   *
   * @param itemId the item id
   * @param label the metadata label
   * @param value  the value for the label
   * @param agentId the agent id
   */
  public void addItemMetaData(Long itemId, String label, String value,
                              String agentId)
  {
    try
    {
      ItemService service = new ItemService();
      service.addItemMetaData(itemId, label, value, agentId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }
  }

  /**
   * Save item.
   * @param item interface
   * @return item interface
   */
  public ItemDataIfc saveItem(ItemDataIfc item)
  {
    try
    {
      String itemId = item.getItemIdString();
      ItemService service = new ItemService();
      item = service.saveItem(service.getItem(itemId));
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }

    return item;
  }

  /**
   * Get item.
   * @param itemId the item id
   * @return item interface
   */
  public ItemDataIfc getItem(String itemId)
  {
    ItemFacade item = null;
    try
    {
      ItemService service = new ItemService();
      item = service.getItem(itemId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }

    return item;
  }

  /**
   * Search for items.
   * @param keyword the keyword to search by.
   * @return Map of ItemDataIfcs with item id strings as keys.
   */

  public Map getItemsByKeyword(String keyword)
  {
    Map itemKeywordMap = new HashMap();
    try
    {
      ItemService service = new ItemService();
      itemKeywordMap = service.getItemsByKeyword(keyword);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }

    return itemKeywordMap;
  }
}
