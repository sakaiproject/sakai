package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A Section iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class SectionIteratorFacade
{
  private Iterator sections;

  /**
   * Creates a new SectionIteratorImpl object.
   *
   * @param psections DOCUMENTATION PENDING
   */
  public SectionIteratorFacade(Collection psections)
  {
    sections = psections.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextSection()
    throws DataFacadeException
  {
    try{
      return sections.hasNext();
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
  public SectionFacade nextSection()
    throws DataFacadeException
  {
    try
    {
      return (SectionFacade) sections.next();
    }
    catch(Exception e)
    {
      throw new DataFacadeException("No objects to return.");
    }
  }
}
