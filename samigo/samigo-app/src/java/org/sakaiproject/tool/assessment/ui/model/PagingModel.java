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


package org.sakaiproject.tool.assessment.ui.model;

import java.io.Serializable;

public class PagingModel implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -8166345520085915986L;
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
