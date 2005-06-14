package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A Item iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class ItemIteratorFacade
  //implements ItemIterator
{
  private Iterator itemIter;
  private int size = 0;

  /**
   * Creates a new ItemIteratorImpl object.
   *
   * @param pitems DOCUMENTATION PENDING
   */
  public ItemIteratorFacade(Collection pitems)
  {
    itemIter = pitems.iterator();
    this.size = pitems.size();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextItem()
    throws DataFacadeException
  {
    try{
      return itemIter.hasNext();
    }
    catch(Exception e){
      throw new DataFacadeException("No objects to return.");
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public ItemFacade nextItem()
    throws DataFacadeException
  {
    try
    {
      return (ItemFacade) itemIter.next();
    }
    catch(Exception e)
    {
      throw new DataFacadeException("No objects to return.");
    }
  }

  public int getSize(){
    return size;
  }

}
