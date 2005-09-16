/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.services;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.ItemFacade;

//import osid.assessment.Item;


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
      log.error(e); throw new Error(e);
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

      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItem(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
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

      // you are not allowed to delete item if you are not the owner

      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItemContent(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
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

      // you are not allowed to delete item if you are not the owner

      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItemMetaData(itemId, label);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
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

      // you are not allowed to delete item if you are not the owner

      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
      PersistenceService.getInstance().getItemFacadeQueries().
        addItemMetaData(itemId, label, value);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
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
//      throw new Error(e);

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
      log.error(e); throw new Error(e);
    }
  }

  public HashMap getItemsByKeyword(String keyword)
  {
    keyword="%" + keyword + "%";
    HashMap map= null;
      map= PersistenceService.getInstance().getItemFacadeQueries().getItemsByKeyword(keyword);
    return map;

  }


}
