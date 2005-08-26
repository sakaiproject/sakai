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
package org.sakaiproject.tool.assessment.shared.api.assessment;

import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

/**
 * The ItemServiceAPI declares a shared interface to get/set item
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
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