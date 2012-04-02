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

package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.osid.assessment.AssessmentException;
import org.osid.assessment.Item;

public class ItemIteratorImpl implements Serializable, org.osid.assessment.ItemIterator
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -2167985085297166661L;
private Iterator items;

  /**
   * Creates a new ItemIteratorImpl object.
   *
   * @param pitems DOCUMENTATION PENDING
   */
  public ItemIteratorImpl(Collection items)
  {
    this.items = items.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean hasNextItem()
    throws AssessmentException
  {
    return items.hasNext();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws AssessmentException DOCUMENTATION PENDING
   */
  public Item nextItem()
    throws AssessmentException
  {
    try
    {
      return (Item) items.next();
    }
    catch(Exception e)
    {
      throw new AssessmentException("No objects to return.");
    }
  }

}
