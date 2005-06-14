package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A AssessmentBase iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AssessmentBaseIteratorFacade
  //implements AssessmentBaseIterator
{
  private Iterator assessmentBaseIter;

  /**
   * Creates a new AssessmentBaseIteratorImpl object.
   *
   * @param passessmentBases DOCUMENTATION PENDING
   */
  public AssessmentBaseIteratorFacade(Collection passessmentBases)
  {
    assessmentBaseIter = passessmentBases.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextAssessmentBase()
    throws DataFacadeException
  {
    try{
      return assessmentBaseIter.hasNext();
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
  public AssessmentBaseFacade nextAssessmentBase()
    throws DataFacadeException
  {
    try
    {
      return (AssessmentBaseFacade) assessmentBaseIter.next();
    }
    catch(Exception e)
    {
      throw new DataFacadeException("No objects to return.");
    }
  }
}
