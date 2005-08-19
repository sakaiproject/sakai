/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 1285 2005-08-19 02:05:48Z esmiley@stanford.edu $
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
package org.sakaiproject.tool.assessment.shared.impl.assessment;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.ItemFacade;

import org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import java.util.Map;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceException;
import org.sakaiproject.tool.assessment.services.ItemService;

// our API just uses our internal service. ItemFacade implements
// ItemDataIfc.  If we want, we can always replace this internal
// service and use its implementation as our own.

public class ItemServiceImpl implements ItemServiceAPI
{
 /**
 * Get a particular item from the backend, with all questions.
 *
 * @param itemId
 * @param agentId
 * @return
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
   * Delete a item
   *
   * @param itemId
   * @param agentId
   */
  public void deleteItem(Long itemId, String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItem() not yet implemented.");

  }

  /**
   * Delete itemtextset for an item, used for modify
   *
   * @param itemId
   * @param agentId
   */
  public void deleteItemContent(Long itemId, String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItem() not yet implemented.");

  }

  /**
   * Delete metadata for an item, used for modify
   * param:  itemid, label, agentId
   *
   * @param itemId
   * @param label
   * @param agentId
   */
  public void deleteItemMetaData(Long itemId, String label, String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItem() not yet implemented.");

  }

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
                              String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItem() not yet implemented.");

  }
  /**
   * Save item.
   * @param item interface
   * @return item interface
   */
  public ItemDataIfc saveItem(ItemDataIfc item)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveItem() not yet implemented.");
  }

  /**
   * Get item.
   * @param itemId
   * @return item interface
   */
  public ItemDataIfc getItem(String itemId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItem() not yet implemented.");
  }

  /**
   * Search for items.
   * @param keyword
   * @return Map of ItemDataIfcs with item idstrings as keys
   */

  public Map getItemsByKeyword(String keyword)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItemsByKeyword() not yet implemented.");
  }
}