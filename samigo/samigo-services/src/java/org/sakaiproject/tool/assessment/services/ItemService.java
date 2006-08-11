/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.services;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.facade.ItemFacade;

/**
 * The ItemService calls persistent service locator to reach the
 * manager on the back end.
 */
public class ItemService
{
  private static Log log = LogFactory.getLog(ItemService.class);

  /**
   * Creates a new ItemService object.
   */
  public ItemService()
  {
  }


  /**
   * Get a particular item from the backend, with all questions.
   */
  public ItemFacade getItem(Long itemId, String agentId)
  {
    ItemFacade item = null;
    try
    {
      item =
        PersistenceService.getInstance().getItemFacadeQueries().
          getItem(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }

    return item;
  }

  /**
   * Delete a item
   */
  public void deleteItem(Long itemId, String agentId)
  {
    try
    {
      ItemFacade item= PersistenceService.getInstance().
        getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new RuntimeException("you are not allowed to delete item if you are not the owner");
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItem(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }


  /**
   * Delete itemtextset for an item, used for modify
   */
  public void deleteItemContent(Long itemId, String agentId)
  {
    try
    {
      ItemFacade item= PersistenceService.getInstance().
        getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new RuntimeException("you are not allowed to delete item if you are not the owner");
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItemContent(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }


  /**
   * Delete metadata for an item, used for modify
   * param:  itemid, label, agentId
   */
  public void deleteItemMetaData(Long itemId, String label, String agentId)
  {
    try
    {
      ItemFacade item= PersistenceService.getInstance().
        getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItemMetaData(itemId, label);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

   /**
   * Add metadata for an item, used for modify
   * param:  itemid, label, value, agentId
   */
  public void addItemMetaData(Long itemId, String label, String value, String agentId)
  {
    try
    {
      ItemFacade item= PersistenceService.getInstance().
        getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        addItemMetaData(itemId, label, value);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }




  /**
   * Save a question item.
   */
  public ItemFacade saveItem(ItemFacade item)
  {
    try
    {
      return PersistenceService.getInstance().getItemFacadeQueries().saveItem(item);
    }
    catch(Exception e)
    {
      log.error(e);

      return item;
    }
  }

  public ItemFacade getItem(String itemId) {
    try{
      return PersistenceService.getInstance().getItemFacadeQueries().
          getItem(new Long(itemId));
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public HashMap getItemsByKeyword(String keyword)
  {
    keyword="%" + keyword + "%";
    HashMap map= null;
      map= PersistenceService.getInstance().getItemFacadeQueries().getItemsByKeyword(keyword);
    return map;

  }

  public Long getItemTextId(Long publishedItemId) {
	    try{
	      return PersistenceService.getInstance().getItemFacadeQueries().
	      getItemTextId(publishedItemId);
	    }
	    catch(Exception e)
	    {
	      log.error(e); throw new RuntimeException(e);
	    }
  }
}
