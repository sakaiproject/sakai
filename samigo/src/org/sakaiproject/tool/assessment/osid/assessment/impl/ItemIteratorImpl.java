/**********************************************************************************
* $HeadURL$
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
package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.osid.assessment.AssessmentException;
import org.osid.assessment.Item;

public class ItemIteratorImpl implements Serializable, org.osid.assessment.ItemIterator
{
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
