/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.tool.assessment.ui.model;

import java.io.Serializable;

public class PagingModel implements Serializable
{
  private int firstItem;
  private int lastItem;
  private int numItems;
  private int totalItems;

  public int getFirstItem()
  {
    return firstItem;
  }
  public void setFirstItem(int firstItem)
  {
    this.firstItem = firstItem;
  }
  public int getLastItem()
  {
    return lastItem;
  }
  public void setLastItem(int lastItem)
  {
    this.lastItem = lastItem;
  }
  public int getNumItems()
  {
    return numItems;
  }
  public void setNumItems(int numItems)
  {
    this.numItems = numItems;
  }
  public int getTotalItems()
  {
    return totalItems;
  }
  public void setTotalItems(int totalItems)
  {
    this.totalItems = totalItems;
  }
}
