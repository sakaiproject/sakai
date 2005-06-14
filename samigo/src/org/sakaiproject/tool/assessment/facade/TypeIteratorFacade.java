package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A Section iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class TypeIteratorFacade
{
  private Iterator typeIter;

  /**
   * Creates a new SectionIteratorImpl object.
   *
   * @param psections DOCUMENTATION PENDING
   */
  public TypeIteratorFacade(Collection types)
  {
    typeIter = types.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextType()
    throws DataFacadeException
  {
    try{
      return typeIter.hasNext();
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
  public TypeFacade nextType()
    throws DataFacadeException
  {
    try
    {
      return (TypeFacade) typeIter.next();
    }
    catch(Exception e)
    {
      throw new DataFacadeException("No objects to return.");
    }
  }
}
