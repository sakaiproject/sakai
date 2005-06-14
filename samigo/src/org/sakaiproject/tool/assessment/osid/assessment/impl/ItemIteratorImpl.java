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
